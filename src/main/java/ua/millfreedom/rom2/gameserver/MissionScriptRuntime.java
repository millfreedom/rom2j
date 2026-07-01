package ua.millfreedom.rom2.gameserver;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.gameserver.missionruntime.*;
import ua.millfreedom.rom2.model.*;
import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.model.enums.EffectId;
import ua.millfreedom.rom2.model.enums.SpellId;
import ua.millfreedom.rom2.model.enums.UnitActionState;
import ua.millfreedom.rom2.model.spell.AreaEffect;
import ua.millfreedom.rom2.model.spell.Spell;
import ua.millfreedom.rom2.model.spell.TransientSpellCastSpec;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.world.CWorldMap;
import ua.millfreedom.rom2.res.ResInHeap;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

import static ua.millfreedom.rom2.model.enums.MissionActionCode.*;

/**
 * Java model of MissionScriptRuntime.
 * <p>
 * Core native functions:
 * - ctor with map/player list: 0x00568c9a
 * - base ctor chain root: 0x0056811b
 * - child runtime init: 0x005688d7
 * - save serialization: 0x0057468d
 * - post-load self pointer restore: 0x00576fb1
 */
public final class MissionScriptRuntime extends MissionRuntimeBase implements MfcSerializable {
    public static final int SIZE_0X3E8 = 0x3E8;
    public static final int SIZE_0XFA0 = 0xFA0;
    public static final int SIZE_0X28 = 0x28;
    public static final int SELECTOR_SLOT_COUNT = 10;
    public static final int PLAYER_RELATION_HOSTILE_MASK = 0x01;
    public static final int SCRIPT_TURN_COUNTER_VARIABLE = 999;
    public static final int SCRIPT_RUNTIME_COUNTER_VARIABLE = 0x386;
    public static final int SCRIPT_DISTANCE_UNAVAILABLE = 0xFF;
    public static final int SCENARIO_TRANSIENT_VAR_BASE = 0x2F0;
    public static final int GROUP_SCRIPT_STATE_DISABLED = -1;
    public static final int GROUP_SCRIPT_STATE_IDLE = 0;
    public static final int GROUP_SCRIPT_STATE_ADVANCE_TO_MISSION_CELL = 1;
    public static final int GROUP_SCRIPT_STATE_MOVE_TO_CELL = 2;
    public static final int GROUP_SCRIPT_STATE_ATTACK = 3;
    public static final int GROUP_SCRIPT_STATE_COMMAND_CELL_MOVE = 4;
    public static final int GROUP_SCRIPT_STATE_COMMAND_CELL_OR_TARGET = 5;
    public static final int GROUP_SCRIPT_STATE_WANDER_TARGET_CELL = 0x11;
    public static final int GROUP_SCRIPT_STATE_PATROL = 0x12;
    public static final int GROUP_SCRIPT_STATE_PREPARE_DEFAULT = 0x13;
    public static final int MISSION_UNIT_EVENT_ENGAGEMENT = 1;
    public static final int MISSION_UNIT_EVENT_TARGETED_SPELL_ACTION = 2;
    public static final int MISSION_UNIT_EVENT_CELL_SPELL_ACTION = 3;
    public static final int MISSION_UNIT_EVENT_TARGET_ASSIGNMENT = 4;
    public static final int UNIT_STATUS_INACTIVE = 0x08;
    public static final int UNIT_MISSION_COMMAND_NONE = 0;
    public static final int UNIT_MISSION_COMMAND_MOVE_TO_CELL = 1;
    public static final int UNIT_MISSION_COMMAND_MOVE_TO_TARGET_UNIT = 2;
    public static final int UNIT_MISSION_COMMAND_REPATH_TO_TARGET = 4;
    public static final int UNIT_MISSION_COMMAND_ENGAGE_TARGET = 5;
    public static final int UNIT_MISSION_COMMAND_NEAREST_ENGAGEMENT = 6;
    public static final int UNIT_MISSION_COMMAND_PICKUP_TRANSITION = 7;
    public static final int UNIT_MISSION_COMMAND_TARGETED_SPELL = 8;
    public static final int UNIT_MISSION_COMMAND_CELL_SPELL = 9;
    public static final int UNIT_MISSION_COMMAND_FACE_LAST = 10;
    public static final int UNIT_MISSION_COMMAND_LOOK_AROUND = 0x0B;
    public static final int UNIT_MISSION_COMMAND_ATTACK_COMMAND_CELL = 0x0C;
    public static final int UNIT_MISSION_COMMAND_INTERACT_COMMAND_CELL = 0x0F;
    public static final int UNIT_MISSION_RUNTIME_STATE_MOVING = 1;
    public static final int UNIT_MISSION_RUNTIME_STATE_SPELL_ACTION = 2;
    public static final int UNIT_MISSION_RUNTIME_STATE_PATHING = 3;
    public static final int UNIT_MISSION_RUNTIME_STATE_EFFECT_HIDDEN = 4;
    public static final int EFFECT_FLAG_MISSION_HIDDEN = 0x40000;
    public static final int WIMPY_SCAN_RADIUS_BONUS = 5;
    public static final int WITHDRAW_SCAN_RADIUS = 2;
    public static final int RETREAT_PROJECT_DISTANCE_TILES = 3;
    public static final int RETREAT_SELF_EFFECT_KEY_MASK = 0x0C;
    public static final int MAGE_SUPPORT_MIN_MP_EXCLUSIVE = 10;
    public static final int MAGE_SUPPORT_INACTIVE_MIN_MP = 5;
    public static final int MAGE_SUPPORT_REPEAT_DELAY_TICKS = 0x14;
    public static final int PLAYER_RELATION_ALLIED_MASK = 0x02;
    public static final int PREFERRED_AREA_FRIENDLY_SELF_PENALTY = 100;
    public static final int PREFERRED_AREA_SCORE_INITIAL = -1000;
    public static final int SCENARIO_SCRIPT_ARG_GROUP = 2;
    public static final int SCENARIO_SCRIPT_ARG_PLAYER = 3;
    public static final int SCENARIO_SCRIPT_ARG_UNIT = 4;
    public static final String SCRIPT_REG_PREFIX = "world/mission/scr_";
    public static final String SCRIPT_REG_SUFFIX = ".reg";

    //0xC784
    public final byte[] scriptVariables = new byte[SIZE_0XFA0];
    //0xE6C4
    public final byte[] scriptPatternFiredFlags = new byte[SIZE_0X3E8];
    //0xBE0C
    public int missionCompleteCount;
    //0xBE10
    public int specialMissionFailureArmed;
    //0xBE14
    public int missionFailureValue;

    //0xC20C
    public final int[] selectorMatchCounts = new int[100];
    //0xC39C
    public final int[] selectorState = new int[100];
    //0xC52C
    public final byte[] runtimeScratchBytes = new byte[SIZE_0X28];
    //0xD724
    public final Object[] scriptObjectVariables = new Object[SIZE_0X3E8];
    //0xC554
    public final SelectorSlot[] selectorSlots = new SelectorSlot[SELECTOR_SLOT_COUNT];
    //0xC70C
    public int selectorSlotCount = SELECTOR_SLOT_COUNT;
    //0xC714
    public final Map<Integer, Unit> scriptUnitsByReferenceKey = new LinkedHashMap<>();
    //0xC730
    public final Map<Integer, UnitGroup> scriptGroupsByReferenceKey = new LinkedHashMap<>();
    //0xC74C
    public final Map<Integer, Player> scriptPlayersByReferenceIndex = new LinkedHashMap<>();
    //0xC768
    public final CustomList<Integer> runtimeScratchArray = CustomList.std(Integer.class);
    //0xBE18
    public ScratchByteState scratchByteState = new ScratchByteState();
    //0xEAAC
    public CustomList<ScriptCheck> scriptChecks = new CustomList<>(ScriptCheck.class);
    //0xEAB0
    public CustomList<ScriptInstant> scriptInstants = new CustomList<>(ScriptInstant.class);
    //0xEAB4
    public CustomList<ScriptPattern> scriptPatterns = new CustomList<>(ScriptPattern.class);
    //0xEAB8
    public String scriptResourcePath = "";
    //0xEB1C
    public ResInHeap scriptResource;
    //0xC77C
    public PlayerList playerList;

    //0xBE00
    public Unit specialMissionUnit;
    //0xBE04
    public int dword0xBE04;
    //0xBE08
    public int specialMissionCompletionArmed;
    //0xC710
    public int runtimeCounterInstantCount;
    //0xC780
    public int scenarioEntryCounter;
    //0xBDEC
    public int specialMissionCoordinateToggleTicks;
    //0xBDED
    public int specialMissionCoordinateIndex;
    //0xBDEE
    public final byte[] specialMissionCoordinateTable = new byte[6];
    //0xBDFC
    public int specialMissionPhaseCounter;

    /**
     * Native: MissionScriptRuntime::initializeDefault @00568BCF.
     * Fully ported.
     */
    public MissionScriptRuntime() {
        initializeDefault();
    }

    /**
     * Native: MissionScriptRuntime::initializeWithWorldMap @00568DCD.
     * Fully ported.
     */
    public MissionScriptRuntime(CWorldMap worldMap) {
        initializeWithWorldMap(worldMap);
    }

    /**
     * Native: MissionScriptRuntime::MissionScriptRuntime @00568C9A.
     * Fully ported.
     */
    public MissionScriptRuntime(CWorldMap worldMap, PlayerList playerList) {
        initializeWithWorldMapAndPlayers(worldMap, playerList);
    }

    /**
     * Native: MissionScriptRuntime::initializeDefault @00568BCF.
     * Fully ported.
     */
    public void initializeDefault() {
        initializeBaseWrapper();
        scratchByteState.initialize();
        initializeSelectorsAndRuntimeContainers();
        resetRuntimeState();
    }

    /**
     * Native: MissionScriptRuntime::initializeWithWorldMap @00568DCD.
     * Fully ported.
     */
    public void initializeWithWorldMap(CWorldMap worldMap) {
        initializeDefault();
        attachRuntimeReferences(worldMap, null);
    }

    /**
     * Native support extracted from MissionScriptRuntime::MissionScriptRuntime @00568C9A.
     * Fully ported.
     */
    public void initializeWithWorldMapAndPlayers(CWorldMap worldMap, PlayerList playerList) {
        initializeDefault();
        attachRuntimeReferences(worldMap, playerList);
    }

    /**
     * Native: constructor staging in MissionScriptRuntime::MissionScriptRuntime @00568C9A.
     */
    public void initializeSelectorsAndRuntimeContainers() {
        for (int i = 0; i < selectorSlots.length; i++) {
            selectorSlots[i] = new SelectorSlot();
        }
        scriptUnitsByReferenceKey.clear();
        scriptGroupsByReferenceKey.clear();
        scriptPlayersByReferenceIndex.clear();
        runtimeScratchArray.clear();
    }

    /**
     * Native: MissionScriptRuntime::resetRuntimeState @005688D7.
     * Fully ported.
     */
    public void resetRuntimeState() {
        lastTurnElapsedMsLowDword = 0;
        lastTurnElapsedMsHighDword = 0;
        serializedStateByte0 = 0;
        serializedStateByte1 = 0;
        missionTurnCounter = 0;

        specialMissionCoordinateToggleTicks = 0;
        specialMissionCoordinateIndex = 0;
        specialMissionCoordinateTable[0] = 'K';
        specialMissionCoordinateTable[1] = 'A';
        specialMissionCoordinateTable[2] = 'U';
        specialMissionCoordinateTable[3] = 'A';
        specialMissionCoordinateTable[4] = '_';
        specialMissionCoordinateTable[5] = 'A';
        specialMissionPhaseCounter = 0;

        specialMissionUnit = null;
        dword0xBE04 = 0;
        specialMissionCompletionArmed = 0;
        missionCompleteCount = 0;
        specialMissionFailureArmed = 0;
        missionFailureValue = 0;
        runtimeCounterInstantCount = 0;
        scenarioEntryCounter = 0;

        worldMap = null;
        Arrays.fill(runtimeScratchBytes, (byte) 0);
        Arrays.fill(scriptVariables, (byte) 0);
        Arrays.fill(scriptObjectVariables, null);
        Arrays.fill(scriptPatternFiredFlags, (byte) 0);
        Arrays.fill(serializedStateBlob, (byte) 0);
        Arrays.fill(runtimeStateBlob, (byte) 0);
        scriptResourcePath = "";
        playerList = null;

        bindMissionDiplomacyStateOwner();
        ByteBuffer scriptVariableBytes = ByteBuffer.wrap(scriptVariables).order(ByteOrder.LITTLE_ENDIAN);
        scriptVariableBytes.putInt(0xE18, 0);
        scriptVariableBytes.putInt(0xE10, 0);
        scriptVariableBytes.putInt(0xE10, 0);
        scriptVariableBytes.putInt(0xE14, 1);
        scriptVariableBytes.putInt(0xF9C, 0);

        scriptChecks = new CustomList<>(ScriptCheck.class);
        scriptInstants = new CustomList<>(ScriptInstant.class);
        scriptPatterns = new CustomList<>(ScriptPattern.class);
    }

    /**
     * Native: MissionScriptRuntime::advanceScenarioEntryCounter @00573D34.
     * Fully ported.
     */
    public void advanceScenarioEntryCounter() {
        scenarioEntryCounter++;
    }

    /**
     * Native: MissionScriptRuntime::processMissionTurn @0056FE4D.
     * Fully ported.
     */
    public void processMissionTurn(PlayerList playerList) {
        turnPerfMonitor.queryCounter();
        if (forceAllMissionGroupsUpdate == 0) {
            activatingPerfMonitor.queryCounter();
            worldMap.unitVisibilityState0x92ECC.rebuildUnitVisibilityState();
            turnTimingStats.recordActivatingElapsed(activatingPerfMonitor.finishElapsedMilliseconds());
        }
        scriptPerfMonitor.queryCounter();
        advanceScenarioScripts();
        turnTimingStats.recordScriptElapsed(scriptPerfMonitor.finishElapsedMilliseconds());
        aiPerfMonitor.queryCounter();
        for (Player player : playerList.players) {
            processPlayerMissionGroups(player);
        }
        processVirtualCasterSpellCasts();
        turnTimingStats.recordAiElapsed(aiPerfMonitor.finishElapsedMilliseconds());
        missionTurnCounter++;
        int totalElapsed = turnPerfMonitor.finishElapsedMilliseconds();
        lastTurnElapsedMsLowDword = totalElapsed;
        lastTurnElapsedMsHighDword = turnPerfMonitor.elapsedMillisecondsHighDword;
        turnTimingStats.recordTurnElapsed(totalElapsed);
        if (Globals.gameServer.isTurnTracingEnabled()) {
            CServerApp.sendServerChatText(
                    "Stats: last turn - %d, average: %d.\n".formatted(
                            turnTimingStats.lastTurnElapsedMs,
                            Integer.divideUnsigned(
                                    turnTimingStats.accumulatedTurnElapsedMs,
                                    missionTurnCounter
                            )
                    ),
                    null
            );
        }
    }

    /**
     * Native: MissionScriptRuntime::updateActiveUnitMissionActions @0056920C.
     * Fully ported.
     */
    public void updateActiveUnitMissionActions() {
        turnPerfMonitor.queryCounter();
        for (Unit unit : worldMap.activeUnits0xA456C) {
            updateUnitMissionAction(unit);
        }
        missionTurnCounter++;
        turnPerfMonitor.finishElapsedMilliseconds();
        lastTurnElapsedMsLowDword = 0;
        lastTurnElapsedMsHighDword = 0;
        serializedStateByte0 = 0;
        missionTurnCounter++;
    }

    /**
     * Native: MissionScriptRuntime::initializePlayerMissionRuntimeState @005688B2.
     * Fully ported.
     */
    public void initializePlayerMissionRuntimeState() {
        initializeActivePlayerMissionGroups();
        initializeInactivePlayerMissionUnitsAndScanGrid();
    }

