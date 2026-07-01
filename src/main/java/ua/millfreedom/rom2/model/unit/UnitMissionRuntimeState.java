package ua.millfreedom.rom2.model.unit;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.Token;
import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.model.spell.Spell;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * UnitMissionRuntimeState native class.
 * <p>
 * Known native methods:
 * - UnitMissionRuntimeState::New        @00569003
 * - UnitMissionRuntimeState::~UnitMissionRuntimeState @005690b0
 * - UnitMissionRuntimeState::Serialize  @00574452
 * - UnitMissionRuntimeState::RestoreContext @00576d00
 * - UnitMissionRuntimeState::copyFrom   @0057ADA2
 * <p>
 * Native layout size is 0xB8 bytes, with a trailing CList<short>* at +0xB4.
 * We keep full raw bytes for unknown fields and also expose known typed offsets.
 */
public class UnitMissionRuntimeState implements MfcSerializable {
    private static final int STRUCT_SIZE = 0xB8;
    private static final int O_MISSION_SCRIPT_CELL = 0x00;
    private static final int O_WAYPOINT_TARGET_CELL = 0x02;
    private static final int O_WAYPOINT_REFRESH_FLAG = 0x04;
    private static final int O_COMMAND = 0x08;
    private static final int O_RUNTIME_STATE = 0x09;
    private static final int O_COMMAND_CELL = 0x0A;
    private static final int O_TARGET_TOKEN = 0x0C;
    private static final int O_RANGE_TARGET_TOKEN = 0x10;
    private static final int O_ATTACK_RANGE = 0x14;
    private static final int O_COMMAND_STATE_BYTE = 0x15;
    private static final int O_W1 = 0x16;
    private static final int O_REPATH_TARGET_TOKEN = 0x18;
    private static final int O_ASSIGNED_TARGET_UNIT = 0x20;
    private static final int O_COMMAND_FORMATION_OFFSET_X = 0x24;
    private static final int O_COMMAND_FORMATION_OFFSET_Y = 0x26;
    private static final int O_SPELL_TARGET_TOKEN = 0x28;
    private static final int O_SPELL = 0x30;
    private static final int O_GROUP_SCRIPT_STATE = 0x38;
    private static final int O_CELL_SPELL_TARGET_CELL = 0x3C;
    private static final int O_WITHDRAW = 0x40;
    private static final int O_WIMPY = 0x44;
    private static final int O_BATTLE_PREFERENCE_MODE = 0x4C;
    private static final int O_UNIT_SCRIPT_STATE = 0x50;
    private static final int O_ENGAGEMENT_ACTIVE_FLAG = 0x54;
    private static final int O_CELL = 0x58;
    private static final int O_ENGAGEMENT_CELL_REPEAT_COUNT = 0x5A;
    private static final int O_SPELL_ACTION_MODE = 0x5C;
    private static final int O_VIRTUAL_CASTER_QUEUED_FLAG = 0x60;
    private static final int O_MISSION_SPEED_OVERRIDE_ACTIVE_FLAG = 0x6C;
    private static final int O_INTERACTION_TARGET = 0x68;
    private static final int O_RANGE_TARGET_MODE = 0x70;
    private static final int O_SEE_INVISIBLE = 0x71;
    private static final int O_SPELL_INDEX = 0x78;
    private static final int O_ENGAGEMENT_SPELLS = 0x7C;
    private static final int O_ENGAGEMENT_SPELL_PROBABILITIES = 0x88;
    private static final int O_LAST_ENGAGEMENT_SOURCE_UNIT = 0x94;
    private static final int O_LAST_ENGAGEMENT_TICK = 0x98;
    private static final int O_ENGAGEMENT_PROJECTED_CELL = 0x9C;
    private static final int O_PATROL_REPOSITION_TICKS = 0x9E;
    private static final int O_PATROL_REPOSITION_CELL = 0xA0;
    private static final int O_LAST_INCOMING_SPELL_ID = 0xA4;
    private static final int O_LAST_INCOMING_SPELL_TICK = 0xA8;
    private static final int O_ENGAGEMENT_COUNTER = 0xAC;
    private static final int O_PENDING_MISSION_ENTRY_CELL = 0xB0;

    //0x00
    public byte[] raw = new byte[STRUCT_SIZE];

