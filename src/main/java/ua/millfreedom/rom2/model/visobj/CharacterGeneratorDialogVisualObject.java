package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CGameSession;
import ua.millfreedom.rom2.model.CMousePointer;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.ScriptDataSupport;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.sound.Sound;
import ua.millfreedom.rom2.model.sound.SoundSystem;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.res.ResInHeap;

import java.util.ArrayList;
import java.util.List;

import static ua.millfreedom.rom2.model.enums.MessageCodes.DIALOG_OK;
import static ua.millfreedom.rom2.model.enums.MessageCodes.CLEAR_TIP_PROMPT;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RENDER_FRAME;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RETURN_TO_GAME;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SELECT_NEXT_HERO;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SELECT_PREVIOUS_HERO;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;

/**
 * Native class: CharacterGeneratorDialogVisualObject (vtbl @0x005CB6A8).
 * Purpose: character-generator dialog (`id=0x456`) hosting stats, summary strip, action buttons, portrait controls, and skill selection.
 */
public class CharacterGeneratorDialogVisualObject extends HandlerVisualObject {
    public static final int NATIVE_SIZE = 0x108; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int RESET_ALLOCATED_POINTS = 100;
    private static final int RESET_STAT_VALUE = 0x19;
    private static final int TIP_FIGHTER_ALIGNMENT_5 = 5;
    private static final int TIP_MAGE_ALIGNMENT_6 = 6;
    private static final int TIP_ADJUST_STATS_7 = 7;
    private static final int UNIT_FLAG_SETUP_CUSTOMIZED = 0x08;
    private static final String SFX_STAT_ADJUST_WAV = "SFX/ChrGen/+_-.wav";
    private static final String SFX_CLICK_OK_WAV = "SFX/Click_Ok.wav";
    private static final String SFX_RESET_WAV = "SFX/Sbros.wav";
    private static final String SFX_BACK_WAV = "SFX/Back.wav";
    public static final String SCENARIO_NPC_REG = "scenario/npc.reg";

    //0x68
    public SelectionInfoPanelVisualObject transferredSelectionInfoPanel;
    //0x6c
    public MapVisualObject mapVisual;
    //0x70
    public StatsAllocationPanelVisualObject statsAllocationPanel;
    //0x74
    public FullStatsPanelVisualObject fullStatsPanel;
    //0x78
    public ActionButtonsPanelVisualObject actionButtonsPanel;
    //0x7c
    public SkillSelectionPanelVisualObject skillSelectionPanel;
    //0x80
    public TipsPromptDialogVisualObject tipsPrompt;
    //0x84
    public Sound statAdjustSound;
    //0x88
    public Sound okButtonSound;
    //0x8c
    public Sound resetButtonSound;
    //0x90
    public Sound backButtonSound;
    //0x94
    public CUnit currentCharacterProfile;
    //0x98
    public int forwardLButtonUpToStatsAllocationPanel;
    //0x9c
    public int field0x9c;
    //0xa0
    public final List<Short> facesMm = new ArrayList<>();
    //0xb4
    public final List<Short> facesMf = new ArrayList<>();
    //0xc8
    public final List<Short> facesFm = new ArrayList<>();
    //0xdc
    public final List<Short> facesFf = new ArrayList<>();
    //0xf0
    public int magePortraitFlag;
    //0xf4
    public int femalePortraitFlag;
    //0xf8
    public int selectedPortraitId;
    //0xfc
    public int selectedPortraitListIndex;
    //0x100
    public int tipsRefreshStep;
    //0x104
    public int dialogActiveFlag;

