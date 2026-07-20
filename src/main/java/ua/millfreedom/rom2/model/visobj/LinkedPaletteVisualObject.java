package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.color.RGB32;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CGameBitmap;
import ua.millfreedom.rom2.model.CRect;

/**
 * Native class: LinkedPaletteVisualObject.
 * Purpose: lightweight visual that renders through a linked drawable using a native frame selector.
 */
public class LinkedPaletteVisualObject extends CVisualObject {
    public static final int NATIVE_SIZE = 0x64; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    //0x5c
    public CGameBitmap linkedDrawable;
    //0x60
    public int frameSelector;

    /**
     * Native: LinkedPaletteVisualObject::LinkedPaletteVisualObject @004DB8F7.
     * Fully ported.
     */
    public LinkedPaletteVisualObject() {
        super();
        linkedDrawable = null;
    }

    /**
     * Native: LinkedPaletteVisualObject::LinkedPaletteVisualObject @004DB920.
     * Fully ported.
     */
    public LinkedPaletteVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            CGameBitmap linkedDrawable,
            int frameSelector
    ) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        this.linkedDrawable = linkedDrawable;
        this.frameSelector = frameSelector;
    }

    /**
     * Native: LinkedPaletteVisualObject::LinkedPaletteVisualObject @004DB969.
     * Fully ported.
     */
    public LinkedPaletteVisualObject(int id, CRect rect, CGameBitmap linkedDrawable, int frameSelector) {
        super(id, rect, null);
        this.linkedDrawable = linkedDrawable;
        this.frameSelector = frameSelector;
    }

    /**
     * vtbl +0x2C: LinkedPaletteVisualObject::Update @004DBA28.
     * Fully ported.
     */
    @Override
    public void update() {
        if (linkedDrawable == null) {
            return;
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        Globals.renderer.lockSurface();
        try {
            if (frameSelector == 1) {
                m_pParent.renderSelf(screenRect);
                CBmp64k bitmap = (CBmp64k) linkedDrawable;
                int srcBottom = bitmap.ySizeOf(0);
                int srcRight = bitmap.xSizeOf(0);
                bitmap.drawRectMasked(
                        screenRect.left,
                        screenRect.top,
                        0,
                        0,
                        srcRight,
                        srcBottom
                );
            } else if (frameSelector == 2) {
                m_pParent.renderSelf(screenRect);
                Globals.renderer.fillScreenRect(
                        screenRect.left + 8,
                        screenRect.top + 7,
                        screenRect.left + 0x50,
                        screenRect.top + 0x65,
                        RGB32.BLACK
                );
                CBmp64k bitmap = (CBmp64k) linkedDrawable;
                int srcBottom = bitmap.ySizeOf(0);
                int srcRight = bitmap.xSizeOf(0);
                bitmap.drawRectMasked(
                        screenRect.left,
                        screenRect.top,
                        0,
                        0,
                        srcRight,
                        srcBottom
                );
            } else if (frameSelector < 11) {
                linkedDrawable.draw(screenRect.left, screenRect.top, 0, 0, false);
            } else {
                linkedDrawable.draw(screenRect.left, screenRect.top, frameSelector - 10, 0, false);
            }
        } finally {
            Globals.renderer.unlockSurface();
        }
    }

    /**
     * Native: LinkedPaletteVisualObject::GetDrawable @004E0E00.
     * Fully ported.
     */
    public CGameBitmap getDrawable() {
        return linkedDrawable;
    }

    /**
     * Native: LinkedPaletteVisualObject::SetDrawable @004E0E20.
     * Fully ported.
     */
    public void setDrawable(CGameBitmap drawable) {
        linkedDrawable = drawable;
    }
}
