package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.net.CLlDriver;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.ComPortSettings;
import ua.millfreedom.rom2.model.LlDriverConnectionOption;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.text.DialogsText;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static ua.millfreedom.rom2.model.enums.MessageCodes.DIALOG_OK;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SHOW_MULTIPLAYER_CONNECTION_DIALOG;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SHOW_MULTIPLAYER_SESSION_DIALOG;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RETURN_TO_GAME;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SERIAL_CREATE_GAME;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SERIAL_JOIN_GAME;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SERIAL_RESET_SETTINGS;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SELECT_SERIAL_HOST_DRIVER;
import static ua.millfreedom.rom2.text.DialogsText.CANCEL_1;
import static ua.millfreedom.rom2.text.DialogsText.CLICK_HERE_TO_JOIN_128;
import static ua.millfreedom.rom2.text.DialogsText.CLICK_HERE_TO_START_THE_GAME_AND_WAIT_FOR_YOUR_OPPONENT_129;
import static ua.millfreedom.rom2.text.DialogsText.COM_PORT_106;
import static ua.millfreedom.rom2.text.DialogsText.COM_PORT_111;
import static ua.millfreedom.rom2.text.DialogsText.CREATE_123;
import static ua.millfreedom.rom2.text.DialogsText.DTR_102;
import static ua.millfreedom.rom2.text.DialogsText.EVEN_PARITY_96;
import static ua.millfreedom.rom2.text.DialogsText.FLOW_CONTROL_109;
import static ua.millfreedom.rom2.text.DialogsText.FLOW_CONTROL_114;
import static ua.millfreedom.rom2.text.DialogsText.JOIN_122;
import static ua.millfreedom.rom2.text.DialogsText.MARK_PARITY_97;
import static ua.millfreedom.rom2.text.DialogsText.NONE_103;
import static ua.millfreedom.rom2.text.DialogsText.NO_PARITY_94;
import static ua.millfreedom.rom2.text.DialogsText.ODD_PARITY_95;
import static ua.millfreedom.rom2.text.DialogsText.RESET_SETTINGS_130;
import static ua.millfreedom.rom2.text.DialogsText.RTS_101;
import static ua.millfreedom.rom2.text.DialogsText.RTS_DTR_105;
import static ua.millfreedom.rom2.text.DialogsText.SELECT_BAUD_RATE_107;
import static ua.millfreedom.rom2.text.DialogsText.SELECT_BAUD_RATE_112;
import static ua.millfreedom.rom2.text.DialogsText.SELECT_PARITY_108;
import static ua.millfreedom.rom2.text.DialogsText.SELECT_PARITY_113;
import static ua.millfreedom.rom2.text.DialogsText.SERIAL_SETTINGS_148;
import static ua.millfreedom.rom2.text.DialogsText.STOP_BITS_110;
import static ua.millfreedom.rom2.text.DialogsText.STOP_BITS_115;
import static ua.millfreedom.rom2.text.DialogsText.THIS_WILL_RESET_THE_SETTINGS_TO_THEIR_DEFAULT_VALUES_131;
import static ua.millfreedom.rom2.text.DialogsText.VALUE_1_5_BITS_99;
import static ua.millfreedom.rom2.text.DialogsText.VALUE_1_BIT_98;
import static ua.millfreedom.rom2.text.DialogsText.VALUE_2_BITS_100;
import static ua.millfreedom.rom2.text.DialogsText.XON_XOFF_104;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.TextTableId.DIALOGS;

/**
 * Native class: SerialSettingsDialogVisualObject.
 * Purpose: centered serial settings dialog with COM-port, baud-rate, stop-bit, parity, and flow-control selections.
 */
