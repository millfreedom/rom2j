package ua.millfreedom.rom2.model.quest;

import ua.millfreedom.rom2.Globals;

/**
 * Native class: Quest_6.
 */
public class Quest_6 extends Quest {
    /**
     * Native: Quest_6::New @00548030.
     * Fully ported.
     */
    public Quest_6() {
        super();
    }

    /**
     * vtbl +0x28: Quest_6::getId @00548050.
     * Fully ported.
     */
    @Override
    public int getId() {
        return 6;
    }

    /**
     * vtbl +0x2C: Quest_6::getEventMessageCode @00548060.
     * Fully ported.
     */
    @Override
    public int getEventMessageCode() {
        return MESSAGE_CELL_EVENT;
    }

    /**
     * vtbl +0x30: Quest_6::getEventMessageArgument @00548070.
     * Fully ported.
     */
    @Override
    public int getEventMessageArgument() {
        return primaryArgument;
    }

    /**
     * vtbl +0x14: Quest_6::OnQuestMessage @0052EE9B.
     * Fully ported.
     */
    @Override
    public int onQuestMessage(int message, int arg2, int arg3) {
        if (message == MESSAGE_CELL_EVENT) {
            if (arg2 == primaryArgument && highWord(arg3) == secondaryIndexKey && !isAccepted() && !isCompleted()) {
                int deadlineTick = progressValue + ((secondaryArgument * 1000) >>> 4);
                storage.addQuestRelatedEntry(
                        Globals.currentTickMillis() < deadlineTick ? MESSAGE_MARK_ACTIVE : MESSAGE_MARK_COMPLETE,
                        questKey,
                        0
                );
            }
        } else if (message == MESSAGE_SET_TIMER_START && arg2 == questKey) {
            progressValue = Globals.currentTickMillis();
        }
        return super.onQuestMessage(message, arg2, arg3);
    }
}
