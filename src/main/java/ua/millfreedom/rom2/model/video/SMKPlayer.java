package ua.millfreedom.rom2.model.video;

import lombok.Getter;
import lombok.SneakyThrows;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.color.RGB32;
import ua.millfreedom.rom2.model.render.Renderer;
import ua.millfreedom.rom2.model.sound.Sound;
import ua.millfreedom.rom2.model.sound.SoundSystem;
import ua.millfreedom.rom2.model.sound.WaveFormat;
import ua.millfreedom.rom2.res.ResInHeap;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;


public class SMKPlayer implements AutoCloseable {
    private static final String REGISTRY_EXTENSION = ".reg";
    private static final String REGISTRY_COMMON_SECTION = "Common";
    private static final String REGISTRY_FADING_COUNT_KEY = "nFadings";
    private static final String REGISTRY_PANARAMING_COUNT_KEY = "nPanaramings";
    private static final String REGISTRY_START_FRAME_KEY = "startframe";
    private static final String REGISTRY_END_FRAME_KEY = "endframe";
    private static final String REGISTRY_START_FADE_KEY = "startfade";
    private static final String REGISTRY_END_FADE_KEY = "endfade";
    private static final String REGISTRY_START_X_KEY = "startx";
    private static final String REGISTRY_START_Y_KEY = "starty";
    private static final String REGISTRY_STEP_X_KEY = "stepx";
    private static final String REGISTRY_STEP_Y_KEY = "stepy";
    private static final int NATIVE_LETTERBOX_WIDTH = 0x280;
    private static final int NATIVE_LETTERBOX_HEIGHT = 0x168;
    private static final int NATIVE_FULL_HEIGHT = 0x1E0;
    private static final int NATIVE_LETTERBOX_TOP_OFFSET = 0x3C;

    private final AtomicBoolean nextFrameReady = new AtomicBoolean(false);
    //0x1c
    private final List<Fading> fadings = new ArrayList<>();
    //0x28
    private final List<Panaraming> panaramings = new ArrayList<>();
    @Getter
    private Smacker.SMKAudioInfo audioInfo;
    @Getter
    private Smacker.SMKVideoInfo videoInfo;

