package ua.millfreedom.rom2;

import ua.millfreedom.rom2.model.ComPortSettings;
import ua.millfreedom.rom2.model.Hat;
import ua.millfreedom.rom2.model.PhoneBook;
import ua.millfreedom.rom2.model.sound.SoundPreferences;
import ua.millfreedom.rom2.model.window.CMainWindow;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.PatchText.CTHULHU_FHTAGN_118;
import static ua.millfreedom.rom2.text.TextTableId.PATCH;

public final class ApplicationPreferences {
    private static final String PREFERENCES_NODE_PATH = "/SOFTWARE/ROM2";
    private static final String KEY_RESOLUTION = "RESOLUTION";
    private static final String DEFAULT_RESOLUTION = "-1024";
    private static final String KEY_SOUND_RANDOM = "SoundRandom";
    private static final String KEY_SOUND_MUSIC_POSITION = "SoundMusPos";
    private static final String KEY_SOUND_SFX_POSITION = "SoundSfxPos";
    private static final String KEY_SOUND_SPEECH_POSITION = "SoundSpeechPos";
    private static final String KEY_MUSIC_ENABLED = "MusicEnabled";
    private static final String KEY_LAST_PROTOCOL = "lastprotocol";
    private static final String KEY_PHONE_BOOK_SIZE = "phonebooksize";
    private static final String PHONE_ENTRY_FORMAT = "phone%d";
    private static final String KEY_COM_PORT_SETTINGS = "comportsettings";
    private static final String KEY_LAST_IP = "lastip";
    private static final String KEY_HAT_IP = "hatip";
    private static final String KEY_IS_HAT = "ishat";
    private static final String KEY_USING_VXD = "Using VxD";

    /**
     * Native: Global::loadApplicationPreferences @00440BB7.
     * Fully ported.
     */
    public static int loadApplicationPreferences(CMainWindow mainWindow) {
        Globals.gamePreferences.bindRuntimeValues(mainWindow);
        mainWindow.LastProtocol = 0;
        resetComPortSettings(mainWindow.serialSettings);
        if (!preferencesNodeExists()) {
            return 0;
        }

        Preferences preferences = Preferences.userRoot().node(PREFERENCES_NODE_PATH);
        loadSoundPreferences(preferences, Globals.soundPreferences);
        Globals.gamePreferences.load(preferences, mainWindow);
        loadProtocol(preferences, mainWindow);
        loadPhoneBook(preferences, mainWindow.PhoneBook);
        loadComPortSettings(preferences, mainWindow.serialSettings);
        loadLastIp(preferences, mainWindow.lastIP);
        loadHat(preferences, mainWindow.Hat);
        Globals.usingVxD = preferences.getInt(KEY_USING_VXD, Globals.usingVxD);
        if (Globals.usingVxD == 0) {
            Globals.usingVxD = 1;
        }
        return 1;
    }

    /**
     * Native: Global::saveApplicationPreferences @00440CD2.
     * Fully ported.
     */
    public static int saveApplicationPreferences(CMainWindow mainWindow) {
        Preferences preferences = Preferences.userRoot().node(PREFERENCES_NODE_PATH);
        saveSoundPreferences(preferences, Globals.soundPreferences);
        Globals.gamePreferences.save(preferences, mainWindow);
        saveProtocol(preferences, mainWindow);
        savePhoneBook(preferences, mainWindow.PhoneBook);
        saveComPortSettings(preferences, mainWindow.serialSettings);
        saveLastIp(preferences, mainWindow.lastIP);
        saveHat(preferences, mainWindow.Hat);
        preferences.putInt(KEY_USING_VXD, Globals.usingVxD);
        flushPreferences(preferences);
        return 1;
    }

    /**
     * Native support extracted from the RESOLUTION registry query in CMainApp::InitInstance @00480EA9.
     */
    public static String loadResolutionPreference() {
        if (!preferencesNodeExists()) {
            return DEFAULT_RESOLUTION;
        }
        Preferences preferences = Preferences.userRoot().node(PREFERENCES_NODE_PATH);
        return preferences.get(KEY_RESOLUTION, DEFAULT_RESOLUTION);
    }

