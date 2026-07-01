package ua.millfreedom.rom2.text;

import ua.millfreedom.rom2.CTextFile;

import java.util.Objects;

/**
 * Wrapper around the native g_StringTable-style text access patterns.
 */
public final class GameTexts {
    /**
     * Utility class constructor. not ported.
     */
    private GameTexts() {
    }

    /**
     * Reads a value by flat g_StringTable index. not ported.
     */
    public static String get(int globalIndex) {
        TextTableId tableId = TextTableId.fromGlobalIndex(globalIndex);
        return get(tableId, globalIndex - tableId.globalStartIndex());
    }

    /**
     * Reads a value by table-local enum entry. not ported.
     */
    public static String get(TextTableLocalEntry entry) {
        Objects.requireNonNull(entry, "entry");
        return get(entry.tableId(), entry.index());
    }

    /**
     * Reads a value by explicit table id and matching table-local enum entry. not ported.
     */
    public static String get(TextTableId tableId, TextTableLocalEntry entry) {
        Objects.requireNonNull(tableId, "tableId");
        Objects.requireNonNull(entry, "entry");
        if (entry.tableId() != tableId) {
            throw new IllegalArgumentException(
                    "Mismatched text table entry: expected " + tableId + " but got " + entry.tableId()
            );
        }
        return get(tableId, entry.index());
    }

    /**
     * Reads a value by explicit table id and local index. not ported.
     */
    public static String get(TextTableId tableId, int index) {
        Objects.requireNonNull(tableId, "tableId");
        return CTextFile.GetValue(tableId.arrayName(), index);
    }
}
