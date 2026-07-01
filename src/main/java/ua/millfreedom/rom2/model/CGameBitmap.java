package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.model.palette.CGamePalette;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palette256;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ua.millfreedom.rom2.Globals.gameFileManager;

/**
 * CGameBitmap
 */
public abstract class CGameBitmap implements MfcSerializable {

    // Java helper backing the loaded resource bytes; not a native field.
    public ByteBuffer buf;
    //0x04
    public int frameCount;
    //0x08
    public long dataSize;
    //0x0c
    public List<GameBitmapFrame> frames;
    //0x10
    public GameBitmapSurface surface;
    //0x14
    public final CGamePalette palette = new CGamePalette();
    //0x20
    public Palette256 palette256;

    /**
     * Native: CGameBitmap::CGameBitmap @00423E86.
     */
    public CGameBitmap() {
    }

    /**
     * Native: CGameBitmap::CGameBitmap @00423EDD.
     * Fully ported. Native deep-copies frame bytes and palette pages, while sharing the source Palette256 pointer.
     */
    protected CGameBitmap(CGameBitmap source) {
        if (source.buf != null) {
            buf = source.buf.duplicate();
            buf.order(source.buf.order());
        }
        frameCount = source.frameCount;
        dataSize = source.dataSize;
        frames = new ArrayList<>(frameCount);
        for (int i = 0; i < frameCount; i++) {
            GameBitmapFrame frame = source.frames.get(i);
            frames.add(new GameBitmapFrame(
                    frame.xSize(),
                    frame.ySize(),
                    frame.dataSize(),
                    Arrays.copyOf(frame.data(), frame.dataSize())
            ));
        }
        // Java keeps sprite frame storage in `frames`; `surface` is populated only for render-target bitmaps.
        if (source.surface != null) {
            surface = new GameBitmapSurface(
                    source.surface.width(),
                    source.surface.height(),
                    Arrays.copyOf(source.surface.pixels(), source.surface.pixels().length)
            );
        }
        palette.nPages = source.palette.nPages;
        palette.paletteData = new Palette16[palette.nPages];
        for (int i = 0; i < palette.nPages; i++) {
            palette.paletteData[i] = new Palette16(
                    Arrays.copyOf(source.palette.paletteData[i].data(), source.palette.paletteData[i].data().length)
            );
        }
        palette256 = source.palette256;
    }

    /**
     * vtbl +0x08: CObject::Serialize @00401970.
     */
    @Override
    public void serialize(CArchive ar) {
        // Native CObject::Serialize is a no-op.
    }

    /**
     * Native: CGameBitmap::CGameBitmap @00423CA1.
     */
    public CGameBitmap(String name) {
        buf = gameFileManager.get(name);
        dataSize = buf.limit();
        frameCount = buf.getInt((int) (dataSize - 4));
        frames = getFrames(buf, frameCount);
    }

    /**
     * Native: CGameBitmap::GetDataSize @00480BB0.
     * Java port status: fully ported.
     */
    public int getDataSize() {
        return (int) dataSize;
    }

    /**
     * Native support extracted from CGameBitmap::CGameBitmap @00423CA1.
     */
    List<GameBitmapFrame> getFrames(ByteBuffer b, int count) {
        List<GameBitmapFrame> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int w = b.getInt();
            int h = b.getInt();
            int sz = b.getInt();
            byte[] data = new byte[sz];
            b.get(data);
            result.add(new GameBitmapFrame(w, h, sz, data));
        }
        return result;
    }

    /**
     * Native: CGameBitmap::InitPalette @00424390.
     */
    public void initPalette(int nPages, int nMode, int bGamma) {
        palette.free();
        palette.init(palette256, nPages, nMode, bGamma);

    }

    /**
     * not ported.
     */
    @Override
    public String toString() {
        return "CGameBitmap{" +
                "frameCount=" + frameCount +
                ",\ndataSize=" + dataSize +
                ",\nframes=" + frames +
                ",\npalette=" + palette +
                ",\npalette256 =" + palette256 +
                '}';
    }

    /**
     * vtbl +0x14: CGameBitmap::DrawWithPalette @00428430.
     * Native stub.
     * Fully ported.
     */
    public void drawWithPalette(int x, int y, int nFrameIndex, int nPaletteIndex, CGamePalette paletteOverride, boolean bFlipX) {
    }

    /**
     * vtbl +0x18: CGameBitmap::Draw @00428420.
     * Native stub.
     * Fully ported.
     */
    public void draw(int x, int y, int nFrameIndex, Object palette, boolean bFlipX) {
    }

    /**
     * vtbl +0x1c: CGameBitmap::DrawAlpha @00428440.
     * Native stub.
     * Fully ported.
     */
    public void drawAlpha(int x, int y, int nFrameIndex, int brightness, boolean bFlipX) {
    }

    /**
     * vtbl +0x20: CGameBitmap::xSizeOf @004243C6.
     */
    public int xSizeOf(int i) {
        return frames.get(i).xSize();
    }

    /**
     * vtbl +0x24: CGameBitmap::ySizeOf @004243F1.
     */
    public int ySizeOf(int i) {
        return frames.get(i).ySize();
    }

    /**
     * vtbl +0x28: CGameBitmap::SetAsActiveRenderTarget @00424437.
     * Partial port. Native redirects the shared 16-bit render-surface descriptor to this bitmap's surface; the current
     * Java renderer stays bound to the BGRA screen surface, so there is no equivalent render-target switch yet.
     */
    public void setAsActiveRenderTarget() {
    }

    /**
     * vtbl +0x2c: CGameBitmap::MirrorY @00428450.
     * Native stub.
     * Fully ported.
     */
    public void mirrorY() {
    }

    /**
     * vtbl +0x30: CGameBitmap::GetBytesPerPixel @00428460.
     * Fully ported.
     */
    public int getBytesPerPixel() {
        return 2;
    }
}
