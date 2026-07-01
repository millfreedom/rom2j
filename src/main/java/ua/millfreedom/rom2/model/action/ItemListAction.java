package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.net.CBufferManager;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CMultiShopShelf;
import ua.millfreedom.rom2.model.CGameSession;
import ua.millfreedom.rom2.model.Inventory;
import ua.millfreedom.rom2.model.Item;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.Token;
import ua.millfreedom.rom2.model.TokenEntry;
import ua.millfreedom.rom2.model.actiondata.ActionPayloads;
import ua.millfreedom.rom2.model.actiondata.ItemInfoPacketHeader;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.gameobj.CGameObject;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.unit.humanoid.Humanoid;
import ua.millfreedom.rom2.model.visobj.BasicInnDialogVisualObject;
import ua.millfreedom.rom2.model.visobj.CVisualObject;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;
import ua.millfreedom.rom2.model.visobj.ShopDialogVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.INN_DIALOG;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_NOW_86;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_PICKED_UP_85;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_PIECES_87;

/**
 * Fixed-header + optional trailing data packet (ID 0x76).
 */
public class ItemListAction extends CGameAction {
    public static final int ACTION_ID = GameActionId.ITEM_LIST_ACTION_76.id;
    public static final ItemListAction global = new ItemListAction();

    // Native subtype literal compared at CServerApp::sendDialogItemList @0050344E.
    private static final int SHOP_TRANSFER_DIALOG_ITEM_LIST_SUBTYPE = 4;
    // Native subtype literal for Inn reward dialog items in MapVisualObject::HandleGameAction @00412711.
    private static final int INN_REWARD_DIALOG_ITEM_LIST_SUBTYPE = 9;
    // Native item-list payload flag consumed by MapVisualObject::HandleGameAction @0040D9B2 subtype 2.
    private static final int PICKUP_ENTRY_FLAG = 0x40;
    // Native timed pickup line lifetime used by MapVisualObject::HandleGameAction @0040D9B2.
    private static final int PICKUP_LINE_LIFETIME_MS = 3000;
    // Native CUnit unitFlags bit checked by MapVisualObject::HandleGameAction @0040D9B2 selected-character item writes.
    private static final int SAVED_CHARACTER_ITEM_LIST_SOURCE_FLAG = 0x20;

    //0x0A
    public final Property<Integer> itemListSubjectTokenId = u16(BODY_OFFSET);
    //0x0C
    public final Property<Integer> actionSubtypeAndFlags = u8(BODY_OFFSET + Short.BYTES);
    //0x0D
    public final Property<Integer> itemCount = u16(BODY_OFFSET + 3);
    //0x0F
    public final Property<Integer> headerExtraWord = u16(BODY_OFFSET + 5);
    //0x11
    public final Property<Integer> trailingDataLength = u16(BODY_OFFSET + 7);
    //0x13
    public final Property<byte[]> trailingData = bytes(
            BODY_OFFSET + 9,
            () -> Math.max(trailingDataLength.get(), 0)
    );

    /**
     * Native: ItemListAction::ItemListAction @0050C7F5.
     * Fully ported.
     */
    public ItemListAction() {
        super();
        ID.set(ACTION_ID);
        itemListSubjectTokenId.set(0);
        actionSubtypeAndFlags.set(0);
        itemCount.set(0);
        headerExtraWord.set(0);
        trailingDataLength.set(0);
        trailingData.set(new byte[0]);
    }

    /**
     * Native: ItemListAction::prepareItemPayloadCapacity @0050C9CA.
     * Fully ported.
     */
    public void prepareItemPayloadCapacity(@SuppressWarnings("unused") int itemCount) {
    }

