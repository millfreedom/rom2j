package ua.millfreedom.rom2.platform.glfw;

import org.lwjgl.glfw.GLFWCursorEnterCallbackI;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;
import org.lwjgl.system.MemoryStack;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.render.PresentationSupport;
import ua.millfreedom.rom2.model.render.PresentationTransform;
import ua.millfreedom.rom2.model.visobj.MessageTarget;

import java.awt.Point;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwGetKey;
import static org.lwjgl.glfw.GLFW.glfwGetMouseButton;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;
import static org.lwjgl.system.MemoryStack.stackPush;

/**
 * Bridges GLFW mouse callbacks into Win32-style message delivery for any message target.
 * not ported.
 */
public final class GlfwMouseMessageAdapter {
    private static final int MK_LBUTTON = 0x0001;
    private static final int MK_RBUTTON = 0x0002;
    private static final int MK_SHIFT = 0x0004;
    private static final int MK_CONTROL = 0x0008;
    private static final int WHEEL_DELTA = 120;
    private static final long DOUBLE_CLICK_INTERVAL_NANOS = 500_000_000L;
    private static final int DOUBLE_CLICK_MAX_DISTANCE = 4;

    private final MessageTarget target;
    private final GLFWCursorPosCallbackI cursorPosCallback;
    private final GLFWMouseButtonCallbackI mouseButtonCallback;
    private final GLFWCursorEnterCallbackI cursorEnterCallback;
    private final GLFWScrollCallbackI scrollCallback;
    private int lastClickButton = -1;
    private int lastClickX;
    private int lastClickY;
    private long lastClickNanos;
    private double lastWindowCursorX;
    private double lastWindowCursorY;
    private boolean hasLastWindowCursorPoint;
    private boolean cursorInsideWindow = true;

    /**
     * Creates a mouse-message adapter for a message target.
     * not ported.
     */
    public GlfwMouseMessageAdapter(MessageTarget target) {
        this.target = target;
        cursorPosCallback = this::handleCursorPos;
        mouseButtonCallback = this::handleMouseButton;
        cursorEnterCallback = this::handleCursorEnter;
        scrollCallback = this::handleScroll;
    }

    /**
     * Returns the GLFW cursor-position callback bound to this adapter.
     * not ported.
     */
    public GLFWCursorPosCallbackI cursorPosCallback() {
        return cursorPosCallback;
    }

    /**
     * Returns the GLFW mouse-button callback bound to this adapter.
     * not ported.
     */
    public GLFWMouseButtonCallbackI mouseButtonCallback() {
        return mouseButtonCallback;
    }

    /**
     * Returns the GLFW cursor-enter callback bound to this adapter.
     * not ported.
     */
    public GLFWCursorEnterCallbackI cursorEnterCallback() {
        return cursorEnterCallback;
    }

    /**
     * Returns the GLFW scroll callback bound to this adapter.
     * not ported.
     */
    public GLFWScrollCallbackI scrollCallback() {
        return scrollCallback;
    }

    /**
     * Converts GLFW cursor positions into logical client coordinates and forwards them as `WM_MOUSEMOVE`.
     * not ported.
     */
    private void handleCursorPos(long window, double xpos, double ypos) {
        if (cursorInsideWindow) {
            rememberWindowCursorPoint(xpos, ypos);
        }
        Point point = mapWindowCursorPoint(window, xpos, ypos);
        if (target == null) {
            return;
        }
        if (Globals.blockingPlaybackActive) {
            return;
        }

        int clientX = point.x;
        int clientY = point.y;
        int flags = getMouseFlags(window);
        if ((flags & MK_RBUTTON) == 0) {
            updateMousePointerPosition(clientX, clientY);
        }
        int handled = target.onMessage(MessageCodes.WM_MOUSEMOVE, flags, packMousePointToLParam(clientX, clientY));
        if (handled != 0 && (flags & MK_RBUTTON) != 0
                && (clientX != Globals.mousePointer.getX() || clientY != Globals.mousePointer.getY())) {
            restorePlatformCursorToMousePointer(window);
        }
    }

