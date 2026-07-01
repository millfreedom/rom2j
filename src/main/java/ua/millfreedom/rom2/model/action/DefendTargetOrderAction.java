package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `UnitTokenListAction` packet id `0x1B` used for defend-target orders over a list of unit tokens.
 */
public class DefendTargetOrderAction extends UnitTokenListAction {
    public static final int ACTION_ID = GameActionId.DEFEND_TARGET_ORDER_ACTION_1B.id;
    public static final DefendTargetOrderAction global = new DefendTargetOrderAction();

    //0x0E
    public final Property<Integer> targetTokenId = u16(BODY_OFFSET + 4);

    /**
     * Native support extracted from MapVisualObject::IssueMinimapDefendTargetOrder @00418FC1.
     */
    public DefendTargetOrderAction() {
        super();
        ID.set(ACTION_ID);
        targetTokenId.set(0);
    }

    /**
     * Native support extracted from MapVisualObject::IssueMinimapDefendTargetOrder @00418FC1.
     */
    public DefendTargetOrderAction(DefendTargetOrderAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from MapVisualObject::IssueMinimapDefendTargetOrder @00418FC1.
     */
    @Override
    public DefendTargetOrderAction Clone() {
        return new DefendTargetOrderAction(this);
    }

}
