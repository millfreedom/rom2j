package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.model.enums.MessageCodes;

/**
 * Receives Win32-style message tuples through the existing visual-object dispatch contract.
 * not ported.
 */
public interface MessageTarget {

    /**
     * Handles a Win32-style message tuple.
     * not ported.
     */
    int onMessage(MessageCodes msg, Object wParam, Object lParam);
}
