package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.color.RGB16;
import ua.millfreedom.rom2.model.palette.CGamePalette;
import ua.millfreedom.rom2.model.palette.Palette256;
import ua.millfreedom.rom2.model.render.Rle8RunWriter;
import ua.millfreedom.rom2.model.render.Rle8SpriteDecoder;

import static ua.millfreedom.rom2.Globals.gameFileManager;

/**
 * CSprite256 : CGameBitmap (no new members)
 */
public class CSprite256 extends CGameBitmap {

    /**
     * Native: CSprite256::CSprite256 @00424559.
     */
    public CSprite256(String name) {

        buf = gameFileManager.get(name);
        int fileLen = buf.limit();
        int rawFrameCount = buf.getInt(fileLen - 4);

        boolean hasPalette = (rawFrameCount & 0x8000_0000) != 0;
        this.frameCount = rawFrameCount & 0x7FFF_FFFF;
        this.dataSize = hasPalette ? (fileLen - 0x400L) : fileLen;

        if (hasPalette) {
            this.palette256 = Palette256.read(buf);
        } else {
            this.palette256 = null;
        }

        this.frames = getFrames(buf, frameCount);
        this.surface = null;

    }

    /**
     * Native: CSprite256::CSprite256 @0042479F.
     * Fully ported.
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
        GameBitmapFrame gbf = frames.get(nFrameIndex);
        // CGamePalette::GetAt(..., nPaletteIndex) -> returns Palette16 (256 entries)
        RGB16[] palData = palette.paletteData[paletteIndex].data();

        if (!bFlipX) {
            Globals.renderer.drawSpriteRLE8(x, y, gbf.xSize(), gbf.ySize(), gbf.data(), palData);
        } else {
            Globals.renderer.drawSpriteRLE8FlipX(x, y, gbf.xSize(), gbf.ySize(), gbf.data(), palData);
        }

    }

    /**
     * Native support extracted from CSprite256::Draw @004247C4 and DrawSprite_RLE8_to_16 @00454344 when a
     * CBmp64k destination has been selected by CGameBitmap::SetAsActiveRenderTarget @00424437.
     */
    public void drawInto(CBmp64k target, int x, int y, int nFrameIndex, int nPaletteIndex, boolean bFlipX) {
        GameBitmapFrame frame = frames.get(nFrameIndex);
        RGB16[] palData = palette.paletteData[nPaletteIndex].data();
        int targetWidth = target.surface.width();
        int targetHeight = target.surface.height();
        RGB16[] targetPixels = target.surface.pixels();

        Rle8RunWriter writer = (runX, runY, paletteIndices, offset, count, stepX) -> {
            int dest = runY * targetWidth + runX;
            for (int i = 0; i < count; i++) {
                targetPixels[dest] = palData[Byte.toUnsignedInt(paletteIndices[offset + i])];
                dest += stepX;
            }
        };

        if (!bFlipX) {
            Rle8SpriteDecoder.decodeClipped(
                    x,
                    y,
                    frame.xSize(),
                    frame.ySize(),
                    frame.data(),
                    0,
                    0,
                    targetWidth,
                    targetHeight,
                    writer
            );
        } else {
            Rle8SpriteDecoder.decodeClippedFlipX(
                    x,
                    y,
                    frame.xSize(),
                    frame.ySize(),
                    frame.data(),
                    0,
                    0,
                    targetWidth,
                    targetHeight,
                    writer
            );
        }
    }

    /**
     * vtbl +0x34: CSprite256::DrawFrame_ClippedY @00424980.
     */
    public void drawFrameClippedY(int x, int y, int nFrameIndex, int nPaletteIndex, CGamePalette paletteOverride, boolean bFlipX) {
        GameBitmapFrame gbf = this.frames.get(nFrameIndex);
        RGB16[] palData = paletteOverride.paletteData[nPaletteIndex].data();

        if (!bFlipX) {
            Globals.renderer.drawSpriteRLE8Blend(x, y, gbf.xSize(), gbf.ySize(), gbf.data(), palData);
        } else {
            Globals.renderer.drawSpriteRLE8BlendFlipX(x, y, gbf.xSize(), gbf.ySize(), gbf.data(), palData);
        }
    }

