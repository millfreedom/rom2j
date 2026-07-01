package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.ScriptDataSupport;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.sound.Sound;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_CLICK_TO_BRING_UP_THE_MAIN_MENU_235;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_GIRL_359;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_GIRL_360;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_GUARD_361;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_INN_236;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_SHOP_233;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_SLEEPING_MAN_362;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_START_MISSION_237;

/**
 * Native class: KaargTownDialogVisualObject.
 * Purpose: Kaarg town-screen dialog (`id=0x3FC`) with Kaarg-specific hover hotspots, animated NPCs, and ambient sounds.
 */
public class KaargTownDialogVisualObject extends BasicTownDialogVisualObject {
    public static final int NATIVE_SIZE = 0x314; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int HOVER_REFRESH_INTERVAL_MS = 100;
    private static final int RANDOM_SHORT_DELAY_BASE_MS = 2000;
    private static final int RANDOM_CHARACTER_DELAY_BASE_MS = 0xDAC;
    private static final int RANDOM_DERVISH_DELAY_BASE_MS = 0xE74;
    private static final int RANDOM_GUARD_DELAY_BASE_MS = 0x1D4C;
    private static final int RANDOM_DERVISH_DELAY_SPAN_MS = 500;
    private static final int RANDOM_AMBIENT_DELAY_SPAN_MS = 5000;
    private static final int RANDOM_GLOBAL_AMBIENT_DELAY_SPAN_MS = 2000;
    private static final int RANDOM_GLOBAL_AMBIENT_DELAY_BASE_MS = 2000;
    private static final int RANDOM_UNUSED_UPDATE_DELAY_BASE_MS = 1000;
    private static final int DERVISH_SOUND_REPEAT_MS = 45000;
    private static final int SCENARIO_CHAPTER_VAR_ID = 0x300;
    private static final int DERVISH_FRAME_MAX_COUNT = 0x1E;
    private static final int GUARD_FRAME_MAX_COUNT = 0x37;
    private static final int GIRL_FRAME_MAX_COUNT = 0x1F;
    private static final int MAINGATE_FRAME_COUNT = 0x0B;

    private static final String KAARG_TOWN_MAIN_BMP = "graphics/interface/town_kaarg/townmain.bmp";
    private static final String KAARG_TOWN_MASK_BMP = "graphics/interface/town_kaarg/townmask.bmp";
    private static final String KAARG_TAVERN_HIGHLIGHT_BMP = "graphics/interface/town_kaarg/hili_tavern.bmp";
    private static final String KAARG_SHOP_HIGHLIGHT_BMP = "graphics/interface/town_kaarg/hili_shop.bmp";
    private static final String GIRL1_FRAME_PATH_FORMAT = "graphics/interface/town_kaarg/girl1/g%d%03d.bmp";
    private static final String GIRL2_FRAME_PATH_FORMAT = "graphics/interface/town_kaarg/girl2/g%d%03d.bmp";
    private static final String GUARD_FRAME_PATH_FORMAT = "graphics/interface/town_kaarg/guard/g%04d.bmp";
    private static final String DERVISH_FRAME_PATH_FORMAT = "graphics/interface/town_kaarg/dervish/d%04d.bmp";
    private static final String MAINGATE_FRAME_PATH_FORMAT = "graphics/interface/town_kaarg/maingates/m%04d.bmp";
    private static final String KAARG_DOOR_OPEN_WAV = "sfx/town_kaarg/kdoor1.wav";
    private static final String KAARG_DOOR_CLOSE_WAV = "sfx/town_kaarg/kdoor2.wav";
    private static final String KAARG_WOMAN_SCRIPT_FORMAT = "kaargwoman%d";
    private static final String KAARG_GUARD_SCRIPT_FORMAT = "kaargguard%d";
    private static final String KAARG_MAN_SCRIPT_FORMAT = "kaargman%d";

    private static final String[] KAARG_AMBIENT_SOUND_PATHS = {
            "sfx/town_kaarg/kvox2.wav",
            "sfx/town_kaarg/kvox3.wav",
            "sfx/town_kaarg/kvox4.wav",
            "sfx/town_kaarg/kbird1.wav",
            "sfx/town_kaarg/kbird2.wav",
            "sfx/town_kaarg/kbird3.wav",
            "sfx/town_kaarg/kbird4.wav",
            "sfx/town_kaarg/kvox1.wav",
            "sfx/town_kaarg/kenter2.wav",
            "sfx/town_kaarg/kenter1.wav",
            "sfx/town_kaarg/kman1.wav",
            "sfx/town_kaarg/ksteps2.wav",
            "sfx/town_kaarg/ksteps21.wav",
            "sfx/town_kaarg/ksteps1.wav",
            "sfx/town_kaarg/ksteps11.wav",
            "sfx/town_kaarg/ksteps3.wav",
            "sfx/town_kaarg/ksteps31.wav"
    };

