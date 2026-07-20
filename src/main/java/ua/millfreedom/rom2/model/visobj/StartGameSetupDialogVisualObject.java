package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.*;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.sound.Sound;
import ua.millfreedom.rom2.model.sound.SoundSystem;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.text.NpcNamesText;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static ua.millfreedom.rom2.model.enums.MessageCodes.*;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.NpcNamesText.*;
import static ua.millfreedom.rom2.text.StringTableIndex.*;
import static ua.millfreedom.rom2.text.TextTableId.NPCNAMES;

/**
 * Native class: StartGameSetupDialogVisualObject (vtbl @0x005CB998).
 * Purpose: start-game setup dialog (`id=0x466`) for starting hero portrait, difficulty, and name entry.
 */
public class StartGameSetupDialogVisualObject extends HandlerVisualObject {
    public static final int NATIVE_SIZE = 0x220; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int NO_HIT_CODE = -1;
    private static final int STATE_SELECTED = 0x1;
    private static final int STATE_HOVERED = 0x2;
    private static final int NPC_NAME_MAX_INDEX = 0x1A;
    private static final int DEFAULT_NPC_NAME_INDEX = 0x14;
    private static final int DEFAULT_TIP_ID = 8;
    private static final int CHARACTER_TIP_ID = 9;
    private static final int DIFFICULTY_TIP_ID = 10;
    private static final int TORCH_FRAME_COUNT = 15;
    private static final int TORCH_RIGHT_FRAME_OFFSET = 8;
    private static final int BLIND_ANIMATION_MIN_DELAY_MS = 500;
    private static final int NATIVE_RAND_MAX = 0x7FFF;
    private static final int BLIND_ANIMATION_DELAY_DIVISOR = 0x41;
    private static final int BLIND_ANIMATION_STEP_MS = 0x3F;
    private static final int SELECT_CURSOR_REFRESH_MS = 0x43;
    private static final int TIPS_PROMPT_TARGET_CAPTURE_DELAY_MS = 500;
    private static final int TIPS_PROMPT_TARGET_STEP_MS = 300;
    private static final int HIT_MASK_STRIDE = 0x280;
    private static final int PORTRAIT_LEFT_PAIR_X = 0x70;
    private static final int PORTRAIT_RIGHT_PAIR_X = 0x104;
    private static final int PORTRAIT_PAIR_Y = 0x2c;
    private static final int PORTRAIT_PAIR_WIDTH = 0x10c;
    private static final int PORTRAIT_PAIR_HEIGHT = 0x154;

    private static final int HIT_DIFFICULTY_EASY = 0x14;
    private static final int HIT_DIFFICULTY_MEDIUM = 0x28;
    private static final int HIT_DIFFICULTY_HARD = 0x3C;
    private static final int HIT_PORTRAIT_0 = 0x50;
    private static final int HIT_PORTRAIT_1 = 0x64;
    private static final int HIT_PORTRAIT_2 = 0x78;
    private static final int HIT_PORTRAIT_3 = 0x8C;
    private static final int HIT_RETURN_TO_GAME = 0xA0;
    private static final int HIT_ACCEPT = 0xB4;
    private static final int[] PORTRAIT_PLAYER_TYPES = {0, 2, 3, 1};
    private static final int[] PORTRAIT_BITMAP_LOAD_ORDER = {0, 3, 1, 2};

    private static final String PRECREATE_DIR = "graphics/interface/chrgen/precreate";
    private static final String PRECREATE_MAINAREA_BMP = PRECREATE_DIR + "/mainarea.bmp";
    private static final String PRECREATE_MASK_BMP = PRECREATE_DIR + "/mask.bmp";
    private static final String PRECREATE_RETURN_BMP = PRECREATE_DIR + "/cancell.bmp";
    private static final String PRECREATE_ACCEPT_BMP = PRECREATE_DIR + "/okl.bmp";
    private static final String PRECREATE_TABLEAU_BMP = PRECREATE_DIR + "/tablol.bmp";
    private static final String PRECREATE_BLIND_SPRITES_16A = PRECREATE_DIR + "/blind/sprites.16a";
    private static final String[] PORTRAIT_ON_BITMAPS = {
            PRECREATE_DIR + "/heroes/h1on.bmp",
            PRECREATE_DIR + "/heroes/h2on.bmp",
            PRECREATE_DIR + "/heroes/h3on.bmp",
            PRECREATE_DIR + "/heroes/h4on.bmp"
    };
    private static final String[] PORTRAIT_HOVER_BITMAPS = {
            PRECREATE_DIR + "/heroes/h1sel.bmp",
            PRECREATE_DIR + "/heroes/h2sel.bmp",
            PRECREATE_DIR + "/heroes/h3sel.bmp",
            PRECREATE_DIR + "/heroes/h4sel.bmp"
    };
    private static final String[] PORTRAIT_SELECTED_HOVER_BITMAPS = {
            PRECREATE_DIR + "/heroes/h1sel2.bmp",
            PRECREATE_DIR + "/heroes/h2sel1.bmp",
            PRECREATE_DIR + "/heroes/h3sel4.bmp",
            PRECREATE_DIR + "/heroes/h4sel3.bmp"
    };
    private static final String[] DIFFICULTY_SELECTED_BITMAPS = {
            PRECREATE_DIR + "/levels/level0on.bmp",
            PRECREATE_DIR + "/levels/level1on.bmp",
            PRECREATE_DIR + "/levels/level2on.bmp"
    };
    private static final String[] DIFFICULTY_HOVER_BITMAPS = {
            PRECREATE_DIR + "/levels/level0l.bmp",
            PRECREATE_DIR + "/levels/level1l.bmp",
            PRECREATE_DIR + "/levels/level2l.bmp"
    };
    private static final String[] DIFFICULTY_SELECTED_HOVER_BITMAPS = {
            PRECREATE_DIR + "/levels/level0lon.bmp",
            PRECREATE_DIR + "/levels/level1lon.bmp",
            PRECREATE_DIR + "/levels/level2lon.bmp"
    };
    private static final String TORCH1_FRAME_PATH_FORMAT = PRECREATE_DIR + "/torch1/t100%02d.bmp";
    private static final String TORCH2_FRAME_PATH_FORMAT = PRECREATE_DIR + "/torch2/t200%02d.bmp";

    private static final String SFX_LEVEL1_WAV = "sfx/chrgen/level1.wav";
    private static final String SFX_LEVEL2_WAV = "sfx/chrgen/level2.wav";
    private static final String SFX_LEVEL3_WAV = "sfx/chrgen/level3.wav";
    private static final String SFX_CHAR_WAV = "sfx/chrgen/char.wav";
    private static final String SFX_OK_WAV = "sfx/chrgen/ok.wav";
    private static final String SFX_LETTER1 = "sfx/letter1.wav";
    private static final String SFX_LETTER2 = "sfx/letter2.wav";
    private static final String SFX_LETTER3 = "sfx/letter3.wav";

    private static boolean nextBlindAnimationDelayInitialized;
    private static int nextBlindAnimationDelayMs;
    private static boolean blindAnimationTickInitialized;
    private static int lastBlindAnimationTick;
    private static boolean selectCursorRefreshInitialized;
    private static int lastSelectCursorRefreshTick;
    private static boolean tipsPromptTargetCaptureTickInitialized;
    private static int lastTipsPromptTargetCaptureTick;
    private static boolean tipsPromptTargetAdvanceTickInitialized;
    private static int lastTipsPromptTargetAdvanceTick;
    private static int capturedTipsPromptTargetHitCode = NO_HIT_CODE;
    private static int tipsPromptTargetHintIndex;
    private static int tipsPromptTargetHintCount;

