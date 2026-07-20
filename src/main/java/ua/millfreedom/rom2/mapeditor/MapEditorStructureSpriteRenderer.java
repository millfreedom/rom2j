package ua.millfreedom.rom2.mapeditor;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.Screen;
import ua.millfreedom.rom2.model.StructureDef;
import ua.millfreedom.rom2.model.Structures;
import ua.millfreedom.rom2.model.enums.BuildingId;
import ua.millfreedom.rom2.model.palette.CGamePalette;
import ua.millfreedom.rom2.model.render.Renderer;
import ua.millfreedom.rom2.model.render.SwingRenderer;
import ua.millfreedom.rom2.model.world.ScenarioDescriptor;
import ua.millfreedom.rom2.model.world.scenario.BuildingDTO;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Editor-only BUILDINGS-section sprite painter backed by the existing Structures sprite assets.
 * not ported.
 */
final class MapEditorStructureSpriteRenderer {
    private static final int TILE_SCREEN_SIZE = 0x20;
    private static final int TERRAIN_BRIGHTNESS_PAGE_SHIFT = 2;

    private SwingRenderer structureRenderer;
    private int structureRendererWidth;
    private int structureRendererHeight;

    /**
     * Java support constructor for editor structure sprite previews.
     * not ported.
     */
    MapEditorStructureSpriteRenderer() {
    }

    /**
     * Java support sprite overlay for saved BUILDINGS-section records in the editor viewport.
     * not ported.
     */
    void drawStructureSprites(
            Graphics2D graphics,
            ScenarioDescriptor scenario,
            int left,
            int top,
            int cellSize,
            int animationTick
    ) {
        if (cellSize != TILE_SCREEN_SIZE || scenario.sec4Buildings.isEmpty()) {
            return;
        }

        Rectangle clip = graphics.getClipBounds();
        if (clip == null) {
            clip = new Rectangle(left, top, scenario.mapWidth * cellSize, scenario.mapHeight * cellSize);
        }
        if (clip.width <= 0 || clip.height <= 0) {
            return;
        }

        ensureStructureRenderer(clip.width, clip.height);
        structureRenderer.clearSurface();
        Renderer previousRenderer = Globals.renderer;
        Globals.renderer = structureRenderer;
        try {
            int brightness = structureBrightnessPage(scenario);
            List<StructureTile> tiles = collectStructureTiles(scenario);
            if (Globals.gamePreferences.shadows != 0) {
                for (StructureTile tile : tiles) {
                    drawStructureTileShadow(tile, scenario, left, top, clip.x, clip.y, animationTick);
                }
            }
            for (StructureTile tile : tiles) {
                drawStructureTileMain(tile, left, top, clip.x, clip.y, brightness, animationTick);
            }
        } finally {
            Globals.renderer = previousRenderer;
        }
        structureRenderer.presentSurface(clip.width, clip.height);
        structureRenderer.drawTo(graphics, clip.x, clip.y, clip.width, clip.height);
    }

    /**
     * Java support visible-structure Swing render surface reuse.
     * not ported.
     */
    private void ensureStructureRenderer(int width, int height) {
        if (structureRenderer != null && structureRendererWidth == width && structureRendererHeight == height) {
            return;
        }
        structureRenderer = new SwingRenderer(Screen.createArgbSurface(width, height));
        structureRendererWidth = width;
        structureRendererHeight = height;
    }

    /**
     * Java support extracted from MapVisualObject object-layer structure dispatch.
     * not ported.
     */
    List<StructureTile> collectStructureTiles(ScenarioDescriptor scenario) {
        List<StructureTile> tiles = new ArrayList<>();
        for (BuildingDTO building : scenario.sec4Buildings) {
            StructureDef def = Objects.requireNonNull(
                    Structures.getStructureDef(building.typeID),
                    "Missing StructureDef for id " + building.typeID
            );
            StructureMetrics metrics = structureMetrics(scenario, building, def);
            for (int localY = 0; localY < metrics.tileHeight(); localY++) {
                for (int localX = 0; localX < metrics.tileWidth(); localX++) {
                    tiles.add(new StructureTile(
                            building,
                            def,
                            metrics,
                            building.x + localX,
                            building.y + localY,
                            localX,
                            localY
                    ));
                }
            }
        }
        tiles.sort(Comparator.comparingInt(StructureTile::worldY)
                .thenComparing((left, right) -> Integer.compare(right.worldX(), left.worldX())));
        return tiles;
    }

    /**
     * Java support extracted from CGameObject::RefreshMapDerivedState @0046025D structure footprint setup.
     * not ported.
     */
    private StructureMetrics structureMetrics(ScenarioDescriptor scenario, BuildingDTO building, StructureDef def) {
        int tileWidth = structureTileWidth(building, def);
        int tileHeight = structureTileHeight(building, def);
        int fullHeight = isBridge(building) ? tileHeight : def.fullHeight;
        return new StructureMetrics(
                tileWidth,
                tileHeight,
                fullHeight,
                interpolateTerrainHeight(scenario, building.x, building.y, tileWidth, tileHeight)
        );
    }

