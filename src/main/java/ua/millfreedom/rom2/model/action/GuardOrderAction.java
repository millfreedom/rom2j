package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `UnitTokenListAction` packet id `0x17` used for guard orders over a list of unit tokens.
 */
public class GuardOrderAction extends UnitTokenListAction {
    public static final int ACTION_ID = GameActionId.GUARD_ORDER_ACTION_17.id;
    public static final GuardOrderAction global = new GuardOrderAction();

    /**
     * Native support extracted from MapVisualObject::ExecuteOrderType @00418A02 and
     * MapVisualObject::issueGuardOrder @00418E51.
     */
    public GuardOrderAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::ExecuteOrderType @00418A02 and
     * MapVisualObject::issueGuardOrder @00418E51.
     */
    public GuardOrderAction(GuardOrderAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from MapVisualObject::ExecuteOrderType @00418A02 and
     * MapVisualObject::issueGuardOrder @00418E51.
     */
    @Override
    public GuardOrderAction Clone() {
        return new GuardOrderAction(this);
    }
}
