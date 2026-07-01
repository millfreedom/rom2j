package ua.millfreedom.rom2.model.world.scenario;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public final class EffectDTO {
    //0x00
    public int itemID;
    //0x04
    public WorldSack pWorldSack;
    //0x08
    public int x;
    //0x0C
    public int y;
    //0x10
    public int effectMode;
    //0x12
    public int min;
    //0x14
    public int spread;
    //0x16
    public int spellId;
    //0x18
    public int spellStrength;
    //0x1A
    public final List<EffectOrTrapMod> carr = new ArrayList<>();

    /**
     * Native: EffectDTO::EffectDTO @00536EA3. Fully ported.
     */
    public EffectDTO() {
    }

    /**
     * Native: EffectDTO::copyConstructor @00536F10. Fully ported.
     */
    public EffectDTO(EffectDTO source) {
        itemID = source.itemID;
        pWorldSack = source.pWorldSack;
        x = source.x;
        y = source.y;
        effectMode = source.effectMode;
        min = source.min;
        spread = source.spread;
        spellId = source.spellId;
        spellStrength = source.spellStrength;
        for (EffectOrTrapMod modifier : source.carr) {
            carr.add(new EffectOrTrapMod(modifier));
        }
    }

    /**
     * Native support extracted from ScenarioDescriptor::ScenarioDescriptor @00534AD4 effect-record materialization.
     * Fully ported.
     */
    public static EffectDTO read(ByteBuffer section, int version) {
        EffectDTO effect = new EffectDTO();
        if (version > 0x3dd) {
            effect.itemID = section.getInt();
        }
        effect.x = section.getInt();
        effect.y = section.getInt();
        effect.effectMode = Short.toUnsignedInt(section.getShort());
        effect.min = Short.toUnsignedInt(section.getShort());
        effect.spread = Short.toUnsignedInt(section.getShort());
        int typeAndStrength = section.getInt();
        effect.spellId = typeAndStrength & 0xffff;
        effect.spellStrength = (typeAndStrength >>> 16) & 0xffff;
        int modifierCount = section.getInt();
        for (int modifierIndex = 0; modifierIndex < modifierCount; modifierIndex++) {
            effect.carr.add(EffectOrTrapMod.read(section));
        }
        return effect;
    }
}
