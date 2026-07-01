package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;

import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.palette.Palettes;

/**
 * Native class: HeaderDialogVariantVisualObject.
 * Purpose: header-dialog specialization used in message box flows.
 */
public class HeaderDialogVariantVisualObject extends HeaderDialogVisualObject {
    public static final int NATIVE_SIZE = 0x78; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    /**
     * Native: HeaderDialogVariantVisualObject::HeaderDialogVariantVisualObject @00444243.
     * Fully ported.
     */
    public HeaderDialogVariantVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, Object payload, String title, int buttonLayoutMode) {
        super(id, xLeft, yTop, xRight, yBottom, payload, title, buttonLayoutMode, null);
    }

    /**
     * vtbl +0x88: HeaderDialogVariantVisualObject::createDialogContent @00444286.
     * Fully ported.
     */
    @Override
    protected CVisualObject createDialogContent(Object payload, CRect contentRect) {
        CBitmapFont dialogFont = Globals.fonts.font1;
        WrappedTextSourceListVisualObject contentChild = new WrappedTextSourceListVisualObject(
                2,
                contentRect.left,
                contentRect.top,
                contentRect.right,
                contentRect.bottom,
                payload.toString(),
                dialogFont,
                Palettes.grayDim,
                0
        );
        addChild(contentChild);
        contentChild.configureWrappedTextSourceRows();
        return contentChild;
    }
}
