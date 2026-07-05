package ua.millfreedom.rom2.mapeditor;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CPlayer;
import ua.millfreedom.rom2.model.CSprite256;
import ua.millfreedom.rom2.model.CUnitInfo;
import ua.millfreedom.rom2.model.GraphicsUnitsFile;
import ua.millfreedom.rom2.model.Screen;
import ua.millfreedom.rom2.model.UnitRenderState;
import ua.millfreedom.rom2.model.UnitTypes;
import ua.millfreedom.rom2.model.column.HumanColumn;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.palette.CGamePalette;
import ua.millfreedom.rom2.model.render.Renderer;
import ua.millfreedom.rom2.model.render.SwingRenderer;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.unit.humanoid.human.Human;
import ua.millfreedom.rom2.model.unit.humanoid.human.HumanInfo;
import ua.millfreedom.rom2.model.world.ScenarioDescriptor;
import ua.millfreedom.rom2.model.world.scenario.UnitDTO;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Editor-only UNITS-section sprite painter backed by the existing CUnit sprite helpers and unit assets.
 * not ported.
 */
final class MapEditorUnitSpriteRenderer {
    private static final int TILE_SCREEN_SIZE = 0x20;
    private static final int TILE_WORLD_CENTER = 0x80;
    private static final int TERRAIN_BRIGHTNESS_PAGE_SHIFT = 2;
    private static final int AIR_UNIT_Z = 0x10;
    private static final int SCENARIO_HUMANOID_UNIT_FLAG = 0x10;
    private static final int HERO_VISUAL_SERVER_ID_MIN = 0x14;
    private static final int HERO_VISUAL_SERVER_ID_MAX = 0x18;
    private static final int HERO_VISUAL_TYPE_BASE = 0x21;

    private SwingRenderer unitRenderer;
    private int unitRendererWidth;
    private int unitRendererHeight;

    /**
     * Java support constructor for editor unit sprite previews.
     * not ported.
     */
    MapEditorUnitSpriteRenderer() {
    }

    /**
     * Java support sprite overlay for saved UNITS-section records in the editor viewport.
     * not ported.
     */
    void drawUnitSprites(
            Graphics2D graphics,
            ScenarioDescriptor scenario,
            int left,
            int top,
            int cellSize,
            int animationTick
    ) {
        if (cellSize != TILE_SCREEN_SIZE || scenario.sec6Units.isEmpty()) {
            return;
        }

        Rectangle clip = graphics.getClipBounds();
        if (clip == null) {
            clip = new Rectangle(left, top, scenario.mapWidth * cellSize, scenario.mapHeight * cellSize);
        }
        if (clip.width <= 0 || clip.height <= 0) {
            return;
        }

        ensureUnitRenderer(clip.width, clip.height);
        unitRenderer.clearSurface();
        Renderer previousRenderer = Globals.renderer;
        Globals.renderer = unitRenderer;
        try {
            int brightness = unitBrightnessPage(scenario);
            List<PreviewUnit> units = collectPreviewUnits(scenario, left, top, clip.x, clip.y, animationTick);
            if (Globals.gamePreferences.shadows != 0) {
                for (PreviewUnit unit : units) {
                    unit.drawEditorShadow(scenario);
                }
            }
            for (PreviewUnit unit : units) {
                unit.drawEditorMain(brightness);
            }
        } finally {
            Globals.renderer = previousRenderer;
        }
        unitRenderer.presentSurface(clip.width, clip.height);
        unitRenderer.drawTo(graphics, clip.x, clip.y, clip.width, clip.height);
    }

    /**
     * Java support visible-unit Swing render surface reuse.
     * not ported.
     */
    private void ensureUnitRenderer(int width, int height) {
        if (unitRenderer != null && unitRendererWidth == width && unitRendererHeight == height) {
            return;
        }
        unitRenderer = new SwingRenderer(Screen.createBgraSurface(width, height));
        unitRendererWidth = width;
        unitRendererHeight = height;
    }

