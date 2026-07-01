package ua.millfreedom.rom2.model.quest;

/**
 * Native class: Quest_10.
 */
public class Quest_10 extends Quest {
    /**
     * Native: Quest_10::New @005483A0.
     * Fully ported.
     */
    public Quest_10() {
        super();
    }

    /**
     * vtbl +0x28: Quest_10::getId @005483C0.
     * Fully ported.
     */
    @Override
    public int getId() {
        return 10;
    }

    /**
     * vtbl +0x2C: Quest_10::getEventMessageCode @005483D0.
     * Fully ported.
     */
    @Override
    public int getEventMessageCode() {
        return MESSAGE_RAISE_DEAD_10_EVENT;
    }

    /**
     * vtbl +0x30: Quest_10::getEventMessageArgument @005483E0.
     * Fully ported.
     */
    @Override
    public int getEventMessageArgument() {
        return ownerPlayerId | (secondaryArgument << 16);
    }

    /**
     * vtbl +0x14: Quest_10::OnQuestMessage @0052F386.
     * Fully ported.
     */
    @Override
    public int onQuestMessage(int message, int arg2, int arg3) {
        if (message == MESSAGE_RAISE_DEAD_10_EVENT
                && secondaryArgument <= lowWord(arg2)
                && lowWord(arg3) == ownerPlayerId
                && highWord(arg3) == secondaryIndexKey
                && !isCompleted()) {
            int spellPower = arg2 >>> 16;
            tertiaryArgument = spellPower * spellPower >>> 3;
            storage.addQuestRelatedEntry(MESSAGE_MARK_ACTIVE, questKey, 0);
        }
        return super.onQuestMessage(message, arg2, arg3);
    }
}
