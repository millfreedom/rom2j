package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.model.color.RGB32;

import java.util.Arrays;

// not ported.
public record Screen(int x, int y, int cx, int cy, int pitchPixels, int[] surface) {
    /**
     * Java renderer support for the native global render surface allocated by InitDirectDraw_Fullscreen @0045293C.
     * not ported.
     */
    public static Screen createArgbSurface(int width, int height) {
        int[] surface = new int[Math.multiplyExact(width, height)];
        Arrays.fill(surface, RGB32.BLACK);
        return new Screen(0, 0, width, height, width, surface);
    }

    // not ported.
    public int w() {
        return cx - x;
    }

    // not ported.
    public int h() {
        return cy - y;
    }


}
