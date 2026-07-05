package ua.millfreedom.rom2.mapeditor;

import ua.millfreedom.rom2.model.CPlayer;

/**
 * Editor-facing diplomacy relation choices from the native MapEditor players dialog.
 * not ported.
 */
public enum MapEditorDiplomacyRelation {
    ENEMY("Enemy", CPlayer.ENEMY_MASK),
    NEUTRAL("Neutral", 0),
    ALLIANCE("Alliance", CPlayer.ALLIED_MASK);

    private final String label;
    private final int baseFlags;

    /**
     * Java support constructor for editor diplomacy choices.
     * not ported.
     */
    MapEditorDiplomacyRelation(String label, int baseFlags) {
        this.label = label;
        this.baseFlags = baseFlags;
    }

    /**
     * Java support accessor for the relation bits represented by this editor choice.
     * not ported.
     */
    public int baseFlags() {
        return baseFlags;
    }

    /**
     * Java support label accessor for Swing controls.
     * not ported.
     */
    public String label() {
        return label;
    }

    /**
     * Java support relation decoder for existing scenario diplomacy words.
     * not ported.
     */
    public static MapEditorDiplomacyRelation fromFlags(int flags) {
        if ((flags & CPlayer.ALLIED_MASK) != 0) {
            return ALLIANCE;
        }
        if ((flags & CPlayer.ENEMY_MASK) != 0) {
            return ENEMY;
        }
        return NEUTRAL;
    }

    /**
     * Java support Swing display string for relation choices.
     * not ported.
     */
    @Override
    public String toString() {
        return label;
    }
}
