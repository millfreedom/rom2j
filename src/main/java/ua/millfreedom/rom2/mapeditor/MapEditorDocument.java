package ua.millfreedom.rom2.mapeditor;

import ua.millfreedom.rom2.model.CPlayer;
import ua.millfreedom.rom2.model.world.ScenarioDescriptor;
import ua.millfreedom.rom2.model.world.scenario.BuildingDTO;
import ua.millfreedom.rom2.model.world.scenario.EffectDTO;
import ua.millfreedom.rom2.model.world.scenario.EffectOrTrapMod;
import ua.millfreedom.rom2.model.world.scenario.GroupDTO;
import ua.millfreedom.rom2.model.world.scenario.InnDescriptor;
import ua.millfreedom.rom2.model.world.scenario.Instant;
import ua.millfreedom.rom2.model.world.scenario.MusicDTO;
import ua.millfreedom.rom2.model.world.scenario.PostDescriptor;
import ua.millfreedom.rom2.model.world.scenario.ShopDescriptor;
import ua.millfreedom.rom2.model.world.scenario.Trigger;
import ua.millfreedom.rom2.model.world.scenario.UnitDTO;
import ua.millfreedom.rom2.model.world.scenario.WorldSack;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.random.RandomGenerator;

/**
 * Java support document model for the standalone MapEditor.
 * not ported.
 */
public final class MapEditorDocument {
    public static final int MAX_PLAYERS = 16;
    private static final int SCENARIO_DIPLOMACY_WORD_COUNT = 0x10;
    private static final int INSTANT_ARGUMENT_COUNT = 10;
    private static final int TRIGGER_CHECK_REFERENCE_COUNT = 6;
    private static final int TRIGGER_INSTANT_REFERENCE_COUNT = 4;
    private static final int CELL_LAYER_TILE = 1;
    private static final int CELL_LAYER_HEIGHT = 2;
    private static final int CELL_LAYER_OBJECT = 3;
    static final int MISSION_ENTRY_DROP_INSTANT_TYPE = 0x10002;
    static final String MISSION_ENTRY_DROP_INSTANT_NAME = "Drop Location";
    private static final int MISSION_ENTRY_DROP_COORDINATE_MAX = 0xFF;
    private static final int SCENARIO_EFFECT_BYTE_VALUE_MAX = 0xFF;
    private static final int SCENARIO_EFFECT_TRANSIENT_SPELL_MODE_MAX = 3;
    private static final int SCENARIO_EFFECT_BUILDING_VIRTUAL_CASTER_MODE = 0x04;
    private static final int TERRAIN_TILE_RANDOM_BITS_MASK = 0x3F;
    private static final int TERRAIN_TILE_VARIANT_SHIFT = 4;
    private static final int TERRAIN_TILE_VARIANT_COUNT = 4;
    private static final int TERRAIN_TILE_FRAME_COUNT = 16;

    private final ScenarioDescriptor scenario;
    private final ArrayDeque<CellEditBatch> undoStack = new ArrayDeque<>();
    private final ArrayDeque<CellEditBatch> redoStack = new ArrayDeque<>();
    private Path sourcePath;
    private boolean dirty;

    /**
     * Java support constructor for editor documents.
     * not ported.
     */
    private MapEditorDocument(ScenarioDescriptor scenario, Path sourcePath, boolean dirty) {
        this.scenario = scenario;
        this.sourcePath = sourcePath;
        this.dirty = dirty;
    }

    /**
     * Java support factory for a new blank editor map.
     * not ported.
     */
    public static MapEditorDocument newBlank(int width, int height, int tileId, int heightValue) {
        ScenarioDescriptor scenario = new ScenarioDescriptor(width, height, tileId, heightValue);
        scenario.loaded = true;
        scenario.error = 0;
        scenario.useTiles = 0x1FFF;
        return new MapEditorDocument(scenario, null, true);
    }

    /**
     * Java support factory for a loaded editor map.
     * not ported.
     */
    public static MapEditorDocument loaded(ScenarioDescriptor scenario, Path sourcePath) {
        normalizeLoadedDescriptorIds(scenario);
        return new MapEditorDocument(scenario, sourcePath, false);
    }

    /**
     * Java support accessor for the loaded scenario descriptor.
     * not ported.
     */
    public ScenarioDescriptor scenario() {
        return scenario;
    }

    /**
     * Java support accessor for the source file path.
     * not ported.
     */
    public Path sourcePath() {
        return sourcePath;
    }

    /**
     * Java support dirty-state accessor for editor UI state.
     * not ported.
     */
    public boolean dirty() {
        return dirty;
    }

    /**
     * Java support undo-state accessor for editor-owned direct cell edits.
     * not ported.
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * Java support redo-state accessor for editor-owned direct cell edits.
     * not ported.
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * Java support metadata update for the editor's currently exposed map fields.
     * not ported.
     */
    public void applyMetadata(String mapName, String authors, int recommendedPlayers, int mapLevel) {
        scenario.mapName.set(mapName.getBytes(StandardCharsets.ISO_8859_1));
        scenario.authors.set(authors.getBytes(StandardCharsets.ISO_8859_1));
        scenario.recommendedPlayers = recommendedPlayers;
        scenario.mapLevel = mapLevel;
        dirty = true;
    }

    /**
     * Java support environment/light settings update for INFO-section fields exposed by the editor.
     * not ported.
     */
    public void applyEnvironmentSettings(
            int time,
            int brightness,
            int contrast,
            int useTiles,
            double sunAngleDegrees
    ) {
        scenario.time = time;
        scenario.darkness = brightness;
        scenario.contrast = contrast;
        scenario.useTiles = useTiles;
        scenario.sunAngle = (float) Math.toRadians(sunAngleDegrees);
        dirty = true;
    }

    /**
     * Java support cell edit for the native ALM tiles section.
     * not ported.
     */
    public void setTile(int tileX, int tileY, int tileId) {
        int index = cellIndex(tileX, tileY);
        recordCellEdit(CELL_LAYER_TILE, index, scenario.sec1Tiles[index] & 0xFFFF, tileId & 0xFFFF);
    }

    /**
     * Java support cell edit for the native ALM heights section.
     * not ported.
     */
    public void setHeight(int tileX, int tileY, int heightValue) {
        int index = cellIndex(tileX, tileY);
        recordCellEdit(CELL_LAYER_HEIGHT, index, Byte.toUnsignedInt(scenario.sec2Heights[index]), heightValue & 0xFF);
    }

    /**
     * Java support cell edit for the native ALM objects section.
     * not ported.
     */
    public void setObject(int tileX, int tileY, int objectValue) {
        int index = cellIndex(tileX, tileY);
        recordCellEdit(CELL_LAYER_OBJECT, index, Byte.toUnsignedInt(scenario.sec3Objects[index]), objectValue & 0xFF);
    }

    /**
     * Java support move/update command for one native ALM object cell.
     * not ported.
     */
    public void updateObjectCell(int previousTileX, int previousTileY, int tileX, int tileY, int objectValue) {
        int previousIndex = cellIndex(previousTileX, previousTileY);
        int nextIndex = cellIndex(tileX, tileY);
        int nextObjectValue = objectValue & 0xFF;
        List<CellEdit> edits = new ArrayList<>();
        int previousObjectValue = Byte.toUnsignedInt(scenario.sec3Objects[previousIndex]);
        if (previousIndex == nextIndex) {
            if (previousObjectValue != nextObjectValue) {
                edits.add(new CellEdit(CELL_LAYER_OBJECT, previousIndex, previousObjectValue, nextObjectValue));
            }
        } else {
            if (previousObjectValue != 0) {
                edits.add(new CellEdit(CELL_LAYER_OBJECT, previousIndex, previousObjectValue, 0));
            }
            int replacedObjectValue = Byte.toUnsignedInt(scenario.sec3Objects[nextIndex]);
            if (replacedObjectValue != nextObjectValue) {
                edits.add(new CellEdit(CELL_LAYER_OBJECT, nextIndex, replacedObjectValue, nextObjectValue));
            }
        }
        recordCellEditBatch(edits);
    }

    /**
     * Java support selected-area copy command for the currently editable ALM cell layers.
     * not ported.
     */
    MapEditorAreaClipboard copyArea(MapEditorAreaSelection selection) {
        ensureAreaSelection(selection);
        int cellCount = selection.cellCount();
        int[] tiles = new int[cellCount];
        int[] heights = new int[cellCount];
        int[] objects = new int[cellCount];
        int targetIndex = 0;
        for (int y = selection.top(); y <= selection.bottom(); y++) {
            for (int x = selection.left(); x <= selection.right(); x++) {
                int sourceIndex = cellIndex(x, y);
                tiles[targetIndex] = scenario.sec1Tiles[sourceIndex] & 0xFFFF;
                heights[targetIndex] = Byte.toUnsignedInt(scenario.sec2Heights[sourceIndex]);
                objects[targetIndex] = Byte.toUnsignedInt(scenario.sec3Objects[sourceIndex]);
                targetIndex++;
            }
        }
        return new MapEditorAreaClipboard(selection.width(), selection.height(), tiles, heights, objects);
    }

    /**
     * Native editor behavior mirrored from Cut handler @0041FFA0 and shared selected-area helper @004200B0.
     * Java support selected-area cut command for the currently editable ALM cell layers.
     * not ported.
     */
    MapEditorAreaClipboard cutArea(MapEditorAreaSelection selection) {
        MapEditorAreaClipboard clipboard = copyArea(selection);
        int fillCellIndex = cellIndex(0, 0);
        int fillTile = scenario.sec1Tiles[fillCellIndex] & 0xFFFF;
        int fillHeight = Byte.toUnsignedInt(scenario.sec2Heights[fillCellIndex]);
        List<CellEdit> edits = new ArrayList<>();
        for (int y = selection.top(); y <= selection.bottom(); y++) {
            for (int x = selection.left(); x <= selection.right(); x++) {
                int sourceIndex = cellIndex(x, y);
                appendCellEdit(
                        edits,
                        CELL_LAYER_TILE,
                        sourceIndex,
                        scenario.sec1Tiles[sourceIndex] & 0xFFFF,
                        fillTile
                );
                appendCellEdit(
                        edits,
                        CELL_LAYER_HEIGHT,
                        sourceIndex,
                        Byte.toUnsignedInt(scenario.sec2Heights[sourceIndex]),
                        fillHeight
                );
                appendCellEdit(
                        edits,
                        CELL_LAYER_OBJECT,
                        sourceIndex,
                        Byte.toUnsignedInt(scenario.sec3Objects[sourceIndex]),
                        0
                );
            }
        }
        recordCellEditBatch(edits);
        return clipboard;
    }

