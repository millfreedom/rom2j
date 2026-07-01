package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Concrete no-payload packet id `0x03` handled by MapVisualObject::HandleGameAction @0040D9B2.
 */
public class NoPayloadAction03 extends CGameAction {
    public static final int ACTION_ID = GameActionId.NO_PAYLOAD_ACTION_03.id;
    public static final NoPayloadAction03 global = new NoPayloadAction03();

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2.
     */
    public NoPayloadAction03() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CGameAction::CGameAction @0050BAB4 and MapVisualObject::HandleGameAction @0040D9B2.
     */
    public NoPayloadAction03(NoPayloadAction03 from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CGameAction::Clone @00540490.
     */
    @Override
    public NoPayloadAction03 Clone() {
        return new NoPayloadAction03(this);
    }
}