    /**
     * Java support extracted from MapVisualObject object-layer unit dispatch.
     * not ported.
     */
    List<PreviewUnit> collectPreviewUnits(
            ScenarioDescriptor scenario,
            int left,
            int top,
            int surfaceLeft,
            int surfaceTop,
            int animationTick
    ) {
        List<PreviewUnit> units = new ArrayList<>();
        for (UnitDTO unit : scenario.sec6Units) {
            units.add(createPreviewUnit(scenario, unit, left, top, surfaceLeft, surfaceTop, animationTick));
        }
        units.sort(Comparator.comparingInt(PreviewUnit::worldTileY)
                .thenComparing((leftUnit, rightUnit) -> Integer.compare(rightUnit.worldTileX(), leftUnit.worldTileX())));
        return units;
    }

    /**
     * Java support extracted from MapDescriptor::MapDescriptor @004A449C scenario visual CUnit setup.
     * not ported.
     */
    private PreviewUnit createPreviewUnit(
            ScenarioDescriptor scenario,
            UnitDTO source,
            int left,
            int top,
            int surfaceLeft,
            int surfaceTop,
            int animationTick
    ) {
        PreviewUnit unit = createScenarioPreviewUnit(source);
        unit.cPlayer = resolveScenarioPlayer(scenario, source.playerID);
        unit.serverID = (short) source.serverID;
        unit.questFlags = source.questFlags;
        unit.dir = source.rotation;
        unit.location.x = source.x;
        unit.location.y = source.y;
        unit.location2.x = source.x;
        unit.location2.y = source.y;

        CUnitInfo renderInfo = Objects.requireNonNull(
                UnitTypes.getUnitInfo(unit.type),
                "Missing CUnitInfo for id " + unit.type
        );
        int tileSize = renderInfo.m_TileSize;
        int centerWorldX8 = source.x - TILE_WORLD_CENTER + tileSize * TILE_WORLD_CENTER;
        int centerWorldY8 = source.y - TILE_WORLD_CENTER + tileSize * TILE_WORLD_CENTER;
        unit.centerWorldX8 = centerWorldX8;
        unit.centerWorldY8 = centerWorldY8;
        unit.centerScreenX = left + (centerWorldX8 >> 3) - surfaceLeft;
        unit.centerScreenY = top + (centerWorldY8 >> 3) - surfaceTop;
        unit.terrainHeightOffset = interpolateTerrainHeight(scenario, centerWorldX8, centerWorldY8);
        unit.phase = idlePhase(renderInfo, source, animationTick);
        unit.actionPhase = unit.phase;
        unit.lastAction = 0;
        unit.tileX = shiftedTile(source.x);
        unit.tileY = shiftedTile(source.y);
        return unit;
    }

    /**
     * Java support extracted from MapDescriptor::MapDescriptor @004A449C scenario visual CUnit setup.
     * not ported.
     */
    private PreviewUnit createScenarioPreviewUnit(UnitDTO source) {
        if (isScenarioHumanoidUnit(source)) {
            return createScenarioHumanoidPreviewUnit(source);
        }

        CUnitInfo initialInfo = Objects.requireNonNull(
                UnitTypes.getUnitInfo(source.typeID),
                "Missing CUnitInfo for id " + source.typeID
        );
        PreviewUnit unit = new PreviewUnit(initialInfo.m_ZOffset != 0);
        unit.type = source.typeID;
        unit.applyScenarioFace(source.face);
        unit.serverID = (short) source.serverID;
        unit.questFlags = source.questFlags;
        unit.HP = (short) source.hp;
        unit.MaxHP = (short) source.maxHp;
        return unit;
    }

    /**
     * Java support extracted from MapDescriptor::MapDescriptor @004A449C humanoid branch.
     * not ported.
     */
    private PreviewUnit createScenarioHumanoidPreviewUnit(UnitDTO source) {
        Human human = createScenarioHumanUnit(source);
        applyHeroVisualTypeOverride(human);

        PreviewUnit unit = new PreviewUnit(false);
        unit.copyFromRuntimeUnit(human);
        return unit;
    }

    /**
     * Java support extracted from MapDescriptor::MapDescriptor @004A449C scenario-unit flag check.
     * not ported.
     */
    private static boolean isScenarioHumanoidUnit(UnitDTO source) {
        return (source.unitFlags1 & SCENARIO_HUMANOID_UNIT_FLAG) != 0;
    }

