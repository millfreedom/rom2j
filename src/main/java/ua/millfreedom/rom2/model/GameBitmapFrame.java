package ua.millfreedom.rom2.model;

import java.util.Arrays;

public record GameBitmapFrame(
        int xSize,
        int ySize,
        int dataSize,
        byte[] data
) {

    @Override
    // not ported.
    public String toString() {
        return "GameBitmapFrame{" +
                "xSize=" + xSize +
                ", ySize=" + ySize +
                ", dataSize=" + dataSize +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