    /**
     * Java support extracted from CBridge::GetTileWidth @0046DE00 and CStructure::GetTileWidth @004620FC.
     * not ported.
     */
    private int structureTileWidth(BuildingDTO building, StructureDef def) {
        if (!isBridge(building)) {
            return def.tileWidth;
        }
        return building.sizeX == 0 ? def.tileWidth : building.sizeX;
    }

    /**
     * Java support extracted from CBridge::GetTileHeight @0046DE20 and CStructure::GetTileHeight @0046211D.
     * not ported.
     */
    private int structureTileHeight(BuildingDTO building, StructureDef def) {
        if (!isBridge(building)) {
            return def.tileHeight;
        }
        return building.sizeY == 0 ? def.tileHeight : building.sizeY;
    }

    /**
     * Java support extracted from CStructure::DrawShadow @00461890.
     * not ported.
     */
    void drawStructureTileShadow(
            StructureTile tile,
            ScenarioDescriptor scenario,
            int left,
            int top,
            int surfaceLeft,
            int surfaceTop,
            int animationTick
    ) {
        if (isBridge(tile.building()) || tile.def().hasSentinelShadowY()) {
            return;
        }

        int shadowSlope = terrainShadowSlope(scenario);
        double shadowAngle = Math.tan(shadowAngle(scenario));
        int firstStructureRow = tile.localY();
        int rowEnd = firstStructureRow + tile.metrics().fullHeight() - tile.metrics().tileHeight();
        int rowStart = firstStructureRow == 0 ? 0 : rowEnd;
        int drawY = top + tile.worldY() * TILE_SCREEN_SIZE - tile.metrics().terrainHeightOffset() - surfaceTop;
        for (int row = rowEnd; rowStart <= row; row--) {
            int frameGridIndex = tile.localX() + row * tile.def().tileWidth;
            int frameIndex = resolveStructureFrameIndex(
                    tile.def(),
                    frameGridIndex,
                    animationTick,
                    tile.building().hp < 1
            );
            int shadowXOffset = (int) (shadowAngle * ((tile.def().fullHeight - row) * 32.0 - tile.def().shadowY));
            int drawX = left + tile.worldX() * TILE_SCREEN_SIZE + shadowXOffset - surfaceLeft;
            tile.def().getSpriteMain().drawWithRenderEffect(
                    drawX,
                    drawY,
                    frameIndex,
                    Globals.lighting.shadowLength,
                    shadowSlope,
                    false
            );
            if (Globals.gamePreferences.smoothing != 0) {
                tile.def().getSpriteSecondary().drawWithRenderEffect(
                        drawX,
                        drawY,
                        frameIndex,
                        Globals.lighting.lightHeight,
                        shadowSlope,
                        false
                );
            }
            drawY -= TILE_SCREEN_SIZE;
        }
    }

    /**
     * Java support extracted from CStructure::Draw @00461649 and bridge draw overrides @00462654/@0046283C.
     * not ported.
     */
    void drawStructureTileMain(
            StructureTile tile,
            int left,
            int top,
            int surfaceLeft,
            int surfaceTop,
            int brightness,
            int animationTick
    ) {
        if (tile.building().typeID == BuildingId.VERTICAL_WOODEN_BRIDGE.id) {
            drawBridgeTileMain(
                    tile,
                    left,
                    top,
                    surfaceLeft,
                    surfaceTop,
                    verticalBridgeFrameIndex(tile),
                    brightness
            );
            return;
        }
        if (tile.building().typeID == BuildingId.HORISONTAL_WOODEN_BRIDGE.id) {
            drawBridgeTileMain(
                    tile,
                    left,
                    top,
                    surfaceLeft,
                    surfaceTop,
                    horizontalBridgeFrameIndex(tile, animationTick),
                    brightness
            );
            return;
        }
        drawGenericStructureTileMain(tile, left, top, surfaceLeft, surfaceTop, brightness, animationTick);
    }

