package ua.millfreedom.rom2;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Utils {
    private static final Pattern ATOF_PREFIX = Pattern.compile("^\\s*([+-]?(?:\\d+\\.\\d*|\\d+|\\.\\d+)(?:[eE][+-]?\\d+)?)");
    private static final double oneInclusive = Math.nextAfter(1, Double.MAX_VALUE);


    // not ported.
    public static String readText(ByteBuffer bb, Charset cs) {
        return cs.decode(bb).toString();
    }

    // not ported.
    public static List<String> readAllLines(ByteBuffer bb, Charset cs) {
        return readText(bb, cs).lines().toList();
    }

    // not ported.
    public static String firstChars(String s, int x) {
        if (s == null) return null;
        if (x <= 0) return "";
        return s.substring(0, Math.min(x, s.length()));
    }

    // not ported.
    public static String join(String delimiter, Iterable<?> iterable) {
        StringBuilder sb = new StringBuilder();
        iterable.forEach(e -> sb.append(e).append(delimiter));
        return sb.toString();
    }

    // not ported.
    public static String join(Iterable<?> iterable, Object prefix, Object suffix) {
        StringBuilder sb = new StringBuilder();
        for (Object e : iterable) {
            sb.append(prefix).append(e).append(suffix);
        }
        return sb.toString();
    }

    // not ported.
    public static String join(String delimiter, Object[] arr) {
        StringBuilder sb = new StringBuilder();
        if (arr == null) {
            sb.append("NULL");
        } else {
            for (Object o : arr) {
                sb.append(o).append(delimiter);
            }
        }
        return sb.toString();
    }

    /**
     * Native: FormatDecimalThousands @00475263.
     * Fully ported. Native copies the current CString, empties the destination, then prepends comma-prefixed
     * 3-character suffix groups until only the leading group remains; a leading sign stops the loop at 4 chars.
     */
    public static String formatDecimalThousands(String text) {
        String prefix = text;
        StringBuilder suffix = new StringBuilder();
        int remainingLength = prefix.length();
        while (remainingLength > 3
                && (remainingLength > 4
                || (prefix.charAt(0) != '-' && prefix.charAt(0) != '+'))) {
            suffix.insert(0, "," + prefix.substring(remainingLength - 3));
            prefix = prefix.substring(0, remainingLength - 3);
            remainingLength = prefix.length();
        }
        return prefix + suffix;
    }

    /**
     * Native support for FormatDecimalThousands @00475263 call sites that first format integer values as decimal text.
     */
    public static String formatDecimalThousands(int value) {
        return formatDecimalThousands(Integer.toString(value));
    }

    /**
     * Native: GetCurDirectory @00474AB9.
     * Fully ported. Native copies at most maxSize bytes from g_CurrentDirectory and returns the copied length.
     */
    public static String getCurDirectory(int maxSize) {
        String currentDirectory = Globals.currentDirectory.toString();
        int copiedLength = Math.min(maxSize, currentDirectory.length());
        return currentDirectory.substring(0, copiedLength);
    }

    /**
     * Native support for GetCurDirectory @00474AB9 call sites that need the full Java path object.
     */
    public static Path getCurDirectory() {
        return Globals.currentDirectory;
    }

    /**
     * Native: CustomToLower @00474CF9.
     * Fully ported. Native applies custom-encoding byte remaps before the CRT tolower ASCII fast path.
     */
    public static int customToLower(int c) {
        int value = c & 0xFF;
        if (Globals.useCustomEncoding) {
            if (value >= 0x80 && value < 0x90) {
                return value + 0x20;
            }
            if (value >= 0x90 && value < 0xA0) {
                return value + 0x50;
            }
        }
        return asciiToLower(value);
    }

    /**
     * Native support extracted from CRT tolower @00584890 no-locale branch called by CustomToLower @00474CF9.
     */
    private static int asciiToLower(int c) {
        if (c > 0x40 && c < 0x5B) {
            return c + 0x20;
        }
        return c;
    }

    /**
     * Native: RandExclusive @004A0FD0. Fully ported.
     *
     * @param minInclusive
     * @param maxExclusive
     * @return
     */
    public static int randExclusive(int minInclusive, int maxExclusive) {
        if (minInclusive >= maxExclusive) {
            log.error("rand({},{}) method is called! Possibly a bug!", minInclusive, maxExclusive);
            return maxExclusive;
        }
        return ThreadLocalRandom.current().nextInt(minInclusive, maxExclusive);
    }

    /**
     * Native: RandExclusive @004A0FD0.
     * Fully ported.
     */
    public static int randExclusive(int maxExclusive) {
        return randExclusive(0, maxExclusive);
    }

    /**
     * Native: Rand @0051FA25.
     * Fully ported.
     */
    public static int randInclusive(int nInclusive) {
        return randExclusive(nInclusive + 1);
    }

    /**
     * Native support extracted from inclusive Rand @0051FA25.
     */
    public static int randInclusive(int minInclusive, int maxInclusive) {
        return randExclusive(minInclusive, maxInclusive + 1);
    }

    /**
     * Native: randomOneToN @0051FA4E.
     * Fully ported.
     */
    public static int randomOneToN(int nInclusive) {
        if (nInclusive == 0) {
            return 1;
        }
        return randInclusive(1, nInclusive);
    }

    /**
     * Native: randomFloat0to1Inclusive @0051FA0C.
     * Fully ported.
     */
    public static double randomFloat0to1Inclusive() {
        return ThreadLocalRandom.current().nextDouble(0, oneInclusive);
    }

    /**
     * Native support extracted from RandExclusive @004A0FD0 plus constant-base additions.
     * Same as: randExclusive(0, rangeExclusive) + base
     *
     */
    public static int randBased(int base, int rangeExclusive) {
        return randExclusive(rangeExclusive) + base;
    }

    /**
     * Native support extracted from RandExclusive @004A0FD0 percent-roll call sites.
     */
    public static int randPercent0To99() {
        return randExclusive(0, 100);
    }

    // not ported.
    public static int atoiLike(String text) {
        int length = text.length();
        int index = 0;

        while (index < length && Character.isWhitespace(text.charAt(index))) {
            index++;
        }

        int sign = 1;
        if (index < length) {
            char signChar = text.charAt(index);
            if (signChar == '+' || signChar == '-') {
                sign = signChar == '-' ? -1 : 1;
                index++;
            }
        }

        long value = 0;
        boolean hasDigit = false;
        while (index < length) {
            char ch = text.charAt(index);
            if (ch < '0' || ch > '9') {
                break;
            }
            hasDigit = true;
            value = value * 10 + (ch - '0');
            if (value > (long) Integer.MAX_VALUE + 1L) {
                value = (long) Integer.MAX_VALUE + 1L;
                break;
            }
            index++;
        }

        if (!hasDigit) {
            return 0;
        }

        long signed = sign > 0 ? value : -value;
        if (signed > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (signed < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) signed;
    }

    // not ported.
    public static double atofLike(String text) {
        Matcher matcher = ATOF_PREFIX.matcher(text);
        if (!matcher.find()) {
            return 0.0;
        }
        String numericPrefix = matcher.group(1);
        if (numericPrefix == null || numericPrefix.isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(numericPrefix);
        } catch (NumberFormatException ignored) {
            return 0.0;
        }
    }

    /**
     * Normalize pointer-like map keys read from archive tokens.
     * not ported.
     */
    public static Object normalizePointerMapKey(Object keyToken) {
        if (keyToken instanceof Number n) {
            return n.intValue();
        }
        return keyToken;
    }

    /**
     * Encode object reference into int token for MFC-like pointer serialization.
     * not ported.
     */
    public static int encodePointerLike(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        return System.identityHashCode(value);
    }

    public static class Counter {
        private int val = 0;

        // not ported.
        public static Counter from(int start) {
            return new Counter(start);
        }

        // not ported.
        public Counter(int val) {
            this.val = val;
        }

        // not ported.
        public Counter() {
        }

        @Override
        // not ported.
        public String toString() {
            return String.valueOf(val++);
        }
    }

    /**
     * Java helper for unpacking low/high word coordinates.
     * not ported.
     */
    public static Point point(int hiLowEncoded) {
        return new Point(hiLowEncoded & 0xFFFF, (hiLowEncoded >>> 16) & 0xFFFF);
    }
}
