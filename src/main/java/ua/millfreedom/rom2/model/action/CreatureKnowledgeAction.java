package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.KnowledgeTableCompression;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `BinaryBlobAction` packet id `0xBA` used to send the full RLE-compressed
 * `Player +0x44` knowledge table.
 */
public class CreatureKnowledgeAction extends BinaryBlobAction {
    public static final int ACTION_ID = GameActionId.CREATURE_KNOWLEDGE_ACTION_BA.id;
    public static final CreatureKnowledgeAction global = new CreatureKnowledgeAction();

    /**
     * Native support extracted from CServerApp::sendPlayerKnowledgeAction @00505582,
     * FUN_0053C140 @0053C140,
     * FUN_0053C1F0 @0053C1F0, and
     * FUN_0053C2B0 @0053C2B0.
     */
    public CreatureKnowledgeAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from BinaryBlobAction::BinaryBlobAction @0050C682 and
     * CServerApp::sendPlayerKnowledgeAction @00505582.
     */
    public CreatureKnowledgeAction(CreatureKnowledgeAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CServerApp::sendPlayerKnowledgeAction @00505582 packet field writes.
     */
    public static CreatureKnowledgeAction prepareForPlayerKnowledgeSnapshot(Player player) {
        CreatureKnowledgeAction action = global;
        action.ID.set(ACTION_ID);
        action.playerID.set(player == null ? 0 : player.playerId);
        byte[] compressedKnowledge = KnowledgeTableCompression.compress(player.knowledgeTable);
        action.payloadSize.set(compressedKnowledge.length);
        action.data.set(compressedKnowledge);
        return action;
    }

    /**
     * Native support extracted from BinaryBlobAction::Clone @00541870.
     */
    @Override
    public CreatureKnowledgeAction Clone() {
        return new CreatureKnowledgeAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0041547D / @0040EDCC.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        byte[] decompressed = MapVisualObject.decompressKnowledgeTable(data.get());
        System.arraycopy(decompressed, 0, mapVisualObject.playerKnowledgeTable, 0, decompressed.length);
        Globals.mainWindow.m_GameSession.refreshSavedPlayerKnowledgeTable();
        MapVisualObject.refreshPlayerKnowledgeIfNeeded();
    }

}
