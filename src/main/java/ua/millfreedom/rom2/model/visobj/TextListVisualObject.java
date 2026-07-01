package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.color.RGB16;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;

import java.util.ArrayList;
import java.util.List;

import static ua.millfreedom.rom2.model.enums.MessageCodes.TEXT_LIST_SELECTION_CHANGED;
import static ua.millfreedom.rom2.model.enums.MessageCodes.TEXT_LIST_SELECTION_COMMITTED;
import static ua.millfreedom.rom2.model.enums.MessageCodes.TEXT_LIST_SELECTION_DBLCLK;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_DOWN;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_NEXT;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_PRIOR;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_UP;
import static ua.millfreedom.rom2.model.window.windowproc.handlers.CMainWindowWindowProcSupport.readMessageInt;

/**
 * Native class: TextListVisualObject.
 * Purpose: text list view with row-height/font bookkeeping and selection navigation.
 */
public class TextListVisualObject extends CVisualObject {
    private static final int MK_LBUTTON = 0x1;

    public static final int NATIVE_SIZE = 0x94; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    //0x5c
    public int rowHeight;
    //0x60
    public int rowPitch;
    //0x64
    public final List<String> rows = new ArrayList<>();
    //0x78
    public CBitmapFont bitmapFont;
    //0x7c
    public Palette16 field0x7c;
    //0x80
    public Palette16 field0x80;
    //0x84
    public int firstVisibleRow;
    //0x88
    public int selectedRow;
    //0x8c
    public int visibleRowCount;
    //0x90
    public int linkedChildId;

    /**
     * Native: TextListVisualObject::TextListVisualObject @004D82A1.
     * Fully ported.
     */
    public TextListVisualObject() {
        super();
    }

