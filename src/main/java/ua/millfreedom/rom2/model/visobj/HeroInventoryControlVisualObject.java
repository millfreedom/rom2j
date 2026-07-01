package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.*;
import ua.millfreedom.rom2.model.color.RGB16;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.enums.ShapeId;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.res.Resources;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static ua.millfreedom.rom2.model.window.windowproc.handlers.CMainWindowWindowProcSupport.readMessageInt;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.MODAL_DIALOG;
import static ua.millfreedom.rom2.res.Constants.GRAPHICS;
import static ua.millfreedom.rom2.res.Constants.INTERFACE;
import static ua.millfreedom.rom2.res.Constants.MONEY;
import static ua.millfreedom.rom2.res.Constants.MONEY_16A;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_MONEY_74;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_NO_HERO_SELECTED_51;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_SCROLL_LEFT_54;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_SCROLL_RIGHT_55;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_YOUR_HERO_S_INVENTORY_58;

/**
 * Native class: HeroInventoryControlVisualObject.
 * Purpose: hero-inventory control with scroll arrows, money handling, and player-slot assignment hooks.
 */
public class HeroInventoryControlVisualObject extends GridOverlayVisualObject {
    public static final int NATIVE_SIZE = 0x20AC; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int CELL_SIZE = 0x50;
    private static final int CELL_TOP_OFFSET = 6;
    private static final int CONTENT_LEFT_OFFSET = 0x20;
    private static final int LEFT_ARROW_WIDTH = 0x20;
    private static final int LEFT_ARROW_HEIGHT = 0x58;
    private static final int INVENTORY_TOP_SEAM_HEIGHT = 2;
    private static final int INVENTORY_TOP_SEAM_SOURCE_TOP = 2;
    private static final int FONT2_TEXT_X_OFFSET = 0x23;
    private static final int FONT3_TEXT_Y_OFFSET = 8;
    private static final int QUANTITY_TEXT_BOTTOM_OFFSET = 0x0E;
    private static final int SHIFT_MONEY_DRAG_AMOUNT = 1000;
    private static final int MAX_PLAYER_SLOTS = 9;
    private static final int PLAYER_MARKER_BASE = 4;
    private static final int TEXT_PALETTE_YELLOWISH = 0;
    private static final int TEXT_PALETTE_GRAY = 1;
    private static final int DYNAMIC_PAYLOAD_FLAG = 0x20;
    // Tint starts here. Raise it to keep more dark pixels untouched; lower it to tint shadows too.
    private static final int INVENTORY_BACKDROP_DARK_LUMINANCE = 24;
    // Tint reaches exact ShapeId.color here. Lower it for stronger saturation; raise it for more shaded highlights.
    private static final int INVENTORY_BACKDROP_LIGHT_LUMINANCE = 94;
    // Channel blend scale. This is byte-like 0..255 math; changing it requires retuning the curve weights below.
    private static final int RGB_WEIGHT_SCALE = 255;
    // Current "dirty" curve weight. Higher keeps the old squared falloff sharper and less smooth.
    private static final int INVENTORY_BACKDROP_SQUARED_CURVE_WEIGHT = 3;
    // Smoother curve weight. Higher smooths the highlight ramp more, especially near the brightest pixels.
    private static final int INVENTORY_BACKDROP_SMOOTHERSTEP_CURVE_WEIGHT = 1;
    // Standard RGB luminance weights. Raising one makes that channel count more when detecting bright frame pixels.
    private static final int RED_LUMINANCE_WEIGHT = 77;
    private static final int GREEN_LUMINANCE_WEIGHT = 150;
    private static final int BLUE_LUMINANCE_WEIGHT = 29;
    private static final String INVENTORY_DIRECTORY = "inventory";
    private static final String A16_SUFFIX = ".16a";
    // Java support, not a native field.
    private static CBmp64k tintedInventoryBackdropSource;
    // Java support, not a native field.
    private static final Map<ShapeId, CBmp64k> tintedInventoryBackdrops = new EnumMap<>(ShapeId.class);

    /**
     * Native: HeroInventoryControlVisualObject::HeroInventoryControlVisualObject @004A2B2B.
     * Fully ported.
     */
    public HeroInventoryControlVisualObject() {
        super();
        this.visibleColumns = cRect.width() / CELL_SIZE - 1;
        this.visibleRows = cRect.height() / CELL_SIZE;
        initArrays();
    }

    /**
     * Native: HeroInventoryControlVisualObject::HeroInventoryControlVisualObject @004A2C5F.
     * Fully ported.
     */
    public HeroInventoryControlVisualObject(int id, CRect rect) {
        super(id, rect);
        this.visibleColumns = cRect.width() / CELL_SIZE - 1;
        this.visibleRows = cRect.height() / CELL_SIZE;
        initArrays();
    }

