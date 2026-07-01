package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CServerApp;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.visobj.HeaderDialogVariantVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.model.window.MessageSystem;
import ua.millfreedom.rom2.text.GameTexts;
import ua.millfreedom.rom2.text.MainText;
import ua.millfreedom.rom2.text.PatchText;

import static ua.millfreedom.rom2.text.TextTableId.MAIN;

/**
 * Native support extracted from the `RUN_MULTIPLAYER_SESSION_BOOTSTRAP` branch in CMainWindow::WindowProc @004852D8.
 */
public final class RunMultiplayerSessionBootstrapMessageHandler {
    private static final int ESCAPE_BOOTSTRAP_ABORT_STATUS_WORD = 0x100C;
    private static final int SERVER_LOOP_BROADCAST_INTERVAL_MS = 1000;
    private static final int SESSION_STATUS_LOW_BYTE_MASK = 0xFF;
    private static final int MULTIPLAYER_BOOTSTRAP_STATUS_MAIN_TEXT_BASE = 0xC0;

    /**
     * Native support state extracted from the client bootstrap wait loop in CMainWindow::WindowProc @004852D8.
     */
    private enum ClientBootstrapWaitResult {
        COMPLETE,
        ABORTED,
        FAILED
    }

    /**
     * Java utility constructor.
     * not ported.
     */
    private RunMultiplayerSessionBootstrapMessageHandler() {
    }

    /**
     * Native support extracted from the `RUN_MULTIPLAYER_SESSION_BOOTSTRAP` branch in CMainWindow::WindowProc @004852D8.
     * Fully ported at the Java message-pump boundary.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        if (mainWindow.sessionMode != CMainWindow.SESSION_MODE_DEDICATED_SERVER) {
            if (mainWindow.sessionMode == CMainWindow.SESSION_MODE_MULTIPLAYER_CLIENT) {
                initializeHostBootstrapState(mainWindow);
                ClientBootstrapWaitResult waitResult = waitForClientBootstrapChecksum(mainWindow);
                if (waitResult == ClientBootstrapWaitResult.ABORTED) {
                    return 0;
                }
                if (waitResult == ClientBootstrapWaitResult.FAILED) {
                    mainWindow.showDialogAndAwaitResult(createBootstrapFailureDialog());
                    mainWindow.postMessage(MessageCodes.RETURN_TO_MULTIPLAYER_SETUP, 0, 0);
                    return 1;
                }
                if (hasResolvedHostBootstrapFailure(mainWindow)) {
                    mainWindow.showDialogAndAwaitResult(createBootstrapFailureDialog());
                    mainWindow.postMessage(MessageCodes.RETURN_TO_MULTIPLAYER_SETUP, 0, 0);
                }
            }
            if (!mainWindow.m_GameSession.submitCharacterSetupAndWaitForSelectedUnit()) {
                mainWindow.showDialogAndAwaitResult(createBootstrapFailureDialog());
                mainWindow.postMessage(MessageCodes.RETURN_TO_MULTIPLAYER_SETUP, 0, 0);
                return 1;
            }
        }
        if (!mainWindow.runSessionBootstrap(0)) {
            mainWindow.showDialogAndAwaitResult(createBootstrapFailureDialog());
            mainWindow.postMessage(MessageCodes.RETURN_TO_MULTIPLAYER_SETUP, 0, 0);
        }
        return 1;
    }

    /**
     * Native support extracted from the host wait-loop preamble in CMainWindow::WindowProc @004852D8.
     * Fully ported.
     */
    private static void initializeHostBootstrapState(CMainWindow mainWindow) {
        Globals.multiplayerBootstrapStatusWord = PatchText.SERVER_IS_NOT_RESPONDING_0.index();
        mainWindow.fileTransferChecksumMatched = 0;
        mainWindow.pMapVisualObject.notifyMapChunkTransferComplete();
    }

    /**
     * Native support extracted from the client wait loop in CMainWindow::WindowProc @004852D8.
     * Fully ported at the Java message-pump boundary.
     */
    private static ClientBootstrapWaitResult waitForClientBootstrapChecksum(CMainWindow mainWindow) {
        do {
            if (mainWindow.fileTransferChecksumMatched != 0) {
                return ClientBootstrapWaitResult.COMPLETE;
            }
            if (CServerApp.getPendingSegmentMarkerCount() == 0) {
                int currentTick = Globals.currentTickMillis();
                if (Math.abs(currentTick - Globals.lastRemoteServerLoopCounterBroadcastTick)
                        > SERVER_LOOP_BROADCAST_INTERVAL_MS) {
                    CServerApp.broadcastRemoteServerLoopCounter(1);
                    Globals.lastRemoteServerLoopCounterBroadcastTick = currentTick;
                }
            } else if (!mainWindow.pMapVisualObject.handleGameAction(null, 100)) {
                return ClientBootstrapWaitResult.FAILED;
            }
            MessageSystem.pumpPostedMessage();
            Globals.mousePointer.update();
            if (Globals.escapeKeyDown) {
                Globals.multiplayerBootstrapStatusWord = ESCAPE_BOOTSTRAP_ABORT_STATUS_WORD;
                mainWindow.postMessage(MessageCodes.RETURN_TO_MULTIPLAYER_SETUP, 0, 0);
                return ClientBootstrapWaitResult.ABORTED;
            }
        } while (Globals.multiplayerBootstrapStatusWord == PatchText.SERVER_IS_NOT_RESPONDING_0.index());
        return ClientBootstrapWaitResult.COMPLETE;
    }

    /**
     * Native support extracted from the host wait-loop failure exit in CMainWindow::WindowProc @004852D8.
     * Fully ported.
     */
    private static boolean hasResolvedHostBootstrapFailure(CMainWindow mainWindow) {
        return mainWindow.sessionMode == CMainWindow.SESSION_MODE_MULTIPLAYER_CLIENT
                && mainWindow.fileTransferChecksumMatched == 0
                && Globals.multiplayerBootstrapStatusWord
                != PatchText.SERVER_IS_NOT_RESPONDING_0.index();
    }

    /**
     * Native support extracted from the host wait-loop failure prompt in CMainWindow::WindowProc @004852D8.
     * Fully ported.
     */
    private static HeaderDialogVariantVisualObject createBootstrapFailureDialog() {
        return new HeaderDialogVariantVisualObject(
                1,
                0x40,
                100,
                0x17C,
                0x252,
                resolveBootstrapFailureText(),
                null,
                0
        );
    }

    /**
     * Native support extracted from the status-text lookup in CMainWindow::WindowProc @004852D8.
     * Fully ported.
     */
    private static String resolveBootstrapFailureText() {
        int statusLowByte = Globals.multiplayerBootstrapStatusWord & SESSION_STATUS_LOW_BYTE_MASK;
        if (statusLowByte < PatchText.YOUR_CHARACTER_FILE_NOT_FOUND_IT_MAY_BE_DELETED_11.index()) {
            return GameTexts.get(MAIN, MainText.byIndex(MULTIPLAYER_BOOTSTRAP_STATUS_MAIN_TEXT_BASE + statusLowByte));
        }
        return GameTexts.get(PatchText.byIndex(statusLowByte));
    }
}
