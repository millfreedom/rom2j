package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.actiondata.ActionPayloads;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native short-array packet id `0x45` used to push updated diplomacy relation flags.
 */
public class UpdateDiplomacyRelationsAction extends ShortArrayBlobAction {
    public static final int ACTION_ID = GameActionId.UPDATE_DIPLOMACY_RELATIONS_ACTION_45.id;
    public static final UpdateDiplomacyRelationsAction global = new UpdateDiplomacyRelationsAction();

    /**
     * Native support extracted from CMainWindow::onDialogClosed @004891D8 and MapVisualObject::sendDiplomacyRelationsAction @0041A17A.
     */
    public UpdateDiplomacyRelationsAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CMainWindow::onDialogClosed @004891D8 and MapVisualObject::sendDiplomacyRelationsAction @0041A17A.
     */
    public UpdateDiplomacyRelationsAction(UpdateDiplomacyRelationsAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from CMainWindow::onDialogClosed @004891D8 and MapVisualObject::sendDiplomacyRelationsAction @0041A17A.
     */
    @Override
    public UpdateDiplomacyRelationsAction Clone() {
        return new UpdateDiplomacyRelationsAction(this);
    }

}
