package ua.millfreedom.rom2.model.spell;

public class MinStatsFromEffects extends StatsFromEffects {

    @Override
    // not ported.
    public void set(int index, int value) {
        super.set(index, Math.min(get(index), value));
    }

    @Override
    // not ported.
    public void reset() {
        fill(Integer.MAX_VALUE);
    }
}
