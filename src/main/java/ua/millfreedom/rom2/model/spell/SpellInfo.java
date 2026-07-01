package ua.millfreedom.rom2.model.spell;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.model.TableLine;
import ua.millfreedom.rom2.model.column.SpellColumn;
import ua.millfreedom.rom2.CStaticDataMgr;

import java.io.IOException;
import java.util.Map;

import static ua.millfreedom.rom2.model.spell.Spheres.*;

public class SpellInfo extends TableLine {
    //0x1c
    public String effect = "";

    /**
     * Native: SpellInfo::New @00539AA0. Fully ported.
     */
    public SpellInfo() {
    }

    /**
     * Native vtbl +0x18: SpellInfo::ParseToken @004FF43D.
     * Fully ported.
     */
    @Override
    protected void ParseToken(String token, int mode, int[] pIntOut, int intOutIndex, double[] pDoubleOut, int doubleOutIndex) {
        String value = token;
        switch (mode) {
            case 3 -> {
                if (value.isEmpty()) {
                    pIntOut[intOutIndex] = 0;
                    return;
                }
                pIntOut[intOutIndex] = switch (value.charAt(0)) {
                    case 'F' -> FIRE;
                    case 'W' -> WATER;
                    case 'A' -> AIR;
                    case 'G' -> EARTH;
                    case 'I' -> ASTRAL;
                    default -> 0;
                };
            }
            case 5 -> pIntOut[intOutIndex] = "Unit".equalsIgnoreCase(value) ? 1 : 2;
            case 6 -> pIntOut[intOutIndex] = "Spell Effect".equalsIgnoreCase(value) ? 2 : 1;
            case 9 -> {
                if ("Point".equals(value)) {
                    pIntOut[intOutIndex] = 1;
                    return;
                }
                if (value.indexOf("Round") > 0) {
                    pIntOut[intOutIndex] = 3;
                    return;
                }
                if (value.indexOf("Long") > 0) {
                    pIntOut[intOutIndex] = 4;
                    return;
                }
                if (value.indexOf("Phase") > 0) {
                    pIntOut[intOutIndex] = 5;
                    return;
                }
                pIntOut[intOutIndex] = 2;
            }
            case 0x0E -> pIntOut[intOutIndex] = "Hang On Unit".equalsIgnoreCase(value) ? 2 : 1;
            default -> parseTokenBase(token, mode, pIntOut, intOutIndex, pDoubleOut, doubleOutIndex);
        }
    }

    /**
     * Native: SpellInfo::Init @0053E760.
     * Fully ported.
     */
    public void init(String text) {
        // SpellsCount @005F81BC.
        TokenizeLine(text, 0x16);
        effect = text.substring(text.lastIndexOf('\t') + 1);
    }

    /**
     * Native: SpellInfo::Serialize @004FF6E9.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        super.serialize(ar);
        if (ar.isStoring()) {
            ar.writeCString(effect);
        } else {
            effect = ar.readCString();
        }
    }

    // not ported.
    public Map<String, Number> getValuesMap() {
        return mapValues(CStaticDataMgr.Tables.spells);
    }

    // not ported.
    public int getAttribute(SpellColumn column) {
        return getValue(column.index);
    }

    @Override
    // not ported.
    public String toString() {
        return "SpellInfo{" +
                "name = " + name +
                ", effect='" + effect + "'" +
                ", " + getValuesMap() +
                '}';
    }
}
