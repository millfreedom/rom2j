package ua.millfreedom.rom2.model.sound;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.lwjgl.openal.AL10.*;

/**
 * Java sound-playback backend.
 * Implementation note: native-facing controllers such as {@link MusicPlayer} should use this class as the actual playback backend.
 */
public final class SoundSystem {
    private static final int DEFAULT_CHANNELS = 64;
    private static final int MAP_TILE_PIXEL_SIZE = 0x100;
    private static final int MAP_TILE_CENTER_OFFSET = 0x80;
    private static final int MAP_NATIVE_SOUND_MIN_VOLUME = -10000;
    private static final float MAP_AUDIO_WORLD_UNITS_PER_OPENAL_UNIT = MAP_TILE_PIXEL_SIZE * 8.0f;
    private static final float MAP_AUDIO_LATERAL_WIDTH_SCALE = 0.5f;
    private static final float MAP_AUDIO_LATERAL_DEPTH_SCALE = 1.1f;
    private static final double OPENAL_MIN_NONZERO_DECIBELS = -40.0d;
    private static final double OPENAL_DECIBEL_TO_GAIN_SCALE = Math.log(10.0d) / 20.0d;
    private static final FloatBuffer LISTENER_ORIENTATION = BufferUtils.createFloatBuffer(6)
            .put(new float[]{0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f})
            .flip();
    private static SoundSystem instance;

    private long device;
    private long context;
    private final List<SoundChannel> channels = new ArrayList<>();
    private final Map<Sound, Integer> bufferIds = new IdentityHashMap<>();
    private final Map<SoundChannel, ChannelPlaybackState> channelStates = new IdentityHashMap<>();
    private SoundPosition listenerPosition = SoundPosition.ORIGIN;
    private int mapAudioViewTileX;
    private int mapAudioViewTileY;
    private int mapAudioGridWidth;
    private int mapAudioGridHeight;
    private int mapAudioListenerWorldX;
    private int mapAudioListenerWorldY;

    /**
     * Java-only playback state retained so camera-listener movement can refresh manual distance gain.
     */
    private static final class ChannelPlaybackState {
        private final Sound sound;
        private final int volume;
        private final float gainScale;
        private final SoundPosition sourcePosition;
        private final SoundAttenuation attenuation;
        private final boolean positionalRequest;
        private final int mapWorldX;
        private final int mapWorldY;
        private final boolean mapPositioned;

        // not ported.
        private ChannelPlaybackState(
                Sound sound,
                int volume,
                float gainScale,
                SoundPosition sourcePosition,
                SoundAttenuation attenuation,
                boolean positionalRequest,
                int mapWorldX,
                int mapWorldY,
                boolean mapPositioned
        ) {
            this.sound = sound;
            this.volume = volume;
            this.gainScale = gainScale;
            this.sourcePosition = sourcePosition;
            this.attenuation = attenuation;
            this.positionalRequest = positionalRequest;
            this.mapWorldX = mapWorldX;
            this.mapWorldY = mapWorldY;
            this.mapPositioned = mapPositioned;
        }
    }

    /**
     * Native support aggregate for visible map sound sources in MapVisualObject::RefreshAmbientAudio @0041B311.
     */
    public final class MapSoundAccumulator {
        private int nativeSourceCount;
        private int nativeVolumeDelta = MAP_NATIVE_SOUND_MIN_VOLUME;
        private long sourceWorldXSum;
        private long sourceWorldYSum;
        private long weightedSourceWorldXSum;
        private long weightedSourceWorldYSum;
        private long sourceWeightSum;

        // not ported.
        private MapSoundAccumulator() {

        }

        /**
         * Native support extracted from MapVisualObject::RefreshAmbientAudio @0041B311 source aggregation.
         */
        public void addTile(int tileX, int tileY) {
            int weight = mapSoundWeightForTile(tileX, tileY);
            int worldX = tileX * MAP_TILE_PIXEL_SIZE + MAP_TILE_CENTER_OFFSET;
            int worldY = tileY * MAP_TILE_PIXEL_SIZE + MAP_TILE_CENTER_OFFSET;
            nativeVolumeDelta = Math.max(nativeVolumeDelta, MAP_NATIVE_SOUND_MIN_VOLUME + weight);
            sourceWorldXSum += worldX;
            sourceWorldYSum += worldY;
            if (weight > 0) {
                weightedSourceWorldXSum += (long) worldX * weight;
                weightedSourceWorldYSum += (long) worldY * weight;
                sourceWeightSum += weight;
            }
            nativeSourceCount++;
        }

        /**
         * Native support extracted from MapVisualObject::RefreshAmbientAudio @0041B311 aggregate volume blocks.
         */
        private int nativeVolumeDelta() {
            return nativeVolumeDelta;
        }

