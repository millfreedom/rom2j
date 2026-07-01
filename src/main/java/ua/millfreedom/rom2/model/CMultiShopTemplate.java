package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.unit.Unit;

import java.util.ArrayList;
import java.util.List;

public class CMultiShopTemplate implements MfcSerializable {
    private static final int SHOP_SHELF_COUNT = 4;
    private static final int MAX_TRACKED_INSTANCES = 0xFA;
    private static final int SHELF_EXTRA_MAGIC_MASK = 0x20000000;
    private static final int SOLD_ITEM_WEAPON_LIKE_MASK = 0x00400000;
    private static final int SOLD_ITEM_ARMOR_LIKE_MASK = 0x01000000;
    private static final int SOLD_ITEM_OTHER_LIKE_MASK = 0x04000000;
    private static final int SOLD_ITEM_MAGIC_TYPE = 5;

    //0x04
    public int activeInstanceCount;
    //0x08
    public int pendingRefreshTicks;
    //0x0c
    public final CMultiShopShelf[] shelves = {
            new CMultiShopShelf(),
            new CMultiShopShelf(),
            new CMultiShopShelf(),
            new CMultiShopShelf()
    };
    //0x7c
    public final List<CMultiShopInstance> trackedInstances = new ArrayList<>();
    //0x90
    public int refreshLimit;
    //0x94
    public Shop owner;

    /**
     * vtbl +0x08: CObject::Serialize @00401970.
     */
    @Override
    public void serialize(CArchive ar) {
        // Native CObject::Serialize is a no-op.
    }

    /**
     * Native: CMultiShopTemplate::CMultiShopTemplate @005221AF.
     * Fully ported.
     */
    public CMultiShopTemplate() {
        initializeCommon();
    }

    /**
     * Native: CMultiShopTemplate::CMultiShopTemplate @00522289.
     * Fully ported.
     */
    public CMultiShopTemplate(Shop owner) {
        initializeCommon();
        attachOwner(owner);
    }

    /**
     * Native: CMultiShopTemplate::attachOwner @00543E40 with Shop::getRefreshLimit @00543E70.
     * Fully ported.
     */
    public void attachOwner(Shop owner) {
        this.owner = owner;
        refreshLimit = owner.refreshLimit;
    }

    /**
     * Native support extracted from tracked-instance CArray::GetSize @00543E90.
     * Fully ported.
     */
    public int getTrackedEntriesCount() {
        return trackedInstances.size();
    }

    /**
     * Native support extracted from tracked-instance CArray::GetAt @00543EB0.
     * Fully ported.
     */
    public CMultiShopInstance getTrackedEntry(int index) {
        return trackedInstances.get(index);
    }

    /**
     * Native support extracted from tracked-instance CArray::RemoveAt @00543FC0 and
     * CMultiShopTemplate::removeTrackedToken @00522936 instance cleanup.
     * Fully ported.
     */
    public void removeTrackedRange(int index, int count) {
        for (int instanceIndex = index; instanceIndex < index + count; instanceIndex++) {
            CMultiShopInstance instance = trackedInstances.get(instanceIndex);
            if (instance != null) {
                instance.detachFromTemplate();
            }
        }
        trackedInstances.subList(index, index + count).clear();
    }

    /**
     * Native: CMultiShopTemplate::updateRegen @00522CCA.
     * Fully ported.
     */
    public void updateRegen() {
        if (Globals.gameServer.networkSessionActive == 0) {
            return;
        }

        if (Globals.gameServer.someValue % 0xB4 == 0) {
            pendingRefreshTicks += 1;
            refreshAssortmentIfNeeded();
        }

        for (int instanceIndex = 0; instanceIndex < getTrackedEntriesCount(); instanceIndex++) {
            CMultiShopInstance instance = getTrackedEntry(instanceIndex);
            Unit unit = instance.getTrackedToken();
            if (unit.owner.mapLoadPending == 0) {
                owner.handleTrackedTokenUpdate(unit);
            }
        }
    }

    /**
     * Native: CMultiShopTemplate::removeTrackedToken @00522936.
     * Fully ported.
     */
    public int removeTrackedToken(Token token) {
        int index = findTrackedTokenIndex(token);
        if (index != -1) {
            removeTrackedRange(index, 1);
            removeEmptyNonStackingShelfItems();
            refreshAssortmentIfNeeded();
        }
        return 1;
    }

