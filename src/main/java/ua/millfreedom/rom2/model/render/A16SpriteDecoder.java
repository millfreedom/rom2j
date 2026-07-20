package ua.millfreedom.rom2.model.render;

import ua.millfreedom.rom2.model.color.RGB32;
import ua.millfreedom.rom2.model.palette.Palette16;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Shared decoder for the engine's CA16 word-coded sprite streams.
 */
public final class A16SpriteDecoder {
    private static final int A16_BLEND_DENOMINATOR = 16;
    private static final int ARGB_ALPHA_MAX = 0xFF;

    /**
     * Utility class.
     * not ported.
     */
    private A16SpriteDecoder() {
    }

    /**
     * Native support extracted from DrawSprite_A16 @0045889B.
     */
    public static void decodeClipped(
            int x,
            int y,
            int width,
            int height,
            byte[] a16Data,
            int clipLeft,
            int clipTop,
            int clipRight,
            int clipBottom,
            A16RunWriter writer
    ) {
        decodeClippedInternal(x, y, width, height, a16Data, clipLeft, clipTop, clipRight, clipBottom, false, writer);
    }

    /**
     * Native support extracted from DrawSprite_A16_FlipX @00458C10.
     */
    public static void decodeClippedFlipX(
            int x,
            int y,
            int width,
            int height,
            byte[] a16Data,
            int clipLeft,
            int clipTop,
            int clipRight,
            int clipBottom,
            A16RunWriter writer
    ) {
        decodeClippedInternal(x, y, width, height, a16Data, clipLeft, clipTop, clipRight, clipBottom, true, writer);
    }

    /**
     * Native support extracted from DrawSprite_A16 @0045889B and DrawSprite_A16_FlipX @00458C10.
     */
    private static void decodeClippedInternal(
            int x,
            int y,
            int width,
            int height,
            byte[] a16Data,
            int clipLeft,
            int clipTop,
            int clipRight,
            int clipBottom,
            boolean flipX,
            A16RunWriter writer
    ) {
        if (width <= 0 || height <= 0) {
            return;
        }

        if (x + width <= clipLeft || x >= clipRight || y + height <= clipTop || y >= clipBottom) {
            return;
        }

        ByteBuffer bb = ByteBuffer.wrap(a16Data)
                .order(ByteOrder.LITTLE_ENDIAN);
        int rowsLeft = height;
        int curY = y;

        if (!flipX) {
            int curX = x;
            final int startX = x;

            while (rowsLeft > 0) {
                int cmd = bb.getShort() & 0xFFFF;

                if ((cmd & 0x4000) != 0) {
                    int skipRows = cmd & 0x3FFF;
                    curY += skipRows;
                    rowsLeft -= skipRows;
                    if (rowsLeft <= 0) {
                        break;
                    }
                } else if ((cmd & 0x8000) != 0) {
                    curX += cmd & 0x3FFF;
                } else {
                    int runCount = cmd;
                    if (runCount > 0 && curY >= clipTop && curY < clipBottom) {
                        int visibleLeft = Math.max(curX, clipLeft);
                        int visibleRight = Math.min(curX + runCount, clipRight);
                        if (visibleLeft < visibleRight) {
                            int visibleOffset = bb.position() + ((visibleLeft - curX) << 1);
                            int visibleCount = visibleRight - visibleLeft;
                            writer.writeRun(visibleLeft, curY, a16Data, visibleOffset, visibleCount, 1);
                        }
                    }
                    bb.position(bb.position() + (runCount << 1));
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
            int cmd = bb.getShort() & 0xFFFF;

            if ((cmd & 0x4000) != 0) {
                int skipRows = cmd & 0x3FFF;
                curY += skipRows;
                rowsLeft -= skipRows;
                if (rowsLeft <= 0) {
                    break;
                }
            } else if ((cmd & 0x8000) != 0) {
                curX -= cmd & 0x3FFF;
            } else {
                int runCount = cmd;
                if (runCount > 0 && curY >= clipTop && curY < clipBottom) {
                    int visibleLeft = Math.max(curX - runCount + 1, clipLeft);
                    int visibleRight = Math.min(curX + 1, clipRight);
                    if (visibleLeft < visibleRight) {
                        int visibleFirstX = visibleRight - 1;
                        int streamSkip = curX - visibleFirstX;
                        int visibleOffset = bb.position() + (streamSkip << 1);
                        int visibleCount = visibleRight - visibleLeft;
                        writer.writeRun(visibleFirstX, curY, a16Data, visibleOffset, visibleCount, -1);
                    }
                }
                bb.position(bb.position() + (runCount << 1));
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
     * Native support extracted from DrawSprite_A16 @0045889B and DrawSprite_A16_FlipX @00458C10.
     * Converts the native preweighted source contribution into straight ARGB for source-over composition.
     */
    public static int sourceColor(int encodedPixel, Palette16[] palettePages) {
        int color8 = (encodedPixel >>> 1) & 0xFF;
        int alphaLevel = (encodedPixel >>> 9) & 0x0F;
        if (alphaLevel == 0) {
            return RGB32.TBLACK;
        }
        int sourceContribution = palettePages[alphaLevel].data()[color8];
        return toStraightArgb(sourceContribution, alphaLevel);
    }

    /**
     * Native support extracted from CA16Font::DrawTextInternal @0045E8FD explicit color-table dispatch.
     * Converts the native LUT-weighted source contribution into straight ARGB for source-over composition.
     */
    public static int sourceColor(int encodedPixel, int[] palette) {
        int color8 = (encodedPixel >>> 1) & 0xFF;
        int alphaLevel = (encodedPixel >>> 9) & 0x0F;
        if (alphaLevel == 0) {
            return RGB32.TBLACK;
        }
        // CGamePalette mode 4 page index 0 stores direct brightness level 1, so base-palette dispatch uses level + 1.
        int sourceContribution = RGB32.withBrightness(palette[color8], alphaLevel + 1);
        return toStraightArgb(sourceContribution, alphaLevel);
    }

    /**
     * not ported. Reconstructs straight ARGB from the native A16 preweighted RGB contribution.
     */
    private static int toStraightArgb(int sourceContribution, int alphaLevel) {
        int alpha = (alphaLevel * ARGB_ALPHA_MAX + A16_BLEND_DENOMINATOR / 2) / A16_BLEND_DENOMINATOR;
        return RGB32.ARGB(
                unpremultiplySourceChannel(RGB32.r(sourceContribution), alpha),
                unpremultiplySourceChannel(RGB32.g(sourceContribution), alpha),
                unpremultiplySourceChannel(RGB32.b(sourceContribution), alpha),
                alpha
        );
    }

    /**
     * not ported. Reconstructs one straight color channel from a native A16 source contribution.
     */
    private static int unpremultiplySourceChannel(int sourceContribution, int alpha) {
        return Math.min(ARGB_ALPHA_MAX,
                (sourceContribution * ARGB_ALPHA_MAX + alpha / 2) / alpha);
    }
}
