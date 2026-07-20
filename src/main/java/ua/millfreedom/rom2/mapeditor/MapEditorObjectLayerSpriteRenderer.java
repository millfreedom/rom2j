package ua.millfreedom.rom2.mapeditor;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.Screen;
import ua.millfreedom.rom2.model.render.Renderer;
import ua.millfreedom.rom2.model.render.SwingRenderer;
import ua.millfreedom.rom2.model.world.ScenarioDescriptor;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Editor-only object-layer compositor that keeps saved map sprites in native-style row order.
 * not ported.
 */
final class MapEditorObjectLayerSpriteRenderer {
    private static final int TILE_SCREEN_SIZE = 0x20;
    private static final int RENDER_TILE_MARGIN = 12;

    private final MapEditorObjectSpriteRenderer objectSpriteRenderer = new MapEditorObjectSpriteRenderer();
    private final MapEditorStructureSpriteRenderer structureSpriteRenderer = new MapEditorStructureSpriteRenderer();
    private final MapEditorUnitSpriteRenderer unitSpriteRenderer = new MapEditorUnitSpriteRenderer();
    private final MapEditorSackSpriteRenderer sackSpriteRenderer = new MapEditorSackSpriteRenderer();
    private final MapEditorEffectSpriteRenderer effectSpriteRenderer = new MapEditorEffectSpriteRenderer();

    private SwingRenderer objectLayerRenderer;
    private int objectLayerRendererWidth;
    private int objectLayerRendererHeight;

    /**
     * Java support constructor for the editor object-layer sprite compositor.
     * not ported.
     */
    MapEditorObjectLayerSpriteRenderer() {
    }

    /**
     * Java support object-layer pass modeled after MapVisualObject::RenderFrame @00406F43 row ordering.
     * not ported.
     */
    void drawObjectLayerSprites(
            Graphics2D graphics,
            ScenarioDescriptor scenario,
            int left,
            int top,
            int cellSize,
            int animationTick,
            boolean objectResourcesReady,
            boolean structureResourcesReady,
            boolean unitResourcesReady,
            boolean sackResourcesReady,
            boolean effectResourcesReady
    ) {
        if (cellSize != TILE_SCREEN_SIZE
                || (!objectResourcesReady
                && !structureResourcesReady
                && !unitResourcesReady
                && !sackResourcesReady
                && !effectResourcesReady)) {
            return;
        }

        Rectangle clip = graphics.getClipBounds();
        if (clip == null) {
            clip = new Rectangle(left, top, scenario.mapWidth * cellSize, scenario.mapHeight * cellSize);
        }
        if (clip.width <= 0 || clip.height <= 0) {
            return;
        }

        ensureObjectLayerRenderer(clip.width, clip.height);
        objectLayerRenderer.clearSurface();
        Renderer previousRenderer = Globals.renderer;
        Globals.renderer = objectLayerRenderer;
        try {
            drawObjectLayerSpritesToActiveRenderer(
                    scenario,
                    left,
                    top,
                    clip,
                    animationTick,
                    objectResourcesReady,
                    structureResourcesReady,
                    unitResourcesReady,
                    sackResourcesReady,
                    effectResourcesReady
            );
        } finally {
            Globals.renderer = previousRenderer;
        }
        objectLayerRenderer.presentSurface(clip.width, clip.height);
        objectLayerRenderer.drawTo(graphics, clip.x, clip.y, clip.width, clip.height);
    }

    /**
     * Java support reusable Swing render surface for the editor object-layer pass.
     * not ported.
     */
    private void ensureObjectLayerRenderer(int width, int height) {
        if (objectLayerRenderer != null && objectLayerRendererWidth == width && objectLayerRendererHeight == height) {
            return;
        }
        objectLayerRenderer = new SwingRenderer(Screen.createArgbSurface(width, height));
        objectLayerRendererWidth = width;
        objectLayerRendererHeight = height;
    }

