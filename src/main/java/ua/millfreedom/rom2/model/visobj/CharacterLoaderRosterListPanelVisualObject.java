package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CMousePointer;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;

/**
 * Native class: CharacterLoaderRosterListPanelVisualObject (vtbl @0x005CB820).
 * Purpose: character-loader roster list panel used as child `id=0x463` under dialog `0x460`.
 */
public class CharacterLoaderRosterListPanelVisualObject extends CVisualObject {
    public static final int NATIVE_SIZE = 0xDC; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final String LOADER_CENTERAREA_BMP = "graphics/interface/chrgen/loader/centerarea.bmp";
    private static final String LOADER_UP_SHINE_BMP = "graphics/interface/chrgen/loader/up/shine.bmp";
    private static final String LOADER_UP_SHINE_ON_BMP = "graphics/interface/chrgen/loader/up/shine_on.bmp";
    private static final String LOADER_DOWN_SHINE_BMP = "graphics/interface/chrgen/loader/down/shine.bmp";
    private static final String LOADER_DOWN_SHINE_ON_BMP = "graphics/interface/chrgen/loader/down/shine_on.bmp";

    //0x5c
    public CharacterLoaderDialogVisualObject ownerDialog;

    //0x60
    public final CRect entryRowTemplateRect = new CRect();

    //0x70
    public final CRect entryListViewportRect = new CRect();

    //0x80
    public final CRect scrollUpButtonRect = new CRect();

    //0x90
    public final CRect scrollDownButtonRect = new CRect();

    //0xa0
    public int firstVisibleEntryIndex;

    //0xa4
    public CBmp64k backgroundGraphic;

    //0xa8
    public CBmp64k scrollUpHoverGraphic;

    //0xac
    public CBmp64k scrollUpPressedGraphic;

    //0xb0
    public CBmp64k scrollDownHoverGraphic;

    //0xb4
    public CBmp64k scrollDownPressedGraphic;

    //0xb8
    public CBmp64k currentScrollUpGraphic;

    //0xbc
    public CBmp64k currentScrollDownGraphic;

    //0xd0
    public int selectedEntryIndex;

    //0xd4
    public int hoveredEntryIndex;

    //0xd8
    public int renameModeActiveFlag;

