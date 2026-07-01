package ua.millfreedom.rom2.model.visobj;

/**
 * Java bridge for linked selection mirrors reached from text-list setters.
 * not ported.
 */
interface TextListSelectionMirror {
    /**
     * Native support target implemented by linked-child mirrors such as
     * PostSetupVisualObject::syncSelectionState @004DA769. not ported.
     */
    void syncSelectionState(int selectedRow, int rowCount);
}
