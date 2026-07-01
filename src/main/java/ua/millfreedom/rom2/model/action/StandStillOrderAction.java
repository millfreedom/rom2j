package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `UnitTokenListAction` packet id `0x12` used for the `L` hotkey stand-still order over a list of unit tokens.
 */
public class StandStillOrderAction extends UnitTokenListAction {
    public static final int ACTION_ID = GameActionId.STAND_STILL_ORDER_ACTION_12.id;
    public static final StandStillOrderAction global = new StandStillOrderAction();

    /**
     * Native support extracted from MapVisualObject::issueStandStillOrder @00419FFD and GameServer::handleServerGameAction @004F515D.
     */
    public StandStillOrderAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::issueStandStillOrder @00419FFD and GameServer::handleServerGameAction @004F515D.
     */
    public StandStillOrderAction(StandStillOrderAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from MapVisualObject::issueStandStillOrder @00419FFD and GameServer::handleServerGameAction @004F515D.
     */
    @Override
    public StandStillOrderAction Clone() {
        return new StandStillOrderAction(this);
    }
}
