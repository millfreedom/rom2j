package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.model.window.CMainWindow;

import java.util.prefs.Preferences;

/**
 * Native owner: g_GamePreferences global used by multiple dialog-setting flows.
 * Purpose: minimal Java holder for recovered game-options fields that are read/written by centered option dialogs.
 */
public class GamePreferences {
    public static final int NATIVE_SIZE = 0x3C; // VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    public static final int AUTOCAST_BASE = 0x08;
    public static final int AUTOCAST_MODE_AVERAGE = 0x10;
    public static final int AUTOCAST_MODE_MAXIMUM = 0x20;
    public static final int AUTOCAST_OWN = 0x01;
    public static final int AUTOCAST_ALLIES = 0x02;
    public static final int AUTOCAST_NEUTRAL = 0x04;
    private static final String KEY_GAME_SPEED = "GameSpeed";
    private static final String KEY_FORMATION_MODE = "FormationMode";
    private static final String KEY_WIMPY_MODE = "WimpyMode";
    private static final String KEY_SHOW_ALL_HIT_POINTS = "ShowAllHitPoints";
    private static final String KEY_SMOOTHING = "Smoothing";
    private static final String KEY_SHOW_FLYING_HP = "ShowFlyingHP";
    private static final String KEY_SHOW_TIME_FLOW = "ShowTimeFlow";
    private static final String KEY_TIPS_MODE = "TipsMode";
    private static final String KEY_AUTO_CASTING = "AutoCasting";
    private static final String KEY_ACKNOWLEDGEMENT = "Acknowledgement";
    private static final String KEY_SHADOWS = "Shadows";
    private static final String KEY_LIGHTING = "Lighting";
    private static final String KEY_ANIMATION = "Animation";
    private static final String KEY_CLAN_NAMES = "ClanNames";
    private static final String KEY_MESSAGE_COLORS = "MessageColors";

    //0x00
    public int gameSpeed = 8;
    //0x04
    public int formationMode;
    //0x08
    public int wimpyMode;
    //0x0c
    public int showAllHitPoints = 1;
    //0x10
    public int smoothing = 1;
    //0x14
    public int showFlyingHp = 1;
    //0x18
    public int showTimeFlow = 1;
    //0x1c
    public int tipsMode = 1;
    //0x20
    public int acknowledgement = 1;
    //0x24
    public int autoCasting = AUTOCAST_BASE | AUTOCAST_MODE_AVERAGE | AUTOCAST_OWN;
    //0x28
    public int shadows = 1;
    //0x2c
    public int lighting = 1;
    //0x30
    public int animation = 1;
    //0x34
    public int clanNames = 1;
    //0x38
    public int messageColors;

    /**
     * Native: GamePreferences::GamePreferences @0044131D.
     * Fully ported.
     */
    public GamePreferences() {
    }

    /**
     * Native: GamePreferences::New @0044153A.
     * Fully ported. Java stores native pointer-backed preference slots as direct values, so this captures the same runtime targets.
     */
    public void bindRuntimeValues(CMainWindow mainWindow) {
        gameSpeed = mainWindow.gameSpeed;
        formationMode = mainWindow.pMapVisualObject.formationMode;
        wimpyMode = mainWindow.pMapVisualObject.wimpyMode;
        showAllHitPoints = mainWindow.pMapVisualObject.showHitPointBars;
        showFlyingHp = mainWindow.pMapVisualObject.showFlyingHitPointBars;
    }

    /**
     * Native: GamePreferences::Load @004415D4.
     * Fully ported.
     */
    public void load(Preferences preferences, CMainWindow mainWindow) {
        gameSpeed = preferences.getInt(KEY_GAME_SPEED, gameSpeed);
        formationMode = preferences.getInt(KEY_FORMATION_MODE, formationMode);
        wimpyMode = preferences.getInt(KEY_WIMPY_MODE, wimpyMode);
        showAllHitPoints = preferences.getInt(KEY_SHOW_ALL_HIT_POINTS, showAllHitPoints);
        smoothing = preferences.getInt(KEY_SMOOTHING, smoothing);
        showFlyingHp = preferences.getInt(KEY_SHOW_FLYING_HP, showFlyingHp);
        showTimeFlow = preferences.getInt(KEY_SHOW_TIME_FLOW, showTimeFlow);
        tipsMode = preferences.getInt(KEY_TIPS_MODE, tipsMode);
        autoCasting = preferences.getInt(KEY_AUTO_CASTING, autoCasting);
        acknowledgement = preferences.getInt(KEY_ACKNOWLEDGEMENT, acknowledgement);
        shadows = preferences.getInt(KEY_SHADOWS, shadows);
        lighting = preferences.getInt(KEY_LIGHTING, lighting);
        animation = preferences.getInt(KEY_ANIMATION, animation);
        clanNames = preferences.getInt(KEY_CLAN_NAMES, clanNames);
        messageColors = preferences.getInt(KEY_MESSAGE_COLORS, messageColors);
        applyRuntimeValues(mainWindow);
    }

    /**
     * Native: GamePreferences::Save @00441372.
     * Fully ported.
     */
    public void save(Preferences preferences, CMainWindow mainWindow) {
        bindRuntimeValues(mainWindow);
        preferences.putInt(KEY_GAME_SPEED, gameSpeed);
        preferences.putInt(KEY_FORMATION_MODE, formationMode);
        preferences.putInt(KEY_WIMPY_MODE, wimpyMode);
        preferences.putInt(KEY_SHOW_ALL_HIT_POINTS, showAllHitPoints);
        preferences.putInt(KEY_SMOOTHING, smoothing);
        preferences.putInt(KEY_SHOW_FLYING_HP, showFlyingHp);
        preferences.putInt(KEY_SHOW_TIME_FLOW, showTimeFlow);
        preferences.putInt(KEY_TIPS_MODE, tipsMode);
        preferences.putInt(KEY_AUTO_CASTING, autoCasting);
        preferences.putInt(KEY_ACKNOWLEDGEMENT, acknowledgement);
        preferences.putInt(KEY_SHADOWS, shadows);
        preferences.putInt(KEY_LIGHTING, lighting);
        preferences.putInt(KEY_ANIMATION, animation);
        preferences.putInt(KEY_CLAN_NAMES, clanNames);
        preferences.putInt(KEY_MESSAGE_COLORS, messageColors);
    }

    /**
     * Native support extracted from GamePreferences pointer-backed runtime targets initialized by
     * GamePreferences::New @0044153A and written by GamePreferences::Load @004415D4.
     */
    public void applyRuntimeValues(CMainWindow mainWindow) {
        mainWindow.gameSpeed = gameSpeed;
        mainWindow.pMapVisualObject.formationMode = formationMode;
        mainWindow.pMapVisualObject.wimpyMode = wimpyMode;
        mainWindow.pMapVisualObject.showHitPointBars = showAllHitPoints;
        mainWindow.pMapVisualObject.showFlyingHitPointBars = showFlyingHp;
    }
}