    /**
     * Java support active-renderer body for MapVisualObject::RenderFrame @00406F43 object-layer ordering.
     * not ported.
     */
    private void drawObjectLayerSpritesToActiveRenderer(
            ScenarioDescriptor scenario,
            int left,
            int top,
            Rectangle clip,
            int animationTick,
            boolean objectResourcesReady,
            boolean structureResourcesReady,
            boolean unitResourcesReady,
            boolean sackResourcesReady,
            boolean effectResourcesReady
    ) {
        int startX = Math.max(0, Math.floorDiv(clip.x - left, TILE_SCREEN_SIZE) - RENDER_TILE_MARGIN);
        int startY = Math.max(0, Math.floorDiv(clip.y - top, TILE_SCREEN_SIZE) - RENDER_TILE_MARGIN);
        int endX = Math.min(
                scenario.mapWidth,
                Math.floorDiv(clip.x + clip.width - left + TILE_SCREEN_SIZE - 1, TILE_SCREEN_SIZE)
                        + RENDER_TILE_MARGIN
        );
        int endY = Math.min(
                scenario.mapHeight,
                Math.floorDiv(clip.y + clip.height - top + TILE_SCREEN_SIZE - 1, TILE_SCREEN_SIZE)
                        + RENDER_TILE_MARGIN
        );
        if (startX >= endX || startY >= endY) {
            return;
        }

        int structureBrightness = structureResourcesReady
                ? MapEditorStructureSpriteRenderer.structureBrightnessPage(scenario)
                : 0;
        int unitBrightness = unitResourcesReady ? MapEditorUnitSpriteRenderer.unitBrightnessPage(scenario) : 0;
        int sackBrightness = sackResourcesReady ? MapEditorSackSpriteRenderer.sackBrightnessPage(scenario) : 0;
        int objectBrightness = objectResourcesReady ? MapEditorObjectSpriteRenderer.objectBrightnessPage(scenario) : 0;
        List<MapEditorStructureSpriteRenderer.StructureTile> structureTiles =
                structureResourcesReady ? structureSpriteRenderer.collectStructureTiles(scenario) : List.of();
        Map<Integer, List<MapEditorStructureSpriteRenderer.StructureTile>> nonFlatStructureTilesByCell =
                structureResourcesReady ? collectNonFlatStructureTilesByCell(structureTiles) : Map.of();
        Map<Integer, List<MapEditorUnitSpriteRenderer.PreviewUnit>> groundUnitsByCell =
                unitResourcesReady ? collectUnitsByCell(scenario, left, top, clip, animationTick, false) : Map.of();
        Map<Integer, List<MapEditorUnitSpriteRenderer.PreviewUnit>> airUnitsByCell =
                unitResourcesReady ? collectUnitsByCell(scenario, left, top, clip, animationTick, true) : Map.of();
        Map<Integer, List<MapEditorSackSpriteRenderer.PreviewBackPack>> sacksByCell =
                sackResourcesReady ? collectSacksByCell(scenario, left, top, clip) : Map.of();
        Map<Integer, List<MapEditorEffectSpriteRenderer.PreviewEffect>> effectsByCell =
                effectResourcesReady ? collectEffectsByCell(scenario, left, top, clip, animationTick) : Map.of();

        if (structureResourcesReady && Globals.gamePreferences.shadows != 0) {
            for (MapEditorStructureSpriteRenderer.StructureTile tile : structureTiles) {
                structureSpriteRenderer.drawStructureTileShadow(
                        tile,
                        scenario,
                        left,
                        top,
                        clip.x,
                        clip.y,
                        animationTick
                );
            }
        }

        if (structureResourcesReady) {
            for (MapEditorStructureSpriteRenderer.StructureTile tile : structureTiles) {
                if (isFlatStructureTile(tile)) {
                    structureSpriteRenderer.drawStructureTileMain(
                            tile,
                            left,
                            top,
                            clip.x,
                            clip.y,
                            structureBrightness,
                            animationTick
                    );
                }
            }
        }

        for (int row = startY; row < endY; row++) {
            for (int col = endX - 1; col >= startX; col--) {
                int cellKey = cellKey(col, row);
                if (structureResourcesReady) {
                    for (MapEditorStructureSpriteRenderer.StructureTile tile :
                            nonFlatStructureTilesByCell.getOrDefault(cellKey, List.of())) {
                        structureSpriteRenderer.drawStructureTileMain(
                                tile,
                                left,
                                top,
                                clip.x,
                                clip.y,
                                structureBrightness,
                                animationTick
                        );
                    }
                }
                if (sackResourcesReady) {
                    for (MapEditorSackSpriteRenderer.PreviewBackPack sack :
                            sacksByCell.getOrDefault(cellKey, List.of())) {
                        if (Globals.gamePreferences.shadows != 0) {
                            sack.drawEditorShadow(scenario);
                        }
                        sack.drawEditorMain(sackBrightness);
                    }
                }
                if (unitResourcesReady) {
                    for (MapEditorUnitSpriteRenderer.PreviewUnit unit :
                            groundUnitsByCell.getOrDefault(cellKey, List.of())) {
                        if (Globals.gamePreferences.shadows != 0) {
                            unit.drawEditorShadow(scenario);
                        }
                        unit.drawEditorMain(unitBrightness);
                    }
                }
                if (objectResourcesReady) {
                    objectSpriteRenderer.drawObjectCell(
                            scenario,
                            col,
                            row,
                            left,
                            top,
                            clip.x,
                            clip.y,
                            objectBrightness,
                            animationTick
                    );
                }
            }
        }

        if (unitResourcesReady && Globals.gamePreferences.shadows != 0) {
            drawAirUnitShadows(scenario, airUnitsByCell, startX, startY, endX, endY);
        }
        if (effectResourcesReady) {
            drawTransientEffects(effectsByCell, startX, startY, endX, endY);
        }
        if (unitResourcesReady) {
            drawAirUnitMains(airUnitsByCell, startX, startY, endX, endY, unitBrightness);
        }
    }

