package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.actiondata.ActionPayloads;
import ua.millfreedom.rom2.model.CUnitInfo;
import ua.millfreedom.rom2.model.UnitTypes;
import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.gameobj.CGameObject;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `ShortArrayBlobAction` packet family used by
 * `CServerApp::dispatchSpellEffectVisibilityGatedAction @00503BEF`
 * and `Spell::CastPrismaticSpray @00519C04` to broadcast the prismatic-spray source plus hit targets.
 */
public class EffectMultiTargetAction extends ShortArrayBlobAction {
    public static final int ACTION_ID = GameActionId.EFFECT_MULTI_TARGET_ACTION_8A.id;
    public static final EffectMultiTargetAction global = new EffectMultiTargetAction();
    // Native visual spell/effect type used by MapVisualObject::HandleGameAction @00411CB9.
    private static final int PRISMATIC_SPRAY_VISUAL_ID = 0x1E;

    /**
     * Native support extracted from CServerApp::dispatchSpellTargets @00504391 and
     * Spell::CastPrismaticSpray @00519C04.
     */
    public EffectMultiTargetAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from ShortArrayBlobAction::ShortArrayBlobAction @0050C745 and
     * CServerApp::dispatchSpellTargets @00504391.
     */
    public EffectMultiTargetAction(EffectMultiTargetAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from CServerApp::dispatchSpellTargets @00504391 packet field writes.
     */
    public static EffectMultiTargetAction prepareForSpellTargets(Unit caster, CustomList<Unit> targets) {
        return prepareSpellTargetsAction(global, ACTION_ID, caster, targets);
    }

    /**
     * Native support extracted from CServerApp::dispatchSpellTargets @00504391 packet field writes shared by
     * `EffectMultiTargetAction` and `EffectMultiFromAction`.
     */
    static <T extends ShortArrayBlobAction> T prepareSpellTargetsAction(
            T action,
            int actionId,
            Unit caster,
            CustomList<Unit> targets
    ) {
        action.ID.set(actionId);
        action.playerID.set(0);
        ActionPayloads.setShortArray(action.shortValueCount, action.shortValues, buildSpellTargetsPayload(caster, targets));
        return action;
    }

    /**
     * Native support extracted from ShortArrayBlobAction::Clone @00541940.
     */
    @Override
    public EffectMultiTargetAction Clone() {
        return new EffectMultiTargetAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00411CB9.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        short[] decodedValues = MapVisualObject.decodeShortValues(shortValueCount.get(), shortValues.get());
        CGameObject caster = mapVisualObject.getObjectByToken(decodedValues[0]);
        if (caster == null) {
            return;
        }
        CUnitInfo unitInfo = UnitTypes.getUnitInfo(caster.type);
        if (caster.actionSegments == 0 && unitInfo.m_AttackPhases != 0) {
            caster.actionSegments = unitInfo.m_AttackFrameSequenceCount;
            caster.action = 8;
            caster.actionPhase = 0;
            caster.actionTargets.clear();
            for (int i = 1; i < decodedValues.length; i++) {
                caster.actionTargets.add(decodedValues[i]);
            }
            caster.actionTarget = decodedValues[1];
            caster.actionSpell = PRISMATIC_SPRAY_VISUAL_ID;
            mapVisualObject.renderFrameDirty = 1;
        }
    }

    /**
     * Native support extracted from CServerApp::dispatchSpellTargets @00504391.
     */
    private static short[] buildSpellTargetsPayload(Unit caster, CustomList<Unit> targets) {
        short[] payload = new short[targets.size() + 1];
        payload[0] = (short) caster.idFull;
        if (caster.getTokenTypeId() == 0) {
            payload[0] = (short) ((caster.m_pTargetHandle.getX() & 0xFF)
                    | ((caster.m_pTargetHandle.getY() & 0xFF) << 8));
        }
        int payloadIndex = 1;
        for (Unit target : targets) {
            payload[payloadIndex++] = (short) target.idFull;
        }
        return payload;
    }

}
