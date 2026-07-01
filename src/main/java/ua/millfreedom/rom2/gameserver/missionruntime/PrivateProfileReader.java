package ua.millfreedom.rom2.gameserver.missionruntime;

/**
 * Native private-profile reader state initialized by PrivateProfileReader::initialize @00567300.
 */
public final class PrivateProfileReader {
    //0x00
    public String profilePath;
    //0x04
    public String defaultSectionName;
    //0x08
    public String defaultStringValue;
    //0x0C
    public int defaultIntValue;

    /**
     * Native: PrivateProfileReader::initialize @00567300.
     * Fully ported.
     */
    public void initialize() {
        profilePath = "";
        defaultSectionName = "";
        defaultStringValue = "";
        defaultIntValue = 0;
    }
}
