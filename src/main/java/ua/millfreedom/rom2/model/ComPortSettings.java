package ua.millfreedom.rom2.model;

/**
 * Native class: ComPortSettings.
 * Purpose: 0x14-byte serial COM-port settings payload used by SerialSettingsDialogVisualObject.
 */
public class ComPortSettings {
    public static final int NATIVE_SIZE = 0x14; // VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    //0x00
    public int comPortNumber;
    //0x04
    public int baudRate;
    //0x08
    public int stopBitsSelection;
    //0x0c
    public int paritySelection;
    //0x10
    public int flowControlSelection;

    /**
     * Native: ComPortSettings::New @0043C692.
     * Fully ported.
     */
    public ComPortSettings() {
        comPortNumber = 1;
        baudRate = 0x3840;
        stopBitsSelection = 0;
        paritySelection = 0;
        flowControlSelection = 4;
    }
}