        /**
         * Java positional source X for aggregated map ambient sources.
         * not ported.
         */
        private int worldX() {
            if (sourceWeightSum != 0) {
                return Math.toIntExact(weightedSourceWorldXSum / sourceWeightSum);
            }
            if (nativeSourceCount == 0) {
                return mapAudioListenerWorldX;
            }
            return Math.toIntExact(sourceWorldXSum / nativeSourceCount);
        }

        /**
         * Java positional source Y for aggregated map ambient sources.
         * not ported.
         */
        private int worldY() {
            if (sourceWeightSum != 0) {
                return Math.toIntExact(weightedSourceWorldYSum / sourceWeightSum);
            }
            if (nativeSourceCount == 0) {
                return mapAudioListenerWorldY;
            }
            return Math.toIntExact(sourceWorldYSum / nativeSourceCount);
        }
    }

    // not ported.
    private SoundSystem() {

    }

    /**
     * Native support boundary for CMainWindow::InitSound @0045ADE7, called from
     * CMainWindow::initializeRuntimeGraphicsAndAudio @004823E2.
     * Full port at the Java/OpenAL backend boundary for the InitSound(hwnd, 0x10, null) call.
     */
    public static synchronized boolean tryInitialize(int channelCount) {
        if (instance != null) {
            return true;
        }
        try {
            instance = new SoundSystem().init(channelCount);
            return true;
        } catch (RuntimeException | LinkageError exception) {
            instance = null;
            return false;
        }
    }

    /**
     * Native support extracted from MusicPlayer::MusicPlayer @00459ACD `g_pDirectSound` availability branch.
     */
    public static synchronized boolean isInitialized() {
        return instance != null;
    }

    // not ported.
    public static SoundSystem get() {
        if (instance == null) {
            instance = new SoundSystem().init(DEFAULT_CHANNELS);
        }
        return instance;
    }

    /**
     * Native: Global::ReleaseSound @0045B123.
     * Full port at the Java/OpenAL shutdown boundary.
     */
    public static synchronized void shutdownIfInitialized() {
        if (instance != null) {
            instance.shutdown();
        }
    }

    // not ported.
    private SoundSystem init(int channelCount) {
        device = ALC10.alcOpenDevice((ByteBuffer) null);
        if (device == 0) {
            throw new IllegalStateException("Failed to open OpenAL device.");
        }

        context = ALC10.alcCreateContext(device, (int[]) null);
        if (context == 0) {
            ALC10.alcCloseDevice(device);
            throw new IllegalStateException("Failed to create OpenAL context.");
        }

        ALC10.alcMakeContextCurrent(context);
        ALCCapabilities alcCapabilities = ALC.createCapabilities(device);
        AL.createCapabilities(alcCapabilities);
        AL10.alDistanceModel(AL10.AL_NONE);
        AL10.alListener3f(AL10.AL_POSITION, 0.0f, 0.0f, 0.0f);
        AL10.alListener3f(AL10.AL_VELOCITY, 0.0f, 0.0f, 0.0f);
        AL10.alListenerfv(AL10.AL_ORIENTATION, LISTENER_ORIENTATION);

        for (int i = 0; i < channelCount; i++) {
            int sourceId = AL10.alGenSources();
            channels.add(new SoundChannel(sourceId));
        }
        return this;
    }

    // not ported.
    public void shutdown() {
        for (SoundChannel channel : channels) {
            AL10.alSourceStop(channel.sourceId);
            AL10.alSourcei(channel.sourceId, AL10.AL_BUFFER, 0);
            AL10.alDeleteSources(channel.sourceId);
        }
        channels.clear();
        for (int bufferId : bufferIds.values()) {
            AL10.alDeleteBuffers(bufferId);
        }
        bufferIds.clear();
        channelStates.clear();
        if (context != 0) {
            ALC10.alcDestroyContext(context);
            context = 0;
        }
        if (device != 0) {
            ALC10.alcCloseDevice(device);
            device = 0;
        }
        if (instance == this) {
            instance = null;
        }
    }

    /**
     * Native support backend for Sound::Play @0045AA2F.
     * Java centers source and listener for global or unknown-position sounds instead of preserving DirectSound pan.
     */
    public void playGlobal(Sound sound, int volume, boolean loop, byte priority, int freq) {
        playGlobal(sound, volume, loop, priority, freq, 0);
    }

    /**
     * Native support backend for MusicPlayer::Play @00459EC5 resume from the current DirectSound cursor.
     * Java centers source and listener for global music playback instead of preserving DirectSound pan.
     */
    public void playGlobal(Sound sound, int volume, boolean loop, byte priority, int freq, int startPositionMs) {
        SoundChannel channel = preparePlaybackChannel(sound, priority);
        if (channel == null) {
            return;
        }

        configureGlobalChannel(channel, sound, volume, freq);
        if (startPositionMs > 0) {
            AL11.alSourcef(channel.sourceId, AL11.AL_SEC_OFFSET, startPositionMs / 1000.0f);
        }
        channel.play(loop);
    }