    /**
     * Java support extracted from CStructure::Draw @00461649.
     * not ported.
     */
    private void drawGenericStructureTileMain(
            StructureTile tile,
            int left,
            int top,
            int surfaceLeft,
            int surfaceTop,
            int brightness,
            int animationTick
    ) {
        CGamePalette mainPalette = tile.def().getSpriteMain().palette;
        int firstStructureRow = tile.localY();
        int rowEnd = firstStructureRow + tile.metrics().fullHeight() - tile.metrics().tileHeight();
        int rowStart = firstStructureRow == 0 ? 0 : rowEnd;
        int drawY = top + tile.worldY() * TILE_SCREEN_SIZE - tile.metrics().terrainHeightOffset() - surfaceTop;
        for (int row = rowEnd; rowStart <= row; row--) {
            int frameGridIndex = tile.localX() + row * tile.def().tileWidth;
            int frameIndex = resolveStructureFrameIndex(
                    tile.def(),
                    frameGridIndex,
                    animationTick,
                    tile.building().hp < 1 && tile.def().indestructible == 0
            );
            int drawX = left + tile.worldX() * TILE_SCREEN_SIZE - surfaceLeft;
            int palettePage = clampBrightness(brightness, mainPalette);
            tile.def().getSpriteMain().draw(drawX, drawY, frameIndex, palettePage, false);
            if (Globals.gamePreferences.smoothing != 0) {
                tile.def().getSpriteSecondary().drawFrameClippedY(
                        drawX,
                        drawY,
                        frameIndex,
                        palettePage,
                        mainPalette,
                        false
                );
            }
            drawY -= TILE_SCREEN_SIZE;
        }
    }

    /**
     * Java support extracted from CVerticalWoodenBridge::Draw @00462654 and CHorisontalWoodenBridge::Draw @0046283C.
     * not ported.
     */
    private void drawBridgeTileMain(
            StructureTile tile,
            int left,
            int top,
            int surfaceLeft,
            int surfaceTop,
            int frameIndex,
            int brightness
    ) {
        CGamePalette mainPalette = tile.def().getSpriteMain().palette;
        int drawX = left + tile.worldX() * TILE_SCREEN_SIZE - surfaceLeft;
        int drawY = top + tile.worldY() * TILE_SCREEN_SIZE - tile.metrics().terrainHeightOffset() - surfaceTop;
        int palettePage = clampBrightness(brightness, mainPalette);
        tile.def().getSpriteMain().draw(drawX, drawY, frameIndex, palettePage, false);
        if (Globals.gamePreferences.smoothing != 0) {
            tile.def().getSpriteSecondary().drawFrameClippedY(
                    drawX,
                    drawY,
                    frameIndex,
                    palettePage,
                    mainPalette,
                    false
            );
        }
    }

    /**
     * Native support extracted from CStructure::Draw @00461649 and CStructure::DrawShadow @00461890.
     */
    private int resolveStructureFrameIndex(
            StructureDef def,
            int frameGridIndex,
            int animationTick,
            boolean useDestroyedFrame
    ) {
        if (useDestroyedFrame) {
            return frameGridIndex + def.getSpriteMain().frameCount - def.tileWidth * def.fullHeight;
        }

        int animationFrame = 0;
        if (def.animationDataCount != 0) {
            int phase = Math.floorMod(animationTick, def.animationDataCount);
            animationFrame = def.animationData.get(phase);
        }
        if (Globals.gamePreferences.animation == 0 || animationFrame == 0) {
            return frameGridIndex;
        }

        if (def.animationMask.charAt(frameGridIndex) == '-') {
            return frameGridIndex;
        }

        int frameIndex = def.tileWidth * def.fullHeight + (animationFrame - 1) * def.animationMaskSolidCount;
        for (int i = 0; i < frameGridIndex; i++) {
            if (def.animationMask.charAt(i) != '-') {
                frameIndex++;
            }
        }
        return frameIndex;
    }

    /**
     * Native support extracted from CVerticalWoodenBridge::Draw @00462654.
     */
    private int verticalBridgeFrameIndex(StructureTile tile) {
        int lastX = tile.metrics().tileWidth() - 1;
        int lastY = tile.metrics().tileHeight() - 1;
        if (tile.localX() == 0 && tile.localY() == 0) {
            return 0;
        }
        if (tile.localX() == lastX && tile.localY() == 0) {
            return 2;
        }
        if (tile.localY() == 0) {
            return 1;
        }
        if (tile.localX() == 0 && tile.localY() == lastY) {
            return 6;
        }
        if (tile.localX() == lastX && tile.localY() == lastY) {
            return 8;
        }
        if (tile.localY() == lastY) {
            return 7;
        }
        if (tile.localX() == 0) {
            return 3;
        }
        if (tile.localX() == lastX) {
            return 5;
        }
        return 4;
    }

