package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.enums.HumanId;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.action.ItemListAction;
import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.unit.humanoid.human.Human;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.model.window.MessageSystem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ua.millfreedom.rom2.model.SkillProgression.skillLevelForBonusPermille;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_NEW_CHARACTER_120;

/**
 * Native class: CGameSession.
 */
public class CGameSession implements MfcSerializable {
    public static final int NATIVE_SIZE = 0x148; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    public static final int SESSION_TYPE_MAGE = 0x40;
    public static final int SESSION_TYPE_FEMALE = 0x80;
    private static final int SESSION_TYPE_CLASS_AND_SEX_MASK = SESSION_TYPE_MAGE | SESSION_TYPE_FEMALE;
    private static final int STARTING_SKILL_LEVEL = 0x14;
    private static final int UNIT_FLAG_SETUP_CUSTOMIZED = 0x08;
    private static final int INITIAL_CHARACTER_GOLD = 1000;
    private static final int CHARACTER_SETUP_TIMEOUT_STATUS_WORD = 0x1008;
    private static final int CHARACTER_INITIALIZED_PENDING_SAVE_FLAG = 0x01;
    private static final int SAVED_CHARACTER_PROGRESS_REFRESH_INTERVAL_MS = 15000;
    private static final int PLAYER_SLOT_SNAPSHOT_BUFFER_SIZE = 0xA00;
    private static final String CHARACTER_ROSTER_FILE_PATTERN = "*.a2c";
    private static final int CHARACTER_FILE_MAGIC = 0x04507989;
    private static final int LEGACY_CHARACTER_FILE_MAGIC = 0x68436C42;
    private static final int LEGACY_CHARACTER_FLAGS_OFFSET = 0x4C;
    private static final int LEGACY_CHARACTER_DELETE_PENDING_FLAG = 0x01;
    private static final int LEGACY_CHARACTER_DELETE_COMPLETE_FLAG = 0x04;
    private static final int SAVE_SECTION_HEADER_SIZE = 0x10;
    public static final int SAVE_SECTION_CHARACTER_HEADER_INDEX = 0;
    public static final int SAVE_SECTION_CHARACTER_STATS_INDEX = 1;
    public static final int SAVE_SECTION_KNOWLEDGE_TABLE_INDEX = 2;
    public static final int SAVE_SECTION_PLAYER_SLOTS_INDEX = 3;
    public static final int SAVE_SECTION_PRIMARY_ITEM_LIST_INDEX = 4;
    public static final int SAVE_SECTION_SECONDARY_ITEM_LIST_INDEX = 5;
    private static final int SAVE_SECTION_COUNT = 6;
    private static final int SAVE_SECTION_CHARACTER_HEADER_MARKER = 0xAAAAAAAA;
    private static final int SAVE_SECTION_CHARACTER_STATS_MARKER = 0x41392521;
    private static final int SAVE_SECTION_KNOWLEDGE_TABLE_MARKER = 0x55555555;
    private static final int SAVE_SECTION_PRIMARY_ITEM_LIST_MARKER = 0xDE0DE0DE;
    private static final int SAVE_SECTION_SECONDARY_ITEM_LIST_MARKER = 0x3A5A3A5A;
    private static final int SAVE_SECTION_PLAYER_SLOTS_MARKER = 0x40A40A40;
    private static final int SAVE_SECTION_RANDOM_SEED_MAX = 0x7FFF;
    private static final int SAVE_SECTION_RANDOM_PADDING_BYTE_MAX = 0xFF;
    private static final int CHARACTER_HEADER_SECTION_SIZE = 0x34;
    private static final int CHARACTER_STATS_SECTION_SIZE = 0x34;
    private static final int CHARACTER_HEADER_SESSION_KEY_PART1_OFFSET = 0x00;
    private static final int CHARACTER_HEADER_SESSION_KEY_PART2_OFFSET = 0x04;
    private static final int CHARACTER_HEADER_OWNER_OFFSET = 0x08;
    private static final int CHARACTER_HEADER_PLAYER_NAME_OFFSET = 0x0C;
    private static final int CHARACTER_HEADER_PLAYER_NAME_SIZE = 0x20;
    public static final int CHARACTER_HEADER_TYPE_OFFSET = 0x2C;
    public static final int CHARACTER_HEADER_FACE_OFFSET = 0x2D;
    public static final int CHARACTER_HEADER_STARTING_SKILL_OFFSET = 0x2E;
    private static final int CHARACTER_HEADER_INITIALIZED_OFFSET = 0x2F;
    private static final int CHARACTER_HEADER_CLAN_SERVER_ID_OFFSET = 0x30;
    public static final int CHARACTER_STATS_MONSTERS_KILLED_OFFSET = 0x00;
    public static final int CHARACTER_STATS_PLAYERS_KILLED_OFFSET = 0x04;
    public static final int CHARACTER_STATS_FRAGS_OFFSET = 0x08;
    public static final int CHARACTER_STATS_DEATH_COUNT_OFFSET = 0x0C;
    public static final int CHARACTER_STATS_GOLD_OFFSET = 0x10;
    public static final int CHARACTER_STATS_BODY_OFFSET = 0x14;
    public static final int CHARACTER_STATS_REACTION_OFFSET = 0x15;
    public static final int CHARACTER_STATS_MIND_OFFSET = 0x16;
    public static final int CHARACTER_STATS_SPIRIT_OFFSET = 0x17;
    public static final int CHARACTER_STATS_AVAILABLE_SPELL_MASK_OFFSET = 0x18;
    public static final int CHARACTER_STATS_AUTO_CAST_SPELL_ID_OFFSET = 0x1C;
    public static final int CHARACTER_STATS_SKILL_BONUS_OFFSET = 0x20;
    private static final int CHARACTER_OWNER_CURRENT_MACHINE_SENTINEL = 0x529C0291;
    private static final int CHARACTER_OWNER_LEGACY_CURRENT_MACHINE_SENTINEL = 0xBAADF00D;
    private static final int PLAYER_NAME_WITHOUT_CLAN_COPY_SIZE = 0x0B;
    private static final int PLAYER_NAME_WITH_CLAN_COPY_SIZE = 0x0C;
    private static final int CLAN_SERVER_ID_MAX = 0x10;
    public static final int FIRST_SKILL_INDEX = 1;
    public static final int SKILL_INDEX_LIMIT = 6;
    public static final int ITEM_LIST_SECTION_TRAILING_DATA_OFFSET = 0x09;
    public static final int SAVED_CHARACTER_ITEM_SLOT_COUNT = 12;
    private static final char CHARACTER_ROSTER_NAME_SEPARATOR = '|';
    private static final char CHARACTER_RENAME_CLAN_SEPARATOR = ':';

    //0x04
    public int field_4;

    //0x08
    public int sessionKeyPart1;
    //0x0c
    public int sessionKeyPart2;
    //0x10
    public String m_PlayerName = "Self";

    //0x30
    public int initialized;
    //0x34
    public int type;
    //0x38
    public int characterGold;
    //0x3c
    public int monstersKilled;
    //0x40
    public int playersKilled;
    //0x44
    public int fragCount;
    //0x48
    public int deathCount;
    //0x4c
    public int body;
    //0x50
    public int reaction;
    //0x54
    public int mind;
    //0x58
    public int spirit;
    //0x5c
    public int startingSkillIndex;
    //0x60
    public int face;
    //0x64
    public int characterFileOwnerId;
    //0x88
    public int clanServerId;
    //0x8c
    public final PlayerSlot[] m_PlayerSlots = createPlayerSlots();
    //0xf8
    public final CustomList<String> characterRosterNames = CustomList.std(String.class);

    //0x10c
    public int skipFormerCharacterPrompt;
    //0x110
    public int lastSavedCharacterProgressTick;
    //0x114
    public int minimumPlayerCount;
    //0x118
    public int maximumPlayerCount;
    //0x11c
    public final CustomList<String> characterRosterFilePaths = CustomList.std(String.class);

    //0x130
    public int selectedCharacterRosterFileIndex;

    //0x134
    public final CustomList<Short> wordArr = CustomList.std(Short.class);

    /**
     * Native: CGameSession::New @0048FE1C.
     * Fully ported.
     */
    public CGameSession() {
        selectedCharacterRosterFileIndex = -1;
        characterGold = 0;
        monstersKilled = 0;
        playersKilled = 0;
        deathCount = 0;
        body = 0;
        reaction = 0;
        mind = 0;
        spirit = 0;
        clanServerId = 0;
        lastSavedCharacterProgressTick = 0;
        maximumPlayerCount = 1;
        minimumPlayerCount = 1;
    }

