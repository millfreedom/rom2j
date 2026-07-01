package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.color.RGB16;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static ua.millfreedom.rom2.Globals.gameFileManager;

public final class CBmp64k extends CGameBitmap {
    private static final int BITMAP_PIXEL_START_OFFSET = 0x36;

    /**
     * Native: CBmp64k::CBmp64k @0042505C.
     * Fully ported.
     */
    public CBmp64k(int width, int height) {
        int linearSize = Math.multiplyExact(width, height);
        int bytesCount = Math.multiplyExact(linearSize, 2);

        RGB16[] pixels = new RGB16[linearSize];
        Arrays.fill(pixels, RGB16.BLACK);
        byte[] frameData = new byte[bytesCount];

        this.frameCount = 1;
        this.dataSize = frameData.length;
        this.surface = new GameBitmapSurface(width, height, pixels);
        this.frames = List.of(
                new GameBitmapFrame(width, height, frameData.length, frameData)
        );
        this.palette256 = null;
    }

    /**
     * Native: CBmp64k::CBmp64k @00424E4B.
     */
    public CBmp64k(String filename) {
        ByteBuffer source = gameFileManager.get(filename).duplicate().order(ByteOrder.LITTLE_ENDIAN);

        int w = source.getInt(0x12);
        int h = source.getInt(0x16);

        int linearSize = Math.multiplyExact(w, h);
        int bytesCount = Math.multiplyExact(linearSize, 2);

        RGB16[] pixels = new RGB16[linearSize];
        byte[] frameData = new byte[bytesCount];

        source.position(BITMAP_PIXEL_START_OFFSET);
        ByteBuffer bb = ByteBuffer.wrap(frameData).order(ByteOrder.LITTLE_ENDIAN);
        for (int p = 0; p < linearSize; p++) {
            int blue = Byte.toUnsignedInt(source.get());
            int green = Byte.toUnsignedInt(source.get());
            int red = Byte.toUnsignedInt(source.get());
            RGB16 rgb16 = RGB16.from(red, green, blue);
            pixels[p] = rgb16;
            bb.putShort(rgb16.val());
        }

        this.frameCount = 1;
        this.dataSize = frameData.length + 8L;

        this.surface = new GameBitmapSurface(w, h, pixels);
        this.frames = List.of(
                new GameBitmapFrame(w, h, frameData.length, frameData)
        );

        // 64k bitmap => no palette table source
        this.palette256 = null;
    }

    /**
     * Native: CBmp64k::LoadBmp24Pixels @004250F5.
     * Fully ported.
     */
    public void loadBmp24Pixels(String filename, CGameBitmap maskBitmap) {
        ByteBuffer source = gameFileManager.get(filename).duplicate().order(ByteOrder.LITTLE_ENDIAN);
        int width = source.getInt(0x12);
        int height = source.getInt(0x16);
        int linearSize = Math.multiplyExact(width, height);

        source.position(BITMAP_PIXEL_START_OFFSET);
        ByteBuffer frameBytes = ByteBuffer.wrap(frames.getFirst().data()).order(ByteOrder.LITTLE_ENDIAN);
        RGB16[] pixels = surface.pixels();
        for (int pixelIndex = 0; pixelIndex < linearSize; pixelIndex++) {
            int blue = Byte.toUnsignedInt(source.get());
            int green = Byte.toUnsignedInt(source.get());
            int red = Byte.toUnsignedInt(source.get());
            RGB16 rgb16 = RGB16.from(red, green, blue);
            pixels[pixelIndex] = rgb16;
            frameBytes.putShort(pixelIndex * 2, rgb16.val());
        }

        if (maskBitmap != null) {
            source.get(maskBitmap.frames.getFirst().data(), 0, linearSize);
        }
    }

