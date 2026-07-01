package ua.millfreedom.rom2.model.sound;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

final class MsAdpcmDecoder {
    private static final int[][] DEFAULT_COEFFICIENTS = {
            {256, 0},
            {512, -256},
            {0, 0},
            {192, 64},
            {240, 0},
            {460, -208},
            {392, -232}
    };

    // not ported.
    private MsAdpcmDecoder() {
    }

    // not ported.
    static ByteBuffer decode(ByteBuffer encodedAudio, WaveFormat format) {
        if (encodedAudio == null || format == null) {
            return null;
        }
        int channels = format.channels();
        if (!(channels == 1 || channels == 2)) {
            return null;
        }

        int blockAlign = format.blockAlign();
        if (blockAlign <= 0) {
            return null;
        }

        int samplesPerBlock = samplesPerBlock(format, blockAlign);
        if (samplesPerBlock <= 0) {
            return null;
        }

        int totalBlocks = encodedAudio.remaining() / blockAlign;
        if (totalBlocks <= 0) {
            return ByteBuffer.allocate(0).order(ByteOrder.LITTLE_ENDIAN);
        }

        int totalSamples = totalBlocks * samplesPerBlock;
        int outputBytes = totalSamples * channels * 2;
        ByteBuffer output = ByteBuffer.allocate(outputBytes).order(ByteOrder.LITTLE_ENDIAN);

        int[][] coefficients = readCoefficients(format.extraData());

        ByteBuffer input = encodedAudio.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        for (int blockIndex = 0; blockIndex < totalBlocks; blockIndex++) {
            int blockStart = input.position();
            decodeBlock(input, output, channels, blockAlign, samplesPerBlock, coefficients);
            input.position(blockStart + blockAlign);
        }

        output.flip();
        return output;
    }

    // not ported.
    private static int samplesPerBlock(WaveFormat format, int blockAlign) {
        byte[] extra = format.extraData();
        if (extra != null && extra.length >= 2) {
            int value = Short.toUnsignedInt(ByteBuffer.wrap(extra).order(ByteOrder.LITTLE_ENDIAN).getShort(0));
            if (value > 0) {
                return value;
            }
        }
        int channels = format.channels();
        int header = 7 * channels;
        int data = blockAlign - header;
        if (data <= 0) {
            return 0;
        }
        return (data * 2 / channels) + 2;
    }

    // not ported.
    private static int[][] readCoefficients(byte[] extra) {
        if (extra == null || extra.length < 4) {
            return DEFAULT_COEFFICIENTS;
        }
        ByteBuffer buf = ByteBuffer.wrap(extra).order(ByteOrder.LITTLE_ENDIAN);
        buf.getShort();
        int count = Short.toUnsignedInt(buf.getShort());
        if (count <= 0 || buf.remaining() < count * 4) {
            return DEFAULT_COEFFICIENTS;
        }
        int[][] coeffs = new int[count][2];
        for (int i = 0; i < count; i++) {
            coeffs[i][0] = buf.getShort();
            coeffs[i][1] = buf.getShort();
        }
        return coeffs;
    }

