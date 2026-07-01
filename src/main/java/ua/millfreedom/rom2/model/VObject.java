package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;

import java.util.ArrayList;
import java.util.List;

/**
 * Native class: VObject.
 */
public final class VObject implements MfcSerializable {
    //0x04
    public int id;

    //0x08
    public int registryIndex;

    //0x0C
    public int fileId;

    //0x10
    public int spriteIndex;

    //0x14
    public int phases;

    //0x18
    public int width;

    //0x1C
    public int height;

    //0x20
    public int centerX;

    //0x24
    public int centerY;

    //0x28
    public final List<Integer> animationFrames = new ArrayList<>();

    //0x3C
    public int animationFrameCount;

    //0x40
    public int fireObjectId;

    //0x44
    public int deadObjectId;

    //0x48
    public int inMapEditor;

    //0x4C
    public String descriptionText = "";

    /**
     * vtbl +0x08: CObject::Serialize @00401970.
     */
    @Override
    public void serialize(CArchive ar) {
        // Native CObject::Serialize is a no-op.
    }

    /**
     * Native: VObject::VObject @004799F7.
     * Java port status: fully ported.
     */
    public VObject() {
    }
}
