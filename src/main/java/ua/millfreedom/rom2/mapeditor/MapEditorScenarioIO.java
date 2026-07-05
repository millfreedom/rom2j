package ua.millfreedom.rom2.mapeditor;

import ua.millfreedom.rom2.model.CPlayer;
import ua.millfreedom.rom2.model.enums.BuildingId;
import ua.millfreedom.rom2.model.world.ScenarioDescriptor;
import ua.millfreedom.rom2.model.world.scenario.BuildingDTO;
import ua.millfreedom.rom2.model.world.scenario.EffectDTO;
import ua.millfreedom.rom2.model.world.scenario.EffectOrTrapMod;
import ua.millfreedom.rom2.model.world.scenario.GroupDTO;
import ua.millfreedom.rom2.model.world.scenario.InnDescriptor;
import ua.millfreedom.rom2.model.world.scenario.Instant;
import ua.millfreedom.rom2.model.world.scenario.MusicDTO;
import ua.millfreedom.rom2.model.world.scenario.PostDescriptor;
import ua.millfreedom.rom2.model.world.scenario.ScenarioFileHeader;
import ua.millfreedom.rom2.model.world.scenario.ScenarioRawSection;
import ua.millfreedom.rom2.model.world.scenario.ScenarioSectionHeader;
import ua.millfreedom.rom2.model.world.scenario.ScenarioSectionId;
import ua.millfreedom.rom2.model.world.scenario.ShopDescriptor;
import ua.millfreedom.rom2.model.world.scenario.Trigger;
import ua.millfreedom.rom2.model.world.scenario.UnitDTO;
import ua.millfreedom.rom2.model.world.scenario.WorldSack;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * MapEditor-owned adapter around existing scenario descriptor parsing and ALM section output.
 * not ported.
 */
public final class MapEditorScenarioIO {
    private static final int CURRENT_SECTION_VERSION_STAMP = 0x14100032;
    private static final int CURRENT_SECTION_TYPE = 7;
    private static final int MAX_SUPPORTED_MAP_VERSION = 0x640;
    private static final int INFO_SECTION_DECLARED_SIZE_BIAS = 0x10;
    private static final int INFO_SECTION_PAYLOAD_SIZE = 0x294;
    private static final int SCENARIO_DIPLOMACY_WORD_COUNT = 0x10;
    private static final int BUILDING_BASE_RECORD_SIZE = 0x14;
    private static final int BUILDING_EXTENDED_SIZE_BYTES = 0x08;
    private static final int UNIT_RECORD_SIZE_CURRENT = 0x30;
    private static final int INSTANT_RECORD_SIZE = 0x31C;
    private static final int SACK_RECORD_FIXED_SIZE_CURRENT = 0x14;
    private static final int SACK_ITEM_RECORD_SIZE = 0x0A;
    private static final int EFFECT_SECTION_COUNT_SIZE = 0x04;
    private static final int EFFECT_FIXED_RECORD_SIZE_CURRENT = 0x1A;
    private static final int EFFECT_MODIFIER_RECORD_SIZE = 0x06;
    private static final int GROUP_RECORD_SIZE = 0x10;
    private static final int INN_DESCRIPTOR_RECORD_SIZE = 0x0C;
    private static final int SHOP_DESCRIPTOR_RECORD_SIZE = 0x54;
    private static final int POST_DESCRIPTOR_RECORD_SIZE = 0x0C;
    private static final int MUSIC_RECORD_SIZE = 0x1C;
    private static final Method INIT_DEFAULTS = scenarioMethod("initDefaults");
    private static final Method PARSE_SCENARIO_PAYLOAD = scenarioMethod("parseScenarioPayload", ByteBuffer.class);

    /**
     * Java utility constructor.
     * not ported.
     */
    private MapEditorScenarioIO() {
    }

