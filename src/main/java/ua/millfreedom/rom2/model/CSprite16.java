package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.palette.Palette16;


public class CSprite16 extends CGameBitmap {

    /**
     * not ported.
     */
    public CSprite16() {
        super();
    }

    /**
     * Native: CSprite16::CSprite16 @00424498.
     */
    public CSprite16(String name) {
        super(name);
    }

    /**
     * not ported.
     */
    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * Java helper for `CSprite16::DrawFrame_ClippedY` call sites.
     * not ported.
     */
    public void DrawFrameClippedY(int x, int y, int nFrameIndex, Palette16 nPalette) {
        DrawFrameClippedY(x, y, nFrameIndex, nPalette, false);
    }

    /**
     * vtbl +0x34: CSprite16::DrawFrame_ClippedY @004244BD.
     */
    public void DrawFrameClippedY(int x, int y, int nFrameIndex, Palette16 nPalette, boolean bFlipX) {
        GameBitmapFrame frame = frames.get(nFrameIndex);
        Globals.renderer.drawSpriteRLE4(x, y, frame.xSize(), frame.ySize(), frame.data(), nPalette.data());
    }

}
