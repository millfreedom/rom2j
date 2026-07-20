package ua.millfreedom.rom2.model.palette;

import ua.millfreedom.rom2.model.color.RGB32;

import java.nio.ByteBuffer;

import static ua.millfreedom.rom2.console.Utils.colorReset;

public record Palette256(int[] data) implements Palette32 {

    public static final int DEFAULT_SIZE = 256;

    // not ported.
    public static Palette256 create() {
        return create(DEFAULT_SIZE);
    }

    // not ported.
    public static Palette256 create(int size) {
        return new Palette256(new int[size]);
    }

    // not ported.
    public static Palette256 read(ByteBuffer bb) {
        int[] palData = new int[DEFAULT_SIZE];
        for (int i = 0; i < palData.length; i++) {
            palData[i] = RGB32.opaque(bb.getInt());
        }
        return new Palette256(palData);
    }

    @Override
    // not ported.
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int color : data) {
            sb.append(RGB32.toString(color));
        }
        sb.append(colorReset());
        sb.append(']');
        return "Palette256{" + sb + '}';
    }
}
