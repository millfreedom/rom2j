package ua.millfreedom.rom2.model.world;

import ua.millfreedom.rom2.CString;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CPlayer;
import ua.millfreedom.rom2.model.world.scenario.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Native: ScenarioDescriptor::ScenarioDescriptor @00534ad4.
 */
public final class ScenarioDescriptor {
    private static final int DEFAULT_MAP_DIMENSION = 0x10;
    private static final int MAX_SUPPORTED_MAP_VERSION = 0x640;
    private static final int PLAYER_RECORD_SIZE = 0x4c;
    private static final int INSTANCE_RECORD_SIZE = 0x31c;
    private static final int TRIGGER_RECORD_SIZE = 0xb8;
    private static final int GROUP_RECORD_SIZE = 0x10;
    private static final int INN_DESCRIPTOR_SIZE = 0x0c;
    private static final int SHOP_DESCRIPTOR_SIZE = 0x54;
    private static final int POST_DESCRIPTOR_SIZE = 0x0c;
    private static final int MUSIC_RECORD_SIZE = 0x1c;
    private static final int BUILDING_RECORD_BASE_SIZE = 0x14;
    private static final int BUILDING_EXTENDED_SIZE = 0x08;
    private static final int SACK_ITEM_RECORD_SIZE = 0x0a;
    private static final int EFFECT_MOD_RECORD_SIZE = 0x06;
    private static final int EFFECT_FIXED_RECORD_SIZE_NEW = 0x1a;
    private static final int EFFECT_FIXED_RECORD_SIZE_OLD = 0x16;

    public static final int LOAD_ERR_FILE_NOT_FOUND = 1;
    public static final int LOAD_ERR_NOT_A_MAP_FILE = 2;
    public static final int LOAD_ERR_WRONG_BLOCK_NUMBER = 3;
    public static final int LOAD_ERR_MAP_VERSION_TOO_NEW = 4;
    public static final int LOAD_ERR_TILES_BLOCK_NOT_FOUND = 5;
    public static final int LOAD_ERR_ALTITUDES_BLOCK_NOT_FOUND = 6;

    //0x00
    public int mapWidth;
    //0x04
    public int mapHeight;
    //0x08
    public int tileSectionType;
    //0x0C
    public short[] sec1Tiles;
    //0x10
    public byte[] sec3Objects;
    //0x14
    public byte[] sec2Heights;
    //0x18
    public float sunAngle;
    //0x1C
    public int time;
    //0x20
    public int darkness;
    //0x24
    public int contrast;
    //0x28
    public final List<CPlayer> sec5Players = new ArrayList<>();
    //0x3C
    public CPlayer pCPlayer;
    //0x40
    public final List<Instant> sect7Instants = new ArrayList<>();
    //0x5C
    public final List<Instant> sect7Checks = new ArrayList<>();
    //0x78
    public final List<Trigger> sect7Triggers = new ArrayList<>();
    //0x8C
    public int error;
    //0x90
    public int groupsHighestID;
    //0x94
    public int useTiles;
    //0x98
    public final CString mapName = new CString(0x40);
    //0xD8
    public int recommendedPlayers;
    //0xDC
    public int mapLevel;
    //0xE0
    public int unk;
    //0xE4
    public int unk2;
    //0xE8
    public final CString authors = new CString(0x200);
    //0x2E8
    public final List<WorldSack> sect8Sacks = new ArrayList<>();
    //0x2FC
    public final List<EffectDTO> sect9Effects = new ArrayList<>();
    //0x310
    public final List<GroupDTO> sect10Groups = new ArrayList<>();
    //0x324
    public final List<ShopDescriptor> sect11ShopDescriptors = new ArrayList<>();
    //0x338
    public final List<InnDescriptor> sect11InnDescriptors = new ArrayList<>();
    //0x34C
    public final List<PostDescriptor> sect11PostDescriptors = new ArrayList<>();
    // Native global g_CArray<MusicDTO> @00622798 lifecycle thunks @0047415A/@00474169/@00474178/@0047418A
    // are represented by this active ScenarioDescriptor-owned list.
    //0x360
    public final List<MusicDTO> sect12Music = new ArrayList<>();
    //0x374
    public final MusicDTO defaultMusic = new MusicDTO();
    //0x390
    public final List<BuildingDTO> sec4Buildings = new ArrayList<>();
    //0x3A4
    public final List<UnitDTO> sec6Units = new ArrayList<>();

