package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.color.RGB16;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.palette.Palette16;

/**
 * Native class: StaticTextVisualObject.
 * Purpose: editable single-line text visual with font/palette binding.
 */
public class StaticTextVisualObject extends CVisualObject {
    public static final int NATIVE_SIZE = 0x7C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int STATE_ACTIVE = 0x01;
    private static final int STATE_ENABLED = 0x04;
    private static final int MK_LBUTTON = 0x1;
    private static final int CARET_BLINK_INTERVAL_MS = 500;
    private static final int TEXT_LEFT_PADDING = 4;
    private static final int LIGHT_BORDER_COLOR_565 = RGB16.from(0x08, 0x08, 0x08).val() & 0xFFFF;
    private static final int DARK_BORDER_COLOR_565 = RGB16.from(0x5E, 0x73, 0x65).val() & 0xFFFF;
    private static final int CARET_COLOR_565 = RGB16.from(0xFF, 0xFF, 0xFF).val() & 0xFFFF;

    //0x5C
    public String text = "";
    //0x60
    public CBitmapFont bitmapFont;
    //0x64
    public Palette16 fontPalette;
    //0x68
    public int selectionStart;
    //0x6C
    public int selectionEnd;
    //0x70
    public int caretIndex;
    //0x74
    public int caretVisibleFlag;
    //0x78
    public int lastBlinkTick;

    /**
     * Native: StaticTextVisualObject::StaticTextVisualObject @004D5FC4.
     * Fully ported.
     */
    public StaticTextVisualObject() {
        super();
        this.text = "";
    }

