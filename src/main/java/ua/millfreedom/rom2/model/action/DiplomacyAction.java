package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CPlayer;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.actiondata.ActionPayloads;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `ShortArrayBlobAction` packet id `0xB9` used by
 * `CServerApp::sendDiplomacyStateSnapshot @00504E87`
 * to replay one player's diplomacy row, including the incoming war/alliance/vision state bits used for
 * diplomacy notifications.
 */
public class DiplomacyAction extends ShortArrayBlobAction {
    public static final int ACTION_ID = GameActionId.DIPLOMACY_ACTION_B9.id;
    public static final DiplomacyAction global = new DiplomacyAction();
    // Native CServerApp::sendDiplomacyStateSnapshot @00504E87 incoming alliance bit in the short payload.
    private static final int INCOMING_ALLIANCE_PAYLOAD_MASK = 0x20;
    // Native CServerApp::sendDiplomacyStateSnapshot @00504E87 incoming war bit in the short payload.
    private static final int INCOMING_WAR_PAYLOAD_MASK = 0x40;

    /**
     * Native support extracted from CServerApp::sendDiplomacyStateSnapshot @00504E87,
     * GameServer::FUN_004F1D9C @004F1D9C, and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public DiplomacyAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CServerApp::sendDiplomacyStateSnapshot @00504E87 and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public DiplomacyAction(DiplomacyAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from CServerApp::sendDiplomacyStateSnapshot @00504E87.
     */
    public static DiplomacyAction prepareForStateSnapshot(Player player) {
        DiplomacyAction action = global;
        action.ID.set(ACTION_ID);
        action.playerID.set(player.playerId);
        ActionPayloads.setShortArray(action.shortValueCount, action.shortValues, buildStateSnapshotPayload(player));
        return action;
    }

    /**
     * Native support extracted from CServerApp::sendDiplomacyStateSnapshot @00504E87 short-array payload writes.
     */
    private static short[] buildStateSnapshotPayload(Player player) {
        short[] diplomacyFlags = new short[Globals.gameServer.playerList.getMaxPlayerId() + 1];
        for (Player sourcePlayer : Globals.gameServer.playerList.players) {
            int sourcePlayerId = sourcePlayer.playerId & 0xFFFF;
            int outgoingFlags = Globals.gameServer.missionScriptRuntime.getRelationFlags(player, sourcePlayer);
            int incomingFlags = Globals.gameServer.missionScriptRuntime.getRelationFlags(sourcePlayer, player);
            int packedFlags = outgoingFlags & 0xFF;
            if ((incomingFlags & CPlayer.DIPLOMACY_VISIBLE_MASK) != 0) {
                packedFlags |= CPlayer.MAP_VISIBLE_MASK;
            }
            if ((incomingFlags & CPlayer.ALLIED_MASK) != 0) {
                packedFlags |= INCOMING_ALLIANCE_PAYLOAD_MASK;
            }
            if ((incomingFlags & CPlayer.ENEMY_MASK) != 0) {
                packedFlags |= INCOMING_WAR_PAYLOAD_MASK;
            }
            diplomacyFlags[sourcePlayerId] = (short) packedFlags;
        }
        return diplomacyFlags;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2.
     * Partial port. Java keeps the relation-flag copy, main-window diplomacy rebuild, and live-dialog refresh.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        if (mapVisualObject.currentPlayer != null) {
            mapVisualObject.currentPlayer.diplomacyFlags = ActionPayloads.getShortArray(shortValues);
            mapVisualObject.ensureCurrentPlayerSelfMapVisible();
        }
        Globals.mainWindow.rebuildDiplomacy();
        if (Globals.mainWindow.pDiplomacySettingsDialogVisualObject != null) {
            Globals.mainWindow.pDiplomacySettingsDialogVisualObject.setValue(Globals.mainWindow.m_Dilpomacy);
            Globals.mainWindow.pDiplomacySettingsDialogVisualObject.draw();
        }
    }

    /**
     * Native support extracted from CServerApp::sendDiplomacyStateSnapshot @00504E87 and
     * ShortArrayBlobAction::Clone @00541940.
     */
    @Override
    public DiplomacyAction Clone() {
        return new DiplomacyAction(this);
    }

}
