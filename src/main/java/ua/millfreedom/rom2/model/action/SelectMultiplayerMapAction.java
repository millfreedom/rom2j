package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `ChatTextAction` packet id `0xAE` used to broadcast the selected multiplayer map id/name text.
 */
public class SelectMultiplayerMapAction extends ChatTextAction {
    public static final int ACTION_ID = GameActionId.SELECT_MULTIPLAYER_MAP_ACTION_AE.id;
    public static final SelectMultiplayerMapAction global = new SelectMultiplayerMapAction();

    /**
     * Native support extracted from MapVisualObject::sendSelectedMultiplayerMapAction @0041C7E0,
     * MultiplayerMapSelectionDialogVisualObject::OnMessage @0044BBE8, and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public SelectMultiplayerMapAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::sendSelectedMultiplayerMapAction @0041C7E0,
     * MultiplayerMapSelectionDialogVisualObject::OnMessage @0044BBE8, and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public SelectMultiplayerMapAction(SelectMultiplayerMapAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2.
     * Fully ported. Packet id `0xAE` posts the selected-map text pointer as `wParam` and zero `lParam`.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        Globals.mainWindow.getInputController().onMessage(
                MessageCodes.MULTIPLAYER_MAP_SELECTION_SELECT_MAP_BY_NAME,
                text.get(),
                0
        );
    }

    /**
     * Native support extracted from MapVisualObject::sendSelectedMultiplayerMapAction @0041C7E0,
     * MultiplayerMapSelectionDialogVisualObject::OnMessage @0044BBE8, and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    @Override
    public SelectMultiplayerMapAction Clone() {
        return new SelectMultiplayerMapAction(this);
    }
}
