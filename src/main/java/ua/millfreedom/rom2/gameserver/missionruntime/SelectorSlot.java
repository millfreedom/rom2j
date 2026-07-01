package ua.millfreedom.rom2.gameserver.missionruntime;

import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.UnitGroup;
import ua.millfreedom.rom2.model.unit.Unit;

/**
 * Native 0x2C-byte selector slot at MissionScriptRuntime +0xC554.
 */
public final class SelectorSlot {
    //0x00
    public int xOrLeft;
    //0x04
    public int yOrTop;
    //0x08
    public int right;
    //0x0C
    public int bottom;
    //0x10
    public int radius;
    //0x14
    public Unit unit;
    //0x18
    public UnitGroup group;
    //0x1C
    public Player player;
    //0x20
    public int field0x20;
    //0x24
    public int enabled;
    //0x28
    public int field0x28;

    /**
     * Native: SelectorSlot::New @0057357D.
     * Fully ported.
     */
    public SelectorSlot() {
    }
}