    /**
     * Java support selected-area paste command for the currently editable ALM cell layers.
     * not ported.
     */
    void pasteArea(MapEditorAreaClipboard clipboard, int targetTileX, int targetTileY) {
        ensureAreaClipboardFits(clipboard, targetTileX, targetTileY);
        List<CellEdit> edits = new ArrayList<>();
        for (int y = 0; y < clipboard.height(); y++) {
            for (int x = 0; x < clipboard.width(); x++) {
                int targetIndex = cellIndex(targetTileX + x, targetTileY + y);
                appendCellEdit(
                        edits,
                        CELL_LAYER_TILE,
                        targetIndex,
                        scenario.sec1Tiles[targetIndex] & 0xFFFF,
                        clipboard.tileAt(x, y)
                );
                appendCellEdit(
                        edits,
                        CELL_LAYER_HEIGHT,
                        targetIndex,
                        Byte.toUnsignedInt(scenario.sec2Heights[targetIndex]),
                        clipboard.heightAt(x, y)
                );
                appendCellEdit(
                        edits,
                        CELL_LAYER_OBJECT,
                        targetIndex,
                        Byte.toUnsignedInt(scenario.sec3Objects[targetIndex]),
                        clipboard.objectAt(x, y)
                );
            }
        }
        recordCellEditBatch(edits);
    }

    /**
     * Java support undo command for editor-owned direct cell edits.
     * not ported.
     */
    public void undo() {
        if (undoStack.isEmpty()) {
            throw new IllegalStateException("No map cell edit is available to undo.");
        }
        CellEditBatch batch = undoStack.pop();
        for (int i = batch.edits.length - 1; i >= 0; i--) {
            CellEdit edit = batch.edits[i];
            applyCellEditValue(edit.layer, edit.cellIndex, edit.previousValue);
        }
        redoStack.push(batch);
        dirty = true;
    }

    /**
     * Java support redo command for editor-owned direct cell edits.
     * not ported.
     */
    public void redo() {
        if (redoStack.isEmpty()) {
            throw new IllegalStateException("No map cell edit is available to redo.");
        }
        CellEditBatch batch = redoStack.pop();
        for (CellEdit edit : batch.edits) {
            applyCellEditValue(edit.layer, edit.cellIndex, edit.nextValue);
        }
        undoStack.push(batch);
        dirty = true;
    }

    /**
     * Java support full-map terrain tile randomization command for editor-owned native Help surface.
     * not ported.
     */
    public void randomizeTerrainVariants() {
        List<CellEdit> edits = new ArrayList<>();
        appendRandomTerrainVariantEdits(edits, RandomGenerator.getDefault());
        recordCellEditBatch(edits);
    }

    /**
     * Java support full-map altitude randomization command for editor-owned native Help surface.
     * not ported.
     */
    public void randomizeAltitude(int baseHeight, int spread) {
        List<CellEdit> edits = new ArrayList<>();
        appendRandomAltitudeEdits(edits, RandomGenerator.getDefault(), baseHeight, spread);
        recordCellEditBatch(edits);
    }

    /**
     * Java support full-map tile and altitude randomization command for editor-owned native Help surface.
     * not ported.
     */
    public void randomizeTerrainAndAltitude(int baseHeight, int spread) {
        List<CellEdit> edits = new ArrayList<>();
        RandomGenerator random = RandomGenerator.getDefault();
        appendRandomTerrainVariantEdits(edits, random);
        appendRandomAltitudeEdits(edits, random, baseHeight, spread);
        recordCellEditBatch(edits);
    }

    /**
     * Java support full-map altitude delta command for editor-owned native Help surface.
     * not ported.
     */
    public void adjustAltitude(int delta) {
        List<CellEdit> edits = new ArrayList<>();
        appendAltitudeDeltaEdits(edits, delta);
        recordCellEditBatch(edits);
    }

    /**
     * Java support accessor for the editable scenario-building count.
     * not ported.
     */
    public int buildingCount() {
        return scenario.sec4Buildings.size();
    }

    /**
     * Java support indexed scenario-building accessor for editor panels.
     * not ported.
     */
    public BuildingDTO buildingAt(int buildingIndex) {
        ensureBuildingIndex(buildingIndex);
        return scenario.sec4Buildings.get(buildingIndex);
    }

    /**
     * Java support scenario-building append command for the editor building list.
     * not ported.
     */
    public BuildingDTO addBuilding(int x, int y, int typeID, int hp, int playerID, int sizeX, int sizeY) {
        ensureMapCell(x, y);
        BuildingDTO building = new BuildingDTO(x, y, typeID, hp, playerID, nextScenarioObjectId(), sizeX, sizeY);
        scenario.sec4Buildings.add(building);
        dirty = true;
        return building;
    }

    /**
     * Java support scenario-building attribute update for editor controls.
     * not ported.
     */
    public void updateBuilding(
            int buildingIndex,
            int x,
            int y,
            int typeID,
            int hp,
            int playerID,
            int buildingID,
            int sizeX,
            int sizeY
    ) {
        ensureBuildingIndex(buildingIndex);
        ensureMapCell(x, y);
        ensureScenarioObjectIdAvailableForBuilding(buildingID, buildingIndex);
        BuildingDTO building = scenario.sec4Buildings.get(buildingIndex);
        building.x = x & 0xFFFF;
        building.y = y & 0xFFFF;
        building.typeID = typeID & 0xFFFF;
        building.hp = hp & 0xFFFF;
        building.playerID = playerID & 0xFFFF;
        building.buildingID = buildingID;
        building.sizeX = sizeX & 0xFFFF;
        building.sizeY = sizeY & 0xFFFF;
        dirty = true;
    }

    /**
     * Java support scenario-building delete command for the editor building list.
     * not ported.
     */
    public void deleteBuilding(int buildingIndex) {
        ensureBuildingIndex(buildingIndex);
        scenario.sec4Buildings.remove(buildingIndex);
        dirty = true;
    }

    /**
     * Java support accessor for the editable scenario-unit count.
     * not ported.
     */
    public int unitCount() {
        return scenario.sec6Units.size();
    }

    /**
     * Java support indexed scenario-unit accessor for editor panels.
     * not ported.
     */
    public UnitDTO unitAt(int unitIndex) {
        ensureUnitIndex(unitIndex);
        return scenario.sec6Units.get(unitIndex);
    }

    /**
     * Java support scenario-unit append command for the editor unit list.
     * not ported.
     */
    public UnitDTO addUnit(
            int tileX,
            int tileY,
            int typeID,
            int face,
            int serverID,
            int playerID,
            int sackIDX,
            int rotation,
            int hp,
            int maxHp,
            int unitFlags1,
            int questFlags,
            int groupID
    ) {
        ensureMapCell(tileX, tileY);
        UnitDTO unit = new UnitDTO();
        updateUnitFields(
                unit,
                tileX,
                tileY,
                typeID,
                face,
                serverID,
                playerID,
                sackIDX,
                rotation,
                hp,
                maxHp,
                nextScenarioObjectId(),
                unitFlags1,
                questFlags,
                groupID
        );
        scenario.sec6Units.add(unit);
        dirty = true;
        return unit;
    }

    /**
     * Java support scenario-unit attribute update for editor controls.
     * not ported.
     */
    public void updateUnit(
            int unitIndex,
            int tileX,
            int tileY,
            int typeID,
            int face,
            int serverID,
            int playerID,
            int sackIDX,
            int rotation,
            int hp,
            int maxHp,
            int unitID,
            int unitFlags1,
            int questFlags,
            int groupID
    ) {
        ensureUnitIndex(unitIndex);
        ensureMapCell(tileX, tileY);
        ensureScenarioObjectIdAvailableForUnit(unitID, unitIndex);
        updateUnitFields(
                scenario.sec6Units.get(unitIndex),
                tileX,
                tileY,
                typeID,
                face,
                serverID,
                playerID,
                sackIDX,
                rotation,
                hp,
                maxHp,
                unitID,
                unitFlags1,
                questFlags,
                groupID
        );
        dirty = true;
    }

    /**
     * Java support scenario-unit delete command for the editor unit list.
     * not ported.
     */
    public void deleteUnit(int unitIndex) {
        ensureUnitIndex(unitIndex);
        scenario.sec6Units.remove(unitIndex);
        dirty = true;
    }

    /**
     * Java support accessor for the editable scenario-group count.
     * not ported.
     */
    public int groupCount() {
        return scenario.sect10Groups.size();
    }

    /**
     * Java support indexed scenario-group accessor for editor panels.
     * not ported.
     */
    public GroupDTO groupAt(int groupIndex) {
        ensureGroupIndex(groupIndex);
        return scenario.sect10Groups.get(groupIndex);
    }

    /**
     * Java support scenario-group append command for the editor repop/group list.
     * not ported.
     */
    public GroupDTO addGroup(int repopTime, int flags, int instID) {
        GroupDTO group = new GroupDTO();
        updateGroupFields(group, nextGroupId(), repopTime, flags, instID);
        scenario.sect10Groups.add(group);
        refreshGroupsHighestId();
        dirty = true;
        return group;
    }

    /**
     * Java support scenario-group attribute update for editor controls.
     * not ported.
     */
    public void updateGroup(int groupIndex, int id, int repopTime, int flags, int instID) {
        ensureGroupIndex(groupIndex);
        updateGroupFields(scenario.sect10Groups.get(groupIndex), id, repopTime, flags, instID);
        refreshGroupsHighestId();
        dirty = true;
    }

    /**
     * Java support scenario-group delete command for the editor repop/group list.
     * not ported.
     */
    public void deleteGroup(int groupIndex) {
        ensureGroupIndex(groupIndex);
        scenario.sect10Groups.remove(groupIndex);
        refreshGroupsHighestId();
        dirty = true;
    }

