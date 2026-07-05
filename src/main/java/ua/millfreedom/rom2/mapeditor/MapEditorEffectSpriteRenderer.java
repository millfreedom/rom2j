package ua.millfreedom.rom2.mapeditor;

import ua.millfreedom.rom2.model.CProjectileInfo;
import ua.millfreedom.rom2.model.CSprite256;
import ua.millfreedom.rom2.model.Projectiles;
import ua.millfreedom.rom2.model.world.ScenarioDescriptor;
import ua.millfreedom.rom2.model.world.scenario.EffectDTO;
import ua.millfreedom.rom2.model.world.scenario.EffectOrTrapMod;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Editor-only EFFECTS-section transient spell/trap sprite preview backed by existing projectile assets.
 * not ported.
 */
final class MapEditorEffectSpriteRenderer {
    private static final int TILE_SCREEN_SIZE = 0x20;
    private static final int TILE_WORLD_SIZE = 0x100;
    private static final int TILE_WORLD_CENTER = 0x80;
    private static final int SCENARIO_EFFECT_MODE_TRANSIENT_SPELL_LIMIT = 4;
    private static final int SPELL_EFFECT_VISUAL_OFFSET = 8;
    private static final int EFFECT_TRAVEL_STEPS = 24;

    /**
     * Java support constructor for editor effect sprite previews.
     * not ported.
     */
    MapEditorEffectSpriteRenderer() {
    }

    /**
     * Java support grouping saved transient spell/trap effect previews by their current animated projectile cell.
     * not ported.
     */
    Map<Integer, List<PreviewEffect>> collectTransientEffectsByCell(
            ScenarioDescriptor scenario,
            int left,
            int top,
            Rectangle clip,
            int animationTick
    ) {
        Map<Integer, List<PreviewEffect>> byCell = new HashMap<>();
        for (int effectIndex = 0; effectIndex < scenario.sect9Effects.size(); effectIndex++) {
            EffectDTO effect = scenario.sect9Effects.get(effectIndex);
            if (!isTransientSpellCellEffect(effect)) {
                continue;
            }
            PreviewEffect preview = createPreviewEffect(
                    scenario,
                    effect,
                    effectIndex,
                    left,
                    top,
                    clip.x,
                    clip.y,
                    animationTick
            );
            if (preview == null) {
                continue;
            }
            byCell.computeIfAbsent(cellKey(preview.worldTileX(), preview.worldTileY()), key -> new ArrayList<>())
                    .add(preview);
        }
        for (List<PreviewEffect> effects : byCell.values()) {
            effects.sort(Comparator.comparingInt(PreviewEffect::worldTileY)
                    .thenComparing((leftEffect, rightEffect) ->
                            Integer.compare(rightEffect.worldTileX(), leftEffect.worldTileX())));
        }
        return byCell;
    }

    /**
     * Java support gate matching ScenarioMapLoader::materializeScenarioSacksAndEffects @00564072 transient spell-cell
     * branch.
     * not ported.
     */
    private static boolean isTransientSpellCellEffect(EffectDTO effect) {
        return (effect.x != 0 || effect.y != 0)
                && (effect.effectMode & 0xFFFF) < SCENARIO_EFFECT_MODE_TRANSIENT_SPELL_LIMIT
                && (effect.spellId & 0xFF) != 0;
    }

    /**
     * Java support current animated map tile for an editor-rendered transient spell/trap effect.
     * not ported.
     */
    static Point currentTransientEffectTile(EffectDTO effect, int effectIndex, int animationTick) {
        if (!hasDrawableTransientEffect(effect)) {
            return null;
        }
        int[] worldPoint = currentTransientEffectWorldPoint(effect, effectIndex, animationTick);
        return new Point(worldPoint[0] >> 8, worldPoint[1] >> 8);
    }

    /**
     * Java support drawable gate for transient spell/trap effect preview sprites.
     * not ported.
     */
    private static boolean hasDrawableTransientEffect(EffectDTO effect) {
        return isTransientSpellCellEffect(effect)
                && projectileInfoOrNull(spellEffectVisualProjectileId(effect.spellId)) != null;
    }