    /**
     * Native support extracted from CHorisontalWoodenBridge::Draw @0046283C.
     */
    private int horizontalBridgeFrameIndex(StructureTile tile, int animationTick) {
        int lastX = tile.metrics().tileWidth() - 1;
        int lastY = tile.metrics().tileHeight() - 1;
        if (tile.localX() == 0 && tile.localY() == 0) {
            return 0;
        }
        if (tile.localX() == lastX && tile.localY() == 0) {
            return 3;
        }
        if (tile.localY() == 0) {
            return (tile.localX() & 1) + 1;
        }
        if (tile.localX() == 0 && tile.localY() == lastY) {
            return 7;
        }
        if (tile.localX() == lastX && tile.localY() == lastY) {
            return 0x0D;
        }
        if (tile.localY() == lastY) {
            if ((tile.localX() & 1) == 0) {
                return (animationTick & 0x03) + 8;
            }
            return 0x0C;
        }
        if (tile.localX() == 0) {
            return 4;
        }
        if (tile.localX() == lastX) {
            return 6;
        }
        return 5;
    }

    /**
     * Java support extracted from MapDescriptor::MapDescriptor @004A449C scenario-building type branch.
     * not ported.
     */
    private static boolean isBridge(BuildingDTO building) {
        return building.typeID == BuildingId.VERTICAL_WOODEN_BRIDGE.id
                || building.typeID == BuildingId.HORISONTAL_WOODEN_BRIDGE.id;
    }

    /**
     * Native support extracted from CGameObject::RefreshMapDerivedState @0046025D.
     */
    private int interpolateTerrainHeight(
            ScenarioDescriptor scenario,
            int tileX,
            int tileY,
            int tileWidth,
            int tileHeight
    ) {
        int mapPixelX = tileX * 0x100 + tileWidth * 0x80;
        int mapPixelY = tileY * 0x100 + tileHeight * 0x80;
        int tileBaseX = mapPixelX >> 8;
        int tileBaseY = mapPixelY >> 8;
        int horizontalFraction = (mapPixelX >> 3) & 0x1F;
        int verticalFraction = (mapPixelY >> 3) & 0x1F;
        int topLeft = signedHeightAt(scenario, tileBaseX, tileBaseY);
        int topRight = signedHeightAt(scenario, tileBaseX + 1, tileBaseY);
        int bottomLeft = signedHeightAt(scenario, tileBaseX, tileBaseY + 1);
        int bottomRight = signedHeightAt(scenario, tileBaseX + 1, tileBaseY + 1);
        int top = topLeft + arithmeticDivideBy32((topRight - topLeft) * horizontalFraction);
        int bottom = bottomLeft + arithmeticDivideBy32((bottomRight - bottomLeft) * horizontalFraction);
        return top + arithmeticDivideBy32((bottom - top) * verticalFraction);
    }

    /**
     * Java support signed height lookup matching MapDescriptor::heightAt.
     * not ported.
     */
    private int signedHeightAt(ScenarioDescriptor scenario, int tileX, int tileY) {
        if (tileX < 0 || tileY < 0 || tileX >= scenario.mapWidth || tileY >= scenario.mapHeight) {
            return 0;
        }
        return scenario.sec2Heights[tileY * scenario.mapWidth + tileX];
    }

    /**
     * Native support extracted from CGameObject::RefreshMapDerivedState @0046025D.
     */
    private static int arithmeticDivideBy32(int value) {
        return (value + ((value >> 31) & 0x1F)) >> 5;
    }

    /**
     * Native support extracted from MapDescriptor::GetShadowAngle @004A4429.
     */
    private double shadowAngle(ScenarioDescriptor scenario) {
        if (scenario.sunAngle >= 0.0 || scenario.sunAngle <= -0.05) {
            if (scenario.sunAngle <= 0.0 || scenario.sunAngle >= 0.05) {
                return scenario.sunAngle / 1.5;
            }
            return 0.05;
        }
        return -0.05;
    }

    /**
     * Native support extracted from CGameObject::ResolveShadowSlope for CStructure::DrawShadow @00461890.
     */
    private int terrainShadowSlope(ScenarioDescriptor scenario) {
        return (int) (Math.tan(shadowAngle(scenario)) * 65536.0);
    }

    /**
     * Java support brightness-page selection matching the editor terrain preview convention.
     * not ported.
     */
    static int structureBrightnessPage(ScenarioDescriptor scenario) {
        return Math.max(0, scenario.darkness >> TERRAIN_BRIGHTNESS_PAGE_SHIFT);
    }

    /**
     * Java support clamp for sprite palette page lookups.
     * not ported.
     */
    private static int clampBrightness(int brightness, CGamePalette palette) {
        return Math.min(brightness, palette.paletteData.length - 1);
    }

    /**
     * Java record for structure footprint and terrain-height draw state.
     * not ported.
     */
    record StructureMetrics(int tileWidth, int tileHeight, int fullHeight, int terrainHeightOffset) {
    }

    /**
     * Java record for one structure object-layer tile draw dispatch.
     * not ported.
     */
    record StructureTile(
            BuildingDTO building,
            StructureDef def,
            StructureMetrics metrics,
            int worldX,
            int worldY,
            int localX,
            int localY
    ) {
    }
}
