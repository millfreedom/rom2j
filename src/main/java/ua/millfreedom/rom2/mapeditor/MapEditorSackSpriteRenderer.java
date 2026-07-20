package ua.millfreedom.rom2.mapeditor;

import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.gameserver.ScenarioMapLoader;
import ua.millfreedom.rom2.model.Item;
import ua.millfreedom.rom2.model.Screen;
import ua.millfreedom.rom2.model.gameobj.CBackPack;
import ua.millfreedom.rom2.model.palette.CGamePalette;
import ua.millfreedom.rom2.model.render.Renderer;
import ua.millfreedom.rom2.model.render.SwingRenderer;
import ua.millfreedom.rom2.model.world.ScenarioDescriptor;
import ua.millfreedom.rom2.model.world.scenario.WorldSack;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Editor-only SACKS-section backpack sprite painter backed by the existing CBackPack sprite path.
 * not ported.
 */
final class MapEditorSackSpriteRenderer {
    private static final int TILE_SCREEN_SIZE = 0x20;
    private static final int TILE_WORLD_CENTER = 0x80;
    private static final int TERRAIN_BRIGHTNESS_PAGE_SHIFT = 2;

    private SwingRenderer sackRenderer;
    private int sackRendererWidth;
    private int sackRendererHeight;

    /**
     * Java support constructor for editor backpack sprite previews.
     * not ported.
     */
    MapEditorSackSpriteRenderer() {
    }

    /**
     * Java support sprite overlay for saved ground SACKS-section records in the editor viewport.
     * not ported.
     */
    void drawSackSprites(Graphics2D graphics, ScenarioDescriptor scenario, int left, int top, int cellSize) {
        if (cellSize != TILE_SCREEN_SIZE || scenario.sect8Sacks.isEmpty()) {
            return;
        }

        Rectangle clip = graphics.getClipBounds();
        if (clip == null) {
            clip = new Rectangle(left, top, scenario.mapWidth * cellSize, scenario.mapHeight * cellSize);
        }
        if (clip.width <= 0 || clip.height <= 0) {
            return;
        }

        ensureSackRenderer(clip.width, clip.height);
        sackRenderer.clearSurface();
        Renderer previousRenderer = Globals.renderer;
        Globals.renderer = sackRenderer;
        try {
            int brightness = sackBrightnessPage(scenario);
            List<PreviewBackPack> sacks = collectPreviewBackpacks(scenario, left, top, clip.x, clip.y);
            if (Globals.gamePreferences.shadows != 0) {
                for (PreviewBackPack sack : sacks) {
                    sack.drawEditorShadow(scenario);
                }
            }
            for (PreviewBackPack sack : sacks) {
                sack.drawEditorMain(brightness);
            }
        } finally {
            Globals.renderer = previousRenderer;
        }
        sackRenderer.presentSurface(clip.width, clip.height);
        sackRenderer.drawTo(graphics, clip.x, clip.y, clip.width, clip.height);
    }

    /**
     * Java support visible-sack Swing render surface reuse.
     * not ported.
     */
    private void ensureSackRenderer(int width, int height) {
        if (sackRenderer != null && sackRendererWidth == width && sackRendererHeight == height) {
            return;
        }
        sackRenderer = new SwingRenderer(Screen.createArgbSurface(width, height));
        sackRendererWidth = width;
        sackRendererHeight = height;
    }

    /**
     * Java support extracted from MapVisualObject misc-layer backpack dispatch.
     * not ported.
     */
    List<PreviewBackPack> collectPreviewBackpacks(
            ScenarioDescriptor scenario,
            int left,
            int top,
            int surfaceLeft,
            int surfaceTop
    ) {
        List<PreviewBackPack> sacks = new ArrayList<>();
        for (WorldSack sack : scenario.sect8Sacks) {
            if (sack.unitID != 0) {
                continue;
            }
            int tileX = shiftedTile(sack.x);
            int tileY = shiftedTile(sack.y);
            if (!tileInBounds(scenario, tileX, tileY)) {
                continue;
            }
            sacks.add(createPreviewBackpack(scenario, sack, tileX, tileY, left, top, surfaceLeft, surfaceTop));
        }
        sacks.sort(Comparator.comparingInt(PreviewBackPack::worldTileY)
                .thenComparing((leftSack, rightSack) -> Integer.compare(rightSack.worldTileX(), leftSack.worldTileX())));
        return sacks;
    }

    /**
     * Java support extracted from MapVisualObject::HandleGameAction @00414B10 and TargetHandle::initFromBytes @0054F9B9.
     * not ported.
     */
    private PreviewBackPack createPreviewBackpack(
            ScenarioDescriptor scenario,
            WorldSack source,
            int tileX,
            int tileY,
            int left,
            int top,
            int surfaceLeft,
            int surfaceTop
    ) {
        PreviewBackPack backpack = new PreviewBackPack();
        int centerWorldX8 = tileX * 0x100 + TILE_WORLD_CENTER;
        int centerWorldY8 = tileY * 0x100 + TILE_WORLD_CENTER;
        backpack.initializeBackpackVisualState(
                0,
                sackSpriteType(scenario, source),
                centerWorldX8,
                centerWorldY8,
                0,
                0,
                0,
                0,
                1
        );
        backpack.location.x = centerWorldX8;
        backpack.location.y = centerWorldY8;
        backpack.location2.x = centerWorldX8;
        backpack.location2.y = centerWorldY8;
        backpack.centerWorldX8 = centerWorldX8;
        backpack.centerWorldY8 = centerWorldY8;
        backpack.centerScreenX = left + (centerWorldX8 >> 3) - surfaceLeft;
        backpack.centerScreenY = top + (centerWorldY8 >> 3) - surfaceTop;
        backpack.terrainHeightOffset = interpolateTerrainHeight(scenario, centerWorldX8, centerWorldY8);
        backpack.tileX = tileX;
        backpack.tileY = tileY;
        return backpack;
    }

