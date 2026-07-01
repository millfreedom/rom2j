package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.net.CLlDriver;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.LlDriverProtocolOption;
import ua.millfreedom.rom2.model.IntPointer;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.enums.ProtocolId;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.window.CMainWindow;

import java.util.ArrayList;
import java.util.List;

import static ua.millfreedom.rom2.model.enums.MessageCodes.DIALOG_OK;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SHOW_TCP_IP_SETTINGS_DIALOG;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SHOW_MODEM_SETTINGS_DIALOG;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SHOW_SERIAL_SETTINGS_DIALOG;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SHOW_MULTIPLAYER_SESSION_DIALOG;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SHOW_MAIN_MENU;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RETURN_TO_GAME;
import static ua.millfreedom.rom2.text.DialogsText.AVAILABLE_PROTOCOLS_81;
import static ua.millfreedom.rom2.text.DialogsText.SELECT_CONNECTION_80;
import static ua.millfreedom.rom2.text.DialogsText.SELECT_CONNECTION_FOR_MULTIPLAYER_GAME_146;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_DIRECTPLAY3_NOT_DETECTED_PLEASE_INSTALL_DIRECTX_6_1_152;
import static ua.millfreedom.rom2.text.TextTableId.DIALOGS;

/**
 * Native class: MpConnectionDialogVisualObject.
 * Purpose: header dialog for multiplayer connection protocol selection.
 */
public class MpConnectionDialogVisualObject extends HeaderDialogVisualObject {
    private static final int PROTOCOL_LIST_ID = 2;
    private static final int PROTOCOL_SCROLLBAR_ID = 10;

    public static final int NATIVE_SIZE = 0x80; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    //0x78
    public final List<LlDriverProtocolOption> availableProtocols = new ArrayList<>();
    //0x7c
    public IntPointer selectedProtocolIndexPointer;

