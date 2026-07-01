package ua.millfreedom.rom2.model.control;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Java sidecar for the GameEdit port.
 * Java port status: support only.
 * not ported.
 */
final class GameEditState {
    private static final Map<GameEdit, State> STATE_BY_EDIT = new IdentityHashMap<>();

    /**
     * Java utility constructor.
     * not ported.
     */
    private GameEditState() {
    }

    /**
     * Java helper for the CWnd::GetWindowText call in GameEdit::OnKeyDown @00492747.
     * Java port status: support only.
     * not ported.
     */
    static String getWindowText(GameEdit edit) {
        return STATE_BY_EDIT.computeIfAbsent(edit, ignored -> new State()).windowText;
    }

    /**
     * Java helper for the edit text state read by GameEdit::OnKeyDown @00492747.
     * Java port status: support only.
     * not ported.
     */
    static void setWindowText(GameEdit edit, String text) {
        STATE_BY_EDIT.computeIfAbsent(edit, ignored -> new State()).windowText = text;
    }

    /**
     * Java helper for the CEdit::SetSel @00493530 call in GameEdit::OnKeyDown @00492747.
     * Java port status: support only.
     * not ported.
     */
    static void setSel(GameEdit edit, int start, int end) {
        State state = STATE_BY_EDIT.computeIfAbsent(edit, ignored -> new State());
        state.selectionStart = start;
        state.selectionEnd = end;
    }

    /**
     * Java sidecar state for the GameEdit port.
     * Java port status: support only.
     * not ported.
     */
    private static final class State {
        private String windowText = "";
        private int selectionStart;
        private int selectionEnd;

        /**
         * Java utility constructor.
         * not ported.
         */
        private State() {
        }
    }
}
