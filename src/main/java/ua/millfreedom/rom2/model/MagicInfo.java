package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.model.column.MagicColumn;
import ua.millfreedom.rom2.CStaticDataMgr;

import java.util.Map;

public class MagicInfo extends TableLine {
    /**
     * Native: MagicInfo::New @005397E0. Fully ported.
     */
    public MagicInfo() {
    }

    /**
     * Native: MagicInfo::Init @0053E4A0.
     * Fully ported.
     */
    public void init(String text) {
        // INT_005F81B4.
        TokenizeLine(text, 0x1C);
    }

    // not ported.
    public Map<String, Number> getValuesMap() {
        return mapValues(CStaticDataMgr.Tables.magic);
    }

    // not ported.
    public int getAttribute(MagicColumn column) {
        return getValue(column.index);
    }

    @Override
    // not ported.
    public String toString() {
        return "MagicInfo{" +
                "name = " + name +
                ", " + getValuesMap() +
                '}';
    }
}
