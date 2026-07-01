package ua.millfreedom.rom2.model.control;

import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.window.CWnd;

/**
 * Native support type extracted from the windowed listbox call sites in CMainWindow::WindowProc @004852D8.
 */
public class CListBox extends CWnd {
    /**
     * Native: CListBox::Create @005B5D28.
     * Java port status: modeled HWND/control creation state.
     */
    public boolean create(int style, CRect rect, CWnd parent, int controlId) {
        return createModeledChildWindow(style, rect, parent, controlId, "");
    }

    /**
     * Native: CListBox::AddString @004933A0.
     * Java port status: fully ported.
     */
    public int addString(String entry) {
        return CListBoxState.addString(this, entry);
    }

    /**
     * Native: CListBox::SetCurSel @00493370.
     * Java port status: fully ported.
     */
    public void setCurSel(int selectionIndex) {
        CListBoxState.setCurSel(this, selectionIndex);
    }

    /**
     * Native: CListBox::GetCurSel @00493340.
     * Java port status: fully ported.
     */
    public int getCurSel() {
        return CListBoxState.getCurSel(this);
    }

    /**
     * Native support extracted from CListBox::GetText caller CMainWindow::kickSelectedPlayerCommand @00492A3A.
     */
    public String getText(int index) {
        return CListBoxState.getText(this, index);
    }

    /**
     * Native: CListBox::ResetContent @004933D0.
     * Java port status: fully ported.
     */
    public void resetContent() {
        CListBoxState.resetContent(this);
    }

    /**
     * Native: CListBox::SelectString @00493400.
     * Java port status: fully ported.
     */
    public int selectString(int startAfterIndex, String itemPrefix) {
        return CListBoxState.selectString(this, startAfterIndex, itemPrefix);
    }
}
