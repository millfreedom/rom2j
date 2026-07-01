package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.Item;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.unit.humanoid.Humanoid;

/**
 * Native `ItemListAction` subtype `1` used by `CServerApp::sendUnitEquipmentStateUpdate @00502D67`
 * to send full worn-equipment item payloads for a unit.
 */
public class UnitEquipmentStateAction extends ItemListAction {
    public static final UnitEquipmentStateAction global = new UnitEquipmentStateAction();

    // Native subtype byte written by CServerApp::sendUnitEquipmentStateUpdate @00502EA8.
    private static final int EQUIPMENT_STATE_ACTION_SUBTYPE = 1;

    //0x0A
    public final Property<Integer> unitTokenId = u16(BODY_OFFSET);
    //0x0F
    public final Property<Integer> equipmentSlotMask = u16(BODY_OFFSET + 5);

    /**
     * Native support extracted from CServerApp::sendUnitEquipmentStateUpdate @00502D67 and
     * MapVisualObject::HandleGameAction @0040D9B2.
     * Fully ported.
     */
    public UnitEquipmentStateAction() {
        super();
        actionSubtypeAndFlags.set(EQUIPMENT_STATE_ACTION_SUBTYPE);
    }

    /**
     * Native support extracted from CServerApp::sendUnitEquipmentStateUpdate @00502D67 packet field writes.
     * Fully ported.
     */
    public static UnitEquipmentStateAction prepareForEquipmentFullState(
            Humanoid unit,
            Player targetPlayer,
            int slotMask
    ) {
        UnitEquipmentStateAction action = global;
        action.ID.set(ACTION_ID);
        action.playerID.set(targetPlayer.playerId & 0xFFFF);
        action.unitTokenId.set(unit.idFull);
        action.actionSubtypeAndFlags.set(EQUIPMENT_STATE_ACTION_SUBTYPE);
        action.itemCount.set(0);
        action.equipmentSlotMask.set(slotMask);
        action.trailingDataLength.set(0);
        action.trailingData.set(new byte[0]);
        appendEquipmentStatePayload(action, unit, slotMask);
        return action;
    }

    /**
     * Native support extracted from CServerApp::sendUnitEquipmentStateUpdate @00502D67.
     * Fully ported.
     */
    private static void appendEquipmentStatePayload(UnitEquipmentStateAction action, Humanoid unit, int slotMask) {
        if ((slotMask & 1) != 0) {
            appendEquipmentStateItem(action, unit.pWeapon);
        }
        if ((slotMask & 2) != 0) {
            appendEquipmentStateItem(action, unit.pShield);
        }
        for (int nativeSlot = 3; nativeSlot < 0x0D; nativeSlot++) {
            if ((slotMask & (1 << (nativeSlot - 1))) != 0) {
                appendEquipmentStateItem(action, unit.items[nativeSlot - 1]);
            }
        }
    }

    /**
     * Native support extracted from CServerApp::sendUnitEquipmentStateUpdate @00502D67 and
     * Item::appendNetworkItemPayload @005241BF.
     * Fully ported.
     */
    private static void appendEquipmentStateItem(UnitEquipmentStateAction action, Item item) {
        Item payloadItem = item == null ? new Item() : item;
        payloadItem.appendNetworkItemPayload(action, false);
        action.itemCount.set(action.itemCount.get() + 1);
    }

    /**
     * Native support extracted from ItemListAction::ItemListAction @0050C852 and
     * CServerApp::sendUnitEquipmentStateUpdate @00502D67.
     */
    public UnitEquipmentStateAction(UnitEquipmentStateAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from ItemListAction::Clone @00541A10.
     */
    @Override
    public UnitEquipmentStateAction Clone() {
        return new UnitEquipmentStateAction(this);
    }

}
