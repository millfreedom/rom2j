package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `UnitTokenAction` packet id `0x38` used to enter an inn interaction with the current target unit token.
 */
public class EnterInnAction extends UnitTokenAction {
    public static final int ACTION_ID = GameActionId.ENTER_INN_ACTION_38.id;
    public static final EnterInnAction global = new EnterInnAction();

    /**
     * Native support extracted from CMainWindow::showInnDialog @0048B885 and MapVisualObject::PrepareRemoteInnDialog @0041A800.
     */
    public EnterInnAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CMainWindow::showInnDialog @0048B885 and MapVisualObject::PrepareRemoteInnDialog @0041A800.
     */
    public EnterInnAction(EnterInnAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from CMainWindow::showInnDialog @0048B885 and MapVisualObject::PrepareRemoteInnDialog @0041A800.
     */
    @Override
    public EnterInnAction Clone() {
        return new EnterInnAction(this);
    }
}
