package ua.millfreedom.rom2.model.quest;

/**
 * Native class: Quest_1.
 */
public class Quest_1 extends Quest {
    /**
     * Native: Quest_1::New @00547D50.
     * Fully ported.
     */
    public Quest_1() {
        super();
    }

    /**
     * vtbl +0x28: Quest_1::getId @00547D70.
     * Fully ported.
     */
    @Override
    public int getId() {
        return 1;
    }

    /**
     * vtbl +0x14: Quest_1::OnQuestMessage @0052EAC8.
     * Fully ported.
     */
    @Override
    public int onQuestMessage(int message, int arg2, int arg3) {
        if (message == MESSAGE_UNIT_EVENT) {
            if (arg2 == primaryArgument && lowWord(arg3) == ownerPlayerId && !isAccepted()) {
                storage.addQuestRelatedEntry(MESSAGE_MARK_ACTIVE, questKey, 0);
            }
        } else if (message == MESSAGE_TYPE_1_PROBE && arg2 == primaryArgument && state == 0) {
            return questKey;
        }
        return super.onQuestMessage(message, arg2, arg3);
    }
}
