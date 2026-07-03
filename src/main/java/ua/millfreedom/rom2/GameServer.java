package ua.millfreedom.rom2;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.CFile.CFile;
import ua.millfreedom.rom2.dserver.DedicatedServerConsoleSink;
import ua.millfreedom.rom2.gameserver.GameServerDebugState;
import ua.millfreedom.rom2.gameserver.Cheats;
import ua.millfreedom.rom2.gameserver.MissionScriptRuntime;
import ua.millfreedom.rom2.gameserver.ScenarioMapLoader;
import ua.millfreedom.rom2.model.*;
import ua.millfreedom.rom2.model.action.*;
import ua.millfreedom.rom2.model.actiondata.ActionPayloads;
import ua.millfreedom.rom2.model.enums.*;
import ua.millfreedom.rom2.model.net.CBufferManager;
import ua.millfreedom.rom2.model.net.CLlDriver;
import ua.millfreedom.rom2.model.spell.*;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.unit.UnitDirtyFlags;
import ua.millfreedom.rom2.model.unit.UnitInfo;
import ua.millfreedom.rom2.model.unit.humanoid.Humanoid;
import ua.millfreedom.rom2.model.unit.humanoid.human.Human;
import ua.millfreedom.rom2.model.unit.humanoid.human.HumanInfo;
import ua.millfreedom.rom2.model.visobj.StatsAllocationPanelVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.model.world.CWorldMap;
import ua.millfreedom.rom2.model.world.ScenarioDescriptor;
import ua.millfreedom.rom2.res.Resources;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static ua.millfreedom.rom2.gameserver.missionruntime.MissionDiplomacyState.SELF_RELATION_FLAGS;
import static ua.millfreedom.rom2.res.Constants.SCENARIO;

/**
 * Partial Java port of GameServer save load/serialize routines.
 * Native singleton: g_GameServer @006044c4, represented by Globals.gameServer.
 */
public class GameServer implements MfcSerializable {

    private static final int SAVE_MAGIC = 0x26677342;
    private static final int MIN_SUPPORTED_SAVE_VERSION = 0x0BAD0002;
    private static final int SAVE_END_MARKER = 0xBADFACE1;
    private static final int SAVE_TITLE_BLOCK_SIZE = 0x100;
    private static final String SERVER_MULTIPLAYER_SAVE_TITLE = "Server Multiplayer save file.";
    private static final int PACKED_STREAM_MAX_RUN_UNITS = 0x7F;
    private static final int BITS_MAP_SIZE_BYTES = 0xC00; // g_bitsMap memset size in __ioinit0
    private static final int CHEAT_COMMAND_FLAG_ENABLED = 0xFF;
    private static final int CHEAT_COMMAND_FLAG_DISABLED = 0;
    private static final int COMMAND_FAILED_EVENT = 6;
    private static final DateTimeFormatter SERVER_LOG_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yy HH:mm:ss ");
    private static final int COMMAND_SUCCEEDED_EVENT = 7;
    private static final int PLAYER_JOINED_EVENT = 3;
    private static final int PLAYER_RETURNED_EVENT = 4;
    private static final int JOIN_REJECT_NAME_TOO_SHORT = 1;
    private static final int JOIN_REJECT_RESTRICTED_NAME = 2;
    private static final int JOIN_REJECT_DUPLICATE_NAME = 3;
    private static final int JOIN_REJECT_TOO_FEW_CDS = 6;
    private static final int JOIN_REJECT_DEMO_RELEASE_MISMATCH = 8;
    private static final int JOIN_REJECT_SINGLE_CHARACTER_LOCK = 0x0C;
    private static final int JOIN_REJECT_TOO_STRONG = 0x0E;
    private static final int JOIN_REJECT_TOO_WEAK = 0x0F;
    private static final int MIN_LATENCY_MILLIS = 0x32;
    private static final int MAX_LATENCY_MILLIS = 10_000;
    private static final int PLAYER_RELATION_HOSTILE_MASK = 1;
    private static final int MAP_VISIBILITY_HIDDEN = 0;
    private static final int MAP_VISIBILITY_SHOWN = 1;
    private static final int MAP_VICTORY = 2;
    private static final int ALL_UNIT_UPDATE_FLAGS = -1;
    private static final int UNIT_EQUIPMENT_BROADCAST_MASK = 0x0FFB;
    private static final int LOAD_MAP_ERR_NO_ALM_EXTENSION = 3;
    private static final int LOAD_MAP_ERR_SAVE_FILE = 4;
    private static final int LOAD_MAP_ERR_SCENARIO = 5;
    private static final int[] JOIN_REQUIRED_MEDIA_COUNTS = {
            1, 1, 1, 2,
            2, 2, 3, 3,
            3, 3, 4, 4,
            4, 4, 4, 4
    };
    private static final String MAP_EXTENSION = ".alm";
    private static final String DEFAULT_SAVE_RESTORE_FILE = "game0000.sav";
    private static final int SERVER_GRID_SIZE = 0x20;
    private static final int SCENARIO_OBJECT_ID_BASE = 0x6000;
    private static final int SESSION_TYPE_MAGE = 0x40;
    private static final int SESSION_TYPE_FEMALE = 0x80;
    private static final int SESSION_TYPE_CLASS_AND_SEX_MASK = SESSION_TYPE_MAGE | SESSION_TYPE_FEMALE;
    private static final int STARTING_SKILL_LEVEL = 0x14;
    private static final int LOCAL_HERO_START_X = 8;
    private static final int LOCAL_HERO_START_Y = 0x0C;
    public static final int LOCAL_CAMPAIGN_HERO_SERVER_ID = 0x15;
    private static final int INN_ENTRY_PLAYER_SERVER_ID_FIRST = 0x15;
    private static final int INN_ENTRY_PLAYER_SERVER_ID_LIMIT = 0x1F;
    private static final int SCENARIO_INN_ENTRY_TEMPLATE_BASE = 10_000;
    private static final int SCENARIO_INN_ENTRY_CHAPTER_VARIANT_BASE = 0x270E;
    private static final int SCENARIO_INN_ENTRY_VARIANT_LIMIT = 10;
    private static final int MISSION_ENTRY_RELATION_FIRST_COPY_ID = 2;
    private static final int MISSION_ENTRY_RELATION_COPY_LIMIT = 0x10;
    private static final int MISSION_ENTRY_FALLBACK_CELL_MIN = 0x1E;
    private static final int MISSION_ENTRY_FALLBACK_CELL_RANDOM_MAX = 0x46;
    private static final int MISSION_ENTRY_MIN_PLACEMENT_DIAMETER = 5;
    private static final int MISSION_ENTRY_PLACEMENT_DIAMETER_PADDING = 4;
    private static final int MISSION_RESULT_COMPLETE = 1;
    private static final int MISSION_RESULT_FAILED = 2;
    private static final int LOCAL_PLAYER_STARTING_GOLD = 1000;
    private static final int MAP_CHUNK_TRANSFER_SIZE = 0x1000;
    private static final int CONTROLLED_HUMANOID_SAVE_INTERVAL_MS = 15_000;
    private static final int SERVER_SAVED_CHARACTER_FILE_MAX_BYTES = 0x10000;
    private static final int SERVER_SAVED_CHARACTER_REJECTION_STATUS = 0x0B;
    private static final int UNIT_STATUS_INACTIVE = 0x08;
    private static final int DIPLOMACY_CLIENT_MUTABLE_MASK = 0x17;
    private static final int SERVER_STATUS_REPORT_INTERVAL_MS = 300_000;
    private static final int HUMANOID_PUBLIC_TYPE_FIRST = 0x21;
    private static final int HUMANOID_PUBLIC_TYPE_LAST = 0x3F;
    private static final int BATTLE_PREFERENCE_WIMPY_MODE = 1;
    private static final int BATTLE_PREFERENCE_FORMATION_MODE = 2;
    private static final int BATTLE_PREFERENCE_AUTO_CASTING_MODE = 3;
    private static final int BATTLE_PREFERENCE_UNIT_AUTOCAST_SPELL = 4;
    private static final int BATTLE_PREFERENCE_ALT_DEBUG_COMMAND = 0x80;
    private static final int COMMAND_SPAWN_DIAMETER = 3;
    private static final int COMMAND_MAGIC_ITEM_SLOT = 0x0E;
    private static final int INVENTORY_CONTAINER_EQUIPMENT = 1;
    private static final int INVENTORY_CONTAINER_UNIT_INVENTORY = 2;
    private static final int INVENTORY_CONTAINER_GROUND_SACK = 3;
    private static final int INVENTORY_CONTAINER_SHOP_TRANSACTION = 4;
    private static final int INVENTORY_CONTAINER_SHOP_SHELF_MAX = 8;
    public static final String SELF_PLAYER_NAME = "Self";
    private static final int DIRECTPLAY_TCP_IP_PROTOCOL = ProtocolId.DPSP_TCPIP;
    private static final int RUNTIME_SPELL_POINTER_COUNT = 0x1E;
    private static final int[] UNIT_ORDER_SPELL_SLOT_TO_SPELL_ID = {
            SpellId.EMPTY.id,
            SpellId.FIRE_ARROW.id,
            SpellId.FIRE_BALL.id,
            SpellId.WALL_OF_FIRE.id,
            SpellId.PROTECTION_FROM_FIRE.id,
            SpellId.HEAL.id,
            SpellId.BLESS.id,
            SpellId.HASTE.id,
            SpellId.DRAIN_LIFE.id,
            SpellId.PROTECTION_FROM_AIR.id,
            SpellId.INVISIBILITY.id,
            SpellId.PRISMATIC_SPRAY.id,
            SpellId.LIGHTNING.id,
            SpellId.ICE_MISSILE.id,
            SpellId.POISON_CLOUD.id,
            SpellId.BLIZZARD.id,
            SpellId.PROTECTION_FROM_WATER.id,
            SpellId.SUMMON.id,
            SpellId.CONTROL_SPIRIT.id,
            SpellId.TELEPORT.id,
            SpellId.SHIELD.id,
            SpellId.PROTECTION_FROM_EARTH.id,
            SpellId.STONE_CURSE.id,
            SpellId.WALL_OF_EARTH.id,
            SpellId.DIAMOND_DUST.id,
            SpellId.FIRE_BALL.id
    };

    // Native global: g_bitsMap (3072-byte table)
    private static final byte[] gBitsMap = new byte[BITS_MAP_SIZE_BYTES];
    // Native global: g_MapChecksum, written by GameServer::LoadMapByName @004EB715.
    private static int gMapChecksum;
    // Native global: g_Grid32x32_ServerRelated, cleared by GameServer::LoadMapByName @004EB715.
    private static final int[][] serverGrid32x32 = new int[SERVER_GRID_SIZE][SERVER_GRID_SIZE];
    // Native global: serverStatusFilePath @00627158.
    private static String serverStatusFilePath = "";
    // Native global: DWORD_006ca254, used by GameServer::runServerLoopTick @004F08C0.
    private static int lastServerStatusReportTick;
    // Java lifecycle flag for native g_GameServer allocation in CMainWindow::CreateServer @0048AB76.
    private static boolean serverLifecycleAllocated;

    //0x04
    public int serverLoopCounter;
    //0x08
    public final Cheats cheats = new Cheats();
    //0x70
    public int pendingSessionStart;
    //0x74
    public int networkSessionActive;
    //0x78
    public String serverBaseDir = "";

    //0x00
    public int someValue;
    //0x8c
    public int field16_0x8c;
    //0x90
    public String mapFileName = "";
    // Flattened ScenarioMapLoader::gameMapNumber used by ScenarioMapLoader::loadScenarioMap @005606AA.
    //0xb8
    public int scenarioMapLoaderGameMapNumber;
    //0xd4
    public int mapNumber;
    //0xd8
    public int difficultyLevelSetting;
    //0x174
    public int field174;
    //0x178
    public int field26_0x178;
    //0x17c
    public int field17C;
    //0x180
    public int field180;
    //0x184
    public int field184;
    //0x188
    public int field188;
    //0x18c
    public int field18C;
    //0x190
    public int field190;
    //0x194
    public int field194;
    //0x198
    public int field198;
    //0x19c
    public int field19C;
    //0x1a0
    public int field1A0;
    //0x1a4
    public int useGlobalCampaignShop;
    // Native runtime flag `field38_0x1A8`, toggled by DedicatedServerConsoleVisualObject::OnMessage @0044D135.
    //0x1a8
    public int keepSavedCharactersOnServer;
    //0x1ac
    public int loadSavedGameOnMapLoad;
    //0x1b0
    public int dedicatedServerConsoleActive;
    //0x1b4
    public int cpuUsageTenthPct;
    //0x1b8
    public int cpuUsageTenthPctPeak;
    //0x1bc
    public int cpuUsageTenthPctSum;
    //0x1c0
    public int cpuUsageSampleCount;
    //0x1c4
    public int cpuUsageTenthPctMax;
    //0x1c8
    public String mapName = "";
    //0x1cc
    public int difficultyLevel;
    //0x94
    public int worldLoaded;
    //0xdc
    public final Map<Object, Object> cMapPtrToPtr = new HashMap<>();
    public final PlayerList playerList = new PlayerList();
    // Global active unit waypointCells used in GameServer::Serialize extended branch.
    public final UnitList activeUnits = new UnitList();
    // Native global: g_deferredDeathUnits, allocated by GameServer::Start @004EB356 and drained by FUN_0052BBB7.
    public final UnitList deferredDeathUnits = new UnitList();
    //0x7c
    public final GameServerObjectLists objectLists = new GameServerObjectLists();
    //0xf8
    public final Spell[] runtimeSpells = new Spell[RUNTIME_SPELL_POINTER_COUNT];
    // Native temporary object from FUN_00534ad4 (0x00534ad4), then fed into CWorldMap ctor.
    public ScenarioDescriptor scenarioDescriptor;
    // Native `ScenarioMapLoader::wordArray`, populated by ScenarioMapLoader::materializeScenarioScriptRuntime
    // @00562745 from scenario instant type 0x10002.
    public final ArrayList<Integer> missionEntryDropCells = new ArrayList<>();
    // Native global g_CWorldMap.
    public CWorldMap worldMap;
    // FUN_0057468d (0x0057468d), PTR_0066832c
    public MissionScriptRuntime missionScriptRuntime;
    // Native global: Shop::global consumed by GameServer::findInteractiveShopNearTarget @004F3CAD.
    public final Shop globalCampaignShop = new Shop();
    //0x170
    public final GameServerDebugState debugState = new GameServerDebugState();

    /**
     * Native support extracted from GameServer::New @004E957B.
     * Fully ported.
     */
    GameServer() {
        initializeNewServerState();
    }

    /**
     * Native support extracted from GameServer::New @004E957B.
     * Java keeps the native singleton instance and reapplies the constructor state when CMainWindow::CreateServer
     * starts a fresh server.
     */
    public void initializeNewServerState() {
        serverBaseDir = "";
        pendingSessionStart = 0;
        networkSessionActive = 0;
        field16_0x8c = 0;
        mapFileName = "1.alm";
        worldLoaded = 0;
        cMapPtrToPtr.clear();
        playerList.removeAll();
        playerList.counter = 0;
        activeUnits.clear();
        deferredDeathUnits.clear();
        objectLists.clearOwnedObjectLists();
        missionEntryDropCells.clear();
        scenarioDescriptor = null;
        worldMap = null;
        Globals.worldMap = null;
        missionScriptRuntime = null;
        someValue = 0;
        serverLoopCounter = 0;
        field174 = 0;
        field26_0x178 = 0;
        field17C = 0;
        field180 = 0;
        field184 = 0;
        field188 = 0;
        field18C = 0;
        field190 = 0;
        field194 = 0;
        field198 = 0;
        field19C = 0;
        field1A0 = 0;
        useGlobalCampaignShop = 1;
        keepSavedCharactersOnServer = 1;
        loadSavedGameOnMapLoad = 0;
        scenarioMapLoaderGameMapNumber = 0;
        mapNumber = 0;
        difficultyLevelSetting = 2;
        dedicatedServerConsoleActive = 0;
        resetCpuUsageStats();
        mapName = "";
        difficultyLevel = -1;
        Arrays.fill(runtimeSpells, null);
        debugState.clear();
        randomizeOnTimer();
    }

    /**
     * Native support extracted from CMainWindow::CreateServer @0048AB76 native `g_GameServer` assignment.
     * Fully ported.
     */
    public void markServerLifecycleAllocated() {
        serverLifecycleAllocated = true;
    }

    /**
     * Native support extracted from `g_GameServer` null checks in CMainWindow::onDialogClosed @004891D8.
     */
    public boolean isServerLifecycleAllocated() {
        return serverLifecycleAllocated;
    }

    /**
     * Native: GameServer::SetDifficultyLevelSetting @00493D80.
     * Fully ported.
     */
    public void setDifficultyLevelSetting(int difficultyLevelSetting) {
        this.difficultyLevelSetting = difficultyLevelSetting;
    }

    /**
     * Native: GameServer::Start @004EB356.
     * Fully ported.
     */
    public int start(int startupMode) {
        ioInit0();
        serverBaseDir = "";
        networkSessionActive = startupMode < 2 ? 1 : 0;
        pendingSessionStart = startupMode > 0 ? 1 : 0;
        keepSavedCharactersOnServer = networkSessionActive == 0 ? 0 : 1;
        field16_0x8c = 0;
        CServerApp.setLocalNetworkDriver(CLlDriver.class);

        int staticDataLoadResult = loadStaticDataForServerStart();
        if (staticDataLoadResult != 0) {
            return staticDataLoadResult;
        }

        runtimeSpells[0] = null;
        for (int spellId = 1; spellId < RUNTIME_SPELL_POINTER_COUNT; spellId++) {
            runtimeSpells[spellId] = new Spell((byte) spellId);
        }
        activeUnits.clear();
        deferredDeathUnits.clear();
        objectLists.allocateCorpseListForServerStart();
        playerList.removeAll();
        playerList.counter = 0;
        pushMessage("Server started");
        clearServerGrid32x32();
        return 0;
    }

    /**
     * Native support extracted from GameServer::Start @004EB356 CStaticDataMgr::LoadOrRebuild failure branch.
     */
    private static int loadStaticDataForServerStart() {
        try {
            return Globals.staticDataMgr.loadOrRebuild();
        } catch (RuntimeException e) {
            return 2;
        }
    }

    /**
     * Native: GameServer::StartAndLoadMapByName @004EB2C3.
     * Fully ported.
     */
    public int startAndLoadMapByName(String mapName, int startupMode) {
        int result = start(startupMode);
        if (result != 0) {
            return result;
        }
        return loadMapByName(mapName);
    }

    /**
     * Native: GameServer::SaveGameFile @004E9816.
     * Fully ported.
     */
    public void saveGameFile(String saveFileName) {
        pushMessage("Saving to:" + saveFileName);
        removeEmptyUnitGroupsBeforeSave();

        try (OutputStream output = openSaveFileOutput(saveFileName)) {
            byte[] serialized = padOddSerializedSaveBytes(serializeGameServerForSaveFile());
            byte[] packed = encodePackedWideStream(serialized);
            output.write(buildSaveFileBytes(packed));
        } catch (IOException e) {
            throw new UncheckedIOException("GameServer::SaveGameFile failed while writing " + saveFileName, e);
        }
    }

    /**
     * Fully ported native support extracted from GameServer::SaveGameFile @004E9816 active-player unit-group cleanup
     * block and UnitGroupList::RemoveAndDestroy @00539D50.
     */
    private void removeEmptyUnitGroupsBeforeSave() {
        for (Player player : playerList.players) {
            if (player.isActive == 0) {
                Iterator<UnitGroup> unitGroups = player.unitGroups.iterator();
                while (unitGroups.hasNext()) {
                    UnitGroup unitGroup = unitGroups.next();
                    if (unitGroup.units.isEmpty()) {
                        unitGroups.remove();
                    }
                }
            }
        }
    }

    /**
     * Native support extracted from GameServer::SaveGameFile @004E9816 CFile::Open call.
     */
    private static OutputStream openSaveFileOutput(String saveFileName) throws IOException {
        return Files.newOutputStream(
                SavedGameFiles.resolvePath(saveFileName),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );
    }

