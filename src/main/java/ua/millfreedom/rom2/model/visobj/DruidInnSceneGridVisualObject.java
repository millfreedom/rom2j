package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CBmp64kFrameSequence;
import ua.millfreedom.rom2.model.sound.Sound;
import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native class: DruidInnSceneGridVisualObject (vtbl @0x005CE710).
 * Purpose: druid inn animated scene grid for the menu `0x44C` dialog.
 */
public class DruidInnSceneGridVisualObject extends BasicInnSceneGridVisualObject {
    public static final int NATIVE_SIZE = 0x468; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!
    private static final String DRUID_TAVERN_MAIN_BMP = "graphics/interface/inn_druid/tavernmain.bmp";
    private static final String DRUID_TAVERNER_STILL_BMP = "graphics/interface/inn_druid/taverner/a30001.bmp";
    private static final String PRIMARY_SELECTION_BMP = "graphics/interface/inn/manback.bmp";
    private static final String SECONDARY_SELECTION_BMP = "graphics/interface/inn/manbacktalk.bmp";
    private static final String LEFT_UPPER_OVERLAY_BMP = "graphics/interface/inn/luover.bmp";
    private static final String LEFT_LOWER_OVERLAY_BMP = "graphics/interface/inn/ldover.bmp";
    private static final String RIGHT_UPPER_OVERLAY_BMP = "graphics/interface/inn/ruover.bmp";
    private static final String QUEST_BITMAP_TEMPLATE = "graphics/interface/inn/quests/%02d.bmp";
    private static boolean druidSceneTimersInitialized;
    private static long druidWaterFrameAdvanceTick;
    private static long druidTavernSceneAdvanceTick;
    private static long druidAuxSceneInitTick;
    private static int nextDruidTavernSceneDelay;
    private static int druidTavernSceneMode;

    //0x3cc
    public CBmp64k druidTavernerStillBitmap;
    //0x3d0
    public final CBmp64kFrameSequence druidWaterFrames = new CBmp64kFrameSequence();
    //0x400
    public final CBmp64kFrameSequence druidTavernFramesA = new CBmp64kFrameSequence();
    //0x430
    public final CBmp64kFrameSequence druidTavernFramesB = new CBmp64kFrameSequence();
    //0x460
    public int lastAmbientBirdSoundTick;
    //0x464
    public int nextAmbientBirdSoundDelay;

