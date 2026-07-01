package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CMousePointer;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.CGameSession;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.sound.Sound;
import ua.millfreedom.rom2.model.sound.SoundSystem;
import ua.millfreedom.rom2.text.GameTexts;
import ua.millfreedom.rom2.text.PatchText;

import static ua.millfreedom.rom2.model.enums.MessageCodes.DIALOG_OK;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RENDER_FRAME;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RETURN_TO_GAME;

/**
 * Native class: CharacterLoaderDialogVisualObject (vtbl @0x005CB898).
 * Purpose: character roster/loader dialog (`id=0x460`) for selecting, renaming, deleting, or creating characters before dialog `0x466`.
 */
public class CharacterLoaderDialogVisualObject extends HandlerVisualObject {
    public static final int NATIVE_SIZE = 0x9C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int ACCEPT_BUTTON_INDEX = 0;
    private static final int DELETE_BUTTON_INDEX = 1;
    private static final int CANCEL_BUTTON_INDEX = 3;
    private static final String SFX_CLICK_OK_WAV = "SFX/Click_Ok.wav";
    private static final String SFX_RENAME_WAV = "SFX/Rename.wav";
    private static final String SFX_DELETE_WAV = "SFX/Delete.wav";
    private static final String SFX_UNDO_WAV = "SFX/Undo.wav";

    //0x68
    public CGameSession gameSession;

    //0x6c
    public SelectionInfoPanelVisualObject selectionInfoPanel;

    //0x70
    public MapVisualObject mapVisual;

    //0x74
    public CharacterLoaderDetailsPanelVisualObject detailsPanel;

    //0x78
    public CharacterLoaderActionButtonsPanelVisualObject actionButtonsPanel;

    //0x7c
    public CharacterLoaderRosterListPanelVisualObject rosterListPanel;

    //0x80
    public StaticTextVisualObject renameEntryLabel;

    //0x84
    public CUnit selectedUnit;

    //0x88
    public Sound okButtonSound;

    //0x8c
    public Sound renameButtonSound;

    //0x90
    public Sound deleteButtonSound;

    //0x94
    public Sound cancelButtonSound;

    //0x98
    public int dialogActiveFlag;

