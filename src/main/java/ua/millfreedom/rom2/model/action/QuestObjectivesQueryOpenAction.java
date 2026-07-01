package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `TwoDwordAction` packet id `0xB6` used by
 * `CServerApp::sendQuestObjectivesQueryOpen @00505204`
 * to post `QUEST_OBJECTIVES | WM_QUERYOPEN` (`0x0433`) on the client.
 */
public class QuestObjectivesQueryOpenAction extends TwoDwordAction {
    public static final int ACTION_ID = GameActionId.QUEST_OBJECTIVES_QUERY_OPEN_ACTION_B6.id;
    public static final QuestObjectivesQueryOpenAction global = new QuestObjectivesQueryOpenAction();

    /**
     * Native support extracted from CServerApp::sendQuestObjectivesQueryOpen @00505204,
     * MapVisualObject::HandleGameAction @0040D9B2, and
     * scenario instant handlers FUN_00577B16 @00577B16 / FUN_00577C48 @00577C48.
     */
    public QuestObjectivesQueryOpenAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CServerApp::sendQuestObjectivesQueryOpen @00505204 and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public QuestObjectivesQueryOpenAction(QuestObjectivesQueryOpenAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from CServerApp::sendQuestObjectivesQueryOpen @00505204 packet field writes.
     */
    public static QuestObjectivesQueryOpenAction prepareForQuestObjectivesQueryOpen(
            Player player,
            int queryOpenCode,
            int unusedPayload
    ) {
        QuestObjectivesQueryOpenAction action = global;
        action.ID.set(ACTION_ID);
        action.playerID.set(player == null ? 0 : player.playerId);
        action.firstPayloadDword.set(queryOpenCode);
        action.secondPayloadDword.set(unusedPayload);
        return action;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040DA65.
     * Ported action-id case: `QuestObjectivesQueryOpenAction` forwards `HANDLE_QUEST_EVENT_DIALOG` (`0x0433`).
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        Globals.mainWindow.postMessage(MessageCodes.HANDLE_QUEST_EVENT_DIALOG, firstPayloadDword.get(), 0);
    }

    /**
     * Native support extracted from CServerApp::sendQuestObjectivesQueryOpen @00505204 and
     * TwoDwordAction::Clone @005410C0.
     */
    @Override
    public QuestObjectivesQueryOpenAction Clone() {
        return new QuestObjectivesQueryOpenAction(this);
    }

}
