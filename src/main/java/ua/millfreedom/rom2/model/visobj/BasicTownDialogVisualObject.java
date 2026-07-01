package ua.millfreedom.rom2.model.visobj;

import lombok.extern.slf4j.Slf4j;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.CA16;
import ua.millfreedom.rom2.model.CBmp256;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CGameBitmap;
import ua.millfreedom.rom2.model.CMousePointer;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.ScriptDataSupport;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.sound.Sound;
import ua.millfreedom.rom2.model.sound.SoundSystem;
import ua.millfreedom.rom2.res.Resources;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static ua.millfreedom.rom2.model.window.windowproc.handlers.CMainWindowWindowProcSupport.readMessageInt;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_CLICK_TO_BRING_UP_THE_MAIN_MENU_235;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_INN_236;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_SCHOOL_OF_MAGIC_AND_MARTIAL_ARTS_234;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_SHOP_233;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_START_MISSION_237;

/**
 * Native class: BasicTownDialogVisualObject.
 * Purpose: dialog branch keyed by id 0x3FC with town-screen bitmap, hover mask, and ambient audio state.
 */
@Slf4j
public class BasicTownDialogVisualObject extends HandlerVisualObject {
    public static final int NATIVE_SIZE = 0x20C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int HOVER_UPDATE_CADENCE_MS = 0x43;
    private static final int TOWN_SPRITE_PALETTE_PAGES = 0x10;
    private static final int TOWN_SPRITE_PALETTE_MODE = 4;
    private static final String TOWN_MAIN_BMP = "graphics/interface/town/townmain.bmp";
    private static final String TOWN_MASK_BMP = "graphics/interface/town/townmask.bmp";
    private static final String TOWN_TAVERN_HIGHLIGHT_BMP = "graphics/interface/town/tavern_l.bmp";
    private static final String TOWN_TRAINER_HIGHLIGHT_BMP = "graphics/interface/town/trener_l.bmp";
    private static final String TOWN_SHOP_HIGHLIGHT_BMP = "graphics/interface/town/shop_l.bmp";
    private static final String TOWN_TAVERNKEEPER_SPRITES_16A = "graphics/interface/townbirds/tavern/sprites.16a";
    private static final String TOWN_FIGHTER_SPRITES_16A = "graphics/interface/townbirds/fighter/sprites.16a";
    private static final String TOWN_MAGE_SPRITES_16A = "graphics/interface/townbirds/mage/sprites.16a";
    private static final String TOWN_SHOPKEEPER_SPRITES_16A = "graphics/interface/townbirds/shopie/sprites.16a";
    private static final String TOWN_GUARD_SPRITES_16A = "graphics/interface/townbirds/guards/sprites.16a";
    private static final String TOWN_ADD_BMP = "graphics/interface/town/town_add.bmp";
    private static final String TOWN_SIGN_BMP_FORMAT = "graphics/interface/town/sign/v%02d.bmp";
    private static final String TOWN_DOOR_BMP_FORMAT = "graphics/interface/town/door/t%02d.bmp";
    private static final String TOWN_STARS_BMP_FORMAT = "graphics/interface/town/stars/s%02d.bmp";
    private static final String TOWN_FLUGEL_BMP_FORMAT = "graphics/interface/town/fluger/f%02d.bmp";
    private static final String TOWN_BIRDS_SPRITES_16A_FORMAT = "graphics/interface/townbirds/birds%d/sprites.16a";
    private static final String TOWN_HORSE_SPRITES_16A_FORMAT = "graphics/interface/townbirds/horse%d/a%d/sprites.16a";
    private static final String TOWN_BABY_BIRD_SPRITES_16A_FORMAT = "graphics/interface/townbirds/baba%d/a%d/sprites.16a";
    private static final String TOWN_DERVISH_SPRITES_16A_FORMAT = "graphics/interface/townbirds/dervish%d/sprites.16a";
    private static final String TOWN_CROWD_WAV = "sfx/town/crowd.wav";
    private static final String TOWN_BIRDS1_WAV = "sfx/town/birds1.wav";
    private static final String TOWN_BIRDS2_WAV = "sfx/town/birds2.wav";
    private static final String TOWN_FLUGEL_WAV = "sfx/town/flugel.wav";
    private static final String TOWN_FLAG_WAV = "sfx/town/flag.wav";
    private static final String TOWN_POINT_WAV = "sfx/town/point.wav";
    private static final String TOWN_SHOP_ENTER_WAV = "sfx/town/shop/enter.wav";
    private static final String TOWN_SCHOOL_POINT_WAV = "sfx/town/school/point.wav";
    private static final String TOWN_STARS_WAV = "sfx/town/stars.wav";
    private static final String TOWN_HORSE2_WAV = "sfx/town/horse2.wav";
    private static final String TOWN_HORSE3_WAV = "sfx/town/horse3.wav";
    private static final String TOWN_HORSE1_WAV = "sfx/town/horse1.wav";
    private static final String TOWN_GATE_UP_WAV = "sfx/town/gateup.wav";
    private static final String TOWN_GATE_DOWN_WAV = "sfx/town/gatedn.wav";
    private static final String TOWN_GUARD1_WAV = "sfx/town/guard1.wav";
    private static final String TOWN_GUARD2_WAV = "sfx/town/guard2.wav";
    private static final int[] TOWN_HORSE_X = {0x68, 0x68, 0x100, 0x1C0, 0x8C};
    private static final int[] TOWN_HORSE_Y = {0x194, 0x194, 0x158, 0x190, 0x190};
    private static final int[] TOWN_BABY_BIRD_X = {0xD8, 0x134, 0x180, 0x244};
    private static final int[] TOWN_BABY_BIRD_Y = {0x16C, 0x1A8, 0x1A8, 0x180};
    private static final int[] TOWN_DERVISH_X = {0xE0, 0x144, 0x188, 0x250};
    private static final int[] TOWN_DERVISH_Y = {0x16C, 0x1A8, 0x1A4, 0x184};

    private static int townTimerInitFlags;
    private static int lastTownAnimationTick;
    private static int nextAmbientBirdDelayMs;
    private static int townStarsIdleDelay;
    private static int townFighterDirection;
    private static int townMageDirection;

