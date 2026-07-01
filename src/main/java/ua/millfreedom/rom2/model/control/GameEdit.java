package ua.millfreedom.rom2.model.control;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.CServerApp;
import ua.millfreedom.rom2.model.window.CWnd;

/**
 * Native type: GameEdit embedded in CMainWindow at +0x758.
 */
public final class GameEdit extends CWnd {
    /**
     * Java allocation for the embedded native GameEdit control.
     * not ported.
     */
    public GameEdit() {
    }

    /**
     * Native: CEdit::Create @005B5FC4.
     * Java port status: modeled HWND/control creation state.
     */
    public boolean create(int style, CRect rect, CWnd parent, int controlId) {
        return createModeledChildWindow(style, rect, parent, controlId, "");
    }

    /**
     * Native support extracted from CWnd::GetWindowText call in GameEdit::OnKeyDown @00492747.
     */
    public String getWindowText() {
        return GameEditState.getWindowText(this);
    }

    /**
     * Native support for Java-side edit text state used by GameEdit::OnKeyDown @00492747.
     */
    public void setWindowText(String text) {
        GameEditState.setWindowText(this, text);
    }

    /**
     * Native support extracted from CEdit::SetSel @00493530 call in GameEdit::OnKeyDown @00492747.
     */
    public void setSel(int start, int end) {
        GameEditState.setSel(this, start, end);
    }

    /**
     * Native: GameEdit::OnKeyDown @00492747.
     * Java port status: fully ported for the recovered server-chat side effects; Java state models the edit text and
     * selection behavior.
     */
    public void onKeyDown(int keyCode, int repeatCount, int flags) {
        if (keyCode == 0x0D) {
            String text = getWindowText();
            CServerApp.sendServerChatText(text, null);
            Globals.gameServer.pushMessage(text);
            setSel(0xFFFF0000, 0);
        }
    }
}
