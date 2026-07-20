package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.model.palette.Palette256;
import ua.millfreedom.rom2.model.render.A16SpriteDecoder;
import ua.millfreedom.rom2.model.render.Rle8SpriteDecoder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ua.millfreedom.rom2.Globals.gameFileManager;

/**
 * Java-only load boundary that expands compressed sprite commands exactly once into canonical int pixel codes.
 * Encoded bytes never escape this class. RLE frames use unsigned palette indices with -1 transparent gaps; A16 frames
 * retain one unsigned packed index/alpha code per pixel.
 */
final class IndexedSpriteResource {
    private static final int EMBEDDED_PALETTE_FLAG = 0x8000_0000;
    private static final int FRAME_COUNT_MASK = 0x7FFF_FFFF;

    /**
     * not ported. Utility class.
     */
    private IndexedSpriteResource() {
    }

    /**
     * Java support for load-time RLE8 expansion into one unsigned-index frame set.
     * not ported.
     */
    static DecodedSprite loadRle8(String resourceName) {
        return loadFrames(resourceName, IndexedSpriteResource::decodeRle8, GameBitmapFrame::indexedSprite);
    }

    /**
     * Java support for load-time RLE4 expansion into one unsigned-index frame set.
     * not ported.
     */
    static DecodedSprite loadRle4(String resourceName) {
        return loadFrames(resourceName, IndexedSpriteResource::decodeRle4, GameBitmapFrame::indexedSprite);
    }

    /**
     * Java support for load-time A16 expansion into one unsigned packed index/alpha frame set.
     * not ported.
     */
    static DecodedSprite loadA16(String resourceName) {
        return loadFrames(resourceName, IndexedSpriteResource::decodeA16, GameBitmapFrame::a16Sprite);
    }

    /**
     * Java support for parsing frame records locally and returning only canonical int-code frames.
     * not ported.
     */
    private static DecodedSprite loadFrames(
            String resourceName,
            FrameDecoder decoder,
            FrameFactory frameFactory
    ) {
        ByteBuffer source = gameFileManager.get(resourceName).duplicate().order(ByteOrder.LITTLE_ENDIAN);
        int fileLength = source.limit();
        if (fileLength == 0) {
            return new DecodedSprite(0, null, List.of());
        }
        int rawFrameCount = source.getInt(fileLength - Integer.BYTES);
        int frameCount = rawFrameCount & FRAME_COUNT_MASK;
        Palette256 embeddedPalette = null;
        if ((rawFrameCount & EMBEDDED_PALETTE_FLAG) != 0) {
            source.position(0);
            embeddedPalette = Palette256.read(source);
        } else {
            source.position(0);
        }

        List<GameBitmapFrame> decodedFrames = new ArrayList<>(frameCount);
        for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
            int width = source.getInt();
            int height = source.getInt();
            int dataSize = source.getInt();
            byte[] encoded = new byte[dataSize];
            source.get(encoded);
            decodedFrames.add(frameFactory.create(width, height, decoder.decode(width, height, encoded)));
        }
        return new DecodedSprite(fileLength, embeddedPalette, decodedFrames);
    }

    /**
     * Native support extracted from DrawSprite_RLE8_to_16 @00454344.
     */
    private static int[] decodeRle8(int width, int height, byte[] encoded) {
        int[] pixels = new int[width * height];
        Arrays.fill(pixels, GameBitmapFrame.TRANSPARENT_INDEX);
        Rle8SpriteDecoder.decodeClipped(
                0,
                0,
                width,
                height,
                encoded,
                0,
                0,
                width,
                height,
                (runX, runY, paletteIndices, offset, count, stepX) -> {
                    int destination = runY * width + runX;
                    for (int i = 0; i < count; i++) {
                        pixels[destination] = Byte.toUnsignedInt(paletteIndices[offset + i]);
                        destination += stepX;
                    }
                }
        );
        return pixels;
    }

    /**
     * Native support extracted from DrawSprite_RLE4_to_16 @004540D1.
     */
    private static int[] decodeRle4(int width, int height, byte[] encoded) {
        int[] pixels = new int[width * height];
        Arrays.fill(pixels, GameBitmapFrame.TRANSPARENT_INDEX);
        int source = 0;
        int currentX = 0;
        int currentY = 0;
        int rowsLeft = height;

        while (rowsLeft > 0) {
            int command = Byte.toUnsignedInt(encoded[source++]);
            int operation = command & 0xC0;
            int count = command & 0x3F;
            if (operation != 0) {
                if (operation == 0x40) {
                    currentY += count;
                    rowsLeft -= count;
                    if (rowsLeft <= 0) {
                        break;
                    }
                } else {
                    currentX += count;
                }
            } else {
                for (int i = 0; i < count; i++) {
                    int packedIndices = Byte.toUnsignedInt(encoded[source++]);
                    pixels[currentY * width + currentX++] = packedIndices & 0x0F;
                    int highIndex = packedIndices >>> 4;
                    if (highIndex == 0) {
                        break;
                    }
                    pixels[currentY * width + currentX++] = highIndex;
                }
            }

            if (currentX >= width) {
                currentX = 0;
                currentY++;
                rowsLeft--;
            }
        }
        return pixels;
    }

    /**
     * Native support extracted from DrawSprite_A16 @0045889B.
     */
    private static int[] decodeA16(int width, int height, byte[] encoded) {
        int[] pixels = new int[width * height];
        A16SpriteDecoder.decodeClipped(
                0,
                0,
                width,
                height,
                encoded,
                0,
                0,
                width,
                height,
                (runX, runY, encodedPixels, offset, count, stepX) -> {
                    int destination = runY * width + runX;
                    ByteBuffer source = ByteBuffer.wrap(encodedPixels)
                            .order(ByteOrder.LITTLE_ENDIAN)
                            .position(offset);
                    for (int i = 0; i < count; i++) {
                        pixels[destination] = Short.toUnsignedInt(source.getShort());
                        destination += stepX;
                    }
                }
        );
        return pixels;
    }

    /**
     * Java-only local frame decoder; encoded bytes cannot escape the load call.
     * not ported.
     */
    @FunctionalInterface
    private interface FrameDecoder {
        /**
         * not ported.
         */
        int[] decode(int width, int height, byte[] encoded);
    }

    /**
     * Java-only canonical-frame factory selected once per decoded resource format.
     * not ported.
     */
    @FunctionalInterface
    private interface FrameFactory {
        /**
         * not ported.
         */
        GameBitmapFrame create(int width, int height, int[] pixels);
    }

    /**
     * Java-only result of one complete sprite resource parse.
     */
    record DecodedSprite(int resourceSize, Palette256 embeddedPalette, List<GameBitmapFrame> frames) {
    }
}
