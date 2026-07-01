package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.palette.Palette16;

import java.util.ArrayList;
import java.util.List;
import java.nio.IntBuffer;

/**
 * Native class: StringListVisualObject.
 * Purpose: string-array list view with font/palette binding and selection payload state.
 */
public class StringListVisualObject extends CVisualObject {
    public static final int NATIVE_SIZE = 0x8C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int STATE_ACTIVE = 0x01;
    private static final int STATE_ENABLED = 0x04;
    private static final int STATE_VISIBLE = 0x08;
    private static final int MK_LBUTTON = 0x1;
    private static final int HITBOX_PADDING = 4;

    //0x5c
    public int visibleFlag;
    //0x64
    public final List<String> rows = new ArrayList<>();
    //0x78
    public CBitmapFont listBitmapFont;
    //0x7c
    public Palette16 listFontPalette;
    //0x80
    public int selectionValue;
    //0x84
    public int field0x84;
    //0x88
    public int selectionAuxValue;

    /**
     * Native: StringListVisualObject::StringListVisualObject @004D7258.
     * Fully ported.
     */
    public StringListVisualObject() {
        super();
    }

    /**
     * Native: StringListVisualObject::StringListVisualObject @004D72AF.
     * Fully ported.
     */
    public StringListVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            CBitmapFont listBitmapFont,
            Palette16 listFontPalette,
            String name
    ) {
        super(id, xLeft, yTop, xRight, yBottom, name);
        this.visibleFlag = 0;
        this.listBitmapFont = listBitmapFont;
        this.listFontPalette = listFontPalette;
        this.selectionValue = 0;
        this.field0x84 = 0;
        this.selectionAuxValue = 0;
        this.m_nState |= 0x2;
    }

    /**
     * Native: StringListVisualObject::StringListVisualObject @004D735B.
     * Fully ported.
     */
    public StringListVisualObject(
            int id,
            CRect rect,
            CBitmapFont listBitmapFont,
            Palette16 listFontPalette,
            String name
    ) {
        super(id, rect, name);
        this.visibleFlag = 0;
        this.listBitmapFont = listBitmapFont;
        this.listFontPalette = listFontPalette;
        this.selectionValue = 0;
        this.field0x84 = 0;
        this.selectionAuxValue = 0;
        this.m_nState |= 0x2;
    }

    /**
     * vtbl +0x24: StringListVisualObject::SetVisible @004D7451.
     * Fully ported.
     */
    @Override
    public void setVisible(int bVisible) {
        super.setVisible(bVisible);
        visibleFlag = bVisible;
    }

    /**
     * vtbl +0x3C: StringListVisualObject::getValue @0044EED0.
     * Fully ported. Native writes the selection through one pointer-sized payload; Java models the recovered `int *`
     * case through `int[]` and base-walker `IntBuffer` payloads.
     */
    @Override
    public void getValue(Object value) {
        if (value instanceof int[] out && out.length > 0) {
            out[0] = selectionValue;
            return;
        }
        if (value instanceof IntBuffer out) {
            out.put(0, selectionValue);
            return;
        }
        throw new IllegalArgumentException("StringListVisualObject slot 0x3C expects int[] or IntBuffer payload");
    }

    /**
     * Native support: typed Java adapter for StringListVisualObject slot `0x3C` @0044EED0.
     */
    public int getSelectionValue() {
        return selectionValue;
    }

    /**
     * vtbl +0x40: StringListVisualObject::getValueRecursiveSize @0044EEC0.
     * Fully ported.
     */
    @Override
    public int getValueRecursiveSize() {
        return 4;
    }

    /**
     * vtbl +0x44: StringListVisualObject::setValue @0044F030.
     * Fully ported. Native reads the selection through one pointer-sized payload; Java models the recovered `int *`,
     * base-walker `IntBuffer`, and direct-value helpers.
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
        throw new IllegalArgumentException("StringListVisualObject slot 0x44 expects Number, int[], or IntBuffer payload");
    }

    /**
     * Native support: typed Java adapter for StringListVisualObject slot `0x44` @0044F030.
     */
    public void setSelectionValue(int value) {
        selectionValue = value;
        selectionAuxValue = value;
    }

    /**
     * Native: StringListVisualObject::AddRow @0044EE50.
     * Fully ported.
     */
    public void addRow(String rowText) {
        rows.add(rowText);
    }

    /**
     * vtbl +0x4C: StringListVisualObject::OnMouseMove @004D7473.
     * Fully ported.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        if (m_pParent != null && checkStateFlag(STATE_ACTIVE) != 0) {
            if (checkStateFlag(STATE_ENABLED) == 0) {
                m_pParent.switchEnabledChild(this, true);
            }

            if ((nFlags & MK_LBUTTON) == 0) {
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
            } else {
                onLButtonDown(nFlags, x, y);
            }
        }
        return 0;
    }

    /**
     * vtbl +0x78: StringListVisualObject::GetRowIndexAtScreenY @004D7405.
     * Fully ported.
     */
    protected int getRowIndexAtScreenY(int screenY) {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        return (screenY - screenRect.top) / getRadioButtonRowHeight();
    }

    /**
     * Native support: `g_Spr_RadioButtons->ySizeOf(0)` used by StringListVisualObject::GetRowIndexAtScreenY @004D7405.
     * Fully ported.
     */
    protected static int getRadioButtonRowHeight() {
        return GUI.sprRadioButtons.frames.get(0).ySize();
    }

    /**
     * Native support for `FUN_004564DF` inactive-overlay branches in StringListVariantAVisualObject::Update
     * @004D757C, StringListVariantBVisualObject::Update @004D7E04, and StringListVariantCVisualObject::Update
     * @004D7B31.
     * Fully ported.
     */
    protected static void shadeRect(CRect screenRect, int shadeLevel) {
        Globals.renderer.applyShadeToRect(screenRect.left, screenRect.top, screenRect.right, screenRect.bottom, shadeLevel);
    }
}
