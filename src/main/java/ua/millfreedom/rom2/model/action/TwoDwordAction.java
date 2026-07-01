package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.net.CLlDriver;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;

/**
 * Raw packet model for native TwoDwordAction.
 */
public class TwoDwordAction extends CGameAction {
    public static final TwoDwordAction global = new TwoDwordAction();

    //0x0A
    public final Property<Integer> firstPayloadDword = i32(BODY_OFFSET);
    //0x0E
    public final Property<Integer> secondPayloadDword = i32(BODY_OFFSET + Integer.BYTES);

    /**
     * Native: TwoDwordAction::TwoDwordAction @0050BDA2.
     * Fully ported.
     */
    public TwoDwordAction() {
        super();
        firstPayloadDword.set(0);
        secondPayloadDword.set(0);
    }

    /**
     * Native: TwoDwordAction::TwoDwordAction @0050BDD5.
     * Fully ported.
     */
    public TwoDwordAction(TwoDwordAction from) {
        super();
        int wireSize = GetPayloadSize();
        PutSlice(ID_OFFSET, from.GetSlice(ID_OFFSET, wireSize), 0, wireSize);
    }

    /**
     * Native support extracted from CServerApp::sendTwoDwordAction @00505347 packet field writes.
     */
    public static TwoDwordAction prepareForTwoDwordAction(
            Player player,
            GameActionId actionId,
            int firstPayload,
            int secondPayload
    ) {
        TwoDwordAction action = global;
        action.ID.set(actionId.id);
        action.playerID.set(player == null ? 0 : player.playerId);
        action.firstPayloadDword.set(firstPayload);
        action.secondPayloadDword.set(secondPayload);
        return action;
    }

    /**
     * vtbl +0x04: TwoDwordAction::Clone @005410D0.
     * Fully ported.
     */
    @Override
    public TwoDwordAction Clone() {
        return new TwoDwordAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00415C67.
     * Partial port. Handles the recovered raw `TwoDwordAction` packet id `0x0B` status write and gameplay-dialog
     * network close; MapVisualObject::HandleGameAction owns the no-gameplay-dialog drain-and-close return tail.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        if (ID.get() == GameActionId.TWO_DWORD_ACTION_0B.id) {
            Globals.multiplayerBootstrapStatusWord = firstPayloadDword.get() | 0x1000;
            if (GAMEPLAY.isSetIn(Globals.mainWindow.dialogsMask)) {
                Globals.mainWindow.postMessage(MessageCodes.RETURN_TO_MULTIPLAYER_SETUP, 0, 0);
                CLlDriver.handleNetworkErrorAndClose();
            }
        } else if (ID.get() == GameActionId.TWO_DWORD_ACTION_AA.id) {
            MapLightOverrideAction.applyMapLightOverride(mapVisualObject, firstPayloadDword.get());
        }
    }

    /**
     * vtbl +0x10: TwoDwordAction::getWireSize @00541150.
     * Port name: GetPayloadSize.
     * Fully ported.
     */
    @Override
    public int GetPayloadSize() {
        return 0x09;
    }
}
