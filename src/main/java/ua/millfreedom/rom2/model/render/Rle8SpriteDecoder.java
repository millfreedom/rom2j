package ua.millfreedom.rom2.model.render;

/**
 * Shared decoder for the engine's 8-bit RLE sprite streams.
 */
public final class Rle8SpriteDecoder {
    /**
     * Utility class.
     * not ported.
     */
    private Rle8SpriteDecoder() {
    }

    /**
     * Native support extracted from DrawSprite_RLE8_to_16 @00454344 and drawSpriteRLE8To16Blend @00454656.
     */
    public static void decodeClipped(
            int x,
            int y,
            int width,
            int height,
            byte[] rleData,
            int clipLeft,
            int clipTop,
            int clipRight,
            int clipBottom,
            Rle8RunWriter writer
    ) {
        decodeClippedInternal(x, y, width, height, rleData, clipLeft, clipTop, clipRight, clipBottom, false, writer);
    }

    /**
     * Native support extracted from DrawSprite_RLE8_to_16_FlipX @0045537D and drawSpriteRLE8To16BlendFlipX @00455617.
     */
    public static void decodeClippedFlipX(
            int x,
            int y,
            int width,
            int height,
            byte[] rleData,
            int clipLeft,
            int clipTop,
            int clipRight,
            int clipBottom,
            Rle8RunWriter writer
    ) {
        decodeClippedInternal(x, y, width, height, rleData, clipLeft, clipTop, clipRight, clipBottom, true, writer);
    }

    /**
     * Native support extracted from DrawSpriteRLE8To16Lut @0045506F.
     */
    public static void decodeClippedSheared(
            int x,
            int y,
            int width,
            int height,
            byte[] rleData,
            int slope,
            int clipLeft,
            int clipTop,
            int clipRight,
            int clipBottom,
            Rle8RunWriter writer
    ) {
        decodeClippedShearedInternal(x, y, width, height, rleData, slope, clipLeft, clipTop, clipRight, clipBottom,
                false, writer);
    }

    /**
     * Native support extracted from DrawSpriteRLE8To16LutFlipX @00456021.
     */
    public static void decodeClippedShearedFlipX(
            int x,
            int y,
            int width,
            int height,
            byte[] rleData,
            int slope,
            int clipLeft,
            int clipTop,
            int clipRight,
            int clipBottom,
            Rle8RunWriter writer
    ) {
        decodeClippedShearedInternal(x, y, width, height, rleData, slope, clipLeft, clipTop, clipRight, clipBottom,
                true, writer);
    }

    /**
     * Native support extracted from DrawSprite_RLE8_to_16 @00454344, drawSpriteRLE8To16Blend @00454656,
     * DrawSprite_RLE8_to_16_FlipX @0045537D, and drawSpriteRLE8To16BlendFlipX @00455617.
     */
    private static void decodeClippedInternal(
            int x,
            int y,
            int width,
            int height,
            byte[] rleData,
            int clipLeft,
            int clipTop,
            int clipRight,
            int clipBottom,
            boolean flipX,
            Rle8RunWriter writer
    ) {
        if (width <= 0 || height <= 0) {
            return;
        }

        if (x + width <= clipLeft || x >= clipRight || y + height <= clipTop || y >= clipBottom) {
            return;
        }

        int src = 0;
        int rowsLeft = height;
        int curY = y;

        if (!flipX) {
            int curX = x;
            final int startX = x;

            while (rowsLeft > 0) {
                int cmd = rleData[src++] & 0xFF;
                int op = cmd & 0xC0;
                int cnt = cmd & 0x3F;

                if (op != 0) {
                    if (op == 0x40) {
                        curY += cnt;
                        rowsLeft -= cnt;
                        if (rowsLeft <= 0) {
                            break;
                        }
                    } else {
                        curX += cnt;
                    }
                } else {
                    int runCount = cnt;
                    if (runCount > 0 && curY >= clipTop && curY < clipBottom) {
                        int visibleLeft = Math.max(curX, clipLeft);
                        int visibleRight = Math.min(curX + runCount, clipRight);
                        if (visibleLeft < visibleRight) {
                            int visibleOffset = src + (visibleLeft - curX);
                            int visibleCount = visibleRight - visibleLeft;
                            writer.writeRun(visibleLeft, curY, rleData, visibleOffset, visibleCount, 1);
                        }
                    }
                    src += runCount;
                    curX += runCount;
                }

                if ((curX - startX) >= width) {
                    curX = startX;
                    curY += 1;
                    rowsLeft -= 1;
                }
            }
            return;
        }

        final int rightX = x + width - 1;
        int curX = rightX;

        while (rowsLeft > 0) {
            int cmd = rleData[src++] & 0xFF;
            int op = cmd & 0xC0;
            int cnt = cmd & 0x3F;

            if (op != 0) {
                if (op == 0x40) {
                    curY += cnt;
                    rowsLeft -= cnt;
                    if (rowsLeft <= 0) {
                        break;
                    }
                } else {
                    curX -= cnt;
                }
            } else {
                int runCount = cnt;
                if (runCount > 0 && curY >= clipTop && curY < clipBottom) {
                    int visibleLeft = Math.max(curX - runCount + 1, clipLeft);
                    int visibleRight = Math.min(curX + 1, clipRight);
                    if (visibleLeft < visibleRight) {
                        int visibleFirstX = visibleRight - 1;
                        int streamSkip = curX - visibleFirstX;
                        int visibleOffset = src + streamSkip;
                        int visibleCount = visibleRight - visibleLeft;
                        writer.writeRun(visibleFirstX, curY, rleData, visibleOffset, visibleCount, -1);
                    }
                }
                src += runCount;
                curX -= runCount;
            }

            if (curX < x) {
                curX = rightX;
                curY += 1;
                rowsLeft -= 1;
            }
        }
    }

