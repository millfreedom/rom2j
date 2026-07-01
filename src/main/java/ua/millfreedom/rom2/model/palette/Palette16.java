package ua.millfreedom.rom2.model.palette;

import ua.millfreedom.rom2.model.color.RGB16;

import static ua.millfreedom.rom2.console.Utils.colorReset;

public record Palette16(RGB16[] data) implements Palette {
    // not ported.
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (RGB16 v : data) {
            sb.append(v);
        }
        sb.append(colorReset());
        sb.append(']');
        return "Palette16{" + sb + '}';
    }
}