    /**
     * Java support preview state creation from the serialized transient spell/trap shape consumed by ScenarioMapLoader.
     * not ported.
     */
    private PreviewEffect createPreviewEffect(
            ScenarioDescriptor scenario,
            EffectDTO effect,
            int effectIndex,
            int left,
            int top,
            int surfaceLeft,
            int surfaceTop,
            int animationTick
    ) {
        int projectileId = spellEffectVisualProjectileId(effect.spellId);
        CProjectileInfo projectileInfo = projectileInfoOrNull(projectileId);
        if (projectileInfo == null) {
            return null;
        }

        int[] currentWorldPoint = currentTransientEffectWorldPoint(effect, effectIndex, animationTick);
        EffectOrTrapMod source = effect.carr.get(0);
        EffectOrTrapMod target = effect.carr.get(1);
        int sourceWorldX8 = cellCenterWorldCoordinate(source.type);
        int sourceWorldY8 = cellCenterWorldCoordinate(source.value);
        int targetWorldX8 = cellCenterWorldCoordinate(target.type);
        int targetWorldY8 = cellCenterWorldCoordinate(target.value);
        int currentWorldX8 = currentWorldPoint[0];
        int currentWorldY8 = currentWorldPoint[1];
        int screenX = left + (currentWorldX8 >> 3) - surfaceLeft;
        int screenY = top + (currentWorldY8 >> 3)
                - interpolateTerrainHeight(scenario, currentWorldX8, currentWorldY8)
                - surfaceTop;
        int framePhase = Math.floorMod(animationTick + effectIndex, Math.max(1, projectileInfo.phases));
        int direction = resolveDirectionToPoint(sourceWorldX8, sourceWorldY8, targetWorldX8, targetWorldY8);
        return new PreviewEffect(
                currentWorldX8 >> 8,
                currentWorldY8 >> 8,
                projectileInfo,
                screenX,
                screenY,
                resolveDrawFrameIndex(projectileInfo, framePhase, direction),
                resolveDrawFlipX(projectileInfo, direction)
        );
    }

    /**
     * Java support current animated world point for an editor-rendered transient spell/trap effect.
     * not ported.
     */
    private static int[] currentTransientEffectWorldPoint(EffectDTO effect, int effectIndex, int animationTick) {
        EffectOrTrapMod source = effect.carr.get(0);
        EffectOrTrapMod target = effect.carr.get(1);
        int sourceWorldX8 = cellCenterWorldCoordinate(source.type);
        int sourceWorldY8 = cellCenterWorldCoordinate(source.value);
        int targetWorldX8 = cellCenterWorldCoordinate(target.type);
        int targetWorldY8 = cellCenterWorldCoordinate(target.value);
        int travelStep = Math.floorMod(animationTick + effectIndex, EFFECT_TRAVEL_STEPS);
        return new int[]{
                interpolate(sourceWorldX8, targetWorldX8, travelStep, EFFECT_TRAVEL_STEPS - 1),
                interpolate(sourceWorldY8, targetWorldY8, travelStep, EFFECT_TRAVEL_STEPS - 1)
        };
    }

    /**
     * Java support visual projectile id mapping from EffectAction::baseSpellEffectPayload for spell casts.
     * not ported.
     */
    private static int spellEffectVisualProjectileId(int spellId) {
        return ((spellId & 0xFF) * 2 + SPELL_EFFECT_VISUAL_OFFSET) & 0xFF;
    }

    /**
     * Java support projectile-info lookup matching MapVisualObject::hasProjectileInfo @00411864.
     * not ported.
     */
    private static CProjectileInfo projectileInfoOrNull(int projectileId) {
        if (projectileId < 0 || Projectiles.PROJECTILES_BY_ID.size() <= projectileId) {
            return null;
        }
        return Projectiles.PROJECTILES_BY_ID.get(projectileId);
    }

    /**
     * Java support byte-domain cell center coordinate used by TargetHandle::initFromBytes @0054F9B9 callers.
     * not ported.
     */
    private static int cellCenterWorldCoordinate(int cell) {
        return (cell & 0xFF) * TILE_WORLD_SIZE + TILE_WORLD_CENTER;
    }

