package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.color.RGB16;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.render.PresentationSupport;
import ua.millfreedom.rom2.model.visobj.CVisualObject;
import ua.millfreedom.rom2.model.visobj.HandlerVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;

import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.MODAL_DIALOG;

public class CMousePointer extends CGameBitmap {
    private static final int FIRST_TOOLTIP_DELAY_MS = 500;
    private static final int TOOLTIP_AUTO_HIDE_MS = 0x639C;
    private static final int HELD_BUTTON_INITIAL_DELAY_MS = 0x96;
    private static final int HELD_BUTTON_REPEAT_DELAY_MS = 0x42;
    private static final int TOOLTIP_LINE_HEIGHT = 0x0E;
    private static final int TOOLTIP_LEFT_PADDING = 5;
    private static final int TOOLTIP_TOP_PADDING = 4;
    private static final int TOOLTIP_RIGHT_PADDING = 0x0B;
    private static final int TOOLTIP_PANEL_GAP = 2;
    private static final short TOOLTIP_BACKGROUND_COLOR = RGB16.from(0x24, 0x2C, 0x27).val();
    private static final short TOOLTIP_BORDER_LIGHT_COLOR = RGB16.from(0xA0, 0x78, 0x32).val();
    private static final short TOOLTIP_BORDER_DARK_COLOR = RGB16.from(0x50, 0x3C, 0x18).val();
    private static final int SELECTION_LINE_COUNT = 8;
    private static final int SELECTION_LINE_LONG_PIXELS = 0x640;
    private static final int SELECTION_LINE_THICKNESS = 2;
    private static final short SELECTION_BOX_COLOR = RGB16.WHITE.val();

    public static final CCursor Cursor_Default = new CCursor("graphics/cursors/default/sprites.16a", 4, 4, 2_000_000_000);
    public static final CCursor Cursor_Move = new CCursor("graphics/cursors/move/sprites.16a", 2, 3, 100);
    public static final CCursor Cursor_Swarm = new CCursor("graphics/cursors/swarm/sprites.16a", 2, 3, 100);
    public static final CCursor Cursor_Attack = new CCursor("graphics/cursors/attack/sprites.16a", 3, 3, 100);
    public static final CCursor Cursor_Defend = new CCursor("graphics/cursors/defend/sprites.16a", 0x10, 0x10, 100);
    public static final CCursor Cursor_Select = new CCursor("graphics/cursors/select/sprites.16a", 3, 3, 100);
    public static final CCursor Cursor_Patrol = new CCursor("graphics/cursors/patrol/sprites.16a", 8, 0x19, 100);
    public static final CCursor Cursor_Cast = new CCursor("graphics/cursors/cast/sprites.16a", 0x10, 0x10, 100);
    public static final CCursor Cursor_Pickup = new CCursor("graphics/cursors/pickup/sprites.16a", 0x10, 0xc, 0x42);
    public static final CCursor Cursor_ArrowN = new CCursor("graphics/cursors/arrow0/sprites.16a", 0x10, 6, 2_000_000_000);
    public static final CCursor Cursor_ArrowS = new CCursor("graphics/cursors/arrow4/sprites.16a", 0x10, 0x1c, 2_000_000_000);
    public static final CCursor Cursor_ArrowW = new CCursor("graphics/cursors/arrow6/sprites.16a", 5, 0x10, 2_000_000_000);
    public static final CCursor Cursor_ArrowE = new CCursor("graphics/cursors/arrow2/sprites.16a", 0x1a, 0x10, 2_000_000_000);
    public static final CCursor Cursor_ArrowNW = new CCursor("graphics/cursors/arrow7/sprites.16a", 8, 9, 2_000_000_000);
    public static final CCursor Cursor_ArrowSW = new CCursor("graphics/cursors/arrow5/sprites.16a", 8, 0x18, 2_000_000_000);
    public static final CCursor Cursor_ArrowNE = new CCursor("graphics/cursors/arrow1/sprites.16a", 0x17, 9, 2_000_000_000);
    public static final CCursor Cursor_ArrowSE = new CCursor("graphics/cursors/arrow3/sprites.16a", 0x17, 0x18, 2_000_000_000);
    public static final CCursor Cursor_SmallDefault = new CCursor("graphics/cursors/sdefault/sprites.16a", 1, 1, 2_000_000_000);
    public static final CCursor Cursor_SmallMove = new CCursor("graphics/cursors/smove.256", 0, 0, 2_000_000_000);
    public static final CCursor Cursor_SmallAttack = new CCursor("graphics/cursors/sattack.256", 0, 0, 2_000_000_000);
    public static final CCursor Cursor_SmallDefend = new CCursor("graphics/cursors/sdefend.256", 0, 0, 2_000_000_000);
    public static final CCursor Cursor_SmallPatrol = new CCursor("graphics/cursors/spatrol.256", 0, 0, 2_000_000_000);
    public static final CCursor Cursor_SmallCast = new CCursor("graphics/cursors/scast.256", 0, 0, 2_000_000_000);
    public static final CCursor Cursor_CantPut = new CCursor("graphics/cursors/cantput/sprites.16a", 0x26, 0x24, 2_000_000_000);
    public static final CCursor Cursor_Town = new CCursor("graphics/cursors/town/sprites.16a", 0x10, 0x10, 2_000_000_000);
    public static final CCursor Cursor_Dice = new CCursor("graphics/cursors/dice/sprites.16a", 0x10, 0x10, 100);
    public static final CCursor Cursor_Wait = new CCursor("graphics/cursors/wait/sprites.16a", 0x10, 0x10, 100);
    public static final CCursor Cursor_Backpack = new CCursor("graphics/cursors/backpack/sprites.16a", 0x10, 0x10, 100);