    /**
     * Native support extracted from the `RegOpenKeyExA` failure branch in Global::loadApplicationPreferences @00440BB7.
     */
    private static boolean preferencesNodeExists() {
        try {
            return Preferences.userRoot().nodeExists(PREFERENCES_NODE_PATH);
        } catch (BackingStoreException e) {
            return false;
        }
    }

    /**
     * Native support extracted from SoundPreferences::Load @0043C978.
     * Java-normalized for clamped 0..100 volume values.
     */
    private static void loadSoundPreferences(Preferences preferences, SoundPreferences soundPreferences) {
        soundPreferences.soundRandom = preferences.getInt(KEY_SOUND_RANDOM, soundPreferences.soundRandom);
        soundPreferences.musicVolume = SoundPreferences.clampVolume(
                preferences.getInt(KEY_SOUND_MUSIC_POSITION, soundPreferences.musicVolume)
        );
        soundPreferences.sfxVolume = SoundPreferences.clampVolume(
                preferences.getInt(KEY_SOUND_SFX_POSITION, soundPreferences.sfxVolume)
        );
        soundPreferences.speechVolume = SoundPreferences.clampVolume(
                preferences.getInt(KEY_SOUND_SPEECH_POSITION, soundPreferences.speechVolume)
        );
        soundPreferences.musicEnabled = preferences.getInt(KEY_MUSIC_ENABLED, soundPreferences.musicEnabled);
    }

    /**
     * Native support extracted from SoundPreferences::Save @0043CA2B.
     * Java-normalized for clamped 0..100 volume values.
     */
    private static void saveSoundPreferences(Preferences preferences, SoundPreferences soundPreferences) {
        preferences.putInt(KEY_SOUND_RANDOM, soundPreferences.soundRandom);
        preferences.putInt(KEY_SOUND_MUSIC_POSITION, SoundPreferences.clampVolume(soundPreferences.musicVolume));
        preferences.putInt(KEY_SOUND_SFX_POSITION, SoundPreferences.clampVolume(soundPreferences.sfxVolume));
        preferences.putInt(KEY_SOUND_SPEECH_POSITION, SoundPreferences.clampVolume(soundPreferences.speechVolume));
        preferences.putInt(KEY_MUSIC_ENABLED, soundPreferences.musicEnabled);
    }

    /**
     * Native support extracted from Protocol::Load @0043C731.
     */
    private static void loadProtocol(Preferences preferences, CMainWindow mainWindow) {
        mainWindow.LastProtocol = 0;
        mainWindow.LastProtocol = preferences.getInt(KEY_LAST_PROTOCOL, mainWindow.LastProtocol);
    }

    /**
     * Native support extracted from Protocol::Save @0043C76D.
     */
    private static void saveProtocol(Preferences preferences, CMainWindow mainWindow) {
        preferences.putInt(KEY_LAST_PROTOCOL, mainWindow.LastProtocol);
    }

    /**
     * Native support extracted from PhoneBook::Load @0043C51D.
     */
    private static void loadPhoneBook(Preferences preferences, PhoneBook phoneBook) {
        int phoneBookSize = preferences.getInt(KEY_PHONE_BOOK_SIZE, 0);
        for (int i = 0; i < phoneBookSize; i++) {
            phoneBook.numbers.add(preferences.get(PHONE_ENTRY_FORMAT.formatted(i), ""));
        }
    }

    /**
     * Native support extracted from PhoneBook::Save @0043C5C9.
     */
    private static void savePhoneBook(Preferences preferences, PhoneBook phoneBook) {
        int phoneBookSize = phoneBook.numbers.size();
        preferences.putInt(KEY_PHONE_BOOK_SIZE, phoneBookSize);
        for (int i = 0; i < phoneBookSize; i++) {
            preferences.put(PHONE_ENTRY_FORMAT.formatted(i), phoneBook.numbers.get(i));
        }
    }

