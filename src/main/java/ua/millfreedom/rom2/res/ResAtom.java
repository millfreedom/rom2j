package ua.millfreedom.rom2.res;

import java.util.Objects;

/**
 * Native: ResAtom layout used by ResNode.
 */
public record ResAtom(
        //0x00
        int magic,
        //0x04
        ResNodeData data,
        //0x0c
        int flags
) {
    public static final int SIZE = 0x10;

    /**
     * not ported.
     */
    public ResAtom {
        Objects.requireNonNull(data);
    }

    /**
     * Native support extracted from Res::FindChildByName @004E918E temporary lookup node construction.
     */
    public ResAtom() {
        this(0, new ResHeapNode(0, 0), 0);
    }
}