    /**
     * Native: CMousePointer::InitCursors @0047B92D.
     * Fully ported at the Java static cursor registry boundary.
     */
    public static void initCursors() {
        // The native global cursor objects are represented by the static fields above.
    }

    //0x4
    public CSprite256 sourceBitmap;

    //0x8
    public CBmp64k backBuffer;

    //0xc
    public CBmp64k cursorBuffer;

    //0x10
    public int currentX;

    //0x14
    public int currentY;

    //0x18
    public int hotSpotX;

    //0x1c
    public int hotSpotY;

    //0x20
    public int showCount;

    //0x24
    public int currentFrame;

    //0x28
    public int frameCount;

    //0x2c
    public int animSpeed;

    //0x30
    public int lastAnimTime;

    //0x34
    public int inputTimerStart;

    //0x38
    public int inputTimerDuration;

    //0x3c
    public String tooltipText = "";

    //0x40
    public int tooltipLineCount;

    //0x44
    public int tooltipWidth;

    //0x48
    public final CRect tooltipRect = new CRect();

    //0x58
    public long totalMoveTime;

    //0x5c
    public long lastMoveTime;

    //0x60
    public int tooltipVisibleFlag;

    //0x6c
    public final CRect selectionRect = new CRect();

    //0x7c
    public final CBmp64k[] selectionLines = new CBmp64k[SELECTION_LINE_COUNT];

    //0x9c
    public int selecting;

    // Runtime state for cursor visibility in this Java port.
    public int cursorVisible;

    //0xa0
    public int backgroundCaptureEnabled;

    // Last normalized selection rectangle drawn by drawSelectionBox().
    public final CRect lastDrawnSelectionRect = new CRect();

    /**
     * Native: CMousePointer::CreateObject @00425BC9.
     * Fully ported for Java's final-overlay tooltip model; native tooltip background buffers at 0x64/0x68 are
     * intentionally omitted.
     */
    public CMousePointer() {
        initializeNativeStorage();
    }

