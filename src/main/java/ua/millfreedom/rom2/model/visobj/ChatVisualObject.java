package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CCursor;
import ua.millfreedom.rom2.model.CMousePointer;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.color.RGB32;
import ua.millfreedom.rom2.model.control.CGameListControl;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.window.CMainWindow;

import java.util.Arrays;

import static ua.millfreedom.rom2.model.enums.MessageCodes.DIALOG_OK;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RETURN_TO_GAME;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.*;

/**
 * Native class: ChatVisualObject.
 * Purpose: top status banner (`id=0x4B0`) with a text block child.
 */
public class ChatVisualObject extends HandlerVisualObject {
    public static final int NATIVE_SIZE = 0x170; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    // not ported. Java-only resize hit-test code for the active chat panel.
    private static final int RESIZE_NONE = 0;

    // not ported. Java-only resize hit-test code for the active chat panel.
    private static final int RESIZE_TOP = 1;

    // not ported. Java-only resize hit-test code for the active chat panel.
    private static final int RESIZE_RIGHT = 2;

    // not ported. Java-only resize hit-test code for the active chat panel.
    private static final int RESIZE_TOP_RIGHT = 3;

    // not ported. Java-only top resize hotspot thickness.
    private static final int BORDER_HIT_SIZE = 5;

    // not ported. Java-only active chat panel margin from the right panel and screen top.
    private static final int RESIZE_LIMIT_MARGIN = 32;

    // not ported. Java-only retained-history text inset.
    private static final int TEXT_LEFT_PADDING = 8;

    // not ported. Java-only retained-history text inset.
    private static final int TEXT_TOP_PADDING = 8;

    // not ported. Java-only retained-history panel shade level.
    private static final int SHADE_LEVEL = 6;

    // not ported. Java-only mouse-wheel scroll step.
    private static final int SCROLL_LINES_PER_WHEEL = 1;

    // not ported. Java-only default retained-history line count shown above the input.
    private static final int DEFAULT_HISTORY_LINES = 8;

    // not ported. Java-only expanded panel border color.
    private static final int BORDER_COLOR = RGB32.from(164, 125, 0);

    // not ported. Java-only separator color between retained history and input.
    private static final int SEPARATOR_COLOR = BORDER_COLOR;

    //0x68 Native 0x100-byte owner buffer cleared when the banner input opens.
    public final byte[] dialogInputResetBuffer = new byte[0x100];
    //0x168
    public SoundConfigRootVisualObject textBlock;
    //0x16c
    public int dialogActiveFlag;
    // not ported. Java-only retained chat source rendered above the active input.
    private CGameListControl gameListControl;
    // not ported. Java-only saved active chat panel height across close/open cycles.
    private int savedChatHeight;
    // not ported. Java-only saved active chat panel width across close/open cycles.
    private int savedChatWidth;
    // not ported. Java-only count of newer retained lines hidden while browsing older history.
    private int scrollOffset;
    // not ported. Java-only active resize mode.
    private int resizeMode;
    // not ported. Java-only mouse anchor for right-edge resizing.
    private int dragStartX;
    // not ported. Java-only mouse anchor for top-edge resizing.
    private int dragStartY;
    // not ported. Java-only panel width at resize start.
    private int dragStartWidth;
    // not ported. Java-only panel height at resize start.
    private int dragStartHeight;

    /**
     * Native: ChatVisualObject::ChatVisualObject @0043B1C5.
     * Fully ported.
     */
    public ChatVisualObject() {
        super();
        initialize();
    }