    //0x68
    public CBmp64k mainBackgroundBitmap;
    //0x6c
    public CBmp256 hoverMaskBitmap;
    //0x70
    public CBmp64k townBirdOverlayBitmap;
    //0x74
    public Sound crowdAmbientSound;
    //0x78
    public Sound gateTransientSound;
    //0x7c
    public Sound guardTransientSound;
    //0x80
    public Sound birdsAmbientSound1;
    //0x84
    public Sound birdsAmbientSound2;
    //0x88
    public Sound flugelSound;
    //0x8c
    public Sound flagSound;
    //0x90
    public Sound pointSound;
    //0x94
    public Sound shopEnterSound;
    //0x98
    public Sound schoolPointSound;
    //0x9c
    public Sound starsSound;
    //0xa0
    public Sound horse2Sound;
    //0xa4
    public Sound horse3Sound;
    //0xa8
    public Sound horse1Sound;
    //0xac
    public int shopHoverSoundPlayedFlag;
    //0xb0
    public int secondaryHoverSoundPlayedFlag;
    //0xb4
    public int hoveredActionMask;
    //0xb8
    public int lastAmbientBirdTick;
    //0xbc
    public int ambientBirdGroupIndex;
    //0xc0
    public int activeAmbientBirdCount;
    //0xc4
    public final List<CA16> ambientBirdSprites = new ArrayList<>();
    //0xd8
    public int ambientBirdFrame0;
    //0xdc
    public int ambientBirdFrame1;
    //0xe0
    public int ambientBirdFrame2;
    //0xe4
    public CA16 guardSprite;
    //0xe8
    public int guardAnimationFrame;
    //0xec
    public int guardFrameStep;
    //0xf0
    public int guardSoundState;
    //0xf4
    public CA16 currentBabyBirdSprite;
    //0xf8
    public final List<CA16> babyBirdSprites = new ArrayList<>();
    //0x10c
    public final Point babyBirdPosition = new Point();
    //0x114
    public int babyBirdAnimationFrame;
    //0x118
    public int lastBabyBirdAnimationTick;
    //0x11c
    public int nextBabyBirdAnimationDelayMs;
    //0x120
    public CA16 currentHorseSprite;
    //0x124
    public final List<CA16> horseSprites = new ArrayList<>();
    //0x138
    public final Point horsePosition = new Point();
    //0x140
    public int horseAnimationFrame;
    //0x144
    public int horseAnimationIndex;
    //0x148
    public int lastHorseAnimationTick;
    //0x14c
    public int nextHorseAnimationDelayMs;
    //0x150
    public CA16 dervishSprite;
    //0x154
    public final Point dervishPosition = new Point();
    //0x15c
    public int dervishAnimationFrame;
    //0x160
    public CA16 tavernkeeperSprite;
    //0x164
    public CBmp64k tavernHighlightBitmap;
    //0x168
    public int tavernkeeperAnimationFrame;
    //0x16c
    public final List<CBmp64k> signFrameBitmaps = new ArrayList<>();
    //0x180
    public CBmp64k currentSignBitmap;
    //0x184
    public int signAnimationFrame;
    //0x188
    public final List<CBmp64k> doorFrameBitmaps = new ArrayList<>();
    //0x19c
    public CBmp64k currentDoorBitmap;
    //0x1a0
    public int doorAnimationFrame;
    //0x1a4
    public int doorOpenFlag;
    //0x1a8
    public final List<CBmp64k> starsFrameBitmaps = new ArrayList<>();
    //0x1bc
    public CBmp64k currentStarsBitmap;
    //0x1c0
    public int starsAnimationFrame;
    //0x1c4
    public CBmp64k trainerHighlightBitmap;
    //0x1c8
    public CA16 fighterSprite;
    //0x1cc
    public int fighterAnimationFrame;
    //0x1d0
    public CA16 mageSprite;
    //0x1d4
    public int mageAnimationFrame;
    //0x1d8
    public CA16 shopkeeperSprite;
    //0x1dc
    public CBmp64k shopHighlightBitmap;
    //0x1e0
    public int shopkeeperAnimationFrame;
    //0x1e4
    public final List<CBmp64k> flugelFrameBitmaps = new ArrayList<>();
    //0x1f8
    public CBmp64k currentFlugelBitmap;
    //0x1fc
    public int flugelAnimationFrame;
    //0x200
    public CVisualObject tipsPromptDialog;
    //0x204
    public int dialogActiveFlag;
    //0x208
    public int townAnimationFlags;

    /**
     * Native: BasicTownDialogVisualObject::BasicTownDialogVisualObject @004CA140.
     * Fully ported.
     */
    public BasicTownDialogVisualObject() {
        super();
        initialize();
    }

