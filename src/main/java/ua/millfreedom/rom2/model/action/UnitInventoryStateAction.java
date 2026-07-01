package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.Item;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.unit.Unit;

/**
 * Native `ItemListAction` subtype `2` used by `CServerApp::sendUnitInventoryItemsUpdate @00503236`
 * to send full or ranged unit inventory item payload updates.
 */
public class UnitInventoryStateAction extends ItemListAction {
    public static final UnitInventoryStateAction global = new UnitInventoryStateAction();

    // Native subtype byte written by CServerApp::sendUnitInventoryItemsUpdate @005032BC.
    private static final int UNIT_INVENTORY_ACTION_SUBTYPE = 2;
    // Native range-update flag ORed at CServerApp::sendUnitInventoryItemsUpdate @005032D9.
    private static final int PARTIAL_RANGE_FLAG = 0x80;

    //0x0A
    public final Property<Integer> unitTokenId = u16(BODY_OFFSET);
    //0x0F
    public final Property<Integer> startIndex = u16(BODY_OFFSET + 5);

    /**
     * Native support extracted from CServerApp::sendUnitInventoryItemsUpdate @00503236 and
     * MapVisualObject::HandleGameAction @0040D9B2.
     * Fully ported.
     */
    public UnitInventoryStateAction() {
        super();
        actionSubtypeAndFlags.set(UNIT_INVENTORY_ACTION_SUBTYPE);
    }

    /**
     * Native support extracted from CServerApp::sendUnitInventoryItemsUpdate @00503236 packet field writes.
     * Fully ported.
     */
    public static UnitInventoryStateAction prepareForUnitInventoryItemsUpdate(
            Unit unit,
            Player targetPlayer,
            int inventoryStart,
            int inventoryEnd
    ) {
        int inventorySize = unit.inventory.items.size();
        if (inventoryStart + inventoryEnd == 0) {
            inventoryStart = 0;
            inventoryEnd = inventorySize;
        }

        int playerId = targetPlayer == null ? unit.owner.playerId : targetPlayer.playerId;
        int itemCount = inventoryEnd - inventoryStart;
        boolean partialRange = inventoryStart != 0 || inventoryEnd != inventorySize;

        UnitInventoryStateAction action = global;
        action.ID.set(ACTION_ID);
        action.playerID.set(playerId & 0xFFFF);
        action.unitTokenId.set(unit.idFull);
        action.actionSubtypeAndFlags.set(partialRange
                ? UNIT_INVENTORY_ACTION_SUBTYPE | PARTIAL_RANGE_FLAG
                : UNIT_INVENTORY_ACTION_SUBTYPE);
        action.startIndex.set(inventoryStart);
        action.itemCount.set(itemCount);
        action.trailingDataLength.set(0);
        action.trailingData.set(new byte[0]);
        appendInventoryStatePayload(action, unit, inventoryStart, inventoryEnd);
        return action;
    }

    /**
     * Native support extracted from CServerApp::sendUnitInventoryItemsUpdate @00503236 and
     * Item::appendNetworkItemPayload @005241BF.
     * Fully ported.
     */
    private static void appendInventoryStatePayload(
            UnitInventoryStateAction action,
            Unit unit,
            int inventoryStart,
            int inventoryEnd
    ) {
        int index = 0;
        for (Item item : unit.inventory.items) {
            if (inventoryStart <= index && index < inventoryEnd) {
                item.appendNetworkItemPayload(action, false);
            }
            index++;
        }
    }

    /**
     * Native support extracted from ItemListAction::ItemListAction @0050C852 and
     * CServerApp::sendUnitInventoryItemsUpdate @00503236.
     */
    public UnitInventoryStateAction(UnitInventoryStateAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from ItemListAction::Clone @00541A10.
     */
    @Override
    public UnitInventoryStateAction Clone() {
        return new UnitInventoryStateAction(this);
    }

}
