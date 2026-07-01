package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CBmp64kFrameSequence;
import ua.millfreedom.rom2.model.sound.Sound;
import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native class: KaargInnSceneGridVisualObject (vtbl @0x005CE828).
 * Purpose: kaarg inn animated scene grid for the menu `0x44C` dialog.
 */
public class KaargInnSceneGridVisualObject extends BasicInnSceneGridVisualObject {
    public static final int NATIVE_SIZE = 0x4D0; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!
    private static final String PRIMARY_SELECTION_BMP = "graphics/interface/inn/manback.bmp";
    private static final String SECONDARY_SELECTION_BMP = "graphics/interface/inn/manbacktalk.bmp";
    private static final String CENTER_AREA_BMP = "graphics/interface/inn_kaarg/tavernmain.bmp";
    private static final String LEFT_UPPER_OVERLAY_BMP = "graphics/interface/inn/luover.bmp";
    private static final String LEFT_LOWER_OVERLAY_BMP = "graphics/interface/inn/ldover.bmp";
    private static final String RIGHT_UPPER_OVERLAY_BMP = "graphics/interface/inn/ruover.bmp";
    private static final String QUEST_BITMAP_TEMPLATE = "graphics/interface/inn/quests/%02d.bmp";
    private static final int KAARG_TAVERN_FRAMES_E_SCRIPT_END = -1;
    private static final int[] KAARG_TAVERN_FRAMES_E_SCRIPT = {
            0, 1, 2, 3, 4, 4, 4, 4, 4, 4, 4, 4,
            4, 5, 6, 7, 8, 8, 8, 8, 8, 8, 8, 8,
            8, 8, 8, 8, 5, 6, 7, 8, 9, 10, 11, 12,
            13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 14,
            15, 16, 17, 18, 19, 20, 20, 20, 20, 20, 20, 20,
            20, 20, 20, 20, 21, 22, 23, 24, 24, 24, 24, 24,
            24, 24, 24, 24, 24, 5, 6, 7, 24, 24, 24, 24,
            24, 24, 24, 24, 24, 24, 24, 25, 26, 27, 28,
            KAARG_TAVERN_FRAMES_E_SCRIPT_END
    };
    private static boolean kaargSceneTimersInitialized;
    private static long kaargSceneAdvanceTick;
    private static long kaargSceneInitTick;
    private static long kaargAuxSceneTick;
    private static int kaargSceneRandomSeed;
    private static int kaargTavernSceneMode = -1;

    //0x3cc
    public final CBmp64kFrameSequence kaargTavernFramesA = new CBmp64kFrameSequence();
    //0x3fc
    public final CBmp64kFrameSequence kaargTavernFramesB = new CBmp64kFrameSequence();
    //0x42c
    public final CBmp64kFrameSequence kaargTavernFramesC = new CBmp64kFrameSequence();
    //0x45c
    public final CBmp64kFrameSequence kaargTavernFramesD = new CBmp64kFrameSequence();
    //0x48c
    public final CBmp64kFrameSequence kaargTavernFramesE = new CBmp64kFrameSequence();
    //0x4bc
    public int kaargTavernFramesEScriptIndex;
    //0x4c0
    public int lastAmbientDishSoundTick;
    //0x4c4
    public int nextAmbientDishSoundDelay;
    //0x4c8
    public int lastAmbientVoiceSoundTick;
    //0x4cc
    public int nextAmbientVoiceSoundDelay;

