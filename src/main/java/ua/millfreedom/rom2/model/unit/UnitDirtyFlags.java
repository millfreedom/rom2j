package ua.millfreedom.rom2.model.unit;

import java.util.Collection;
import java.util.Objects;

/**
 * Java enum port of the Unit.+0x150 dirty/update bitmask consumed by
 * CServerApp::NetUpdate @00502019.
 */
public enum UnitDirtyFlags {
    /**
     * No dirty flags set.
     */
    NONE(0x00000000),
    /**
     * Current HP changed.
     */
    HP(0x00000001),
    /**
     * Current MP changed.
     */
    MP(0x00000002),
    /**
     * Skill levels / skill-derived outputs changed.
     */
    SKILLS(0x00000004),
    /**
     * Derived vitals (max/regen-like dependent values) changed.
     */
    VITALS_DERIVED(0x00000008),
    /**
     * Position/facing payload changed.
     */
    POSITION_AND_FACING(0x00000010),
    /**
     * Type-id/face payload changed.
     */
    TYPE_AND_FACE(0x00000020),
    /**
     * Owning player id changed.
     */
    OWNER_PLAYER_ID(0x00000040),
    /**
     * Server id changed.
     */
    SERVER_ID(0x00000080),
    /**
     * Skill-bonus slot 1 changed.
     */
    SKILL_BONUS_1(0x00000100),
    /**
     * Skill-bonus slot 2 changed.
     */
    SKILL_BONUS_2(0x00000200),
    /**
     * Skill-bonus slot 3 changed.
     */
    SKILL_BONUS_3(0x00000400),
    /**
     * Skill-bonus slot 4 changed.
     */
    SKILL_BONUS_4(0x00000800),
    /**
     * Skill-bonus slot 5 changed.
     */
    SKILL_BONUS_5(0x00001000),
    /**
     * Encumbrance-weight payload changed.
     */
    ENCUMBRANCE_WEIGHT(0x00002000),

    /**
     * Defence-related value changed.
     */
    DEFENCE(0x00004000),
    /**
     * Absorbtion-related value changed.
     */
    ABSORBTION(0x00008000),
    /**
     * To-hit / accuracy group changed.
     */
    TO_HIT(0x00010000),
    /**
     * Damage profile/value group changed.
     */
    DAMAGE_PROFILE(0x00020000),
    /**
     * Packed sight-range value changed.
     */
    SIGHT_RANGE(0x00040000),
    /**
     * Speed-related value changed.
     */
    SPEED(0x00080000),
    /**
     * Spellbook/known spells changed.
     */
    SPELLBOOK(0x00100000),
    /**
     * Inventory-items payload changed.
     */
    INVENTORY_ITEMS(0x00200000),
    /**
     * Equipped-items payload changed.
     */
    EQUIPPED_ITEMS(0x00400000),
    /**
     * Active effects changed.
     */
    EFFECTS(0x00800000),
    /**
     * Protection/resistance group changed.
     */
    PROTECTIONS(0x01000000),
    /**
     * Controlled-owner stats block changed.
     */
    CONTROLLED_OWNER_STATS(0x02000000),
    /**
     * Primary attributes (body/mind/reaction/spirit) changed.
     */
    PRIMARY_ATTRIBUTES(0x20000000),
    /**
     * Visibility/show-hide state changed.
     */
    VISIBILITY_STATE(0x40000000),
    /**
     * Display name changed.
     */
    DISPLAY_NAME(0x80000000),