    /**
     * Native: CMousePointer::CMousePointer @00425DF4.
     * Fully ported for Java's platform-cursor and final-overlay tooltip model.
     */
    public CMousePointer(CSprite256 bitmap, int x, int y, int frameCount, int animSpeed) {
        initializeNativeStorage();
        init(bitmap, x, y, frameCount, animSpeed);
    }

    /**
     * Native support extracted from CMousePointer::CreateObject @00425BC9 and CMousePointer::CMousePointer @00425DF4.
     * Fully ported for Java's final-overlay tooltip model; native tooltip background buffers at 0x64/0x68 are
     * intentionally omitted.
     */
    private void initializeNativeStorage() {
        sourceBitmap = null;
        backBuffer = null;
        cursorBuffer = null;
        currentX = 0;
        currentY = 0;
        hotSpotX = 0;
        hotSpotY = 0;
        showCount = 0;
        currentFrame = 0;
        frameCount = 0;
        animSpeed = 0;
        lastAnimTime = 0;
        inputTimerStart = 0;
        inputTimerDuration = 0;
        tooltipText = "";
        tooltipLineCount = 0;
        tooltipWidth = 0;
        tooltipRect.set(0, 0, 0, 0);
        totalMoveTime = 0;
        lastMoveTime = 0;
        tooltipVisibleFlag = 0;
        selectionRect.set(0, 0, 0, 0);
        for (int i = 0; i < selectionLines.length; i++) {
            if ((i & 2) == 0) {
                selectionLines[i] = new CBmp64k(SELECTION_LINE_LONG_PIXELS, SELECTION_LINE_THICKNESS);
            } else {
                selectionLines[i] = new CBmp64k(SELECTION_LINE_THICKNESS, SELECTION_LINE_LONG_PIXELS);
            }
        }
        selecting = 0;
        backgroundCaptureEnabled = 0;
    }

    /**
     * Native: CMousePointer::IsSelecting @0041E730.
     * Fully ported.
     */
    public boolean isSelecting() {
        return selecting != 0;
    }

    /**
     * Native: CMousePointer::GetX @0041E6D0.
     * Fully ported.
     */
    public int getX() {
        return currentX;
    }

    /**
     * Native: CMousePointer::GetY @0041E6F0.
     * Fully ported.
     */
    public int getY() {
        return currentY;
    }

    /**
     * Native support extracted from CMousePointer::OnMouseMove @00426594 and cursor-position writes before message fan-out.
     */
    public void setPosition(int x, int y) {
        if (currentX == x && currentY == y) {
            if (lastMoveTime == 0) {
                lastMoveTime = System.currentTimeMillis();
                totalMoveTime = 0;
            }
            return;
        }
        onMouseMove(x, y);
    }

    /**
     * Native: CMousePointer::OnMouseMove @00426594.
     * Fully ported at the Java platform-cursor and final-overlay selection boundary.
     */
    public void onMouseMove(int x, int y) {
        totalMoveTime = 0;
        lastMoveTime = System.currentTimeMillis();
        if (tooltipVisible()) {
            restoreDragArea();
        }
        tooltipVisibleFlag = 0;
        currentX = x;
        currentY = y;
        selectionRect.set(selectionRect.left, selectionRect.top, currentX, currentY);
    }

    /**
     * Native: CMousePointer::GetSelectionRect @0041E750.
     * Fully ported.
     */
    public CRect getSelectionRect() {
        return selectionRect;
    }

    /**
     * Native: CMousePointer::GetSourceBitmap @0041E6B0.
     * Fully ported.
     */
    public CGameBitmap getSourceBitmap() {
        return sourceBitmap;
    }

    /**
     * Native: CMousePointer::Init @00426183.
     * Fully ported at the Java platform-cursor hook boundary.
     */
    public void init(CSprite256 bitmap, int x, int y, int frameCount, int animSpeed) {
        if (Globals.isWindowed != 0) {
            return;
        }

        hide();
        sourceBitmap = bitmap;
        hotSpotX = x;
        hotSpotY = y;
        currentFrame = 0;
        lastAnimTime = 0;
        inputTimerStart = 0;
        this.animSpeed = animSpeed;
        this.frameCount = frameCount;
        allocateCursorBuffers(bitmap);
        postInit();
        show();
    }

