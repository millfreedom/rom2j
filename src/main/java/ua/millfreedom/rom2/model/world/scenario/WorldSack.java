package ua.millfreedom.rom2.model.world.scenario;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public final class WorldSack {
    //0x00
    public final List<Integer> itemPackedHashes = new ArrayList<>();
    //0x14
    public final List<Integer> effectIndices = new ArrayList<>();
    //0x28
    public final List<Integer> incomingItemFlags = new ArrayList<>();
    //0x3C
    public int unitID;
    //0x40
    public int x;
    //0x44
    public int y;
    //0x48
    public int gold;

    /**
     * Native: WorldSack::WorldSack @00536C67. Fully ported.
     */
    public WorldSack() {
    }

    /**
     * Native: WorldSack::copyConstructor @00536CEC. Fully ported.
     */
    public WorldSack(WorldSack source) {
        unitID = source.unitID;
        x = source.x;
        y = source.y;
        gold = source.gold;
        itemPackedHashes.addAll(source.itemPackedHashes);
        incomingItemFlags.addAll(source.incomingItemFlags);
        effectIndices.addAll(source.effectIndices);
    }

    /**
     * Native: WorldSack::addItemPackedHash @00536DF4. Fully ported.
     */
    public void addItemPackedHash(int itemPackedHash) {
        itemPackedHashes.add(itemPackedHash);
        incomingItemFlags.add(0);
        effectIndices.add(0);
    }

    /**
     * Native support extracted from ScenarioDescriptor::ScenarioDescriptor @00534AD4 sack-record materialization.
     * Fully ported.
     */
    public static WorldSack read(ByteBuffer section, int version) {
        WorldSack sack = new WorldSack();
        int itemCount = section.getInt();
        sack.unitID = section.getInt();
        sack.x = section.getInt();
        sack.y = section.getInt();
        if (version >= 0x3dd) {
            sack.gold = section.getInt();
        }
        for (int itemIndex = 0; itemIndex < itemCount; itemIndex++) {
            sack.itemPackedHashes.add(section.getInt());
            sack.incomingItemFlags.add(Short.toUnsignedInt(section.getShort()));
            sack.effectIndices.add(section.getInt());
        }
        return sack;
    }
}
