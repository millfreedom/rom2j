package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `UnitTokenListAction` packet id `0x1D` used for patrol orders over a list of unit tokens.
 */
public class PatrolOrderAction extends UnitTokenListAction {
    public static final int ACTION_ID = GameActionId.PATROL_ORDER_ACTION_1D.id;
    public static final PatrolOrderAction global = new PatrolOrderAction();

    //0x0A
    public final Property<Integer> targetCellX = u16(BODY_OFFSET);
    //0x0C
    public final Property<Integer> targetCellY = u16(BODY_OFFSET + Short.BYTES);

    /**
     * Native support extracted from MapVisualObject::IssueMinimapPatrolOrder @004190BE.
     */
    public PatrolOrderAction() {
        super();
        ID.set(ACTION_ID);
        targetCellX.set(0);
        targetCellY.set(0);
    }

    /**
     * Native support extracted from MapVisualObject::IssueMinimapPatrolOrder @004190BE.
     */
    public PatrolOrderAction(PatrolOrderAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from MapVisualObject::IssueMinimapPatrolOrder @004190BE.
     */
    @Override
    public PatrolOrderAction Clone() {
        return new PatrolOrderAction(this);
    }

}