public class SerialSettingsDialogVisualObject extends CenteredDialogVisualObject {
    private static final int COM_PORT_PANEL_ID = 1;
    private static final int BAUD_RATE_PANEL_ID = 2;
    private static final int STOP_BITS_PANEL_ID = 3;
    private static final int PARITY_PANEL_ID = 4;
    private static final int FLOW_CONTROL_PANEL_ID = 5;
    private static final int COM_PORT_LABEL_ID = 0x1E;
    private static final int BAUD_RATE_LABEL_ID = 0x1F;
    private static final int PARITY_LABEL_ID = 0x20;
    private static final int FLOW_CONTROL_LABEL_ID = 0x21;
    private static final int STOP_BITS_LABEL_ID = 0x22;
    private static final int JOIN_BUTTON_ID = 10;
    private static final int CREATE_BUTTON_ID = 11;
    private static final int RESET_BUTTON_ID = 12;
    private static final int CANCEL_BUTTON_ID = 13;

    public static final int NATIVE_SIZE = 0x6C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    //0x64
    public ComPortSettings serialSettings;

    /**
     * Native: SerialSettingsDialogVisualObject::SerialSettingsDialogVisualObject @00447E85.
     * Fully ported.
     */
    public SerialSettingsDialogVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            ComPortSettings serialSettings
    ) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        this.serialSettings = serialSettings;
        if (getClass() == SerialSettingsDialogVisualObject.class) {
            initialize();
        }
    }

    /**
     * vtbl +0x78: SerialSettingsDialogVisualObject::Initialize @00447EC5.
     * Fully ported.
     */
    @Override
    public void initialize() {
        CBitmapFont dialogFont = Globals.fonts.font1;
        addChild(new DialogWindowVisualObject(
                -1,
                0x28,
                0x14,
                cRect.width() - 0x28,
                0x2C,
                get(DIALOGS, SERIAL_SETTINGS_148),
                dialogFont,
                Palettes.grayDim,
                0x2
        ));

        CRect labelRect = new CRect(0x28, 0x38, 0xD0, 0x3C);
        addLabel(COM_PORT_LABEL_ID, labelRect, COM_PORT_106, dialogFont);
        offsetRectVertically(labelRect, 0x28);
        addLabel(BAUD_RATE_LABEL_ID, labelRect, SELECT_BAUD_RATE_107, dialogFont);
        offsetRectVertically(labelRect, 0x28);
        addLabel(PARITY_LABEL_ID, labelRect, SELECT_PARITY_108, dialogFont);
        offsetRectVertically(labelRect, 0x28);
        addLabel(FLOW_CONTROL_LABEL_ID, labelRect, FLOW_CONTROL_109, dialogFont);
        offsetRectVertically(labelRect, 0x28);
        addLabel(STOP_BITS_LABEL_ID, labelRect, STOP_BITS_110, dialogFont);

        ScrollablePanelVisualObject comPortPanel = new ScrollablePanelVisualObject(
                COM_PORT_PANEL_ID,
                0xE4,
                0x38,
                cRect.width() - 0x2C,
                0xB0,
                get(DIALOGS, COM_PORT_111)
        );
        addChild(comPortPanel);
        addSelectionRows(comPortPanel, "COM1", "COM2", "COM3", "COM4");
        setSelectionRow(comPortPanel, serialSettings.comPortNumber - 1);

        CRect panelRect = new CRect(comPortPanel.getRect());
        offsetRectVertically(panelRect, 0x28);
        panelRect.bottom = panelRect.top + 0xC0;
        ScrollablePanelVisualObject baudRatePanel = new ScrollablePanelVisualObject(
                BAUD_RATE_PANEL_ID,
                panelRect.left,
                panelRect.top,
                panelRect.right,
                panelRect.bottom,
                get(DIALOGS, SELECT_BAUD_RATE_112)
        );
        addChild(baudRatePanel);
        addSelectionRows(baudRatePanel, "14400", "19200", "38400", "56000", "57600", "115200", "128000", "256000");
        setSelectionRow(baudRatePanel, resolveBaudRateSelection(serialSettings.baudRate));

        offsetRectVertically(panelRect, 0x28);
        panelRect.bottom = panelRect.top + 0x78;
        ScrollablePanelVisualObject parityPanel = new ScrollablePanelVisualObject(
                PARITY_PANEL_ID,
                panelRect.left,
                panelRect.top,
                panelRect.right,
                panelRect.bottom,
                get(DIALOGS, SELECT_PARITY_113)
        );
        addChild(parityPanel);
        addSelectionRows(
                parityPanel,
                get(DIALOGS, NO_PARITY_94),
                get(DIALOGS, ODD_PARITY_95),
                get(DIALOGS, EVEN_PARITY_96),
                get(DIALOGS, MARK_PARITY_97)
        );
        setSelectionRow(parityPanel, serialSettings.paritySelection);

        offsetRectVertically(panelRect, 0x28);
        panelRect.bottom = panelRect.top + 0x78;
        ScrollablePanelVisualObject flowControlPanel = new ScrollablePanelVisualObject(
                FLOW_CONTROL_PANEL_ID,
                panelRect.left,
                panelRect.top,
                panelRect.right,
                panelRect.bottom,
                get(DIALOGS, FLOW_CONTROL_114)
        );
        addChild(flowControlPanel);
        addSelectionRows(
                flowControlPanel,
                get(DIALOGS, NONE_103),
                get(DIALOGS, XON_XOFF_104),
                get(DIALOGS, RTS_101),
                get(DIALOGS, DTR_102),
                get(DIALOGS, RTS_DTR_105)
        );
        setSelectionRow(flowControlPanel, serialSettings.flowControlSelection);

        offsetRectVertically(panelRect, 0x28);
        panelRect.bottom = panelRect.top + 0x48;
        ScrollablePanelVisualObject stopBitsPanel = new ScrollablePanelVisualObject(
                STOP_BITS_PANEL_ID,
                panelRect.left,
                panelRect.top,
                panelRect.right,
                panelRect.bottom,
                get(DIALOGS, STOP_BITS_115)
        );
        addChild(stopBitsPanel);
        addSelectionRows(
                stopBitsPanel,
                get(DIALOGS, VALUE_1_BIT_98),
                get(DIALOGS, VALUE_1_5_BITS_99),
                get(DIALOGS, VALUE_2_BITS_100)
        );
        setSelectionRow(stopBitsPanel, serialSettings.stopBitsSelection);

        CRect buttonRect = new CRect(0x3C, 0x110, 0x141, 0x128);
        CommandButtonVisualObject joinButton = createButton(
                JOIN_BUTTON_ID,
                buttonRect,
                get(DIALOGS, JOIN_122),
                SERIAL_JOIN_GAME,
                get(DIALOGS, CLICK_HERE_TO_JOIN_128),
                dialogFont
        );
        addChild(joinButton);

        offsetRectVertically(buttonRect, 0x1E);
        CommandButtonVisualObject createButton = createButton(
                CREATE_BUTTON_ID,
                buttonRect,
                get(DIALOGS, CREATE_123),
                SERIAL_CREATE_GAME,
                get(DIALOGS, CLICK_HERE_TO_START_THE_GAME_AND_WAIT_FOR_YOUR_OPPONENT_129),
                dialogFont
        );
        addChild(createButton);
        if (!Globals.hasValidGameMedia) {
            createButton.setStateFlag(0x1, 0);
        }
        linkButtonsHorizontally(createButton, joinButton);

        offsetRectVertically(buttonRect, 0x1E);
        CommandButtonVisualObject resetButton = createButton(
                RESET_BUTTON_ID,
                buttonRect,
                get(DIALOGS, RESET_SETTINGS_130),
                SERIAL_RESET_SETTINGS,
                get(DIALOGS, THIS_WILL_RESET_THE_SETTINGS_TO_THEIR_DEFAULT_VALUES_131),
                dialogFont
        );
        addChild(resetButton);
        linkButtonsHorizontally(resetButton, createButton);

        offsetRectVertically(buttonRect, 0x1E);
        CommandButtonVisualObject cancelButton = createButton(
                CANCEL_BUTTON_ID,
                buttonRect,
                get(DIALOGS, CANCEL_1),
                RETURN_TO_GAME,
                "",
                dialogFont
        );
        addChild(cancelButton);
        linkButtonsHorizontally(cancelButton, resetButton);
    }

    /**
     * vtbl +0x48: SerialSettingsDialogVisualObject::OnMessage @00448E48.
     * Fully ported. CLlDriver serial connect/server calls remain explicit support boundaries; native child destruction
     * in the reset branch is represented by Java child detachment.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        switch (msg) {
            case RETURN_TO_GAME:
                int result = super.onMessage(msg, wParam, lParam);
                Globals.mainWindow.postMessage(SHOW_MULTIPLAYER_CONNECTION_DIALOG, 0, 0);
                return result;
            case SERIAL_JOIN_GAME:
                writeSelectionsToSettings();
                if (CLlDriver.prepareSerialConnectBoundary(copySerialSettingsToConnectionOption())) {
                    Globals.mainWindow.postMessage(SHOW_MULTIPLAYER_SESSION_DIALOG, 0, 0);
                    super.onMessage(DIALOG_OK, 0, 0);
                }
                return 1;
            case SERIAL_CREATE_GAME:
                writeSelectionsToSettings();
                if (CLlDriver.startSerialServerBoundary(
                        readMainWindowPlayerName(),
                        copySerialSettingsToConnectionOption()
                )) {
                    super.onMessage(DIALOG_OK, 0, 0);
                    Globals.mainWindow.postMessage(SELECT_SERIAL_HOST_DRIVER, 0, 0);
                }
                return 1;
            case SERIAL_RESET_SETTINGS:
                clearChildren();
                initialize();
                switchEnabledChild(getChildById(RESET_BUTTON_ID), false);
                draw();
                return 1;
            default:
                return super.onMessage(msg, wParam, lParam);
        }
    }

    /**
     * Native: SerialSettingsDialogVisualObject::WriteSelectionsToSettings @00448AB3.
     * Fully ported.
     */
    private void writeSelectionsToSettings() {
        serialSettings.comPortNumber = parseComPortNumber(readSelectionText(COM_PORT_PANEL_ID));
        serialSettings.baudRate = parseAtoi(readSelectionText(BAUD_RATE_PANEL_ID));

        String stopBits = readSelectionText(STOP_BITS_PANEL_ID);
        if (stopBits.equals(get(DIALOGS, VALUE_1_BIT_98))) {
            serialSettings.stopBitsSelection = 0;
        } else if (stopBits.equals(get(DIALOGS, VALUE_1_5_BITS_99))) {
            serialSettings.stopBitsSelection = 1;
        } else if (stopBits.equals(get(DIALOGS, VALUE_2_BITS_100))) {
            serialSettings.stopBitsSelection = 2;
        }

        String parity = readSelectionText(PARITY_PANEL_ID);
        if (parity.equals(get(DIALOGS, NO_PARITY_94))) {
            serialSettings.paritySelection = 0;
        } else if (parity.equals(get(DIALOGS, ODD_PARITY_95))) {
            serialSettings.paritySelection = 1;
        } else if (parity.equals(get(DIALOGS, EVEN_PARITY_96))) {
            serialSettings.paritySelection = 2;
        } else if (parity.equals(get(DIALOGS, MARK_PARITY_97))) {
            serialSettings.paritySelection = 3;
        }

        String flowControl = readSelectionText(FLOW_CONTROL_PANEL_ID);
        if (flowControl.equals(get(DIALOGS, RTS_DTR_105))) {
            serialSettings.flowControlSelection = 4;
        } else if (flowControl.equals(get(DIALOGS, RTS_101))) {
            serialSettings.flowControlSelection = 2;
        } else if (flowControl.equals(get(DIALOGS, DTR_102))) {
            serialSettings.flowControlSelection = 3;
        } else if (flowControl.equals(get(DIALOGS, NONE_103))) {
            serialSettings.flowControlSelection = 0;
        } else if (flowControl.equals(get(DIALOGS, XON_XOFF_104))) {
            serialSettings.flowControlSelection = 1;
        }
    }

    /**
     * Native helper block inside SerialSettingsDialogVisualObject::Initialize @00447EC5.
     * not ported.
     */
    private void addLabel(int id, CRect rect, DialogsText textId, CBitmapFont dialogFont) {
        addChild(new DialogWindowVisualObject(
                id,
                rect.left,
                rect.top,
                rect.right,
                rect.bottom,
                get(DIALOGS, textId),
                dialogFont,
                Palettes.grayDim,
                0
        ));
    }

    /**
     * Java helper for repeated row appends in SerialSettingsDialogVisualObject::Initialize @00447EC5.
     * not ported.
     */
    private static void addSelectionRows(ScrollablePanelVisualObject panel, String... rows) {
        for (String row : rows) {
            panel.addRow(row);
        }
    }

    /**
     * Java helper for repeated initial-row restores in SerialSettingsDialogVisualObject::Initialize @00447EC5.
     * not ported.
     */
    private static void setSelectionRow(ScrollablePanelVisualObject panel, int selectedRow) {
        panel.setSelectedRow(selectedRow);
    }

    /**
     * Java helper for `FUN_004D4AAB` neighbor wiring in SerialSettingsDialogVisualObject::Initialize @00447EC5.
     * not ported.
     */
    private static void linkButtonsHorizontally(CVisualObject button, CVisualObject leftNeighbor) {
        button.leftNeighbor = leftNeighbor;
        leftNeighbor.rightNeighbor = button;
    }

    /**
     * Java helper for the `CRect` y-offset increments in SerialSettingsDialogVisualObject::Initialize @00447EC5.
     * not ported.
     */
    private static void offsetRectVertically(CRect rect, int deltaY) {
        rect.top += deltaY;
        rect.bottom += deltaY;
    }

    /**
     * Native support extracted from the stack-buffer text reads in
     * SerialSettingsDialogVisualObject::WriteSelectionsToSettings @00448AB3.
     * Fully ported.
     */
    private String readSelectionText(int panelId) {
        ScrollablePanelVisualObject panel = (ScrollablePanelVisualObject) getChildById(panelId);
        StringBuilder textBuffer = new StringBuilder();
        panel.copyHeaderText(textBuffer);
        return textBuffer.toString();
    }

    /**
     * Native support extracted from the stack LlDriverConnectionOption setup in
     * SerialSettingsDialogVisualObject::OnMessage @00448E48.
     * Fully ported.
     */
    private LlDriverConnectionOption copySerialSettingsToConnectionOption() {
        LlDriverConnectionOption connectionOption = new LlDriverConnectionOption();
        ByteBuffer buffer = ByteBuffer.wrap(connectionOption.directPlayAddressData).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(serialSettings.comPortNumber);
        buffer.putInt(serialSettings.baudRate);
        buffer.putInt(serialSettings.stopBitsSelection);
        buffer.putInt(serialSettings.paritySelection);
        buffer.putInt(serialSettings.flowControlSelection);
        return connectionOption;
    }

    /**
     * Native helper branch inside SerialSettingsDialogVisualObject::Initialize @00447EC5.
     * not ported.
     */
    private static int resolveBaudRateSelection(int baudRate) {
        if (baudRate == 0x4B00) {
            return 1;
        }
        if (baudRate == 0x9600) {
            return 2;
        }
        if (baudRate == 56000) {
            return 3;
        }
        if (baudRate == 0xE100) {
            return 4;
        }
        if (baudRate == 0x1C200) {
            return 5;
        }
        if (baudRate == 0x1F400) {
            return 6;
        }
        if (baudRate == 0x3E800) {
            return 7;
        }
        return 0;
    }

    /**
     * Java helper for the repeated CommandButtonVisualObject constructor branches in SerialSettingsDialogVisualObject::Initialize @00447EC5.
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
     * Native support boundary for `AfxGetMainWnd()->m_GameSession.m_PlayerName` used by SerialSettingsDialogVisualObject::OnMessage @00448E48.
     * Fully ported.
     */
    private static String readMainWindowPlayerName() {
        return Globals.mainWindow.m_GameSession.m_PlayerName;
    }

    /**
     * Java helper for the numeric row parsing branches inside FUN_00448AB3 @00448AB3.
     * not ported.
     */
    private static int parseAtoi(String value) {
        if (value.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    /**
     * Native helper branch inside FUN_00448AB3 @00448AB3.
     * not ported.
     */
    private static int parseComPortNumber(String value) {
        if (value.length() > 3) {
            return parseAtoi(value.substring(3));
        }
        return parseAtoi(value);
    }

}
