package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.net.CLlDriver;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;

import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;

/**
 * Native `CGameAction` packet id `0xAF` sent when the hosted server closes and clients must tear down pending network state.
 */
public class ServerClosedAction extends CGameAction {
    public static final int ACTION_ID = GameActionId.SERVER_CLOSED_ACTION_AF.id;
    public static final ServerClosedAction global = new ServerClosedAction();

    /**
     * Native support extracted from GameServer::~GameServer @004EC3BE and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public ServerClosedAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from GameServer::~GameServer @004EC3BE and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public ServerClosedAction(ServerClosedAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040DC36.
     * Partial port. `ServerClosedAction` resets the pending network-driver state on the gameplay-dialog true-return
     * path; MapVisualObject::HandleGameAction owns the no-gameplay-dialog drain-and-close false-return tail.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_MULTIPLAYER_CLIENT
                && GAMEPLAY.isSetIn(Globals.mainWindow.dialogsMask)) {
            CLlDriver.handleNetworkErrorAndClose();
        }
    }

    /**
     * Native support extracted from GameServer::~GameServer @004EC3BE and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    @Override
    public ServerClosedAction Clone() {
        return new ServerClosedAction(this);
    }
}
