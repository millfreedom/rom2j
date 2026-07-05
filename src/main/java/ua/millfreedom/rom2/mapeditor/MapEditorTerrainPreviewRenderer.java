package ua.millfreedom.rom2.mapeditor;

import ua.millfreedom.rom2.model.CBmp256;
import ua.millfreedom.rom2.model.GameBitmapFrame;
import ua.millfreedom.rom2.model.Screen;
import ua.millfreedom.rom2.model.palette.CGamePalette;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.render.SwingRenderer;
import ua.millfreedom.rom2.model.world.ScenarioDescriptor;
import ua.millfreedom.rom2.model.world.TerrainGraphics;

import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Editor-only terrain preview painter backed by the existing terrain graphics tables and software renderer.
 * not ported.
 */
final class MapEditorTerrainPreviewRenderer {
    private static final int TERRAIN_TILE_SIZE = 0x20;
    private static final int TERRAIN_TILE_PIXELS = TERRAIN_TILE_SIZE * TERRAIN_TILE_SIZE;
    private static final int TERRAIN_TILE_INDEX_MASK = 0x1FFF;
    private static final int TERRAIN_TILE_FAMILY_SHIFT = 6;
    private static final int TERRAIN_TILE_VARIANT_MASK = 0x03;
    private static final int TERRAIN_TILE_VARIANT_SHIFT = 4;
    private static final int TERRAIN_TILE_FRAME_MASK = 0x0F;
    private static final int TERRAIN_BRIGHTNESS_PAGE_SHIFT = 2;
    private static final int ANIMATED_TERRAIN_FIRST_TILE = 8;
    private static final int ANIMATED_TERRAIN_LAST_TILE = 0x0B;
    private static final int TERRAIN_RENDER_ROW_MARGIN = 6;
    private static final int TERRAIN_RENDER_VERTICAL_MARGIN = TERRAIN_TILE_SIZE * TERRAIN_RENDER_ROW_MARGIN;

    private SwingRenderer terrainRenderer;
    private int terrainRendererWidth;
    private int terrainRendererHeight;
    private int loadedTerrainTileMask = -1;

    /**
     * Java support constructor for editor terrain previews.
     * not ported.
     */
    MapEditorTerrainPreviewRenderer() {
    }

    /**
     * Java support textured terrain overlay for the editor's full-map preview.
     * not ported.
     */
    void drawTerrainOverlay(
            Graphics2D graphics,
            ScenarioDescriptor scenario,
            int left,
            int top,
            int cellSize,
            int animationTick
    ) {
        if (cellSize != TERRAIN_TILE_SIZE || scenario.useTiles == 0) {
            return;
        }

        ensureTerrainGraphicsLoaded(scenario.useTiles);
        int mapWidth = scenario.mapWidth;
        int mapHeight = scenario.mapHeight;
        Rectangle clip = graphics.getClipBounds();
        if (clip == null) {
            clip = new Rectangle(left, top, mapWidth * cellSize, mapHeight * cellSize + viewportBottomPadding(scenario));
        }
        int startX = Math.max(0, Math.floorDiv(clip.x - left, cellSize));
        int startY = Math.max(0, Math.floorDiv(clip.y - top - TERRAIN_RENDER_VERTICAL_MARGIN, cellSize));
        int endX = visibleCellEnd(clip.x + clip.width, left, cellSize, mapWidth);
        int endY = visibleCellEnd(
                clip.y + clip.height + TERRAIN_RENDER_VERTICAL_MARGIN,
                top,
                cellSize,
                mapHeight
        );
        if (startX >= endX || startY >= endY) {
            return;
        }

        int surfaceLeft = left + startX * cellSize;
        int surfaceRight = left + endX * cellSize;
        int surfaceTop = Math.max(0, top + startY * cellSize - TERRAIN_RENDER_VERTICAL_MARGIN);
        int surfaceBottom = Math.min(
                top + mapHeight * cellSize + viewportBottomPadding(scenario),
                top + endY * cellSize + TERRAIN_RENDER_VERTICAL_MARGIN
        );
        int surfaceWidth = surfaceRight - surfaceLeft;
        int surfaceHeight = surfaceBottom - surfaceTop;
        if (surfaceWidth <= 0 || surfaceHeight <= 0) {
            return;
        }

        ensureTerrainRenderer(surfaceWidth, surfaceHeight);
        terrainRenderer.clearSurface();
        int brightnessPage = terrainBrightnessPage(scenario);
        for (int y = startY; y < endY; y++) {
            for (int x = endX - 1; x >= startX; x--) {
                int cellIndex = y * mapWidth + x;
                drawTerrainCell(
                        terrainRenderer,
                        scenario,
                        x,
                        y,
                        scenario.sec1Tiles[cellIndex] & 0xFFFF,
                        left + x * cellSize - surfaceLeft,
                        top + y * cellSize - surfaceTop,
                        cellSize,
                        brightnessPage,
                        animationTick
                );
            }
        }
        terrainRenderer.presentSurface(surfaceWidth, surfaceHeight);
        terrainRenderer.drawTo(graphics, surfaceLeft, surfaceTop, surfaceWidth, surfaceHeight);
    }

