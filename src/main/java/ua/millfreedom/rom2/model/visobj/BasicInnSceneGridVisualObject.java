package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.CA16;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CBmp64kFrameSequence;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.TokenEntry;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.quest.Quest;
import ua.millfreedom.rom2.model.sound.Sound;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.res.Resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_ALLY_358;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_GIVEN_344;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_HIRED_257;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_MAGIC_354;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_MONEY_74;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_SELECTED_355;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_UPGRADE_353;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_XP_46;

/**
 * Native class: BasicInnSceneGridVisualObject (vtbl @0x005CE568).
 * Purpose: dialog `0x44C` grid/table panel with animated inn scene and reward cells.
 */
public class BasicInnSceneGridVisualObject extends CVisualObject {
    public static final int NATIVE_SIZE = 0x3CC; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int GRID_COLUMNS = 6;
    private static final int GRID_ROWS = 3;
    private static final int REWARD_COLUMNS = 3;
    private static final int REWARD_ROWS = 3;
    private static final String AVAILABLE_ENTRY_BACKDROP_BMP = "graphics/interface/inn/manback.bmp";
    private static final String RESERVED_ENTRY_BACKDROP_BMP = "graphics/interface/inn/manbacktalk.bmp";
    private static final String CENTER_AREA_BMP = "graphics/interface/inn/centerarea.bmp";
    private static final String LEFT_UPPER_OVERLAY_BMP = "graphics/interface/inn/luover.bmp";
    private static final String LEFT_LOWER_OVERLAY_BMP = "graphics/interface/inn/ldover.bmp";
    private static final String RIGHT_UPPER_OVERLAY_BMP = "graphics/interface/inn/ruover.bmp";
    private static final String UNIT_ENTRY_SPRITE_TEMPLATE = "graphics/interface/inn/unit%d/sprites.16a";
    private static final String HERO_FIGHTER_ENTRY_SPRITE = "graphics/interface/inn/herofighter/sprites.16a";
    private static final String HERO_MAGE_ENTRY_SPRITE = "graphics/interface/inn/heromage/sprites.16a";
    private static final String REWARD_EFFECT_SPRITE_TEMPLATE = "graphics/inventory/%s.16a";
    private static final int UNIT_FLAG_HUMANOID = 0x01;
    private static final int UNIT_FLAG_MAGIC_CLASS = 0x02;
    private static final int INN_ENTRY_HIRED_FLAG = 0x80000000;
    private static final int REWARD_EFFECT_ALLY_SENTINEL = 0xFFFD;
    private static final int REWARD_EFFECT_XP_SENTINEL = 0xFFFE;
    private static final int REWARD_EFFECT_MONEY_SENTINEL = 0xFFFF;
    private static final int REWARD_EFFECT_UNIT_VALUE = 0xFA;
    private static final int REWARD_TOKEN_MAGIC_TRAIL_FLAG = 0x20;
    private static final int MAGIC_TRAIL_RANDOM_OFFSET_MASK = 0x3FF;
    private static final int MAGIC_TRAIL_RED = 0xFF;
    private static final int MAGIC_TRAIL_GREEN = 0;
    private static final int MAGIC_TRAIL_BLUE = 0xFF;

    private static boolean sceneTimersInitialized;
    protected static long selectedFrameAdvanceTick;
    protected static long sceneFrameAdvanceTick;
    protected static long innKeeperAnimationTick;
    protected static long ambientLoopSoundTick;
    protected static int innKeeperIdleDelay;
    protected static int innKeeperAnimationMode;
    protected static int innKeeperDrinkDirection;

    //0x5c
    public BasicInnDialogVisualObject ownerDialog;
    //0x60
    public final CRect[] gridCellRects = new CRect[GRID_COLUMNS * GRID_ROWS];
    //0x180
    public final CRect[] rewardCellRects = new CRect[REWARD_COLUMNS * REWARD_ROWS];
    //0x210
    public final List<CA16> rewardEffectBitmaps = new ArrayList<>();
    //0x224
    public final List<CA16> availableEntryBitmaps = new ArrayList<>();
    //0x238
    public final List<CA16> reservedEntryBitmaps = new ArrayList<>();
    //0x24c
    public final List<Integer> availableEntryFrameIndices = new ArrayList<>();
    //0x260
    public final List<Integer> reservedEntryFrameIndices = new ArrayList<>();
    //0x274
    public CBmp64k availableEntryBackdropBitmap;
    //0x278
    public CBmp64k reservedEntryBackdropBitmap;
    //0x27c
    public CBmp64k centerAreaBitmap;
    //0x280
    public final CBmp64kFrameSequence candleFrames = new CBmp64kFrameSequence();
    //0x2b0
    public final CBmp64kFrameSequence cauldronFrames = new CBmp64kFrameSequence();
    //0x2e0
    public final CBmp64kFrameSequence innKeeperBreathFrames = new CBmp64kFrameSequence();
    //0x310
    public final CBmp64kFrameSequence innKeeperDrinkFrames = new CBmp64kFrameSequence();
    //0x340
    public CBmp64k leftUpperOverlayBitmap;
    //0x344
    public CBmp64k leftLowerOverlayBitmap;
    //0x348
    public CBmp64k rightUpperOverlayBitmap;
    //0x34c
    public final CBmp64k[] questIconBitmaps = new CBmp64k[0xD];

