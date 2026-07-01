package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.model.column.WorldItemColumn;
import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.CStaticDataMgr;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class WorldItem extends TableLine {
    //0x1c
    public short[] materialMasks = new short[7];

    //0x2c
    public CustomList<String> strings = new CustomList<>(String.class);

    /**
     * Native: WorldItem::New @00539270. Fully ported.
     */
    public WorldItem() {
    }

    /**
     * Native vtbl +0x18: WorldItem::ParseToken @004FDC48.
     * Fully ported.
     */
    @Override
    protected void ParseToken(String token, int mode, int[] pIntOut, int intOutIndex, double[] pDoubleOut, int doubleOutIndex) {
        String value = token;
        WorldItemColumn col = WorldItemColumn.from(mode - 1);
        switch (col) {
            case SHAPE, MATERIAL -> pIntOut[intOutIndex] = 0;
            case ATTACK_TYPE -> pIntOut[intOutIndex] = switch (value) {
                case "blade" -> 1;
                case "axe" -> 2;
                case "crush" -> 3;
                case "pike" -> 4;
                case "shoot" -> 5;
                case "fire" -> 11;
                case "water" -> 12;
                case "air" -> 13;
                case "earth" -> 14;
                case "astral" -> 15;
                default -> 0;
            };
            default -> super.ParseToken(token, mode, pIntOut, intOutIndex, pDoubleOut, doubleOutIndex);
        }

    }

    /**
     * Native: WorldItem::Init @0053E330.
     * Fully ported.
     */
    public void init(String text) {
        // WorldItemColumnCount @005F81A4.
        TokenizeLine(text, 0x11);
    }

    /**
     * Native support extracted from MagicalItemInfo::Serialize @004FE55D TableLine prefix.
     */
    void TableLineSerialize(CArchive ar) throws IOException {
        super.serialize(ar);
    }

    /**
     * Native support extracted from WorldItem::Serialize @004FE3F9 material-mask block.
     */
    void serializeAttributes(CArchive ar) throws IOException {
        serializeAttributes(ar, materialMasks.length);
    }

    /**
     * Native support extracted from WorldItem::Serialize @004FE3F9 and MagicalItemInfo::Serialize @004FE55D.
     */
    void serializeAttributes(CArchive ar, int count) throws IOException {
        if (!ar.isStoring()) {
            for (int i = 0; i < count; i++) {
                materialMasks[i] = ar.readShort();
            }
        } else {
            for (int i = 0; i < count; i++) {
                ar.writeShort(materialMasks[i]);
            }
        }
    }

    /**
     * Native: WorldItem::Serialize @004FE3F9.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        super.serialize(ar);
        serializeAttributes(ar);
        ar.serialize(strings);
    }

    // not ported.
    public Map<String, Number> getValuesMap() {
        return mapValues(CStaticDataMgr.Tables.worldItems);
    }

    // not ported.
    public int getAttribute(WorldItemColumn column) {
        return getValue(column.index);
    }

    @Override
    // not ported.
    public String toString() {
        return "WorldItem{" +
                "name = " + name +
                ", materialMasks=" + Arrays.toString(materialMasks) +
                ", strings=" + strings +
                ", " + getValuesMap() +
                '}';
    }
}
