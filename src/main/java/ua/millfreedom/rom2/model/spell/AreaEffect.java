package ua.millfreedom.rom2.model.spell;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.CServerApp;
import ua.millfreedom.rom2.model.Effect;
import ua.millfreedom.rom2.model.Effect_DirectDamage;
import ua.millfreedom.rom2.model.TargetHandle;
import ua.millfreedom.rom2.model.Token;
import ua.millfreedom.rom2.model.column.SpellColumn;
import ua.millfreedom.rom2.model.enums.SpellId;
import ua.millfreedom.rom2.model.unit.Unit;

import java.io.IOException;

public class AreaEffect extends SpellEffect {
    private static final int[] DIRECTIONAL_POINTS_005F8400_X = {
            -2, -1, 0, 1, 2, -2, -1, 0, 1, 2
    };
    private static final int[] DIRECTIONAL_POINTS_005F8400_Y = {
            1, 1, 1, 1, 1, 0, 0, 0, 0, 0
    };
    private static final int[] DIRECTIONAL_POINTS_005F84A8_X = {
            -2, -1, 0, 1, 2, -1, 0, 1, 2
    };
    private static final int[] DIRECTIONAL_POINTS_005F84A8_Y = {
            2, 1, 0, -1, -2, 2, 1, 0, -1
    };
    private static final int MOVING_PATTERN_RESET_DURATION_TICKS = 2;
    private static final int MOVING_PATTERN_BLIZZARD_FRAME_LIMIT = 0x20;
    private static final int MOVING_PATTERN_BLIZZARD_VISUAL_TYPE = 0x10;
    private static final int MOVING_PATTERN_ACID_STREAM_VISUAL_TYPE = 0x12;
    private static final int[][] ACID_STREAM_POINTS1_005F8550_X = {
            {0},
            {-1, 0, 1},
            {-1, -1, 0, 1, 2},
            {-3, -2, -1, 0, 1, 2, 3},
            {-4, -3, -2, -1, 0, 1, 2, 3, 4},
            {}
    };
    private static final int[][] ACID_STREAM_POINTS1_005F8550_Y = {
            {0},
            {1, 1, 1},
            {2, 2, 2, 2, 2},
            {3, 3, 3, 3, 3, 3, 3},
            {4, 4, 4, 4, 4, 4, 4, 4, 4},
            {}
    };
    private static final int[][] ACID_STREAM_POINTS2_005F8928_X = {
            {0},
            {1, 0},
            {2, 1, 0},
            {3, 2, 1, 0},
            {4, 3, 2, 1, 0},
            {5, 4, 3, 2, 1, 0}
    };
    private static final int[][] ACID_STREAM_POINTS2_005F8928_Y = {
            {0},
            {0, 1},
            {0, 1, 2},
            {0, 1, 2, 3},
            {0, 1, 2, 3, 4},
            {0, 1, 2, 3, 4, 5}
    };

    //0x48
    public Effect payload;
    //0x4C
    public int mode;
    //0x4D
    public int radiusLengthHalf;
    //0x4E
    public int facing;
    //0x4F
    public int patternFrame;
    //0x50
    public int durationTicks;

    /**
     * Native: AreaEffect::New @00517BE0.
     * Fully ported.
     */
    public AreaEffect() {
        payload = null;
        scenarioObjectId = 0;
        mode = 0;
    }

    /**
     * Native: AreaEffect::AreaEffect @00517C1A.
     * Fully ported.
     */
    public AreaEffect(Effect payload, TargetHandle targetHandle, int radiusLengthHalf) {
        super(targetHandle);
        this.payload = payload;
        scenarioObjectId = 0;
        mode = 0;
        this.radiusLengthHalf = radiusLengthHalf & 0xFF;
        patternFrame = 0;
        durationTicks = 0;
    }

    /**
     * Native: AreaEffect::operator>> @00517BC4 (ReadObject path).
     * Fully ported.
     */
    public static AreaEffect readFromArchive(CArchive ar) throws IOException {
        return ar.readObject(AreaEffect.class);
    }

