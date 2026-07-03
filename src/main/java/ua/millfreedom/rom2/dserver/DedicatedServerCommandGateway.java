package ua.millfreedom.rom2.dserver;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CServerApp;
import ua.millfreedom.rom2.model.action.ChatTextAction;
import ua.millfreedom.rom2.model.visobj.DedicatedServerConsoleVisualObject;

/**
 * Java support command gateway shared by the fullscreen visual console and Swing dedicated-server UI.
 * not ported.
 */
public final class DedicatedServerCommandGateway {
    /**
     * Java utility constructor.
     * not ported.
     */
    private DedicatedServerCommandGateway() {
    }

    /**
     * Java support equivalent of DedicatedServerConsoleVisualObject::OnKeyDown @0044D04B submit handling.
     */
    public static void submitConsoleText(String submittedText) {
        if (submittedText == null || submittedText.isEmpty()) {
            return;
        }
        if (submittedText.charAt(0) == '#') {
            DedicatedServerConsoleVisualObject.sendServerCommand(submittedText.substring(1));
            return;
        }

        ChatTextAction action = ChatTextAction.global;
        action.ID.set(ChatTextAction.ACTION_ID);
        action.firstPayloadDword.set(0);
        action.playerID.set(0);
        action.text.set(submittedText);
        Globals.gameServer.pushMessage(submittedText);
        CServerApp.sendGameAction(action);
    }
}
