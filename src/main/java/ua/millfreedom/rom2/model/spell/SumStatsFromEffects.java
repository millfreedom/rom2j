package ua.millfreedom.rom2.model.spell;

public class SumStatsFromEffects extends StatsFromEffects {

    @Override
    // not ported.
    public void set(int index, int value) {
        super.set(index, get(index) + value);
    }
}