    private static final int VOICE_2_SOUND_INDEX = 0;
    private static final int VOICE_3_SOUND_INDEX = 1;
    private static final int VOICE_4_SOUND_INDEX = 2;
    private static final int BIRD_1_SOUND_INDEX = 3;
    private static final int BIRD_2_SOUND_INDEX = 4;
    private static final int BIRD_3_SOUND_INDEX = 5;
    private static final int BIRD_4_SOUND_INDEX = 6;
    private static final int VOICE_1_SOUND_INDEX = 7;
    private static final int SHOP_ENTER_SOUND_INDEX = 8;
    private static final int INN_ENTER_SOUND_INDEX = 9;
    private static final int DERVISH_SOUND_INDEX = 10;
    private static final int GUARD_STEP_2_SOUND_INDEX = 11;
    private static final int GUARD_STEP_21_SOUND_INDEX = 12;
    private static final int GUARD_STEP_1_SOUND_INDEX = 13;
    private static final int GUARD_STEP_11_SOUND_INDEX = 14;
    private static final int GUARD_STEP_3_SOUND_INDEX = 15;
    private static final int GUARD_STEP_31_SOUND_INDEX = 16;

    private static boolean hoverRefreshInitialized;
    private static int lastHoverRefreshTick;
    private static boolean unusedUpdateDelayInitialized;
    private static int unusedUpdateDelayMs;
    private static boolean nextVoiceAmbientDelayInitialized;
    private static int nextVoiceAmbientDelayMs;
    private static boolean nextBirdAmbientDelayInitialized;
    private static int nextBirdAmbientDelayMs;

    //0x20c..0x24c
    public final Sound[] ambientSounds = new Sound[KAARG_AMBIENT_SOUND_PATHS.length];
    //0x250
    public int innHoverSoundPlayedFlag;
    //0x254
    public int exitHoverSoundResetFlag;
    //0x258
    public int guardStepSoundPlayedFlag;
    //0x25c
    public int lastBirdAmbientTick;
    //0x260
    public int lastDervishSoundTick;
    //0x264
    public final List<CBmp64k> guardFrameBitmaps = new ArrayList<>();
    //0x278
    public int lastGuardAmbientTick;
    //0x27c
    public int nextGuardAmbientDelayMs;
    //0x280
    public final List<CBmp64k> dervishFrameBitmaps = new ArrayList<>();
    //0x294
    public int lastDervishAnimationTick;
    //0x298
    public int nextDervishAnimationDelayMs;
    //0x29c
    public final List<List<CBmp64k>> girl1FrameGroups = new ArrayList<>();
    //0x2c4
    public int girl1AnimationGroup;
    //0x2c8
    public final List<List<CBmp64k>> girl2FrameGroups = new ArrayList<>();
    //0x2f0
    public int girl2AnimationGroup;
    //0x2f4
    public int girl2AnimationFrame;
    //0x2f8
    public int lastGirl2AmbientTick;
    //0x2fc
    public int nextGirl2AmbientDelayMs;
    //0x300
    public final List<CBmp64k> mainGateFrameBitmaps = new ArrayList<>();

