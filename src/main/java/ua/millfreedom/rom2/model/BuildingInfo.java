package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.model.column.BuildingColumn;
import ua.millfreedom.rom2.CStaticDataMgr;

import java.io.IOException;
import java.util.Map;

public class BuildingInfo extends TableLine {
    /**
     * Native: BuildingInfo::New @00539A30. Fully ported.
     */
    public BuildingInfo() {
    }

    /**
     * Native vtbl +0x08: BuildingInfo::Serialize @004FF2E4.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        super.serialize(ar);
    }

    /**
     * Native vtbl +0x18: BuildingInfo::ParseToken @004FF1E8.
     * Fully ported.
     */
    @Override
    protected void ParseToken(String token, int mode, int[] pIntOut, int intOutIndex, double[] pDoubleOut, int doubleOutIndex) {
        BuildingColumn col = BuildingColumn.from(mode - 1);
        switch (col) {
            case PASSABILITY, BUILDING_PRESENT -> {
                String value = token;
                int mask = 0;
                int limit = Math.min(value.length(), 0x20);
                for (int i = 0; i < limit; i++) {
                    if (value.charAt(i) == '1') {
                        mask |= 1 << (i & 0x1F);
                    }
                }
                pIntOut[intOutIndex] = mask;
            }
            default -> super.ParseToken(token, mode, pIntOut, intOutIndex, pDoubleOut, doubleOutIndex);
        }
    }

    /**
     * Native: BuildingInfo::Init @0053E6C0.
     * Fully ported.
     */
    public void init(String text) {
        // INT_005F81B8.
        TokenizeLine(text, 0x6);
    }

    // not ported.
    public Map<String, Number> getValuesMap() {
        return mapValues(CStaticDataMgr.Tables.buildings);
    }

    // not ported.
    public int getAttribute(BuildingColumn column) {
        return getValue(column.index);
    }

    @Override
    // not ported.
    public String toString() {
        return "BuildingInfo{" +
                "name = " + name +
                ", " + getValuesMap() +
                '}';
    }
}
