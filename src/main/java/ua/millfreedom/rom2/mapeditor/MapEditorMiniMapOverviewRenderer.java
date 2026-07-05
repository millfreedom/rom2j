package ua.millfreedom.rom2.mapeditor;

import ua.millfreedom.rom2.model.CBmp256;
import ua.millfreedom.rom2.model.GameBitmapFrame;
import ua.millfreedom.rom2.model.color.RGB16;
import ua.millfreedom.rom2.model.palette.CGamePalette;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.world.ScenarioDescriptor;
import ua.millfreedom.rom2.model.world.TerrainGraphics;

import java.awt.image.BufferedImage;

import static ua.millfreedom.rom2.model.world.TerrainGraphics.reloadTerrainTileGraphics;

/**
 * Editor-only minimap overview raster backed by existing terrain tile graphics.
 * not ported.
 */
final class MapEditorMiniMapOverviewRenderer {
    private static final int TERRAIN_TILE_SIZE = 0x20;
    private static final int TERRAIN_TILE_PIXELS = TERRAIN_TILE_SIZE * TERRAIN_TILE_SIZE;
    private static final int TERRAIN_TILE_VARIANT_MASK = 0x03;
    private static final int TERRAIN_TILE_VARIANT_SHIFT = 4;
    private static final int TERRAIN_TILE_FRAME_MASK = 0x0F;
    private static final int OPAQUE_BLACK = 0xFF0C0C10;

    private int loadedTerrainTileMask = -1;

    /**
     * Java support constructor for editor minimap terrain previews.
     * not ported.
     */
    MapEditorMiniMapOverviewRenderer() {
    }

    /**
     * Java support animation-phase cache key matching the animated-terrain tile id calculation.
     * not ported.
     */
    static int terrainAnimationPhase(int animationTick) {
        return animationTick >> 2;
    }

    /**
     * Java support terrain-asset minimap overview generation.
     * not ported.
     */
    BufferedImage buildOverviewImage(ScenarioDescriptor scenario, int width, int height, int animationTick) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        if (scenario.useTiles != 0) {
            ensureTerrainGraphicsLoaded(scenario.useTiles);
        }
        for (int overviewY = 0; overviewY < height; overviewY++) {
            for (int overviewX = 0; overviewX < width; overviewX++) {
                image.setRGB(overviewX, overviewY, terrainSampleArgb(scenario, overviewX, overviewY, width, height, animationTick));
            }
        }
        return image;
    }

    /**
     * Java support terrain tile-mask loader for editor minimap overview rendering.
     * not ported.
     */
    private void ensureTerrainGraphicsLoaded(int terrainTileMask) {
        if (loadedTerrainTileMask == terrainTileMask) {
            return;
        }
        reloadTerrainTileGraphics(terrainTileMask);
        loadedTerrainTileMask = terrainTileMask;
    }

    /**
     * Java support one terrain-texture sample for the minimap overview.
     * not ported.
     */
    private static int terrainSampleArgb(
            ScenarioDescriptor scenario,
            int overviewX,
            int overviewY,
            int overviewWidth,
            int overviewHeight,
            int animationTick
    ) {
        if (scenario.mapWidth <= 0 || scenario.mapHeight <= 0) {
            return OPAQUE_BLACK;
        }
        int mapPixelX = (int) ((long) overviewX * scenario.mapWidth * TERRAIN_TILE_SIZE / Math.max(1, overviewWidth));
        int mapPixelY = (int) ((long) overviewY * scenario.mapHeight * TERRAIN_TILE_SIZE / Math.max(1, overviewHeight));
        int tileX = Math.clamp(mapPixelX >> 5, 0, scenario.mapWidth - 1);
        int tileY = Math.clamp(mapPixelY >> 5, 0, scenario.mapHeight - 1);
        int sourceX = mapPixelX & (TERRAIN_TILE_SIZE - 1);
        int sourceY = mapPixelY & (TERRAIN_TILE_SIZE - 1);
        int cellIndex = tileY * scenario.mapWidth + tileX;
        int tileWord = scenario.sec1Tiles[cellIndex] & 0xFFFF;
        int tileId = MapEditorTerrainPreviewRenderer.resolveTerrainTileId(
                MapEditorTerrainPreviewRenderer.terrainTileId(tileWord),
                tileX,
                tileY,
                animationTick
        );
        int variant = (tileWord >>> TERRAIN_TILE_VARIANT_SHIFT) & TERRAIN_TILE_VARIANT_MASK;
        int sampledColor = terrainTextureArgb(scenario, tileId, variant, tileWord, sourceX, sourceY);
        return sampledColor == 0 ? MapPreviewPanel.argbForCell(scenario, cellIndex) : sampledColor;
    }

    /**
     * Java support terrain texture/palette lookup shared with the editor terrain preview renderer.
     * not ported.
     */
    private static int terrainTextureArgb(
            ScenarioDescriptor scenario,
            int tileId,
            int variant,
            int tileWord,
            int sourceX,
            int sourceY
    ) {
        CBmp256 tileBitmap = MapEditorTerrainPreviewRenderer.terrainTileBitmap(tileId, variant);
        CGamePalette terrainPalette = MapEditorTerrainPreviewRenderer.terrainTilePalette(tileId);
        if (tileBitmap == null || terrainPalette == null || terrainPalette.paletteData == null) {
            return 0;
        }
        GameBitmapFrame frame = tileBitmap.frames.getFirst();
        int sourceOffset = (tileWord & TERRAIN_TILE_FRAME_MASK) * TERRAIN_TILE_PIXELS
                + sourceY * TERRAIN_TILE_SIZE
                + sourceX;
        if (sourceOffset < 0 || sourceOffset >= frame.data().length) {
            return 0;
        }
        Palette16[] palettePages = terrainPalette.paletteData;
        if (palettePages.length == 0) {
            return 0;
        }
        int brightness = Math.min(MapEditorTerrainPreviewRenderer.terrainBrightnessPage(scenario), palettePages.length - 1);
        RGB16[] colors = palettePages[brightness].data();
        int colorIndex = Byte.toUnsignedInt(frame.data()[sourceOffset]);
        if (colorIndex >= colors.length || colors[colorIndex] == null) {
            return 0;
        }
        return colors[colorIndex].ARGB();
    }
}
