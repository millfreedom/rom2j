package ua.millfreedom.rom2.model.render;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.visobj.CVisualObject;
import ua.millfreedom.rom2.model.visobj.HandlerVisualObject;

/**
 * Java presentation-shell support for mapping the native software surface into the platform framebuffer.
 * not ported.
 */
public final class PresentationSupport {
    /**
     * Java utility constructor.
     * not ported.
     */
    private PresentationSupport() {
    }

    /**
     * Resolves the active presentation transform from the native visual tree and platform target size.
     * not ported.
     */
    public static PresentationTransform currentTransform(int availableWidth, int availableHeight) {
        PresentationMode mode = currentMode();
        CRect sourceRect = mode == PresentationMode.MAIN_WINDOW_FIT ? Globals.mainWindowRect : Globals.screenRect;
        return PresentationTransform.create(
                mode,
                sourceRect,
                availableWidth,
                availableHeight
        );
    }

    /**
     * Returns the active visible source rectangle used by Java presentation.
     * not ported.
     */
    public static CRect currentSourceRect() {
        return currentMode() == PresentationMode.MAIN_WINDOW_FIT ? Globals.mainWindowRect : Globals.screenRect;
    }

    /**
     * Derives Java presentation mode from active native top-level visual objects.
     * not ported.
     */
    private static PresentationMode currentMode() {
        if (Globals.mainWindow == null || Globals.mainWindow.getInputController() == null) {
            return PresentationMode.FULL_SCREEN_NATIVE;
        }

        PresentationMode visualMode = presentationModeForActiveTopLevelDialogs(Globals.mainWindow.getInputController());
        return visualMode == null ? PresentationMode.FULL_SCREEN_NATIVE : visualMode;
    }

    /**
     * Scans top-most active dialogs first, allowing small modal dialogs to inherit the larger context below them.
     * not ported.
     */
    private static PresentationMode presentationModeForActiveTopLevelDialogs(CVisualObject inputController) {
        for (int i = inputController.children.size() - 1; i >= 0; i--) {
            CVisualObject child = inputController.children.get(i);
            if (!(child instanceof HandlerVisualObject handler) || handler.activeFlag == 0) {
                continue;
            }
            if (child.getRect().equals(Globals.screenRect) && !child.getRect().equals(Globals.mainWindowRect)) {
                return PresentationMode.FULL_SCREEN_NATIVE;
            }
            if (child.getRect().equals(Globals.mainWindowRect)) {
                return PresentationMode.MAIN_WINDOW_FIT;
            }
        }
        return null;
    }
}
