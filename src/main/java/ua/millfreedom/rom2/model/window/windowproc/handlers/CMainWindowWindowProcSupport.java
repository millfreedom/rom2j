package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.CString;
import ua.millfreedom.rom2.model.enums.MessageCodes;

/**
 * Native support extracted from CMainWindow::WindowProc @004852D8.
 */
public final class CMainWindowWindowProcSupport {

    /**
     * Java utility constructor.
     * not ported.
     */
    private CMainWindowWindowProcSupport() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8 message parameter reads.
     */
    public static int readMessageInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof MessageCodes messageCode) {
            return messageCode.id;
        }
        if (value instanceof Boolean bool) {
            return bool ? 1 : 0;
        }
        throw new IllegalArgumentException("Expected integer-like message payload but got " + value);
    }

    /**
     * Native support extracted from string payload reads in CMainWindow::WindowProc @004852D8.
     * Partial port. Accepts the recovered string-like message payloads used by the native dialog/list update paths.
     */
    static String readMessageText(Object value) {
        if (value instanceof CString string) {
            return string.toString();
        }
        if (value instanceof CharSequence text) {
            return text.toString();
        }
        throw new IllegalArgumentException("Expected string-like message payload but got " + value);
    }


}