    /**
     * Converts GLFW mouse-button callbacks into matching Win32 button messages at the current logical cursor position.
     * not ported.
     */
    private void handleMouseButton(long window, int button, int action, int mods) {
        if (target == null) {
            return;
        }
        if (Globals.blockingPlaybackActive) {
            if (action == GLFW_PRESS && isHandledButton(button)) {
                Globals.blockingPlaybackAbortRequested = true;
            }
            return;
        }

        if ((action != GLFW_PRESS && action != GLFW_RELEASE) || !isHandledButton(button)) {
            return;
        }

        Point point = getCurrentLogicalCursorPoint(window);
        updateMousePointerPosition(point.x, point.y);

        MessageCodes message = getButtonMessage(button, action, point);
        if (message == null) {
            return;
        }

        int flags = getMouseFlags(window);
        if (action == GLFW_PRESS) {
            flags |= getButtonMask(button);
        } else if (action == GLFW_RELEASE) {
            flags &= ~getButtonMask(button);
        }
        target.onMessage(message, flags, packMousePointToLParam(point.x, point.y));
    }

    /**
     * Converts GLFW scroll callbacks into Win32-style `WM_MOUSEWHEEL` messages at the current cursor position.
     * not ported.
     */
    private void handleScroll(long window, @SuppressWarnings("unused") double xoffset, double yoffset) {
        if (target == null) {
            return;
        }
        if (Globals.blockingPlaybackActive) {
            return;
        }

        int wheelDelta = (int) Math.round(yoffset * WHEEL_DELTA);
        if (wheelDelta == 0) {
            return;
        }

        Point point = getCurrentLogicalCursorPoint(window);
        updateMousePointerPosition(point.x, point.y);
        int wParam = packMouseWheelWParam(getMouseFlags(window), wheelDelta);
        target.onMessage(MessageCodes.WM_MOUSEWHEEL, wParam, packMousePointToLParam(point.x, point.y));
    }

    /**
     * Tracks cursor-window enter state, keeping the platform cursor inside the GLFW window when it tries to leave.
     * not ported.
     */
    private void handleCursorEnter(long window, boolean entered) {
        if (!entered) {
            cursorInsideWindow = false;
            returnPlatformCursorToLastWindowPoint(window);
            return;
        }
        cursorInsideWindow = true;
        if (target == null) {
            return;
        }
        if (Globals.blockingPlaybackActive) {
            return;
        }

        Point point = getCurrentLogicalCursorPoint(window);
        updateMousePointerPosition(point.x, point.y);
        int flags = getMouseFlags(window);
        target.onMessage(MessageCodes.WM_MOUSEMOVE, flags, packMousePointToLParam(point.x, point.y));
    }

    /**
     * Remembers the last raw GLFW cursor position reported while the pointer is still in the window.
     * not ported.
     */
    private void rememberWindowCursorPoint(double xpos, double ypos) {
        lastWindowCursorX = xpos;
        lastWindowCursorY = ypos;
        hasLastWindowCursorPoint = true;
    }

    /**
     * Java GLFW boundary support: on cursor leave, put the platform cursor back where it last was inside the window.
     * not ported.
     */
    private void returnPlatformCursorToLastWindowPoint(long window) {
        if (hasLastWindowCursorPoint) {
            glfwSetCursorPos(window, lastWindowCursorX, lastWindowCursorY);
        }
    }

