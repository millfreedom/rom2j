package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `ChatTextAction` packet id `0x08` used to request a save-game load by filename/path text.
 */
public class LoadGameRequestAction extends ChatTextAction {
    public static final int ACTION_ID = GameActionId.LOAD_GAME_REQUEST_ACTION_08.id;
    public static final LoadGameRequestAction global = new LoadGameRequestAction();

    /**
     * Native support extracted from CServerApp::decodeIncomingGameAction @005056F1,
     * CMainWindow::loadSelectedCampaignSaveGame @0048DB37, and GameServer::handleServerGameAction @004F515D.
     */
    public LoadGameRequestAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CServerApp::decodeIncomingGameAction @005056F1,
     * CMainWindow::loadSelectedCampaignSaveGame @0048DB37, and GameServer::handleServerGameAction @004F515D.
     */
    public LoadGameRequestAction(LoadGameRequestAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CServerApp::decodeIncomingGameAction @005056F1,
     * CMainWindow::loadSelectedCampaignSaveGame @0048DB37, and GameServer::handleServerGameAction @004F515D.
     */
    @Override
    public LoadGameRequestAction Clone() {
        return new LoadGameRequestAction(this);
    }
}
