package ua.millfreedom.rom2.model.world;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.BuildingInfo;
import ua.millfreedom.rom2.model.CGameLighting;
import ua.millfreedom.rom2.model.CPlayer;
import ua.millfreedom.rom2.model.CUnitInfo;
import ua.millfreedom.rom2.model.UnitTypes;
import ua.millfreedom.rom2.model.column.BuildingColumn;
import ua.millfreedom.rom2.model.column.HumanColumn;
import ua.millfreedom.rom2.model.column.UnitColumn;
import ua.millfreedom.rom2.model.enums.BuildingId;
import ua.millfreedom.rom2.model.gameobj.CAirUnit;
import ua.millfreedom.rom2.model.gameobj.CHorisontalWoodenBridge;
import ua.millfreedom.rom2.model.gameobj.CStructure;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.gameobj.CVerticalWoodenBridge;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.unit.UnitInfo;
import ua.millfreedom.rom2.model.unit.humanoid.human.Human;
import ua.millfreedom.rom2.model.unit.humanoid.human.HumanInfo;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.model.world.scenario.BuildingDTO;
import ua.millfreedom.rom2.model.world.scenario.UnitDTO;

import java.util.List;

public final class MapDescriptor implements MfcSerializable {
    private static final int SCENARIO_OBJECT_ID_BASE = 0x6000;
    private static final int SCENARIO_HUMANOID_UNIT_FLAG = 0x10;
    private static final int DEFAULT_TERRAIN_TILE_MASK = 0x1FFF;
    private static final int OBJECT_COPY_LEFT_GUARD = 8;
    private static final int OBJECT_COPY_RIGHT_GUARD = 8;
    private static final int OBJECT_COPY_TOP_GUARD = 8;
    private static final int OBJECT_COPY_BOTTOM_GUARD = 4;
    private static final int HIDDEN_TILE_MASK = 0x2000;
    private static final int MAX_TERRAIN_LIGHT = 94;
    private static final int DEFAULT_SHADOW_DISTANCE_SCALE = 32;
    private static final double TERRAIN_LIGHT_REFERENCE_ANGLE = 0.5235987666666667d;
    private static final int VISIBILITY_BORDER_GUARD_TILES = 7;

    //0x00
    public int width;

    //0x04
    public int height;

    //0x08 native flat W*H row-major buffer.
    public short[] tilesWxH = new short[0];

    //0x0C native flat W*H row-major buffer.
    public byte[] heightsWxH = new byte[0];

    //0x10 native flat W*H row-major buffer.
    public byte[] objectsWxH = new byte[0];

    //0x14 native flat W*H row-major buffer.
    public byte[] terrainLightWxH = new byte[0];

    //0x18
    public byte brightness;

    //0x19
    public byte shadowContrast;

    //0x1C
    public double sunAngle;

    //0x24
    public int terrainTileMask;

    //0x28
    public int time;

    /**
     * vtbl +0x08: CObject::Serialize @00401970.
     */
    @Override
    public void serialize(CArchive ar) {
        // Native CObject::Serialize is a no-op.
    }

    /**
     * Native: MapDescriptor::MapDescriptor @004A449C.
     * Fully ported. Java keeps the loaded ScenarioDescriptor as the backing owner for native global music records.
     */
    public MapDescriptor(String scenarioPath) {
        ScenarioDescriptor scenario = new ScenarioDescriptor(scenarioPath);
        if (!scenario.loaded) {
            throw new IllegalStateException("Failed to load scenario " + scenarioPath + ": error " + scenario.error);
        }

        width = scenario.mapWidth;
        height = scenario.mapHeight;
        sunAngle = scenario.sunAngle;
        time = scenario.time;
        brightness = (byte) scenario.darkness;
        shadowContrast = (byte) scenario.contrast;
        terrainTileMask = DEFAULT_TERRAIN_TILE_MASK;

        tilesWxH = new short[nativeMapSectionBufferLength()];
        System.arraycopy(scenario.getTiles(), 0, tilesWxH, 0, mapCellCount());
        heightsWxH = scenario.sec2Heights.clone();
        objectsWxH = new byte[width * height];
        terrainLightWxH = new byte[nativeMapSectionBufferLength()];

        copyInteriorObjects(scenario.sec3Objects);
        installScenarioMusicRecords(scenario);
        materializeScenarioBuildings(scenario);
        materializeScenarioUnits(scenario);
        clearHiddenTerrainBits();
    }

    /**
     * Native: MapDescriptor::GetWidth @0041E950.
     * Fully ported.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Native: MapDescriptor::GetHeight @0041E970.
     * Fully ported.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Native: MapDescriptor::GetTilesWxH @0041E8B0.
     * Fully ported.
     */
    public short[] getTilesWxH() {
        return tilesWxH;
    }

