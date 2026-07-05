package ua.millfreedom.rom2.mapeditor;

import ua.millfreedom.rom2.model.CUnitInfo;
import ua.millfreedom.rom2.model.StructureDef;
import ua.millfreedom.rom2.model.Structures;
import ua.millfreedom.rom2.model.UnitTypes;
import ua.millfreedom.rom2.model.enums.BuildingId;
import ua.millfreedom.rom2.model.world.ScenarioDescriptor;
import ua.millfreedom.rom2.model.world.scenario.BuildingDTO;
import ua.millfreedom.rom2.model.world.scenario.EffectDTO;
import ua.millfreedom.rom2.model.world.scenario.InnDescriptor;
import ua.millfreedom.rom2.model.world.scenario.Instant;
import ua.millfreedom.rom2.model.world.scenario.MusicDTO;
import ua.millfreedom.rom2.model.world.scenario.PostDescriptor;
import ua.millfreedom.rom2.model.world.scenario.ShopDescriptor;
import ua.millfreedom.rom2.model.world.scenario.UnitDTO;
import ua.millfreedom.rom2.model.world.scenario.WorldSack;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Path2D;

/**
 * Editor-owned overlay for saved map entities on the scroll viewport and minimap.
 * not ported.
 */
final class MapEditorEntityOverlayRenderer {
    private static final Color OBJECT_COLOR = new Color(0, 82, 255, 102);
    private static final Color BUILDING_FILL = new Color(89, 255, 0, 77);
    private static final Color BUILDING_OUTLINE = new Color(255, 0, 0, 128);
    private static final Color UNIT_FILL = new Color(215, 62, 88, 230);
    private static final Color UNIT_OUTLINE = new Color(255, 238, 238, 230);
    private static final Color SACK_FILL = new Color(240, 220, 74, 220);
    private static final Color EFFECT_FILL = new Color(70, 210, 235, 220);
    private static final Color LOGIC_FILL = new Color(180, 115, 255, 230);
    private static final Color MUSIC_OUTLINE = new Color(80, 210, 255, 145);
    private static final Color SELECTION_SHADOW = new Color(15, 15, 15, 230);
    private static final Color SELECTION_OUTLINE = new Color(255, 245, 55, 245);
    private static final Color LABEL_COLOR = new Color(245, 245, 245, 230);
    private static final int UNIT_COORDINATE_SHIFT = 8;
    private static final int TILE_COORDINATE_SCALE = 0x100;
    private static final int TILE_WORLD_CENTER = 0x80;

    /**
     * Java support private constructor for static editor overlay helpers.
     * not ported.
     */
    private MapEditorEntityOverlayRenderer() {
    }

    /**
     * Java support saved-entity overlay for the scrollable map viewport.
     * not ported.
     */
    static void drawViewportOverlay(
            Graphics2D graphics,
            ScenarioDescriptor scenario,
            int cellSize,
            int viewportTopOffset,
            Rectangle clip
    ) {
        drawViewportObjectMarkers(graphics, scenario, cellSize, viewportTopOffset, clip);
        drawViewportMusicAreas(graphics, scenario, cellSize, viewportTopOffset);
        drawViewportBuildings(graphics, scenario, cellSize, viewportTopOffset, clip);
        drawViewportSacks(graphics, scenario, cellSize, viewportTopOffset, clip);
        drawViewportEffects(graphics, scenario, cellSize, viewportTopOffset, clip);
        drawViewportDropLocationInstants(graphics, scenario, cellSize, viewportTopOffset, clip);
        drawViewportUnits(graphics, scenario, cellSize, viewportTopOffset, clip);
    }

    /**
     * Java support selected saved-entity highlight for the scrollable map viewport.
     * not ported.
     */
    static void drawViewportSelection(
            Graphics2D graphics,
            ScenarioDescriptor scenario,
            MapEditorEntitySelection selection,
            int cellSize,
            int viewportTopOffset,
            Rectangle clip
    ) {
        drawViewportSelection(graphics, scenario, selection, cellSize, viewportTopOffset, clip, null);
    }

    /**
     * Java support tick-aware selected saved-entity highlight for animated editor preview sprites.
     * not ported.
     */
    static void drawViewportSelection(
            Graphics2D graphics,
            ScenarioDescriptor scenario,
            MapEditorEntitySelection selection,
            int cellSize,
            int viewportTopOffset,
            Rectangle clip,
            Integer animationTick
    ) {
        if (selection != null && selection.kind == MapEditorEntitySelection.Kind.MUSIC) {
            Rectangle bounds = viewportSelectionBounds(scenario, selection, cellSize, viewportTopOffset, animationTick);
            if (bounds == null || !bounds.intersects(clip)) {
                return;
            }
            drawSelectedOval(graphics, bounds);
            return;
        }

        Path2D path = viewportSelectionPath(scenario, selection, cellSize, viewportTopOffset, animationTick);
        if (path == null || !path.getBounds().intersects(clip)) {
            return;
        }
        drawSelectedPath(graphics, path);
    }

    /**
     * Java support saved-entity overlay for the editor minimap.
     * not ported.
     */
    static void drawMinimapOverlay(Graphics2D graphics, ScenarioDescriptor scenario, int[] bounds) {
        drawMinimapObjectMarkers(graphics, scenario, bounds);
        drawMinimapMusicAreas(graphics, scenario, bounds);
        for (BuildingDTO building : scenario.sec4Buildings) {
            Rectangle footprint = buildingFootprint(scenario, building);
            if (footprint != null) {
                drawMiniRectMarker(graphics, scenario, bounds, footprint, 4, BUILDING_FILL);
            }
        }
        for (WorldSack sack : scenario.sect8Sacks) {
            drawMiniRectMarker(graphics, scenario, bounds, shiftedTile(sack.x), shiftedTile(sack.y), 3, SACK_FILL);
        }
        for (EffectDTO effect : scenario.sect9Effects) {
            drawMiniRectMarker(graphics, scenario, bounds, effect.x, effect.y, 3, EFFECT_FILL);
        }
        for (Instant instant : scenario.sect7Instants) {
            Rectangle dropLocation = dropLocationInstantTileBounds(scenario, instant);
            if (dropLocation != null) {
                drawMiniRectMarker(graphics, scenario, bounds, dropLocation.x, dropLocation.y, 3, LOGIC_FILL);
            }
        }
        for (UnitDTO unit : scenario.sec6Units) {
            drawMiniCircleMarker(graphics, scenario, bounds, unit, 4, UNIT_FILL);
        }
    }

