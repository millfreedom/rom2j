package ua.millfreedom.rom2.model;

public final class EffectType {
    public static final int PERMANENT = 0x00;
    public static final int DURATION = 0x01;
    public static final int CONTINUOUS = 0x02;
    public static final int CHARGES = 0x04;
    public static final int SINGLE_USE = 0x08;
    public static final int EXPIRED = 0x80;

    // not ported.
    private EffectType() {
    }
}
