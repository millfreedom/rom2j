package ua.millfreedom.rom2.platform.glfw;

import org.lwjgl.glfw.GLFWCharCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.visobj.MessageTarget;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_0;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_9;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_CAPS_LOCK;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DELETE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_END;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F1;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F24;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_HOME;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_INSERT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_0;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_9;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ADD;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_DECIMAL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_DIVIDE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_MULTIPLY;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_SUBTRACT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_NUM_LOCK;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PAUSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PRINT_SCREEN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SCROLL_LOCK;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_TAB;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Z;
import static org.lwjgl.glfw.GLFW.GLFW_MOD_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_MOD_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_MOD_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_REPEAT;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_0;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_A;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_ADD;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_BACK;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_CAPITAL;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_DECIMAL;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_DELETE;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_DIVIDE;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_DOWN;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_END;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_ESCAPE;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_F1;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_HOME;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_INSERT;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_LCONTROL;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_LEFT;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_LMENU;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_LSHIFT;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_MULTIPLY;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_NEXT;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_NUMLOCK;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_PAUSE;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_NUMPAD0;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_PRIOR;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_RCONTROL;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_RETURN;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_RIGHT;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_RMENU;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_RSHIFT;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_SCROLL;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_SNAPSHOT;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_SPACE;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_SUBTRACT;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_TAB;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_UP;

/**
 * Bridges GLFW keyboard callbacks into Win32-style message delivery for any message target.
 * not ported.
 */
public final class GlfwKeyboardMessageAdapter {
    private final MessageTarget target;
    private final GLFWKeyCallbackI keyCallback;
    private final GLFWCharCallbackI charCallback;

    /**
     * Creates a keyboard-message adapter for a message target.
     * not ported.
     */
    public GlfwKeyboardMessageAdapter(MessageTarget target) {
        this.target = target;
        keyCallback = this::handleKey;
        charCallback = this::handleChar;
    }

    /**
     * Returns the GLFW key callback bound to this adapter.
     * not ported.
     */
    public GLFWKeyCallbackI keyCallback() {
        return keyCallback;
    }

    /**
     * Returns the GLFW char callback bound to this adapter.
     * not ported.
     */
    public GLFWCharCallbackI charCallback() {
        return charCallback;
    }

    /**
     * Converts GLFW key callbacks into Win32 `WM_KEYDOWN` and `WM_KEYUP` messages.
     * not ported.
     */
    private void handleKey(long window, int key, int scancode, int action, int mods) {
        if (Globals.blockingPlaybackActive) {
            if (action == GLFW_PRESS) {
                Globals.blockingPlaybackAbortRequested = true;
            }
            return;
        }

        int virtualKey = toVirtualKey(key);
        if (virtualKey == 0) {
            return;
        }
        updateModifierGlobals(virtualKey, action, mods);

        MessageCodes message = switch (action) {
            case GLFW_PRESS, GLFW_REPEAT -> (mods & GLFW_MOD_ALT) != 0
                    ? MessageCodes.WM_SYSKEYDOWN
                    : MessageCodes.WM_KEYDOWN;
            case GLFW_RELEASE -> MessageCodes.WM_KEYUP;
            default -> null;
        };
        if (message == null) {
            return;
        }

        int lParam = (mods & GLFW_MOD_ALT) != 0 ? 0x2000 : 0;
        int result = target == null ? 0 : target.onMessage(message, virtualKey, lParam);
//        if (virtualKey == VK_ESCAPE && action == GLFW_PRESS && result == 0) {
//            glfwSetWindowShouldClose(window, true);
//        }
    }

    /**
     * Converts GLFW text-input callbacks into Win32 `WM_CHAR` messages.
     * not ported.
     */
    private void handleChar(long window, int codepoint) {
        if (Globals.blockingPlaybackActive) {
            Globals.blockingPlaybackAbortRequested = true;
            return;
        }
        if (target == null) {
            return;
        }
        target.onMessage(MessageCodes.WM_CHAR, codepoint, 0);
    }

