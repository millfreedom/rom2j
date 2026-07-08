package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.Building;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Java-only dedicated map-transfer marker for a structure token that should accept enter-building orders.
 * not ported.
 */
public class MapTransferBuildingAction extends CGameAction {
    public static final int ACTION_ID = GameActionId.MAP_TRANSFER_BUILDING_ACTION_C5.id;

    //0x0A
    public final Property<Integer> buildingTokenId = u16(BODY_OFFSET);
    //0x0C
    public final Property<Integer> interactionEnabled = u8(BODY_OFFSET + Short.BYTES);

    /**
     * Java support constructor for the dedicated map-transfer building marker packet.
     * not ported.
     */
    public MapTransferBuildingAction() {
        super();
        ID.set(ACTION_ID);
        buildingTokenId.set(0);
        interactionEnabled.set(0);
    }

    /**
     * Java support copy constructor for the dedicated map-transfer building marker packet.
     * not ported.
     */
    public MapTransferBuildingAction(MapTransferBuildingAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Java support packet construction for a transfer-backed building token.
     * not ported.
     */
    public static MapTransferBuildingAction createForBuilding(Building building, Player player, boolean enabled) {
        MapTransferBuildingAction action = new MapTransferBuildingAction();
        action.playerID.set(player == null ? 0 : player.playerId);
        action.buildingTokenId.set(building.idFull & 0xFFFF);
        action.interactionEnabled.set(enabled ? 1 : 0);
        return action;
    }

    /**
     * Java support fixed wire size for the transfer-backed building marker packet.
     * not ported.
     */
    @Override
    public int GetPayloadSize() {
        return 1 + Short.BYTES + 1;
    }

    /**
     * Java support clone for the transfer-backed building marker packet.
     * not ported.
     */
    @Override
    public MapTransferBuildingAction Clone() {
        return new MapTransferBuildingAction(this);
    }

    /**
     * Java support client-side registration of transfer-backed building tokens.
     * not ported.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        mapVisualObject.markDedicatedMapTransferBuilding(
                buildingTokenId.get(),
                interactionEnabled.get() != 0
        );
    }
}
