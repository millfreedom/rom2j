package ua.millfreedom.rom2.model.unit;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;

import java.io.IOException;
import java.util.Arrays;

/**
 * Native UnitMovementState POD structure.
 * <p>
 * Known native methods:
 * - UnitMovementState::Init @00551346
 * - UnitMovementState::getFacingStep2 @005514CE
 * - UnitMovementState::applyTokenMovementLayerMask @00557A89
 * - UnitMovementState::PositionChanged @00558B3D
 * - UnitMovementState::Serialize @005595A8
 * - UnitMovementState::RestoreContext @00559681
 * <p>
 * Native layout size is 0xB4 bytes. Unrecovered fields are preserved in reserved gap fields.
 */
public class UnitMovementState implements MfcSerializable {
    private static final int MOVEMENT_SEGMENT_CAPACITY = 0x10;
    private static final int DEFAULT_MOVEMENT_LAYER_MASK = 0x41;
    private static final int DEFAULT_MISSION_SCAN_RADIUS = 5;
    private static final int DEFAULT_PATH_SEARCH_REFRESH_COUNT = 0xFF;
    private static final int DEFAULT_ROTATION_SPEED = 0x10;

    //0x00
    public int facing;
    //0x01
    public int facingLast;
    //0x02
    public final byte[] reserved0x02 = new byte[0x02];
    //0x04
    public int pathHeadRetryDelay;
    //0x05
    public int movementLayerMask;
    //0x06
    public int positionCellX;
    //0x07
    public int positionCellY;
    //0x08
    public int missionScanRadius;
    //0x09
    public int pathSearchRefreshCount;
    //0x0A
    public int rotationSpeed;
    //0x0B
    public int movementSegmentCount;
    //0x0C
    public final int[] movementSegmentState = new int[MOVEMENT_SEGMENT_CAPACITY];
    //0x1C
    public final int[] movementSegmentDirection = new int[MOVEMENT_SEGMENT_CAPACITY];
    //0x2C
    public final int[] movementSegmentXdX = new int[MOVEMENT_SEGMENT_CAPACITY];
    //0x4C
    public final int[] movementSegmentYdY = new int[MOVEMENT_SEGMENT_CAPACITY];
    //0x6C
    public int movementRequestPending;
    //0x70
    public int pathCurrentCell;
    //0x72
    public int terrainStepDistance;
    //0x74
    public int pathTargetCell;
    //0x76
    public int missionReentryCell;
    //0x78
    public int pathRefreshTicks;
    //0x79
    public final byte[] reserved0x79 = new byte[0x03];
    //0x7C
    public Object movementTargetUnit;
    //0x80
    public int cell;
    //0x82
    public int attachCellX;
    //0x83
    public int attachCellY;
    //0x84
    public int attachCellXdX;
    //0x85
    public int attachCellYdY;
    //0x86
    public int detachCellX;
    //0x87
    public int detachCellY;
    //0x88
    public int detachCellXdX;
    //0x89
    public int detachCellYdY;
    //0x8A
    public int targetEngagementStaticPathLength;
    //0x8C
    public int targetEngagementCell;
    //0x8E
    public final byte[] reserved0x8E = new byte[0x02];
    //0x90
    public int missionReentryPending;
    //0x94
    public int dynamicPathTargetCell;
    //0x96
    public int dynamicPathSourceCell;
    //0x98
    public int movementEventPending;
    //0x9C
    public int movementStepState;
    //0x9D
    public int rotationTicks;
    //0x9E
    public final byte[] reserved0x9E = new byte[0x02];
    //0xA0
    public int rotationActive;
    //0xA4
    public int positionChangedStep;
    //0xA5
    public final byte[] reserved0xA5 = new byte[0x03];
    //0xA8
    public int subTileStepDistance;
    //0xAA
    public int stepTickLimit;
    //0xAC
    public int stepTick;
    //0xAE
    public int moveDirOrMode;
    //0xB0
    public int deltaXdX;
    //0xB1
    public int deltaYdY;
    //0xB2
    public final byte[] reserved0xB2 = new byte[0x02];

    /**
     * Native support wrapper around UnitMovementState::Init @00551346.
     */
    public UnitMovementState() {
        resetToDefaults();
    }

