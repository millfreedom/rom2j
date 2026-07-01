package ua.millfreedom.rom2.model.world;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.gameserver.MissionScriptRuntime;
import ua.millfreedom.rom2.gameserver.missionruntime.PerfMonitorState;
import ua.millfreedom.rom2.gameserver.missionruntime.PrivateProfileReader;
import ua.millfreedom.rom2.model.*;
import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.model.enums.SpellId;
import ua.millfreedom.rom2.model.spell.AreaEffect;
import ua.millfreedom.rom2.model.spell.Spell;
import ua.millfreedom.rom2.model.spell.TransientSpellCastSpec;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.world.node.WorldMapNode;
import ua.millfreedom.rom2.model.world.node.WorldMapNodeMap;
import ua.millfreedom.rom2.res.ResInHeap;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Logical Java port of CWorldMap serialization path used by GameServer::Serialize:
 * - CWorldMap__serialize / FUN_00550008 at 0x00550008
 * - CMap<short,UNK0x3c>::Serialize / FUN_0055f6b0 at 0x0055f6b0
 * - pointer restore traversal / FUN_005595e5 + FUN_005596ff at 0x005595e5 + 0x005596ff
 */
public final class CWorldMap implements MfcSerializable {
    private static final String MAP_REG_PATH = "World/Data/map.reg";
    private static final String TERRAIN_SECTION = "Terrain";
    private static final String PATH_FINDING_SECTION = "Path Finding";
    private static final int TILE_SCAN_START = 0x0807;
    private static final int TILE_SCAN_END_EXCLUSIVE = 0xEDEE;
    private static final int TILE_ARRAY_SIZE = 0x10000;
    private static final byte LAYER_BORDER_BLOCK = 0x1F;

    // not native; Java scenario metadata input.
    public ScenarioDescriptor descriptor;

    //0x00000
    public final byte[] layer0_0x00000 = new byte[TILE_ARRAY_SIZE];
    //0x10000
    public final byte[] layer1_0x10000 = new byte[TILE_ARRAY_SIZE];
    //0x20000
    public final byte[] layer2_0x20000 = new byte[TILE_ARRAY_SIZE];
    //0x30000
    public final short[] layer3_0x30000 = new short[TILE_ARRAY_SIZE];

    //0x50000
    public int mapWidth0x50000 = 0x10;
    //0x50004
    public int mapHeight0x50004 = 0x10;
    //0x50008
    public final byte[] pathFrontierX0x50008 = new byte[0x1000];
    //0x51008
    public final byte[] pathFrontierY0x51008 = new byte[0x1000];
    //0x52008
    public final byte[] nextPathFrontierX0x52008 = new byte[0x1000];
    //0x53008
    public final byte[] nextPathFrontierY0x53008 = new byte[0x1000];
    //0x54008
    public int pathFrontierCount0x54008;
    //0x5400A
    public int nextPathFrontierCount0x5400A;

    //0x5400C
    public final UnitList embeddedUnitList0x5400C = new UnitList();
    //0x5402C
    public final WorldMapNode scratchNode0x5402C = new WorldMapNode();
    //0x54068
    public final CustomList<Short> shortWorkList0x54068 = CustomList.std(Short.class);
    //0x54084
    public final WorldMapNodeMap nodeMap = new WorldMapNodeMap();

    //0x540A6
    public final byte[][] transitionBlendMode0x540A6 = new byte[4][16];
    //0x54126
    public final Point8[] transitionTerrainPairs0x54126 = new Point8[16];
    //0x54146
    public byte costNoPass0x54146;
    //0x54147
    public byte costLand0x54147;
    //0x54148
    public byte costGrass0x54148;
    //0x54149
    public byte costFlowers0x54149;
    //0x5414A
    public byte costSand0x5414A;
    //0x5414B
    public byte costCracked0x5414B;
    //0x5414C
    public byte costStones0x5414C;
    //0x5414D
    public byte costSavanna0x5414D;
    //0x5414E
    public byte costMountain0x5414E;
    //0x5414F
    public byte costWater0x5414F;
    //0x54150
    public byte costRoad0x54150;
    //0x54156
    public final Point8[][] cardinalStepSets0x54156 = new Point8[3][4];
    //0x54188
    public int pathFindingField0x54188;
    //0x5418C
    public int pathFindingField0x5418C;
    //0x54190
    public final WorldMapSearchScratchState searchScratchState0x54190 = new WorldMapSearchScratchState();

    //0x5859C
    public final PrivateProfileReader privateProfileReader = new PrivateProfileReader();
    //0x58D80
    public MissionScriptRuntime missionScriptRuntime0x58D80;
    //0x58D84
    public int speedMultiplier0x58D84;
    //0x58D88
    public final WorldMapNode scratchNode2_0x58D88 = new WorldMapNode();
    //0x58DC8
    public final PerfMonitorState perfMonitor1_0x58DC8 = new PerfMonitorState();
    //0x58DF8
    public final PerfMonitorState perfMonitor2_0x58DF8 = new PerfMonitorState();
    //0x58E28
    public final PerfMonitorState perfMonitor3_0x58E28 = new PerfMonitorState();
    //0x58E58
    public final PerfMonitorState perfMonitor4_0x58E58 = new PerfMonitorState();
    //0x58E88
    public final NeighborStepTable neighborStepTable0x58E88 = new NeighborStepTable();
    //0x58EB8
    public final Rect16 rect0x58EB8 = new Rect16();
    //0x58EBC
    public int rectLtPacked0x58EBC;
    //0x58EBE
    public int rectRbPacked0x58EBE;
    //0x58EC0
    public final VisionAndDistance visionAndDistance0x58EC0 = new VisionAndDistance();
    //0x92ECC
    public final WorldMapUnitVisibilityState unitVisibilityState0x92ECC = new WorldMapUnitVisibilityState();
    //0x944F4
    public final byte[] unkByteArray0x944F4 = new byte[TILE_ARRAY_SIZE];
    //0xA44F4
    public final CustomList<Object> unknownObjectList0xA44F4 = CustomList.std(Object.class);
    //0xA4510
    public final CustomList<Short> sackInteractionCells = CustomList.std(Short.class);
    //0xA452C
    public final byte[] array64Bytes0xA452C = new byte[64];
    //0xA456C
    public UnitList activeUnits0xA456C;

    // not native; Java lookup cache.
    public final Map<Integer, Building> buildingByCell = new HashMap<>();
    // not native; Java lookup cache.
    public final Map<Integer, Sack> sackByCell = new HashMap<>();
    // not native; Java lookup cache.
    public final Map<Integer, Unit> unitByCell = new HashMap<>();

    /**
     * Native support extracted from CWorldMap constructors @0054CE60, @0054CFAF, @0054D104, and @0054D2D9.
     */
    public CWorldMap() {
        for (int i = 0; i < transitionTerrainPairs0x54126.length; i++) {
            transitionTerrainPairs0x54126[i] = new Point8();
        }
        for (int row = 0; row < cardinalStepSets0x54156.length; row++) {
            for (int col = 0; col < cardinalStepSets0x54156[row].length; col++) {
                cardinalStepSets0x54156[row][col] = new Point8();
            }
        }
        searchScratchState0x54190.initialize();
        privateProfileReader.initialize();
        perfMonitor1_0x58DC8.initialize();
        perfMonitor2_0x58DF8.initialize();
        perfMonitor3_0x58E28.initialize();
        perfMonitor4_0x58E58.initialize();
        visionAndDistance0x58EC0.pCWorldMap = this;
    }

    /**
     * Native: CWorldMap::CWorldMap @0054D2D9.
     * Fully ported.
     */
    public CWorldMap(ScenarioDescriptor descriptor, UnitList activeUnits) {
        this();
        this.descriptor = descriptor;
        initializeRuntimeState(activeUnits);
        initializeFromScenario(descriptor);
    }

    /**
     * Native: CWorldMap::CWorldMap @0054D104.
     * Fully ported.
     */
    public CWorldMap(String scenarioPath, UnitList activeUnits) {
        this();
        initializeRuntimeState(activeUnits);
        descriptor = new ScenarioDescriptor(scenarioPath);
        initializeFromScenario(descriptor);
    }

    /**
     * Native support extracted from CWorldMap::CWorldMap @0054D2D9,
     * CWorldMap::initializeRuntimeState @00553D51, CWorldMap::initializeFromScenario @00554626, and
     * CWorldMap::serialize @00550008 save/load rebinding.
     */
    public void attachRuntimeInputs(ScenarioDescriptor descriptor, UnitList activeUnits) {
        this.descriptor = descriptor;
        initializeRuntimeStatePreservingWorldNodes(activeUnits);
        initializeFromScenario(descriptor);
    }

    /**
     * Native: CWorldMap::attachMissionScriptRuntime @00558F4B.
     * Fully ported.
     */
    public void attachMissionScriptRuntime(MissionScriptRuntime missionScriptRuntime) {
        missionScriptRuntime0x58D80 = missionScriptRuntime;
    }

    /**
     * Native: CWorldMap::GetMapWidth @00550D9F.
     * Fully ported.
     */
    public int getMapWidth() {
        return mapWidth0x50000;
    }

    /**
     * Native: CWorldMap::GetMapHeight @00550DB3.
     * Fully ported.
     */
    public int getMapHeight() {
        return mapHeight0x50004;
    }

    /**
     * Native: CWorldMap::IsFullyInside @00559536.
     * Fully ported.
     */
    public boolean isFullyInside(int x, int y) {
        int xByte = x & 0xFF;
        int yByte = y & 0xFF;
        return xByte >= (rect0x58EB8.lt.x & 0xFF)
                && xByte <= (rect0x58EB8.rb.x & 0xFF)
                && yByte >= (rect0x58EB8.lt.y & 0xFF)
                && yByte <= (rect0x58EB8.rb.y & 0xFF);
    }

    /**
     * Native: CWorldMap::GetRangeInTiles @00556DCD.
     * Fully ported.
     */
    public int getRangeInTiles(Token first, Token second) {
        int firstSize = first.getTokenSizeVirtual() & 0xFF;
        int secondSize = second.getTokenSizeVirtual() & 0xFF;
        int firstCenterXdX = (first.m_pTargetHandle.packXdX() + (firstSize - 1) * 0x80) & 0xFFFF;
        int secondCenterXdX = (second.m_pTargetHandle.packXdX() + (secondSize - 1) * 0x80) & 0xFFFF;
        int dx = Math.abs(firstCenterXdX - secondCenterXdX) - (firstSize + secondSize) * 0x80;
        if (dx < 0) {
            dx = 0;
        }

        int firstCenterYdY = (first.m_pTargetHandle.packYdY() + (firstSize - 1) * 0x80) & 0xFFFF;
        int secondCenterYdY = (second.m_pTargetHandle.packYdY() + (secondSize - 1) * 0x80) & 0xFFFF;
        int dy = Math.abs(firstCenterYdY - secondCenterYdY) - (firstSize + secondSize) * 0x80;
        if (dy < 0) {
            dy = 0;
        }

        int distance = Math.max(dx, dy);
        return (distance >> 8) + 1;
    }

    /**
     * Native: CWorldMap::hasTokenFootprintOverlap @0054F6FD.
     * Fully ported.
     */
    public boolean hasTokenFootprintOverlap(Token first, Token second) {
        int firstX = first.m_pTargetHandle.getX() & 0xFF;
        int firstY = first.m_pTargetHandle.getY() & 0xFF;
        int secondX = second.m_pTargetHandle.getX() & 0xFF;
        int secondY = second.m_pTargetHandle.getY() & 0xFF;
        int firstSize = first.getTokenSizeVirtual() & 0xFF;
        int secondSize = second.getTokenSizeVirtual() & 0xFF;

        boolean xOverlap = false;
        if (secondX <= ((firstX + firstSize) & 0xFF)
                && ((firstX - 1) & 0xFF) < secondX + secondSize) {
            xOverlap = true;
        }
        boolean yOverlap = false;
        if (secondY <= ((firstY + firstSize) & 0xFF)
                && ((firstY - 1) & 0xFF) < secondY + secondSize) {
            yOverlap = true;
        }
        return xOverlap && yOverlap;
    }

    /**
     * Native: CWorldMap::collectUnitsAroundCell @0054EC6A.
     * Fully ported.
     */
    public UnitList collectUnitsAroundCell(int sourceCell, int radius) {
        embeddedUnitList0x5400C.clear();
        addGroundUnitAtCell(sourceCell);
        int radiusByte = radius & 0xFF;
        for (int ring = 1; ring < radiusByte + 1; ring++) {
            for (int delta = -ring; delta < ring + 1; delta++) {
                addGroundUnitAtCell(sourceCell + ring * 0x100 + delta);
                addGroundUnitAtCell(sourceCell - ring * 0x100 + delta);
                if (Math.abs(delta) != ring) {
                    addGroundUnitAtCell(sourceCell + delta * 0x100 + ring);
                    addGroundUnitAtCell(sourceCell + delta * 0x100 - ring);
                }
            }
        }
        return embeddedUnitList0x5400C;
    }

    /**
     * Native: CWorldMap::collectActiveUnitsNearUnitSquare @0054EB80.
     * Fully ported.
     */
    private UnitList collectActiveUnitsNearUnitSquare(Unit unit, int radius) {
        embeddedUnitList0x5400C.clear();
        int radiusByte = radius & 0xFF;
        for (Unit activeUnit : activeUnits0xA456C) {
            int dx = Math.abs((activeUnit.m_pTargetHandle.getX() & 0xFF) - (unit.m_pTargetHandle.getX() & 0xFF));
            if (dx < radiusByte) {
                int dy = Math.abs((activeUnit.m_pTargetHandle.getY() & 0xFF) - (unit.m_pTargetHandle.getY() & 0xFF));
                if (dy < radiusByte) {
                    embeddedUnitList0x5400C.add(activeUnit);
                }
            }
        }
        return embeddedUnitList0x5400C;
    }

    /**
     * Native support extracted from CWorldMap::collectUnitsAroundCell @0054EC6A.
     */
    private void addGroundUnitAtCell(int cell) {
        Unit unit = getGroundUnitAtCell(cell);
        if (unit != null) {
            embeddedUnitList0x5400C.add(unit);
        }
    }

    /**
     * Native: CWorldMap::getGroundUnitAtCell @00551EDB and
     * CWorldMap::getGroundUnitAtCellForEffectsAndMissions @0055201A.
     * Fully ported.
     */
    public Unit getGroundUnitAtCell(int cell) {
        int packedCell = cell & 0xFFFF;
        if ((layer1_0x10000[packedCell] & 0x20) == 0) {
            return null;
        }
        WorldMapNode node = lookupNode(packedCell);
        if (node == null) {
            return null;
        }
        scratchNode0x5402C.copyFrom(node);
        return scratchNode0x5402C.groundOccupancyUnit;
    }

    /**
     * Native: CWorldMap::getGroundUnitAtTargetHandle @0055641C.
     * Fully ported.
     */
    public Unit getGroundUnitAtTargetHandle(TargetHandle targetHandle) {
        return getGroundUnitAtCell(targetHandle.getCell());
    }

    /**
     * Native: CWorldMap::getAirUnitAtCell @00552079.
     * Fully ported.
     */
    public Unit getAirUnitAtCell(int cell) {
        int packedCell = cell & 0xFFFF;
        if ((layer1_0x10000[packedCell] & 0x20) == 0) {
            return null;
        }
        WorldMapNode node = lookupNode(packedCell);
        if (node == null) {
            return null;
        }
        scratchNode0x5402C.copyFrom(node);
        return scratchNode0x5402C.airOccupancyUnit;
    }

    /**
     * Native: CWorldMap::getBuildingAtCell @00559BC9.
     * Fully ported.
     */
    public Building getBuildingAtCell(int cell) {
        int packedCell = cell & 0xFFFF;
        if ((layer1_0x10000[packedCell] & 0x20) == 0) {
            return null;
        }
        WorldMapNode node = lookupNode(packedCell);
        if (node == null) {
            return null;
        }
        scratchNode0x5402C.copyFrom(node);
        return scratchNode0x5402C.building;
    }

    /**
     * Native: CWorldMap::getBuildingAtTargetHandle @00559BAB.
     * Fully ported.
     */
    public Building getBuildingAtTargetHandle(TargetHandle targetHandle) {
        return getBuildingAtCell(targetHandle.getCell());
    }

