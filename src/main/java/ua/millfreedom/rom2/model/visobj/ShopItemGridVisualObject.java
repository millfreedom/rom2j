package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.*;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.sound.Sound;
import ua.millfreedom.rom2.model.sound.SoundSystem;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.res.Resources;

import java.awt.Point;
import java.util.List;

import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_MONEY_74;

/**
 * Native class: ShopItemGridVisualObject.
 * Purpose: shop-dialog item-slot grid base with shared hit-testing and drag/drop handling across catalog, selection, and transfer panels.
 */
public class ShopItemGridVisualObject extends GridOverlayVisualObject {
    public static final int NATIVE_SIZE = 0x20C0; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final String GRAPHICS_DIRECTORY = "graphics";
    private static final String INTERFACE_DIRECTORY = "interface";
    private static final String INVENTORY_DIRECTORY = "inventory";
    private static final String MONEY_DIRECTORY = "money";
    private static final String MONEY_SPRITE_FILE = "money.16a";
    private static final String A16_SUFFIX = ".16a";
    private static final String SFX_PUT_ON_WAV = "SFX/Put On.wav";
    private static final String SFX_PUT_OFF_WAV = "SFX/Put Off.wav";
    private static final String SFX_SCROLL_WAV = "SFX/Scroll.wav";
    private static final int CATALOG_SUBTYPE_MODE_BASE = 5;
    private static final int SUBTYPE_GRID_MODE_MIN = 5;
    private static final int SUBTYPE_GRID_MODE_MAX = 8;
    private static final int VALUE_BADGE_SHADOW_BRIGHTNESS = 8;
    private static final int VALUE_ATTRIBUTE_ID = 1;
    private static final int DYNAMIC_PAYLOAD_FLAG = 0x20;

    //0x20ac
    public ShopDialogVisualObject ownerDialog;
    //0x20b0
    public Sound primaryGridSound;
    //0x20b4
    public Sound putOnSound;
    //0x20b8
    public Sound putOffSound;
    //0x20bc
    public Sound scrollSound;

    /**
     * Native: ShopItemGridVisualObject::ShopItemGridVisualObject @004B10D0.
     * Full port.
     */
    public ShopItemGridVisualObject() {
        super();
        initializeShopItemGrid(null);
    }

    /**
     * Native: ShopItemGridVisualObject::ShopItemGridVisualObject @004B11A8.
     * Full port.
     */
    public ShopItemGridVisualObject(int id, CRect rect, ShopDialogVisualObject ownerDialog) {
        super(id, rect);
        initializeShopItemGrid(ownerDialog);
    }

