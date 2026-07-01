package ua.millfreedom.rom2.model.spell;

import static ua.millfreedom.rom2.model.enums.SpellId.*;

public final class SpellIndex {
    // index -> id
    private static final int[] MAP = {
            FIRE_ARROW.id, FIRE_BALL.id, WALL_OF_FIRE.id, PROTECTION_FROM_FIRE.id, HEAL.id, BLESS.id,
            HASTE.id, DRAIN_LIFE.id, PROTECTION_FROM_AIR.id, INVISIBILITY.id, PRISMATIC_SPRAY.id, LIGHTNING.id,
            ICE_MISSILE.id, POISON_CLOUD.id, BLIZZARD.id, PROTECTION_FROM_WATER.id, SUMMON.id, CONTROL_SPIRIT.id,
            TELEPORT.id, SHIELD.id, PROTECTION_FROM_EARTH.id, STONE_CURSE.id, WALL_OF_EARTH.id, DIAMOND_DUST.id
    };

    // not ported.
    private SpellIndex() {
    }

    // not ported.
    public static int toId(int index) {
        return MAP[index];
    }
}