    //0x68
    public CBmp64k mainAreaBitmap;
    //0x6c
    public CBmp256 hotspotMaskBitmap;
    //0x70
    public CA16 blindAnimation;
    //0x74
    public int blindAnimationFrame;
    //0x78
    public final Point blindAnimationPosition;
    //0x80
    public final List<CRect> blindSpawnRects;
    //0x94
    public final CBmp64k[] portraitSelectedBitmaps;
    //0xa8
    public final CBmp64k[] portraitHoverBitmaps;
    //0xbc
    public final CBmp64k[] portraitSelectedHoverBitmaps;
    //0xd0
    public final CBmp64k[] difficultySelectedBitmaps;
    //0xe4
    public final CBmp64k[] difficultyHoverBitmaps;
    //0xf8
    public final CBmp64k[] difficultySelectedHoverBitmaps;
    //0x10c
    public final List<CRect> portraitRects;
    //0x120
    public final List<CRect> difficultyRects;
    //0x134
    public final int[] portraitStateFlags;
    //0x148
    public final int[] difficultyStateFlags;
    //0x15c
    public CBmp64k returnToGameButtonBitmap;
    //0x160
    public CBmp64k acceptButtonBitmap;
    //0x164
    public CBmp64k tableauBitmap;
    //0x168
    public final List<CBmp64k> leftTorchFrames;
    //0x17c
    public final List<CBmp64k> rightTorchFrames;
    //0x190
    public final CRect returnToGameButtonRect;
    //0x1a0
    public final CRect acceptButtonRect;
    //0x1b0
    public int torchFrameTick;
    //0x1b4
    public CBmp64k returnToGameHoverBitmap;
    //0x1b8
    public CBmp64k acceptHoverBitmap;
    //0x1bc
    public int field0x1bc;
    //0x1c0
    public int field0x1c0;
    //0x1c4
    public Sound difficultyLevel1Sound;
    //0x1c8
    public Sound difficultyLevel2Sound;
    //0x1cc
    public Sound difficultyLevel3Sound;
    //0x1d0
    public Sound portraitSelectSound;
    //0x1d4
    public Sound acceptSound;
    //0x1d8
    public Sound returnSound;
    //0x1dc
    public Sound[] labelInputSound;
    //0x1e0
    //public Sound labelInputSound2;
    //0x1e4
    //public Sound labelInputSound3;
    //0x1e8
    public int labelInputSoundIndex;
    //0x1ec
    public SetupLabel464VisualObject networkNameLabel;
    //0x1f0
    public SetupLabel464VisualObject nameLabel;
    //0x1f4
    public SetupLabel464VisualObject clanLabel;
    //0x1f8
    public TipsPromptDialogVisualObject tipsPrompt;
    //0x1fc
    public String leaderName;
    //0x200
    public String clanName;
    //0x204
    public int field0x204;
    //0x208
    public int selectedPortraitIndex;
    //0x20c
    public int selectedDifficultyIndex;
    //0x210
    public int committedDifficultyIndex;
    //0x214
    public int committedPortraitIndex;
    //0x218
    public int tipsProgress;
    //0x21c
    public int dialogActiveFlag;

    /**
     * Native: StartGameSetupDialogVisualObject::StartGameSetupDialogVisualObject @00432584.
     * Fully ported.
     */
    public StartGameSetupDialogVisualObject() {
        super();
        blindAnimationPosition = new Point();
        blindSpawnRects = new ArrayList<>();
        portraitSelectedBitmaps = new CBmp64k[4];
        portraitHoverBitmaps = new CBmp64k[4];
        portraitSelectedHoverBitmaps = new CBmp64k[4];
        difficultySelectedBitmaps = new CBmp64k[3];
        difficultyHoverBitmaps = new CBmp64k[3];
        difficultySelectedHoverBitmaps = new CBmp64k[3];
        portraitRects = new ArrayList<>();
        difficultyRects = new ArrayList<>();
        portraitStateFlags = new int[4];
        difficultyStateFlags = new int[3];
        leftTorchFrames = new ArrayList<>();
        rightTorchFrames = new ArrayList<>();
        returnToGameButtonRect = new CRect();
        acceptButtonRect = new CRect();
        leaderName = "";
        clanName = "";
        initialize();
    }

