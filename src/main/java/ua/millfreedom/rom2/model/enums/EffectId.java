package ua.millfreedom.rom2.model.enums;

import java.util.HashMap;
import java.util.Map;

public enum EffectId {
    EMPTY(0, ""),
    PRICE(1, "price"),
    BODY(2, "body"),
    MIND(3, "mind"),
    REACTION(4, "reaction"),
    SPIRIT(5, "spirit"),
    HEALTH(6, "health"),
    HEALTH_MAX(7, "healthMax"),
    HEALTH_REGENERATION(8, "healthRegeneration"),
    MANA(9, "mana"),
    MANA_MAX(10, "manaMax"),
    MANA_REGENERATION(11, "manaRegeneration"),
    TO_HIT(12, "toHit"),
    DAMAGE_MIN(13, "damageMin"),
    DAMAGE_MAX(14, "damageMax"),
    DEFENCE(15, "defence"),
    ABSORBTION(16, "absorbtion"),
    SPEED(17, "speed"),
    ROTATION_SPEED(18, "rotationSpeed"),
    SCAN_RANGE(19, "scanRange"),
    PROTECTION_0(20, "protection0"),
    PROTECTION_FIRE(21, "protectionFire"),
    PROTECTION_WATER(22, "protectionWater"),
    PROTECTION_AIR(23, "protectionAir"),
    PROTECTION_EARTH(24, "protectionEarth"),
    PROTECTION_ASTRAL(25, "protectionAstral"),
    FIGHTER_SKILL_0(26, "fighterSkill0"),
    SKILL_BLADE(27, "skillBlade"),
    SKILL_AXE(28, "skillAxe"),
    SKILL_BLUDGEON(29, "skillBludgeon"),
    SKILL_PIKE(30, "skillPike"),
    SKILL_SHOOTING(31, "skillShooting"),
    MAGE_SKILL_0(32, "mageSkill0"),
    SKILL_FIRE(33, "skillFire"),
    SKILL_WATER(34, "skillWater"),
    SKILL_AIR(35, "skillAir"),
    SKILL_EARTH(36, "skillEarth"),
    SKILL_ASTRAL(37, "skillAstral"),
    ITEM_LORE(38, "itemLore"),
    MAGIC_LORE(39, "magicLore"),
    CREATURE_LORE(40, "creatureLore"),
    CAST_SPELL(41, "castSpell"),
    TEACH_SPELL(42, "teachSpell"),
    DAMAGE(43, "damage"),
    DAMAGE_FIRE(44, "damageFire"),
    DAMAGE_WATER(45, "damageWater"),
    DAMAGE_AIR(46, "damageAir"),
    DAMAGE_EARTH(47, "damageEarth"),
    DAMAGE_ASTRAL(48, "damageAstral"),
    DAMAGE_BONUS(49, "damageBonus"),
    UNKNOWN(-1, "unknown");

    private static final EffectId[] BY_ID = createLookup();
    private static final Map<String, EffectId> BY_TABLE_NAME = createTableNameLookup();

    public final int id;
    public final String tableName;

    EffectId(int id, String tableName) {
        this.id = id;
        this.tableName = tableName;
    }

    public static EffectId fromId(int id) {
        if (id < 0 || id >= BY_ID.length) {
            return UNKNOWN;
        }
        EffectId effectId = BY_ID[id];
        return effectId == null ? UNKNOWN : effectId;
    }

    public static EffectId fromTableName(String tableName) {
        if (tableName == null || tableName.isBlank()) {
            return UNKNOWN;
        }
        EffectId effectId = BY_TABLE_NAME.get(normalizeTableName(tableName));
        return effectId == null ? UNKNOWN : effectId;
    }

    public boolean isBetween(EffectId from, EffectId to) {
        return id >= from.id && id <= to.id;
    }

    private static EffectId[] createLookup() {
        int maxId = DAMAGE_BONUS.id;
        EffectId[] values = new EffectId[maxId + 1];
        for (EffectId effectId : EffectId.values()) {
            if (effectId.id >= 0) {
                values[effectId.id] = effectId;
            }
        }
        return values;
    }

    private static Map<String, EffectId> createTableNameLookup() {
        Map<String, EffectId> lookup = new HashMap<>();
        for (EffectId effectId : EffectId.values()) {
            if (effectId == UNKNOWN) {
                continue;
            }
            lookup.put(normalizeTableName(effectId.tableName), effectId);
        }
        return lookup;
    }

    private static String normalizeTableName(String value) {
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char ch = Character.toLowerCase(value.charAt(i));
            if ((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')) {
                out.append(ch);
            }
        }
        return out.toString();
    }
}