    /**
     * Native: UnitMovementState::Init @00551346.
     * Fully ported.
     */
    public void resetToDefaults() {
        facing = 0;
        facingLast = 0;
        Arrays.fill(reserved0x02, (byte) 0);
        pathHeadRetryDelay = 0;
        movementLayerMask = DEFAULT_MOVEMENT_LAYER_MASK;
        positionCellX = 0;
        positionCellY = 0;
        missionScanRadius = DEFAULT_MISSION_SCAN_RADIUS;
        pathSearchRefreshCount = DEFAULT_PATH_SEARCH_REFRESH_COUNT;
        rotationSpeed = DEFAULT_ROTATION_SPEED;
        movementSegmentCount = 0;
        Arrays.fill(movementSegmentState, 0);
        Arrays.fill(movementSegmentDirection, 0);
        Arrays.fill(movementSegmentXdX, 0);
        Arrays.fill(movementSegmentYdY, 0);
        movementRequestPending = 0;
        pathCurrentCell = 0;
        terrainStepDistance = 0;
        pathTargetCell = 0;
        missionReentryCell = 0;
        pathRefreshTicks = 0;
        Arrays.fill(reserved0x79, (byte) 0);
        movementTargetUnit = 0;
        cell = 0;
        attachCellX = 0;
        attachCellY = 0;
        attachCellXdX = 0;
        attachCellYdY = 0;
        detachCellX = 0;
        detachCellY = 0;
        detachCellXdX = 0;
        detachCellYdY = 0;
        targetEngagementStaticPathLength = 0;
        targetEngagementCell = 0;
        Arrays.fill(reserved0x8E, (byte) 0);
        missionReentryPending = 0;
        dynamicPathTargetCell = 0;
        dynamicPathSourceCell = 0;
        movementEventPending = 0;
        movementStepState = 0;
        rotationTicks = 0;
        Arrays.fill(reserved0x9E, (byte) 0);
        rotationActive = 0;
        positionChangedStep = 0;
        Arrays.fill(reserved0xA5, (byte) 0);
        subTileStepDistance = 0;
        stepTickLimit = 0;
        stepTick = 0;
        moveDirOrMode = 0;
        deltaXdX = 0;
        deltaYdY = 0;
        Arrays.fill(reserved0xB2, (byte) 0);
    }

    /**
     * Native: UnitMovementState::applyTokenMovementLayerMask @00557A89.
     * Fully ported.
     */
    public void applyTokenMovementLayerMask(Unit unit) {
        switch (unit.getMovementType() & 0xFF) {
            case 1 -> movementLayerMask = 0x41;
            case 2 -> movementLayerMask = 0x44;
            case 3 -> movementLayerMask = 0x82;
            default -> {
                return;
            }
        }
    }

    /**
     * Native: UnitMovementState::PositionChanged @00558B3D.
     * Fully ported. Java returns the native outStep byte, or -1 when the native BOOL result is false.
     */
    public int positionChanged(int facingLastSnapshot) {
        if (facingLastSnapshot == facingLast) {
            return -1;
        }
        return positionChangedStep;
    }

    /**
     * Native: UnitMovementState::getFacingStep2 @005514CE.
     * Fully ported.
     */
    public int getFacingStep2() {
        return ((facing + 8) & 0xFF) >>> 4;
    }

    /**
     * Native support extracted from MissionScriptRuntime::EnterLoadedScenarioGroupUnitScriptState @0056D4BA.
     */
    public int packPositionCell() {
        return ((positionCellY & 0xFF) << 8) | (positionCellX & 0xFF);
    }

    /**
     * Native support extracted from Effect::applyScaledModifier @0051D436.
     */
    public void addRotationSpeed(int delta) {
        int next = (rotationSpeed + delta) & 0xFF;
        rotationSpeed = next;
    }

    /**
     * Native: UnitMovementState::Serialize @005595A8.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        if (!ar.isStoring()) {
            readFromArchive(ar);
        } else {
            writeToArchive(ar);
        }
    }

    /**
     * Native: UnitMovementState::RestoreContext @00559681.
     * Fully ported.
     */
    public void restoreContext() {
        if (!(movementTargetUnit instanceof Number token)) {
            return;
        }
        int key = token.intValue();
        if (key == 0) {
            return;
        }
        if (!Globals.gameServer.hasPointerMapKey(key)) {
            return;
        }

        movementTargetUnit = Globals.gameServer.lookupPointerMap(key);
    }

