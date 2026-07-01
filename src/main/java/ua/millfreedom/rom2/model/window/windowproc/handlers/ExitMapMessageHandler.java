package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.net.CLlDriver;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.model.world.ScenarioLocation;
import ua.millfreedom.rom2.model.world.ScenarioProgressSupport;

import java.util.List;

import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;

/**
 * Native support extracted from the `EXIT_MAP` branch in CMainWindow::WindowProc @004852D8.
 */
public final class ExitMapMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private ExitMapMessageHandler() {
    }

    /**
     * Native support extracted from the `EXIT_MAP` branch in CMainWindow::WindowProc @004852D8.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        if (mainWindow.sessionMode == CMainWindow.SESSION_MODE_NETWORK_HOST
                || mainWindow.sessionMode == CMainWindow.SESSION_MODE_DEDICATED_SERVER) {
            return handleHostedSessionExit(mainWindow);
        }
        if (mainWindow.sessionMode == CMainWindow.SESSION_MODE_MULTIPLAYER_CLIENT) {
            return handleClientSessionExit(mainWindow);
        }
        if (mainWindow.completedMissionExitPending == 0) {
            mainWindow.cleanupActiveSessionForMenuReturn();
            mainWindow.postMessage(MessageCodes.SHOW_MAIN_MENU, 0, 0);
            return 1;
        }
        handleCompletedCampaignMissionExit(mainWindow);
        return 1;
    }

    /**
     * Native support extracted from the NETWORK_HOST/DEDICATED_SERVER branch in CMainWindow::WindowProc @004852D8.
     */
    private static int handleHostedSessionExit(CMainWindow mainWindow) {
        if (mainWindow.sessionMode == CMainWindow.SESSION_MODE_NETWORK_HOST) {
            mainWindow.pMapVisualObject.clearSessionForAllPlayersLobbyReturn();
        } else {
            mainWindow.pMapVisualObject.clearSessionForFirstPlayerLobbyReturn();
        }
        if (GAMEPLAY.isSetIn(mainWindow.dialogsMask)) {
            mainWindow.cleanupGameplayResources();
        }
        Globals.gameServer.returnToLobby();
        mainWindow.postMessage(MessageCodes.PREPARE_MULTIPLAYER_MAP_SELECTION, 0, 0);
        return 1;
    }

    /**
     * Native support extracted from the MULTIPLAYER_CLIENT branch in CMainWindow::WindowProc @004852D8.
     */
    private static int handleClientSessionExit(CMainWindow mainWindow) {
        mainWindow.pMapVisualObject.clearSessionForFirstPlayerLobbyReturn();
        if (GAMEPLAY.isSetIn(mainWindow.dialogsMask)) {
            mainWindow.cleanupGameplayResources();
        }
        CLlDriver.handleNetworkErrorAndClose();
        mainWindow.m_LastRenderTime = Integer.MAX_VALUE;
        mainWindow.m_LastTickTime = Integer.MAX_VALUE;
        mainWindow.postMessage(MessageCodes.SHOW_MULTIPLAYER_SESSION_DIALOG, 0, 0);
        return 1;
    }

    /**
     * Native support extracted from the completed campaign-location branch in CMainWindow::WindowProc @004852D8.
     */
    private static void handleCompletedCampaignMissionExit(CMainWindow mainWindow) {
        mainWindow.m_FameHall.addMissionElapsedTime(Globals.gameServer.serverLoopCounter / 16);
        mainWindow.pMapVisualObject.cleanupCompletedMissionMapState();
        if (GAMEPLAY.isSetIn(mainWindow.dialogsMask)) {
            mainWindow.cleanupGameplayResources();
        }
        returnCompletedCampaignServerToLobbyIfActive();
        mainWindow.restoreCoreVisualTreeForMenuTransition();
        ScenarioLocation currentLocation = Globals.scenarioLib.getCurrentLocation();
        mainWindow.pGlobalMapDialogVisualObject.setTravelOrigin(currentLocation.rect.left, currentLocation.rect.top);

        int[] cutsceneId = {-1};
        int playerGoldDelta = Globals.scenarioLib.leaveLocation(cutsceneId);
        preparePostMissionInnEntryUnitUpdates(mainWindow);
        if (cutsceneId[0] >= 0) {
            mainWindow.playLocationCutscene(cutsceneId[0]);
        }
        if (!ScenarioProgressSupport.isCampaignEndingReached()) {
            continueCampaignAfterMission(mainWindow, playerGoldDelta);
        } else {
            mainWindow.m_FameHall.submitScore();
            mainWindow.pMapVisualObject.clearSessionForFirstPlayerLobbyReturn();
            mainWindow.postMessage(MessageCodes.SHOW_CREDITS_DIALOG, 0, 0);
        }
    }

    /**
     * Native support extracted from GetPostMissionInnEntryId @00493DE0 and the
     * GameServer::PrepareInnEntryUnitUpdates @004F312A call site in CMainWindow::WindowProc @004852D8.
     */
    private static void preparePostMissionInnEntryUnitUpdates(CMainWindow mainWindow) {
        int postMissionInnEntryId = ScenarioProgressSupport.getPostMissionInnEntryId();
        if (postMissionInnEntryId != 0) {
            Globals.gameServer.prepareInnEntryUnitUpdates(
                    Globals.scenarioLib.getVar(0x300),
                    List.of(postMissionInnEntryId)
            );
            mainWindow.pMapVisualObject.pumpPendingGameActions();
        }
    }

    /**
     * Native support extracted from the non-ending campaign continuation tail in CMainWindow::WindowProc @004852D8.
     */
    private static void continueCampaignAfterMission(CMainWindow mainWindow, int playerGoldDelta) {
        if (playerGoldDelta != 0) {
            mainWindow.pMapVisualObject.sendAdjustPlayerGoldAction(playerGoldDelta);
        }
        if (!Globals.scenarioLib.isTownAvailable(0)) {
            ScenarioLocation nextLocation = Globals.scenarioLib.getAvailableLocations().getFirst();
            Globals.scenarioLib.enterLocation(nextLocation);
            mainWindow.postMessage(MessageCodes.CONTINUE_SCENARIO_LOCATION_ENTRY, 0, 0);
        } else {
            mainWindow.pGlobalMapDialogVisualObject.umoirMapMode = 0;
            mainWindow.showGlobalMapDialog();
        }
    }

    /**
     * Native support extracted from the completed campaign-location branch of CMainWindow::WindowProc @004852D8.
     * Java keeps `g_GameServer` as the permanent Globals.gameServer singleton.
     */
    private static void returnCompletedCampaignServerToLobbyIfActive() {
        Globals.gameServer.returnToLobby();
    }
}
