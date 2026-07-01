package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `ChatTextAction` packet id `0x49` used by GameServer::handleServerGameAction @004F515D
 * to request spawning a named humanoid template from the command text payload.
 */
public class NamedCharacterSpawnRequestAction extends ChatTextAction {
    public static final int ACTION_ID = GameActionId.NAMED_CHARACTER_SPAWN_REQUEST_ACTION_49.id;
    public static final NamedCharacterSpawnRequestAction global = new NamedCharacterSpawnRequestAction();

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D and
     * GameServer::createUnitFromCommandText @004F89D1.
     */
    public NamedCharacterSpawnRequestAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from ChatTextAction::ChatTextAction @0050BF16,
     * GameServer::handleServerGameAction @004F515D, and GameServer::createUnitFromCommandText @004F89D1.
     */
    public NamedCharacterSpawnRequestAction(NamedCharacterSpawnRequestAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from ChatTextAction::Clone @00541340.
     */
    @Override
    public NamedCharacterSpawnRequestAction Clone() {
        return new NamedCharacterSpawnRequestAction(this);
    }
}
