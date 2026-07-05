package ua.millfreedom.rom2.mapeditor;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CSprite256;
import ua.millfreedom.rom2.model.GraphicsObjectsFile;
import ua.millfreedom.rom2.model.Screen;
import ua.millfreedom.rom2.model.VObject;
import ua.millfreedom.rom2.model.VObjects;
import ua.millfreedom.rom2.model.palette.CGamePalette;
import ua.millfreedom.rom2.model.render.Renderer;
import ua.millfreedom.rom2.model.render.SwingRenderer;
import ua.millfreedom.rom2.model.world.ScenarioDescriptor;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Objects;

/**
 * Editor-only terrain visual-object sprite painter backed by the existing VObjects sprite assets.
 * not ported.
 */
final class MapEditorObjectSpriteRenderer {
    private static final int TILE_SCREEN_SIZE = 0x20;
    private static final int TERRAIN_DEAD_VISUAL_OBJECT_MASK = 0x2000;
    private static final int TERRAIN_LIGHT_FULLY_BLOCKED_MASK = 0xC000;
    private static final int TERRAIN_BRIGHTNESS_PAGE_SHIFT = 2;
    private static final int OBJECT_RENDER_TILE_MARGIN = 8;

    private SwingRenderer objectRenderer;
    private int objectRendererWidth;
    private int objectRendererHeight;

    /**
     * Java support constructor for editor object sprite previews.
     * not ported.
     */
    MapEditorObjectSpriteRenderer() {
    }

    /**
     * Java support sprite overlay for nonzero scenario object/tree cells in the editor viewport.
     * not ported.
     */
    void drawObjectSprites(
            Graphics2D graphics,
            ScenarioDescriptor scenario,
            int left,
            int top,
            int cellSize,
            int animationTick
    ) {
        if (cellSize != TILE_SCREEN_SIZE) {
            return;
        }

        Rectangle clip = graphics.getClipBounds();
        if (clip == null) {
            clip = new Rectangle(left, top, scenario.mapWidth * cellSize, scenario.mapHeight * cellSize);
        }
        if (clip.width <= 0 || clip.height <= 0) {
            return;
        }

        int startX = Math.max(0, Math.floorDiv(clip.x - left, cellSize) - OBJECT_RENDER_TILE_MARGIN);
        int startY = Math.max(0, Math.floorDiv(clip.y - top, cellSize) - OBJECT_RENDER_TILE_MARGIN);
        int endX = Math.min(
                scenario.mapWidth,
                Math.floorDiv(clip.x + clip.width - left + cellSize - 1, cellSize) + OBJECT_RENDER_TILE_MARGIN
        );
        int endY = Math.min(
                scenario.mapHeight,
                Math.floorDiv(clip.y + clip.height - top + cellSize - 1, cellSize) + OBJECT_RENDER_TILE_MARGIN
        );
        if (startX >= endX || startY >= endY) {
            return;
        }

        ensureObjectRenderer(clip.width, clip.height);
        objectRenderer.clearSurface();
        Renderer previousRenderer = Globals.renderer;
        Globals.renderer = objectRenderer;
        try {
            int brightness = objectBrightnessPage(scenario);
            for (int y = startY; y < endY; y++) {
                for (int x = endX - 1; x >= startX; x--) {
                    drawObjectCell(
                            scenario,
                            x,
                            y,
                            left,
                            top,
                            clip.x,
                            clip.y,
                            brightness,
                            animationTick
                    );
                }
            }
        } finally {
            Globals.renderer = previousRenderer;
        }
        objectRenderer.presentSurface(clip.width, clip.height);
        objectRenderer.drawTo(graphics, clip.x, clip.y, clip.width, clip.height);
    }

    /**
     * Java support visible-object Swing render surface reuse.
     * not ported.
     */
    private void ensureObjectRenderer(int width, int height) {
        if (objectRenderer != null && objectRendererWidth == width && objectRendererHeight == height) {
            return;
        }
        objectRenderer = new SwingRenderer(Screen.createBgraSurface(width, height));
        objectRendererWidth = width;
        objectRendererHeight = height;
    }