    /**
     * Native: ChatVisualObject::ChatVisualObject @0043B219.
     * Fully ported.
     */
    public ChatVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        initialize();
    }

    /**
     * Native: ChatVisualObject::ChatVisualObject @0043B285.
     * Fully ported.
     */
    public ChatVisualObject(int id, CRect rect) {
        super(id, rect.left, rect.top, rect.right, rect.bottom, null);
        initialize();
    }

    /**
     * vtbl +0x78: ChatVisualObject::Initialize @0043B2E5.
     * Fully ported.
     */
    @Override
    public void initialize() {
        CBitmapFont bitmapFont = Globals.fonts.font1;
        int textHeight = bitmapFont.getHeight();
        textBlock = new SoundConfigRootVisualObject(
                4,
                TEXT_LEFT_PADDING,
                0,
                cRect.width(),
                textHeight,
                bitmapFont,
                Palettes.gray,
                null
        );
        cRect.bottom = cRect.top + 4 + textHeight;
        addChild(textBlock);
        dialogActiveFlag = 0;
        savedChatHeight = defaultExpandedChatHeight();
        savedChatWidth = cRect.width();
    }

    /**
     * vtbl +0x2C: ChatVisualObject::Update @0043B529.
     * Ported with Java-only merged retained-history rendering while active.
     */
    @Override
    public void update() {
        if (dialogActiveFlag != 0) {
            layoutTextBlockAtBottom();
            super.update();
        }
    }

    /**
     * vtbl +0x30: ChatVisualObject::RenderSelf @0043B51C.
     * Native returns without drawing; Java renders the merged retained-history panel above the active chat input.
     */
    @Override
    public void renderSelf(CRect clipRect) {
        drawActiveChatPanel(clipRect);
    }

    /**
     * vtbl +0x48: ChatVisualObject::OnMessage @0043B474.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * vtbl +0x6C: ChatVisualObject::OnKeyDown @0043B495.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        return switch (nChar) {
            case VK_RETURN -> {
                textBlock.copyCommittedLinesToHistory();
                onMessage(DIALOG_OK, 0, 0);
                yield 1;
            }
            case VK_ESCAPE -> {
                onMessage(RETURN_TO_GAME, 0, 0);
                yield 1;
            }
            case VK_UP -> {
                textBlock.restoreCommittedLinesFromHistory();
                yield super.onKeyDown(nChar);
            }
            default -> super.onKeyDown(nChar);
        };
    }

    /**
     * vtbl +0x80: ChatVisualObject::ShowDialog @0043B3D7.
     * Fully ported.
     */
    @Override
    public void showDialog() {
        Globals.mainWindow.setChatInputCapture(true);
        Arrays.fill(dialogInputResetBuffer, (byte) 0);
        textBlock.resetInputSession();
        scrollOffset = 0;
        refreshMapPanelLayout();
        super.showDialog();
        dialogActiveFlag = 1;
        textBlock.lastBlinkTick = Globals.currentTickMillis();
    }

    /**
     * vtbl +0x84: ChatVisualObject::HideDialog @0043B43F.
     * Fully ported.
     */
    @Override
    public HandlerVisualObject hideDialog(MessageCodes reason) {
        Globals.mainWindow.setChatInputCapture(false);
        dialogActiveFlag = 0;
        resizeMode = RESIZE_NONE;
        return super.hideDialog(reason);
    }

    /**
     * Native: ChatVisualObject::refreshMapPanelLayout @0043B548.
     * Ported with Java-only game-list rectangle synchronization.
     */
    public void refreshMapPanelLayout() {
        layoutForMapVisual(Globals.mainWindow.pMapVisualObject);
    }

    /**
     * Java-only layout refresh for the transparent default message view.
     * not ported.
     */
    public void refreshDefaultGameListLayout() {
        layoutForMapVisual(Globals.mainWindow.pMapVisualObject);
    }

    /**
     * Java-only support shared by active chat and transparent default message layout.
     * not ported.
     */
    private void layoutForMapVisual(CVisualObject mapVisual) {
        int chatBottom = chatBottom(mapVisual);
        int chatHeight = clampChatHeight(savedChatHeight, mapVisual);
        int chatWidth = clampChatWidth(savedChatWidth);
        int top = chatBottom - chatHeight;
        setBounds(0, top, chatWidth, top + chatHeight);
        savedChatHeight = chatHeight;
        savedChatWidth = chatWidth;
        layoutTextBlockAtBottom();
        configureGameListRectForCurrentChat();
        clampScrollOffset();
    }

    /**
     * Java-only retained-history source binding supplied by CMainWindow after MapVisualObject construction.
     * not ported.
     */
    public void attachGameListControl(CGameListControl gameListControl) {
        this.gameListControl = gameListControl;
        clampScrollOffset();
    }

    /**
     * Java-only draw-suppression flag for MapVisualObject's native game-list draw call while chat is active.
     * not ported.
     */
    public boolean suppressesGameListDraw() {
        return Globals.mainWindow.sessionMode != CMainWindow.SESSION_MODE_DEDICATED_SERVER && dialogActiveFlag != 0;
    }

    /**
     * Java-only cursor override for active chat top-edge resizing.
     * not ported.
     */
    public CCursor resizeCursorForPoint(int x, int y) {
        if (dialogActiveFlag == 0) {
            return null;
        }

        return switch (resizeModeAt(x, y)) {
            case RESIZE_TOP -> CMousePointer.Cursor_ArrowN;
            case RESIZE_RIGHT -> CMousePointer.Cursor_ArrowE;
            case RESIZE_TOP_RIGHT -> CMousePointer.Cursor_ArrowNE;
            default -> null;
        };
    }

    /**
     * Java-only mouse movement handler for active chat top-edge resize drags.
     * not ported.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        if (dialogActiveFlag == 0) {
            return 0;
        }
        if (resizeMode != RESIZE_NONE) {
            resizeChat(x - dragStartX, y - dragStartY);
            return 1;
        }
        return screenRect().contains(x, y) ? 1 : 0;
    }

    /**
     * Java-only mouse button handler for starting active chat top-edge resize drags.
     * not ported.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        if (dialogActiveFlag == 0) {
            return 0;
        }
        int hitResizeMode = resizeModeAt(x, y);
        if (hitResizeMode != RESIZE_NONE) {
            resizeMode = hitResizeMode;
            dragStartX = x;
            dragStartY = y;
            dragStartWidth = cRect.width();
            dragStartHeight = cRect.height();
            return 1;
        }
        return screenRect().contains(x, y) ? 1 : 0;
    }

    /**
     * Java-only mouse button handler for ending active chat resize drags.
     * not ported.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        if (dialogActiveFlag == 0) {
            return 0;
        }
        if (resizeMode != RESIZE_NONE) {
            resizeMode = RESIZE_NONE;
            return 1;
        }
        return screenRect().contains(x, y) ? 1 : 0;
    }

    /**
     * Java-only mouse-wheel handler for scrolling retained chat history while the active panel is open.
     * not ported.
     */
    @Override
    public int onMouseWheel(int nFlagsAndDelta, int x, int y) {
        if (dialogActiveFlag == 0) {
            return 0;
        }
        CRect screenRect = screenRect();
        if (!screenRect.contains(x, y)) {
            return 0;
        }
        CRect historyRect = historyScreenRect(screenRect);
        if (!historyRect.contains(x, y)) {
            return 1;
        }

        int wheelDelta = (short) ((nFlagsAndDelta >>> 16) & 0xFFFF);
        if (wheelDelta > 0) {
            scrollOffset += wheelLineStep(wheelDelta);
        } else if (wheelDelta < 0) {
            scrollOffset -= wheelLineStep(wheelDelta);
        }
        clampScrollOffset();
        return 1;
    }

    /**
     * Java-only renderer for the active chat panel and retained history above the input.
     * not ported.
     */
    private void drawActiveChatPanel(CRect screenRect) {
        Globals.renderer.applyShadeToRect(screenRect.left, screenRect.top, screenRect.right, screenRect.bottom, SHADE_LEVEL);
        Globals.renderer.drawRect(screenRect.left, screenRect.top, screenRect.right - 1, screenRect.bottom - 1, BORDER_COLOR);

        CRect historyRect = historyScreenRect(screenRect);
        int separatorY = Math.max(screenRect.top, historyRect.bottom);
        Globals.renderer.drawLine(screenRect.left + 1, separatorY, screenRect.right - 2, separatorY, SEPARATOR_COLOR);
        drawRetainedHistory(historyRect);
    }

    /**
     * Java-only retained-history renderer for the active chat panel.
     * not ported.
     */
    private void drawRetainedHistory(CRect historyRect) {
        int visibleLines = expandedVisibleLineCount(historyRect);
        int firstLineIndex = Math.max(0, gameListControl.getSize() - visibleLines - scrollOffset);
        int endLineIndex = Math.min(gameListControl.getSize(), firstLineIndex + visibleLines);
        Globals.renderer.pushClip(historyRect.left + 1, historyRect.top + 1, historyRect.right - 1, historyRect.bottom);
        try {
            gameListControl.drawLineRangeBottomAligned(
                    firstLineIndex,
                    endLineIndex,
                    historyRect.left + TEXT_LEFT_PADDING,
                    historyRect.bottom - TEXT_TOP_PADDING,
                    Globals.fonts.font1
            );
        } finally {
            Globals.renderer.popClip();
        }
    }

    /**
     * Java-only text-block layout that keeps the input at the active chat panel bottom.
     * not ported.
     */
    private void layoutTextBlockAtBottom() {
        int textHeight = textBlock.getRect().height();
        int top = cRect.height() - inputAreaHeight();
        textBlock.setBounds(TEXT_LEFT_PADDING, top, cRect.width(), top + textHeight);
    }

    /**
     * Java-only active chat resize operation with the bottom edge anchored above any right-side panels.
     * not ported.
     */
    private void resizeChat(int dx, int dy) {
        int width = dragStartWidth;
        int height = dragStartHeight;
        if (resizeMode == RESIZE_RIGHT || resizeMode == RESIZE_TOP_RIGHT) {
            width += dx;
        }
        if (resizeMode == RESIZE_TOP || resizeMode == RESIZE_TOP_RIGHT) {
            height -= dy;
        }

        savedChatWidth = clampChatWidth(width);
        savedChatHeight = clampChatHeight(height, m_pParent);
        refreshMapPanelLayout();
        Globals.mainWindow.pMapVisualObject.areaEffectRefreshPending = 1;
    }

    /**
     * Java-only resize hotspot classifier for the active chat panel.
     * not ported.
     */
    private int resizeModeAt(int x, int y) {
        CRect rect = screenRect();
        boolean nearTop = x >= rect.left
                && x < rect.right + BORDER_HIT_SIZE
                && y >= rect.top
                && y < rect.top + BORDER_HIT_SIZE;
        boolean nearRight = x >= rect.right - BORDER_HIT_SIZE
                && x < rect.right + BORDER_HIT_SIZE
                && y >= rect.top
                && y < rect.bottom;
        if (nearTop && nearRight) {
            return RESIZE_TOP_RIGHT;
        }
        if (nearTop) {
            return RESIZE_TOP;
        }
        return nearRight ? RESIZE_RIGHT : RESIZE_NONE;
    }

    /**
     * Java-only screen-space active chat panel rectangle helper.
     * not ported.
     */
    private CRect screenRect() {
        CRect rect = new CRect();
        clientToScreen(rect, cRect);
        return rect;
    }

    /**
     * Java-only screen-space retained-history rectangle helper.
     * not ported.
     */
    private CRect historyScreenRect() {
        return historyScreenRect(screenRect());
    }

    /**
     * Java-only screen-space retained-history rectangle helper.
     * not ported.
     */
    private CRect historyScreenRect(CRect screenRect) {
        int bottom = Math.max(screenRect.top, screenRect.bottom - inputAreaHeight() - 1);
        return new CRect(screenRect.left, screenRect.top, screenRect.right, bottom);
    }

    /**
     * Java-only retained-history rectangle helper in the chat parent's coordinate space.
     * not ported.
     */
    private CRect historyClientRect() {
        int bottom = Math.max(cRect.top, cRect.bottom - inputAreaHeight() - 1);
        return new CRect(cRect.left, cRect.top, cRect.right, bottom);
    }

    /**
     * Java-only synchronization that makes the transparent default view use the same history area as active chat.
     * not ported.
     */
    private void configureGameListRectForCurrentChat() {
        if (gameListControl != null) {
            gameListControl.configureMessageRect(historyClientRect());
        }
    }

    /**
     * Java-only active chat height clamp.
     * not ported.
     */
    private int clampChatHeight(int height, CVisualObject mapVisual) {
        int minHeight = minChatHeight();
        int maxHeight = Math.max(minHeight, chatBottom(mapVisual) - Globals.screenRect.top - RESIZE_LIMIT_MARGIN);
        return Math.clamp(height, minHeight, maxHeight);
    }

    /**
     * Java-only active chat width clamp.
     * not ported.
     */
    private int clampChatWidth(int width) {
        int minWidth = Math.max(1, Globals.screenRect.width() / 4);
        int maxWidth = Math.max(minWidth, Globals.mainWindow.pRightPanelContainerVisualObject.getRect().left - RESIZE_LIMIT_MARGIN);
        return Math.clamp(width, minWidth, maxWidth);
    }

    /**
     * Java-only minimum active chat panel height.
     * not ported.
     */
    private int minChatHeight() {
        return inputAreaHeight() + 1 + CGameListControl.linePitch(Globals.fonts.font1) + TEXT_TOP_PADDING;
    }

    /**
     * Java-only initial active chat panel height.
     * not ported.
     */
    private int defaultExpandedChatHeight() {
        return inputAreaHeight() + 1 + TEXT_TOP_PADDING + CGameListControl.linePitch(Globals.fonts.font1) * DEFAULT_HISTORY_LINES;
    }

    /**
     * Java-only native-input-area height helper.
     * not ported.
     */
    private int inputAreaHeight() {
        return textBlock.getRect().height() + 4;
    }

    /**
     * Java-only active chat bottom anchor above any open bottom panels.
     * not ported.
     */
    private int chatBottom(CVisualObject mapVisual) {
        return mapVisual.getRect().bottom - panelStackHeight(mapVisual);
    }

    /**
     * Java-only currently-open panel stack height below chat.
     * not ported.
     */
    private int panelStackHeight(CVisualObject mapVisual) {
        int height = 0;
        if (mapVisual.hasSpellPanelChild()) {
            height += Globals.mainWindow.pSpellPanelVisualObject.getRect().height();
        }
        if (mapVisual.hasSelectionPanelChild()) {
            height += Globals.mainWindow.pHeroInventoryControlVisualObject.getRect().height();
        }
        return height;
    }

    /**
     * Java-only visible-line calculation for the retained-history panel.
     * not ported.
     */
    private int expandedVisibleLineCount(CRect historyRect) {
        int usableHeight = Math.max(0, historyRect.height() - TEXT_TOP_PADDING);
        return Math.max(1, usableHeight / CGameListControl.linePitch(Globals.fonts.font1));
    }

    /**
     * Java-only retained-history scroll clamp.
     * not ported.
     */
    private void clampScrollOffset() {
        if (gameListControl == null) {
            scrollOffset = 0;
            return;
        }
        int maxScrollOffset = Math.max(0, gameListControl.getSize() - expandedVisibleLineCount(historyScreenRect()));
        scrollOffset = Math.clamp(scrollOffset, 0, maxScrollOffset);
    }

    /**
     * Java-only Win32 wheel delta to retained-history line-step mapping.
     * not ported.
     */
    private static int wheelLineStep(int wheelDelta) {
        return Math.max(1, Math.abs(wheelDelta) / 120) * SCROLL_LINES_PER_WHEEL;
    }
}
