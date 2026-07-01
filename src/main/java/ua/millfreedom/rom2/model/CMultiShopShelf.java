package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.model.container.CustomList;

public class CMultiShopShelf implements MfcSerializable {
    //0x04
    public int shelfType;
    //0x08
    public final CustomList<Item> items = new CustomList<>(Item.class);

    /**
     * vtbl +0x08: CObject::Serialize @00401970.
     */
    @Override
    public void serialize(CArchive ar) {
        // Native CObject::Serialize is a no-op.
    }

    /**
     * Native: CMultiShopShelf::CMultiShopShelf @00521467.
     * Fully ported.
     */
    public CMultiShopShelf() {
        shelfType = -1;
    }

    /**
     * Native: CMultiShopShelf::getItemsCount @005407D0 and @00543F10.
     * Fully ported.
     */
    public int getItemsCount() {
        return items.size();
    }

    /**
     * Native: CMultiShopShelf::getItemAt @005407F0.
     * Native support extracted from CMultiShopShelf::getItemSlot @00543F30.
     * Fully ported.
     */
    public Item getItemAt(int index) {
        return items.get(index);
    }

    /**
     * Native: CMultiShopShelf::takeItemAt @005217A2.
     * Fully ported.
     */
    public Item takeItemAt(int itemIndex, int quantity) {
        if (itemIndex < items.size() && itemIndex >= 0) {
            Item item = items.get(itemIndex);
            if ((item.count & 0xFFFF) < quantity) {
                quantity = item.count & 0xFFFF;
            }
            item.count -= quantity - 1;
            Item taken = item.splitOne();
            taken.count = quantity;
            return taken;
        }
        return null;
    }

    /**
     * Native support extracted from CMultiShopShelf::mergeOrAddItemAndReturn @0052151C.
     * Fully ported.
     */
    public boolean mergeOrAddItem(Item item) {
        return mergeOrAddItemAndReturn(item) != item;
    }

    /**
     * Native: CMultiShopShelf::mergeOrAddItemAndReturn @0052151C.
     * Fully ported.
     */
    public Item mergeOrAddItemAndReturn(Item item) {
        item.inventoryPayloadTag = shelfType;
        for (Item existing : items) {
            if (item.canStackInInventory() != 0 && existing.matchesInventoryIdentity(item)) {
                existing.count = ((existing.count & 0xFFFF) + (item.count & 0xFFFF)) & 0xFFFF;
                return existing;
            }
            if (item.canStackInInventory() == 0 && existing.count == 0 && existing.matchesInventoryIdentity(item)) {
                existing.count = ((existing.count & 0xFFFF) + (item.count & 0xFFFF)) & 0xFFFF;
                return existing;
            }
        }
        items.add(item);
        return item;
    }

    /**
     * Native support extracted from CMultiShopInstance::returnItemToShelf @00521D4C shared item-pointer handling.
     */
    public boolean containsItem(Item item) {
        return items.contains(item);
    }

    /**
     * Native support extracted from CMultiShopTemplate::removeTrackedToken @00522936.
     */
    public void removeUnreferencedEmptyItems() {
        for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
            Item item = items.get(itemIndex);
            if (item.count == 0 && item.trackedShopReferenceCount == 0) {
                removeItemAt(itemIndex);
                itemIndex--;
            }
        }
    }

    /**
     * Native: CMultiShopShelf::removeItemAt @0052182C.
     * Fully ported.
     */
    private void removeItemAt(int itemIndex) {
        if (itemIndex < items.size() && itemIndex >= 0) {
            items.remove(itemIndex);
        }
    }

    /**
     * Native: CMultiShopShelf::clearTrackedInstanceItems @00521860.
     * Fully ported.
     */
    public void clearTrackedInstanceItems() {
        for (Item item : items) {
            item.trackedShopReferenceCount--;
        }
        items.clear();
    }

    /**
     * Native: CMultiShopShelf::clearItems @005218C0.
     * Fully ported.
     */
    public void clearItems() {
        items.clear();
    }
}