    /**
     * Java support accessor for the editable scenario-sack count.
     * not ported.
     */
    public int sackCount() {
        return scenario.sect8Sacks.size();
    }

    /**
     * Java support indexed scenario-sack accessor for editor panels.
     * not ported.
     */
    public WorldSack sackAt(int sackIndex) {
        ensureSackIndex(sackIndex);
        return scenario.sect8Sacks.get(sackIndex);
    }

    /**
     * Java support scenario-sack append command for the editor items/bags list.
     * not ported.
     */
    public WorldSack addSack(int tileX, int tileY, int unitID, int gold) {
        ensureMapCell(tileX, tileY);
        WorldSack sack = new WorldSack();
        updateSackFields(sack, tileX, tileY, unitID, gold);
        scenario.sect8Sacks.add(sack);
        dirty = true;
        return sack;
    }

    /**
     * Java support scenario-sack attribute update for editor controls.
     * not ported.
     */
    public void updateSack(int sackIndex, int tileX, int tileY, int unitID, int gold) {
        ensureSackIndex(sackIndex);
        ensureMapCell(tileX, tileY);
        updateSackFields(scenario.sect8Sacks.get(sackIndex), tileX, tileY, unitID, gold);
        dirty = true;
    }

    /**
     * Java support scenario-sack delete command for the editor items/bags list.
     * not ported.
     */
    public void deleteSack(int sackIndex) {
        ensureSackIndex(sackIndex);
        scenario.sect8Sacks.remove(sackIndex);
        dirty = true;
    }

    /**
     * Java support accessor for a scenario-sack item count.
     * not ported.
     */
    public int sackItemCount(int sackIndex) {
        WorldSack sack = sackAt(sackIndex);
        ensureSackItemListsAligned(sack);
        return sack.itemPackedHashes.size();
    }

    /**
     * Java support scenario-sack item append command for the editor items/bags list.
     * not ported.
     */
    public void addSackItem(int sackIndex, int itemPackedHash, int incomingItemFlags, int effectIndex) {
        WorldSack sack = sackAt(sackIndex);
        ensureSackItemListsAligned(sack);
        updateSackItemFields(sack, sack.itemPackedHashes.size(), itemPackedHash, incomingItemFlags, effectIndex);
        dirty = true;
    }

    /**
     * Java support scenario-sack item update command for the editor items/bags list.
     * not ported.
     */
    public void updateSackItem(
            int sackIndex,
            int itemIndex,
            int itemPackedHash,
            int incomingItemFlags,
            int effectIndex
    ) {
        WorldSack sack = sackAt(sackIndex);
        ensureSackItemIndex(sack, itemIndex);
        updateSackItemFields(sack, itemIndex, itemPackedHash, incomingItemFlags, effectIndex);
        dirty = true;
    }

    /**
     * Java support scenario-sack item delete command for the editor items/bags list.
     * not ported.
     */
    public void deleteSackItem(int sackIndex, int itemIndex) {
        WorldSack sack = sackAt(sackIndex);
        ensureSackItemIndex(sack, itemIndex);
        sack.itemPackedHashes.remove(itemIndex);
        sack.incomingItemFlags.remove(itemIndex);
        sack.effectIndices.remove(itemIndex);
        dirty = true;
    }

    /**
     * Java support accessor for the editable scenario-effect count.
     * not ported.
     */
    public int effectCount() {
        return scenario.sect9Effects.size();
    }

    /**
     * Java support indexed scenario-effect accessor for editor panels.
     * not ported.
     */
    public EffectDTO effectAt(int effectIndex) {
        ensureEffectIndex(effectIndex);
        return scenario.sect9Effects.get(effectIndex);
    }

    /**
     * Java support scenario-effect append command for the editor effects/traps list.
     * not ported.
     */
    public EffectDTO addEffect(
            int itemID,
            int x,
            int y,
            int effectMode,
            int min,
            int spread,
            int spellId,
            int spellStrength
    ) {
        EffectDTO effect = new EffectDTO();
        updateEffectFields(effect, itemID, x, y, effectMode, min, spread, spellId, spellStrength);
        scenario.sect9Effects.add(effect);
        dirty = true;
        return effect;
    }

    /**
     * Java support native-style transient spell/trap cell helper consumed by ScenarioMapLoader's EFFECTS branch.
     * not ported.
     */
    public EffectDTO addTransientSpellCell(
            int cellX,
            int cellY,
            int effectMode,
            int spellId,
            int spellStrength,
            int sourceX,
            int sourceY,
            int targetX,
            int targetY
    ) {
        ensureMapCell(cellX, cellY);
        ensureScenarioEffectByteValue(cellX, "Cell X");
        ensureScenarioEffectByteValue(cellY, "Cell Y");
        ensureTransientSpellCellMode(effectMode);
        ensureScenarioEffectByteValue(spellId, "Spell");
        ensureScenarioEffectByteValue(spellStrength, "Strength");
        ensureScenarioEffectByteValue(sourceX, "Source X");
        ensureScenarioEffectByteValue(sourceY, "Source Y");
        ensureScenarioEffectByteValue(targetX, "Target X");
        ensureScenarioEffectByteValue(targetY, "Target Y");
        EffectDTO effect = addEffect(0, cellX, cellY, effectMode, 0, 0, spellId, spellStrength);
        appendEffectModifier(effect, sourceX, sourceY);
        appendEffectModifier(effect, targetX, targetY);
        dirty = true;
        return effect;
    }

    /**
     * Java support native-style building virtual-caster helper consumed by ScenarioMapLoader's EFFECTS branch.
     * not ported.
     */
    public EffectDTO addStructureCasting(
            int buildingScenarioId,
            int sourceX,
            int sourceY,
            int targetSearchRadius,
            int spellId,
            int spellStrength
    ) {
        ensurePositiveScenarioReference(buildingScenarioId, "Building id");
        ensureMapCell(sourceX, sourceY);
        ensureScenarioEffectByteValue(sourceX, "Source X");
        ensureScenarioEffectByteValue(sourceY, "Source Y");
        ensureScenarioEffectByteValue(targetSearchRadius, "Radius");
        ensureScenarioEffectByteValue(spellId, "Spell");
        ensureScenarioEffectByteValue(spellStrength, "Strength");
        return addEffect(
                targetSearchRadius,
                sourceX,
                sourceY,
                SCENARIO_EFFECT_BUILDING_VIRTUAL_CASTER_MODE,
                buildingScenarioId & 0xFFFF,
                (buildingScenarioId >>> 16) & 0xFFFF,
                spellId,
                spellStrength
        );
    }

    /**
     * Java support scenario-effect attribute update for editor controls.
     * not ported.
     */
    public void updateEffect(
            int effectIndex,
            int itemID,
            int x,
            int y,
            int effectMode,
            int min,
            int spread,
            int spellId,
            int spellStrength
    ) {
        ensureEffectIndex(effectIndex);
        updateEffectFields(
                scenario.sect9Effects.get(effectIndex),
                itemID,
                x,
                y,
                effectMode,
                min,
                spread,
                spellId,
                spellStrength
        );
        dirty = true;
    }

    /**
     * Java support scenario-effect delete command for the editor effects/traps list.
     * not ported.
     */
    public void deleteEffect(int effectIndex) {
        ensureEffectIndex(effectIndex);
        scenario.sect9Effects.remove(effectIndex);
        normalizeSackEffectIndicesAfterDelete(effectIndex + 1);
        dirty = true;
    }

    /**
     * Java support accessor for a scenario-effect modifier count.
     * not ported.
     */
    public int effectModifierCount(int effectIndex) {
        return effectAt(effectIndex).carr.size();
    }

    /**
     * Java support scenario-effect modifier append command.
     * not ported.
     */
    public void addEffectModifier(int effectIndex, int type, int value) {
        EffectDTO effect = effectAt(effectIndex);
        EffectOrTrapMod modifier = new EffectOrTrapMod();
        updateEffectModifierFields(modifier, type, value);
        effect.carr.add(modifier);
        dirty = true;
    }

    /**
     * Java support scenario-effect modifier update command.
     * not ported.
     */
    public void updateEffectModifier(int effectIndex, int modifierIndex, int type, int value) {
        EffectDTO effect = effectAt(effectIndex);
        ensureEffectModifierIndex(effect, modifierIndex);
        updateEffectModifierFields(effect.carr.get(modifierIndex), type, value);
        dirty = true;
    }

    /**
     * Java support scenario-effect modifier delete command.
     * not ported.
     */
    public void deleteEffectModifier(int effectIndex, int modifierIndex) {
        EffectDTO effect = effectAt(effectIndex);
        ensureEffectModifierIndex(effect, modifierIndex);
        effect.carr.remove(modifierIndex);
        dirty = true;
    }

    /**
     * Java support accessor for the editable scenario-inn-descriptor count.
     * not ported.
     */
    public int innDescriptorCount() {
        return scenario.sect11InnDescriptors.size();
    }

    /**
     * Java support indexed scenario-inn-descriptor accessor for editor panels.
     * not ported.
     */
    public InnDescriptor innDescriptorAt(int descriptorIndex) {
        ensureInnDescriptorIndex(descriptorIndex);
        return scenario.sect11InnDescriptors.get(descriptorIndex);
    }

    /**
     * Java support scenario-inn-descriptor append command for the editor descriptor list.
     * not ported.
     */
    public int addInnDescriptor(int id, int flags, int itemID) {
        InnDescriptor descriptor = firstInnDescriptorById(id);
        if (descriptor == null) {
            descriptor = new InnDescriptor();
            scenario.sect11InnDescriptors.add(descriptor);
        }
        updateInnDescriptorFields(descriptor, id, flags, itemID);
        int descriptorIndex = removeDuplicateInnDescriptorsKeeping(descriptor);
        dirty = true;
        return descriptorIndex;
    }

    /**
     * Java support scenario-inn-descriptor attribute update for editor controls.
     * not ported.
     */
    public int updateInnDescriptor(int descriptorIndex, int id, int flags, int itemID) {
        ensureInnDescriptorIndex(descriptorIndex);
        InnDescriptor descriptor = scenario.sect11InnDescriptors.get(descriptorIndex);
        updateInnDescriptorFields(descriptor, id, flags, itemID);
        int effectiveDescriptorIndex = removeDuplicateInnDescriptorsKeeping(descriptor);
        dirty = true;
        return effectiveDescriptorIndex;
    }