    // Known fields from structure annotations/references.
    //0x00
    public int missionScriptCell;
    //0x02
    public int waypointTargetCell;
    //0x04
    public int waypointRefreshFlag;
    //0x08
    public int command;
    //0x09
    public int runtimeState;
    //0x0A
    public int commandCell;
    //0x0C
    public Token targetToken;
    // Java-only restore token for native +0x0C before UnitMissionRuntimeState::RestoreContext @00576D00 resolves the pointer map.
    public Object targetTokenRestoreToken;
    //0x10
    public Token rangeTargetToken;
    // Java-only restore token for native +0x10 before UnitMissionRuntimeState::RestoreContext @00576D00 resolves the pointer map.
    public Object rangeTargetTokenRestoreToken;
    //0x14
    public int attackRange;
    //0x15
    public int commandStateByte;
    //0x16
    public int w1;
    //0x18
    public Token repathTargetToken;
    // Java-only restore token for native +0x18 before UnitMissionRuntimeState::RestoreContext @00576D00 resolves the pointer map.
    public Object repathTargetTokenRestoreToken;
    //0x20
    public Unit assignedTargetUnit;
    // Java-only restore token for native +0x20 before UnitMissionRuntimeState::RestoreContext @00576D00 resolves the pointer map.
    public Object assignedTargetUnitRestoreToken;
    //0x24
    public int commandFormationOffsetX;
    //0x26
    public int commandFormationOffsetY;
    //0x28
    public Token spellTargetToken;
    // Java-only restore token for native +0x28 before UnitMissionRuntimeState::RestoreContext @00576D00 resolves the pointer map.
    public Object spellTargetTokenRestoreToken;
    //0x30
    public Spell spell;
    // Java-only restore token for native +0x30 before UnitMissionRuntimeState::RestoreContext @00576D00 resolves the pointer map.
    public Object spellRestoreToken;
    //0x38
    public int groupScriptState;
    //0x3C
    public int cellSpellTargetCell;
    //0x40
    public int withdraw;
    //0x44
    public int wimpy;
    //0x4C
    public int battlePreferenceMode;
    //0x50
    public int unitScriptState;
    //0x54
    public int engagementActiveFlag;
    //0x58
    public int cell;
    //0x5A
    public int engagementCellRepeatCount;
    //0x5C
    public int spellActionMode;
    //0x60
    public int virtualCasterQueuedFlag;
    //0x6C
    public int missionSpeedOverrideActiveFlag;
    //0x68
    public Object interactionTarget;
    //0x70
    public int rangeTargetMode;
    //0x71
    public int seeInvisible;
    //0x78
    public int spellIndex;
    //0x7C
    public final int[] engagementSpells = new int[3];
    //0x88
    public final int[] engagementSpellProbabilities = new int[3];
    //0x94
    public Unit lastEngagementSourceUnit;
    // Java-only serialized pointer token for native +0x94; UnitMissionRuntimeState::RestoreContext @00576D00 does not resolve it.
    public Object lastEngagementSourceUnitRestoreToken;
    //0x98
    public int lastEngagementTick;
    //0x9C
    public int engagementProjectedCell;
    //0x9E
    public int patrolRepositionTicks;
    //0xA0
    public int patrolRepositionCell;
    //0xA4
    public int lastIncomingSpellId;
    //0xA8
    public int lastIncomingSpellTick;
    //0xAC
    public int engagementCounter;
    //0xB0
    public int pendingMissionEntryCell;

    //0xB4
    public final CustomList<Short> waypointCells = CustomList.std(Short.class);

    /**
     * Native: UnitMissionRuntimeState::New @00569003.
     * Fully ported.
     */
    public UnitMissionRuntimeState() {
        resetToDefaults();
    }

