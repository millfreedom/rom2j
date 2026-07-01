package ua.millfreedom.rom2.model.quest;

/**
 * Native class: Quest_3.
 */
public class Quest_3 extends Quest {
    /**
     * Native: Quest_3::New @00547E50.
     * Fully ported.
     */
    public Quest_3() {
        super();
    }

    /**
     * vtbl +0x28: Quest_3::getId @00547E70.
     * Fully ported.
     */
    @Override
    public int getId() {
        return 3;
    }

    /**
     * vtbl +0x14: Quest_3::OnQuestMessage @0052EC1D.
     * Fully ported.
     */
    @Override
    public int onQuestMessage(int message, int arg2, int arg3) {
        if (message == MESSAGE_GROUP_EVENT) {
            if (arg2 == primaryArgument && lowWord(arg3) == ownerPlayerId && !isAccepted()) {
                storage.addQuestRelatedEntry(MESSAGE_MARK_ACTIVE, questKey, 0);
            }
        } else if (message == MESSAGE_TYPE_3_PROBE && arg2 == primaryArgument && state == 0) {
            return questKey;
        }
        return super.onQuestMessage(message, arg2, arg3);
    }
}
