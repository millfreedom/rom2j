package ua.millfreedom.rom2.model.sound;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.enums.MessageCodes;

import java.util.ArrayList;
import java.util.List;

/**
 * Native class: MusicPlayer.
 * Purpose: music-playback controller used by the main window and sound preferences dialog.
 * Implementation note: keep this API aligned with native `MusicPlayer`, but route actual playback through {@link SoundSystem}.
 */
public class MusicPlayer implements MfcSerializable {
    public static final int PLAYBACK_STATE_ACTIVE = 1;
    public static final int PLAYBACK_STATE_FADING = 2;
    public static final int PLAYBACK_STATE_STOPPED = 3;
    public static final int PLAYBACK_STATE_NO_TRACK = 4;
    public static final int PLAYBACK_STATE_UNAVAILABLE = 5;
    private static final byte DEFAULT_MUSIC_PRIORITY = 0;

    //0x08
    public int nextUpdateTick;
    //0x0c
    public boolean playbackStopped;
    //0x10
    public boolean trackLoaded;
    //0x14
    public boolean fadeOutActive;
    //0x18
    public boolean queuedTrackSwitchActive;
    //0x1c
    public int queuedTrackIndex = -1;
    //0x20
    public int randomOrderMask;
    //0x24
    public boolean firstHalfRefillPending;
    //0x28
    public boolean secondHalfRefillPending;
    //0x2c Native CDWordArray playlist order.
    public final List<Integer> playlistOrder = new ArrayList<>();
    //0x40 Native CStringArray music playlist.
    public final List<String> musicFileNames = new ArrayList<>();
    //0x54
    public int bufferBytes;
    //0x58
    public int currentTrackIndex = -1;
    //0x5c
    public int updateStepMs;
    //0x60
    public int fadeWindowMs;
    //0x64
    public int fadeFloorMagnitude;
    //0x68
    public boolean playbackProgressNotificationsEnabled;
    //0x6c Native flag set when a stream refill crosses into a new track.
    public boolean refillCrossedTrackBoundary;
    //0x70
    public int playbackElapsedMs;
    //0x74 Native fade-start volume captured by MusicPlayer::BeginFadeOut.
    public int fadeStartVolume;
    //0x78 Native DirectSound buffer byte offset for the next track boundary, or bufferBytes when no boundary is pending.
    public int trackBoundaryBufferOffset;
    //0x80 Java Sound replacing native CGameFile currentTrackFile pointer.
    public Sound currentTrackSound;
    //0x9c Java/OpenAL availability flag replacing native pIDirectSoundBuffer pointer.
    public final boolean playbackBackendAvailable;
    // Java support, not a native field.
    public int backendVolume;

    /**
     * vtbl +0x08: CObject::Serialize @00401970.
     */
    @Override
    public void serialize(CArchive ar) {
        // Native CObject::Serialize is a no-op.
    }

    /**
     * Native: MusicPlayer::MusicPlayer @00459ACD.
     * Fully ported at the Java/OpenAL boundary. Java mirrors the native DirectSound-buffer availability branch and
     * initializes controller state only when the playback backend was created successfully.
     */
    public MusicPlayer(int bufferBytes) {
        this.bufferBytes = bufferBytes;
        playbackBackendAvailable = SoundSystem.isInitialized();
        if (!playbackBackendAvailable) {
            return;
        }
        playbackStopped = true;
        randomOrderMask = 1;
        trackBoundaryBufferOffset = bufferBytes;
    }

    /**
     * Native: MusicPlayer::GetMusicFileNames @00450240.
     * Fully ported. Native returns the backing CStringArray at +0x40.
     */
    public List<String> getMusicFileNames() {
        return musicFileNames;
    }

    /**
     * Native: MusicPlayer::GetMusicFileCount @004502E0.
     * Fully ported. Native returns the backing CStringArray size at +0x40.
     */
    public int getMusicFileCount() {
        return musicFileNames.size();
    }

    /**
     * Native: MusicPlayer::GetMusicFileNameAt @004502C0.
     * Fully ported.
     */
    public String getMusicFileNameAt(int trackIndex) {
        return musicFileNames.get(trackIndex);
    }

