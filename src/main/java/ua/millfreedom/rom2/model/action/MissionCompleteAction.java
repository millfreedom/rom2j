package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `TwoDwordAction` packet id `0xB5` used to signal mission completion and close the quest-objectives dialog.
 */
public class MissionCompleteAction extends TwoDwordAction {
    public static final int ACTION_ID = GameActionId.MISSION_COMPLETE_ACTION_B5.id;
    public static final MissionCompleteAction global = new MissionCompleteAction();

    /**
     * Native support extracted from FUN_004F856A @004F856A and
     * MapVisualObject::HandleGameAction @0040DA86.
     */
    public MissionCompleteAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from FUN_004F856A @004F856A and
     * MapVisualObject::HandleGameAction @0040DA86.
     */
    public MissionCompleteAction(MissionCompleteAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040DA86.
     * Ported action-id case: `MissionCompleteAction`.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        Globals.mainWindow.postMessage(MessageCodes.SHOW_MISSION_COMPLETED_DIALOG, 0, 0);
    }

    /**
     * Native support extracted from FUN_004F856A @004F856A and
     * TwoDwordAction::Clone @005410D0.
     */
    @Override
    public MissionCompleteAction Clone() {
        return new MissionCompleteAction(this);
    }
}