    /**
     * Java support extracted from MapDescriptor::MapDescriptor @004A449C humanoid Human setup.
     * not ported.
     */
    private static Human createScenarioHumanUnit(UnitDTO source) {
        Human human = new Human().initializeDefaultTemplate();
        int humanInfoIndex = Globals.staticDataMgr.findHumanByServerId(source.serverID);
        human.applyScenarioHumanInfo(humanInfoIndex);
        applyScenarioHealthOverrides(human, source);
        HumanInfo humanInfo = Globals.staticDataMgr.humans.get(humanInfoIndex);
        human.addScenarioEquipment(humanInfo);
        return human;
    }

    /**
     * Java support extracted from MapDescriptor::MapDescriptor @004A449C humanoid HP/max-HP branch.
     * not ported.
     */
    private static void applyScenarioHealthOverrides(Unit unit, UnitDTO source) {
        int scenarioHp = signedScenarioShort(source.hp);
        int scenarioMaxHp = signedScenarioShort(source.maxHp);
        if (scenarioMaxHp != -1 && scenarioMaxHp == scenarioHp) {
            scenarioHp = -1;
            scenarioMaxHp = -1;
        }
        if (scenarioMaxHp > 0) {
            unit.m_nMaxHP = scenarioMaxHp;
        }
        if (scenarioHp != -1) {
            unit.m_nHP = scenarioHp;
        }
    }

    /**
     * Java support extracted from MapDescriptor::MapDescriptor @004A449C hero visual type override.
     * not ported.
     */
    private static void applyHeroVisualTypeOverride(Unit runtimeUnit) {
        if (runtimeUnit.serverID <= HERO_VISUAL_SERVER_ID_MIN || runtimeUnit.serverID >= HERO_VISUAL_SERVER_ID_MAX) {
            return;
        }
        int humanoidTypeBase = runtimeUnit.unitInfoLine.getValue(HumanColumn.IS_FEMALE.index);
        runtimeUnit.typeID = (humanoidTypeBase + HERO_VISUAL_TYPE_BASE + (runtimeUnit.m_nMaxMP > 0 ? 2 : 0)) & 0xFFFF;
        runtimeUnit.face &= 0x3F;
    }

    /**
     * Java support extracted from MapDescriptor::MapDescriptor @004A449C signed scenario WORD checks.
     * not ported.
     */
    private static int signedScenarioShort(int value) {
        return (short) (value & 0xFFFF);
    }

    /**
     * Java support player-palette lookup for serialized editor scenario players.
     * not ported.
     */
    private CPlayer resolveScenarioPlayer(ScenarioDescriptor scenario, int playerId) {
        if (scenario.sec5Players.isEmpty()) {
            return new CPlayer(0, 0);
        }
        CPlayer resolved = scenario.sec5Players.get(0);
        for (CPlayer player : scenario.sec5Players) {
            if (player.playerId == playerId) {
                resolved = player;
                break;
            }
        }
        return new CPlayer(resolved);
    }

    /**
     * Java support idle-animation phase selection without running CUnit::AdvanceMapObjectState.
     * not ported.
     */
    private static int idlePhase(CUnitInfo info, UnitDTO unit, int animationTick) {
        if (Globals.gamePreferences.animation == 0 || info.m_IdlePhases == 0 || info.m_IdleFrameSequenceCount == 0) {
            return 0;
        }
        return Math.floorMod(animationTick + unit.unitID, info.m_IdleFrameSequenceCount);
    }