    /**
     * Java support terrain tile-mask loader for editor preview rendering.
     * not ported.
     */
    private void ensureTerrainGraphicsLoaded(int terrainTileMask) {
        if (loadedTerrainTileMask == terrainTileMask) {
            return;
        }
        TerrainGraphics.reloadTerrainTileGraphics(terrainTileMask);
        loadedTerrainTileMask = terrainTileMask;
    }

    /**
     * Java support viewport top padding needed because terrain height subtracts from screen Y.
     * not ported.
     */
    static int viewportTopOffset(ScenarioDescriptor scenario) {
        return Math.max(0, maxSignedHeight(scenario)) + TERRAIN_TILE_SIZE;
    }

    /**
     * Java support viewport bottom padding for low signed terrain heights.
     * not ported.
     */
    static int viewportBottomPadding(ScenarioDescriptor scenario) {
        return Math.max(0, -minSignedHeight(scenario)) + TERRAIN_TILE_SIZE;
    }

    /**
     * Java support maximum signed terrain height scan for editor viewport geometry.
     * not ported.
     */
    private static int maxSignedHeight(ScenarioDescriptor scenario) {
        int max = Integer.MIN_VALUE;
        for (byte height : scenario.sec2Heights) {
            max = Math.max(max, height);
        }
        return max;
    }

    /**
     * Java support minimum signed terrain height scan for editor viewport geometry.
     * not ported.
     */
    private static int minSignedHeight(ScenarioDescriptor scenario) {
        int min = Integer.MAX_VALUE;
        for (byte height : scenario.sec2Heights) {
            min = Math.min(min, height);
        }
        return min;
    }

    /**
     * Java support visible-terrain Swing render surface reuse.
     * not ported.
     */
    private void ensureTerrainRenderer(int width, int height) {
        if (terrainRenderer != null && terrainRendererWidth == width && terrainRendererHeight == height) {
            return;
        }
        terrainRenderer = new SwingRenderer(Screen.createBgraSurface(width, height));
        terrainRendererWidth = width;
        terrainRendererHeight = height;
    }

    /**
     * Java support visible-cell start calculation for clipped terrain preview painting.
     * not ported.
     */
    private static int visibleCellStart(int clipStart, int mapStart, int cellSize) {
        if (clipStart <= mapStart) {
            return 0;
        }
        return (clipStart - mapStart) / cellSize;
    }

    /**
     * Java support visible-cell end calculation for clipped terrain preview painting.
     * not ported.
     */
    private static int visibleCellEnd(int clipEnd, int mapStart, int cellSize, int mapCells) {
        if (clipEnd <= mapStart) {
            return 0;
        }
        return Math.min(mapCells, (clipEnd - mapStart + cellSize - 1) / cellSize);
    }

    /**
     * Java support for rendering one native terrain tile word into the visible skewed terrain surface.
     * not ported.
     */
    private void drawTerrainCell(
            SwingRenderer renderer,
            ScenarioDescriptor scenario,
            int tileX,
            int tileY,
            int tileWord,
            int left,
            int top,
            int cellSize,
            int brightnessPage,
            int animationTick
    ) {
        int tileId = resolveTerrainTileId(terrainTileId(tileWord), tileX, tileY, animationTick);
        int variant = (tileWord >>> TERRAIN_TILE_VARIANT_SHIFT) & TERRAIN_TILE_VARIANT_MASK;
        CBmp256 tileBitmap = terrainTileBitmap(tileId, variant);
        CGamePalette terrainPalette = terrainTilePalette(tileId);
        if (tileBitmap == null || terrainPalette == null) {
            return;
        }

        GameBitmapFrame frame = tileBitmap.frames.getFirst();
        int sourceOffset = (tileWord & TERRAIN_TILE_FRAME_MASK) * TERRAIN_TILE_PIXELS;
        if (sourceOffset + TERRAIN_TILE_PIXELS > frame.data().length) {
            return;
        }

        Palette16[] palettePages = terrainPalette.paletteData;
        int brightness = Math.min(brightnessPage, palettePages.length - 1);
        int topLeftY = top - terrainVertexHeightAt(scenario, tileX, tileY);
        int topRightY = top - terrainVertexHeightAt(scenario, tileX + 1, tileY);
        int bottomLeftY = top + cellSize - terrainVertexHeightAt(scenario, tileX, tileY + 1);
        int bottomRightY = top + cellSize - terrainVertexHeightAt(scenario, tileX + 1, tileY + 1);
        if (topLeftY == topRightY && bottomLeftY == bottomRightY && topLeftY + cellSize == bottomLeftY) {
            renderer.drawFlatTerrainTile(
                    left,
                    topLeftY,
                    brightness,
                    brightness,
                    brightness,
                    brightness,
                    frame.data(),
                    sourceOffset,
                    palettePages
            );
            return;
        }
        renderer.drawSkewedTerrainTile(
                left,
                left + cellSize,
                topLeftY,
                topRightY,
                bottomLeftY,
                bottomRightY,
                brightness,
                brightness,
                brightness,
                brightness,
                frame.data(),
                sourceOffset,
                palettePages
        );
    }

