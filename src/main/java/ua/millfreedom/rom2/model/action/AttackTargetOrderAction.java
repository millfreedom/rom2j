package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `UnitTokenListAction` packet id `0x19` used for attack-target orders over a list of unit tokens.
 */
public class AttackTargetOrderAction extends UnitTokenListAction {
    public static final int ACTION_ID = GameActionId.ATTACK_TARGET_ORDER_ACTION_19.id;
    public static final AttackTargetOrderAction global = new AttackTargetOrderAction();

    //0x0E
    public final Property<Integer> targetTokenId = u16(BODY_OFFSET + 4);

    /**
     * Native support extracted from MapVisualObject::IssueMinimapAttackTargetOrder @00418CB3.
     */
    public AttackTargetOrderAction() {
        super();
        ID.set(ACTION_ID);
        targetTokenId.set(0);
    }

    /**
     * Native support extracted from MapVisualObject::IssueMinimapAttackTargetOrder @00418CB3.
     */
    public AttackTargetOrderAction(AttackTargetOrderAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from MapVisualObject::IssueMinimapAttackTargetOrder @00418CB3.
     */
    @Override
    public AttackTargetOrderAction Clone() {
        return new AttackTargetOrderAction(this);
    }

}
