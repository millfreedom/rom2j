package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `UnitTokenListAction` packet id `0x24` used for enter-town or enter-building orders over a list of unit tokens.
 */
public class EnterTownOrderAction extends UnitTokenListAction {
    public static final int ACTION_ID = GameActionId.ENTER_TOWN_ORDER_ACTION_24.id;
    public static final EnterTownOrderAction global = new EnterTownOrderAction();

    //0x0E
    public final Property<Integer> targetTokenId = u16(BODY_OFFSET + 4);

    /**
     * Native support extracted from MapVisualObject::IssueEnterTownOrder @0041A0B5.
     */
    public EnterTownOrderAction() {
        super();
        ID.set(ACTION_ID);
        targetTokenId.set(0);
    }

    /**
     * Native support extracted from MapVisualObject::IssueEnterTownOrder @0041A0B5.
     */
    public EnterTownOrderAction(EnterTownOrderAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from MapVisualObject::IssueEnterTownOrder @0041A0B5.
     */
    @Override
    public EnterTownOrderAction Clone() {
        return new EnterTownOrderAction(this);
    }

}
