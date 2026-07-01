package ua.millfreedom.rom2.model.window;

import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;

public class CWnd extends CCmdTarget {
    //0x1c
    public Object m_hWnd;
    //0x20
    public Object m_hWndOwner;
    //0x24
    public int m_nFlags;
    //0x28
    public Object m_pfnSuper;
    //0x2c
    public int m_nModalResult;
    //0x30
    public Object m_pDropTarget;
    //0x34
    public Object m_pCtrlCont;
    //0x38
    public Object m_pCtrlSite;

    /**
     * Native: CWnd::GetClientRect @00493150.
     * Java port status: fully ported for modeled window/control geometry; HWND-backed dimensions are represented by
     * CWndState support.
     */
    public void getClientRect(CRect rect) {
        CWndState.getClientRect(this, rect);
    }

    /**
     * Native: CWnd::SetWindowPos @005A85EE.
     * Java port status: fully ported for modeled window/control geometry; HWND/OLE platform calls remain outside the
     * Java port boundary.
     */
    public boolean setWindowPos(CWnd pWndInsertAfter, int x, int y, int cx, int cy, int nFlags) {
        return CWndState.setWindowPos(this, pWndInsertAfter, x, y, cx, cy, nFlags);
    }

    /**
     * Native support extracted from CListBox::Create @005B5D28, CStatic::Create @005B5C1B, and CEdit::Create @005B5FC4
     * call sites in CMainWindow::OnCreate @004826A0.
     */
    protected boolean createModeledChildWindow(int style, CRect rect, CWnd parent, int controlId, String text) {
        return CWndState.createChildWindow(this, style, rect, parent, controlId, text);
    }

    /**
     * Native support extracted from CStatusBar::Create @005B8CB7 call in CMainWindow::OnCreate @004826A0.
     */
    protected boolean createModeledOwnedWindow(CWnd parent, int style, int controlId) {
        return CWndState.createOwnedWindow(this, parent, style, controlId);
    }

    /**
     * Native: CWnd::SetDlgItemTextA @005A8384.
     * Java port status: modeled control-text storage for HWND-backed child controls.
     */
    public void setDlgItemText(int controlId, String text) {
        CWndState.setDlgItemText(this, controlId, text);
    }

    /**
     * Native boundary: CWnd::PostMessage @0041E3F0.
     * Java support posts to the global targetless message system because this port does not model per-HWND targets.
     */
    public void postMessage(MessageCodes msg, Object wParam, Object lParam) {
        MessageSystem.post(msg, wParam, lParam);
    }

    /**
     * Native: CWnd::SendMessage @004A8540.
     * Full port for modeled window targets. Java dispatches synchronously through the modeled window procedure because
     * this port does not model per-HWND targets.
     */
    public int sendMessage(MessageCodes msg, Object wParam, Object lParam) {
        return windowProc(msg, wParam, lParam);
    }

    /**
     * Native boundary: CWnd::WindowProc @005a5b04, reached from the shared epilogue of
     * CMainWindow::WindowProc @004852D8.
     * not ported.
     */
    public int windowProc(MessageCodes msg, Object wParam, Object lParam) {
        return 0;
    }

    /**
     * Native boundary: CWnd::DefWindowProc @005a5403, reached from the input-controller handled fallback in
     * CMainWindow::WindowProc @004852D8.
     * not ported.
     */
    public int defWindowProc(MessageCodes msg, Object wParam, Object lParam) {
        return 0;
    }
}