    // not ported.
    public void playPositioned(
            Sound sound,
            int volume,
            SoundPosition sourcePosition,
            SoundAttenuation attenuation,
            boolean loop,
            byte priority,
            int freq
    ) {
        SoundChannel channel = preparePlaybackChannel(sound, priority);
        if (channel == null) {
            return;
        }

        configurePositionedChannel(channel, sound, volume, sourcePosition, attenuation, freq);
        channel.play(loop);
    }

    /**
     * Native support boundary for MapVisualObject camera state consumed by MapVisualObject::ComputeMapRelativeSoundPosition @0041B1F8.
     * not ported.
     */
    public void updateMapAudioView(int viewTileX, int viewTileY, int gridWidth, int gridHeight) {
        mapAudioViewTileX = viewTileX;
        mapAudioViewTileY = viewTileY;
        mapAudioGridWidth = Math.max(0, gridWidth);
        mapAudioGridHeight = Math.max(0, gridHeight);
        mapAudioListenerWorldX = mapAudioViewTileX * MAP_TILE_PIXEL_SIZE + mapAudioGridWidth * MAP_TILE_CENTER_OFFSET;
        mapAudioListenerWorldY = mapAudioViewTileY * MAP_TILE_PIXEL_SIZE + mapAudioGridHeight * MAP_TILE_CENTER_OFFSET;
        setListenerPosition(mapListenerSoundPosition(mapAudioListenerWorldX, mapAudioListenerWorldY));
        refreshMapPositionedChannels();
    }

    /**
     * Native support boundary for MapVisualObject::RefreshAmbientAudio @0041B311 ambient source aggregation.
     * not ported.
     */
    public MapSoundAccumulator newMapSoundAccumulator() {
        return new MapSoundAccumulator();
    }

    /**
     * Native support boundary for CUnit voice/sound callbacks that call MapVisualObject::ComputeMapRelativeSoundPosition @0041B1F8.
     * not ported.
     */
    public int mapSoundPriority(int worldX, int worldY) {
        SoundPlacement placement = computeMapSoundPlacement(worldX, worldY);
        return (10000 - Math.abs(placement.nativeVolumeDelta)) / 100;
    }

    /**
     * Native support boundary for map-positioned sound playback via MapVisualObject::ComputeMapRelativeSoundPosition @0041B1F8.
     * not ported.
     */
    public void playMapSound(Sound sound, int volume, int worldX, int worldY, boolean loop, byte priority, int freq) {
        SoundChannel channel = preparePlaybackChannel(sound, priority);
        if (channel == null) {
            return;
        }

        SoundPlacement placement = computeMapSoundPlacement(worldX, worldY);
        configureMapPositionedChannel(channel, sound, volume, placement, freq, worldX, worldY);
        channel.play(loop);
    }

    /**
     * Native support extracted from MapVisualObject::RefreshAmbientAudio @0041B311 aggregate loop playback.
     */
    public void playMapAggregateLoopSound(
            Sound sound,
            int volume,
            MapSoundAccumulator source,
            byte priority,
            int freq
    ) {
        SoundChannel channel = preparePlaybackChannel(sound, priority);
        if (channel == null) {
            return;
        }

        configureMapAggregateLoopChannel(channel, sound, volume, source, freq);
        channel.play(true);
    }

    /**
     * Native support extracted from MapVisualObject::RefreshAmbientAudio @0041B311 ambient object playback.
     */
    public void playMapAmbientObjectSound(
            Sound sound,
            int volume,
            MapSoundAccumulator source,
            byte priority,
            int freq
    ) {
        playMapSound(sound, volume, source.worldX(), source.worldY(), false, priority, freq);
    }

    // not ported.
    public void setListenerPosition(SoundPosition position) {
        listenerPosition = Objects.requireNonNull(position, "position");
        AL10.alListener3f(AL10.AL_POSITION, listenerPosition.x, listenerPosition.y, listenerPosition.z);
        refreshPositionedChannelGains();
    }

    // not ported.
    public void updatePositionedChannel(
            SoundChannel channel,
            int volume,
            SoundPosition sourcePosition,
            SoundAttenuation attenuation
    ) {
        Sound sound = channel.sound;
        if (sound == null) {
            return;
        }
        int clampedVolume = SoundPreferences.clampVolume(volume);
        ChannelPlaybackState state = new ChannelPlaybackState(
                sound,
                clampedVolume,
                1.0f,
                sourcePosition,
                attenuation,
                true,
                0,
                0,
                false
        );
        channelStates.put(channel, state);
        applyPositionedChannelState(channel, state);
    }