    /**
     * Native: BasicInnSceneGridVisualObject::BasicInnSceneGridVisualObject @004974E3.
     * Fully ported at the Java model-state level; native vtable installation and allocation mechanics are not emulated.
     */
    public BasicInnSceneGridVisualObject() {
        super();
        initializeBasicInnSceneGrid();
    }

    /**
     * Native: BasicInnSceneGridVisualObject::BasicInnSceneGridVisualObject @00497606.
     * Fully ported at the Java model-state level; native vtable installation and allocation mechanics are not emulated.
     */
    public BasicInnSceneGridVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            BasicInnDialogVisualObject ownerDialog
    ) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        this.ownerDialog = ownerDialog;
        initializeBasicInnSceneGrid();
    }

    /**
     * Native: BasicInnSceneGridVisualObject::InitializeBasicInnSceneGrid @0049783C.
     * Fully ported.
     */
    private void initializeBasicInnSceneGrid() {
        rewardEffectBitmaps.clear();
        availableEntryBitmaps.clear();
        reservedEntryBitmaps.clear();
        availableEntryFrameIndices.clear();
        reservedEntryFrameIndices.clear();

        int anchorX = cRect.left + 0x10;
        int anchorY = cRect.bottom;

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLUMNS; col++) {
                int left = anchorX + (col * 0x30);
                int top = anchorY - ((row + 1) * 0x40);
                gridCellRects[row * GRID_COLUMNS + col] = new CRect(left, top, left + 0x30, top + 0x40);
            }
        }

        for (int row = 0; row < REWARD_ROWS; row++) {
            for (int col = 0; col < REWARD_COLUMNS; col++) {
                int left = anchorX + 0x18 + (col * 0x50);
                int top = anchorY - ((row + 1) * 0x50);
                rewardCellRects[row * REWARD_COLUMNS + col] = new CRect(left, top, left + 0x50, top + 0x50);
            }
        }

        availableEntryBackdropBitmap = null;
        reservedEntryBackdropBitmap = null;
        centerAreaBitmap = null;
        leftUpperOverlayBitmap = null;
        leftLowerOverlayBitmap = null;
        rightUpperOverlayBitmap = null;
        for (int index = 0; index < questIconBitmaps.length; index++) {
            questIconBitmaps[index] = null;
        }
    }

    /**
     * vtbl +0x14: BasicInnSceneGridVisualObject::GetText @004995DA.
     * Fully ported. Native stores TokenEntry pointers in BasicInnDialogVisualObject::activeRewardTokenEntries
     * despite the old decompiler `SpellEffect` cast, as seen in MapVisualObject::HandleGameAction @0040D9B2.
     */
    @Override
    public String getText() {
        int localX = Globals.mousePointer.getX() - ownerDialog.cRect.left;
        int localY = Globals.mousePointer.getY() - ownerDialog.cRect.top;
        int rewardCount = ownerDialog.activeRewardTokenEntries.size();
        for (int index = 0; index < rewardCount; index++) {
            if (!rewardCellRects[index].contains(localX, localY)) {
                continue;
            }

            TokenEntry rewardToken = ownerDialog.activeRewardTokenEntries.get(index);
            int rewardTokenKind = getRewardTokenKind(rewardToken);
            if (rewardTokenKind == REWARD_EFFECT_ALLY_SENTINEL) {
                return get(MAIN_ALLY_358);
            }
            if (rewardTokenKind == REWARD_EFFECT_XP_SENTINEL) {
                return get(MAIN_XP_46);
            }
            if (rewardTokenKind == REWARD_EFFECT_MONEY_SENTINEL) {
                return get(MAIN_MONEY_74);
            }
            return rewardToken.resolveTooltipText();
        }

        return null;
    }

    /**
     * vtbl +0x2C: BasicInnSceneGridVisualObject::Update @00497ADF.
     * Fully ported.
     */
    @Override
    public void update() {
        long now = System.currentTimeMillis();
        initializeSceneTimers(now);

        if (now - ambientLoopSoundTick > 10_000L) {
            playSoundPointer(ownerDialog.steamSound);
            ambientLoopSoundTick = now;
        }

        int ownerLeft = ownerDialog.cRect.left;
        int ownerTop = ownerDialog.cRect.top;

        Globals.renderer.lockSurface();
        try {
            centerAreaBitmap.draw(ownerLeft + cRect.left, ownerTop + cRect.top, 0, 0, false);
            if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN) {
                renderInnScene(ownerLeft, ownerTop, now);
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
    }

    /**
     * Native support extracted from CVisualObject::Update tail calls in BasicInnSceneGridVisualObject::Update @00497ADF
     * DruidInnSceneGridVisualObject::Update @0049CF22, and KaargInnSceneGridVisualObject::Update @0049F467.
     * Fully ported.
     */
    protected final void updateChildVisualObjects() {
        super.update();
    }

    /**
     * vtbl +0x4C: BasicInnSceneGridVisualObject::OnMouseMove @0049A188.
     * Fully ported. Native method returns `0`.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        return 0;
    }

    /**
     * vtbl +0x54: BasicInnSceneGridVisualObject::OnLButtonDown @0049A347.
     * Fully ported.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        int selectionId = getSelectionIndexAtScreenPoint(x, y);
        if (selectionId == -1) {
            return 0;
        }

        ownerDialog.leftPanelVisual.selectEntry(selectionId);
        return 1;
    }

    /**
     * vtbl +0x5C: BasicInnSceneGridVisualObject::OnLButtonDblClk @0049A197.
     * Fully ported.
     */
    @Override
    public int onLButtonDblClk(int nFlags, int x, int y) {
        if (Globals.mainWindow.sessionMode != CMainWindow.SESSION_MODE_CAMPAIGN
                && ownerDialog.hasQuestToggleTarget()) {
            ownerDialog.pendingInnQuestSelectionToggle = ownerDialog.pendingInnQuestSelectionToggle == 0 ? 1 : 0;
            return 1;
        }

        if (onLButtonDown(nFlags, x, y) == 0) {
            return 0;
        }

        if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN) {
            int selectedIndex = ownerDialog.innEntrySelectionIndex;
            if (selectedIndex < ownerDialog.availableInnEntries.size()) {
                CUnit selectedEntry = ownerDialog.availableInnEntries.get(selectedIndex);
                if (isAvailableEntryMarkedHired(selectedEntry)) {
                    ownerDialog.dismissSelectedHiredInnEntry();
                } else {
                    ownerDialog.acceptSelectedInnEntryOffer();
                }
                ownerDialog.actionPanelVisual.refreshPrimaryActionLabel();
            } else {
                ownerDialog.handleReservedEntry();
            }
        } else {
            int selectedQuestKey = ownerDialog.questContext;
            if (ownerDialog.questSelectionIndex == selectedQuestKey) {
                ownerDialog.questSelectionIndex = -1;
            } else {
                ownerDialog.questSelectionIndex = selectedQuestKey;
            }
        }

        return 1;
    }

    /**
     * vtbl +0x78: BasicInnSceneGridVisualObject::LoadEntryBitmaps @00499783.
     * Native array sizing, sprite path construction, palette setup, and resource binding are ported.
     * Fully ported.
     */
    public void loadEntryBitmaps() {
        availableEntryBitmaps.clear();
        availableEntryFrameIndices.clear();
        reservedEntryBitmaps.clear();
        reservedEntryFrameIndices.clear();

        for (CUnit entry : ownerDialog.availableInnEntries) {
            availableEntryBitmaps.add(loadEntrySprite(String.format(UNIT_ENTRY_SPRITE_TEMPLATE, entry.type)));
            availableEntryFrameIndices.add(0);
        }

        for (CUnit entry : ownerDialog.reservedInnEntries) {
            reservedEntryBitmaps.add(loadEntrySprite(resolveReservedEntrySpritePath(entry)));
            reservedEntryFrameIndices.add(0);
        }
    }

    /**
     * Native support extracted from BasicInnSceneGridVisualObject::LoadEntryBitmaps @00499783.
     */
    private static String resolveReservedEntrySpritePath(CUnit entry) {
        if ((entry.unitFlags & UNIT_FLAG_HUMANOID) == 0) {
            return String.format(UNIT_ENTRY_SPRITE_TEMPLATE, entry.type);
        }
        return (entry.unitFlags & UNIT_FLAG_MAGIC_CLASS) == 0
                ? HERO_FIGHTER_ENTRY_SPRITE
                : HERO_MAGE_ENTRY_SPRITE;
    }

    /**
     * Native support extracted from BasicInnSceneGridVisualObject::Update @00497ADF and BasicInnDialogVisualObject::FindInnEntryIdIndex @0049AB92.
     */
    private boolean isAvailableEntryMarkedHired(CUnit entry) {
        int entryIndex = ownerDialog.findInnEntryIdIndex(entry);
        return (ownerDialog.innEntryIds.get(entryIndex) & INN_ENTRY_HIRED_FLAG) != 0;
    }

    /**
     * vtbl +0x7C: BasicInnSceneGridVisualObject::ReleaseEntryBitmaps @00499A5F.
     * Fully ported. Java clears retained sprite references and frame indices instead of modeling native delete/free calls.
     */
    public void releaseEntryBitmaps() {
        availableEntryBitmaps.clear();
        availableEntryFrameIndices.clear();
        reservedEntryBitmaps.clear();
        reservedEntryFrameIndices.clear();
    }

    /**
     * vtbl +0x80: BasicInnSceneGridVisualObject::LoadPanelBitmaps @00499B6A.
     * Native bitmap load order and non-campaign quest-strip population are ported.
     * Fully ported.
     */
    public void loadPanelBitmaps() {
        releasePanelBitmaps();

        availableEntryBackdropBitmap = new CBmp64k(AVAILABLE_ENTRY_BACKDROP_BMP);
        Globals.renderer.refreshMousePointer();
        reservedEntryBackdropBitmap = new CBmp64k(RESERVED_ENTRY_BACKDROP_BMP);
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
                questIconBitmaps[index] = new CBmp64k(String.format("graphics/interface/inn/quests/%02d.bmp", index + 1));
            }
        }
    }

    /**
     * vtbl +0x84: BasicInnSceneGridVisualObject::ReleasePanelBitmaps @00499EA8.
     * Fully ported. Java clears retained bitmap references instead of reproducing native delete/free calls.
     */
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
     * Native helper: BasicInnSceneGridVisualObject::GetSelectionIndexAtScreenPoint @00499367.
     * Fully ported.
     */
    private int getSelectionIndexAtScreenPoint(int x, int y) {
        int localX = x - ownerDialog.cRect.left;
        int localY = y - ownerDialog.cRect.top;

        if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN) {
            int totalEntries = ownerDialog.availableInnEntries.size() + ownerDialog.reservedInnEntries.size();
            for (int index = 0; index < totalEntries; index++) {
                if (gridCellRects[index].contains(localX, localY)) {
                    return index;
                }
            }
            return -1;
        }

        if (ownerDialog.getQuestCount() == 0) {
            int rewardCount = ownerDialog.activeRewardTokenEntries.size();
            for (int index = 0; index < rewardCount; index++) {
                if (rewardCellRects[index].contains(localX, localY)) {
                    return index;
                }
            }
            return -1;
        }

        return ownerDialog.findQuestSelectionIdAtPoint(gridCellRects, localX, localY);
    }

    /**
     * Native branch helper inside BasicInnSceneGridVisualObject::Update @00497ADF.
     * Native draw order, frame stepping, and selection animation for the basic inn scene are ported.
     */
    private void renderInnScene(int ownerLeft, int ownerTop, long now) {
        candleFrames.drawCurrentFrame(ownerLeft + cRect.left, ownerTop + cRect.top + 0x30);
        cauldronFrames.drawCurrentFrame(ownerLeft + cRect.left + 0x104, ownerTop + cRect.top + 0xA0);
        if (now - sceneFrameAdvanceTick > 100L) {
            candleFrames.advanceLooped();
            cauldronFrames.advanceLooped();
            sceneFrameAdvanceTick = now;
        }

        renderInnKeeperScene(ownerLeft, ownerTop, now);
        renderAvailableEntries(ownerLeft, ownerTop, now);
        renderReservedEntries(ownerLeft, ownerTop, now);
    }

    /**
     * Native helper: BasicInnSceneGridVisualObject::RenderQuestRewardGrid @004984D3.
     * Fully ported.
     */
    protected void renderQuestRewardGrid(int ownerLeft, int ownerTop) {
        MapVisualObject mapVisualObject = Globals.mainWindow.pMapVisualObject;
        int pendingQuestKey = mapVisualObject.questStorage.findQuestKeyByMessage(
                Quest.MESSAGE_INN_PROBE,
                mapVisualObject.currentPlayer.playerId,
                ownerDialog.innInteractionTargetTokenId
        );
        if (pendingQuestKey != 0) {
            Quest quest = mapVisualObject.questStorage.findQuestByKey(pendingQuestKey);
            renderQuestIcon(ownerLeft, ownerTop, 0, quest);
            if (ownerDialog.pendingInnQuestSelectionToggle == 0) {
                drawQuestGridLabel(ownerLeft, ownerTop, 0, get(MAIN_GIVEN_344), Palettes.redish);
            }
            return;
        }

        if (ownerDialog.questsStorage.questsByKey.size() != 0) {
            int gridIndex = 0;
            for (Map.Entry<Integer, Quest> questEntry : ownerDialog.questsStorage.questsByKey.entrySet()) {
                renderQuestIcon(ownerLeft, ownerTop, gridIndex, questEntry.getValue());
                if (ownerDialog.questSelectionIndex == questEntry.getKey()) {
                    drawQuestGridLabel(ownerLeft, ownerTop, gridIndex, get(MAIN_GIVEN_344), Palettes.redish);
                }
                gridIndex++;
            }
            return;
        }

        renderRewardEffects(ownerLeft, ownerTop);
    }

    /**
     * Native support extracted from BasicInnSceneGridVisualObject::RenderQuestRewardGrid @004984D3 quest icon draws.
     * Fully ported.
     */
    private void renderQuestIcon(int ownerLeft, int ownerTop, int gridIndex, Quest quest) {
        CRect cellRect = gridCellRects[gridIndex];
        questIconBitmaps[quest.getId() - 1].draw(ownerLeft + cellRect.left, ownerTop + cellRect.top, 0, 0, false);
    }

    /**
     * Native support extracted from BasicInnSceneGridVisualObject::RenderQuestRewardGrid @004984D3 centered quest labels.
     * Fully ported.
     */
    private void drawQuestGridLabel(int ownerLeft, int ownerTop, int gridIndex, String text, Palette16 palette) {
        CRect cellRect = gridCellRects[gridIndex];
        Globals.fonts.font2.drawTextShadowed(
                ownerLeft + cellRect.left + cellRect.width() / 2,
                ownerTop + cellRect.top + cellRect.height() / 2,
                text,
                TextAlign.combine(TextAlign.CENTER, TextAlign.VERTICAL_CENTER),
                palette,
                1
        );
    }

    /**
     * Native support extracted from BasicInnSceneGridVisualObject::RenderQuestRewardGrid @004984D3 reward-cell branch.
     * Fully ported.
     */
    private void renderRewardEffects(int ownerLeft, int ownerTop) {
        int rewardCount = ownerDialog.activeRewardTokenEntries.size();
        for (int rewardIndex = 0; rewardIndex < rewardCount; rewardIndex++) {
            CRect cellRect = rewardCellRects[rewardIndex];
            int drawX = ownerLeft + cellRect.left;
            int drawY = ownerTop + cellRect.top;
            GUI.backInv.draw(drawX, drawY, 0, 0, false);

            if (ownerDialog.questContext == rewardIndex) {
                Globals.renderer.applyShadeAdditiveToRect(drawX, drawY, drawX + 0x50, drawY + 0x50, 2);
            }

            ensureRewardEffectBitmapSlot(rewardIndex);
            TokenEntry rewardToken = ownerDialog.activeRewardTokenEntries.get(rewardIndex);
            int rewardEffectKind = getRewardTokenKind(rewardToken);
            if (rewardEffectKind == REWARD_EFFECT_ALLY_SENTINEL) {
                drawRewardCenteredText(drawX, drawY + 0x23, get(MAIN_ALLY_358), Palettes.yellowish);
            } else if (rewardEffectKind == REWARD_EFFECT_XP_SENTINEL) {
                drawRewardCenteredText(drawX, drawY + 0x23, get(MAIN_XP_46), Palettes.yellowish);
                drawRewardQuantityText(
                        drawX,
                        drawY,
                        Utils.formatDecimalThousands(
                                getRewardTokenQuantity(rewardToken) * REWARD_EFFECT_UNIT_VALUE
                        )
                );
            } else if (rewardEffectKind == REWARD_EFFECT_MONEY_SENTINEL) {
                GUI.sprMoney.draw(drawX, drawY, 0, 0, false);
                drawRewardQuantityText(
                        drawX,
                        drawY,
                        Utils.formatDecimalThousands(
                                getRewardTokenQuantity(rewardToken) * REWARD_EFFECT_UNIT_VALUE
                        )
                );
            } else {
                renderRegularRewardEffect(ownerLeft, ownerTop, rewardIndex, rewardToken, cellRect);
            }

            if (ownerDialog.questSelectionIndex == rewardIndex) {
                drawRewardCenteredText(drawX, drawY + 0x0F, get(MAIN_SELECTED_355), Palettes.grayDim);
            }
        }
    }

    /**
     * Native support extracted from BasicInnSceneGridVisualObject::RenderQuestRewardGrid @004984D3 regular token branch.
     * Fully ported.
     */
    private void renderRegularRewardEffect(
            int ownerLeft,
            int ownerTop,
            int rewardIndex,
            TokenEntry rewardToken,
            CRect cellRect
    ) {
        CA16 sprite = rewardEffectBitmaps.get(rewardIndex);
        if (sprite == null) {
            sprite = loadRewardEffectSprite(rewardToken);
            rewardEffectBitmaps.set(rewardIndex, sprite);
        }

        int drawX = ownerLeft + cellRect.left;
        int drawY = ownerTop + cellRect.top;
        sprite.draw(drawX, drawY, 0, 0, false);

        int quantity = getRewardTokenQuantity(rewardToken);
        if (quantity > 1) {
            drawRewardQuantityText(drawX, drawY, String.valueOf(quantity));
        }

        if ((rewardToken.wireFlags & REWARD_TOKEN_MAGIC_TRAIL_FLAG) != 0) {
            drawRewardMagicTrailDots(drawX, drawY, rewardIndex);
        }

        if (rewardIndex > 1
                && getRewardTokenKind(ownerDialog.activeRewardTokenEntries.get(rewardIndex - 1)) > 0xFFFC) {
            drawRewardCenteredText(drawX, drawY + 0x23, get(MAIN_UPGRADE_353), Palettes.yellowish);
            drawRewardCenteredText(drawX, drawY + 0x2D, get(MAIN_MAGIC_354), Palettes.yellowish);
        }
    }

    /**
     * Native support extracted from BasicInnSceneGridVisualObject::RenderQuestRewardGrid @004984D3 CArray SetSize path.
     * Fully ported.
     */
    private void ensureRewardEffectBitmapSlot(int rewardIndex) {
        while (rewardEffectBitmaps.size() <= rewardIndex) {
            rewardEffectBitmaps.add(null);
        }
    }

    /**
     * Native support extracted from BasicInnSceneGridVisualObject::RenderQuestRewardGrid @004984D3 inventory CA16 load.
     * Fully ported.
     */
    private static CA16 loadRewardEffectSprite(TokenEntry rewardToken) {
        CA16 sprite = new CA16(Resources.path(String.format(
                REWARD_EFFECT_SPRITE_TEMPLATE,
                rewardToken.getEquipmentPortraitResourceName()
        )));
        sprite.initPalette(0x10, 4, 0);
        return sprite;
    }

    /**
     * Native support extracted from BasicInnSceneGridVisualObject::RenderQuestRewardGrid @004984D3 sentinel checks and
     * BasicInnSceneGridVisualObject::GetText @004995DA.
     * Fully ported.
     */
    private static int getRewardTokenKind(TokenEntry rewardToken) {
        return rewardToken.packedTokenHash & 0xFFFF;
    }

    /**
     * Native support extracted from BasicInnSceneGridVisualObject::RenderQuestRewardGrid @004984D3 offset +0x10 reads.
     * Fully ported.
     */
    private static int getRewardTokenQuantity(TokenEntry rewardToken) {
        return rewardToken.quantity;
    }

    /**
     * Native support extracted from BasicInnSceneGridVisualObject::RenderQuestRewardGrid @004984D3 reward-cell text draws.
     * Fully ported.
     */
    private static void drawRewardCenteredText(int drawX, int drawY, String text, Palette16 palette) {
        Globals.fonts.font2.drawTextShadowed(
                drawX + 0x28,
                drawY,
                text,
                TextAlign.CENTER.mask,
                palette,
                1
        );
    }

    /**
     * Native support extracted from BasicInnSceneGridVisualObject::RenderQuestRewardGrid @004984D3 reward quantity draws.
     * Fully ported.
     */
    private static void drawRewardQuantityText(int drawX, int drawY, String text) {
        Globals.fonts.font2.drawTextShadowed(
                drawX + 3,
                drawY + 0x42,
                text,
                TextAlign.DEFAULT.mask,
                Palettes.yellowish,
                1
        );
    }

    /**
     * Native support extracted from BasicInnSceneGridVisualObject::RenderQuestRewardGrid @004984D3 magic reward trail.
     * Fully ported.
     */
    private static void drawRewardMagicTrailDots(int drawX, int drawY, int rewardIndex) {
        HeroInventoryControlVisualObject inventory = Globals.mainWindow.pHeroInventoryControlVisualObject;
        int frame = (int) (Globals.currentTickMillis() / 0x78L);
        drawRewardMagicTrailDot(inventory, drawX, drawY, frame, 0x3F);
        if (frame > 0) {
            drawRewardMagicTrailDot(inventory, drawX, drawY, frame - 1, 0x7F);
        }
        if (frame > 1) {
            drawRewardMagicTrailDot(inventory, drawX, drawY, frame - 2, 0xBF);
        }
        if (frame > 2) {
            drawRewardMagicTrailDot(inventory, drawX, drawY, frame - 3, 0xFF);
        }
        if (frame > 3) {
            drawRewardMagicTrailDot(inventory, drawX, drawY, frame - 4, 0xBF);
        }
        if (frame > 4) {
            drawRewardMagicTrailDot(inventory, drawX, drawY, frame - 5, 0x7F);
        }
        if (frame > 5) {
            drawRewardMagicTrailDot(inventory, drawX, drawY, frame - 6, 0x3F);
        }
        inventory.drawMagicTrailDots(drawX, drawY, rewardIndex);
    }

    /**
     * Native support extracted from BasicInnSceneGridVisualObject::RenderQuestRewardGrid @004984D3 magic reward trail dot draws.
     * Fully ported.
     */
    private static void drawRewardMagicTrailDot(
            HeroInventoryControlVisualObject inventory,
            int baseX,
            int baseY,
            int frame,
            int alpha
    ) {
        int randomIndex = frame & MAGIC_TRAIL_RANDOM_OFFSET_MASK;
        Globals.renderer.drawSoftDot(
                baseX + inventory.cellRandomOffsets1.get(randomIndex),
                baseY + inventory.cellRandomOffsets2.get(randomIndex),
                MAGIC_TRAIL_RED,
                MAGIC_TRAIL_GREEN,
                MAGIC_TRAIL_BLUE,
                alpha
        );
    }

    /**
     * Native helper branch inside BasicInnSceneGridVisualObject::Update @00497ADF.
     * Fully ported.
     */
    private void renderInnKeeperScene(int ownerLeft, int ownerTop, long now) {
        if (innKeeperIdleDelay < now - innKeeperAnimationTick) {
            if ((innKeeperIdleDelay & 1) == 0) {
                innKeeperAnimationMode = 2;
                playSoundPointer(ownerDialog.chairSound);
                playSoundPointer(ownerDialog.selectionChangedSound);
            } else {
                innKeeperDrinkDirection = 1;
                innKeeperAnimationMode = 1;
            }
        }

        if (innKeeperAnimationMode == 1) {
            innKeeperDrinkFrames.drawCurrentFrame(ownerLeft + cRect.left + 0x50, ownerTop + cRect.top + 0x98);
            if (innKeeperDrinkFrames.getCurrentFrameIndex() == 0x1E) {
                playSoundPointer(ownerDialog.drinkSound);
            }
        } else if (innKeeperAnimationMode == 2) {
            innKeeperBreathFrames.drawCurrentFrame(ownerLeft + cRect.left + 0x50, ownerTop + cRect.top + 0x98);
        }

        if (innKeeperAnimationMode != 0 && now - innKeeperAnimationTick > 0x53L) {
            if (innKeeperAnimationMode == 1) {
                if (innKeeperDrinkDirection == -1) {
                    if (innKeeperDrinkFrames.rewind() == null) {
                        innKeeperDrinkDirection = 0;
                        innKeeperAnimationMode = 0;
                        innKeeperIdleDelay = nextInnKeeperIdleDelay();
                        playSoundPointer(ownerDialog.gulpSound);
                    }
                } else if (innKeeperDrinkDirection == 1 && innKeeperDrinkFrames.advance() == null) {
                    innKeeperDrinkDirection = -1;
                    innKeeperDrinkFrames.rewind();
                }
                innKeeperAnimationTick = now;
            } else if (innKeeperAnimationMode == 2) {
                if (innKeeperBreathFrames.advance() == null) {
                    innKeeperAnimationMode = 0;
                    innKeeperIdleDelay = nextInnKeeperIdleDelay();
                    innKeeperBreathFrames.setCurrentFrameIndex(0);
                }
                innKeeperAnimationTick = now;
            }
        }
    }

    /**
     * Native helper branch inside BasicInnSceneGridVisualObject::Update @00497ADF,
     * DruidInnSceneGridVisualObject::Update @0049CF22, and KaargInnSceneGridVisualObject::Update @0049F467.
     * Native available-entry placement, selection background, selected-frame stepping, sprite draw, and hired label draw are ported.
     */
    protected void renderAvailableEntries(int ownerLeft, int ownerTop, long now) {
        int availableCount = ownerDialog.availableInnEntries.size();
        for (int index = 0; index < availableCount; index++) {
            int row = index / GRID_COLUMNS;
            int col = index % GRID_COLUMNS;
            CRect cellRect = gridCellRects[row * GRID_COLUMNS + col];
            if (ownerDialog.innEntrySelectionIndex != -1) {
                availableEntryBackdropBitmap.draw(ownerLeft + cellRect.left, ownerTop + cellRect.top, 0, 0, false);

                if (ownerDialog.innEntrySelectionIndex == index) {
                    int selectedIndex = ownerDialog.innEntrySelectionIndex;
                    int frameIndex = availableEntryFrameIndices.get(selectedIndex);
                    CA16 sprite = availableEntryBitmaps.get(selectedIndex);
                    sprite.draw(ownerLeft + cellRect.left, ownerTop + cellRect.top, frameIndex, 0, false);
                    if (now - selectedFrameAdvanceTick > 0x7DL) {
                        availableEntryFrameIndices.set(selectedIndex, (frameIndex + 1) % sprite.frameCount);
                        selectedFrameAdvanceTick = now;
                    }
                } else {
                    int frameIndex = availableEntryFrameIndices.get(index);
                    availableEntryBitmaps.get(index).draw(
                            ownerLeft + cellRect.left,
                            ownerTop + cellRect.top,
                            frameIndex,
                            0,
                            false
                    );
                }

                if (isAvailableEntryMarkedHired(ownerDialog.availableInnEntries.get(index))) {
                    drawHiredLabel(ownerLeft + cellRect.left, ownerTop + cellRect.top, cellRect);
                }
            }
        }
    }

    /**
     * Native helper branch inside BasicInnSceneGridVisualObject::Update @00497ADF,
     * DruidInnSceneGridVisualObject::Update @0049CF22, and KaargInnSceneGridVisualObject::Update @0049F467.
     * Native reserved-entry placement, selection background, selected-frame stepping, and sprite draw are ported.
     */
    protected void renderReservedEntries(int ownerLeft, int ownerTop, long now) {
        int availableCount = ownerDialog.availableInnEntries.size();
        int reservedCount = ownerDialog.reservedInnEntries.size();
        for (int index = 0; index < reservedCount; index++) {
            int gridIndex = availableCount + index;
            int row = gridIndex / GRID_COLUMNS;
            int col = gridIndex % GRID_COLUMNS;
            CRect cellRect = gridCellRects[row * GRID_COLUMNS + col];
            if (ownerDialog.innEntrySelectionIndex != -1) {
                if (ownerDialog.innEntrySelectionIndex == gridIndex && now - selectedFrameAdvanceTick > 0x7DL) {
                    int frameIndex = reservedEntryFrameIndices.get(index);
                    CA16 sprite = reservedEntryBitmaps.get(index);
                    reservedEntryFrameIndices.set(index, (frameIndex + 1) % sprite.frameCount);
                    selectedFrameAdvanceTick = now;
                }

                reservedEntryBackdropBitmap.draw(ownerLeft + cellRect.left, ownerTop + cellRect.top, 0, 0, false);
                reservedEntryBitmaps.get(index).draw(
                        ownerLeft + cellRect.left,
                        ownerTop + cellRect.top,
                        reservedEntryFrameIndices.get(index),
                        0,
                        false
                );
            }
        }
    }

    /**
     * Native support extracted from BasicInnSceneGridVisualObject::Update @00497ADF static timer initialization.
     */
    private static void initializeSceneTimers(long now) {
        if (sceneTimersInitialized) {
            return;
        }
        sceneTimersInitialized = true;
        selectedFrameAdvanceTick = now;
        sceneFrameAdvanceTick = now;
        innKeeperAnimationTick = now;
        ambientLoopSoundTick = now;
        innKeeperIdleDelay = nextInnKeeperIdleDelay();
    }

    /**
     * Native support extracted from the `rand() >> 4 + 3000` delay math in BasicInnSceneGridVisualObject::Update @00497ADF.
     */
    private static int nextInnKeeperIdleDelay() {
        return Utils.randInclusive(3000, 5047);
    }

    /**
     * Native helper boundary: `MAIN_HIRED_257` text draw inside BasicInnSceneGridVisualObject::Update @00497ADF,
     * DruidInnSceneGridVisualObject::Update @0049CF22, and KaargInnSceneGridVisualObject::Update @0049F467.
     */
    private static void drawHiredLabel(
            int drawX,
            int drawY,
            CRect cellRect
    ) {
        Globals.fonts.font2.drawTextShadowed(
                drawX + cellRect.width() / 2,
                drawY + cellRect.height() / 2,
                get(MAIN_HIRED_257),
                TextAlign.combine(TextAlign.CENTER, TextAlign.VERTICAL_CENTER),
                Palettes.redish,
                1
        );
    }

    /**
     * Native support extracted from BasicInnSceneGridVisualObject::LoadEntryBitmaps @00499783.
     */
    private static CA16 loadEntrySprite(String resourcePath) {
        CA16 sprite = new CA16(Resources.path(resourcePath));
        sprite.initPalette(0x10, 4, 0);
        return sprite;
    }

    /**
     * Native support extracted from Sound::PlayPointer call sites in BasicInnSceneGridVisualObject::Update @00497ADF.
     */
    protected static void playSoundPointer(Sound sound) {
        if (sound != null) {
            sound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
        }
    }

}
