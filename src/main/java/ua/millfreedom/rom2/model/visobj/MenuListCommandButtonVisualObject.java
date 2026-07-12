package ua.millfreedom.rom2.model.visobj;

import org.jetbrains.annotations.NotNull;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palette16;

import static ua.millfreedom.rom2.model.enums.MessageCodes.MENU_LIST_BUTTON_ACTION;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_RETURN;

/**
 * Native class: MenuListCommandButtonVisualObject.
 * Purpose: CommandButtonVisualObject specialization frequently used in menu item lists.
 */
public class MenuListCommandButtonVisualObject extends CommandButtonVisualObject {
    public static final int NATIVE_SIZE = 0x78; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!


    /**
     * Native: MenuListCommandButtonVisualObject::MenuListCommandButtonVisualObject @0044F330.
     * Fully ported.
     */
    public MenuListCommandButtonVisualObject(int id, String caption, @NotNull CBitmapFont bitmapFont, Palette16 hoverPalette, MessageCodes msg, int hotKey, String name) {
        super(id, 0, 0, 0, 0, caption, bitmapFont, hoverPalette, msg, hotKey, name);
    }

    /**
     * Native: MenuListCommandButtonVisualObject::MenuListCommandButtonVisualObject @0044F560.
     * Fully ported.
     */
    public MenuListCommandButtonVisualObject(int id, CRect rect, String caption, @NotNull CBitmapFont bitmapFont, Palette16 hoverPalette, MessageCodes msg, int hotKey, String name) {
        super(id, rect, caption, bitmapFont, hoverPalette, msg, hotKey, name);
    }

    /**
     * vtbl +0x2C: MenuListCommandButtonVisualObject::Update @004D9FC3.
     * Fully ported.
     */
    @Override
    public void update() {
        super.update();
    }

    /**
     * vtbl +0x4C: MenuListCommandButtonVisualObject::OnMouseMove @004D9FD6.
     * Fully ported.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        return super.onMouseMove(nFlags, x, y);
    }

    /**
     * vtbl +0x54: MenuListCommandButtonVisualObject::OnLButtonDown @004D9FF7.
     * Fully ported.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        return super.onLButtonDown(nFlags, x, y);
    }

    /**
     * vtbl +0x58: MenuListCommandButtonVisualObject::OnLButtonUp @004DA018.
     * Fully ported.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        if (pressedState == 0) {
            return 0;
        }

        setPressedState(false);
        if (screenRect.contains(x, y)) {
            postMenuListButtonAction();
        }
        return 1;
    }

    /**
     * vtbl +0x6C: MenuListCommandButtonVisualObject::OnKeyDown @004DA114.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        return 0;
    }

    /**
     * vtbl +0x74: MenuListCommandButtonVisualObject::OnChar @004DA098.
     * Fully ported.
     */
    @Override
    public int onChar(int nChar) {
        if (m_pParent == null || checkStateFlag(STATE_ACTIVE) == 0) {
            return 0;
        }

        int normalizedChar = Character.toLowerCase(nChar);
        if (normalizedChar != VK_RETURN && normalizedChar != hotKey) {
            return 0;
        }

        postMenuListButtonAction();
        return 1;
    }

    /**
     * Native owner: CWnd::PostMessage(mainWnd, MENU_LIST_BUTTON_ACTION, msg, 0) branches in MenuListCommandButtonVisualObject::OnLButtonUp @004DA018
     * and MenuListCommandButtonVisualObject::OnChar @004DA098.
     * System-boundary bridge. Java deliberately routes the recovered message tuple through `Globals.mainWindow.postMessage(...)`.
     */
    private void postMenuListButtonAction() {
        Globals.mainWindow.postMessage(MENU_LIST_BUTTON_ACTION, msg.id, 0);
    }
}
