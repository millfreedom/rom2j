package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;

import java.nio.IntBuffer;

import static ua.millfreedom.rom2.model.enums.MessageCodes.TEXT_LIST_SELECTION_CHANGED;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_DOWN;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_UP;

/**
 * Native class: StringListVariantAVisualObject.
 * Purpose: string-list toggle control with radio-button sprites and bitmask-backed selection state.
 */
public class StringListVariantAVisualObject extends StringListVisualObject {
    public static final int NATIVE_SIZE = 0x8C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int STATE_ACTIVE = 0x01;
    private static final int STATE_ENABLED = 0x04;
    private static final int STATE_VISIBLE = 0x08;
    private static final int HITBOX_PADDING = 4;
    private static final int TOGGLE_FRAME_CLEAR = 2;
    private static final int TOGGLE_FRAME_SELECTED = 3;
    private static final int TOGGLE_ALPHA_X_OFFSET = 5;
    private static final int TOGGLE_ALPHA_Y_OFFSET = 4;
    private static final int TOGGLE_X_OFFSET = 1;
    private static final int ROW_LABEL_GAP = 6;
    private static final int ROW_LABEL_Y_OFFSET = 5;
    private static final int TEXT_SHADOW_OFFSET = 1;
    private static final int TOGGLE_ALPHA_BRIGHTNESS = 4;
    private static final int DISABLED_SHADE_LEVEL = 3;

    /**
     * Native: StringListVariantAVisualObject::StringListVariantAVisualObject @004D755D.
     * Fully ported.
     */
    public StringListVariantAVisualObject() {
        super();
    }

