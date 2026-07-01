package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.CTextFile;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.res.Resources;

import java.util.LinkedHashMap;
import java.util.Map;

import static ua.millfreedom.rom2.model.enums.MessageCodes.DIALOG_OK;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RENDER_FRAME;
import static ua.millfreedom.rom2.res.Constants.GRAPHICS;
import static ua.millfreedom.rom2.res.Constants.MAIN;
import static ua.millfreedom.rom2.text.TextTableId.CREDITS;

/**
 * Native class: CreditsDialogVisualObject.
 * Purpose: credits dialog with scroll-driven text/logo playback.
 */
public class CreditsDialogVisualObject extends HandlerVisualObject {
    public static final int NATIVE_SIZE = 0xA0; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int CREDITS_SCROLL_STEP_MS = 0x16;
    private static final int CREDITS_SCROLL_HEIGHT = 0x1E0;
    private static final int CREDITS_CENTER_X_OFFSET = 0x140;
    private static final int CREDITS_PRELOAD_LINE_COUNT = 10;
    private static int scrollTimerInitializedFlag;
    private static int lastScrollTick;

    //0x68
    public final Map<String, CBmp64k> assets = new LinkedHashMap<>();

    //0x84
    public CTextFile creditsTextFile = CTextFile.createEmpty(CREDITS.arrayName());

    //0x94
    public int creditsScrollActiveFlag;

    //0x98
    public int firstVisibleCreditsLineIndex;

    //0x9c
    public int creditsScrollOffsetY;

    /**
     * Native: CreditsDialogVisualObject::CreditsDialogVisualObject @0043B960.
     * Fully ported.
     */
    public CreditsDialogVisualObject() {
        super();
        this.handler = assets;
        initialize();
    }