    /**
     * Java support linear interpolation for one animated editor preview projectile.
     * not ported.
     */
    private static int interpolate(int source, int target, int step, int maxStep) {
        return source + (target - source) * step / maxStep;
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
     * Native support adapted from CGameObject::ResolveDirectionToPoint @004608DE for editor preview points.
     */
    private static int resolveDirectionToPoint(int sourceX, int sourceY, int targetX, int targetY) {
        int dx = targetX - sourceX;
        int dy = targetY - sourceY;
        int absDx = Math.abs(dx);
        int absDy = Math.abs(dy);
        int direction;
        if (absDx < absDy * 4) {
            if (absDx * 3 < absDy * 4) {
                if (absDy < absDx * 4) {
                    if (absDy * 3 < absDx * 4) {
                        direction = 2;
                    } else {
                        direction = 3;
                    }
                } else {
                    direction = 4;
                }
            } else {
                direction = 1;
            }
        } else {
            direction = 0;
        }
        if (dy < 1) {
            if (dx < 0) {
                direction += 0x0C;
            } else {
                direction = 4 - direction;
            }
        } else if (dx < 1) {
            direction = 0x0C - direction;
        } else {
            direction += 4;
        }
        return direction & 0x0F;
    }

    /**
     * Native support extracted from CProjectile::Draw @0046CB7B default frame calculation.
     */
    private static int resolveDrawFrameIndex(CProjectileInfo projectileInfo, int phase, int direction) {
        int directionIndex = (direction - 8) & 0x0F;
        if (projectileInfo.flip != 0 && directionIndex > 8) {
            directionIndex = 0x10 - directionIndex;
        }
        int frameIndex = phase + projectileInfo.phases * directionIndex;
        if (projectileInfo.rotationPhases == 1) {
            frameIndex = phase;
        }
        return frameIndex;
    }

    /**
     * Native support extracted from CProjectile::Draw @0046CB7B default frame flip calculation.
     */
    private static boolean resolveDrawFlipX(CProjectileInfo projectileInfo, int direction) {
        int directionIndex = (direction - 8) & 0x0F;
        return projectileInfo.flip != 0 && directionIndex > 8;
    }

    /**
     * Java support packed map-cell key for effect row-pass grouping.
     * not ported.
     */
    static int cellKey(int tileX, int tileY) {
        return (tileY << 16) ^ (tileX & 0xFFFF);
    }

    /**
     * Java support one animated saved-effect projectile preview.
     * not ported.
     */
    static final class PreviewEffect {
        private final int worldTileX;
        private final int worldTileY;
        private final CProjectileInfo projectileInfo;
        private final int screenX;
        private final int screenY;
        private final int frame;
        private final boolean flipX;

        /**
         * Java support constructor for one saved-effect preview projectile.
         * not ported.
         */
        private PreviewEffect(
                int worldTileX,
                int worldTileY,
                CProjectileInfo projectileInfo,
                int screenX,
                int screenY,
                int frame,
                boolean flipX
        ) {
            this.worldTileX = worldTileX;
            this.worldTileY = worldTileY;
            this.projectileInfo = projectileInfo;
            this.screenX = screenX;
            this.screenY = screenY;
            this.frame = frame;
            this.flipX = flipX;
        }

        /**
         * Java support map-tile X used for editor draw ordering.
         * not ported.
         */
        int worldTileX() {
            return worldTileX;
        }

        /**
         * Java support map-tile Y used for editor draw ordering.
         * not ported.
         */
        int worldTileY() {
            return worldTileY;
        }

        /**
         * Java support main projectile sprite draw using CProjectile::Draw @0046CB7B default sprite routing.
         * not ported.
         */
        void drawEditorMain() {
            CSprite256 spriteA = projectileInfo.getSpriteA();
            int drawX = screenX - projectileInfo.width / 2;
            int drawY = screenY - projectileInfo.height / 2;
            if (projectileInfo.palette == 0) {
                spriteA.drawWithPalette(drawX, drawY, frame, 0, Projectiles.PROJECTILES_PAL, flipX);
                return;
            }

            spriteA.draw(drawX, drawY, frame, 0, flipX);
            CSprite256 spriteB = projectileInfo.getSpriteB();
            if (spriteB != null) {
                spriteB.drawBlend(drawX, drawY, frame, 0, flipX);
            }
        }
    }
}
