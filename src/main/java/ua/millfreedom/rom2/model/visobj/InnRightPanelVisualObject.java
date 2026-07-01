package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.TokenEntry;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.quest.Quest;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.sound.Sound;
import ua.millfreedom.rom2.model.window.CMainWindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.PatchText.CANCEL_QUEST_96;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_ABOUT_AWARD_357;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_ABOUT_QUEST_352;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_EXIT_232;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_FIRE_259;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_GET_351;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_HIRE_258;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_HIRE_FIRE_243;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_SELECT_356;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_TALK_242;
import static ua.millfreedom.rom2.text.TextTableId.PATCH;

/**
 * Native class: InnRightPanelVisualObject (vtbl @0x005CE4F0).
 * Purpose: inn dialog right action panel.
 */
public class InnRightPanelVisualObject extends CVisualObject {
    public static final int NATIVE_SIZE = 0xC8; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int BUTTON_COUNT = 3;
    private static final int BUTTON_TEXT_ALIGN = TextAlign.combine(TextAlign.CENTER, TextAlign.VERTICAL_CENTER);
    static final String BLANK_BUTTON_LABEL = " ";
    private static final short REWARD_KIND_ALLY = -3;
    private static final short REWARD_KIND_XP = -2;
    private static final short REWARD_KIND_MONEY = -1;
    private static final String BUTTON1_ON_BMP = "graphics/interface/inn/button1on.bmp";
    private static final String BUTTON2_ON_BMP = "graphics/interface/inn/button2on.bmp";
    private static final String BUTTON3_ON_BMP = "graphics/interface/inn/button3on.bmp";
    private static final String BUTTON1_OFF_BMP = "graphics/interface/inn/button1off.bmp";
    private static final String BUTTON2_OFF_BMP = "graphics/interface/inn/button2off.bmp";
    private static final String BUTTON3_OFF_BMP = "graphics/interface/inn/button3off.bmp";
    private static final String BUTTONS_AREA_BMP = "graphics/interface/inn/buttonsarea.bmp";
    private static final String NPC_ABOUT_SCRIPT = "npc%dabout";
    private static final String TREASURE_ALLY_SCRIPT = "treasureally";
    private static final String TREASURE_XP_SCRIPT = "treasurexp";
    private static final String TREASURE_MONEY_SCRIPT = "treasuremoney";
    private static final String TREASURE_ENCHANT_SCRIPT = "treasureenchant";
    private static final String TREASURE_ITEM_SCRIPT = "treasureitem";
    private static final String QUEST_SCRIPT = "quest%d";

    //0x5c
    public BasicInnDialogVisualObject ownerDialog;
    //0x60
    public final List<String> buttonLabels = new ArrayList<>();
    //0x74
    public CBmp64k button1OnBitmap;
    //0x78
    public CBmp64k button2OnBitmap;
    //0x7c
    public CBmp64k button3OnBitmap;
    //0x80
    public CBmp64k button1OffBitmap;
    //0x84
    public CBmp64k button2OffBitmap;
    //0x88
    public CBmp64k button3OffBitmap;
    //0x8c
    public CBmp64k buttonsAreaBitmap;
    //0x90
    public final CRect[] buttonRects = {new CRect(), new CRect(), new CRect()};
    //0xc0
    public int pressedButtonIndex;
    //0xc4
    public int hotButtonIndex;

    /**
     * Native: InnRightPanelVisualObject::InnRightPanelVisualObject @00495FFC.
     * Fully ported at the Java model-state level; native vtable installation and allocation mechanics are not emulated.
     */
    public InnRightPanelVisualObject() {
        super();
        initializeInnRightPanel();
    }

