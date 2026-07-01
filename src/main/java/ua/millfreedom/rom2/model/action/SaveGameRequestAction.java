package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `ChatTextAction` packet id `0x07` used to request a save-game write by filename/path text.
 */
public class SaveGameRequestAction extends ChatTextAction {
    public static final int ACTION_ID = GameActionId.SAVE_GAME_REQUEST_ACTION_07.id;
    public static final SaveGameRequestAction global = new SaveGameRequestAction();

    /**
     * Native support extracted from CMainWindow::writeCurrentMissionResumeSave @0048DC9F,
     * MapVisualObject::sendSaveGameRequestAction @0041AA10, and GameServer::handleServerGameAction @004F515D.
     */
    public SaveGameRequestAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CMainWindow::writeCurrentMissionResumeSave @0048DC9F,
     * MapVisualObject::sendSaveGameRequestAction @0041AA10, and GameServer::handleServerGameAction @004F515D.
     */
    public SaveGameRequestAction(SaveGameRequestAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CMainWindow::writeCurrentMissionResumeSave @0048DC9F,
     * MapVisualObject::sendSaveGameRequestAction @0041AA10, and GameServer::handleServerGameAction @004F515D.
     */
    @Override
    public SaveGameRequestAction Clone() {
        return new SaveGameRequestAction(this);
    }
}