    /**
     * Native support helper for Java-side UnitMovementState field copies. not ported.
     */
    public void assign(UnitMovementState source) {
        if (source == null) {
            resetToDefaults();
            return;
        }
        facing = source.facing;
        facingLast = source.facingLast;
        System.arraycopy(source.reserved0x02, 0, reserved0x02, 0, reserved0x02.length);
        pathHeadRetryDelay = source.pathHeadRetryDelay;
        movementLayerMask = source.movementLayerMask;
        positionCellX = source.positionCellX;
        positionCellY = source.positionCellY;
        missionScanRadius = source.missionScanRadius;
        pathSearchRefreshCount = source.pathSearchRefreshCount;
        rotationSpeed = source.rotationSpeed;
        movementSegmentCount = source.movementSegmentCount;
        System.arraycopy(source.movementSegmentState, 0, movementSegmentState, 0, movementSegmentState.length);
        System.arraycopy(source.movementSegmentDirection, 0, movementSegmentDirection, 0, movementSegmentDirection.length);
        System.arraycopy(source.movementSegmentXdX, 0, movementSegmentXdX, 0, movementSegmentXdX.length);
        System.arraycopy(source.movementSegmentYdY, 0, movementSegmentYdY, 0, movementSegmentYdY.length);
        movementRequestPending = source.movementRequestPending;
        pathCurrentCell = source.pathCurrentCell;
        terrainStepDistance = source.terrainStepDistance;
        pathTargetCell = source.pathTargetCell;
        missionReentryCell = source.missionReentryCell;
        pathRefreshTicks = source.pathRefreshTicks;
        System.arraycopy(source.reserved0x79, 0, reserved0x79, 0, reserved0x79.length);
        movementTargetUnit = source.movementTargetUnit;
        cell = source.cell;
        attachCellX = source.attachCellX;
        attachCellY = source.attachCellY;
        attachCellXdX = source.attachCellXdX;
        attachCellYdY = source.attachCellYdY;
        detachCellX = source.detachCellX;
        detachCellY = source.detachCellY;
        detachCellXdX = source.detachCellXdX;
        detachCellYdY = source.detachCellYdY;
        targetEngagementStaticPathLength = source.targetEngagementStaticPathLength;
        targetEngagementCell = source.targetEngagementCell;
        System.arraycopy(source.reserved0x8E, 0, reserved0x8E, 0, reserved0x8E.length);
        missionReentryPending = source.missionReentryPending;
        dynamicPathTargetCell = source.dynamicPathTargetCell;
        dynamicPathSourceCell = source.dynamicPathSourceCell;
        movementEventPending = source.movementEventPending;
        movementStepState = source.movementStepState;
        rotationTicks = source.rotationTicks;
        System.arraycopy(source.reserved0x9E, 0, reserved0x9E, 0, reserved0x9E.length);
        rotationActive = source.rotationActive;
        positionChangedStep = source.positionChangedStep;
        System.arraycopy(source.reserved0xA5, 0, reserved0xA5, 0, reserved0xA5.length);
        subTileStepDistance = source.subTileStepDistance;
        stepTickLimit = source.stepTickLimit;
        stepTick = source.stepTick;
        moveDirOrMode = source.moveDirOrMode;
        deltaXdX = source.deltaXdX;
        deltaYdY = source.deltaYdY;
        System.arraycopy(source.reserved0xB2, 0, reserved0xB2, 0, reserved0xB2.length);
    }

    /**
     * Native support extracted from UnitMovementState::Serialize @005595A8.
     */
    private void readFromArchive(CArchive ar) throws IOException {
        facing = ar.readByte() & 0xFF;
        facingLast = ar.readByte() & 0xFF;
        readBytesInto(ar, reserved0x02);
        pathHeadRetryDelay = ar.readByte() & 0xFF;
        movementLayerMask = ar.readByte() & 0xFF;
        positionCellX = ar.readByte() & 0xFF;
        positionCellY = ar.readByte() & 0xFF;
        missionScanRadius = ar.readByte() & 0xFF;
        pathSearchRefreshCount = ar.readByte() & 0xFF;
        rotationSpeed = ar.readByte() & 0xFF;
        movementSegmentCount = ar.readByte() & 0xFF;
        for (int i = 0; i < MOVEMENT_SEGMENT_CAPACITY; i++) {
            movementSegmentState[i] = ar.readByte() & 0xFF;
        }
        for (int i = 0; i < MOVEMENT_SEGMENT_CAPACITY; i++) {
            movementSegmentDirection[i] = ar.readByte() & 0xFF;
        }
        for (int i = 0; i < MOVEMENT_SEGMENT_CAPACITY; i++) {
            movementSegmentXdX[i] = ar.readUShort();
        }
        for (int i = 0; i < MOVEMENT_SEGMENT_CAPACITY; i++) {
            movementSegmentYdY[i] = ar.readUShort();
        }
        movementRequestPending = ar.readInt();
        pathCurrentCell = ar.readUShort();
        terrainStepDistance = ar.readUShort();
        pathTargetCell = ar.readUShort();
        missionReentryCell = ar.readUShort();
        pathRefreshTicks = ar.readByte() & 0xFF;
        readBytesInto(ar, reserved0x79);
        movementTargetUnit = ar.readInt();
        cell = ar.readUShort();
        attachCellX = ar.readByte() & 0xFF;
        attachCellY = ar.readByte() & 0xFF;
        attachCellXdX = ar.readByte() & 0xFF;
        attachCellYdY = ar.readByte() & 0xFF;
        detachCellX = ar.readByte() & 0xFF;
        detachCellY = ar.readByte() & 0xFF;
        detachCellXdX = ar.readByte() & 0xFF;
        detachCellYdY = ar.readByte() & 0xFF;
        targetEngagementStaticPathLength = ar.readUShort();
        targetEngagementCell = ar.readUShort();
        readBytesInto(ar, reserved0x8E);
        missionReentryPending = ar.readInt();
        dynamicPathTargetCell = ar.readUShort();
        dynamicPathSourceCell = ar.readUShort();
        movementEventPending = ar.readInt();
        movementStepState = ar.readByte() & 0xFF;
        rotationTicks = ar.readByte() & 0xFF;
        readBytesInto(ar, reserved0x9E);
        rotationActive = ar.readInt();
        positionChangedStep = ar.readByte() & 0xFF;
        readBytesInto(ar, reserved0xA5);
        subTileStepDistance = ar.readUShort();
        stepTickLimit = ar.readUShort();
        stepTick = ar.readUShort();
        moveDirOrMode = ar.readUShort();
        deltaXdX = ar.readByte();
        deltaYdY = ar.readByte();
        readBytesInto(ar, reserved0xB2);
    }

