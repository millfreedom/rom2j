package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.SavedGameFiles;

import static ua.millfreedom.rom2.model.enums.MessageCodes.DIALOG_OK;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RETURN_TO_GAME;
import static ua.millfreedom.rom2.text.DialogsText.EXIT_TO_MAIN_MENU_44;
import static ua.millfreedom.rom2.text.DialogsText.LOAD_GAME_35;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_MISSION_FAILED_141;
import static ua.millfreedom.rom2.text.TextTableId.DIALOGS;

/**
 * Native class: MissionFailedHeaderDialogVisualObject.
 * Purpose: header-dialog specialization for mission-failed flows.
 */
public class MissionFailedHeaderDialogVisualObject extends HeaderDialogVariantVisualObject {
    public static final int NATIVE_SIZE = 0x78; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    /**
     * Native: MissionFailedHeaderDialogVisualObject::MissionFailedHeaderDialogVisualObject @0044D1E2.
     * Fully ported.
     */
    public MissionFailedHeaderDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, String messageText) {
        super(id, xLeft, yTop, xRight, yBottom, messageText, get(MAIN_MISSION_FAILED_141), 0xFFFF);
    }

    /**
     * vtbl +0x78: MissionFailedHeaderDialogVisualObject::Initialize @0044D237.
     * Fully ported.
     */
    @Override
    public void initialize() {
        super.initialize();
        cRect.bottom += 0x40;

        CBitmapFont dialogFont = Globals.fonts.font1;
        int buttonLeft = cRect.width() / 0x8;
        int buttonRight = (cRect.width() * 0x7) / 0x8;
        int firstButtonTop = cRect.height() - 0x60;
        int firstButtonBottom = cRect.height() - 0x48;
        CRect exitButtonRect = new CRect(buttonLeft, firstButtonTop, buttonRight, firstButtonBottom);
        CommandButtonVisualObject exitButton = new CommandButtonVisualObject(
                4,
                exitButtonRect,
                get(DIALOGS, EXIT_TO_MAIN_MENU_44),
                dialogFont,
                null,
                DIALOG_OK,
                0,
                ""
        );
        addChild(exitButton);

        int secondButtonTop = cRect.height() - 0x48;
        int secondButtonBottom = cRect.height() - 0x30;
        CRect loadGameButtonRect = new CRect(buttonLeft, secondButtonTop, buttonRight, secondButtonBottom);
        CommandButtonVisualObject loadGameButton = new CommandButtonVisualObject(
                5,
                loadGameButtonRect,
                get(DIALOGS, LOAD_GAME_35),
                dialogFont,
                null,
                RETURN_TO_GAME,
                0,
                ""
        );
        addChild(loadGameButton);
        linkDownNeighbor(getChildById(4));

        if (!SavedGameFiles.saveFileExists()) {
            loadGameButton.setStateFlag(1, 0);
        }
    }
}
