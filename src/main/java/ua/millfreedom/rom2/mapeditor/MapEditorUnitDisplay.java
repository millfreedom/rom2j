package ua.millfreedom.rom2.mapeditor;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.column.HumanColumn;
import ua.millfreedom.rom2.model.column.UnitColumn;
import ua.millfreedom.rom2.model.enums.UnitId;
import ua.millfreedom.rom2.model.unit.UnitInfo;
import ua.millfreedom.rom2.model.unit.humanoid.human.HumanInfo;
import ua.millfreedom.rom2.model.world.scenario.UnitDTO;

/**
 * Editor-only scenario-unit presentation helpers matching native materialization semantics.
 * not ported.
 */
final class MapEditorUnitDisplay {
    private static final int SCENARIO_HUMANOID_UNIT_FLAG = 0x10;
    private static final int UNKNOWN_HEALTH = Integer.MIN_VALUE;

    /**
     * Java utility constructor.
     * not ported.
     */
    private MapEditorUnitDisplay() {
    }

    /**
     * Java support list label for scenario units using native-visible ids and resolved static-data names.
     * not ported.
     */
    static String unitLabel(UnitDTO unit) {
        return "#" + nativeUnitId(unit)
                + " " + resolvedName(unit)
                + " @ " + unitTileX(unit) + "," + unitTileY(unit)
                + " p" + unit.playerID
                + " HP " + healthLabel(unit);
    }

    /**
     * Java support native-visible scenario unit id display.
     * not ported.
     */
    static int nativeUnitId(UnitDTO unit) {
        return nativeUnitIdForStoredId(unit.unitID);
    }

    /**
     * Java support native-visible scenario unit id display for raw stored unit ids.
     * not ported.
     */
    static int nativeUnitIdForStoredId(int unitID) {
        return (short) unitID;
    }

    /**
     * Java support preserving the loaded raw id word when the editor-visible id was not changed.
     * not ported.
     */
    static int storedUnitId(UnitDTO unit, int editedNativeUnitId) {
        if (editedNativeUnitId == nativeUnitId(unit)) {
            return unit.unitID;
        }
        return editedNativeUnitId;
    }

    /**
     * Java support signed WORD display for native scenario HP and max-HP sentinel values.
     * not ported.
     */
    static int signedScenarioWord(int value) {
        return (short) (value & 0xFFFF);
    }

    /**
     * Java support editor-property HP display using the same native-materialized value as unit labels.
     * not ported.
     */
    static int displayHp(UnitDTO unit) {
        int maxHp = effectiveMaxHp(unit);
        int hp = effectiveHp(unit, maxHp);
        if (hp == UNKNOWN_HEALTH || maxHp == UNKNOWN_HEALTH) {
            return signedScenarioWord(unit.hp);
        }
        return hp;
    }

    /**
     * Java support editor-property max-HP display using the same native-materialized value as unit labels.
     * not ported.
     */
    static int displayMaxHp(UnitDTO unit) {
        int maxHp = effectiveMaxHp(unit);
        if (maxHp == UNKNOWN_HEALTH) {
            return signedScenarioWord(unit.maxHp);
        }
        return maxHp;
    }

    /**
     * Java support preserving the loaded raw HP word when the editor-visible HP was not changed.
     * not ported.
     */
    static int storedHp(UnitDTO unit, int editedDisplayHp) {
        if (editedDisplayHp == displayHp(unit)) {
            return unit.hp;
        }
        return editedDisplayHp;
    }

    /**
     * Java support preserving the loaded raw max-HP word when the editor-visible max-HP was not changed.
     * not ported.
     */
    static int storedMaxHp(UnitDTO unit, int editedDisplayMaxHp) {
        if (editedDisplayMaxHp == displayMaxHp(unit)) {
            return unit.maxHp;
        }
        return editedDisplayMaxHp;
    }

    /**
     * Java support conversion from raw scenario-unit X coordinate to tile coordinate.
     * not ported.
     */
    static int unitTileX(UnitDTO unit) {
        return (unit.x >>> 8) & 0xFFFF;
    }

    /**
     * Java support conversion from raw scenario-unit Y coordinate to tile coordinate.
     * not ported.
     */
    static int unitTileY(UnitDTO unit) {
        return (unit.y >>> 8) & 0xFFFF;
    }

