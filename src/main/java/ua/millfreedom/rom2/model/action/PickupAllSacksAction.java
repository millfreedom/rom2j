package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `UnitTokenListAction` packet id `0x13` used for the `P` hotkey pickup-all-sacks action for heroes.
 */
public class PickupAllSacksAction extends UnitTokenListAction {
    public static final int ACTION_ID = GameActionId.PICKUP_ALL_SACKS_ACTION_13.id;
    public static final PickupAllSacksAction global = new PickupAllSacksAction();

    /**
     * Native support extracted from MapVisualObject::issuePickupAllSacksAction @00419F2A and GameServer::handleServerGameAction @004F515D.
     */
    public PickupAllSacksAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::issuePickupAllSacksAction @00419F2A and GameServer::handleServerGameAction @004F515D.
     */
    public PickupAllSacksAction(PickupAllSacksAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from MapVisualObject::issuePickupAllSacksAction @00419F2A and GameServer::handleServerGameAction @004F515D.
     */
    @Override
    public PickupAllSacksAction Clone() {
        return new PickupAllSacksAction(this);
    }
}
