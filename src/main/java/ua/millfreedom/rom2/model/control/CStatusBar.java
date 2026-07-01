package ua.millfreedom.rom2.model.control;

import ua.millfreedom.rom2.model.window.CWnd;

/**
 * Native type: CStatusBar embedded in CMainWindow at +0x5EC.
 */
public final class CStatusBar extends CWnd {
    /**
     * Java allocation for the embedded native CStatusBar control.
     * not ported.
     */
    public CStatusBar() {
    }

    /**
     * Native: CStatusBar::Create @005B8CB7.
     * Java port status: modeled HWND/control creation state.
     */
    public boolean create(CWnd parent, int style, int controlId) {
        return createModeledOwnedWindow(parent, style, controlId);
    }
}