    /**
     * Native: CWorldMap::getBuildingAtPoint @00559B7F.
     * Fully ported.
     */
    public Building getBuildingAtPoint(int x, int y) {
        return getBuildingAtCell(packCell(x, y));
    }

    /**
     * Native: CWorldMap::projectCellAwayFromPackedPosition @00558B92.
     * Fully ported.
     */
    public int projectCellAwayFromPackedPosition(Unit unit, int packedPosition, int distanceTiles) {
        int unitXdX = unit.m_pTargetHandle.packXdX() & 0xFFFF;
        int unitYdY = unit.m_pTargetHandle.packYdY() & 0xFFFF;
        int dx = unitXdX - (packedPosition & 0xFFFF);
        int dy = unitYdY - (packedPosition >>> 16);
        if (dx == 0) {
            dx = 1;
        }
        if (dy == 0) {
            dy = 1;
        }

        int projectedXdX;
        int projectedYdY;
        int distanceXdX = (distanceTiles & 0xFF) * 0x100;
        if (Math.abs(dy) < Math.abs(dx)) {
            projectedXdX = unitXdX + (dx < 1 ? -distanceXdX : distanceXdX);
            projectedYdY = (int) (projectedXdX * ((float) dy / (float) dx)
                    + (unitYdY - unitXdX * ((float) dy / (float) dx)));
        } else {
            projectedYdY = unitYdY + (dy < 1 ? -distanceXdX : distanceXdX);
            projectedXdX = (int) (projectedYdY * ((float) dx / (float) dy)
                    + (unitXdX - unitYdY * ((float) dx / (float) dy)));
        }

        int projectedX = projectedXdX >> 8;
        int projectedY = projectedYdY >> 8;
        if (projectedX < 8) {
            projectedX = 8;
        }
        if (getMapWidth() - 9 < projectedX) {
            projectedX = getMapWidth() - 9;
        }
        if (projectedY < 8) {
            projectedY = 8;
        }
        if (getMapHeight() - 9 < projectedY) {
            projectedY = getMapHeight() - 9;
        }
        return projectedY * 0x100 + projectedX;
    }

    /**
     * Native: CWorldMap::isFacingUnitInRange @0055532D.
     * Fully ported.
     */
    public boolean isFacingUnitInRange(Unit unit, Token target, int maxRangeTiles) {
        int direction = getDirection8Code(unit, target);
        return (unit.movementState.facing & 0xFF) == direction
                && (getRangeInTiles(unit, target) & 0xFF) <= (maxRangeTiles & 0xFF);
    }

    /**
     * Fully ported. Native: CWorldMap::getDirection8Code @005568E4.
     */
    public int getDirection8Code(Token first, Token second) {
        int dx = (second.getCenterXdX() & 0xFFFF) - (first.getCenterXdX() & 0xFFFF);
        int dy = (second.getCenterYdY() & 0xFFFF) - (first.getCenterYdY() & 0xFFFF);
        int absDx = dx < 1 ? (-((short) dx)) & 0xFFFF : dx & 0xFFFF;
        int absDy = dy < 1 ? (-((short) dy)) & 0xFFFF : dy & 0xFFFF;

        int result;
        if (dx < 1) {
            if (dy < 1) {
                if (absDy < absDx) {
                    result = absDy * 2 < absDx ? 0x0C : 0x0D;
                } else if (absDx * 2 < absDy) {
                    result = 0x0F;
                } else {
                    result = 0x0E;
                }
            } else if (absDy < absDx) {
                result = absDy * 2 < absDx ? 0x0B : 0x0A;
            } else if (absDx * 2 < absDy) {
                result = 0x08;
            } else {
                result = 0x09;
            }
        } else if (dy < 1) {
            if (absDy < absDx) {
                result = absDy * 2 < absDx ? 0x03 : 0x02;
            } else if (absDx * 2 < absDy) {
                result = 0x00;
            } else {
                result = 0x01;
            }
        } else if (absDy < absDx) {
            result = absDy * 2 < absDx ? 0x04 : 0x05;
        } else if (absDx * 2 < absDy) {
            result = 0x07;
        } else {
            result = 0x06;
        }

        if (result != 0) {
            result++;
        }
        return ((result >>> 1) << 5) & 0xFF;
    }

    /**
     * Fully ported. Native: CWorldMap::getFacingAngularDistance8 @0055AA21.
     */
    public int getFacingAngularDistance8(int facing, int direction) {
        int delta = Math.abs((facing & 0xFF) - (direction & 0xFF));
        return delta > 0x80 ? (-delta) & 0xFF : delta;
    }

    /**
     * Native: CWorldMap::isFacingCellInRange @0055539A.
     * Fully ported.
     */
    public boolean isFacingCellInRange(Unit unit, int cell, int range) {
        int direction = direction8CodeToCell(unit, cell);
        return (unit.movementState.facing & 0xFF) == direction
                && cellChebyshevDistance(unit.m_pTargetHandle.getCell(), cell) <= (range & 0xFF);
    }

