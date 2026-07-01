package ua.millfreedom.rom2.model;

// not ported.
public record Screen(int x, int y, int cx, int cy, int pitchBytes, byte[] surface) {
    /**
     * Java renderer support for the native global render surface allocated by InitDirectDraw_Fullscreen @0045293C.
     * not ported.
     */
    public static Screen createBgraSurface(int width, int height) {
        return new Screen(0, 0, width, height, width * 4, new byte[width * height * 4]);
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
