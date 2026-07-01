package ua.millfreedom.rom2.model.spell;

public class MaxStatsFromEffects extends StatsFromEffects {

    @Override
    // not ported.
    public void set(int index, int value) {
        super.set(index, Math.max(get(index), value));
    }

    @Override
    // not ported.
    public void reset() {
        fill(Integer.MIN_VALUE);
    }
}
