package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.MessageCodes;

/**
 * Native class: CenteredDialogVisualObject.
 * Purpose: centered modal/dialog base derived from handler visual object.
 */
public class CenteredDialogVisualObject extends HandlerVisualObject {
    public static final int NATIVE_SIZE = 0x68; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    /**
     * Native: CenteredDialogVisualObject::CenteredDialogVisualObject @004DC48E.
     * Fully ported.
     */
    public CenteredDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, Object handler) {
        super(id, xLeft, yTop, xRight, yBottom, handler);
        centerOnScreen(Globals.screenRect.right, Globals.screenRect.bottom);
    }

    /**
     * Java rendering extension for live centered-modal backdrop composition.
     * not ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        if (msg == MessageCodes.RENDER_FRAME) {
            int result = super.onMessage(msg, wParam, lParam);
            shadeScreen(6);
            draw();
            return result;
        }
        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * vtbl +0x6C: CenteredDialogVisualObject::OnKeyDown @004DC595.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        if (nChar == 0x1B) {
            return onMessage(MessageCodes.RETURN_TO_GAME, 0, 0);
        }
        return super.onKeyDown(nChar);
    }
}
