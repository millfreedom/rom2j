package ua.millfreedom.rom2.gameserver.missionruntime;

/**
 * Native 0x3F4-byte scratch byte/index state initialized by ScratchByteState::initialize @0055F2F0.
 */
public final class ScratchByteState {
    public static final int SCRATCH_BYTES_COUNT = 0x3E8;

    //0x000
    public int cursor0x000;
    //0x004
    public int limit0x004;
    //0x008
    public final byte[] scratchBytes0x008 = new byte[SCRATCH_BYTES_COUNT];
    //0x3F0
    public byte mode0x3F0;

    /**
     * Native: ScratchByteState::initialize @0055F2F0.
     * Fully ported.
     */
    public void initialize() {
        cursor0x000 = 0;
        limit0x004 = 0;
        mode0x3F0 = 2;
    }
}
