package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;

import static ua.millfreedom.rom2.model.enums.MessageCodes.END_QUEST;
import static ua.millfreedom.rom2.model.enums.MessageCodes.LOAD_GAME;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RETURN_TO_GAME;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SAVE_GAME;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SOUND_OPTIONS;
import static ua.millfreedom.rom2.text.DialogsText.*;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.TextTableId.DIALOGS;

/**
 * Native class: TownMenuListDialogVisualObject.
 * Purpose: in-town menu-list dialog with save/load/sound/abort/return actions.
 */
public class TownMenuListDialogVisualObject extends MenuListDialogVisualObject {
    public static final int NATIVE_SIZE = 0x7C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    /**
     * Native: TownMenuListDialogVisualObject::TownMenuListDialogVisualObject @004407C2.
     * Fully ported.
     */
    public TownMenuListDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, CRect rect) {
        super(id, xLeft, yTop, xRight, yBottom, null, 0, rect);
        CBitmapFont dialogFont = Globals.fonts.font1;
        addMenuButton(2, get(DIALOGS, SAVE_GAME_34), SAVE_GAME, 'S', dialogFont);
        addMenuButton(1, get(DIALOGS, LOAD_GAME_35), LOAD_GAME, 'L', dialogFont);
        addMenuButton(3, get(DIALOGS, SOUND_OPTIONS_37), SOUND_OPTIONS, 'N', dialogFont);
        addMenuButton(4, get(DIALOGS, ABORT_GAME_77), END_QUEST, 'E', dialogFont);
        addMenuButton(5, get(DIALOGS, RETURN_TO_GAME_40), RETURN_TO_GAME, 'R', dialogFont);
    }

    /**
     * Native support extracted from TownMenuListDialogVisualObject::TownMenuListDialogVisualObject @004407C2.
     */
    private void addMenuButton(int buttonId, String caption, MessageCodes message, char hotKey, CBitmapFont dialogFont) {
        MenuListCommandButtonVisualObject button = new MenuListCommandButtonVisualObject(
                buttonId,
                caption,
                dialogFont,
                null,
                message,
                hotKey,
                null
        );
        appendItem(button, 0x1E);
    }
}