    /**
     * Armor equip/take-off composite used by Armor/Shield paths.
     */
    ARMOR_DEFENCE_ABSORBTION(toValue(DEFENCE, ABSORBTION)),
    /**
     * Weapon equip/take-off composite used by Weapon paths.
     */
    WEAPON_COMBAT(toValue(DEFENCE, TO_HIT, DAMAGE_PROFILE)),
    /**
     * Composite covering all five skill-bonus slots.
     */
    SKILL_BONUSES(toValue(SKILL_BONUS_1, SKILL_BONUS_2, SKILL_BONUS_3, SKILL_BONUS_4, SKILL_BONUS_5)),
    /**
     * Body-derived dirty set used by Effect::applyScaledModifier @0051D436.
     */
    BODY(toValue(PRIMARY_ATTRIBUTES, DAMAGE_PROFILE, TO_HIT, VITALS_DERIVED, HP)),
    /**
     * Mind-derived dirty set used by Effect::applyScaledModifier @0051D436.
     */
    MIND(toValue(PRIMARY_ATTRIBUTES, SIGHT_RANGE)),
    /**
     * Reaction-derived dirty set used by Effect::applyScaledModifier @0051D436.
     */
    REACTION(toValue(PRIMARY_ATTRIBUTES, SIGHT_RANGE, DAMAGE_PROFILE, TO_HIT, DEFENCE)),
    /**
     * Spirit-derived dirty set used by Effect::applyScaledModifier @0051D436.
     */
    SPIRIT(toValue(PRIMARY_ATTRIBUTES, PROTECTIONS, VITALS_DERIVED, MP)),
    /**
     * Max-HP dirty set used when health capacity changes.
     */
    MAX_HP(toValue(HP, VITALS_DERIVED)),
    /**
     * Max-MP dirty set used when mana capacity changes.
     */
    MAX_MP(toValue(MP, VITALS_DERIVED)),
    /**
     * Fighter-skill dirty set used by skill effect paths.
     */
    FIGHTER_SKILLS(toValue(SKILLS, TO_HIT, DAMAGE_PROFILE)),
    /**
     * Shared dirty set emitted for skill-level-up recalculation.
     */
    SKILL_LEVEL_UP(toValue(SKILLS, VITALS_DERIVED, TO_HIT, DAMAGE_PROFILE)),
    /**
     * Dirty set emitted while beginning the dying-state transition.
     */
    DYING_TRANSITION(toValue(HP, DEFENCE)),
    /**
     * Dirty set emitted after looting a sack into inventory.
     */
    INVENTORY_AND_ENCUMBRANCE(toValue(INVENTORY_ITEMS, SPEED, ENCUMBRANCE_WEIGHT)),
    /**
     * Dirty set emitted for controlled-owner death handling.
     */
    CONTROLLED_OWNER_DEATH(toValue(CONTROLLED_OWNER_STATS, EQUIPPED_ITEMS)),
    /**
     * Outpost release baseline sent when a unit re-enters the world.
     */
    WORLD_ENTRY_BASELINE(toValue(
            HP,
            MP,
            VITALS_DERIVED,
            POSITION_AND_FACING,
            TYPE_AND_FACE,
            OWNER_PLAYER_ID,
            SERVER_ID,
            DISPLAY_NAME
    ));

    public final int value;

    /**
     * Native support for dirty flags recovered from CServerApp::NetUpdate @00502019.
     */
    UnitDirtyFlags(int value) {
        this.value = value;
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019.
     */
    public static int toValue(Collection<UnitDirtyFlags> flags) {
        Objects.requireNonNull(flags, "flags");
        int value = 0;
        for (UnitDirtyFlags flag : flags) {
            value |= Objects.requireNonNull(flag, "flag").value;
        }
        return value;
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019.
     */
    public static int toValue(UnitDirtyFlags... flags) {
        Objects.requireNonNull(flags, "flags");
        int value = 0;
        for (UnitDirtyFlags flag : flags) {
            value |= Objects.requireNonNull(flag, "flag").value;
        }
        return value;
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019 and
     * Unit::updateSkills @005175AB.
     */
    public static UnitDirtyFlags skillBonusSlot(int skillIndex) {
        return switch (skillIndex) {
            case 1 -> SKILL_BONUS_1;
            case 2 -> SKILL_BONUS_2;
            case 3 -> SKILL_BONUS_3;
            case 4 -> SKILL_BONUS_4;
            case 5 -> SKILL_BONUS_5;
            default -> throw new IllegalArgumentException("Unsupported skill bonus slot: " + skillIndex);
        };
    }
}
