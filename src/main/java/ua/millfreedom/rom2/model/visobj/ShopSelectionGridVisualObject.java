package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CMousePointer;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.TokenEntry;
import ua.millfreedom.rom2.model.window.CMainWindow;

import java.awt.Point;

import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_SCROLL_LEFT_54;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_SCROLL_RIGHT_55;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_YOUR_HERO_S_INVENTORY_58;

/**
 * Native class: ShopSelectionGridVisualObject (vtbl @0x005CF180).
 * Purpose: shop selection item-slot grid with left/right scroll controls and a horizontally scrolled slot body.
 */
public class ShopSelectionGridVisualObject extends ShopItemGridVisualObject {
    public static final int NATIVE_SIZE = 0x20EC; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!
    private static final int LEFT_BUTTON_FLAG = 0x1;
    private static final int SCROLL_ARROW_WIDTH = 0x20;
    private static final int SCROLL_ARROW_HEIGHT = 0x58;
    private static final int RIGHT_SCROLL_BUTTON_X_OFFSET = 0x1B0;

    //0x20c4
    public CRect[] visibleCellRects;

    //0x20cc
    public final CRect leftScrollButtonRect = new CRect();

    //0x20dc
    public final CRect rightScrollButtonRect = new CRect();

    /**
     * Native: ShopSelectionGridVisualObject::ShopSelectionGridVisualObject @004B46D8.
     * Full port.
     */
    public ShopSelectionGridVisualObject() {
        super();
        visibleStartRef = null;
    }

    /**
     * Native: ShopSelectionGridVisualObject::ShopSelectionGridVisualObject @004B4767.
     * Full port.
     */
    public ShopSelectionGridVisualObject(int id, CRect rect, ShopDialogVisualObject ownerDialog) {
        super(id, rect, ownerDialog);
        initializeShopSelectionGrid();
    }

