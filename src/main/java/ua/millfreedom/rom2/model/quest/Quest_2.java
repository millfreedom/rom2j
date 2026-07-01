package ua.millfreedom.rom2.model.quest;

/**
 * Native class: Quest_2.
 */
public class Quest_2 extends Quest {
    /**
     * Native: Quest_2::New @00547DD0.
     * Fully ported.
     */
    public Quest_2() {
        super();
    }

    /**
     * vtbl +0x28: Quest_2::getId @00547DF0.
     * Fully ported.
     */
    @Override
    public int getId() {
        return 2;
    }

    /**
     * vtbl +0x14: Quest_2::OnQuestMessage @0052EB6F.
     * Fully ported.
     */
    @Override
    public int onQuestMessage(int message, int arg2, int arg3) {
        if (message == MESSAGE_UNIT_EVENT
                && highWord(arg3) == primaryArgument
                && lowWord(arg3) == ownerPlayerId
                && !isAccepted()
                && progressValue < secondaryArgument) {
            progressValue++;
            if (secondaryArgument <= progressValue) {
                storage.addQuestRelatedEntry(MESSAGE_MARK_ACTIVE, questKey, 0);
            }
        }
        return super.onQuestMessage(message, arg2, arg3);
    }
}
