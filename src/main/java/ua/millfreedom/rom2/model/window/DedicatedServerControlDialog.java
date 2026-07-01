package ua.millfreedom.rom2.model.window;

import ua.millfreedom.rom2.model.net.CBufferManager;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CServerApp;
import ua.millfreedom.rom2.model.Player;

/**
 * Native resource-0x6B CDialog subclass allocated by CMainWindow::showDedicatedServerControlDialogCommand @004928CA.
 */
public final class DedicatedServerControlDialog {
    /**
     * Native: DedicatedServerControlDialog::DedicatedServerControlDialog @00492983.
     * Fully ported at the Java CDialog boundary.
     */
    public DedicatedServerControlDialog(CWnd parent) {
    }

    /**
     * Native support boundary for CDialog::Create @00493600 call from
     * CMainWindow::showDedicatedServerControlDialogCommand @004928CA.
     * not ported.
     */
    public void create(int resourceId, CWnd parent) {
    }

    /**
     * Native support boundary for CWnd::SetActiveWindow @004935E0 call from
     * CMainWindow::showDedicatedServerControlDialogCommand @004928CA.
     * not ported.
     */
    public void setActiveWindow() {
    }

    /**
     * Native support boundary for CWnd::DestroyWindow call in CMainWindow::OnDestroy @00482526.
     * Java port status: lifecycle detach only.
     */
    public void destroyWindow() {
    }

    /**
     * Native: DedicatedServerControlDialog::decreaseGameSpeedCommand @004929E6.
     * Java port status: fully ported.
     */
    public static void decreaseGameSpeedCommand() {
        CMainWindow mainWindow = Globals.mainWindow;
        mainWindow.setGameSpeed(mainWindow.gameSpeed - 1);
    }

    /**
     * Native: DedicatedServerControlDialog::increaseGameSpeedCommand @00492A10.
     * Java port status: fully ported.
     */
    public static void increaseGameSpeedCommand() {
        CMainWindow mainWindow = Globals.mainWindow;
        mainWindow.setGameSpeed(mainWindow.gameSpeed + 1);
    }

    /**
     * Native: DedicatedServerControlDialog::kickSelectedPlayerCommand @00492A3A.
     * Java port status: fully ported.
     */
    public static void kickSelectedPlayerCommand() {
        CMainWindow mainWindow = Globals.mainWindow;
        String playerName = mainWindow.gameListBox.getText(mainWindow.gameListBox.getCurSel());
        Player player = Globals.gameServer.playerList.getByName(playerName);
        kickPlayer(player);
    }

    /**
     * Native support extracted from DedicatedServerControlDialog::kickSelectedPlayerCommand @00492A3A for Java system
     * UIs that select by server player id instead of the modeled CListBox row.
     */
    public static void kickPlayerById(int playerId) {
        Player player = Globals.gameServer.playerList.getPlayerById(playerId);
        kickPlayer(player);
    }

    /**
     * Native support extracted from DedicatedServerControlDialog::kickSelectedPlayerCommand @00492A3A.
     */
    private static void kickPlayer(Player player) {
        if (player != null) {
            Globals.gameServer.pushMessage("Player " + player.name + " kicked from server");
            CBufferManager client = CServerApp.getLocalClientByNetId(player.playerId);
            if (client != null) {
                CServerApp.removeLocalClient(client);
                CServerApp.broadcastPlayerKickedAction(player);
            }
        }
    }
}
