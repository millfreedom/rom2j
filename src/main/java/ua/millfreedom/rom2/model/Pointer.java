package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;

import java.io.IOException;

public class Pointer extends Building {
    //0x6C
    public int scriptInstantIndex;

    /**
     * Native: Pointer::Pointer @00543360.
     * Fully ported.
     */
    public Pointer() {
        scriptInstantIndex = -1;
    }

    /**
     * Native: Pointer::Pointer @005672C0.
     * Fully ported.
     */
    public Pointer(int buildingId, TargetHandle targetHandle, int widthTiles, int heightTiles) {
        super(buildingId, targetHandle, widthTiles, heightTiles);
        scriptInstantIndex = -1;
    }

    /**
     * vtbl +0x08: Pointer::Serialize @0052041B.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        // Pointer::Serialize (0052041b) = Building::serialize + t4 at +0x6C.
        super.serialize(ar);
        if (!ar.isStoring()) {
            scriptInstantIndex = ar.readInt();
        } else {
            ar.writeInt(scriptInstantIndex);
        }
    }
}
