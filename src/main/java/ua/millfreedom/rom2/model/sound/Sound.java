package ua.millfreedom.rom2.model.sound;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.function.Predicate;

public class Sound implements MfcSerializable {
    private static final int RIFF_ID = 0x46464952; // "RIFF"
    private static final int WAVE_ID = 0x45564157; // "WAVE"
    private static final int FMT_ID = 0x20746d66; // "fmt "
    private static final int DATA_ID = 0x61746164; // "data"
    public static final byte POINTER_SFX_PRIORITY = (byte) 0x80;

    public static final Predicate<Sound> isPlaying = Sound::isPlaying;
    public static final Predicate<Sound> notPlaying = isPlaying.negate();

    public String fileName;
    public WaveFormat format;
    public ByteBuffer audioData;
    public boolean loaded;


    /**
     * vtbl +0x08: CObject::Serialize @00401970.
     */
    @Override
    public void serialize(CArchive ar) {
        // Native CObject::Serialize is a no-op.
    }

    /**
     * Native: Sound::Sound @0045A137.
     * Fully ported. Java field defaults cover native unloaded buffer state.
     */
    public Sound() {
        this.fileName = "";
    }

    /**
     * Native: Sound::Sound @0045A1A2.
     * Fully ported. Java field defaults cover native unloaded buffer state.
     */
    public Sound(String fileName) {
        this.fileName = fileName;
    }

    // not ported.
    public Sound play(boolean loop) {
        return play(Globals.soundPreferences.sfxVolume, loop, (byte) 0, 0);
    }

    /**
     * Native: Sound::Play @0045AA2F.
     * Java/OpenAL playback boundary for global sounds with no recovered source position.
     * Java centers source and listener instead of using legacy DirectSound pan.
     */
    public Sound play(int volume, boolean loop, byte priority, int freq) {
        SoundSystem.get().playGlobal(this, volume, loop, priority, freq);
        return this;
    }

    // not ported.
    public Sound playPositioned(
            int volume,
            SoundPosition sourcePosition,
            SoundAttenuation attenuation,
            boolean loop,
            byte priority,
            int freq
    ) {
        SoundSystem.get().playPositioned(this, volume, sourcePosition, attenuation, loop, priority, freq);
        return this;
    }

    /**
     * Native: Sound::GetPlayingChannel @0045A964.
     * Fully ported. SoundSystem owns the Java/OpenAL channel array that replaces the native global channel array.
     */
    public SoundChannel getPlayingChannel() {
        return SoundSystem.get().getPlayingChannel(this);
    }

    // not ported.
    public boolean isPlaying() {
        return SoundSystem.get().isSoundPlaying(this);
    }

    // not ported.
    public Sound playIf(Predicate<Sound> p) {
        if (p.test(this)) {
            play(Globals.soundPreferences.sfxVolume, false, (byte) 0, 0);
        }
        return this;
    }

    // not ported.
    public Sound playIf(Predicate<Sound> p, int volume, boolean loop, byte priority, int freq) {
        if (p.test(this)) {
            play(volume, loop, priority, freq);
        }
        return this;
    }

    // not ported.
    public Sound playIfNotPlaying() {
        return playIf(notPlaying);
    }

    // not ported.
    public Sound playIfNotPlaying(int volume, boolean loop, byte priority, int freq) {
        return playIf(notPlaying, volume, loop, priority, freq);
    }

    // not ported.
    public Sound playFresh() {
        return stop()
                .play(Globals.soundPreferences.sfxVolume, false, (byte) 0, 0);
    }

    /**
     * Stops the native-style active playback channel bound to this sound.
     * not ported.
     */
    public Sound stop() {
        SoundSystem.get().stopAndRewind(this);
        return this;
    }

    /**
     * Native: Sound::Unload @0045A36F.
     * Fully ported. SoundSystem owns the Java/OpenAL buffer state that replaces native DirectSound buffers.
     */
    public void unload() {
        if (loaded) {
            SoundSystem.get().releaseSound(this);
        }
    }

    /**
     * Native support extracted from Sound::~Sound @0045A222 for Java sound-manager release.
     */
    public void release() {
        if (loaded || audioData != null || format != null) {
            SoundSystem.get().releaseSound(this);
        }
        fileName = null;
    }

    /**
     * Native: Sound::Load @0045A3F4.
     * Full port at the Java sound-data/OpenAL-buffer boundary; Java returns load success for callers.
     */
    public boolean load() {
        return load(Objects.requireNonNull(fileName, "fileName"));
    }