    /**
     * Java support grouping saved non-flat structure tiles by their map cell for row-pass lookup.
     * not ported.
     */
    private Map<Integer, List<MapEditorStructureSpriteRenderer.StructureTile>> collectNonFlatStructureTilesByCell(
            List<MapEditorStructureSpriteRenderer.StructureTile> structureTiles
    ) {
        Map<Integer, List<MapEditorStructureSpriteRenderer.StructureTile>> byCell = new HashMap<>();
        for (MapEditorStructureSpriteRenderer.StructureTile tile : structureTiles) {
            if (isFlatStructureTile(tile)) {
                continue;
            }
            byCell.computeIfAbsent(cellKey(tile.worldX(), tile.worldY()), key -> new ArrayList<>()).add(tile);
        }
        return byCell;
    }

    /**
     * Java support grouping saved units by their map cell for row-pass lookup.
     * not ported.
     */
    private Map<Integer, List<MapEditorUnitSpriteRenderer.PreviewUnit>> collectUnitsByCell(
            ScenarioDescriptor scenario,
            int left,
            int top,
            Rectangle clip,
            int animationTick,
            boolean airLayer
    ) {
        Map<Integer, List<MapEditorUnitSpriteRenderer.PreviewUnit>> byCell = new HashMap<>();
        for (MapEditorUnitSpriteRenderer.PreviewUnit unit : unitSpriteRenderer.collectPreviewUnits(
                scenario,
                left,
                top,
                clip.x,
                clip.y,
                animationTick
        )) {
            if (unit.isAirLayer() != airLayer) {
                continue;
            }
            byCell.computeIfAbsent(cellKey(unit.worldTileX(), unit.worldTileY()), key -> new ArrayList<>()).add(unit);
        }
        return byCell;
    }

