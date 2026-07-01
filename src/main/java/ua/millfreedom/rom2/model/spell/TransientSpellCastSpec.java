package ua.millfreedom.rom2.model.spell;

import ua.millfreedom.rom2.CArchive.CArchive;

import java.io.IOException;

/**
 * Native six-byte spell-cast payload used by WorldMapNode and VirtualCaster.
 */
public final class TransientSpellCastSpec {
    //0x00
    public int spellId;
    //0x01
    public int skillLevel;
    //0x02
    public int sourceX;
    //0x03
    public int sourceY;
    //0x04
    public int targetX;
    //0x05
    public int targetY;

    /**
     * Fully ported. Native: TransientSpellCastSpec::TransientSpellCastSpec @0055B95F.
     */
    public TransientSpellCastSpec() {
    }

    /**
     * Native support extracted from WorldMapNode::clear @0054F89A.
     */
    public void clear() {
        spellId = 0;
        skillLevel = 0;
        sourceX = 0;
        sourceY = 0;
        targetX = 0;
        targetY = 0;
    }

    /**
     * Native support extracted from the six-byte TransientSpellCastSpec copy helper @0055B978.
     */
    public void copyFrom(TransientSpellCastSpec from) {
        spellId = from.spellId;
        skillLevel = from.skillLevel;
        sourceX = from.sourceX;
        sourceY = from.sourceY;
        targetX = from.targetX;
        targetY = from.targetY;
    }

    /**
     * Native support extracted from VirtualCaster::Serialize @0052DB3E and SerializeElements<WorldMapNode> @0055FFF0.
     */
    public void serialize(CArchive ar) throws IOException {
        if (!ar.isStoring()) {
            spellId = ar.readByte() & 0xFF;
            skillLevel = ar.readByte() & 0xFF;
            sourceX = ar.readByte() & 0xFF;
            sourceY = ar.readByte() & 0xFF;
            targetX = ar.readByte() & 0xFF;
            targetY = ar.readByte() & 0xFF;
        } else {
            ar.writeByte(spellId);
            ar.writeByte(skillLevel);
            ar.writeByte(sourceX);
            ar.writeByte(sourceY);
            ar.writeByte(targetX);
            ar.writeByte(targetY);
        }
    }
}
