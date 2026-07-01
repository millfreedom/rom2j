package ua.millfreedom.rom2.model.world.node;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.Building;
import ua.millfreedom.rom2.model.Sack;
import ua.millfreedom.rom2.model.spell.AreaEffect;
import ua.millfreedom.rom2.model.spell.TransientSpellCastSpec;
import ua.millfreedom.rom2.model.unit.Unit;

import java.io.IOException;
import java.util.Arrays;

/**
 * Native WorldMapNode struct serialized by SerializeElements<WorldMapNode> @0055FFF0.
 */
public final class WorldMapNode implements MfcSerializable {
    //0x00
    public int layer0Cell;
    //0x01
    public int layer1Cell;
    //0x02
    public int effectsCount;
    //0x03
    public int reserved0x03;
    //0x04
    public Unit groundOccupancyUnit;
    // Java-only restore token for native +0x04 before WorldMapNode::restoreContext @005596FF resolves the pointer map.
    public Object groundOccupancyUnitRestoreToken;
    //0x08
    public Unit airOccupancyUnit;
    // Java-only restore token for native +0x08 before WorldMapNode::restoreContext @005596FF resolves the pointer map.
    public Object airOccupancyUnitRestoreToken;
    //0x0c
    public Building building;
    // Java-only restore token for native +0x0C before WorldMapNode::restoreContext @005596FF resolves the pointer map.
    public Object buildingRestoreToken;
    //0x10
    public Sack sack;
    // Java-only restore token for native +0x10 before WorldMapNode::restoreContext @005596FF resolves the pointer map.
    public Object sackRestoreToken;
    //0x14
    public final AreaEffect[] effectLayer = new AreaEffect[6];
    // Java-only restore tokens for native +0x14 AreaEffect *[6] before WorldMapNode::restoreContext @005596FF resolves the pointer map.
    public final Object[] effectLayerRestoreTokens = new Object[6];
    //0x2c
    public Unit primaryLayerStateUnit;
    // Java-only restore token for native +0x2C before WorldMapNode::restoreContext @005596FF resolves the pointer map.
    public Object primaryLayerStateUnitRestoreToken;
    //0x30
    public Unit secondaryLayerStateUnit;
    // Java-only restore token for native +0x30 before WorldMapNode::restoreContext @005596FF resolves the pointer map.
    public Object secondaryLayerStateUnitRestoreToken;
    //0x34
    public final TransientSpellCastSpec transientSpellCastSpec = new TransientSpellCastSpec();
    //0x3a
    public int key;

    /**
     * Native: WorldMapNode::WorldMapNode @0054F84C.
     * Fully ported.
     */
    public WorldMapNode() {
        clear();
    }

    /**
     * Native: WorldMapNode::clear @0054F89A.
     * Fully ported.
     */
    public void clear() {
        layer0Cell = 0;
        layer1Cell = 0;
        effectsCount = 0;
        reserved0x03 = 0;
        groundOccupancyUnit = null;
        groundOccupancyUnitRestoreToken = null;
        airOccupancyUnit = null;
        airOccupancyUnitRestoreToken = null;
        building = null;
        buildingRestoreToken = null;
        sack = null;
        sackRestoreToken = null;
        Arrays.fill(effectLayer, null);
        Arrays.fill(effectLayerRestoreTokens, null);
        primaryLayerStateUnit = null;
        primaryLayerStateUnitRestoreToken = null;
        secondaryLayerStateUnit = null;
        secondaryLayerStateUnitRestoreToken = null;
        transientSpellCastSpec.clear();
        key = 0;
    }

