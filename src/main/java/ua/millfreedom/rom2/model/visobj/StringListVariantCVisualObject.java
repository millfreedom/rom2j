package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;

/**
 * Native class: StringListVariantCVisualObject.
 * Purpose: StringListVariantA extension with per-row hover-highlighted labels.
 */
public class StringListVariantCVisualObject extends StringListVariantAVisualObject {
    public static final int NATIVE_SIZE = 0x8C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int STATE_ACTIVE = 0x01;
    private static final int HITBOX_PADDING = 4;
    private static final int RADIO_FRAME_CLEAR = 4;
    private static final int RADIO_FRAME_SELECTED = 5;
    private static final int RADIO_ALPHA_X_OFFSET = 5;
    private static final int RADIO_ALPHA_Y_OFFSET = 4;
    private static final int RADIO_X_OFFSET = 1;
    private static final int ROW_LABEL_GAP = 6;
    private static final int ROW_LABEL_Y_OFFSET = 3;
    private static final int TEXT_SHADOW_OFFSET = 1;
    private static final int RADIO_ALPHA_BRIGHTNESS = 4;
    private static final int DISABLED_SHADE_LEVEL = 3;

    /**
     * Native: StringListVariantCVisualObject::StringListVariantCVisualObject @0044F170.
     * Fully ported.
     */
    public StringListVariantCVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, CBitmapFont bitmapFont, Palette16 textPalette, String name) {
        super(id, xLeft, yTop, xRight, yBottom, bitmapFont, textPalette, name);
    }

    /**
     * vtbl +0x2C: StringListVariantCVisualObject::Update @004D7B31.
     * Fully ported.
     */
    @Override
    public void update() {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        screenRect.right += HITBOX_PADDING;
        screenRect.bottom += HITBOX_PADDING;

        int labelX = screenRect.left + getRadioButtonWidth() + ROW_LABEL_GAP;
        int rowTop = screenRect.top;
        int rowHeight = getVariantRadioButtonRowHeight();
        int mouseX = Globals.mousePointer.getX();
        int mouseY = Globals.mousePointer.getY();

        Globals.renderer.lockSurface();
        try {
            m_pParent.renderSelf(screenRect);
            screenRect.right -= HITBOX_PADDING;
            screenRect.bottom -= HITBOX_PADDING;

            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                drawRowToggle(screenRect.left, rowTop, isRowSelected(rowIndex));
                CRect rowRect = new CRect(screenRect.left, rowTop, screenRect.right, rowTop + rowHeight);
                listFontPalette = rowRect.contains(mouseX, mouseY) ? Palettes.brownish : Palettes.yellowish;
                drawChoiceLabel(labelX, rowTop + ROW_LABEL_Y_OFFSET, rowIndex);
                rowTop += rowHeight;
            }

            if (checkStateFlag(STATE_ACTIVE) == 0) {
                shadeRect(getInactiveShadeRect(), DISABLED_SHADE_LEVEL);
            }
        } finally {
            Globals.renderer.unlockSurface();
        }
    }

    /**
     * vtbl +0x78: StringListVariantCVisualObject::GetRowIndexAtScreenY @004D7D99.
     * Fully ported.
     */
    @Override
    protected int getRowIndexAtScreenY(int screenY) {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        return (screenY - screenRect.top) / getVariantRadioButtonRowHeight();
    }

    /**
     * Native support: row bit test inside StringListVariantCVisualObject::Update @004D7B31.
     */
    private boolean isRowSelected(int rowIndex) {
        int selectionMask = 1 << rowIndex;
        return (selectionValue & selectionMask) != 0;
    }

    /**
     * Native support: `g_Spr_RadioButtons::DrawAlpha/Draw` pair inside StringListVariantCVisualObject::Update @004D7B31.
     */
    private static void drawRowToggle(int x, int y, boolean selected) {
        int frameIndex = selected ? RADIO_FRAME_SELECTED : RADIO_FRAME_CLEAR;
        GUI.sprRadioButtons.drawAlpha(x + RADIO_ALPHA_X_OFFSET, y + RADIO_ALPHA_Y_OFFSET, frameIndex, RADIO_ALPHA_BRIGHTNESS, false);
        GUI.sprRadioButtons.draw(x + RADIO_X_OFFSET, y, frameIndex, 0, false);
    }

    /**
     * Native support: `g_Spr_RadioButtons->xSizeOf(4)` used by StringListVariantCVisualObject::Update @004D7B31.
     */
    private static int getRadioButtonWidth() {
        return GUI.sprRadioButtons.frames.get(RADIO_FRAME_CLEAR).xSize();
    }

    /**
     * Native support: `g_Spr_RadioButtons->ySizeOf(4)` used by StringListVariantCVisualObject::Update @004D7B31.
     */
    private static int getVariantRadioButtonRowHeight() {
        return GUI.sprRadioButtons.frames.get(RADIO_FRAME_CLEAR).ySize();
    }

    /**
     * Native support: `CStringArray::GetAt` + `CBitmapFont::DrawTextShadowed` call site in StringListVariantCVisualObject::Update @004D7B31.
     * Fully ported.
     */
    private void drawChoiceLabel(int x, int y, int rowIndex) {
        drawTextShadowed(listBitmapFont, x, y, rows.get(rowIndex), 0, listFontPalette, TEXT_SHADOW_OFFSET);
    }

    /**
     * Native owner: `CBitmapFont::DrawTextShadowed @0045D536` call site in StringListVariantCVisualObject::Update @004D7B31.
     * Fully ported.
     */
    private static void drawTextShadowed(
            CBitmapFont bitmapFont,
            int x,
            int y,
            String text,
            int textAlignFlags,
            Palette16 textPalette,
            int shadowOffset
    ) {
        bitmapFont.drawTextShadowed(x, y, text, textAlignFlags, textPalette, shadowOffset);
    }

    /**
     * Native support: inactive overlay bounds setup inside StringListVariantCVisualObject::Update @004D7B31.
     */
    private CRect getInactiveShadeRect() {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        screenRect.left -= 1;
        screenRect.top -= 1;
        screenRect.right += 1;
        screenRect.bottom += 1;
        return screenRect;
    }

}
