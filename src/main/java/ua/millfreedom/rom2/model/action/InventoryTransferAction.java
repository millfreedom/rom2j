package ua.millfreedom.rom2.model.action;

/**
 * Inventory/equipment transfer order payload (packet ID 0x22 path in GameServer::handleServerGameAction).
 */
public class InventoryTransferAction extends CGameAction {
    public static final InventoryTransferAction global = new InventoryTransferAction();

    //0x0A
    public final Property<Integer> unitTokenId = u16(BODY_OFFSET);
    //0x0C
    public final Property<Integer> sourceContainerType = u8(BODY_OFFSET + 2);
    //0x0D
    public final Property<Integer> destinationContainerType = u8(BODY_OFFSET + 3);
    //0x0E
    public final Property<Integer> sourceSlot = u16(BODY_OFFSET + 4);
    //0x10
    public final Property<Integer> destinationSlot = u16(BODY_OFFSET + 6);
    //0x12
    public final Property<Integer> quantityOrItemId = u16(BODY_OFFSET + 8);

    /**
     * Native: InventoryTransferAction::InventoryTransferAction @0050BC65.
     * Fully ported.
     */
    public InventoryTransferAction() {
        super();
        unitTokenId.set(0);
        sourceContainerType.set(0);
        destinationContainerType.set(0);
        sourceSlot.set(0);
        destinationSlot.set(0);
        quantityOrItemId.set(1);
    }

    /**
     * vtbl +0x10: InventoryTransferAction::getWireSize @00540FF0.
     * Port name: GetPayloadSize.
     * Fully ported.
     */
    @Override
    public int GetPayloadSize() {
        return 0x0B;
    }
}