    /**
     * Native: MusicPlayer::GetCurrentTrackIndex @004502A0.
     * Fully ported. Native resolves the active entry through the `CDWordArray` at +0x2C.
     */
    public int getCurrentTrackIndex() {
        return playlistOrder.get(currentTrackIndex);
    }

    /**
     * Native: MusicPlayer::GetVolume @004501A0.
     * Fully ported.
     */
    public int getVolume() {
        return backendVolume;
    }

    /**
     * Native: MusicPlayer::GetPlaybackState @00450150.
     * Fully ported at the Java/OpenAL playback boundary. Native reports state from DirectSound-buffer availability,
     * trackLoaded, playbackStopped, and fadeOutActive without querying live playback status.
     */
    public int getPlaybackState() {
        if (!isPlaybackBackendAvailable()) {
            return PLAYBACK_STATE_UNAVAILABLE;
        }
        if (!trackLoaded) {
            return PLAYBACK_STATE_NO_TRACK;
        }
        if (playbackStopped) {
            return PLAYBACK_STATE_STOPPED;
        }
        if (fadeOutActive) {
            return PLAYBACK_STATE_FADING;
        }
        return PLAYBACK_STATE_ACTIVE;
    }

    /**
     * Native: MusicPlayer::SetPlaybackProgressNotificationsEnabled @00450260.
     * Fully ported.
     */
    public void setPlaybackProgressNotificationsEnabled(boolean enabled) {
        playbackProgressNotificationsEnabled = enabled;
    }

    /**
     * Native: MusicPlayer::SetRandomOrderMask @00459FB5.
     * Fully ported.
     */
    public void setRandomOrderMask(int randomOrderMask) {
        if (!isPlaybackBackendAvailable()) {
            return;
        }
        this.randomOrderMask = randomOrderMask;
        queuedTrackSwitchActive = false;
    }

    /**
     * Native: MusicPlayer::SetVolume @004501E0.
     * Java-normalized at the playback boundary: native signed DirectSound volumes are represented as 0..100 percent values.
     */
    public void setVolume(int volume) {
        backendVolume = SoundPreferences.clampVolume(volume);
        if (currentTrackSound != null) {
            SoundSystem.get().setSoundVolume(currentTrackSound, backendVolume);
        }
    }

    /**
     * Native: MusicPlayer::SetFadeOutActive @00450280.
     * Fully ported.
     */
    public void setFadeOutActive(boolean fadeActive) {
        fadeOutActive = fadeActive;
    }

    /**
     * Native: MusicPlayer::SelectTrack @0045A030.
     * Fully ported at the Java/OpenAL boundary. Java loads the whole selected sound eagerly, replacing native
     * DirectSound half-buffer priming while preserving the selected-track state reset.
     */
    public void selectTrack(int trackIndex) {
        currentTrackIndex = trackIndex;
        firstHalfRefillPending = false;
        secondHalfRefillPending = true;
        loadTrack(currentTrackIndex);
        playbackElapsedMs = 0;
        trackBoundaryBufferOffset = bufferBytes;
    }

    /**
     * Native: MusicPlayer::Play @00459EC5.
     * Fully ported at the Java/OpenAL playback boundary. Native resumes from the current DirectSound cursor after
     * StopPlayback; Java preserves that cursor as playbackElapsedMs while stopped.
     */
    public void play() {
        if (!isMusicEnabled() || !isPlaybackBackendAvailable() || !trackLoaded) {
            return;
        }
        refreshUpdateStepMs();
        int resumePositionMs = playbackStopped ? playbackElapsedMs : 0;
        if (playbackStopped) {
            schedulePlaybackUpdate();
        }
        playbackStopped = false;
        if (!SoundSystem.get().isSoundPlaying(currentTrackSound)) {
            SoundSystem.get().playGlobal(currentTrackSound, backendVolume, true, DEFAULT_MUSIC_PRIORITY, 0, resumePositionMs);
        }
    }

