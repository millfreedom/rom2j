package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Java-only dedicated map-transfer redirect packet sent by a source map server to its client.
 * not ported.
 */
public class MapTransferRedirectAction extends CGameAction {
    public static final int ACTION_ID = GameActionId.MAP_TRANSFER_REDIRECT_ACTION_C2.id;
    private static final int TARGET_HOST_SIZE = 0x80;
    private static final int TRANSFER_TOKEN_SIZE = 0x80;

    //0x0A
    public final Property<Integer> targetGamePort = i32(BODY_OFFSET);
    //0x0E
    public final Property<String> targetHost = fixedCString(BODY_OFFSET + Integer.BYTES, TARGET_HOST_SIZE);
    //0x8E
    public final Property<String> transferToken = fixedCString(
            BODY_OFFSET + Integer.BYTES + TARGET_HOST_SIZE,
            TRANSFER_TOKEN_SIZE
    );

    /**
     * Java support constructor for the dedicated map-transfer redirect packet.
     * not ported.
     */
    public MapTransferRedirectAction() {
        super();
        ID.set(ACTION_ID);
        targetHost.set("");
        targetGamePort.set(0);
        transferToken.set("");
    }

    /**
     * Java support copy constructor for the dedicated map-transfer redirect packet.
     * not ported.
     */
    public MapTransferRedirectAction(MapTransferRedirectAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Java support packet construction for the source dedicated server redirect.
     * not ported.
     */
    public static MapTransferRedirectAction createForRedirect(
            Player player,
            String targetHost,
            int targetGamePort,
            String transferToken
    ) {
        MapTransferRedirectAction action = new MapTransferRedirectAction();
        action.playerID.set(player == null ? 0 : player.playerId);
        action.targetHost.set(targetHost);
        action.targetGamePort.set(targetGamePort);
        action.transferToken.set(transferToken);
        return action;
    }

    /**
     * Java support wire size for the fixed dedicated map-transfer redirect packet.
     * not ported.
     */
    @Override
    public int GetPayloadSize() {
        return 1 + Integer.BYTES + TARGET_HOST_SIZE + TRANSFER_TOKEN_SIZE;
    }

    /**
     * Java support clone for the dedicated map-transfer redirect packet.
     * not ported.
     */
    @Override
    public MapTransferRedirectAction Clone() {
        return new MapTransferRedirectAction(this);
    }

    /**
     * Java support client-side handling for a source-server dedicated map-transfer redirect.
     * not ported.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        mapVisualObject.handleDedicatedMapTransferRedirect(
                targetHost.get(),
                targetGamePort.get(),
                transferToken.get()
        );
    }
}
