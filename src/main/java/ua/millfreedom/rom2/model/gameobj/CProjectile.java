package ua.millfreedom.rom2.model.gameobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CA16;
import ua.millfreedom.rom2.model.CProjectileInfo;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.CSprite256;
import ua.millfreedom.rom2.model.Projectiles;
import ua.millfreedom.rom2.model.VObjects;
import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.model.enums.SfxSounds;
import ua.millfreedom.rom2.model.sound.Sound;
import ua.millfreedom.rom2.model.sound.SoundManager;
import ua.millfreedom.rom2.model.sound.SoundSystem;
import ua.millfreedom.rom2.model.spell.EffectVisualBuilder;
import ua.millfreedom.rom2.model.spell.VisualElem;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;
import ua.millfreedom.rom2.model.world.MapDescriptor;

public class CProjectile extends CGameObject {
    private static final int TERRAIN_TILE_INDEX_MASK = 0x1FFF;
    private static final int TERRAIN_TILE_FAMILY_SHIFT = 6;
    private static final int ANIMATED_TERRAIN_FIRST_TILE = 8;
    private static final int ANIMATED_TERRAIN_LAST_TILE = 0x0B;
    private static final int TERRAIN_EFFECT_CELL_MASK = 0x2000;

    //0x144
    public final CustomList<Integer> smokeTrailPositions = CustomList.std(Integer.class);

    /**
     * Native: CProjectile::CProjectile @0046C0FA.
     * Fully ported.
     */
    public CProjectile() {
    }

    /**
     * Native: CProjectile::CProjectile @0046C154.
     * Fully ported.
     */
    public CProjectile(CProjectile source) {
        super(source);
        smokeTrailPositions.addAll(source.smokeTrailPositions);
    }

    /**
     * vtbl +0x10: CProjectile::Dump @0046D79C.
     * Fully ported.
     */
    @Override
    public String dump() {
        return "CProjectile";
    }

    /**
     * vtbl +0x20: CProjectile::GetTileWidth @0046DF50.
     * Full port. Native returns a constant tile width of `1`.
     */
    @Override
    public int getTileWidth() {
        return 1;
    }

    /**
     * vtbl +0x24: CProjectile::GetTileHeight @0046DF60.
     * Full port. Native returns a constant tile height of `1`.
     */
    @Override
    public int getTileHeight() {
        return 1;
    }

    /**
     * vtbl +0x28: CProjectile::Draw @0046CB7B.
     * Fully ported.
     */
    @Override
    public void draw(int param1, int param2, int paletteIndex) {
        CProjectileInfo projectileInfo = resolveProjectileInfoOrNull();
        if (projectileInfo == null) {
            return;
        }

        int drawX = screenX - projectileInfo.width / 2;
        int drawY = ((screenY - projectileInfo.height / 2) - terrainHeightOffset) - z;
        switch (type) {
            case 7 -> Globals.materialRuntimeData.radialScreenDistortion.applyToRenderSurface(
                    screenX,
                    (screenY - terrainHeightOffset) - z
            );
            case 0x17 -> drawBlizzardProjectile(projectileInfo, drawX, drawY, paletteIndex);
            case 0x1C -> drawArcEffectVisuals(0x1C);
            case 0x1E -> drawMultiArcEffectVisuals();
            case 0x36 -> projectileInfo.getSpriteA().draw(drawX, drawY, phase, paletteIndex, false);
            case 0x38 -> {
            }
            default -> drawDefaultProjectile(projectileInfo, drawX, drawY, paletteIndex);
        }
    }