    /**
     * Native support boundary for MapVisualObject::RefreshAmbientAudio @0041B311 channel source refresh.
     * not ported.
     */
    public void updateMapSoundChannel(SoundChannel channel, int volume, int worldX, int worldY) {
        Sound sound = channel.sound;
        if (sound == null) {
            return;
        }

        SoundPlacement placement = computeMapSoundPlacement(worldX, worldY);
        int clampedVolume = SoundPreferences.clampVolume(volume);
        ChannelPlaybackState state = new ChannelPlaybackState(
                sound,
                clampedVolume,
                1.0f,
                placement.sourcePosition,
                SoundAttenuation.NATIVE_MAP_EXPONENTIAL,
                true,
                worldX,
                worldY,
                true
        );
        channelStates.put(channel, state);
        applyPositionedChannelState(channel, state);
    }

    /**
     * Native support boundary for MapVisualObject::RefreshAmbientAudio @0041B311 aggregate channel source refresh.
     */
    public void updateMapAggregateLoopChannel(SoundChannel channel, int volume, MapSoundAccumulator source) {
        int clampedVolume = SoundPreferences.clampVolume(volume);
        Sound sound = channel.sound;
        int sourceWorldX = source.worldX();
        int sourceWorldY = source.worldY();
        SoundPlacement placement = computeMapSoundPlacement(sourceWorldX, sourceWorldY);
        ChannelPlaybackState state = new ChannelPlaybackState(
                sound,
                clampedVolume,
                normalizeNativeVolumeDelta(source.nativeVolumeDelta()),
                placement.sourcePosition,
                SoundAttenuation.NONE,
                true,
                sourceWorldX,
                sourceWorldY,
                true
        );
        channelStates.put(channel, state);
        applyPositionedChannelState(channel, state);
    }

    // not ported.
    private SoundChannel preparePlaybackChannel(Sound sound, byte priority) {
        if (sound == null) {
            return null;
        }
        if (!sound.loaded) {
            sound.load();
        }
        if (!sound.loaded || sound.audioData == null || sound.format == null) {
            return null;
        }

        SoundChannel channel = SoundChannel.allocate(priority);
        if (channel == null) {
            return null;
        }

        int bufferId = toBufferId(sound);
        if (bufferId == 0) {
            return null;
        }

        resetSource(channel);
        AL10.alSourcei(channel.sourceId, AL10.AL_BUFFER, bufferId);
        channel.sound = sound;
        channel.priority = priority;
        return channel;
    }

    /**
     * Java support for global or unknown-position sounds.
     * The OpenAL source is relative and centered, making source and listener the same point.
     * not ported.
     */
    private void configureGlobalChannel(SoundChannel channel, Sound sound, int volume, int freq) {
        int clampedVolume = SoundPreferences.clampVolume(volume);
        AL10.alSourcef(channel.sourceId, AL10.AL_GAIN, normalizeVolume(clampedVolume));
        AL10.alSourcei(channel.sourceId, AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE);
        AL10.alSource3f(channel.sourceId, AL10.AL_POSITION, 0.0f, 0.0f, 0.0f);
        configurePitch(channel, sound, freq);
        channelStates.put(channel, new ChannelPlaybackState(
                sound,
                clampedVolume,
                1.0f,
                SoundPosition.ORIGIN,
                SoundAttenuation.NONE,
                false,
                0,
                0,
                false
        ));
    }

    /**
     * Native support extracted from MapVisualObject::RefreshAmbientAudio @0041B311 aggregate loop playback.
     * Java-normalized: user SFX volume is 0..100 and the native aggregate volume delta remains a separate gain multiplier.
     */
    private void configureMapAggregateLoopChannel(
            SoundChannel channel,
            Sound sound,
            int volume,
            MapSoundAccumulator source,
            int freq
    ) {
        configureMapAggregateChannel(
                channel,
                sound,
                volume,
                source,
                normalizeNativeVolumeDelta(source.nativeVolumeDelta()),
                freq
        );
    }

    /**
     * Native support extracted from MapVisualObject::RefreshAmbientAudio @0041B311 aggregate source playback.
     * Java maps the native aggregate source tiles to one positional source while keeping native aggregate volume as gain.
     */
    private void configureMapAggregateChannel(
            SoundChannel channel,
            Sound sound,
            int volume,
            MapSoundAccumulator source,
            float gainScale,
            int freq
    ) {
        int clampedVolume = SoundPreferences.clampVolume(volume);
        int sourceWorldX = source.worldX();
        int sourceWorldY = source.worldY();
        SoundPlacement placement = computeMapSoundPlacement(sourceWorldX, sourceWorldY);
        ChannelPlaybackState state = new ChannelPlaybackState(
                sound,
                clampedVolume,
                gainScale,
                placement.sourcePosition,
                SoundAttenuation.NONE,
                true,
                sourceWorldX,
                sourceWorldY,
                true
        );
        channelStates.put(channel, state);
        applyPositionedChannelState(channel, state);
        configurePitch(channel, sound, freq);
    }

