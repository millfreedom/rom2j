package ua.millfreedom.rom2.model.quest;

/**
 * Native class: Quest_4.
 */
public class Quest_4 extends Quest {
    /**
     * Native: Quest_4::New @00547ED0.
     * Fully ported.
     */
    public Quest_4() {
        super();
    }

    /**
     * vtbl +0x28: Quest_4::getId @00547EF0.
     * Fully ported.
     */
    @Override
    public int getId() {
        return 4;
    }

    /**
     * vtbl +0x2C: Quest_4::getEventMessageCode @00547F00.
     * Fully ported.
     */
    @Override
    public int getEventMessageCode() {
        return MESSAGE_CELL_EVENT;
    }

    /**
     * vtbl +0x30: Quest_4::getEventMessageArgument @00547F10.
     * Fully ported.
     */
    @Override
    public int getEventMessageArgument() {
        return primaryArgument;
    }

    /**
     * vtbl +0x14: Quest_4::OnQuestMessage @0052ECC4.
     * Fully ported.
     */
    @Override
    public int onQuestMessage(int message, int arg2, int arg3) {
        if (message == MESSAGE_UNIT_EVENT) {
            if (arg2 == primaryArgument && !isCompleted()) {
                storage.addQuestRelatedEntry(MESSAGE_MARK_COMPLETE, questKey, 0);
            }
        } else if (message == MESSAGE_CELL_EVENT) {
            if (arg2 == primaryArgument && highWord(arg3) == secondaryIndexKey && !isAccepted()) {
                storage.addQuestRelatedEntry(MESSAGE_MARK_ACTIVE, questKey, 0);
            }
        } else if (message == MESSAGE_TYPE_1_PROBE && arg2 == primaryArgument) {
            return questKey;
        }
        return super.onQuestMessage(message, arg2, arg3);
    }
}
