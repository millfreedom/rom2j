package ua.millfreedom.rom2.model;

import java.util.Arrays;

/**
 * One canonical post-load bitmap frame backed by Java unsigned pixel/index values.
 * The pixel array contains either straight ARGB values or decoded palette/index codes, according to its owning
 * CGameBitmap subtype. Compressed bytes and parallel derived frame arrays are never retained here.
 */
public final class GameBitmapFrame {
    public static final int TRANSPARENT_INDEX = -1;

    // Java-only canonical frame width.
    private final int width;
    // Java-only canonical frame height.
    private final int height;
    // Java-only canonical post-load pixels or unsigned palette/index codes.
    private final int[] pixels;
    // Java-only immutable covered-row metadata for decoded sprite frames; null for dense bitmap frames.
    private final RowCoverageSpans coverageSpans;

    /**
     * Java support for retaining one dense ARGB or indexed bitmap frame.
     * not ported.
     */
    public static GameBitmapFrame bitmap(int width, int height, int[] pixels) {
        return new GameBitmapFrame(width, height, pixels, null);
    }

    /**
     * Java support for retaining one decoded RLE sprite as unsigned indices with -1 transparent gaps.
     * not ported.
     */
    public static GameBitmapFrame indexedSprite(int width, int height, int[] pixels) {
        return new GameBitmapFrame(
                width,
                height,
                pixels,
                RowCoverageSpans.from(width, height, pixels, value -> value != TRANSPARENT_INDEX)
        );
    }

    /**
     * Java support for retaining one decoded A16 sprite as unsigned packed index/alpha codes.
     * not ported.
     */
    public static GameBitmapFrame a16Sprite(int width, int height, int[] pixels) {
        return new GameBitmapFrame(
                width,
                height,
                pixels,
                RowCoverageSpans.from(width, height, pixels, value -> ((value >>> 9) & 0x0F) != 0)
        );
    }

    /**
     * Java support for retaining privately owned canonical pixels and optional derived coverage metadata.
     * not ported.
     */
    private GameBitmapFrame(int width, int height, int[] pixels, RowCoverageSpans coverageSpans) {
        this.width = width;
        this.height = height;
        this.pixels = pixels;
        this.coverageSpans = coverageSpans;
    }

    /**
     * Java support for reading the canonical frame width.
     * not ported.
     */
    public int width() {
        return width;
    }

    /**
     * Java support for reading the canonical frame height.
     * not ported.
     */
    public int height() {
        return height;
    }

    /**
     * Java support for consuming the one canonical post-load pixel/index array.
     * not ported.
     */
    public int[] pixels() {
        return pixels;
    }

    /**
     * Java support for traversing immutable covered spans of a decoded sprite frame.
     * not ported.
     */
    public RowCoverageSpans coverageSpans() {
        return coverageSpans;
    }

    /**
     * Java support for deep-copying one canonical frame while sharing immutable derived coverage metadata.
     * not ported.
     */
    public GameBitmapFrame copy() {
        return new GameBitmapFrame(width, height, Arrays.copyOf(pixels, pixels.length), coverageSpans);
    }

    /**
     * not ported.
     */
    @Override
    public String toString() {
        return "GameBitmapFrame{" +
                "width=" + width +
                ", height=" + height +
                ", pixelCount=" + pixels.length +
                '}';
    }

    /**
     * Java-only primitive coverage predicate used only while constructing immutable row spans.
     */
    @FunctionalInterface
    private interface PixelCoverage {
        /**
         * not ported.
         */
        boolean isCovered(int pixel);
    }

    /**
     * Java-only immutable row coverage derived from the canonical decoded pixel-code array.
     */
    public static final class RowCoverageSpans {
        // Java-only first span index for each row plus one terminal index.
        private final int[] rowSpanOffsets;
        // Java-only packed start-inclusive/end-exclusive source-X bounds.
        private final int[] spanBounds;
        /**
         * Java support for retaining privately owned immutable span arrays.
         * not ported.
         */
        private RowCoverageSpans(int[] rowSpanOffsets, int[] spanBounds) {
            this.rowSpanOffsets = rowSpanOffsets;
            this.spanBounds = spanBounds;
        }

        /**
         * Java support for deriving row spans from one canonical decoded sprite array.
         * not ported.
         */
        private static RowCoverageSpans from(
                int width,
                int height,
                int[] pixels,
                PixelCoverage coverage
        ) {
            int spanCount = 0;
            for (int row = 0; row < height; row++) {
                int rowPixelIndex = row * width;
                boolean covered = false;
                for (int sourceX = 0; sourceX < width; sourceX++) {
                    boolean pixelCovered = coverage.isCovered(pixels[rowPixelIndex + sourceX]);
                    if (pixelCovered && !covered) {
                        spanCount++;
                    }
                    covered = pixelCovered;
                }
            }

            int[] rowSpanOffsets = new int[height + 1];
            int[] spanBounds = new int[spanCount * 2];
            int spanIndex = 0;
            for (int row = 0; row < height; row++) {
                rowSpanOffsets[row] = spanIndex;
                int rowPixelIndex = row * width;
                int sourceX = 0;
                while (sourceX < width) {
                    while (sourceX < width && !coverage.isCovered(pixels[rowPixelIndex + sourceX])) {
                        sourceX++;
                    }
                    if (sourceX == width) {
                        break;
                    }
                    int start = sourceX;
                    while (sourceX < width && coverage.isCovered(pixels[rowPixelIndex + sourceX])) {
                        sourceX++;
                    }
                    spanBounds[spanIndex * 2] = start;
                    spanBounds[spanIndex * 2 + 1] = sourceX;
                    spanIndex++;
                }
            }
            rowSpanOffsets[height] = spanIndex;
            return new RowCoverageSpans(rowSpanOffsets, spanBounds);
        }

        /**
         * Java support for locating the first coverage span on one source row.
         * not ported.
         */
        public int firstSpanIndex(int row) {
            return rowSpanOffsets[row];
        }

        /**
         * Java support for locating the exclusive terminal coverage span on one source row.
         * not ported.
         */
        public int endSpanIndex(int row) {
            return rowSpanOffsets[row + 1];
        }

        /**
         * Java support for reading one source span's inclusive X bound.
         * not ported.
         */
        public int start(int spanIndex) {
            return spanBounds[spanIndex * 2];
        }

        /**
         * Java support for reading one source span's exclusive X bound.
         * not ported.
         */
        public int end(int spanIndex) {
            return spanBounds[spanIndex * 2 + 1];
        }

    }
}