    /**
     * Native: KaargInnSceneGridVisualObject::KaargInnSceneGridVisualObject @0049F353.
     * Fully ported for the Java-instantiated inn scene-grid path.
     */
    public KaargInnSceneGridVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            BasicInnDialogVisualObject ownerDialog
    ) {
        super(id, xLeft, yTop, xRight, yBottom, ownerDialog);
        initializeKaargAmbientTimers();
    }

    /**
     * vtbl +0x2C: KaargInnSceneGridVisualObject::Update @0049F467.
     * Fully ported.
     */
    @Override
    public void update() {
        long now = System.currentTimeMillis();
        int currentTick = (int) now;
        initializeKaargSceneTimers(now);

        int ownerLeft = ownerDialog.cRect.left;
        int ownerTop = ownerDialog.cRect.top;
        KaargInnDialogVisualObject kaargOwner = (KaargInnDialogVisualObject) ownerDialog;

        Globals.renderer.lockSurface();
        try {
            centerAreaBitmap.draw(ownerLeft + cRect.left, ownerTop + cRect.top, 0, 0, false);
            if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN) {
                renderKaargNetworkInnScene(kaargOwner, ownerLeft, ownerTop, now);
            } else {
                renderQuestRewardGrid(ownerLeft, ownerTop);
            }

            leftUpperOverlayBitmap.drawRectMasked(ownerLeft + 0xA0, ownerTop, 0, 0, 0x10, 0xEE);
            leftLowerOverlayBitmap.drawRectMasked(ownerLeft + 0xA0, ownerTop + 0xEE, 0, 0, 0x10, 0xF2);
            rightUpperOverlayBitmap.drawRectMasked(ownerLeft + 0x1D0, ownerTop, 0, 0, 0x10, 0xEE);
            (ownerDialog.useHumanBottomStrip() ? GUI.humanBackL : GUI.textBackL)
                    .drawRectMasked(ownerLeft + 0x1D0, ownerTop + 0xEE, 0, 0, 0x10, 0xF2);
        } finally {
            Globals.renderer.unlockSurface();
        }

        if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN
                && nextAmbientVoiceSoundDelay < currentTick - lastAmbientVoiceSoundTick) {
            Sound.playPointer(kaargOwner.kaargSceneSounds, switch (Utils.randExclusive(3)) {
                case 0 -> 7;
                case 1 -> 8;
                case 2 -> 9;
                default -> -1;
            });
            nextAmbientVoiceSoundDelay = Utils.randBased(2000, 2000);
            lastAmbientVoiceSoundTick = currentTick;
        }

        if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN
                && nextAmbientDishSoundDelay < currentTick - lastAmbientDishSoundTick) {
            Sound.playPointer(kaargOwner.kaargSceneSounds, switch (Utils.randExclusive(4)) {
                case 0 -> 0;
                case 1 -> 1;
                case 2 -> 2;
                case 3 -> 3;
                default -> -1;
            });
            nextAmbientDishSoundDelay = Utils.randBased(2000, 2000);
            lastAmbientDishSoundTick = currentTick;
        }

        updateChildVisualObjects();
    }

    /**
     * vtbl +0x80: KaargInnSceneGridVisualObject::LoadPanelBitmaps @004A0096.
     * Fully ported. Java clears retained bitmap references instead of reproducing native delete/free calls.
     */
    @Override
    public void loadPanelBitmaps() {
        releasePanelBitmaps();

        availableEntryBackdropBitmap = new CBmp64k(PRIMARY_SELECTION_BMP);
        Globals.renderer.refreshMousePointer();
        reservedEntryBackdropBitmap = new CBmp64k(SECONDARY_SELECTION_BMP);
        Globals.renderer.refreshMousePointer();
        centerAreaBitmap = new CBmp64k(CENTER_AREA_BMP);
        Globals.renderer.refreshMousePointer();
        leftUpperOverlayBitmap = new CBmp64k(LEFT_UPPER_OVERLAY_BMP);
        Globals.renderer.refreshMousePointer();
        leftLowerOverlayBitmap = new CBmp64k(LEFT_LOWER_OVERLAY_BMP);
        Globals.renderer.refreshMousePointer();
        rightUpperOverlayBitmap = new CBmp64k(RIGHT_UPPER_OVERLAY_BMP);
        Globals.renderer.refreshMousePointer();

        if (Globals.mainWindow.sessionMode != CMainWindow.SESSION_MODE_CAMPAIGN) {
            for (int index = 0; index < questIconBitmaps.length; index++) {
                questIconBitmaps[index] = new CBmp64k(String.format(QUEST_BITMAP_TEMPLATE, index + 1));
            }
        }
    }

    /**
     * vtbl +0x84: KaargInnSceneGridVisualObject::ReleasePanelBitmaps @004A03D4.
     * Fully ported. Java clears retained bitmap references instead of reproducing native delete/free calls.
     */
    @Override
    public void releasePanelBitmaps() {
        availableEntryBackdropBitmap = null;
        reservedEntryBackdropBitmap = null;
        centerAreaBitmap = null;
        leftUpperOverlayBitmap = null;
        leftLowerOverlayBitmap = null;
        rightUpperOverlayBitmap = null;

        if (Globals.mainWindow.sessionMode != CMainWindow.SESSION_MODE_CAMPAIGN) {
            for (int index = 0; index < questIconBitmaps.length; index++) {
                questIconBitmaps[index] = null;
            }
        }

        rewardEffectBitmaps.clear();
    }

    /**
     * Native branch helper inside KaargInnSceneGridVisualObject::Update @0049F467.
     * Fully ported.
     */
    private void renderKaargNetworkInnScene(
            KaargInnDialogVisualObject kaargOwner,
            int ownerLeft,
            int ownerTop,
            long now
    ) {
        if (kaargTavernSceneMode == -1) {
            kaargTavernFramesEScriptIndex = 0;
            int modeRoll = Utils.randInclusive(0x14);
            if (modeRoll == 0) {
                kaargTavernSceneMode = 1;
            } else if (modeRoll == 1) {
                kaargTavernSceneMode = 2;
            } else if (modeRoll == 4) {
                kaargTavernSceneMode = 5;
            } else if (modeRoll == 2 || modeRoll == 3 || modeRoll == 5) {
                kaargTavernSceneMode = 3;
            } else {
                kaargTavernSceneMode = 4;
            }

            if (kaargTavernSceneMode == 1) {
                Sound.playPointer(kaargOwner.kaargSceneSounds, 5);
            }
            if (kaargTavernSceneMode == 2) {
                Sound.playPointer(kaargOwner.kaargSceneSounds, 4);
            }
        }

        switch (kaargTavernSceneMode) {
            case 1 -> kaargTavernFramesA.drawCurrentFrame(ownerLeft + cRect.left + 0x48, ownerTop + cRect.top + 0x58);
            case 2 -> kaargTavernFramesB.drawCurrentFrame(ownerLeft + cRect.left + 0x48, ownerTop + cRect.top + 0x58);
            case 3 -> kaargTavernFramesC.drawCurrentFrame(ownerLeft + cRect.left + 0x70, ownerTop + cRect.top + 0x70);
            case 4 -> kaargTavernFramesD.drawCurrentFrame(ownerLeft + cRect.left + 0xC8, ownerTop + cRect.top + 0x88);
            case 5 -> kaargTavernFramesE.drawFrameAt(
                    ownerLeft + cRect.left + 0x58,
                    ownerTop + cRect.top + 0x54,
                    KAARG_TAVERN_FRAMES_E_SCRIPT[kaargTavernFramesEScriptIndex]
            );
            default -> {
            }
        }

        if (kaargTavernSceneMode != -1 && now - kaargSceneAdvanceTick > 100L) {
            switch (kaargTavernSceneMode) {
                case 1 -> {
                    if (kaargTavernFramesA.advance() == null) {
                        kaargTavernSceneMode = -1;
                        kaargTavernFramesA.setCurrentFrameIndex(0);
                    }
                    kaargSceneAdvanceTick = now;
                }
                case 2 -> {
                    if (kaargTavernFramesB.advance() == null) {
                        kaargTavernSceneMode = -1;
                        kaargTavernFramesB.setCurrentFrameIndex(0);
                    }
                    kaargSceneAdvanceTick = now;
                }
                case 3 -> {
                    if (kaargTavernFramesC.advance() == null) {
                        kaargTavernSceneMode = -1;
                        kaargTavernFramesC.setCurrentFrameIndex(0);
                    }
                    kaargSceneAdvanceTick = now;
                }
                case 4 -> {
                    if (kaargTavernFramesD.advance() == null) {
                        kaargTavernSceneMode = -1;
                        kaargTavernFramesD.setCurrentFrameIndex(0);
                    }
                    kaargSceneAdvanceTick = now;
                }
                case 5 -> {
                    kaargTavernFramesEScriptIndex += 1;
                    if (KAARG_TAVERN_FRAMES_E_SCRIPT[kaargTavernFramesEScriptIndex]
                            == KAARG_TAVERN_FRAMES_E_SCRIPT_END) {
                        kaargTavernSceneMode = -1;
                        kaargTavernFramesE.setCurrentFrameIndex(0);
                    }
                    kaargSceneAdvanceTick = now;
                }
                default -> {
                }
            }
        }

        renderAvailableEntries(ownerLeft, ownerTop, now);
        renderReservedEntries(ownerLeft, ownerTop, now);
    }

    /**
     * Native support extracted from KaargInnSceneGridVisualObject::Update @0049F467 static timer initialization.
     */
    private static void initializeKaargSceneTimers(long now) {
        if (kaargSceneTimersInitialized) {
            return;
        }
        kaargSceneTimersInitialized = true;
        kaargSceneRandomSeed = Utils.randInclusive(0x7FFF) / 0x41;
        selectedFrameAdvanceTick = now;
        kaargSceneInitTick = now;
        kaargSceneAdvanceTick = now;
        kaargAuxSceneTick = now;
    }

    /**
     * Native support extracted from KaargInnSceneGridVisualObject::KaargInnSceneGridVisualObject @0049F259
     * and @0049F353.
     */
    private void initializeKaargAmbientTimers() {
        lastAmbientDishSoundTick = (int) System.currentTimeMillis();
        nextAmbientDishSoundDelay = Utils.randBased(2000, 2000);
        lastAmbientVoiceSoundTick = (int) System.currentTimeMillis();
        nextAmbientVoiceSoundDelay = Utils.randBased(2000, 2000);
    }

}