    /**
     * Native: MusicPlayer::StopPlayback @00459F2F.
     * Fully ported at the Java/OpenAL playback boundary. Native preserves the DirectSound cursor on stop and hot-swaps
     * queued tracks without forcing playback to start when it was already stopped.
     */
    public void stopPlayback() {
        if (!isPlaybackBackendAvailable() || !trackLoaded) {
            return;
        }
        if (queuedTrackSwitchActive) {
            boolean resumePlayback = !playbackStopped;
            fadeOutActive = false;
            int queuedTrackIndex = this.queuedTrackIndex;
            stopCurrentTrackPlayback();
            selectTrack(queuedTrackIndex);
            setVolume(Globals.soundPreferences.musicVolume);
            queuedTrackSwitchActive = true;
            if (resumePlayback && isMusicEnabled()) {
                play();
            }
            return;
        }
        clearMusicTracker();
        playbackElapsedMs = getCurrentPositionMs();
        playbackStopped = true;
        stopCurrentTrackPlayback();
    }

    /**
     * Native: MusicPlayer::BeginFadeOut @00459D68.
     * Fully ported at the Java/OpenAL boundary. Native clamps the fade window to the requested delay, the remaining
     * loaded-track duration, and any pending track-boundary buffer offset.
     */
    public void beginFadeOut(int fadeDelayMs, int fadeFloorMagnitude) {
        if (!isPlaybackBackendAvailable() || !trackLoaded) {
            return;
        }
        this.fadeFloorMagnitude = fadeFloorMagnitude;
        fadeStartVolume = backendVolume;
        fadeWindowMs = Math.min(Math.min(fadeDelayMs, getRemainingTrackMs()), getPendingTrackBoundaryMs());
        fadeOutActive = true;
    }

    /**
     * Native support extracted from MusicPlayer::BeginFadeOut @00459D68.
     */
    private int getRemainingTrackMs() {
        return getCurrentTrackDurationMs() - getCurrentPositionMs();
    }

    /**
     * Native support extracted from MusicPlayer::BeginFadeOut @00459D68 track-boundary clamp.
     */
    private int getPendingTrackBoundaryMs() {
        if (trackBoundaryBufferOffset == bufferBytes) {
            return Integer.MAX_VALUE;
        }
        int currentPosition = getCurrentPosition();
        int bytesToBoundary = trackBoundaryBufferOffset < currentPosition
                ? bufferBytes - (currentPosition - trackBoundaryBufferOffset)
                : trackBoundaryBufferOffset - currentPosition;
        return bufferBytesToMs(bytesToBoundary);
    }

    /**
     * Native support extracted from MusicPlayer::BeginFadeOut @00459D68 byte-count-to-time conversion.
     */
    private int bufferBytesToMs(int byteCount) {
        return (int) (((long) byteCount * 1000L) / currentTrackSound.format.avgBytesPerSec());
    }

    /**
     * Native support extracted from MusicPlayer::BeginFadeOut @00459D68 and
     * MusicPlayer::UpdateStreamingPlayback @00459313.
     */
    private int getCurrentTrackDurationMs() {
        long trackDurationMs = ((long) currentTrackSound.audioData.remaining() * 1000L)
                / currentTrackSound.format.avgBytesPerSec();
        return (int) trackDurationMs;
    }

    /**
     * Native: MusicPlayer::QueueTrackAfterFade @00459FE1.
     * Fully ported.
     */
    public void queueTrackAfterFade(int trackIndex) {
        queuedTrackSwitchActive = true;
        beginFadeOut(2000, 8000);
        queuedTrackIndex = trackIndex;
    }

    /**
     * Native: MusicPlayer::SetMusicFileNames @00459A5F.
     * Fully ported at the Java collection boundary. Native gates on the DirectSound buffer, copies the playlist,
     * resizes the CDWordArray at +0x2C, selects `rand() % size`, and always resets `g_CurrentMusicTrack`.
     */
    public void setMusicFileNames(List<String> newMusicFileNames) {
        if (isPlaybackBackendAvailable()) {
            stopPlayback();
            musicFileNames.clear();
            musicFileNames.addAll(newMusicFileNames);
            setPlaylistOrderSize(musicFileNames.size());
            int startTrackIndex = Utils.randExclusive(0, musicFileNames.size());
            selectTrack(startTrackIndex);
        }
        Globals.currentMusicTrack = -1;
    }

