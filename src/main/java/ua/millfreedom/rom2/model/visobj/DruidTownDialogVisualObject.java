package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.CA16;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.ScriptDataSupport;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.sound.Sound;
import ua.millfreedom.rom2.model.sound.SoundSystem;
import ua.millfreedom.rom2.res.Resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Native class: DruidTownDialogVisualObject.
 * Purpose: druid town-screen dialog (`id=0x3FC`) with druid-specific ambient sounds, hover hotspots, and shop/tavern/lizard scene animations.
 */
public class DruidTownDialogVisualObject extends BasicTownDialogVisualObject {
    public static final int NATIVE_SIZE = 0x320; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int AMBIENT_SOUND_COUNT = 21;
    private static final int RANDOM_AMBIENT_DELAY_SPAN_MS = 5000;
    private static final int RANDOM_CHARACTER_DELAY_BASE_MS = 0xDAC;
    private static final int RANDOM_SHORT_DELAY_BASE_MS = 2000;
    private static final int RANDOM_BUG_DELAY_BASE_MS = 10000;
    private static final int RANDOM_BUG_DELAY_SPAN_MS = 10000;
    private static final int RANDOM_WOLF_REPEAT_MS = 60000;
    private static final int HOVER_REFRESH_INTERVAL_MS = 100;
    private static final int SCENARIO_CHAPTER_VAR_ID = 0x300;
    private static final int BUG_ANIMATION_FRAME_COUNT = 0x3D;
    private static final int DRUID_CHARACTER_FRAME_MAX_COUNT = 20;
    private static final int PALETTE_PAGE_COUNT = 0x10;
    private static final int DRUID_SPRITE_PALETTE_MODE = 4;
    private static final String DRUID_TOWN_MAIN_BMP = "graphics/interface/town_druid/townmain.bmp";
    private static final String DRUID_TOWN_MASK_BMP = "graphics/interface/town_druid/townmask.bmp";
    private static final String DRUID_TAVERN_HIGHLIGHT_BMP = "graphics/interface/town_druid/hili_tavern.bmp";
    private static final String DRUID_SHOP_HIGHLIGHT_BMP = "graphics/interface/town_druid/hili_shop.bmp";
    private static final String DRUID_WOMAN_FRAME_PATH_FORMAT = "graphics/interface/town_druid/woman/a%d%04d.bmp";
    private static final String DRUID_MAN_FRAME_PATH_FORMAT = "graphics/interface/town_druid/man/a%d%04d.bmp";
    private static final String DRUID_BUG_SPRITES_16A = "graphics/interface/town_druid/bug/sprites.16a";
    private static final String DRUID_LIZARD_SPRITES_16A = "graphics/interface/town_druid/lizard/sprites.16a";
    private static final String DRUID_INNKEEPER_SCRIPT_FORMAT = "druidinnkeeper%d";
    private static final String DRUID_SHOPKEEPER_SCRIPT_FORMAT = "druidshopkeeper%d";
    private static final String[] DRUID_AMBIENT_SOUND_PATHS = {
            "sfx/town_druid/dlizard1.wav",
            "sfx/town_druid/dlizard2.wav",
            "sfx/town_druid/dlizard3.wav",
            "sfx/town_druid/dlizard4.wav",
            "sfx/town_druid/dbug1.wav",
            "sfx/town_druid/dbug2.wav",
            "sfx/town_druid/dbug3.wav",
            "sfx/town_druid/dbird1.wav",
            "sfx/town_druid/dbird2.wav",
            "sfx/town_druid/dbird3.wav",
            "sfx/town_druid/dtree1.wav",
            "sfx/town_druid/dtree2.wav",
            "sfx/town_druid/dtree3.wav",
            "sfx/town_druid/dtree4.wav",
            "sfx/town_druid/dforest1.wav",
            "sfx/town_druid/ddruid1.wav",
            "sfx/town_druid/ddruid2.wav",
            "sfx/town_druid/denter2.wav",
            "sfx/town_druid/denter1.wav",
            "sfx/town_druid/dout.wav",
            "sfx/town_druid/dwolf1.wav"
    };
    private static final int LIZARD_IDLE_1_SOUND_INDEX = 0;
    private static final int LIZARD_IDLE_2_SOUND_INDEX = 1;
    private static final int LIZARD_IDLE_3_SOUND_INDEX = 2;
    private static final int LIZARD_IDLE_4_SOUND_INDEX = 3;
    private static final int BUG_1_SOUND_INDEX = 4;
    private static final int BUG_2_SOUND_INDEX = 5;
    private static final int BUG_3_SOUND_INDEX = 6;
    private static final int BIRD_1_SOUND_INDEX = 7;
    private static final int BIRD_2_SOUND_INDEX = 8;
    private static final int BIRD_3_SOUND_INDEX = 9;
    private static final int TREE_1_SOUND_INDEX = 10;
    private static final int TREE_2_SOUND_INDEX = 11;
    private static final int TREE_3_SOUND_INDEX = 12;
    private static final int TREE_4_SOUND_INDEX = 13;
    private static final int FOREST_LOOP_SOUND_INDEX = 14;
    private static final int DRUID_MAN_SOUND_INDEX = 15;
    private static final int DRUID_WOMAN_SOUND_INDEX = 16;
    private static final int SHOP_ENTER_SOUND_INDEX = 17;
    private static final int TAVERN_ENTER_SOUND_INDEX = 18;
    private static final int TOWN_EXIT_SOUND_INDEX = 19;
    private static final int WOLF_1_SOUND_INDEX = 20;
    private static final int[][] LIZARD_ANIMATION_FRAME_TABLE = {
            {39, 40, 41, 42, 41, 40, 39, 38, -1},
            {28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, -1},
            {2, 3, 4, 5, 6, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 22, 23, 24, 25, 26, 27, -1},
            {2, 3, 4, 5, 6, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 9, 10, 11, 12, 13, 14,
                    15, 16, 17, 18, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 20, 21, 22, 23, 24,
                    25, 26, 27, -1}
    };
    private static boolean druidUpdateTimerInitialized;
    private static int lastDruidHoverRefreshTick;
    private static boolean ambientBirdDelayInitialized;
    private static int nextAmbientBirdDelayMs;
    private static boolean ambientTreeDelayInitialized;
    private static int nextAmbientTreeDelayMs;

