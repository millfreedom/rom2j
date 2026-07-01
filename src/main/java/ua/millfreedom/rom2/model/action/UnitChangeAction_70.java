package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Specialized Java action for native `UnitChangeAction` packet id `0x70`.
 */
public class UnitChangeAction_70 extends UnitChangeAction {
    public static final int ACTION_ID = GameActionId.UNIT_CHANGE_ACTION_70.id;
    public static final UnitChangeAction_70 global = new UnitChangeAction_70();

    /**
     * Native support extracted from UnitChangeAction::WritePayload @0050CDA6.
     */
    public UnitChangeAction_70() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from UnitChangeAction::WritePayload @0050CDA6.
     */
    public UnitChangeAction_70(UnitChangeAction from) {
        super(from, ACTION_ID);
    }

    /**
     * not ported.
     */
    @Override
    public UnitChangeAction_70 Clone() {
        return new UnitChangeAction_70(this);
    }
}