    // Java support: raw section snapshots are not stored in native ScenarioDescriptor.
    public final List<ScenarioRawSection> rawSections = new ArrayList<>();
    // Java support: requested file path is not stored in native ScenarioDescriptor.
    public String requestedPath = "";
    // Java support: resolved file path is not stored in native ScenarioDescriptor.
    public String resolvedPath = "";
    // Java support: original scenario payload is not stored in native ScenarioDescriptor.
    public byte[] payload = new byte[0];
    // Java support: copied from ScenarioFileHeader::sectionsCount during parsing.
    public int sectionCount;
    // Java support: copied from ScenarioFileHeader::version during parsing.
    public int version;
    // Java support: load-completion flag is not stored in native ScenarioDescriptor.
    public boolean loaded;

    // Java support: native ScenarioDescriptor::ScenarioDescriptor @00534AD4 local.
    private int sec5PlayersCount;
    // Java support: native ScenarioDescriptor::ScenarioDescriptor @00534AD4 local.
    private int sec4BuildingsCount;
    // Java support: native ScenarioDescriptor::ScenarioDescriptor @00534AD4 local.
    private int sec6UnitsCount;
    // Java support: native ScenarioDescriptor::ScenarioDescriptor @00534AD4 local.
    private int sect8SacksCount;
    // Java support: native ScenarioDescriptor::ScenarioDescriptor @00534AD4 local.
    private int sect10GroupsCount;
    // Java support: native ScenarioDescriptor::ScenarioDescriptor @00534AD4 local.
    private int sect11InnDescriptorsCount;
    // Java support: native ScenarioDescriptor::ScenarioDescriptor @00534AD4 local.
    private int sect11ShopDescriptorsCount;
    // Java support: native ScenarioDescriptor::ScenarioDescriptor @00534AD4 local.
    private int sect11PostDescriptorsCount;
    // Java support: native ScenarioDescriptor::ScenarioDescriptor @00534AD4 local.
    private int sect12MusicCount;
    // Java support: native ScenarioDescriptor::ScenarioDescriptor @00534AD4 local.
    private int heightsPayloadSize;
    // Java support: native ScenarioDescriptor::ScenarioDescriptor @00534AD4 local.
    private boolean infoSectionLoaded;

    /**
     * Native: ScenarioDescriptor::ScenarioDescriptor @00534789.
     * Fully ported.
     */
    public ScenarioDescriptor() {
        initBlankMapDefaults();
    }

    /**
     * Native: ScenarioDescriptor::ScenarioDescriptor @00534106.
     * Fully ported.
     */
    public ScenarioDescriptor(int width, int height, int tileId, int heightValue) {
        mapWidth = width;
        mapHeight = height;
        int cellCount = mapWidth * mapHeight;
        sec2Heights = new byte[cellCount];
        Arrays.fill(sec2Heights, (byte) heightValue);
        sec1Tiles = new short[cellCount];
        Arrays.fill(sec1Tiles, (short) tileId);
        sec3Objects = new byte[cellCount];
        sunAngle = Float.intBitsToFloat(0x3f490fda);
        time = 0x168;
        darkness = 0x15;
        contrast = 0x3f;
        error = LOAD_ERR_FILE_NOT_FOUND;
        useTiles = 0;
        groupsHighestID = 0;
        pCPlayer = new CPlayer(1, 1);
        sec5Players.add(pCPlayer);
        mapName.clear();
        authors.clear();
        mapLevel = 1;
        recommendedPlayers = 1;
        unk = 0;
        unk2 = 60_000_000;
        tileSectionType = 5;
        defaultMusic.x = 0;
        defaultMusic.y = 0;
        defaultMusic.radius = 0;
        defaultMusic.m1 = -1;
        defaultMusic.m2 = -1;
        defaultMusic.m3 = -1;
        defaultMusic.m4 = -1;
    }

