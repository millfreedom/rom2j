package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.unit.UnitDirtyFlags;

public class CMultiShopInstance implements MfcSerializable {
    //0x04
    public final CMultiShopShelf[] shelves = {
            new CMultiShopShelf(),
            new CMultiShopShelf(),
            new CMultiShopShelf(),
            new CMultiShopShelf()
    };
    //0x74
    public Token trackedToken;
    //0x78
    public final Inventory inventory = new Inventory();
    //0x9c
    public CMultiShopTemplate template;

    /**
     * vtbl +0x08: CObject::Serialize @00401970.
     */
    @Override
    public void serialize(CArchive ar) {
        // Native CObject::Serialize is a no-op.
    }

    /**
     * Native: CMultiShopInstance::CMultiShopInstance @0052195E.
     * Fully ported.
     */
    public CMultiShopInstance() {
        initializeShelfTypes();
    }

    /**
     * Native support extracted from CMultiShopTemplate::createTrackedInstance @005224E7.
     * Fully ported.
     */
    public CMultiShopInstance(Token trackedToken) {
        this();
        this.trackedToken = trackedToken;
    }

    /**
     * Native: CMultiShopInstance::getTrackedToken @00544040.
     * Fully ported.
     */
    public Unit getTrackedToken() {
        return (Unit) trackedToken;
    }

    /**
     * Native support boundary for CMultiShopTemplate::updateRegen @00522CCA.
     */
    public boolean isInactive() {
        return trackedToken == null;
    }

    /**
     * Native support extracted from CMultiShopInstance::~CMultiShopInstance @00521A12.
     * Java lifecycle cleanup only detaches references and updates the owning template count.
     */
    public void detachFromTemplate() {
        if (template != null) {
            template.activeInstanceCount -= 1;
            template = null;
        }
        trackedToken = null;
        inventory.items.clear();
        for (CMultiShopShelf shelf : shelves) {
            shelf.clearTrackedInstanceItems();
        }
    }

    /**
     * Native: CMultiShopInstance::sendCatalogItemLists @00521F3A.
     * Fully ported.
     */
    public void sendCatalogItemLists() {
        sendCatalogItemLists(-1);
    }

    /**
     * Native: CMultiShopInstance::sendCatalogItemLists @00521F3A.
     * Fully ported.
     */
    public void sendCatalogItemLists(int shelfSelector) {
        Player targetPlayer = trackedToken.owner;
        if (shelfSelector == 0) {
            CServerApp.sendDialogItemList(getDialogOwnerToken(), inventory, targetPlayer, 4);
            return;
        }

        CServerApp.sendShopCatalogItemLists(this, targetPlayer, shelfSelector);
    }

    /**
     * Native: CMultiShopInstance::transferItem @00521FD1.
     * Fully ported.
     */
    public void transferItem(
            int sourceContainerType,
            int sourceSlot,
            int destinationContainerType,
            int destinationSlot,
            int quantity
    ) {
        int sourceShelfSelector = sourceContainerType - 4;
        Item item;
        if (sourceShelfSelector == 0) {
            item = takeTransactionItem(sourceSlot, quantity);
            if (item == null || item.isZeroCountItem()) {
                sendCatalogItemLists(0);
                return;
            }
        } else {
            item = takeShelfItem(sourceShelfSelector, sourceSlot, quantity);
            if (item == null || item.isZeroCountItem()) {
                sendCatalogItemLists(sourceShelfSelector);
                return;
            }
            notifyOtherTrackedCatalogs(sourceShelfSelector);
        }

        if (destinationContainerType == 4) {
            addTransactionItem(destinationSlot, item);
            return;
        }

        boolean templateShelfChanged = returnItemToShelf(item);
        if (templateShelfChanged) {
            notifyOtherTrackedCatalogs(-1);
        }
    }

    /**
     * Native: CMultiShopInstance::takeTransactionItem @00521FB1.
     * Fully ported.
     */
    public Item takeTransactionItem(int sourceSlot, int quantity) {
        return inventory.takeItemAt(sourceSlot, quantity);
    }

    /**
     * Native: CMultiShopInstance::addTransactionItem @00521F91.
     * Fully ported.
     */
    public void addTransactionItem(int destinationSlot, Item item) {
        inventory.insertItemPreservingPayloadTag(destinationSlot, item);
    }

    /**
     * Native: CMultiShopInstance::commitBuy @00521C29.
     * Fully ported.
     */
    public void commitBuy() {
        Unit unit = trackedUnit();
        Player player = resolveTrackedPlayer();

        int index = 0;
        while (index < inventory.items.size()) {
            Item item = inventory.items.get(index);
            if (item.owner != null) {
                index++;
                continue;
            }
            int itemCost = transactionItemValue(item);
            if (player.gold < itemCost) {
                break;
            }
            player.gold -= itemCost;
            removeTransactionItemAt(index);
            item.owner = player;
            item.trackedShopReferenceCount = 0;
            unit.inventory.addItem(item);
        }
        sendCatalogItemLists(0);
        player.adjustGoldAndNotify(0, 0);
        refreshTrackedUnitInventory(unit, player);
    }