    /**
     * Native: ItemListAction::reserveItemInfoPacketHeader @0050C953.
     * Fully ported.
     */
    public int reserveItemInfoPacketHeader() {
        int headerOffset = trailingDataLength.get();
        itemCount.set(itemCount.get() + 1);
        ActionPayloads.appendTrailingShort(trailingDataLength, trailingData, 0);
        ActionPayloads.appendTrailingShort(trailingDataLength, trailingData, 0);
        ActionPayloads.appendTrailingByte(trailingDataLength, trailingData, 0);
        ActionPayloads.appendTrailingByte(trailingDataLength, trailingData, 0);
        ActionPayloads.appendTrailingByte(trailingDataLength, trailingData, 0);
        return headerOffset;
    }

    /**
     * Native: ItemListAction::appendItemInfoByteDescriptor @0050C9D7.
     * Fully ported.
     */
    public void appendItemInfoByteDescriptor(int descriptorId, int value, ItemInfoPacketHeader header) {
        ActionPayloads.appendTrailingByte(trailingDataLength, trailingData, descriptorId);
        ActionPayloads.appendTrailingByte(trailingDataLength, trailingData, value);
        header.descriptorCount += 1;
        header.descriptorByteLength += 2;
    }

    /**
     * Native: ItemListAction::appendItemInfoShortDescriptor @0050CA4A.
     * Fully ported.
     */
    @SuppressWarnings("unused")
    public void appendItemInfoShortDescriptor(int descriptorId, int value, ItemInfoPacketHeader header) {
        ActionPayloads.appendTrailingByte(trailingDataLength, trailingData, descriptorId);
        ActionPayloads.appendTrailingShort(trailingDataLength, trailingData, value);
        header.descriptorCount += 1;
        header.descriptorByteLength += 3;
    }

    /**
     * Native: ItemListAction::appendItemInfoIntDescriptor @0050CABF.
     * Fully ported.
     */
    public void appendItemInfoIntDescriptor(int descriptorId, int value, ItemInfoPacketHeader header) {
        ActionPayloads.appendTrailingByte(trailingDataLength, trailingData, descriptorId);
        ActionPayloads.appendTrailingInt(trailingDataLength, trailingData, value);
        header.descriptorCount += 1;
        header.descriptorByteLength += 5;
    }

    /**
     * Native: ItemListAction::appendItemInfoByteZeroByteDescriptor @0050CB32.
     * Fully ported.
     */
    @SuppressWarnings("unused")
    public void appendItemInfoByteZeroByteDescriptor(
            int descriptorId,
            int lowValue,
            int highValue,
            ItemInfoPacketHeader header
    ) {
        ActionPayloads.appendTrailingByte(trailingDataLength, trailingData, descriptorId);
        ActionPayloads.appendTrailingByte(trailingDataLength, trailingData, lowValue);
        ActionPayloads.appendTrailingByte(trailingDataLength, trailingData, 0);
        ActionPayloads.appendTrailingByte(trailingDataLength, trailingData, highValue);
        header.descriptorCount += 1;
        header.descriptorByteLength += 4;
    }

    /**
     * Native support extracted from CUnit::CopyFromRuntimeUnit @004699D2 item-list global rebuild and
     * GameServer::Save @004E9E97 primary saved-character equipment section construction.
     */
    public static ItemListAction prepareForRuntimeUnitEquipmentSnapshot(Humanoid unit) {
        ItemListAction action = global;
        action.itemCount.set(0);
        action.headerExtraWord.set(0);
        action.trailingDataLength.set(0);
        action.trailingData.set(new byte[0]);
        appendRuntimeEquipmentSnapshotItem(action, unit.pWeapon);
        appendRuntimeEquipmentSnapshotItem(action, unit.pShield);
        for (int slotIndex = 2; slotIndex < unit.items.length; slotIndex++) {
            appendRuntimeEquipmentSnapshotItem(action, unit.items[slotIndex]);
        }
        return action;
    }

    /**
     * Native support extracted from CUnit::CopyFromRuntimeUnit @004699D2 item payload branches.
     */
    private static void appendRuntimeEquipmentSnapshotItem(ItemListAction action, Item item) {
        Item payloadItem = item == null ? new Item() : item;
        payloadItem.appendNetworkItemPayload(action, false);
    }