    /**
     * Native support extracted from DrawSpriteRLE8To16Lut @0045506F and DrawSpriteRLE8To16LutFlipX @00456021.
     */
    private static void decodeClippedShearedInternal(
            int x,
            int y,
            int width,
            int height,
            byte[] rleData,
            int slope,
            int clipLeft,
            int clipTop,
            int clipRight,
            int clipBottom,
            boolean flipX,
            Rle8RunWriter writer
    ) {
        if (width <= 0 || height <= 0) {
            return;
        }

        int src = 0;
        int rowsLeft = height;
        int curY = y;
        int shearFraction = 0;
        int shearX = truncateDivideBy65536(slope * height);

        if (!flipX) {
            final int startX = x;
            int curX = startX;
            while (rowsLeft > 0) {
                int cmd = rleData[src++] & 0xFF;
                int op = cmd & 0xC0;
                int count = cmd & 0x3F;

                if (op != 0) {
                    if (op == 0x40) {
                        curY += count;
                        rowsLeft -= count;
                        if (rowsLeft <= 0) {
                            break;
                        }
                        int shearStep = shearFraction + count * slope;
                        shearFraction = shearStep & 0xFFFF;
                        shearX -= shearStep >> 16;
                    } else {
                        curX += count;
                    }
                } else {
                    int runCount = count;
                    int destX = curX + shearX;
                    if (runCount > 0 && curY >= clipTop && curY < clipBottom) {
                        int visibleLeft = Math.max(destX, clipLeft);
                        int visibleRight = Math.min(destX + runCount, clipRight);
                        if (visibleLeft < visibleRight) {
                            writer.writeRun(visibleLeft, curY, rleData, src + (visibleLeft - destX),
                                    visibleRight - visibleLeft, 1);
                        }
                    }
                    src += runCount;
                    curX += runCount;
                }

                if ((curX - startX) >= width) {
                    curX = startX;
                    curY++;
                    rowsLeft--;
                    int shearStep = shearFraction + slope;
                    shearFraction = shearStep & 0xFFFF;
                    shearX -= shearStep >> 16;
                }
            }
            return;
        }

        final int rightX = x + width - 1;
        int curX = rightX;
        while (rowsLeft > 0) {
            int cmd = rleData[src++] & 0xFF;
            int op = cmd & 0xC0;
            int count = cmd & 0x3F;

            if (op != 0) {
                if (op == 0x40) {
                    curY += count;
                    rowsLeft -= count;
                    if (rowsLeft <= 0) {
                        break;
                    }
                    int shearStep = shearFraction + count * slope;
                    shearFraction = shearStep & 0xFFFF;
                    shearX -= shearStep >> 16;
                } else {
                    curX -= count;
                }
            } else {
                int runCount = count;
                int destRight = curX + shearX;
                int destLeft = curX - runCount + 1 + shearX;
                if (runCount > 0 && curY >= clipTop && curY < clipBottom) {
                    int visibleLeft = Math.max(destLeft, clipLeft);
                    int visibleRight = Math.min(destRight + 1, clipRight);
                    if (visibleLeft < visibleRight) {
                        int visibleFirstX = visibleRight - 1;
                        writer.writeRun(visibleFirstX, curY, rleData, src + (destRight - visibleFirstX),
                                visibleRight - visibleLeft, -1);
                    }
                }
                src += runCount;
                curX -= runCount;
            }

            if (curX < x) {
                curX = rightX;
                curY++;
                rowsLeft--;
                int shearStep = shearFraction + slope;
                shearFraction = shearStep & 0xFFFF;
                shearX -= shearStep >> 16;
            }
        }
    }

    /**
     * Native support extracted from DrawSpriteRLE8To16Lut @0045506F signed 16.16 fixed-point setup.
     */
    private static int truncateDivideBy65536(int value) {
        return value / 0x10000;
    }
}