    /**
     * vtbl +0x38: CSprite256::DrawBlend @0042485A.
     */
    public void drawBlend(int x, int y, int nFrameIndex, int nPaletteIndex, boolean bFlipX) {
        GameBitmapFrame gbf = this.frames.get(nFrameIndex);
        // CGamePalette::GetAt(..., nPaletteIndex) -> returns Palette16 (256 entries)
        RGB16[] palData = this.palette.paletteData[nPaletteIndex].data();

        if (!bFlipX) {
            Globals.renderer.drawSpriteRLE8Blend(x, y, gbf.xSize(), gbf.ySize(), gbf.data(), palData);
        } else {
            Globals.renderer.drawSpriteRLE8BlendFlipX(x, y, gbf.xSize(), gbf.ySize(), gbf.data(), palData);
        }
    }

    /**
     * vtbl +0x14: CSprite256::DrawWithPalette @004248F0.
     * Full port. Native draws with an explicit `CGamePalette` override and the requested page index.
     */
    @Override
    public void drawWithPalette(int x, int y, int nFrameIndex, int nPaletteIndex, CGamePalette paletteOverride, boolean bFlipX) {
        GameBitmapFrame gbf = this.frames.get(nFrameIndex);
        RGB16[] palData = paletteOverride.paletteData[nPaletteIndex].data();

        if (!bFlipX) {
            Globals.renderer.drawSpriteRLE8(x, y, gbf.xSize(), gbf.ySize(), gbf.data(), palData);
        } else {
            Globals.renderer.drawSpriteRLE8FlipX(x, y, gbf.xSize(), gbf.ySize(), gbf.data(), palData);
        }
    }

    /**
     * vtbl +0x1C: CSprite256::DrawAlpha @00424A10.
     * Native low-memory tint fallback is not modeled because g_IsLowMemory_ALWAYS_ZERO is always zero.
     */
    @Override
    public void drawAlpha(int x, int y, int nFrameIndex, int brightness, boolean bFlipX) {
        GameBitmapFrame gbf = this.frames.get(nFrameIndex);

        if (!bFlipX) {
            Globals.renderer.drawSpriteRLE8AlphaBlend(x, y, gbf.xSize(), gbf.ySize(), gbf.data(), brightness);
        } else {
            Globals.renderer.drawSpriteRLE8AlphaBlendFlipX(x, y, gbf.xSize(), gbf.ySize(), gbf.data(), brightness);
        }
    }

    /**
     * vtbl +0x3C: CSprite256::DrawWithRenderEffect @00424AF4.
     * Full port for the normal-memory path. Native low-memory fallback is unused because g_IsLowMemory_ALWAYS_ZERO is always zero.
     */
    public void drawWithRenderEffect(int x, int y, int nFrameIndex, int brightness, int slope, boolean bFlipX) {
        GameBitmapFrame gbf = this.frames.get(nFrameIndex);

        if (!bFlipX) {
            Globals.renderer.drawSpriteRLE8To16Lut(x, y, gbf.xSize(), gbf.ySize(), gbf.data(), brightness, slope);
        } else {
            Globals.renderer.drawSpriteRLE8To16LutFlipX(x, y, gbf.xSize(), gbf.ySize(), gbf.data(), brightness, slope);
        }
    }

    /**
     * vtbl +0x40: CSprite256::DrawWithColor @00424BE8.
     * Native writes sprite coverage through DrawSprite_RLE8_SolidIndexed8 @004545C5 into the active 8bpp render
     * target; Java preserves the same solid coverage on the clipped 32bpp render target.
     */
    public void drawWithColor(int destX, int destY, int frameIndex, byte fillColor) {
        GameBitmapFrame frame = frames.get(frameIndex);
        int fillIntensity = Byte.toUnsignedInt(fillColor);
        Globals.renderer.drawSpriteRLE8Solid(
                destX,
                destY,
                frame.xSize(),
                frame.ySize(),
                frame.data(),
                RGB16.from(fillIntensity, fillIntensity, fillIntensity)
        );
    }


}
