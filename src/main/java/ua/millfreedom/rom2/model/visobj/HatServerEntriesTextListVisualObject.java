package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;

import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.PatchText.CONNECTING_PLEASE_WAIT_114;
import static ua.millfreedom.rom2.text.PatchText.EASY_98;
import static ua.millfreedom.rom2.text.PatchText.ERROR_DOWNLOADING_SERVER_LIST_113;
import static ua.millfreedom.rom2.text.PatchText.HARD_100;
import static ua.millfreedom.rom2.text.PatchText.HORROR_101;
import static ua.millfreedom.rom2.text.PatchText.MEDIUM_99;
import static ua.millfreedom.rom2.text.TextTableId.PATCH;

/**
 * Native class: HatServerEntriesTextListVisualObject.
 * Purpose: HAT server browser text-list specialization for server rows.
 */
public class HatServerEntriesTextListVisualObject extends TextListVisualObject {
    public static final int NATIVE_SIZE = 0x98; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!
    private static final int TEXT_SHADOW_OFFSET = 1;
    private static final int SERVER_COLUMN_CLIP_RIGHT_OFFSET = 0xD9;
    private static final int MAP_COLUMN_X_OFFSET = 0xDC;
    private static final int MAP_COLUMN_CLIP_RIGHT_OFFSET = 0x160;
    private static final int SIZE_COLUMN_X_OFFSET = 0x163;
    private static final int LEVEL_COLUMN_X_OFFSET = 0x1AE;
    private static final int PLAYERS_COLUMN_X_OFFSET = 0x203;
    private static final int UNJOINABLE_SERVER_PALETTE_INDEX = 0x0F;

    //0x94
    public int refreshState = -1;