    /**
     * Native: ScenarioDescriptor::ScenarioDescriptor @0053440C.
     * Fully ported.
     */
    public ScenarioDescriptor(ScenarioDescriptor source) {
        mapWidth = source.getWidth();
        mapHeight = source.getHeight();
        int cellCount = mapWidth * mapHeight;
        sec2Heights = new byte[cellCount];
        System.arraycopy(source.sec2Heights, 0, sec2Heights, 0, cellCount);
        sec1Tiles = new short[cellCount];
        System.arraycopy(source.sec1Tiles, 0, sec1Tiles, 0, cellCount);
        sec3Objects = new byte[cellCount];
        System.arraycopy(source.sec3Objects, 0, sec3Objects, 0, cellCount);
        sunAngle = source.sunAngle;
        groupsHighestID = source.groupsHighestID;
        darkness = source.darkness;
        contrast = source.contrast;
        time = source.time;
        mapName.set(source.mapName.toString());
        authors.set(source.authors.toString());
        mapLevel = source.mapLevel;
        recommendedPlayers = source.recommendedPlayers;
        unk = source.unk;
        unk2 = source.unk2;
        error = source.error;
        pCPlayer = new CPlayer(source.pCPlayer);
        sec5Players.add(pCPlayer);
        for (int i = 1; i < source.sec5Players.size(); i++) {
            sec5Players.add(new CPlayer(source.sec5Players.get(i)));
        }
        tileSectionType = 7;
    }

    /**
     * Native: ScenarioDescriptor::ScenarioDescriptor @00534ad4.
     * Fully ported.
     */
    public ScenarioDescriptor(String scenarioPath) {
        initDefaults();

        if (!Globals.gameFileManager.exists(scenarioPath)) {
            error = LOAD_ERR_FILE_NOT_FOUND;
            return;
        }
        ByteBuffer source = Globals.gameFileManager.get(scenarioPath).duplicate().order(ByteOrder.LITTLE_ENDIAN);
        payload = new byte[source.remaining()];
        source.get(payload);
        parseScenarioPayload(ByteBuffer.wrap(payload).order(ByteOrder.LITTLE_ENDIAN));
    }

    /**
     * Native: ScenarioDescriptor::GetWidth @004A66B0.
     * Fully ported.
     */
    public int getWidth() {
        return mapWidth;
    }

    /**
     * Native: ScenarioDescriptor::GetHeight @004A66C0.
     * Fully ported.
     */
    public int getHeight() {
        return mapHeight;
    }

    /**
     * Native: ScenarioDescriptor::GetTiles @004A66E0.
     * Fully ported.
     */
    public short[] getTiles() {
        return sec1Tiles;
    }

    /**
     * Native support extracted from ScenarioDescriptor::ScenarioDescriptor @00534AD4 constructor prologue.
     * Fully ported.
     */
    private void initDefaults() {
        clearScenarioLoadSections();
        mapWidth = DEFAULT_MAP_DIMENSION;
        mapHeight = DEFAULT_MAP_DIMENSION;
        sunAngle = 0.0f;
        error = LOAD_ERR_FILE_NOT_FOUND;
        groupsHighestID = 0;
        darkness = 0x20;
        contrast = 0x20;
        sec1Tiles = null;
        sec2Heights = null;
        sec3Objects = null;
        defaultMusic.x = 0;
        defaultMusic.y = 0;
        defaultMusic.radius = 0;
        defaultMusic.m1 = -1;
        defaultMusic.m2 = -1;
        defaultMusic.m3 = -1;
        defaultMusic.m4 = -1;
        pCPlayer = null;
        loaded = false;
        sec5PlayersCount = 0;
        sec4BuildingsCount = 0;
        sec6UnitsCount = 0;
        sect8SacksCount = 0;
        sect10GroupsCount = 0;
        sect11InnDescriptorsCount = 0;
        sect11ShopDescriptorsCount = 0;
        sect11PostDescriptorsCount = 0;
        sect12MusicCount = 0;
        heightsPayloadSize = 0;
        infoSectionLoaded = false;
    }

    /**
     * Native support for ScenarioDescriptor::ScenarioDescriptor @00534AD4 fresh CArray/CList construction before parse.
     * Java support clears section lists when the loader reuses a blank-map descriptor instance.
     */
    private void clearScenarioLoadSections() {
        sec5Players.clear();
        sect7Instants.clear();
        sect7Checks.clear();
        sect7Triggers.clear();
        sect8Sacks.clear();
        sect9Effects.clear();
        sect10Groups.clear();
        sect11ShopDescriptors.clear();
        sect11InnDescriptors.clear();
        sect11PostDescriptors.clear();
        sect12Music.clear();
        sec4Buildings.clear();
        sec6Units.clear();
        rawSections.clear();
    }

