package ua.millfreedom.rom2.gameserver.missionruntime;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.UnitGroup;
import ua.millfreedom.rom2.model.Building;
import ua.millfreedom.rom2.model.unit.Unit;

import java.io.IOException;

/**
 * Native 0x74-byte instant record stored in CArray at MissionScriptRuntime +0xEAB0.
 */
public class ScriptInstant implements MfcSerializable {
    public static final int VARIABLE_REFERENCE_MODE_PRIMARY = 1;
    public static final int VARIABLE_REFERENCE_MODE_SECONDARY = 2;

    //0x00
    public int digit;
    //0x04
    public int type;
    //0x08
    public final int[] argValues = new int[10];
    //0x30
    public final int[] argTypes = new int[10];
    //0x3a
    public final byte[] variableReferenceMode = new byte[10];
    //0x44
    public final byte[] variableReferencePage = new byte[10];
    //0x4e
    public final byte[] variableReferenceSlot = new byte[10];
    //0x58
    public int variableReferenceCount;
    //0x5C
    public Unit unit;
    //0x60
    public UnitGroup group;
    //0x64
    public Player player;
    //0x68
    public Object secondaryTarget;
    //0x6c
    public int itemId;
    //0x70
    public Building building;
    // Java-only restore token for native +0x5C before ScriptInstant::restoreContext @00579AE7 resolves the pointer map.
    public Object unitRestoreToken;
    // Java-only restore token for native +0x60 before ScriptInstant::restoreContext @00579AE7 resolves the pointer map.
    public Object groupRestoreToken;
    // Java-only restore token for native +0x64 before ScriptInstant::restoreContext @00579AE7 resolves the pointer map.
    public Object playerRestoreToken;
    // Java-only storage for the native +0x70 archive slot when a building pointer token is not yet resolved.
    public Object buildingRestoreToken;

    /**
     * Native: ScriptInstant::ScriptInstant @00573DD3.
     * Fully ported.
     */
    public ScriptInstant() {
    }

    /**
     * Native support extracted from MissionScriptRuntime::loadScriptInstant @00576380.
     */
    public ScriptInstant(int digit, int type) {
        this.digit = digit;
        this.type = type;
    }

    /**
     * Native: ScriptInstant::ScriptInstant @00573E1B.
     * Fully ported.
     */
    public ScriptInstant(ScriptInstant from) {
        copyFrom(from);
    }

    /**
     * Native: ScriptInstant::clone @00573DFC.
     * Fully ported.
     */
    public void copyFrom(ScriptInstant from) {
        digit = from.digit;
        type = from.type;
        System.arraycopy(from.argValues, 0, argValues, 0, argValues.length);
        System.arraycopy(from.argTypes, 0, argTypes, 0, argTypes.length);
        System.arraycopy(from.variableReferenceMode, 0, variableReferenceMode, 0, variableReferenceMode.length);
        System.arraycopy(from.variableReferencePage, 0, variableReferencePage, 0, variableReferencePage.length);
        System.arraycopy(from.variableReferenceSlot, 0, variableReferenceSlot, 0, variableReferenceSlot.length);
        variableReferenceCount = from.variableReferenceCount;
        unit = from.unit;
        unitRestoreToken = from.unitRestoreToken;
        group = from.group;
        groupRestoreToken = from.groupRestoreToken;
        player = from.player;
        playerRestoreToken = from.playerRestoreToken;
        secondaryTarget = from.secondaryTarget;
        itemId = from.itemId;
        building = from.building;
        buildingRestoreToken = from.buildingRestoreToken;
    }

    /**
     * Native support extracted from ScenarioMapLoader::materializeScenarioScriptRuntime @00562745 variable-reference branch.
     */
    public void setVariableReference(int argIndex, int mode, int normalizedValue) {
        variableReferenceMode[argIndex] = (byte) mode;
        variableReferencePage[argIndex] = (byte) (normalizedValue / 100);
        variableReferenceSlot[argIndex] = (byte) (normalizedValue % 100);
        variableReferenceCount++;
    }