    /**
     * Maps GLFW key constants onto Win32 virtual-key constants consumed by recovered visual objects.
     * not ported.
     */
    private static int toVirtualKey(int key) {
        if (key >= GLFW_KEY_A && key <= GLFW_KEY_Z) {
            return VK_A + (key - GLFW_KEY_A);
        }
        if (key >= GLFW_KEY_0 && key <= GLFW_KEY_9) {
            return VK_0 + (key - GLFW_KEY_0);
        }
        if (key >= GLFW_KEY_F1 && key <= GLFW_KEY_F24) {
            return VK_F1 + (key - GLFW_KEY_F1);
        }
        if (key >= GLFW_KEY_KP_0 && key <= GLFW_KEY_KP_9) {
            return VK_NUMPAD0 + (key - GLFW_KEY_KP_0);
        }

        return switch (key) {
            case GLFW_KEY_BACKSPACE -> VK_BACK;
            case GLFW_KEY_TAB -> VK_TAB;
            case GLFW_KEY_ENTER, GLFW_KEY_KP_ENTER -> VK_RETURN;
            case GLFW_KEY_ESCAPE -> VK_ESCAPE;
            case GLFW_KEY_SPACE -> VK_SPACE;
            case GLFW_KEY_PAGE_UP -> VK_PRIOR;
            case GLFW_KEY_PAGE_DOWN -> VK_NEXT;
            case GLFW_KEY_END -> VK_END;
            case GLFW_KEY_HOME -> VK_HOME;
            case GLFW_KEY_LEFT -> VK_LEFT;
            case GLFW_KEY_UP -> VK_UP;
            case GLFW_KEY_RIGHT -> VK_RIGHT;
            case GLFW_KEY_DOWN -> VK_DOWN;
            case GLFW_KEY_INSERT -> VK_INSERT;
            case GLFW_KEY_DELETE -> VK_DELETE;
            case GLFW_KEY_LEFT_SHIFT -> VK_LSHIFT;
            case GLFW_KEY_RIGHT_SHIFT -> VK_RSHIFT;
            case GLFW_KEY_LEFT_CONTROL -> VK_LCONTROL;
            case GLFW_KEY_RIGHT_CONTROL -> VK_RCONTROL;
            case GLFW_KEY_LEFT_ALT -> VK_LMENU;
            case GLFW_KEY_RIGHT_ALT -> VK_RMENU;
            case GLFW_KEY_CAPS_LOCK -> VK_CAPITAL;
            case GLFW_KEY_PRINT_SCREEN -> VK_SNAPSHOT;
            case GLFW_KEY_PAUSE -> VK_PAUSE;
            case GLFW_KEY_NUM_LOCK -> VK_NUMLOCK;
            case GLFW_KEY_SCROLL_LOCK -> VK_SCROLL;
            case GLFW_KEY_KP_DECIMAL -> VK_DECIMAL;
            case GLFW_KEY_KP_DIVIDE -> VK_DIVIDE;
            case GLFW_KEY_KP_MULTIPLY -> VK_MULTIPLY;
            case GLFW_KEY_KP_SUBTRACT -> VK_SUBTRACT;
            case GLFW_KEY_KP_ADD -> VK_ADD;
            default -> 0;
        };
    }

    /**
     * Native support for keyCONTROL/keyALT/keySHIFT globals read by MapVisualObject::SelectMapCursor @0040B2B8.
     */
    private static void updateModifierGlobals(int virtualKey, int action, int mods) {
        Globals.controlKeyDown = (mods & GLFW_MOD_CONTROL) != 0;
        Globals.altKeyDown = (mods & GLFW_MOD_ALT) != 0;
        Globals.shiftKeyDown = (mods & GLFW_MOD_SHIFT) != 0;

        boolean pressed = action != GLFW_RELEASE;
        if (virtualKey == VK_LCONTROL || virtualKey == VK_RCONTROL) {
            Globals.controlKeyDown = pressed || Globals.controlKeyDown;
        } else if (virtualKey == VK_LMENU || virtualKey == VK_RMENU) {
            Globals.altKeyDown = pressed || Globals.altKeyDown;
        } else if (virtualKey == VK_LSHIFT || virtualKey == VK_RSHIFT) {
            Globals.shiftKeyDown = pressed || Globals.shiftKeyDown;
        } else if (virtualKey == VK_ESCAPE) {
            Globals.escapeKeyDown = pressed;
        }
    }
}
