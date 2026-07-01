package ua.millfreedom.rom2.model.quest;

/**
 * Native class: Quest_5.
 */
public class Quest_5 extends Quest {
    /**
     * Native: Quest_5::New @00547F80.
     * Fully ported.
     */
    public Quest_5() {
        super();
    }

    /**
     * vtbl +0x28: Quest_5::getId @00547FA0.
     * Fully ported.
     */
    @Override
    public int getId() {
        return 5;
    }

    /**
     * vtbl +0x2C: Quest_5::getEventMessageCode @00547FB0.
     * Fully ported.
     */
    @Override
    public int getEventMessageCode() {
        return MESSAGE_OWNER_CELL_EVENT;
    }

    /**
     * vtbl +0x30: Quest_5::getEventMessageArgument @00547FC0.
     * Fully ported.
     */
    @Override
    public int getEventMessageArgument() {
        return primaryArgument;
    }

    /**
     * vtbl +0x14: Quest_5::OnQuestMessage @0052ED92.
     * Fully ported.
     */
    @Override
    public int onQuestMessage(int message, int arg2, int arg3) {
        if (message == MESSAGE_OWNER_CELL_EVENT) {
            if (arg2 == primaryArgument && highWord(arg3) == secondaryIndexKey) {
                if (lowWord(arg3) == ownerPlayerId && !isAccepted()) {
                    storage.addQuestRelatedEntry(MESSAGE_MARK_ACTIVE, questKey, 0);
                } else if (lowWord(arg3) != ownerPlayerId && !isCompleted()) {
                    storage.addQuestRelatedEntry(MESSAGE_MARK_COMPLETE, questKey, 0);
                }
            }
        } else if (message == MESSAGE_TYPE_5_PROBE && arg2 == primaryArgument && state == 0) {
            return questKey;
        }
        return super.onQuestMessage(message, arg2, arg3);
    }
}
