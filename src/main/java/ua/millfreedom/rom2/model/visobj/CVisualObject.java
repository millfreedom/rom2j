package ua.millfreedom.rom2.model.visobj;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.window.DialogsMaskFlag;

import java.awt.*;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static ua.millfreedom.rom2.Utils.point;
import static ua.millfreedom.rom2.model.enums.MessageCodes.*;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.MODAL_DIALOG;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.SHOP_DIALOG;
import static ua.millfreedom.rom2.model.window.windowproc.handlers.CMainWindowWindowProcSupport.readMessageInt;

@Slf4j
public class CVisualObject implements MfcSerializable, MessageTarget {
    public static final int NATIVE_SIZE = 0x5C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int STATE_ENABLED = 0x04;
    private static final int STATE_VISIBLE = 0x08;
    private static final int STATE_HIDDEN = 0x20;

    //0x04
    public int id;
    //0x08
    public final CRect cRect = new CRect();
    //0x18
    public int m_nState;
    //0x1c
    public final List<CVisualObject> children = new ArrayList<>();
    //0x30
    public CVisualObject m_pParent;
    //0x34 Mouse and WM_USER input priority target selected by SetVisible.
    public CVisualObject mouseInputTarget;
    //0x38 Keyboard input priority target selected by SetEnabled.
    public CVisualObject keyboardInputTarget;
    //0x3c
    public String name;
    //0x40 Previous mouse input target restored when the current target is hidden.
    public CVisualObject previousMouseInputTarget;
    //0x44 Previous keyboard input target restored when the current target is disabled.
    public CVisualObject previousKeyboardInputTarget;
    //0x48
    public CVisualObject upNeighbor;
    //0x4c
    public CVisualObject downNeighbor;
    //0x50
    public CVisualObject leftNeighbor;
    //0x54
    public CVisualObject rightNeighbor;
    //0x58
    public CVisualObject gameDialogControls;

    /**
     * Native: CVisualObject::CVisualObject @004D34E0.
     * Fully ported.
     */
    public CVisualObject() {
        initCommon();
        id = -1;
        name = "";
        setBounds(0, 0, 0, 0);
    }

    /**
     * Native: CVisualObject::CVisualObject @004D35F0.
     * Fully ported.
     */
    public CVisualObject(int id, CRect rect, String name) {
        initCommon();
        this.id = id;
        if (name != null) {
            this.name = name;
        }
        setBounds(rect);
    }

