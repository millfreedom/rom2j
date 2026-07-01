package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.net.CBufferManager;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.Item;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.gameobj.CGameObject;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.actiondata.ActionPayloads;
import ua.millfreedom.rom2.model.unit.humanoid.Humanoid;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native action class `BitmaskShortListAction` / packet id `0x9C` used by
 * `CServerApp::sendUnitEquipmentStateUpdate @00502D67`
 * to send hash-only worn-equipment slot updates for a unit.
 */
public class EnemyEquipmentAction extends CGameAction {
    public static final int ACTION_ID = GameActionId.ENEMY_EQUIPMENT_ACTION_9C.id;
    public static final EnemyEquipmentAction global = new EnemyEquipmentAction();
    private static final int NATIVE_COPY_SIZE = 0x1D;

    //0x0A
    public final Property<Integer> unitTokenId = u16(BODY_OFFSET);
    //0x0C
    public final Property<Integer> equipmentSlotMask = u16(BODY_OFFSET + Short.BYTES);
    //0x0E
    public final Property<byte[]> equipmentItemHashes = u16Array(
            BODY_OFFSET + Integer.BYTES,
            () -> Integer.bitCount(equipmentSlotMask.get())
    );

    /**
     * Native support extracted from BitmaskShortListAction::BitmaskShortListAction @0050CC4E,
     * CServerApp::sendUnitEquipmentStateUpdate @00502D67, and
     * MapVisualObject::HandleGameAction @0040D9B2.
     * Fully ported.
     */
    public EnemyEquipmentAction() {
        super();
        unitTokenId.set(0);
        equipmentSlotMask.set(0);
        equipmentItemHashes.set(new byte[0]);
    }

    /**
     * Native support extracted from CServerApp::sendUnitEquipmentStateUpdate @00502D67 packet field writes.
     * Fully ported.
     */
    public static EnemyEquipmentAction prepareForEquipmentHashList(
            Humanoid unit,
            Player targetPlayer,
            int slotMask
    ) {
        EnemyEquipmentAction action = global;
        action.ID.set(ACTION_ID);
        action.playerID.set(targetPlayer.playerId);
        action.unitTokenId.set(unit.idFull);
        action.equipmentSlotMask.set(slotMask);
        appendEquipmentHashListPayload(action, unit, slotMask);
        return action;
    }

    /**
     * Native support extracted from CServerApp::sendUnitEquipmentStateUpdate @00502D67.
     * Fully ported.
     */
    private static void appendEquipmentHashListPayload(EnemyEquipmentAction action, Humanoid unit, int slotMask) {
        short[] hashes = new short[Integer.bitCount(slotMask)];
        int hashIndex = 0;
        if ((slotMask & 1) != 0) {
            hashes[hashIndex++] = (short) (unit.pWeapon == null ? 0 : unit.pWeapon.hash);
        }
        if ((slotMask & 2) != 0) {
            hashes[hashIndex++] = (short) (unit.pShield == null ? 0 : unit.pShield.hash);
        }
        for (int nativeSlot = 3; nativeSlot < 0x0D; nativeSlot++) {
            if ((slotMask & (1 << (nativeSlot - 1))) != 0) {
                Item item = unit.items[nativeSlot - 1];
                hashes[hashIndex++] = (short) (item == null ? 0 : item.hash);
            }
        }
        ActionPayloads.setShortArray(action.equipmentItemHashes, hashes);
    }

    /**
     * Native support extracted from BitmaskShortListAction::BitmaskShortListAction @0050CC74 and
     * CServerApp::sendUnitEquipmentStateUpdate @00502D67.
     * Fully ported.
     */
    public EnemyEquipmentAction(EnemyEquipmentAction from) {
        super();
        PutSlice(ID_OFFSET, from.GetSlice(ID_OFFSET, NATIVE_COPY_SIZE), 0, NATIVE_COPY_SIZE);
    }

    /**
     * vtbl +0x04: BitmaskShortListAction::Clone @00541BA0.
     * Fully ported.
     */
    @Override
    public EnemyEquipmentAction Clone() {
        return new EnemyEquipmentAction(this);
    }

    /**
     * Native support extracted from CGameAction::ReadPayload @0050BB15,
     * CServerApp::decodeIncomingGameAction @005056F1, and BitmaskShortListAction::GetPayloadSize @0050CCAD.
     */
    @Override
    public boolean ReadPayload(CBufferManager source) {
        boolean result = source.Read(this, BODY_OFFSET, Integer.BYTES);
        int hashBytes = Integer.bitCount(equipmentSlotMask.get()) * Short.BYTES;
        if (hashBytes == 0) {
            return result;
        }
        return source.Read(this, equipmentItemHashes.startPosition(), hashBytes);
    }

    /**
     * vtbl +0x10: BitmaskShortListAction::GetPayloadSize @0050CCAD.
     * Fully ported.
     */
    @Override
    public int GetPayloadSize() {
        int localC = Integer.bitCount(equipmentSlotMask.get());
        return (0x0C - localC) * -2 + 0x1D;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00412FC5.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        CGameObject object = mapVisualObject.getObjectByToken((short) (int) unitTokenId.get());
        if (!(object instanceof CUnit unit)) {
            return;
        }

        int hashIndex = 0;
        for (int slotIndex = 0; slotIndex < unit.equipmentTokenEntries.length; slotIndex++) {
            if ((equipmentSlotMask.get() & (1 << slotIndex)) == 0) {
                continue;
            }
            int tokenHash = equipmentItemHashes.unsignedWordAtIndex(hashIndex++);
            unit.equipmentTokenEntries[slotIndex] = tokenHash == 0 ? null : MapVisualObject.createHashOnlyTokenEntry(tokenHash, 1);
        }
        unit.unitFlags |= 0x08;
        unit.refreshUnitSpritesAfterRuntimeCopy();
    }
}