    /**
     * Java support scenario-inn-descriptor delete command for the editor descriptor list.
     * not ported.
     */
    public void deleteInnDescriptor(int descriptorIndex) {
        ensureInnDescriptorIndex(descriptorIndex);
        scenario.sect11InnDescriptors.remove(descriptorIndex);
        dirty = true;
    }

    /**
     * Java support accessor for the editable scenario-shop-descriptor count.
     * not ported.
     */
    public int shopDescriptorCount() {
        return scenario.sect11ShopDescriptors.size();
    }

    /**
     * Java support indexed scenario-shop-descriptor accessor for editor panels.
     * not ported.
     */
    public ShopDescriptor shopDescriptorAt(int descriptorIndex) {
        ensureShopDescriptorIndex(descriptorIndex);
        return scenario.sect11ShopDescriptors.get(descriptorIndex);
    }

    /**
     * Java support scenario-shop-descriptor append command for the editor descriptor list.
     * not ported.
     */
    public int addShopDescriptor(
            int id,
            int[] shelfFlags,
            int[] minPrices,
            int[] maxPrices,
            int[] maxItems,
            int[] maxSameTypeItems
    ) {
        ShopDescriptor descriptor = firstShopDescriptorById(id);
        if (descriptor == null) {
            descriptor = new ShopDescriptor();
            scenario.sect11ShopDescriptors.add(descriptor);
        }
        updateShopDescriptorFields(descriptor, id, shelfFlags, minPrices, maxPrices, maxItems, maxSameTypeItems);
        int descriptorIndex = removeDuplicateShopDescriptorsKeeping(descriptor);
        dirty = true;
        return descriptorIndex;
    }

    /**
     * Java support scenario-shop-descriptor attribute update for editor controls.
     * not ported.
     */
    public int updateShopDescriptor(
            int descriptorIndex,
            int id,
            int[] shelfFlags,
            int[] minPrices,
            int[] maxPrices,
            int[] maxItems,
            int[] maxSameTypeItems
    ) {
        ensureShopDescriptorIndex(descriptorIndex);
        ShopDescriptor descriptor = scenario.sect11ShopDescriptors.get(descriptorIndex);
        updateShopDescriptorFields(
                descriptor,
                id,
                shelfFlags,
                minPrices,
                maxPrices,
                maxItems,
                maxSameTypeItems
        );
        int effectiveDescriptorIndex = removeDuplicateShopDescriptorsKeeping(descriptor);
        dirty = true;
        return effectiveDescriptorIndex;
    }

    /**
     * Java support scenario-shop-descriptor delete command for the editor descriptor list.
     * not ported.
     */
    public void deleteShopDescriptor(int descriptorIndex) {
        ensureShopDescriptorIndex(descriptorIndex);
        scenario.sect11ShopDescriptors.remove(descriptorIndex);
        dirty = true;
    }

    /**
     * Java support accessor for the editable scenario-post-descriptor count.
     * not ported.
     */
    public int postDescriptorCount() {
        return scenario.sect11PostDescriptors.size();
    }

    /**
     * Java support indexed scenario-post-descriptor accessor for editor panels.
     * not ported.
     */
    public PostDescriptor postDescriptorAt(int descriptorIndex) {
        ensurePostDescriptorIndex(descriptorIndex);
        return scenario.sect11PostDescriptors.get(descriptorIndex);
    }

    /**
     * Java support scenario-post-descriptor append command for the editor descriptor list.
     * not ported.
     */
    public PostDescriptor addPostDescriptor(int id, int instanceOn, int instanceID) {
        PostDescriptor descriptor = new PostDescriptor();
        updatePostDescriptorFields(descriptor, id, instanceOn, instanceID);
        scenario.sect11PostDescriptors.add(descriptor);
        dirty = true;
        return descriptor;
    }

    /**
     * Java support scenario-post-descriptor attribute update for editor controls.
     * not ported.
     */
    public void updatePostDescriptor(int descriptorIndex, int id, int instanceOn, int instanceID) {
        ensurePostDescriptorIndex(descriptorIndex);
        updatePostDescriptorFields(scenario.sect11PostDescriptors.get(descriptorIndex), id, instanceOn, instanceID);
        dirty = true;
    }

    /**
     * Java support scenario-post-descriptor delete command for the editor descriptor list.
     * not ported.
     */
    public void deletePostDescriptor(int descriptorIndex) {
        ensurePostDescriptorIndex(descriptorIndex);
        scenario.sect11PostDescriptors.remove(descriptorIndex);
        dirty = true;
    }

    /**
     * Java support accessor for the editable scenario-instant count.
     * not ported.
     */
    public int instantCount() {
        return scenario.sect7Instants.size();
    }

    /**
     * Java support indexed scenario-instant accessor for editor panels.
     * not ported.
     */
    public Instant instantAt(int instantIndex) {
        ensureInstantIndex(instantIndex);
        return scenario.sect7Instants.get(instantIndex);
    }

    /**
     * Java support scenario-instant append command for the editor logic list.
     * not ported.
     */
    public Instant addInstant(
            String name,
            int typeId,
            int executeOnce,
            int[] argumentValues,
            int[] argumentTypes,
            String[] argumentNames
    ) {
        Instant instant = new Instant();
        updateInstantFields(
                instant,
                name,
                typeId,
                nextScriptRecordIndex(scenario.sect7Instants),
                executeOnce,
                argumentValues,
                argumentTypes,
                argumentNames
        );
        scenario.sect7Instants.add(instant);
        dirty = true;
        return instant;
    }

    /**
     * Java support native-editor Drop Location helper, consumed by ScenarioMapLoader's special instant type `0x10002`.
     * not ported.
     */
    public Instant addDropLocation(int tileX, int tileY) {
        ensureMapCell(tileX, tileY);
        ensureDropLocationCoordinate(tileX, "X");
        ensureDropLocationCoordinate(tileY, "Y");
        int[] argumentValues = new int[INSTANT_ARGUMENT_COUNT];
        int[] argumentTypes = new int[INSTANT_ARGUMENT_COUNT];
        String[] argumentNames = new String[INSTANT_ARGUMENT_COUNT];
        Arrays.fill(argumentNames, "");
        argumentValues[0] = tileX;
        argumentValues[1] = tileY;
        argumentNames[0] = "X";
        argumentNames[1] = "Y";
        return addInstant(
                MISSION_ENTRY_DROP_INSTANT_NAME,
                MISSION_ENTRY_DROP_INSTANT_TYPE,
                0,
                argumentValues,
                argumentTypes,
                argumentNames
        );
    }

    /**
     * Java support Drop Location coordinate validator matching ScenarioMapLoader's byte-sized drop-cell read.
     * not ported.
     */
    private static void ensureDropLocationCoordinate(int coordinate, String axis) {
        if (coordinate > MISSION_ENTRY_DROP_COORDINATE_MAX) {
            throw new IllegalArgumentException(axis + " must fit the mission-entry drop-cell byte domain.");
        }
    }

    /**
     * Java support scenario-instant attribute update for editor controls.
     * not ported.
     */
    public void updateInstant(
            int instantIndex,
            String name,
            int typeId,
            int index,
            int executeOnce,
            int[] argumentValues,
            int[] argumentTypes,
            String[] argumentNames
    ) {
        ensureInstantIndex(instantIndex);
        updateInstantFields(
                scenario.sect7Instants.get(instantIndex),
                name,
                typeId,
                index,
                executeOnce,
                argumentValues,
                argumentTypes,
                argumentNames
        );
        dirty = true;
    }

    /**
     * Java support scenario-instant delete command for the editor logic list.
     * not ported.
     */
    public void deleteInstant(int instantIndex) {
        ensureInstantIndex(instantIndex);
        scenario.sect7Instants.remove(instantIndex);
        dirty = true;
    }

    /**
     * Java support accessor for the editable scenario-check count.
     * not ported.
     */
    public int checkCount() {
        return scenario.sect7Checks.size();
    }

    /**
     * Java support indexed scenario-check accessor for editor panels.
     * not ported.
     */
    public Instant checkAt(int checkIndex) {
        ensureCheckIndex(checkIndex);
        return scenario.sect7Checks.get(checkIndex);
    }

    /**
     * Java support scenario-check append command for the editor logic list.
     * not ported.
     */
    public Instant addCheck(
            String name,
            int typeId,
            int executeOnce,
            int[] argumentValues,
            int[] argumentTypes,
            String[] argumentNames
    ) {
        Instant check = new Instant();
        updateInstantFields(
                check,
                name,
                typeId,
                nextScriptRecordIndex(scenario.sect7Checks),
                executeOnce,
                argumentValues,
                argumentTypes,
                argumentNames
        );
        scenario.sect7Checks.add(check);
        dirty = true;
        return check;
    }

    /**
     * Java support scenario-check attribute update for editor controls.
     * not ported.
     */
    public void updateCheck(
            int checkIndex,
            String name,
            int typeId,
            int index,
            int executeOnce,
            int[] argumentValues,
            int[] argumentTypes,
            String[] argumentNames
    ) {
        ensureCheckIndex(checkIndex);
        updateInstantFields(
                scenario.sect7Checks.get(checkIndex),
                name,
                typeId,
                index,
                executeOnce,
                argumentValues,
                argumentTypes,
                argumentNames
        );
        dirty = true;
    }

    /**
     * Java support scenario-check delete command for the editor logic list.
     * not ported.
     */
    public void deleteCheck(int checkIndex) {
        ensureCheckIndex(checkIndex);
        scenario.sect7Checks.remove(checkIndex);
        dirty = true;
    }

    /**
     * Java support accessor for the editable scenario-trigger count.
     * not ported.
     */
    public int triggerCount() {
        return scenario.sect7Triggers.size();
    }

    /**
     * Java support indexed scenario-trigger accessor for editor panels.
     * not ported.
     */
    public Trigger triggerAt(int triggerIndex) {
        ensureTriggerIndex(triggerIndex);
        return scenario.sect7Triggers.get(triggerIndex);
    }

