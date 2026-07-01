package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Concrete `FixedDwordAction` packet id `0x0E` sent by CServerApp::sendLoginAcceptedHandshake @00504CCC
 * and handled by MapVisualObject::HandleGameAction @0040DD4B.
 */
public class FixedDwordAction0E extends FixedDwordAction {
    public static final int ACTION_ID = GameActionId.FIXED_DWORD_ACTION_0E.id;
    public static final FixedDwordAction0E global = new FixedDwordAction0E();
    // Native loginAcceptedHandshakePayload @00627594 read by CServerApp::sendLoginAcceptedHandshake @00504CCC.
    private static final int LOGIN_ACCEPTED_HANDSHAKE_PAYLOAD = 0;

    /**
     * Native support extracted from FixedDwordAction::FixedDwordAction @0050BE42,
     * CServerApp::sendLoginAcceptedHandshake @00504CCC, and MapVisualObject::HandleGameAction @0040DD4B.
     */
    public FixedDwordAction0E() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from FixedDwordAction::FixedDwordAction @0050BE6B,
     * CServerApp::sendLoginAcceptedHandshake @00504CCC, and MapVisualObject::HandleGameAction @0040DD4B.
     */
    public FixedDwordAction0E(FixedDwordAction0E from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CServerApp::sendLoginAcceptedHandshake @00504CCC.
     */
    public static FixedDwordAction0E createForLoginAcceptedHandshake() {
        FixedDwordAction0E action = global;
        action.ID.set(ACTION_ID);
        action.payloadDword.set(LOGIN_ACCEPTED_HANDSHAKE_PAYLOAD);
        return action.Clone();
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040DD4B.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        super.handle(mapVisualObject);
    }

    /**
     * Native support extracted from FixedDwordAction::Clone @00541190.
     */
    @Override
    public FixedDwordAction0E Clone() {
        return new FixedDwordAction0E(this);
    }
}
