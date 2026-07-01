package ua.millfreedom.rom2.model.sound;

/**
 * Java-only bridge between native map sound calculations and OpenAL positional playback.
 */
public final class SoundPlacement {
    // Java-only listener position, usually the map camera center.
    public final SoundPosition listenerPosition;
    // Java-only source position.
    public final SoundPosition sourcePosition;
    // Native DirectSound-compatible volume delta retained for priority/debug parity.
    public final int nativeVolumeDelta;
    // Java-only source distance from the listener in OpenAL units.
    public final float distance;

    // not ported.
    public SoundPlacement(
            SoundPosition listenerPosition,
            SoundPosition sourcePosition,
            int nativeVolumeDelta,
            float distance
    ) {
        this.listenerPosition = listenerPosition;
        this.sourcePosition = sourcePosition;
        this.nativeVolumeDelta = nativeVolumeDelta;
        this.distance = distance;
    }
}
