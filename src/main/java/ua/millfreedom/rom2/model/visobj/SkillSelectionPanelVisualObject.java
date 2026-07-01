package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.model.CBmp256;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.GameBitmapFrame;
import ua.millfreedom.rom2.model.sound.Sound;
import ua.millfreedom.rom2.model.sound.SoundSystem;
import ua.millfreedom.rom2.text.MainText;

import java.awt.Dimension;
import java.awt.Point;

import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.MainText.BLADE_TOOLTIP_171;
import static ua.millfreedom.rom2.text.MainText.FIRE_SPHERE_TOOLTIP_176;
import static ua.millfreedom.rom2.text.TextTableId.MAIN;

/**
 * Native class: SkillSelectionPanelVisualObject (vtbl @0x005CB630).
 * Purpose: character-generator skill/sphere selection panel used as child `id=0x45A` under dialog `0x456`.
 */
public class SkillSelectionPanelVisualObject extends CVisualObject {
    public static final int NATIVE_SIZE = 0x138; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int OPTION_COUNT = 4;
    private static final int NATIVE_SLOT_COUNT = 5;
    private static final int PANEL_LEFT_WITHIN_DIALOG = 0xA0;
    private static final long IDLE_HIGHLIGHT_START_DELAY_MS = 500;
    private static final long IDLE_HIGHLIGHT_STEP_MS = 300;
    private static final String CHRGEN_DIR = "graphics/interface/chrgen";
    private static final String FIGHTER_SKILL_DIR = CHRGEN_DIR + "/fighter";
    private static final String MAGE_SKILL_DIR = CHRGEN_DIR + "/mag";
    private static final String ROLL_STATS_BITMAP = CHRGEN_DIR + "/rollstatsr.bmp";
    private static final String FULL_STATS_BITMAP = CHRGEN_DIR + "/fullstatsr.bmp";
    private static final String ROLLOVER_BITMAP = "graphics/interface/inn/ruover.bmp";
    private static final String FIGHTER_MASK_BITMAP = FIGHTER_SKILL_DIR + "/mask.bmp";
    private static final String FIGHTER_BACKGROUND_BITMAP = FIGHTER_SKILL_DIR + "/column.bmp";
    private static final String MAGE_MASK_BITMAP = MAGE_SKILL_DIR + "/mask.bmp";
    private static final String MAGE_BACKGROUND_BITMAP = MAGE_SKILL_DIR + "/column.bmp";
    private static final String[] FIGHTER_SELECTION_SOUND_PATHS = {
            "SFX/ChrGen/Skill/FSword.wav",
            "SFX/ChrGen/Skill/FAxe.wav",
            "SFX/ChrGen/Skill/FClub.wav",
            "SFX/ChrGen/Skill/FPike.wav",
            "SFX/ChrGen/Skill/FBow.wav"
    };
    private static final String[] MAGE_SELECTION_SOUND_PATHS = {
            "SFX/ChrGen/Skill/MFire.wav",
            "SFX/ChrGen/Skill/MWater.wav",
            "SFX/ChrGen/Skill/MAir.wav",
            "SFX/ChrGen/Skill/MEarth.wav",
            "SFX/ChrGen/Skill/MAstral.wav"
    };
    private static final String[][] FIGHTER_OPTION_GRAPHIC_PATHS = {
            {
                    FIGHTER_SKILL_DIR + "/sword/on.bmp",
                    FIGHTER_SKILL_DIR + "/sword/shine_off.bmp",
                    FIGHTER_SKILL_DIR + "/sword/shine_on.bmp"
            },
            {
                    FIGHTER_SKILL_DIR + "/axe/on.bmp",
                    FIGHTER_SKILL_DIR + "/axe/shine_off.bmp",
                    FIGHTER_SKILL_DIR + "/axe/shine_on.bmp"
            },
            {
                    FIGHTER_SKILL_DIR + "/mace/on.bmp",
                    FIGHTER_SKILL_DIR + "/mace/shine_off.bmp",
                    FIGHTER_SKILL_DIR + "/mace/shine_on.bmp"
            },
            {
                    FIGHTER_SKILL_DIR + "/pike/on.bmp",
                    FIGHTER_SKILL_DIR + "/pike/shine_off.bmp",
                    FIGHTER_SKILL_DIR + "/pike/shine_on.bmp"
            },
            {
                    FIGHTER_SKILL_DIR + "/bow/on.bmp",
                    FIGHTER_SKILL_DIR + "/bow/shine_off.bmp",
                    FIGHTER_SKILL_DIR + "/bow/shine_on.bmp"
            }
    };
    private static final String[][] MAGE_OPTION_GRAPHIC_PATHS = {
            {
                    MAGE_SKILL_DIR + "/fire/on.bmp",
                    MAGE_SKILL_DIR + "/fire/shine_off.bmp",
                    MAGE_SKILL_DIR + "/fire/shine_on.bmp"
            },
            {
                    MAGE_SKILL_DIR + "/water/on.bmp",
                    MAGE_SKILL_DIR + "/water/shine_off.bmp",
                    MAGE_SKILL_DIR + "/water/shine_on.bmp"
            },
            {
                    MAGE_SKILL_DIR + "/air/on.bmp",
                    MAGE_SKILL_DIR + "/air/shine_off.bmp",
                    MAGE_SKILL_DIR + "/air/shine_on.bmp"
            },
            {
                    MAGE_SKILL_DIR + "/earth/on.bmp",
                    MAGE_SKILL_DIR + "/earth/shine_off.bmp",
                    MAGE_SKILL_DIR + "/earth/shine_on.bmp"
            },
            {
                    MAGE_SKILL_DIR + "/astral/on.bmp",
                    MAGE_SKILL_DIR + "/astral/shine_off.bmp",
                    MAGE_SKILL_DIR + "/astral/shine_on.bmp"
            }
    };
    private static final int[][] FIGHTER_OPTION_LAYOUTS = {
            {0xf8, 0x5d, 0x8c, 0x2f},
            {0xfc, 0x7e, 0x84, 0x39},
            {0xf8, 0xb6, 0x8c, 0x2e},
            {0xf4, 0xe1, 0x94, 0x1c},
            {0xf8, 0xfa, 0x8c, 0x28}
    };
    private static final int[][] MAGE_OPTION_LAYOUTS = {
            {0x168, 0x96, 0x2c, 0x34},
            {0xe8, 0xa5, 0x30, 0x24},
            {0x124, 0x62, 0x30, 0x26},
            {0x12c, 0xe4, 0x30, 0x26},
            {0x128, 0x9e, 0x30, 0x2d}
    };
    private static final int[] FIGHTER_OPTION_MARKER_IDS = {
            0xff,
            0xbf,
            0x98,
            0x7f,
            0x66
    };
    private static final int[] MAGE_OPTION_MARKER_IDS = {
            0x7f,
            0x66,
            0xff,
            0x98,
            0xbf
    };
    private static int lastHoveredOptionIndex = -1;
    private static int rotatingHighlightOptionIndex;
    private static long lastHoverChangeTimeMs;
    private static long lastIdleHighlightStepTimeMs;