    /**
     * Native: UnitMissionRuntimeState::copyFrom @0057ADA2.
     * Mirrors the native copy constructor: copy the fixed 0xB8-byte state, replace this object's waypoint list storage,
     * then deep-copy the source waypoint cells.
     */
    public UnitMissionRuntimeState copyFrom(UnitMissionRuntimeState source) {
        raw = Arrays.copyOf(source.raw, STRUCT_SIZE);
        missionScriptCell = source.missionScriptCell;
        waypointTargetCell = source.waypointTargetCell;
        waypointRefreshFlag = source.waypointRefreshFlag;
        command = source.command;
        runtimeState = source.runtimeState;
        commandCell = source.commandCell;
        targetToken = source.targetToken;
        targetTokenRestoreToken = source.targetTokenRestoreToken;
        rangeTargetToken = source.rangeTargetToken;
        rangeTargetTokenRestoreToken = source.rangeTargetTokenRestoreToken;
        attackRange = source.attackRange;
        commandStateByte = source.commandStateByte;
        w1 = source.w1;
        repathTargetToken = source.repathTargetToken;
        repathTargetTokenRestoreToken = source.repathTargetTokenRestoreToken;
        assignedTargetUnit = source.assignedTargetUnit;
        assignedTargetUnitRestoreToken = source.assignedTargetUnitRestoreToken;
        commandFormationOffsetX = source.commandFormationOffsetX;
        commandFormationOffsetY = source.commandFormationOffsetY;
        spellTargetToken = source.spellTargetToken;
        spellTargetTokenRestoreToken = source.spellTargetTokenRestoreToken;
        spell = source.spell;
        spellRestoreToken = source.spellRestoreToken;
        groupScriptState = source.groupScriptState;
        cellSpellTargetCell = source.cellSpellTargetCell;
        withdraw = source.withdraw;
        wimpy = source.wimpy;
        battlePreferenceMode = source.battlePreferenceMode;
        unitScriptState = source.unitScriptState;
        engagementActiveFlag = source.engagementActiveFlag;
        cell = source.cell;
        engagementCellRepeatCount = source.engagementCellRepeatCount;
        spellActionMode = source.spellActionMode;
        virtualCasterQueuedFlag = source.virtualCasterQueuedFlag;
        missionSpeedOverrideActiveFlag = source.missionSpeedOverrideActiveFlag;
        interactionTarget = source.interactionTarget;
        rangeTargetMode = source.rangeTargetMode;
        seeInvisible = source.seeInvisible;
        spellIndex = source.spellIndex;
        System.arraycopy(source.engagementSpells, 0, engagementSpells, 0, engagementSpells.length);
        System.arraycopy(
                source.engagementSpellProbabilities,
                0,
                engagementSpellProbabilities,
                0,
                engagementSpellProbabilities.length
        );
        lastEngagementSourceUnit = source.lastEngagementSourceUnit;
        lastEngagementSourceUnitRestoreToken = source.lastEngagementSourceUnitRestoreToken;
        lastEngagementTick = source.lastEngagementTick;
        engagementProjectedCell = source.engagementProjectedCell;
        patrolRepositionTicks = source.patrolRepositionTicks;
        patrolRepositionCell = source.patrolRepositionCell;
        lastIncomingSpellId = source.lastIncomingSpellId;
        lastIncomingSpellTick = source.lastIncomingSpellTick;
        engagementCounter = source.engagementCounter;
        pendingMissionEntryCell = source.pendingMissionEntryCell;
        waypointCells.clear();
        waypointCells.addAll(source.waypointCells);
        return this;
    }

    /**
     * Native support extracted from UnitMissionRuntimeState::New @00569003.
     * Fully ported. Java keeps the native waypoint CList as Java-owned waypointCells storage.
     */
    public void resetToDefaults() {
        // Native: memset(this,0,0xB8); command=0; runtimeState=0; battlePreferenceMode=0; SeeInvisible=1; w1=0xFFDC.
        Arrays.fill(raw, (byte) 0);
        command = 0;
        runtimeState = 0;
        commandCell = 0;
        targetToken = null;
        targetTokenRestoreToken = null;
        rangeTargetToken = null;
        rangeTargetTokenRestoreToken = null;
        missionScriptCell = 0;
        waypointTargetCell = 0;
        waypointRefreshFlag = 0;
        attackRange = 0;
        commandStateByte = 0;
        repathTargetToken = null;
        repathTargetTokenRestoreToken = null;
        assignedTargetUnit = null;
        assignedTargetUnitRestoreToken = null;
        commandFormationOffsetX = 0;
        commandFormationOffsetY = 0;
        spellTargetToken = null;
        spellTargetTokenRestoreToken = null;
        spell = null;
        spellRestoreToken = null;
        groupScriptState = 0;
        cellSpellTargetCell = 0;
        withdraw = 0;
        wimpy = 0;
        battlePreferenceMode = 0;
        unitScriptState = 0;
        engagementActiveFlag = 0;
        cell = 0;
        engagementCellRepeatCount = 0;
        spellActionMode = 0;
        virtualCasterQueuedFlag = 0;
        missionSpeedOverrideActiveFlag = 0;
        interactionTarget = null;
        rangeTargetMode = 0;
        seeInvisible = 1;
        spellIndex = 0;
        Arrays.fill(engagementSpells, 0);
        Arrays.fill(engagementSpellProbabilities, 0);
        lastEngagementSourceUnit = null;
        lastEngagementSourceUnitRestoreToken = null;
        lastEngagementTick = 0;
        engagementProjectedCell = 0;
        patrolRepositionTicks = 0;
        patrolRepositionCell = 0;
        lastIncomingSpellId = 0;
        lastIncomingSpellTick = 0;
        engagementCounter = 0;
        pendingMissionEntryCell = 0;
        w1 = 0xFFDC;
        waypointCells.clear();
        encodeKnownToRaw();
    }

