package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CCursor;
import ua.millfreedom.rom2.model.CGameBitmap;
import ua.millfreedom.rom2.model.CMousePointer;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.model.window.DialogsMaskFlag;

import java.awt.Point;

import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.INN_DIALOG;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.MODAL_DIALOG;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.SHOP_DIALOG;
import static ua.millfreedom.rom2.model.window.windowproc.handlers.CMainWindowWindowProcSupport.readMessageInt;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_ATTACK_A_0;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_CAST_C_4;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_DEFEND_D_3;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_GUARD_G_2;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_MOVE_M_1;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_RETREAT_R_7;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_STAND_GROUND_T_6;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_SWARM_S_5;

/**
 * Native class: OrderToolbarVisualObject.
 * Purpose: right-side order toolbar that exposes up to eight order buttons for the active map context.
 */
public class OrderToolbarVisualObject extends CVisualObject {
    public static final int NATIVE_SIZE = 0x70; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int[] ORDER_TOOLTIP_INDICES = {
            MAIN_ATTACK_A_0,
            MAIN_MOVE_M_1,
            MAIN_GUARD_G_2,
            MAIN_DEFEND_D_3,
            MAIN_CAST_C_4,
            MAIN_SWARM_S_5,
            MAIN_STAND_GROUND_T_6,
            MAIN_RETREAT_R_7
    };

    private static final int ORDER_SLOT_COUNT = 8;
    private static final int SLOT_WIDTH = 0x22;
    private static final int SLOT_HEIGHT = 0x22;
    private static final int SLOT_LEFT = 8;
    private static final int SLOT_TOP = 7;
    private static final int SPELLBOOK_ORDER_SLOT = 4;

    //0x5c
    public CVisualObject mapContext0x5c;
    //0x60
    public int selectedOrder0x60;
    //0x64
    public int availableOrdersMask0x64;
    //0x68
    public int toolbarEnabled0x68;
    //0x6c
    public int dirtyFlag0x6c;

    /**
     * Native: OrderToolbarVisualObject::OrderToolbarVisualObject @004AD541.
     * Full port.
     */
    public OrderToolbarVisualObject() {
        super();
        initializeOrderToolbarFields();
    }

    /**
     * Native: OrderToolbarVisualObject::OrderToolbarVisualObject @004AD5D3.
     * Full port.
     */
    public OrderToolbarVisualObject(int id, CRect rect) {
        super(id, rect, null);
        initializeOrderToolbarFields();
    }

