package ua.millfreedom.rom2.model.world.scenario;

import java.nio.ByteBuffer;

public final class EffectOrTrapMod {
    //0x00
    public int type;
    //0x02
    public int value;

    /**
     * not ported.
     */
    public EffectOrTrapMod() {
    }

    /**
     * Native support extracted from EffectDTO::copyConstructor @00536F10 EffectOrTrapMod value copy.
     * Fully ported support helper.
     */
    public EffectOrTrapMod(EffectOrTrapMod source) {
        type = source.type;
        value = source.value;
    }

    /**
     * Native support extracted from ScenarioDescriptor::ScenarioDescriptor @00534AD4 effect-modifier materialization.
     * Fully ported.
     */
    public static EffectOrTrapMod read(ByteBuffer section) {
        EffectOrTrapMod modifier = new EffectOrTrapMod();
        modifier.type = Short.toUnsignedInt(section.getShort());
        modifier.value = section.getInt();
        return modifier;
    }
}
