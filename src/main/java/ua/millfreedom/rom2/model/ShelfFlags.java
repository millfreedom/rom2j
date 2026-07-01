package ua.millfreedom.rom2.model;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;

/**
 * Bitmask helpers for the native Shop::ShelfFlags enum at /classes/Shop/ShelfFlags.
 */
public final class ShelfFlags {
    /**
     * not ported. Utility holder for native ShelfFlags bitmask conversions.
     */
    private ShelfFlags() {
    }

    /**
     * not ported. Expands a native ShelfFlags bitmask into the matching Java enum set.
     */
    public static EnumSet<ShelfFlagValues> toSet(int value) {
        EnumSet<ShelfFlagValues> flags = EnumSet.noneOf(ShelfFlagValues.class);
        for (ShelfFlagValues flag : ShelfFlagValues.values()) {
            if ((value & flag.value) != 0) {
                flags.add(flag);
            }
        }
        return flags;
    }

    /**
     * not ported. Packs a Java collection of ShelfFlags values into the native bitmask representation.
     */
    public static int toValue(Collection<ShelfFlagValues> flags) {
        Objects.requireNonNull(flags, "flags");
        int value = 0;
        for (ShelfFlagValues flag : flags) {
            value |= Objects.requireNonNull(flag, "flag").value;
        }
        return value;
    }

    /**
     * not ported. Packs Java ShelfFlags values into the native bitmask representation.
     */
    public static int toValue(ShelfFlagValues... flags) {
        Objects.requireNonNull(flags, "flags");
        int value = 0;
        for (ShelfFlagValues flag : flags) {
            value |= Objects.requireNonNull(flag, "flag").value;
        }
        return value;
    }
}
