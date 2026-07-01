package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.net.CLlDriver;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.LlDriverConnectionOption;
import ua.millfreedom.rom2.model.PhoneBook;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palettes;

import java.util.ArrayList;
import java.util.List;

import static ua.millfreedom.rom2.model.enums.MessageCodes.SHOW_MULTIPLAYER_CONNECTION_DIALOG;
import static ua.millfreedom.rom2.model.enums.MessageCodes.MODEM_ANSWER;
import static ua.millfreedom.rom2.model.enums.MessageCodes.MODEM_DIAL;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SHOW_MODEM_SETTINGS_DIALOG;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SHOW_MULTIPLAYER_SESSION_DIALOG;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RETURN_TO_GAME;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SELECT_MODEM_HOST_DRIVER;
import static ua.millfreedom.rom2.model.window.windowproc.handlers.CMainWindowWindowProcSupport.readMessageInt;
import static ua.millfreedom.rom2.text.DialogsText.ANSWER_121;
import static ua.millfreedom.rom2.text.DialogsText.CANCEL_1;
import static ua.millfreedom.rom2.text.DialogsText.CLICK_HERE_TO_DIAL_126;
import static ua.millfreedom.rom2.text.DialogsText.CLICK_HERE_TO_START_THE_GAME_AND_WAIT_FOR_YOUR_OPPONENT_127;
import static ua.millfreedom.rom2.text.DialogsText.DIAL_120;
import static ua.millfreedom.rom2.text.DialogsText.ENTER_NUMBER_AND_NAME_152;
import static ua.millfreedom.rom2.text.DialogsText.ENTER_THE_PHONE_NUMBER_TO_DIAL_91;
import static ua.millfreedom.rom2.text.DialogsText.MODEM_SETTINGS_147;
import static ua.millfreedom.rom2.text.DialogsText.SELECT_THE_MODEM_WHICH_YOU_WISH_TO_USE_93;
import static ua.millfreedom.rom2.text.DialogsText.THIS_IS_THE_LIST_OF_RECENTLY_USED_PHONE_NUMBERS_PRESS_DEL_TO_90;
import static ua.millfreedom.rom2.text.DialogsText.YOUR_PHONE_BOOK_92;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_UNKNOWN_ERROR_154;
import static ua.millfreedom.rom2.text.TextTableId.DIALOGS;

/**
 * Native class: ModemSettingsDialogVisualObject.
 * Purpose: centered modem settings dialog with phone-book input and available-modem selection.
 */
public class ModemSettingsDialogVisualObject extends CenteredDialogVisualObject {
    private static final int PHONE_BOOK_LIST_ID = 1;
    private static final int PHONE_BOOK_SCROLLBAR_ID = 2;
    private static final int PHONE_INPUT_ID = 3;
    private static final int PHONE_BOOK_HEADER_ID = 4;
    private static final int MODEM_LIST_ID = 7;
    private static final int MODEM_LIST_SCROLLBAR_ID = 8;
    private static final int DIAL_BUTTON_ID = 10;
    private static final int ANSWER_BUTTON_ID = 11;
    private static final int CANCEL_BUTTON_ID = 12;

    public static final int NATIVE_SIZE = 0x74; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    //0x64
    public int availableModemCount;
    //0x68
    public PhoneBook phoneBook;
    //0x6c
    public final List<LlDriverConnectionOption> availableModemOptions = new ArrayList<>();