    /**
     * Native: CMultiShopTemplate::findTrackedInstanceByToken @005226F5.
     * Fully ported.
     */
    public CMultiShopInstance findTrackedInstanceByToken(Token token) {
        int index = findTrackedTokenIndex(token);
        return index == -1 ? null : trackedInstances.get(index);
    }

    /**
     * Native: CMultiShopTemplate::commitTrackedBuy @00522A71.
     * Fully ported.
     */
    public boolean commitTrackedBuy(Token token) {
        CMultiShopInstance instance = findTrackedInstanceByToken(token);
        if (instance == null) {
            return false;
        }
        instance.trackedToken = token;
        instance.commitBuy();
        return true;
    }

    /**
     * Native: CMultiShopTemplate::commitTrackedSell @00522AB2.
     * Fully ported.
     */
    public boolean commitTrackedSell(Token token) {
        CMultiShopInstance instance = findTrackedInstanceByToken(token);
        if (instance == null) {
            return false;
        }
        instance.trackedToken = token;
        instance.commitSell();
        return true;
    }

    /**
     * Native: CMultiShopTemplate::rollbackTrackedTransaction @00522C75.
     * Fully ported.
     */
    public void rollbackTrackedTransaction(Token token) {
        CMultiShopInstance instance = findTrackedInstanceByToken(token);
        if (instance == null) {
            pushInvalidShopTransactionCustomer();
            return;
        }
        instance.trackedToken = token;
        instance.rollbackTransaction();
    }

    /**
     * Native: CMultiShopTemplate::transferTrackedItem @00522BAF.
     * Fully ported.
     */
    public void transferTrackedItem(
            Token token,
            int sourceContainerType,
            int sourceSlot,
            int destinationContainerType,
            int destinationSlot,
            int quantity
    ) {
        CMultiShopInstance instance = findTrackedInstanceByToken(token);
        if (instance == null) {
            pushInvalidShopTransactionCustomer();
            return;
        }
        instance.trackedToken = token;
        instance.transferItem(sourceContainerType, sourceSlot, destinationContainerType, destinationSlot, quantity);
    }

    /**
     * Native: CMultiShopTemplate::takeTrackedTransactionItem @00522B50.
     * Fully ported.
     */
    public Item takeTrackedTransactionItem(Token token, int sourceSlot, int quantity) {
        CMultiShopInstance instance = findTrackedInstanceByToken(token);
        if (instance == null) {
            pushInvalidShopTransactionCustomer();
            return null;
        }
        instance.trackedToken = token;
        return instance.takeTransactionItem(sourceSlot, quantity);
    }

    /**
     * Native: CMultiShopTemplate::addTrackedTransactionItem @00522AF3.
     * Fully ported.
     */
    public void addTrackedTransactionItem(Token token, int destinationSlot, Item item) {
        CMultiShopInstance instance = findTrackedInstanceByToken(token);
        if (instance == null) {
            pushInvalidShopTransactionCustomer();
            return;
        }
        instance.trackedToken = token;
        instance.addTransactionItem(destinationSlot, item);
    }

    /**
     * Native: CMultiShopTemplate::returnTrackedItemToShelf @00522C18.
     * Fully ported.
     */
    public void returnTrackedItemToShelf(Token token, Item item) {
        CMultiShopInstance instance = findTrackedInstanceByToken(token);
        if (instance == null) {
            pushInvalidShopTransactionCustomer();
            return;
        }
        instance.trackedToken = token;
        instance.returnItemToShelf(item);
    }

    /**
     * Native: CMultiShopTemplate::refreshAssortmentIfNeeded @00522D70.
     * Fully ported.
     */
    public int refreshAssortmentIfNeeded() {
        if (pendingRefreshTicks < 1 || activeInstanceCount != 0) {
            return 0;
        }
        rebuildShelvesFromOwnerData();
        pendingRefreshTicks = 0;
        return 1;
    }

    /**
     * Native support extracted from CMultiShopTemplate::openTrackedShopForToken @005228C1 and
     * tracked-instance CArray::Add @00543F50.
     * Fully ported.
     */
    public void addTrackedEntry(CMultiShopInstance entry) {
        trackedInstances.add(entry);
    }