    /**
     * Native: StaticTextVisualObject::StaticTextVisualObject @004D601B.
     * Fully ported.
     */
    public StaticTextVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            CBitmapFont bitmapFont,
            Palette16 fontPalette,
            String name
    ) {
        super(id, xLeft, yTop, xRight, yBottom, name);
        this.text = "";
        this.bitmapFont = bitmapFont;
        this.fontPalette = fontPalette;
        this.selectionStart = 0;
        this.selectionEnd = 0;
        this.caretIndex = 0;
        this.caretVisibleFlag = 1;
        this.lastBlinkTick = 0;
        this.m_nState |= 0x2;
    }

    /**
     * Native: StaticTextVisualObject::StaticTextVisualObject @004D60E8.
     * Fully ported.
     */
    public StaticTextVisualObject(
            int id,
            CRect rect,
            CBitmapFont bitmapFont,
            Palette16 fontPalette,
            String name
    ) {
        super(id, rect, name);
        this.text = "";
        this.bitmapFont = bitmapFont;
        this.fontPalette = fontPalette;
        this.selectionStart = 0;
        this.selectionEnd = 0;
        this.caretIndex = 0;
        this.caretVisibleFlag = 1;
        this.lastBlinkTick = 0;
        this.m_nState |= 0x2;
    }

    /**
     * vtbl +0x2C: StaticTextVisualObject::Update @004D61A9.
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
            drawFrameBorder(screenRect);
            if (selectionStart != selectionEnd) {
                drawSelectionFill(screenRect);
            }
            drawTextShadowed(screenRect);
            if (checkStateFlag(STATE_ENABLED) != 0 && caretVisibleFlag != 0) {
                drawCaretFill(screenRect);
            }
        } finally {
            Globals.renderer.unlockSurface();
        }
    }

    /**
     * vtbl +0x3C: StaticTextVisualObject::getValue @0044EDB0.
     * Fully ported. Native copies `text` into the pointer-sized destination buffer with `strcpy`; Java models the
     * recovered `char *` destination through `StringBuilder`.
     */
    @Override
    public void getValue(Object value) {
        if (value instanceof StringBuilder out) {
            copyTextToBuffer(out);
            return;
        }
        throw new IllegalArgumentException("StaticTextVisualObject slot 0x3C expects StringBuilder payload");
    }

    /**
     * Native support: typed Java adapter for StaticTextVisualObject slot `0x3C` @0044EDB0.
     */
    public void copyTextToBuffer(StringBuilder out) {
        out.setLength(0);
        out.append(text);
    }

    /**
     * vtbl +0x40: StaticTextVisualObject::getValueRecursiveSize @0044EDA0.
     * Fully ported.
     */
    @Override
    public int getValueRecursiveSize() {
        return 4;
    }

    /**
     * vtbl +0x44: StaticTextVisualObject::setValue @0044ED80.
     * Fully ported. Native assigns the pointer-sized `LPCSTR` payload through `CString::operator= @005A1960`;
     * that callee maps null input to an empty string.
     */
    @Override
    public void setValue(Object value) {
        if (value == null) {
            setInputText(null);
            return;
        }
        if (value instanceof CharSequence inputText) {
            setInputText(inputText.toString());
            return;
        }
        throw new IllegalArgumentException("StaticTextVisualObject slot 0x44 expects CharSequence payload");
    }

    /**
     * Native support: typed Java adapter for StaticTextVisualObject slot `0x44` @0044ED80 and
     * CString::operator= @005A1960.
     */
    public void setInputText(String inputText) {
        text = inputText == null ? "" : inputText;
    }

    /**
     * Native support extracted from StaticTextVisualObject::Update @004D61A9,
     * StaticTextVisualObject::InsertNormalizedCharacter @004D667A,
     * StaticTextVisualObject::DeleteSelectedText @004D67ED, and StaticTextVisualObject::OnKeyDown @004D6B73
     * CString::Left call sites.
     */
    private static String leftText(String value, int count) {
        if (count <= 0) {
            return "";
        }
        if (count >= value.length()) {
            return value;
        }
        return value.substring(0, count);
    }

    /**
     * Native support extracted from StaticTextVisualObject::InsertNormalizedCharacter @004D667A,
     * StaticTextVisualObject::DeleteSelectedText @004D67ED, and StaticTextVisualObject::OnKeyDown @004D6B73
     * CString::Right call sites.
     */
    private static String rightText(String value, int count) {
        if (count <= 0) {
            return "";
        }
        int valueLength = value.length();
        if (count >= valueLength) {
            return value;
        }
        return value.substring(valueLength - count);
    }

    /**
     * vtbl +0x48: StaticTextVisualObject::OnMessage @004D71AF.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        int currentTick = (int) System.currentTimeMillis();
        if (msg == MessageCodes.STATIC_TEXT_CARET_BLINK_TICK
                && Integer.compareUnsigned(currentTick - lastBlinkTick, CARET_BLINK_INTERVAL_MS) > 0) {
            lastBlinkTick = currentTick;
            caretVisibleFlag = caretVisibleFlag == 0 ? 1 : 0;
            if (checkStateFlag(STATE_ACTIVE) != 0) {
                draw();
            }
        }
        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * vtbl +0x4C: StaticTextVisualObject::OnMouseMove @004D69B4.
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
        lastBlinkTick = (int) System.currentTimeMillis();
        if ((nFlags & MK_LBUTTON) != 0) {
            m_pParent.switchEnabledChild(this, true);
            int nextCaretIndex = getCaretIndexAtScreenX(x);
            if (nextCaretIndex < caretIndex) {
                if (selectionEnd == selectionStart) {
                    selectionEnd = caretIndex;
                    selectionStart = nextCaretIndex;
                } else if (selectionStart < nextCaretIndex) {
                    selectionEnd = nextCaretIndex;
                } else {
                    selectionStart = nextCaretIndex;
                }
            } else if (caretIndex < nextCaretIndex) {
                if (selectionEnd == selectionStart) {
                    selectionStart = caretIndex;
                    selectionEnd = nextCaretIndex;
                } else if (nextCaretIndex < selectionEnd) {
                    selectionStart = nextCaretIndex;
                } else {
                    selectionEnd = nextCaretIndex;
                }
            }
            caretIndex = nextCaretIndex;
            resetCaretBlink();
            draw();
        }
        return 0;
    }

    /**
     * vtbl +0x54: StaticTextVisualObject::OnLButtonDown @004D6B02.
     * Fully ported.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        if (m_pParent == null || checkStateFlag(STATE_ACTIVE) == 0) {
            return 0;
        }

        lastBlinkTick = (int) System.currentTimeMillis();
        caretIndex = getCaretIndexAtScreenX(x);
        selectionEnd = caretIndex;
        selectionStart = selectionEnd;
        draw();
        return 1;
    }

    /**
     * vtbl +0x6C: StaticTextVisualObject::OnKeyDown @004D6B73.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        if (checkStateFlag(STATE_ENABLED) == 0
                || m_pParent == null
                || checkStateFlag(STATE_ACTIVE) == 0) {
            return 0;
        }

        lastBlinkTick = (int) System.currentTimeMillis();
        int handled = 0;
        switch (nChar) {
            case 0x08 -> {
                if (caretIndex != 0) {
                    caretIndex--;
                    String suffix = rightText(text, text.length() - caretIndex - 1);
                    String prefix = leftText(text, caretIndex);
                    text = prefix + suffix;
                    resetCaretBlink();
                    draw();
                    handled = 1;
                }
            }
            case 0x23 -> {
                int textLength = text.length();
                if (caretIndex < textLength) {
                    if (getShiftState() == 0) {
                        selectionEnd = selectionStart;
                    } else {
                        if (selectionStart == selectionEnd) {
                            selectionStart = caretIndex;
                        } else if (caretIndex < selectionEnd) {
                            selectionStart = selectionEnd;
                        }
                        selectionEnd = textLength;
                    }
                    caretIndex = textLength;
                    resetCaretBlink();
                    draw();
                    handled = 1;
                }
            }
            case 0x24 -> {
                if (caretIndex != 0) {
                    if (getShiftState() == 0) {
                        selectionEnd = selectionStart;
                    } else {
                        if (selectionStart == selectionEnd) {
                            selectionEnd = caretIndex;
                        } else if (selectionStart < caretIndex) {
                            selectionEnd = selectionStart;
                        }
                        selectionStart = 0;
                    }
                    caretIndex = 0;
                    resetCaretBlink();
                    draw();
                    handled = 1;
                }
            }
            case 0x25 -> {
                if (caretIndex != 0) {
                    caretIndex--;
                    if (getShiftState() == 0) {
                        selectionEnd = selectionStart;
                    } else if (selectionEnd == selectionStart) {
                        selectionEnd = caretIndex + 1;
                        selectionStart = caretIndex;
                    } else if (caretIndex + 1 == selectionStart) {
                        selectionStart--;
                    } else {
                        selectionEnd--;
                    }
                    resetCaretBlink();
                    draw();
                    handled = 1;
                }
            }
            case 0x27 -> {
                int textLength = text.length();
                if (caretIndex < textLength) {
                    caretIndex++;
                    if (getShiftState() == 0) {
                        selectionEnd = selectionStart;
                    } else if (selectionEnd == selectionStart) {
                        selectionStart = caretIndex - 1;
                        selectionEnd = caretIndex;
                    } else if (caretIndex - 1 == selectionEnd) {
                        selectionEnd++;
                    } else {
                        selectionStart++;
                    }
                    resetCaretBlink();
                    draw();
                    handled = 1;
                }
            }
            case 0x2E -> {
                if (selectionEnd == selectionStart
                        || selectionEnd - selectionStart < 0) {
                    int textLength = text.length();
                    if (caretIndex < textLength) {
                        String suffix = rightText(text, textLength - caretIndex - 1);
                        String prefix = leftText(text, caretIndex);
                        text = prefix + suffix;
                    }
                } else {
                    deleteSelectedText();
                }
                resetCaretBlink();
                draw();
                handled = 1;
            }
            default -> {
            }
        }
        m_pParent.onMessage(MessageCodes.TEXT_LIST_SELECTION_CHANGED, id, 0);
        return handled;
    }

    /**
     * vtbl +0x74: StaticTextVisualObject::OnChar @004D70F0.
     * Fully ported.
     */
    @Override
    public int onChar(int nChar) {
        if (isNativeAlnum(nChar) && checkStateFlag(STATE_ENABLED) == 0) {
            m_pParent.switchEnabledChild(this, true);
        }

        if (checkStateFlag(STATE_ENABLED) != 0
                && m_pParent != null
                && checkStateFlag(STATE_ACTIVE) != 0) {
            lastBlinkTick = (int) System.currentTimeMillis();
            if (nChar > 0x1F) {
                insertNormalizedCharacter((byte) nChar);
            }
            draw();
            m_pParent.onMessage(MessageCodes.TEXT_LIST_SELECTION_CHANGED, id, 0);
            return 1;
        }
        return 0;
    }

    /**
     * Native helper: StaticTextVisualObject::InsertNormalizedCharacter @004D667A.
     * Fully ported.
     */
    private void insertNormalizedCharacter(byte rawChar) {
        if (selectionEnd != selectionStart
                && selectionEnd - selectionStart >= 0) {
            deleteSelectedText();
        }

        char normalizedChar = (char) normalizeHotKeyInput(rawChar);
        String suffix = rightText(text, text.length() - caretIndex);
        String prefix = leftText(text, caretIndex);
        String updatedText = prefix + normalizedChar + suffix;
        if (getTextWidth(updatedText) + 8 < cRect.width()) {
            text = updatedText;
            caretIndex++;
        }
        resetCaretBlink();
    }

    /**
     * Native helper: StaticTextVisualObject::DeleteSelectedText @004D67ED.
     * Fully ported.
     */
    private void deleteSelectedText() {
        String suffix = rightText(text, text.length() - selectionEnd);
        String prefix = leftText(text, selectionStart);
        text = prefix + suffix;
        selectionEnd = selectionStart;
        caretIndex = selectionStart;
    }

    /**
     * Native helper: StaticTextVisualObject::GetCaretIndexAtScreenX @004D68D9.
     * Fully ported.
     */
    private int getCaretIndexAtScreenX(int screenX) {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        int caretIndex = 0;
        while (caretIndex < text.length()) {
            int prefixWidth = getTextWidth(text.substring(0, caretIndex + 1));
            if (screenRect.left + TEXT_LEFT_PADDING + prefixWidth >= screenX) {
                return caretIndex;
            }
            caretIndex++;
        }
        return caretIndex;
    }

    /**
     * Native helper: StaticTextVisualObject::ResetCaretBlink @004D7237.
     * Fully ported.
     */
    private void resetCaretBlink() {
        lastBlinkTick = (int) System.currentTimeMillis();
        caretVisibleFlag = 1;
    }

    /**
     * Native support extracted from StaticTextVisualObject::Update @004D61A9 and
     * ScrollablePanelHeaderStaticTextVisualObject::Update @004E06E0.
     */
    protected static void drawFrameBorder(CRect screenRect) {
        Globals.renderer.drawLine(
                screenRect.left + 1,
                screenRect.top,
                screenRect.right - 1,
                screenRect.top,
                (short) LIGHT_BORDER_COLOR_565
        );
        Globals.renderer.drawLine(
                screenRect.left,
                screenRect.top + 1,
                screenRect.left,
                screenRect.bottom - 1,
                (short) LIGHT_BORDER_COLOR_565
        );
        Globals.renderer.drawLine(
                screenRect.right,
                screenRect.top + 1,
                screenRect.right,
                screenRect.bottom - 1,
                (short) DARK_BORDER_COLOR_565
        );
        Globals.renderer.drawLine(
                screenRect.left + 1,
                screenRect.bottom,
                screenRect.right - 1,
                screenRect.bottom,
                (short) DARK_BORDER_COLOR_565
        );
    }

    /**
     * Native owner: selection fill branch inside StaticTextVisualObject::Update @004D61A9.
     * Fully ported.
     */
    private void drawSelectionFill(CRect screenRect) {
        int selectionLeft = screenRect.left + TEXT_LEFT_PADDING + getTextWidth(leftText(text, selectionStart));
        int selectionRight = screenRect.left + TEXT_LEFT_PADDING + getTextWidth(leftText(text, selectionEnd));
        Globals.renderer.applyShadeToRect(selectionLeft, screenRect.top + 2, selectionRight, screenRect.bottom - 2, 0x0C);
    }

    /**
     * Native owner: CBitmapFont::DrawTextShadowed @0045D536 call site in StaticTextVisualObject::Update @004D61A9.
     * Fully ported.
     */
    private void drawTextShadowed(CRect screenRect) {
        bitmapFont.drawTextShadowed(
                screenRect.left + TEXT_LEFT_PADDING,
                screenRect.top + screenRect.height() / 2,
                text,
                TextAlign.VERTICAL_CENTER.mask,
                fontPalette,
                1
        );
    }

    /**
     * Native owner: caret fill branch inside StaticTextVisualObject::Update @004D61A9.
     * Fully ported.
     */
    private void drawCaretFill(CRect screenRect) {
        String caretPrefix = leftText(text, caretIndex);
        int caretLeft = screenRect.left + TEXT_LEFT_PADDING + getTextWidth(caretPrefix);
        int caretRight = screenRect.left + TEXT_LEFT_PADDING + 2 + getTextWidth(caretPrefix);
        Globals.renderer.fillScreenRect(caretLeft, screenRect.top + 2, caretRight, screenRect.bottom - 2, (short) CARET_COLOR_565);
    }

    /**
     * Native support: keySHIFT read in StaticTextVisualObject::OnKeyDown @004D6B73.
     * Fully ported.
     */
    private static int getShiftState() {
        return Globals.shiftKeyDown ? 1 : 0;
    }

    /**
     * Native helper: NormalizeHotKeyInput @00474C85.
     * Fully ported.
     */
    private static int normalizeHotKeyInput(int c) {
        int value = c & 0xFF;
        if (Globals.useCustomEncoding && value > 0x7F) {
            if (value >= 0xC0 && value <= 0xEF) {
                return value - 0x40;
            }
            if (value > 0xEF) {
                return value - 0x10;
            }
        }
        return value;
    }

    /**
     * Native support: CBaseFont::GetTextWidth @0045CB59 calls from StaticTextVisualObject input/update methods.
     * Fully ported.
     */
    private int getTextWidth(String value) {
        return bitmapFont.getTextWidth(value);
    }

    /**
     * Native support: C runtime isalnum gate in StaticTextVisualObject::OnChar @004D70F0.
     * Fully ported.
     */
    private static boolean isNativeAlnum(int value) {
        int ch = value & 0xFF;
        return (ch >= '0' && ch <= '9')
                || (ch >= 'A' && ch <= 'Z')
                || (ch >= 'a' && ch <= 'z');
    }
}
