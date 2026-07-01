package ua.millfreedom.rom2.model;

/**
 * Native type: LlDriverConnectionOption.
 * Purpose: one CLlDriver modem/connection option row returned by CLlDriver::GetConnectionOptions.
 */
public class LlDriverConnectionOption {
    public static final int NATIVE_SIZE = 0x214; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    //0x0
    public String label = "";
    //0x100
    public String addressText = "";
    //0x200
    public final byte[] directPlayAddressData = new byte[0x14];

    /**
     * Java utility constructor.
     * not ported.
     */
    public LlDriverConnectionOption() {
    }

    /**
     * Java utility constructor.
     * not ported.
     */
    public LlDriverConnectionOption(String label) {
        this.label = label;
    }
}
