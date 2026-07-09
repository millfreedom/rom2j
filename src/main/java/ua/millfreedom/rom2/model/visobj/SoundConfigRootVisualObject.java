package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CPlayer;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palette16;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_BACK;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_ESCAPE;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_RETURN;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_TAB;

/**
 * Native class: SoundConfigRootVisualObject (vtbl @0x005CBA58).
 * Purpose: status-banner text-entry root used by dialog `0x4B0`.
 */
public class SoundConfigRootVisualObject extends CVisualObject {
    public static final int NATIVE_SIZE = 0xA4; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int MAX_COMMITTED_LINES = 2;
    private static final String CURSOR_SUFFIX = "_";

    //0x5c
    public final List<String> committedLineHistory = new ArrayList<>();
    //0x70
    public final List<String> committedLines = new ArrayList<>();
    //0x84
    public String inputText = "";
    //0x88
    public CBitmapFont bitmapFont;
    //0x8c
    public Palette16 inputTextPalette;
    //0x90
    public int chatRecipientPlayerIndex = -1;
    //0x94
    public int chatRecipientPrefixLength;
    //0x98
    public int lastBlinkTick;
    //0x9c
    public int currentTick;
    //0xa0
    public int cursorVisibleFlag = 1;

    /**
     * Native: SoundConfigRootVisualObject::SoundConfigRootVisualObject @0043A1D8.
     * Fully ported.
     */
    public SoundConfigRootVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            CBitmapFont bitmapFont,
            Palette16 inputTextPalette,
            String name
    ) {
        super(id, xLeft, yTop, xRight, yBottom, name);
        this.bitmapFont = bitmapFont;
        this.inputTextPalette = inputTextPalette;
        this.cursorVisibleFlag = 1;
        this.chatRecipientPlayerIndex = -1;
        this.chatRecipientPrefixLength = 0;
        this.m_nState |= 0x2;
    }

    /**
     * Native: SoundConfigRootVisualObject::resetInputSession @0043A33B.
     * Fully ported.
     */
    void resetInputSession() {
        chatRecipientPlayerIndex = -1;
        chatRecipientPrefixLength = 0;
        committedLines.clear();
        inputText = "";
    }

    /**
     * Native: SoundConfigRootVisualObject::copyCommittedLinesToHistory @0043A435.
     * Fully ported.
     */
    void copyCommittedLinesToHistory() {
        committedLineHistory.clear();
        committedLineHistory.addAll(committedLines);
    }

    /**
     * Native: SoundConfigRootVisualObject::restoreCommittedLinesFromHistory @0043A452.
     * Fully ported.
     */
    void restoreCommittedLinesFromHistory() {
        committedLines.clear();
        committedLines.addAll(committedLineHistory);
        inputText = "";
        if (!committedLines.isEmpty()) {
            int lastIndex = committedLines.size() - 1;
            inputText = committedLines.remove(lastIndex);
        }
    }

    /**
     * vtbl +0x2C: SoundConfigRootVisualObject::Update @0043A9D8.
     * Fully ported.
     */
    @Override
    public void update() {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        Globals.renderer.pushClip(screenRect.left, screenRect.top, screenRect.right, screenRect.bottom);
        try {
            int fontHeight = bitmapFont.getHeight();
            for (int lineIndex = 0; lineIndex < committedLines.size(); lineIndex++) {
                String line = committedLines.get(lineIndex);
                if (lineIndex == 0) {
                    drawTopCommittedLine(screenRect, line, fontHeight);
                } else {
                    int y = screenRect.bottom - fontHeight * ((committedLines.size() - lineIndex) + 1);
                    drawLine(screenRect.left, y, line, inputTextPalette);
                }
            }

            String displayText = cursorVisibleFlag == 0 ? inputText : inputText + CURSOR_SUFFIX;
            if (committedLines.isEmpty()) {
                drawTopCommittedLine(screenRect, displayText, fontHeight);
            } else {
                drawLine(screenRect.left, screenRect.bottom - fontHeight, displayText, inputTextPalette);
            }
        } finally {
            Globals.renderer.popClip();
        }
    }

    /**
     * vtbl +0x3C: SoundConfigRootVisualObject::getValue @0043A3E4.
     * Fully ported. Java models the native `CString *` append target through `StringBuilder`.
     */
    @Override
    public void getValue(Object value) {
        if (value instanceof StringBuilder out) {
            appendCommittedLines(out);
            return;
        }
        throw new IllegalArgumentException("SoundConfigRootVisualObject slot 0x3C expects StringBuilder payload");
    }

    /**
     * Native support: typed Java adapter for SoundConfigRootVisualObject slot `0x3C` @0043A3E4.
     * Fully ported.
     */
    public void appendCommittedLines(StringBuilder out) {
        for (String line : committedLines) {
            out.append(line);
        }
    }

    /**
     * vtbl +0x40: SoundConfigRootVisualObject::getValueRecursiveSize @0043B6C0.
     * Fully ported.
     */
    @Override
    public int getValueRecursiveSize() {
        return 4;
    }

    /**
     * vtbl +0x44: SoundConfigRootVisualObject::setValue @0043A379.
     * Fully ported. Java models the native `LPCSTR` payload through `CharSequence`.
     */
    @Override
    public void setValue(Object value) {
        if (value == null) {
            setInputText(null);
            return;
        }
        if (value instanceof CharSequence text) {
            setInputText(text.toString());
            return;
        }
        throw new IllegalArgumentException("SoundConfigRootVisualObject slot 0x44 expects CharSequence payload");
    }

    /**
     * Native support: typed Java adapter for SoundConfigRootVisualObject slot `0x44` @0043A379.
     * Fully ported.
     */
    public void setInputText(String text) {
        inputText = text == null ? "" : text;
    }

    /**
     * vtbl +0x48: SoundConfigRootVisualObject::OnMessage @0043A92C.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        if (msg == MessageCodes.RENDER_FRAME) {
            currentTick = (int) System.currentTimeMillis();
            if (Integer.compareUnsigned(currentTick - lastBlinkTick, 500) > 0) {
                lastBlinkTick = currentTick;
                cursorVisibleFlag ^= 1;
            }
            Globals.mainWindow.pMapVisualObject.areaEffectRefreshPending = 1;
        }
        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * vtbl +0x6C: SoundConfigRootVisualObject::OnKeyDown @0043A804.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        return switch (nChar) {
            case VK_BACK -> {
                if (Globals.mainWindow.isControlKeyDown()) {
                    removeTrailingWord();
                } else {
                    removeTrailingCharacter();
                }
                yield 1;
            }
            case VK_TAB -> {
                autoCompletePlayerName();
                yield 1;
            }
            case VK_RETURN -> {
                if (!inputText.isEmpty()) {
                    committedLines.add(inputText);
                    inputText = "";
                }
                yield super.onKeyDown(nChar);
            }
            case VK_ESCAPE -> super.onKeyDown(nChar);
            default -> 0;
        };
    }

    /**
     * vtbl +0x74: SoundConfigRootVisualObject::OnChar @0043A8EB.
     * Fully ported at the Java WM_CHAR boundary; GLFW supplies Unicode code points where native MFC supplied ANSI chars.
     */
    @Override
    public int onChar(int nChar) {
        if (nChar < 0x20 || nChar == 0x7F) {
            return super.onChar(nChar);
        }
        appendWrappedCodepoint(nChar);
        return 1;
    }

    /**
     * Native support extracted from SoundConfigRootVisualObject::appendWrappedCharacter @0043A4CB.
     * Java receives WM_CHAR from GLFW as a Unicode code point, not an ANSI byte.
     */
    private int appendWrappedCodepoint(int codepoint) {
        if (bitmapFont.getTextWidth(inputText) + bitmapFont.getTextWidth(CURSOR_SUFFIX) < cRect.width()) {
            inputText += Character.toString(codepoint);
            Globals.mainWindow.pMapVisualObject.areaEffectRefreshPending = 1;
            return 1;
        }

        if (committedLines.size() >= MAX_COMMITTED_LINES) {
            return 0;
        }

        int lastSpace = inputText.lastIndexOf(' ');
        if (lastSpace < 10) {
            committedLines.add(inputText);
            inputText = "";
        } else {
            committedLines.add(inputText.substring(0, lastSpace + 1));
            inputText = inputText.substring(lastSpace + 1);
        }
        inputText = inputText.stripLeading();
        appendWrappedCodepoint(codepoint);
        return 0;
    }

    /**
     * Native: SoundConfigRootVisualObject::removeTrailingCharacter @0043A6E5.
     * Fully ported.
     */
    private int removeTrailingCharacter() {
        int nextLength = Math.max(0, inputText.length() - 1);
        inputText = inputText.substring(0, nextLength).stripLeading();
        if (inputText.isEmpty() && !committedLines.isEmpty()) {
            int lastIndex = committedLines.size() - 1;
            inputText = committedLines.remove(lastIndex).stripLeading();
        }
        return 1;
    }

    /**
     * Native: SoundConfigRootVisualObject::removeTrailingWord @0043B11E.
     * Fully ported.
     */
    private void removeTrailingWord() {
        int lastIndex = inputText.length() - 1;
        if (lastIndex < 0) {
            return;
        }
        if (!isAutocompleteDelimiter(inputText.charAt(lastIndex))) {
            while (lastIndex >= 1 && !isAutocompleteDelimiter(inputText.charAt(lastIndex))) {
                removeTrailingCharacter();
                lastIndex = inputText.length() - 1;
                if (lastIndex < 0) {
                    return;
                }
            }
        }
        removeTrailingCharacter();
    }

    /**
     * Native: SoundConfigRootVisualObject::autoCompletePlayerName @0043ADCD.
     * Fully ported.
     */
    private void autoCompletePlayerName() {
        int tokenStartSearch = inputText.length() - 1;
        if (tokenStartSearch == -1) {
            return;
        }

        int prefixBoundary = 0;
        char firstChar = inputText.charAt(0);
        if (firstChar == '=' || firstChar == '-') {
            prefixBoundary = 1;
        }
        if (isAutocompleteDelimiter(inputText.charAt(tokenStartSearch))) {
            return;
        }

        while (tokenStartSearch >= prefixBoundary && !isAutocompleteDelimiter(inputText.charAt(tokenStartSearch))) {
            tokenStartSearch--;
        }

        String token = inputText.substring(tokenStartSearch + 1);
        if (token.isEmpty()) {
            return;
        }

        int prefixEnd = tokenStartSearch;
        String normalizedToken = normalizeAutocompleteText(token);
        List<String> candidates = new ArrayList<>();
        for (CPlayer player : Globals.mainWindow.pMapVisualObject.clientPlayers) {
            if (player != null && (player.flags & 0x1) == 0) {
                candidates.add(player.name.toString());
            }
        }

        for (int charIndex = 0; charIndex < normalizedToken.length(); charIndex++) {
            int compareIndex = charIndex;
            char expected = normalizedToken.charAt(charIndex);
            candidates.removeIf(candidate -> {
                String normalizedCandidate = normalizeAutocompleteText(candidate);
                return compareIndex >= normalizedCandidate.length()
                        || normalizedCandidate.charAt(compareIndex) != expected;
            });
            if (candidates.isEmpty()) {
                return;
            }
        }

        inputText = inputText.substring(0, prefixEnd + 1) + candidates.getFirst();
        Globals.mainWindow.pMapVisualObject.areaEffectRefreshPending = 1;
    }

    /**
     * Native: SoundConfigRootVisualObject::drawTopCommittedLine @0043ABAD.
     * Fully ported.
     */
    private void drawTopCommittedLine(CRect screenRect, String text, int fontHeight) {
        chatRecipientPlayerIndex = -1;
        chatRecipientPrefixLength = 0;
        int y = screenRect.bottom - fontHeight * (committedLines.size() + 1);
        drawLine(screenRect.left, y, text, inputTextPalette);
        if (!text.isEmpty() && text.charAt(0) == '-') {
            String recipientText = text.substring(1);
            for (int playerIndex = 0; playerIndex < Globals.mainWindow.pMapVisualObject.clientPlayers.size(); playerIndex++) {
                CPlayer player = Globals.mainWindow.pMapVisualObject.clientPlayers.get(playerIndex);
                if (player != null) {
                    String playerName = player.name.toString();
                    if (recipientText.startsWith(playerName)) {
                        chatRecipientPlayerIndex = playerIndex;
                        chatRecipientPrefixLength = playerName.length() + 1;
                        break;
                    }
                }
            }
        }
    }

    /**
     * Native support: font draw call shared by SoundConfigRootVisualObject::Update @0043A9D8
     * and SoundConfigRootVisualObject::drawTopCommittedLine @0043ABAD.
     * Fully ported.
     */
    private void drawLine(int x, int y, String text, Palette16 palette) {
        bitmapFont.drawTextInternal(x, y, text, 0, palette);
    }


    /**
     * Native support extracted from Global::isAutocompleteDelimiter @0043A190.
     * Fully ported.
     */
    private static boolean isAutocompleteDelimiter(char c) {
        return c == ' ' || c == ',' || c == '.' || c == ':' || c == ';';
    }

    /**
     * Native support extracted from Global::normalizeAutocompleteText @00474B12.
     * Java keeps input text as Unicode, so autocomplete compares Unicode lowercase strings.
     */
    private static String normalizeAutocompleteText(String text) {
        return text.toLowerCase(Locale.ROOT);
    }
}
