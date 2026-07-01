package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.unit.UnitDirtyFlags;
import ua.millfreedom.rom2.model.unit.humanoid.Humanoid;
import ua.millfreedom.rom2.model.unit.humanoid.human.Human;

import java.io.IOException;
import java.util.Iterator;

public class Outpost extends Building {
    //0x6C
    public final UnitList outpostUnits = new UnitList();
    //0x8C
    public final UnitList clonedOutpostUnits = new UnitList();
    //0xAC
    public int groupKey;
    //0xB0
    public int respawnDelayTicks;
    //0xB4
    public int respawnTimerTicks;
    //0xB8
    public int postReleaseActionId;
    //0xBC
    public int spawnSpread;
    //0xC0
    public int releaseGatePassed;
    //0xC4
    public int groupFlag0;
    //0xC8
    public int groupFlag1;

    /**
     * Native: Outpost::Outpost @00520517.
     * Fully ported.
     */
    public Outpost() {
        initializeOutpostDefaults();
    }

    /**
     * Native: Outpost::Outpost @0052058C.
     * Fully ported.
     */
    public Outpost(TargetHandle targetHandle) {
        super(targetHandle);
        initializeOutpostDefaults();
    }

    /**
     * Native: Outpost::initializeOutpostDefaults @00520607.
     * Fully ported.
     */
    private void initializeOutpostDefaults() {
        groupKey = 0;
        postReleaseActionId = -1;
        buildingPresentMask = 0;
        spawnSpread = 0;
        respawnDelayTicks = 0x78;
        releaseGatePassed = 0;
        groupFlag0 = 0;
        groupFlag1 = 0;
    }

    /**
     * Native: Outpost::setOutpostUnitsFromList @005206FA.
     * Fully ported.
     */
    public void setOutpostUnitsFromList(CustomList<Unit> units) {
        outpostUnits.clear();
        clonedOutpostUnits.clear();

        for (Unit unit : units) {
            outpostUnits.add(unit);
            Unit clone;
            if (unit.isHumanoidToken() == 0) {
                clone = new Unit();
                clone.copyFrom(unit);
            } else {
                clone = new Human().initializeDefaultTemplate();
                ((Humanoid) clone).copyHumanoidCloneState((Humanoid) unit);
            }
            clonedOutpostUnits.add(clone);
        }
    }

    /**
     * vtbl +0x08: Outpost::serialize @0052D641.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        super.serialize(ar);
    }

    /**
     * vtbl +0x14: Outpost::updateRegen @00520853.
     * Fully ported.
     */
    @Override
    public void updateRegen() {
        super.updateRegen();

        if (!isReleaseGateSatisfied()) {
            return;
        }

        int timerBeforeTick = respawnTimerTicks;
        respawnTimerTicks -= 1;
        if (timerBeforeTick > 0) {
            return;
        }

        for (Unit unit : outpostUnits) {
            if (unit.respawning < 5) {
                healthCurrent = (short) 0xFDA7;
                unit.updateCorpseDecay();
            }
        }

        UnitGroup ownerGroup = findOrCreateOwnerGroup();
        if (!ownerGroup.units.isEmpty()) {
            return;
        }

        releaseUnits(ownerGroup);
    }

    /**
     * Fully ported. vtbl +0x38: Outpost::isOutpostBuilding @00543620.
     */
    @Override
    public int isOutpostBuilding() {
        return 1;
    }

    /**
     * Native support extracted from Outpost::updateRegen @00520853 gate check block.
     * Fully ported.
     */
    private boolean isReleaseGateSatisfied() {
        if (releaseGatePassed != 0) {
            return true;
        }
        for (Unit unit : outpostUnits) {
            if (unit.respawning < 3) {
                return false;
            }
        }
        releaseGatePassed = 1;
        return true;
    }

    /**
     * Native support extracted from Outpost::updateRegen @00520853 owner-group lookup block.
     * Fully ported.
     */
    private UnitGroup findOrCreateOwnerGroup() {
        Player player = owner;
        for (UnitGroup group : player.unitGroups) {
            if (group.groupKey == groupKey) {
                return group;
            }
        }

        UnitGroup created = new UnitGroup();
        created.groupKey = groupKey;
        player.unitGroups.add(created);
        return created;
    }

    /**
     * Native: Outpost::releaseUnits @00520A5E.
     * Fully ported.
     */
    private void releaseUnits(UnitGroup ownerGroup) {
        Iterator<Unit> unitIt = outpostUnits.iterator();
        Iterator<Unit> cloneIt = clonedOutpostUnits.iterator();

        while (unitIt.hasNext()) {
            Unit unit = unitIt.next();
            Unit clone = cloneIt.next();

            Globals.gameServer.objectLists.corpses.remove(unit);
            if (clone.isHumanoidToken() == 0) {
                unit.copyFrom(clone);
            } else {
                ((Humanoid) unit).copyHumanoidCloneState((Humanoid) clone);
            }
            unit.word = 0;

            if (tryPlaceReleasedUnit(unit)) {
                releaseGatePassed = 0;
                respawnTimerTicks = respawnDelayTicks;

                Globals.gameServer.activeUnits.addAndAssignScenarioId(unit);
                ownerGroup.addUnit(unit);
                Player player = owner;
                player.ownedUnits.add(unit);
                CServerApp.netUpdate(unit, UnitDirtyFlags.WORLD_ENTRY_BASELINE.value);
            }
        }

        ownerGroup.innGroupRelocationQuestFlag = groupFlag0;
        ownerGroup.hostileGroupRelocationQuestFlag = groupFlag1;
        notifyMissionScriptRuntime(ownerGroup);
    }

    /**
     * Native support extracted from Outpost::releaseUnits @00520A5E placement call.
     * Fully ported.
     */
    private boolean tryPlaceReleasedUnit(Unit unit) {
        int centerX;
        int centerY;
        int diameter;
        if (spawnSpread == 0) {
            centerX = unit.m_pTargetHandle.x & 0xFF;
            centerY = unit.m_pTargetHandle.y & 0xFF;
            diameter = 0;
        } else {
            centerX = m_pTargetHandle.x & 0xFF;
            centerY = m_pTargetHandle.y & 0xFF;
            diameter = spawnSpread & 0xFF;
        }
        return unit.placeNearMissionCell(centerX, centerY, diameter);
    }

    /**
     * Native support extracted from Outpost::releaseUnits @00520A5E tail callbacks.
     * Fully ported.
     */
    private void notifyMissionScriptRuntime(UnitGroup ownerGroup) {
        Globals.gameServer.missionScriptRuntime.initializeLoadedScenarioGroup(ownerGroup, 0);
        if (postReleaseActionId >= 0) {
            Globals.gameServer.missionScriptRuntime.executeScriptInstant(postReleaseActionId);
        }
    }

}