    /**
     * Java support extracted from MapVisualObject::RenderFrame @00406F43 terrain visual-object draw order.
     * not ported.
     */
    void drawObjectCell(
            ScenarioDescriptor scenario,
            int tileX,
            int tileY,
            int left,
            int top,
            int surfaceLeft,
            int surfaceTop,
            int brightness,
            int animationTick
    ) {
        int encodedVisualObjectId = Byte.toUnsignedInt(scenario.sec3Objects[tileY * scenario.mapWidth + tileX]);
        if (encodedVisualObjectId == 0) {
            return;
        }

        int visualObjectId = encodedVisualObjectId - 1;
        VObject visualObject = Objects.requireNonNull(VObjects.getVObject(visualObjectId), "Missing VObject id " + visualObjectId);
        TerrainVisualSprite visualSprite = resolveTerrainVisualSprite(
                scenario,
                visualObject,
                tileX,
                tileY,
                combinedTileOccupancyMask(scenario, tileX, tileY),
                animationTick
        );
        GraphicsObjectsFile file = Objects.requireNonNull(
                VObjects.getGraphicsObjectsFile(visualSprite.fileId),
                "Missing GraphicsObjectsFile id " + visualSprite.fileId
        );
        CSprite256 spriteA = file.getSpriteA();
        CSprite256 spriteB = file.getSpriteB();
        int frame = visualSprite.frame;
        int tileAverageHeight = tileAverageHeightAt(scenario, tileX, tileY);
        int shadowSlope = terrainShadowSlope(scenario);
        int shadowSkew = shadowSkewForSprite(scenario, spriteA, frame, visualObject);
        if (Globals.gamePreferences.shadows != 0) {
            drawObjectSpriteShadow(
                    tileX,
                    tileY,
                    left,
                    top,
                    surfaceLeft,
                    surfaceTop,
                    tileAverageHeight,
                    visualObject,
                    spriteA,
                    frame,
                    shadowSlope,
                    shadowSkew
            );
            if (Globals.gamePreferences.smoothing != 0) {
                drawObjectSpriteSmoothingShadow(
                        tileX,
                        tileY,
                        left,
                        top,
                        surfaceLeft,
                        surfaceTop,
                        tileAverageHeight,
                        visualObject,
                        spriteB,
                        frame,
                        shadowSlope,
                        shadowSkew
                );
            }
        }
        drawObjectSpriteMain(
                tileX,
                tileY,
                left,
                top,
                surfaceLeft,
                surfaceTop,
                tileAverageHeight,
                visualObject,
                spriteA,
                frame,
                clampBrightness(brightness, spriteA.palette),
                spriteA.palette
        );
        if (Globals.gamePreferences.smoothing != 0) {
            drawObjectSpriteSmoothing(
                    tileX,
                    tileY,
                    left,
                    top,
                    surfaceLeft,
                    surfaceTop,
                    tileAverageHeight,
                    visualObject,
                    spriteB,
                    frame,
                    clampBrightness(brightness, spriteA.palette),
                    spriteA.palette
            );
        }
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 visual-object frame routing.
     */
    private TerrainVisualSprite resolveTerrainVisualSprite(
            ScenarioDescriptor scenario,
            VObject visualObject,
            int tileX,
            int tileY,
            int occupancyMask,
            int animationTick
    ) {
        int tileWord = tileWordAt(scenario, tileX, tileY);
        int fileId = visualObject.fileId;
        int frame = visualObject.spriteIndex;
        boolean hasLiveObjectOrNoDeadVariant =
                (tileWord & TERRAIN_DEAD_VISUAL_OBJECT_MASK) == 0 || visualObject.deadObjectId == -1;
        if (visualObject.animationFrameCount != 0
                && occupancyMask == TERRAIN_LIGHT_FULLY_BLOCKED_MASK
                && hasLiveObjectOrNoDeadVariant) {
            int phase = Math.floorMod(animationTick + tileX + tileX * tileY, visualObject.animationFrameCount);
            frame += visualObject.animationFrames.get(phase);
        } else if (!hasLiveObjectOrNoDeadVariant) {
            VObject deadObject = Objects.requireNonNull(
                    VObjects.getVObject(visualObject.deadObjectId),
                    "Missing dead VObject id " + visualObject.deadObjectId
            );
            fileId = deadObject.fileId;
            frame = 0;
        }
        if (Globals.gamePreferences.animation == 0) {
            frame = 0;
        }
        return new TerrainVisualSprite(fileId, frame);
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 terrain visual-object shadow draw.
     */
    private void drawObjectSpriteShadow(
            int tileX,
            int tileY,
            int left,
            int top,
            int surfaceLeft,
            int surfaceTop,
            int tileAverageHeight,
            VObject visualObject,
            CSprite256 sprite,
            int frame,
            int shadowSlope,
            int shadowSkew
    ) {
        Point drawPoint = objectSpriteShadowDrawPoint(
                tileX,
                tileY,
                left,
                top,
                surfaceLeft,
                surfaceTop,
                tileAverageHeight,
                visualObject,
                sprite,
                shadowSkew
        );
        sprite.drawWithRenderEffect(drawPoint.x, drawPoint.y, frame, Globals.lighting.shadowLength, shadowSlope, false);
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 terrain visual-object smoothing shadow draw.
     */
    private void drawObjectSpriteSmoothingShadow(
            int tileX,
            int tileY,
            int left,
            int top,
            int surfaceLeft,
            int surfaceTop,
            int tileAverageHeight,
            VObject visualObject,
            CSprite256 sprite,
            int frame,
            int shadowSlope,
            int shadowSkew
    ) {
        Point drawPoint = objectSpriteShadowDrawPoint(
                tileX,
                tileY,
                left,
                top,
                surfaceLeft,
                surfaceTop,
                tileAverageHeight,
                visualObject,
                sprite,
                shadowSkew
        );
        sprite.drawWithRenderEffect(drawPoint.x, drawPoint.y, frame, Globals.lighting.lightHeight, shadowSlope, false);
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 terrain visual-object main sprite draw.
     */
    private void drawObjectSpriteMain(
            int tileX,
            int tileY,
            int left,
            int top,
            int surfaceLeft,
            int surfaceTop,
            int tileAverageHeight,
            VObject visualObject,
            CSprite256 sprite,
            int frame,
            int brightness,
            CGamePalette palette
    ) {
        Point drawPoint = objectSpriteDrawPoint(
                tileX,
                tileY,
                left,
                top,
                surfaceLeft,
                surfaceTop,
                tileAverageHeight,
                visualObject,
                sprite,
                frame,
                0
        );
        sprite.drawWithPalette(drawPoint.x, drawPoint.y, frame, brightness, palette, false);
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 terrain visual-object smoothing draw.
     */
    private void drawObjectSpriteSmoothing(
            int tileX,
            int tileY,
            int left,
            int top,
            int surfaceLeft,
            int surfaceTop,
            int tileAverageHeight,
            VObject visualObject,
            CSprite256 sprite,
            int frame,
            int brightness,
            CGamePalette palette
    ) {
        Point drawPoint = objectSpriteDrawPoint(
                tileX,
                tileY,
                left,
                top,
                surfaceLeft,
                surfaceTop,
                tileAverageHeight,
                visualObject,
                sprite,
                frame,
                0
        );
        sprite.drawFrameClippedY(drawPoint.x, drawPoint.y, frame, brightness, palette, false);
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 terrain visual-object shadow coordinate math.
     * Native anchors terrain visual-object shadows with sprite frame 0 while drawing the current animated frame.
     */
    private Point objectSpriteShadowDrawPoint(
            int tileX,
            int tileY,
            int left,
            int top,
            int surfaceLeft,
            int surfaceTop,
            int tileAverageHeight,
            VObject visualObject,
            CSprite256 sprite,
            int shadowSkew
    ) {
        return objectSpriteDrawPoint(
                tileX,
                tileY,
                left,
                top,
                surfaceLeft,
                surfaceTop,
                tileAverageHeight,
                visualObject,
                sprite,
                0,
                shadowSkew
        );
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 terrain visual-object coordinate math.
     */
    private Point objectSpriteDrawPoint(
            int tileX,
            int tileY,
            int left,
            int top,
            int surfaceLeft,
            int surfaceTop,
            int tileAverageHeight,
            VObject visualObject,
            CSprite256 sprite,
            int frame,
            int shadowSkew
    ) {
        int centerX = (visualObject.centerX - visualObject.width / 2) + sprite.xSizeOf(frame) / 2;
        int centerY = (visualObject.centerY - visualObject.height / 2) + sprite.ySizeOf(frame) / 2;
        return new Point(
                left + tileX * TILE_SCREEN_SIZE + TILE_SCREEN_SIZE / 2 - centerX - shadowSkew - surfaceLeft,
                top + tileY * TILE_SCREEN_SIZE + TILE_SCREEN_SIZE / 2 - centerY - tileAverageHeight - surfaceTop
        );
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 visual-object shadow offset.
     */
    private int shadowSkewForSprite(ScenarioDescriptor scenario, CSprite256 sprite, int frame, VObject visualObject) {
        double sunSlope = Math.tan(shadowAngle(scenario));
        return (int) (sunSlope * ((sprite.ySizeOf(frame) / 2 + visualObject.height / 2) - visualObject.centerY));
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 visual-object shadow slope.
     */
    private int terrainShadowSlope(ScenarioDescriptor scenario) {
        return (int) (Math.tan(shadowAngle(scenario)) * 65536.0);
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
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 four-corner tile occupancy reads.
     */
    private int combinedTileOccupancyMask(ScenarioDescriptor scenario, int tileX, int tileY) {
        return (tileWordAt(scenario, tileX, tileY)
                | tileWordAt(scenario, tileX + 1, tileY)
                | tileWordAt(scenario, tileX, tileY + 1)
                | tileWordAt(scenario, tileX + 1, tileY + 1))
                & TERRAIN_LIGHT_FULLY_BLOCKED_MASK;
    }

    /**
     * Native support extracted from MapVisualObject::RebuildTileHeightRenderGrids @004065B1.
     */
    private int tileAverageHeightAt(ScenarioDescriptor scenario, int tileX, int tileY) {
        int baseX = tileX - 1;
        int baseY = tileY - 1;
        int sum = signedHeightAt(scenario, baseX, baseY)
                + signedHeightAt(scenario, baseX + 1, baseY)
                + signedHeightAt(scenario, baseX, baseY + 1)
                + signedHeightAt(scenario, baseX + 1, baseY + 1);
        return arithmeticDivideByFour(sum);
    }

    /**
     * Native support extracted from MapDescriptor::GetTilesWxH @0041E8B0 flat WORD reads.
     */
    private int tileWordAt(ScenarioDescriptor scenario, int tileX, int tileY) {
        if (tileX < 0 || tileY < 0 || tileX >= scenario.mapWidth || tileY >= scenario.mapHeight) {
            return 0;
        }
        return scenario.sec1Tiles[tileY * scenario.mapWidth + tileX] & 0xFFFF;
    }

    /**
     * Native support extracted from MapVisualObject::RebuildTileHeightRenderGrids @004065B1.
     */
    private int signedHeightAt(ScenarioDescriptor scenario, int tileX, int tileY) {
        if (tileX < 0 || tileY < 0 || tileX >= scenario.mapWidth || tileY >= scenario.mapHeight) {
            return 0;
        }
        return scenario.sec2Heights[tileY * scenario.mapWidth + tileX];
    }

    /**
     * Native support extracted from MapVisualObject::RebuildTileHeightRenderGrids @004065B1.
     */
    private static int arithmeticDivideByFour(int value) {
        return (value + ((value >> 31) & 3)) >> 2;
    }

    /**
     * Java support brightness-page selection matching the editor terrain preview convention.
     * not ported.
     */
    static int objectBrightnessPage(ScenarioDescriptor scenario) {
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
     * Java record for the terrain visual-object sprite routing result.
     * not ported.
     */
    private record TerrainVisualSprite(int fileId, int frame) {
    }
}
