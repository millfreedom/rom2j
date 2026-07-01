package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.net.CLlDriver;
import ua.millfreedom.rom2.model.DirectPlayRuntime;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.enums.ProtocolId;
import ua.millfreedom.rom2.model.window.CMainWindow;

import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.INN_DIALOG;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.MODAL_DIALOG;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.SHOP_DIALOG;

/**
 * Native support extracted from the `RETURN_TO_MULTIPLAYER_SETUP` branch in CMainWindow::WindowProc @004852D8.
 */
public final class ReturnToMultiplayerSetupMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private ReturnToMultiplayerSetupMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     * Partial port. Preserves the recovered CString::IsEmpty close routing, modal close forwarding, lobby cleanup,
     * server teardown, and corrected protocol/transport branching.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        if (!mainWindow.connectionScratchState.directAddress.isEmpty()) {
            mainWindow.postMessage(MessageCodes.WM_CLOSE, 0, 0);
            return 1;
        }
        if (mainWindow.sessionMode == CMainWindow.SESSION_MODE_MULTIPLAYER_CLIENT
                || mainWindow.sessionMode == CMainWindow.SESSION_MODE_NETWORK_HOST) {
            if (MODAL_DIALOG.isSetIn(mainWindow.dialogsMask)) {
                mainWindow.inputController.onMessage(MessageCodes.RETURN_TO_GAME, 0, 0);
            }
            if (SHOP_DIALOG.isSetIn(mainWindow.dialogsMask)) {
                mainWindow.inputController.onMessage(MessageCodes.DIALOG_OK, 0, 0);
            }
            if (INN_DIALOG.isSetIn(mainWindow.dialogsMask)) {
                mainWindow.inputController.onMessage(MessageCodes.DIALOG_OK, 0, 0);
            }
            mainWindow.pMapVisualObject.clearSessionForFirstPlayerLobbyReturn();
            if (GAMEPLAY.isSetIn(mainWindow.dialogsMask)) {
                mainWindow.cleanupGameplayResources();
            }
            mainWindow.destroyServer();
            switch (CLlDriver.getProtocolId()) {
                case ProtocolId.DPSP_SERIAL -> mainWindow.postMessage(MessageCodes.SHOW_SERIAL_SETTINGS_DIALOG, 0, 0);
                case ProtocolId.DPSP_MODEM -> mainWindow.postMessage(MessageCodes.SHOW_MODEM_SETTINGS_DIALOG, 0, 0);
                default -> mainWindow.postMessage(MessageCodes.SHOW_MULTIPLAYER_SESSION_DIALOG, 0, 0);
            }
            return 1;
        }
        if (mainWindow.sessionMode == CMainWindow.SESSION_MODE_DEDICATED_SERVER) {
            int protocolId = CLlDriver.getProtocolId();
            if (protocolId == ProtocolId.DPSP_TCPIP || protocolId == ProtocolId.TCP_IP) {
                mainWindow.postMessage(MessageCodes.SHOW_TCP_IP_SETTINGS_DIALOG, 0, 0);
                mainWindow.destroyServer();
                if (protocolId == ProtocolId.DPSP_TCPIP) {
                    DirectPlayRuntime.probeAvailabilityBoundary();
                }
            } else {
                mainWindow.postMessage(MessageCodes.SHOW_MULTIPLAYER_CONNECTION_DIALOG, 0, 0);
                mainWindow.destroyServer();
            }
            return 1;
        }
        return 1;
    }
}
