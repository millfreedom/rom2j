package ua.millfreedom.rom2.model.visobj;

/**
 * Native support payload for EnterYourNameHeaderDialogVisualObject context buffer.
 */
public class EnterYourNameDialogContext {
    public static final int NATIVE_SIZE = 0x104;

    //0x0
    public String name = "";
    //0x100
    public int sexSelection;

    /**
     * Java convenience constructor.
     * not ported.
     */
    public EnterYourNameDialogContext() {
    }

    /**
     * Java convenience constructor for the native enter-name dialog payload shape.
     * not ported.
     */
    public EnterYourNameDialogContext(String name, int sexSelection) {
        this.name = name;
        this.sexSelection = sexSelection;
    }
}
