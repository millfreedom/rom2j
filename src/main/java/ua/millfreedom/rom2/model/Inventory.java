package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.model.container.CustomList;

import java.io.IOException;

public class Inventory implements MfcSerializable {
    //0x00
    public final CustomList<Item> items = new CustomList<>(Item.class);
    //0x1c
    public int insertIndex;
    //0x20
    public int weight;

    /**
     * Native: Inventory::Inventory @005299B3.
     * Fully ported.
     */
    public Inventory() {
        insertIndex = 0x7FFF;
        weight = 0;
    }

    /**
     * Native: Inventory::addItem @00529AC2.
     * Fully ported.
     */
    public void addItem(Item candidate) {
        insertItem(insertIndex, candidate);
    }

    /**
     * Native: Inventory::drainItemsFrom @00529E5B.
     * Fully ported.
     */
    public void drainItemsFrom(Inventory source) {
        if (source != null && size() < 0x400) {
            Item item = source.takeItemAt(0, 1);
            while (item != null) {
                addItem(item);
                item = source.takeItemAt(0, 1);
            }
            source.items.clear();
        }
    }

    /**
     * Native: Inventory::InsertItem @00529AE2.
     * Fully ported.
     */
    public void insertItem(int targetIndex, Item candidate) {
        insertItem(targetIndex, candidate, false);
    }

    /**
     * Native: Inventory::insertItemPreservingPayloadTag @00529C96.
     * Fully ported.
     */
    public void insertItemPreservingPayloadTag(int targetIndex, Item candidate) {
        insertItem(targetIndex, candidate, true);
    }

    /**
     * Native support extracted from Inventory::insertItem @00529AE2 and
     * Inventory::insertItemPreservingPayloadTag @00529C96.
     * Fully ported.
     */
    private void insertItem(int targetIndex, Item candidate, boolean preservePayloadTag) {
        if (candidate == null) {
            return;
        }

        if (!preservePayloadTag) {
            candidate.inventoryPayloadTag = 0;
        }
        if (candidate.canStackInInventory() != 0) {
            for (Item existing : items) {
                if (existing.getIdHashLowWord() != candidate.getIdHashLowWord()
                        || existing.canStackInInventory() == 0
                        || (preservePayloadTag && existing.inventoryPayloadTag != candidate.inventoryPayloadTag)) {
                    continue;
                }

                int candidateCount = candidate.count & 0xFFFF;
                weight += candidate.weight * candidateCount;
                existing.count = ((existing.count & 0xFFFF) + candidateCount) & 0xFFFF;
                existing.scenarioObjectId |= candidate.scenarioObjectId;
                return;
            }
        }

        if (targetIndex < items.size()) {
            items.add(Math.max(targetIndex, 0), candidate);
        } else {
            items.add(candidate);
        }
        weight += candidate.weight * (candidate.count & 0xFFFF);
    }

    /**
     * Native: Inventory::Inventory(copy) @00529A25.
     * Fully ported.
     */
    public Inventory copyFrom(Inventory source) {
        items.clear();
        for (Item srcItem : source.items) {
            addItem(srcItem.copyItemVirtual());
        }
        insertIndex = source.insertIndex;
        weight = source.weight;
        return this;
    }

    /**
     * Native: Inventory::GetItemsCount @0053DE20.
     * Fully ported.
     */
    public int size() {
        return items.size();
    }

    /**
     * Native: Inventory::takeItemAt @00529FB6.
     * Fully ported.
     */
    public Item takeItemAt(int itemIndex, int quantity) {
        if (itemIndex <= items.size()) {
            for (int currentIndex = 0; currentIndex < items.size(); currentIndex++) {
                Item item = items.get(currentIndex);
                if (currentIndex == itemIndex) {
                    Item taken;
                    if (quantity < (item.count & 0xFFFF)) {
                        int quantityWord = quantity & 0xFFFF;
                        item.count = ((item.count & 0xFFFF) - (quantityWord - 1)) & 0xFFFF;
                        taken = item.splitOne();
                        taken.count = quantityWord;
                    } else {
                        taken = item;
                        items.remove(currentIndex);
                    }
                    weight -= taken.weight * (taken.count & 0xFFFF);
                    return taken;
                }
            }
        }
        return null;
    }

    /**
     * Native: Inventory::takeOneByHash @00529F3F.
     * Fully ported.
     */
    public Item takeOneByHash(int itemHash) {
        int normalizedHash = itemHash & 0xFFFF;
        for (int index = 0; index < items.size(); index++) {
            Item item = items.get(index);
            if (item.getIdHashLowWord() != normalizedHash) {
                continue;
            }

            Item taken;
            if ((item.count & 0xFFFF) < 2) {
                taken = items.remove(index);
            } else {
                taken = item.splitOne();
            }
            if (taken != null) {
                weight -= taken.weight * (taken.count & 0xFFFF);
            }
            return taken;
        }
        return null;
    }

    /**
     * Native: Inventory::Serialize @0052DA4D.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        ar.serialize(items);
        if (!ar.isStoring()) {
            insertIndex = ar.readInt();
            weight = ar.readInt();
        } else {
            ar.writeInt(insertIndex);
            ar.writeInt(weight);
        }
    }
}
