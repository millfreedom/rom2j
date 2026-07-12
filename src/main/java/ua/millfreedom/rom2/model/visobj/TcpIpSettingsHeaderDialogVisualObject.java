package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.CString;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.net.CLlDriver;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palettes;


import static ua.millfreedom.rom2.model.enums.MessageCodes.DIALOG_OK;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SHOW_MULTIPLAYER_CONNECTION_DIALOG;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SHOW_MULTIPLAYER_SESSION_DIALOG;
import static ua.millfreedom.rom2.text.DialogsText.ENTER_COMPUTER_NAME_OR_IP_ADDRESS_OR_LEAVE_THIS_FIELD_BLANK_TO_117;
import static ua.millfreedom.rom2.text.DialogsText.ENTER_THE_COMPUTER_NAME_OR_IP_ADDRESS_OR_LEAVE_IT_BLANK_116;
import static ua.millfreedom.rom2.text.DialogsText.TCP_IP_SETTINGS_149;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.TextTableId.DIALOGS;

/**
 * Native class: TcpIpSettingsHeaderDialogVisualObject (vtbl @0x005CC810).
 * Purpose: TCP/IP settings header dialog with editable connection string.
 */
public class TcpIpSettingsHeaderDialogVisualObject extends HeaderDialogVisualObject {
    public static final int NATIVE_SIZE = 0x7C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    //0x78
    public CString context;

    /**
     * Native: TcpIpSettingsHeaderDialogVisualObject::TcpIpSettingsHeaderDialogVisualObject @004490B6.
     * Fully ported.
     */
    public TcpIpSettingsHeaderDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, CString context) {
        super(id, xLeft, yTop, xRight, yBottom, context, get(DIALOGS, ENTER_THE_COMPUTER_NAME_OR_IP_ADDRESS_OR_LEAVE_IT_BLANK_116), 1, get(DIALOGS, TCP_IP_SETTINGS_149));
        this.context = context;
    }

    /**
     * vtbl +0x88: TcpIpSettingsHeaderDialogVisualObject::createDialogContent @004491C4.
     * Fully ported.
     */
    @Override
    protected CVisualObject createDialogContent(Object payload, CRect contentRect) {
        CString inputContext = (CString) payload;
        StaticTextVisualObject hostInput = new StaticTextVisualObject(
                2,
                contentRect.left,
                contentRect.top,
                contentRect.right,
                contentRect.top + 0x18,
                Globals.fonts.font1,
                Palettes.grayDim,
                get(DIALOGS, ENTER_COMPUTER_NAME_OR_IP_ADDRESS_OR_LEAVE_THIS_FIELD_BLANK_TO_117)
        );
        addChild(hostInput);
        hostInput.setValue(inputContext.toString());
        return hostInput;
    }

    /**
     * vtbl +0x8C: TcpIpSettingsHeaderDialogVisualObject::OnHeaderDialogAction @00449117.
     * Fully ported. CLlDriver network-error cleanup remains an explicit support boundary.
     */
    @Override
    protected void onHeaderDialogAction(MessageCodes action) {
        if (action == DIALOG_OK) {
            StringBuilder selectedConnection = new StringBuilder();
            getChildById(2).getValue(selectedConnection);

            String selectedConnectionText = selectedConnection.toString();
            context.set(selectedConnectionText);
            Globals.mainWindow.postMessage(SHOW_MULTIPLAYER_SESSION_DIALOG, 0, 0);
            Globals.mainWindow.setPendingConnectionString(selectedConnectionText);
            return;
        }

        CLlDriver.handleNetworkErrorAndClose();
        Globals.mainWindow.postMessage(SHOW_MULTIPLAYER_CONNECTION_DIALOG, 0, 0);
    }
}
