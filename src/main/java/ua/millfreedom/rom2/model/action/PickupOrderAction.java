package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `UnitTokenListAction` packet id `0x21` used for pickup interactions over a list of unit tokens.
 */
public class PickupOrderAction extends UnitTokenListAction {
    public static final int ACTION_ID = GameActionId.PICKUP_ORDER_ACTION_21.id;
    public static final PickupOrderAction global = new PickupOrderAction();

    //0x0A
    public final Property<Integer> targetCellX = u16(BODY_OFFSET);
    //0x0C
    public final Property<Integer> targetCellY = u16(BODY_OFFSET + Short.BYTES);
    //0x0E
    public final Property<Integer> inventoryInsertIndex = u16(BODY_OFFSET + Short.BYTES * 2);

    /**
     * Native support extracted from MapVisualObject::IssuePickupOrder @00419D8E.
     */
    public PickupOrderAction() {
        super();
        ID.set(ACTION_ID);
        targetCellX.set(0);
        targetCellY.set(0);
        inventoryInsertIndex.set(0);
    }

    /**
     * Native support extracted from MapVisualObject::IssuePickupOrder @00419D8E.
     */
    public PickupOrderAction(PickupOrderAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from MapVisualObject::IssuePickupOrder @00419D8E.
     */
    @Override
    public PickupOrderAction Clone() {
        return new PickupOrderAction(this);
    }

}
