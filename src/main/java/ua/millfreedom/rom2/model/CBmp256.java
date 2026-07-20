package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.color.RGB32;
import ua.millfreedom.rom2.model.palette.Palette256;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import static ua.millfreedom.rom2.Globals.gameFileManager;

public final class CBmp256 extends CGameBitmap {
    private static final int PALETTE_START_OFFSET = 0x36;

    /**
     * Native: CBmp256::CBmp256 @004259AF.
     * Fully ported.
     */
    public CBmp256(int width, int height) {
        int[] indices = new int[Math.multiplyExact(width, height)];
        this.palette256 = null;
        this.frameCount = 1;
        this.frames = List.of(
                GameBitmapFrame.bitmap(width, height, indices)
        );
    }

    /**
     * Native: CBmp256::CBmp256 @0042566A.
     * Fully ported.
     */
    public CBmp256(String filename) {
        ByteBuffer source = gameFileManager.get(filename).duplicate().order(ByteOrder.LITTLE_ENDIAN);

        int w = source.getInt(0x12);
        int h = source.getInt(0x16);
        int colorsUsed = source.getInt(0x2E);

        source.position(PALETTE_START_OFFSET);
        this.palette256 = readPaletteFromBmp(source, colorsUsed);
        int[] indices = new int[Math.multiplyExact(w, h)];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = Byte.toUnsignedInt(source.get());
        }

        flipVerticalInPlace(indices, w, h);

        this.frameCount = 1;
        this.dataSize = 0x408L + (long) indices.length;
        this.frames = List.of(
                GameBitmapFrame.bitmap(w, h, indices)
        );
    }

    /**
     * Native constructor palette step inside CBmp256::CBmp256 @0042566A.
     * Fully ported.
     */
    private static Palette256 readPaletteFromBmp(ByteBuffer source, int colorsUsed) {
        Palette256 palette256 = Palette256.create();
        int[] entries = palette256.data();
        for (int i = 0; i < entries.length; i++) {
            entries[i] = RGB32.BLACK;
        }

        int colorsToRead = colorsUsed == 0 ? entries.length : colorsUsed;
        for (int i = 0; i < colorsToRead; i++) {
            int blue = Byte.toUnsignedInt(source.get());
            int green = Byte.toUnsignedInt(source.get());
            int red = Byte.toUnsignedInt(source.get());
            source.get();
            entries[i] = RGB32.from(red, green, blue);
        }
        return palette256;
    }

    /**
     * Native support extracted from CBmp256::MirrorY @00425884.
     * Fully ported.
     */
    private static void flipVerticalInPlace(int[] pixels, int rowPixels, int height) {
        int[] tmp = new int[rowPixels];
        for (int y = 0, half = height / 2; y < half; y++) {
            int top = y * rowPixels;
            int bot = (height - 1 - y) * rowPixels;

            System.arraycopy(pixels, top, tmp, 0, rowPixels);
            System.arraycopy(pixels, bot, pixels, top, rowPixels);
            System.arraycopy(tmp, 0, pixels, bot, rowPixels);
        }
    }

    /**
     * vtbl +0x18: CBmp256::Draw @00425ABD.
     * Fully ported.
     */
    @Override
    public void draw(int x, int y, int nFrameIndex, Object paletteOverride, boolean bFlipX) {
        int paletteIndex = (Integer) paletteOverride;
        GameBitmapFrame frame = frames.getFirst();
        Globals.renderer.blitIndexedToScreen(x, y, 0, 0, frame.width(), frame.height(),
                frame.pixels(), frame.width(), frame.height(), palette.paletteData[paletteIndex].data());
    }

    /**
     * vtbl +0x34: CBmp256::DrawRect @00425A63.
     * Fully ported.
     */
    public void drawRect(int x, int y, int srcLeft, int srcTop, int srcRight, int srcBottom, int paletteIndex) {
        GameBitmapFrame frame = frames.getFirst();
        Globals.renderer.blitIndexedToScreen(x, y, srcLeft, srcTop, srcRight, srcBottom,
                frame.pixels(), frame.width(), frame.height(), palette.paletteData[paletteIndex].data());
    }

    /**
     * vtbl +0x38: CBmp256::DrawRectAdditive @00425B1E.
     * Fully ported.
     */
    public void drawRectAdditive(int x, int y, int srcLeft, int srcTop, int srcRight, int srcBottom, int paletteIndex) {
        GameBitmapFrame frame = frames.getFirst();
        Globals.renderer.blitIndexedToScreenAdditive(x, y, srcLeft, srcTop, srcRight, srcBottom,
                frame.pixels(), frame.width(), frame.height(), palette.paletteData[paletteIndex].data());
    }

    /**
     * Java helper for whole-frame masked indexed blits used by UI call sites.
     * not ported.
     */
    public void drawRectMasked(int x, int y) {
        drawRectMasked(x, y, 0, 0, xSizeOf(0), ySizeOf(0));
    }

    /**
     * Java helper for sub-rect masked indexed blits used by UI call sites.
     * not ported.
     */
    public void drawRectMasked(int x, int y, int srcLeft, int srcTop, int srcRight, int srcBottom) {
        GameBitmapFrame frame = frames.getFirst();
        Globals.renderer.blitIndexedToScreenMasked(x, y, srcLeft, srcTop, srcRight, srcBottom,
                frame.pixels(), frame.width(), frame.height(), palette.paletteData[0].data());
    }

    /**
     * vtbl +0x2c: CBmp256::MirrorY @00425884.
     * Fully ported.
     */
    @Override
    public void mirrorY() {
        GameBitmapFrame frame = frames.getFirst();
        flipVerticalInPlace(frame.pixels(), frame.width(), frame.height());
    }

    /**
     * vtbl +0x20: CBmp256::xSizeOf @00425B9E.
     * Fully ported.
     */
    @Override
    public int xSizeOf(int i) {
        return frames.getFirst().width();
    }

    /**
     * vtbl +0x24: CBmp256::ySizeOf @00425BB3.
     * Fully ported.
     */
    @Override
    public int ySizeOf(int i) {
        return frames.getFirst().height();
    }

    /**
     * vtbl +0x30: CBmp256::GetBytesPerPixel @00428470.
     * Fully ported.
     */
    @Override
    public int getBytesPerPixel() {
        return 1;
    }

}
