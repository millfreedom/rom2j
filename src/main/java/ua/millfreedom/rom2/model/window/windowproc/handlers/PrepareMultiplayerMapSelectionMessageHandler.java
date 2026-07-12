package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.CString;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.visobj.MultiplayerMapSelectionDialogVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;


/**
 * Native support extracted from the `PREPARE_MULTIPLAYER_MAP_SELECTION` branch in CMainWindow::WindowProc @004852D8.
 */
public final class PrepareMultiplayerMapSelectionMessageHandler {
    private static final String MAP_COMMAND_LINE_OPTION = "-map ";

    /**
     * Java utility constructor.
     * not ported.
     */
    private PrepareMultiplayerMapSelectionMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     * Fully ported.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        String commandLineMap = getCommandLineMapOverride();
        if (commandLineMap != null) {
            assignMapName(mainWindow.map_, commandLineMap);
            mainWindow.postMessage(MessageCodes.RUN_MULTIPLAYER_SESSION_BOOTSTRAP, 0, 0);
        } else if (!Globals.serverConfig.maps.isEmpty()) {
            assignMapName(mainWindow.map_, Globals.serverConfig.maps.get(Globals.serverConfig.field15_0x8c));
            mainWindow.postMessage(MessageCodes.RUN_MULTIPLAYER_SESSION_BOOTSTRAP, 0, 0);
        } else {
            mainWindow.pMultiplayerMapSelectionDialogVisualObject = new MultiplayerMapSelectionDialogVisualObject(
                    1,
                    0,
                    -0x40,
                    0x280,
                    0x1E0,
                    mainWindow.map_
            );
            mainWindow.showDialog(mainWindow.pMultiplayerMapSelectionDialogVisualObject);
        }
        return 1;
    }

    /**
     * Native support extracted from the `-map ` command-line branch in CMainWindow::WindowProc @004852D8.
     */
    private static String getCommandLineMapOverride() {
        int optionIndex = Globals.commandLine.indexOf(MAP_COMMAND_LINE_OPTION);
        if (optionIndex == -1) {
            return null;
        }
        String mapName = Globals.commandLine.substring(optionIndex + MAP_COMMAND_LINE_OPTION.length());
        int quoteIndex = mapName.indexOf('"');
        if (quoteIndex != 0) {
            mapName = quoteIndex < 0 ? "" : mapName.substring(0, quoteIndex);
        }
        return mapName;
    }

    /**
     * Native support extracted from CString::operator= calls in CMainWindow::WindowProc @004852D8.
     */
    private static void assignMapName(CString target, String mapName) {
        target.set(mapName);
    }
}