    /**
     * Native: UnitMissionRuntimeState::Serialize @00574452.
     * Fully ported. Java preserves the native raw block plus serialized waypointCells payload, while native waypointCells deletion and
     * reallocation are represented by clearing the existing Java waypointCells.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        if (!ar.isStoring()) {
            // Native: delete old waypointCells (if any), Read(this,0xB8), allocate waypointCells, Serialize(waypointCells).
            raw = ar.readBytes(STRUCT_SIZE);
            decodeKnownFromRaw();
            waypointCells.clear();
            ar.serialize(waypointCells);
        } else {
            ensureRawSize();
            encodeKnownToRaw();
            ar.writeBytes(raw);
            ar.serialize(waypointCells);
        }
    }

    /**
     * Native: UnitMissionRuntimeState::RestoreContext @00576D00.
     * Fully ported.
     */
    public void restoreContext() {
        Object targetTokenRef = targetTokenRestoreToken != null ? targetTokenRestoreToken : targetToken;
        targetToken = (Token) Globals.gameServer.lookupPointerMapOrKeepToken(targetTokenRef);
        targetTokenRestoreToken = null;
        Object rangeTargetTokenRef = rangeTargetTokenRestoreToken != null ? rangeTargetTokenRestoreToken : rangeTargetToken;
        rangeTargetToken = (Token) Globals.gameServer.lookupPointerMapOrKeepToken(rangeTargetTokenRef);
        rangeTargetTokenRestoreToken = null;
        Object repathTargetTokenRef = repathTargetTokenRestoreToken != null ? repathTargetTokenRestoreToken : repathTargetToken;
        repathTargetToken = (Token) Globals.gameServer.lookupPointerMapOrKeepToken(repathTargetTokenRef);
        repathTargetTokenRestoreToken = null;
        Object assignedTargetUnitRef = assignedTargetUnitRestoreToken != null ? assignedTargetUnitRestoreToken : assignedTargetUnit;
        assignedTargetUnit = (Unit) Globals.gameServer.lookupPointerMapOrKeepToken(assignedTargetUnitRef);
        assignedTargetUnitRestoreToken = null;
        Object spellTargetTokenRef = spellTargetTokenRestoreToken != null ? spellTargetTokenRestoreToken : spellTargetToken;
        spellTargetToken = (Token) Globals.gameServer.lookupPointerMapOrKeepToken(spellTargetTokenRef);
        spellTargetTokenRestoreToken = null;
        Object spellRef = spellRestoreToken != null ? spellRestoreToken : spell;
        spell = (Spell) Globals.gameServer.lookupPointerMapOrKeepToken(spellRef);
        spellRestoreToken = null;
        interactionTarget = Globals.gameServer.lookupPointerMapOrKeepToken(interactionTarget);
    }

    // not ported.
    private void ensureRawSize() {
        if (raw == null || raw.length != STRUCT_SIZE) {
            raw = new byte[STRUCT_SIZE];
        }
    }

