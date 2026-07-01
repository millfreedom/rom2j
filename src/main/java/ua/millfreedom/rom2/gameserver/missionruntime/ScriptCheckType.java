package ua.millfreedom.rom2.gameserver.missionruntime;

/**
 * Native check type domain used by MissionScriptRuntime::applyScriptCheck @00574955.
 * The values share storage with ScriptInstant.type in native ScriptCheck inheritance, but not semantics.
 */
public enum ScriptCheckType {
    INVALID(0),
    GROUP_UNIT_COUNT(1),
    UNIT_IN_RECTANGLE(2),
    UNIT_NEAR_POINT(3),
    UNIT_HIT_POINTS(4),
    UNIT_ALIVE(5),
    UNIT_DISTANCE(6),
    UNIT_POINT_DISTANCE(7),
    PLAYER_UNIT_COUNT(8),
    ACTIVE_ENGAGEMENT_TARGET_SCRIPT_ID(9),
    DIPLOMACY_STATE(10),
    NO_OP_11(11),
    UNIT_INVENTORY_ITEM(12),
    NO_OP_13(13),
    SACK_AT_POINT(14),
    CLOSEST_PLAYER_UNIT_TO_POINT(15),
    INVENTORY_ITEM_DISTANCE(16),
    UNIT_INVENTORY_ITEM_DUPLICATE(17),
    DEAD_UNIT_FAILURE_COUNTER(18),
    SCRIPT_VARIABLE(19),
    PLAYER_BUILDING_COUNT(20),
    BUILDING_HIT_POINTS(21),
    MISSION_ENTRY_RELOCATION(22),
    SCENARIO_VAR(23),
    SCENARIO_TRANSIENT_VAR(24),
    AREA_EFFECT_LAYER_PRESENCE(25),
    UNIT_EFFECT_KEY_FLAG(26),
    UNIT_EXACT_CELL(27),
    UNKNOWN(-1);

    // Native enum value.
    public final int id;

    /**
     * Native support for MissionScriptRuntime::applyScriptCheck @00574955 case values.
     */
    ScriptCheckType(int id) {
        this.id = id;
    }

    /**
     * Native support for MissionScriptRuntime::applyScriptCheck @00574955 dispatch.
     */
    public static ScriptCheckType fromId(int id) {
        for (ScriptCheckType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
