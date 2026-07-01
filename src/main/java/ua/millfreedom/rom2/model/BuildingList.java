package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.container.CustomList;

import java.io.IOException;
import java.util.Iterator;

public class BuildingList implements MfcSerializable, Iterable<Building> {
    private static final int SCENARIO_OBJECT_ID_BASE = 0x6000;

    //0x0
    public final CustomList<Building> buildings = new CustomList<>(Building.class);

    /**
     * Native: BuildingList::New @0052BF45.
     * Fully ported.
     */
    public BuildingList() {
    }

    /**
     * Native: BuildingList::Serialize @005462B0; forwarding wrapper @0052C3DA.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        ar.serialize(buildings);
    }

    /**
     * Native: BuildingList::GetCount @0044FC40.
     * Fully ported.
     */
    public int size() {
        return buildings.size();
    }

    /**
     * Native support extracted from BuildingList::GetFirst @0053CAA0 and BuildingList::ClearOwnedBuildings @0053CA60.
     */
    public boolean isEmpty() {
        return buildings.isEmpty();
    }

    /**
     * Native support extracted from BuildingList::Serialize @005462B0 read-side AddTail loop.
     */
    public boolean add(Building building) {
        return buildings.add(building);
    }

    /**
     * Fully ported. Native: BuildingList::addAndAssignScenarioId @0052C1BE.
     */
    public void addAndAssignScenarioId(Building building) {
        add(building);
        building.idFull = building.scenarioObjectId + SCENARIO_OBJECT_ID_BASE;
    }

    /**
     * Native support extracted from BuildingList::FindByTokenId @0052BFA3 and BuildingList::FindOwnedInn @0052C0C1.
     */
    @Override
    public Iterator<Building> iterator() {
        return buildings.iterator();
    }

    /**
     * Native: BuildingList::GetFirst @0053CAA0.
     * Fully ported.
     */
    public Building firstOrNull() {
        if (buildings.isEmpty()) {
            return null;
        }
        return buildings.get(0);
    }

    /**
     * Native: BuildingList::findByTokenId @0052BFA3.
     * Fully ported.
     */
    public Building findByTokenId(int tokenId) {
        int nativeTokenId = (short) tokenId;
        for (Building building : buildings) {
            if (building.idFull == nativeTokenId) {
                return building;
            }
        }
        return null;
    }

    /**
     * Native: BuildingList::findOwnedInn @0052C0C1.
     * Fully ported.
     */
    public Inn findOwnedInn(Player owner) {
        for (Building building : buildings) {
            if (building.id != 0 && building.owner == owner && building instanceof Inn inn) {
                return inn;
            }
        }
        return null;
    }

    /**
     * Native: BuildingList::findNearTarget @0052BFE7.
     * Fully ported.
     */
    public Building findNearTarget(TargetHandle target) {
        for (Building building : buildings) {
            if (building.m_pTargetHandle.chebyshevDistanceByXY(target) < 3) {
                return building;
            }
        }
        return null;
    }

    /**
     * Native: BuildingList::findClosestPresentBuilding @0052C040.
     */
    public Building findClosestPresentBuilding(TargetHandle target) {
        int bestDistance = 0x7FFF;
        Building closest = null;
        for (Building building : buildings) {
            if (building.id != 0) {
                int distance = building.m_pTargetHandle.chebyshevDistanceByXY(target);
                if (distance <= bestDistance) {
                    closest = building;
                    bestDistance = distance;
                }
            }
        }
        return closest;
    }

    /**
     * Native: BuildingList::findInteractiveNearTarget @0052C129.
     * Fully ported.
     */
    public Building findInteractiveNearTarget(TargetHandle target) {
        return findNearTarget(offsetTargetByOneCell(target));
    }

    /**
     * Native support extracted from BuildingList::findInteractiveNearTarget @0052C129.
     */
    private static TargetHandle offsetTargetByOneCell(TargetHandle source) {
        TargetHandle shifted = new TargetHandle();
        shifted.initFromBytes(source.getX() - 1, source.getY() - 1, Globals.worldMap);
        return shifted;
    }

    /**
     * Native: BuildingList::updateRegen @0052C17C.
     * Fully ported.
     */
    public void updateRegen() {
        for (Building building : buildings) {
            building.updateRegen();
        }
    }

    /**
     * Native: BuildingList::ClearOwnedBuildings @0053CA60.
     * Fully ported.
     * Native deletes each removed building; Java drops the owned references.
     */
    public void clear() {
        buildings.clear();
    }
}
