package ua.millfreedom.rom2.model.sound;

import java.util.ArrayList;
import java.util.List;

/**
 * Native global struct: SoundPreferences (`g_SoundPreferences @00622688`).
 * Purpose: persisted sound/music option values shared across UI and runtime audio code.
 */
public class SoundPreferences {
    public static final int NATIVE_SIZE = 0x28; // VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!
    public static final int VOLUME_MIN = 0;
    public static final int VOLUME_MAX = 100;
    public static final int DEFAULT_VOLUME = 93; // Native default -700 converted from signed DirectSound volume to normalized 0..100 volume.

    //0x0
    public int soundRandom;
    //0x4 native CStringArray *names_; Java owns the array directly.
    public final List<String> names = new ArrayList<>();
    //0x8
    public int musicVolume;
    //0xc
    public int musicVolumeMax;
    //0x10
    public int sfxVolume;
    //0x14
    public int sfxVolumeMax;
    //0x18
    public int speechVolume;
    //0x1c
    public int speechVolumeMax;
    //0x20
    public int musicAvailable;
    //0x24
    public int musicEnabled;

    /**
     * Native: SoundPreferences::SoundPreferences @00476130.
     * Java-normalized for 0..100 volume values. Native allocates the names_ CStringArray; Java owns that list through the field initializer.
     * Native cleanup @004761F0 only deletes names_, so Java has no explicit destructor body.
     */
    public SoundPreferences() {
        musicVolume = DEFAULT_VOLUME;
        musicVolumeMax = VOLUME_MAX;
        sfxVolume = DEFAULT_VOLUME;
        sfxVolumeMax = VOLUME_MAX;
        speechVolume = DEFAULT_VOLUME;
        speechVolumeMax = VOLUME_MAX;
        musicAvailable = 1;
        musicEnabled = 1;
    }

    /**
     * Java-normalized support for volume values that used to be native DirectSound signed-volume values.
     * not ported.
     */
    public static int clampVolume(int volume) {
        if (volume < VOLUME_MIN) {
            return VOLUME_MIN;
        }
        if (volume > VOLUME_MAX) {
            return VOLUME_MAX;
        }
        return volume;
    }
}