    //0x5c
    public CharacterGeneratorDialogVisualObject ownerDialog;
    //0x60
    public CBmp64k backgroundGraphic;
    //0x64
    public CBmp256 optionHitMaskBitmap;
    //0x68
    public CBmp64k rollStatsGraphic;
    //0x6c
    public CBmp64k fullStatsGraphic;
    //0x70
    public CBmp64k rolloverGraphic;
    //0x74
    public CBmp64k sharedHumanBackdropGraphic;
    //0x78
    public final CBmp64k[] selectedOptionGraphics = new CBmp64k[NATIVE_SLOT_COUNT];
    //0x8c
    public final CBmp64k[] hoveredOptionGraphics = new CBmp64k[NATIVE_SLOT_COUNT];
    //0xa0
    public final CBmp64k[] rotatingHighlightOptionGraphics = new CBmp64k[NATIVE_SLOT_COUNT];
    //0xb4
    public final Point[] optionDrawPoints = new Point[NATIVE_SLOT_COUNT];
    //0xdc
    public final Dimension[] optionDrawSizes = new Dimension[NATIVE_SLOT_COUNT];
    //0x104
    public byte optionHitMarkerId0;
    //0x105
    public byte optionHitMarkerId1;
    //0x106
    public byte optionHitMarkerId2;
    //0x107
    public byte optionHitMarkerId3;
    //0x108
    public byte optionHitMarkerId4;
    //0x10c
    public final int[] optionStateFlags = new int[NATIVE_SLOT_COUNT];
    //0x120
    public final Sound[] selectionSounds = new Sound[NATIVE_SLOT_COUNT];
    //0x134
    public int selectedOptionIndex;

