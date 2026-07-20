package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.palette.CGamePalette;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palette256;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * CGameBitmap
 */
public abstract class CGameBitmap implements MfcSerializable {

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
    // Java-only effective palette recipe; not a native field.
    public PaletteInitialization paletteInitialization;

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
        frameCount = source.frameCount;
        dataSize = source.dataSize;
        frames = copyFrames(source.frames);
        if (source.surface != null) {
            int[] copiedSurfacePixels = source.frames != null
                    && !source.frames.isEmpty()
                    && source.surface.pixels() == source.frames.getFirst().pixels()
                    ? frames.getFirst().pixels()
                    : Arrays.copyOf(source.surface.pixels(), source.surface.pixels().length);
            surface = new GameBitmapSurface(
                    source.surface.width(),
                    source.surface.height(),
                    copiedSurfacePixels
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
        paletteInitialization = source.paletteInitialization;
    }

    /**
     * vtbl +0x08: CObject::Serialize @00401970.
     */
    @Override
    public void serialize(CArchive ar) {
        // Native CObject::Serialize is a no-op.
    }

    /**
     * Native: CGameBitmap::GetDataSize @00480BB0.
     * Java port status: fully ported.
     */
    public int getDataSize() {
        return (int) dataSize;
    }

    /**
     * Native: CGameBitmap::InitPalette @00424390.
     * Java preserves the native output while retaining the existing palette generation when every effective input
     * remains identical.
     */
    public void initPalette(int nPages, int nMode, int bGamma) {
        int applyTint = bGamma == 0 ? 0 : 1;
        int tintRed = Globals.lighting.tintR & 0xFF;
        int tintGreen = Globals.lighting.tintG & 0xFF;
        int tintBlue = Globals.lighting.tintB & 0xFF;
        if (applyTint == 0) {
            tintRed = tintGreen = tintBlue = 0;
        }
        if (paletteInitialization != null
                && paletteInitialization.matches(palette256, nPages, nMode, applyTint, tintRed, tintGreen, tintBlue)) {
            return;
        }

        palette.free();
        palette.init(palette256, nPages, nMode, bGamma);
        paletteInitialization = new PaletteInitialization(
                palette256,
                nPages,
                nMode,
                applyTint,
                tintRed,
                tintGreen,
                tintBlue
        );
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
        return frames.get(i).width();
    }

    /**
     * vtbl +0x24: CGameBitmap::ySizeOf @004243F1.
     */
    public int ySizeOf(int i) {
        return frames.get(i).height();
    }

    /**
     * Java support for installing the one canonical post-load frame set.
     * not ported.
     */
    protected final void setFrames(List<GameBitmapFrame> decodedFrames) {
        frames = decodedFrames;
        frameCount = decodedFrames.size();
    }

    /**
     * Java support for consumers that operate on the canonical post-load frame.
     * not ported.
     */
    public final GameBitmapFrame frame(int frameIndex) {
        return frames.get(frameIndex);
    }

    /**
     * Java support for deep-copying canonical frames without restoring compressed resource data.
     * not ported.
     */
    protected static List<GameBitmapFrame> copyFrames(List<GameBitmapFrame> sourceFrames) {
        List<GameBitmapFrame> copies = new ArrayList<>(sourceFrames.size());
        for (GameBitmapFrame frame : sourceFrames) {
            copies.add(frame.copy());
        }
        return copies;
    }

    /**
     * Java support for keeping CGameBitmap::InitPalette @00424390 static while its effective inputs are unchanged.
     * not ported.
     */
    public record PaletteInitialization(
            Palette256 sourcePalette,
            int pageCount,
            int mode,
            int applyTint,
            int tintRed,
            int tintGreen,
            int tintBlue
    ) {
        /**
         * not ported.
         */
        private boolean matches(
                Palette256 source,
                int candidatePageCount,
                int candidateMode,
                int candidateApplyTint,
                int candidateTintRed,
                int candidateTintGreen,
                int candidateTintBlue
        ) {
            return pageCount == candidatePageCount
                    && mode == candidateMode
                    && applyTint == candidateApplyTint
                    && tintRed == candidateTintRed
                    && tintGreen == candidateTintGreen
                    && tintBlue == candidateTintBlue
                    && sourcePalette == source;
        }
    }

    /**
     * vtbl +0x28: CGameBitmap::SetAsActiveRenderTarget @00424437.
     * Partial port. Native redirects the shared render-surface descriptor to this bitmap's surface; the current Java
     * renderer stays bound to the straight-ARGB int[] screen surface, so there is no equivalent render-target switch yet.
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
