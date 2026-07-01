package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `TwoDwordAction` packet id `0x39` used to acknowledge that a chunked map-file transfer completed.
 */
public class MapChunkTransferCompleteAction extends TwoDwordAction {
    public static final int ACTION_ID = GameActionId.MAP_CHUNK_TRANSFER_COMPLETE_ACTION_39.id;
    public static final MapChunkTransferCompleteAction global = new MapChunkTransferCompleteAction();

    /**
     * Native support extracted from MapVisualObject::notifyMapChunkTransferComplete @0041C55E and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public MapChunkTransferCompleteAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::notifyMapChunkTransferComplete @0041C55E and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public MapChunkTransferCompleteAction(MapChunkTransferCompleteAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from MapVisualObject::notifyMapChunkTransferComplete @0041C55E and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    @Override
    public MapChunkTransferCompleteAction Clone() {
        return new MapChunkTransferCompleteAction(this);
    }

}
