package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.enums.BuildingId;
import ua.millfreedom.rom2.model.unit.Unit;

import java.io.IOException;

import static ua.millfreedom.rom2.model.column.BuildingColumn.*;

public class Building extends Token {
    //0x3C
    public BuildingInfo pBuildingInfo;
    //0x40
    public int id;
    //0x42
    public int healthCurrent;
    //0x44
    public int healthMax;
    //0x46
    public int f0x46;
    //0x48
    public int scanRange;
    //0x4A
    public final StatData mStatData = new StatData();
    //0x60; Building::getSizeX @0055F950 is represented by direct field access.
    public int sizeX;
    //0x61
    public int sizeY;
    //0x64
    public int passabilityMask;
    //0x68
    public int buildingPresentMask;

    /**
     * Native: Building::Building @0051FC1D.
     * Fully ported.
     */
    public Building() {
        initializeByName("");
    }

    /**
     * Native: Building::Building @0051FC9F.
     * Fully ported.
     */
    protected Building(TargetHandle targetHandle) {
        m_pTargetHandle.assignFrom(targetHandle);
        initializeByName("");
    }

    /**
     * Native: Building::Building @0051FD27.
     * Fully ported.
     */
    public Building(String name) {
        initializeByName(name);
    }

    /**
     * Native: Building::Building @0051FD8C.
     * Fully ported.
     */
    public Building(int buildingId, TargetHandle targetHandle, int widthTiles, int heightTiles) {
        m_pTargetHandle.assignFrom(targetHandle);
        id = buildingId & 0xFF;
        pBuildingInfo = null;
        typeID = 0;
        initializeFromBuildingInfo(widthTiles, heightTiles);
    }

    /**
     * Native: Building::initializeByName @0051FE15.
     * Fully ported.
     */
    protected final void initializeByName(String name) {
        id = 0;
        pBuildingInfo = null;
        typeID = 0;
        scanRange = 0;
        healthMax = 0x32;
        healthCurrent = 0x32;

        if (name.length() < 2) {
            return;
        }

        id = findBuildingIdByName(name);
        if (isA(BuildingId.EMPTY)) {
            Globals.gameServer.pushMessage("Invalid building " + name + " created.");
            return;
        }

        initializeFromBuildingInfo(0, 0);
    }

    /**
     * Native: Building::initializeFromBuildingInfo @0051FF33.
     * Fully ported.
     */
    protected final void initializeFromBuildingInfo(int widthTiles, int heightTiles) {
        if (isA(BuildingId.EMPTY) || id >= Globals.staticDataMgr.buildings.size()) {
            return;
        }

        typeID = id & 0xFFFF;
        pBuildingInfo = Globals.staticDataMgr.buildings.get(id);

        scanRange = pBuildingInfo.getAttribute(SCAN_RANGE) & 0xFF;
        healthMax = pBuildingInfo.getAttribute(HEALTH_MAX) & 0xFFFF;
        healthCurrent = healthMax;

        if (((widthTiles & 0xFF) + (heightTiles & 0xFF)) > 0) {
            sizeX = widthTiles & 0xFF;
            sizeY = heightTiles & 0xFF;
            passabilityMask = -1;
            buildingPresentMask = -1;
            passabilityMask = 0;
        } else {
            sizeX = pBuildingInfo.getAttribute(SIZE_X) & 0xFF;
            sizeY = pBuildingInfo.getAttribute(SIZE_Y) & 0xFF;
            passabilityMask = pBuildingInfo.getAttribute(PASSABILITY);
            buildingPresentMask = pBuildingInfo.getAttribute(BUILDING_PRESENT);
        }

        Globals.worldMap.attachBuildingFootprint(this);
    }