    /**
     * Native: ModemSettingsDialogVisualObject::ModemSettingsDialogVisualObject @00447058.
     * Fully ported.
     */
    public ModemSettingsDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, PhoneBook phoneBook) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        this.phoneBook = phoneBook;
        if (getClass() == ModemSettingsDialogVisualObject.class) {
            initialize();
        }
    }

    /**
     * vtbl +0x78: ModemSettingsDialogVisualObject::Initialize @004470A2.
     * Fully ported. CLlDriver modem enumeration remains an explicit support boundary.
     */
    @Override
    public void initialize() {
        availableModemOptions.clear();
        CLlDriver.loadAvailableModemConnectionOptions(availableModemOptions);
        availableModemCount = availableModemOptions.size();

        CBitmapFont dialogFont = Globals.fonts.font1;
        addChild(new DialogWindowVisualObject(
                -1,
                0x28,
                0x14,
                cRect.width() - 0x28,
                0x2C,
                get(DIALOGS, MODEM_SETTINGS_147),
                dialogFont,
                Palettes.grayDim,
                0x2
        ));

        int listWidth = cRect.width() / 3;
        int listBottom = cRect.height() - 0x40;
        CRect phoneBookRect = new CRect(0x28, 0x50, 0x28 + listWidth, listBottom);
        RecentPhoneNumbersTextListVisualObject phoneBookList = new RecentPhoneNumbersTextListVisualObject(
                PHONE_BOOK_LIST_ID,
                phoneBookRect,
                dialogFont,
                Palettes.grayDim,
                Palettes.gray,
                PHONE_BOOK_SCROLLBAR_ID,
                get(DIALOGS, THIS_IS_THE_LIST_OF_RECENTLY_USED_PHONE_NUMBERS_PRESS_DEL_TO_90),
                phoneBook.numbers
        );
        phoneBookList.rows.addAll(phoneBook.numbers);
        addChild(phoneBookList);

        CRect phoneBookListRect = phoneBookList.getRect();
        addChild(new PostSetupVisualObject(
                PHONE_BOOK_SCROLLBAR_ID,
                phoneBookListRect.right,
                phoneBookListRect.top,
                phoneBookListRect.right + 0x18,
                phoneBookListRect.bottom,
                null
        ));

        DialogWindowVisualObject phoneBookHeader = new DialogWindowVisualObject(
                PHONE_BOOK_HEADER_ID,
                phoneBookListRect.left + 10,
                phoneBookListRect.top - 0x1E,
                phoneBookListRect.right + 10,
                phoneBookListRect.top - 10,
                get(DIALOGS, YOUR_PHONE_BOOK_92),
                dialogFont,
                Palettes.grayDim,
                0x2
        );
        addChild(phoneBookHeader);
        phoneBookList.gameDialogControls = phoneBookHeader;

        addChild(new DialogWindowVisualObject(
                -2,
                phoneBookListRect.right + 0x30,
                phoneBookListRect.top - 0x1E,
                phoneBookListRect.right + listWidth * 3,
                phoneBookListRect.top - 10,
                get(DIALOGS, ENTER_NUMBER_AND_NAME_152),
                dialogFont,
                Palettes.grayDim,
                0
        ));

        StaticTextVisualObject phoneInput = new StaticTextVisualObject(
                PHONE_INPUT_ID,
                phoneBookListRect.right + 0x30,
                phoneBookListRect.top,
                phoneBookListRect.right + (listWidth * 3) / 2,
                phoneBookListRect.top + 0x18,
                dialogFont,
                Palettes.grayDim,
                get(DIALOGS, ENTER_THE_PHONE_NUMBER_TO_DIAL_91)
        );
        addChild(phoneInput);

        CRect modemListRect = new CRect(phoneInput.getRect());
        modemListRect.top += phoneInput.getRect().height() + 4;
        modemListRect.bottom = modemListRect.top + 0x30;
        TextListVisualObject modemList = new TextListVisualObject(
                MODEM_LIST_ID,
                modemListRect,
                dialogFont,
                Palettes.grayDim,
                Palettes.gray,
                MODEM_LIST_SCROLLBAR_ID,
                get(DIALOGS, SELECT_THE_MODEM_WHICH_YOU_WISH_TO_USE_93)
        );
        for (LlDriverConnectionOption option : availableModemOptions) {
            modemList.rows.add(option.label);
        }
        addChild(modemList);

        modemListRect.top += modemList.getRect().height() + 4;
        modemListRect.bottom = modemListRect.top + 0x18;
        CommandButtonVisualObject dialButton = createButton(
                DIAL_BUTTON_ID,
                modemListRect,
                get(DIALOGS, DIAL_120),
                MODEM_DIAL,
                get(DIALOGS, CLICK_HERE_TO_DIAL_126),
                dialogFont
        );
        addChild(dialButton);
        if (!Globals.hasValidGameMedia || availableModemCount == 0) {
            setButtonActiveState(dialButton, false);
        }

        modemListRect.top += 0x1C;
        modemListRect.bottom = modemListRect.top + 0x18;
        CommandButtonVisualObject answerButton = createButton(
                ANSWER_BUTTON_ID,
                modemListRect,
                get(DIALOGS, ANSWER_121),
                MODEM_ANSWER,
                get(DIALOGS, CLICK_HERE_TO_START_THE_GAME_AND_WAIT_FOR_YOUR_OPPONENT_127),
                dialogFont
        );
        addChild(answerButton);
        if (!Globals.hasValidGameMedia || availableModemCount == 0) {
            setButtonActiveState(answerButton, false);
        }

        modemListRect.top += 0x1C;
        modemListRect.bottom = modemListRect.top + 0x18;
        addChild(createButton(
                CANCEL_BUTTON_ID,
                modemListRect,
                get(DIALOGS, CANCEL_1),
                RETURN_TO_GAME,
                get(DIALOGS, CANCEL_1),
                dialogFont
        ));

        String initialPhoneNumber = phoneBook.numbers.isEmpty() ? "" : phoneBook.numbers.getFirst();
        phoneInput.setInputText(initialPhoneNumber);
        updateDialButtonState(initialPhoneNumber);
    }

    /**
     * vtbl +0x48: ModemSettingsDialogVisualObject::OnMessage @0044793A.
     * Fully ported. CLlDriver modem connect/server calls remain explicit support boundaries.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        int w = readMessageInt(wParam);
        int l = readMessageInt(lParam);
        switch (msg) {
            case MessageCodes.TEXT_LIST_SELECTION_DBLCLK:
                // Model the native main-window PostMessage round trip locally until the dialog loop is ported.
                onMessage(MessageCodes.TEXT_LIST_SELECTION_CHANGED, wParam, lParam);
                return onMessage(MODEM_DIAL, 0, 0);
            case RETURN_TO_GAME:
                int result = super.onMessage(msg, wParam, lParam);
                Globals.mainWindow.postMessage(SHOW_MULTIPLAYER_CONNECTION_DIALOG, 0, 0);
                return result;
            case MessageCodes.TEXT_LIST_SELECTION_CHANGED:
                if (w == PHONE_BOOK_LIST_ID) {
                    String selectedPhoneNumber = l < 0
                            ? ""
                            : phoneBook.numbers.get(l);
                    updateDialButtonState(selectedPhoneNumber);
                    StaticTextVisualObject phoneInput = (StaticTextVisualObject) getChildById(PHONE_INPUT_ID);
                    phoneInput.setInputText(selectedPhoneNumber);
                    phoneInput.draw();
                }
                if (w == PHONE_INPUT_ID) {
                    StaticTextVisualObject phoneInput = (StaticTextVisualObject) getChildById(PHONE_INPUT_ID);
                    updateDialButtonState(phoneInput.text);
                }
                return 1;
            case MODEM_DIAL:
                CVisualObject dialButton = getChildById(DIAL_BUTTON_ID);
                if (dialButton.checkStateFlag(0x1) == 0) {
                    return 1;
                }

                String dialNumber = readDialNumber();
                phoneBook.dialNumber = dialNumber;
                boolean alreadyPresent = false;
                for (String storedPhoneNumber : phoneBook.numbers) {
                    if (dialNumber.equals(storedPhoneNumber)) {
                        alreadyPresent = true;
                        break;
                    }
                }
                if (!alreadyPresent) {
                    phoneBook.numbers.addFirst(dialNumber);
                }

                if (CLlDriver.prepareModemConnect(dialNumber, readSelectedModemOption())) {
                    Globals.mainWindow.postMessage(SHOW_MULTIPLAYER_SESSION_DIALOG, 0, 0);
                    super.onMessage(MessageCodes.DIALOG_OK, 0, 0);
                }
                return 1;
            case MODEM_ANSWER:
                super.onMessage(MessageCodes.DIALOG_OK, 0, 0);
                boolean startedServer = CLlDriver.startModemServerBoundary(
                        readMainWindowPlayerName(),
                        readSelectedModemOption()
                );
                if (!startedServer) {
                    Globals.mainWindow.showDialog(new HeaderDialogVariantVisualObject(
                            1,
                            100,
                            100,
                            0x21C,
                            0x17C,
                            null,
                            get(MAIN_UNKNOWN_ERROR_154),
                            0
                    ));
                    Globals.mainWindow.postMessage(SHOW_MODEM_SETTINGS_DIALOG, 0, 0);
                } else {
                    Globals.mainWindow.postMessage(SELECT_MODEM_HOST_DRIVER, 0, 0);
                }
                return 1;
            default:
                return super.onMessage(msg, wParam, lParam);
        }
    }

    /**
     * Native support extracted from ModemSettingsDialogVisualObject::updateDialButtonState @004478B8.
     * Fully ported.
     */
    private void updateDialButtonState(String dialNumber) {
        CommandButtonVisualObject dialButton = (CommandButtonVisualObject) getChildById(DIAL_BUTTON_ID);
        boolean canDial = dialNumber != null && availableModemCount != 0;
        setButtonActiveState(dialButton, canDial);
    }

    /**
     * Native support extracted from the selected modem row read in
     * ModemSettingsDialogVisualObject::OnMessage @0044793A.
     * Fully ported.
     */
    private LlDriverConnectionOption readSelectedModemOption() {
        TextListVisualObject modemList = (TextListVisualObject) getChildById(MODEM_LIST_ID);
        return availableModemOptions.get(modemList.selectedRow);
    }

    /**
     * Native support extracted from the StaticTextVisualObject text-buffer read in
     * ModemSettingsDialogVisualObject::OnMessage @0044793A.
     * Fully ported.
     */
    private String readDialNumber() {
        return ((StaticTextVisualObject) getChildById(PHONE_INPUT_ID)).text;
    }

    /**
     * Java helper for repeated CommandButtonVisualObject constructor branches in ModemSettingsDialogVisualObject::Initialize @004470A2.
     * not ported.
     */
    private static CommandButtonVisualObject createButton(
            int id,
            CRect rect,
            String caption,
            MessageCodes msg,
            String name,
            CBitmapFont dialogFont
    ) {
        return new CommandButtonVisualObject(id, rect, caption, dialogFont, Palettes.grayDim, msg, 0, name);
    }

    /**
     * Java helper for native `SetStateFlag(..., 1, set)` button gates in ModemSettingsDialogVisualObject own methods.
     * not ported.
     */
    private static void setButtonActiveState(CommandButtonVisualObject button, boolean active) {
        button.setStateFlag(0x1, active ? 1 : 0);
        if (!active) {
            button.pressedState = 0;
        }
        button.draw();
    }

    /**
     * Native support extracted from `AfxGetMainWnd()->m_GameSession.m_PlayerName` used by
     * ModemSettingsDialogVisualObject::OnMessage @0044793A.
     * Fully ported.
     */
    private static String readMainWindowPlayerName() {
        return Globals.mainWindow.m_GameSession.m_PlayerName;
    }

}