    // not ported.
    private void configurePositionedChannel(
            SoundChannel channel,
            Sound sound,
            int volume,
            SoundPosition sourcePosition,
            SoundAttenuation attenuation,
            int freq
    ) {
        int clampedVolume = SoundPreferences.clampVolume(volume);
        ChannelPlaybackState state = new ChannelPlaybackState(
                sound,
                clampedVolume,
                1.0f,
                sourcePosition,
                attenuation,
                true,
                0,
                0,
                false
        );
        channelStates.put(channel, state);
        applyPositionedChannelState(channel, state);
        configurePitch(channel, sound, freq);
    }

    // not ported.
    private void configureMapPositionedChannel(
            SoundChannel channel,
            Sound sound,
            int volume,
            SoundPlacement placement,
            int freq,
            int mapWorldX,
            int mapWorldY
    ) {
        int clampedVolume = SoundPreferences.clampVolume(volume);
        ChannelPlaybackState state = new ChannelPlaybackState(
                sound,
                clampedVolume,
                1.0f,
                placement.sourcePosition,
                SoundAttenuation.NATIVE_MAP_EXPONENTIAL,
                true,
                mapWorldX,
                mapWorldY,
                true
        );
        channelStates.put(channel, state);
        applyPositionedChannelState(channel, state);
        configurePitch(channel, sound, freq);
    }

    // not ported.
    public boolean isPlaying() {
        for (SoundChannel channel : channels) {
            int state = AL10.alGetSourcei(channel.sourceId, AL10.AL_SOURCE_STATE);
            if (state == AL10.AL_PLAYING) {
                return true;
            }
        }
        return false;
    }

    /**
     * Native support boundary for Sound::PlayPointer @00438570.
     * not ported.
     */
    public boolean isSoundPlaying(Sound sound) {
        if (sound == null) {
            return false;
        }
        for (SoundChannel channel : channels) {
            int state = AL10.alGetSourcei(channel.sourceId, AL10.AL_SOURCE_STATE);
            if (state == AL10.AL_PLAYING && channel.sound == sound) {
                return true;
            }
        }
        return false;
    }

    /**
     * Native support backend for Sound::GetPlayingChannel @0045A964.
     */
    public SoundChannel getPlayingChannel(Sound sound) {
        if (sound == null) {
            return null;
        }
        for (SoundChannel channel : channels) {
            int state = AL10.alGetSourcei(channel.sourceId, AL10.AL_SOURCE_STATE);
            if (state == AL10.AL_PLAYING && channel.sound == sound) {
                return channel;
            }
        }
        return null;
    }

    /**
     * Native support backend for SoundChannel::SetVolume @0041F4A0.
     * not ported.
     */
    public void setChannelVolume(SoundChannel channel, int volume) {
        int clampedVolume = SoundPreferences.clampVolume(volume);
        ChannelPlaybackState state = channelStates.get(channel);
        if (state != null && state.positionalRequest) {
            ChannelPlaybackState updatedState = new ChannelPlaybackState(
                    state.sound,
                    clampedVolume,
                    state.gainScale,
                    state.sourcePosition,
                    state.attenuation,
                    true,
                    state.mapWorldX,
                    state.mapWorldY,
                    state.mapPositioned
            );
            channelStates.put(channel, updatedState);
            AL10.alSourcef(channel.sourceId, AL10.AL_GAIN, positionedGain(updatedState));
            return;
        }
        if (state != null) {
            channelStates.put(channel, new ChannelPlaybackState(
                    state.sound,
                    clampedVolume,
                    state.gainScale,
                    state.sourcePosition,
                    state.attenuation,
                    false,
                    state.mapWorldX,
                    state.mapWorldY,
                    state.mapPositioned
            ));
        }
        AL10.alSourcef(channel.sourceId, AL10.AL_GAIN, normalizeVolume(clampedVolume));
    }

    /**
     * Native support backend for SoundChannel::GetVolume @0045ADB4.
     */
    int getChannelVolume(SoundChannel channel) {
        ChannelPlaybackState state = channelStates.get(channel);
        return state == null ? 0 : state.volume;
    }

    /**
     * Native support boundary for MusicPlayer::SetVolume @004501E0.
     * not ported.
     */
    public void setSoundVolume(Sound sound, int volume) {
        if (sound == null) {
            return;
        }
        int clampedVolume = SoundPreferences.clampVolume(volume);
        float gain = normalizeVolume(clampedVolume);
        for (SoundChannel channel : channels) {
            if (channel.sound != sound) {
                continue;
            }
            ChannelPlaybackState state = channelStates.get(channel);
            if (state != null && state.positionalRequest) {
                ChannelPlaybackState updatedState = new ChannelPlaybackState(
                        state.sound,
                        clampedVolume,
                        state.gainScale,
                        state.sourcePosition,
                        state.attenuation,
                        true,
                        state.mapWorldX,
                        state.mapWorldY,
                        state.mapPositioned
                );
                channelStates.put(channel, updatedState);
                AL10.alSourcef(channel.sourceId, AL10.AL_GAIN, positionedGain(updatedState));
            } else {
                if (state != null) {
                    channelStates.put(channel, new ChannelPlaybackState(
                            state.sound,
                            clampedVolume,
                            state.gainScale,
                            state.sourcePosition,
                            state.attenuation,
                            false,
                            state.mapWorldX,
                            state.mapWorldY,
                            state.mapPositioned
                    ));
                }
                AL10.alSourcef(channel.sourceId, AL10.AL_GAIN, gain);
            }
        }
    }

