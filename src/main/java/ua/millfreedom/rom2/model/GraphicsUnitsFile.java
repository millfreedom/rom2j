package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.res.Resources;

import static ua.millfreedom.rom2.res.Constants.*;

/**
 * Native class: GraphicsUnitsFile.
 * Purpose: lazy holder for the paired unit sprite sheets loaded from `graphics/units/...`.
 */
public final class GraphicsUnitsFile implements MfcSerializable {
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
     * Native: GraphicsUnitsFile::GraphicsUnitsFile @0047B0D6.
     * Java port status: fully ported. Native constructs the empty `CString path` holder used by MFC list/archive
     * instantiation; Java field initialization provides the same default object state.
     */
    public GraphicsUnitsFile() {
    }

    /**
     * Native: GraphicsUnitsFile::GraphicsUnitsFile @0047B12D.
     * Fully ported. Native initializes the path string, clears `loaded`, and appends the unit registry file segment
     * after the `graphics/units/` prefix.
     */
    public GraphicsUnitsFile(String relativePath) {
        path = Resources.path(GRAPHICS, UNITS, normalizeRelativePath(relativePath));
        loaded = false;
    }

    /**
     * Native: GraphicsUnitsFile::Load @0047B213.
     * Fully ported. Native allocates the paired `.256` / `b.256` `CSprite256` sheets and marks the holder loaded.
     */
    public void load() {
        sprite = new CSprite256(path + ".256");
        b_sprite = new CSprite256(path + "b.256");
        loaded = true;
    }

    /**
     * Native: GraphicsUnitsFile::Unload @0047B374.
     * Fully ported. Java detaches the loaded sprite references in place of native deletion.
     */
    public void unload() {
        if (loaded) {
            sprite = null;
            b_sprite = null;
            loaded = false;
        }
    }

    /**
     * Native: GraphicsUnitsFile::getSprite @0046DFF0.
     * Fully ported. Native lazy-loads the sheet when `loaded == 0`, then returns `sprite`.
     */
    public CSprite256 getSprite() {
        if (!loaded) {
            load();
        }
        return sprite;
    }

    /**
     * Native: GraphicsUnitsFile::getBSprite @0046E020.
     * Fully ported. Native lazy-loads the sheet when `loaded == 0`, then returns `b_sprite`.
     */
    public CSprite256 getBSprite() {
        if (!loaded) {
            load();
        }
        return b_sprite;
    }

    /**
     * Native support for GraphicsUnitsFile::GraphicsUnitsFile @0047B12D.
     * Native appends the registry path segment directly after the `graphics/units/` prefix; Java normalizes separators
     * for resource lookup while preserving native fail-fast behavior for null input.
     */
    private static String normalizeRelativePath(String relativePath) {
        return relativePath.replace('\\', '/');
    }
}
