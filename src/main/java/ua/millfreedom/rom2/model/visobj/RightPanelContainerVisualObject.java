package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;

import static ua.millfreedom.rom2.model.enums.MessageCodes.WM_MOUSEMOVE;

public final class RightPanelContainerVisualObject extends CVisualObject {
    public static final int NATIVE_SIZE = 0x5C;

    /**
     * Native: RightPanelContainerVisualObject::RightPanelContainerVisualObject @004AB640.
     * Full port.
     */
    public RightPanelContainerVisualObject() {
        super();
    }

    /**
     * Native: RightPanelContainerVisualObject::RightPanelContainerVisualObject @004AB65F.
     * Full port.
     */
    public RightPanelContainerVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        super(id, xLeft, yTop, xRight, yBottom, null);
    }

    /**
     * Native: RightPanelContainerVisualObject::RightPanelContainerVisualObject @004AB696.
     * Full port.
     */
    public RightPanelContainerVisualObject(int id, CRect rect) {
        super(id, rect, null);
    }

    /**
     * vtbl +0x2C: RightPanelContainerVisualObject::Update @004AB6C1.
     * Full port.
     */
    @Override
    public void update() {
        super.update();
    }

    /**
     * vtbl +0x38 inherited renderRect slot.
     * not ported. Java extension draws scalable responsive-tier filler before child panels render.
     */
    @Override
    public void renderRect() {
        drawRightPanelFiller();
        super.renderRect();
    }

    /**
     * not ported. Java extension of native right-panel filler for arbitrary monitor heights.
     */
    private void drawRightPanelFiller() {
        if (!isGameplayVisualDialogMode()) {
            return;
        }
        RightPanelLayout rightPanelLayout = RightPanelLayout.forScreenHeight(cRect.height());
        CBmp64k fillBitmap;
        if (rightPanelLayout.usesHighResolutionArt()) {
            fillBitmap = rightPanelLayout.usesTallExtraFillArt() ? GUI.extraRight800 : GUI.extraRight1024;
        } else if (rightPanelLayout.usesMediumResolutionArt()) {
            fillBitmap = GUI.extraRight800;
        } else {
            return;
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        int tileHeight = fillBitmap.ySizeOf(0);
        int tileWidth = fillBitmap.xSizeOf(0);
        int fillBottom = rightPanelLayout.extraFillBottom();
        for (int fillTop = rightPanelLayout.extraFillTop; fillTop < fillBottom; fillTop += tileHeight) {
            int height = Math.min(tileHeight, fillBottom - fillTop);
            fillBitmap.drawRectMasked(
                    screenRect.left,
                    screenRect.top + fillTop,
                    0,
                    0,
                    tileWidth,
                    height
            );
        }
    }

    /**
     * vtbl +0x48: RightPanelContainerVisualObject::OnMessage @004AB6D4.
     * Full port.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        if (msg == WM_MOUSEMOVE && Globals.mousePointer.isSelecting()) {
            Globals.mousePointer.finishSelectionDrag();
        }
        return super.onMessage(msg, wParam, lParam);
    }
}
