package ua.millfreedom.rom2.model;

/**
 * Native class: Diplomacy.
 * Purpose: one row in the diplomacy settings wrapper shown by the main-window diplomacy dialog.
 */
public class Diplomacy {
    //0x0
    public String name;

    //0x4
    public boolean enemy;

    //0x8
    public boolean alliance;

    //0xc
    public boolean visible;

    //0x10
    public boolean silent;

    /**
     * Native: Diplomacy::Diplomacy @00444BF8.
     * Fully ported.
     */
    public Diplomacy(String name, boolean enemy, boolean alliance, boolean visible, boolean silent) {
        this.name = name;
        this.enemy = enemy;
        this.alliance = alliance;
        this.visible = visible;
        this.silent = silent;
    }
}
