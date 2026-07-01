package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.color.RGB16;
import ua.millfreedom.rom2.model.palette.Palette16;

/**
 * CA16 : CSprite256 (no new members)
 */
public class CA16 extends CSprite256 {
    /**
     * Native: CA16::CA16 @00427ECB.
     * Fully ported.
     */
    public CA16(String path) {
        super(path);
    }

    /**
     * vtbl +0x18: CA16::Draw @00427EF0.
     * Fully ported.
     */
    @Override
    public void draw(int x, int y, int nFrameIndex, Object paletteOverride, boolean bFlipX) {
        GameBitmapFrame gbf = this.frames.get(nFrameIndex);
        if (paletteOverride instanceof RGB16[] pal) {
            Globals.renderer.drawSpriteA16WithBasePalette(x, y, gbf.xSize(), gbf.ySize(), gbf.data(), pal, bFlipX);
            return;
        }

        Palette16[] palettePages;
        if (paletteOverride instanceof Palette16[] pages) {
            palettePages = pages;
        } else {
            palettePages = palette.paletteData;
        }
        if (!bFlipX) {
            Globals.renderer.drawSpriteA16(x, y, gbf.xSize(), gbf.ySize(), gbf.data(), palettePages);
        } else {
            Globals.renderer.drawSpriteA16FlipX(x, y, gbf.xSize(), gbf.ySize(), gbf.data(), palettePages);
        }
    }
}
