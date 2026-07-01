package ua.millfreedom.rom2.gameserver.missionruntime;

/**
 * Native: ScriptInstant::InstantType enum used by MissionScriptRuntime::executeScriptInstant @00574F3F.
 */
public enum InstantType {
    INVALID(0),
    INCREMENT_RUNTIME_COUNTERS(1),
    OPEN_QUEST_OBJECTIVES_QUERY(2),
    SET_SCRIPT_VARIABLE(3),
    MISSION_COMPLETE(4),
    MISSION_FAILED(5),
    GROUP_SCRIPT_STATE(6),
    SET_PLAYER_FORMATION_MODE(7),
    INCREMENT_SCRIPT_VARIABLE(8),
    NO_OP(9),
    SET_DIPLOMACY_STATE(10),
    TRANSFER_UNIT_INVENTORY_ITEM(11),
    ADD_UNIT_INVENTORY_ITEM(12),
    REMOVE_UNIT_INVENTORY_ITEM(13),
    SEND_UNIT_EQUIPMENT_STATE_UPDATE(0x0E),
    SEND_UNIT_EQUIPMENT_STATE_UPDATE_DUPLICATE(0x0F),
    HIDE_UNIT_FROM_MISSION_MAP(0x10),
    RETURN_UNIT_TO_MISSION_MAP(0x11),
    SWAP_HIDDEN_UNIT_WITH_TARGET_UNIT(0x12),
    TRANSFER_UNIT_TO_PLAYER(0x13),
    DROP_UNIT_INVENTORY_DEATH_SACK(0x14),
    TRANSIENT_POINT_SPELL_CAST(0x15),
    TRANSFER_GROUP_UNITS_TO_PLAYER(0x16),
    ADJUST_PLAYER_GOLD(0x17),
    TRANSIENT_TARGET_SPELL_CAST(0x18),
    TRANSIENT_CELL_SPELL_CAST(0x19),
    SET_BUILDING_HIT_POINTS(0x1A),
    RELOCATE_UNIT_MISSION_ENTRY(0x1B),
    DRAIN_UNIT_INVENTORY_TO_TARGET(0x1C),
    SET_AREA_EFFECT_DURATION(0x1D),
    SET_UNIT_EFFECT_VALUE(0x1E),
    SET_OUTPOST_RESPAWN_TIMER(0x1F),
    HIDE_GROUP_UNITS_FROM_MISSION_MAP(0x20),
    RETURN_GROUP_UNITS_TO_MISSION_MAP(0x21),
    SET_UNIT_STAT_VALUE(0x22),
    SCENARIO_SET_VAR(0x23),
    SCENARIO_TRANSIENT_STATE(0x24),
    REMOVE_ITEM_FROM_ACTIVE_UNIT_INVENTORIES(0x26),
    CLEAR_SCENARIO_SCRIPT_REFERENCED_FLAG(0x27),
    UNKNOWN(-1);

    // Native enum value.
    public final int id;

    /**
     * Native support for ScriptInstant::InstantType numeric values.
     */
    InstantType(int id) {
        this.id = id;
    }

    /**
     * Native support for MissionScriptRuntime::loadScriptInstant @00576380 and
     * MissionScriptRuntime::executeScriptInstant @00574F3F.
     */
    public static InstantType fromId(int id) {
        for (InstantType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