    /**
     * Native support extracted from CServerApp::sendDialogItemList @0050338E packet field writes.
     * Fully ported.
     */
    public static ItemListAction prepareForDialogItemList(
            Token dialogOwner,
            Inventory inventory,
            Player targetPlayer,
            int actionSubtype
    ) {
        ItemListAction action = global;
        action.ID.set(ACTION_ID);
        action.itemCount.set(0);
        action.headerExtraWord.set(0);
        action.trailingDataLength.set(0);
        action.trailingData.set(new byte[0]);
        action.itemListSubjectTokenId.set(dialogOwner.idFull);
        int playerId;
        if (targetPlayer == null) {
            Globals.gameServer.pushMessage("Error - notify about shop table contents to all");
            playerId = 0;
        } else {
            playerId = targetPlayer.playerId;
        }
        action.playerID.set(playerId & 0xFFFF);
        action.actionSubtypeAndFlags.set(actionSubtype);
        action.prepareItemPayloadCapacity(inventory.items.size());
        for (Item item : inventory.items) {
            item.appendNetworkItemPayload(action, actionSubtype == SHOP_TRANSFER_DIALOG_ITEM_LIST_SUBTYPE);
            action.itemCount.set(action.itemCount.get() + 1);
        }
        return action;
    }

    /**
     * Native support extracted from CServerApp::sendShopCatalogShelfItemList @00503493 packet field writes.
     * Fully ported.
     */
    public static ItemListAction prepareForShopCatalogShelfItemList(
            Token dialogOwner,
            CMultiShopShelf shelf,
            Player targetPlayer,
            int actionSubtype
    ) {
        ItemListAction action = global;
        action.ID.set(ACTION_ID);
        int playerId;
        if (targetPlayer == null) {
            Globals.gameServer.pushMessage("Error - notify about shop contents to all");
            playerId = 0;
        } else {
            playerId = targetPlayer.playerId;
        }
        action.playerID.set(playerId & 0xFFFF);
        action.itemListSubjectTokenId.set(dialogOwner.idFull);
        action.actionSubtypeAndFlags.set(actionSubtype);
        action.itemCount.set(0);
        action.headerExtraWord.set(0);
        action.trailingDataLength.set(0);
        action.trailingData.set(new byte[0]);
        Item emptyItem = new Item();
        int itemCount = shelf.getItemsCount();
        for (int itemIndex = 0; itemIndex < itemCount; itemIndex++) {
            Item item = shelf.getItemAt(itemIndex);
            Item payloadItem = item == null ? emptyItem : item;
            payloadItem.appendNetworkItemPayload(action, false);
            action.itemCount.set(action.itemCount.get() + 1);
        }
        return action;
    }

    /**
     * Native: ItemListAction::ItemListAction @0050C852.
     * Fully ported.
     */
    public ItemListAction(ItemListAction from) {
        super();
        ID.set(from.ID.get());
        itemListSubjectTokenId.set(from.itemListSubjectTokenId.get());
        actionSubtypeAndFlags.set(from.actionSubtypeAndFlags.get());
        itemCount.set(from.itemCount.get());
        headerExtraWord.set(from.headerExtraWord.get());
        trailingDataLength.set(from.trailingDataLength.get());
        trailingData.set(from.trailingData.get());
    }

    /**
     * vtbl +0x04: ItemListAction::Clone @00541A10.
     * Fully ported.
     */
    @Override
    public ItemListAction Clone() {
        return new ItemListAction(this);
    }

    /**
     * vtbl +0x10: ItemListAction::GetPayloadSize @00541A90.
     * Fully ported.
     */
    @Override
    public int GetPayloadSize() {
        return trailingDataLength.get() + 10;
    }

