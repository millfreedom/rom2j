package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.net.CBufferManager;

/**
 * Shared variable-size unit-token list suffix used by batch order packets.
 */
public class UnitTokenListAction extends CGameAction {
    public static final UnitTokenListAction global = new UnitTokenListAction();

    //0x12
    public final Property<Integer> entryCount = u8(BODY_OFFSET + 8);
    //0x13
    public final Property<byte[]> unitTokenIds = u16Array(BODY_OFFSET + 9, 0xFD);

    /**
     * Native: UnitTokenListAction::UnitTokenListAction @0050BB82.
     * Fully ported.
     */
    public UnitTokenListAction() {
        super();
        entryCount.set(0);
    }

    /**
     * Native support extracted from UnitTokenListAction::UnitTokenListAction @0050BB82.
     */
    public UnitTokenListAction(UnitTokenListAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native: UnitTokenListAction::Add @0050BBB8.
     * Fully ported.
     */
    public UnitTokenListAction addUnitToken(int unitTokenId) {
        int count = entryCount.get();
        if (count < 0xFD) {
            unitTokenIds.setUnsignedWordAtIndex(count, unitTokenId);
            entryCount.set(count + 1);
        }
        return this;
    }

    /**
     * vtbl +0x10: UnitTokenListAction::GetPayloadSize @00540FA0.
     * Fully ported.
     */
    @Override
    public int GetPayloadSize() {
        return entryCount.get() * 2 + 10;
    }

    /**
     * vtbl +0x08: UnitTokenListAction::WritePayload @0050BBF8.
     * Fully ported.
     */
    @Override
    public boolean WritePayload(CBufferManager target) {
        return target.Write(this, ID_OFFSET, entryCount.get() * Short.BYTES + 10);
    }

    /**
     * vtbl +0x0C: UnitTokenListAction::ReadPayload @0050BC21.
     * Fully ported.
     */
    @Override
    public boolean ReadPayload(CBufferManager source) {
        boolean result = source.Read(this, BODY_OFFSET, 9);
        if (entryCount.get() != 0) {
            int tokenBytes = entryCount.get() * Short.BYTES;
            result = source.Read(this, unitTokenIds.startPosition(), tokenBytes);
        }
        return result;
    }
}
