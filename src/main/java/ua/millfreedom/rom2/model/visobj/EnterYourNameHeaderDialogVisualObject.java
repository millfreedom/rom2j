package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palettes;

import static ua.millfreedom.rom2.model.enums.MessageCodes.DIALOG_OK;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RETURN_TO_GAME;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SHOW_MAIN_MENU;
import static ua.millfreedom.rom2.model.enums.MessageCodes.START_NEW_GAME;
import static ua.millfreedom.rom2.text.DialogsText.ENTER_YOUR_NAME_69;
import static ua.millfreedom.rom2.text.DialogsText.ENTER_YOUR_NAME_70;
import static ua.millfreedom.rom2.text.DialogsText.FEMALE_73;
import static ua.millfreedom.rom2.text.DialogsText.MALE_72;
import static ua.millfreedom.rom2.text.DialogsText.SELECT_YOUR_SEX_71;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.TextTableId.DIALOGS;

/**
 * Native class: EnterYourNameHeaderDialogVisualObject (vtbl @0x005CC438).
 * Purpose: enter-your-name header dialog with editable name and sex controls.
 */
public class EnterYourNameHeaderDialogVisualObject extends HeaderDialogVisualObject {
    public static final int NATIVE_SIZE = 0x7C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!
    private static final int NAME_INPUT_ID = 2;
    private static final int SEX_LIST_ID = 3;

    //0x78
    public EnterYourNameDialogContext context;

    /**
     * Native: EnterYourNameHeaderDialogVisualObject::EnterYourNameHeaderDialogVisualObject @0044498D.
     * Fully ported.
     */
    public EnterYourNameHeaderDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, EnterYourNameDialogContext context) {
        super(id, xLeft, yTop, xRight, yBottom, context, get(DIALOGS, ENTER_YOUR_NAME_70), 4, null);
        this.context = context;
    }

    /**
     * vtbl +0x88: EnterYourNameHeaderDialogVisualObject::createDialogContent @004449E0.
     * Fully ported.
     */
    @Override
    protected CVisualObject createDialogContent(Object payload, @SuppressWarnings("unused") CRect contentRect) {
        EnterYourNameDialogContext dialogContext = (EnterYourNameDialogContext) payload;
        CBitmapFont dialogFont = Globals.fonts.font1;

        StaticTextVisualObject nameInput = new StaticTextVisualObject(
                NAME_INPUT_ID,
                0x28,
                0x3C,
                0x14A,
                0x54,
                dialogFont,
                Palettes.grayDim,
                get(DIALOGS, ENTER_YOUR_NAME_69)
        );
        addChild(nameInput);
        nameInput.setValue(dialogContext.name);

        StringListVariantBVisualObject sexList = new StringListVariantBVisualObject(
                SEX_LIST_ID,
                0x28,
                0x60,
                0x14A,
                0x90,
                dialogFont,
                Palettes.grayDim,
                get(DIALOGS, SELECT_YOUR_SEX_71)
        );
        sexList.addRow(get(DIALOGS, MALE_72));
        sexList.addRow(get(DIALOGS, FEMALE_73));
        addChild(sexList);
        sexList.setValue(dialogContext.sexSelection);
        return sexList;
    }

    /**
     * vtbl +0x8C: EnterYourNameHeaderDialogVisualObject::OnHeaderDialogAction @00444B61.
     * Fully ported.
     */
    @Override
    protected void onHeaderDialogAction(MessageCodes action) {
        if (action == DIALOG_OK) {
            StringBuilder selectedName = new StringBuilder();
            StaticTextVisualObject nameInput = (StaticTextVisualObject) getChildById(NAME_INPUT_ID);
            nameInput.getValue(selectedName);
            context.name = selectedName.toString();

            int[] selectedSex = new int[1];
            StringListVisualObject sexList = (StringListVisualObject) getChildById(SEX_LIST_ID);
            sexList.getValue(selectedSex);
            context.sexSelection = selectedSex[0];

            Globals.mainWindow.postMessage(START_NEW_GAME, 0, 0);
        } else if (action != RETURN_TO_GAME) {
            return;
        }
        if (action == DIALOG_OK || action == RETURN_TO_GAME) {
            Globals.mainWindow.postMessage(SHOW_MAIN_MENU, 0, 0);
        }
    }
}