    /**
     * Java support late air-object shadow pass matching MapVisualObject::RenderFrame @00406F43 airObjectLayer order.
     * not ported.
     */
    private void drawAirUnitShadows(
            ScenarioDescriptor scenario,
            Map<Integer, List<MapEditorUnitSpriteRenderer.PreviewUnit>> airUnitsByCell,
            int startX,
            int startY,
            int endX,
            int endY
    ) {
        for (int row = startY; row < endY; row++) {
            for (int col = endX - 1; col >= startX; col--) {
                for (MapEditorUnitSpriteRenderer.PreviewUnit unit :
                        airUnitsByCell.getOrDefault(cellKey(col, row), List.of())) {
                    unit.drawEditorShadow(scenario);
                }
            }
        }
    }

    /**
     * Java support late air-object main pass matching MapVisualObject::RenderFrame @00406F43 airObjectLayer order.
     * not ported.
     */
    private void drawAirUnitMains(
            Map<Integer, List<MapEditorUnitSpriteRenderer.PreviewUnit>> airUnitsByCell,
            int startX,
            int startY,
            int endX,
            int endY,
            int brightness
    ) {
        for (int row = startY; row < endY; row++) {
            for (int col = endX - 1; col >= startX; col--) {
                for (MapEditorUnitSpriteRenderer.PreviewUnit unit :
                        airUnitsByCell.getOrDefault(cellKey(col, row), List.of())) {
                    unit.drawEditorMain(brightness);
                }
            }
        }
    }

    /**
     * Java support transient-object pass matching MapVisualObject::RenderFrame @00406F43 ordering.
     * not ported.
     */
    private void drawTransientEffects(
            Map<Integer, List<MapEditorEffectSpriteRenderer.PreviewEffect>> effectsByCell,
            int startX,
            int startY,
            int endX,
            int endY
    ) {
        for (int row = startY; row < endY; row++) {
            for (int col = endX - 1; col >= startX; col--) {
                for (MapEditorEffectSpriteRenderer.PreviewEffect effect :
                        effectsByCell.getOrDefault(cellKey(col, row), List.of())) {
                    effect.drawEditorMain();
                }
            }
        }
    }

    /**
     * Java support grouping saved ground backpacks by their map cell for row-pass lookup.
     * not ported.
     */
    private Map<Integer, List<MapEditorSackSpriteRenderer.PreviewBackPack>> collectSacksByCell(
            ScenarioDescriptor scenario,
            int left,
            int top,
            Rectangle clip
    ) {
        Map<Integer, List<MapEditorSackSpriteRenderer.PreviewBackPack>> byCell = new HashMap<>();
        for (MapEditorSackSpriteRenderer.PreviewBackPack sack : sackSpriteRenderer.collectPreviewBackpacks(
                scenario,
                left,
                top,
                clip.x,
                clip.y
        )) {
            byCell.computeIfAbsent(cellKey(sack.worldTileX(), sack.worldTileY()), key -> new ArrayList<>()).add(sack);
        }
        return byCell;
    }

    /**
     * Java support grouping saved transient spell/trap effects by their animated projectile cell for row-pass lookup.
     * not ported.
     */
    private Map<Integer, List<MapEditorEffectSpriteRenderer.PreviewEffect>> collectEffectsByCell(
            ScenarioDescriptor scenario,
            int left,
            int top,
            Rectangle clip,
            int animationTick
    ) {
        return effectSpriteRenderer.collectTransientEffectsByCell(scenario, left, top, clip, animationTick);
    }

    /**
     * Java support packed map-cell key for object-layer row-pass grouping.
     * not ported.
     */
    private static int cellKey(int tileX, int tileY) {
        return (tileY << 16) ^ (tileX & 0xFFFF);
    }

    /**
     * Java support flat-structure gate matching MapVisualObject::RenderFrame @00406F43 StructureDef.flat checks.
     * not ported.
     */
    private static boolean isFlatStructureTile(MapEditorStructureSpriteRenderer.StructureTile tile) {
        return tile.def().flat != 0;
    }
}
