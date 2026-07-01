package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.CGameSession;
import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `TwoDwordAction` packet id `0x48` used to submit the current multiplayer character-setup bytes from `CGameSession`.
 */
public class SubmitCharacterSetupAction extends TwoDwordAction {
    public static final int ACTION_ID = GameActionId.SUBMIT_CHARACTER_SETUP_ACTION_48.id;
    public static final SubmitCharacterSetupAction global = new SubmitCharacterSetupAction();

    //0x0A
    public final Property<Integer> body = u8(BODY_OFFSET);
    //0x0B
    public final Property<Integer> reaction = u8(BODY_OFFSET + 1);
    //0x0C
    public final Property<Integer> mind = u8(BODY_OFFSET + 2);
    //0x0D
    public final Property<Integer> spirit = u8(BODY_OFFSET + 3);
    //0x0E
    public final Property<Integer> startingSkillIndex = u8(BODY_OFFSET + 4);
    //0x0F
    public final Property<Integer> faceAndType = u8(BODY_OFFSET + 5);
    //0x10
    public final Property<Integer> clanServerId = u8(BODY_OFFSET + 6);

    /**
     * Native support extracted from MapVisualObject::submitCharacterSetup @0041C5F5,
     * CGameSession::submitCharacterSetupAndWaitForSelectedUnit @0049183E, and CMainWindow::onDialogClosed @004891D8.
     */
    public SubmitCharacterSetupAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::submitCharacterSetup @0041C5F5,
     * CGameSession::submitCharacterSetupAndWaitForSelectedUnit @0049183E, and CMainWindow::onDialogClosed @004891D8.
     */
    public SubmitCharacterSetupAction(SubmitCharacterSetupAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from MapVisualObject::submitCharacterSetup @0041C5F5 packet field writes.
     */
    public static SubmitCharacterSetupAction prepareForCharacterSetup(int netId, CGameSession gameSession) {
        SubmitCharacterSetupAction action = global;
        action.ID.set(ACTION_ID);
        action.netID.set(netId);
        action.playerID.set(0);
        action.body.set(gameSession.body);
        action.reaction.set(gameSession.reaction);
        action.mind.set(gameSession.mind);
        action.spirit.set(gameSession.spirit);
        action.startingSkillIndex.set(gameSession.startingSkillIndex);
        action.faceAndType.set(gameSession.face | gameSession.type);
        action.clanServerId.set(gameSession.clanServerId);
        return action;
    }

    /**
     * Native support extracted from MapVisualObject::submitCharacterSetup @0041C5F5,
     * CGameSession::submitCharacterSetupAndWaitForSelectedUnit @0049183E, and CMainWindow::onDialogClosed @004891D8.
     */
    @Override
    public SubmitCharacterSetupAction Clone() {
        return new SubmitCharacterSetupAction(this);
    }
}