    /**
     * Native support extracted from CUnit::DrawShadow @00464487 shadow sprite tuple.
     */
    private static void drawUnitShadowSprite(
            ScenarioDescriptor scenario,
            PreviewUnit unit,
            UnitRenderState renderState,
            CSprite256 sprite,
            int effectIndex
    ) {
        int shadowSlope = terrainShadowSlope(scenario);
        int shadowSkew = shadowSkewForSprite(scenario, sprite, renderState);
        int drawX = unit.centerScreenX
                - ((renderState.info.m_CenterX - renderState.info.m_Width / 2)
                + sprite.xSizeOf(renderState.frameIndex) / 2)
                - shadowSkew;
        int drawY = (unit.centerScreenY
                - ((renderState.info.m_CenterY - renderState.info.m_Height / 2)
                + sprite.ySizeOf(renderState.frameIndex) / 2))
                - unit.terrainHeightOffset;
        if (unit.airShadow) {
            sprite.drawAlpha(
                    drawX + shadowSkew + shadowSlope / 2000,
                    drawY,
                    renderState.frameIndex,
                    effectIndex,
                    renderState.flipX
            );
            return;
        }

        sprite.drawWithRenderEffect(drawX, drawY, renderState.frameIndex, effectIndex, shadowSlope, renderState.flipX);
    }

    /**
     * Native support extracted from CUnit::DrawShadow @00464487 shadow skew.
     */
    private static int shadowSkewForSprite(ScenarioDescriptor scenario, CSprite256 sprite, UnitRenderState renderState) {
        double shadowAngle = Math.tan(shadowAngle(scenario));
        return (int) (shadowAngle
                * ((sprite.ySizeOf(renderState.frameIndex) / 2 + renderState.info.m_Height / 2)
                - renderState.info.m_CenterY));
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
     * Native support extracted from CGameObject::ResolveShadowSlope for CUnit::DrawShadow @00464487.
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
     * Java support brightness-page selection matching the editor terrain preview convention.
     * not ported.
     */
    static int unitBrightnessPage(ScenarioDescriptor scenario) {
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
     * Java support CUnit subclass exposing preview-only draw entry points without calling CUnit::Draw.
     * not ported.
     */
    static final class PreviewUnit extends CUnit {
        private final boolean airShadow;

        /**
         * Java support constructor for one editor preview unit.
         * not ported.
         */
        private PreviewUnit(boolean airShadow) {
            this.airShadow = airShadow;
            if (airShadow) {
                z = AIR_UNIT_Z;
            }
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
         * Java support visual-layer gate matching MapDescriptor::MapDescriptor @004A449C CAirUnit selection.
         * not ported.
         */
        boolean isAirLayer() {
            return airShadow;
        }

        /**
         * Java support main unit sprite draw using existing CUnit helpers.
         * not ported.
         */
        void drawEditorMain(int brightness) {
            if (getDeathState() >= 5) {
                return;
            }

            UnitRenderState renderState = resolveRenderState();
            CGamePalette palette = resolveRenderPalette(UnitTypes.getUnitInfo(type), renderState.info, false);
            int palettePage = clampBrightness(brightness, palette);
            if ((unitFlags & 0x1) == 0) {
                drawGroundUnit(renderState, palettePage, palette, false);
                return;
            }

            if (pCSprite256 != null && renderState.frameIndex < pCSprite256.frameCount) {
                drawAirUnit(renderState, palettePage, palette, false);
            }
        }

        /**
         * Java support unit shadow sprite draw using existing UnitTypes sprite sheets.
         * not ported.
         */
        void drawEditorShadow(ScenarioDescriptor scenario) {
            if (getDeathState() >= 5) {
                return;
            }

            UnitRenderState renderState = resolveRenderState();
            if ((unitFlags & 0x1) == 0) {
                GraphicsUnitsFile graphicsUnitsFile = UnitTypes.getGraphicsUnitsFile(renderState.fileId);
                drawUnitShadowSprite(scenario, this, renderState, graphicsUnitsFile.getSprite(), Globals.lighting.shadowLength);
                if (Globals.gamePreferences.smoothing != 0) {
                    drawUnitShadowSprite(scenario, this, renderState, graphicsUnitsFile.getBSprite(), Globals.lighting.lightHeight);
                }
                return;
            }

            if (pCSprite256 != null && renderState.frameIndex < pCSprite256.frameCount) {
                drawUnitShadowSprite(scenario, this, renderState, pCSprite256, Globals.lighting.shadowLength);
                if (Globals.gamePreferences.smoothing != 0 && pCSprite256_2 != null) {
                    drawUnitShadowSprite(scenario, this, renderState, pCSprite256_2, Globals.lighting.lightHeight);
                }
            }
        }
    }
}
