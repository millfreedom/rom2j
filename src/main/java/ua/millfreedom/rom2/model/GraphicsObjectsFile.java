package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.res.Resources;

import static ua.millfreedom.rom2.res.Constants.GRAPHICS;

/**
 * Native class: GraphicsObjectsFile.
 * Purpose: lazy holder for the paired object sprite sheets loaded from `graphics/objects/...`.
 */
public final class GraphicsObjectsFile implements MfcSerializable {
    private static final String OBJECTS = "objects";

    //0x04
    public CSprite256 sprite;

    //0x08
    public CSprite256 b_sprite;

    //0x0C
    public String path = "";

    //0x10
    public boolean loaded;

    /**
     * vtbl +0x08: CObject::Serialize @00401970.
     */
    @Override
    public void serialize(CArchive ar) {
        // Native CObject::Serialize is a no-op.
    }

    /**
     * Native: GraphicsObjectsFile::GraphicsObjectsFile @004796CC.
     * Java port status: fully ported. Native constructs the empty `CString path` holder used by MFC list/archive
     * instantiation; Java field initialization provides the same default object state.
     */
    public GraphicsObjectsFile() {
    }

    /**
     * Native: GraphicsObjectsFile::GraphicsObjectsFile @00479723.
     * Java port status: fully ported.
     */
    public GraphicsObjectsFile(String relativePath) {
        path = Resources.path(GRAPHICS, OBJECTS, normalizeRelativePath(relativePath));
        loaded = false;
    }

    /**
     * Native: GraphicsObjectsFile::GetSpriteA @0041F280.
     * Fully ported.
     */
    public CSprite256 getSpriteA() {
        if (!loaded) {
            load();
        }
        return sprite;
    }

    /**
     * Native: GraphicsObjectsFile::GetSpriteB @0041F2B0.
     * Fully ported.
     */
    public CSprite256 getSpriteB() {
        if (!loaded) {
            load();
        }
        return b_sprite;
    }

    /**
     * Native: GraphicsObjectsFile::Load @00479809.
     * Java port status: fully ported.
     */
    private void load() {
        sprite = new CSprite256(path + ".256");
        b_sprite = new CSprite256(path + "b.256");
        sprite.initPalette(0x10, 2, 1);
        loaded = true;
    }

    /**
     * Native: GraphicsObjectsFile::FreeResources @0047997B.
     * Java port status: fully ported. Java detaches loaded sprite references in place of native object deletion.
     */
    public void freeResources() {
        if (!loaded) {
            return;
        }
        sprite = null;
        b_sprite = null;
        loaded = false;
    }

    /**
     * Native support extracted from GraphicsObjectsFile::GraphicsObjectsFile @00479723.
     */
    private static String normalizeRelativePath(String relativePath) {
        return relativePath.replace('\\', '/');
    }
}
