package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.MagicItemId;

public class MagicItem extends Item {
    private static final int MAGIC_ITEM_SLOT = 0x0E;
    private static final int RUNTIME_TYPE_POTION = 3;
    private static final int RUNTIME_TYPE_SCROLL = 4;
    private static final int RUNTIME_TYPE_BOOK = 5;
    private static final String CROWN_BLOCK_PATTERN = "Crown";
    private static final String DOCUMENTS_BLOCK_PATTERN = "Documents";
    private static final String TREASURE_BLOCK_PATTERN = "Treasure";
    private static final String QUEST_TREASURE_NAME = "Quest Treasure";

    /**
     * Native support for CArchive::ReadObject @005AC3E9 materializing a MagicItem before Item::serialize @0052D65A
     * reads the archive state; defaults match MagicItem constructors @00522EE0 and @00522FAB.
     */
    public MagicItem() {
        initializeMagicItemDefaults();
    }

    /**
     * Native: MagicItem::MagicItem @00522EE0.
     * Fully ported.
     */
    public MagicItem(int magicItemIndex) {
        this(MAGIC_ITEM_SLOT, magicItemIndex);
    }

    /**
     * Native: MagicItem::MagicItem @00522EE0.
     * Fully ported. Native allocates an Item-sized object, installs Item::vtbl, stores the supplied magic row id, and
     * initializes from CStaticDataMgr::global.magicItems using the supplied slot.
     */
    public MagicItem(int slot, int magicItemIndex) {
        initializeMagicItemDefaults();
        key = (magicItemIndex) & 0xFFFF;
        applyMagicItemInfo(slot);
    }

    /**
     * Native: MagicItem::MagicItem(CString*) @00522FAB.
     * Fully ported. Native scans CStaticDataMgr::global.magicItems by name, then initializes the matched entry with
     * slot 0x0E; if not found, it pushes a UI message and returns the default-initialized object.
     */
    public MagicItem(String itemName) {
        initializeMagicItemDefaults();
        int magicItemIndex = findMagicItemIndexByName(itemName);
        if (magicItemIndex == 0) {
            notifyMissingMagicItem(itemName);
            return;
        }

        key = (magicItemIndex) & 0xFFFF;
        applyMagicItemInfo(MAGIC_ITEM_SLOT);
    }

    /**
     * Native support wrapper around MagicItem::MagicItem @00522EE0.
     */
    public static MagicItem createById(MagicItemId magicItemId) {
        if (magicItemId == null || magicItemId.id < 1) {
            return null;
        }
        return createByIndex(magicItemId.id);
    }

    /**
     * Native support wrapper around MagicItem::MagicItem @00522EE0.
     */
    public static MagicItem createByIndex(int magicItemIndex) {
        if (magicItemIndex < 1 || magicItemIndex >= Globals.staticDataMgr.magicItems.size()) {
            return null;
        }
        return new MagicItem(MAGIC_ITEM_SLOT, magicItemIndex);
    }

    /**
     * Native support wrapper around MagicItem::MagicItem(CString*) @00522FAB.
     */
    public static MagicItem createByName(String itemName) {
        if (itemName == null || itemName.isEmpty()) {
            return null;
        }

        MagicItem item = new MagicItem(itemName);
        return (item.key & 0xFFFF) == 0 ? null : item;
    }

    /**
     * Native support extracted from MagicItem::MagicItem @00522EE0 and MagicItem::MagicItem(CString*) @00522FAB;
     * both constructors install Item::vtbl, so archive writes use Item.
     */
    @Override
    public Class<?> mfcRuntimeClass() {
        return Item.class;
    }

    /**
     * Native: MagicItem::IsShopCatalogEntryDragBlocked @00439C73.
     * Fully ported.
     */
    public static boolean isShopCatalogEntryDragBlocked(TokenEntry token, int sessionMode) {
        if (token.getType() != MAGIC_ITEM_SLOT) {
            return false;
        }

        String itemName = resolveMagicItemWorldName(token);
        return itemName.contains(CROWN_BLOCK_PATTERN)
                || itemName.contains(DOCUMENTS_BLOCK_PATTERN)
                || (itemName.contains(TREASURE_BLOCK_PATTERN) && sessionMode != ua.millfreedom.rom2.model.window.CMainWindow.SESSION_MODE_CAMPAIGN);
    }

