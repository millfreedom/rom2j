package ua.millfreedom.rom2.model.color;

public class Utils {
    // not ported.
    public static int clamp255(int v) {
        if (v < 0) return 0;
        if (v > 0xFF) return 0xFF;
        return v;
    }
}
