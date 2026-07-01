package ua.millfreedom.rom2.model.window;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.MessageCodes;

import java.util.ArrayDeque;

/**
 * Java support for the targetless global posted-message queue.
 */
public final class MessageSystem {
    // Java support, not a native field.
    private static final ArrayDeque<MessageEnvelope> postedMessages = new ArrayDeque<>();

    /**
     * Java utility constructor.
     * not ported.
     */
    private MessageSystem() {
    }

    /**
     * Native support extracted from CWnd::PostMessage @0041E3F0.
     * Java enqueues the targetless posted message in the global message system.
     */
    public static void post(MessageCodes msg, Object wParam, Object lParam) {
        postedMessages.addLast(new MessageEnvelope(msg, wParam, lParam));
    }

    /**
     * Native support extracted from the Win32 message-pump dispatch around CWnd::PostMessage @0041E3F0.
     * Java dispatches at most one posted message for one outer pump/frame cycle.
     */
    public static boolean pumpPostedMessage() {
        if (postedMessages.isEmpty()) {
            return false;
        }

        MessageEnvelope envelope = postedMessages.removeFirst();
        Globals.mainWindow.windowProc(envelope.messageCode(), envelope.wParam(), envelope.lParam());
        return true;
    }

    /**
     * Java support envelope for targetless main-window posted messages.
     *
     * @param messageCode Java support, not a native field.
     * @param wParam      Java support, not a native field.
     * @param lParam      Java support, not a native field.
     */
    private record MessageEnvelope(MessageCodes messageCode, Object wParam, Object lParam) {
        /**
         * Java support constructor for queued CWnd::PostMessage @0041E3F0 payloads.
         */
        private MessageEnvelope {
        }
    }
}
