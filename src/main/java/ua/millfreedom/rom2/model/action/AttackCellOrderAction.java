package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `UnitTokenListAction` packet id `0x1A` used for attack-cell orders over a list of unit tokens.
 */
public class AttackCellOrderAction extends UnitTokenListAction {
    public static final int ACTION_ID = GameActionId.ATTACK_CELL_ORDER_ACTION_1A.id;
    public static final AttackCellOrderAction global = new AttackCellOrderAction();

    //0x0A
    public final Property<Integer> targetCellX = u16(BODY_OFFSET);
    //0x0C
    public final Property<Integer> targetCellY = u16(BODY_OFFSET + Short.BYTES);

    /**
     * Native support extracted from MapVisualObject::IssueMinimapAttackCellOrder @00418D78.
     */
    public AttackCellOrderAction() {
        super();
        ID.set(ACTION_ID);
        targetCellX.set(0);
        targetCellY.set(0);
    }

    /**
     * Native support extracted from MapVisualObject::IssueMinimapAttackCellOrder @00418D78.
     */
    public AttackCellOrderAction(AttackCellOrderAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from MapVisualObject::IssueMinimapAttackCellOrder @00418D78.
     */
    @Override
    public AttackCellOrderAction Clone() {
        return new AttackCellOrderAction(this);
    }

}
