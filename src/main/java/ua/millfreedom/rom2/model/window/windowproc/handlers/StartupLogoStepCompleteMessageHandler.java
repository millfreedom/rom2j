package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.window.CMainWindow;

import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.STARTUP_LOGO;

/**
 * Native support extracted from the `STARTUP_LOGO_STEP_COMPLETE` branch in CMainWindow::WindowProc @004852D8.
 */
public final class StartupLogoStepCompleteMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private StartupLogoStepCompleteMessageHandler() {
    }

    /**
     * Native support extracted from the `STARTUP_LOGO_STEP_COMPLETE` branch in CMainWindow::WindowProc @004852D8.
     * Fully ported.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        int startupStep = CMainWindowWindowProcSupport.readMessageInt(wParam);
        switch (startupStep) {
            case 0, 1 -> {
                mainWindow.pStartupLogoDialogVisualObject.onMessage(MessageCodes.STARTUP_LOGO_SET_STEP, startupStep + 1, 0);
                mainWindow.pStartupLogoDialogVisualObject.onMessage(MessageCodes.STARTUP_LOGO_SET_TIMEOUT, 4000, 0);
                mainWindow.inputController.draw();
            }
            case 2 -> {
                mainWindow.pStartupLogoDialogVisualObject.onMessage(MessageCodes.STARTUP_LOGO_SET_STEP, startupStep + 1, 0);
                mainWindow.pStartupLogoDialogVisualObject.onMessage(MessageCodes.STARTUP_LOGO_SET_TIMEOUT, 6000, 0);
                mainWindow.inputController.draw();
                mainWindow.initializeRuntimeGraphicsAndAudio();
            }
            case 3 -> {
                mainWindow.pStartupLogoDialogVisualObject.onMessage(MessageCodes.STARTUP_LOGO_SET_STEP, startupStep + 1, 0);
                mainWindow.pStartupLogoDialogVisualObject.onMessage(MessageCodes.STARTUP_LOGO_SET_TIMEOUT, 30000, 0);
                mainWindow.inputController.draw();
            }
            case 4 -> {
                hideStartupLogoDialog(mainWindow);
                mainWindow.postMessage(MessageCodes.SHOW_MAIN_MENU, 0, 0);
            }
            case 5 -> {
                hideStartupLogoDialog(mainWindow);
                mainWindow.postMessage(MessageCodes.WM_CLOSE, 0, 0);
            }
            case 6, 7, 8, 9 -> {
                hideStartupLogoDialog(mainWindow);
                mainWindow.postMessage(MessageCodes.START_NEW_GAME, 0, 0);
            }
            default -> {
            }
        }
        return 1;
    }

    /**
     * Native support extracted from the `STARTUP_LOGO_STEP_COMPLETE` branch in CMainWindow::WindowProc @004852D8.
     */
    private static void hideStartupLogoDialog(CMainWindow mainWindow) {
        mainWindow.pStartupLogoDialogVisualObject.hideDialog(MessageCodes.DIALOG_OK);
        mainWindow.inputController.removeChild(mainWindow.pStartupLogoDialogVisualObject);
        mainWindow.dialogsMask = STARTUP_LOGO.excludeIn(mainWindow.dialogsMask);
    }
}