    /**
     * Java support selected saved-entity highlight for the editor minimap.
     * not ported.
     */
    static void drawMinimapSelection(
            Graphics2D graphics,
            ScenarioDescriptor scenario,
            int[] bounds,
            MapEditorEntitySelection selection
    ) {
        drawMinimapSelection(graphics, scenario, bounds, selection, null);
    }

    /**
     * Java support selected saved-entity highlight for the editor minimap with animated EFFECTS projection.
     * not ported.
     */
    static void drawMinimapSelection(
            Graphics2D graphics,
            ScenarioDescriptor scenario,
            int[] bounds,
            MapEditorEntitySelection selection,
            Integer animationTick
    ) {
        Rectangle selectionBounds = minimapSelectionBounds(scenario, bounds, selection, animationTick);
        if (selectionBounds == null) {
            return;
        }
        graphics.setColor(SELECTION_SHADOW);
        graphics.drawRect(selectionBounds.x - 2, selectionBounds.y - 2, selectionBounds.width + 4, selectionBounds.height + 4);
        graphics.setColor(SELECTION_OUTLINE);
        graphics.drawRect(selectionBounds.x - 1, selectionBounds.y - 1, selectionBounds.width + 2, selectionBounds.height + 2);
        graphics.drawRect(selectionBounds.x, selectionBounds.y, selectionBounds.width, selectionBounds.height);
    }

    /**
     * Java support tile-coordinate hit-test for saved entities rendered on the scrollable viewport.
     * not ported.
     */
    static MapEditorEntitySelection hitTestTile(ScenarioDescriptor scenario, int tileX, int tileY) {
        return hitTestTile(scenario, tileX, tileY, null);
    }

    /**
     * Java support tick-aware tile-coordinate hit-test for saved entities rendered on the scrollable viewport.
     * not ported.
     */
    static MapEditorEntitySelection hitTestTile(
            ScenarioDescriptor scenario,
            int tileX,
            int tileY,
            Integer animationTick
    ) {
        if (!tileInBounds(scenario, tileX, tileY)) {
            return null;
        }

        MapEditorEntitySelection unitSelection = hitTestUnits(scenario, tileX, tileY);
        if (unitSelection != null) {
            return unitSelection;
        }
        MapEditorEntitySelection dropLocationSelection = hitTestDropLocationInstants(scenario, tileX, tileY);
        if (dropLocationSelection != null) {
            return dropLocationSelection;
        }
        MapEditorEntitySelection animatedEffectSelection = hitTestAnimatedEffects(
                scenario,
                tileX,
                tileY,
                animationTick
        );
        if (animatedEffectSelection != null) {
            return animatedEffectSelection;
        }
        MapEditorEntitySelection effectSelection = hitTestEffects(scenario, tileX, tileY);
        if (effectSelection != null) {
            return effectSelection;
        }
        MapEditorEntitySelection sackSelection = hitTestSacks(scenario, tileX, tileY);
        if (sackSelection != null) {
            return sackSelection;
        }
        MapEditorEntitySelection buildingSelection = hitTestBuildings(scenario, tileX, tileY);
        if (buildingSelection != null) {
            return buildingSelection;
        }
        MapEditorEntitySelection objectSelection = hitTestObjects(scenario, tileX, tileY);
        if (objectSelection != null) {
            return objectSelection;
        }
        return hitTestMusicAreas(scenario, tileX, tileY);
    }

    /**
     * Java support selected saved-entity viewport pixel bounds.
     * not ported.
     */
    static Rectangle viewportSelectionBounds(
            ScenarioDescriptor scenario,
            MapEditorEntitySelection selection,
            int cellSize,
            int viewportTopOffset
    ) {
        return viewportSelectionBounds(scenario, selection, cellSize, viewportTopOffset, null);
    }

    /**
     * Java support tick-aware selected saved-entity viewport pixel bounds.
     * not ported.
     */
    static Rectangle viewportSelectionBounds(
            ScenarioDescriptor scenario,
            MapEditorEntitySelection selection,
            int cellSize,
            int viewportTopOffset,
            Integer animationTick
    ) {
        Rectangle tileBounds = selectionTileBounds(scenario, selection, animationTick);
        if (tileBounds == null) {
            return null;
        }
        if (selection.kind == MapEditorEntitySelection.Kind.MUSIC) {
            return viewportMusicSelectionBounds(scenario, selection.index, cellSize, viewportTopOffset);
        }
        return projectedTileAreaPath(scenario, tileBounds, cellSize, viewportTopOffset).getBounds();
    }

    /**
     * Java support selected saved-entity viewport path using projected terrain-grid vertices.
     * not ported.
     */
    private static Path2D viewportSelectionPath(
            ScenarioDescriptor scenario,
            MapEditorEntitySelection selection,
            int cellSize,
            int viewportTopOffset,
            Integer animationTick
    ) {
        Rectangle tileBounds = selectionTileBounds(scenario, selection, animationTick);
        if (tileBounds == null || selection.kind == MapEditorEntitySelection.Kind.MUSIC) {
            return null;
        }
        return projectedTileAreaPath(scenario, tileBounds, cellSize, viewportTopOffset);
    }

    /**
     * Java support projected tile-footprint path using the same terrain vertices as the skewed terrain preview.
     * not ported.
     */
    private static Path2D projectedTileAreaPath(
            ScenarioDescriptor scenario,
            Rectangle tileBounds,
            int cellSize,
            int viewportTopOffset
    ) {
        int left = tileBounds.x;
        int top = tileBounds.y;
        int right = tileBounds.x + tileBounds.width;
        int bottom = tileBounds.y + tileBounds.height;
        Path2D path = new Path2D.Double();
        path.moveTo(left * cellSize, projectedTerrainVertexY(scenario, left, top, viewportTopOffset, cellSize));
        for (int x = left + 1; x <= right; x++) {
            path.lineTo(x * cellSize, projectedTerrainVertexY(scenario, x, top, viewportTopOffset, cellSize));
        }
        for (int y = top + 1; y <= bottom; y++) {
            path.lineTo(right * cellSize, projectedTerrainVertexY(scenario, right, y, viewportTopOffset, cellSize));
        }
        for (int x = right - 1; x >= left; x--) {
            path.lineTo(x * cellSize, projectedTerrainVertexY(scenario, x, bottom, viewportTopOffset, cellSize));
        }
        for (int y = bottom - 1; y >= top; y--) {
            path.lineTo(left * cellSize, projectedTerrainVertexY(scenario, left, y, viewportTopOffset, cellSize));
        }
        path.closePath();
        return path;
    }