    /**
     * Java support loader for file-chooser paths. It reuses ScenarioDescriptor's existing native parser without adding
     * editor-only constructors to the game model class.
     * not ported.
     */
    public static ScenarioDescriptor load(Path path) {
        byte[] payload;
        try {
            payload = Files.readAllBytes(path);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to read scenario " + path, exception);
        }

        ScenarioDescriptor scenario = new ScenarioDescriptor();
        invokeScenarioMethod(INIT_DEFAULTS, scenario);
        scenario.requestedPath = path.toString();
        scenario.resolvedPath = path.toAbsolutePath().normalize().toString();
        scenario.payload = payload;
        invokeScenarioMethod(
                PARSE_SCENARIO_PAYLOAD,
                scenario,
                ByteBuffer.wrap(payload).order(ByteOrder.LITTLE_ENDIAN)
        );
        return scenario;
    }

    /**
     * Java support writer for map-editor ALM output. Rebuilds core edited sections and carries unsupported loaded
     * sections byte-for-byte until each section has an isolated editor serializer.
     * not ported.
     */
    public static byte[] toAlmBytes(ScenarioDescriptor scenario) {
        List<ScenarioRawSection> sections = buildWritableSections(scenario);
        int payloadSize = 0;
        for (ScenarioRawSection section : sections) {
            payloadSize += ScenarioSectionHeader.BYTE_SIZE + section.payload.length;
        }

        ByteBuffer output = ByteBuffer.allocate(ScenarioFileHeader.BYTE_SIZE + payloadSize)
                .order(ByteOrder.LITTLE_ENDIAN);
        writeFileHeader(output, scenario, payloadSize, sections.size());
        for (ScenarioRawSection section : sections) {
            writeSection(output, section.header, section.payload);
        }
        return output.array();
    }

