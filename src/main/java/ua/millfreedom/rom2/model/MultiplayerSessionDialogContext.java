package ua.millfreedom.rom2.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Native type: MultiplayerSessionDialogContext.
 * Purpose: main-window-owned runtime context for the multiplayer session browser dialog.
 */
public class MultiplayerSessionDialogContext {
    public static final int NATIVE_SIZE = 0x14; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    /**
     * Native: MultiplayerSessionDialogContext::MultiplayerSessionDialogContext @00492C90.
     * Java port status: fully ported.
     */
    public MultiplayerSessionDialogContext() {
    }

    //0x0
    public String playerName = "";
    //0x4
    public int selectedSessionIndex;
    //0x8
    public final List<LlDriverSessionEntry> sessionEntries = new ArrayList<>();
    //0xc
    public int sessionEntryCount;
    //0x10
    public int committedSessionIndex;
}
