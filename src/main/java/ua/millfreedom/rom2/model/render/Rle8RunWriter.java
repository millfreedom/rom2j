package ua.millfreedom.rom2.model.render;

/**
 * Receives a visible horizontal run of decoded RLE8 palette indices.
 */
@FunctionalInterface
public interface Rle8RunWriter {
    /**
     * Writes one visible run of palette indices at the given destination row.
     * not ported.
     */
    void writeRun(int x, int y, byte[] paletteIndices, int offset, int count, int stepX);
}