    /**
     * Native support extracted from GameServer::SaveGameFile @004E9816 CMemFile + CArchive storing block.
     */
    private byte[] serializeGameServerForSaveFile() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(0x400);
        try (CArchive archive = CArchive.forWritingToBytes(output, ByteOrder.LITTLE_ENDIAN)) {
            serialize(archive);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Failed to close GameServer save archive", e);
        }
        return output.toByteArray();
    }

    /**
     * Native support extracted from GameServer::SaveGameFile @004E9816 odd CMemFile length padding branch.
     */
    private static byte[] padOddSerializedSaveBytes(byte[] serialized) {
        if ((serialized.length & 1) == 0) {
            return serialized;
        }
        byte[] padded = Arrays.copyOf(serialized, serialized.length + 1);
        padded[serialized.length] = (byte) padded.length;
        return padded;
    }

    /**
     * Native support extracted from GameServer::SaveGameFile @004E9816 save-header and title-offset writes.
     */
    private byte[] buildSaveFileBytes(byte[] packed) {
        ByteArrayOutputStream output = new ByteArrayOutputStream(0x10 + packed.length + SAVE_TITLE_BLOCK_SIZE);
        writeLittleEndianInt(output, SAVE_MAGIC);
        writeLittleEndianInt(output, 0);
        writeLittleEndianInt(output, MIN_SUPPORTED_SAVE_VERSION);
        writeLittleEndianInt(output, packed.length);
        output.writeBytes(packed);

        int titleOffset = output.size();
        if (networkSessionActive != 0) {
            byte[] titleBlock = new byte[SAVE_TITLE_BLOCK_SIZE];
            byte[] title = SERVER_MULTIPLAYER_SAVE_TITLE.getBytes(StandardCharsets.ISO_8859_1);
            System.arraycopy(title, 0, titleBlock, 0, title.length);
            output.writeBytes(titleBlock);
        }

        byte[] saveFileBytes = output.toByteArray();
        ByteBuffer.wrap(saveFileBytes).order(ByteOrder.LITTLE_ENDIAN).putInt(Integer.BYTES, titleOffset);
        return saveFileBytes;
    }

    /**
     * Native: GameServer::LoadSaveFile @004E9B95.
     * Fully ported.
     *
     * @return 0 on success, 1 on failure.
     */
    public int loadSaveFile(String saveFileName) {
        pushMessage("Loading from:" + saveFileName);

        CFile file;
        try {
            file = new CFile(SavedGameFiles.resolvePath(saveFileName));
        } catch (IOException e) {
            return 1;
        }

        final byte[] packed;
        try (file) {
            file.setByteOrder(ByteOrder.LITTLE_ENDIAN);

            if (file.readInt() != SAVE_MAGIC) {
                pushMessage("Invalid save file.");
                return 1;
            }

            file.readInt();

            int saveVersion = file.readInt();
            if (saveVersion < MIN_SUPPORTED_SAVE_VERSION) {
                pushMessage("Outdated save file.");
                return 1;
            }

            int packedSize = file.readInt();
            ByteBuffer packedBuffer = file.readBytes(packedSize);
            packed = new byte[packedSize];
            packedBuffer.get(packed);
        } catch (Exception e) {
            pushMessage("Invalid save file.");
            return 1;
        }

        DecodedSave decoded;
        try {
            decoded = decodePackedWideStream(packed);
            //Files.write(Path.of("tmp.txt"),decoded.utf16LeBytes);
        } catch (IOException e) {
            pushMessage("Invalid save file.");
            return 1;
        }

        try (CArchive ar = CArchive.forReadingFromBytes(decoded.utf16LeBytes, ByteOrder.LITTLE_ENDIAN)) {
            serialize(ar); // GameServer::Serialize (0x004ec732).
        } catch (Exception e) {
            pushMessage("Invalid save file.");
            e.printStackTrace();
            return 1;
        }

        return 0;
    }

    /**
     * Native: GameServer::LoadMapByName @004EB715.
     * Fully ported.
     */
    public int loadMapByName(String mapName) {
        field16_0x8c = 0;
        mapFileName = mapName;
        randomizeOnTimer();

        if (mapNumber == 0) {
            int parsedMapNumber = parseMapNumberFromAlmName(mapFileName);
            if (parsedMapNumber < 0) {
                return LOAD_MAP_ERR_NO_ALM_EXTENSION;
            }
            if (parsedMapNumber > 0) {
                mapNumber = parsedMapNumber;
            }
        }

        scenarioMapLoaderGameMapNumber = mapNumber;
        resetMapObjectContainers();
        if (loadSavedGameOnMapLoad == 0) {
            if (ScenarioMapLoader.loadScenarioMap(mapFileName) != 0) {
                return LOAD_MAP_ERR_SCENARIO;
            }
            someValue = 0;
            serverLoopCounter = 0;
        } else if (loadSaveFile(DEFAULT_SAVE_RESTORE_FILE) != 0) {
            return LOAD_MAP_ERR_SAVE_FILE;
        }

        if (networkSessionActive != 0) {
            gMapChecksum = calculateChecksum(mapFileName);
        }
        worldLoaded = 1;
        CServerApp.sendNoPayloadAction(GameActionId.MISSION_STARTED_ACTION_B7, null);
        resetCpuUsageStats();
        reportServerStatusToConfiguredTargets();
        cleanupStaleCharacterLocks();
        pushMessage("Loaded map: " + mapName + ".");
        clearServerGrid32x32();
        Globals.questStorage.removeAndDeleteQuestsForOwner(0);
        return 0;
    }

    /**
     * Native: GameServer::returnToLobby @004EBD5F.
     * Fully ported.
     */
    public void returnToLobby() {
        reportNoMapServerStatusAndDeleteStatusFile();
        field16_0x8c = 1;
        prepareInactiveControlledUnitsForReturnToLobby();
        objectLists.clearLoadedWorldObjectLists();
        missionScriptRuntime = null;
        worldMap = null;
        Globals.worldMap = null;
        activeUnits.clear();
        deferredDeathUnits.clear();
        clearInactivePlayerUnitEffectsForReturnToLobby();
        resetPlayersForReturnToLobby();
        cMapPtrToPtr.clear();
        missionEntryDropCells.clear();
        scenarioDescriptor = null;
        mapNumber = 0;
        worldLoaded = 0;
        someValue = 0;
        serverLoopCounter = 0;
        CServerApp.sendNoPayloadAction(GameActionId.RETURN_TO_LOBBY_ACTION_B8, null);
        CServerApp.broadcastServerLoopCounter(serverLoopCounter + 1);
    }

    /**
     * Fully ported lifecycle cleanup extracted from GameServer::~GameServer @004EC3BE and
     * CMainWindow::DestroyServer @0048AC34. Java keeps the GameServer singleton instance, so native object deletion,
     * string destruction, and server-thread handle waits are represented as explicit state cleanup where applicable.
     */
    public void destroyServerLifecycle() {
        if (!serverLifecycleAllocated) {
            return;
        }
        serverLifecycleAllocated = false;
        CServerApp.sendNoPayloadAction(GameActionId.SERVER_CLOSED_ACTION_AF, null);
        CServerApp.broadcastServerLoopCounter(serverLoopCounter + 1);
        CServerApp.flushActiveClientWriteBuffers();
        missionScriptRuntime = null;
        worldMap = null;
        Globals.worldMap = null;
        objectLists.clearOwnedObjectLists();
        playerList.removeAll();
        activeUnits.clear();
        deferredDeathUnits.clear();
        Arrays.fill(runtimeSpells, null);
        debugState.clear();
        pushMessage("Server closed.");
    }

    /**
     * Fully ported native support extracted from GameServer::returnToLobby @004EBD5F inactive controlled-unit
     * pre-cleanup loop.
     */
    private void prepareInactiveControlledUnitsForReturnToLobby() {
        for (Player player : playerList.players) {
            if (player.isActive == 0 && player.controlledUnit instanceof Unit controlledUnit) {
                closeReturnToLobbyShopSession(controlledUnit);
                Inn inn = findInteractiveInnNearTarget(controlledUnit.m_pTargetHandle);
                if (inn != null) {
                    inn.closeUnitSession(controlledUnit, 0xFFFFFFFF);
                }
                if (controlledUnit.respawning != 0 && networkSessionActive != 0) {
                    reenterMissionAfterReturnToLobby(player, controlledUnit);
                }
            }
        }
    }

    /**
     * Fully ported native support extracted from GameServer::returnToLobby @004EBD5F controlled-unit shop session
     * branch.
     */
    private void closeReturnToLobbyShopSession(Unit controlledUnit) {
        Shop shop = findInteractiveShopNearTarget(controlledUnit.m_pTargetHandle);
        if (shop != null
                && shop.multiShopTemplate != null
                && shop.multiShopTemplate.findTrackedInstanceByToken(controlledUnit) != null) {
            shop.multiShopTemplate.removeTrackedToken(controlledUnit);
            if (networkSessionActive != 0) {
                controlledUnit.returnToMissionMap();
            }
        }
    }

    /**
     * Fully ported native support extracted from GameServer::returnToLobby @004EBD5F network respawn return branch.
     */
    private void reenterMissionAfterReturnToLobby(Player player, Unit controlledUnit) {
        player.returnAfterDeathPending = 0;
        if (controlledUnit.inventory == null) {
            controlledUnit.inventory = new Inventory();
        }
        objectLists.corpses.remove(controlledUnit);
        controlledUnit.word = 0;
        restoreExistingControlledUnitForPlayer(player);
        placeMissionEntryUnits(player);
        player.missionResultState = 0;
        restoreMissionReentryRelationsFromSelf(player);
    }

    /**
     * Fully ported native support extracted from GameServer::returnToLobby @004EBD5F inactive-player effect cleanup
     * block.
     */
    private void clearInactivePlayerUnitEffectsForReturnToLobby() {
        for (Player player : playerList.players) {
            if (player.isActive == 0) {
                for (Unit unit : player.ownedUnits) {
                    clearUnitEffectsForReturnToLobby(unit);
                }
            }
        }
    }

    /**
     * Fully ported native support extracted from GameServer::returnToLobby @004EBD5F player reset/destroy branch.
     */
    private void resetPlayersForReturnToLobby() {
        for (Player player : new ArrayList<>(playerList.players)) {
            if (player.isActive == 0 && !player.name.equals(SELF_PLAYER_NAME)) {
                player.missionEntryStateSent = 0;
                for (Unit unit : new ArrayList<>(player.ownedUnits)) {
                    if (isReturnToLobbyRestoredScenarioUnit(unit)) {
                        resetScenarioUnitForReturnToLobby(unit);
                    } else {
                        player.detachDeadUnitFromOwner(unit);
                    }
                }
            } else {
                playerList.destroy(player);
            }
        }
        if (networkSessionActive == 0) {
            playerList.counter = 2;
        }
    }

    /**
     * Fully ported native support extracted from GameServer::returnToLobby @004EBD5F Token.typeID `0x21..0x3F`
     * branch.
     */
    private static boolean isReturnToLobbyRestoredScenarioUnit(Unit unit) {
        int typeId = unit.getTokenTypeId() & 0xFFFF;
        return typeId >= HUMANOID_PUBLIC_TYPE_FIRST && typeId <= HUMANOID_PUBLIC_TYPE_LAST;
    }

    /**
     * Fully ported native support extracted from GameServer::returnToLobby @004EBD5F restored owned-unit state block.
     */
    private static void resetScenarioUnitForReturnToLobby(Unit unit) {
        unit.m_nHP = unit.m_nMaxHP;
        unit.m_nMP = unit.m_nMaxMP;
        unit.respawning = 0;
        unit.resetActionStateToDying();
        unit.actionTarget = null;
        unit.spell = null;
        unit.secondarySpell = null;
        unit.pItem = null;
        unit.lastDamageSource = null;
    }

    /**
     * Fully ported native support extracted from GameServer::returnToLobby @004EBD5F inactive-player effect cleanup
     * block.
     */
    private static void clearUnitEffectsForReturnToLobby(Unit unit) {
        for (int i = 0; i < unit.effects.size(); ) {
            Effect effect = unit.effects.get(i);
            effect.mValue.setS2(1);
            effect.sourceUnit = null;
            effect.updateOnTick(unit);
            unit.effects.remove(i);
        }
    }

    /**
     * Native: GameServer::ensurePlayerForJoinName @004F23D9.
     * Fully ported.
     */
    public Player ensurePlayerForJoinName(String playerName) {
        Player player = null;
        boolean existingPlayer = false;
        if (networkSessionActive == 0) {
            player = playerList.getByName(SELF_PLAYER_NAME);
            existingPlayer = player != null;
        }
        if (player == null) {
            player = new Player();
        }

        player.name = playerName;
        player.isActive = 0;
        if (player.gold == 0) {
            player.gold = LOCAL_PLAYER_STARTING_GOLD;
        }
        if (!existingPlayer) {
            playerList.addAssigningIdAndScanMask(player);
        }
        return player;
    }

    /**
     * Native: GameServer::handlePlayerJoinRequest @004F0CBE.
     * Java receives the decoded GameServer::handleServerGameAction @004F515D case `0x02` packet wrapper.
     * Fully ported.
     */
    private void handlePlayerJoinRequest(PlayerJoinAction action) {
        String playerName = action.playerName.get();
        CBufferManager sourceClient = action.getSourceClient();
        if (playerName.length() < 1) {
            pushMessage("Illegal player " + playerName + " ID:" + action.sessionKeyPart1.get() + ". Client rejected.");
            rejectClientJoin(sourceClient, JOIN_REJECT_NAME_TOO_SHORT, "Your name too short. At least 1 letter required.");
            return;
        }

        if (SELF_PLAYER_NAME.equals(playerName) && networkSessionActive == 0) {
            playerName = playerList.getFirst().name;
        }

        int joinOptionsPacked = action.joinOptionsPacked.get();
        int mediaOption = joinOptionsPacked & 0xFF;
        Player player;
        String bannedName = joinNameBeforeSessionSuffix(playerName);
        if (isBannedPlayerName(bannedName)) {
            pushMessage("Player " + playerName + " rejected (banned name)");
            rejectClientJoin(sourceClient, JOIN_REJECT_RESTRICTED_NAME, "Restricted name. Try different.");
            return;
        }
        if (CLlDriver.getProtocolId() == ProtocolId.TCP_IP && isBannedIpAddress(sourceClient.getAddressText())) {
            pushMessage("Player " + playerName + " rejected (banned IP address)");
            rejectClientJoin(sourceClient, JOIN_REJECT_RESTRICTED_NAME, "Restricted name. Try different.");
            return;
        }
        if (!isJoinMediaOptionCompatible(mediaOption)) {
            rejectClientJoin(sourceClient, JOIN_REJECT_DEMO_RELEASE_MISMATCH, "Demo & Release are incompatible");
            return;
        }

        String clientLoginName = sourceClient.getActiveLoginName();
        Player existingPlayer = null;
        for (Player candidate : playerList.players) {
            if (candidate.name.equals(playerName)) {
                if (networkSessionActive != 0
                        && (candidate.characterSessionKeyPart1 != action.sessionKeyPart1.get()
                        || candidate.characterSessionKeyPart2 != action.sessionKeyPart2.get()
                        || CServerApp.getLocalClientByNetId(candidate.playerId) != null)) {
                    pushMessage("Warning - other player with same name joined");
                    rejectClientJoin(
                            sourceClient,
                            JOIN_REJECT_DUPLICATE_NAME,
                            "This name already used. Try different."
                    );
                    return;
                }
                existingPlayer = candidate;
                break;
            }
            if (clientLoginName != null && candidate.characterLockName.equals(clientLoginName)) {
                rejectClientJoin(sourceClient, JOIN_REJECT_SINGLE_CHARACTER_LOCK, "Only one character allowed.");
                return;
            }
        }

        boolean newPlayer = existingPlayer == null;
        if (newPlayer) {
            if (rejectJoinForDifficultyBounds(sourceClient, playerName, joinOptionsPacked)) {
                return;
            }
            if (mediaOption == 0 && hasTooFewMediaCopiesForJoin()) {
                rejectClientJoin(sourceClient, JOIN_REJECT_TOO_FEW_CDS, "too few CDs");
                return;
            }
            player = ensurePlayerForJoinName(playerName);
            writeJoinSessionKeys(player, action.sessionKeyPart1.get(), action.sessionKeyPart2.get());
            player.joinOptions = mediaOption;
        } else {
            if (CLlDriver.getProtocolId() != ProtocolId.TCP_IP
                    && rejectJoinForDifficultyBounds(sourceClient, playerName, joinOptionsPacked)) {
                return;
            }
            player = existingPlayer;
            pushMessage("Player " + player.name + " returns to game.");
            player.pendingRemovalServerTick = 0;
        }
        sourceClient.SetNetId(player.playerId);
        player.clientConnected = 1;

        int colorSlot = (joinOptionsPacked >>> 8) & 0xFF;
        if (colorSlot == 0 && newPlayer) {
            colorSlot = firstFreeInactivePlayerColorSlot();
        }
        if (colorSlot != 0) {
            player.colorSlot = colorSlot;
        }

        CServerApp.sendGameAction(NewPlayerAction.prepareForPlayerJoinBroadcast(player, newPlayer));
        pushMessage("Player " + playerName + " joined.");
        CServerApp.sendGameEventNotification(
                newPlayer ? PLAYER_JOINED_EVENT : PLAYER_RETURNED_EVENT,
                (short) player.playerId,
                null
        );
        player.adjustGoldAndNotify(0, 1);
        CServerApp.sendLobbyPlayerInfoSnapshot(player);
        if (worldLoaded != 0) {
            CServerApp.sendNoPayloadAction(GameActionId.MISSION_STARTED_ACTION_B7, player);
        }
        CServerApp.sendCurrentServerLoopCounter(null);
        if (pendingSessionStart != 0) {
            CServerApp.flushActiveClientWriteBuffers();
        }
        if (clientLoginName != null) {
            player.characterLockName = clientLoginName;
        }
    }

    /**
     * Native support extracted from GameServer::handlePlayerJoinRequest @004F0CBE banned-name match setup.
     */
    private static String joinNameBeforeSessionSuffix(String playerName) {
        int separator = playerName.indexOf('|');
        return separator == -1 ? playerName : playerName.substring(0, separator);
    }

    /**
     * Native support extracted from GameServer::handlePlayerJoinRequest @004F0CBE banned-name loop.
     */
    private static boolean isBannedPlayerName(String playerName) {
        for (String bannedPlayer : Globals.serverConfig.bannedplayers) {
            if (playerName.equals(bannedPlayer)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Native support extracted from GameServer::handlePlayerJoinRequest @004F0CBE banned-IP loop and
     * Global::matchAddressText @004EFF90.
     */
    private static boolean isBannedIpAddress(String addressText) {
        for (String bannedIpPattern : Globals.serverConfig.bannedips) {
            if (matchAddressText(addressText, bannedIpPattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Native: Global::matchAddressText @004EFF90.
     * Fully ported.
     */
    private static boolean matchAddressText(String addressText, String pattern) {
        if (!pattern.contains("*")) {
            return addressText.equals(pattern);
        }
        String remainingAddress = addressText;
        String remainingPattern = pattern;
        for (int octet = 0; octet < 4; octet++) {
            String patternOctet = firstAddressPart(remainingPattern);
            remainingPattern = afterFirstAddressPart(remainingPattern);
            String addressOctet = firstAddressPart(remainingAddress);
            remainingAddress = afterFirstAddressPart(remainingAddress);
            if (!patternOctet.equals("*") && !patternOctet.equals(addressOctet)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Native support extracted from Global::matchAddressText @004EFF90.
     */
    private static String firstAddressPart(String value) {
        int separator = value.indexOf('.');
        return separator == -1 ? value : value.substring(0, separator);
    }

    /**
     * Native support extracted from Global::matchAddressText @004EFF90.
     */
    private static String afterFirstAddressPart(String value) {
        int separator = value.indexOf('.');
        return separator == -1 ? "" : value.substring(separator + 1);
    }

    /**
     * Native support extracted from GameServer::handlePlayerJoinRequest @004F0CBE demo/release compatibility branch.
     */
    private static boolean isJoinMediaOptionCompatible(int mediaOption) {
        return mediaOption == 0 || mediaOption == 1;
    }

    /**
     * Native support extracted from GameServer::handlePlayerJoinRequest @004F0CBE difficulty rejection branches.
     */
    private boolean rejectJoinForDifficultyBounds(CBufferManager sourceClient, String playerName, int joinOptionsPacked) {
        if (difficultyLevel == -1) {
            return false;
        }
        int minimumDifficulty = (joinOptionsPacked >>> 16) & 0xFF;
        if (difficultyLevel < minimumDifficulty) {
            rejectClientJoin(sourceClient, JOIN_REJECT_TOO_STRONG, "Character is too strong for this map.");
            pushMessage("Player " + playerName + " rejected (too strong for this map)");
            return true;
        }
        int maximumDifficulty = (joinOptionsPacked >>> 24) & 0xFF;
        if (maximumDifficulty < difficultyLevel) {
            rejectClientJoin(sourceClient, JOIN_REJECT_TOO_WEAK, "Character is too weak for this map.");
            pushMessage("Player " + playerName + " rejected (too weak for this map)");
            return true;
        }
        return false;
    }

    /**
     * Native support extracted from GameServer::handlePlayerJoinRequest @004F0CBE too-few-CDs branch.
     */
    private boolean hasTooFewMediaCopiesForJoin() {
        int mediaCount = playerList.countPlayersWithJoinOptions();
        if (pendingSessionStart == 0) {
            mediaCount++;
        }
        int playerCount = playerList.getPlayersCount();
        if (playerCount > 0x0F) {
            playerCount = 0x0F;
        }
        return mediaCount < JOIN_REQUIRED_MEDIA_COUNTS[playerCount];
    }

    /**
     * Native support extracted from GameServer::handlePlayerJoinRequest @004F0CBE writes to Player +0x10/+0x14.
     */
    private static void writeJoinSessionKeys(Player player, int sessionKeyPart1, int sessionKeyPart2) {
        player.characterSessionKeyPart1 = sessionKeyPart1;
        player.characterSessionKeyPart2 = sessionKeyPart2;
    }

    /**
     * Native support extracted from GameServer::handlePlayerJoinRequest @004F0CBE color-slot selection loop.
     */
    private int firstFreeInactivePlayerColorSlot() {
        for (int colorSlot = 1; colorSlot < 0x10; colorSlot++) {
            if (!hasInactivePlayerWithColorSlot(colorSlot)) {
                return colorSlot;
            }
        }
        return 0x10;
    }

    /**
     * Native support extracted from GameServer::handlePlayerJoinRequest @004F0CBE color-slot selection loop.
     */
    private boolean hasInactivePlayerWithColorSlot(int colorSlot) {
        for (Player player : playerList.players) {
            if (player.colorSlot == colorSlot && player.isActive == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x48` packet unpacking for
     * GameServer::ensureControlledUnitForPlayer @004F254B.
     */
    private Unit ensureControlledUnitFromSetup(Player player, SubmitCharacterSetupAction setupAction) {
        return ensureControlledUnitForPlayer(
                player,
                setupAction.body.get(),
                setupAction.reaction.get(),
                setupAction.mind.get(),
                setupAction.spirit.get(),
                setupAction.startingSkillIndex.get(),
                setupAction.faceAndType.get()
        );
    }

    /**
     * Native: GameServer::ensureControlledUnitForPlayer @004F254B.
     * Fully ported.
     */
    private Unit ensureControlledUnitForPlayer(
            Player player,
            int body,
            int reaction,
            int mind,
            int spirit,
            int startingSkillIndex,
            int sessionType
    ) {
        if (player.controlledUnit != null) {
            return restoreExistingControlledUnitForPlayer(player);
        }

        Human human = createControlledHumanFromSetup(body, reaction, mind, spirit, startingSkillIndex, sessionType);
        human.str = player.name;
        human.idFull = allocateNextFreeId() & 0xFFFF;
        human.owner = player;
        player.ownedUnits.add(human);

        UnitGroup unitGroup = new UnitGroup();
        player.unitGroups.add(unitGroup);
        unitGroup.addUnit(human);

        if (worldMap != null) {
            human.m_pTargetHandle.initFromBytes(LOCAL_HERO_START_X, LOCAL_HERO_START_Y, worldMap);
        }
        player.controlledUnit = human;
        if (networkSessionActive == 0) {
            human.serverID = LOCAL_CAMPAIGN_HERO_SERVER_ID;
        }
        human.visiblePlayerMask = player.scanMask;
        CServerApp.netUpdate(
                human,
                player,
                ALL_UNIT_UPDATE_FLAGS,
                UNIT_EQUIPMENT_BROADCAST_MASK,
                0,
                0
        );
        return human;
    }

    /**
     * Native: GameServer::rejectClientJoin @004F0B71.
     * Fully ported.
     */
    private void rejectClientJoin(CBufferManager client, int statusCode, String message) {
        CServerApp.rejectClientJoin(client, statusCode, message);
    }

    /**
     * Native support extracted from GameServer::ensureControlledUnitForPlayer @004F254B existing-controlled-unit branch.
     */
    private Unit restoreExistingControlledUnitForPlayer(Player player) {
        Humanoid controlledHumanoid = (Humanoid) player.controlledUnit;
        if (controlledHumanoid.respawning != 0) {
            controlledHumanoid.prepareMissionReentryRespawn();
            controlledHumanoid.m_nHP = controlledHumanoid.m_nMaxHP;
            controlledHumanoid.m_nMP = controlledHumanoid.m_nMaxMP;
            player.ownedUnits.add(controlledHumanoid);
            if (controlledHumanoid.unitGroup == null) {
                UnitGroup unitGroup = new UnitGroup();
                player.unitGroups.add(unitGroup);
                unitGroup.addUnit(controlledHumanoid);
            }
        }
        return controlledHumanoid;
    }

    /**
     * Native support extracted from GameServer::ensureControlledUnitForPlayer @004F254B Human construction and
     * starting-stat setup.
     */
    private static Human createControlledHumanFromSetup(
            int body,
            int reaction,
            int mind,
            int spirit,
            int startingSkillIndex,
            int sessionType
    ) {
        body &= 0xFF;
        reaction &= 0xFF;
        mind &= 0xFF;
        spirit &= 0xFF;
        startingSkillIndex &= 0xFF;
        int faceAndType = sessionType & 0xFF;
        Human human = Human.createFromTemplate(startingHumanTemplate(faceAndType), true, false);
        if (remainingStatPool(body, reaction, mind, spirit) < 0) {
            human.m_nBody = 0x19;
            human.m_nReaction = 0x19;
            human.m_nMind = 0x19;
            human.m_nSpirit = 0x19;
        } else {
            human.m_nBody = body;
            human.m_nReaction = reaction;
            human.m_nMind = mind;
            human.m_nSpirit = spirit;
        }
        human.face = faceAndType & ~SESSION_TYPE_CLASS_AND_SEX_MASK;
        human.configureStartingLoadout(startingSkillIndex, STARTING_SKILL_LEVEL);
        human.recalculateDerivedStats();
        human.m_nHP = human.m_nMaxHP;
        human.m_nMP = human.m_nMaxMP;
        return human;
    }

    /**
     * Native support extracted from GameServer::ensureControlledUnitForPlayer @004F254B stat-pool validation.
     */
    private static int remainingStatPool(int body, int reaction, int mind, int spirit) {
        return 0x8C
                - StatsAllocationPanelVisualObject.getScaledStatCost(body)
                - StatsAllocationPanelVisualObject.getScaledStatCost(reaction)
                - StatsAllocationPanelVisualObject.getScaledStatCost(mind)
                - StatsAllocationPanelVisualObject.getScaledStatCost(spirit);
    }

    /**
     * Native support extracted from GameServer::ensureControlledUnitForPlayer @004F254B Start_* template routing.
     */
    private static String startingHumanTemplate(int sessionType) {
        HumanId templateId = switch (sessionType & SESSION_TYPE_CLASS_AND_SEX_MASK) {
            case SESSION_TYPE_MAGE -> HumanId.START_MM;
            case SESSION_TYPE_FEMALE -> HumanId.START_FF;
            case SESSION_TYPE_CLASS_AND_SEX_MASK -> HumanId.START_FM;
            default -> HumanId.START_MF;
        };
        return templateId.tableName + startingHumanFaceSuffix(sessionType);
    }

    /**
     * Native support extracted from GameServer::ensureControlledUnitForPlayer @004F254B.
     */
    private static String startingHumanFaceSuffix(int sessionType) {
        int packedSessionType = sessionType & 0xFF;
        if (packedSessionType == 0) {
            return ".f5";
        }
        String facePrefix = (packedSessionType & SESSION_TYPE_FEMALE) != 0 ? ".f" : ".m";
        return facePrefix + (packedSessionType & 0x3F);
    }

    /**
     * Native: GameServer::runServerLoopTick @004F08C0.
     * Full port of the recovered native server-loop tick. Java advances the per-loop server counter, tick-phase object
     * maintenance, mission script runtime turn, local game-action drain, object-list tick, active unit ticks, periodic
     * mission-result publication, quest/status maintenance, CPU usage accounting, and the server-loop-counter broadcast
     * boundary through GameServer::advanceServerLoopCounterAndObjects.
     * Fully ported.
     */
    public void runServerLoopTick() {
        if (worldLoaded == 0) {
            return;
        }
        int tickStart = Globals.currentTickMillis();
        if (Integer.compareUnsigned(tickStart - lastServerStatusReportTick, SERVER_STATUS_REPORT_INTERVAL_MS) > 0) {
            lastServerStatusReportTick = tickStart;
            reportServerStatusToConfiguredTargets();
        }
        int preTickPhase = nativeServerLoopPhase();
        if (preTickPhase == 6) {
            missionScriptRuntime.processMissionTurn(playerList);
        }
        if (preTickPhase == 0x0C) {
            processPeriodicWorldMaintenance();
        }
        advanceServerLoopCounterAndObjects();
        if (nativeServerLoopPhase() == 0x0F) {
            updatePeriodicMissionResults();
            someValue++;
        }
        updateCpuUsageStats(tickStart);
    }

    /**
     * Native support extracted from GameServer::runServerLoopTick @004F08C0 signed `% 0x10` phase expression.
     */
    private int nativeServerLoopPhase() {
        return serverLoopCounter % 0x10;
    }

    /**
     * Native support extracted from GameServer::runServerLoopTick @004F08C0 phase `0x0C`.
     */
    private void processPeriodicWorldMaintenance() {
        activeUnits.updateRegenCorpsesAndDeferredDeaths(deferredDeathUnits);
        objectLists.updatePeriodicBuildingAndSackState();
        playerList.updatePeriodicPlayerState();
        CServerApp.sampleLocalClientTrafficStats();
        publishDirtyQuestState();
    }

    /**
     * Native support extracted from GameServer::runServerLoopTick @004F08C0 quest dirty-publication branch.
     */
    private void publishDirtyQuestState() {
        QuestsStorage questsStorage = Globals.questStorage;
        questsStorage.processPendingQuestMessages();
        for (Map.Entry<Integer, Integer> entry : new ArrayList<>(questsStorage.ownerQuestChangeFlags.entrySet())) {
            int changeFlags = entry.getValue();
            Player player = playerList.getPlayerById(entry.getKey());
            if (changeFlags != 0 && player != null) {
                CServerApp.sendQuestListAction(questsStorage, player, false);
                sendQuestChangedNotifications(player, changeFlags);
            }
        }
        questsStorage.ownerQuestChangeFlags.clear();
    }

    /**
     * Native support extracted from GameServer::runServerLoopTick @004F08C0 quest event-notification branch.
     */
    private static void sendQuestChangedNotifications(Player player, int changeFlags) {
        if ((changeFlags & 0x10) != 0) {
            CServerApp.sendGameEventNotification(0x10, 0, player);
        }
        if ((changeFlags & 0x20) != 0) {
            CServerApp.sendGameEventNotification(0x20, 0, player);
        }
        if ((changeFlags & 0x40) != 0) {
            CServerApp.sendGameEventNotification(0x40, 0, player);
        }
        if ((changeFlags & 0x80) != 0) {
            CServerApp.sendGameEventNotification(0x80, 0, player);
        }
    }

    /**
     * Native support extracted from GameServer::runServerLoopTick @004F08C0 CPU usage tail.
     */
    private void updateCpuUsageStats(int tickStart) {
        cpuUsageTenthPctPeak += Globals.currentTickMillis() - tickStart;
        if (nativeServerLoopPhase() == 0x0F) {
            cpuUsageTenthPct = cpuUsageTenthPctPeak;
            cpuUsageTenthPctSum += cpuUsageTenthPctPeak;
            cpuUsageSampleCount++;
            if (cpuUsageTenthPctMax < cpuUsageTenthPctPeak) {
                cpuUsageTenthPctMax = cpuUsageTenthPctPeak;
            }
            cpuUsageTenthPctPeak = 0;
        }
    }

    /**
     * Native: GameServer::updatePeriodicMissionResults @004F8967.
     * Fully ported.
     */
    private void updatePeriodicMissionResults() {
        if (dedicatedServerConsoleActive == 0) {
            countConnectedPlayersForLocalConsole();
        }
        updateMissionResultsForPlayers();
    }

    /**
     * Native support extracted from GameServer::updatePeriodicMissionResults @004F8967.
     * Fully ported.
     */
    private int countConnectedPlayersForLocalConsole() {
        int connectedPlayers = 0;
        for (Player player : playerList.players) {
            if (player.clientConnected != 0) {
                connectedPlayers++;
            }
        }
        return connectedPlayers;
    }

    /**
     * Native: GameServer::updateMissionResultsForPlayers @004F8922.
     * Fully ported.
     */
    private void updateMissionResultsForPlayers() {
        for (Player player : playerList.players) {
            updateMissionResultForPlayer(player);
        }
    }

    /**
     * Native: GameServer::updateMissionResultForPlayer @004F856A.
     * Fully ported.
     */
    private void updateMissionResultForPlayer(Player player) {
        if (player.missionEntryStateSent != 0 && player.isActive == 0) {
            if (player.missionResultState < MISSION_RESULT_FAILED) {
                if (player.controlledUnit == null || ((Unit) player.controlledUnit).respawning != 0) {
                    player.missionResultState = MISSION_RESULT_FAILED;
                    CServerApp.sendTwoDwordAction(player, GameActionId.MISSION_FAILED_ACTION_B4, player.missionResultState, 0);
                    if (player.clientConnected != 0 && networkSessionActive == 0) {
                        CServerApp.sendTwoDwordAction(player, GameActionId.MISSION_FAILED_ACTION_B4, 0, 0);
                    }
                } else if (missionScriptRuntime.missionFailureValue < 1) {
                    if (missionScriptRuntime.missionCompleteCount != 0 && player.missionResultState != MISSION_RESULT_COMPLETE) {
                        pushMessage("Logic : Mission Complete");
                        CServerApp.sendTwoDwordAction(player, GameActionId.MISSION_COMPLETE_ACTION_B5, 0, 0);
                        player.missionResultState = MISSION_RESULT_COMPLETE;
                    }
                } else if (player.missionResultState < MISSION_RESULT_FAILED) {
                    int missionFailureCode = missionScriptRuntime.missionFailureValue;
                    pushMessage("Logic : Mission Failed");
                    CServerApp.sendTwoDwordAction(player, GameActionId.MISSION_FAILED_ACTION_B4, missionFailureCode, 0);
                    player.missionResultState = missionFailureCode & 0xFF;
                }
            } else if (networkSessionActive != 0
                    && player.isMapLoadPending()
                    && (short) ((Unit) player.controlledUnit).m_nHP < -0x27
                    && player.returnAfterDeathPending != 0) {
                reenterMissionAfterNetworkDeath(player);
            }
        }
    }

    /**
     * Native support extracted from GameServer::updateMissionResultForPlayer @004F856A return-after-death branch.
     * Fully ported.
     */
    private void reenterMissionAfterNetworkDeath(Player player) {
        player.returnAfterDeathPending = 0;
        Unit controlledUnit = (Unit) player.controlledUnit;
        if (controlledUnit.inventory == null) {
            controlledUnit.inventory = new Inventory();
        }
        objectLists.corpses.remove(controlledUnit);
        controlledUnit.word = 0;
        restoreExistingControlledUnitForPlayer(player);
        placeMissionEntryUnits(player);
        player.missionResultState = 0;
        if (networkSessionActive != 0) {
            restoreMissionReentryRelationsFromSelf(player);
        }
        CServerApp.netUpdate(controlledUnit, null, ALL_UNIT_UPDATE_FLAGS, UNIT_EQUIPMENT_BROADCAST_MASK, 0, 0);
    }

    /**
     * Native support extracted from GameServer::updateMissionResultForPlayer @004F856A relation-copy block.
     * Fully ported.
     */
    private void restoreMissionReentryRelationsFromSelf(Player player) {
        Player self = playerList.getByName(SELF_PLAYER_NAME);
        if (self != null) {
            for (int playerId = MISSION_ENTRY_RELATION_FIRST_COPY_ID;
                 playerId < MISSION_ENTRY_RELATION_COPY_LIMIT;
                 playerId++) {
                missionScriptRuntime.missionDiplomacyState.setRelationFlags(
                        playerId,
                        player.playerId,
                        missionScriptRuntime.missionDiplomacyState.relationFlags(playerId, self.playerId)
                );
                missionScriptRuntime.missionDiplomacyState.setRelationFlags(
                        player.playerId,
                        playerId,
                        missionScriptRuntime.missionDiplomacyState.relationFlags(self.playerId, playerId)
                );
            }
            missionScriptRuntime.missionDiplomacyState.setRelationFlags(
                    self.playerId,
                    player.playerId,
                    SELF_RELATION_FLAGS
            );
            missionScriptRuntime.missionDiplomacyState.setRelationFlags(
                    player.playerId,
                    self.playerId,
                    SELF_RELATION_FLAGS
            );
            CServerApp.sendDiplomacyStateSnapshot(player);
        }
    }

    /**
     * Native: GameServer::advanceServerLoopCounterAndObjects @004F8521.
     * Fully ported.
     */
    private void advanceServerLoopCounterAndObjects() {
        serverLoopCounter++;
        drainLocalServerGameActions();
        objectLists.update();
        activeUnits.updateActiveUnits();
        CServerApp.broadcastServerLoopCounter(serverLoopCounter);
    }

    /**
     * Native: GameServer::pumpServerWorldActions @004F84F8.
     * Fully ported.
     */
    public void pumpServerWorldActions() {
        drainLocalServerGameActions();
        CServerApp.sendCurrentServerLoopCounter(null);
        CServerApp.flushActiveClientWriteBuffers();
    }

    /**
     * Native: GameServer::drainLocalServerGameActions @004F84C6.
     * Fully ported.
     */
    private void drainLocalServerGameActions() {
        CGameAction action = CServerApp.readNextServerGameAction();
        while (action != null) {
            handleServerGameAction(action);
            action = CServerApp.readNextServerGameAction();
        }
    }

    /**
     * Native: GameServer::handleServerGameAction @004F515D.
     * Fully ported.
     */
    private void handleServerGameAction(CGameAction action) {
        if (action instanceof RequestMapLoadAction requestMapLoadAction) {
            Player player = playerList.getPlayerById(requestMapLoadAction.netID.get());
            if (player != null) {
                int shouldStageMissionEntry = requestMapLoadAction.firstPayloadDword.get() == 0 ? 1 : 0;
                sendInitialScenarioState(player, shouldStageMissionEntry);
            }
            return;
        }
        if (action instanceof MapLoadCompleteAction) {
            handleMapLoadCompleteRequest(action);
            return;
        }
        if (action.ID.get() == GameActionId.NEW_SEGMENT_ACTION_64.id) {
            return;
        }
        if (action.unitOrderMode.get() != 0 && action instanceof UnitTokenListAction unitTokenListAction) {
            handleMissionUnitOrderAction(unitTokenListAction);
            return;
        }
        dispatchClientRequestAction(action);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D unit-order branch.
     */
    private void handleMissionUnitOrderAction(UnitTokenListAction action) {
        if (missionScriptRuntime == null || action.entryCount.get() == 0) {
            return;
        }
        Unit firstUnit = findUnitOrderMember(action, 0);
        if (firstUnit == null) {
            return;
        }
        Player owner = firstUnit.owner;
        MissionOrderContext orderContext = buildMissionOrderGroup(action, owner, firstUnit);
        Token target = null;
        if (action.unitOrderMode.get() == 3) {
            target = findMissionOrderTargetByToken(readActionWord(action, CGameAction.BODY_OFFSET + 4));
            if (target == null) {
                return;
            }
        }
        dispatchMissionUnitOrder(action, orderContext.orderGroup(), orderContext.activeUnit(), target);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D order group creation branch.
     */
    private MissionOrderContext buildMissionOrderGroup(UnitTokenListAction action, Player owner, Unit firstUnit) {
        UnitGroup orderGroup = owner.unitGroups.createGroupReplacingFirstEmpty();
        Unit activeUnit = firstUnit;
        orderGroup.addUnit(firstUnit);
        firstUnit.beginDyingTransition();
        for (int index = 0; index < action.entryCount.get(); index++) {
            if (index == 0) {
                continue;
            }
            activeUnit = findUnitOrderMember(action, index);
            if (activeUnit != null) {
                orderGroup.addUnit(activeUnit);
                activeUnit.beginDyingTransition();
            }
        }
        owner.unitGroups.add(orderGroup);
        return new MissionOrderContext(orderGroup, activeUnit);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D unit-order locals `pUnitGroup_` and
     * `pUnit_` after the token-list group creation loop.
     */
    private record MissionOrderContext(UnitGroup orderGroup, Unit activeUnit) {
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D unit-order switch.
     */
    private void dispatchMissionUnitOrder(UnitTokenListAction action, UnitGroup orderGroup, Unit activeUnit, Token target) {
        GameActionId actionId = GameActionId.fromId(action.ID.get());
        if (actionId == null) {
            return;
        }
        Unit targetUnit = target instanceof Unit unit ? unit : null;
        Building targetBuilding = target instanceof Building building ? building : null;
        int x = readActionByte(action, CGameAction.BODY_OFFSET);
        int y = readActionByte(action, CGameAction.BODY_OFFSET + 2);
        Spell selectedOrderSpell = null;
        if (actionId == GameActionId.CAST_SPELL_AT_UNIT_ACTION_1E || actionId == GameActionId.CAST_SPELL_AT_POINT_ACTION_1F) {
            int spellId = mapUnitOrderSpellSlot(readActionByte(action, CGameAction.BODY_OFFSET + 6));
            selectedOrderSpell = prepareSelectedUnitOrderSpells(orderGroup, spellId);
        }
        switch (actionId) {
            case STAND_STILL_ORDER_ACTION_12 -> missionScriptRuntime.initializeStandStillOrderGroup(orderGroup);
            case PICKUP_ALL_SACKS_ACTION_13 -> missionScriptRuntime.initializePickupAllSacksOrderUnit(activeUnit);
            case RETREAT_ORDER_ACTION_14 -> missionScriptRuntime.initializeRetreatOrderGroup(orderGroup);
            case MOVE_ORDER_ACTION_16, UNIT_TOKEN_LIST_ACTION_1C ->
                    handleCommandCellMoveOrder(actionId, orderGroup, x, y);
            case GUARD_ORDER_ACTION_17 -> missionScriptRuntime.initializeAdvanceToMissionCellGroup(orderGroup, 0);
            case STAND_GROUND_ORDER_ACTION_18 -> orderGroup.enterScenarioMissionGroupScriptState(missionScriptRuntime);
            case ATTACK_TARGET_ORDER_ACTION_19 -> {
                if (targetBuilding != null) {
                    missionScriptRuntime.initializeAttackTargetGroup(orderGroup, targetBuilding);
                } else {
                    missionScriptRuntime.initializeAttackTargetGroup(orderGroup, targetUnit);
                }
            }
            case ATTACK_CELL_ORDER_ACTION_1A ->
                    missionScriptRuntime.initializeCommandCellOrTargetGroupFromBytes(orderGroup, x, y);
            case DEFEND_TARGET_ORDER_ACTION_1B ->
                    missionScriptRuntime.initializeRangeTargetEngagementGroup(orderGroup, target, 0);
            case PATROL_ORDER_ACTION_1D -> missionScriptRuntime.initializeWaypointCellGroup(orderGroup, x, y);
            case CAST_SPELL_AT_UNIT_ACTION_1E ->
                    missionScriptRuntime.initializeTargetSpellOrderGroup(orderGroup, target, selectedOrderSpell);
            case CAST_SPELL_AT_POINT_ACTION_1F -> {
                if (selectedOrderSpell.isPointTarget()) {
                    missionScriptRuntime.initializeCellSpellOrderGroup(orderGroup, x, y, selectedOrderSpell);
                }
            }
            case PICKUP_ORDER_ACTION_21 -> {
                int targetCell = ((y & 0xFF) << 8) | (x & 0xFF);
                if (worldMap.hasSackAtCell(targetCell)) {
                    activeUnit.inventory.insertIndex = readActionWord(action, CGameAction.BODY_OFFSET + 4);
                    missionScriptRuntime.initializePickupOrderUnit(activeUnit, x, y);
                } else {
                    pushMessage("Sack not found at "
                            + readActionWord(action, CGameAction.BODY_OFFSET)
                            + ","
                            + readActionWord(action, CGameAction.BODY_OFFSET + 2));
                }
            }
            case ENTER_TOWN_ORDER_ACTION_24 -> {
                int buildingTokenId = readActionWord(action, CGameAction.BODY_OFFSET + 4);
                Building building = objectLists.buildings.findByTokenId(buildingTokenId);
                if (building != null) {
                    missionScriptRuntime.initializeEnterBuildingOrderUnit(activeUnit, building);
                } else {
                    pushMessage("No building #" + buildingTokenId);
                }
            }
            case CANCEL_UNIT_SPELL_EFFECT_ACTION_25 -> {
                Spell spell = takeUnitOrderCastSpellItem(activeUnit, readActionWord(action, CGameAction.BODY_OFFSET + 6));
                if (spell != null) {
                    missionScriptRuntime.initializeTargetSpellOrderGroup(orderGroup, target, spell);
                }
            }
            case CANCEL_POINT_SPELL_EFFECT_ACTION_26 -> {
                int itemSlot = readActionWord(action, CGameAction.BODY_OFFSET + 6);
                Spell spell = takeUnitOrderCastSpellItem(activeUnit, itemSlot);
                if (spell != null) {
                    if (spell.isPointTarget()) {
                        missionScriptRuntime.initializeCellSpellOrderGroup(orderGroup, x, y, spell);
                    } else {
                        restoreUnitOrderCastSpellItem(activeUnit, itemSlot);
                    }
                }
            }
            default -> handleNativeNoopMissionUnitOrderDefault(action);
        }
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D command-cell order cases.
     */
    private void handleCommandCellMoveOrder(GameActionId actionId, UnitGroup orderGroup, int x, int y) {
        if (actionId == GameActionId.UNIT_TOKEN_LIST_ACTION_1C) {
            pushMessage("defend location comes");
        }
        missionScriptRuntime.initializeCommandCellMoveGroup(orderGroup, x, y);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D cancel spell-effect item branch.
     */
    private static Spell takeUnitOrderCastSpellItem(Unit unit, int itemSlot) {
        Item item = unit.inventory.takeItemAt(itemSlot, 1);
        if (item == null) {
            return null;
        }
        Effect firstEffect = item.effects.isEmpty() ? null : item.effects.getFirst();
        if (firstEffect == null || !firstEffect.isA(EffectId.CAST_SPELL)) {
            return null;
        }
        Spell spell = new Spell((byte) firstEffect.mValue.getS1());
        unit.pItem = item;
        unit.secondarySpell = spell;
        spell.updateStats(firstEffect.mValue.getB3());
        return spell;
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D cancel point-spell non-point branch.
     */
    private static void restoreUnitOrderCastSpellItem(Unit unit, int itemSlot) {
        unit.inventory.insertItem(itemSlot, unit.pItem);
        unit.pItem = null;
        unit.secondarySpell = null;
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D selected spell lookup table
     * DAT_005f8120/g_Spell_IDs @005F8120.
     */
    private static int mapUnitOrderSpellSlot(int spellSlot) {
        return UNIT_ORDER_SPELL_SLOT_TO_SPELL_ID[spellSlot] & 0xFF;
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D selected spell branch.
     */
    private static Spell prepareSelectedUnitOrderSpells(UnitGroup orderGroup, int spellId) {
        Spell selectedSpell = null;
        for (Unit unit : orderGroup.units) {
            if (unit.spellbook != null && spellId != 0) {
                unit.secondarySpell = unit.spellbook.find(spellId);
                if (unit.secondarySpell != null) {
                    selectedSpell = unit.secondarySpell;
                }
            }
        }
        return selectedSpell;
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D unit-token list reads.
     */
    private Unit findUnitOrderMember(UnitTokenListAction action, int index) {
        byte[] unitTokenIds = action.unitTokenIds.get();
        int offset = index * Short.BYTES;
        int tokenId = Short.toUnsignedInt(ByteBuffer.wrap(unitTokenIds, offset, Short.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getShort());
        return resolveActionUnit(action.netID.get(), tokenId);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D unit-target lookup branch.
     */
    private Token findMissionOrderTargetByToken(int tokenId) {
        int normalizedTokenId = tokenId & 0xFFFF;
        Unit unit = activeUnits.findByTokenId(normalizedTokenId);
        if (unit != null) {
            return unit;
        }
        return objectLists.buildings.findByTokenId(normalizedTokenId);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D order packet word reads.
     */
    private static int readActionWord(CGameAction action, int nativeObjectOffset) {
        byte[] bytes = action.GetSlice(nativeObjectOffset, Short.BYTES);
        return Short.toUnsignedInt(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort());
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D order packet byte reads.
     */
    private static int readActionByte(CGameAction action, int nativeObjectOffset) {
        return action.GetSlice(nativeObjectOffset, 1)[0] & 0xFF;
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D unit-order switch default/no-op branch.
     * Fully ported support helper.
     */
    private void handleNativeNoopMissionUnitOrderDefault(@SuppressWarnings("unused") UnitTokenListAction action) {
        // Native default falls through without side effects after group construction.
    }

    /**
     * Native support boundary for GameServer::handleServerGameAction @004F515D request-dispatch cases.
     */
    private void dispatchClientRequestAction(CGameAction action) {
        if (action instanceof PlayerJoinAction playerJoinAction) {
            if (playerJoinAction.getSourceClient().GetLoginName() != null) {
                handlePlayerJoinRequest(playerJoinAction);
            }
            return;
        }
        if (action instanceof MapChunkTransferCompleteAction) {
            handleMapChunkTransferComplete(action);
            return;
        }
        if (action instanceof RequestMapChunkAction requestMapChunkAction) {
            sendRequestedMapChunk(requestMapChunkAction);
            return;
        }
        if (action.ID.get() == GameActionId.MAP_LOAD_COMPLETE_ACTION_05.id) {
            handleMapLoadCompleteRequest(action);
            return;
        }
        if (action.ID.get() == GameActionId.SAVE_GAME_REQUEST_ACTION_07.id
                && action instanceof SaveGameRequestAction saveGameRequestAction) {
            handleSaveGameRequest(saveGameRequestAction);
            return;
        }
        if (action instanceof LoadGameRequestAction) {
            handleLoadGameRequest();
            return;
        }
        if (action instanceof ClientShutdownRequestAction) {
            handleClientShutdownRequest();
            return;
        }
        if (action instanceof LoginRequestAction loginRequestAction) {
            handleLoginRequest(loginRequestAction);
            return;
        }
        if (action instanceof InventoryTransferAction inventoryTransferAction) {
            handleInventoryTransferRequest(inventoryTransferAction);
            return;
        }
        if (action instanceof DropGoldAction dropGoldAction) {
            handleDropGoldRequest(dropGoldAction);
            return;
        }
        if (action.ID.get() == GameActionId.ADJUST_PLAYER_GOLD_ACTION_3E.id
                && action instanceof TwoDwordAction adjustPlayerGoldAction) {
            handleAdjustPlayerGoldRequest(adjustPlayerGoldAction);
            return;
        }
        if (action.ID.get() == GameActionId.UPDATE_DIPLOMACY_RELATIONS_ACTION_45.id
                && action instanceof ShortArrayBlobAction updateDiplomacyRelationsAction) {
            handleUpdateDiplomacyRelationsRequest(updateDiplomacyRelationsAction);
            return;
        }
        if (action.ID.get() == GameActionId.UPDATE_BATTLE_PREFERENCE_ACTION_46.id
                && action instanceof TwoDwordAction updateBattlePreferenceAction) {
            handleUpdateBattlePreferenceRequest(updateBattlePreferenceAction);
            return;
        }
        if (action.ID.get() == GameActionId.REQUEST_PLAYER_STATE_RESYNC_ACTION_4A.id) {
            handlePlayerStateResyncRequest(action);
            return;
        }
        if (action.ID.get() == GameActionId.RETURN_AFTER_DEATH_ACTION_4B.id) {
            handleReturnAfterDeathRequest(action);
            return;
        }
        if (action.ID.get() == GameActionId.REVIVE_STUCK_HERO_ACTION_4C.id) {
            handleReviveStuckHeroRequest(action);
            return;
        }
        if (action instanceof UnitTokenAction unitTokenAction
                && action.ID.get() == GameActionId.OPEN_SHOP_DIALOG_ACTION_32.id) {
            handleOpenShopDialogRequest(unitTokenAction);
            return;
        }
        if (action instanceof ChatTextAction chatTextAction
                && action.ID.get() == GameActionId.CHAT_TEXT_ACTION_91.id) {
            handleChatTextRequest(chatTextAction);
            return;
        }
        if (action instanceof SelectMultiplayerMapAction selectMultiplayerMapAction) {
            handleSelectMultiplayerMapRequest(selectMultiplayerMapAction);
            return;
        }
        if (action instanceof UploadCharacterFileAction uploadCharacterFileAction) {
            handleUploadCharacterFileRequest(uploadCharacterFileAction);
            return;
        }
        if (action instanceof LatencySettingAction latencySettingAction) {
            handleLatencySettingRequest(latencySettingAction);
            return;
        }
        if (isServerOwnedRequest(action.ID.get())) {
            dispatchServerOwnedRequest(action);
        }
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0xC1`.
     * Fully ported support helper.
     */
    private static void handleLatencySettingRequest(LatencySettingAction action) {
        int latencyMillis = action.payloadDword.get();
        if (latencyMillis == 0 || (latencyMillis >= MIN_LATENCY_MILLIS && latencyMillis <= MAX_LATENCY_MILLIS)) {
            CBufferManager client = CServerApp.getLocalClientByMaskedSocketId(action.netID.get());
            CLlDriver.setClientSendIntervalMs(client.GetIPAddress(), latencyMillis);
        }
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x05`.
     */
    private void handleMapLoadCompleteRequest(CGameAction action) {
        Player player = resolveActionPlayer(action.netID.get());
        if (player != null) {
            player.mapLoadPending = 0;
        }
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x07`.
     */
    private void handleSaveGameRequest(SaveGameRequestAction action) {
        saveGameFile(action.text.get());
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x08`.
     */
    private void handleLoadGameRequest() {
        pushMessage("Loading not suppported now.");
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x09`.
     */
    private void handleClientShutdownRequest() {
        pushMessage("Client request shutdown.");
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x0F`.
     */
    private void handleLoginRequest(LoginRequestAction action) {
        String credentials = action.text.get();
        CBufferManager sourceClient = action.getSourceClient();
        if (!Globals.passwordManager.checkPassword(credentials)) {
            rejectClientJoin(sourceClient, 0, "Incorrect password");
            return;
        }
        CServerApp.sendLoginAcceptedHandshake(sourceClient);
        int separator = credentials.indexOf('\u0001');
        sourceClient.SetLoginName(credentials.substring(0, separator));
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x39` and
     * GameServer::sendMapChecksumToPlayer @004F1CDD.
     */
    private void handleMapChunkTransferComplete(CGameAction action) {
        Player player = playerList.getPlayerById(action.netID.get());
        if (player != null) {
            sendMapChecksumToPlayer(player);
        }
    }

    /**
     * Native: GameServer::sendMapChecksumToPlayer @004F1CDD.
     * Fully ported.
     */
    private void sendMapChecksumToPlayer(Player player) {
        MapChecksumAction action = new MapChecksumAction();
        action.playerID.set(player.playerId);
        action.firstPayloadDword.set(gMapChecksum);
        action.text.set(mapFileName);
        CServerApp.sendGameAction(action);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x3B`.
     */
    private void sendRequestedMapChunk(RequestMapChunkAction request) {
        Player player = resolveActionPlayer(request.netID.get());
        if (player == null) {
            return;
        }

        ByteBuffer mapFile = Globals.gameFileManager.get(mapFileName).duplicate();
        int requestedOffset = request.firstPayloadDword.get();
        int chunkSize = Math.min(MAP_CHUNK_TRANSFER_SIZE, mapFile.limit() - requestedOffset);
        byte[] chunkBytes = new byte[chunkSize];
        mapFile.position(requestedOffset);
        mapFile.get(chunkBytes);

        MapChunkAction action = MapChunkAction.global;
        action.playerID.set(player.playerId);
        action.ID.set(MapChunkAction.ACTION_ID);
        action.chunkFilePath.set(getMapChunkTransferFileName());
        action.currentOffset.set(requestedOffset);
        action.totalFileSize.set(mapFile.limit());
        action.size.set(chunkSize);
        action.chunkBytes.set(chunkBytes);
        CServerApp.sendGameAction(action);
        CServerApp.sendCurrentServerLoopCounter(player);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x3B` map path basename branch.
     */
    private String getMapChunkTransferFileName() {
        int lastBackslash = mapFileName.lastIndexOf('\\');
        return lastBackslash == -1 ? mapFileName : mapFileName.substring(lastBackslash + 1);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x22`.
     * Fully ported support helper.
     */
    private void handleInventoryTransferRequest(InventoryTransferAction action) {
        Unit unit = resolveActionUnit(action.netID.get(), action.unitTokenId.get());
        if (unit == null) {
            return;
        }

        int sourceContainerType = action.sourceContainerType.get();
        int destinationContainerType = action.destinationContainerType.get();
        if (isShopContainer(sourceContainerType) && isShopContainer(destinationContainerType)) {
            handleShopInventoryTransferRequest(action, unit);
            return;
        }
        if (sourceContainerType == INVENTORY_CONTAINER_GROUND_SACK) {
            handleGroundSackToUnitInventoryTransfer(unit, action.destinationSlot.get());
            return;
        }
        unit.changedValues = UnitDirtyFlags.NONE.value;
        if (sourceContainerType == INVENTORY_CONTAINER_UNIT_INVENTORY
                && destinationContainerType == INVENTORY_CONTAINER_UNIT_INVENTORY) {
            handleUnitInventoryToUnitInventoryTransfer(unit, action.sourceSlot.get(), action.destinationSlot.get(), action.quantityOrItemId.get());
            return;
        }
        if (sourceContainerType == INVENTORY_CONTAINER_UNIT_INVENTORY
                && destinationContainerType == INVENTORY_CONTAINER_GROUND_SACK) {
            handleUnitInventoryToGroundSackTransfer(unit, action.sourceSlot.get(), action.destinationSlot.get(), action.quantityOrItemId.get());
            return;
        }
        if (isShopContainer(sourceContainerType) || isShopContainer(destinationContainerType)) {
            handleShopInventoryTransferRequest(action, unit);
            return;
        }
        if (sourceContainerType == INVENTORY_CONTAINER_EQUIPMENT
                && destinationContainerType == INVENTORY_CONTAINER_EQUIPMENT) {
            handleEquipmentToEquipmentTransfer(unit, action.sourceSlot.get());
            return;
        }
        if (sourceContainerType == INVENTORY_CONTAINER_EQUIPMENT
                && destinationContainerType == INVENTORY_CONTAINER_UNIT_INVENTORY) {
            handleEquipmentToUnitInventoryTransfer(unit, action.sourceSlot.get(), action.destinationSlot.get());
            return;
        }
        if (sourceContainerType == INVENTORY_CONTAINER_EQUIPMENT
                && destinationContainerType == INVENTORY_CONTAINER_GROUND_SACK) {
            handleEquipmentToGroundSackTransfer(unit, action.sourceSlot.get(), action.destinationSlot.get());
            return;
        }
        if (sourceContainerType == INVENTORY_CONTAINER_UNIT_INVENTORY
                && destinationContainerType == INVENTORY_CONTAINER_EQUIPMENT) {
            handleUnitInventoryToEquipmentTransfer(unit, action.sourceSlot.get(), action.quantityOrItemId.get());
            return;
        }
        handleNativeInventoryTransferDestinationFallthrough(action, unit);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x22`, source container `3`.
     */
    private void handleGroundSackToUnitInventoryTransfer(Unit unit, int destinationSlot) {
        if (worldMap.findSackAtTargetHandle(unit.m_pTargetHandle) == null) {
            pushMessage("Invalid pickup order - no sack there.");
            return;
        }
        unit.inventory.insertIndex = destinationSlot;
        unit.missionActionCode = MissionActionCode.PICKUP_ORDER;
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x22` shop branches.
     */
    private void handleShopInventoryTransferRequest(InventoryTransferAction action, Unit unit) {
        int sourceContainerType = action.sourceContainerType.get();
        int destinationContainerType = action.destinationContainerType.get();
        int sourceSlot = (short) (int) action.sourceSlot.get();
        int destinationSlot = (short) (int) action.destinationSlot.get();
        int quantity = action.quantityOrItemId.get() & 0xFFFF;
        if (isShopContainer(sourceContainerType) && isShopContainer(destinationContainerType)) {
            Shop shop = findInteractiveShopNearTarget(unit.m_pTargetHandle);
            if (shop == null) {
                return;
            }
            shop.transferTrackedItem(
                    unit,
                    sourceContainerType,
                    sourceSlot,
                    destinationContainerType,
                    destinationSlot,
                    quantity
            );
            return;
        }
        if (sourceContainerType == INVENTORY_CONTAINER_UNIT_INVENTORY
                && destinationContainerType == INVENTORY_CONTAINER_SHOP_TRANSACTION) {
            handleUnitInventoryToShopTransaction(unit, sourceSlot, destinationSlot, quantity);
            return;
        }
        if (sourceContainerType == INVENTORY_CONTAINER_SHOP_TRANSACTION
                && destinationContainerType == INVENTORY_CONTAINER_UNIT_INVENTORY) {
            Shop shop = findInteractiveShopNearTarget(unit.m_pTargetHandle);
            if (shop == null) {
                return;
            }
            handleShopTransactionToUnitInventory(unit, shop, sourceSlot, destinationSlot, quantity);
            return;
        }
        if (sourceContainerType == INVENTORY_CONTAINER_SHOP_TRANSACTION
                && destinationContainerType == INVENTORY_CONTAINER_GROUND_SACK) {
            Shop shop = findInteractiveShopNearTarget(unit.m_pTargetHandle);
            if (shop == null) {
                return;
            }
            handleShopTransactionToGroundSackTransfer(unit, shop, sourceSlot, destinationSlot, quantity);
            return;
        }
        if (sourceContainerType == INVENTORY_CONTAINER_EQUIPMENT
                && destinationContainerType == INVENTORY_CONTAINER_SHOP_TRANSACTION) {
            handleEquipmentToShopTransaction(unit, sourceSlot, destinationSlot);
            return;
        }
        if (sourceContainerType == INVENTORY_CONTAINER_SHOP_TRANSACTION
                && destinationContainerType == INVENTORY_CONTAINER_EQUIPMENT) {
            Shop shop = findInteractiveShopNearTarget(unit.m_pTargetHandle);
            if (shop == null) {
                return;
            }
            handleShopTransactionToEquipment(unit, shop, sourceSlot, quantity);
            return;
        }
        handleNativeInventoryTransferDestinationFallthrough(action, unit);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x22`, source container `2` and destination container `2`.
     */
    private void handleUnitInventoryToUnitInventoryTransfer(
            Unit unit,
            int sourceSlot,
            int destinationSlot,
            int quantity
    ) {
        Item item = unit.inventory.takeItemAt((short) sourceSlot, quantity & 0xFFFF);
        if (item == null) {
            return;
        }
        unit.inventory.insertItem((short) destinationSlot, item);
        refreshTransferredUnitInventory(unit);
        saveInventoryTransferHumanoidIfDue(unit);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x22`, source container `2`.
     */
    private void handleUnitInventoryToShopTransaction(
            Unit unit,
            int sourceSlot,
            int destinationSlot,
            int quantity
    ) {
        Item item = unit.inventory.takeItemAt(sourceSlot, quantity);
        if (item == null) {
            return;
        }
        item.owner = unit.owner;
        Shop shop = findInteractiveShopNearTarget(unit.m_pTargetHandle);
        if (shop == null) {
            return;
        }
        shop.addTrackedTransactionItem(unit, destinationSlot, item);
        refreshTransferredUnitInventory(unit);
        saveInventoryTransferHumanoidIfDue(unit);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x22`, source container `4`.
     */
    private void handleShopTransactionToUnitInventory(
            Unit unit,
            Shop shop,
            int sourceSlot,
            int destinationSlot,
            int quantity
    ) {
        Item item = shop.takeTrackedTransactionItem(unit, sourceSlot, quantity);
        if (item == null) {
            return;
        }
        unit.inventory.insertItem(destinationSlot, item);
        refreshTransferredUnitInventory(unit);
        saveInventoryTransferHumanoidIfDue(unit);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x22`, destination container `3`.
     */
    private void handleUnitInventoryToGroundSackTransfer(
            Unit unit,
            int sourceSlot,
            int destinationPackedCell,
            int quantity
    ) {
        Item item = unit.inventory.takeItemAt((short) sourceSlot, quantity & 0xFFFF);
        if (item == null) {
            return;
        }
        dropTransferredItemToGroundSack(unit, item, destinationPackedCell);
        refreshTransferredUnitInventory(unit);
        saveInventoryTransferHumanoidIfDue(unit);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x22`, source container `4` and destination container `3`.
     */
    private void handleShopTransactionToGroundSackTransfer(
            Unit unit,
            Shop shop,
            int sourceSlot,
            int destinationPackedCell,
            int quantity
    ) {
        Item item = shop.takeTrackedTransactionItem(unit, sourceSlot, quantity);
        if (item == null) {
            return;
        }
        dropTransferredItemToGroundSack(unit, item, destinationPackedCell);
        refreshTransferredUnitEncumbrance(unit);
        saveInventoryTransferHumanoidIfDue(unit);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x22`, source container `1` and destination container `1`.
     */
    private void handleEquipmentToEquipmentTransfer(Unit unit, int sourceSlot) {
        Item item = takeEquippedTransferItem(unit, sourceSlot);
        if (item == null) {
            return;
        }
        int equipmentMask = putTransferItemOnEquipment(unit, (short) sourceSlot, item);
        refreshTransferredUnitEquipment(unit, equipmentMask | equipmentSlotMaskFromSourceSlot(sourceSlot));
        saveInventoryTransferHumanoidIfDue(unit);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x22`, source container `1` and destination container `2`.
     */
    private void handleEquipmentToUnitInventoryTransfer(Unit unit, int sourceSlot, int destinationSlot) {
        Item item = takeEquippedTransferItem(unit, sourceSlot);
        if (item == null) {
            return;
        }
        unit.inventory.insertItem((short) destinationSlot, item);
        refreshTransferredUnitInventoryAndEquipment(unit, equipmentSlotMaskFromSourceSlot(sourceSlot));
        saveInventoryTransferHumanoidIfDue(unit);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x22`, source container `1` and destination container `3`.
     */
    private void handleEquipmentToGroundSackTransfer(Unit unit, int sourceSlot, int destinationPackedCell) {
        Item item = takeEquippedTransferItem(unit, sourceSlot);
        if (item == null) {
            return;
        }
        dropTransferredItemToGroundSack(unit, item, destinationPackedCell);
        refreshTransferredUnitEquipment(unit, equipmentSlotMaskFromSourceSlot(sourceSlot));
        saveInventoryTransferHumanoidIfDue(unit);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x22`, source container `2` and destination container `1`.
     */
    private void handleUnitInventoryToEquipmentTransfer(Unit unit, int sourceSlot, int quantity) {
        int previousInventoryCount = unit.inventory.size();
        Item item = unit.inventory.takeItemAt((short) sourceSlot, quantity & 0xFFFF);
        if (item == null) {
            return;
        }
        int equipmentMask = equipmentSlotMaskFromItem(item);
        Item remainder = putTransferItemOnEquipmentAndReturnRemainder(unit, (short) sourceSlot, item);
        int inventoryStart = 0;
        int inventoryEnd = 0;
        if (unit.inventory.size() == previousInventoryCount && remainder != item) {
            inventoryStart = (short) sourceSlot;
            inventoryEnd = inventoryStart + 1;
        }
        refreshTransferredUnitInventoryAndEquipment(unit, equipmentMask, inventoryStart, inventoryEnd);
        saveInventoryTransferHumanoidIfDue(unit);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x22`, source container `1` and destination container `4`.
     */
    private void handleEquipmentToShopTransaction(Unit unit, int sourceSlot, int destinationSlot) {
        Item item = takeEquippedTransferItem(unit, sourceSlot);
        if (item == null) {
            return;
        }
        item.owner = unit.owner;
        Shop shop = findInteractiveShopNearTarget(unit.m_pTargetHandle);
        if (shop == null) {
            return;
        }
        shop.addTrackedTransactionItem(unit, destinationSlot, item);
        refreshTransferredUnitEquipment(unit, equipmentSlotMaskFromSourceSlot(sourceSlot));
        saveInventoryTransferHumanoidIfDue(unit);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x22`, source container `4` and destination container `1`.
     */
    private void handleShopTransactionToEquipment(Unit unit, Shop shop, int sourceSlot, int quantity) {
        Item item = shop.takeTrackedTransactionItem(unit, sourceSlot, quantity);
        if (item == null) {
            return;
        }
        int equipmentMask = putTransferItemOnEquipment(unit, (short) sourceSlot, item);
        refreshTransferredUnitEquipment(unit, equipmentMask);
        saveInventoryTransferHumanoidIfDue(unit);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x22`, source container `1`.
     */
    private Item takeEquippedTransferItem(Unit unit, int sourceSlot) {
        int nativeSlot = (short) sourceSlot + 1;
        if (nativeSlot == 1) {
            return unit.releaseIncomingObject(unit.pWeapon);
        }
        if (nativeSlot == 2) {
            return unit.releaseIncomingObject(unit.pShield);
        }
        if (nativeSlot > 2 && nativeSlot < 0x0D) {
            if (unit instanceof Humanoid humanoid) {
                return unit.releaseIncomingObject(humanoid.items[nativeSlot - 1]);
            }
            pushMessage("Error - Trying to takeoff armor from non humanoid");
        }
        return null;
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x22`, destination container `1`.
     */
    private static int putTransferItemOnEquipment(Unit unit, short sourceSlot, Item item) {
        int equipmentMask = equipmentSlotMaskFromItem(item);
        putTransferItemOnEquipmentAndReturnRemainder(unit, sourceSlot, item);
        return equipmentMask;
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x22`, destination container `1`.
     */
    private static Item putTransferItemOnEquipmentAndReturnRemainder(Unit unit, short sourceSlot, Item item) {
        unit.inventory.insertIndex = sourceSlot;
        Item remainder = unit.prepareIncomingObject(item);
        if (remainder != null) {
            unit.inventory.insertItem(sourceSlot, remainder);
        }
        unit.refreshEncumbrance(0);
        return remainder;
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D equipment-mask writes in case `0x22`.
     */
    private static int equipmentSlotMaskFromSourceSlot(int sourceSlot) {
        int nativeSlot = (short) sourceSlot + 1;
        if (nativeSlot == 1 || nativeSlot == 2) {
            return 3;
        }
        if (nativeSlot > 2 && nativeSlot < 0x0D) {
            return 1 << (nativeSlot - 1);
        }
        return 0;
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D destination-equipment mask branch.
     */
    private static int equipmentSlotMaskFromItem(Item item) {
        int slot = item.getSlot();
        return slot < 3 ? 3 : 1 << (slot - 1);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x22` post-NetUpdate save tail
     * and cases `0x33`/`0x34` shop buy/sell save tails.
     */
    private static void saveInventoryTransferHumanoidIfDue(Unit unit) {
        Player player = unit.owner;
        if (Globals.gameServer.shouldSaveControlledHumanoid(player)) {
            Globals.gameServer.saveControlledHumanoid((Humanoid) unit);
        }
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x22` ground-sack drop branch.
     */
    private void dropTransferredItemToGroundSack(Unit unit, Item item, int destinationPackedCell) {
        Inventory droppedInventory = new Inventory();
        droppedInventory.addItem(item);
        int targetX = destinationPackedCell & 0xFF;
        int targetY = (destinationPackedCell >>> 8) & 0xFF;
        TargetHandle targetHandle;
        if (Math.abs(unit.m_pTargetHandle.getX() - targetX) < 3
                && Math.abs(unit.m_pTargetHandle.getY() - targetY) < 3) {
            targetHandle = new TargetHandle();
            targetHandle.initFromBytes(targetX, targetY, Globals.worldMap);
        } else {
            targetHandle = unit.m_pTargetHandle;
        }
        objectLists.sacks.createOrMergeSackAtTargetAndNotify(targetHandle, droppedInventory, 0, 1);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x22` shop-container test.
     */
    private static boolean isShopContainer(int containerType) {
        return containerType >= INVENTORY_CONTAINER_SHOP_TRANSACTION
                && containerType <= INVENTORY_CONTAINER_SHOP_SHELF_MAX;
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x22` unmatched-destination
     * fall-through after the source-container item take.
     */
    private void handleNativeInventoryTransferDestinationFallthrough(InventoryTransferAction action, Unit unit) {
        int sourceContainerType = action.sourceContainerType.get();
        int sourceSlot = (short) (int) action.sourceSlot.get();
        int quantity = action.quantityOrItemId.get() & 0xFFFF;
        Item item;
        int unitUpdateFlags = UnitDirtyFlags.NONE.value;
        int equipmentMask = 0;
        if (sourceContainerType == INVENTORY_CONTAINER_UNIT_INVENTORY) {
            item = unit.inventory.takeItemAt(sourceSlot, quantity);
            if (item == null) {
                return;
            }
            unitUpdateFlags = UnitDirtyFlags.INVENTORY_AND_ENCUMBRANCE.value;
        } else if (sourceContainerType == INVENTORY_CONTAINER_SHOP_TRANSACTION) {
            Shop shop = findInteractiveShopNearTarget(unit.m_pTargetHandle);
            if (shop == null) {
                return;
            }
            item = shop.takeTrackedTransactionItem(unit, sourceSlot, quantity);
            if (item == null) {
                return;
            }
        } else if (sourceContainerType == INVENTORY_CONTAINER_EQUIPMENT) {
            item = takeEquippedTransferItem(unit, sourceSlot);
            if (item == null) {
                return;
            }
            unitUpdateFlags = UnitDirtyFlags.toValue(
                    UnitDirtyFlags.EQUIPPED_ITEMS,
                    UnitDirtyFlags.SPEED,
                    UnitDirtyFlags.ENCUMBRANCE_WEIGHT
            );
            equipmentMask = equipmentSlotMaskFromSourceSlot(sourceSlot);
        } else {
            return;
        }
        CServerApp.netUpdate(unit, null, unitUpdateFlags | unit.changedValues, equipmentMask, 0, 0);
        saveInventoryTransferHumanoidIfDue(unit);
    }

    /**
     * Native support extracted from Unit::refreshEncumbrance @0050F065 and CServerApp::NetUpdate @00502019 in the inventory-transfer tail.
     */
    private static void refreshTransferredUnitInventory(Unit unit) {
        unit.refreshEncumbrance(0);
        CServerApp.netUpdate(
                unit,
                null,
                UnitDirtyFlags.INVENTORY_AND_ENCUMBRANCE.value,
                0,
                0,
                0
        );
    }

    /**
     * Native support extracted from Unit::refreshEncumbrance @0050F065 and GameServer::handleServerGameAction @004F515D case `0x22`, destination container `3`.
     */
    private static void refreshTransferredUnitEncumbrance(Unit unit) {
        unit.refreshEncumbrance(0);
        CServerApp.netUpdate(
                unit,
                null,
                UnitDirtyFlags.SPEED.value | UnitDirtyFlags.ENCUMBRANCE_WEIGHT.value | unit.changedValues,
                0,
                0,
                0
        );
    }

    /**
     * Native support extracted from Unit::refreshEncumbrance @0050F065 and CServerApp::NetUpdate @00502019 in the inventory-transfer equipment tail.
     */
    private static void refreshTransferredUnitEquipment(Unit unit, int equipmentMask) {
        unit.refreshEncumbrance(0);
        CServerApp.netUpdate(
                unit,
                null,
                UnitDirtyFlags.toValue(
                        UnitDirtyFlags.EQUIPPED_ITEMS,
                        UnitDirtyFlags.SPEED,
                        UnitDirtyFlags.ENCUMBRANCE_WEIGHT
                ) | unit.changedValues,
                equipmentMask,
                0,
                0
        );
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019 in the inventory-transfer tail when inventory and equipment both change.
     */
    private static void refreshTransferredUnitInventoryAndEquipment(Unit unit, int equipmentMask) {
        refreshTransferredUnitInventoryAndEquipment(unit, equipmentMask, 0, 0);
    }

    /**
     * Native support extracted from Unit::refreshEncumbrance @0050F065 and CServerApp::NetUpdate @00502019 in the inventory-transfer tail when inventory and equipment both change.
     */
    private static void refreshTransferredUnitInventoryAndEquipment(
            Unit unit,
            int equipmentMask,
            int inventoryStart,
            int inventoryEnd
    ) {
        unit.refreshEncumbrance(0);
        CServerApp.netUpdate(
                unit,
                null,
                UnitDirtyFlags.INVENTORY_AND_ENCUMBRANCE.value
                        | UnitDirtyFlags.EQUIPPED_ITEMS.value
                        | unit.changedValues,
                equipmentMask,
                inventoryStart,
                inventoryEnd
        );
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x23`.
     */
    private void handleDropGoldRequest(DropGoldAction action) {
        int playerId = action.netID.get();
        Player player = playerList.getPlayerById(playerId);
        if (player == null) {
            pushMessage("Order error: no such Player " + playerId);
            return;
        }
        Unit headUnit = null;
        for (Unit unit : player.ownedUnits) {
            headUnit = unit;
            break;
        }
        if (headUnit == null) {
            return;
        }
        int amount = action.firstPayloadDword.get();
        if (amount < 1 || player.gold < amount) {
            return;
        }
        player.gold -= amount;
        dropGoldToGroundSack(headUnit, action.secondPayloadDword.get(), amount);
        if (shouldSaveControlledHumanoid(player)) {
            saveControlledHumanoid((Humanoid) headUnit);
        }
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x23`.
     */
    private void dropGoldToGroundSack(Unit unit, int destinationPackedCell, int amount) {
        int targetX = destinationPackedCell & 0xFF;
        int targetY = (destinationPackedCell >>> 8) & 0xFF;
        TargetHandle targetHandle;
        if (Math.abs(unit.m_pTargetHandle.getX() - targetX) < 3
                && Math.abs(unit.m_pTargetHandle.getY() - targetY) < 3) {
            targetHandle = new TargetHandle();
            targetHandle.initFromBytes(targetX, targetY, Globals.worldMap);
        } else {
            targetHandle = unit.m_pTargetHandle;
        }
        objectLists.sacks.createOrMergeSackAtTarget(targetHandle, null, amount, 1);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x3E`.
     */
    private void handleAdjustPlayerGoldRequest(TwoDwordAction action) {
        if (networkSessionActive != 0) {
            return;
        }
        Player player = resolveActionPlayer(action.netID.get());
        if (player != null) {
            player.adjustGoldAndNotify(action.firstPayloadDword.get(), 1);
        }
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x45`.
     */
    private void handleUpdateDiplomacyRelationsRequest(ShortArrayBlobAction action) {
        if (missionScriptRuntime == null) {
            return;
        }
        Player sourcePlayer = playerList.getPlayerById(action.netID.get());
        if (sourcePlayer == null) {
            return;
        }

        short[] relationFlags = ActionPayloads.getShortArray(action.shortValues);
        for (Player targetPlayer : playerList.players) {
            int targetPlayerId = (short) targetPlayer.playerId;
            int oldFlags = missionScriptRuntime.getRelationFlags(sourcePlayer, targetPlayer);
            int newFlags = relationFlags[targetPlayerId] & DIPLOMACY_CLIENT_MUTABLE_MASK;
            if (oldFlags == newFlags) {
                continue;
            }

            missionScriptRuntime.missionDiplomacyState.setRelationFlags(
                    sourcePlayer.playerId,
                    targetPlayer.playerId,
                    newFlags
            );
            synchronizeDiplomacyVisibilityChange(sourcePlayer, targetPlayer, oldFlags, newFlags);
            CServerApp.sendDiplomacyStateSnapshot(targetPlayer);
        }
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x45`.
     */
    private void synchronizeDiplomacyVisibilityChange(
            Player sourcePlayer,
            Player targetPlayer,
            int oldFlags,
            int newFlags
    ) {
        if ((newFlags & CPlayer.DIPLOMACY_VISIBLE_MASK) == 0) {
            sourcePlayer.scanMaskMirror &= ~targetPlayer.scanMask;
            if ((oldFlags & CPlayer.DIPLOMACY_VISIBLE_MASK) != 0) {
                Unit controlledUnit = (Unit) sourcePlayer.controlledUnit;
                CServerApp.netUpdate(
                        controlledUnit,
                        targetPlayer,
                        UnitDirtyFlags.EQUIPPED_ITEMS.value,
                        UNIT_EQUIPMENT_BROADCAST_MASK,
                        0,
                        0
                );
            }
            return;
        }

        sourcePlayer.scanMaskMirror |= targetPlayer.scanMask;
        if ((oldFlags & CPlayer.DIPLOMACY_VISIBLE_MASK) == 0 && sourcePlayer.ownedUnits != null) {
            for (Unit unit : sourcePlayer.ownedUnits) {
                CServerApp.netUpdate(unit, targetPlayer, ALL_UNIT_UPDATE_FLAGS, UNIT_EQUIPMENT_BROADCAST_MASK, 0, 0);
            }
        }
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x46`.
     */
    private void handleUpdateBattlePreferenceRequest(TwoDwordAction action) {
        if (missionScriptRuntime == null) {
            return;
        }
        Player player = resolveActionPlayer(action.netID.get());
        if (player == null) {
            return;
        }

        int preferenceKind = action.firstPayloadDword.get();
        switch (preferenceKind) {
            case BATTLE_PREFERENCE_WIMPY_MODE -> handleWimpyModePreferenceRequest(player, action);
            case BATTLE_PREFERENCE_FORMATION_MODE -> handleFormationModePreferenceRequest(player, action);
            case BATTLE_PREFERENCE_AUTO_CASTING_MODE -> handleAutoCastingPreferenceRequest(player, action);
            case BATTLE_PREFERENCE_UNIT_AUTOCAST_SPELL -> handleToggleUnitAutocastSpellRequest(player, action);
            case BATTLE_PREFERENCE_ALT_DEBUG_COMMAND -> handleAltDebugCommandRequest(player, action);
            default -> handleUnknownBattlePreferenceRequest(action);
        }
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x46`, preference kind `1`,
     * MissionScriptRuntime::applyWithdrawThresholds @0057393C, and
     * MissionScriptRuntime::applyWimpyThresholds @00573B38.
     * Fully ported support for battle-threshold preference requests.
     */
    private void handleWimpyModePreferenceRequest(Player player, TwoDwordAction action) {
        int preferenceValue = action.secondPayloadDword.get();
        int wimpyPercent = switch (preferenceValue) {
            case 1 -> 10;
            case 2 -> 0x1E;
            default -> 0;
        };
        missionScriptRuntime.applyWithdrawThresholds(0, wimpyPercent << 1, wimpyPercent * 3, player);
        missionScriptRuntime.applyWimpyThresholds(wimpyPercent, wimpyPercent, wimpyPercent, player);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x46`, preference kind `2`,
     * and MissionScriptRuntime::setPlayerFormationMode @005737BA.
     * Fully ported support for formation-mode preference requests.
     */
    private void handleFormationModePreferenceRequest(Player player, TwoDwordAction action) {
        int preferenceValue = action.secondPayloadDword.get();
        int formationMode = switch (preferenceValue) {
            case 0 -> 0;
            case 2 -> 1;
            default -> 2;
        };
        missionScriptRuntime.setPlayerFormationMode(player, formationMode);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x46`, preference kind `3`.
     */
    private static void handleAutoCastingPreferenceRequest(Player player, TwoDwordAction action) {
        if (player.battlePreferences == null) {
            return;
        }
        player.battlePreferences.autoCasting = action.secondPayloadDword.get() & 0xFF;
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x46`, preference kind `4`.
     * Fully ported support for unit-autocast spell preference requests.
     */
    private void handleToggleUnitAutocastSpellRequest(Player player, TwoDwordAction action) {
        int packedTokenAndSpell = action.secondPayloadDword.get();
        Unit unit = resolveActionOwnedAliveUnit(player, packedTokenAndSpell & 0xFFFF);
        if (unit == null || unit.missionRuntimeState == null) {
            return;
        }

        int spellIdHighWord = packedTokenAndSpell >> 16;
        int spellIdByte = spellIdHighWord & 0xFF;
        unit.missionRuntimeState.spellIndex = unit.missionRuntimeState.spellIndex == spellIdHighWord ? 0 : spellIdByte;
        CServerApp.netUpdate(unit, unit.owner, UnitDirtyFlags.SPELLBOOK.value, UNIT_EQUIPMENT_BROADCAST_MASK, 0, 0);
    }

    /**
     * Native: GameServer::resolveActionUnit @004F3A19.
     * Fully ported.
     */
    private Unit resolveActionUnit(int playerId, int tokenId) {
        Player player = resolveActionPlayer(playerId);
        return player == null ? null : resolveActionOwnedAliveUnit(player, tokenId);
    }

    /**
     * Native: GameServer::resolveActionPlayer @004F3BA9.
     * Fully ported.
     */
    private Player resolveActionPlayer(int playerId) {
        Player player = playerList.getPlayerById(playerId);
        if (player == null) {
            pushMessage("Order error, no such Player " + playerId);
        }
        return player;
    }

    /**
     * Native: GameServer::resolveActionOwnedAliveUnit @004F3A52.
     * Fully ported.
     */
    private Unit resolveActionOwnedAliveUnit(Player player, int tokenId) {
        Unit unit = player.ownedUnits.findByTokenId(tokenId);
        if (unit == null) {
            pushMessage("Order error: Player " + player.playerId + " have no unit #" + (tokenId & 0xFFFF));
            return null;
        }
        return unit.state == UnitActionState.DEAD ? null : unit;
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x46` unknown-parameter
     * diagnostic branch.
     * Fully ported support helper.
     */
    private void handleUnknownBattlePreferenceRequest(TwoDwordAction action) {
        pushMessage("Request to set unknown parameter " + action.firstPayloadDword.get() + " unprocessed");
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x46`, preference kind `0x80`.
     */
    private void handleAltDebugCommandRequest(Player player, TwoDwordAction action) {
        missionScriptRuntime.handleAltDebugCommand(player, action.secondPayloadDword.get());
    }

    /**
     * Native support extracted from MissionScriptRuntime::processMissionTurn @0056FE4D and
     * MissionScriptRuntime::handleAltDebugCommand @00578910 debug command `3`.
     */
    public boolean isTurnTracingEnabled() {
        return debugState.turnTracingEnabled != 0;
    }

    /**
     * Native support extracted from MissionScriptRuntime::executeScriptInstant @00574F3F and
     * MissionScriptRuntime::handleAltDebugCommand @00578910 debug command `0x13`.
     */
    public boolean isScriptTracingEnabled() {
        return debugState.scriptTracingEnabled != 0;
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x32`.
     */
    private void handleOpenShopDialogRequest(UnitTokenAction action) {
        Unit unit = resolveActionUnit(action.netID.get(), action.unitTokenId.get());
        if (unit == null) {
            return;
        }
        Shop shop = findInteractiveShopNearTarget(unit.m_pTargetHandle);
        if (shop == null) {
            return;
        }
        unit.beginDyingTransition();
        unit.owner.missionEntryDropCell = unit.m_pTargetHandle.getX() | (unit.m_pTargetHandle.getY() << 8);
        shop.openTrackedShopForToken(unit);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x4A`.
     */
    private void handlePlayerStateResyncRequest(CGameAction action) {
        Player player = playerList.getPlayerById(action.netID.get());
        if (player != null) {
            CServerApp.sendPlayerStateResync(player);
        }
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x4B`.
     */
    private void handleReturnAfterDeathRequest(CGameAction action) {
        Player player = resolveActionPlayer(action.netID.get());
        if (player == null || player.controlledUnit == null) {
            return;
        }
        Unit controlledUnit = (Unit) player.controlledUnit;
        if ((short) controlledUnit.m_nHP < -0x27) {
            player.returnAfterDeathPending = 1;
        }
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x4C` and
     * CServerApp::notifyUnitHitPointsChanged @00504B1D.
     */
    private void handleReviveStuckHeroRequest(CGameAction action) {
        if (missionScriptRuntime == null) {
            return;
        }
        Player player = resolveActionPlayer(action.netID.get());
        if (player == null || player.controlledUnit == null) {
            return;
        }
        Unit controlledUnit = (Unit) player.controlledUnit;
        if ((short) controlledUnit.m_nHP <= -1) {
            return;
        }
        controlledUnit.m_nHP -= 1;
        CServerApp.notifyUnitHitPointsChanged(controlledUnit);
    }

    /**
     * Native: GameServer::findInteractiveShopNearTarget @004F3CAD.
     * Fully ported.
     */
    private Shop findInteractiveShopNearTarget(TargetHandle targetHandle) {
        if (useGlobalCampaignShop != 0) {
            return globalCampaignShop;
        }
        if (objectLists.buildings == null) {
            return null;
        }
        Building building = objectLists.buildings.findInteractiveNearTarget(targetHandle);
        return building instanceof Shop shop ? shop : null;
    }

    /**
     * Native: GameServer::findInteractiveInnNearTarget @004F3D14.
     * Fully ported.
     */
    private Inn findInteractiveInnNearTarget(TargetHandle targetHandle) {
        if (objectLists.buildings == null) {
            return null;
        }
        Building building = objectLists.buildings.findInteractiveNearTarget(targetHandle);
        return building instanceof Inn inn ? inn : null;
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x91`.
     */
    private void handleChatTextRequest(ChatTextAction action) {
        String text = action.text.get();
        if (text.isEmpty()) {
            return;
        }

        Player sender = playerList.getPlayerById(action.netID.get());
        if (sender == null) {
            return;
        }

        pushMessage(sender.name + ": " + text);
        if (text.charAt(0) == '#') {
            handleChatCommand(sender, text);
            return;
        }

        int packedTargetAndDelivery = action.firstPayloadDword.get();
        int targetPlayerId = packedTargetAndDelivery & 0xFF;
        int deliveryType = (packedTargetAndDelivery >>> 8) & 0xFF;
        switch (deliveryType) {
            case ChatTextAction.CHAT_DELIVERY_SAY -> sendSayChatText(sender, text);
            case ChatTextAction.CHAT_DELIVERY_ALLIED -> sendAlliedChatText(sender, text);
            case ChatTextAction.CHAT_DELIVERY_PRIVATE -> sendPrivateChatText(sender, text, targetPlayerId);
            case ChatTextAction.CHAT_DELIVERY_SHOUT -> sendShoutChatText(sender, text);
            case ChatTextAction.CHAT_DELIVERY_BROADCAST -> sendRoutedChatText(sender, text, deliveryType, 0);
            default -> {
            }
        }
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x91`, chat delivery `0`.
     */
    private void sendSayChatText(Player sender, String text) {
        for (Player recipient : playerList.players) {
            if (recipient.isActive == 0 && canReceiveSayChat(sender, recipient)) {
                sendRoutedChatText(sender, text, ChatTextAction.CHAT_DELIVERY_SAY, recipient.playerId);
            }
        }
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x91`, chat delivery `0`.
     */
    private static boolean canReceiveSayChat(Player sender, Player recipient) {
        Unit senderUnit = (Unit) sender.controlledUnit;
        Unit recipientUnit = (Unit) recipient.controlledUnit;
        return senderUnit.m_pTargetHandle.chebyshevDistanceByXY(recipientUnit.m_pTargetHandle)
                <= Globals.serverConfig.sayrange;
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x91`, chat delivery `1`.
     */
    private void sendAlliedChatText(Player sender, String text) {
        for (Player recipient : playerList.players) {
            if (recipient.isActive == 0
                    && (missionScriptRuntime.getRelationFlags(sender, recipient) & CPlayer.ALLIED_MASK) != 0) {
                sendRoutedChatText(sender, text, ChatTextAction.CHAT_DELIVERY_ALLIED, recipient.playerId);
            }
        }
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x91`, chat delivery `2`.
     */
    private void sendPrivateChatText(Player sender, String text, int targetPlayerId) {
        sendRoutedChatText(sender, text, ChatTextAction.CHAT_DELIVERY_PRIVATE, sender.playerId);
        sendRoutedChatText(sender, text, ChatTextAction.CHAT_DELIVERY_PRIVATE, targetPlayerId);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x91`, chat delivery `3`.
     */
    private void sendShoutChatText(Player sender, String text) {
        if (sender.shoutDelayTicksRemaining == 0) {
            sendRoutedChatText(sender, text, ChatTextAction.CHAT_DELIVERY_SHOUT, 0);
            sender.shoutDelayTicksRemaining = Globals.serverConfig.shoutdelay;
            return;
        }
        sendCommandEvent(8, sender.shoutDelayTicksRemaining, sender);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x91`.
     */
    private static void sendRoutedChatText(Player sender, String text, int deliveryType, int recipientPlayerId) {
        ChatTextAction response = new ChatTextAction(text);
        if (deliveryType >= ChatTextAction.CHAT_DELIVERY_PRIVATE) {
            response.netID.set(sender.playerId);
        }
        response.playerID.set(recipientPlayerId);
        response.firstPayloadDword.set((sender.playerId & 0xFF) | ((deliveryType & 0xFF) << 8));
        CServerApp.sendGameAction(response);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0xAE`.
     * Fully ported support for selected-multiplayer-map request forwarding.
     */
    private static void handleSelectMultiplayerMapRequest(SelectMultiplayerMapAction action) {
        CServerApp.sendGameAction(action);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D server-owned request cases.
     */
    private boolean isServerOwnedRequest(int actionIdValue) {
        GameActionId actionId = GameActionId.fromId(actionIdValue);
        if (actionId == null) {
            return false;
        }
        return switch (actionId) {
            case SHOP_BUY_ACTION_33,
                 SHOP_SELL_ACTION_34,
                 UNDO_SHOP_ACTION_35,
                 CLOSE_SHOP_DIALOG_ACTION_36,
                 ENTER_INN_ACTION_38,
                 LEAVE_INN_ACTION_3A,
                 REFRESH_SHOP_SHELVES_ACTION_3F,
                 NAMED_CHARACTER_SPAWN_REQUEST_ACTION_49,
                 SUBMIT_CHARACTER_SETUP_ACTION_48 -> true;
            default -> false;
        };
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D server-owned request cases.
     */
    private void dispatchServerOwnedRequest(CGameAction action) {
        GameActionId actionId = GameActionId.fromId(action.ID.get());
        if (actionId == null) {
            return;
        }
        switch (actionId) {
            case SHOP_BUY_ACTION_33,
                 SHOP_SELL_ACTION_34,
                 UNDO_SHOP_ACTION_35,
                 CLOSE_SHOP_DIALOG_ACTION_36 -> handleShopSessionMutationRequest(action);
            case ENTER_INN_ACTION_38,
                 LEAVE_INN_ACTION_3A -> handleInnSessionMutationRequest(action);
            case REFRESH_SHOP_SHELVES_ACTION_3F -> handleRefreshShopShelvesRequest(action);
            case NAMED_CHARACTER_SPAWN_REQUEST_ACTION_49 -> handleNamedCharacterSpawnRequest(action);
            case SUBMIT_CHARACTER_SETUP_ACTION_48 -> handleCharacterSetupJoinRequest(action);
            default -> {
            }
        }
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D cases `0x33`..`0x36`,
     * Shop commit helpers @0052115F/@0052117B/@005211BB, and Shop::handleTrackedTokenUpdate @005210C9.
     */
    private void handleShopSessionMutationRequest(CGameAction action) {
        if (!(action instanceof UnitTokenAction unitTokenAction)) {
            return;
        }
        Unit unit = resolveActionUnit(action.netID.get(), unitTokenAction.unitTokenId.get());
        if (unit == null) {
            return;
        }
        Shop shop = findInteractiveShopNearTarget(unit.m_pTargetHandle);
        if (shop == null) {
            return;
        }

        GameActionId actionId = GameActionId.fromId(action.ID.get());
        switch (actionId) {
            case SHOP_BUY_ACTION_33 -> {
                shop.commitTrackedBuy(unit);
                saveInventoryTransferHumanoidIfDue(unit);
            }
            case SHOP_SELL_ACTION_34 -> {
                shop.commitTrackedSell(unit);
                saveInventoryTransferHumanoidIfDue(unit);
            }
            case UNDO_SHOP_ACTION_35 -> shop.rollbackTrackedTransaction(unit);
            case CLOSE_SHOP_DIALOG_ACTION_36 -> {
                shop.rollbackTrackedTransaction(unit);
                shop.handleTrackedTokenUpdate(unit);
            }
            default -> {
            }
        }
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D cases `0x38` and `0x3A`,
     * Inn::openUnitSession @0052F813, and Inn::closeUnitSession @0052F8E2.
     * Fully ported support helper.
     */
    private void handleInnSessionMutationRequest(CGameAction action) {
        GameActionId actionId = GameActionId.fromId(action.ID.get());
        if (actionId == null) {
            return;
        }
        switch (actionId) {
            case ENTER_INN_ACTION_38 -> {
                if (!(action instanceof UnitTokenAction enterInnAction)) {
                    return;
                }
                Unit unit = resolveActionUnit(action.netID.get(), enterInnAction.unitTokenId.get());
                if (unit == null) {
                    return;
                }
                Inn inn = findInteractiveInnNearTarget(unit.m_pTargetHandle);
                if (inn == null) {
                    return;
                }
                unit.beginDyingTransition();
                unit.owner.missionEntryDropCell = unit.m_pTargetHandle.getX() | (unit.m_pTargetHandle.getY() << 8);
                inn.openUnitSession(unit);
            }
            case LEAVE_INN_ACTION_3A -> {
                if (!(action instanceof TwoDwordAction leaveInnAction)) {
                    return;
                }
                Unit unit = resolveActionUnit(action.netID.get(), leaveInnAction.firstPayloadDword.get());
                if (unit == null) {
                    return;
                }
                Inn inn = findInteractiveInnNearTarget(unit.m_pTargetHandle);
                if (inn == null) {
                    return;
                }
                inn.closeUnitSession(unit, leaveInnAction.secondPayloadDword.get());
            }
            default -> {
            }
        }
    }

    /**
     * Native: GameServer::PrepareInnEntryUnitUpdates @004F312A.
     * Fully ported.
     */
    public void prepareInnEntryUnitUpdates(int scenarioChapter, List<Integer> innEntries) {
        UnitList transientInnUnits = new UnitList();
        try {
            for (int entry : innEntries) {
                int serverId = entry & 0xFFFF;
                if (isPlayerInnEntryServerId(serverId)) {
                    preparePlayerInnEntryUnit(scenarioChapter, serverId);
                    continue;
                }
                Unit unit = createInnEntryUnit(serverId, scenarioChapter, false);
                unit.serverID = serverId;
                unit.owner = playerList.getFirst();
                addTransientInnEntryUnit(transientInnUnits, unit);
                CServerApp.netUpdate(
                        unit,
                        unit.owner,
                        ALL_UNIT_UPDATE_FLAGS,
                        UNIT_EQUIPMENT_BROADCAST_MASK,
                        0,
                        0
                );
            }
        } finally {
            clearTransientInnEntryUnitIds(transientInnUnits);
        }
        CServerApp.sendCurrentServerLoopCounter(null);
        CServerApp.flushActiveClientWriteBuffers();
    }

    /**
     * Native support extracted from GameServer::PrepareInnEntryUnitUpdates @004F312A player-owned server-id branch.
     * Fully ported support helper.
     */
    private void preparePlayerInnEntryUnit(int scenarioChapter, int serverId) {
        Player player = playerList.getFirst();
        if (findOwnedInnEntryUnitByServerId(player, serverId) != null) {
            return;
        }

        Unit unit = createInnEntryUnit(serverId, scenarioChapter, true);
        unit.serverID = serverId;
        unit.idFull = allocateNextFreeId() & 0xFFFF;
        unit.owner = player;
        player.ownedUnits.add(unit);

        UnitGroup unitGroup = new UnitGroup();
        player.unitGroups.add(unitGroup);
        unitGroup.addUnit(unit);

        unit.initializeScenarioMissionEntryUnit(missionScriptRuntime);
        unitGroup.initializeScenarioMissionEntryGroup(missionScriptRuntime);
        CServerApp.netUpdate(
                unit,
                player,
                ALL_UNIT_UPDATE_FLAGS,
                UNIT_EQUIPMENT_BROADCAST_MASK,
                0,
                0
        );
    }

    /**
     * Native support extracted from GameServer::PrepareInnEntryUnitUpdates @004F312A temporary UnitList::AddAndAssignRuntimeId call.
     * Fully ported support helper.
     */
    private void addTransientInnEntryUnit(UnitList transientInnUnits, Unit unit) {
        unit.idFull = allocateNextFreeId() & 0xFFFF;
        transientInnUnits.add(unit);
    }

    /**
     * Native support extracted from GameServer::PrepareInnEntryUnitUpdates @004F312A temporary UnitList cleanup loop.
     * Fully ported support helper.
     */
    private void clearTransientInnEntryUnitIds(UnitList transientInnUnits) {
        for (Unit unit : transientInnUnits) {
            clearBitForId(unit.idFull);
        }
        transientInnUnits.clear();
    }

    /**
     * Native support extracted from GameServer::PrepareInnEntryUnitUpdates @004F312A player-owned unit scan.
     * Fully ported support helper.
     */
    private static Unit findOwnedInnEntryUnitByServerId(Player player, int serverId) {
        for (Unit unit : player.ownedUnits) {
            if ((unit.serverID & 0xFFFF) == serverId) {
                return unit;
            }
        }
        return null;
    }

    /**
     * Native support extracted from GameServer::PrepareInnEntryUnitUpdates @004F312A Human/Unit construction branches.
     * Fully ported support helper.
     */
    private static Unit createInnEntryUnit(int serverId, int scenarioChapter, boolean playerEntry) {
        int templateIndex = resolveInnEntryTemplateIndex(serverId, scenarioChapter);
        if (templateIndex > 0) {
            HumanInfo humanInfo = Globals.staticDataMgr.humans.get(templateIndex);
            return Human.createFromTemplate(humanInfo.name, playerEntry, false);
        }
        if (playerEntry) {
            throw new IllegalStateException("Missing player inn-entry HumanInfo template for server id " + serverId);
        }

        int unitInfoIndex = -templateIndex;
        UnitInfo unitInfo = Globals.staticDataMgr.units.get(unitInfoIndex);
        return Unit.createFromTemplateName(unitInfo.name);
    }

    /**
     * Native support extracted from GameServer::PrepareInnEntryUnitUpdates @004F312A HumanInfo/UnitInfo selection.
     * Fully ported support helper.
     */
    private static int resolveInnEntryTemplateIndex(int serverId, int scenarioChapter) {
        int baseVariantServerId = serverId * 10 + SCENARIO_INN_ENTRY_TEMPLATE_BASE;
        int baseHumanIndex = Globals.staticDataMgr.findHumanByServerId(baseVariantServerId);
        if (baseHumanIndex != 0) {
            return resolveInnEntryHumanVariantIndex(serverId, scenarioChapter, baseHumanIndex);
        }

        int baseUnitIndex = Globals.staticDataMgr.findUnitByServerId(baseVariantServerId);
        if (baseUnitIndex != 0) {
            return -resolveInnEntryUnitVariantIndex(serverId, scenarioChapter, baseUnitIndex);
        }

        int humanIndex = Globals.staticDataMgr.findHumanByServerId(serverId);
        if (humanIndex != 0) {
            return humanIndex;
        }

        int unitIndex = Globals.staticDataMgr.findUnitByServerId(serverId);
        return -unitIndex;
    }

    /**
     * Native support extracted from GameServer::PrepareInnEntryUnitUpdates @004F312A HumanInfo chapter-variant probe loop.
     * Fully ported support helper.
     */
    private static int resolveInnEntryHumanVariantIndex(int serverId, int scenarioChapter, int baseVariantIndex) {
        for (int variant = 1; variant < SCENARIO_INN_ENTRY_VARIANT_LIMIT; variant++) {
            if (Globals.staticDataMgr.findHumanByServerId(
                    serverId * 10 + SCENARIO_INN_ENTRY_TEMPLATE_BASE + variant
            ) != 0) {
                int chapterVariant = Globals.staticDataMgr.findHumanByServerId(
                        serverId * 10 + SCENARIO_INN_ENTRY_CHAPTER_VARIANT_BASE + scenarioChapter / 10
                );
                return chapterVariant == 0 ? baseVariantIndex : chapterVariant;
            }
        }
        return baseVariantIndex;
    }

    /**
     * Native support extracted from GameServer::PrepareInnEntryUnitUpdates @004F312A UnitInfo chapter-variant probe loop.
     * Fully ported support helper.
     */
    private static int resolveInnEntryUnitVariantIndex(int serverId, int scenarioChapter, int baseVariantIndex) {
        for (int variant = 1; variant < SCENARIO_INN_ENTRY_VARIANT_LIMIT; variant++) {
            if (Globals.staticDataMgr.findUnitByServerId(
                    serverId * 10 + SCENARIO_INN_ENTRY_TEMPLATE_BASE + variant
            ) != 0) {
                int chapterVariant = Globals.staticDataMgr.findUnitByServerId(
                        serverId * 10 + SCENARIO_INN_ENTRY_CHAPTER_VARIANT_BASE + scenarioChapter / 10
                );
                return chapterVariant == 0 ? baseVariantIndex : chapterVariant;
            }
        }
        return baseVariantIndex;
    }

    /**
     * Native support extracted from GameServer::PrepareInnEntryUnitUpdates @004F312A `0x15..0x1E` owned-unit branch.
     * Fully ported support helper.
     */
    private static boolean isPlayerInnEntryServerId(int serverId) {
        return serverId >= INN_ENTRY_PLAYER_SERVER_ID_FIRST && serverId < INN_ENTRY_PLAYER_SERVER_ID_LIMIT;
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0xBE`.
     * Fully ported support helper.
     */
    private void handleUploadCharacterFileRequest(UploadCharacterFileAction action) {
        Player player = resolveActionPlayer(action.netID.get());
        Human loadedHuman = null;
        if (player.controlledUnit == null) {
            if (keepSavedCharactersOnServer == 0) {
                if (networkSessionActive != 0) {
                    loadedHuman = loadServerSavedCharacterForUploadRequest(action, player);
                }
            } else {
                loadedHuman = materializeSavedCharacter(action, player);
            }
            if (loadedHuman == null) {
                rejectOrFallbackInvalidUploadedCharacter(player);
            } else {
                attachMaterializedSavedCharacter(loadedHuman, player);
            }
            return;
        }

        CServerApp.netUpdate(
                (Unit) player.controlledUnit,
                player,
                ALL_UNIT_UPDATE_FLAGS,
                UNIT_EQUIPMENT_BROADCAST_MASK,
                0,
                0
        );
        CServerApp.sendCurrentServerLoopCounter(player);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0xBE` server-side file branch.
     * Fully ported support helper.
     */
    private Human loadServerSavedCharacterForUploadRequest(UploadCharacterFileAction action, Player player) {
        Path lockFlagPath = resolveServerSavedCharacterLockFlagPath(player);
        if (lockFlagPath != null && canOpenServerSavedCharacterLockFlag(lockFlagPath)) {
            return null;
        }

        Human loadedHuman = null;
        try {
            byte[] payload = Files.readAllBytes(resolveServerSavedCharacterPath(player));
            action.payloadSize.set(payload.length);
            action.data.set(payload);
            loadedHuman = materializeSavedCharacter(action, player);
        } catch (IOException ignored) {
            // Native CFile::Open failure leaves the loaded Human null and falls through to rejection/fallback.
        }
        if (lockFlagPath != null) {
            writeServerSavedCharacterLockFlag(lockFlagPath);
        }
        return loadedHuman;
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0xBE` lockflag path format.
     * Fully ported support helper.
     */
    private static Path resolveServerSavedCharacterLockFlagPath(Player player) {
        if (player.characterLockName.isEmpty()) {
            return null;
        }
        return Path.of(Globals.serverConfig.chrbase)
                .resolve("chr")
                .resolve(player.characterLockName)
                .resolve("lockflag");
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0xBE` lockflag CFile::Open.
     * Fully ported support helper.
     */
    private static boolean canOpenServerSavedCharacterLockFlag(Path lockFlagPath) {
        try (InputStream ignored = Files.newInputStream(lockFlagPath)) {
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0xBE` lockflag write.
     * Fully ported support helper.
     */
    private static void writeServerSavedCharacterLockFlag(Path lockFlagPath) {
        try {
            Files.writeString(
                    lockFlagPath,
                    serverStatusFilePath,
                    StandardCharsets.ISO_8859_1,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write saved-character lock flag", e);
        }
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0xBE` invalid-character branch.
     * Fully ported support helper.
     */
    private void rejectOrFallbackInvalidUploadedCharacter(Player player) {
        if (keepSavedCharactersOnServer == 0 && networkSessionActive != 0) {
            pushMessage("Characters created outside this server are not accepted");
            CServerApp.sendTwoDwordAction(
                    player,
                    GameActionId.TWO_DWORD_ACTION_0B,
                    SERVER_SAVED_CHARACTER_REJECTION_STATUS,
                    0
            );
            CServerApp.sendCurrentServerLoopCounter(player);
            player.markDisconnectedForRemoval();
            return;
        }

        pushMessage("Invalid character joined the game.");
        ensureControlledUnitForPlayer(player, 0x19, 0x19, 0x19, 0x19, 1, 1);
        CServerApp.sendCurrentServerLoopCounter(player);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0xBE` loaded-Human attach branch.
     * Fully ported support helper.
     */
    private void attachMaterializedSavedCharacter(Human loadedHuman, Player player) {
        loadedHuman.idFull = allocateNextFreeId() & 0xFFFF;
        loadedHuman.owner = player;
        player.controlledUnit = loadedHuman;
        player.ownedUnits.add(loadedHuman);

        UnitGroup unitGroup = new UnitGroup();
        player.unitGroups.add(unitGroup);
        unitGroup.addUnit(loadedHuman);

        CServerApp.netUpdate(
                loadedHuman,
                loadedHuman.owner,
                ALL_UNIT_UPDATE_FLAGS,
                UNIT_EQUIPMENT_BROADCAST_MASK,
                0,
                0
        );
        CServerApp.sendCurrentServerLoopCounter(loadedHuman.owner);
        CServerApp.sendPlayerKnowledgeAction(0, loadedHuman.owner);
        if (loadedHuman.savedCharacterKillHistoryMarker != 0) {
            CServerApp.sendGameEventNotification(CServerApp.CHEAT_NOTIFICATION_EVENT, (short) player.playerId, null);
            pushMessage(String.format(
                    "Cheating detected! Player: %s file: %s%s.a2c",
                    player.name,
                    Integer.toUnsignedString(player.characterSessionKeyPart1),
                    Integer.toUnsignedString(player.characterSessionKeyPart2)
            ));
        }
    }

    /**
     * Native: GameServer::LoadServerSavedCharacterForPlayer @004EA472.
     * Fully ported.
     */
    private Human loadServerSavedCharacterForPlayer(Player player) {
        Path characterFilePath = resolveServerSavedCharacterPath(player);
        try {
            ensureServerSavedCharacterDirectory(player, characterFilePath);
            long fileSize = Files.size(characterFilePath);
            if (fileSize >= SERVER_SAVED_CHARACTER_FILE_MAX_BYTES) {
                return null;
            }
            byte[] payload = Files.readAllBytes(characterFilePath);
            BinaryBlobAction action = BinaryBlobAction.global;
            action.payloadSize.set((int) fileSize);
            action.data.set(payload);
            return materializeSavedCharacter(action, player);
        } catch (IOException ignored) {
            // Native CFile::Open/GetLength/Read failure returns null from this helper.
            return null;
        }
    }

    /**
     * Native: GameServer::MaterializeSavedCharacter @004EA6AB.
     * Fully ported.
     */
    private Human materializeSavedCharacter(BinaryBlobAction action, Player player) {
        byte[][] sections = CGameSession.readSavedCharacterSections(action.data.get());
        if (sections == null
                || sections[CGameSession.SAVE_SECTION_CHARACTER_HEADER_INDEX] == null
                || sections[CGameSession.SAVE_SECTION_CHARACTER_STATS_INDEX] == null
                || sections[CGameSession.SAVE_SECTION_PRIMARY_ITEM_LIST_INDEX] == null) {
            return null;
        }

        byte[] headerSection = sections[CGameSession.SAVE_SECTION_CHARACTER_HEADER_INDEX];
        byte[] statsSection = sections[CGameSession.SAVE_SECTION_CHARACTER_STATS_INDEX];
        Human human = createSavedCharacterHuman(headerSection);
        applySavedCharacterStats(human, headerSection, statsSection);
        applySavedCharacterPlayerState(
                human,
                player,
                statsSection,
                sections[CGameSession.SAVE_SECTION_KNOWLEDGE_TABLE_INDEX]
        );
        applySavedCharacterItems(
                human,
                sections[CGameSession.SAVE_SECTION_PRIMARY_ITEM_LIST_INDEX],
                sections[CGameSession.SAVE_SECTION_SECONDARY_ITEM_LIST_INDEX]
        );
        resetMaterializedSavedCharacterRuntimeState(human, player);
        return human;
    }

    /**
     * Native support extracted from GameServer::MaterializeSavedCharacter @004EA6AB Start_* Human construction.
     * Fully ported support helper.
     */
    private static Human createSavedCharacterHuman(byte[] headerSection) {
        int sessionType = Byte.toUnsignedInt(headerSection[CGameSession.CHARACTER_HEADER_TYPE_OFFSET]);
        HumanId templateId = switch (sessionType & SESSION_TYPE_CLASS_AND_SEX_MASK) {
            case SESSION_TYPE_MAGE -> HumanId.START_MM;
            case SESSION_TYPE_FEMALE -> HumanId.START_FF;
            case SESSION_TYPE_CLASS_AND_SEX_MASK -> HumanId.START_FM;
            default -> HumanId.START_MF;
        };
        return Human.createFromTemplate(templateId.tableName, true, false);
    }

    /**
     * Native support extracted from GameServer::MaterializeSavedCharacter @004EA6AB stat and spellbook application.
     * Fully ported support helper.
     */
    private static void applySavedCharacterStats(Human human, byte[] headerSection, byte[] statsSection) {
        ByteBuffer stats = ByteBuffer.wrap(statsSection).order(ByteOrder.LITTLE_ENDIAN);
        human.m_nBody = Byte.toUnsignedInt(statsSection[CGameSession.CHARACTER_STATS_BODY_OFFSET]);
        human.m_nReaction = Byte.toUnsignedInt(statsSection[CGameSession.CHARACTER_STATS_REACTION_OFFSET]);
        human.m_nMind = Byte.toUnsignedInt(statsSection[CGameSession.CHARACTER_STATS_MIND_OFFSET]);
        human.m_nSpirit = Byte.toUnsignedInt(statsSection[CGameSession.CHARACTER_STATS_SPIRIT_OFFSET]);
        human.face = Byte.toUnsignedInt(headerSection[CGameSession.CHARACTER_HEADER_FACE_OFFSET]);
        for (int skillIndex = CGameSession.FIRST_SKILL_INDEX;
             skillIndex < CGameSession.SKILL_INDEX_LIMIT;
             skillIndex++) {
            int bonusPermille = stats.getInt(CGameSession.CHARACTER_STATS_SKILL_BONUS_OFFSET
                    + (skillIndex - CGameSession.FIRST_SKILL_INDEX) * Integer.BYTES);
            human.skillBonusesPermille.data[skillIndex] = bonusPermille;
            human.skillData.skillLevels[skillIndex] =
                    (short) SkillProgression.skillLevelForBonusPermille(bonusPermille);
        }
        human.skillBonusesPermille.data[0] =
                Byte.toUnsignedInt(headerSection[CGameSession.CHARACTER_HEADER_STARTING_SKILL_OFFSET]);
        for (int skillIndex = CGameSession.FIRST_SKILL_INDEX;
             skillIndex < CGameSession.SKILL_INDEX_LIMIT;
             skillIndex++) {
            human.skillDataSnapshot.skillLevels[skillIndex] = human.skillData.skillLevels[skillIndex];
        }
        human.skillsTotalBonusPermille = 0;
        for (int skillIndex = CGameSession.FIRST_SKILL_INDEX;
             skillIndex < CGameSession.SKILL_INDEX_LIMIT;
             skillIndex++) {
            human.skillsTotalBonusPermille += human.skillBonusesPermille.data[skillIndex];
        }
        human.recalculateDerivedStats();
        if (human.m_nMaxMP > 0) {
            applySavedCharacterSpellbook(human, statsSection);
        }
    }

    /**
     * Native support extracted from GameServer::MaterializeSavedCharacter @004EA6AB spellbook mask branch.
     * Fully ported support helper.
     */
    private static void applySavedCharacterSpellbook(Human human, byte[] statsSection) {
        ByteBuffer stats = ByteBuffer.wrap(statsSection).order(ByteOrder.LITTLE_ENDIAN);
        human.status |= Unit.UNIT_STATUS_CAN_CAST | Unit.UNIT_STATUS_MAGE_CLASS;
        human.spellbook = new Spellbook();
        int spellbookMask = stats.getInt(CGameSession.CHARACTER_STATS_AVAILABLE_SPELL_MASK_OFFSET);
        for (int spellId = SpellId.FIRE_ARROW.id; spellId < CStaticDataMgr.SPELL_LIMIT; spellId++) {
            if ((spellbookMask & (1 << (spellId & 0x1F))) != 0) {
                human.spellbook.setAt(spellId, new Spell((byte) spellId));
            }
        }
        human.missionRuntimeState.spellIndex =
                Byte.toUnsignedInt(statsSection[CGameSession.CHARACTER_STATS_AUTO_CAST_SPELL_ID_OFFSET]);
    }

    /**
     * Native support extracted from GameServer::MaterializeSavedCharacter @004EA6AB Player field application.
     * Fully ported support helper.
     */
    private static void applySavedCharacterPlayerState(
            Human human,
            Player player,
            byte[] statsSection,
            byte[] knowledgeSection
    ) {
        ByteBuffer stats = ByteBuffer.wrap(statsSection).order(ByteOrder.LITTLE_ENDIAN);
        human.str = player.name;
        player.gold = stats.getInt(CGameSession.CHARACTER_STATS_GOLD_OFFSET);
        player.creatureKillCount = stats.getInt(CGameSession.CHARACTER_STATS_MONSTERS_KILLED_OFFSET);
        player.deathCount = stats.getInt(CGameSession.CHARACTER_STATS_DEATH_COUNT_OFFSET);
        player.playerKillCount = stats.getInt(CGameSession.CHARACTER_STATS_PLAYERS_KILLED_OFFSET);
        player.fragCount = stats.getInt(CGameSession.CHARACTER_STATS_FRAGS_OFFSET);
        if (knowledgeSection != null) {
            System.arraycopy(knowledgeSection, 0, player.knowledgeTable, 0, player.knowledgeTable.length);
        }
    }

    /**
     * Native support extracted from GameServer::MaterializeSavedCharacter @004EA6AB item-list materialization.
     * Fully ported support helper.
     */
    private static void applySavedCharacterItems(
            Human human,
            byte[] primaryItemListSection,
            byte[] secondaryItemListSection
    ) {
        human.moveEquippedItemsToInventory();
        if (human.pWeapon != null) {
            human.inventory.addItem(human.releaseIncomingObject(human.pWeapon));
        }
        if (human.pShield != null) {
            human.inventory.addItem(human.releaseIncomingObject(human.pShield));
        }
        human.inventory = new Inventory();

        ByteBuffer primaryItems = savedCharacterItemPayloadCursor(primaryItemListSection);
        for (int slotIndex = 0; slotIndex < CGameSession.SAVED_CHARACTER_ITEM_SLOT_COUNT; slotIndex++) {
            Item item = Item.readSavedCharacterItemPayload(primaryItems);
            if (item.hash != 0) {
                human.addIncomingObjectToInventory(item);
            }
            markSavedCharacterMagicCapacityViolation(human, item);
        }
        if (secondaryItemListSection != null) {
            ByteBuffer secondaryItems = savedCharacterItemPayloadCursor(secondaryItemListSection);
            while (secondaryItems.position() < secondaryItemListSection.length) {
                Item item = Item.readSavedCharacterItemPayload(secondaryItems);
                if (item.hash != 0) {
                    human.inventory.addItem(item);
                }
                markSavedCharacterMagicCapacityViolation(human, item);
            }
        }
    }

    /**
     * Native support extracted from GameServer::MaterializeSavedCharacter @004EA6AB ItemListAction buffer cursor use.
     * Fully ported support helper.
     */
    private static ByteBuffer savedCharacterItemPayloadCursor(byte[] itemListSection) {
        ByteBuffer itemPayload = ByteBuffer.wrap(itemListSection).order(ByteOrder.LITTLE_ENDIAN);
        itemPayload.position(CGameSession.ITEM_LIST_SECTION_TRAILING_DATA_OFFSET);
        return itemPayload;
    }

    /**
     * Native support extracted from GameServer::MaterializeSavedCharacter @004EA6AB magic-capacity cheat marker branch.
     * Fully ported support helper.
     */
    private static void markSavedCharacterMagicCapacityViolation(Human human, Item item) {
        if (item.exceedsMagicCapacity != 0) {
            human.savedCharacterKillHistoryMarker = 1;
        }
    }

    /**
     * Native support extracted from GameServer::MaterializeSavedCharacter @004EA6AB final runtime-state reset block.
     * Fully ported support helper.
     */
    private static void resetMaterializedSavedCharacterRuntimeState(Human human, Player player) {
        human.movementState.resetToDefaults();
        human.missionRuntimeState.resetToDefaults();
        human.m_pTargetHandle.initDefault();
        human.actionTarget = null;
        human.spell = null;
        human.secondarySpell = null;
        human.pItem = null;
        human.lastDamageSource = null;
        human.resetActionStateToDying();
        human.m_nHP = human.m_nMaxHP;
        human.m_nMP = human.m_nMaxMP;
        human.respawning = 0;
        human.recalculateDerivedStats();
        human.status &= ~UNIT_STATUS_INACTIVE;
        human.word = 0;
        human.visiblePlayerMask = player.scanMask;
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x3F` and
     * FUN_00521083 @00521083.
     */
    private void handleRefreshShopShelvesRequest(@SuppressWarnings("unused") CGameAction action) {
        if (networkSessionActive == 0) {
            globalCampaignShop.rebuildShelvesFromOwnerData();
        }
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x49` and
     * GameServer::createUnitFromCommandText @004F89D1.
     */
    private void handleNamedCharacterSpawnRequest(CGameAction action) {
        if (networkSessionActive != 0) {
            return;
        }
        Player player = playerList.getPlayerById(action.netID.get());
        if (player == null || player.controlledUnit == null) {
            return;
        }
        ChatTextAction spawnAction = (ChatTextAction) action;
        int humanInfoIndex = spawnAction.firstPayloadDword.get();
        if (humanInfoIndex <= 0 || humanInfoIndex >= Globals.staticDataMgr.humans.size()) {
            return;
        }
        HumanInfo humanInfo = Globals.staticDataMgr.humans.get(humanInfoIndex);
        Unit spawnedUnit = createUnitFromCommandText(humanInfo.name, (Unit) player.controlledUnit, 1);
        if (spawnedUnit != null) {
            spawnedUnit.str = spawnAction.text.get();
            CServerApp.netUpdate(
                    spawnedUnit,
                    spawnedUnit.owner,
                    ALL_UNIT_UPDATE_FLAGS,
                    UNIT_EQUIPMENT_BROADCAST_MASK,
                    0,
                    0
            );
        }
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x48` and
     * GameServer::ensureControlledUnitForPlayer @004F254B.
     * Fully ported support helper.
     */
    private void handleCharacterSetupJoinRequest(CGameAction action) {
        if (!(action instanceof SubmitCharacterSetupAction setupAction)) {
            return;
        }
        Player player = resolveActionPlayer(action.netID.get());
        if (player == null) {
            return;
        }
        Unit controlledUnit = ensureControlledUnitFromSetup(player, setupAction);
        CServerApp.sendCurrentServerLoopCounter(controlledUnit.owner);
        if (shouldSaveControlledHumanoid(controlledUnit.owner)) {
            saveControlledHumanoid((Humanoid) controlledUnit);
        }
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x48` save throttle.
     */
    private static boolean shouldSaveControlledHumanoid(Player player) {
        int elapsed = Globals.currentTickMillis() - player.lastSaveTick;
        return Integer.compareUnsigned(elapsed, CONTROLLED_HUMANOID_SAVE_INTERVAL_MS) >= 0;
    }

    /**
     * Native: GameServer::sendInitialScenarioState @004F1D9C.
     * Fully ported.
     */
    private void sendInitialScenarioState(Player player, int shouldStageMissionEntry) {
        player.missionEntryDropCell = 0;
        CServerApp.sendLobbyPlayerInfoSnapshot(player);

        LoadScenarioAction loadScenarioAction = LoadScenarioAction.global;
        loadScenarioAction.text.set(mapFileName);
        loadScenarioAction.firstPayloadDword.set(mapNumber);
        loadScenarioAction.playerID.set(player.playerId);
        CServerApp.sendGameAction(loadScenarioAction);

        Unit unit = (Unit) player.controlledUnit;
        if (unit == null) {
            pushMessage("Client " + player.name + " tries to enter mission without Hero");
            CServerApp.sendServerChatText("You can't enter mission without Hero", player);
            return;
        }

        player.missionResultState = 0;
        if (networkSessionActive != 0 && shouldStageMissionEntry != 0) {
            applyMissionEntryRelationsFromSelf(player);
        }
        if (shouldStageMissionEntry != 0 && player.missionEntryStateSent == 0) {
            placeMissionEntryUnits(player);
        }
        player.missionEntryStateSent = 1;
        player.mapLoadPending = 1;

        missionScriptRuntime.advanceScenarioEntryCounter();
        prepareMissionEntryVisibilityForPlayer(player);
        CServerApp.sendDiplomacyStateSnapshot(player);

        SetCameraPositionAction setCameraPositionAction = SetCameraPositionAction.global;
        setCameraPositionAction.playerID.set(player.playerId);
        setCameraPositionAction.firstPayloadDword.set(unit.m_pTargetHandle.getX());
        setCameraPositionAction.secondPayloadDword.set(unit.m_pTargetHandle.getY());
        CServerApp.sendGameAction(setCameraPositionAction);
        CServerApp.netUpdate(unit, null, ALL_UNIT_UPDATE_FLAGS, 0x0FFB, 0, 0);
        CServerApp.sendInitialUnitAndBuildingSnapshotsForPlayer(player);
        CServerApp.sendInitialSackSnapshotsForPlayer(player);
        CServerApp.sendPlayerKnowledgeAction(0, player);
        CServerApp.sendUnitVisibilityAction(unit, false, null);
        if (objectLists.spellEffects != null) {
            for (SpellEffect spellEffect : objectLists.spellEffects) {
                if ((spellEffect.idFull & 0xFF) != 0 && spellEffect instanceof AreaEffect areaEffect) {
                    CServerApp.sendSpellEffectStateAction(areaEffect, 1);
                }
            }
        }
        if (networkSessionActive != 0) {
            CServerApp.sendQuestListAction(Globals.questStorage, player, false);
        }
        CServerApp.sendTileVisibilityMask(player);
        player.adjustGoldAndNotify(0, 1);
        CServerApp.sendNoPayloadAction(GameActionId.NO_PAYLOAD_ACTION_03, player);
        reportServerStatusToConfiguredTargets();
    }

    /**
     * Native support extracted from GameServer::FUN_004F1D9C @004F1D9C `param_2 != 0` network-session branch.
     * Fully ported.
     */
    private void applyMissionEntryRelationsFromSelf(Player player) {
        Player self = playerList.getByName(SELF_PLAYER_NAME);
        if (self != null) {
            for (int playerId = MISSION_ENTRY_RELATION_FIRST_COPY_ID;
                 playerId < MISSION_ENTRY_RELATION_COPY_LIMIT;
                 playerId++) {
                missionScriptRuntime.missionDiplomacyState.setRelationFlags(
                        playerId,
                        player.playerId,
                        missionScriptRuntime.missionDiplomacyState.relationFlags(playerId, self.playerId)
                );
                missionScriptRuntime.missionDiplomacyState.setRelationFlags(
                        player.playerId,
                        playerId,
                        missionScriptRuntime.missionDiplomacyState.relationFlags(self.playerId, playerId)
                );
            }
            missionScriptRuntime.missionDiplomacyState.setRelationFlags(
                    self.playerId,
                    player.playerId,
                    SELF_RELATION_FLAGS
            );
            for (Player activePlayer : playerList.players) {
                if (activePlayer.isActive == 0 && activePlayer.playerId != player.playerId) {
                    missionScriptRuntime.missionDiplomacyState.setRelationFlags(activePlayer.playerId, player.playerId, 0);
                    missionScriptRuntime.missionDiplomacyState.setRelationFlags(player.playerId, activePlayer.playerId, 0);
                    player.scanMaskMirror &= ~activePlayer.scanMask;
                    activePlayer.scanMaskMirror &= ~player.scanMask;
                }
            }
        }
    }

    /**
     * Native: GameServer::placeMissionEntryUnits @004F2C83.
     * Fully ported.
     */
    private void placeMissionEntryUnits(Player player) {
        ensureMissionEntryGroup(player);
        int entryCell = missionEntryCell(player);
        int entryX = entryCell & 0xFF;
        int entryY = (entryCell >>> 8) & 0xFF;
        if (entryX * entryY == 0) {
            entryX = Utils.randInclusive(MISSION_ENTRY_FALLBACK_CELL_RANDOM_MAX) + MISSION_ENTRY_FALLBACK_CELL_MIN;
            entryY = Utils.randInclusive(MISSION_ENTRY_FALLBACK_CELL_RANDOM_MAX) + MISSION_ENTRY_FALLBACK_CELL_MIN;
            pushMessage("Warning - no drop location in map, random position used");
        }

        int placementDiameter = Math.max(
                MISSION_ENTRY_MIN_PLACEMENT_DIAMETER,
                (int) Math.sqrt(player.ownedUnits.size()) + MISSION_ENTRY_PLACEMENT_DIAMETER_PADDING
        );
        for (Unit unit : player.ownedUnits) {
            if (worldMap.getGroundUnitAtTargetHandle(unit.m_pTargetHandle) == unit) {
                continue;
            }
            unit.m_pTargetHandle.initFromBytes(entryX, entryY, worldMap);
            boolean placed = false;
            if (unit == player.controlledUnit) {
                placed = unit.placeNearMissionCell(entryX, entryY, 0);
            }
            if (!placed) {
                placed = unit.placeNearMissionCell(entryX, entryY, placementDiameter);
            }
            if (!placed) {
                pushMessage("Error - can't place hero from previous mission");
            }
            activeUnits.add(unit);
            resetMissionEntryUnitScriptData(unit);
            unit.unitGroup.initializeScenarioMissionEntryGroup(missionScriptRuntime);
        }
    }

    /**
     * Native support extracted from GameServer::placeMissionEntryUnits @004F2C83 empty-group setup.
     * Fully ported support helper.
     */
    private UnitGroup ensureMissionEntryGroup(Player player) {
        if (player.unitGroups.isEmpty()) {
            pushMessage("Oops - player has no groups!!!");
            UnitGroup unitGroup = new UnitGroup();
            player.unitGroups.add(unitGroup);
            return unitGroup;
        }
        UnitGroup unitGroup = player.unitGroups.get(0);
        while (unitGroup.units.isEmpty() && player.unitGroups.size() > 1) {
            player.unitGroups.remove(unitGroup);
            unitGroup = player.unitGroups.get(0);
        }
        return unitGroup;
    }

    /**
     * Native support extracted from GameServer::placeMissionEntryUnits @004F2C83 drop-cell selection.
     * Fully ported support helper.
     */
    private int missionEntryCell(Player player) {
        if (networkSessionActive != 0 && player.missionEntryDropCell != 0) {
            return player.missionEntryDropCell;
        }
        if (!missionEntryDropCells.isEmpty()) {
            return missionEntryDropCells.get(Utils.randInclusive(missionEntryDropCells.size() - 1));
        }
        return 0;
    }

    /**
     * Native support extracted from GameServer::placeMissionEntryUnits @004F2C83 DATA_b4_180/DATA_b8_184 reset.
     * Fully ported support helper.
     */
    private static void resetMissionEntryUnitScriptData(Unit unit) {
        int spellIndex = unit.missionRuntimeState.spellIndex;
        unit.movementState.resetToDefaults();
        unit.missionRuntimeState.resetToDefaults();
        unit.missionRuntimeState.spellIndex = spellIndex;
    }

    /**
     * Native support extracted from GameServer::FUN_004F1D9C @004F1D9C after `Player +0x43` is set.
     */
    private void prepareMissionEntryVisibilityForPlayer(Player player) {
        worldMap.unitVisibilityState0x92ECC.rebuildUnitVisibilityState();
        activeUnits.clearVisibilityMaskForPlayer(player);
        objectLists.sacks.clearVisibilityMaskForPlayer(player);
        objectLists.corpses.clearVisibilityMaskForPlayer(player);
        for (Player sourcePlayer : playerList.players) {
            sourcePlayer.ownedUnits.clearVisibilityMaskForPlayer(player);
        }
    }

    /**
     * Native support boundary for RandomizeOnTimer called by GameServer::New @004E957B and
     * GameServer::LoadMapByName @004EB715.
     * Java's Utils RNG does not expose the native seed slot, so this preserves the call boundary without an invented
     * deterministic seed reset.
     */
    private static void randomizeOnTimer() {
    }

    /**
     * Native support extracted from GameServer::LoadMapByName @004EB715 object-container allocations.
     */
    private void resetMapObjectContainers() {
        cMapPtrToPtr.clear();
        objectLists.allocateMapObjectListsForMapLoad();
        activeUnits.clear();
        missionEntryDropCells.clear();
        scenarioDescriptor = null;
        worldMap = null;
        missionScriptRuntime = null;
        Globals.worldMap = null;
    }

    /**
     * Native: Global::CalculateChecksum @004EBC3E.
     * Fully ported.
     */
    private static int calculateChecksum(String fileName) {
        if (!Globals.gameFileManager.exists(fileName)) {
            return 0;
        }
        ByteBuffer file = Globals.gameFileManager.get(fileName).duplicate().order(ByteOrder.LITTLE_ENDIAN);
        int sum = 0;
        while (file.remaining() >= Integer.BYTES) {
            sum += file.getInt();
        }
        int tail = 0;
        for (int shift = 0; file.hasRemaining(); shift += 8) {
            tail |= (file.get() & 0xFF) << shift;
        }
        return sum + tail;
    }

    /**
     * Native support extracted from GameServer::LoadMapByName @004EB715 CPU usage field reset.
     */
    private void resetCpuUsageStats() {
        cpuUsageTenthPct = 0;
        cpuUsageTenthPctPeak = 0;
        cpuUsageTenthPctSum = 0;
        cpuUsageSampleCount = 0;
        cpuUsageTenthPctMax = 0;
    }

    /**
     * Native: Global::reportNoMapServerStatusAndDeleteStatusFile @004ED298.
     * Native WinInet/DeleteFile failures are ignored by the caller; Java mirrors that failure-tolerant behavior.
     * Fully ported.
     */
    private static void reportNoMapServerStatusAndDeleteStatusFile() {
        if (isServerStatusReportingProtocol()) {
            String serverStatusParameters = buildNoMapServerStatusParameters();
            for (String reportTarget : Globals.serverConfig.reporttowww) {
                reportToWebEndpoint(reportTarget, serverStatusParameters, true);
            }
        }
        if (!serverStatusFilePath.isEmpty()) {
            try {
                Files.deleteIfExists(Path.of(serverStatusFilePath));
            } catch (IOException e) {
                // Native DeleteFile failures are ignored by the caller.
            }
        }
    }

    /**
     * Native: GameServer::reportServerStatusToConfiguredTargets @004ED0B1.
     * Fully ported for native DirectPlay TCP/IP status reports; Java also reports for the raw TCP/IP replacement used
     * by the no-GL dedicated server path.
     */
    public void reportServerStatusToConfiguredTargets() {
        if (isServerStatusReportingProtocol()) {
            String serverStatusParameters = buildServerStatusParameters();
            for (String reportTarget : Globals.serverConfig.reporttowww) {
                reportToWebEndpoint(reportTarget, serverStatusParameters, true);
            }
        }
        if (!serverStatusFilePath.isEmpty()) {
            writeServerStatusFile();
        }
    }

    /**
     * Native support extracted from GameServer::reportServerStatusToConfiguredTargets @004ED0B1 and the Java raw
     * TCP/IP replacement path started by CMainWindow::startHatDedicatedServer @0048EF1F.
     */
    private static boolean isServerStatusReportingProtocol() {
        int protocolId = CLlDriver.getProtocolId();
        return protocolId == DIRECTPLAY_TCP_IP_PROTOCOL || protocolId == ProtocolId.TCP_IP;
    }

    /**
     * Fully ported native support extracted from GameServer::reportServerStatusToConfiguredTargets @004ED0B1
     * server-status format.
     */
    private String buildServerStatusParameters() {
        if (!mapName.isEmpty()) {
            return String.format(
                    Locale.ROOT,
                    "servername=%s&version=1.01&mapname=%s&mapsize=%dx%d&difficultylevel=%d&players=%d&ip=%s",
                    Globals.serverConfig.ServerName,
                    mapName,
                    worldMap.getMapWidth() - 0x10,
                    worldMap.getMapHeight() - 0x10,
                    difficultyLevel,
                    playerList.getPlayersCount(),
                    Globals.serverConfig.ipaddress
            );
        }
        return buildNoMapServerStatusParameters();
    }

    /**
     * Fully ported native support extracted from Global::reportNoMapServerStatusAndDeleteStatusFile @004ED298 and
     * GameServer::reportServerStatusToConfiguredTargets @004ED0B1 no-map server-status format.
     */
    private static String buildNoMapServerStatusParameters() {
        return String.format(
                Locale.ROOT,
                "servername=%s&version=1.01&mapname=no map&mapsize=0x0&difficultylevel=9&players=-1&ip=%s",
                Globals.serverConfig.ServerName,
                Globals.serverConfig.ipaddress
        );
    }

    /**
     * Native: Global::reportToSrvSendTargets @004E1AC9.
     * Fully ported.
     */
    private static boolean reportToSrvSendTargets(String urlParameters, boolean readResponse) {
        BufferedReader reader;
        try {
            reader = Files.newBufferedReader(Path.of("srv_send.txt"), StandardCharsets.ISO_8859_1);
        } catch (IOException e) {
            return false;
        }

        try (reader) {
            String reportTarget;
            while ((reportTarget = reader.readLine()) != null) {
                reportToWebEndpoint(reportTarget, urlParameters, readResponse);
            }
        } catch (IOException e) {
            return true;
        }
        return true;
    }

    /**
     * Native: Global::reportToWebEndpoint @004E174F.
     * Native WinInet failures return 0 and the caller ignores that result; Java mirrors that failure-tolerant behavior.
     * Fully ported.
     */
    private static boolean reportToWebEndpoint(String reportTarget, String urlParameters, boolean readResponse) {
        if (reportTarget == null || urlParameters == null) {
            return false;
        }
        String normalizedParameters = urlParameters.replace(' ', '+');
        String url = reportTarget + "?" + normalizedParameters;
        if (!url.regionMatches(true, 0, "http://", 0, "http://".length())) {
            url = "http://" + url;
        }
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("User-Agent", "RageOfMages2")
                    .header("Cache-Control", "no-cache")
                    .GET()
                    .build();
            HttpResponse<InputStream> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream responseBody = response.body()) {
                if (readResponse) {
                    responseBody.readNBytes(1000);
                }
            }
            return true;
        } catch (IOException | IllegalArgumentException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Fully ported native support extracted from GameServer::reportServerStatusToConfiguredTargets @004ED0B1
     * status-file write.
     * Native CStdioFile open/write failures do not affect the caller; Java mirrors that failure-tolerant behavior.
     */
    private void writeServerStatusFile() {
        try {
            Files.writeString(
                    Path.of(serverStatusFilePath),
                    mapName + playerList.getPlayersCount(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            // Native CStdioFile failures are ignored by the caller.
        }
    }

    /**
     * Native: GameServer::cleanupStaleCharacterLocks @004ED380.
     * Native CFileFind/CStdioFile/DeleteFile failures are ignored by the caller; Java mirrors that behavior.
     * Fully ported.
     */
    private void cleanupStaleCharacterLocks() {
        Path characterRoot = Path.of(Globals.serverConfig.chrbase).resolve("chr");
        if (!Files.isDirectory(characterRoot)) {
            return;
        }
        try (DirectoryStream<Path> characterEntries = Files.newDirectoryStream(characterRoot)) {
            for (Path characterEntry : characterEntries) {
                if (Files.isDirectory(characterEntry)) {
                    deleteStaleCharacterLock(characterEntry.resolve("lockflag"));
                }
            }
        } catch (IOException e) {
            // Native CFileFind failures are ignored by the caller.
        }
    }

    /**
     * Native support extracted from GameServer::cleanupStaleCharacterLocks @004ED380 lockflag read/delete branch.
     * Fully ported.
     */
    private static void deleteStaleCharacterLock(Path lockFlagPath) {
        if (!Files.exists(lockFlagPath)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(lockFlagPath, StandardCharsets.UTF_8)) {
            String lockOwner = reader.readLine();
            if (lockOwner == null) {
                lockOwner = "";
            }
            if (!lockOwner.equals(serverStatusFilePath)) {
                Files.deleteIfExists(lockFlagPath);
            }
        } catch (IOException e) {
            // Native CStdioFile/DeleteFile failures are ignored by the caller.
        }
    }

    /**
     * Native support extracted from GameServer::LoadMapByName @004EB715 g_Grid32x32_ServerRelated clear.
     */
    private static void clearServerGrid32x32() {
        for (int[] row : serverGrid32x32) {
            Arrays.fill(row, 0);
        }
    }

    /**
     * Native support extracted from CServerApp::sendDiplomacyStateSnapshot @00504E87
     * g_Grid32x32_ServerRelated comparison and pair write.
     */
    public boolean updateServerDiplomacyRelationFlags(
            int rowPlayerId,
            int columnPlayerId,
            int outgoingFlags,
            int incomingFlags
    ) {
        int packedRelationFlags = (outgoingFlags & 0xFF) | ((incomingFlags & 0xFF) << Byte.SIZE);
        boolean changed = serverGrid32x32[rowPlayerId][columnPlayerId] != packedRelationFlags;
        serverGrid32x32[rowPlayerId][columnPlayerId] = packedRelationFlags;
        serverGrid32x32[columnPlayerId][rowPlayerId] = (incomingFlags & 0xFF) | ((outgoingFlags & 0xFF) << Byte.SIZE);
        return changed;
    }

    /**
     * Native support extracted from GameServer::LoadMapByName @004EB715 map-number parsing.
     */
    private static int parseMapNumberFromAlmName(String mapName) {
        int almIndex = mapName.toLowerCase(Locale.ROOT).indexOf(MAP_EXTENSION);
        if (almIndex < 0) {
            return -1;
        }
        return Utils.atoiLike(mapName.substring(0, almIndex));
    }

    /**
     * Native: GameServer::Serialize @004EC732.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        if (ar.isStoring()) {
            serializeWrite(ar);
        } else {
            serializeRead(ar);
        }

        debugState.serialize(ar);
    }

    /**
     * Native support extracted from GameServer::Serialize @004EC732 read branch.
     * Fully ported.
     */
    private void serializeRead(CArchive ar) throws IOException {
        ioInit0();

        serverLoopCounter = ar.readInt();
        someValue = ar.readInt();
        mapFileName = ar.readCString();

        field174 = ar.readInt();
        field17C = ar.readInt();
        field180 = ar.readInt();
        field184 = ar.readInt();
        field188 = ar.readInt();
        field18C = ar.readInt();
        field190 = ar.readInt();
        field194 = ar.readInt();
        field1A0 = ar.readInt();
        field19C = ar.readInt();
        field198 = ar.readInt();
        mapNumber = ar.readInt();

        int maybeDifficulty = ar.readInt();
        if (maybeDifficulty >= 1 && maybeDifficulty <= 3) {
            difficultyLevelSetting = maybeDifficulty;
        }
        ar.serialize(playerList);
        serializeCorpses(ar);

        int hasExtendedBlock = ar.readByte() & 0xFF;
        if (hasExtendedBlock != 0) {
            preExtendedLoadCleanup();
            objectLists.ensureBuildingsForExtendedSaveRead();
            serializeBuildings(ar);
            objectLists.ensureSpellEffectsForExtendedSaveRead();
            serializeSpellEffects(ar);

            String scenarioPath = mapFileName;
            if (mapNumber != 0) {
                scenarioPath = Resources.path(SCENARIO, mapFileName);
            }
            createAndInstallWorldMapForScenario(scenarioPath);
            serializeWorldMap(ar);
            serializeMissionRuntime(ar);

            objectLists.ensureSacksForExtendedSaveRead();
            serializeSacks(ar);
            serializeVirtualCasters(ar);

            postWorldLoadRebind();
            worldLoaded = 1;
        } else {
            worldLoaded = 0;
        }

        if (hasExtendedBlock != 0) {
            postExtendedLoadInit();
        } else {
            clearPerPlayerTransientFlags();
        }

        // Native reads these unconditionally; current reverse indicates they are trailing sentinels.
        int marker1 = ar.readInt();
        int marker2 = ar.readInt();
        if (marker1 != SAVE_END_MARKER || marker2 != SAVE_END_MARKER) {
            pushMessage("Invalid save file.");
        }
    }

    /**
     * Native support extracted from GameServer::Serialize @004EC732 write branch.
     * Fully ported.
     */
    private void serializeWrite(CArchive ar) throws IOException {
        ar.writeInt(serverLoopCounter);
        ar.writeInt(someValue);
        ar.writeCString(mapFileName);

        ar.writeInt(field174);
        ar.writeInt(field17C);
        ar.writeInt(field180);
        ar.writeInt(field184);
        ar.writeInt(field188);
        ar.writeInt(field18C);
        ar.writeInt(field190);
        ar.writeInt(field194);
        ar.writeInt(field1A0);
        ar.writeInt(field19C);
        ar.writeInt(field198);
        ar.writeInt(mapNumber);
        ar.writeInt(difficultyLevelSetting);

        ar.serialize(playerList);
        serializeCorpses(ar);

        if (worldLoaded != 0) {
            ar.writeByte(1);
            serializeBuildings(ar);
            serializeSpellEffects(ar);
            serializeWorldMap(ar);
            serializeMissionRuntime(ar);
            serializeSacks(ar);
            serializeVirtualCasters(ar);
        } else {
            ar.writeByte(0);
        }

        ar.writeInt(SAVE_END_MARKER);
        ar.writeInt(SAVE_END_MARKER);
    }

    /**
     * Native: GameServer::encodePackedWideStream @00539E70. Fully ported.
     */
    static byte[] encodePackedWideStream(byte[] unpacked) {
        int[] words = littleEndianWords(unpacked);
        ByteArrayOutputStream output = new ByteArrayOutputStream(unpacked.length + Integer.BYTES);
        writeLittleEndianInt(output, words.length);
        int sourceIndex = 0;
        while (sourceIndex < words.length) {
            if (sourceIndex + 1 < words.length && words[sourceIndex] == words[sourceIndex + 1]) {
                sourceIndex += encodeRun(words, sourceIndex, output);
            } else {
                sourceIndex += encodeLiteral(words, sourceIndex, output);
            }
        }
        return output.toByteArray();
    }

    /**
     * Native: GameServer::encodeRun @0053A040. Fully ported.
     */
    private static int encodeRun(int[] words, int sourceIndex, ByteArrayOutputStream output) {
        int count = 1;
        int scanIndex = sourceIndex;
        while (scanIndex + 1 < words.length
                && words[scanIndex] == words[scanIndex + 1]
                && count < PACKED_STREAM_MAX_RUN_UNITS
                && sourceIndex + count < words.length) {
            scanIndex++;
            count++;
        }
        output.write(count | 0x80);
        writeLittleEndianShort(output, words[scanIndex]);
        return count;
    }

    /**
     * Native: GameServer::encodeLiteral @0053A110. Fully ported.
     */
    private static int encodeLiteral(int[] words, int sourceIndex, ByteArrayOutputStream output) {
        int marker = 1;
        int scanIndex = sourceIndex;
        while (scanIndex + 1 < words.length
                && words[scanIndex] != words[scanIndex + 1]
                && marker < PACKED_STREAM_MAX_RUN_UNITS
                && sourceIndex + marker < words.length) {
            scanIndex++;
            marker++;
        }
        if (sourceIndex + marker == words.length) {
            marker++;
        }

        int count = marker - 1;
        output.write(count);
        for (int i = 0; i < count; i++) {
            writeLittleEndianShort(output, words[sourceIndex + i]);
        }
        return count;
    }

    /**
     * Native support extracted from GameServer::encodePackedWideStream @00539E70 source-short interpretation.
     */
    private static int[] littleEndianWords(byte[] bytes) {
        int[] words = new int[bytes.length / Short.BYTES];
        for (int i = 0; i < words.length; i++) {
            int byteIndex = i * Short.BYTES;
            words[i] = Byte.toUnsignedInt(bytes[byteIndex])
                    | (Byte.toUnsignedInt(bytes[byteIndex + 1]) << Byte.SIZE);
        }
        return words;
    }

    /**
     * Native support extracted from GameServer::SaveGameFile @004E9816 and
     * GameServer::encodePackedWideStream @00539E70 little-endian dword writes.
     */
    private static void writeLittleEndianInt(ByteArrayOutputStream output, int value) {
        output.write(value);
        output.write(value >>> Byte.SIZE);
        output.write(value >>> (Byte.SIZE * 2));
        output.write(value >>> (Byte.SIZE * 3));
    }

    /**
     * Native support extracted from GameServer::encodeRun @0053A040 and GameServer::encodeLiteral @0053A110.
     */
    private static void writeLittleEndianShort(ByteArrayOutputStream output, int value) {
        output.write(value);
        output.write(value >>> Byte.SIZE);
    }

    /**
     * Native: GameServer::decodePackedWideStream @0053A330. Fully ported.
     */
    static DecodedSave decodePackedWideStream(byte[] packed) throws IOException {
        ByteBuffer src = ByteBuffer.wrap(packed).order(ByteOrder.LITTLE_ENDIAN);
        int utf16Units = src.getInt();
        byte[] out = new byte[utf16Units << 1];
        int srcPos = 4;
        int dstPos = 0;

        while (srcPos < packed.length) {
            int tag = packed[srcPos] & 0xFF;
            if ((tag & 0x80) != 0) {
                int consumed = decodeRun(packed, srcPos, out, dstPos);
                int copies = tag & 0x7F;
                dstPos += copies * 2;
                srcPos += consumed;
            } else {
                int consumed = decodeLiteral(packed, srcPos, out, dstPos);
                int units = tag;
                dstPos += units * 2;
                srcPos += consumed;
            }
        }

        return new DecodedSave(out, utf16Units);
    }

    /**
     * Native: GameServer::decodeLiteral @0053A450. Fully ported.
     */
    private static int decodeLiteral(byte[] src, int srcPos, byte[] dst, int dstPos) {
        int count = src[srcPos] & 0xFF;
        int bytesToCopy = count * 2;
        int payloadPos = srcPos + 1;

        System.arraycopy(src, payloadPos, dst, dstPos, bytesToCopy);
        return bytesToCopy + 1;
    }

    /**
     * Native: GameServer::decodeRun @0053A3D0. Fully ported.
     */
    private static int decodeRun(byte[] src, int srcPos, byte[] dst, int dstPos) {
        int count = src[srcPos] & 0x7F;
        int symbolPos = srcPos + 1;
        byte lo = src[symbolPos];
        byte hi = src[symbolPos + 1];

        int p = dstPos;
        for (int i = 0; i < count; i++) {
            dst[p++] = lo;
            dst[p++] = hi;
        }

        return 3;
    }

    // ---- Helper routines referenced by Serialize(0x004ec732). ----

    /**
     * Native: Global::__ioinit0 @004F8CC9.
     * Fully ported.
     */
    private void ioInit0() {
        Arrays.fill(gBitsMap, (byte) 0);
        writeBitsMapDword(0, 1);
    }

    /**
     * Native: Global::SetBitForId @004F8CEC.
     * Fully ported.
     */
    public void setBitForId(int param1) {
        int value = param1 & 0xFFFF;
        if (value < 0x6000) {
            int dwordIndex = value >>> 5;
            int bitMask = 1 << (value & 0x1F);
            int dword = readBitsMapDword(dwordIndex);
            writeBitsMapDword(dwordIndex, dword | bitMask);
        }
    }

    /**
     * Native: Global::ClearBitForId @004F8D3D.
     * Fully ported.
     */
    public void clearBitForId(int param1) {
        int value = param1 & 0xFFFF;
        if (value < 0x6000) {
            int dwordIndex = value >>> 5;
            int bitMask = 1 << (value & 0x1F);
            int dword = readBitsMapDword(dwordIndex);
            writeBitsMapDword(dwordIndex, dword & ~bitMask);
        }
    }

    /**
     * Native: Global::GetNextFreeID @004F8D90.
     * Fully ported.
     */
    public int allocateNextFreeId() {
        int dwordIndex = 0;
        while (readBitsMapDword(dwordIndex) == -1) {
            dwordIndex++;
        }

        int bitMask = 1;
        int bitIndex = 0;
        int dword = readBitsMapDword(dwordIndex);
        while ((dword & bitMask) != 0) {
            bitMask <<= 1;
            bitIndex++;
        }

        int id = ((dwordIndex << 5) + bitIndex) & 0xFFFF;
        setBitForId(id);
        return id;
    }

    /**
     * Native support extracted from Global::__ioinit0 @004F8CC9, Global::SetBitForId @004F8CEC,
     * Global::ClearBitForId @004F8D3D, and Global::GetNextFreeID @004F8D90.
     */
    private static int readBitsMapDword(int dwordIndex) {
        int byteIndex = dwordIndex << 2;
        return (gBitsMap[byteIndex] & 0xFF)
                | ((gBitsMap[byteIndex + 1] & 0xFF) << 8)
                | ((gBitsMap[byteIndex + 2] & 0xFF) << 16)
                | ((gBitsMap[byteIndex + 3] & 0xFF) << 24);
    }

    /**
     * Native support extracted from Global::__ioinit0 @004F8CC9, Global::SetBitForId @004F8CEC,
     * Global::ClearBitForId @004F8D3D, and Global::GetNextFreeID @004F8D90.
     */
    private static void writeBitsMapDword(int dwordIndex, int value) {
        int byteIndex = dwordIndex << 2;
        gBitsMap[byteIndex] = (byte) (value & 0xFF);
        gBitsMap[byteIndex + 1] = (byte) ((value >>> 8) & 0xFF);
        gBitsMap[byteIndex + 2] = (byte) ((value >>> 16) & 0xFF);
        gBitsMap[byteIndex + 3] = (byte) ((value >>> 24) & 0xFF);
    }

    // not ported.
    public void setPointerMapNullEntry() {
        cMapPtrToPtr.put(null, null);
    }

    // not ported.
    public void setPointerMapEntry(Object key, Object value) {
        cMapPtrToPtr.put(key, value);
    }

    // not ported.
    public boolean hasPointerMapKey(Object key) {
        return cMapPtrToPtr.containsKey(key);
    }

    // not ported.
    public Object lookupPointerMap(Object key) {
        return cMapPtrToPtr.get(key);
    }

    /**
     * Native support extracted from Global::RestorePointerContext @005465D0, Item::RestoreContext @00546590,
     * Spell::RestoreContext @00546550, Unit::RestoreContext @00546700, and restoreMappedPointer @005432C0.
     * Fully ported.
     * Java also accepts already-restored object references.
     */
    public Object lookupPointerMapOrNull(Object keyToken) {
        if (keyToken == null) {
            return null;
        }
        if (!(keyToken instanceof Number)) {
            return keyToken;
        }
        int key = ((Number) keyToken).intValue();
        if (key == 0 || !hasPointerMapKey(key)) {
            return null;
        }
        return lookupPointerMap(key);
    }

    /**
     * Resolve pointer-like token through this GameServer cMapPtrToPtr.
     * Keeps unresolved numeric token as-is for deferred restoration.
     * not ported.
     */
    public Object lookupPointerMapOrKeepToken(Object keyToken) {
        if (keyToken == null) {
            return null;
        }
        if (!(keyToken instanceof Number)) {
            return keyToken;
        }
        int key = ((Number) keyToken).intValue();
        if (key == 0) {
            return null;
        }
        if (!hasPointerMapKey(key)) {
            return key;
        }
        return lookupPointerMap(key);
    }

    /**
     * Native support extracted from GameServer::Serialize @004EC732; delegates to UnitList::Serialize @0052C500.
     */
    private void serializeCorpses(CArchive ar) throws IOException {
        ar.serialize(objectLists.corpses);
    }

    /**
     * Native support extracted from GameServer::Serialize @004EC732; delegates to BuildingList::Serialize wrapper
     * <p>
     * at 0052C3DA.
     */
    private void serializeBuildings(CArchive ar) throws IOException {
        ar.serialize(objectLists.buildings);
    }

    /**
     * Native support extracted from GameServer::Serialize @004EC732; delegates to SpellEffectList::Serialize.
     */
    private void serializeSpellEffects(CArchive ar) throws IOException {
        ar.serialize(objectLists.spellEffects);
    }

    /**
     * Native support extracted from GameServer::Serialize @004EC732; delegates to
     * CWorldMap::serialize @00550008.
     * Fully ported.
     */
    private void serializeWorldMap(CArchive ar) throws IOException {
        Globals.worldMap = worldMap;
        ar.serialize(worldMap);
    }

    /**
     * Native support extracted from GameServer::Serialize @004EC732; delegates to
     * MissionScriptRuntime::MissionScriptRuntime @00568C9A and MissionScriptRuntime::Serialize @0057468D.
     * Fully ported.
     */
    private void serializeMissionRuntime(CArchive ar) throws IOException {
        if (ar.isStoring()) {
            missionScriptRuntime.serialize(ar);
            return;
        }
        if (missionScriptRuntime == null) {
            missionScriptRuntime = new MissionScriptRuntime(worldMap, playerList);
            missionScriptRuntime.serialize(ar);
        }
    }

    /**
     * Native support extracted from GameServer::Serialize @004EC732; delegates to SackList::serialize @0052D5B2.
     */
    private void serializeSacks(CArchive ar) throws IOException {
        ar.serialize(objectLists.sacks);
    }

    /**
     * Native support extracted from GameServer::Serialize @004EC732; delegates to VirtualCasterList::Serialize.
     */
    private void serializeVirtualCasters(CArchive ar) throws IOException {
        ar.serialize(objectLists.virtualCasters);
    }

    /**
     * Native support extracted from GameServer::Serialize @004EC732 extended-load active-unit rebuild.
     * Fully ported.
     */
    private void preExtendedLoadCleanup() {
        activeUnits.clear();
        for (Player player : playerList.players) {
            if (player.ownedUnits != null) {
                for (Unit unit : player.ownedUnits) {
                    if ((unit.status & 0x08) == 0) {
                        activeUnits.add(unit);
                    }
                }
            }
        }
    }

    /**
     * Native support extracted from GameServer::Serialize @004EC732 scenario/world-map creation.
     * Fully ported.
     */
    private void createAndInstallWorldMapForScenario(String scenarioPath) {
        scenarioDescriptor = new ScenarioDescriptor(scenarioPath);
        worldMap = new CWorldMap(scenarioDescriptor, activeUnits);
        Globals.worldMap = worldMap;
    }

    /**
     * Native support extracted from GameServer::Serialize @004EC732; delegates to
     * ScenarioMapLoader::materializeScenarioScriptRuntime @00562745 with initializeLoadedGroups = 0.
     * Fully ported.
     */
    public void postWorldLoadRebind() {
        ScenarioMapLoader.materializeScenarioScriptRuntime(scenarioDescriptor, 0);
    }

    /**
     * Native support extracted from GameServer::Serialize @004EC732 extended-load context restore.
     * Fully ported.
     */
    private void postExtendedLoadInit() {
        activeUnits.restoreContext();
        objectLists.corpses.restoreContext();
        worldMap.restoreContext();
        missionScriptRuntime.bindMissionDiplomacyStateOwner();
        objectLists.spellEffects.restoreContext();
    }

    /**
     * Native support extracted from GameServer::Serialize @004EC732 no-extended-load pointer cleanup.
     * Fully ported.
     */
    private void clearPerPlayerTransientFlags() {
        for (Player player : playerList.players) {
            if (player.ownedUnits != null) {
                for (Unit unit : player.ownedUnits) {
                    unit.lastDamageSource = null;
                    unit.secondarySpell = null;
                    unit.actionTarget = null;
                }
            }
        }
    }

    /**
     * Native: Global::PushMessage @0043A0A8.
     * Fully ported. Native only emits pushed messages while the main window is in dedicated-server mode; Java also
     * mirrors those messages to the optional headless console sink.
     */
    public void pushMessage(String msg) {
        if (Globals.mainWindow.sessionMode != CMainWindow.SESSION_MODE_DEDICATED_SERVER) {
            return;
        }
        DedicatedServerConsoleSink.emitServerMessage(msg);
        logMessage(msg);
    }

    /**
     * Native: Global::logMsg @00439EE7.
     * Fully ported. Posts the console message and appends the optional configured dedicated-server log file.
     */
    private void logMessage(String msg) {
        Globals.mainWindow.postMessage(MessageCodes.APPEND_DEDICATED_SERVER_LOG_LINE, msg, 0);
        if (!Globals.serverConfig.logfile.isEmpty()) {
            appendServerLogMessage(msg);
        }
    }

    /**
     * Native support extracted from Global::logMsg @00439EE7.
     * Fully ported. `CStdioFile::Open` failure skips file output; write failures after a successful open stay fail-fast.
     */
    private static void appendServerLogMessage(String msg) {
        BufferedWriter logFile;
        try {
            logFile = Files.newBufferedWriter(
                    Path.of(Globals.serverConfig.logfile),
                    StandardCharsets.ISO_8859_1,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException ignored) {
            return;
        }

        try (logFile) {
            logFile.write(SERVER_LOG_TIMESTAMP_FORMAT.format(LocalDateTime.now()));
            logFile.write(msg);
            logFile.write('\n');
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Native: Global::logServerTurnPlayerCount @0043A128.
     * Fully ported.
     */
    public void logServerTurnPlayerCount(int turn, int playersOnline) {
        logMessage(String.format(Locale.ROOT, "Turn %d. %d players online", turn, playersOnline));
    }

    /**
     * Fully ported. Native: Global::IssueWarning @00560613.
     */
    public void issueWarning(String text) {
        String message = "WARNING! : " + text;
        if (networkSessionActive == 0) {
            CServerApp.sendServerChatText(message, null);
        } else {
            pushMessage(message);
        }
    }

    /**
     * Native: GameServer::Save @004E9E97.
     * Fully ported.
     */
    public void saveControlledHumanoid(Humanoid humanoid) {
        if (keepSavedCharactersOnServer != 0 || networkSessionActive == 0) {
            return;
        }

        Player player = humanoid.owner;
        player.lastSaveTick = Globals.currentTickMillis();
        Path characterFilePath = resolveServerSavedCharacterPath(player);
        try {
            ensureServerSavedCharacterDirectory(player, characterFilePath);
            CGameSession.writeEncryptedSaveSections(
                    characterFilePath,
                    packServerSavedCharacterHeader(humanoid, player),
                    packServerSavedCharacterStats(humanoid, player),
                    player.knowledgeTable,
                    ItemListAction.prepareForRuntimeUnitEquipmentSnapshot(humanoid).packSavedCharacterItemListSection(),
                    packServerSavedCharacterInventoryItems(humanoid).packSavedCharacterItemListSection(),
                    null
            );
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to save controlled humanoid", e);
        }
    }

    /**
     * Native support extracted from GameServer::Save @004E9E97 `%u%u.a2c` CString::Format calls.
     */
    private static String formatServerSavedCharacterFileName(Player player) {
        return Integer.toUnsignedString(player.characterSessionKeyPart1)
                + Integer.toUnsignedString(player.characterSessionKeyPart2)
                + ".a2c";
    }

    /**
     * Native support extracted from GameServer::Save @004E9E97 character path selection.
     */
    private static Path resolveServerSavedCharacterPath(Player player) {
        String fileName = formatServerSavedCharacterFileName(player);
        if (player.characterLockName.isEmpty()) {
            return Path.of(fileName);
        }
        return Path.of(Globals.serverConfig.chrbase)
                .resolve("chr")
                .resolve(player.characterLockName)
                .resolve(fileName);
    }

    /**
     * Native support extracted from GameServer::Save @004E9E97 locked-character directory creation branch.
     */
    private static void ensureServerSavedCharacterDirectory(Player player, Path characterFilePath) throws IOException {
        if (player.characterLockName.isEmpty() || Files.exists(characterFilePath)) {
            return;
        }
        Files.createDirectories(characterFilePath.getParent());
    }

    /**
     * Native support extracted from GameServer::Save @004E9E97 saved-character header section construction.
     */
    private static byte[] packServerSavedCharacterHeader(Humanoid humanoid, Player player) {
        int type = (humanoid.isFemale() ? 0x80 : 0) | (humanoid.isMageClass() ? 0x40 : 0);
        return CGameSession.packSavedCharacterHeaderSection(
                player.characterSessionKeyPart1,
                player.characterSessionKeyPart2,
                0,
                player.name,
                type,
                humanoid.face,
                humanoid.skillBonusesPermille.data[0],
                4,
                player.colorSlot
        );
    }

    /**
     * Native support extracted from GameServer::Save @004E9E97 saved-character stats section construction.
     */
    private static byte[] packServerSavedCharacterStats(Humanoid humanoid, Player player) {
        int spellbookMask = humanoid.spellbook == null ? 0 : humanoid.spellbook.getSpellbookMask();
        int autoCastSpellId = humanoid.missionRuntimeState == null ? 0 : humanoid.missionRuntimeState.spellIndex;
        return CGameSession.packSavedCharacterStatsSection(
                player.creatureKillCount,
                player.playerKillCount,
                player.fragCount,
                player.deathCount,
                player.gold,
                humanoid.m_nBody - humanoid.mModifiers.body,
                humanoid.m_nReaction - humanoid.mModifiers.reaction,
                humanoid.m_nMind - humanoid.mModifiers.mind,
                humanoid.m_nSpirit - humanoid.mModifiers.spirit,
                spellbookMask,
                autoCastSpellId,
                new int[]{
                        humanoid.skillBonusesPermille.data[1],
                        humanoid.skillBonusesPermille.data[2],
                        humanoid.skillBonusesPermille.data[3],
                        humanoid.skillBonusesPermille.data[4],
                        humanoid.skillBonusesPermille.data[5]
                }
        );
    }

    /**
     * Native support extracted from GameServer::Save @004E9E97 secondary inventory item-list section construction.
     */
    private static ItemListAction packServerSavedCharacterInventoryItems(Humanoid humanoid) {
        ItemListAction inventoryItems = new ItemListAction();
        for (Item item : humanoid.inventory.items) {
            item.appendNetworkItemPayload(inventoryItems, false);
            inventoryItems.itemCount.set(inventoryItems.itemCount.get() + 1);
        }
        return inventoryItems;
    }

    /**
     * Native: GameServer::handleChatCommand @004F3D68.
     * Fully ported.
     */
    public void handleChatCommand(Player player, String message) {
        if (networkSessionActive != 0) {
            handleNetworkChatCommand(player, message);
            return;
        }
        handleLocalCheatCommand(player, message);
    }

    /**
     * Native support extracted from GameServer::handleChatCommand @004F3D68.
     */
    private void handleNetworkChatCommand(Player player, String message) {
        if (isDirectDeliveryAdminClient(player)) {
            handleDirectDeliveryNetworkCommand(player, message);
            return;
        }
        handleRemoteNetworkCommand(player, message);
    }

    /**
     * Native support extracted from GameServer::handleChatCommand @004F3D68.
     */
    private boolean isDirectDeliveryAdminClient(Player player) {
        CBufferManager client = CServerApp.getLocalClientByNetId(player.playerId);
        return client != null && client.IsDirectDelivery();
    }

    /**
     * Native support extracted from GameServer::handleChatCommand @004F3D68.
     */
    private void handleRemoteNetworkCommand(Player player, String message) {
        if (message.startsWith("#set latency ")) {
            int latencyMillis = parseLeadingIntOrZero(commandTail(message, "#set latency ".length()));
            if (latencyMillis != 0 && (latencyMillis < MIN_LATENCY_MILLIS || latencyMillis > MAX_LATENCY_MILLIS)) {
                sendCommandEvent(COMMAND_FAILED_EVENT, (short) player.playerId, player);
                return;
            }
            applyClientLatencySetting(player, latencyMillis);
            return;
        }
        if (message.startsWith("#show latency")) {
            sendLatencyStatusMessage(player);
        }
    }

    /**
     * Native support extracted from GameServer::handleChatCommand @004F3D68.
     */
    private void applyClientLatencySetting(Player player, int latencyMillis) {
        CBufferManager client = CServerApp.getLocalClientByNetId(player.playerId);
        if (client != null) {
            CLlDriver.setClientSendIntervalMs(client.GetIPAddress(), latencyMillis);
            CServerApp.sendLatencySetting(latencyMillis, player);
        }
    }

    /**
     * Native support extracted from GameServer::handleChatCommand @004F3D68.
     */
    private void sendLatencyStatusMessage(Player player) {
        CBufferManager client = CServerApp.getLocalClientByNetId(player.playerId);
        if (client == null) {
            return;
        }
        int socketId = client.GetIPAddress();
        int latencyMillis = CLlDriver.getClientSendIntervalMs(socketId);
        long retransmitRate = CLlDriver.getClientRetransmitRate(socketId);
        CServerApp.sendServerChatText(String.format(
                "%s: latency %dms, packet loss %d.%03d%%",
                player.name,
                latencyMillis,
                retransmitRate / 1000,
                retransmitRate % 1000
        ), player);
    }

    /**
     * Native support extracted from GameServer::handleChatCommand @004F3D68.
     */
    private void handleDirectDeliveryNetworkCommand(Player player, String message) {
        if (message.startsWith("#kick ")) {
            Player target = playerList.getByName(commandTail(message, "#kick ".length()));
            CBufferManager targetClient = target == null ? null : CServerApp.getLocalClientByNetId(target.playerId);
            if (targetClient != null && !targetClient.IsDirectDelivery()) {
                CServerApp.broadcastPlayerKickedAction(target);
            }
            return;
        }
        if (message.startsWith("#locate ")) {
            Player target = playerList.getByName(commandTail(message, "#locate ".length()));
            CBufferManager targetClient = target == null ? null : CServerApp.getLocalClientByNetId(target.playerId);
            if (targetClient != null && target.isActive == 0) {
                Unit unit = (Unit) target.controlledUnit;
                CServerApp.sendServerChatText(
                        target.name + ": " + unit.m_pTargetHandle.getX() + ", " + unit.m_pTargetHandle.getY(),
                        player
                );
            }
        }
    }

    /**
     * Native support extracted from GameServer::handleChatCommand @004F3D68.
     */
    private void handleLocalCheatCommand(Player player, String message) {
        if (!player.isCheatCommandEnabled()) {
            if (cheats.matchCheatCommandPrefix(message) == 1) {
                String text = "Player " + player.name + " enable cheating.";
                pushMessage(text);
                player.setCheatCommandFlag(CHEAT_COMMAND_FLAG_ENABLED);
                sendCommandEvent(CServerApp.CHEAT_NOTIFICATION_EVENT, (short) player.playerId, null);
            }
            return;
        }
        if (message.startsWith("#create ")) {
            handleCreateCommand(player, commandTail(message, "#create ".length()));
            return;
        }
        if (message.startsWith("#modify ")) {
            handleModifyCommand(player, commandTail(message, "#modify ".length()));
            return;
        }
        if (message.startsWith("#summon ")) {
            handleSummonCommand(player, commandTail(message, "#summon ".length()));
            return;
        }
        if (message.startsWith("#killall") || message.startsWith("#kill all")) {
            handleKillAllCommand(player);
            return;
        }
        if (message.startsWith("#kill cheaters")) {
            handleKillCheatersCommand(player);
            return;
        }
        if (message.startsWith("#kill ")) {
            handleKillPlayerCommand(commandTail(message, "#kill ".length()));
            return;
        }
        if (message.startsWith("#pickup all")) {
            handlePickupAllCommand(player);
            return;
        }
        if (message.startsWith("#show map")) {
            sendMapCommand(player, MAP_VISIBILITY_SHOWN);
            return;
        }
        if (message.startsWith("#hide map")) {
            sendMapCommand(player, MAP_VISIBILITY_HIDDEN);
            return;
        }
        if (message.startsWith("#victory")) {
            sendMapCommand(player, MAP_VICTORY);
            return;
        }
        if (message.startsWith("#event ")) {
            int eventId = parseLeadingIntOrZero(commandTail(message, "#event ".length()));
            CServerApp.sendQuestObjectivesQueryOpen(player, eventId, 0);
        }
    }

    /**
     * Native support extracted from GameServer::handleChatCommand @004F3D68.
     */
    private void handleCreateCommand(Player player, String commandText) {
        Unit unit = (Unit) player.controlledUnit;
        if (unit.respawning != 0) {
            sendCommandEvent(COMMAND_FAILED_EVENT, (short) player.playerId, null);
            return;
        }
        StringBuilder tail = new StringBuilder(commandText);
        int count = parseCommandRepeatCount(tail);
        if ("Gold".equalsIgnoreCase(tail.toString())) {
            player.adjustGoldAndNotify(count, 0);
            sendCommandEvent(COMMAND_SUCCEEDED_EVENT, (short) player.playerId, null);
            return;
        }
        Item item = createItemFromCommandText(tail.toString());
        if (item == null || !item.isShapeMaterialCombinationAllowed()) {
            sendCommandEvent(COMMAND_FAILED_EVENT, (short) player.playerId, null);
            return;
        }
        item.count = count & 0xFFFF;
        unit.inventory.addItem(item);
        unit.refreshEncumbrance(0);
        CServerApp.netUpdate(unit, player, ALL_UNIT_UPDATE_FLAGS, UNIT_EQUIPMENT_BROADCAST_MASK, 0, 0);
        sendCommandEvent(COMMAND_SUCCEEDED_EVENT, (short) player.playerId, null);
    }

    /**
     * Native: GameServer::createItemFromCommandText @004FBA5F.
     * Fully ported.
     */
    private Item createItemFromCommandText(String commandText) {
        Item item = Armor.createByServiceName(commandText);
        if (item == null) {
            item = Weapon.createByServiceName(commandText);
        }
        if (item == null) {
            item = Shield.createByServiceName(commandText);
        }
        if (item == null) {
            item = createMagicItemFromCommandText(commandText);
        }
        if (item == null) {
            pushMessage("Invalid item " + commandText);
        }
        return item;
    }

    /**
     * Native support extracted from createItemFromCommandText @004FBA5F and ParseStructuredLine @004FA6C6.
     */
    private static MagicItem createMagicItemFromCommandText(String commandText) {
        StringBuilder itemName = new StringBuilder(Item.extractStructuredItemKey(commandText));
        Globals.staticDataMgr.findShapeID(itemName.toString(), itemName);
        Globals.staticDataMgr.findMaterialID(itemName.toString(), itemName);
        int magicItemIndex = findCommandMagicItemIndexByName(itemName.toString());
        if (magicItemIndex == 0) {
            return null;
        }

        MagicItem item = new MagicItem(COMMAND_MAGIC_ITEM_SLOT, magicItemIndex);
        String effectsText = Item.extractStructuredItemValue(commandText);
        if (!effectsText.isEmpty()) {
            if (!item.effects.isEmpty()) {
                item.effects.clear();
            }
            item.parseEffects(effectsText);
        }
        return item;
    }

    /**
     * Native support extracted from GameServer::createItemFromCommandText @004FBA5F magic item fallback scan.
     * Fully ported.
     */
    private static int findCommandMagicItemIndexByName(String itemName) {
        for (int index = Globals.staticDataMgr.magicItems.size() - 1; index >= 1; index--) {
            MagicalItemInfo magicItemInfo = Globals.staticDataMgr.magicItems.get(index);
            if (itemName.equals(magicItemInfo.name)) {
                return index;
            }
        }
        return 0;
    }

    /**
     * Native support extracted from GameServer::handleChatCommand @004F3D68.
     */
    private void handleModifyCommand(Player player, String commandText) {
        int targetMode;
        String tail;
        if (commandText.startsWith("self")) {
            targetMode = 1;
            tail = commandTail(commandText, "self".length());
        } else if (commandText.startsWith("army")) {
            targetMode = 2;
            tail = commandTail(commandText, "army".length());
        } else {
            return;
        }
        if (tail.startsWith("+god")) {
            applyGodModeCommand(player, targetMode);
            sendCommandEvent(COMMAND_SUCCEEDED_EVENT, (short) player.playerId, null);
        } else if (tail.startsWith("+spell ")) {
            handleSingleSpellCommand(player, tail, targetMode);
        } else if (tail.startsWith("+spells") && targetMode == 1) {
            if (grantAllSpells((Unit) player.controlledUnit)) {
                sendCommandEvent(COMMAND_SUCCEEDED_EVENT, (short) player.playerId, null);
            }
        }
        if (tail.startsWith("+knowledge")) {
            CServerApp.sendPlayerKnowledgeAction(0, player);
        }
    }

    /**
     * Native support extracted from GameServer::handleChatCommand @004F3D68.
     */
    private void applyGodModeCommand(Player player, int targetMode) {
        if (targetMode == 1) {
            Unit unit = (Unit) player.controlledUnit;
            unit.applyGodModeCheat();
            CServerApp.netUpdate(unit, null, ALL_UNIT_UPDATE_FLAGS, 0x0FFB, 0, 0);
            return;
        }
        for (Unit unit : player.ownedUnits) {
            if (unit != null) {
                unit.applyGodModeCheat();
                CServerApp.netUpdate(unit, null, ALL_UNIT_UPDATE_FLAGS, 0x0FFB, 0, 0);
            }
        }
    }

    /**
     * Native support extracted from GameServer::handleChatCommand @004F3D68.
     */
    private void handleSingleSpellCommand(Player player, String commandText, int targetMode) {
        if (targetMode != 1) {
            return;
        }
        Unit unit = (Unit) player.controlledUnit;
        if (unit.spellbook == null) {
            return;
        }
        int spellId = parseLeadingIntOrZero(commandTail(commandText, "+spell ".length()));
        if (spellId > 0 && spellId < Globals.staticDataMgr.spells.size()) {
            unit.spellbook.setAt(spellId, new Spell((byte) spellId));
        }
        CServerApp.netUpdate(unit, unit.owner, ALL_UNIT_UPDATE_FLAGS, 0x0FFB, 0, 0);
        sendCommandEvent(COMMAND_SUCCEEDED_EVENT, (short) player.playerId, null);
    }

    /**
     * Native support extracted from GameServer::handleChatCommand @004F3D68.
     */
    private boolean grantAllSpells(Unit unit) {
        if (unit.spellbook == null) {
            return false;
        }
        for (int spellId = 1; spellId < CStaticDataMgr.SPELL_LIMIT; spellId++) {
            unit.spellbook.setAt(spellId, new Spell((byte) spellId));
        }
        CServerApp.netUpdate(unit, unit.owner, ALL_UNIT_UPDATE_FLAGS, 0x0FFB, 0, 0);
        return true;
    }

    /**
     * Native support extracted from GameServer::handleChatCommand @004F3D68 and
     * GameServer::createUnitFromCommandText @004F89D1 call sites.
     */
    private void handleSummonCommand(Player player, String commandText) {
        if (player.controlledUnit == null) {
            return;
        }
        StringBuilder tail = new StringBuilder(commandText);
        int count = parseCommandRepeatCount(tail);
        int createHero = 0;
        if (tail.toString().startsWith("hero")) {
            count = 1;
            createHero = 1;
            String unitName = commandTail(tail.toString(), "hero".length());
            tail.setLength(0);
            tail.append(unitName);
        }
        for (int index = 0; index < count; index++) {
            createUnitFromCommandText(tail.toString(), (Unit) player.controlledUnit, createHero);
        }
    }

    /**
     * Native: GameServer::createUnitFromCommandText @004F89D1.
     * Fully ported.
     */
    private Unit createUnitFromCommandText(String unitName, Unit nearUnit, int createHero) {
        Unit unit = createCommandSpawnUnit(unitName, createHero);
        if (unit.getTokenTypeId() == 0) {
            return null;
        }

        if (worldLoaded == 0) {
            unit.idFull = allocateNextFreeId() & 0xFFFF;
        } else {
            if (!unit.placeNearMissionCell(
                    nearUnit.m_pTargetHandle.getX(),
                    nearUnit.m_pTargetHandle.getY(),
                    COMMAND_SPAWN_DIAMETER
            )) {
                return null;
            }
            activeUnits.addAndAssignRuntimeId(unit);
        }

        Player owner = nearUnit.owner;
        unit.owner = owner;
        owner.ownedUnits.add(unit);

        UnitGroup unitGroup = new UnitGroup();
        owner.unitGroups.add(unitGroup);
        unitGroup.addUnit(unit);

        unit.initializeScenarioMissionEntryUnit(missionScriptRuntime);
        unitGroup.initializeScenarioMissionEntryGroup(missionScriptRuntime);
        CServerApp.netUpdate(
                unit,
                worldLoaded == 0 ? owner : null,
                ALL_UNIT_UPDATE_FLAGS,
                UNIT_EQUIPMENT_BROADCAST_MASK,
                0,
                0
        );
        return unit;
    }

    /**
     * Native support extracted from GameServer::createUnitFromCommandText @004F89D1 Unit::Unit(CString*) and
     * Human::Human fallback calls.
     * Fully ported support helper.
     */
    private Unit createCommandSpawnUnit(String unitName, int createHero) {
        Unit unit = Unit.createFromTemplateName(unitName);
        if (unit.getTokenTypeId() != 0) {
            return unit;
        }
        return Human.createFromTemplate(unitName, createHero != 0, false);
    }

    /**
     * Native support extracted from GameServer::handleChatCommand @004F3D68.
     */
    private void handleKillAllCommand(Player player) {
        for (Player target : playerList.players) {
            if (target != null && missionScriptRuntime.hasRelationFlag(target, player, PLAYER_RELATION_HOSTILE_MASK)) {
                target.killOwnedUnits();
            }
        }
        sendCommandEvent(COMMAND_SUCCEEDED_EVENT, (short) player.playerId, null);
    }

    /**
     * Native support extracted from GameServer::handleChatCommand @004F3D68.
     */
    private void handleKillCheatersCommand(Player player) {
        for (Player target : playerList.players) {
            if (target != null && target != player && target.isCheatCommandEnabled()) {
                target.setCheatCommandFlag(CHEAT_COMMAND_FLAG_DISABLED);
                target.killOwnedUnits();
            }
        }
    }

    /**
     * Native support extracted from GameServer::handleChatCommand @004F3D68.
     */
    private void handleKillPlayerCommand(String playerName) {
        Player target = playerList.getByName(playerName);
        if (target != null) {
            target.killOwnedUnits();
            sendCommandEvent(COMMAND_SUCCEEDED_EVENT, (short) target.playerId, null);
        }
    }

    /**
     * Native support extracted from GameServer::handleChatCommand @004F3D68 and Unit::pickupSackContents @005108FA.
     */
    private void handlePickupAllCommand(Player player) {
        if (player.controlledUnit == null) {
            return;
        }
        Unit unit = (Unit) player.controlledUnit;
        for (Sack sack : new ArrayList<>(objectLists.sacks.sacks)) {
            if (sack != null) {
                Globals.worldMap.detachSack(sack);
                objectLists.sacks.remove(sack);
                unit.pickupSackContents(sack);
            }
        }
        sendCommandEvent(COMMAND_SUCCEEDED_EVENT, (short) player.playerId, null);
        pushMessage("All sacks picked up");
    }

    /**
     * Native support extracted from GameServer::handleChatCommand @004F3D68.
     */
    private void sendMapCommand(Player player, int command) {
        CServerApp.sendTwoDwordAction(player, GameActionId.TWO_DWORD_ACTION_AA, command, 0);
        if (command != MAP_VICTORY) {
            sendCommandEvent(COMMAND_SUCCEEDED_EVENT, (short) player.playerId, null);
        }
    }

    /**
     * Native support extracted from GameServer::handleChatCommand @004F3D68 and
     * CServerApp::sendGameEventNotification @005052D2.
     */
    private static void sendCommandEvent(int eventKind, int eventValue, Player recipient) {
        CServerApp.sendGameEventNotification(eventKind, eventValue, recipient);
    }

    /**
     * Native: parseCommandRepeatCount @004F5083.
     * Fully ported.
     */
    private static int parseCommandRepeatCount(StringBuilder commandText) {
        int space = commandText.indexOf(" ");
        if (space <= 0) {
            return 1;
        }
        int count = parseLeadingIntOrZero(commandText.substring(0, space));
        if (count < 1) {
            return 1;
        }
        commandText.delete(0, space + 1);
        return count;
    }

    /**
     * Native support extracted from GetInt callers in GameServer::handleChatCommand @004F3D68.
     */
    private static int parseLeadingIntOrZero(String text) {
        String value = text.stripLeading();
        int end = 0;
        if (!value.isEmpty() && (value.charAt(0) == '-' || value.charAt(0) == '+')) {
            end = 1;
        }
        while (end < value.length() && Character.isDigit(value.charAt(end))) {
            end++;
        }
        if (end == 0 || (end == 1 && (value.charAt(0) == '-' || value.charAt(0) == '+'))) {
            return 0;
        }
        try {
            return Integer.parseInt(value.substring(0, end));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    /**
     * Native support extracted from CString::Mid + CString::TrimLeft callers in GameServer::handleChatCommand @004F3D68.
     */
    private static String commandTail(String commandText, int prefixLength) {
        return commandText.substring(prefixLength).stripLeading();
    }

    static final class DecodedSave {
        final byte[] utf16LeBytes;
        final int utf16Units;

        // not ported.
        DecodedSave(byte[] utf16LeBytes, int utf16Units) {
            this.utf16LeBytes = utf16LeBytes;
            this.utf16Units = utf16Units;
        }
    }

}
