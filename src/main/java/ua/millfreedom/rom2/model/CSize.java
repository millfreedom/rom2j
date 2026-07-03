package ua.millfreedom.rom2.model;

/**
 * Java support value object for native SIZE/CSize width-height pairs.
 * not ported.
 */
public class CSize {
    //0x0
    public int width;
    //0x4
    public int height;

    /**
     * Java support constructor for zero-initialized native SIZE/CSize values.
     * not ported.
     */
    public CSize() {
        this(0, 0);
    }

    /**
     * Java support constructor for native SIZE/CSize width-height values.
     * not ported.
     */
    public CSize(int width, int height) {
        setSize(width, height);
    }

    /**
     * Java support setter for native SIZE/CSize width-height values.
     * not ported.
     */
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