    /**
     * Native support extracted from UnitMissionRuntimeState::Serialize @00574452 raw-block reads.
     * Fully ported for the currently modeled field offsets.
     */
    private void decodeKnownFromRaw() {
        ByteBuffer bb = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
        missionScriptCell = bb.getShort(O_MISSION_SCRIPT_CELL) & 0xFFFF;
        waypointTargetCell = bb.getShort(O_WAYPOINT_TARGET_CELL) & 0xFFFF;
        waypointRefreshFlag = bb.getInt(O_WAYPOINT_REFRESH_FLAG);
        command = bb.get(O_COMMAND) & 0xFF;
        runtimeState = bb.get(O_RUNTIME_STATE) & 0xFF;
        commandCell = bb.getShort(O_COMMAND_CELL) & 0xFFFF;
        targetToken = null;
        targetTokenRestoreToken = bb.getInt(O_TARGET_TOKEN);
        rangeTargetToken = null;
        rangeTargetTokenRestoreToken = bb.getInt(O_RANGE_TARGET_TOKEN);
        attackRange = bb.get(O_ATTACK_RANGE) & 0xFF;
        commandStateByte = bb.get(O_COMMAND_STATE_BYTE) & 0xFF;
        w1 = bb.getShort(O_W1) & 0xFFFF;
        repathTargetToken = null;
        repathTargetTokenRestoreToken = bb.getInt(O_REPATH_TARGET_TOKEN);
        assignedTargetUnit = null;
        assignedTargetUnitRestoreToken = bb.getInt(O_ASSIGNED_TARGET_UNIT);
        commandFormationOffsetX = bb.getShort(O_COMMAND_FORMATION_OFFSET_X);
        commandFormationOffsetY = bb.getShort(O_COMMAND_FORMATION_OFFSET_Y);
        spellTargetToken = null;
        spellTargetTokenRestoreToken = bb.getInt(O_SPELL_TARGET_TOKEN);
        spell = null;
        spellRestoreToken = bb.getInt(O_SPELL);
        groupScriptState = bb.getInt(O_GROUP_SCRIPT_STATE);
        cellSpellTargetCell = bb.getShort(O_CELL_SPELL_TARGET_CELL) & 0xFFFF;
        withdraw = bb.getInt(O_WITHDRAW);
        wimpy = bb.getInt(O_WIMPY);
        battlePreferenceMode = bb.get(O_BATTLE_PREFERENCE_MODE) & 0xFF;
        unitScriptState = bb.getInt(O_UNIT_SCRIPT_STATE);
        engagementActiveFlag = bb.getInt(O_ENGAGEMENT_ACTIVE_FLAG);
        cell = bb.getShort(O_CELL) & 0xFFFF;
        engagementCellRepeatCount = bb.get(O_ENGAGEMENT_CELL_REPEAT_COUNT) & 0xFF;
        spellActionMode = bb.getInt(O_SPELL_ACTION_MODE);
        virtualCasterQueuedFlag = bb.getInt(O_VIRTUAL_CASTER_QUEUED_FLAG);
        missionSpeedOverrideActiveFlag = bb.getInt(O_MISSION_SPEED_OVERRIDE_ACTIVE_FLAG);
        interactionTarget = bb.getInt(O_INTERACTION_TARGET);
        rangeTargetMode = bb.get(O_RANGE_TARGET_MODE) & 0xFF;
        seeInvisible = bb.get(O_SEE_INVISIBLE) & 0xFF;
        spellIndex = bb.get(O_SPELL_INDEX) & 0xFF;
        for (int i = 0; i < engagementSpells.length; i++) {
            engagementSpells[i] = bb.getInt(O_ENGAGEMENT_SPELLS + i * Integer.BYTES);
            engagementSpellProbabilities[i] = bb.getInt(O_ENGAGEMENT_SPELL_PROBABILITIES + i * Integer.BYTES);
        }
        lastEngagementSourceUnit = null;
        lastEngagementSourceUnitRestoreToken = bb.getInt(O_LAST_ENGAGEMENT_SOURCE_UNIT);
        lastEngagementTick = bb.getInt(O_LAST_ENGAGEMENT_TICK);
        engagementProjectedCell = bb.getShort(O_ENGAGEMENT_PROJECTED_CELL) & 0xFFFF;
        patrolRepositionTicks = bb.get(O_PATROL_REPOSITION_TICKS) & 0xFF;
        patrolRepositionCell = bb.getShort(O_PATROL_REPOSITION_CELL) & 0xFFFF;
        lastIncomingSpellId = bb.getInt(O_LAST_INCOMING_SPELL_ID);
        lastIncomingSpellTick = bb.getInt(O_LAST_INCOMING_SPELL_TICK);
        engagementCounter = bb.getInt(O_ENGAGEMENT_COUNTER);
        pendingMissionEntryCell = bb.getShort(O_PENDING_MISSION_ENTRY_CELL) & 0xFFFF;
    }

