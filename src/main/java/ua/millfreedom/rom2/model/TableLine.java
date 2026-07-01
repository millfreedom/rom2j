package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.container.CustomList;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.LinkedHashMap.newLinkedHashMap;

public class TableLine implements MfcSerializable {
    //0x4
    public String name = "";
    //0x8
    public CustomList<Integer> values = CustomList.std(Integer.class); //CArray

    /**
     * Native: TableLine::New @005392F0. Fully ported.
     */
    public TableLine() {
    }

    /**
     * Native: TableLine::Serialize @004FE3AB.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        if (ar.isStoring()) {
            ar.writeCString(name);
        } else {
            name = ar.readCString();
        }
        ar.serialize(values);
    }

    @Override
    // not ported.
    public String toString() {
        return "'" + name + '\'' +
                " : " + values;
    }

    // not ported.
    public List<Integer> getValues() {
        return Collections.unmodifiableList(values);
    }

    // not ported.
    public int getValue(int index) {
        if (index < 0 || index >= values.size()) {
            throw new IndexOutOfBoundsException("Value index out of range: " + index);
        }
        return values.get(index);
    }

    // not ported.
    protected static void setUppercaseTokenBitMask(int[] pIntOut, int intOutIndex, String value) {
        pIntOut[intOutIndex] = 0;
        if (value.length() <= 0x13) {
            return;
        }

        String compact = value.trim();
        int spaceIndex = compact.indexOf(' ');
        while (spaceIndex != -1) {
            compact = compact.substring(0, spaceIndex) + compact.substring(spaceIndex + 1);
            spaceIndex = compact.indexOf(' ');
        }

        int mask = 0;
        int limit = Math.min(compact.length(), 0x1F);
        for (int i = 1; i <= limit; i++) {
            char ch = compact.charAt(i - 1);
            if (ch >= 'A' && ch <= 'Z') {
                mask |= 1 << (i & 0x1F);
            }
        }
        pIntOut[intOutIndex] = mask;
    }

    /**
     * Native support extracted from FUN_004FE96B, used by UnitInfo::Init @0053E540 and HumanInfo::Init @0053E600.
     */
    protected static void parseTrailingTextList(String text, String[] dest) {
        for (int i = 0; i < dest.length; i++) {
            dest[i] = "";
        }

        String remaining = text.substring(text.lastIndexOf('\t') + 1) + ",";
        remaining = remaining.trim();
        StringBuilder noQuotes = new StringBuilder(remaining.length());
        for (int i = 0; i < remaining.length(); i++) {
            char ch = remaining.charAt(i);
            if (ch != '"') {
                noQuotes.append(ch);
            }
        }
        remaining = noQuotes.toString();

        int index = 0;
        int bracePos = remaining.indexOf('{');
        int commaPos = remaining.indexOf(',');
        while ((bracePos != -1 || commaPos != -1) && index < dest.length) {
            if (commaPos < bracePos || bracePos == -1) {
                dest[index++] = remaining.substring(0, commaPos);
                remaining = remaining.substring(commaPos + 1);
            } else {
                int closeBrace = remaining.indexOf('}');
                dest[index++] = remaining.substring(0, closeBrace + 1);
                remaining = remaining.substring(closeBrace + 2);
            }
            remaining = remaining.stripLeading();
            bracePos = remaining.indexOf('{');
            commaPos = remaining.indexOf(',');
        }
    }

    /**
     * Native support overload for TableLine::TokenizeLine @004FD935.
     */
    public void TokenizeLine(String line, int numFields) {
        TokenizeLine(line, numFields, null);
    }

    /**
     * Native vtbl +0x14: TableLine::TokenizeLine @004FD935.
     * Fully ported.
     */
    public void TokenizeLine(String line, int numFields, double[] parsedDoubles) {
        String source = line;
        StringBuilder noQuotes = new StringBuilder(source.length() + 1);
        for (int i = 0; i < source.length(); i++) {
            char ch = source.charAt(i);
            if (ch != '"') {
                noQuotes.append(ch);
            }
        }
        noQuotes.append('\t');
        source = noQuotes.toString();

        int nameEnd = source.indexOf('\t');
        if (nameEnd < 0) {
            name = source;
            return;
        }
        name = source.substring(0, nameEnd);
        source = source.substring(nameEnd + 1);

        values.clear();
        for (int i = 0; i < numFields; i++) {
            values.add(0);
        }

        for (int tokenIndex = 0; tokenIndex < numFields; tokenIndex++) {
            int tokenEnd = source.indexOf('\t');
            if (tokenEnd < 0) {
                return;
            }

            String token = source.substring(0, tokenEnd);
            int nativeMode = tokenIndex + 1;
            if (parsedDoubles == null) {
                if (tokenEnd < 1) {
                    values.set(tokenIndex, -1);
                } else {
                    int[] intOut = new int[1];
                    ParseToken(token, nativeMode, intOut, 0, null, 0);
                    values.set(tokenIndex, intOut[0]);
                }
            } else {
                ParseToken(token, nativeMode, null, 0, parsedDoubles, tokenIndex);
            }

            source = source.substring(tokenEnd + 1);
        }
    }

    /**
     * Native vtbl +0x18: TableLine::ParseToken @004FDB9A.
     * Fully ported.
     */
    protected void ParseToken(String token, int mode, int[] pIntOut, int intOutIndex, double[] pDoubleOut, int doubleOutIndex) {
        parseTokenBase(token, mode, pIntOut, intOutIndex, pDoubleOut, doubleOutIndex);
    }

    /**
     * Native support extracted from TableLine::ParseToken @004FDB9A.
     */
    protected final void parseTokenBase(String token, int mode, int[] pIntOut, int intOutIndex, double[] pDoubleOut, int doubleOutIndex) {
        String value = token;

        if (mode == 0) {
            name = value;
            return;
        }

        if (pDoubleOut == null) {
            if (value.isEmpty()) {
                pIntOut[intOutIndex] = -1;
            } else {
                pIntOut[intOutIndex] = Utils.atoiLike(value);
            }
            return;
        }

        pDoubleOut[doubleOutIndex] = Utils.atofLike(value);
    }

    // not ported.
    protected Map<String, Number> mapValues(CustomList<String> names) {
        Map<String, Number> result = newLinkedHashMap(values.size());
        if (names != null && names.size() > values.size()) {
            for (int i = 0; i < values.size(); i++) {
                result.put(names.get(i + 1), values.get(i));
            }
        }
        return result;
    }

}
