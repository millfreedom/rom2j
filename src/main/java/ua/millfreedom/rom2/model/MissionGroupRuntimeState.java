package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.model.container.CustomList;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MissionGroupRuntimeState implements MfcSerializable {
    private static final int STRUCT_SIZE = 0x50;

    //0x00
    public int scenarioGroupCell;
    //0x02
    public byte[] reserved0x02 = new byte[8];
    //0x0A
    public int scriptTargetCell;
    //0x0C
    public byte[] reserved0x0C = new byte[9];
    //0x15
    public int scriptTargetCellAge;
    //0x16
    public byte[] reserved0x16 = new byte[10];
    //0x20
    public int scriptRuntimeState;
    //0x21
    public byte[] reserved0x21 = new byte[3];
    //0x24
    public int scenarioGroupCenterSubpos;
    //0x28
    public int scenarioGroupCenterCell;
    //0x2A
    public int scenarioGroupMaxMemberDistance;
    //0x2B
    public int scenarioGroupMaxSightRange;
    //0x2C
    public int scenarioGroupMaxDistancePlusSight;
    //0x2D
    public int scenarioGroupGuardRange;
    //0x2E
    public byte[] reserved0x2E = new byte[2];
    //0x30
    public int priorHeadUnitPresence;
    //0x34
    public int presenceScanCounter;
    //0x38
    public int scenarioGroupGuardRangeMirror;
    //0x39
    public int scenarioGroupGuardRefreshFlag;
    //0x3A
    public byte[] reserved0x3A = new byte[10];
    //0x44
    public int missionScriptSpeedOverride;
    //0x45
    public int damagedUnitMissionUpdatePending;
    //0x46
    public byte[] reserved0x46 = new byte[2];
    //0x48
    public int scenarioScriptReferencedFlag;
    //0x4C
    public int serializedWaypointCellsPointer;
    //0x4C native pointer target
    public CustomList<Short> waypointCells = CustomList.std(Short.class);

    /**
     * Native: MissionGroupRuntimeState::New @005690EE.
     * Fully ported.
     */
    public MissionGroupRuntimeState() {
        scriptRuntimeState = 0;
        damagedUnitMissionUpdatePending = 1;
    }

    /**
     * Native: MissionGroupRuntimeState::Serialize @00574560.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        if (ar.isStoring()) {
            ar.writeBytes(toFixedBytes());
            ar.serialize(waypointCells);
        } else {
            fromFixedBytes(ar.readBytes(STRUCT_SIZE));
            waypointCells = CustomList.std(Short.class);
            ar.serialize(waypointCells);
        }
    }

    /**
     * Native support extracted from MissionGroupRuntimeState::Serialize @00574560.
     */
    private void fromFixedBytes(byte[] fixedBytes) {
        ByteBuffer buffer = ByteBuffer.wrap(fixedBytes).order(ByteOrder.LITTLE_ENDIAN);
        scenarioGroupCell = Short.toUnsignedInt(buffer.getShort(0x00));
        System.arraycopy(fixedBytes, 0x02, reserved0x02, 0, reserved0x02.length);
        scriptTargetCell = Short.toUnsignedInt(buffer.getShort(0x0A));
        System.arraycopy(fixedBytes, 0x0C, reserved0x0C, 0, reserved0x0C.length);
        scriptTargetCellAge = Byte.toUnsignedInt(fixedBytes[0x15]);
        System.arraycopy(fixedBytes, 0x16, reserved0x16, 0, reserved0x16.length);
        scriptRuntimeState = fixedBytes[0x20];
        System.arraycopy(fixedBytes, 0x21, reserved0x21, 0, reserved0x21.length);
        scenarioGroupCenterSubpos = buffer.getInt(0x24);
        scenarioGroupCenterCell = Short.toUnsignedInt(buffer.getShort(0x28));
        scenarioGroupMaxMemberDistance = Byte.toUnsignedInt(fixedBytes[0x2A]);
        scenarioGroupMaxSightRange = Byte.toUnsignedInt(fixedBytes[0x2B]);
        scenarioGroupMaxDistancePlusSight = Byte.toUnsignedInt(fixedBytes[0x2C]);
        scenarioGroupGuardRange = Byte.toUnsignedInt(fixedBytes[0x2D]);
        System.arraycopy(fixedBytes, 0x2E, reserved0x2E, 0, reserved0x2E.length);
        priorHeadUnitPresence = buffer.getInt(0x30);
        presenceScanCounter = buffer.getInt(0x34);
        scenarioGroupGuardRangeMirror = Byte.toUnsignedInt(fixedBytes[0x38]);
        scenarioGroupGuardRefreshFlag = Byte.toUnsignedInt(fixedBytes[0x39]);
        System.arraycopy(fixedBytes, 0x3A, reserved0x3A, 0, reserved0x3A.length);
        missionScriptSpeedOverride = Byte.toUnsignedInt(fixedBytes[0x44]);
        damagedUnitMissionUpdatePending = Byte.toUnsignedInt(fixedBytes[0x45]);
        System.arraycopy(fixedBytes, 0x46, reserved0x46, 0, reserved0x46.length);
        scenarioScriptReferencedFlag = buffer.getInt(0x48);
        serializedWaypointCellsPointer = buffer.getInt(0x4C);
    }

    /**
     * Native support extracted from MissionGroupRuntimeState::Serialize @00574560.
     */
    private byte[] toFixedBytes() {
        byte[] fixedBytes = new byte[STRUCT_SIZE];
        ByteBuffer buffer = ByteBuffer.wrap(fixedBytes).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort(0x00, (short) scenarioGroupCell);
        System.arraycopy(reserved0x02, 0, fixedBytes, 0x02, reserved0x02.length);
        buffer.putShort(0x0A, (short) scriptTargetCell);
        System.arraycopy(reserved0x0C, 0, fixedBytes, 0x0C, reserved0x0C.length);
        fixedBytes[0x15] = (byte) scriptTargetCellAge;
        System.arraycopy(reserved0x16, 0, fixedBytes, 0x16, reserved0x16.length);
        fixedBytes[0x20] = (byte) scriptRuntimeState;
        System.arraycopy(reserved0x21, 0, fixedBytes, 0x21, reserved0x21.length);
        buffer.putInt(0x24, scenarioGroupCenterSubpos);
        buffer.putShort(0x28, (short) scenarioGroupCenterCell);
        fixedBytes[0x2A] = (byte) scenarioGroupMaxMemberDistance;
        fixedBytes[0x2B] = (byte) scenarioGroupMaxSightRange;
        fixedBytes[0x2C] = (byte) scenarioGroupMaxDistancePlusSight;
        fixedBytes[0x2D] = (byte) scenarioGroupGuardRange;
        System.arraycopy(reserved0x2E, 0, fixedBytes, 0x2E, reserved0x2E.length);
        buffer.putInt(0x30, priorHeadUnitPresence);
        buffer.putInt(0x34, presenceScanCounter);
        fixedBytes[0x38] = (byte) scenarioGroupGuardRangeMirror;
        fixedBytes[0x39] = (byte) scenarioGroupGuardRefreshFlag;
        System.arraycopy(reserved0x3A, 0, fixedBytes, 0x3A, reserved0x3A.length);
        fixedBytes[0x44] = (byte) missionScriptSpeedOverride;
        fixedBytes[0x45] = (byte) damagedUnitMissionUpdatePending;
        System.arraycopy(reserved0x46, 0, fixedBytes, 0x46, reserved0x46.length);
        buffer.putInt(0x48, scenarioScriptReferencedFlag);
        buffer.putInt(0x4C, serializedWaypointCellsPointer);
        return fixedBytes;
    }

    /**
     * Native support extracted from WorldMapUnitVisibilityState::MarkUnitVisibilityFootprint @0055ACA2 and
     * WorldMapUnitVisibilityState::applyActiveUnitVisibilityState @0055B406.
     */
    public void markDamagedUnitMissionUpdatePending() {
        damagedUnitMissionUpdatePending = 1;
    }

    /**
     * Native support extracted from WorldMapUnitVisibilityState::ResetVisibilityState @0055B879.
     */
    public void clearDamagedUnitMissionUpdatePending() {
        damagedUnitMissionUpdatePending = 0;
    }

    /**
     * Native support extracted from MissionScriptRuntime::processPlayerMissionGroups @00570087.
     */
    public boolean isDamagedUnitMissionUpdatePending() {
        return damagedUnitMissionUpdatePending != 0;
    }

    /**
     * Native support extracted from ScenarioMapLoader::materializeScenarioScriptRuntime @00562745.
     */
    public void markScenarioScriptReferenced() {
        scenarioScriptReferencedFlag = 1;
    }

    /**
     * Native support extracted from MissionScriptRuntime::executeClearScenarioScriptReferencedFlagInstant @005781A7.
     */
    public void clearScenarioScriptReferenced() {
        scenarioScriptReferencedFlag = 0;
    }

    /**
     * Native support extracted from MissionScriptRuntime::processMissionGroupState @005700F3.
     */
    public int getScriptTargetCell() {
        return scriptTargetCell;
    }

    /**
     * Native support extracted from MissionScriptRuntime::setGroupMissionMoveTarget @005783C9.
     */
    public void setScriptTargetCell(int cell) {
        scriptTargetCell = cell & 0xFFFF;
    }

    /**
     * Native support extracted from MissionScriptRuntime::processGuardGroupState @0057153B.
     */
    public int getScenarioGroupCenterCell() {
        return scenarioGroupCenterCell;
    }

    /**
     * Native support extracted from MissionScriptRuntime::updateWanderTargetCell @00570942.
     */
    public int getScriptTargetCellAge() {
        return scriptTargetCellAge;
    }

    /**
     * Native support extracted from MissionScriptRuntime::updateWanderTargetCell @00570942.
     */
    public void clearScriptTargetCellAge() {
        scriptTargetCellAge = 0;
    }

    /**
     * Native support extracted from MissionScriptRuntime::updateWanderTargetCell @00570942.
     */
    public void incrementScriptTargetCellAge() {
        scriptTargetCellAge = (scriptTargetCellAge + 1) & 0xFF;
    }

    /**
     * Native support extracted from MissionScriptRuntime::initializeScenarioGroupMovement @00577FDE.
     */
    public void setScenarioGroupCenterAndRanges(int centerSubpos, int centerCell, int maxMemberDistance,
                                                int maxSightRange, int maxDistancePlusSight, int guardRange) {
        scenarioGroupCenterSubpos = centerSubpos;
        scenarioGroupCenterCell = centerCell & 0xFFFF;
        scenarioGroupMaxMemberDistance = maxMemberDistance & 0xFF;
        scenarioGroupMaxSightRange = maxSightRange & 0xFF;
        scenarioGroupMaxDistancePlusSight = maxDistancePlusSight & 0xFF;
        scenarioGroupGuardRange = guardRange & 0xFF;
        scenarioGroupGuardRangeMirror = guardRange & 0xFF;
    }

    /**
     * Native support extracted from MissionScriptRuntime::RefreshScenarioGroupCenterAndFootprintRange @0056F850.
     */
    public void setScenarioGroupCenterAndRanges(int centerSubpos, int centerCell, int maxMemberDistance,
                                                int maxSightRange, int maxDistancePlusSight) {
        scenarioGroupCenterSubpos = centerSubpos;
        scenarioGroupCenterCell = centerCell & 0xFFFF;
        scenarioGroupMaxMemberDistance = maxMemberDistance & 0xFF;
        scenarioGroupMaxSightRange = maxSightRange & 0xFF;
        scenarioGroupMaxDistancePlusSight = maxDistancePlusSight & 0xFF;
    }

    /**
     * Native support extracted from MissionScriptRuntime::RefreshScenarioGroupCenterAndGuardRange @0057026E.
     */
    public void copyScenarioGroupCenterAndGuardRangeFromFootprint() {
        scenarioGroupCell = scenarioGroupCenterCell;
        scenarioGroupGuardRange = scenarioGroupMaxDistancePlusSight;
        scenarioGroupGuardRangeMirror = scenarioGroupMaxDistancePlusSight;
    }

    /**
     * Native support extracted from MissionScriptRuntime::copyScenarioGroupCenterAndGuardRangeFromFootprint @00577E7C.
     */
    public void copyScenarioGroupCenterAndGuardRangeFromFootprint(int centerSubpos, int centerCell, int guardRange) {
        scenarioGroupCenterSubpos = centerSubpos;
        scenarioGroupCenterCell = centerCell & 0xFFFF;
        scenarioGroupGuardRange = guardRange & 0xFF;
        scenarioGroupGuardRangeMirror = guardRange & 0xFF;
    }

    /**
     * Native support extracted from MissionScriptRuntime::processGuardGroupState @0057153B.
     */
    public int getScenarioGroupGuardRange() {
        return scenarioGroupGuardRange;
    }

    /**
     * Native support extracted from MissionScriptRuntime::processGuardGroupState @0057153B.
     */
    public int getScenarioGroupGuardRangeMirror() {
        return scenarioGroupGuardRangeMirror;
    }

    /**
     * Native support extracted from MissionScriptRuntime::setGroupMissionAttackTarget @00577B7E.
     */
    public void setScenarioGroupGuardRange(int value) {
        scenarioGroupGuardRange = value & 0xFF;
        scenarioGroupGuardRangeMirror = value & 0xFF;
    }

    /**
     * Native support extracted from MissionScriptRuntime::processGuardGroupState @0057153B.
     */
    public void setScenarioGroupCurrentGuardRange(int value) {
        scenarioGroupGuardRange = value & 0xFF;
    }

    /**
     * Native support extracted from MissionScriptRuntime::updateMissionGroupPresenceState @00570448.
     */
    public boolean hasPriorHeadUnitPresence() {
        return priorHeadUnitPresence != 0;
    }

    /**
     * Native support extracted from MissionScriptRuntime::updateMissionGroupPresenceState @00570448.
     */
    public void setPriorHeadUnitPresence(boolean present) {
        priorHeadUnitPresence = present ? 1 : 0;
    }

    /**
     * Native support extracted from MissionScriptRuntime::setGroupMissionAttackTarget @00577B7E.
     */
    public void clearScenarioGroupGuardRefreshFlag() {
        scenarioGroupGuardRefreshFlag = 0;
    }

    /**
     * Native support extracted from MissionScriptRuntime::updateEmptyGroupScriptState @0057375A.
     * Fully ported support for the empty-group script disable write.
     */
    public void disableEmptyGroupScriptState(int groupSize) {
        if (groupSize == 0) {
            scriptRuntimeState = -1;
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::updateMissionGroupPresenceState @00570448.
     */
    public void incrementPresenceScanCounter() {
        presenceScanCounter++;
    }

    /**
     * Native support extracted from MissionScriptRuntime::updateMissionGroupPresenceState @00570448.
     */
    public void clearPresenceScanCounter() {
        presenceScanCounter = 0;
    }

    /**
     * Native support extracted from CWorldMap::getUnitSpeed @00564701.
     */
    public int getMissionScriptSpeedOverride() {
        return missionScriptSpeedOverride;
    }

    /**
     * Native support extracted from MissionScriptRuntime::setGroupMissionMoveTarget @005783C9.
     */
    public void setMissionScriptSpeedOverride(int speed) {
        missionScriptSpeedOverride = speed & 0xFF;
    }

    /**
     * Native support extracted from MissionScriptRuntime::processCommandCellMoveGroup @00573198.
     */
    public boolean hasMissionScriptSpeedOverride() {
        return missionScriptSpeedOverride != 0;
    }
}
