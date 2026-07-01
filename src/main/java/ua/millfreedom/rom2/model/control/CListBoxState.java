package ua.millfreedom.rom2.model.control;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Java sidecar for the CListBox port.
 * Java port status: support only.
 * not ported.
 */
final class CListBoxState {
    private static final Map<CListBox, State> STATE_BY_LIST_BOX = new IdentityHashMap<>();

    /**
     * Java utility constructor.
     * not ported.
     */
    private CListBoxState() {
    }

    /**
     * Java helper for the CListBox::AddString @004933A0 port.
     * Java port status: support only.
     * not ported.
     */
    static int addString(CListBox listBox, String entry) {
        State state = STATE_BY_LIST_BOX.computeIfAbsent(listBox, ignored -> new State());
        state.entries.add(entry);
        return state.entries.size() - 1;
    }

    /**
     * Java helper for the CListBox::SetCurSel @00493370 port.
     * Java port status: support only.
     * not ported.
     */
    static void setCurSel(CListBox listBox, int selectionIndex) {
        State state = STATE_BY_LIST_BOX.computeIfAbsent(listBox, ignored -> new State());
        state.currentSelectionIndex = selectionIndex;
    }

    /**
     * Java helper for the CListBox::GetCurSel @00493340 port.
     * Java port status: support only.
     * not ported.
     */
    static int getCurSel(CListBox listBox) {
        State state = STATE_BY_LIST_BOX.get(listBox);
        return state == null ? -1 : state.currentSelectionIndex;
    }

    /**
     * Java helper for the CListBox::GetText call in CMainWindow::kickSelectedPlayerCommand @00492A3A.
     * Java port status: support only.
     * not ported.
     */
    static String getText(CListBox listBox, int index) {
        return STATE_BY_LIST_BOX.get(listBox).entries.get(index);
    }

    /**
     * Java helper for the CListBox::ResetContent @004933D0 port.
     * Java port status: support only.
     * not ported.
     */
    static void resetContent(CListBox listBox) {
        STATE_BY_LIST_BOX.remove(listBox);
    }

    /**
     * Java helper for the CListBox::SelectString @00493400 port.
     * Java port status: support only.
     * not ported.
     */
    static int selectString(CListBox listBox, int startAfterIndex, String itemPrefix) {
        State state = STATE_BY_LIST_BOX.get(listBox);
        if (state == null || state.entries.isEmpty()) {
            return -1;
        }
        int entryCount = state.entries.size();
        int firstIndex = Math.floorMod(startAfterIndex + 1, entryCount);
        for (int offset = 0; offset < entryCount; offset++) {
            int index = (firstIndex + offset) % entryCount;
            if (state.entries.get(index).startsWith(itemPrefix)) {
                state.currentSelectionIndex = index;
                return index;
            }
        }
        return -1;
    }

    /**
     * Java sidecar state for the CListBox port.
     * Java port status: support only.
     * not ported.
     */
    private static final class State {
        private final List<String> entries = new ArrayList<>();
        private int currentSelectionIndex = -1;

        /**
         * Java utility constructor.
         * not ported.
         */
        private State() {
        }
    }
}
