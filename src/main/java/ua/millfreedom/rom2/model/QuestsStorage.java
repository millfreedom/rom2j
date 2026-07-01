package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.quest.Quest;
import ua.millfreedom.rom2.model.quest.QuestIndexBucket;
import ua.millfreedom.rom2.model.quest.QuestRelated;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Native class: QuestsStorage.
 */
public final class QuestsStorage implements MfcSerializable {
    private static final int QUEST_REMOVED_FLAG = 0x01;

    //0x04
    public final Map<Integer, Quest> questsByKey = new LinkedHashMap<>();

    //0x20
    public final List<QuestRelated> questRelatedEntries = new ArrayList<>();

    //0x3C
    public final Map<Integer, QuestIndexBucket> questsBySecondaryIndex = new LinkedHashMap<>();

    //0x58
    public final Map<Integer, Integer> ownerQuestChangeFlags = new LinkedHashMap<>();

    //0x74
    public Object iterationNode;

    //0x78
    public Quest currentQuest;

    //0x7C
    public QuestIndexBucket currentIndexBucket;

    //0x80
    public Integer currentKey;

    /**
     * vtbl +0x08: CObject::Serialize @00401970.
     */
    @Override
    public void serialize(CArchive ar) {
        // Native CObject::Serialize is a no-op.
    }

    /**
     * Native: QuestsStorage::New @0052DE35.
     * Fully ported.
     */
    public QuestsStorage() {
    }

    /**
     * Native: QuestsStorage::clearSecondaryIndexBuckets @0052DEC1.
     * Fully ported.
     */
    public void clearSecondaryIndexBuckets() {
        for (QuestIndexBucket bucket : questsBySecondaryIndex.values()) {
            currentIndexBucket = bucket;
            if (currentIndexBucket != null) {
                currentIndexBucket.storage = null;
            }
        }
        questsBySecondaryIndex.clear();
    }

    /**
     * Native: QuestsStorage::RemoveQuest @0052E27D.
     * Fully ported.
     */
    public void removeQuest(Quest quest) {
        int flags = ownerQuestChangeFlags.getOrDefault(quest.ownerPlayerId, 0);
        ownerQuestChangeFlags.put(quest.ownerPlayerId, flags | QUEST_REMOVED_FLAG);
        questsByKey.remove(quest.questKey);
        quest.storage = null;
        if (quest.secondaryIndexKey != 0) {
            currentIndexBucket = questsBySecondaryIndex.get(quest.secondaryIndexKey);
            if (currentIndexBucket != null) {
                currentIndexBucket.questsByKey.remove(quest.questKey);
            }
        }
    }

    /**
     * Native: QuestsStorage::removeAndDeleteQuestsForOwner @0052E34C.
     * Fully ported. Java removes matching quests from storage; GC replaces native object deletion.
     */
    public void removeAndDeleteQuestsForOwner(int ownerPlayerId) {
        Iterator<Quest> iterator = questsByKey.values().iterator();
        while (iterator.hasNext()) {
            Quest quest = iterator.next();
            currentQuest = quest;
            currentKey = quest.questKey;
            if (quest.ownerPlayerId == ownerPlayerId || ownerPlayerId == 0) {
                int flags = ownerQuestChangeFlags.getOrDefault(quest.ownerPlayerId, 0);
                ownerQuestChangeFlags.put(quest.ownerPlayerId, flags | QUEST_REMOVED_FLAG);
                iterator.remove();
                quest.storage = null;
                if (quest.secondaryIndexKey != 0) {
                    currentIndexBucket = questsBySecondaryIndex.get(quest.secondaryIndexKey);
                    if (currentIndexBucket != null) {
                        currentIndexBucket.questsByKey.remove(quest.questKey);
                    }
                }
            }
        }
    }

    /**
     * Native: QuestsStorage::addQuest @0052E212.
     * Fully ported.
     */
    public void addQuest(Quest quest) {
        questsByKey.put(quest.questKey, quest);
        quest.storage = this;
        if (quest.secondaryIndexKey != 0) {
            currentIndexBucket = questsBySecondaryIndex.get(quest.secondaryIndexKey);
            if (currentIndexBucket != null) {
                currentIndexBucket.questsByKey.put(quest.questKey, quest);
            }
        }
    }

    /**
     * Native: QuestsStorage::registerSecondaryIndexBucket @0052E320.
     * Fully ported.
     */
    public void registerSecondaryIndexBucket(QuestIndexBucket bucket) {
        questsBySecondaryIndex.put(bucket.secondaryIndexKey, bucket);
        bucket.storage = this;
    }

    /**
     * Native: QuestsStorage::addQuestRelatedEntry @0052E07E.
     * Fully ported.
     */
    public void addQuestRelatedEntry(int firstValue, int secondValue, int thirdValue) {
        questRelatedEntries.add(new QuestRelated(firstValue, secondValue, thirdValue));
    }

    /**
     * vtbl +0x14: QuestsStorage::FindQuestKeyByMessage @0052E198.
     * Native iterates `questsByKey` and returns the first map key whose virtual
     * `Quest::OnQuestMessage @0052DD20` result is nonzero; Java mirrors the recovered container dispatch while concrete
     * quest subclass message overrides live on the concrete Quest descendants.
     * Fully ported.
     */
    public int findQuestKeyByMessage(int message, int arg2, int arg3) {
        for (Map.Entry<Integer, Quest> entry : questsByKey.entrySet()) {
            currentKey = entry.getKey();
            currentQuest = entry.getValue();
            if (currentQuest.onQuestMessage(message, arg2, arg3) != 0) {
                return currentKey;
            }
        }
        return 0;
    }

    /**
     * Native: QuestsStorage::FindQuestByKey @004A1050.
     * Fully ported; Java map lookup represents the native CMapWordToPtr::Lookup helper chain.
     */
    public Quest findQuestByKey(int questKey) {
        return questsByKey.get(questKey);
    }

    /**
     * Native: QuestsStorage::chooseDifferentSecondaryIndexKey @0052E91E.
     * Fully ported.
     */
    public int chooseDifferentSecondaryIndexKey(int excludedKey) {
        if (questsBySecondaryIndex.size() < 2) {
            return 0;
        }
        do {
            int index = Utils.randInclusive(questsBySecondaryIndex.size() - 1);
            Iterator<Integer> iterator = questsBySecondaryIndex.keySet().iterator();
            for (int skipped = 0; skipped < index; skipped++) {
                iterator.next();
            }
            currentKey = iterator.next();
        } while (currentKey == excludedKey);
        return currentKey;
    }

    /**
     * Native: QuestsStorage::processPendingQuestMessages @0052E122.
     * Fully ported.
     */
    public void processPendingQuestMessages() {
        tickSecondaryIndexedQuests();
        drainQueuedQuestMessages();
    }

    /**
     * Native support extracted from QuestsStorage::tickSecondaryIndexedQuests @0052E13D.
     * Fully ported.
     */
    private void tickSecondaryIndexedQuests() {
        for (QuestIndexBucket bucket : questsBySecondaryIndex.values()) {
            currentIndexBucket = bucket;
            currentIndexBucket.processSecondaryIndexTick();
        }
    }

    /**
     * Native support extracted from QuestsStorage::drainQueuedQuestMessages @0052E0C4.
     * Fully ported.
     */
    private void drainQueuedQuestMessages() {
        while (!questRelatedEntries.isEmpty()) {
            QuestRelated related = questRelatedEntries.removeFirst();
            findQuestKeyByMessage(related.firstValue, related.secondValue, related.thirdValue);
        }
    }

}