    /**
     * Native: CharacterGeneratorDialogVisualObject::CharacterGeneratorDialogVisualObject @0042D39D.
     * Fully ported.
     */
    public CharacterGeneratorDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        initialize();
    }

    /**
     * vtbl +0x2C: CharacterGeneratorDialogVisualObject::Update @0042E1ED.
     * Fully ported. Native resets any non-default/non-dice cursor before HandlerVisualObject::Update.
     */
    @Override
    public void update() {
        if (!isCurrentCursorDefaultOrDice()) {
            CMousePointer.Cursor_Default.setToMousePointer();
        }
        super.update();
    }

    /**
     * vtbl +0x30: CharacterGeneratorDialogVisualObject::RenderSelf @0042E243.
     * Fully ported.
     */
    @Override
    public void renderSelf(CRect clipRect) {
        // Native no-op.
    }

    /**
     * vtbl +0x48: CharacterGeneratorDialogVisualObject::OnMessage @0042D71C.
     * Fully ported. Native dispatches render, portrait navigation, tip clear, and then forwards to HandlerVisualObject.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        if (msg == RENDER_FRAME) {
            draw();
        } else if (msg == SELECT_PREVIOUS_HERO) {
            selectPreviousPortrait();
        } else if (msg == SELECT_NEXT_HERO) {
            selectNextPortrait();
        } else if (msg == CLEAR_TIP_PROMPT) {
            clearTipsPrompt();
        }
        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * vtbl +0x4C: CharacterGeneratorDialogVisualObject::OnMouseMove @0042D85A.
     * Fully ported.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        if (!isPointInsideChild(actionButtonsPanel, x, y)) {
            actionButtonsPanel.resetButtonTracking();
        }
        return super.onMouseMove(nFlags, x, y);
    }

    /**
     * vtbl +0x58: CharacterGeneratorDialogVisualObject::OnLButtonUp @0042E0C5.
     * Fully ported.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        if (forwardLButtonUpToStatsAllocationPanel == 0) {
            return super.onLButtonUp(nFlags, x, y);
        }
        statsAllocationPanel.onLButtonUp(nFlags, x, y);
        return 0;
    }

    /**
     * vtbl +0x6C: CharacterGeneratorDialogVisualObject::OnKeyDown @0042E113.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        if (nChar == 0x0D) {
            acceptCharacterGenerator();
            return 1;
        }
        return super.onKeyDown(nChar);
    }

    /**
     * vtbl +0x78: CharacterGeneratorDialogVisualObject::InitializeCharacterGeneratorDialog @0042D4B0.
     * Fully ported.
     */
    @Override
    public void initialize() {
        statAdjustSound = null;
        okButtonSound = null;
        resetButtonSound = null;
        backButtonSound = null;
        tipsPrompt = null;
        transferredSelectionInfoPanel = null;
        mapVisual = null;

        statsAllocationPanel = new StatsAllocationPanelVisualObject(0x457, 0, 0, 0xA0, 0xEE, this);
        fullStatsPanel = new FullStatsPanelVisualObject(0x458, 0, 0xEE, 0xA0, 0xF2, this);
        actionButtonsPanel = new ActionButtonsPanelVisualObject(0x459, 0x1E0, 0, 0x280, 0xEE, this);
        skillSelectionPanel = new SkillSelectionPanelVisualObject(0x45A, 0xA0, 0, 0x1E0, 0x1E0, this);
        addChild(statsAllocationPanel);
        addChild(fullStatsPanel);
        addChild(actionButtonsPanel);
        addChild(skillSelectionPanel);

        forwardLButtonUpToStatsAllocationPanel = 0;
        field0x9c = -1;
        magePortraitFlag = 0;
        femalePortraitFlag = 0;
        selectedPortraitId = 0;
        dialogActiveFlag = 0;
    }

    /**
     * vtbl +0x80: CharacterGeneratorDialogVisualObject::ShowDialog @0042D8DC.
     * Fully ported. Native rehosts the selection-info panel, refreshes generator resources, clears the screen, updates the
     * render region, shows the handler dialog, and restores the default cursor.
     */
    @Override
    public void showDialog() {
        Globals.mousePointer.disableBackgroundCapture();
        transferredSelectionInfoPanel = Globals.mainWindow.pSelectionInfoPanelVisualObject;
        mapVisual = Globals.mainWindow.getMapVisual();
        currentCharacterProfile = getCurrentCharacterProfile();

        statsAllocationPanel.loadCurrentCharacterProfileStats();
        selectedPortraitId = resolveCurrentProfilePortraitId() - 1;
        int currentProfileFlags = resolveCurrentProfileFlags();
        magePortraitFlag = (currentProfileFlags & 0x2) << 5;
        femalePortraitFlag = (currentProfileFlags & 0x4) << 6;

        moveSelectionInfoPanelIntoDialog();
        if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_MULTIPLAYER_CLIENT) {
            loadCharacterPortraitFaceSets();
        }

        if (isTipsModeEnabled()) {
            ensureTipsPrompt(resolveTownTipText(
                    (currentProfileFlags & 0x2) == 0 ? TIP_FIGHTER_ALIGNMENT_5 : TIP_MAGE_ALIGNMENT_6
            ));
        } else {
            clearTipsPrompt();
        }

        tipsRefreshStep = 0;
        forwardLButtonUpToStatsAllocationPanel = 0;
        field0x9c = -1;

        statsAllocationPanel.loadCharacterGeneratorStatsResources();
        fullStatsPanel.initializeFullStatsPanel();
        actionButtonsPanel.loadCharacterGeneratorButtonArt();
        skillSelectionPanel.loadSkillSelectionGraphics((currentProfileFlags & 0x2) != 0);
        skillSelectionPanel.loadSkillSelectionSounds((currentProfileFlags & 0x2) != 0);

        loadCharacterGeneratorSounds();
        statsAllocationPanel.freePoolPoints = 0;
        statsAllocationPanel.refreshButtonGraphics(0, 0, 0);
        clearSkillSelectionState();
        skillSelectionPanel.selectedOptionIndex = resolveSelectedSetupIndex1Based() - 1;
        skillSelectionPanel.optionStateFlags[skillSelectionPanel.selectedOptionIndex] = 1;

        selectedPortraitListIndex = 0;
        dialogActiveFlag = 1;
        clearScreen();
        super.showDialog();
        CMousePointer.Cursor_Default.setToMousePointer();
        Globals.mousePointer.enableBackgroundCapture();
    }

    /**
     * vtbl +0x84: CharacterGeneratorDialogVisualObject::HideDialog @0042DE80.
     * Fully ported. Native redraws, restores the rehosted panel, releases child art/sounds, clears the selected profile, and hides the handler dialog.
     */
    @Override
    public HandlerVisualObject hideDialog(MessageCodes reason) {
        draw();
        dialogActiveFlag = 0;
        restoreSelectionInfoPanelToSpellbook();
        clearTipsPrompt();
        statsAllocationPanel.releaseCharacterGeneratorStatsResources();
        fullStatsPanel.releaseFullStatsBitmap();
        actionButtonsPanel.releaseCharacterGeneratorButtonArt();
        skillSelectionPanel.releaseSkillSelectionGraphics();
        skillSelectionPanel.releaseSkillSelectionSounds();
        releaseCharacterGeneratorSounds();
        currentCharacterProfile = null;
        return super.hideDialog(reason);
    }

    /**
     * Native support extracted from StatsAllocationPanelVisualObject::IncreaseStatValue @00429E3E,
     * StatsAllocationPanelVisualObject::DecreaseStatValue @00429F41, and
     * CharacterGeneratorDialogVisualObject::ResetCharacterGeneratorStats @0042DFEE.
     */
    public int getSelectedSetupIndex() {
        return skillSelectionPanel.selectedOptionIndex;
    }

    /**
     * Native owner: GameSession stat writeback shared by StatsAllocationPanelVisualObject::Increase/DecreaseStatValue @00429E3E / @00429F41 and SkillSelectionPanelVisualObject::OnLButtonDown @0042B736.
     */
    public void onStatsAllocationChanged(
            int bodyValue,
            int agilityValue,
            int mindValue,
            int spiritValue,
            int selectedSetupIndex1Based
    ) {
        resolveGameSession().applyCharacterGeneratorBuild(
                bodyValue,
                agilityValue,
                mindValue,
                spiritValue,
                selectedSetupIndex1Based
        );
        currentCharacterProfile = getCurrentCharacterProfile();
        mapVisual.updateSelectionState();
        refreshSelectionInfoPanelPreview();
    }

    /**
     * Native owner: setup dialog sound refresh from StatsAllocationPanelVisualObject::Increase/DecreaseStatValue @00429E3E / @00429F41.
     * Fully ported. Native stops any current stat-adjust channel and then routes through Sound::PlayPointer.
     */
    public void refreshSetupAudio() {
        replayPointerSound(statAdjustSound);
    }

    /**
     * Native owner: delegated setup-strip stats render from FullStatsPanelVisualObject::Update @0042A2B0
     * through CharacterGeneratorDialogVisualObject.currentCharacterProfile +0x94.
     */
    public void renderFullStatsPanel(CRect screenRect) {
        currentCharacterProfile.renderFullStatsInfo(screenRect);
    }

    /**
     * Native owner: delegated setup-strip tooltip lookup from FullStatsPanelVisualObject::GetText @0042A34B
     * through CharacterGeneratorDialogVisualObject.currentCharacterProfile +0x94.
     */
    public String getFullStatsPanelTooltipText(int localX, int localY) {
        return currentCharacterProfile.getFullStatsTooltipText(localX, localY);
    }

    /**
     * Native owner: button sound fan-out from ActionButtonsPanelVisualObject::OnLButtonDown @0042A9C9 through dialog fields `+0x88`, `+0x8C`, and `+0x90`.
     */
    public void playActionButtonSound(int buttonIndex) {
        switch (buttonIndex) {
            case 0 -> playPointerSound(okButtonSound);
            case 1 -> playPointerSound(resetButtonSound);
            case 2 -> playPointerSound(backButtonSound);
            default -> throw new IndexOutOfBoundsException("buttonIndex: " + buttonIndex);
        }
    }

    /**
     * Native owner: Accept/Reset/Back activation from ActionButtonsPanelVisualObject::OnLButtonUp @0042AA58.
     */
    public void activateActionButton(int buttonIndex) {
        switch (buttonIndex) {
            case 0 -> acceptCharacterGenerator();
            case 1 -> resetCharacterGeneratorStats();
            case 2 -> returnToGame();
            default -> throw new IndexOutOfBoundsException("buttonIndex: " + buttonIndex);
        }
    }

    /**
     * Native: CharacterGeneratorDialogVisualObject::RefreshSkillSelectionTips @0042E14B.
     * Fully ported. Native advances the prompt text once when tips mode is enabled and a prompt is present.
     */
    public void refreshSkillSelectionTips() {
        if (!isTipsModeEnabled()
                || tipsRefreshStep != 0
                || tipsPrompt == null) {
            return;
        }
        tipsPrompt.messageText.setText(resolveTownTipText(TIP_ADJUST_STATS_7));
        tipsRefreshStep += 1;
    }

    /**
     * Native: CharacterGeneratorDialogVisualObject::SelectNextPortrait @0042E2A4.
     * Fully ported. Native advances through the active face array and writes the selected profile/session face.
     */
    private void selectNextPortrait() {
        if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN) {
            return;
        }
        List<Short> activeFaceSet = getActivePortraitFaceSet();
        selectedPortraitListIndex =
                (selectedPortraitListIndex + 1) % activeFaceSet.size();
        selectedPortraitId =
                Short.toUnsignedInt(activeFaceSet.get(selectedPortraitListIndex));
        writeCurrentPortraitSelection(selectedPortraitId);
    }

    /**
     * Native: CharacterGeneratorDialogVisualObject::SelectPreviousPortrait @0042E485.
     * Fully ported. Native walks backward through the active face array and writes the selected profile/session face.
     */
    private void selectPreviousPortrait() {
        if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN) {
            return;
        }
        List<Short> activeFaceSet = getActivePortraitFaceSet();
        selectedPortraitListIndex =
                (activeFaceSet.size() - 1 + selectedPortraitListIndex) % activeFaceSet.size();
        selectedPortraitId =
                Short.toUnsignedInt(activeFaceSet.get(selectedPortraitListIndex));
        writeCurrentPortraitSelection(selectedPortraitId);
    }

    /**
     * Native: CharacterGeneratorDialogVisualObject::AcceptCharacterGenerator @0042E250.
     * Fully ported.
     */
    private void acceptCharacterGenerator() {
        onMessage(DIALOG_OK, 0, 0);
    }

    /**
     * Native: CharacterGeneratorDialogVisualObject::ReturnToGame @0042E26F.
     * Fully ported.
     */
    private void returnToGame() {
        onMessage(RETURN_TO_GAME, 0, 0);
    }

    /**
     * Native: CharacterGeneratorDialogVisualObject::ResetCharacterGeneratorStats @0042DFEE.
     * Fully ported. Native resets local stat/free-pool values, reapplies the current setup slot, and refreshes button art.
     */
    private void resetCharacterGeneratorStats() {
        statsAllocationPanel.freePoolPoints = RESET_ALLOCATED_POINTS;
        statsAllocationPanel.bodyValue = RESET_STAT_VALUE;
        statsAllocationPanel.agilityValue = RESET_STAT_VALUE;
        statsAllocationPanel.mindValue = RESET_STAT_VALUE;
        statsAllocationPanel.spiritValue = RESET_STAT_VALUE;
        resolveGameSession().applyCharacterGeneratorBuild(
                statsAllocationPanel.bodyValue,
                statsAllocationPanel.agilityValue,
                statsAllocationPanel.mindValue,
                statsAllocationPanel.spiritValue,
                getSelectedSetupIndex() + 1
        );
        statsAllocationPanel.refreshButtonGraphics(0, 0, 0);
    }

    /**
     * Native: CharacterGeneratorDialogVisualObject::LoadCharacterGeneratorSounds @0042E676.
     * Fully ported.
     */
    private void loadCharacterGeneratorSounds() {
        releaseCharacterGeneratorSounds();
        statAdjustSound = loadCharacterGeneratorStatAdjustSound();
        okButtonSound = new Sound(SFX_CLICK_OK_WAV);
        resetButtonSound = new Sound(SFX_RESET_WAV);
        backButtonSound = new Sound(SFX_BACK_WAV);
    }

    /**
     * Native: CharacterGeneratorDialogVisualObject::ReleaseCharacterGeneratorSounds @0042E6E3.
     * Fully ported. Java clears retained sound references instead of emulating native delete semantics.
     */
    private void releaseCharacterGeneratorSounds() {

        statAdjustSound = null;

        okButtonSound = null;

        resetButtonSound = null;

        backButtonSound = null;
    }

    /**
     * Native: CharacterGeneratorDialogVisualObject::GetCurrentCharacterProfile @0042E28E.
     * Fully ported.
     */
    private CUnit getCurrentCharacterProfile() {
        return mapVisual.getSelectedCUnit();
    }

    /**
     * Native support extracted from CharacterGeneratorDialogVisualObject::ShowDialog @0042D8DC.
     */
    private int resolveCurrentProfilePortraitId() {
        return currentCharacterProfile.field8_0x28;
    }

    /**
     * Native support extracted from CharacterGeneratorDialogVisualObject::ShowDialog @0042D8DC.
     */
    private int resolveCurrentProfileFlags() {
        return currentCharacterProfile.unitFlags;
    }

    /**
     * Native support extracted from CharacterGeneratorDialogVisualObject::ShowDialog @0042D8DC.
     * Native reads `m_GameSession.startingSkillIndex`.
     */
    private int resolveSelectedSetupIndex1Based() {
        return resolveGameSession().startingSkillIndex;
    }

    /**
     * Native support extracted from CharacterGeneratorDialogVisualObject portrait cycling @0042E2A4 / @0042E485.
     */
    private void writeCurrentPortraitSelection(int portraitId) {
        currentCharacterProfile.field8_0x28 = portraitId;
        currentCharacterProfile.unitFlags |= UNIT_FLAG_SETUP_CUSTOMIZED;
        resolveGameSession().face = portraitId;
    }

    /**
     * Native support extracted from CharacterGeneratorDialogVisualObject::ShowDialog @0042D8DC.
     */
    private static CGameSession resolveGameSession() {
        return Globals.mainWindow.m_GameSession;
    }

    /**
     * Native support extracted from `g_GamePreferences.TipsMode` reads in CharacterGeneratorDialogVisualObject::ShowDialog @0042D8DC
     * and CharacterGeneratorDialogVisualObject::RefreshSkillSelectionTips @0042E14B.
     */
    private boolean isTipsModeEnabled() {
        return Globals.gamePreferences.tipsMode != 0;
    }

    /**
     * Native support for prompt-text loading through FUN_004DDC9C in CharacterGeneratorDialogVisualObject::ShowDialog @0042D8DC
     * / RefreshSkillSelectionTips @0042E14B.
     */
    private String resolveTownTipText(int tipIndex) {
        return ScriptDataSupport.getTipText(tipIndex);
    }

    /**
     * Native child-removal tail shared by CharacterGeneratorDialogVisualObject::OnMessage @0042D71C and ::HideDialog @0042DE80.
     */
    private void clearTipsPrompt() {
        if (tipsPrompt == null) {
            return;
        }
        skillSelectionPanel.removeChild(tipsPrompt);
        tipsPrompt.detachFromParentSlot(1);
        tipsPrompt = null;
    }

    /**
     * Native support boundary for tips-prompt allocation in CharacterGeneratorDialogVisualObject::ShowDialog @0042D8DC.
     * Java preserves the recovered constructor parameters, parent attachment, and prompt text assignment.
     */
    private void ensureTipsPrompt(String promptText) {
        clearTipsPrompt();
        tipsPrompt = new TipsPromptDialogVisualObject(0x467, 0, 0x118, 0x138, 0x1E0);
        tipsPrompt.messageText.setText(promptText);
        skillSelectionPanel.addChild(tipsPrompt);
    }

    /**
     * Native support extracted from CharacterGeneratorDialogVisualObject::ShowDialog @0042D8DC and
     * SelectionInfoPanelVisualObject::OnMessage @004AE89E.
     */
    private void refreshSelectionInfoPanelPreview() {
        transferredSelectionInfoPanel.onMessage(MessageCodes.SET_MAP_CONTEXT, mapVisual, 0);
        transferredSelectionInfoPanel.selectionInfoViewMode0x70 = 1;
    }

    /**
     * Native support boundary for the selection-info panel detach/rehost block in CharacterGeneratorDialogVisualObject::ShowDialog @0042D8DC.
     * Fully ported.
     */
    private void moveSelectionInfoPanelIntoDialog() {
        CVisualObject rightPanelContainer = Globals.mainWindow.getRightPanelContainerVisual();
        rightPanelContainer.removeChild(transferredSelectionInfoPanel);
        CRect panelRect = transferredSelectionInfoPanel.getRect();
        panelRect.offset(0x280 - panelRect.width(), 0);
        transferredSelectionInfoPanel.setBounds(panelRect);
        addChild(transferredSelectionInfoPanel);
    }

    /**
     * Native support boundary for the selection-info panel restore block in CharacterGeneratorDialogVisualObject::HideDialog @0042DE80.
     * Fully ported.
     */
    private void restoreSelectionInfoPanelToSpellbook() {
        CVisualObject rightPanelContainer = Globals.mainWindow.getRightPanelContainerVisual();
        CRect panelRect = transferredSelectionInfoPanel.getRect();
        panelRect.offset(panelRect.width() - 0x280, 0);
        transferredSelectionInfoPanel.setBounds(panelRect);
        removeChild(transferredSelectionInfoPanel);
        rightPanelContainer.addChild(transferredSelectionInfoPanel);
        transferredSelectionInfoPanel = null;
        mapVisual = null;
    }

    /**
     * not ported. Java ownership-boundary layout restore for the shared right-panel selection-info panel.
     */
    private static void applyRightPanelLayoutAfterDialog() {
        if (GAMEPLAY.isSetIn(Globals.mainWindow.dialogsMask)) {
            Globals.mainWindow.applyGameplayRightPanelLayout();
        } else {
            Globals.mainWindow.applyDefaultRightPanelLayout();
        }
    }

    /**
     * Native support boundary for `scenario_npc_reg` face-array loads in CharacterGeneratorDialogVisualObject::ShowDialog @0042D8DC.
     * Java preserves the four recovered array names and clears/reloads them via `ResInHeap`.
     */
    private void loadCharacterPortraitFaceSets() {
        clearPortraitFaceSets();
        try {
            ResInHeap res = ResInHeap.load(SCENARIO_NPC_REG);
            res.getShortArray("Multiplayer", "FacesMM", facesMm);
            res.getShortArray("Multiplayer", "FacesMF", facesMf);
            res.getShortArray("Multiplayer", "FacesFM", facesFm);
            res.getShortArray("Multiplayer", "FacesFF", facesFf);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load character-generator portrait face sets", ex);
        }
    }

    /**
     * Native support extracted from CharacterGeneratorDialogVisualObject::SelectPreviousPortrait @0042E485 / SelectNextPortrait @0042E2A4.
     */
    private List<Short> getActivePortraitFaceSet() {
        if (magePortraitFlag == 0) {
            return femalePortraitFlag == 0 ? facesMf : facesFf;
        }
        return femalePortraitFlag == 0 ? facesMm : facesFm;
    }

    /**
     * Java helper extracted from CharacterGeneratorDialogVisualObject::ShowDialog @0042D8DC.
     * Resets the skill-selection state array before applying `m_GameSession.gap_5C - 1`.
     */
    private void clearSkillSelectionState() {
        for (int i = 0; i < skillSelectionPanel.optionStateFlags.length; i++) {
            skillSelectionPanel.optionStateFlags[i] = 0;
        }
    }

    /**
     * Native support extracted from the four CWordArray::RemoveAll calls in CharacterGeneratorDialogVisualObject::ShowDialog @0042D8DC.
     */
    private void clearPortraitFaceSets() {
        facesMm.clear();
        facesMf.clear();
        facesFm.clear();
        facesFf.clear();
    }

    /**
     * Native support: `GetSourceBitmap == g_Cursor_Default || g_Cursor_Dice` test in CharacterGeneratorDialogVisualObject::Update @0042E1ED.
     * Fully ported.
     */
    private boolean isCurrentCursorDefaultOrDice() {
        return Globals.mousePointer.getSourceBitmap() == CMousePointer.Cursor_Default.getBitmap()
                || Globals.mousePointer.getSourceBitmap() == CMousePointer.Cursor_Dice.getBitmap();
    }

    /**
     * Native support extracted from CharacterGeneratorDialogVisualObject::LoadCharacterGeneratorSounds @0042E676.
     */
    private static Sound loadCharacterGeneratorStatAdjustSound() {
        return new Sound(SFX_STAT_ADJUST_WAV);
    }

    /**
     * Native support around Sound::PlayPointer @00438570 for single-slot call sites.
     */
    private static void playPointerSound(Sound sound) {
        if (sound != null) {
            sound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
        }
    }

    /**
     * Native support extracted from Sound::StopAndRewindPointerSound @004385B0 and Sound::PlayPointer @00438570
     * in character-generator stat allocation changes.
     */
    private static void replayPointerSound(Sound sound) {
        if (sound == null) {
            return;
        }
        SoundSystem.get().stopAndRewind(sound);
        playPointerSound(sound);
    }

    /**
     * Native support extracted from child hit testing in CharacterGeneratorDialogVisualObject::OnMouseMove @0042D85A.
     */
    private boolean isPointInsideChild(CVisualObject child, int x, int y) {
        CRect screenRect = new CRect();
        child.clientToScreen(screenRect, child.cRect);
        return screenRect.contains(x, y);
    }

}
