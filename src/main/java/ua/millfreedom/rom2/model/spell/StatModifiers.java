package ua.millfreedom.rom2.model.spell;

public final class StatModifiers {

    //0x000
    private final StatsFromEffects minDamage = new SumStatsFromEffects();
    //0x060
    private final StatsFromEffects maxDamage = new SumStatsFromEffects();
    //0x0c0
    private final StatsFromEffects minRange = new MinStatsFromEffects();
    //0x120
    private final StatsFromEffects maxRange = new UMaxStatsFromEffects();
    //0x180
    private final StatsFromEffects manaCost = new StatsFromEffects();
    //0x1e0
    private final StatsFromEffects manaCostMirror = new StatsFromEffects();
    //0x240
    private final StatsFromEffects minDuration = new MinStatsFromEffects();
    //0x2a0
    private final StatsFromEffects maxDuration = new UMaxStatsFromEffects();
    //0x300
    private final StatsFromEffects minSpeed = new MinStatsFromEffects();
    //0x360
    private final StatsFromEffects maxSpeed = new MaxStatsFromEffects();
    //0x3c0
    private final StatsFromEffects minResistance = new MinStatsFromEffects();
    //0x420
    private final StatsFromEffects maxResistance = new UMaxStatsFromEffects();
    //0x480
    private final StatsFromEffects minSight = new MinStatsFromEffects();
    //0x4e0
    private final StatsFromEffects maxSight = new MaxStatsFromEffects();
    //0x540
    private final StatsFromEffects minMaximumDamageProbability = new MinStatsFromEffects();
    //0x5a0
    private final StatsFromEffects maxMaximumDamageProbability = new UMaxStatsFromEffects();
    //0x600
    private final StatsFromEffects minMinimumDamageProbability = new MinStatsFromEffects();
    //0x660
    private final StatsFromEffects maxMinimumDamageProbability = new UMaxStatsFromEffects();
    //0x6c0
    private final StatsFromEffects minRays = new MinStatsFromEffects();
    //0x720
    private final StatsFromEffects maxRays = new UMaxStatsFromEffects();
    //0x780
    private final StatsFromEffects minAbsorption = new MinStatsFromEffects();
    //0x7e0
    private final StatsFromEffects maxAbsorption = new UMaxStatsFromEffects();

    // not ported.
    public StatsFromEffects minDamage() {
        return minDamage;
    }

    // not ported.
    public StatsFromEffects maxDamage() {
        return maxDamage;
    }

    // not ported.
    public StatsFromEffects minRange() {
        return minRange;
    }

    // not ported.
    public StatsFromEffects maxRange() {
        return maxRange;
    }

    // not ported.
    public StatsFromEffects manaCost() {
        return manaCost;
    }

    // not ported.
    public StatsFromEffects manaCostMirror() {
        return manaCostMirror;
    }

    // not ported.
    public StatsFromEffects minDuration() {
        return minDuration;
    }

    // not ported.
    public StatsFromEffects maxDuration() {
        return maxDuration;
    }

    // not ported.
    public StatsFromEffects minSpeed() {
        return minSpeed;
    }

    // not ported.
    public StatsFromEffects maxSpeed() {
        return maxSpeed;
    }

    // not ported.
    public StatsFromEffects minResistance() {
        return minResistance;
    }

    // not ported.
    public StatsFromEffects maxResistance() {
        return maxResistance;
    }

    // not ported.
    public StatsFromEffects minSight() {
        return minSight;
    }

    // not ported.
    public StatsFromEffects maxSight() {
        return maxSight;
    }

    // not ported.
    public StatsFromEffects minMaximumDamageProbability() {
        return minMaximumDamageProbability;
    }

    // not ported.
    public StatsFromEffects maxMaximumDamageProbability() {
        return maxMaximumDamageProbability;
    }

    // not ported.
    public StatsFromEffects minMinimumDamageProbability() {
        return minMinimumDamageProbability;
    }

    // not ported.
    public StatsFromEffects maxMinimumDamageProbability() {
        return maxMinimumDamageProbability;
    }

    // not ported.
    public StatsFromEffects minRays() {
        return minRays;
    }

    // not ported.
    public StatsFromEffects maxRays() {
        return maxRays;
    }

    // not ported.
    public StatsFromEffects minAbsorption() {
        return minAbsorption;
    }

    // not ported.
    public StatsFromEffects maxAbsorption() {
        return maxAbsorption;
    }

    // not ported.
    public void resetDefaults() {
        minDamage.reset();
        maxDamage.reset();
        minRange.reset();
        maxRange.reset();
        manaCost.reset();
        manaCostMirror.reset();
        minDuration.reset();
        maxDuration.reset();
        minSpeed.reset();
        maxSpeed.reset();
        minResistance.reset();
        maxResistance.reset();
        minSight.reset();
        maxSight.reset();
        minMaximumDamageProbability.reset();
        maxMaximumDamageProbability.reset();
        minMinimumDamageProbability.reset();
        maxMinimumDamageProbability.reset();
        minRays.reset();
        maxRays.reset();
        minAbsorption.reset();
        maxAbsorption.reset();
    }
}
