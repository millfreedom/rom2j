package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `CGameAction` packet id `0x4A` used to request a full resynchronization of the current player's state.
 */
public class RequestPlayerStateResyncAction extends CGameAction {
    public static final int ACTION_ID = GameActionId.REQUEST_PLAYER_STATE_RESYNC_ACTION_4A.id;
    public static final RequestPlayerStateResyncAction global = new RequestPlayerStateResyncAction();

    /**
     * Native support extracted from CMainWindow::loadSelectedCampaignSaveGame @0048DB37,
     * MapVisualObject::requestPlayerStateResync @0041AB50, and GameServer::handleServerGameAction @004F515D.
     */
    public RequestPlayerStateResyncAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CMainWindow::loadSelectedCampaignSaveGame @0048DB37,
     * MapVisualObject::requestPlayerStateResync @0041AB50, and GameServer::handleServerGameAction @004F515D.
     */
    public RequestPlayerStateResyncAction(RequestPlayerStateResyncAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CMainWindow::loadSelectedCampaignSaveGame @0048DB37,
     * MapVisualObject::requestPlayerStateResync @0041AB50, and GameServer::handleServerGameAction @004F515D.
     */
    @Override
    public RequestPlayerStateResyncAction Clone() {
        return new RequestPlayerStateResyncAction(this);
    }
}
