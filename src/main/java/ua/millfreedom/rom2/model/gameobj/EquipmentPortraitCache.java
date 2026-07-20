package ua.millfreedom.rom2.model.gameobj;

import ua.millfreedom.rom2.model.CBmp256;
import ua.millfreedom.rom2.model.CBmp64k;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Java in-memory replacement for native dynamic equipment portrait temp BMP files.
 */
final class EquipmentPortraitCache {
    private static final int MAX_ENTRIES = 32;

    private static final Map<String, Entry> ENTRIES = new LinkedHashMap<>(MAX_ENTRIES, 0.75f, true) {
        /**
         * Java LRU eviction hook.
         * not ported.
         */
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Entry> eldest) {
            return size() > MAX_ENTRIES;
        }
    };

    /**
     * Java utility constructor.
     * not ported.
     */
    private EquipmentPortraitCache() {
    }

    /**
     * Java in-memory replacement for native temp portrait BMP reloads.
     * not ported.
     */
    static synchronized boolean restore(String key, CBmp64k targetBitmap, CBmp256 maskBitmap) {
        Entry entry = ENTRIES.get(key);
        if (entry == null) {
            return false;
        }

        entry.copyTo(targetBitmap, maskBitmap);
        return true;
    }

    /**
     * Java in-memory replacement for native CBmp64k::DumpBmp24PixelsWithMask @00425273 cache writes.
     */
    static synchronized void store(String key, CBmp64k targetBitmap, CBmp256 maskBitmap) {
        ENTRIES.put(key, Entry.capture(targetBitmap, maskBitmap));
    }

    private static final class Entry {
        private final int[] targetPixels;
        private final int[] maskCodes;

        /**
         * Java cache snapshot constructor.
         * not ported.
         */
        private Entry(int[] targetPixels, int[] maskCodes) {
            this.targetPixels = targetPixels;
            this.maskCodes = maskCodes;
        }

        /**
         * Java cache snapshot capture.
         * not ported.
         */
        private static Entry capture(CBmp64k targetBitmap, CBmp256 maskBitmap) {
            int[] maskCopy = maskBitmap == null ? null : Arrays.copyOf(
                    maskBitmap.frames.getFirst().pixels(),
                    maskBitmap.frames.getFirst().pixels().length
            );
            return new Entry(
                    Arrays.copyOf(targetBitmap.surface.pixels(), targetBitmap.surface.pixels().length),
                    maskCopy
            );
        }

        /**
         * Java cache snapshot copy into retained UI bitmaps.
         * not ported.
         */
        private void copyTo(CBmp64k targetBitmap, CBmp256 maskBitmap) {
            System.arraycopy(targetPixels, 0, targetBitmap.surface.pixels(), 0, targetPixels.length);
            if (maskBitmap != null && maskCodes != null) {
                System.arraycopy(maskCodes, 0, maskBitmap.frames.getFirst().pixels(), 0, maskCodes.length);
            }
        }
    }
}
