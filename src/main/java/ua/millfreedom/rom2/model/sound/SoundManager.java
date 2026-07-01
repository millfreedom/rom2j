package ua.millfreedom.rom2.model.sound;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.SfxSounds;
import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.res.ResInHeap;

import static ua.millfreedom.rom2.res.Constants.*;

public final class SoundManager {

    private static final int DEFAULT_SFX_VOLUME = 1;
    private static final byte DEFAULT_SFX_PRIORITY = (byte) 0xDC;
    public static final char[] genders = new char[]{'m', 'f'}; // for LGBTQ supporters: add more genders here. JUST KIDDING, PLEASE - DON'T!
    // Java load-state guard for Global::LoadSounds @004C4A47 and Global::ReleaseSounds @004C4F1F.
    private static boolean loaded;

    public static final CustomList<Sound> SFX_SOUNDS = new CustomList<>(Sound.class);
    public static final SoundPack[] FIGHTER_PACKS = new SoundPack[2];
    public static final SoundPack[] MAGE_PACKS = new SoundPack[2];
    public static final SoundPack[] PEASANT_PACKS = new SoundPack[2];
    public static SoundPack ALDOR;
    public static SoundPack SAGITA;
    public static SoundPack HILDARIUS;
    public static SoundPack GALINEL;
    public static SoundPack IGLES;
    public static SoundPack DINA;
    public static SoundPack XBOW;
    public static SoundPack KNIGHT;
    public static SoundPack DRUID;
    public static SoundPack TROLL;
    public static SoundPack ORC;
    public static SoundPack MAGE;

    static {
        ensureLoaded();
    }

    /**
     * Java utility constructor.
     * not ported.
     */
    private SoundManager() {
    }

    /**
     * Native support for startup paths that call Global::LoadSounds @004C4A47.
     */
    public static void ensureLoaded() {
        if (loaded) {
            return;
        }
        try {
            loadSounds();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Native: Global::LoadSounds @004C4A47.
     * Fully ported at the Java sound backend boundary.
     */
    public static void loadSounds() throws Exception {
        ResInHeap sfxReg = ResInHeap.load(SFX_REG);
        int sfxCount = sfxReg.getInt(GLOBAL, SFX_COUNT, 0);

        SFX_SOUNDS.clear();
        for (int i = 0; i <= sfxCount; i++) {
            SFX_SOUNDS.add(null);
        }

        for (int i = 1; i <= sfxCount; i++) {
            String key = String.format(SFX_ENTRY_N, i);
            StringBuilder value = new StringBuilder(0x100);
            sfxReg.getValueAsString(SFX, key, "", value, 0x100);
            if (value.isEmpty()) {
                continue;
            }
            String path = String.format(SFX_S_WAV, value);
            SFX_SOUNDS.set(i, new Sound(path));
        }

        for (int i = 0; i < genders.length; i++) {
            FIGHTER_PACKS[i] = new SoundPack(genders[i] + "_fighter");
            MAGE_PACKS[i] = new SoundPack(genders[i] + "_mage");
            PEASANT_PACKS[i] = new SoundPack(genders[i] + "_peasant");
        }

        ALDOR = new SoundPack("aldor");
        SAGITA = new SoundPack("sagita");
        HILDARIUS = new SoundPack("hildarius");
        GALINEL = new SoundPack("galinel");
        IGLES = new SoundPack("igles");
        DINA = new SoundPack("dina");
        XBOW = new SoundPack("xbow");
        KNIGHT = new SoundPack("knight");
        DRUID = new SoundPack("druid");
        TROLL = new SoundPack("troll");
        ORC = new SoundPack("orc");
        MAGE = new SoundPack("mage");
        loaded = true;
    }

    /**
     * Native: Global::ReleaseSounds @004C4F1F.
     * Fully ported at the Java sound backend boundary.
     */
    public static void releaseSounds() {
        if (!loaded) {
            return;
        }

        for (int i = 0; i < SFX_SOUNDS.size(); i++) {
            releaseSound(SFX_SOUNDS.get(i));
        }
        SFX_SOUNDS.clear();

        for (int i = 0; i < 2; i++) {
            releaseSoundPack(FIGHTER_PACKS[i]);
            FIGHTER_PACKS[i] = null;
            releaseSoundPack(MAGE_PACKS[i]);
            MAGE_PACKS[i] = null;
            releaseSoundPack(PEASANT_PACKS[i]);
            PEASANT_PACKS[i] = null;
        }

        releaseSoundPack(ALDOR);
        ALDOR = null;
        releaseSoundPack(SAGITA);
        SAGITA = null;
        releaseSoundPack(HILDARIUS);
        HILDARIUS = null;
        releaseSoundPack(GALINEL);
        GALINEL = null;
        releaseSoundPack(IGLES);
        IGLES = null;
        releaseSoundPack(DINA);
        DINA = null;
        releaseSoundPack(XBOW);
        XBOW = null;
        releaseSoundPack(KNIGHT);
        KNIGHT = null;
        releaseSoundPack(DRUID);
        DRUID = null;
        releaseSoundPack(TROLL);
        TROLL = null;
        releaseSoundPack(ORC);
        ORC = null;
        releaseSoundPack(MAGE);
        MAGE = null;
        loaded = false;
    }

    /**
     * Native support extracted from Global::ReleaseSounds @004C4F1F SFX pointer deletion loop.
     */
    private static void releaseSound(Sound sound) {
        if (sound != null) {
            sound.release();
        }
    }

    /**
     * Native support extracted from Global::ReleaseSounds @004C4F1F SoundPack release loops.
     */
    private static void releaseSoundPack(SoundPack soundPack) {
        soundPack.releaseSounds();
    }

    /**
     * Native owner: CArray&lt;Sound&gt;::GetAt + Sound::Play .
     * not ported.
     */
    public static void playSfx(SfxSounds soundEffect) {
        Sound sound = SFX_SOUNDS.get(soundEffect.id);
        if (sound != null) {
            sound.play(Globals.soundPreferences.sfxVolume, false, DEFAULT_SFX_PRIORITY, 0);
        }
    }
}