    /**
     * Java support scenario-unit name resolution matching ScenarioMapLoader/MapDescriptor server-id lookup.
     * not ported.
     */
    private static String resolvedName(UnitDTO unit) {
        if (isScenarioHumanoidUnit(unit)) {
            int humanIndex = Globals.staticDataMgr.findHumanByServerId(unit.serverID);
            if (humanIndex > 0 && humanIndex < Globals.staticDataMgr.humans.size()) {
                HumanInfo info = Globals.staticDataMgr.humans.get(humanIndex);
                if (!info.name.isEmpty()) {
                    return info.name;
                }
            }
        } else {
            int unitIndex = Globals.staticDataMgr.findUnitByServerId(unit.serverID);
            if (unitIndex > 0 && unitIndex < Globals.staticDataMgr.units.size()) {
                UnitInfo info = Globals.staticDataMgr.units.get(unitIndex);
                if (!info.name.isEmpty()) {
                    return info.name;
                }
            }
        }
        return rawTypeName(unit.typeID);
    }

    /**
     * Java support effective HP/max-HP display after native scenario override semantics.
     * not ported.
     */
    private static String healthLabel(UnitDTO unit) {
        return displayHp(unit) + "/" + displayMaxHp(unit);
    }

    /**
     * Java support effective max-HP calculation for editor labels.
     * not ported.
     */
    private static int effectiveMaxHp(UnitDTO unit) {
        int scenarioMaxHp = signedScenarioWord(unit.maxHp);
        if (isIgnoredHumanoidEqualHealthOverride(unit)) {
            scenarioMaxHp = -1;
        }
        if (scenarioMaxHp != -1) {
            return scenarioMaxHp;
        }
        return templateMaxHp(unit);
    }

    /**
     * Java support effective HP calculation for editor labels.
     * not ported.
     */
    private static int effectiveHp(UnitDTO unit, int maxHp) {
        int scenarioHp = signedScenarioWord(unit.hp);
        if (isIgnoredHumanoidEqualHealthOverride(unit)) {
            scenarioHp = -1;
        }
        if (scenarioHp != -1) {
            return scenarioHp;
        }
        return maxHp;
    }

    /**
     * Java support humanoid scenario-health override sentinel used by native materialization.
     * not ported.
     */
    private static boolean isIgnoredHumanoidEqualHealthOverride(UnitDTO unit) {
        int scenarioHp = signedScenarioWord(unit.hp);
        int scenarioMaxHp = signedScenarioWord(unit.maxHp);
        return isScenarioHumanoidUnit(unit) && scenarioMaxHp != -1 && scenarioMaxHp == scenarioHp;
    }

    /**
     * Java support static-data template max-HP lookup by scenario server id.
     * not ported.
     */
    private static int templateMaxHp(UnitDTO unit) {
        if (isScenarioHumanoidUnit(unit)) {
            int humanIndex = Globals.staticDataMgr.findHumanByServerId(unit.serverID);
            if (humanIndex > 0 && humanIndex < Globals.staticDataMgr.humans.size()) {
                return Globals.staticDataMgr.humans.get(humanIndex).getValue(HumanColumn.HEALTH_MAX.index);
            }
            return UNKNOWN_HEALTH;
        }
        int unitIndex = Globals.staticDataMgr.findUnitByServerId(unit.serverID);
        if (unitIndex > 0 && unitIndex < Globals.staticDataMgr.units.size()) {
            return Globals.staticDataMgr.units.get(unitIndex).getAttribute(UnitColumn.HEALTH_MAX);
        }
        return UNKNOWN_HEALTH;
    }

    /**
     * Java support scenario humanoid flag predicate.
     * not ported.
     */
    private static boolean isScenarioHumanoidUnit(UnitDTO unit) {
        return (unit.unitFlags1 & SCENARIO_HUMANOID_UNIT_FLAG) != 0;
    }

    /**
     * Java support raw unit type fallback label.
     * not ported.
     */
    private static String rawTypeName(int typeID) {
        UnitId unitId = UnitId.fromId(typeID);
        return unitId == UnitId.UNKNOWN || unitId.tableName.isEmpty() ? "type " + typeID : unitId.tableName;
    }
}
