package ua.millfreedom.rom2.model.world.scenario;

import ua.millfreedom.rom2.CString;

import java.nio.ByteBuffer;

/**
 * Native Trigger scenario record serialized by Trigger::SerializeElements @0054A730.
 */
public final class Trigger {
    public static final int SERIALIZED_SIZE = 0xB8;

    //0x00
    public final CString description = new CString(0x80);
    //0x80
    public final int[] checkIds = new int[6];
    //0x98
    public final int[] instantIds = new int[4];
    //0xA8
    public int check12Operator;
    //0xAC
    public int check34Operator;
    //0xB0
    public int check56Operator;
    //0xB4
    public int runOnce;

    /**
     * Native support extracted from Trigger::SerializeElements @0054A730 loading branch and
     * ScenarioDescriptor::ScenarioDescriptor @00534AD4 trigger section.
     * Fully ported.
     */
    public static Trigger read(ByteBuffer section) {
        Trigger trigger = new Trigger();
        trigger.description.read(section);
        for (int i = 0; i < trigger.checkIds.length; i++) {
            trigger.checkIds[i] = section.getInt();
        }
        for (int i = 0; i < trigger.instantIds.length; i++) {
            trigger.instantIds[i] = section.getInt();
        }
        trigger.check12Operator = section.getInt();
        trigger.check34Operator = section.getInt();
        trigger.check56Operator = section.getInt();
        trigger.runOnce = section.getInt();
        return trigger;
    }

    /**
     * Native support extracted from Trigger::SerializeElements @0054A730 storing branch.
     * Fully ported.
     */
    public void write(ByteBuffer section) {
        description.write(section);
        for (int checkId : checkIds) {
            section.putInt(checkId);
        }
        for (int instantId : instantIds) {
            section.putInt(instantId);
        }
        section.putInt(check12Operator);
        section.putInt(check34Operator);
        section.putInt(check56Operator);
        section.putInt(runOnce);
    }
}
