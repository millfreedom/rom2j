package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.Hat;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palettes;

import static ua.millfreedom.rom2.model.enums.MessageCodes.SHOW_CHARACTER_LOADER_DIALOG;
import static ua.millfreedom.rom2.model.enums.MessageCodes.DIALOG_OK;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SHOW_MAIN_MENU;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.PatchText.ENTER_URL_103;
import static ua.millfreedom.rom2.text.PatchText.ENTER_SERVER_LIST_PAGE_URL_OR_HAT_URL_112;
import static ua.millfreedom.rom2.text.PatchText.RETRIEVING_SERVER_LIST_FROM_WEB_PAGE_OR_HAT_102;
import static ua.millfreedom.rom2.text.TextTableId.PATCH;

/**
 * Native class: HatServerListDialogVisualObject.
 * Purpose: header dialog for HAT server list retrieval.
 */
public class HatServerListDialogVisualObject extends HeaderDialogVisualObject {
    public static final int NATIVE_SIZE = 0x7C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!
    private static final int SERVER_LIST_URL_INPUT_ID = 2;

    //0x78
    public Hat hat;

    /**
     * Native: HatServerListDialogVisualObject::HatServerListDialogVisualObject @00449292.
     * Fully ported.
     */
    public HatServerListDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, Hat hat) {
        super(id, xLeft, yTop, xRight, yBottom, hat,
                get(PATCH, RETRIEVING_SERVER_LIST_FROM_WEB_PAGE_OR_HAT_102),
                1,
                get(PATCH, ENTER_URL_103));
        this.hat = hat;
    }

    /**
     * vtbl +0x88: HatServerListDialogVisualObject::createDialogContent @004493CB.
     * Fully ported.
     */
    @Override
    protected CVisualObject createDialogContent(Object payload, CRect contentRect) {
        Hat targetHat = (Hat) payload;
        StaticTextVisualObject urlInput = new StaticTextVisualObject(
                SERVER_LIST_URL_INPUT_ID,
                contentRect.left,
                contentRect.top,
                contentRect.right,
                contentRect.top + 0x18,
                Globals.fonts.font1,
                Palettes.grayDim,
                get(PATCH, ENTER_SERVER_LIST_PAGE_URL_OR_HAT_URL_112)
        );
        addChild(urlInput);
        urlInput.setValue(targetHat.ip);
        return urlInput;
    }

    /**
     * vtbl +0x8C: HatServerListDialogVisualObject::OnHeaderDialogAction @004492F0.
     * Fully ported. Native writes `isHat = 0` before the branch, making the alternate `0x0488` post unreachable.
     */
    @Override
    protected void onHeaderDialogAction(MessageCodes action) {
        if (action != DIALOG_OK) {
            Globals.mainWindow.postMessage(SHOW_MAIN_MENU, 0, 0);
            return;
        }

        StringBuilder inputText = new StringBuilder();
        getChildById(SERVER_LIST_URL_INPUT_ID).getValue(inputText);
        hat.ip = inputText.toString();
        hat.isHat = false;
        Globals.mainWindow.connectionScratchState.serverListSourceIsWebPage = 1;
        Globals.mainWindow.postMessage(SHOW_CHARACTER_LOADER_DIALOG, 0, 0);
    }
}
