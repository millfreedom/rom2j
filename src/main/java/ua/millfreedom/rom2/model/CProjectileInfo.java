package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.res.Resources;

import static ua.millfreedom.rom2.res.Constants.GRAPHICS;
import static ua.millfreedom.rom2.res.Constants.PROJECTILES;

/**
 * Inferred layout from FUN_0047cabc + LoadProjectiles stores + FUN_0047cb3b.
 * Size in C++ was 0x3c.
 */
public final class CProjectileInfo implements MfcSerializable {
    //0x04
    public CSprite256 spriteA;
    //0x08
    public CSprite256 spriteB;

    //0x0c
    public final String file;

    //0x10
    public int phases;
    //0x14
    public int id;
    //0x18
    public int rotationPhases;
    //0x1c
    public int width;
    //0x20
    public int height;
    //0x24
    public int sfx;
    //0x28
    public int palette;
    //0x2c
    public int homing;
    //0x30
    public int flip;

    //0x34
    public final boolean a16;

    //0x38
    public boolean loaded;

    /**
     * vtbl +0x08: CObject::Serialize @00401970.
     */
    @Override
    public void serialize(CArchive ar) {
        // Native CObject::Serialize is a no-op.
    }

    /**
     * Native: CProjectileInfo::CProjectileInfo @0047CABC.
     * Fully ported.
     */
    public CProjectileInfo(String file, boolean a16) {
        this.file = file;
        this.a16 = a16;
        this.loaded = false;
    }

    /**
     * Native: CProjectileInfo::GetSpriteA @0041F300.
     * Fully ported.
     */
    public CSprite256 getSpriteA() {
        if (!loaded) {
            ensureLoaded();
        }
        return spriteA;
    }

    /**
     * Native: CProjectileInfo::GetSpriteB @0046E050.
     * Fully ported.
     */
    public CSprite256 getSpriteB() {
        if (!loaded) {
            ensureLoaded();
        }
        return spriteB;
    }

    /**
     * Native: CProjectileInfo::ensureLoaded @0047CB3B.
     * Fully ported.
     */
    public void ensureLoaded() {
        if (loaded) return;

        String base = Resources.path(GRAPHICS, PROJECTILES, file);

        if (!a16) {
            // base + ".256"
            this.spriteA = new CSprite256(base + ".256");
            // base + "b.256" (matches asm)
            this.spriteB = new CSprite256(base + "b.256");
        } else {
            this.spriteA = new CA16(base + ".16a");
            this.spriteB = null;
        }

        // if (palette != 0) { InitPalette(...) }
        if (palette != 0) {
            if (a16) {
                // CGameBitmap::InitPalette(spriteA, 0x10, 4, 0)
                spriteA.initPalette(0x10, 4, 0);
            } else {
                // CGameBitmap::InitPalette(spriteA, 0x10, 2, 1)
                spriteA.initPalette(0x10, 2, 1);
                spriteB.initPalette(0x10, 2, 1);
            }
        }

        loaded = true;
    }
}