    /**
     * Native: CMultiShopTemplate::openTrackedShopForToken @005228C1.
     * Fully ported.
     */
    public boolean openTrackedShopForToken(Token token) {
        if (getTrackedEntriesCount() >= MAX_TRACKED_INSTANCES) {
            return false;
        }
        if (shelves[0].items.isEmpty()) {
            rebuildShelvesFromOwnerData();
        }

        CMultiShopInstance instance = createTrackedInstance(token);
        addTrackedEntry(instance);
        instance.sendCatalogItemLists();
        return true;
    }

    /**
     * Native: CMultiShopTemplate::createTrackedInstance @005224E7.
     * Fully ported.
     */
    private CMultiShopInstance createTrackedInstance(Token token) {
        CMultiShopInstance instance = new CMultiShopInstance(token);
        instance.template = this;
        activeInstanceCount += 1;
        for (int shelfIndex = 0; shelfIndex < SHOP_SHELF_COUNT; shelfIndex++) {
            CMultiShopShelf sourceShelf = shelves[shelfIndex];
            CMultiShopShelf instanceShelf = instance.shelves[shelfIndex];
            instanceShelf.items.clear();
            for (Item item : sourceShelf.items) {
                if (item.count != 0) {
                    item.trackedShopReferenceCount++;
                    instanceShelf.items.add(item);
                }
            }
        }
        return instance;
    }

    /**
     * Native support extracted from CMultiShopTemplate constructors @005221AF / @00522289.
     * Fully ported.
     */
    private void initializeCommon() {
        activeInstanceCount = 0;
        pendingRefreshTicks = 0;
        trackedInstances.clear();
        owner = null;
        refreshLimit = 1000;
        shelves[0].shelfType = 1;
        shelves[1].shelfType = 2;
        shelves[2].shelfType = 3;
        shelves[3].shelfType = 4;
    }