    /**
     * vtbl +0x08: CObject::Serialize @00401970.
     */
    @Override
    public void serialize(CArchive ar) {
        // Native CObject::Serialize is a no-op.
    }

    /**
     * Native: CGameSession::refreshCharacterRosterFiles @0048FFC7.
     * Fully ported.
     */
    public void refreshCharacterRosterFiles() {
        characterRosterFilePaths.clear();
        characterRosterNames.clear();
        if (Globals.mainWindow.sessionMode != CMainWindow.SESSION_MODE_CAMPAIGN) {
            refreshSavedCharacterRosterFiles();
            selectedCharacterRosterFileIndex = 0;
            loadCharacterRosterEntry(selectedCharacterRosterFileIndex);
        }
        characterRosterNames.add(get(MAIN_NEW_CHARACTER_120));
    }

    /**
     * Native: CGameSession::deletePendingLegacyCharacterFilesAndLoadFirstEntry @004902E4.
     * Fully ported.
     */
    public void deletePendingLegacyCharacterFilesAndLoadFirstEntry() {
        if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN) {
            return;
        }

        try (DirectoryStream<Path> characterFiles = Files.newDirectoryStream(Path.of("."), CHARACTER_ROSTER_FILE_PATTERN)) {
            for (Path characterFilePath : characterFiles) {
                if (isPendingLegacyCharacterFile(characterFilePath)) {
                    try {
                        Files.deleteIfExists(characterFilePath);
                    } catch (IOException ignored) {
                        // Native DeleteFileA return value is ignored.
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to scan pending legacy character files", e);
        }

        selectedCharacterRosterFileIndex = 0;
        loadCharacterRosterEntry(selectedCharacterRosterFileIndex);
    }

    /**
     * Native support extracted from CGameSession::deletePendingLegacyCharacterFilesAndLoadFirstEntry @004902E4.
     */
    private static boolean isPendingLegacyCharacterFile(Path characterFilePath) throws IOException {
        byte[] characterFileBytes = Files.readAllBytes(characterFilePath);
        if (characterFileBytes.length < Integer.BYTES) {
            return false;
        }

        ByteBuffer file = ByteBuffer.wrap(characterFileBytes).order(ByteOrder.LITTLE_ENDIAN);
        if (file.getInt() != LEGACY_CHARACTER_FILE_MAGIC
                || characterFileBytes.length < LEGACY_CHARACTER_FLAGS_OFFSET + Integer.BYTES) {
            return false;
        }

        int flags = file.getInt(LEGACY_CHARACTER_FLAGS_OFFSET);
        return (flags & LEGACY_CHARACTER_DELETE_PENDING_FLAG) != 0
                && (flags & LEGACY_CHARACTER_DELETE_COMPLETE_FLAG) == 0;
    }

    /**
     * Native support extracted from CGameSession::RefreshCharacterRosterFiles @0048FFC7.
     */
    private void refreshSavedCharacterRosterFiles() {
        List<FileTime> characterFileTimes = new ArrayList<>();
        try (DirectoryStream<Path> characterFiles = Files.newDirectoryStream(Path.of("."), CHARACTER_ROSTER_FILE_PATTERN)) {
            for (Path characterFilePath : characterFiles) {
                String playerName = readRosterPlayerName(characterFilePath, Globals.mainWindow.connectionScratchState.acceptedCharacterFileOwnerId);
                if (playerName == null) {
                    continue;
                }
                FileTime fileTime = Files.getLastModifiedTime(characterFilePath);
                int insertIndex = 0;
                while (insertIndex < characterFileTimes.size()
                        && fileTime.compareTo(characterFileTimes.get(insertIndex)) <= 0) {
                    insertIndex++;
                }
                characterFileTimes.add(insertIndex, fileTime);
                characterRosterFilePaths.add(insertIndex, characterFilePath.getFileName().toString());
                characterRosterNames.add(insertIndex, playerName);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to scan saved character roster files", e);
        }
    }

    /**
     * Native support extracted from CGameSession::RefreshCharacterRosterFiles @0048FFC7 first `0xAAAAAAAA` section read.
     */
    private String readRosterPlayerName(Path characterFilePath, int expectedOwnerId) throws IOException {
        byte[] headerSection = readFirstSavedCharacterHeaderSection(characterFilePath);
        if (headerSection == null) {
            return null;
        }

        ByteBuffer header = ByteBuffer.wrap(headerSection).order(ByteOrder.LITTLE_ENDIAN);
        int ownerId = header.getInt(CHARACTER_HEADER_OWNER_OFFSET);
        String playerName = readNullTerminatedString(
                headerSection,
                CHARACTER_HEADER_PLAYER_NAME_OFFSET,
                CHARACTER_HEADER_PLAYER_NAME_SIZE
        );
        m_PlayerName = playerName;
        if (ownerId == CHARACTER_OWNER_CURRENT_MACHINE_SENTINEL
                || ownerId == CHARACTER_OWNER_LEGACY_CURRENT_MACHINE_SENTINEL) {
            ownerId = expectedOwnerId;
        }
        return ownerId == expectedOwnerId ? playerName : null;
    }

    /**
     * Native support extracted from CGameSession::RefreshCharacterRosterFiles @0048FFC7 first encrypted-section read.
     */
    private static byte[] readFirstSavedCharacterHeaderSection(Path characterFilePath) throws IOException {
        ByteBuffer file = ByteBuffer.wrap(Files.readAllBytes(characterFilePath)).order(ByteOrder.LITTLE_ENDIAN);
        try {
            if (file.remaining() < Integer.BYTES || file.getInt() != CHARACTER_FILE_MAGIC) {
                return null;
            }
            if (file.remaining() < SAVE_SECTION_HEADER_SIZE) {
                return null;
            }

            file.getInt();
            int sectionSize = file.getInt();
            file.getShort();
            int seed = Short.toUnsignedInt(file.getShort());
            int checksum = file.getInt();
            return readXorEncryptedSaveSection(file, sectionSize, seed, checksum);
        } catch (BufferUnderflowException e) {
            return null;
        }
    }

    /**
     * Native: Global::readEncryptedSaveSections @004EE7CB.
     * Fully ported.
     */
    public static byte[][] readSavedCharacterSections(Path characterFilePath) throws IOException {
        return readSavedCharacterSections(Files.readAllBytes(characterFilePath));
    }

    /**
     * Native: Global::readEncryptedSaveSections @004EE7CB.
     * Fully ported.
     */
    public static byte[][] readSavedCharacterSections(byte[] characterFileBytes) {
        ByteBuffer file = ByteBuffer.wrap(characterFileBytes).order(ByteOrder.LITTLE_ENDIAN);
        try {
            if (file.remaining() < Integer.BYTES || file.getInt() != CHARACTER_FILE_MAGIC) {
                return null;
            }

            byte[][] sections = new byte[SAVE_SECTION_COUNT][];
            while (file.hasRemaining()) {
                if (file.remaining() < SAVE_SECTION_HEADER_SIZE) {
                    return null;
                }

                int marker = file.getInt();
                int sectionSize = file.getInt();
                file.getShort();
                int seed = Short.toUnsignedInt(file.getShort());
                int checksum = file.getInt();
                switch (marker) {
                    case SAVE_SECTION_CHARACTER_HEADER_MARKER -> {
                        sections[SAVE_SECTION_CHARACTER_HEADER_INDEX] =
                                readXorEncryptedSaveSection(file, sectionSize, seed, checksum);
                        if (sections[SAVE_SECTION_CHARACTER_HEADER_INDEX] == null) {
                            return null;
                        }
                    }
                    case SAVE_SECTION_CHARACTER_STATS_MARKER -> {
                        sections[SAVE_SECTION_CHARACTER_STATS_INDEX] =
                                readCharacterStatsSaveSection(file, sectionSize, seed, checksum);
                        if (sections[SAVE_SECTION_CHARACTER_STATS_INDEX] == null) {
                            return null;
                        }
                    }
                    case SAVE_SECTION_PLAYER_SLOTS_MARKER -> {
                        sections[SAVE_SECTION_PLAYER_SLOTS_INDEX] =
                                readXorEncryptedSaveSection(file, sectionSize, seed, checksum);
                        if (sections[SAVE_SECTION_PLAYER_SLOTS_INDEX] == null) {
                            return null;
                        }
                    }
                    case SAVE_SECTION_KNOWLEDGE_TABLE_MARKER -> {
                        byte[] compressedKnowledgeTable = readXorEncryptedSaveSection(file, sectionSize, seed, checksum);
                        if (compressedKnowledgeTable == null) {
                            return null;
                        }
                        sections[SAVE_SECTION_KNOWLEDGE_TABLE_INDEX] =
                                MapVisualObject.decompressKnowledgeTable(compressedKnowledgeTable);
                    }
                    case SAVE_SECTION_PRIMARY_ITEM_LIST_MARKER -> {
                        sections[SAVE_SECTION_PRIMARY_ITEM_LIST_INDEX] =
                                readXorEncryptedSaveSection(file, sectionSize, seed, checksum);
                        if (sections[SAVE_SECTION_PRIMARY_ITEM_LIST_INDEX] == null) {
                            return null;
                        }
                    }
                    case SAVE_SECTION_SECONDARY_ITEM_LIST_MARKER -> {
                        sections[SAVE_SECTION_SECONDARY_ITEM_LIST_INDEX] =
                                readXorEncryptedSaveSection(file, sectionSize, seed, checksum);
                        if (sections[SAVE_SECTION_SECONDARY_ITEM_LIST_INDEX] == null) {
                            return null;
                        }
                    }
                    default -> {
                        return null;
                    }
                }
            }
            return sections;
        } catch (BufferUnderflowException e) {
            return null;
        }
    }

    /**
     * Fully ported native support extracted from Global::readEncryptedSaveSections @004EE7CB standard XOR-encrypted
     * section branches.
     */
    private static byte[] readXorEncryptedSaveSection(ByteBuffer file, int sectionSize, int seed, int checksum) {
        if (sectionSize < 0 || file.remaining() < sectionSize) {
            return null;
        }

        byte[] sectionBytes = new byte[sectionSize];
        file.get(sectionBytes);
        xorEncryptedSaveSection(sectionBytes, seed);
        if (calculateEncryptedSectionChecksum(sectionBytes) != checksum) {
            return null;
        }
        return sectionBytes;
    }

    /**
     * Fully ported native support extracted from Global::readEncryptedSaveSections @004EE7CB `0x41392521` section
     * branch.
     */
    private static byte[] readCharacterStatsSaveSection(ByteBuffer file, int encodedSectionSize, int flags, int checksum) {
        if (encodedSectionSize < CHARACTER_STATS_SECTION_SIZE || file.remaining() < encodedSectionSize) {
            return null;
        }

        int encodedStart = file.position();
        byte[] sectionBytes = new byte[CHARACTER_STATS_SECTION_SIZE];
        ByteBuffer decoded = ByteBuffer.wrap(sectionBytes).order(ByteOrder.LITTLE_ENDIAN);

        skipOptionalStatsPadding(file, flags, 0x0001);
        int monstersKilled = file.getInt() ^ 0x01529251;
        decoded.putInt(CHARACTER_STATS_MONSTERS_KILLED_OFFSET, monstersKilled);

        skipOptionalStatsPadding(file, flags, 0x0002);
        int playersKilled = file.getInt() + 0x13141516 + monstersKilled * 5;
        decoded.putInt(CHARACTER_STATS_PLAYERS_KILLED_OFFSET, playersKilled);

        skipOptionalStatsPadding(file, flags, 0x0004);
        int frags = file.getInt() + 0x00ABCDEF + playersKilled * 7;
        decoded.putInt(CHARACTER_STATS_FRAGS_OFFSET, frags);

        skipOptionalStatsPadding(file, flags, 0x0008);
        int deathCount = file.getInt() ^ 0x17FF12AA;
        decoded.putInt(CHARACTER_STATS_DEATH_COUNT_OFFSET, deathCount);

        skipOptionalStatsPadding(file, flags, 0x0010);
        int gold = file.getInt() + 0xDEADBABE + monstersKilled * 3;
        decoded.putInt(CHARACTER_STATS_GOLD_OFFSET, gold);

        skipOptionalStatsPadding(file, flags, 0x0020);
        byte body = (byte) (file.get() + (byte) gold * 0x11 + (byte) monstersKilled * 0x13);
        sectionBytes[CHARACTER_STATS_BODY_OFFSET] = body;

        skipOptionalStatsPadding(file, flags, 0x0040);
        byte reaction = (byte) (file.get() + body * 0x03);
        sectionBytes[CHARACTER_STATS_REACTION_OFFSET] = reaction;

        skipOptionalStatsPadding(file, flags, 0x0080);
        byte mind = (byte) (file.get() + body + reaction * 0x05);
        sectionBytes[CHARACTER_STATS_MIND_OFFSET] = mind;

        skipOptionalStatsPadding(file, flags, 0x0100);
        byte spirit = (byte) (file.get() + body * 0x07 + mind * 0x09);
        sectionBytes[CHARACTER_STATS_SPIRIT_OFFSET] = spirit;

        skipOptionalStatsPadding(file, flags, 0x4000);
        decoded.putInt(0x18, file.getInt() + 0xEFEDE68C);

        skipOptionalStatsPadding(file, flags, 0x2000);
        decoded.putInt(0x1C, file.getInt());

        for (int skillIndex = 0; skillIndex < 5; skillIndex++) {
            skipOptionalStatsPadding(file, flags, 0x0200 << skillIndex);
            int encodedBonus = file.getInt();
            int bonus = skillIndex == 0
                    ? encodedBonus ^ 0xDADEDADE
                    : encodedBonus + decoded.getInt(0x1C + skillIndex * Integer.BYTES) * -0x771;
            decoded.putInt(CHARACTER_STATS_SKILL_BONUS_OFFSET + skillIndex * Integer.BYTES, bonus);
        }

        if (file.position() != encodedStart + encodedSectionSize
                || calculateEncryptedSectionChecksum(sectionBytes) != checksum) {
            return null;
        }
        return sectionBytes;
    }

    /**
     * Fully ported native support extracted from Global::readEncryptedSaveSections @004EE7CB `0x41392521` random
     * padding flags.
     */
    private static void skipOptionalStatsPadding(ByteBuffer file, int flags, int mask) {
        if ((flags & mask) != 0) {
            file.get();
        }
    }

    /**
     * Native support extracted from xorEncryptedSaveSection @004EDB23.
     * Fully ported.
     */
    private static void xorEncryptedSaveSection(byte[] sectionBytes, int seed) {
        int seedWord = seed & 0xFFFF;
        int rollingMask = (seedWord << 16) | seedWord;
        for (int offset = 0; offset < sectionBytes.length; offset++) {
            sectionBytes[offset] = (byte) (sectionBytes[offset] ^ (rollingMask >>> 16));
            rollingMask <<= 1;
            if ((offset & 0x0F) == 0x0F) {
                rollingMask |= seedWord;
            }
        }
    }

    /**
     * Native support extracted from calculateEncryptedSectionChecksum @004EDACD.
     * Fully ported.
     */
    private static int calculateEncryptedSectionChecksum(byte[] sectionBytes) {
        int checksum = 0;
        for (byte sectionByte : sectionBytes) {
            checksum = checksum * 2 + Byte.toUnsignedInt(sectionByte);
        }
        return checksum;
    }

    /**
     * Native support extracted from CString copy reads in CGameSession::RefreshCharacterRosterFiles @0048FFC7.
     */
    private static String readNullTerminatedString(byte[] bytes, int offset, int maxLength) {
        int length = 0;
        while (length < maxLength && bytes[offset + length] != 0) {
            length++;
        }
        return new String(bytes, offset, length, StandardCharsets.ISO_8859_1);
    }

    /**
     * Native: CGameSession::initializeNewCharacterSession @00491312.
     * Fully ported.
     */
    public void initializeNewCharacterSession(int type, String playerName) {
        CMainWindow mainWindow = Globals.mainWindow;
        characterFileOwnerId = mainWindow.connectionScratchState.acceptedCharacterFileOwnerId;
        if (playerName != null) {
            m_PlayerName = playerName;
            this.type = type;
        }
        long sessionKey = System.nanoTime();
        sessionKeyPart1 = (int) sessionKey;
        sessionKeyPart2 = (int) (sessionKey >>> 32);
        characterRosterNames.add(characterRosterNames.size() - 1, m_PlayerName);
        characterRosterFilePaths.add(formatCharacterFileName());
        selectedCharacterRosterFileIndex = characterRosterFilePaths.size() - 1;
        initialized = 1;
        Arrays.fill(mainWindow.pMapVisualObject.playerKnowledgeTable, (byte) 0);
        mainWindow.pMapVisualObject.ensureSelectedCUnit();
        maximumPlayerCount = 1;
        minimumPlayerCount = 1;
        startingSkillIndex = 0;
        refreshRuntimeUnitFromSession();
    }

    /**
     * Native: CGameSession::refreshSetupAcceptedCharacterSession @00491EC3.
     * Fully ported.
     */
    public void refreshSetupAcceptedCharacterSession() {
        body = 0;
        startingSkillIndex = 0;
        refreshRuntimeUnitFromSession();
    }

    /**
     * Native: CGameSession::applyCharacterGeneratorBuild @00491988.
     * Fully ported.
     */
    public void applyCharacterGeneratorBuild(
            int body,
            int reaction,
            int mind,
            int spirit,
            int startingSkillIndex
    ) {
        CUnit selectedUnit = Globals.mainWindow.pMapVisualObject.getSelectedCUnit();
        this.body = body;
        this.reaction = reaction;
        this.mind = mind;
        this.spirit = spirit;
        if (startingSkillIndex != 0) {
            this.startingSkillIndex = startingSkillIndex;
            selectedUnit.unitFlags |= UNIT_FLAG_SETUP_CUSTOMIZED;
        }
        refreshRuntimeUnitFromSession();
    }

    /**
     * Native: CGameSession::refreshRuntimeUnitFromSession @004919FF.
     * Fully ported. Java uses the modeled Human template/copy path; native also destroys the temporary Human after copying it.
     */
    private void refreshRuntimeUnitFromSession() {
        CUnit selectedUnit = Globals.mainWindow.pMapVisualObject.getSelectedCUnit();
        if (selectedUnit == null) {
            return;
        }

        Human runtimeUnit = createStartingRuntimeHuman();
        if (body != 0) {
            runtimeUnit.m_nBody = body;
            runtimeUnit.m_nReaction = reaction;
            runtimeUnit.m_nMind = mind;
            runtimeUnit.m_nSpirit = spirit;
            runtimeUnit.recalculateDerivedStats();
            runtimeUnit.face = face;
        }
        if (startingSkillIndex == 0) {
            startingSkillIndex = runtimeUnit.skillBonusesPermille.data[0];
        }
        runtimeUnit.configureStartingLoadout(startingSkillIndex & 0xFF, STARTING_SKILL_LEVEL);
        selectedUnit.copyFromRuntimeUnit(runtimeUnit);

        selectedUnit.copiedBody = selectedUnit.body;
        body = Byte.toUnsignedInt(selectedUnit.copiedBody);
        selectedUnit.copiedReaction = selectedUnit.reaction;
        reaction = Byte.toUnsignedInt(selectedUnit.copiedReaction);
        selectedUnit.copiedMind = selectedUnit.mind;
        mind = Byte.toUnsignedInt(selectedUnit.copiedMind);
        selectedUnit.copiedSpirit = selectedUnit.spirit;
        spirit = Byte.toUnsignedInt(selectedUnit.copiedSpirit);
        face = selectedUnit.field8_0x28;
    }

    /**
     * Native: CGameSession::getMinimumPlayerCount @0041FAA0.
     * Fully ported.
     */
    public int getMinimumPlayerCount() {
        return minimumPlayerCount;
    }

    /**
     * Native: CGameSession::getMaximumPlayerCount @0041FAC0.
     * Fully ported.
     */
    public int getMaximumPlayerCount() {
        return maximumPlayerCount;
    }

    /**
     * Native: CGameSession::submitCharacterSetupAndWaitForSelectedUnit @0049183E.
     * Fully ported.
     */
    public boolean submitCharacterSetupAndWaitForSelectedUnit() {
        Globals.multiplayerBootstrapStatusWord = 0;
        CMainWindow mainWindow = Globals.mainWindow;
        if ((initialized & 1) == 0 && mainWindow.sessionMode != CMainWindow.SESSION_MODE_CAMPAIGN) {
            String characterFilePath = characterRosterFilePaths.get(selectedCharacterRosterFileIndex);
            mainWindow.pMapVisualObject.uploadCharacterFile(characterFilePath);
        } else {
            mainWindow.pMapVisualObject.submitCharacterSetup();
        }
        if (mainWindow.sessionMode == CMainWindow.SESSION_MODE_NETWORK_HOST
                || mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN) {
            Globals.gameServer.pumpServerWorldActions();
        }
        return waitForCharacterSetupSelectedUnit(mainWindow);
    }

    /**
     * Native support extracted from the selected-unit wait loop in
     * CGameSession::submitCharacterSetupAndWaitForSelectedUnit @0049183E.
     * Fully ported.
     */
    private static boolean waitForCharacterSetupSelectedUnit(CMainWindow mainWindow) {
        int startTick = Globals.currentTickMillis();
        MapVisualObject mapVisualObject = mainWindow.pMapVisualObject;
        while (true) {
            if (mapVisualObject.getSelectedCUnit() != null) {
                return true;
            }
            while (CServerApp.getPendingSegmentMarkerCount() == 0) {
                MessageSystem.pumpPostedMessage();
                if (Integer.compareUnsigned(Globals.currentTickMillis() - startTick, Globals.networkTimeoutMillis) > 0) {
                    Globals.multiplayerBootstrapStatusWord = CHARACTER_SETUP_TIMEOUT_STATUS_WORD;
                    return false;
                }
                CServerApp.processRemoteNetworkEvents();
            }
            if (!mapVisualObject.handleGameAction(null, 100)) {
                return false;
            }
        }
    }

    /**
     * Native: CGameSession::saveSelectedCharacterFile @004915CD.
     * Fully ported.
     */
    public void saveSelectedCharacterFile() {
        CMainWindow mainWindow = Globals.mainWindow;
        if (mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN) {
            return;
        }

        MapVisualObject mapVisualObject = mainWindow.pMapVisualObject;
        CUnit selectedUnit = mapVisualObject.getSelectedCUnit();
        characterGold = INITIAL_CHARACTER_GOLD;
        monstersKilled = 0;
        playersKilled = 0;
        fragCount = 0;
        deathCount = 0;
        face = selectedUnit.field8_0x28;
        try {
            writeEncryptedSaveSections(
                    Path.of(characterRosterFilePaths.get(selectedCharacterRosterFileIndex)),
                    packSelectedCharacterHeaderForFile(m_PlayerName),
                    packSelectedCharacterStatsForFile(selectedUnit),
                    mapVisualObject.playerKnowledgeTable,
                    ItemListAction.global.packSavedCharacterItemListSection(),
                    null,
                    packPlayerSlotsForCharacterFile()
            );
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to save selected character file", e);
        }
    }

    /**
     * Native support extracted from CGameSession::saveSelectedCharacterFile @004915CD,
     * CGameSession::commitSelectedCharacterRename @00491078, and
     * CGameSession::refreshSavedCharacterProgress @00491F1C.
     */
    private byte[] packSelectedCharacterHeaderForFile(String playerName) {
        return packSavedCharacterHeaderSection(
                sessionKeyPart1,
                sessionKeyPart2,
                characterFileOwnerId,
                playerName,
                type,
                face,
                startingSkillIndex,
                initialized,
                clanServerId
        );
    }

    /**
     * Native support extracted from CGameSession::saveSelectedCharacterFile @004915CD.
     */
    private byte[] packSelectedCharacterStatsForFile(CUnit selectedUnit) {
        return packSavedCharacterStatsSection(
                monstersKilled,
                playersKilled,
                fragCount,
                deathCount,
                characterGold,
                Byte.toUnsignedInt(selectedUnit.copiedBody),
                Byte.toUnsignedInt(selectedUnit.copiedReaction),
                Byte.toUnsignedInt(selectedUnit.copiedMind),
                Byte.toUnsignedInt(selectedUnit.copiedSpirit),
                selectedUnit.spellbookMask,
                Byte.toUnsignedInt(selectedUnit.autoCastSpellId),
                selectedUnit.copiedSkillBonusesPermille
        );
    }

    /**
     * Native support extracted from CGameSession::saveSelectedCharacterFile @004915CD and
     * GameServer::Save @004E9E97 saved-character header construction.
     */
    public static byte[] packSavedCharacterHeaderSection(
            int sessionKeyPart1,
            int sessionKeyPart2,
            int owner,
            String playerName,
            int type,
            int face,
            int startingSkillIndex,
            int initialized,
            int clanServerId
    ) {
        byte[] section = new byte[CHARACTER_HEADER_SECTION_SIZE];
        ByteBuffer header = ByteBuffer.wrap(section).order(ByteOrder.LITTLE_ENDIAN);
        header.putInt(CHARACTER_HEADER_SESSION_KEY_PART1_OFFSET, sessionKeyPart1);
        header.putInt(CHARACTER_HEADER_SESSION_KEY_PART2_OFFSET, sessionKeyPart2);
        header.putInt(CHARACTER_HEADER_OWNER_OFFSET, owner);
        writeFixedNativeCString(section, CHARACTER_HEADER_PLAYER_NAME_OFFSET, CHARACTER_HEADER_PLAYER_NAME_SIZE, playerName);
        section[CHARACTER_HEADER_TYPE_OFFSET] = (byte) type;
        section[CHARACTER_HEADER_FACE_OFFSET] = (byte) face;
        section[CHARACTER_HEADER_STARTING_SKILL_OFFSET] = (byte) startingSkillIndex;
        section[CHARACTER_HEADER_INITIALIZED_OFFSET] = (byte) initialized;
        section[CHARACTER_HEADER_CLAN_SERVER_ID_OFFSET] = (byte) clanServerId;
        return section;
    }

    /**
     * Native support extracted from CGameSession::LoadCharacterRosterEntry @004904C5 header session-key read.
     */
    public static int readSavedCharacterHeaderSessionKeyPart1(byte[] headerSection) {
        return ByteBuffer.wrap(headerSection).order(ByteOrder.LITTLE_ENDIAN)
                .getInt(CHARACTER_HEADER_SESSION_KEY_PART1_OFFSET);
    }

    /**
     * Native support extracted from CGameSession::LoadCharacterRosterEntry @004904C5 header session-key read.
     */
    public static int readSavedCharacterHeaderSessionKeyPart2(byte[] headerSection) {
        return ByteBuffer.wrap(headerSection).order(ByteOrder.LITTLE_ENDIAN)
                .getInt(CHARACTER_HEADER_SESSION_KEY_PART2_OFFSET);
    }

    /**
     * Native support extracted from CGameSession::LoadCharacterRosterEntry @004904C5 header player-name read.
     */
    public static String readSavedCharacterHeaderPlayerName(byte[] headerSection) {
        return readNullTerminatedString(
                headerSection,
                CHARACTER_HEADER_PLAYER_NAME_OFFSET,
                CHARACTER_HEADER_PLAYER_NAME_SIZE
        );
    }

    /**
     * Native support extracted from CGameSession::saveSelectedCharacterFile @004915CD and
     * GameServer::Save @004E9E97 saved-character stats construction.
     */
    public static byte[] packSavedCharacterStatsSection(
            int monstersKilled,
            int playersKilled,
            int fragCount,
            int deathCount,
            int characterGold,
            int body,
            int reaction,
            int mind,
            int spirit,
            int availableSpellMask,
            int autoCastSpellId,
            int[] skillBonusesPermille
    ) {
        byte[] section = new byte[CHARACTER_STATS_SECTION_SIZE];
        ByteBuffer stats = ByteBuffer.wrap(section).order(ByteOrder.LITTLE_ENDIAN);
        stats.putInt(CHARACTER_STATS_MONSTERS_KILLED_OFFSET, monstersKilled);
        stats.putInt(CHARACTER_STATS_PLAYERS_KILLED_OFFSET, playersKilled);
        stats.putInt(CHARACTER_STATS_FRAGS_OFFSET, fragCount);
        stats.putInt(CHARACTER_STATS_DEATH_COUNT_OFFSET, deathCount);
        stats.putInt(CHARACTER_STATS_GOLD_OFFSET, characterGold);
        section[CHARACTER_STATS_BODY_OFFSET] = (byte) body;
        section[CHARACTER_STATS_REACTION_OFFSET] = (byte) reaction;
        section[CHARACTER_STATS_MIND_OFFSET] = (byte) mind;
        section[CHARACTER_STATS_SPIRIT_OFFSET] = (byte) spirit;
        stats.putInt(CHARACTER_STATS_AVAILABLE_SPELL_MASK_OFFSET, availableSpellMask);
        stats.putInt(CHARACTER_STATS_AUTO_CAST_SPELL_ID_OFFSET, autoCastSpellId);
        for (int skillIndex = 0; skillIndex < skillBonusesPermille.length; skillIndex++) {
            stats.putInt(
                    CHARACTER_STATS_SKILL_BONUS_OFFSET + skillIndex * Integer.BYTES,
                    skillBonusesPermille[skillIndex]
            );
        }
        return section;
    }

    /**
     * Native support extracted from CGameSession::saveSelectedCharacterFile @004915CD CString copy into local_34.
     */
    private static void writeFixedNativeCString(byte[] bytes, int offset, int maxLength, String value) {
        byte[] encoded = value.getBytes(StandardCharsets.ISO_8859_1);
        System.arraycopy(encoded, 0, bytes, offset, Math.min(encoded.length, maxLength - 1));
    }

    /**
     * Native support extracted from CGameSession::saveSelectedCharacterFile @004915CD.
     */
    private byte[] packPlayerSlotsForCharacterFile() {
        ByteBuffer buffer = ByteBuffer.allocate(PLAYER_SLOT_SNAPSHOT_BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN);
        for (PlayerSlot playerSlot : m_PlayerSlots) {
            playerSlot.writeToBuffer(buffer);
        }
        byte[] snapshot = new byte[buffer.position()];
        buffer.flip();
        buffer.get(snapshot);
        return snapshot;
    }

    /**
     * Native: CGameSession::refreshSavedCharacterProgressIfDue @00491EEA.
     * Fully ported.
     */
    public void refreshSavedCharacterProgressIfDue() {
        int currentTick = Globals.currentTickMillis();
        if (Integer.compareUnsigned(
                currentTick - lastSavedCharacterProgressTick,
                SAVED_CHARACTER_PROGRESS_REFRESH_INTERVAL_MS
        ) > 0) {
            refreshSavedCharacterProgress();
        }
    }

    /**
     * Native: CGameSession::refreshSavedCharacterProgress @00491F1C.
     * Fully ported.
     */
    public void refreshSavedCharacterProgress() {
        CMainWindow mainWindow = Globals.mainWindow;
        if (mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN) {
            return;
        }

        MapVisualObject mapVisualObject = mainWindow.pMapVisualObject;
        CUnit selectedUnit = mapVisualObject.getSelectedCUnit();
        if ((initialized & CHARACTER_INITIALIZED_PENDING_SAVE_FLAG) != 0) {
            initialized &= ~CHARACTER_INITIALIZED_PENDING_SAVE_FLAG;
        }
        face = selectedUnit.field8_0x28;
        try {
            writeEncryptedSaveSections(
                    Path.of(characterRosterFilePaths.get(selectedCharacterRosterFileIndex)),
                    packSelectedCharacterHeaderForFile(m_PlayerName),
                    packSelectedCharacterProgressStatsForFile(selectedUnit, mapVisualObject.currentPlayer.gold),
                    null,
                    null,
                    null,
                    null
            );
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to refresh saved character progress", e);
        }
        lastSavedCharacterProgressTick = Globals.currentTickMillis();
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2 item-list subtype `1`
     * selected-character primary item-list write branch.
     */
    public void refreshSavedCharacterPrimaryItemList(ItemListAction itemListAction) {
        try {
            writeEncryptedSaveSections(
                    Path.of(characterRosterFilePaths.get(selectedCharacterRosterFileIndex)),
                    null,
                    null,
                    null,
                    itemListAction.packSavedCharacterItemListSection(),
                    null,
                    null
            );
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to refresh saved character primary item list", e);
        }
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2 item-list subtype `2`
     * selected-character secondary item-list write branch.
     */
    public void refreshSavedCharacterSecondaryItemList(ItemListAction itemListAction) {
        try {
            writeEncryptedSaveSections(
                    Path.of(characterRosterFilePaths.get(selectedCharacterRosterFileIndex)),
                    null,
                    null,
                    null,
                    null,
                    itemListAction.packSavedCharacterItemListSection(),
                    null
            );
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to refresh saved character secondary item list", e);
        }
    }

    /**
     * Native support extracted from CGameSession::refreshSavedCharacterProgress @00491F1C.
     */
    private byte[] packSelectedCharacterProgressStatsForFile(CUnit selectedUnit, int currentGold) {
        return packSavedCharacterStatsSection(
                monstersKilled,
                playersKilled,
                fragCount,
                deathCount,
                currentGold,
                Byte.toUnsignedInt(selectedUnit.copiedBody),
                Byte.toUnsignedInt(selectedUnit.copiedReaction),
                Byte.toUnsignedInt(selectedUnit.copiedMind),
                Byte.toUnsignedInt(selectedUnit.copiedSpirit),
                selectedUnit.spellbookMask,
                Byte.toUnsignedInt(selectedUnit.autoCastSpellId),
                selectedUnit.copiedSkillBonusesPermille
        );
    }

    /**
     * Native: CGameSession::refreshSavedPlayerSlots @004920EE.
     * Fully ported.
     */
    public void refreshSavedPlayerSlots() {
        CMainWindow mainWindow = Globals.mainWindow;
        if (mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN) {
            return;
        }

        mainWindow.pMapVisualObject.getSelectedCUnit();
        try {
            writeEncryptedSaveSections(
                    Path.of(characterRosterFilePaths.get(selectedCharacterRosterFileIndex)),
                    null,
                    null,
                    null,
                    null,
                    null,
                    packPlayerSlotsForCharacterFile()
            );
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to refresh saved player slots", e);
        }
    }

    /**
     * Native: CGameSession::refreshSavedPlayerKnowledgeTable @004921BE.
     * Fully ported.
     */
    public void refreshSavedPlayerKnowledgeTable() {
        CMainWindow mainWindow = Globals.mainWindow;
        if (mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN) {
            return;
        }

        MapVisualObject mapVisualObject = mainWindow.pMapVisualObject;
        mapVisualObject.getSelectedCUnit();
        try {
            writeEncryptedSaveSections(
                    Path.of(characterRosterFilePaths.get(selectedCharacterRosterFileIndex)),
                    null,
                    null,
                    mapVisualObject.playerKnowledgeTable,
                    null,
                    null,
                    null
            );
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to refresh saved player knowledge table", e);
        }
    }

    /**
     * Native: CGameSession::commitSelectedCharacterRename @00491078.
     * Fully ported.
     */
    public void commitSelectedCharacterRename(String updatedName) {
        CUnit selectedUnit = Globals.mainWindow.pMapVisualObject.getSelectedCUnit();
        int clanDelimiterIndex = updatedName.indexOf(CHARACTER_RENAME_CLAN_SEPARATOR);
        if (clanDelimiterIndex != -1) {
            clanServerId = Utils.atoiLike(updatedName.substring(clanDelimiterIndex + 1));
            if (clanServerId == 0 || Integer.compareUnsigned(clanServerId, CLAN_SERVER_ID_MAX) > 0) {
                clanServerId = 0;
            }
        }

        selectedUnit.clan = clanDelimiterIndex == -1
                ? updatedName
                : updatedName.substring(0, clanDelimiterIndex);
        String rosterName = selectedUnit.name;
        if (!selectedUnit.clan.isEmpty()) {
            rosterName += CHARACTER_ROSTER_NAME_SEPARATOR + selectedUnit.clan;
        }
        characterRosterNames.set(selectedCharacterRosterFileIndex, rosterName);
        m_PlayerName = characterRosterNames.get(0);
        face = selectedUnit.field8_0x28;

        try {
            writeEncryptedSaveSections(
                    Path.of(characterRosterFilePaths.get(selectedCharacterRosterFileIndex)),
                    packSelectedCharacterHeaderForFile(rosterName),
                    null,
                    null,
                    null,
                    null,
                    null
            );
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to commit selected character rename", e);
        }
    }

    /**
     * Native: Global::writeEncryptedSaveSections @004EDBAF.
     * Fully ported.
     */
    public static void writeEncryptedSaveSections(
            Path characterFilePath,
            byte[] characterHeaderSection,
            byte[] characterStatsSection,
            byte[] playerKnowledgeTable,
            byte[] primaryItemListSection,
            byte[] secondaryItemListSection,
            byte[] playerSlotsSection
    ) throws IOException {
        boolean shouldReuseExistingSections = Files.exists(characterFilePath) && Files.size(characterFilePath) != 0;
        byte[][] existingSections = shouldReuseExistingSections ? readSavedCharacterSections(characterFilePath) : null;
        if (shouldReuseExistingSections && existingSections == null) {
            return;
        }

        if (characterHeaderSection == null && existingSections != null) {
            characterHeaderSection = existingSections[SAVE_SECTION_CHARACTER_HEADER_INDEX];
        }
        if (characterHeaderSection == null) {
            return;
        }
        if (characterStatsSection == null && existingSections != null) {
            characterStatsSection = existingSections[SAVE_SECTION_CHARACTER_STATS_INDEX];
        }
        if (playerKnowledgeTable == null && existingSections != null) {
            playerKnowledgeTable = existingSections[SAVE_SECTION_KNOWLEDGE_TABLE_INDEX];
        }
        if (playerSlotsSection == null && existingSections != null) {
            playerSlotsSection = existingSections[SAVE_SECTION_PLAYER_SLOTS_INDEX];
        }
        if (primaryItemListSection == null && existingSections != null) {
            primaryItemListSection = existingSections[SAVE_SECTION_PRIMARY_ITEM_LIST_INDEX];
        }
        if (secondaryItemListSection == null && existingSections != null) {
            secondaryItemListSection = existingSections[SAVE_SECTION_SECONDARY_ITEM_LIST_INDEX];
        }

        ByteArrayOutputStream file = new ByteArrayOutputStream();
        writeIntLE(file, CHARACTER_FILE_MAGIC);
        writeStandardEncryptedSaveSection(file, SAVE_SECTION_CHARACTER_HEADER_MARKER, characterHeaderSection);
        if (playerKnowledgeTable != null) {
            writeStandardEncryptedSaveSection(
                    file,
                    SAVE_SECTION_KNOWLEDGE_TABLE_MARKER,
                    KnowledgeTableCompression.compress(playerKnowledgeTable)
            );
        }
        if (playerSlotsSection != null) {
            writeStandardEncryptedSaveSection(file, SAVE_SECTION_PLAYER_SLOTS_MARKER, playerSlotsSection);
        }
        if (primaryItemListSection != null) {
            writeStandardEncryptedSaveSection(file, SAVE_SECTION_PRIMARY_ITEM_LIST_MARKER, primaryItemListSection);
        }
        if (characterStatsSection != null) {
            writeCharacterStatsSaveSection(file, characterStatsSection);
        }
        if (secondaryItemListSection != null) {
            writeStandardEncryptedSaveSection(file, SAVE_SECTION_SECONDARY_ITEM_LIST_MARKER, secondaryItemListSection);
        }
        Files.write(characterFilePath, file.toByteArray());
    }

    /**
     * Native support extracted from writeEncryptedSaveSections @004EDBAF standard XOR-encrypted section branches.
     * Fully ported.
     */
    private static void writeStandardEncryptedSaveSection(
            ByteArrayOutputStream file,
            int marker,
            byte[] sectionBytes
    ) {
        int checksum = calculateEncryptedSectionChecksum(sectionBytes);
        byte[] encoded = sectionBytes.clone();
        int seed = nativeRand(SAVE_SECTION_RANDOM_SEED_MAX);
        xorEncryptedSaveSection(encoded, seed);
        writeSaveSectionHeader(file, marker, encoded.length, seed, checksum);
        file.writeBytes(encoded);
    }

    /**
     * Native support extracted from writeEncryptedSaveSections @004EDBAF `0x41392521` section branch.
     * Fully ported.
     */
    private static void writeCharacterStatsSaveSection(ByteArrayOutputStream file, byte[] statsSection) {
        int checksum = calculateEncryptedSectionChecksum(statsSection);
        ByteBuffer stats = ByteBuffer.wrap(statsSection).order(ByteOrder.LITTLE_ENDIAN);
        ByteArrayOutputStream encoded = new ByteArrayOutputStream();
        int paddingFlags = nativeRand(SAVE_SECTION_RANDOM_SEED_MAX);

        writeOptionalStatsPadding(encoded, paddingFlags, 0x0001);
        writeIntLE(encoded, stats.getInt(CHARACTER_STATS_MONSTERS_KILLED_OFFSET) ^ 0x01529251);
        int monstersKilled = stats.getInt(CHARACTER_STATS_MONSTERS_KILLED_OFFSET);
        int playersKilled = stats.getInt(CHARACTER_STATS_PLAYERS_KILLED_OFFSET);
        writeOptionalStatsPadding(encoded, paddingFlags, 0x0002);
        writeIntLE(encoded, playersKilled - (monstersKilled * 5 + 0x13141516));
        int frags = stats.getInt(CHARACTER_STATS_FRAGS_OFFSET);
        writeOptionalStatsPadding(encoded, paddingFlags, 0x0004);
        writeIntLE(encoded, frags - (playersKilled * 7 + 0x00ABCDEF));
        writeOptionalStatsPadding(encoded, paddingFlags, 0x0008);
        writeIntLE(encoded, stats.getInt(CHARACTER_STATS_DEATH_COUNT_OFFSET) ^ 0x17FF12AA);
        int gold = stats.getInt(CHARACTER_STATS_GOLD_OFFSET);
        writeOptionalStatsPadding(encoded, paddingFlags, 0x0010);
        writeIntLE(encoded, gold - (monstersKilled * 3 + 0xDEADBABE));

        int body = statsSection[CHARACTER_STATS_BODY_OFFSET];
        int reaction = statsSection[CHARACTER_STATS_REACTION_OFFSET];
        int mind = statsSection[CHARACTER_STATS_MIND_OFFSET];
        writeOptionalStatsPadding(encoded, paddingFlags, 0x0020);
        encoded.write(body - ((byte) gold * 0x11 + (byte) monstersKilled * 0x13));
        writeOptionalStatsPadding(encoded, paddingFlags, 0x0040);
        encoded.write(reaction - body * 0x03);
        writeOptionalStatsPadding(encoded, paddingFlags, 0x0080);
        encoded.write(mind - (body + reaction * 0x05));
        writeOptionalStatsPadding(encoded, paddingFlags, 0x0100);
        encoded.write(statsSection[CHARACTER_STATS_SPIRIT_OFFSET] - (body * 0x07 + mind * 0x09));

        writeOptionalStatsPadding(encoded, paddingFlags, 0x4000);
        writeIntLE(encoded, stats.getInt(CHARACTER_STATS_AVAILABLE_SPELL_MASK_OFFSET) + 0x10121974);
        writeOptionalStatsPadding(encoded, paddingFlags, 0x2000);
        writeIntLE(encoded, stats.getInt(CHARACTER_STATS_AUTO_CAST_SPELL_ID_OFFSET));
        for (int skillIndex = 0; skillIndex < 5; skillIndex++) {
            writeOptionalStatsPadding(encoded, paddingFlags, 0x0200 << skillIndex);
            int bonus = stats.getInt(CHARACTER_STATS_SKILL_BONUS_OFFSET + skillIndex * Integer.BYTES);
            if (skillIndex == 0) {
                writeIntLE(encoded, bonus ^ 0xDADEDADE);
            } else {
                int previousBonus = stats.getInt(CHARACTER_STATS_SKILL_BONUS_OFFSET
                        + (skillIndex - 1) * Integer.BYTES);
                writeIntLE(encoded, bonus + previousBonus * 0x771);
            }
        }

        writeSaveSectionHeader(
                file,
                SAVE_SECTION_CHARACTER_STATS_MARKER,
                encoded.size(),
                paddingFlags,
                checksum
        );
        file.writeBytes(encoded.toByteArray());
    }

    /**
     * Native support extracted from writeEncryptedSaveSections @004EDBAF stats-section random padding.
     * Fully ported.
     */
    private static void writeOptionalStatsPadding(ByteArrayOutputStream file, int flags, int mask) {
        int padding = nativeRand(SAVE_SECTION_RANDOM_PADDING_BYTE_MAX);
        if ((flags & mask) != 0) {
            file.write(padding);
        }
    }

    /**
     * Native support extracted from writeEncryptedSaveSections @004EDBAF section header writes.
     * Fully ported.
     */
    private static void writeSaveSectionHeader(
            ByteArrayOutputStream file,
            int marker,
            int sectionSize,
            int seedOrFlags,
            int checksum
    ) {
        writeIntLE(file, marker);
        writeIntLE(file, sectionSize);
        writeShortLE(file, 0);
        writeShortLE(file, seedOrFlags);
        writeIntLE(file, checksum);
    }

    /**
     * Native support extracted from writeEncryptedSaveSections @004EDBAF little-endian DWORD writes.
     * Fully ported.
     */
    private static void writeIntLE(ByteArrayOutputStream buffer, int value) {
        buffer.write(value & 0xFF);
        buffer.write((value >>> 8) & 0xFF);
        buffer.write((value >>> 16) & 0xFF);
        buffer.write((value >>> 24) & 0xFF);
    }

    /**
     * Native support extracted from writeEncryptedSaveSections @004EDBAF little-endian WORD writes.
     * Fully ported.
     */
    private static void writeShortLE(ByteArrayOutputStream buffer, int value) {
        buffer.write(value & 0xFF);
        buffer.write((value >>> 8) & 0xFF);
    }

    /**
     * Native support extracted from Rand @0051FA25.
     */
    private static int nativeRand(int max) {
        return Utils.randInclusive(max);
    }

    /**
     * Native support extracted from CGameSession::loadCharacterRosterEntry @004904C5 and
     * CGameSession::refreshRuntimeUnitFromSession @004919FF.
     */
    private Human createStartingRuntimeHuman() {
        HumanId templateId = switch (type & SESSION_TYPE_CLASS_AND_SEX_MASK) {
            case SESSION_TYPE_MAGE -> HumanId.START_MM;
            case SESSION_TYPE_FEMALE -> HumanId.START_FF;
            case SESSION_TYPE_CLASS_AND_SEX_MASK -> HumanId.START_FM;
            default -> HumanId.START_MF;
        };
        return Human.createFromTemplate(templateId.tableName, true, false);
    }

    /**
     * Native support extracted from the `%u%u.a2c` CString::Format call in
     * CGameSession::InitializeNewCharacterSession @00491312.
     */
    private String formatCharacterFileName() {
        return Integer.toUnsignedString(sessionKeyPart1) + Integer.toUnsignedString(sessionKeyPart2) + ".a2c";
    }

    /**
     * Native: CGameSession::GetCharacterRosterEntryCount @004383C0.
     * Fully ported.
     */
    public int getCharacterRosterEntryCount() {
        return characterRosterNames.size();
    }

    /**
     * Native support extracted from CGameSession::New @0048FE1C PlayerSlot[9] construction and
     * CMainWindow::runSessionBootstrap @0048C8A3 embedded PlayerSlot[9] layout.
     */
    private static PlayerSlot[] createPlayerSlots() {
        PlayerSlot[] playerSlots = new PlayerSlot[9];
        for (int index = 0; index < playerSlots.length; index++) {
            playerSlots[index] = new PlayerSlot();
        }
        return playerSlots;
    }

    /**
     * Native: CGameSession::loadCharacterRosterEntry @004904C5.
     * Fully ported.
     */
    public void loadCharacterRosterEntry(int entryIndex) {
        selectedCharacterRosterFileIndex = entryIndex;
        MapVisualObject mapVisualObject = Globals.mainWindow.pMapVisualObject;
        boolean createdSelectedUnit = mapVisualObject.getSelectedCUnit() == null;
        CUnit selectedUnit = mapVisualObject.ensureSelectedCUnit();
        if (createdSelectedUnit) {
            mapVisualObject.onMessage(MessageCodes.REFRESH_LAYOUT, 0, 0);
        }
        if (entryIndex < 0 || entryIndex >= characterRosterFilePaths.size()) {
            return;
        }

        try {
            byte[][] sections = readSavedCharacterSections(Path.of(characterRosterFilePaths.get(entryIndex)));
            if (sections == null
                    || sections[SAVE_SECTION_CHARACTER_HEADER_INDEX] == null
                    || sections[SAVE_SECTION_CHARACTER_STATS_INDEX] == null) {
                return;
            }

            applySavedCharacterHeader(sections[SAVE_SECTION_CHARACTER_HEADER_INDEX]);
            applySavedCharacterNameToPreviewUnit(selectedUnit, m_PlayerName);
            applySavedCharacterStatsToPreviewUnit(
                    mapVisualObject,
                    selectedUnit,
                    sections[SAVE_SECTION_CHARACTER_STATS_INDEX],
                    sections[SAVE_SECTION_PRIMARY_ITEM_LIST_INDEX]
            );
            if (sections[SAVE_SECTION_PLAYER_SLOTS_INDEX] != null) {
                readPlayerSlotsFromSavedSection(sections[SAVE_SECTION_PLAYER_SLOTS_INDEX]);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load saved character roster entry", e);
        }
    }

    /**
     * Native support extracted from CGameSession::LoadCharacterRosterEntry @004904C5 header section reads.
     */
    private void applySavedCharacterHeader(byte[] headerSection) {
        ByteBuffer header = ByteBuffer.wrap(headerSection).order(ByteOrder.LITTLE_ENDIAN);
        sessionKeyPart1 = header.getInt(CHARACTER_HEADER_SESSION_KEY_PART1_OFFSET);
        sessionKeyPart2 = header.getInt(CHARACTER_HEADER_SESSION_KEY_PART2_OFFSET);
        characterFileOwnerId = header.getInt(CHARACTER_HEADER_OWNER_OFFSET);
        m_PlayerName = readNullTerminatedString(
                headerSection,
                CHARACTER_HEADER_PLAYER_NAME_OFFSET,
                CHARACTER_HEADER_PLAYER_NAME_SIZE
        );
        type = Byte.toUnsignedInt(headerSection[CHARACTER_HEADER_TYPE_OFFSET]);
        face = Byte.toUnsignedInt(headerSection[CHARACTER_HEADER_FACE_OFFSET]);
        startingSkillIndex = Byte.toUnsignedInt(headerSection[CHARACTER_HEADER_STARTING_SKILL_OFFSET]);
        initialized = Byte.toUnsignedInt(headerSection[CHARACTER_HEADER_INITIALIZED_OFFSET]);
        clanServerId = Byte.toUnsignedInt(headerSection[CHARACTER_HEADER_CLAN_SERVER_ID_OFFSET]);
    }

    /**
     * Native support extracted from CGameSession::LoadCharacterRosterEntry @004904C5 player-name split.
     */
    private static void applySavedCharacterNameToPreviewUnit(CUnit selectedUnit, String playerName) {
        int splitIndex = playerName.indexOf('|');
        if (splitIndex == -1) {
            selectedUnit.name = truncateNativeCString(playerName, PLAYER_NAME_WITHOUT_CLAN_COPY_SIZE);
            selectedUnit.clan = "";
            return;
        }
        selectedUnit.name = truncateNativeCString(playerName.substring(0, splitIndex), PLAYER_NAME_WITH_CLAN_COPY_SIZE);
        selectedUnit.clan = truncateNativeCString(playerName.substring(splitIndex + 1), PLAYER_NAME_WITH_CLAN_COPY_SIZE);
    }

    /**
     * Native support extracted from CGameSession::LoadCharacterRosterEntry @004904C5 stat section reads.
     */
    private void applySavedCharacterStatsToPreviewUnit(
            MapVisualObject mapVisualObject,
            CUnit selectedUnit,
            byte[] statsSection,
            byte[] primaryItemListSection
    ) {
        ByteBuffer stats = ByteBuffer.wrap(statsSection).order(ByteOrder.LITTLE_ENDIAN);
        monstersKilled = stats.getInt(CHARACTER_STATS_MONSTERS_KILLED_OFFSET);
        playersKilled = stats.getInt(CHARACTER_STATS_PLAYERS_KILLED_OFFSET);
        fragCount = stats.getInt(CHARACTER_STATS_FRAGS_OFFSET);
        deathCount = stats.getInt(CHARACTER_STATS_DEATH_COUNT_OFFSET);
        characterGold = stats.getInt(CHARACTER_STATS_GOLD_OFFSET);
        body = Byte.toUnsignedInt(statsSection[CHARACTER_STATS_BODY_OFFSET]);
        reaction = Byte.toUnsignedInt(statsSection[CHARACTER_STATS_REACTION_OFFSET]);
        mind = Byte.toUnsignedInt(statsSection[CHARACTER_STATS_MIND_OFFSET]);
        spirit = Byte.toUnsignedInt(statsSection[CHARACTER_STATS_SPIRIT_OFFSET]);

        Human runtimeUnit = createStartingRuntimeHuman();
        runtimeUnit.m_nBody = body;
        runtimeUnit.m_nReaction = reaction;
        runtimeUnit.m_nMind = mind;
        runtimeUnit.m_nSpirit = spirit;
        runtimeUnit.face = face;

        int highestSkillLevel = 0;
        runtimeUnit.skillsTotalBonusPermille = 0;
        for (int skillIndex = FIRST_SKILL_INDEX; skillIndex < SKILL_INDEX_LIMIT; skillIndex++) {
            int bonusPermille = stats.getInt(CHARACTER_STATS_SKILL_BONUS_OFFSET
                    + (skillIndex - FIRST_SKILL_INDEX) * Integer.BYTES);
            runtimeUnit.skillBonusesPermille.data[skillIndex] = bonusPermille;
            runtimeUnit.skillData.skillLevels[skillIndex] = (short) skillLevelForBonusPermille(bonusPermille);
            highestSkillLevel = Math.max(highestSkillLevel, runtimeUnit.skillData.skillLevels[skillIndex]);
            runtimeUnit.skillsTotalBonusPermille += bonusPermille;
        }
        for (int skillIndex = FIRST_SKILL_INDEX; skillIndex < SKILL_INDEX_LIMIT; skillIndex++) {
            runtimeUnit.skillDataSnapshot.skillLevels[skillIndex] = runtimeUnit.skillData.skillLevels[skillIndex];
        }
        updatePlayerCountBoundsForSavedSkillLevel(highestSkillLevel);
        runtimeUnit.recalculateDerivedStats();
        if (primaryItemListSection != null) {
            applySavedCharacterItemList(runtimeUnit, primaryItemListSection);
        }

        selectedUnit.copyFromRuntimeUnit(runtimeUnit);
        selectedUnit.copiedBody = statsSection[CHARACTER_STATS_BODY_OFFSET];
        selectedUnit.copiedReaction = statsSection[CHARACTER_STATS_REACTION_OFFSET];
        selectedUnit.copiedMind = statsSection[CHARACTER_STATS_MIND_OFFSET];
        selectedUnit.copiedSpirit = statsSection[CHARACTER_STATS_SPIRIT_OFFSET];
        selectedUnit.cPlayer = mapVisualObject.clientPlayers.getFirst();
    }

    /**
     * Native support extracted from CGameSession::LoadCharacterRosterEntry @004904C5 primary item-list branch.
     */
    private static void applySavedCharacterItemList(Human runtimeUnit, byte[] primaryItemListSection) {
        runtimeUnit.moveEquippedItemsToInventory();
        if (runtimeUnit.pWeapon != null) {
            runtimeUnit.inventory.addItem(runtimeUnit.releaseIncomingObject(runtimeUnit.pWeapon));
        }
        if (runtimeUnit.pShield != null) {
            runtimeUnit.inventory.addItem(runtimeUnit.releaseIncomingObject(runtimeUnit.pShield));
        }
        runtimeUnit.inventory = new Inventory();

        ByteBuffer itemList = ByteBuffer.wrap(primaryItemListSection).order(ByteOrder.LITTLE_ENDIAN);
        itemList.position(ITEM_LIST_SECTION_TRAILING_DATA_OFFSET);
        for (int slotIndex = 0; slotIndex < SAVED_CHARACTER_ITEM_SLOT_COUNT; slotIndex++) {
            Item item = Item.readSavedCharacterItemPayload(itemList);
            if (item.hash != 0) {
                runtimeUnit.addIncomingObjectToInventory(item);
            }
        }
    }

    /**
     * Native support extracted from CGameSession::LoadCharacterRosterEntry @004904C5 min/max player gates.
     */
    private void updatePlayerCountBoundsForSavedSkillLevel(int highestSkillLevel) {
        if (highestSkillLevel < 0x60) {
            if (highestSkillLevel < 0x4C) {
                minimumPlayerCount = highestSkillLevel < 0x33 ? 1 : 2;
            } else {
                minimumPlayerCount = 3;
            }
        } else {
            minimumPlayerCount = 4;
        }

        if (highestSkillLevel < 0x1A) {
            maximumPlayerCount = 1;
        } else if (highestSkillLevel < 0x33) {
            maximumPlayerCount = 2;
        } else if (highestSkillLevel < 0x5A) {
            maximumPlayerCount = 3;
        } else {
            maximumPlayerCount = 4;
        }
    }

    /**
     * Native support extracted from CGameSession::LoadCharacterRosterEntry @004904C5 PlayerSlot::readFromBuffer loop.
     */
    private void readPlayerSlotsFromSavedSection(byte[] playerSlotsSection) {
        ByteBuffer playerSlots = ByteBuffer.wrap(playerSlotsSection).order(ByteOrder.LITTLE_ENDIAN);
        for (PlayerSlot playerSlot : m_PlayerSlots) {
            playerSlot.readFromBuffer(playerSlots);
        }
    }

    /**
     * Native support extracted from bounded character-array copies in CGameSession::LoadCharacterRosterEntry @004904C5.
     */
    private static String truncateNativeCString(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    /**
     * Native: CGameSession::deleteSelectedCharacterRosterEntry @00491532.
     * Fully ported.
     */
    public void deleteSelectedCharacterRosterEntry() {
        String filePath = characterRosterFilePaths.get(selectedCharacterRosterFileIndex);
        try {
            Files.deleteIfExists(Path.of(filePath));
        } catch (IOException ignored) {
            // Native DeleteFileA return value is ignored.
        }
        characterRosterFilePaths.remove(selectedCharacterRosterFileIndex);
        characterRosterNames.remove(selectedCharacterRosterFileIndex);
        if (selectedCharacterRosterFileIndex == 0) {
            selectedCharacterRosterFileIndex = 1;
        }
        loadCharacterRosterEntry(selectedCharacterRosterFileIndex - 1);
    }
}
