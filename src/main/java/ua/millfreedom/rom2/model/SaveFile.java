package ua.millfreedom.rom2.model;

/**
 * Native class: SaveFile.
 * Purpose: load/save dialog payload with selected title and backing filename buffers.
 */
public class SaveFile {
    //0x0
    public String title;
    //0x100
    public String filename;

    /**
     * Native: SaveFile::New @0043EBD8.
     * Fully ported.
     */
    public SaveFile() {
        title = "";
        filename = "";
    }
}
