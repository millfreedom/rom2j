package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `CGameAction` packet id `0xB7` sent when the hosted server enters a live mission.
 */
public class MissionStartedAction extends CGameAction {
    public static final int ACTION_ID = GameActionId.MISSION_STARTED_ACTION_B7.id;
    public static final MissionStartedAction global = new MissionStartedAction();

    /**
     * Native support extracted from GameServer::LoadMapByName @004EB715,
     * GameServer::handlePlayerJoinRequest @004F0CBE, and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public MissionStartedAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from GameServer::LoadMapByName @004EB715,
     * GameServer::handlePlayerJoinRequest @004F0CBE, and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public MissionStartedAction(MissionStartedAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040DB1C.
     * Ported action-id case: `MissionStartedAction` kicks the multiplayer session-validation refresh.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        if (Globals.mainWindow.sessionMode == 0) {
            Globals.mainWindow.multiplayerRefreshGamesPending = 1;
            Globals.mainWindow.postMessage(MessageCodes.VALIDATE_MULTIPLAYER_MAP_SELECTION, 0, 0);
        }
    }

    /**
     * Native support extracted from GameServer::LoadMapByName @004EB715,
     * GameServer::handlePlayerJoinRequest @004F0CBE, and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    @Override
    public MissionStartedAction Clone() {
        return new MissionStartedAction(this);
    }
}
