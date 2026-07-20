package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.palette.Palette16;

import static ua.millfreedom.rom2.model.enums.MessageCodes.TEXT_LIST_SELECTION_CHANGED;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_DOWN;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_UP;

/**
 * Native class: StringListVariantBVisualObject.
 * Purpose: alternate StringList specialization used by header dialogs.
 */
public class StringListVariantBVisualObject extends StringListVisualObject {
    public static final int NATIVE_SIZE = 0x8C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int STATE_ACTIVE = 0x01;
    private static final int STATE_ENABLED = 0x04;
    private static final int HITBOX_PADDING = 4;
    private static final int RADIO_FRAME_CLEAR = 0;
    private static final int RADIO_FRAME_SELECTED = 1;
    private static final int RADIO_ALPHA_X_OFFSET = 5;
    private static final int RADIO_ALPHA_Y_OFFSET = 4;
    private static final int RADIO_X_OFFSET = 1;
    private static final int ROW_LABEL_GAP = 6;
    private static final int ROW_LABEL_Y_OFFSET = 5;
    private static final int TEXT_SHADOW_OFFSET = 1;
    private static final int RADIO_ALPHA_BRIGHTNESS = 4;
    private static final int DISABLED_SHADE_LEVEL = 3;

    /**
     * Native: StringListVariantBVisualObject::StringListVariantBVisualObject @004D7DE5.
     * Fully ported.
     */
    public StringListVariantBVisualObject() {
        super();
    }

    /**
     * Native: StringListVariantBVisualObject::StringListVariantBVisualObject @0044EFE0.
     * Fully ported.
     */
    public StringListVariantBVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, CBitmapFont bitmapFont, Palette16 textPalette, String name) {
        super(id, xLeft, yTop, xRight, yBottom, bitmapFont, textPalette, name);
    }

    /**
     * vtbl +0x2C: StringListVariantBVisualObject::Update @004D7E04.
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

        Globals.renderer.lockSurface();
        try {
            m_pParent.renderSelf(screenRect);
            screenRect.right -= HITBOX_PADDING;
            screenRect.bottom -= HITBOX_PADDING;

            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                boolean selected = isRowSelected(rowIndex);
                drawRowToggle(screenRect.left, rowTop, selected);
                listFontPalette = selected && checkStateFlag(STATE_ENABLED) != 0
                        ? Palettes.yellowish
                        : Palettes.grayDim;
                drawChoiceLabel(labelX, rowTop + ROW_LABEL_Y_OFFSET, rowIndex);
                rowTop += getRadioButtonRowHeight();
            }

            if (checkStateFlag(STATE_ACTIVE) == 0) {
                shadeRect(screenRect, DISABLED_SHADE_LEVEL);
            }
        } finally {
            Globals.renderer.unlockSurface();
        }
    }

    /**
     * vtbl +0x4C: StringListVariantBVisualObject::OnMouseMove @004D8280.
     * Fully ported.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        return super.onMouseMove(nFlags, x, y);
    }

    /**
     * vtbl +0x54: StringListVariantBVisualObject::OnLButtonDown @004D806B.
     * Fully ported.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        if (m_pParent == null || checkStateFlag(STATE_ACTIVE) == 0) {
            return 0;
        }

        selectionValue = getRowIndexAtScreenY(y);
        selectionAuxValue = selectionValue;
        draw();
        m_pParent.onMessage(TEXT_LIST_SELECTION_CHANGED, id, selectionValue);
        return 1;
    }

    /**
     * vtbl +0x6C: StringListVariantBVisualObject::OnKeyDown @004D8174.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        if (nChar == VK_DOWN && checkStateFlag(STATE_ENABLED) != 0) {
            int updatedSelection = selectionValue + (selectionValue < rows.size() - 1 ? 1 : 0);
            if (updatedSelection != selectionValue) {
                selectionValue = updatedSelection;
            }
            selectionAuxValue = selectionValue;
            draw();
            return 1;
        }
        if (nChar == VK_UP && checkStateFlag(STATE_ENABLED) != 0) {
            int updatedSelection = selectionValue - (selectionValue > 0 ? 1 : 0);
            if (updatedSelection != selectionValue) {
                selectionValue = updatedSelection;
            }
            selectionAuxValue = selectionValue;
            draw();
            return 1;
        }
        return super.onKeyDown(nChar);
    }

    /**
     * vtbl +0x74: StringListVariantBVisualObject::OnChar @004D80F9.
     * Fully ported.
     */
    @Override
    public int onChar(int nChar) {
        if (nChar == ' ' && checkStateFlag(STATE_ENABLED) != 0) {
            selectionValue = selectionAuxValue;
            draw();
            m_pParent.onMessage(TEXT_LIST_SELECTION_CHANGED, id, selectionValue);
            return 1;
        }
        return super.onChar(nChar);
    }

    /**
     * Native support: row equality test inside StringListVariantBVisualObject::Update @004D7E04.
     */
    private boolean isRowSelected(int rowIndex) {
        return selectionValue == rowIndex;
    }

    /**
     * Native support: `g_Spr_RadioButtons::DrawAlpha/Draw` pair inside StringListVariantBVisualObject::Update @004D7E04.
     * Fully ported.
     */
    private static void drawRowToggle(int x, int y, boolean selected) {
        int frameIndex = selected ? RADIO_FRAME_SELECTED : RADIO_FRAME_CLEAR;
        GUI.sprRadioButtons.drawAlpha(x + RADIO_ALPHA_X_OFFSET, y + RADIO_ALPHA_Y_OFFSET, frameIndex, RADIO_ALPHA_BRIGHTNESS, false);
        GUI.sprRadioButtons.draw(x + RADIO_X_OFFSET, y, frameIndex, 0, false);
    }

    /**
     * Native support: `g_Spr_RadioButtons->xSizeOf(0)` used by StringListVariantBVisualObject::Update @004D7E04.
     * Fully ported.
     */
    private static int getRadioButtonWidth() {
        return GUI.sprRadioButtons.frames.get(0).width();
    }

    /**
     * Native support: `CStringArray::GetAt` + `CBitmapFont::DrawTextShadowed` call site in StringListVariantBVisualObject::Update @004D7E04.
     * Fully ported.
     */
    private void drawChoiceLabel(int x, int y, int rowIndex) {
        drawTextShadowed(listBitmapFont, x, y, rows.get(rowIndex), 0, listFontPalette, TEXT_SHADOW_OFFSET);
    }

    /**
     * Native owner: `CBitmapFont::DrawTextShadowed @0045D536` call site in StringListVariantBVisualObject::Update @004D7E04.
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
}