    /**
     * Native support extracted from writeEncryptedSaveSections @004EDBAF primary/secondary item-list section branches.
     * Fully ported.
     */
    public byte[] packSavedCharacterItemListSection() {
        return GetSlice(BODY_OFFSET, trailingDataLength.get() + 9);
    }

    /**
     * vtbl +0x08: ItemListAction::WritePayload @0050C8AB.
     * Fully ported.
     */
    @Override
    public boolean WritePayload(CBufferManager target) {
        boolean headerResult = target.Write(this, ID_OFFSET, 10);
        if (itemCount.get() == 0) {
            return headerResult;
        }
        return target.Write(this, BODY_OFFSET + 9, trailingDataLength.get());
    }

    /**
     * vtbl +0x0C: ItemListAction::ReadPayload @0050C907.
     * Fully ported.
     */
    @Override
    public boolean ReadPayload(CBufferManager source) {
        source.Read(this, BODY_OFFSET, 9);
        if (itemCount.get() <= 0) {
            return false;
        }
        return source.Read(this, BODY_OFFSET + 9, trailingDataLength.get());
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040F67B / @0040FE15 / @004105B7.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        int subtype = actionSubtypeAndFlags.get() & 0x7F;
        if (subtype == 1) {
            CGameObject target = mapVisualObject.getObjectByToken((short) (int) itemListSubjectTokenId.get());
            if (target instanceof CUnit unit) {
                byte[] itemPayload = trailingData.get();
                MapVisualObject.applyUnitEquipmentPayload(unit, itemPayload, headerExtraWord.get());
                refreshSavedCharacterEquipmentItems(mapVisualObject, unit, itemPayload);
            }
            routeShopItemListAction(subtype, null);
            return;
        }
        if (subtype == 2) {
            applyUnitInventoryPayload(mapVisualObject);
            return;
        }

        List<TokenEntry> entries = MapVisualObject.readTokenEntries(
                trailingData.get(),
                itemCount.get(),
                subtype == 4
        );
        if (subtype >= 4 && subtype <= 8) {
            filterKnownShopGridEntries(entries);
            if (routeShopItemListAction(subtype, entries)) {
                return;
            }
        }
        if (subtype == INN_REWARD_DIALOG_ITEM_LIST_SUBTYPE && routeInnRewardItemListAction(entries)) {
            return;
        }

        mapVisualObject.dialogItemTokenEntries.clear();
        mapVisualObject.dialogItemTokenEntries.addAll(entries);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00412711-00412817.
     */
    private boolean routeInnRewardItemListAction(List<TokenEntry> entries) {
        if (!INN_DIALOG.isSetIn(Globals.mainWindow.dialogsMask)) {
            return false;
        }
        BasicInnDialogVisualObject innDialog =
                (BasicInnDialogVisualObject) Globals.mainWindow.inputController.getChildById(0x44C);
        for (TokenEntry entry : entries) {
            if (BasicInnDialogVisualObject.isVisibleInnRewardEntry(entry)) {
                innDialog.activeRewardTokenEntries.add(entry);
            }
        }
        return true;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2 item-list subtype 2 branch.
     */
    private void applyUnitInventoryPayload(MapVisualObject mapVisualObject) {
        CGameObject target = mapVisualObject.getObjectByToken((short) (int) itemListSubjectTokenId.get());
        if (!(target instanceof CUnit unit)) {
            return;
        }

        List<TokenEntry> inventoryEntries = unit.tokenEntries;
        int startIndex = headerExtraWord.get();
        int count = itemCount.get();
        boolean partialRange = (actionSubtypeAndFlags.get() & 0x80) != 0;
        if (!partialRange) {
            inventoryEntries.clear();
        } else {
            removeTrailingMoneyEntry(inventoryEntries);
            int nonMoneyEntryCount = inventoryEntries.size();
            if (nonMoneyEntryCount <= startIndex) {
                return;
            }
            if (nonMoneyEntryCount < startIndex + count) {
                count = nonMoneyEntryCount - startIndex;
            }
            removeInventoryRange(inventoryEntries, startIndex, count);
        }

        List<TokenEntry> entries = MapVisualObject.readTokenEntries(trailingData.get(), count, false);
        refreshSavedCharacterInventoryItems(mapVisualObject, unit, startIndex, count, partialRange);
        boolean attachMoneyEntry = mapVisualObject.shouldAttachHeroInventoryMoneyEntry(unit);
        for (int index = 0; index < entries.size(); index++) {
            TokenEntry entry = entries.get(index);
            entry.gridModeCode = 2;
            addPickedUpItemLine(mapVisualObject, entry);
            int targetIndex = startIndex + index;
            if (targetIndex < inventoryEntries.size()) {
                inventoryEntries.add(targetIndex, entry);
            } else {
                inventoryEntries.add(entry);
            }
        }
        if (attachMoneyEntry) {
            inventoryEntries.add(createHeroInventoryMoneyEntry(mapVisualObject));
        }
        mapVisualObject.notifySelectionUi();
        if (routeShopItemListAction(2, null)) {
            return;
        }
        mapVisualObject.refreshHeroInventoryBindingForInventoryUnit(unit);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2 item-list subtype `1`
     * selected-character primary item-list write branch.
     */
    private void refreshSavedCharacterEquipmentItems(MapVisualObject mapVisualObject, CUnit unit, byte[] itemPayload) {
        if (!shouldRefreshSavedCharacterItemLists(unit)) {
            return;
        }

        ByteBuffer cursor = ByteBuffer.wrap(itemPayload).order(ByteOrder.LITTLE_ENDIAN);
        for (int slotIndex = 0; slotIndex < CGameSession.SAVED_CHARACTER_ITEM_SLOT_COUNT; slotIndex++) {
            if ((headerExtraWord.get() & (1 << slotIndex)) != 0) {
                mapVisualObject.savedCharacterEquipmentItems[slotIndex] = Item.readSavedCharacterItemPayload(cursor);
            }
        }
        Globals.mainWindow.m_GameSession.refreshSavedCharacterPrimaryItemList(
                prepareSavedCharacterItemList(mapVisualObject.savedCharacterEquipmentItems)
        );
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2 item-list subtype `2`
     * selected-character secondary item-list write branch.
     */
    private void refreshSavedCharacterInventoryItems(
            MapVisualObject mapVisualObject,
            CUnit unit,
            int startIndex,
            int count,
            boolean partialRange
    ) {
        if (!shouldRefreshSavedCharacterItemLists(unit)) {
            return;
        }

        List<Item> inventoryItems = mapVisualObject.savedCharacterInventoryItems;
        if (!partialRange) {
            inventoryItems.clear();
        } else {
            removeSavedCharacterInventoryRange(inventoryItems, startIndex, count);
        }

        ByteBuffer cursor = ByteBuffer.wrap(trailingData.get()).order(ByteOrder.LITTLE_ENDIAN);
        for (int index = 0; index < count; index++) {
            Item item = Item.readSavedCharacterItemPayload(cursor);
            int targetIndex = startIndex + index;
            if (targetIndex < inventoryItems.size()) {
                inventoryItems.add(targetIndex, item);
            } else {
                inventoryItems.add(item);
            }
        }
        Globals.mainWindow.m_GameSession.refreshSavedCharacterSecondaryItemList(
                prepareSavedCharacterItemList(inventoryItems)
        );
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2 selected-character item-list guard.
     */
    private static boolean shouldRefreshSavedCharacterItemLists(CUnit unit) {
        return Globals.mainWindow.sessionMode != CMainWindow.SESSION_MODE_CAMPAIGN
                && (unit.unitFlags & SAVED_CHARACTER_ITEM_LIST_SOURCE_FLAG) != 0;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2 selected-character inventory cache update.
     */
    private static void removeSavedCharacterInventoryRange(List<Item> inventoryItems, int startIndex, int count) {
        for (int index = 0; index < count && startIndex < inventoryItems.size(); index++) {
            inventoryItems.remove(startIndex);
        }
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2 selected-character item-list rebuild loop.
     */
    private static ItemListAction prepareSavedCharacterItemList(Item[] items) {
        ItemListAction savedItems = new ItemListAction();
        for (Item item : items) {
            appendSavedCharacterItem(savedItems, item);
        }
        return savedItems;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2 selected-character item-list rebuild loop.
     */
    private static ItemListAction prepareSavedCharacterItemList(List<Item> items) {
        ItemListAction savedItems = new ItemListAction();
        for (Item item : items) {
            appendSavedCharacterItem(savedItems, item);
            savedItems.itemCount.set(savedItems.itemCount.get() + 1);
        }
        return savedItems;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2 selected-character item-list rebuild loop.
     */
    private static void appendSavedCharacterItem(ItemListAction savedItems, Item item) {
        Item payloadItem = item == null ? new Item() : item;
        payloadItem.appendNetworkItemPayload(savedItems, false);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2 item-list subtype 2 pickup-line branch.
     */
    private static void addPickedUpItemLine(MapVisualObject mapVisualObject, TokenEntry entry) {
        if ((entry.wireFlags & PICKUP_ENTRY_FLAG) == 0) {
            return;
        }
        entry.wireFlags &= ~PICKUP_ENTRY_FLAG;
        String itemName = Globals.itemNames.resolveItemNameFromHash(entry.packedTokenHash);
        String line = entry.quantity < 2
                ? get(MAIN_PICKED_UP_85) + " " + itemName
                : get(MAIN_PICKED_UP_85) + " " + itemName + " ("
                  + get(MAIN_NOW_86) + " " + entry.quantity + " " + get(MAIN_PIECES_87) + ")";
        mapVisualObject.gameListControl.addTimedLine(line, Palettes.messageDim(), PICKUP_LINE_LIFETIME_MS);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2 partial inventory replacement.
     */
    private static void removeInventoryRange(List<TokenEntry> inventoryEntries, int startIndex, int count) {
        for (int index = 0; index < count && startIndex < inventoryEntries.size(); index++) {
            inventoryEntries.remove(startIndex);
        }
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2 existing trailing money entry removal.
     */
    private static void removeTrailingMoneyEntry(List<TokenEntry> inventoryEntries) {
        if (!inventoryEntries.isEmpty() && inventoryEntries.getLast().isMoneyEntry()) {
            inventoryEntries.removeLast();
        }
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2 money entry append after unit inventory entries.
     */
    private static TokenEntry createHeroInventoryMoneyEntry(MapVisualObject mapVisualObject) {
        TokenEntry money = new TokenEntry(0);
        money.packedTokenHash = TokenEntry.MONEY_ENTRY_HASH;
        money.quantity = mapVisualObject.currentPlayer.gold;
        money.wireFlags = 0;
        money.payloadEntryCount = 0;
        money.gridModeCode = 2;
        return money;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2 item-list subtype 4..8 branch.
     */
    private static void filterKnownShopGridEntries(List<TokenEntry> entries) {
        entries.removeIf(entry -> !entry.hasKnownItemName());
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2 item-list subtype 1, 2, and 4..8 branches and
     * ShopDialogVisualObject::OnMessage @004B7102.
     */
    private static boolean routeShopItemListAction(int subtype, Object entries) {
        CVisualObject inputController = Globals.mainWindow.getInputController();
        if (inputController == null) {
            return false;
        }
        CVisualObject root = inputController.getChildById(1000);
        if (!(root instanceof ShopDialogVisualObject shopDialog)) {
            return false;
        }
        shopDialog.onMessage(MessageCodes.SHOP_ITEM_GRID_TRANSFER, subtype, entries);
        return true;
    }
}