    /**
     * Native: TextListVisualObject::TextListVisualObject @004D82F8.
     * Fully ported.
     */
    public TextListVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            Object bitmapFont,
            Palette16 field0x7c,
            Palette16 field0x80,
            int linkedChildId,
            String name
    ) {
        super(id, xLeft, yTop, xRight, yBottom, name);
        initializeTextListFields((CBitmapFont) bitmapFont, field0x7c, field0x80, linkedChildId);
    }

    /**
     * Native: TextListVisualObject::TextListVisualObject @004D8417.
     * Fully ported.
     */
    public TextListVisualObject(int id, CRect rect, Object bitmapFont, Palette16 field0x7c, Palette16 field0x80, int linkedChildId, String name) {
        super(id, rect, name);
        initializeTextListFields((CBitmapFont) bitmapFont, field0x7c, field0x80, linkedChildId);
    }

    /**
     * Native support extracted from TextListVisualObject constructors @004D82F8 and @004D8417.
     * Fully ported.
     */
    private void initializeTextListFields(
            CBitmapFont bitmapFont,
            Palette16 field0x7c,
            Palette16 field0x80,
            int linkedChildId
    ) {
        this.bitmapFont = bitmapFont;
        this.field0x7c = field0x7c;
        this.field0x80 = field0x80;
        this.linkedChildId = linkedChildId;
        rowHeight = bitmapFont.getHeight();
        rowPitch = rowHeight + 4;
        m_nState |= 0x2;
        selectedRow = -1;
        firstVisibleRow = 0;
        int height = cRect.height();
        visibleRowCount = height / rowPitch;
        cRect.bottom = cRect.top + 2 + (visibleRowCount * rowPitch);
    }

    /**
     * vtbl +0x2C: TextListVisualObject::Update @004D8802.
     * Fully ported.
     */
    @Override
    public void update() {
        if (m_pParent == null) {
            return;
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        Globals.renderer.lockSurface();
        try {
            m_pParent.renderSelf(screenRect);
            int y = screenRect.top + 2;
            for (int rowIndex = firstVisibleRow; rowIndex < firstVisibleRow + visibleRowCount; rowIndex++) {
                int bevelPitch = rowPitch - 2;
                int verticalBottom = y - 3 + bevelPitch;
                int bottomLineY = y - 2 + bevelPitch;
                Globals.renderer.drawLine(screenRect.left + 1, y - 2, screenRect.right - 5, y - 2, RGB16.from(8, 8, 8).val());
                Globals.renderer.drawLine(screenRect.left, y - 1, screenRect.left, verticalBottom, RGB16.from(8, 8, 8).val());
                Globals.renderer.drawLine(screenRect.left + 1, bottomLineY, screenRect.right - 5, bottomLineY, RGB16.from(0x5E, 0x73, 0x65).val());
                Globals.renderer.drawLine(screenRect.right - 4, y - 1, screenRect.right - 4, verticalBottom, RGB16.from(0x5E, 0x73, 0x65).val());

                if (rowIndex == selectedRow) {
                    drawSelectionFrame(screenRect.left, y, screenRect);
                    drawRowText(
                            rowIndex,
                            screenRect.left + 4,
                            y - 2,
                            checkStateFlag(0x4) == 0 ? Palettes.gray : Palettes.yellowish
                    );
                } else {
                    drawRowText(rowIndex, screenRect.left + 4, y - 2, Palettes.grayDim);
                }
                y += rowPitch;
            }
        } finally {
            Globals.renderer.unlockSurface();
        }
    }

    /**
     * vtbl +0x40: TextListVisualObject::getValueRecursiveSize @0044F150.
     * Fully ported.
     */
    @Override
    public int getValueRecursiveSize() {
        return 4;
    }

    /**
     * vtbl +0x3C: TextListVisualObject::GetValue @004D87A1.
     * Fully ported.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void getValue(Object value) {
        List<String> destinationRows = (List<String>) value;
        destinationRows.clear();
        destinationRows.addAll(rows);
    }

    /**
     * vtbl +0x44: TextListVisualObject::SetValue @004D87CF.
     * Fully ported.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void setValue(Object value) {
        List<String> sourceRows = (List<String>) value;
        rows.clear();
        rows.addAll(sourceRows);
    }

    /**
     * vtbl +0x48: TextListVisualObject::OnMessage @004D8D14.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        int result = super.onMessage(msg, wParam, lParam);
        if (result != 0) {
            return result;
        }

        int w = readMessageInt(wParam);
        int l = readMessageInt(lParam);

        if (w != linkedChildId) {
            return 0;
        }

        return switch (msg) {
            case TEXT_LIST_SET_SELECTED_ROW -> {
                setSelectedRow(l);
                yield 1;
            }
            case TEXT_LIST_SELECT_PREVIOUS_ROW -> {
                selectPreviousRow();
                yield 1;
            }
            case TEXT_LIST_SELECT_NEXT_ROW -> {
                selectNextRow();
                yield 1;
            }
            case TEXT_LIST_PAGE_UP -> {
                pageUp();
                yield 1;
            }
            case TEXT_LIST_PAGE_DOWN -> {
                pageDown();
                yield 1;
            }
            default -> 0;
        };
    }

    /**
     * vtbl +0x4C: TextListVisualObject::OnMouseMove @004D8F89.
     * Fully ported.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        if (checkStateFlag(0x4) == 0) {
            m_pParent.switchEnabledChild(this, true);
        }
        if ((nFlags & MK_LBUTTON) != 0) {
            onLButtonDown(nFlags, x, y);
        }
        return 0;
    }

    /**
     * vtbl +0x54: TextListVisualObject::OnLButtonDown @004D8E24.
     * Fully ported.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        int rowOffset = getRowOffsetAtScreenY(y);
        if (rowOffset >= visibleRowCount) {
            throw new IllegalStateException("TextListVisualObject::OnLButtonDown reached native undefined row path");
        }

        int rowIndex = firstVisibleRow + rowOffset;
        int rowCount = rows.size();
        int nextSelectedRow = rowIndex < rowCount - 1 ? rowIndex : rowCount - 1;
        selectedRow = nextSelectedRow;
        draw();
        syncLinkedSelectionChild();
        m_pParent.onMessage(TEXT_LIST_SELECTION_CHANGED, id, selectedRow);
        return 1;
    }

    /**
     * vtbl +0x58: TextListVisualObject::OnLButtonUp @004D8F17.
     * Fully ported.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        m_pParent.onMessage(TEXT_LIST_SELECTION_COMMITTED, id, selectedRow);
        return 1;
    }

    /**
     * vtbl +0x5C: TextListVisualObject::OnLButtonDblClk @004D8F50.
     * Fully ported.
     */
    @Override
    public int onLButtonDblClk(int nFlags, int x, int y) {
        m_pParent.onMessage(TEXT_LIST_SELECTION_DBLCLK, id, selectedRow);
        return 1;
    }

    /**
     * vtbl +0x6C: TextListVisualObject::OnKeyDown @004D8FDB.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        if (checkStateFlag(0x4) == 0) {
            return super.onKeyDown(nChar);
        }

        switch (nChar) {
            case VK_PRIOR -> pageUp();
            case VK_NEXT -> pageDown();
            case VK_UP -> selectPreviousRow();
            case VK_DOWN -> selectNextRow();
            default -> {
                return 0;
            }
        }
        m_pParent.onMessage(TEXT_LIST_SELECTION_CHANGED, id, selectedRow);
        return 1;
    }

    /**
     * vtbl +0x78: TextListVisualObject::IsRowIndexValid @004D8C0D.
     * Fully ported.
     */
    public boolean isRowIndexValid(int rowIndex) {
        return 0 <= rowIndex && rowIndex < rows.size();
    }

    /**
     * vtbl +0x7C: TextListVisualObject::DrawRowText @004D8C45.
     * Fully ported.
     */
    public void drawRowText(int rowIndex, int x, int y, Palette16 textPalette) {
        if (!isRowIndexValid(rowIndex)) {
            return;
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        Globals.renderer.pushClip(x, y, screenRect.right - 6, y + 2 + bitmapFont.getHeight());
        try {
            bitmapFont.drawTextShadowed(x, y, rows.get(rowIndex), 0, textPalette, 1);
        } finally {
            Globals.renderer.popClip();
        }
    }

    /**
     * vtbl +0x80: TextListVisualObject::SetSelectedRow @004D8564.
     * Fully ported.
     */
    public void setSelectedRow(int rowIndex) {
        if (rowIndex >= rows.size()) {
            rowIndex = rows.size() - 1;
        }

        if (rowIndex < 0) {
            firstVisibleRow = 0;
        } else if (rowIndex < firstVisibleRow) {
            firstVisibleRow = rowIndex;
        } else if (firstVisibleRow + visibleRowCount <= rowIndex) {
            firstVisibleRow = rowIndex - visibleRowCount + 1;
        }

        selectedRow = rowIndex;
        draw();
        syncLinkedSelectionChild();
        m_pParent.onMessage(TEXT_LIST_SELECTION_CHANGED, id, selectedRow);
    }

    /**
     * Native: TextListVisualObject::GetSelectedRow @0044F0F0.
     * Fully ported.
     */
    public int getSelectedRow() {
        return selectedRow;
    }

    /**
     * Native: TextListVisualObject::assignSelectedRow @0044F110.
     * Fully ported.
     */
    public void assignSelectedRow(int rowIndex) {
        selectedRow = rowIndex;
    }

    /**
     * Native: TextListVisualObject::GetFirstVisibleRow @0044F0B0.
     * Fully ported.
     */
    public int getFirstVisibleRow() {
        return firstVisibleRow;
    }

    /**
     * Native: TextListVisualObject::SetFirstVisibleRow @0044F0D0.
     * Fully ported.
     */
    public void setFirstVisibleRow(int rowIndex) {
        firstVisibleRow = rowIndex;
    }

    /**
     * Native: TextListVisualObject::FollowAppendedRowIfSelectionAtEnd @0044EE00.
     * Fully ported.
     */
    public void followAppendedRowIfSelectionAtEnd() {
        if (selectedRow == getRowCount() - 2) {
            setSelectedRow(firstVisibleRow + visibleRowCount);
        }
    }

    /**
     * vtbl +0x84: TextListVisualObject::GetRowCount @0044F130.
     * Fully ported.
     */
    public int getRowCount() {
        return rows.size();
    }

    /**
     * Native: TextListVisualObject::GetRowTextAtClampedIndex @0044F2D0.
     * Fully ported.
     */
    public String getRowTextAtClampedIndex(int rowIndex) {
        int resolvedRowIndex = rowIndex < rows.size() ? rowIndex : rows.size();
        return rows.get(resolvedRowIndex);
    }

    /**
     * vtbl +0x88: TextListVisualObject::DrawSelectionFrame @004D8BD3.
     * Fully ported.
     */
    public void drawSelectionFrame(int x, int y, CRect screenRect) {
        Globals.renderer.applyShadeToRect(x, y - 1, screenRect.right - 4, y - 3 + rowPitch, 10);
    }

    /**
     * Native helper: TextListVisualObject::GetRowOffsetAtScreenY @004D852A.
     * Fully ported.
     */
    protected int getRowOffsetAtScreenY(int y) {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        return ((y - screenRect.top) - 1) / rowPitch;
    }

    /**
     * Native helper: TextListVisualObject::SelectPreviousRow @004D866E.
     * Fully ported.
     */
    protected void selectPreviousRow() {
        if (selectedRow > 0) {
            setSelectedRow(selectedRow - 1);
        }
    }

    /**
     * Native helper: TextListVisualObject::SelectNextRow @004D86A0.
     * Fully ported.
     */
    protected void selectNextRow() {
        setSelectedRow(selectedRow + 1);
    }

    /**
     * Native helper: TextListVisualObject::PageUp @004D86C6.
     * Fully ported.
     */
    protected void pageUp() {
        if (selectedRow == firstVisibleRow) {
            setSelectedRow(firstVisibleRow - visibleRowCount);
            return;
        }
        setSelectedRow(firstVisibleRow);
    }

    /**
     * Native helper: TextListVisualObject::PageDown @004D8720.
     * Fully ported.
     */
    protected void pageDown() {
        int lastVisibleRow = firstVisibleRow + visibleRowCount - 1;
        if (selectedRow == lastVisibleRow) {
            setSelectedRow(lastVisibleRow + visibleRowCount);
            return;
        }
        setSelectedRow(lastVisibleRow);
    }

    /**
     * Native: TextListVisualObject::RemoveRowAndAdjustSelection @0044F250.
     * Fully ported.
     */
    protected void removeRowAndAdjustSelection(int rowIndex) {
        rows.remove(rowIndex);
        if (rows.isEmpty()) {
            selectedRow -= 1;
        }
    }

    /**
     * Native: TextListVisualObject::ClearSelection @0044F2A0.
     * Fully ported.
     */
    public void clearSelection() {
        rows.clear();
        selectedRow = -1;
    }

    /**
     * Java convenience helper.
     * not ported.
     */
    protected void copyRowsFrom(List<String> sourceRows) {
        if (sourceRows == null || sourceRows.isEmpty()) {
            rows.clear();
            return;
        }
        setValue(sourceRows);
    }

    /**
     * Java convenience helper.
     * not ported.
     */
    protected void copyRowsTo(List<String> destinationRows) {
        if (destinationRows == null) {
            return;
        }
        getValue(destinationRows);
    }

    /**
     * Native support extracted from TextListVisualObject::SetSelectedRow @004D8564 and
     * TextListVisualObject::OnLButtonDown @004D8E24.
     * Fully ported.
     */
    private void syncLinkedSelectionChild() {
        CVisualObject linkedChild = m_pParent.getChildById(linkedChildId);
        if (linkedChild != null) {
            ((PostSetupVisualObject) linkedChild).syncSelectionState(selectedRow, rows.size());
        }
    }

}
