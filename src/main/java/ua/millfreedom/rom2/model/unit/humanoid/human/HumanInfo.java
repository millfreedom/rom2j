package ua.millfreedom.rom2.model.unit.humanoid.human;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.model.TableLine;
import ua.millfreedom.rom2.model.column.HumanColumn;
import ua.millfreedom.rom2.CStaticDataMgr;
import ua.millfreedom.rom2.res.ResInHeap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static ua.millfreedom.rom2.Utils.join;

public class HumanInfo extends TableLine {
    //0x1c
    public String[] equipment = new String[10];

    /**
     * Native: HumanInfo::New @00539940. Fully ported.
     */
    public HumanInfo() {
        Arrays.fill(equipment, "");
    }

    /**
     * Native vtbl +0x18: HumanInfo::ParseToken @004FEC73.
     * Fully ported.
     */
    @Override
    protected void ParseToken(String token, int mode, int[] pIntOut, int intOutIndex, double[] pDoubleOut, int doubleOutIndex) {
        String value = token;
        HumanColumn col = HumanColumn.from(mode - 1);
        if (col == HumanColumn.KNOWN_SPELLS) {
            setUppercaseTokenBitMask(pIntOut, intOutIndex, value);
        } else {
            super.ParseToken(token, mode, pIntOut, intOutIndex, pDoubleOut, doubleOutIndex);
        }

    }

    /**
     * Native: HumanInfo::Init @0053E600.
     * Fully ported.
     */
    public void init(String text) {
        // INT_005F81B0.
        TokenizeLine(text, 0x1A);
        parseTrailingTextList(text, equipment);
    }

    /**
     * Native: HumanInfo::initFromResourceSection @004FEE52.
     * Fully ported.
     */
    public void initFromResourceSection(ResInHeap res, String sectionName) {
        name = sectionName;
        int resourceColumnCount = CStaticDataMgr.Tables.humans.size();
        resizeResourceValues(Math.max(resourceColumnCount, 1));

        String parentName = res.getString(sectionName, "Parent", "nothing");
        if (!"nothing".equals(parentName)) {
            values.set(0, CStaticDataMgr.getInstance().findHumanInfoIndexByName(parentName));
        }

        for (int index = 1; index < resourceColumnCount; index++) {
            String value = res.getString(sectionName, CStaticDataMgr.Tables.humans.get(index), "nothing");
            if (!"nothing".equals(value)) {
                int[] parsedValue = new int[1];
                ParseToken(value, index, parsedValue, 0, null, 0);
                values.set(index, parsedValue[0]);
            }
        }

        Arrays.fill(equipment, "");
        equipment[0] = getOptionalEquipmentName(res, sectionName, "Weapon");
        equipment[1] = getOptionalEquipmentName(res, sectionName, "Shield");
        List<String> armor = new ArrayList<>();
        res.getStringArray(sectionName, "Armor", armor);
        for (int index = 0; index < armor.size() && index + 2 < equipment.length; index++) {
            equipment[index + 2] = armor.get(index);
        }
    }

    /**
     * Native support extracted from HumanInfo::initFromResourceSection @004FEE52 value array setup.
     */
    private void resizeResourceValues(int size) {
        values.clear();
        for (int index = 0; index < size; index++) {
            values.add(0);
        }
    }

    /**
     * Native support extracted from HumanInfo::initFromResourceSection @004FEE52 Weapon/Shield fields.
     */
    private static String getOptionalEquipmentName(ResInHeap res, String sectionName, String keyName) {
        String value = res.getString(sectionName, keyName, "nothing");
        return "nothing".equals(value) ? "" : value;
    }

    /**
     * Native vtbl +0x08: HumanInfo::Serialize @004FF159.
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
        return mapValues(CStaticDataMgr.Tables.humans);
    }

    // not ported.
    public int getAttribute(HumanColumn column) {
        return getValue(column.index);
    }

    @Override
    // not ported.
    public String toString() {
        return "HumanInfo{" +
                "name = " + name +
                ", equipment: {" + join(",", equipment) + "}" +
                ", " + getValuesMap() +
                '}';
    }
}