    /**
     * Native: CBmp64k::DumpBmp24PixelsWithMask @00425273.
     * Fully ported for CBmp64k portrait-cache files; native writes a tight 24-bit BMP pixel stream followed by raw
     * mask bytes when a paired mask bitmap is supplied.
     */
    public void dumpBmp24PixelsWithMask(String filename, CGameBitmap maskBitmap) {
        int width = xSizeOf(0);
        int height = ySizeOf(0);
        int pixelCount = Math.multiplyExact(width, height);
        int rgbByteCount = Math.multiplyExact(pixelCount, 3);
        int maskByteCount = maskBitmap == null ? 0 : pixelCount;
        int headerSize = BITMAP_PIXEL_START_OFFSET;
        int fileSize = headerSize + rgbByteCount + maskByteCount;

        ByteArrayOutputStream file = new ByteArrayOutputStream(fileSize);
        ByteBuffer header = ByteBuffer.allocate(headerSize).order(ByteOrder.LITTLE_ENDIAN);
        header.putShort((short) 0x4D42);
        header.putInt(fileSize);
        header.putShort((short) 0);
        header.putShort((short) 0);
        header.putInt(headerSize);
        header.putInt(0x28);
        header.putInt(width);
        header.putInt(height);
        header.putShort((short) 1);
        header.putShort((short) 24);
        header.putInt(0);
        header.putInt(rgbByteCount);
        header.putInt(0);
        header.putInt(0);
        header.putInt(0);
        header.putInt(0);
        file.writeBytes(header.array());

        RGB16[] pixels = surface.pixels();
        for (int i = 0; i < pixelCount; i++) {
            RGB16 pixel = pixels[i];
            file.write(pixel.b());
            file.write(pixel.g());
            file.write(pixel.r());
        }
        if (maskBitmap != null) {
            file.write(maskBitmap.frames.getFirst().data(), 0, pixelCount);
        }

        try {
            Files.write(Path.of(filename), file.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * vtbl +0x18: CBmp64k::Draw @004254EA.
     */
    @Override
    public void draw(int x, int y, int nFrameIndex, Object paletteOverride, boolean bFlipX) {
        Globals.renderer.blitToScreen(x, y, 0, 0, surface.width(), surface.height(),
                surface.pixels(), surface.width(), surface.height());
    }

    /**
     * Java helper for whole-bitmap masked draws.
     * not ported.
     */
    public void drawRectMasked(int x, int y) {
        drawRectMasked(x, y, 0, 0, surface.width(), surface.height());
    }

    /**
     * vtbl +0x34: CBmp64k::DrawRect @0042553B.
     */
    public void drawRect(int x, int y, int srcLeft, int srcTop, int srcRight, int srcBottom) {
        Globals.renderer.blitToScreen(x, y, srcLeft, srcTop, srcRight, srcBottom,
                surface.pixels(), surface.width(), surface.height());
    }

    /**
     * vtbl +0x38: CBmp64k::DrawRectMasked @00425585.
     */
    public void drawRectMasked(int x, int y, int srcLeft, int srcTop, int srcRight, int srcBottom) {
        Globals.renderer.blitToScreenMasked(x, y, srcLeft, srcTop, srcRight, srcBottom,
                surface.pixels(), surface.width(), surface.height());
    }

    /**
     * Native support extracted from CBmp64k::DrawRect @0042553B and BlitToScreen @004538DD when a
     * CBmp64k destination has been selected by CGameBitmap::SetAsActiveRenderTarget @00424437.
     */
    public void drawRectTo(CBmp64k target, int x, int y, int srcLeft, int srcTop, int srcRight, int srcBottom) {
        blitRectToBitmap(target, x, y, srcLeft, srcTop, srcRight, srcBottom, false);
    }

    /**
     * Native support extracted from CBmp64k::DrawRectMasked @00425585 and BlitToScreenMasked @00453BCA when a
     * CBmp64k destination has been selected by CGameBitmap::SetAsActiveRenderTarget @00424437.
     */
    public void drawRectMaskedTo(CBmp64k target, int x, int y, int srcLeft, int srcTop, int srcRight, int srcBottom) {
        blitRectToBitmap(target, x, y, srcLeft, srcTop, srcRight, srcBottom, true);
    }

    /**
     * vtbl +0x3c: CBmp64k::DrawRectAdditive @004255CF.
     */
    public void drawRectAdditive(int x, int y, int srcLeft, int srcTop, int srcRight, int srcBottom) {
        Globals.renderer.blitToScreenAdditive(x, y, srcLeft, srcTop, srcRight, srcBottom,
                surface.pixels(), surface.width(), surface.height());
    }

    /**
     * vtbl +0x2c: CBmp64k::MirrorY @004253B9.
     */
    @Override
    public void mirrorY() {
        int width = surface.width();
        int height = surface.height();
        RGB16[] pixels = surface.pixels();
        RGB16[] tmpRow = new RGB16[width];
        for (int y = 0, half = height / 2; y < half; y++) {
            int top = y * width;
            int bottom = (height - 1 - y) * width;
            System.arraycopy(pixels, top, tmpRow, 0, width);
            System.arraycopy(pixels, bottom, pixels, top, width);
            System.arraycopy(tmpRow, 0, pixels, bottom, width);
        }
        mirrorFrameBytesInPlace();
    }

    /**
     * vtbl +0x20: CBmp64k::xSizeOf @0042563F.
     */
    @Override
    public int xSizeOf(int i) {
        return surface.width();
    }

    /**
     * vtbl +0x24: CBmp64k::ySizeOf @00425654.
     */
    @Override
    public int ySizeOf(int i) {
        return surface.height();
    }

    /**
     * Java support for direct CBmp64k surface composition paths.
     * not ported.
     */
    public void syncFrameBytesFromSurface() {
        ByteBuffer buffer = ByteBuffer.wrap(frames.getFirst().data()).order(ByteOrder.LITTLE_ENDIAN);
        for (RGB16 pixel : surface.pixels()) {
            buffer.putShort(pixel == null ? RGB16.BLACK.val() : pixel.val());
        }
    }

    /**
     * Native support extracted from BlitToScreen @004538DD and BlitToScreenMasked @00453BCA.
     */
    private void blitRectToBitmap(
            CBmp64k target,
            int dstX,
            int dstY,
            int srcLeft,
            int srcTop,
            int srcRight,
            int srcBottom,
            boolean zeroTransparent
    ) {
        int width = srcRight - srcLeft;
        int height = srcBottom - srcTop;
        int sourceWidth = surface.width();
        int sourceHeight = surface.height();
        int targetWidth = target.surface.width();
        RGB16[] sourcePixels = surface.pixels();
        RGB16[] targetPixels = target.surface.pixels();

        for (int row = 0; row < height; row++) {
            int sourceIndex = (sourceHeight - 1 - srcTop - row) * sourceWidth + srcLeft;
            int targetIndex = (dstY + row) * targetWidth + dstX;
            for (int x = 0; x < width; x++) {
                RGB16 pixel = sourcePixels[sourceIndex + x];
                if (!zeroTransparent || pixel.val() != 0) {
                    targetPixels[targetIndex + x] = pixel;
                }
            }
        }
    }

    /**
     * Java helper for keeping the packed 16-bit frame bytes aligned with {@link #surface} row swaps.
     * not ported.
     */
    private void mirrorFrameBytesInPlace() {
        GameBitmapFrame frame = frames.getFirst();
        int rowBytes = Math.multiplyExact(surface.width(), 2);
        byte[] data = frame.data();
        byte[] tmpRow = new byte[rowBytes];
        for (int y = 0, half = surface.height() / 2; y < half; y++) {
            int top = y * rowBytes;
            int bottom = (surface.height() - 1 - y) * rowBytes;
            System.arraycopy(data, top, tmpRow, 0, rowBytes);
            System.arraycopy(data, bottom, data, top, rowBytes);
            System.arraycopy(tmpRow, 0, data, bottom, rowBytes);
        }
    }
}
