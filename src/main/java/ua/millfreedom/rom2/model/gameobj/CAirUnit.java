package ua.millfreedom.rom2.model.gameobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.CUnitInfo;

public class CAirUnit extends CUnit {
    private static final int AIR_MAP_LAYER = 3;
    private static final int DIRTY_RECT_TILE_ALIGNMENT = 0xFFFFFFE0;
    private static final int DIRTY_RECT_TILE_SIZE = 0x20;

    /**
     * Native: CAirUnit::New @0046BFA6.
     * Fully ported.
     */
    public CAirUnit() {
        z = 0x10;
    }

    /**
     * vtbl +0x10: CAirUnit::Dump @0046C0E0.
     * Fully ported.
     */
    @Override
    public String dump() {
        return "CAirUnit";
    }

    /**
     * vtbl +0x38: CAirUnit::UpdateMapLayer @0046BFF4.
     * Fully ported.
     */
    @Override
    public void updateMapLayer() {
        markObjectLayerCell(AIR_MAP_LAYER, this);
        if (Globals.gamePreferences.animation != 0 || getBlockedDrawState() != 0) {
            return;
        }

        CUnitInfo info = getUnitInfo();
        int leftBase = centerScreenX - info.m_CenterX;
        int top = centerScreenY - info.m_CenterY - terrainHeightOffset - z;
        pMapVisualObject.dirtyRenderRect.unionWith(new CRect(
                leftBase & DIRTY_RECT_TILE_ALIGNMENT,
                top,
                ((leftBase + info.m_Width) & DIRTY_RECT_TILE_ALIGNMENT) + DIRTY_RECT_TILE_SIZE,
                top + info.m_Height
        ));
    }
}
