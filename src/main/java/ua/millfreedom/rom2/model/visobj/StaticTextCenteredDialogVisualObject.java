package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.palette.Palettes;

/**
 * Native class: StaticTextCenteredDialogVisualObject.
 * Purpose: centered dialog variant that only injects a static text header child.
 */
public class StaticTextCenteredDialogVisualObject extends CenteredDialogVisualObject {
    public static final int NATIVE_SIZE = 0x68; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    /**
     * Native: StaticTextCenteredDialogVisualObject::StaticTextCenteredDialogVisualObject @0043C420.
     * Fully ported.
     */
    public StaticTextCenteredDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, Object polyHandler) {
        super(id, xLeft, yTop, xRight, yBottom, polyHandler);
        initialize();
    }

    /**
     * vtbl +0x78: StaticTextCenteredDialogVisualObject::Initialize @0043C48E.
     * Fully ported.
     */
    @Override
    public void initialize() {
        addChild(new DialogWindowVisualObject(
                2,
                0x32,
                0x14,
                0xC8,
                0x2D,
                "Static text",
                Globals.fonts.font1,
                Palettes.grayDim,
                2
        ));
    }
}