    /**
     * Native support extracted from CMousePointer::Init @00426183 cursor back-buffer and draw-buffer allocation.
     * Fully ported.
     */
    private void allocateCursorBuffers(CSprite256 bitmap) {
        int width = bitmap.xSizeOf(0);
        int height = bitmap.ySizeOf(0);
        backBuffer = new CBmp64k(width, height);
        cursorBuffer = new CBmp64k(width, height);
    }

    //not ported
    public void postInit() {

    }

    /**
     * Native support extracted from CMainWindow::OnSetCursor @00484A09.
     * Java base cursor has no platform OS cursor to update.
     */
    public int applyMainWindowSetCursor() {
        return 1;
    }

    /**
     * Native: CMousePointer::Hide @00426342.
     * Fully ported.
     */
    public void hide() {
        if (Globals.isWindowed != 0) {
            return;
        }

        showCount -= 1;
        if (showCount == 0 && sourceBitmap != null) {
            erase();
        }
    }

    /**
     * Native: CMousePointer::Show @00426381.
     * Fully ported.
     */
    public void show() {
        if (Globals.isWindowed != 0) {
            return;
        }

        if (showCount == 0 && sourceBitmap != null) {
            drawCurrentCursorFrame();
        }
        showCount += 1;
    }

    /**
     * Native: CMousePointer::Update @004268C4.
     * Fully ported at the Java platform-cursor and final-overlay tooltip boundary.
     * skipped: native GetDirectDrawSurfaceLockCount @00452251 has no Java DirectDraw lock counter equivalent.
     */
    public void update() {
        if (Globals.isWindowed != 0) {
            return;
        }

        long now = System.currentTimeMillis();
        int nowTick = (int) now;
        CMainWindow mainWindow = Globals.mainWindow;
        if (sourceBitmap != null
                && frameCount > 1
                && Integer.compareUnsigned(nowTick - lastAnimTime, animSpeed) > 0
                && mainWindow.haveFocus != 0) {
            lastAnimTime = nowTick;
            currentFrame = (currentFrame + 1) % frameCount;
            onCursorFrameChanged();
        }

        maybePostHeldButtonUserMessage(nowTick);
        updateTooltip(now);
        Runnable musicUpdater = mainWindow.musicUpdater;
        if (musicUpdater != null) {
            musicUpdater.run();
        }
    }

    /**
     * Native support extracted from CMousePointer::Update @004268C4 frame-swap branch.
     */
    protected void onCursorFrameChanged() {
        hide();
        show();
    }

    /**
     * Native support extracted from CMousePointer::Update @004268C4.
     */
    private void maybePostHeldButtonUserMessage(int now) {
        if (!Globals.leftButtonPressed) {
            return;
        }
        if (Integer.compareUnsigned(now - inputTimerStart, inputTimerDuration) <= 0) {
            return;
        }

        Globals.mainWindow.postMessage(MessageCodes.WM_USER, 1, packPoint(currentX, currentY));
        inputTimerStart = now;
        inputTimerDuration = HELD_BUTTON_REPEAT_DELAY_MS;
    }

