package ua.millfreedom.rom2.model.quest;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.QuestsStorage;

/**
 * Native class: Quest.
 */
public class Quest implements MfcSerializable {
    public static final int MESSAGE_INN_PROBE = 0x11;
    protected static final int MESSAGE_MARK_ACTIVE = 0x01;
    protected static final int MESSAGE_MARK_COMPLETE = 0x02;
    protected static final int MESSAGE_UNIT_EVENT = 0x04;
    protected static final int MESSAGE_GROUP_EVENT = 0x05;
    protected static final int MESSAGE_CELL_EVENT = 0x06;
    protected static final int MESSAGE_OWNER_CELL_EVENT = 0x07;
    protected static final int MESSAGE_RAISE_DEAD_8_EVENT = 0x08;
    protected static final int MESSAGE_RAISE_DEAD_9_EVENT = 0x09;
    protected static final int MESSAGE_RAISE_DEAD_10_EVENT = 0x0A;
    protected static final int MESSAGE_GROUP_CELL_EVENT = 0x0B;
    protected static final int MESSAGE_TYPE_1_PROBE = 0x0C;
    protected static final int MESSAGE_TYPE_3_PROBE = 0x0D;
    protected static final int MESSAGE_TYPE_5_PROBE = 0x0E;
    protected static final int MESSAGE_PLAYER_DEATH_EVENT = 0x0F;
    protected static final int MESSAGE_SET_TIMER_START = 0x10;
    protected static final int OWNER_QUEST_ACTIVE_CHANGED_FLAG = 0x20;
    protected static final int OWNER_QUEST_COMPLETE_CHANGED_FLAG = 0x40;
    protected static final int OWNER_QUEST_REMOVED_CHANGED_FLAG = 0x80;

    //0x04
    public int questKey;

    //0x08
    public int ownerPlayerId;

    //0x0C
    public int mapNumber;

    //0x10
    public int state;

    //0x14
    public int primaryArgument;

    //0x18
    public int secondaryIndexKey;

    //0x1C
    public int secondaryArgument;

    //0x20
    public int progressValue;

    //0x24
    public int tertiaryArgument;

    //0x28
    public QuestsStorage storage;

    //0x2C
    public Object runtimePayload;

    /**
     * Native: createQuestById @0052E3EB.
     * Fully ported.
     * <p>
     * Native support extracted from MapVisualObject::HandleGameAction @00414624,
     * CServerApp::sendQuestListAction @00506526, and Inn::generateInnQuestList @005303ED concrete quest allocation.
     */
    public static Quest createById(int questId) {
        return switch (questId) {
            case 1 -> new Quest_1();
            case 2 -> new Quest_2();
            case 3 -> new Quest_3();
            case 4 -> new Quest_4();
            case 5 -> new Quest_5();
            case 6 -> new Quest_6();
            case 8 -> new Quest_8();
            case 9 -> new Quest_9();
            case 10 -> new Quest_10();
            case 11 -> new Quest_11();
            case 12 -> new Quest_12();
            case 13 -> new Quest_13();
            default -> new Quest();
        };
    }

    /**
     * vtbl +0x08: CObject::Serialize @00401970.
     */
    @Override
    public void serialize(CArchive ar) {
        // Native CObject::Serialize is a no-op.
    }

    /**
     * Native: Quest::New @0052DBA5.
     * Fully ported.
     */
    public Quest() {
        state = 0;
        questKey = 0;
        ownerPlayerId = 0;
        primaryArgument = 0;
        secondaryIndexKey = 0;
        secondaryArgument = 0;
        progressValue = 0;
        storage = null;
        runtimePayload = null;
    }

    /**
     * vtbl +0x24: Quest::setQuestData @0052DC3A.
     * Fully ported.
     */
    public void setQuestData(
            int questKey,
            int ownerPlayerId,
            int mapNumber,
            int primaryArgument,
            int secondaryIndexKey,
            int secondaryArgument,
            int tertiaryArgument
    ) {
        state = 0;
        this.questKey = questKey;
        this.ownerPlayerId = ownerPlayerId;
        this.primaryArgument = primaryArgument;
        this.mapNumber = mapNumber;
        this.secondaryIndexKey = secondaryIndexKey;
        this.secondaryArgument = secondaryArgument;
        progressValue = 0;
        this.tertiaryArgument = tertiaryArgument;
        runtimePayload = null;
    }

