package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `UnitTokenListAction` packet id `0x16` used for move orders over a list of unit tokens.
 */
public class MoveOrderAction extends UnitTokenListAction {
    public static final int ACTION_ID = GameActionId.MOVE_ORDER_ACTION_16.id;
    public static final MoveOrderAction global = new MoveOrderAction();

    //0x0A
    public final Property<Integer> destinationCellX = u16(BODY_OFFSET);
    //0x0C
    public final Property<Integer> destinationCellY = u16(BODY_OFFSET + Short.BYTES);

    /**
     * Native support extracted from MapVisualObject::IssueMinimapMoveOrder @00418BC3.
     */
    public MoveOrderAction() {
        super();
        ID.set(ACTION_ID);
        destinationCellX.set(0);
        destinationCellY.set(0);
    }

    /**
     * Native support extracted from MapVisualObject::IssueMinimapMoveOrder @00418BC3.
     */
    public MoveOrderAction(MoveOrderAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from MapVisualObject::IssueMinimapMoveOrder @00418BC3.
     */
    @Override
    public MoveOrderAction Clone() {
        return new MoveOrderAction(this);
    }

}