    /**
     * Java support scenario-trigger append command for the editor logic list.
     * not ported.
     */
    public Trigger addTrigger(
            String description,
            int[] checkIds,
            int[] instantIds,
            int check12Operator,
            int check34Operator,
            int check56Operator,
            int runOnce
    ) {
        Trigger trigger = new Trigger();
        updateTriggerFields(
                trigger,
                description,
                checkIds,
                instantIds,
                check12Operator,
                check34Operator,
                check56Operator,
                runOnce
        );
        scenario.sect7Triggers.add(trigger);
        dirty = true;
        return trigger;
    }

    /**
     * Java support scenario-trigger attribute update for editor controls.
     * not ported.
     */
    public void updateTrigger(
            int triggerIndex,
            String description,
            int[] checkIds,
            int[] instantIds,
            int check12Operator,
            int check34Operator,
            int check56Operator,
            int runOnce
    ) {
        ensureTriggerIndex(triggerIndex);
        updateTriggerFields(
                scenario.sect7Triggers.get(triggerIndex),
                description,
                checkIds,
                instantIds,
                check12Operator,
                check34Operator,
                check56Operator,
                runOnce
        );
        dirty = true;
    }

    /**
     * Java support scenario-trigger delete command for the editor logic list.
     * not ported.
     */
    public void deleteTrigger(int triggerIndex) {
        ensureTriggerIndex(triggerIndex);
        scenario.sect7Triggers.remove(triggerIndex);
        dirty = true;
    }

    /**
     * Java support accessor for the editable scenario-player count.
     * not ported.
     */
    public int playerCount() {
        return scenario.sec5Players.size();
    }

    /**
     * Java support indexed scenario-player accessor for editor panels.
     * not ported.
     */
    public CPlayer playerAt(int playerIndex) {
        ensurePlayerIndex(playerIndex);
        return scenario.sec5Players.get(playerIndex);
    }

    /**
     * Java support scenario-player append command matching the native editor's 16-player limit.
     * not ported.
     */
    public CPlayer addPlayer() {
        if (scenario.sec5Players.size() >= MAX_PLAYERS) {
            throw new IllegalStateException("The native editor limits maps to 16 players.");
        }
        CPlayer player = new CPlayer(scenario.sec5Players.size() + 1, nextAvailablePlayerColor());
        int addedPlayerIndex = scenario.sec5Players.size();
        scenario.sec5Players.add(player);
        clearScenarioDiplomacySlot(addedPlayerIndex);
        renumberScenarioPlayers();
        scenario.recommendedPlayers = Math.max(scenario.recommendedPlayers, scenario.sec5Players.size());
        dirty = true;
        return player;
    }

    /**
     * Java support scenario-player delete command for the editor player list.
     * not ported.
     */
    public void deletePlayer(int playerIndex) {
        ensurePlayerIndex(playerIndex);
        if (scenario.sec5Players.size() <= 1) {
            throw new IllegalStateException("A scenario must keep at least one player.");
        }
        scenario.sec5Players.remove(playerIndex);
        removeScenarioDiplomacySlot(playerIndex);
        renumberScenarioPlayers();
        scenario.recommendedPlayers = Math.min(scenario.recommendedPlayers, scenario.sec5Players.size());
        dirty = true;
    }

    /**
     * Java support scenario-player attribute update for editor controls.
     * not ported.
     */
    public void updatePlayer(int playerIndex, String name, int color, int gold, int flags) {
        CPlayer player = playerAt(playerIndex);
        player.name.set(name.getBytes(StandardCharsets.ISO_8859_1));
        player.color = color;
        player.gold = gold;
        player.flags = flags;
        dirty = true;
    }

    /**
     * Java support diplomacy-word accessor for the scenario players section.
     * not ported.
     */
    public int diplomacyFlags(int sourcePlayerIndex, int targetPlayerIndex) {
        CPlayer sourcePlayer = playerAt(sourcePlayerIndex);
        int slot = scenarioDiplomacySlot(targetPlayerIndex);
        return ensureScenarioDiplomacyWords(sourcePlayer)[slot] & 0xFFFF;
    }

    /**
     * Java support diplomacy-word update for the scenario players section.
     * not ported.
     */
    public void setDiplomacy(
            int sourcePlayerIndex,
            int targetPlayerIndex,
            MapEditorDiplomacyRelation relation,
            boolean sharesVision
    ) {
        CPlayer sourcePlayer = playerAt(sourcePlayerIndex);
        int flags = relation.baseFlags();
        if (sharesVision) {
            flags |= CPlayer.DIPLOMACY_VISIBLE_MASK;
        }
        ensureScenarioDiplomacyWords(sourcePlayer)[scenarioDiplomacySlot(targetPlayerIndex)] = (short) flags;
        dirty = true;
    }

    /**
     * Java support accessor for the default scenario music record.
     * not ported.
     */
    public MusicDTO defaultMusic() {
        return scenario.defaultMusic;
    }

    /**
     * Java support accessor for the editable interactive music-area count.
     * not ported.
     */
    public int musicZoneCount() {
        return scenario.sect12Music.size();
    }

    /**
     * Java support indexed interactive music-area accessor for editor panels.
     * not ported.
     */
    public MusicDTO musicZoneAt(int musicZoneIndex) {
        ensureMusicZoneIndex(musicZoneIndex);
        return scenario.sect12Music.get(musicZoneIndex);
    }

    /**
     * Java support update for default map music fields.
     * not ported.
     */
    public void updateDefaultMusic(int x, int y, int radius, int m1, int m2, int m3, int m4) {
        updateMusicRecord(scenario.defaultMusic, x, y, radius, m1, m2, m3, m4);
        dirty = true;
    }

    /**
     * Java support append command for one interactive music area.
     * not ported.
     */
    public MusicDTO addMusicZone(int x, int y, int radius, int m1, int m2, int m3, int m4) {
        MusicDTO music = new MusicDTO();
        updateMusicRecord(music, x, y, radius, m1, m2, m3, m4);
        scenario.sect12Music.add(music);
        dirty = true;
        return music;
    }

    /**
     * Java support update for one interactive music area.
     * not ported.
     */
    public void updateMusicZone(int musicZoneIndex, int x, int y, int radius, int m1, int m2, int m3, int m4) {
        updateMusicRecord(musicZoneAt(musicZoneIndex), x, y, radius, m1, m2, m3, m4);
        dirty = true;
    }

    /**
     * Java support delete command for one interactive music area.
     * not ported.
     */
    public void deleteMusicZone(int musicZoneIndex) {
        ensureMusicZoneIndex(musicZoneIndex);
        scenario.sect12Music.remove(musicZoneIndex);
        dirty = true;
    }

    /**
     * Java support undo-entry recorder for direct editor cell mutations.
     * not ported.
     */
    private void recordCellEdit(int layer, int cellIndex, int previousValue, int nextValue) {
        if (previousValue == nextValue) {
            return;
        }
        recordCellEditBatch(List.of(new CellEdit(layer, cellIndex, previousValue, nextValue)));
    }

    /**
     * Java support undo-entry recorder for full-map editor cell mutations.
     * not ported.
     */
    private void recordCellEditBatch(List<CellEdit> edits) {
        if (edits.isEmpty()) {
            return;
        }
        CellEditBatch batch = new CellEditBatch(edits.toArray(CellEdit[]::new));
        for (CellEdit edit : batch.edits) {
            applyCellEditValue(edit.layer, edit.cellIndex, edit.nextValue);
        }
        undoStack.push(batch);
        redoStack.clear();
        dirty = true;
    }

    /**
     * Java support cell-edit list append helper for area paste commands.
     * not ported.
     */
    private static void appendCellEdit(List<CellEdit> edits, int layer, int cellIndex, int previousValue, int nextValue) {
        if (previousValue != nextValue) {
            edits.add(new CellEdit(layer, cellIndex, previousValue, nextValue));
        }
    }

    /**
     * Java support random terrain edit generation preserving each cell's terrain family/high flags.
     * not ported.
     */
    private void appendRandomTerrainVariantEdits(List<CellEdit> edits, RandomGenerator random) {
        int cellCount = scenario.mapWidth * scenario.mapHeight;
        for (int cellIndex = 0; cellIndex < cellCount; cellIndex++) {
            int previousValue = scenario.sec1Tiles[cellIndex] & 0xFFFF;
            int nextValue = (previousValue & ~TERRAIN_TILE_RANDOM_BITS_MASK)
                    | (random.nextInt(TERRAIN_TILE_VARIANT_COUNT) << TERRAIN_TILE_VARIANT_SHIFT)
                    | random.nextInt(TERRAIN_TILE_FRAME_COUNT);
            if (previousValue != nextValue) {
                edits.add(new CellEdit(CELL_LAYER_TILE, cellIndex, previousValue, nextValue));
            }
        }
    }

    /**
     * Java support random altitude edit generation around the editor's current paint height.
     * not ported.
     */
    private void appendRandomAltitudeEdits(List<CellEdit> edits, RandomGenerator random, int baseHeight, int spread) {
        int center = clampByteValue(baseHeight);
        int radius = clampByteValue(spread);
        int range = radius * 2 + 1;
        int cellCount = scenario.mapWidth * scenario.mapHeight;
        for (int cellIndex = 0; cellIndex < cellCount; cellIndex++) {
            int previousValue = Byte.toUnsignedInt(scenario.sec2Heights[cellIndex]);
            int nextValue = clampByteValue(center + random.nextInt(range) - radius);
            if (previousValue != nextValue) {
                edits.add(new CellEdit(CELL_LAYER_HEIGHT, cellIndex, previousValue, nextValue));
            }
        }
    }

    /**
     * Java support whole-map altitude shift edit generation for Help Page Up/Page Down commands.
     * not ported.
     */
    private void appendAltitudeDeltaEdits(List<CellEdit> edits, int delta) {
        int cellCount = scenario.mapWidth * scenario.mapHeight;
        for (int cellIndex = 0; cellIndex < cellCount; cellIndex++) {
            int previousValue = Byte.toUnsignedInt(scenario.sec2Heights[cellIndex]);
            int nextValue = clampByteValue(previousValue + delta);
            if (previousValue != nextValue) {
                edits.add(new CellEdit(CELL_LAYER_HEIGHT, cellIndex, previousValue, nextValue));
            }
        }
    }