    /**
     * Native: ShopSelectionGridVisualObject::ShopSelectionGridVisualObject @004B484F.
     * Full port.
     */
    public ShopSelectionGridVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            ShopDialogVisualObject ownerDialog
    ) {
        super(id, xLeft, yTop, xRight, yBottom, ownerDialog);
        initializeShopSelectionGrid();
    }

    /**
     * Native support extracted from ShopSelectionGridVisualObject constructors @004B4767 and @004B484F.
     */
    private void initializeShopSelectionGrid() {
        this.visibleColumns = cRect.width() / 0x50 - 1;
        this.visibleRows = cRect.height() / 0x50;
        this.visibleStartRef = null;
        initializeOverlayLayout();
        initArrays();
    }

    /**
     * vtbl +0x14: ShopSelectionGridVisualObject::GetText @004B4BA3.
     * Full port.
     */
    @Override
    public String getText() {
        if (ownerDialog.dialogActiveFlag == 0 || Globals.mainWindow.getUiLockPayload() != null) {
            return null;
        }

        int mouseX = Globals.mousePointer.getX();
        int mouseY = Globals.mousePointer.getY();
        if (getGridIndexAtScreenPoint(mouseX, mouseY) >= 0) {
            return super.getText();
        }

        Point ownerLocalPoint = toOwnerLocalPoint(mouseX, mouseY);
        if (leftScrollButtonRect.contains(ownerLocalPoint.x, ownerLocalPoint.y)) {
            return get(MAIN_SCROLL_LEFT_54);
        }
        if (rightScrollButtonRect.contains(ownerLocalPoint.x, ownerLocalPoint.y)) {
            return get(MAIN_SCROLL_RIGHT_55);
        }
        return get(MAIN_YOUR_HERO_S_INVENTORY_58);
    }

    /**
     * vtbl +0x2C: ShopSelectionGridVisualObject::Update @004B4CD6.
     * Full port.
     */
    @Override
    public void update() {
        if (ownerDialog.dialogActiveFlag == 0) {
            return;
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        GUI.invFrame.drawRectMasked(
                screenRect.left,
                screenRect.top,
                0,
                0,
                cRect.width(),
                cRect.height()
        );

        super.update();
        if (canScrollBackwardOneEntry()) {
            if (isMouseOverOwnerRect(leftScrollButtonRect)) {
                drawLeftScrollHoverButton();
            } else {
                drawLeftScrollAvailableButton();
            }
        }
        if (canScrollForwardOneEntry()) {
            if (isMouseOverOwnerRect(rightScrollButtonRect)) {
                drawRightScrollHoverButton();
            } else {
                drawRightScrollAvailableButton();
            }
        }
    }

    /**
     * vtbl +0x50: ShopSelectionGridVisualObject::OnUserMsg @004B4FE4.
     * Full port.
     */
    @Override
    public int onUserMsg(int nFlags, int x, int y) {
        if ((nFlags & LEFT_BUTTON_FLAG) != 0) {
            onLButtonDown(nFlags, x, y);
        }
        return 1;
    }

    /**
     * vtbl +0x58: ShopSelectionGridVisualObject::OnLButtonUp @004B5017.
     * Full port.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        return super.onLButtonUp(nFlags, x, y);
    }

    /**
     * vtbl +0x54: ShopSelectionGridVisualObject::OnLButtonDown @004B5038.
     * Full port.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        Point ownerLocalPoint = toOwnerLocalPoint(x, y);
        if (leftScrollButtonRect.contains(ownerLocalPoint.x, ownerLocalPoint.y)) {
            scrollBackwardOneEntry();
            playScrollSound();
            return 1;
        }
        if (rightScrollButtonRect.contains(ownerLocalPoint.x, ownerLocalPoint.y)) {
            scrollForwardOneEntry();
            playScrollSound();
            return 1;
        }
        return super.onLButtonDown(nFlags, x, y);
    }

    /**
     * vtbl +0x4C: ShopSelectionGridVisualObject::OnMouseMove @004B5156.
     * Full port.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        CMainWindow mainWindow = Globals.mainWindow;
        Object payload = mainWindow.getUiLockPayload();
        if (payload != null) {
            int gridModeCode = getPayloadGridModeCode(payload);
            if (gridModeCode == 1 || gridModeCode == 2) {
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
     * vtbl +0xAC: ShopSelectionGridVisualObject::InitializeOverlayLayout @004B496C.
     * Full port.
     */
    @Override
    public void initializeOverlayLayout() {
        int visibleCellCount = visibleColumns * visibleRows;
        visibleCellRects = new CRect[visibleCellCount];

        leftScrollButtonRect.set(cRect.left, cRect.top, cRect.left + 0x20, cRect.bottom);
        rightScrollButtonRect.set(cRect.left + 0x1B0, cRect.top, cRect.right, cRect.bottom);

        for (int column = 0; column < visibleColumns; column++) {
            for (int row = 0; row < visibleRows; row++) {
                visibleCellRects[row * visibleColumns + column] = new CRect(
                        leftScrollButtonRect.right + column * 0x50,
                        cRect.top + 5 + row * 0x50,
                        leftScrollButtonRect.right + 0x50 + column * 0x50,
                        cRect.top + 0x55 + row * 0x50
                );
            }
        }
    }

    /**
     * Native support hook for inherited ShopItemGridVisualObject::Update @004B18E2.
     * ShopSelectionGridVisualObject::Update @004B4CD6 draws the selection frame before delegating to the inherited item renderer.
     */
    @Override
    protected void drawPanelBackground(CRect screenRect) {
    }

    /**
     * vtbl +0x78: ShopSelectionGridVisualObject::MergeOrInsertEntryAt @004B6740.
     * Full port.
     */
    @Override
    public int mergeOrInsertEntryAt(Object entry, int insertIndex) {
        ((TokenEntry) entry).gridModeCode = 2;
        return super.mergeOrInsertEntryAt(entry, insertIndex);
    }

    /**
     * vtbl +0x7C: ShopSelectionGridVisualObject::MergeOrAppendEntry @004B6710.
     * Full port.
     */
    @Override
    public int mergeOrAppendEntry(Object entry) {
        ((TokenEntry) entry).gridModeCode = 2;
        return super.mergeOrAppendEntry(entry);
    }

    /**
     * vtbl +0xA8: ShopSelectionGridVisualObject::GetGridModeCode @004B6770.
     * Full port.
     */
    @Override
    public int getGridModeCode() {
        return 2;
    }

    /**
     * Java helper for the child-owned `CRect[]` geometry populated by ShopSelectionGridVisualObject::InitializeOverlayLayout @004B496C.
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
     * Native: ShopSelectionGridVisualObject::drawLeftScrollHoverButton @004B4E78.
     * Full port.
     */
    private void drawLeftScrollHoverButton() {
        CRect screenRect = getSelectionGridScreenRect();
        if (GUI.invArrow1 != null) {
            GUI.invArrow3.drawRectMasked(
                    screenRect.left,
                    screenRect.top,
                    0,
                    0,
                    SCROLL_ARROW_WIDTH,
                    SCROLL_ARROW_HEIGHT
            );
        }
    }

    /**
     * Native: ShopSelectionGridVisualObject::drawLeftScrollAvailableButton @004B4ED0.
     * Full port.
     */
    private void drawLeftScrollAvailableButton() {
        CRect screenRect = getSelectionGridScreenRect();
        if (GUI.invArrow3 != null) {
            GUI.invArrow1.drawRectMasked(
                    screenRect.left,
                    screenRect.top,
                    0,
                    0,
                    SCROLL_ARROW_WIDTH,
                    SCROLL_ARROW_HEIGHT
            );
        }
    }

    /**
     * Native: ShopSelectionGridVisualObject::drawRightScrollHoverButton @004B4F28.
     * Full port.
     */
    private void drawRightScrollHoverButton() {
        CRect screenRect = getSelectionGridScreenRect();
        if (GUI.invArrow2 != null) {
            GUI.invArrow4.drawRectMasked(
                    screenRect.left + RIGHT_SCROLL_BUTTON_X_OFFSET,
                    screenRect.top,
                    0,
                    0,
                    SCROLL_ARROW_WIDTH,
                    SCROLL_ARROW_HEIGHT
            );
        }
    }

    /**
     * Native: ShopSelectionGridVisualObject::drawRightScrollAvailableButton @004B4F86.
     * Full port.
     */
    private void drawRightScrollAvailableButton() {
        CRect screenRect = getSelectionGridScreenRect();
        if (GUI.invArrow4 != null) {
            GUI.invArrow2.drawRectMasked(
                    screenRect.left + RIGHT_SCROLL_BUTTON_X_OFFSET,
                    screenRect.top,
                    0,
                    0,
                    SCROLL_ARROW_WIDTH,
                    SCROLL_ARROW_HEIGHT
            );
        }
    }

    /**
     * Native support extracted from ShopSelectionGridVisualObject scroll-button draw helpers @004B4E78, @004B4ED0,
     * @004B4F28, and @004B4F86.
     */
    private CRect getSelectionGridScreenRect() {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        return screenRect;
    }

    /**
     * Native support extracted from ShopSelectionGridVisualObject::GetText @004B4BA3,
     * Update @004B4CD6, and OnLButtonDown @004B5038.
     */
    private Point toOwnerLocalPoint(int screenX, int screenY) {
        CRect ownerScreenRect = new CRect();
        ownerDialog.clientToScreen(ownerScreenRect, ownerDialog.cRect);
        return new Point(screenX - ownerScreenRect.left, screenY - ownerScreenRect.top);
    }

    /**
     * Native support extracted from ShopSelectionGridVisualObject::Update @004B4CD6.
     */
    private boolean isMouseOverOwnerRect(CRect rect) {
        Point ownerLocalPoint = toOwnerLocalPoint(
                Globals.mousePointer.getX(),
                Globals.mousePointer.getY()
        );
        return rect.contains(ownerLocalPoint.x, ownerLocalPoint.y);
    }

    /**
     * Native helper thunk pair: Sound::StopAndRewindPointerSound @004385B0 + Sound::PlayPointer @00438570.
     */
    private void playScrollSound() {
        scrollSound.playFresh();
    }

    /**
     * Native support extracted from the carried TokenEntry +0x18 grid-mode read in
     * ShopSelectionGridVisualObject::OnMouseMove @004B5156.
     */
    private static int getPayloadGridModeCode(Object payload) {
        return payload instanceof TokenEntry tokenEntry ? tokenEntry.gridModeCode : 0;
    }
}
