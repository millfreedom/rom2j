package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.res.Resources;

import java.util.ArrayList;
import java.util.List;

import static ua.millfreedom.rom2.res.Constants.GRAPHICS;

/**
 * Native class: StructureDef.
 */
public final class StructureDef implements MfcSerializable {
    private static final String STRUCTURES = "structures";

    //0x04
    public CSprite256 spriteMain;

    //0x08
    public CSprite256 spriteSecondary;

    //0x0C
    public int id;

    //0x10
    public int tileWidth;

    //0x14
    public int tileHeight;

    //0x18
    public int fullHeight;

    //0x1C
    public int phases;

    //0x20
    public final CRect selection = new CRect();

    //0x30
    public int shadowY;

    //0x34
    public String animationMask;

    //0x38
    public int animationMaskSolidCount;

    //0x3C
    public int animationDataCount;

    //0x40
    public final List<Integer> animationData = new ArrayList<>();

    //0x54
    public String picture = "";

    //0x64
    public int indestructible;

    //0x68
    public String descriptionText = "";

    //0x88
    public int variableSize;

    //0x8C
    public int usable;

    //0x90
    public int flat;

    //0x94
    public int lightRadius;

    //0x98
    public int lightPulse;

    //0x9C
    public String filePath = "";

    //0xA0
    public boolean spritesLoaded;

    /**
     * vtbl +0x08: CObject::Serialize @00401970.
     */
    @Override
    public void serialize(CArchive ar) {
        // Native CObject::Serialize is a no-op.
    }

    /**
     * Native: StructureDef::StructureDef @0047D572.
     * Full port. Java normalizes native backslash separators for resource lookup.
     */
    public StructureDef(String filename) {
        spritesLoaded = false;
        filePath = Resources.path(GRAPHICS, STRUCTURES, normalizeRelativePath(filename));
    }

    /**
     * Native: StructureDef::getSpriteMain @0046E080.
     * Full port. Native lazily loads both structure sprite files before returning `spriteMain`.
     */
    public CSprite256 getSpriteMain() {
        if (!spritesLoaded) {
            loadSprites();
        }
        return spriteMain;
    }

    /**
     * Native: StructureDef::getSpriteSecondary @0046E0B0.
     * Full port. Native lazily loads both structure sprite files before returning `spriteSecondary`.
     */
    public CSprite256 getSpriteSecondary() {
        if (!spritesLoaded) {
            loadSprites();
        }
        return spriteSecondary;
    }

    /**
     * Native: StructureDef::LoadSprites @0047D6A8.
     * Full port.
     */
    private void loadSprites() {
        spriteMain = new CSprite256(filePath + ".256");
        spriteSecondary = new CSprite256(filePath + "b.256");
        spriteMain.initPalette(0x10, 2, 1);
        spritesLoaded = true;
    }

    /**
     * Native support extracted from cleanupStructures @0047DEB4.
     * Java detaches loaded sprite references in place of native object deletion.
     */
    public void unloadForStructureRegistryCleanup() {
        spriteMain = null;
        spriteSecondary = null;
        spritesLoaded = false;
    }

    /**
     * Native support extracted from StructureDef::StructureDef @0047D572.
     * Java resource paths use '/' separators.
     */
    private static String normalizeRelativePath(String relativePath) {
        return relativePath.replace('\\', '/');
    }
}
