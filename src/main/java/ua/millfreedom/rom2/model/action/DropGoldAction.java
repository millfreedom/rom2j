package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `TwoDwordAction` packet id `0x23` used to drop a gold amount onto the map at a packed target cell.
 */
public class DropGoldAction extends TwoDwordAction {
    public static final int ACTION_ID = GameActionId.DROP_GOLD_ACTION_23.id;
    public static final DropGoldAction global = new DropGoldAction();

    /**
     * Native support extracted from MapVisualObject::sendDropGoldAction @0041A6CD,
     * DropGoldPromptVisualObject::HideDialog @004411A2, and GameServer::handleServerGameAction @004F515D.
     */
    public DropGoldAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::sendDropGoldAction @0041A6CD,
     * DropGoldPromptVisualObject::HideDialog @004411A2, and GameServer::handleServerGameAction @004F515D.
     */
    public DropGoldAction(DropGoldAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from MapVisualObject::sendDropGoldAction @0041A6CD,
     * DropGoldPromptVisualObject::HideDialog @004411A2, and GameServer::handleServerGameAction @004F515D.
     */
    @Override
    public DropGoldAction Clone() {
        return new DropGoldAction(this);
    }

}
