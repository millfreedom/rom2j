package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `UnitTokenListAction` packet id `0x14` used for retreat orders over a list of unit tokens.
 */
public class RetreatOrderAction extends UnitTokenListAction {
    public static final int ACTION_ID = GameActionId.RETREAT_ORDER_ACTION_14.id;
    public static final RetreatOrderAction global = new RetreatOrderAction();

    /**
     * Native support extracted from MapVisualObject::ExecuteOrderType @00418A02 and
     * MapVisualObject::issueRetreatOrder @00419E72.
     */
    public RetreatOrderAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::ExecuteOrderType @00418A02 and
     * MapVisualObject::issueRetreatOrder @00419E72.
     */
    public RetreatOrderAction(RetreatOrderAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from MapVisualObject::ExecuteOrderType @00418A02 and
     * MapVisualObject::issueRetreatOrder @00419E72.
     */
    @Override
    public RetreatOrderAction Clone() {
        return new RetreatOrderAction(this);
    }
}