    /**
     * Java support byte-domain clamp helper for editor-owned altitude commands.
     * not ported.
     */
    private static int clampByteValue(int value) {
        return Math.max(0, Math.min(0xFF, value));
    }

    /**
     * Java support raw cell-value writer used by undo/redo replay.
     * not ported.
     */
    private void applyCellEditValue(int layer, int cellIndex, int value) {
        switch (layer) {
            case CELL_LAYER_TILE -> scenario.sec1Tiles[cellIndex] = (short) value;
            case CELL_LAYER_HEIGHT -> scenario.sec2Heights[cellIndex] = (byte) value;
            case CELL_LAYER_OBJECT -> scenario.sec3Objects[cellIndex] = (byte) value;
            default -> throw new IllegalArgumentException("Unknown editor cell layer.");
        }
    }

    /**
     * Java support cell-index helper for editor surface mutations.
     * not ported.
     */
    private int cellIndex(int tileX, int tileY) {
        ensureMapCell(tileX, tileY);
        return tileY * scenario.mapWidth + tileX;
    }

    /**
     * Java support selected-area bounds validator for editor copy commands.
     * not ported.
     */
    private void ensureAreaSelection(MapEditorAreaSelection selection) {
        if (selection == null) {
            throw new IllegalArgumentException("No map area is selected.");
        }
        ensureMapCell(selection.left(), selection.top());
        ensureMapCell(selection.right(), selection.bottom());
    }

    /**
     * Java support selected-area paste bounds validator.
     * not ported.
     */
    private void ensureAreaClipboardFits(MapEditorAreaClipboard clipboard, int targetTileX, int targetTileY) {
        if (clipboard == null) {
            throw new IllegalArgumentException("No map area has been copied.");
        }
        ensureMapCell(targetTileX, targetTileY);
        ensureMapCell(targetTileX + clipboard.width() - 1, targetTileY + clipboard.height() - 1);
    }

    /**
     * Java support map-cell validator for editor commands.
     * not ported.
     */
    private void ensureMapCell(int tileX, int tileY) {
        if (tileX < 0 || tileY < 0 || tileX >= scenario.mapWidth || tileY >= scenario.mapHeight) {
            throw new IllegalArgumentException("Map cell is outside the current map.");
        }
    }

    /**
     * Java support building-index validator for editor commands.
     * not ported.
     */
    private void ensureBuildingIndex(int buildingIndex) {
        if (buildingIndex < 0 || buildingIndex >= scenario.sec4Buildings.size()) {
            throw new IllegalArgumentException("Building index is outside the current scenario building list.");
        }
    }

    /**
     * Java support unit-index validator for editor commands.
     * not ported.
     */
    private void ensureUnitIndex(int unitIndex) {
        if (unitIndex < 0 || unitIndex >= scenario.sec6Units.size()) {
            throw new IllegalArgumentException("Unit index is outside the current scenario unit list.");
        }
    }

    /**
     * Java support group-index validator for editor commands.
     * not ported.
     */
    private void ensureGroupIndex(int groupIndex) {
        if (groupIndex < 0 || groupIndex >= scenario.sect10Groups.size()) {
            throw new IllegalArgumentException("Group index is outside the current scenario group list.");
        }
    }

    /**
     * Java support sack-index validator for editor commands.
     * not ported.
     */
    private void ensureSackIndex(int sackIndex) {
        if (sackIndex < 0 || sackIndex >= scenario.sect8Sacks.size()) {
            throw new IllegalArgumentException("Sack index is outside the current scenario sack list.");
        }
    }

    /**
     * Java support sack-item-index validator for editor commands.
     * not ported.
     */
    private void ensureSackItemIndex(WorldSack sack, int itemIndex) {
        ensureSackItemListsAligned(sack);
        if (itemIndex < 0 || itemIndex >= sack.itemPackedHashes.size()) {
            throw new IllegalArgumentException("Sack item index is outside the current scenario sack item list.");
        }
    }

    /**
     * Java support integrity check for the parallel arrays used by the native sack section shape.
     * not ported.
     */
    private static void ensureSackItemListsAligned(WorldSack sack) {
        if (sack.itemPackedHashes.size() != sack.incomingItemFlags.size()
                || sack.itemPackedHashes.size() != sack.effectIndices.size()) {
            throw new IllegalStateException("Scenario sack item arrays are not aligned.");
        }
    }

    /**
     * Java support effect-index validator for editor commands.
     * not ported.
     */
    private void ensureEffectIndex(int effectIndex) {
        if (effectIndex < 0 || effectIndex >= scenario.sect9Effects.size()) {
            throw new IllegalArgumentException("Effect index is outside the current scenario effect list.");
        }
    }

    /**
     * Java support effect-modifier-index validator for editor commands.
     * not ported.
     */
    private static void ensureEffectModifierIndex(EffectDTO effect, int modifierIndex) {
        if (modifierIndex < 0 || modifierIndex >= effect.carr.size()) {
            throw new IllegalArgumentException("Effect modifier index is outside the current scenario effect modifier list.");
        }
    }

    /**
     * Java support inn-descriptor-index validator for editor commands.
     * not ported.
     */
    private void ensureInnDescriptorIndex(int descriptorIndex) {
        if (descriptorIndex < 0 || descriptorIndex >= scenario.sect11InnDescriptors.size()) {
            throw new IllegalArgumentException("Inn descriptor index is outside the current scenario descriptor list.");
        }
    }

    /**
     * Java support shop-descriptor-index validator for editor commands.
     * not ported.
     */
    private void ensureShopDescriptorIndex(int descriptorIndex) {
        if (descriptorIndex < 0 || descriptorIndex >= scenario.sect11ShopDescriptors.size()) {
            throw new IllegalArgumentException("Shop descriptor index is outside the current scenario descriptor list.");
        }
    }

    /**
     * Java support post-descriptor-index validator for editor commands.
     * not ported.
     */
    private void ensurePostDescriptorIndex(int descriptorIndex) {
        if (descriptorIndex < 0 || descriptorIndex >= scenario.sect11PostDescriptors.size()) {
            throw new IllegalArgumentException("Post descriptor index is outside the current scenario descriptor list.");
        }
    }

    /**
     * Java support instant-index validator for editor commands.
     * not ported.
     */
    private void ensureInstantIndex(int instantIndex) {
        if (instantIndex < 0 || instantIndex >= scenario.sect7Instants.size()) {
            throw new IllegalArgumentException("Instant index is outside the current scenario instant list.");
        }
    }

    /**
     * Java support check-index validator for editor commands.
     * not ported.
     */
    private void ensureCheckIndex(int checkIndex) {
        if (checkIndex < 0 || checkIndex >= scenario.sect7Checks.size()) {
            throw new IllegalArgumentException("Check index is outside the current scenario check list.");
        }
    }

    /**
     * Java support trigger-index validator for editor commands.
     * not ported.
     */
    private void ensureTriggerIndex(int triggerIndex) {
        if (triggerIndex < 0 || triggerIndex >= scenario.sect7Triggers.size()) {
            throw new IllegalArgumentException("Trigger index is outside the current scenario trigger list.");
        }
    }

    /**
     * Java support shared scenario object-id allocation for newly added editor building/unit records.
     * Native MapDescriptor::MapDescriptor @004A449C inserts buildings and units into the same object map keyed by
     * 0x6000 + scenario id, so these ids must be unique across both record kinds.
     * not ported.
     */
    private int nextScenarioObjectId() {
        int maxScenarioObjectId = 0;
        for (BuildingDTO building : scenario.sec4Buildings) {
            maxScenarioObjectId = Math.max(maxScenarioObjectId, building.buildingID);
        }
        for (UnitDTO unit : scenario.sec6Units) {
            maxScenarioObjectId = Math.max(maxScenarioObjectId, MapEditorUnitDisplay.nativeUnitId(unit));
        }
        if (maxScenarioObjectId >= Short.MAX_VALUE) {
            throw new IllegalStateException("No positive scenario object ids remain.");
        }
        return maxScenarioObjectId + 1;
    }

    /**
     * Java support validator for editor-assigned building ids that share the native scenario object-id namespace with
     * units.
     * not ported.
     */
    private void ensureScenarioObjectIdAvailableForBuilding(int buildingID, int buildingIndex) {
        ensurePositiveScenarioObjectId(buildingID);
        for (int index = 0; index < scenario.sec4Buildings.size(); index++) {
            if (index != buildingIndex && scenario.sec4Buildings.get(index).buildingID == buildingID) {
                throw new IllegalArgumentException("Scenario object id " + buildingID + " is already used by another building.");
            }
        }
        for (UnitDTO unit : scenario.sec6Units) {
            if (MapEditorUnitDisplay.nativeUnitId(unit) == buildingID) {
                throw new IllegalArgumentException("Scenario object id " + buildingID + " is already used by a unit.");
            }
        }
    }

    /**
     * Java support validator for editor-assigned unit ids that share the native scenario object-id namespace with
     * buildings.
     * not ported.
     */
    private void ensureScenarioObjectIdAvailableForUnit(int unitID, int unitIndex) {
        int nativeUnitId = MapEditorUnitDisplay.nativeUnitIdForStoredId(unitID);
        ensurePositiveScenarioObjectId(nativeUnitId);
        for (BuildingDTO building : scenario.sec4Buildings) {
            if (building.buildingID == nativeUnitId) {
                throw new IllegalArgumentException("Scenario object id " + nativeUnitId + " is already used by a building.");
            }
        }
        for (int index = 0; index < scenario.sec6Units.size(); index++) {
            if (index != unitIndex && MapEditorUnitDisplay.nativeUnitId(scenario.sec6Units.get(index)) == nativeUnitId) {
                throw new IllegalArgumentException("Scenario object id " + nativeUnitId + " is already used by another unit.");
            }
        }
    }

    /**
     * Java support validator for native signed-WORD scenario object ids used by MapDescriptor object-map keys.
     * not ported.
     */
    private static void ensurePositiveScenarioObjectId(int objectId) {
        if (objectId <= 0 || objectId > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Scenario object id must be between 1 and " + Short.MAX_VALUE + ".");
        }
    }

