package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `TwoDwordAction` packet id `0xB4` used to signal mission failure / defeat state updates
 * through the quest-objectives dialog path.
 */
public class MissionFailedAction extends TwoDwordAction {
    public static final int ACTION_ID = GameActionId.MISSION_FAILED_ACTION_B4.id;
    public static final MissionFailedAction global = new MissionFailedAction();

    /**
     * Native support extracted from FUN_004F856A @004F856A,
     * Unit::FinalizeDeath @00510A70, and
     * MapVisualObject::HandleGameAction @0040DAA2.
     */
    public MissionFailedAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from FUN_004F856A @004F856A and
     * TwoDwordAction::Clone @005410D0.
     */
    public MissionFailedAction(MissionFailedAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040DAA2.
     * Ported action-id case: `MissionFailedAction`.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        Globals.mainWindow.postMessage(MessageCodes.HANDLE_QUEST_EVENT_DIALOG, 0xFF, firstPayloadDword.get());
    }

    /**
     * Native support extracted from FUN_004F856A @004F856A and
     * TwoDwordAction::Clone @005410D0.
     */
    @Override
    public MissionFailedAction Clone() {
        return new MissionFailedAction(this);
    }

}