    /**
     * Java support for the native sack price bucket used by CServerApp::notifyStateChanged @00503672.
     * not ported.
     */
    private static int sackSpriteType(ScenarioDescriptor scenario, WorldSack sack) {
        int price = sack.gold;
        for (int itemIndex = 0; itemIndex < sack.itemPackedHashes.size(); itemIndex++) {
            Item item = ScenarioMapLoader.createScenarioSackItem(sack, itemIndex, scenario);
            if (item != null) {
                price += item.price;
            }
        }
        if (price <= 0) {
            return 0;
        }
        return Math.min((int) Math.clamp(Math.log10(price), 0, 255), 5);
    }

    /**
     * Native support extracted from CGameObject::RefreshMapDerivedState @0046025D.
     */
    private int interpolateTerrainHeight(ScenarioDescriptor scenario, int mapPixelX, int mapPixelY) {
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
    private static double shadowAngle(ScenarioDescriptor scenario) {
        if (scenario.sunAngle >= 0.0 || scenario.sunAngle <= -0.05) {
            if (scenario.sunAngle <= 0.0 || scenario.sunAngle >= 0.05) {
                return scenario.sunAngle / 1.5;
            }
            return 0.05;
        }
        return -0.05;
    }

    /**
     * Native support extracted from CGameObject::ResolveShadowSlope for CBackPack::DrawShadow @00461292.
     */
    private static int terrainShadowSlope(ScenarioDescriptor scenario) {
        return (int) (Math.tan(shadowAngle(scenario)) * 65536.0);
    }

    /**
     * Java support conversion from raw shifted scenario coordinate to tile coordinate.
     * not ported.
     */
    private static int shiftedTile(int rawCoordinate) {
        return (rawCoordinate >>> 8) & 0xFFFF;
    }

    /**
     * Java support tile-bounds guard for editor sack preview data.
     * not ported.
     */
    private static boolean tileInBounds(ScenarioDescriptor scenario, int tileX, int tileY) {
        return tileX >= 0 && tileY >= 0 && tileX < scenario.mapWidth && tileY < scenario.mapHeight;
    }

    /**
     * Java support brightness-page selection matching the editor terrain preview convention.
     * not ported.
     */
    static int sackBrightnessPage(ScenarioDescriptor scenario) {
        return Math.max(0, scenario.darkness >> TERRAIN_BRIGHTNESS_PAGE_SHIFT);
    }

    /**
     * Java support clamp for backpack sprite palette page lookups.
     * not ported.
     */
    private static int clampBrightness(int brightness, CGamePalette palette) {
        return Math.min(brightness, palette.paletteData.length - 1);
    }

    /**
     * Java support CBackPack subclass exposing preview-only draw entry points without a live MapVisualObject.
     * not ported.
     */
    static final class PreviewBackPack extends CBackPack {
        /**
         * Java support constructor for one editor preview backpack.
         * not ported.
         */
        private PreviewBackPack() {
        }

        /**
         * Java support map-tile X used for editor draw ordering.
         * not ported.
         */
        int worldTileX() {
            return tileX;
        }

        /**
         * Java support map-tile Y used for editor draw ordering.
         * not ported.
         */
        int worldTileY() {
            return tileY;
        }

        /**
         * Java support main backpack sprite draw using CBackPack::Draw @004611BF.
         * not ported.
         */
        void drawEditorMain(int brightness) {
            int palettePage = clampBrightness(brightness, GUI.sprBackpack.palette);
            draw(0, 0, palettePage);
        }

        /**
         * Java support backpack shadow sprite draw using CBackPack::DrawShadow @00461292 math.
         * not ported.
         */
        void drawEditorShadow(ScenarioDescriptor scenario) {
            int shadowSlope = terrainShadowSlope(scenario);
            double shadowAngle = Math.tan(shadowAngle(scenario));
            int halfHeight = GUI.sprBackpack.ySizeOf(0) / 2;
            int shadowSkew = (int) (shadowAngle * halfHeight);
            int x = centerScreenX - GUI.sprBackpack.xSizeOf(0) / 2 - shadowSkew;
            int y = centerScreenY - GUI.sprBackpack.ySizeOf(0) / 2 - terrainHeightOffset - 4;
            GUI.sprBackpack.drawWithRenderEffect(x, y, type, Globals.lighting.shadowLength, shadowSlope, false);
            GUI.sprBackpackB.drawWithRenderEffect(x, y, type, Globals.lighting.lightHeight, shadowSlope, false);
        }
    }
}