    /**
     * Native: StartGameSetupDialogVisualObject::StartGameSetupDialogVisualObject @0043270D.
     * Fully ported.
     */
    public StartGameSetupDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        blindAnimationPosition = new Point();
        blindSpawnRects = new ArrayList<>();
        portraitSelectedBitmaps = new CBmp64k[4];
        portraitHoverBitmaps = new CBmp64k[4];
        portraitSelectedHoverBitmaps = new CBmp64k[4];
        difficultySelectedBitmaps = new CBmp64k[3];
        difficultyHoverBitmaps = new CBmp64k[3];
        difficultySelectedHoverBitmaps = new CBmp64k[3];
        portraitRects = new ArrayList<>();
        difficultyRects = new ArrayList<>();
        portraitStateFlags = new int[4];
        difficultyStateFlags = new int[3];
        leftTorchFrames = new ArrayList<>();
        rightTorchFrames = new ArrayList<>();
        returnToGameButtonRect = new CRect();
        acceptButtonRect = new CRect();
        leaderName = "";
        clanName = "";
        initialize();
    }

    /**
     * Native: StartGameSetupDialogVisualObject::SetLeaderName @00493CA0.
     * Fully ported.
     */
    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

    /**
     * Native: StartGameSetupDialogVisualObject::SetSelectedDifficulty @00493C80.
     * Fully ported.
     */
    public void setSelectedDifficulty(int selectedDifficulty) {
        selectedDifficultyIndex = selectedDifficulty;
    }

    /**
     * Native: StartGameSetupDialogVisualObject::SetSelectedPortrait @00493C60.
     * Fully ported.
     */
    public void setSelectedPortrait(int selectedPortrait) {
        selectedPortraitIndex = selectedPortrait;
    }

    /**
     * Native: StartGameSetupDialogVisualObject::GetLeaderName @00493D00.
     * Fully ported.
     */
    public String getLeaderName() {
        return leaderName;
    }

    /**
     * Native: StartGameSetupDialogVisualObject::GetClanName @00493D40.
     * Fully ported.
     */
    public String getClanName() {
        return clanName;
    }

    /**
     * Native: StartGameSetupDialogVisualObject::GetCommittedPortraitPlayerType @00493CC0.
     * Fully ported.
     */
    public int getCommittedPortraitPlayerType() {
        return PORTRAIT_PLAYER_TYPES[selectedPortraitIndex];
    }

    /**
     * Native: StartGameSetupDialogVisualObject::GetCommittedDifficultyIndex @00493CE0.
     * Fully ported.
     */
    public int getCommittedDifficultyIndex() {
        return selectedDifficultyIndex;
    }

    /**
     * vtbl +0x14: StartGameSetupDialogVisualObject::GetText @004374BF.
     * Fully ported.
     */
    @Override
    public String getText() {
        if (dialogActiveFlag == 0) {
            return null;
        }
        int hitCode = getHitCodeAtScreenPoint(Globals.mousePointer.getX(), Globals.mousePointer.getY());
        if (isCampaignStartGameSetup()) {
            if (hitCode == HIT_DIFFICULTY_EASY) {
                return get(MAIN_EASY_247);
            }
            if (hitCode == HIT_DIFFICULTY_MEDIUM) {
                return get(MAIN_MEDIUM_248);
            }
            if (hitCode == HIT_DIFFICULTY_HARD) {
                return get(MAIN_HARD_249);
            }
        }
        return switch (hitCode) {
            case HIT_PORTRAIT_0 -> get(MAIN_MALE_FIGHTER_250);
            case HIT_PORTRAIT_1 -> get(MAIN_FEMALE_FIGHTER_252);
            case HIT_PORTRAIT_2 -> get(MAIN_FEMALE_MAGE_253);
            case HIT_PORTRAIT_3 -> get(MAIN_MALE_MAGE_251);
            case HIT_RETURN_TO_GAME -> get(MAIN_RETURN_TO_MAIN_MENU_255);
            case HIT_ACCEPT -> get(MAIN_CONTINUE_254);
            default -> null;
        };
    }

    /**
     * vtbl +0x2C: StartGameSetupDialogVisualObject::Update @0043541E.
     * Fully ported.
     */
    @Override
    public void update() {
        int currentTick = currentTick();
        int screenLeft = cRect.left;
        int screenTop = cRect.top;
        ensureBlindAnimationTimersInitialized(currentTick);
        ensureSelectCursorRefreshInitialized(currentTick);
        if (Integer.compareUnsigned(currentTick - lastSelectCursorRefreshTick, SELECT_CURSOR_REFRESH_MS) <= 0) {
            return;
        }

        lastSelectCursorRefreshTick = currentTick;
        if (!isSelectCursorActive()) {
            CMousePointer.Cursor_Select.setToMousePointer();
        }

        Globals.renderer.pushClip(cRect.left, cRect.top, cRect.right, cRect.bottom);
        try {
            Globals.renderer.lockSurface();
            try {
                renderBackground(screenLeft, screenTop);
                renderBottomButtonHoverBitmaps(screenLeft, screenTop);
                if (isCampaignStartGameSetup()) {
                    renderDifficultyChoices(screenLeft, screenTop);
                }
                renderPortraitChoices(screenLeft, screenTop);
                renderBottomButtonHoverBitmaps(screenLeft, screenTop);
                renderTipsPrompt();
                renderBlindAnimation(currentTick, screenLeft, screenTop);
                renderPromptLabel(getChildById(0x464), MAIN_NAME_365, screenLeft, screenTop);
                renderPromptLabel(getChildById(0x465), MAIN_CLAN_366, screenLeft, screenTop);
                renderTorchFlames(screenLeft, screenTop);
            } finally {
                Globals.renderer.unlockSurface();
            }
        } finally {
            Globals.renderer.popClip();
        }
        super.update();
    }

    /**
     * vtbl +0x30: StartGameSetupDialogVisualObject::RenderSelf @00438470.
     * Fully ported.
     */
    @Override
    public void renderSelf(CRect clipRect) {
        // Native no-op.
    }

    /**
     * vtbl +0x48: StartGameSetupDialogVisualObject::OnMessage @00436C1D.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        if (msg == RENDER_FRAME) {
            draw();
        } else if (msg == CLEAR_TIP_PROMPT && tipsPrompt != null) {
            removeTipsPrompt();
        }
        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * vtbl +0x4C: StartGameSetupDialogVisualObject::OnMouseMove @00436D10.
     * Fully ported.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        updateHoveredChoice(x, y, (nFlags & 1) != 0);
        return super.onMouseMove(nFlags, x, y);
    }

    /**
     * vtbl +0x54: StartGameSetupDialogVisualObject::OnLButtonDown @00436D48.
     * Fully ported.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        int hitCode = updateHoveredChoice(x, y, true);
        SetupLabel464VisualObject activeNameLabel = isCampaignStartGameSetup()
                ? networkNameLabel
                : nameLabel;
        maybeAdvanceTipsPrompt(hitCode);
        switch (hitCode) {
            case HIT_DIFFICULTY_EASY -> {
                if (isCampaignStartGameSetup()) {
                    committedDifficultyIndex = 0;
                    stopAndPlayPointerSound(difficultyLevel1Sound);
                }
            }
            case HIT_DIFFICULTY_MEDIUM -> {
                if (isCampaignStartGameSetup()) {
                    committedDifficultyIndex = 1;
                    stopAndPlayPointerSound(difficultyLevel2Sound);
                }
            }
            case HIT_DIFFICULTY_HARD -> {
                if (isCampaignStartGameSetup()) {
                    committedDifficultyIndex = 2;
                    stopAndPlayPointerSound(difficultyLevel3Sound);
                }
            }
            case HIT_PORTRAIT_0 -> {
                if (committedPortraitIndex != 0 && isSetupDefaultNpcName(activeNameLabel.getLabelText())) {
                    activeNameLabel.setLabelText(get(NPCNAMES, ALDOR_23));
                }
                committedPortraitIndex = 0;
                stopAndPlayPointerSound(portraitSelectSound);
            }
            case HIT_PORTRAIT_1 -> {
                if (committedPortraitIndex != 1 && isSetupDefaultNpcName(activeNameLabel.getLabelText())) {
                    activeNameLabel.setLabelText(get(NPCNAMES, SAGITA_24));
                }
                committedPortraitIndex = 1;
                stopAndPlayPointerSound(portraitSelectSound);
            }
            case HIT_PORTRAIT_2 -> {
                if (committedPortraitIndex != 2 && isSetupDefaultNpcName(activeNameLabel.getLabelText())) {
                    activeNameLabel.setLabelText(get(NPCNAMES, GALINEL_26));
                }
                committedPortraitIndex = 2;
                stopAndPlayPointerSound(portraitSelectSound);
            }
            case HIT_PORTRAIT_3 -> {
                if (committedPortraitIndex != 3 && isSetupDefaultNpcName(activeNameLabel.getLabelText())) {
                    activeNameLabel.setLabelText(get(NPCNAMES, GILDARIUS_25));
                }
                committedPortraitIndex = 3;
                stopAndPlayPointerSound(portraitSelectSound);
            }
            case HIT_RETURN_TO_GAME -> returnToGame();
            case HIT_ACCEPT -> acceptStartGameSetupDialog();
            default -> {
            }
        }
        return super.onLButtonDown(nFlags, x, y);
    }

    /**
     * vtbl +0x6C: StartGameSetupDialogVisualObject::OnKeyDown @00436CC3.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        if (nChar == 0x0D) {
            acceptStartGameSetupDialog();
            return 1;
        }
        if (nChar == 0x1B) {
            returnToGame();
            return 1;
        }
        return super.onKeyDown(nChar);
    }

    /**
     * vtbl +0x78: StartGameSetupDialogVisualObject::InitializeStartGameSetupDialog @00432A6B.
     * Fully ported.
     */
    @Override
    public void initialize() {
        dialogActiveFlag = 0;
        mainAreaBitmap = null;
        hotspotMaskBitmap = null;
        blindAnimation = null;
        blindAnimationFrame = 0;
        blindAnimationPosition.setLocation(0, 0);
        returnToGameButtonBitmap = null;
        acceptButtonBitmap = null;
        tableauBitmap = null;
        returnToGameHoverBitmap = null;
        acceptHoverBitmap = null;
        field0x1bc = 0;
        field0x1c0 = 0;
        difficultyLevel1Sound = null;
        difficultyLevel2Sound = null;
        difficultyLevel3Sound = null;
        portraitSelectSound = null;
        acceptSound = null;
        returnSound = null;
        labelInputSound = new Sound[3];
        //labelInputSound2 = null;
        //labelInputSound3 = null;
        labelInputSoundIndex = 0;
        portraitRects.clear();
        difficultyRects.clear();
        blindSpawnRects.clear();
        clearStateFlags(portraitStateFlags);
        clearStateFlags(difficultyStateFlags);

        portraitRects.add(new CRect(0x74, 0x2C, 0xB4, 0x120));
        portraitRects.add(new CRect(0xB4, 0x2C, 0xF4, 0x120));
        portraitRects.add(new CRect(0x188, 0x2C, 0x1C8, 0x120));
        portraitRects.add(new CRect(0x1C8, 0x2C, 0x208, 0x120));

        difficultyRects.add(new CRect(0x08, 0x00, 0x38, 0x48));
        difficultyRects.add(new CRect(0x128, 0x00, 0x158, 0x48));
        difficultyRects.add(new CRect(0x244, 0x00, 0x274, 0x48));

        returnToGameButtonRect.set(0x10, 0x190, 0x50, 0x1DC);
        acceptButtonRect.set(0x224, 0x190, 0x274, 0x1DC);

        for (int i = 0; i < difficultyRects.size(); i++) {
            blindSpawnRects.add(new CRect(difficultyRects.get(i)));
        }
        blindSpawnRects.add(new CRect(returnToGameButtonRect));
        blindSpawnRects.add(new CRect(acceptButtonRect));

        leaderName = "Master Oberic";
        networkNameLabel = new SetupLabel464VisualObject(0x464, 300, 0x1B1, 0x1D0, 0x1C1, this);
        nameLabel = new SetupLabel464VisualObject(0x464, 300, 0x1A6, 0x1D0, 0x1B6, this);
        clanLabel = new SetupLabel464VisualObject(0x465, 300, 0x1BC, 0x1D0, 0x1CC, this);
        tipsPrompt = null;
        tipsProgress = 0;
        torchFrameTick = 0;
    }

    /**
     * vtbl +0x80: StartGameSetupDialogVisualObject::ShowDialog @004334DF.
     * Fully ported.
     */
    @Override
    public void showDialog() {
        Globals.mousePointer.disableBackgroundCapture();
        torchFrameTick = 0;
        loadStartGameSetupGraphics();
        loadStartGameSetupSounds();
        resetStartGameSetupTransientState();
        blindAnimationPosition.setLocation(0, 0);
        blindAnimationFrame = 0;
        clearStateFlags(portraitStateFlags);
        clearStateFlags(difficultyStateFlags);
        selectedPortraitIndex = 0;
        portraitStateFlags[selectedPortraitIndex] = STATE_SELECTED;
        difficultyStateFlags[selectedDifficultyIndex] = STATE_SELECTED;
        if (isSetupDefaultNpcName(leaderName)) {
            leaderName = get(NPCNAMES, NpcNamesText.byIndex(DEFAULT_NPC_NAME_INDEX));
        }
        committedPortraitIndex = selectedPortraitIndex;
        committedDifficultyIndex = selectedDifficultyIndex;
        removeChildById(0x464);
        removeChildById(0x465);
        if (isCampaignStartGameSetup()) {
            addChild(networkNameLabel);
            networkNameLabel.setLabelText(leaderName);
        } else {
            addChild(nameLabel);
            nameLabel.setLabelText(leaderName);
            addChild(clanLabel);
            clanLabel.setLabelText(clanName);
        }
        if (isTipsModeEnabled()) {
            ensureTipsPrompt(resolveStartGameSetupTipText(DEFAULT_TIP_ID));
        } else {
            removeTipsPrompt();
        }

        tipsProgress = 0;
        clearScreen();
        super.showDialog();
        CMousePointer.Cursor_Select.setToMousePointer();
        dialogActiveFlag = 1;
        Globals.mousePointer.enableBackgroundCapture();
    }

    /**
     * vtbl +0x84: StartGameSetupDialogVisualObject::HideDialog @0043389D.
     * Fully ported.
     */
    @Override
    public HandlerVisualObject hideDialog(MessageCodes reason) {
        draw();
        dialogActiveFlag = 0;
        releaseStartGameSetupGraphics();
        releaseStartGameSetupSounds();
        if (isCampaignStartGameSetup()) {
            leaderName = networkNameLabel.getLabelText();
        } else {
            leaderName = nameLabel.getLabelText();
            clanName = clanLabel.getLabelText();
        }
        removeTipsPrompt();
        return super.hideDialog(reason);
    }

    /**
     * Native: StartGameSetupDialogVisualObject::PlaySetupLabel464InputSound @00437985.
     * Fully ported.
     */
    public void playSetupLabel464InputSound() {
        Sound.playPointer(labelInputSound, labelInputSoundIndex);
    }

    /**
     * Native: StartGameSetupDialogVisualObject::AcceptStartGameSetupDialog @00437829.
     * Fully ported.
     */
    private void acceptStartGameSetupDialog() {
        SetupLabel464VisualObject activeNameLabel = isCampaignStartGameSetup()
                ? networkNameLabel
                : nameLabel;
        if (!activeNameLabel.getLabelText().isEmpty()) {
            playPointerSound(acceptSound);
            onMessage(DIALOG_OK, 0, 0);
        }
    }

    /**
     * Native: StartGameSetupDialogVisualObject::ReturnToGame @00437955.
     * Fully ported.
     */
    private void returnToGame() {
        playPointerSound(returnSound);
        onMessage(RETURN_TO_GAME, 0, 0);
    }

    /**
     * Native: StartGameSetupDialogVisualObject::LoadStartGameSetupGraphics @00433A3D.
     * Fully ported.
     */
    private void loadStartGameSetupGraphics() {
        releaseStartGameSetupGraphics();
        hotspotMaskBitmap = loadBmp256(PRECREATE_MASK_BMP);
        mainAreaBitmap = loadBmp64k(PRECREATE_MAINAREA_BMP);
        returnToGameButtonBitmap = loadBmp64k(PRECREATE_RETURN_BMP);
        acceptButtonBitmap = loadBmp64k(PRECREATE_ACCEPT_BMP);
        tableauBitmap = loadBmp64k(PRECREATE_TABLEAU_BMP);
        blindAnimation = loadSprite(PRECREATE_BLIND_SPRITES_16A);
        blindAnimation.initPalette(0x10, 4, 0);
        loadBitmapArray(portraitHoverBitmaps, PORTRAIT_HOVER_BITMAPS, PORTRAIT_BITMAP_LOAD_ORDER);
        loadBitmapArray(portraitSelectedBitmaps, PORTRAIT_ON_BITMAPS, PORTRAIT_BITMAP_LOAD_ORDER);
        loadBitmapArray(portraitSelectedHoverBitmaps, PORTRAIT_SELECTED_HOVER_BITMAPS, PORTRAIT_BITMAP_LOAD_ORDER);
        loadBitmapArray(difficultySelectedBitmaps, DIFFICULTY_SELECTED_BITMAPS);
        loadBitmapArray(difficultyHoverBitmaps, DIFFICULTY_HOVER_BITMAPS);
        loadBitmapArray(difficultySelectedHoverBitmaps, DIFFICULTY_SELECTED_HOVER_BITMAPS);
        loadTorchFrames(leftTorchFrames, TORCH1_FRAME_PATH_FORMAT);
        loadTorchFrames(rightTorchFrames, TORCH2_FRAME_PATH_FORMAT);
    }

    /**
     * Native: StartGameSetupDialogVisualObject::ReleaseStartGameSetupGraphics @00434749.
     * Fully ported.
     */
    private void releaseStartGameSetupGraphics() {
        blindAnimation = releaseSprite(blindAnimation);
        mainAreaBitmap = releaseBmp(mainAreaBitmap);
        hotspotMaskBitmap = releaseBmp256(hotspotMaskBitmap);
        acceptButtonBitmap = releaseBmp(acceptButtonBitmap);
        returnToGameButtonBitmap = releaseBmp(returnToGameButtonBitmap);
        tableauBitmap = releaseBmp(tableauBitmap);
        returnToGameHoverBitmap = null;
        acceptHoverBitmap = null;
        field0x1bc = 0;
        field0x1c0 = 0;
        releasePortraitBitmapArrays(portraitHoverBitmaps, portraitSelectedBitmaps, portraitSelectedHoverBitmaps);
        releaseDifficultyBitmapArrays(difficultySelectedBitmaps, difficultyHoverBitmaps, difficultySelectedHoverBitmaps);
        releaseTorchFramePairs(leftTorchFrames, rightTorchFrames);
    }

    /**
     * Native: StartGameSetupDialogVisualObject::ResetStartGameSetupTransientState @004353D2.
     * Fully ported.
     */
    private void resetStartGameSetupTransientState() {
        field0x204 = 0;
        returnToGameHoverBitmap = null;
        acceptHoverBitmap = null;
        field0x1bc = 0;
        field0x1c0 = 0;
    }

    /**
     * Native: StartGameSetupDialogVisualObject::UpdateHoveredChoice @00434F15.
     * Fully ported.
     */
    private int updateHoveredChoice(int screenX, int screenY, boolean pressed) {
        int hitCode = getHitCodeAtScreenPoint(screenX, screenY);
        clearPortraitHoverFlags();
        clearDifficultyHoverFlags();
        returnToGameHoverBitmap = null;
        acceptHoverBitmap = null;
        if (pressed) {
            switch (hitCode) {
                case HIT_DIFFICULTY_EASY -> {
                    clearDifficultySelectionFlags();
                    difficultyStateFlags[0] |= STATE_SELECTED;
                    selectedDifficultyIndex = 0;
                }
                case HIT_DIFFICULTY_MEDIUM -> {
                    clearDifficultySelectionFlags();
                    difficultyStateFlags[1] |= STATE_SELECTED;
                    selectedDifficultyIndex = 1;
                }
                case HIT_DIFFICULTY_HARD -> {
                    clearDifficultySelectionFlags();
                    difficultyStateFlags[2] |= STATE_SELECTED;
                    selectedDifficultyIndex = 2;
                }
                case HIT_PORTRAIT_0 -> {
                    clearPortraitSelectionFlags();
                    portraitStateFlags[0] |= STATE_SELECTED;
                    selectedPortraitIndex = 0;
                }
                case HIT_PORTRAIT_1 -> {
                    clearPortraitSelectionFlags();
                    portraitStateFlags[1] |= STATE_SELECTED;
                    selectedPortraitIndex = 1;
                }
                case HIT_PORTRAIT_2 -> {
                    clearPortraitSelectionFlags();
                    portraitStateFlags[2] |= STATE_SELECTED;
                    selectedPortraitIndex = 2;
                }
                case HIT_PORTRAIT_3 -> {
                    clearPortraitSelectionFlags();
                    portraitStateFlags[3] |= STATE_SELECTED;
                    selectedPortraitIndex = 3;
                }
                default -> {
                }
            }
        }

        switch (hitCode) {
            case HIT_DIFFICULTY_EASY -> difficultyStateFlags[0] |= STATE_HOVERED;
            case HIT_DIFFICULTY_MEDIUM -> difficultyStateFlags[1] |= STATE_HOVERED;
            case HIT_DIFFICULTY_HARD -> difficultyStateFlags[2] |= STATE_HOVERED;
            case HIT_PORTRAIT_0 -> portraitStateFlags[0] |= STATE_HOVERED;
            case HIT_PORTRAIT_1 -> portraitStateFlags[1] |= STATE_HOVERED;
            case HIT_PORTRAIT_2 -> portraitStateFlags[2] |= STATE_HOVERED;
            case HIT_PORTRAIT_3 -> portraitStateFlags[3] |= STATE_HOVERED;
            case HIT_RETURN_TO_GAME -> returnToGameHoverBitmap = returnToGameButtonBitmap;
            case HIT_ACCEPT -> acceptHoverBitmap = acceptButtonBitmap;
            default -> {
            }
        }
        return hitCode;
    }

    /**
     * Native: StartGameSetupDialogVisualObject::GetHitCodeAtScreenPoint @00434D74.
     * Fully ported.
     */
    private int getHitCodeAtScreenPoint(int screenX, int screenY) {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        if (!screenRect.contains(screenX, screenY)) {
            return NO_HIT_CODE;
        }

        GameBitmapFrame hitFrame = hotspotMaskBitmap.frames.get(0);
        int localX = screenX - screenRect.left;
        int localY = screenY - screenRect.top;
        return hitFrame.pixels()[localY * HIT_MASK_STRIDE + localX];
    }

    /**
     * Native: StartGameSetupDialogVisualObject::LoadStartGameSetupSounds @004376A0.
     * Fully ported.
     */
    private void loadStartGameSetupSounds() {
        releaseStartGameSetupSounds();
        difficultyLevel1Sound = loadSound(SFX_LEVEL1_WAV);
        difficultyLevel2Sound = loadSound(SFX_LEVEL2_WAV);
        difficultyLevel3Sound = loadSound(SFX_LEVEL3_WAV);
        portraitSelectSound = loadSound(SFX_CHAR_WAV);
        acceptSound = loadSound(SFX_OK_WAV);
        labelInputSound[0] = loadSound(SFX_LETTER1);
        labelInputSound[1] = loadSound(SFX_LETTER2);
        labelInputSound[2] = loadSound(SFX_LETTER3);
        returnSound = loadSound(SFX_OK_WAV);
    }

    /**
     * Native: StartGameSetupDialogVisualObject::ReleaseStartGameSetupSounds @0043777F.
     * Fully ported.
     */
    private void releaseStartGameSetupSounds() {
        difficultyLevel1Sound = releaseSound(difficultyLevel1Sound);
        difficultyLevel2Sound = releaseSound(difficultyLevel2Sound);
        difficultyLevel3Sound = releaseSound(difficultyLevel3Sound);
        portraitSelectSound = releaseSound(portraitSelectSound);
        acceptSound = releaseSound(acceptSound);
        labelInputSound[0] = releaseSound(labelInputSound[0]);
        labelInputSound[1] = releaseSound(labelInputSound[1]);
        labelInputSound[2] = releaseSound(labelInputSound[2]);
        returnSound = releaseSound(returnSound);

    }

    /**
     * Native: StartGameSetupDialogVisualObject::MaybeAdvanceTipsPrompt @004372D1.
     * Fully ported.
     */
    private void maybeAdvanceTipsPrompt(int hitCode) {
        if (!isTipsModeEnabled() || tipsPrompt == null) {
            return;
        }
        if ((hitCode == HIT_DIFFICULTY_EASY || hitCode == HIT_DIFFICULTY_MEDIUM || hitCode == HIT_DIFFICULTY_HARD)
                && tipsProgress == 1) {
            tipsProgress += 1;
            tipsPrompt.setPromptText(resolveStartGameSetupTipText(DIFFICULTY_TIP_ID));
        } else if ((hitCode == HIT_PORTRAIT_0 || hitCode == HIT_PORTRAIT_1 || hitCode == HIT_PORTRAIT_2 || hitCode == HIT_PORTRAIT_3)
                && tipsProgress == 0) {
            tipsProgress += 1;
            tipsPrompt.setPromptText(resolveStartGameSetupTipText(CHARACTER_TIP_ID));
        }
    }

    /**
     * Native support boundary for tips-prompt allocation in StartGameSetupDialogVisualObject::ShowDialog @004334DF.
     */
    private void ensureTipsPrompt(String promptText) {
        tipsPrompt = new TipsPromptDialogVisualObject(0x467, 0xE8, 0x30, 0x280, 0xB8, promptText);
        addChild(tipsPrompt);
    }

    /**
     * Native child-removal tail shared by StartGameSetupDialogVisualObject::OnMessage @00436C1D and ::HideDialog @0043389D.
     */
    private void removeTipsPrompt() {
        if (tipsPrompt == null) {
            return;
        }
        removeChild(tipsPrompt);
        tipsPrompt.detachFromParentSlot(1);
        tipsPrompt = null;
    }

    /**
     * Native support extracted from StartGameSetupDialogVisualObject::Update @0043541E.
     */
    private void renderBackground(int screenLeft, int screenTop) {
        mainAreaBitmap.draw(screenLeft, screenTop, 0, null, false);
    }

    /**
     * Native support extracted from the level-state draw loop in StartGameSetupDialogVisualObject::Update @0043541E.
     */
    private void renderDifficultyChoices(int screenLeft, int screenTop) {
        for (int i = 0; i < difficultyStateFlags.length; i++) {
            if (difficultyStateFlags[i] == 0) {
                continue;
            }
            CBmp64k bitmap = selectStateBitmap(
                    difficultyStateFlags[i],
                    difficultySelectedBitmaps[i],
                    difficultyHoverBitmaps[i],
                    difficultySelectedHoverBitmaps[i]
            );
            if (bitmap != null) {
                CRect rect = difficultyRects.get(i);
                bitmap.drawRectMasked(screenLeft + rect.left, screenTop + rect.top, 0, 0, rect.width(), rect.height());
            }
        }
    }

    /**
     * Native support extracted from StartGameSetupDialogVisualObject::Update @0043541E.
     */
    private void renderPortraitChoices(int screenLeft, int screenTop) {
        if ((portraitStateFlags[0] & STATE_SELECTED) != 0) {
            drawPortraitPair(screenLeft, screenTop, 0, 1, PORTRAIT_LEFT_PAIR_X);
            drawHoveredSelectedPortrait(screenLeft, screenTop, 2);
            drawHoveredSelectedPortrait(screenLeft, screenTop, 3);
        }
        if ((portraitStateFlags[1] & STATE_SELECTED) != 0) {
            drawPortraitPair(screenLeft, screenTop, 1, 0, PORTRAIT_LEFT_PAIR_X);
            drawHoveredSelectedPortrait(screenLeft, screenTop, 2);
            drawHoveredSelectedPortrait(screenLeft, screenTop, 3);
        }
        if ((portraitStateFlags[2] & STATE_SELECTED) != 0) {
            drawPortraitPair(screenLeft, screenTop, 2, 3, PORTRAIT_RIGHT_PAIR_X);
            drawHoveredSelectedPortrait(screenLeft, screenTop, 0);
            drawHoveredSelectedPortrait(screenLeft, screenTop, 1);
        }
        if ((portraitStateFlags[3] & STATE_SELECTED) != 0) {
            drawPortraitPair(screenLeft, screenTop, 3, 2, PORTRAIT_RIGHT_PAIR_X);
            drawHoveredSelectedPortrait(screenLeft, screenTop, 0);
            drawHoveredSelectedPortrait(screenLeft, screenTop, 1);
        }
    }

    /**
     * Native support extracted from StartGameSetupDialogVisualObject::Update @0043541E.
     */
    private void drawPortraitPair(int screenLeft, int screenTop, int selectedIndex, int pairedIndex, int pairX) {
        CBmp64k bitmap = (portraitStateFlags[pairedIndex] & STATE_HOVERED) == 0
                ? portraitHoverBitmaps[selectedIndex]
                : portraitSelectedHoverBitmaps[selectedIndex];
        bitmap.drawRectMasked(screenLeft + pairX, screenTop + PORTRAIT_PAIR_Y, 0, 0, PORTRAIT_PAIR_WIDTH,
                PORTRAIT_PAIR_HEIGHT);
    }

    /**
     * Native support extracted from StartGameSetupDialogVisualObject::Update @0043541E.
     */
    private void drawHoveredSelectedPortrait(int screenLeft, int screenTop, int portraitIndex) {
        if ((portraitStateFlags[portraitIndex] & STATE_HOVERED) == 0) {
            return;
        }
        CRect rect = portraitRects.get(portraitIndex);
        portraitSelectedBitmaps[portraitIndex].drawRectMasked(screenLeft + rect.left, screenTop + rect.top, 0, 0,
                rect.width(), rect.height());
    }

    /**
     * Native support extracted from StartGameSetupDialogVisualObject::Update @0043541E.
     * Preserves the native null checks for optional hover bitmap fields.
     */
    private void renderBottomButtonHoverBitmaps(int screenLeft, int screenTop) {
        if (returnToGameHoverBitmap != null) {
            returnToGameHoverBitmap.drawRectMasked(
                    screenLeft + returnToGameButtonRect.left,
                    screenTop + returnToGameButtonRect.top,
                    0,
                    0,
                    returnToGameButtonRect.width(),
                    returnToGameButtonRect.height()
            );
        }
        if (acceptHoverBitmap != null) {
            acceptHoverBitmap.drawRectMasked(
                    screenLeft + acceptButtonRect.left,
                    screenTop + acceptButtonRect.top,
                    0,
                    0,
                    acceptButtonRect.width(),
                    acceptButtonRect.height()
            );
        }
    }

    /**
     * Native helper for the blind-sprite timer/random-position flow in StartGameSetupDialogVisualObject::Update @0043541E.
     */
    private void renderBlindAnimation(int currentTick, int screenLeft, int screenTop) {
        if (blindAnimationFrame == 0) {
            if (Integer.compareUnsigned(currentTick - lastBlindAnimationTick, nextBlindAnimationDelayMs) > 0) {
                CRect rect = selectRandomBlindSpawnRect();
                blindAnimationPosition.x = rect.left + nativeRandScaled(rect.width());
                blindAnimationPosition.y = rect.top + nativeRandScaled(rect.height());
                blindAnimationFrame = 1;
                nextBlindAnimationDelayMs = computeNextBlindAnimationDelayMs();
            }
            return;
        }
        blindAnimation.draw(
                screenLeft + blindAnimationPosition.x,
                screenTop + blindAnimationPosition.y,
                blindAnimationFrame,
                0,
                false
        );
        if (Integer.compareUnsigned(currentTick - lastBlindAnimationTick, BLIND_ANIMATION_STEP_MS) > 0) {
            blindAnimationFrame = (blindAnimationFrame + 1) % blindAnimation.frameCount;
            lastBlindAnimationTick = currentTick;
        }
    }

    /**
     * Native helper for the recovered Name/Clan `gFont4` draws in StartGameSetupDialogVisualObject::Update @0043541E.
     */
    private void renderPromptLabel(CVisualObject label, int stringTableIndex, int screenLeft, int screenTop) {
        if (label == null) {
            return;
        }
        CRect labelRect = label.getRect();
        Palette16 palette = Palettes.p1.paletteData[0];
        Globals.fonts.font4.drawTextInternal(
                screenLeft - 10 + labelRect.left,
                screenTop + labelRect.top,
                get(stringTableIndex),
                TextAlign.RIGHT.mask,
                palette
        );
    }

    /**
     * Native helper for the two torch-frame draws in StartGameSetupDialogVisualObject::Update @0043541E.
     */
    private void renderTorchFlames(int screenLeft, int screenTop) {
        torchFrameTick += 1;
        int leftFrameIndex = torchFrameTick % TORCH_FRAME_COUNT;
        int rightFrameIndex = (torchFrameTick + TORCH_RIGHT_FRAME_OFFSET) % TORCH_FRAME_COUNT;
        getTorchFrame(leftTorchFrames, leftFrameIndex).draw(screenLeft + 4, screenTop + 200, 0, null, false);
        getTorchFrame(rightTorchFrames, rightFrameIndex).draw(screenLeft + 0x24C, screenTop + 200, 0, null, false);
    }

    /**
     * Native helper for the tip-prompt update branch in StartGameSetupDialogVisualObject::Update @0043541E.
     */
    private void renderTipsPrompt() {
        if (!isTipsModeEnabled() || tipsPrompt == null) {
            return;
        }
        int hitCode = getHitCodeAtScreenPoint(Globals.mousePointer.getX(), Globals.mousePointer.getY());
        renderTipsPromptTargetHint(hitCode);
    }

    /**
     * Native: StartGameSetupDialogVisualObject::RenderTipsPromptTargetHint @004364F7.
     * Fully ported.
     */
    private int renderTipsPromptTargetHint(int hitCode) {
        int screenLeft = cRect.left;
        int screenTop = cRect.top;
        ensureTipsPromptTargetHintTimersInitialized();
        int currentTick = currentTick();
        int result = currentTick;
        int nextAdvanceTick = currentTick;

        if (Integer.compareUnsigned(
                currentTick - lastTipsPromptTargetCaptureTick,
                TIPS_PROMPT_TARGET_CAPTURE_DELAY_MS - 1
        ) > 0) {
            applyCapturedTipsPromptTargetHit();
            if (tipsProgress == 0) {
                if (isPortraitHit(hitCode)) {
                    captureTipsPromptTargetHit(hitCode, currentTick);
                    return currentTick;
                }
                tipsPromptTargetHintCount = 4;
                tipsPromptTargetHintIndex %= tipsPromptTargetHintCount;
                drawPortraitTipsPromptTargetHint(screenLeft, screenTop);
            } else if (tipsProgress == 1) {
                if (isDifficultyHit(hitCode)) {
                    captureTipsPromptTargetHit(hitCode, currentTick);
                    return currentTick;
                }
                tipsPromptTargetHintCount = 3;
                tipsPromptTargetHintIndex %= tipsPromptTargetHintCount;
                drawDifficultyTipsPromptTargetHint(screenLeft, screenTop);
            } else if (tipsProgress == 2) {
                if (isBottomButtonHit(hitCode)) {
                    captureTipsPromptTargetHit(hitCode, currentTick);
                    return currentTick;
                }
                tipsPromptTargetHintCount = 2;
                tipsPromptTargetHintIndex %= tipsPromptTargetHintCount;
                drawBottomButtonTipsPromptTargetHint(screenLeft, screenTop);
            } else {
                tipsPromptTargetHintCount = -1;
            }

            capturedTipsPromptTargetHitCode = NO_HIT_CODE;
            result = currentTick - lastTipsPromptTargetAdvanceTick;
            nextAdvanceTick = lastTipsPromptTargetAdvanceTick;
            if (Integer.compareUnsigned(result, TIPS_PROMPT_TARGET_STEP_MS) > 0
                    && tipsPromptTargetHintCount != -1) {
                int nextHintIndex = tipsPromptTargetHintIndex + 1;
                tipsPromptTargetHintIndex = nextHintIndex % tipsPromptTargetHintCount;
                result = nextHintIndex / tipsPromptTargetHintCount;
                nextAdvanceTick = currentTick;
            }
        }

        lastTipsPromptTargetAdvanceTick = nextAdvanceTick;
        return result;
    }

    /**
     * Native support extracted from `g_GamePreferences.TipsMode` reads in StartGameSetupDialogVisualObject::ShowDialog @004334DF,
     * ::Update @0043541E, and ::MaybeAdvanceTipsPrompt @004372D1.
     */
    private boolean isTipsModeEnabled() {
        return Globals.gamePreferences.tipsMode != 0;
    }

    /**
     * Native support for `#tips<n>` script lookup via FUN_004DDC9C in StartGameSetupDialogVisualObject::ShowDialog @004334DF
     * and ::MaybeAdvanceTipsPrompt @004372D1.
     */
    private String resolveStartGameSetupTipText(int tipIndex) {
        return ScriptDataSupport.getTipText(tipIndex);
    }

    /**
     * Native support extracted from the `CMousePointer::GetSourceBitmap() == g_Cursor_Select` check in
     * StartGameSetupDialogVisualObject::Update @0043541E.
     */
    private boolean isSelectCursorActive() {
        return Globals.mousePointer.getSourceBitmap() == CMousePointer.Cursor_Select.getBitmap();
    }

    /**
     * Native support extracted from Sound::PlayPointer @00438570 calls on single StartGameSetup sound fields.
     */
    private static void playPointerSound(Sound sound) {
        if (sound != null) {
            sound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
        }
    }

    /**
     * Native support extracted from paired Sound::StopAndRewindPointerSound @004385B0 and Sound::PlayPointer @00438570 calls.
     */
    private static void stopAndPlayPointerSound(Sound sound) {
        if (sound != null) {
            sound.stop();
            playPointerSound(sound);
        }
    }

    /**
     * Native support boundary for Sound::Sound @004384F0 pointer-slot helper.
     * not ported.
     */
    private static Sound loadSound(String resourcePath) {
        return new Sound(resourcePath);
    }

    /**
     * Native support boundary for DeleteSound @00438480 pointer-slot helper.
     * not ported.
     */
    private static Sound releaseSound(Sound sound) {
        if (sound != null) {
            SoundSystem.get().releaseSound(sound);
        }
        return null;
    }

    /**
     * Native support extracted from `timeGetTime`-backed timer reads in
     * StartGameSetupDialogVisualObject::Update @0043541E.
     */
    private static int currentTick() {
        return (int) System.currentTimeMillis();
    }

    /**
     * Native support extracted from StartGameSetupDialogVisualObject::RenderTipsPromptTargetHint @004364F7.
     */
    private static void ensureTipsPromptTargetHintTimersInitialized() {
        int currentTick = currentTick();
        if (!tipsPromptTargetCaptureTickInitialized) {
            tipsPromptTargetCaptureTickInitialized = true;
            lastTipsPromptTargetCaptureTick = currentTick;
        }
        if (!tipsPromptTargetAdvanceTickInitialized) {
            tipsPromptTargetAdvanceTickInitialized = true;
            lastTipsPromptTargetAdvanceTick = currentTick;
        }
    }

    /**
     * Native support extracted from StartGameSetupDialogVisualObject::RenderTipsPromptTargetHint @004364F7.
     */
    private static void captureTipsPromptTargetHit(int hitCode, int currentTick) {
        capturedTipsPromptTargetHitCode = hitCode;
        lastTipsPromptTargetCaptureTick = currentTick;
        lastTipsPromptTargetAdvanceTick = currentTick;
    }

    /**
     * Native support extracted from StartGameSetupDialogVisualObject::RenderTipsPromptTargetHint @004364F7.
     */
    private static void applyCapturedTipsPromptTargetHit() {
        switch (capturedTipsPromptTargetHitCode) {
            case HIT_DIFFICULTY_EASY, HIT_PORTRAIT_0, HIT_RETURN_TO_GAME -> tipsPromptTargetHintIndex = 1;
            case HIT_DIFFICULTY_MEDIUM, HIT_PORTRAIT_1, HIT_ACCEPT -> tipsPromptTargetHintIndex = 2;
            case HIT_DIFFICULTY_HARD, HIT_PORTRAIT_2 -> tipsPromptTargetHintIndex = 3;
            case HIT_PORTRAIT_3 -> tipsPromptTargetHintIndex = 4;
            default -> {
            }
        }
    }

    /**
     * Native support extracted from StartGameSetupDialogVisualObject::RenderTipsPromptTargetHint @004364F7.
     */
    private void drawPortraitTipsPromptTargetHint(int screenLeft, int screenTop) {
        CBmp64k pairBitmap = tipsPromptTargetHintIndex == 1
                ? portraitSelectedHoverBitmaps[0]
                : portraitHoverBitmaps[0];
        pairBitmap.drawRectMasked(
                screenLeft + PORTRAIT_LEFT_PAIR_X,
                screenTop + PORTRAIT_PAIR_Y,
                0,
                0,
                PORTRAIT_PAIR_WIDTH,
                PORTRAIT_PAIR_HEIGHT
        );
        if (tipsPromptTargetHintIndex == 2) {
            drawPortraitTargetHint(screenLeft, screenTop, 2);
        }
        if (tipsPromptTargetHintIndex == 3) {
            drawPortraitTargetHint(screenLeft, screenTop, 3);
        }
    }

    /**
     * Native support extracted from StartGameSetupDialogVisualObject::RenderTipsPromptTargetHint @004364F7.
     */
    private void drawPortraitTargetHint(int screenLeft, int screenTop, int portraitIndex) {
        CRect rect = portraitRects.get(portraitIndex);
        portraitSelectedBitmaps[portraitIndex].drawRectMasked(
                screenLeft + rect.left,
                screenTop + rect.top,
                0,
                0,
                rect.width(),
                rect.height()
        );
    }

    /**
     * Native support extracted from StartGameSetupDialogVisualObject::RenderTipsPromptTargetHint @004364F7.
     */
    private void drawDifficultyTipsPromptTargetHint(int screenLeft, int screenTop) {
        CRect rect = difficultyRects.get(tipsPromptTargetHintIndex);
        CBmp64k bitmap = difficultyStateFlags[tipsPromptTargetHintIndex] == STATE_SELECTED
                ? difficultySelectedHoverBitmaps[tipsPromptTargetHintIndex]
                : difficultyHoverBitmaps[tipsPromptTargetHintIndex];
        bitmap.drawRectMasked(
                screenLeft + rect.left,
                screenTop + rect.top,
                0,
                0,
                rect.width(),
                rect.height()
        );
    }

    /**
     * Native support extracted from StartGameSetupDialogVisualObject::RenderTipsPromptTargetHint @004364F7.
     */
    private void drawBottomButtonTipsPromptTargetHint(int screenLeft, int screenTop) {
        if (tipsPromptTargetHintIndex == 0) {
            returnToGameButtonBitmap.drawRectMasked(
                    screenLeft + returnToGameButtonRect.left,
                    screenTop + returnToGameButtonRect.top,
                    0,
                    0,
                    returnToGameButtonRect.width(),
                    returnToGameButtonRect.height()
            );
        } else if (tipsPromptTargetHintIndex == 1) {
            acceptButtonBitmap.drawRectMasked(
                    screenLeft + acceptButtonRect.left,
                    screenTop + acceptButtonRect.top,
                    0,
                    0,
                    acceptButtonRect.width(),
                    acceptButtonRect.height()
            );
        }
    }

    /**
     * Native support extracted from StartGameSetupDialogVisualObject::RenderTipsPromptTargetHint @004364F7.
     */
    private static boolean isPortraitHit(int hitCode) {
        return hitCode == HIT_PORTRAIT_0
                || hitCode == HIT_PORTRAIT_1
                || hitCode == HIT_PORTRAIT_2
                || hitCode == HIT_PORTRAIT_3;
    }

    /**
     * Native support extracted from StartGameSetupDialogVisualObject::RenderTipsPromptTargetHint @004364F7.
     */
    private static boolean isDifficultyHit(int hitCode) {
        return hitCode == HIT_DIFFICULTY_EASY
                || hitCode == HIT_DIFFICULTY_MEDIUM
                || hitCode == HIT_DIFFICULTY_HARD;
    }

    /**
     * Native support extracted from StartGameSetupDialogVisualObject::RenderTipsPromptTargetHint @004364F7.
     */
    private static boolean isBottomButtonHit(int hitCode) {
        return hitCode == HIT_RETURN_TO_GAME || hitCode == HIT_ACCEPT;
    }

    /**
     * Native support extracted from the difficulty draw loop in StartGameSetupDialogVisualObject::Update @0043541E.
     */
    private static CBmp64k selectStateBitmap(int stateFlags, CBmp64k selected, CBmp64k hovered, CBmp64k selectedHovered) {
        return switch (stateFlags) {
            case STATE_SELECTED -> selected;
            case STATE_HOVERED -> hovered;
            case STATE_SELECTED | STATE_HOVERED -> selectedHovered;
            default -> null;
        };
    }

    /**
     * Native support extracted from the blind-animation spawn-rect selection in
     * StartGameSetupDialogVisualObject::Update @0043541E.
     */
    private CRect selectRandomBlindSpawnRect() {
        return blindSpawnRects.get(nativeRandScaled(blindSpawnRects.size()));
    }

    /**
     * Native support extracted from the static blind-animation timer slots used by
     * StartGameSetupDialogVisualObject::Update @0043541E.
     */
    private static void ensureBlindAnimationTimersInitialized(int currentTick) {
        if (!nextBlindAnimationDelayInitialized) {
            nextBlindAnimationDelayInitialized = true;
            nextBlindAnimationDelayMs = computeNextBlindAnimationDelayMs();
        }
        if (!blindAnimationTickInitialized) {
            blindAnimationTickInitialized = true;
            lastBlindAnimationTick = currentTick;
        }
    }

    /**
     * Native support extracted from the static select-cursor refresh timer used by
     * StartGameSetupDialogVisualObject::Update @0043541E.
     */
    private static void ensureSelectCursorRefreshInitialized(int currentTick) {
        if (!selectCursorRefreshInitialized) {
            selectCursorRefreshInitialized = true;
            lastSelectCursorRefreshTick = currentTick;
        }
    }

    /**
     * Java helper for `CArray<>::GetSize`-style state resets in StartGameSetupDialogVisualObject own methods.
     * not ported.
     */
    private static void clearStateFlags(int[] stateFlags) {
        for (int i = 0; i < stateFlags.length; i++) {
            stateFlags[i] = 0;
        }
    }

    /**
     * Native: StartGameSetupDialogVisualObject::ClearDifficultySelectionFlags @00434DF5.
     * Fully ported.
     */
    private void clearDifficultySelectionFlags() {
        clearSelectionFlags(difficultyStateFlags);
    }

    /**
     * Native: StartGameSetupDialogVisualObject::ClearPortraitSelectionFlags @00434E3D.
     * Fully ported.
     */
    private void clearPortraitSelectionFlags() {
        clearSelectionFlags(portraitStateFlags);
    }

    /**
     * Native: StartGameSetupDialogVisualObject::ClearPortraitHoverFlags @00434ECD.
     * Fully ported.
     */
    private void clearPortraitHoverFlags() {
        clearHoverFlags(portraitStateFlags);
    }

    /**
     * Native: StartGameSetupDialogVisualObject::ClearDifficultyHoverFlags @00434E85.
     * Fully ported.
     */
    private void clearDifficultyHoverFlags() {
        clearHoverFlags(difficultyStateFlags);
    }

    /**
     * Native support extracted from StartGameSetupDialogVisualObject::ClearDifficultySelectionFlags @00434DF5
     * and ::ClearPortraitSelectionFlags @00434E3D.
     */
    private static void clearSelectionFlags(int[] stateFlags) {
        for (int i = 0; i < stateFlags.length; i++) {
            stateFlags[i] &= ~STATE_SELECTED;
        }
    }

    /**
     * Native support extracted from StartGameSetupDialogVisualObject::ClearPortraitHoverFlags @00434ECD
     * and ::ClearDifficultyHoverFlags @00434E85.
     */
    private static void clearHoverFlags(int[] stateFlags) {
        for (int i = 0; i < stateFlags.length; i++) {
            stateFlags[i] &= ~STATE_HOVERED;
        }
    }

    /**
     * Native support: StartGameSetupDialogVisualObject::IsSetupDefaultNpcName @00437D90.
     * Fully ported.
     */
    private static boolean isSetupDefaultNpcName(String value) {
        for (int index = 0x17; index <= NPC_NAME_MAX_INDEX; index++) {
            if (value.equals(get(NPCNAMES, NpcNamesText.byIndex(index)))) {
                return true;
            }
        }
        return value.equals("Unnamed");
    }

    /**
     * Native support extracted from StartGameSetupDialogVisualObject own methods, including Update @0043541E.
     */
    private boolean isCampaignStartGameSetup() {
        return Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN;
    }

    /**
     * Native: StartGameSetupDialogVisualObject::RemoveChildById @004D3B33.
     * Fully ported.
     */
    private void removeChildById(int childId) {
        for (int i = 0; i < children.size(); i++) {
            CVisualObject child = children.get(i);
            if (child.id == childId) {
                child.m_pParent = null;
                if (mouseInputTarget == child) {
                    mouseInputTarget = null;
                }
                children.remove(i);
                return;
            }
        }
    }

    /**
     * Native support extracted from `new CBmp64k(path)` load sites in StartGameSetupDialogVisualObject::LoadStartGameSetupGraphics @00433A3D.
     */
    private static CBmp64k loadBmp64k(String resourcePath) {
        return new CBmp64k(resourcePath);
    }

    /**
     * Native support extracted from `new CBmp256(path)` load sites in StartGameSetupDialogVisualObject::LoadStartGameSetupGraphics @00433A3D.
     */
    private static CBmp256 loadBmp256(String resourcePath) {
        return new CBmp256(resourcePath);
    }

    /**
     * Native support extracted from `new CA16(path)` load sites in StartGameSetupDialogVisualObject::LoadStartGameSetupGraphics @00433A3D.
     */
    private static CA16 loadSprite(String resourcePath) {
        return new CA16(resourcePath);
    }

    /**
     * Native support extracted from the repeated portrait/difficulty bitmap-array loads in StartGameSetupDialogVisualObject::LoadStartGameSetupGraphics @00433A3D.
     */
    private static void loadBitmapArray(CBmp64k[] target, String[] resourcePaths) {
        for (int i = 0; i < target.length; i++) {
            loadBitmapArrayElement(target, resourcePaths, i);
        }
    }

    /**
     * Native support extracted from the non-linear portrait bitmap loads in StartGameSetupDialogVisualObject::LoadStartGameSetupGraphics @00433A3D.
     */
    private static void loadBitmapArray(CBmp64k[] target, String[] resourcePaths, int[] nativeOrder) {
        for (int index : nativeOrder) {
            loadBitmapArrayElement(target, resourcePaths, index);
        }
    }

    /**
     * Native support extracted from one CBmp64k pointer assignment in StartGameSetupDialogVisualObject::LoadStartGameSetupGraphics @00433A3D.
     */
    private static void loadBitmapArrayElement(CBmp64k[] target, String[] resourcePaths, int index) {
        target[index] = loadBmp64k(resourcePaths[index]);
    }

    /**
     * Native support extracted from the repeated torch-frame loads in StartGameSetupDialogVisualObject::LoadStartGameSetupGraphics @00433A3D.
     */
    private static void loadTorchFrames(List<CBmp64k> target, String resourcePathFormat) {
        target.clear();
        for (int frameIndex = 0; frameIndex < TORCH_FRAME_COUNT; frameIndex++) {
            target.add(loadBmp64k(String.format(resourcePathFormat, frameIndex)));
        }
    }

    /**
     * Native support extracted from the repeated portrait/difficulty bitmap-array releases in StartGameSetupDialogVisualObject::ReleaseStartGameSetupGraphics @00434749.
     */
    private static void releasePortraitBitmapArrays(CBmp64k[] hover, CBmp64k[] selected, CBmp64k[] selectedHover) {
        for (int i = 0; i < hover.length; i++) {
            hover[i] = releaseBmp(hover[i]);
            selected[i] = releaseBmp(selected[i]);
            selectedHover[i] = releaseBmp(selectedHover[i]);
        }
    }

    /**
     * Native support extracted from the repeated difficulty bitmap-array releases in StartGameSetupDialogVisualObject::ReleaseStartGameSetupGraphics @00434749.
     */
    private static void releaseDifficultyBitmapArrays(CBmp64k[] selected, CBmp64k[] hover, CBmp64k[] selectedHover) {
        for (int i = 0; i < selected.length; i++) {
            selected[i] = releaseBmp(selected[i]);
            hover[i] = releaseBmp(hover[i]);
            selectedHover[i] = releaseBmp(selectedHover[i]);
        }
    }

    /**
     * Native support extracted from the repeated torch-frame releases in StartGameSetupDialogVisualObject::ReleaseStartGameSetupGraphics @00434749.
     */
    private static void releaseTorchFramePairs(List<CBmp64k> leftFrames, List<CBmp64k> rightFrames) {
        for (int i = 0; i < leftFrames.size(); i++) {
            leftFrames.set(i, releaseBmp(leftFrames.get(i)));
            rightFrames.set(i, releaseBmp(rightFrames.get(i)));
        }
        leftFrames.clear();
        rightFrames.clear();
    }

    /**
     * Native support extracted from the torch-frame lookup in StartGameSetupDialogVisualObject::Update @0043541E.
     */
    private static CBmp64k getTorchFrame(List<CBmp64k> frames, int frameIndex) {
        return frames.get(frameIndex);
    }

    /**
     * Native support extracted from the `rand() / 0x41 + 500` blind-animation delay in
     * StartGameSetupDialogVisualObject::Update @0043541E.
     */
    private static int computeNextBlindAnimationDelayMs() {
        return BLIND_ANIMATION_MIN_DELAY_MS + Utils.randInclusive(NATIVE_RAND_MAX) / BLIND_ANIMATION_DELAY_DIVISOR;
    }

    /**
     * Native support extracted from the `rand() / (0x7fff / bound)` blind-animation position scaling in
     * StartGameSetupDialogVisualObject::Update @0043541E.
     */
    private static int nativeRandScaled(int bound) {
        return Utils.randInclusive(NATIVE_RAND_MAX) / (NATIVE_RAND_MAX / bound);
    }

    /**
     * Native lifecycle support extracted from bitmap destruction in StartGameSetupDialogVisualObject::ReleaseStartGameSetupGraphics @00434749.
     */
    private static CBmp64k releaseBmp(CBmp64k bitmap) {
        return null;
    }

    /**
     * Native lifecycle support extracted from palette-bitmap destruction in StartGameSetupDialogVisualObject::ReleaseStartGameSetupGraphics @00434749.
     */
    private static CBmp256 releaseBmp256(CBmp256 bitmap) {
        return null;
    }

    /**
     * Native lifecycle support extracted from sprite destruction in StartGameSetupDialogVisualObject::ReleaseStartGameSetupGraphics @00434749.
     */
    private static CA16 releaseSprite(CA16 sprite) {
        return null;
    }
}
