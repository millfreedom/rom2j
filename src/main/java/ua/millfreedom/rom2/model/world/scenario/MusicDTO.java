package ua.millfreedom.rom2.model.world.scenario;

import java.nio.ByteBuffer;

public final class MusicDTO {
    //0x00
    public int x;
    //0x04
    public int y;
    //0x08
    public int radius;
    //0x0C
    public int m1;
    //0x10
    public int m2;
    //0x14
    public int m3;
    //0x18
    public int m4;

    /**
     * Native support extracted from ScenarioDescriptor::ScenarioDescriptor @00534AD4 music section.
     * Fully ported.
     */
    public void read(ByteBuffer section) {
        x = section.getInt();
        y = section.getInt();
        radius = section.getInt();
        m1 = section.getInt();
        m2 = section.getInt();
        m3 = section.getInt();
        m4 = section.getInt();
    }
}