    /**
     * Native: OrderToolbarVisualObject::OrderToolbarVisualObject @004AD57E.
     * Full port.
     */
    public OrderToolbarVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        initializeOrderToolbarFields();
    }

    /**
     * Native support extracted from OrderToolbarVisualObject constructors @004AD541, @004AD57E, and @004AD5D3.
     */
    private void initializeOrderToolbarFields() {
        this.mapContext0x5c = null;
        this.selectedOrder0x60 = -1;
        this.availableOrdersMask0x64 = 0;
        this.toolbarEnabled0x68 = 0;
        this.dirtyFlag0x6c = 0;
    }

    /**
     * vtbl +0x14: OrderToolbarVisualObject::GetText @004AD61C.
     * Full port.
     */
    @Override
    public String getText() {
        if (toolbarEnabled0x68 == 0) {
            return null;
        }

        if (DialogsMaskFlag.containsAny(dialogModeFlags(), SHOP_DIALOG, INN_DIALOG, MODAL_DIALOG)
                || Globals.mainWindow.uiLockPayload != null) {
            return null;
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        Point mouse = new Point(Globals.mousePointer.getX(), Globals.mousePointer.getY());
        for (int slot = 0; slot < ORDER_SLOT_COUNT; slot++) {
            if (!isOrderSlotEnabled(slot)) {
                continue;
            }
            if (getOrderSlotRect(screenRect, slot).contains(mouse.x, mouse.y)) {
                return get(ORDER_TOOLTIP_INDICES[slot]);
            }
        }
        return null;
    }

    /**
     * vtbl +0x2C: OrderToolbarVisualObject::Update @004ADB65.
     * Full port.
     */
    @Override
    public void update() {
        if (!isGameplayVisualDialogMode()) {
            return;
        }
        renderOrderToolbar();
    }

    /**
     * Native support extracted from OrderToolbarVisualObject::Update @004ADB65 render body.
     */
    private void renderOrderToolbar() {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        Globals.renderer.lockSurface();
        try {
            if (toolbarEnabled0x68 == 0) {
                GUI.headsR.draw(screenRect.left, screenRect.top, 0, null, false);
            } else {
                GUI.commandBarR.draw(screenRect.left, screenRect.top, 0, null, false);
                for (int slot = 0; slot < ORDER_SLOT_COUNT; slot++) {
                    if (!isOrderSlotEnabled(slot)) {
                        drawOrderSlotOverlay(GUI.commandEmpR, screenRect, slot);
                    }
                }
                if (selectedOrder0x60 >= 0) {
                    drawOrderSlotOverlay(GUI.commandDnR, screenRect, selectedOrder0x60);
                }
            }
        } finally {
            Globals.renderer.unlockSurface();
        }
        dirtyFlag0x6c = 0;
    }

    /**
     * vtbl +0x48: OrderToolbarVisualObject::OnMessage @004AD753.
     * Full port.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        int result = super.onMessage(msg, wParam, lParam);
        if (result != 0) {
            return result;
        }

        return switch (msg) {
            case RENDER_FRAME -> {
                if (isGameplayVisualDialogMode()) {
                    draw();
                }
                if (isNonModalGameplayDialogMode()) {
                    updateCursorForHover();
                }
                yield 0;
            }
            case SET_MAP_CONTEXT -> {
                mapContext0x5c = (CVisualObject) wParam;
                yield 0;
            }
            case NOTIFY_SELECTION_PANEL -> {
                final int w = readMessageInt(wParam);
                if (toolbarEnabled0x68 == 0 || availableOrdersMask0x64 != w) {
                    toolbarEnabled0x68 = 1;
                    dirtyFlag0x6c = 1;
                    availableOrdersMask0x64 = w;
                }
                yield 1;
            }
            case NOTIFY_SELECTION_OVERLAY -> {
                if (toolbarEnabled0x68 != 0) {
                    toolbarEnabled0x68 = 0;
                    dirtyFlag0x6c = 1;
                }
                yield 1;
            }
            case RESET_ORDER_SELECTION -> {
                if (toolbarEnabled0x68 != 0) {
                    selectedOrder0x60 = -1;
                    dirtyFlag0x6c = 1;
                }
                yield 0;
            }
            case SELECT_ORDER_IN_TOOLBAR -> {
                final int w = readMessageInt(wParam);
                if (toolbarEnabled0x68 != 0
                        && selectedOrder0x60 != w
                        && (availableOrdersMask0x64 & (1 << (w & 0x1F))) != 0) {
                    selectedOrder0x60 = w;
                    dirtyFlag0x6c = 1;
                }
                yield 1;
            }
            default -> 0;
        };
    }

    /**
     * vtbl +0x4C: OrderToolbarVisualObject::OnMouseMove @004ADB35.
     * Full port.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        if ((nFlags & 0x1) != 0) {
            onLButtonDown(nFlags, x, y);
        }
        return 0;
    }

    /**
     * vtbl +0x54: OrderToolbarVisualObject::OnLButtonDown @004AD8FD.
     * Full port.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        if (Globals.mainWindow.uiLockPayload != null) {
            return 1;
        }

        if (toolbarEnabled0x68 == 0) {
            return 1;
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        CVisualObject mapContext = resolveMapContext();
        for (int slot = 0; slot < ORDER_SLOT_COUNT; slot++) {
            if (!isOrderSlotEnabled(slot)) {
                continue;
            }
            if (!getOrderSlotRect(screenRect, slot).contains(x, y)) {
                continue;
            }
            if (selectedOrder0x60 == slot) {
                return 1;
            }
            if (slot == SPELLBOOK_ORDER_SLOT && mapContext.hasSpellPanelChild()) {
                return 1;
            }

            selectedOrder0x60 = slot;
            draw();
            mapContext.onMessage(MessageCodes.EXECUTE_ORDER, slot, 0);
            return 1;
        }

        selectedOrder0x60 = -1;
        draw();
        mapContext.onMessage(MessageCodes.EXECUTE_ORDER, -1, 0);
        return 1;
    }

    /**
     * vtbl +0x58: OrderToolbarVisualObject::OnLButtonUp @004ADACE.
     * Full port.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        if (Globals.mainWindow.uiLockPayload != null) {
            restoreCursorAfterUiLockClick();
        }
        return 1;
    }

    /**
     * vtbl +0x5C: OrderToolbarVisualObject::OnLButtonDblClk @004ADA95.
     * Full port.
     */
    @Override
    public int onLButtonDblClk(int nFlags, int x, int y) {
        return onLButtonDown(nFlags | 1, x, y);
    }

    /**
     * vtbl +0x60: OrderToolbarVisualObject::OnRButtonDown @004ADA83.
     * Full port.
     */
    @Override
    public int onRButtonDown(int nFlags, int x, int y) {
        return 1;
    }

    /**
     * vtbl +0x64: OrderToolbarVisualObject::OnRButtonUp @004ADB09.
     * Full port.
     */
    @Override
    public int onRButtonUp(int nFlags, int x, int y) {
        return resolveMapContext().onMessage(MessageCodes.REFRESH_LAYOUT, 0, 0);
    }

    /**
     * vtbl +0x68: OrderToolbarVisualObject::OnRButtonDblClk @004ADABC.
     * Full port.
     */
    @Override
    public int onRButtonDblClk(int nFlags, int x, int y) {
        return 1;
    }

    /**
     * Native: OrderToolbarVisualObject::UpdateCursorForHover @004ADCEA.
     * Full port.
     */
    private void updateCursorForHover() {
        CMainWindow mainWindow = Globals.mainWindow;
        CGameBitmap sourceBitmap = Globals.mousePointer.getSourceBitmap();
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        CCursor nextCursor = null;
        int mouseX = Globals.mousePointer.getX();
        int mouseY = Globals.mousePointer.getY();
        if (!screenRect.contains(mouseX, mouseY)) {
            return;
        }

        if (mouseX >= Globals.screenRect.right - 2 && isNonModalGameplayDialogMode()) {
            if (mouseY == 0) {
                nextCursor = CMousePointer.Cursor_ArrowNE;
            } else if (mouseY < Globals.screenRect.bottom - 2) {
                nextCursor = CMousePointer.Cursor_ArrowE;
            } else {
                nextCursor = CMousePointer.Cursor_ArrowSE;
            }
        } else {
            nextCursor = CMousePointer.Cursor_Default;
        }
        if (mainWindow.uiLockPayload != null) {
            nextCursor = mainWindow.cursor;
        }

        if (nextCursor != null && sourceBitmap != nextCursor.getBitmap()) {
            nextCursor.setToMousePointer();
        }
    }

    /**
     * Native slot-rect math shared by OrderToolbarVisualObject::GetText @004AD61C and ::OnLButtonDown @004AD8FD.
     * Full support port.
     */
    private CRect getOrderSlotRect(CRect screenRect, int slot) {
        int slotLeft = screenRect.left + SLOT_LEFT + ((slot & 3) * SLOT_WIDTH);
        int slotTop = screenRect.top + SLOT_TOP + ((slot >> 2) * SLOT_HEIGHT);
        return new CRect(slotLeft, slotTop, slotLeft + SLOT_WIDTH, slotTop + SLOT_HEIGHT);
    }

    /**
     * Native support extracted from OrderToolbarVisualObject::GetText @004AD61C,
     * OrderToolbarVisualObject::OnMessage @004AD753, OrderToolbarVisualObject::OnLButtonDown @004AD8FD,
     * and OrderToolbarVisualObject::Update @004ADB65.
     * Full support port.
     */
    private boolean isOrderSlotEnabled(int slot) {
        return slot >= 0
                && slot < ORDER_SLOT_COUNT
                && (availableOrdersMask0x64 & (1 << slot)) != 0;
    }

    /**
     * Native owner: OrderToolbarVisualObject::Update @004ADB65.
     * Full support port.
     */
    private static void drawOrderSlotOverlay(ua.millfreedom.rom2.model.CBmp64k bitmap, CRect screenRect, int slot) {
        int srcLeft = (slot & 3) * SLOT_WIDTH;
        int srcTop = (slot >> 2) * SLOT_HEIGHT;
        bitmap.drawRect(
                screenRect.left + SLOT_LEFT + srcLeft,
                screenRect.top + SLOT_TOP + srcTop,
                SLOT_LEFT + srcLeft,
                SLOT_TOP + srcTop,
                srcLeft + 0x2A,
                srcTop + 0x29
        );
    }

    /**
     * Native context pointer assignment in OrderToolbarVisualObject::OnMessage @004AD753.
     * Full support port.
     */
    private CVisualObject resolveMapContext() {
        return mapContext0x5c;
    }

    /**
     * Native tail of OrderToolbarVisualObject::OnLButtonUp @004ADACE.
     * Full support port.
     */
    private void restoreCursorAfterUiLockClick() {
        CMousePointer.Cursor_Default.setToMousePointer();
        Globals.mainWindow.clearUiLockState();
    }
}
