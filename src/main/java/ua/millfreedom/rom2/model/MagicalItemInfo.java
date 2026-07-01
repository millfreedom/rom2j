package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.model.column.MagicItemColumn;
import ua.millfreedom.rom2.CStaticDataMgr;

import java.io.IOException;
import java.util.Map;

public class MagicalItemInfo extends WorldItem {
    //0x40
    public String effect = "";

    /**
     * Native: MagicalItemInfo::New @00539690. Fully ported.
     */
    public MagicalItemInfo() {
    }

    /**
     * Native vtbl +0x18: MagicalItemInfo::ParseToken @005396F0.
     * Fully ported.
     */
    @Override
    protected void ParseToken(String token, int mode, int[] pIntOut, int intOutIndex, double[] pDoubleOut, int doubleOutIndex) {
        parseTokenBase(token, mode, pIntOut, intOutIndex, pDoubleOut, doubleOutIndex);
    }

    /**
     * Native: MagicalItemInfo::Init @004FE4B2.
     * Fully ported.
     */
    public void init(String text) {
        // INT_005F81A8.
        TokenizeLine(text, 2);
        effect = text.substring(text.lastIndexOf('\t') + 1);
    }

    /**
     * Native vtbl +0x08: MagicalItemInfo::Serialize @004FE55D.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        TableLineSerialize(ar);
        if (ar.isStoring()) {
            ar.writeByte(materialMasks[0]);
            ar.writeCString(effect);
        } else {
            materialMasks[0] = (short) (ar.readByte() & 0xFF);
            effect = ar.readCString();
        }
    }

    @Override
    // not ported.
    public Map<String, Number> getValuesMap() {
        return mapValues(CStaticDataMgr.Tables.magicItems);
    }

    // not ported.
    public int getAttribute(MagicItemColumn column) {
        return getValue(column.index);
    }

    @Override
    // not ported.
    public String toString() {
        return "MagicalItemInfo{" +
                "name = " + name +
                ", effect='" + effect + "'" +
                ", " + getValuesMap() +
                '}';
    }
}
