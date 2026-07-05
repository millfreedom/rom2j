package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CMousePointer;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.Token;
import ua.millfreedom.rom2.model.TokenEntry;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.window.CMainWindow;

import java.util.ArrayList;
import java.util.List;

import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_SCROLL_DOWN_57;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_SCROLL_UP_56;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_SHOP_SHELF_60;

/**
 * Native class: ShopCatalogGridVisualObject (vtbl @0x005CF0C0).
 * Purpose: shop catalog item-slot grid with top/bottom scroll controls and a two-column slot body.
 */
public class ShopCatalogGridVisualObject extends ShopItemGridVisualObject {
    public static final int NATIVE_SIZE = 0x2150; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int LEFT_BUTTON_FLAG = 0x1;
    private static final int SCROLL_ARROW_STEP = 2;
    private static final int SCROLL_PAGE_STEP = 6;
    private static final int VK_PRIOR = 0x21;
    private static final int VK_NEXT = 0x22;
    private static final int CATALOG_SUBTYPE_MODE_MIN = 5;
    private static final int CATALOG_SUBTYPE_MODE_MAX = 8;
    private static final String GRAPHICS_INTERFACE_DIRECTORY = "graphics/interface/";
    private static final String SHOP_ARROW1_BMP = "shoparrow1.bmp";
    private static final String SHOP_ARROW3_BMP = "shoparrow3.bmp";
    private static final String SHOP_ARROW2_BMP = "shoparrow2.bmp";
    private static final String SHOP_ARROW4_BMP = "shoparrow4.bmp";
    private static final String SHOP_INV_BMP = "shopinv.bmp";

    //0x20c4
    public CRect[] visibleCellRects;

    //0x20cc
    public final CRect topScrollButtonRect = new CRect();

    //0x20dc
    public final CRect bottomScrollButtonRect = new CRect();

    //0x20ec
    public CBmp64k topScrollHoverBitmap;
    //0x20f0
    public CBmp64k topScrollAvailableBitmap;
    //0x20f4
    public CBmp64k bottomScrollHoverBitmap;
    //0x20f8
    public CBmp64k bottomScrollAvailableBitmap;
    //0x20fc
    public CBmp64k inventoryBackgroundBitmap;
    //0x2100
    public final List<TokenEntry> catalogCategory0Entries = new ArrayList<>();
    //0x2114
    public final List<TokenEntry> catalogCategory1Entries = new ArrayList<>();
    //0x2128
    public final List<TokenEntry> catalogCategory2Entries = new ArrayList<>();
    //0x213c
    public final List<TokenEntry> catalogCategory3Entries = new ArrayList<>();

    /**
     * Native: ShopCatalogGridVisualObject::ShopCatalogGridVisualObject @004B2B42.
     */
    public ShopCatalogGridVisualObject(int id, CRect rect, ShopDialogVisualObject ownerDialog) {
        super(id, rect, ownerDialog);
        initializeShopCatalogGrid();
    }

