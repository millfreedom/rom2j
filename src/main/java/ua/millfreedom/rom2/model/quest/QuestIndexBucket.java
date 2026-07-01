package ua.millfreedom.rom2.model.quest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Native support bucket used by QuestsStorage secondary quest index maps at @0052E212 and @0052E27D.
 */
public class QuestIndexBucket {
    //0x04
    public int secondaryIndexKey;

    //0x08
    public final Map<Integer, Quest> questsByKey = new LinkedHashMap<>();

    //0x24
    public ua.millfreedom.rom2.model.QuestsStorage storage;

    //0x28
    public Object iterationNode;

    //0x2C
    public Quest currentQuest;

    //0x30
    public Integer currentKey;

    /**
     * Native: QuestIndexBucket::QuestIndexBucket @0057F070.
     */
    public QuestIndexBucket(int secondaryIndexKey) {
        this.secondaryIndexKey = secondaryIndexKey;
    }

    /**
     * vtbl +0x14: QuestIndexBucket::processSecondaryIndexTick @0052E9B9.
     */
    public void processSecondaryIndexTick() {
        for (Map.Entry<Integer, Quest> entry : new ArrayList<>(questsByKey.entrySet())) {
            currentKey = entry.getKey();
            currentQuest = entry.getValue();
            switch (currentQuest.getEventMessageCode()) {
                case Quest.MESSAGE_CELL_EVENT -> handleCellQuestEvent(currentQuest);
                case Quest.MESSAGE_OWNER_CELL_EVENT -> handleOwnerCellQuestEvent(currentQuest);
                case Quest.MESSAGE_RAISE_DEAD_8_EVENT -> handleRaiseDead8QuestEvent(currentQuest);
                case Quest.MESSAGE_RAISE_DEAD_9_EVENT -> handleRaiseDead9QuestEvent(currentQuest);
                case Quest.MESSAGE_RAISE_DEAD_10_EVENT -> handleRaiseDead10QuestEvent(currentQuest);
                case Quest.MESSAGE_GROUP_CELL_EVENT -> handleGroupCellQuestEvent(currentQuest);
                default -> {
                }
            }
        }
    }

    /**
     * vtbl +0x18: QuestIndexBucket::handleCellQuestEvent @0057F0E0.
     */
    protected void handleCellQuestEvent(Quest quest) {
    }

    /**
     * vtbl +0x1C: QuestIndexBucket::handleOwnerCellQuestEvent @0057F0F0.
     */
    protected void handleOwnerCellQuestEvent(Quest quest) {
    }

    /**
     * vtbl +0x20: QuestIndexBucket::handleGroupCellQuestEvent @0057F100.
     */
    protected void handleGroupCellQuestEvent(Quest quest) {
    }

    /**
     * vtbl +0x24: QuestIndexBucket::handleRaiseDead8QuestEvent @0057F110.
     */
    protected void handleRaiseDead8QuestEvent(Quest quest) {
    }

    /**
     * vtbl +0x28: QuestIndexBucket::handleRaiseDead9QuestEvent @0057F120.
     */
    protected void handleRaiseDead9QuestEvent(Quest quest) {
    }

    /**
     * vtbl +0x2C: QuestIndexBucket::handleRaiseDead10QuestEvent @0057F130.
     */
    protected void handleRaiseDead10QuestEvent(Quest quest) {
    }

    /**
     * vtbl +0x30: QuestIndexBucket::getX @0057F140.
     */
    public int getX() {
        return 0;
    }

    /**
     * vtbl +0x34: QuestIndexBucket::getY @0057F150.
     */
    public int getY() {
        return 0;
    }
}
