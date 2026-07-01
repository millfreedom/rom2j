package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.gameserver.MissionScriptRuntime;
import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.model.unit.Unit;

import java.io.IOException;

/**
 * Native player-owned unit membership container (size 0x50).
 * Recovered members:
 * +0x00 CList<Unit> units
 * +0x1c int groupKey
 * +0x20 CList<short> shortsList
 * +0x3c MissionGroupRuntimeState* missionState
 * +0x40 Unit* unit
 * +0x44 Player* owner
 * +0x48 inn/group relocation quest flag
 * +0x4c hostile-group relocation quest flag
 * <p>
 * Native methods:
 * - New @0052AE43
 * - AddUnit @0052AF8C
 * - RemoveUnit @0052AFD2
 * - Serialize @0052D932
 */
public class UnitGroup implements MfcSerializable {
    //0x00
    public final CustomList<Unit> units = new CustomList<>(Unit.class);
    //0x1C
    public int groupKey;
    //0x20
    public final CustomList<Short> shortsList = CustomList.std(Short.class);
    //0x3C
    public MissionGroupRuntimeState missionState;
    //0x40
    public Unit unit;
    //0x44
    public Player owner;
    //0x48
    public int innGroupRelocationQuestFlag;
    //0x4C
    public int hostileGroupRelocationQuestFlag;

    /**
     * Native: UnitGroup::New @0052AE43.
     * Fully ported.
     */
    public UnitGroup() {
        missionState = new MissionGroupRuntimeState();
        unit = null;
        owner = null;
        innGroupRelocationQuestFlag = 0;
        hostileGroupRelocationQuestFlag = 0;
    }

    /**
     * Native support extracted from UnitGroup::getHeadUnit @00545370 and CList<Unit>::GetHeadUnit @0055F220.
     * Fully ported.
     */
    public Unit getHeadUnit() {
        return units.isEmpty() ? null : units.getFirst();
    }

    /**
     * Native support extracted from UnitGroup::findNextUnitAfter @00545390 and
     * CList<Unit>::FindNextUnitAfter @0055F240.
     * Fully ported.
     */
    public Unit findNextUnitAfter(Unit unit) {
        int index = units.indexOf(unit);
        if (index < 0 || index + 1 >= units.size()) {
            return null;
        }
        return units.get(index + 1);
    }

    /**
     * Native: UnitGroup::addUnit @0052AF8C.
     * Fully ported.
     */
    public void addUnit(Unit unit) {
        if (unit.unitGroup != null) {
            unit.unitGroup.removeUnit(unit);
        }
        units.add(unit);
        unit.unitGroup = this;
        owner = unit.owner;
    }

    /**
     * Native: UnitGroup::removeUnit @0052AFD2.
     * Fully ported.
     */
    public void removeUnit(Unit unit) {
        units.remove(unit);
        unit.unitGroup = null;
    }

    /**
     * Native: UnitGroup::updateRegen @0052B01F.
     * Fully ported.
     */
    public void updateRegen() {
        for (Unit unit : units) {
            unit.updateRegen();
        }
    }

    /**
     * Native: UnitGroup::Serialize @0052D932.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        ar.serialize(shortsList);
        ar.serialize(missionState);

        if (ar.isStoring()) {
            ar.serialize(units);
            ar.writeInt(groupKey);
            ar.writeInt(Utils.encodePointerLike(unit));
            ar.writeInt(Utils.encodePointerLike(owner));
        } else {
            for (Unit unit : units) {
                if (unit != null && unit.unitGroup == this) {
                    unit.unitGroup = null;
                }
            }
            units.clear();
            int size = ar.readInt();
            for (int i = 0; i < size; i++) {
                addUnit(Unit.readFromArchive(ar));
            }
            groupKey = ar.readInt();

            unit = Unit.restoreContextToken(ar.readInt());
            owner = (Player) Globals.gameServer.lookupPointerMapOrKeepToken(ar.readInt());
        }
    }

    /**
     * Native support extracted from Player::Serialize post-load group fixups @0052D52E.
     * Native support extracted from UnitGroup::Serialize @0052D932 pointer restoration.
     * Fully ported.
     */
    public void restoreContext() {
        unit = Unit.restoreContextToken(unit);
        owner = (Player) Globals.gameServer.lookupPointerMapOrKeepToken(owner);
    }

    /**
     * Native: InitializeScenarioMissionEntryGroup @00570F2B.
     * Fully ported.
     *
     * @param missionScriptRuntime
     */
    public void initializeScenarioMissionEntryGroup(MissionScriptRuntime missionScriptRuntime) {
        resetScenarioMissionGroupScriptState(missionScriptRuntime);
        enterScenarioMissionGroupScriptState(missionScriptRuntime);
    }

    /**
     * Native: resetScenarioMissionGroupScriptState @00570E83.
     * Fully ported.
     *
     * @param missionScriptRuntime
     */
    public void resetScenarioMissionGroupScriptState(MissionScriptRuntime missionScriptRuntime) {
        for (Unit unit : units) {
            unit.resetScenarioMissionUnitScriptState(missionScriptRuntime);
            unit.missionRuntimeState.groupScriptState = 0;
        }
        missionState.scriptRuntimeState = 0;
    }

    /**
     * Native: EnterScenarioMissionGroupScriptState @00570B8B.
     * Fully ported.
     *
     * @param missionScriptRuntime
     */
    public void enterScenarioMissionGroupScriptState(MissionScriptRuntime missionScriptRuntime) {
        resetScenarioMissionGroupScriptState(missionScriptRuntime);
        for (Unit unit : units) {
            unit.enterScenarioMissionUnitScriptState(missionScriptRuntime);
        }
        missionState.scriptRuntimeState = 3;
    }
}
