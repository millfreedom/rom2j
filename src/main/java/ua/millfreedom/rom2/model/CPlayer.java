package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.CString;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class CPlayer implements MfcSerializable {
    public static final int NATIVE_SIZE = 0x4C;
    public static final int ENEMY_MASK = 0x01;
    public static final int ALLIED_MASK = 0x02;
    public static final int SILENT_DIPLOMACY_MASK = 0x04;
    public static final int MAP_VISIBLE_MASK = 0x08;
    public static final int DIPLOMACY_VISIBLE_MASK = 0x10;
    private static final int DIPLOMACY_WORD_COUNT = 0x20;
    private static final int SCENARIO_DIPLOMACY_WORD_COUNT = 0x10;
    private static final String DEFAULT_NAME = "NoName";


    //0x04
    public int playerId;

    //0x08
    public int networkSlotId;

    //0x0c
    public int color;

    //0x10
    public int gold;

    //0x14
    public final CString name = new CString(0x20);

    //0x34
    public int flags;

    //0x38
    public short[] diplomacyFlags = new short[DIPLOMACY_WORD_COUNT];

    /**
     * Native: CPlayer::CPlayer @0043B6E0.
     * Fully ported.
     */
    public CPlayer() {
        gold = 0;
        flags = 0;
    }

    /**
     * Native: CPlayer::CPlayer @0043B75E.
     * Fully ported.
     */
    public CPlayer(CPlayer from) {
        this();
        playerId = from.playerId;
        networkSlotId = from.networkSlotId;
        color = from.color;
        gold = from.gold;
        flags = from.flags;
        name.set(from.name.toString());
        diplomacyFlags = from.diplomacyFlags.clone();
    }

    /**
     * Native: CPlayer::CPlayer @0043B81F.
     * Fully ported.
     */
    public CPlayer(int playerId, int color) {
        this();
        this.playerId = playerId;
        this.color = color;
        name.set(DEFAULT_NAME);
    }

    /**
     * vtbl +0x08: CObject::Serialize @00401970.
     */
    @Override
    public void serialize(CArchive ar) {
        // Native CObject::Serialize is a no-op.
    }

    /**
     * Native support extracted from ScenarioDescriptor::ScenarioDescriptor @00534AD4 players section.
     * Fully ported.
     */
    public static CPlayer read(ByteBuffer section, int playerIndex) {
        CPlayer player = new CPlayer();
        player.color = section.getInt();
        player.flags = section.getInt();
        player.gold = section.getInt();
        player.name.read(section);
        player.playerId = playerIndex;
        for (int i = 0; i < SCENARIO_DIPLOMACY_WORD_COUNT; i++) {
            player.diplomacyFlags[i] = section.getShort();
        }
        return player;
    }

    /**
     * vtbl +0x10: CPlayer::Dump @0043B8C5.
     * Fully ported.
     */
    public String dump() {
        return "CPlayer";
    }

    /**
     * Native support extracted from CPlayer::isSilentDiplomacy @0041E800, CPlayer::isEnemy @0041E830, and
     * CPlayer::isMapVisible @0041E860.
     * Fully ported support helper.
     */
    public boolean hasDiplomacyFlag(int playerId, int relationMask) {
        return (diplomacyFlags[playerId] & relationMask) != 0;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @004156C5 CWordArray::SetAtGrow diplomacy writes.
     */
    public void setDiplomacyFlagAtGrow(int playerId, int relationFlags) {
        if (playerId >= diplomacyFlags.length) {
            diplomacyFlags = Arrays.copyOf(diplomacyFlags, playerId + 1);
        }
        diplomacyFlags[playerId] = (short) relationFlags;
    }

    /**
     * Native: CPlayer::isSilentDiplomacy @0041E800.
     * Fully ported.
     */
    public boolean isSilentDiplomacy(int playerId) {
        return hasDiplomacyFlag(playerId, SILENT_DIPLOMACY_MASK);
    }

    /**
     * Native: CPlayer::isEnemy @0041E830.
     * Fully ported.
     */
    public boolean isEnemy(int playerId) {
        return hasDiplomacyFlag(playerId, ENEMY_MASK);
    }

    /**
     * Native: CPlayer::isMapVisible @0041E860.
     * Fully ported.
     */
    public boolean isMapVisible(int playerId) {
        return hasDiplomacyFlag(playerId, MAP_VISIBLE_MASK);
    }
}
