package ua.millfreedom.rom2.gameserver;

/**
 * Native RandomRelated state embedded at MissionRuntimeBase +0x0000.
 */
class RandomRelated {
    //0x00
    public int randomScaleLimit;
    //0x04
    public final byte[] reserved0x04 = new byte[0x04];

    /**
     * Native: RandomRelated::initializeLimits @0057AF80.
     * Fully ported. Java's process RNG is already timer-seeded; this preserves the native object-owned scale limit.
     */
    public void initializeLimits() {
        randomScaleLimit = 0x8000;
    }
}
