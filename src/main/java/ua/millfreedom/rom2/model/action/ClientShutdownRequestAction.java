package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `CGameAction` packet id `0x09` received when a client requests shutdown from the hosted server.
 */
public class ClientShutdownRequestAction extends CGameAction {
    public static final int ACTION_ID = GameActionId.CLIENT_SHUTDOWN_REQUEST_ACTION_09.id;
    public static final ClientShutdownRequestAction global = new ClientShutdownRequestAction();

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D and
     * CServerApp::decodeIncomingGameAction @005056F1.
     */
    public ClientShutdownRequestAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D and
     * CServerApp::decodeIncomingGameAction @005056F1.
     */
    public ClientShutdownRequestAction(ClientShutdownRequestAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D and
     * CServerApp::decodeIncomingGameAction @005056F1.
     */
    @Override
    public ClientShutdownRequestAction Clone() {
        return new ClientShutdownRequestAction(this);
    }
}
