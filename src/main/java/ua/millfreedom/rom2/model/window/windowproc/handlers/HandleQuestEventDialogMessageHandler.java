package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.enums.SfxSounds;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.sound.SoundManager;
import ua.millfreedom.rom2.model.visobj.RoleDialogSupport;
import ua.millfreedom.rom2.model.window.CMainWindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.PatchText.YOU_VE_GOT_A_NEW_MISSION_97;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.MODAL_DIALOG;

/**
 * Native support extracted from the `HANDLE_QUEST_EVENT_DIALOG` branch in CMainWindow::WindowProc @004852D8.
 */
public final class HandleQuestEventDialogMessageHandler {
    private static final int QUEST_EVENT_MISSION_FAILED_DIALOG = 0xFF;
    private static final int QUEST_EVENT_MISSION_FAILED_SOUND = 0xFE;
    private static final int QUEST_EVENT_MISSION_COMPLETED_SOUND = 0xFD;
    private static final int QUEST_EVENT_MISSION_NEW = 0xFA;
    private static final int QUEST_EVENT_TOAST_LIFETIME_MS = 30000;
    private static final String EVENT_SCRIPT_FORMAT = "event%d";

    // Native global: CDWordArray_00627170, queued role-dialog ids while MODAL_DIALOG is active.
    private static final List<Integer> queuedRoleDialogIds = new ArrayList<>();

    /**
     * Java utility constructor.
     * not ported.
     */
    private HandleQuestEventDialogMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::CreateServer @0048AB76.
     */
    public static void clearQueuedRoleDialogIds() {
        queuedRoleDialogIds.clear();
    }

    /**
     * Native support extracted from the queued CDWordArray_00627170 drain in CMainWindow::onDialogClosed @004891D8.
     */
    public static void postNextQueuedRoleDialogId(CMainWindow mainWindow) {
        if (!queuedRoleDialogIds.isEmpty()) {
            mainWindow.postMessage(MessageCodes.HANDLE_QUEST_EVENT_DIALOG, queuedRoleDialogIds.getFirst(), 0);
            queuedRoleDialogIds.removeFirst();
        }
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     * Fully ported.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        mainWindow.questEventDialogId = CMainWindowWindowProcSupport.readMessageInt(wParam);
        switch (mainWindow.questEventDialogId) {
            case QUEST_EVENT_MISSION_FAILED_DIALOG -> mainWindow.postMessage(MessageCodes.SHOW_MISSION_FAILED_DIALOG, lParam, 0);
            case QUEST_EVENT_MISSION_FAILED_SOUND -> playQuestEventSound(SfxSounds.MFAILED);
            case QUEST_EVENT_MISSION_COMPLETED_SOUND -> playQuestEventSound(SfxSounds.MCOMPLET);
            case QUEST_EVENT_MISSION_NEW -> onNewMission();
            default -> {
                if (MODAL_DIALOG.isUnsetIn(mainWindow.dialogsMask)) {
                    RoleDialogSupport.showRoleKeyDialog(String.format(Locale.ROOT, EVENT_SCRIPT_FORMAT, mainWindow.questEventDialogId));
                } else {
                    queuedRoleDialogIds.add(mainWindow.questEventDialogId);
                }
            }
        }

        return 1;
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8 QUEST_EVENT_MISSION_NEW branch.
     * Fully ported.
     */
    private static void onNewMission() {
        playQuestEventSound(SfxSounds.MCOMPLET);
        Globals.mainWindow.pMapVisualObject.gameListControl.addTimedLine(
                get(YOU_VE_GOT_A_NEW_MISSION_97),
                Palettes.gray,
                QUEST_EVENT_TOAST_LIFETIME_MS
        );
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8 MFAILED/MCOMPLET playback gates.
     * Fully ported.
     */
    private static void playQuestEventSound(SfxSounds sound) {
        SoundManager.SFX_SOUNDS.get(sound.id)
                .playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, (byte) 0xDC, 0);
    }
}
