package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.render.A16PaletteLookup;

/**
 * CA16 : CSprite256 (no native members).
 */
public class CA16 extends CSprite256 {
    // Java-only last palette-page generation resolved to a compact A16 lookup.
    private Palette16[] pageLookupGeneration;
    // Java-only compact lookup paired with pageLookupGeneration; never contains frame pixels.
    private A16PaletteLookup pageLookup;

    /**
     * Native: CA16::CA16 @00427ECB.
     * Fully ported without retaining A16 command words.
     */
    public CA16(String path) {
        super();
        IndexedSpriteResource.DecodedSprite decoded = IndexedSpriteResource.loadA16(path);
        palette256 = decoded.embeddedPalette();
        dataSize = palette256 == null ? decoded.resourceSize() : decoded.resourceSize() - 0x400L;
        setFrames(decoded.frames());
        surface = null;
    }

    /**
     * vtbl +0x18: CA16::Draw @00427EF0.
     * Fully ported through canonical packed A16 frames and compact palette resolution.
     */
    @Override
    public void draw(int x, int y, int nFrameIndex, Object paletteOverride, boolean bFlipX) {
        GameBitmapFrame selectedFrame = frame(nFrameIndex);
        A16PaletteLookup paletteLookup;
        if (paletteOverride instanceof A16PaletteLookup explicitLookup) {
            paletteLookup = explicitLookup;
        } else if (paletteOverride instanceof int[] basePalette) {
            paletteLookup = A16PaletteLookup.resolve(basePalette);
        } else if (paletteOverride instanceof Palette16[] palettePages) {
            paletteLookup = resolvePageLookup(palettePages);
        } else {
            paletteLookup = resolvePageLookup(palette.paletteData);
        }
        Globals.renderer.drawA16Sprite(x, y, selectedFrame, paletteLookup, bFlipX);
    }

    /**
     * Java support for resolving immutable palette pages only when the effective CGamePalette generation changes.
     * not ported.
     */
    private A16PaletteLookup resolvePageLookup(Palette16[] palettePages) {
        if (pageLookupGeneration != palettePages) {
            pageLookupGeneration = palettePages;
            pageLookup = A16PaletteLookup.resolve(palettePages);
        }
        return pageLookup;
    }
}
