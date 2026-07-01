package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBmp64kFrameSequence;
import ua.millfreedom.rom2.model.CMousePointer;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.QuestsStorage;
import ua.millfreedom.rom2.model.SelectedUnitsSnapshot;
import ua.millfreedom.rom2.model.ScriptDataSupport;
import ua.millfreedom.rom2.model.TokenEntry;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.action.LeaveInnAction;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.quest.Quest;
import ua.millfreedom.rom2.model.sound.Sound;
import ua.millfreedom.rom2.model.sound.SoundSystem;
import ua.millfreedom.rom2.model.window.CMainWindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static ua.millfreedom.rom2.model.enums.MessageCodes.CLEAR_TIP_PROMPT;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SELECT_NEXT_HERO;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SELECT_PREVIOUS_HERO;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RENDER_FRAME;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.INN_DIALOG;

/**
 * Native class: BasicInnDialogVisualObject.
 * Purpose: dialog branch keyed by id 0x44C in main window setup.
 */
public class BasicInnDialogVisualObject extends HandlerVisualObject {
    public static final int NATIVE_SIZE = 0x144; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final String INN_DRINK_WAV = "sfx/town/inn/drink.wav";
    private static final String INN_GLOTOK_WAV = "sfx/town/inn/glotok.wav";
    private static final String INN_STEAM_WAV = "sfx/town/inn/steam.wav";
    private static final String INN_WATER_WAV = "sfx/town/inn/water.wav";
    private static final String INN_CHAIR_WAV = "sfx/town/inn/chair.wav";
    private static final String ADD_WAV = "sfx/add.wav";
    private static final String NOADD_WAV = "sfx/noadd.wav";
    private static final String NOFIT_WAV = "sfx/town/shop/nofit.wav";
    private static final String INN_ENTER_WAV = "sfx/town/inn/enter.wav";
    private static final String INN_HELPER_WAV = "sfx/town/inn/helper.wav";
    private static final String SHOP_BREATH_WAV = "sfx/town/shop/breath.wav";
    private static final String OUT_WAV = "sfx/out.wav";
    private static final String TALK_WAV = "sfx/talk.wav";
    private static final String NPC_ACCEPT_SCRIPT = "npc%daccept%d";
    private static final String NPC_REJECT_SCRIPT = "npc%dreject%d";
    private static final String NPC_TALK_SCRIPT = "npc%dtalk%d";
    private static final String CANDLE_FRAME_TEMPLATE = "graphics/interface/inn/candle/t%04d.bmp";
    private static final String CAULDRON_FRAME_TEMPLATE = "graphics/interface/inn/cauldron/t%04d.bmp";
    private static final String TENDER_BREATH_FRAME_TEMPLATE = "graphics/interface/inn/tender/breath/br%04d.bmp";
    private static final String TENDER_DRINK_FRAME_TEMPLATE = "graphics/interface/inn/tender/drink/dr%04d.bmp";
    private static final int INN_ENTRY_HIRED_FLAG = 0x80000000;
    private static final int INN_ENTRY_GROUP_MASK = 0x70000000;
    private static final int INN_ENTRY_ACCEPTABLE_GROUP = 0x10000000;
    private static final int INN_REWARD_SENTINEL_HASH_THRESHOLD = 0xFFF0;

    //0x68
    public MapVisualObject mapVisualObject;
    //0x6c
    public CVisualObject selectionInfoPanelVisualObject;
    //0x70
    public InnLeftPanelVisualObject leftPanelVisual;
    //0x74
    public InnRightPanelVisualObject actionPanelVisual;
    //0x7c
    public BasicInnSceneGridVisualObject sceneGridVisual;
    //0x80
    public CVisualObject tipsPromptChild;
    //0x84
    public Sound drinkSound;
    //0x88
    public Sound gulpSound;
    //0x8c
    public Sound steamSound;
    //0x90
    public Sound ambientLoopSound;
    //0x94
    public Sound chairSound;
    //0x98
    public Sound addEntrySound;
    //0x9c
    public Sound rejectAddEntrySound;
    //0xa0
    public Sound noFitSound;
    //0xa4
    public Sound enterSound;
    //0xa8
    public Sound helperSound;
    //0xac
    public Sound selectionChangedSound;
    //0xb0
    public Sound exitSound;
    //0xb4
    public Sound talkSound;
    //0xb8
    public int innEntrySelectionIndex;
    //0xbc
    public int selectedPartyIndex;
    //0xc0
    public final List<CUnit> availableInnEntries = new ArrayList<>();
    //0xd4
    public final List<CUnit> selectedInnEntries = new ArrayList<>();
    //0xe8
    public final List<CUnit> reservedInnEntries = new ArrayList<>();
    //0xfc
    public final List<Integer> innEntryIds = new ArrayList<>();
    //0x110
    public int dialogActiveFlag;
    //0x114
    public QuestsStorage questsStorage;
    //0x118
    public int questContext;
    //0x11c
    public int questSelectionIndex;
    //0x120
    public int questMirrorSelectionIndex;
    //0x124
    public final List<TokenEntry> activeRewardTokenEntries = new ArrayList<>();
    //0x138
    public int innInteractionTargetTokenId;
    //0x13c
    public int pendingInnQuestSelectionToggle;

