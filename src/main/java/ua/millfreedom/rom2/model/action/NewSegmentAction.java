package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `FixedDwordAction` packet id `0x64` used by
 * `CServerApp::broadcastServerLoopCounter @00504BB7` and
 * `CServerApp::sendCurrentServerLoopCounter @00504BFA`
 * to send `GameServer.field1_0x4`, the advancing server loop counter.
 */
public class NewSegmentAction extends FixedDwordAction {
    public static final int ACTION_ID = GameActionId.NEW_SEGMENT_ACTION_64.id;
    public static final NewSegmentAction global = new NewSegmentAction();

    /**
     * Native support extracted from CServerApp::broadcastServerLoopCounter @00504BB7,
     * CServerApp::sendCurrentServerLoopCounter @00504BFA,
     * GameServer::FUN_004F8521 @004F8521, and
     * GameServer::runServerLoopTick @004F08C0.
     */
    public NewSegmentAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from FixedDwordAction::FixedDwordAction @0050BE6B and
     * CServerApp::broadcastServerLoopCounter @00504BB7 and
     * CServerApp::sendCurrentServerLoopCounter @00504BFA.
     */
    public NewSegmentAction(NewSegmentAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CServerApp::broadcastServerLoopCounter @00504BB7 packet field writes.
     */
    public static NewSegmentAction prepareForBroadcastServerLoopCounter(int serverLoopCounter) {
        return prepareServerLoopCounterPayload(0, serverLoopCounter);
    }

    /**
     * Native support extracted from CServerApp::sendCurrentServerLoopCounter @00504BFA and
     * CServerApp::sendLoginAcceptedHandshake @00504CCC.
     */
    public static NewSegmentAction createForServerLoopCounter(int playerId, int serverLoopCounter) {
        return prepareServerLoopCounterPayload(playerId, serverLoopCounter);
    }

    /**
     * Native support extracted from CServerApp::broadcastServerLoopCounter @00504BB7 and
     * CServerApp::sendCurrentServerLoopCounter @00504BFA, and
     * CServerApp::sendLoginAcceptedHandshake @00504CCC packet field writes.
     */
    private static NewSegmentAction prepareServerLoopCounterPayload(int playerId, int serverLoopCounter) {
        NewSegmentAction action = global;
        action.playerID.set(playerId);
        action.ID.set(ACTION_ID);
        action.payloadDword.set(serverLoopCounter);
        return action;
    }

    /**
     * Native support extracted from FixedDwordAction::Clone @00541190.
     */
    @Override
    public NewSegmentAction Clone() {
        return new NewSegmentAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040DA4B, CServerApp::broadcastServerLoopCounter @00504BB7, and GameServer::FUN_004F8521 @004F8521.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        Globals.mainWindow.serverLoopCounter = payloadDword.get();
    }

}