    /**
     * Java support brightness-page selection matching MapVisualObject's base terrain brightness convention.
     * not ported.
     */
    static int terrainBrightnessPage(ScenarioDescriptor scenario) {
        return Math.max(0, scenario.darkness >> TERRAIN_BRIGHTNESS_PAGE_SHIFT);
    }

    /**
     * Java support terrain tile-id extraction matching MapVisualObject::DrawFullTerrainFrame @00404E0E.
     * not ported.
     */
    static int terrainTileId(int tileWord) {
        return (tileWord & TERRAIN_TILE_INDEX_MASK) >> TERRAIN_TILE_FAMILY_SHIFT;
    }

    /**
     * Java support extracted from MapVisualObject::DrawFullTerrainFrame @00404E0E animated terrain branch.
     * not ported.
     */
    static int resolveTerrainTileId(int baseTileId, int tileX, int tileY, int animationTick) {
        if (!isAnimatedTerrainTile(baseTileId)) {
            return baseTileId;
        }
        int phase = (baseTileId + (tileX + 1) * tileY + (animationTick >> 2)) & TERRAIN_TILE_VARIANT_MASK;
        return ANIMATED_TERRAIN_FIRST_TILE + phase;
    }

    /**
     * Java support extracted from MapVisualObject::DrawFullTerrainFrame @00404E0E animated terrain test.
     * not ported.
     */
    private static boolean isAnimatedTerrainTile(int tileId) {
        return tileId >= ANIMATED_TERRAIN_FIRST_TILE && tileId <= ANIMATED_TERRAIN_LAST_TILE;
    }

    /**
     * Java support signed height lookup matching MapDescriptor::heightAt and MapVisualObject::mapHeightAt.
     * not ported.
     */
    static int terrainVertexHeightAt(ScenarioDescriptor scenario, int tileX, int tileY) {
        if (tileX < 0 || tileY < 0 || tileX >= scenario.mapWidth || tileY >= scenario.mapHeight) {
            return 0;
        }
        return scenario.sec2Heights[tileY * scenario.mapWidth + tileX];
    }

    /**
     * Java support projected terrain-grid vertex Y shared by editor overlays and hit visuals.
     * not ported.
     */
    static int projectedTerrainVertexY(
            ScenarioDescriptor scenario,
            int vertexX,
            int vertexY,
            int viewportTopOffset,
            int cellSize
    ) {
        return viewportTopOffset + vertexY * cellSize - terrainVertexHeightAt(scenario, vertexX, vertexY);
    }

    /**
     * Native support extracted from CGameObject::RefreshMapDerivedState @0046025D.
     */
    static int terrainHeightAtWorldPoint(ScenarioDescriptor scenario, int mapPixelX, int mapPixelY) {
        int tileBaseX = mapPixelX >> 8;
        int tileBaseY = mapPixelY >> 8;
        int horizontalFraction = (mapPixelX >> 3) & 0x1F;
        int verticalFraction = (mapPixelY >> 3) & 0x1F;
        int topLeft = terrainVertexHeightAt(scenario, tileBaseX, tileBaseY);
        int topRight = terrainVertexHeightAt(scenario, tileBaseX + 1, tileBaseY);
        int bottomLeft = terrainVertexHeightAt(scenario, tileBaseX, tileBaseY + 1);
        int bottomRight = terrainVertexHeightAt(scenario, tileBaseX + 1, tileBaseY + 1);
        int top = topLeft + arithmeticDivideBy32((topRight - topLeft) * horizontalFraction);
        int bottom = bottomLeft + arithmeticDivideBy32((bottomRight - bottomLeft) * horizontalFraction);
        return top + arithmeticDivideBy32((bottom - top) * verticalFraction);
    }

    /**
     * Native support extracted from CGameObject::RefreshMapDerivedState @0046025D.
     */
    private static int arithmeticDivideBy32(int value) {
        return (value + ((value >> 31) & 0x1F)) >> 5;
    }

    /**
     * Java support terrain tile lookup for editor preview rendering.
     * not ported.
     */
    static CBmp256 terrainTileBitmap(int tileId, int variant) {
        if (tileId < 0 || tileId >= TerrainGraphics.terrainTileSet.length) {
            return null;
        }
        return TerrainGraphics.terrainTileSet[tileId][variant];
    }

    /**
     * Java support terrain palette lookup for editor preview rendering.
     * not ported.
     */
    static CGamePalette terrainTilePalette(int tileId) {
        if (tileId < 0 || tileId >= TerrainGraphics.terrainTilePalettes.length) {
            return null;
        }
        return TerrainGraphics.terrainTilePalettes[tileId];
    }
}
