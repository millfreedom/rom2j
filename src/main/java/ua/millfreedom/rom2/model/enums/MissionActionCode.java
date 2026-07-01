package ua.millfreedom.rom2.model.enums;

/**
 * Native enum: MissionActionCode, the Unit.missionActionCode selector dispatched by
 * MissionScriptRuntime::applyUnitMissionActionState @00569321.
 * Distinct from UnitActionState, which is the live Unit.state runtime/action selector.
 */
public enum MissionActionCode {
    // Native fallback sentinel for unresolved serialized selectors.
    UNKNOWN(-1),
    // Native default/death-transition selector; dispatcher writes UnitActionState.DYING.
    DYING(0),
    // Native movement order initialized at @0056D107 and @0056D16E.
    MOVE_TO_CELL(1),
    // Native pickup order initialized at @004F5B73 and @0056DAAE.
    PICKUP_ORDER(2),
    // Native target engagement order initialized at @0056D1EA.
    ATTACK_TARGET(3),
    // Native attack-cell/command-cell selector initialized at @0056D3A9; dispatcher either moves to
    // commandCell or engages a hostile already at the unit's current cell.
    COMMAND_CELL_OR_TARGET(4),
    // Native range-target engagement selector initialized at @0056D5E5.
    RANGE_TARGET_ENGAGE(8),
    // Native waypoint selector initialized at @0056D6CD, @0056D7CB, and @0056D870.
    WAYPOINT(10),
    // Native script-cell status selector initialized at @0050EC6A and @0056D4D0.
    SCRIPT_CELL_STATUS(0x0B),
    // Native engage-nearest selector initialized at @0056D541.
    ENGAGE_NEAREST(0x0C),
    // Native targeted spell order initialized at @0056D3FC.
    TARGETED_SPELL_ORDER(0x0D),
    // Native cell spell order initialized at @0056D45C.
    CELL_SPELL_ORDER(0x0E),
    // Native interaction/building order initialized at @0056D243.
    INTERACT(0x0F),
    // Native final death selector written by Unit::FinalizeDeath @00510A70.
    DEAD(0x10),
    // Native range-target retreat selector initialized at @0056D664.
    RANGE_TARGET_RETREAT(0x11),
    // Native retreat order initialized at @0056DA65.
    RETREAT_ORDER(0x16),
    // Native stand-still order initialized at @0056D588.
    STAND_STILL_ORDER(0x17),
    // Native special hide mission status initialized at @0056DB57.
    HIDE(0x18),
    // Native fixed evacuation mission status initialized at @0056DB87.
    FIXED_EVACUATION(0x19),
    // Native pickup-all-sacks order initialized at @0056DB14.
    PICKUP_ALL_SACKS_ORDER(0x1A),
    // Native mission-hidden selector written by hideUnitMissionStateIfDead @005736E4.
    MISSION_HIDDEN(0x1B);

    // Native MissionActionCode integer selector value.
    public final int value;

    /**
     * Native support for MissionActionCode enum values recovered from MissionScriptRuntime::applyUnitMissionActionState @00569321.
     */
    MissionActionCode(int value) {
        this.value = value;
    }

    /**
     * Native support extracted from Unit::Serialize @0052C618 missionActionCode archive reads.
     */
    public static MissionActionCode fromValue(int value) {
        for (MissionActionCode actionCode : values()) {
            if (actionCode.value == value) {
                return actionCode;
            }
        }
        throw new IllegalArgumentException("Unknown mission action code: " + value);
    }
}
