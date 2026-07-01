package ua.millfreedom.rom2.model.spell;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;

import java.io.IOException;

/**
 * Native struct: VisualElem (size 0x08), stored in EffectVisualBuilder::visualElements.
 */
public class VisualElem implements MfcSerializable {
    //0x0
    public short x;

    //0x2
    public short y;

    //0x4
    public short z;

    //0x6
    public byte spriteId;

    //0x7
    public byte frame;

    /**
     * Native: VisualElem::VisualElem @004C9BA0.
     * Fully ported.
     */
    public VisualElem() {
    }

    /**
     * Native: VisualElem::VisualElem @004C9BA0.
     * Fully ported.
     */
    public VisualElem(int x, int y, int z, int spriteId, int frame) {
        this.x = (short) x;
        this.y = (short) y;
        this.z = (short) z;
        this.spriteId = (byte) spriteId;
        this.frame = (byte) frame;
    }

    /**
     * Native support extracted from CGameObject::CGameObject @0045FC42 VisualElem array copy.
     * Fully ported.
     */
    public VisualElem(VisualElem source) {
        x = source.x;
        y = source.y;
        z = source.z;
        spriteId = source.spriteId;
        frame = source.frame;
    }

    /**
     * Native support for VisualElem::SerializeElements @0046E170 element payload.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        if (ar.isStoring()) {
            ar.writeShort(x);
            ar.writeShort(y);
            ar.writeShort(z);
            ar.writeByte(spriteId);
            ar.writeByte(frame);
            return;
        }

        x = ar.readShort();
        y = ar.readShort();
        z = ar.readShort();
        spriteId = ar.readByte();
        frame = ar.readByte();
    }
}
