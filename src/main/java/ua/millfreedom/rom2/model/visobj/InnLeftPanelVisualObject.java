package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CBmp256;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.CUnitInfo;
import ua.millfreedom.rom2.model.quest.Quest;
import ua.millfreedom.rom2.model.TokenEntry;
import ua.millfreedom.rom2.model.UnitTypes;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.gameobj.CGameObject;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.sound.Sound;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.res.Resources;
import ua.millfreedom.rom2.text.BuildingText;
import ua.millfreedom.rom2.text.NpcNamesText;
import ua.millfreedom.rom2.text.UnitNameText;

import java.util.Arrays;
import java.util.Locale;

import static ua.millfreedom.rom2.res.Constants.GRAPHICS;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_THIRD_HERO_DIES_IN_THE_WORST_POSSIBLE_WAY_284;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_N_W_317;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_NO_UNITS_47;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_SELECTED_48;
import static ua.millfreedom.rom2.text.TextTableId.BUILDING;
import static ua.millfreedom.rom2.text.TextTableId.NPCNAMES;
import static ua.millfreedom.rom2.text.TextTableId.UNITNAME;

/**
 * Native class: InnLeftPanelVisualObject (vtbl @0x005CE478).
 * Purpose: inn dialog left detail panel for unit stats, portrait/equipment hotspots, and quest text.
 */
public class InnLeftPanelVisualObject extends CVisualObject {
    public static final int NATIVE_SIZE = 0x190; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int DETAIL_BITMAP_WIDTH = 0xA0;
    private static final int DETAIL_BITMAP_HEIGHT = 0xF0;
    private static final int TOP_PANEL_HEIGHT = 0xEE;
    private static final int STATS_PANEL_X_OFFSET = 0x0C;
    private static final int HOTSPOT_X_OFFSET = 0x0C;
    private static final int HOTSPOT_Y_OFFSET = 0xF0;
    private static final int UNIT_FLAG_HIDE_STATS_PANEL = 0x40;
    private static final int UNIT_FLAG_DYNAMIC_INFO_PICTURE_MASK = 0x11;
    private static final int UNIT_FLAG_EQUIPMENT_PORTRAIT_DIRTY = 0x08;
    private static final int QUEST_TEXT_LINE_SPACING = 10;
    private static final int MONSTER_TYPE_FIRST = 0x52;
    private static final int MONSTER_TYPE_LAST = 0x66;
    private static final int QUEST_ID_TOKEN_TARGET = 1;
    private static final int QUEST_ID_PACKED_UNIT_TARGET = 2;
    private static final int QUEST_ID_FLAGGED_UNIT_TARGET = 3;
    private static final int QUEST_ID_OBJECT_TARGET = 4;
    private static final int QUEST_ID_NPC_TARGET = 11;
    private static final int QUEST_ID_FLAGGED_UNIT_TARGET_NO_ARGS = 12;
    private static final short REWARD_KIND_ALLY = -3;
    private static final String LEFT_STATS_BMP = "graphics/interface/inn/leftstats.bmp";
    private static final String LEFT_PICTURE_BMP = "graphics/interface/inn/leftpicture.bmp";
    private static final String INFOWINDOW_DIRECTORY = "infowindow";
    private static final String BMP_SUFFIX = ".bmp";

    //0x5c
    public BasicInnDialogVisualObject ownerDialog;
    //0x60
    public String cachedPortraitKey;
    //0x160
    public CBmp64k detailBitmap;
    //0x164
    public CBmp256 detailHotspotBitmap;
    //0x168
    public CBmp64k topPanelBitmap;
    //0x16c
    public CBmp64k bottomPanelBitmap;
    //0x170
    public final CRect leftActionRect = new CRect();
    //0x180
    public final CRect rightActionRect = new CRect();

    /**
     * Native: InnLeftPanelVisualObject::InnLeftPanelVisualObject @004941D0.
     * Fully ported at the Java model-state level; native vtable installation and allocation mechanics are not emulated.
     */
    public InnLeftPanelVisualObject() {
        super();
        initializeInnLeftPanel();
    }