    /**
     * Native: CharacterLoaderDialogVisualObject::CharacterLoaderDialogVisualObject @00431565.
     * Fully ported.
     */
    public CharacterLoaderDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        initialize();
    }

    /**
     * vtbl +0x30: CharacterLoaderDialogVisualObject::RenderSelf @00438400.
     * Fully ported.
     */
    @Override
    public void renderSelf(CRect clipRect) {
        // Native no-op.
    }

    /**
     * vtbl +0x48: CharacterLoaderDialogVisualObject::OnMessage @00431D5B.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        if (msg == RENDER_FRAME) {
            draw();
            return 1;
        }
        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * vtbl +0x4C: CharacterLoaderDialogVisualObject::OnMouseMove @00431DA1.
     * Fully ported.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        if (!containsScreenPoint(actionButtonsPanel, x, y)) {
            actionButtonsPanel.resetButtonTracking();
        }
        if (containsScreenPoint(detailsPanel, x, y)) {
            CMousePointer.Cursor_Default.setToMousePointer();
        }
        return super.onMouseMove(nFlags, x, y);
    }

    /**
     * vtbl +0x78: CharacterLoaderDialogVisualObject::InitializeCharacterLoaderDialog @00431698.
     * Fully ported.
     */
    @Override
    public void initialize() {
        selectionInfoPanel = null;
        mapVisual = null;
        okButtonSound = null;
        renameButtonSound = null;
        deleteButtonSound = null;
        cancelButtonSound = null;
        selectedUnit = null;

        detailsPanel = new CharacterLoaderDetailsPanelVisualObject(0x462, 0, 0, 0xA0, 0x1E0, this);
        actionButtonsPanel = new CharacterLoaderActionButtonsPanelVisualObject(0x461, 0x1E0, 0, 0x280, 0xEE, this);
        rosterListPanel = new CharacterLoaderRosterListPanelVisualObject(0x463, 0xA0, 0, 0x1E0, 0x1E0, this);

        CRect renameRect = new CRect();
        renameRect.set(rosterListPanel.entryRowTemplateRect);
        CRect rosterRect = rosterListPanel.getRect();
        renameRect.top += rosterRect.top;
        renameRect.bottom += rosterRect.top;
        renameEntryLabel = new StaticTextVisualObject(
                0x464,
                renameRect,
                null,
                defaultPalette1Slot0(),
                null
        );

        addChild(detailsPanel);
        addChild(actionButtonsPanel);
        addChild(rosterListPanel);
        dialogActiveFlag = 0;
    }

    /**
     * vtbl +0x80: CharacterLoaderDialogVisualObject::ShowDialog @004319F7.
     * Fully ported.
     */
    @Override
    public void showDialog() {
        gameSession = resolveGameSession();
        Globals.mousePointer.disableBackgroundCapture();
        refreshCharacterRosterFiles();

        selectionInfoPanel = Globals.mainWindow.pSelectionInfoPanelVisualObject;
        mapVisual = Globals.mainWindow.getMapVisual();
        clearCharacterLoaderMapState();

        selectedUnit = resolveCurrentSelectedUnit();
        detailsPanel.refreshSelectedUnitPrimaryAttributes();

        if (getRosterEntryCount() > 1) {
            applySelectedCharacterToMap(selectedUnit);
        }

        moveSelectionInfoPanelIntoDialog();
        loadCharacterLoaderSounds();
        detailsPanel.loadDetailsResources();
        actionButtonsPanel.loadCharacterLoaderButtonArt();
        rosterListPanel.loadRosterListGraphics();
        rosterListPanel.resetRosterListState();

        clearScreen();
        super.showDialog();
        CMousePointer.Cursor_Default.setToMousePointer();
        dialogActiveFlag = 1;
        Globals.mousePointer.enableBackgroundCapture();
    }

    /**
     * vtbl +0x84: CharacterLoaderDialogVisualObject::HideDialog @00431BD3.
     * Fully ported.
     */
    @Override
    public HandlerVisualObject hideDialog(MessageCodes reason) {
        draw();
        if (reason == DIALOG_OK
                && rosterListPanel.selectedEntryIndex == getLastCharacterRosterEntryIndex()) {
            finalizeTailCharacterSelection();
        }

        dialogActiveFlag = 0;
        restoreSelectionInfoPanelToSpellbook();
        selectedUnit = null;
        releaseCharacterLoaderSounds();
        detailsPanel.releaseDetailsResources();
        actionButtonsPanel.releaseCharacterLoaderButtonArt();
        rosterListPanel.releaseRosterListGraphics();
        hideInlineRenameLabel();
        return super.hideDialog(reason);
    }

    /**
     * Native: CharacterLoaderDialogVisualObject::GetSelectedUnit @00438410.
     * Fully ported.
     */
    public CUnit getSelectedUnit() {
        return selectedUnit;
    }

    /**
     * Native support extracted from CharacterLoaderDetailsPanelVisualObject::Update @0042EE73
     * through CharacterLoaderDialogVisualObject::GetSelectedUnit @00438410 and CUnit::RenderFullStatsInfo @0046AA1D.
     * Fully ported.
     */
    public void renderSelectedUnitDetails(CRect detailsScreenRect) {
        getSelectedUnit().renderFullStatsInfo(detailsScreenRect);
    }

    /**
     * Native support extracted from CharacterLoaderDetailsPanelVisualObject::GetText @0042F31B
     * through CharacterLoaderDialogVisualObject::GetSelectedUnit @00438410 and CUnit::GetFullStatsTooltipText @0046B9F0.
     * Fully ported.
     */
    public String getSelectedUnitDetailsTooltip(int localX, int localY) {
        return getSelectedUnit().getFullStatsTooltipText(localX, localY);
    }

    /**
     * Native owner: sound fan-out from CharacterLoaderActionButtonsPanelVisualObject::OnLButtonDown @0042F9D1 through owner sound slots `+0x88`, `+0x8C`, `+0x90`, and `+0x94`.
     * Fully ported. Native plays the Accept sound twice in this mouse-down path and plays Rename only after release activation.
     */
    public void playActionButtonPressSound(int buttonIndex) {
        switch (buttonIndex) {
            case ACCEPT_BUTTON_INDEX -> {
                playPointerSound(okButtonSound);
                playPointerSound(okButtonSound);
            }
            case DELETE_BUTTON_INDEX -> playPointerSound(deleteButtonSound);
            case CANCEL_BUTTON_INDEX -> playPointerSound(cancelButtonSound);
        }
    }

    /**
     * Native support extracted from CharacterLoaderActionButtonsPanelVisualObject::OnLButtonUp @0042FA81.
     * Fully ported.
     */
    public void playRenameButtonReleaseSound() {
        playPointerSound(renameButtonSound);
    }

    /**
     * Native support extracted from CharacterLoaderActionButtonsPanelVisualObject::OnLButtonUp @0042FA81.
     * Fully ported.
     */
    public boolean isRosterEntryRenameActive() {
        return rosterListPanel.renameModeActiveFlag != 0;
    }

    /**
     * Native owner: roster-list entry count from owner field `+0x68` / CGameSession::GetCharacterRosterEntryCount @004383C0.
     * Fully ported.
     */
    public int getRosterEntryCount() {
        return gameSession.getCharacterRosterEntryCount();
    }

    /**
     * Native owner: roster-list non-tail selection refresh from CharacterLoaderRosterListPanelVisualObject::SelectEntry @00430D7D.
     * Fully ported.
     */
    public void selectRosterEntry(int entryIndex) {
        gameSession.loadCharacterRosterEntry(entryIndex);
        detailsPanel.refreshSelectedUnitPrimaryAttributes();
        applySelectedCharacterToMap(selectedUnit);
    }

    /**
     * Native owner: roster-list tail-entry selection refresh from CharacterLoaderRosterListPanelVisualObject::SelectEntry @00430D7D.
     * Fully ported.
     */
    public void selectNewCharacterEntry() {
        selectedUnit.setSelected(false);
        mapVisual.updateSelectionState();
    }

    /**
     * Native owner: roster-list Enter key when not in rename mode from CharacterLoaderRosterListPanelVisualObject::OnKeyDown @0043124E.
     * Fully ported.
     */
    public void acceptRosterSelection() {
        acceptCharacterLoaderSelection();
    }

    /**
     * Native owner: roster-list rename start from CharacterLoaderRosterListPanelVisualObject::OnLButtonDown @00430E1D through `ShowInlineRenameLabel @00431EAA`.
     * Fully ported.
     */
    public void startRosterEntryRename() {
        showInlineRenameLabel();
    }

    /**
     * Native owner: roster-list rename cancel from CharacterLoaderRosterListPanelVisualObject::OnKeyDown @0043124E through `HideInlineRenameLabel @00431FEA`.
     * Fully ported.
     */
    public void cancelRosterEntryRename() {
        hideInlineRenameLabel();
    }

    /**
     * Native owner: roster-list rename commit from CharacterLoaderRosterListPanelVisualObject::OnKeyDown @0043124E.
     * Fully ported.
     */
    public void commitRosterEntryRename() {
        StringBuilder updatedName = new StringBuilder();
        renameEntryLabel.copyTextToBuffer(updatedName);
        gameSession.commitSelectedCharacterRename(updatedName.toString());
        hideInlineRenameLabel();
    }

    /**
     * Native: CharacterLoaderDialogVisualObject::AcceptCharacterLoaderSelection @00431E6C.
     * Fully ported.
     */
    public void acceptCharacterLoaderSelection() {
        onMessage(DIALOG_OK, 0, 0);
    }

    /**
     * Native: CharacterLoaderDialogVisualObject::ReturnToGame @00431E8B.
     * Fully ported.
     */
    public void returnToGame() {
        onMessage(RETURN_TO_GAME, 0, 0);
    }

    /**
     * Native: CharacterLoaderDialogVisualObject::LoadCharacterLoaderSounds @00431939.
     * Fully ported.
     */
    private void loadCharacterLoaderSounds() {
        releaseCharacterLoaderSounds();
        okButtonSound = loadSound(SFX_CLICK_OK_WAV);
        renameButtonSound = loadSound(SFX_RENAME_WAV);
        deleteButtonSound = loadSound(SFX_DELETE_WAV);
        cancelButtonSound = loadSound(SFX_UNDO_WAV);
    }

    /**
     * Native: CharacterLoaderDialogVisualObject::ReleaseCharacterLoaderSounds @004319A6.
     * Fully ported.
     */
    private void releaseCharacterLoaderSounds() {
        okButtonSound = releaseSound(okButtonSound);
        renameButtonSound = releaseSound(renameButtonSound);
        deleteButtonSound = releaseSound(deleteButtonSound);
        cancelButtonSound = releaseSound(cancelButtonSound);
    }

    /**
     * Native: CharacterLoaderDialogVisualObject::ShowInlineRenameLabel @00431EAA.
     * Fully ported.
     */
    private void showInlineRenameLabel() {
        if (getLastCharacterRosterEntryIndex() == rosterListPanel.selectedEntryIndex) {
            return;
        }

        int rowHeight = rosterListPanel.entryRowTemplateRect.height();
        CRect rect = new CRect(renameEntryLabel.getRect());
        rect.top = rosterListPanel.entryListViewportRect.top
                + rowHeight * ((rosterListPanel.selectedEntryIndex + 1) - rosterListPanel.firstVisibleEntryIndex)
                - 4;
        rect.bottom = rosterListPanel.entryListViewportRect.top
                + rowHeight * ((rosterListPanel.selectedEntryIndex + 2) - rosterListPanel.firstVisibleEntryIndex);
        renameEntryLabel.setBounds(rect);
        renameEntryLabel.setInputText(getSelectedCharacterName());
        rosterListPanel.addChild(renameEntryLabel);
        switchEnabledChild(renameEntryLabel, true);
        rosterListPanel.renameModeActiveFlag = 1;
    }

    /**
     * Native: CharacterLoaderDialogVisualObject::HideInlineRenameLabel @00431FEA.
     * Fully ported.
     */
    private void hideInlineRenameLabel() {
        if (rosterListPanel.renameModeActiveFlag == 0) {
            return;
        }
        rosterListPanel.renameModeActiveFlag = 0;
        rosterListPanel.removeChild(renameEntryLabel);
        rosterListPanel.keyboardInputTarget = null;
        rosterListPanel.mouseInputTarget = null;
    }

    /**
     * Native: CharacterLoaderDialogVisualObject::ApplySelectedCharacterToMap @00431D0C.
     * Fully ported.
     */
    private void applySelectedCharacterToMap(CUnit selectedUnit) {
        if (selectedUnit != null) {
            selectedUnit.setSelected(true);
            selectedUnit.unitFlags |= 0x8;
        }
        mapVisual.updateSelectionState();
    }

    /**
     * Native support boundary for `AfxGetMainWnd()->m_GameSession` in CharacterLoaderDialogVisualObject::ShowDialog @004319F7.
     * Fully ported.
     */
    private CGameSession resolveGameSession() {
        return Globals.mainWindow.m_GameSession;
    }

    /**
     * Native support boundary for character-file scan and `New Character` tail insertion in CharacterLoaderDialogVisualObject::ShowDialog @004319F7 via FUN_0048FFC7.
     * Fully ported.
     */
    private void refreshCharacterRosterFiles() {
        gameSession.refreshCharacterRosterFiles();
    }

    /**
     * Native support boundary for clearing map fields `+0x998..+0x9A4` in CharacterLoaderDialogVisualObject::ShowDialog @004319F7.
     * Fully ported.
     */
    private void clearCharacterLoaderMapState() {
        mapVisual.clearCharacterLoaderQuestHoverKeys();
    }

    /**
     * Native: CharacterLoaderDialogVisualObject::ResolveCurrentSelectedUnit @004383E0.
     * Fully ported.
     */
    private CUnit resolveCurrentSelectedUnit() {
        return mapVisual.getSelectedCUnit();
    }

    /**
     * Native support extracted from CharacterLoaderDialogVisualObject::ShowInlineRenameLabel @00431EAA.
     * Fully ported.
     */
    private String getSelectedCharacterName() {
        return Globals.mainWindow.pMapVisualObject.getSelectedCUnit().clan;
    }

    /**
     * Native support boundary for the tail-entry accept path in CharacterLoaderDialogVisualObject::HideDialog @00431BD3 through FUN_00491312.
     * Fully ported.
     */
    private void finalizeTailCharacterSelection() {
        gameSession.initializeNewCharacterSession(0, null);
    }

    /**
     * Native support extracted from CharacterLoaderActionButtonsPanelVisualObject::OnLButtonUp @0042FA81.
     * Fully ported.
     */
    public void deleteSelectedCharacter() {
        int selectedEntryIndex = rosterListPanel.selectedEntryIndex;
        if (selectedEntryIndex == getLastCharacterRosterEntryIndex()) {
            return;
        }
        MessageCodes promptResult = Globals.mainWindow.showDialogAndAwaitResult(
                new HeaderDialogVariantVisualObject(
                        1,
                        0x40,
                        100,
                        0x17C,
                        0x252,
                        GameTexts.get(PatchText.ARE_YOU_SURE_YOU_WANT_TO_DELETE_CHARACTER_93)
                                + gameSession.m_PlayerName
                                + GameTexts.get(PatchText.BLANK_94),
                        null,
                        4
                )
        );
        if (promptResult != DIALOG_OK) {
            return;
        }

        gameSession.deleteSelectedCharacterRosterEntry();
        if (selectedEntryIndex < getLastCharacterRosterEntryIndex()) {
            gameSession.loadCharacterRosterEntry(selectedEntryIndex);
            detailsPanel.refreshSelectedUnitPrimaryAttributes();
            applySelectedCharacterToMap(selectedUnit);
        } else {
            rosterListPanel.selectedEntryIndex = getLastCharacterRosterEntryIndex();
            selectedUnit.setSelected(false);
            mapVisual.updateSelectionState();
        }
    }

    /**
     * Native support boundary for the selection-info panel detach/rehost block in CharacterLoaderDialogVisualObject::ShowDialog @004319F7.
     * Fully ported.
     */
    private void moveSelectionInfoPanelIntoDialog() {
        CVisualObject rightPanelContainer = Globals.mainWindow.getRightPanelContainerVisual();
        rightPanelContainer.removeChild(selectionInfoPanel);
        CRect rect = selectionInfoPanel.getRect();
        rect.offset(0x280 - rect.width(), 0);
        selectionInfoPanel.setBounds(rect);
        addChild(selectionInfoPanel);
    }

    /**
     * Native support boundary for the selection-info panel restore block in CharacterLoaderDialogVisualObject::HideDialog @00431BD3.
     * Fully ported.
     */
    private void restoreSelectionInfoPanelToSpellbook() {
        CRect rect = selectionInfoPanel.getRect();
        rect.offset(rect.width() - 0x280, 0);
        selectionInfoPanel.setBounds(rect);
        CVisualObject rightPanelContainer = Globals.mainWindow.getRightPanelContainerVisual();
        removeChild(selectionInfoPanel);
        rightPanelContainer.addChild(selectionInfoPanel);
        selectionInfoPanel = null;
        mapVisual = null;
    }

    /**
     * Native support extracted from CharacterLoaderDialogVisualObject::HideDialog @00431BD3 and
     * CharacterLoaderDialogVisualObject::ShowInlineRenameLabel @00431EAA.
     * Fully ported.
     */
    private int getLastCharacterRosterEntryIndex() {
        return getRosterEntryCount() - 1;
    }

    /**
     * Java helper around Sound::PlayPointer @00438570 for single-slot call sites.
     * not ported.
     */
    private static void playPointerSound(Sound sound) {
        if (sound != null) {
            sound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
        }
    }

    /**
     * Native support thunk: FUN_004384F0 @004384F0.
     * Fully ported for direct CharacterLoaderDialogVisualObject sound slots.
     */
    private static Sound loadSound(String resourcePath) {
        return new Sound(resourcePath);
    }

    /**
     * Native support thunk: FUN_00438480 @00438480.
     * Fully ported for direct CharacterLoaderDialogVisualObject sound slots.
     */
    private static Sound releaseSound(Sound sound) {
        if (sound != null) {
            SoundSystem.get().releaseSound(sound);
        }
        return null;
    }

    /**
     * Native helper for character-loader rename-label palette slot `g_pPalette1[0]` in InitializeCharacterLoaderDialog @00431698.
     * Fully ported.
     */
    private static Palette16 defaultPalette1Slot0() {
        return Palettes.p1.paletteData[0];
    }

    /**
     * Java helper for child hit testing in CharacterLoaderDialogVisualObject::OnMouseMove @00431DA1.
     * Fully ported.
     */
    private boolean containsScreenPoint(CVisualObject child, int x, int y) {
        CRect screenRect = new CRect();
        child.clientToScreen(screenRect, child.cRect);
        return screenRect.contains(x, y);
    }

}