    /**
     * Native support extracted from ScenarioDescriptor::ScenarioDescriptor @00534789.
     * Fully ported.
     */
    private void initBlankMapDefaults() {
        mapWidth = DEFAULT_MAP_DIMENSION;
        mapHeight = DEFAULT_MAP_DIMENSION;
        int cellCount = mapWidth * mapHeight;
        sec2Heights = new byte[cellCount];
        Arrays.fill(sec2Heights, (byte) 0x3F);
        sec1Tiles = new short[cellCount];
        Arrays.fill(sec1Tiles, (short) 0x11);
        sec3Objects = new byte[cellCount];
        sunAngle = 0.0f;
        darkness = 0x20;
        contrast = 0x20;
        time = 0x168;
        error = LOAD_ERR_FILE_NOT_FOUND;
        groupsHighestID = 0;
        mapName.clear();
        authors.clear();
        mapLevel = 1;
        unk = 0;
        unk2 = 60_000_000;
        recommendedPlayers = 1;
        pCPlayer = new CPlayer(1, 1);
        pCPlayer.gold = 0;
        pCPlayer.flags = 0;
        sec5Players.add(pCPlayer);
        tileSectionType = 0;
        defaultMusic.x = 0;
        defaultMusic.y = 0;
        defaultMusic.radius = 0;
        defaultMusic.m1 = -1;
        defaultMusic.m2 = -1;
        defaultMusic.m3 = -1;
        defaultMusic.m4 = -1;
    }

    /**
     * Native: ScenarioDescriptor::ScenarioDescriptor @00534ad4.
     * Fully ported.
     */
    private void parseScenarioPayload(ByteBuffer bb) {
        ScenarioFileHeader fileHeader = ScenarioFileHeader.read(bb);
        sectionCount = fileHeader.sectionCount;
        version = fileHeader.version;

        if (!fileHeader.isM7R()) {
            error = LOAD_ERR_NOT_A_MAP_FILE;
            return;
        }
        if (sectionCount < 3) {
            error = LOAD_ERR_WRONG_BLOCK_NUMBER;
            return;
        }
        if (version > MAX_SUPPORTED_MAP_VERSION) {
            error = LOAD_ERR_MAP_VERSION_TOO_NEW;
            return;
        }
        for (int i = 0; i < sectionCount; i++) {
            ScenarioSectionHeader sectionHeader = ScenarioSectionHeader.read(bb, fileHeader);
            int sectionDataStart = bb.position();

            switch (sectionHeader.id) {
                case ScenarioSectionId.INFO -> parseInfoSection(bb);
                case ScenarioSectionId.TILES -> parseTilesSection(sectionHeader, bb);
                case ScenarioSectionId.HEIGHTS -> parseHeightsSection(bb, sectionHeader.dataSize);
                case ScenarioSectionId.OBJECTS -> parseObjectsSection(bb);
                case ScenarioSectionId.BUILDINGS -> parseBuildingsSection(bb);
                case ScenarioSectionId.PLAYERS -> parsePlayersSection(bb);
                case ScenarioSectionId.UNITS -> parseUnitsSection(bb);
                case ScenarioSectionId.INSTANTS -> parseInstantsSection(bb);
                case ScenarioSectionId.SACKS -> parseSacksSection(bb);
                case ScenarioSectionId.EFFECTS -> parseEffectsSection(bb);
                case ScenarioSectionId.GROUPS -> parseGroupsSection(bb);
                case ScenarioSectionId.DESCRIPTORS -> parseDescriptorsSection(bb);
                case ScenarioSectionId.MUSIC -> parseMusicSection(bb);
                default -> skipUnknownSection(sectionHeader, bb);
            }

            rawSections.add(new ScenarioRawSection(
                    sectionHeader,
                    copyPayloadSlice(sectionDataStart, bb.position())
            ));
        }

        finalizeLoad();
    }