    /**
     * Native: ShopCatalogGridVisualObject::ShopCatalogGridVisualObject @004B2C61.
     */
    public ShopCatalogGridVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            ShopDialogVisualObject ownerDialog
    ) {
        super(id, xLeft, yTop, xRight, yBottom, ownerDialog);
        initializeShopCatalogGrid();
    }

    /**
     * Native support extracted from ShopCatalogGridVisualObject constructors @004B2B42 and @004B2C61.
     */
    private void initializeShopCatalogGrid() {
        this.visibleColumns = cRect.width() / 0x50;
        this.visibleRows = cRect.height() / 0x50;
        initializeOverlayLayout();
        initArrays();
        this.topScrollHoverBitmap = null;
        this.topScrollAvailableBitmap = null;
        this.bottomScrollHoverBitmap = null;
        this.bottomScrollAvailableBitmap = null;
        this.inventoryBackgroundBitmap = null;
    }

    /**
     * vtbl +0x14: ShopCatalogGridVisualObject::GetText @004B37F7.
     * Full port.
     */
    @Override
    public String getText() {
        if (ownerDialog.dialogActiveFlag == 0
                || Globals.mainWindow.getUiLockPayload() != null) {
            return null;
        }

        int mouseX = Globals.mousePointer.getX();
        int mouseY = Globals.mousePointer.getY();
        if (getCatalogEntryIndexAtScreenPoint(mouseX, mouseY) >= 0) {
            return super.getText();
        }

        java.awt.Point ownerLocalPoint = toOwnerLocalPoint(mouseX, mouseY);
        if (topScrollButtonRect.contains(ownerLocalPoint.x, ownerLocalPoint.y)) {
            return get(MAIN_SCROLL_UP_56);
        }
        if (bottomScrollButtonRect.contains(ownerLocalPoint.x, ownerLocalPoint.y)) {
            return get(MAIN_SCROLL_DOWN_57);
        }
        return get(MAIN_SHOP_SHELF_60);
    }

    /**
     * vtbl +0x2C: ShopCatalogGridVisualObject::Update @004B392A.
     * Full port.
     */
    @Override
    public void update() {
        if (ownerDialog.dialogActiveFlag == 0 || inventoryBackgroundBitmap == null) {
            return;
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        inventoryBackgroundBitmap.draw(
                screenRect.left,
                screenRect.top,
                0,
                null,
                false
        );

        super.update();
        drawAvailableScrollButtons();
    }

    /**
     * Native: ShopCatalogGridVisualObject::SelectCatalogCategory @004B3C4E.
     * Full port.
     */
    public void selectCatalogCategory(int categoryIndex) {
        setGridSource(catalogCategoryEntriesAt(categoryIndex));
        resetCellUpdateCountersAndRefresh();
    }

    /**
     * Native: ShopCatalogGridVisualObject::ClearCatalogCategoryEntries @004B40C3.
     * Full logical port. Native deletes each entry pointer before `CArray<>::RemoveAll`; Java clears collection ownership.
     */
    public void clearCatalogCategoryEntries() {
        catalogCategory0Entries.clear();
        catalogCategory1Entries.clear();
        catalogCategory2Entries.clear();
        catalogCategory3Entries.clear();
    }

    /**
     * Native support for the consecutive category CArray fields used by
     * ShopCatalogGridVisualObject::SelectCatalogCategory @004B3C4E and ClearCatalogCategoryEntries @004B40C3.
     * Full support port.
     */
    public List<TokenEntry> catalogCategoryEntriesAt(int categoryIndex) {
        return switch (categoryIndex) {
            case 0 -> catalogCategory0Entries;
            case 1 -> catalogCategory1Entries;
            case 2 -> catalogCategory2Entries;
            case 3 -> catalogCategory3Entries;
            default -> throw new IndexOutOfBoundsException("catalog category index: " + categoryIndex);
        };
    }

    /**
     * vtbl +0x78: ShopCatalogGridVisualObject::MergeOrInsertEntryAt @004B4177.
     * Full port.
     */
    @Override
    public int mergeOrInsertEntryAt(Object entry, int insertIndex) {
        TokenEntry catalogEntry = (TokenEntry) entry;
        List<TokenEntry> sourceEntries = requireCatalogGridSourceEntries();
        catalogEntry.gridModeCode = catalogEntry.categoryIndex + 5;
        if (catalogEntry.gridModeCode > 4
                && catalogEntry.gridModeCode < 9
                && catalogEntry.sourceIndex >= 0
                && catalogEntry.sourceIndex < sourceEntries.size()) {
            TokenEntry existing = sourceEntries.get(catalogEntry.sourceIndex);
            if (existing.isEmptyRegularEntry()) {
                existing.copyFrom(catalogEntry);
            } else {
                existing.addQuantity(catalogEntry.quantity);
            }
            return catalogEntry.sourceIndex;
        }

        if (insertIndex >= 0 && insertIndex < sourceEntries.size()) {
            sourceEntries.add(insertIndex, catalogEntry);
            return insertIndex;
        }
        if (!sourceEntries.isEmpty()) {
            int lastIndex = sourceEntries.size() - 1;
            if (sourceEntries.get(lastIndex).isMoneyEntry()) {
                sourceEntries.add(lastIndex, catalogEntry);
                return lastIndex;
            }
        }

        sourceEntries.add(catalogEntry);
        return sourceEntries.size() - 1;
    }

    /**
     * vtbl +0x84: ShopCatalogGridVisualObject::DetachMatchingEntry @004B4309.
     * Full port. Native keeps catalog slots allocated by clearing depleted non-money entries in place.
     */
    @Override
    public TokenEntry detachMatchingTokenEntry(Object entry, int quantity) {
        TokenEntry requested = (TokenEntry) entry;
        List<TokenEntry> sourceEntries = requireCatalogGridSourceEntries();
        for (int index = 0; index < sourceEntries.size(); index++) {
            TokenEntry existing = sourceEntries.get(index);
            if (!requested.matchesStackIdentity(existing)) {
                continue;
            }
            if (existing.isEmptyRegularEntry()) {
                return null;
            }

            TokenEntry detached = new TokenEntry(existing);
            if (existing.tryRemoveQuantityLeavingRemainder(quantity)) {
                detached.quantity = quantity;
            } else if (existing.isMoneyEntry()) {
                existing.quantity = 0;
            } else {
                detached.quantity = quantity;
                existing.quantity = 0;
                existing.packedTokenHash = 0;
            }
            detached.sourceIndex = index;
            detached.gridModeCode = getGridModeCode();
            return detached;
        }
        return null;
    }

    /**
     * vtbl +0xA4: ShopCatalogGridVisualObject::CompleteUiDrag @004B4586.
     * Full port. Native sends the catalog entry's original source-slot descriptor as the destination slot.
     */
    @Override
    public int completeUiDrag(int insertIndex) {
        CMainWindow mainWindow = Globals.mainWindow;
        Object payload = mainWindow.getUiLockPayload();
        if (payload == null) {
            return -1;
        }

        int quantityOrItemId = ((TokenEntry) payload).quantity;
        int destinationModeCode = getGridModeCode();
        int sourceModeCode = mainWindow.getUiLockPackedModeCode();
        int result = mergeOrInsertEntryAt(payload, insertIndex);
        int destinationSlot = requireCatalogGridSourceEntries().get(result).sourceSlotDescriptor;
        if (getGridModeCode() == 2) {
            bindGridSourceFromContext(resolveGridOverlayBindingContext(mainWindow));
        } else {
            setGridSource(gridSource);
        }
        if (sourceModeCode != destinationModeCode) {
            notifyGridOverlayDropCommitted(
                    mainWindow,
                    sourceModeCode,
                    mainWindow.getUiLockSourceIndex(),
                    getGridModeCode(),
                    destinationSlot,
                    quantityOrItemId
            );
        }
        mainWindow.clearUiLockState();
        mainWindow.getInputController().onMessage(MessageCodes.TEXT_LIST_SELECTION_CHANGED, id, 0);
        return result;
    }

    /**
     * vtbl +0xAC: ShopCatalogGridVisualObject::InitializeOverlayLayout @004B35B0.
     * Full port.
     */
    @Override
    public void initializeOverlayLayout() {
        int visibleCellCount = visibleColumns * visibleRows;
        visibleCellRects = new CRect[visibleCellCount];

        topScrollButtonRect.set(cRect.left + 0x2E, cRect.top, cRect.left + 0x76, cRect.top + 0x20);
        bottomScrollButtonRect.set(cRect.left + 0x2E, cRect.top + 0x10F, cRect.left + 0x76, cRect.top + 0x12F);

        for (int column = 0; column < visibleColumns; column++) {
            for (int row = 0; row < visibleRows; row++) {
                visibleCellRects[row * visibleColumns + column] = new CRect(
                        cRect.left + 1 + column * 0x50,
                        topScrollButtonRect.bottom - 1 + row * 0x50,
                        cRect.left + 0x51 + column * 0x50,
                        topScrollButtonRect.bottom + 0x4F + row * 0x50
                );
            }
        }
    }

    /**
     * vtbl +0xA8: ShopCatalogGridVisualObject::GetGridModeCode @004B66E0.
     * Full port.
     */
    @Override
    public int getGridModeCode() {
        return (ownerDialog.selectedCatalogCategoryIndex & 0xFFFF) + 5;
    }

    /**
     * vtbl +0xB4: ShopCatalogGridVisualObject::InitializeOverlayBitmaps @004B2EA6.
     * Native: ShopCatalogGridVisualObject::InitializeOverlayBitmaps @004B2EA6.
     * Loads the catalog scroll arrows and shelf background:
     * ShopArrow1, ShopArrow3, ShopArrow2, ShopArrow4, ShopInv.
     */
    @Override
    public void initializeOverlayBitmaps() {
        initializeOverlayBitmapsFromDirectory(GRAPHICS_INTERFACE_DIRECTORY);
    }

    /**
     * Native support extracted from ShopCatalogGridVisualObject::InitializeOverlayBitmaps @004B2EA6,
     * DruidShopCatalogGridVisualObject::InitializeOverlayBitmaps @004B307F, and
     * KaargShopCatalogGridVisualObject::InitializeOverlayBitmaps @004B3258.
     * Full support port.
     */
    protected final void initializeOverlayBitmapsFromDirectory(String graphicsInterfaceDirectory) {
        releaseOverlayBitmaps();
        topScrollHoverBitmap = new CBmp64k(graphicsInterfaceDirectory + SHOP_ARROW1_BMP);
        Globals.renderer.refreshMousePointer();
        topScrollAvailableBitmap = new CBmp64k(graphicsInterfaceDirectory + SHOP_ARROW3_BMP);
        Globals.renderer.refreshMousePointer();
        bottomScrollHoverBitmap = new CBmp64k(graphicsInterfaceDirectory + SHOP_ARROW2_BMP);
        Globals.renderer.refreshMousePointer();
        bottomScrollAvailableBitmap = new CBmp64k(graphicsInterfaceDirectory + SHOP_ARROW4_BMP);
        Globals.renderer.refreshMousePointer();
        inventoryBackgroundBitmap = new CBmp64k(graphicsInterfaceDirectory + SHOP_INV_BMP);
        Globals.renderer.refreshMousePointer();
    }

    /**
     * vtbl +0xB8: ShopCatalogGridVisualObject::ReleaseOverlayBitmaps @004B3431.
     * Native: ShopCatalogGridVisualObject::ReleaseOverlayBitmaps @004B3431.
     * Releases the five catalog overlay bitmaps initialized by @004B2EA6.
     */
    @Override
    public void releaseOverlayBitmaps() {
        topScrollHoverBitmap = null;
        topScrollAvailableBitmap = null;
        bottomScrollHoverBitmap = null;
        bottomScrollAvailableBitmap = null;
        inventoryBackgroundBitmap = null;
    }

    /**
     * Native support extracted from ShopCatalogGridVisualObject::Update @004B392A.
     * Full support port. The catalog override draws its own panel background before delegating to the inherited item renderer.
     */
    @Override
    protected void drawPanelBackground(CRect screenRect) {
    }

    /**
     * vtbl +0x4C: ShopCatalogGridVisualObject::OnMouseMove @004B3EFA.
     * Full port.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        CMainWindow mainWindow = Globals.mainWindow;
        Object payload = mainWindow.getUiLockPayload();
        if (payload != null) {
            if (isCatalogSubtypePayload(payload)) {
                ownerDialog.uiLockPlacementAllowedFlag = 1;
                mainWindow.cursor.setToMousePointer();
            } else {
                CMousePointer.Cursor_CantPut.setToMousePointer();
                ownerDialog.uiLockPlacementAllowedFlag = 0;
            }
        }
        return super.onMouseMove(nFlags, x, y);
    }

    /**
     * vtbl +0x50: ShopCatalogGridVisualObject::OnUserMsg @004B3C9D.
     * Full port.
     */
    @Override
    public int onUserMsg(int nFlags, int x, int y) {
        if ((nFlags & LEFT_BUTTON_FLAG) != 0) {
            scrollFromPoint(x, y);
        }
        return 1;
    }

    /**
     * vtbl +0x54: ShopCatalogGridVisualObject::OnLButtonDown @004B3DCC.
     * Full port.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        if (scrollFromPoint(x, y)) {
            return 1;
        }
        return super.onLButtonDown(nFlags, x, y);
    }

    /**
     * vtbl +0x58: ShopCatalogGridVisualObject::OnLButtonUp @004B3C7C.
     * Full port.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        return super.onLButtonUp(nFlags, x, y);
    }

    /**
     * Java extension for mouse-wheel scrolling over the shop shelf.
     * not ported.
     */
    @Override
    public int onMouseWheel(int nFlagsAndDelta, int x, int y) {
        if (ownerDialog == null || ownerDialog.dialogActiveFlag == 0 || !isShopShelfPoint(x, y)) {
            return 0;
        }

        int wheelDelta = (short) ((nFlagsAndDelta >>> 16) & 0xFFFF);
        if (wheelDelta > 0) {
            if (canScrollBackwardOneEntry()) {
                scrollCatalogBackward(SCROLL_ARROW_STEP);
                scrollSound.playFresh();
                draw();
            }
            return 1;
        }
        if (wheelDelta < 0) {
            if (canScrollForwardOneEntry()) {
                scrollCatalogForward(SCROLL_ARROW_STEP);
                scrollSound.playFresh();
                draw();
            }
            return 1;
        }
        return 0;
    }

    /**
     * vtbl +0x6C: ShopCatalogGridVisualObject::OnKeyDown @004B3FE6.
     * Full port.
     */
    @Override
    public int onKeyDown(int nChar) {
        if (nChar == VK_PRIOR) {
            scrollCatalogBackward(SCROLL_PAGE_STEP);
            scrollSound.playFresh();
            return 1;
        }
        if (nChar == VK_NEXT) {
            scrollCatalogForward(SCROLL_PAGE_STEP);
            scrollSound.playFresh();
            return 1;
        }
        return 0;
    }

    /**
     * Java helper for the child-owned `CRect[]` geometry populated by ShopCatalogGridVisualObject::InitializeOverlayLayout @004B35B0.
     */
    @Override
    protected CRect getCellRect(int visibleIndex) {
        if (visibleCellRects == null
                || visibleIndex < 0
                || visibleIndex >= visibleCellRects.length) {
            return null;
        }
        return visibleCellRects[visibleIndex];
    }

    /**
     * Native support extracted from ShopCatalogGridVisualObject::Update @004B392A.
     */
    private void drawAvailableScrollButtons() {
        if (canScrollBackwardOneEntry()) {
            drawTopScrollButton(isMouseOverOwnerRect(topScrollButtonRect));
        }
        if (canScrollForwardOneEntry()) {
            drawBottomScrollButton(isMouseOverOwnerRect(bottomScrollButtonRect));
        }
    }

    /**
     * Native helper: ShopCatalogGridVisualObject::drawTopScrollHoverButton @004B3ACE and
     * ShopCatalogGridVisualObject::drawTopScrollAvailableButton @004B3B2B.
     */
    private void drawTopScrollButton(boolean hovered) {
        if (topScrollHoverBitmap == null) {
            return;
        }
        CBmp64k bitmap = hovered ? topScrollHoverBitmap : topScrollAvailableBitmap;
        drawScrollButton(bitmap, topScrollButtonRect);
    }

    /**
     * Native helper: ShopCatalogGridVisualObject::drawBottomScrollHoverButton @004B3B88 and
     * ShopCatalogGridVisualObject::drawBottomScrollAvailableButton @004B3BEB.
     */
    private void drawBottomScrollButton(boolean hovered) {
        if (bottomScrollHoverBitmap == null) {
            return;
        }
        CBmp64k bitmap = hovered ? bottomScrollHoverBitmap : bottomScrollAvailableBitmap;
        drawScrollButton(bitmap, bottomScrollButtonRect);
    }

    /**
     * Native support extracted from ShopCatalogGridVisualObject::drawTopScrollHoverButton @004B3ACE,
     * drawTopScrollAvailableButton @004B3B2B, drawBottomScrollHoverButton @004B3B88, and
     * drawBottomScrollAvailableButton @004B3BEB.
     */
    private void drawScrollButton(CBmp64k bitmap, CRect buttonRect) {
        CRect ownerScreenRect = new CRect();
        ownerDialog.clientToScreen(ownerScreenRect, ownerDialog.cRect);
        bitmap.draw(
                ownerScreenRect.left + buttonRect.left,
                ownerScreenRect.top + buttonRect.top,
                0,
                null,
                false
        );
    }

    /**
     * Native support extracted from ShopCatalogGridVisualObject::OnUserMsg @004B3C9D and
     * ShopCatalogGridVisualObject::OnLButtonDown @004B3DCC.
     */
    private boolean scrollFromPoint(int x, int y) {
        java.awt.Point ownerLocalPoint = toOwnerLocalPoint(x, y);
        if (topScrollButtonRect.contains(ownerLocalPoint.x, ownerLocalPoint.y)) {
            scrollCatalogBackward(SCROLL_ARROW_STEP);
            scrollSound.playFresh();
            return true;
        }
        if (bottomScrollButtonRect.contains(ownerLocalPoint.x, ownerLocalPoint.y)) {
            scrollCatalogForward(SCROLL_ARROW_STEP);
            scrollSound.playFresh();
            return true;
        }
        return false;
    }

    /**
     * Java hit-test support for the non-ported mouse-wheel shelf shortcut.
     * not ported.
     */
    private boolean isShopShelfPoint(int screenX, int screenY) {
        java.awt.Point ownerLocalPoint = toOwnerLocalPoint(screenX, screenY);
        return cRect.contains(ownerLocalPoint.x, ownerLocalPoint.y);
    }

    /**
     * Native support extracted from the repeated GridOverlayVisualObject::ScrollBackwardOneEntry @004A1BFF calls in
     * ShopCatalogGridVisualObject::OnUserMsg @004B3C9D, OnLButtonDown @004B3DCC, and OnKeyDown @004B3FE6.
     */
    private void scrollCatalogBackward(int stepCount) {
        for (int step = 0; step < stepCount; step++) {
            scrollBackwardOneEntry();
        }
    }

    /**
     * Native support extracted from the repeated GridOverlayVisualObject::ScrollForwardOneEntry @004A1A78 calls in
     * ShopCatalogGridVisualObject::OnUserMsg @004B3C9D, OnLButtonDown @004B3DCC, and OnKeyDown @004B3FE6.
     */
    private void scrollCatalogForward(int stepCount) {
        for (int step = 0; step < stepCount; step++) {
            scrollForwardOneEntry();
        }
    }

    /**
     * Native support extracted from ShopCatalogGridVisualObject::OnMouseMove @004B3EFA.
     */
    private static boolean isCatalogSubtypePayload(Object payload) {
        int modeCode = payload instanceof TokenEntry tokenEntry
                ? tokenEntry.gridModeCode
                : payload instanceof Token token ? token.getMovementType() : 0;
        return modeCode >= CATALOG_SUBTYPE_MODE_MIN && modeCode <= CATALOG_SUBTYPE_MODE_MAX;
    }

    /**
     * Native support extracted from ShopCatalogGridVisualObject::GetText @004B37F7 and scroll hit-test handlers.
     */
    private java.awt.Point toOwnerLocalPoint(int screenX, int screenY) {
        CRect ownerScreenRect = new CRect();
        ownerDialog.clientToScreen(ownerScreenRect, ownerDialog.cRect);
        return new java.awt.Point(screenX - ownerScreenRect.left, screenY - ownerScreenRect.top);
    }

    /**
     * Native support extracted from ShopCatalogGridVisualObject::Update @004B392A.
     */
    private boolean isMouseOverOwnerRect(CRect rect) {
        java.awt.Point ownerLocalPoint = toOwnerLocalPoint(
                Globals.mousePointer.getX(),
                Globals.mousePointer.getY()
        );
        return rect.contains(ownerLocalPoint.x, ownerLocalPoint.y);
    }

    /**
     * Native support extracted from ShopCatalogGridVisualObject::GetText @004B37F7 and
     * the ShopItemGridVisualObject grid-index hit test @004B16F8.
     */
    private int getCatalogEntryIndexAtScreenPoint(int screenX, int screenY) {
        java.awt.Point ownerLocalPoint = toOwnerLocalPoint(screenX, screenY);
        int visibleStart = getCatalogVisibleStart();
        for (int visibleIndex = 0; visibleIndex < visibleCellRects.length; visibleIndex++) {
            CRect cellRect = visibleCellRects[visibleIndex];
            if (!cellRect.contains(ownerLocalPoint.x, ownerLocalPoint.y)) {
                continue;
            }

            int sourceIndex = visibleStart + visibleIndex;
            Object entry = getCatalogEntryAt(sourceIndex);
            if (entry instanceof TokenEntry tokenEntry && !tokenEntry.isEmptyRegularEntry()) {
                return sourceIndex;
            }
            return -1;
        }
        return -1;
    }

    /**
     * Native support extracted from the ShopItemGridVisualObject grid-index hit test @004B16F8.
     */
    private int getCatalogVisibleStart() {
        if (visibleStartRef instanceof int[] visibleStartRef && visibleStartRef.length > 0) {
            return visibleStartRef[0];
        }
        if (visibleStartRef instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }

    /**
     * Native support extracted from the ShopItemGridVisualObject grid-index hit test @004B16F8.
     */
    private Object getCatalogEntryAt(int sourceIndex) {
        if (sourceIndex < 0) {
            return null;
        }
        if (gridSource instanceof List<?> entries) {
            return sourceIndex < entries.size() ? entries.get(sourceIndex) : null;
        }
        if (gridSource instanceof Object[] entries) {
            return sourceIndex < entries.length ? entries[sourceIndex] : null;
        }
        return null;
    }

    /**
     * Native support extracted from ShopCatalogGridVisualObject::MergeOrInsertEntryAt @004B4177,
     * DetachMatchingEntry @004B4309, and CompleteUiDrag @004B4586.
     */
    @SuppressWarnings("unchecked")
    private List<TokenEntry> requireCatalogGridSourceEntries() {
        return (List<TokenEntry>) gridSource;
    }
}
