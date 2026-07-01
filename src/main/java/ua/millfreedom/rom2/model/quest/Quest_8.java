package ua.millfreedom.rom2.model.quest;

/**
 * Native class: Quest_8.
 */
public class Quest_8 extends Quest {
    /**
     * Native: Quest_8::New @00548240.
     * Fully ported.
     */
    public Quest_8() {
        super();
    }

    /**
     * vtbl +0x28: Quest_8::getId @00548260.
     * Fully ported.
     */
    @Override
    public int getId() {
        return 8;
    }

    /**
     * vtbl +0x2C: Quest_8::getEventMessageCode @00548270.
     * Fully ported.
     */
    @Override
    public int getEventMessageCode() {
        return MESSAGE_RAISE_DEAD_8_EVENT;
    }

    /**
     * vtbl +0x30: Quest_8::getEventMessageArgument @00548280.
     * Fully ported.
     */
    @Override
    public int getEventMessageArgument() {
        return ownerPlayerId | (secondaryArgument << 16);
    }

    /**
     * vtbl +0x14: Quest_8::OnQuestMessage @0052F222.
     * Fully ported.
     */
    @Override
    public int onQuestMessage(int message, int arg2, int arg3) {
        if (message == MESSAGE_RAISE_DEAD_8_EVENT
                && secondaryArgument <= lowWord(arg2)
                && lowWord(arg3) == ownerPlayerId
                && highWord(arg3) == secondaryIndexKey
                && !isCompleted()) {
            int spellPower = arg2 >>> 16;
            tertiaryArgument = spellPower * spellPower / 5;
            storage.addQuestRelatedEntry(MESSAGE_MARK_ACTIVE, questKey, 0);
        }
        return super.onQuestMessage(message, arg2, arg3);
    }
}
