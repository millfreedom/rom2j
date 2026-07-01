package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `TwoDwordAction` packet id `0x3A` used to leave an inn and commit the current inn selection state.
 */
public class LeaveInnAction extends TwoDwordAction {
    public static final int LEAVE_INN_SELECTION_SENTINEL = 0xAAAAAAAA;
    public static final int ACTION_ID = GameActionId.LEAVE_INN_ACTION_3A.id;
    public static final LeaveInnAction global = new LeaveInnAction();

    /**
     * Native support extracted from BasicInnDialogVisualObject::LeaveInn @0049B41B,
     * DruidInnDialogVisualObject::LeaveInn @0049CB27, KaargInnDialogVisualObject::LeaveInn @0049ECF7,
     * and MapVisualObject::commitLeaveInnSelection @0041A861.
     */
    public LeaveInnAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::commitLeaveInnSelection @0041A861.
     */
    public static LeaveInnAction prepareForLeaveInn(int actingUnitToken, int netId, int leaveSelectionFlag) {
        LeaveInnAction action = global;
        action.ID.set(ACTION_ID);
        action.netID.set(netId);
        action.playerID.set(0);
        action.firstPayloadDword.set(actingUnitToken);
        action.secondPayloadDword.set(leaveSelectionFlag);
        return action;
    }

    /**
     * Native support extracted from BasicInnDialogVisualObject::LeaveInn @0049B41B,
     * DruidInnDialogVisualObject::LeaveInn @0049CB27, KaargInnDialogVisualObject::LeaveInn @0049ECF7,
     * and MapVisualObject::commitLeaveInnSelection @0041A861.
     */
    public LeaveInnAction(LeaveInnAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from BasicInnDialogVisualObject::LeaveInn @0049B41B,
     * DruidInnDialogVisualObject::LeaveInn @0049CB27, KaargInnDialogVisualObject::LeaveInn @0049ECF7,
     * and MapVisualObject::commitLeaveInnSelection @0041A861.
     */
    @Override
    public LeaveInnAction Clone() {
        return new LeaveInnAction(this);
    }

}
