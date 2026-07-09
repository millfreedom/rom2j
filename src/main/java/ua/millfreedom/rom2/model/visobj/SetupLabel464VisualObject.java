package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;

import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_TYPE_CHARACTER_NAME_HERE_256;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_BACK;

/**
 * Native class: SetupLabel464VisualObject (vtbl @0x005CB920).
 * Purpose: editable setup label used by start-game setup dialog `0x466`.
 */
public class SetupLabel464VisualObject extends CVisualObject {
    public static final int NATIVE_SIZE = 0x78; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int STATE_ENABLED = 0x04;
    private static final int MAX_TEXT_LENGTH = 10;
    private static final int CARET_BLINK_INTERVAL_MS = 500;

    //0x5c
    public StartGameSetupDialogVisualObject ownerDialog;
    //0x60
    public String text = "";
    //0x64
    public Palette16 currentPalette;
    //0x68
    public Palette16 palettePrimary;
    //0x6c
    public Palette16 paletteSecondary;
    //0x70
    public int caretVisibleFlag;
    //0x74
    public int blinkTimestamp;

    /**
     * Native: SetupLabel464VisualObject::SetupLabel464VisualObject @00432043.
     * Fully ported.
     */
    public SetupLabel464VisualObject() {
        super();
        initializeLabelPaletteState();
    }

    /**
     * Native: SetupLabel464VisualObject::SetupLabel464VisualObject @004320B0.
     * Fully ported.
     */
    public SetupLabel464VisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            StartGameSetupDialogVisualObject ownerDialog
    ) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        this.ownerDialog = ownerDialog;
        initializeLabelPaletteState();
    }

    /**
     * Native: SetupLabel464VisualObject::InitializeLabelPaletteState @00432188.
     * Fully ported.
     */
    private void initializeLabelPaletteState() {
        palettePrimary = Palettes.p2.paletteData[0];
        paletteSecondary = Palettes.p2.paletteData[0];
        currentPalette = paletteSecondary;
        caretVisibleFlag = 1;
        blinkTimestamp = (int) System.currentTimeMillis();
        m_nState |= 0x2;
    }

    /**
     * vtbl +0x14: SetupLabel464VisualObject::GetText @00432555.
     * Fully ported.
     */
    @Override
    public String getText() {
        if (ownerDialog.dialogActiveFlag == 0) {
            return null;
        }
        return get(MAIN_TYPE_CHARACTER_NAME_HERE_256);
    }

    /**
     * vtbl +0x2C: SetupLabel464VisualObject::Update @0043240A.
     * Fully ported.
     */
    @Override
    public void update() {
        int currentTicks = (int) System.currentTimeMillis();
        String displayText = text;
        if (caretVisibleFlag != 0 && checkStateFlag(STATE_ENABLED) != 0) {
            displayText = text + '|';
        }

        Globals.renderer.lockSurface();
        try {
            CRect ownerRect = ownerDialog.getRect();
            drawLabelText(
                    ownerRect.left + cRect.left,
                    ownerRect.top + cRect.bottom,
                    displayText,
                    currentPalette
            );
            if (Integer.compareUnsigned(currentTicks - blinkTimestamp, CARET_BLINK_INTERVAL_MS) > 0) {
                blinkTimestamp = currentTicks;
                caretVisibleFlag ^= 1;
            }
        } finally {
            Globals.renderer.unlockSurface();
        }
    }

    /**
     * vtbl +0x4C: SetupLabel464VisualObject::OnMouseMove @004322D3.
     * Fully ported.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        CRect ownerRect = ownerDialog.getRect();
        CRect screenRect = new CRect(
                ownerRect.left + cRect.left,
                ownerRect.top + cRect.top,
                ownerRect.left + cRect.right,
                ownerRect.top + cRect.bottom
        );
        currentPalette = screenRect.contains(x, y) ? palettePrimary : paletteSecondary;
        return 0;
    }

    /**
     * vtbl +0x54: SetupLabel464VisualObject::OnLButtonDown @00432352.
     * Fully ported.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        if (cRect.contains(x, y)) {
            ownerDialog.switchEnabledChild(this, true);
        }
        return 0;
    }

    /**
     * vtbl +0x6C: SetupLabel464VisualObject::OnKeyDown @00432397.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        if (nChar == VK_BACK) {
            removeTrailingCharacter();
            return 1;
        }
        return super.onKeyDown(nChar);
    }

    /**
     * vtbl +0x74: SetupLabel464VisualObject::OnChar @004323CF.
     * Fully ported at the Java WM_CHAR boundary; GLFW supplies Unicode code points where native MFC supplied ANSI chars.
     */
    @Override
    public int onChar(int nChar) {
        if (text.length() < MAX_TEXT_LENGTH) {
            appendInputCodepoint(nChar);
            return 1;
        }
        return 0;
    }

    /**
     * Native support extracted from SetupLabel464VisualObject::AppendNormalizedChar @004321E9.
     * Java receives WM_CHAR from GLFW as a Unicode code point, not an ANSI byte.
     */
    private void appendInputCodepoint(int codepoint) {
        caretVisibleFlag = 1;
        blinkTimestamp = (int) System.currentTimeMillis();
        if (codepoint > 0x1F) {
            ownerDialog.playSetupLabel464InputSound();
            text += Character.toString(codepoint);
        }
    }

    /**
     * Native: SetupLabel464VisualObject::RemoveTrailingCharacter @00432234.
     * Fully ported.
     */
    private void removeTrailingCharacter() {
        int nextLength = Math.max(0, text.length() - 1);
        text = text.substring(0, nextLength);
        ownerDialog.playSetupLabel464InputSound();
    }

    /**
     * Native: SetupLabel464VisualObject::SetLabelText @004322B7.
     * Fully ported.
     */
    public void setLabelText(String text) {
        this.text = text;
    }

    /**
     * Native: SetupLabel464VisualObject::GetLabelText @00438430.
     * Fully ported.
     */
    public String getLabelText() {
        return text;
    }


    /**
     * Native owner: `gFont4` draw path inside SetupLabel464VisualObject::Update @0043240A.
     */
    private static void drawLabelText(int x, int yBottom, String text, Palette16 palette) {
        Globals.fonts.font4.drawTextInternal(
                x,
                yBottom - Globals.fonts.font4.getFrameHeight(),
                text,
                0,
                palette
        );
    }

}
