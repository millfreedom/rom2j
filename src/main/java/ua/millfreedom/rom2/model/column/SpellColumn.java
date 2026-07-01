package ua.millfreedom.rom2.model.column;

import lombok.Getter;

public enum SpellColumn {
    COMPLICATION_LEVEL(0),
    MANA_COST(1),
    SPHERE(2),
    ITEM(3),
    SPELL_TARGET(4),
    DELIVERY_SYSTEM(5),
    MAX_RANGE(6),
    SPELL_EFFECT_SPEED(7),
    DISTRIBUTION_SYSTEM(8),
    RADIUS_LENGTH_HALF(9),
    AREA_EFFECT_AFFECT(10),
    AREA_EFFECT_DURATION(11),
    AREA_EFFECT_FREQUENCY(12),
    APPLY_ON_UNIT_METHOD(13),
    SPELL_DURATION(14),
    SPELL_FREQUENCY(15),
    DAMAGE_MIN(16),
    DAMAGE_MAX(17),
    DEFENSIVE(18),
    SKILL_OFFSET(19),
    SCROLL_COST(20),
    BOOK_COST(21);

    public final int index;

    SpellColumn(int index) {
        this.index = index;
    }

    public static SpellColumn from(int index){
        for (var column : SpellColumn.values()) {
            if (column.index == index){
                return column;
            }
        }
        throw new RuntimeException("no such column");
    }
}
