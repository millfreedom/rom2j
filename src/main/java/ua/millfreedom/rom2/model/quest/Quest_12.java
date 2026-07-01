package ua.millfreedom.rom2.model.quest;

/**
 * Native class: Quest_12.
 */
public class Quest_12 extends Quest {
    /**
     * Native: Quest_12::New @00548190.
     * Fully ported.
     */
    public Quest_12() {
        super();
    }

    /**
     * vtbl +0x28: Quest_12::getId @005481B0.
     * Fully ported.
     */
    @Override
    public int getId() {
        return 12;
    }

    /**
     * vtbl +0x2C: Quest_12::getEventMessageCode @005481C0.
     * Fully ported.
     */
    @Override
    public int getEventMessageCode() {
        return MESSAGE_GROUP_CELL_EVENT;
    }

    /**
     * vtbl +0x30: Quest_12::getEventMessageArgument @005481D0.
     * Fully ported.
     */
    @Override
    public int getEventMessageArgument() {
        return primaryArgument;
    }

    /**
     * vtbl +0x14: Quest_12::OnQuestMessage @0052F0D4.
     * Fully ported.
     */
    @Override
    public int onQuestMessage(int message, int arg2, int arg3) {
        if (message == MESSAGE_GROUP_EVENT) {
            if (arg2 == primaryArgument && !isAccepted() && !isCompleted()) {
                if (lowWord(arg3) != ownerPlayerId) {
                    markOwnerQuestChanged(OWNER_QUEST_REMOVED_CHANGED_FLAG);
                    storage.removeQuest(this);
                    return 0;
                }
                storage.addQuestRelatedEntry(MESSAGE_MARK_ACTIVE, questKey, 0);
            }
        } else if (message == MESSAGE_GROUP_CELL_EVENT) {
            if (arg2 == primaryArgument && highWord(arg3) == secondaryIndexKey && !isCompleted()) {
                storage.addQuestRelatedEntry(MESSAGE_MARK_COMPLETE, questKey, 0);
            }
        } else if (message == MESSAGE_TYPE_3_PROBE && arg2 == primaryArgument) {
            return questKey;
        }
        return super.onQuestMessage(message, arg2, arg3);
    }
}