    //0x20c
    public Sound lizardAmbientSound1;
    //0x210
    public Sound lizardAmbientSound2;
    //0x214
    public Sound lizardAmbientSound3;
    //0x218
    public Sound lizardAmbientSound4;
    //0x21c
    public Sound bugAmbientSound1;
    //0x220
    public Sound bugAmbientSound2;
    //0x224
    public Sound bugAmbientSound3;
    //0x228
    public Sound birdAmbientSound1;
    //0x22c
    public Sound birdAmbientSound2;
    //0x230
    public Sound birdAmbientSound3;
    //0x234
    public Sound treeAmbientSound1;
    //0x238
    public Sound treeAmbientSound2;
    //0x23c
    public Sound treeAmbientSound3;
    //0x240
    public Sound treeAmbientSound4;
    //0x244
    public Sound forestAmbientLoopSound;
    //0x248
    public Sound shopkeeperAmbientSound;
    //0x24c
    public Sound tavernkeeperAmbientSound;
    //0x250
    public Sound druidShopEnterSound;
    //0x254
    public Sound druidTavernEnterSound;
    //0x258
    public Sound druidTownExitSound;
    //0x25c
    public Sound wolfAmbientSound;
    //0x268
    public final List<List<CBmp64k>> tavernkeeperAnimationFrameGroups = new ArrayList<>();
    //0x2b0
    public final List<List<CBmp64k>> shopkeeperAnimationFrameGroups = new ArrayList<>();
    //0x300
    public CA16 bugSprite;
    //0x314
    public CA16 lizardSprite;
    //0x260
    public int tavernHoverSoundPlayedFlag;
    //0x264
    public int missionExitHoverSoundPlayedFlag;
    //0x2a4
    public int tavernkeeperAnimationGroup;
    //0x2a8
    public int lastTavernkeeperAmbientTick;
    //0x2ac
    public int nextTavernkeeperAmbientDelayMs;
    //0x2ec
    public int shopkeeperAnimationGroup;
    //0x2f0
    public int lastShopkeeperAmbientTick;
    //0x2f4
    public int nextShopkeeperAmbientDelayMs;
    //0x2f8
    public int lastTreeAmbientTick;
    //0x2fc
    public int lastWolfAmbientTick;
    //0x304
    public int lastBugAmbientTick;
    //0x308
    public int nextBugAmbientDelayMs;
    //0x30c
    public int bugAnimationVariant;
    //0x310
    public int bugAnimationFrame;
    //0x318
    public int lizardAnimationVariant;
    //0x31c
    public int lizardAnimationFrame;

    /**
     * Native: DruidTownDialogVisualObject::DruidTownDialogVisualObject @004CE105.
     * Fully ported.
     */
    public DruidTownDialogVisualObject() {
        super();
        initializeDruidFields();
    }