    /**
     * Native support boundary for MusicPlayer::GetCurrentPosition @0045B500.
     * not ported.
     */
    public int getPlaybackPositionMs(Sound sound) {
        if (sound == null) {
            return 0;
        }
        for (SoundChannel channel : channels) {
            if (channel.sound != sound) {
                continue;
            }
            float seconds = AL11.alGetSourcef(channel.sourceId, AL11.AL_SEC_OFFSET);
            return Math.max(0, Math.round(seconds * 1000.0f));
        }
        return 0;
    }

    /**
     * Native support boundary for global sound-slot helpers used by town dialog ports.
     * not ported.
     */
    public void stopAndRewind(Sound sound) {
        if (sound == null) {
            return;
        }
        SoundChannel channel = getPlayingChannel(sound);
        if (channel != null) {
            channel.stopAndRewind();
        }
    }

    /**
     * Native support boundary for the SoundChannel::Stop loops in MapVisualObject::clearSessionForLobbyReturn @0041CD15
     * and MapVisualObject::cleanupCompletedMissionMapState @0041C897.
     * not ported.
     */
    public void stopAllChannels() {
        for (SoundChannel channel : channels) {
            channel.stop();
        }
        channelStates.clear();
    }

    /**
     * Native support backend for Sound::Unload @0045A36F and global sound-slot destruction helpers.
     */
    public void releaseSound(Sound sound) {
        if (sound == null) {
            return;
        }
        for (SoundChannel channel : channels) {
            if (channel.sound == sound) {
                channel.stopAndRewind();
                clearChannelAssignment(channel);
            }
        }
        Integer bufferId = bufferIds.remove(sound);
        if (bufferId != null) {
            AL10.alDeleteBuffers(bufferId);
        }
        sound.audioData = null;
        sound.format = null;
        sound.loaded = false;
    }

    /**
     * Native support backend for SoundChannel::Allocate @0045AB9C.
     * not ported.
     */
    SoundChannel allocateChannel(byte priority) {
        SoundChannel free = null;
        for (SoundChannel channel : channels) {
            int state = AL10.alGetSourcei(channel.sourceId, AL10.AL_SOURCE_STATE);
            if (state != AL10.AL_PLAYING) {
                free = channel;
                break;
            }
        }
        if (free != null) {
            clearChannelAssignment(free);
            return free;
        }
        int requestedPriority = Byte.toUnsignedInt(priority);
        SoundChannel lowerPriorityChannel = null;
        int lowerPriority = Integer.MAX_VALUE;
        for (SoundChannel channel : channels) {
            int channelPriority = Byte.toUnsignedInt(channel.priority);
            if (channelPriority < requestedPriority && channelPriority < lowerPriority) {
                lowerPriorityChannel = channel;
                lowerPriority = channelPriority;
            }
        }
        if (lowerPriorityChannel != null) {
            lowerPriorityChannel.stopAndRewind();
            clearChannelAssignment(lowerPriorityChannel);
        }
        return lowerPriorityChannel;
    }

    // not ported.
    private int toBufferId(Sound sound) {
        Integer cachedBufferId = bufferIds.get(sound);
        if (cachedBufferId != null) {
            return cachedBufferId;
        }

        int format = resolveFormat(sound.format.channels(), sound.format.bitsPerSample());
        if (format == -1) {
            return 0;
        }

        ByteBuffer sourceData = sound.audioData.duplicate();
        sourceData.position(0);
        ByteBuffer alBufferData = BufferUtils.createByteBuffer(sourceData.remaining());
        alBufferData.put(sourceData);
        alBufferData.flip();

        int bufferId = AL10.alGenBuffers();
        AL10.alBufferData(bufferId, format, alBufferData, sound.format.samplesPerSec());
        bufferIds.put(sound, bufferId);
        return bufferId;
    }

    // not ported.
    private void resetSource(SoundChannel channel) {
        AL10.alSourceStop(channel.sourceId);
        AL10.alSourcei(channel.sourceId, AL10.AL_BUFFER, 0);
        AL10.alSourcef(channel.sourceId, AL10.AL_GAIN, 1.0f);
        AL10.alSourcef(channel.sourceId, AL10.AL_PITCH, 1.0f);
        AL10.alSourcei(channel.sourceId, AL10.AL_LOOPING, AL10.AL_FALSE);
        AL10.alSourcei(channel.sourceId, AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE);
        AL10.alSource3f(channel.sourceId, AL10.AL_POSITION, 0.0f, 0.0f, 0.0f);
        AL10.alSource3f(channel.sourceId, AL10.AL_VELOCITY, 0.0f, 0.0f, 0.0f);
        clearChannelAssignment(channel);
    }

