package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.QuestsStorage;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.actiondata.ActionPayloads;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.quest.Quest;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `ShortArrayBlobAction` packet id `0xBB` used to send the player's active quest list.
 */
public class PlayerQuestsAction extends ShortArrayBlobAction {
    public static final int ACTION_ID = GameActionId.PLAYER_QUESTS_ACTION_BB.id;
    public static final PlayerQuestsAction global = new PlayerQuestsAction();
    private static final int QUEST_WIRE_RECORD_WORD_COUNT = 15;

    /**
     * Native support extracted from CServerApp::sendQuestListAction @00506526,
     * GameServer::runServerLoopTick @004F08C0,
     * GameServer::FUN_004F1D9C @004F1D9C, and
     * Inn::closeUnitSession @0052F8E2.
     */
    public PlayerQuestsAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from ShortArrayBlobAction::ShortArrayBlobAction @0050C745 and
     * CServerApp::sendQuestListAction @00506526.
     */
    public PlayerQuestsAction(PlayerQuestsAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CServerApp::sendQuestListAction @00506526 packet field writes.
     */
    public static PlayerQuestsAction prepareForQuestList(QuestsStorage questsStorage, Player player) {
        return prepareQuestListAction(global, ACTION_ID, questsStorage, player);
    }

    /**
     * Native support extracted from CServerApp::sendQuestListAction @00506526 packet field writes shared by
     * `PlayerQuestsAction` and `InnQuestsAction`.
     */
    static <T extends ShortArrayBlobAction> T prepareQuestListAction(
            T action,
            int actionId,
            QuestsStorage questsStorage,
            Player player
    ) {
        action.ID.set(actionId);
        action.playerID.set(player.playerId);
        ActionPayloads.setShortArray(action.shortValueCount, action.shortValues, packQuestListPayload(questsStorage, player));
        return action;
    }

    /**
     * Native support extracted from ShortArrayBlobAction::Clone @00541940.
     */
    @Override
    public PlayerQuestsAction Clone() {
        return new PlayerQuestsAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00414624.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        boolean wasEmpty = mapVisualObject.questStorage.questsByKey.isEmpty();
        MapVisualObject.loadQuestListIntoStorage(mapVisualObject.questStorage, shortValueCount.get(), shortValues.get());
        if (wasEmpty && !mapVisualObject.questStorage.questsByKey.isEmpty()) {
            Globals.mainWindow.postMessage(MessageCodes.TOGGLE_QUEST_STATUS_DIALOG, 1, 0);
        }
    }

    /**
     * Native support extracted from CServerApp::sendQuestListAction @00506526.
     */
    private static short[] packQuestListPayload(QuestsStorage questsStorage, Player player) {
        int ownerPlayerId = (short) player.playerId;
        int questCount = 0;
        for (Quest quest : questsStorage.questsByKey.values()) {
            if (quest.ownerPlayerId == ownerPlayerId) {
                questCount++;
            }
        }

        short[] payload = new short[1 + questCount * QUEST_WIRE_RECORD_WORD_COUNT];
        payload[0] = (short) questCount;
        int payloadOffset = 1;
        for (Quest quest : questsStorage.questsByKey.values()) {
            if (quest.ownerPlayerId != ownerPlayerId) {
                continue;
            }
            payload[payloadOffset] = (short) quest.getId();
            writeQuestBaseDword(payload, payloadOffset + 1, quest.questKey);
            writeQuestBaseDword(payload, payloadOffset + 3, quest.ownerPlayerId);
            writeQuestBaseDword(payload, payloadOffset + 5, quest.mapNumber);
            writeQuestBaseDword(payload, payloadOffset + 7, quest.state);
            writeQuestBaseDword(payload, payloadOffset + 9, quest.primaryArgument);
            writeQuestBaseDword(payload, payloadOffset + 11, quest.secondaryIndexKey);
            writeQuestBaseDword(payload, payloadOffset + 13, quest.secondaryArgument);
            payloadOffset += QUEST_WIRE_RECORD_WORD_COUNT;
        }
        return payload;
    }

    /**
     * Native support extracted from the raw Quest_Base memcpy in CServerApp::sendQuestListAction @00506526.
     */
    private static void writeQuestBaseDword(short[] payload, int offset, int value) {
        payload[offset] = (short) value;
        payload[offset + 1] = (short) (value >>> Short.SIZE);
    }

}
