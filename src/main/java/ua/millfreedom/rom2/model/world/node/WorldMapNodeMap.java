package ua.millfreedom.rom2.model.world.node;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CMap<short,UNK0x3c>::Serialize at FUN_0055f6b0 (0x0055f6b0).
 */
public final class WorldMapNodeMap implements MfcSerializable {
    public final Map<Integer, WorldMapNode> entries = new LinkedHashMap<>();

    /**
     * Native: CMap<short,UNK0x3c>::Serialize @0055F6B0.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        if (ar.isStoring()) {
            ar.writeCount(entries.size());
            for (Map.Entry<Integer, WorldMapNode> entry : entries.entrySet()) {
                ar.writeShort(entry.getKey());
                ar.serialize(entry.getValue());
            }
        } else {
            entries.clear();
            int count = ar.readCount();
            for (int i = 0; i < count; i++) {
                int key = ar.readUShort();
                WorldMapNode node = new WorldMapNode();
                ar.serialize(node);
                entries.put(key, node);
            }
        }
    }

    /**
     * Native support extracted from CWorldMap::restoreContext @005595E5 node-map traversal.
     */
    public void restoreContext() {
        for (Map.Entry<Integer, WorldMapNode> entry : entries.entrySet()) {
            WorldMapNode node = entry.getValue();
            node.restoreContext();
            entry.setValue(node);
        }
    }
}