    /**
     * Native support extracted from Sound::Load @0045A3F4.
     */
    public boolean load(String name) {
        Objects.requireNonNull(name, "name");
        this.fileName = name;

        ByteBuffer source;
        try {
            source = Globals.gameFileManager.get(name);
        } catch (Exception e) {
            // Native CGameFile::Open returns false here and leaves the sound unloaded.
            loaded = false;
            return false;
        }
        ByteBuffer buffer = source.duplicate().order(source.order());

        if (buffer.remaining() < 12) {
            loaded = false;
            return false;
        }

        int riff = buffer.getInt();
        buffer.getInt();
        int wave = buffer.getInt();
        if (riff != RIFF_ID || wave != WAVE_ID) {
            loaded = false;
            return false;
        }

        WaveFormat foundFormat = null;
        ByteBuffer foundAudio = null;

        while (buffer.remaining() >= 8) {
            int chunkId = buffer.getInt();
            int chunkSize = buffer.getInt();
            if (chunkSize < 0 || chunkSize > buffer.remaining()) {
                loaded = false;
                return false;
            }

            int chunkStart = buffer.position();
            if (chunkId == FMT_ID) {
                foundFormat = readFormat(buffer, chunkSize);
            } else if (chunkId == DATA_ID) {
                ByteBuffer slice = buffer.slice().order(buffer.order());
                slice.limit(chunkSize);
                foundAudio = slice;
                buffer.position(chunkStart + chunkSize);
            } else {
                buffer.position(chunkStart + chunkSize);
            }

            if ((chunkSize & 1) == 1 && buffer.hasRemaining()) {
                buffer.position(buffer.position() + 1);
            }

            if (foundFormat != null && foundAudio != null) {
                break;
            }
        }

        if (foundFormat == null || foundAudio == null) {
            loaded = false;
            return false;
        }

        if (foundFormat.formatTag() != 1) {
            if (foundFormat.formatTag() == 2) {
                return decodeMsAdpcm(foundFormat, foundAudio);
            }
            return decodeToPcm(foundFormat, source);
        }

        this.format = foundFormat;
        this.audioData = foundAudio;
        this.loaded = true;
        return true;
    }

    // not ported.
    private static WaveFormat readFormat(ByteBuffer buffer, int chunkSize) {
        if (chunkSize < 16) {
            buffer.position(buffer.position() + chunkSize);
            return null;
        }

        int start = buffer.position();
        int formatTag = Short.toUnsignedInt(buffer.getShort());
        int channels = Short.toUnsignedInt(buffer.getShort());
        int samplesPerSec = buffer.getInt();
        int avgBytesPerSec = buffer.getInt();
        int blockAlign = Short.toUnsignedInt(buffer.getShort());
        int bitsPerSample = Short.toUnsignedInt(buffer.getShort());

        byte[] extra = null;
        int remaining = chunkSize - 16;
        if (remaining >= 2) {
            int extraSize = Short.toUnsignedInt(buffer.getShort());
            remaining -= 2;
            if (extraSize > 0 && remaining > 0) {
                int toRead = Math.min(extraSize, remaining);
                extra = new byte[toRead];
                buffer.get(extra);
                remaining -= toRead;
            }
        }

        if (remaining > 0) {
            buffer.position(buffer.position() + remaining);
        }

        buffer.position(start + chunkSize);
        return new WaveFormat(formatTag, channels, samplesPerSec, avgBytesPerSec, blockAlign, bitsPerSample, extra);
    }

    // not ported.
    private boolean decodeToPcm(WaveFormat sourceFormat, ByteBuffer sourceBuffer) {
        ByteBuffer wavBuffer = sourceBuffer.duplicate();
        wavBuffer.position(0);
        wavBuffer.limit(sourceBuffer.limit());
        byte[] wavBytes = new byte[wavBuffer.remaining()];
        wavBuffer.get(wavBytes);

        try (AudioInputStream ais = AudioSystem.getAudioInputStream(new ByteArrayInputStream(wavBytes))) {
            AudioFormat baseFormat = ais.getFormat();
            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false
            );
            try (AudioInputStream decodedStream = AudioSystem.getAudioInputStream(decodedFormat, ais)) {
                byte[] pcmBytes = decodedStream.readAllBytes();
                this.format = new WaveFormat(
                        1,
                        decodedFormat.getChannels(),
                        (int) decodedFormat.getSampleRate(),
                        (int) (decodedFormat.getSampleRate() * decodedFormat.getChannels() * 2),
                        decodedFormat.getChannels() * 2,
                        16,
                        sourceFormat.extraData()
                );
                this.audioData = ByteBuffer.wrap(pcmBytes).order(ByteOrder.LITTLE_ENDIAN);
                this.loaded = true;
                return true;
            }
        } catch (UnsupportedAudioFileException | IOException e) {
            loaded = false;
            return false;
        }
    }

    // not ported.
    private boolean decodeMsAdpcm(WaveFormat sourceFormat, ByteBuffer encodedAudio) {
        ByteBuffer pcmData = MsAdpcmDecoder.decode(encodedAudio, sourceFormat);
        if (pcmData == null) {
            loaded = false;
            return false;
        }

        this.format = new WaveFormat(
                1,
                sourceFormat.channels(),
                sourceFormat.samplesPerSec(),
                sourceFormat.samplesPerSec() * sourceFormat.channels() * 2,
                sourceFormat.channels() * 2,
                16,
                sourceFormat.extraData()
        );
        this.audioData = pcmData;
        this.loaded = true;
        return true;
    }

    /**
     * Native: Sound::PlayPointer @00438570.
     * Fully ported.
     */
    public static void playPointer(Sound[] sounds, int index) {
        Sound sound = sounds[index];
        if (sound == null || SoundSystem.get().isSoundPlaying(sound)) {
            return;
        }
        sound.play(Globals.soundPreferences.sfxVolume, false, POINTER_SFX_PRIORITY, 0);
    }

}