    /**
     * Native: ScriptInstant::Serialize @00579AB0.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        if (ar.isStoring()) {
            writeNativeRecord(ar);
        } else {
            readNativeRecord(ar);
        }
    }

    /**
     * Native support extracted from ScriptInstant::Serialize @00579AB0 storing path.
     */
    protected void writeNativeRecord(CArchive ar) throws IOException {
        ar.writeInt(digit);
        ar.writeInt(type);
        for (int value : argValues) {
            ar.writeInt(value);
        }
        for (int argType : argTypes) {
            ar.writeByte(argType);
        }
        ar.writeBytes(variableReferenceMode);
        ar.writeBytes(variableReferencePage);
        ar.writeBytes(variableReferenceSlot);
        ar.writeInt(variableReferenceCount);
        ar.writeInt(Utils.encodePointerLike(unit != null ? unit : unitRestoreToken));
        ar.writeInt(Utils.encodePointerLike(group != null ? group : groupRestoreToken));
        ar.writeInt(Utils.encodePointerLike(player != null ? player : playerRestoreToken));
        ar.writeInt(Utils.encodePointerLike(secondaryTarget));
        ar.writeInt(itemId);
        ar.writeInt(Utils.encodePointerLike(building != null ? building : buildingRestoreToken));
    }

    /**
     * Native support extracted from ScriptInstant::Serialize @00579AB0 loading path.
     */
    protected void readNativeRecord(CArchive ar) throws IOException {
        digit = ar.readInt();
        type = ar.readInt();
        for (int i = 0; i < argValues.length; i++) {
            argValues[i] = ar.readInt();
        }
        for (int i = 0; i < argTypes.length; i++) {
            argTypes[i] = ar.readByte() & 0xFF;
        }
        copyInto(variableReferenceMode, ar.readBytes(variableReferenceMode.length));
        copyInto(variableReferencePage, ar.readBytes(variableReferencePage.length));
        copyInto(variableReferenceSlot, ar.readBytes(variableReferenceSlot.length));
        variableReferenceCount = ar.readInt();
        unitRestoreToken = Globals.gameServer.lookupPointerMapOrKeepToken(ar.readInt());
        unit = unitRestoreToken instanceof Unit resolvedUnit ? resolvedUnit : null;
        if (unit != null) {
            unitRestoreToken = null;
        }
        groupRestoreToken = Globals.gameServer.lookupPointerMapOrKeepToken(ar.readInt());
        group = groupRestoreToken instanceof UnitGroup resolvedGroup ? resolvedGroup : null;
        if (group != null) {
            groupRestoreToken = null;
        }
        playerRestoreToken = Globals.gameServer.lookupPointerMapOrKeepToken(ar.readInt());
        player = playerRestoreToken instanceof Player resolvedPlayer ? resolvedPlayer : null;
        if (player != null) {
            playerRestoreToken = null;
        }
        secondaryTarget = Globals.gameServer.lookupPointerMapOrKeepToken(ar.readInt());
        itemId = ar.readInt();
        buildingRestoreToken = Globals.gameServer.lookupPointerMapOrKeepToken(ar.readInt());
        building = buildingRestoreToken instanceof Building resolvedBuilding ? resolvedBuilding : null;
        if (building != null) {
            buildingRestoreToken = null;
        }
    }

    /**
     * Native: ScriptInstant::restoreContext @00579AE7.
     */
    public void restoreContext() {
        Object unitRef = unitRestoreToken != null ? unitRestoreToken : unit;
        unit = (Unit) Globals.gameServer.lookupPointerMapOrNull(unitRef);
        unitRestoreToken = null;
        Object groupRef = groupRestoreToken != null ? groupRestoreToken : group;
        group = (UnitGroup) Globals.gameServer.lookupPointerMapOrNull(groupRef);
        groupRestoreToken = null;
        Object playerRef = playerRestoreToken != null ? playerRestoreToken : player;
        player = (Player) Globals.gameServer.lookupPointerMapOrNull(playerRef);
        playerRestoreToken = null;
    }

    /**
     * Native support extracted from ScriptInstant::Serialize @00579AB0 fixed-size byte array reads.
     */
    private static void copyInto(byte[] destination, byte[] source) {
        System.arraycopy(source, 0, destination, 0, destination.length);
    }
}