    /**
     * Native: CMultiShopInstance::commitSell @00521AE3.
     * Fully ported.
     */
    public void commitSell() {
        Unit unit = trackedUnit();
        Player player = resolveTrackedPlayer();

        boolean templateShelfChanged = false;
        int index = 0;
        while (index < inventory.items.size()) {
            Item item = inventory.items.get(index);
            if (item.owner == null || item.price == 0) {
                index++;
                continue;
            }
            player.gold += (int) Math.floor(transactionItemValue(item) * 0.5d + 0.5d);
            removeTransactionItemAt(index);
            item.owner = null;
            if (returnItemToShelf(item)) {
                templateShelfChanged = true;
            }
        }
        if (templateShelfChanged) {
            notifyOtherTrackedCatalogs(-1);
        }
        sendCatalogItemLists(0);
        sendCatalogItemLists(-1);
        player.adjustGoldAndNotify(0, 0);
        refreshTrackedUnitInventory(unit, player);
    }

    /**
     * Native: CMultiShopInstance::rollbackTransaction @005220D9.
     * Fully ported.
     */
    public void rollbackTransaction() {
        if (inventory.size() == 0) {
            return;
        }

        Unit unit = trackedUnit();
        for (Item item : inventory.items) {
            if (item.owner == null) {
                returnItemToShelf(item);
            } else {
                unit.inventory.addItem(item);
            }
        }
        inventory.items.clear();
        sendCatalogItemLists(0);
        sendCatalogItemLists(-1);
        template.sendCatalogItemListsExcept(this, -1);
    }

    /**
     * Native support extracted from CMultiShopInstance::CMultiShopInstance @0052195E.
     * Fully ported.
     */
    private void initializeShelfTypes() {
        shelves[0].shelfType = 1;
        shelves[1].shelfType = 2;
        shelves[2].shelfType = 3;
        shelves[3].shelfType = 4;
    }

    /**
     * Native: CMultiShopInstance::getDialogOwnerToken @00540850.
     * Fully ported.
     */
    Token getDialogOwnerToken() {
        return template == null ? null : template.owner;
    }

    /**
     * Native support extracted from CMultiShopInstance::commitBuy @00521C29 and commitSell @00521AE3.
     */
    private Player resolveTrackedPlayer() {
        return trackedToken.owner;
    }

    /**
     * Native support extracted from CMultiShopInstance::commitBuy @00521C29, commitSell @00521AE3,
     * and rollbackTransaction @005220D9.
     */
    private Unit trackedUnit() {
        return (Unit) trackedToken;
    }

    /**
     * Native support extracted from CArray<Item>::takeItemAt @005217A2.
     */
    private Item takeShelfItem(int shelfSelector, int sourceSlot, int quantity) {
        return shelves[shelfSelector - 1].takeItemAt(sourceSlot, quantity);
    }

    /**
     * Native: CMultiShopInstance::returnItemToShelf @00521D4C.
     * Fully ported.
     */
    boolean returnItemToShelf(Item item) {
        int shelfIndex = template.chooseShelfIndexForReturnedItem(item);
        Item shelfItem = template.shelves[shelfIndex].mergeOrAddItemAndReturn(item);
        if (shelfItem != item) {
            shelfItem.trackedShopReferenceCount = 0;
            return true;
        }
        return shelves[shelfIndex].mergeOrAddItem(item);
    }

    /**
     * Native support extracted from CMultiShopTemplate::sendCatalogItemListsExcept @00522470.
     */
    private void notifyOtherTrackedCatalogs(int shelfSelector) {
        template.sendCatalogItemListsExcept(this, shelfSelector);
    }


    /**
     * Native support extracted from CMultiShopInstance::commitBuy @00521C29 and commitSell @00521AE3.
     */
    private static int transactionItemValue(Item item) {
        return (item.count & 0xFFFF) * item.price;
    }

    /**
     * Native support extracted from CList<Item>::RemoveFirstMatch @00543760 in CMultiShopInstance transaction commits.
     */
    private Item removeTransactionItemAt(int index) {
        return inventory.items.remove(index);
    }

    /**
     * Native support extracted from Unit::refreshEncumbrance @0050F065 and CServerApp::NetUpdate @00502019.
     */
    private static void refreshTrackedUnitInventory(Unit unit, Player player) {
        unit.refreshEncumbrance(0);
        CServerApp.netUpdate(
                unit,
                player,
                UnitDirtyFlags.INVENTORY_AND_ENCUMBRANCE.value,
                0x0FFB,
                0,
                0
        );
    }
}