    /**
     * Native support extracted from UnitMissionRuntimeState::Serialize @00574452 raw-block writes.
     * Fully ported for the currently modeled field offsets.
     */
    private void encodeKnownToRaw() {
        ByteBuffer bb = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
        bb.putShort(O_MISSION_SCRIPT_CELL, (short) missionScriptCell);
        bb.putShort(O_WAYPOINT_TARGET_CELL, (short) waypointTargetCell);
        bb.putInt(O_WAYPOINT_REFRESH_FLAG, waypointRefreshFlag);
        bb.put(O_COMMAND, (byte) command);
        bb.put(O_RUNTIME_STATE, (byte) runtimeState);
        bb.putShort(O_COMMAND_CELL, (short) commandCell);
        bb.putInt(O_TARGET_TOKEN, Utils.encodePointerLike(targetToken != null ? targetToken : targetTokenRestoreToken));
        bb.putInt(O_RANGE_TARGET_TOKEN, Utils.encodePointerLike(
                rangeTargetToken != null ? rangeTargetToken : rangeTargetTokenRestoreToken
        ));
        bb.put(O_ATTACK_RANGE, (byte) attackRange);
        bb.put(O_COMMAND_STATE_BYTE, (byte) commandStateByte);
        bb.putShort(O_W1, (short) w1);
        bb.putInt(O_REPATH_TARGET_TOKEN, Utils.encodePointerLike(
                repathTargetToken != null ? repathTargetToken : repathTargetTokenRestoreToken
        ));
        bb.putInt(O_ASSIGNED_TARGET_UNIT, Utils.encodePointerLike(
                assignedTargetUnit != null ? assignedTargetUnit : assignedTargetUnitRestoreToken
        ));
        bb.putShort(O_COMMAND_FORMATION_OFFSET_X, (short) commandFormationOffsetX);
        bb.putShort(O_COMMAND_FORMATION_OFFSET_Y, (short) commandFormationOffsetY);
        bb.putInt(O_SPELL_TARGET_TOKEN, Utils.encodePointerLike(
                spellTargetToken != null ? spellTargetToken : spellTargetTokenRestoreToken
        ));
        bb.putInt(O_SPELL, Utils.encodePointerLike(spell != null ? spell : spellRestoreToken));
        bb.putInt(O_GROUP_SCRIPT_STATE, groupScriptState);
        bb.putShort(O_CELL_SPELL_TARGET_CELL, (short) cellSpellTargetCell);
        bb.putInt(O_WITHDRAW, withdraw);
        bb.putInt(O_WIMPY, wimpy);
        bb.put(O_BATTLE_PREFERENCE_MODE, (byte) battlePreferenceMode);
        bb.putInt(O_UNIT_SCRIPT_STATE, unitScriptState);
        bb.putInt(O_ENGAGEMENT_ACTIVE_FLAG, engagementActiveFlag);
        bb.putShort(O_CELL, (short) cell);
        bb.put(O_ENGAGEMENT_CELL_REPEAT_COUNT, (byte) engagementCellRepeatCount);
        bb.putInt(O_SPELL_ACTION_MODE, spellActionMode);
        bb.putInt(O_VIRTUAL_CASTER_QUEUED_FLAG, virtualCasterQueuedFlag);
        bb.putInt(O_MISSION_SPEED_OVERRIDE_ACTIVE_FLAG, missionSpeedOverrideActiveFlag);
        bb.putInt(O_INTERACTION_TARGET, Utils.encodePointerLike(interactionTarget));
        bb.put(O_RANGE_TARGET_MODE, (byte) rangeTargetMode);
        bb.put(O_SEE_INVISIBLE, (byte) seeInvisible);
        bb.put(O_SPELL_INDEX, (byte) spellIndex);
        for (int i = 0; i < engagementSpells.length; i++) {
            bb.putInt(O_ENGAGEMENT_SPELLS + i * Integer.BYTES, engagementSpells[i]);
            bb.putInt(O_ENGAGEMENT_SPELL_PROBABILITIES + i * Integer.BYTES, engagementSpellProbabilities[i]);
        }
        bb.putInt(O_LAST_ENGAGEMENT_SOURCE_UNIT, Utils.encodePointerLike(
                lastEngagementSourceUnit != null ? lastEngagementSourceUnit : lastEngagementSourceUnitRestoreToken
        ));
        bb.putInt(O_LAST_ENGAGEMENT_TICK, lastEngagementTick);
        bb.putShort(O_ENGAGEMENT_PROJECTED_CELL, (short) engagementProjectedCell);
        bb.put(O_PATROL_REPOSITION_TICKS, (byte) patrolRepositionTicks);
        bb.putShort(O_PATROL_REPOSITION_CELL, (short) patrolRepositionCell);
        bb.putInt(O_LAST_INCOMING_SPELL_ID, lastIncomingSpellId);
        bb.putInt(O_LAST_INCOMING_SPELL_TICK, lastIncomingSpellTick);
        bb.putInt(O_ENGAGEMENT_COUNTER, engagementCounter);
        bb.putShort(O_PENDING_MISSION_ENTRY_CELL, (short) pendingMissionEntryCell);
    }
}
