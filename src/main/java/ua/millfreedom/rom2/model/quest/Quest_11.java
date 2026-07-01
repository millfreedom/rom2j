package ua.millfreedom.rom2.model.quest;

/**
 * Native class: Quest_11.
 */
public class Quest_11 extends Quest {
    /**
     * Native: Quest_11::New @005480E0.
     * Fully ported.
     */
    public Quest_11() {
        super();
    }

    /**
     * vtbl +0x28: Quest_11::getId @00548100.
     * Fully ported.
     */
    @Override
    public int getId() {
        return 11;
    }

    /**
     * vtbl +0x2C: Quest_11::getEventMessageCode @00548110.
     * Fully ported.
     */
    @Override
    public int getEventMessageCode() {
        return MESSAGE_CELL_EVENT;
    }

    /**
     * vtbl +0x30: Quest_11::getEventMessageArgument @00548120.
     * Fully ported.
     */
    @Override
    public int getEventMessageArgument() {
        return primaryArgument;
    }

    /**
     * vtbl +0x14: Quest_11::OnQuestMessage @0052EF86.
     * Fully ported.
     */
    @Override
    public int onQuestMessage(int message, int arg2, int arg3) {
        if (message == MESSAGE_UNIT_EVENT) {
            if (arg2 == primaryArgument && !isAccepted() && !isCompleted()) {
                if (lowWord(arg3) != ownerPlayerId) {
                    markOwnerQuestChanged(OWNER_QUEST_REMOVED_CHANGED_FLAG);
                    storage.removeQuest(this);
                    return 0;
                }
                storage.addQuestRelatedEntry(MESSAGE_MARK_ACTIVE, questKey, 0);
            }
        } else if (message == MESSAGE_CELL_EVENT) {
            if (arg2 == primaryArgument && highWord(arg3) == secondaryIndexKey && !isCompleted()) {
                storage.addQuestRelatedEntry(MESSAGE_MARK_COMPLETE, questKey, 0);
            }
        } else if (message == MESSAGE_TYPE_1_PROBE && arg2 == primaryArgument) {
            return questKey;
        }
        return super.onQuestMessage(message, arg2, arg3);
    }
}