    /**
     * Native: BasicInnDialogVisualObject::BasicInnDialogVisualObject @0049A428.
     * Fully ported.
     */
    public BasicInnDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        super(id, xLeft, yTop, xRight, yBottom, null);
    }

    /**
     * Native support extracted from CMainWindow::showInnDialog @0048B885 and ShowInnDialogAction payload handling.
     * Partial port. Stashes the recovered inn/building token consumed by the non-campaign quest/reward branch.
     */
    public void setInnInteractionTargetTokenId(int innTokenId) {
        innInteractionTargetTokenId = innTokenId;
    }

    /**
     * vtbl +0x2C: BasicInnDialogVisualObject::Update @0049A8F3.
     * Fully ported.
     */
    @Override
    public void update() {
        if (dialogActiveFlag != 0) {
            super.update();
        }
        primeAmbientLoopSound(ambientLoopSound);
    }

    /**
     * vtbl +0x30: BasicInnDialogVisualObject::RenderSelf @0049A924.
     * Fully ported. Native method is a no-op.
     */
    @Override
    public void renderSelf(CRect clipRect) {
        // Native no-op.
    }

    /**
     * vtbl +0x48: BasicInnDialogVisualObject::OnMessage @0049A931.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        if (msg == RENDER_FRAME) {
            if ((!activeRewardTokenEntries.isEmpty() || questsStorage.questsByKey.size() != 0)
                    && actionPanelVisual.buttonLabels.get(0).equals(InnRightPanelVisualObject.BLANK_BUTTON_LABEL)) {
                actionPanelVisual.refreshPrimaryActionLabel();
            }

            int field404 = Globals.mainWindow.dialogsMask;
            if (field404 == 4 || field404 == 5) {
                draw();
            }
        } else if (msg == SELECT_PREVIOUS_HERO) {
            selectPreviousInnEntry();
        } else if (msg == SELECT_NEXT_HERO) {
            selectNextInnEntry();
        } else if (msg == CLEAR_TIP_PROMPT) {
            clearTipsPromptChild();
        }

        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * vtbl +0x4C: BasicInnDialogVisualObject::OnMouseMove @0049AB10.
     * Fully ported.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        if (!isPointInsideChild(actionPanelVisual, x, y)) {
            actionPanelVisual.resetButtonState();
        }
        return super.onMouseMove(nFlags, x, y);
    }

    /**
     * vtbl +0x6C: BasicInnDialogVisualObject::OnKeyDown @0049AAE2.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        if (nChar != 0x1B) {
            return 0;
        }
        handleExitAction();
        return 1;
    }

    /**
     * Native: BasicInnDialogVisualObject::InitializeInnChildren @0049A65E.
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
        sceneGridVisual = new BasicInnSceneGridVisualObject(0x450, 0xA0, 0, 0x1E0, 0x1E0, this);

        addChild(leftPanelVisual);
        addChild(actionPanelVisual);
        addChild(sceneGridVisual);

        innEntrySelectionIndex = 0;
        availableInnEntries.clear();
        questsStorage = createQuestsStorage();
    }

    /**
     * vtbl +0x80: BasicInnDialogVisualObject::EnterInn @0049ABFE.
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

        populateActiveRewardEntriesFromPendingDialogItemList();
        populateInnEntries();
        materializeInnEntries();
        populateSelectedPartyEntries();

        sceneGridVisual.loadEntryBitmaps();
        if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN) {
            reloadFrameSequence(sceneGridVisual.candleFrames, CANDLE_FRAME_TEMPLATE, 0, 10);
            reloadFrameSequence(sceneGridVisual.cauldronFrames, CAULDRON_FRAME_TEMPLATE, 0, 21);
            reloadFrameSequence(sceneGridVisual.innKeeperBreathFrames, TENDER_BREATH_FRAME_TEMPLATE, 1, 25);
            reloadFrameSequence(sceneGridVisual.innKeeperDrinkFrames, TENDER_DRINK_FRAME_TEMPLATE, 1, 41);
        }
        actionPanelVisual.loadButtonBitmaps();
        actionPanelVisual.resetButtonState();
        sceneGridVisual.loadPanelBitmaps();
        leftPanelVisual.loadInnPanelBitmaps();
        actionPanelVisual.refreshPrimaryActionLabel();
        loadInnSounds();

        dialogActiveFlag = 1;
        clearScreen();
        super.showDialog();
        enterSound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
        CMousePointer.Cursor_Default.setToMousePointer();
        Globals.mousePointer.enableBackgroundCapture();

        questContext = 0;
        questSelectionIndex = -1;
        pendingInnQuestSelectionToggle = 0;
    }

    /**
     * Native support extracted from CMainWindow::showInnDialog @0048BAB9 / @0048BB07 and
     * MapVisualObject::HandleGameAction @00412711 / @00412811.
     */
    private void populateActiveRewardEntriesFromPendingDialogItemList() {
        if (Globals.mainWindow.sessionMode != CMainWindow.SESSION_MODE_CAMPAIGN) {
            for (TokenEntry entry : mapVisualObject.dialogItemTokenEntries) {
                if (isVisibleInnRewardEntry(entry)) {
                    activeRewardTokenEntries.add(entry);
                }
            }
            mapVisualObject.dialogItemTokenEntries.clear();
        }
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @004127E2-00412802.
     */
    public static boolean isVisibleInnRewardEntry(TokenEntry entry) {
        return entry.hasKnownItemName() || (entry.packedTokenHash & 0xFFFF) > INN_REWARD_SENTINEL_HASH_THRESHOLD;
    }

    /**
     * vtbl +0x84: BasicInnDialogVisualObject::LeaveInn @0049B41B.
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
        sceneGridVisual.releasePanelBitmaps();
        sceneGridVisual.releaseEntryBitmaps();
        clearFrameSequence(sceneGridVisual.candleFrames);
        clearFrameSequence(sceneGridVisual.cauldronFrames);
        clearFrameSequence(sceneGridVisual.innKeeperDrinkFrames);
        clearFrameSequence(sceneGridVisual.innKeeperBreathFrames);
        leftPanelVisual.releaseInnPanelBitmaps();
        releaseInnSounds();
        availableInnEntries.clear();
        selectedInnEntries.clear();
        reservedInnEntries.clear();
        super.hideDialog(reason);
        clearQuestsStorage();
        activeRewardTokenEntries.clear();
        return this;
    }

    /**
     * Native: BasicInnDialogVisualObject::LeaveInn @0049B41B.
     * Delegates to MapVisualObject::commitLeaveInnSelection @0041A861 for the campaign cleanup or remote packet path.
     */
    protected void sendLeaveInnPacket() {
        int leaveSelectionValue = pendingInnQuestSelectionToggle == 0
                ? questSelectionIndex
                : LeaveInnAction.LEAVE_INN_SELECTION_SENTINEL;
        mapVisualObject.commitLeaveInnSelection(leaveSelectionValue);
    }

    /**
     * vtbl +0x88: BasicInnDialogVisualObject::LoadInnSounds @0049BB47.
     * Fully ported.
     */
    public void loadInnSounds() {
        releaseInnSounds();
        drinkSound = loadSound(INN_DRINK_WAV);
        gulpSound = loadSound(INN_GLOTOK_WAV);
        steamSound = loadSound(INN_STEAM_WAV);
        ambientLoopSound = loadSound(INN_WATER_WAV);
        chairSound = loadSound(INN_CHAIR_WAV);
        addEntrySound = loadSound(ADD_WAV);
        rejectAddEntrySound = loadSound(NOADD_WAV);
        noFitSound = loadSound(NOFIT_WAV);
        enterSound = loadSound(INN_ENTER_WAV);
        helperSound = loadSound(INN_HELPER_WAV);
        selectionChangedSound = loadSound(SHOP_BREATH_WAV);
        exitSound = loadSound(OUT_WAV);
        talkSound = loadSound(TALK_WAV);
    }

    /**
     * vtbl +0x8C: BasicInnDialogVisualObject::ReleaseInnSounds @0049BC86.
     * Fully ported.
     */
    public void releaseInnSounds() {
        drinkSound = releaseSound(drinkSound);
        gulpSound = releaseSound(gulpSound);
        steamSound = releaseSound(steamSound);
        ambientLoopSound = releaseSound(ambientLoopSound);
        chairSound = releaseSound(chairSound);
        addEntrySound = releaseSound(addEntrySound);
        rejectAddEntrySound = releaseSound(rejectAddEntrySound);
        noFitSound = releaseSound(noFitSound);
        enterSound = releaseSound(enterSound);
        helperSound = releaseSound(helperSound);
        selectionChangedSound = releaseSound(selectionChangedSound);
        exitSound = releaseSound(exitSound);
        talkSound = releaseSound(talkSound);
    }

    /**
     * Native owner: BasicInnDialogVisualObject action chain called from InnRightPanelVisualObject::OnLButtonUp @00496BBA.
     * not ported.
     */
    public void handlePrimaryActionNetwork() {
    }

    /**
     * Native owner: BasicInnDialogVisualObject action chain called from InnRightPanelVisualObject::OnLButtonUp @00496BBA.
     * not ported.
     */
    public void handleSecondaryActionNetwork() {
    }

    /**
     * Native owner: BasicInnDialogVisualObject action chain called from InnRightPanelVisualObject::OnLButtonUp @00496BBA.
     * not ported.
     */
    public void handlePrimaryActionCampaign() {
    }

    /**
     * Native owner: BasicInnDialogVisualObject action chain called from InnRightPanelVisualObject::OnLButtonUp @00496BBA.
     * not ported.
     */
    public void handleSecondaryActionCampaign() {
    }

    /**
     * Native: BasicInnDialogVisualObject::handleExitAction @0049B69B.
     * Fully ported.
     */
    public void handleExitAction() {
        onMessage(MessageCodes.DIALOG_OK, 0, 0);
        if (Globals.mainWindow.dialogsMask == INN_DIALOG.mask) {
            Globals.mainWindow.postMessage(MessageCodes.SHOW_CURRENT_TOWN_DIALOG, 0, 0);
        }
    }

    /**
     * Native owner: QuestsStorage::quests_Count call sites in BasicInnSceneGridVisualObject::GetSelectionIndexAtScreenPoint @00499367.
     * Fully ported at the modeled QuestsStorage boundary.
     */
    public int getQuestCount() {
        return questsStorage.questsByKey.size();
    }

    /**
     * Native support extracted from the QuestsStorage iteration path in BasicInnSceneGridVisualObject::GetSelectionIndexAtScreenPoint @00499367.
     * Fully ported at the modeled QuestsStorage boundary.
     */
    public int findQuestSelectionIdAtPoint(CRect[] questRects, int localX, int localY) {
        int index = 0;
        for (Quest quest : questsStorage.questsByKey.values()) {
            if (questRects[index].contains(localX, localY)) {
                return quest.questKey;
            }
            index++;
        }
        return -1;
    }

    /**
     * Native: BasicInnDialogVisualObject::findInnEntryIdIndex @0049AB92.
     * Fully ported.
     */
    protected int findInnEntryIdIndex(CUnit entry) {
        int serverId = Short.toUnsignedInt(entry.serverID);
        for (int index = 0; index < innEntryIds.size(); index++) {
            if ((innEntryIds.get(index) & 0xFFFF) == serverId) {
                return index;
            }
        }
        return -1;
    }

    /**
     * Native support extracted from the map quest probe in BasicInnSceneGridVisualObject::OnLButtonDblClk @0049A197.
     * Fully ported.
     */
    public boolean hasQuestToggleTarget() {
        MapVisualObject mapVisualObject = Globals.mainWindow.pMapVisualObject;
        int questKey = mapVisualObject.questStorage.findQuestKeyByMessage(
                Quest.MESSAGE_INN_PROBE,
                mapVisualObject.currentPlayer.playerId,
                innInteractionTargetTokenId
        );
        return questKey != 0;
    }

    /**
     * Native support extracted from the selection-info mode read through FUN_004B011E @004B011E in
     * BasicInnSceneGridVisualObject::Update @00497ADF.
     */
    public boolean useHumanBottomStrip() {
        return selectionInfoPanelVisualObject instanceof SelectionInfoPanelVisualObject selectionInfoPanel
                && selectionInfoPanel.selectionInfoViewMode0x70 != 0;
    }

    /**
     * Native: BasicInnDialogVisualObject::acceptSelectedInnEntryOffer @0049B812.
     * Fully ported.
     */
    public void acceptSelectedInnEntryOffer() {
        CUnit selectedEntry = availableInnEntries.get(innEntrySelectionIndex);
        int entryIdIndex = findInnEntryIdIndex(selectedEntry);
        int packedEntry = innEntryIds.get(entryIdIndex);
        int entryId = packedEntry & 0xFFFF;
        int scenarioChapter = Globals.scenarioLib.getVar(0x300);
        if ((packedEntry & INN_ENTRY_GROUP_MASK) == INN_ENTRY_ACCEPTABLE_GROUP) {
            RoleDialogSupport.showRoleKeyDialog(String.format(Locale.ROOT, NPC_ACCEPT_SCRIPT, entryId, scenarioChapter));
            packedEntry |= INN_ENTRY_HIRED_FLAG;
            innEntryIds.set(entryIdIndex, packedEntry);
            Globals.scenarioLib.talkTo(packedEntry);
        } else {
            RoleDialogSupport.showRoleKeyDialog(String.format(Locale.ROOT, NPC_REJECT_SCRIPT, entryId, scenarioChapter));
        }
        addEntrySound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
    }

    /**
     * Native: BasicInnDialogVisualObject::dismissSelectedHiredInnEntry @0049B989.
     * Fully ported.
     */
    public void dismissSelectedHiredInnEntry() {
        CUnit selectedEntry = availableInnEntries.get(innEntrySelectionIndex);
        int entryIdIndex = findInnEntryIdIndex(selectedEntry);
        int packedEntry = innEntryIds.get(entryIdIndex) & ~INN_ENTRY_HIRED_FLAG;
        innEntryIds.set(entryIdIndex, packedEntry);
        Globals.scenarioLib.talkTo(packedEntry);
        rejectAddEntrySound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
    }

    /**
     * Native: BasicInnDialogVisualObject::handleReservedEntry @0049BA28.
     * Fully ported.
     */
    public void handleReservedEntry() {
        int reservedIndex = innEntrySelectionIndex - availableInnEntries.size();
        CUnit reservedEntry = reservedInnEntries.get(reservedIndex);
        int entryIdIndex = findInnEntryIdIndex(reservedEntry);
        int packedEntry = innEntryIds.get(entryIdIndex);
        int entryId = packedEntry & 0xFFFF;
        int actionId = (packedEntry >>> 16) & 0x0FFF;
        RoleDialogSupport.showRoleKeyDialog(String.format(Locale.ROOT, NPC_TALK_SCRIPT, entryId, actionId));
        Globals.scenarioLib.talkTo(packedEntry);
        talkSound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
    }

    /**
     * Native: BasicInnDialogVisualObject::SelectPreviousInnEntry @0049B77C.
     * Fully ported.
     */
    private void selectPreviousInnEntry() {
        mapVisualObject.onMessage(MessageCodes.REFRESH_LAYOUT, 0, 0);
        if (selectedPartyIndex == 0) {
            selectedPartyIndex = selectedInnEntries.size() - 1;
        } else {
            selectedPartyIndex -= 1;
        }
        activateCurrentPartyEntry();
    }

    /**
     * Native: BasicInnDialogVisualObject::SelectNextInnEntry @0049B6E5.
     * Fully ported.
     */
    private void selectNextInnEntry() {
        mapVisualObject.onMessage(MessageCodes.REFRESH_LAYOUT, 0, 0);
        selectedPartyIndex += 1;
        if (selectedPartyIndex >= selectedInnEntries.size()) {
            selectedPartyIndex = 0;
        }
        activateCurrentPartyEntry();
    }

    /**
     * Native support extracted from BasicInnDialogVisualObject::SelectPreviousInnEntry @0049B77C and SelectNextInnEntry @0049B6E5.
     * Fully ported.
     */
    private void activateCurrentPartyEntry() {
        CUnit unit = selectedInnEntries.get(selectedPartyIndex);
        unit.setSelected(true);
        mapVisualObject.updateSelectionState();
    }

    /**
     * Native support extracted from the selection-info panel reparenting block in BasicInnDialogVisualObject::EnterInn @0049ABFE,
     * DruidInnDialogVisualObject::EnterInn @0049C359, and KaargInnDialogVisualObject::EnterInn @0049E451.
     */
    protected void moveSelectionInfoPanelIntoInn() {
        mapVisualObject = Globals.mainWindow.pMapVisualObject;
        selectionInfoPanelVisualObject = Globals.mainWindow.pSelectionInfoPanelVisualObject;
        CVisualObject selectionInfoPanel = selectionInfoPanelVisualObject;
        Globals.mainWindow.pRightPanelContainerVisualObject.removeChild(selectionInfoPanel);
        CRect selectionInfoRect = selectionInfoPanel.getRect();
        selectionInfoRect.offset(0x280 - selectionInfoRect.width(), 0);
        selectionInfoPanel.setBounds(selectionInfoRect);
        addChild(selectionInfoPanel);
    }

    /**
     * Native support extracted from the selection-info panel restore block in BasicInnDialogVisualObject::LeaveInn @0049B41B.
     */
    protected void restoreSelectionInfoPanelFromInn() {
        CVisualObject selectionInfoPanel = selectionInfoPanelVisualObject;
        CRect selectionInfoRect = selectionInfoPanel.getRect();
        selectionInfoRect.offset(selectionInfoRect.width() - 0x280, 0);
        selectionInfoPanel.setBounds(selectionInfoRect);
        removeChild(selectionInfoPanel);
        Globals.mainWindow.pRightPanelContainerVisualObject.addChild(selectionInfoPanel);
    }

    /**
     * Native support extracted from the `g_GamePreferences.TipsMode` / `sessionMode` branch in BasicInnDialogVisualObject::EnterInn @0049ABFE.
     */
    protected boolean shouldShowInnTipsPrompt() {
        return Globals.gamePreferences.tipsMode != 0 && Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN;
    }

    /**
     * Native support extracted from tips-prompt allocation in BasicInnDialogVisualObject::EnterInn @0049ABFE,
     * DruidInnDialogVisualObject::EnterInn @0049C359, and KaargInnDialogVisualObject::EnterInn @0049E451.
     */
    protected void ensureTipsPromptChild() {
        TipsPromptDialogVisualObject tipsPrompt = new TipsPromptDialogVisualObject(
                0x467,
                0,
                0,
                0x138,
                200,
                ScriptDataSupport.getTipText(2)
        );
        tipsPromptChild = tipsPrompt;
        sceneGridVisual.addChild(tipsPromptChild);
    }

    /**
     * Native child-removal branch shared by BasicInnDialogVisualObject::OnMessage @0049A931 and EnterInn/LeaveInn.
     * Partial port. Java removes and detaches the child while leaving native destructor semantics to GC.
     */
    protected void clearTipsPromptChild() {
        if (tipsPromptChild == null) {
            return;
        }
        sceneGridVisual.removeChild(tipsPromptChild);
        tipsPromptChild.detachFromParent();
        tipsPromptChild = null;
    }

    /**
     * Native support extracted from `ScenarioEnterInn` + raw entry-list capture in BasicInnDialogVisualObject::EnterInn @0049ABFE.
     */
    protected void populateInnEntries() {
        if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN) {
            innEntryIds.clear();
            innEntryIds.addAll(Globals.scenarioLib.enterInn());
        }
    }

    /**
     * Native support extracted from BasicInnDialogVisualObject::EnterInn @0049AF03-0049B01F.
     */
    protected void materializeInnEntries() {
        availableInnEntries.clear();
        reservedInnEntries.clear();
        for (Integer entryId : innEntryIds) {
            int packedEntry = entryId;
            int serverId = packedEntry & 0xFFFF;
            int entryGroup = (packedEntry >>> 0x1C) & 0x7;
            CUnit unit = mapVisualObject.getOrCreateCUnit(serverId);
            if (entryGroup == 1 || entryGroup == 2) {
                availableInnEntries.add(unit);
            } else {
                reservedInnEntries.add(unit);
            }
        }
        innEntrySelectionIndex = availableInnEntries.isEmpty() && reservedInnEntries.isEmpty() ? -1 : 0;
    }

    /**
     * Native support extracted from selected-party list copy and activation in BasicInnDialogVisualObject::EnterInn @0049B01F-0049B100.
     */
    protected void populateSelectedPartyEntries() {
        SelectedUnitsSnapshot selectedUnitsSnapshot = SelectedUnitsSnapshot.GLOBAL;
        selectedUnitsSnapshot.rebuildFromCurrentPlayerUnits(mapVisualObject);
        selectedInnEntries.clear();
        selectedInnEntries.addAll(selectedUnitsSnapshot.getPrimaryUnits());
        selectedPartyIndex = selectedUnitsSnapshot.findFlag20PrimaryUnitIndex();
        mapVisualObject.onMessage(MessageCodes.REFRESH_LAYOUT, 0, 0);
        activateCurrentPartyEntry();
        CUnit selectedUnit = selectedInnEntries.get(selectedPartyIndex);
        selectedUnit.unitFlags |= 0x08;
    }

    /**
     * Native support boundary for quests-storage reset in BasicInnDialogVisualObject::LeaveInn @0049B41B.
     */
    protected void clearQuestsStorage() {
        questsStorage.removeAndDeleteQuestsForOwner(0);
    }

    /**
     * Native support extracted from `AfxAllocMemory(0x84)` + `QuestsStorage::New` in
     * BasicInnDialogVisualObject::InitializeInnChildren @0049A65E,
     * DruidInnDialogVisualObject::InitializeInnChildren @0049BDCA, and
     * KaargInnDialogVisualObject::InitializeInnChildren @0049E1BC.
     */
    protected QuestsStorage createQuestsStorage() {
        return new QuestsStorage();
    }

    /**
     * Native support extracted from the session-mode gate around ScenarioLeaveInn in BasicInnDialogVisualObject::LeaveInn @0049B41B.
     */
    protected void leaveScenarioInnIfCampaign() {
        if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN) {
            Globals.scenarioLib.leaveInn();
        }
    }

    /**
     * Native support bridge: HandlerVisualObject::ShowDialog @004DC232 from menu-44C child overrides.
     * not ported.
     */
    protected void showBaseDialog() {
        super.showDialog();
    }

    /**
     * Native support bridge: HandlerVisualObject::HideDialog @004DC26D from menu-44C child overrides.
     * not ported.
     */
    protected void hideBaseDialog(MessageCodes reason) {
        super.hideDialog(reason);
    }

    /**
     * Native support: Sound::PlayLoopPointer @004A0FF0.
     * Fully ported.
     */
    protected void primeAmbientLoopSound(Sound sound) {
        if (sound != null) {
            sound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, true, Sound.POINTER_SFX_PRIORITY, 0);
        }
    }

    /**
     * Native support extracted from BasicInnDialogVisualObject::OnMouseMove @0049AB10.
     * Fully ported.
     */
    private boolean isPointInsideChild(CVisualObject child, int x, int y) {
        CRect screenRect = new CRect();
        child.clientToScreen(screenRect, child.cRect);
        return screenRect.contains(x, y);
    }

    /**
     * Native support thunk: FUN_004384F0 @004384F0.
     * not ported.
     */
    protected static Sound loadSound(String resourcePath) {
        if (resourcePath == null) {
            return null;
        }
        return new Sound(resourcePath);
    }

    /**
     * Native support thunk: FUN_00438480 @00438480.
     * not ported.
     */
    protected static Sound releaseSound(Sound sound) {
        if (sound != null) {
            SoundSystem.get().releaseSound(sound);
        }
        return null;
    }

    /**
     * Java helper for BasicInnDialogVisualObject inn-scene frame loading in EnterInn @0049ABFE.
     * not ported.
     */
    protected static void reloadFrameSequence(
            CBmp64kFrameSequence sequence,
            String template,
            int startInclusive,
            int endExclusive
    ) {
        clearFrameSequence(sequence);
        List<String> frameNames = new ArrayList<>();
        for (int frameIndex = startInclusive; frameIndex < endExclusive; frameIndex++) {
            String resourcePath = String.format(template, frameIndex);
            frameNames.add(resourcePath);
        }
        sequence.loadFrames(frameNames);
    }

    /**
     * Java helper for BasicInnDialogVisualObject inn-scene frame teardown in LeaveInn @0049B41B.
     * not ported.
     */
    protected static void clearFrameSequence(CBmp64kFrameSequence sequence) {
        sequence.releaseFrames();
    }
}