    /**
     * Native: ScenarioDescriptor::ScenarioDescriptor @00534ad4 info section.
     * Fully ported.
     */
    private void parseInfoSection(ByteBuffer section) {
        infoSectionLoaded = true;
        mapWidth = section.getInt();
        mapHeight = section.getInt();
        sunAngle = section.getFloat();
        time = section.getInt();
        darkness = section.getInt();
        contrast = section.getInt();
        useTiles = section.getInt();
        sec5PlayersCount = section.getInt();
        sec4BuildingsCount = section.getInt();
        sec6UnitsCount = section.getInt();
        section.getInt();
        sect8SacksCount = section.getInt();

        if (version > 0x47d) {
            sect10GroupsCount = section.getInt();
        }
        if (version > 0x4cd) {
            sect11InnDescriptorsCount = section.getInt();
            sect11ShopDescriptorsCount = section.getInt();
            sect11PostDescriptorsCount = section.getInt();
        }
        if (version > 0x513) {
            sect12MusicCount = section.getInt();
        }

        mapName.read(section);
        recommendedPlayers = section.getInt();
        mapLevel = section.getInt();
        if (version > 0x487) {
            unk = section.getInt();
            unk2 = section.getInt();
        }
        authors.read(section);
    }

    /**
     * Native: ScenarioDescriptor::ScenarioDescriptor @00534ad4 tiles section.
     * Fully ported.
     */
    private void parseTilesSection(ScenarioSectionHeader sectionHeader, ByteBuffer section) {
        tileSectionType = sectionHeader.type;
        sec1Tiles = new short[mapWidth * mapHeight];
        for (int i = 0; i < sec1Tiles.length; i++) {
            sec1Tiles[i] = section.getShort();
        }
    }

    /**
     * Native: ScenarioDescriptor::ScenarioDescriptor @00534ad4 heights section.
     * Fully ported.
     */
    private void parseHeightsSection(ByteBuffer bb, int declaredSize) {
        heightsPayloadSize = declaredSize;
        sec2Heights = new byte[declaredSize];
        bb.get(sec2Heights);
    }

    /**
     * Native: ScenarioDescriptor::ScenarioDescriptor @00534ad4 objects section.
     * Fully ported.
     */
    private void parseObjectsSection(ByteBuffer bb) {
        sec3Objects = new byte[heightsPayloadSize];
        bb.get(sec3Objects);
    }

    /**
     * Native: ScenarioDescriptor::ScenarioDescriptor @00534ad4 buildings section.
     * Fully ported.
     */
    private void parseBuildingsSection(ByteBuffer bb) {
        for (int parsed = 0; parsed < sec4BuildingsCount; parsed++) {
            sec4Buildings.add(BuildingDTO.read(bb));
        }
    }

    /**
     * Native: ScenarioDescriptor::ScenarioDescriptor @00534ad4 players section.
     * Fully ported.
     */
    private void parsePlayersSection(ByteBuffer bb) {
        for (int playerIndex = 1; playerIndex <= sec5PlayersCount; playerIndex++) {
            sec5Players.add(CPlayer.read(bb, playerIndex));
        }

        pCPlayer = sec5Players.getFirst();
    }

    /**
     * Native: ScenarioDescriptor::ScenarioDescriptor @00534ad4 units section.
     * Fully ported.
     */
    private void parseUnitsSection(ByteBuffer bb) {
        for (int parsed = 0; parsed < sec6UnitsCount; parsed++) {
            UnitDTO unit = UnitDTO.read(bb, version);
            if (groupsHighestID < unit.groupID) {
                groupsHighestID = unit.groupID;
            }
            sec6Units.add(unit);
        }
    }

    /**
     * Native: ScenarioDescriptor::ScenarioDescriptor @00534ad4 instances/checks/triggers section.
     * Fully ported.
     */
    private void parseInstantsSection(ByteBuffer section) {
        int instanceCount = section.getInt();
        for (int i = 0; i < instanceCount; i++) {
            sect7Instants.add(Instant.read(section));
        }

        int checkCount = section.getInt();
        for (int i = 0; i < checkCount; i++) {
            sect7Checks.add(Instant.read(section));
        }

        int triggerCount = section.getInt();
        for (int i = 0; i < triggerCount; i++) {
            sect7Triggers.add(Trigger.read(section));
        }
    }

    /**
     * Native: ScenarioDescriptor::ScenarioDescriptor @00534ad4 sacks section.
     * Fully ported.
     */
    private void parseSacksSection(ByteBuffer bb) {
        for (int parsed = 0; parsed < sect8SacksCount; parsed++) {
            sect8Sacks.add(WorldSack.read(bb, version));
        }
    }

