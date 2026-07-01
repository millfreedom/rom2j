package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Specialized Java action for native `UnitChangeAction` packet id `0x6E`.
 */
public class UnitChangeAction_6E extends UnitChangeAction {
    public static final int ACTION_ID = GameActionId.UNIT_CHANGE_ACTION_6E.id;
    public static final UnitChangeAction_6E global = new UnitChangeAction_6E();

    /**
     * Native support extracted from UnitChangeAction::WritePayload @0050CDA6.
     */
    public UnitChangeAction_6E() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from UnitChangeAction::WritePayload @0050CDA6.
     */
    public UnitChangeAction_6E(UnitChangeAction from) {
        super(from, ACTION_ID);
    }

    /**
     * not ported.
     */
    @Override
    public UnitChangeAction_6E Clone() {
        return new UnitChangeAction_6E(this);
    }
}
