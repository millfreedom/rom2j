package ua.millfreedom.rom2.model.world.scenario;

import java.nio.ByteBuffer;

public final class InnDescriptor {
    public static final int FLAG_ITEM_DELIVERY = 0x02;
    public static final int FLAG_RAISE_DEAD = 0x04;
    public static final int FLAG_KILL_ALL_HUMANS = 0x10;
    public static final int FLAG_KILL_ALL_MONSTERS = 0x20;
    public static final int FLAG_KILL_ALL_UNDEAD_NECRO = 0x40;

    //0x00
    public int id;
    //0x04
    public int flags;
    //0x08
    public int itemID;

    /**
     * Native support extracted from ScenarioDescriptor::ScenarioDescriptor @00534AD4 descriptors section.
     * Fully ported.
     */
    public static InnDescriptor read(ByteBuffer section) {
        InnDescriptor descriptor = new InnDescriptor();
        descriptor.id = section.getInt();
        descriptor.flags = section.getInt();
        descriptor.itemID = section.getInt();
        return descriptor;
    }
}
