package ua.millfreedom.rom2.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Native type: LlDriverSessionEntry.
 * Purpose: one enumerated DirectPlay/LlDriver session row used by the multiplayer session browser.
 */
public class LlDriverSessionEntry {
    public static final int NATIVE_SIZE = 0x110; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    //0x0
    public String sessionName = "";
    //0x100
    public UUID sessionGuid;
    // Java support, not a native field. Native obtains this through CLlDriver::GetSessionPlayerNames @0050814C.
    public final List<String> playerNames = new ArrayList<>();

    /**
     * Java utility constructor.
     * not ported.
     */
    public LlDriverSessionEntry() {
    }

    /**
     * Java utility constructor.
     * not ported.
     */
    public LlDriverSessionEntry(String sessionName) {
        this.sessionName = sessionName == null ? "" : sessionName;
    }
}
