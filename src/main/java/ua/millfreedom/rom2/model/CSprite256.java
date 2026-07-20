package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.color.RGB32;
import ua.millfreedom.rom2.model.palette.CGamePalette;

/**
 * CSprite256 : CGameBitmap (no native members).
 */
public class CSprite256 extends CGameBitmap {
    /**
     * Java support constructor for decoded descendants such as CA16.
     * not ported.
     */
    protected CSprite256() {
    }

    /**
     * Native: CSprite256::CSprite256 @00424559.
     */
    public CSprite256(String name) {
        IndexedSpriteResource.DecodedSprite decoded = IndexedSpriteResource.loadRle8(name);
        palette256 = decoded.embeddedPalette();
        dataSize = palette256 == null ? decoded.resourceSize() : decoded.resourceSize() - 0x400L;
        setFrames(decoded.frames());
        surface = null;
    }

    /**
     * Native: CSprite256::CSprite256 @0042479F.
     * Fully ported without restoring source RLE bytes.
     */
    public CSprite256(CSprite256 source) {
        super(source);
    }

    /**
     * vtbl +0x18: CSprite256::Draw @004247C4.
     */
    @Override
    public void draw(int x, int y, int nFrameIndex, Object nPaletteIndex, boolean bFlipX) {
        if (!(nPaletteIndex instanceof Integer paletteIndex)) {
            throw new IllegalArgumentException("nPaletteIndex expected to be an integer in this override");
        }
        Globals.renderer.drawIndexedSprite(
                x,
                y,
                frame(nFrameIndex),
                palette.paletteData[paletteIndex].data(),
                bFlipX
        );
    }

    /**
     * Native support extracted from CSprite256::Draw @004247C4 and DrawSprite_RLE8_to_16 @00454344 when a
     * CBmp64k destination has been selected by CGameBitmap::SetAsActiveRenderTarget @00424437.
     */
    public void drawInto(CBmp64k target, int x, int y, int nFrameIndex, int nPaletteIndex, boolean bFlipX) {
        GameBitmapFrame frame = frame(nFrameIndex);
        int[] sourcePixels = frame.pixels();
        int sourceWidth = frame.width();
        int sourceHeight = frame.height();
        int[] paletteColors = palette.paletteData[nPaletteIndex].data();
        int[] targetPixels = target.surface.pixels();
        int targetWidth = target.surface.width();
        int targetHeight = target.surface.height();

        for (int sourceY = 0; sourceY < sourceHeight; sourceY++) {
            int targetY = y + sourceY;
            if (targetY < 0 || targetY >= targetHeight) {
                continue;
            }
            for (int drawX = 0; drawX < sourceWidth; drawX++) {
                int targetX = x + drawX;
                if (targetX < 0 || targetX >= targetWidth) {
                    continue;
                }
                int sourceX = bFlipX ? sourceWidth - 1 - drawX : drawX;
                int paletteIndex = sourcePixels[sourceY * sourceWidth + sourceX];
                if (paletteIndex != GameBitmapFrame.TRANSPARENT_INDEX) {
                    targetPixels[targetY * targetWidth + targetX] = paletteColors[paletteIndex];
                }
            }
        }
    }

    /**
     * vtbl +0x34: CSprite256::DrawFrame_ClippedY @00424980.
     */
    public void drawFrameClippedY(int x, int y, int nFrameIndex, int nPaletteIndex,
                                  CGamePalette paletteOverride, boolean bFlipX) {
        Globals.renderer.drawIndexedSpriteBlend(
                x,
                y,
                frame(nFrameIndex),
                paletteOverride.paletteData[nPaletteIndex].data(),
                bFlipX
        );
    }

    /**
     * vtbl +0x38: CSprite256::DrawBlend @0042485A.
     */
    public void drawBlend(int x, int y, int nFrameIndex, int nPaletteIndex, boolean bFlipX) {
        Globals.renderer.drawIndexedSpriteBlend(
                x,
                y,
                frame(nFrameIndex),
                palette.paletteData[nPaletteIndex].data(),
                bFlipX
        );
    }

    /**
     * vtbl +0x14: CSprite256::DrawWithPalette @004248F0.
     * Full port. Native draws with an explicit CGamePalette override and the requested page index.
     */
    @Override
    public void drawWithPalette(int x, int y, int nFrameIndex, int nPaletteIndex,
                                CGamePalette paletteOverride, boolean bFlipX) {
        Globals.renderer.drawIndexedSprite(
                x,
                y,
                frame(nFrameIndex),
                paletteOverride.paletteData[nPaletteIndex].data(),
                bFlipX
        );
    }

    /**
     * vtbl +0x1C: CSprite256::DrawAlpha @00424A10.
     * Native low-memory tint fallback is not modeled because g_IsLowMemory_ALWAYS_ZERO is always zero.
     */
    @Override
    public void drawAlpha(int x, int y, int nFrameIndex, int brightness, boolean bFlipX) {
        Globals.renderer.drawIndexedSpriteShade(x, y, frame(nFrameIndex), brightness, bFlipX);
    }

    /**
     * vtbl +0x3C: CSprite256::DrawWithRenderEffect @00424AF4.
     * Full port for the normal-memory path.
     */
    public void drawWithRenderEffect(int x, int y, int nFrameIndex, int brightness, int slope, boolean bFlipX) {
        Globals.renderer.drawIndexedSpriteShearedShade(x, y, frame(nFrameIndex), brightness, slope, bFlipX);
    }

    /**
     * vtbl +0x40: CSprite256::DrawWithColor @00424BE8.
     */
    public void drawWithColor(int destX, int destY, int frameIndex, byte fillColor) {
        int fillIntensity = Byte.toUnsignedInt(fillColor);
        int argbFill = RGB32.from(fillIntensity, fillIntensity, fillIntensity);
        Globals.renderer.drawIndexedSpriteSolid(destX, destY, frame(frameIndex), argbFill, false);
    }
}