    /**
     * vtbl +0x08: Building::serialize @0052029C.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        super.serialize(ar);
        ar.serialize(mStatData);

        if (!ar.isStoring()) {
            id = ar.readByte() & 0xFF;
            healthCurrent = ar.readUShort();
            healthMax = ar.readUShort();
            f0x46 = ar.readUShort();
            scanRange = ar.readByte() & 0xFF;
            sizeX = ar.readByte() & 0xFF;
            sizeY = ar.readByte() & 0xFF;
            passabilityMask = ar.readInt();
            buildingPresentMask = ar.readInt();

            if (isA(BuildingId.EMPTY)) {
                pBuildingInfo = null;
            } else {
                pBuildingInfo = Globals.staticDataMgr.buildings.get(id);
            }
        } else {
            ar.writeByte(id);
            ar.writeShort(healthCurrent);
            ar.writeShort(healthMax);
            ar.writeShort(f0x46);
            ar.writeByte(scanRange);
            ar.writeByte(sizeX);
            ar.writeByte(sizeY);
            ar.writeInt(passabilityMask);
            ar.writeInt(buildingPresentMask);
        }
    }

    /**
     * vtbl +0x14: Building::updateRegen @00520137.
     * Fully ported.
     */
    @Override
    public void updateRegen() {
        if (Globals.gameServer.someValue % 0x3C == 0
                && (isA(BuildingId.MAGIC_WELL_2) || isA(BuildingId.MAGIC_WELL_3))
                && (short) healthCurrent < (short) healthMax
                && (short) healthCurrent >= 0) {
            healthCurrent = (short) (healthCurrent + 1);
            CServerApp.notifyStateChanged(this);
        }
    }

    /**
     * Native support extracted from Unit::update @0050F12C Magic Well reward branch.
     */
    public boolean hasMagicWellRewardCharge(BuildingId rewardBuildingId) {
        return isA(rewardBuildingId) && (short) healthCurrent > 0;
    }

    /**
     * Native support extracted from Unit::update @0050F12C Magic Well reward branch.
     */
    public void consumeMagicWellRewardCharge() {
        healthCurrent -= 1;
        CServerApp.notifyStateChanged(this);
    }

    /**
     * Fully ported. vtbl +0x34: Building::isBuildingToken @00543390.
     * Enables the non-unit building-health token path in CServerApp::notifyStateChanged @00503672.
     */
    @Override
    public int isBuildingToken() {
        return 1;
    }

    /**
     * Fully ported. vtbl +0x38: Building::isOutpostBuilding @005433A0.
     * Base building marker returns 0 for non-outpost building subclasses.
     */
    public int isOutpostBuilding() {
        return 0;
    }

    /**
     * Native: Building::calculateIncomingDamage @005201B2.
     * Fully ported.
     */
    public int calculateIncomingDamage(SkillData attackData, Unit attacker) {
        if (attackData == null || healthMax == 0) {
            return 0;
        }

        int damage = 0;
        int spread = attackData.skillDamageType2Modifier & 0xFF;
        if (spread != 0) {
            damage += (attackData.skillDamageType2Min & 0xFF) + Utils.randInclusive(spread);
        }
        damage -= 5;
        if (damage < 1) {
            return 0;
        }

        if ((short) healthCurrent <= damage) {
            removeVirtualCastersAtTargetCell();
        }
        return damage;
    }

    /**
     * Native support extracted from Building::calculateIncomingDamage @005201B2.
     * Fully ported.
     */
    private void removeVirtualCastersAtTargetCell() {
        Globals.gameServer.objectLists.virtualCasters.removeIf(virtualCaster ->
                m_pTargetHandle.isSameCell(virtualCaster.m_pTargetHandle)
        );
    }

    // not ported.
    public boolean isA(BuildingId buildingId) {
        return buildingId != null && id == buildingId.id;
    }

    /**
     * Native support extracted from CArray<BuildingInfo>::FindByName @00543430.
     * Fully ported.
     */
    private int findBuildingIdByName(String name) {
        for (int i = Globals.staticDataMgr.buildings.size() - 1; i > 0; i--) {
            BuildingInfo info = Globals.staticDataMgr.buildings.get(i);
            if (name.equals(info.name)) {
                return i;
            }
        }
        return 0;
    }

}