    /**
     * Native: ShopItemGridVisualObject::ShopItemGridVisualObject @004B127C.
     * Full port.
     */
    public ShopItemGridVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            ShopDialogVisualObject ownerDialog
    ) {
        super(id, xLeft, yTop, xRight, yBottom);
        initializeShopItemGrid(ownerDialog);
    }

    /**
     * Native support extracted from ShopItemGridVisualObject constructors @004B10D0, @004B11A8, and @004B127C.
     * Full support port.
     */
    private void initializeShopItemGrid(ShopDialogVisualObject ownerDialog) {
        this.ownerDialog = ownerDialog;
        this.gridSource = null;
        this.m_nState |= 0x2;
        this.visibleStartRef = new int[]{0};
        this.primaryGridSound = null;
        this.putOnSound = null;
        this.putOffSound = null;
        this.scrollSound = null;
    }

    /**
     * vtbl +0x14: ShopItemGridVisualObject::GetText @004B14D4.
     * Full port.
     */
    @Override
    public String getText() {
        if (ownerDialog.dialogActiveFlag == 0) {
            return null;
        }

        int gridIndex = getGridIndexAtScreenPoint(Globals.mousePointer.getX(), Globals.mousePointer.getY());
        Object entry = getGridSourceEntry(gridIndex);
        if (entry == null) {
            return null;
        }
        if (isMoneyEntry(entry)) {
            return get(MAIN_MONEY_74);
        }
        if (isCatalogEntrySpriteRefreshSuppressed(entry)) {
            return null;
        }
        return resolveCatalogEntryTooltip(entry);
    }

    /**
     * vtbl +0x2C: ShopItemGridVisualObject::Update @004B18E2.
     * Full port.
     */
    @Override
    public void update() {
        if (ownerDialog.dialogActiveFlag == 0) {
            return;
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        drawPanelBackground(screenRect);
        refreshVisibleCellSprites();
        if (gridSource == null) {
            return;
        }

        int visibleStart = getVisibleStartValue();
        int visibleSourceCount = Math.min(getGridSourceSize(), getVisibleCellCount());
        advanceAnimatedCells();
        for (int visibleIndex = 0; visibleIndex < getVisibleCellCount(); visibleIndex++) {
            if (getCellUpdateCounter(visibleIndex) == 0 || visibleIndex >= visibleSourceCount) {
                continue;
            }

            Object entry = getGridSourceEntry(visibleStart + visibleIndex);
            CRect cellRect = getCellRect(visibleIndex);
            if (entry == null || cellRect == null) {
                continue;
            }

            drawCatalogEntryBackground(entry, cellRect);
            if (!isMoneyEntry(entry) && !isCatalogEntrySpriteRefreshSuppressed(entry)) {
                drawCachedCatalogEntrySprite(visibleIndex, cellRect);
                drawCatalogEntryMagicTrail(entry, visibleIndex, cellRect);
            }

            int quantity = getCatalogEntryQuantity(entry);
            if (quantity > 1) {
                drawCatalogEntryQuantity(cellRect, quantity);
            }
            if (!isMoneyEntry(entry) && !isCatalogEntrySpriteRefreshSuppressed(entry)) {
                drawCatalogEntryValueBadge(cellRect, entry);
            }
        }

        ownerDialog.drawSelectionInfoPanelLeftBackdrop();
        ownerDialog.dirtyFlags |= 0x40;
    }

    /**
     * vtbl +0x4C: ShopItemGridVisualObject::OnMouseMove @004B242D.
     * Full port with a Java synthetic-money guard: this shop-grid path does not debit currentPlayer.gold before
     * GridOverlayVisualObject::CompleteUiDrag @004A24E8 restores carried money.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        int modeCode = getGridModeCode();
        if (ownerDialog.hoveredControlRegion != modeCode) {
            ownerDialog.hoveredControlRegion = modeCode;
            ownerDialog.ringButtons.initializeButtonState();
        }

        CMainWindow mainWindow = Globals.mainWindow;
        if ((nFlags & 1) == 0) {
            return 0;
        }

        int sourceIndex = mainWindow.getUiLockSourceIndex();
        if (sourceIndex < 0 || sourceIndex >= getGridSourceSize() || mainWindow.getUiLockPayload() != null) {
            return 0;
        }

        Object entry = getGridSourceEntry(sourceIndex);
        int dragQuantity = isShiftPressed(nFlags) ? getCatalogEntryQuantity(entry) : 1;
        if (isMoneyEntry(entry) || isCatalogEntryDragBlocked(entry)) {
            return 0;
        }

        Object payload = beginUiDrag(sourceIndex, dragQuantity);
        if (payload == null) {
            return 0;
        }

        beginUiLockDragVisual(sourceIndex);
        clampVisibleStart();
        resetCellUpdateCountersAndRefresh();
        draw();
        return 0;
    }

    /**
     * vtbl +0x54: ShopItemGridVisualObject::OnLButtonDown @004B2588.
     * Full port.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        CMainWindow mainWindow = Globals.mainWindow;
        if (mainWindow.getUiLockPayload() == null) {
            mainWindow.setUiLockSourceIndex(getGridIndexAtPoint(new Point(x, y)));
        }
        return 1;
    }

    /**
     * vtbl +0x58: ShopItemGridVisualObject::OnLButtonUp @004B25CB.
     * Full port.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        CMainWindow mainWindow = Globals.mainWindow;
        Object payload = mainWindow.getUiLockPayload();
        if (payload == null) {
            return 1;
        }

        CMousePointer.Cursor_Default.setToMousePointer();
        if (getCatalogEntryValue(payload) == 0 && getGridModeCode() != 2) {
            ownerDialog.uiLockPlacementAllowedFlag = 0;
        }
        if (ownerDialog.uiLockPlacementAllowedFlag == 0) {
            ownerDialog.cancelUiLockSelection();
            playPointerSound(ownerDialog.noFitSound);
            return 1;
        }

        int targetIndex = getGridIndexAtPoint(new Point(x, y));
        int targetModeCode = getGridModeCode();
        if (targetModeCode == 4) {
            completeUiDrag(targetIndex);
            ownerDialog.recomputeCatalogTransactionTotals();
            ownerDialog.dirtyFlags |= 0x20;
        } else if (targetModeCode < SUBTYPE_GRID_MODE_MIN || targetModeCode > SUBTYPE_GRID_MODE_MAX) {
            completeUiDrag(targetIndex);
        } else if (targetModeCode == getCatalogEntryType(payload)) {
            completeUiDrag(targetIndex);
        } else {
            ownerDialog.cancelUiLockSelection();
        }

        clearUiLockDragState();
        resetCellUpdateCountersAndRefresh();
        draw();
        return 1;
    }

    /**
     * vtbl +0x5C: ShopItemGridVisualObject::OnLButtonDblClk @004B27AD.
     * Full port with the same Java synthetic-money guard as OnMouseMove.
     */
    @Override
    public int onLButtonDblClk(int nFlags, int x, int y) {
        int sourceIndex = getGridIndexAtPoint(new Point(x, y));
        if (sourceIndex < 0 || sourceIndex >= getGridSourceSize()) {
            return 1;
        }
        Object entry = getGridSourceEntry(sourceIndex);
        if (isMoneyEntry(entry) || isCatalogEntryBlockedForDoubleClick(entry)) {
            return 1;
        }

        CMainWindow mainWindow = Globals.mainWindow;
        Object payload = beginUiDrag(sourceIndex, 1);
        if (payload == null || mainWindow.getUiLockPayload() == null) {
            return 1;
        }

        int sourceModeCode = mainWindow.getUiLockPackedModeCode();
        if (sourceModeCode == 4) {
            handleCatalogModeDoubleClick(mainWindow.getUiLockPayload(), mainWindow.getUiLockSourceIndex());
            resetCellUpdateCountersAndRefresh();
            ownerDialog.recomputeCatalogTransactionTotals();
            ownerDialog.dirtyFlags |= 0x20;
        } else if (sourceModeCode == 2) {
            applyTransferredPanelSelection(getCatalogEntryKind(mainWindow.getUiLockPayload()) - 1);
        } else if (!ownerDialog.tradeTransferGrid.isTransferGridFullForDistinctEntry(
                (TokenEntry) mainWindow.getUiLockPayload())) {
            handleTransferGridDoubleClick();
        }
        return 1;
    }

    /**
     * vtbl +0x60: ShopItemGridVisualObject::OnRButtonDown @004B2B0C.
     * Full port.
     */
    @Override
    public int onRButtonDown(int nFlags, int x, int y) {
        return 1;
    }

    /**
     * vtbl +0x64: ShopItemGridVisualObject::OnRButtonUp @004B2B1E.
     * Full port.
     */
    @Override
    public int onRButtonUp(int nFlags, int x, int y) {
        return 1;
    }

    /**
     * vtbl +0x68: ShopItemGridVisualObject::OnRButtonDblClk @004B2B30.
     * Full port.
     */
    @Override
    public int onRButtonDblClk(int nFlags, int x, int y) {
        return 1;
    }

    /**
     * vtbl +0x88: ShopItemGridVisualObject::GetGridIndexAtScreenPoint @004B1813.
     * Full port.
     */
    @Override
    public int getGridIndexAtScreenPoint(int x, int y) {
        return getGridIndexAtPoint(new Point(x, y));
    }

    /**
     * vtbl +0x8C: ShopItemGridVisualObject::GetGridIndexAtPoint @004B16F8.
     * Full port. Child classes expose their native `CRect[]` slot array through `getCellRect`.
     */
    public int getGridIndexAtPoint(Point point) {
        Point localPoint = new Point(
                point.x - ownerDialog.cRect.left,
                point.y - ownerDialog.cRect.top
        );
        int visibleStart = getVisibleStartValue();
        for (int visibleIndex = 0; visibleIndex < getVisibleCellCount(); visibleIndex++) {
            CRect cellRect = getCellRect(visibleIndex);
            if (!cellRect.contains(localPoint.x, localPoint.y)) {
                continue;
            }

            int sourceIndex = visibleStart + visibleIndex;
            if (sourceIndex >= getGridSourceSize()) {
                return -1;
            }

            TokenEntry entry = (TokenEntry) getGridSourceEntry(sourceIndex);
            if (!isCatalogEntryEmptySlot(entry)) {
                return sourceIndex;
            }
        }
        return -1;
    }

    /**
     * vtbl +0x98: ShopItemGridVisualObject::ClampVisibleStartToLastPage @004B1841.
     * Full port.
     */
    @Override
    public void clampVisibleStart() {
        int visibleStart = getVisibleStartValue();
        int visibleCellCount = getVisibleCellCount();
        int sourceSize = getGridSourceSize();
        if (sourceSize - visibleStart < visibleCellCount) {
            setVisibleStartValue(Math.max(0, sourceSize - visibleCellCount));
        }
    }

    /**
     * vtbl +0x90: ShopItemGridVisualObject::SetGridSource @004B1601.
     * Full port.
     */
    @Override
    public void setGridSource(Object gridSource) {
        this.gridSource = gridSource;
    }

    /**
     * vtbl +0xA8: ShopItemGridVisualObject::GetGridModeCode @004B66B0.
     * Full port.
     */
    @Override
    public int getGridModeCode() {
        return -1;
    }

    /**
     * vtbl +0xAC: ShopItemGridVisualObject::InitializeOverlayLayout @004B66A0.
     * Full port.
     */
    public void initializeOverlayLayout() {
    }

    /**
     * vtbl +0xB0: ShopItemGridVisualObject::AdoptEntriesFromArray @004B161A.
     * Full port.
     */
    public void adoptEntriesFromArray(Object entries) {
        List<TokenEntry> transferredEntries = mutableEntryList(entries);
        int modeCode = getGridModeCode();
        if (modeCode != 4) {
            for (TokenEntry entry : transferredEntries) {
                setEntryGridModeCode(entry, modeCode);
            }
        }
        List<TokenEntry> sourceEntries = mutableGridSourceList();
        sourceEntries.clear();
        sourceEntries.addAll(transferredEntries);
        transferredEntries.clear();
    }

    /**
     * Native: ShopItemGridVisualObject::LoadGridSounds @004B1435.
     * Full port.
     */
    public void loadGridSounds() {
        releaseGridSounds();
        putOnSound = new Sound(SFX_PUT_ON_WAV);
        putOffSound = new Sound(SFX_PUT_OFF_WAV);
        scrollSound = new Sound(SFX_SCROLL_WAV);
    }

    /**
     * Native: ShopItemGridVisualObject::ReleaseGridSounds @004B148C.
     * Native support also includes ShopItemGridVisualObject::ReleasePrimaryGridSound @004B1419.
     * Full port.
     */
    public void releaseGridSounds() {
        primaryGridSound = releaseSound(primaryGridSound);
        putOnSound = releaseSound(putOnSound);
        putOffSound = releaseSound(putOffSound);
        scrollSound = releaseSound(scrollSound);
    }

    /**
     * Native support extracted from ShopItemGridVisualObject::AdoptEntriesFromArray @004B161A.
     */
    @SuppressWarnings("unchecked")
    private List<TokenEntry> mutableEntryList(Object entries) {
        if (entries instanceof List<?> list) {
            return (List<TokenEntry>) list;
        }
        throw new IllegalArgumentException("Shop item-grid transfer payload is not a CArray-compatible list");
    }

    /**
     * Native support extracted from ShopItemGridVisualObject::AdoptEntriesFromArray @004B161A.
     */
    @SuppressWarnings("unchecked")
    private List<TokenEntry> mutableGridSourceList() {
        if (gridSource instanceof List<?> list) {
            return (List<TokenEntry>) list;
        }
        throw new IllegalStateException("Shop item-grid source is not a CArray-compatible list");
    }

    /**
     * Native support extracted from ShopItemGridVisualObject::AdoptEntriesFromArray @004B161A.
     */
    private static void setEntryGridModeCode(TokenEntry entry, int modeCode) {
        entry.gridModeCode = modeCode;
    }

    /**
     * vtbl +0xB4: ShopItemGridVisualObject::InitializeOverlayBitmaps @004B66C0.
     * Full port.
     */
    public void initializeOverlayBitmaps() {
    }

    /**
     * vtbl +0xB8: ShopItemGridVisualObject::ReleaseOverlayBitmaps @004B66D0.
     * Full port.
     */
    public void releaseOverlayBitmaps() {
    }

    /**
     * Native support extracted from child overlay bitmap setup in ShopCatalogGridVisualObject::InitializeOverlayBitmaps @004B2EA6
     * and ShopTransferGridVisualObject::InitializeOverlayBitmaps @004B55AD, rendered through inherited
     * ShopItemGridVisualObject::Update @004B18E2.
     */
    protected void drawPanelBackground(CRect screenRect) {
    }

    /**
     * Native: ShopItemGridVisualObject::ResetCellUpdateCountersAndRefresh @004B15A3.
     * Full port. Child classes expose their native `CRect[]` slot array through `getCellRect`.
     */
    final void resetCellUpdateCountersAndRefresh() {
        clampVisibleStart();
        for (int visibleIndex = 0; visibleIndex < getVisibleCellCount(); visibleIndex++) {
            setCellUpdateCounter(visibleIndex, 0);
        }
        refreshVisibleCellSprites();
    }

    /**
     * Native: ShopItemGridVisualObject::BeginUiLockDragVisual @004B22DB.
     * Full port. Native builds the cursor from the carried token portrait and marks placement allowed.
     */
    private int beginUiLockDragVisual(int sourceIndex) {
        CMainWindow mainWindow = Globals.mainWindow;
        Object payload = mainWindow.getUiLockPayload();
        String spritePath = resolveCatalogEntryDragSpritePath(payload);
        mainWindow.beginShopGridDragVisual(payload, sourceIndex, spritePath, getGridModeCode());
        mainWindow.cursor.setToMousePointer();
        ownerDialog.uiLockPlacementAllowedFlag = 1;
        return 1;
    }

    /**
     * Native: ShopItemGridVisualObject::ClearUiLockDragState @004B23F0.
     * Full port.
     */
    private int clearUiLockDragState() {
        CMousePointer.Cursor_Default.setToMousePointer();
        Globals.mainWindow.clearUiLockState();
        ownerDialog.uiLockPlacementAllowedFlag = 0;
        return 1;
    }

    /**
     * Native owner: child-owned `CRect[visibleColumns * visibleRows]` array at `this + 0x20C4`.
     * not ported.
     */
    protected CRect getCellRect(@SuppressWarnings("unused") int visibleIndex) {
        return null;
    }

    /**
     * Native owner: token-entry CArray `GetSize` reads in ShopItemGridVisualObject own methods.
     * not ported.
     */
    private int getGridSourceSize() {
        if (gridSource instanceof java.util.List<?> list) {
            return list.size();
        }
        if (gridSource instanceof Object[] array) {
            return array.length;
        }
        return 0;
    }

    /**
     * Native owner: token-entry CArray `GetAt` reads in ShopItemGridVisualObject own methods.
     * not ported.
     */
    private Object getGridSourceEntry(int index) {
        if (index < 0) {
            return null;
        }
        if (gridSource instanceof java.util.List<?> list) {
            return index < list.size() ? list.get(index) : null;
        }
        if (gridSource instanceof Object[] array) {
            return index < array.length ? array[index] : null;
        }
        return null;
    }

    /**
     * Native owner: `int *` visible-start pointer reads at GridOverlayVisualObject +0x90.
     * not ported.
     */
    private int getVisibleStartValue() {
        if (visibleStartRef instanceof int[] visibleStartRef && visibleStartRef.length > 0) {
            return visibleStartRef[0];
        }
        if (visibleStartRef instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }

    /**
     * Native owner: `int *` visible-start pointer writes at GridOverlayVisualObject +0x90 in ShopItemGridVisualObject::ClampVisibleStartToLastPage @004B1841.
     * not ported.
     */
    private void setVisibleStartValue(int value) {
        if (visibleStartRef instanceof int[] visibleStartRef && visibleStartRef.length > 0) {
            visibleStartRef[0] = value;
            return;
        }
        visibleStartRef = value;
    }

    /**
     * Native owner: visible cell count at GridOverlayVisualObject +0x88/+0x8C.
     * not ported.
     */
    private int getVisibleCellCount() {
        return Math.max(0, visibleColumns * visibleRows);
    }

    /**
     * Native owner: `CDWordArray::GetAt` reads from GridOverlayVisualObject +0x70.
     * not ported.
     */
    private int getCellUpdateCounter(int index) {
        if (index < 0 || index >= cellUpdateCounters.size()) {
            return 0;
        }
        Integer value = cellUpdateCounters.get(index);
        return value == null ? 0 : value;
    }

    /**
     * Native owner: `CDWordArray::SetAt` writes to GridOverlayVisualObject +0x70.
     * not ported.
     */
    private void setCellUpdateCounter(int index, int value) {
        if (index < 0 || index >= cellUpdateCounters.size()) {
            return;
        }
        cellUpdateCounters.set(index, value);
    }

    /**
     * Native owner: `CArray<>::GetAt` reads from GridOverlayVisualObject +0x5C.
     * not ported.
     */
    private CA16 getCachedCellSprite(int visibleIndex) {
        if (visibleIndex < 0 || visibleIndex >= visibleCellSprites.size()) {
            return null;
        }
        Object sprite = visibleCellSprites.get(visibleIndex);
        return sprite instanceof CA16 ca16 ? ca16 : null;
    }

    /**
     * Native branch helper in ShopItemGridVisualObject::OnLButtonDblClk @004B27AD.
     * Full port.
     */
    private void handleCatalogModeDoubleClick(Object payload, int sourceIndex) {
        switch (getCatalogEntryType(payload)) {
            case 1 -> {
                applyTransferredPanelSelection(sourceIndex);
                ownerDialog.dirtyFlags |= 0x8;
            }
            case 2 -> {
                ownerDialog.unitInventoryGrid.completeUiDrag(
                        getVisibleStartValue(ownerDialog.unitInventoryGrid)
                );
                ownerDialog.unitInventoryGrid.resetCellUpdateCountersAndRefresh();
            }
            case 5, 6, 7, 8 -> handleCatalogSubtypeDoubleClick(getCatalogEntryType(payload));
            default -> {
            }
        }
    }

    /**
     * Native branch helper in ShopItemGridVisualObject::OnLButtonDblClk @004B27AD.
     * Full port.
     */
    private void handleTransferGridDoubleClick() {
        ownerDialog.tradeTransferGrid.completeUiDrag(
                getVisibleStartValue(ownerDialog.tradeTransferGrid)
        );
        resetCellUpdateCountersAndRefresh();
        draw();
        ownerDialog.tradeTransferGrid.resetCellUpdateCountersAndRefresh();
        ownerDialog.recomputeCatalogTransactionTotals();
        ownerDialog.dirtyFlags |= 0x20;
    }

    /**
     * Native support extracted from ShopItemGridVisualObject::OnLButtonDblClk @004B27AD.
     * Full port.
     */
    private void handleCatalogSubtypeDoubleClick(int catalogEntryType) {
        Object selectedCatalogSource = ownerDialog.shopCatalogGrid.gridSource;
        int subtypeIndex = catalogEntryType - CATALOG_SUBTYPE_MODE_BASE;
        ownerDialog.shopCatalogGrid.setGridSource(ownerDialog.shopCatalogGrid.catalogCategoryEntriesAt(subtypeIndex));
        ownerDialog.shopCatalogGrid.completeUiDrag(-1);
        ownerDialog.shopCatalogGrid.setGridSource(selectedCatalogSource);
        ownerDialog.shopCatalogGrid.resetCellUpdateCountersAndRefresh();
    }

    /**
     * Native support extracted from ShopItemGridVisualObject::OnLButtonDblClk @004B27AD.
     * Full port.
     */
    private void applyTransferredPanelSelection(int slotIndex) {
        ownerDialog.selectionInfoPanel.applyCarriedTokenToSelectionSlot(slotIndex);
    }

    /**
     * Native support: MagicItem::IsShopCatalogEntryDragBlocked @00439C73.
     * Full port.
     */
    private static boolean isCatalogEntryDragBlocked(Object entry) {
        return entry instanceof TokenEntry tokenEntry
                && MagicItem.isShopCatalogEntryDragBlocked(tokenEntry, Globals.mainWindow.sessionMode);
    }

    /**
     * Native support: MagicItem::IsShopCatalogEntryBlockedForDoubleClick @00439D63.
     * Full port.
     */
    private static boolean isCatalogEntryBlockedForDoubleClick(Object entry) {
        return entry instanceof TokenEntry tokenEntry
                && MagicItem.isShopCatalogEntryBlockedForDoubleClick(tokenEntry);
    }

    /**
     * Native support boundary for the empty-slot check on `*(short *)(entry + 6) == -1` in ShopItemGridVisualObject::GetGridIndexAtPoint @004B16F8.
     * Full port.
     */
    private static boolean isCatalogEntryEmptySlot(TokenEntry entry) {
        return entry.isEmptyRegularEntry();
    }

    /**
     * Native support boundary for `FUN_0041EA40` money-entry checks in ShopItemGridVisualObject own methods.
     * Full port.
     */
    private static boolean isMoneyEntry(Object entry) {
        return entry instanceof TokenEntry tokenEntry && tokenEntry.isMoneyEntry();
    }

    /**
     * Native support: TokenEntry::isEmptyRegularEntry @004A4170.
     * Full port.
     */
    private static boolean isCatalogEntrySpriteRefreshSuppressed(Object entry) {
        return entry instanceof TokenEntry tokenEntry && tokenEntry.isEmptyRegularEntry();
    }

    /**
     * Native support extracted from Token::GetKind @0041E9E0 and entry `+0x18` reads in ShopItemGridVisualObject::OnLButtonUp @004B25CB / ShopItemGridVisualObject::OnLButtonDblClk @004B27AD.
     * Full port.
     */
    private static int getCatalogEntryType(Object entry) {
        if (entry instanceof TokenEntry tokenEntry) {
            return tokenEntry.gridModeCode;
        }
        return entry instanceof Token token ? token.getMovementType() : 0;
    }

    /**
     * Native support: Token::GetKind in ShopItemGridVisualObject::OnLButtonDblClk @004B27AD.
     * Full port.
     */
    private static int getCatalogEntryKind(Object entry) {
        if (entry instanceof TokenEntry tokenEntry) {
            return tokenEntry.getType();
        }
        return entry instanceof Token token ? token.getMovementType() : 0;
    }

    /**
     * Native support extracted from entry quantity reads at `entry +0x10` in ShopItemGridVisualObject::Update @004B18E2 and ShopItemGridVisualObject::OnMouseMove @004B242D.
     * Full port.
     */
    private static int getCatalogEntryQuantity(Object entry) {
        if (entry instanceof TokenEntry tokenEntry) {
            return tokenEntry.quantity;
        }
        return 1;
    }

    /**
     * Native support: TokenEntry::GetCatalogEntryValue @004B6680.
     * Full port.
     */
    private static int getCatalogEntryValue(Object entry) {
        if (entry instanceof TokenEntry tokenEntry) {
            return tokenEntry.getAttribute(VALUE_ATTRIBUTE_ID);
        }
        return entry instanceof Token token ? token.price : 0;
    }

    /**
     * Native support boundary for shop-entry tooltip helper `TokenEntry::resolveTooltipText` in ShopItemGridVisualObject::GetText @004B14D4.
     * Full port.
     */
    private static String resolveCatalogEntryTooltip(Object entry) {
        return entry instanceof TokenEntry tokenEntry ? tokenEntry.resolveTooltipText() : null;
    }

    /**
     * Native support boundary for `Token::GetEquipmentPortraitResourceName @00438BA1` sprite-name lookup in ShopItemGridVisualObject own paths.
     * Full port.
     */
    private static String resolveCatalogEntrySpriteName(Object entry) {
        return entry instanceof TokenEntry tokenEntry ? tokenEntry.getEquipmentPortraitResourceName() : null;
    }

    /**
     * Native support boundary for `Token::GetEquipmentPortraitResourceName @00438BA1` in
     * ShopItemGridVisualObject::BeginUiLockDragVisual @004B22DB, with Java resource mapping for money entries.
     * Full support port.
     */
    private static String resolveCatalogEntryDragSpritePath(Object entry) {
        if (isMoneyEntry(entry)) {
            return Resources.path(GRAPHICS_DIRECTORY, INTERFACE_DIRECTORY, MONEY_DIRECTORY, MONEY_SPRITE_FILE);
        }

        String spriteName = resolveCatalogEntrySpriteName(entry);
        return spriteName == null || spriteName.isBlank()
                ? null
                : Resources.path(GRAPHICS_DIRECTORY, INVENTORY_DIRECTORY, spriteName + A16_SUFFIX);
    }

    /**
     * Native support boundary for `keySHIFT` reads in ShopItemGridVisualObject::OnMouseMove @004B242D.
     * Full port.
     */
    private static boolean isShiftPressed(@SuppressWarnings("unused") int nFlags) {
        return Globals.shiftKeyDown;
    }

    /**
     * Native support extracted from ShopItemGridVisualObject::Update @004B18E2.
     * Full port.
     */
    private void drawCatalogEntryBackground(Object entry, CRect cellRect) {
        int x = ownerDialog.cRect.left + cellRect.left;
        int y = ownerDialog.cRect.top + cellRect.top + 1;
        if (isMoneyEntry(entry)) {
            if (ownerDialog.mapVisual != null && entry instanceof TokenEntry tokenEntry) {
                tokenEntry.quantity = ownerDialog.mapVisual.currentPlayer.gold;
            }
            GUI.backInv.drawRectMasked(x, y);
            GUI.sprMoney.draw(x, y, 0, null, false);
            return;
        }

        boolean selectableForCurrentUnit = isSelectableForCurrentUnit(entry);
        if (!isSubtypeGridMode(getCatalogEntryType(entry))) {
            drawInventoryBackground(selectableForCurrentUnit ? GUI.backInv : ownerDialog.backInventoryGreenBitmap, x, y);
            return;
        }

        CBmp64k background = ownerDialog.currentGold < getCatalogEntryValue(entry) || !selectableForCurrentUnit
                ? ownerDialog.backInventoryGreenBitmap
                : ownerDialog.backInventorySelectedBitmap;
        drawInventoryBackground(background, x, y);
    }

    /**
     * Native support extracted from ShopItemGridVisualObject::Update @004B18E2.
     * Full port.
     */
    private void drawCachedCatalogEntrySprite(int visibleIndex, CRect cellRect) {
        CA16 sprite = getCachedCellSprite(visibleIndex);
        if (sprite == null) {
            return;
        }
        sprite.draw(ownerDialog.cRect.left + cellRect.left, ownerDialog.cRect.top + cellRect.top + 1, 0, null, false);
    }

    /**
     * Native support extracted from GridOverlayVisualObject::DrawMagicTrailDots @004A285A as used by ShopItemGridVisualObject::Update @004B18E2.
     * Full port.
     */
    private void drawCatalogEntryMagicTrail(Object entry, int visibleIndex, CRect cellRect) {
        if (!(entry instanceof TokenEntry tokenEntry) || (tokenEntry.wireFlags & DYNAMIC_PAYLOAD_FLAG) == 0) {
            return;
        }

        int baseX = ownerDialog.cRect.left + cellRect.left;
        int baseY = ownerDialog.cRect.top + cellRect.top + 1;
        drawMagicTrailDots(baseX, baseY, visibleIndex);
    }

    /**
     * Native support extracted from ShopItemGridVisualObject::Update @004B18E2.
     * Full port.
     */
    private void drawCatalogEntryQuantity(CRect cellRect, int quantity) {
        Globals.fonts.font2.drawTextShadowed(
                ownerDialog.cRect.left + cellRect.left + 10,
                ownerDialog.cRect.top + cellRect.bottom - 0xF,
                Utils.formatDecimalThousands(quantity),
                TextAlign.DEFAULT.mask,
                Palettes.yellowish,
                1
        );
    }

    /**
     * Native support extracted from ShopItemGridVisualObject::Update @004B18E2.
     * Full port.
     */
    private void drawCatalogEntryValueBadge(CRect cellRect, Object entry) {
        int value = getCatalogEntryValue(entry);
        int frameIndex = value > 0 ? (int) Math.log10(value) : 0;
        List<CBmp64k> valueBitmaps = getCatalogEntryType(entry) == 2
                ? ownerDialog.costMediumBitmaps
                : ownerDialog.costSmallBitmaps;
        if (valueBitmaps.isEmpty()) {
            return;
        }

        frameIndex = Math.min(frameIndex, valueBitmaps.size() - 1);
        CBmp64k badge = valueBitmaps.get(frameIndex);
        int badgeX = ownerDialog.cRect.left + cellRect.right - badge.xSizeOf(0);
        int badgeY = ownerDialog.cRect.top + cellRect.top + 1;
        badge.drawAlpha(badgeX - 3, badgeY + 2, 0, VALUE_BADGE_SHADOW_BRIGHTNESS, false);
        badge.drawRectMasked(badgeX, badgeY, 0, 0, badge.xSizeOf(0), badge.ySizeOf(0));

        int displayValue = getCatalogEntryType(entry) == 2 ? (value + 1) / 2 : value;
        Globals.fonts.font2.drawTextShadowed(
                badgeX - 6 + badge.xSizeOf(0),
                badgeY,
                Utils.formatDecimalThousands(displayValue),
                TextAlign.RIGHT.mask,
                Palettes.yellowish,
                1
        );
    }

    /**
     * Native support extracted from ShopItemGridVisualObject::Update @004B18E2.
     * Full port.
     */
    private void drawInventoryBackground(CBmp64k bitmap, int x, int y) {
        if (bitmap != null) {
            bitmap.draw(x, y, 0, null, false);
        }
    }

    /**
     * Native support extracted from ShopItemGridVisualObject::Update @004B18E2 catalog-entry gating through
     * CUnit::IsSelectableForShopEntry @0046A9C2.
     * Full port.
     */
    private boolean isSelectableForCurrentUnit(Object entry) {
        if (!(entry instanceof TokenEntry tokenEntry)) {
            return true;
        }

        CUnit unit = ownerDialog.selectedPrimaryUnits.get(ownerDialog.selectedUnitIndex & 0xFFFF);
        return unit.isSelectableForShopEntry(tokenEntry);
    }

    /**
     * Native support extracted from ShopItemGridVisualObject::Update @004B18E2.
     * Full port.
     */
    private static boolean isSubtypeGridMode(int modeCode) {
        return modeCode >= SUBTYPE_GRID_MODE_MIN && modeCode <= SUBTYPE_GRID_MODE_MAX;
    }

    /**
     * Java helper around Sound::PlayPointer @00438570 for ShopItemGridVisualObject own methods.
     * not ported.
     */
    private static void playPointerSound(Sound sound) {
        if (sound != null) {
            sound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
        }
    }

    /**
     * Native support thunk: DeleteSound as used by ShopItemGridVisualObject::ReleaseGridSounds @004B148C.
     * not ported.
     */
    private static Sound releaseSound(Sound sound) {
        if (sound != null) {
            SoundSystem.get().releaseSound(sound);
        }
        return null;
    }

    /**
     * Native support extracted from sibling visible-start pointer reads in ShopItemGridVisualObject::OnLButtonDblClk @004B27AD.
     * Full support port.
     */
    private static int getVisibleStartValue(ShopItemGridVisualObject grid) {
        if (grid.visibleStartRef instanceof int[] visibleStartRef && visibleStartRef.length > 0) {
            return visibleStartRef[0];
        }
        if (grid.visibleStartRef instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }
}
