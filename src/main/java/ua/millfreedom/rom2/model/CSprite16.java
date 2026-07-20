package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.palette.Palette16;

import java.util.List;

/**
 * CSprite16 decoded RLE4 sprite.
 */
public class CSprite16 extends CGameBitmap {
    /**
     * not ported.
     */
    public CSprite16() {
        setFrames(List.of());
    }

    /**
     * Native: CSprite16::CSprite16 @00424498.
     */
    public CSprite16(String name) {
        IndexedSpriteResource.DecodedSprite decoded = IndexedSpriteResource.loadRle4(name);
        dataSize = decoded.resourceSize();
        palette256 = null;
        setFrames(decoded.frames());
    }

    /**
     * not ported.
     */
    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * Java helper for CSprite16::DrawFrame_ClippedY call sites.
     * not ported.
     */
    public void DrawFrameClippedY(int x, int y, int nFrameIndex, Palette16 nPalette) {
        DrawFrameClippedY(x, y, nFrameIndex, nPalette, false);
    }

    /**
     * vtbl +0x34: CSprite16::DrawFrame_ClippedY @004244BD.
     */
    public void DrawFrameClippedY(int x, int y, int nFrameIndex, Palette16 nPalette, boolean bFlipX) {
        Globals.renderer.drawIndexedSprite(x, y, frame(nFrameIndex), nPalette.data(), bFlipX);
    }
}