    /**
     * Java support scenario-group id allocation for newly added editor records.
     * not ported.
     */
    private int nextGroupId() {
        int maxGroupId = 0;
        for (GroupDTO group : scenario.sect10Groups) {
            maxGroupId = Math.max(maxGroupId, group.id);
        }
        if (maxGroupId == Integer.MAX_VALUE) {
            throw new IllegalStateException("No positive scenario group ids remain.");
        }
        return maxGroupId + 1;
    }

    /**
     * Java support scenario script-record id allocation for newly added instants and checks.
     * not ported.
     */
    private static int nextScriptRecordIndex(Iterable<Instant> records) {
        int maxRecordIndex = 0;
        for (Instant record : records) {
            maxRecordIndex = Math.max(maxRecordIndex, record.index);
        }
        if (maxRecordIndex == Integer.MAX_VALUE) {
            throw new IllegalStateException("No positive scenario script record ids remain.");
        }
        return maxRecordIndex + 1;
    }

    /**
     * Java support refresh for the ScenarioDescriptor highest group id cache after editor-owned group mutations.
     * not ported.
     */
    private void refreshGroupsHighestId() {
        int maxGroupId = 0;
        for (GroupDTO group : scenario.sect10Groups) {
            maxGroupId = Math.max(maxGroupId, group.id);
        }
        scenario.groupsHighestID = maxGroupId;
    }

    /**
     * Java support player-index validator for editor commands.
     * not ported.
     */
    private void ensurePlayerIndex(int playerIndex) {
        if (playerIndex < 0 || playerIndex >= scenario.sec5Players.size()) {
            throw new IllegalArgumentException("Player index is outside the current scenario player list.");
        }
    }

    /**
     * Java support diplomacy slot resolver for serialized scenario player records.
     * not ported.
     */
    private int scenarioDiplomacySlot(int targetPlayerIndex) {
        ensurePlayerIndex(targetPlayerIndex);
        if (targetPlayerIndex >= SCENARIO_DIPLOMACY_WORD_COUNT) {
            throw new IllegalArgumentException("Scenario diplomacy stores at most 16 player relation words.");
        }
        return targetPlayerIndex;
    }

    /**
     * Java support scenario diplomacy buffer sizing for editor-written player records.
     * not ported.
     */
    private static short[] ensureScenarioDiplomacyWords(CPlayer player) {
        if (player.diplomacyFlags.length < SCENARIO_DIPLOMACY_WORD_COUNT) {
            player.diplomacyFlags = Arrays.copyOf(player.diplomacyFlags, SCENARIO_DIPLOMACY_WORD_COUNT);
        }
        return player.diplomacyFlags;
    }

    /**
     * Java support diplomacy-column removal after deleting one scenario player.
     * not ported.
     */
    private void removeScenarioDiplomacySlot(int deletedPlayerIndex) {
        for (CPlayer player : scenario.sec5Players) {
            short[] diplomacyWords = ensureScenarioDiplomacyWords(player);
            int copyLength = SCENARIO_DIPLOMACY_WORD_COUNT - deletedPlayerIndex - 1;
            if (copyLength > 0) {
                System.arraycopy(
                        diplomacyWords,
                        deletedPlayerIndex + 1,
                        diplomacyWords,
                        deletedPlayerIndex,
                        copyLength
                );
            }
            diplomacyWords[SCENARIO_DIPLOMACY_WORD_COUNT - 1] = 0;
        }
    }

    /**
     * Java support diplomacy-column reset after adding one scenario player.
     * not ported.
     */
    private void clearScenarioDiplomacySlot(int addedPlayerIndex) {
        for (CPlayer player : scenario.sec5Players) {
            ensureScenarioDiplomacyWords(player)[addedPlayerIndex] = 0;
        }
    }

    /**
     * Java support color chooser for newly added scenario players.
     * not ported.
     */
    private int nextAvailablePlayerColor() {
        boolean[] usedColors = new boolean[MAX_PLAYERS];
        for (CPlayer player : scenario.sec5Players) {
            if (0 <= player.color && player.color < usedColors.length) {
                usedColors[player.color] = true;
            }
        }
        for (int color = 0; color < usedColors.length; color++) {
            if (!usedColors[color]) {
                return color;
            }
        }
        return scenario.sec5Players.size() % MAX_PLAYERS;
    }

    /**
     * Java support player-id normalization after editor-owned player list mutations.
     * not ported.
     */
    private void renumberScenarioPlayers() {
        for (int i = 0; i < scenario.sec5Players.size(); i++) {
            scenario.sec5Players.get(i).playerId = i + 1;
        }
        scenario.pCPlayer = scenario.sec5Players.getFirst();
    }

    /**
     * Java support music-zone index validator for editor commands.
     * not ported.
     */
    private void ensureMusicZoneIndex(int musicZoneIndex) {
        if (musicZoneIndex < 0 || musicZoneIndex >= scenario.sect12Music.size()) {
            throw new IllegalArgumentException("Music area index is outside the current scenario music list.");
        }
    }

    /**
     * Java support assignment helper for editor-owned music record mutations.
     * not ported.
     */
    private static void updateMusicRecord(MusicDTO music, int x, int y, int radius, int m1, int m2, int m3, int m4) {
        music.x = x;
        music.y = y;
        music.radius = radius;
        music.m1 = m1;
        music.m2 = m2;
        music.m3 = m3;
        music.m4 = m4;
    }

    /**
     * Java support assignment helper for editor-owned scenario-unit mutations.
     * not ported.
     */
    private static void updateUnitFields(
            UnitDTO unit,
            int tileX,
            int tileY,
            int typeID,
            int face,
            int serverID,
            int playerID,
            int sackIDX,
            int rotation,
            int hp,
            int maxHp,
            int unitID,
            int unitFlags1,
            int questFlags,
            int groupID
    ) {
        unit.x = tileX << 8;
        unit.y = tileY << 8;
        unit.typeID = typeID & 0xFFFF;
        unit.face = face & 0xFFFF;
        unit.serverID = serverID;
        unit.playerID = playerID & 0xFFFF;
        unit.sackIDX = sackIDX;
        unit.rotation = rotation;
        unit.hp = hp & 0xFFFF;
        unit.maxHp = maxHp & 0xFFFF;
        unit.unitID = unitID;
        unit.unitFlags1 = unitFlags1;
        unit.questFlags = questFlags;
        unit.groupID = groupID;
    }

    /**
     * Java support assignment helper for editor-owned scenario-group mutations.
     * not ported.
     */
    private static void updateGroupFields(GroupDTO group, int id, int repopTime, int flags, int instID) {
        group.id = id;
        group.repopTime = repopTime;
        group.flags = flags;
        group.instID = instID;
    }

    /**
     * Java support assignment helper for editor-owned scenario-sack mutations.
     * not ported.
     */
    private static void updateSackFields(WorldSack sack, int tileX, int tileY, int unitID, int gold) {
        sack.x = tileX << 8;
        sack.y = tileY << 8;
        sack.unitID = unitID;
        sack.gold = gold;
    }

    /**
     * Java support assignment helper for editor-owned scenario-sack item mutations.
     * not ported.
     */
    private static void updateSackItemFields(
            WorldSack sack,
            int itemIndex,
            int itemPackedHash,
            int incomingItemFlags,
            int effectIndex
    ) {
        if (itemIndex == sack.itemPackedHashes.size()) {
            sack.itemPackedHashes.add(itemPackedHash);
            sack.incomingItemFlags.add(incomingItemFlags & 0xFFFF);
            sack.effectIndices.add(effectIndex);
            return;
        }
        sack.itemPackedHashes.set(itemIndex, itemPackedHash);
        sack.incomingItemFlags.set(itemIndex, incomingItemFlags & 0xFFFF);
        sack.effectIndices.set(itemIndex, effectIndex);
    }

    /**
     * Java support assignment helper for editor-owned scenario-effect mutations.
     * not ported.
     */
    private static void updateEffectFields(
            EffectDTO effect,
            int itemID,
            int x,
            int y,
            int effectMode,
            int min,
            int spread,
            int spellId,
            int spellStrength
    ) {
        effect.itemID = itemID;
        effect.x = x;
        effect.y = y;
        effect.effectMode = effectMode & 0xFFFF;
        effect.min = min & 0xFFFF;
        effect.spread = spread & 0xFFFF;
        effect.spellId = spellId & 0xFFFF;
        effect.spellStrength = spellStrength & 0xFFFF;
    }

    /**
     * Java support assignment helper for editor-owned scenario-effect modifier mutations.
     * not ported.
     */
    private static void updateEffectModifierFields(EffectOrTrapMod modifier, int type, int value) {
        modifier.type = type & 0xFFFF;
        modifier.value = value;
    }

    /**
     * Java support append helper for EFFECTS modifier rows used by native-style editor helpers.
     * not ported.
     */
    private static void appendEffectModifier(EffectDTO effect, int type, int value) {
        EffectOrTrapMod modifier = new EffectOrTrapMod();
        updateEffectModifierFields(modifier, type, value);
        effect.carr.add(modifier);
    }

    /**
     * Java support byte-domain validator for scenario EFFECTS helper fields masked by ScenarioMapLoader.
     * not ported.
     */
    private static void ensureScenarioEffectByteValue(int value, String fieldName) {
        if (value < 0 || value > SCENARIO_EFFECT_BYTE_VALUE_MAX) {
            throw new IllegalArgumentException(fieldName + " must fit the scenario effect byte domain.");
        }
    }

    /**
     * Java support transient spell-cell mode validator matching ScenarioMapLoader's `< 4` mode branch.
     * not ported.
     */
    private static void ensureTransientSpellCellMode(int effectMode) {
        if (effectMode < 0 || effectMode > SCENARIO_EFFECT_TRANSIENT_SPELL_MODE_MAX) {
            throw new IllegalArgumentException("Trap mode must be between 0 and 3.");
        }
    }