    /**
     * Native support extracted from CMousePointer::Update @004268C4.
     */
    private void updateTooltip(long now) {
        if (lastMoveTime == 0) {
            lastMoveTime = now;
            totalMoveTime = 0;
            return;
        }

        long previousMoveTime = totalMoveTime;
        totalMoveTime += now - lastMoveTime;
        lastMoveTime = now;

        CMainWindow mainWindow = Globals.mainWindow;
        if (totalMoveTime >= TOOLTIP_AUTO_HIDE_MS && tooltipVisible() && mainWindow.haveFocus != 0) {
            clearTooltip();
            return;
        }
        if (tooltipVisible()) {
            return;
        }
        if (previousMoveTime >= FIRST_TOOLTIP_DELAY_MS
                || totalMoveTime < FIRST_TOOLTIP_DELAY_MS
                || totalMoveTime >= TOOLTIP_AUTO_HIDE_MS
                || selecting != 0
                || backgroundCaptureEnabled == 0
                || mainWindow.haveFocus == 0
                || mainWindow.getInputController() == null) {
            return;
        }

        CVisualObject target = resolveTooltipTarget(mainWindow);
        if (target == null) {
            return;
        }

        String text = target.getText();
        if (text == null || text.isEmpty()) {
            return;
        }

        tooltipText = text;
        tooltipVisibleFlag = 1;
        measureTooltipText();
    }

    /**
     * Native support extracted from CMousePointer::Update @004268C4 and CMainWindow::ShowDialog @0048B33B.
     * Java models the native full-screen modal backdrop as a tooltip hit-test shield while preserving modal-child
     * tooltip lookup inside the active dialog.
     */
    private CVisualObject resolveTooltipTarget(CMainWindow mainWindow) {
        CVisualObject inputController = mainWindow.getInputController();
        if (inputController == null) {
            return null;
        }

        CVisualObject modalDialog = findTopActiveModalDialog(mainWindow, inputController);
        if (modalDialog == null) {
            return inputController.findDeepestChildAtPoint(currentX, currentY);
        }

        CRect modalScreenRect = new CRect();
        modalDialog.clientToScreen(modalScreenRect, modalDialog.getRect());
        if (!modalScreenRect.contains(currentX, currentY)) {
            return null;
        }
        return modalDialog.findDeepestChildAtPoint(currentX, currentY);
    }

    /**
     * Native support extracted from CMainWindow::ShowDialog @0048B33B and HandlerVisualObject::ShowDialog @004DC232.
     */
    private static CVisualObject findTopActiveModalDialog(CMainWindow mainWindow, CVisualObject inputController) {
        if (MODAL_DIALOG.isUnsetIn(mainWindow.dialogsMask)) {
            return null;
        }
        for (int i = inputController.children.size() - 1; i >= 0; i--) {
            CVisualObject child = inputController.children.get(i);
            if (child instanceof HandlerVisualObject handlerDialog && handlerDialog.activeFlag != 0) {
                return child;
            }
        }
        return null;
    }

    /**
     * Java final-overlay support for CMousePointer::DrawTooltip @0042787B.
     */
    public void drawTooltipOverlay() {
        if (tooltipVisible()) {
            drawTooltip();
        }
    }

    /**
     * Native support extracted from CMousePointer::MeasureTooltipText @0042677D.
     * Fully ported.
     */
    private void measureTooltipText() {
        if (TooltipText.isSideBySide(tooltipText)) {
            String[] tooltips = TooltipText.splitSideBySide(tooltipText);
            String[] leftLines = TooltipText.splitRows(tooltips[0]);
            String[] rightLines = TooltipText.splitRows(tooltips[1]);
            tooltipWidth = measureTooltipWidth(leftLines) + TOOLTIP_PANEL_GAP
                    + measureTooltipWidth(rightLines) + TOOLTIP_RIGHT_PADDING;
            tooltipLineCount = Math.max(leftLines.length, rightLines.length);
            return;
        }
        String[] lines = TooltipText.splitRows(tooltipText);
        tooltipWidth = measureTooltipWidth(lines);
        tooltipLineCount = lines.length;
    }

    /**
     * Native: CMousePointer::DrawTooltip @0042787B.
     * Fully ported at the Java final-overlay tooltip boundary.
     */
    private void drawTooltip() {
        if (TooltipText.isSideBySide(tooltipText)) {
            drawSideBySideTooltip();
            return;
        }

        drawPlainTooltip();
    }

