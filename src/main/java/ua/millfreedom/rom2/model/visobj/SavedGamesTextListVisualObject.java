package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.palette.Palette16;

import java.util.List;

/**
 * Native class: SavedGamesTextListVisualObject.
 * Purpose: save/load dialog text-list specialization for saved-game rows.
 */
public class SavedGamesTextListVisualObject extends TextListVisualObject {
    public static final int NATIVE_SIZE = 0x98; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    //0x94
    public List<String> tooltipRows;

    /**
     * Native: SavedGamesTextListVisualObject::SavedGamesTextListVisualObject @0043DDB9.
     * Fully ported.
     */
    public SavedGamesTextListVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, Object bitmapFont, Palette16 field0x7c, Palette16 field0x80, int field0x90, String name) {
        super(id, new CRect(xLeft, yTop, xRight, yBottom), bitmapFont, field0x7c, field0x80, field0x90, name);
    }

    /**
     * vtbl +0x14: SavedGamesTextListVisualObject::GetText @0043DE1B.
     * Fully ported.
     */
    @Override
    public String getText() {
        int rowOffset = getRowOffsetAtScreenY(Globals.mousePointer.getY());
        if (rowOffset >= visibleRowCount) {
            return null;
        }

        int rowIndex = firstVisibleRow + rowOffset;
        if (rowIndex >= rows.size()) {
            return null;
        }

        String tooltip = tooltipRows.get(rowIndex);
        return tooltip == null || tooltip.isEmpty() ? null : tooltip;
    }

    /**
     * Native: SavedGamesTextListVisualObject::SetTooltipRows @0043DE02.
     * Fully ported.
     */
    public void setTooltipRows(List<String> tooltipRows) {
        this.tooltipRows = tooltipRows;
    }

    /**
     * Native: SavedGamesTextListVisualObject::AddRow @0044F210.
     * Fully ported.
     */
    public void addRow(String rowText) {
        rows.add(rowText);
        if (selectedRow < 0) {
            selectedRow += 1;
        }
    }
}
