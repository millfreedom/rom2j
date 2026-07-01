package ua.millfreedom.rom2.text;

/**
 * Table-local text entry contract for generated per-file enums such as MainText or PatchText.
 */
public interface TextTableLocalEntry {
    /**
     * Returns the owning text table id. not ported.
     */
    TextTableId tableId();

    /**
     * Returns the table-local index. not ported.
     */
    int index();
}
