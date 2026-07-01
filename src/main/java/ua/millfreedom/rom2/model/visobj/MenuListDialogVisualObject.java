package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;

import static ua.millfreedom.rom2.model.enums.MessageCodes.DIALOG_OK;
import static ua.millfreedom.rom2.model.enums.MessageCodes.MENU_LIST_BUTTON_ACTION;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RETURN_TO_GAME;
import static ua.millfreedom.rom2.model.enums.MessageCodes.WM_NULL;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_DOWN;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_UP;
import static ua.millfreedom.rom2.model.window.windowproc.handlers.CMainWindowWindowProcSupport.readMessageInt;

/**
 * Native class: MenuListDialogVisualObject.
 * Purpose: centered menu list dialog with optional handle and content rectangle.
 */
public class MenuListDialogVisualObject extends CenteredDialogVisualObject {
    public static final int NATIVE_SIZE = 0x7C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    //0x68
    public int field0x64;
    //0x6c
    public final CRect contentRect = new CRect();

    /**
     * Native: MenuListDialogVisualObject::MenuListDialogVisualObject @00450300.
     * Fully ported.
     */
    public MenuListDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, Object handler, int field0x64, CRect rect) {
        super(id, xLeft, yTop, xRight, yBottom, handler);
        contentRect.set(rect);
        this.field0x64 = field0x64;
        contentRect.left = 0x28;
        contentRect.right = cRect.width() - 0x30;
    }

    /**
     * vtbl +0x48: MenuListDialogVisualObject::OnMessage @00440008.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        int w = readMessageInt(wParam);
        if (msg != MENU_LIST_BUTTON_ACTION) {
            return super.onMessage(msg, wParam, lParam);
        }

        MessageCodes action = MessageCodes.fromId(w);
        if (action == WM_NULL) {
            return super.onMessage(RETURN_TO_GAME, 0, 0);
        }

        int result = super.onMessage(DIALOG_OK, 0, 0);
        Globals.mainWindow.postMessage(action, 0, 0);
        return result;
    }

    /**
     * vtbl +0x6C: MenuListDialogVisualObject::OnKeyDown @00440082.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        if (nChar == VK_UP) {
            cycleEnabledChild(false, true);
            return 1;
        }
        if (nChar == VK_DOWN) {
            cycleEnabledChild(true, true);
            return 1;
        }
        return super.onKeyDown(nChar);
    }

    /**
     * Native: MenuListDialogVisualObject::AppendItem @0043FFC5.
     * Fully ported.
     */
    protected final void appendItem(CVisualObject child, int height) {
        contentRect.top = contentRect.bottom;
        contentRect.bottom = contentRect.top + height;
        child.setBounds(contentRect);
        addChild(child);
    }
}