    /**
     * Native: StringListVariantAVisualObject::StringListVariantAVisualObject @0044EE70.
     * Fully ported.
     */
    public StringListVariantAVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, CBitmapFont bitmapFont, Palette16 textPalette, String name) {
        super(id, xLeft, yTop, xRight, yBottom, bitmapFont, textPalette, name);
    }

    /**
     * vtbl +0x2C: StringListVariantAVisualObject::Update @004D757C.
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
                drawRowToggle(screenRect.left, rowTop, isRowSelected(rowIndex));
                listFontPalette = checkStateFlag(STATE_ENABLED) != 0 ? Palettes.yellowish : Palettes.grayDim;
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
     * vtbl +0x44: StringListVariantAVisualObject::setValue @0044EEF0.
     * Fully ported. Native reads the incoming toggle bitmask through one pointer-sized payload; Java models the
     * recovered `int *`, base-walker `IntBuffer`, and direct-value helpers.
     */
    @Override
    public void setValue(Object payload) {
        if (payload instanceof Number value) {
            setSelectionValue(value.intValue());
            return;
        }
        if (payload instanceof int[] values && values.length > 0) {
            setSelectionValue(values[0]);
            return;
        }
        if (payload instanceof IntBuffer values) {
            setSelectionValue(values.get(0));
            return;
        }
        throw new IllegalArgumentException("StringListVariantAVisualObject slot 0x44 expects Number, int[], or IntBuffer payload");
    }

    /**
     * Native support: typed Java adapter for StringListVariantAVisualObject slot `0x44` @0044EEF0.
     */
    public void setSelectionValue(int value) {
        selectionValue = value;
    }

    /**
     * vtbl +0x4C: StringListVariantAVisualObject::OnMouseMove @004D78B8.
     * Fully ported.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        if (m_pParent != null && checkStateFlag(STATE_ACTIVE) != 0) {
            if (checkStateFlag(STATE_ENABLED) == 0) {
                m_pParent.switchEnabledChild(this, true);
            }

            CRect screenRect = new CRect();
            clientToScreen(screenRect, cRect);
            screenRect.right += HITBOX_PADDING;
            screenRect.bottom += HITBOX_PADDING;
            if (!screenRect.contains(x, y)) {
                setVisible(0);
            } else if (checkStateFlag(STATE_VISIBLE) == 0) {
                setVisible(1);
            }
            draw();
        }
        return 0;
    }

    /**
     * vtbl +0x54: StringListVariantAVisualObject::OnLButtonDown @004D77E6.
     * Fully ported.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        if (m_pParent == null || checkStateFlag(STATE_ACTIVE) == 0) {
            return 0;
        }

        int rowIndex = getRowIndexAtScreenY(y);
        int selectionMask = 1 << rowIndex;
        if ((selectionValue & selectionMask) == 0) {
            selectionValue |= selectionMask;
        } else {
            selectionValue &= ~selectionMask;
        }
        selectionAuxValue = rowIndex;
        draw();
        m_pParent.onMessage(TEXT_LIST_SELECTION_CHANGED, id, selectionValue);
        return 1;
    }

    /**
     * vtbl +0x5C: StringListVariantAVisualObject::OnLButtonDblClk @0044EF10.
     * Fully ported.
     */
    @Override
    public int onLButtonDblClk(int nFlags, int x, int y) {
        return onLButtonDown(nFlags, x, y);
    }

    /**
     * vtbl +0x6C: StringListVariantAVisualObject::OnKeyDown @004D7A57.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        int rowCount = rows.size();
        if (nChar == VK_DOWN && checkStateFlag(STATE_ENABLED) != 0) {
            if (rowCount > 1) {
                selectionAuxValue += selectionAuxValue < rowCount - 1 ? 1 : 0;
                draw();
                return 1;
            }
            return 0;
        }
        if (nChar == VK_UP && checkStateFlag(STATE_ENABLED) != 0) {
            if (rowCount > 1) {
                selectionAuxValue -= selectionAuxValue > 0 ? 1 : 0;
                draw();
                return 1;
            }
            return 0;
        }
        return super.onKeyDown(nChar);
    }

    /**
     * vtbl +0x74: StringListVariantAVisualObject::OnChar @004D7991.
     * Fully ported.
     */
    @Override
    public int onChar(int nChar) {
        if (nChar == ' ' && checkStateFlag(STATE_ENABLED) != 0) {
            int selectionMask = 1 << selectionAuxValue;
            if ((selectionValue & selectionMask) == 0) {
                selectionValue |= selectionMask;
            } else {
                selectionValue &= ~selectionMask;
            }
            draw();
            m_pParent.onMessage(TEXT_LIST_SELECTION_CHANGED, id, selectionValue);
            return 1;
        }
        return super.onChar(nChar);
    }

    /**
     * Native support: row bit test inside StringListVariantAVisualObject::Update @004D757C.
     */
    private boolean isRowSelected(int rowIndex) {
        int selectionMask = 1 << rowIndex;
        return (selectionValue & selectionMask) != 0;
    }

    /**
     * Native support: `g_Spr_RadioButtons::DrawAlpha/Draw` pair inside StringListVariantAVisualObject::Update @004D757C.
     * Fully ported.
     */
    private static void drawRowToggle(int x, int y, boolean selected) {
        int frameIndex = selected ? TOGGLE_FRAME_SELECTED : TOGGLE_FRAME_CLEAR;
        GUI.sprRadioButtons.drawAlpha(x + TOGGLE_ALPHA_X_OFFSET, y + TOGGLE_ALPHA_Y_OFFSET, frameIndex, TOGGLE_ALPHA_BRIGHTNESS, false);
        GUI.sprRadioButtons.draw(x + TOGGLE_X_OFFSET, y, frameIndex, 0, false);
    }

    /**
     * Native support: `g_Spr_RadioButtons->xSizeOf(0)` used by StringListVariantAVisualObject::Update @004D757C.
     * Fully ported.
     */
    private static int getRadioButtonWidth() {
        return GUI.sprRadioButtons.frames.get(0).width();
    }

    /**
     * Native support: `CStringArray::GetAt` + `CBitmapFont::DrawTextShadowed` call site in StringListVariantAVisualObject::Update @004D757C.
     * Fully ported.
     */
    private void drawChoiceLabel(int x, int y, int rowIndex) {
        drawTextShadowed(listBitmapFont, x, y, rows.get(rowIndex), 0, listFontPalette, TEXT_SHADOW_OFFSET);
    }

    /**
     * Native owner: `CBitmapFont::DrawTextShadowed @0045D536` call site in StringListVariantAVisualObject::Update @004D757C.
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
