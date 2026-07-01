package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.palette.Palettes;

import static ua.millfreedom.rom2.model.enums.MessageCodes.ROLE_DIALOG_ADVANCE_PART;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_OK_77;

/**
 * Native support for role-key dialog creation.
 */
public final class RoleDialogSupport {
    private static final String NOTHING_TO_SAY = "Nothing to say";

    /**
     * Java utility constructor.
     * not ported.
     */
    private RoleDialogSupport() {
    }

    /**
     * Native: global ShowRoleKeyDialog @0041D15E.
     * Fully ported. Java maps native object allocations to constructors, preserves the `hasNpcDirective` layout
     * branch, adds the native children in order, and hands the dialog to CMainWindow::ShowDialog. Dialog part parsing,
     * portrait binding, speech, and tune playback continue inside RoleKeyDialogVisualObject.
     */
    public static void showRoleKeyDialog(String scriptName) {
        RoleKeyDialogVisualObject roleDialog = new RoleKeyDialogVisualObject(
                9,
                0x1E,
                0x78,
                0x262,
                0x168,
                scriptName
        );
        if (roleDialog.hasNpcDirective == 0) {
            roleDialog.addChild(new WrappedTextSourceListVisualObject(
                    10,
                    0x30,
                    0x24,
                    0x1AC,
                    0xAC,
                    NOTHING_TO_SAY,
                    Globals.fonts.font1,
                    Palettes.gray,
                    0
            ));
        } else {
            roleDialog.addChild(new LinkedPaletteVisualObject(
                    0x0C,
                    0x1E,
                    0x36,
                    0x76,
                    0xA8,
                    null,
                    2
            ));
            roleDialog.addChild(new WrappedTextSourceListVisualObject(
                    10,
                    0x80,
                    0x24,
                    0x1AC,
                    0xAC,
                    NOTHING_TO_SAY,
                    Globals.fonts.font1,
                    Palettes.gray,
                    0
            ));
        }
        roleDialog.addChild(new CommandButtonVisualObject(
                0x0B,
                200,
                0xAC,
                0x118,
                0xC6,
                get(MAIN_OK_77),
                Globals.fonts.font1,
                Palettes.grayDim,
                ROLE_DIALOG_ADVANCE_PART,
                0,
                null
        ));
        Globals.mainWindow.showDialog(roleDialog);
    }
}
