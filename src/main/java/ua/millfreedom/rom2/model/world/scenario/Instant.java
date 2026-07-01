package ua.millfreedom.rom2.model.world.scenario;

import ua.millfreedom.rom2.CString;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Native Instant scenario record constructed by Instant::Instant @00536AB0 and read by
 * ScenarioDescriptor::ScenarioDescriptor @00534AD4.
 */
public final class Instant {
    private static final int ARGUMENT_COUNT = 10;

    //0x00
    public final CString name = new CString(0x40);
    //0x40
    public int typeId;
    //0x44
    public int index;
    //0x318
    public int executeOnce;
    //0x48 values, 0x70 types, 0x98 names
    public final InstArgument[] arguments = new InstArgument[ARGUMENT_COUNT];

    /**
     * Native: Instant::Instant @00536AB0.
     * Fully ported.
     */
    public Instant() {
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = new InstArgument();
        }
    }

    /**
     * Native: Instant::Instant @00536B24.
     * Fully ported.
     */
    public Instant(String name, int typeId, int[] argumentValues, String[] argumentNames, int executeOnce) {
        this();
        this.name.set(name.getBytes(StandardCharsets.ISO_8859_1));
        this.typeId = typeId;
        for (int i = 0; i < arguments.length; i++) {
            arguments[i].value = argumentValues[i];
            arguments[i].name.set(argumentNames[i].getBytes(StandardCharsets.ISO_8859_1));
        }
        this.executeOnce = executeOnce;
    }

    /**
     * Native: Instant::copyConstructor @00536BAD.
     * Fully ported.
     */
    public Instant(Instant source) {
        this();
        name.set(source.name.toString().getBytes(StandardCharsets.ISO_8859_1));
        typeId = source.typeId;
        index = source.index;
        executeOnce = source.executeOnce;
        for (int i = 0; i < arguments.length; i++) {
            arguments[i].value = source.arguments[i].value;
            arguments[i].type = source.arguments[i].type;
            arguments[i].name.set(source.arguments[i].name.toString().getBytes(StandardCharsets.ISO_8859_1));
        }
    }

    /**
     * Native support extracted from ScenarioDescriptor::ScenarioDescriptor @00534AD4 instances/checks section.
     * Fully ported.
     */
    public static Instant read(ByteBuffer section) {
        Instant instant = new Instant();
        instant.name.read(section);
        instant.typeId = section.getInt();
        instant.index = section.getInt();
        instant.executeOnce = section.getInt();
        for (int i = 0; i < instant.arguments.length; i++) {
            instant.arguments[i].value = section.getInt();
        }
        for (int i = 0; i < instant.arguments.length; i++) {
            instant.arguments[i].type = section.getInt();
        }
        for (int i = 0; i < instant.arguments.length; i++) {
            instant.arguments[i].name.read(section);
        }
        return instant;
    }
}