    /**
     * Native: CreditsDialogVisualObject::CreditsDialogVisualObject @0043B9D7.
     * Fully ported.
     */
    public CreditsDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        this.handler = assets;
        initialize();
    }

    /**
     * vtbl +0x78: CreditsDialogVisualObject::InitializeCreditsDialog @0043BAD8.
     * Fully ported.
     */
    @Override
    public void initialize() {
        assets.clear();
        CVisualObject closeButton = new CommandButtonVisualObject(
                4,
                0,
                0,
                0,
                0,
                "",
                Globals.fonts.font1,
                Palettes.grayDim,
                DIALOG_OK,
                0,
                null
        );
        addChild(closeButton);
    }

    /**
     * vtbl +0x2C: CreditsDialogVisualObject::Update @0043BCAC.
     * Fully ported.
     */
    @Override
    public void update() {
        if (creditsScrollActiveFlag == 0) {
            return;
        }

        if ((scrollTimerInitializedFlag & 1) == 0) {
            scrollTimerInitializedFlag |= 1;
            lastScrollTick = (int) System.currentTimeMillis();
        }

        int now = (int) System.currentTimeMillis();
        int fontHeight = Globals.fonts.font1.getFrameHeight();
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        if (Integer.compareUnsigned(now - lastScrollTick, CREDITS_SCROLL_STEP_MS) <= 0) {
            return;
        }

        creditsScrollOffsetY -= 1;
        if (creditsScrollOffsetY < 0) {
            firstVisibleCreditsLineIndex = Math.abs(creditsScrollOffsetY) / fontHeight;
        } else {
            firstVisibleCreditsLineIndex = 0;
        }

        boolean shouldClose;
        Globals.renderer.pushClip(screenRect.left, screenRect.top, screenRect.right, screenRect.bottom);
        try {
            Globals.renderer.fillScreenRect(screenRect.left, screenRect.top, screenRect.right, screenRect.bottom, (short) 0);

            int lastVisibleLineExclusive = firstVisibleCreditsLineIndex + 1 + (CREDITS_SCROLL_HEIGHT / fontHeight);
            int lineCount = creditsTextFile.getLineCount();
            if (lineCount < lastVisibleLineExclusive) {
                lastVisibleLineExclusive = lineCount;
            }

            int firstRenderedLine = Math.max(0, firstVisibleCreditsLineIndex - CREDITS_PRELOAD_LINE_COUNT);
            for (int lineIndex = firstRenderedLine; lineIndex < lastVisibleLineExclusive; lineIndex++) {
                String line = creditsTextFile.getAt(lineIndex);
                int lineY = screenRect.top + creditsScrollOffsetY + lineIndex * fontHeight;
                if (!line.isEmpty() && line.charAt(0) == '"') {
                    drawQuotedLogoLine(screenRect, lineY, line);
                } else if (lineIndex == 0) {
                    drawCenteredCreditsText(screenRect.left + CREDITS_CENTER_X_OFFSET, lineY, line, false);
                } else {
                    String previousLine = creditsTextFile.getAt(lineIndex - 1);
                    drawCenteredCreditsText(
                            screenRect.left + CREDITS_CENTER_X_OFFSET,
                            lineY,
                            line,
                            !previousLine.isEmpty()
                    );
                }
            }
            shouldClose = lastVisibleLineExclusive <= firstVisibleCreditsLineIndex;
        } finally {
            Globals.renderer.popClip();
        }
        if (shouldClose) {
            closeCreditsDialog();
        }
        lastScrollTick = (int) System.currentTimeMillis();
    }

    /**
     * vtbl +0x30: CreditsDialogVisualObject::RenderSelf @0043C048.
     * Fully ported.
     */
    @Override
    public void renderSelf(CRect clipRect) {
    }

    /**
     * vtbl +0x48: CreditsDialogVisualObject::OnMessage @0043BC35.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        if (msg == RENDER_FRAME) {
            draw();
        }
        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * vtbl +0x54: CreditsDialogVisualObject::OnLButtonDown @0043BC95.
     * Fully ported.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        closeCreditsDialog();
        return 0;
    }

    /**
     * vtbl +0x6C: CreditsDialogVisualObject::OnKeyDown @0043BC74.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        closeCreditsDialog();
        return super.onKeyDown(nChar);
    }

    /**
     * vtbl +0x80: CreditsDialogVisualObject::ShowDialog @0043BB76.
     * Fully ported.
     */
    @Override
    public void showDialog() {
        Globals.mousePointer.disableBackgroundCapture();
        creditsScrollOffsetY = CREDITS_SCROLL_HEIGHT;
        loadCreditsAssets();
        creditsScrollActiveFlag = 1;
        Globals.mousePointer.hide();
        clearScreen();
        super.showDialog();
    }

    /**
     * vtbl +0x84: CreditsDialogVisualObject::HideDialog @0043BBF3.
     * Fully ported.
     */
    @Override
    public HandlerVisualObject hideDialog(MessageCodes reason) {
        releaseCreditsAssets();
        Globals.mousePointer.show();
        creditsScrollActiveFlag = 0;
        HandlerVisualObject hidden = super.hideDialog(reason);
        Globals.mousePointer.enableBackgroundCapture();
        return hidden;
    }


    /**
     * Native: CreditsDialogVisualObject::LoadCreditsAssets @0043C055.
     * Fully ported.
     */
    private void loadCreditsAssets() {
        releaseCreditsAssets();
        loadCreditsText();
        for (int i = 0; i < creditsTextFile.getLineCount(); i++) {
            String line = creditsTextFile.getAt(i);
            if (line.isEmpty() || line.charAt(0) != '"') {
                continue;
            }

            assets.put(line, loadCreditsLogoBitmap(line));
        }
    }

    /**
     * Native: CreditsDialogVisualObject::ReleaseCreditsAssets @0043C217.
     * Fully ported.
     */
    private void releaseCreditsAssets() {
        assets.clear();
        creditsTextFile.delete();
    }

    /**
     * Native owner: quoted-logo draw branch inside CreditsDialogVisualObject::Update @0043BCAC.
     * Fully ported support helper.
     */
    private void drawQuotedLogoLine(CRect screenRect, int y, String line) {
        CBmp64k bitmap = assets.get(line);
        if (bitmap == null) {
            return;
        }

        int bitmapWidth = bitmap.surface.width();
        int bitmapHeight = bitmap.surface.height();
        int x = screenRect.left + CREDITS_CENTER_X_OFFSET - (bitmapWidth / 2);
        int drawLeft = Math.max(x, screenRect.left);
        int drawTop = Math.max(y, screenRect.top);
        int drawRight = Math.min(x + bitmapWidth, screenRect.right);
        int drawBottom = Math.min(y + bitmapHeight, screenRect.bottom);
        if (drawLeft >= drawRight || drawTop >= drawBottom) {
            return;
        }

        bitmap.drawRectMasked(
                drawLeft,
                drawTop,
                drawLeft - x,
                drawTop - y,
                drawRight - x,
                drawBottom - y
        );
    }

    /**
     * Native owner: centered `gFont1` draw branches inside CreditsDialogVisualObject::Update @0043BCAC.
     * Fully ported support helper.
     */
    private void drawCenteredCreditsText(int centerX, int y, String text, boolean dimmed) {
        Globals.fonts.font1.drawTextInternal(
                centerX,
                y,
                text,
                TextAlign.CENTER.mask,
                dimmed ? Palettes.grayDim : Palettes.gray
        );
    }

    /**
     * Native owner: `CTextFile::LoadAndParse` call site inside CreditsDialogVisualObject::LoadCreditsAssets @0043C055.
     * Fully ported support helper.
     */
    private void loadCreditsText() {
        creditsTextFile = CTextFile.LoadAndParse(CREDITS);
    }

    /**
     * Native owner: quoted `main/graphics/logo/` bitmap load branch inside CreditsDialogVisualObject::LoadCreditsAssets @0043C055.
     * Fully ported support helper.
     */
    private static CBmp64k loadCreditsLogoBitmap(String line) {
        String bitmapName = line.substring(1, line.length() - 1);
        return new CBmp64k(Resources.path(MAIN, GRAPHICS, "logo", bitmapName));
    }

    /**
     * Native: CreditsDialogVisualObject::closeCreditsDialog @0043C2D2.
     * Fully ported.
     */
    private void closeCreditsDialog() {
        onMessage(DIALOG_OK, 0, 0);
    }

}
