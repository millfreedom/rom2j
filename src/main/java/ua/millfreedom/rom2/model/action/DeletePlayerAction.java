package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.net.CLlDriver;
import ua.millfreedom.rom2.model.CPlayer;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `TwoDwordAction` packet id `0x97` used to remove a disconnected player from the current session.
 */
public class DeletePlayerAction extends TwoDwordAction {
    public static final int ACTION_ID = GameActionId.DELETE_PLAYER_ACTION_97.id;
    public static final DeletePlayerAction global = new DeletePlayerAction();

    /**
     * Native support extracted from FUN_00505D02 @00505D02,
     * FUN_005164E8 @005164E8, and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public DeletePlayerAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from FUN_00505D02 @00505D02,
     * FUN_005164E8 @005164E8, and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public DeletePlayerAction(DeletePlayerAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from FUN_00505D02 @00505D02,
     * FUN_005164E8 @005164E8, and
     * TwoDwordAction::Clone @005410D0.
     */
    @Override
    public DeletePlayerAction Clone() {
        return new DeletePlayerAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00415911.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        int playerId = firstPayloadDword.get();
        if (mapVisualObject.currentPlayer != null && mapVisualObject.currentPlayer.playerId == playerId) {
            Globals.mainWindow.postMessage(MessageCodes.RETURN_TO_MULTIPLAYER_SETUP, 0, 0);
            CLlDriver.handleNetworkErrorAndClose();
            return;
        }

        CPlayer removedPlayer = mapVisualObject.findClientPlayerById(playerId);
        mapVisualObject.removeScenarioObjectsOwnedBy(removedPlayer);
        mapVisualObject.removeClientPlayerById(playerId);
        mapVisualObject.refreshLayoutAfterAction();
        Globals.mainWindow.rebuildDiplomacy();
        Globals.mainWindow.postMessage(MessageCodes.MULTIPLAYER_LOBBY_REFRESH_PLAYER_LIST, 0, 0);
    }

}
