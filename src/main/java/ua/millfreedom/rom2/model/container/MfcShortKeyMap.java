package ua.millfreedom.rom2.model.container;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Java support for VC97 {@code CMap<short, ...>} iteration order.
 * not ported.
 */
public final class MfcShortKeyMap<V> extends TreeMap<Short, V> {
    private static final int DEFAULT_HASH_TABLE_SIZE = 17;

    private final Map<Short, Integer> insertionPositions;
    private int nextInsertionPosition;

    /**
     * Java support for the VC97 default {@code CMap<short, ...>} constructor.
     * not ported.
     */
    public MfcShortKeyMap() {
        this(DEFAULT_HASH_TABLE_SIZE);
    }

    /**
     * Java support for a VC97 {@code CMap<short, ...>} with a known hash table size.
     * not ported.
     */
    public MfcShortKeyMap(int hashTableSize) {
        this(hashTableSize, new HashMap<>());
    }

    /**
     * Java support for copying entries while treating source iteration order as insertion history.
     * not ported.
     */
    public MfcShortKeyMap(Map<Short, ? extends V> entries) {
        this();
        putAll(entries);
    }

    /**
     * Java support for binding comparator state before the backing {@link TreeMap} is initialized.
     * not ported.
     */
    private MfcShortKeyMap(int hashTableSize, Map<Short, Integer> insertionPositions) {
        super((left, right) -> compareNativeShortKeys(hashTableSize, insertionPositions, left, right));
        this.insertionPositions = insertionPositions;
    }

    /**
     * Java support for VC97 {@code CMap::operator[]} new-association insertion.
     * not ported.
     */
    @Override
    public V put(Short key, V value) {
        if (!containsKey(key)) {
            insertionPositions.put(key, nextInsertionPosition++);
        }
        return super.put(key, value);
    }

    /**
     * Java support for deterministic bulk insertion using source map iteration order.
     * not ported.
     */
    @Override
    public void putAll(Map<? extends Short, ? extends V> map) {
        for (Map.Entry<? extends Short, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Java support for VC97 {@code CMap::RemoveKey} insertion-history updates.
     * not ported.
     */
    @Override
    public V remove(Object key) {
        boolean contained = containsKey(key);
        V removed = super.remove(key);
        if (contained) {
            insertionPositions.remove(key);
        }
        return removed;
    }

    /**
     * Java support for VC97 {@code CMap::RemoveAll}.
     * not ported.
     */
    @Override
    public void clear() {
        super.clear();
        insertionPositions.clear();
        nextInsertionPosition = 0;
    }

    /**
     * Java support for VC97 {@code HashKey<short>} bucket and bucket-chain traversal order.
     * not ported.
     */
    private static int compareNativeShortKeys(
            int hashTableSize,
            Map<Short, Integer> insertionPositions,
            Short left,
            Short right
    ) {
        if (left.equals(right)) {
            return 0;
        }
        int leftBucket = nativeShortBucket(left, hashTableSize);
        int rightBucket = nativeShortBucket(right, hashTableSize);
        if (leftBucket != rightBucket) {
            return Integer.compare(leftBucket, rightBucket);
        }
        Integer leftPosition = insertionPositions.get(left);
        Integer rightPosition = insertionPositions.get(right);
        if (leftPosition != null && rightPosition != null && !leftPosition.equals(rightPosition)) {
            return Integer.compare(rightPosition, leftPosition);
        }
        if (leftPosition != null) {
            return -1;
        }
        if (rightPosition != null) {
            return 1;
        }
        return Short.compare(left, right);
    }

    /**
     * Java support for VC97 generic {@code HashKey(ARG_KEY key) >> 4} behavior on short keys.
     * not ported.
     */
    private static int nativeShortBucket(short key, int hashTableSize) {
        return (key >>> 4) % hashTableSize;
    }
}
