package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.text.ItemNameText;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedHashMap;
import java.util.Map;

import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.TextTableId.ITEMNAME;

/**
 * Java/Ghidra grouping owner for native shared item-name globals.
 */
public final class ItemNames {
    // Native g_itemNameByHash @00622640, populated by ItemNames::loadItemNames @00474268.
    // Static lifecycle thunks @0047401B/@0047402A/@0047403B/@0047404D are represented by Java object lifecycle.
    public final Map<Integer, ItemNameText> itemNameByHash = new LinkedHashMap<>();

    // Native g_payloadTemplateByHash @006225C0, populated by ItemNames::loadItemNames @00474268.
    // Static lifecycle thunks @0047405C/@0047406B/@0047407C/@0047408E are represented by Java object lifecycle.
    public final Map<Integer, ItemPayloadTemplate> payloadTemplateByHash = new LinkedHashMap<>();

    /**
     * Java support constructor for the shared item-name wrapper.
     * not ported.
     */
    public ItemNames() {
    }

    /**
     * Native: ItemNames::loadItemNames @00474268.
     * Java port status: fully ported.
     */
    public void loadItemNames() {
        itemNameByHash.clear();
        payloadTemplateByHash.clear();
        ByteBuffer countSource = Globals.gameFileManager.get("world/data/itemname.bin")
                .duplicate()
                .order(ByteOrder.LITTLE_ENDIAN);
        int entryCount = countSource.remaining() / Short.BYTES;
        for (int index = 0; index < entryCount; index++) {
            int hash = Short.toUnsignedInt(countSource.getShort());
            itemNameByHash.put(hash, ItemNameText.byIndex(index));
        }

        ByteBuffer payloadSource = Globals.gameFileManager.get("world/data/itemname.pkt")
                .duplicate()
                .order(ByteOrder.LITTLE_ENDIAN);
        payloadSource.position(9);
        for (int index = 0; index < entryCount; index++) {
            int entryStart = payloadSource.position();
            int hash = Short.toUnsignedInt(payloadSource.getShort(entryStart));
            int staticFlags = Byte.toUnsignedInt(payloadSource.get(entryStart + 4));
            int payloadEntryCount = Byte.toUnsignedInt(payloadSource.get(entryStart + 5));
            int payloadSize = Byte.toUnsignedInt(payloadSource.get(entryStart + 6));
            byte[] payloadBytes = new byte[payloadSize];
            payloadSource.position(entryStart + 7);
            payloadSource.get(payloadBytes);

            if ((hash & 0x0F00) == 0x0E00) {
                MagicalItemInfo magicItem = Globals.staticDataMgr.magicItems.get(hash & 0xFF);
                int price = magicItem.getValue(0);
                ByteBuffer.wrap(payloadBytes).order(ByteOrder.LITTLE_ENDIAN).putInt(1, price);
            }

            payloadTemplateByHash.put(hash, new ItemPayloadTemplate(staticFlags, payloadEntryCount, payloadBytes));
            payloadSource.position(entryStart + 7 + payloadSize);
        }
    }

    /**
     * Native support over DAT_006225C0 initialized by ItemNames::loadItemNames @00474268.
     */
    public ItemPayloadTemplate lookupStaticPayloadTemplate(int packedTokenHash) {
        return payloadTemplateByHash.get(packedTokenHash & 0xFFFF);
    }

    /**
     * Native support over CMapWordToPtr_00622640 initialized by ItemNames::loadItemNames @00474268.
     */
    public String resolveItemNameFromHash(int packedTokenHash) {
        ItemNameText itemName = itemNameByHash.get(packedTokenHash & 0xFFFF);
        return itemName == null ? "" : get(ITEMNAME, itemName);
    }
}
