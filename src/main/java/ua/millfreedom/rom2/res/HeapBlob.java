package ua.millfreedom.rom2.res;

import java.util.Objects;

public final class HeapBlob {
    //0x00
    public int used;

    //0x04
    public int capacity;

    //0x08
    public byte[] data;

    /**
     * Native: ResInHeap heap layout used by ResInHeap::New @004E4BB0 and ResInHeap::Read @004E488C.
     */
    public HeapBlob(int used, int capacity, byte[] data) {
        this.used = used;
        this.capacity = capacity;
        this.data = Objects.requireNonNull(data, "data");
    }

    /**
     * not ported.
     */
    public int used() {
        return used;
    }

    /**
     * not ported.
     */
    public int capacity() {
        return capacity;
    }

    /**
     * not ported.
     */
    public byte[] data() {
        return data;
    }
}