    /**
     * Native support extracted from MusicPlayer::SetMusicFileNames @00459A5F and CDWordArray::SetSize @0059F06D.
     */
    private void setPlaylistOrderSize(int newSize) {
        while (playlistOrder.size() > newSize) {
            playlistOrder.remove(playlistOrder.size() - 1);
        }
        while (playlistOrder.size() < newSize) {
            playlistOrder.add(0);
        }
    }

    /**
     * Native: MusicPlayer::SchedulePlaybackUpdate @00459020.
     * Full port.
     */
    public void schedulePlaybackUpdate() {
        Globals.mainWindow.setMusicTracker(MusicPlayer::updateMusicStreaming);
        nextUpdateTick = (int) System.currentTimeMillis() + updateStepMs;
    }

    /**
     * Native: MusicPlayer::ClearMusicTracker @0045904E.
     * Full port.
     */
    public void clearMusicTracker() {
        Globals.mainWindow.setMusicTracker(null);
    }

    /**
     * Native: MusicPlayer::updateMusicStreaming @004592FC.
     * Full port.
     */
    public static void updateMusicStreaming() {
        Globals.mainWindow.musicPlayer.updateStreamingPlayback((int) System.currentTimeMillis());
    }

    /**
     * Native: MusicPlayer::ComputeFadeStepVolume @0045922C.
     * Java-normalized support: preserves native fade timing while applying it to 0..100 percent volumes.
     */
    public int computeFadeStepVolume(int currentVolume, int deltaMs) {
        int clampedCurrentVolume = SoundPreferences.clampVolume(currentVolume);
        if (fadeWindowMs <= 0 || deltaMs <= 0) {
            return SoundPreferences.VOLUME_MIN;
        }
        int fadeStart = Math.max(1, SoundPreferences.clampVolume(fadeStartVolume));
        int fadeStep = Math.max(1, (int) Math.ceil((double) fadeStart * (double) deltaMs / (double) fadeWindowMs));
        return SoundPreferences.clampVolume(clampedCurrentVolume - fadeStep);
    }

    /**
     * Native: MusicPlayer::UpdateStreamingPlayback @00459313.
     * Java-normalized at the fade-volume boundary. Java keeps the native tick gate, progress notification, and
     * completed-stream rollover selection while replacing DirectSound half-buffer copies with eager Sound reloads.
     */
    public void updateStreamingPlayback(int currentTimeMs) {
        if (nextUpdateTick > currentTimeMs) {
            return;
        }
        nextUpdateTick += updateStepMs;
        playbackElapsedMs += updateStepMs;
        if (playbackProgressNotificationsEnabled) {
            Globals.mainWindow.postMessage(
                    MessageCodes.MUSIC_PLAYBACK_PROGRESS_NOTIFICATION,
                    currentTrackIndex,
                    getElapsedSeconds()
            );
        }
        advanceTrackAfterElapsedDuration();
        if (fadeOutActive) {
            int currentVolume = backendVolume;
            if (currentVolume <= SoundPreferences.VOLUME_MIN) {
                stopPlayback();
            } else {
                int nextVolume = computeFadeStepVolume(currentVolume, updateStepMs);
                setVolume(nextVolume);
                if (nextVolume <= SoundPreferences.VOLUME_MIN) {
                    stopPlayback();
                }
            }
        }
    }

    /**
     * Native support extracted from MusicPlayer::UpdateStreamingPlayback @00459313 track-boundary branch and
     * MusicPlayer::RefillStreamingBuffer @00459067 completed-track reload selection.
     */
    private void advanceTrackAfterElapsedDuration() {
        if (playbackElapsedMs < getCurrentTrackDurationMs()) {
            return;
        }
        boolean resumePlayback = !playbackStopped;
        int nextTrackIndex = (currentTrackIndex + 1) % musicFileNames.size();
        int loadedTrackIndex = Globals.currentMusicTrack >= 0
                ? Globals.currentMusicTrack
                : (queuedTrackSwitchActive ? currentTrackIndex : nextTrackIndex);
        stopCurrentTrackPlayback();
        loadTrack(loadedTrackIndex);
        if (!queuedTrackSwitchActive) {
            currentTrackIndex = nextTrackIndex;
        }
        playbackElapsedMs = 0;
        trackBoundaryBufferOffset = bufferBytes;
        if (resumePlayback && isMusicEnabled()) {
            play();
        }
    }