    /**
     * Native: MapDescriptor::GetHeightsWxH @0041E8D0.
     * Fully ported.
     */
    public byte[] getHeightsWxH() {
        return heightsWxH;
    }

    /**
     * Native: MapDescriptor::GetObjectsWxH @0041E8F0.
     * Fully ported.
     */
    public byte[] getObjectsWxH() {
        return objectsWxH;
    }

    /**
     * Native: MapDescriptor::GetTerrainLightWxH @0041E910 and @0041E930.
     * Fully ported.
     */
    public byte[] getTerrainLightWxH() {
        return terrainLightWxH;
    }

    /**
     * Native: MapDescriptor::IsValidVisibilityTile @0041E990.
     * Fully ported.
     */
    public boolean isValidVisibilityTile(int tileX, int tileY) {
        return tileX >= VISIBILITY_BORDER_GUARD_TILES
                && tileY >= VISIBILITY_BORDER_GUARD_TILES
                && tileX < width - VISIBILITY_BORDER_GUARD_TILES
                && tileY < height - VISIBILITY_BORDER_GUARD_TILES;
    }

    /**
     * Native: MapDescriptor::GetShadowAngle @004A4429.
     * Fully ported.
     */
    public double getShadowAngle() {
        if (sunAngle >= 0.0 || sunAngle <= -0.05) {
            if (sunAngle <= 0.0 || sunAngle >= 0.05) {
                return sunAngle / 1.5;
            }
            return 0.05;
        }
        return -0.05;
    }