    /**
     * Native: MpConnectionDialogVisualObject::MpConnectionDialogVisualObject @00445004.
     * Fully ported.
     */
    public MpConnectionDialogVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            IntPointer selectedProtocolIndexPointer
    ) {
        super(
                id,
                xLeft,
                yTop,
                xRight,
                yBottom,
                selectedProtocolIndexPointer,
                get(DIALOGS, SELECT_CONNECTION_80),
                1,
                get(DIALOGS, SELECT_CONNECTION_FOR_MULTIPLAYER_GAME_146)
        );
        this.selectedProtocolIndexPointer = selectedProtocolIndexPointer;
    }

    /**
     * vtbl +0x88: MpConnectionDialogVisualObject::createDialogContent @004451FE.
     * Fully ported.
     */
    @Override
    protected CVisualObject createDialogContent(@SuppressWarnings("unused") Object payload, CRect contentRect) {
        contentRect.right -= 0x18;
        TextListVisualObject availableProtocolsList = new TextListVisualObject(
                PROTOCOL_LIST_ID,
                new CRect(contentRect),
                Globals.fonts.font1,
                Palettes.grayDim,
                Palettes.gray,
                PROTOCOL_SCROLLBAR_ID,
                get(DIALOGS, AVAILABLE_PROTOCOLS_81)
        );
        addChild(availableProtocolsList);

        CRect listRect = availableProtocolsList.getRect();
        addChild(new PostSetupVisualObject(
                PROTOCOL_SCROLLBAR_ID,
                listRect.right,
                listRect.top,
                listRect.right + 0x18,
                listRect.bottom,
                null
        ));

        refreshAvailableProtocols();
        populateAvailableProtocolRows(availableProtocolsList);
        syncSelectedProtocolIndex(availableProtocolsList);
        availableProtocolsList.gameDialogControls = getChildById(-1);
        return availableProtocolsList;
    }

    /**
     * vtbl +0x8C: MpConnectionDialogVisualObject::OnHeaderDialogAction @0044506F.
     * Fully ported.
     */
    @Override
    protected void onHeaderDialogAction(MessageCodes action) {
        if (action == DIALOG_OK) {
            commitSelectedProtocol();
            return;
        }
        if (action == RETURN_TO_GAME) {
            Globals.mainWindow.postMessage(SHOW_MAIN_MENU, 0, 0);
        }
    }

    /**
     * Native helper block inside MpConnectionDialogVisualObject::createDialogContent @004451FE.
     * Java keeps raw TCP/IP visible when DirectPlay providers are hidden because it is the implemented TCP route.
     */
    private void refreshAvailableProtocols() {
        availableProtocols.clear();
        CLlDriver.loadAvailableProtocols(availableProtocols);

        boolean hideSerialAndModem =
                Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_DEDICATED_SERVER;
        boolean hideRawTcpIp = shouldHideRawTcpIpProtocol(availableProtocols);
        List<LlDriverProtocolOption> filteredProtocols = new ArrayList<>();
        for (LlDriverProtocolOption availableProtocol : availableProtocols) {
            int protocolId = availableProtocol.protocolId;
            if ((protocolId == ProtocolId.TCP_IP && hideRawTcpIp)
                    || (hideSerialAndModem
                    && (protocolId == ProtocolId.DPSP_MODEM
                    || protocolId == ProtocolId.DPSP_SERIAL))) {
                continue;
            }
            filteredProtocols.add(availableProtocol);
        }

        availableProtocols.clear();
        availableProtocols.addAll(filteredProtocols);
    }

    /**
     * Native support extracted from MpConnectionDialogVisualObject::createDialogContent @004451FE.
     * Native hides raw TCP/IP when DirectPlay provider rows are available; Java leaves it visible in TCP-only mode.
     */
    private static boolean shouldHideRawTcpIpProtocol(List<LlDriverProtocolOption> protocols) {
        for (LlDriverProtocolOption protocol : protocols) {
            if (protocol.protocolId != ProtocolId.TCP_IP) {
                return true;
            }
        }
        return false;
    }

    /**
     * Native helper block inside MpConnectionDialogVisualObject::createDialogContent @004451FE.
     * Fully ported. The native CStringArray fan-out is modeled directly through TextListVisualObject rows.
     */
    private void populateAvailableProtocolRows(TextListVisualObject availableProtocolsList) {
        availableProtocolsList.rows.clear();
        for (LlDriverProtocolOption availableProtocol : availableProtocols) {
            availableProtocolsList.rows.add(normalizeProtocolDisplayName(availableProtocol.displayName));
        }
    }

    /**
     * Native helper block inside MpConnectionDialogVisualObject::createDialogContent @004451FE.
     * Fully ported.
     */
    private void syncSelectedProtocolIndex(TextListVisualObject availableProtocolsList) {
        int rowCount = availableProtocolsList.getRowCount();
        int selectedProtocolIndex = readSelectedProtocolIndex();
        int maxProtocolIndex = rowCount - 1;
        if (Integer.compareUnsigned(selectedProtocolIndex, maxProtocolIndex) >= 0) {
            selectedProtocolIndex = maxProtocolIndex;
        }
        writeSelectedProtocolIndex(selectedProtocolIndex);
        availableProtocolsList.selectedRow = selectedProtocolIndex;
    }

    /**
     * Native helper block inside MpConnectionDialogVisualObject::OnHeaderDialogAction @0044506F.
     * Fully ported.
     */
    private void commitSelectedProtocol() {
        TextListVisualObject availableProtocolsList = (TextListVisualObject) getChildById(PROTOCOL_LIST_ID);
        int selectedProtocolIndex = availableProtocolsList.selectedRow;
        writeSelectedProtocolIndex(selectedProtocolIndex);

        LlDriverProtocolOption selectedProtocol = availableProtocols.get(selectedProtocolIndex);
        int protocolId = selectedProtocol.protocolId;
        if (!CLlDriver.setProtocolId(protocolId)) {
            showProtocolUnavailablePrompt();
            Globals.mainWindow.postMessage(SHOW_MAIN_MENU, 0, 0);
            return;
        }

        MessageCodes selectedProtocolMessage = resolveProtocolSelectionMessage(protocolId);
        if (selectedProtocolMessage != null) {
            Globals.mainWindow.postMessage(selectedProtocolMessage, 0, 0);
        }
    }

    /**
     * Native helper block inside MpConnectionDialogVisualObject::OnHeaderDialogAction @0044506F.
     * Fully ported.
     */
    private void showProtocolUnavailablePrompt() {
        HeaderDialogVariantVisualObject unavailablePrompt = new HeaderDialogVariantVisualObject(
                1,
                100,
                100,
                0x21C,
                0x17C,
                get(MAIN_DIRECTPLAY3_NOT_DETECTED_PLEASE_INSTALL_DIRECTX_6_1_152),
                null,
                0
        );
        Globals.mainWindow.showDialog(unavailablePrompt);
    }

    /**
     * Native helper branch inside MpConnectionDialogVisualObject::OnHeaderDialogAction @0044506F.
     * Java also routes the visible raw TCP/IP row to the TCP/IP settings dialog.
     */
    private MessageCodes resolveProtocolSelectionMessage(int protocolId) {
        return switch (protocolId) {
            case ProtocolId.TCP_IP -> SHOW_TCP_IP_SETTINGS_DIALOG;
            case ProtocolId.DPSP_TCPIP -> SHOW_TCP_IP_SETTINGS_DIALOG;
            case ProtocolId.DPSP_SERIAL -> SHOW_SERIAL_SETTINGS_DIALOG;
            case ProtocolId.DPSP_MODEM -> SHOW_MODEM_SETTINGS_DIALOG;
            case ProtocolId.DPSP_IPX -> SHOW_MULTIPLAYER_SESSION_DIALOG;
            default -> null;
        };
    }

    /**
     * Native support extracted from MpConnectionDialogVisualObject::createDialogContent @004451FE and
     * MpConnectionDialogVisualObject::OnHeaderDialogAction @0044506F for the selected protocol pointer at `+0x7C`.
     * Fully ported through the Java IntPointer backing field.
     */
    private int readSelectedProtocolIndex() {
        return selectedProtocolIndexPointer.get();
    }

    /**
     * Native support extracted from MpConnectionDialogVisualObject::createDialogContent @004451FE and
     * MpConnectionDialogVisualObject::OnHeaderDialogAction @0044506F for the selected protocol pointer at `+0x7C`.
     * Fully ported through the Java IntPointer backing field.
     */
    private void writeSelectedProtocolIndex(int selectedProtocolIndex) {
        selectedProtocolIndexPointer.set(selectedProtocolIndex);
    }

    /**
     * Native helper block inside MpConnectionDialogVisualObject::createDialogContent @004451FE using
     * NormalizeHotKeyInput @00474C85.
     * Fully ported. Java rebuilds the display row through StringBuilder instead of CString.
     */
    private static String normalizeProtocolDisplayName(String protocolDisplayName) {
        StringBuilder normalizedName = new StringBuilder(protocolDisplayName.length());
        for (int index = 0; index < protocolDisplayName.length(); index++) {
            normalizedName.append((char) normalizeHotKeyInput(protocolDisplayName.charAt(index)));
        }
        return normalizedName.toString();
    }

    /**
     * Native helper: NormalizeHotKeyInput @00474C85.
     */
    private static int normalizeHotKeyInput(int c) {
        int value = c & 0xFF;
        if (Globals.useCustomEncoding && value > 0x7F) {
            if (value >= 0xC0 && value <= 0xEF) {
                return value - 0x40;
            }
            if (value > 0xEF) {
                return value - 0x10;
            }
        }
        return value;
    }
}