    /**
     * Native support extracted from CMousePointer::DrawTooltip @0042787B for a single tooltip frame.
     * Fully ported at the Java final-overlay tooltip boundary.
     */
    private void drawPlainTooltip() {
        tooltipRect.set(
                currentX,
                currentY - 5 - tooltipLineCount * TOOLTIP_LINE_HEIGHT,
                currentX + TOOLTIP_RIGHT_PADDING + tooltipWidth,
                currentY
        );
        CRect presentationSourceRect = PresentationSupport.currentSourceRect();
        if (presentationSourceRect.right < tooltipRect.right) {
            moveTooltipRect(presentationSourceRect.right - tooltipRect.right, 0);
        }
        if (tooltipRect.top < presentationSourceRect.top) {
            moveTooltipRect(0, presentationSourceRect.top - tooltipRect.top);
        }

        drawTooltipFrame();
        drawTooltipLines(tooltipRect, TooltipText.splitRows(tooltipText));
    }

    /**
     * Java-only side-by-side tooltip renderer for two complete tooltip panels.
     * not ported.
     */
    private void drawSideBySideTooltip() {
        String[] tooltips = TooltipText.splitSideBySide(tooltipText);
        String[] leftLines = TooltipText.splitRows(tooltips[0]);
        String[] rightLines = TooltipText.splitRows(tooltips[1]);
        CRect leftRect = makeTooltipRect(currentX, currentY, measureTooltipWidth(leftLines), leftLines.length);
        CRect rightRect = makeTooltipRect(leftRect.right + TOOLTIP_PANEL_GAP, currentY, measureTooltipWidth(rightLines), rightLines.length);

        tooltipRect.set(
                leftRect.left,
                Math.min(leftRect.top, rightRect.top),
                rightRect.right,
                Math.max(leftRect.bottom, rightRect.bottom)
        );
        CRect presentationSourceRect = PresentationSupport.currentSourceRect();
        int dx = 0;
        int dy = 0;
        if (presentationSourceRect.right < tooltipRect.right) {
            dx = presentationSourceRect.right - tooltipRect.right;
        }
        if (tooltipRect.top < presentationSourceRect.top) {
            dy = presentationSourceRect.top - tooltipRect.top;
        }
        if (dx != 0 || dy != 0) {
            moveTooltipRect(leftRect, dx, dy);
            moveTooltipRect(rightRect, dx, dy);
            moveTooltipRect(dx, dy);
        }

        drawTooltipFrame(leftRect);
        drawTooltipFrame(rightRect);
        drawTooltipLines(leftRect, leftLines);
        drawTooltipLines(rightRect, rightLines);
    }

    /**
     * Java final-overlay support for building a tooltip panel rectangle from measured text.
     * not ported.
     */
    private static CRect makeTooltipRect(int left, int bottom, int textWidth, int lineCount) {
        return new CRect(
                left,
                bottom - 5 - lineCount * TOOLTIP_LINE_HEIGHT,
                left + TOOLTIP_RIGHT_PADDING + textWidth,
                bottom
        );
    }

    /**
     * Native support extracted from CMousePointer::MeasureTooltipText @0042677D line-width scan,
     * with Java-only tooltip color control stripping.
     */
    private static int measureTooltipWidth(String[] lines) {
        int width = 0;
        for (String line : lines) {
            width = Math.max(width, Globals.fonts.font2.getTextWidth(TooltipText.stripColorCodes(line)));
        }
        return width;
    }

