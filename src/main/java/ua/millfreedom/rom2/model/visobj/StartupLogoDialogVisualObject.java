package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBmp256;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.res.Resources;

import static ua.millfreedom.rom2.model.enums.MessageCodes.DIALOG_OK;
import static ua.millfreedom.rom2.model.enums.MessageCodes.STARTUP_LOGO_STEP_COMPLETE;
import static ua.millfreedom.rom2.model.window.windowproc.handlers.CMainWindowWindowProcSupport.readMessageInt;
import static ua.millfreedom.rom2.res.Constants.GRAPHICS;

/**
 * Native class: StartupLogoDialogVisualObject.
 * Purpose: startup logo splash dialog with timeout and click-to-advance behavior.
 */
public class StartupLogoDialogVisualObject extends HandlerVisualObject {
    public static final int NATIVE_SIZE = 0x7C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final String[] STARTUP_LOGO_BITMAP_PATHS_BY_STEP = {
            Resources.path(GRAPHICS, "logo", "monolith.bmp"),
            Resources.path(GRAPHICS, "logo", "buka.bmp"),
            Resources.path(GRAPHICS, "logo", "nival.bmp"),
            Resources.path(GRAPHICS, "logo", "allods.bmp"),
            Resources.path(GRAPHICS, "logo", "millfreedom.bmp")
    };

    //0x68
    public CBmp256 startupLogoBitmap;
    //0x6c
    public int startupStep;
    //0x70
    public int timeoutStartTick;
    //0x74
    public int timeoutTicks;
    //0x78
    public int drawPendingFlag;

    /**
     * Native: StartupLogoDialogVisualObject::StartupLogoDialogVisualObject @004C9CB0.
     * Fully ported.
     */
    public StartupLogoDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        initialize();
    }

    /**
     * vtbl +0x78: StartupLogoDialogVisualObject::Initialize @004C9D9E.
     * Fully ported.
     */
    @Override
    public void initialize() {
        timeoutTicks = Integer.MAX_VALUE;
        startupLogoBitmap = null;
        CVisualObject closeButton = new CommandButtonVisualObject(4, 0, 0, 0, 0, "", Globals.fonts.font1, Palettes.grayDim, DIALOG_OK, 0, null);
        addChild(closeButton);
    }

    /**
     * vtbl +0x2C: StartupLogoDialogVisualObject::Update @004CA0B1.
     * Fully ported.
     */
    @Override
    public void update() {
        drawPendingFlag = 0;
        Globals.renderer.lockSurface();
        try {
            if (startupLogoBitmap != null) {
                startupLogoBitmap.drawRectMasked(cRect.left, cRect.top);
            }
        } finally {
            Globals.renderer.unlockSurface();
        }
    }

    /**
     * vtbl +0x30: StartupLogoDialogVisualObject::RenderSelf @004CA0FE.
     * Fully ported.
     */
    @Override
    public void renderSelf(CRect clipRect) {
        // Native no-op.
    }

    /**
     * vtbl +0x48: StartupLogoDialogVisualObject::OnMessage @004C9F0E.
     * Fully ported. The native step-to-logo table at 0x005F6540 is translated to packaged Java bitmap equivalents.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        switch (msg) {
            case RENDER_FRAME -> {
                if (drawPendingFlag != 0) {
                    draw();
                }
                if (Integer.compareUnsigned(currentTick() - timeoutStartTick, timeoutTicks) > 0) {
                    postStartupLogoStepComplete();
                    timeoutTicks = Integer.MAX_VALUE;
                }
                return 1;
            }
            case NOTIFY_MAP_CONTEXT_CHANGED -> {
                drawPendingFlag = 1;
                return 1;
            }
            case STARTUP_LOGO_SET_TIMEOUT -> {
                timeoutTicks = readMessageInt(wParam);
                timeoutStartTick = currentTick();
                return 1;
            }
            case STARTUP_LOGO_SET_STEP -> {
                startupStep = readMessageInt(wParam);
                startupLogoBitmap = loadStartupLogoBitmapForStep(startupStep);
                startupLogoBitmap.initPalette(1, 1, 0);
                return 1;
            }
            default -> {
                return super.onMessage(msg, wParam, lParam);
            }
        }
    }

    /**
     * vtbl +0x54: StartupLogoDialogVisualObject::OnLButtonDown @004C9EA2.
     * Fully ported.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        postStartupLogoStepComplete();
        timeoutTicks = Integer.MAX_VALUE;
        return 1;
    }

    /**
     * vtbl +0x6C: StartupLogoDialogVisualObject::OnKeyDown @004C9ED8.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        postStartupLogoStepComplete();
        timeoutTicks = Integer.MAX_VALUE;
        return 1;
    }

    /**
     * vtbl +0x84: StartupLogoDialogVisualObject::HideDialog @004C9E46.
     * Fully ported.
     */
    @Override
    public HandlerVisualObject hideDialog(MessageCodes reason) {
        startupLogoBitmap = null;
        return super.hideDialog(reason);
    }

    /**
     * Native support boundary: repeated `CWnd::PostMessage(mainWnd, STARTUP_LOGO_STEP_COMPLETE, startupStep, 0)` calls in
     * StartupLogoDialogVisualObject::OnMessage @004C9F0E, StartupLogoDialogVisualObject::OnLButtonDown @004C9EA2, and
     * StartupLogoDialogVisualObject::OnKeyDown @004C9ED8.
     * System-boundary bridge. Java deliberately routes the recovered message tuple through `Globals.mainWindow.postMessage(...)`.
     */
    private void postStartupLogoStepComplete() {
        Globals.mainWindow.postMessage(STARTUP_LOGO_STEP_COMPLETE, startupStep, 0);
    }

    /**
     * Java helper for the startup logo resource table used by StartupLogoDialogVisualObject::OnMessage @004C9F0E.
     * not ported.
     */
    private static CBmp256 loadStartupLogoBitmapForStep(int startupStep) {
        return new CBmp256(STARTUP_LOGO_BITMAP_PATHS_BY_STEP[startupStep]);
    }

    /**
     * Java helper for `timeGetTime` call sites in StartupLogoDialogVisualObject own methods.
     * not ported.
     */
    private static int currentTick() {
        return (int) System.currentTimeMillis();
    }
}