    /**
     * Native support extracted from UnitMovementState::Serialize @005595A8.
     */
    private void writeToArchive(CArchive ar) throws IOException {
        ar.writeByte(facing);
        ar.writeByte(facingLast);
        ar.writeBytes(reserved0x02);
        ar.writeByte(pathHeadRetryDelay);
        ar.writeByte(movementLayerMask);
        ar.writeByte(positionCellX);
        ar.writeByte(positionCellY);
        ar.writeByte(missionScanRadius);
        ar.writeByte(pathSearchRefreshCount);
        ar.writeByte(rotationSpeed);
        ar.writeByte(movementSegmentCount);
        for (int i = 0; i < MOVEMENT_SEGMENT_CAPACITY; i++) {
            ar.writeByte(movementSegmentState[i]);
        }
        for (int i = 0; i < MOVEMENT_SEGMENT_CAPACITY; i++) {
            ar.writeByte(movementSegmentDirection[i]);
        }
        for (int i = 0; i < MOVEMENT_SEGMENT_CAPACITY; i++) {
            ar.writeShort(movementSegmentXdX[i]);
        }
        for (int i = 0; i < MOVEMENT_SEGMENT_CAPACITY; i++) {
            ar.writeShort(movementSegmentYdY[i]);
        }
        ar.writeInt(movementRequestPending);
        ar.writeShort(pathCurrentCell);
        ar.writeShort(terrainStepDistance);
        ar.writeShort(pathTargetCell);
        ar.writeShort(missionReentryCell);
        ar.writeByte(pathRefreshTicks);
        ar.writeBytes(reserved0x79);
        ar.writeInt(Utils.encodePointerLike(movementTargetUnit));
        ar.writeShort(cell);
        ar.writeByte(attachCellX);
        ar.writeByte(attachCellY);
        ar.writeByte(attachCellXdX);
        ar.writeByte(attachCellYdY);
        ar.writeByte(detachCellX);
        ar.writeByte(detachCellY);
        ar.writeByte(detachCellXdX);
        ar.writeByte(detachCellYdY);
        ar.writeShort(targetEngagementStaticPathLength);
        ar.writeShort(targetEngagementCell);
        ar.writeBytes(reserved0x8E);
        ar.writeInt(missionReentryPending);
        ar.writeShort(dynamicPathTargetCell);
        ar.writeShort(dynamicPathSourceCell);
        ar.writeInt(movementEventPending);
        ar.writeByte(movementStepState);
        ar.writeByte(rotationTicks);
        ar.writeBytes(reserved0x9E);
        ar.writeInt(rotationActive);
        ar.writeByte(positionChangedStep);
        ar.writeBytes(reserved0xA5);
        ar.writeShort(subTileStepDistance);
        ar.writeShort(stepTickLimit);
        ar.writeShort(stepTick);
        ar.writeShort(moveDirOrMode);
        ar.writeByte(deltaXdX);
        ar.writeByte(deltaYdY);
        ar.writeBytes(reserved0xB2);
    }

    /**
     * Native support extracted from UnitMovementState::Serialize @005595A8 reserved gap reads.
     */
    private static void readBytesInto(CArchive ar, byte[] destination) throws IOException {
        byte[] loaded = ar.readBytes(destination.length);
        System.arraycopy(loaded, 0, destination, 0, destination.length);
    }
}
