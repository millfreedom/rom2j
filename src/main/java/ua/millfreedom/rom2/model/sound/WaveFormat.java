package ua.millfreedom.rom2.model.sound;

public record WaveFormat(int formatTag, int channels, int samplesPerSec, int avgBytesPerSec, int blockAlign,
                         int bitsPerSample, byte[] extraData) {
}