    /**
     * Native: MagicItem::IsShopCatalogEntryBlockedForDoubleClick @00439D63.
     * Fully ported.
     */
    public static boolean isShopCatalogEntryBlockedForDoubleClick(TokenEntry token) {
        if (token.getType() != MAGIC_ITEM_SLOT) {
            return false;
        }
        return QUEST_TREASURE_NAME.equals(resolveMagicItemWorldName(token));
    }

    /**
     * Native support extracted from MagicItem constructors @00522EE0 and @00522FAB.
     * Fully ported.
     */
    private void initializeMagicItemDefaults() {
        worldItem = null;
        hash = 0;
        count = 1;
        type = 0;
        shapeID = 0;
        materialID = 0;
        key = 0;
        magicVolume = 0;
        weight = 1;
        field7_0x43 = 0;
        scenarioObjectId = 0;
        trackedShopReferenceCount = 0;
        inventoryPayloadTag = 0;
        exceedsMagicCapacity = 0;
        price = 0;
        effects.clear();
    }

    /**
     * Native: MagicItem::applyMagicItemInfo @00523148.
     * Fully ported. Native resolves the magic item info row, derives the runtime item subtype from the info name, packs
     * the item with the supplied slot, parses the row effects, then dispatches Item::recalculatePrice.
     */
    private void applyMagicItemInfo(int slot) {
        int magicItemIndex = key & 0xFFFF;
        if (magicItemIndex >= Globals.staticDataMgr.magicItems.size()) {
            notifyInvalidMagicItem();
            return;
        }

        MagicalItemInfo magicItemInfo = Globals.staticDataMgr.magicItems.get(magicItemIndex);
        worldItem = magicItemInfo;
        this.type = resolveRuntimeType(magicItemInfo.name);
        hash = packIDs(slot, shapeID, materialID, magicItemIndex);
        if (!magicItemInfo.effect.isEmpty()) {
            parseEffects(magicItemInfo.effect);
        }
        recalculatePrice();
    }

    /**
     * Native support extracted from MagicItem::MagicItem(CString*) @00522FAB.
     * Fully ported.
     */
    private static int findMagicItemIndexByName(String itemName) {
        for (int index = 1; index < Globals.staticDataMgr.magicItems.size(); index++) {
            MagicalItemInfo magicItemInfo = Globals.staticDataMgr.magicItems.get(index);
            if (itemName.equals(magicItemInfo.name)) {
                return index;
            }
        }
        return 0;
    }

    /**
     * Native support extracted from MagicItem::applyMagicItemInfo @00523148.
     * Fully ported.
     */
    private static int resolveRuntimeType(String itemName) {
        if (itemName.startsWith("Potion")) {
            return RUNTIME_TYPE_POTION;
        }
        if (itemName.startsWith("Book")) {
            return RUNTIME_TYPE_BOOK;
        }
        if (itemName.startsWith("Scroll") || itemName.startsWith("SuperScroll")) {
            return RUNTIME_TYPE_SCROLL;
        }
        return 0;
    }

    /**
     * Native support extracted from MagicItem::IsShopCatalogEntryDragBlocked @00439C73 and
     * MagicItem::IsShopCatalogEntryBlockedForDoubleClick @00439D63.
     */
    private static String resolveMagicItemWorldName(TokenEntry token) {
        MagicalItemInfo magicItemInfo = Globals.staticDataMgr.magicItems.get((token.getShape() << 5) | token.getId());
        return magicItemInfo.name;
    }

    /**
     * Native support extracted from MagicItem::MagicItem(CString*) @00522FAB.
     * Fully ported.
     */
    private static void notifyMissingMagicItem(String itemName) {
        Globals.gameServer.pushMessage("Invalind Item " + itemName + " - no such ID");
    }

    /**
     * Native support extracted from MagicItem::applyMagicItemInfo @00523148.
     * Fully ported.
     */
    private static void notifyInvalidMagicItem() {
        Globals.gameServer.pushMessage("Error - creating invalid Item");
    }
}
