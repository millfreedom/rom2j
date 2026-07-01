package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;

/**
 * Native class: ScrollablePanelHeaderStaticTextVisualObject.
 * Purpose: scrollable-panel header text specialization.
 */
public class ScrollablePanelHeaderStaticTextVisualObject extends StaticTextVisualObject {
    public static final int NATIVE_SIZE = 0x7C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int STATE_ENABLED = 0x04;

    /**
     * Native: ScrollablePanelHeaderStaticTextVisualObject::ScrollablePanelHeaderStaticTextVisualObject @004E0690.
     * Fully ported.
     */
    public ScrollablePanelHeaderStaticTextVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, CBitmapFont bitmapFont, Palette16 textPalette, String name) {
        super(id, xLeft, yTop, xRight, yBottom, bitmapFont, textPalette, name);
    }

    /**
     * vtbl +0x2C: ScrollablePanelHeaderStaticTextVisualObject::Update @004E06E0.
     * Fully ported.
     */
    @Override
    public void update() {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        Globals.renderer.lockSurface();
        try {
            m_pParent.renderSelf(screenRect);
            drawFrameBorder(screenRect);
            Palette16 textPalette = checkStateFlag(STATE_ENABLED) == 0 ? Palettes.grayDim : Palettes.yellowish;
            bitmapFont.drawTextShadowed(
                    screenRect.left + 4,
                    screenRect.top + screenRect.height() / 2,
                    text,
                    TextAlign.VERTICAL_CENTER.mask,
                    textPalette,
                    1
            );
        } finally {
            Globals.renderer.unlockSurface();
        }
    }
}
