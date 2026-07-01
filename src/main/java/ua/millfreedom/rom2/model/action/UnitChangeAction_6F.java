package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Specialized Java action for native `UnitChangeAction` packet id `0x6F`.
 */
public class UnitChangeAction_6F extends UnitChangeAction {
    public static final int ACTION_ID = GameActionId.UNIT_CHANGE_ACTION_6F.id;
    public static final UnitChangeAction_6F global = new UnitChangeAction_6F();

    /**
     * Native support extracted from UnitChangeAction::WritePayload @0050CDA6.
     */
    public UnitChangeAction_6F() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from UnitChangeAction::WritePayload @0050CDA6.
     */
    public UnitChangeAction_6F(UnitChangeAction from) {
        super(from, ACTION_ID);
    }

    /**
     * not ported.
     */
    @Override
    public UnitChangeAction_6F Clone() {
        return new UnitChangeAction_6F(this);
    }
}
