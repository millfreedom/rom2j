package ua.millfreedom.rom2.model.sound;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.res.Constants;

import java.util.Objects;

public final class SoundPack {
    //0x04
    public final Sound[] select = new Sound[4];
    //0x14
    public final Sound[] attack = new Sound[4];
    //0x24
    public final Sound[] move = new Sound[4];
    //0x34
    public final Sound[] swarm = new Sound[4];

    //0x44
    public final Sound retreat;
    //0x48
    public final Sound pickup;
    //0x4c
    public final Sound defend;
    //0x50
    public final Sound easy;
    //0x54
    public final Sound hard;
    //0x58
    public final Sound dead;

    //0x5c
    public final int selectCount;
    //0x60
    public final int attackCount;
    //0x64
    public final int moveCount;
    //0x68
    public final int swarmCount;

    /**
     * Native: SoundPack::SoundPack @004C5038.
     * Full port. Native owns four fixed voice pointer arrays with count fields plus six direct Sound pointers.
     */
    public SoundPack(String characterName) {
        Objects.requireNonNull(characterName, "characterName");

        selectCount = loadVariantArray(select, Constants.SFX_CHARACTERS_S_SELECT_D_WAV, characterName);
        attackCount = loadVariantArray(attack, Constants.SFX_CHARACTERS_S_ATTACK_D_WAV, characterName);
        moveCount = loadVariantArray(move, Constants.SFX_CHARACTERS_S_MOVE_D_WAV, characterName);
        swarmCount = loadVariantArray(swarm, Constants.SFX_CHARACTERS_S_SWARM_D_WAV, characterName);

        retreat = createNumberedSound(Constants.SFX_CHARACTERS_S_RETREAT_D_WAV, characterName, 1);
        pickup = createNumberedSound(Constants.SFX_CHARACTERS_S_PICKUP_D_WAV, characterName, 1);
        defend = createNumberedSound(Constants.SFX_CHARACTERS_S_DEFEND_D_WAV, characterName, 1);
        easy = createNamedSound(Constants.SFX_CHARACTERS_S_EASY_WAV, characterName);
        hard = createNamedSound(Constants.SFX_CHARACTERS_S_HARD_WAV, characterName);
        dead = createNamedSound(Constants.SFX_CHARACTERS_S_DEAD_WAV, characterName);
    }

    /**
     * Native: SoundPack::ReleaseSounds @004C5A86.
     * Fully ported at the Java sound backend boundary.
     */
    public void releaseSounds() {
        for (int i = 0; i < 4; i++) {
            releaseSound(attack[i]);
            releaseSound(move[i]);
            releaseSound(swarm[i]);
            releaseSound(select[i]);
        }
        releaseSound(retreat);
        releaseSound(defend);
        releaseSound(pickup);
        releaseSound(easy);
        releaseSound(hard);
        releaseSound(dead);
    }

    /**
     * Native support extracted from SoundPack::ReleaseSounds @004C5A86 null-checked Sound deletion calls.
     */
    private static void releaseSound(Sound sound) {
        if (sound != null) {
            sound.release();
        }
    }

    /**
     * Native support extracted from SoundPack::SoundPack @004C5038 CGameFile existence probes.
     */
    private static boolean fileExists(String path) {
        return Globals.gameFileManager.exists(path);
    }

    /**
     * Native support extracted from SoundPack::SoundPack @004C5038 four-entry voice arrays and count fields.
     */
    private static int loadVariantArray(Sound[] sounds, String format, String characterName) {
        int count = 0;
        for (int i = 0; i < sounds.length; i++) {
            String path = String.format(format, characterName, i + 1);
            sounds[i] = new Sound(path);
            if (fileExists(path)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Native support extracted from SoundPack::SoundPack @004C5038 direct numbered Sound pointer fields.
     */
    private static Sound createNumberedSound(String format, String characterName, int index) {
        return new Sound(String.format(format, characterName, index));
    }

    /**
     * Native support extracted from SoundPack::SoundPack @004C5038 direct named Sound pointer fields.
     */
    private static Sound createNamedSound(String format, String characterName) {
        return new Sound(String.format(format, characterName));
    }
}
