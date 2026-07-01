package ua.millfreedom.rom2.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Native class: PhoneBook.
 * Purpose: modem dial payload with one extra string, the current dial number, and the stored phone-number list.
 */
public class PhoneBook {
    public static final int NATIVE_SIZE = 0x1C; // VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    /**
     * Native: PhoneBook::PhoneBook @00492CD0.
     * Java port status: fully ported.
     */
    public PhoneBook() {
    }

    //0x00
    public String entryName = "";
    //0x04
    public String dialNumber = "";
    //0x08
    public final List<String> numbers = new ArrayList<>();
}
