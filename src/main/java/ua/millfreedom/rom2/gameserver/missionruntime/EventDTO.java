package ua.millfreedom.rom2.gameserver.missionruntime;

import java.util.Arrays;

/**
 * Native mission-runtime event record stored in CList<EventDTO> at MissionRuntimeBase +0xBBEC.
 */
public final class EventDTO {
    //0x00; event discriminator: 1 engagement, 2 targeted spell, 3 cell spell, 4 target assignment.
    public int eventCode;
    //0x04; cell spell target X for MissionScriptRuntime::updateUnitMissionRuntime @0056DCA2 event code 3.
    public int cellTargetX;
    //0x08; cell spell target Y for MissionScriptRuntime::updateUnitMissionRuntime @0056DCA2 event code 3.
    public int cellTargetY;
    //0x0C
    public final byte[] reserved0x0c = new byte[0x20];
    //0x2C; Unit* source for event 1, Unit* caster for events 2/3, UnitGroup* group for event 4.
    public Object runtimeSource;
    //0x30; Unit* target for event 1, Spell* for events 2/3.
    public Object runtimePayload;
    //0x34; Token* action target for event 2.
    public Object runtimeActionTarget;
    //0x38
    public final byte[] reserved0x38 = new byte[0x1c];

    /**
     * Native: EventDTO::EventDTO @0057AD79.
     */
    public void initialize() {
        eventCode = 0;
        cellTargetX = 0;
        cellTargetY = 0;
        Arrays.fill(reserved0x0c, (byte) 0);
        runtimeSource = null;
        runtimePayload = null;
        runtimeActionTarget = null;
        Arrays.fill(reserved0x38, (byte) 0);
    }

    /**
     * Native support extracted from CList<EventDTO>::AddTail @0057B210 0x54 by-value copy.
     */
    public EventDTO copy() {
        EventDTO copy = new EventDTO();
        copy.eventCode = eventCode;
        copy.cellTargetX = cellTargetX;
        copy.cellTargetY = cellTargetY;
        System.arraycopy(reserved0x0c, 0, copy.reserved0x0c, 0, reserved0x0c.length);
        copy.runtimeSource = runtimeSource;
        copy.runtimePayload = runtimePayload;
        copy.runtimeActionTarget = runtimeActionTarget;
        System.arraycopy(reserved0x38, 0, copy.reserved0x38, 0, reserved0x38.length);
        return copy;
    }
}
