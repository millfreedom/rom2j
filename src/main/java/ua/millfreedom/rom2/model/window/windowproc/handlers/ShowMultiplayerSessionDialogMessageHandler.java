package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.net.CLlDriver;
import ua.millfreedom.rom2.model.enums.ProtocolId;
import ua.millfreedom.rom2.model.visobj.CenteredDialogContextArrayVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `SHOW_MULTIPLAYER_SESSION_DIALOG` branch in CMainWindow::WindowProc @004852D8.
 */
public final class ShowMultiplayerSessionDialogMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private ShowMultiplayerSessionDialogMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     * Partially ported: Java additionally prepares raw TCP/IP for the visible TCP-only replacement route.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        int protocolId = CLlDriver.getProtocolId();
        if (protocolId == ProtocolId.DPSP_IPX) {
            CLlDriver.resetDirectPlayConnectionBoundary();
            CLlDriver.prepareForConnectBoundary("", null);
        } else if (protocolId == ProtocolId.DPSP_TCPIP) {
            CLlDriver.resetDirectPlayConnectionBoundary();
            CLlDriver.prepareForConnectBoundary(mainWindow.connectionScratchState.pendingSessionConnectionString, null);
        } else if (protocolId == ProtocolId.TCP_IP) {
            CLlDriver.prepareForConnectBoundary(mainWindow.connectionScratchState.pendingSessionConnectionString, null);
        }
        if (mainWindow.sessionMode == CMainWindow.SESSION_MODE_DEDICATED_SERVER) {
            mainWindow.startDedicatedMultiplayerSession();
        } else {
            mainWindow.pCenteredDialogContextArrayVisualObject = new CenteredDialogContextArrayVisualObject(
                    1,
                    0,
                    0x1E,
                    0x280,
                    0x1C2,
                    mainWindow.multiplayerSessionDialogContext
            );
            mainWindow.showDialog(mainWindow.pCenteredDialogContextArrayVisualObject);
        }
        return 1;
    }
}
