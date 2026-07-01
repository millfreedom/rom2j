package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `TwoDwordAction` packet id `0x04` used to request map loading/initialization before the map descriptor arrives.
 */
public class RequestMapLoadAction extends TwoDwordAction {
    public static final int ACTION_ID = GameActionId.REQUEST_MAP_LOAD_ACTION_04.id;
    public static final RequestMapLoadAction global = new RequestMapLoadAction();

    /**
     * Native support extracted from MapVisualObject::FUN_0041C50D @0041C50D and
     * CMainWindow::runSessionBootstrap @0048C8A3.
     */
    public RequestMapLoadAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::FUN_0041C50D @0041C50D and
     * CMainWindow::runSessionBootstrap @0048C8A3.
     */
    public RequestMapLoadAction(RequestMapLoadAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from MapVisualObject::FUN_0041C50D @0041C50D and
     * CMainWindow::runSessionBootstrap @0048C8A3.
     */
    @Override
    public RequestMapLoadAction Clone() {
        return new RequestMapLoadAction(this);
    }

}