    /**
     * Native: DruidInnSceneGridVisualObject::DruidInnSceneGridVisualObject @0049CE4F.
     * Fully ported.
     */
    public DruidInnSceneGridVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            BasicInnDialogVisualObject ownerDialog
    ) {
        super(id, xLeft, yTop, xRight, yBottom, ownerDialog);
        initializeDruidAmbientTimers();
    }

    /**
     * Native support extracted from DruidInnSceneGridVisualObject::DruidInnSceneGridVisualObject @0049CD96
     * and DruidInnSceneGridVisualObject::DruidInnSceneGridVisualObject @0049CE4F.
     */
    private void initializeDruidAmbientTimers() {
        lastAmbientBirdSoundTick = (int) System.currentTimeMillis();
        nextAmbientBirdSoundDelay = Utils.randBased(2000, 2000);
        druidTavernerStillBitmap = null;
    }

    /**
     * vtbl +0x2C: DruidInnSceneGridVisualObject::Update @0049CF22.
     * Fully ported.
     */
    @Override
    public void update() {
        long now = System.currentTimeMillis();
        int currentTick = (int) now;
        initializeDruidSceneTimers(now);

        int ownerLeft = ownerDialog.cRect.left;
        int ownerTop = ownerDialog.cRect.top;
        DruidInnDialogVisualObject druidOwner = (DruidInnDialogVisualObject) ownerDialog;

        Globals.renderer.lockSurface();
        try {
            centerAreaBitmap.draw(ownerLeft + cRect.left, ownerTop + cRect.top, 0, 0, false);
            if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN) {
                renderDruidNetworkInnScene(druidOwner, ownerLeft, ownerTop, now);
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

        updateChildVisualObjects();

        if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN
                && nextAmbientBirdSoundDelay < currentTick - lastAmbientBirdSoundTick) {
            Sound.playPointer(druidOwner.druidSceneSounds, Utils.randExclusive(3) + 8);
            nextAmbientBirdSoundDelay = Utils.randBased(2000, 2000);
            lastAmbientBirdSoundTick = (int) System.currentTimeMillis();
        }
    }

    /**
     * vtbl +0x80: DruidInnSceneGridVisualObject::LoadPanelBitmaps @0049DA92.
     * The druid-specific bitmap load order and non-campaign quest-strip binding are ported.
     * Fully ported.
     */
    @Override
    public void loadPanelBitmaps() {
        releasePanelBitmaps();

        availableEntryBackdropBitmap = new CBmp64k(PRIMARY_SELECTION_BMP);
        Globals.renderer.refreshMousePointer();
        reservedEntryBackdropBitmap = new CBmp64k(SECONDARY_SELECTION_BMP);
        Globals.renderer.refreshMousePointer();
        centerAreaBitmap = new CBmp64k(DRUID_TAVERN_MAIN_BMP);
        Globals.renderer.refreshMousePointer();
        leftUpperOverlayBitmap = new CBmp64k(LEFT_UPPER_OVERLAY_BMP);
        Globals.renderer.refreshMousePointer();
        leftLowerOverlayBitmap = new CBmp64k(LEFT_LOWER_OVERLAY_BMP);
        Globals.renderer.refreshMousePointer();
        rightUpperOverlayBitmap = new CBmp64k(RIGHT_UPPER_OVERLAY_BMP);
        Globals.renderer.refreshMousePointer();
        druidTavernerStillBitmap = new CBmp64k(DRUID_TAVERNER_STILL_BMP);
        Globals.renderer.refreshMousePointer();

        if (Globals.mainWindow.sessionMode != CMainWindow.SESSION_MODE_CAMPAIGN) {
            for (int index = 0; index < questIconBitmaps.length; index++) {
                questIconBitmaps[index] = new CBmp64k(String.format(QUEST_BITMAP_TEMPLATE, index + 1));
            }
        }
    }

    /**
     * vtbl +0x84: DruidInnSceneGridVisualObject::ReleasePanelBitmaps @0049DE3E.
     * Fully ported. Java clears retained bitmap references instead of reproducing native delete/free calls.
     */
    @Override
    public void releasePanelBitmaps() {
        super.releasePanelBitmaps();
        druidTavernerStillBitmap = null;
    }

    /**
     * Native branch helper inside DruidInnSceneGridVisualObject::Update @0049CF22.
     * Fully ported.
     */
    private void renderDruidNetworkInnScene(
            DruidInnDialogVisualObject druidOwner,
            int ownerLeft,
            int ownerTop,
            long now
    ) {
        druidWaterFrames.drawCurrentFrame(ownerLeft + cRect.left + 0xA8, ownerTop + cRect.top + 0x90);
        if (now - druidWaterFrameAdvanceTick > 100L) {
            if (druidWaterFrames.getCurrentFrameIndex() == 0) {
                switch (Utils.randExclusive(4)) {
                    case 0 -> Sound.playPointer(druidOwner.druidSceneSounds, 0);
                    case 1 -> Sound.playPointer(druidOwner.druidSceneSounds, 1);
                    case 2 -> Sound.playPointer(druidOwner.druidSceneSounds, 2);
                    case 3 -> Sound.playPointer(druidOwner.druidSceneSounds, 3);
                    default -> {
                    }
                }
            }
            druidWaterFrames.advanceLooped();
            druidWaterFrameAdvanceTick = now;
        }

        if (nextDruidTavernSceneDelay < now - druidTavernSceneAdvanceTick) {
            druidTavernSceneMode = (nextDruidTavernSceneDelay & 3) + 1;
            if (druidTavernSceneMode == 4) {
                druidTavernSceneMode = 3;
            }
            if (druidTavernSceneMode == 1) {
                Sound.playPointer(druidOwner.druidSceneSounds, 5);
            }
            if (druidTavernSceneMode == 2) {
                Sound.playPointer(druidOwner.druidSceneSounds, 4);
            }
        }

        switch (druidTavernSceneMode) {
            case 1 -> {
                druidTavernFramesA.drawCurrentFrame(ownerLeft + cRect.left + 0x28, ownerTop + cRect.top + 0x80);
                if (druidTavernFramesA.getCurrentFrameIndex() == 0x1E) {
                    Sound.playPointer(druidOwner.druidSceneSounds, 6);
                }
            }
            case 2 -> druidTavernFramesB.drawCurrentFrame(ownerLeft + cRect.left + 0x28, ownerTop + cRect.top + 0x80);
            case 3, 4 ->
                    druidTavernerStillBitmap.drawRectMasked(ownerLeft + cRect.left + 0x68, ownerTop + cRect.top + 0x98);
            default -> {
            }
        }

        if (druidTavernSceneMode != -1 && now - druidTavernSceneAdvanceTick > 100L) {
            switch (druidTavernSceneMode) {
                case 1 -> {
                    if (druidTavernFramesA.advance() == null) {
                        druidTavernSceneMode = -1;
                        nextDruidTavernSceneDelay = nextDruidIdleDelay();
                        druidTavernFramesA.setCurrentFrameIndex(0);
                    }
                    druidTavernSceneAdvanceTick = now;
                }
                case 2 -> {
                    if (druidTavernFramesB.advance() == null) {
                        druidTavernSceneMode = -1;
                        nextDruidTavernSceneDelay = nextDruidIdleDelay();
                        druidTavernFramesB.setCurrentFrameIndex(0);
                    }
                    druidTavernSceneAdvanceTick = now;
                }
                case 3 -> {
                    druidTavernSceneMode += 1;
                    nextDruidTavernSceneDelay = nextDruidStillDelay();
                    druidTavernSceneAdvanceTick = now;
                }
                case 4 -> druidTavernSceneMode = -1;
                default -> {
                }
            }
        }

        renderAvailableEntries(ownerLeft, ownerTop, now);
        renderReservedEntries(ownerLeft, ownerTop, now);
    }

    /**
     * Native support extracted from DruidInnSceneGridVisualObject::Update @0049CF22 static timer initialization.
     */
    private static void initializeDruidSceneTimers(long now) {
        if (druidSceneTimersInitialized) {
            return;
        }
        druidSceneTimersInitialized = true;
        nextDruidTavernSceneDelay = nextDruidIdleDelay();
        selectedFrameAdvanceTick = now;
        druidWaterFrameAdvanceTick = now;
        druidTavernSceneAdvanceTick = now;
        druidAuxSceneInitTick = now;
    }

    /**
     * Native support extracted from the `rand() >> 4 + 0xC80` delay math in DruidInnSceneGridVisualObject::Update @0049CF22.
     */
    private static int nextDruidIdleDelay() {
        return Utils.randInclusive(3200, 5247);
    }

    /**
     * Native support extracted from the `rand() >> 4 + 1000` delay math in DruidInnSceneGridVisualObject::Update @0049CF22.
     */
    private static int nextDruidStillDelay() {
        return Utils.randInclusive(1000, 3047);
    }

}
