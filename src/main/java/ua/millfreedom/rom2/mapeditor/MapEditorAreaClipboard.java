package ua.millfreedom.rom2.mapeditor;

/**
 * Editor-owned clipboard payload for selected terrain, altitude, and object cell layers.
 * not ported.
 */
final class MapEditorAreaClipboard {
    private final int width;
    private final int height;
    private final int[] tiles;
    private final int[] heights;
    private final int[] objects;

    /**
     * Java support constructor for selected-area clipboard data.
     * not ported.
     */
    MapEditorAreaClipboard(int width, int height, int[] tiles, int[] heights, int[] objects) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Clipboard dimensions must be positive.");
        }
        int expectedCellCount = width * height;
        if (tiles.length != expectedCellCount || heights.length != expectedCellCount || objects.length != expectedCellCount) {
            throw new IllegalArgumentException("Clipboard cell-layer arrays do not match clipboard dimensions.");
        }
        this.width = width;
        this.height = height;
        this.tiles = tiles.clone();
        this.heights = heights.clone();
        this.objects = objects.clone();
    }

    /**
     * Java support clipboard width accessor.
     * not ported.
     */
    int width() {
        return width;
    }

    /**
     * Java support clipboard height accessor.
     * not ported.
     */
    int height() {
        return height;
    }

    /**
     * Java support clipboard cell-count accessor.
     * not ported.
     */
    int cellCount() {
        return width * height;
    }

    /**
     * Java support tile-layer value accessor.
     * not ported.
     */
    int tileAt(int tileX, int tileY) {
        return tiles[cellIndex(tileX, tileY)];
    }

    /**
     * Java support altitude-layer value accessor.
     * not ported.
     */
    int heightAt(int tileX, int tileY) {
        return heights[cellIndex(tileX, tileY)];
    }

    /**
     * Java support object-layer value accessor.
     * not ported.
     */
    int objectAt(int tileX, int tileY) {
        return objects[cellIndex(tileX, tileY)];
    }

    /**
     * Java support local clipboard coordinate conversion.
     * not ported.
     */
    private int cellIndex(int tileX, int tileY) {
        if (tileX < 0 || tileY < 0 || tileX >= width || tileY >= height) {
            throw new IllegalArgumentException("Clipboard cell is outside the selected area.");
        }
        return tileY * width + tileX;
    }
}
