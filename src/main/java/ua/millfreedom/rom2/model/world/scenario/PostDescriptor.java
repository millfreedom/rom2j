package ua.millfreedom.rom2.model.world.scenario;

import java.nio.ByteBuffer;

public final class PostDescriptor {
    //0x00
    public int id;
    //0x04
    public int instanceOn;
    //0x08
    public int instanceID;

    /**
     * Native support extracted from ScenarioDescriptor::ScenarioDescriptor @00534AD4 descriptors section.
     * Fully ported.
     */
    public static PostDescriptor read(ByteBuffer section) {
        PostDescriptor descriptor = new PostDescriptor();
        descriptor.id = section.getInt();
        descriptor.instanceOn = section.getInt();
        descriptor.instanceID = section.getInt();
        return descriptor;
    }
}