    /**
     * Native: MapDescriptor::RecalculateTerrainLighting @004A5DD2.
     * Fully ported.
     */
    public void recalculateTerrainLighting(int startX, int startY, int areaWidth, int areaHeight) {
        CGameLighting lighting = Globals.lighting;
        sunAngle = lighting.sunAngleRad;
        brightness = (byte) lighting.brightness;
        shadowContrast = (byte) lighting.shadowContrast;

        if (areaWidth == 0 && areaHeight == 0) {
            areaWidth = width - 2;
            areaHeight = height - 2;
        }
        int endX = startX + areaWidth;
        int endY = startY + areaHeight;
        double absSunAngle = Math.abs(sunAngle);
        double sunSlope = Math.tan(absSunAngle);
        double distanceScale = DEFAULT_SHADOW_DISTANCE_SCALE / Math.cos(absSunAngle);

        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                int lightForward = computeTerrainLight(computeForwardHeightDelta(x, y, sunSlope), distanceScale);
                int lightBackward = computeTerrainLight(computeBackwardHeightDelta(x, y, sunSlope), distanceScale);
                terrainLightWxH[tileIndex(x, y)] = (byte) ((lightForward + lightBackward) / 2);
            }
        }
    }

    /**
     * Native support extracted from MapDescriptor::MapDescriptor @004A449C.
     */
    private void copyInteriorObjects(byte[] scenarioObjects) {
        for (int y = OBJECT_COPY_TOP_GUARD; y < height - OBJECT_COPY_BOTTOM_GUARD; y++) {
            for (int x = OBJECT_COPY_LEFT_GUARD; x < width - OBJECT_COPY_RIGHT_GUARD; x++) {
                objectsWxH[tileIndex(x, y)] = scenarioObjects[tileIndex(x, y)];
            }
        }
    }

    /**
     * Native support extracted from MapDescriptor::MapDescriptor @004A449C and FUN_004A64B0.
     * Fully ported support. Native appends MusicDTO record pointers into global `g_CArray<MusicDTO>` and clears the
     * descriptor list; Java keeps the loaded ScenarioDescriptor as the backing music/diplomacy owner used by runtime
     * support.
     */
    private static void installScenarioMusicRecords(ScenarioDescriptor scenario) {
        Globals.gameServer.scenarioDescriptor = scenario;
    }

    /**
     * Native support extracted from MapDescriptor::MapDescriptor @004A449C scenario-building loop.
     */
    private void materializeScenarioBuildings(ScenarioDescriptor scenario) {
        MapVisualObject mapVisualObject = Globals.mainWindow.pMapVisualObject;
        for (BuildingDTO building : scenario.sec4Buildings) {
            CStructure structure = createScenarioStructure(building);
            structure.m_id = building.buildingID + SCENARIO_OBJECT_ID_BASE;
            structure.pMapVisualObject = mapVisualObject;
            structure.type = building.typeID;
            structure.location.x = (building.x << 8) + 0x80;
            structure.location.y = (building.y << 8) + 0x80;
            structure.z = 0;
            structure.cPlayer = resolveScenarioClientPlayer(mapVisualObject.clientPlayers, building.playerID);
            applyScenarioBuildingStats(structure, building);
            structure.location2.x = structure.location.x;
            structure.location2.y = structure.location.y;
            mapVisualObject.putScenarioObject(scenarioObjectKey(structure.m_id), structure);
        }
    }

    /**
     * Native support extracted from MapDescriptor::MapDescriptor @004A449C scenario-building type branch.
     */
    private static CStructure createScenarioStructure(BuildingDTO building) {
        if (building.typeID == BuildingId.VERTICAL_WOODEN_BRIDGE.id) {
            return new CVerticalWoodenBridge(building.sizeX, building.sizeY);
        }
        if (building.typeID == BuildingId.HORISONTAL_WOODEN_BRIDGE.id) {
            return new CHorisontalWoodenBridge(building.sizeX, building.sizeY);
        }
        return new CStructure();
    }

    /**
     * Native support extracted from MapDescriptor::MapDescriptor @004A449C building stats copy.
     */
    private static void applyScenarioBuildingStats(CStructure structure, BuildingDTO building) {
        BuildingInfo buildingInfo = Globals.staticDataMgr.buildings.get(building.typeID);
        structure.packedSightRange = (short) (buildingInfo.getAttribute(BuildingColumn.SCAN_RANGE) << 8);
        int maxHp = buildingInfo.getAttribute(BuildingColumn.HEALTH_MAX);
        if (maxHp == 0) {
            maxHp = 1000;
        }
        structure.MaxHP = (short) maxHp;
        structure.HP = building.hp == 0 ? 0 : structure.MaxHP;
    }

    /**
     * Native support extracted from MapDescriptor::MapDescriptor @004A449C scenario-unit loop.
     */
    private void materializeScenarioUnits(ScenarioDescriptor scenario) {
        MapVisualObject mapVisualObject = Globals.mainWindow.pMapVisualObject;
        for (UnitDTO scenarioUnit : scenario.sec6Units) {
            Unit runtimeUnit;
            CUnit visualUnit;
            if ((scenarioUnit.unitFlags1 & SCENARIO_HUMANOID_UNIT_FLAG) == 0) {
                runtimeUnit = createScenarioMonsterUnit(scenarioUnit);
                visualUnit = createScenarioVisualUnit(scenarioUnit.typeID);
            } else {
                Human human = createScenarioHumanUnit(scenarioUnit);
                runtimeUnit = human;
                visualUnit = new CUnit();
            }

            visualUnit.m_id = ((short) scenarioUnit.unitID) + SCENARIO_OBJECT_ID_BASE;
            visualUnit.pMapVisualObject = mapVisualObject;
            visualUnit.questFlags = scenarioUnit.groupID;
            applyHeroVisualTypeOverride(runtimeUnit);
            visualUnit.copyFromRuntimeUnit(runtimeUnit);
            visualUnit.location.x = scenarioUnit.x;
            visualUnit.location.y = scenarioUnit.y;
            visualUnit.location2.x = visualUnit.location.x;
            visualUnit.location2.y = visualUnit.location.y;
            visualUnit.cPlayer = resolveScenarioClientPlayer(mapVisualObject.clientPlayers, scenarioUnit.playerID);
            mapVisualObject.putScenarioObject(scenarioObjectKey(visualUnit.m_id), visualUnit);
            visualUnit.unitFlags |= 0x80;
        }
    }

    /**
     * Native support extracted from MapDescriptor::MapDescriptor @004A449C non-humanoid Unit::Unit branch.
     */
    private static Unit createScenarioMonsterUnit(UnitDTO scenarioUnit) {
        Unit unit = new Unit();
        int unitInfoIndex = Globals.staticDataMgr.findUnitByServerId(scenarioUnit.serverID);
        unit.key = (unitInfoIndex) & 0xFFFF;
        UnitInfo unitInfo = Globals.staticDataMgr.units.get(unitInfoIndex);
        unit.applyUnitInfoValues(unitInfo);
        if (unitInfo.values.size() > 0x37) {
            unit.serverID = unitInfo.getAttribute(UnitColumn.SERVER_ID) & 0xFFFF;
        }
        applyMultiplayerDifficultyScaling(unit);
        applyScenarioHealthOverrides(unit, scenarioUnit, false);
        return unit;
    }

    /**
     * Native support extracted from MapDescriptor::MapDescriptor @004A449C visual CUnit/CAirUnit branch.
     */
    private static CUnit createScenarioVisualUnit(int unitTypeId) {
        CUnitInfo unitInfo = UnitTypes.getUnitInfo(unitTypeId);
        if (unitInfo.m_ZOffset == 0) {
            return new CUnit();
        }
        return new CAirUnit();
    }

    /**
     * Native support extracted from MapDescriptor::MapDescriptor @004A449C humanoid Human::initializeDefaultTemplate branch.
     */
    private static Human createScenarioHumanUnit(UnitDTO scenarioUnit) {
        Human human = new Human().initializeDefaultTemplate();
        int humanInfoIndex = Globals.staticDataMgr.findHumanByServerId(scenarioUnit.serverID);
        human.applyScenarioHumanInfo(humanInfoIndex);
        applyScenarioHealthOverrides(human, scenarioUnit, true);
        HumanInfo humanInfo = Globals.staticDataMgr.humans.get(humanInfoIndex);
        human.addScenarioEquipment(humanInfo);
        return human;
    }

    /**
     * Native support extracted from MapDescriptor::MapDescriptor @004A449C multiplayer difficulty branch.
     */
    private static void applyMultiplayerDifficultyScaling(Unit unit) {
        if (Globals.mainWindow.sessionMode != CMainWindow.SESSION_MODE_CAMPAIGN
                || Globals.gameServer.difficultyLevelSetting == 2) {
            return;
        }
        if (Globals.gameServer.difficultyLevelSetting == 1) {
            unit.m_nMaxHP = (int) (unit.m_nMaxHP * 0.5d);
            unit.m_nHP = unit.m_nMaxHP;
            return;
        }
        unit.skillData.toHit += 0x32;
        unit.unitStatData.defence += 0x32;
        unit.m_nMaxHP = (int) (unit.m_nMaxHP * 1.5d);
        unit.m_nHP = unit.m_nMaxHP;
    }

    /**
     * Native support extracted from MapDescriptor::MapDescriptor @004A449C scenario HP/max-HP override branch.
     */
    private static void applyScenarioHealthOverrides(Unit unit, UnitDTO scenarioUnit, boolean humanoid) {
        int scenarioHp = signedScenarioShort(scenarioUnit.hp);
        int scenarioMaxHp = signedScenarioShort(scenarioUnit.maxHp);
        if (humanoid
                && (scenarioUnit.unitFlags1 & SCENARIO_HUMANOID_UNIT_FLAG) != 0
                && scenarioMaxHp != -1
                && scenarioMaxHp == scenarioHp) {
            scenarioHp = -1;
            scenarioMaxHp = -1;
        }
        if (scenarioMaxHp > 0) {
            unit.m_nMaxHP = scenarioMaxHp;
        }
        if (scenarioHp != -1) {
            unit.m_nHP = scenarioHp;
        }
    }

    /**
     * Native support extracted from MapDescriptor::MapDescriptor @004A449C hero visual type override.
     */
    private static void applyHeroVisualTypeOverride(Unit runtimeUnit) {
        if (runtimeUnit.serverID <= 0x14 || runtimeUnit.serverID >= 0x18) {
            return;
        }
        int humanoidTypeBase = runtimeUnit.unitInfoLine.getValue(HumanColumn.IS_FEMALE.index);
        runtimeUnit.typeID = (humanoidTypeBase + 0x21 + (runtimeUnit.m_nMaxMP > 0 ? 2 : 0)) & 0xFFFF;
        runtimeUnit.face &= 0x3F;
    }

    /**
     * Native support extracted from MapDescriptor::MapDescriptor @004A449C player-resource lookup.
     */
    private static CPlayer resolveScenarioClientPlayer(List<CPlayer> players, int playerId) {
        CPlayer resolved = players.get(0);
        int networkSlotId = (short) playerId;
        for (int i = 1; i < players.size(); i++) {
            CPlayer candidate = players.get(i);
            if (candidate != null && candidate.networkSlotId == networkSlotId) {
                resolved = candidate;
            }
        }
        return resolved;
    }

    /**
     * Native support extracted from MapDescriptor::MapDescriptor @004A449C object-map key writes.
     */
    private static short scenarioObjectKey(int objectId) {
        return (short) (objectId & 0xFFFF);
    }

    /**
     * Native support extracted from MapDescriptor::MapDescriptor @004A449C signed scenario WORD checks.
     */
    private static int signedScenarioShort(int value) {
        return (short) (value & 0xFFFF);
    }

    /**
     * Native support extracted from MapDescriptor::MapDescriptor @004A449C.
     */
    private void clearHiddenTerrainBits() {
        for (int i = 0; i < mapCellCount(); i++) {
            tilesWxH[i] = (short) (tilesWxH[i] & ~HIDDEN_TILE_MASK);
        }
    }

    /**
     * Native support extracted from MapDescriptor::RecalculateTerrainLighting @004A5DD2.
     * Fully ported.
     */
    private double computeForwardHeightDelta(int x, int y, double sunSlope) {
        if (sunAngle >= 0.0) {
            int ridgeHeight = signedHeight(x, y + 1);
            return (ridgeHeight - (ridgeHeight - signedHeight(x - 1, y + 1)) * sunSlope)
                    - signedHeight(x, y);
        }

        int ridgeHeight = signedHeight(x, y + 1);
        return ((signedHeight(x + 1, y + 1) - ridgeHeight) * sunSlope + ridgeHeight)
                - signedHeight(x, y);
    }

    /**
     * Native support extracted from MapDescriptor::RecalculateTerrainLighting @004A5DD2.
     * Fully ported.
     */
    private double computeBackwardHeightDelta(int x, int y, double sunSlope) {
        if (sunAngle >= 0.0) {
            int ridgeHeight = signedHeight(x, y - 1);
            return signedHeight(x, y) - ((signedHeight(x + 1, y - 1) - ridgeHeight) * sunSlope + ridgeHeight);
        }

        int ridgeHeight = signedHeight(x, y - 1);
        return signedHeight(x, y) - (ridgeHeight - (ridgeHeight - signedHeight(x - 1, y - 1)) * sunSlope);
    }

    /**
     * Native support extracted from MapDescriptor::RecalculateTerrainLighting @004A5DD2.
     * Fully ported.
     */
    private int computeTerrainLight(double heightDelta, double distanceScale) {
        double light = Byte.toUnsignedInt(brightness)
                + 0x20
                + (Byte.toUnsignedInt(shadowContrast) >> 1)
                - Byte.toUnsignedInt(shadowContrast)
                * Math.sin(TERRAIN_LIGHT_REFERENCE_ANGLE - Math.atan2(heightDelta, distanceScale));
        return Math.max(0, Math.min(MAX_TERRAIN_LIGHT, (int) light));
    }

    /**
     * Native support extracted from MapDescriptor::RecalculateTerrainLighting @004A5DD2.
     * Fully ported.
     */
    private int signedHeight(int x, int y) {
        return heightAt(x, y);
    }

    /**
     * Native support extracted from MapDescriptor::RecalculateTerrainLighting @004A5DD2 and
     * MapVisualObject::RenderFrame @00406F43 flat map-section indexing.
     */
    public int tileIndex(int x, int y) {
        return x + y * width;
    }

    /**
     * Native support extracted from MapDescriptor::MapDescriptor @004A449C flat map-section allocation sizes.
     */
    public int mapCellCount() {
        return width * height;
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 bottom-neighbor guard reads.
     */
    private int nativeMapSectionBufferLength() {
        return mapCellCount() + width + 1;
    }

    /**
     * Native support extracted from MapDescriptor::GetTilesWxH @0041E8B0 flat WORD reads.
     */
    public int tileWordAt(int x, int y) {
        return Short.toUnsignedInt(tilesWxH[tileIndex(x, y)]);
    }

    /**
     * Native support extracted from MapDescriptor::GetTilesWxH @0041E8B0 flat WORD reads.
     */
    public int tileWordFlatAt(int flatIndex) {
        return Short.toUnsignedInt(tilesWxH[flatIndex]);
    }

    /**
     * Native support extracted from MapDescriptor::GetTilesWxH @0041E8B0 flat WORD writes.
     */
    public void setTileWordAt(int x, int y, int value) {
        tilesWxH[tileIndex(x, y)] = (short) value;
    }

    /**
     * Native support extracted from MapDescriptor::GetTilesWxH @0041E8B0 flat WORD writes.
     */
    public void setTileWordFlatAt(int flatIndex, int value) {
        tilesWxH[flatIndex] = (short) value;
    }

    /**
     * Native support extracted from MapDescriptor::GetHeightsWxH @0041E8D0 flat signed-byte reads.
     */
    public int heightAt(int x, int y) {
        return heightsWxH[tileIndex(x, y)];
    }

    /**
     * Native support extracted from MapDescriptor::GetObjectsWxH @0041E8F0 flat BYTE reads.
     */
    public int objectIdFlatAt(int flatIndex) {
        return Byte.toUnsignedInt(objectsWxH[flatIndex]);
    }

    /**
     * Native support extracted from MapDescriptor::GetTerrainLightWxH @0041E910 flat BYTE reads.
     */
    public int terrainLightAt(int x, int y) {
        return Byte.toUnsignedInt(terrainLightWxH[tileIndex(x, y)]);
    }
}