    // not ported.
    private static void decodeBlock(ByteBuffer input,
                                    ByteBuffer output,
                                    int channels,
                                    int blockAlign,
                                    int samplesPerBlock,
                                    int[][] coefficients) {
        int[] predictor = new int[channels];
        int[] delta = new int[channels];
        int[] sample1 = new int[channels];
        int[] sample2 = new int[channels];

        for (int ch = 0; ch < channels; ch++) {
            predictor[ch] = Byte.toUnsignedInt(input.get());
        }
        for (int ch = 0; ch < channels; ch++) {
            delta[ch] = Short.toUnsignedInt(input.getShort());
        }
        for (int ch = 0; ch < channels; ch++) {
            sample1[ch] = input.getShort();
        }
        for (int ch = 0; ch < channels; ch++) {
            sample2[ch] = input.getShort();
        }

        int remainingHeader = (7 * channels) - (channels + (2 * channels) + (2 * channels) + (2 * channels));
        if (remainingHeader > 0 && remainingHeader <= input.remaining()) {
            input.position(input.position() + remainingHeader);
        }

        int[] coef1 = new int[channels];
        int[] coef2 = new int[channels];
        for (int ch = 0; ch < channels; ch++) {
            int index = predictor[ch];
            if (index < 0 || index >= coefficients.length) {
                index = 0;
            }
            coef1[ch] = coefficients[index][0];
            coef2[ch] = coefficients[index][1];
        }

        for (int ch = 0; ch < channels; ch++) {
            output.putShort((short) sample1[ch]);
            output.putShort((short) sample2[ch]);
        }

        int samplesDecoded = 2;
        int dataBytes = blockAlign - (7 * channels);
        int dataSamples = (dataBytes * 2) / channels;
        int samplesToDecode = Math.min(samplesPerBlock - 2, dataSamples);

        if (channels == 1) {
            for (int i = 0; i < samplesToDecode; i++) {
                int nibble = (i % 2 == 0)
                        ? (Byte.toUnsignedInt(input.get()) >> 4)
                        : (Byte.toUnsignedInt(input.get(input.position() - 1)) & 0x0F);
                int sample = decodeNibble(nibble, 0, delta, sample1, sample2, coef1, coef2);
                output.putShort((short) sample);
                samplesDecoded++;
            }
        } else {
            for (int i = 0; i < samplesToDecode; i += 2) {
                int packed = Byte.toUnsignedInt(input.get());
                int nibble0 = (packed >> 4) & 0x0F;
                int nibble1 = packed & 0x0F;
                int sampleLeft = decodeNibble(nibble0, 0, delta, sample1, sample2, coef1, coef2);
                int sampleRight = decodeNibble(nibble1, 1, delta, sample1, sample2, coef1, coef2);
                output.putShort((short) sampleLeft);
                output.putShort((short) sampleRight);
                samplesDecoded += 2;
                if (samplesDecoded >= samplesPerBlock) {
                    break;
                }
            }
        }

        int samplesRemaining = samplesPerBlock - samplesDecoded;
        for (int i = 0; i < samplesRemaining; i++) {
            for (int ch = 0; ch < channels; ch++) {
                output.putShort((short) sample1[ch]);
            }
        }
    }

    // not ported.
    private static int decodeNibble(int nibble,
                                    int channel,
                                    int[] delta,
                                    int[] sample1,
                                    int[] sample2,
                                    int[] coef1,
                                    int[] coef2) {
        int signedNibble = (nibble & 0x08) != 0 ? nibble - 16 : nibble;
        int predicted = (sample1[channel] * coef1[channel] + sample2[channel] * coef2[channel]) / 256;
        int sample = predicted + signedNibble * delta[channel];
        sample = clamp(sample);

        sample2[channel] = sample1[channel];
        sample1[channel] = sample;

        int newDelta = (delta[channel] * adaptionTable(nibble)) / 256;
        if (newDelta < 16) {
            newDelta = 16;
        }
        delta[channel] = newDelta;
        return sample;
    }

    // not ported.
    private static int adaptionTable(int nibble) {
        return switch (nibble & 0x0F) {
            case 0 -> 230;
            case 1 -> 230;
            case 2 -> 230;
            case 3 -> 230;
            case 4 -> 307;
            case 5 -> 409;
            case 6 -> 512;
            case 7 -> 614;
            case 8 -> 768;
            case 9 -> 614;
            case 10 -> 512;
            case 11 -> 409;
            case 12 -> 307;
            case 13 -> 230;
            case 14 -> 230;
            case 15 -> 230;
            default -> 230;
        };
    }

    // not ported.
    private static int clamp(int sample) {
        if (sample > 32767) {
            return 32767;
        }
        if (sample < -32768) {
            return -32768;
        }
        return sample;
    }
}