    /**
     * Java support projected center Y for one tile marker on the skewed terrain preview.
     * not ported.
     */
    private static int projectedTileCenterY(
            ScenarioDescriptor scenario,
            int tileX,
            int tileY,
            int viewportTopOffset,
            int cellSize
    ) {
        int centerWorldX = tileX * TILE_COORDINATE_SCALE + TILE_WORLD_CENTER;
        int centerWorldY = tileY * TILE_COORDINATE_SCALE + TILE_WORLD_CENTER;
        return viewportTopOffset
                + worldToScreenCoordinate(centerWorldY, cellSize)
                - MapEditorTerrainPreviewRenderer.terrainHeightAtWorldPoint(scenario, centerWorldX, centerWorldY);
    }

    /**
     * Java support projected interactive-music selection oval matching the viewport music marker.
     * not ported.
     */
    private static Rectangle viewportMusicSelectionBounds(
            ScenarioDescriptor scenario,
            int index,
            int cellSize,
            int viewportTopOffset
    ) {
        if (index < 0 || index >= scenario.sect12Music.size()) {
            return null;
        }
        MusicDTO music = scenario.sect12Music.get(index);
        if (!tileInBounds(scenario, music.x, music.y)) {
            return null;
        }
        int centerX = music.x * cellSize + cellSize / 2;
        int centerY = projectedTileCenterY(scenario, music.x, music.y, viewportTopOffset, cellSize);
        int radius = Math.max(1, music.radius) * cellSize;
        return new Rectangle(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }

    /**
     * Java support terrain-projection Y lookup shared with MapPreviewPanel overlays.
     * not ported.
     */
    private static int projectedTerrainVertexY(
            ScenarioDescriptor scenario,
            int vertexX,
            int vertexY,
            int viewportTopOffset,
            int cellSize
    ) {
        return MapEditorTerrainPreviewRenderer.projectedTerrainVertexY(
                scenario,
                vertexX,
                vertexY,
                viewportTopOffset,
                cellSize
        );
    }

    /**
     * Java support clip expansion for projected terrain-height variance.
     * not ported.
     */
    private static int projectedVerticalMargin(ScenarioDescriptor scenario) {
        return MapEditorTerrainPreviewRenderer.viewportTopOffset(scenario)
                + MapEditorTerrainPreviewRenderer.viewportBottomPadding(scenario);
    }

    /**
     * Java support object-byte markers for visible viewport cells.
     * not ported.
     */
    private static void drawViewportObjectMarkers(
            Graphics2D graphics,
            ScenarioDescriptor scenario,
            int cellSize,
            int viewportTopOffset,
            Rectangle clip
    ) {
        int startX = Math.max(0, clip.x / cellSize);
        int verticalMargin = projectedVerticalMargin(scenario);
        int startY = Math.max(0, Math.floorDiv(clip.y - viewportTopOffset - verticalMargin, cellSize));
        int endX = Math.min(scenario.mapWidth, (clip.x + clip.width + cellSize - 1) / cellSize);
        int endY = Math.max(0, Math.min(scenario.mapHeight,
                (clip.y + clip.height - viewportTopOffset + verticalMargin + cellSize - 1) / cellSize));
        int markerSize = Math.max(4, cellSize / 5);
        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                int index = y * scenario.mapWidth + x;
                if (Byte.toUnsignedInt(scenario.sec3Objects[index]) != 0) {
                    int centerX = x * cellSize + cellSize / 2;
                    int centerY = projectedTileCenterY(scenario, x, y, viewportTopOffset, cellSize);
                    drawViewportDiamond(graphics, centerX, centerY, markerSize, OBJECT_COLOR, clip);
                }
            }
        }
    }

    /**
     * Java support interactive-music area markers for the map viewport.
     * not ported.
     */
    private static void drawViewportMusicAreas(
            Graphics2D graphics,
            ScenarioDescriptor scenario,
            int cellSize,
            int viewportTopOffset
    ) {
        graphics.setColor(MUSIC_OUTLINE);
        for (MusicDTO music : scenario.sect12Music) {
            if (tileInBounds(scenario, music.x, music.y)) {
                int centerX = music.x * cellSize + cellSize / 2;
                int centerY = projectedTileCenterY(scenario, music.x, music.y, viewportTopOffset, cellSize);
                int radius = Math.max(1, music.radius) * cellSize;
                graphics.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
                drawViewportLabel(graphics, "M", centerX + 4, centerY - 4);
            }
        }
    }

    /**
     * Java support building markers for the map viewport.
     * not ported.
     */
    private static void drawViewportBuildings(
            Graphics2D graphics,
            ScenarioDescriptor scenario,
            int cellSize,
            int viewportTopOffset,
            Rectangle clip
    ) {
        for (BuildingDTO building : scenario.sec4Buildings) {
            Rectangle footprint = buildingFootprint(scenario, building);
            if (footprint == null) {
                continue;
            }
            Path2D footprintPath = projectedTileAreaPath(scenario, footprint, cellSize, viewportTopOffset);
            Rectangle bounds = footprintPath.getBounds();
            if (bounds.intersects(clip)) {
                graphics.setColor(BUILDING_FILL);
                graphics.fill(footprintPath);
                graphics.setColor(BUILDING_OUTLINE);
                graphics.draw(footprintPath);
                drawViewportLabel(graphics, Integer.toString(building.buildingID), bounds.x + 3, bounds.y + 12);
            }
        }
    }

    /**
     * Java support sack markers for the map viewport.
     * not ported.
     */
    private static void drawViewportSacks(
            Graphics2D graphics,
            ScenarioDescriptor scenario,
            int cellSize,
            int viewportTopOffset,
            Rectangle clip
    ) {
        for (WorldSack sack : scenario.sect8Sacks) {
            int x = shiftedTile(sack.x);
            int y = shiftedTile(sack.y);
            if (tileInBounds(scenario, x, y)) {
                int centerX = x * cellSize + cellSize / 2;
                int centerY = projectedTileCenterY(scenario, x, y, viewportTopOffset, cellSize);
                drawViewportDiamond(graphics, centerX, centerY, Math.max(5, cellSize / 5), SACK_FILL, clip);
            }
        }
    }

    /**
     * Java support effect/trap markers for the map viewport.
     * not ported.
     */
    private static void drawViewportEffects(
            Graphics2D graphics,
            ScenarioDescriptor scenario,
            int cellSize,
            int viewportTopOffset,
            Rectangle clip
    ) {
        for (EffectDTO effect : scenario.sect9Effects) {
            if (tileInBounds(scenario, effect.x, effect.y)) {
                int centerX = effect.x * cellSize + cellSize / 2;
                int centerY = projectedTileCenterY(scenario, effect.x, effect.y, viewportTopOffset, cellSize);
                drawViewportDiamond(graphics, centerX, centerY, Math.max(5, cellSize / 5), EFFECT_FILL, clip);
            }
        }
    }

    /**
     * Java support Drop Location instant markers for the map viewport.
     * not ported.
     */
    private static void drawViewportDropLocationInstants(
            Graphics2D graphics,
            ScenarioDescriptor scenario,
            int cellSize,
            int viewportTopOffset,
            Rectangle clip
    ) {
        for (Instant instant : scenario.sect7Instants) {
            Rectangle dropLocation = dropLocationInstantTileBounds(scenario, instant);
            if (dropLocation != null) {
                int centerX = dropLocation.x * cellSize + cellSize / 2;
                int centerY = projectedTileCenterY(scenario, dropLocation.x, dropLocation.y, viewportTopOffset, cellSize);
                int radius = Math.max(5, cellSize / 5);
                if (!new Rectangle(centerX - radius, centerY - radius, radius * 2, radius * 2).intersects(clip)) {
                    continue;
                }
                drawViewportDiamond(graphics, centerX, centerY, radius, LOGIC_FILL, clip);
                drawViewportLabel(graphics, "L", centerX + radius + 2, centerY - 2);
            }
        }
    }

    /**
     * Java support unit markers for the map viewport.
     * not ported.
     */
    private static void drawViewportUnits(
            Graphics2D graphics,
            ScenarioDescriptor scenario,
            int cellSize,
            int viewportTopOffset,
            Rectangle clip
    ) {
        int radius = Math.max(5, cellSize / 5);
        for (UnitDTO unit : scenario.sec6Units) {
            Rectangle footprint = unitFootprint(scenario, unit);
            if (footprint == null) {
                continue;
            }
            int tileSize = unitTileSize(unit.typeID);
            int centerWorldX = unitCenterWorldCoordinate(unit.x, tileSize);
            int centerWorldY = unitCenterWorldCoordinate(unit.y, tileSize);
            int centerX = worldToScreenCoordinate(centerWorldX, cellSize);
            int centerY = viewportTopOffset
                    + worldToScreenCoordinate(centerWorldY, cellSize)
                    - MapEditorTerrainPreviewRenderer.terrainHeightAtWorldPoint(scenario, centerWorldX, centerWorldY);
            if (new Rectangle(centerX - radius, centerY - radius, radius * 2, radius * 2).intersects(clip)) {
                graphics.setColor(UNIT_FILL);
                graphics.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
                graphics.setColor(UNIT_OUTLINE);
                graphics.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
                drawViewportLabel(
                        graphics,
                        MapEditorUnitDisplay.nativeUnitId(unit) + "/" + unit.groupID,
                        centerX + radius + 2,
                        centerY - 2
                );
            }
        }
    }

    /**
     * Java support minimap music radius markers.
     * not ported.
     */
    private static void drawMinimapMusicAreas(Graphics2D graphics, ScenarioDescriptor scenario, int[] bounds) {
        graphics.setColor(MUSIC_OUTLINE);
        for (MusicDTO music : scenario.sect12Music) {
            if (tileInBounds(scenario, music.x, music.y)) {
                int centerX = miniX(scenario, bounds, music.x);
                int centerY = miniY(scenario, bounds, music.y);
                int radiusX = (int) ((long) Math.max(1, music.radius) * bounds[2] / scenario.mapWidth);
                int radiusY = (int) ((long) Math.max(1, music.radius) * bounds[3] / scenario.mapHeight);
                graphics.drawOval(centerX - radiusX, centerY - radiusY, radiusX * 2, radiusY * 2);
            }
        }
    }

    /**
     * Java support object-byte markers for the editor minimap.
     * not ported.
     */
    private static void drawMinimapObjectMarkers(Graphics2D graphics, ScenarioDescriptor scenario, int[] bounds) {
        for (int y = 0; y < scenario.mapHeight; y++) {
            for (int x = 0; x < scenario.mapWidth; x++) {
                int index = y * scenario.mapWidth + x;
                if (Byte.toUnsignedInt(scenario.sec3Objects[index]) != 0) {
                    drawMiniRectMarker(graphics, scenario, bounds, x, y, 2, OBJECT_COLOR);
                }
            }
        }
    }

    /**
     * Java support unit marker hit testing by tile coordinate.
     * not ported.
     */
    private static MapEditorEntitySelection hitTestUnits(ScenarioDescriptor scenario, int tileX, int tileY) {
        for (int i = 0; i < scenario.sec6Units.size(); i++) {
            UnitDTO unit = scenario.sec6Units.get(i);
            Rectangle footprint = unitFootprint(scenario, unit);
            if (footprint != null && containsTile(footprint, tileX, tileY)) {
                return new MapEditorEntitySelection(MapEditorEntitySelection.Kind.UNIT, i);
            }
        }
        return null;
    }

    /**
     * Java support animated transient-effect sprite hit testing by current drawn tile.
     * not ported.
     */
    private static MapEditorEntitySelection hitTestAnimatedEffects(
            ScenarioDescriptor scenario,
            int tileX,
            int tileY,
            Integer animationTick
    ) {
        if (animationTick == null) {
            return null;
        }
        for (int i = 0; i < scenario.sect9Effects.size(); i++) {
            EffectDTO effect = scenario.sect9Effects.get(i);
            Point currentTile = MapEditorEffectSpriteRenderer.currentTransientEffectTile(effect, i, animationTick);
            if (currentTile != null && currentTile.x == tileX && currentTile.y == tileY) {
                return new MapEditorEntitySelection(MapEditorEntitySelection.Kind.EFFECT, i);
            }
        }
        return null;
    }

    /**
     * Java support effect marker hit testing by tile coordinate.
     * not ported.
     */
    private static MapEditorEntitySelection hitTestEffects(ScenarioDescriptor scenario, int tileX, int tileY) {
        for (int i = 0; i < scenario.sect9Effects.size(); i++) {
            EffectDTO effect = scenario.sect9Effects.get(i);
            if (effect.x == tileX && effect.y == tileY) {
                return new MapEditorEntitySelection(MapEditorEntitySelection.Kind.EFFECT, i);
            }
        }
        return null;
    }

    /**
     * Java support Drop Location instant marker hit testing by tile coordinate.
     * not ported.
     */
    private static MapEditorEntitySelection hitTestDropLocationInstants(ScenarioDescriptor scenario, int tileX, int tileY) {
        for (int i = 0; i < scenario.sect7Instants.size(); i++) {
            Rectangle dropLocation = dropLocationInstantTileBounds(scenario, scenario.sect7Instants.get(i));
            if (dropLocation != null && containsTile(dropLocation, tileX, tileY)) {
                return new MapEditorEntitySelection(MapEditorEntitySelection.Kind.DROP_LOCATION_INSTANT, i);
            }
        }
        return null;
    }

    /**
     * Java support sack marker hit testing by tile coordinate.
     * not ported.
     */
    private static MapEditorEntitySelection hitTestSacks(ScenarioDescriptor scenario, int tileX, int tileY) {
        for (int i = 0; i < scenario.sect8Sacks.size(); i++) {
            WorldSack sack = scenario.sect8Sacks.get(i);
            if (shiftedTile(sack.x) == tileX && shiftedTile(sack.y) == tileY) {
                return new MapEditorEntitySelection(MapEditorEntitySelection.Kind.SACK, i);
            }
        }
        return null;
    }

    /**
     * Java support building footprint hit testing by tile coordinate.
     * not ported.
     */
    private static MapEditorEntitySelection hitTestBuildings(ScenarioDescriptor scenario, int tileX, int tileY) {
        for (int i = 0; i < scenario.sec4Buildings.size(); i++) {
            BuildingDTO building = scenario.sec4Buildings.get(i);
            Rectangle footprint = buildingFootprint(scenario, building);
            if (footprint != null && containsTile(footprint, tileX, tileY)) {
                MapEditorEntitySelection descriptorSelection = linkedDescriptorSelection(scenario, building);
                return descriptorSelection == null
                        ? new MapEditorEntitySelection(MapEditorEntitySelection.Kind.BUILDING, i)
                        : descriptorSelection;
            }
        }
        return null;
    }

    /**
     * Java support object-byte marker hit testing by tile coordinate.
     * not ported.
     */
    private static MapEditorEntitySelection hitTestObjects(ScenarioDescriptor scenario, int tileX, int tileY) {
        int cellIndex = tileY * scenario.mapWidth + tileX;
        if (Byte.toUnsignedInt(scenario.sec3Objects[cellIndex]) == 0) {
            return null;
        }
        return new MapEditorEntitySelection(MapEditorEntitySelection.Kind.OBJECT, cellIndex);
    }

    /**
     * Java support interactive-music marker hit testing by tile coordinate.
     * not ported.
     */
    private static MapEditorEntitySelection hitTestMusicAreas(ScenarioDescriptor scenario, int tileX, int tileY) {
        for (int i = 0; i < scenario.sect12Music.size(); i++) {
            MusicDTO music = scenario.sect12Music.get(i);
            int radius = Math.max(1, music.radius);
            int dx = tileX - music.x;
            int dy = tileY - music.y;
            if (dx * dx + dy * dy <= radius * radius) {
                return new MapEditorEntitySelection(MapEditorEntitySelection.Kind.MUSIC, i);
            }
        }
        return null;
    }

    /**
     * Java support diamond marker drawing for compact viewport entities.
     * not ported.
     */
    private static void drawViewportDiamond(
            Graphics2D graphics,
            int centerX,
            int centerY,
            int radius,
            Color color,
            Rectangle clip
    ) {
        if (!new Rectangle(centerX - radius, centerY - radius, radius * 2, radius * 2).intersects(clip)) {
            return;
        }
        graphics.setColor(color);
        graphics.fillPolygon(
                new int[]{centerX, centerX + radius, centerX, centerX - radius},
                new int[]{centerY - radius, centerY, centerY + radius, centerY},
                4
        );
    }

    /**
     * Java support label drawing for large viewport markers.
     * not ported.
     */
    private static void drawViewportLabel(Graphics2D graphics, String text, int x, int y) {
        graphics.setColor(LABEL_COLOR);
        graphics.drawString(text, x, y);
    }

    /**
     * Java support minimap square marker drawing.
     * not ported.
     */
    private static void drawMiniRectMarker(
            Graphics2D graphics,
            ScenarioDescriptor scenario,
            int[] bounds,
            int tileX,
            int tileY,
            int size,
            Color color
    ) {
        if (!tileInBounds(scenario, tileX, tileY)) {
            return;
        }
        int x = miniX(scenario, bounds, tileX);
        int y = miniY(scenario, bounds, tileY);
        graphics.setColor(color);
        graphics.fillRect(x - size / 2, y - size / 2, size, size);
    }

    /**
     * Java support minimap square marker drawing at a tile footprint center.
     * not ported.
     */
    private static void drawMiniRectMarker(
            Graphics2D graphics,
            ScenarioDescriptor scenario,
            int[] bounds,
            Rectangle tileBounds,
            int size,
            Color color
    ) {
        int x = miniCenterX(scenario, bounds, tileBounds);
        int y = miniCenterY(scenario, bounds, tileBounds);
        graphics.setColor(color);
        graphics.fillRect(x - size / 2, y - size / 2, size, size);
    }

    /**
     * Java support minimap circle marker drawing at the native unit footprint center.
     * not ported.
     */
    private static void drawMiniCircleMarker(
            Graphics2D graphics,
            ScenarioDescriptor scenario,
            int[] bounds,
            UnitDTO unit,
            int size,
            Color color
    ) {
        Rectangle footprint = unitFootprint(scenario, unit);
        if (footprint == null) {
            return;
        }
        int x = miniUnitCenterX(scenario, bounds, unit.x, unitTileSize(unit.typeID));
        int y = miniUnitCenterY(scenario, bounds, unit.y, unitTileSize(unit.typeID));
        graphics.setColor(color);
        graphics.fillOval(x - size / 2, y - size / 2, size, size);
    }

    /**
     * Java support selected entity tile bounds for viewport and minimap highlights.
     * not ported.
     */
    private static Rectangle selectionTileBounds(ScenarioDescriptor scenario, MapEditorEntitySelection selection) {
        return selectionTileBounds(scenario, selection, null);
    }

    /**
     * Java support selected entity tile bounds with current animated EFFECTS sprite position.
     * not ported.
     */
    private static Rectangle selectionTileBounds(
            ScenarioDescriptor scenario,
            MapEditorEntitySelection selection,
            Integer animationTick
    ) {
        if (selection == null || selection.index < 0) {
            return null;
        }
        return switch (selection.kind) {
            case OBJECT -> objectSelectionTileBounds(scenario, selection.index);
            case BUILDING -> buildingSelectionTileBounds(scenario, selection.index);
            case SHOP_DESCRIPTOR -> shopDescriptorSelectionTileBounds(scenario, selection.index);
            case INN_DESCRIPTOR -> innDescriptorSelectionTileBounds(scenario, selection.index);
            case POST_DESCRIPTOR -> postDescriptorSelectionTileBounds(scenario, selection.index);
            case DROP_LOCATION_INSTANT -> dropLocationInstantSelectionTileBounds(scenario, selection.index);
            case UNIT -> unitSelectionTileBounds(scenario, selection.index);
            case SACK -> sackSelectionTileBounds(scenario, selection.index);
            case EFFECT -> effectSelectionTileBounds(scenario, selection.index, animationTick);
            case MUSIC -> musicSelectionTileBounds(scenario, selection.index);
        };
    }

    /**
     * Java support selected building footprint tile bounds.
     * not ported.
     */
    private static Rectangle buildingSelectionTileBounds(ScenarioDescriptor scenario, int index) {
        if (index >= scenario.sec4Buildings.size()) {
            return null;
        }
        return buildingFootprint(scenario, scenario.sec4Buildings.get(index));
    }

    /**
     * Java support selected shop-descriptor tile bounds through its linked scenario building id.
     * not ported.
     */
    private static Rectangle shopDescriptorSelectionTileBounds(ScenarioDescriptor scenario, int index) {
        if (index >= scenario.sect11ShopDescriptors.size()) {
            return null;
        }
        return descriptorBuildingFootprint(scenario, scenario.sect11ShopDescriptors.get(index).id);
    }

    /**
     * Java support selected inn-descriptor tile bounds through its linked scenario building id.
     * not ported.
     */
    private static Rectangle innDescriptorSelectionTileBounds(ScenarioDescriptor scenario, int index) {
        if (index >= scenario.sect11InnDescriptors.size()) {
            return null;
        }
        return descriptorBuildingFootprint(scenario, scenario.sect11InnDescriptors.get(index).id);
    }

    /**
     * Java support selected pointer/post-descriptor tile bounds through its linked scenario building id.
     * not ported.
     */
    private static Rectangle postDescriptorSelectionTileBounds(ScenarioDescriptor scenario, int index) {
        if (index >= scenario.sect11PostDescriptors.size()) {
            return null;
        }
        return descriptorBuildingFootprint(scenario, scenario.sect11PostDescriptors.get(index).id);
    }

    /**
     * Java support selected Drop Location instant tile bounds through its serialized X/Y arguments.
     * not ported.
     */
    private static Rectangle dropLocationInstantSelectionTileBounds(ScenarioDescriptor scenario, int index) {
        if (index >= scenario.sect7Instants.size()) {
            return null;
        }
        return dropLocationInstantTileBounds(scenario, scenario.sect7Instants.get(index));
    }

    /**
     * Java support selected object-cell tile bounds.
     * not ported.
     */
    private static Rectangle objectSelectionTileBounds(ScenarioDescriptor scenario, int cellIndex) {
        if (cellIndex >= scenario.mapWidth * scenario.mapHeight) {
            return null;
        }
        if (Byte.toUnsignedInt(scenario.sec3Objects[cellIndex]) == 0) {
            return null;
        }
        return singleTileBounds(scenario, cellIndex % scenario.mapWidth, cellIndex / scenario.mapWidth);
    }

    /**
     * Java support selected unit tile bounds.
     * not ported.
     */
    private static Rectangle unitSelectionTileBounds(ScenarioDescriptor scenario, int index) {
        if (index >= scenario.sec6Units.size()) {
            return null;
        }
        return unitFootprint(scenario, scenario.sec6Units.get(index));
    }

    /**
     * Java support selected sack tile bounds.
     * not ported.
     */
    private static Rectangle sackSelectionTileBounds(ScenarioDescriptor scenario, int index) {
        if (index >= scenario.sect8Sacks.size()) {
            return null;
        }
        WorldSack sack = scenario.sect8Sacks.get(index);
        return singleTileBounds(scenario, shiftedTile(sack.x), shiftedTile(sack.y));
    }

    /**
     * Java support selected effect/trap tile bounds.
     * not ported.
     */
    private static Rectangle effectSelectionTileBounds(ScenarioDescriptor scenario, int index) {
        return effectSelectionTileBounds(scenario, index, null);
    }

    /**
     * Java support selected effect/trap tile bounds with current animated projectile position.
     * not ported.
     */
    private static Rectangle effectSelectionTileBounds(ScenarioDescriptor scenario, int index, Integer animationTick) {
        if (index >= scenario.sect9Effects.size()) {
            return null;
        }
        EffectDTO effect = scenario.sect9Effects.get(index);
        if (animationTick != null) {
            Point currentTile = MapEditorEffectSpriteRenderer.currentTransientEffectTile(effect, index, animationTick);
            if (currentTile != null) {
                return singleTileBounds(scenario, currentTile.x, currentTile.y);
            }
        }
        return singleTileBounds(scenario, effect.x, effect.y);
    }

    /**
     * Java support selected music-area tile bounds.
     * not ported.
     */
    private static Rectangle musicSelectionTileBounds(ScenarioDescriptor scenario, int index) {
        if (index >= scenario.sect12Music.size()) {
            return null;
        }
        MusicDTO music = scenario.sect12Music.get(index);
        if (!tileInBounds(scenario, music.x, music.y)) {
            return null;
        }
        int radius = Math.max(1, music.radius);
        int left = clamp(music.x - radius, 0, scenario.mapWidth - 1);
        int top = clamp(music.y - radius, 0, scenario.mapHeight - 1);
        int right = clamp(music.x + radius, 0, scenario.mapWidth - 1);
        int bottom = clamp(music.y + radius, 0, scenario.mapHeight - 1);
        return new Rectangle(left, top, right - left + 1, bottom - top + 1);
    }

    /**
     * Java support Drop Location instant tile extraction matching ScenarioMapLoader's byte-masked branch.
     * not ported.
     */
    private static Rectangle dropLocationInstantTileBounds(ScenarioDescriptor scenario, Instant instant) {
        if (!isDropLocationInstant(instant)) {
            return null;
        }
        int x = instant.arguments[0].value & 0xFF;
        int y = instant.arguments[1].value & 0xFF;
        return singleTileBounds(scenario, x, y);
    }

    /**
     * Java support Drop Location instant type check matching ScenarioMapLoader's special instant branch.
     * not ported.
     */
    private static boolean isDropLocationInstant(Instant instant) {
        return instant.typeId == MapEditorDocument.MISSION_ENTRY_DROP_INSTANT_TYPE;
    }

    /**
     * Java support selected single-cell entity tile bounds.
     * not ported.
     */
    private static Rectangle singleTileBounds(ScenarioDescriptor scenario, int tileX, int tileY) {
        if (!tileInBounds(scenario, tileX, tileY)) {
            return null;
        }
        return new Rectangle(tileX, tileY, 1, 1);
    }

    /**
     * Java support selected entity minimap pixel bounds.
     * not ported.
     */
    private static Rectangle minimapSelectionBounds(
            ScenarioDescriptor scenario,
            int[] bounds,
            MapEditorEntitySelection selection
    ) {
        return minimapSelectionBounds(scenario, bounds, selection, null);
    }

    /**
     * Java support selected entity minimap pixel bounds with current animated EFFECTS sprite position.
     * not ported.
     */
    private static Rectangle minimapSelectionBounds(
            ScenarioDescriptor scenario,
            int[] bounds,
            MapEditorEntitySelection selection,
            Integer animationTick
    ) {
        Rectangle tileBounds = selectionTileBounds(scenario, selection, animationTick);
        if (tileBounds == null) {
            return null;
        }
        int left = bounds[0] + (int) ((long) tileBounds.x * bounds[2] / scenario.mapWidth);
        int top = bounds[1] + (int) ((long) tileBounds.y * bounds[3] / scenario.mapHeight);
        int right = bounds[0] + (int) ((long) (tileBounds.x + tileBounds.width) * bounds[2] / scenario.mapWidth);
        int bottom = bounds[1] + (int) ((long) (tileBounds.y + tileBounds.height) * bounds[3] / scenario.mapHeight);
        return new Rectangle(left, top, Math.max(4, right - left), Math.max(4, bottom - top));
    }

    /**
     * Java support selected entity projected path highlight drawing.
     * not ported.
     */
    private static void drawSelectedPath(Graphics2D graphics, Path2D path) {
        Stroke oldStroke = graphics.getStroke();
        try {
            graphics.setColor(SELECTION_SHADOW);
            graphics.setStroke(new BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            graphics.draw(path);
            graphics.setColor(SELECTION_OUTLINE);
            graphics.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            graphics.draw(path);
            graphics.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            graphics.draw(path);
        } finally {
            graphics.setStroke(oldStroke);
        }
    }

    /**
     * Java support selected music-area oval highlight drawing.
     * not ported.
     */
    private static void drawSelectedOval(Graphics2D graphics, Rectangle bounds) {
        graphics.setColor(SELECTION_SHADOW);
        graphics.drawOval(bounds.x - 3, bounds.y - 3, bounds.width + 6, bounds.height + 6);
        graphics.setColor(SELECTION_OUTLINE);
        graphics.drawOval(bounds.x - 2, bounds.y - 2, bounds.width + 4, bounds.height + 4);
        graphics.drawOval(bounds.x - 1, bounds.y - 1, bounds.width + 2, bounds.height + 2);
    }

    /**
     * Java support coordinate conversion from raw shifted scenario coordinate to tile coordinate.
     * not ported.
     */
    private static int shiftedTile(int rawCoordinate) {
        return (rawCoordinate >>> UNIT_COORDINATE_SHIFT) & 0xFFFF;
    }

    /**
     * Java support saved building footprint matching the editor structure sprite renderer.
     * not ported.
     */
    private static Rectangle buildingFootprint(ScenarioDescriptor scenario, BuildingDTO building) {
        if (!tileInBounds(scenario, building.x, building.y)) {
            return null;
        }
        return new Rectangle(
                building.x,
                building.y,
                structureTileWidth(building),
                structureTileHeight(building)
        );
    }

    /**
     * Java support descriptor-to-building footprint lookup matching ScenarioMapLoader descriptor id binding.
     * not ported.
     */
    private static Rectangle descriptorBuildingFootprint(ScenarioDescriptor scenario, int buildingId) {
        for (BuildingDTO building : scenario.sec4Buildings) {
            if (building.buildingID == buildingId) {
                return buildingFootprint(scenario, building);
            }
        }
        return null;
    }

    /**
     * Java support descriptor hit-test routing for special structures with property records.
     * Mirrors ScenarioMapLoader's shop/inn/pointer descriptor application by building id.
     * not ported.
     */
    private static MapEditorEntitySelection linkedDescriptorSelection(ScenarioDescriptor scenario, BuildingDTO building) {
        BuildingId buildingId = BuildingId.fromId(building.typeID & 0xFF);
        if (isScenarioShopBuilding(buildingId)) {
            int descriptorIndex = shopDescriptorIndexById(scenario, building.buildingID);
            if (descriptorIndex >= 0) {
                return new MapEditorEntitySelection(MapEditorEntitySelection.Kind.SHOP_DESCRIPTOR, descriptorIndex);
            }
        }
        if (isScenarioInnBuilding(buildingId)) {
            int descriptorIndex = innDescriptorIndexById(scenario, building.buildingID);
            if (descriptorIndex >= 0) {
                return new MapEditorEntitySelection(MapEditorEntitySelection.Kind.INN_DESCRIPTOR, descriptorIndex);
            }
        }
        if (isScenarioPointerBuilding(buildingId)) {
            int descriptorIndex = postDescriptorIndexById(scenario, building.buildingID);
            if (descriptorIndex >= 0) {
                return new MapEditorEntitySelection(MapEditorEntitySelection.Kind.POST_DESCRIPTOR, descriptorIndex);
            }
        }
        return null;
    }

    /**
     * Java support first matching shop descriptor lookup by scenario building id.
     * not ported.
     */
    private static int shopDescriptorIndexById(ScenarioDescriptor scenario, int buildingId) {
        for (int i = 0; i < scenario.sect11ShopDescriptors.size(); i++) {
            ShopDescriptor descriptor = scenario.sect11ShopDescriptors.get(i);
            if (descriptor.id == buildingId) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Java support first matching inn descriptor lookup by scenario building id.
     * not ported.
     */
    private static int innDescriptorIndexById(ScenarioDescriptor scenario, int buildingId) {
        for (int i = 0; i < scenario.sect11InnDescriptors.size(); i++) {
            InnDescriptor descriptor = scenario.sect11InnDescriptors.get(i);
            if (descriptor.id == buildingId) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Java support first matching pointer/post descriptor lookup by scenario building id.
     * not ported.
     */
    private static int postDescriptorIndexById(ScenarioDescriptor scenario, int buildingId) {
        for (int i = 0; i < scenario.sect11PostDescriptors.size(); i++) {
            PostDescriptor descriptor = scenario.sect11PostDescriptors.get(i);
            if (descriptor.id == buildingId) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Java support saved unit footprint matching the editor unit sprite renderer.
     * not ported.
     */
    private static Rectangle unitFootprint(ScenarioDescriptor scenario, UnitDTO unit) {
        int x = shiftedTile(unit.x);
        int y = shiftedTile(unit.y);
        if (!tileInBounds(scenario, x, y)) {
            return null;
        }
        int tileSize = unitTileSize(unit.typeID);
        return new Rectangle(x, y, tileSize, tileSize);
    }

    /**
     * Java support extracted from CBridge::GetTileWidth @0046DE00 and CStructure::GetTileWidth @004620FC.
     * not ported.
     */
    private static int structureTileWidth(BuildingDTO building) {
        StructureDef def = Structures.getStructureDef(building.typeID);
        if (isBridge(building) && building.sizeX != 0) {
            return Math.max(1, building.sizeX);
        }
        if (def != null && def.tileWidth > 0) {
            return def.tileWidth;
        }
        return Math.max(1, building.sizeX);
    }

    /**
     * Java support extracted from CBridge::GetTileHeight @0046DE20 and CStructure::GetTileHeight @0046211D.
     * not ported.
     */
    private static int structureTileHeight(BuildingDTO building) {
        StructureDef def = Structures.getStructureDef(building.typeID);
        if (isBridge(building) && building.sizeY != 0) {
            return Math.max(1, building.sizeY);
        }
        if (def != null && def.tileHeight > 0) {
            return def.tileHeight;
        }
        return Math.max(1, building.sizeY);
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
     * Java support extracted from ScenarioMapLoader::materializeScenarioBuildings @00561422 shop class branch.
     * not ported.
     */
    private static boolean isScenarioShopBuilding(BuildingId buildingId) {
        return buildingId.isBetween(BuildingId.SHOP, BuildingId.SHOP_2)
                || buildingId.isBetween(BuildingId.KAARG_SHOP_1, BuildingId.MULTIPLAYER_KAARG_SHOP_3)
                || buildingId.isBetween(BuildingId.DRUID_SHOP_1, BuildingId.MULTIPLAYER_DRUID_SHOP_3);
    }

    /**
     * Java support extracted from ScenarioMapLoader::materializeScenarioBuildings @00561422 inn class branch.
     * not ported.
     */
    private static boolean isScenarioInnBuilding(BuildingId buildingId) {
        return buildingId.isBetween(BuildingId.INN_1, BuildingId.INN_3)
                || buildingId.isBetween(BuildingId.MULTIPLAYER_INN_1, BuildingId.MULTIPLAYER_INN_3)
                || buildingId.isBetween(BuildingId.KAARG_INN_1, BuildingId.MULTIPLAYER_KAARG_INN_3)
                || buildingId.isBetween(BuildingId.DRUID_INN_1, BuildingId.MULTIPLAYER_DRUID_INN_3);
    }

    /**
     * Java support extracted from ScenarioMapLoader::materializeScenarioBuildings @00561422 pointer class branch.
     * not ported.
     */
    private static boolean isScenarioPointerBuilding(BuildingId buildingId) {
        return buildingId.isBetween(BuildingId.POINTER_1, BuildingId.POINTER_6);
    }

    /**
     * Java support unit tile-size lookup for editor marker alignment.
     * not ported.
     */
    private static int unitTileSize(int unitTypeId) {
        CUnitInfo info = unitTypeId >= 0 && unitTypeId < UnitTypes.UNIT_TYPES_BY_ID.size()
                ? UnitTypes.UNIT_TYPES_BY_ID.get(unitTypeId)
                : null;
        if (info == null || info.m_TileSize < 1) {
            return 1;
        }
        return info.m_TileSize;
    }

    /**
     * Java support native-unit center conversion from raw shifted map coordinate to screen pixels.
     * not ported.
     */
    private static int unitCenterWorldCoordinate(int rawCoordinate, int tileSize) {
        return rawCoordinate - TILE_WORLD_CENTER + tileSize * TILE_WORLD_CENTER;
    }

    /**
     * Java support native world-coordinate to editor screen-coordinate conversion.
     * not ported.
     */
    private static int worldToScreenCoordinate(int rawCoordinate, int cellSize) {
        return (int) ((long) rawCoordinate * cellSize / TILE_COORDINATE_SCALE);
    }

    /**
     * Java support tile-footprint containment for editor hit testing.
     * not ported.
     */
    private static boolean containsTile(Rectangle bounds, int tileX, int tileY) {
        return tileX >= bounds.x
                && tileY >= bounds.y
                && tileX < bounds.x + bounds.width
                && tileY < bounds.y + bounds.height;
    }

    /**
     * Java support tile-bounds guard for editor overlay data.
     * not ported.
     */
    private static boolean tileInBounds(ScenarioDescriptor scenario, int tileX, int tileY) {
        return tileX >= 0 && tileY >= 0 && tileX < scenario.mapWidth && tileY < scenario.mapHeight;
    }

    /**
     * Java support integer clamp helper for selected entity bounds.
     * not ported.
     */
    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Java support map-tile to minimap-X coordinate conversion.
     * not ported.
     */
    private static int miniX(ScenarioDescriptor scenario, int[] bounds, int tileX) {
        return bounds[0] + (int) ((long) tileX * bounds[2] / scenario.mapWidth);
    }

    /**
     * Java support map-tile to minimap-Y coordinate conversion.
     * not ported.
     */
    private static int miniY(ScenarioDescriptor scenario, int[] bounds, int tileY) {
        return bounds[1] + (int) ((long) tileY * bounds[3] / scenario.mapHeight);
    }

    /**
     * Java support tile-footprint center to minimap-X coordinate conversion.
     * not ported.
     */
    private static int miniCenterX(ScenarioDescriptor scenario, int[] bounds, Rectangle tileBounds) {
        long centerNumerator = (long) tileBounds.x * 2L + tileBounds.width;
        return bounds[0] + (int) (centerNumerator * bounds[2] / (2L * scenario.mapWidth));
    }

    /**
     * Java support tile-footprint center to minimap-Y coordinate conversion.
     * not ported.
     */
    private static int miniCenterY(ScenarioDescriptor scenario, int[] bounds, Rectangle tileBounds) {
        long centerNumerator = (long) tileBounds.y * 2L + tileBounds.height;
        return bounds[1] + (int) (centerNumerator * bounds[3] / (2L * scenario.mapHeight));
    }

    /**
     * Java support native-unit center to minimap coordinate conversion.
     * not ported.
     */
    private static int miniUnitCenterX(ScenarioDescriptor scenario, int[] bounds, int rawCoordinate, int tileSize) {
        int centerCoordinate = rawCoordinate - TILE_WORLD_CENTER + tileSize * TILE_WORLD_CENTER;
        return bounds[0] + (int) ((long) centerCoordinate * bounds[2]
                / ((long) scenario.mapWidth * TILE_COORDINATE_SCALE));
    }

    /**
     * Java support native-unit center to minimap coordinate conversion.
     * not ported.
     */
    private static int miniUnitCenterY(ScenarioDescriptor scenario, int[] bounds, int rawCoordinate, int tileSize) {
        int centerCoordinate = rawCoordinate - TILE_WORLD_CENTER + tileSize * TILE_WORLD_CENTER;
        return bounds[1] + (int) ((long) centerCoordinate * bounds[3]
                / ((long) scenario.mapHeight * TILE_COORDINATE_SCALE));
    }
}
