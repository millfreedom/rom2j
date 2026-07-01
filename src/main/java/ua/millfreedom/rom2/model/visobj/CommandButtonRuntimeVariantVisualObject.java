package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palette16;

/**
 * Native class: CommandButtonRuntimeVariantVisualObject (vtbl @0x005CBED8).
 * Purpose: CommandButtonVisualObject runtime variant with alternate behavior table.
 */
public class CommandButtonRuntimeVariantVisualObject extends CommandButtonVisualObject {
    public static final int NATIVE_SIZE = 0x78; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!


    /**
     * Native: CommandButtonRuntimeVariantVisualObject::CommandButtonRuntimeVariantVisualObject @0044DF10.
     * Fully ported.
     */
    public CommandButtonRuntimeVariantVisualObject(int id, CRect rect, String caption, CBitmapFont bitmapFont, Palette16 hoverPalette, MessageCodes msg, int hotKey, String name) {
        super(id, rect, caption, bitmapFont, hoverPalette, msg, hotKey, name);
    }

    /**
     * vtbl +0x6C: CommandButtonRuntimeVariantVisualObject::OnKeyDown @0044DF60.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        if (checkStateFlag(STATE_ENABLED) == 0) {
            return 0;
        }
        return super.onKeyDown(nChar);
    }
}