    /**
     * vtbl +0x08: AreaEffect::serialize @0051C88E.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        super.serialize(ar);
        if (!ar.isStoring()) {
            mode = ar.readByte() & 0xFF;
            radiusLengthHalf = ar.readByte() & 0xFF;
            facing = ar.readByte() & 0xFF;
            patternFrame = ar.readByte() & 0xFF;
            durationTicks = ar.readUShort();
            payload = Effect.readFromArchive(ar);
        } else {
            ar.writeByte(mode);
            ar.writeByte(radiusLengthHalf);
            ar.writeByte(facing);
            ar.writeByte(patternFrame);
            ar.writeShort(durationTicks);
            ar.writeObject(payload);
        }
    }

    /**
     * vtbl +0x24: AreaEffect::restoreContext @0051C96C.
     * Fully ported.
     */
    @Override
    public void restoreContext() {
        super.restoreContext();
        if (payload != null) {
            payload.restoreContext();
        }
    }

    /**
     * not ported.
     */
    @Override
    public SpellTokenKind getTokenKind() {
        return SpellTokenKind.SPELL;
    }

    /**
     * not ported.
     */
    @Override
    public int getSpellIdIndex() {
        return key & 0xFF;
    }

    /**
     * vtbl +0x18: AreaEffect::update @00517CFE.
     * Fully ported.
     */
    @Override
    public Object update() {
        if (hasScenarioObjectFlag2() != 0) {
            updateMovingPatternAreaEffect();
            return null;
        }
        if (hasScenarioObjectFlag1() == 0) {
            applyInstantAreaEffectFootprint();
            return null;
        }
        if (mode == 0) {
            activatePersistentAreaEffect();
            return null;
        }
        if ((short) durationTicks == 0) {
            removePersistentAreaEffect();
            completedFlag = 1;
            return null;
        }

        durationTicks = (durationTicks - 1) & 0xFFFF;
        if ((durationTicks & 0x0F) == 0) {
            applyPayloadToPrimaryUnitsOnActiveLayer();
        }
        return null;
    }

