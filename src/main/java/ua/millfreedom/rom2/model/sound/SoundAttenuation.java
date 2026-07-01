package ua.millfreedom.rom2.model.sound;

/**
 * Java-only distance-gain policy for positional sound playback.
 */
public final class SoundAttenuation {
    public static final SoundAttenuation NONE = new SoundAttenuation(0.0f, 0.0f);
    public static final SoundAttenuation NATIVE_MAP_EXPONENTIAL = new SoundAttenuation(0.0f, 1.0f);

    // Java-only gain floor after distance attenuation.
    public final float minimumGain;
    // Java-only multiplier applied to OpenAL-unit distance before the native exponential curve.
    public final float curveScale;

    // not ported.
    public SoundAttenuation(float minimumGain, float curveScale) {
        this.minimumGain = minimumGain;
        this.curveScale = curveScale;
    }

    // not ported.
    public float gainForDistance(float distance) {
        if (curveScale == 0.0f) {
            return 1.0f;
        }

        double curveDistance = Math.max(0.0f, distance) * curveScale;
        double volume = -(Math.exp(curveDistance) - 1.0d) * 100.0d;
        if (volume <= -10000.0d) {
            return minimumGain;
        }

        float gain = (float) ((volume + 10000.0d) / 10000.0d);
        if (gain < minimumGain) {
            return minimumGain;
        }
        if (gain > 1.0f) {
            return 1.0f;
        }
        return gain;
    }
}
