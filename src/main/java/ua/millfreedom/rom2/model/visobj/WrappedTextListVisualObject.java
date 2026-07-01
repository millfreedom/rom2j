package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.palette.Palette16;

import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_RETURN;

/**
 * Native class: WrappedTextListVisualObject (vtbl @0x005D0460).
 * Purpose: wrapped text list specialization with no additional native fields beyond TextListVisualObject.
 */
public class WrappedTextListVisualObject extends TextListVisualObject {
    public static final int NATIVE_SIZE = 0x94; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    /**
     * Native: WrappedTextListVisualObject::WrappedTextListVisualObject @004E05B0.
     * Fully ported.
     */
    public WrappedTextListVisualObject(int id, CRect rect, Object bitmapFont, Palette16 field0x7c, Palette16 field0x80, int field0x90, String name) {
        super(id, rect, bitmapFont, field0x7c, field0x80, field0x90, name);
    }

    /**
     * vtbl +0x6C: WrappedTextListVisualObject::OnKeyDown @004E0600.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        if (nChar == VK_RETURN) {
            ((ScrollablePanelVisualObject) m_pParent).commitSelectedRowToHeader();
            return 1;
        }
        return super.onKeyDown(nChar);
    }
}