    /**
     * Native: AreaEffect::applyInstantAreaEffectFootprint @00518833.
     * Fully ported.
     */
    private void applyInstantAreaEffectFootprint() {
        CServerApp.sendSpellEffectStateAction(this, 1);
        int radius = radiusLengthHalf & 0xFF;
        int centerX = m_pTargetHandle.getX();
        int centerY = m_pTargetHandle.getY();
        boolean directDamageLayer = isPersistentDirectDamageAreaEffect();
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int x = centerX + dx;
                int y = centerY + dy;
                applyPayloadToAreaCellObjects(packCell(x, y));
                if (directDamageLayer) {
                    removeConflictingAreaEffectLayers(x, y);
                    Globals.worldMap.markDirectDamageAreaEffectCell(x, y);
                }
            }
        }
        completedFlag = 1;
    }

    /**
     * Native: AreaEffect::activatePersistentAreaEffect @00518024.
     * Fully ported.
     */
    private void activatePersistentAreaEffect() {
        if (Globals.staticDataMgr.spells.get(getSpellIdIndex())
                .getAttribute(SpellColumn.DISTRIBUTION_SYSTEM) == 4) {
            activateDirectionalPersistentAreaEffect();
        } else {
            activateRadiusPersistentAreaEffect();
        }
    }

    /**
     * Native: AreaEffect::removePersistentAreaEffect @00518594.
     * Fully ported.
     */
    private void removePersistentAreaEffect() {
        int radius = radiusLengthHalf & 0xFF;
        int centerX = m_pTargetHandle.getX();
        int centerY = m_pTargetHandle.getY();
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                Globals.worldMap.detachAreaEffectAtPoint(this, centerX + dx, centerY + dy);
            }
        }
        CServerApp.sendSpellEffectStateAction(this, 0);
        mode = 0;
    }

    /**
     * Native: AreaEffect::activateDirectionalPersistentAreaEffect @005182B6.
     * Fully ported.
     */
    private void activateDirectionalPersistentAreaEffect() {
        applyDirectionalAreaOffsetPattern(facing & 0xFF);
        CServerApp.sendSpellEffectStateAction(this, 1);
        mode = 1;
    }

    /**
     * Native: AreaEffect::activateRadiusPersistentAreaEffect @005183A2.
     * Fully ported.
     */
    private void activateRadiusPersistentAreaEffect() {
        int radius = radiusLengthHalf & 0xFF;
        int centerX = m_pTargetHandle.getX();
        int centerY = m_pTargetHandle.getY();
        boolean directDamageLayer = isPersistentDirectDamageAreaEffect();
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                if (Math.abs(dx) + Math.abs(dy) <= radius + 1) {
                    attachPersistentAreaEffectCell(centerX + dx, centerY + dy, directDamageLayer);
                }
            }
        }
        CServerApp.sendSpellEffectStateAction(this, 1);
        mode = 1;
    }

    /**
     * Native support extracted from AreaEffect::attachPersistentAreaEffectCell @005184BF.
     * Fully ported.
     */
    private void attachPersistentAreaEffectCell(int x, int y, boolean directDamageLayer) {
        int packedCell = packCell(x, y);
        if (Globals.worldMap.getBuildingAtPoint(x, y) != null
                && (Globals.worldMap.layer1_0x10000[packedCell] & 4) != 0) {
            return;
        }

        if (getSpellIdIndex() == SpellId.WALL_OF_EARTH.id) {
            if (Globals.worldMap.getGroundUnitAtCell(packedCell) == null) {
                Globals.worldMap.attachAreaEffectAtPoint(this, x, y);
            }
            return;
        }

        Globals.worldMap.attachAreaEffectAtPoint(this, x, y);
        removeConflictingAreaEffectLayers(x, y);
        if (directDamageLayer) {
            Globals.worldMap.markDirectDamageAreaEffectCell(x, y);
        }
    }

    /**
     * Native support extracted from AreaEffect::removeConflictingAreaEffectLayers @005189B5.
     * Fully ported.
     */
    private void removeConflictingAreaEffectLayers(int x, int y) {
        AreaEffect[] layers = Globals.worldMap.getAreaEffectLayersAtCell(packCell(x, y));
        if (layers == null) {
            return;
        }

        switch (SpellId.fromId(getSpellIdIndex())) {
            case FIRE_BALL, WALL_OF_FIRE -> {
                if (layers[2] != null) {
                    removeConflictingAreaEffectLayer(layers[2], x, y);
                }
                if (layers[1] != null) {
                    removeConflictingAreaEffectLayer(layers[1], x, y);
                }
            }
            case POISON_CLOUD -> {
                if (layers[0] != null) {
                    removeConflictingAreaEffectLayer(layers[2], x, y);
                }
            }
            case DARKNESS -> {
                if (layers[4] != null) {
                    Globals.worldMap.detachAreaEffectAtPoint(layers[5], x, y);
                    removeConflictingAreaEffectLayer(layers[4], x, y);
                }
            }
            case LIGHT -> {
                if (layers[5] != null) {
                    Globals.worldMap.detachAreaEffectAtPoint(layers[4], x, y);
                    removeConflictingAreaEffectLayer(layers[5], x, y);
                }
            }
            default -> {
            }
        }
    }

    /**
     * Native support extracted from AreaEffect::removeConflictingAreaEffectLayers @005189B5.
     * Fully ported.
     */
    private void removeConflictingAreaEffectLayer(AreaEffect areaEffect, int x, int y) {
        Globals.worldMap.detachAreaEffectAtPoint(areaEffect, x, y);
        CServerApp.sendSpellEffectStateAction(areaEffect, 0);
    }

    /**
     * Native support extracted from AreaEffect::activateDirectionalPersistentAreaEffect @005182B6,
     * AreaEffect::activateRadiusPersistentAreaEffect @005183A2, and
     * AreaEffect::applyInstantAreaEffectFootprint @00518833.
     * Fully ported.
     */
    private boolean isPersistentDirectDamageAreaEffect() {
        return payload instanceof Effect_DirectDamage directDamage
                && (directDamage.unitSkillData.skillDamageType2Modifier & 0xFF) != 0;
    }

    /**
     * Native support extracted from AreaOffsetPatternView::InitFromDirection @00517EC3.
     * Fully ported.
     */
    private void applyDirectionalAreaOffsetPattern(int facing) {
        switch (facing) {
            case 0 -> applyDirectionalAreaOffsets(DIRECTIONAL_POINTS_005F8400_X, DIRECTIONAL_POINTS_005F8400_Y, 1, -1);
            case 1 -> applyDirectionalAreaOffsets(DIRECTIONAL_POINTS_005F84A8_X, DIRECTIONAL_POINTS_005F84A8_Y, 1, -1);
            case 2 -> applyDirectionalAreaOffsets(DIRECTIONAL_POINTS_005F8400_Y, DIRECTIONAL_POINTS_005F8400_X, 1, 1);
            case 4 -> applyDirectionalAreaOffsets(DIRECTIONAL_POINTS_005F8400_X, DIRECTIONAL_POINTS_005F8400_Y, -1, 1);
            case 5 -> applyDirectionalAreaOffsets(DIRECTIONAL_POINTS_005F84A8_X, DIRECTIONAL_POINTS_005F84A8_Y, -1, 1);
            case 6 -> applyDirectionalAreaOffsets(DIRECTIONAL_POINTS_005F8400_Y, DIRECTIONAL_POINTS_005F8400_X, -1, 1);
            case 7 -> applyDirectionalAreaOffsets(DIRECTIONAL_POINTS_005F84A8_X, DIRECTIONAL_POINTS_005F84A8_Y, -1, -1);
            default -> applyDirectionalAreaOffsets(DIRECTIONAL_POINTS_005F84A8_X, DIRECTIONAL_POINTS_005F84A8_Y, 1, 1);
        }
    }

    /**
     * Native support extracted from AreaEffect::activateDirectionalPersistentAreaEffect @005182B6.
     * Fully ported.
     */
    private void applyDirectionalAreaOffsets(int[] xOffsets, int[] yOffsets, int xSign, int ySign) {
        int centerX = m_pTargetHandle.getX();
        int centerY = m_pTargetHandle.getY();
        boolean directDamageLayer = isPersistentDirectDamageAreaEffect();
        for (int i = 0; i < xOffsets.length; i++) {
            attachPersistentAreaEffectCell(
                    centerX + xSign * xOffsets[i],
                    centerY + ySign * yOffsets[i],
                    directDamageLayer
            );
        }
    }

    /**
     * Native: AreaEffect::updateMovingPatternAreaEffect @0051806C.
     * Fully ported.
     */
    private void updateMovingPatternAreaEffect() {
        int previousDurationTicks = durationTicks;
        durationTicks = (durationTicks - 1) & 0xFFFF;
        if ((short) previousDurationTicks != 0) {
            return;
        }

        durationTicks = MOVING_PATTERN_RESET_DURATION_TICKS;
        int centerX = m_pTargetHandle.getX();
        int centerY = m_pTargetHandle.getY();
        MovingAreaOffsetPattern pattern = movingAreaOffsetPattern();
        for (int i = 0; i < pattern.count(); i++) {
            int x = centerX + pattern.xSign() * pattern.dx()[i];
            int y = centerY + pattern.ySign() * pattern.dy()[i];
            payload.typeID = (getSpellIdIndex() * 2 + 9) & 0xFFFF;
            if (payload.m_pTargetHandle.setPosition(x, y)) {
                CServerApp.sendEffectTokenVisualAction(payload, pattern.visualType());
                applyPayloadToAreaCellObjects(packCell(x, y));
            }
        }

        patternFrame = (patternFrame + 1) & 0xFF;
        if (pattern.frameLimit() <= (patternFrame & 0xFF)) {
            completedFlag = 1;
        }
    }

    /**
     * Native support extracted from AreaEffect::updateMovingPatternAreaEffect @0051806C.
     * Fully ported.
     */
    private MovingAreaOffsetPattern movingAreaOffsetPattern() {
        return switch (SpellId.fromId(getSpellIdIndex())) {
            case BLIZZARD -> movingAreaOffsetPatternFromPoints(
                    new int[]{Utils.randInclusive(4) - 2},
                    new int[]{Utils.randInclusive(4) - 2},
                    MOVING_PATTERN_BLIZZARD_FRAME_LIMIT,
                    MOVING_PATTERN_BLIZZARD_VISUAL_TYPE
            );
            case ACID_STREAM -> directionalMovingAreaOffsetPattern(
                    ACID_STREAM_POINTS1_005F8550_X[patternFrame & 0xFF],
                    ACID_STREAM_POINTS1_005F8550_Y[patternFrame & 0xFF],
                    ACID_STREAM_POINTS2_005F8928_X[patternFrame & 0xFF],
                    ACID_STREAM_POINTS2_005F8928_Y[patternFrame & 0xFF],
                    facing & 0xFF
            );
            default -> throw new IllegalStateException("Unsupported moving area effect spell id " + getSpellIdIndex());
        };
    }

    /**
     * Native support extracted from AreaOffsetPatternView::InitFromPoints @00517E80.
     * Fully ported.
     */
    private static MovingAreaOffsetPattern movingAreaOffsetPatternFromPoints(
            int[] dx,
            int[] dy,
            int frameLimit,
            int visualType
    ) {
        return new MovingAreaOffsetPattern(dx, dy, Math.min(dx.length, dy.length), 1, 1, frameLimit, visualType);
    }

    /**
     * Native support extracted from AreaOffsetPatternView::InitFromDirection @00517EC3.
     * Fully ported.
     */
    private static MovingAreaOffsetPattern directionalMovingAreaOffsetPattern(
            int[] points1X,
            int[] points1Y,
            int[] points2X,
            int[] points2Y,
            int direction
    ) {
        int[] dx = points2X;
        int[] dy = points2Y;
        int xSign = 1;
        int ySign = 1;
        switch (direction) {
            case 0 -> {
                dx = points1X;
                dy = points1Y;
                ySign = -1;
            }
            case 1 -> ySign = -1;
            case 2 -> {
                dx = points1Y;
                dy = points1X;
            }
            case 4 -> {
                dx = points1X;
                dy = points1Y;
                xSign = -1;
            }
            case 5 -> xSign = -1;
            case 6 -> {
                dx = points1Y;
                dy = points1X;
                xSign = -1;
            }
            case 7 -> {
                xSign = -1;
                ySign = -1;
            }
            default -> {
            }
        }
        return new MovingAreaOffsetPattern(
                dx,
                dy,
                Math.min(dx.length, dy.length),
                xSign,
                ySign,
                ACID_STREAM_POINTS2_005F8928_X.length,
                MOVING_PATTERN_ACID_STREAM_VISUAL_TYPE
        );
    }

    /**
     * Native support extracted from AreaEffect::update @00517CFE periodic active-layer payload pass.
     * Fully ported.
     */
    private void applyPayloadToPrimaryUnitsOnActiveLayer() {
        int radius = radiusLengthHalf & 0xFF;
        int centerX = m_pTargetHandle.getX();
        int centerY = m_pTargetHandle.getY();
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int cell = packCell(centerX + dx, centerY + dy);
                if (Globals.worldMap.findAreaEffectAtLayerPoint(this, centerX + dx, centerY + dy) != null) {
                    applyPayloadToAreaCellObject(Globals.worldMap.getGroundUnitAtCell(cell));
                }
            }
        }
    }

    /**
     * Native support extracted from AreaEffect::updateMovingPatternAreaEffect @0051806C and
     * AreaEffect::applyInstantAreaEffectFootprint @00518833.
     * Fully ported.
     */
    private void applyPayloadToAreaCellObjects(int cell) {
        applyPayloadToAreaCellObject(Globals.worldMap.getGroundUnitAtCell(cell));
        applyPayloadToAreaCellObject(Globals.worldMap.getAirUnitAtCell(cell));
        applyPayloadToAreaCellObject(Globals.worldMap.getBuildingAtCell(cell));
    }

    /**
     * Native support extracted from AreaEffect::applyPayloadToAreaCellObject @00518681.
     * Fully ported.
     */
    private void applyPayloadToAreaCellObject(Token target) {
        if (target == null || getSpellIdIndex() == SpellId.WALL_OF_EARTH.id) {
            return;
        }

        if (getSpellIdIndex() == SpellId.FIRE_BALL.id) {
            applyScaledDirectDamageToAreaCellTarget(target);
        } else {
            payload.applyToTarget(target);
        }

        if (getSpellIdIndex() != SpellId.LIGHT.id) {
            applyDamageAttributionToAreaCellTarget(target);
        }
    }

    /**
     * Native support extracted from AreaEffect::applyPayloadToAreaCellObject @00518681.
     * Fully ported.
     */
    private void applyScaledDirectDamageToAreaCellTarget(Token target) {
        Effect_DirectDamage scaledPayload = new Effect_DirectDamage().copyFrom(payload);
        int targetSize = target.getTokenSizeVirtual() & 0xFF;
        int footprintArea = targetSize * targetSize;
        scaledPayload.unitSkillData.skillDamageType2Min =
                (byte) ((scaledPayload.unitSkillData.skillDamageType2Min & 0xFF) / footprintArea);
        scaledPayload.unitSkillData.skillDamageType2Modifier =
                (byte) ((scaledPayload.unitSkillData.skillDamageType2Modifier & 0xFF) / footprintArea);
        scaledPayload.applyToTarget(target);
    }

    /**
     * Native support extracted from AreaEffect::applyPayloadToAreaCellObject @00518681.
     * Fully ported.
     */
    private void applyDamageAttributionToAreaCellTarget(Token target) {
        if ((target.getMovementType() & 0xFF) <= 0 || sourceCaster == null) {
            return;
        }

        Unit targetUnit = (Unit) target;
        if (!sourceCaster.hasNoTableLine()) {
            if (sourceCaster.owner == null) {
                targetUnit.lastDamageSource = null;
            } else if (getSpellIdIndex() != SpellId.DARKNESS.id) {
                targetUnit.lastDamageSource = sourceCaster;
                targetUnit.killCreditSkillContext = payload.key & 0xFF;
            }
        } else {
            targetUnit.lastDamageSource = null;
        }

        if (sourceCaster.owner != null && targetUnit.owner != null) {
            Globals.gameServer.missionScriptRuntime.recordUnitEngagement(sourceCaster, targetUnit, 1);
        }
    }

    /**
     * Native support extracted from AreaEffect cell walkers @00517CFE, @0051806C, and @00518833.
     * Fully ported.
     */
    private static int packCell(int x, int y) {
        return (x & 0xFF) | ((y & 0xFF) << 8);
    }

    /**
     * vtbl +0x38: AreaEffect::applyPayloadToObject @0051865F.
     * Fully ported.
     */
    @Override
    public void applyPayloadToObject(Token target) {
        payload.applyToTarget(target);
    }

    /**
     * Native: AreaEffect::mapLayer @00518BA3.
     * Fully ported.
     */
    public int mapLayer() {
        return switch (SpellId.fromId(key & 0xFFFF)) {
            case WALL_OF_FIRE -> 0;
            case POISON_CLOUD -> 2;
            case WALL_OF_EARTH -> 3;
            case LIGHT -> 4;
            case DARKNESS -> 5;
            default -> 0;
        };
    }

    /**
     * Native: AreaEffect::appliesTerrainCostReduction @0055F350.
     * Fully ported.
     */
    public int appliesTerrainCostReduction() {
        return 1;
    }

    /**
     * Native support type for AreaOffsetPatternView used by AreaEffect::updateMovingPatternAreaEffect @0051806C.
     */
    private record MovingAreaOffsetPattern(
            int[] dx,
            int[] dy,
            int count,
            int xSign,
            int ySign,
            int frameLimit,
            int visualType
    ) {
    }
}
