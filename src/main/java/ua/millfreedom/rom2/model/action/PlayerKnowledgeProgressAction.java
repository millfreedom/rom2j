package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `FixedDwordAction` packet id `0xB3` used to send one updated player-knowledge byte
 * keyed by a defeated unit/server id.
 */
public class PlayerKnowledgeProgressAction extends FixedDwordAction {
    public static final int ACTION_ID = GameActionId.PLAYER_KNOWLEDGE_PROGRESS_ACTION_B3.id;
    public static final PlayerKnowledgeProgressAction global = new PlayerKnowledgeProgressAction();

    /**
     * Native support extracted from CServerApp::sendPlayerKnowledgeAction @00505582 and
     * FUN_0052B459 @0052B459.
     */
    public PlayerKnowledgeProgressAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from FixedDwordAction::FixedDwordAction @0050BE6B and
     * CServerApp::sendPlayerKnowledgeAction @00505582.
     */
    public PlayerKnowledgeProgressAction(PlayerKnowledgeProgressAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CServerApp::sendPlayerKnowledgeAction @00505582 packet field writes.
     */
    public static PlayerKnowledgeProgressAction prepareForKnowledgeProgress(int knowledgeServerId, Player player) {
        PlayerKnowledgeProgressAction action = global;
        action.ID.set(ACTION_ID);
        action.playerID.set(player == null ? 0 : player.playerId);
        int knowledgeTableIndex = knowledgeServerId & 0xFFFF;
        int knowledgeByte = player.knowledgeTable[knowledgeTableIndex] & 0xFF;
        action.payloadDword.set(knowledgeTableIndex | (knowledgeByte << 16));
        return action;
    }

    /**
     * Native support extracted from FixedDwordAction::Clone @00541190.
     */
    @Override
    public PlayerKnowledgeProgressAction Clone() {
        return new PlayerKnowledgeProgressAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @004153CC / @0040EE5B.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        int packedValue = payloadDword.get();
        int tableIndex = packedValue & 0xFFFF;
        mapVisualObject.playerKnowledgeTable[tableIndex] = (byte) ((packedValue >>> 16) & 0xFF);
        Globals.mainWindow.m_GameSession.refreshSavedPlayerKnowledgeTable();
        MapVisualObject.refreshPlayerKnowledgeIfNeeded();
    }

}