    /**
     * Reads the current GLFW content-area size and derives the active Java presentation transform.
     * not ported.
     */
    private static PresentationTransform getWindowPresentationTransform(long window) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            glfwGetWindowSize(window, width, height);
            return PresentationSupport.currentTransform(width.get(0), height.get(0));
        }
    }

    /**
     * Reads the current GLFW cursor position and converts it into logical client coordinates.
     * not ported.
     */
    private static Point getCurrentLogicalCursorPoint(long window) {
        try (MemoryStack stack = stackPush()) {
            DoubleBuffer xpos = stack.mallocDouble(1);
            DoubleBuffer ypos = stack.mallocDouble(1);
            glfwGetCursorPos(window, xpos, ypos);
            return mapWindowCursorPoint(window, xpos.get(0), ypos.get(0));
        }
    }

    /**
     * Native support extracted from the cursor-position write before Win32 mouse message fan-out in
     * CMainWindow::WindowProc @004852D8.
     */
    private static void updateMousePointerPosition(int x, int y) {
        Globals.mousePointer.setPosition(x, y);
    }

    /**
     * Native support extracted from the `SetCursorPos(GetX(g_CMousePointer), GetY(g_CMousePointer))` branch after a
     * handled `WM_MOUSEMOVE` in CMainWindow::WindowProc @004852D8.
     */
    private static void restorePlatformCursorToMousePointer(long window) {
        PresentationTransform transform = getWindowPresentationTransform(window);
        glfwSetCursorPos(
                window,
                transform.drawX() + (Globals.mousePointer.getX() - transform.sourceLeft()) * transform.scaleX(),
                transform.drawY() + (Globals.mousePointer.getY() - transform.sourceTop()) * transform.scaleY()
        );
    }

    /**
     * Java platform support for translating GLFW window coordinates into logical game coordinates.
     * not ported.
     */
    private static Point mapWindowCursorPoint(long window, double xpos, double ypos) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            glfwGetWindowSize(window, width, height);
            int windowWidth = width.get(0);
            int windowHeight = height.get(0);
            if (windowWidth <= 0 || windowHeight <= 0) {
                return new Point(0, 0);
            }

            PresentationTransform transform = PresentationSupport.currentTransform(windowWidth, windowHeight);
            return new Point(transform.mapX(xpos), transform.mapY(ypos));
        }
    }

    /**
     * Builds the Win32 mouse-key-state bitmask expected in mouse-message `wParam` low words.
     * not ported.
     */
    private static int getMouseFlags(long window) {
        int flags = 0;
        if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS) {
            flags |= MK_LBUTTON;
        }
        if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS) {
            flags |= MK_RBUTTON;
        }
        if (isPressed(window, GLFW_KEY_LEFT_SHIFT) || isPressed(window, GLFW_KEY_RIGHT_SHIFT)) {
            flags |= MK_SHIFT;
        }
        if (isPressed(window, GLFW_KEY_LEFT_CONTROL) || isPressed(window, GLFW_KEY_RIGHT_CONTROL)) {
            flags |= MK_CONTROL;
        }
        return flags;
    }

    /**
     * Reads a GLFW key state as a boolean.
     * not ported.
     */
    private static boolean isPressed(long window, int key) {
        return glfwGetKey(window, key) == GLFW_PRESS;
    }

    /**
     * Maps a GLFW mouse button/action pair onto the matching Win32 message code.
     * not ported.
     */
    private MessageCodes getButtonMessage(int button, int action, Point point) {
        if (action != GLFW_PRESS && action != GLFW_RELEASE) {
            return null;
        }

        return switch (button) {
            case GLFW_MOUSE_BUTTON_LEFT -> action == GLFW_PRESS
                    ? resolvePressMessage(button, point, MessageCodes.WM_LBUTTONDOWN, MessageCodes.WM_LBUTTONDBLCLK)
                    : MessageCodes.WM_LBUTTONUP;
            case GLFW_MOUSE_BUTTON_RIGHT -> action == GLFW_PRESS
                    ? resolvePressMessage(button, point, MessageCodes.WM_RBUTTONDOWN, MessageCodes.WM_RBUTTONDBLCLK)
                    : MessageCodes.WM_RBUTTONUP;
            default -> null;
        };
    }

    /**
     * Java GLFW bridge button filter used before logical cursor conversion.
     * not ported.
     */
    private static boolean isHandledButton(int button) {
        return button == GLFW_MOUSE_BUTTON_LEFT || button == GLFW_MOUSE_BUTTON_RIGHT;
    }

    /**
     * Java GLFW bridge for Win32 double-click message synthesis.
     * not ported.
     */
    private MessageCodes resolvePressMessage(
            int button,
            Point point,
            MessageCodes downMessage,
            MessageCodes doubleClickMessage
    ) {
        long now = System.nanoTime();
        boolean doubleClick = button == lastClickButton
                && now - lastClickNanos <= DOUBLE_CLICK_INTERVAL_NANOS
                && Math.abs(point.x - lastClickX) <= DOUBLE_CLICK_MAX_DISTANCE
                && Math.abs(point.y - lastClickY) <= DOUBLE_CLICK_MAX_DISTANCE;
        lastClickButton = doubleClick ? -1 : button;
        lastClickX = point.x;
        lastClickY = point.y;
        lastClickNanos = now;
        return doubleClick ? doubleClickMessage : downMessage;
    }

    /**
     * Returns the Win32 `MK_*` bit corresponding to a GLFW mouse button.
     * not ported.
     */
    private static int getButtonMask(int button) {
        return switch (button) {
            case GLFW_MOUSE_BUTTON_LEFT -> MK_LBUTTON;
            case GLFW_MOUSE_BUTTON_RIGHT -> MK_RBUTTON;
            default -> 0;
        };
    }

    /**
     * Packs mouse key flags and wheel delta into Win32-style `WM_MOUSEWHEEL::wParam`.
     * not ported.
     */
    private static int packMouseWheelWParam(int flags, int wheelDelta) {
        return (flags & 0xFFFF) | (wheelDelta << 16);
    }

    /**
     * Packs client-space mouse coordinates into a Win32-style `lParam`.
     * not ported.
     */
    private static int packMousePointToLParam(int x, int y) {
        return (x & 0xFFFF) | ((y & 0xFFFF) << 16);
    }
}