    /**
     * Native: CVisualObject::CVisualObject @004D36FF.
     * Fully ported.
     */
    public CVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, String name) {
        initCommon();
        this.id = id;
        if (name != null) {
            this.name = name;
        }
        setBounds(xLeft, yTop, xRight, yBottom);
    }

    /**
     * Java lifecycle helper.
     * Native destructor semantics are intentionally not ported.
     */
    public void detachFromParent() {
        m_pParent = null;
    }

    // not ported.
    private void initCommon() {
        m_pParent = null;
        mouseInputTarget = null;
        keyboardInputTarget = null;
        previousMouseInputTarget = null;
        previousKeyboardInputTarget = null;
        m_nState = 1;
        name = "";
        upNeighbor = null;
        downNeighbor = null;
        leftNeighbor = null;
        rightNeighbor = null;
        gameDialogControls = null;
    }

    /**
     * Native: CVisualObject::SetBounds @004D38E0.
     * Fully ported.
     */
    public void setBounds(CRect rect) {
        cRect.set(rect);
    }

    /**
     * Native: CVisualObject::SetBounds @004D3918.
     * Fully ported.
     */
    public void setBounds(int xLeft, int yTop, int xRight, int yBottom) {
        cRect.set(xLeft, yTop, xRight, yBottom);
    }

    /**
     * Native: CVisualObject::GetRect @0041E770.
     * Fully ported.
     */
    public CRect getRect() {
        return cRect;
    }

    /**
     * Native: CVisualObject::AddChild @004D3A64.
     * Fully ported.
     */
    public void addChild(CVisualObject child) {
        children.add(child);
        child.m_pParent = this;
    }

    /**
     * Native: CVisualObject::ClearChildren @004D4074.
     * Fully ported.
     */
    public void clearChildren() {
        for (CVisualObject child : children) {
            child.m_pParent = null;
        }
        children.clear();
        mouseInputTarget = null;
        keyboardInputTarget = null;
        previousMouseInputTarget = null;
        previousKeyboardInputTarget = null;
    }

    /**
     * Native: CVisualObject::RemoveChild @004D3A89.
     * Fully ported.
     */
    public void removeChild(CVisualObject child) {
        int childIndex = -1;
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i) == child) {
                childIndex = i;
                break;
            }
        }
        if (childIndex < 0) {
            return;
        }
        children.remove(childIndex);

        child.m_pParent = null;
        if (mouseInputTarget == child) {
            mouseInputTarget = null;
        }
        if (keyboardInputTarget == child) {
            keyboardInputTarget = previousKeyboardInputTarget;
        }
        if (previousKeyboardInputTarget == child) {
            keyboardInputTarget = null;
        }
    }

    /**
     * Native: CVisualObject::RemoveAndDeleteChild @004D3BD0.
     * Fully ported for Java lifecycle: native deletes the removed object; Java detaches it.
     */
    public void removeAndDetachChild(CVisualObject child) {
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i) == child) {
                children.remove(i);
                if (mouseInputTarget == child) {
                    mouseInputTarget = null;
                }
                if (child == null) {
                    return;
                }
                child.m_pParent = null;
                return;
            }
        }
    }

    /**
     * Native: CVisualObject::RemoveAndDeleteChildById @004D3C6F.
     * Fully ported for Java lifecycle: native deletes the removed object; Java detaches it.
     */
    public void removeAndDetachChildById(int childId) {
        for (int i = 0; i < children.size(); i++) {
            CVisualObject child = children.get(i);
            if (child.id == childId) {
                child.m_pParent = null;
                if (mouseInputTarget == child) {
                    mouseInputTarget = null;
                }
                children.remove(i);
                return;
            }
        }
    }

    /**
     * Native: CVisualObject::FindNextChildIndexById @004D3D31.
     * Fully ported.
     */
    public int findNextChildIndexById(int childId) {
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).id == childId) {
                int nextIndex = i + 1;
                return nextIndex < children.size() ? nextIndex : 0;
            }
        }
        return -1;
    }

    /**
     * Native: CVisualObject::FindPreviousChildIndexById @004D3DA3.
     * Fully ported.
     */
    public int findPreviousChildIndexById(int childId) {
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).id == childId) {
                int previousIndex = i - 1;
                return previousIndex >= 0 ? previousIndex : children.size() - 1;
            }
        }
        return -1;
    }

    /**
     * Native: CVisualObject::GetChildById @004D4013.
     * Fully ported.
     */
    public CVisualObject getChildById(int childId) {
        for (int i = 0; i < children.size(); i++) {
            CVisualObject child = children.get(i);
            if (child.id == childId) {
                return child;
            }
        }
        return null;
    }

    /**
     * Native: CVisualObject::GetNextChildID @004E0D20.
     * Fully ported.
     */
    public int getNextChildID() {
        return children.size() + 1;
    }

    /**
     * Native: CVisualObject::HasSelectionPanelChild @0041AED6.
     * Fully ported.
     */
    public boolean hasSelectionPanelChild() {
        return getChildById(2) != null;
    }

    /**
     * Native: CVisualObject::HasSpellPanelChild @0041AEF1.
     * Fully ported.
     */
    public boolean hasSpellPanelChild() {
        if (SHOP_DIALOG.isUnsetIn(dialogModeFlags())) {
            return getChildById(3) != null;
        }

        CVisualObject inputController = Globals.mainWindow.getInputController();
        CVisualObject root = inputController.getChildById(1000);
        return root.getChildById(3) != null;
    }

    /**
     * Native: CVisualObject::CenterOnScreen @004DC4FC.
     * Fully ported.
     */
    public void centerOnScreen(int screenWidth, int screenHeight) {
        int width = cRect.width();
        int height = cRect.height();
        width = ((width - 8) / 0x60) * 0x60 + 8;
        height = ((height - 0x68 + (((height - 0x68) >> 31) & 0x3f)) >> 6) * 0x40 + 0x68;

        cRect.left = (screenWidth - width) >> 1;
        cRect.top = (screenHeight - height) >> 1;
        cRect.right = cRect.left + width;
        cRect.bottom = cRect.top + height;
    }

    /**
     * Native: CVisualObject::ClientToScreen @004D3993.
     * Fully ported.
     */
    public void clientToScreen(Point dest, Point source) {
        dest.x = source.x;
        dest.y = source.y;

        for (CVisualObject parent = m_pParent; parent != null; parent = parent.m_pParent) {
            dest.translate(parent.cRect.left, parent.cRect.top);
        }
    }

    /**
     * Native: CVisualObject::ClientToScreen @004D3A23.
     * Fully ported.
     */
    public void clientToScreen(CRect dest, CRect source) {
        Point topLeft = new Point(source.left, source.top);
        Point bottomRight = new Point(source.right, source.bottom);
        clientToScreen(topLeft, topLeft);
        clientToScreen(bottomRight, bottomRight);

        dest.set(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y);
    }

    /**
     * Native: CVisualObject::ScreenToClient @004D3940.
     * Fully ported.
     */
    public void screenToClient(Point dest, Point source) {
        dest.x = source.x;
        dest.y = source.y;

        for (CVisualObject parent = m_pParent; parent != null; parent = parent.m_pParent) {
            dest.translate(-parent.cRect.left, -parent.cRect.top);
        }
    }

    /**
     * Native: CVisualObject::ScreenToClient @004D39E2.
     * Fully ported.
     */
    public void screenToClient(CRect dest, CRect source) {
        Point topLeft = new Point(source.left, source.top);
        Point bottomRight = new Point(source.right, source.bottom);
        screenToClient(topLeft, topLeft);
        screenToClient(bottomRight, bottomRight);

        dest.set(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y);
    }

    /**
     * vtbl +0x00: CObject::GetRuntimeClass @005A1ED9.
     */
    public Class<?> getRuntimeClass() {
        return CVisualObject.class;
    }

    /**
     * vtbl +0x04: CVisualObject::scalar deleting destructor slot @004E0230.
     * not ported by policy. Java keeps only lightweight parent detachment semantics.
     */
    public void detachFromParentSlot(@SuppressWarnings("unused") int shouldFree) {
        m_pParent = null;
    }

    /**
     * vtbl +0x08: CObject::Serialize @00401970.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        // Native CObject::Serialize is a no-op.
    }

    /**
     * vtbl +0x0C: CObject::AssertValid @00401980.
     */
    public void assertValid() {
        // Native CObject::AssertValid is a no-op.
    }

    /**
     * vtbl +0x10: CVisualObject::Dump @004D4A53.
     * Fully ported for Java diagnostics: native writes the same class name into CDumpContext.
     */
    public String dump() {
        return "CVisualObject";
    }

    /**
     * vtbl +0x14: CVisualObject::GetText @004D43B5.
     * Fully ported.
     */
    public String getText() {
        return checkStateFlag(STATE_HIDDEN) == 0 ? name : null;
    }

    /**
     * vtbl +0x18: CVisualObject::SetText @004D4399.
     * Fully ported.
     */
    public void setText(String text) {
        name = text == null ? "" : text;
    }

    /**
     * vtbl +0x1C: CVisualObject::SetStateFlag @004D41A3.
     * Fully ported.
     */
    public void setStateFlag(int mask, int set) {
        if (set == 0) {
            m_nState &= ~mask;
        } else {
            m_nState |= mask;
        }
    }

    /**
     * vtbl +0x20: CVisualObject::CheckStateFlag @004D418D.
     * Fully ported.
     */
    public int checkStateFlag(int mask) {
        return m_nState & mask;
    }

    /**
     * vtbl +0x24: CVisualObject::SetVisible @004D41DA.
     * Fully ported.
     */
    public void setVisible(int bVisible) {
        setStateFlag(STATE_VISIBLE, bVisible);

        if (m_pParent == null) {
            return;
        }

        if (bVisible != 0) {
            m_pParent.previousMouseInputTarget = m_pParent.mouseInputTarget;
            m_pParent.mouseInputTarget = this;
        } else if (m_pParent.mouseInputTarget == this) {
            m_pParent.mouseInputTarget = m_pParent.previousMouseInputTarget;
        }
    }

    /**
     * vtbl +0x28: CVisualObject::SetEnabled @004D4249.
     * Fully ported.
     */
    public void setEnabled(int enabled) {
        setStateFlag(STATE_ENABLED, enabled);

        if (m_pParent == null) {
            return;
        }

        if (enabled != 0) {
            m_pParent.previousKeyboardInputTarget = m_pParent.keyboardInputTarget;
            m_pParent.keyboardInputTarget = this;
            if (gameDialogControls != null) {
                ((DialogWindowVisualObject) gameDialogControls).setEnabledVisuals(enabled);
            }
        } else {
            if (m_pParent.keyboardInputTarget == this) {
                m_pParent.keyboardInputTarget = m_pParent.previousKeyboardInputTarget;
            }
            if (gameDialogControls != null) {
                ((DialogWindowVisualObject) gameDialogControls).setEnabledVisuals(0);
            }
        }
    }

    /**
     * vtbl +0x2C: CVisualObject::Update @004D43E0.
     * Fully ported.
     */
    public void update() {
        int hidden = checkStateFlag(STATE_HIDDEN);
        if (hidden != 0) {
            return;
        }

        for (int i = 0; i < children.size(); i++) {
            CVisualObject child = children.get(i);
            if (child.checkStateFlag(STATE_HIDDEN) == 0) {
                child.update();
            }
        }
    }

    /**
     * vtbl +0x30: CVisualObject::RenderSelf @0041E790.
     * Fully ported.
     */
    public void renderSelf(CRect clipRect) {
        // Native no-op.
    }

    /**
     * vtbl +0x34: CVisualObject::Draw @004D4468.
     * Fully ported.
     */
    public void draw() {
        if (checkStateFlag(STATE_HIDDEN) == 0 && Globals.isWindowed == 0) {
            update();
            renderRect();
        }
    }

    /**
     * vtbl +0x38: CVisualObject::RenderRect @004D44A3.
     * Fully ported for Java's renderer model. Native computes this visual object's screen rectangle and forwards it to
     * the DirectDraw present-region helper. Java redraws the full GL target each tick and uses a hardware cursor, so
     * this virtual slot has no required side effect here.
     */
    public void renderRect() {
    }

    /**
     * Java support for dialog-mode checks that still need raw modal state.
     * not ported.
     */
    protected final int dialogModeFlags() {
        return Globals.mainWindow.dialogsMask;
    }

    /**
     * Java rendering support for visual layer checks. Modal dialogs shade the existing scene but do not hide gameplay
     * visual layers, so rendering checks ignore only the modal bit.
     * not ported.
     */
    protected final int visualDialogModeFlags() {
        return MODAL_DIALOG.excludeIn(dialogModeFlags());
    }

    /**
     * Java support for input and gameplay-mechanics gates that require plain, non-modal gameplay mode.
     * not ported.
     */
    protected final boolean isNonModalGameplayDialogMode() {
        return isGameplayDialogMode(dialogModeFlags());
    }

    /**
     * Java rendering support for visual gates that should continue through modal shading.
     * not ported.
     */
    protected final boolean isGameplayVisualDialogMode() {
        return isGameplayDialogMode(visualDialogModeFlags());
    }

    /**
     * Java support for checks against a caller-supplied dialog mode snapshot.
     * not ported.
     */
    protected final boolean isGameplayDialogMode(int modeFlags) {
        return DialogsMaskFlag.isExactly(modeFlags, GAMEPLAY);
    }

    /**
     * Native support extracted from full-screen FillScreenRect @00456348 call sites in
     * MainMenuVisualObject::ShowDialog @004A833D, CreditsDialogVisualObject::ShowDialog @0043BB76,
     * CharacterLoaderDialogVisualObject::ShowDialog @004319F7, CharacterGeneratorDialogVisualObject::ShowDialog @0042D8DC,
     * StartGameSetupDialogVisualObject::ShowDialog @004334DF, GlobalMapDialogVisualObject::ShowDialog @0046FA4B,
     * ShopDialogVisualObject::ShowDialog @004B8B98, BasicTownDialogVisualObject::showDialog @004CA7E2,
     * DruidTownDialogVisualObject::showDialog @004CE641, KaargTownDialogVisualObject::showDialog @004D0D46,
     * and FameHallDocumentDialogVisualObject own methods @004AAD03 / @004AB033.
     */
    protected void clearScreen() {
        Globals.renderer.clearSurface();
    }

    /**
     * Java rendering support for modal backdrop composition with caller-supplied brightness.
     * not ported.
     */
    public void shadeScreen(int brightness) {
        Globals.renderer.applyShadeToRect(
                Globals.screen.x(), Globals.screen.y(), Globals.screen.cx(), Globals.screen.cy(), brightness);
    }

    /**
     * vtbl +0x3C: CVisualObject::getValue @004D4539.
     * Fully ported. Native forwards a value cursor through child slot `0x3C` and advances it by child slot `0x40`.
     */
    public void getValue(Object value) {
        int byteOffset = 0;
        for (int i = 0; i < children.size(); i++) {
            CVisualObject child = children.get(i);
            child.getValue(valuePayloadAt(value, byteOffset));
            byteOffset += child.getValueRecursiveSize();
        }
    }

    /**
     * vtbl +0x40: CVisualObject::getValueRecursiveSize @004D44D7.
     * Fully ported. Native returns the recursive advance used by the base `0x3C/0x44` value walk.
     */
    public int getValueRecursiveSize() {
        int total = 0;
        for (int i = 0; i < children.size(); i++) {
            CVisualObject child = children.get(i);
            total += child.getValueRecursiveSize();
        }
        return total;
    }

    /**
     * vtbl +0x44: CVisualObject::setValue @004D45A2.
     * Fully ported. Native forwards a value cursor through child slot `0x44` and advances it by child slot `0x40`.
     */
    public void setValue(Object value) {
        int byteOffset = 0;
        for (int i = 0; i < children.size(); i++) {
            CVisualObject child = children.get(i);
            child.setValue(valuePayloadAt(value, byteOffset));
            byteOffset += child.getValueRecursiveSize();
        }
    }

    /**
     * Native support for CVisualObject value cursor advancement @004D4539 and @004D45A2.
     */
    private static Object valuePayloadAt(Object value, int byteOffset) {
        int slotIndex = byteOffset / Integer.BYTES;
        if (value instanceof Object[] slots) {
            return slots[slotIndex];
        }
        if (value instanceof int[] values) {
            IntBuffer buffer = IntBuffer.wrap(values);
            buffer.position(slotIndex);
            return buffer.slice();
        }
        return value;
    }

    /**
     * vtbl +0x48: CVisualObject::OnMessage @004D4769.
     * Fully ported, with Java-only `WM_MOUSEWHEEL` routing for GLFW input.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        //if (msg != WM_MOUSEMOVE && msg != RENDER_FRAME)
//        if (msg != WM_MOUSEMOVE && msg != WM_MOUSEWHEEL && msg != RENDER_FRAME && msg != STATIC_TEXT_CARET_BLINK_TICK && msg != INITIALIZE_UI)
//            log.info("{} - {}", msg, this);

        int modalResult = routeMessageToActiveModalChild(msg, wParam, lParam);
        if (modalResult != 0) {
            return modalResult;
        }

        int result;
        if (isMouseOrUserRoutingMessage(msg)) {
            if (mouseInputTarget == null) {
                result = dispatchToChildren(msg, wParam, lParam);
            } else {
                result = mouseInputTarget.onMessage(msg, wParam, lParam);
            }
        } else if (keyboardInputTarget != null && msg.isBetween(WM_KEYDOWN, WM_CHAR)) {
            result = keyboardInputTarget.onMessage(msg, wParam, lParam);
            if (result == 0) {
                result = dispatchToChildren(msg, wParam, lParam);
            }
        } else {
            result = dispatchToChildren(msg, wParam, lParam);
        }
        if (result != 0) return result;
        return switch (msg) {
            case WM_MOUSEMOVE -> onMouseMove(wParam, lParam);
            case WM_MOUSEWHEEL -> onMouseWheel(wParam, lParam);
            case WM_KEYDOWN -> onKeyDown(wParam);
            case WM_KEYUP -> onKeyUp(wParam);
            case WM_CHAR -> onChar(wParam);
            case WM_USER -> onUserMsg(wParam, lParam);
            case WM_LBUTTONDOWN -> onLButtonDown(wParam, lParam);
            case WM_LBUTTONUP -> onLButtonUp(wParam, lParam);
            case WM_LBUTTONDBLCLK -> onLButtonDblClk(wParam, lParam);
            case WM_RBUTTONDOWN -> onRButtonDown(wParam, lParam);
            case WM_RBUTTONUP -> onRButtonUp(wParam, lParam);
            case WM_RBUTTONDBLCLK -> onRButtonDblClk(wParam, lParam);
            default -> 0;
        };
    }

    /**
     * Java modal routing support. Active modal children consume every parent-layer message except render frames so
     * input and command messages cannot leak to visuals behind the modal.
     * not ported.
     */
    private int routeMessageToActiveModalChild(MessageCodes msg, Object wParam, Object lParam) {
        if (msg == RENDER_FRAME || MODAL_DIALOG.isUnsetIn(dialogModeFlags())) {
            return 0;
        }

        HandlerVisualObject activeModal = findActiveModalChild();
        if (activeModal == null) {
            return 0;
        }

        int result = activeModal.onMessage(msg, wParam, lParam);
        return result == 0 ? 1 : result;
    }

    /**
     * Native support extracted from CVisualObject::OnMessage @004D4769.
     */
    private int onMouseMove(Object wParam, Object lParam) {
        Point pt = point(readMessageInt(lParam));
        return onMouseMove(readMessageInt(wParam), pt.x, pt.y);
    }

    /**
     * vtbl +0x4C: CVisualObject::OnMouseMove @0041E7A0.
     * Fully ported.
     */
    public int onMouseMove(int nFlags, int x, int y) {
        return 0;
    }

    /**
     * Java extension for GLFW mouse-wheel message parameter unpacking.
     * not ported.
     */
    private int onMouseWheel(Object wParam, Object lParam) {
        Point pt = point(readMessageInt(lParam));
        return onMouseWheel(readMessageInt(wParam), pt.x, pt.y);
    }

    /**
     * Java extension for GLFW mouse-wheel messages.
     * not ported.
     */
    public int onMouseWheel(int nFlagsAndDelta, int x, int y) {
        return 0;
    }

    /**
     * Native support extracted from CVisualObject::OnMessage @004D4769.
     */
    private int onUserMsg(Object wParam, Object lParam) {
        Point pt = point(readMessageInt(lParam));
        return onUserMsg(readMessageInt(wParam), pt.x, pt.y);
    }

    /**
     * vtbl +0x50: CVisualObject::OnUserMsg @0041E7B0.
     * Fully ported.
     */
    public int onUserMsg(int nFlags, int x, int y) {
        return 0;
    }

    /**
     * Native support extracted from CVisualObject::OnMessage @004D4769.
     */
    private int onLButtonDown(Object wParam, Object lParam) {
        Point pt = point(readMessageInt(lParam));
        return onLButtonDown(readMessageInt(wParam), pt.x, pt.y);
    }

    /**
     * vtbl +0x54: CVisualObject::OnLButtonDown @00437F00.
     * Fully ported.
     */
    public int onLButtonDown(int nFlags, int x, int y) {
        return 0;
    }

    /**
     * Native support extracted from CVisualObject::OnMessage @004D4769.
     */
    private int onLButtonUp(Object wParam, Object lParam) {
        Point pt = point(readMessageInt(lParam));
        return onLButtonUp(readMessageInt(wParam), pt.x, pt.y);
    }

    /**
     * vtbl +0x58: CVisualObject::OnLButtonUp @00437F10.
     * Fully ported.
     */
    public int onLButtonUp(int nFlags, int x, int y) {
        return 0;
    }

    /**
     * Native support extracted from CVisualObject::OnMessage @004D4769.
     */
    private int onLButtonDblClk(Object wParam, Object lParam) {
        Point pt = point(readMessageInt(lParam));
        return onLButtonDblClk(readMessageInt(wParam), pt.x, pt.y);
    }

    /**
     * vtbl +0x5C: CVisualObject::OnLButtonDblClk @00437F20.
     * Fully ported.
     */
    public int onLButtonDblClk(int nFlags, int x, int y) {
        return 0;
    }

    /**
     * Native support extracted from CVisualObject::OnMessage @004D4769.
     */
    private int onRButtonDown(Object wParam, Object lParam) {
        Point pt = point(readMessageInt(lParam));
        return onRButtonDown(readMessageInt(wParam), pt.x, pt.y);
    }

    /**
     * vtbl +0x60: CVisualObject::OnRButtonDown @00437F30.
     * Fully ported.
     */
    public int onRButtonDown(int nFlags, int x, int y) {
        return 0;
    }

    /**
     * Native support extracted from CVisualObject::OnMessage @004D4769.
     */
    private int onRButtonUp(Object wParam, Object lParam) {
        Point pt = point(readMessageInt(lParam));
        return onRButtonUp(readMessageInt(wParam), pt.x, pt.y);
    }

    /**
     * vtbl +0x64: CVisualObject::OnRButtonUp @00437F40.
     * Fully ported.
     */
    public int onRButtonUp(int nFlags, int x, int y) {
        return 0;
    }

    /**
     * Native support extracted from CVisualObject::OnMessage @004D4769.
     */
    private int onRButtonDblClk(Object wParam, Object lParam) {
        Point pt = point(readMessageInt(lParam));
        return onRButtonDblClk(readMessageInt(wParam), pt.x, pt.y);
    }

    /**
     * vtbl +0x68: CVisualObject::OnRButtonDblClk @0041E7C0.
     * Fully ported.
     */
    public int onRButtonDblClk(int nFlags, int x, int y) {
        return 0;
    }

    /**
     * Native support extracted from CVisualObject::OnMessage @004D4769.
     */
    private int onKeyDown(Object wParam) {
        return onKeyDown(readMessageInt(wParam));
    }

    /**
     * vtbl +0x6C: CVisualObject::OnKeyDown @00437F50.
     * Fully ported.
     */
    public int onKeyDown(int nChar) {
        return 0;
    }

    /**
     * Native support extracted from CVisualObject::OnMessage @004D4769.
     */
    private int onKeyUp(Object wParam) {
        return onKeyUp(readMessageInt(wParam));
    }

    /**
     * vtbl +0x70: CVisualObject::OnKeyUp @0041E7D0.
     * Fully ported.
     */
    public int onKeyUp(int nChar) {
        return 0;
    }

    /**
     * Native support extracted from CVisualObject::OnMessage @004D4769.
     */
    private int onChar(Object wParam) {
        return onChar(readMessageInt(wParam));
    }

    /**
     * vtbl +0x74: CVisualObject::OnChar @00437F60.
     * Fully ported.
     */
    public int onChar(int nChar) {
        return 0;
    }

    /**
     * Native helper: CVisualObject::CycleEnabledChild @004D3E14.
     * Fully ported.
     */
    protected void cycleEnabledChild(boolean forward, boolean redraw) {
        CVisualObject currentKeyboardInputTarget = keyboardInputTarget;
        int step = forward ? 1 : -1;
        int currentIndex = children.indexOf(keyboardInputTarget);
        if (currentIndex < 0) {
            step = 1;
        }

        if (children.isEmpty()) {
            return;
        }

        int index = currentIndex;
        while (true) {
            index += step;
            if (index >= children.size()) {
                index = 0;
            }
            if (index < 0) {
                index = children.size() - 1;
            }

            CVisualObject candidate = children.get(index);
            int state = candidate.checkStateFlag(-1);
            if ((state & 0x1) == 0 || (state & 0x2) == 0) {
                continue;
            }

            if (currentKeyboardInputTarget != null) {
                currentKeyboardInputTarget.setEnabled(0);
                if (redraw) {
                    currentKeyboardInputTarget.draw();
                }
            }

            candidate.setEnabled(1);
            if (redraw) {
                candidate.draw();
            }
            return;
        }
    }

    /**
     * Native helper: CVisualObject::SwitchEnabledChild @004D3FB6.
     * Fully ported.
     */
    protected void switchEnabledChild(CVisualObject nextChild, boolean redraw) {
        CVisualObject currentKeyboardInputTarget = keyboardInputTarget;
        if (currentKeyboardInputTarget != null) {
            currentKeyboardInputTarget.setEnabled(0);
            if (redraw) {
                currentKeyboardInputTarget.draw();
            }
        }

        nextChild.setEnabled(1);
        if (redraw) {
            nextChild.draw();
        }
    }

    /**
     * Native helper: CVisualObject::FindDeepestChildAtPoint @004D42E8.
     * Fully ported.
     */
    public CVisualObject findDeepestChildAtPoint(int x, int y) {
        for (int i = children.size() - 1; i >= 0; i--) {
            CVisualObject child = children.get(i);
            CVisualObject hit = child.findDeepestChildAtPoint(x, y);
            if (hit != null) {
                return hit;
            }
        }

        Point point = new Point(x, y);
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        return screenRect.contains(point.x, point.y) ? this : null;
    }

    /**
     * Native: CVisualObject::LinkLeftNeighbor @004D4A6D.
     * Fully ported.
     */
    public void linkLeftNeighbor(CVisualObject leftNeighbor) {
        this.leftNeighbor = leftNeighbor;
        leftNeighbor.rightNeighbor = this;
    }

    /**
     * Native: CVisualObject::LinkRightNeighbor @004D4A8C.
     * Fully ported.
     */
    public void linkRightNeighbor(CVisualObject rightNeighbor) {
        this.rightNeighbor = rightNeighbor;
        rightNeighbor.leftNeighbor = this;
    }

    /**
     * Native: CVisualObject::LinkDownNeighbor @004D4AAB.
     * Fully ported.
     */
    public void linkDownNeighbor(CVisualObject downNeighbor) {
        this.downNeighbor = downNeighbor;
        downNeighbor.upNeighbor = this;
    }

    /**
     * Native: CVisualObject::LinkUpNeighbor @004D4ACA.
     * Fully ported.
     */
    public void linkUpNeighbor(CVisualObject upNeighbor) {
        this.upNeighbor = upNeighbor;
        upNeighbor.downNeighbor = this;
    }

    /**
     * Native: CVisualObject::SetGameDialogControls @004D4AE9.
     * Fully ported.
     */
    public void setGameDialogControls(CVisualObject controls) {
        gameDialogControls = controls;
    }

    /**
     * Java modal routing support for top-level dialog dispatch.
     * not ported.
     */
    private HandlerVisualObject findActiveModalChild() {
        for (int i = children.size() - 1; i >= 0; i--) {
            CVisualObject child = children.get(i);
            if (child.checkStateFlag(STATE_HIDDEN) == 0
                    && child instanceof HandlerVisualObject handlerVisualObject
                    && handlerVisualObject.activeFlag != 0) {
                return handlerVisualObject;
            }
        }
        return null;
    }

    /**
     * Native helper used by OnMessage: CVisualObject_dispatchToChildren @004D460B.
     * Fully ported, with Java-only `WM_MOUSEWHEEL` routing for GLFW input.
     */
    private int dispatchToChildren(MessageCodes msg, Object wParam, Object lParam) {
        int result = 0;
        boolean mouseOrUser = isMouseOrUserRoutingMessage(msg);

        for (int i = 0, childrenSize = children.size(); i < childrenSize; i++) {
            CVisualObject child = children.get(i);

            CRect childScreenRect = new CRect();
            child.clientToScreen(childScreenRect, child.cRect);
            if (!mouseOrUser || childScreenRect.contains(point(readMessageInt(lParam)))) {
                result = child.onMessage(msg, wParam, lParam);
                if (result != 0) {
                    return result;
                }
                if (mouseOrUser) {
                    return 0;
                }
            }
        }

        return result;
    }

    /**
     * Java support for CVisualObject mouse/user routing; extends the native mouse range with `WM_MOUSEWHEEL`.
     * not ported.
     */
    private static boolean isMouseOrUserRoutingMessage(MessageCodes msg) {
        return msg.isBetween(WM_MOUSEMOVE, WM_RBUTTONDBLCLK)
                || msg == WM_MOUSEWHEEL
                || msg == WM_USER;
    }

    private static AtomicInteger plv = new AtomicInteger(0);

    // not ported.
    @Override
    @SneakyThrows
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String pad = "\t".repeat(plv.get());
        sb.append(pad)
                .append(" (").append(this.getClass().getSimpleName()).append(')')
                .append(" id=").append("0x").append(Integer.toHexString(id))
                .append(" rect=").append(cRect)
                .append(" state=").append(m_nState);
        if (!this.children.isEmpty()) {
            sb.append(" children[").append(children.size()).append("]=");
            plv.incrementAndGet();
            for (int i = 0; i < children.size(); i++) {
                sb.append("\n")
                        .append("[").append(i).append("]=")
                        .append(children.get(i));
            }
            plv.decrementAndGet();
        }
        //+ ", name=" + this.name
        //+ "\n," + this.m_pParent
        return sb.toString();
//                "(" +  this.getClass().getSimpleName() + ")"
//                +"(id=" + this.id
//                + ", rect=" + this.cRect
//                + ", state=" + this.m_nState
//                //+ ", name=" + this.name
//                //+ "\n," + this.m_pParent
//                + (!this.children.isEmpty() ? ("\n\t," + this.children): "");
    }
}
