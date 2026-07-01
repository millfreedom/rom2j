package ua.millfreedom.rom2.model;

import java.util.List;

public final class SequentialArrayWalker {
    //0x0
    public final List<Integer> array;
    //0x4
    public int cur;

    /**
     * Native: SequentialArrayWalker::SequentialArrayWalker @004FD85F.
     * Fully ported.
     */
    public SequentialArrayWalker(List<Integer> array) {
        this.array = array;
        cur = 0;
    }

    /**
     * Native: SequentialArrayWalker::NextShort @005425C0.
     * Fully ported.
     */
    public int nextShort(int currentValue) {
        int value = array.get(cur);
        cur++;
        return value == -1 ? currentValue : (short) value;
    }

    /**
     * Native: SequentialArrayWalker::NextByte @00542610.
     * Fully ported.
     */
    public int nextByte(int currentValue) {
        int value = array.get(cur);
        cur++;
        return value == -1 ? currentValue : value & 0xFF;
    }

    /**
     * Native: SequentialArrayWalker::NextInt @00542660.
     * Fully ported.
     */
    public int nextInt(int currentValue) {
        int value = array.get(cur);
        cur++;
        return value == -1 ? currentValue : value;
    }

    /**
     * Native: SequentialArrayWalker::NextUShort @005426B0.
     * Fully ported.
     */
    public int nextUnsignedShort(int currentValue) {
        int value = array.get(cur);
        cur++;
        return value == -1 ? currentValue : value & 0xFFFF;
    }
}
