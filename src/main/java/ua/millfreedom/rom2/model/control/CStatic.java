package ua.millfreedom.rom2.model.control;

import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.window.CWnd;

/**
 * Native type: CStatic embedded in CMainWindow at +0x6E0 and +0x71C.
 */
public final class CStatic extends CWnd {
    /**
     * Java allocation for the embedded native CStatic controls.
     * not ported.
     */
    public CStatic() {
    }

    /**
     * Native: CStatic::Create @005B5C1B.
     * Java port status: modeled HWND/control creation state.
     */
    public boolean create(String text, int style, CRect rect, CWnd parent, int controlId) {
        return createModeledChildWindow(style, rect, parent, controlId, text);
    }
}
