package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Specialized Java action for native `UnitChangeAction` packet id `0x6C`.
 */
public class UnitChangeAction_6C extends UnitChangeAction {
    public static final int ACTION_ID = GameActionId.UNIT_CHANGE_ACTION_6C.id;
    public static final UnitChangeAction_6C global = new UnitChangeAction_6C();

    /**
     * Native support extracted from UnitChangeAction::WritePayload @0050CDA6.
     */
    public UnitChangeAction_6C() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from UnitChangeAction::WritePayload @0050CDA6.
     */
    public UnitChangeAction_6C(UnitChangeAction from) {
        super(from, ACTION_ID);
    }

    /**
     * not ported.
     */
    @Override
    public UnitChangeAction_6C Clone() {
        return new UnitChangeAction_6C(this);
    }
}