    /**
     * Native: WorldMapNode::copyFrom @0055385F.
     * Fully ported.
     */
    public WorldMapNode copyFrom(WorldMapNode from) {
        layer0Cell = from.layer0Cell;
        layer1Cell = from.layer1Cell;
        effectsCount = from.effectsCount;
        reserved0x03 = from.reserved0x03;
        groundOccupancyUnit = from.groundOccupancyUnit;
        groundOccupancyUnitRestoreToken = from.groundOccupancyUnitRestoreToken;
        airOccupancyUnit = from.airOccupancyUnit;
        airOccupancyUnitRestoreToken = from.airOccupancyUnitRestoreToken;
        building = from.building;
        buildingRestoreToken = from.buildingRestoreToken;
        sack = from.sack;
        sackRestoreToken = from.sackRestoreToken;
        System.arraycopy(from.effectLayer, 0, effectLayer, 0, effectLayer.length);
        System.arraycopy(from.effectLayerRestoreTokens, 0, effectLayerRestoreTokens, 0, effectLayerRestoreTokens.length);
        primaryLayerStateUnit = from.primaryLayerStateUnit;
        primaryLayerStateUnitRestoreToken = from.primaryLayerStateUnitRestoreToken;
        secondaryLayerStateUnit = from.secondaryLayerStateUnit;
        secondaryLayerStateUnitRestoreToken = from.secondaryLayerStateUnitRestoreToken;
        transientSpellCastSpec.copyFrom(from.transientSpellCastSpec);
        key = from.key;
        return this;
    }

    /**
     * Native support extracted from CWorldMap::createDynamicNodeForCell @005508A0.
     */
    public int getLayer0Cell() {
        return layer0Cell & 0xFF;
    }

    /**
     * Native support extracted from CWorldMap::createDynamicNodeForCell @005508A0.
     */
    public void setLayer0Cell(int value) {
        layer0Cell = value & 0xFF;
    }

    /**
     * Native support extracted from CWorldMap::createDynamicNodeForCell @005508A0.
     */
    public int getLayer1Cell() {
        return layer1Cell & 0xFF;
    }

    /**
     * Native support extracted from CWorldMap::createDynamicNodeForCell @005508A0.
     */
    public void setLayer1Cell(int value) {
        layer1Cell = value & 0xFF;
    }

    /**
     * Native support extracted from CWorldMap::attachAreaEffectAtCell @0055A37A,
     * CWorldMap::detachAreaEffectAtCell @0055A533, and WorldMapNode::isEmpty @005513B6.
     */
    public int getEffectsCount() {
        return effectsCount & 0xFF;
    }

    /**
     * Native support extracted from CWorldMap::attachAreaEffectAtCell @0055A37A and
     * CWorldMap::detachAreaEffectAtCell @0055A533.
     */
    public void recalculateEffectsCount() {
        int count = 0;
        for (AreaEffect areaEffect : effectLayer) {
            if (areaEffect != null) {
                count++;
            }
        }
        effectsCount = count;
    }

    /**
     * Native support extracted from CWorldMap::createDynamicNodeForCell @005508A0 and
     * CWorldMap::refreshNodeLayers @00550A96.
     */
    public int getKey() {
        return key & 0xFFFF;
    }

    /**
     * Native support extracted from CWorldMap::createDynamicNodeForCell @005508A0.
     */
    public void setKey(int key) {
        this.key = key & 0xFFFF;
    }

    /**
     * Native: WorldMapNode::isEmpty @005513B6.
     * Fully ported.
     */
    public boolean isEmpty() {
        return groundOccupancyUnit == null
                && airOccupancyUnit == null
                && building == null
                && sack == null
                && getEffectsCount() == 0
                && primaryLayerStateUnit == null
                && secondaryLayerStateUnit == null
                && getTransientSpellCastId() == 0;
    }

    /**
     * Native support extracted from CWorldMap::setTransientSpellCastAtCell @0055BA36.
     */
    public boolean hasTransientSpellCastSpec() {
        return transientSpellCastSpec.spellId != 0;
    }

    /**
     * Native support extracted from CWorldMap::updateUnitMissionPathProgress @00555B38 transient spec read.
     */
    public int getTransientSpellCastId() {
        return transientSpellCastSpec.spellId & 0xFF;
    }

