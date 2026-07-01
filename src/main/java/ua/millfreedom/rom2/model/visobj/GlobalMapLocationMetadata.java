package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.model.CRect;

import java.util.ArrayList;
import java.util.List;

/**
 * Native class: GlobalMapLocationMetadata, formerly UNK_0x4c.
 * Element type of GlobalMapDialogVisualObject locationMetadata at native offset +0x16C.
 */
public class GlobalMapLocationMetadata {
    //0x0
    public final CRect rect1 = new CRect();
    //0x10
    public final CRect rect2 = new CRect();
    //0x20 Current middle-scroll bitmap repeat count; collapsed to 1 or expanded to expandedMiddleTileCount.
    public int visibleMiddleTileCount;
    //0x24 Middle-scroll bitmap repeat count copied into visibleMiddleTileCount when the panel expands on hover.
    public int expandedMiddleTileCount;
    //0x28 Selects the scroll bitmap set: 0 uses scrollPanel* bitmaps, non-zero uses regular scroll* bitmaps.
    public int scrollBitmapVariant;
    //0x2c Location index copied into GlobalMapDialogVisualObject.hoveredLocationIndex on panel hover.
    public int locationIndex;
    //0x30 Native CString.
    public String text = "";
    //0x34 Native CStringArray.
    public final List<String> lines = new ArrayList<>();
    //0x48
    public int field48;

    /**
     * Native: GlobalMapLocationMetadata::New @00473740.
     * Fully ported. Java field initializers cover native CRect/CString/CStringArray construction.
     */
    public GlobalMapLocationMetadata() {
        visibleMiddleTileCount = 1;
        expandedMiddleTileCount = 2;
        scrollBitmapVariant = 10;
        locationIndex = 0;
        rect1.set(0, 0, 0, 0);
        field48 = 0;
    }
}
