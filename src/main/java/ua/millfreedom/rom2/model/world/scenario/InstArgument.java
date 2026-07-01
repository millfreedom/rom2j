package ua.millfreedom.rom2.model.world.scenario;

import ua.millfreedom.rom2.CString;

/**
 * Java grouping for Instant's split native argument arrays.
 */
public class InstArgument {
    // Instant +0x48 + index * 4
    public int value = 0;
    // Instant +0x70 + index * 4
    public int type = 0;
    // Instant +0x98 + index * 0x40
    public final CString name = new CString(0x40);
}
