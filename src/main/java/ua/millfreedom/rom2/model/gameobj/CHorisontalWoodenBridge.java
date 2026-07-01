package ua.millfreedom.rom2.model.gameobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.StructureDef;
import ua.millfreedom.rom2.model.palette.CGamePalette;

public class CHorisontalWoodenBridge extends CBridge {
    /**
     * Native: CHorisontalWoodenBridge::CHorisontalWoodenBridge @004A67C0.
     * Fully ported.
     */
    public CHorisontalWoodenBridge(int bridgeSizeX, int bridgeSizeY) {
        super(bridgeSizeX, bridgeSizeY);
    }

    /**
     * vtbl +0x28: CHorisontalWoodenBridge::Draw @0046283C.
     * Full port. Native uses a horizontal-bridge-specific frame map.
     */
    @Override
    public void draw(int viewTileX, int viewTileY, int palettePage) {
        StructureDef def = getStructureDef();
        int relativeX = viewTileX + pMapVisualObject.view.x - tileX;
        int worldY = viewTileY + pMapVisualObject.view.y;
        int relativeY = worldY - tileY;
        int frameIndex = resolveBridgeFrameIndex(relativeX, worldY, relativeY);

        int drawX = viewTileX << 5;
        int drawY = (viewTileY << 5) - z - terrainHeightOffset;
        CGamePalette mainPalette = getMainPalette(def);
        def.getSpriteMain().draw(drawX, drawY, frameIndex, palettePage, false);
        if (Globals.gamePreferences.smoothing != 0) {
            def.getSpriteSecondary().drawFrameClippedY(drawX, drawY, frameIndex, palettePage, mainPalette, false);
        }
    }

    /**
     * vtbl +0x2C: CHorisontalWoodenBridge::DrawShadow @00462A3D.
     * Full port. Native override returns without drawing.
     */
    @Override
    public void drawShadow(int viewTileX, int viewTileY) {
    }

    /**
     * Native support extracted from CHorisontalWoodenBridge::Draw @0046283C.
     */
    private int resolveBridgeFrameIndex(int relativeX, int worldY, int relativeY) {
        if (relativeX == 0 && worldY == tileY) {
            return 0;
        }
        if (relativeX == bridgeSizeX - 1 && worldY == tileY) {
            return 3;
        }
        if (worldY == tileY) {
            return (relativeX & 1) + 1;
        }
        if (relativeX == 0 && relativeY == bridgeSizeY - 1) {
            return 7;
        }
        if (relativeX == bridgeSizeX - 1 && relativeY == bridgeSizeY - 1) {
            return 0xD;
        }
        if (relativeY == bridgeSizeY - 1) {
            if ((relativeX & 1) == 0) {
                return (pMapVisualObject.mapAnimationTick & 0x3) + 8;
            }
            return 0xC;
        }
        if (relativeX == 0) {
            return 4;
        }
        if (relativeX == bridgeSizeX - 1) {
            return 6;
        }
        return 5;
    }
}