    /**
     * Native: MusicPlayer::LoadTrack @0045977D.
     * Fully ported at the Java/OpenAL boundary. Native replaces the current track file, preserves playbackStopped,
     * posts EXIT_GAME on load failure, and refreshes update timing after a successful load.
     */
    public void loadTrack(int trackIndex) {
        if (trackLoaded && currentTrackSound != null) {
            currentTrackSound.release();
            trackLoaded = false;
        }
        String trackFileName = getMusicFileNameAt(trackIndex);
        currentTrackSound = new Sound(trackFileName);
        trackLoaded = currentTrackSound.load();
        if (!trackLoaded) {
            Globals.mainWindow.postMessage(MessageCodes.EXIT_GAME, 0, 0);
            return;
        }
        refreshUpdateStepMs();
    }

    /**
     * Native: MusicPlayer::RefreshUpdateStepMs @0045B4A0.
     * Fully ported.
     */
    public void refreshUpdateStepMs() {
        int avgBytesPerSec = currentTrackSound == null || currentTrackSound.format == null
                ? 0
                : currentTrackSound.format.avgBytesPerSec();
        if (bufferBytes <= 0 || avgBytesPerSec <= 0) {
            updateStepMs = 0;
            return;
        }
        long rawTickMs = (((long) bufferBytes / (long) avgBytesPerSec) * 1000L) >> 4;
        updateStepMs = (int) Math.min(rawTickMs, 100L);
    }

    /**
     * Native: MusicPlayer::GetCurrentPosition @0045B500.
     * Fully ported at the Java/OpenAL boundary. Native returns the DirectSound buffer byte cursor.
     */
    public int getCurrentPosition() {
        if (!isPlaybackBackendAvailable()) {
            return 0;
        }
        if (currentTrackSound == null) {
            return 0;
        }
        return playbackMsToBufferOffset(getCurrentPositionMs());
    }

    /**
     * Native support extracted from MusicPlayer::GetCurrentPosition @0045B500 for Java/OpenAL millisecond offsets.
     */
    private int getCurrentPositionMs() {
        if (!playbackStopped) {
            return SoundSystem.get().getPlaybackPositionMs(currentTrackSound);
        }
        return Math.max(0, playbackElapsedMs);
    }

    /**
     * Native support extracted from MusicPlayer::GetCurrentPosition @0045B500 byte-cursor projection.
     */
    private int playbackMsToBufferOffset(int positionMs) {
        long bytePosition = ((long) Math.max(0, positionMs) * currentTrackSound.format.avgBytesPerSec()) / 1000L;
        return (int) (bytePosition % bufferBytes);
    }

    /**
     * Native: MusicPlayer::GetElapsedSeconds @0045B540.
     * Fully ported.
     */
    public int getElapsedSeconds() {
        if (currentTrackSound == null || !trackLoaded) {
            return 0;
        }
        return playbackElapsedMs / 1000;
    }

    /**
     * Native support extracted from MusicPlayer::MusicPlayer @00459ACD and the recovered DirectSound-availability
     * branch in MusicPlayer::GetPlaybackState @00450150.
     */
    private boolean isPlaybackBackendAvailable() {
        return playbackBackendAvailable;
    }

    /**
     * Java helper for the recovered `g_SoundPreferences.MusicEnabled` gate in MusicPlayer::Play @00459EC5.
     * not ported.
     */
    private static boolean isMusicEnabled() {
        return Globals.soundPreferences == null || Globals.soundPreferences.musicEnabled != 0;
    }

    /**
     * Java helper for the OpenAL stop/rewind boundary used by native-backed MusicPlayer stop and track-switch paths.
     * not ported.
     */
    private void stopCurrentTrackPlayback() {
        if (currentTrackSound != null) {
            SoundSystem.get().stopAndRewind(currentTrackSound);
        }
    }

}