    /**
     * Native support backend for SoundChannel::Stop @0041F430.
     * not ported.
     */
    void stopChannel(SoundChannel channel) {
        if (AL10.alGetSourcei(channel.sourceId, AL10.AL_BUFFER) != 0) {
            AL10.alSourceStop(channel.sourceId);
        }
        clearChannelAssignment(channel);
    }

    /**
     * Native support backend for SoundChannel::StopAndRewind @0041F610.
     * not ported.
     */
    void stopAndRewindChannel(SoundChannel channel) {
        if (AL10.alGetSourcei(channel.sourceId, AL10.AL_BUFFER) != 0) {
            AL10.alSourceStop(channel.sourceId);
            AL10.alSourceRewind(channel.sourceId);
        }
    }

    /**
     * Native support backend for SoundChannel::Play @0045AD66.
     * not ported.
     */
    void playChannel(SoundChannel channel, boolean loop) {
        if (AL10.alGetSourcei(channel.sourceId, AL10.AL_BUFFER) != 0) {
            configureLooping(channel, loop);
            alSourcePlay(channel.sourceId);
        }
    }

    /**
     * Native support extracted from SoundChannel::Stop @0041F430 and SoundChannel::Allocate @0045AB9C.
     */
    private void clearChannelAssignment(SoundChannel channel) {
        AL10.alSourcei(channel.sourceId, AL10.AL_BUFFER, 0);
        channel.sound = null;
        channel.priority = 0;
        channelStates.remove(channel);
    }

    // not ported.
    private void configurePitch(SoundChannel channel, Sound sound, int freq) {
        if (freq > 0 && sound.format.samplesPerSec() > 0) {
            AL10.alSourcef(channel.sourceId, AL10.AL_PITCH,
                    (float) freq / (float) sound.format.samplesPerSec());
        } else {
            AL10.alSourcef(channel.sourceId, AL10.AL_PITCH, 1.0f);
        }
    }

    // not ported.
    private void configureLooping(SoundChannel channel, boolean loop) {
        if (loop) {
            alSourcei(channel.sourceId, AL_LOOPING, AL_TRUE);
        } else {
            alSourcei(channel.sourceId, AL_LOOPING, AL_FALSE);
        }
    }

    // not ported.
    private void applyPositionedChannelState(SoundChannel channel, ChannelPlaybackState state) {
        AL10.alSourcef(channel.sourceId, AL10.AL_GAIN, positionedGain(state));
        AL10.alSourcei(channel.sourceId, AL10.AL_SOURCE_RELATIVE, AL10.AL_FALSE);
        AL10.alSource3f(
                channel.sourceId,
                AL10.AL_POSITION,
                state.sourcePosition.x,
                state.sourcePosition.y,
                state.sourcePosition.z
        );
    }

    // not ported.
    private void refreshMapPositionedChannels() {
        for (SoundChannel channel : channels) {
            ChannelPlaybackState state = channelStates.get(channel);
            if (state == null || !state.mapPositioned) {
                continue;
            }

            SoundPlacement placement = computeMapSoundPlacement(state.mapWorldX, state.mapWorldY);
            ChannelPlaybackState updatedState = new ChannelPlaybackState(
                    state.sound,
                    state.volume,
                    state.gainScale,
                    placement.sourcePosition,
                    SoundAttenuation.NATIVE_MAP_EXPONENTIAL,
                    true,
                    state.mapWorldX,
                    state.mapWorldY,
                    true
            );
            channelStates.put(channel, updatedState);
            applyPositionedChannelState(channel, updatedState);
        }
    }

    // not ported.
    private void refreshPositionedChannelGains() {
        for (Map.Entry<SoundChannel, ChannelPlaybackState> entry : channelStates.entrySet()) {
            ChannelPlaybackState state = entry.getValue();
            if (state.positionalRequest) {
                AL10.alSourcef(entry.getKey().sourceId, AL10.AL_GAIN, positionedGain(state));
            }
        }
    }

    // not ported.
    private float positionedGain(ChannelPlaybackState state) {
        float distanceGain = state.attenuation.gainForDistance(state.sourcePosition.distanceTo(listenerPosition));
        return normalizeVolume(state.volume) * distanceGain * state.gainScale;
    }

    /**
     * Native support extracted from MapVisualObject::ComputeMapRelativeSoundPosition @0041B1F8.
     * Java extends the native volume result with listener/source positions for OpenAL positional playback.
     */
    private SoundPlacement computeMapSoundPlacement(int worldX, int worldY) {
        double dx = worldX - mapAudioListenerWorldX;
        double dy = worldY - mapAudioListenerWorldY;
        double distanceTiles = (Math.sqrt(dy * dy + dx * dx) / MAP_TILE_PIXEL_SIZE) / 8.0;
        double volume = -(nativeSoundDistanceCurve(distanceTiles) - 1.0) * 100.0;
        if (volume < MAP_NATIVE_SOUND_MIN_VOLUME) {
            volume = MAP_NATIVE_SOUND_MIN_VOLUME;
        }
        SoundPosition sourcePosition = mapWorldToSoundPosition(worldX, worldY);
        return new SoundPlacement(listenerPosition, sourcePosition, (int) volume, sourcePosition.distanceTo(listenerPosition));
    }

