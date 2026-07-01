package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.visobj.TownMenuListDialogVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `SHOW_TOWN_MENU` branch in CMainWindow::WindowProc @004852D8.
 */
public final class ShowTownMenuMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private ShowTownMenuMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     */
    public static int handle(CMainWindow mainWindow, @SuppressWarnings("unused") Object wParam, @SuppressWarnings("unused") Object lParam) {
        mainWindow.showDialog(new TownMenuListDialogVisualObject(
                1,
                100,
                100,
                0x1B8,
                0x154,
                new CRect(0, 0, 0xF0, 0x28)
        ));
        return 1;
    }
}
