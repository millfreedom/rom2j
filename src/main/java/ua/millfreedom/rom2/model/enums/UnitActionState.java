package ua.millfreedom.rom2.model.enums;

/**
 * Native enum: UnitActionState, the live Unit.state selector used by Unit::update @0050F12C and
 * MissionScriptRuntime::updateUnitMissionRuntime @0056DCA2.
 * Distinct from MissionActionCode, which selects mission-order dispatch.
 */
public enum UnitActionState {
    // Native default/reset state and death-transition state; written by Unit::InitSomething @0050E173,
    // Unit::update @0050F12C, and MissionScriptRuntime::updateUnitMissionRuntime @0056DCA2.
    DYING(0),
    // Native idle motion state handled by Unit::update @0050F12C.
    IDLE(1),
    // Native contact/command-cell action state handled by Unit::update @0050F12C.
    ATTACK(2),
    // Native action execution state for movement/weapon action windup in Unit::update @0050F12C.
    MOVE(3),
    // Values 4..12 are present in native enum data but were not resolved by the supplied Unit.state usage sites.
    STATE_4(4),
    STATE_5(5),
    STATE_6(6),
    STATE_7(7),
    STATE_8(8),
    STATE_9(9),
    STATE_10(10),
    STATE_11(11),
    STATE_12(12),
    // Native spell-cast action state handled by Unit::update @0050F12C.
    CAST_SPELL(13),
    // Native point-skill action state handled by Unit::update @0050F12C.
    USE_SKILL(14),
    // Native building/object interaction state handled by Unit::update @0050F12C.
    INTERACT(15),
    // Native final dead state written by Unit::FinalizeDeath @00510A70.
    DEAD(16),
    // Native writes literal 0x1B in MissionScriptRuntime::updateUnitMissionRuntime @0056DCA2 and
    // hideUnitMissionStateIfDead @005736E4 for mission-hidden/suppressed units.
    MISSION_HIDDEN(0x1B);

    // Native UnitActionState integer selector value.
    public final int value;

    /**
     * Native support for UnitActionState enum values recovered from /classes/Unit/UnitActionState.
     */
    UnitActionState(int value) {
        this.value = value;
    }

    /**
     * Native support extracted from Unit::Serialize @0052C618 state archive reads.
     */
    public static UnitActionState fromValue(int value) {
        for (UnitActionState state : values()) {
            if (state.value == value) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown unit state: " + value);
    }
}