    /**
     * Native: ScenarioDescriptor::ScenarioDescriptor @00534ad4 effects section.
     * Fully ported.
     */
    private void parseEffectsSection(ByteBuffer section) {
        int effectCount = section.getInt();
        for (int i = 0; i < effectCount; i++) {
            sect9Effects.add(EffectDTO.read(section, version));
        }
    }

    /**
     * Native: ScenarioDescriptor::ScenarioDescriptor @00534ad4 groups section.
     * Fully ported.
     */
    private void parseGroupsSection(ByteBuffer bb) {
        for (int i = 0; i < sect10GroupsCount; i++) {
            sect10Groups.add(GroupDTO.read(bb));
        }
    }

    /**
     * Native: ScenarioDescriptor::ScenarioDescriptor @00534ad4 descriptors section.
     * Fully ported.
     */
    private void parseDescriptorsSection(ByteBuffer section) {
        for (int i = 0; i < sect11InnDescriptorsCount; i++) {
            sect11InnDescriptors.add(InnDescriptor.read(section));
        }
        for (int i = 0; i < sect11ShopDescriptorsCount; i++) {
            sect11ShopDescriptors.add(ShopDescriptor.read(section));
        }
        for (int i = 0; i < sect11PostDescriptorsCount; i++) {
            sect11PostDescriptors.add(PostDescriptor.read(section));
        }
    }

    /**
     * Native: ScenarioDescriptor::ScenarioDescriptor @00534ad4 music section.
     * Fully ported.
     */
    private void parseMusicSection(ByteBuffer bb) {
        defaultMusic.read(bb);
        for (int i = 0; i < sect12MusicCount; i++) {
            MusicDTO music = new MusicDTO();
            music.read(bb);
            sect12Music.add(music);
        }
    }

    /**
     * Native support extracted from ScenarioDescriptor::ScenarioDescriptor @00534AD4 unknown-section branch.
     * Fully ported.
     */
    private void skipUnknownSection(ScenarioSectionHeader sectionHeader, ByteBuffer bb) {
        bb.position(bb.position() + sectionHeader.dataSize);
    }

    /**
     * Native: ScenarioDescriptor::ScenarioDescriptor @00534ad4.
     * Fully ported. Native leaves `error` at its initialized value after a successful load; Java exposes `loaded` as
     * the Java-only success flag.
     */
    private void finalizeLoad() {
        if (sec1Tiles == null) {
            error = LOAD_ERR_TILES_BLOCK_NOT_FOUND;
            loaded = false;
            return;
        }
        if (sec2Heights == null) {
            error = LOAD_ERR_ALTITUDES_BLOCK_NOT_FOUND;
            loaded = false;
            return;
        }

        if (sec3Objects == null) {
            sec3Objects = new byte[heightsPayloadSize];
        }

        resolveDeferredLinks();

        if (pCPlayer == null) {
            CPlayer player = new CPlayer(1, 1);
            player.gold = 5000;
            player.flags = 0;
            sec5Players.add(player);
            pCPlayer = player;
        }

        if (!infoSectionLoaded) {
            mapName.clear();
            authors.clear();
            mapLevel = 1;
            recommendedPlayers = 1;
        }

        loaded = true;
    }

    /**
     * Native: ScenarioDescriptor::ScenarioDescriptor @00534ad4 deferred cross-links.
     * Fully ported.
     */
    private void resolveDeferredLinks() {
        for (UnitDTO unit : sec6Units) {
            if (unit.sackIDX == -1 || sect8SacksCount <= unit.sackIDX) {
                unit.sackIDX = -1;
            } else {
                sect8Sacks.get(unit.sackIDX).unitID = (short) unit.unitID;
            }
        }

        for (WorldSack sack : sect8Sacks) {
            for (int itemIndex = 0; itemIndex < sack.effectIndices.size(); itemIndex++) {
                int effectID = sack.effectIndices.get(itemIndex);
                if (effectID != 0) {
                    EffectDTO effect = sect9Effects.get(effectID - 1);
                    effect.itemID = itemIndex;
                    effect.pWorldSack = sack;
                }
            }
        }
    }

    // not ported.
    private byte[] copyPayloadSlice(int startInclusive, int endExclusive) {
        return Arrays.copyOfRange(payload, startInclusive, endExclusive);
    }

}
