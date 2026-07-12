package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CPlayer;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;


/**
 * Native `ChatTextAction` packet id `0x96` used by `GameServer::handlePlayerJoinRequest @004F0CBE`
 * and `CServerApp::sendLobbyPlayerInfoSnapshot @00504D39`
 * to broadcast lobby player name/slot/presence metadata.
 */
public class NewPlayerAction extends ChatTextAction {
    public static final int ACTION_ID = GameActionId.NEW_PLAYER_ACTION_96.id;
    public static final NewPlayerAction global = new NewPlayerAction();

    /**
     * Native support extracted from GameServer::handlePlayerJoinRequest @004F0CBE and
     * CServerApp::sendLobbyPlayerInfoSnapshot @00504D39.
     */
    public NewPlayerAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from GameServer::handlePlayerJoinRequest @004F0CBE and
     * CServerApp::sendLobbyPlayerInfoSnapshot @00504D39.
     */
    public NewPlayerAction(NewPlayerAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CServerApp::sendLobbyPlayerInfoSnapshot @00504D39.
     */
    public static NewPlayerAction prepareForLobbyPlayerInfoSnapshot(Player targetPlayer, Player sourcePlayer) {
        NewPlayerAction action = global;
        action.ID.set(ACTION_ID);
        action.playerID.set(targetPlayer.playerId);
        action.firstPayloadDword.set(packLobbyPlayerInfo(sourcePlayer));
        action.text.set(sourcePlayer.name);
        return action;
    }

    /**
     * Native support extracted from GameServer::handlePlayerJoinRequest @004F0CBE `NEW_PLAYER_ACTION_96` send.
     */
    public static NewPlayerAction prepareForPlayerJoinBroadcast(Player player, boolean newPlayer) {
        NewPlayerAction action = global;
        action.ID.set(ACTION_ID);
        action.playerID.set(0);
        int packedInfo = packLobbyPlayerInfo(player);
        if (newPlayer) {
            packedInfo |= 0x02000000;
        }
        action.firstPayloadDword.set(packedInfo);
        action.text.set(player.name);
        return action;
    }

    /**
     * Native support extracted from CServerApp::sendLobbyPlayerInfoSnapshot @00504D39 firstPayloadDword packing.
     */
    private static int packLobbyPlayerInfo(Player player) {
        return (player.playerId & 0xFF)
                | ((player.scenarioPlayerId & 0xFF) << 8)
                | (((player.colorSlot - 1) & 0xFF) << 16)
                | (player.isActive != 0 ? 0x01000000 : 0);
    }

    /**
     * Native support extracted from GameServer::handlePlayerJoinRequest @004F0CBE and
     * CServerApp::sendLobbyPlayerInfoSnapshot @00504D39.
     */
    @Override
    public NewPlayerAction Clone() {
        return new NewPlayerAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @004156C5.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        int packedPlayerState = firstPayloadDword.get();
        int playerId = packedPlayerState & 0xFF;
        boolean firstClientPlayerSlotOnly = mapVisualObject.clientPlayers.size() == 1;
        CPlayer player = mapVisualObject.ensureClientPlayerById(playerId);
        player.playerId = playerId;
        player.networkSlotId = (packedPlayerState >>> 8) & 0xFF;
        player.color = (packedPlayerState >>> 16) & 0xFF;
        if ((packedPlayerState & 0x01000000) == 0) {
            player.flags &= ~1;
        } else {
            player.flags |= 1;
        }
        player.name.set(text.get());

        if (firstClientPlayerSlotOnly) {
            Globals.mainWindow.m_GameSession.skipFormerCharacterPrompt = (packedPlayerState & 0x02000000) != 0 ? 1 : 0;
            mapVisualObject.currentPlayer = player;
            player.setDiplomacyFlagAtGrow(0, 0);
            player.setDiplomacyFlagAtGrow(player.playerId, 0x3A);
        } else if ((player.flags & 1) == 0) {
            if (player != mapVisualObject.currentPlayer) {
                mapVisualObject.currentPlayer.setDiplomacyFlagAtGrow(player.playerId, 0);
            }
        } else {
            mapVisualObject.currentPlayer.setDiplomacyFlagAtGrow(player.playerId, 1);
        }
        Globals.mainWindow.rebuildDiplomacy();
        Globals.mainWindow.postMessage(MessageCodes.MULTIPLAYER_LOBBY_REFRESH_PLAYER_LIST, 0, 0);
    }
}
