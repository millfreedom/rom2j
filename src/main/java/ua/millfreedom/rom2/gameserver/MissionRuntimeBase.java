package ua.millfreedom.rom2.gameserver;

import ua.millfreedom.rom2.gameserver.missionruntime.PerfMonitorState;
import ua.millfreedom.rom2.gameserver.missionruntime.MissionDiplomacyState;
import ua.millfreedom.rom2.gameserver.missionruntime.EventDTO;
import ua.millfreedom.rom2.gameserver.missionruntime.MissionTurnTimingStats;
import ua.millfreedom.rom2.gameserver.missionruntime.PrivateProfileReader;
import ua.millfreedom.rom2.model.UnitList;
import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.model.world.CWorldMap;
import ua.millfreedom.rom2.res.ResInHeap;

/**
 * Base native mission-runtime state initialized from MissionRuntimeBase::MissionRuntimeBase @0056811B.
 */
class MissionRuntimeBase {
    static final int MISSION_TARGET_SCORE_MATRIX_DIMENSION = 100;
    private static final String AI_REG_PATH = "world/data/ai.reg";
    private static final String AI_REG_SECTION_SCANNING = "Scanning";
    private static final String AI_REG_KEY_MINIMAL_GUARD_RANGE = "MinimalGuardRange";
    private static final int MINIMAL_GUARD_RANGE_DEFAULT = 10;

    //0x0000
    public final RandomRelated mRandomRelated = new RandomRelated();
    //0x0008
    public final PerfMonitorState turnPerfMonitor = new PerfMonitorState();
    //0x0038
    public final PerfMonitorState activatingPerfMonitor = new PerfMonitorState();
    //0x0068
    public final PerfMonitorState scriptPerfMonitor = new PerfMonitorState();
    //0x0098
    public final PerfMonitorState aiPerfMonitor = new PerfMonitorState();
    //0x00C8
    public final PrivateProfileReader privateProfileReader = new PrivateProfileReader();
    //0x08B0
    public int lastTurnElapsedMsLowDword;
    //0x08B4
    public int lastTurnElapsedMsHighDword;
    //0x08B8
    public final MissionTurnTimingStats turnTimingStats = new MissionTurnTimingStats();
    //0x0A48
    public int serializedStateByte0;
    //0x0A49
    public int serializedStateByte1;
    //0x0A4C
    public int missionTurnCounter;
    //0x0A50
    public CWorldMap worldMap;
    //0x0A54
    public byte[][] targetRelationModeTable = new byte[4][4];
    //0x0A64
    public final UnitList primaryCandidateUnits = new UnitList();
    //0x0A84
    public final UnitList fallbackCandidateUnits = new UnitList();
    //0x0AA4
    public final UnitList preferredFriendlyCandidates = new UnitList();
    //0x0AC4
    public final UnitList preferredHostileCandidates = new UnitList();
    //0x0AE4
    public final int[][] missionTargetScoreMatrix =
            new int[MISSION_TARGET_SCORE_MATRIX_DIMENSION][MISSION_TARGET_SCORE_MATRIX_DIMENSION];
    //0xA724
    public int relativeFormationDistanceLimit;
    //0xA728
    public final byte[] serializedStateBlob = new byte[0x190];
    //0xA8B8
    public int minimalGuardRange;
    //0xA8BC
    public final MissionDiplomacyState missionDiplomacyState = new MissionDiplomacyState();
    //0xBBE8
    public int forceAllMissionGroupsUpdate;
    //0xBBEC
    public final CustomList<EventDTO> pendingMissionUnitEventDTOs = CustomList.std(EventDTO.class);
    //0xBC08
    public final byte[] runtimeStateBlob = new byte[0x190];
    //0xBD98
    public final EventDTO scratchMissionUnitEventDTO = new EventDTO();

    /**
     * Native support extracted from MissionScriptRuntime wrapper ctor stage @005686FA.
     * Fully ported.
     */
    protected void initializeBaseWrapper() {
        initializeBaseLayer();
    }

    /**
     * Native support extracted from MissionScriptRuntime wrapper ctor stage @00568645.
     * Fully ported.
     */
    protected void initializeBaseLayer() {
        initializeCoreLayer();
    }

    /**
     * Native support extracted from MissionRuntimeBase ctor stage @005680BA.
     * Fully ported.
     */
    protected void initializeCoreLayer() {
        initializeBaseState();
        lastTurnElapsedMsLowDword = 0;
        lastTurnElapsedMsHighDword = 0;
        serializedStateByte0 = 0;
        serializedStateByte1 = 0;
        missionTurnCounter = 0;
    }

    /**
     * Native: MissionRuntimeBase::MissionRuntimeBase @0056811B.
     * Fully ported.
     */
    protected void initializeBaseState() {
        mRandomRelated.initializeLimits();

        turnPerfMonitor.initialize();
        activatingPerfMonitor.initialize();
        scriptPerfMonitor.initialize();
        aiPerfMonitor.initialize();

        privateProfileReader.initialize();
        primaryCandidateUnits.clear();
        fallbackCandidateUnits.clear();
        preferredFriendlyCandidates.clear();
        preferredHostileCandidates.clear();
        pendingMissionUnitEventDTOs.clear();
        scratchMissionUnitEventDTO.initialize();

        lastTurnElapsedMsLowDword = 0;
        lastTurnElapsedMsHighDword = 0;
        serializedStateByte0 = 0;
        serializedStateByte1 = 0;
        missionTurnCounter = 0;
        relativeFormationDistanceLimit = 2;
        forceAllMissionGroupsUpdate = 0;
        turnTimingStats.clear();

        targetRelationModeTable = new byte[][]{{2, 1, 2, 4}, {4, 2, 1, 0}, {2, 1, 4, 0}, {2, 1, 2, 4}};
        minimalGuardRange = loadMinimalGuardRangeFromAiReg();
    }

    /**
     * Native support extracted from MissionRuntimeBase::MissionRuntimeBase @0056811B ai.reg load path.
     * Fully ported.
     */
    private int loadMinimalGuardRangeFromAiReg() {
        try {
            ResInHeap aiReg = ResInHeap.load(AI_REG_PATH);
            StringBuilder value = new StringBuilder();
            aiReg.getValueAsString(
                    AI_REG_SECTION_SCANNING,
                    AI_REG_KEY_MINIMAL_GUARD_RANGE,
                    Integer.toString(MINIMAL_GUARD_RANGE_DEFAULT),
                    value,
                    64
            );
            String text = value.toString().trim();
            if (text.isEmpty()) {
                return MINIMAL_GUARD_RANGE_DEFAULT;
            }
            return Integer.parseInt(text);
        } catch (Exception ignored) {
            return MINIMAL_GUARD_RANGE_DEFAULT;
        }
    }
}