    /**
     * Native: CharacterLoaderRosterListPanelVisualObject::CharacterLoaderRosterListPanelVisualObject @00430274.
     * Fully ported.
     */
    public CharacterLoaderRosterListPanelVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            CharacterLoaderDialogVisualObject ownerDialog
    ) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        this.ownerDialog = ownerDialog;
        initializeRosterListPanel();
    }

    /**
     * Native: CharacterLoaderRosterListPanelVisualObject::InitializeRosterListPanel @0043036C.
     * Fully ported.
     */
    private void initializeRosterListPanel() {
        int fontHeight = estimateFont4Height();
        entryRowTemplateRect.set(0x38, 0x38, 0x38 + 0xCE, 0x38 + fontHeight);
        entryListViewportRect.set(cRect.left + 0x38, cRect.top + 0x38, cRect.left + 0x38 + 0xCE, cRect.top + 0x38 + 0xEC);

        firstVisibleEntryIndex = 0;
        backgroundGraphic = null;
        scrollUpHoverGraphic = null;
        scrollUpPressedGraphic = null;
        scrollDownHoverGraphic = null;
        scrollDownPressedGraphic = null;
        currentScrollUpGraphic = null;
        currentScrollDownGraphic = null;

        scrollUpButtonRect.set(cRect.left + 0x78, cRect.top, cRect.left + 0x78 + 0x50, cRect.top + 0x3C);
        scrollDownButtonRect.set(cRect.left + 0x78, cRect.top + 0x188, cRect.left + 0x78 + 0x50, cRect.top + 0x188 + 0x34);
        resetRosterListState();
    }

    /**
     * Native: CharacterLoaderRosterListPanelVisualObject::ResetRosterListState @00430666.
     * Fully ported.
     */
    void resetRosterListState() {
        firstVisibleEntryIndex = 0;
        selectedEntryIndex = 0;
        hoveredEntryIndex = -1;
        renameModeActiveFlag = 0;
    }

    /**
     * vtbl +0x2C: CharacterLoaderRosterListPanelVisualObject::Update @004306A5.
     * Fully ported.
     */
    @Override
    public void update() {
        if (ownerDialog.dialogActiveFlag == 0) {
            return;
        }

        int visibleLimit = firstVisibleEntryIndex + (entryListViewportRect.height() / entryRowTemplateRect.height());
        int entryCount = getEntryCount();
        if (entryCount < visibleLimit) {
            visibleLimit = entryCount;
        }

        int ownerLeft = ownerDialog.cRect.left;
        int ownerTop = ownerDialog.cRect.top;
        Globals.renderer.lockSurface();
        try {
            drawGraphic(backgroundGraphic, ownerLeft + cRect.left, ownerTop + cRect.top);
            if (currentScrollUpGraphic != null) {
                drawGraphic(currentScrollUpGraphic, ownerLeft + scrollUpButtonRect.left, ownerTop + scrollUpButtonRect.top);
            }
            if (currentScrollDownGraphic != null) {
                drawGraphic(currentScrollDownGraphic, ownerLeft + scrollDownButtonRect.left, ownerTop + scrollDownButtonRect.top);
            }
            for (int entryIndex = firstVisibleEntryIndex; entryIndex < visibleLimit; entryIndex++) {
                boolean isHovered = entryIndex == hoveredEntryIndex;
                boolean isSelected = entryIndex == selectedEntryIndex;
                if (isSelected) {
                    drawSelectedEntryUnderline(ownerLeft, ownerTop, entryIndex);
                }
                if (!isSelected || renameModeActiveFlag == 0) {
                    drawEntryLabel(ownerLeft, ownerTop, entryIndex, isHovered);
                }
            }
        } finally {
            Globals.renderer.unlockSurface();
        }
        super.update();
    }

    /**
     * vtbl +0x4C: CharacterLoaderRosterListPanelVisualObject::OnMouseMove @00430CC2.
     * Fully ported.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        if (renameModeActiveFlag == 0) {
            hoveredEntryIndex = getEntryIndexAtScreenPoint(x, y);
            updateCursorForHoveredEntry();
            refreshScrollButtonGraphics(x, y, (nFlags & 1) != 0);
        }
        return super.onMouseMove(nFlags, x, y);
    }

    /**
     * vtbl +0x5C: CharacterLoaderRosterListPanelVisualObject::OnLButtonDblClk @00430D59.
     * Fully ported.
     */
    @Override
    public int onLButtonDblClk(int nFlags, int x, int y) {
        return onLButtonDown(nFlags, x, y);
    }

    /**
     * vtbl +0x54: CharacterLoaderRosterListPanelVisualObject::OnLButtonDown @00430E1D.
     * Fully ported.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        if (renameModeActiveFlag != 0) {
            return super.onLButtonDown(nFlags, x, y);
        }

        int entryIndex = getEntryIndexAtScreenPoint(x, y);
        selectEntry(entryIndex);
        if (scrollDownButtonRect.contains(x, y)) {
            scrollDown();
        } else if (scrollUpButtonRect.contains(x, y)) {
            scrollUp();
        }
        refreshScrollButtonGraphics(x, y, true);
        return 1;
    }

    /**
     * vtbl +0x58: CharacterLoaderRosterListPanelVisualObject::OnLButtonUp @00430EE5.
     * Fully ported.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        if (renameModeActiveFlag != 0) {
            return super.onLButtonUp(nFlags, x, y);
        }

        refreshScrollButtonGraphics(x, y, false);
        return 1;
    }

    /**
     * vtbl +0x6C: CharacterLoaderRosterListPanelVisualObject::OnKeyDown @0043124E.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        return switch (nChar) {
            case 0x0D -> {
                if (renameModeActiveFlag == 0) {
                    ownerDialog.acceptRosterSelection();
                } else {
                    ownerDialog.commitRosterEntryRename();
                }
                yield 1;
            }
            case 0x1B -> {
                if (renameModeActiveFlag != 0) {
                    ownerDialog.cancelRosterEntryRename();
                }
                yield 0;
            }
            case 0x26 -> scrollUp();
            case 0x28 -> scrollDown();
            default -> 0;
        };
    }

    /**
     * Native: CharacterLoaderRosterListPanelVisualObject::LoadRosterListGraphics @00430970.
     * Fully ported.
     */
    void loadRosterListGraphics() {
        releaseRosterListGraphics();
        backgroundGraphic = loadBmp64k(LOADER_CENTERAREA_BMP);
        scrollUpHoverGraphic = loadBmp64k(LOADER_UP_SHINE_BMP);
        scrollUpPressedGraphic = loadBmp64k(LOADER_UP_SHINE_ON_BMP);
        scrollDownHoverGraphic = loadBmp64k(LOADER_DOWN_SHINE_BMP);
        scrollDownPressedGraphic = loadBmp64k(LOADER_DOWN_SHINE_ON_BMP);
    }

    /**
     * Native: CharacterLoaderRosterListPanelVisualObject::ReleaseRosterListGraphics @00430B43.
     * Fully ported.
     */
    void releaseRosterListGraphics() {
        backgroundGraphic = releaseGraphic(backgroundGraphic);
        scrollUpHoverGraphic = releaseGraphic(scrollUpHoverGraphic);
        scrollUpPressedGraphic = releaseGraphic(scrollUpPressedGraphic);
        scrollDownHoverGraphic = releaseGraphic(scrollDownHoverGraphic);
        scrollDownPressedGraphic = releaseGraphic(scrollDownPressedGraphic);
    }

    /**
     * Native: CharacterLoaderRosterListPanelVisualObject::GetEntryIndexAtScreenPoint @00430F2B.
     * Fully ported.
     */
    private int getEntryIndexAtScreenPoint(int x, int y) {
        int visibleLimit = firstVisibleEntryIndex + (entryListViewportRect.height() / entryRowTemplateRect.height());
        int entryCount = getEntryCount();
        if (entryCount < visibleLimit) {
            visibleLimit = entryCount;
        }

        for (int entryIndex = firstVisibleEntryIndex; entryIndex < visibleLimit; entryIndex++) {
            CRect rowRect = getEntryScreenRect(entryIndex);
            if (rowRect.contains(x, y)) {
                return entryIndex;
            }
        }
        return -1;
    }

    /**
     * Native: CharacterLoaderRosterListPanelVisualObject::RefreshScrollButtonGraphics @00431350.
     * Fully ported.
     */
    private void refreshScrollButtonGraphics(int x, int y, boolean isMouseDown) {
        int localX = x - ownerDialog.cRect.left;
        int localY = y - ownerDialog.cRect.top;
        if (selectedEntryIndex - 1 < 0 || !scrollUpButtonRect.contains(localX, localY)) {
            currentScrollUpGraphic = null;
        } else {
            currentScrollUpGraphic = isMouseDown
                    ? scrollUpPressedGraphic
                    : scrollUpHoverGraphic;
        }

        int entryCount = getEntryCount();
        if (selectedEntryIndex + 1 < entryCount && scrollDownButtonRect.contains(localX, localY)) {
            currentScrollDownGraphic = isMouseDown
                    ? scrollDownPressedGraphic
                    : scrollDownHoverGraphic;
        } else {
            currentScrollDownGraphic = null;
        }
    }

    /**
     * Native: CharacterLoaderRosterListPanelVisualObject::SelectEntry @00430D7D.
     * Fully ported.
     */
    private void selectEntry(int entryIndex) {
        if (entryIndex == -1) {
            return;
        }

        int entryCount = getEntryCount();
        if (entryIndex == entryCount - 1) {
            ownerDialog.selectNewCharacterEntry();
        } else {
            ownerDialog.selectRosterEntry(entryIndex);
        }
        selectedEntryIndex = entryIndex;
    }

    /**
     * Native: CharacterLoaderRosterListPanelVisualObject::ScrollUp @004310BE.
     * Fully ported.
     */
    private int scrollUp() {
        if (selectedEntryIndex - 1 < 0) {
            return 0;
        }

        if (selectedEntryIndex - 1 < firstVisibleEntryIndex) {
            firstVisibleEntryIndex--;
        }
        selectedEntryIndex--;
        selectEntry(selectedEntryIndex);
        return 1;
    }

    /**
     * Native: CharacterLoaderRosterListPanelVisualObject::ScrollDown @0043114C.
     * Fully ported.
     */
    private int scrollDown() {
        int visibleLimit = firstVisibleEntryIndex + (entryListViewportRect.height() / entryRowTemplateRect.height());
        int entryCount = getEntryCount();
        if (entryCount < visibleLimit) {
            visibleLimit = entryCount;
        }
        if (selectedEntryIndex + 1 >= entryCount) {
            return 0;
        }

        if (visibleLimit - 1 < selectedEntryIndex + 1) {
            firstVisibleEntryIndex++;
            selectedEntryIndex++;
        } else {
            selectedEntryIndex++;
        }
        selectEntry(selectedEntryIndex);
        return 1;
    }

    /**
     * Native support extracted from CharacterLoaderRosterListPanelVisualObject::OnMouseMove @00430CC2.
     * Fully ported.
     */
    private void updateCursorForHoveredEntry() {
        if (hoveredEntryIndex == -1) {
            CMousePointer.Cursor_Default.setToMousePointer();
        } else {
            CMousePointer.Cursor_Select.setToMousePointer();
        }
    }

    /**
     * Native support extracted from CharacterLoaderRosterListPanelVisualObject::Update @004306A5.
     * Fully ported.
     */
    private static void drawGraphic(CBmp64k graphic, int x, int y) {
        graphic.draw(x, y, 0, null, false);
    }

    /**
     * Native support extracted from CharacterLoaderRosterListPanelVisualObject::Update @004306A5
     * through `FUN_004564DF @004564DF`.
     */
    private void drawSelectedEntryUnderline(int ownerLeft, int ownerTop, int entryIndex) {
        int rowHeight = entryRowTemplateRect.height();
        int left = ownerLeft + entryListViewportRect.left;
        int top = ownerTop + entryListViewportRect.top - 4
                + rowHeight * ((entryIndex + 1) - firstVisibleEntryIndex);
        int right = ownerLeft + entryListViewportRect.right;
        int bottom = ownerTop + entryListViewportRect.top
                + rowHeight * ((entryIndex - firstVisibleEntryIndex) + 2);
        Globals.renderer.applyShadeToRect(left, top, right, bottom, 8);
    }

    /**
     * Native support extracted from CharacterLoaderRosterListPanelVisualObject::Update @004306A5.
     */
    private void drawEntryLabel(int ownerLeft, int ownerTop, int entryIndex, boolean isHovered) {
        Palette16 palette = isHovered ? Palettes.p2.paletteData[0] : Palettes.p1.paletteData[0];
        int rowHeight = entryRowTemplateRect.height();
        int x = ownerLeft + entryListViewportRect.left + entryListViewportRect.width() / 2;
        int y = ownerTop + entryListViewportRect.top
                + rowHeight * ((entryIndex - firstVisibleEntryIndex) + 1);
        Globals.fonts.font4.drawTextShadowed(
                x,
                y,
                ownerDialog.gameSession.characterRosterNames.get(entryIndex),
                TextAlign.CENTER.mask,
                palette,
                1
        );
    }

    /**
     * Native support extracted from CharacterLoaderRosterListPanelVisualObject::GetEntryIndexAtScreenPoint @00430F2B.
     * Fully ported.
     */
    private CRect getEntryScreenRect(int entryIndex) {
        int rowHeight = entryRowTemplateRect.height();
        int top = ownerDialog.cRect.top + entryListViewportRect.top + rowHeight * ((entryIndex - firstVisibleEntryIndex) + 1);
        return new CRect(
                ownerDialog.cRect.left + entryListViewportRect.left,
                top,
                ownerDialog.cRect.left + entryListViewportRect.left + entryRowTemplateRect.width(),
                top + rowHeight
        );
    }

    /**
     * Native support extracted from CharacterLoaderRosterListPanelVisualObject::Update @004306A5,
     * CharacterLoaderRosterListPanelVisualObject::SelectEntry @00430D7D,
     * CharacterLoaderRosterListPanelVisualObject::GetEntryIndexAtScreenPoint @00430F2B,
     * CharacterLoaderRosterListPanelVisualObject::ScrollDown @0043114C,
     * and CharacterLoaderRosterListPanelVisualObject::RefreshScrollButtonGraphics @00431350
     * through `FUN_004383C0 @004383C0`.
     * Fully ported.
     */
    private int getEntryCount() {
        return ownerDialog.getRosterEntryCount();
    }

    /**
     * Native support extracted from CharacterLoaderRosterListPanelVisualObject::InitializeRosterListPanel @0043036C.
     */
    private static int estimateFont4Height() {
        return Globals.fonts.font4.getFrameHeight();
    }

    /**
     * Native support extracted from CharacterLoaderRosterListPanelVisualObject::LoadRosterListGraphics @00430970.
     * Fully ported.
     */
    private static CBmp64k loadBmp64k(String resourcePath) {
        CBmp64k graphic = new CBmp64k(resourcePath);
        Globals.mousePointer.update();
        return graphic;
    }

    /**
     * Native support extracted from CharacterLoaderRosterListPanelVisualObject::ReleaseRosterListGraphics @00430B43.
     * Fully ported.
     */
    private static CBmp64k releaseGraphic(CBmp64k graphic) {
        return null;
    }
}