    /**
     * Native: DruidTownDialogVisualObject::DruidTownDialogVisualObject @004CE2B9.
     * Fully ported.
     */
    public DruidTownDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        super(id, xLeft, yTop, xRight, yBottom);
        initializeDruidFields();
    }

    /**
     * Native: DruidTownDialogVisualObject::DruidTownDialogVisualObject @004CE483.
     * Fully ported.
     */
    public DruidTownDialogVisualObject(int id, CRect rect) {
        super(id, rect);
        initializeDruidFields();
    }

    /**
     * Native support extracted from DruidTownDialogVisualObject constructors @004CE105, @004CE2B9, and @004CE483.
     * Full support port.
     */
    private void initializeDruidFields() {
        tavernkeeperAnimationFrameGroups.clear();
        shopkeeperAnimationFrameGroups.clear();
        for (int i = 0; i < 3; i++) {
            tavernkeeperAnimationFrameGroups.add(new ArrayList<>());
            shopkeeperAnimationFrameGroups.add(new ArrayList<>());
        }
        lizardAmbientSound1 = null;
        lizardAmbientSound2 = null;
        lizardAmbientSound3 = null;
        lizardAmbientSound4 = null;
        bugAmbientSound1 = null;
        bugAmbientSound2 = null;
        bugAmbientSound3 = null;
        birdAmbientSound1 = null;
        birdAmbientSound2 = null;
        birdAmbientSound3 = null;
        treeAmbientSound1 = null;
        treeAmbientSound2 = null;
        treeAmbientSound3 = null;
        treeAmbientSound4 = null;
        forestAmbientLoopSound = null;
        shopkeeperAmbientSound = null;
        tavernkeeperAmbientSound = null;
        druidShopEnterSound = null;
        druidTavernEnterSound = null;
        druidTownExitSound = null;
        wolfAmbientSound = null;
        bugSprite = null;
        lizardSprite = null;
        tavernHoverSoundPlayedFlag = 0;
        missionExitHoverSoundPlayedFlag = 0;
        tavernkeeperAnimationGroup = 0;
        lastTavernkeeperAmbientTick = 0;
        nextTavernkeeperAmbientDelayMs = 0;
        shopkeeperAnimationGroup = 0;
        lastShopkeeperAmbientTick = 0;
        nextShopkeeperAmbientDelayMs = 0;
        lastTreeAmbientTick = 0;
        lastWolfAmbientTick = 0;
        lastBugAmbientTick = 0;
        nextBugAmbientDelayMs = 0;
        bugAnimationVariant = 0;
        bugAnimationFrame = 0;
        lizardAnimationVariant = 0;
        lizardAnimationFrame = 0;
    }

    /**
     * vtbl +0x2C: DruidTownDialogVisualObject::Update @004CF65A.
     * Fully ported.
     */
    @Override
    public void update() {
        if (dialogActiveFlag == 0) {
            return;
        }

        ensureHoverRefreshInitialized();
        int now = currentTick();
        if (hasElapsed(now, lastDruidHoverRefreshTick, HOVER_REFRESH_INTERVAL_MS)) {
            updateHoveredAction(Globals.mousePointer.getX(), Globals.mousePointer.getY());
            advanceAmbientAnimations();
            lastDruidHoverRefreshTick = currentTick();
        }

        updateRandomAmbientSelections();
        Globals.renderer.lockSurface();
        try {
            renderDruidTownScene();
        } finally {
            Globals.renderer.unlockSurface();
        }
        updateHandlerChildren();
    }

    /**
     * vtbl +0x54: DruidTownDialogVisualObject::onLButtonDown @004D06CC.
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
            case 8 -> {
                closeDialog();
                postMainWindowMessage(MessageCodes.WRITE_CURRENT_MISSION_RESUME_SAVE, 1, 0);
                postMainWindowMessage(MessageCodes.SHOW_GLOBAL_MAP_DIALOG, 0, 0);
            }
            case 0x10 -> postMainWindowMessage(MessageCodes.SHOW_TOWN_MENU, 0, 0);
            case 0x200 -> showTownPlacard(DRUID_INNKEEPER_SCRIPT_FORMAT);
            case 0x1000 -> showTownPlacard(DRUID_SHOPKEEPER_SCRIPT_FORMAT);
            default -> {
            }
        }
        return 1;
    }

    /**
     * vtbl +0x80: DruidTownDialogVisualObject::showDialog @004CE641.
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
                    ScriptDataSupport.getTipText(0x0B)
            );
            addChild(tipsPromptDialog);
        }

        lastShopkeeperAmbientTick = currentTick();
        nextShopkeeperAmbientDelayMs = Utils.randInclusive(RANDOM_SHORT_DELAY_BASE_MS, RANDOM_SHORT_DELAY_BASE_MS + 1999);
        lastTavernkeeperAmbientTick = currentTick();
        nextTavernkeeperAmbientDelayMs = Utils.randInclusive(RANDOM_SHORT_DELAY_BASE_MS, RANDOM_SHORT_DELAY_BASE_MS + 1999);
        lastAmbientBirdTick = currentTick();
        lastTreeAmbientTick = currentTick();
        lastWolfAmbientTick = currentTick();
        nextBugAmbientDelayMs = Utils.randInclusive(7000, 7000 + RANDOM_AMBIENT_DELAY_SPAN_MS - 1);
        lastBugAmbientTick = currentTick();
        bugAnimationFrame = -1;
        lizardAnimationFrame = -1;
        hoveredActionMask = -1;

        clearScreen();
        showHandlerDialog();
        dialogActiveFlag = 1;
        draw();
        primeAmbientCrowdSound();
        Globals.mousePointer.enableBackgroundCapture();
    }

    /**
     * vtbl +0x88: DruidTownDialogVisualObject::loadAmbientSounds @004CF2C0.
     * Fully ported.
     */
    @Override
    public void loadAmbientSounds() {
        releaseAmbientSounds();
        for (int i = 0; i < AMBIENT_SOUND_COUNT; i++) {
            setAmbientSound(i, new Sound(DRUID_AMBIENT_SOUND_PATHS[i]));
        }
        tavernHoverSoundPlayedFlag = 0;
        shopHoverSoundPlayedFlag = 0;
        missionExitHoverSoundPlayedFlag = 0;
    }

    /**
     * vtbl +0x8C: DruidTownDialogVisualObject::releaseAmbientSounds @004CF4DC.
     * Fully ported.
     */
    @Override
    public void releaseAmbientSounds() {
        for (int i = 0; i < AMBIENT_SOUND_COUNT; i++) {
            releaseAmbientSound(i);
        }
    }

    /**
     * vtbl +0x90: DruidTownDialogVisualObject::primeAmbientCrowdSound @004CF2A4.
     * Fully ported.
     */
    @Override
    public void primeAmbientCrowdSound() {
        if (forestAmbientLoopSound != null) {
            forestAmbientLoopSound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, true, Sound.POINTER_SFX_PRIORITY, 0);
        }
    }

    /**
     * vtbl +0x98: DruidTownDialogVisualObject::UpdateHoveredAction @004CFD58.
     * Fully ported.
     */
    @Override
    public BasicTownDialogVisualObject updateHoveredAction(int x, int y) {
        int hoveredActionMask = getHoveredActionMaskAtPoint(x, y);
        int previousHoveredActionMask = this.hoveredActionMask;
        guardFrameStep = 1;
        this.hoveredActionMask = hoveredActionMask;

        if (hoveredActionMask < 9) {
            switch (hoveredActionMask) {
                case 1 -> {
                    if (shopkeeperAnimationGroup != 2 && previousHoveredActionMask != hoveredActionMask) {
                        stopPointerSound(DRUID_MAN_SOUND_INDEX);
                        lastShopkeeperAmbientTick = currentTick();
                        shopkeeperAnimationGroup = 2;
                        shopkeeperAnimationFrame = -1;
                        townAnimationFlags |= hoveredActionMask;
                        advanceShopkeeperAnimation();
                        playPointerSound(DRUID_MAN_SOUND_INDEX);
                    }
                    if (shopHoverSoundPlayedFlag == 0) {
                        stopPointerSound(TAVERN_ENTER_SOUND_INDEX);
                        stopPointerSound(TOWN_EXIT_SOUND_INDEX);
                        playPointerSound(SHOP_ENTER_SOUND_INDEX);
                        shopHoverSoundPlayedFlag = 1;
                    }
                    tavernHoverSoundPlayedFlag = 0;
                    missionExitHoverSoundPlayedFlag = 0;
                }
                case 2 -> {
                    if (tavernkeeperAnimationGroup != 2 && previousHoveredActionMask != hoveredActionMask) {
                        stopPointerSound(DRUID_WOMAN_SOUND_INDEX);
                        lastTavernkeeperAmbientTick = currentTick();
                        tavernkeeperAnimationGroup = 2;
                        tavernkeeperAnimationFrame = -1;
                        townAnimationFlags |= hoveredActionMask;
                        advanceTavernkeeperAnimation();
                        playPointerSound(DRUID_WOMAN_SOUND_INDEX);
                    }
                    if (tavernHoverSoundPlayedFlag == 0) {
                        stopPointerSound(SHOP_ENTER_SOUND_INDEX);
                        stopPointerSound(TOWN_EXIT_SOUND_INDEX);
                        playPointerSound(TAVERN_ENTER_SOUND_INDEX);
                        tavernHoverSoundPlayedFlag = 1;
                    }
                    shopHoverSoundPlayedFlag = 0;
                    missionExitHoverSoundPlayedFlag = 0;
                }
                case 4 -> {
                    return this;
                }
                case 8 -> {
                    tavernHoverSoundPlayedFlag = 0;
                    shopHoverSoundPlayedFlag = 0;
                    if (missionExitHoverSoundPlayedFlag == 0) {
                        stopPointerSound(SHOP_ENTER_SOUND_INDEX);
                        stopPointerSound(TAVERN_ENTER_SOUND_INDEX);
                        playPointerSound(TOWN_EXIT_SOUND_INDEX);
                        missionExitHoverSoundPlayedFlag = 1;
                    }
                }
                case -1 -> {
                    restoreDefaultCursor();
                    tavernHoverSoundPlayedFlag = 0;
                    shopHoverSoundPlayedFlag = 0;
                    missionExitHoverSoundPlayedFlag = 0;
                }
                default -> {
                    townAnimationFlags |= hoveredActionMask;
                    tavernHoverSoundPlayedFlag = 0;
                    shopHoverSoundPlayedFlag = 0;
                    missionExitHoverSoundPlayedFlag = 0;
                }
            }
            return this;
        }

        if (hoveredActionMask == 0x200 || hoveredActionMask == 0x1000) {
            return this;
        }

        townAnimationFlags |= hoveredActionMask;
        tavernHoverSoundPlayedFlag = 0;
        shopHoverSoundPlayedFlag = 0;
        missionExitHoverSoundPlayedFlag = 0;
        return this;
    }

    /**
     * vtbl +0x9C: DruidTownDialogVisualObject::UpdateRandomAmbientSelections @004D00DE.
     * Fully ported.
     */
    @Override
    public void updateRandomAmbientSelections() {
        int now = currentTick();
        int hoveredActionMask = getHoveredActionMaskAtPoint(Globals.mousePointer.getX(), Globals.mousePointer.getY());
        if (hasElapsed(now, lastShopkeeperAmbientTick, nextShopkeeperAmbientDelayMs)) {
            nextShopkeeperAmbientDelayMs =
                    Utils.randInclusive(RANDOM_CHARACTER_DELAY_BASE_MS, RANDOM_CHARACTER_DELAY_BASE_MS + RANDOM_AMBIENT_DELAY_SPAN_MS - 1);
            lastShopkeeperAmbientTick = now;
            if (shopkeeperAnimationGroup == -1) {
                shopkeeperAnimationFrame = 0;
                shopkeeperAnimationGroup = hoveredActionMask == 1 ? 2 : Utils.randInclusive(1);
                townAnimationFlags |= 1;
                stopPointerSound(DRUID_MAN_SOUND_INDEX);
                playPointerSound(DRUID_MAN_SOUND_INDEX);
            }
        }
        if (hasElapsed(now, lastTavernkeeperAmbientTick, nextTavernkeeperAmbientDelayMs)) {
            nextTavernkeeperAmbientDelayMs =
                    Utils.randInclusive(RANDOM_CHARACTER_DELAY_BASE_MS, RANDOM_CHARACTER_DELAY_BASE_MS + RANDOM_AMBIENT_DELAY_SPAN_MS - 1);
            lastTavernkeeperAmbientTick = now;
            if (tavernkeeperAnimationGroup == -1) {
                tavernkeeperAnimationFrame = 0;
                tavernkeeperAnimationGroup = hoveredActionMask == 2 ? 2 : Utils.randInclusive(1);
                townAnimationFlags |= 2;
                stopPointerSound(DRUID_WOMAN_SOUND_INDEX);
                playPointerSound(DRUID_WOMAN_SOUND_INDEX);
            }
        }

        ensureAmbientDelayInitialized();
        if (hasElapsed(now, lastAmbientBirdTick, nextAmbientBirdDelayMs)) {
            activeAmbientBirdCount = Utils.randInclusive(1, 3);
            if (activeAmbientBirdCount == 1) {
                playPointerSound(BIRD_1_SOUND_INDEX);
            } else if (activeAmbientBirdCount == 2) {
                playPointerSound(BIRD_2_SOUND_INDEX);
            } else if (activeAmbientBirdCount == 3) {
                playPointerSound(BIRD_3_SOUND_INDEX);
            }
            nextAmbientBirdDelayMs = Utils.randInclusive(RANDOM_SHORT_DELAY_BASE_MS, RANDOM_SHORT_DELAY_BASE_MS + 1999);
            lastAmbientBirdTick = currentTick();
        }
        if (hasElapsed(now, lastTreeAmbientTick, nextAmbientTreeDelayMs)) {
            int treeSoundIndex = Utils.randInclusive(3);
            switch (treeSoundIndex) {
                case 0 -> playPointerSound(TREE_1_SOUND_INDEX);
                case 1 -> playPointerSound(TREE_2_SOUND_INDEX);
                case 2 -> playPointerSound(TREE_3_SOUND_INDEX);
                case 3 -> playPointerSound(TREE_4_SOUND_INDEX);
                default -> {
                }
            }
            nextAmbientTreeDelayMs = Utils.randInclusive(RANDOM_SHORT_DELAY_BASE_MS, RANDOM_SHORT_DELAY_BASE_MS + 1999);
            lastTreeAmbientTick = currentTick();
        }
        if (hasElapsed(now, lastWolfAmbientTick, RANDOM_WOLF_REPEAT_MS)) {
            playPointerSound(WOLF_1_SOUND_INDEX);
            lastWolfAmbientTick = currentTick();
        }
        if (hasElapsed(now, lastBugAmbientTick, nextBugAmbientDelayMs)) {
            int bugVariant = Utils.randInclusive(1, 3);
            if (bugVariant == 1) {
                playPointerSound(BUG_1_SOUND_INDEX);
            } else if (bugVariant == 2) {
                playPointerSound(BUG_2_SOUND_INDEX);
            } else if (bugVariant == 3) {
                playPointerSound(BUG_3_SOUND_INDEX);
            }
            bugAnimationVariant = bugVariant - 1;
            townAnimationFlags |= 0x80;
            nextBugAmbientDelayMs = Utils.randInclusive(
                    RANDOM_BUG_DELAY_BASE_MS,
                    RANDOM_BUG_DELAY_BASE_MS + RANDOM_BUG_DELAY_SPAN_MS - 1);
            lastBugAmbientTick = currentTick();
        }
        if (lizardAnimationVariant == -1) {
            int randomLizardAnimation = Utils.randInclusive(1, 0x0F);
            if (randomLizardAnimation == 1) {
                lizardAnimationVariant = 1;
                playPointerSound(LIZARD_IDLE_2_SOUND_INDEX);
            } else if (randomLizardAnimation == 2) {
                lizardAnimationVariant = 2;
                playPointerSound(LIZARD_IDLE_3_SOUND_INDEX);
            } else if (randomLizardAnimation == 3) {
                lizardAnimationVariant = 3;
                playPointerSound(LIZARD_IDLE_4_SOUND_INDEX);
            } else {
                lizardAnimationVariant = 0;
                playPointerSound(LIZARD_IDLE_1_SOUND_INDEX);
            }
            townAnimationFlags |= 0x100;
            lizardAnimationFrame = 0;
        }
    }

    /**
     * vtbl +0xA0: DruidTownDialogVisualObject::loadTownAssets @004CE914.
     * Fully ported.
     */
    @Override
    public void loadTownAssets() {
        releaseTownAssets();
        hoverMaskBitmap = loadBmp256(DRUID_TOWN_MASK_BMP);
        Globals.mousePointer.update();
        mainBackgroundBitmap = loadBmp64k(DRUID_TOWN_MAIN_BMP);
        Globals.mousePointer.update();
        tavernHighlightBitmap = loadBmp64k(DRUID_TAVERN_HIGHLIGHT_BMP);
        shopHighlightBitmap = loadBmp64k(DRUID_SHOP_HIGHLIGHT_BMP);
        Globals.mousePointer.update();
        loadBitmapGroups(shopkeeperAnimationFrameGroups, DRUID_WOMAN_FRAME_PATH_FORMAT);
        loadBitmapGroups(tavernkeeperAnimationFrameGroups, DRUID_MAN_FRAME_PATH_FORMAT);
        bugSprite = loadSprite(DRUID_BUG_SPRITES_16A);
        lizardSprite = loadSprite(DRUID_LIZARD_SPRITES_16A);
        tavernkeeperAnimationFrame = -1;
        tavernkeeperAnimationGroup = -1;
        advanceTavernkeeperAnimation();
        signAnimationFrame = -1;
        doorAnimationFrame = 9;
        starsAnimationFrame = -1;
        fighterAnimationFrame = 0;
        mageAnimationFrame = 0;
        shopkeeperAnimationFrame = -1;
        shopkeeperAnimationGroup = -1;
        advanceShopkeeperAnimation();
        flugelAnimationFrame = -1;
        lizardAnimationVariant = -1;
        lizardAnimationFrame = -1;
        babyBirdAnimationFrame = -1;
        horseAnimationFrame = -1;
        dervishAnimationFrame = -1;
        bugAnimationVariant = -1;
    }

    /**
     * vtbl +0xA4: DruidTownDialogVisualObject::releaseTownAssets @004CEFC0.
     * Fully ported.
     */
    @Override
    public void releaseTownAssets() {
        mainBackgroundBitmap = null;
        hoverMaskBitmap = null;
        tavernHighlightBitmap = releaseBmp(tavernHighlightBitmap);
        shopHighlightBitmap = releaseBmp(shopHighlightBitmap);
        bugSprite = releaseSprite(bugSprite);
        lizardSprite = releaseSprite(lizardSprite);
        releaseBitmapGroups(tavernkeeperAnimationFrameGroups);
        releaseBitmapGroups(shopkeeperAnimationFrameGroups);
    }

    /**
     * vtbl +0xA8: DruidTownDialogVisualObject::AdvanceAmbientAnimations @004D065B.
     * Fully ported.
     */
    @Override
    public void advanceAmbientAnimations() {
        if ((townAnimationFlags & 1) != 0) {
            advanceShopkeeperAnimation();
        }
        if ((townAnimationFlags & 2) != 0) {
            advanceTavernkeeperAnimation();
        }
        if ((townAnimationFlags & 0x80) != 0) {
            advanceBugAnimation();
        }
        if ((townAnimationFlags & 0x100) != 0) {
            advanceLizardAnimation();
        }
    }

    /**
     * Native support helper backing DruidTownDialogVisualObject::Update @004CF65A.
     * Full support port.
     */
    private void renderDruidTownScene() {
        int screenLeft = cRect.left;
        int screenTop = cRect.top;
        if (mainBackgroundBitmap != null) {
            mainBackgroundBitmap.draw(screenLeft, screenTop, 0, null, false);
        }
        if (hoveredActionMask == 1 || shopkeeperAnimationGroup == 2) {
            shopHighlightBitmap.draw(screenLeft + 0x1A4, screenTop + 0xE0, 0, null, false);
        }
        if (shopkeeperAnimationGroup < 0) {
            if (hoveredActionMask == 1) {
                shopkeeperAnimationFrameGroups.get(2).get(0).draw(screenLeft + 0x154, screenTop + 0xE0, 0, null, false);
            } else {
                shopkeeperAnimationFrameGroups.get(0).get(0).draw(screenLeft + 0x150, screenTop + 0xF4, 0, null, false);
            }
        } else {
            int x = screenLeft + 0x150 + (shopkeeperAnimationGroup / 2) * 4;
            int y = screenTop + 0xF4 - (shopkeeperAnimationGroup / 2) * 0x14;
            shopkeeperAnimationFrameGroups
                    .get(shopkeeperAnimationGroup)
                    .get(shopkeeperAnimationFrame)
                    .draw(x, y, 0, null, false);
        }
        if (hoveredActionMask == 2 || tavernkeeperAnimationGroup == 2) {
            tavernHighlightBitmap.draw(screenLeft, screenTop + 0xB8, 0, null, false);
        }
        if (tavernkeeperAnimationGroup < 0) {
            if (hoveredActionMask == 2) {
                tavernkeeperAnimationFrameGroups.get(2).get(0).draw(screenLeft + 0x98, screenTop + 0xB8, 0, null, false);
            } else {
                tavernkeeperAnimationFrameGroups.get(0).get(0).draw(screenLeft + 0xA4, screenTop + 200, 0, null, false);
            }
        } else {
            int x = screenLeft + 0xA4 - (tavernkeeperAnimationGroup / 2) * 0x0C;
            int y = screenTop + 200 - (tavernkeeperAnimationGroup / 2) * 0x10;
            tavernkeeperAnimationFrameGroups
                    .get(tavernkeeperAnimationGroup)
                    .get(tavernkeeperAnimationFrame)
                    .draw(x, y, 0, null, false);
        }
        if (lizardAnimationVariant < 0 || lizardAnimationFrame < 0) {
            lizardSprite.draw(screenLeft, screenTop + 300, 0, 0, false);
        } else {
            lizardSprite.draw(screenLeft, screenTop + 300,
                    resolveLizardFrameIndex(lizardAnimationVariant, lizardAnimationFrame), 0, false);
        }
        if (bugAnimationVariant >= 0 && bugAnimationFrame >= 0) {
            bugSprite.draw(screenLeft, screenTop + 0xD4, bugAnimationVariant * BUG_ANIMATION_FRAME_COUNT + bugAnimationFrame, 0, false);
        }
    }

    /**
     * Native helper: DruidTownDialogVisualObject::advanceTavernkeeperAnimation @004CFB45.
     * Fully ported.
     */
    private void advanceTavernkeeperAnimation() {
        if (tavernkeeperAnimationFrame == 0) {
            stopPointerSound(shopEnterSound);
            playPointerSound(pointSound);
        }
        if (tavernkeeperAnimationGroup >= 0) {
            tavernkeeperAnimationFrame += 1;
            int frameCount = getBitmapGroupSize(tavernkeeperAnimationFrameGroups, tavernkeeperAnimationGroup);
            if (tavernkeeperAnimationFrame == frameCount) {
                tavernkeeperAnimationFrame = 0;
                tavernkeeperAnimationGroup = -1;
                townAnimationFlags &= ~2;
            }
        }
    }

    /**
     * Native helper: DruidTownDialogVisualObject::advanceShopkeeperAnimation @004CFBF6.
     * Fully ported.
     */
    private void advanceShopkeeperAnimation() {
        if (shopkeeperAnimationGroup >= 0) {
            shopkeeperAnimationFrame += 1;
            int frameCount = getBitmapGroupSize(shopkeeperAnimationFrameGroups, shopkeeperAnimationGroup);
            if (shopkeeperAnimationFrame == frameCount) {
                shopkeeperAnimationFrame = 0;
                shopkeeperAnimationGroup = -1;
                townAnimationFlags &= ~1;
            }
        }
    }

    /**
     * Native helper: DruidTownDialogVisualObject::advanceBugAnimation @004CFC77.
     * Fully ported.
     */
    private void advanceBugAnimation() {
        if (bugAnimationVariant >= 0
                && ++bugAnimationFrame == BUG_ANIMATION_FRAME_COUNT) {
            bugAnimationFrame = 0;
            bugAnimationVariant = -1;
            townAnimationFlags &= ~0x80;
        }
    }

    /**
     * Native helper: DruidTownDialogVisualObject::advanceLizardAnimation @004CFCDE.
     * Fully ported.
     */
    private void advanceLizardAnimation() {
        if (lizardAnimationVariant >= 0) {
            lizardAnimationFrame += 1;
            if (isLizardAnimationComplete(lizardAnimationVariant, lizardAnimationFrame)) {
                lizardAnimationFrame = -1;
                lizardAnimationVariant = -1;
                townAnimationFlags &= ~0x100;
            }
        }
    }

    /**
     * Native support extracted from ShowRoleKeyDialog(dynamic placard script) @0041D15E call sites in
     * DruidTownDialogVisualObject::onLButtonDown @004D06CC.
     */
    private void showTownPlacardScript(String scriptName) {
        RoleDialogSupport.showRoleKeyDialog(scriptName);
    }

    /**
     * Native support extracted from DruidTownDialogVisualObject::onLButtonDown @004D06CC for the dynamic druid-town placard format strings.
     * Full support port.
     */
    private void showTownPlacard(String scriptNameFormat) {
        int townScriptVariant = Globals.scenarioLib.getVar(SCENARIO_CHAPTER_VAR_ID);
        showTownPlacardScript(String.format(Locale.ROOT, scriptNameFormat, townScriptVariant));
    }

    /**
     * Native support extracted from the behavior-relevant static hover-refresh timer slot in DruidTownDialogVisualObject::Update @004CF65A.
     * Full support port. Native also initializes write-only `unusedDruidTownUpdateDelayMs @00628258`, which has no native readers and is omitted in Java.
     */
    private static void ensureHoverRefreshInitialized() {
        if (!druidUpdateTimerInitialized) {
            druidUpdateTimerInitialized = true;
            lastDruidHoverRefreshTick = currentTick();
        }
    }

    /**
     * Native support extracted from the static bird/tree delay slots used by DruidTownDialogVisualObject::updateRandomAmbientSelections @004D00DE.
     * Full support port.
     */
    private static void ensureAmbientDelayInitialized() {
        if (!ambientBirdDelayInitialized) {
            ambientBirdDelayInitialized = true;
            nextAmbientBirdDelayMs = Utils.randInclusive(RANDOM_SHORT_DELAY_BASE_MS, RANDOM_SHORT_DELAY_BASE_MS + 1999);
        }
        if (!ambientTreeDelayInitialized) {
            ambientTreeDelayInitialized = true;
            nextAmbientTreeDelayMs = Utils.randInclusive(RANDOM_SHORT_DELAY_BASE_MS, RANDOM_SHORT_DELAY_BASE_MS + 1999);
        }
    }

    /**
     * Native support extracted from the repeated druid-town `CArray<CBmp64k>::Add` loops in DruidTownDialogVisualObject::loadTownAssets @004CE914.
     * Full support port.
     */
    private static void loadBitmapGroups(List<List<CBmp64k>> targetGroups, String resourcePathFormat) {
        for (int groupIndex = 0; groupIndex < targetGroups.size(); groupIndex++) {
            List<CBmp64k> group = targetGroups.get(groupIndex);
            group.clear();
            for (int frameIndex = 1; frameIndex <= DRUID_CHARACTER_FRAME_MAX_COUNT; frameIndex++) {
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
     * Native support extracted from repeated druid-town bitmap-array teardown in DruidTownDialogVisualObject::releaseTownAssets @004CEFC0.
     * Full support port.
     */
    private static void releaseBitmapGroups(List<List<CBmp64k>> targetGroups) {
        for (List<CBmp64k> group : targetGroups) {
            for (int frameIndex = 0; frameIndex < group.size(); frameIndex++) {
                group.set(frameIndex, releaseBmp(group.get(frameIndex)));
            }
            group.clear();
        }
    }

    /**
     * Native support extracted from `new CA16(path)` load sites in DruidTownDialogVisualObject::loadTownAssets @004CE914.
     * Full support port.
     */
    private static CA16 loadSprite(String resourcePath) {
        CA16 sprite = new CA16(Resources.path(resourcePath));
        sprite.initPalette(PALETTE_PAGE_COUNT, DRUID_SPRITE_PALETTE_MODE, 0);
        return sprite;
    }

    /**
     * Native support extracted from bitmap destruction sites in DruidTownDialogVisualObject::releaseTownAssets @004CEFC0, represented by Java reference clearing.
     * Full support port.
     */
    private static CBmp64k releaseBmp(CBmp64k bitmap) {
        return null;
    }

    /**
     * Native support extracted from sprite destruction sites in DruidTownDialogVisualObject::releaseTownAssets @004CEFC0, represented by Java reference clearing.
     * Full support port.
     */
    private static CA16 releaseSprite(CA16 sprite) {
        return null;
    }

    /**
     * Native support extracted from DeleteSound @00438480 call sites in DruidTownDialogVisualObject::releaseAmbientSounds @004CF4DC.
     * Full support port.
     */
    private void releaseAmbientSound(int index) {
        Sound sound = getAmbientSound(index);
        SoundSystem.get().releaseSound(sound);
        setAmbientSound(index, null);
    }

    /**
     * Native support extracted from recovered druid-town sound slots in DruidTownDialogVisualObject::loadAmbientSounds @004CF2C0.
     * Full support port.
     */
    private Sound getAmbientSound(int index) {
        return switch (index) {
            case LIZARD_IDLE_1_SOUND_INDEX -> lizardAmbientSound1;
            case LIZARD_IDLE_2_SOUND_INDEX -> lizardAmbientSound2;
            case LIZARD_IDLE_3_SOUND_INDEX -> lizardAmbientSound3;
            case LIZARD_IDLE_4_SOUND_INDEX -> lizardAmbientSound4;
            case BUG_1_SOUND_INDEX -> bugAmbientSound1;
            case BUG_2_SOUND_INDEX -> bugAmbientSound2;
            case BUG_3_SOUND_INDEX -> bugAmbientSound3;
            case BIRD_1_SOUND_INDEX -> birdAmbientSound1;
            case BIRD_2_SOUND_INDEX -> birdAmbientSound2;
            case BIRD_3_SOUND_INDEX -> birdAmbientSound3;
            case TREE_1_SOUND_INDEX -> treeAmbientSound1;
            case TREE_2_SOUND_INDEX -> treeAmbientSound2;
            case TREE_3_SOUND_INDEX -> treeAmbientSound3;
            case TREE_4_SOUND_INDEX -> treeAmbientSound4;
            case FOREST_LOOP_SOUND_INDEX -> forestAmbientLoopSound;
            case DRUID_MAN_SOUND_INDEX -> shopkeeperAmbientSound;
            case DRUID_WOMAN_SOUND_INDEX -> tavernkeeperAmbientSound;
            case SHOP_ENTER_SOUND_INDEX -> druidShopEnterSound;
            case TAVERN_ENTER_SOUND_INDEX -> druidTavernEnterSound;
            case TOWN_EXIT_SOUND_INDEX -> druidTownExitSound;
            case WOLF_1_SOUND_INDEX -> wolfAmbientSound;
            default -> null;
        };
    }

    /**
     * Native support extracted from recovered druid-town sound slot assignment in DruidTownDialogVisualObject::loadAmbientSounds @004CF2C0.
     * Full support port.
     */
    private void setAmbientSound(int index, Sound sound) {
        switch (index) {
            case LIZARD_IDLE_1_SOUND_INDEX -> lizardAmbientSound1 = sound;
            case LIZARD_IDLE_2_SOUND_INDEX -> lizardAmbientSound2 = sound;
            case LIZARD_IDLE_3_SOUND_INDEX -> lizardAmbientSound3 = sound;
            case LIZARD_IDLE_4_SOUND_INDEX -> lizardAmbientSound4 = sound;
            case BUG_1_SOUND_INDEX -> bugAmbientSound1 = sound;
            case BUG_2_SOUND_INDEX -> bugAmbientSound2 = sound;
            case BUG_3_SOUND_INDEX -> bugAmbientSound3 = sound;
            case BIRD_1_SOUND_INDEX -> birdAmbientSound1 = sound;
            case BIRD_2_SOUND_INDEX -> birdAmbientSound2 = sound;
            case BIRD_3_SOUND_INDEX -> birdAmbientSound3 = sound;
            case TREE_1_SOUND_INDEX -> treeAmbientSound1 = sound;
            case TREE_2_SOUND_INDEX -> treeAmbientSound2 = sound;
            case TREE_3_SOUND_INDEX -> treeAmbientSound3 = sound;
            case TREE_4_SOUND_INDEX -> treeAmbientSound4 = sound;
            case FOREST_LOOP_SOUND_INDEX -> forestAmbientLoopSound = sound;
            case DRUID_MAN_SOUND_INDEX -> shopkeeperAmbientSound = sound;
            case DRUID_WOMAN_SOUND_INDEX -> tavernkeeperAmbientSound = sound;
            case SHOP_ENTER_SOUND_INDEX -> druidShopEnterSound = sound;
            case TAVERN_ENTER_SOUND_INDEX -> druidTavernEnterSound = sound;
            case TOWN_EXIT_SOUND_INDEX -> druidTownExitSound = sound;
            case WOLF_1_SOUND_INDEX -> wolfAmbientSound = sound;
            default -> {
            }
        }
    }

    /**
     * Java helper for `timeGetTime`-style unsigned timer comparisons used in DruidTownDialogVisualObject own methods.
     * not ported.
     */
    private static boolean hasElapsed(int now, int lastTick, int delayMs) {
        return Integer.compareUnsigned(now - lastTick, delayMs) > 0;
    }

    /**
     * Native support extracted from Sound::StopAndRewindPointerSound @004385B0 call sites in DruidTownDialogVisualObject::updateHoveredAction @004CFD58, ::updateRandomAmbientSelections @004D00DE, and ::advanceTavernkeeperAnimation @004CFB45.
     * Full support port.
     */
    private void stopPointerSound(int soundIndex) {
        stopPointerSound(getAmbientSound(soundIndex));
    }

    /**
     * Native support extracted from Sound::StopAndRewindPointerSound @004385B0 call sites in DruidTownDialogVisualObject::advanceTavernkeeperAnimation @004CFB45.
     * Full support port.
     */
    private static void stopPointerSound(Sound sound) {
        if (sound != null && sound.isPlaying()) {
            sound.stop();
        }
    }

    /**
     * Native support extracted from Sound::PlayPointer @00438570 call sites in DruidTownDialogVisualObject::updateHoveredAction @004CFD58, ::updateRandomAmbientSelections @004D00DE, and ::advanceTavernkeeperAnimation @004CFB45.
     * Full support port.
     */
    private void playPointerSound(int soundIndex) {
        playPointerSound(getAmbientSound(soundIndex));
    }

    /**
     * Native support extracted from Sound::PlayPointer @00438570 call sites in DruidTownDialogVisualObject::advanceTavernkeeperAnimation @004CFB45.
     * Full support port.
     */
    private static void playPointerSound(Sound sound) {
        if (sound != null) {
            sound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
        }
    }

    /**
     * Java helper for the repeated `CArray<CBmp64k>::GetSize` calls in DruidTownDialogVisualObject own methods.
     * not ported.
     */
    private static int getBitmapGroupSize(List<List<CBmp64k>> groups, int groupIndex) {
        if (groupIndex < 0 || groupIndex >= groups.size()) {
            return 0;
        }
        return groups.get(groupIndex).size();
    }

    /**
     * Native support extracted from the lizard frame-table lookup behind `PTR_DAT_005F68C8` in DruidTownDialogVisualObject::Update @004CF65A.
     * Full support port.
     */
    private static int resolveLizardFrameIndex(int animationVariant, int animationFrame) {
        return LIZARD_ANIMATION_FRAME_TABLE[animationVariant][animationFrame] - 1;
    }

    /**
     * Native support extracted from the lizard frame-table completion check behind `PTR_DAT_005F68C8` in DruidTownDialogVisualObject::advanceLizardAnimation @004CFCDE.
     * Full support port.
     */
    private static boolean isLizardAnimationComplete(int animationVariant, int animationFrame) {
        return LIZARD_ANIMATION_FRAME_TABLE[animationVariant][animationFrame] == -1;
    }
}