    private Smacker smacker;
    private double microsecondsPerFrame;
    private int logicalFrameCount;
    private int audioTrackIndex;
    private Sound audioTrackSound;
    private long frameDurationNanos;
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);
    private volatile Thread playbackThread;
    //0x18
    private int nextFadingIndex;
    //0x24
    private int nextPanaramingIndex;
    //0x2c
    private int startx;
    //0x30
    private int starty;
    //0x34
    private boolean paletteFadeActive;
    //0x33c
    private float currentFade = 1.0f;
    //0x340
    private float fadeStep;
    //0x344
    private int fadeFramesRemaining;
    //0x348
    private int sourceX;
    //0x34c
    private int sourceY;
    //0x350
    private int sourceWidth;
    //0x354
    private int sourceHeight;
    //0x358
    private int destinationX;
    //0x35c
    private int destinationY;
    //0x360
    private int panaramingStepX;
    //0x364
    private int panaramingStepY;
    //0x368
    private boolean doubleSizeBlitActive;
    // Java-only compact presentation palette reused while a native fade is active.
    private final int[] fadedPalette = new int[256];

    /**
     * Opens the selected Smacker file and enables canonical integer-selector video decoding.
     * Native support extracted from SMKPlayer::SMKPlayer @004C3679,
     * SMKPlayer::OpenWithRegistry @004C3FF1, and
     * SMKPlayer::OpenSmackerFile @004C3B6E.
     * Fully ported support helper.
     */
    public SMKPlayer(String smk) throws Exception {
        smacker = Smacker.fromByteBuffer(Globals.gameFileManager.get(smk), Smacker.MODE_MEMORY);
        loadCutsceneRegistry(smk);
        microsecondsPerFrame = smacker.infoAll().microsecondsPerFrame();
        logicalFrameCount = smacker.infoAll().frameCount();
        audioInfo = smacker.infoAudio();
        audioTrackIndex = audioInfo.trackMask() == 0 ? -1 : Integer.numberOfTrailingZeros(audioInfo.trackMask());
        if (audioTrackIndex >= 0) {
            audioTrackSound = decodeAudioTrackSound(audioTrackIndex);
        }
        smacker.enableVideo(true);
        videoInfo = smacker.infoVideo();
        initializeNativeDisplayState();
        frameDurationNanos = Math.max(1L, Math.round(microsecondsPerFrame * 1_000.0));
    }

    /**
     * Native support extracted from CMainWindow::playSmkCutsceneSegment @0048FC2E calls to
     * SMKPlayer::SetDestinationPosition @00493DF0.
     * Fully ported.
     */
    public void setCutsceneWindowOrigin(int left, int top) {
        setDestinationPosition(
                left,
                videoInfo.height() == NATIVE_FULL_HEIGHT ? top : top + NATIVE_LETTERBOX_TOP_OFFSET
        );
    }

    /**
     * Native: SMKPlayer::SetDestinationPosition @00493DF0.
     * Fully ported.
     */
    public void setDestinationPosition(int x, int y) {
        destinationX = x;
        destinationY = y;
    }

    /**
     * not ported. Starts playback from the beginning of the Smacker stream.
     */
    public void play() {
        stop();
        waitForPlaybackThread();
        stopRequested.set(false);
        smacker.rewind();
        resetCutsceneEffects();
        playbackThread = Thread.startVirtualThread(this::playLoop);
    }

    /**
     * Plays the Smacker stream synchronously into the logical game renderer.
     * Native support extracted from CMainWindow::playSmkCutsceneSegment @0048FC2E and
     * SMKPlayer::RenderFrame @004C399A.
     */
    public boolean playBlocking(Renderer renderer, Runnable presentFrame, BooleanSupplier shouldAbortPlayback) throws Exception {
        Objects.requireNonNull(renderer, "renderer");
        Objects.requireNonNull(presentFrame, "presentFrame");
        Objects.requireNonNull(shouldAbortPlayback, "shouldAbortPlayback");

        stop();
        waitForPlaybackThread();
        stopRequested.set(false);
        smacker.rewind();
        resetCutsceneEffects();
        nextFrameReady.set(false);
        playbackThread = Thread.currentThread();

        try {
            if (audioTrackSound != null) {
                audioTrackSound.play(Globals.soundPreferences.sfxVolume, false, (byte) 0, 0);
            }

            long nextFrameDeadlineNanos = System.nanoTime();
            while (!shouldAbortPlayback.getAsBoolean() && !stopRequested.get()) {
                if (!waitForFrameDeadline(nextFrameDeadlineNanos, () ->
                        shouldAbortPlayback.getAsBoolean() || stopRequested.get())) {
                    return false;
                }

                int status = smacker.next();
                if (status == Smacker.DONE) {
                    return true;
                }
                updatePanaramingForCurrentFrame();
                float paletteFade = advanceCurrentFramePaletteFade();
                renderCurrentFrame(renderer, paletteFade);
                presentFrame.run();
                if (status == Smacker.LAST) {
                    // Native returns after blitting the final frame without SmackNextFrame.
                    return true;
                }
                advancePanaramingBorders();
                nextFrameDeadlineNanos += frameDurationNanos;
            }
            return false;
        } finally {
            stopAudioPlayback();
            nextFrameReady.set(false);
            stopRequested.set(false);
            if (Thread.currentThread() == playbackThread) {
                playbackThread = null;
            }
        }
    }

    /**
     * not ported. Returns whether a decoded frame is ready for presentation.
     */
    public boolean hasNextFrame() {
        return nextFrameReady.get();
    }

    /**
     * not ported. Marks the current decoded frame as consumed by the renderer.
     */
    public void consumeFrame() {
        nextFrameReady.set(false);
    }

    /**
     * not ported. Decodes video frames on a background virtual thread and schedules them by frame time.
     */
    @SneakyThrows
    private void playLoop() {
        try {
            if (audioTrackSound != null) {
                audioTrackSound.play(Globals.soundPreferences.sfxVolume, false, (byte) 0, 0);
            }
            long nextFrameDeadlineNanos = System.nanoTime();
            while (!stopRequested.get()) {
                if (!waitForFrameDeadline(nextFrameDeadlineNanos, stopRequested::get)) {
                    return;
                }

                int status = smacker.next();
                if (status == Smacker.DONE) {
                    return;
                }
                advanceCurrentFramePaletteFade();
                nextFrameReady.set(true);
                nextFrameDeadlineNanos += frameDurationNanos;
            }
        } finally {
            stopAudioPlayback();
            nextFrameReady.set(false);
            if (Thread.currentThread() == playbackThread) {
                playbackThread = null;
            }
        }
    }

    /**
     * Waits until the next frame deadline while still honoring native-style input aborts.
     * not ported.
     */
    private static boolean waitForFrameDeadline(long deadlineNanos, BooleanSupplier shouldAbortPlayback) {
        while (!shouldAbortPlayback.getAsBoolean()) {
            long remainingNanos = deadlineNanos - System.nanoTime();
            if (remainingNanos <= 0) {
                return true;
            }
            LockSupport.parkNanos(Math.min(remainingNanos, 5_000_000L));
        }
        return false;
    }

    /**
     * Native support extracted from SMKPlayer::RenderFrame @004C399A and
     * SMKPlayer::UpdateFadingForCurrentFrame @004C4561. Advances presentation-only palette state without creating a
     * second full-frame pixel plane.
     */
    private float advanceCurrentFramePaletteFade() {
        updateFadingForCurrentFrame();
        return advancePaletteFadeIfActive();
    }

    /**
     * Native support extracted from SMKPlayer::OpenWithRegistry @004C3FF1 fade and panaraming registry reads.
     */
    private void loadCutsceneRegistry(String smk) throws Exception {
        ResInHeap registry = ResInHeap.load(toCutsceneRegistryName(smk));
        startx = registry.getInt(REGISTRY_COMMON_SECTION, REGISTRY_START_X_KEY, 0);
        starty = registry.getInt(REGISTRY_COMMON_SECTION, REGISTRY_START_Y_KEY, 0);
        int fadingCount = registry.getInt(REGISTRY_COMMON_SECTION, REGISTRY_FADING_COUNT_KEY, 0);
        fadings.clear();
        for (int i = 0; i < fadingCount; i++) {
            String section = "Fading%d".formatted(i + 1);
            Fading fading = new Fading();
            fading.startframe = registry.getInt(section, REGISTRY_START_FRAME_KEY, 0);
            fading.endframe = registry.getInt(section, REGISTRY_END_FRAME_KEY, 0);
            fading.startfade = (float) registry.getDouble(section, REGISTRY_START_FADE_KEY, 0.0);
            fading.endfade = (float) registry.getDouble(section, REGISTRY_END_FADE_KEY, 0.0);
            fadings.add(fading);
        }

        int panaramingCount = registry.getInt(REGISTRY_COMMON_SECTION, REGISTRY_PANARAMING_COUNT_KEY, 0);
        panaramings.clear();
        for (int i = 0; i < panaramingCount; i++) {
            String section = "Panaraming%d".formatted(i + 1);
            Panaraming panaraming = new Panaraming();
            panaraming.startframe = registry.getInt(section, REGISTRY_START_FRAME_KEY, 0);
            panaraming.endframe = registry.getInt(section, REGISTRY_END_FRAME_KEY, 0);
            panaraming.stepx = registry.getInt(section, REGISTRY_STEP_X_KEY, 0);
            panaraming.stepy = registry.getInt(section, REGISTRY_STEP_Y_KEY, 0);
            panaramings.add(panaraming);
        }
    }

    /**
     * Native support extracted from SMKPlayer::OpenWithRegistry @004C3FF1 `.smk` to `.reg` path conversion.
     */
    private static String toCutsceneRegistryName(String smk) {
        String registryName = smk.trim();
        int extensionIndex = registryName.lastIndexOf('.');
        registryName = (extensionIndex < 0 ? "" : registryName.substring(0, extensionIndex)) + REGISTRY_EXTENSION;
        return registryName.trim();
    }

    /**
     * Native support extracted from SMKPlayer::OpenWithRegistry @004C3FF1 playback-state initialization.
     */
    private void resetCutsceneEffects() {
        nextFadingIndex = 0;
        nextPanaramingIndex = 0;
        panaramingStepX = 0;
        panaramingStepY = 0;
        initializeNativeDisplayState();
        stopPaletteFade();
    }

    /**
     * Native: SMKPlayer::UpdateFadingForCurrentFrame @004C4561.
     */
    private void updateFadingForCurrentFrame() {
        if (nextFadingIndex < fadings.size()) {
            Fading fading = fadings.get(nextFadingIndex);
            if (fading.startframe == smacker.infoAll().frame()) {
                startPaletteFade(fading.endframe - fading.startframe, fading.startfade, fading.endfade);
                nextFadingIndex++;
            }
        }
    }

    /**
     * Native: SMKPlayer::UpdatePanaramingForCurrentFrame @004C4618.
     */
    private void updatePanaramingForCurrentFrame() {
        if (nextPanaramingIndex < panaramings.size()) {
            Panaraming panaraming = panaramings.get(nextPanaramingIndex);
            int frame = smacker.infoAll().frame();
            if (panaraming.startframe == frame) {
                panaramingStepX = panaraming.stepx;
                panaramingStepY = panaraming.stepy;
            } else if (panaraming.endframe == frame) {
                panaramingStepX = 0;
                panaramingStepY = 0;
                nextPanaramingIndex++;
            }
        }
    }

    /**
     * Native: SMKPlayer::AdvancePanaramingBorders @004C46D7.
     */
    private void advancePanaramingBorders() {
        sourceX += panaramingStepX;
        sourceY += panaramingStepY;
    }

    /**
     * Native: SMKPlayer::StartPaletteFade @004C4840.
     */
    private void startPaletteFade(int frameCount, float startFade, float endFade) {
        fadeFramesRemaining = frameCount;
        currentFade = startFade;
        fadeStep = (endFade - startFade) / frameCount;
        paletteFadeActive = true;
    }

    /**
     * Native: SMKPlayer::ApplyActivePaletteFade @004C38D1.
     */
    private float advancePaletteFadeIfActive() {
        if (!paletteFadeActive) {
            return Float.NaN;
        }
        currentFade += fadeStep;
        float frameFade = currentFade;
        fadeFramesRemaining--;
        if (fadeFramesRemaining == 0) {
            stopPaletteFade();
        }
        return frameFade;
    }

    /**
     * Native support extracted from SMKPlayer::ApplyActivePaletteFade @004C38D1 palette component multiplication.
     */
    private static int fadePaletteComponent(int component, float fade) {
        return Byte.toUnsignedInt((byte) (int) (component * fade));
    }

    /**
     * Native: SMKPlayer::StopPaletteFade @004C4890.
     */
    private void stopPaletteFade() {
        paletteFadeActive = false;
    }

    /**
     * Draws the current selector-plane source viewport into the logical software surface, resolving the active palette
     * directly during the scaled blit.
     * Native support extracted from SMKPlayer::RenderFrame @004C399A.
     */
    private void renderCurrentFrame(Renderer renderer, float paletteFade) {
        int destWidth = doubleSizeBlitActive ? sourceWidth << 1 : sourceWidth;
        int destHeight = doubleSizeBlitActive ? sourceHeight << 1 : sourceHeight;

        renderer.clearSurface();
        renderer.blitOpaqueIndexedScaled(
                smacker.getVideo(),
                videoInfo.width(),
                videoInfo.height(),
                sourceX,
                sourceY,
                sourceWidth,
                sourceHeight,
                presentationPalette(paletteFade),
                destinationX,
                destinationY,
                destWidth,
                destHeight
        );
    }

    /**
     * Native support extracted from SMKPlayer::ApplyActivePaletteFade @004C38D1. Returns the canonical decoded palette
     * unchanged when no fade is active, or a reusable 256-color faded palette without retaining another frame.
     */
    private int[] presentationPalette(float paletteFade) {
        int[] palette = smacker.getPalette();
        if (Float.isNaN(paletteFade)) {
            return palette;
        }

        for (int paletteIndex = 0; paletteIndex < palette.length; paletteIndex++) {
            int color = palette[paletteIndex];
            fadedPalette[paletteIndex] = RGB32.from(
                    fadePaletteComponent(RGB32.r(color), paletteFade),
                    fadePaletteComponent(RGB32.g(color), paletteFade),
                    fadePaletteComponent(RGB32.b(color), paletteFade)
            );
        }
        return fadedPalette;
    }

    /**
     * Native support extracted from CMainWindow::playSmkCutsceneSegment @0048FC2E and
     * SMKPlayer::OpenWithRegistry @004C3FF1 / SMKPlayer::OpenSmackerFile @004C3B6E display-state initialization.
     */
    private void initializeNativeDisplayState() {
        int nativeSourceWidth;
        int nativeSourceHeight;
        if (videoInfo.height() == NATIVE_FULL_HEIGHT) {
            nativeSourceWidth = videoInfo.width();
            nativeSourceHeight = videoInfo.height();
        } else {
            nativeSourceWidth = NATIVE_LETTERBOX_WIDTH;
            nativeSourceHeight = NATIVE_LETTERBOX_HEIGHT;
        }
        doubleSizeBlitActive = usesNativeDoubleSizeBlit(nativeSourceWidth, nativeSourceHeight);
        if (doubleSizeBlitActive) {
            nativeSourceWidth >>>= 1;
            nativeSourceHeight >>>= 1;
        }
        setBorders(startx, starty, nativeSourceWidth, nativeSourceHeight);
    }

    /**
     * Native support extracted from SMKPlayer::OpenSmackerFile @004C3B6E double-size blit setup.
     */
    private boolean usesNativeDoubleSizeBlit(int nativeSourceWidth, int nativeSourceHeight) {
        return videoInfo.width() * 2 <= nativeSourceWidth || videoInfo.height() * 2 <= nativeSourceHeight;
    }

    /**
     * Native: SMKPlayer::SetBorders @004C4800.
     */
    private void setBorders(int x1, int y1, int x2, int y2) {
        sourceX = x1;
        sourceY = y1;
        sourceWidth = x2;
        sourceHeight = y2;
    }

    /**
     * Mirrors the OpenAL format limits already hard-coded inside {@link SoundSystem}.
     * not ported.
     */
    private static boolean isSupportedSoundFormat(int channels, int bitsPerSample) {
        return (channels == 1 || channels == 2) && (bitsPerSample == 8 || bitsPerSample == 16);
    }

    /**
     * Appends the current frame's decoded SMK audio bytes to the aggregate PCM stream.
     * not ported.
     */
    private void appendAudioChunk(ByteArrayOutputStream pcm, int track) {
        int size = smacker.getAudioSize(track);
        if (size <= 0) {
            return;
        }
        pcm.write(smacker.getAudio(track), 0, size);
    }

    /**
     * not ported. Fully decodes one Smacker audio track into a transient Sound instance.
     */
    private Sound decodeAudioTrackSound(int track) throws Exception {
        int channels = audioInfo.channels()[track];
        int bitsPerSample = audioInfo.bitDepth()[track];
        int sampleRate = Math.toIntExact(audioInfo.audioRate()[track]);
        if (!isSupportedSoundFormat(channels, bitsPerSample)) {
            System.err.println("Skipping unsupported SMK audio format: channels=" + channels + ", bits=" + bitsPerSample);
            return null;
        }
        smacker.enableAll(1 << track);
        ByteArrayOutputStream pcm = new ByteArrayOutputStream();
        smacker.first();
        appendAudioChunk(pcm, track);
        for (int frame = 1; frame < logicalFrameCount; frame++) {
            smacker.next();
            appendAudioChunk(pcm, track);
        }

        byte[] pcmBytes = pcm.toByteArray();
        if (pcmBytes.length == 0) {
            return null;
        }

        Sound sound = new Sound("smk-audio");
        int blockAlign = channels * (bitsPerSample / 8);
        sound.format = new WaveFormat(
                1,
                channels,
                sampleRate,
                sampleRate * blockAlign,
                blockAlign,
                bitsPerSample,
                null
        );
        sound.audioData = ByteBuffer.wrap(pcmBytes).order(ByteOrder.LITTLE_ENDIAN);
        sound.loaded = true;
        return sound;
    }

    /**
     * Requests active video playback to stop.
     * not ported.
     */
    public void stop() {
        stopRequested.set(true);
    }

    /**
     * Waits for the active playback loop to release the decoder before restart or close.
     * not ported.
     */
    private void waitForPlaybackThread() {
        Thread thread = playbackThread;
        if (thread == null || thread == Thread.currentThread()) {
            return;
        }
        while (thread.isAlive()) {
            LockSupport.parkNanos(1_000_000L);
        }
        if (playbackThread == thread) {
            playbackThread = null;
        }
    }

    /**
     * Stops the decoded SMK audio track if one was prepared.
     * not ported.
     */
    private void stopAudioPlayback() {
        if (audioTrackSound != null) {
            audioTrackSound.stop();
        }
    }

    /**
     * Stops playback and closes the underlying Smacker decoder state.
     * Native support extracted from SMKPlayer::ClosePlaybackState @004C3D85 and
     * SMKPlayer::CloseWithRegistry @004C4543.
     */
    @Override
    public void close() {
        stop();
        waitForPlaybackThread();
        stopAudioPlayback();
        nextFrameReady.set(false);
        if (smacker != null) {
            smacker.close();
            smacker = null;
        }
    }

}
