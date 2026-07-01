package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.window.CMainWindow;

import static ua.millfreedom.rom2.model.window.CMainWindow.SESSION_MODE_DEDICATED_SERVER;

/**
 * Native support extracted from the `APPEND_DEDICATED_SERVER_LOG_LINE` branch in CMainWindow::WindowProc @004852D8.
 */
public final class AppendDedicatedServerLogLineMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private AppendDedicatedServerLogLineMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     * Partial port. Preserves the recovered fullscreen-vs-windowed split: dedicated fullscreen hosting appends a gray
     * timed line to `MapVisualObject +0xA28`, while the windowed path appends/selects the decoded line in `cListBox1`.
     * Native `GetInt`/`OemToCharA` preprocessing is approximated by reusing the already-decoded Java string payload.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        String lineText = CMainWindowWindowProcSupport.readMessageText(wParam);
        if (Globals.isWindowed == 0) {
            if (mainWindow.sessionMode == SESSION_MODE_DEDICATED_SERVER) {
                mainWindow.pMapVisualObject.gameListControl.addTimedLine(
                        lineText,
                        Palettes.gray,
                        30000
                );
            }
            return 1;
        }
        int rowIndex = mainWindow.cListBox1.addString(lineText);
        mainWindow.cListBox1.setCurSel(rowIndex);
        return 1;
    }
}