    /**
     * Native support extracted from MapVisualObject::RefreshAmbientAudio @0041B311 cell attenuation blocks.
     */
    private int mapSoundWeightForTile(int tileX, int tileY) {
        int centerX = mapAudioViewTileX + (mapAudioGridWidth >> 1);
        int centerY = mapAudioViewTileY + (mapAudioGridHeight >> 1);
        double distance = Math.sqrt((double) (tileX - centerX) * (tileX - centerX)
                + (double) (tileY - centerY) * (tileY - centerY));
        int attenuation = (int) (10000.0 - (nativeSoundDistanceCurve((int) distance >> 3) - 1.0) * 100.0);
        if (attenuation < 0) {
            return 0;
        }
        return attenuation;
    }

    /**
     * Native support extracted from MapVisualObject::ComputeMapRelativeSoundPosition @0041B1F8.
     * Java scales the camera center to OpenAL units; one unit matches the native 8-tile distance step.
     */
    private static SoundPosition mapListenerSoundPosition(int worldX, int worldY) {
        return new SoundPosition(
                worldX / MAP_AUDIO_WORLD_UNITS_PER_OPENAL_UNIT,
                0.0f,
                worldY / MAP_AUDIO_WORLD_UNITS_PER_OPENAL_UNIT
        );
    }

    /**
     * Native support extracted from MapVisualObject::ComputeMapRelativeSoundPosition @0041B1F8.
     * Java calibrates map sound projection by narrowing lateral placement and adding depth from lateral distance.
     */
    private SoundPosition mapWorldToSoundPosition(int worldX, int worldY) {
        float relativeX = (worldX - mapAudioListenerWorldX) / MAP_AUDIO_WORLD_UNITS_PER_OPENAL_UNIT;
        float relativeZ = (worldY - mapAudioListenerWorldY) / MAP_AUDIO_WORLD_UNITS_PER_OPENAL_UNIT;
        float projectedX = relativeX * MAP_AUDIO_LATERAL_WIDTH_SCALE;
        float lateralDepth = Math.abs(relativeX) * MAP_AUDIO_LATERAL_DEPTH_SCALE;
        float projectedZ = relativeZ + (relativeZ < 0.0f ? -lateralDepth : lateralDepth);
        return new SoundPosition(
                listenerPosition.x + projectedX,
                0.0f,
                listenerPosition.z + projectedZ
        );
    }

    /**
     * Native support extracted from MapVisualObject::ComputeMapRelativeSoundPosition @0041B1F8 and RefreshAmbientAudio @0041B311.
     */
    private static double nativeSoundDistanceCurve(double distance) {
        return Math.exp(distance);
    }

    // not ported.
    private static int resolveFormat(int channels, int bitsPerSample) {
        if (channels == 1 && bitsPerSample == 8) {
            return AL10.AL_FORMAT_MONO8;
        }
        if (channels == 1 && bitsPerSample == 16) {
            return AL10.AL_FORMAT_MONO16;
        }
        if (channels == 2 && bitsPerSample == 8) {
            return AL10.AL_FORMAT_STEREO8;
        }
        if (channels == 2 && bitsPerSample == 16) {
            return AL10.AL_FORMAT_STEREO16;
        }
        return -1;
    }

    /**
     * Java support for OpenAL gain conversion from normalized 0..100 game volume.
     * Uses an evenly spaced decibel ramp so UI volume steps feel linear; 0 remains exact silence.
     * not ported.
     */
    private static float normalizeVolume(int volume) {
        int clamped = SoundPreferences.clampVolume(volume);
        if (clamped == SoundPreferences.VOLUME_MIN) {
            return 0.0f;
        }
        double normalized = (clamped - 1.0d) / (SoundPreferences.VOLUME_MAX - 1.0d);
        double decibels = OPENAL_MIN_NONZERO_DECIBELS * (1.0d - normalized);
        return (float) Math.exp(decibels * OPENAL_DECIBEL_TO_GAIN_SCALE);
    }

    /**
     * Java support for native map/ambient DirectSound attenuation deltas that remain outside the user-facing 0..100 volume domain.
     * not ported.
     */
    private static float normalizeNativeVolumeDelta(int nativeVolumeDelta) {
        int clamped = Math.max(MAP_NATIVE_SOUND_MIN_VOLUME, Math.min(0, nativeVolumeDelta));
        return (clamped - MAP_NATIVE_SOUND_MIN_VOLUME) / (float) -MAP_NATIVE_SOUND_MIN_VOLUME;
    }

}