    /**
     * Native: KaargTownDialogVisualObject::KaargTownDialogVisualObject @004D09EE.
     * Also covers the shared Kaarg-owned initialization from constructor overloads @004D0852 and @004D0BA0;
     * those overloads differ only by the BasicTownDialogVisualObject constructor they invoke.
     * Fully ported.
     */
    public KaargTownDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        super(id, xLeft, yTop, xRight, yBottom);
        dervishFrameBitmaps.clear();
        guardFrameBitmaps.clear();
        girl1FrameGroups.clear();
        girl2FrameGroups.clear();
        mainGateFrameBitmaps.clear();
        for (int i = 0; i < 2; i++) {
            girl1FrameGroups.add(new ArrayList<>());
            girl2FrameGroups.add(new ArrayList<>());
        }
        innHoverSoundPlayedFlag = 0;
        exitHoverSoundResetFlag = 0;
        guardStepSoundPlayedFlag = 0;
        lastBirdAmbientTick = 0;
        lastDervishSoundTick = 0;
        lastDervishAnimationTick = 0;
        nextDervishAnimationDelayMs = 0;
        lastGuardAmbientTick = 0;
        nextGuardAmbientDelayMs = 0;
        girl1AnimationGroup = 0;
        girl2AnimationFrame = 0;
        girl2AnimationGroup = 0;
        lastGirl2AmbientTick = 0;
        nextGirl2AmbientDelayMs = 0;
    }

    /**
     * vtbl +0x14: KaargTownDialogVisualObject::GetText @004D31BD.
     * Fully ported.
     */
    @Override
    public String getText() {
        if (dialogActiveFlag == 0) {
            return null;
        }

        int hoveredActionMask = getHoveredActionMaskAtPoint(
                Globals.mousePointer.getX(),
                Globals.mousePointer.getY()
        );
        return switch (hoveredActionMask) {
            case 1 -> get(MAIN_SHOP_233);
            case 2 -> get(MAIN_INN_236);
            case 4 -> get(MAIN_GIRL_360);
            case 8 -> get(MAIN_START_MISSION_237);
            case 0x10 -> get(MAIN_CLICK_TO_BRING_UP_THE_MAIN_MENU_235);
            case 0x200 -> get(MAIN_GIRL_359);
            case 0x400 -> get(MAIN_SLEEPING_MAN_362);
            case 0x800 -> get(MAIN_GUARD_361);
            default -> null;
        };
    }

    /**
     * vtbl +0x2C: KaargTownDialogVisualObject::Update @004D1F6D.
     * Fully ported.
     */
    @Override
    public void update() {
        if (dialogActiveFlag == 0) {
            return;
        }

        ensureHoverRefreshInitialized();
        int now = currentTick();
        if (hasElapsed(now, lastHoverRefreshTick, HOVER_REFRESH_INTERVAL_MS)) {
            updateHoveredAction(Globals.mousePointer.getX(), Globals.mousePointer.getY());
            advanceAmbientAnimations();
            lastHoverRefreshTick = currentTick();
        }

        updateRandomAmbientSelections();
        Globals.renderer.lockSurface();
        try {
            renderKaargTownScene();
        } finally {
            Globals.renderer.unlockSurface();
        }
        updateHandlerChildren();
    }

    /**
     * vtbl +0x54: KaargTownDialogVisualObject::OnLButtonDown @004D2FA3.
     * Fully ported.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        int hoveredActionMask = getHoveredActionMaskAtPoint(x, y);
        switch (hoveredActionMask) {
            case 1 -> {
                closeDialog();
                postMainWindowMessage(MessageCodes.SHOW_SHOP_DIALOG, 0, 0);
            }
            case 2 -> {
                closeDialog();
                postMainWindowMessage(MessageCodes.SHOW_INN_DIALOG, 0, 0);
            }
            case 4, 0x200 -> showTownPlacard(KAARG_WOMAN_SCRIPT_FORMAT);
            case 8 -> {
                closeDialog();
                postMainWindowMessage(MessageCodes.WRITE_CURRENT_MISSION_RESUME_SAVE, 1, 0);
                postMainWindowMessage(MessageCodes.SHOW_GLOBAL_MAP_DIALOG, 0, 0);
            }
            case 0x10 -> postMainWindowMessage(MessageCodes.SHOW_TOWN_MENU, 0, 0);
            case 0x400 -> showTownPlacard(KAARG_MAN_SCRIPT_FORMAT);
            case 0x800 -> showTownPlacard(KAARG_GUARD_SCRIPT_FORMAT);
            default -> {
            }
        }
        return 1;
    }

    /**
     * vtbl +0x80: KaargTownDialogVisualObject::showDialog @004D0D46.
     * Fully ported.
     */
    @Override
    public void showDialog() {
        Globals.mousePointer.disableBackgroundCapture();
        loadTownAssets();
        loadAmbientSounds();
        townAnimationFlags = 0;
        doorOpenFlag = 0;
        guardSoundState = 0;

        if (!shouldShowTipsPrompt()) {
            clearTipsPromptChild();
        } else {
            tipsPromptDialog = new TipsPromptDialogVisualObject(
                    0x467,
                    0x148,
                    0,
                    0x280,
                    200,
                    ScriptDataSupport.getTipText(0x0C)
            );
            addChild(tipsPromptDialog);
        }

        girl2AnimationFrame = -1;
        girl2AnimationGroup = -1;
        lastBabyBirdAnimationTick = currentTick();
        nextBabyBirdAnimationDelayMs = Utils.randInclusive(RANDOM_SHORT_DELAY_BASE_MS, RANDOM_SHORT_DELAY_BASE_MS + 1999);
        lastGirl2AmbientTick = currentTick();
        nextGirl2AmbientDelayMs = Utils.randInclusive(RANDOM_SHORT_DELAY_BASE_MS, RANDOM_SHORT_DELAY_BASE_MS + 1999);
        lastGuardAmbientTick = currentTick();
        nextGuardAmbientDelayMs = Utils.randInclusive(RANDOM_SHORT_DELAY_BASE_MS, RANDOM_SHORT_DELAY_BASE_MS + 1999);
        lastDervishAnimationTick = currentTick();
        nextDervishAnimationDelayMs = Utils.randInclusive(4000, 4000 + RANDOM_DERVISH_DELAY_SPAN_MS - 1);
        hoveredActionMask = -1;

        clearScreen();
        showHandlerDialog();
        dialogActiveFlag = 1;
        draw();
        primeInitialVoiceSound();
        Globals.mousePointer.enableBackgroundCapture();
    }

    /**
     * vtbl +0x88: KaargTownDialogVisualObject::LoadAmbientSounds @004D1C83.
     * Fully ported.
     */
    @Override
    public void loadAmbientSounds() {
        releaseAmbientSounds();
        for (int i = 0; i < ambientSounds.length; i++) {
            ambientSounds[i] = new Sound(KAARG_AMBIENT_SOUND_PATHS[i]);
        }
        innHoverSoundPlayedFlag = 0;
        shopHoverSoundPlayedFlag = 0;
        exitHoverSoundResetFlag = 0;
    }

    /**
     * vtbl +0x8C: KaargTownDialogVisualObject::ReleaseAmbientSounds @004D1E36.
     * Fully ported.
     */
    @Override
    public void releaseAmbientSounds() {
        for (int i = 0; i < ambientSounds.length; i++) {
            ambientSounds[i] = null;
        }
    }

    /**
     * vtbl +0x90: KaargTownDialogVisualObject::primeAmbientCrowdSound @004D1C69.
     * Fully ported. Native dispatch only probes the inherited ambient slot at `this+0x74`, so Java delegates to the parent implementation and preserves the same no-op behavior while that shared slot remains unset for Kaarg town.
     */
    @Override
    public void primeAmbientCrowdSound() {
        super.primeAmbientCrowdSound();
    }

    /**
     * Native support extracted from KaargTownDialogVisualObject::showDialog @004D0D46 direct Sound::PlayLoopPointer @004A0FF0 call on voice1Sound.
     */
    private void primeInitialVoiceSound() {
        Sound voice1Sound = ambientSounds[VOICE_1_SOUND_INDEX];
        if (voice1Sound == null) {
            return;
        }
        voice1Sound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, true, Sound.POINTER_SFX_PRIORITY, 0);
    }

    /**
     * vtbl +0x98: KaargTownDialogVisualObject::updateHoveredAction @004D244E.
     * Fully ported.
     */
    @Override
    public BasicTownDialogVisualObject updateHoveredAction(int x, int y) {
        int hoveredActionMask = getHoveredActionMaskAtPoint(x, y);
        guardFrameStep = 1;
        this.hoveredActionMask = hoveredActionMask;

        if (hoveredActionMask < 9) {
            switch (hoveredActionMask) {
                case 1 -> {
                    if (shopHoverSoundPlayedFlag == 0) {
                        stopPointerSound(INN_ENTER_SOUND_INDEX);
                        playPointerSound(SHOP_ENTER_SOUND_INDEX);
                        shopHoverSoundPlayedFlag = 1;
                    }
                    innHoverSoundPlayedFlag = 0;
                    exitHoverSoundResetFlag = 0;
                }
                case 2 -> {
                    if (innHoverSoundPlayedFlag == 0) {
                        stopPointerSound(SHOP_ENTER_SOUND_INDEX);
                        playPointerSound(INN_ENTER_SOUND_INDEX);
                        innHoverSoundPlayedFlag = 1;
                    }
                    shopHoverSoundPlayedFlag = 0;
                    exitHoverSoundResetFlag = 0;
                }
                case 4 -> {
                    return this;
                }
                case 8 -> {
                    innHoverSoundPlayedFlag = 0;
                    shopHoverSoundPlayedFlag = 0;
                    if (exitHoverSoundResetFlag == 0) {
                        stopPointerSound(SHOP_ENTER_SOUND_INDEX);
                        stopPointerSound(INN_ENTER_SOUND_INDEX);
                        exitHoverSoundResetFlag = 1;
                    }
                    return this;
                }
                case -1 -> {
                    restoreDefaultCursor();
                    innHoverSoundPlayedFlag = 0;
                    shopHoverSoundPlayedFlag = 0;
                    exitHoverSoundResetFlag = 0;
                }
                default -> {
                    townAnimationFlags |= hoveredActionMask;
                    innHoverSoundPlayedFlag = 0;
                    shopHoverSoundPlayedFlag = 0;
                    exitHoverSoundResetFlag = 0;
                }
            }
            return this;
        }

        if (hoveredActionMask == 0x200 || hoveredActionMask == 0x400 || hoveredActionMask == 0x800) {
            return this;
        }

        townAnimationFlags |= hoveredActionMask;
        innHoverSoundPlayedFlag = 0;
        shopHoverSoundPlayedFlag = 0;
        exitHoverSoundResetFlag = 0;
        return this;
    }

    /**
     * Native support extracted from KaargTownDialogVisualObject::updateHoveredAction @004D244E
     * call sites to Sound::StopAndRewindPointerSound @004385B0.
     */
    private void stopPointerSound(int soundIndex) {
        Sound sound = ambientSounds[soundIndex];
        if (sound != null && sound.isPlaying()) {
            sound.stop();
        }
    }

    /**
     * Native support extracted from KaargTownDialogVisualObject::updateHoveredAction @004D244E
     * call sites to Sound::PlayPointer @00438570.
     */
    private void playPointerSound(int soundIndex) {
        Sound sound = ambientSounds[soundIndex];
        if (sound == null) {
            return;
        }
        sound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
    }

    /**
     * vtbl +0x9C: KaargTownDialogVisualObject::UpdateRandomAmbientSelections @004D26A4.
     * Fully ported.
     */
    @Override
    public void updateRandomAmbientSelections() {
        int now = currentTick();
        if (hasElapsed(now, lastDervishAnimationTick, nextDervishAnimationDelayMs)) {
            nextDervishAnimationDelayMs =
                    Utils.randInclusive(RANDOM_DERVISH_DELAY_BASE_MS, RANDOM_DERVISH_DELAY_BASE_MS + RANDOM_DERVISH_DELAY_SPAN_MS - 1);
            lastDervishAnimationTick = now;
            dervishAnimationFrame = 0;
            townAnimationFlags |= 0x400;
        }
        if (hasElapsed(now, lastBabyBirdAnimationTick, nextBabyBirdAnimationDelayMs)) {
            nextBabyBirdAnimationDelayMs = Utils.randInclusive(
                    RANDOM_CHARACTER_DELAY_BASE_MS,
                    RANDOM_CHARACTER_DELAY_BASE_MS + RANDOM_AMBIENT_DELAY_SPAN_MS - 1);
            lastBabyBirdAnimationTick = now;
            babyBirdAnimationFrame = 0;
            girl1AnimationGroup = Utils.randInclusive(1);
            townAnimationFlags |= 0x200;
        }
        if (hasElapsed(now, lastGirl2AmbientTick, nextGirl2AmbientDelayMs)) {
            nextGirl2AmbientDelayMs =
                    Utils.randInclusive(
                            RANDOM_CHARACTER_DELAY_BASE_MS,
                            RANDOM_CHARACTER_DELAY_BASE_MS + RANDOM_AMBIENT_DELAY_SPAN_MS - 1);
            lastGirl2AmbientTick = now;
            girl2AnimationFrame = 0;
            girl2AnimationGroup = Utils.randInclusive(1);
            townAnimationFlags |= 4;
        }
        if (hasElapsed(now, lastGuardAmbientTick, nextGuardAmbientDelayMs)) {
            nextGuardAmbientDelayMs =
                    Utils.randInclusive(RANDOM_GUARD_DELAY_BASE_MS, RANDOM_GUARD_DELAY_BASE_MS + RANDOM_AMBIENT_DELAY_SPAN_MS - 1);
            lastGuardAmbientTick = now;
            fighterAnimationFrame = 0;
            townAnimationFlags |= 0x800;
        }

        ensureAmbientDelayInitialized();
        if (hasElapsed(now, lastAmbientBirdTick, nextVoiceAmbientDelayMs)) {
            activeAmbientBirdCount = Utils.randInclusive(1, 3);
            if (activeAmbientBirdCount == 1) {
                ambientSounds[VOICE_2_SOUND_INDEX].playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
            } else if (activeAmbientBirdCount == 2) {
                ambientSounds[VOICE_3_SOUND_INDEX].playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
            } else if (activeAmbientBirdCount == 3) {
                ambientSounds[VOICE_4_SOUND_INDEX].playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
            }
            nextVoiceAmbientDelayMs =
                    Utils.randInclusive(
                            RANDOM_GLOBAL_AMBIENT_DELAY_BASE_MS,
                            RANDOM_GLOBAL_AMBIENT_DELAY_BASE_MS + RANDOM_GLOBAL_AMBIENT_DELAY_SPAN_MS - 1);
            lastAmbientBirdTick = currentTick();
        }
        if (hasElapsed(now, lastBirdAmbientTick, nextBirdAmbientDelayMs)) {
            int birdSoundIndex = Utils.randInclusive(3);
            switch (birdSoundIndex) {
                case 0 -> ambientSounds[BIRD_1_SOUND_INDEX].playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
                case 1 -> ambientSounds[BIRD_2_SOUND_INDEX].playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
                case 2 -> ambientSounds[BIRD_3_SOUND_INDEX].playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
                case 3 -> ambientSounds[BIRD_4_SOUND_INDEX].playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
                default -> {
                }
            }
            nextBirdAmbientDelayMs =
                    Utils.randInclusive(
                            RANDOM_GLOBAL_AMBIENT_DELAY_BASE_MS,
                            RANDOM_GLOBAL_AMBIENT_DELAY_BASE_MS + RANDOM_GLOBAL_AMBIENT_DELAY_SPAN_MS - 1);
            lastBirdAmbientTick = currentTick();
        }
        if (hasElapsed(now, lastDervishSoundTick, DERVISH_SOUND_REPEAT_MS)) {
            ambientSounds[DERVISH_SOUND_INDEX].playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
            lastDervishSoundTick = currentTick();
        }
        if ((townAnimationFlags & 0x800) != 0) {
            switch (fighterAnimationFrame) {
                case 5, 0x0D, 0x15, 0x2F, 0x37, 0x3F -> {
                    if (guardStepSoundPlayedFlag == 0) {
                        if (Utils.randInclusive(3) == 0) {
                            ambientSounds[GUARD_STEP_21_SOUND_INDEX].playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
                        } else {
                            ambientSounds[GUARD_STEP_2_SOUND_INDEX].playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
                        }
                        guardStepSoundPlayedFlag = 1;
                    }
                }
                case 9, 0x11, 0x17, 0x33, 0x3B -> {
                    if (guardStepSoundPlayedFlag == 0) {
                        if (Utils.randInclusive(3) == 0) {
                            ambientSounds[GUARD_STEP_11_SOUND_INDEX].playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
                        } else {
                            ambientSounds[GUARD_STEP_1_SOUND_INDEX].playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
                        }
                        guardStepSoundPlayedFlag = 1;
                    }
                }
                case 0x1F, 0x45 -> {
                    if (guardStepSoundPlayedFlag == 0) {
                        if (Utils.randInclusive(3) == 0) {
                            ambientSounds[GUARD_STEP_31_SOUND_INDEX].playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
                        } else {
                            ambientSounds[GUARD_STEP_3_SOUND_INDEX].playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
                        }
                        guardStepSoundPlayedFlag = 1;
                    }
                }
                default -> guardStepSoundPlayedFlag = 0;
            }
        }
    }

    /**
     * vtbl +0xA0: KaargTownDialogVisualObject::LoadTownAssets @004D101A.
     * Fully ported.
     */
    @Override
    public void loadTownAssets() {
        releaseTownAssets();
        hoverMaskBitmap = loadBmp256(KAARG_TOWN_MASK_BMP);
        Globals.mousePointer.update();
        mainBackgroundBitmap = loadBmp64k(KAARG_TOWN_MAIN_BMP);
        Globals.mousePointer.update();
        tavernHighlightBitmap = loadBmp64k(KAARG_TAVERN_HIGHLIGHT_BMP);
        shopHighlightBitmap = loadBmp64k(KAARG_SHOP_HIGHLIGHT_BMP);
        Globals.mousePointer.update();
        loadBitmapFrames(dervishFrameBitmaps, DERVISH_FRAME_PATH_FORMAT, DERVISH_FRAME_MAX_COUNT);
        loadBitmapFrames(guardFrameBitmaps, GUARD_FRAME_PATH_FORMAT, GUARD_FRAME_MAX_COUNT);
        loadGroupedBitmapFrames(girl1FrameGroups, GIRL1_FRAME_PATH_FORMAT, GIRL_FRAME_MAX_COUNT);
        loadGroupedBitmapFrames(girl2FrameGroups, GIRL2_FRAME_PATH_FORMAT, GIRL_FRAME_MAX_COUNT);
        loadBitmapFrames(mainGateFrameBitmaps, MAINGATE_FRAME_PATH_FORMAT, MAINGATE_FRAME_COUNT);
        tavernkeeperAnimationFrame = -1;
        signAnimationFrame = -1;
        doorAnimationFrame = 0;
        starsAnimationFrame = -1;
        fighterAnimationFrame = -1;
        mageAnimationFrame = 0;
        shopkeeperAnimationFrame = -1;
        flugelAnimationFrame = -1;
        babyBirdAnimationFrame = -1;
        girl1AnimationGroup = -1;
        horseAnimationFrame = -1;
        dervishAnimationFrame = -1;
    }

    /**
     * vtbl +0xA4: KaargTownDialogVisualObject::ReleaseTownAssets @004D18C6.
     * Fully ported.
     */
    @Override
    public void releaseTownAssets() {
        mainBackgroundBitmap = null;
        hoverMaskBitmap = null;
        tavernHighlightBitmap = releaseBmp(tavernHighlightBitmap);
        shopHighlightBitmap = releaseBmp(shopHighlightBitmap);
        releaseBitmapFrames(dervishFrameBitmaps);
        releaseBitmapFrames(guardFrameBitmaps);
        releaseGroupedBitmapFrames(girl1FrameGroups);
        releaseGroupedBitmapFrames(girl2FrameGroups);
        releaseBitmapFrames(mainGateFrameBitmaps);
    }

    /**
     * vtbl +0xA8: KaargTownDialogVisualObject::AdvanceAmbientAnimations @004D2F27.
     * Fully ported.
     */
    @Override
    public void advanceAmbientAnimations() {
        if ((townAnimationFlags & 0x200) != 0) {
            advanceGirl1Animation();
        }
        if ((townAnimationFlags & 4) != 0) {
            advanceGirl2Animation();
        }
        if ((townAnimationFlags & 0x400) != 0) {
            advanceDervishAnimation();
        }
        if ((townAnimationFlags & 0x800) != 0) {
            advanceGuardAnimation();
        }
        advanceMainGatesAnimation();
    }

    /**
     * Native support helper backing KaargTownDialogVisualObject::Update @004D1F6D.
     * Fully ported Kaarg-town background, highlights, girl groups, guard, dervish, and maingates rendering path.
     */
    private void renderKaargTownScene() {
        int screenLeft = cRect.left;
        int screenTop = cRect.top;
        if (mainBackgroundBitmap != null) {
            mainBackgroundBitmap.draw(screenLeft, screenTop, 0, null, false);
        }
        if (hoveredActionMask == 1) {
            shopHighlightBitmap.draw(screenLeft + 0x148, screenTop + 0x100, 0, null, false);
        }
        if (hoveredActionMask == 2) {
            tavernHighlightBitmap.draw(screenLeft + 0x1E0, screenTop + 0xC4, 0, null, false);
        }
        if ((townAnimationFlags & 0x200) == 0) {
            girl1FrameGroups.get(0).get(0).draw(screenLeft + 0xD8, screenTop + 0x11C, 0, null, false);
        } else {
            girl1FrameGroups
                    .get(girl1AnimationGroup)
                    .get(babyBirdAnimationFrame)
                    .draw(screenLeft + 0xD8, screenTop + 0x11C, 0, null, false);
        }
        if ((townAnimationFlags & 4) == 0) {
            girl2FrameGroups.get(0).get(0).draw(screenLeft + 0x104, screenTop + 0x11C, 0, null, false);
        } else {
            girl2FrameGroups
                    .get(girl2AnimationGroup)
                    .get(girl2AnimationFrame)
                    .draw(screenLeft + 0x104, screenTop + 0x11C, 0, null, false);
        }
        if ((townAnimationFlags & 0x800) == 0) {
            guardFrameBitmaps.get(0).draw(screenLeft + 0xB8, screenTop + 0x9C, 0, null, false);
        } else if (fighterAnimationFrame < 0x0D || 0x25 < fighterAnimationFrame) {
            guardFrameBitmaps.get(fighterAnimationFrame).draw(screenLeft + 0xB8, screenTop + 0x9C, 0, null, false);
        } else {
            guardFrameBitmaps.get(fighterAnimationFrame).draw(screenLeft + 0x8C, screenTop + 0x98, 0, null, false);
        }
        if ((townAnimationFlags & 0x400) == 0) {
            dervishFrameBitmaps.get(0).draw(screenLeft + 0x1A0, screenTop + 0x148, 0, null, false);
        } else {
            dervishFrameBitmaps.get(dervishAnimationFrame).draw(screenLeft + 0x1A0, screenTop + 0x148, 0, null, false);
        }
        mainGateFrameBitmaps.get(doorAnimationFrame).draw(screenLeft + 0x98, screenTop + 0x100, 0, null, false);
    }

    /**
     * Native helper: KaargTownDialogVisualObject::advanceGirl1Animation @004D2D87.
     * Fully ported.
     */
    private void advanceGirl1Animation() {
        babyBirdAnimationFrame += 1;
        if (babyBirdAnimationFrame == getBitmapGroupSize(girl1FrameGroups, girl1AnimationGroup)) {
            babyBirdAnimationFrame = 0;
            girl1AnimationGroup = -1;
            townAnimationFlags &= ~0x200;
        }
    }

    /**
     * Native helper: KaargTownDialogVisualObject::advanceGirl2Animation @004D2DFC.
     * Fully ported.
     */
    private void advanceGirl2Animation() {
        girl2AnimationFrame += 1;
        if (girl2AnimationFrame == getBitmapGroupSize(girl2FrameGroups, girl2AnimationGroup)) {
            girl2AnimationFrame = 0;
            girl2AnimationGroup = -1;
            townAnimationFlags &= ~4;
        }
    }

    /**
     * Native helper: KaargTownDialogVisualObject::advanceDervishAnimation @004D2ECC.
     * Fully ported.
     */
    private void advanceDervishAnimation() {
        dervishAnimationFrame += 1;
        if (dervishAnimationFrame == dervishFrameBitmaps.size()) {
            dervishAnimationFrame = 0;
            townAnimationFlags &= ~0x400;
        }
    }

    /**
     * Native helper: KaargTownDialogVisualObject::advanceGuardAnimation @004D2E71.
     * Fully ported.
     */
    private void advanceGuardAnimation() {
        fighterAnimationFrame += 1;
        if (fighterAnimationFrame == guardFrameBitmaps.size()) {
            fighterAnimationFrame = 0;
            townAnimationFlags &= ~0x800;
        }
    }

    /**
     * Native helper: KaargTownDialogVisualObject::advanceMainGatesAnimation @004D2C2C.
     * Fully ported.
     */
    private void advanceMainGatesAnimation() {
        int hoveredActionMask = getHoveredActionMaskAtPoint(
                Globals.mousePointer.getX(),
                Globals.mousePointer.getY()
        );
        if (hoveredActionMask == 8) {
            if (doorOpenFlag == 0) {
                playDoorSound(KAARG_DOOR_OPEN_WAV);
            }
            doorOpenFlag = 1;
            doorAnimationFrame += 1;
            if (doorAnimationFrame > MAINGATE_FRAME_COUNT - 2) {
                doorAnimationFrame = MAINGATE_FRAME_COUNT - 1;
                townAnimationFlags &= ~8;
            }
            return;
        }

        if (doorOpenFlag != 0) {
            playDoorSound(KAARG_DOOR_CLOSE_WAV);
        }
        doorOpenFlag = 0;
        doorAnimationFrame -= 1;
        if (doorAnimationFrame < 1) {
            doorAnimationFrame = 0;
            townAnimationFlags &= ~8;
        }
    }

    /**
     * Native support extracted from ShowRoleKeyDialog(dynamic placard script) @0041D15E call sites in
     * KaargTownDialogVisualObject::OnLButtonDown @004D2FA3.
     */
    private void showTownPlacardScript(String scriptName) {
        RoleDialogSupport.showRoleKeyDialog(scriptName);
    }

    /**
     * Java helper around the Kaarg placard format strings used by KaargTownDialogVisualObject::OnLButtonDown @004D2FA3.
     * Partial port. Java sources the dynamic placard variant directly from ScenarioLib; the placard dialog dispatch is
     * handled by the shared ShowRoleKeyDialog port.
     */
    private void showTownPlacard(String scriptNameFormat) {
        int townScriptVariant = Globals.scenarioLib.getVar(SCENARIO_CHAPTER_VAR_ID);
        showTownPlacardScript(String.format(Locale.ROOT, scriptNameFormat, townScriptVariant));
    }

    /**
     * Native support extracted from KaargTownDialogVisualObject::Update @004D1F6D for static hover-refresh and unused-delay slots.
     */
    private static void ensureHoverRefreshInitialized() {
        if (!hoverRefreshInitialized) {
            hoverRefreshInitialized = true;
            lastHoverRefreshTick = currentTick();
        }
        if (!unusedUpdateDelayInitialized) {
            unusedUpdateDelayInitialized = true;
            unusedUpdateDelayMs = Utils.randBased(RANDOM_UNUSED_UPDATE_DELAY_BASE_MS, RANDOM_GLOBAL_AMBIENT_DELAY_SPAN_MS);
        }
    }

    /**
     * Native support extracted from KaargTownDialogVisualObject::UpdateRandomAmbientSelections @004D26A4 for shared static Kaarg ambient-delay slots.
     */
    private static void ensureAmbientDelayInitialized() {
        if (!nextVoiceAmbientDelayInitialized) {
            nextVoiceAmbientDelayInitialized = true;
            nextVoiceAmbientDelayMs =
                    Utils.randInclusive(
                            RANDOM_GLOBAL_AMBIENT_DELAY_BASE_MS,
                            RANDOM_GLOBAL_AMBIENT_DELAY_BASE_MS + RANDOM_GLOBAL_AMBIENT_DELAY_SPAN_MS - 1);
        }
        if (!nextBirdAmbientDelayInitialized) {
            nextBirdAmbientDelayInitialized = true;
            nextBirdAmbientDelayMs =
                    Utils.randInclusive(
                            RANDOM_GLOBAL_AMBIENT_DELAY_BASE_MS,
                            RANDOM_GLOBAL_AMBIENT_DELAY_BASE_MS + RANDOM_GLOBAL_AMBIENT_DELAY_SPAN_MS - 1);
        }
    }

    /**
     * Native support extracted from KaargTownDialogVisualObject::LoadTownAssets @004D101A for repeated Kaarg-town sequential `CBmp64k` loads.
     */
    private static void loadBitmapFrames(List<CBmp64k> targetFrames, String resourcePathFormat, int maxFrameCount) {
        targetFrames.clear();
        for (int frameIndex = 0; frameIndex < maxFrameCount; frameIndex++) {
            String resourcePath = String.format(Locale.ROOT, resourcePathFormat, frameIndex);
            if (!Globals.gameFileManager.exists(resourcePath)) {
                break;
            }
            targetFrames.add(loadBmp64k(resourcePath));
            Globals.mousePointer.update();
        }
    }

    /**
     * Native support extracted from KaargTownDialogVisualObject::LoadTownAssets @004D101A for paired Kaarg girl-group bitmap loads.
     */
    private static void loadGroupedBitmapFrames(List<List<CBmp64k>> targetGroups, String resourcePathFormat, int maxFrameIndex) {
        for (int groupIndex = 0; groupIndex < targetGroups.size(); groupIndex++) {
            List<CBmp64k> group = targetGroups.get(groupIndex);
            group.clear();
            for (int frameIndex = groupIndex; frameIndex < maxFrameIndex; frameIndex++) {
                String resourcePath = String.format(Locale.ROOT, resourcePathFormat, groupIndex + 1, frameIndex);
                if (!Globals.gameFileManager.exists(resourcePath)) {
                    break;
                }
                group.add(loadBmp64k(resourcePath));
                Globals.mousePointer.update();
            }
        }
    }

    /**
     * Native support extracted from KaargTownDialogVisualObject::ReleaseTownAssets @004D18C6 for repeated Kaarg bitmap-list teardown.
     */
    private static void releaseBitmapFrames(List<CBmp64k> targetFrames) {
        for (int frameIndex = 0; frameIndex < targetFrames.size(); frameIndex++) {
            targetFrames.set(frameIndex, releaseBmp(targetFrames.get(frameIndex)));
        }
        targetFrames.clear();
    }

    /**
     * Native support extracted from KaargTownDialogVisualObject::ReleaseTownAssets @004D18C6 for repeated Kaarg grouped-bitmap teardown.
     */
    private static void releaseGroupedBitmapFrames(List<List<CBmp64k>> targetGroups) {
        for (List<CBmp64k> group : targetGroups) {
            releaseBitmapFrames(group);
        }
    }

    /**
     * Native support extracted from KaargTownDialogVisualObject::ReleaseTownAssets @004D18C6 for bitmap destruction sites represented by Java reference clearing.
     */
    private static CBmp64k releaseBmp(@SuppressWarnings("unused") CBmp64k bitmap) {
        return null;
    }

    /**
     * Native support extracted from KaargTownDialogVisualObject::Update @004D1F6D and KaargTownDialogVisualObject::UpdateRandomAmbientSelections @004D26A4 for `timeGetTime`-style unsigned comparisons.
     */
    private static boolean hasElapsed(int now, int lastTick, int delayMs) {
        return Integer.compareUnsigned(now - lastTick, delayMs) > 0;
    }

    /**
     * Native support extracted from KaargTownDialogVisualObject::advanceGirl1Animation @004D2D87 and KaargTownDialogVisualObject::advanceGirl2Animation @004D2DFC for repeated `CArray<CBmp64k>::GetSize` calls.
     */
    private static int getBitmapGroupSize(List<List<CBmp64k>> groups, int groupIndex) {
        if (groupIndex < 0 || groupIndex >= groups.size()) {
            return 0;
        }
        return groups.get(groupIndex).size();
    }

    /**
     * Native support extracted from KaargTownDialogVisualObject::advanceMainGatesAnimation @004D2C2C for `FUN_004384F0(slot, soundPath)` transient door-sound reloads.
     */
    private void playDoorSound(String resourcePath) {
        gateTransientSound = null;
        gateTransientSound = new Sound(resourcePath);
        gateTransientSound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
    }
}