    /**
     * Native: SkillSelectionPanelVisualObject::SkillSelectionPanelVisualObject @0042B052.
     * Fully ported.
     */
    public SkillSelectionPanelVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            CharacterGeneratorDialogVisualObject ownerDialog
    ) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        this.ownerDialog = ownerDialog;
        initializeSkillSelectionPanel();
    }

    /**
     * vtbl +0x14: SkillSelectionPanelVisualObject::GetText @0042D292.
     * Fully ported.
     */
    @Override
    public String getText() {
        if (ownerDialog.dialogActiveFlag == 0) {
            return null;
        }

        int optionIndex = getOptionIndexAtScreenPoint(
                Globals.mousePointer.getX(),
                Globals.mousePointer.getY()
        );
        if (optionIndex < 0) {
            return null;
        }

        MainText tooltipBase = ownerDialog.magePortraitFlag == 0
                ? BLADE_TOOLTIP_171
                : FIRE_SPHERE_TOOLTIP_176;
        return get(MAIN, MainText.byIndex(tooltipBase.index() + optionIndex));
    }

    /**
     * vtbl +0x2C: SkillSelectionPanelVisualObject::Update @0042B220.
     * Fully ported.
     */
    @Override
    public void update() {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        optionStateFlags[NATIVE_SLOT_COUNT - 1] |= 0x1;
        if (ownerDialog.dialogActiveFlag != 0) {
            Globals.renderer.lockSurface();
            try {
                drawBackgroundGraphic(backgroundGraphic, screenRect.left, screenRect.top);
                for (int i = 0; i < OPTION_COUNT; i++) {
                    CBmp64k optionGraphic = getStateGraphicByIndex(i);
                    if (optionGraphic != null) {
                        drawOptionGraphic(optionGraphic, optionDrawPoints[i], optionDrawSizes[i]);
                    }
                }
                drawRotatingIdleHighlight();
            } finally {
                Globals.renderer.unlockSurface();
            }
        }
        super.update();
    }

    /**
     * vtbl +0x4C: SkillSelectionPanelVisualObject::OnMouseMove @0042B691.
     * Fully ported.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        for (int i = 0; i < NATIVE_SLOT_COUNT; i++) {
            optionStateFlags[i] &= ~0x2;
        }

        int optionIndex = getOptionIndexAtScreenPoint(x, y);
        if (optionIndex >= 0) {
            optionStateFlags[optionIndex] |= 0x2;
        }
        return super.onMouseMove(nFlags, x, y);
    }

    /**
     * vtbl +0x54: SkillSelectionPanelVisualObject::OnLButtonDown @0042B736.
     * Fully ported.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        int optionIndex = getOptionIndexAtScreenPoint(x, y);
        if (optionIndex < 0 || optionIndex >= OPTION_COUNT) {
            return super.onLButtonDown(nFlags, x, y);
        }

        for (int i = 0; i < OPTION_COUNT; i++) {
            optionStateFlags[i] &= ~0x1;
        }
        optionStateFlags[optionIndex] |= 0x1;
        selectedOptionIndex = optionIndex;
        Sound.playPointer(selectionSounds, optionIndex);
        ownerDialog.onStatsAllocationChanged(
                ownerDialog.statsAllocationPanel.bodyValue,
                ownerDialog.statsAllocationPanel.agilityValue,
                ownerDialog.statsAllocationPanel.mindValue,
                ownerDialog.statsAllocationPanel.spiritValue,
                optionIndex + 1
        );
        ownerDialog.refreshSkillSelectionTips();
        return super.onLButtonDown(nFlags, x, y);
    }

    /**
     * vtbl +0x58: SkillSelectionPanelVisualObject::OnLButtonUp @0042B86F.
     * Fully ported.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        return super.onLButtonUp(nFlags, x, y);
    }

    /**
     * Native: SkillSelectionPanelVisualObject::InitializeSkillSelectionPanel @0042B147.
     * Fully ported.
     */
    private void initializeSkillSelectionPanel() {
        backgroundGraphic = null;
        optionHitMaskBitmap = null;
        rollStatsGraphic = null;
        fullStatsGraphic = null;
        rolloverGraphic = null;
        sharedHumanBackdropGraphic = null;
        optionHitMarkerId0 = 0;
        optionHitMarkerId1 = 0;
        optionHitMarkerId2 = 0;
        optionHitMarkerId3 = 0;
        optionHitMarkerId4 = 0;

        for (int i = 0; i < NATIVE_SLOT_COUNT; i++) {
            selectedOptionGraphics[i] = null;
            hoveredOptionGraphics[i] = null;
            rotatingHighlightOptionGraphics[i] = null;
            optionDrawPoints[i] = new Point();
            optionDrawSizes[i] = new Dimension();
            optionStateFlags[i] = 0;
            selectionSounds[i] = null;
        }

        selectedOptionIndex = 0;
        optionStateFlags[selectedOptionIndex] = 0x1;
    }

    /**
     * Native: SkillSelectionPanelVisualObject::LoadSkillSelectionGraphics @0042B890.
     * Fully ported.
     */
    void loadSkillSelectionGraphics(boolean mageAlignmentSelected) {
        releaseSkillSelectionGraphics();

        rollStatsGraphic = loadBmp64k(ROLL_STATS_BITMAP);
        fullStatsGraphic = loadBmp64k(FULL_STATS_BITMAP);
        rolloverGraphic = loadBmp64k(ROLLOVER_BITMAP);
        sharedHumanBackdropGraphic = GUI.humanBackL;

        String maskPath = mageAlignmentSelected ? MAGE_MASK_BITMAP : FIGHTER_MASK_BITMAP;
        String backgroundPath = mageAlignmentSelected ? MAGE_BACKGROUND_BITMAP : FIGHTER_BACKGROUND_BITMAP;
        String[][] optionGraphicPaths = mageAlignmentSelected ? MAGE_OPTION_GRAPHIC_PATHS : FIGHTER_OPTION_GRAPHIC_PATHS;
        int[][] optionLayouts = mageAlignmentSelected ? MAGE_OPTION_LAYOUTS : FIGHTER_OPTION_LAYOUTS;
        int[] optionMarkerIds = mageAlignmentSelected ? MAGE_OPTION_MARKER_IDS : FIGHTER_OPTION_MARKER_IDS;

        optionHitMaskBitmap = loadBmp256(maskPath);
        backgroundGraphic = loadBmp64k(backgroundPath);
        loadOptionGraphics(optionGraphicPaths);
        applyOptionLayout(optionLayouts);
        applyOptionHitMarkers(optionMarkerIds);
    }

    /**
     * Native: SkillSelectionPanelVisualObject::ReleaseSkillSelectionGraphics @0042CDB1.
     * Fully ported. Java clears retained bitmap references instead of emulating native delete semantics.
     */
    void releaseSkillSelectionGraphics() {
        backgroundGraphic = null;
        optionHitMaskBitmap = null;
        rollStatsGraphic = null;
        fullStatsGraphic = null;
        rolloverGraphic = null;
        sharedHumanBackdropGraphic = null;
        for (int i = 0; i < NATIVE_SLOT_COUNT; i++) {
            selectedOptionGraphics[i] = null;
            hoveredOptionGraphics[i] = null;
            rotatingHighlightOptionGraphics[i] = null;
        }
    }

    /**
     * Native: SkillSelectionPanelVisualObject::LoadSkillSelectionSounds @0042D027.
     * Fully ported.
     */
    void loadSkillSelectionSounds(boolean mageAlignmentSelected) {
        String[] paths = mageAlignmentSelected ? MAGE_SELECTION_SOUND_PATHS : FIGHTER_SELECTION_SOUND_PATHS;
        for (int i = 0; i < paths.length && i < selectionSounds.length; i++) {
            selectionSounds[i] = loadSound(paths[i]);
        }
    }

    /**
     * Native: SkillSelectionPanelVisualObject::ReleaseSkillSelectionSounds @0042D11E.
     * Fully ported.
     */
    void releaseSkillSelectionSounds() {
        for (int i = 0; i < selectionSounds.length; i++) {
            selectionSounds[i] = releaseSound(selectionSounds[i]);
        }
    }

    /**
     * Native support extracted from SkillSelectionPanelVisualObject::LoadSkillSelectionGraphics @0042B890.
     */
    private void loadOptionGraphics(String[][] optionGraphicPaths) {
        for (int i = 0; i < NATIVE_SLOT_COUNT; i++) {
            selectedOptionGraphics[i] = loadBmp64k(optionGraphicPaths[i][0]);
            hoveredOptionGraphics[i] = loadBmp64k(optionGraphicPaths[i][1]);
            rotatingHighlightOptionGraphics[i] = loadBmp64k(optionGraphicPaths[i][2]);
        }
    }

    /**
     * Native support extracted from SkillSelectionPanelVisualObject::LoadSkillSelectionGraphics @0042B890.
     */
    private void applyOptionLayout(int[][] optionLayouts) {
        int ownerLeft = ownerDialog.cRect.left;
        int ownerTop = ownerDialog.cRect.top;
        for (int i = 0; i < NATIVE_SLOT_COUNT; i++) {
            optionDrawPoints[i].x = ownerLeft + optionLayouts[i][0];
            optionDrawPoints[i].y = ownerTop + optionLayouts[i][1];
            optionDrawSizes[i].width = optionLayouts[i][2];
            optionDrawSizes[i].height = optionLayouts[i][3];
        }
    }

    /**
     * Native support extracted from SkillSelectionPanelVisualObject::LoadSkillSelectionGraphics @0042B890.
     */
    private void applyOptionHitMarkers(int[] optionMarkerIds) {
        optionHitMarkerId0 = (byte) optionMarkerIds[0];
        optionHitMarkerId1 = (byte) optionMarkerIds[1];
        optionHitMarkerId2 = (byte) optionMarkerIds[2];
        optionHitMarkerId3 = (byte) optionMarkerIds[3];
        optionHitMarkerId4 = (byte) optionMarkerIds[4];
    }

    /**
     * Native support extracted from SkillSelectionPanelVisualObject::LoadSkillSelectionGraphics @0042B890.
     */
    private static CBmp64k loadBmp64k(String path) {
        CBmp64k bitmap = new CBmp64k(path);
        Globals.mousePointer.update();
        return bitmap;
    }

    /**
     * Native support extracted from SkillSelectionPanelVisualObject::LoadSkillSelectionGraphics @0042B890.
     */
    private static CBmp256 loadBmp256(String path) {
        CBmp256 bitmap = new CBmp256(path);
        Globals.mousePointer.update();
        return bitmap;
    }

    /**
     * Native: SkillSelectionPanelVisualObject::GetOptionIndexAtScreenPoint @0042D1A3.
     * Fully ported.
     */
    private int getOptionIndexAtScreenPoint(int x, int y) {
        if (ownerDialog.dialogActiveFlag == 0 || !ownerDialog.cRect.contains(x, y)) {
            return -1;
        }

        byte markerId = getHitMaskByte(
                x - ownerDialog.cRect.left - PANEL_LEFT_WITHIN_DIALOG,
                y - ownerDialog.cRect.top
        );

        for (int i = 0; i < OPTION_COUNT; i++) {
            if (markerId == getOptionHitMarkerIdByIndex(i)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Java helper extracted from SkillSelectionPanelVisualObject::GetOptionIndexAtScreenPoint @0042D1A3.
     * Fully ported.
     */
    private byte getHitMaskByte(int localX, int localY) {
        GameBitmapFrame frame = optionHitMaskBitmap.frames.getFirst();
        int offset = localY * frame.xSize() + localX;
        return frame.data()[offset];
    }

    /**
     * Java helper extracted from SkillSelectionPanelVisualObject::LoadSkillSelectionGraphics @0042B890
     * and SkillSelectionPanelVisualObject::GetOptionIndexAtScreenPoint @0042D1A3.
     */
    private byte getOptionHitMarkerIdByIndex(int optionIndex) {
        return switch (optionIndex) {
            case 0 -> optionHitMarkerId0;
            case 1 -> optionHitMarkerId1;
            case 2 -> optionHitMarkerId2;
            case 3 -> optionHitMarkerId3;
            case 4 -> optionHitMarkerId4;
            default -> throw new IndexOutOfBoundsException("optionIndex: " + optionIndex);
        };
    }

    /**
     * Java helper extracted from SkillSelectionPanelVisualObject::Update @0042B220
     * and SkillSelectionPanelVisualObject::DrawRotatingIdleHighlight @0042B406.
     */
    private CBmp64k getStateGraphicByIndex(int optionIndex) {
        int stateFlags = optionStateFlags[optionIndex];
        return switch (stateFlags) {
            case 1 -> selectedOptionGraphics[optionIndex];
            case 2 -> hoveredOptionGraphics[optionIndex];
            case 3 -> rotatingHighlightOptionGraphics[optionIndex];
            default -> null;
        };
    }

    /**
     * Java helper extracted from SkillSelectionPanelVisualObject::Update @0042B220.
     */
    private static void drawBackgroundGraphic(CBmp64k backgroundGraphic, int x, int y) {
        backgroundGraphic.draw(x, y, 0, null, false);
    }

    /**
     * Java helper extracted from SkillSelectionPanelVisualObject::Update @0042B220.
     */
    private static void drawOptionGraphic(
            CBmp64k optionGraphic,
            Point drawPoint,
            Dimension drawSize
    ) {
        optionGraphic.drawRectMasked(drawPoint.x, drawPoint.y, 0, 0, drawSize.width, drawSize.height);
    }

    /**
     * Native: SkillSelectionPanelVisualObject::DrawRotatingIdleHighlight @0042B406.
     * Fully ported.
     */
    private void drawRotatingIdleHighlight() {
        long now = System.currentTimeMillis();
        if (lastHoverChangeTimeMs == 0) {
            lastHoverChangeTimeMs = now;
        }
        if (lastIdleHighlightStepTimeMs == 0) {
            lastIdleHighlightStepTimeMs = now;
        }
        int hoveredOptionIndex = getOptionIndexAtScreenPoint(
                Globals.mousePointer.getX(),
                Globals.mousePointer.getY()
        );
        if (ownerDialog.tipsRefreshStep != 0
                || ownerDialog.tipsPrompt == null
                || now - lastHoverChangeTimeMs <= IDLE_HIGHLIGHT_START_DELAY_MS) {
            return;
        }

        if (lastHoveredOptionIndex != -1) {
            rotatingHighlightOptionIndex = (lastHoveredOptionIndex + 1) % OPTION_COUNT;
        }
        lastHoveredOptionIndex = hoveredOptionIndex;
        lastHoverChangeTimeMs = now;
        if (hoveredOptionIndex == -1) {
            rotatingHighlightOptionIndex = Math.floorMod(rotatingHighlightOptionIndex, OPTION_COUNT);
            CBmp64k graphic = optionStateFlags[rotatingHighlightOptionIndex] == 1
                    ? rotatingHighlightOptionGraphics[rotatingHighlightOptionIndex]
                    : hoveredOptionGraphics[rotatingHighlightOptionIndex];
            drawOptionGraphic(
                    graphic,
                    optionDrawPoints[rotatingHighlightOptionIndex],
                    optionDrawSizes[rotatingHighlightOptionIndex]
            );
            lastHoveredOptionIndex = -1;
            if (now - lastIdleHighlightStepTimeMs > IDLE_HIGHLIGHT_STEP_MS) {
                rotatingHighlightOptionIndex = (rotatingHighlightOptionIndex + 1) % OPTION_COUNT;
                lastIdleHighlightStepTimeMs = now;
            }
        }
    }

    /**
     * Java helper around FUN_004384F0 @004384F0 extracted for SkillSelectionPanelVisualObject::LoadSkillSelectionSounds @0042D027.
     */
    private static Sound loadSound(String resourcePath) {
        return new Sound(resourcePath);
    }

    /**
     * Java helper around FUN_00438480 @00438480 extracted for SkillSelectionPanelVisualObject::ReleaseSkillSelectionSounds @0042D11E.
     */
    private static Sound releaseSound(Sound sound) {
        if (sound != null) {
            SoundSystem.get().releaseSound(sound);
        }
        return null;
    }
}
