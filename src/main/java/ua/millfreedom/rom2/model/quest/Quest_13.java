package ua.millfreedom.rom2.model.quest;

/**
 * Native class: Quest_13.
 */
public class Quest_13 extends Quest {
    /**
     * Native: Quest_13::New @00548450.
     * Fully ported.
     */
    public Quest_13() {
        super();
    }

    /**
     * vtbl +0x28: Quest_13::getId @00548470.
     * Fully ported.
     */
    @Override
    public int getId() {
        return 13;
    }

    /**
     * vtbl +0x14: Quest_13::OnQuestMessage @0052F432.
     * Fully ported.
     */
    @Override
    public int onQuestMessage(int message, int arg2, int arg3) {
        if (message == MESSAGE_PLAYER_DEATH_EVENT
                && arg2 == primaryArgument
                && lowWord(arg3) == ownerPlayerId
                && !isCompleted()) {
            storage.addQuestRelatedEntry(MESSAGE_MARK_ACTIVE, questKey, 0);
        }
        return super.onQuestMessage(message, arg2, arg3);
    }
}