    /**
     * Java support scenario object-reference validator for native-style EFFECTS helpers.
     * not ported.
     */
    private static void ensurePositiveScenarioReference(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be a positive scenario id.");
        }
    }

    /**
     * Java support reference cleanup for 1-based sack effect indices after deleting one effect.
     * not ported.
     */
    private void normalizeSackEffectIndicesAfterDelete(int removedEffectIndex) {
        for (WorldSack sack : scenario.sect8Sacks) {
            ensureSackItemListsAligned(sack);
            for (int itemIndex = 0; itemIndex < sack.effectIndices.size(); itemIndex++) {
                int effectIndex = sack.effectIndices.get(itemIndex);
                if (effectIndex == removedEffectIndex) {
                    sack.effectIndices.set(itemIndex, 0);
                } else if (effectIndex > removedEffectIndex) {
                    sack.effectIndices.set(itemIndex, effectIndex - 1);
                }
            }
        }
    }

    /**
     * Java support load-time cleanup for shop/inn descriptor ids that the runtime resolves by first match.
     * not ported.
     */
    private static void normalizeLoadedDescriptorIds(ScenarioDescriptor scenario) {
        removeDuplicateInnDescriptorIdsKeepingFirst(scenario.sect11InnDescriptors);
        removeDuplicateShopDescriptorIdsKeepingFirst(scenario.sect11ShopDescriptors);
    }

    /**
     * Java support first-match lookup for editor-owned inn descriptor upsert commands.
     * not ported.
     */
    private InnDescriptor firstInnDescriptorById(int id) {
        for (InnDescriptor descriptor : scenario.sect11InnDescriptors) {
            if (descriptor.id == id) {
                return descriptor;
            }
        }
        return null;
    }

    /**
     * Java support first-match lookup for editor-owned shop descriptor upsert commands.
     * not ported.
     */
    private ShopDescriptor firstShopDescriptorById(int id) {
        for (ShopDescriptor descriptor : scenario.sect11ShopDescriptors) {
            if (descriptor.id == id) {
                return descriptor;
            }
        }
        return null;
    }

    /**
     * Java support duplicate-inn cleanup matching runtime first-match descriptor application.
     * not ported.
     */
    private static void removeDuplicateInnDescriptorIdsKeepingFirst(List<InnDescriptor> descriptors) {
        Set<Integer> seenIds = new HashSet<>();
        descriptors.removeIf(descriptor -> !seenIds.add(descriptor.id));
    }

    /**
     * Java support duplicate-shop cleanup matching runtime first-match descriptor application.
     * not ported.
     */
    private static void removeDuplicateShopDescriptorIdsKeepingFirst(List<ShopDescriptor> descriptors) {
        Set<Integer> seenIds = new HashSet<>();
        descriptors.removeIf(descriptor -> !seenIds.add(descriptor.id));
    }

    /**
     * Java support duplicate-inn cleanup that keeps the descriptor just edited by the MapEditor.
     * not ported.
     */
    private int removeDuplicateInnDescriptorsKeeping(InnDescriptor keptDescriptor) {
        for (int i = scenario.sect11InnDescriptors.size() - 1; i >= 0; i--) {
            InnDescriptor descriptor = scenario.sect11InnDescriptors.get(i);
            if (descriptor != keptDescriptor && descriptor.id == keptDescriptor.id) {
                scenario.sect11InnDescriptors.remove(i);
            }
        }
        return scenario.sect11InnDescriptors.indexOf(keptDescriptor);
    }

    /**
     * Java support duplicate-shop cleanup that keeps the descriptor just edited by the MapEditor.
     * not ported.
     */
    private int removeDuplicateShopDescriptorsKeeping(ShopDescriptor keptDescriptor) {
        for (int i = scenario.sect11ShopDescriptors.size() - 1; i >= 0; i--) {
            ShopDescriptor descriptor = scenario.sect11ShopDescriptors.get(i);
            if (descriptor != keptDescriptor && descriptor.id == keptDescriptor.id) {
                scenario.sect11ShopDescriptors.remove(i);
            }
        }
        return scenario.sect11ShopDescriptors.indexOf(keptDescriptor);
    }

    /**
     * Java support assignment helper for editor-owned scenario-inn-descriptor mutations.
     * not ported.
     */
    private static void updateInnDescriptorFields(InnDescriptor descriptor, int id, int flags, int itemID) {
        descriptor.id = id;
        descriptor.flags = flags;
        descriptor.itemID = itemID;
    }

    /**
     * Java support assignment helper for editor-owned scenario-shop-descriptor mutations.
     * not ported.
     */
    private static void updateShopDescriptorFields(
            ShopDescriptor descriptor,
            int id,
            int[] shelfFlags,
            int[] minPrices,
            int[] maxPrices,
            int[] maxItems,
            int[] maxSameTypeItems
    ) {
        ensureShopDescriptorArrayLengths(shelfFlags, minPrices, maxPrices, maxItems, maxSameTypeItems);
        descriptor.id = id;
        System.arraycopy(shelfFlags, 0, descriptor.shelfFlags, 0, descriptor.shelfFlags.length);
        System.arraycopy(minPrices, 0, descriptor.minPrices, 0, descriptor.minPrices.length);
        System.arraycopy(maxPrices, 0, descriptor.maxPrices, 0, descriptor.maxPrices.length);
        System.arraycopy(maxItems, 0, descriptor.maxItems, 0, descriptor.maxItems.length);
        System.arraycopy(maxSameTypeItems, 0, descriptor.maxSameTypeItems, 0, descriptor.maxSameTypeItems.length);
    }

    /**
     * Java support shop-descriptor array-shape validator for editor commands.
     * not ported.
     */
    private static void ensureShopDescriptorArrayLengths(
            int[] shelfFlags,
            int[] minPrices,
            int[] maxPrices,
            int[] maxItems,
            int[] maxSameTypeItems
    ) {
        if (shelfFlags.length != 4
                || minPrices.length != 4
                || maxPrices.length != 4
                || maxItems.length != 4
                || maxSameTypeItems.length != 4) {
            throw new IllegalArgumentException("Shop descriptors require exactly four shelf values per field.");
        }
    }

    /**
     * Java support assignment helper for editor-owned scenario-post-descriptor mutations.
     * not ported.
     */
    private static void updatePostDescriptorFields(PostDescriptor descriptor, int id, int instanceOn, int instanceID) {
        descriptor.id = id;
        descriptor.instanceOn = instanceOn;
        descriptor.instanceID = instanceID;
    }

    /**
     * Java support assignment helper for editor-owned scenario-instant/check mutations.
     * not ported.
     */
    private static void updateInstantFields(
            Instant instant,
            String name,
            int typeId,
            int index,
            int executeOnce,
            int[] argumentValues,
            int[] argumentTypes,
            String[] argumentNames
    ) {
        ensureInstantArgumentArrayLengths(argumentValues, argumentTypes, argumentNames);
        instant.name.set(name.getBytes(StandardCharsets.ISO_8859_1));
        instant.typeId = typeId;
        instant.index = index;
        instant.executeOnce = executeOnce;
        for (int i = 0; i < instant.arguments.length; i++) {
            instant.arguments[i].value = argumentValues[i];
            instant.arguments[i].type = argumentTypes[i];
            instant.arguments[i].name.set(argumentNames[i].getBytes(StandardCharsets.ISO_8859_1));
        }
    }

    /**
     * Java support instant/check argument array-shape validator for editor commands.
     * not ported.
     */
    private static void ensureInstantArgumentArrayLengths(
            int[] argumentValues,
            int[] argumentTypes,
            String[] argumentNames
    ) {
        if (argumentValues.length != INSTANT_ARGUMENT_COUNT
                || argumentTypes.length != INSTANT_ARGUMENT_COUNT
                || argumentNames.length != INSTANT_ARGUMENT_COUNT) {
            throw new IllegalArgumentException("Instant and check records require exactly ten argument slots.");
        }
    }

    /**
     * Java support assignment helper for editor-owned scenario-trigger mutations.
     * not ported.
     */
    private static void updateTriggerFields(
            Trigger trigger,
            String description,
            int[] checkIds,
            int[] instantIds,
            int check12Operator,
            int check34Operator,
            int check56Operator,
            int runOnce
    ) {
        ensureTriggerReferenceArrayLengths(checkIds, instantIds);
        trigger.description.set(description.getBytes(StandardCharsets.ISO_8859_1));
        System.arraycopy(checkIds, 0, trigger.checkIds, 0, trigger.checkIds.length);
        System.arraycopy(instantIds, 0, trigger.instantIds, 0, trigger.instantIds.length);
        trigger.check12Operator = check12Operator;
        trigger.check34Operator = check34Operator;
        trigger.check56Operator = check56Operator;
        trigger.runOnce = runOnce;
    }

    /**
     * Java support trigger reference array-shape validator for editor commands.
     * not ported.
     */
    private static void ensureTriggerReferenceArrayLengths(int[] checkIds, int[] instantIds) {
        if (checkIds.length != TRIGGER_CHECK_REFERENCE_COUNT
                || instantIds.length != TRIGGER_INSTANT_REFERENCE_COUNT) {
            throw new IllegalArgumentException("Triggers require six check references and four instant references.");
        }
    }

    /**
     * Java support save payload selection for map-editor save-as.
     * not ported.
     */
    public byte[] toAlmBytes() {
        if (!dirty && scenario.payload.length > 0) {
            return scenario.payload.clone();
        }
        return MapEditorScenarioIO.toAlmBytes(scenario);
    }

    /**
     * Java support save-state update after a successful save-as.
     * not ported.
     */
    public void markSavedAs(Path path, byte[] payload) {
        sourcePath = path;
        scenario.payload = payload.clone();
        dirty = false;
        scenario.requestedPath = path.toString();
        scenario.resolvedPath = path.toAbsolutePath().normalize().toString();
    }

    /**
     * Java support immutable undo entry for one editor cell-mutation command.
     * not ported.
     */
    private static final class CellEditBatch {
        private final CellEdit[] edits;

        /**
         * Java support constructor for an editor cell undo batch.
         * not ported.
         */
        private CellEditBatch(CellEdit[] edits) {
            this.edits = edits;
        }
    }

    /**
     * Java support immutable undo entry for one direct editor cell mutation.
     * not ported.
     */
    private static final class CellEdit {
        private final int layer;
        private final int cellIndex;
        private final int previousValue;
        private final int nextValue;

        /**
         * Java support constructor for a direct editor cell undo entry.
         * not ported.
         */
        private CellEdit(int layer, int cellIndex, int previousValue, int nextValue) {
            this.layer = layer;
            this.cellIndex = cellIndex;
            this.previousValue = previousValue;
            this.nextValue = nextValue;
        }
    }
}