    /**
     * Native: InnLeftPanelVisualObject::InnLeftPanelVisualObject @00494240.
     * Fully ported at the Java model-state level; native vtable installation and allocation mechanics are not emulated.
     */
    public InnLeftPanelVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            BasicInnDialogVisualObject ownerDialog
    ) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        this.ownerDialog = ownerDialog;
        initializeInnLeftPanel();
    }

    /**
     * Native: InnLeftPanelVisualObject::InitializeInnLeftPanel @0049437E.
     * Fully ported at the Java model-state level.
     */
    private void initializeInnLeftPanel() {
        this.m_nState |= 0x2;

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);

        this.leftActionRect.set(
                screenRect.left + 0x11,
                screenRect.top + 0x1BB,
                screenRect.left + 0x31,
                screenRect.top + 0x1DB
        );
        this.rightActionRect.set(
                screenRect.left + 0x87,
                screenRect.top + 0x1BB,
                screenRect.left + 0xA7,
                screenRect.top + 0x1DB
        );

        this.detailBitmap = new CBmp64k(DETAIL_BITMAP_WIDTH, DETAIL_BITMAP_HEIGHT);
        this.detailHotspotBitmap = new CBmp256(DETAIL_BITMAP_WIDTH, DETAIL_BITMAP_HEIGHT);
        this.cachedPortraitKey = "";
        this.topPanelBitmap = null;
        this.bottomPanelBitmap = null;
    }

    /**
     * Native: InnLeftPanelVisualObject::SelectPreviousAvailableEntry @004945C4.
     * Fully ported.
     */
    public void selectPreviousAvailableEntry() {
        if (ownerDialog.innEntrySelectionIndex < 1) {
            ownerDialog.innEntrySelectionIndex = ownerDialog.availableInnEntries.size() - 1;
        } else {
            ownerDialog.innEntrySelectionIndex--;
        }
        ownerDialog.actionPanelVisual.refreshPrimaryActionLabel();
        playSoundPointer(ownerDialog.helperSound);
    }

    /**
     * Native: InnLeftPanelVisualObject::SelectNextAvailableEntry @0049463B.
     * Fully ported.
     */
    public void selectNextAvailableEntry() {
        if (ownerDialog.innEntrySelectionIndex < ownerDialog.availableInnEntries.size() - 1) {
            ownerDialog.innEntrySelectionIndex++;
        } else {
            ownerDialog.innEntrySelectionIndex = 0;
        }
        ownerDialog.actionPanelVisual.refreshPrimaryActionLabel();
        playSoundPointer(ownerDialog.helperSound);
    }

    /**
     * vtbl +0x14: InnLeftPanelVisualObject::GetText @00495BFD.
     * Fully ported at the modeled bitmap/token boundary.
     */
    @Override
    public String getText() {
        if (ownerDialog.dialogActiveFlag == 0) {
            return null;
        }
        if (Globals.mainWindow.sessionMode != CMainWindow.SESSION_MODE_CAMPAIGN) {
            return null;
        }

        CUnit selectedEntry = resolveTooltipSelectedEntry();
        int mouseX = Globals.mousePointer.getX();
        int mouseY = Globals.mousePointer.getY();
        int localX = mouseX - ownerDialog.cRect.left;
        int localY = mouseY - ownerDialog.cRect.top;

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        CRect topPanelRect = new CRect(0, 0, screenRect.width(), TOP_PANEL_HEIGHT);
        if (topPanelRect.contains(localX, localY)) {
            if (isStatsPanelSuppressed(selectedEntry)) {
                return null;
            }
            return resolveEntryPanelTooltip(selectedEntry, localX, localY);
        }

        int hotspotIndex = getBottomHotspotIndex(screenRect, mouseX, mouseY);
        if (hotspotIndex == 0) {
            return null;
        }
        return resolveEntryHotspotTooltip(selectedEntry, hotspotIndex);
    }

    /**
     * vtbl +0x2C: InnLeftPanelVisualObject::Update @00494780.
     * Fully ported.
     */
    @Override
    public void update() {
        CUnit selectedEntry = ownerDialog.innEntrySelectionIndex < 0 ? null : resolveSelectedEntry();
        renderSelectedEntry(selectedEntry);
    }

    /**
     * vtbl +0x54: InnLeftPanelVisualObject::OnLButtonDown @00494771.
     * Fully ported. Native returns `0`.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        return 0;
    }

    /**
     * Native: InnLeftPanelVisualObject::SelectEntry @004946B6.
     * Fully ported at the modeled owner-dialog boundary.
     */
    public void selectEntry(int selectionId) {
        if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN) {
            ownerDialog.innEntrySelectionIndex = selectionId;
            resolveSelectedEntry();
            ownerDialog.actionPanelVisual.refreshPrimaryActionLabel();
        } else {
            ownerDialog.questContext = selectionId;
        }

        playSoundPointer(ownerDialog.helperSound);
    }

    /**
     * Native: InnLeftPanelVisualObject::LoadInnPanelBitmaps @00495E81.
     * Fully ported at the Java bitmap-owner boundary.
     */
    void loadInnPanelBitmaps() {
        releaseInnPanelBitmaps();
        topPanelBitmap = new CBmp64k(LEFT_STATS_BMP);
        Globals.renderer.refreshMousePointer();
        bottomPanelBitmap = new CBmp64k(LEFT_PICTURE_BMP);
        Globals.renderer.refreshMousePointer();
    }

    /**
     * Native: InnLeftPanelVisualObject::ReleaseInnPanelBitmaps @00495F5B.
     * Fully ported at the Java bitmap-owner boundary.
     */
    void releaseInnPanelBitmaps() {
        topPanelBitmap = null;
        bottomPanelBitmap = null;
    }

    /**
     * Native support extracted from InnLeftPanelVisualObject::SelectEntry @004946B6 and Update @00494780.
     * Fully ported for the recovered inn-entry array split.
     */
    private CUnit resolveSelectedEntry() {
        int selectedIndex = ownerDialog.innEntrySelectionIndex;
        if (selectedIndex < 0) {
            return null;
        }
        if (selectedIndex < ownerDialog.availableInnEntries.size()) {
            return ownerDialog.availableInnEntries.get(selectedIndex);
        }
        return ownerDialog.reservedInnEntries.get(selectedIndex - ownerDialog.availableInnEntries.size());
    }

    /**
     * Native support extracted from InnLeftPanelVisualObject::GetText @00495BFD.
     * Fully ported for native tooltip lookup semantics.
     */
    private CUnit resolveTooltipSelectedEntry() {
        if (ownerDialog.availableInnEntries.isEmpty() && ownerDialog.reservedInnEntries.isEmpty()) {
            return null;
        }
        int selectedIndex = ownerDialog.innEntrySelectionIndex;
        if (selectedIndex < ownerDialog.availableInnEntries.size()) {
            return ownerDialog.availableInnEntries.get(selectedIndex);
        }
        return ownerDialog.reservedInnEntries.get(selectedIndex - ownerDialog.availableInnEntries.size());
    }

    /**
     * Native: InnLeftPanelVisualObject::RenderSelectedEntry @00494832.
     * Fully ported at the Java model/render-target boundary.
     */
    private void renderSelectedEntry(CUnit selectedEntry) {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);

        Globals.renderer.lockSurface();
        try {
            topPanelBitmap.draw(screenRect.left, screenRect.top, 0, null, false);
            bottomPanelBitmap.draw(screenRect.left, screenRect.top + TOP_PANEL_HEIGHT, 0, null, false);
            if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN) {
                if (selectedEntry == null) {
                    drawEmptySelectionText(screenRect);
                } else {
                    if (!isStatsPanelSuppressed(selectedEntry)) {
                        drawEntryStatsPanel(selectedEntry, screenRect);
                    }
                    refreshEntryPortrait(selectedEntry);
                    detailBitmap.drawRectMasked(
                            screenRect.left + 0x0B,
                            screenRect.top + HOTSPOT_Y_OFFSET,
                            0,
                            0,
                            DETAIL_BITMAP_WIDTH,
                            DETAIL_BITMAP_HEIGHT
                    );
                }
            }

            drawQuestSummary(screenRect);
        } finally {
            Globals.renderer.unlockSurface();
        }
    }

    /**
     * Native support extracted from unit flag `0x40` branches in InnLeftPanelVisualObject::GetText @00495BFD
     * and RenderSelectedEntry @00494832.
     * Fully ported.
     */
    private static boolean isStatsPanelSuppressed(CUnit selectedEntry) {
        return (selectedEntry.unitFlags & UNIT_FLAG_HIDE_STATS_PANEL) != 0;
    }

    /**
     * Native support extracted from InnLeftPanelVisualObject::GetText @00495BFD bottom-panel hit-map indexing.
     * Fully ported at the modeled bitmap access boundary.
     */
    private int getBottomHotspotIndex(CRect screenRect, int mouseX, int mouseY) {
        int localX = mouseX - screenRect.left - HOTSPOT_X_OFFSET;
        int localY = mouseY - screenRect.top - HOTSPOT_Y_OFFSET;
        var frame = detailHotspotBitmap.frames.getFirst();
        return frame.pixels()[localY * DETAIL_BITMAP_WIDTH + localX];
    }

    /**
     * Native support extracted from InnLeftPanelVisualObject::RenderSelectedEntry @00494832 empty-selection branch.
     * Fully ported.
     */
    private static void drawEmptySelectionText(CRect screenRect) {
        CBitmapFont font = Globals.fonts.font2;
        int centerX = screenRect.left + 0x58;
        font.drawTextShadowed(centerX, screenRect.top + 0x36, get(MAIN_NO_UNITS_47), TextAlign.CENTER.mask, Palettes.yellowish, 1);
        font.drawTextShadowed(centerX, screenRect.top + 0x42, get(MAIN_SELECTED_48), TextAlign.CENTER.mask, Palettes.yellowish, 1);
    }

    /**
     * Native support extracted from CUnit::RenderFullStatsInfo @0046AA1D call inside
     * InnLeftPanelVisualObject::RenderSelectedEntry @00494832.
     * Fully ported as a native call boundary.
     */
    private static void drawEntryStatsPanel(CUnit selectedEntry, CRect screenRect) {
        CRect statsRect = new CRect(
                screenRect.left + STATS_PANEL_X_OFFSET,
                screenRect.top,
                screenRect.left + STATS_PANEL_X_OFFSET + DETAIL_BITMAP_WIDTH,
                screenRect.top + TOP_PANEL_HEIGHT
        );
        selectedEntry.renderFullStatsInfo(statsRect);
    }

    /**
     * Native support extracted from info-picture refresh branches inside
     * InnLeftPanelVisualObject::RenderSelectedEntry @00494832.
     * Fully ported at the Java bitmap/render-target boundary.
     */
    private void refreshEntryPortrait(CUnit selectedEntry) {
        if ((selectedEntry.unitFlags & UNIT_FLAG_DYNAMIC_INFO_PICTURE_MASK) == 0) {
            loadStaticInfoPicture(resolveInfoPictureName(selectedEntry));
            return;
        }

        refreshDynamicInfoPicture(selectedEntry);
    }

    /**
     * Native support extracted from static unit info-picture name branches inside
     * InnLeftPanelVisualObject::RenderSelectedEntry @00494832.
     * Fully ported.
     */
    private static String resolveInfoPictureName(CUnit selectedEntry) {
        return resolveInfoPictureName(selectedEntry.type, selectedEntry.field8_0x28);
    }

    /**
     * Native support extracted from static unit info-picture name branches inside
     * InnLeftPanelVisualObject::RenderSelectedEntry @00494832.
     * Fully ported.
     */
    private static String resolveInfoPictureName(int unitType, int portraitVariant) {
        CUnitInfo info = UnitTypes.getUnitInfo(unitType);
        return portraitVariant > 1 ? info.m_InfoPicture + portraitVariant : info.m_InfoPicture;
    }

    /**
     * Native support extracted from dynamic temp-cache name branches inside
     * InnLeftPanelVisualObject::RenderSelectedEntry @00494832.
     * Fully ported for the native cache key.
     */
    private static String resolveDynamicInfoPictureName(CUnit selectedEntry) {
        return "allods-2-" + (selectedEntry.m_id & 0xFFFF) + ".$$$";
    }

    /**
     * Native support extracted from dynamic info-picture cache branches inside
     * InnLeftPanelVisualObject::RenderSelectedEntry @00494832.
     * Fully ported for the native cache-key and dirty-flag behavior. Java routes cache misses through the in-memory
     * equipment portrait cache instead of reloading the native temp BMP file.
     */
    private void refreshDynamicInfoPicture(CUnit unit) {
        String pictureName = resolveDynamicInfoPictureName(unit);
        boolean cacheHit = pictureName.equals(cachedPortraitKey);
        if (cacheHit && (unit.unitFlags & UNIT_FLAG_EQUIPMENT_PORTRAIT_DIRTY) == 0) {
            return;
        }

        cachedPortraitKey = pictureName;
        renderEquipmentPortraitWithSurfaceUnlocked(unit, pictureName);
    }

    /**
     * Native support extracted from dynamic portrait branches around CUnit::RenderEquipmentPortrait @004688F7 in
     * InnLeftPanelVisualObject::RenderSelectedEntry @00494832.
     * Fully ported for the native unlock/render/relock call shape and temp-cache key.
     */
    private void renderEquipmentPortraitWithSurfaceUnlocked(CUnit unit, String pictureName) {
        Globals.renderer.unlockSurface();
        try {
            unit.renderEquipmentPortrait(pictureName, detailBitmap, detailHotspotBitmap);
        } finally {
            Globals.renderer.lockSurface();
        }
    }

    /**
     * Native support extracted from static info-window bitmap loading inside
     * InnLeftPanelVisualObject::RenderSelectedEntry @00494832.
     * Fully ported at the Java bitmap-owner boundary.
     */
    private void loadStaticInfoPicture(String pictureName) {
        if (pictureName.equals(cachedPortraitKey)) {
            return;
        }

        cachedPortraitKey = pictureName;
        detailBitmap.loadBmp24Pixels(Resources.path(GRAPHICS, INFOWINDOW_DIRECTORY, pictureName + BMP_SUFFIX), null);
        clearDetailHotspotBitmap();
    }

    /**
     * Native support extracted from static portrait reload hit-map clearing in
     * InnLeftPanelVisualObject::RenderSelectedEntry @00494832.
     * Fully ported.
     */
    private void clearDetailHotspotBitmap() {
        Arrays.fill(detailHotspotBitmap.frames.getFirst().pixels(), 0);
    }

    /**
     * Native support extracted from InnLeftPanelVisualObject::RenderSelectedEntry @00494832 quest objective branch.
     * Fully ported.
     */
    private void drawQuestSummary(CRect screenRect) {
        Quest quest = resolveQuestSummaryQuest();
        if (quest == null) {
            drawRewardAllyPortrait(screenRect);
            return;
        }

        int questId = quest.getId();
        CGameObject targetObject = resolveQuestPrimaryObject(quest, questId);
        boolean targetPortraitReady = questId == QUEST_ID_PACKED_UNIT_TARGET
                ? refreshPackedQuestTargetPortrait(quest.primaryArgument)
                : refreshQuestTargetPortrait(targetObject);
        if (targetPortraitReady) {
            drawDetailPortrait(screenRect);
        }

        String objectiveText = formatQuestObjectiveText(quest, questId, targetObject);
        drawQuestObjectiveText(screenRect, objectiveText);
    }

    /**
     * Native support extracted from reward ally portrait branch in
     * InnLeftPanelVisualObject::RenderSelectedEntry @00494832.
     * Fully ported.
     */
    private void drawRewardAllyPortrait(CRect screenRect) {
        if (ownerDialog.activeRewardTokenEntries.isEmpty()) {
            return;
        }

        int selectedIndex = ownerDialog.questContext;
        TokenEntry rewardToken = ownerDialog.activeRewardTokenEntries.get(selectedIndex);
        short rewardKind = (short) rewardToken.packedTokenHash;
        if (rewardKind != REWARD_KIND_ALLY) {
            return;
        }

        if (refreshPackedQuestTargetPortrait(rewardToken.quantity)) {
            drawDetailPortrait(screenRect);
        }
    }

    /**
     * Native support extracted from CBmp64k::DrawRectMasked @00425585 call sites in
     * InnLeftPanelVisualObject::RenderSelectedEntry @00494832.
     * Fully ported as a native bitmap-call boundary.
     */
    private void drawDetailPortrait(CRect screenRect) {
        detailBitmap.drawRectMasked(
                screenRect.left + 0x0B,
                screenRect.top + HOTSPOT_Y_OFFSET,
                0,
                0,
                DETAIL_BITMAP_WIDTH,
                DETAIL_BITMAP_HEIGHT
        );
    }

    /**
     * Native support extracted from QuestsStorage::FindQuestKeyByMessage @0052E198 and
     * QuestsStorage::FindQuestByKey @004A1050 calls inside InnLeftPanelVisualObject::RenderSelectedEntry @00494832.
     * Fully ported at the modeled QuestsStorage boundary.
     */
    private Quest resolveQuestSummaryQuest() {
        MapVisualObject mapVisualObject = Globals.mainWindow.pMapVisualObject;
        int pendingQuestKey = mapVisualObject.questStorage.findQuestKeyByMessage(
                Quest.MESSAGE_INN_PROBE,
                mapVisualObject.currentPlayer.playerId,
                ownerDialog.innInteractionTargetTokenId
        );
        if (pendingQuestKey != 0) {
            return mapVisualObject.questStorage.findQuestByKey(pendingQuestKey);
        }

        int questKey = ownerDialog.questContext;
        return ownerDialog.questsStorage.findQuestByKey(questKey);
    }

    /**
     * Native support extracted from InnLeftPanelVisualObject::RenderSelectedEntry @00494832 quest primary-object
     * lookup branches.
     * Fully ported.
     */
    private static CGameObject resolveQuestPrimaryObject(Quest quest, int questId) {
        MapVisualObject mapVisualObject = Globals.mainWindow.pMapVisualObject;
        return switch (questId) {
            case QUEST_ID_FLAGGED_UNIT_TARGET, QUEST_ID_FLAGGED_UNIT_TARGET_NO_ARGS ->
                    mapVisualObject.findUnitByQuestFlags(quest.primaryArgument);
            case QUEST_ID_TOKEN_TARGET, QUEST_ID_OBJECT_TARGET, QUEST_ID_NPC_TARGET ->
                    mapVisualObject.getObjectByToken((short) quest.primaryArgument);
            default -> null;
        };
    }

    /**
     * Native support extracted from InnLeftPanelVisualObject::RenderSelectedEntry @00494832 quest target portrait
     * branches.
     * Fully ported at the modeled CUnit portrait boundary.
     */
    private boolean refreshQuestTargetPortrait(CGameObject targetObject) {
        if (targetObject == null) {
            return false;
        }
        CUnit targetUnit = (CUnit) targetObject;
        if ((targetUnit.unitFlags & UNIT_FLAG_DYNAMIC_INFO_PICTURE_MASK) == 0) {
            loadStaticInfoPicture(resolveInfoPictureName(targetUnit));
            return true;
        }

        refreshDynamicInfoPicture(targetUnit);
        return true;
    }

    /**
     * Native support extracted from packed unit/portrait branches in
     * InnLeftPanelVisualObject::RenderSelectedEntry @00494832.
     * Fully ported.
     */
    private boolean refreshPackedQuestTargetPortrait(int packedUnitAndPortrait) {
        int unitType = packedUnitAndPortrait & 0xFF;
        int portraitVariant = packedUnitAndPortrait >>> 8;
        String pictureName = resolveInfoPictureName(unitType, portraitVariant);
        if (pictureName.isEmpty()) {
            return false;
        }
        loadStaticInfoPicture(pictureName);
        return true;
    }

    /**
     * Native support extracted from CString::Format calls in InnLeftPanelVisualObject::RenderSelectedEntry @00494832.
     * Fully ported.
     */
    private static String formatQuestObjectiveText(Quest quest, int questId, CGameObject targetObject) {
        if (questId <= 0) {
            return "";
        }

        String format = get(MAIN_THIRD_HERO_DIES_IN_THE_WORST_POSSIBLE_WAY_284 + questId);
        String targetName = resolveQuestTargetName(quest, questId, targetObject);
        String regionName = resolveQuestRegionName(quest);
        String landmarkName = resolveQuestLandmarkName(quest);
        return switch (questId) {
            case QUEST_ID_TOKEN_TARGET -> String.format(Locale.ROOT, format, targetName, regionName, landmarkName);
            case QUEST_ID_PACKED_UNIT_TARGET -> String.format(Locale.ROOT, format, quest.secondaryArgument, targetName);
            case QUEST_ID_FLAGGED_UNIT_TARGET -> String.format(Locale.ROOT, format, regionName, landmarkName);
            case 4, 5, 13 -> String.format(Locale.ROOT, format, regionName);
            case 6 -> String.format(Locale.ROOT, format, Integer.divideUnsigned(quest.secondaryArgument, 0x3C0),
                    (quest.secondaryArgument >>> 4) % 0x3C, regionName);
            case 8, 9, 10 -> String.format(Locale.ROOT, format, quest.secondaryArgument);
            case QUEST_ID_NPC_TARGET -> String.format(Locale.ROOT, format, targetName);
            case QUEST_ID_FLAGGED_UNIT_TARGET_NO_ARGS -> String.format(Locale.ROOT, format);
            default -> "";
        };
    }

    /**
     * Native support extracted from quest primary target-name branches in
     * InnLeftPanelVisualObject::RenderSelectedEntry @00494832.
     * Fully ported.
     */
    private static String resolveQuestTargetName(Quest quest, int questId, CGameObject targetObject) {
        if (questId == QUEST_ID_PACKED_UNIT_TARGET) {
            return resolvePackedQuestTargetName(quest.primaryArgument);
        }
        return targetObject == null ? "" : resolveQuestTargetName(targetObject);
    }

    /**
     * Native support extracted from packed unit-name branch for quest id `2` in
     * InnLeftPanelVisualObject::RenderSelectedEntry @00494832.
     * Fully ported.
     */
    private static String resolvePackedQuestTargetName(int packedUnitAndPortrait) {
        int unitType = packedUnitAndPortrait & 0xFF;
        int portraitVariant = packedUnitAndPortrait >>> 8;
        String unitName = get(UNITNAME, UnitNameText.byIndex(unitType));
        if (isMonsterType(unitType)) {
            return unitName;
        }
        return String.format(Locale.ROOT, "%s (%d)", unitName, portraitVariant);
    }

    /**
     * Native support extracted from unit/NPC target-name branches in
     * InnLeftPanelVisualObject::RenderSelectedEntry @00494832.
     * Fully ported for modeled CGameObject/CUnit targets.
     */
    private static String resolveQuestTargetName(CGameObject targetObject) {
        if (targetObject instanceof CUnit targetUnit
                && (targetUnit.unitFlags & UNIT_FLAG_DYNAMIC_INFO_PICTURE_MASK) != 0) {
            int npcNameIndex = Short.toUnsignedInt(targetUnit.serverID) - 1;
            return get(NPCNAMES, NpcNamesText.byIndex(npcNameIndex));
        }

        String unitName = get(UNITNAME, UnitNameText.byIndex(targetObject.type));
        if (isMonsterType(targetObject.type)) {
            return unitName;
        }
        return String.format(Locale.ROOT, "%s (%d)", unitName, targetObject.field8_0x28);
    }

    /**
     * Native support extracted from monster-type name-format branch in
     * InnLeftPanelVisualObject::RenderSelectedEntry @00494832.
     * Fully ported.
     */
    private static boolean isMonsterType(int unitType) {
        return unitType >= MONSTER_TYPE_FIRST && unitType <= MONSTER_TYPE_LAST;
    }

    /**
     * Native support extracted from secondary object region lookup in
     * InnLeftPanelVisualObject::RenderSelectedEntry @00494832.
     * Fully ported for modeled secondary-object lookup.
     */
    private static String resolveQuestRegionName(Quest quest) {
        CGameObject landmarkObject = resolveQuestSecondaryObject(quest);
        if (landmarkObject == null) {
            return "";
        }

        MapVisualObject mapVisualObject = Globals.mainWindow.pMapVisualObject;
        int regionX = ((landmarkObject.tileX - 8) * 5) / (mapVisualObject.cachedMapWidth - 0x10);
        int regionY = ((landmarkObject.tileY - 8) * 5) / (mapVisualObject.cachedMapHeight - 0x10);
        return get(MAIN_N_W_317 + regionX + regionY * 5);
    }

    /**
     * Native support extracted from secondary object building-name lookup in
     * InnLeftPanelVisualObject::RenderSelectedEntry @00494832.
     * Fully ported for modeled secondary-object lookup.
     */
    private static String resolveQuestLandmarkName(Quest quest) {
        CGameObject landmarkObject = resolveQuestSecondaryObject(quest);
        if (landmarkObject == null) {
            return "";
        }
        return get(BUILDING, BuildingText.byIndex(landmarkObject.type - 1));
    }

    /**
     * Native support extracted from MapVisualObject::m_ObjectMap lookup by Quest::getSecondaryIndexKey @0041E580
     * in InnLeftPanelVisualObject::RenderSelectedEntry @00494832.
     * Fully ported at the MapVisualObject object-map boundary.
     */
    private static CGameObject resolveQuestSecondaryObject(Quest quest) {
        return Globals.mainWindow.pMapVisualObject.getObjectByToken((short) quest.secondaryIndexKey);
    }

    /**
     * Native support extracted from CBitmapFont::FUN_0045D693 @0045D693 call inside
     * InnLeftPanelVisualObject::RenderSelectedEntry @00494832.
     * Fully ported as a native font-layout call boundary.
     */
    private static void drawQuestObjectiveText(CRect screenRect, String objectiveText) {
        CRect textRect = new CRect(
                screenRect.left + 0x12,
                screenRect.top + 0x32,
                screenRect.left + 0x96,
                screenRect.top + 0x120
        );
        Globals.fonts.font2.drawWrappedJustifiedTextShadowed(textRect, objectiveText, Palettes.yellowish,
                QUEST_TEXT_LINE_SPACING);
    }

    /**
     * Native support extracted from CUnit::GetFullStatsTooltipText @0046B9F0 call inside
     * InnLeftPanelVisualObject::GetText @00495BFD.
     * Fully ported as a native call boundary.
     */
    private static String resolveEntryPanelTooltip(CUnit selectedEntry, int localX, int localY) {
        return selectedEntry.getFullStatsTooltipText(localX, localY);
    }

    /**
     * Native support extracted from TokenEntry::resolveTooltipText @0043901F call inside
     * InnLeftPanelVisualObject::GetText @00495BFD.
     * Fully ported through the modeled TokenEntry tooltip path.
     */
    private static String resolveEntryHotspotTooltip(CUnit selectedEntry, int hotspotIndex) {
        TokenEntry token = selectedEntry.equipmentTokenEntries[hotspotIndex - 1];
        return token.resolveTooltipText();
    }

    /**
     * Native support extracted from Sound::PlayPointer @00438570 call in
     * InnLeftPanelVisualObject::SelectEntry @004946B6.
     * Fully ported at the Java Sound wrapper boundary.
     */
    private static void playSoundPointer(Sound sound) {
        if (sound != null) {
            sound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
        }
    }
}
