package ua.millfreedom.rom2.model;

/**
 * Recovered 0x14 native shop-assortment record returned by ScenarioGetShopAssortment @100037e8
 * and consumed by CMultiShopTemplate::FUN_00522767 @00522767 / FUN_00521d4c @00521d4c.
 */
public final class ShopAssortmentEntry {
    //0x00
    public int minPrice;
    //0x04
    public int maxPrice;
    //0x08
    public int itemCount;
    //0x0C
    public int maxSameTypeItems;
    //0x10
    public int selectionMask;
}
