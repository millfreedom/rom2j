package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `CGameAction` packet id `0x4B` used to return to the game after death in multiplayer.
 */
public class ReturnAfterDeathAction extends CGameAction {
    public static final int ACTION_ID = GameActionId.RETURN_AFTER_DEATH_ACTION_4B.id;
    public static final ReturnAfterDeathAction global = new ReturnAfterDeathAction();

    /**
     * Native support extracted from MapVisualObject::OnKeyDown @0040C8A0,
     * MapVisualObject::sendReturnAfterDeathAction @0041AB96, and GameServer::handleServerGameAction @004F515D.
     */
    public ReturnAfterDeathAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::OnKeyDown @0040C8A0,
     * MapVisualObject::sendReturnAfterDeathAction @0041AB96, and GameServer::handleServerGameAction @004F515D.
     */
    public ReturnAfterDeathAction(ReturnAfterDeathAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::OnKeyDown @0040C8A0,
     * MapVisualObject::sendReturnAfterDeathAction @0041AB96, and GameServer::handleServerGameAction @004F515D.
     */
    @Override
    public ReturnAfterDeathAction Clone() {
        return new ReturnAfterDeathAction(this);
    }
}
