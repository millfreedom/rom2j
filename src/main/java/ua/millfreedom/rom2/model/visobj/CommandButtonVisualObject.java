package ua.millfreedom.rom2.model.visobj;

import org.jetbrains.annotations.NotNull;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.enums.SfxSounds;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.color.RGB32;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.sound.SoundManager;

import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_RETURN;

/**
 * Native class: CommandButtonVisualObject.
 * Purpose: clickable dialog/menu button with message id and optional hotkey.
 */
public class CommandButtonVisualObject extends CVisualObject {
    public static final int NATIVE_SIZE = 0x78; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    protected static final int STATE_ACTIVE = 0x1;
    protected static final int STATE_ENABLED = 0x4;
    private static final int STATE_VISIBLE = 0x8;
    //0x5c
    public String caption = "";
    //0x60
    public CBitmapFont bitmapFont;
    //0x64
    public Palette16 hoverPalette;
    //0x68
    public int hoverState;
    //0x6c
    public int pressedState;
    //0x70
    public MessageCodes msg;
    //0x74
    public int hotKey;

    /**
     * Native: CommandButtonVisualObject::CommandButtonVisualObject @004D5419.
     * Fully ported.
     */
    public CommandButtonVisualObject() {
        super();
        this.caption = "";
    }

    /**
     * Native: CommandButtonVisualObject::CommandButtonVisualObject @004D5470.
     * Fully ported.
     */
    public CommandButtonVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            String caption,
            @NotNull CBitmapFont bitmapFont,
            Palette16 hoverPalette,
            MessageCodes msg,
            int hotKey,
            String name
    ) {
        super(id, xLeft, yTop, xRight, yBottom, name);
        this.caption = caption;
        this.bitmapFont = bitmapFont;
        this.hoverPalette = hoverPalette;
        this.hoverState = 0;
        this.pressedState = 0;
        this.msg = msg;
        this.hotKey = resolveHotKeyFromCaption(this.caption, hotKey);
        this.m_nState |= 0x2;
    }

    /**
     * Native: CommandButtonVisualObject::CommandButtonVisualObject @004D55CB.
     * Fully ported.
     */
    public CommandButtonVisualObject(
            int id,
            CRect rect,
            String caption,
            CBitmapFont bitmapFont,
            Palette16 hoverPalette,
            MessageCodes msg,
            int hotKey,
            String name
    ) {
        super(id, rect, name);
        this.caption = caption;
        this.bitmapFont = bitmapFont;
        this.hoverPalette = hoverPalette;
        this.hoverState = 0;
        this.pressedState = 0;
        this.msg = msg;
        this.hotKey = resolveHotKeyFromCaption(this.caption, hotKey);
        this.m_nState |= 0x2;
    }

    /**
     * vtbl +0x2C: CommandButtonVisualObject::Update @004D571A.
     * Fully ported.
     */
    @Override
    public void update() {
        if (m_pParent == null) {
            return;
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);

        Globals.renderer.lockSurface();
        try {
            m_pParent.renderSelf(screenRect);
            screenRect.right -= 1;
            screenRect.bottom -= 1;

            boolean pressedInBounds = screenRect.contains(Globals.mousePointer.getX(), Globals.mousePointer.getY())
                    && pressedState != 0;
            int brightEdge = RGB32.from(0x29, 0x45, 0x3F);
            int darkEdge = RGB32.from(0x07, 0x0C, 0x09);
            int shadowOffset = pressedInBounds ? 4 : 2;

            drawTextShadowed(
                    bitmapFont,
                    screenRect.left + 1 + (screenRect.width() / 2),
                    screenRect.top + (screenRect.height() / 2),
                    caption,
                    TextAlign.combine(TextAlign.VERTICAL_CENTER, TextAlign.CENTER),
                    resolveCaptionPalette(),
                    shadowOffset
            );
            drawBevelFrame(
                    screenRect,
                    pressedInBounds ? darkEdge : brightEdge,
                    pressedInBounds ? brightEdge : darkEdge
            );
            if (checkStateFlag(STATE_ACTIVE) == 0) {
                shadeRect(screenRect, 3);
            }
        } finally {
            Globals.renderer.unlockSurface();
        }
    }

    /**
     * vtbl +0x4C: CommandButtonVisualObject::OnMouseMove @004D5D1B.
     * Fully ported.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        if (m_pParent == null || checkStateFlag(STATE_ACTIVE) == 0) {
            return 0;
        }

        if (checkStateFlag(STATE_ENABLED) == 0) {
            m_pParent.switchEnabledChild(this, true);
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        if (!screenRect.contains(x, y)) {
            if (checkStateFlag(STATE_VISIBLE) != 0 && pressedState == 0) {
                setVisible(0);
            }
            if (hoverState == 1) {
                hoverState = 0;
                draw();
            }
            return 0;
        }

        if (checkStateFlag(STATE_VISIBLE) == 0) {
            setVisible(1);
        }
        if (hoverState == 0) {
            hoverState = 1;
            draw();
        }
        return 0;
    }

    /**
     * vtbl +0x54: CommandButtonVisualObject::OnLButtonDown @004D5E2D.
     * Fully ported.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        if (m_pParent == null || checkStateFlag(STATE_ACTIVE) == 0) {
            return 0;
        }
        if (pressedState != 0) {
            return 0;
        }
        setPressedState(true);
        return 1;
    }

    /**
     * vtbl +0x58: CommandButtonVisualObject::OnLButtonUp @004D5E74.
     * Fully ported.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        if (m_pParent == null || checkStateFlag(STATE_ACTIVE) == 0) {
            return 0;
        }
        if (pressedState == 0) {
            return 0;
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        setPressedState(false);
        if (screenRect.contains(x, y)) {
            postButtonMessage();
        }
        return 1;
    }

    /**
     * vtbl +0x6C: CommandButtonVisualObject::OnKeyDown @004D5F0F.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        if (m_pParent == null || checkStateFlag(STATE_ACTIVE) == 0) {
            return 0;
        }
        if (nChar != VK_RETURN) {
            return 0;
        }
        postButtonMessage();
        return 1;
    }

    /**
     * vtbl +0x74: CommandButtonVisualObject::OnChar @004D5F60.
     * Fully ported.
     */
    @Override
    public int onChar(int nChar) {
        if (m_pParent == null || checkStateFlag(STATE_ACTIVE) == 0) {
            return 0;
        }
        if (Character.toLowerCase(nChar) != hotKey) {
            return 0;
        }
        postButtonMessage();
        return 1;
    }

    /**
     * Native helper: CommandButtonVisualObject constructor hotkey scan loop @004D5470 / @004D55CB.
     * Fully ported.
     */
    private static int resolveHotKeyFromCaption(String caption, int fallbackHotKey) {
        int idx = 0;
        int len = caption.length();
        while (true) {
            idx += 1;
            if (len <= idx) {
                return fallbackHotKey;
            }

            char curr = caption.charAt(idx);
            char prev = caption.charAt(idx - 1);
            if (curr == '~' && prev == '~') {
                idx += 2;
                continue;
            }
            if (curr != '~' && prev == '~') {
                return Character.toLowerCase(curr);
            }
        }
    }

    /**
     * Native helper: CommandButtonVisualObject::SetPressedState @004D5C41.
     * Fully ported.
     */
    protected final void setPressedState(boolean pressed) {
        if (!pressed) {
            pressedState = 0;
            if (checkStateFlag(STATE_VISIBLE) != 0) {
                setVisible(0);
            }
            draw();
            return;
        }

        SoundManager.playSfx(SfxSounds.CLICK01);
        pressedState = 1;
        if (checkStateFlag(STATE_VISIBLE) == 0) {
            setVisible(1);
        }
        draw();
    }

    /**
     * Native helper branch inside CommandButtonVisualObject::Update @004D571A.
     * Fully ported.
     */
    private Palette16 resolveCaptionPalette() {
        if (hoverPalette == null) {
            if (((hoverState == 0 && checkStateFlag(STATE_ENABLED) == 0)
                    || checkStateFlag(STATE_ACTIVE) == 0)) {
                return Palettes.grayDim;
            }
            return Palettes.yellowish;
        }
        if (hoverState == 0) {
            return Palettes.yellowish;
        }
        return Palettes.brownish;
    }

    /**
     * Native owner: CBitmapFont::DrawTextShadowed @0045D536 call site in CommandButtonVisualObject::Update @004D571A.
     * Fully ported.
     */
    private static void drawTextShadowed(
            CBitmapFont bitmapFont,
            int x,
            int y,
            String text,
            int textAlignFlags,
            Palette16 textPalette,
            int shadowOffset
    ) {
        bitmapFont.drawTextShadowed(x, y, text, textAlignFlags, textPalette, shadowOffset);
    }

    /**
     * Native support extracted from bevel/frame raster branches in CommandButtonVisualObject::Update @004D571A.
     * Fully ported.
     */
    private static void drawBevelFrame(CRect screenRect, int topLeftColor, int bottomRightColor) {
        Globals.renderer.drawLine(screenRect.right, screenRect.top + 2, screenRect.right, screenRect.bottom - 2, bottomRightColor);
        Globals.renderer.drawLine(screenRect.right - 1, screenRect.top + 1, screenRect.right - 1, screenRect.bottom - 1, bottomRightColor);
        Globals.renderer.drawLine(screenRect.left + 2, screenRect.bottom, screenRect.right - 2, screenRect.bottom, bottomRightColor);
        Globals.renderer.drawLine(screenRect.left + 1, screenRect.bottom - 1, screenRect.right - 1, screenRect.bottom - 1, bottomRightColor);
        Globals.renderer.drawLine(screenRect.left + 2, screenRect.top, screenRect.right - 2, screenRect.top, topLeftColor);
        Globals.renderer.drawLine(screenRect.left, screenRect.top + 2, screenRect.left, screenRect.bottom - 2, topLeftColor);
        Globals.renderer.drawLine(screenRect.left + 1, screenRect.top + 1, screenRect.left + 1, screenRect.top + 1, topLeftColor);
        Globals.renderer.drawLine(screenRect.right - 2, screenRect.bottom - 2, screenRect.right - 2, screenRect.bottom - 2, bottomRightColor);
    }

    /**
     * Native support extracted from FUN_004564DF disabled-overlay branch in CommandButtonVisualObject::Update @004D571A.
     * Fully ported.
     */
    private static void shadeRect(CRect screenRect, int shadeLevel) {
        Globals.renderer.applyShadeToRect(screenRect.left, screenRect.top, screenRect.right, screenRect.bottom, shadeLevel);
    }

    /**
     * Native owner: CWnd::PostMessage(mainWnd, msg, 0, 0) branches in CommandButtonVisualObject::OnLButtonUp/@004D5E74, OnKeyDown/@004D5F0F, OnChar/@004D5F60.
     * System-boundary bridge. Java deliberately routes the recovered message tuple through `Globals.mainWindow.postMessage(...)`.
     * Fully ported.
     */
    private void postButtonMessage() {
        Globals.mainWindow.postMessage(msg, 0, 0);
    }

}
