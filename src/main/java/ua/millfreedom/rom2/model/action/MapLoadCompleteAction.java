package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `CGameAction` packet id `0x05` used as the client acknowledgment that mission/map loading completed.
 */
public class MapLoadCompleteAction extends CGameAction {
    public static final int ACTION_ID = GameActionId.MAP_LOAD_COMPLETE_ACTION_05.id;
    public static final MapLoadCompleteAction global = new MapLoadCompleteAction();

    /**
     * Native support extracted from MapVisualObject::sendMapLoadComplete @0041C79A,
     * GameServer::handleServerGameAction @004F515D case `0x05`, and
     * GameServer::FUN_004F1D9C @004F1D9C.
     */
    public MapLoadCompleteAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::sendMapLoadComplete @0041C79A,
     * GameServer::handleServerGameAction @004F515D case `0x05`, and
     * GameServer::FUN_004F1D9C @004F1D9C.
     */
    public MapLoadCompleteAction(MapLoadCompleteAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::sendMapLoadComplete @0041C79A,
     * GameServer::handleServerGameAction @004F515D case `0x05`, and
     * GameServer::FUN_004F1D9C @004F1D9C.
     */
    @Override
    public MapLoadCompleteAction Clone() {
        return new MapLoadCompleteAction(this);
    }
}