    /**
     * Native: HatServerEntriesTextListVisualObject::HatServerEntriesTextListVisualObject @0044E890.
     * Fully ported.
     */
    public HatServerEntriesTextListVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, Object bitmapFont, Palette16 field0x7c, Palette16 field0x80, int field0x90, String name) {
        super(id, new CRect(xLeft, yTop, xRight, yBottom), bitmapFont, field0x7c, field0x80, field0x90, name);
    }

    /**
     * vtbl +0x7C: HatServerEntriesTextListVisualObject::DrawRowText @00449FCF.
     * Fully ported.
     */
    @Override
    public void drawRowText(int rowIndex, int x, int y, Palette16 textPalette) {
        String placeholderRowText = getPlaceholderRowText(rowIndex);
        if (placeholderRowText != null) {
            bitmapFont.drawTextShadowed(x + 5, y, placeholderRowText, 0, Palettes.gray, TEXT_SHADOW_OFFSET);
            return;
        }
        if (refreshState == 0 || !isRowIndexValid(rowIndex)) {
            return;
        }

        String rowText = rows.get(rowIndex);
        Palette16 resolvedPalette = resolveRowTextPalette(rowIndex, textPalette);
        int mainWindowLeftInset = Globals.mainWindowRect.left;
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);

        drawClippedServerColumn(
                bitmapFont,
                screenRect,
                x + 5,
                y,
                readDelimitedField(rowText, 1),
                resolvedPalette,
                mainWindowLeftInset + SERVER_COLUMN_CLIP_RIGHT_OFFSET
        );
        drawClippedServerColumn(
                bitmapFont,
                screenRect,
                mainWindowLeftInset + MAP_COLUMN_X_OFFSET,
                y,
                readDelimitedField(rowText, 3),
                resolvedPalette,
                mainWindowLeftInset + MAP_COLUMN_CLIP_RIGHT_OFFSET
        );
        drawServerColumn(
                bitmapFont,
                mainWindowLeftInset + SIZE_COLUMN_X_OFFSET,
                y,
                readDelimitedField(rowText, 4),
                resolvedPalette
        );
        drawServerColumn(
                bitmapFont,
                mainWindowLeftInset + LEVEL_COLUMN_X_OFFSET,
                y,
                readDifficultyText(rowText),
                resolvedPalette
        );
        drawServerColumn(
                bitmapFont,
                mainWindowLeftInset + PLAYERS_COLUMN_X_OFFSET,
                y,
                readDelimitedField(rowText, 6),
                resolvedPalette
        );
    }

    /**
     * Native: HatServerEntriesTextListVisualObject::IsServerJoinable @00449E23.
     * Fully ported.
     */
    public boolean isServerJoinable(int rowIndex) {
        if (refreshState == 0) {
            return false;
        }
        if (!isRowIndexValid(rowIndex)) {
            return false;
        }

        int minimumPlayers = readGameSessionMinimumPlayers();
        int maximumPlayers = readGameSessionMaximumPlayers();
        String rowText = rows.get(rowIndex);
        int selectedPlayers = readDifficultyBucket(rowText) + 1;
        int playerToken = readFieldPrefixInt(rowText, 6, 2);
        return selectedPlayers >= minimumPlayers
                && selectedPlayers <= maximumPlayers
                && playerToken != 0x10;
    }

    /**
     * Native: Global::FindDelimitedFieldStart @00449D3D.
     * Fully ported.
     */
    static int findDelimitedFieldStart(String rowText, int delimiterCount) {
        int fieldStart = 0;
        int delimiterIndex = 0;
        String remainingText = rowText;
        while (true) {
            if (delimiterCount <= delimiterIndex) {
                return -1;
            }
            int delimiterPosition = remainingText.indexOf('|');
            fieldStart += delimiterPosition + 1;
            if (delimiterIndex == delimiterCount - 1) {
                return fieldStart;
            }
            remainingText = mfcMid(remainingText, delimiterPosition + 1);
            delimiterIndex++;
        }
    }

    /**
     * Native support extracted from HatServerEntriesTextListVisualObject::DrawRowText @00449FCF and
     * HatServerBrowserDialogVisualObject::OnMessage @0044AB6C.
     */
    static String readDelimitedField(String rowText, int delimiterCount) {
        int fieldStart = findDelimitedFieldStart(rowText, delimiterCount);
        String fieldTail = mfcMid(rowText, fieldStart);
        return mfcLeft(fieldTail, fieldTail.indexOf('|'));
    }

    /**
     * Native support extracted from HatServerEntriesTextListVisualObject::IsServerJoinable @00449E23
     * and CGameSession::getMinimumPlayerCount @0041FAA0.
     */
    private static int readGameSessionMinimumPlayers() {
        return Globals.mainWindow.m_GameSession.getMinimumPlayerCount();
    }

    /**
     * Native support extracted from HatServerEntriesTextListVisualObject::IsServerJoinable @00449E23
     * and CGameSession::getMaximumPlayerCount @0041FAC0.
     */
    private static int readGameSessionMaximumPlayers() {
        return Globals.mainWindow.m_GameSession.getMaximumPlayerCount();
    }

    /**
     * Native placeholder branches inside HatServerEntriesTextListVisualObject::DrawRowText @00449FCF.
     */
    private String getPlaceholderRowText(int rowIndex) {
        if (rowIndex != 0) {
            return null;
        }
        if (refreshState == 0) {
            return get(PATCH, ERROR_DOWNLOADING_SERVER_LIST_113);
        }
        if (refreshState == -1) {
            return get(PATCH, CONNECTING_PLEASE_WAIT_114);
        }
        return null;
    }

    /**
     * Native palette selection path feeding HatServerEntriesTextListVisualObject::DrawRowText @00449FCF.
     * Fully ported.
     */
    private Palette16 resolveRowTextPalette(int rowIndex, Palette16 textPalette) {
        if (!isServerJoinable(rowIndex)) {
            return Palettes.unitPaletteComplements[UNJOINABLE_SERVER_PALETTE_INDEX];
        }
        return textPalette;
    }

    /**
     * Native field-5 difficulty label mapping inside HatServerEntriesTextListVisualObject::DrawRowText @00449FCF.
     */
    private static String readDifficultyText(String rowText) {
        return switch (readDifficultyBucket(rowText)) {
            case 0 -> get(PATCH, EASY_98);
            case 1 -> get(PATCH, MEDIUM_99);
            case 2 -> get(PATCH, HARD_100);
            default -> get(PATCH, HORROR_101);
        };
    }

    /**
     * Native field-5 difficulty bucket clamp inside HatServerEntriesTextListVisualObject::DrawRowText @00449FCF.
     */
    private static int readDifficultyBucket(String rowText) {
        int difficultyFieldStart = findDelimitedFieldStart(rowText, 5);
        int difficultyBucket = rowText.charAt(difficultyFieldStart) - '1';
        if (difficultyBucket < 0) {
            difficultyBucket = 0;
        }
        if (difficultyBucket > 3) {
            difficultyBucket = 3;
        }
        return difficultyBucket;
    }

    /**
     * Native `SetScreenClipRect @0045332A` / `CBitmapFont::DrawTextShadowed` call sites for the first two columns in
     * HatServerEntriesTextListVisualObject::DrawRowText @00449FCF.
     */
    private static void drawClippedServerColumn(
            CBitmapFont cBitmapFont,
            CRect screenRect,
            int x,
            int y,
            String text,
            Palette16 textPalette,
            int clipRight
    ) {
        Globals.renderer.pushClip(screenRect.left, screenRect.top, clipRight, screenRect.bottom);
        try {
            drawServerColumn(cBitmapFont, x, y, text, textPalette);
        } finally {
            Globals.renderer.popClip();
        }
    }

    /**
     * Native `CBitmapFont::DrawTextShadowed` call sites in HatServerEntriesTextListVisualObject::DrawRowText @00449FCF.
     */
    private static void drawServerColumn(CBitmapFont cBitmapFont, int x, int y, String text, Palette16 textPalette) {
        cBitmapFont.drawTextShadowed(x, y, text, 0, textPalette, TEXT_SHADOW_OFFSET);
    }

    /**
     * Native support extracted from the `CString::Mid(..., ..., 2)` / `GetInt` path in
     * HatServerEntriesTextListVisualObject::IsServerJoinable @00449E23.
     */
    private static int readFieldPrefixInt(String rowText, int delimiterCount, int maxLength) {
        return nativeAtoi(mfcLeft(readDelimitedField(rowText, delimiterCount), maxLength));
    }

    /**
     * Native support extracted from MFC CString::Mid call sites in Global::FindDelimitedFieldStart @00449D3D.
     */
    private static String mfcMid(String value, int first) {
        return mfcMid(value, first, value.length() - first);
    }

    /**
     * Native support extracted from MFC CString::Mid call sites in Global::FindDelimitedFieldStart @00449D3D,
     * HatServerEntriesTextListVisualObject::IsServerJoinable @00449E23, and
     * HatServerEntriesTextListVisualObject::DrawRowText @00449FCF.
     */
    private static String mfcMid(String value, int first, int count) {
        if (first < 0) {
            first = 0;
        }
        if (count < 0) {
            count = 0;
        }
        if (first + count > value.length()) {
            count = value.length() - first;
        }
        if (first > value.length()) {
            first = value.length();
            count = 0;
        }
        return value.substring(first, first + count);
    }

    /**
     * Native support extracted from MFC CString::Left call sites in
     * HatServerEntriesTextListVisualObject::DrawRowText @00449FCF.
     */
    private static String mfcLeft(String value, int count) {
        if (count < 0) {
            count = 0;
        } else if (count > value.length()) {
            count = value.length();
        }
        return value.substring(0, count);
    }

    /**
     * Native support extracted from GetInt @00584400.
     */
    private static int nativeAtoi(String value) {
        int index = 0;
        while (index < value.length() && value.charAt(index) <= ' ') {
            index++;
        }

        int sign = 1;
        if (index < value.length()) {
            char signChar = value.charAt(index);
            if (signChar == '-') {
                sign = -1;
                index++;
            } else if (signChar == '+') {
                index++;
            }
        }

        int result = 0;
        while (index < value.length()) {
            int digit = value.charAt(index) - '0';
            if (digit < 0 || digit > 9) {
                break;
            }
            result = result * 10 + digit;
            index++;
        }
        return sign * result;
    }
}
