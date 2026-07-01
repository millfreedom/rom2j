package ua.millfreedom.rom2.model.gameobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.StructureDef;
import ua.millfreedom.rom2.model.palette.CGamePalette;

public class CVerticalWoodenBridge extends CBridge {
    /**
     * Native: CVerticalWoodenBridge::CVerticalWoodenBridge @004A6740.
     * Fully ported.
     */
    public CVerticalWoodenBridge(int bridgeSizeX, int bridgeSizeY) {
        super(bridgeSizeX, bridgeSizeY);
    }

    /**
     * vtbl +0x28: CVerticalWoodenBridge::Draw @00462654.
     * Full port. Native uses a vertical-bridge-specific frame map.
     */
    @Override
    public void draw(int viewTileX, int viewTileY, int palettePage) {
        StructureDef def = getStructureDef();
        int relativeX = viewTileX + pMapVisualObject.view.x - tileX;
        int relativeY = viewTileY + pMapVisualObject.view.y - tileY;
        int frameIndex = resolveBridgeFrameIndex(viewTileX, viewTileY, relativeX, relativeY);

        int drawX = viewTileX << 5;
        int drawY = (viewTileY << 5) - z - terrainHeightOffset;
        CGamePalette mainPalette = getMainPalette(def);
        def.getSpriteMain().draw(drawX, drawY, frameIndex, palettePage, false);
        if (Globals.gamePreferences.smoothing != 0) {
            def.getSpriteSecondary().drawFrameClippedY(drawX, drawY, frameIndex, palettePage, mainPalette, false);
        }
    }

    /**
     * vtbl +0x2C: CVerticalWoodenBridge::DrawShadow @0046282F.
     * Full port. Native override returns without drawing.
     */
    @Override
    public void drawShadow(int viewTileX, int viewTileY) {
    }

    /**
     * Native support extracted from CVerticalWoodenBridge::Draw @00462654.
     */
    private int resolveBridgeFrameIndex(int viewTileX, int viewTileY, int relativeX, int relativeY) {
        int worldX = viewTileX + pMapVisualObject.view.x;
        int worldY = viewTileY + pMapVisualObject.view.y;
        if (worldX == tileX && worldY == tileY) {
            return 0;
        }
        if (relativeX == bridgeSizeX - 1 && worldY == tileY) {
            return 2;
        }
        if (worldY == tileY) {
            return 1;
        }
        if (worldX == tileX && relativeY == bridgeSizeY - 1) {
            return 6;
        }
        if (relativeX == bridgeSizeX - 1 && relativeY == bridgeSizeY - 1) {
            return 8;
        }
        if (relativeY == bridgeSizeY - 1) {
            return 7;
        }
        if (worldX == tileX) {
            return 3;
        }
        if (relativeX == bridgeSizeX - 1) {
            return 5;
        }
        return 4;
    }
}
