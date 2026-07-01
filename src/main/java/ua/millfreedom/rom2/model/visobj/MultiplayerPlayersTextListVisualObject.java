package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.palette.Palette16;

import java.util.ArrayList;
import java.util.List;

/**
 * Native class: MultiplayerPlayersTextListVisualObject.
 * Purpose: multiplayer setup text-list specialization for player rows.
 */
public class MultiplayerPlayersTextListVisualObject extends TextListVisualObject {
    public static final int NATIVE_SIZE = 0xA8; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!
    private static final int TEXT_SHADOW_OFFSET = 1;

    //0x94
    public final List<Palette16> rowPalettes = new ArrayList<>();

    /**
     * Native: MultiplayerPlayersTextListVisualObject::MultiplayerPlayersTextListVisualObject @0044EAF0.
     * Fully ported.
     */
    public MultiplayerPlayersTextListVisualObject(int id, CRect rect, Object bitmapFont, Palette16 field0x7c, Palette16 field0x80, int linkedChildId, String name) {
        super(id, rect, bitmapFont, field0x7c, field0x80, linkedChildId, name);
    }

    /**
     * vtbl +0x7C: MultiplayerPlayersTextListVisualObject::DrawRowText @0044B090.
     * Fully ported.
     */
    @Override
    public void drawRowText(int rowIndex, int x, int y, Palette16 textPalette) {
        if (!isRowIndexValid(rowIndex)) {
            return;
        }

        String text = rows.get(rowIndex);
        Palette16 rowPalette = rowPalettes.get(rowIndex);
        bitmapFont.drawTextShadowed(x, y, text, 0, rowPalette, TEXT_SHADOW_OFFSET);
    }
}
