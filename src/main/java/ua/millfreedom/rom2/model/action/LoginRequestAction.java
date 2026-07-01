package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `ChatTextAction` packet id `0x0F` used by the HAT/master-server login flow.
 * The text payload stores `login + '\\x01' + password`.
 */
public class LoginRequestAction extends ChatTextAction {
    public static final int ACTION_ID = GameActionId.LOGIN_REQUEST_ACTION_0F.id;
    public static final LoginRequestAction global = new LoginRequestAction();

    /**
     * Native support extracted from sendDirectAddressLoginRequest @0040D6F6 and
     * CMainWindow::connectToServerAddress @0048E90F.
     */
    public LoginRequestAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from ChatTextAction::ChatTextAction @0050BF16,
     * sendDirectAddressLoginRequest @0040D6F6, and
     * CMainWindow::connectToServerAddress @0048E90F.
     */
    public LoginRequestAction(LoginRequestAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from sendDirectAddressLoginRequest @0040D6F6.
     * Fully ported.
     */
    public static LoginRequestAction prepareForDirectAddressLogin(String loginName, String password) {
        LoginRequestAction action = global;
        action.ID.set(ACTION_ID);
        action.playerID.set(0);
        action.text.set(loginName + '\u0001' + password);
        return action;
    }

    /**
     * Native support extracted from ChatTextAction::Clone @00541340.
     */
    @Override
    public LoginRequestAction Clone() {
        return new LoginRequestAction(this);
    }
}
