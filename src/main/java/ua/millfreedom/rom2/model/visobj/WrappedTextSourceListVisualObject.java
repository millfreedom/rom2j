package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palette16;

import static ua.millfreedom.rom2.model.enums.MessageCodes.TEXT_LIST_SELECTION_CHANGED;

/**
 * Native class: WrappedTextSourceListVisualObject (vtbl @0x005CFDC8).
 * Purpose: wrapped text list variant that stores the source text inline after the TextListVisualObject payload.
 */
public class WrappedTextSourceListVisualObject extends TextListVisualObject {
    public static final int NATIVE_SIZE = 0x98; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int MK_LBUTTON = 0x1;
    private static final int WRAPPED_HEADER_SCROLLBAR_ID = 0xDF23;

    //0x94
    public String sourceText;

    /**
     * Native: WrappedTextSourceListVisualObject::WrappedTextSourceListVisualObject @004D4DC9.
     * Fully ported.
     */
    public WrappedTextSourceListVisualObject(
            int id,
            CRect rect,
            String sourceText,
            CBitmapFont bitmapFont,
            Palette16 textPalette,
            int rowPitch
    ) {
        super(id, rect, bitmapFont, textPalette, null, 0, null);
        initializeWrappedTextSource(sourceText, rowPitch, false);
    }

    /**
     * Native: WrappedTextSourceListVisualObject::WrappedTextSourceListVisualObject @004D4E9B.
     * Fully ported.
     */
    public WrappedTextSourceListVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            String sourceText,
            CBitmapFont bitmapFont,
            Palette16 textPalette,
            int rowPitch
    ) {
        super(id, xLeft, yTop, xRight, yBottom, bitmapFont, textPalette, null, 0, null);
        initializeWrappedTextSource(sourceText, rowPitch, true);
    }

    /**
     * Native support extracted from WrappedTextSourceListVisualObject constructors @004D4DC9 and @004D4E9B.
     * Fully ported.
     */
    private void initializeWrappedTextSource(String sourceText, int rowPitch, boolean clampVisibleRowCountToRows) {
        this.sourceText = sourceText;
        if (rowPitch == 0) {
            rowPitch = rowHeight + 2;
        }
        this.rowPitch = rowPitch;
        copyRowsFrom(bitmapFont.formatText(cRect, sourceText));
        int rowsVisibleInRect = cRect.height() / this.rowPitch;
        visibleRowCount = clampVisibleRowCountToRows ? Math.min(rowsVisibleInRect, rows.size()) : rowsVisibleInRect;
    }

    /**
     * vtbl +0x2C: WrappedTextSourceListVisualObject::Update @004D5134.
     * Fully ported.
     */
    @Override
    public void update() {
        if (m_pParent == null) {
            return;
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        m_pParent.renderSelf(screenRect);
        Globals.renderer.lockSurface();
        try {
            bitmapFont.drawWrappedTextRows(screenRect, firstVisibleRow, firstVisibleRow + visibleRowCount, rows, field0x7c, rowPitch);
        } finally {
            Globals.renderer.unlockSurface();
        }
    }

    /**
     * vtbl +0x48: WrappedTextSourceListVisualObject::OnMessage @004D5339.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * vtbl +0x4C: WrappedTextSourceListVisualObject::OnMouseMove @004D535A.
     * Fully ported.
     */
    @Override
    public int onMouseMove(@SuppressWarnings("unused") int nFlags, @SuppressWarnings("unused") int x, @SuppressWarnings("unused") int y) {
        return 0;
    }

    /**
     * vtbl +0x50: WrappedTextSourceListVisualObject::OnUserMsg @004D537B.
     * Fully ported.
     */
    @Override
    public int onUserMsg(int nFlags, int x, int y) {
        if ((nFlags & MK_LBUTTON) != 0 && linkedChildId != 0) {
            CRect screenRect = new CRect();
            clientToScreen(screenRect, cRect);
            if (((screenRect.top + screenRect.bottom) >> 1) < y) {
                selectNextRow();
            } else {
                selectPreviousRow();
            }
        }
        return 1;
    }

    /**
     * vtbl +0x54: WrappedTextSourceListVisualObject::OnLButtonDown @004D53DF.
     * Fully ported.
     */
    @Override
    public int onLButtonDown(@SuppressWarnings("unused") int nFlags, @SuppressWarnings("unused") int x, @SuppressWarnings("unused") int y) {
        return 0;
    }

    /**
     * vtbl +0x6C: WrappedTextSourceListVisualObject::OnKeyDown @004D5400.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        return super.onKeyDown(nChar);
    }

    /**
     * vtbl +0x80: WrappedTextSourceListVisualObject::SetSelectedRow @004D503D.
     * Fully ported.
     */
    @Override
    public void setSelectedRow(int rowIndex) {
        int candidateTopRow = rowIndex < 0 ? 0 : rowIndex;
        int maxFirstVisibleRow = rows.size() - visibleRowCount;
        int clampedTopRow = candidateTopRow < maxFirstVisibleRow ? candidateTopRow : maxFirstVisibleRow;
        firstVisibleRow = clampedTopRow;
        selectedRow = clampedTopRow;
        draw();
        syncLinkedSelectionChild(clampedTopRow);
        m_pParent.onMessage(TEXT_LIST_SELECTION_CHANGED, id, clampedTopRow);
    }

    /**
     * Native: WrappedTextSourceListVisualObject::setSourceText @004D4FB6.
     * Fully ported.
     */
    public void setSourceText(String text) {
        copyRowsFrom(bitmapFont.formatText(cRect, text));
        visibleRowCount = Math.min(rows.size(), cRect.height() / rowPitch);
    }

    /**
     * Native: WrappedTextSourceListVisualObject::configureWrappedTextSourceRows @004D51DB.
     * Fully ported.
     */
    public void configureWrappedTextSourceRows() {
        visibleRowCount = rows.size();
        int totalHeight = rowHeight * visibleRowCount;
        if (totalHeight <= cRect.height()) {
            cRect.bottom = cRect.top + rowPitch * visibleRowCount;
            return;
        }

        cRect.right -= 0x1A;
        copyRowsFrom(bitmapFont.formatText(cRect, sourceText));
        linkedChildId = WRAPPED_HEADER_SCROLLBAR_ID;
        m_pParent.addChild(new PostSetupVisualObject(
                linkedChildId,
                cRect.right,
                cRect.top,
                cRect.right + 0x18,
                cRect.bottom,
                null
        ));
        visibleRowCount = cRect.height() / rowPitch;
    }

    /**
     * Native support: linked-child scrollbar update branch inside
     * WrappedTextSourceListVisualObject::SetSelectedRow @004D503D.
     * Fully ported.
     */
    private void syncLinkedSelectionChild(int selectedTopRow) {
        CVisualObject linkedChild = m_pParent.getChildById(linkedChildId);
        if (linkedChild != null) {
            ((PostSetupVisualObject) linkedChild).syncSelectionState(selectedTopRow, rows.size() - visibleRowCount + 1);
        }
    }
}