    /**
     * Native support extracted from CMultiShopTemplate::findTrackedTokenIndex @0052269B.
     * Fully ported.
     */
    private int findTrackedTokenIndex(Token token) {
        for (int i = 0; i < trackedInstances.size(); i++) {
            CMultiShopInstance entry = trackedInstances.get(i);
            if (entry.getTrackedToken().owner == token.owner) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Native support extracted from CMultiShopTemplate tracked transaction wrappers @00522AF3,
     *
     * @00522B50, @00522BAF, @00522C18, and @00522C75.
     * Fully ported.
     */
    private static void pushInvalidShopTransactionCustomer() {
        Globals.gameServer.pushMessage("Invalid shop transaction - no such customer");
    }

    /**
     * Native: CMultiShopTemplate::sendCatalogItemListsExcept @00522470.
     * Fully ported.
     */
    public void sendCatalogItemListsExcept(CMultiShopInstance skippedInstance, int shelfSelector) {
        for (CMultiShopInstance instance : trackedInstances) {
            if (instance != null && instance != skippedInstance) {
                instance.sendCatalogItemLists(shelfSelector);
            }
        }
    }

    /**
     * Native support extracted from CMultiShopTemplate::removeTrackedToken @00522936.
     * Fully ported.
     */
    private void removeEmptyNonStackingShelfItems() {
        for (CMultiShopShelf shelf : shelves) {
            shelf.removeUnreferencedEmptyItems();
        }
    }

    /**
     * Native support extracted from CMultiShopInstance::returnItemToShelf @00521D4C.
     */
    int chooseShelfIndexForReturnedItem(Item item) {
        if (item.inventoryPayloadTag != 0) {
            return item.inventoryPayloadTag - 1;
        }

        ShopAssortmentEntry[] assortment = resolveActiveAssortment();
        int typeMask = shopTypeMaskForSoldItem(item);
        if (assortment != null && typeMask != 0) {
            if (item.canStackInInventory() == 0 && item.type != SOLD_ITEM_MAGIC_TYPE) {
                return chooseFirstExtraMagicShelfIndex(assortment);
            }
            for (int shelfIndex = 0; shelfIndex < Math.min(SHOP_SHELF_COUNT, assortment.length); shelfIndex++) {
                ShopAssortmentEntry entry = assortment[shelfIndex];
                if (entry != null
                        && (entry.selectionMask & typeMask) != 0
                        && (entry.selectionMask & SHELF_EXTRA_MAGIC_MASK) == 0) {
                    return shelfIndex;
                }
            }
            for (int shelfIndex = 0; shelfIndex < Math.min(SHOP_SHELF_COUNT, assortment.length); shelfIndex++) {
                ShopAssortmentEntry entry = assortment[shelfIndex];
                if (entry != null && (entry.selectionMask & typeMask) != 0) {
                    return shelfIndex;
                }
            }
        }
        return 0;
    }

    /**
     * Native support extracted from CMultiShopInstance::returnItemToShelf @00521D4C non-stackable item branch.
     */
    private static int chooseFirstExtraMagicShelfIndex(ShopAssortmentEntry[] assortment) {
        int shelfIndex = 0;
        while (shelfIndex < SHOP_SHELF_COUNT - 1
                && shelfIndex < assortment.length
                && (assortment[shelfIndex].selectionMask & SHELF_EXTRA_MAGIC_MASK) == 0) {
            shelfIndex++;
        }
        return Math.min(shelfIndex, SHOP_SHELF_COUNT - 1);
    }

    /**
     * Native support extracted from CMultiShopInstance::returnItemToShelf @00521D4C item-type mask mapping.
     */
    private static int shopTypeMaskForSoldItem(Item item) {
        return switch (item.type) {
            case 1 -> SOLD_ITEM_ARMOR_LIKE_MASK;
            case 2 -> SOLD_ITEM_WEAPON_LIKE_MASK;
            case 3, 4, 5 -> SOLD_ITEM_OTHER_LIKE_MASK;
            default -> 0;
        };
    }

    /**
     * Native: CMultiShopTemplate::rebuildShelvesFromOwnerData @00522767.
     * Fully ported.
     */
    public boolean rebuildShelvesFromOwnerData() {
        if (activeInstanceCount >= 1) {
            return false;
        }

        populateShelvesFromAssortment(resolveActiveAssortment(), shelves);
        return true;
    }

    /**
     * Native support extracted from CMultiShopTemplate::rebuildShelvesFromOwnerData @00522767.
     */
    static CMultiShopShelf[] createShelvesFromAssortment(ShopAssortmentEntry[] assortment) {
        CMultiShopShelf[] generatedShelves = createEmptyShelves();
        populateShelvesFromAssortment(assortment, generatedShelves);
        return generatedShelves;
    }

    /**
     * Native support extracted from CMultiShopTemplate::rebuildShelvesFromOwnerData @00522767.
     * Fully ported.
     */
    private static void populateShelvesFromAssortment(
            ShopAssortmentEntry[] assortment,
            CMultiShopShelf[] targetShelves
    ) {
        clearShelfItems(targetShelves);

        ItemAssortmentGenerator generator = new ItemAssortmentGenerator();
        for (int shelfIndex = 0; shelfIndex < SHOP_SHELF_COUNT; shelfIndex++) {
            CMultiShopShelf shelf = targetShelves[shelfIndex];
            Inventory generatedItems = generator.generateItems(assortment[shelfIndex], new Inventory());
            for (Item item : generatedItems.items) {
                item.inventoryPayloadTag = shelf.shelfType;
                shelf.items.add(item);
            }
        }
    }

    /**
     * Native support extracted from CMultiShopTemplate::clearShelfItems @0052272E.
     * Fully ported.
     */
    void clearShelfItems() {
        clearShelfItems(shelves);
    }

    /**
     * Native support extracted from CMultiShopTemplate::clearShelfItems @0052272E.
     * Fully ported.
     */
    private static void clearShelfItems(CMultiShopShelf[] shelves) {
        for (CMultiShopShelf shelf : shelves) {
            shelf.clearItems();
        }
    }

    /**
     * Native support extracted from CMultiShopTemplate constructors @005221AF / @00522289.
     */
    private static CMultiShopShelf[] createEmptyShelves() {
        CMultiShopShelf[] generatedShelves = {
                new CMultiShopShelf(),
                new CMultiShopShelf(),
                new CMultiShopShelf(),
                new CMultiShopShelf()
        };
        for (int shelfIndex = 0; shelfIndex < generatedShelves.length; shelfIndex++) {
            generatedShelves[shelfIndex].shelfType = shelfIndex + 1;
        }
        return generatedShelves;
    }

    /**
     * Native source split in FUN_00522767 @00522767 and FUN_00521d4c @00521d4c.
     * Partial port. Native reads from ScenarioGetShopAssortment() when `g_GameServer->networkSessionActive == 0`, otherwise from `owner + 0x70`.
     */
    private ShopAssortmentEntry[] resolveActiveAssortment() {
        if (Globals.gameServer.networkSessionActive == 0) {
            return Globals.scenarioLib.getShopAssortment();
        }
        return owner.localShopAssortment;
    }
}