    /**
     * Native support extracted from ComPortSettings::New @0043C692.
     */
    private static void resetComPortSettings(ComPortSettings settings) {
        settings.comPortNumber = 1;
        settings.baudRate = 0x3840;
        settings.stopBitsSelection = 0;
        settings.paritySelection = 0;
        settings.flowControlSelection = 4;
    }

    /**
     * Native support extracted from ComPortSettings::Load @0043C6CE.
     */
    private static void loadComPortSettings(Preferences preferences, ComPortSettings settings) {
        resetComPortSettings(settings);
        byte[] payload = preferences.getByteArray(KEY_COM_PORT_SETTINGS, new byte[0]);
        settings.comPortNumber = readInt32(payload, 0x00, settings.comPortNumber);
        settings.baudRate = readInt32(payload, 0x04, settings.baudRate);
        settings.stopBitsSelection = readInt32(payload, 0x08, settings.stopBitsSelection);
        settings.paritySelection = readInt32(payload, 0x0C, settings.paritySelection);
        settings.flowControlSelection = readInt32(payload, 0x10, settings.flowControlSelection);
    }

    /**
     * Native support extracted from ComPortSettings::Save @0043C709.
     */
    private static void saveComPortSettings(Preferences preferences, ComPortSettings settings) {
        ByteBuffer payload = ByteBuffer.allocate(ComPortSettings.NATIVE_SIZE).order(ByteOrder.LITTLE_ENDIAN);
        payload.putInt(settings.comPortNumber);
        payload.putInt(settings.baudRate);
        payload.putInt(settings.stopBitsSelection);
        payload.putInt(settings.paritySelection);
        payload.putInt(settings.flowControlSelection);
        preferences.putByteArray(KEY_COM_PORT_SETTINGS, payload.array());
    }

    /**
     * Native support extracted from little-endian fixed-size payload reads in ComPortSettings::Load @0043C6CE.
     */
    private static int readInt32(byte[] payload, int offset, int defaultValue) {
        if (payload.length < offset + Integer.BYTES) {
            return defaultValue;
        }
        return ByteBuffer.wrap(payload, offset, Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    /**
     * Native support extracted from LastIp::Load @0043C795.
     */
    private static void loadLastIp(Preferences preferences, CString lastIp) {
        setCString(lastIp, preferences.get(KEY_LAST_IP, ""));
    }

    /**
     * Native support extracted from LastIp::Save @0043C7EA.
     */
    private static void saveLastIp(Preferences preferences, CString lastIp) {
        preferences.put(KEY_LAST_IP, lastIp.toString());
    }

    /**
     * Native support extracted from Hat::Load @0043C849.
     */
    private static void loadHat(Preferences preferences, Hat hat) {
        hat.ip = preferences.get(KEY_HAT_IP, "");
        if (hat.ip.isEmpty()) {
            hat.ip = get(PATCH, CTHULHU_FHTAGN_118);
        }
        hat.isHat = preferences.getInt(KEY_IS_HAT, hat.isHat ? 1 : 0) != 0;
    }

    /**
     * Native support extracted from Hat::Save @0043C8EF.
     */
    private static void saveHat(Preferences preferences, Hat hat) {
        preferences.put(KEY_HAT_IP, hat.ip);
        preferences.putInt(KEY_IS_HAT, hat.isHat ? 1 : 0);
    }

    /**
     * Native support extracted from CString assignment in LastIp::Load @0043C795.
     */
    private static void setCString(CString target, String value) {
        target.set(value.getBytes(StandardCharsets.ISO_8859_1));
    }

    /**
     * Native support extracted from the RegCloseKey tail of Global::saveApplicationPreferences @00440CD2.
     */
    private static void flushPreferences(Preferences preferences) {
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            throw new IllegalStateException("Unable to flush application preferences", e);
        }
    }
}
