package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;

import static ua.millfreedom.rom2.model.enums.MessageCodes.DIALOG_OK;
import static ua.millfreedom.rom2.model.enums.MessageCodes.NOTIFY_DIALOG_CLOSED;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RETURN_TO_GAME;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_DOWN;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_LEFT;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_RIGHT;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_UP;

/**
 * Native class: HandlerVisualObject.
 * Purpose: handler-aware visual object with an extra virtual init slot (+0x78).
 */
public class HandlerVisualObject extends CVisualObject
        implements DialogResultCarrier {
    public static final int NATIVE_SIZE = 0x68; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    //0x5c
    public int activeFlag;
    //0x60
    public MessageCodes closeReasonMessage;
    //0x64
    public Object handler;

    /**
     * Native: HandlerVisualObject::HandlerVisualObject @004DBBB4.
     * Fully ported.
     */
    public HandlerVisualObject() {
        super();
        activeFlag = 0;
    }

    /**
     * Native: HandlerVisualObject::HandlerVisualObject @004DBBDD.
     * Fully ported.
     */
    public HandlerVisualObject(int id, CRect rect, Object handler) {
        super(id, rect, null);
        this.activeFlag = 0;
        this.handler = handler;
    }

    /**
     * Native: HandlerVisualObject::HandlerVisualObject @004DBC50.
     * Fully ported.
     */
    public HandlerVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, Object handler) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        this.activeFlag = 0;
        this.handler = handler;
    }

    /**
     * vtbl +0x78: HandlerVisualObject::Initialize @0044F320.
     * Fully ported.
     */
    public void initialize() {
    }

    /**
     * vtbl +0x7C: HandlerVisualObject::ReleaseResources @00437F70.
     * Fully ported.
     */
    public void releaseResources() {
    }

    /**
     * vtbl +0x2C: HandlerVisualObject::Update @004DBD20.
     * Fully ported.
     */
    @Override
    public void update() {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        renderSelf(screenRect);
        super.update();
    }

    /**
     * vtbl +0x30: HandlerVisualObject::RenderSelf @004DBD5F.
     * Fully ported.
     */
    @Override
    public void renderSelf(CRect clipRect) {
        clipRect.left = Math.max(Globals.screenRect.left, clipRect.left);
        clipRect.top = Math.max(Globals.screenRect.top, clipRect.top);
        clipRect.right = Math.min(Globals.screenRect.right, clipRect.right);
        clipRect.bottom = Math.min(Globals.screenRect.bottom, clipRect.bottom);

        Globals.renderer.pushClip(clipRect.left, clipRect.top, clipRect.right, clipRect.bottom);
        try {
            if (handler == null) {
                drawDefaultFrame();
                return;
            }

            ((HandlerFrameRenderer) handler).renderHandlerFrame(cRect.left, cRect.top, 0, 0, 0);
        } finally {
            Globals.renderer.popClip();
        }
    }

    /**
     * vtbl +0x48: HandlerVisualObject::OnMessage @004DC2B0.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        if (msg != DIALOG_OK && msg != RETURN_TO_GAME) {
            return super.onMessage(msg, wParam, lParam);
        }

        if (activeFlag != 0) {
            hideDialog(msg);
            Globals.mainWindow.postMessage(NOTIFY_DIALOG_CLOSED, this, 0);
        }
        return 1;
    }

    /**
     * vtbl +0x54: HandlerVisualObject::OnLButtonDown @004DC5CA.
     * Fully ported.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        if (previousMouseInputTarget == mouseInputTarget) {
            previousMouseInputTarget = null;
            CVisualObject hit = findDeepestChildAtPoint(x, y);
            if (hit != null && hit != this) {
                return hit.onLButtonDown(nFlags, x, y);
            }
        }
        return super.onLButtonDown(nFlags, x, y);
    }

    /**
     * vtbl +0x6C: HandlerVisualObject::OnKeyDown @004DC326.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        switch (nChar) {
            case 0x09 -> {
                cycleEnabledChild(!isShiftPressed(), true);
                return 1;
            }
            case VK_LEFT -> {
                if (keyboardInputTarget != null && keyboardInputTarget.leftNeighbor != null) {
                    switchEnabledChild(keyboardInputTarget.leftNeighbor, true);
                    return 1;
                }
            }
            case VK_UP -> {
                if (keyboardInputTarget != null && keyboardInputTarget.upNeighbor != null) {
                    switchEnabledChild(keyboardInputTarget.upNeighbor, true);
                    return 1;
                }
            }
            case VK_RIGHT -> {
                if (keyboardInputTarget != null && keyboardInputTarget.rightNeighbor != null) {
                    switchEnabledChild(keyboardInputTarget.rightNeighbor, true);
                    return 1;
                }
            }
            case VK_DOWN -> {
                if (keyboardInputTarget != null && keyboardInputTarget.downNeighbor != null) {
                    switchEnabledChild(keyboardInputTarget.downNeighbor, true);
                    return 1;
                }
            }
            default -> {
            }
        }
        return 0;
    }

    /**
     * vtbl +0x80: HandlerVisualObject::ShowDialog @004DC232.
     * Fully ported.
     */
    public void showDialog() {
        activeFlag = 1;
        setVisible(1);
        setEnabled(1);
        cycleEnabledChild(true, false);
    }

    /**
     * vtbl +0x84: HandlerVisualObject::HideDialog @004DC26D.
     * Fully ported.
     */
    public HandlerVisualObject hideDialog(MessageCodes reason) {
        if (activeFlag != 0) {
            activeFlag = 0;
            setVisible(0);
            setEnabled(0);
            closeReasonMessage = reason;
        }
        return this;
    }

    /**
     * Native: HandlerVisualObject::GetClosedReason @00437F80.
     * Fully ported.
     */
    @Override
    public MessageCodes getClosedReason() {
        return closeReasonMessage;
    }

    /**
     * Native support: keySHIFT read in HandlerVisualObject::OnKeyDown @004DC326.
     * Fully ported.
     */
    private static boolean isShiftPressed() {
        return Globals.shiftKeyDown;
    }

    /**
     * Native default frame path inside HandlerVisualObject::RenderSelf @004DBD5F.
     * Fully ported.
     */
    private void drawDefaultFrame() {
        CRect frameRect = new CRect();
        clientToScreen(frameRect, cRect);
        frameRect.right -= 8;
        frameRect.bottom -= 8;

        GUI.uiFrameSprite.drawAlpha(frameRect.right - 0x28, frameRect.top + 8, 3, 6, false);
        GUI.uiFrameSprite.drawAlpha(frameRect.left + 8, frameRect.bottom - 0x28, 6, 6, false);
        GUI.uiFrameSprite.drawAlpha(frameRect.right - 0x28, frameRect.bottom - 0x28, 8, 6, false);

        for (int i = 0; i < (frameRect.width() - 0x60) / 0x60; i++) {
            GUI.uiFrameSprite.drawAlpha(frameRect.left + 0x38 + i * 0x60, frameRect.bottom - 0x28, 7, 6, false);
        }
        for (int i = 0; i < ((frameRect.height() - 0x60) >> 6); i++) {
            GUI.uiFrameSprite.drawAlpha(frameRect.right - 0x28, frameRect.top + 0x38 + i * 0x40, 5, 6, false);
        }

        GUI.uiFrameSprite.draw(frameRect.left, frameRect.top, 1, 0, false);
        GUI.uiFrameSprite.draw(frameRect.right - 0x30, frameRect.top, 3, 0, false);
        GUI.uiFrameSprite.draw(frameRect.left, frameRect.bottom - 0x30, 6, 0, false);
        GUI.uiFrameSprite.draw(frameRect.right - 0x30, frameRect.bottom - 0x30, 8, 0, false);

        for (int i = 0; i < (frameRect.width() - 0x60) / 0x60; i++) {
            int x = frameRect.left + 0x30 + i * 0x60;
            GUI.uiFrameSprite.draw(x, frameRect.top, 2, 0, false);
            GUI.uiFrameSprite.draw(x, frameRect.bottom - 0x30, 7, 0, false);
        }
        for (int i = 0; i < ((frameRect.height() - 0x60) >> 6); i++) {
            int y = frameRect.top + 0x30 + i * 0x40;
            GUI.uiFrameSprite.draw(frameRect.left, y, 4, 0, false);
            GUI.uiFrameSprite.draw(frameRect.right - 0x30, y, 5, 0, false);
        }
        for (int x = 0; x < (frameRect.width() - 0x60) / 0x60; x++) {
            for (int y = 0; y < ((frameRect.height() - 0x60) >> 6); y++) {
                GUI.uiFrameSprite.draw(frameRect.left + 0x30 + x * 0x60, frameRect.top + 0x30 + y * 0x40, 0, 0, false);
            }
        }
    }
}
