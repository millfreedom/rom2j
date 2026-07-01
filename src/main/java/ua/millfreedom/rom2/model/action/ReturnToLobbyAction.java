package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.visobj.BasicInnDialogVisualObject;
import ua.millfreedom.rom2.model.visobj.CVisualObject;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;
import ua.millfreedom.rom2.model.visobj.ShopDialogVisualObject;

import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.INN_DIALOG;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.SHOP_DIALOG;

/**
 * Native `CGameAction` packet id `0xB8` sent when the hosted server leaves the mission and returns clients to the lobby.
 */
public class ReturnToLobbyAction extends CGameAction {
    public static final int ACTION_ID = GameActionId.RETURN_TO_LOBBY_ACTION_B8.id;
    public static final ReturnToLobbyAction global = new ReturnToLobbyAction();

    /**
     * Native support extracted from GameServer::returnToLobby @004EBD5F and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public ReturnToLobbyAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from GameServer::returnToLobby @004EBD5F and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public ReturnToLobbyAction(ReturnToLobbyAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040DB4B.
     * Ported action-id case: `ReturnToLobbyAction` closes live-map overlays and refreshes the multiplayer browser.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        if (Globals.mainWindow.sessionMode == 0) {
            CVisualObject inputController = Globals.mainWindow.getInputController();
            if (SHOP_DIALOG.isSetIn(Globals.mainWindow.dialogsMask)) {
                ShopDialogVisualObject shopDialog = (ShopDialogVisualObject) inputController.getChildById(1000);
                shopDialog.handleExitAction();
                inputController.removeChild(inputController.getChildById(1000));
            }
            if (INN_DIALOG.isSetIn(Globals.mainWindow.dialogsMask)) {
                BasicInnDialogVisualObject menuDialog = (BasicInnDialogVisualObject) inputController.getChildById(0x44C);
                menuDialog.handleExitAction();
                inputController.removeChild(inputController.getChildById(0x44C));
            }
            inputController.onMessage(MessageCodes.RETURN_TO_GAME, 0, 0);
            Globals.mainWindow.multiplayerRefreshGamesPending = 0;
            Globals.mainWindow.postMessage(MessageCodes.CLIENT_RETURN_TO_MAP_SELECTION, 0, 0);
        }
    }

    /**
     * Native support extracted from GameServer::returnToLobby @004EBD5F and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    @Override
    public ReturnToLobbyAction Clone() {
        return new ReturnToLobbyAction(this);
    }
}