    /**
     * Native: MissionScriptRuntime::initializeActivePlayerMissionGroups @00568723.
     * Fully ported.
     */
    public void initializeActivePlayerMissionGroups() {
        clearActiveUnitRuntimePathScratch();
        for (Player player : playerList.players) {
            if (player.isActive != 0) {
                initializeActivePlayerUnitGroups(player);
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::clearActiveUnitRuntimePathScratch @005687BF.
     * Fully ported.
     */
    public void clearActiveUnitRuntimePathScratch() {
        for (Unit unit : worldMap.activeUnits0xA456C) {
            worldMap.clearUnitRuntimePathScratch(unit);
        }
    }

    /**
     * Native: MissionScriptRuntime::initializeInactivePlayerMissionUnitsAndScanGrid @00568771.
     * Fully ported.
     */
    public void initializeInactivePlayerMissionUnitsAndScanGrid() {
        for (Player player : playerList.players) {
            if (player.isActive == 0) {
                initializeInactivePlayerOwnedUnits(player);
            }
        }
        rebuildActiveUnitScanGrid();
    }

    /**
     * Native: MissionScriptRuntime::rebuildActiveUnitScanGrid @0056881E.
     * Fully ported.
     */
    public void rebuildActiveUnitScanGrid() {
        worldMap.visionAndDistance0x58EC0.clearGrid();
        for (Unit unit : worldMap.activeUnits0xA456C) {
            worldMap.visionAndDistance0x58EC0.markUnitScanMask(unit);
            if (!unit.m_pTargetHandle.isSubPosUnknown()) {
                unit.state = UnitActionState.IDLE;
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::initializeActivePlayerUnitGroups @0056866E.
     * Fully ported.
     */
    public void initializeActivePlayerUnitGroups(Player player) {
        for (UnitGroup group : player.unitGroups) {
            initializeActiveUnitGroupMissionTargets(group);
        }
    }

    /**
     * Native: MissionScriptRuntime::initializeInactivePlayerOwnedUnits @005686B1.
     * Fully ported.
     */
    public void initializeInactivePlayerOwnedUnits(Player player) {
        for (Unit unit : player.ownedUnits) {
            initializeInactiveUnitMissionState(unit);
        }
    }

    /**
     * Native: MissionScriptRuntime::initializeActiveUnitGroupMissionTargets @00568523.
     * Fully ported.
     */
    public void initializeActiveUnitGroupMissionTargets(UnitGroup group) {
        Unit headUnit = group.getHeadUnit();
        UnitList candidates = collectFilteredHostileCandidatesAroundUnit(headUnit);
        if (!candidates.isEmpty()) {
            engageMissionGroupAgainstTarget(group, candidates.getFirst());
        }
    }

    /**
     * Native: MissionScriptRuntime::engageMissionGroupAgainstTarget @005684D0.
     * Fully ported.
     */
    public void engageMissionGroupAgainstTarget(UnitGroup group, Unit target) {
        for (Unit unit : group.units) {
            unit.state = UnitActionState.DYING;
            tryBeginMoveToMissionTarget(unit, target);
        }
    }

    /**
     * Native: MissionScriptRuntime::initializeUnitListDefaultMissionState @0056856D.
     * Fully ported.
     */
    public void initializeUnitListDefaultMissionState(UnitList units) {
        for (Unit unit : units) {
            unit.state = UnitActionState.DYING;
            refreshDefaultUnitGameStateFromPath(unit);
        }
    }

    /**
     * Native: MissionScriptRuntime::initializeInactiveUnitMissionState @005685EB.
     * Fully ported.
     */
    public void initializeInactiveUnitMissionState(Unit unit) {
        unit.state = UnitActionState.DYING;
        refreshDefaultUnitGameStateFromPath(unit);
    }

    /**
     * Native: MissionScriptRuntime::tryBeginMoveToMissionTarget @00568089.
     * Fully ported.
     */
    public boolean tryBeginMoveToMissionTarget(Unit unit, Unit target) {
        if (isStationaryFacingOverlappingTarget(unit, target)) {
            beginMoveToMissionTarget(unit, target);
            return true;
        }
        return false;
    }

    /**
     * Native: MissionScriptRuntime::isStationaryFacingOverlappingTarget @00567FF6.
     * Fully ported.
     */
    public boolean isStationaryFacingOverlappingTarget(Unit unit, Unit target) {
        if (!worldMap.hasTokenFootprintOverlap(unit, target) || !unit.m_pTargetHandle.isSubPosUnknown()) {
            refreshDefaultUnitGameStateFromPath(unit);
            return false;
        }
        int direction = CWorldMap.getSubTileDirection8CodeToCellCenter(unit, target.m_pTargetHandle.getCell());
        if ((unit.movementState.facing & 0xFF) == direction) {
            return true;
        }
        refreshDefaultUnitGameStateFromPath(unit);
        return false;
    }

    /**
     * Native: MissionScriptRuntime::beginMoveToMissionTarget @00567710.
     * Fully ported.
     */
    public static boolean beginMoveToMissionTarget(Unit unit, Unit target) {
        unit.actionTarget = target;
        unit.state = UnitActionState.MOVE;
        return true;
    }

    /**
     * Native: MissionScriptRuntime::refreshDefaultUnitGameStateFromPath @005676C0.
     * Fully ported.
     */
    public void refreshDefaultUnitGameStateFromPath(Unit unit) {
        if (!unit.mList1.isEmpty()) {
            if (worldMap.refreshUnitQueuedMissionPathState(unit)) {
                unit.state = UnitActionState.IDLE;
            } else {
                unit.state = UnitActionState.DYING;
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::handleAltDebugCommand @00578910.
     */
    public void handleAltDebugCommand(Player player, int command) {
        if (!player.isCheatCommandEnabled()) {
            return;
        }
        switch (command) {
            case 3 -> toggleTurnTracing();
            case 7 -> sendAltDebugHelp();
            case 8 -> sendLastTurnStatisticsDebug();
            case 0x10 -> toggleSafeMode();
            case 0x13 -> toggleScriptTracing();
            case 0x14 -> sendMissionUnitXpStatisticsDebug(player);
            default -> {
            }
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::handleAltDebugCommand @00578910, debug command `3`.
     */
    private static void toggleTurnTracing() {
        var debugState = Globals.gameServer.debugState;
        if (debugState.turnTracingEnabled == 0) {
            debugState.turnTracingEnabled = 1;
            CServerApp.sendServerChatText("Turn tracing turned on.", null);
        } else {
            debugState.turnTracingEnabled = 0;
            CServerApp.sendServerChatText("Turn tracing turned off.", null);
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::handleAltDebugCommand @00578910, debug command `7`.
     */
    private static void sendAltDebugHelp() {
        CServerApp.sendServerChatText("<Alt h> This help", null);
        CServerApp.sendServerChatText("<Alt q> Safe mode on/off", null);
        CServerApp.sendServerChatText("<Alt t> Script tracing on/off", null);
        CServerApp.sendServerChatText("<Alt i> Last turn full info", null);
        CServerApp.sendServerChatText("<Alt d> Turn timings debug", null);
        CServerApp.sendServerChatText("<Alt u> XP statistics", null);
    }

    /**
     * Native support extracted from MissionScriptRuntime::handleAltDebugCommand @00578910, debug command `0x10`.
     */
    private void toggleSafeMode() {
        if (forceAllMissionGroupsUpdate == 0) {
            forceAllMissionGroupsUpdate = 1;
            CServerApp.sendServerChatText("Safe mode turned on.", null);
        } else {
            forceAllMissionGroupsUpdate = 0;
            CServerApp.sendServerChatText("Safe mode turned off.", null);
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::handleAltDebugCommand @00578910, debug command `0x13`.
     */
    private static void toggleScriptTracing() {
        var debugState = Globals.gameServer.debugState;
        if (debugState.scriptTracingEnabled == 0) {
            debugState.scriptTracingEnabled = 1;
            CServerApp.sendServerChatText("Script tracing turned on.", null);
        } else {
            debugState.scriptTracingEnabled = 0;
            CServerApp.sendServerChatText("Script tracing turned off.", null);
        }
    }

    /**
     * Native: MissionScriptRuntime::sendLastTurnStatisticsDebug @00578CCB.
     */
    public void sendLastTurnStatisticsDebug() {
        var visibilityState = worldMap.unitVisibilityState0x92ECC;
        var timingStats = turnTimingStats;
        int turnCount = missionTurnCounter;

        CServerApp.sendServerChatText("Last Turn Statistics:\n", null);
        CServerApp.sendServerChatText("\n", null);
        CServerApp.sendServerChatText(
                "Turn: %d, Segment: %d, Active - %d/%d.\n".formatted(
                        timingStats.lastTurnElapsedMs,
                        timingStats.lastSegmentElapsedMs,
                        visibilityState.unitsCount1_0x1614,
                        visibilityState.activeUnits0x1610.size()
                ),
                null
        );
        CServerApp.sendServerChatText(
                "AI:   %d, Script:  %d, Activating: %d.\n".formatted(
                        timingStats.lastAiElapsedMs,
                        timingStats.lastScriptElapsedMs,
                        timingStats.lastActivatingElapsedMs
                ),
                null
        );
        CServerApp.sendServerChatText("\n", null);
        CServerApp.sendServerChatText("Average Turn Statistics:\n", null);
        CServerApp.sendServerChatText("\n", null);
        CServerApp.sendServerChatText(
                "Turn: %d, Segment: %d(%d), Scripted - %d.\n".formatted(
                        Integer.divideUnsigned(timingStats.accumulatedTurnElapsedMs, turnCount),
                        Integer.divideUnsigned(timingStats.accumulatedSegmentElapsedMs, turnCount),
                        Integer.divideUnsigned(timingStats.accumulatedSegmentElapsedMs, turnCount << 4),
                        visibilityState.field0x1624
                ),
                null
        );
        CServerApp.sendServerChatText(
                "AI:   %d, Script:  %d, Activating: %d.\n".formatted(
                        Integer.divideUnsigned(timingStats.accumulatedAiElapsedMs, turnCount),
                        Integer.divideUnsigned(timingStats.accumulatedScriptElapsedMs, turnCount),
                        Integer.divideUnsigned(timingStats.accumulatedActivatingElapsedMs, turnCount)
                ),
                null
        );
        CServerApp.sendServerChatText("\n", null);
    }

    /**
     * Native: MissionScriptRuntime::sendMissionUnitXpStatisticsDebug @0057909A.
     */
    public void sendMissionUnitXpStatisticsDebug(Player player) {
        int[] unitCounts = new int[200];
        Unit[] sampleUnits = new Unit[200];
        int[] xpTotals = new int[200];
        int monsterCount = 0;
        int monsterXpTotal = 0;

        CServerApp.sendServerChatText("Mission units stats:\n", null);
        CServerApp.sendServerChatText("\n", null);

        UnitList activeUnits = worldMap.activeUnits0xA456C;
        if (activeUnits != null) {
            for (Unit unit : activeUnits) {
                if (unit.scenarioObjectId >= 0 && unit.scenarioObjectId < 0x2711) {
                    int playerId = (short) player.playerId;
                    int ownerId = (short) unit.owner.playerId;
                    if ((missionDiplomacyState.relationGrid[playerId][ownerId]
                            & PLAYER_RELATION_HOSTILE_MASK) != 0) {
                        if (unit.isHumanoidToken() == 0) {
                            int unitInfoIndex = (unit.key & 0xFFFF);
                            unitCounts[unitInfoIndex]++;
                            sampleUnits[unitInfoIndex] = unit;
                            xpTotals[unitInfoIndex] += unit.price;
                            monsterCount++;
                            monsterXpTotal += unit.price;
                        } else {
                            unitCounts[0]++;
                            sampleUnits[0] = unit;
                            xpTotals[0] += unit.price;
                        }
                    }
                } else {
                    CServerApp.sendServerChatText("Broken unit detected\n", null);
                }
            }
        }

        if (unitCounts[0] != 0) {
            CServerApp.sendServerChatText(
                    "%-3d Humans          ( %-6d xp )\n".formatted(unitCounts[0], xpTotals[0]),
                    null
            );
        }
        for (int unitKey = 1; unitKey < unitCounts.length; unitKey++) {
            if (unitCounts[unitKey] != 0) {
                CServerApp.sendServerChatText(
                        "%-3d %-15s ( %-6d xp )\n".formatted(
                                unitCounts[unitKey],
                                sampleUnits[unitKey].unitInfoLine.name,
                                xpTotals[unitKey]
                        ),
                        null
                );
            }
        }
        CServerApp.sendServerChatText(
                "\n\nTotal monsters: %d, total monster xp: %d.\n".formatted(monsterCount, monsterXpTotal),
                null
        );
        CServerApp.sendServerChatText(
                "\n\nTotal mission: %d, total mission xp: %d.\n".formatted(
                        monsterCount + unitCounts[0],
                        monsterXpTotal + xpTotals[0]
                ),
                null
        );
    }

    /**
     * Native: MissionScriptRuntime::processPlayerMissionGroups @00570087.
     * Fully ported.
     */
    public void processPlayerMissionGroups(Player player) {
        for (UnitGroup group : player.unitGroups) {
            if (forceAllMissionGroupsUpdate == 0) {
                if (group.missionState.isDamagedUnitMissionUpdatePending()) {
                    processMissionGroupState(group);
                }
            } else {
                processMissionGroupState(group);
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::processMissionGroupState @005700F3.
     * Fully ported.
     */
    public void processMissionGroupState(UnitGroup group) {
        updateEmptyGroupScriptState(group);
        switch (group.missionState.scriptRuntimeState) {
            case GROUP_SCRIPT_STATE_DISABLED -> idleGroupUnitsWithoutTargets(group);
            case GROUP_SCRIPT_STATE_IDLE -> processIdleMissionGroup(group);
            case GROUP_SCRIPT_STATE_ADVANCE_TO_MISSION_CELL -> processAdvanceToMissionCellGroup(group);
            case GROUP_SCRIPT_STATE_MOVE_TO_CELL ->
                    processMoveToGroupTargetCell(group, group.missionState.getScriptTargetCell());
            case GROUP_SCRIPT_STATE_ATTACK -> processAttackMissionGroup(group);
            case GROUP_SCRIPT_STATE_COMMAND_CELL_MOVE ->
                    processCommandCellMoveGroup(group, group.missionState.getScriptTargetCell());
            case GROUP_SCRIPT_STATE_COMMAND_CELL_OR_TARGET ->
                    processCommandCellOrTargetGroup(group, group.missionState.getScriptTargetCell());
            case GROUP_SCRIPT_STATE_WANDER_TARGET_CELL -> processWanderTargetCellGroup(group);
            case GROUP_SCRIPT_STATE_PATROL -> processPatrolMissionGroup(group);
            case GROUP_SCRIPT_STATE_PREPARE_DEFAULT -> prepareDefaultMissionGroup(group);
            default -> {
            }
        }
        processMissionGroupPostState(group);
    }

    /**
     * Native: MissionScriptRuntime::updateEmptyGroupScriptState @0057375A.
     * Fully ported.
     */
    public static void updateEmptyGroupScriptState(UnitGroup group) {
        group.missionState.disableEmptyGroupScriptState(group.units.size());
    }

    /**
     * Native support extracted from MissionScriptRuntime::processMissionGroupState @005700F3 state `0` via
     * MissionScriptRuntime::processIdleMissionUnit @0056FE34.
     */
    public void processIdleMissionGroup(UnitGroup group) {
        for (Unit unit : group.units) {
            processIdleMissionUnit(unit);
        }
    }

    /**
     * Native: MissionScriptRuntime::processIdleMissionUnit @0056FE34.
     * Fully ported.
     */
    public void processIdleMissionUnit(Unit unit) {
        updateUnitMissionAction(unit);
    }

    /**
     * Native: MissionScriptRuntime::idleGroupUnitsWithoutTargets @00573159.
     * Fully ported.
     */
    public void idleGroupUnitsWithoutTargets(UnitGroup group) {
        for (Unit unit : group.units) {
            setUnitDefaultMissionAction(unit, 0);
        }
    }

    /**
     * Native: MissionScriptRuntime::prepareDefaultMissionGroup @0057311C.
     * Fully ported.
     */
    public void prepareDefaultMissionGroup(UnitGroup group) {
        for (Unit unit : group.units) {
            prepareUnitDefaultMissionAction(unit);
        }
    }

    /**
     * Native: MissionScriptRuntime::processAttackMissionGroup @0057309D.
     * Fully ported.
     */
    public void processAttackMissionGroup(UnitGroup group) {
        rebuildMissionTargetCandidates(group);
        assignClosestAttackTargets(group);
        for (Unit unit : group.units) {
            Unit target = unit.missionRuntimeState.assignedTargetUnit;
            if (target == null) {
                prepareUnitDefaultMissionAction(unit);
            } else {
                engageMissionTarget(unit, target);
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::processMoveToGroupTargetCell @00572D57.
     * Fully ported.
     */
    public void processMoveToGroupTargetCell(UnitGroup group, int targetCell) {
        rebuildMissionTargetCandidates(group);
        assignClosestMissionTargets(group);
        for (Unit unit : group.units) {
            Unit target = unit.missionRuntimeState.assignedTargetUnit;
            if (target == null) {
                if (unit.m_pTargetHandle.getCell() == targetCell && unit.m_pTargetHandle.isSubPosUnknown()) {
                    setUnitDefaultMissionAction(unit, 1);
                } else {
                    queueMissionMoveToCell(unit, targetCell);
                }
            } else {
                engageMissionTarget(unit, target);
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::processAdvanceToMissionCellGroup @0057275F.
     * Fully ported.
     */
    public void processAdvanceToMissionCellGroup(UnitGroup group) {
        refreshUnitGroupCellCenterAndFootprintRange(group);
        rebuildMissionTargetCandidates(group);
        pruneMissionTargetCandidatesToGuardRange(group);
        assignClosestMissionTargets(group);
        Unit firstUnit = group.getHeadUnit();
        updateAdvanceToMissionCellPresenceState(group, firstUnit != null);
        if (firstUnit == null) {
            return;
        }
        for (Unit unit : group.units) {
            Unit target = unit.missionRuntimeState.assignedTargetUnit;
            if (target == null) {
                if (unit.m_pTargetHandle.getCell() == unit.missionRuntimeState.missionScriptCell
                        && unit.m_pTargetHandle.isSubPosUnknown()) {
                    setUnitDefaultMissionAction(unit, 1);
                } else {
                    queueMissionMoveToCell(unit, unit.missionRuntimeState.missionScriptCell);
                }
            } else {
                engageMissionTarget(unit, target);
            }
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::processAdvanceToMissionCellGroup @0057275F,
     * MissionScriptRuntime::randomizeCurrentGuardRangeFromMirror @0057272C, and
     * MissionScriptRuntime::randomizeExpandedCurrentGuardRangeFromMirror @005726F7.
     */
    public void updateAdvanceToMissionCellPresenceState(UnitGroup group, boolean hasHeadUnit) {
        if (!hasHeadUnit) {
            if (!group.missionState.hasPriorHeadUnitPresence()) {
                group.missionState.incrementPresenceScanCounter();
            } else {
                randomizeCurrentGuardRangeFromMirror(group);
                group.missionState.clearPresenceScanCounter();
            }
            group.missionState.setPriorHeadUnitPresence(false);
            return;
        }
        if (!group.missionState.hasPriorHeadUnitPresence()) {
            randomizeExpandedCurrentGuardRangeFromMirror(group);
            group.missionState.clearPresenceScanCounter();
        } else {
            group.missionState.incrementPresenceScanCounter();
        }
        group.missionState.setPriorHeadUnitPresence(true);
    }

    /**
     * Native: MissionScriptRuntime::randomizeCurrentGuardRangeFromMirror @0057272C.
     * Fully ported.
     */
    public void randomizeCurrentGuardRangeFromMirror(UnitGroup group) {
        int guardRange = group.missionState.getScenarioGroupGuardRangeMirror() + randomInclusiveRange(-1, 1);
        group.missionState.setScenarioGroupCurrentGuardRange(guardRange);
    }

    /**
     * Native: MissionScriptRuntime::randomizeExpandedCurrentGuardRangeFromMirror @005726F7.
     * Fully ported.
     */
    public void randomizeExpandedCurrentGuardRangeFromMirror(UnitGroup group) {
        int guardRange = group.missionState.getScenarioGroupGuardRangeMirror() + 4 + randomInclusiveRange(-1, 1);
        group.missionState.setScenarioGroupCurrentGuardRange(guardRange);
    }

    /**
     * Native: MissionScriptRuntime::processCommandCellMoveGroup @00573198.
     * Fully ported.
     */
    public void processCommandCellMoveGroup(UnitGroup group, int targetCell) {
        refreshScenarioGroupCenterAndFootprintRange(group);
        for (Unit unit : group.units) {
            if (unit.m_pTargetHandle.getCell() == unit.missionRuntimeState.commandCell) {
                if (unit.m_pTargetHandle.isSubPosUnknown() && unit.missionRuntimeState.unitScriptState == 0) {
                    reenterScenarioMissionEntryUnit(unit);
                    unit.movementState.missionReentryPending = 0;
                }
            } else if (unit.movementState.missionReentryPending != 0
                    && unit.movementState.missionReentryCell == unit.m_pTargetHandle.getCell()) {
                reenterScenarioMissionEntryUnit(unit);
                unit.movementState.missionReentryPending = 0;
            }
            if (unit.missionRuntimeState.unitScriptState == 0) {
                if (group.missionState.hasMissionScriptSpeedOverride()) {
                    unit.missionRuntimeState.missionSpeedOverrideActiveFlag = 1;
                }
                queueMissionMoveToCell(unit, unit.missionRuntimeState.commandCell);
                if (!worldMap.hasClearLayer2Footprint(unit, targetCell)
                        && unit.missionRuntimeState.commandCell == (targetCell & 0xFFFF)
                        && cellChebyshevDistance(unit.m_pTargetHandle.getCell(), targetCell) < 2) {
                    engageNearestTarget(unit);
                }
            } else {
                setUnitDefaultMissionAction(unit, 0);
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::reenterScenarioMissionEntryUnit @0056DBC6.
     * Fully ported.
     */
    public void reenterScenarioMissionEntryUnit(Unit unit) {
        unit.resetScenarioMissionUnitRuntimeContext(this);
        unit.initializeScenarioMissionEntryUnit(this);
        unit.missionRuntimeState.unitScriptState = 1;
    }

    /**
     * Native: MissionScriptRuntime::restartScenarioMissionEntryUnit @0056DC24.
     * Fully ported.
     */
    public void restartScenarioMissionEntryUnit(Unit unit) {
        unit.resetScenarioMissionUnitRuntimeContext(this);
        unit.initializeScenarioMissionEntryUnit(this);
    }

    /**
     * Native: MissionScriptRuntime::processCommandCellOrTargetGroup @00572E1E.
     * Fully ported.
     */
    public void processCommandCellOrTargetGroup(UnitGroup group, int targetCell) {
        rebuildMissionTargetCandidates(group);
        if (primaryCandidateUnits.isEmpty()) {
            processCommandCellMoveGroup(group, targetCell);
            return;
        }
        assignClosestMissionTargets(group);
        for (Unit unit : group.units) {
            Unit target = unit.missionRuntimeState.assignedTargetUnit;
            if (target == null) {
                setUnitDefaultMissionAction(unit, 1);
            } else {
                engageMissionTarget(unit, target);
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::processWanderTargetCellGroup @00572EC4.
     * Fully ported.
     */
    public void processWanderTargetCellGroup(UnitGroup group) {
        int targetCell = group.missionState.getScriptTargetCell();
        int maxDistance = 0;
        for (Unit unit : group.units) {
            maxDistance = Math.max(maxDistance, cellChebyshevDistance(unit.m_pTargetHandle.getCell(), targetCell));
        }
        if (maxDistance < 10 || group.missionState.getScriptTargetCellAge() > 0x32) {
            targetCell = chooseWanderTargetCell(targetCell);
            group.missionState.setScriptTargetCell(targetCell);
            group.missionState.clearScriptTargetCellAge();
        }
        processCommandCellOrTargetGroup(group, targetCell);
        group.missionState.incrementScriptTargetCellAge();
    }

    /**
     * Native support extracted from MissionScriptRuntime::processWanderTargetCellGroup @00572EC4 random target-cell branch.
     * Fully ported.
     */
    public int chooseWanderTargetCell(int sourceCell) {
        int candidateX;
        int candidateY;
        do {
            int direction = randomInclusiveRange(0, 7);
            candidateX = (sourceCell & 0xFF) + worldMap.neighborStepTable0x58E88.dx[direction] * 0x14;
            candidateY = ((sourceCell >>> 8) & 0xFF) + worldMap.neighborStepTable0x58E88.dy[direction] * 0x14;
        } while (!worldMap.isFullyInside(candidateX, candidateY));
        return ((candidateY & 0xFF) << 8) | (candidateX & 0xFF);
    }

    /**
     * Native: MissionScriptRuntime::processPatrolMissionGroup @005728FA.
     * Fully ported.
     */
    public void processPatrolMissionGroup(UnitGroup group) {
        refreshUnitGroupCellCenterAndFootprintRange(group);
        rebuildMissionTargetCandidates(group);
        pruneMissionTargetCandidatesToGuardRange(group);
        assignClosestMissionTargets(group);
        Unit firstUnit = group.getHeadUnit();
        updateAdvanceToMissionCellPresenceState(group, firstUnit != null);
        if (firstUnit == null) {
            return;
        }
        for (Unit unit : group.units) {
            if (shouldRetreatToEngagementProjection(unit)) {
                queueMissionMoveToCell(unit, unit.missionRuntimeState.engagementProjectedCell);
                continue;
            }
            if (recentEngagementProjectionAge(unit) < 10) {
                queueMissionMoveToCell(unit, unit.missionRuntimeState.engagementProjectedCell);
                continue;
            }
            Unit target = unit.missionRuntimeState.assignedTargetUnit;
            if (target == null) {
                if (unit.m_nHP == unit.m_nMaxHP || lastEngagementTickAge(unit) > 9) {
                    if (unit.missionRuntimeState.patrolRepositionTicks == 0) {
                        if (unit.m_pTargetHandle.getCell() == unit.missionRuntimeState.missionScriptCell
                                && unit.m_pTargetHandle.isSubPosUnknown()) {
                            setUnitDefaultMissionAction(unit, 1);
                        } else {
                            queueMissionMoveToCell(unit, unit.missionRuntimeState.missionScriptCell);
                        }
                    } else {
                        unit.missionRuntimeState.patrolRepositionTicks =
                                (unit.missionRuntimeState.patrolRepositionTicks + 1) & 0xFF;
                        queueMissionMoveToCell(unit, unit.missionRuntimeState.patrolRepositionCell);
                        if ((unit.missionRuntimeState.patrolRepositionTicks & 0xFF) > 10) {
                            unit.missionRuntimeState.patrolRepositionTicks = 0;
                        }
                    }
                } else {
                    queueMissionMoveToCell(unit, unit.missionRuntimeState.engagementProjectedCell);
                }
            } else {
                engageMissionTarget(unit, target);
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::selectorSlotMatchesUnit @0057337B.
     * Fully ported.
     */
    public boolean selectorSlotMatchesUnit(Unit unit, int selectorIndex) {
        SelectorSlot selector = selectorSlots[selectorIndex];
        int x = unit.m_pTargetHandle.getX() & 0xFF;
        int y = unit.m_pTargetHandle.getY() & 0xFF;
        if (selector.right == 0) {
            return chebyshevDistance(x, y, selector.xOrLeft, selector.yOrTop) < selector.radius;
        }
        return selector.xOrLeft < x
                && selector.yOrTop < y
                && x < selector.right
                && y < selector.bottom;
    }

    /**
     * Native: MissionScriptRuntime::countGroupSelectorMatches @00573486.
     * Fully ported.
     */
    public int countGroupSelectorMatches(UnitGroup group, int selectorIndex) {
        int count = 0;
        for (Unit unit : group.units) {
            if (selectorSlotMatchesUnit(unit, selectorIndex)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Native: MissionScriptRuntime::countPlayerSelectorMatches @005734DE.
     * Fully ported.
     */
    public int countPlayerSelectorMatches(Player player, int selectorIndex) {
        int count = 0;
        for (UnitGroup group : player.unitGroups) {
            for (Unit unit : group.units) {
                if (selectorSlotMatchesUnit(unit, selectorIndex)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Native: MissionScriptRuntime::refreshAllSelectorMatchCounts @005735A6.
     * Fully ported.
     */
    public void refreshAllSelectorMatchCounts() {
        for (int selectorIndex = 0; selectorIndex < selectorSlotCount; selectorIndex++) {
            refreshSelectorMatchCount(selectorIndex);
        }
    }

    /**
     * Native: MissionScriptRuntime::refreshSelectorMatchCount @005735EC.
     * Fully ported.
     */
    public void refreshSelectorMatchCount(int selectorIndex) {
        SelectorSlot selector = selectorSlots[selectorIndex];
        int count = 0;
        if (selector.enabled == 1) {
            if (selector.unit != null) {
                count = selectorSlotMatchesUnit(selector.unit, selectorIndex) ? 1 : 0;
            } else if (selector.group != null) {
                count = countGroupSelectorMatches(selector.group, selectorIndex);
            } else if (selector.player != null) {
                count = countPlayerSelectorMatches(selector.player, selectorIndex);
            }
        }
        selectorMatchCounts[selectorIndex] = count;
        selectorState[selectorIndex] = 0;
    }

    /**
     * Native support extracted from MissionScriptRuntime::processPatrolMissionGroup @005728FA wound retreat branch.
     * Fully ported.
     */
    public static boolean shouldRetreatToEngagementProjection(Unit unit) {
        if (unit.m_nHP == unit.m_nMaxHP) {
            return false;
        }
        return recentEngagementProjectionAge(unit) <= 0x1D;
    }

    /**
     * Native support extracted from MissionScriptRuntime::processPatrolMissionGroup @005728FA.
     * Fully ported.
     */
    public static int recentEngagementProjectionAge(Unit unit) {
        return Math.abs((unit.missionRuntimeState.w1 & 0xFFFF) - (Globals.gameServer.someValue & 0xFFFF));
    }

    /**
     * Native support extracted from MissionScriptRuntime::processPatrolMissionGroup @005728FA.
     * Fully ported.
     */
    public static int lastEngagementTickAge(Unit unit) {
        return Math.abs(unit.missionRuntimeState.lastEngagementTick - Globals.gameServer.serverLoopCounter);
    }

    /**
     * Native: MissionScriptRuntime::assignClosestMissionTargets @0057240B.
     * Fully ported.
     */
    public void assignClosestMissionTargets(UnitGroup group) {
        if ((group.units.size() & 0xFF) == 0 || (primaryCandidateUnits.size() & 0xFF) == 0) {
            for (Unit unit : group.units) {
                unit.missionRuntimeState.assignedTargetUnit = null;
            }
            return;
        }
        appendMissionTargetAssignmentGroupDTO(group);
        for (Unit unit : group.units) {
            unit.missionRuntimeState.assignedTargetUnit = findNearestMissionCandidateTarget(unit, false);
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::assignClosestMissionTargets @0057240B CList<EventDTO>::AddTail copy.
     * Fully ported.
     */
    public void appendMissionTargetAssignmentGroupDTO(UnitGroup group) {
        scratchMissionUnitEventDTO.eventCode = MISSION_UNIT_EVENT_TARGET_ASSIGNMENT;
        scratchMissionUnitEventDTO.runtimeSource = group;
        pendingMissionUnitEventDTOs.add(scratchMissionUnitEventDTO.copy());
    }

    /**
     * Native: MissionScriptRuntime::assignClosestAttackTargets @005725A8.
     * Fully ported.
     */
    public void assignClosestAttackTargets(UnitGroup group) {
        if ((group.units.size() & 0xFF) == 0 || (primaryCandidateUnits.size() & 0xFF) == 0) {
            for (Unit unit : group.units) {
                unit.missionRuntimeState.assignedTargetUnit = null;
            }
            return;
        }
        for (Unit unit : group.units) {
            unit.missionRuntimeState.assignedTargetUnit = findNearestMissionCandidateTarget(unit, true);
        }
    }

    /**
     * Native: MissionScriptRuntime::buildMissionTargetScoreMatrix @005722E1.
     * Fully ported.
     */
    public void buildMissionTargetScoreMatrix(UnitGroup group) {
        if ((group.units.size() & 0xFF) == 0 || (primaryCandidateUnits.size() & 0xFF) == 0) {
            for (Unit unit : group.units) {
                unit.missionRuntimeState.assignedTargetUnit = null;
            }
            return;
        }
        int unitIndex = 1;
        for (Unit unit : group.units) {
            int candidateIndex = 1;
            for (Unit candidate : primaryCandidateUnits) {
                missionTargetScoreMatrix[unitIndex][candidateIndex] =
                        computeMissionTargetScore(unit, candidate, false);
                candidateIndex++;
            }
            unitIndex++;
        }
        finalizeMissionTargetScoreMatrix();
    }

    /**
     * Native: MissionScriptRuntime::finalizeMissionTargetScoreMatrix @005710CD.
     * Fully ported.
     */
    public void finalizeMissionTargetScoreMatrix() {
    }

    /**
     * Native: MissionScriptRuntime::rebuildMissionTargetCandidates @00571C4D.
     * Fully ported.
     */
    public void rebuildMissionTargetCandidates(UnitGroup group) {
        primaryCandidateUnits.clear();
        Unit firstUnit = group.getHeadUnit();
        if (firstUnit == null) {
            return;
        }
        worldMap.visionAndDistance0x58EC0.clearVisibilityMarkers();
        for (Unit unit : group.units) {
            worldMap.visionAndDistance0x58EC0.scanUnitVisibility(unit, unit.m_pTargetHandle.getCell());
            refreshEngagementVisibilityMarker(unit);
        }
        if (worldMap.activeUnits0xA456C != null) {
            for (Unit unit : worldMap.activeUnits0xA456C) {
                if (worldMap.visionAndDistance0x58EC0.visibilityMarkers[unit.m_pTargetHandle.getCell() & 0xFFFF] != 0) {
                    primaryCandidateUnits.add(unit);
                }
            }
        }
        filterCandidateUnits(firstUnit, primaryCandidateUnits, 0);
        moveDeadCandidatesToFallbackList();
        promoteFallbackCandidatesIfPrimaryEmpty();
    }

    /**
     * Native: MissionScriptRuntime::rebuildMissionTargetCandidates @00569FD4.
     * Fully ported.
     */
    public void rebuildMissionTargetCandidates(Unit unit) {
        rebuildMissionTargetCandidatesFromCell(unit, unit.m_pTargetHandle.getCell());
    }

    /**
     * Native: MissionScriptRuntime::rebuildMissionTargetCandidatesFromCell @005720CC.
     * Fully ported.
     */
    public void rebuildMissionTargetCandidatesFromCell(Unit unit, int scanCell) {
        primaryCandidateUnits.clear();
        worldMap.visionAndDistance0x58EC0.clearVisibilityMarkers();
        worldMap.visionAndDistance0x58EC0.scanUnitVisibility(unit, scanCell);
        refreshEngagementVisibilityMarker(unit);
        if (worldMap.activeUnits0xA456C != null) {
            for (Unit candidate : worldMap.activeUnits0xA456C) {
                if (worldMap.visionAndDistance0x58EC0.visibilityMarkers[candidate.m_pTargetHandle.getCell() & 0xFFFF] != 0) {
                    primaryCandidateUnits.add(candidate);
                }
            }
        }
        filterCandidateUnits(unit, primaryCandidateUnits, 0);
        moveDeadCandidatesToFallbackList();
        promoteFallbackCandidatesIfPrimaryEmpty();
    }

    /**
     * Native: MissionScriptRuntime::rebuildAdjacentMissionTargetCandidates @00569F6F.
     * Fully ported.
     */
    public void rebuildAdjacentMissionTargetCandidates(Unit unit) {
        turnPerfMonitor.queryCounter();
        rebuildAdjacentCellOccupantMissionCandidates(unit);
        turnPerfMonitor.finishElapsedMilliseconds();
        turnPerfMonitor.queryCounter();
        appendAdjacentActiveMissionCandidates(unit);
        turnPerfMonitor.finishElapsedMilliseconds();
    }

    /**
     * Native: MissionScriptRuntime::rebuildAdjacentCellOccupantMissionCandidates @00569CE2.
     * Fully ported.
     */
    public void rebuildAdjacentCellOccupantMissionCandidates(Unit unit) {
        primaryCandidateUnits.clear();
        int unitY = unit.m_pTargetHandle.getY() & 0xFF;
        int unitX = unit.m_pTargetHandle.getX() & 0xFF;
        int minScanY = unitY - 1;
        int maxScanY = unitY + 1;
        int minScanX = unitX - 1;
        int maxScanX = unitX + 1;
        if (minScanY < 8) {
            minScanY = 8;
        }
        if ((worldMap.rect0x58EB8.rb.y & 0xFF) < maxScanY) {
            maxScanY = worldMap.rect0x58EB8.rb.y & 0xFF;
        }
        if (minScanX < 8) {
            minScanX = 8;
        }
        if ((worldMap.rect0x58EB8.rb.x & 0xFF) < maxScanX) {
            maxScanX = worldMap.rect0x58EB8.rb.x & 0xFF;
        }
        for (int y = minScanY; y <= maxScanY; y++) {
            for (int x = minScanX; x <= maxScanX; x++) {
                int cell = ((y & 0xFF) << 8) | (x & 0xFF);
                appendAdjacentCellOccupantMissionCandidate(worldMap.getGroundUnitAtCell(cell));
                appendAdjacentCellOccupantMissionCandidate(worldMap.getAirUnitAtCell(cell));
            }
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::rebuildAdjacentCellOccupantMissionCandidates @00569CE2.
     * Fully ported.
     */
    private void appendAdjacentCellOccupantMissionCandidate(Unit unit) {
        if (unit != null && primaryCandidateUnits.findByTokenId(unit.idFull) == null) {
            primaryCandidateUnits.add(unit);
        }
    }

    /**
     * Native: MissionScriptRuntime::appendAdjacentActiveMissionCandidates @00569ED8.
     * Fully ported.
     */
    public void appendAdjacentActiveMissionCandidates(Unit unit) {
        if (worldMap.activeUnits0xA456C == null) {
            return;
        }
        for (Unit candidate : worldMap.activeUnits0xA456C) {
            if ((unit.m_pTargetHandle.chebyshevDistanceByXY(candidate.m_pTargetHandle) & 0xFF) < 2) {
                primaryCandidateUnits.add(candidate);
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::rebuildVisibleMapCellMissionTargetCandidates @0057184F.
     * Fully ported.
     */
    public void rebuildVisibleMapCellMissionTargetCandidates(UnitGroup group) {
        primaryCandidateUnits.clear();
        Unit firstUnit = group.getHeadUnit();
        if (firstUnit == null) {
            return;
        }
        int maxSightRange = 0;
        int minX = 0xFF;
        int maxX = 0;
        int minY = 0xFF;
        int maxY = 0;
        worldMap.visionAndDistance0x58EC0.clearVisibilityMarkers();
        for (Unit unit : group.units) {
            if (unit.owner.isActive != 0) {
                int sightRange = unit.sightRange & 0xFF;
                if (maxSightRange < sightRange) {
                    maxSightRange = sightRange;
                }
                int x = unit.m_pTargetHandle.getX() & 0xFF;
                int y = unit.m_pTargetHandle.getY() & 0xFF;
                if (maxX < x) {
                    maxX = x;
                }
                if (x < minX) {
                    minX = x;
                }
                if (maxY < y) {
                    maxY = y;
                }
                if (y < minY) {
                    minY = y;
                }
            }
            worldMap.visionAndDistance0x58EC0.scanUnitVisibility(unit, unit.m_pTargetHandle.getCell());
            refreshEngagementVisibilityMarker(unit);
        }
        int minScanY = minY - maxSightRange * 2;
        int maxScanY = maxY + maxSightRange * 2;
        int minScanX = minX - maxSightRange * 2;
        int maxScanX = maxX + maxSightRange * 2;
        if (minScanY < 8) {
            minScanY = 8;
        }
        if ((worldMap.rect0x58EB8.rb.y & 0xFF) < maxScanY) {
            maxScanY = worldMap.rect0x58EB8.rb.y & 0xFF;
        }
        if (minScanX < 8) {
            minScanX = 8;
        }
        if ((worldMap.rect0x58EB8.rb.x & 0xFF) < maxScanX) {
            maxScanX = worldMap.rect0x58EB8.rb.x & 0xFF;
        }
        for (int y = minScanY; y <= maxScanY; y++) {
            for (int x = minScanX; x <= maxScanX; x++) {
                int cell = ((y & 0xFF) << 8) | (x & 0xFF);
                if (worldMap.visionAndDistance0x58EC0.visibilityMarkers[cell] != 0) {
                    appendVisibleMapCellCandidate(worldMap.getGroundUnitAtCell(cell));
                    appendVisibleMapCellCandidate(worldMap.getAirUnitAtCell(cell));
                }
            }
        }
        filterCandidateUnits(firstUnit, primaryCandidateUnits, 0);
        moveDeadCandidatesToFallbackList();
        promoteFallbackCandidatesIfPrimaryEmpty();
    }

    /**
     * Native support extracted from MissionScriptRuntime::rebuildVisibleMapCellMissionTargetCandidates @0057184F.
     * Preserves the native CWorldMap lookup result check before UnitList::FindByTokenId.
     * Fully ported.
     */
    private void appendVisibleMapCellCandidate(Unit unit) {
        if (unit != null && primaryCandidateUnits.findByTokenId(unit.idFull) == null) {
            primaryCandidateUnits.add(unit);
        }
    }

    /**
     * Native: MissionScriptRuntime::rebuildPreferredSpellVisibleCandidates @00571E9B.
     * Fully ported.
     */
    public void rebuildPreferredSpellVisibleCandidates(UnitGroup group) {
        primaryCandidateUnits.clear();
        Unit firstUnit = group.getHeadUnit();
        if (firstUnit == null) {
            return;
        }
        worldMap.visionAndDistance0x58EC0.clearVisibilityMarkers();
        for (Unit unit : group.units) {
            worldMap.visionAndDistance0x58EC0.scanUnitVisibility(unit, unit.m_pTargetHandle.getCell());
            refreshEngagementVisibilityMarker(unit);
        }
        if (worldMap.activeUnits0xA456C != null) {
            for (Unit unit : worldMap.activeUnits0xA456C) {
                if (worldMap.visionAndDistance0x58EC0.visibilityMarkers[unit.m_pTargetHandle.getCell() & 0xFFFF] != 0) {
                    primaryCandidateUnits.add(unit);
                }
            }
        }
        moveDeadCandidatesToFallbackList();
        promoteFallbackCandidatesIfPrimaryEmpty();
    }

    /**
     * Native support extracted from MissionScriptRuntime::rebuildMissionTargetCandidates @00571C4D, @00569FD4,
     * MissionScriptRuntime::rebuildPreferredSpellVisibleCandidates @00571E9B, and
     * MissionScriptRuntime::rebuildMissionTargetCandidatesFromCell @005720CC.
     * Fully ported.
     */
    public void refreshEngagementVisibilityMarker(Unit unit) {
        if (unit.owner.isActive != 0 && unit.missionRuntimeState.cell != 0) {
            int cell = unit.missionRuntimeState.cell & 0xFFFF;
            worldMap.visionAndDistance0x58EC0.visibilityMarkers[cell]++;
            unit.missionRuntimeState.engagementCellRepeatCount = (unit.missionRuntimeState.engagementCellRepeatCount + 1) & 0xFF;
            if (unit.missionRuntimeState.engagementCellRepeatCount > 0x14) {
                unit.missionRuntimeState.cell = 0;
            }
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::rebuildMissionTargetCandidates @00571C4D, @00569FD4,
     * MissionScriptRuntime::rebuildPreferredSpellVisibleCandidates @00571E9B, and
     * MissionScriptRuntime::rebuildMissionTargetCandidatesFromCell @005720CC.
     * Fully ported.
     */
    public void promoteFallbackCandidatesIfPrimaryEmpty() {
        if (primaryCandidateUnits.isEmpty() && !fallbackCandidateUnits.isEmpty()) {
            primaryCandidateUnits.addAll(fallbackCandidateUnits);
            fallbackCandidateUnits.clear();
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::pruneMissionTargetCandidatesToGuardRange @00567E92.
     * Fully ported.
     */
    public void pruneMissionTargetCandidatesToGuardRange(UnitGroup group) {
        pruneMissionTargetCandidatesToGuardRange(
                group.missionState.getScenarioGroupCenterCell(),
                group.missionState.getScenarioGroupGuardRange()
        );
    }

    /**
     * Native: MissionScriptRuntime::pruneMissionTargetCandidatesToGuardRange @00567E92.
     * Fully ported.
     */
    public void pruneMissionTargetCandidatesToGuardRange(int centerCell, int guardRange) {
        for (int index = 0; index < primaryCandidateUnits.size(); ) {
            Unit candidate = primaryCandidateUnits.get(index);
            if (guardRange < cellChebyshevDistance(centerCell, candidate.m_pTargetHandle.getCell())) {
                primaryCandidateUnits.remove(index);
            } else {
                index++;
            }
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::assignClosestMissionTargets @0057240B and
     * MissionScriptRuntime::assignClosestAttackTargets @005725A8.
     */
    public Unit findNearestMissionCandidateTarget(Unit unit, boolean attackTargetsOnly) {
        Unit selected = null;
        int selectedScore = 0xFFFFFF;
        for (Unit candidate : primaryCandidateUnits) {
            int score = computeMissionTargetScore(unit, candidate, attackTargetsOnly);
            if (score < selectedScore) {
                selected = candidate;
                selectedScore = score;
            }
        }
        return selectedScore == 0xFFFFFF ? null : selected;
    }

    /**
     * Native support extracted from MissionScriptRuntime::computeMissionTargetScore @005711EA and
     * MissionScriptRuntime::computeAdjacentAttackTargetScore @005713F8.
     * Fully ported.
     */
    public int computeMissionTargetScore(Unit unit, Unit candidate, boolean attackTargetsOnly) {
        int distance = cellChebyshevDistance(unit.m_pTargetHandle.getCell(), candidate.m_pTargetHandle.getCell());
        int candidateType = candidate.getMovementType() & 0xFF;
        if (candidateType == 1 && getCastRangeForFirstCastableSpellOrFallback(candidate) > 1) {
            candidateType = 0;
        }
        int relationMode;
        int unitCastRange = getCastRangeForFirstCastableSpellOrFallback(unit) & 0xFF;
        if (unitCastRange < 2) {
            relationMode = targetRelationModeTable[unit.getMovementType()][candidateType] & 0xFF;
        } else {
            relationMode = targetRelationModeTable[0][candidateType] & 0xFF;
            distance = unitCastRange < distance ? (distance - unitCastRange) + 1 : 1;
        }
        int facingDelta = worldMap.getFacingAngularDistance8(
                unit.movementState.facing,
                worldMap.getDirection8Code(unit, candidate)
        );
        int score = distance * 0x100 + facingDelta;
        if (relationMode == 0) {
            return 0xFFFFFF;
        }
        if (attackTargetsOnly) {
            if (distance >= 2) {
                return 0xFFFFFF;
            }
            if (relationMode == 1) {
                score *= 2;
            } else if (relationMode == 4) {
                score >>= 1;
            }
            return score;
        }
        if (relationMode == 1) {
            if ((unit.getTokenSizeVirtual() & 0xFF) < 2 && (short) unit.m_nMind > 0x0E) {
                score += score >> 1;
            }
        } else if (relationMode == 4 && (unit.getTokenSizeVirtual() & 0xFF) < 2 && (short) unit.m_nMind > 0x0E) {
            score -= score >> 2;
        }
        if ((candidate.effectKeyFlags & 0x40000) != 0) {
            score += 0x7F;
            if (score < 1) {
                score = 1;
            }
        }
        return score;
    }

    /**
     * Native support extracted from MissionScriptRuntime::engageNearestTarget @0056A1FB.
     * Fully ported.
     */
    public Unit findNearestHostileTarget(Unit unit) {
        rebuildMissionTargetCandidates(unit);
        Unit selected = findNearestEngagementCandidate(unit, primaryCandidateUnits, true);
        if (selected == null && !fallbackCandidateUnits.isEmpty()) {
            selected = findNearestEngagementCandidate(unit, fallbackCandidateUnits, false);
        }
        return selected;
    }

    /**
     * Native: MissionScriptRuntime::engageClosestCandidateNearAnchorPreferringBuildings @0056A73B.
     * Fully ported.
     */
    public void engageClosestCandidateNearAnchorPreferringBuildings(Unit unit, Unit scanAnchor) {
        UnitList candidates = collectFilteredHostileCandidatesAroundUnit(scanAnchor);
        if (candidates.isEmpty()) {
            setUnitDefaultMissionAction(unit, 0);
            return;
        }

        Unit selectedBuilding = null;
        int selectedBuildingRange = 0xFF;
        for (Unit candidate : candidates) {
            if ((candidate.getMovementType() & 0xFF) == 3) {
                int range = worldMap.getRangeInTiles(unit, candidate) & 0xFF;
                if (selectedBuilding == null || range < selectedBuildingRange) {
                    selectedBuilding = candidate;
                    selectedBuildingRange = range;
                }
            }
        }
        if (selectedBuilding != null) {
            engageMissionTarget(unit, selectedBuilding);
            return;
        }

        Unit selectedUnit = null;
        int selectedUnitRange = 0xFF;
        for (Unit candidate : candidates) {
            int range = worldMap.getRangeInTiles(unit, candidate) & 0xFF;
            if (range < selectedUnitRange) {
                selectedUnit = candidate;
                selectedUnitRange = range;
            }
        }
        if (selectedUnit != null) {
            engageMissionTarget(unit, selectedUnit);
        }
    }

    /**
     * Native: MissionScriptRuntime::tryHealTargetOrEngageClosestCandidateNearAnchor @00569968.
     * Fully ported.
     */
    public void tryHealTargetOrEngageClosestCandidateNearAnchor(Unit caster, Unit target) {
        int spellId = 0;
        boolean spellQueued = false;
        if ((short) caster.m_wRegenStore == 0) {
            if ((short) target.m_nHP < (short) target.m_nMaxHP) {
                spellId = SpellId.HEAL.id;
            }
        } else if ((short) caster.m_wRegenStore < (short) caster.m_nMaxMP + 3
                && (short) target.m_nHP < ((short) target.m_nMaxHP >> 1)) {
            spellId = SpellId.HEAL.id;
        }
        if (spellId != 0) {
            Spell spell = caster.spellbook.find(spellId);
            if (spell != null && (short) spell.manaCost <= (short) caster.m_nMP) {
                caster.missionRuntimeState.virtualCasterQueuedFlag = 1;
                queueTargetedSpellCast(caster, target, spell);
                spellQueued = true;
            }
        }
        if (!spellQueued) {
            engageClosestCandidateNearAnchorPreferringBuildings(caster, target);
        }
    }

    /**
     * Native: MissionScriptRuntime::trySupportSpellActionOrEngageClosestCandidateNearAnchor @00569A80.
     * Fully ported.
     */
    public void trySupportSpellActionOrEngageClosestCandidateNearAnchor(Unit caster, Unit target) {
        int spellId = 0;
        boolean spellQueued = false;
        if ((short) target.m_nHP < (short) target.m_nMaxHP * 2) {
            spellId = SpellId.HEAL.id;
        } else if ((short) caster.m_nMP == (short) caster.m_nMaxMP) {
            spellId = SpellId.SHIELD.id;
        }
        if (spellId != 0) {
            caster.missionRuntimeState.spellTargetToken = caster.missionRuntimeState.targetToken;
            Spell spell = caster.spellbook.find(spellId);
            caster.missionRuntimeState.spell = spell;
            if ((short) spell.manaCost <= (short) caster.m_nMP) {
                beginTargetedSpellMissionAction(caster);
                spellQueued = true;
            }
        }
        if (!spellQueued) {
            engageClosestCandidateNearAnchorPreferringBuildings(caster, target);
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::engageNearestTarget @0056A1FB.
     * Fully ported.
     */
    public Unit findNearestEngagementCandidate(Unit unit, UnitList candidates, boolean scorePrimaryCandidates) {
        Unit selected = null;
        int selectedFacingDelta = 0xFF;
        int selectedRange = (getCastRangeForFirstCastableSpellOrFallback(unit) & 0xFF) + 1;
        for (Unit candidate : candidates) {
            if (scorePrimaryCandidates && computeMissionTargetScore(unit, candidate, false) == 0xFFFFFF) {
                continue;
            }
            int range = worldMap.getRangeInTiles(unit, candidate) & 0xFF;
            if (scorePrimaryCandidates && (candidate.getMovementType() & 0xFF) == 3 && (unit.getMovementType() & 0xFF) != 3) {
                range = (range + 1) & 0xFF;
            }
            int facingDelta = worldMap.getFacingAngularDistance8(
                    worldMap.getDirection8Code(unit, candidate),
                    unit.movementState.facing
            );
            if (range < selectedRange) {
                selected = candidate;
                selectedFacingDelta = facingDelta;
                selectedRange = range;
            } else if (range == selectedRange
                    && range <= (getCastRangeForFirstCastableSpellOrFallback(unit) & 0xFF)
                    && facingDelta <= selectedFacingDelta) {
                selected = candidate;
                selectedFacingDelta = facingDelta;
            }
        }
        if ((getCastRangeForFirstCastableSpellOrFallback(unit) & 0xFF) < selectedRange) {
            return null;
        }
        return selected;
    }

    /**
     * Native: MissionScriptRuntime::classifyMissionMoveCell @0056FC69.
     * Fully ported.
     */
    public int classifyMissionMoveCell(Unit unit, int cell) {
        int targetCell = cell & 0xFFFF;
        Unit occupyingUnit = worldMap.getGroundUnitAtCell(targetCell);
        if ((unit.getTokenSizeVirtual() & 0xFF) != 1) {
            return 0;
        }
        if (unit.owner.isActive != 0) {
            return 0;
        }
        if ((unit.movementState.cell & 0xFFFF) == targetCell) {
            return 0;
        }
        if (occupyingUnit == null) {
            UnitList nearbyUnits = worldMap.collectUnitsAroundCell(targetCell, 1);
            for (Unit nearbyUnit : nearbyUnits) {
                if ((nearbyUnit.movementState.pathCurrentCell & 0xFFFF) == targetCell
                        && !nearbyUnit.m_pTargetHandle.isSubPosUnknown()) {
                    return 2;
                }
            }
            if (occupyingUnit == null) {
                return 0;
            }
        }
        if (occupyingUnit.m_nHP < 1) {
            return 0;
        }
        if (!occupyingUnit.m_pTargetHandle.isSubPosUnknown()) {
            return unit.movementState.packPositionCell() == targetCell ? 0 : 1;
        }
        if (occupyingUnit.missionRuntimeState.command == UNIT_MISSION_COMMAND_MOVE_TO_CELL
                || occupyingUnit.missionRuntimeState.command == UNIT_MISSION_COMMAND_REPATH_TO_TARGET) {
            return (unit.missionRuntimeState.commandCell & 0xFFFF) == targetCell ? 0 : 2;
        }
        return 0;
    }

    /**
     * Native: MissionScriptRuntime::queueMoveToCell @0056C8AF.
     * Fully ported.
     */
    public static void queueMissionMoveToCell(Unit unit, int cell) {
        unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_MOVE_TO_CELL;
        unit.missionRuntimeState.commandCell = cell & 0xFFFF;
    }

    /**
     * Native: MissionScriptRuntime::queueMoveToTargetUnit @0056C937.
     * Fully ported.
     */
    public static void queueMoveToTargetUnit(Unit unit, Token target) {
        unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_MOVE_TO_TARGET_UNIT;
        unit.missionRuntimeState.targetToken = target;
    }

    /**
     * Native: MissionScriptRuntime::engageMissionTarget @0056ACC8.
     * Fully ported.
     */
    public void engageMissionTarget(Unit unit, Unit target) {
        if ((target.status & UNIT_STATUS_INACTIVE) != 0) {
            setUnitDefaultMissionAction(unit, 0);
            return;
        }

        int scriptedSpellId = selectEngagementScriptedSpell(unit);
        if (scriptedSpellId != 0) {
            if (scriptedSpellId != SpellId.TELEPORT.id) {
                castSelectedSpellAtTarget(unit, target, scriptedSpellId);
                return;
            }
            if (cellChebyshevDistance(target.m_pTargetHandle.getCell(), unit.m_pTargetHandle.getCell()) > 2) {
                castSelectedSpellAtTarget(unit, target, scriptedSpellId);
                return;
            }
        }

        Unit resolvedTarget = unit == target ? null : target;
        if (resolvedTarget == null) {
            UnitList candidates = collectHostileCandidatesAtCell(unit, unit.m_pTargetHandle.getCell());
            if (!candidates.isEmpty()) {
                resolvedTarget = candidates.getFirst();
            }
        }
        if (resolvedTarget == null) {
            setUnitDefaultMissionAction(unit, 0);
            return;
        }
        if (unit.owner.isActive == 0) {
            if (unit.isMageClass() && tryCastPreferredCombatSpell(unit, resolvedTarget)) {
                return;
            }
            unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_ENGAGE_TARGET;
            unit.missionRuntimeState.targetToken = resolvedTarget;
            unit.missionRuntimeState.attackRange = getCastRangeForFirstCastableSpellOrFallback(unit);
            if (unit.isMageClass() && (unit.missionRuntimeState.attackRange & 0xFF) < 2) {
                tryCastMageSupportSpell(unit);
            }
            return;
        }
        if (unit.isMageClass() && tryCastRandomOffensiveSpell(unit, resolvedTarget)) {
            return;
        }
        unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_ENGAGE_TARGET;
        unit.missionRuntimeState.targetToken = resolvedTarget;
        unit.missionRuntimeState.attackRange = getCastRangeForFirstCastableSpellOrFallback(unit);
    }

    /**
     * Native support extracted from MissionScriptRuntime::engageMissionTarget @0056ACC8 scripted spell-probability scan.
     * Fully ported.
     */
    public int selectEngagementScriptedSpell(Unit unit) {
        int selectedSpellId = 0;
        for (int i = 0; i < unit.missionRuntimeState.engagementSpells.length; i++) {
            int spellId = unit.missionRuntimeState.engagementSpells[i];
            if (spellId != 0 && randomRaw() < unit.missionRuntimeState.engagementSpellProbabilities[i]) {
                selectedSpellId = spellId;
            }
        }
        return selectedSpellId;
    }

    /**
     * Native: MissionScriptRuntime::castSelectedSpellAtTarget @0056A981.
     * Fully ported.
     */
    public void castSelectedSpellAtTarget(Unit caster, Unit target, int spellId) {
        Spell spell = caster.spellbook.find(spellId);
        if (spell == null) {
            return;
        }
        switch (SpellId.fromId(spellId)) {
            case FIRE_ARROW, ICE_MISSILE, LIGHTNING, PRISMATIC_SPRAY, DIAMOND_DUST, STONE_CURSE, DRAIN_LIFE, CURSE,
                 SLOW -> queueTargetedSpellCast(caster, target, spell);
            case FIRE_BALL, WALL_OF_FIRE, POISON_CLOUD, BLIZZARD, DARKNESS, LIGHT, WALL_OF_EARTH ->
                    queueCellSpellCast(caster, target.m_pTargetHandle.getCell(), spell);
            case PROTECTION_FROM_FIRE, PROTECTION_FROM_WATER, INVISIBILITY, PROTECTION_FROM_AIR,
                 PROTECTION_FROM_EARTH, BLESS, HASTE, HEAL, SUMMON, SHIELD ->
                    queueTargetedSpellCast(caster, caster, spell);
            case ACID_STREAM -> {
                int direction = worldMap.getDirection8Code(caster, target) >>> 5;
                int cell = ((caster.m_pTargetHandle.getY()
                        + worldMap.neighborStepTable0x58E88.dy[direction]) & 0xFF) << 8;
                cell |= (caster.m_pTargetHandle.getX()
                        + worldMap.neighborStepTable0x58E88.dx[direction]) & 0xFF;
                queueCellSpellCast(caster, cell, spell);
            }
            case TELEPORT -> {
                int targetCell = target.m_pTargetHandle.getCell();
                if (cellChebyshevDistance(targetCell, caster.m_pTargetHandle.getCell()) > 2) {
                    int teleportCell = findTeleportCellNearTarget(caster, targetCell);
                    if (teleportCell != 0) {
                        queueCellSpellCast(caster, teleportCell, spell);
                    }
                }
            }
            default -> {
            }
        }
        caster.missionRuntimeState.virtualCasterQueuedFlag = 1;
    }

    /**
     * Native support extracted from MissionScriptRuntime::castSelectedSpellAtTarget @0056A981 teleport case.
     * Fully ported.
     */
    public int findTeleportCellNearTarget(Unit caster, int targetCell) {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                int cell = (targetCell - 0x101 + x + y * 0x100) & 0xFFFF;
                if (worldMap.hasClearLayer2Footprint(caster, cell)) {
                    return cell;
                }
            }
        }
        return 0;
    }

    /**
     * Native: MissionScriptRuntime::queueNearestEngagementTarget @0056C840.
     * Fully ported.
     */
    public void queueNearestEngagementTarget(Unit unit, Unit target) {
        if (unit.isMageClass() && unit.owner.isActive == 0 && tryCastPreferredCombatSpell(unit, target)) {
            return;
        }
        unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_NEAREST_ENGAGEMENT;
        unit.missionRuntimeState.targetToken = target;
        unit.missionRuntimeState.attackRange = getCastRangeForFirstCastableSpellOrFallback(unit);
    }

    /**
     * Native: MissionScriptRuntime::tryCastPreferredCombatSpell @0056B059.
     * Fully ported.
     */
    public boolean tryCastPreferredCombatSpell(Unit caster, Unit target) {
        int spellId = caster.missionRuntimeState.spellIndex & 0xFF;
        if (spellId == 0) {
            return false;
        }
        Spell spell = findAffordableSpell(caster, spellId);
        if (spell == null) {
            return false;
        }
        switch (SpellId.fromId(spellId)) {
            case FIRE_ARROW, ICE_MISSILE, LIGHTNING, PRISMATIC_SPRAY, DIAMOND_DUST, DRAIN_LIFE -> {
                caster.missionRuntimeState.virtualCasterQueuedFlag = 1;
                queueTargetedSpellCast(caster, target, spell);
                return true;
            }
            case FIRE_BALL -> {
                return tryCastPreferredAreaSpell(caster, target, spell, 1, true);
            }
            case WALL_OF_FIRE -> {
                return tryCastPreferredAreaSpell(caster, target, spell, 2, true);
            }
            case POISON_CLOUD, BLIZZARD -> {
                return tryCastPreferredAreaSpell(caster, target, spell, 2, false);
            }
            case STONE_CURSE, CURSE -> {
                return tryCastPreferredEffectKeyTargetSpell(caster, target, spell, spellId);
            }
            case CONTROL_SPIRIT -> {
                return false;
            }
            case HEAL -> {
                return tryCastPreferredHealSpell(caster, spell);
            }
            case SUMMON -> {
                queueTargetedSpellCast(caster, caster, spell);
                return true;
            }
            case SHIELD -> {
                if ((caster.effectKeyFlags & spellEffectKeyMask(SpellId.SHIELD)) != 0) {
                    return false;
                }
                caster.missionRuntimeState.virtualCasterQueuedFlag = 1;
                queueTargetedSpellCast(caster, caster, spell);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::tryCastActiveOwnerFallbackSpell @0057A608.
     */
    public boolean tryCastActiveOwnerFallbackSpell(Unit caster, Unit target) {
        if (caster.owner.isActive == 0) {
            return tryCastPreferredCombatSpell(caster, target);
        }

        Spell selected = findAffordableSpell(caster, SpellId.PRISMATIC_SPRAY.id);
        if (selected == null && (target.effectKeyFlags & EFFECT_FLAG_MISSION_HIDDEN) == 0) {
            selected = findAffordableSpell(caster, SpellId.STONE_CURSE.id);
        }
        if (selected == null) {
            selected = findAffordableSpell(caster, SpellId.LIGHTNING.id);
        }
        if (selected == null) {
            selected = findAffordableSpell(caster, SpellId.FIRE_ARROW.id);
        }
        if (selected == null) {
            return false;
        }

        queueTargetedSpellCast(caster, target, selected);
        return true;
    }

    /**
     * Native: MissionScriptRuntime::tryCastRandomOffensiveSpell @0057A769.
     */
    public boolean tryCastRandomOffensiveSpell(Unit caster, Unit target) {
        if ((short) caster.m_nMind >= 0x3C && randomBelow(100) <= 0x1D) {
            engageMissionTarget(caster, target);
            return false;
        }

        List<Spell> offensiveSpells = new ArrayList<>();
        for (int spellId = SpellId.FIRE_ARROW.id; spellId < SpellId.SLOW.id; spellId++) {
            Spell spell = caster.spellbook.find(spellId);
            if (spell != null && (short) spell.manaCost <= (short) caster.m_nMP && !spell.isDefensive) {
                offensiveSpells.add(spell);
            }
        }
        int selectedIndex = randomBelow(offensiveSpells.size());
        Spell selected = null;
        for (int i = 0; i < offensiveSpells.size(); i++) {
            selected = offensiveSpells.get(i);
            if (i == selectedIndex) {
                break;
            }
        }
        if (selected == null) {
            return false;
        }
        caster.missionRuntimeState.virtualCasterQueuedFlag = 1;
        queueTargetedSpellCast(caster, target, selected);
        return true;
    }

    /**
     * Native: RANDOM @0057AFC0.
     */
    public int randomRaw() {
        return Utils.randExclusive(0, mRandomRelated.randomScaleLimit);
    }

    /**
     * Native: MissionScriptRuntime::randomBelow @0057AFD0.
     */
    public int randomBelow(int upperExclusive) {
        return (randomRaw() * upperExclusive) / mRandomRelated.randomScaleLimit;
    }

    /**
     * Native: MissionScriptRuntime::randomInclusiveRange @0057AFF0.
     */
    public int randomInclusiveRange(int lowerInclusive, int upperInclusive) {
        return (randomRaw() * ((upperInclusive - lowerInclusive) + 1)) / mRandomRelated.randomScaleLimit
                + lowerInclusive;
    }

    /**
     * Native support extracted from MissionScriptRuntime::tryCastPreferredCombatSpell @0056B059 area-score cases.
     * Fully ported.
     */
    public boolean tryCastPreferredAreaSpell(Unit caster, Unit target, Spell spell, int radius, boolean projectFacingCell) {
        rebuildPreferredSpellCandidateBuckets(caster);
        int bestScore = PREFERRED_AREA_SCORE_INITIAL;
        for (Unit hostile : preferredHostileCandidates) {
            int cell = hostile.m_pTargetHandle.getCell();
            if (projectFacingCell && !hostile.m_pTargetHandle.isSubPosUnknown()) {
                int direction = (hostile.movementState.facing & 0xFF) >> 5;
                cell = (cell
                        + worldMap.neighborStepTable0x58E88.dx[direction]
                        + worldMap.neighborStepTable0x58E88.dy[direction] * 0x100) & 0xFFFF;
            }
            int score = scorePreferredAreaSpellCell(caster, cell, radius);
            if (bestScore < score) {
                bestScore = score;
            }
        }
        if (bestScore < 1) {
            return false;
        }
        caster.missionRuntimeState.virtualCasterQueuedFlag = 1;
        queueTargetedSpellCast(caster, target, spell);
        return true;
    }

    /**
     * Native support extracted from MissionScriptRuntime::tryCastPreferredCombatSpell @0056B059,
     * MissionScriptRuntime::rebuildPreferredSpellVisibleCandidates @00571E9B, and
     * MissionScriptRuntime::splitPreferredSpellVisibleCandidateBuckets @00567C56.
     * Fully ported.
     */
    public void rebuildPreferredSpellCandidateBuckets(Unit caster) {
        rebuildPreferredSpellVisibleCandidates(caster.unitGroup);
        splitPreferredSpellVisibleCandidateBuckets(caster, primaryCandidateUnits);
    }

    /**
     * Native: MissionScriptRuntime::splitPreferredSpellVisibleCandidateBuckets @00567C56.
     * Fully ported.
     */
    public void splitPreferredSpellVisibleCandidateBuckets(Unit caster, UnitList candidates) {
        preferredFriendlyCandidates.clear();
        preferredHostileCandidates.clear();
        for (Unit candidate : candidates) {
            if (candidate.state == UnitActionState.DEAD) {
                continue;
            }
            Player casterOwner = caster.owner;
            Player candidateOwner = candidate.owner;
            if ((candidate.effectKeyFlags & spellEffectKeyMask(SpellId.INVISIBILITY)) != 0
                    && getRelationFlags(casterOwner, candidateOwner) != PLAYER_RELATION_ALLIED_MASK
                    && !isInvisibleCandidateVisible(caster, candidate)) {
                continue;
            }
            if (isHostile(casterOwner, candidateOwner)) {
                preferredHostileCandidates.add(candidate);
            } else {
                preferredFriendlyCandidates.add(candidate);
            }
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::tryCastPreferredCombatSpell @0056B059 area-score cases.
     * Fully ported.
     */
    public int scorePreferredAreaSpellCell(Unit caster, int cell, int radius) {
        UnitList nearby = worldMap.collectUnitsAroundCell(cell, radius);
        int friendlyPenalty = 0;
        int hostileCount = 0;
        for (Unit nearbyUnit : nearby) {
            if (isHostile(caster.owner, nearbyUnit.owner)) {
                hostileCount++;
            } else if (nearbyUnit == caster) {
                friendlyPenalty += PREFERRED_AREA_FRIENDLY_SELF_PENALTY;
            } else {
                friendlyPenalty++;
            }
        }
        return hostileCount - friendlyPenalty;
    }

    /**
     * Native support extracted from MissionScriptRuntime::tryCastPreferredCombatSpell @0056B059 cases `0x12` and `0x1C`.
     * Fully ported.
     */
    public boolean tryCastPreferredEffectKeyTargetSpell(Unit caster, Unit target, Spell spell, int spellId) {
        int effectMask = 1 << (spellId & 0x1F);
        if ((target.effectKeyFlags & effectMask) != 0) {
            caster.missionRuntimeState.virtualCasterQueuedFlag = 1;
            queueTargetedSpellCast(caster, target, spell);
            return true;
        }
        rebuildPreferredSpellCandidateBuckets(caster);
        for (Unit hostile : preferredHostileCandidates) {
            if ((hostile.effectKeyFlags & effectMask) == 0) {
                caster.missionRuntimeState.virtualCasterQueuedFlag = 1;
                queueTargetedSpellCast(caster, target, spell);
                return true;
            }
        }
        return false;
    }

    /**
     * Native support extracted from MissionScriptRuntime::tryCastPreferredCombatSpell @0056B059 case `0x18`.
     * Fully ported.
     */
    public boolean tryCastPreferredHealSpell(Unit caster, Spell spell) {
        rebuildPreferredSpellCandidateBuckets(caster);
        Unit selected = null;
        double selectedHpRatio = 1.0;
        for (Unit candidate : preferredFriendlyCandidates) {
            if (isPreferredHealTarget(caster, candidate)) {
                double hpRatio = nativeHealSelectionHpQuotient(candidate);
                if (hpRatio < selectedHpRatio) {
                    selected = candidate;
                    selectedHpRatio = hpRatio;
                }
            }
        }
        if (selectedHpRatio >= 1.0) {
            caster.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
            return true;
        }
        caster.missionRuntimeState.virtualCasterQueuedFlag = 1;
        queueTargetedSpellCast(caster, selected, spell);
        return true;
    }

    /**
     * Native: MissionScriptRuntime::isPreferredHealTarget @0056BB53.
     * Fully ported.
     */
    public boolean isPreferredHealTarget(Unit caster, Unit target) {
        Player casterOwner = caster.owner;
        Player targetOwner = target.owner;
        if (!hasRelationFlag(casterOwner, targetOwner, 0x03)
                && (casterOwner.battlePreferences.autoCasting & GamePreferences.AUTOCAST_NEUTRAL) != 0) {
            return true;
        }
        if (hasRelationFlag(casterOwner, targetOwner, PLAYER_RELATION_ALLIED_MASK)
                && (casterOwner.battlePreferences.autoCasting & GamePreferences.AUTOCAST_ALLIES) != 0) {
            return true;
        }
        return (caster.owner == target.owner
                && (casterOwner.battlePreferences.autoCasting & GamePreferences.AUTOCAST_OWN) != 0) || caster == target;
    }

    /**
     * Native: MissionScriptRuntime::setUnitDefaultMissionAction @0056AFAC.
     * Fully ported.
     */
    public void setUnitDefaultMissionAction(Unit unit, int attackNearest) {
        if (attackNearest == 0) {
            engageNearestTarget(unit);
            return;
        }
        prepareUnitDefaultMissionAction(unit);
    }

    /**
     * Native: MissionScriptRuntime::prepareUnitDefaultMissionAction @0056AFD9.
     * Fully ported.
     */
    public void prepareUnitDefaultMissionAction(Unit unit) {
        Player owner = unit.owner;
        if (owner.isActive == 0) {
            prepareInactiveDefaultMissionAction(unit);
            return;
        }
        unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_LOOK_AROUND;
        if (unit.isMageClass()) {
            tryCastActiveMageSupportSpell(unit);
        }
    }

    /**
     * Native: MissionScriptRuntime::prepareInactiveDefaultMissionAction @0056B025.
     * Fully ported.
     */
    public void prepareInactiveDefaultMissionAction(Unit unit) {
        if (!unit.isMageClass()) {
            unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
        } else {
            tryCastMageSupportSpell(unit);
        }
    }

    /**
     * Native: MissionScriptRuntime::tryCastActiveMageSupportSpell @0056C592.
     * Fully ported.
     */
    public void tryCastActiveMageSupportSpell(Unit unit) {
        if (unit.spellbook == null || (short) unit.m_nMP <= MAGE_SUPPORT_MIN_MP_EXCLUSIVE) {
            return;
        }
        Spell heal = unit.spellbook.find(SpellId.HEAL.id);
        if (heal == null) {
            return;
        }
        if ((short) unit.m_nHP != (short) unit.m_nMaxHP) {
            unit.missionRuntimeState.virtualCasterQueuedFlag = 1;
            queueTargetedSpellCast(unit, unit, heal);
            return;
        }

        UnitList candidates = worldMap.collectUnitsAroundCell(
                unit.m_pTargetHandle.getCell(),
                heal.maxRange & 0xFF
        );
        filterMageSupportCandidates(unit, candidates);
        Unit selected = null;
        double selectedHpRatio = 2.0;
        for (Unit candidate : candidates) {
            double hpRatio = nativeHealSelectionHpQuotient(candidate);
            if (hpRatio < selectedHpRatio) {
                selected = candidate;
                selectedHpRatio = hpRatio;
            }
        }
        if (selectedHpRatio >= 1.0) {
            tryCastRandomMageSupportSpell(unit, candidates);
        } else {
            unit.missionRuntimeState.virtualCasterQueuedFlag = 1;
            queueTargetedSpellCast(unit, selected, heal);
        }
    }

    /**
     * Native: MissionScriptRuntime::filterMageSupportCandidates @00567B2D.
     * Fully ported.
     */
    public void filterMageSupportCandidates(Unit unit, UnitList candidates) {
        for (int index = 0; index < candidates.size(); ) {
            Unit candidate = candidates.get(index);
            boolean remove = !hasRelationFlag(unit.owner, candidate.owner, PLAYER_RELATION_ALLIED_MASK);
            if (!remove && (candidate.effectKeyFlags & spellEffectKeyMask(SpellId.INVISIBILITY)) != 0) {
                remove = !isInvisibleCandidateVisible(unit, candidate);
            }
            if (remove) {
                candidates.remove(index);
            } else {
                index++;
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::tryCastRandomMageSupportSpell @0057A999.
     */
    public boolean tryCastRandomMageSupportSpell(Unit unit, UnitList candidates) {
        Spell heal = unit.spellbook.find(SpellId.HEAL.id);
        if ((short) unit.m_nHP != (short) unit.m_nMaxHP && heal != null) {
            unit.missionRuntimeState.virtualCasterQueuedFlag = 1;
            queueTargetedSpellCast(unit, unit, heal);
            return true;
        }

        Spell shield = unit.spellbook.find(SpellId.SHIELD.id);
        if (shield != null && (unit.effectKeyFlags & spellEffectKeyMask(shield)) == 0) {
            unit.missionRuntimeState.virtualCasterQueuedFlag = 1;
            queueTargetedSpellCast(unit, unit, shield);
            return true;
        }

        int selectedIndex = randomBelow(candidates.size());
        Unit selected = null;
        for (int i = 0; i < candidates.size(); i++) {
            selected = candidates.get(i);
            if (i == selectedIndex) {
                break;
            }
        }
        if (selected == null || selected.owner != unit.owner) {
            return false;
        }
        if ((short) selected.m_nHP != (short) selected.m_nMaxHP && heal != null) {
            unit.missionRuntimeState.virtualCasterQueuedFlag = 1;
            queueTargetedSpellCast(unit, selected, heal);
            return true;
        }
        if ((short) unit.m_nMP != (short) unit.m_nMaxMP) {
            return false;
        }

        List<Spell> defensiveSpells = new ArrayList<>();
        for (int spellId = SpellId.FIRE_ARROW.id;
             spellId < SpellId.SLOW.id;
             spellId++) {
            Spell spell = findAffordableSpell(unit, spellId);
            if (spell != null
                    && spell.isDefensive
                    && (spell.id & 0xFF) != SpellId.HEAL.id
                    && (spell.id & 0xFF) != SpellId.SHIELD.id) {
                defensiveSpells.add(spell);
            }
        }
        int selectedSpellIndex = randomBelow(defensiveSpells.size());
        Spell selectedSpell = null;
        for (int i = 0; i < defensiveSpells.size(); i++) {
            selectedSpell = defensiveSpells.get(i);
            if (i == selectedSpellIndex) {
                break;
            }
        }
        if (selectedSpell == null || (selected.effectKeyFlags & spellEffectKeyMask(selectedSpell)) != 0) {
            return false;
        }
        unit.missionRuntimeState.virtualCasterQueuedFlag = 1;
        queueTargetedSpellCast(unit, selected, selectedSpell);
        return true;
    }

    /**
     * Native support extracted from MissionScriptRuntime::tryCastRandomMageSupportSpell @0057A999.
     */
    public static int spellEffectKeyMask(Spell spell) {
        return 1 << (spell.id & 0x1F);
    }

    /**
     * Native support extracted from MissionScriptRuntime::tryCastMageSupportSpell @0056BC7B spell effect-key checks,
     * using the native spell id domain.
     * Fully ported.
     */
    public static int spellEffectKeyMask(SpellId spellId) {
        return 1 << (spellId.id & 0x1F);
    }

    /**
     * Native: MissionScriptRuntime::tryCastMageSupportSpell @0056BC7B.
     * Fully ported.
     */
    public void tryCastMageSupportSpell(Unit caster) {
        Player owner = caster.owner;
        if ((short) caster.m_nMP < MAGE_SUPPORT_INACTIVE_MIN_MP
                || (owner.battlePreferences.autoCasting & GamePreferences.AUTOCAST_BASE) == 0) {
            caster.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
            return;
        }

        Spell spell = findAffordableSpell(caster, SpellId.HEAL.id);
        if ((short) caster.m_nHP != (short) caster.m_nMaxHP && spell != null) {
            caster.missionRuntimeState.virtualCasterQueuedFlag = 1;
            queueTargetedSpellCast(caster, caster, spell);
            return;
        }

        spell = findAffordableSpell(caster, SpellId.SHIELD.id);
        if ((caster.effectKeyFlags & spellEffectKeyMask(SpellId.SHIELD)) == 0 && spell != null) {
            caster.missionRuntimeState.virtualCasterQueuedFlag = 1;
            queueTargetedSpellCast(caster, caster, spell);
            return;
        }

        rebuildPreferredSpellCandidateBuckets(caster);
        spell = findAffordableSpell(caster, SpellId.HEAL.id);
        if (spell != null && preferredFriendlyCandidates.size() > 1) {
            Unit selected = null;
            double selectedHpRatio = 2.0;
            for (Unit candidate : preferredFriendlyCandidates) {
                if (isPreferredHealTarget(caster, candidate) && isTargetWithinSpellRange(caster, candidate, spell)) {
                    double hpRatio = nativeHealSelectionHpQuotient(candidate);
                    if (hpRatio < selectedHpRatio) {
                        selected = candidate;
                        selectedHpRatio = hpRatio;
                    }
                }
            }
            if (selectedHpRatio < 1.0) {
                caster.missionRuntimeState.virtualCasterQueuedFlag = 1;
                queueTargetedSpellCast(caster, selected, spell);
                return;
            }
        }

        if ((owner.battlePreferences.autoCasting & GamePreferences.AUTOCAST_MODE_AVERAGE) == 0
                || (short) caster.m_nMP < MAGE_SUPPORT_MIN_MP_EXCLUSIVE) {
            caster.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
            return;
        }
        for (Unit candidate : preferredFriendlyCandidates) {
            if (isPreferredHealTarget(caster, candidate)) {
                if (tryQueueMageSupportBuff(caster, candidate, SpellId.BLESS, true)) {
                    return;
                }
                if (tryQueueMageSupportBuff(caster, candidate, SpellId.HASTE, false)) {
                    return;
                }
            }
        }

        if ((owner.battlePreferences.autoCasting & GamePreferences.AUTOCAST_MODE_MAXIMUM) == 0
                || (short) caster.m_nMP < 0x28) {
            caster.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
            return;
        }
        for (Unit candidate : preferredFriendlyCandidates) {
            if (isPreferredHealTarget(caster, candidate)) {
                if (tryQueueMageSupportBuff(caster, candidate, SpellId.PROTECTION_FROM_AIR, false)) {
                    return;
                }
                if (tryQueueMageSupportBuff(caster, candidate, SpellId.PROTECTION_FROM_EARTH, false)) {
                    return;
                }
                if (tryQueueMageSupportBuff(caster, candidate, SpellId.PROTECTION_FROM_FIRE, false)) {
                    return;
                }
                if (tryQueueMageSupportBuff(caster, candidate, SpellId.PROTECTION_FROM_WATER, false)) {
                    return;
                }
                if (caster != candidate
                        && hasRelationFlag(caster.owner, candidate.owner, 0x03)
                        && tryQueueMageSupportBuff(caster, candidate, SpellId.INVISIBILITY, false)) {
                    return;
                }
            }
        }

        spell = findAffordableSpell(caster, SpellId.INVISIBILITY.id);
        if (spell == null || (caster.effectKeyFlags & spellEffectKeyMask(SpellId.INVISIBILITY)) != 0) {
            caster.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
        } else {
            caster.missionRuntimeState.virtualCasterQueuedFlag = 1;
            queueTargetedSpellCast(caster, caster, spell);
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::tryCastMageSupportSpell @0056BC7B support-buff branches.
     * Fully ported.
     */
    public boolean tryQueueMageSupportBuff(Unit caster, Unit target, SpellId spellId, boolean requireNonMage) {
        Spell spell = findAffordableSpell(caster, spellId.id);
        if (spell == null || (requireNonMage && !target.isNonMageClass())) {
            return false;
        }
        if ((target.effectKeyFlags & spellEffectKeyMask(spellId)) != 0) {
            return false;
        }
        if (target.missionRuntimeState.lastIncomingSpellId == SpellId.BLESS.id
                && Math.abs(target.missionRuntimeState.lastIncomingSpellTick - Globals.gameServer.serverLoopCounter)
                <= MAGE_SUPPORT_REPEAT_DELAY_TICKS) {
            return false;
        }
        if (!isTargetWithinSpellRange(caster, target, spell)) {
            return false;
        }
        caster.missionRuntimeState.virtualCasterQueuedFlag = 1;
        queueTargetedSpellCast(caster, target, spell);
        return true;
    }

    /**
     * Native support extracted from heal target selection in MissionScriptRuntime::tryCastPreferredCombatSpell @0056B059,
     * MissionScriptRuntime::tryCastMageSupportSpell @0056BC7B, and
     * MissionScriptRuntime::tryCastActiveMageSupportSpell @0056C592.
     * Fully ported.
     */
    public static double nativeHealSelectionHpQuotient(Unit unit) {
        return (short) unit.m_nHP / (short) unit.m_nMaxHP;
    }

    /**
     * Native support extracted from MissionScriptRuntime::isTargetWithinSpellRange @0056BC2E.
     * Fully ported.
     */
    public static boolean isTargetWithinSpellRange(Unit caster, Unit target, Spell spell) {
        return cellChebyshevDistance(caster.m_pTargetHandle.getCell(), target.m_pTargetHandle.getCell())
                <= (spell.maxRange & 0xFF);
    }

    /**
     * Native: MissionScriptRuntime::updateUnitMissionAction @005692D5.
     * Fully ported.
     */
    public void updateUnitMissionAction(Unit unit) {
        if (unit.owner.isActive == 0) {
            applyUnitMissionActionState(unit);
        } else {
            applyActiveUnitMissionActionState(unit);
        }
    }

    /**
     * Native: MissionScriptRuntime::applyActiveUnitMissionActionState @00569308.
     * Fully ported.
     */
    public void applyActiveUnitMissionActionState(Unit unit) {
        applyUnitMissionActionState(unit);
    }

    /**
     * Native: MissionScriptRuntime::applyUnitMissionActionState @00569321.
     * Fully ported.
     */
    public void applyUnitMissionActionState(Unit unit) {
        if (unit.missionActionCode == ATTACK_TARGET
                && unit.missionRuntimeState.targetToken != null
                && ((Unit) unit.missionRuntimeState.targetToken).state == UnitActionState.DEAD) {
            reenterScenarioMissionEntryUnit(unit);
        }
        if ((unit.missionActionCode == RANGE_TARGET_ENGAGE
                || unit.missionActionCode == RANGE_TARGET_RETREAT)
                && ((Unit) unit.missionRuntimeState.rangeTargetToken).state == UnitActionState.DEAD) {
            reenterScenarioMissionEntryUnit(unit);
        }
        if (unit.missionActionCode == MOVE_TO_CELL
                && unit.movementState.missionReentryPending != 0
                && unit.movementState.missionReentryCell == unit.m_pTargetHandle.getCell()) {
            reenterScenarioMissionEntryUnit(unit);
        }
        switch (unit.missionActionCode) {
            case DYING -> unit.state = UnitActionState.DYING;
            case MOVE_TO_CELL -> queueMissionMoveToCell(unit, unit.missionRuntimeState.commandCell);
            case PICKUP_ORDER -> queuePickupTransitionAtCell(unit, unit.missionRuntimeState.commandCell);
            case ATTACK_TARGET -> engageMissionTokenTarget(
                    unit,
                    unit.missionRuntimeState.targetToken
            );
            case COMMAND_CELL_OR_TARGET ->
                    moveToCommandCellOrEngageHostileAtCurrentCell(unit, unit.missionRuntimeState.commandCell);
            case RANGE_TARGET_ENGAGE ->
                    processRangeTargetUnitEngagement(unit, (Unit) unit.missionRuntimeState.rangeTargetToken);
            case WAYPOINT -> processMissionWaypointStatus(unit);
            case SCRIPT_CELL_STATUS -> processMissionScriptCellStatus(unit);
            case ENGAGE_NEAREST -> engageNearestTarget(unit);
            case TARGETED_SPELL_ORDER -> queueTargetedSpellCast(
                    unit,
                    unit.missionRuntimeState.spellTargetToken,
                    unit.missionRuntimeState.spell
            );
            case CELL_SPELL_ORDER -> queueCellSpellCast(
                    unit,
                    unit.missionRuntimeState.cellSpellTargetCell,
                    unit.missionRuntimeState.spell
            );
            case INTERACT -> queueInteractMissionCommand(unit, unit.missionRuntimeState.interactionTarget);
            case RANGE_TARGET_RETREAT ->
                    processRangeTargetUnitRetreat(unit, (Unit) unit.missionRuntimeState.rangeTargetToken);
            case RETREAT_ORDER -> processWimpyRetreat(unit);
            case HIDE -> processSpecialMissionStatus(unit);
            case FIXED_EVACUATION -> processFixedEvacuationMissionStatus(unit);
            case PICKUP_ALL_SACKS_ORDER -> processSackInteractionMissionStatus(unit);
            case MISSION_HIDDEN -> unit.state = UnitActionState.MISSION_HIDDEN;
            default -> {
                unit.state = UnitActionState.DYING;
                setUnitDefaultMissionAction(unit, 0);
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::queuePickupTransitionAtCell @0056C8DA.
     * Fully ported.
     */
    public static void queuePickupTransitionAtCell(Unit unit, int cell) {
        if (unit.m_pTargetHandle.getCell() == (cell & 0xFFFF)) {
            unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_PICKUP_TRANSITION;
            unit.missionRuntimeState.commandCell = cell & 0xFFFF;
        } else {
            queueMissionMoveToCell(unit, cell);
        }
    }

    /**
     * Native: MissionScriptRuntime::moveToCommandCellOrEngageHostileAtCurrentCell @00569638.
     * Fully ported.
     */
    public void moveToCommandCellOrEngageHostileAtCurrentCell(Unit unit, int commandCell) {
        UnitList candidates = collectHostileCandidatesAtCell(unit, unit.m_pTargetHandle.getCell());
        if (candidates.isEmpty()) {
            queueMissionMoveToCell(unit, commandCell);
        } else {
            engageMissionTarget(unit, candidates.getFirst());
        }
    }

    /**
     * Native: MissionScriptRuntime::processMissionScriptCellEngagement @0056A5FC.
     * Fully ported.
     */
    public void processMissionScriptCellEngagement(Unit unit) {
        if (unit.missionRuntimeState.missionScriptCell == 0) {
            unit.missionRuntimeState.missionScriptCell = unit.m_pTargetHandle.getCell();
        }
        int missionCell = unit.missionRuntimeState.missionScriptCell;
        if ((unit.sightRange & 0xFF) <= cellChebyshevDistance(unit.m_pTargetHandle.getCell(), missionCell)) {
            queueMissionMoveToCell(unit, missionCell);
        }
        UnitList candidates = collectHostileCandidatesAtCell(unit, missionCell);
        if (candidates.isEmpty()) {
            if (unit.m_pTargetHandle.getCell() == missionCell) {
                setUnitDefaultMissionAction(unit, 0);
            } else {
                queueMissionMoveToCell(unit, missionCell);
            }
        } else {
            engageMissionTarget(unit, candidates.getFirst());
        }
    }

    /**
     * Native: MissionScriptRuntime::processMissionScriptCellStatus @00569CC9.
     * Fully ported.
     */
    public void processMissionScriptCellStatus(Unit unit) {
        processMissionScriptCellEngagement(unit);
    }

    /**
     * Native: MissionScriptRuntime::queueRepathToRangeTarget @0056C960.
     * Fully ported.
     */
    public static void queueRepathToRangeTarget(Unit unit, Token target, int distanceTiles) {
        unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_REPATH_TO_TARGET;
        unit.missionRuntimeState.repathTargetToken = target;
        unit.missionRuntimeState.attackRange = distanceTiles == 0 ? unit.sightRange & 0xFF : distanceTiles & 0xFF;
    }

    /**
     * Native: MissionScriptRuntime::processRangeTargetUnitEngagement @005696E8.
     * Fully ported.
     */
    public void processRangeTargetUnitEngagement(Unit unit, Unit target) {
        int distanceTiles = unit.missionRuntimeState.rangeTargetMode & 0xFF;
        int range = cellChebyshevDistance(unit.m_pTargetHandle.getCell(), target.m_pTargetHandle.getCell());
        if (distanceTiles < range) {
            queueRepathToRangeTarget(unit, target, distanceTiles);
            return;
        }
        if (unit.spellbook == null) {
            engageClosestCandidateNearAnchorPreferringBuildings(unit, target);
        } else {
            tryHealTargetOrEngageClosestCandidateNearAnchor(unit, target);
        }
        switch (unit.missionRuntimeState.command) {
            case UNIT_MISSION_COMMAND_ENGAGE_TARGET,
                 UNIT_MISSION_COMMAND_NEAREST_ENGAGEMENT,
                 UNIT_MISSION_COMMAND_TARGETED_SPELL,
                 UNIT_MISSION_COMMAND_CELL_SPELL -> {
            }
            default -> {
                if (range < 2) {
                    int projectedCell = worldMap.projectCellAwayFromPackedPosition(
                            unit,
                            (target.m_pTargetHandle.packYdY() << 16) + target.m_pTargetHandle.packXdX(),
                            distanceTiles
                    );
                    queueMissionMoveToCell(unit, projectedCell);
                }
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::processRangeTargetUnitRetreat @00569857.
     * Fully ported.
     */
    public void processRangeTargetUnitRetreat(Unit unit, Unit target) {
        int distanceTiles = unit.missionRuntimeState.rangeTargetMode & 0xFF;
        int range = cellChebyshevDistance(unit.m_pTargetHandle.getCell(), target.m_pTargetHandle.getCell());
        if (distanceTiles < range) {
            queueRepathToRangeTarget(unit, target, distanceTiles);
        } else if (range < 2) {
            int projectedCell = worldMap.projectCellAwayFromPackedPosition(
                    unit,
                    (target.m_pTargetHandle.packYdY() << 16) + target.m_pTargetHandle.packXdX(),
                    distanceTiles
            );
            queueMissionMoveToCell(unit, projectedCell);
        } else {
            setUnitDefaultMissionAction(unit, 0);
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::queueInteractMissionCommand @0056AF23.
     * Fully ported.
     */
    public static void queueInteractMissionCommand(Unit unit, Object interactionTarget) {
        unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_INTERACT_COMMAND_CELL;
        unit.missionRuntimeState.interactionTarget = interactionTarget;
    }

    /**
     * Native: MissionScriptRuntime::processFixedEvacuationMissionStatus @0056F71A.
     * Fully ported.
     * Native support includes MissionScriptRuntime::resetUnitToScenarioCellCommand @0056D393.
     */
    public void processFixedEvacuationMissionStatus(Unit unit) {
        queueMissionMoveToCell(unit, 0x4F5B);
        if ((unit.m_pTargetHandle.getX() & 0xFF) > 0x58) {
            resetUnitToScenarioCellCommand(unit, 0x52, 0x2F);
        }
    }

    /**
     * Native: MissionScriptRuntime::processSackInteractionMissionStatus @0056A909.
     * Fully ported.
     */
    public void processSackInteractionMissionStatus(Unit unit) {
        worldMap.collectSackInteractionCells(unit, true);
        if (worldMap.sackInteractionCells.isEmpty()) {
            unit.enterScenarioMissionUnitScriptState(this);
        } else {
            unit.missionRuntimeState.commandCell = worldMap.sackInteractionCells.getFirst() & 0xFFFF;
            unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_ATTACK_COMMAND_CELL;
        }
    }

    /**
     * Native: MissionScriptRuntime::updateSpecialMissionOutcomeState @0056F2B5.
     * Fully ported.
     */
    public void updateSpecialMissionOutcomeState() {
        specialMissionCoordinateToggleTicks = (specialMissionCoordinateToggleTicks + 1) & 0xFF;
        if ((specialMissionCoordinateToggleTicks & 0xFF) > 0x1E) {
            specialMissionCoordinateToggleTicks = 0;
            if ((specialMissionCoordinateIndex & 0xFF) == 0) {
                if ((specialMissionPhaseCounter & 0xFF) == 0) {
                    specialMissionCoordinateIndex = randomInclusiveRange(1, 3) & 0xFF;
                }
            } else {
                specialMissionCoordinateIndex = 0;
            }
        }

        boolean hasSpecialMissionUnit = false;
        int inactiveUnitCount = 0;
        UnitList activeUnits = worldMap.activeUnits0xA456C;
        if (activeUnits != null) {
            for (Unit activeUnit : activeUnits) {
                if ((activeUnit.getTokenTypeId() & 0xFFFF) == 0x18) {
                    hasSpecialMissionUnit = true;
                }
                if (activeUnit.owner.isActive == 0) {
                    inactiveUnitCount = (inactiveUnitCount + 1) & 0xFF;
                    if (!hasSpecialMissionUnit) {
                        int distance = chebyshevDistance(
                                activeUnit.m_pTargetHandle.getX(),
                                activeUnit.m_pTargetHandle.getY(),
                                0x3F,
                                0x1A
                        );
                        if (distance < 4) {
                            specialMissionCompletionArmed = 1;
                        }
                    }
                }
            }
        }

        if (!hasSpecialMissionUnit && specialMissionCompletionArmed != 0) {
            missionCompleteCount++;
        }
        if ((inactiveUnitCount & 0xFF) == 0) {
            if (specialMissionFailureArmed != 0) {
                missionFailureValue++;
            }
        } else if (specialMissionFailureArmed == 0) {
            specialMissionFailureArmed = 1;
            worldMap.markSpecialMissionExitAreaBlocked();
        }
    }

    /**
     * Native: MissionScriptRuntime::processSpecialMissionStatus @0056F492.
     * Fully ported.
     */
    public void processSpecialMissionStatus(Unit unit) {
        int activeCoordinateIndex = specialMissionCoordinateIndex & 0xFF;
        boolean queuedSpecialCoordinate = false;
        if ((unit.getTokenTypeId() & 0xFFFF) == 0x18) {
            specialMissionUnit = unit;
            unit.sightRange = 5;
            unit.defaultCastRange = 5;
            engageNearestTarget(unit);
            if (unit.missionRuntimeState.command == UNIT_MISSION_COMMAND_NEAREST_ENGAGEMENT) {
                specialMissionPhaseCounter = (specialMissionPhaseCounter + 1) & 0xFF;
            }
            if (specialMissionPhaseCounter != 0) {
                specialMissionPhaseCounter = (specialMissionPhaseCounter + 1) & 0xFF;
            }
            return;
        }
        if (specialMissionPhaseCounter == 0) {
            if (activeCoordinateIndex != 0) {
                int targetCell = getSpecialMissionCoordinateCell(activeCoordinateIndex);
                if (cellChebyshevDistance(unit.m_pTargetHandle.getCell(), targetCell) < 5) {
                    queuedSpecialCoordinate = true;
                    if (unit.missionRuntimeState.runtimeState != 0xFF) {
                        queueMissionMoveToCell(unit, targetCell);
                    }
                }
            }
            if (!queuedSpecialCoordinate) {
                clearHiddenRuntimeState(unit);
                if (unit.missionRuntimeState.missionScriptCell == 0) {
                    unit.missionRuntimeState.missionScriptCell = unit.m_pTargetHandle.getCell();
                }
                processMissionScriptCellEngagement(unit);
            }
            return;
        }
        clearHiddenRuntimeState(unit);
        if (unit.missionRuntimeState.waypointCells.isEmpty()) {
            int targetCell = (randomInclusiveRange(0x2D, 0x37) << 8) | randomInclusiveRange(0x4D, 0x57);
            unit.missionRuntimeState.missionScriptCell = targetCell;
            unit.missionRuntimeState.waypointCells.add((short) targetCell);
        }
        processMissionScriptCellEngagement(unit);
    }

    /**
     * Native: MissionScriptRuntime::processMissionWaypointStatus @00569B62.
     * Fully ported.
     */
    public void processMissionWaypointStatus(Unit unit) {
        unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
        if (unit.missionRuntimeState.waypointRefreshFlag != 0) {
            unit.missionRuntimeState.missionScriptCell = unit.m_pTargetHandle.getCell();
            unit.missionRuntimeState.waypointRefreshFlag = 0;
        }
        processMissionScriptCellEngagement(unit);
        if (unit.missionRuntimeState.command == UNIT_MISSION_COMMAND_NONE
                || unit.missionRuntimeState.command == UNIT_MISSION_COMMAND_LOOK_AROUND) {
            unit.missionRuntimeState.waypointRefreshFlag = 1;
            int targetCell = unit.missionRuntimeState.waypointTargetCell;
            if (unit.m_pTargetHandle.getCell() == (targetCell & 0xFFFF)) {
                targetCell = nextMissionWaypointCell(unit, targetCell);
            }
            unit.missionRuntimeState.waypointTargetCell = targetCell;
            queueMissionMoveToCell(unit, targetCell);
        }
    }

    /**
     * Native: MissionScriptRuntime::initializeMissionWaypointStatus @0056D850.
     * Fully ported.
     */
    public void initializeMissionWaypointStatus(Unit unit) {
        unit.resetScenarioMissionUnitScriptState(this);
        unit.missionActionCode = WAYPOINT;
        int currentCell = unit.m_pTargetHandle.getCell();
        unit.missionRuntimeState.missionScriptCell = currentCell;
        unit.missionRuntimeState.waypointCells.clear();
        unit.missionRuntimeState.waypointCells.add((short) currentCell);
        unit.missionRuntimeState.waypointCells.add((short) 0);
        int targetX = randomInclusiveRange(1, 2) == 1 ? 0x46 : 100;
        int targetY = randomInclusiveRange(0x3C, 0x46) & 0xFF;
        unit.missionRuntimeState.waypointTargetCell = (targetY << 8) | targetX;
        unit.missionRuntimeState.waypointRefreshFlag = 0;
        unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
    }

    /**
     * Native: MissionScriptRuntime::processMissionWaypointEdgeStatus @0056F111.
     * Fully ported.
     */
    public void processMissionWaypointEdgeStatus(Unit unit) {
        if (unit.missionRuntimeState.waypointCells.isEmpty()) {
            initializeMissionWaypointStatus(unit);
        }
        unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
        if (unit.missionRuntimeState.waypointRefreshFlag != 0) {
            unit.missionRuntimeState.missionScriptCell = unit.m_pTargetHandle.getCell();
            unit.missionRuntimeState.waypointRefreshFlag = 0;
        }
        processMissionScriptCellEngagement(unit);
        if (unit.missionRuntimeState.command == UNIT_MISSION_COMMAND_NONE) {
            unit.missionRuntimeState.waypointRefreshFlag = 1;
            int targetCell = unit.missionRuntimeState.waypointTargetCell & 0xFFFF;
            int targetX = targetCell & 0xFF;
            int currentX = unit.m_pTargetHandle.getX() & 0xFF;
            if (targetX < 0x55 && currentX < 0x48) {
                targetCell = ((randomInclusiveRange(0x3C, 0x46) & 0xFF) << 8) | 100;
            }
            if (targetX > 0x55 && currentX > 0x62) {
                targetCell = ((randomInclusiveRange(0x3C, 0x46) & 0xFF) << 8) | 0x46;
            }
            unit.missionRuntimeState.waypointTargetCell = targetCell;
            queueMissionMoveToCell(unit, targetCell);
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::processSpecialMissionStatus @0056F492 coordinate table.
     * Fully ported.
     */
    public int getSpecialMissionCoordinateCell(int index) {
        int tableIndex = ((index & 0xFF) - 1) * 2;
        int x = Byte.toUnsignedInt(specialMissionCoordinateTable[tableIndex]);
        int y = Byte.toUnsignedInt(specialMissionCoordinateTable[tableIndex + 1]);
        return ((y & 0xFF) << 8) | (x & 0xFF);
    }

    /**
     * Native support extracted from MissionScriptRuntime::processSpecialMissionStatus @0056F492 hidden-runtime reset.
     * Fully ported.
     */
    public static void clearHiddenRuntimeState(Unit unit) {
        if (unit.missionRuntimeState.runtimeState == 0xFF) {
            unit.missionRuntimeState.runtimeState = 0;
        }
    }

    /**
     * Native: MissionScriptRuntime::processMissionGroupPostState @0057377D.
     * Fully ported.
     */
    public void processMissionGroupPostState(UnitGroup group) {
        for (Unit unit : group.units) {
            processMissionUnitPostState(unit);
        }
    }

    /**
     * Native: MissionScriptRuntime::processMissionUnitPostState @00573713.
     * Fully ported.
     */
    public void processMissionUnitPostState(Unit unit) {
        if (shouldRunWimpyRetreat(unit)) {
            processWimpyRetreat(unit);
        } else if (shouldRunWithdrawRetreat(unit)) {
            processWithdrawRetreat(unit);
        }
    }

    /**
     * Native: MissionScriptRuntime::shouldRunWimpyRetreat @0056CB0C.
     * Fully ported.
     */
    public boolean shouldRunWimpyRetreat(Unit unit) {
        if ((short) unit.m_nHP <= unit.missionRuntimeState.wimpy) {
            UnitList candidates = collectFilteredCandidates(
                    unit,
                    unit.m_pTargetHandle.getCell(),
                    missionScanRadius(unit) + WIMPY_SCAN_RADIUS_BONUS,
                    0
            );
            return !candidates.isEmpty();
        }
        return false;
    }

    /**
     * Native: MissionScriptRuntime::shouldRunWithdrawRetreat @0056CD11.
     * Fully ported.
     */
    public boolean shouldRunWithdrawRetreat(Unit unit) {
        if ((short) unit.m_nHP <= unit.missionRuntimeState.withdraw) {
            UnitList candidates = collectFilteredCandidates(unit, unit.m_pTargetHandle.getCell(), WITHDRAW_SCAN_RADIUS, 0);
            return !candidates.isEmpty();
        }
        return false;
    }

    /**
     * Native: MissionScriptRuntime::processWimpyRetreat @0056C9BC.
     * Fully ported.
     */
    public void processWimpyRetreat(Unit unit) {
        if (unit.spellbook != null && findAffordableSpell(unit, SpellId.TELEPORT.id) != null) {
            processSpellAwareWimpyRetreat(unit);
            return;
        }
        UnitList candidates = collectFilteredCandidates(
                unit,
                unit.m_pTargetHandle.getCell(),
                missionScanRadius(unit) + WIMPY_SCAN_RADIUS_BONUS,
                0
        );
        if (!hasAliveCandidate(candidates)) {
            setUnitDefaultMissionAction(unit, 0);
            return;
        }
        int averagePackedPosition = averageUnitListPackedPosition(candidates);
        int retreatCell = worldMap.projectCellAwayFromPackedPosition(
                unit,
                averagePackedPosition,
                RETREAT_PROJECT_DISTANCE_TILES
        );
        queueMissionMoveToCell(unit, retreatCell);
    }

    /**
     * Native: MissionScriptRuntime::processSpellAwareWimpyRetreat @0056CB8C.
     * Fully ported.
     */
    public void processSpellAwareWimpyRetreat(Unit unit) {
        UnitList candidates = collectHostileCandidatesAtCell(unit, unit.m_pTargetHandle.getCell());
        if (!hasAliveCandidate(candidates)) {
            setUnitDefaultMissionAction(unit, 0);
            return;
        }
        int averagePackedPosition = averageUnitListPackedPosition(candidates);
        int retreatCell = worldMap.projectCellAwayFromPackedPosition(
                unit,
                averagePackedPosition,
                (unit.sightRange + 1) & 0xFF
        );
        queueRetreatSpellOrMove(unit, retreatCell);
    }

    /**
     * Native: MissionScriptRuntime::queueMoveToCellOrRetreatFromCurrentHostiles @0056CD81.
     * Fully ported.
     */
    public void queueMoveToCellOrRetreatFromCurrentHostiles(Unit unit, int fallbackCell) {
        UnitList candidates = collectHostileCandidatesAtCell(unit, unit.m_pTargetHandle.getCell());
        if (candidates.isEmpty()) {
            queueMissionMoveToCell(unit, fallbackCell);
            return;
        }
        int averagePackedPosition = averageUnitListPackedPosition(candidates);
        int retreatCell = worldMap.projectCellAwayFromPackedPosition(
                unit,
                averagePackedPosition,
                RETREAT_PROJECT_DISTANCE_TILES
        );
        queueMissionMoveToCell(unit, retreatCell);
    }

    /**
     * Native: MissionScriptRuntime::processWithdrawRetreat @0056CE0F.
     * Fully ported.
     */
    public void processWithdrawRetreat(Unit unit) {
        if (unit.spellbook != null && findAffordableSpell(unit, SpellId.TELEPORT.id) != null) {
            processSpellAwareWithdrawRetreat(unit);
            return;
        }
        UnitList candidates = collectFilteredCandidates(unit, unit.m_pTargetHandle.getCell(), WITHDRAW_SCAN_RADIUS, 0);
        if (candidates.isEmpty()) {
            engageNearestTarget(unit);
            return;
        }
        int averagePackedPosition = averageUnitListPackedPosition(candidates);
        int retreatCell = worldMap.projectCellAwayFromPackedPosition(
                unit,
                averagePackedPosition,
                RETREAT_PROJECT_DISTANCE_TILES
        );
        queueMissionMoveToCell(unit, retreatCell);
    }

    /**
     * Native: MissionScriptRuntime::processSpellAwareWithdrawRetreat @0056CEF3.
     * Fully ported.
     */
    public void processSpellAwareWithdrawRetreat(Unit unit) {
        UnitList candidates = collectFilteredCandidates(unit, unit.m_pTargetHandle.getCell(), WITHDRAW_SCAN_RADIUS, 0);
        if (candidates.isEmpty()) {
            engageNearestTarget(unit);
            return;
        }
        int averagePackedPosition = averageUnitListPackedPosition(candidates);
        int retreatCell = worldMap.projectCellAwayFromPackedPosition(
                unit,
                averagePackedPosition,
                (unit.sightRange - 1) & 0xFF
        );
        queueRetreatSpellOrMove(unit, retreatCell);
    }

    /**
     * Native: MissionScriptRuntime::collectHostileCandidatesAtCell @00567988.
     * Fully ported.
     */
    public UnitList collectHostileCandidatesAtCell(Unit unit, int cell) {
        UnitList candidates = worldMap.collectUnitsAroundCell(cell, missionScanRadius(unit));
        filterCandidateUnits(unit, candidates, 0);
        return candidates;
    }

    /**
     * Native: MissionScriptRuntime::collectNonHostileCandidatesAtCell @005679CF.
     * Fully ported.
     */
    public UnitList collectNonHostileCandidatesAtCell(Unit unit, int cell) {
        UnitList candidates = worldMap.collectUnitsAroundCell(cell, missionScanRadius(unit));
        filterCandidateUnits(unit, candidates, 1);
        return candidates;
    }

    /**
     * Native: MissionScriptRuntime::collectUnitsAroundUnitScanRadius @00567911.
     * Fully ported.
     */
    public UnitList collectUnitsAroundUnitScanRadius(Unit unit) {
        return worldMap.collectUnitsAroundCell(unit.m_pTargetHandle.getCell(), missionScanRadius(unit));
    }

    /**
     * Native: MissionScriptRuntime::collectFilteredHostileCandidatesAroundUnit @00567945.
     * Fully ported.
     */
    public UnitList collectFilteredHostileCandidatesAroundUnit(Unit unit) {
        UnitList candidates = collectUnitsAroundUnitScanRadius(unit);
        filterCandidateUnits(unit, candidates, 0);
        removeBuildingCandidatesForMeleeUnit(unit, candidates);
        return candidates;
    }

    /**
     * Native: MissionScriptRuntime::removeBuildingCandidatesForMeleeUnit @00567F49.
     * Fully ported.
     */
    public void removeBuildingCandidatesForMeleeUnit(Unit unit, UnitList candidates) {
        if ((getCastRangeForFirstCastableSpellOrFallback(unit) & 0xFF) >= 2) {
            return;
        }
        for (int index = 0; index < candidates.size(); ) {
            Unit candidate = candidates.get(index);
            if ((candidate.getTokenTypeId() & 0xFF) == 3) {
                candidates.remove(index);
            } else {
                index++;
            }
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::collectHostileCandidatesAtCell @00567988,
     * MissionScriptRuntime::collectNonHostileCandidatesAtCell @005679CF, and
     * MissionScriptRuntime::filterCandidateUnits @00567A16.
     * Fully ported.
     */
    public UnitList collectFilteredCandidates(Unit unit, int sourceCell, int radius, int removedRelationFlag) {
        UnitList candidates = worldMap.collectUnitsAroundCell(sourceCell, radius);
        filterCandidateUnits(unit, candidates, removedRelationFlag);
        return candidates;
    }

    /**
     * Native: MissionScriptRuntime::filterCandidateUnits @00567A16.
     * Fully ported.
     */
    public void filterCandidateUnits(Unit unit, UnitList candidates, int removedRelationFlag) {
        for (int index = 0; index < candidates.size(); ) {
            Unit candidate = candidates.get(index);
            boolean relationMatches = missionDiplomacyState.hasHostileRelation(unit, candidate);
            boolean remove = relationMatches == (removedRelationFlag != 0);
            if (!remove && (candidate.effectKeyFlags & spellEffectKeyMask(SpellId.INVISIBILITY)) != 0) {
                remove = !isInvisibleCandidateVisible(unit, candidate);
            }
            if (remove) {
                candidates.remove(index);
            } else {
                index++;
            }
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::filterCandidateUnits @00567A16 invisible-unit branch.
     * Fully ported.
     */
    public boolean isInvisibleCandidateVisible(Unit unit, Unit candidate) {
        for (Unit groupUnit : unit.unitGroup.units) {
            int range = worldMap.getRangeInTiles(candidate, groupUnit) & 0xFF;
            if (range <= (groupUnit.missionRuntimeState.seeInvisible & 0xFF)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Native support extracted from MissionScriptRuntime::processWimpyRetreat @0056C9BC and
     * MissionScriptRuntime::processSpellAwareWimpyRetreat @0056CB8C.
     */
    public static boolean hasAliveCandidate(UnitList candidates) {
        for (Unit candidate : candidates) {
            if (candidate.m_nHP > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Native support extracted from averageUnitListPackedPosition @0056F765.
     * Fully ported.
     */
    public static int averageUnitListPackedPosition(UnitList units) {
        int count = units.size() & 0xFF;
        if (count == 0) {
            return 0;
        }
        int totalXdX = 0;
        int totalYdY = 0;
        for (Unit unit : units) {
            totalXdX += unit.m_pTargetHandle.packXdX() & 0xFFFF;
            totalYdY += unit.m_pTargetHandle.packYdY() & 0xFFFF;
        }
        return (totalYdY / count) * 0x10000 + totalXdX / count;
    }

    /**
     * Native support extracted from global averageUnitGroupPackedPosition @00570F50.
     * Fully ported.
     */
    public static int averageUnitGroupPackedPosition(UnitGroup group) {
        int count = group.units.size() & 0xFF;
        int totalXdX = 0;
        int totalYdY = 0;
        for (Unit unit : group.units) {
            totalXdX += unit.m_pTargetHandle.packXdX() & 0xFFFF;
            totalYdY += unit.m_pTargetHandle.packYdY() & 0xFFFF;
        }
        return (totalYdY / count) * 0x10000 + totalXdX / count;
    }

    /**
     * Native support extracted from global averageUnitGroupCellFromPackedPosition @00571005.
     * Fully ported.
     */
    public static int averageUnitGroupCellFromPackedPosition(UnitGroup group) {
        int count = group.units.size() & 0xFF;
        int totalXdX = 0;
        int totalYdY = 0;
        for (Unit unit : group.units) {
            totalXdX += unit.m_pTargetHandle.packXdX() & 0xFFFF;
            totalYdY += unit.m_pTargetHandle.packYdY() & 0xFFFF;
        }
        int averageXdX = totalXdX / count;
        int averageYdY = totalYdY / count;
        return (averageYdY & 0xFF00) | ((averageXdX >>> 8) & 0xFF);
    }

    /**
     * Native support extracted from global computeMutualMeleeDurabilityScore @005710D8.
     * Fully ported.
     */
    public static int computeMutualMeleeDurabilityScore(Unit first, Unit second) {
        return (int) (oneWayMeleeDurabilityScore(first, second) + oneWayMeleeDurabilityScore(second, first));
    }

    /**
     * Native support extracted from global computeMutualMeleeDurabilityScore @005710D8.
     * Fully ported.
     */
    private static double oneWayMeleeDurabilityScore(Unit attacker, Unit defender) {
        int hitAdvantage = (attacker.skillData.toHit + 0x32) - defender.unitStatData.absorbtion;
        if (hitAdvantage > 100) {
            hitAdvantage = 100;
        }
        if (hitAdvantage < 1) {
            return 30000.0;
        }
        int damage = (attacker.skillData.skillDamageType0And3Min & 0xFF)
                + (attacker.skillData.skillDamageType0And3Modifier & 0xFF);
        return (((short) defender.m_nMaxHP << 1) / damage) * 100.0 / hitAdvantage;
    }

    /**
     * Native support extracted from MissionScriptRuntime::collectHostileCandidatesAtCell @00567988 and
     * MissionScriptRuntime::shouldRunWimpyRetreat @0056CB0C.
     */
    public static int missionScanRadius(Unit unit) {
        return unit.movementState.missionScanRadius & 0xFF;
    }

    /**
     * Native support extracted from MissionScriptRuntime::findAffordableSpell @0056BB0D.
     * Fully ported.
     */
    public static Spell findAffordableSpell(Unit unit, int spellId) {
        Spell spell = unit.spellbook.find(spellId);
        if (spell == null || (short) unit.m_nMP < (short) spell.manaCost) {
            return null;
        }
        return spell;
    }

    /**
     * Native support extracted from MissionScriptRuntime::processSpellAwareWimpyRetreat @0056CB8C and
     * MissionScriptRuntime::processSpellAwareWithdrawRetreat @0056CEF3.
     */
    public void queueRetreatSpellOrMove(Unit unit, int retreatCell) {
        Spell spell = findAffordableSpell(unit, SpellId.TELEPORT.id);
        if (spell != null) {
            unit.missionRuntimeState.virtualCasterQueuedFlag = 1;
            queueCellSpellCast(unit, retreatCell, spell);
            return;
        }
        spell = findAffordableSpell(unit, SpellId.INVISIBILITY.id);
        if (spell != null && (unit.effectKeyFlags & RETREAT_SELF_EFFECT_KEY_MASK) == 0) {
            unit.missionRuntimeState.virtualCasterQueuedFlag = 1;
            queueTargetedSpellCast(unit, unit, spell);
            return;
        }
        queueMissionMoveToCell(unit, retreatCell);
    }

    /**
     * Native: MissionScriptRuntime::queueTargetedSpellCast @0056C72B.
     * Fully ported.
     */
    public void queueTargetedSpellCast(Unit caster, Token target, Spell spell) {
        Token resolvedTarget = target;
        if (resolvedTarget == null) {
            UnitList candidates = spell.isDefensive
                    ? collectNonHostileCandidatesAtCell(caster, caster.m_pTargetHandle.getCell())
                    : collectHostileCandidatesAtCell(caster, caster.m_pTargetHandle.getCell());
            if (!candidates.isEmpty()) {
                resolvedTarget = candidates.getFirst();
            }
        }
        if (resolvedTarget == null) {
            setUnitDefaultMissionAction(caster, 0);
        } else {
            caster.missionRuntimeState.command = UNIT_MISSION_COMMAND_TARGETED_SPELL;
            caster.missionRuntimeState.spellTargetToken = resolvedTarget;
            caster.missionRuntimeState.spell = spell;
            Unit targetUnit = (Unit) resolvedTarget;
            targetUnit.missionRuntimeState.lastIncomingSpellId = Byte.toUnsignedInt(spell.id);
            targetUnit.missionRuntimeState.lastIncomingSpellTick = Globals.gameServer.serverLoopCounter;
        }
        caster.missionRuntimeState.attackRange = spell.maxRange & 0xFF;
    }

    /**
     * Native: MissionScriptRuntime::queueCellSpellCast @0056969C.
     * Fully ported.
     */
    public static void queueCellSpellCast(Unit caster, int cell, Spell spell) {
        caster.missionRuntimeState.command = UNIT_MISSION_COMMAND_CELL_SPELL;
        caster.missionRuntimeState.cellSpellTargetCell = cell & 0xFFFF;
        caster.missionRuntimeState.spell = spell;
        caster.missionRuntimeState.attackRange = spell.maxRange & 0xFF;
    }

    /**
     * Native: MissionScriptRuntime::engageNearestTarget @0056A1FB.
     * Fully ported.
     */
    public void engageNearestTarget(Unit unit) {
        Unit target = findNearestHostileTarget(unit);
        if (target == null) {
            prepareUnitDefaultMissionAction(unit);
        } else {
            queueNearestEngagementTarget(unit, target);
        }
        refreshUnitFootprintAfterNearestEngagement(unit);
    }

    /**
     * Native support extracted from MissionScriptRuntime::engageNearestTarget @0056A1FB tail.
     * Fully ported.
     */
    public void refreshUnitFootprintAfterNearestEngagement(Unit unit) {
        if (unit.m_pTargetHandle.isSubPosUnknown() && unit.movementState.cell != 0) {
            int targetCell = unit.m_pTargetHandle.getCell();
            if ((unit.movementState.cell & 0xFFFF) != targetCell) {
                worldMap.refreshUnitCellFromTargetHandle(unit);
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::updateUnitMissionRuntime @0056DCA2.
     * Fully ported.
     */
    public void updateUnitMissionRuntime(Unit unit) {
        if (forceAllMissionGroupsUpdate == 0 && !unit.unitGroup.missionState.isDamagedUnitMissionUpdatePending()
                && unit.missionRuntimeState.runtimeState == 0) {
            unit.state = UnitActionState.MISSION_HIDDEN;
            return;
        }
        if (unit.state != UnitActionState.ATTACK && unit.state != UnitActionState.INTERACT) {
            unit.state = UnitActionState.DYING;
        }
        if ((unit.effectKeyFlags & EFFECT_FLAG_MISSION_HIDDEN) != 0 && unit.missionRuntimeState.runtimeState == 0) {
            unit.missionRuntimeState.runtimeState = UNIT_MISSION_RUNTIME_STATE_EFFECT_HIDDEN;
        }
        if (unit.missionRuntimeState.runtimeState == 0) {
            if (unit.missionRuntimeState.pendingMissionEntryCell != 0 && unit.m_pTargetHandle.isSubPosUnknown()) {
                worldMap.relocateScenarioMissionEntryUnit(unit, unit.missionRuntimeState.pendingMissionEntryCell);
                unit.missionRuntimeState.pendingMissionEntryCell = 0;
            }
            switch (unit.missionRuntimeState.command) {
                case UNIT_MISSION_COMMAND_MOVE_TO_CELL -> applyMoveToCellMissionCommand(unit);
                case UNIT_MISSION_COMMAND_MOVE_TO_TARGET_UNIT -> beginMoveToTargetUnit(unit);
                case UNIT_MISSION_COMMAND_REPATH_TO_TARGET ->
                        requestEngagementOrRepath(unit, unit.missionRuntimeState.repathTargetToken);
                case UNIT_MISSION_COMMAND_ENGAGE_TARGET -> updateMoveOrEngageTarget(unit);
                case UNIT_MISSION_COMMAND_NEAREST_ENGAGEMENT -> applyNearestEngagementMissionCommand(unit);
                case UNIT_MISSION_COMMAND_PICKUP_TRANSITION -> applyPickupTransitionMissionCommand(unit);
                case UNIT_MISSION_COMMAND_TARGETED_SPELL -> updateTargetedSpellMissionCommand(unit);
                case UNIT_MISSION_COMMAND_CELL_SPELL -> updateCellSpellMissionCommand(unit);
                case UNIT_MISSION_COMMAND_FACE_LAST -> applyFaceLastMissionCommand(unit, unit.movementState.facingLast);
                case UNIT_MISSION_COMMAND_LOOK_AROUND -> applyLookAroundMissionCommand(unit);
                case UNIT_MISSION_COMMAND_ATTACK_COMMAND_CELL -> applyAttackCommandCellMissionCommand(unit);
                case UNIT_MISSION_COMMAND_INTERACT_COMMAND_CELL -> applyInteractCommandCellMissionCommand(unit);
                default -> {
                }
            }
        } else if (unit.missionRuntimeState.runtimeState == UNIT_MISSION_RUNTIME_STATE_MOVING) {
            applyMovingMissionRuntimeState(unit);
        } else if (unit.missionRuntimeState.runtimeState == UNIT_MISSION_RUNTIME_STATE_SPELL_ACTION) {
            applySpellActionMissionRuntimeState(unit);
        } else if (unit.missionRuntimeState.runtimeState == UNIT_MISSION_RUNTIME_STATE_PATHING) {
            applyPathingMissionRuntimeState(unit);
        } else if (unit.missionRuntimeState.runtimeState == UNIT_MISSION_RUNTIME_STATE_EFFECT_HIDDEN) {
            applyEffectHiddenMissionRuntimeState(unit);
        } else if (unit.missionRuntimeState.runtimeState == 0xFF) {
            unit.state = UnitActionState.MISSION_HIDDEN;
        } else {
            unit.missionRuntimeState.runtimeState = 0;
            unit.state = UnitActionState.MISSION_HIDDEN;
        }
        processMissionMovementEventTail(unit);
    }

    /**
     * Native: MissionScriptRuntime::updateActiveUnitMissionRuntimes @0056DC49.
     * Fully ported.
     */
    public void updateActiveUnitMissionRuntimes() {
        for (Unit unit : worldMap.activeUnits0xA456C) {
            updateUnitMissionRuntime(unit);
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::updateUnitMissionRuntime @0056DCA2 command `1`.
     * Fully ported.
     */
    public void applyMoveToCellMissionCommand(Unit unit) {
        requestMissionMoveToCell(unit);
    }

    /**
     * Native: MissionScriptRuntime::beginMoveToTargetUnit @0056E8C3.
     * Fully ported.
     */
    public void beginMoveToTargetUnit(Unit unit) {
        unit.missionRuntimeState.runtimeState = UNIT_MISSION_RUNTIME_STATE_MOVING;
        unit.missionRuntimeState.commandStateByte = 0;
        unit.state = UnitActionState.MOVE;
        unit.actionTarget = unit.missionRuntimeState.targetToken;
    }

    /**
     * Native: MissionScriptRuntime::getCastRangeForFirstCastableSpellOrFallback @0056AC5B.
     * Fully ported.
     */
    public int getCastRangeForFirstCastableSpellOrFallback(Unit unit) {
        return unit.getCastRangeForFirstCastableSpellOrFallbackSupport();
    }

    /**
     * Native: MissionScriptRuntime::initializeUnitBattlePreferenceDefaults @0057382C.
     * Fully ported.
     */
    public void initializeUnitBattlePreferenceDefaults(Unit unit) {
        unit.initializeUnitBattlePreferenceDefaults(this);
    }

    /**
     * Native: MissionScriptRuntime::updateMoveOrEngageTarget @0056EAB5.
     * Fully ported.
     */
    public void updateMoveOrEngageTarget(Unit unit) {
        Token target = unit.missionRuntimeState.targetToken;
        int range = getCastRangeForFirstCastableSpellOrFallback(unit) & 0xFF;
        if (!worldMap.isFacingUnitInRange(unit, target, range)) {
            requestEngagementOrRepath(unit, target);
            unit.state = UnitActionState.IDLE;
        } else {
            beginMoveToTargetUnit(unit);
            unit.state = UnitActionState.MOVE;
        }
    }

    /**
     * Native: MissionScriptRuntime::requestEngagementOrRepath @0056EA52.
     * Fully ported.
     */
    public void requestEngagementOrRepath(Unit unit, Token target) {
        worldMap.requestTargetEngagementPath(unit, target, unit.missionRuntimeState.attackRange);
        if (!unit.m_pTargetHandle.isSubPosUnknown()) {
            unit.missionRuntimeState.runtimeState = UNIT_MISSION_RUNTIME_STATE_PATHING;
            unit.missionRuntimeState.commandStateByte = 0;
        }
        unit.state = UnitActionState.IDLE;
    }

    /**
     * Native: MissionScriptRuntime::applyPickupTransitionMissionCommand @0056EA24.
     * Fully ported.
     */
    public void applyPickupTransitionMissionCommand(Unit unit) {
        if (unit.state == UnitActionState.ATTACK) {
            reenterScenarioMissionEntryUnit(unit);
        } else {
            unit.state = UnitActionState.ATTACK;
        }
    }

    /**
     * Native: MissionScriptRuntime::applyNearestEngagementMissionCommand @0056EFF3.
     * Fully ported.
     */
    public void applyNearestEngagementMissionCommand(Unit unit) {
        if (unit.isMageClass()
                && (getCastRangeForFirstCastableSpellOrFallback(unit) & 0xFF) < 2
                && unit.owner.isActive == 0) {
            unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
            return;
        }
        Unit target = (Unit) unit.missionRuntimeState.targetToken;
        int range = getCastRangeForFirstCastableSpellOrFallback(unit) & 0xFF;
        if (!worldMap.isFacingUnitInRange(unit, target, range)) {
            rotateUnitTowardFacingAndMarkIdle(unit, worldMap.getDirection8Code(unit, target));
            unit.state = UnitActionState.IDLE;
        } else {
            beginMoveToTargetUnit(unit);
            unit.state = UnitActionState.MOVE;
        }
    }

    /**
     * Native: MissionScriptRuntime::updateTargetedSpellMissionCommand @0056EBC0.
     * Fully ported.
     */
    public void updateTargetedSpellMissionCommand(Unit unit) {
        Token target = unit.missionRuntimeState.spellTargetToken;
        if (unit != target && !worldMap.isFacingUnitInRange(unit, target, unit.missionRuntimeState.attackRange)) {
            requestEngagementOrRepath(unit, target);
            unit.state = UnitActionState.IDLE;
            return;
        }
        beginTargetedSpellMissionAction(unit);
    }

    /**
     * Native: MissionScriptRuntime::updateCellSpellMissionCommand @0056EC3F.
     * Fully ported.
     */
    public void updateCellSpellMissionCommand(Unit unit) {
        if (!worldMap.isFacingCellInRange(
                unit,
                unit.missionRuntimeState.cellSpellTargetCell,
                unit.missionRuntimeState.attackRange
        )) {
            requestMissionMoveToCell(unit, unit.missionRuntimeState.cellSpellTargetCell, unit.missionRuntimeState.attackRange);
            unit.state = UnitActionState.IDLE;
            return;
        }
        beginCellSpellMissionAction(unit);
    }

    /**
     * Native support extracted from MissionScriptRuntime::updateUnitMissionRuntime @0056DCA2 state `1`.
     * Fully ported.
     */
    public static void applyMovingMissionRuntimeState(Unit unit) {
        unit.state = UnitActionState.MOVE;
        unit.missionRuntimeState.commandStateByte = (unit.missionRuntimeState.commandStateByte + 1) & 0xFF;
        if (unit.missionRuntimeState.commandStateByte > 2 && unit.actionReadyFlag != 0) {
            unit.missionRuntimeState.runtimeState = 0;
            unit.state = UnitActionState.DYING;
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::updateUnitMissionRuntime @0056DCA2 state `2`.
     * Fully ported.
     */
    public void applySpellActionMissionRuntimeState(Unit unit) {
        unit.state = unit.missionRuntimeState.spellActionMode == 0 ? UnitActionState.USE_SKILL : UnitActionState.CAST_SPELL;
        unit.missionRuntimeState.commandStateByte = (unit.missionRuntimeState.commandStateByte + 1) & 0xFF;
        if (unit.missionRuntimeState.commandStateByte > 2) {
            if (unit.missionRuntimeState.commandStateByte == 3) {
                appendSpellActionMissionUnitEventDTO(unit);
            }
            if (unit.actionReadyFlag != 0) {
                unit.missionRuntimeState.runtimeState = 0;
                unit.state = UnitActionState.DYING;
            }
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::updateUnitMissionRuntime @0056DCA2 state `2` DTO append.
     * Fully ported.
     */
    public void appendSpellActionMissionUnitEventDTO(Unit unit) {
        if (unit.missionRuntimeState.spellActionMode == 0) {
            appendCellSpellActionMissionUnitEventDTO(unit);
        } else {
            appendTargetedSpellActionMissionUnitEventDTO(unit);
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::updateUnitMissionRuntime @0056DCA2
     * spellActionMode `0` DTO append.
     * Fully ported.
     */
    public void appendCellSpellActionMissionUnitEventDTO(Unit unit) {
        scratchMissionUnitEventDTO.eventCode = MISSION_UNIT_EVENT_CELL_SPELL_ACTION;
        scratchMissionUnitEventDTO.runtimeSource = unit;
        scratchMissionUnitEventDTO.runtimePayload = unit.spell;
        scratchMissionUnitEventDTO.cellTargetX = unit.skillTargetX;
        scratchMissionUnitEventDTO.cellTargetY = unit.skillTargetY;
        pendingMissionUnitEventDTOs.add(scratchMissionUnitEventDTO.copy());
    }

    /**
     * Native support extracted from MissionScriptRuntime::updateUnitMissionRuntime @0056DCA2
     * nonzero spellActionMode DTO append.
     * Fully ported.
     */
    public void appendTargetedSpellActionMissionUnitEventDTO(Unit unit) {
        scratchMissionUnitEventDTO.eventCode = MISSION_UNIT_EVENT_TARGETED_SPELL_ACTION;
        scratchMissionUnitEventDTO.runtimeSource = unit;
        scratchMissionUnitEventDTO.runtimePayload = unit.spell;
        scratchMissionUnitEventDTO.runtimeActionTarget = unit.actionTarget;
        pendingMissionUnitEventDTOs.add(scratchMissionUnitEventDTO.copy());
    }

    /**
     * Native support extracted from MissionScriptRuntime::updateUnitMissionRuntime @0056DCA2 state `3`.
     * Fully ported.
     */
    public void applyPathingMissionRuntimeState(Unit unit) {
        worldMap.updateUnitMissionPathProgress(unit);
        unit.state = UnitActionState.IDLE;
        if (unit.m_pTargetHandle.isSubPosUnknown()) {
            unit.missionRuntimeState.runtimeState = 0;
            unit.state = UnitActionState.DYING;
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::updateUnitMissionRuntime @0056DCA2 state `4`.
     * Fully ported.
     */
    public static void applyEffectHiddenMissionRuntimeState(Unit unit) {
        unit.state = UnitActionState.MISSION_HIDDEN;
        if ((unit.effectKeyFlags & EFFECT_FLAG_MISSION_HIDDEN) == 0) {
            unit.missionRuntimeState.runtimeState = 0;
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::updateUnitMissionRuntime @0056DCA2 tail.
     * Fully ported.
     */
    public void processMissionMovementEventTail(Unit unit) {
        if (unit.movementState.movementEventPending == 0) {
            return;
        }
        unit.movementState.movementEventPending = 0;
        if (unit.missionActionCode == MOVE_TO_CELL) {
            restartScenarioMissionEntryUnit(unit);
        } else if (unit.missionActionCode == WAYPOINT) {
            unit.missionRuntimeState.waypointRefreshFlag = 1;
            unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
            advanceNearbyMissionWaypointTarget(unit);
        } else if (unit.missionActionCode == HIDE) {
            unit.missionRuntimeState.runtimeState = 0xFF;
        } else {
            unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
            retargetAfterMissionMovementEvent(unit);
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::updateUnitMissionRuntime @0056DCA2 status `10` tail.
     * Fully ported.
     */
    public void advanceNearbyMissionWaypointTarget(Unit unit) {
        if (cellChebyshevDistance(unit.m_pTargetHandle.getCell(), unit.missionRuntimeState.waypointTargetCell) < 3) {
            unit.missionRuntimeState.waypointTargetCell = nextMissionWaypointCell(unit, unit.missionRuntimeState.waypointTargetCell);
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::processMissionWaypointStatus @00569B62.
     * Fully ported.
     */
    public static int nextMissionWaypointCell(Unit unit, int currentCell) {
        int index = unit.missionRuntimeState.waypointCells.indexOf((short) currentCell);
        if (index < 0) {
            throw new IllegalStateException("Mission waypoint target cell not present in waypoint list: "
                    + (currentCell & 0xFFFF));
        }
        int nextIndex = index + 1 >= unit.missionRuntimeState.waypointCells.size() ? 0 : index + 1;
        return unit.missionRuntimeState.waypointCells.get(nextIndex) & 0xFFFF;
    }

    /**
     * Native: MissionScriptRuntime::retargetAfterMissionMovementEvent @0056ED4E.
     * Fully ported.
     */
    public void retargetAfterMissionMovementEvent(Unit unit) {
        primaryCandidateUnits.clear();
        int nearestRange = 0xFF;
        if (worldMap.activeUnits0xA456C != null) {
            for (Unit candidate : worldMap.activeUnits0xA456C) {
                int range = unit.m_pTargetHandle.chebyshevDistanceByXY(candidate.m_pTargetHandle) & 0xFF;
                if (range <= (getCastRangeForFirstCastableSpellOrFallback(unit) & 0xFF)
                        && range <= nearestRange
                        && candidate != unit) {
                    primaryCandidateUnits.add(candidate);
                    nearestRange = range;
                }
            }
        }
        filterCandidateUnits(unit, primaryCandidateUnits, 0);
        moveDeadCandidatesToFallbackList();
        promoteFallbackCandidatesIfPrimaryEmpty();
        Unit selected = selectLowestFacingDeltaCandidate(unit);
        if (selected == null) {
            if (unit.owner.isActive == 0) {
                unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
            } else {
                unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_LOOK_AROUND;
                applyLookAroundMissionCommand(unit);
            }
        } else {
            unit.missionRuntimeState.targetToken = selected;
            unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_NEAREST_ENGAGEMENT;
            applyNearestEngagementMissionCommand(unit);
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::retargetAfterMissionMovementEvent @0056ED4E candidate facing selection.
     * Fully ported.
     */
    public Unit selectLowestFacingDeltaCandidate(Unit unit) {
        Unit selected = null;
        int selectedDelta = 300;
        for (Unit candidate : primaryCandidateUnits) {
            int direction = worldMap.getDirection8Code(unit, candidate);
            int delta = worldMap.getFacingAngularDistance8(unit.movementState.facing, direction) & 0xFF;
            if (delta <= selectedDelta) {
                selected = candidate;
                selectedDelta = delta;
            }
        }
        return selected;
    }

    /**
     * Native: MissionScriptRuntime::beginTargetedSpellMissionAction @0056E906.
     * Fully ported.
     */
    public void beginTargetedSpellMissionAction(Unit unit) {
        unit.missionRuntimeState.runtimeState = UNIT_MISSION_RUNTIME_STATE_SPELL_ACTION;
        unit.missionRuntimeState.commandStateByte = 0;
        unit.state = UnitActionState.CAST_SPELL;
        unit.actionTarget = unit.missionRuntimeState.spellTargetToken;
        unit.spell = unit.missionRuntimeState.spell;
        unit.missionRuntimeState.spellActionMode = 1;
        if (unit.missionRuntimeState.virtualCasterQueuedFlag == 0) {
            reenterScenarioMissionEntryUnit(unit);
        }
    }

    /**
     * Native: MissionScriptRuntime::beginCellSpellMissionAction @0056E986.
     * Fully ported.
     */
    public void beginCellSpellMissionAction(Unit unit) {
        unit.missionRuntimeState.runtimeState = UNIT_MISSION_RUNTIME_STATE_SPELL_ACTION;
        unit.missionRuntimeState.commandStateByte = 0;
        unit.state = UnitActionState.USE_SKILL;
        unit.skillTargetX = unit.missionRuntimeState.cellSpellTargetCell & 0xFF;
        unit.skillTargetY = (unit.missionRuntimeState.cellSpellTargetCell >>> 8) & 0xFF;
        unit.spell = unit.missionRuntimeState.spell;
        unit.missionRuntimeState.spellActionMode = 0;
        if (unit.missionRuntimeState.virtualCasterQueuedFlag == 0) {
            reenterScenarioMissionEntryUnit(unit);
        }
    }

    /**
     * Native: MissionScriptRuntime::requestMissionMoveToCell @0056E4AC.
     * Fully ported.
     */
    public void requestMissionMoveToCell(Unit unit) {
        requestMissionMoveToCell(unit, unit.missionRuntimeState.commandCell, 0);
    }

    /**
     * Native: MissionScriptRuntime::requestMissionMoveToCell @0056E4AC.
     * Fully ported.
     */
    public void requestMissionMoveToCell(Unit unit, int cell, int stopRange) {
        int targetCell = cell & 0xFFFF;
        unit.movementState.movementRequestPending = 1;
        if (unit.movementState.pathTargetCell != targetCell) {
            unit.movementState.pathSearchRefreshCount = 0xFF;
        }
        worldMap.requestMissionMoveToCell(unit, targetCell, stopRange & 0xFF);
        if (!unit.m_pTargetHandle.isSubPosUnknown()) {
            unit.missionRuntimeState.runtimeState = UNIT_MISSION_RUNTIME_STATE_PATHING;
            unit.missionRuntimeState.commandStateByte = 0;
        }
        unit.state = UnitActionState.IDLE;
    }

    /**
     * Native: MissionScriptRuntime::applyLookAroundMissionCommand @0056E5BC.
     * Fully ported.
     */
    public void applyLookAroundMissionCommand(Unit unit) {
        if ((unit.movementState.facing & 0xFF) == (unit.movementState.facingLast & 0xFF)) {
            if (unit.missionRuntimeState.engagementActiveFlag == 0 && randomRaw() > 0xCC) {
                unit.state = UnitActionState.DYING;
                return;
            }
            unit.movementState.facingLast = (unit.movementState.facing + 0x21 + randomBelow(0xBE)) & 0xFF;
            unit.missionRuntimeState.engagementActiveFlag = 0;
        }
        rotateUnitTowardFacingLast(unit);
        unit.state = UnitActionState.IDLE;
    }

    /**
     * Native: MissionScriptRuntime::applyFaceLastMissionCommand @0056E55D.
     * Fully ported.
     */
    public void applyFaceLastMissionCommand(Unit unit, int facingLast) {
        if ((unit.movementState.facing & 0xFF) == (facingLast & 0xFF)) {
            unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
            unit.state = UnitActionState.DYING;
        } else {
            rotateUnitTowardFacingAndMarkIdle(unit, facingLast);
            unit.state = UnitActionState.IDLE;
        }
    }

    /**
     * Native: MissionScriptRuntime::applyAttackCommandCellMissionCommand @0056EB2D.
     * Fully ported.
     */
    public void applyAttackCommandCellMissionCommand(Unit unit) {
        if (unit.m_pTargetHandle.getCell() == unit.missionRuntimeState.commandCell && unit.m_pTargetHandle.isSubPosUnknown()) {
            if (unit.state == UnitActionState.ATTACK) {
                unit.state = UnitActionState.DYING;
                unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
            } else {
                unit.state = UnitActionState.ATTACK;
            }
        } else {
            requestMissionMoveToCell(unit);
            unit.state = UnitActionState.IDLE;
        }
    }

    /**
     * Native: MissionScriptRuntime::applyInteractCommandCellMissionCommand @0056ECBC.
     * Fully ported.
     */
    public void applyInteractCommandCellMissionCommand(Unit unit) {
        if (!worldMap.isFacingCellInRange(
                unit,
                unit.missionRuntimeState.commandCell,
                unit.missionRuntimeState.attackRange
        )) {
            requestMissionMoveToCell(unit, unit.missionRuntimeState.commandCell, unit.missionRuntimeState.attackRange);
            unit.state = UnitActionState.IDLE;
        } else if (unit.state == UnitActionState.INTERACT) {
            reenterScenarioMissionEntryUnit(unit);
        } else {
            unit.state = UnitActionState.INTERACT;
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::applyFaceLastMissionCommand @0056E55D and
     * MissionScriptRuntime::applyLookAroundMissionCommand @0056E5BC.
     * Fully ported.
     */
    public void rotateUnitTowardFacingLast(Unit unit) {
        rotateUnitTowardFacingAndMarkIdle(unit, unit.movementState.facingLast);
    }

    /**
     * Native: MissionScriptRuntime::rotateUnitTowardFacingAndMarkIdle @0056F0BB.
     * Fully ported.
     */
    public void rotateUnitTowardFacingAndMarkIdle(Unit unit, int facing) {
        int oldFacing = unit.movementState.facing & 0xFF;
        worldMap.rotateUnitTowardFacing(unit, facing);
        if ((unit.movementState.facing & 0xFF) != oldFacing) {
            unit.state = UnitActionState.IDLE;
        }
    }

    /**
     * Native: MissionScriptRuntime::advanceScenarioScripts @0057485B.
     * Fully ported.
     */
    public void advanceScenarioScripts() {
        incrementScriptVariable(SCRIPT_TURN_COUNTER_VARIABLE);
        updateScriptChecks();
        evaluateScriptPatterns();
        pendingMissionUnitEventDTOs.clear();
    }

    /**
     * Native: MissionScriptRuntime::updateScriptChecks @00574899.
     * Fully ported.
     */
    public void updateScriptChecks() {
        for (ScriptCheck check : scriptChecks) {
            if ((check.variableReferenceCount & 0xFF) == 0) {
                applyScriptCheck(check);
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::applyScriptCheck @00574955.
     * Fully ported.
     */
    public void applyScriptCheck(ScriptCheck check) {
        switch (ScriptCheckType.fromId(check.type)) {
            case GROUP_UNIT_COUNT -> applyGroupUnitCountCheck(check);
            case UNIT_IN_RECTANGLE -> applyUnitInRectangleCheck(check);
            case UNIT_NEAR_POINT -> applyUnitNearPointCheck(check);
            case UNIT_HIT_POINTS -> applyUnitHitPointsCheck(check);
            case UNIT_ALIVE -> applyUnitAliveCheck(check);
            case UNIT_DISTANCE -> applyUnitDistanceCheck(check);
            case UNIT_POINT_DISTANCE -> applyUnitPointDistanceCheck(check);
            case PLAYER_UNIT_COUNT -> applyPlayerUnitCountCheck(check);
            case ACTIVE_ENGAGEMENT_TARGET_SCRIPT_ID -> applyActiveEngagementTargetScriptIdCheck(check);
            case NO_OP_11, NO_OP_13 -> {
            }
            case SACK_AT_POINT -> applySackAtPointCheck(check);
            case DIPLOMACY_STATE -> applyDiplomacyStateCheck(check);
            case UNIT_INVENTORY_ITEM -> applyUnitInventoryItemCheck(check);
            case UNIT_INVENTORY_ITEM_DUPLICATE -> applyUnitInventoryItemCheckDuplicate(check);
            case CLOSEST_PLAYER_UNIT_TO_POINT -> applyClosestPlayerUnitToPointCheck(check);
            case INVENTORY_ITEM_DISTANCE -> applyInventoryItemDistanceCheck(check);
            case DEAD_UNIT_FAILURE_COUNTER -> applyDeadUnitFailureCounterCheck(check);
            case SCRIPT_VARIABLE -> applyScriptVariableCheck(check);
            case PLAYER_BUILDING_COUNT -> applyPlayerBuildingCountCheck(check);
            case BUILDING_HIT_POINTS -> applyBuildingHitPointsCheck(check);
            case MISSION_ENTRY_RELOCATION -> applyMissionEntryRelocationCheck(check);
            case SCENARIO_VAR -> applyScenarioVarCheck(check);
            case SCENARIO_TRANSIENT_VAR -> applyScenarioTransientVarCheck(check);
            case AREA_EFFECT_LAYER_PRESENCE -> applyAreaEffectLayerPresenceCheck(check);
            case UNIT_EFFECT_KEY_FLAG -> applyUnitEffectKeyFlagCheck(check);
            case UNIT_EXACT_CELL -> applyUnitExactCellCheck(check);
            default -> {
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::applyActiveEngagementTargetScriptIdCheck @00577300.
     * Fully ported.
     */
    public void applyActiveEngagementTargetScriptIdCheck(ScriptCheck check) {
        if (check.unit.missionRuntimeState.command == UNIT_MISSION_COMMAND_ENGAGE_TARGET) {
            writeScriptVariable(check.digit, check.unit.missionRuntimeState.targetToken.scenarioObjectId);
        } else {
            writeScriptVariable(check.digit, 0);
        }
    }

    /**
     * Native: MissionScriptRuntime::applyGroupUnitCountCheck @00576FC8.
     * Fully ported.
     */
    public void applyGroupUnitCountCheck(ScriptCheck check) {
        writeScriptVariable(check.digit, check.group.units.size());
    }

    /**
     * Native: MissionScriptRuntime::applyUnitInRectangleCheck @00577030.
     * Fully ported.
     */
    public void applyUnitInRectangleCheck(ScriptCheck check) {
        int x = check.unit.m_pTargetHandle.getX();
        int y = check.unit.m_pTargetHandle.getY();
        int inside = check.argValues[0] <= x
                && x <= check.argValues[2]
                && check.argValues[1] <= y
                && y <= check.argValues[3]
                ? 1
                : 0;
        writeScriptVariable(check.digit, inside);
    }

    /**
     * Native: MissionScriptRuntime::applyUnitNearPointCheck @005770D1.
     * Fully ported.
     */
    public void applyUnitNearPointCheck(ScriptCheck check) {
        int distance = chebyshevDistance(
                check.unit.m_pTargetHandle.getX(),
                check.unit.m_pTargetHandle.getY(),
                check.argValues[0] & 0xFF,
                check.argValues[1] & 0xFF
        );
        writeScriptVariable(check.digit, check.argValues[2] < distance ? 0 : 1);
    }

    /**
     * Native: MissionScriptRuntime::applyUnitHitPointsCheck @005771A8.
     * Fully ported.
     */
    public void applyUnitHitPointsCheck(ScriptCheck check) {
        if (check.argValues[0] == 6) {
            writeScriptVariable(check.digit, check.unit.m_nHP);
        }
    }

    /**
     * Native: MissionScriptRuntime::applyUnitAliveCheck @00576FEF.
     * Fully ported.
     */
    public void applyUnitAliveCheck(ScriptCheck check) {
        writeScriptVariable(check.digit, check.unit.state == UnitActionState.DEAD ? 0 : 1);
    }

    /**
     * Native: MissionScriptRuntime::applyUnitDistanceCheck @00577268.
     * Fully ported.
     */
    public void applyUnitDistanceCheck(ScriptCheck check) {
        Unit secondaryUnit = (Unit) check.secondaryTarget;
        if (check.unit.state == UnitActionState.DEAD || secondaryUnit.state == UnitActionState.DEAD) {
            writeScriptVariable(check.digit, SCRIPT_DISTANCE_UNAVAILABLE);
            return;
        }
        writeScriptVariable(check.digit, chebyshevDistance(check.unit, secondaryUnit));
    }

    /**
     * Native: MissionScriptRuntime::applyUnitPointDistanceCheck @0057714D.
     * Fully ported.
     */
    public void applyUnitPointDistanceCheck(ScriptCheck check) {
        writeScriptVariable(check.digit, chebyshevDistance(
                check.unit.m_pTargetHandle.getX(),
                check.unit.m_pTargetHandle.getY(),
                check.argValues[0] & 0xFF,
                check.argValues[1] & 0xFF
        ));
    }

    /**
     * Native: MissionScriptRuntime::applyPlayerUnitCountCheck @005771E4.
     * Fully ported.
     */
    public void applyPlayerUnitCountCheck(ScriptCheck check) {
        int count = 0;
        for (UnitGroup group : check.player.unitGroups) {
            count += group.units.size();
        }
        writeScriptVariable(check.digit, count);
    }

    /**
     * Native: MissionScriptRuntime::applyDiplomacyStateCheck @00577359.
     * Fully ported.
     */
    public void applyDiplomacyStateCheck(ScriptCheck check) {
        Player secondaryPlayer = (Player) check.secondaryTarget;
        writeScriptVariable(check.digit, getRelationFlags(check.player, secondaryPlayer) & 3);
    }

    /**
     * Native: MissionScriptRuntime::applySackAtPointCheck @00577406.
     * Fully ported.
     */
    public void applySackAtPointCheck(ScriptCheck check) {
        writeScriptVariable(
                check.digit,
                worldMap.findSackAtPoint(check.argValues[0], check.argValues[1]) == null ? 0 : 1
        );
    }

    /**
     * Native: MissionScriptRuntime::applyUnitInventoryItemCheck @005773AA.
     * Fully ported.
     */
    public void applyUnitInventoryItemCheck(ScriptCheck check) {
        writeScriptVariable(check.digit, inventoryHasItemByHash(check.unit.inventory, check.itemId) ? 1 : 0);
    }

    /**
     * Native: MissionScriptRuntime::applyUnitInventoryItemCheckDuplicate @005775B1.
     * Fully ported.
     */
    public void applyUnitInventoryItemCheckDuplicate(ScriptCheck check) {
        writeScriptVariable(check.digit, inventoryHasItemByHash(check.unit.inventory, check.itemId) ? 1 : 0);
    }

    /**
     * Native: MissionScriptRuntime::applyClosestPlayerUnitToPointCheck @0057745B.
     * Fully ported.
     */
    public void applyClosestPlayerUnitToPointCheck(ScriptCheck check) {
        int minDistance = SCRIPT_DISTANCE_UNAVAILABLE;
        for (UnitGroup group : check.player.unitGroups) {
            for (Unit unit : group.units) {
                minDistance = Math.min(minDistance, chebyshevDistance(
                        unit.m_pTargetHandle.getX(),
                        unit.m_pTargetHandle.getY(),
                        check.argValues[0] & 0xFF,
                        check.argValues[1] & 0xFF
                ));
            }
        }
        writeScriptVariable(check.digit, minDistance);
    }

    /**
     * Native: MissionScriptRuntime::applyInventoryItemDistanceCheck @00577527.
     * Fully ported.
     */
    public void applyInventoryItemDistanceCheck(ScriptCheck check) {
        if (!inventoryHasItemByHash(check.unit.inventory, check.itemId)) {
            writeScriptVariable(check.digit, SCRIPT_DISTANCE_UNAVAILABLE);
            return;
        }
        writeScriptVariable(check.digit, chebyshevDistance(
                check.unit.m_pTargetHandle.getX(),
                check.unit.m_pTargetHandle.getY(),
                check.argValues[0] & 0xFF,
                check.argValues[1] & 0xFF
        ));
    }

    /**
     * Native: MissionScriptRuntime::applyDeadUnitFailureCounterCheck @00577600.
     * Fully ported.
     */
    public void applyDeadUnitFailureCounterCheck(ScriptCheck check) {
        if (check.unit.state == UnitActionState.DEAD) {
            missionFailureValue++;
        }
    }

    /**
     * Native: MissionScriptRuntime::applyScriptVariableCheck @0057762E.
     * Fully ported.
     */
    public void applyScriptVariableCheck(ScriptCheck check) {
        writeScriptVariable(check.digit, readScriptVariable(check.argValues[0]));
    }

    /**
     * Native: MissionScriptRuntime::applyPlayerBuildingCountCheck @0057765C.
     * Fully ported.
     */
    public void applyPlayerBuildingCountCheck(ScriptCheck check) {
        int count = 0;
        for (Building building : Globals.gameServer.objectLists.buildings) {
            if (building.owner == check.player && (short) building.healthCurrent > 0) {
                count++;
            }
        }
        writeScriptVariable(check.digit, count);
    }

    /**
     * Native: MissionScriptRuntime::applyBuildingHitPointsCheck @005776D7.
     * Fully ported.
     */
    public void applyBuildingHitPointsCheck(ScriptCheck check) {
        writeScriptVariable(check.digit, (short) check.building.healthCurrent);
    }

    /**
     * Native: MissionScriptRuntime::applyMissionEntryRelocationCheck @005776FD.
     * Fully ported.
     */
    public void applyMissionEntryRelocationCheck(ScriptCheck check) {
        int sourceCell = (short) check.argValues[0] + (short) check.argValues[1] * 0x100;
        Unit unit = worldMap.getGroundUnitAtCell(sourceCell);
        if (unit == null) {
            writeScriptVariable(check.digit, 0);
            return;
        }
        int targetCell = ((check.argValues[3] & 0xFF) << 8) | (check.argValues[2] & 0xFF);
        worldMap.relocateScenarioMissionEntryUnit(unit, targetCell);
        writeScriptVariable(check.digit, 1);
    }

    /**
     * Native: MissionScriptRuntime::applyScenarioVarCheck @0057777B.
     * Fully ported.
     */
    public void applyScenarioVarCheck(ScriptCheck check) {
        writeScriptVariable(check.digit, Globals.scenarioLib.getVar(check.argValues[0]));
    }

    /**
     * Native: MissionScriptRuntime::applyScenarioTransientVarCheck @005777A4.
     * Fully ported.
     */
    public void applyScenarioTransientVarCheck(ScriptCheck check) {
        writeScriptVariable(check.digit, Globals.scenarioLib.getVar(check.argValues[0] + SCENARIO_TRANSIENT_VAR_BASE));
    }

    /**
     * Native: MissionScriptRuntime::applyAreaEffectLayerPresenceCheck @005777D3.
     * Fully ported.
     */
    public void applyAreaEffectLayerPresenceCheck(ScriptCheck check) {
        int packedCell = (short) check.argValues[0] + (short) check.argValues[1] * 0x100;
        writeScriptVariable(
                check.digit,
                worldMap.hasAreaEffectLayerAtCell(packedCell, check.argValues[2]) ? 1 : 0
        );
    }

    /**
     * Native: MissionScriptRuntime::applyUnitEffectKeyFlagCheck @00577857.
     * Fully ported.
     */
    public void applyUnitEffectKeyFlagCheck(ScriptCheck check) {
        int mask = 1 << (check.argValues[0] & 0x1F);
        writeScriptVariable(check.digit, (check.unit.effectKeyFlags & mask) == 0 ? 0 : 1);
    }

    /**
     * Native: MissionScriptRuntime::applyUnitExactCellCheck @005778AD.
     * Fully ported.
     */
    public void applyUnitExactCellCheck(ScriptCheck check) {
        int matches = check.unit.m_pTargetHandle.getX() == check.argValues[1]
                && check.unit.m_pTargetHandle.getY() == check.argValues[2]
                && check.unit.m_pTargetHandle.isSubPosUnknown()
                ? 1
                : 0;
        writeScriptVariable(check.digit, matches);
    }

    /**
     * Native: MissionScriptRuntime::evaluateScriptPatterns @00574BA1.
     * Fully ported.
     */
    public void evaluateScriptPatterns() {
        ScriptPattern pattern = new ScriptPattern();
        for (ScriptPattern storedPattern : scriptPatterns) {
            pattern.copyFrom(storedPattern);
            if (pattern.selfDestruct != 0 && readScriptPatternFiredFlag(pattern.digit) != 0) {
                continue;
            }
            writeScriptPatternFiredFlag(pattern.digit, 0);
            int matched = 1;
            for (ScriptCondition condition : pattern.conditions) {
                if (!evaluateScriptCondition(condition)) {
                    matched = 0;
                    break;
                }
            }
            if (matched != 0) {
                writeScriptPatternFiredFlag(pattern.digit, 1);
                if (pattern.selfDestruct != 0 && Globals.gameServer.isScriptTracingEnabled()) {
                    CServerApp.sendServerChatText(
                            "Script: Trigger %d ( %d ifs, %d instants ).\n".formatted(
                                    pattern.digit,
                                    pattern.conditions.size(),
                                    pattern.instantIds.size()
                            ),
                            null
                    );
                }
                for (Integer instantId : pattern.instantIds) {
                    executeScriptInstant(instantId);
                }
            }
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::evaluateScriptPatterns @00574BA1 comparison switch.
     */
    public boolean evaluateScriptCondition(ScriptCondition condition) {
        int left = readScriptVariable(condition.leftVariableIndex);
        int right = readScriptVariable(condition.rightVariableIndex);
        return switch (condition.comparison) {
            case ScriptCondition.EQUAL -> left == right;
            case ScriptCondition.NOT_EQUAL -> left != right;
            case ScriptCondition.GREATER_THAN -> left > right;
            case ScriptCondition.LESS_THAN -> left < right;
            case ScriptCondition.GREATER_OR_EQUAL -> left >= right;
            case ScriptCondition.LESS_OR_EQUAL -> left <= right;
            default -> false;
        };
    }

    /**
     * Native: MissionScriptRuntime::executeScriptInstant @00574F3F.
     * Fully ported.
     */
    public void executeScriptInstant(int instantIndex) {
        ScriptInstant instant = new ScriptInstant(scriptInstants.get(instantIndex));
        if (Globals.gameServer.isScriptTracingEnabled()) {
            CServerApp.sendServerChatText(
                    "Script: Instant %d ( type: %d ).\n".formatted(instantIndex, instant.type),
                    null
            );
        }
        rebindInstantObjectVariableReferences(instant);
        switch (InstantType.fromId(instant.type)) {
            case INCREMENT_RUNTIME_COUNTERS -> executeIncrementRuntimeCountersInstant(instant);
            case OPEN_QUEST_OBJECTIVES_QUERY -> executeQuestObjectivesQueryOpenInstant(instant);
            case SET_SCRIPT_VARIABLE -> executeSetScriptVariableInstant(instant);
            case MISSION_COMPLETE -> executeMissionCompleteInstant(instant);
            case MISSION_FAILED -> executeMissionFailedInstant(instant);
            case GROUP_SCRIPT_STATE -> executeGroupScriptStateInstant(instant);
            case INCREMENT_SCRIPT_VARIABLE -> executeIncrementScriptVariableInstant(instant);
            case NO_OP -> executeNoOpInstant(instant);
            case SET_DIPLOMACY_STATE -> executeDiplomacyStateInstant(instant);
            case SET_PLAYER_FORMATION_MODE -> executeSetPlayerFormationModeInstant(instant);
            case TRANSFER_UNIT_INVENTORY_ITEM -> executeTransferUnitInventoryItemInstant(instant);
            case ADD_UNIT_INVENTORY_ITEM -> executeAddUnitInventoryItemInstant(instant);
            case REMOVE_UNIT_INVENTORY_ITEM -> executeRemoveUnitInventoryItemInstant(instant);
            case SEND_UNIT_EQUIPMENT_STATE_UPDATE -> executeSendUnitEquipmentStateUpdateInstant(instant);
            case SEND_UNIT_EQUIPMENT_STATE_UPDATE_DUPLICATE ->
                    executeSendUnitEquipmentStateUpdateDuplicateInstant(instant);
            case HIDE_UNIT_FROM_MISSION_MAP -> executeHideUnitFromMissionMapInstant(instant);
            case RETURN_UNIT_TO_MISSION_MAP -> executeReturnUnitToMissionMapInstant(instant);
            case SWAP_HIDDEN_UNIT_WITH_TARGET_UNIT -> executeSwapHiddenUnitWithTargetUnitInstant(instant);
            case TRANSFER_UNIT_TO_PLAYER -> executeTransferUnitToPlayerInstant(instant);
            case DROP_UNIT_INVENTORY_DEATH_SACK -> executeDropUnitInventoryDeathSackInstant(instant);
            case TRANSIENT_POINT_SPELL_CAST -> executeTransientPointSpellCastInstant(instant);
            case TRANSFER_GROUP_UNITS_TO_PLAYER -> executeTransferGroupUnitsToPlayerInstant(instant);
            case ADJUST_PLAYER_GOLD -> executeAdjustPlayerGoldInstant(instant);
            case TRANSIENT_TARGET_SPELL_CAST -> executeTransientTargetSpellCastInstant(instant);
            case TRANSIENT_CELL_SPELL_CAST -> executeTransientCellSpellCastInstant(instant);
            case SET_BUILDING_HIT_POINTS -> executeSetBuildingHitPointsInstant(instant);
            case RELOCATE_UNIT_MISSION_ENTRY -> executeRelocateUnitMissionEntryInstant(instant);
            case DRAIN_UNIT_INVENTORY_TO_TARGET -> executeDrainUnitInventoryToTargetInstant(instant);
            case SET_AREA_EFFECT_DURATION -> executeSetAreaEffectDurationInstant(instant);
            case SET_UNIT_EFFECT_VALUE -> executeSetUnitEffectValueInstant(instant);
            case SET_OUTPOST_RESPAWN_TIMER -> executeSetOutpostRespawnTimerInstant(instant);
            case HIDE_GROUP_UNITS_FROM_MISSION_MAP -> executeHideGroupUnitsFromMissionMapInstant(instant);
            case RETURN_GROUP_UNITS_TO_MISSION_MAP -> executeReturnGroupUnitsToMissionMapInstant(instant);
            case SET_UNIT_STAT_VALUE -> executeSetUnitStatValueInstant(instant);
            case SCENARIO_SET_VAR -> executeScenarioSetVarInstant(instant);
            case SCENARIO_TRANSIENT_STATE -> executeScenarioTransientStateInstant(instant);
            case REMOVE_ITEM_FROM_ACTIVE_UNIT_INVENTORIES -> executeRemoveItemFromActiveUnitInventoriesInstant(instant);
            case CLEAR_SCENARIO_SCRIPT_REFERENCED_FLAG -> executeClearScenarioScriptReferencedFlagInstant(instant);
            default -> {
                if (Globals.gameServer.isTurnTracingEnabled()) {
                    CServerApp.sendServerChatText(
                            "Script: Bad instant %d.\n".formatted(instant.type),
                            null
                    );
                }
            }
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::executeScriptInstant @00574F3F object-variable rebinding.
     */
    public void rebindInstantObjectVariableReferences(ScriptInstant instant) {
        if ((instant.variableReferenceCount & 0xFF) == 0) {
            return;
        }
        int unitReferenceCount = 0;
        int groupReferenceCount = 0;
        int playerReferenceCount = 0;
        for (int i = 0; i < SELECTOR_SLOT_COUNT; i++) {
            if (instant.variableReferenceMode[i] == 0) {
                continue;
            }
            Object reference = scriptObjectVariables[instant.variableReferenceSlot[i] & 0xFF];
            switch (instant.argTypes[i]) {
                case SCENARIO_SCRIPT_ARG_GROUP -> {
                    if (groupReferenceCount == 0) {
                        instant.group = (UnitGroup) reference;
                    } else {
                        instant.secondaryTarget = reference;
                    }
                    groupReferenceCount++;
                }
                case SCENARIO_SCRIPT_ARG_PLAYER -> {
                    if (playerReferenceCount == 0) {
                        instant.player = (Player) reference;
                    } else {
                        instant.secondaryTarget = reference;
                    }
                    playerReferenceCount++;
                }
                case SCENARIO_SCRIPT_ARG_UNIT -> {
                    if (unitReferenceCount == 0) {
                        instant.unit = (Unit) reference;
                    } else {
                        instant.secondaryTarget = reference;
                    }
                    unitReferenceCount++;
                }
                default -> {
                }
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::executeIncrementRuntimeCountersInstant @0057792A.
     * Fully ported.
     */
    public void executeIncrementRuntimeCountersInstant(ScriptInstant instant) {
        runtimeCounterInstantCount++;
        incrementScriptVariable(SCRIPT_RUNTIME_COUNTER_VARIABLE);
    }

    /**
     * Native: MissionScriptRuntime::executeQuestObjectivesQueryOpenInstant @00577C48.
     */
    public void executeQuestObjectivesQueryOpenInstant(ScriptInstant instant) {
        CServerApp.sendQuestObjectivesQueryOpen(null, instant.argValues[0], 0);
    }

    /**
     * Native: MissionScriptRuntime::executeSetScriptVariableInstant @00577C6A.
     */
    public void executeSetScriptVariableInstant(ScriptInstant instant) {
        writeScriptVariable(instant.argValues[0], instant.argValues[1]);
    }

    /**
     * Native: MissionScriptRuntime::executeMissionCompleteInstant @00577CB0.
     */
    public void executeMissionCompleteInstant(ScriptInstant instant) {
        missionCompleteCount++;
    }

    /**
     * Native: MissionScriptRuntime::executeMissionFailedInstant @00577CD2.
     */
    public void executeMissionFailedInstant(ScriptInstant instant) {
        missionFailureValue = instant.argValues[0];
    }

    /**
     * Native: MissionScriptRuntime::executeGroupScriptStateInstant @00577CEE.
     */
    public void executeGroupScriptStateInstant(ScriptInstant si) {
        switch (si.argValues[0]) {
            case 1 -> initializeAdvanceToMissionCellGroup(si.group, si.argValues[1]);
            case 2 -> initializeMoveToCellGroup(si.group, si.argValues[1], si.argValues[2]);
            case 3 -> si.group.enterScenarioMissionGroupScriptState(this);
            case 4 -> initializeCommandCellMoveGroup(si.group, si.argValues[1], si.argValues[2]);
            case 5 -> initializeCommandCellOrTargetGroup(si.group, si.argValues[1], si.argValues[2]);
            case 10 -> initializeAttackTargetGroup(si.group, si.unit);
            case 11 -> initializeRangeTargetEngagementGroup(si.group, si.unit, si.argValues[1]);
            case 14 -> initializeWaypointCellGroup(si.group, si.argValues[1], si.argValues[2]);
            case 15 -> initializeRangeTargetRetreatGroup(si.group, si.unit, si.argValues[1]);
            case 17 -> initializeWanderTargetCellGroup(si.group);
            case 18 -> initializeLoadedScenarioGroup(si.group, si.argValues[1]);
            default -> {
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::initializeAdvanceToMissionCellGroup @005709D9.
     * Fully ported.
     */
    public void initializeAdvanceToMissionCellGroup(UnitGroup group, int guardRange) {
        guardRange &= 0xFF;
        group.resetScenarioMissionGroupScriptState(this);
        for (Unit unit : group.units) {
            enterLoadedScenarioGroupUnitScriptState(unit);
        }
        group.missionState.scriptRuntimeState = GROUP_SCRIPT_STATE_ADVANCE_TO_MISSION_CELL;
        refreshScenarioGroupCenterAndGuardRange(group);
        if (guardRange == 0) {
            if (group.missionState.getScenarioGroupGuardRange() < minimalGuardRange) {
                group.missionState.setScenarioGroupGuardRange(minimalGuardRange);
            }
        } else if (group.missionState.getScenarioGroupGuardRange() < guardRange) {
            group.missionState.setScenarioGroupGuardRange(guardRange);
        }
        group.missionState.clearScenarioGroupGuardRefreshFlag();
    }

    /**
     * Native: MissionScriptRuntime::initializeMoveToCellGroup @005708BC.
     * Fully ported.
     */
    public void initializeMoveToCellGroup(UnitGroup group, int x, int y) {
        group.resetScenarioMissionGroupScriptState(this);
        group.missionState.scriptRuntimeState = GROUP_SCRIPT_STATE_MOVE_TO_CELL;
        group.missionState.setScriptTargetCell(((y & 0xFF) << 8) | (x & 0xFF));
    }

    /**
     * Native: MissionScriptRuntime::initializeCommandCellMoveGroup @005703C0.
     * Fully ported.
     */
    public void initializeCommandCellMoveGroup(UnitGroup group, int x, int y) {
        initializeCommandCellGroup(group, x, y, GROUP_SCRIPT_STATE_COMMAND_CELL_MOVE);
    }

    /**
     * Native: MissionScriptRuntime::initializeCommandCellOrTargetGroup @0057063E.
     * Fully ported.
     */
    public void initializeCommandCellOrTargetGroup(UnitGroup group, int x, int y) {
        initializeCommandCellGroup(group, x, y, GROUP_SCRIPT_STATE_COMMAND_CELL_OR_TARGET);
    }

    /**
     * Native: MissionScriptRuntime::initializeCommandCellOrTargetGroupFromBytes @005709B8.
     * Fully ported.
     */
    public void initializeCommandCellOrTargetGroupFromBytes(UnitGroup group, int x, int y) {
        initializeCommandCellOrTargetGroup(group, x, y);
    }

    /**
     * Native support extracted from MissionScriptRuntime::initializeCommandCellMoveGroup @005703C0 and
     * MissionScriptRuntime::initializeCommandCellOrTargetGroup @0057063E.
     */
    public void initializeCommandCellGroup(UnitGroup group, int x, int y, int scriptRuntimeState) {
        boolean useRelativeFormation = true;
        int targetX = x & 0xFF;
        int targetY = y & 0xFF;
        int centerX = 0;
        int centerY = 0;

        group.resetScenarioMissionGroupScriptState(this);
        int formationMode = group.owner.battlePreferences.formationMode & 0xFF;
        if (formationMode == 2) {
            int centerSubpos = refreshScenarioGroupCenterAndFootprintRange(group);
            centerX = (centerSubpos >>> 8) & 0xFF;
            centerY = (centerSubpos >>> 24) & 0xFF;
            int centerCell = ((centerY & 0xFF) << 8) | (centerX & 0xFF);
            for (Unit unit : group.units) {
                int distance = cellChebyshevDistance(unit.m_pTargetHandle.getCell(), centerCell);
                if ((relativeFormationDistanceLimit & 0xFF) < distance) {
                    useRelativeFormation = false;
                }
            }
        } else {
            useRelativeFormation = formationMode != 0;
            if (useRelativeFormation) {
                int centerSubpos = refreshScenarioGroupCenterAndFootprintRange(group);
                centerX = (centerSubpos >>> 8) & 0xFF;
                centerY = (centerSubpos >>> 24) & 0xFF;
            }
        }

        group.resetScenarioMissionGroupScriptState(this);
        int minSpeed = 0xFA;
        for (Unit unit : group.units) {
            if (!useRelativeFormation) {
                initializeCommandCellUnit(unit, targetX, targetY);
            } else {
                unit.missionRuntimeState.commandFormationOffsetX = (unit.m_pTargetHandle.getX() & 0xFF) - centerX;
                unit.missionRuntimeState.commandFormationOffsetY = (unit.m_pTargetHandle.getY() & 0xFF) - centerY;
                initializeCommandCellUnit(
                        unit,
                        targetX + unit.missionRuntimeState.commandFormationOffsetX,
                        targetY + unit.missionRuntimeState.commandFormationOffsetY
                );
                if ((short) unit.speed < minSpeed) {
                    minSpeed = unit.speed & 0xFF;
                }
            }
        }
        if (useRelativeFormation) {
            group.missionState.setMissionScriptSpeedOverride(minSpeed);
        }
        group.missionState.scriptRuntimeState = scriptRuntimeState;
        group.missionState.setScriptTargetCell((targetY << 8) | targetX);
    }

    /**
     * Native: MissionScriptRuntime::initializeCommandCellUnit @0056D032.
     * Fully ported.
     */
    public void initializeCommandCellUnit(Unit unit, int x, int y) {
        unit.resetScenarioMissionUnitScriptState(this);
        int targetX = x & 0xFF;
        int targetY = y & 0xFF;
        if (targetX < (worldMap.rect0x58EB8.lt.x & 0xFF)) {
            targetX = worldMap.rect0x58EB8.lt.x & 0xFF;
        }
        if ((worldMap.rect0x58EB8.rb.x & 0xFF) < targetX) {
            targetX = worldMap.rect0x58EB8.rb.x & 0xFF;
        }
        if (targetY < (worldMap.rect0x58EB8.lt.y & 0xFF)) {
            targetY = worldMap.rect0x58EB8.lt.y & 0xFF;
        }
        if ((worldMap.rect0x58EB8.rb.y & 0xFF) < targetY) {
            targetY = worldMap.rect0x58EB8.rb.y & 0xFF;
        }
        unit.missionActionCode = MOVE_TO_CELL;
        unit.missionRuntimeState.commandCell = ((targetY & 0xFF) << 8) | (targetX & 0xFF);
        unit.movementState.missionReentryPending = 0;
        unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_MOVE_TO_CELL;
    }

    /**
     * Native: MissionScriptRuntime::initializeMoveToCellUnit @0056D158.
     * Fully ported.
     */
    public void initializeMoveToCellUnit(Unit unit, int x, int y) {
        unit.resetScenarioMissionUnitScriptState(this);
        unit.missionActionCode = MOVE_TO_CELL;
        unit.missionRuntimeState.commandCell = ((y & 0xFF) << 8) | (x & 0xFF);
        unit.movementState.missionReentryPending = 0;
        unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_MOVE_TO_CELL;
    }

    /**
     * Native: MissionScriptRuntime::initializeAttackTargetGroup @00570946.
     * Fully ported.
     */
    public void initializeAttackTargetGroup(UnitGroup group, Unit target) {
        group.resetScenarioMissionGroupScriptState(this);
        for (Unit unit : group.units) {
            if (computeMissionTargetScore(unit, target, false) == 0xFFFFFF) {
                unit.initializeScenarioMissionEntryUnit(this);
            } else {
                initializeAttackTargetUnit(unit, target);
            }
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::initializeAttackTargetGroup @00570946 for building-token
     * targets passed through GameServer::handleServerGameAction @004F515D.
     */
    public void initializeAttackTargetGroup(UnitGroup group, Building target) {
        group.resetScenarioMissionGroupScriptState(this);
        for (Unit unit : group.units) {
            initializeAttackTokenTargetUnit(unit, target);
        }
    }

    /**
     * Native: MissionScriptRuntime::initializeRangeTargetEngagementGroup @00570C84.
     * Fully ported.
     */
    public void initializeRangeTargetEngagementGroup(UnitGroup group, Token target, int rangeFlag) {
        group.resetScenarioMissionGroupScriptState(this);
        for (Unit unit : group.units) {
            initializeRangeTargetEngagementUnit(unit, target, rangeFlag);
        }
    }

    /**
     * Native: MissionScriptRuntime::initializeWaypointCellGroup @00570D26.
     * Fully ported.
     */
    public void initializeWaypointCellGroup(UnitGroup group, int x, int y) {
        group.resetScenarioMissionGroupScriptState(this);
        for (Unit unit : group.units) {
            initializeWaypointCellUnit(unit, x, y);
        }
    }

    /**
     * Native: MissionScriptRuntime::initializeWaypointRouteGroup @00570D77.
     * Fully ported.
     */
    public void initializeWaypointRouteGroup(UnitGroup group) {
        group.resetScenarioMissionGroupScriptState(this);
        for (Unit unit : group.units) {
            unit.missionRuntimeState.waypointCells.clear();
            unit.missionRuntimeState.waypointCells.addAll(group.missionState.waypointCells);
            initializeWaypointRouteUnit(unit);
        }
    }

    /**
     * Native: MissionScriptRuntime::initializeTargetSpellOrderGroup @005702BE.
     * Native accepts and ignores the stack Spell parameter supplied by GameServer::handleServerGameAction @004F515D.
     * Fully ported.
     */
    public void initializeTargetSpellOrderGroup(
            UnitGroup group,
            Token target,
            @SuppressWarnings("unused") Spell selectedSpellStackArg
    ) {
        group.resetScenarioMissionGroupScriptState(this);
        for (Unit unit : group.units) {
            Spell selectedSpell = unit.secondarySpell;
            if (selectedSpell == null) {
                unit.initializeScenarioMissionEntryUnit(this);
            } else {
                initializeTargetSpellOrderUnit(unit, target, selectedSpell);
            }
            unit.secondarySpell = null;
        }
        group.missionState.scriptRuntimeState = GROUP_SCRIPT_STATE_IDLE;
    }

    /**
     * Native: MissionScriptRuntime::initializeCellSpellOrderGroup @0057033D.
     * Native accepts and ignores the stack Spell parameter supplied by GameServer::handleServerGameAction @004F515D.
     * Fully ported.
     */
    public void initializeCellSpellOrderGroup(
            UnitGroup group,
            int x,
            int y,
            @SuppressWarnings("unused") Spell selectedSpellStackArg
    ) {
        group.resetScenarioMissionGroupScriptState(this);
        for (Unit unit : group.units) {
            Spell selectedSpell = unit.secondarySpell;
            if (selectedSpell == null) {
                unit.initializeScenarioMissionEntryUnit(this);
            } else {
                initializeCellSpellOrderUnit(unit, x, y, selectedSpell);
            }
            unit.secondarySpell = null;
        }
        group.missionState.scriptRuntimeState = GROUP_SCRIPT_STATE_IDLE;
    }

    /**
     * Native: MissionScriptRuntime::initializeStandStillOrderGroup @00570BDE.
     * Fully ported.
     */
    public void initializeStandStillOrderGroup(UnitGroup group) {
        group.resetScenarioMissionGroupScriptState(this);
        for (Unit unit : group.units) {
            initializeStandStillOrderUnit(unit);
        }
        group.missionState.scriptRuntimeState = GROUP_SCRIPT_STATE_PREPARE_DEFAULT;
    }

    /**
     * Native: MissionScriptRuntime::initializeRetreatOrderGroup @00570C31.
     * Fully ported.
     */
    public void initializeRetreatOrderGroup(UnitGroup group) {
        group.resetScenarioMissionGroupScriptState(this);
        for (Unit unit : group.units) {
            initializeRetreatOrderUnit(unit);
        }
        group.missionState.scriptRuntimeState = GROUP_SCRIPT_STATE_IDLE;
    }

    /**
     * Native: MissionScriptRuntime::initializeRangeTargetRetreatGroup @00570CD5.
     * Fully ported.
     */
    public void initializeRangeTargetRetreatGroup(UnitGroup group, Unit target, int rangeFlag) {
        group.resetScenarioMissionGroupScriptState(this);
        for (Unit unit : group.units) {
            initializeRangeTargetRetreatUnit(unit, target, rangeFlag);
        }
    }

    /**
     * Native: MissionScriptRuntime::initializePickupOrderGroup @00570EDA.
     * Fully ported.
     */
    public void initializePickupOrderGroup(UnitGroup group, int x, int y) {
        group.resetScenarioMissionGroupScriptState(this);
        for (Unit unit : group.units) {
            initializePickupOrderUnit(unit, x, y);
        }
    }

    /**
     * Native: MissionScriptRuntime::initializeAttackTargetUnit @0056D1BE.
     * Fully ported.
     */
    public void initializeAttackTargetUnit(Unit unit, Unit target) {
        if (unit == target) {
            unit.initializeScenarioMissionEntryUnit(this);
            return;
        }
        initializeAttackTokenTargetUnit(unit, target);
    }

    /**
     * Native support extracted from MissionScriptRuntime::initializeAttackTargetUnit @0056D1BE token-target field writes.
     * Fully ported.
     */
    public void initializeAttackTokenTargetUnit(Unit unit, Token target) {
        unit.resetScenarioMissionUnitScriptState(this);
        unit.missionActionCode = ATTACK_TARGET;
        unit.missionRuntimeState.targetToken = target;
        unit.missionRuntimeState.attackRange = getCastRangeForFirstCastableSpellOrFallback(unit);
        unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
    }

    /**
     * Native: MissionScriptRuntime::initializeTargetSpellOrderUnit @0056D3E6.
     * Fully ported.
     */
    public void initializeTargetSpellOrderUnit(Unit unit, Token target, Spell spell) {
        unit.resetScenarioMissionUnitScriptState(this);
        unit.missionActionCode = TARGETED_SPELL_ORDER;
        unit.missionRuntimeState.spellTargetToken = target;
        unit.missionRuntimeState.spell = spell;
        unit.missionRuntimeState.attackRange = spell.maxRange & 0xFF;
        unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
    }

    /**
     * Native: MissionScriptRuntime::initializeCellSpellOrderUnit @0056D446.
     * Fully ported.
     */
    public void initializeCellSpellOrderUnit(Unit unit, int x, int y, Spell spell) {
        unit.resetScenarioMissionUnitScriptState(this);
        unit.missionActionCode = CELL_SPELL_ORDER;
        unit.missionRuntimeState.cellSpellTargetCell = ((y & 0xFF) << 8) | (x & 0xFF);
        unit.missionRuntimeState.spell = spell;
        unit.missionRuntimeState.attackRange = spell.maxRange & 0xFF;
        unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
    }

    /**
     * Native: MissionScriptRuntime::initializeRangeTargetEngagementUnit @0056D5B9.
     * Fully ported.
     */
    public void initializeRangeTargetEngagementUnit(Unit unit, Token target, int rangeFlag) {
        unit.resetScenarioMissionUnitScriptState(this);
        if (unit == target) {
            unit.initializeScenarioMissionEntryUnit(this);
            return;
        }
        unit.missionActionCode = RANGE_TARGET_ENGAGE;
        unit.missionRuntimeState.rangeTargetToken = target;
        unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
        unit.missionRuntimeState.rangeTargetMode = rangeFlag == 0 ? 3 : rangeFlag & 0xFF;
    }

    /**
     * Native support extracted from MissionScriptRuntime::applyUnitMissionActionState @00569321 target-token dispatch.
     * Fully ported.
     */
    public void engageMissionTokenTarget(Unit unit, Token target) {
        if (target instanceof Unit targetUnit) {
            engageMissionTarget(unit, targetUnit);
            return;
        }
        if (target instanceof Building building) {
            unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_ENGAGE_TARGET;
            unit.missionRuntimeState.targetToken = building;
            unit.missionRuntimeState.attackRange = getCastRangeForFirstCastableSpellOrFallback(unit);
        }
    }

    /**
     * Native: MissionScriptRuntime::initializeWaypointCellUnit @0056D6B7.
     * Fully ported.
     */
    public void initializeWaypointCellUnit(Unit unit, int x, int y) {
        unit.resetScenarioMissionUnitScriptState(this);
        unit.missionActionCode = WAYPOINT;
        int currentCell = unit.m_pTargetHandle.getCell();
        unit.missionRuntimeState.missionScriptCell = currentCell;
        unit.missionRuntimeState.waypointCells.clear();
        unit.missionRuntimeState.waypointCells.add((short) currentCell);
        unit.missionRuntimeState.waypointCells.add((short) (((y & 0xFF) << 8) | (x & 0xFF)));
        unit.missionRuntimeState.waypointTargetCell = unit.missionRuntimeState.waypointCells.getFirst() & 0xFFFF;
        unit.missionRuntimeState.waypointRefreshFlag = 0;
        unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
        worldMap.visionAndDistance0x58EC0.scanUnitVisibility(unit, currentCell);
    }

    /**
     * Native: MissionScriptRuntime::initializeWaypointRouteUnit @0056D7B5.
     * Fully ported.
     */
    public void initializeWaypointRouteUnit(Unit unit) {
        unit.resetScenarioMissionUnitScriptState(this);
        unit.missionActionCode = WAYPOINT;
        int currentCell = unit.m_pTargetHandle.getCell();
        unit.missionRuntimeState.missionScriptCell = currentCell;
        unit.missionRuntimeState.waypointTargetCell = unit.missionRuntimeState.waypointCells.getFirst() & 0xFFFF;
        unit.missionRuntimeState.waypointCells.add((short) currentCell);
        unit.missionRuntimeState.waypointRefreshFlag = 0;
        unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
    }

    /**
     * Native: MissionScriptRuntime::initializeRangeTargetRetreatUnit @0056D638.
     * Fully ported.
     */
    public void initializeRangeTargetRetreatUnit(Unit unit, Unit target, int rangeFlag) {
        unit.resetScenarioMissionUnitScriptState(this);
        if (unit == target) {
            unit.initializeScenarioMissionEntryUnit(this);
            return;
        }
        unit.missionActionCode = RANGE_TARGET_RETREAT;
        unit.missionRuntimeState.rangeTargetToken = target;
        unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
        unit.missionRuntimeState.rangeTargetMode = rangeFlag == 0 ? 3 : rangeFlag & 0xFF;
    }

    /**
     * Native: MissionScriptRuntime::initializeStandStillOrderUnit @0056D572.
     * Fully ported.
     */
    public void initializeStandStillOrderUnit(Unit unit) {
        unit.resetScenarioMissionUnitScriptState(this);
        unit.missionActionCode = STAND_STILL_ORDER;
        unit.missionRuntimeState.missionScriptCell = unit.m_pTargetHandle.getCell();
        unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
    }

    /**
     * Native: MissionScriptRuntime::initializeRetreatOrderUnit @0056DA4F.
     * Fully ported.
     */
    public void initializeRetreatOrderUnit(Unit unit) {
        unit.resetScenarioMissionUnitScriptState(this);
        unit.missionActionCode = RETREAT_ORDER;
        unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
    }

    /**
     * Native: MissionScriptRuntime::initializePickupOrderUnit @0056DA98.
     * Fully ported.
     */
    public void initializePickupOrderUnit(Unit unit, int x, int y) {
        unit.resetScenarioMissionUnitScriptState(this);
        unit.missionActionCode = PICKUP_ORDER;
        unit.missionRuntimeState.commandCell = ((y & 0xFF) << 8) | (x & 0xFF);
        unit.movementState.missionReentryPending = 0;
        unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
    }

    /**
     * Native: MissionScriptRuntime::initializeEnterBuildingOrderUnit @0056D22B.
     * Fully ported.
     */
    public void initializeEnterBuildingOrderUnit(Unit unit, Building building) {
        unit.resetScenarioMissionUnitScriptState(this);
        unit.missionActionCode = INTERACT;
        unit.missionRuntimeState.interactionTarget = building;
        unit.missionRuntimeState.attackRange = 1;
        unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
        unit.missionRuntimeState.commandCell = building.m_pTargetHandle.getCell();
        switch (building.sizeX & 0xFF) {
            case 1, 2 -> {
            }
            case 3, 4 -> {
                unit.missionRuntimeState.attackRange = 2;
                unit.missionRuntimeState.commandCell = (unit.missionRuntimeState.commandCell + 0x101) & 0xFFFF;
            }
            default -> {
                unit.missionRuntimeState.attackRange = 3;
                unit.missionRuntimeState.commandCell = (unit.missionRuntimeState.commandCell + 0x202) & 0xFFFF;
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::initializePickupAllSacksOrderUnit @0056DAFE.
     * Fully ported.
     */
    public void initializePickupAllSacksOrderUnit(Unit unit) {
        unit.resetScenarioMissionUnitScriptState(this);
        unit.missionActionCode = PICKUP_ALL_SACKS_ORDER;
        unit.movementState.missionReentryPending = 0;
        unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
    }

    /**
     * Native: MissionScriptRuntime::initializeSpecialMissionStatusUnit @0056DB41.
     * Fully ported.
     */
    public void initializeSpecialMissionStatusUnit(Unit unit) {
        unit.resetScenarioMissionUnitScriptState(this);
        unit.missionActionCode = HIDE;
        unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
    }

    /**
     * Native: MissionScriptRuntime::initializeFixedEvacuationMissionStatusUnit @0056DB71.
     * Fully ported.
     */
    public void initializeFixedEvacuationMissionStatusUnit(Unit unit) {
        unit.resetScenarioMissionUnitScriptState(this);
        unit.missionActionCode = FIXED_EVACUATION;
        unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
    }

    /**
     * Native: MissionScriptRuntime::initializeWanderTargetCellGroup @005708FF.
     * Fully ported.
     */
    public void initializeWanderTargetCellGroup(UnitGroup group) {
        group.resetScenarioMissionGroupScriptState(this);
        group.missionState.scriptRuntimeState = GROUP_SCRIPT_STATE_WANDER_TARGET_CELL;
        Unit headUnit = group.getHeadUnit();
        group.missionState.setScriptTargetCell(headUnit.m_pTargetHandle.getCell());
        group.missionState.clearScriptTargetCellAge();
    }

    /**
     * Native: MissionScriptRuntime::executeIncrementScriptVariableInstant @00577C18.
     */
    public void executeIncrementScriptVariableInstant(ScriptInstant instant) {
        incrementScriptVariable(instant.argValues[0]);
    }

    /**
     * Native: MissionScriptRuntime::executeSetPlayerFormationModeInstant @00577C8D.
     */
    public void executeSetPlayerFormationModeInstant(ScriptInstant instant) {
        instant.player.battlePreferences.formationMode = instant.argValues[0] & 0xFF;
    }

    /**
     * Native: MissionScriptRuntime::executeNoOpInstant @00577E9F.
     */
    public void executeNoOpInstant(ScriptInstant instant) {
    }

    /**
     * Native: MissionScriptRuntime::executeTransferUnitInventoryItemInstant @00577F8D.
     */
    public void executeTransferUnitInventoryItemInstant(ScriptInstant instant) {
        Unit sourceUnit = instant.unit;
        Unit targetUnit = (Unit) instant.secondaryTarget;
        Item item = sourceUnit.inventory.takeOneByHash(instant.itemId);
        CServerApp.sendUnitInventoryItemsUpdate(sourceUnit, sourceUnit.owner, 0, 0);
        if (item != null) {
            targetUnit.inventory.addItem(item);
            CServerApp.sendUnitInventoryItemsUpdate(targetUnit, targetUnit.owner, 0, 0);
        }
    }

    /**
     * Native: MissionScriptRuntime::executeAddUnitInventoryItemInstant @0057800B.
     */
    public void executeAddUnitInventoryItemInstant(ScriptInstant instant) {
        Item item = Globals.staticDataMgr.createItemFromPackedHash(instant.itemId);
        if (item != null) {
            instant.unit.inventory.addItem(item);
        }
        CServerApp.sendUnitInventoryItemsUpdate(instant.unit, instant.unit.owner, 0, 0);
    }

    /**
     * Native: MissionScriptRuntime::executeRemoveUnitInventoryItemInstant @00578066.
     */
    public void executeRemoveUnitInventoryItemInstant(ScriptInstant instant) {
        instant.unit.inventory.takeOneByHash(instant.itemId);
        CServerApp.sendUnitInventoryItemsUpdate(instant.unit, instant.unit.owner, 0, 0);
    }

    /**
     * Native: MissionScriptRuntime::executeRemoveItemFromActiveUnitInventoriesInstant @005780DE.
     */
    public void executeRemoveItemFromActiveUnitInventoriesInstant(ScriptInstant instant) {
        if (worldMap.activeUnits0xA456C != null) {
            for (Unit unit : worldMap.activeUnits0xA456C) {
                Item item = unit.inventory.takeOneByHash(instant.itemId);
                if (item != null) {
                    CServerApp.sendUnitInventoryItemsUpdate(unit, unit.owner, 0, 0);
                }
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::executeSendUnitEquipmentStateUpdateInstant @005781C4.
     */
    public void executeSendUnitEquipmentStateUpdateInstant(ScriptInstant instant) {
        CServerApp.sendUnitEquipmentStateUpdate(instant.unit, instant.unit.owner, 0x0FFB);
    }

    /**
     * Native: MissionScriptRuntime::executeSendUnitEquipmentStateUpdateDuplicateInstant @005781F1.
     */
    public void executeSendUnitEquipmentStateUpdateDuplicateInstant(ScriptInstant instant) {
        CServerApp.sendUnitEquipmentStateUpdate(instant.unit, instant.unit.owner, 0x0FFB);
    }

    /**
     * Native: MissionScriptRuntime::executeHideUnitFromMissionMapInstant @0057821E.
     */
    public void executeHideUnitFromMissionMapInstant(ScriptInstant instant) {
        instant.unit.hideFromMissionMap();
    }

    /**
     * Native: MissionScriptRuntime::executeReturnUnitToMissionMapInstant @00578236.
     */
    public void executeReturnUnitToMissionMapInstant(ScriptInstant instant) {
        instant.unit.returnToMissionMap();
    }

    /**
     * Native: MissionScriptRuntime::executeSwapHiddenUnitWithTargetUnitInstant @0057824E.
     */
    public void executeSwapHiddenUnitWithTargetUnitInstant(ScriptInstant instant) {
        instant.unit.hideFromMissionMap();
        Unit targetUnit = (Unit) instant.secondaryTarget;
        targetUnit.returnToMissionMapNearCell(
                instant.unit.m_pTargetHandle.getX(),
                instant.unit.m_pTargetHandle.getY(),
                3
        );
    }

    /**
     * Native: MissionScriptRuntime::executeTransferUnitToPlayerInstant @00578291.
     */
    public void executeTransferUnitToPlayerInstant(ScriptInstant instant) {
        instant.unit.transferToPlayerForMissionScript(instant.player);
    }

    /**
     * Native: MissionScriptRuntime::executeDropUnitInventoryDeathSackInstant @00578330.
     */
    public void executeDropUnitInventoryDeathSackInstant(ScriptInstant instant) {
        instant.unit.dropInventoryToDeathSackForScript();
    }

    /**
     * Native: MissionScriptRuntime::executeTransferGroupUnitsToPlayerInstant @005782BA.
     */
    public void executeTransferGroupUnitsToPlayerInstant(ScriptInstant instant) {
        for (Unit unit : new ArrayList<>(instant.group.units)) {
            unit.transferToPlayerForMissionScript(instant.player);
        }
    }

    /**
     * Native: MissionScriptRuntime::executeTransientPointSpellCastInstant @005783BE.
     */
    public void executeTransientPointSpellCastInstant(ScriptInstant instant) {
        int skillLevel = instant.argValues[5] == 0 ? 99 : instant.argValues[5] & 0xFFFF;
        Globals.gameServer.objectLists.queueTransientPointSpellCast(
                instant.argValues[0] & 0xFF,
                instant.argValues[1] & 0xFF,
                instant.argValues[2] & 0xFF,
                instant.argValues[3] & 0xFF,
                instant.argValues[4],
                skillLevel
        );
    }

    /**
     * Native: MissionScriptRuntime::executeAdjustPlayerGoldInstant @0057830F.
     */
    public void executeAdjustPlayerGoldInstant(ScriptInstant instant) {
        instant.player.adjustGoldAndNotify(instant.argValues[0], 0);
    }

    /**
     * Native: MissionScriptRuntime::executeTransientTargetSpellCastInstant @00578440.
     */
    public void executeTransientTargetSpellCastInstant(ScriptInstant instant) {
        Globals.gameServer.objectLists.queueTransientTargetSpellCast(
                instant.argValues[0] & 0xFF,
                instant.argValues[1] & 0xFF,
                instant.unit,
                instant.argValues[2],
                instant.argValues[3] & 0xFFFF
        );
    }

    /**
     * Native: MissionScriptRuntime::executeTransientCellSpellCastInstant @0057847E.
     */
    public void executeTransientCellSpellCastInstant(ScriptInstant instant) {
        int x = instant.argValues[2] & 0xFF;
        int y = instant.argValues[3] & 0xFF;
        TransientSpellCastSpec spec = new TransientSpellCastSpec();
        spec.spellId = instant.argValues[0] & 0xFF;
        spec.skillLevel = instant.argValues[1] & 0xFF;
        spec.sourceX = x;
        spec.sourceY = y;
        spec.targetX = x;
        spec.targetY = y;
        if (instant.argValues[4] != 0) {
            spec.sourceX = instant.argValues[4] & 0xFF;
            spec.sourceY = instant.argValues[5] & 0xFF;
            spec.targetX = instant.argValues[6] & 0xFF;
            spec.targetY = instant.argValues[7] & 0xFF;
        }
        worldMap.setTransientSpellCastAtCell((y << 8) | x, spec);
    }

    /**
     * Native: MissionScriptRuntime::executeSetBuildingHitPointsInstant @0057855F.
     */
    public void executeSetBuildingHitPointsInstant(ScriptInstant instant) {
        instant.building.healthCurrent = (short) instant.argValues[0];
    }

    /**
     * Native: MissionScriptRuntime::executeRelocateUnitMissionEntryInstant @0057857D.
     */
    public void executeRelocateUnitMissionEntryInstant(ScriptInstant instant) {
        worldMap.relocateScenarioMissionEntryUnit(
                instant.unit,
                instant.argValues[0] & 0xFF,
                instant.argValues[1] & 0xFF
        );
        if (instant.unit.owner.isActive != 0) {
            enterLoadedScenarioGroupUnitScriptState(instant.unit);
        }
    }

    /**
     * Native: MissionScriptRuntime::executeDrainUnitInventoryToTargetInstant @005785CB.
     */
    public void executeDrainUnitInventoryToTargetInstant(ScriptInstant instant) {
        Unit targetUnit = (Unit) instant.secondaryTarget;
        targetUnit.inventory.drainItemsFrom(instant.unit.inventory);
        instant.unit.inventory = new Inventory();
        CServerApp.sendUnitInventoryItemsUpdate(instant.unit, instant.unit.owner, 0, 0);
        CServerApp.sendUnitInventoryItemsUpdate(targetUnit, targetUnit.owner, 0, 0);
    }

    /**
     * Native: MissionScriptRuntime::executeSetAreaEffectDurationInstant @00578696.
     */
    public void executeSetAreaEffectDurationInstant(ScriptInstant instant) {
        int packedCell = (short) instant.argValues[0] + (short) instant.argValues[1] * 0x100;
        AreaEffect[] layers = worldMap.getAreaEffectLayersAtCell(packedCell);
        if (layers != null) {
            for (AreaEffect areaEffect : layers) {
                if (areaEffect != null && (areaEffect.key & 0xFFFF) == instant.argValues[2]) {
                    areaEffect.durationTicks = (short) instant.argValues[3];
                }
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::executeSetUnitEffectValueInstant @0057871F.
     */
    public void executeSetUnitEffectValueInstant(ScriptInstant instant) {
        for (Effect effect : instant.unit.effects) {
            if ((effect.key & 0xFFFF) == instant.argValues[0]) {
                effect.mValue.setS1(instant.argValues[1] & 0xFFFF);
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::executeSetOutpostRespawnTimerInstant @0057877D.
     */
    public void executeSetOutpostRespawnTimerInstant(ScriptInstant instant) {
        for (Building building : Globals.gameServer.objectLists.buildings.buildings) {
            if (building.isOutpostBuilding() != 0
                    && building instanceof Outpost outpost
                    && outpost.groupKey == instant.argValues[0]) {
                outpost.respawnTimerTicks = instant.argValues[1];
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::executeHideGroupUnitsFromMissionMapInstant @005787F2.
     */
    public void executeHideGroupUnitsFromMissionMapInstant(ScriptInstant instant) {
        for (Unit unit : instant.group.units) {
            unit.hideFromMissionMap();
        }
    }

    /**
     * Native: MissionScriptRuntime::executeReturnGroupUnitsToMissionMapInstant @00578836.
     */
    public void executeReturnGroupUnitsToMissionMapInstant(ScriptInstant instant) {
        for (Unit unit : instant.group.units) {
            unit.returnToMissionMap();
        }
    }

    /**
     * Native: MissionScriptRuntime::executeSetUnitStatValueInstant @0057887A.
     */
    public void executeSetUnitStatValueInstant(ScriptInstant instant) {
        int value = instant.argValues[1] & 0xFFFF;
        switch (EffectId.fromId(instant.argValues[0])) {
            case HEALTH -> instant.unit.m_nHP = (short) value;
            case DEFENCE -> instant.unit.unitStatData.defence = (short) value;
            case ABSORBTION -> instant.unit.unitStatData.absorbtion = (short) value;
            default -> {
            }
        }
        CServerApp.notifyBuildingStateChanged(instant.unit);
    }

    /**
     * Native: MissionScriptRuntime::executeClearScenarioScriptReferencedFlagInstant @005781A7.
     */
    public void executeClearScenarioScriptReferencedFlagInstant(ScriptInstant instant) {
        instant.group.missionState.clearScenarioScriptReferenced();
    }

    /**
     * Native: MissionScriptRuntime::executeDiplomacyStateInstant @00577EAC.
     */
    public void executeDiplomacyStateInstant(ScriptInstant instant) {
        int rowPlayerId = instant.argValues[0];
        int columnPlayerId = instant.argValues[1];
        int relationFlags = missionDiplomacyState.relationFlags(rowPlayerId, columnPlayerId);
        missionDiplomacyState.setRelationFlags(
                rowPlayerId,
                columnPlayerId,
                (relationFlags & 0xFC) + (instant.argValues[2] & 0xFF)
        );
        for (Player player : playerList.players) {
            if ((short) player.playerId == rowPlayerId || (short) player.playerId == columnPlayerId) {
                CServerApp.sendDiplomacyStateSnapshot(player);
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::executeScenarioSetVarInstant @00577961.
     * Fully ported.
     */
    public void executeScenarioSetVarInstant(ScriptInstant instant) {
        Globals.scenarioLib.setVar(instant.argValues[0], instant.argValues[1]);
    }

    /**
     * Native: MissionScriptRuntime::appendScenarioTransientRuntimeCounterInstantWords @00577982.
     * Fully ported.
     */
    public void appendScenarioTransientRuntimeCounterInstantWords(CustomList<Short> words) {
        int transientRuntimeCounterType = InstantType.SCENARIO_TRANSIENT_STATE.id
                | InstantType.INCREMENT_RUNTIME_COUNTERS.id;
        for (int instantIndex = 0; instantIndex < scriptInstants.size() - 1; instantIndex++) {
            ScriptInstant instant = scriptInstants.get(instantIndex);
            if (instant.type != transientRuntimeCounterType) {
                continue;
            }

            int firstEmptyArgIndex = 2;
            while (firstEmptyArgIndex < instant.argValues.length && instant.argValues[firstEmptyArgIndex] != 0) {
                firstEmptyArgIndex++;
            }
            words.add((short) instant.argValues[0]);
            words.add((short) instant.argValues[1]);
            words.add((short) (firstEmptyArgIndex - 3));
            for (int argIndex = 2; argIndex < instant.argValues.length; argIndex++) {
                if (instant.argValues[argIndex] != 0) {
                    words.add((short) instant.argValues[argIndex]);
                }
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::executeScenarioTransientStateInstant @00577B16.
     */
    public void executeScenarioTransientStateInstant(ScriptInstant instant) {
        int varId = instant.argValues[0] + SCENARIO_TRANSIENT_VAR_BASE;
        int state = instant.argValues[1];
        if (state == 1) {
            if (Globals.scenarioLib.getVar(varId) == 0) {
                Globals.scenarioLib.setVar(varId, 1);
            }
        } else if (state == 2) {
            int current = Globals.scenarioLib.getVar(varId);
            if (current != 4) {
                if ((current & 2) == 0) {
                    CServerApp.sendQuestObjectivesQueryOpen(null, 0xFD, 0);
                }
                Globals.scenarioLib.setVar(varId, 3);
            }
        } else if (state == 4) {
            CServerApp.sendQuestObjectivesQueryOpen(null, 0xFE, 0);
            Globals.scenarioLib.setVar(varId, 5);
        } else {
            Globals.scenarioLib.setVar(varId, state);
        }
    }

    /**
     * Native support extracted from script variable dword accesses in MissionScriptRuntime @00574BA1, @0057762E,
     *
     * @00577C18, and @00577C6A.
     */
    public int readScriptVariable(int index) {
        return ByteBuffer.wrap(scriptVariables).order(ByteOrder.LITTLE_ENDIAN).getInt(index * Integer.BYTES);
    }

    /**
     * Native support extracted from script variable dword writes in ScenarioMapLoader::materializeScenarioScriptRuntime
     *
     * @00562745 and MissionScriptRuntime @0057762E / @00577C6A.
     */
    public void writeScriptVariable(int index, int value) {
        ByteBuffer.wrap(scriptVariables).order(ByteOrder.LITTLE_ENDIAN).putInt(index * Integer.BYTES, value);
    }

    /**
     * Native support extracted from MissionScriptRuntime::advanceScenarioScripts @0057485B and instant type 8 handler
     *
     * @00577C18.
     */
    public void incrementScriptVariable(int index) {
        writeScriptVariable(index, readScriptVariable(index) + 1);
    }

    /**
     * Native support extracted from MissionScriptRuntime::evaluateScriptPatterns @00574BA1 self-destruct flag reads.
     */
    public int readScriptPatternFiredFlag(int index) {
        return scriptPatternFiredFlags[index] & 0xFF;
    }

    /**
     * Native support extracted from MissionScriptRuntime::evaluateScriptPatterns @00574BA1 self-destruct flag writes.
     */
    public void writeScriptPatternFiredFlag(int index, int value) {
        scriptPatternFiredFlags[index] = (byte) value;
    }

    /**
     * Native support extracted from MissionScriptRuntime::MissionScriptRuntime @00568C9A and
     * MissionScriptRuntime::initializeWithWorldMap @00568DCD.
     */
    public void attachRuntimeReferences(CWorldMap worldMap, PlayerList playerList) {
        this.worldMap = worldMap;
        this.worldMap.attachMissionScriptRuntime(this);
        this.playerList = playerList;

        forceAllMissionGroupsUpdate = 0;
    }

    /**
     * Native: MissionScriptRuntime::processVirtualCasterSpellCasts @0057A47D.
     */
    public void processVirtualCasterSpellCasts() {
        for (VirtualCaster virtualCaster : Globals.gameServer.objectLists.virtualCasters) {
            TransientSpellCastSpec spec = virtualCaster.spellCastSpec;
            Spell spell = Globals.gameServer.runtimeSpells[spec.spellId & 0xFF];
            int removedHostileFlag = spell.isDefensive ? PLAYER_RELATION_HOSTILE_MASK : 0;
            Unit target = selectVirtualCasterTarget(
                    ((spec.sourceY & 0xFF) << 8) | (spec.sourceX & 0xFF),
                    virtualCaster.targetSearchRadius,
                    virtualCaster.owner,
                    removedHostileFlag
            );
            if (target != null) {
                if (spell.canTargetUnit(target)) {
                    Globals.gameServer.objectLists.queueTransientTargetSpellCast(
                            spec.sourceX,
                            spec.sourceY,
                            target,
                            spec.spellId,
                            spec.skillLevel
                    );
                } else {
                    Globals.gameServer.objectLists.queueTransientPointSpellCast(
                            spec.sourceX,
                            spec.sourceY,
                            target.m_pTargetHandle.getX(),
                            target.m_pTargetHandle.getY(),
                            spec.spellId,
                            spec.skillLevel
                    );
                }
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::selectVirtualCasterTarget @0057A307.
     */
    public Unit selectVirtualCasterTarget(int sourceCell, int targetSearchRadius, Player casterOwner,
                                          int removedHostileFlag) {
        worldMap.collectActiveUnitsNearCell(sourceCell, targetSearchRadius, casterOwner, primaryCandidateUnits);
        for (int index = 0; index < primaryCandidateUnits.size(); ) {
            Unit unit = primaryCandidateUnits.get(index);
            int relationFlag = getRelationFlags(casterOwner, unit.owner) & PLAYER_RELATION_HOSTILE_MASK;
            if (relationFlag == removedHostileFlag
                    || (unit.effectKeyFlags & spellEffectKeyMask(SpellId.INVISIBILITY)) != 0) {
                primaryCandidateUnits.remove(index);
            } else {
                index++;
            }
        }

        int count = primaryCandidateUnits.size();
        int selectedOrdinal = count == 0 ? 1 : randomInclusiveRange(1, count);
        Unit selected = null;
        int ordinal = 1;
        for (Unit unit : primaryCandidateUnits) {
            selected = unit;
            if (ordinal == selectedOrdinal) {
                break;
            }
            ordinal++;
        }
        return selected;
    }

    /**
     * Native: MissionScriptRuntime::CollectSpellTargets @00579DDF.
     */
    public void collectSpellTargets(Unit caster, Unit primaryTarget, CustomList<Unit> outTargets, int maxTargets, int maxRangeTiles) {
        outTargets.clear();
        recordUnitEngagement(caster, primaryTarget, 0);
        rebuildMissionTargetCandidates(caster.unitGroup);
        if (primaryCandidateUnits.isEmpty()) {
            outTargets.add(primaryTarget);
            return;
        }

        int maxRangeScore = (maxRangeTiles & 0xFF) << 8;
        int[] candidateScores = new int[100];
        Unit[] candidateUnits = new Unit[100];
        int candidateCount = 0;
        for (Unit unit : primaryCandidateUnits) {
            int score = computeSpellTargetScore(caster, unit) & 0xFFFF;
            if (score <= maxRangeScore) {
                candidateScores[candidateCount] = score;
                candidateUnits[candidateCount] = unit;
                candidateCount++;
            }
        }
        for (Unit unit : fallbackCandidateUnits) {
            int score = computeSpellTargetScore(caster, unit) & 0xFFFF;
            if (score <= maxRangeScore) {
                candidateScores[candidateCount] = score << 8;
                candidateUnits[candidateCount] = unit;
                candidateCount++;
            }
        }

        int targetLimit = maxTargets & 0xFF;
        int[] selectedScores = new int[10];
        int[] selectedIndexes = new int[10];
        Arrays.fill(selectedScores, 0xFFFA);
        Arrays.fill(selectedIndexes, 0xFFFA);
        for (int slot = 0; slot < targetLimit; slot++) {
            for (int index = 0; index < candidateCount; index++) {
                if (candidateScores[index] < selectedScores[slot]) {
                    selectedScores[slot] = candidateScores[index] & 0xFFFF;
                    selectedIndexes[slot] = index;
                }
            }
            if (selectedIndexes[slot] < 60000) {
                candidateScores[selectedIndexes[slot]] = 0xFFDC;
            }
        }

        outTargets.add(primaryTarget);
        for (int slot = 0; slot < targetLimit; slot++) {
            if (selectedIndexes[slot] < 65000
                    && selectedScores[slot] < 65000
                    && candidateUnits[selectedIndexes[slot]] != primaryTarget) {
                outTargets.add(candidateUnits[selectedIndexes[slot]]);
            }
        }
        while (outTargets.size() > targetLimit) {
            outTargets.removeLast();
        }
    }

    /**
     * Native: MissionScriptRuntime::MoveDeadCandidatesToFallbackList @00567DDF.
     * Fully ported.
     */
    public void moveDeadCandidatesToFallbackList() {
        fallbackCandidateUnits.clear();
        for (int index = 0; index < primaryCandidateUnits.size(); ) {
            Unit unit = primaryCandidateUnits.get(index);
            if (unit.m_nHP < 1) {
                primaryCandidateUnits.remove(index);
                fallbackCandidateUnits.add(unit);
            } else {
                index++;
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::recordUnitEngagement @0056E6B8.
     * Fully ported.
     */
    public void recordUnitEngagement(Unit source, Unit target, int gatedRelationUpdate) {
        target.missionRuntimeState.engagementActiveFlag = 1;
        target.missionRuntimeState.cell = source.m_pTargetHandle.getCell();
        target.missionRuntimeState.engagementCellRepeatCount = 0;
        missionDiplomacyState.updateRelationsForUnitEngagement(source, target, gatedRelationUpdate);
        scratchMissionUnitEventDTO.eventCode = MISSION_UNIT_EVENT_ENGAGEMENT;
        scratchMissionUnitEventDTO.runtimeSource = source;
        scratchMissionUnitEventDTO.runtimePayload = target;
        pendingMissionUnitEventDTOs.add(scratchMissionUnitEventDTO.copy());
        target.missionRuntimeState.lastEngagementSourceUnit = source;
        target.missionRuntimeState.lastEngagementTick = Globals.gameServer.serverLoopCounter;
        target.missionRuntimeState.engagementProjectedCell = computeEngagementProjectedCell(source, target);
    }

    /**
     * Native support extracted from MissionScriptRuntime::recordUnitEngagement @0056E6B8.
     * Fully ported.
     */
    public static int computeEngagementProjectedCell(Unit source, Unit target) {
        int dx = (target.m_pTargetHandle.packXdX() & 0xFFFF)
                - (source.m_pTargetHandle.packXdX() & 0xFFFF);
        int dy = (target.m_pTargetHandle.packYdY() & 0xFFFF)
                - (source.m_pTargetHandle.packYdY() & 0xFFFF);
        int distance = Math.max(Math.abs(dx), Math.abs(dy));
        double scale = 10.0 / (double) distance;
        int projectedY = (int) ((target.m_pTargetHandle.getY() & 0xFF) + dy * scale);
        int projectedX = (int) ((target.m_pTargetHandle.getX() & 0xFF) + dx * scale);
        return ((projectedY & 0xFF) << 8) | (projectedX & 0xFF);
    }

    /**
     * Native: MissionScriptRuntime::Serialize @0057468D.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        if (ar.isStoring()) {
            ar.writeBytes(scriptVariables);
            ar.writeBytes(scriptPatternFiredFlags);
            ar.writeBytes(turnPerfMonitor.toNativeBytes());
            ar.writeBytes(serializedStateBlob);
            ar.writeBytes(missionDiplomacyState.toNativeBytes());
            ar.writeByte(serializedStateByte0);
            ar.writeByte(serializedStateByte1);
            ar.writeInt(missionTurnCounter);
            ar.writeInt(missionCompleteCount);
            ar.writeInt(specialMissionFailureArmed);
            ar.writeInt(missionFailureValue);
        } else {
            copyInto(scriptVariables, ar.readBytes(scriptVariables.length));
            copyInto(scriptPatternFiredFlags, ar.readBytes(scriptPatternFiredFlags.length));
            turnPerfMonitor.readNativeBytes(ar.readBytes(PerfMonitorState.SERIALIZED_SIZE));
            copyInto(serializedStateBlob, ar.readBytes(serializedStateBlob.length));
            missionDiplomacyState.readNativeBytes(ar.readBytes(MissionDiplomacyState.SERIALIZED_SIZE));
            serializedStateByte0 = ar.readByte() & 0xFF;
            serializedStateByte1 = ar.readByte() & 0xFF;
            missionTurnCounter = ar.readInt();
            missionCompleteCount = ar.readInt();
            specialMissionFailureArmed = ar.readInt();
            missionFailureValue = ar.readInt();
        }
    }

    /**
     * Native: MissionScriptRuntime::bindMissionDiplomacyStateOwner @00576FB1.
     * Fully ported.
     */
    public void bindMissionDiplomacyStateOwner() {
        missionDiplomacyState.setOwnerRuntime(this);
    }

    /**
     * Native: MissionScriptRuntime::loadMissionScriptReg @005756A1.
     * Fully ported.
     */
    public void loadMissionScriptReg(String missionScriptFilename) throws Exception {
        buildScriptReferenceMaps();
        scriptResourcePath = SCRIPT_REG_PREFIX + stripExtension(missionScriptFilename) + SCRIPT_REG_SUFFIX;
        if (!Globals.gameFileManager.exists(scriptResourcePath)) {
            return;
        }

        scriptResource = ResInHeap.load(scriptResourcePath);
        try {
            for (String sectionName : scriptResource.getRootChildNames()) {
                int digitStart = firstDigitIndex(sectionName);
                if (digitStart < 0) {
                    continue;
                }
                int digit = parseLeadingInt(sectionName.substring(digitStart));
                if (digit == 0) {
                    continue;
                }

                char kind = sectionName.charAt(0);
                if (kind == 'C') {
                    loadScriptCheck(sectionName, digit);
                } else if (kind == 'I') {
                    loadScriptInstant(sectionName, digit);
                } else if (kind == 'P') {
                    loadScriptPattern(sectionName, digit);
                }
            }
            applyInitialScriptVariables();
        } finally {
            clearScriptLoadReferenceState();
        }
        System.out.printf("SCRIPT: Script %s is loaded.%n", scriptResourcePath);
        System.out.printf(
                "SCRIPT: %d checks, %d instants, %d patterns->%n",
                scriptChecks.size(),
                scriptInstants.size(),
                scriptPatterns.size()
        );
    }

    /**
     * Native: MissionScriptRuntime::buildScriptReferenceMaps @00574316.
     * Fully ported.
     */
    public void buildScriptReferenceMaps() {
        scriptUnitsByReferenceKey.clear();
        scriptGroupsByReferenceKey.clear();
        scriptPlayersByReferenceIndex.clear();

        int playerIndex = 0;
        for (Player player : playerList.players) {
            playerIndex++;
            int groupIndex = 0;
            for (UnitGroup group : player.unitGroups) {
                groupIndex++;
                int unitIndex = 0;
                for (Unit unit : group.units) {
                    unitIndex++;
                    scriptUnitsByReferenceKey.put(scriptUnitKey(playerIndex, groupIndex, unitIndex), unit);
                }
                scriptGroupsByReferenceKey.put(scriptGroupKey(playerIndex, groupIndex), group);
            }
            scriptPlayersByReferenceIndex.put(playerIndex, player);
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::loadMissionScriptReg @005756A1 tail.
     */
    public void clearScriptLoadReferenceState() {
        scriptResource = null;
        scriptUnitsByReferenceKey.clear();
        scriptGroupsByReferenceKey.clear();
        scriptPlayersByReferenceIndex.clear();
    }

    /**
     * Native: MissionScriptRuntime::InitializeLoadedScenarioGroup @00570ABA.
     * Fully ported.
     */
    public void initializeLoadedScenarioGroup(UnitGroup group, int guardRange) {
        group.resetScenarioMissionGroupScriptState(this);
        for (Unit unit : group.units) {
            enterLoadedScenarioGroupUnitScriptState(unit);
        }
        group.missionState.scriptRuntimeState = GROUP_SCRIPT_STATE_PATROL;
        refreshScenarioGroupCenterAndGuardRange(group);
        if (guardRange == 0) {
            if (group.missionState.getScenarioGroupGuardRange() < minimalGuardRange) {
                group.missionState.setScenarioGroupGuardRange(0x14);
            }
        } else if (group.missionState.getScenarioGroupGuardRange() < (guardRange & 0xFF)) {
            group.missionState.setScenarioGroupGuardRange(guardRange & 0xFF);
        }
        group.missionState.clearScenarioGroupGuardRefreshFlag();
    }

    /**
     * Native: MissionScriptRuntime::enterLoadedScenarioGroupUnitScriptState @0056D4BA.
     * Fully ported.
     */
    public void enterLoadedScenarioGroupUnitScriptState(Unit unit) {
        unit.resetScenarioMissionUnitScriptState(this);
        unit.missionActionCode = SCRIPT_CELL_STATUS;
        if (!unit.m_pTargetHandle.isSubPosUnknown()) {
            unit.missionRuntimeState.missionScriptCell = unit.movementState.packPositionCell();
        } else {
            unit.missionRuntimeState.missionScriptCell = unit.m_pTargetHandle.getCell();
        }
        unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
    }

    /**
     * Native: MissionScriptRuntime::resetUnitToScenarioCellCommand @0056D393.
     * Fully ported.
     */
    public void resetUnitToScenarioCellCommand(Unit unit, int x, int y) {
        unit.resetScenarioMissionUnitScriptState(this);
        unit.missionActionCode = COMMAND_CELL_OR_TARGET;
        unit.missionRuntimeState.commandCell = ((y & 0xFF) << 8) | (x & 0xFF);
        unit.missionRuntimeState.command = UNIT_MISSION_COMMAND_NONE;
    }

    /**
     * Native: MissionScriptRuntime::RefreshScenarioGroupCenterAndGuardRange @0057026E.
     * Fully ported.
     */
    public void refreshScenarioGroupCenterAndGuardRange(UnitGroup group) {
        refreshScenarioGroupCenterAndFootprintRange(group);
        group.missionState.copyScenarioGroupCenterAndGuardRangeFromFootprint();
    }

    /**
     * Native: MissionScriptRuntime::RefreshScenarioGroupCenterAndFootprintRange @0056F850.
     * Fully ported.
     */
    public int refreshScenarioGroupCenterAndFootprintRange(UnitGroup group) {
        int count = group.units.size() & 0xFF;
        if (count == 0) {
            return 0;
        }

        int totalXdX = 0;
        int totalYdY = 0;
        for (Unit unit : group.units) {
            totalXdX += unit.m_pTargetHandle.packXdX() & 0xFFFF;
            totalYdY += unit.m_pTargetHandle.packYdY() & 0xFFFF;
        }

        int averageXdX = totalXdX / count;
        int averageYdY = totalYdY / count;
        int centerCell = (((averageYdY >>> 8) & 0xFF) << 8) | ((averageXdX >>> 8) & 0xFF);
        int centerSubpos = (averageYdY << 16) | (averageXdX & 0xFFFF);

        int maxMemberDistance = 0;
        int maxSightRange = 0;
        int maxDistancePlusSight = 0;
        for (Unit unit : group.units) {
            int distance = cellChebyshevDistance(unit.m_pTargetHandle.getCell(), centerCell);
            maxMemberDistance = Math.max(maxMemberDistance, distance);
            maxSightRange = Math.max(maxSightRange, unit.sightRange & 0xFF);
            maxDistancePlusSight = Math.max(maxDistancePlusSight, distance + (unit.sightRange & 0xFF));
        }
        group.missionState.setScenarioGroupCenterAndRanges(
                centerSubpos,
                centerCell,
                maxMemberDistance,
                maxSightRange,
                maxDistancePlusSight
        );
        return centerSubpos;
    }

    /**
     * Native: MissionScriptRuntime::refreshUnitGroupCellCenterAndFootprintRange @0056FA63.
     * Fully ported.
     */
    public int refreshUnitGroupCellCenterAndFootprintRange(UnitGroup group) {
        int count = group.units.size() & 0xFF;
        if (count == 0) {
            return 0;
        }

        int totalX = 0;
        int totalY = 0;
        for (Unit unit : group.units) {
            totalX += unit.m_pTargetHandle.getX() & 0xFF;
            totalY += unit.m_pTargetHandle.getY() & 0xFF;
        }

        int averageX = totalX / count;
        int averageY = totalY / count;
        int centerCell = ((averageY & 0xFF) << 8) | (averageX & 0xFF);
        int centerSubpos = (averageY << 16) | (averageX & 0xFFFF);

        int maxMemberDistance = 0;
        int maxSightRange = 0;
        int maxDistancePlusSight = 0;
        for (Unit unit : group.units) {
            int distance = cellChebyshevDistance(unit.m_pTargetHandle.getCell(), centerCell);
            maxMemberDistance = Math.max(maxMemberDistance, distance);
            maxSightRange = Math.max(maxSightRange, unit.sightRange & 0xFF);
            maxDistancePlusSight = Math.max(maxDistancePlusSight, distance + (unit.sightRange & 0xFF));
        }
        group.missionState.setScenarioGroupCenterAndRanges(
                centerSubpos,
                centerCell,
                maxMemberDistance,
                maxSightRange,
                maxDistancePlusSight
        );
        return centerSubpos;
    }

    /**
     * Native support extracted from getCellChebyshevDistance used by
     * MissionScriptRuntime::RefreshScenarioGroupCenterAndFootprintRange @0056F850.
     */
    public static int cellChebyshevDistance(int cellA, int cellB) {
        return CWorldMap.getChebyshevDistance(
                cellA & 0xFF,
                (cellA >>> 8) & 0xFF,
                cellB & 0xFF,
                (cellB >>> 8) & 0xFF
        );
    }

    /**
     * Native support extracted from CWorldMap::getChebyshevDistance @00551463 callers in script check handlers
     * at 005770D1, 0057714D, 00577268, 0057745B, 00577527, and 005778AD.
     */
    public static int chebyshevDistance(Unit first, Unit second) {
        return chebyshevDistance(
                first.m_pTargetHandle.getX(),
                first.m_pTargetHandle.getY(),
                second.m_pTargetHandle.getX(),
                second.m_pTargetHandle.getY()
        );
    }

    /**
     * Native support extracted from CWorldMap::getChebyshevDistance @00551463 callers in script check handlers
     * at 005770D1, 0057714D, 00577268, 0057745B, 00577527, and 005778AD.
     */
    public static int chebyshevDistance(int x1, int y1, int x2, int y2) {
        return CWorldMap.getChebyshevDistance(x1, y1, x2, y2);
    }

    /**
     * Native support extracted from script item checks @005773AA, @00577527, and @005775B1.
     */
    public static boolean inventoryHasItemByHash(Inventory inventory, int itemHash) {
        int normalizedHash = itemHash & 0xFFFF;
        for (Item item : inventory.items) {
            if (item.getIdHashLowWord() == normalizedHash) {
                return true;
            }
        }
        return false;
    }

    /**
     * Native support extracted from MissionScriptRuntime::resetScenarioMissionGroupRuntimeContext @00570E22.
     * Fully ported.
     */
    public void resetScenarioMissionGroupRuntimeContext(UnitGroup group) {
        for (Unit unit : group.units) {
            unit.resetScenarioMissionUnitRuntimeContext(this);
            unit.missionRuntimeState.groupScriptState = 0;
        }
        group.missionState.setMissionScriptSpeedOverride(0);
        group.missionState.scriptRuntimeState = 0;
    }

    /**
     * Native: MissionScriptRuntime::loadScriptCheck @00575D05.
     * Fully ported.
     */
    public void loadScriptCheck(String sectionName, int digit) {
        int type = scriptResource.getInt(sectionName, "Type", 0);
        if (type == 0) {
            return;
        }

        ScriptCheck check = new ScriptCheck(digit, type);
        copyScriptArgValues(sectionName, check.argValues);
        List<Integer> indexes = new ArrayList<>();
        if (scriptResource.getByteArray(sectionName, "Unit", indexes)) {
            check.unit = findScriptUnit(indexes);
            if (check.unit == null) {
                logMissingUnit(indexes);
                return;
            }
        }
        if (scriptResource.getByteArray(sectionName, "Group", indexes)) {
            check.group = findScriptGroup(indexes);
            if (check.group == null) {
                logMissingGroup(indexes);
                return;
            }
        }
        if (scriptResource.getByteArray(sectionName, "Player", indexes)) {
            check.player = findScriptPlayer(indexes);
            if (check.player == null) {
                logMissingPlayer(indexes);
                return;
            }
        }
        if (scriptResource.getByteArray(sectionName, "Unit2", indexes)) {
            check.secondaryTarget = findScriptUnit(indexes);
            if (check.secondaryTarget == null) {
                logMissingUnit(indexes);
                return;
            }
        } else if (scriptResource.getByteArray(sectionName, "Group2", indexes)) {
            check.secondaryTarget = findScriptGroup(indexes);
            if (check.secondaryTarget == null) {
                logMissingGroup(indexes);
                return;
            }
        } else if (scriptResource.getByteArray(sectionName, "Player2", indexes)) {
            check.secondaryTarget = findScriptPlayer(indexes);
            if (check.secondaryTarget == null) {
                logMissingPlayer(indexes);
                return;
            }
        }
        scriptChecks.add(check);
    }

    /**
     * Native: MissionScriptRuntime::loadScriptInstant @00576380.
     * Fully ported.
     */
    public void loadScriptInstant(String sectionName, int digit) {
        int type = scriptResource.getInt(sectionName, "Type", 0);
        if (InstantType.fromId(type) == InstantType.INVALID) {
            return;
        }

        ScriptInstant instant = new ScriptInstant(digit, type);
        copyScriptArgValues(sectionName, instant.argValues);
        List<Integer> indexes = new ArrayList<>();
        if (scriptResource.getByteArray(sectionName, "Unit", indexes)) {
            instant.unit = findScriptUnit(indexes);
            if (instant.unit == null) {
                return;
            }
        }
        if (scriptResource.getByteArray(sectionName, "Group", indexes)) {
            instant.group = findScriptGroup(indexes);
            if (instant.group == null) {
                return;
            }
        }
        if (scriptResource.getByteArray(sectionName, "Player", indexes)) {
            instant.player = findScriptPlayer(indexes);
            if (instant.player == null) {
                return;
            }
        }
        if (scriptResource.getByteArray(sectionName, "Unit2", indexes)) {
            instant.secondaryTarget = findScriptUnit(indexes);
            if (instant.secondaryTarget == null) {
                logMissingUnit(indexes);
                return;
            }
        } else if (scriptResource.getByteArray(sectionName, "Group2", indexes)) {
            instant.secondaryTarget = findScriptGroup(indexes);
            if (instant.secondaryTarget == null) {
                logMissingGroup(indexes);
                return;
            }
        } else if (scriptResource.getByteArray(sectionName, "Player2", indexes)) {
            instant.secondaryTarget = findScriptPlayer(indexes);
            if (instant.secondaryTarget == null) {
                logMissingPlayer(indexes);
                return;
            }
        }
        setScriptInstant(digit, instant);
    }

    /**
     * Native: MissionScriptRuntime::loadScriptPattern @00576944.
     * Fully ported.
     */
    public void loadScriptPattern(String sectionName, int digit) {
        ScriptPattern pattern = new ScriptPattern();
        List<String> conditionStrings = new ArrayList<>();
        scriptResource.getStringArray(sectionName, "If", conditionStrings);
        for (String conditionString : conditionStrings) {
            pattern.conditions.add(parseScriptCondition(conditionString));
        }

        List<Short> instantIds = new ArrayList<>();
        scriptResource.getShortArray(sectionName, "Instants", instantIds);
        for (Short instantId : instantIds) {
            pattern.instantIds.add(Short.toUnsignedInt(instantId));
        }

        pattern.digit = digit;
        pattern.selfDestruct = scriptResource.getInt(sectionName, "SelfDestruct", 0);
        pattern.reserved0x10 = 0;
        scriptPatterns.add(pattern);
    }

    /**
     * Native: MissionScriptRuntime::applyInitialScriptVariables @00575BF9.
     * Fully ported.
     */
    public void applyInitialScriptVariables() {
        List<Integer> variables = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        if (!scriptResource.getIntArray("Variables", "Variable", variables)) {
            return;
        }
        if (!scriptResource.getIntArray("Variables", "Value", values)) {
            return;
        }
        if (variables.size() != values.size()) {
            return;
        }
        ByteBuffer scriptVariableBytes = ByteBuffer.wrap(scriptVariables).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < variables.size(); i++) {
            scriptVariableBytes.putInt(variables.get(i) * Integer.BYTES, values.get(i));
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::loadScriptInstant @00576380.
     */
    public void setScriptInstant(int index, ScriptInstant instant) {
        while (scriptInstants.size() <= index) {
            scriptInstants.add(null);
        }
        scriptInstants.set(index, instant);
    }

    /**
     * Native support extracted from MissionScriptRuntime::loadScriptCheck @00575D05 and FUN_00576380 @00576380.
     */
    public void copyScriptArgValues(String sectionName, int[] target) {
        List<Integer> params = new ArrayList<>();
        if (scriptResource.getIntArray(sectionName, "Param", params)) {
            for (int i = 0; i < params.size(); i++) {
                target[i] = params.get(i);
            }
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::loadScriptPattern @00576944.
     */
    public static ScriptCondition parseScriptCondition(String text) {
        int operatorStart = -1;
        int rightStart = -1;
        for (int i = 0; i < text.length(); i++) {
            if (operatorStart < 0) {
                if (!Character.isDigit(text.charAt(i))) {
                    operatorStart = i;
                }
            } else if (Character.isDigit(text.charAt(i))) {
                rightStart = i;
                break;
            }
        }
        if (operatorStart < 0 || rightStart < 0) {
            throw new IllegalArgumentException("Malformed script condition: " + text);
        }

        int left = parseLeadingInt(text.substring(0, operatorStart));
        int right = parseLeadingInt(text.substring(rightStart));
        String operator = text.substring(operatorStart, rightStart);
        int comparison = switch (operator) {
            case "=" -> ScriptCondition.EQUAL;
            case "!", "!=" -> ScriptCondition.NOT_EQUAL;
            case ">" -> ScriptCondition.GREATER_THAN;
            case "<" -> ScriptCondition.LESS_THAN;
            case ">=" -> ScriptCondition.GREATER_OR_EQUAL;
            case "<=" -> ScriptCondition.LESS_OR_EQUAL;
            default -> throw new IllegalArgumentException("Unsupported script condition operator: " + operator);
        };
        return new ScriptCondition(left, right, comparison);
    }

    /**
     * Native support extracted from MissionScriptRuntime::buildScriptReferenceMaps @00574316.
     */
    public static int scriptUnitKey(int playerIndex, int groupIndex, int unitIndex) {
        return playerIndex * 0x10000 + groupIndex * 0x100 + unitIndex;
    }

    /**
     * Native support extracted from MissionScriptRuntime::buildScriptReferenceMaps @00574316.
     */
    public static int scriptGroupKey(int playerIndex, int groupIndex) {
        return playerIndex * 0x100 + groupIndex;
    }

    /**
     * Native support extracted from MissionScriptRuntime::loadScriptCheck @00575D05 and FUN_00576380 @00576380.
     */
    public Unit findScriptUnit(List<Integer> indexes) {
        return scriptUnitsByReferenceKey.get(scriptUnitKey(indexes.get(0), indexes.get(1), indexes.get(2)));
    }

    /**
     * Native support extracted from MissionScriptRuntime::loadScriptCheck @00575D05 and FUN_00576380 @00576380.
     */
    public UnitGroup findScriptGroup(List<Integer> indexes) {
        return scriptGroupsByReferenceKey.get(scriptGroupKey(indexes.get(0), indexes.get(1)));
    }

    /**
     * Native support extracted from MissionScriptRuntime::loadScriptCheck @00575D05 and FUN_00576380 @00576380.
     */
    public Player findScriptPlayer(List<Integer> indexes) {
        return scriptPlayersByReferenceIndex.get(indexes.get(0));
    }

    /**
     * Native support extracted from MissionScriptRuntime::loadMissionScriptReg @005756A1.
     */
    public static String stripExtension(String filename) {
        int dot = filename.indexOf('.');
        return dot < 0 ? filename : filename.substring(0, dot);
    }

    /**
     * Native support extracted from MissionScriptRuntime::loadMissionScriptReg @005756A1.
     */
    public static int firstDigitIndex(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.isDigit(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Native support extracted from GetInt callers in MissionScriptRuntime::loadMissionScriptReg @005756A1.
     */
    public static int parseLeadingInt(String text) {
        int end = 0;
        while (end < text.length() && Character.isDigit(text.charAt(end))) {
            end++;
        }
        return Integer.parseInt(text.substring(0, end));
    }

    /**
     * Native support extracted from MissionScriptRuntime::loadScriptCheck @00575D05 and
     * MissionScriptRuntime::loadScriptInstant @00576380.
     */
    public static void logMissingUnit(List<Integer> indexes) {
        System.out.printf("SCRIPT: Cannot retrieve unit in check (%d,%d,%d).%n", indexes.get(0), indexes.get(1), indexes.get(2));
    }

    /**
     * Native support extracted from MissionScriptRuntime::loadScriptCheck @00575D05 and
     * MissionScriptRuntime::loadScriptInstant @00576380.
     */
    public static void logMissingGroup(List<Integer> indexes) {
        System.out.printf("SCRIPT: Cannot retrieve group in check (%d,%d).%n", indexes.get(0), indexes.get(1));
    }

    /**
     * Native support extracted from MissionScriptRuntime::loadScriptCheck @00575D05 and
     * MissionScriptRuntime::loadScriptInstant @00576380.
     */
    public static void logMissingPlayer(List<Integer> indexes) {
        System.out.printf("SCRIPT: Cannot retrieve player in check (%d).%n", indexes.getFirst());
    }

    /**
     * Native support extracted from MissionScriptRuntime::Serialize @0057468D CArchive::Read fixed-buffer copies.
     */
    public static void copyInto(byte[] dst, byte[] src) {
        System.arraycopy(src, 0, dst, 0, dst.length);
    }

    /**
     * Native: MissionScriptRuntime::ComputeSpellTargetScore @00579D62.
     */
    public int computeSpellTargetScore(Unit caster, Unit target) {
        int range = worldMap.getRangeInTiles(caster, target);
        int direction = worldMap.getDirection8Code(caster, target);
        int facingDelta = worldMap.getFacingAngularDistance8(caster.movementState.facing, direction);
        return ((range & 0xFF) << 8) + (facingDelta & 0xFF);
    }

    /**
     * Native: MissionScriptRuntime::applyWithdrawThresholds @0057393C.
     * Fully ported.
     */
    public void applyWithdrawThresholds(int mode0Percent, int mode1Percent, int mode2Percent, Player player) {
        if (worldMap.activeUnits0xA456C == null) {
            return;
        }
        for (Unit unit : worldMap.activeUnits0xA456C) {
            if (player == null ? unit.owner.isActive == 0 : unit.owner == player) {
                int modePercent = resolveUnitBattlePreferenceModePercent(unit, mode0Percent, mode1Percent, mode2Percent);
                if (modePercent >= 0) {
                    unit.missionRuntimeState.withdraw = unit.m_nMaxHP * modePercent / 100;
                }
            }
        }
    }

    /**
     * Native: MissionScriptRuntime::applyWimpyThresholds @00573B38.
     * Fully ported.
     */
    public void applyWimpyThresholds(int mode0Percent, int mode1Percent, int mode2Percent, Player player) {
        if (worldMap.activeUnits0xA456C == null) {
            return;
        }
        for (Unit unit : worldMap.activeUnits0xA456C) {
            if (player == null ? unit.owner.isActive == 0 : unit.owner == player) {
                int modePercent = resolveUnitBattlePreferenceModePercent(unit, mode0Percent, mode1Percent, mode2Percent);
                if (modePercent >= 0) {
                    unit.missionRuntimeState.wimpy = unit.m_nMaxHP * modePercent / 100;
                }
            }
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::applyWithdrawThresholds @0057393C and
     * MissionScriptRuntime::applyWimpyThresholds @00573B38.
     * Fully ported support for the shared battle-preference mode switch.
     */
    private static int resolveUnitBattlePreferenceModePercent(
            Unit unit,
            int mode0Percent,
            int mode1Percent,
            int mode2Percent
    ) {
        return switch (unit.missionRuntimeState.battlePreferenceMode) {
            case 0 -> mode0Percent;
            case 1 -> mode1Percent;
            case 2 -> mode2Percent;
            default -> {
                unit.missionRuntimeState.battlePreferenceMode = 0;
                yield -1;
            }
        };
    }

    /**
     * Native: MissionScriptRuntime::setPlayerFormationMode @005737BA.
     * Fully ported.
     */
    public void setPlayerFormationMode(Player player, int formationMode) {
        player.battlePreferences.formationMode = formationMode & 0xFF;
    }

    /**
     * Native support extracted from relation matrix reads in MissionScriptRuntime @0056B059 and @0056BB53.
     */
    public boolean isHostile(Player casterOwner, Player targetOwner) {
        return hasRelationFlag(casterOwner, targetOwner, PLAYER_RELATION_HOSTILE_MASK);
    }

    /**
     * Native support extracted from relation matrix reads in MissionScriptRuntime @0056B059, @0056BB53, and @00571E9B.
     */
    public boolean hasRelationFlag(Player casterOwner, Player targetOwner, int relationMask) {
        int row = casterOwner.playerId & 0xFFFF;
        int col = targetOwner.playerId & 0xFFFF;
        return missionDiplomacyState.hasRelationFlag(row, col, relationMask);
    }

    /**
     * Native support extracted from CServerApp::sendDiplomacyStateSnapshot @00504E87 relation-matrix reads.
     */
    public int getRelationFlags(Player rowOwner, Player columnOwner) {
        int row = rowOwner.playerId & 0xFFFF;
        int col = columnOwner.playerId & 0xFFFF;
        return missionDiplomacyState.relationFlags(row, col);
    }
}
