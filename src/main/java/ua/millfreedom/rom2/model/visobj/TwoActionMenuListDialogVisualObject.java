package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;

import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palettes;

import static ua.millfreedom.rom2.model.enums.MessageCodes.EXIT_MAP;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RETURN_TO_GAME;
import static ua.millfreedom.rom2.text.DialogsText.CONTINUE_154;
import static ua.millfreedom.rom2.text.DialogsText.VICTORY_43;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.TextTableId.DIALOGS;

/**
 * Native class: TwoActionMenuListDialogVisualObject.
 * Purpose: menu-list dialog with a retained header caption and two action entries.
 */
public class TwoActionMenuListDialogVisualObject extends MenuListDialogVisualObject {
    public static final int NATIVE_SIZE = 0x80; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    //0x7c
    public String dialogCaption;

    /**
     * Native: TwoActionMenuListDialogVisualObject::TwoActionMenuListDialogVisualObject @0044DA0F.
     * Fully ported.
     */
    public TwoActionMenuListDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, String dialogCaption, CRect rect) {
        super(id, xLeft, yTop, xRight, yBottom, null, 0, rect);
        this.dialogCaption = dialogCaption;
    }

    /**
     * vtbl +0x48: TwoActionMenuListDialogVisualObject::OnMessage @0044DC7A.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * vtbl +0x78: TwoActionMenuListDialogVisualObject::Initialize @0044DA55.
     * Fully ported.
     */
    @Override
    public void initialize() {
        CBitmapFont dialogFont = Globals.fonts.font1;

        DialogWindowVisualObject header = new DialogWindowVisualObject(
                1,
                0x28,
                0x20,
                0xE8,
                0x50,
                dialogCaption,
                dialogFont,
                Palettes.grayDim,
                0x2
        );
        addChild(header);

        int buttonLeft = cRect.width() / 0x8;
        int buttonRight = (cRect.width() * 0x7) / 0x8;
        int firstButtonTop = cRect.height() - 0x60;
        int firstButtonBottom = cRect.height() - 0x48;
        CRect victoryButtonRect = new CRect(buttonLeft, firstButtonTop, buttonRight, firstButtonBottom);
        MenuListCommandButtonVisualObject victoryButton = new MenuListCommandButtonVisualObject(
                2,
                victoryButtonRect,
                get(DIALOGS, VICTORY_43),
                dialogFont,
                null,
                EXIT_MAP,
                'V',
                ""
        );
        appendItem(victoryButton, victoryButtonRect.height());

        int secondButtonTop = cRect.height() - 0x48;
        int secondButtonBottom = cRect.height() - 0x30;
        CRect continueButtonRect = new CRect(buttonLeft, secondButtonTop, buttonRight, secondButtonBottom);
        MenuListCommandButtonVisualObject continueButton = new MenuListCommandButtonVisualObject(
                3,
                continueButtonRect,
                get(DIALOGS, CONTINUE_154),
                dialogFont,
                null,
                RETURN_TO_GAME,
                'C',
                ""
        );
        appendItem(continueButton, continueButtonRect.height());
    }
}
