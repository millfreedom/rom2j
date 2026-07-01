package ua.millfreedom.rom2.model.window;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;

/**
 * Native support enum for the distinct bits stored in CMainWindow::dialogsMask @+0x404.
 */
public enum DialogsMaskFlag {
    GAMEPLAY(0x0001),
    SHOP_DIALOG(0x0002),
    INN_DIALOG(0x0004),
    MODAL_DIALOG(0x0008),
    GLOBAL_MAP(0x0010),
    UNK1(0x0020),
    CREDITS(0x0040),
    MAIN_MENU(0x0080),
    STARTUP_LOGO(0x0100),
    CHARACTER_GENERATOR(0x0200),
    CHARACTER_LOADER(0x0400),
    START_GAME_SETUP(0x0800),
    FAME_HALL(0x1000),
    CUTSCENE_PLAYBACK(0x2000),
    FAME_HALL_DOCUMENT(0x4000),
    HEADER_DIALOG_VARIANT(0x8000);

    public final int mask;

    /**
     * Native support for CMainWindow::dialogsMask bit constants at CMainWindow +0x404.
     */
    DialogsMaskFlag(int mask) {
        this.mask = mask;
    }

    /**
     * not ported. Expands any CMainWindow::dialogsMask integer into known Java flag values.
     */
    public static EnumSet<DialogsMaskFlag> fromMask(int value) {
        EnumSet<DialogsMaskFlag> flags = EnumSet.noneOf(DialogsMaskFlag.class);
        for (DialogsMaskFlag flag : values()) {
            if (flag.isSetIn(value)) {
                flags.add(flag);
            }
        }
        return flags;
    }

    /**
     * not ported. Packs Java dialogsMask flag values into the native integer representation.
     */
    public static int toMask(Collection<DialogsMaskFlag> flags) {
        Objects.requireNonNull(flags, "flags");
        int value = 0;
        for (DialogsMaskFlag flag : flags) {
            value |= Objects.requireNonNull(flag, "flag").mask;
        }
        return value;
    }

    /**
     * not ported. Packs Java dialogsMask flag values into the native integer representation.
     */
    public static int maskOf(DialogsMaskFlag... flags) {
        Objects.requireNonNull(flags, "flags");
        int value = 0;
        for (DialogsMaskFlag flag : flags) {
            value |= Objects.requireNonNull(flag, "flag").mask;
        }
        return value;
    }

    /**
     * not ported. Returns true when every requested dialogsMask flag is present in a native integer value.
     */
    public static boolean contains(int value, DialogsMaskFlag... flags) {
        Objects.requireNonNull(flags, "flags");
        for (DialogsMaskFlag flag : flags) {
            if (!Objects.requireNonNull(flag, "flag").isSetIn(value)) {
                return false;
            }
        }
        return true;
    }

    /**
     * not ported. Returns true when any requested dialogsMask flag is present in a native integer value.
     */
    public static boolean containsAny(int value, DialogsMaskFlag... flags) {
        Objects.requireNonNull(flags, "flags");
        for (DialogsMaskFlag flag : flags) {
            if (Objects.requireNonNull(flag, "flag").isSetIn(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * not ported. Returns true when none of the requested dialogsMask flags are present in a native integer value.
     */
    public static boolean doesNotContain(int value, DialogsMaskFlag... flags) {
        return !containsAny(value, flags);
    }

    /**
     * not ported. Returns true when the native integer value contains exactly the requested dialogsMask flags.
     */
    public static boolean isExactly(int value, DialogsMaskFlag... flags) {
        return value == maskOf(flags);
    }

    /**
     * not ported. Returns true when this dialogsMask flag is present in a native integer value.
     */
    public boolean isSetIn(int value) {
        return (value & mask) != 0;
    }

    /**
     * not ported. Returns true when this dialogsMask flag is absent from a native integer value.
     */
    public boolean isUnsetIn(int value) {
        return (value & mask) == 0;
    }

    /**
     * not ported. Returns a native dialogsMask integer with this flag included.
     */
    public int includeTo(int value) {
        return value | mask;
    }

    /**
     * not ported. Returns a native dialogsMask integer with this flag excluded.
     */
    public int excludeIn(int value) {
        return value & ~mask;
    }
}
