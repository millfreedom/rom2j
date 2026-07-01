package ua.millfreedom.rom2.model.render;

import ua.millfreedom.rom2.model.color.RGB16;
import ua.millfreedom.rom2.model.color.RGB32;
import ua.millfreedom.rom2.model.palette.Palette16;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static ua.millfreedom.rom2.model.color.Utils.clamp255;

/**
 * Shared decoder for the engine's CA16 word-coded sprite streams.
 */
public final class A16SpriteDecoder {
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
     * Source color lookup matches the native `pPaletteData + encodedPixel` page-offset addressing.
     */
    public static RGB32 composeColor(int encodedPixel, RGB32 destinationColor, Palette16[] palettePages) {
        int color8 = (encodedPixel >>> 1) & 0xFF;
        int page4 = (encodedPixel >>> 9) & 0x0F;
        RGB32 srcPart = palettePages[page4].data()[color8].toRGB32();
        RGB32 dstPart = destinationColor.withShade(page4);
        return RGB32.from(
                clamp255(srcPart.r() + dstPart.r()),
                clamp255(srcPart.g() + dstPart.g()),
                clamp255(srcPart.b() + dstPart.b())
        );
    }

    /**
     * Native support extracted from CA16Font::DrawTextInternal @0045E8FD explicit color-table dispatch.
     * bits 1..8 = 256-entry source index
     * bits 9..12 = 4-bit blend factor
     * blend factor applies:
     * directly to source
     * inversely to destination
     */
    public static RGB32 composeColor(int encodedPixel, RGB32 destinationColor, RGB16[] palette16) {
        int color8 = (encodedPixel >>> 1) & 0xFF;
        int page4 = (encodedPixel >>> 9) & 0x0F;
        RGB32 srcPart = palette16[color8].toRGB32().withBrightness(page4);
        RGB32 dstPart = destinationColor.withShade(page4);
        return RGB32.from(
                clamp255(srcPart.r() + dstPart.r()),
                clamp255(srcPart.g() + dstPart.g()),
                clamp255(srcPart.b() + dstPart.b())
        );
    }
}
