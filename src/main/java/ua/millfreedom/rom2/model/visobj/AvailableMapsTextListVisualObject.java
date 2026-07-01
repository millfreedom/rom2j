package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;

import java.util.function.IntSupplier;

import static ua.millfreedom.rom2.text.DialogsText.MAP_SIZE_134;
import static ua.millfreedom.rom2.text.DialogsText.THE_DIFFICULTY_LEVEL_OF_THE_MAP_MAPS_SHOWN_IN_GRAY_ARE_TOO_136;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.PatchText.EASY_98;
import static ua.millfreedom.rom2.text.PatchText.HARD_100;
import static ua.millfreedom.rom2.text.PatchText.HORROR_101;
import static ua.millfreedom.rom2.text.PatchText.MEDIUM_99;
import static ua.millfreedom.rom2.text.TextTableId.DIALOGS;
import static ua.millfreedom.rom2.text.TextTableId.PATCH;

/**
 * Native class: AvailableMapsTextListVisualObject.
 * Purpose: multiplayer map-selection text-list specialization for available maps.
 */
public class AvailableMapsTextListVisualObject extends TextListVisualObject {
    public static final int NATIVE_SIZE = 0x98; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!
    private static final int MAP_SIZE_TOOLTIP_BOUNDARY = 300;
    private static final int DIFFICULTY_TOOLTIP_BOUNDARY = 0x180;
    private static final int MAP_NAME_X_OFFSET = 0x1E;
    private static final int MAP_SIZE_X_OFFSET = 300;
    private static final int DIFFICULTY_X_OFFSET = 0x180;
    private static final int TEXT_SHADOW_OFFSET = 1;
    private static final int UNAVAILABLE_MAP_PALETTE_INDEX = 0x0F;

    //0x94
    public IntSupplier selectedMapIndexBinding;

    /**
     * Native: AvailableMapsTextListVisualObject::AvailableMapsTextListVisualObject @0044E940.
     * Fully ported.
     */
    public AvailableMapsTextListVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            Object bitmapFont,
            Palette16 field0x7c,
            Palette16 field0x80,
            int field0x90,
            String name,
            IntSupplier selectedMapIndexBinding
    ) {
        super(id, new CRect(xLeft, yTop, xRight, yBottom), bitmapFont, field0x7c, field0x80, field0x90, name);
        this.selectedMapIndexBinding = selectedMapIndexBinding;
    }

    /**
     * vtbl +0x14: AvailableMapsTextListVisualObject::GetText @00449C82.
     * Fully ported.
     */
    @Override
    public String getText() {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);

        int mouseX = Globals.mousePointer.getX();
        if (mouseX < screenRect.left + MAP_SIZE_TOOLTIP_BOUNDARY) {
            int rowIndex = firstVisibleRow + getRowOffsetAtScreenY(Globals.mousePointer.getY());
            if (!isRowIndexValid(rowIndex)) {
                return null;
            }
            return readMapDescription(rowIndex);
        }
        if (mouseX < screenRect.left + DIFFICULTY_TOOLTIP_BOUNDARY) {
            return get(DIALOGS, MAP_SIZE_134);
        }
        return get(DIALOGS, THE_DIFFICULTY_LEVEL_OF_THE_MAP_MAPS_SHOWN_IN_GRAY_ARE_TOO_136);
    }

    /**
     * vtbl +0x7C: AvailableMapsTextListVisualObject::DrawRowText @00449A61.
     * Fully ported.
     */
    @Override
    public void drawRowText(int rowIndex, int x, int y, Palette16 textPalette) {
        if (!isRowIndexValid(rowIndex)) {
            return;
        }

        String rowText = rows.get(rowIndex);
        if (rowIndex == selectedMapIndexBinding.getAsInt()) {
            drawSelectedMapMarker(x, y);
        }

        Palette16 resolvedPalette = resolveRowTextPalette(rowText, textPalette);
        drawColumnText(bitmapFont, x + MAP_NAME_X_OFFSET, y, readMapName(rowText), resolvedPalette);
        drawColumnText(bitmapFont, x + MAP_SIZE_X_OFFSET, y, readMapSizeText(rowText), resolvedPalette);
        drawColumnText(bitmapFont, x + DIFFICULTY_X_OFFSET, y, readDifficultyText(rowText), resolvedPalette);
    }

    /**
     * Native parent-array lookup in AvailableMapsTextListVisualObject::GetText @00449C82.
     * Fully ported.
     */
    private String readMapDescription(int rowIndex) {
        MultiplayerMapSelectionDialogVisualObject ownerDialog =
                (MultiplayerMapSelectionDialogVisualObject) m_pParent;
        return ownerDialog.availableMaps.get(rowIndex).tooltipText;
    }

    /**
     * Native first-column split inside AvailableMapsTextListVisualObject::DrawRowText @00449A61.
     */
    private static String readMapName(String rowText) {
        int separatorIndex = rowText.indexOf('#');
        return rowText.substring(0, separatorIndex);
    }

    /**
     * Native second-column split inside AvailableMapsTextListVisualObject::DrawRowText @00449A61.
     */
    private static String readMapSizeText(String rowText) {
        int firstSeparatorIndex = rowText.indexOf('#');
        int secondSeparatorIndex = rowText.indexOf('#', firstSeparatorIndex + 1);
        return rowText.substring(firstSeparatorIndex + 1, secondSeparatorIndex);
    }

    /**
     * Native last-character difficulty mapping inside AvailableMapsTextListVisualObject::DrawRowText @00449A61.
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
     * Native difficulty bucket clamp inside AvailableMapsTextListVisualObject::DrawRowText @00449A61.
     */
    private static int readDifficultyBucket(String rowText) {
        int difficultyBucket = rowText.charAt(rowText.length() - 1) - '1';
        if (difficultyBucket < 0) {
            difficultyBucket = 0;
        }
        if (difficultyBucket > 3) {
            difficultyBucket = 3;
        }
        return difficultyBucket;
    }

    /**
     * Native compatibility gate inside AvailableMapsTextListVisualObject::DrawRowText @00449A61.
     */
    private static boolean isCompatibleWithCurrentSession(String rowText) {
        int selectedPlayers = readDifficultyBucket(rowText) + 1;
        return selectedPlayers >= Globals.mainWindow.m_GameSession.getMinimumPlayerCount()
                && selectedPlayers <= Globals.mainWindow.m_GameSession.getMaximumPlayerCount();
    }

    /**
     * Native palette path feeding AvailableMapsTextListVisualObject::DrawRowText @00449A61, including
     * `g_pal16Colors[15]` for maps outside the current session player-count bounds.
     */
    private Palette16 resolveRowTextPalette(String rowText, Palette16 textPalette) {
        if (!isCompatibleWithCurrentSession(rowText)) {
            textPalette = Palettes.unitPaletteComplements[UNAVAILABLE_MAP_PALETTE_INDEX];
        }
        return textPalette;
    }

    /**
     * Native owner: `g_Bmp_Server->Draw(...)` marker branch in AvailableMapsTextListVisualObject::DrawRowText @00449A61.
     */
    private static void drawSelectedMapMarker(int x, int y) {
        GUI.server.draw(x, y, 0, 0, false);
    }

    /**
     * Native owner: `CBitmapFont::DrawTextShadowed` call sites in AvailableMapsTextListVisualObject::DrawRowText @00449A61.
     */
    private static void drawColumnText(CBitmapFont cBitmapFont, int x, int y, String text, Palette16 textPalette) {
        cBitmapFont.drawTextShadowed(x, y, text, 0, textPalette, TEXT_SHADOW_OFFSET);
    }
}
