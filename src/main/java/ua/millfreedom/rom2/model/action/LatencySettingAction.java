package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.CServerApp;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `FixedDwordAction` packet id `0xC1` used by
 * `CServerApp::sendLatencySetting @00504C76`
 * to send the configured network latency in milliseconds.
 */
public class LatencySettingAction extends FixedDwordAction {
    public static final int ACTION_ID = GameActionId.LATENCY_SETTING_ACTION_C1.id;
    public static final LatencySettingAction global = new LatencySettingAction();

    /**
     * Native support extracted from CServerApp::sendLatencySetting @00504C76,
     * MapVisualObject::sendPlayerJoinAndWaitForPlayerList @0040D791, and
     * FUN_004F3D68 @004F3D68.
     */
    public LatencySettingAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from FixedDwordAction::FixedDwordAction @0050BE6B and
     * CServerApp::sendLatencySetting @00504C76.
     */
    public LatencySettingAction(LatencySettingAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CServerApp::sendLatencySetting @00504C76.
     */
    public static LatencySettingAction createForLatencySetting(int latencyMillis, Player player) {
        LatencySettingAction action = global;
        action.ID.set(ACTION_ID);
        action.payloadDword.set(latencyMillis);
        action.playerID.set(player == null ? 0 : player.playerId);
        return action;
    }

    /**
     * Native support extracted from FixedDwordAction::Clone @00541190.
     */
    @Override
    public LatencySettingAction Clone() {
        return new LatencySettingAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2, CServerApp::sendLatencySetting @00504C76, and GameServer::handleServerGameAction @004F515D.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        CServerApp.setNextRemoteClientSendInterval(payloadDword.get());
    }

}
