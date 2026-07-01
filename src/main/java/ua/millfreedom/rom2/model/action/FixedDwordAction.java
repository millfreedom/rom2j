package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Raw packet model for native FixedDwordAction.
 */
public class FixedDwordAction extends CGameAction {
    public static final FixedDwordAction global = new FixedDwordAction();

    //0x0A
    public final Property<Integer> payloadDword = i32(BODY_OFFSET);

    /**
     * Native: FixedDwordAction::FixedDwordAction @0050BE42.
     * Fully ported.
     */
    public FixedDwordAction() {
        super();
        payloadDword.set(0);
    }

    /**
     * Native: FixedDwordAction::FixedDwordAction @0050BE6B.
     * Fully ported.
     */
    public FixedDwordAction(FixedDwordAction from) {
        super();
        int wireSize = GetPayloadSize();
        PutSlice(ID_OFFSET, from.GetSlice(ID_OFFSET, wireSize), 0, wireSize);
    }

    /**
     * vtbl +0x04: FixedDwordAction::Clone @00541190.
     * Fully ported.
     */
    @Override
    public FixedDwordAction Clone() {
        return new FixedDwordAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040DD4B.
     * Ported action-id case: `FixedDwordAction` packet id `0x0E`.
     * Java also records packet receipt for CMainWindow::connectToServerAddress @0048E90F because the audited native
     * login-accepted payload global @00627594 is zero.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        if (ID.get() == GameActionId.FIXED_DWORD_ACTION_0E.id) {
            Globals.mainWindow.connectionScratchState.acceptedCharacterFileOwnerId = payloadDword.get();
            Globals.mainWindow.connectionScratchState.directAddressLoginAccepted = true;
        }
    }

    /**
     * vtbl +0x10: FixedDwordAction::getWireSize @00541210.
     * Port name: GetPayloadSize.
     * Fully ported.
     */
    @Override
    public int GetPayloadSize() {
        return 0x05;
    }
}
