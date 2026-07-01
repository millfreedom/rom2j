package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `UnitTokenListAction` packet id `0x18` used for stand-ground orders over a list of unit tokens.
 */
public class StandGroundOrderAction extends UnitTokenListAction {
    public static final int ACTION_ID = GameActionId.STAND_GROUND_ORDER_ACTION_18.id;
    public static final StandGroundOrderAction global = new StandGroundOrderAction();

    /**
     * Native support extracted from MapVisualObject::ExecuteOrderType @00418A02 and
     * MapVisualObject::issueStandGroundOrder @00418F09.
     */
    public StandGroundOrderAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::ExecuteOrderType @00418A02 and
     * MapVisualObject::issueStandGroundOrder @00418F09.
     */
    public StandGroundOrderAction(StandGroundOrderAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from MapVisualObject::ExecuteOrderType @00418A02 and
     * MapVisualObject::issueStandGroundOrder @00418F09.
     */
    @Override
    public StandGroundOrderAction Clone() {
        return new StandGroundOrderAction(this);
    }
}
