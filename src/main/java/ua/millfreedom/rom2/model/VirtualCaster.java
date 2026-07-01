package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.model.spell.TransientSpellCastSpec;

import java.io.IOException;

public class VirtualCaster extends Token {
    //0x3c
    public int targetSearchRadius;
    //0x40
    public final TransientSpellCastSpec spellCastSpec = new TransientSpellCastSpec();

    /**
     * Native: VirtualCaster::VirtualCaster @0050DA58.
     * Fully ported.
     */
    public VirtualCaster() {
        targetSearchRadius = 0;
    }

    /**
     * Native: VirtualCaster::operator>>(CArchive*, VirtualCaster**) @0050DA3C.
     * Fully ported.
     */
    public static VirtualCaster readFromArchive(CArchive ar) throws IOException {
        return ar.readObject(VirtualCaster.class);
    }

    /**
     * Native: VirtualCaster::Serialize @0052DB3E.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        super.serialize(ar);
        if (!ar.isStoring()) {
            targetSearchRadius = ar.readByte() & 0xFF;
        } else {
            ar.writeByte(targetSearchRadius);
        }
        spellCastSpec.serialize(ar);
    }
}