    /**
     * vtbl +0x20: Quest::copyQuestDataFrom @0052DCA4.
     * Fully ported. Native copies Quest_Base bytes through secondaryArgument, then clears progress, tertiaryArgument,
     * and runtimePayload.
     */
    public void copyQuestDataFrom(Quest source) {
        questKey = source.questKey;
        ownerPlayerId = source.ownerPlayerId;
        mapNumber = source.mapNumber;
        state = source.state;
        primaryArgument = source.primaryArgument;
        secondaryIndexKey = source.secondaryIndexKey;
        secondaryArgument = source.secondaryArgument;
        progressValue = 0;
        tertiaryArgument = 0;
        runtimePayload = null;
    }

    /**
     * vtbl +0x28: Quest::GetId @00546A20.
     * Fully ported.
     */
    public int getId() {
        return 0;
    }

    /**
     * vtbl +0x2C: Quest::getEventMessageCode @00546A30.
     * Fully ported.
     */
    public int getEventMessageCode() {
        return 0;
    }

    /**
     * vtbl +0x30: Quest::getEventMessageArgument @00546A40.
     * Fully ported.
     */
    public int getEventMessageArgument() {
        return 0;
    }

    /**
     * vtbl +0x14: Quest::OnQuestMessage @0052DD20.
     * Fully ported.
     */
    public int onQuestMessage(int message, int arg2, int arg3) {
        if (message == MESSAGE_MARK_ACTIVE) {
            if (arg2 == questKey) {
                setStateTo1IfCurrentState0();
                markOwnerQuestChanged(OWNER_QUEST_ACTIVE_CHANGED_FLAG);
            }
            return 0;
        }

        if (message == MESSAGE_MARK_COMPLETE) {
            if (arg2 == questKey) {
                setStateTo2();
                markOwnerQuestChanged(OWNER_QUEST_COMPLETE_CHANGED_FLAG);
            }
            return 0;
        }

        if (message == MESSAGE_INN_PROBE
                && arg2 == ownerPlayerId
                && arg3 == mapNumber
                && state == 0) {
            return questKey;
        }

        return 0;
    }

    /**
     * Native: Quest::isAccepted @005484F0.
     * Fully ported.
     */
    public boolean isAccepted() {
        return state == 1;
    }

    /**
     * Native: Quest::isCompleted @00548510.
     * Fully ported.
     */
    public boolean isCompleted() {
        return state == 2;
    }

    /**
     * Native: Quest::isPending @0041E5C0.
     * Fully ported.
     */
    public boolean isPending() {
        return state == 0;
    }

    /**
     * Native: Quest::setProgressToCurrentTick @00549370.
     * Fully ported.
     */
    public void setProgressToCurrentTick() {
        progressValue = Globals.currentTickMillis();
    }

    /**
     * Native: Quest::SetState @00549390.
     * Fully ported.
     */
    public void setState(int state) {
        this.state = state;
    }

    /**
     * Native: Quest::getOwnerPlayerId @005493B0.
     * Fully ported.
     */
    public int getOwnerPlayerId() {
        return ownerPlayerId;
    }

    /**
     * Native: Quest::getMapNumber @005484D0.
     * Fully ported.
     */
    public int getMapNumber() {
        return mapNumber;
    }

    /**
     * Native: Quest::getRewardBudget @005493D0.
     * Fully ported.
     */
    public int getRewardBudget() {
        return tertiaryArgument;
    }

    /**
     * Native support extracted from Quest_1/2/3/5/8/9/10/11/12/13::OnQuestMessage low-word argument checks
     *
     * @0052EAC8, @0052EB6F, @0052EC1D, @0052ED92, @0052F222, @0052F2D4, @0052F386, @0052EF86,
     * @0052F0D4, and @0052F432.
     */
    protected static int lowWord(int value) {
        return value & 0xFFFF;
    }

    /**
     * Native support extracted from Quest_2/4/5/6/8/9/10/11/12::OnQuestMessage high-word argument checks
     *
     * @0052EB6F, @0052ECC4, @0052ED92, @0052EE9B, @0052F222, @0052F2D4, @0052F386, @0052EF86,
     * and @0052F0D4.
     */
    protected static int highWord(int value) {
        return value >> 16;
    }

    /**
     * vtbl +0x1C: Quest::SetStateTo1_if0 @0052DCE4.
     * Fully ported.
     */
    public void setStateTo1IfCurrentState0() {
        if (state == 0) {
            state = 1;
        }
    }

    /**
     * vtbl +0x18: Quest::SetStateTo2 @0052DD02.
     * Fully ported.
     */
    public void setStateTo2() {
        if (state != 2) {
            state = 2;
        }
    }

    /**
     * Native support extracted from Quest::OnQuestMessage @0052DD20 owner change-map updates.
     * Fully ported.
     */
    protected void markOwnerQuestChanged(int flag) {
        int flags = storage.ownerQuestChangeFlags.getOrDefault(ownerPlayerId, 0);
        storage.ownerQuestChangeFlags.put(ownerPlayerId, flags | flag);
    }
}