    /**
     * Native support extracted from CProjectile::Draw @0046CB7B default frame/flip calculation.
     */
    private int resolveDrawFrameIndex(CProjectileInfo projectileInfo) {
        int directionIndex = (dir - 8) & 0x0F;
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
    private boolean resolveDrawFlipX(CProjectileInfo projectileInfo) {
        int directionIndex = (dir - 8) & 0x0F;
        return projectileInfo.flip != 0 && directionIndex > 8;
    }

    /**
     * Native support extracted from CProjectile::Draw @0046CB7B default projectile branch.
     */
    private void drawDefaultProjectile(CProjectileInfo projectileInfo, int drawX, int drawY, int paletteIndex) {
        int frameIndex = resolveDrawFrameIndex(projectileInfo);
        boolean flipX = resolveDrawFlipX(projectileInfo);
        CSprite256 spriteA = projectileInfo.getSpriteA();
        if (projectileInfo.palette == 0) {
            spriteA.drawWithPalette(drawX, drawY, frameIndex, paletteIndex, Projectiles.PROJECTILES_PAL, flipX);
            return;
        }

        spriteA.draw(drawX, drawY, frameIndex, paletteIndex, flipX);
        CSprite256 spriteB = projectileInfo.getSpriteB();
        if (spriteB != null) {
            spriteB.drawBlend(drawX, drawY, frameIndex, paletteIndex, flipX);
        }
        if (!smokeTrailPositions.isEmpty()) {
            drawSmokeTrail(paletteIndex);
        }
    }

    /**
     * Native support extracted from CProjectile::Draw @0046CB7B blizzard projectile branch.
     */
    private void drawBlizzardProjectile(CProjectileInfo projectileInfo, int drawX, int drawY, int paletteIndex) {
        int frameIndex;
        int adjustedY = drawY;
        if (phase < 8) {
            frameIndex = 8;
            adjustedY -= 0x1C - phase * 4;
        } else {
            frameIndex = phase - 8;
        }
        projectileInfo.getSpriteA().draw(drawX, adjustedY, frameIndex, paletteIndex, false);
    }

    /**
     * Native support extracted from CProjectile::Draw @0046CB7B projectile type 0x1C visual-element branch.
     */
    private void drawArcEffectVisuals(int projectileId) {
        CSprite256 sprite = Projectiles.PROJECTILES_BY_ID.get(projectileId).getSpriteA();
        for (VisualElem visual : transientVisualElements) {
            sprite.draw(visual.x - 8, visual.y - 8, phase, 0, false);
        }
    }

    /**
     * Native support extracted from CProjectile::Draw @0046CB7B projectile type 0x1E visual-element branch.
     */
    private void drawMultiArcEffectVisuals() {
        CSprite256 sprite = Projectiles.PROJECTILES_BY_ID.get(0x1E).getSpriteA();
        for (VisualElem visual : transientVisualElements) {
            int frameIndex = phase + Byte.toUnsignedInt(visual.spriteId) * 5;
            sprite.draw(visual.x - 8, visual.y - 8, frameIndex, 0, false);
        }
    }

    /**
     * Native support extracted from CProjectile::Draw @0046CB7B smoke-trail branch.
     */
    private void drawSmokeTrail(int paletteIndex) {
        CA16 smokeSprite = Projectiles.SMOKE_SPRITES[type == 0x0A ? 0 : 1];
        for (int index = 0; index < smokeTrailPositions.size(); index++) {
            int packedPosition = smokeTrailPositions.get(index);
            int drawX = (((packedPosition & 0xFFFF) >>> 3) - pMapVisualObject.view.x * 0x20)
                    - smokeSprite.xSizeOf(0) / 2;
            int drawY = (((packedPosition >>> 19) - pMapVisualObject.view.y * 0x20)
                    - smokeSprite.ySizeOf(0) / 2) - terrainHeightOffset - z;
            smokeSprite.draw(drawX, drawY, index, paletteIndex, false);
        }
    }

    /**
     * vtbl +0x3C: CProjectile::AdvanceMapObjectState @0046C1D0.
     * Fully ported. Native projectile visual rebuild remains delegated through vtbl +0x50.
     */
    @Override
    public boolean advanceMapObjectState() {
        MapVisualObject map = pMapVisualObject;
        markProjectileDirtyRect(map);
        if (actionSegments == 0) {
            if (type == 0x0D) {
                map.renderFrameDirty = 1;
            }
            return false;
        }

        int previousX = location.x;
        int previousY = location.y;
        CGameObject target = null;
        CProjectileInfo projectileInfo = resolveProjectileInfoOrNull();
        if (action == 1) {
            if (actionTarget != 0) {
                target = resolveActionTarget(map);
            }

            int stepX = (actionX - location.x) / actionSegments;
            int stepY = (actionY - location.y) / actionSegments;
            int stepZ = (actionZ - z) / actionSegments;
            dir = actionDir;
            actionPhase++;
            advanceMoveAction(projectileInfo, target, stepX, stepY, stepZ);
            updateSmokeTrail(previousX, previousY);
            updateEffectVisuals();
        }

        lastAction = action;
        actionSegments--;
        location2.x = location.x;
        location2.y = location.y;
        return true;
    }

    /**
     * Native support extracted from CProjectile::AdvanceMapObjectState @0046C1D0 dirty rectangle update.
     */
    private void markProjectileDirtyRect(MapVisualObject map) {
        map.dirtyRenderRect.unionWith(new CRect(
                (screenX - 0x40) & 0xFFFFFFE0,
                screenY - terrainHeightOffset - 0x40,
                ((screenX + 0x40) & 0xFFFFFFE0) + 0x20,
                screenY - terrainHeightOffset + 0x40
        ));
    }

    /**
     * Native support extracted from CProjectile::AdvanceMapObjectState @0046C1D0 projectile-info lookup.
     */
    private CProjectileInfo resolveProjectileInfoOrNull() {
        if (Projectiles.PROJECTILES_BY_ID.size() <= type) {
            return null;
        }
        return Projectiles.PROJECTILES_BY_ID.get(type);
    }

    /**
     * Native support extracted from CProjectile::AdvanceMapObjectState @0046C1D0 action-target lookup and homing refresh.
     */
    private CGameObject resolveActionTarget(MapVisualObject map) {
        CGameObject target = map.getObjectByToken(actionTarget);
        if (target != null) {
            actionX = target.centerWorldX8;
            actionY = target.centerWorldY8;
            actionZ = target.z;
            actionDir = (byte) resolveDirectionToPoint(actionX, actionY);
        }
        return target;
    }

    /**
     * Native support extracted from CProjectile::AdvanceMapObjectState @0046C1D0 type switch.
     */
    private void advanceMoveAction(CProjectileInfo projectileInfo, CGameObject target, int stepX, int stepY, int stepZ) {
        switch (type) {
            case 0x0D -> advanceTerrainEffectProjectile(projectileInfo);
            case 0x10, 0x14, 0x18, 0x22, 0x2C, 0x2E, 0x30, 0x32, 0x3E, 0x40, 0x42 ->
                    applyTargetTimedEffect(target, 0xFFFF);
            case 0x17 -> advanceBlizzardProjectile(stepX, stepY, stepZ);
            case 0x1C, 0x1E -> phase = projectilePhaseForArcEffect();
            case 0x36 -> phase = actionPhase - 1;
            case 0x38, 0x3C -> applyRefreshableTargetTimedEffect((CUnit) target);
            default -> advanceDefaultProjectile(projectileInfo, stepX, stepY, stepZ);
        }
    }

    /**
     * Native support extracted from CProjectile::AdvanceMapObjectState @0046C1D0 default movement branch.
     */
    private void advanceDefaultProjectile(CProjectileInfo projectileInfo, int stepX, int stepY, int stepZ) {
        location.x += stepX;
        location.y += stepY;
        z += stepZ;
        dir = actionDir;
        phase = projectileInfo == null ? 0 : (actionPhase / 2) % projectileInfo.phases;
    }

    /**
     * Native support extracted from CProjectile::AdvanceMapObjectState @0046C1D0 terrain-effect projectile branch.
     */
    private void advanceTerrainEffectProjectile(CProjectileInfo projectileInfo) {
        if (actionPhase == 8) {
            markTerrainEffectCellsBlocked();
        } else if (actionPhase == 4) {
            addTransientTerrainEffectCells();
        }
        phase = (actionPhase / 2) % projectileInfo.phases;
    }

    /**
     * Native support extracted from CProjectile::AdvanceMapObjectState @0046C1D0 phase-8 terrain mutation.
     */
    private void markTerrainEffectCellsBlocked() {
        MapDescriptor descriptor = pMapVisualObject.mapDescriptor;
        short[] tiles = descriptor.getTilesWxH();
        byte[] objects = descriptor.getObjectsWxH();
        for (int dy = -1; dy < 2; dy++) {
            for (int dx = -1; dx < 2; dx++) {
                int flatIndex = terrainEffectFlatIndex(dx, dy);
                int terrainFamily = (Short.toUnsignedInt(tiles[flatIndex]) & TERRAIN_TILE_INDEX_MASK)
                        >> TERRAIN_TILE_FAMILY_SHIFT;
                int objectId = Byte.toUnsignedInt(objects[flatIndex]);
                if (terrainFamily < ANIMATED_TERRAIN_FIRST_TILE
                        || terrainFamily > ANIMATED_TERRAIN_LAST_TILE
                        || objectId != 0) {
                    tiles[flatIndex] = (short) (tiles[flatIndex] | TERRAIN_EFFECT_CELL_MASK);
                }
            }
        }
    }

    /**
     * Native support extracted from CProjectile::AdvanceMapObjectState @0046C1D0 phase-4 transient terrain effects.
     */
    private void addTransientTerrainEffectCells() {
        MapDescriptor descriptor = pMapVisualObject.mapDescriptor;
        short[] tiles = descriptor.getTilesWxH();
        byte[] objects = descriptor.getObjectsWxH();
        for (int dy = -1; dy < 2; dy++) {
            for (int dx = -1; dx < 2; dx++) {
                int flatIndex = terrainEffectFlatIndex(dx, dy);
                if ((Short.toUnsignedInt(tiles[flatIndex]) & TERRAIN_EFFECT_CELL_MASK) != 0) {
                    continue;
                }

                int objectId = Byte.toUnsignedInt(objects[flatIndex]);
                if (objectId != 0 && VObjects.getVObject(objectId - 1).deadObjectId != -1) {
                    addTransientTerrainEffectCell(dx, dy);
                }

                int terrainFamily = (Short.toUnsignedInt(tiles[flatIndex]) & TERRAIN_TILE_INDEX_MASK)
                        >> TERRAIN_TILE_FAMILY_SHIFT;
                if (terrainFamily > 7 && terrainFamily < 0x0C) {
                    addTransientTerrainEffectCell(dx, dy);
                }
            }
        }
    }

    /**
     * Native support extracted from CProjectile::AdvanceMapObjectState @0046C1D0 flat tile indexing.
     */
    private int terrainEffectFlatIndex(int dx, int dy) {
        return tileX + dx + (tileY + dy) * pMapVisualObject.cachedMapWidth;
    }

    /**
     * Native support extracted from CProjectile::AdvanceMapObjectState @0046C1D0 transient-effect map writes.
     */
    private void addTransientTerrainEffectCell(int dx, int dy) {
        int effectTileX = tileX + dx;
        int effectTileY = tileY + dy;
        pMapVisualObject.transientEffectCells.put((effectTileX & 0xFF) | ((effectTileY & 0xFF) << 8), 0);
    }

    /**
     * Native support extracted from CProjectile::AdvanceMapObjectState @0046C1D0 one-shot target effect branch.
     */
    private void applyTargetTimedEffect(CGameObject target, int duration) {
        if (target != null) {
            location.x = actionX;
            location.y = actionY;
            phase = actionPhase;
            target.dwarr_130.add((type << 16) | duration);
        }
    }

    /**
     * Native support extracted from CProjectile::AdvanceMapObjectState @0046C1D0 blizzard movement and sound branch.
     */
    private void advanceBlizzardProjectile(int stepX, int stepY, int stepZ) {
        location.x += stepX;
        location.y += stepY;
        z += stepZ;
        dir = actionDir;
        phase = actionPhase;
        if (actionPhase == 8) {
            Sound sound = SoundManager.SFX_SOUNDS.get(SfxSounds.MAGIC_BLIZZARD.id);
            if (sound != null) {
                SoundSystem soundSystem = SoundSystem.get();
                pMapVisualObject.updateSoundSystemMapAudioView(soundSystem);
                byte priority = objectMapSoundPriority(soundSystem);
                playObjectMapSound(soundSystem, sound, Globals.soundPreferences.sfxVolume, priority, 0);
            }
        }
    }

    /**
     * Native support extracted from CProjectile::AdvanceMapObjectState @0046C1D0 arcing effect phase table.
     */
    private int projectilePhaseForArcEffect() {
        return switch (actionPhase) {
            case 1, 0x0D -> 4;
            case 2, 0x0C -> 3;
            case 3, 7, 0x0B -> 2;
            case 4, 6, 8, 10 -> 1;
            case 5, 9 -> 0;
            default -> phase;
        };
    }

    /**
     * Native support extracted from CProjectile::AdvanceMapObjectState @0046C1D0 refreshable target effect branch.
     */
    private void applyRefreshableTargetTimedEffect(CUnit target) {
        if (target != null) {
            location.x = actionX;
            location.y = actionY;
            phase = actionPhase;
            int existingIndex = target.findPackedEffectIndex(type);
            int packedEffect = (type << 16) | 0x20;
            if (existingIndex < 0) {
                target.dwarr_130.add(packedEffect);
            } else {
                target.dwarr_130.set(existingIndex, packedEffect);
            }
        }
    }

    /**
     * Native support extracted from CProjectile::AdvanceMapObjectState @0046C1D0 smoke-trail history branch.
     */
    private void updateSmokeTrail(int previousX, int previousY) {
        if (type == 0x0A || type == 0x0C) {
            if (smokeTrailPositions.size() > 5) {
                smokeTrailPositions.remove(0);
            }
            smokeTrailPositions.add(previousX | (previousY << 16));
        }
    }

    /**
     * vtbl +0x40: CProjectile::UpdateMapOverlay @0046D0EF.
     * Fully ported.
     */
    @Override
    public void updateMapOverlay() {
        MapVisualObject map = pMapVisualObject;
        switch (type) {
            case 0x0A, 0x12, 0x28 -> map.applyDynamicLightOverride(tileX, tileY, 0, (byte) 0x10);
            case 0x0C -> map.applyDynamicLightOverride(tileX, tileY, 1, (byte) 0x10);
            case 0x0D -> updateTerrainEffectLightOverlay(map);
            case 0x1C, 0x1E -> updateArcEffectLightOverlay(map);
            default -> {
                if (Globals.gamePreferences.animation == 0) {
                    map.dirtyRenderRect.unionWith(new CRect(
                            (tileX - 2) * 0x20,
                            (tileY - 2) * 0x20,
                            (tileX + 3) * 0x20,
                            (tileY + 3) * 0x20
                    ));
                }
            }
        }
    }

    /**
     * Native support extracted from CProjectile::UpdateMapOverlay @0046D0EF projectile type 0x0D phase branch.
     */
    private void updateTerrainEffectLightOverlay(MapVisualObject map) {
        int radius = 3;
        int brightness = 0;
        switch (phase) {
            case 0 -> {
                radius = 1;
                brightness = 0x10;
            }
            case 1 -> {
                radius = 2;
                brightness = 0x08;
            }
            case 4 -> brightness = 0x08;
            case 5 -> brightness = 0x10;
            case 6 -> brightness = 0x18;
            case 7 -> brightness = 0x20;
            case 8 -> brightness = 0x28;
            case 9, 10 -> brightness = 0x2E;
            default -> {
            }
        }
        map.applyDynamicLightOverride(tileX, tileY, radius, (byte) brightness);
    }

    /**
     * Native support extracted from CProjectile::UpdateMapOverlay @0046D0EF projectile types 0x1C and 0x1E.
     */
    private void updateArcEffectLightOverlay(MapVisualObject map) {
        byte brightness = (byte) (phase * 10);
        for (VisualElem visual : transientVisualElements) {
            int lightTileX = visual.x >> 5;
            if (lightTileX < 0 || lightTileX > map.gridWidth) {
                continue;
            }
            int lightTileY = map.getTileYForScreenPoint(visual.x, visual.y);
            if (lightTileY >= 0 && lightTileY <= map.gridHeight + 4) {
                int gridX = lightTileX + 3;
                int gridY = lightTileY + 3;
                map.dynamicLightOverrideGrid[gridX][gridY] = brightness;
                map.dynamicLightOverrideGrid[gridX + 1][gridY] = brightness;
                map.dynamicLightOverrideGrid[gridX][gridY + 1] = brightness;
                map.dynamicLightOverrideGrid[gridX + 1][gridY + 1] = brightness;
                map.dynamicLightCellCount++;
                map.dirtyRenderRect.unionWith(new CRect(
                        (lightTileX - 1) * 0x20,
                        (lightTileY - 1) * 0x20,
                        (lightTileX + 2) * 0x20,
                        (lightTileY + 2) * 0x20
                ));
            }
        }
    }

    /**
     * vtbl +0x50: CProjectile::UpdateEffectVisuals @0046D46A.
     * Fully ported.
     */
    @Override
    public void updateEffectVisuals() {
        if (type == 0x1C) {
            EffectVisualBuilder builder = new EffectVisualBuilder();
            int sourceX = screenX;
            int sourceY = (screenY - terrainHeightOffset) - z;
            CGameObject target = pMapVisualObject.getObjectByToken(actionTarget);
            if (target != null) {
                builder.buildArcPathVisuals(
                        sourceX,
                        sourceY,
                        target.centerScreenX,
                        (target.centerScreenY - target.terrainHeightOffset) - target.z,
                        0x1C
                );
                transientVisualElements.clear();
                transientVisualElements.addAll(builder.visualElements);
            }
        } else if (type == 0x1E) {
            EffectVisualBuilder builder = new EffectVisualBuilder();
            int sourceX = screenX;
            int sourceY = (screenY - terrainHeightOffset) - z;
            transientVisualElements.clear();
            for (int index = 0; index < actionTargets.size(); index++) {
                CGameObject target = pMapVisualObject.getObjectByToken(actionTargets.get(index));
                if (target != null) {
                    builder.buildArcPathVisuals(
                            sourceX,
                            sourceY,
                            target.centerScreenX,
                            (target.centerScreenY - target.terrainHeightOffset) - target.z,
                            index % 7
                    );
                    transientVisualElements.addAll(builder.visualElements);
                    builder.clearReusableArcState();
                }
            }
        }
    }
}
