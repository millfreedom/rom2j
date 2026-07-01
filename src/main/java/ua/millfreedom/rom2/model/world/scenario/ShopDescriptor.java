package ua.millfreedom.rom2.model.world.scenario;

import java.nio.ByteBuffer;

public final class ShopDescriptor {
    //0x00
    public int id;
    //0x04
    public final int[] shelfFlags = new int[4];
    //0x14
    public final int[] minPrices = new int[4];
    //0x24
    public final int[] maxPrices = new int[4];
    //0x34
    public final int[] maxItems = new int[4];
    //0x44
    public final int[] maxSameTypeItems = new int[4];

    /**
     * Native support extracted from ScenarioDescriptor::ScenarioDescriptor @00534AD4 descriptors section.
     * Fully ported.
     */
    public static ShopDescriptor read(ByteBuffer section) {
        ShopDescriptor descriptor = new ShopDescriptor();
        descriptor.id = section.getInt();
        for (int shelf = 0; shelf < descriptor.shelfFlags.length; shelf++) {
            descriptor.shelfFlags[shelf] = section.getInt();
        }
        for (int shelf = 0; shelf < descriptor.minPrices.length; shelf++) {
            descriptor.minPrices[shelf] = section.getInt();
        }
        for (int shelf = 0; shelf < descriptor.maxPrices.length; shelf++) {
            descriptor.maxPrices[shelf] = section.getInt();
        }
        for (int shelf = 0; shelf < descriptor.maxItems.length; shelf++) {
            descriptor.maxItems[shelf] = section.getInt();
        }
        for (int shelf = 0; shelf < descriptor.maxSameTypeItems.length; shelf++) {
            descriptor.maxSameTypeItems[shelf] = section.getInt();
        }
        return descriptor;
    }
}