    /**
     * Native support extracted from CWorldMap::attachUnitToNodeCell @005503AF transient spec read.
     */
    public int getTransientSpellCastSkillLevel() {
        return transientSpellCastSpec.skillLevel & 0xFF;
    }

    /**
     * Native support extracted from CWorldMap::attachUnitToNodeCell @005503AF transient spec read.
     */
    public int getTransientSpellCastSourceX() {
        return transientSpellCastSpec.sourceX & 0xFF;
    }

    /**
     * Native support extracted from CWorldMap::attachUnitToNodeCell @005503AF transient spec read.
     */
    public int getTransientSpellCastSourceY() {
        return transientSpellCastSpec.sourceY & 0xFF;
    }

    /**
     * Native support extracted from CWorldMap::updateUnitMissionPathProgress @00555B38 transient spec read.
     */
    public int getTransientSpellCastTargetX() {
        return transientSpellCastSpec.targetX & 0xFF;
    }

    /**
     * Native support extracted from CWorldMap::updateUnitMissionPathProgress @00555B38 transient spec read.
     */
    public int getTransientSpellCastTargetY() {
        return transientSpellCastSpec.targetY & 0xFF;
    }

    /**
     * Native support extracted from CWorldMap::setTransientSpellCastAtCell @0055BA36 and
     * the six-byte TransientSpellCastSpec copy helper @0055B978 into WorldMapNode +0x34.
     */
    public void setTransientSpellCastSpec(TransientSpellCastSpec spec) {
        transientSpellCastSpec.copyFrom(spec);
    }

    /**
     * Native support extracted from SerializeElements<WorldMapNode> @0055FFF0.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        if (!ar.isStoring()) {
            layer0Cell = ar.readByte() & 0xFF;
            layer1Cell = ar.readByte() & 0xFF;
            effectsCount = ar.readByte() & 0xFF;
            reserved0x03 = ar.readByte() & 0xFF;
            groundOccupancyUnit = null;
            groundOccupancyUnitRestoreToken = readPointerToken(ar);
            airOccupancyUnit = null;
            airOccupancyUnitRestoreToken = readPointerToken(ar);
            building = null;
            buildingRestoreToken = readPointerToken(ar);
            sack = null;
            sackRestoreToken = readPointerToken(ar);
            Arrays.fill(effectLayer, null);
            for (int i = 0; i < effectLayer.length; i++) {
                effectLayerRestoreTokens[i] = readPointerToken(ar);
            }
            primaryLayerStateUnit = null;
            primaryLayerStateUnitRestoreToken = readPointerToken(ar);
            secondaryLayerStateUnit = null;
            secondaryLayerStateUnitRestoreToken = readPointerToken(ar);
            transientSpellCastSpec.serialize(ar);
            key = ar.readUShort();
        } else {
            ar.writeByte(layer0Cell);
            ar.writeByte(layer1Cell);
            ar.writeByte(effectsCount);
            ar.writeByte(reserved0x03);
            writePointerToken(ar, pointerValue(groundOccupancyUnit, groundOccupancyUnitRestoreToken));
            writePointerToken(ar, pointerValue(airOccupancyUnit, airOccupancyUnitRestoreToken));
            writePointerToken(ar, pointerValue(building, buildingRestoreToken));
            writePointerToken(ar, pointerValue(sack, sackRestoreToken));
            for (int i = 0; i < effectLayer.length; i++) {
                writePointerToken(ar, pointerValue(effectLayer[i], effectLayerRestoreTokens[i]));
            }
            writePointerToken(ar, pointerValue(primaryLayerStateUnit, primaryLayerStateUnitRestoreToken));
            writePointerToken(ar, pointerValue(secondaryLayerStateUnit, secondaryLayerStateUnitRestoreToken));
            transientSpellCastSpec.serialize(ar);
            ar.writeShort(key);
        }
    }

    /**
     * Native: WorldMapNode::restoreContext @005596FF.
     * Fully ported.
     */
    public void restoreContext() {
        Object resolvedGroundOccupancyUnit = resolvePointerToken(groundOccupancyUnitRestoreToken, groundOccupancyUnit);
        groundOccupancyUnit = castResolvedPointer(Unit.class, resolvedGroundOccupancyUnit);
        groundOccupancyUnitRestoreToken = unresolvedPointerToken(resolvedGroundOccupancyUnit);
        Object resolvedAirOccupancyUnit = resolvePointerToken(airOccupancyUnitRestoreToken, airOccupancyUnit);
        airOccupancyUnit = castResolvedPointer(Unit.class, resolvedAirOccupancyUnit);
        airOccupancyUnitRestoreToken = unresolvedPointerToken(resolvedAirOccupancyUnit);
        Object resolvedBuilding = resolvePointerToken(buildingRestoreToken, building);
        building = castResolvedPointer(Building.class, resolvedBuilding);
        buildingRestoreToken = unresolvedPointerToken(resolvedBuilding);
        Object resolvedSack = resolvePointerToken(sackRestoreToken, sack);
        sack = castResolvedPointer(Sack.class, resolvedSack);
        sackRestoreToken = unresolvedPointerToken(resolvedSack);
        Object resolvedPrimaryLayerStateUnit = resolvePointerToken(primaryLayerStateUnitRestoreToken, primaryLayerStateUnit);
        primaryLayerStateUnit = castResolvedPointer(Unit.class, resolvedPrimaryLayerStateUnit);
        primaryLayerStateUnitRestoreToken = unresolvedPointerToken(resolvedPrimaryLayerStateUnit);
        Object resolvedSecondaryLayerStateUnit = resolvePointerToken(secondaryLayerStateUnitRestoreToken, secondaryLayerStateUnit);
        secondaryLayerStateUnit = castResolvedPointer(Unit.class, resolvedSecondaryLayerStateUnit);
        secondaryLayerStateUnitRestoreToken = unresolvedPointerToken(resolvedSecondaryLayerStateUnit);
        for (int i = 0; i < effectLayer.length; i++) {
            Object resolvedEffectLayer = resolvePointerToken(effectLayerRestoreTokens[i], effectLayer[i]);
            effectLayer[i] = castResolvedPointer(AreaEffect.class, resolvedEffectLayer);
            effectLayerRestoreTokens[i] = unresolvedPointerToken(resolvedEffectLayer);
        }
    }

