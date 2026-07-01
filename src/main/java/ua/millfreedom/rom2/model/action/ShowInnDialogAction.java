package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.Inn;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `TwoDwordAction` packet id `0x84` used to open the inn interaction dialog on the client.
 */
public class ShowInnDialogAction extends TwoDwordAction {
    public static final int ACTION_ID = GameActionId.SHOW_INN_DIALOG_ACTION_84.id;
    public static final ShowInnDialogAction global = new ShowInnDialogAction();

    /**
     * Native support extracted from Unit::Update @0050F12C and
     * MapVisualObject::HandleGameAction @0040DCF5.
     */
    public ShowInnDialogAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from Unit::Update @0050F12C and
     * CServerApp::sendTwoDwordAction @00505347 packet field writes.
     */
    public static ShowInnDialogAction prepareForOpenInnDialog(Inn inn, Player player) {
        ShowInnDialogAction action = global;
        action.ID.set(ACTION_ID);
        action.playerID.set(player == null ? 0 : player.playerId);
        action.firstPayloadDword.set(inn.id);
        action.secondPayloadDword.set(inn.idFull);
        return action;
    }

    /**
     * Native support extracted from Unit::Update @0050F12C and
     * MapVisualObject::HandleGameAction @0040DCF5.
     */
    public ShowInnDialogAction(ShowInnDialogAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040DCF5.
     * Ported action-id case: `ShowInnDialogAction`.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        Globals.mainWindow.postMessage(
                MessageCodes.SHOW_INN_DIALOG,
                firstPayloadDword.get(),
                secondPayloadDword.get()
        );
    }

    /**
     * Native support extracted from Unit::Update @0050F12C and
     * TwoDwordAction::Clone @005410D0.
     */
    @Override
    public ShowInnDialogAction Clone() {
        return new ShowInnDialogAction(this);
    }

}
