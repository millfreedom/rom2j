package ua.millfreedom.rom2.gameserver;

/**
 * Native Cheats helper embedded in GameServer at +0x08.
 */
public final class Cheats {
    // Native command decoded by Cheats::initializeObfuscatedCheatCodes @005674DC. The encoded row terminates at index 7,
    // so the checked prefix stops before the final 'd'.
    private static final String ENABLE_PREFIX = "##Coward";

    /**
     * Native: Cheats::initializeObfuscatedCheatCodes @005674DC.
     * Fully ported. Java stores the decoded command prefix directly instead of the obfuscated row table.
     */
    public Cheats() {
    }

    /**
     * Native: Cheats::matchCheatCommandPrefix @00567618.
     * Fully ported.
     */
    public int matchCheatCommandPrefix(String text) {
        return text.startsWith(ENABLE_PREFIX) ? 1 : 0;
    }
}