    /**
     * Native: HeroInventoryControlVisualObject::HeroInventoryControlVisualObject @004A2BBA.
     * Fully ported.
     */
    public HeroInventoryControlVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        super(id, xLeft, yTop, xRight, yBottom);
        this.visibleColumns = cRect.width() / CELL_SIZE - 1;
        this.visibleRows = cRect.height() / CELL_SIZE;
        initArrays();
    }

    /**
     * vtbl +0x14: HeroInventoryControlVisualObject::GetText @004A3F0B.
     * Fully ported for TokenEntry-backed hero inventory entries.
     */
    @Override
    public String getText() {
        CMainWindow mainWindow = Globals.mainWindow;
        if (mainWindow.getUiLockPayload() != null || mainWindow.dialogsMask != GAMEPLAY.mask) {
            return null;
        }

        int horizontalInset = getHeroInventoryHorizontalInset();
        int mouseX = Globals.mousePointer.getX();
        if (mouseX < horizontalInset + CONTENT_LEFT_OFFSET) {
            return get(MAIN_SCROLL_LEFT_54);
        }
        if (mouseX >= horizontalInset + CONTENT_LEFT_OFFSET + visibleColumns * CELL_SIZE) {
            return get(MAIN_SCROLL_RIGHT_55);
        }
        if (gridSource == null) {
            return get(MAIN_YOUR_HERO_S_INVENTORY_58);
        }

        int gridIndex = getVisibleStartValue() + ((mouseX - CONTENT_LEFT_OFFSET) - horizontalInset) / CELL_SIZE;
        if (gridIndex < 0 || gridIndex >= getGridSourceSize()) {
            return get(MAIN_YOUR_HERO_S_INVENTORY_58);
        }

        Object entry = getGridSourceEntry(gridIndex);
        if (entry == null) {
            return null;
        }
        if (isMoneyEntry(entry)) {
            return get(MAIN_MONEY_74);
        }
        String tooltip = resolveSelectionEntryTooltip(entry);
        return appendAltEquippedSlotTooltip(entry, tooltip);
    }

    /**
     * vtbl +0x2C: HeroInventoryControlVisualObject::Update @004A2CF8.
     * Fully ported.
     */
    @Override
    public void update() {
        CMainWindow mainWindow = Globals.mainWindow;
        refreshVisibleCellSprites();

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        int horizontalInset = getHeroInventoryHorizontalInset();

        drawHeroInventoryFrame(screenRect, horizontalInset);

        CRect leftArrowRect = new CRect(screenRect.left, screenRect.top, screenRect.left + CONTENT_LEFT_OFFSET + horizontalInset, screenRect.bottom);
        CRect rightArrowRect = new CRect(
                screenRect.left + horizontalInset + CONTENT_LEFT_OFFSET + visibleColumns * CELL_SIZE,
                screenRect.top,
                screenRect.left + 0x40 + horizontalInset * 2 + visibleColumns * CELL_SIZE,
                screenRect.bottom
        );
        int mouseX = Globals.mousePointer.getX();
        int mouseY = Globals.mousePointer.getY();

        if (gridSource != null && getVisibleStartValue() != 0) {
            (leftArrowRect.contains(mouseX, mouseY) ? GUI.invArrow3 : GUI.invArrow1)
                    .drawRectMasked(screenRect.left + horizontalInset, screenRect.top + 2, 0, 0, LEFT_ARROW_WIDTH, LEFT_ARROW_HEIGHT);
        }
        if (gridSource != null && visibleColumns + getVisibleStartValue() < getGridSourceSize()) {
            (rightArrowRect.contains(mouseX, mouseY) ? GUI.invArrow4 : GUI.invArrow2).drawRectMasked(
                    screenRect.left + horizontalInset + CONTENT_LEFT_OFFSET + visibleColumns * CELL_SIZE,
                    screenRect.top + 2,
                    0,
                    0,
                    LEFT_ARROW_WIDTH,
                    LEFT_ARROW_HEIGHT
            );
        }

        if (gridSource == null) {
            drawTextShadowed(
                    Globals.fonts.font2,
                    horizontalInset + (screenRect.left - 0x10 + screenRect.right) / 2,
                    (screenRect.top + screenRect.bottom) / 2,
                    get(MAIN_NO_HERO_SELECTED_51),
                    TextAlign.combine(TextAlign.CENTER, TextAlign.VERTICAL_CENTER),
                    TEXT_PALETTE_YELLOWISH,
                    1
            );
            return;
        }

        int visibleCellCount = getVisibleCellCount();
        int visibleSourceCount = Math.min(getGridSourceSize(), visibleCellCount);
        for (int visibleIndex = 0; visibleIndex < visibleCellCount; visibleIndex++) {
            if (visibleIndex >= visibleSourceCount) {
                continue;
            }
            if (getCellUpdateCounter(visibleIndex) == 0) {
                continue;
            }

            Object entry = getGridSourceEntry(getVisibleStartValue() + visibleIndex);
            if (entry == null || isHeldSingleQuantityEntry(mainWindow, entry)) {
                continue;
            }

            int drawX = horizontalInset + screenRect.left + CONTENT_LEFT_OFFSET + visibleIndex * CELL_SIZE;
            int drawY = screenRect.top + CELL_TOP_OFFSET;
            drawSelectionEntryBackdrop(entry, drawX, drawY);

            if (isMoneyEntry(entry)) {
                drawSpriteFrame(GUI.sprMoney, drawX, drawY, 0);
                drawTextShadowed(
                        Globals.fonts.font2,
                        horizontalInset + screenRect.left + FONT2_TEXT_X_OFFSET + visibleIndex * CELL_SIZE,
                        screenRect.bottom - QUANTITY_TEXT_BOTTOM_OFFSET,
                        Utils.formatDecimalThousands(resolveHeroInventoryMoneyAmount(mainWindow)),
                        0,
                        TEXT_PALETTE_YELLOWISH,
                        1
                );
                continue;
            }

            CA16 sprite = getCachedCellSprite(visibleIndex);
            drawSpriteFrame(sprite, drawX, drawY, 0);

            int quantity = getSelectionEntryQuantity(entry);
            if (quantity > 1) {
                drawTextShadowed(
                        Globals.fonts.font2,
                        horizontalInset + screenRect.left + FONT2_TEXT_X_OFFSET + visibleIndex * CELL_SIZE,
                        screenRect.bottom - QUANTITY_TEXT_BOTTOM_OFFSET,
                        String.format(Locale.US, "%d", quantity),
                        0,
                        TEXT_PALETTE_YELLOWISH,
                        1
                );
            }

            if (hasDynamicSelectionEntryPayload(entry)) {
                drawMagicTrailDots(drawX, drawY, visibleIndex);
            }

            int playerIndex = resolveSelectionEntryOwnerPlayerIndex(mainWindow, entry);
            if (playerIndex >= 0) {
                drawTextShadowed(
                        Globals.fonts.font3,
                        horizontalInset + screenRect.left + FONT2_TEXT_X_OFFSET + visibleIndex * CELL_SIZE,
                        screenRect.top + FONT3_TEXT_Y_OFFSET,
                        String.format(Locale.US, "F%d", playerIndex + PLAYER_MARKER_BASE),
                        0,
                        TEXT_PALETTE_GRAY,
                        1
                );
            }
        }

    }

    /**
     * vtbl +0x48: HeroInventoryControlVisualObject::OnMessage @004A3CEF.
     * Fully ported for TokenEntry-backed player-slot assignment.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        int w = readMessageInt(wParam);
        int l = readMessageInt(lParam);
        if (msg == MessageCodes.ASSIGN_PLAYER_SLOT) {
            if (l == 0) {
                int ownedIndex = findFirstEntryOwnedByPlayer(w);
                if (ownedIndex >= 0) {
                    Object payload = beginUiDrag(ownedIndex, 1);
                    if (payload != null) {
                        notifySelectionInfoPanelEquipmentSlot(payload);
                    }
                }
            } else if (isMouseInsideHeroInventoryGrid()) {
                int gridIndex = getGridIndexAtScreenPoint(Globals.mousePointer.getX(), -Globals.mousePointer.getY());
                if (gridIndex >= 0) {
                    Object entry = getGridSourceEntry(gridIndex);
                    assignSelectionEntryToPlayer(w, entry);
                    clearOtherPlayerAssignmentsForEntry(w, entry);
                    refreshSelectionPlayerAssignments();
                }
            }
        }
        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * vtbl +0x4C: HeroInventoryControlVisualObject::OnMouseMove @004A3A1D.
     * Fully ported for TokenEntry-backed hero inventory entries.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        CMainWindow mainWindow = Globals.mainWindow;
        if (Globals.mousePointer.isSelecting()) {
            Globals.mousePointer.finishSelectionDrag();
        }

        if ((nFlags & 1) == 0 || gridSource == null || mainWindow.getUiLockPayload() != null) {
            return 0;
        }

        int sourceIndex = getGridIndexAtScreenPoint(x, -y);
        if (sourceIndex < 0) {
            return 0;
        }

        Object entry = getGridSourceEntry(sourceIndex);

        int detachedQuantity = resolveDraggedSelectionEntryQuantity(mainWindow, entry, nFlags);
        if (!canBeginSelectionEntryDrag(entry)) {
            return 0;
        }

        Object payload = beginUiDrag(sourceIndex, detachedQuantity);
        if (payload == null) {
            return 0;
        }

        if (isMoneyEntry(payload)) {
            decrementHeroInventoryMoney(mainWindow, ((TokenEntry) payload).quantity);
            mainWindow.beginShopGridDragVisual(
                    payload,
                    mainWindow.getUiLockSourceIndex(),
                    Resources.path(GRAPHICS, INTERFACE, MONEY, MONEY_16A),
                    mainWindow.getUiLockPackedModeCode()
            );
        } else {
            mainWindow.beginShopGridDragVisual(
                    payload,
                    mainWindow.getUiLockSourceIndex(),
                    resolveSelectionEntrySpritePath(payload),
                    mainWindow.getUiLockPackedModeCode()
            );
        }
        mainWindow.cursor.setToMousePointer();
        return 0;
    }

    /**
     * vtbl +0x50: HeroInventoryControlVisualObject::OnUserMsg @004A3CCB.
     * Fully ported.
     */
    @Override
    public int onUserMsg(int nFlags, int x, int y) {
        return onLButtonDown(nFlags, x, y);
    }

    /**
     * vtbl +0x54: HeroInventoryControlVisualObject::OnLButtonDown @004A3706.
     * Fully ported.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        int horizontalInset = getHeroInventoryHorizontalInset();

        CRect leftArrowRect = new CRect(screenRect.left, screenRect.top, screenRect.left + CONTENT_LEFT_OFFSET + horizontalInset, screenRect.bottom);
        CRect rightArrowRect = new CRect(
                screenRect.right - 0x30 - horizontalInset,
                screenRect.top,
                screenRect.right - 0x10,
                screenRect.bottom
        );
        if (leftArrowRect.contains(x, y)) {
            scrollBackwardOneEntry();
        }
        if (rightArrowRect.contains(x, y)) {
            scrollForwardOneEntry();
        }
        return 1;
    }

    /**
     * vtbl +0x58: HeroInventoryControlVisualObject::OnLButtonUp @004A399A.
     * Fully ported.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        CMainWindow mainWindow = Globals.mainWindow;
        if (gridSource != null) {
            int targetIndex = getGridIndexAtScreenPoint(x, y);
            if (mainWindow.getUiLockPayload() != null) {
                CMousePointer.Cursor_Default.setToMousePointer();
                completeUiDrag(targetIndex);
            }
        }
        return 1;
    }

    /**
     * vtbl +0x5C: HeroInventoryControlVisualObject::OnLButtonDblClk @004A37F3.
     * Fully ported for TokenEntry-backed hero inventory entries.
     */
    @Override
    public int onLButtonDblClk(int nFlags, int x, int y) {
        if (gridSource == null) {
            return 1;
        }

        int sourceIndex = getGridIndexAtScreenPoint(x, -y);
        if (sourceIndex < 0) {
            return 1;
        }

        CMainWindow mainWindow = Globals.mainWindow;
        Object entry = getGridSourceEntry(sourceIndex);

        if (!isMoneyEntry(entry)) {
            if (!isSelectionEntryBlockedForDoubleClick(entry)) {
                Object payload = beginUiDrag(sourceIndex, 1);
                if (payload != null) {
                    notifySelectionInfoPanelEquipmentSlot(payload);
                }
            }
            return 1;
        }

        if (resolveHeroInventoryMoneyAmount(mainWindow) > 0) {
            showDropGoldPromptForSelectionEntry(sourceIndex);
        }
        return 1;
    }

    /**
     * vtbl +0x60: HeroInventoryControlVisualObject::OnRButtonDown @004A37E1.
     * Fully ported.
     */
    @Override
    public int onRButtonDown(int nFlags, int x, int y) {
        return 1;
    }

    /**
     * vtbl +0x64: HeroInventoryControlVisualObject::OnRButtonUp @004A3A0B.
     * Fully ported.
     */
    @Override
    public int onRButtonUp(int nFlags, int x, int y) {
        return 1;
    }

    /**
     * vtbl +0x68: HeroInventoryControlVisualObject::OnRButtonDblClk @004A3988.
     * Fully ported.
     */
    @Override
    public int onRButtonDblClk(int nFlags, int x, int y) {
        return 1;
    }

    /**
     * Java-only mouse-wheel extension for player's inventory scrolling.
     * not ported.
     */
    @Override
    public int onMouseWheel(int nFlagsAndDelta, int x, int y) {
        if (!isHeroInventoryPoint(x, y)) {
            return 0;
        }

        int wheelDelta = (short) ((nFlagsAndDelta >>> 16) & 0xFFFF);
        if (wheelDelta > 0) {
            if (canScrollBackwardOneEntry()) {
                scrollBackwardOneEntry();
                draw();
            }
            return 1;
        }
        if (wheelDelta < 0) {
            if (canScrollForwardOneEntry()) {
                scrollForwardOneEntry();
                draw();
            }
            return 1;
        }
        return 0;
    }

    /**
     * vtbl +0x78: HeroInventoryControlVisualObject::MergeOrInsertEntryAt @004A42A0.
     * Fully ported for TokenEntry-backed hero inventory entries.
     */
    @Override
    public int mergeOrInsertEntryAt(Object entry, int insertIndex) {
        setSelectionEntryGridMode(entry, 2);
        return super.mergeOrInsertEntryAt(entry, insertIndex);
    }

    /**
     * vtbl +0x7C: HeroInventoryControlVisualObject::MergeOrAppendEntry @004A4270.
     * Fully ported for TokenEntry-backed hero inventory entries.
     */
    @Override
    public int mergeOrAppendEntry(Object entry) {
        setSelectionEntryGridMode(entry, 2);
        return super.mergeOrAppendEntry(entry);
    }

    /**
     * vtbl +0x88: HeroInventoryControlVisualObject::GetGridIndexAtScreenPoint @004A35C0.
     * Fully ported.
     */
    @Override
    public int getGridIndexAtScreenPoint(int x, int y) {
        if (gridSource == null) {
            return -1;
        }

        int horizontalInset = getHeroInventoryHorizontalInset();
        int visibleStart = getVisibleStartValue();
        int visibleCellCount = getVisibleCellCount();
        int sourceSize = getGridSourceSize();
        if (y < 0) {
            if (x < horizontalInset + CONTENT_LEFT_OFFSET) {
                return -1;
            }
            if (x >= horizontalInset + CONTENT_LEFT_OFFSET + visibleColumns * CELL_SIZE) {
                return -1;
            }
        } else {
            if (x < horizontalInset + CONTENT_LEFT_OFFSET) {
                return visibleStart;
            }
            if (x >= horizontalInset + CONTENT_LEFT_OFFSET + visibleColumns * CELL_SIZE
                    && visibleStart - 1 + visibleCellCount < sourceSize) {
                return visibleStart - 1 + visibleCellCount;
            }
        }

        int gridIndex = visibleStart + ((x - CONTENT_LEFT_OFFSET) - horizontalInset) / CELL_SIZE;
        return gridIndex >= sourceSize ? -1 : gridIndex;
    }

    /**
     * vtbl +0xA8: HeroInventoryControlVisualObject::GetGridModeCode @004A42D0.
     * Fully ported.
     */
    @Override
    public int getGridModeCode() {
        return 2;
    }

    /**
     * Native draw branch inside HeroInventoryControlVisualObject::Update @004A2CF8.
     * Fully ported.
     */
    private static void drawHeroInventoryFrame(CRect screenRect, int horizontalInset) {
        int frameHeight = GUI.invFrame.surface.height();
        int frameWidth = GUI.invFrame.surface.width();
        GUI.invFrame.drawRectMasked(screenRect.left + horizontalInset, screenRect.top, 0, 0, 0x110, frameHeight);

        int centerSegmentCount = Math.max(0, ((screenWidth() - 0x280) / CELL_SIZE));
        for (int segment = 0; segment < centerSegmentCount; segment++) {
            GUI.invFrame.drawRectMasked(
                    screenRect.left + horizontalInset + 0x110 + segment * CELL_SIZE,
                    screenRect.top,
                    0xC0,
                    0,
                    0x110,
                    frameHeight
            );
        }

        if (horizontalInset != 0 && screenHeight() > 600) {
            GUI.invLeft1024.draw(screenRect.left, screenRect.top, 0, 0, false);
            GUI.invRight1024.drawRectMasked(
                    screenRect.right - horizontalInset - 0x10,
                    screenRect.top,
                    0,
                    0,
                    GUI.invRight1024.surface.width(),
                    GUI.invRight1024.surface.height()
            );
        }

        GUI.invFrame.drawRectMasked(
                screenRect.right - horizontalInset - frameWidth + 0x110,
                screenRect.top,
                0x110,
                0,
                frameWidth,
                frameHeight
        );
        if (Globals.mainWindow.pMapVisualObject.hasSpellPanelChild()) {
            drawHeroInventoryTopSeam(screenRect, horizontalInset, frameWidth, centerSegmentCount);
        }
    }

    /**
     * not ported. Java native-resolution extension covers the transparent top rows in invframe.bmp where it touches the
     * spellbook panel.
     */
    private static void drawHeroInventoryTopSeam(CRect screenRect, int horizontalInset, int frameWidth,
                                                 int centerSegmentCount) {
        drawHeroInventoryTopSeamSegment(screenRect.left + horizontalInset, screenRect.top, 0, 0x110);
        for (int segment = 0; segment < centerSegmentCount; segment++) {
            drawHeroInventoryTopSeamSegment(
                    screenRect.left + horizontalInset + 0x110 + segment * CELL_SIZE,
                    screenRect.top,
                    0xC0,
                    0x110
            );
        }
        drawHeroInventoryTopSeamSegment(
                screenRect.right - horizontalInset - frameWidth + 0x110,
                screenRect.top,
                0x110,
                frameWidth
        );
    }

    /**
     * not ported. Java helper for the inventory/spellbook seam overlay.
     */
    private static void drawHeroInventoryTopSeamSegment(int x, int y, int srcLeft, int srcRight) {
        GUI.invFrame.drawRectMasked(
                x,
                y,
                srcLeft,
                INVENTORY_TOP_SEAM_SOURCE_TOP,
                srcRight,
                INVENTORY_TOP_SEAM_SOURCE_TOP + INVENTORY_TOP_SEAM_HEIGHT
        );
    }

    /**
     * Native helper reused across HeroInventoryControlVisualObject own methods for the `_g_screenRect.RightBottom.x` inset calculation.
     * not ported.
     */
    private static int getHeroInventoryHorizontalInset() {
        return ((Globals.screenRect.right - 0xF0) % CELL_SIZE) / 2;
    }

    /**
     * Native owner: token-entry CArray `GetSize` reads used across HeroInventoryControlVisualObject own methods.
     * not ported.
     */
    private int getGridSourceSize() {
        if (gridSource instanceof List<?> list) {
            return list.size();
        }
        if (gridSource instanceof Object[] array) {
            return array.length;
        }
        return 0;
    }

    /**
     * Native owner: token-entry CArray `GetAt` and `operator[]` reads used across HeroInventoryControlVisualObject own methods.
     * not ported.
     */
    private Object getGridSourceEntry(int index) {
        if (index < 0) {
            return null;
        }
        if (gridSource instanceof List<?> list) {
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
     * Native owner: `int *` visible-start pointer writes at GridOverlayVisualObject +0x90.
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
     * Native owner: visible cell count at HeroInventoryControlVisualObject +0x88/+0x8C.
     * not ported.
     */
    private int getVisibleCellCount() {
        return Math.max(0, visibleColumns * visibleRows);
    }

    /**
     * Native owner: `CDWordArray::GetAt` reads from HeroInventoryControlVisualObject +0x70.
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
     * Native owner: `CDWordArray::SetAt` writes to HeroInventoryControlVisualObject +0x70.
     * not ported.
     */
    private void setCellUpdateCounter(int index, int value) {
        if (index < 0 || index >= cellUpdateCounters.size()) {
            return;
        }
        cellUpdateCounters.set(index, value);
    }

    /**
     * Native owner: `CArray<>::GetAt` reads from HeroInventoryControlVisualObject +0x5C.
     * not ported.
     */
    private CA16 getCachedCellSprite(int index) {
        if (index < 0 || index >= visibleCellSprites.size()) {
            return null;
        }
        Object sprite = visibleCellSprites.get(index);
        return sprite instanceof CA16 ca16 ? ca16 : null;
    }

    /**
     * Native owner: `FUN_0041DDEA` player-slot scan in HeroInventoryControlVisualObject::OnMessage @004A3CEF.
     * Full port for TokenEntry-backed player slots.
     */
    private int findFirstEntryOwnedByPlayer(int playerIndex) {
        for (int index = 0; index < getGridSourceSize(); index++) {
            Object entry = getGridSourceEntry(index);
            if (isSelectionEntryOwnedByPlayer(playerIndex, entry)) {
                return index;
            }
        }
        return -1;
    }

    /**
     * Native owner: `FUN_0041DDEA` player-slot ownership probe in HeroInventoryControlVisualObject::OnMessage @004A3CEF and Update @004A2CF8.
     * Full port for TokenEntry-backed player slots.
     */
    private static boolean isSelectionEntryOwnedByPlayer(int playerIndex, Object entry) {
        return Globals.mainWindow.m_GameSession.m_PlayerSlots[playerIndex].matchesTokenEntry((TokenEntry) entry);
    }

    /**
     * Java-only rarity-color inventory backdrop extension.
     * not ported.
     */
    private static void drawSelectionEntryBackdrop(Object entry, int drawX, int drawY) {
        if (!(entry instanceof TokenEntry tokenEntry) || tokenEntry.isMoneyEntry()) {
            GUI.backInv.draw(drawX, drawY, 0, 0, false);
            return;
        }

        ShapeId shape = ShapeId.fromId(tokenEntry.getShape());
        if (!shape.isBetween(ShapeId.UNCOMMON, ShapeId.GOOD)) {
            GUI.backInv.draw(drawX, drawY, 0, 0, false);
            return;
        }

        resolveTintedInventoryBackdrop(shape).draw(drawX, drawY, 0, null, false);
    }

    /**
     * Java-only cache lookup for shape-tinted inventory backdrops.
     * not ported.
     */
    private static CBmp64k resolveTintedInventoryBackdrop(ShapeId shape) {
        if (tintedInventoryBackdropSource != GUI.backInv) {
            tintedInventoryBackdropSource = GUI.backInv;
            tintedInventoryBackdrops.clear();
        }
        return tintedInventoryBackdrops.computeIfAbsent(shape, HeroInventoryControlVisualObject::createTintedInventoryBackdrop);
    }

    /**
     * Java-only rarity-color inventory backdrop generator.
     * not ported.
     */
    private static CBmp64k createTintedInventoryBackdrop(ShapeId shape) {
        CBmp64k result = new CBmp64k(GUI.backInv.surface.width(), GUI.backInv.surface.height());
        RGB16[] sourcePixels = GUI.backInv.surface.pixels();
        RGB16[] resultPixels = result.surface.pixels();
        for (int pixelIndex = 0; pixelIndex < sourcePixels.length; pixelIndex++) {
            resultPixels[pixelIndex] = tintInventoryBackdropPixel(sourcePixels[pixelIndex], shape);
        }
        result.syncFrameBytesFromSurface();
        return result;
    }

    /**
     * Java-only luminance-weighted inventory backdrop colorizer.
     * not ported.
     */
    private static RGB16 tintInventoryBackdropPixel(RGB16 source, ShapeId shape) {
        int luminance = inventoryBackdropLuminance(source);
        int colorWeight = normalizedInventoryBackdropHighlight(luminance);
        return RGB16.from(
                blendInventoryBackdropChannel(source.r(), shape.color.r(), colorWeight),
                blendInventoryBackdropChannel(source.g(), shape.color.g(), colorWeight),
                blendInventoryBackdropChannel(source.b(), shape.color.b(), colorWeight)
        );
    }

    /**
     * Java-only highlight normalization for the dark source inventory backdrop.
     * not ported.
     */
    private static int normalizedInventoryBackdropHighlight(int luminance) {
        if (luminance <= INVENTORY_BACKDROP_DARK_LUMINANCE) {
            return 0;
        }
        if (luminance >= INVENTORY_BACKDROP_LIGHT_LUMINANCE) {
            return RGB_WEIGHT_SCALE;
        }

        int linearHighlight = (luminance - INVENTORY_BACKDROP_DARK_LUMINANCE) * RGB_WEIGHT_SCALE
                / (INVENTORY_BACKDROP_LIGHT_LUMINANCE - INVENTORY_BACKDROP_DARK_LUMINANCE);
        int squaredHighlight = linearHighlight * linearHighlight / RGB_WEIGHT_SCALE;
        int smootherHighlight = smoothInventoryBackdropHighlight(linearHighlight);
        int totalCurveWeight = INVENTORY_BACKDROP_SQUARED_CURVE_WEIGHT + INVENTORY_BACKDROP_SMOOTHERSTEP_CURVE_WEIGHT;
        return (squaredHighlight * INVENTORY_BACKDROP_SQUARED_CURVE_WEIGHT
                + smootherHighlight * INVENTORY_BACKDROP_SMOOTHERSTEP_CURVE_WEIGHT)
                / totalCurveWeight;
    }

    /**
     * Java-only smootherstep curve for inventory backdrop tint falloff.
     * not ported.
     */
    private static int smoothInventoryBackdropHighlight(int linearHighlight) {
        int squared = linearHighlight * linearHighlight / RGB_WEIGHT_SCALE;
        int cubed = squared * linearHighlight / RGB_WEIGHT_SCALE;
        int polynomial = 10 * RGB_WEIGHT_SCALE - 15 * linearHighlight + 6 * squared;
        return cubed * polynomial / RGB_WEIGHT_SCALE;
    }

    /**
     * Java-only RGB luminance helper for inventory backdrop colorization.
     * not ported.
     */
    private static int inventoryBackdropLuminance(RGB16 color) {
        return (color.r() * RED_LUMINANCE_WEIGHT
                + color.g() * GREEN_LUMINANCE_WEIGHT
                + color.b() * BLUE_LUMINANCE_WEIGHT) >> 8;
    }

    /**
     * Java-only RGB channel blend helper for inventory backdrop colorization.
     * not ported.
     */
    private static int blendInventoryBackdropChannel(int source, int target, int weight) {
        return (source * (RGB_WEIGHT_SCALE - weight) + target * weight + RGB_WEIGHT_SCALE / 2) / RGB_WEIGHT_SCALE;
    }

    /**
     * Native owner: `FUN_0041DD7E` player-slot assignment in HeroInventoryControlVisualObject::OnMessage @004A3CEF.
     * Full port for TokenEntry-backed player slots.
     */
    private static void assignSelectionEntryToPlayer(int playerIndex, Object entry) {
        Globals.mainWindow.m_GameSession.m_PlayerSlots[playerIndex].assignFromTokenEntry((TokenEntry) entry);
    }

    /**
     * Native owner: duplicate-assignment cleanup loop in HeroInventoryControlVisualObject::OnMessage @004A3CEF.
     * Full port for TokenEntry-backed player slots.
     */
    private static void clearOtherPlayerAssignmentsForEntry(int playerIndex, Object entry) {
        for (int otherPlayerIndex = 0; otherPlayerIndex < MAX_PLAYER_SLOTS; otherPlayerIndex++) {
            if (otherPlayerIndex != playerIndex
                    && isSelectionEntryOwnedByPlayer(otherPlayerIndex, entry)) {
                Globals.mainWindow.m_GameSession.m_PlayerSlots[otherPlayerIndex].type = 0;
            }
        }
    }

    /**
     * Native owner: CGameSession::refreshSavedPlayerSlots @004920EE game-session refresh in
     * HeroInventoryControlVisualObject::OnMessage @004A3CEF.
     * Full port.
     */
    private static void refreshSelectionPlayerAssignments() {
        Globals.mainWindow.m_GameSession.refreshSavedPlayerSlots();
    }

    /**
     * Native support extracted from `Token::GetType(payload) - 1` and the SelectionInfoPanelVisualObject vtable slot
     * call in HeroInventoryControlVisualObject::OnMessage @004A3CEF and OnLButtonDblClk @004A37F3.
     */
    private static void notifySelectionInfoPanelEquipmentSlot(Object payload) {
        CMainWindow mainWindow = Globals.mainWindow;
        int slotIndex = resolveSelectionInfoEquipmentSlot(payload);
        SelectionInfoPanelVisualObject selectionInfoPanelVisualObject =
                mainWindow.pSelectionInfoPanelVisualObject;
        selectionInfoPanelVisualObject.applyCarriedTokenToSelectionSlot(slotIndex);
    }

    /**
     * Native support extracted from `Token::GetType(payload) - 1` in HeroInventoryControlVisualObject::OnMessage
     *
     * @004A3CEF and OnLButtonDblClk @004A37F3.
     */
    private static int resolveSelectionInfoEquipmentSlot(Object payload) {
        if (payload instanceof TokenEntry tokenEntry) {
            return tokenEntry.getType() - 1;
        }
        return ((Token) payload).getMovementType() - 1;
    }

    /**
     * Native helper branch in HeroInventoryControlVisualObject::OnMessage @004A3CEF.
     * not ported.
     */
    private boolean isMouseInsideHeroInventoryGrid() {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        return screenRect.contains(Globals.mousePointer.getX(), Globals.mousePointer.getY());
    }

    /**
     * Java-only mouse-wheel hit test for the player's inventory control.
     * not ported.
     */
    private boolean isHeroInventoryPoint(int screenX, int screenY) {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        return screenRect.contains(screenX, screenY);
    }

    /**
     * Native branch helper in HeroInventoryControlVisualObject::OnMouseMove @004A3A1D.
     */
    private int resolveDraggedSelectionEntryQuantity(CMainWindow mainWindow, Object entry, int nFlags) {
        if (!isMoneyEntry(entry)) {
            return isShiftPressed(nFlags) ? getSelectionEntryQuantity(entry) : 1;
        }

        int currentMoney = resolveHeroInventoryMoneyAmount(mainWindow);
        if (currentMoney < 1) {
            return 0;
        }
        return isShiftPressed(nFlags) ? getSelectionEntryQuantity(entry) : SHIFT_MONEY_DRAG_AMOUNT;
    }

    /**
     * Native support extracted from `keySHIFT` reads in HeroInventoryControlVisualObject::OnMouseMove @004A3A1D.
     */
    private static boolean isShiftPressed(@SuppressWarnings("unused") int nFlags) {
        return Globals.shiftKeyDown;
    }

    /**
     * Native support extracted from MagicItem::IsShopCatalogEntryDragBlocked @00439C73 in HeroInventoryControlVisualObject::OnMouseMove @004A3A1D.
     */
    private static boolean canBeginSelectionEntryDrag(Object entry) {
        return !MagicItem.isShopCatalogEntryDragBlocked((TokenEntry) entry, Globals.mainWindow.sessionMode);
    }

    /**
     * Native support extracted from MagicItem::IsShopCatalogEntryBlockedForDoubleClick @00439D63.
     */
    private static boolean isSelectionEntryBlockedForDoubleClick(Object entry) {
        return MagicItem.isShopCatalogEntryBlockedForDoubleClick((TokenEntry) entry);
    }

    /**
     * Native owner: drop-gold prompt branch in HeroInventoryControlVisualObject::OnLButtonDblClk @004A37F3.
     * Partial port. Prompt child insertion, source-index setup, modal flag activation, redraw, and selection-drag reset are ported,
     * but prompt ownership still depends on unresolved main-window bindings.
     */
    private static void showDropGoldPromptForSelectionEntry(int sourceIndex) {
        CMainWindow mainWindow = Globals.mainWindow;
        CVisualObject inputController = mainWindow.getInputController();
        DropGoldPromptVisualObject dropGoldPromptVisualObject =
                (DropGoldPromptVisualObject) mainWindow.getDropGoldPromptVisual();

        mainWindow.dialogsMask = MODAL_DIALOG.includeTo(mainWindow.dialogsMask);
        inputController.addChild(dropGoldPromptVisualObject);
        dropGoldPromptVisualObject.setSelectionEntryIndex(sourceIndex);
        dropGoldPromptVisualObject.showDialog();
        inputController.draw();
        CMousePointer.Cursor_Default.setToMousePointer();
        if (Globals.mousePointer.isSelecting()) {
            Globals.mousePointer.finishSelectionDrag();
        }
        mainWindow.clearHeroInventoryDropGoldPromptState();
    }

    /**
     * Native support extracted from currentPlayer->gold reads in HeroInventoryControlVisualObject::Update @004A2CF8, OnMouseMove @004A3A1D, and OnLButtonDblClk @004A37F3.
     */
    private static int resolveHeroInventoryMoneyAmount(CMainWindow mainWindow) {
        return mainWindow.getHeroInventoryMoneyAmount();
    }

    /**
     * Native support extracted from HeroInventoryControlVisualObject::OnMouseMove @004A3A1D money payload branch.
     */
    private static void decrementHeroInventoryMoney(CMainWindow mainWindow, int amount) {
        mainWindow.setHeroInventoryMoneyAmount(mainWindow.getHeroInventoryMoneyAmount() - amount);
    }

    /**
     * Native owner: carried-entry exclusion branch shared with GridOverlayVisualObject animated-cell paths.
     * not ported.
     */
    private static boolean isHeldSingleQuantityEntry(CMainWindow mainWindow, Object entry) {
        return mainWindow.getUiLockPayload() == entry
                && getSelectionEntryQuantity(entry) == 1;
    }

    /**
     * Native support extracted from Token::GetEquipmentPortraitResourceName @00438BA1 sprite-name lookup in HeroInventoryControlVisualObject::Update @004A2CF8.
     */
    private static String resolveSelectionEntrySpriteName(Object entry) {
        return entry instanceof TokenEntry tokenEntry ? tokenEntry.getEquipmentPortraitResourceName() : null;
    }

    /**
     * Native owner: item tooltip helper TokenEntry::resolveTooltipText in HeroInventoryControlVisualObject::GetText @004A3F0B.
     * Partial port.
     */
    private static String resolveSelectionEntryTooltip(Object entry) {
        return entry instanceof TokenEntry tokenEntry ? tokenEntry.resolveTooltipText() : null;
    }

    /**
     * Java-only ALT tooltip extension for comparing hovered inventory items with the selected unit's equipped slot.
     * not ported.
     */
    private static String appendAltEquippedSlotTooltip(Object entry, String tooltip) {
        if (!Globals.altKeyDown || !(entry instanceof TokenEntry tokenEntry) || tooltip == null) {
            return tooltip;
        }

        TokenEntry equippedToken = resolveSelectedUnitEquipmentToken(tokenEntry);
        if (equippedToken == null) {
            return tooltip;
        }
        return TooltipText.sideBySide(tooltip, equippedToken.resolveTooltipText());
    }

    /**
     * Java-only helper mapping a highlighted token's resolved Item slot onto the selected unit equipment snapshot.
     * not ported.
     */
    private static TokenEntry resolveSelectedUnitEquipmentToken(TokenEntry tokenEntry) {
        Item highlightedItem = resolveTokenEntryItem(tokenEntry);
        if (highlightedItem == null) {
            return null;
        }

        CUnit selectedUnit = resolveCurrentSelectedUnit();
        if (selectedUnit == null) {
            return null;
        }

        int slotIndex = highlightedItem.getSlot() - 1;
        if (slotIndex < 0 || slotIndex >= selectedUnit.equipmentTokenEntries.length) {
            return null;
        }
        return selectedUnit.equipmentTokenEntries[slotIndex];
    }

    /**
     * Java-only helper resolving a highlighted TokenEntry through the same static item factory used by item tooltips.
     * not ported.
     */
    private static Item resolveTokenEntryItem(TokenEntry tokenEntry) {
        return Globals.staticDataMgr.createItemFromPackedHash(tokenEntry.packedTokenHash & 0xFFFF);
    }

    /**
     * Java-only helper mirroring selection-panel gating so ALT comparison only uses a single selected CUnit.
     * not ported.
     */
    private static CUnit resolveCurrentSelectedUnit() {
        MapVisualObject mapVisualObject = Globals.mainWindow.pMapVisualObject;
        if (mapVisualObject == null || mapVisualObject.getSelectedCount() != 1) {
            return null;
        }
        Object selectedObject = mapVisualObject.getPrimarySelectedObject();
        return selectedObject instanceof CUnit selectedUnit ? selectedUnit : null;
    }

    /**
     * Native support: TokenEntry::isMoneyEntry @0041EA40 used throughout HeroInventoryControlVisualObject own methods.
     */
    private static boolean isMoneyEntry(Object entry) {
        return ((TokenEntry) entry).isMoneyEntry();
    }

    /**
     * Native support extracted from entry quantity reads at `entry +0x10` in HeroInventoryControlVisualObject::Update @004A2CF8 and OnMouseMove @004A3A1D.
     */
    private static int getSelectionEntryQuantity(Object entry) {
        if (entry instanceof TokenEntry tokenEntry) {
            return tokenEntry.quantity;
        }
        return 1;
    }

    /**
     * Native support extracted from dynamic payload flag check in HeroInventoryControlVisualObject::Update @004A2CF8.
     */
    private static boolean hasDynamicSelectionEntryPayload(Object entry) {
        return entry instanceof TokenEntry tokenEntry
                && (tokenEntry.wireFlags & DYNAMIC_PAYLOAD_FLAG) != 0;
    }

    /**
     * Native owner: player-marker loop with FUN_0041DDEA in HeroInventoryControlVisualObject::Update @004A2CF8.
     * Full port for TokenEntry-backed player slots.
     */
    private static int resolveSelectionEntryOwnerPlayerIndex(CMainWindow mainWindow, Object entry) {
        for (int playerIndex = 0; playerIndex < MAX_PLAYER_SLOTS; playerIndex++) {
            if (isSelectionEntryOwnedByPlayer(playerIndex, entry)) {
                return playerIndex;
            }
        }
        return -1;
    }

    /**
     * Native support extracted from subclass-specific `entry + 0x18 = 2` writes in
     * HeroInventoryControlVisualObject::MergeOrInsertEntryAt @004A42A0 and MergeOrAppendEntry @004A4270.
     */
    private static void setSelectionEntryGridMode(Object entry, int gridModeCode) {
        if (entry instanceof TokenEntry tokenEntry) {
            tokenEntry.gridModeCode = gridModeCode;
        }
    }

    /**
     * Native support extracted from HeroInventoryControlVisualObject::OnMouseMove @004A3A1D item cursor path construction.
     */
    private static String resolveSelectionEntrySpritePath(Object entry) {
        String spriteName = resolveSelectionEntrySpriteName(entry);
        if (spriteName == null || spriteName.isEmpty()) {
            return null;
        }
        return Resources.path(GRAPHICS, INVENTORY_DIRECTORY, spriteName + A16_SUFFIX);
    }

    /**
     * Native support extracted from CGameBitmap::Draw vtable calls in HeroInventoryControlVisualObject::Update @004A2CF8.
     */
    private static void drawSpriteFrame(CA16 sprite, int x, int y, int frameIndex) {
        sprite.draw(x, y, frameIndex, 0, false);
    }

    /**
     * Native support extracted from CBitmapFont::DrawTextShadowed call sites in HeroInventoryControlVisualObject::Update @004A2CF8.
     */
    private static void drawTextShadowed(
            CBitmapFont bitmapFont,
            int x,
            int y,
            String text,
            int textAlignFlags,
            int paletteId,
            int shadowOffset
    ) {
        bitmapFont.drawTextShadowed(
                x,
                y,
                text,
                textAlignFlags,
                paletteId == TEXT_PALETTE_GRAY ? Palettes.gray : Palettes.yellowish,
                shadowOffset
        );
    }

    /**
     * Native helper: `_g_screenRect.RightBottom.x` used by HeroInventoryControlVisualObject::Update @004A2CF8.
     * not ported.
     */
    private static int screenWidth() {
        return Globals.screenRect.right;
    }

    /**
     * Native helper: `_g_screenRect.RightBottom.y` used by HeroInventoryControlVisualObject::Update @004A2CF8.
     * not ported.
     */
    private static int screenHeight() {
        return Globals.screenRect.bottom;
    }
}
