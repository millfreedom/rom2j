package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.DirectPlayRuntime;
import ua.millfreedom.rom2.model.IntPointer;
import ua.millfreedom.rom2.model.ServerConfig;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.visobj.HeaderDialogVariantVisualObject;
import ua.millfreedom.rom2.model.visobj.MpConnectionDialogVisualObject;
import ua.millfreedom.rom2.model.visobj.TextListVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.text.GameTexts;

import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_DIRECTPLAY3_NOT_DETECTED_PLEASE_INSTALL_DIRECTX_6_1_152;

/**
 * Native support extracted from the `SHOW_MULTIPLAYER_CONNECTION_DIALOG` branch in CMainWindow::WindowProc @004852D8.
 */
public final class ShowMultiplayerConnectionDialogMessageHandler {
    private static final int PROTOCOL_LIST_ID = 2;
    private static final int SERVER_CONFIG_AUTO_PROTOCOL_LIMIT = ServerConfig.CONFIG_PROTOCOL_WSOCK_TCPIP;

    /**
     * Java utility constructor.
     * not ported.
     */
    private ShowMultiplayerConnectionDialogMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     * Fully ported. Preserves the native CString::IsEmpty branch condition, direct-address join handoff, constant-true
     * DirectPlay runtime probe, and standard protocol-selection dialog construction.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        if (!mainWindow.connectionScratchState.directAddress.isEmpty()) {
            mainWindow.continueDirectAddressPlayerJoin();
            return 1;
        }
        if (mainWindow.dialogsMask == 0) {
            mainWindow.musicPlayer.stopPlayback();
            if (!DirectPlayRuntime.isAvailable()) {
                mainWindow.showDialogAndAwaitResult(new HeaderDialogVariantVisualObject(
                        1,
                        100,
                        100,
                        0x21C,
                        0x17C,
                        GameTexts.get(MAIN_DIRECTPLAY3_NOT_DETECTED_PLEASE_INSTALL_DIRECTX_6_1_152),
                        null,
                        0
                ));
                mainWindow.postMessage(MessageCodes.SHOW_MAIN_MENU, 0, 0);
                return 1;
            }
            mainWindow.m_pMPConnectionDialog = new MpConnectionDialogVisualObject(
                    1,
                    0,
                    0,
                    0x280,
                    0x15E,
                    new IntPointer(
                            () -> mainWindow.LastProtocol,
                            value -> mainWindow.LastProtocol = value
                    )
            );
            mainWindow.showDialog(mainWindow.m_pMPConnectionDialog);
            applyCommandLineProtocolSelection(mainWindow);
            applyServerConfigProtocolSelection(mainWindow);
        }
        return 1;
    }

    /**
     * Native support extracted from the `-protocol0`..`-protocol4` and `-protocol` command-line checks in
     * CMainWindow::WindowProc @004852D8.
     */
    private static void applyCommandLineProtocolSelection(CMainWindow mainWindow) {
        String commandLine = Globals.commandLine;
        TextListVisualObject protocolList =
                (TextListVisualObject) mainWindow.m_pMPConnectionDialog.getChildById(PROTOCOL_LIST_ID);
        for (int protocolIndex = 0; protocolIndex <= 4; protocolIndex++) {
            if (commandLine.contains("-protocol" + protocolIndex)) {
                protocolList.setSelectedRow(protocolIndex);
            }
        }
        if (commandLine.contains("-protocol")) {
            mainWindow.m_pMPConnectionDialog.onMessage(MessageCodes.DIALOG_OK, 0, 0);
        }
    }

    /**
     * Native support extracted from the `g_ServerConfig.protocol` branch in CMainWindow::WindowProc @004852D8.
     */
    private static void applyServerConfigProtocolSelection(CMainWindow mainWindow) {
        if (Globals.serverConfig.protocol >= 0
                && Globals.serverConfig.protocol < SERVER_CONFIG_AUTO_PROTOCOL_LIMIT) {
            TextListVisualObject protocolList =
                    (TextListVisualObject) mainWindow.m_pMPConnectionDialog.getChildById(PROTOCOL_LIST_ID);
            protocolList.setSelectedRow(Globals.serverConfig.protocol);
            mainWindow.postMessage(MessageCodes.DIALOG_OK, 0, 0);
        }
    }
}
