package ua.millfreedom.rom2.model.spell;

import java.util.Arrays;

public class StatsFromEffects {
    public static final int EFFECT_COUNT = 24;

    private final int[] fromEffect;

    // not ported.
    public StatsFromEffects() {
        this.fromEffect = new int[EFFECT_COUNT];
        reset();
    }

    // not ported.
    public StatsFromEffects(int[] fromEffect) {
        if (fromEffect.length != EFFECT_COUNT) {
            throw new IllegalArgumentException("Expected " + EFFECT_COUNT + " values, got " + fromEffect.length);
        }
        this.fromEffect = Arrays.copyOf(fromEffect, EFFECT_COUNT);
    }

    // not ported.
    public int get(int index) {
        return fromEffect[index];
    }

    // not ported.
    public void set(int index, int value) {
        fromEffect[index] = value;
    }

    // not ported.
    public void reset() {
        fill(0);
    }

    // not ported.
    public int[] toArray() {
        return Arrays.copyOf(fromEffect, fromEffect.length);
    }

    // not ported.
    public void fill(int value) {
        Arrays.fill(fromEffect, value);
    }
}
