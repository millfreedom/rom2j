package ua.millfreedom.rom2.gameserver;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;

import java.io.IOException;
import java.util.Arrays;

/**
 * Native class: GameServerDebugState (size 0x190).
 */
public final class GameServerDebugState implements MfcSerializable {
    private static final int NATIVE_SIZE = 0x190;
    private static final int RESERVED_TAIL_SIZE = NATIVE_SIZE - 0x08;

    //0x0000
    public int turnTracingEnabled;
    //0x0004
    public int scriptTracingEnabled;
    //0x0008
    public final byte[] reserved0x0008 = new byte[RESERVED_TAIL_SIZE];

    /**
     * Native: GameServerDebugState::New @0057A925.
     */
    public void clear() {
        turnTracingEnabled = 0;
        scriptTracingEnabled = 0;
        Arrays.fill(reserved0x0008, (byte) 0);
    }

    /**
     * Native: GameServerDebugState::serializeTailObject170 @0057A951.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        if (ar.isStoring()) {
            ar.writeInt(turnTracingEnabled);
            ar.writeInt(scriptTracingEnabled);
            ar.writeBytes(reserved0x0008);
        } else {
            turnTracingEnabled = ar.readInt();
            scriptTracingEnabled = ar.readInt();
            byte[] rawReserved = ar.readBytes(RESERVED_TAIL_SIZE);
            System.arraycopy(rawReserved, 0, reserved0x0008, 0, reserved0x0008.length);
        }
    }
}
