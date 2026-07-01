package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CMousePointer;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.sound.Sound;
import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native class: DruidInnDialogVisualObject.
 * Purpose: druid inn dialog in the menu `0x44C` branch.
 */
public class DruidInnDialogVisualObject extends BasicInnDialogVisualObject {
    public static final int NATIVE_SIZE = 0x16C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!
    private static final String ADD_WAV = "sfx/add.wav";
    private static final String NOADD_WAV = "sfx/noadd.wav";
    private static final String NOFIT_WAV = "sfx/town/shop/nofit.wav";
    private static final String HELPER_WAV = "sfx/town/inn/helper.wav";
    private static final String OUT_WAV = "sfx/out.wav";
    private static final String TALK_WAV = "sfx/talk.wav";
    private static final String DRUID_ENTER_WAV = "sfx/town_druid/inn/din1.wav";
    private static final String DRUID_WATER_FRAME_TEMPLATE = "graphics/interface/inn_druid/waterdrop/d%04d.bmp";
    private static final String DRUID_TAVERNER_A1_FRAME_TEMPLATE = "graphics/interface/inn_druid/taverner/a1%04d.bmp";
    private static final String DRUID_TAVERNER_A2_FRAME_TEMPLATE = "graphics/interface/inn_druid/taverner/a2%04d.bmp";
    private static final String[] DRUID_SCENE_SOUND_PATHS = {
            "sfx/town_druid/inn/dwater1.wav",
            "sfx/town_druid/inn/dwater2.wav",
            "sfx/town_druid/inn/dwater3.wav",
            "sfx/town_druid/inn/dwater4.wav",
            "sfx/town_druid/inn/ddruid3.wav",
            "sfx/town_druid/inn/ddruid4.wav",
            "sfx/town_druid/inn/ddruid41.wav",
            "sfx/town_druid/inn/dforest2.wav",
            "sfx/town_druid/inn/dbird4.wav",
            "sfx/town_druid/inn/dbird41.wav",
            "sfx/town_druid/inn/dbird42.wav"
    };
    private static final int DRUID_SCENE_LOOP_SOUND_INDEX = 6;

    //0x140..0x168
    public final Sound[] druidSceneSounds = new Sound[DRUID_SCENE_SOUND_PATHS.length];

