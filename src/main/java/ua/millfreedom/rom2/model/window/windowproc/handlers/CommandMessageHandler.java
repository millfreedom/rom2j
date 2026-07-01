package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.model.window.DedicatedServerControlDialog;

/**
 * Native support extracted from CMainWindow::MSGMAP_ENTRY_ARRAY and
 * DedicatedServerControlDialog AFX_MSGMAP_ENTRY_ARRAY_005cdfa0 WM_COMMAND rows.
 */
public final class CommandMessageHandler {
    private static final int DEDICATED_DIALOG_KICK_SELECTED_PLAYER_COMMAND_ID = 0x3F4;
    private static final int DEDICATED_DIALOG_DECREASE_GAME_SPEED_COMMAND_ID = 0x3F5;
    private static final int DEDICATED_DIALOG_INCREASE_GAME_SPEED_COMMAND_ID = 0x3F6;
    private static final int MAIN_WINDOW_CLOSE_COMMAND_ID = 0x9C4E;
    private static final int MAIN_WINDOW_SHOW_DEDICATED_SERVER_DIALOG_COMMAND_ID = 0x9C4F;

    /**
     * Java utility constructor.
     * not ported.
     */
    private CommandMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::MSGMAP_ENTRY_ARRAY and
     * DedicatedServerControlDialog AFX_MSGMAP_ENTRY_ARRAY_005cdfa0 WM_COMMAND dispatch.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        int commandId = CMainWindowWindowProcSupport.readMessageInt(wParam) & 0xFFFF;
        switch (commandId) {
            case DEDICATED_DIALOG_KICK_SELECTED_PLAYER_COMMAND_ID ->
                    DedicatedServerControlDialog.kickSelectedPlayerCommand();
            case DEDICATED_DIALOG_DECREASE_GAME_SPEED_COMMAND_ID ->
                    DedicatedServerControlDialog.decreaseGameSpeedCommand();
            case DEDICATED_DIALOG_INCREASE_GAME_SPEED_COMMAND_ID ->
                    DedicatedServerControlDialog.increaseGameSpeedCommand();
            case MAIN_WINDOW_CLOSE_COMMAND_ID -> mainWindow.postCloseCommand();
            case MAIN_WINDOW_SHOW_DEDICATED_SERVER_DIALOG_COMMAND_ID ->
                    mainWindow.showDedicatedServerControlDialogCommand();
            default -> {
                return mainWindow.inputController.onMessage(MessageCodes.WM_COMMAND, wParam, lParam);
            }
        }
        return 1;
    }
}
