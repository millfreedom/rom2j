package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.window.CMainWindow;

import java.awt.*;
import java.util.Locale;

import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.SHOP_DIALOG;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_BUY_70;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_EXIT_73;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_SELL_71;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_UNDO_72;

/**
 * Native class: ShopRingButtonsVisualObject.
 * Purpose: shop-dialog right-side action button strip.
 */
public class ShopRingButtonsVisualObject extends CVisualObject {
    public static final int NATIVE_SIZE = 0x100; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int BUTTON_COUNT = 4;
    private static final int MK_LBUTTON = 0x1;
    private static final int INVALID_BUTTON_INDEX = -1;
    private static final int RING_BUTTONS_HOVER_REGION = 0x65;
    private static final int BUTTON_TEXT_ALIGN = TextAlign.combine(TextAlign.CENTER, TextAlign.VERTICAL_CENTER);
    private static final int NORMAL_LABEL_OFFSET_Y = 6;
    private static final int SELL_LABEL_OFFSET_Y = 8;
    private static final int NORMAL_AMOUNT_OFFSET_Y = -2;
    private static final int SELL_AMOUNT_OFFSET_Y = 0;
    private static final int PRESSED_NORMAL_LABEL_OFFSET_Y = 7;
    private static final int PRESSED_SELL_LABEL_OFFSET_Y = 9;
    private static final int PRESSED_NORMAL_AMOUNT_OFFSET_Y = -1;
    private static final int PRESSED_SELL_AMOUNT_OFFSET_Y = 1;
    private static final String GRAPHICS_INTERFACE_DIR = "graphics/interface/";
    private static final String SHOP_BUTTON_BMP_PATTERN = "shopbutton%d.bmp";
    private static final String SHOP_MENU_BMP = "shopmenu.bmp";

    //0x5c
    public ShopDialogVisualObject ownerDialog;
    //0x64
    public CBmp64k undoButtonBitmap;
    //0x68
    public CBmp64k buyButtonBitmap;
    //0x6c
    public CBmp64k sellButtonBitmap;
    //0x70
    public CBmp64k exitButtonBitmap;
    //0x74
    public CBmp64k menuBitmap;
    //0x78
    public final CRect[] outerButtonRects = {new CRect(), new CRect(), new CRect(), new CRect()};
    //0xb8
    public final CRect[] innerButtonRects = {new CRect(), new CRect(), new CRect(), new CRect()};
    //0xf8
    public int pressedButtonIndex;
    //0xfc
    public int hoveredButtonIndex;

