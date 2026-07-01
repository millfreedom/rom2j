package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CMousePointer;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.sound.Sound;
import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native class: KaargInnDialogVisualObject.
 * Purpose: kaarg inn dialog in the menu `0x44C` branch.
 */
public class KaargInnDialogVisualObject extends BasicInnDialogVisualObject {
    public static final int NATIVE_SIZE = 0x168; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!
    private static final String ADD_WAV = "sfx/add.wav";
    private static final String NOADD_WAV = "sfx/noadd.wav";
    private static final String NOFIT_WAV = "sfx/town/shop/nofit.wav";
    private static final String HELPER_WAV = "sfx/town/inn/helper.wav";
    private static final String OUT_WAV = "sfx/out.wav";
    private static final String TALK_WAV = "sfx/talk.wav";
    private static final String KAARG_ENTER_WAV = "sfx/town_kaarg/inn/kin1.wav";
    private static final String KAARG_TAVERNER_A1_FRAME_TEMPLATE =
            "graphics/interface/inn_kaarg/taverner/a1%04d.bmp";
    private static final String KAARG_TAVERNER_A2_FRAME_TEMPLATE =
            "graphics/interface/inn_kaarg/taverner/a2%04d.bmp";
    private static final String KAARG_TAVERNER_A3_FRAME_TEMPLATE =
            "graphics/interface/inn_kaarg/taverner/a3%04d.bmp";
    private static final String KAARG_TAVERNER_A4_FRAME_TEMPLATE =
            "graphics/interface/inn_kaarg/taverner/a4%04d.bmp";
    private static final String KAARG_TAVERNER_A5_FRAME_TEMPLATE =
            "graphics/interface/inn_kaarg/taverner/a5%04d.bmp";
    private static final String[] KAARG_SCENE_SOUND_PATHS = {
            "sfx/town_kaarg/inn/kdish1.wav",
            "sfx/town_kaarg/inn/kdish2.wav",
            "sfx/town_kaarg/inn/kdish3.wav",
            "sfx/town_kaarg/inn/kdish4.wav",
            "sfx/town_kaarg/inn/kman2.wav",
            "sfx/town_kaarg/inn/kman3.wav",
            "sfx/town_kaarg/inn/kvox5.wav",
            "sfx/town_kaarg/inn/kvox6.wav",
            "sfx/town_kaarg/inn/kvox7.wav",
            "sfx/town_kaarg/inn/kvox8.wav"
    };
    private static final int KAARG_VOX5_SOUND_INDEX = 6;

    //0x140..0x164
    public final Sound[] kaargSceneSounds = new Sound[KAARG_SCENE_SOUND_PATHS.length];

    /**
     * Native: KaargInnDialogVisualObject::KaargInnDialogVisualObject @0049E187.
     * Fully ported.
     */
    public KaargInnDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        super(id, xLeft, yTop, xRight, yBottom);
    }

    /**
     * vtbl +0x78: KaargInnDialogVisualObject::InitializeInnChildren @0049E1BC.
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
        sceneGridVisual = new KaargInnSceneGridVisualObject(0x450, 0xA0, 0, 0x1E0, 0x1E0, this);

        addChild(leftPanelVisual);
        addChild(actionPanelVisual);
        addChild(sceneGridVisual);

        innEntrySelectionIndex = 0;
        availableInnEntries.clear();
        questsStorage = createQuestsStorage();
    }

    /**
     * vtbl +0x80: KaargInnDialogVisualObject::EnterInn @0049E451.
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

        KaargInnSceneGridVisualObject kaargSceneGrid = (KaargInnSceneGridVisualObject) sceneGridVisual;
        kaargSceneGrid.loadEntryBitmaps();
        if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN) {
            reloadFrameSequence(
                    kaargSceneGrid.kaargTavernFramesA,
                    KAARG_TAVERNER_A1_FRAME_TEMPLATE,
                    1,
                    16
            );
            reloadFrameSequence(
                    kaargSceneGrid.kaargTavernFramesB,
                    KAARG_TAVERNER_A2_FRAME_TEMPLATE,
                    1,
                    26
            );
            reloadFrameSequence(
                    kaargSceneGrid.kaargTavernFramesC,
                    KAARG_TAVERNER_A3_FRAME_TEMPLATE,
                    1,
                    4
            );
            reloadFrameSequence(
                    kaargSceneGrid.kaargTavernFramesD,
                    KAARG_TAVERNER_A4_FRAME_TEMPLATE,
                    0,
                    7
            );
            reloadFrameSequence(
                    kaargSceneGrid.kaargTavernFramesE,
                    KAARG_TAVERNER_A5_FRAME_TEMPLATE,
                    0,
                    29
            );
        }
        actionPanelVisual.loadButtonBitmaps();
        actionPanelVisual.resetButtonState();
        kaargSceneGrid.loadPanelBitmaps();
        leftPanelVisual.loadInnPanelBitmaps();
        actionPanelVisual.refreshPrimaryActionLabel();

        loadInnSounds();
        dialogActiveFlag = 1;
        clearScreen();
        showBaseDialog();
        enterSound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
        primeAmbientLoopSound(kaargSceneSounds[KAARG_VOX5_SOUND_INDEX]);
        CMousePointer.Cursor_Default.setToMousePointer();
        Globals.mousePointer.enableBackgroundCapture();

        questContext = 0;
        questSelectionIndex = -1;
        pendingInnQuestSelectionToggle = 0;
    }

    /**
     * vtbl +0x84: KaargInnDialogVisualObject::LeaveInn @0049ECF7.
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

        KaargInnSceneGridVisualObject kaargSceneGrid = (KaargInnSceneGridVisualObject) sceneGridVisual;
        kaargSceneGrid.releasePanelBitmaps();
        kaargSceneGrid.releaseEntryBitmaps();
        clearFrameSequence(kaargSceneGrid.kaargTavernFramesA);
        clearFrameSequence(kaargSceneGrid.kaargTavernFramesB);
        clearFrameSequence(kaargSceneGrid.kaargTavernFramesC);
        clearFrameSequence(kaargSceneGrid.kaargTavernFramesD);
        clearFrameSequence(kaargSceneGrid.kaargTavernFramesE);

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
     * vtbl +0x88: KaargInnDialogVisualObject::LoadInnSounds @0049EF88.
     * Fully ported.
     */
    @Override
    public void loadInnSounds() {
        releaseInnSounds();
        addEntrySound = loadSound(ADD_WAV);
        rejectAddEntrySound = loadSound(NOADD_WAV);
        noFitSound = loadSound(NOFIT_WAV);
        enterSound = loadSound(KAARG_ENTER_WAV);
        helperSound = loadSound(HELPER_WAV);
        exitSound = loadSound(OUT_WAV);
        talkSound = loadSound(TALK_WAV);
        for (int soundIndex = 0; soundIndex < kaargSceneSounds.length; soundIndex++) {
            kaargSceneSounds[soundIndex] = loadSound(KAARG_SCENE_SOUND_PATHS[soundIndex]);
        }
    }

    /**
     * vtbl +0x8C: KaargInnDialogVisualObject::ReleaseInnSounds @0049F122.
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
        for (int soundIndex = 0; soundIndex < kaargSceneSounds.length; soundIndex++) {
            kaargSceneSounds[soundIndex] =
                    releaseSound(kaargSceneSounds[soundIndex]);
        }
    }

}