    /**
     * Native support extracted from CMousePointer::DrawTooltip @0042787B tooltip text drawing loop,
     * with Java-only tooltip color control expansion.
     */
    private static void drawTooltipLines(CRect rect, String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            drawTooltipLine(
                    rect.left + TOOLTIP_LEFT_PADDING,
                    rect.top + TOOLTIP_TOP_PADDING + i * TOOLTIP_LINE_HEIGHT,
                    lines[i]
            );
        }
    }

    /**
     * Native support extracted from CMousePointer::DrawTooltip @0042787B one-line text draw,
     * with Java-only tooltip color control expansion.
     */
    private static void drawTooltipLine(int x, int y, String line) {
        int runX = x;
        for (TooltipText.ColoredRun run : TooltipText.coloredRuns(line, Palettes.sepia)) {
            Globals.fonts.font2.drawTextShadowed(
                    runX,
                    y,
                    run.text,
                    0,
                    run.palette,
                    1
            );
            runX += Globals.fonts.font2.getTextWidth(run.text);
        }
    }

    /**
     * Native support extracted from CRect::OffsetRect calls in CMousePointer::DrawTooltip @0042787B.
     */
    private void moveTooltipRect(int dx, int dy) {
        moveTooltipRect(tooltipRect, dx, dy);
    }

    /**
     * Native support extracted from CRect::OffsetRect calls in CMousePointer::DrawTooltip @0042787B.
     */
    private static void moveTooltipRect(CRect rect, int dx, int dy) {
        rect.set(
                rect.left + dx,
                rect.top + dy,
                rect.right + dx,
                rect.bottom + dy
        );
    }

    /**
     * Native: CMousePointer::DrawTooltipFrame @00427999.
     * Fully ported.
     */
    private void drawTooltipFrame() {
        drawTooltipFrame(tooltipRect);
    }

    /**
     * Native support extracted from CMousePointer::DrawTooltipFrame @00427999 for one tooltip frame rectangle.
     * Fully ported.
     */
    private static void drawTooltipFrame(CRect rect) {
        int left = rect.left + 1;
        int right = rect.right - 1;
        int top = rect.top + 1;
        int bottom = rect.bottom - 2;
        Globals.renderer.fillScreenRect(left, top, right, bottom, TOOLTIP_BACKGROUND_COLOR);
        GUI.ball.drawRectMasked(left - 1, top - 1, 0, 0, 4, 4);
        GUI.ball.drawRectMasked(right - 2, top - 1, 0, 0, 4, 4);
        GUI.ball.drawRectMasked(left - 1, bottom - 2, 0, 0, 4, 4);
        GUI.ball.drawRectMasked(right - 2, bottom - 2, 0, 0, 4, 4);
        Globals.renderer.drawLine(left + 2, top, right - 2, top, TOOLTIP_BORDER_LIGHT_COLOR);
        Globals.renderer.drawLine(left + 2, top + 1, right - 2, top + 1, TOOLTIP_BORDER_DARK_COLOR);
        Globals.renderer.drawLine(left + 2, bottom - 1, right - 2, bottom - 1, TOOLTIP_BORDER_LIGHT_COLOR);
        Globals.renderer.drawLine(left + 2, bottom, right - 2, bottom, TOOLTIP_BORDER_DARK_COLOR);
        Globals.renderer.drawLine(left, top + 2, left, bottom - 2, TOOLTIP_BORDER_LIGHT_COLOR);
        Globals.renderer.drawLine(left + 1, top + 2, left + 1, bottom - 2, TOOLTIP_BORDER_DARK_COLOR);
        Globals.renderer.drawLine(right - 1, top + 2, right - 1, bottom - 2, TOOLTIP_BORDER_LIGHT_COLOR);
        Globals.renderer.drawLine(right, top + 2, right, bottom - 2, TOOLTIP_BORDER_DARK_COLOR);
    }


    /**
     * Native support extracted from CMousePointer::Update @004268C4 and CMousePointer::EndDrag @00427EA5.
     */
    private boolean tooltipVisible() {
        return tooltipVisibleFlag != 0;
    }

    /**
     * Native support extracted from CMousePointer::RestoreDragArea @00427E45 call sites.
     * Java final-overlay tooltips are hidden by state only; the next frame redraw removes the overlay.
     */
    private void clearTooltip() {
        tooltipVisibleFlag = 0;
    }

    /**
     * Native support extracted from Win32 lParam point packing in CMousePointer::Update @004268C4.
     */
    private static int packPoint(int x, int y) {
        return (x & 0xFFFF) | ((y & 0xFFFF) << 16);
    }

    /**
     * Native: CMousePointer::DisableBackgroundCapture @00437EE0.
     * Fully ported.
     */
    public void disableBackgroundCapture() {
        backgroundCaptureEnabled = 0;
    }

    /**
     * Native: CMousePointer::EnableBackgroundCapture @00437EC0.
     * Fully ported.
     */
    public void enableBackgroundCapture() {
        backgroundCaptureEnabled = 1;
    }

    /**
     * Native: CMousePointer::DrawSelectionBox @0042772F.
     * Fully ported at the Java final-overlay selection boundary.
     */
    public void drawSelectionBox() {
        int width = selectionRect.width();
        int height = selectionRect.height();
        if (width == 0 || height == 0) {
            return;
        }

        CRect normalized = selectionRect.normalized();
        if (normalized.width() <= 0 || normalized.height() <= 0) {
            return;
        }

        Globals.renderer.drawRect(normalized.left, normalized.top, normalized.right - 1, normalized.bottom - 1,
                SELECTION_BOX_COLOR);
        lastDrawnSelectionRect.set(normalized);
    }

    /**
     * Java final-overlay support for CMousePointer::DrawSelectionBox @0042772F.
     */
    public void drawSelectionOverlay() {
        if (selecting != 0) {
            drawSelectionBox();
        }
    }

    /**
     * Native: CMousePointer::FinishSelectionDrag @00427501.
     * Fully ported at the Java final-overlay selection boundary.
     */
    public void finishSelectionDrag() {
        hide();
        drawSelectionBox();
        show();
        selecting = 0;
        totalMoveTime = 0;
        lastMoveTime = System.currentTimeMillis();
    }

    /**
     * Native: CMousePointer::ResetInputTimer @0042675C.
     * Fully ported.
     */
    public void resetInputTimer() {
        inputTimerStart = (int) System.currentTimeMillis();
        inputTimerDuration = HELD_BUTTON_INITIAL_DELAY_MS;
    }

    /**
     * Native: CMousePointer::StartSelectionDrag @0042748F.
     * Fully ported at the Java final-overlay selection boundary.
     */
    public void startSelectionDrag(int x, int y) {
        selecting = 1;
        selectionRect.set(x, y, x, y);
        if (tooltipVisibleFlag != 0) {
            tooltipVisibleFlag = 0;
            restoreDragArea();
        }
        hide();
        eraseSelectionBox();
        show();
    }

    /**
     * Native: CMousePointer::EraseSelectionBox @0042754F.
     * skipped: Java redraws the selection box as a final-frame overlay instead of restoring DirectDraw backup strips
     * captured by Global::CaptureRenderSurfaceRegionToBottomUpPixels @00452ECE.
     */
    void eraseSelectionBox() {
    }

    /**
     * Native: CMousePointer::RestoreDragArea @00427E45.
     * skipped: Java final-overlay tooltips do not capture or restore background pixels with
     * Global::CaptureRenderSurfaceRegionToBottomUpPixels @00452ECE.
     */
    void restoreDragArea() {
    }

    /**
     * Native: CMousePointer::EndDrag @00427EA5.
     * Fully ported at the Java final-overlay tooltip boundary.
     */
    public void endDrag() {
        if (tooltipVisible()) {
            clearTooltip();
        }
    }

    /**
     * Native: CMousePointer::Erase @004264D5.
     * skipped: Java uses platform cursor handles and final-frame overlays instead of DirectDraw cursor back-buffer
     * restoration.
     */
    void erase() {
        cursorVisible = 0;
    }

    /**
     * Native: CMousePointer::DrawCurrentCursorFrame @004263C0.
     * skipped: Java uses GLCursor platform cursor handles instead of DirectDraw cursor image blits and the
     * Global::CaptureRenderSurfaceRegionToBottomUpPixels @00452ECE back-buffer capture.
     */
    void drawCurrentCursorFrame() {
        cursorVisible = 1;
    }

}
