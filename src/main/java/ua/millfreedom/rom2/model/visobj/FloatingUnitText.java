package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CPlayer;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;

/**
 * Native support type for the short-lived unit text entries rendered from MapVisualObject::RenderFrame @00406F43.
 */
final class FloatingUnitText {
    private static final int MAX_LIFETIME_DELTA_MS = 0x3E8;

    //0x00
    private String text;

    //0x04
    private int value;

    //0x08
    private final Palette16 palette;

    //0x0C
    private int xOffset;

    //0x10
    private int yOffset;

    //0x14
    private final int timestamp;

    //0x18
    private final int moveLeftFlag;

    //0x1C
    private final CUnit unit;

    //0x20
    private final int createdServerLoopCounter;

    /**
     * Native: FloatingUnitText::FloatingUnitText @0045C4EA.
     * Full port.
     */
    FloatingUnitText(int value, Palette16 palette, int moveLeftFlag, int xOffset, int yOffset, CUnit unit) {
        this.value = value;
        this.text = Integer.toString(value);
        this.palette = palette == null ? Palettes.unitPaletteComplements[unit.cPlayer.color] : palette;
        this.moveLeftFlag = moveLeftFlag;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.unit = unit;
        this.timestamp = currentTick();
        this.createdServerLoopCounter = Globals.mainWindow.serverLoopCounter;
    }

    /**
     * Native: FloatingUnitText::progress @0045C67B.
     * Full port.
     */
    void progress() {
        if (moveLeftFlag == 0) {
            xOffset++;
        } else {
            xOffset--;
        }
        yOffset -= 2;
    }

    /**
     * Native support extracted from FloatingUnitText::drawIfAlive @0045C6CD.
     * Full port.
     */
    boolean drawIfAlive() {
        if (Integer.compareUnsigned(currentTick() - timestamp, MAX_LIFETIME_DELTA_MS) > 0) {
            return false;
        }
        if (unit.bIsBlocked != 0) {
            return true;
        }

        CBitmapFont font = Globals.fonts.font2;
        font.drawTextShadowed(
                unit.centerScreenX + xOffset,
                unit.centerScreenY + yOffset - unit.terrainHeightOffset,
                text,
                TextAlign.DEFAULT.mask,
                palette,
                1
        );
        return true;
    }

    /**
     * Native support extracted from addOrMergeFloatingUnitText @0045C801 merge-key comparison.
     * Full port.
     */
    boolean hasSameMergeKey(FloatingUnitText other) {
        return unit == other.unit && createdServerLoopCounter == other.createdServerLoopCounter;
    }

    /**
     * Native support extracted from addOrMergeFloatingUnitText @0045C801 value/text merge branch.
     * Full port.
     */
    void mergeValue(FloatingUnitText other) {
        value += other.value;
        text = Integer.toString(value);
    }

    /**
     * Native: FloatingUnitText::screenX @0041EF90.
     * Fully ported.
     */
    int screenX() {
        return unit.centerScreenX + xOffset;
    }

    /**
     * Native: FloatingUnitText::screenY @0041EFB0.
     * Fully ported.
     */
    int screenY() {
        return (unit.centerScreenY + yOffset) - unit.terrainHeightOffset;
    }

    /**
     * Java support for native timeGetTime reads in FloatingUnitText::FloatingUnitText @0045C4EA
     * and FloatingUnitText::drawIfAlive @0045C6CD.
     */
    private static int currentTick() {
        return (int) System.currentTimeMillis();
    }
}
