package ua.millfreedom.rom2.model.unit;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.model.TableLine;
import ua.millfreedom.rom2.model.column.UnitColumn;
import ua.millfreedom.rom2.model.spell.SpellInfo;
import ua.millfreedom.rom2.CStaticDataMgr;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static ua.millfreedom.rom2.Utils.join;

public class UnitInfo extends TableLine {

    //0x1c
    public String[] equipment = new String[2];

    /**
     * Native: UnitInfo::New @00539850. Fully ported.
     */
    public UnitInfo() {
        Arrays.fill(equipment, "");
    }

    /**
     * Native vtbl +0x18: UnitInfo::ParseToken @004FE5C6.
     * Fully ported.
     */
    @Override
    protected void ParseToken(String token, int mode, int[] pIntOut, int intOutIndex, double[] pDoubleOut, int doubleOutIndex) {
        String value = token;
        UnitColumn col = UnitColumn.from(mode - 1);
        switch (col) {
            case ATTACK_KIND -> {
                switch (value) {
                    case "" -> pIntOut[intOutIndex] = 0;
                    case "Poison" -> pIntOut[intOutIndex] = 1;
                    case "Cold" -> pIntOut[intOutIndex] = 2;
                    case "AP" -> pIntOut[intOutIndex] = 3;
                }
            }
            case SPELL_1, SPELL_2, SPELL_3 -> {
                pIntOut[intOutIndex] = 0;
                if (value.isEmpty()) {
                    return;
                }
                var spells = CStaticDataMgr.getInstance().spells;
                for (int i = spells.size() - 1; i >= 1; i--) {
                    SpellInfo spell = spells.get(i);
                    if (value.equals(spell.name)) {
                        pIntOut[intOutIndex] = i;
                        return;
                    }
                }
            }
            case KNOWN_SPELLS -> setUppercaseTokenBitMask(pIntOut, intOutIndex, value);
            default -> super.ParseToken(token, mode, pIntOut, intOutIndex, pDoubleOut, doubleOutIndex);
        }
    }

    /**
     * Native: UnitInfo::Init @0053E540.
     * Fully ported.
     */
    public void init(String text) {
        // INT_005F81AC.
        TokenizeLine(text, 0x3E);
        parseTrailingTextList(text, equipment);
    }

    /**
     * Native: UnitInfo::Serialize @004FE8DC.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        super.serialize(ar);
        if (ar.isStoring()) {
            for (String value : equipment) {
                ar.writeCString(value);
            }
        } else {
            for (int i = 0; i < equipment.length; i++) {
                equipment[i] = ar.readCString();
            }
        }
    }

    // not ported.
    public Map<String, Number> getValuesMap() {
        return mapValues(CStaticDataMgr.Tables.units);
    }

    // not ported.
    public int getAttribute(UnitColumn column) {
        return getValue(column.index);
    }

    @Override
    // not ported.
    public String toString() {
        return "UnitInfo{" +
                "name = " + name +
                ",equipment: {" + join(",", equipment) + "}" +
                ", " + getValuesMap() +
                '}';
    }
}
