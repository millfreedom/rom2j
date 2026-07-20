package ua.millfreedom.rom2.model.palette;

import ua.millfreedom.rom2.model.color.RGB32;

import static ua.millfreedom.rom2.console.Utils.colorReset;

/**
 * Native-named palette page whose Java storage is expanded opaque ARGB.
 */
public record Palette16(int[] data) implements Palette {

    // not ported.
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int color : data) {
            sb.append(RGB32.toString(color));
        }
        sb.append(colorReset());
        sb.append(']');
        return "Palette16{" + sb + '}';
    }
}