    /**
     * Native: BasicTownDialogVisualObject::BasicTownDialogVisualObject @004CA23C.
     * Fully ported.
     */
    public BasicTownDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        initialize();
    }

    /**
     * Native: BasicTownDialogVisualObject::BasicTownDialogVisualObject @004CA350.
     * Fully ported.
     */
    public BasicTownDialogVisualObject(int id, CRect rect) {
        super(id, rect, null);
        initialize();
    }

    /**
     * vtbl +0x14: BasicTownDialogVisualObject::GetText @004CDDC3.
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
            case 4 -> get(MAIN_SCHOOL_OF_MAGIC_AND_MARTIAL_ARTS_234);
            case 8 -> get(MAIN_START_MISSION_237);
            case 0x10 -> get(MAIN_CLICK_TO_BRING_UP_THE_MAIN_MENU_235);
            default -> null;
        };
    }

    /**
     * vtbl +0x2C: BasicTownDialogVisualObject::Update @004CB05D.
     * Fully ported.
     */
    @Override
    public void update() {
        if (dialogActiveFlag == 0) {
            return;
        }

        if ((townTimerInitFlags & 1) == 0) {
            townTimerInitFlags |= 1;
            lastTownAnimationTick = currentTick();
        }
        if ((townTimerInitFlags & 2) == 0) {
            townTimerInitFlags |= 2;
            nextAmbientBirdDelayMs = Utils.randBased(1000, 2000);
        }

        int now = currentTick();
        if (hasElapsed(now, lastTownAnimationTick, HOVER_UPDATE_CADENCE_MS)) {
            updateHoveredAction(Globals.mousePointer.getX(), Globals.mousePointer.getY());
            if (Utils.randPercent0To99() > 0x5E) {
                townAnimationFlags |= 0x40;
            }
            if (Utils.randPercent0To99() > 0x61) {
                townAnimationFlags |= 0x20;
            }
            advanceAmbientAnimations();
            lastTownAnimationTick = currentTick();
        }

        updateRandomAmbientSelections();
        if (hasElapsed(now, lastAmbientBirdTick, nextAmbientBirdDelayMs) && (townAnimationFlags & 0x80) == 0) {
            nextAmbientBirdDelayMs = scheduleAmbientBirdEvent();
        }

        drawTownScene();
        super.update();
    }

    /**
     * vtbl +0x30: BasicTownDialogVisualObject::RenderSelf @004CAEF3.
     * Fully ported.
     */
    @Override
    public void renderSelf(CRect clipRect) {
        // Native no-op.
    }

    /**
     * vtbl +0x48: BasicTownDialogVisualObject::OnMessage @004CAB95.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {

        if (msg == MessageCodes.RENDER_FRAME) {
            if (Globals.mainWindow.dialogsMask == 0) {
                draw();
            }
        } else if (msg == MessageCodes.WM_KEYDOWN && readMessageInt(wParam) == 0x0D) {
            return 1;
        } else if (msg == MessageCodes.RETURN_TO_GAME) {
            return 1;
        } else if (msg == MessageCodes.CLEAR_TIP_PROMPT && tipsPromptDialog != null) {
            removeChild(tipsPromptDialog);
            tipsPromptDialog.detachFromParent();
            tipsPromptDialog = null;
        }
        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * vtbl +0x4C: BasicTownDialogVisualObject::OnMouseMove @004CACC2.
     * Fully ported.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        updateHoveredAction(x, y);
        return 0;
    }

    /**
     * vtbl +0x54: BasicTownDialogVisualObject::OnLButtonDown @004CACE7.
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
                int startMissionAllowed = Globals.scenarioLib.state.startMissionAllowed;
                if (startMissionAllowed == 0) {
                    showMissionLockedPlacard();
                } else {
                    closeDialog();
                    postMainWindowMessage(MessageCodes.WRITE_CURRENT_MISSION_RESUME_SAVE, 1, 0);
                    postMainWindowMessage(MessageCodes.SHOW_GLOBAL_MAP_DIALOG, 0, 0);
                }
            }
            case 0x10 -> postMainWindowMessage(MessageCodes.SHOW_TOWN_MENU, 0, 0);
            default -> {
            }
        }
        return 1;
    }

    /**
     * vtbl +0x6C: BasicTownDialogVisualObject::OnKeyDown @004CAC8C.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        if (nChar == 0x0D || nChar == 0x1B) {
            return 1;
        }
        return super.onKeyDown(nChar);
    }

    /**
     * vtbl +0x78: BasicTownDialogVisualObject::Initialize @004CA58D.
     * Fully ported.
     */
    @Override
    public void initialize() {
        crowdAmbientSound = null;
        gateTransientSound = null;
        guardTransientSound = null;
        birdsAmbientSound1 = null;
        birdsAmbientSound2 = null;
        flugelSound = null;
        flagSound = null;
        pointSound = null;
        shopEnterSound = null;
        schoolPointSound = null;
        starsSound = null;
        horse2Sound = null;
        horse3Sound = null;
        horse1Sound = null;
        shopHoverSoundPlayedFlag = 0;
        secondaryHoverSoundPlayedFlag = 0;
        mainBackgroundBitmap = null;
        hoverMaskBitmap = null;
        townBirdOverlayBitmap = null;
        guardSprite = null;
        tavernkeeperSprite = null;
        currentSignBitmap = null;
        currentDoorBitmap = null;
        currentStarsBitmap = null;
        fighterSprite = null;
        mageSprite = null;
        shopkeeperSprite = null;
        currentFlugelBitmap = null;
        currentHorseSprite = null;
        currentBabyBirdSprite = null;
        dervishSprite = null;
        tavernHighlightBitmap = null;
        trainerHighlightBitmap = null;
        shopHighlightBitmap = null;
        tipsPromptDialog = null;
        dialogActiveFlag = 0;
        CVisualObject closeButton = new CommandButtonVisualObject(4, 0, 0, 0, 0, "", Globals.fonts.font1, Palettes.grayDim, MessageCodes.DIALOG_OK, 0, null);
        addChild(closeButton);
    }

    /**
     * vtbl +0x80: BasicTownDialogVisualObject::showDialog @004CA7E2.
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

        if (shouldShowTipsPrompt()) {
            tipsPromptDialog = new TipsPromptDialogVisualObject(
                    0x467,
                    0x148,
                    0,
                    0x280,
                    200,
                    ScriptDataSupport.getTipText(1)
            );
            addChild(tipsPromptDialog);
        } else {
            clearTipsPromptChild();
        }

        lastAmbientBirdTick = currentTick();
        guardAnimationFrame = getFrameCount(guardSprite) - 1;
        guardFrameStep = 0;
        townAnimationFlags |= 0x400;
        dervishAnimationFrame = 0;
        lastBabyBirdAnimationTick = currentTick();
        nextBabyBirdAnimationDelayMs = Utils.randBased(2000, 2000);
        currentBabyBirdSprite = babyBirdSprites.getFirst();
        babyBirdAnimationFrame = -1;
        lastHorseAnimationTick = currentTick();
        nextHorseAnimationDelayMs = Utils.randBased(2000, 2000);
        currentHorseSprite = horseSprites.getFirst();
        horseAnimationFrame = -1;
        hoveredActionMask = -1;

        clearScreen();
        super.showDialog();
        dialogActiveFlag = 1;
        draw();
        primeAmbientCrowdSound();
        Globals.mousePointer.enableBackgroundCapture();
    }

    /**
     * vtbl +0x84: BasicTownDialogVisualObject::HideDialog @004CAAED.
     * Fully ported.
     */
    @Override
    public HandlerVisualObject hideDialog(MessageCodes reason) {
        dialogActiveFlag = 0;
        clearTipsPromptChild();
        releaseTownAssets();
        releaseAmbientSounds();
        return super.hideDialog(reason);
    }

    /**
     * vtbl +0x88: BasicTownDialogVisualObject::LoadAmbientSounds @004CDEE4.
     * Fully ported.
     */
    public void loadAmbientSounds() {
        releaseAmbientSounds();
        crowdAmbientSound = loadSound(TOWN_CROWD_WAV);
        birdsAmbientSound1 = loadSound(TOWN_BIRDS1_WAV);
        birdsAmbientSound2 = loadSound(TOWN_BIRDS2_WAV);
        flugelSound = loadSound(TOWN_FLUGEL_WAV);
        flagSound = loadSound(TOWN_FLAG_WAV);
        pointSound = loadSound(TOWN_POINT_WAV);
        shopEnterSound = loadSound(TOWN_SHOP_ENTER_WAV);
        schoolPointSound = loadSound(TOWN_SCHOOL_POINT_WAV);
        starsSound = loadSound(TOWN_STARS_WAV);
        horse2Sound = loadSound(TOWN_HORSE2_WAV);
        horse3Sound = loadSound(TOWN_HORSE3_WAV);
        horse1Sound = loadSound(TOWN_HORSE1_WAV);
    }

    /**
     * vtbl +0x8C: BasicTownDialogVisualObject::ReleaseAmbientSounds @004CE00B.
     * Fully ported.
     */
    public void releaseAmbientSounds() {
        crowdAmbientSound = releaseSound(crowdAmbientSound);
        gateTransientSound = releaseSound(gateTransientSound);
        guardTransientSound = releaseSound(guardTransientSound);
        birdsAmbientSound1 = releaseSound(birdsAmbientSound1);
        birdsAmbientSound2 = releaseSound(birdsAmbientSound2);
        flugelSound = releaseSound(flugelSound);
        flagSound = releaseSound(flagSound);
        pointSound = releaseSound(pointSound);
        shopEnterSound = releaseSound(shopEnterSound);
        schoolPointSound = releaseSound(schoolPointSound);
        starsSound = releaseSound(starsSound);
        horse2Sound = releaseSound(horse2Sound);
        horse3Sound = releaseSound(horse3Sound);
        horse1Sound = releaseSound(horse1Sound);
    }

    /**
     * vtbl +0x90: BasicTownDialogVisualObject::primeAmbientCrowdSound @004CDECA.
     * Fully ported.
     */
    public void primeAmbientCrowdSound() {
        if (crowdAmbientSound == null) {
            return;
        }
        crowdAmbientSound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, true, Sound.POINTER_SFX_PRIORITY, 0);
    }

    /**
     * vtbl +0x94: BasicTownDialogVisualObject::GetHoveredActionMaskAtPoint @004CD8B7.
     * Fully ported.
     */
    public int getHoveredActionMaskAtPoint(int x, int y) {
        if (!cRect.contains(x, y) || hoverMaskBitmap == null) {
            return -1;
        }

        int localX = x - cRect.left;
        int localY = y - cRect.top;
        byte[] pixels = hoverMaskBitmap.frames.getFirst().data();

        return switch (pixels[localX + localY * 0x280] & 0xFF) {
            case 0x20 -> 0x400;
            case 0x40 -> 0x800;
            case 0x50 -> 0x1000;
            case 0x60 -> 0x200;
            case 0x80 -> 2;
            case 0x90 -> 1;
            case 0xA0 -> 8;
            case 0xB0 -> 0x10;
            case 0xC0 -> 4;
            default -> -1;
        };
    }

    /**
     * vtbl +0x98: BasicTownDialogVisualObject::UpdateHoveredAction @004CDBEA.
     * Fully ported.
     */
    public BasicTownDialogVisualObject updateHoveredAction(int x, int y) {
        int hoveredActionMask = getHoveredActionMaskAtPoint(x, y);
        guardFrameStep = 1;
        this.hoveredActionMask = hoveredActionMask;

        if (hoveredActionMask + 1 >= 10) {
            townAnimationFlags |= hoveredActionMask;
            secondaryHoverSoundPlayedFlag = 0;
            shopHoverSoundPlayedFlag = 0;
            return this;
        }

        switch (hoveredActionMask) {
            case 1 -> {
                if (Utils.randPercent0To99() > 0x5F && (townAnimationFlags & 1) == 0) {
                    townAnimationFlags |= hoveredActionMask;
                }
                if (shopHoverSoundPlayedFlag == 0) {
                    stopPointerSound(schoolPointSound);
                    playPointerSound(shopEnterSound);
                    shopHoverSoundPlayedFlag = 1;
                }
                secondaryHoverSoundPlayedFlag = 0;
            }
            case 4 -> {
                // Native case 4 falls through to a jump-table no-op branch.
            }
            case 8 -> {
                int startMissionAllowed = Globals.scenarioLib.state.startMissionAllowed;
                if (startMissionAllowed == 0) {
                    guardFrameStep = -1;
                } else {
                    guardFrameStep = 1;
                }
                secondaryHoverSoundPlayedFlag = 0;
                shopHoverSoundPlayedFlag = 0;
            }
            case -1 -> {
                restoreDefaultCursor();
                secondaryHoverSoundPlayedFlag = 0;
                shopHoverSoundPlayedFlag = 0;
            }
            default -> {
                townAnimationFlags |= hoveredActionMask;
                secondaryHoverSoundPlayedFlag = 0;
                shopHoverSoundPlayedFlag = 0;
            }
        }
        return this;
    }

    /**
     * vtbl +0x9C: BasicTownDialogVisualObject::UpdateRandomAmbientSelections @004CDA92.
     * Fully ported.
     */
    public void updateRandomAmbientSelections() {
        int now = currentTick();
        if (hasElapsed(now, lastBabyBirdAnimationTick, nextBabyBirdAnimationDelayMs)) {
            nextBabyBirdAnimationDelayMs = Utils.randInclusive(2000, 6999);
            lastBabyBirdAnimationTick = now;
            babyBirdAnimationFrame = 0;
            currentBabyBirdSprite = babyBirdSprites.get(Utils.randExclusive(0, babyBirdSprites.size()));
            townAnimationFlags |= 0x200;
        }
        if (hasElapsed(now, lastHorseAnimationTick, nextHorseAnimationDelayMs)) {
            nextHorseAnimationDelayMs = Utils.randInclusive(2000, 6999);
            lastHorseAnimationTick = now;
            horseAnimationFrame = 0;
            horseAnimationIndex = Utils.randExclusive(0, horseSprites.size());
            currentHorseSprite = horseSprites.get(horseAnimationIndex);
            townAnimationFlags |= 0x100;
        }
    }

    /**
     * vtbl +0xA0: BasicTownDialogVisualObject::LoadTownAssets @004CB6C6.
     * Fully ported.
     */
    public void loadTownAssets() {
        releaseTownAssets();
        hoverMaskBitmap = loadBmp256(TOWN_MASK_BMP);
        Globals.renderer.refreshMousePointer();
        mainBackgroundBitmap = loadBmp64k(TOWN_MAIN_BMP);
        Globals.renderer.refreshMousePointer();
        tavernHighlightBitmap = loadBmp64k(TOWN_TAVERN_HIGHLIGHT_BMP);
        trainerHighlightBitmap = loadBmp64k(TOWN_TRAINER_HIGHLIGHT_BMP);
        shopHighlightBitmap = loadBmp64k(TOWN_SHOP_HIGHLIGHT_BMP);
        Globals.renderer.refreshMousePointer();
        tavernkeeperSprite = loadTownSprite(TOWN_TAVERNKEEPER_SPRITES_16A);

        for (int frameIndex = 0; frameIndex < 10; frameIndex++) {
            signFrameBitmaps.add(loadBmp64k(String.format(Locale.ROOT, TOWN_SIGN_BMP_FORMAT, frameIndex)));
            Globals.renderer.refreshMousePointer();
        }
        for (int frameIndex = 0; frameIndex < 9; frameIndex++) {
            doorFrameBitmaps.add(loadBmp64k(String.format(Locale.ROOT, TOWN_DOOR_BMP_FORMAT, frameIndex)));
            Globals.renderer.refreshMousePointer();
            starsFrameBitmaps.add(loadBmp64k(String.format(Locale.ROOT, TOWN_STARS_BMP_FORMAT, frameIndex)));
            Globals.renderer.refreshMousePointer();
        }

        fighterSprite = loadTownSprite(TOWN_FIGHTER_SPRITES_16A);
        Globals.renderer.refreshMousePointer();
        mageSprite = loadTownSprite(TOWN_MAGE_SPRITES_16A);
        Globals.renderer.refreshMousePointer();
        shopkeeperSprite = loadTownSprite(TOWN_SHOPKEEPER_SPRITES_16A);
        for (int frameIndex = 0; frameIndex < 8; frameIndex++) {
            flugelFrameBitmaps.add(loadBmp64k(String.format(Locale.ROOT, TOWN_FLUGEL_BMP_FORMAT, frameIndex)));
            Globals.renderer.refreshMousePointer();
        }
        for (int spriteIndex = 1; spriteIndex <= 9; spriteIndex++) {
            ambientBirdSprites.add(loadTownSprite(String.format(Locale.ROOT, TOWN_BIRDS_SPRITES_16A_FORMAT, spriteIndex)));
            Globals.renderer.refreshMousePointer();
        }

        townBirdOverlayBitmap = loadBmp64k(TOWN_ADD_BMP);
        Globals.renderer.refreshMousePointer();
        guardSprite = loadTownSprite(TOWN_GUARD_SPRITES_16A);
        Globals.renderer.refreshMousePointer();

        int horseVariant = Utils.randInclusive(4);
        horsePosition.x = TOWN_HORSE_X[horseVariant];
        horsePosition.y = TOWN_HORSE_Y[horseVariant];
        for (int animationIndex = 1; animationIndex <= 3; animationIndex++) {
            horseSprites.add(loadTownSprite(String.format(Locale.ROOT, TOWN_HORSE_SPRITES_16A_FORMAT, horseVariant + 1, animationIndex)));
            Globals.renderer.refreshMousePointer();
        }

        int babyVariant = Utils.randInclusive(3);
        babyBirdPosition.x = TOWN_BABY_BIRD_X[babyVariant];
        babyBirdPosition.y = TOWN_BABY_BIRD_Y[babyVariant];
        for (int animationIndex = 1; animationIndex <= 2; animationIndex++) {
            babyBirdSprites.add(loadTownSprite(String.format(Locale.ROOT, TOWN_BABY_BIRD_SPRITES_16A_FORMAT, babyVariant + 1, animationIndex)));
            Globals.renderer.refreshMousePointer();
        }

        int dervishVariant = babyVariant;
        while (dervishVariant == babyVariant) {
            dervishVariant = Utils.randInclusive(3);
        }
        dervishPosition.x = TOWN_DERVISH_X[dervishVariant];
        dervishPosition.y = TOWN_DERVISH_Y[dervishVariant];
        dervishSprite = loadTownSprite(String.format(Locale.ROOT, TOWN_DERVISH_SPRITES_16A_FORMAT, dervishVariant + 1));
        Globals.renderer.refreshMousePointer();

        tavernkeeperAnimationFrame = -1;
        advanceTavernkeeperAnimation();
        signAnimationFrame = -1;
        advanceSignAnimation();
        doorAnimationFrame = 9;
        advanceGateDoorAnimation();
        starsAnimationFrame = -1;
        advanceStarsAnimation();
        fighterAnimationFrame = 0;
        advanceFighterAmbient();
        mageAnimationFrame = 0;
        advanceMageAmbient();
        shopkeeperAnimationFrame = -1;
        advanceShopkeeperAnimation();
        flugelAnimationFrame = -1;
        advanceFlugelAnimation();
        babyBirdAnimationFrame = -1;
        horseAnimationFrame = -1;
        dervishAnimationFrame = -1;
    }

    /**
     * vtbl +0xA4: BasicTownDialogVisualObject::ReleaseTownAssets @004CC4E5.
     * Fully ported.
     */
    public void releaseTownAssets() {
        mainBackgroundBitmap = null;
        hoverMaskBitmap = null;
        townBirdOverlayBitmap = null;
        guardSprite = null;
        currentBabyBirdSprite = null;
        currentHorseSprite = null;
        tavernHighlightBitmap = null;
        trainerHighlightBitmap = null;
        shopHighlightBitmap = null;
        tavernkeeperSprite = null;
        currentSignBitmap = null;
        currentDoorBitmap = null;
        currentStarsBitmap = null;
        fighterSprite = null;
        mageSprite = null;
        shopkeeperSprite = null;
        currentFlugelBitmap = null;
        dervishSprite = null;
        ambientBirdSprites.clear();
        babyBirdSprites.clear();
        horseSprites.clear();
        signFrameBitmaps.clear();
        doorFrameBitmaps.clear();
        starsFrameBitmaps.clear();
        flugelFrameBitmaps.clear();
    }

    /**
     * vtbl +0xA8: BasicTownDialogVisualObject::AdvanceAmbientAnimations @004CD606.
     * Full port. Dispatches the recovered flag-driven Plagat-town animation helpers and guard frame transition.
     */
    public void advanceAmbientAnimations() {
        if ((townAnimationFlags & 1) != 0) {
            advanceShopkeeperAnimation();
        }
        if ((townAnimationFlags & 2) != 0) {
            advanceTavernkeeperAnimation();
        }
        advanceGateDoorAnimation();
        if ((townAnimationFlags & 0x10) != 0) {
            advanceStarsAnimation();
        }
        if ((townAnimationFlags & 0x40) != 0) {
            advanceSignAnimation();
        }
        if ((townAnimationFlags & 0x20) != 0) {
            advanceFlugelAnimation();
        }
        if ((townAnimationFlags & 0x80) != 0) {
            ambientBirdFrame0 += 1;
            ambientBirdFrame1 += 1;
            ambientBirdFrame2 += 1;
        }
        if ((townAnimationFlags & 0x400) != 0) {
            advanceDervishAnimation();
        }
        if ((townAnimationFlags & 0x200) != 0) {
            advanceBabyBirdAnimation();
        }
        if ((townAnimationFlags & 0x100) != 0) {
            advanceHorseAnimation();
        }
        advanceGuardAnimation();
    }

    /**
     * vtbl +0xAC: BasicTownDialogVisualObject::CloseDialog @004CDDA4.
     * Fully ported.
     */
    public void closeDialog() {
        onMessage(MessageCodes.DIALOG_OK, 0, 0);
    }

    /**
     * Native: BasicTownDialogVisualObject::ScheduleAmbientBirdEvent @004CAE28.
     * Fully ported.
     */
    private int scheduleAmbientBirdEvent() {
        townAnimationFlags |= 0x80;
        ambientBirdGroupIndex = Utils.randExclusive(3);
        activeAmbientBirdCount = Utils.randBased(1, 3);
        if (activeAmbientBirdCount == 1) {
            playPointerSound(birdsAmbientSound1);
        } else {
            playPointerSound(birdsAmbientSound2);
        }
        lastAmbientBirdTick = currentTick();
        ambientBirdFrame0 = 0;
        ambientBirdFrame1 = 0;
        ambientBirdFrame2 = 0;
        return Utils.randBased(1000, 2000);
    }

    /**
     * Java helper for the partial Update render path.
     * Native support extracted from BasicTownDialogVisualObject::Update @004CB05D.
     */
    private void drawTownScene() {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        Globals.renderer.lockSurface();
        try {
            renderTownScene(screenRect.left, screenRect.top);
        } finally {
            Globals.renderer.unlockSurface();
        }
    }

    /**
     * Native support extracted from BasicTownDialogVisualObject::Update @004CB05D.
     */
    private void renderTownScene(int screenLeft, int screenTop) {
        if (mainBackgroundBitmap != null) {
            mainBackgroundBitmap.draw(screenLeft, screenTop, 0, null, false);
        }
        renderBirdsAndTownOverlay(screenLeft, screenTop);
        if (hoveredActionMask == 1) {
            shopHighlightBitmap.drawRectMasked(screenLeft + 0x108, screenTop + 0x108);
        }
        if (hoveredActionMask == 2) {
            tavernHighlightBitmap.drawRectMasked(screenLeft + 0x90, screenTop + 0x14C);
        }
        drawSprite(tavernkeeperSprite, screenLeft + 0x7C, screenTop + 0x138, tavernkeeperAnimationFrame);

        if (currentSignBitmap != null) {
            currentSignBitmap.drawRectMasked(screenLeft + 0x168, screenTop + 0xE8);
        }

        if (currentDoorBitmap != null) {
            currentDoorBitmap.drawRectMasked(screenLeft + 0xB4, screenTop + 0x94);
        }

        if (currentStarsBitmap != null) {
            currentStarsBitmap.drawRectMasked(screenLeft + 0x154, screenTop + 0x120);
        }
        drawSprite(shopkeeperSprite, screenLeft + 0x114, screenTop + 0x128, shopkeeperAnimationFrame);

        if (currentFlugelBitmap != null) {
            currentFlugelBitmap.drawRectMasked(screenLeft + 0x134, screenTop + 0x40);
        }
        drawSprite(guardSprite, screenLeft + 0xB8, screenTop + 0x9E, guardAnimationFrame);
        renderHorse(screenLeft, screenTop);
        renderBabyBird(screenLeft, screenTop);
        drawSprite(dervishSprite, screenLeft + dervishPosition.x, screenTop + dervishPosition.y, dervishAnimationFrame);
    }

    /**
     * Native support extracted from BasicTownDialogVisualObject::RenderBirdsAndTownOverlay @004CAF00.
     * Full support port.
     */
    private void renderBirdsAndTownOverlay(int screenLeft, int screenTop) {
        if ((townAnimationFlags & 0x80) == 0) {
            return;
        }

        int finishedBirds = 0;
        int[] birdFrames = {ambientBirdFrame0, ambientBirdFrame1, ambientBirdFrame2};
        for (int birdIndex = 0; birdIndex < activeAmbientBirdCount; birdIndex++) {
            CA16 birdSprite = ambientBirdSprites.get(ambientBirdGroupIndex * 3 + birdIndex);
            int frameIndex = birdFrames[birdIndex];
            if (frameIndex < birdSprite.frameCount) {
                birdSprite.draw(screenLeft, screenTop, frameIndex, 0, false);
            } else {
                finishedBirds += 1;
            }
        }
        townBirdOverlayBitmap.drawRectMasked(screenLeft, screenTop);
        if (finishedBirds == activeAmbientBirdCount) {
            townAnimationFlags &= ~0x80;
        }
        lastAmbientBirdTick = currentTick();
    }

    /**
     * Native support extracted from BasicTownDialogVisualObject::AdvanceShopkeeperAnimation @004CD51C.
     * Full support port.
     */
    private void advanceShopkeeperAnimation() {
        shopkeeperAnimationFrame += 1;
        if (shopkeeperAnimationFrame == shopkeeperSprite.frameCount) {
            shopkeeperAnimationFrame = 0;
            townAnimationFlags &= ~1;
        }
    }

    /**
     * Native support extracted from BasicTownDialogVisualObject::AdvanceTavernkeeperAnimation @004CCFBF.
     * Full support port.
     */
    private void advanceTavernkeeperAnimation() {
        if (tavernkeeperAnimationFrame == 0) {
            stopPointerSound(shopEnterSound);
            stopPointerSound(schoolPointSound);
            playPointerSound(pointSound);
        }
        tavernkeeperAnimationFrame += 1;
        if (tavernkeeperAnimationFrame == tavernkeeperSprite.frameCount) {
            tavernkeeperAnimationFrame = 0;
            townAnimationFlags &= ~2;
        }
    }

    /**
     * Native support extracted from BasicTownDialogVisualObject::AdvanceSignAnimation @004CD05B.
     * Full support port.
     */
    private void advanceSignAnimation() {
        if (signAnimationFrame == 0) {
            playPointerSound(flagSound);
        }
        signAnimationFrame += 1;
        if (signAnimationFrame == 10) {
            signAnimationFrame = 0;
            townAnimationFlags &= ~0x40;
        }
        currentSignBitmap = signFrameBitmaps.get(signAnimationFrame);
    }

    /**
     * Native support extracted from BasicTownDialogVisualObject::AdvanceGateDoorAnimation @004CD0EA.
     * Full support port.
     */
    private void advanceGateDoorAnimation() {
        if (Globals.scenarioLib.state.startMissionAllowed == 0) {
            doorAnimationFrame = 8;
            currentDoorBitmap = doorFrameBitmaps.get(doorAnimationFrame);
            return;
        }

        if (getHoveredActionMaskAtPoint(Globals.mousePointer.getX(), Globals.mousePointer.getY()) == 8) {
            if (doorOpenFlag == 0) {
                playTransientSound(0x78, TOWN_GATE_UP_WAV);
            }
            doorOpenFlag = 1;
            doorAnimationFrame -= 1;
            if (doorAnimationFrame < 1) {
                doorAnimationFrame = 0;
                townAnimationFlags &= ~8;
            }
        } else {
            if (doorOpenFlag != 0) {
                playTransientSound(0x78, TOWN_GATE_DOWN_WAV);
            }
            doorOpenFlag = 0;
            doorAnimationFrame += 1;
            if (doorAnimationFrame > 7) {
                doorAnimationFrame = 8;
                townAnimationFlags &= ~8;
            }
        }
        currentDoorBitmap = doorFrameBitmaps.get(doorAnimationFrame);
    }

    /**
     * Native support extracted from BasicTownDialogVisualObject::AdvanceStarsAnimation @004CD2AC.
     * Full support port.
     */
    private void advanceStarsAnimation() {
        if (starsAnimationFrame == 0) {
            playPointerSound(starsSound);
        }
        starsAnimationFrame += 1;
        if (starsAnimationFrame < 9) {
            currentStarsBitmap = starsFrameBitmaps.get(starsAnimationFrame);
            return;
        }
        townStarsIdleDelay += 1;
        if (townStarsIdleDelay == 10) {
            starsAnimationFrame = 0;
            townStarsIdleDelay = 0;
        }
        currentStarsBitmap = null;
        townAnimationFlags &= ~0x10;
    }

    /**
     * Native support extracted from BasicTownDialogVisualObject::AdvanceFlugelAnimation @004CD577.
     * Full support port.
     */
    private void advanceFlugelAnimation() {
        if (flugelAnimationFrame == 0) {
            playPointerSound(flugelSound);
        }
        flugelAnimationFrame += 1;
        if (flugelAnimationFrame == 8) {
            flugelAnimationFrame = 0;
            townAnimationFlags &= ~0x20;
        }
        currentFlugelBitmap = flugelFrameBitmaps.get(flugelAnimationFrame);
    }

    /**
     * Native support extracted from BasicTownDialogVisualObject::AdvanceFighterAmbient @004CD36A.
     * Full support port.
     */
    private void advanceFighterAmbient() {
        if (fighterAnimationFrame < 1 && townFighterDirection == 0) {
            townFighterDirection = Utils.randPercent0To99() > 0x5F ? 1 : 0;
        } else if (fighterAnimationFrame == 10) {
            townFighterDirection = Utils.randPercent0To99() < 0x60 ? 0 : -1;
        } else if (fighterAnimationFrame == 0 && townFighterDirection == -1) {
            fighterAnimationFrame = 0;
            townFighterDirection = 0;
            townAnimationFlags &= ~4;
        }
        fighterAnimationFrame += townFighterDirection;
    }

    /**
     * Native support extracted from BasicTownDialogVisualObject::AdvanceMageAmbient @004CD443.
     * Full support port.
     */
    private void advanceMageAmbient() {
        if (mageAnimationFrame < 1 && townMageDirection == 0) {
            townMageDirection = Utils.randPercent0To99() > 0x5F ? 1 : 0;
        } else if (mageAnimationFrame == 10) {
            townMageDirection = Utils.randPercent0To99() < 0x60 ? 0 : -1;
        } else if (mageAnimationFrame == 0 && townMageDirection == -1) {
            mageAnimationFrame = 0;
            townMageDirection = 0;
            townAnimationFlags &= ~4;
        }
        mageAnimationFrame += townMageDirection;
    }

    /**
     * Native support extracted from BasicTownDialogVisualObject::AdvanceDervishAnimation @004CD865.
     * Full support port.
     */
    private void advanceDervishAnimation() {
        if (dervishSprite == null) {
            dervishAnimationFrame = -1;
            return;
        }
        dervishAnimationFrame = (dervishAnimationFrame + 1) % dervishSprite.frameCount;
    }

    /**
     * Native support extracted from BasicTownDialogVisualObject::AdvanceBabyBirdAnimation @004CD73B.
     * Full support port.
     */
    private void advanceBabyBirdAnimation() {
        if (currentBabyBirdSprite != null && (townAnimationFlags & 0x200) != 0 && babyBirdAnimationFrame != -1) {
            babyBirdAnimationFrame += 1;
            lastBabyBirdAnimationTick = currentTick();
            if (currentBabyBirdSprite.frameCount <= babyBirdAnimationFrame) {
                babyBirdAnimationFrame = -1;
                townAnimationFlags &= ~0x200;
            }
        }
    }

    /**
     * Native support extracted from BasicTownDialogVisualObject::AdvanceHorseAnimation @004CD7D0.
     * Full support port.
     */
    private void advanceHorseAnimation() {
        if (currentHorseSprite != null && (townAnimationFlags & 0x100) != 0 && horseAnimationFrame != -1) {
            horseAnimationFrame += 1;
            lastHorseAnimationTick = currentTick();
            if (currentHorseSprite.frameCount <= horseAnimationFrame) {
                horseAnimationFrame = -1;
                townAnimationFlags &= ~0x100;
            }
        }
    }

    /**
     * Native support extracted from BasicTownDialogVisualObject::AdvanceGuardAnimation @004CCE81.
     * Full support port.
     */
    private void advanceGuardAnimation() {
        guardAnimationFrame += guardFrameStep;
        if (guardFrameStep == 1 && guardSoundState == 0) {
            playTransientSound(0x7c, TOWN_GUARD2_WAV);
            guardSoundState = 1;
        } else if (guardFrameStep == -1 && guardSoundState != 0) {
            playTransientSound(0x7c, TOWN_GUARD1_WAV);
            guardSoundState = 0;
        }

        if (guardAnimationFrame < 0) {
            guardFrameStep = 0;
            guardAnimationFrame = 0;
            guardTransientSound = releaseSound(guardTransientSound);
        } else if (guardSprite.frameCount <= guardAnimationFrame) {
            guardFrameStep = 0;
            guardAnimationFrame = guardSprite.frameCount - 1;
            guardTransientSound = releaseSound(guardTransientSound);
        }
    }

    /**
     * Native support extracted from BasicTownDialogVisualObject::Update @004CB05D.
     */
    private void renderHorse(int screenLeft, int screenTop) {
        if (currentHorseSprite == null) {
            return;
        }
        int frameIndex = horseAnimationFrame == -1 ? 0 : horseAnimationFrame;
        if (horseAnimationIndex == 0 && horseAnimationFrame == 0x0E) {
            playPointerSound(horse2Sound);
        } else if (horseAnimationIndex == 1) {
            if (horseAnimationFrame == 8 || horseAnimationFrame == 0x0E) {
                playPointerSound(horse2Sound);
            }
        } else if (horseAnimationIndex == 2) {
            if (horseAnimationFrame == 1) {
                playPointerSound(horse1Sound);
            }
            if (horseAnimationFrame == 0x0E) {
                playPointerSound(horse2Sound);
            }
        }
        drawSprite(currentHorseSprite, screenLeft + horsePosition.x, screenTop + horsePosition.y, frameIndex);
    }

    /**
     * Native support extracted from BasicTownDialogVisualObject::Update @004CB05D.
     */
    private void renderBabyBird(int screenLeft, int screenTop) {
        if (currentBabyBirdSprite != null) {
            drawSprite(currentBabyBirdSprite, screenLeft + babyBirdPosition.x, screenTop + babyBirdPosition.y, babyBirdAnimationFrame == -1 ? 0 : babyBirdAnimationFrame);
        }
    }

    /**
     * Java helper for recovered CA16 town sprites.
     * not ported.
     */
    private static void drawSprite(CA16 sprite, int x, int y, int frameIndex) {
        if (sprite != null) {
            sprite.draw(x, y, frameIndex < 0 ? 0 : frameIndex, 0, false);
        }
    }

    /**
     * Native support extracted from Sound::Sound @004384F0 and Sound::PlayPointer @00438570 call sites in
     * BasicTownDialogVisualObject::AdvanceGateDoorAnimation @004CD0EA and ::AdvanceGuardAnimation @004CCE81.
     * Full support port.
     */
    private void playTransientSound(int soundOffset, String resourcePath) {
        if (soundOffset == 0x78) {
            gateTransientSound = releaseSound(gateTransientSound);
            gateTransientSound = new Sound(resourcePath);
            playPointerSound(gateTransientSound);
        } else if (soundOffset == 0x7c) {
            guardTransientSound = releaseSound(guardTransientSound);
            guardTransientSound = new Sound(resourcePath);
            playPointerSound(guardTransientSound);
        }
    }

    /**
     * Native support extracted from Sound::Sound pointer-slot helper @004384F0 call sites in
     * BasicTownDialogVisualObject::LoadAmbientSounds @004CDEE4.
     * Full support port.
     */
    protected static Sound loadSound(String resourcePath) {
        return new Sound(resourcePath);
    }

    /**
     * Native support extracted from DeleteSound @00438480 call sites in
     * BasicTownDialogVisualObject::ReleaseAmbientSounds @004CE00B, ::AdvanceGuardAnimation @004CCE81,
     * and the Sound::Sound @004384F0 helper used by ::AdvanceGateDoorAnimation @004CD0EA and ::AdvanceGuardAnimation @004CCE81.
     * Full support port.
     */
    protected static Sound releaseSound(Sound sound) {
        if (sound != null) {
            SoundSystem.get().releaseSound(sound);
        }
        return null;
    }

    /**
     * Native support boundary: `CWnd::PostMessage(mainWnd, msg, wParam, lParam)` call sites in
     * BasicTownDialogVisualObject::OnLButtonDown @004CACE7.
     * System-boundary bridge. Java deliberately routes the recovered message tuple through `Globals.mainWindow.postMessage(...)`.
     */
    protected static void postMainWindowMessage(MessageCodes msg, int wParam, int lParam) {
        Globals.mainWindow.postMessage(msg, wParam, lParam);
    }

    /**
     * Native support extracted from `g_GamePreferences.TipsMode` reads in BasicTownDialogVisualObject::showDialog
     * @004CA7E2 and child town dialog overrides.
     * Full support port.
     */
    protected static boolean shouldShowTipsPrompt() {
        return Globals.gamePreferences.tipsMode != 0;
    }

    /**
     * Native support extracted from ShowRoleKeyDialog("plagatguard") @0041D15E callback in
     * BasicTownDialogVisualObject::OnLButtonDown @004CACE7.
     */
    protected static void showMissionLockedPlacard() {
        RoleDialogSupport.showRoleKeyDialog("plagatguard");
    }

    /**
     * Native support extracted from CCursor::SetToPointer(g_Cursor_Default) @004739B0 call sites in
     * BasicTownDialogVisualObject::UpdateHoveredAction @004CDBEA.
     * Fully ported.
     */
    protected static void restoreDefaultCursor() {
        CMousePointer.Cursor_Default.setToPointer();
    }

    /**
     * Native support extracted from Sound::StopAndRewindPointerSound @004385B0 call sites in
     * BasicTownDialogVisualObject::UpdateHoveredAction @004CDBEA and ::AdvanceTavernkeeperAnimation @004CCFBF.
     * Full support port.
     */
    private static void stopPointerSound(Sound sound) {
        if (sound != null && sound.isPlaying()) {
            sound.stop();
        }
    }

    /**
     * Native support extracted from Sound::PlayPointer @00438570 call sites in
     * BasicTownDialogVisualObject::UpdateHoveredAction @004CDBEA, ::AdvanceTavernkeeperAnimation @004CCFBF,
     * ::AdvanceSignAnimation @004CD05B, ::AdvanceGateDoorAnimation @004CD0EA, ::AdvanceStarsAnimation @004CD2AC,
     * ::AdvanceFlugelAnimation @004CD577, ::ScheduleAmbientBirdEvent @004CAE28, ::Update @004CB05D,
     * and ::AdvanceGuardAnimation @004CCE81.
     * Full support port.
     */
    private static void playPointerSound(Sound sound) {
        if (sound != null) {
            sound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
        }
    }

    /**
     * Native support extracted from unsigned timeGetTime comparisons in BasicTownDialogVisualObject::Update @004CB05D
     * and ::UpdateRandomAmbientSelections @004CDA92.
     * Full support port.
     */
    private static boolean hasElapsed(int now, int lastTick, int delayMs) {
        return Integer.compareUnsigned(now - lastTick, delayMs) > 0;
    }

    /**
     * Java helper for `timeGetTime` call sites in BasicTownDialogVisualObject own methods.
     * not ported.
     */
    protected static int currentTick() {
        return (int) System.currentTimeMillis();
    }

    /**
     * Java helper for the `CGameBitmap::GetFrameCount` call site in ShowDialog.
     * not ported.
     */
    protected static int getFrameCount(Object bitmap) {
        return bitmap instanceof CGameBitmap gameBitmap ? gameBitmap.frameCount : 0;
    }

    /**
     * Native support extracted from CBmp64k constructor call sites in BasicTownDialogVisualObject::LoadTownAssets @004CB6C6,
     * DruidTownDialogVisualObject::loadTownAssets @004CE914, and KaargTownDialogVisualObject::LoadTownAssets @004D101A.
     * Full support port.
     */
    protected static CBmp64k loadBmp64k(String resourcePath) {
        try {
            return new CBmp64k(Resources.path(resourcePath));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load 64k town bitmap: " + resourcePath, e);
        }
    }

    /**
     * Native support extracted from CBmp256 constructor call sites in BasicTownDialogVisualObject::LoadTownAssets @004CB6C6,
     * DruidTownDialogVisualObject::loadTownAssets @004CE914, and KaargTownDialogVisualObject::LoadTownAssets @004D101A.
     * Full support port.
     */
    protected static CBmp256 loadBmp256(String resourcePath) {
        try {
            return new CBmp256(Resources.path(resourcePath));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load 256-color town bitmap: " + resourcePath, e);
        }
    }

    /**
     * Native support extracted from CA16 constructor and CGameBitmap::InitPalette call sites in BasicTownDialogVisualObject::LoadTownAssets @004CB6C6.
     * Full support port.
     */
    private static CA16 loadTownSprite(String resourcePath) {
        CA16 sprite = new CA16(Resources.path(resourcePath));
        sprite.initPalette(TOWN_SPRITE_PALETTE_PAGES, TOWN_SPRITE_PALETTE_MODE, 0);
        return sprite;
    }

    /**
     * Java helper for tips-child cleanup shared by OnMessage/ShowDialog/HideDialog.
     * not ported.
     */
    protected void clearTipsPromptChild() {
        if (tipsPromptDialog == null) {
            return;
        }
        removeChild(tipsPromptDialog);
        tipsPromptDialog.detachFromParent();
        tipsPromptDialog = null;
    }

    /**
     * Java helper for child overrides that need the raw HandlerVisualObject::ShowDialog dispatch under BasicTownDialogVisualObject.
     * not ported.
     */
    protected final void showHandlerDialog() {
        super.showDialog();
    }

    /**
     * Java helper for child overrides that need the raw HandlerVisualObject::Update dispatch under BasicTownDialogVisualObject.
     * not ported.
     */
    protected final void updateHandlerChildren() {
        super.update();
    }
}
