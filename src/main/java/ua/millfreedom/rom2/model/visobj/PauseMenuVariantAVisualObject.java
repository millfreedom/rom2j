package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.net.CLlDriver;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.enums.ProtocolId;

import static ua.millfreedom.rom2.model.enums.MessageCodes.*;
import static ua.millfreedom.rom2.text.DialogsText.*;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.TextTableId.DIALOGS;

/**
 * Native class: PauseMenuVariantAVisualObject.
 * Purpose: in-game pause menu variant with Exit Map/Exit To Menu/Exit Game/Return buttons.
 */
public class PauseMenuVariantAVisualObject extends MenuListDialogVisualObject {
    public static final int NATIVE_SIZE = 0x7C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    /**
     * Native: PauseMenuVariantAVisualObject::PauseMenuVariantAVisualObject @0044050B.
     * Fully ported.
     */
    public PauseMenuVariantAVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, Object handler, int field0x64, CRect rect, int exitMapEnabledFlag) {
        super(id, xLeft, yTop, xRight, yBottom, handler, field0x64, rect);

        int sessionMode = Globals.mainWindow.sessionMode;
        MenuListCommandButtonVisualObject exitMapButton;
        if (sessionMode == 0 || sessionMode == 1 || sessionMode == 3) {
            exitMapButton = addPauseButton(1, get(DIALOGS, CHANGE_MAP_42), EXIT_MAP, 'C');
        } else {
            exitMapButton = addPauseButton(1, get(DIALOGS, VICTORY_43), EXIT_MAP, 'V');
        }

        if (exitMapEnabledFlag == 0 && sessionMode == 2) {
            exitMapButton.setStateFlag(1, 0);
        }
        if (CLlDriver.getProtocolId() == ProtocolId.TCP_IP) {
            exitMapButton.setStateFlag(1, 0);
        }

        addPauseButton(2, get(DIALOGS, EXIT_TO_MAIN_MENU_44), EXIT_TO_MENU, 'E');
        addPauseButton(3, get(DIALOGS, EXIT_TO_WINDOWS_45), WM_CLOSE, 'W');
        addPauseButton(4, get(DIALOGS, RETURN_TO_GAME_40), RETURN_TO_GAME, 'R');
    }

    /**
     * Native support extracted from PauseMenuVariantAVisualObject::PauseMenuVariantAVisualObject @0044050B.
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
