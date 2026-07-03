package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.action.ChatTextAction;
import ua.millfreedom.rom2.model.palette.Palette16;

/**
 * Native class: StaticTextVariantVisualObject.
 * Purpose: static-text input specialization that submits chat text on Enter.
 */
public class StaticTextVariantVisualObject extends StaticTextVisualObject {
    public static final int NATIVE_SIZE = 0x7C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!


    /**
     * Native: StaticTextVariantVisualObject::StaticTextVariantVisualObject @0044EBF0.
     * Fully ported.
     */
    public StaticTextVariantVisualObject(int id, CRect rect, CBitmapFont bitmapFont, Palette16 textPalette, String name) {
        super(id, rect, bitmapFont, textPalette, name);
    }

    /**
     * vtbl +0x6C: StaticTextVariantVisualObject::OnKeyDown @0044EC30.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        if (nChar != 0x0D) {
            return super.onKeyDown(nChar);
        }

        StringBuilder submittedText = new StringBuilder();
        getValue(submittedText);
        setValue("");
        draw();
        Globals.mainWindow.getMapVisual().sendChatTextAction(
                submittedText.toString(),
                ChatTextAction.CHAT_DELIVERY_BROADCAST,
                0
        );
        return 1;
    }

}
