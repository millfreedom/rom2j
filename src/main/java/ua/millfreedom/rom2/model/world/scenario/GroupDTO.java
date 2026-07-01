package ua.millfreedom.rom2.model.world.scenario;

import java.nio.ByteBuffer;

public final class GroupDTO {
    //0x00
    public int id;
    //0x04
    public int repopTime;
    //0x08
    public int flags;
    //0x0C
    public int instID;

    /**
     * Native support extracted from ScenarioDescriptor::ScenarioDescriptor @00534AD4 groups section.
     * Fully ported.
     */
    public static GroupDTO read(ByteBuffer section) {
        GroupDTO group = new GroupDTO();
        group.id = section.getInt();
        group.repopTime = section.getInt();
        group.flags = section.getInt();
        group.instID = section.getInt();
        return group;
    }
}
