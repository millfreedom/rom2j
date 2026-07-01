package ua.millfreedom.rom2.model.window;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CRect;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Java sidecar for modeled CWnd geometry.
 * Java port status: support only.
 * not ported.
 */
final class CWndState {
    private static final Map<CWnd, CRect> BOUNDS_BY_WINDOW = new IdentityHashMap<>();
    private static final Map<CWnd, Map<Integer, String>> DIALOG_TEXT_BY_WINDOW = new IdentityHashMap<>();
    private static final Map<CWnd, CreatedWindowState> CREATED_STATE_BY_WINDOW = new IdentityHashMap<>();

    /**
     * Java utility constructor.
     * not ported.
     */
    private CWndState() {
    }

    /**
     * Java helper for CWnd::GetClientRect @00493150.
     * Java port status: support only.
     * not ported.
     */
    static void getClientRect(CWnd window, CRect rect) {
        CRect bounds = BOUNDS_BY_WINDOW.get(window);
        if (bounds != null) {
            rect.set(0, 0, bounds.width(), bounds.height());
            return;
        }
        if (window instanceof CMainWindow) {
            rect.set(0, 0, Globals.mainWindowRect.width(), Globals.mainWindowRect.height());
            return;
        }
        rect.setEmpty();
    }

    /**
     * Java helper for CWnd::SetWindowPos @005A85EE.
     * Java port status: support only.
     * not ported.
     */
    static boolean setWindowPos(CWnd window, CWnd pWndInsertAfter, int x, int y, int cx, int cy, int nFlags) {
        BOUNDS_BY_WINDOW.computeIfAbsent(window, ignored -> new CRect()).set(x, y, x + cx, y + cy);
        return true;
    }

    /**
     * Java helper for CListBox::Create @005B5D28, CStatic::Create @005B5C1B, and CEdit::Create @005B5FC4 call sites in
     * CMainWindow::OnCreate @004826A0.
     * Java port status: support only.
     * not ported.
     */
    static boolean createChildWindow(CWnd window, int style, CRect rect, CWnd parent, int controlId, String text) {
        window.m_hWnd = new ModeledWindowHandle(window);
        window.m_hWndOwner = parent;
        CREATED_STATE_BY_WINDOW.put(window, new CreatedWindowState(parent, style, controlId, text));
        BOUNDS_BY_WINDOW.computeIfAbsent(window, ignored -> new CRect()).set(rect);
        DIALOG_TEXT_BY_WINDOW.computeIfAbsent(parent, ignored -> new HashMap<>()).put(controlId, text);
        return true;
    }

    /**
     * Java helper for CStatusBar::Create @005B8CB7 in CMainWindow::OnCreate @004826A0.
     * Java port status: support only.
     * not ported.
     */
    static boolean createOwnedWindow(CWnd window, CWnd parent, int style, int controlId) {
        window.m_hWnd = new ModeledWindowHandle(window);
        window.m_hWndOwner = parent;
        CREATED_STATE_BY_WINDOW.put(window, new CreatedWindowState(parent, style, controlId, ""));
        return true;
    }

    /**
     * Java helper for CWnd::SetDlgItemTextA @005A8384.
     * Java port status: support only.
     * not ported.
     */
    static void setDlgItemText(CWnd window, int controlId, String text) {
        DIALOG_TEXT_BY_WINDOW.computeIfAbsent(window, ignored -> new HashMap<>()).put(controlId, text);
    }

    /**
     * Java support for WM_SIZE client extents before CMainWindow::OnSize @004926CA reads GetClientRect.
     */
    static void setClientSize(CWnd window, int cx, int cy) {
        BOUNDS_BY_WINDOW.computeIfAbsent(window, ignored -> new CRect()).set(0, 0, cx, cy);
    }

    /**
     * Java sidecar for modeled HWND identity.
     * Java port status: support only.
     * not ported.
     */
    private record ModeledWindowHandle(CWnd window) {
    }

    /**
     * Java sidecar for modeled window/control creation state.
     * Java port status: support only.
     * not ported.
     */
    private record CreatedWindowState(CWnd parent, int style, int controlId, String text) {
    }
}