    /**
     * Native support extracted from WorldMapNode::restoreContext @005596FF pointer-map slot resolution.
     */
    private static Object resolvePointerToken(Object restoreToken, Object value) {
        return Globals.gameServer.lookupPointerMapOrKeepToken(pointerValue(value, restoreToken));
    }

    /**
     * Native support extracted from WorldMapNode::restoreContext @005596FF unresolved pointer-token preservation.
     */
    private static Object unresolvedPointerToken(Object resolved) {
        return resolved instanceof Number ? resolved : null;
    }

    /**
     * Native support extracted from WorldMapNode::restoreContext @005596FF typed pointer-slot restoration.
     */
    private static <T> T castResolvedPointer(Class<T> type, Object resolved) {
        if (resolved == null || resolved instanceof Number) {
            return null;
        }
        return type.cast(resolved);
    }

    /**
     * Native support extracted from SerializeElements<WorldMapNode> @0055FFF0 pointer-slot reads.
     */
    private static Object readPointerToken(CArchive ar) throws IOException {
        int token = ar.readInt();
        return token == 0 ? null : token;
    }

    /**
     * Native support extracted from SerializeElements<WorldMapNode> @0055FFF0 pointer-slot writes.
     */
    private static Object pointerValue(Object value, Object restoreToken) {
        return value != null ? value : restoreToken;
    }

    /**
     * Native support extracted from SerializeElements<WorldMapNode> @0055FFF0 pointer-slot writes.
     */
    private static void writePointerToken(CArchive ar, Object value) throws IOException {
        ar.writeInt(Utils.encodePointerLike(value));
    }
}
