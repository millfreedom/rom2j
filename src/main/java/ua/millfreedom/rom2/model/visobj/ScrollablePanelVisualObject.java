package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palettes;

import static ua.millfreedom.rom2.model.enums.MessageCodes.SCROLLABLE_PANEL_TOGGLE_EXPANSION;
import static ua.millfreedom.rom2.model.enums.MessageCodes.TEXT_LIST_SELECTION_COMMITTED;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_DOWN;
import static ua.millfreedom.rom2.model.window.windowproc.handlers.CMainWindowWindowProcSupport.readMessageInt;

/**
 * Native class: ScrollablePanelVisualObject.
 * Purpose: scrollable list panel with a header text control, wrapped-text list child, and expand/collapse button.
 */
public class ScrollablePanelVisualObject extends CVisualObject {
    public static final int NATIVE_SIZE = 0x6C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int STATE_VISIBLE = 0x08;
    private static final int STATE_HIDDEN = 0x20;
    private static final int HEADER_HEIGHT = 0x18;

    //0x5c
    public WrappedTextListVisualObject listControl;
    //0x60
    public ScrollablePanelHeaderStaticTextVisualObject headerControl;
    //0x64
    public int initialHeaderPending;
    //0x68
    public int expandedFlag;

    /**
     * Native: ScrollablePanelVisualObject::ScrollablePanelVisualObject @004DE4F3.
     * Fully ported.
     */
    public ScrollablePanelVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, String name) {
        super(id, xLeft, yTop, xRight, yTop + HEADER_HEIGHT, name);
        int width = xRight - xLeft;

        headerControl = new ScrollablePanelHeaderStaticTextVisualObject(
                1,
                0,
                0,
                width - HEADER_HEIGHT,
                HEADER_HEIGHT,
                Globals.fonts.font1,
                Palettes.grayDim,
                name
        );
        headerControl.setStateFlag(1, 0);
        addChild(headerControl);

        listControl = new WrappedTextListVisualObject(
                2,
                new CRect(0, HEADER_HEIGHT, width, yBottom - yTop),
                Globals.fonts.font1,
                Palettes.grayDim,
                Palettes.gray,
                10,
                name
        );
        listControl.setStateFlag(STATE_HIDDEN, 1);
        addChild(listControl);

        CVisualObject scrollButton = new SpriteCommandButtonVisualObject(
                3,
                width - 0x16,
                2,
                width - 2,
                0x16,
                GUI.sprScrollBars,
                HEADER_HEIGHT,
                SCROLLABLE_PANEL_TOGGLE_EXPANSION,
                0,
                ""
        );
        addChild(scrollButton);

        initialHeaderPending = 1;
        expandedFlag = 0;
        m_nState |= 0x2;
    }

    /**
     * vtbl +0x30: ScrollablePanelVisualObject::RenderSelf @004DEB64.
     * Fully ported.
     */
    @Override
    public void renderSelf(CRect clipRect) {
        m_pParent.renderSelf(clipRect);
    }

    /**
     * vtbl +0x34: ScrollablePanelVisualObject::Draw @004DE701.
     * Fully ported.
     */
    @Override
    public void draw() {
        super.draw();
    }

    /**
     * vtbl +0x3C: ScrollablePanelVisualObject::getValue @004DE714.
     * Fully ported.
     */
    @Override
    public void getValue(Object value) {
        headerControl.getValue(value);
    }

    /**
     * Native support extracted from ScrollablePanelVisualObject::getValue @004DE714.
     */
    public void copyHeaderText(StringBuilder out) {
        headerControl.copyTextToBuffer(out);
    }

    /**
     * Native: ScrollablePanelVisualObject::AddRow @004DE736.
     * Fully ported.
     */
    public void addRow(String rowText) {
        if (initialHeaderPending != 0) {
            headerControl.setValue(rowText);
            initialHeaderPending = 0;
        }
        listControl.rows.add(rowText);
        if (listControl.selectedRow < 0) {
            listControl.selectedRow += 1;
        }
    }

    /**
     * Native: ScrollablePanelVisualObject::SetSelectedRow @004DE77A.
     * Fully ported.
     */
    public void setSelectedRow(int rowIndex) {
        headerControl.setValue(listControl.getRowTextAtClampedIndex(rowIndex));
        listControl.assignSelectedRow(rowIndex);
    }

    /**
     * vtbl +0x48: ScrollablePanelVisualObject::OnMessage @004DE9BB.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        if (msg == SCROLLABLE_PANEL_TOGGLE_EXPANSION) {
            toggleExpandedState();
            return 1;
        }

        int w = readMessageInt(wParam);

        if (msg == MessageCodes.WM_LBUTTONDOWN) {
            CRect screenRect = new CRect();
            clientToScreen(screenRect, cRect);
            int mouseX = Globals.mousePointer.getX();
            int mouseY = Globals.mousePointer.getY();
            if (!screenRect.contains(mouseX, mouseY)) {
                collapseList();
                m_pParent.onLButtonDown(1, mouseX, mouseY);
                return 1;
            }

            m_pParent.switchEnabledChild(this, true);
            return super.onMessage(MessageCodes.WM_LBUTTONDOWN, wParam, lParam);
        }

        if (msg == TEXT_LIST_SELECTION_COMMITTED && w == listControl.id) {
            commitSelectedRowToHeader();
            return 1;
        }

        if (msg == MessageCodes.WM_MOUSEMOVE) {
            return super.onMessage(MessageCodes.WM_MOUSEMOVE, wParam, lParam);
        }

        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * vtbl +0x54: ScrollablePanelVisualObject::OnLButtonDown @004DEAF3.
     * Fully ported.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        return 1;
    }

    /**
     * vtbl +0x6C: ScrollablePanelVisualObject::OnKeyDown @004DEB05.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        if (nChar == VK_DOWN && checkStateFlag(0x4) != 0) {
            setEnabled(1);
            listControl.setEnabled(1);
            toggleExpandedState();
            return 1;
        }
        return super.onKeyDown(nChar);
    }

    /**
     * Native helper: ScrollablePanelVisualObject::ToggleExpandedState @004DE833.
     * Fully ported.
     */
    private void toggleExpandedState() {
        if (expandedFlag == 0) {
            expandedFlag = 1;
            if (checkStateFlag(STATE_VISIBLE) == 0) {
                setVisible(1);
            }
            listControl.setStateFlag(STATE_HIDDEN, 0);
            listControl.setEnabled(1);
            cRect.bottom = cRect.top + HEADER_HEIGHT + listControl.getRect().height();

            CRect screenRect = new CRect();
            clientToScreen(screenRect, cRect);
            screenRect.top += HEADER_HEIGHT;
            m_pParent.renderSelf(screenRect);
            draw();
            return;
        }
        collapseList();
    }

    /**
     * Native helper: ScrollablePanelVisualObject::CollapseList @004DE7BE.
     * Fully ported.
     */
    private void collapseList() {
        setVisible(0);
        expandedFlag = 0;
        listControl.setStateFlag(STATE_HIDDEN, 1);
        listControl.setEnabled(0);
        cRect.bottom = cRect.top + HEADER_HEIGHT;
        m_pParent.draw();
        draw();
    }

    /**
     * Native helper: ScrollablePanelVisualObject::CommitSelectedRowToHeader @004DE90D.
     * Fully ported.
     */
    final void commitSelectedRowToHeader() {
        if (expandedFlag != 0) {
            setVisible(0);
            expandedFlag = 0;
        }
        listControl.setEnabled(0);
        listControl.setStateFlag(STATE_HIDDEN, 1);
        headerControl.setValue(listControl.getRowTextAtClampedIndex(listControl.getSelectedRow()));
        cRect.bottom = cRect.top + HEADER_HEIGHT;
        m_pParent.draw();
        draw();
    }
}