    /**
     * Java support reflection lookup for existing ScenarioDescriptor parser routines.
     * not ported.
     */
    private static Method scenarioMethod(String name, Class<?>... parameterTypes) {
        try {
            Method method = ScenarioDescriptor.class.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    /**
     * Java support reflection invocation for existing ScenarioDescriptor parser routines.
     * not ported.
     */
    private static void invokeScenarioMethod(Method method, ScenarioDescriptor scenario, Object... args) {
        try {
            method.invoke(scenario, args);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("ScenarioDescriptor parser routine is not accessible", exception);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("ScenarioDescriptor parser routine failed", cause);
        }
    }

    /**
     * Java support for composing the editor-writable ALM section list.
     * not ported.
     */
    private static List<ScenarioRawSection> buildWritableSections(ScenarioDescriptor scenario) {
        List<ScenarioRawSection> sections = new ArrayList<>();
        Set<Integer> writtenSectionIds = new HashSet<>();
        for (ScenarioRawSection rawSection : scenario.rawSections) {
            int sectionId = rawSection.header.id;
            ScenarioRawSection writableSection = isCoreEditorSection(sectionId)
                    ? buildCoreEditorSection(scenario, rawSection.header)
                    : rawSection;
            sections.add(writableSection);
            writtenSectionIds.add(sectionId);
        }

        addGeneratedSectionIfMissing(scenario, sections, writtenSectionIds, ScenarioSectionId.INFO);
        addGeneratedSectionIfMissing(scenario, sections, writtenSectionIds, ScenarioSectionId.TILES);
        addGeneratedSectionIfMissing(scenario, sections, writtenSectionIds, ScenarioSectionId.HEIGHTS);
        addGeneratedSectionIfMissing(scenario, sections, writtenSectionIds, ScenarioSectionId.OBJECTS);
        if (!scenario.sec4Buildings.isEmpty()) {
            addGeneratedSectionIfMissing(scenario, sections, writtenSectionIds, ScenarioSectionId.BUILDINGS);
        }
        addGeneratedSectionIfMissing(scenario, sections, writtenSectionIds, ScenarioSectionId.PLAYERS);
        if (!scenario.sec6Units.isEmpty()) {
            addGeneratedSectionIfMissing(scenario, sections, writtenSectionIds, ScenarioSectionId.UNITS);
        }
        if (shouldWriteGeneratedInstantsSection(scenario)) {
            addGeneratedSectionIfMissing(scenario, sections, writtenSectionIds, ScenarioSectionId.INSTANTS);
        }
        if (!scenario.sect8Sacks.isEmpty()) {
            addGeneratedSectionIfMissing(scenario, sections, writtenSectionIds, ScenarioSectionId.SACKS);
        }
        if (!scenario.sect9Effects.isEmpty()) {
            addGeneratedSectionIfMissing(scenario, sections, writtenSectionIds, ScenarioSectionId.EFFECTS);
        }
        if (!scenario.sect10Groups.isEmpty()) {
            addGeneratedSectionIfMissing(scenario, sections, writtenSectionIds, ScenarioSectionId.GROUPS);
        }
        if (shouldWriteGeneratedDescriptorsSection(scenario)) {
            addGeneratedSectionIfMissing(scenario, sections, writtenSectionIds, ScenarioSectionId.DESCRIPTORS);
        }
        if (shouldWriteGeneratedMusicSection(scenario)) {
            addGeneratedSectionIfMissing(scenario, sections, writtenSectionIds, ScenarioSectionId.MUSIC);
        }
        return sections;
    }

    /**
     * Java support for appending generated core ALM sections when saving a new blank editor map.
     * not ported.
     */
    private static void addGeneratedSectionIfMissing(
            ScenarioDescriptor scenario,
            List<ScenarioRawSection> sections,
            Set<Integer> writtenSectionIds,
            int sectionId
    ) {
        if (writtenSectionIds.contains(sectionId)) {
            return;
        }
        sections.add(buildCoreEditorSection(scenario, defaultSectionHeader(scenario, sectionId)));
        writtenSectionIds.add(sectionId);
    }

    /**
     * Java support predicate for ALM sections currently rebuilt by the map editor.
     * not ported.
     */
    private static boolean isCoreEditorSection(int sectionId) {
        return sectionId == ScenarioSectionId.INFO
                || sectionId == ScenarioSectionId.TILES
                || sectionId == ScenarioSectionId.HEIGHTS
                || sectionId == ScenarioSectionId.OBJECTS
                || sectionId == ScenarioSectionId.BUILDINGS
                || sectionId == ScenarioSectionId.PLAYERS
                || sectionId == ScenarioSectionId.UNITS
                || sectionId == ScenarioSectionId.INSTANTS
                || sectionId == ScenarioSectionId.SACKS
                || sectionId == ScenarioSectionId.EFFECTS
                || sectionId == ScenarioSectionId.GROUPS
                || sectionId == ScenarioSectionId.DESCRIPTORS
                || sectionId == ScenarioSectionId.MUSIC;
    }

    /**
     * Java support dispatcher for ALM core-section payload writers used by the map editor.
     * not ported.
     */
    private static ScenarioRawSection buildCoreEditorSection(
            ScenarioDescriptor scenario,
            ScenarioSectionHeader sourceHeader
    ) {
        byte[] sectionPayload = switch (sourceHeader.id) {
            case ScenarioSectionId.INFO -> buildInfoSectionPayload(scenario);
            case ScenarioSectionId.TILES -> buildTilesSectionPayload(scenario);
            case ScenarioSectionId.HEIGHTS -> scenario.sec2Heights.clone();
            case ScenarioSectionId.OBJECTS -> scenario.sec3Objects.clone();
            case ScenarioSectionId.BUILDINGS -> buildBuildingsSectionPayload(scenario);
            case ScenarioSectionId.PLAYERS -> buildPlayersSectionPayload(scenario);
            case ScenarioSectionId.UNITS -> buildUnitsSectionPayload(scenario);
            case ScenarioSectionId.INSTANTS -> buildInstantsSectionPayload(scenario);
            case ScenarioSectionId.SACKS -> buildSacksSectionPayload(scenario);
            case ScenarioSectionId.EFFECTS -> buildEffectsSectionPayload(scenario);
            case ScenarioSectionId.GROUPS -> buildGroupsSectionPayload(scenario);
            case ScenarioSectionId.DESCRIPTORS -> buildDescriptorsSectionPayload(scenario);
            case ScenarioSectionId.MUSIC -> buildMusicSectionPayload(scenario);
            default -> throw new IllegalArgumentException("Unsupported core section " + sourceHeader.id);
        };
        return new ScenarioRawSection(
                sectionHeaderForPayload(scenario, sourceHeader, sectionPayload.length),
                sectionPayload
        );
    }

    /**
     * Java support for generating a current-format ALM section header.
     * not ported.
     */
    private static ScenarioSectionHeader defaultSectionHeader(ScenarioDescriptor scenario, int sectionId) {
        int type = sectionId == ScenarioSectionId.TILES ? scenario.tileSectionType : CURRENT_SECTION_TYPE;
        return new ScenarioSectionHeader(type, ScenarioSectionHeader.BYTE_SIZE, 0, sectionId, CURRENT_SECTION_VERSION_STAMP);
    }

    /**
     * Java support for copying ALM section header metadata while refreshing the payload size.
     * not ported.
     */
    private static ScenarioSectionHeader sectionHeaderForPayload(
            ScenarioDescriptor scenario,
            ScenarioSectionHeader sourceHeader,
            int payloadSize
    ) {
        int dataSize = sourceHeader.id == ScenarioSectionId.INFO
                ? Math.max(0, payloadSize - INFO_SECTION_DECLARED_SIZE_BIAS)
                : payloadSize;
        int type = sourceHeader.id == ScenarioSectionId.TILES ? scenario.tileSectionType : sourceHeader.type;
        int headerSize = sourceHeader.headerSize == 0 ? ScenarioSectionHeader.BYTE_SIZE : sourceHeader.headerSize;
        int sectionVersion = sourceHeader.version == 0 ? CURRENT_SECTION_VERSION_STAMP : sourceHeader.version;
        return new ScenarioSectionHeader(type, headerSize, dataSize, sourceHeader.id, sectionVersion);
    }

    /**
     * Java support writer for the current-format ALM file header.
     * not ported.
     */
    private static void writeFileHeader(
            ByteBuffer output,
            ScenarioDescriptor scenario,
            int payloadSize,
            int writableSectionCount
    ) {
        output.put((byte) 'M');
        output.put((byte) '7');
        output.put((byte) 'R');
        output.put((byte) 0);
        output.putInt(ScenarioFileHeader.BYTE_SIZE);
        output.putInt(payloadSize);
        output.putInt(writableSectionCount);
        output.putInt(MAX_SUPPORTED_MAP_VERSION);
    }

    /**
     * Java support writer for a current-format ALM section block.
     * not ported.
     */
    private static void writeSection(ByteBuffer output, ScenarioSectionHeader header, byte[] sectionPayload) {
        output.putInt(header.type);
        output.putInt(header.headerSize);
        output.putInt(header.dataSize);
        output.putInt(header.id);
        output.putInt(header.version);
        output.put(sectionPayload);
    }

    /**
     * Java support writer for the ALM info section consumed by ScenarioDescriptor::ScenarioDescriptor @00534AD4.
     * not ported.
     */
    private static byte[] buildInfoSectionPayload(ScenarioDescriptor scenario) {
        ByteBuffer section = ByteBuffer.allocate(INFO_SECTION_PAYLOAD_SIZE).order(ByteOrder.LITTLE_ENDIAN);
        section.putInt(scenario.mapWidth);
        section.putInt(scenario.mapHeight);
        section.putFloat(scenario.sunAngle);
        section.putInt(scenario.time);
        section.putInt(scenario.darkness);
        section.putInt(scenario.contrast);
        section.putInt(scenario.useTiles);
        section.putInt(scenario.sec5Players.size());
        section.putInt(scenario.sec4Buildings.size());
        section.putInt(scenario.sec6Units.size());
        section.putInt(scenario.sect9Effects.size());
        section.putInt(scenario.sect8Sacks.size());
        section.putInt(scenario.sect10Groups.size());
        section.putInt(scenario.sect11InnDescriptors.size());
        section.putInt(scenario.sect11ShopDescriptors.size());
        section.putInt(scenario.sect11PostDescriptors.size());
        section.putInt(scenario.sect12Music.size());
        scenario.mapName.write(section);
        section.putInt(scenario.recommendedPlayers);
        section.putInt(scenario.mapLevel);
        section.putInt(scenario.unk);
        section.putInt(scenario.unk2);
        scenario.authors.write(section);
        return section.array();
    }

    /**
     * Java support writer for the ALM tile section consumed by ScenarioDescriptor::ScenarioDescriptor @00534AD4.
     * not ported.
     */
    private static byte[] buildTilesSectionPayload(ScenarioDescriptor scenario) {
        ByteBuffer section = ByteBuffer.allocate(scenario.sec1Tiles.length * Short.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN);
        for (short tile : scenario.sec1Tiles) {
            section.putShort(tile);
        }
        return section.array();
    }

    /**
     * Java support writer for the ALM players section consumed by ScenarioDescriptor::ScenarioDescriptor @00534AD4.
     * not ported.
     */
    private static byte[] buildPlayersSectionPayload(ScenarioDescriptor scenario) {
        ByteBuffer section = ByteBuffer.allocate(scenario.sec5Players.size() * CPlayer.NATIVE_SIZE)
                .order(ByteOrder.LITTLE_ENDIAN);
        for (CPlayer player : scenario.sec5Players) {
            writeScenarioPlayerRecord(section, player);
        }
        return section.array();
    }

    /**
     * Java support writer for the ALM buildings section consumed by ScenarioDescriptor::ScenarioDescriptor @00534AD4.
     * not ported.
     */
    private static byte[] buildBuildingsSectionPayload(ScenarioDescriptor scenario) {
        int payloadSize = 0;
        for (BuildingDTO building : scenario.sec4Buildings) {
            payloadSize += buildingRecordSize(building);
        }
        ByteBuffer section = ByteBuffer.allocate(payloadSize).order(ByteOrder.LITTLE_ENDIAN);
        for (BuildingDTO building : scenario.sec4Buildings) {
            writeBuildingRecord(section, building);
        }
        return section.array();
    }

    /**
     * Java support writer for the ALM units section consumed by ScenarioDescriptor::ScenarioDescriptor @00534AD4.
     * not ported.
     */
    private static byte[] buildUnitsSectionPayload(ScenarioDescriptor scenario) {
        ByteBuffer section = ByteBuffer.allocate(scenario.sec6Units.size() * UNIT_RECORD_SIZE_CURRENT)
                .order(ByteOrder.LITTLE_ENDIAN);
        for (UnitDTO unit : scenario.sec6Units) {
            writeUnitRecord(section, unit);
        }
        return section.array();
    }

    /**
     * Java support writer for the ALM instants/checks/triggers section consumed by
     * ScenarioDescriptor::ScenarioDescriptor @00534AD4.
     * not ported.
     */
    private static byte[] buildInstantsSectionPayload(ScenarioDescriptor scenario) {
        int payloadSize = Integer.BYTES
                + scenario.sect7Instants.size() * INSTANT_RECORD_SIZE
                + Integer.BYTES
                + scenario.sect7Checks.size() * INSTANT_RECORD_SIZE
                + Integer.BYTES
                + scenario.sect7Triggers.size() * Trigger.SERIALIZED_SIZE;
        ByteBuffer section = ByteBuffer.allocate(payloadSize).order(ByteOrder.LITTLE_ENDIAN);
        section.putInt(scenario.sect7Instants.size());
        for (Instant instant : scenario.sect7Instants) {
            writeInstantRecord(section, instant);
        }
        section.putInt(scenario.sect7Checks.size());
        for (Instant check : scenario.sect7Checks) {
            writeInstantRecord(section, check);
        }
        section.putInt(scenario.sect7Triggers.size());
        for (Trigger trigger : scenario.sect7Triggers) {
            trigger.write(section);
        }
        return section.array();
    }

    /**
     * Java support writer for the ALM sacks section consumed by ScenarioDescriptor::ScenarioDescriptor @00534AD4.
     * not ported.
     */
    private static byte[] buildSacksSectionPayload(ScenarioDescriptor scenario) {
        int payloadSize = 0;
        for (WorldSack sack : scenario.sect8Sacks) {
            payloadSize += sackRecordSize(sack);
        }
        ByteBuffer section = ByteBuffer.allocate(payloadSize).order(ByteOrder.LITTLE_ENDIAN);
        for (WorldSack sack : scenario.sect8Sacks) {
            writeSackRecord(section, sack);
        }
        return section.array();
    }

    /**
     * Java support writer for the ALM effects section consumed by ScenarioDescriptor::ScenarioDescriptor @00534AD4.
     * not ported.
     */
    private static byte[] buildEffectsSectionPayload(ScenarioDescriptor scenario) {
        int payloadSize = EFFECT_SECTION_COUNT_SIZE;
        for (EffectDTO effect : scenario.sect9Effects) {
            payloadSize += effectRecordSize(effect);
        }
        ByteBuffer section = ByteBuffer.allocate(payloadSize).order(ByteOrder.LITTLE_ENDIAN);
        section.putInt(scenario.sect9Effects.size());
        for (EffectDTO effect : scenario.sect9Effects) {
            writeEffectRecord(section, effect);
        }
        return section.array();
    }

    /**
     * Java support writer for the ALM groups section consumed by ScenarioDescriptor::ScenarioDescriptor @00534AD4.
     * not ported.
     */
    private static byte[] buildGroupsSectionPayload(ScenarioDescriptor scenario) {
        ByteBuffer section = ByteBuffer.allocate(scenario.sect10Groups.size() * GROUP_RECORD_SIZE)
                .order(ByteOrder.LITTLE_ENDIAN);
        for (GroupDTO group : scenario.sect10Groups) {
            writeGroupRecord(section, group);
        }
        return section.array();
    }

    /**
     * Java support writer for the ALM descriptors section consumed by ScenarioDescriptor::ScenarioDescriptor @00534AD4.
     * not ported.
     */
    private static byte[] buildDescriptorsSectionPayload(ScenarioDescriptor scenario) {
        int payloadSize = scenario.sect11InnDescriptors.size() * INN_DESCRIPTOR_RECORD_SIZE
                + scenario.sect11ShopDescriptors.size() * SHOP_DESCRIPTOR_RECORD_SIZE
                + scenario.sect11PostDescriptors.size() * POST_DESCRIPTOR_RECORD_SIZE;
        ByteBuffer section = ByteBuffer.allocate(payloadSize).order(ByteOrder.LITTLE_ENDIAN);
        for (InnDescriptor descriptor : scenario.sect11InnDescriptors) {
            writeInnDescriptorRecord(section, descriptor);
        }
        for (ShopDescriptor descriptor : scenario.sect11ShopDescriptors) {
            writeShopDescriptorRecord(section, descriptor);
        }
        for (PostDescriptor descriptor : scenario.sect11PostDescriptors) {
            writePostDescriptorRecord(section, descriptor);
        }
        return section.array();
    }

    /**
     * Java support writer for the ALM music section consumed by ScenarioDescriptor::ScenarioDescriptor @00534AD4.
     * not ported.
     */
    private static byte[] buildMusicSectionPayload(ScenarioDescriptor scenario) {
        ByteBuffer section = ByteBuffer.allocate((scenario.sect12Music.size() + 1) * MUSIC_RECORD_SIZE)
                .order(ByteOrder.LITTLE_ENDIAN);
        writeMusicRecord(section, scenario.defaultMusic);
        for (MusicDTO music : scenario.sect12Music) {
            writeMusicRecord(section, music);
        }
        return section.array();
    }

    /**
     * Java support predicate for adding a missing MUSIC section during editor save-as.
     * not ported.
     */
    private static boolean shouldWriteGeneratedMusicSection(ScenarioDescriptor scenario) {
        return scenario.defaultMusic.m1 >= 0
                || scenario.defaultMusic.m2 >= 0
                || scenario.defaultMusic.m3 >= 0
                || scenario.defaultMusic.m4 >= 0
                || !scenario.sect12Music.isEmpty();
    }

    /**
     * Java support predicate for adding a missing DESCRIPTORS section during editor save-as.
     * not ported.
     */
    private static boolean shouldWriteGeneratedDescriptorsSection(ScenarioDescriptor scenario) {
        return !scenario.sect11InnDescriptors.isEmpty()
                || !scenario.sect11ShopDescriptors.isEmpty()
                || !scenario.sect11PostDescriptors.isEmpty();
    }

    /**
     * Java support predicate for adding a missing INSTANTS section during editor save-as.
     * not ported.
     */
    private static boolean shouldWriteGeneratedInstantsSection(ScenarioDescriptor scenario) {
        return !scenario.sect7Instants.isEmpty()
                || !scenario.sect7Checks.isEmpty()
                || !scenario.sect7Triggers.isEmpty();
    }

    /**
     * Java support writer for a CPlayer scenario record, kept in editor IO instead of the game model class.
     * not ported.
     */
    private static void writeScenarioPlayerRecord(ByteBuffer section, CPlayer player) {
        section.putInt(player.color);
        section.putInt(player.flags);
        section.putInt(player.gold);
        player.name.write(section);
        for (int i = 0; i < SCENARIO_DIPLOMACY_WORD_COUNT; i++) {
            section.putShort(player.diplomacyFlags[i]);
        }
    }

    /**
     * Java support writer for one BuildingDTO scenario record, kept in editor IO instead of the game model class.
     * not ported.
     */
    private static void writeBuildingRecord(ByteBuffer section, BuildingDTO building) {
        section.putInt(building.x << 8);
        section.putInt(building.y << 8);
        section.putInt(building.typeID);
        section.putShort((short) building.hp);
        section.putInt(building.playerID);
        section.putShort((short) building.buildingID);
        if (writesExtendedBuildingSize(building)) {
            section.putInt(building.sizeX);
            section.putInt(building.sizeY);
        }
    }

    /**
     * Java support record-size helper for the current ScenarioDescriptor building parser shape.
     * not ported.
     */
    private static int buildingRecordSize(BuildingDTO building) {
        return BUILDING_BASE_RECORD_SIZE
                + (writesExtendedBuildingSize(building) ? BUILDING_EXTENDED_SIZE_BYTES : 0);
    }

    /**
     * Java support predicate matching BuildingDTO.read(...) size-tail consumption.
     * not ported.
     */
    private static boolean writesExtendedBuildingSize(BuildingDTO building) {
        return building.typeID == BuildingId.VERTICAL_WOODEN_BRIDGE.id;
    }

    /**
     * Java support writer for one current-format UnitDTO scenario record, kept in editor IO instead of the game model
     * class.
     * not ported.
     */
    private static void writeUnitRecord(ByteBuffer section, UnitDTO unit) {
        section.putInt(unit.x);
        section.putInt(unit.y);
        section.putShort((short) unit.typeID);
        section.putShort((short) unit.face);
        section.putInt(unit.unitFlags1);
        section.putInt(unit.questFlags);
        section.putInt(unit.serverID);
        section.putInt(unit.playerID);
        section.putInt(unit.sackIDX + 1);
        section.putInt(unit.rotation);
        section.putShort((short) unit.hp);
        section.putShort((short) unit.maxHp);
        section.putInt(unit.unitID);
        section.putInt(unit.groupID);
    }

    /**
     * Java support writer for one Instant scenario record, kept in editor IO instead of the game model class.
     * not ported.
     */
    private static void writeInstantRecord(ByteBuffer section, Instant instant) {
        instant.name.write(section);
        section.putInt(instant.typeId);
        section.putInt(instant.index);
        section.putInt(instant.executeOnce);
        for (int i = 0; i < instant.arguments.length; i++) {
            section.putInt(instant.arguments[i].value);
        }
        for (int i = 0; i < instant.arguments.length; i++) {
            section.putInt(instant.arguments[i].type);
        }
        for (int i = 0; i < instant.arguments.length; i++) {
            instant.arguments[i].name.write(section);
        }
    }

    /**
     * Java support record-size helper for the current ScenarioDescriptor sack parser shape.
     * not ported.
     */
    private static int sackRecordSize(WorldSack sack) {
        return SACK_RECORD_FIXED_SIZE_CURRENT + sackItemCount(sack) * SACK_ITEM_RECORD_SIZE;
    }

    /**
     * Java support item-count helper for the parallel arrays used by the native sack section shape.
     * not ported.
     */
    private static int sackItemCount(WorldSack sack) {
        int itemCount = sack.itemPackedHashes.size();
        if (itemCount != sack.incomingItemFlags.size() || itemCount != sack.effectIndices.size()) {
            throw new IllegalStateException("Scenario sack item arrays are not aligned.");
        }
        return itemCount;
    }

    /**
     * Java support writer for one WorldSack scenario record, kept in editor IO instead of the game model class.
     * not ported.
     */
    private static void writeSackRecord(ByteBuffer section, WorldSack sack) {
        int itemCount = sackItemCount(sack);
        section.putInt(itemCount);
        section.putInt(sack.unitID);
        section.putInt(sack.x);
        section.putInt(sack.y);
        section.putInt(sack.gold);
        for (int itemIndex = 0; itemIndex < itemCount; itemIndex++) {
            section.putInt(sack.itemPackedHashes.get(itemIndex));
            section.putShort((short) (sack.incomingItemFlags.get(itemIndex) & 0xFFFF));
            section.putInt(sack.effectIndices.get(itemIndex));
        }
    }

    /**
     * Java support record-size helper for the current ScenarioDescriptor effect parser shape.
     * not ported.
     */
    private static int effectRecordSize(EffectDTO effect) {
        return EFFECT_FIXED_RECORD_SIZE_CURRENT + effect.carr.size() * EFFECT_MODIFIER_RECORD_SIZE;
    }

    /**
     * Java support writer for one EffectDTO scenario record, kept in editor IO instead of the game model class.
     * not ported.
     */
    private static void writeEffectRecord(ByteBuffer section, EffectDTO effect) {
        section.putInt(effect.itemID);
        section.putInt(effect.x);
        section.putInt(effect.y);
        section.putShort((short) (effect.effectMode & 0xFFFF));
        section.putShort((short) (effect.min & 0xFFFF));
        section.putShort((short) (effect.spread & 0xFFFF));
        section.putInt((effect.spellId & 0xFFFF) | ((effect.spellStrength & 0xFFFF) << 16));
        section.putInt(effect.carr.size());
        for (EffectOrTrapMod modifier : effect.carr) {
            writeEffectModifierRecord(section, modifier);
        }
    }

    /**
     * Java support writer for one EffectOrTrapMod record, kept in editor IO instead of the game model class.
     * not ported.
     */
    private static void writeEffectModifierRecord(ByteBuffer section, EffectOrTrapMod modifier) {
        section.putShort((short) (modifier.type & 0xFFFF));
        section.putInt(modifier.value);
    }

    /**
     * Java support writer for one GroupDTO scenario record, kept in editor IO instead of the game model class.
     * not ported.
     */
    private static void writeGroupRecord(ByteBuffer section, GroupDTO group) {
        section.putInt(group.id);
        section.putInt(group.repopTime);
        section.putInt(group.flags);
        section.putInt(group.instID);
    }

    /**
     * Java support writer for one InnDescriptor scenario record, kept in editor IO instead of the game model class.
     * not ported.
     */
    private static void writeInnDescriptorRecord(ByteBuffer section, InnDescriptor descriptor) {
        section.putInt(descriptor.id);
        section.putInt(descriptor.flags);
        section.putInt(descriptor.itemID);
    }

    /**
     * Java support writer for one ShopDescriptor scenario record, kept in editor IO instead of the game model class.
     * not ported.
     */
    private static void writeShopDescriptorRecord(ByteBuffer section, ShopDescriptor descriptor) {
        section.putInt(descriptor.id);
        writeIntArray(section, descriptor.shelfFlags);
        writeIntArray(section, descriptor.minPrices);
        writeIntArray(section, descriptor.maxPrices);
        writeIntArray(section, descriptor.maxItems);
        writeIntArray(section, descriptor.maxSameTypeItems);
    }

    /**
     * Java support writer for one PostDescriptor scenario record, kept in editor IO instead of the game model class.
     * not ported.
     */
    private static void writePostDescriptorRecord(ByteBuffer section, PostDescriptor descriptor) {
        section.putInt(descriptor.id);
        section.putInt(descriptor.instanceOn);
        section.putInt(descriptor.instanceID);
    }

    /**
     * Java support writer for fixed-width int arrays in descriptor records.
     * not ported.
     */
    private static void writeIntArray(ByteBuffer section, int[] values) {
        for (int value : values) {
            section.putInt(value);
        }
    }

    /**
     * Java support writer for one MusicDTO scenario record, kept in editor IO instead of the game model class.
     * not ported.
     */
    private static void writeMusicRecord(ByteBuffer section, MusicDTO music) {
        section.putInt(music.x);
        section.putInt(music.y);
        section.putInt(music.radius);
        section.putInt(music.m1);
        section.putInt(music.m2);
        section.putInt(music.m3);
        section.putInt(music.m4);
    }
}