    /**
     * Fully ported. Native: CWorldMap::hasClearLayer2Footprint @0055CD50.
     */
    public boolean hasClearLayer2Footprint(Unit unit, int cell) {
        int size = unit.getTokenSizeVirtual() & 0xFF;
        int movementMask = unit.movementState.movementLayerMask & 0xFF;
        int baseCell = cell & 0xFFFF;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int footprintCell = (baseCell + y * 0x100 + x) & 0xFFFF;
                if ((layer2_0x20000[footprintCell] & movementMask) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Fully ported. Native: CWorldMap::hasClearLayer1Footprint @0055C440.
     */
    private boolean hasClearLayer1Footprint(Unit unit, int cell) {
        int size = unit.getTokenSizeVirtual() & 0xFF;
        int movementMask = unit.movementState.movementLayerMask & 0xFF;
        int baseCell = cell & 0xFFFF;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int footprintCell = (baseCell + y * 0x100 + x) & 0xFFFF;
                if ((layer1_0x10000[footprintCell] & movementMask) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Fully ported. Native: CWorldMap::hasClearMovementFootprint @0055C5A0.
     */
    private boolean hasClearMovementFootprint(Unit unit, int cell, boolean useLayer1) {
        int size = unit.getTokenSizeVirtual() & 0xFF;
        int movementMask = unit.movementState.movementLayerMask & 0xFF;
        int baseX = cell & 0xFF;
        int baseY = (cell >>> 8) & 0xFF;
        byte[] layer = useLayer1 ? layer1_0x10000 : layer2_0x20000;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int footprintCell = packCell(baseX + x, baseY + y);
                if ((layer[footprintCell] & movementMask) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Native support extracted from CWorldMap::relocateScenarioMissionEntryUnit @0055A96C.
     */
    public void relocateScenarioMissionEntryUnit(Unit unit, int packedCell) {
        relocateScenarioMissionEntryUnit(unit, packedCell & 0xFF, (packedCell >>> 8) & 0xFF);
    }

    /**
     * Fully ported. Native: CWorldMap::relocateScenarioMissionEntryUnit @0055A96C.
     */
    public void relocateScenarioMissionEntryUnit(Unit unit, int x, int y) {
        int packedCell = ((y & 0xFF) << 8) | (x & 0xFF);
        packedCell &= 0xFFFF;
        if (!canPlaceUnitFootprintAtCell(unit, packedCell)) {
            return;
        }
        detachUnit(unit);
        unit.m_pTargetHandle.setCellAndResetSubPos(packedCell & 0xFF, (packedCell >>> 8) & 0xFF);
        refreshSteppedUnitCell(unit);
        unit.initializeScenarioMissionEntryUnit(missionScriptRuntime0x58D80);
        CServerApp.netUpdate(unit, null, -1, 0x0FFB, 0, 0);
    }

    /**
     * Fully ported. Native: CWorldMap::canPlaceUnitFootprintAtCell @0055A928.
     */
    private boolean canPlaceUnitFootprintAtCell(Unit unit, int packedCell) {
        return hasClearMovementFootprint(unit, packedCell, false)
                && hasClearMovementFootprint(unit, packedCell, true);
    }

    /**
     * Fully ported. Native: CWorldMap::collectActiveUnitsNearCell @0055B997.
     */
    public void collectActiveUnitsNearCell(int sourceCell, int targetSearchRadius,
                                           @SuppressWarnings("unused") Player owner, UnitList outUnits) {
        outUnits.clear();
        if (activeUnits0xA456C != null) {
            for (Unit unit : activeUnits0xA456C) {
                int unitCell = unit.m_pTargetHandle.getCell();
                int dx = Math.abs((sourceCell & 0xFF) - (unitCell & 0xFF));
                int dy = Math.abs(((sourceCell >>> 8) & 0xFF) - ((unitCell >>> 8) & 0xFF));
                if (Math.max(dx, dy) <= (targetSearchRadius & 0xFF)) {
                    outUnits.add(unit);
                }
            }
        }
    }

    /**
     * Native: CWorldMap::EncodeTileVisibilityMaskRuns @00559D70.
     * Fully ported.
     */
    public short[] encodeTileVisibilityMaskRuns() {
        int startX = 8;
        int startY = 8;
        int endX = rect0x58EB8.rb.x & 0xFF;
        int endY = rect0x58EB8.rb.y & 0xFF;

        List<Short> runs = new ArrayList<>();
        int currentMask = layer1_0x10000[packCell(startX, startY)] & 0x10;
        int runLength = 0;
        runs.add((short) currentMask);

        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                int mask = layer1_0x10000[packCell(x, y)] & 0x10;
                if (mask == currentMask) {
                    runLength++;
                } else {
                    runs.add((short) runLength);
                    currentMask = mask;
                    runLength = 1;
                }
            }
        }
        runs.add((short) runLength);

        short[] result = new short[runs.size()];
        for (int i = 0; i < runs.size(); i++) {
            result[i] = runs.get(i);
        }
        return result;
    }

    /**
     * Native: CWorldMap::initializeRuntimeState @00553D51.
     * Fully ported.
     */
    private void initializeRuntimeState(UnitList activeUnits) {
        initializeRuntimeState(activeUnits, true);
    }

    /**
     * Native support extracted from CWorldMap::initializeRuntimeState @00553D51.
     * Preserves serialized world nodes for the Java CWorldMap::serialize @00550008 rebind path when requested.
     */
    private void initializeRuntimeStatePreservingWorldNodes(UnitList activeUnits) {
        initializeRuntimeState(activeUnits, false);
    }

    /**
     * Native support extracted from CWorldMap::initializeRuntimeState @00553D51.
     */
    private void initializeRuntimeState(UnitList activeUnits, boolean resetWorldNodes) {
        activeUnits0xA456C = activeUnits;
        unitVisibilityState0x92ECC.activeUnits0x1610 = activeUnits;
        int unitsCount = activeUnits.size();
        unitVisibilityState0x92ECC.unitsCount1_0x1614 = unitsCount;
        unitVisibilityState0x92ECC.unitsCount2_0x1618 = unitsCount;

        mapWidth0x50000 = 0x80;
        mapHeight0x50004 = 0x80;
        visionAndDistance0x58EC0.pCWorldMap = this;

        Arrays.fill(layer0_0x00000, (byte) 1);
        Arrays.fill(layer1_0x10000, (byte) 0);
        Arrays.fill(layer2_0x20000, (byte) 0);
        Arrays.fill(unkByteArray0x944F4, (byte) 0);
        if (resetWorldNodes) {
            resetWorldNodeState();
        }

        initializeNeighborStepTable();
        initializeTransitionBlendModes();
        initializeTransitionTerrainPairs();
        initializeTerrainAndPathFindingConfig();
        initializeCardinalStepSets();
    }

    /**
     * Native support extracted from CWorldMap::initializeRuntimeState @00553D51 CMap::InitHashTable call.
     */
    private void resetWorldNodeState() {
        nodeMap.entries.clear();
        buildingByCell.clear();
        sackByCell.clear();
        unitByCell.clear();
    }

    /**
     * Native support extracted from CWorldMap::initializeRuntimeState @00553D51.
     */
    private void initializeNeighborStepTable() {
        int[] dx = {0, 1, 1, 1, 0, -1, -1, -1};
        int[] dy = {-1, -1, 0, 1, 1, 1, 0, -1};
        for (int i = 0; i < 8; i++) {
            neighborStepTable0x58E88.dx[i] = (byte) dx[i];
            neighborStepTable0x58E88.dy[i] = (byte) dy[i];
            neighborStepTable0x58E88.cellDelta[i] = (dy[i] << 8) + dx[i];
        }
    }

    /**
     * Native support extracted from CWorldMap::initializeRuntimeState @00553D51.
     */
    private void initializeTransitionBlendModes() {
        byte[][] values = {
                {2, 3, 2, 4, 3, 4, 2, 2, 2, 2, 4, 4, 4, 4},
                {3, 5, 3, 3, 1, 3, 2, 4, 2, 2, 4, 2, 4, 4},
                {2, 3, 2, 4, 3, 4, 2, 4, 2, 2, 4, 2, 4, 4},
                {5, 5, 5, 5, 5, 5, 2, 2, 2, 2, 4, 4, 4, 4}
        };
        for (int row = 0; row < values.length; row++) {
            Arrays.fill(transitionBlendMode0x540A6[row], (byte) 0);
            System.arraycopy(values[row], 0, transitionBlendMode0x540A6[row], 0, values[row].length);
        }
    }

    /**
     * Native support extracted from CWorldMap::initializeRuntimeState @00553D51.
     */
    private void initializeTransitionTerrainPairs() {
        for (Point8 transitionPair : transitionTerrainPairs0x54126) {
            setPoint(transitionPair, 0, 0);
        }
        setTransitionPair(0, 2, 1);
        setTransitionPair(1, 5, 1);
        setTransitionPair(2, 4, 1);
        setTransitionPair(3, 7, 1);
        setTransitionPair(4, 6, 1);
        setTransitionPair(5, 5, 6);
        setTransitionPair(6, 3, 7);
        setTransitionPair(7, 8, 6);
        setTransitionPair(12, 10, 1);
    }

    /**
     * Native support extracted from CWorldMap::initializeRuntimeState @00553D51.
     */
    private void initializeTerrainAndPathFindingConfig() {
        try {
            ResInHeap mapReg = ResInHeap.load(MAP_REG_PATH);
            costNoPass0x54146 = (byte) 0xFF;
            costLand0x54147 = (byte) mapReg.getInt(TERRAIN_SECTION, "CostLand", 8);
            costGrass0x54148 = (byte) mapReg.getInt(TERRAIN_SECTION, "CostGrass", 8);
            costFlowers0x54149 = (byte) mapReg.getInt(TERRAIN_SECTION, "CostFlowers", 9);
            costSand0x5414A = (byte) mapReg.getInt(TERRAIN_SECTION, "CostSand", 0xE);
            costCracked0x5414B = (byte) mapReg.getInt(TERRAIN_SECTION, "CostCracked", 6);
            costStones0x5414C = (byte) mapReg.getInt(TERRAIN_SECTION, "CostStones", 0xC);
            costSavanna0x5414D = (byte) mapReg.getInt(TERRAIN_SECTION, "CostSavanna", 0xB);
            costMountain0x5414E = (byte) mapReg.getInt(TERRAIN_SECTION, "CostMountain", 0x10);
            costWater0x5414F = (byte) mapReg.getInt(TERRAIN_SECTION, "CostWater", 8);
            costRoad0x54150 = (byte) mapReg.getInt(TERRAIN_SECTION, "CostRoad", 6);

            speedMultiplier0x58D84 = mapReg.getInt(PATH_FINDING_SECTION, "SpeedMultiplier", 8);
            searchScratchState0x54190.staticScanAhead0x43F4 =
                    mapReg.getInt(PATH_FINDING_SECTION, "StaticScanAhead", 5);
            searchScratchState0x54190.dynamicScanAhead0x43F8 =
                    mapReg.getInt(PATH_FINDING_SECTION, "DynamicScanAhead", 3);
            searchScratchState0x54190.staticRefreshRate0x43FC =
                    mapReg.getInt(PATH_FINDING_SECTION, "StaticRefreshRate", 0x10);
            searchScratchState0x54190.dynamicRefreshRate0x4400 =
                    mapReg.getInt(PATH_FINDING_SECTION, "DynamicRefreshRate", 0x20);
            searchScratchState0x54190.dynamicByStaticLookup0x4404 =
                    mapReg.getInt(PATH_FINDING_SECTION, "DynamicByStaticLookup", 3);
            searchScratchState0x54190.staticIsntNeeded0x4408 =
                    mapReg.getInt(PATH_FINDING_SECTION, "StaticIsntNeeded", 5);
            pathFindingField0x54188 = 0;
            pathFindingField0x5418C = 0;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load " + MAP_REG_PATH + " for CWorldMap", e);
        }
    }

    /**
     * Native support extracted from CWorldMap::initializeRuntimeState @00553D51.
     */
    private void initializeCardinalStepSets() {
        for (Point8[] row : cardinalStepSets0x54156) {
            setPoint(row[0], 1, 0);
            setPoint(row[1], 0, 1);
            setPoint(row[2], -1, 0);
            setPoint(row[3], 0, -1);
        }
    }

    /**
     * Native: CWorldMap::initializeFromScenario @00554626.
     * Fully ported.
     */
    private int initializeFromScenario(ScenarioDescriptor scenarioDescriptor) {
        mapWidth0x50000 = scenarioDescriptor.mapWidth;
        mapHeight0x50004 = scenarioDescriptor.mapHeight;

        rect0x58EB8.lt.x = 8;
        rect0x58EB8.lt.y = 8;
        rect0x58EB8.rb.x = (byte) (mapWidth0x50000 - 9);
        rect0x58EB8.rb.y = (byte) (mapHeight0x50004 - 9);
        rectLtPacked0x58EBC = packCell(rect0x58EB8.lt.x, rect0x58EB8.lt.y);
        rectRbPacked0x58EBE = packCell(rect0x58EB8.rb.x, rect0x58EB8.rb.y);

        int[] terrainCostOut = new int[1];
        for (int x = 0; x < mapWidth0x50000; x++) {
            for (int y = 0; y < mapHeight0x50004; y++) {
                int tileIndex = y * mapWidth0x50000 + x;
                int cell = packCell(x, y);
                int tile = scenarioDescriptor.getTiles()[tileIndex] & 0xFFFF;

                if ((tile & 0x2000) != 0) {
                    layer1_0x10000[cell] = 1;
                }

                byte height = scenarioDescriptor.sec2Heights[tileIndex];
                byte object = scenarioDescriptor.sec3Objects[tileIndex];
                int terrainId = resolveTerrainCostAndId(tile & 0x3FF, terrainCostOut);

                if (terrainId == 8) {
                    layer1_0x10000[cell] = 1;
                }
                if (terrainId < 0xD) {
                    layer0_0x00000[cell] = (byte) terrainCostOut[0];
                } else {
                    layer0_0x00000[cell] = costNoPass0x54146;
                }
                if ((tile & 0x300) == 0x200) {
                    layer1_0x10000[cell] = 1;
                }
                if (object != 0) {
                    layer1_0x10000[cell] = 5;
                }

                unkByteArray0x944F4[cell] = height;
            }
        }

        applyImpassableBorder();
        System.arraycopy(layer1_0x10000, 0, layer2_0x20000, 0, TILE_ARRAY_SIZE);
        return 1;
    }

    /**
     * Native: CWorldMap::loadAsciiTerrainFile @0054D526.
     * Fully ported.
     */
    private void loadAsciiTerrainFile(String path, int width, int height, UnitList activeUnits) {
        ByteBuffer terrainData = Globals.gameFileManager.get(path);
        mapWidth0x50000 = width & 0xFF;
        mapHeight0x50004 = height & 0xFF;

        int sourceOffset = 0;
        for (int y = 0; y < mapHeight0x50004; y++) {
            for (int x = 0; x < mapWidth0x50000; x++) {
                int cell = packCell(x + 8, y + 8);
                layer0_0x00000[cell] = (byte) ((terrainData.get(sourceOffset) & 0xFF) - '0');
                if (layer0_0x00000[cell] == 0) {
                    layer1_0x10000[cell] = LAYER_BORDER_BLOCK;
                    layer0_0x00000[cell] = (byte) 0xFF;
                }
                sourceOffset++;
            }
            sourceOffset += 2;
        }
        activeUnits0xA456C = activeUnits;
    }

    /**
     * Native: CWorldMap::applyImpassableBorder @00553B30.
     * Fully ported.
     */
    private void applyImpassableBorder() {
        if (mapWidth0x50000 == mapHeight0x50004) {
            for (int i = 0; i < mapWidth0x50000; i++) {
                for (int border = 0; border < 8; border++) {
                    layer1_0x10000[packCell(border, i)] = LAYER_BORDER_BLOCK;
                    layer1_0x10000[packCell(mapWidth0x50000 - border - 1, i)] = LAYER_BORDER_BLOCK;
                    layer1_0x10000[packCell(i, border)] = LAYER_BORDER_BLOCK;
                    layer1_0x10000[packCell(i, mapWidth0x50000 - border - 1)] = LAYER_BORDER_BLOCK;
                }
            }
            return;
        }

        for (int i = 0; i < 0x100; i++) {
            for (int border = 0; border < 8; border++) {
                layer1_0x10000[packCell(border, i)] = LAYER_BORDER_BLOCK;
                layer1_0x10000[packCell(0xFF - border, i)] = LAYER_BORDER_BLOCK;
                layer1_0x10000[packCell(i, border)] = LAYER_BORDER_BLOCK;
                layer1_0x10000[packCell(i, 0xFF - border)] = LAYER_BORDER_BLOCK;
            }
        }
        for (int y = 0; y < mapHeight0x50004; y++) {
            for (int border = 0; border < 8; border++) {
                layer1_0x10000[packCell(mapWidth0x50000 - border - 1, y)] = LAYER_BORDER_BLOCK;
            }
        }
        for (int x = 0; x < mapWidth0x50000; x++) {
            for (int border = 0; border < 8; border++) {
                layer1_0x10000[packCell(x, mapHeight0x50004 - border - 1)] = LAYER_BORDER_BLOCK;
            }
        }
    }

    /**
     * Native: CWorldMap::resolveTerrainCostAndId @0055494D.
     * Fully ported.
     */
    private int resolveTerrainCostAndId(int tileCode, int[] costOut) {
        int terrainId = 0xFF;
        costOut[0] = 8;

        if ((tileCode & 0x300) == 0x200) {
            int localTile = tileCode & 0xF;
            if (localTile < 8) {
                if (localTile == 4) {
                    terrainId = ((tileCode & 0x30) >> 4) == 1 ? 1 : 9;
                } else {
                    terrainId = 9;
                }
            }
            return terrainId;
        }

        int baseTerrain = tileCode & 0xF;
        if (baseTerrain < 0xE) {
            Point8 transitionPair = transitionTerrainPairs0x54126[(tileCode & 0x3C0) >> 6];
            int terrainX = transitionPair.x & 0xFF;
            int terrainY = transitionPair.y & 0xFF;
            int blendMode = transitionBlendMode0x540A6[(tileCode & 0x30) >> 4][baseTerrain] & 0xFF;
            int costX = getTerrainCostById(terrainX);
            int costY = getTerrainCostById(terrainY);

            terrainId = blendMode > 2 ? terrainX : terrainY;
            switch (blendMode) {
                case 1 -> costOut[0] = costY;
                case 2 -> costOut[0] = (costX + costY * 3) >> 2;
                case 3 -> costOut[0] = (costX + costY) >> 1;
                case 4 -> costOut[0] = (costX * 3 + costY) >> 2;
                case 5 -> costOut[0] = costX;
                default -> {
                }
            }
        }
        return terrainId;
    }

    /**
     * Native support extracted from CWorldMap::resolveTerrainCostAndId @0055494D.
     */
    private int getTerrainCostById(int terrainId) {
        return switch (terrainId) {
            case 0 -> costNoPass0x54146 & 0xFF;
            case 1 -> costLand0x54147 & 0xFF;
            case 2 -> costGrass0x54148 & 0xFF;
            case 3 -> costFlowers0x54149 & 0xFF;
            case 4 -> costSand0x5414A & 0xFF;
            case 5 -> costCracked0x5414B & 0xFF;
            case 6 -> costStones0x5414C & 0xFF;
            case 7 -> costSavanna0x5414D & 0xFF;
            case 8 -> costMountain0x5414E & 0xFF;
            case 9 -> costWater0x5414F & 0xFF;
            case 10 -> costRoad0x54150 & 0xFF;
            default -> 0;
        };
    }

    /**
     * Native support extracted from CWorldMap::initializeRuntimeState @00553D51.
     */
    private void setTransitionPair(int index, int x, int y) {
        setPoint(transitionTerrainPairs0x54126[index], x, y);
    }

    /**
     * Native support extracted from CWorldMap native initializers @00553D51 and @00554626.
     */
    private static void setPoint(Point8 point, int x, int y) {
        point.x = (byte) x;
        point.y = (byte) y;
    }

    /**
     * Native: CWorldMap::serialize @00550008.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        CustomList<Integer> packedTiles = CustomList.std(Integer.class);

        if (ar.isStoring()) {
            for (int idx = TILE_SCAN_START; idx < TILE_SCAN_END_EXCLUSIVE; idx++) {
                int hi = layer2_0x20000[idx] & 0xFF;
                if (hi > 0x0F) {
                    int lo = layer1_0x10000[idx] & 0xFF;
                    packedTiles.add((idx << 16) | (hi << 8) | lo);
                }
            }
            ar.serialize(packedTiles);
            ar.serialize(nodeMap);
            ar.writeInt(System.identityHashCode(this));
        } else {
            ar.serialize(packedTiles);
            ar.serialize(nodeMap);
            int selfToken = ar.readInt();
            Globals.gameServer.setPointerMapEntry(selfToken, this);

            for (int packed : packedTiles) {
                int idx = (packed >>> 16) & 0xFFFF;
                layer2_0x20000[idx] = (byte) ((packed >>> 8) & 0xFF);
                layer1_0x10000[idx] = (byte) (packed & 0xFF);
            }
        }
    }

    /**
     * Native: CWorldMap::restoreContext @005595E5.
     * Fully ported.
     */
    public void restoreContext() {
        nodeMap.restoreContext();
    }

    /**
     * Native: CWorldMap::attachBuildingFootprint @005599DF.
     * Fully ported.
     */
    public boolean attachBuildingFootprint(Building building) {
        int bit = 0;
        for (int y = 0; y < (building.sizeY & 0xFF); y++) {
            for (int x = 0; x < (building.sizeX & 0xFF); x++) {
                if ((building.buildingPresentMask & (1 << (bit & 0x1F))) != 0) {
                    int packedCell = packCell((building.m_pTargetHandle.getX() + x) & 0xFF,
                            (building.m_pTargetHandle.getY() + y) & 0xFF);
                    if (!attachBuildingCell(building, packedCell)) {
                        return false;
                    }
                }
                bit++;
            }
        }
        return true;
    }

    /**
     * Native: CWorldMap::detachBuildingFootprint @00559AAF.
     * Fully ported.
     */
    public boolean detachBuildingFootprint(Building building) {
        int bit = 0;
        for (int y = 0; y < (building.sizeY & 0xFF); y++) {
            for (int x = 0; x < (building.sizeX & 0xFF); x++) {
                if ((building.buildingPresentMask & (1 << (bit & 0x1F))) != 0) {
                    int packedCell = packCell((building.m_pTargetHandle.getX() + x) & 0xFF,
                            (building.m_pTargetHandle.getY() + y) & 0xFF);
                    if (!detachBuildingCell(building, packedCell)) {
                        return false;
                    }
                }
                bit++;
            }
        }
        return true;
    }

    /**
     * Native: CWorldMap::detachUnit @00553891.
     * Fully ported.
     */
    public boolean detachUnit(Unit unit) {
        refreshUnitFootprintForDetach(unit);
        boolean detached = detachUnitFootprint(unit);
        unitByCell.values().remove(unit);
        return detached;
    }

    /**
     * Native: CWorldMap::clearUnitRuntimePathScratch @00551682.
     * Fully ported.
     */
    public void clearUnitRuntimePathScratch(Unit unit) {
        unit.mList2.clear();
        refreshNodeLayers(unit.m_pTargetHandle.getCell());
    }

    /**
     * Native: CWorldMap::refreshUnitQueuedMissionPathState @005511F4.
     * Fully ported.
     */
    public boolean refreshUnitQueuedMissionPathState(Unit unit) {
        if (unit.mList1.isEmpty()) {
            return false;
        }
        unit.mList2.clear();
        int headCell = unit.mList1.getFirst() & 0xFFFF;
        int dx = Math.abs((unit.m_pTargetHandle.getX() & 0xFF) - (headCell & 0xFF));
        if (dx < 2) {
            int dy = Math.abs((unit.m_pTargetHandle.getY() & 0xFF) - ((headCell >>> 8) & 0xFF));
            if (dy < 2) {
                boolean refreshed = refreshQueuedMissionPathFacing(unit);
                if (refreshed) {
                    return true;
                }
                unit.mList2.clear();
                if (unit.movementState.pathHeadRetryDelay != 0) {
                    if ((unit.movementState.pathHeadRetryDelay & 0xFF) < 2) {
                        return refreshQueuedMissionPathFacing(unit);
                    }
                    unit.movementState.pathHeadRetryDelay = (unit.movementState.pathHeadRetryDelay - 1) & 0xFF;
                    return false;
                }
                unit.movementState.pathHeadRetryDelay = 1;
                return false;
            }
        }
        return refreshQueuedMissionPathFacing(unit);
    }

    /**
     * Native: CWorldMap::refreshQueuedMissionPathFacing @00550F75.
     * Fully ported.
     */
    public boolean refreshQueuedMissionPathFacing(Unit unit) {
        int packedPositionCell = unit.movementState.packPositionCell() & 0xFFFF;
        int currentOrPositionCell = unit.m_pTargetHandle.getCell();
        if (!unit.m_pTargetHandle.isSubPosUnknown() && packedPositionCell != 0) {
            currentOrPositionCell = packedPositionCell;
        }

        if (!unit.m_pTargetHandle.isSubPosUnknown() && packedPositionCell != 0) {
            unit.mList2.addFirst((short) currentOrPositionCell);
        }

        int remainingSkips = packedPositionCell != 0 ? 1 : 2;
        for (Short rawCell : unit.mList2) {
            if (remainingSkips == 0) {
                break;
            }
            int queuedCell = rawCell & 0xFFFF;
            if (queuedCell != unit.m_pTargetHandle.getCell() && queuedCell != packedPositionCell) {
                remainingSkips--;
            }
        }
        if (unit.mList2.isEmpty()) {
            int direction = getSubTileDirection8CodeToCellCenter(unit, unit.mList1.getLast() & 0xFFFF);
            if ((unit.movementState.facing & 0xFF) != direction) {
                unit.movementState.facingLast = direction;
            }
        }
        return true;
    }

    /**
     * Fully ported native support extracted from CWorldMap::refreshUnitFootprint @0055BE86 detach path mode=2.
     */
    private void refreshUnitFootprintForDetach(Unit unit) {
        int size = unit.getTokenSizeVirtual() & 0xFF;
        int beforeX = unit.m_pTargetHandle.getX() - 1;
        int beforeY = unit.m_pTargetHandle.getY() - 1;
        for (int y = 0; y < size + 2; y++) {
            for (int x = 0; x < size + 2; x++) {
                clearUnitFromNodeLayerState(unit, packCell(beforeX + x, beforeY + y));
            }
        }
        unit.movementState.cell = 0;
    }

    /**
     * Native: CWorldMap::detachUnitFootprint @00550697.
     * Fully ported.
     */
    private boolean detachUnitFootprint(Unit unit) {
        int size = unit.getTokenSizeVirtual() & 0xFF;
        int baseX = unit.m_pTargetHandle.getX() & 0xFF;
        int baseY = unit.m_pTargetHandle.getY() & 0xFF;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (!detachUnitCell(unit, baseX + x, baseY + y)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Native: CWorldMap::detachUnitCell @0055073F.
     * Fully ported.
     */
    private boolean detachUnitCell(Unit unit, int x, int y) {
        int packedCell = packCell(x, y);
        WorldMapNode node = lookupNode(packedCell);
        if (node == null) {
            return false;
        }

        int movementType = unit.getMovementType() & 0xFF;
        if (movementType != 0) {
            if (movementType < 3) {
                if (node.groundOccupancyUnit == null) {
                    return false;
                }
                node.groundOccupancyUnit = null;
            } else if (movementType == 3) {
                if (node.airOccupancyUnit == null) {
                    return false;
                }
                node.airOccupancyUnit = null;
            }
        }

        nodeMap.entries.put(packedCell, node);
        refreshNodeLayers(packedCell);
        if (node.isEmpty()) {
            removeDynamicNodeForCell(packedCell);
        }
        unit.movementState.detachCellX = unit.m_pTargetHandle.getX();
        unit.movementState.detachCellY = unit.m_pTargetHandle.getY();
        unit.movementState.detachCellXdX = unit.m_pTargetHandle.packXdX();
        unit.movementState.detachCellYdY = unit.m_pTargetHandle.packYdY();
        return true;
    }

    /**
     * Fully ported. Native: CWorldMap::clearUnitFromNodeLayerState @0055C1FE.
     */
    private boolean clearUnitFromNodeLayerState(Unit unit, int packedCell) {
        packedCell &= 0xFFFF;
        if ((layer1_0x10000[packedCell] & 0x20) == 0) {
            return false;
        }
        WorldMapNode node = lookupNode(packedCell);
        if (node == null) {
            return false;
        }

        int movementType = unit.getMovementType() & 0xFF;
        if (movementType == 3) {
            if (node.secondaryLayerStateUnit == unit) {
                node.secondaryLayerStateUnit = null;
            }
        } else if (node.primaryLayerStateUnit == unit) {
            node.primaryLayerStateUnit = null;
        }

        refreshNodeLayers(packedCell);
        nodeMap.entries.put(packedCell, node);
        if (node.isEmpty()) {
            return removeDynamicNodeForCell(packedCell);
        }
        return false;
    }

    /**
     * Native: CWorldMap::refreshUnitCellFromTargetHandle @00558B75.
     * Fully ported.
     */
    public void refreshUnitCellFromTargetHandle(Unit unit) {
        refreshUnitFootprintFromTargetHandle(unit);
        unitByCell.values().remove(unit);
        int packedCell = unit.m_pTargetHandle.getCell();
        unitByCell.put(packedCell, unit);
    }

    /**
     * Native: CWorldMap::updateUnitMissionPathProgress @00555B38.
     * Fully ported.
     */
    public void updateUnitMissionPathProgress(Unit unit) {
        int oldCell = unit.m_pTargetHandle.getCell();
        advanceUnitSubTileStep(unit);
        int newCell = unit.m_pTargetHandle.getCell();
        if (oldCell != newCell) {
            unit.movementState.terrainStepDistance =
                    calculateUnitTerrainStepDistance(unit, unit.m_pTargetHandle.getX(), unit.m_pTargetHandle.getY());
        }
        if (unit.m_pTargetHandle.isSubPosUnknown()) {
            unit.movementState.stepTickLimit = 0;
            unit.movementState.stepTick = 0;
            unit.movementState.subTileStepDistance = 0;
            refreshUnitFootprintForDetach(unit);
            refreshUnitFootprintFromTargetHandle(unit);
            unit.mList2.clear();
            unitByCell.values().remove(unit);
            unitByCell.put(unit.m_pTargetHandle.getCell(), unit);
            WorldMapNode node = lookupNode(unit.m_pTargetHandle.getCell());
            if (node != null && node.getTransientSpellCastId() == SpellId.TELEPORT.id) {
                relocateScenarioMissionEntryUnit(
                        unit,
                        node.getTransientSpellCastTargetX(),
                        node.getTransientSpellCastTargetY()
                );
            }
        }
    }

    /**
     * Native: CWorldMap::calculateUnitTerrainStepDistance @0055687D.
     * Fully ported.
     */
    private short calculateUnitTerrainStepDistance(Unit unit, int x, int y) {
        int movementType = unit.getMovementType() & 0xFF;
        if (movementType == 1) {
            return (short) (((unit.speed & 0xFFFF) << 3) / (layer0_0x00000[packCell(x, y)] & 0xFF));
        }
        if (movementType < 2 || 3 < movementType) {
            return 0;
        }
        return (short) unit.speed;
    }

    /**
     * Native: CWorldMap::requestMissionMoveToCell @00555411.
     * Fully ported.
     */
    public void requestMissionMoveToCell(Unit unit, int targetCell, int stopRange) {
        int cell = targetCell & 0xFFFF;
        int distance = cellChebyshevDistance(unit.m_pTargetHandle.getCell(), cell);
        if (!unit.m_pTargetHandle.isSubPosUnknown()) {
            updateUnitMissionPathProgress(unit);
        } else if ((stopRange & 0xFF) < distance) {
            unit.movementState.pathRefreshTicks = (unit.movementState.pathRefreshTicks + 1) & 0xFF;
            if (unit.movementState.pathTargetCell != cell
                    || searchScratchState0x54190.staticRefreshRate0x43FC <
                    (unit.movementState.pathSearchRefreshCount & 0xFF)) {
                rebuildMissionPathSearchFromUnitCell(unit, cell, true, null);
                unit.movementState.missionReentryPending = 0;
                unit.movementState.pathTargetCell = cell;
                if (!unit.mList1.isEmpty()) {
                    unit.movementState.missionReentryCell = unit.mList1.getLast() & 0xFFFF;
                } else {
                    unit.movementState.missionReentryCell = unit.m_pTargetHandle.getCell();
                    unit.movementState.movementEventPending = 1;
                }
                unit.movementState.pathSearchRefreshCount = 0;
                unit.mList2.clear();
            }
            if (unit.m_pTargetHandle.getCell() == unit.movementState.missionReentryCell) {
                unit.movementState.missionReentryPending = 1;
            } else {
                boolean staticPathCellConsumed = false;
                if (unit.mList2.isEmpty()
                        || searchScratchState0x54190.dynamicRefreshRate0x4400 <
                        (unit.movementState.pathRefreshTicks & 0xFF)) {
                    int adjacentStaticPathCell = findAdjacentStaticPathCell(unit);
                    if (adjacentStaticPathCell != 0
                            && !hasClearMovementFootprint(unit, adjacentStaticPathCell, false)
                            && missionScriptRuntime0x58D80.classifyMissionMoveCell(unit, adjacentStaticPathCell) != 0) {
                        applyMissionMoveFacingCommand(unit, adjacentStaticPathCell);
                        staticPathCellConsumed = true;
                    }
                    if (!staticPathCellConsumed) {
                        refreshDynamicMissionPath(unit, null);
                        unit.movementState.pathSearchRefreshCount =
                                (unit.movementState.pathSearchRefreshCount + 1) & 0xFF;
                        unit.movementState.pathRefreshTicks = 0;
                    }
                }
                if (!staticPathCellConsumed) {
                    consumeDynamicMissionPathCell(unit);
                }
            }
        } else if ((stopRange & 0xFF) != 0) {
            rotateUnitTowardCell(unit, cell);
        }
    }

    /**
     * Native: CWorldMap::requestTargetEngagementPath @0055574D.
     * Fully ported.
     */
    public void requestTargetEngagementPath(Unit unit, Token target, int stopRange) {
        int targetCell = target.m_pTargetHandle.getCell();
        int range = getRangeInTiles(unit, target) & 0xFF;
        if (!unit.m_pTargetHandle.isSubPosUnknown()) {
            updateUnitMissionPathProgress(unit);
        } else if ((stopRange & 0xFF) < range) {
            unit.movementState.pathRefreshTicks = (unit.movementState.pathRefreshTicks + 1) & 0xFF;
            if (unit.movementState.movementTargetUnit != target) {
                unit.movementState.pathSearchRefreshCount = 0xFF;
                unit.mList1.clear();
                unit.mList2.clear();
                unit.movementState.targetEngagementStaticPathLength = 0xFF;
                unit.movementState.missionReentryCell = targetCell;
                unit.movementState.targetEngagementCell = targetCell;
            }
            if (unit.mList1.size() / 3 + 1 < (unit.movementState.pathSearchRefreshCount & 0xFF)) {
                if (searchScratchState0x54190.staticIsntNeeded0x4408
                        < (unit.movementState.targetEngagementStaticPathLength & 0xFFFF)) {
                    rebuildMissionPathSearchFromUnitCell(unit, targetCell, true, target);
                } else {
                    unit.mList1.clear();
                    unit.mList1.add((short) unit.movementState.missionReentryCell);
                }
                unit.movementState.targetEngagementStaticPathLength = unit.mList1.size();
                unit.movementState.pathTargetCell = targetCell;
                if (!unit.mList1.isEmpty()) {
                    unit.movementState.missionReentryCell = unit.mList1.getLast() & 0xFFFF;
                    unit.movementState.targetEngagementCell = targetCell;
                } else {
                    unit.movementState.missionReentryCell = unit.m_pTargetHandle.getCell();
                    unit.movementState.targetEngagementCell = targetCell;
                    unit.movementState.movementEventPending = 1;
                }
                unit.movementState.movementTargetUnit = target;
                unit.movementState.pathSearchRefreshCount = 0;
                if (searchScratchState0x54190.staticIsntNeeded0x4408
                        < (unit.movementState.targetEngagementStaticPathLength & 0xFFFF)) {
                    unit.mList2.clear();
                }
            }
            if (unit.m_pTargetHandle.getCell() == unit.movementState.missionReentryCell
                    && unit.movementState.targetEngagementCell == targetCell) {
                unit.movementState.pathSearchRefreshCount =
                        (unit.movementState.pathSearchRefreshCount + 1) & 0xFF;
                unit.movementState.movementTargetUnit = null;
            } else {
                if ((unit.mList2.isEmpty()
                        || searchScratchState0x54190.dynamicRefreshRate0x4400 <
                        (unit.movementState.pathRefreshTicks & 0xFF))
                        && (unit.movementState.facing & 0xFF) == (unit.movementState.facingLast & 0xFF)) {
                    refreshDynamicMissionPath(unit, target);
                    unit.movementState.dynamicPathTargetCell = targetCell;
                    unit.movementState.dynamicPathSourceCell = unit.m_pTargetHandle.getCell();
                    unit.movementState.pathSearchRefreshCount =
                            (unit.movementState.pathSearchRefreshCount + 1) & 0xFF;
                    unit.movementState.pathRefreshTicks = 0;
                }
                consumeDynamicMissionPathCell(unit);
            }
        } else {
            rotateUnitTowardTarget(unit, target);
        }
    }

    /**
     * Native: CWorldMap::findAdjacentStaticPathCell @00558F64.
     * Fully ported.
     */
    private static int findAdjacentStaticPathCell(Unit unit) {
        if (unit.mList1.isEmpty()) {
            return 0;
        }
        int cell = unit.mList1.getFirst() & 0xFFFF;
        return cellChebyshevDistance(unit.m_pTargetHandle.getCell(), cell) == 1 ? cell : 0;
    }

    /**
     * Native: CWorldMap::refreshDynamicMissionPath @00555DC2.
     * Fully ported.
     */
    private void refreshDynamicMissionPath(Unit unit, Token targetContext) {
        int currentCell = unit.m_pTargetHandle.getCell();
        int staticPathSize = unit.mList1.size();
        int staticPathDistance = 0;
        if (staticPathSize != 0) {
            staticPathDistance = cellChebyshevDistance(currentCell, unit.mList1.getFirst() & 0xFFFF);
        }

        int targetCell;
        int sourceMode;
        if (searchScratchState0x54190.staticIsntNeeded0x4408 < staticPathSize) {
            if (searchScratchState0x54190.dynamicByStaticLookup0x4404 < staticPathDistance) {
                targetCell = unit.mList1.getFirst() & 0xFFFF;
                sourceMode = 2;
            } else {
                targetCell = unit.mList1.get(searchScratchState0x54190.dynamicByStaticLookup0x4404) & 0xFFFF;
                sourceMode = 3;
            }
        } else {
            targetCell = unit.movementState.missionReentryCell & 0xFFFF;
            sourceMode = 1;
        }

        rebuildMissionPathSearchFromUnitCell(unit, targetCell, false, targetContext);
        if (unit.mList2.isEmpty() && sourceMode == 1) {
            unit.movementState.movementEventPending = 1;
        }
        if (staticPathSize != 0 &&
                staticPathDistance <= searchScratchState0x54190.dynamicByStaticLookup0x4404) {
            unit.mList1.removeFirst();
        }
    }

    /**
     * Native: CWorldMap::rebuildMissionPathSearchFromUnitCell @0054D72D.
     * Fully ported.
     */
    private void rebuildMissionPathSearchFromUnitCell(Unit unit, int targetCell, boolean staticPath,
                                                      Token targetContext) {
        rebuildMissionPathSearch(
                unit,
                unit.m_pTargetHandle.getX(),
                unit.m_pTargetHandle.getY(),
                targetCell & 0xFF,
                (targetCell >>> 8) & 0xFF,
                staticPath,
                targetContext
        );
    }

    /**
     * Native: CWorldMap::rebuildMissionPathSearch @0054D76E.
     * Fully ported.
     */
    private void rebuildMissionPathSearch(Unit unit, int startX, int startY, int targetX, int targetY,
                                          boolean staticPath, Token targetContext) {
        int startCell = packCell(startX, startY);
        int targetCell = packCell(targetX, targetY);
        if (staticPath) {
            unit.mList1.clear();
        }
        int distance = cellChebyshevDistance(startCell, targetCell);
        if (startX == targetX && startY == targetY) {
            return;
        }

        pathFrontierCount0x54008 = 0;
        nextPathFrontierCount0x5400A = 0;
        pathFrontierX0x50008[pathFrontierCount0x54008] = (byte) startX;
        pathFrontierY0x51008[pathFrontierCount0x54008] = (byte) startY;
        searchScratchState0x54190.pathFrontierCells0x03F4[pathFrontierCount0x54008] = (short) startCell;
        pathFrontierCount0x54008++;

        if (!staticPath) {
            refreshUnitFootprintForDetach(unit);
        }

        Arrays.fill(layer3_0x30000, (short) 0xFFFF);
        layer3_0x30000[startCell] = 0;
        int tokenSize = unit.getTokenSizeVirtual() & 0xFF;
        int configuredScanAhead = staticPath
                ? searchScratchState0x54190.staticScanAhead0x43F4
                : searchScratchState0x54190.dynamicScanAhead0x43F8;
        int scanAhead = tokenSize == 1
                ? distance + Math.max(distance >> 2, configuredScanAhead)
                : distance + configuredScanAhead;
        if (staticPath
                && tokenSize == 1
                && unit.owner.isActive == 0
                && hasClearLayer1Footprint(unit, targetCell)) {
            scanAhead = 1000;
        }
        int steps = 0;
        while ((layer3_0x30000[targetCell] & 0xFFFF) == 0xFFFF
                && pathFrontierCount0x54008 != 0
                && steps < scanAhead) {
            nextPathFrontierCount0x5400A = 0;
            int currentCount = pathFrontierCount0x54008;
            for (int i = 0; i < currentCount; i++) {
                if (tokenSize == 1) {
                    int cell = searchScratchState0x54190.pathFrontierCells0x03F4[i] & 0xFFFF;
                    if (staticPath) {
                        expandOneTileStaticPathCostToNextFrontier(unit, cell);
                    } else {
                        expandOneTileDynamicPathCostToNextFrontier(unit, cell);
                    }
                } else {
                    int x = pathFrontierX0x50008[i] & 0xFF;
                    int y = pathFrontierY0x51008[i] & 0xFF;
                    if (staticPath) {
                        expandStaticPathCostToNextFrontier(unit, x, y);
                    } else {
                        expandDynamicPathCostToNextFrontier(unit, x, y);
                    }
                }
            }
            pathFrontierCount0x54008 = 0;
            int nextCount = nextPathFrontierCount0x5400A;
            for (int i = 0; i < nextCount; i++) {
                if (tokenSize == 1) {
                    int cell = searchScratchState0x54190.nextPathFrontierCells0x23F4[i] & 0xFFFF;
                    if (staticPath) {
                        expandOneTileStaticPathCostToCurrentFrontier(unit, cell);
                    } else {
                        expandOneTileDynamicPathCostToCurrentFrontier(unit, cell);
                    }
                } else {
                    int x = nextPathFrontierX0x52008[i] & 0xFF;
                    int y = nextPathFrontierY0x53008[i] & 0xFF;
                    if (staticPath) {
                        expandStaticPathCostToCurrentFrontier(unit, x, y);
                    } else {
                        expandDynamicPathCostToCurrentFrontier(unit, x, y);
                    }
                }
            }
            steps += 2;
        }

        if (!staticPath) {
            refreshUnitFootprintFromTargetHandle(unit);
        }

        if (staticPath) {
            finalizeStaticMissionPathSearchResult(unit, startX, startY, targetX, targetY);
        } else {
            finalizeDynamicMissionPathSearchResult(unit, startX, startY, targetX, targetY, targetContext);
        }
    }

    /**
     * Native: CWorldMap::buildCellLineWorkList @0054E39D.
     * Fully ported.
     */
    private CustomList<Short> buildCellLineWorkList(int startX, int startY, int targetX, int targetY) {
        shortWorkList0x54068.clear();
        int x = startX & 0xFF;
        int y = startY & 0xFF;
        int endX = targetX & 0xFF;
        int endY = targetY & 0xFF;
        shortWorkList0x54068.add((short) packCell(x, y));

        int dx = Math.abs(endX - x);
        int dy = Math.abs(endY - y);
        int error;
        int errorStep;
        int diagonalErrorStep;
        int stepX = endX < x ? -1 : 1;
        int stepY = endY < y ? -1 : 1;
        if (dy < dx) {
            error = dy * 2 - dx;
            errorStep = dy * 2;
            diagonalErrorStep = (dy - dx) * 2;
            while (x != endX) {
                x += stepX;
                if (error >= 0) {
                    y += stepY;
                    error += diagonalErrorStep;
                } else {
                    error += errorStep;
                }
                shortWorkList0x54068.add((short) packCell(x, y));
            }
        } else {
            error = dx * 2 - dy;
            errorStep = dx * 2;
            diagonalErrorStep = (dx - dy) * 2;
            while (y != endY) {
                y += stepY;
                if (error >= 0) {
                    x += stepX;
                    error += diagonalErrorStep;
                } else {
                    error += errorStep;
                }
                shortWorkList0x54068.add((short) packCell(x, y));
            }
        }
        return shortWorkList0x54068;
    }

    /**
     * Fully ported native support extracted from CWorldMap::rebuildMissionPathSearch @0054D76E static fallback branch
     * and CWorldMap::rebuildStaticMissionPathList @0054E58C.
     */
    private void finalizeStaticMissionPathSearchResult(Unit unit, int startX, int startY, int targetX, int targetY) {
        int targetCell = packCell(targetX, targetY);
        if ((layer3_0x30000[targetCell] & 0xFFFF) == 0xFFFF) {
            int distance = cellChebyshevDistance(packCell(startX, startY), targetCell);
            int requestedTargetCell = targetCell;
            int fallbackCell = findNearestReachablePathCell(unit, requestedTargetCell, (distance >> 2) + 4);
            int fallbackDistance = cellChebyshevDistance(fallbackCell, requestedTargetCell);
            if (getBuildingAtCell(requestedTargetCell) == null) {
                if (1 < fallbackDistance) {
                    CServerApp.sendGameEventNotification(CServerApp.PATH_UNREACHABLE_EVENT, 0, unit.owner);
                }
            } else if (2 < fallbackDistance) {
                CServerApp.sendGameEventNotification(CServerApp.PATH_UNREACHABLE_EVENT, 0, unit.owner);
            }
            if (unit.unitGroup.missionState.scriptRuntimeState == MissionScriptRuntime.GROUP_SCRIPT_STATE_PATROL
                    && unit.missionRuntimeState.command == MissionScriptRuntime.UNIT_MISSION_COMMAND_ENGAGE_TARGET) {
                int fallbackFootprintDistance = getCellDistanceToFootprint(
                        fallbackCell,
                        requestedTargetCell,
                        unit.getTokenSizeVirtual()
                );
                if ((missionScriptRuntime0x58D80.getCastRangeForFirstCastableSpellOrFallback(unit) & 0xFF)
                        < fallbackFootprintDistance) {
                    unit.missionRuntimeState.w1 = Globals.gameServer.someValue;
                }
            }
            if (fallbackCell == 0) {
                unit.mList1.clear();
                return;
            }
            targetCell = fallbackCell;
            targetX = targetCell & 0xFF;
            targetY = (targetCell >>> 8) & 0xFF;
        }
        rebuildMissionPathList(unit, unit.mList1, startX, startY, targetX, targetY);
    }

    /**
     * Native: CWorldMap::getCellDistanceToFootprint @00556F56.
     * Fully ported.
     */
    private static int getCellDistanceToFootprint(int cell, int footprintCell, int footprintSize) {
        int cellX = cell & 0xFF;
        int cellY = (cell >>> 8) & 0xFF;
        int footprintX = footprintCell & 0xFF;
        int footprintY = (footprintCell >>> 8) & 0xFF;
        int size = footprintSize & 0xFF;
        int dx;
        if (cellX < footprintX) {
            dx = (footprintX - cellX - size) + 1;
            if (dx < 0) {
                dx = 0;
            }
        } else {
            dx = cellX - footprintX;
        }

        int dy;
        if (cellY < footprintY) {
            dy = (footprintY - cellY - size) + 1;
            if (dy < 0) {
                dy = 0;
            }
        } else {
            dy = cellY - footprintY;
        }
        return Math.max(dx, dy) & 0xFF;
    }

    /**
     * Fully ported native support extracted from CWorldMap::rebuildMissionPathSearch @0054D76E dynamic fallback branch
     * and CWorldMap::rebuildDynamicMissionPathList @0054E886.
     */
    private void finalizeDynamicMissionPathSearchResult(Unit unit, int startX, int startY, int targetX, int targetY,
                                                        Token targetContext) {
        int targetCell = packCell(targetX, targetY);
        if ((layer3_0x30000[targetCell] & 0xFFFF) == 0xFFFF) {
            targetCell = targetContext == null
                    ? findNearestReachablePathCell(unit, targetCell, 8)
                    : findBestReachableTargetFootprintCell(unit, targetContext);
            if (targetCell == 0) {
                unit.mList2.clear();
                return;
            }
            targetX = targetCell & 0xFF;
            targetY = (targetCell >>> 8) & 0xFF;
        }
        rebuildMissionPathList(unit, unit.mList2, startX, startY, targetX, targetY);
    }

    /**
     * Native: CWorldMap::findBestReachableTargetFootprintCell @00557F08.
     * Fully ported.
     */
    private int findBestReachableTargetFootprintCell(Unit unit, Token target) {
        int bestCost = 0xFFFF;
        int bestX = 0;
        int bestY = 0;

        int targetSize = target.getTokenSizeVirtual() & 0xFF;
        int unitSize = unit.getTokenSizeVirtual() & 0xFF;
        int highX = (target.m_pTargetHandle.getX() + targetSize) & 0xFFFF;
        int highY = (target.m_pTargetHandle.getY() + targetSize) & 0xFFFF;
        int lowX = (target.m_pTargetHandle.getX() - unitSize) & 0xFFFF;
        int lowY = (target.m_pTargetHandle.getY() - unitSize) & 0xFFFF;

        int quadrant = ((getRawDirection16Code(target, unit) + 2) & 0x0C) >> 2;
        int stepSetIndex = quadrant + 4;
        int targetMinusUnitXdX = (target.getCenterXdX() & 0xFFFF) - (unit.getCenterXdX() & 0xFFFF);
        int targetMinusUnitYdY = (target.getCenterYdY() & 0xFFFF) - (unit.getCenterYdY() & 0xFFFF);

        float slope;
        int intercept;
        switch (quadrant) {
            case 0, 2 -> {
                slope = (float) targetMinusUnitXdX / (float) targetMinusUnitYdY;
                intercept = (int) ((float) (target.getCenterXdX() & 0xFFFF)
                        - (float) (target.getCenterYdY() & 0xFFFF) * slope);
            }
            case 1, 3 -> {
                if (targetMinusUnitXdX == 0) {
                    targetMinusUnitXdX++;
                }
                slope = (float) targetMinusUnitYdY / (float) targetMinusUnitXdX;
                intercept = (int) ((float) (target.getCenterYdY() & 0xFFFF)
                        - (float) (target.getCenterXdX() & 0xFFFF) * slope);
            }
            default -> throw new IllegalStateException("Unexpected perimeter quadrant " + quadrant);
        }

        for (int radius = 1; bestCost == 0xFFFF && radius <= 8; radius++) {
            int forwardX;
            int forwardY;
            switch (quadrant) {
                case 0 -> {
                    forwardX = ((int) ((float) (lowY * 0x100 + 0x80) * slope + (float) intercept)) >> 8;
                    forwardY = lowY;
                }
                case 1 -> {
                    forwardY = ((int) ((float) (highX * 0x100 + 0x80) * slope + (float) intercept)) >> 8;
                    forwardX = highX;
                }
                case 2 -> {
                    forwardX = ((int) ((float) (highY * 0x100 + 0x80) * slope + (float) intercept)) >> 8;
                    forwardY = highY;
                }
                case 3 -> {
                    forwardY = ((int) ((float) (lowX * 0x100 + 0x80) * slope + (float) intercept)) >> 8;
                    forwardX = lowX;
                }
                default -> throw new IllegalStateException("Unexpected perimeter quadrant " + quadrant);
            }
            forwardX &= 0xFFFF;
            forwardY &= 0xFFFF;
            int reverseX = forwardX;
            int reverseY = forwardY;
            int forwardStepIndex = stepSetIndex;
            int reverseStepIndex = stepSetIndex;
            int perimeterSteps = ((((highX - lowX) + highY) - lowY) * 2 >> 1) + 1;
            for (int step = 0; step < perimeterSteps; step++) {
                int forwardCost = layer3_0x30000[packCell(forwardX, forwardY)] & 0xFFFF;
                if (forwardCost < bestCost) {
                    bestCost = forwardCost;
                    bestX = forwardX;
                    bestY = forwardY;
                }

                int reverseCost = layer3_0x30000[packCell(reverseX, reverseY)] & 0xFFFF;
                if (reverseCost < bestCost) {
                    bestCost = reverseCost;
                    bestX = reverseX;
                    bestY = reverseY;
                }

                switch (forwardStepIndex) {
                    case 0, 4, 8 -> {
                        if (forwardX == highX) {
                            forwardStepIndex++;
                        }
                    }
                    case 1, 5, 9 -> {
                        if (forwardY == highY) {
                            forwardStepIndex++;
                        }
                    }
                    case 2, 6, 10 -> {
                        if (forwardX == lowX) {
                            forwardStepIndex++;
                        }
                    }
                    case 3, 7, 11 -> {
                        if (forwardY == lowY) {
                            forwardStepIndex++;
                        }
                    }
                    default -> throw new IllegalStateException("Unexpected forward perimeter step " + forwardStepIndex);
                }
                switch (reverseStepIndex) {
                    case 0, 4, 8 -> {
                        if (reverseX == lowX) {
                            reverseStepIndex--;
                        }
                    }
                    case 1, 5, 9 -> {
                        if (reverseY == lowY) {
                            reverseStepIndex--;
                        }
                    }
                    case 2, 6, 10 -> {
                        if (reverseX == highX) {
                            reverseStepIndex--;
                        }
                    }
                    case 3, 7, 11 -> {
                        if (reverseY == highY) {
                            reverseStepIndex--;
                        }
                    }
                    default -> throw new IllegalStateException("Unexpected reverse perimeter step " + reverseStepIndex);
                }

                Point8 forwardStep = getFlatCardinalStep(forwardStepIndex);
                Point8 reverseStep = getFlatCardinalStep(reverseStepIndex);
                forwardX = (forwardX + forwardStep.x) & 0xFFFF;
                forwardY = (forwardY + forwardStep.y) & 0xFFFF;
                reverseX = (reverseX - reverseStep.x) & 0xFFFF;
                reverseY = (reverseY - reverseStep.y) & 0xFFFF;
                if (100 < step) {
                    break;
                }
            }
            lowX = (lowX - 1) & 0xFFFF;
            lowY = (lowY - 1) & 0xFFFF;
            highX = (highX + 1) & 0xFFFF;
            highY = (highY + 1) & 0xFFFF;
        }
        return bestCost == 0xFFFF ? 0 : packCell(bestX, bestY);
    }

    /**
     * Native: CWorldMap::getRawDirection16Code @00557CF1.
     * Fully ported.
     */
    private static int getRawDirection16Code(Token first, Token second) {
        int dx = (second.getCenterXdX() & 0xFFFF) - (first.getCenterXdX() & 0xFFFF);
        int dy = (second.getCenterYdY() & 0xFFFF) - (first.getCenterYdY() & 0xFFFF);
        int absDx = Math.abs(dx);
        int absDy = Math.abs(dy);

        if (dx < 1) {
            if (dy < 1) {
                if (absDy < absDx) {
                    return absDy * 2 < absDx ? 0x0C : 0x0D;
                }
                return absDx * 2 < absDy ? 0x0F : 0x0E;
            }
            if (absDy < absDx) {
                return absDy * 2 < absDx ? 0x0B : 0x0A;
            }
            return absDx * 2 < absDy ? 0x08 : 0x09;
        }
        if (dy < 1) {
            if (absDy < absDx) {
                return absDy * 2 < absDx ? 0x03 : 0x02;
            }
            return absDx * 2 < absDy ? 0x00 : 0x01;
        }
        if (absDy < absDx) {
            return absDy * 2 < absDx ? 0x04 : 0x05;
        }
        return absDx * 2 < absDy ? 0x07 : 0x06;
    }

    /**
     * Native support extracted from CWorldMap::findBestReachableTargetFootprintCell @00557F08.
     */
    private Point8 getFlatCardinalStep(int stepIndex) {
        return cardinalStepSets0x54156[stepIndex / 4][stepIndex & 3];
    }

    /**
     * Native: CWorldMap::findNearestReachablePathCell @005585F4.
     * Fully ported.
     */
    private int findNearestReachablePathCell(Unit unit, int targetCell, int maxRadius) {
        int bestCost = 0xFFFF;
        int bestCell = 0;
        int target = targetCell & 0xFFFF;
        for (int radius = 1; radius < (maxRadius & 0xFFFF); radius++) {
            for (int delta = -radius; delta < radius + 1; delta++) {
                int bottomCell = (target + radius * 0x100 + delta) & 0xFFFF;
                int bottomCost = layer3_0x30000[bottomCell] & 0xFFFF;
                if (bottomCost < bestCost) {
                    bestCost = bottomCost;
                    bestCell = bottomCell;
                }
                int topCell = (target - radius * 0x100 + delta) & 0xFFFF;
                int topCost = layer3_0x30000[topCell] & 0xFFFF;
                if (topCost < bestCost) {
                    bestCost = topCost;
                    bestCell = topCell;
                }
                int rightCell = (target + (delta << 8) + radius) & 0xFFFF;
                int rightCost = layer3_0x30000[rightCell] & 0xFFFF;
                if (rightCost < bestCost) {
                    bestCost = rightCost;
                    bestCell = rightCell;
                }
                int leftCell = (target + (delta << 8) - radius) & 0xFFFF;
                int leftCost = layer3_0x30000[leftCell] & 0xFFFF;
                if (leftCost < bestCost) {
                    bestCost = leftCost;
                    bestCell = leftCell;
                }
            }
            if (bestCost != 0xFFFF) {
                break;
            }
        }
        return bestCost == 0xFFFF ? 0 : bestCell;
    }

    /**
     * Fully ported. Native: CWorldMap::expandStaticPathCostToNextFrontier @0055C6F0.
     */
    private void expandStaticPathCostToNextFrontier(Unit unit, int x, int y) {
        expandPathCostNeighbors(unit, x, y, false, false);
    }

    /**
     * Fully ported. Native: CWorldMap::expandStaticPathCostToCurrentFrontier @0055C910.
     */
    private void expandStaticPathCostToCurrentFrontier(Unit unit, int x, int y) {
        expandPathCostNeighbors(unit, x, y, false, true);
    }

    /**
     * Fully ported. Native: CWorldMap::expandDynamicPathCostToNextFrontier @0055CB30.
     */
    private void expandDynamicPathCostToNextFrontier(Unit unit, int x, int y) {
        expandPathCostNeighbors(unit, x, y, true, false);
    }

    /**
     * Fully ported. Native: CWorldMap::expandDynamicPathCostToCurrentFrontier @0055CDF0.
     */
    private void expandDynamicPathCostToCurrentFrontier(Unit unit, int x, int y) {
        expandPathCostNeighbors(unit, x, y, true, true);
    }

    /**
     * Fully ported. Native: CWorldMap::expandOneTileDynamicPathCostToNextFrontier @0055E050.
     */
    private void expandOneTileDynamicPathCostToNextFrontier(Unit unit, int cell) {
        expandOneTilePathCostNeighbors(unit, cell, true, false);
    }

    /**
     * Fully ported. Native: CWorldMap::expandOneTileDynamicPathCostToCurrentFrontier @0055E870.
     */
    private void expandOneTileDynamicPathCostToCurrentFrontier(Unit unit, int cell) {
        expandOneTilePathCostNeighbors(unit, cell, true, true);
    }

    /**
     * Fully ported. Native: CWorldMap::expandOneTileStaticPathCostToNextFrontier @0055D010.
     */
    private void expandOneTileStaticPathCostToNextFrontier(Unit unit, int cell) {
        expandOneTilePathCostNeighbors(unit, cell, false, false);
    }

    /**
     * Fully ported. Native: CWorldMap::expandOneTileStaticPathCostToCurrentFrontier @0055D830.
     */
    private void expandOneTileStaticPathCostToCurrentFrontier(Unit unit, int cell) {
        expandOneTilePathCostNeighbors(unit, cell, false, true);
    }

    /**
     * Fully ported native support extracted from CWorldMap one-tile path-cost expansion helpers @0055E050, @0055E870,
     *
     * @0055D010, and @0055D830.
     */
    private void expandOneTilePathCostNeighbors(Unit unit, int cell, boolean dynamicLayer, boolean currentFrontier) {
        int source = cell & 0xFFFF;
        int movementMask = unit.movementState.movementLayerMask & 0xFF;
        boolean useTerrainCosts = (unit.getMovementType() & 0xFF) == 1;
        expandOneTilePathCostNeighbor(
                source, source - 0x101, movementMask, useTerrainCosts, dynamicLayer, currentFrontier, true);
        expandOneTilePathCostNeighbor(
                source, source - 0x0FF, movementMask, useTerrainCosts, dynamicLayer, currentFrontier, true);
        expandOneTilePathCostNeighbor(
                source, source + 0x0FF, movementMask, useTerrainCosts, dynamicLayer, currentFrontier, true);
        expandOneTilePathCostNeighbor(
                source, source + 0x101, movementMask, useTerrainCosts, dynamicLayer, currentFrontier, true);
        expandOneTilePathCostNeighbor(
                source, source - 0x100, movementMask, useTerrainCosts, dynamicLayer, currentFrontier, false);
        expandOneTilePathCostNeighbor(
                source, source - 1, movementMask, useTerrainCosts, dynamicLayer, currentFrontier, false);
        expandOneTilePathCostNeighbor(
                source, source + 1, movementMask, useTerrainCosts, dynamicLayer, currentFrontier, false);
        expandOneTilePathCostNeighbor(
                source, source + 0x100, movementMask, useTerrainCosts, dynamicLayer, currentFrontier, false);
    }

    /**
     * Fully ported native support extracted from CWorldMap one-tile path-cost expansion helpers @0055E050, @0055E870,
     *
     * @0055D010, and @0055D830.
     */
    private void expandOneTilePathCostNeighbor(int sourceCell, int candidateCell, int movementMask,
                                               boolean useTerrainCosts, boolean dynamicLayer,
                                               boolean currentFrontier, boolean diagonal) {
        int candidate = candidateCell & 0xFFFF;
        byte[] layer = dynamicLayer ? layer2_0x20000 : layer1_0x10000;
        if ((layer[candidate] & movementMask) != 0) {
            return;
        }
        int sourceCost = layer3_0x30000[sourceCell & 0xFFFF] & 0xFFFF;
        int stepCost;
        if (useTerrainCosts) {
            int terrainCost = layer0_0x00000[candidate] & 0xFF;
            stepCost = diagonal ? terrainCost + (terrainCost >> 1) : terrainCost;
        } else {
            stepCost = diagonal ? 3 : 2;
        }
        int candidateCost = (sourceCost + stepCost) & 0xFFFF;
        if (candidateCost < (layer3_0x30000[candidate] & 0xFFFF)) {
            layer3_0x30000[candidate] = (short) candidateCost;
            if (currentFrontier) {
                searchScratchState0x54190.pathFrontierCells0x03F4[pathFrontierCount0x54008] = (short) candidate;
                pathFrontierCount0x54008 = (pathFrontierCount0x54008 + 1) & 0xFFFF;
            } else {
                searchScratchState0x54190.nextPathFrontierCells0x23F4[nextPathFrontierCount0x5400A] =
                        (short) candidate;
                nextPathFrontierCount0x5400A = (nextPathFrontierCount0x5400A + 1) & 0xFFFF;
            }
        }
    }

    /**
     * Fully ported native support extracted from CWorldMap path-cost expansion helpers @0055C6F0, @0055C910,
     *
     * @0055CB30, and @0055CDF0.
     */
    private void expandPathCostNeighbors(Unit unit, int x, int y, boolean dynamicLayer, boolean currentFrontier) {
        int sourceCell = packCell(x, y);
        int sourceCost = layer3_0x30000[sourceCell] & 0xFFFF;
        boolean useTerrainCosts = (unit.getMovementType() & 0xFF) == 1;
        for (int dx = -1; dx < 2; dx++) {
            for (int dy = -1; dy < 2; dy++) {
                int candidateX = (x + dx) & 0xFF;
                int candidateY = (y + dy) & 0xFF;
                int candidateCell = packCell(candidateX, candidateY);
                boolean passable = dynamicLayer
                        ? hasClearLayer2Footprint(unit, candidateCell)
                        : hasClearLayer1Footprint(unit, candidateCell);
                if (passable) {
                    int stepCost;
                    if ((dx == 0) || (dy == 0)) {
                        stepCost = useTerrainCosts
                                ? layer0_0x00000[candidateCell] & 0xFF
                                : 2;
                    } else {
                        if (useTerrainCosts) {
                            int terrainCost = layer0_0x00000[candidateCell] & 0xFF;
                            stepCost = terrainCost + (terrainCost >> 1);
                        } else {
                            stepCost = 3;
                        }
                    }
                    int candidateCost = (sourceCost + stepCost) & 0xFFFF;
                    if (candidateCost < (layer3_0x30000[candidateCell] & 0xFFFF)) {
                        layer3_0x30000[candidateCell] = (short) candidateCost;
                        if (currentFrontier) {
                            pathFrontierX0x50008[pathFrontierCount0x54008] = (byte) candidateX;
                            pathFrontierY0x51008[pathFrontierCount0x54008] = (byte) candidateY;
                            pathFrontierCount0x54008 = (pathFrontierCount0x54008 + 1) & 0xFFFF;
                        } else {
                            nextPathFrontierX0x52008[nextPathFrontierCount0x5400A] = (byte) candidateX;
                            nextPathFrontierY0x53008[nextPathFrontierCount0x5400A] = (byte) candidateY;
                            nextPathFrontierCount0x5400A = (nextPathFrontierCount0x5400A + 1) & 0xFFFF;
                        }
                    }
                }
            }
        }
    }

    /**
     * Fully ported native support extracted from CWorldMap::rebuildStaticMissionPathList @0054E58C and
     * CWorldMap::rebuildDynamicMissionPathList @0054E886.
     */
    private void rebuildMissionPathList(Unit unit, CustomList<Short> pathList,
                                        int startX, int startY, int targetX, int targetY) {
        int x = targetX & 0xFF;
        int y = targetY & 0xFF;
        pathList.add(0, (short) packCell(x, y));
        int steps = 0;
        while (x != (startX & 0xFF) || y != (startY & 0xFF)) {
            int bestCost = 0xFFFF;
            int bestDx = 0;
            int bestDy = 0;
            steps++;
            if (1000 < steps) {
                break;
            }
            for (int dx = -1; dx < 2; dx++) {
                for (int dy = -1; dy < 2; dy++) {
                    int candidateX = x + dx;
                    int candidateY = y + dy;
                    if (7 < candidateX && candidateX <= mapWidth0x50000 + 8
                            && 7 < candidateY && candidateY <= mapHeight0x50004 + 8) {
                        int layer3Cost = layer3_0x30000[packCell(candidateX, candidateY)] & 0xFFFF;
                        if (layer3Cost != 0xFFFF) {
                            int terrainCost = (unit.getMovementType() & 0xFF) == 1
                                    ? layer0_0x00000[packCell(x, y)] & 0xFF
                                    : 2;
                            int candidateCost;
                            if (dx == 0 || dy == 0) {
                                candidateCost = layer3Cost + terrainCost;
                                if (candidateCost <= bestCost) {
                                    bestDx = dx;
                                    bestDy = dy;
                                    bestCost = candidateCost;
                                }
                            } else {
                                candidateCost = layer3Cost + terrainCost + (terrainCost >> 1);
                                if (candidateCost < bestCost) {
                                    bestDx = dx;
                                    bestDy = dy;
                                    bestCost = candidateCost;
                                }
                            }
                        }
                    }
                }
            }
            x = (x + bestDx) & 0xFF;
            y = (y + bestDy) & 0xFF;
            pathList.add(0, (short) packCell(x, y));
        }
        if (steps < 0x3E9) {
            pathList.removeFirst();
        } else {
            pathList.clear();
        }
    }

    /**
     * Native support extracted from CWorldMap::requestMissionMoveToCell @00555411.
     * Mirrors the inline direction/rotate branch through CWorldMap::direction8CodeToCell @00556B2C and
     * CWorldMap::rotateUnitTowardCell @00556DA2.
     */
    private void applyMissionMoveFacingCommand(Unit unit, int cell) {
        int direction = direction8CodeToCell(unit, cell);
        if ((unit.movementState.facing & 0xFF) == direction) {
            unit.missionRuntimeState.command = 0;
        } else {
            rotateUnitTowardFacing(unit, direction);
            unit.missionRuntimeState.command = 10;
        }
    }

    /**
     * Native: CWorldMap::consumeDynamicMissionPathCell @00555C7E.
     * Fully ported.
     */
    private void consumeDynamicMissionPathCell(Unit unit) {
        if (unit.mList2.isEmpty()) {
            advanceUnitRotation(unit);
            return;
        }
        int nextCell = unit.mList2.getFirst() & 0xFFFF;
        unit.movementState.positionCellX = nextCell & 0xFF;
        unit.movementState.positionCellY = (nextCell >>> 8) & 0xFF;
        unit.movementState.pathCurrentCell = unit.m_pTargetHandle.getCell();
        unit.movementState.movementStepState = 0;
        refreshUnitFootprintForDetach(unit);
        refreshUnitFootprintForMissionStep(unit, nextCell);
        unitByCell.values().remove(unit);
        unitByCell.put(unit.m_pTargetHandle.getCell(), unit);
        int direction = getSubTileDirection8CodeToCellCenter(unit, nextCell);
        unit.movementState.facingLast = direction;
        if ((unit.movementState.facing & 0xFF) == direction) {
            configureMissionSubTileStep(unit, unit.movementState.pathCurrentCell, direction);
            unit.movementState.stepTick = 0;
            advanceUnitSubTileStep(unit);
        } else {
            rotateUnitTowardFacing(unit, direction);
        }
    }

    /**
     * Fully ported native support extracted from CWorldMap::refreshUnitFootprint @0055BE86 mode=0.
     */
    private void refreshUnitFootprintForMissionStep(Unit unit, int cell) {
        int size = unit.getTokenSizeVirtual() & 0xFF;
        Arrays.fill(array64Bytes0xA452C, (byte) 0);
        for (int i = 0; i < size + 2; i++) {
            for (int j = 0; j < size + 2; j++) {
                array64Bytes0xA452C[i * 8 + j] = 1;
            }
        }

        int currentX = unit.m_pTargetHandle.getX();
        int currentY = unit.m_pTargetHandle.getY();
        int nextXDelta = (cell & 0xFF) - currentX;
        int nextYDelta = ((cell >>> 8) & 0xFF) - currentY;
        for (int i = 1; i < size + 1; i++) {
            for (int j = 1; j < size + 1; j++) {
                array64Bytes0xA452C[(i + nextXDelta) * 8 + j + nextYDelta] = 2;
            }
        }
        for (int i = 1; i < size + 1; i++) {
            for (int j = 1; j < size + 1; j++) {
                array64Bytes0xA452C[i * 8 + j] = 3;
            }
        }

        int beforeX = unit.m_pTargetHandle.getX() - 1;
        int beforeY = unit.m_pTargetHandle.getY() - 1;
        for (int i = 0; i < size + 2; i++) {
            for (int j = 0; j < size + 2; j++) {
                int packedCell = packCell(beforeX + i, beforeY + j);
                int marker = array64Bytes0xA452C[i * 8 + j] & 0xFF;
                if (marker == 1) {
                    clearUnitFromNodeLayerState(unit, packedCell);
                } else if (marker == 2 || marker == 3) {
                    setUnitInNodeLayerState(unit, packedCell);
                }
            }
        }
        unit.movementState.cell = cell & 0xFFFF;
    }

    /**
     * Native: CWorldMap::advanceUnitRotation @005551EF.
     * Fully ported.
     */
    public static void advanceUnitRotation(Unit unit) {
        int facing = unit.movementState.facing & 0xFF;
        int target = unit.movementState.facingLast & 0xFF;
        int forward = (target - facing) & 0xFF;
        int backward = (facing - target) & 0xFF;
        int speed = unit.movementState.rotationSpeed & 0xFF;
        if (Math.min(forward, backward) < speed) {
            unit.movementState.facing = target;
        } else if (forward <= backward) {
            unit.movementState.facing = (facing + speed) & 0xFF;
        } else {
            unit.movementState.facing = (facing - speed) & 0xFF;
        }
    }

    /**
     * Native: CWorldMap::rotateUnitTowardFacing @00556454.
     * Fully ported.
     */
    public void rotateUnitTowardFacing(Unit unit, int facing) {
        unit.movementState.facingLast = facing & 0xFF;
        int currentFacing = unit.movementState.facing & 0xFF;
        int forward = (unit.movementState.facingLast - currentFacing) & 0xFF;
        int backward = (currentFacing - unit.movementState.facingLast) & 0xFF;
        int shortest = Math.min(forward, backward);
        if (unit.movementState.rotationActive == 0) {
            unit.movementState.rotationTicks = 0;
        }
        if (unit.movementState.rotationActive == 0 && shortest < 0x21) {
            unit.movementState.facing = unit.movementState.facingLast;
            unit.movementState.positionChangedStep = 1;
        } else {
            advanceUnitRotation(unit);
            int speed = unit.movementState.rotationSpeed & 0xFF;
            unit.movementState.positionChangedStep = (shortest + speed - 1) / speed;
        }
        unit.movementState.rotationActive = 1;
        unit.movementState.rotationTicks = (unit.movementState.rotationTicks + 1) & 0xFF;
        if ((unit.movementState.facing & 0xFF) == (unit.movementState.facingLast & 0xFF)) {
            unit.movementState.rotationActive = 0;
        }
    }

    /**
     * Native: CWorldMap::rotateUnitTowardTarget @00556D78.
     * Fully ported.
     */
    public void rotateUnitTowardTarget(Unit unit, Token target) {
        rotateUnitTowardFacing(unit, getDirection8Code(unit, target));
    }

    /**
     * Native: CWorldMap::rotateUnitTowardCell @00556DA2.
     * Fully ported.
     */
    public void rotateUnitTowardCell(Unit unit, int cell) {
        rotateUnitTowardFacing(unit, direction8CodeToCell(unit, cell));
    }

    /**
     * Native: CWorldMap::getSubTileDirection8CodeToCellCenter @0054F618.
     * Fully ported.
     */
    public static int getSubTileDirection8CodeToCellCenter(Unit unit, int cell) {
        int result = unit.movementState.facing & 0xFF;
        int dx = ((cell & 0xFF) * 0x100 + 0x80) - (unit.m_pTargetHandle.packXdX() & 0xFFFF);
        int dy = (((cell & 0xFFFF) >>> 8) * 0x100 + 0x80) - (unit.m_pTargetHandle.packYdY() & 0xFFFF);
        if (dx < 1) {
            if (dx < 0) {
                if (dy < 1) {
                    result = dy < 0 ? 7 : 6;
                } else {
                    result = 5;
                }
            } else if (dy < 1) {
                if (dy < 0) {
                    result = 0;
                }
            } else {
                result = 4;
            }
        } else if (dy < 1) {
            result = dy < 0 ? 1 : 2;
        } else {
            result = 3;
        }
        return (result << 5) & 0xFF;
    }

    /**
     * Native: CWorldMap::resolveMissionMovementSpeed @0055915A.
     * Fully ported.
     */
    private static int resolveMissionMovementSpeed(Unit unit) {
        int speedOverride = unit.unitGroup.missionState.getMissionScriptSpeedOverride();
        return speedOverride == 0 ? unit.speed & 0xFFFF : speedOverride;
    }

    /**
     * Native: CWorldMap::getEffectiveTerrainCostForMovement @0055A147.
     * Fully ported.
     */
    private int getEffectiveTerrainCostForMovement(int cell) {
        int packedCell = cell & 0xFFFF;
        if ((layer1_0x10000[packedCell] & 0x20) != 0) {
            WorldMapNode node = lookupNode(packedCell);
            if (node != null && node.getEffectsCount() != 0) {
                layer0_0x00000[packedCell] = (byte) ((layer0_0x00000[packedCell] & 0xFF) >> 2);
                return layer0_0x00000[packedCell] & 0xFF;
            }
        }
        return layer0_0x00000[packedCell] & 0xFF;
    }

    /**
     * Native: CWorldMap::configureMissionSubTileStep @00559195.
     * Fully ported.
     */
    private int configureMissionSubTileStep(Unit unit, int currentCell, int facing) {
        int directionIndex = ((facing + 0x10) & 0xFF) >>> 5;
        int nextCell = (currentCell + neighborStepTable0x58E88.cellDelta[directionIndex]) & 0xFFFF;
        int speed = resolveMissionMovementSpeed(unit);
        if ((unit.getMovementType() & 0xFF) == 1) {
            int heightDelta = (byte) ((unkByteArray0x944F4[currentCell & 0xFFFF] & 0xFF)
                    - (unkByteArray0x944F4[nextCell] & 0xFF));
            if (Math.abs(heightDelta) > 0x20) {
                heightDelta = heightDelta < 0 ? -0x20 : 0x20;
            }
            speed *= speedMultiplier0x58D84;
            if (heightDelta < 0) {
                speed -= (speed * Math.abs(heightDelta)) >> 6;
            } else {
                speed += (speed * heightDelta) >> 6;
            }
            int terrainCost = ((getEffectiveTerrainCostForMovement(currentCell)
                    + getEffectiveTerrainCostForMovement(nextCell)) & 0xFF) >> 1;
            if (terrainCost == 0) {
                terrainCost = 8;
            }
            speed /= terrainCost;
        }
        speed = Math.max(1, Math.min(speed, 0x3F));
        int dx = neighborStepTable0x58E88.dx[directionIndex];
        int dy = neighborStepTable0x58E88.dy[directionIndex];
        int deltaX = dx * speed;
        int deltaY = dy * speed;
        if (dx != 0 && dy != 0) {
            deltaX = (int) (deltaX * 0.707d);
            deltaY = (int) (deltaY * 0.707d);
        }
        unit.movementState.positionCellX = nextCell & 0xFF;
        unit.movementState.positionCellY = (nextCell >>> 8) & 0xFF;
        unit.movementState.subTileStepDistance = speed;
        unit.movementState.moveDirOrMode = directionIndex;
        unit.movementState.deltaXdX = deltaX;
        unit.movementState.deltaYdY = deltaY;
        int stepDelta = ((byte) unit.movementState.deltaXdX) == 0
                ? Math.abs((byte) unit.movementState.deltaYdY)
                : Math.abs((byte) unit.movementState.deltaXdX);
        stepDelta &= 0xFF;
        if (stepDelta == 0) {
            stepDelta = 1;
        }
        unit.movementState.stepTickLimit = (0x100 + stepDelta - 1) / stepDelta;
        return speed;
    }

    /**
     * Native: CWorldMap::AdvanceUnitSubTileStep @00554FD2.
     * Fully ported.
     */
    private void advanceUnitSubTileStep(Unit unit) {
        int oldXdX = unit.m_pTargetHandle.packXdX() & 0xFFFF;
        int oldYdY = unit.m_pTargetHandle.packYdY() & 0xFFFF;
        unit.movementState.stepTick = (unit.movementState.stepTick + 1) & 0xFFFF;
        int newXdX = (oldXdX + (byte) unit.movementState.deltaXdX) & 0xFFFF;
        int newYdY = (oldYdY + (byte) unit.movementState.deltaYdY) & 0xFFFF;
        int moveMode = unit.movementState.moveDirOrMode & 0xFFFF;
        if ((moveMode == 1 || moveMode == 5) && (newXdX & 0xFF) == 0 && (newYdY & 0xFF) == 0) {
            newXdX = (newXdX - 1) & 0xFFFF;
        }
        if ((oldXdX >>> 8) == (newXdX >>> 8) && (oldYdY >>> 8) == (newYdY >>> 8)) {
            unit.m_pTargetHandle.setPos(newXdX, newYdY);
        } else {
            int previousCell = unit.m_pTargetHandle.getCell();
            refreshUnitFootprintForDetach(unit);
            detachUnitFootprint(unit);
            unit.m_pTargetHandle.setPos(newXdX, newYdY);
            refreshSteppedUnitCell(unit);
            refreshUnitFootprintForMissionStep(unit, previousCell);
            unitByCell.values().remove(unit);
            unitByCell.put(unit.m_pTargetHandle.getCell(), unit);
        }
        if ((unit.movementState.stepTickLimit & 0xFFFF) <= (unit.movementState.stepTick & 0xFFFF)) {
            unit.m_pTargetHandle.clearSubPos();
        }
    }

    /**
     * Fully ported. Native: CWorldMap::refreshSteppedUnitCell @0055020A.
     */
    public boolean refreshSteppedUnitCell(Unit unit) {
        int size = unit.getTokenSizeVirtual() & 0xFF;
        int x = unit.m_pTargetHandle.getX();
        int y = unit.m_pTargetHandle.getY();
        unit.movementState.terrainStepDistance = calculateUnitTerrainStepDistance(unit, x, y);
        unit.movementState.attachCellX = x;
        unit.movementState.attachCellY = y;
        unit.movementState.attachCellXdX = unit.m_pTargetHandle.packXdX();
        unit.movementState.attachCellYdY = unit.m_pTargetHandle.packYdY();

        if (!canPlaceUnitFootprint(unit)
                && unit.movementState.cell != unit.m_pTargetHandle.getCell()) {
            // Native calls diagnostic helpers here at @0055020A and then continues trying to attach.
        }

        for (int cellY = 0; cellY < size; cellY++) {
            for (int cellX = 0; cellX < size; cellX++) {
                if (!attachUnitToNodeCell(unit, x + cellX, y + cellY)) {
                    return false;
                }
            }
        }
        refreshUnitFootprintFromTargetHandle(unit);
        return true;
    }

    /**
     * Fully ported. Native: CWorldMap::canPlaceUnitFootprint @005563CA.
     */
    public boolean canPlaceUnitFootprint(Unit unit) {
        return canPlaceUnitFootprintAtCell(unit, unit.m_pTargetHandle.getCell());
    }

    /**
     * Native: CWorldMap::attachUnitToNodeCell @005503AF.
     * Fully ported.
     */
    private boolean attachUnitToNodeCell(Unit unit, int x, int y) {
        int key = packCell(x, y);
        int movementType = unit.getMovementType() & 0xFF;
        if (movementType == 0) {
            return false;
        }

        WorldMapNode node = lookupNode(key);
        if (node == null) {
            createDynamicNodeForCell(key);
            node = lookupNode(key);
            if (node == null) {
                return false;
            }
            setUnitNodeLayer(node, unit, movementType);
            nodeMap.entries.put(key, node);
            refreshNodeLayers(key);
            return true;
        }

        if (movementType < 3) {
            applyTransientNodeSpell(unit, node);
            if (node.groundOccupancyUnit != null) {
                // Native calls diagnostic helper FUN_00556447 here and returns false.
                return false;
            }
            node.groundOccupancyUnit = unit;
            nodeMap.entries.put(key, node);
            refreshNodeLayers(key);
            return true;
        }

        if (movementType == 3) {
            if (node.airOccupancyUnit != null) {
                // Native calls diagnostic helper FUN_00556447 here and returns false.
                return false;
            }
            node.airOccupancyUnit = unit;
            nodeMap.entries.put(key, node);
            refreshNodeLayers(key);
            return true;
        }

        return false;
    }

    /**
     * Native support extracted from CWorldMap::attachUnitToNodeCell @005503AF.
     * Fully ported.
     */
    private static void setUnitNodeLayer(WorldMapNode node, Unit unit, int movementType) {
        if (movementType < 3) {
            node.groundOccupancyUnit = unit;
        } else if (movementType == 3) {
            node.airOccupancyUnit = unit;
        }
    }

    /**
     * Native support extracted from CWorldMap::attachUnitToNodeCell @005503AF transient spell branch.
     * Fully ported.
     */
    private void applyTransientNodeSpell(Unit unit, WorldMapNode node) {
        int spellId = node.getTransientSpellCastId();
        if (spellId == 0) {
            return;
        }
        if (spellId == SpellId.TELEPORT.id) {
            int targetX = node.getTransientSpellCastTargetX();
            int targetY = node.getTransientSpellCastTargetY();
            unit.missionRuntimeState.pendingMissionEntryCell = packCell(targetX, targetY);
            return;
        }

        Spell spell = Globals.gameServer.runtimeSpells[spellId];
        if (spell.canTargetUnit(unit)) {
            Globals.gameServer.objectLists.queueTransientTargetSpellCast(
                    node.getTransientSpellCastSourceX(),
                    node.getTransientSpellCastSourceY(),
                    unit,
                    spellId,
                    node.getTransientSpellCastSkillLevel()
            );
        } else {
            Globals.gameServer.objectLists.queueTransientPointSpellCast(
                    node.getTransientSpellCastSourceX(),
                    node.getTransientSpellCastSourceY(),
                    node.getTransientSpellCastTargetX(),
                    node.getTransientSpellCastTargetY(),
                    spellId,
                    node.getTransientSpellCastSkillLevel()
            );
        }
    }

    /**
     * Native: CWorldMap::direction8CodeToCell @00556B2C.
     * Fully ported.
     */
    public int direction8CodeToCell(Unit unit, int cell) {
        int dx = (cell & 0xFF) - (unit.m_pTargetHandle.getX() & 0xFF);
        int dy = ((cell >>> 8) & 0xFF) - (unit.m_pTargetHandle.getY() & 0xFF);
        int absDx = dx < 1 ? (-((short) dx)) & 0xFFFF : dx & 0xFFFF;
        int absDy = dy < 1 ? (-((short) dy)) & 0xFFFF : dy & 0xFFFF;

        int result;
        if (dx < 1) {
            if (dy < 1) {
                if (absDy < absDx) {
                    result = absDy * 2 < absDx ? 0x0C : 0x0D;
                } else if (absDx * 2 < absDy) {
                    result = 0x0F;
                } else {
                    result = 0x0E;
                }
            } else if (absDy < absDx) {
                result = absDy * 2 < absDx ? 0x0B : 0x0A;
            } else if (absDx * 2 < absDy) {
                result = 0x08;
            } else {
                result = 0x09;
            }
        } else if (dy < 1) {
            if (absDy < absDx) {
                result = absDy * 2 < absDx ? 0x03 : 0x02;
            } else if (absDx * 2 < absDy) {
                result = 0x00;
            } else {
                result = 0x01;
            }
        } else if (absDy < absDx) {
            result = absDy * 2 < absDx ? 0x04 : 0x05;
        } else if (absDx * 2 < absDy) {
            result = 0x07;
        } else {
            result = 0x06;
        }

        if (result != 0) {
            result++;
        }
        return ((result >>> 1) << 5) & 0xFF;
    }

    /**
     * Native: CWorldMap::getCellChebyshevDistance @00558FE9.
     * Fully ported.
     */
    private static int cellChebyshevDistance(int cellA, int cellB) {
        return getChebyshevDistance(
                cellA & 0xFF,
                (cellA >>> 8) & 0xFF,
                cellB & 0xFF,
                (cellB >>> 8) & 0xFF
        );
    }

    /**
     * Native: CWorldMap::getChebyshevDistance @00551463.
     * Fully ported.
     */
    public static int getChebyshevDistance(int x1, int y1, int x2, int y2) {
        int dx = Math.abs((x1 & 0xFF) - (x2 & 0xFF));
        int dy = Math.abs((y1 & 0xFF) - (y2 & 0xFF));
        return Math.max(dx, dy) & 0xFF;
    }

    /**
     * Fully ported native support extracted from CWorldMap::refreshUnitFootprint @0055BE86 target-handle path mode=1.
     */
    private void refreshUnitFootprintFromTargetHandle(Unit unit) {
        int size = unit.getTokenSizeVirtual() & 0xFF;
        int beforeX = unit.m_pTargetHandle.getX() - 1;
        int beforeY = unit.m_pTargetHandle.getY() - 1;
        for (int y = 0; y < size + 2; y++) {
            for (int x = 0; x < size + 2; x++) {
                int packedCell = packCell(beforeX + x, beforeY + y);
                if (x > 0 && x < size + 1 && y > 0 && y < size + 1) {
                    setUnitInNodeLayerState(unit, packedCell);
                } else {
                    clearUnitFromNodeLayerState(unit, packedCell);
                }
            }
        }
        unit.movementState.cell = unit.m_pTargetHandle.getCell();
    }

    /**
     * Fully ported. Native: CWorldMap::setUnitInNodeLayerState @0055C2CE.
     */
    private void setUnitInNodeLayerState(Unit unit, int packedCell) {
        packedCell &= 0xFFFF;
        WorldMapNode node = lookupNode(packedCell);
        if ((layer1_0x10000[packedCell] & 0x20) == 0 || node == null) {
            createDynamicNodeForCell(packedCell);
            node = lookupNode(packedCell);
            if (node == null) {
                return;
            }
        }

        int movementType = unit.getMovementType() & 0xFF;
        if (movementType == 3) {
            if (node.secondaryLayerStateUnit != null && node.secondaryLayerStateUnit != unit) {
                return;
            }
            node.secondaryLayerStateUnit = unit;
        } else {
            if (node.primaryLayerStateUnit != null && node.primaryLayerStateUnit != unit) {
                return;
            }
            node.primaryLayerStateUnit = unit;
        }

        refreshNodeLayers(packedCell);
        nodeMap.entries.put(packedCell, node);
    }

    /**
     * Native: CWorldMap::attachSack @005538C7.
     * Fully ported.
     */
    public boolean attachSack(Sack sack) {
        int packedCell = sack.m_pTargetHandle.getCell();
        if ((layer2_0x20000[packedCell] & 1) != 0) {
            return false;
        }

        WorldMapNode node = lookupNode(packedCell);
        boolean created = false;
        if (node == null) {
            createDynamicNodeForCell(packedCell);
            node = lookupNode(packedCell);
            if (node == null) {
                return false;
            }
            created = true;
        }

        if (node.sack != null) {
            return false;
        }
        node.sack = sack;
        nodeMap.entries.put(packedCell, node);
        sackByCell.put(packedCell, sack);
        if (created) {
            refreshNodeLayers(packedCell);
        }
        return true;
    }

    /**
     * Native: CWorldMap::detachSack @005539E5.
     * Fully ported.
     */
    public boolean detachSack(Sack sack) {
        int packedCell = sack.m_pTargetHandle.getCell();
        WorldMapNode node = lookupNode(packedCell);
        if (node == null) {
            return false;
        }

        node.sack = null;
        nodeMap.entries.put(packedCell, node);
        sackByCell.remove(packedCell);
        refreshNodeLayers(packedCell);
        if (node.isEmpty()) {
            removeDynamicNodeForCell(packedCell);
        }
        return true;
    }

    /**
     * Native: CWorldMap::findSackAtTargetHandle @00553AB3.
     * Fully ported.
     */
    public Sack findSackAtTargetHandle(TargetHandle targetHandle) {
        return findSackAtCell(targetHandle.getCell());
    }

    /**
     * Native: CWorldMap::findSackAtPoint @00553A87.
     * Fully ported.
     */
    public Sack findSackAtPoint(int x, int y) {
        return findSackAtCell(((y & 0xFF) << 8) | (x & 0xFF));
    }

    /**
     * Native support extracted from CWorldMap::findSackAtPoint @00553A87 and CWorldMap::findSackAtCell @00553AD1.
     */
    public boolean hasSackAtCell(int packedCell) {
        return findSackAtCell(packedCell & 0xFFFF) != null;
    }

    /**
     * Native: CWorldMap::findSackAtCell @00553AD1.
     * Fully ported.
     */
    private Sack findSackAtCell(int packedCell) {
        if ((layer1_0x10000[packedCell] & 0x20) == 0) {
            return null;
        }
        WorldMapNode node = lookupNode(packedCell);
        return node == null ? null : node.sack;
    }

    /**
     * Native: CWorldMap::collectSackInteractionCells @00559EED.
     * Fully ported.
     */
    public void collectSackInteractionCells(Unit unit, boolean stopAfterFirst) {
        int sourceCell = unit.m_pTargetHandle.getCell();
        int sightRange = unit.sightRange & 0xFF;
        sackInteractionCells.clear();
        Sack sourceSack = findSackAtCell(sourceCell);
        if (sourceSack != null) {
            sackInteractionCells.addFirst((short) sourceCell);
            if (stopAfterFirst) {
                return;
            }
        }
        for (int ring = 1; ring < sightRange + 1; ring++) {
            for (int delta = -ring; delta < ring + 1; delta++) {
                int bottomCell = sourceCell + ring * 0x100 + delta;
                if (isSackInteractionCandidateCell(unit, bottomCell)) {
                    sackInteractionCells.addFirst((short) bottomCell);
                }
                int topCell = sourceCell - ring * 0x100 + delta;
                if (isSackInteractionCandidateCell(unit, topCell)) {
                    sackInteractionCells.addFirst((short) topCell);
                }
                if (Math.abs(delta) != ring) {
                    int rightCell = sourceCell + delta * 0x100 + ring;
                    if (isSackInteractionCandidateCell(unit, rightCell)) {
                        sackInteractionCells.addFirst((short) rightCell);
                    }
                    int leftCell = sourceCell + delta * 0x100 - ring;
                    if (isSackInteractionCandidateCell(unit, leftCell)) {
                        sackInteractionCells.addFirst((short) leftCell);
                    }
                }
                if (stopAfterFirst && !sackInteractionCells.isEmpty()) {
                    return;
                }
            }
        }
    }

    /**
     * Native: CWorldMap::isSackInteractionCandidateCell @0055A10B.
     * Fully ported.
     */
    private boolean isSackInteractionCandidateCell(Unit unit, int cell) {
        int packedCell = cell & 0xFFFF;
        return findSackAtCell(packedCell) != null && hasClearLayer2Footprint(unit, packedCell);
    }

    /**
     * Fully ported. Native: CWorldMap::findAreaEffectAtLayerCell @0055A88B.
     */
    public AreaEffect findAreaEffectAtLayerCell(AreaEffect areaEffect, int packedCell) {
        packedCell &= 0xFFFF;
        if ((layer1_0x10000[packedCell] & 0x20) == 0) {
            return null;
        }
        WorldMapNode node = lookupNode(packedCell);
        if (node == null) {
            return null;
        }
        return getAreaEffectLayer(node, areaEffect.mapLayer());
    }

    /**
     * Fully ported. Native: CWorldMap::findAreaEffectAtLayerPoint @0055A8F8.
     */
    public AreaEffect findAreaEffectAtLayerPoint(AreaEffect areaEffect, int x, int y) {
        return findAreaEffectAtLayerCell(areaEffect, packCell(x, y));
    }

    /**
     * Fully ported. Native: CWorldMap::attachAreaEffectAtPoint @0055A325.
     */
    public boolean attachAreaEffectAtPoint(AreaEffect areaEffect, int x, int y) {
        return attachAreaEffectAtCell(areaEffect, packCell(x, y));
    }

    /**
     * Fully ported. Native: CWorldMap::attachAreaEffectAtCell @0055A37A.
     */
    public boolean attachAreaEffectAtCell(AreaEffect areaEffect, int packedCell) {
        packedCell &= 0xFFFF;
        WorldMapNode node = lookupNode(packedCell);
        if (node == null) {
            createDynamicNodeForCell(packedCell);
            node = lookupNode(packedCell);
            if (node == null) {
                return false;
            }
        }

        setAreaEffectLayer(node, areaEffect.mapLayer(), areaEffect);
        nodeMap.entries.put(packedCell, node);
        refreshNodeLayers(packedCell);
        return true;
    }

    /**
     * Fully ported. Native: CWorldMap::detachAreaEffectAtPoint @0055A4DE.
     */
    public void detachAreaEffectAtPoint(AreaEffect areaEffect, int x, int y) {
        detachAreaEffectAtCell(areaEffect, packCell(x, y));
    }

    /**
     * Fully ported. Native: CWorldMap::detachAreaEffectAtCell @0055A533.
     */
    public boolean detachAreaEffectAtCell(AreaEffect areaEffect, int packedCell) {
        packedCell &= 0xFFFF;
        WorldMapNode node = lookupNode(packedCell);
        if (node == null) {
            return false;
        }
        int layer = areaEffect.mapLayer();
        if (getAreaEffectLayer(node, layer) != areaEffect) {
            return false;
        }

        setAreaEffectLayer(node, layer, null);
        nodeMap.entries.put(packedCell, node);
        refreshNodeLayers(packedCell);
        if (node.isEmpty()) {
            removeDynamicNodeForCell(packedCell);
        }
        return true;
    }

    /**
     * Native: CWorldMap::markDirectDamageAreaEffectCell @00559C28.
     * Fully ported.
     */
    public void markDirectDamageAreaEffectCell(int x, int y) {
        int packedCell = packCell(x, y);
        layer1_0x10000[packedCell] = (byte) (layer1_0x10000[packedCell] | 0x10);
        layer2_0x20000[packedCell] = (byte) (layer2_0x20000[packedCell] | 0x10);
    }

    /**
     * Native support extracted from CWorldMap::findAreaEffectAtLayerCell @0055A88B WorldMapNode effect-layer lookup.
     */
    private static AreaEffect getAreaEffectLayer(WorldMapNode node, int layer) {
        return node.effectLayer[layer];
    }

    /**
     * Native support extracted from CWorldMap::attachAreaEffectAtCell @0055A37A and
     * CWorldMap::detachAreaEffectAtCell @0055A533 WorldMapNode effect-layer writes.
     */
    private static void setAreaEffectLayer(WorldMapNode node, int layer, AreaEffect areaEffect) {
        node.effectLayer[layer] = areaEffect;
        node.recalculateEffectsCount();
    }

    /**
     * Fully ported. Native: CWorldMap::getAreaEffectLayersAtCell @0055A82C.
     */
    public AreaEffect[] getAreaEffectLayersAtCell(int packedCell) {
        packedCell &= 0xFFFF;
        if ((layer1_0x10000[packedCell] & 0x20) == 0) {
            return null;
        }
        WorldMapNode node = lookupNode(packedCell);
        if (node == null) {
            return null;
        }
        return new AreaEffect[]{
                getAreaEffectLayer(node, 0),
                getAreaEffectLayer(node, 1),
                getAreaEffectLayer(node, 2),
                getAreaEffectLayer(node, 3),
                getAreaEffectLayer(node, 4),
                getAreaEffectLayer(node, 5)
        };
    }

    /**
     * Native support extracted from CWorldMap::getAreaEffectLayersAtCell @0055A82C layer presence check.
     */
    public boolean hasAreaEffectLayerAtCell(int packedCell, int layer) {
        packedCell &= 0xFFFF;
        if ((layer1_0x10000[packedCell] & 0x20) == 0) {
            return false;
        }
        WorldMapNode node = lookupNode(packedCell);
        if (node == null) {
            return false;
        }
        return getAreaEffectLayer(node, layer) != null;
    }

    /**
     * Fully ported. Native: CWorldMap::setTransientSpellCastAtCell @0055BA36.
     */
    public boolean setTransientSpellCastAtCell(int packedCell, TransientSpellCastSpec spec) {
        packedCell &= 0xFFFF;
        if ((layer2_0x20000[packedCell] & 1) != 0) {
            return false;
        }
        WorldMapNode node = lookupNode(packedCell);
        if (node == null) {
            createDynamicNodeForCell(packedCell);
            node = lookupNode(packedCell);
            if (node == null) {
                return false;
            }
            node.setTransientSpellCastSpec(spec);
            nodeMap.entries.put(packedCell, node);
            refreshNodeLayers(packedCell);
            return true;
        }
        boolean alreadyHasSpec = node.hasTransientSpellCastSpec();
        node.setTransientSpellCastSpec(spec);
        nodeMap.entries.put(packedCell, node);
        return !alreadyHasSpec;
    }

    /**
     * Native support extracted from CWorldMap::attachSack @005538C7, CWorldMap::detachSack @005539E5,
     * CWorldMap::findSackAtCell @00553AD1, and CWorldMap::createDynamicNodeForCell @005508A0.
     */
    private WorldMapNode lookupNode(int packedCell) {
        return nodeMap.entries.get(packedCell & 0xFFFF);
    }

    /**
     * Native: CWorldMap::createDynamicNodeForCell @005508A0.
     * Fully ported.
     */
    private void createDynamicNodeForCell(int packedCell) {
        packedCell &= 0xFFFF;
        if (lookupNode(packedCell) != null) {
            return;
        }
        WorldMapNode node = new WorldMapNode();
        node.clear();
        node.setLayer0Cell(layer0_0x00000[packedCell]);
        node.setLayer1Cell(layer1_0x10000[packedCell]);
        node.setKey(packedCell);
        nodeMap.entries.put(packedCell, node);
        layer1_0x10000[packedCell] = (byte) (layer1_0x10000[packedCell] | 0x20);
    }

    /**
     * Native: CWorldMap::refreshNodeLayers @00550A53 and CWorldMap::refreshNodeLayersFromNode @00550A96.
     * Fully ported.
     */
    private void refreshNodeLayers(int packedCell) {
        WorldMapNode node = lookupNode(packedCell);
        if (node == null) {
            return;
        }
        int key = node.getKey();
        int previousLayer1 = layer1_0x10000[key] & 0xFF;
        layer1_0x10000[key] = (byte) node.getLayer1Cell();
        layer0_0x00000[key] = (byte) node.getLayer0Cell();
        layer1_0x10000[key] = (byte) (layer1_0x10000[key] | 0x20);
        layer2_0x20000[key] = layer1_0x10000[key];
        if (node.primaryLayerStateUnit != null) {
            applyPrimaryUnitNodeLayerState(key);
        }
        if (node.secondaryLayerStateUnit != null) {
            applySecondaryUnitNodeLayerState(key);
        }
        if (node.building != null) {
            applyBuildingNodeLayerState(key, node.building);
        }
        applyAreaEffectNodeLayerState(node, key);
        if ((previousLayer1 & 0x10) != 0) {
            layer1_0x10000[key] = (byte) (layer1_0x10000[key] | 0x10);
            layer2_0x20000[key] = (byte) (layer2_0x20000[key] | 0x10);
        }
    }

    /**
     * Native: CWorldMap::removeDynamicNodeForCell @00550966.
     * Fully ported.
     */
    private boolean removeDynamicNodeForCell(int packedCell) {
        packedCell &= 0xFFFF;
        WorldMapNode node = lookupNode(packedCell);
        if (node == null) {
            return false;
        }
        int previousLayer1 = layer1_0x10000[packedCell] & 0xFF;
        layer0_0x00000[packedCell] = (byte) node.getLayer0Cell();
        layer1_0x10000[packedCell] = (byte) node.getLayer1Cell();
        nodeMap.entries.remove(packedCell);
        if ((previousLayer1 & 0x10) != 0) {
            layer1_0x10000[packedCell] = (byte) (layer1_0x10000[packedCell] | 0x10);
            layer2_0x20000[packedCell] = (byte) (layer2_0x20000[packedCell] | 0x10);
        }
        return true;
    }

    /**
     * Native: CWorldMap::applyPrimaryUnitNodeLayerState @00550DE0.
     * Fully ported.
     */
    private void applyPrimaryUnitNodeLayerState(int packedCell) {
        layer2_0x20000[packedCell] = (byte) (layer2_0x20000[packedCell] | 0x40);
    }

    /**
     * Native: CWorldMap::applySecondaryUnitNodeLayerState @00550E12.
     * Fully ported.
     */
    private void applySecondaryUnitNodeLayerState(int packedCell) {
        layer2_0x20000[packedCell] = (byte) (layer2_0x20000[packedCell] | 0x80);
    }

    /**
     * Native support extracted from CWorldMap::refreshNodeLayersFromNode @00550A96 area-effect branches.
     */
    private void applyAreaEffectNodeLayerState(WorldMapNode node, int packedCell) {
        for (Object areaEffect : node.effectLayer) {
            if (areaEffect != null) {
                shiftLayer0ForAreaEffect(packedCell);
            }
        }
        if (node.effectLayer[3] != null) {
            layer1_0x10000[packedCell] = (byte) (layer1_0x10000[packedCell] | 5);
            layer2_0x20000[packedCell] = (byte) (layer2_0x20000[packedCell] | 5);
        }
    }

    /**
     * Native support extracted from CWorldMap::refreshNodeLayersFromNode @00550A96 and FUN_0055F350 @0055F350.
     */
    private void shiftLayer0ForAreaEffect(int packedCell) {
        layer0_0x00000[packedCell] = (byte) (layer0_0x00000[packedCell] << 2);
    }

    /**
     * Native: CWorldMap::attachBuildingCell @00559890.
     * Fully ported.
     */
    private boolean attachBuildingCell(Building building, int packedCell) {
        packedCell &= 0xFFFF;
        WorldMapNode node = lookupNode(packedCell);
        boolean created = false;
        if (node == null) {
            createDynamicNodeForCell(packedCell);
            node = lookupNode(packedCell);
            if (node == null) {
                return false;
            }
            created = true;
        }

        if (node.building != null) {
            return false;
        }
        node.building = building;
        nodeMap.entries.put(packedCell, node);
        buildingByCell.put(packedCell, building);
        if (created) {
            refreshNodeLayers(packedCell);
        }
        return true;
    }

    /**
     * Native: CWorldMap::detachBuildingCell @0055995A.
     * Fully ported.
     */
    private boolean detachBuildingCell(Building building, int packedCell) {
        packedCell &= 0xFFFF;
        WorldMapNode node = lookupNode(packedCell);
        if (node == null) {
            return false;
        }

        node.building = null;
        nodeMap.entries.put(packedCell, node);
        buildingByCell.remove(packedCell);
        refreshNodeLayers(packedCell);
        if (node.isEmpty()) {
            removeDynamicNodeForCell(packedCell);
        }
        return true;
    }

    /**
     * Native support extracted from CWorldMap::refreshNodeLayersFromNode @00550A96.
     */
    private void applyBuildingNodeLayerState(int packedCell, Building building) {
        int localX = (packedCell & 0xFF) - building.m_pTargetHandle.getX();
        int localY = ((packedCell >>> 8) & 0xFF) - building.m_pTargetHandle.getY();
        int bit = (localY * (building.sizeX & 0xFF) + localX) & 0x1F;
        if ((building.passabilityMask & (1 << bit)) == 0) {
            layer1_0x10000[packedCell] = (byte) (layer1_0x10000[packedCell] & 0xFA);
            layer2_0x20000[packedCell] = (byte) (layer2_0x20000[packedCell] & 0xFA);
            layer0_0x00000[packedCell] = costCracked0x5414B;
        } else {
            layer1_0x10000[packedCell] = (byte) (layer1_0x10000[packedCell] | 5);
            layer2_0x20000[packedCell] = (byte) (layer2_0x20000[packedCell] | 5);
        }
    }

    /**
     * Native: CWorldMap::markSpecialMissionExitAreaBlocked @00558AAB.
     * Fully ported.
     */
    public void markSpecialMissionExitAreaBlocked() {
        for (int x = 0x3E; x < 0x42; x++) {
            for (int y = 0x18; y < 0x1B; y++) {
                int packedCell = packCell(x, y);
                layer1_0x10000[packedCell] = (byte) ((layer1_0x10000[packedCell] & 0xFF) + 5);
                layer2_0x20000[packedCell] = (byte) (layer2_0x20000[packedCell] | 5);
            }
        }
    }

    /**
     * Fully ported. Native: CWorldMap::ToIdx @0055F9C0.
     * Native support also extracted from CWorldMap native cell packing callers @00554626, @00553B30, @005599DF,
     * and @00559AAF.
     */
    private static int packCell(int x, int y) {
        return ((y & 0xFF) << 8) | (x & 0xFF);
    }
}
