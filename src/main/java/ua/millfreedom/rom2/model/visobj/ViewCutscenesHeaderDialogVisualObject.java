package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;

import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.palette.Palettes;

import java.util.List;

import static ua.millfreedom.rom2.text.DialogsText.VIEW_CUTSCENES_153;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.TextTableId.DIALOGS;

/**
 * Native class: ViewCutscenesHeaderDialogVisualObject (vtbl @0x005CD008).
 * Purpose: view-cutscenes header dialog with a transient cutscene title list.
 */
public class ViewCutscenesHeaderDialogVisualObject extends HeaderDialogVisualObject {
    public static final int NATIVE_SIZE = 0x78; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!
    private static final int CUTSCENE_LIST_ID = 2;
    private static final int CUTSCENE_SCROLLBAR_ID = 0x29B;

    // Native global DAT_00622820, read by createDialogContent and updated by the main window after dialog close.
    static int selectedCutsceneIndex;

    /**
     * Native: ViewCutscenesHeaderDialogVisualObject::ViewCutscenesHeaderDialogVisualObject @0044D428.
     * Fully ported.
     */
    public ViewCutscenesHeaderDialogVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            List<String> cutsceneTitles
    ) {
        super(id, xLeft, yTop, xRight, yBottom, cutsceneTitles, null, 1, get(DIALOGS, VIEW_CUTSCENES_153));
    }

    /**
     * vtbl +0x88: ViewCutscenesHeaderDialogVisualObject::createDialogContent @0044D475.
     * Fully ported.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected CVisualObject createDialogContent(Object payload, CRect contentRect) {
        contentRect.right -= 0x18;
        TextListVisualObject cutsceneList = new TextListVisualObject(
                CUTSCENE_LIST_ID,
                new CRect(contentRect),
                Globals.fonts.font1,
                Palettes.grayDim,
                Palettes.gray,
                CUTSCENE_SCROLLBAR_ID,
                null
        );
        cutsceneList.setValue(payload);
        cutsceneList.assignSelectedRow(selectedCutsceneIndex);
        addChild(cutsceneList);

        addChild(new PostSetupVisualObject(
                CUTSCENE_SCROLLBAR_ID,
                contentRect.right,
                contentRect.top,
                contentRect.right + 0x18,
                contentRect.bottom,
                null
        ));
        return cutsceneList;
    }

    /**
     * Native support for DAT_00622820 written by CMainWindow::onDialogClosed @004891D8.
     */
    public static void setSelectedCutsceneIndex(int selectedCutsceneIndex) {
        ViewCutscenesHeaderDialogVisualObject.selectedCutsceneIndex = selectedCutsceneIndex;
    }
}
