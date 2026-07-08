package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Java-only dedicated map-transfer token packet sent by a reconnecting client to the target map server.
 * not ported.
 */
public class MapTransferTokenAction extends CGameAction {
    public static final int ACTION_ID = GameActionId.MAP_TRANSFER_TOKEN_ACTION_C3.id;
    private static final int TRANSFER_TOKEN_SIZE = 0x80;

    //0x0A
    public final Property<String> transferToken = fixedCString(BODY_OFFSET, TRANSFER_TOKEN_SIZE);

    /**
     * Java support constructor for the dedicated map-transfer token packet.
     * not ported.
     */
    public MapTransferTokenAction() {
        super();
        ID.set(ACTION_ID);
        transferToken.set("");
    }

    /**
     * Java support copy constructor for the dedicated map-transfer token packet.
     * not ported.
     */
    public MapTransferTokenAction(MapTransferTokenAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Java support packet construction for a reconnecting map-transfer client.
     * not ported.
     */
    public static MapTransferTokenAction createForToken(String transferToken) {
        MapTransferTokenAction action = new MapTransferTokenAction();
        action.transferToken.set(transferToken);
        return action;
    }

    /**
     * Java support wire size for the fixed dedicated map-transfer token packet.
     * not ported.
     */
    @Override
    public int GetPayloadSize() {
        return 1 + TRANSFER_TOKEN_SIZE;
    }

    /**
     * Java support clone for the dedicated map-transfer token packet.
     * not ported.
     */
    @Override
    public MapTransferTokenAction Clone() {
        return new MapTransferTokenAction(this);
    }
}