    /**
     * Native: ShopRingButtonsVisualObject::ShopRingButtonsVisualObject @004BC2B0.
     * Full port.
     */
    public ShopRingButtonsVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            ShopDialogVisualObject ownerContext
    ) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        this.ownerDialog = ownerContext;

        this.outerButtonRects[0].set(0x1EE, 0x0F, 0x266, 0x43);
        this.outerButtonRects[1].set(0x1E3, 0x43, 0x26F, 0x71);
        this.outerButtonRects[2].set(0x1E3, 0x72, 0x26F, 0xA0);
        this.outerButtonRects[3].set(0x1EE, 0xA0, 0x266, 0xD4);
        this.innerButtonRects[0].set(0x203, 0x0F, 0x24E, 0x23);
        this.innerButtonRects[1].set(0x1EE, 0x23, 0x27B, 0x43);
        this.innerButtonRects[2].set(0x1EE, 0xA0, 0x266, 0xC0);
        this.innerButtonRects[3].set(0x203, 0xC0, 0x24E, 0xD4);
        this.undoButtonBitmap = null;
        this.buyButtonBitmap = null;
        this.sellButtonBitmap = null;
        this.exitButtonBitmap = null;
        this.menuBitmap = null;
        this.pressedButtonIndex = INVALID_BUTTON_INDEX;
        this.hoveredButtonIndex = INVALID_BUTTON_INDEX;
    }

    /**
     * vtbl +0x14: ShopRingButtonsVisualObject::GetText @004BCCF8.
     */
    @Override
    public String getText() {
        return null;
    }

    /**
     * vtbl +0x2C: ShopRingButtonsVisualObject::Update @004BCD05.
     * Full port.
     */
    @Override
    public void update() {
        ShopDialogVisualObject resolvedOwnerDialog = ownerDialog;
        if (resolvedOwnerDialog.dialogActiveFlag == 0) {
            return;
        }
        if (undoButtonBitmap == null
                || buyButtonBitmap == null
                || sellButtonBitmap == null
                || exitButtonBitmap == null
                || menuBitmap == null) {
            return;
        }
        Globals.renderer.lockSurface();
        try {
            renderRingButtons(resolvedOwnerDialog);
        } finally {
            Globals.renderer.unlockSurface();
        }
    }

    /**
     * Native: ShopRingButtonsVisualObject::InitializeButtonState @004BDBAC.
     */
    public void initializeButtonState() {
        pressedButtonIndex = INVALID_BUTTON_INDEX;
        hoveredButtonIndex = INVALID_BUTTON_INDEX;
    }

    /**
     * Native: ShopRingButtonsVisualObject::LoadButtonBitmaps @004BC8DE.
     */
    public void loadButtonBitmaps() {
        releaseButtonBitmaps();
        String resourcePrefix = resolveResourcePrefix(ownerDialog);
        undoButtonBitmap = new CBmp64k(resourcePrefix + String.format(Locale.ROOT, SHOP_BUTTON_BMP_PATTERN, 1));
        Globals.renderer.refreshMousePointer();
        buyButtonBitmap = new CBmp64k(resourcePrefix + String.format(Locale.ROOT, SHOP_BUTTON_BMP_PATTERN, 2));
        Globals.renderer.refreshMousePointer();
        sellButtonBitmap = new CBmp64k(resourcePrefix + String.format(Locale.ROOT, SHOP_BUTTON_BMP_PATTERN, 3));
        Globals.renderer.refreshMousePointer();
        exitButtonBitmap = new CBmp64k(resourcePrefix + String.format(Locale.ROOT, SHOP_BUTTON_BMP_PATTERN, 4));
        Globals.renderer.refreshMousePointer();
        menuBitmap = new CBmp64k(resourcePrefix + SHOP_MENU_BMP);
        Globals.renderer.refreshMousePointer();
    }

    /**
     * Native: ShopRingButtonsVisualObject::ReleaseButtonBitmaps @004BCBA6.
     */
    public void releaseButtonBitmaps() {
        undoButtonBitmap = null;
        buyButtonBitmap = null;
        sellButtonBitmap = null;
        exitButtonBitmap = null;
        menuBitmap = null;
    }

    /**
     * vtbl +0x4C: ShopRingButtonsVisualObject::OnMouseMove @004BD918.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        ShopDialogVisualObject resolvedOwnerDialog = ownerDialog;
        int hoveredControlRegion = getHoveredControlRegion();
        if (resolvedOwnerDialog.hoveredControlRegion != hoveredControlRegion) {
            resolvedOwnerDialog.hoveredControlRegion = hoveredControlRegion;
            resolvedOwnerDialog.dirtyFlags |= 0x2F;
        }
        updateHoveredButtonState(nFlags, x, y);
        return 0;
    }

    /**
     * vtbl +0x54: ShopRingButtonsVisualObject::OnLButtonDown @004BD988.
     * Full port.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        ShopDialogVisualObject resolvedOwnerDialog = ownerDialog;
        pressedButtonIndex = resolveButtonIndexAtPoint(x, y);
        resolvedOwnerDialog.dirtyFlags |= 0x20;
        resolvedOwnerDialog.playRingButtonsPressSound(pressedButtonIndex);
        return 1;
    }

    /**
     * vtbl +0x58: ShopRingButtonsVisualObject::OnLButtonUp @004BDA16.
     * Full port.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        ShopDialogVisualObject resolvedOwnerDialog = ownerDialog;
        CMainWindow mainWindow = Globals.mainWindow;

        if (mainWindow.getUiLockPayload() == null) {
            if (pressedButtonIndex >= 0
                    && pressedButtonIndex < BUTTON_COUNT
                    && resolveButtonIndexAtPoint(x, y) == pressedButtonIndex) {
                int activatedButtonIndex = pressedButtonIndex;
                pressedButtonIndex = INVALID_BUTTON_INDEX;
                updateHoveredButtonState(nFlags, x, y);
                switch (activatedButtonIndex) {
                    case 0 -> resolvedOwnerDialog.handleUndoAction();
                    case 1 -> resolvedOwnerDialog.handleBuyAction();
                    case 2 -> resolvedOwnerDialog.handleSellAction();
                    case 3 -> {
                        resolvedOwnerDialog.handleExitAction();
                        if (mainWindow.dialogsMask == SHOP_DIALOG.mask) {
                            mainWindow.postMessage(MessageCodes.SHOW_CURRENT_TOWN_DIALOG, 0, 0);
                        }
                    }
                    default -> {
                    }
                }
            }
            resolvedOwnerDialog.dirtyFlags |= 0x20;
            pressedButtonIndex = INVALID_BUTTON_INDEX;
            updateHoveredButtonState(nFlags, x, y);
        } else {
            resolvedOwnerDialog.cancelUiLockSelection();
            pressedButtonIndex = INVALID_BUTTON_INDEX;
            updateHoveredButtonState(nFlags, x, y);
            resolvedOwnerDialog.dirtyFlags |= 0x20;
        }
        return 1;
    }

    /**
     * Native helper: ShopRingButtonsVisualObject::ResolveButtonIndexAtPoint @004BDBD1.
     */
    private int resolveButtonIndexAtPoint(int x, int y) {
        Point ownerTopLeft = getOwnerTopLeftScreenPoint(ownerDialog);
        int localX = x - ownerTopLeft.x;
        int localY = y - ownerTopLeft.y;
        for (int buttonIndex = 0; buttonIndex < BUTTON_COUNT; buttonIndex++) {
            if (!getOuterButtonRect(buttonIndex).contains(localX, localY)) {
                continue;
            }
            if (buttonIndex == 0) {
                if (innerButtonRects[0].contains(localX, localY) || innerButtonRects[1].contains(localX, localY)) {
                    return 0;
                }
                return INVALID_BUTTON_INDEX;
            }
            if (buttonIndex == 3) {
                if (innerButtonRects[2].contains(localX, localY) || innerButtonRects[3].contains(localX, localY)) {
                    return 3;
                }
                return INVALID_BUTTON_INDEX;
            }
            return buttonIndex;
        }
        return INVALID_BUTTON_INDEX;
    }

    /**
     * vtbl +0x78: ShopRingButtonsVisualObject::GetHoveredControlRegion @004C3400.
     */
    private int getHoveredControlRegion() {
        return RING_BUTTONS_HOVER_REGION;
    }

    /**
     * Native helper: ShopRingButtonsVisualObject::UpdateHoveredButtonState @004BDD18.
     * Partial port.
     */
    private void updateHoveredButtonState(int nFlags, int x, int y) {
        int hoveredButtonIndex = resolveButtonIndexAtPoint(x, y);
        if (hoveredButtonIndex < 0 || (nFlags & MK_LBUTTON) != 0) {
            if (hoveredButtonIndex >= 0 && hoveredButtonIndex == pressedButtonIndex && (nFlags & MK_LBUTTON) != 0) {
                this.hoveredButtonIndex = hoveredButtonIndex;
            } else {
                this.hoveredButtonIndex = INVALID_BUTTON_INDEX;
            }
        } else {
            this.hoveredButtonIndex = hoveredButtonIndex;
        }

        ownerDialog.dirtyFlags |= 0x20;
    }

    /**
     * Native owner: gFont4/palette/button draw path inside ShopRingButtonsVisualObject::Update @004BCD05.
     * Native support extracted from ShopRingButtonsVisualObject::Update @004BCD05.
     */
    private void renderRingButtons(ShopDialogVisualObject resolvedOwnerDialog) {
        Point ownerTopLeft = getOwnerTopLeftScreenPoint(resolvedOwnerDialog);
        int ownerLeft = ownerTopLeft.x;
        int ownerTop = ownerTopLeft.y;
        menuBitmap.drawRectMasked(ownerLeft + cRect.left, ownerTop + cRect.top, 0, 0, cRect.width(), cRect.height());

        for (int buttonIndex = 0; buttonIndex < BUTTON_COUNT; buttonIndex++) {
            drawButtonLabelAndAmount(
                    ownerLeft,
                    ownerTop,
                    buttonIndex,
                    getButtonPalette(buttonIndex),
                    getNormalLabelOffsetY(buttonIndex),
                    getNormalAmountOffsetY(buttonIndex),
                    resolvedOwnerDialog
            );
        }

        if (pressedButtonIndex >= 0 && hoveredButtonIndex == pressedButtonIndex) {
            CBmp64k hotButtonBitmap = getHotButtonBitmap();
            if (hotButtonBitmap != null) {
                CRect buttonRect = getOuterButtonRect(pressedButtonIndex);
                hotButtonBitmap.drawRectMasked(ownerLeft + buttonRect.left,
                        ownerTop + buttonRect.top,
                        0,
                        0,
                        buttonRect.width(),
                        buttonRect.height());
                drawButtonLabelAndAmount(
                        ownerLeft,
                        ownerTop,
                        pressedButtonIndex,
                        Palettes.p2.paletteData[0],
                        getPressedLabelOffsetY(pressedButtonIndex),
                        getPressedAmountOffsetY(pressedButtonIndex),
                        resolvedOwnerDialog
                );
            }
        }
    }

    /**
     * Native support extracted from centered `gFont4` label and amount drawing inside
     * ShopRingButtonsVisualObject::Update @004BCD05.
     */
    private void drawButtonLabelAndAmount(
            int ownerLeft,
            int ownerTop,
            int buttonIndex,
            Palette16 palette,
            int labelOffsetY,
            int amountOffsetY,
            ShopDialogVisualObject resolvedOwnerDialog
    ) {
        CRect buttonRect = getOuterButtonRect(buttonIndex);
        int centerX = ownerLeft + buttonRect.left + buttonRect.width() / 2;
        Globals.fonts.font4.drawTextInternal(
                centerX,
                ownerTop + buttonRect.top + buttonRect.height() / 4 + labelOffsetY,
                getButtonLabel(buttonIndex),
                BUTTON_TEXT_ALIGN,
                palette
        );
        Globals.fonts.font4.drawTextInternal(
                centerX,
                ownerTop + buttonRect.top + (buttonRect.height() * 3) / 4 + amountOffsetY,
                Utils.formatDecimalThousands(getButtonAmount(buttonIndex, resolvedOwnerDialog)),
                BUTTON_TEXT_ALIGN,
                palette
        );
    }

    /**
     * Native support extracted from the `g_pPalette1` / `g_pPalette2` choice in
     * ShopRingButtonsVisualObject::Update @004BCD05.
     */
    private Palette16 getButtonPalette(int buttonIndex) {
        return hoveredButtonIndex == buttonIndex ? Palettes.p2.paletteData[0] : Palettes.p1.paletteData[0];
    }

    /**
     * Native support extracted from string-table label lookups in ShopRingButtonsVisualObject::Update @004BCD05.
     */
    private static String getButtonLabel(int buttonIndex) {
        return switch (buttonIndex) {
            case 0 -> get(MAIN_UNDO_72);
            case 1 -> get(MAIN_BUY_70);
            case 2 -> get(MAIN_SELL_71);
            case 3 -> get(MAIN_EXIT_73);
            default -> throw new IndexOutOfBoundsException("buttonIndex: " + buttonIndex);
        };
    }

    /**
     * Native support extracted from amount-field reads in ShopRingButtonsVisualObject::Update @004BCD05.
     */
    private static int getButtonAmount(int buttonIndex, ShopDialogVisualObject resolvedOwnerDialog) {
        return switch (buttonIndex) {
            case 0 -> resolvedOwnerDialog.currentGold;
            case 1 -> resolvedOwnerDialog.pendingBuyGoldDelta;
            case 2 -> resolvedOwnerDialog.pendingSellGoldValue;
            case 3 -> resolvedOwnerDialog.resultingGold;
            default -> throw new IndexOutOfBoundsException("buttonIndex: " + buttonIndex);
        };
    }

    /**
     * Native support extracted from normal label Y offsets in ShopRingButtonsVisualObject::Update @004BCD05.
     */
    private static int getNormalLabelOffsetY(int buttonIndex) {
        return buttonIndex == 2 ? SELL_LABEL_OFFSET_Y : NORMAL_LABEL_OFFSET_Y;
    }

    /**
     * Native support extracted from normal amount Y offsets in ShopRingButtonsVisualObject::Update @004BCD05.
     */
    private static int getNormalAmountOffsetY(int buttonIndex) {
        return buttonIndex == 2 ? SELL_AMOUNT_OFFSET_Y : NORMAL_AMOUNT_OFFSET_Y;
    }

    /**
     * Native support extracted from pressed label Y offsets in ShopRingButtonsVisualObject::Update @004BCD05.
     */
    private static int getPressedLabelOffsetY(int buttonIndex) {
        return buttonIndex == 2 ? PRESSED_SELL_LABEL_OFFSET_Y : PRESSED_NORMAL_LABEL_OFFSET_Y;
    }

    /**
     * Native support extracted from pressed amount Y offsets in ShopRingButtonsVisualObject::Update @004BCD05.
     */
    private static int getPressedAmountOffsetY(int buttonIndex) {
        return buttonIndex == 2 ? PRESSED_SELL_AMOUNT_OFFSET_Y : PRESSED_NORMAL_AMOUNT_OFFSET_Y;
    }

    /**
     * Java helper for the owner screen-space anchor used by the native ring-button hit tests.
     * Native support extracted from owner-relative coordinate math in ShopRingButtonsVisualObject::Update @004BCD05
     * and ShopRingButtonsVisualObject::ResolveButtonIndexAtPoint @004BDBD1.
     */
    private Point getOwnerTopLeftScreenPoint(ShopDialogVisualObject resolvedOwnerDialog) {
        CRect ownerScreenRect = new CRect();
        resolvedOwnerDialog.clientToScreen(ownerScreenRect, resolvedOwnerDialog.cRect);
        return new Point(ownerScreenRect.left, ownerScreenRect.top);
    }

    /**
     * Java helper for the native outer button-rect table.
     * Native support extracted from the outer button-rect table initialized by
     * ShopRingButtonsVisualObject::ShopRingButtonsVisualObject @004BC2B0.
     */
    private CRect getOuterButtonRect(int buttonIndex) {
        return switch (buttonIndex) {
            case 0, 1, 2, 3 -> outerButtonRects[buttonIndex];
            default -> new CRect();
        };
    }

    /**
     * Native support extracted from ShopRingButtonsVisualObject::Update @004BCD05.
     */
    private CBmp64k getHotButtonBitmap() {
        return switch (hoveredButtonIndex) {
            case 0 -> undoButtonBitmap;
            case 1 -> buyButtonBitmap;
            case 2 -> sellButtonBitmap;
            case 3 -> exitButtonBitmap;
            default -> null;
        };
    }

    /**
     * Native support extracted from ShopRingButtonsVisualObject::LoadButtonBitmaps @004BC8DE.
     */
    private static String resolveResourcePrefix(ShopDialogVisualObject resolvedOwnerDialog) {
        String shopResourceDirectory = resolvedOwnerDialog == null ? "" : resolvedOwnerDialog.getShopResourceDirectory();
        return GRAPHICS_INTERFACE_DIR + shopResourceDirectory;
    }
}
