package ua.millfreedom.rom2.model.quest;

/**
 * Native support record allocated by QuestsStorage::addQuestRelatedEntry @0052E07E.
 */
public final class QuestRelated {
    //0x00
    public final int firstValue;

    //0x04
    public final int secondValue;

    //0x08
    public final int thirdValue;

    /**
     * Native support extracted from QuestsStorage::addQuestRelatedEntry @0052E07E.
     */
    public QuestRelated(int firstValue, int secondValue, int thirdValue) {
        this.firstValue = firstValue;
        this.secondValue = secondValue;
        this.thirdValue = thirdValue;
    }
}
