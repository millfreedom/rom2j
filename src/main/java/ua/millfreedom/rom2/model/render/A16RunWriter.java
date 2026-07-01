package ua.millfreedom.rom2.model.render;

/**
 * Receives a visible horizontal run of decoded CA16 source words.
 */
@FunctionalInterface
public interface A16RunWriter {
    /**
     * Writes one visible run of CA16 encoded pixels at the given destination row.
     * not ported.
     */
    void writeRun(int x, int y, byte[] encodedPixels, int offset, int count, int stepX);
}
