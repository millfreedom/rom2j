package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;

import static ua.millfreedom.rom2.model.enums.MessageCodes.*;
import static ua.millfreedom.rom2.text.DialogsText.EXIT_TO_MAIN_MENU_44;
import static ua.millfreedom.rom2.text.DialogsText.EXIT_TO_WINDOWS_45;
import static ua.millfreedom.rom2.text.DialogsText.RETURN_TO_GAME_40;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.TextTableId.DIALOGS;

/**
 * Native class: PauseMenuVariantBVisualObject.
 * Purpose: pause menu variant without Exit Map button.
 */
public class PauseMenuVariantBVisualObject extends MenuListDialogVisualObject {
    public static final int NATIVE_SIZE = 0x7C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    /**
     * Native: PauseMenuVariantBVisualObject::PauseMenuVariantBVisualObject @00440A22.
     * Fully ported.
     */
    public PauseMenuVariantBVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, CRect rect) {
        super(id, xLeft, yTop, xRight, yBottom, null, 0, rect);
        addPauseButton(1, get(DIALOGS, EXIT_TO_MAIN_MENU_44), EXIT_TO_MENU, 'E');
        addPauseButton(2, get(DIALOGS, EXIT_TO_WINDOWS_45), WM_CLOSE, 'W');
        addPauseButton(3, get(DIALOGS, RETURN_TO_GAME_40), RETURN_TO_GAME, 'R');
    }

    /**
     * Native support extracted from PauseMenuVariantBVisualObject::PauseMenuVariantBVisualObject @00440A22.
     */
    private MenuListCommandButtonVisualObject addPauseButton(int buttonId, String caption, MessageCodes message, char hotKey) {
        MenuListCommandButtonVisualObject button = new MenuListCommandButtonVisualObject(
                buttonId,
                caption,
                Globals.fonts.font1,
                null,
                message,
                hotKey,
                null
        );
        appendItem(button, 0x1E);
        return button;
    }
}
