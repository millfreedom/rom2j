package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.gameobj.CGameObject;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `Fixed3ByteAction` packet id `0x89` used by
 * `CServerApp::dispatchSpellEffectVisibilityGatedAction @00503BEF`
 * and `CServerApp::notifyEffectRemoved @00504498` to broadcast that a unit lost an effect entry.
 */
public class EffectGoneAction extends Fixed3ByteAction {
    public static final int ACTION_ID = GameActionId.EFFECT_GONE_ACTION_89.id;
    public static final EffectGoneAction global = new EffectGoneAction();


    /**
     * Native support extracted from Effect::applyToTarget @0051CE12,
     * CServerApp::notifyEffectRemoved @00504498, and
     * CServerApp::AddEffect @005044BA.
     */
    public EffectGoneAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from Fixed3ByteAction::Fixed3ByteAction @0050C3FD and
     * CServerApp::notifyEffectRemoved @00504498.
     */
    public EffectGoneAction(EffectGoneAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from Fixed3ByteAction::Clone @00541560.
     */
    @Override
    public EffectGoneAction Clone() {
        return new EffectGoneAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0041202B.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        CGameObject target = mapVisualObject.getObjectByToken((short) (int) unitTokenId.get());
        if (target == null) {
            return;
        }
        CUnit targetUnit = (CUnit) target;
        int effectType = effectTypeId.get();
        int existingIndex = targetUnit.findPackedEffectIndex(effectType);
        if (existingIndex >= 0) {
            targetUnit.dwarr_130.remove(existingIndex);
        }
        targetUnit.m_bSelectionDirty = 1;
    }
}
