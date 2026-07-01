package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.unit.Unit;

import java.io.IOException;

public class Shop extends Building {
    private static final int DEFAULT_REFRESH_LIMIT = 10_000_000;
    private static final int DEFAULT_LOCAL_ASSORTMENT_MASK = 0x2fffffff;

    //0x6c
    public CMultiShopTemplate multiShopTemplate;
    //0x70
    public final ShopAssortmentEntry[] localShopAssortment = {
            new ShopAssortmentEntry(),
            new ShopAssortmentEntry(),
            new ShopAssortmentEntry(),
            new ShopAssortmentEntry()
    };
    //0xc0
    public int refreshLimit;

    /**
     * Native: Shop::Shop @00520D21.
     * Fully ported.
     */
    public Shop() {
        super();
        initializeShopCore();
        initializeDefaultLocalAssortment();
    }

    /**
     * Native: Shop::Shop @00520E43.
     * Fully ported.
     */
    public Shop(String name) {
        super(name);
        initializeShopCore();
    }

    /**
     * Fully ported. Native: Shop::Shop @00520EE6.
     */
    public Shop(int buildingId, TargetHandle targetHandle) {
        super(buildingId, targetHandle, 0, 0);
        initializeShopCore();
    }

    /**
     * vtbl +0x08: Shop::serialize @005211FB.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        super.serialize(ar);
        if (!ar.isStoring()) {
            refreshLimit = ar.readInt();
        } else {
            ar.writeInt(refreshLimit);
        }
    }

    /**
     * vtbl +0x14: Shop::updateRegen @0052106D.
     * Fully ported.
     */
    @Override
    public void updateRegen() {
        multiShopTemplate.updateRegen();
    }

    /**
     * Native: Shop::handleTrackedTokenUpdate @005210C9.
     * Fully ported.
     */
    public void handleTrackedTokenUpdate(Unit unit) {
        multiShopTemplate.removeTrackedToken(unit);
        if (Globals.gameServer.networkSessionActive != 0) {
            unit.returnToMissionMap();
        }
        CServerApp.notifyBuildingStateChanged(unit);
    }

    /**
     * Native: Shop::openTrackedShopForToken @00521099.
     * Fully ported.
     */
    public void openTrackedShopForToken(Unit unit) {
        multiShopTemplate.openTrackedShopForToken(unit);
        if (Globals.gameServer.networkSessionActive != 0) {
            unit.hideFromMissionMap();
        }
    }

    /**
     * Native: Shop::rebuildShelvesFromOwnerData @00521083.
     * Fully ported.
     */
    public void rebuildShelvesFromOwnerData() {
        multiShopTemplate.rebuildShelvesFromOwnerData();
    }

    /**
     * Native: Shop::clearShelves @00520F91.
     * Fully ported.
     */
    public void clearShelves() {
        if (multiShopTemplate != null) {
            multiShopTemplate.clearShelfItems();
        }
    }

    /**
     * Native: Shop::setRefreshLimit @0052103C.
     * Fully ported.
     */
    public void setRefreshLimit(int refreshLimit) {
        this.refreshLimit = refreshLimit;
        if (multiShopTemplate != null) {
            multiShopTemplate.refreshLimit = refreshLimit;
        }
    }

    /**
     * Native: Shop::takeTrackedTransactionItem @0052110B.
     * Fully ported.
     */
    public Item takeTrackedTransactionItem(Token token, int sourceSlot, int quantity) {
        return multiShopTemplate.takeTrackedTransactionItem(token, sourceSlot, quantity);
    }

    /**
     * Native: Shop::transferTrackedItem @0052112F.
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
        multiShopTemplate.transferTrackedItem(
                token,
                sourceContainerType,
                sourceSlot,
                destinationContainerType,
                destinationSlot,
                quantity
        );
    }

    /**
     * Native: Shop::returnTrackedItemToShelf @00521197.
     * Fully ported.
     */
    public void returnTrackedItemToShelf(Token token, Item item) {
        multiShopTemplate.returnTrackedItemToShelf(token, item);
    }

    /**
     * Native: Shop::commitTrackedBuy @0052115F.
     * Fully ported.
     */
    public void commitTrackedBuy(Token token) {
        multiShopTemplate.commitTrackedBuy(token);
    }

    /**
     * Native: Shop::commitTrackedSell @0052117B.
     * Fully ported.
     */
    public void commitTrackedSell(Token token) {
        multiShopTemplate.commitTrackedSell(token);
    }

    /**
     * Native: Shop::rollbackTrackedTransaction @005211BB.
     * Fully ported.
     */
    public void rollbackTrackedTransaction(Token token) {
        multiShopTemplate.rollbackTrackedTransaction(token);
    }

    /**
     * Native: Shop::addTrackedTransactionItem @005211D7.
     * Fully ported.
     */
    public void addTrackedTransactionItem(Token token, int destinationSlot, Item item) {
        multiShopTemplate.addTrackedTransactionItem(token, destinationSlot, item);
    }

    /**
     * Native support extracted from Shop constructors @00520D21, @00520E43, and @00520EE6.
     * Fully ported.
     */
    private void initializeShopCore() {
        refreshLimit = DEFAULT_REFRESH_LIMIT;
        multiShopTemplate = new CMultiShopTemplate(this);
    }

    /**
     * Native support extracted from Shop::Shop @00520D21.
     * Fully ported.
     */
    private void initializeDefaultLocalAssortment() {
        for (ShopAssortmentEntry entry : localShopAssortment) {
            entry.minPrice = 1_000_000;
            entry.maxPrice = DEFAULT_REFRESH_LIMIT;
            entry.itemCount = 1;
            entry.maxSameTypeItems = 1;
            entry.selectionMask = DEFAULT_LOCAL_ASSORTMENT_MASK;
        }
    }
}
