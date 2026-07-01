package ua.millfreedom.rom2.model.sound;

/**
 * Java-only OpenAL position value used by the sound backend.
 */
public final class SoundPosition {
    public static final SoundPosition ORIGIN = new SoundPosition(0.0f, 0.0f, 0.0f);

    // Java-only OpenAL X coordinate.
    public final float x;
    // Java-only OpenAL Y coordinate.
    public final float y;
    // Java-only OpenAL Z coordinate.
    public final float z;

    // not ported.
    public SoundPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // not ported.
    public float distanceTo(SoundPosition other) {
        float dx = x - other.x;
        float dy = y - other.y;
        float dz = z - other.z;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
