package ua.millfreedom.rom2.mapeditor;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * Rectangular map-cell selection used by the standalone MapEditor command surface.
 * not ported.
 */
final class MapEditorAreaSelection {
    private final int left;
    private final int top;
    private final int right;
    private final int bottom;

    /**
     * Java support constructor for normalized inclusive map-cell bounds.
     * not ported.
     */
    private MapEditorAreaSelection(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    /**
     * Java support factory for a selection between two map cells.
     * not ported.
     */
    static MapEditorAreaSelection fromCells(int firstX, int firstY, int secondX, int secondY) {
        return new MapEditorAreaSelection(
                Math.min(firstX, secondX),
                Math.min(firstY, secondY),
                Math.max(firstX, secondX),
                Math.max(firstY, secondY)
        );
    }

    /**
     * Java support factory for a selection between two map cell points.
     * not ported.
     */
    static MapEditorAreaSelection fromCorners(Point first, Point second) {
        return fromCells(first.x, first.y, second.x, second.y);
    }

    /**
     * Java support accessor for the inclusive left cell coordinate.
     * not ported.
     */
    int left() {
        return left;
    }

    /**
     * Java support accessor for the inclusive top cell coordinate.
     * not ported.
     */
    int top() {
        return top;
    }

    /**
     * Java support accessor for the inclusive right cell coordinate.
     * not ported.
     */
    int right() {
        return right;
    }

    /**
     * Java support accessor for the inclusive bottom cell coordinate.
     * not ported.
     */
    int bottom() {
        return bottom;
    }

    /**
     * Java support selected width in map cells.
     * not ported.
     */
    int width() {
        return right - left + 1;
    }

    /**
     * Java support selected height in map cells.
     * not ported.
     */
    int height() {
        return bottom - top + 1;
    }

    /**
     * Java support selected cell count.
     * not ported.
     */
    int cellCount() {
        return width() * height();
    }

    /**
     * Java support conversion to viewport pixel bounds.
     * not ported.
     */
    Rectangle viewportBounds(int cellPixelSize) {
        return viewportBounds(cellPixelSize, 0);
    }

    /**
     * Java support conversion to viewport pixel bounds with a logical map top offset.
     * not ported.
     */
    Rectangle viewportBounds(int cellPixelSize, int viewportTopOffset) {
        return new Rectangle(
                left * cellPixelSize,
                viewportTopOffset + top * cellPixelSize,
                width() * cellPixelSize,
                height() * cellPixelSize
        );
    }
}