    /**
     * Native: DruidInnDialogVisualObject::DruidInnDialogVisualObject @0049BD95.
     * Fully ported.
     */
    public DruidInnDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        super(id, xLeft, yTop, xRight, yBottom);
    }

    /**
     * vtbl +0x78: DruidInnDialogVisualObject::InitializeInnChildren @0049BDCA.
     * Fully ported.
     */
    @Override
    public void initialize() {
        dialogActiveFlag = 0;
        drinkSound = null;
        gulpSound = null;
        steamSound = null;
        ambientLoopSound = null;
        chairSound = null;
        addEntrySound = null;
        rejectAddEntrySound = null;
        noFitSound = null;
        enterSound = null;
        helperSound = null;
        selectionChangedSound = null;
        exitSound = null;
        talkSound = null;
        tipsPromptChild = null;

        leftPanelVisual = new InnLeftPanelVisualObject(0x44D, 0, 0, 0xA0, 0x1E0, this);
        actionPanelVisual = new InnRightPanelVisualObject(0x44E, 0x1E0, 0, 0x280, 0xEE, this);
        sceneGridVisual = new DruidInnSceneGridVisualObject(0x450, 0xA0, 0, 0x1E0, 0x1E0, this);

        addChild(leftPanelVisual);
        addChild(actionPanelVisual);
        addChild(sceneGridVisual);

        innEntrySelectionIndex = 0;
        availableInnEntries.clear();
        questsStorage = createQuestsStorage();
    }

    /**
     * vtbl +0x80: DruidInnDialogVisualObject::EnterInn @0049C359.
     * Fully ported.
     */
    @Override
    public void showDialog() {
        Globals.mousePointer.disableBackgroundCapture();
        moveSelectionInfoPanelIntoInn();

        if (shouldShowInnTipsPrompt()) {
            ensureTipsPromptChild();
        } else {
            clearTipsPromptChild();
        }

        populateInnEntries();
        materializeInnEntries();
        populateSelectedPartyEntries();

        DruidInnSceneGridVisualObject druidSceneGrid = (DruidInnSceneGridVisualObject) sceneGridVisual;
        druidSceneGrid.loadEntryBitmaps();
        if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN) {
            reloadFrameSequence(druidSceneGrid.druidWaterFrames, DRUID_WATER_FRAME_TEMPLATE, 1, 11);
            reloadFrameSequence(druidSceneGrid.druidTavernFramesA, DRUID_TAVERNER_A1_FRAME_TEMPLATE, 1, 41);
            reloadFrameSequence(druidSceneGrid.druidTavernFramesB, DRUID_TAVERNER_A2_FRAME_TEMPLATE, 1, 31);
        }
        actionPanelVisual.loadButtonBitmaps();
        actionPanelVisual.resetButtonState();
        druidSceneGrid.loadPanelBitmaps();
        leftPanelVisual.loadInnPanelBitmaps();
        actionPanelVisual.refreshPrimaryActionLabel();

        loadInnSounds();
        dialogActiveFlag = 1;
        clearScreen();
        showBaseDialog();
        enterSound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
        primeAmbientLoopSound(druidSceneSounds[DRUID_SCENE_LOOP_SOUND_INDEX]);
        CMousePointer.Cursor_Default.setToMousePointer();
        Globals.mousePointer.enableBackgroundCapture();

        questContext = 0;
        questSelectionIndex = -1;
        pendingInnQuestSelectionToggle = 0;
    }

    /**
     * vtbl +0x84: DruidInnDialogVisualObject::LeaveInn @0049CB27.
     * Fully ported.
     */
    @Override
    public HandlerVisualObject hideDialog(MessageCodes reason) {
        draw();
        leaveScenarioInnIfCampaign();
        sendLeaveInnPacket();
        dialogActiveFlag = 0;
        restoreSelectionInfoPanelFromInn();
        clearTipsPromptChild();

        actionPanelVisual.releaseButtonBitmaps();

        DruidInnSceneGridVisualObject druidSceneGrid = (DruidInnSceneGridVisualObject) sceneGridVisual;
        druidSceneGrid.releasePanelBitmaps();
        druidSceneGrid.releaseEntryBitmaps();
        clearFrameSequence(druidSceneGrid.druidWaterFrames);
        clearFrameSequence(druidSceneGrid.druidTavernFramesA);
        clearFrameSequence(druidSceneGrid.druidTavernFramesB);

        leftPanelVisual.releaseInnPanelBitmaps();

        releaseInnSounds();
        availableInnEntries.clear();
        selectedInnEntries.clear();
        reservedInnEntries.clear();
        hideBaseDialog(reason);
        clearQuestsStorage();
        activeRewardTokenEntries.clear();
        return this;
    }

    /**
     * vtbl +0x88: DruidInnDialogVisualObject::LoadInnSounds @0049C05F.
     * Fully ported.
     */
    @Override
    public void loadInnSounds() {
        releaseInnSounds();
        addEntrySound = loadSound(ADD_WAV);
        rejectAddEntrySound = loadSound(NOADD_WAV);
        noFitSound = loadSound(NOFIT_WAV);
        enterSound = loadSound(DRUID_ENTER_WAV);
        helperSound = loadSound(HELPER_WAV);
        exitSound = loadSound(OUT_WAV);
        talkSound = loadSound(TALK_WAV);
        for (int soundIndex = 0; soundIndex < DRUID_SCENE_SOUND_PATHS.length; soundIndex++) {
            druidSceneSounds[soundIndex] = loadSound(DRUID_SCENE_SOUND_PATHS[soundIndex]);
        }
    }

    /**
     * vtbl +0x8C: DruidInnDialogVisualObject::ReleaseInnSounds @0049C210.
     * Fully ported.
     */
    @Override
    public void releaseInnSounds() {
        addEntrySound = releaseSound(addEntrySound);
        rejectAddEntrySound = releaseSound(rejectAddEntrySound);
        noFitSound = releaseSound(noFitSound);
        enterSound = releaseSound(enterSound);
        helperSound = releaseSound(helperSound);
        exitSound = releaseSound(exitSound);
        talkSound = releaseSound(talkSound);
        for (int soundIndex = 0; soundIndex < druidSceneSounds.length; soundIndex++) {
            druidSceneSounds[soundIndex] =
                    releaseSound(druidSceneSounds[soundIndex]);
        }
    }

}
