package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;

/**
 * Native class: DialogWindowVisualObject.
 * Purpose: framed dialog label/header block used by many menu/settings screens.
 */
public class DialogWindowVisualObject extends CVisualObject {
    public static final int NATIVE_SIZE = 0x6C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    //0x5c
    public String caption;
    //0x60
    public CBitmapFont bitmapFont;
    //0x64
    public Palette16 fontPalette;
    //0x68
    public int textAlignFlags;

    /**
     * Native: DialogWindowVisualObject::DialogWindowVisualObject @004D4AFF.
     * Fully ported.
     */
    public DialogWindowVisualObject() {
        super();
        this.caption = "";
    }

    /**
     * Native: DialogWindowVisualObject::DialogWindowVisualObject @004D4B56.
     * Fully ported.
     */
    public DialogWindowVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            String caption,
            CBitmapFont bitmapFont,
            Palette16 fontPalette,
            int textAlignFlags
    ) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        this.caption = caption;
        this.bitmapFont = bitmapFont;
        this.fontPalette = fontPalette;
        this.textAlignFlags = textAlignFlags;
    }

    /**
     * Native: DialogWindowVisualObject::DialogWindowVisualObject @004D4BF3.
     * Fully ported.
     */
    public DialogWindowVisualObject(
            int id,
            CRect rect,
            String caption,
            CBitmapFont bitmapFont,
            Palette16 fontPalette,
            int textAlignFlags
    ) {
        super(id, rect, null);
        this.caption = caption;
        this.bitmapFont = bitmapFont;
        this.fontPalette = fontPalette;
        this.textAlignFlags = textAlignFlags;
    }

    /**
     * vtbl +0x2C: DialogWindowVisualObject::Update @004D4C84.
     * Fully ported.
     */
    @Override
    public void update() {
        if (m_pParent == null) {
            return;
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);

        Globals.renderer.lockSurface();
        try {
            m_pParent.renderSelf(screenRect);
            drawTextShadowed(
                    bitmapFont,
                    resolveAlignedScreenX(screenRect, textAlignFlags),
                    resolveAlignedScreenY(screenRect, textAlignFlags),
                    caption,
                    textAlignFlags,
                    fontPalette,
                    1
            );
        } finally {
            Globals.renderer.unlockSurface();
        }
    }

    /**
     * Native: DialogWindowVisualObject::SetEnabledVisuals @004D4D86.
     * Fully ported.
     */
    void setEnabledVisuals(int enabled) {
        fontPalette = enabled == 0 ? Palettes.grayDim : Palettes.yellowish;
        draw();
    }

    /**
     * Native helper branch inside DialogWindowVisualObject::Update @004D4C84.
     * Fully ported.
     */
    private static int resolveAlignedScreenX(CRect screenRect, int textAlignFlags) {
        if (TextAlign.RIGHT.matches(textAlignFlags)) {
            return screenRect.right;
        }
        if (TextAlign.CENTER.matches(textAlignFlags)) {
            return screenRect.left + (screenRect.width() / 2);
        }
        return screenRect.left;
    }

    /**
     * Native helper branch inside DialogWindowVisualObject::Update @004D4C84.
     * Fully ported.
     */
    private static int resolveAlignedScreenY(CRect screenRect, int textAlignFlags) {
        if (TextAlign.BOTTOM.matches(textAlignFlags)) {
            return screenRect.bottom;
        }
        if (TextAlign.VERTICAL_CENTER.matches(textAlignFlags)) {
            return screenRect.top + (screenRect.height() / 2);
        }
        return screenRect.top;
    }

    /**
     * Native owner: CBitmapFont::DrawTextShadowed @0045D536 call site in DialogWindowVisualObject::Update @004D4C84.
     * Fully ported.
     */
    private static void drawTextShadowed(
            CBitmapFont bitmapFont,
            int x,
            int y,
            String text,
            int textAlignFlags,
            Palette16 fontPalette,
            int shadowOffset
    ) {
        bitmapFont.drawTextShadowed(x, y, text, textAlignFlags, fontPalette, shadowOffset);
    }
}