    /**
     * Native: InnRightPanelVisualObject::InnRightPanelVisualObject @00496076.
     * Fully ported at the Java model-state level; native vtable installation and allocation mechanics are not emulated.
     */
    public InnRightPanelVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            BasicInnDialogVisualObject ownerDialog
    ) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        this.ownerDialog = ownerDialog;
        initializeInnRightPanel();
    }

    /**
     * Native: InnRightPanelVisualObject::InitializeInnRightPanel @00496172.
     * Fully ported.
     */
    private void initializeInnRightPanel() {
        pressedButtonIndex = -1;
        hotButtonIndex = -1;

        buttonRects[0].set(0x1E4, 0x2C, 0x270, 0x5A);
        buttonRects[1].set(0x1E4, 0x5B, 0x270, 0x89);
        buttonRects[2].set(0x1E4, 0x8A, 0x270, 0xB8);

        clearButtonBitmapReferences();
        buttonLabels.clear();
        buttonLabels.add(get(MAIN_HIRE_FIRE_243));
        buttonLabels.add(get(MAIN_TALK_242));
        buttonLabels.add(get(MAIN_EXIT_232));

        m_nState |= 0x2;
    }

    /**
     * vtbl +0x14: InnRightPanelVisualObject::GetText @004A0FC0.
     */
    @Override
    public String getText() {
        return null;
    }

    /**
     * vtbl +0x2C: InnRightPanelVisualObject::Update @004966AA.
     * Fully ported.
     */
    @Override
    public void update() {
        if (ownerDialog.dialogActiveFlag == 0) {
            return;
        }

        int ownerLeft = ownerDialog.cRect.left;
        int ownerTop = ownerDialog.cRect.top;
        Globals.renderer.lockSurface();
        try {
            buttonsAreaBitmap.drawRectMasked(ownerLeft + cRect.left, ownerTop + cRect.top);
            drawButton(ownerLeft, ownerTop, 1);
            drawButton(ownerLeft, ownerTop, 0);
            drawButton(ownerLeft, ownerTop, 2);
        } finally {
            Globals.renderer.unlockSurface();
        }
    }

    /**
     * vtbl +0x4C: InnRightPanelVisualObject::OnMouseMove @00496B1E.
     * Fully ported.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        updateHotButtonIndex(nFlags, x, y);
        return 0;
    }

    /**
     * vtbl +0x54: InnRightPanelVisualObject::OnLButtonDown @00496B41.
     * Fully ported.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        pressedButtonIndex = getButtonIndexAtScreenPoint(x, y);
        if (pressedButtonIndex == 2) {
            playSoundPointer(ownerDialog.exitSound);
        }
        return 1;
    }

    /**
     * vtbl +0x58: InnRightPanelVisualObject::OnLButtonUp @00496BBA.
     * Fully ported.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        if (pressedButtonIndex >= 0
                && pressedButtonIndex < BUTTON_COUNT
                && getButtonIndexAtScreenPoint(x, y) == pressedButtonIndex) {
            int activatedButtonIndex = pressedButtonIndex;
            pressedButtonIndex = -1;
            updateHotButtonIndex(nFlags, x, y);

            if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN) {
                handleEntryListButton(activatedButtonIndex);
            } else {
                handleQuestRewardButton(activatedButtonIndex);
            }
        }

        pressedButtonIndex = -1;
        updateHotButtonIndex(nFlags, x, y);
        return 1;
    }

    /**
     * vtbl +0x5C: InnRightPanelVisualObject::OnLButtonDblClk @00496B96.
     * Fully ported.
     */
    @Override
    public int onLButtonDblClk(int nFlags, int x, int y) {
        return onLButtonDown(nFlags, x, y);
    }

    /**
     * Native: InnRightPanelVisualObject::LoadButtonBitmaps @00496317.
     * Fully ported.
     */
    public void loadButtonBitmaps() {
        releaseButtonBitmaps();
        button1OnBitmap = new CBmp64k(BUTTON1_ON_BMP);
        Globals.renderer.refreshMousePointer();
        button2OnBitmap = new CBmp64k(BUTTON2_ON_BMP);
        Globals.renderer.refreshMousePointer();
        button3OnBitmap = new CBmp64k(BUTTON3_ON_BMP);
        Globals.renderer.refreshMousePointer();
        button1OffBitmap = new CBmp64k(BUTTON1_OFF_BMP);
        Globals.renderer.refreshMousePointer();
        button2OffBitmap = new CBmp64k(BUTTON2_OFF_BMP);
        Globals.renderer.refreshMousePointer();
        button3OffBitmap = new CBmp64k(BUTTON3_OFF_BMP);
        Globals.renderer.refreshMousePointer();
        buttonsAreaBitmap = new CBmp64k(BUTTONS_AREA_BMP);
        Globals.renderer.refreshMousePointer();
    }

    /**
     * Native: InnRightPanelVisualObject::ReleaseButtonBitmaps @0049658F.
     * Fully ported. Java clears retained bitmap references instead of reproducing native delete/free calls.
     */
    public void releaseButtonBitmaps() {
        clearButtonBitmapReferences();
    }

    /**
     * Native: InnRightPanelVisualObject::ResetButtonState @004970FE.
     */
    public void resetButtonState() {
        pressedButtonIndex = -1;
        hotButtonIndex = -1;
    }

    /**
     * Native support extracted from InnRightPanelVisualObject::InitializeInnRightPanel @00496172
     * and ReleaseButtonBitmaps @0049658F.
     */
    private void clearButtonBitmapReferences() {
        button1OnBitmap = null;
        button2OnBitmap = null;
        button3OnBitmap = null;
        button1OffBitmap = null;
        button2OffBitmap = null;
        button3OffBitmap = null;
        buttonsAreaBitmap = null;
    }

    /**
     * Native: InnRightPanelVisualObject::GetButtonIndexAtScreenPoint @00497123.
     * Fully ported.
     */
    private int getButtonIndexAtScreenPoint(int x, int y) {
        int localX = x - ownerDialog.cRect.left;
        int localY = y - ownerDialog.cRect.top;

        for (int buttonIndex = 0; buttonIndex < BUTTON_COUNT; buttonIndex++) {
            if (buttonRects[buttonIndex].contains(localX, localY)) {
                return buttonIndex;
            }
        }
        return -1;
    }

    /**
     * Native: InnRightPanelVisualObject::UpdateHotButtonIndex @004971AA.
     * Fully ported.
     */
    private void updateHotButtonIndex(int nFlags, int x, int y) {
        int buttonIndex = getButtonIndexAtScreenPoint(x, y);
        if (buttonIndex < 0 || (nFlags & 0x1) != 0) {
            if (buttonIndex >= 0 && buttonIndex == pressedButtonIndex && (nFlags & 0x1) != 0) {
                hotButtonIndex = buttonIndex;
            } else {
                hotButtonIndex = -1;
            }
        } else {
            hotButtonIndex = buttonIndex;
        }
    }

    /**
     * Native: InnRightPanelVisualObject::RefreshPrimaryActionLabel @00497223.
     * Fully ported.
     */
    void refreshPrimaryActionLabel() {
        ensureButtonLabelCount();
        if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN) {
            refreshEntryListActionLabels();
        } else {
            refreshQuestRewardActionLabels();
        }
    }

    /**
     * Native support extracted from `sessionMode == 2` entry-list branch in
     * InnRightPanelVisualObject::RefreshPrimaryActionLabel @00497223.
     */
    private void refreshEntryListActionLabels() {
        CUnit selectedEntry = resolveSelectedAvailableEntryForLabel();
        if (selectedEntry == null) {
            buttonLabels.set(0, "");
        } else {
            buttonLabels.set(0, isAvailableEntryMarkedHired(selectedEntry) ? get(MAIN_FIRE_259) : get(MAIN_HIRE_258));
        }
        buttonLabels.set(1, get(MAIN_TALK_242));
    }

    /**
     * Native support extracted from `sessionMode != 2` quest/reward branch in
     * InnRightPanelVisualObject::RefreshPrimaryActionLabel @00497223.
     */
    private void refreshQuestRewardActionLabels() {
        int questCount = getOwnerQuestCount();
        if (questCount == 0) {
            if (ownerDialog.activeRewardTokenEntries.isEmpty()) {
                if (hasPendingInnQuest()) {
                    buttonLabels.set(0, get(PATCH, CANCEL_QUEST_96));
                    buttonLabels.set(1, BLANK_BUTTON_LABEL);
                } else {
                    buttonLabels.set(0, BLANK_BUTTON_LABEL);
                    buttonLabels.set(1, BLANK_BUTTON_LABEL);
                }
            } else {
                buttonLabels.set(0, get(MAIN_SELECT_356));
                buttonLabels.set(1, get(MAIN_ABOUT_AWARD_357));
            }
        } else {
            buttonLabels.set(0, get(MAIN_GET_351));
            buttonLabels.set(1, get(MAIN_ABOUT_QUEST_352));
        }
    }

    /**
     * Native support extracted from button body drawing in InnRightPanelVisualObject::Update @004966AA.
     */
    private void drawButton(int ownerLeft, int ownerTop, int buttonIndex) {
        CRect buttonRect = buttonRects[buttonIndex];
        boolean pressedAndHot = isPressedAndHot(buttonIndex);
        int drawX = ownerLeft + buttonRect.left;
        int drawY = ownerTop + buttonRect.top + (pressedAndHot ? 1 : 0);
        getCurrentButtonBitmap(buttonIndex, pressedAndHot).drawRectMasked(drawX, drawY);
        drawButtonLabel(buttonIndex, ownerLeft, ownerTop, pressedAndHot);
    }

    /**
     * Native support extracted from button bitmap selection in InnRightPanelVisualObject::Update @004966AA.
     */
    private CBmp64k getCurrentButtonBitmap(int buttonIndex, boolean pressedAndHot) {
        return switch (buttonIndex) {
            case 0 -> pressedAndHot ? button1OnBitmap : button1OffBitmap;
            case 1 -> pressedAndHot ? button2OnBitmap : button2OffBitmap;
            case 2 -> pressedAndHot ? button3OnBitmap : button3OffBitmap;
            default -> throw new IndexOutOfBoundsException("Button index out of range: " + buttonIndex);
        };
    }

    /**
     * Native support extracted from pressed/hot-state checks in InnRightPanelVisualObject::Update @004966AA.
     */
    private boolean isPressedAndHot(int buttonIndex) {
        return pressedButtonIndex >= 0
                && pressedButtonIndex == hotButtonIndex
                && hotButtonIndex == buttonIndex;
    }

    /**
     * Native support extracted from centered gFont4 label draw inside InnRightPanelVisualObject::Update @004966AA.
     */
    private void drawButtonLabel(int buttonIndex, int ownerLeft, int ownerTop, boolean pressedAndHot) {
        CRect buttonRect = buttonRects[buttonIndex];
        Palette16 palette = hotButtonIndex == buttonIndex ? Palettes.p2.paletteData[0] : Palettes.p1.paletteData[0];
        int centerX = ownerLeft + buttonRect.left + buttonRect.width() / 2;
        int centerY = ownerTop + buttonRect.top + (pressedAndHot ? 1 : 0) + buttonRect.height() / 2;
        Globals.fonts.font4.drawTextInternal(centerX, centerY, buttonLabels.get(buttonIndex), BUTTON_TEXT_ALIGN, palette);
    }

    /**
     * Native support extracted from `sessionMode == 2` entry-list button dispatch in
     * InnRightPanelVisualObject::OnLButtonUp @00496BBA.
     */
    private void handleEntryListButton(int buttonIndex) {
        switch (buttonIndex) {
            case 0 -> handleEntryListPrimaryButton();
            case 1 -> handleEntryListTalkButton();
            case 2 -> ownerDialog.handleExitAction();
            default -> {
            }
        }
    }

    /**
     * Native support extracted from primary `sessionMode == 2` entry-list branch in
     * InnRightPanelVisualObject::OnLButtonUp @00496BBA.
     */
    private void handleEntryListPrimaryButton() {
        CUnit selectedEntry = resolveSelectedAvailableEntryForAction();
        if (selectedEntry == null) {
            return;
        }

        if (isAvailableEntryMarkedHired(selectedEntry)) {
            ownerDialog.dismissSelectedHiredInnEntry();
        } else {
            ownerDialog.acceptSelectedInnEntryOffer();
        }
        refreshPrimaryActionLabel();
    }

    /**
     * Native support extracted from talk `sessionMode == 2` entry-list branch in
     * InnRightPanelVisualObject::OnLButtonUp @00496BBA.
     */
    private void handleEntryListTalkButton() {
        CUnit selectedEntry = resolveSelectedAvailableEntryForAction();
        if (selectedEntry == null) {
            ownerDialog.handleReservedEntry();
            return;
        }

        RoleDialogSupport.showRoleKeyDialog(String.format(Locale.ROOT, NPC_ABOUT_SCRIPT, Short.toUnsignedInt(selectedEntry.serverID)));
        playSoundPointer(ownerDialog.talkSound);
    }

    /**
     * Native support extracted from `sessionMode != 2` quest/reward button dispatch in
     * InnRightPanelVisualObject::OnLButtonUp @00496BBA.
     */
    private void handleQuestRewardButton(int buttonIndex) {
        switch (buttonIndex) {
            case 0 -> handleQuestRewardPrimaryButton();
            case 1 -> handleQuestRewardAboutButton();
            case 2 -> ownerDialog.handleExitAction();
            default -> {
            }
        }
    }

    /**
     * Native support extracted from primary `sessionMode != 2` quest/reward branch in
     * InnRightPanelVisualObject::OnLButtonUp @00496BBA.
     */
    private void handleQuestRewardPrimaryButton() {
        if (hasPendingInnQuest()) {
            ownerDialog.pendingInnQuestSelectionToggle = ownerDialog.pendingInnQuestSelectionToggle == 0 ? 1 : 0;
            return;
        }

        if (getOwnerQuestCount() != 0 || !ownerDialog.activeRewardTokenEntries.isEmpty()) {
            toggleSelectedQuestContext();
        }
    }

    /**
     * Native support extracted from about `sessionMode != 2` quest/reward branch in
     * InnRightPanelVisualObject::OnLButtonUp @00496BBA.
     */
    private void handleQuestRewardAboutButton() {
        if (getOwnerQuestCount() == 0) {
            if (!ownerDialog.activeRewardTokenEntries.isEmpty()) {
                showSelectedRewardScript();
                playSoundPointer(ownerDialog.talkSound);
            }
            return;
        }

        Quest quest = resolveOwnerSelectedQuest();
        if (quest != null) {
            int questId = resolveQuestId(quest);
            if (questId != 0) {
                RoleDialogSupport.showRoleKeyDialog(String.format(Locale.ROOT, QUEST_SCRIPT, questId));
            }
        }
        playSoundPointer(ownerDialog.talkSound);
    }

    /**
     * Native support extracted from available-entry lookup branches in InnRightPanelVisualObject::OnLButtonUp @00496BBA.
     */
    private CUnit resolveSelectedAvailableEntryForAction() {
        int selectedIndex = ownerDialog.innEntrySelectionIndex;
        if (selectedIndex >= ownerDialog.availableInnEntries.size()) {
            return null;
        }
        return ownerDialog.availableInnEntries.get(selectedIndex);
    }

    /**
     * Native support extracted from available-entry lookup branches in
     * InnRightPanelVisualObject::RefreshPrimaryActionLabel @00497223.
     */
    private CUnit resolveSelectedAvailableEntryForLabel() {
        int selectedIndex = ownerDialog.innEntrySelectionIndex;
        if (selectedIndex == -1 || selectedIndex >= ownerDialog.availableInnEntries.size()) {
            return null;
        }
        return ownerDialog.availableInnEntries.get(selectedIndex);
    }

    /**
     * Native support extracted from CDWordArray high-bit check in InnRightPanelVisualObject::OnLButtonUp @00496BBA.
     */
    private boolean isAvailableEntryMarkedHired(CUnit selectedEntry) {
        int entryIdIndex = ownerDialog.findInnEntryIdIndex(selectedEntry);
        return (ownerDialog.innEntryIds.get(entryIdIndex) & 0x80000000) != 0;
    }

    /**
     * Native support extracted from QuestsStorage::quests_Count call sites in
     * InnRightPanelVisualObject::OnLButtonUp @00496BBA and RefreshPrimaryActionLabel @00497223.
     */
    private int getOwnerQuestCount() {
        return ownerDialog.questsStorage.questsByKey.size();
    }

    /**
     * Native support extracted from QuestsStorage::FindQuestKeyByMessage @0052E198 call sites in
     * InnRightPanelVisualObject::OnLButtonUp @00496BBA and RefreshPrimaryActionLabel @00497223.
     * Partial port. Java delegates the recovered quest-message dispatch to QuestsStorage while concrete quest subclass
     * message overrides remain unmodeled.
     */
    private boolean hasPendingInnQuest() {
        MapVisualObject mapVisualObject = Globals.mainWindow.pMapVisualObject;
        return mapVisualObject.questStorage.findQuestKeyByMessage(
                Quest.MESSAGE_INN_PROBE,
                mapVisualObject.currentPlayer.playerId,
                ownerDialog.innInteractionTargetTokenId
        ) != 0;
    }

    /**
     * Native support extracted from quest-selection toggle in InnRightPanelVisualObject::OnLButtonUp @00496BBA.
     */
    private void toggleSelectedQuestContext() {
        if (ownerDialog.questSelectionIndex == ownerDialog.questContext) {
            ownerDialog.questSelectionIndex = -1;
        } else {
            ownerDialog.questSelectionIndex = ownerDialog.questContext;
        }
    }

    /**
     * Native support extracted from owner quest lookup in InnRightPanelVisualObject::OnLButtonUp @00496BBA.
     */
    private Quest resolveOwnerSelectedQuest() {
        return ownerDialog.questsStorage.findQuestByKey(ownerDialog.questContext);
    }

    /**
     * Native support extracted from quest `GetId` call in InnRightPanelVisualObject::OnLButtonUp @00496BBA.
     */
    private static int resolveQuestId(Quest quest) {
        return quest.getId();
    }

    /**
     * Native support extracted from reward script-name selection in InnRightPanelVisualObject::OnLButtonUp @00496BBA.
     */
    private void showSelectedRewardScript() {
        int selectedIndex = ownerDialog.questContext;
        TokenEntry rewardToken = ownerDialog.activeRewardTokenEntries.get(selectedIndex);
        short rewardKind = (short) rewardToken.packedTokenHash;
        if (rewardKind == REWARD_KIND_ALLY) {
            RoleDialogSupport.showRoleKeyDialog(TREASURE_ALLY_SCRIPT);
        } else if (rewardKind == REWARD_KIND_XP) {
            RoleDialogSupport.showRoleKeyDialog(TREASURE_XP_SCRIPT);
        } else if (rewardKind == REWARD_KIND_MONEY) {
            RoleDialogSupport.showRoleKeyDialog(TREASURE_MONEY_SCRIPT);
        } else if (selectedIndex < ownerDialog.activeRewardTokenEntries.size() - 1) {
            RoleDialogSupport.showRoleKeyDialog(TREASURE_ITEM_SCRIPT);
        } else {
            RoleDialogSupport.showRoleKeyDialog(TREASURE_ENCHANT_SCRIPT);
        }
    }

    /**
     * Native support extracted from Sound::PlayPointer @00438570 call sites in
     * InnRightPanelVisualObject::OnLButtonDown @00496B41 and OnLButtonUp @00496BBA.
     */
    private static void playSoundPointer(Sound sound) {
        if (sound != null) {
            sound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
        }
    }

    /**
     * Native support extracted from CStringArray::SetSize call in
     * InnRightPanelVisualObject::InitializeInnRightPanel @00496172.
     */
    private void ensureButtonLabelCount() {
        while (buttonLabels.size() < BUTTON_COUNT) {
            buttonLabels.add("");
        }
    }
}
