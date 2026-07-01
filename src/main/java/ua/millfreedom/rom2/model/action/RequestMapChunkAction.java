package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `TwoDwordAction` packet id `0x3B` used to request the next file offset during chunked map transfer.
 */
public class RequestMapChunkAction extends TwoDwordAction {
    public static final int ACTION_ID = GameActionId.REQUEST_MAP_CHUNK_ACTION_3B.id;
    public static final RequestMapChunkAction global = new RequestMapChunkAction();

    /**
     * Native support extracted from MapVisualObject::requestNextMapChunk @0041C5A4 and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public RequestMapChunkAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::requestNextMapChunk @0041C5A4 and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public RequestMapChunkAction(RequestMapChunkAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from map chunk request helper @0041C5A4 and packet-id `0x3B` layout @0040D9B2.
     */
    public static RequestMapChunkAction create(int netID, int nextOffset) {
        RequestMapChunkAction action = new RequestMapChunkAction();
        action.netID.set(netID);
        action.playerID.set(0);
        action.firstPayloadDword.set(nextOffset);
        action.secondPayloadDword.set(0);
        return action;
    }

    /**
     * Native support extracted from MapVisualObject::requestNextMapChunk @0041C5A4 and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    @Override
    public RequestMapChunkAction Clone() {
        return new RequestMapChunkAction(this);
    }

}
