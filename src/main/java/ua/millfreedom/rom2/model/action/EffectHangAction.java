package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.gameobj.CGameObject;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `Fixed3ByteAction` packet id `0x88` used by
 * `CServerApp::dispatchSpellEffectVisibilityGatedAction @00503BEF`
 * and `CServerApp::AddEffect @005044BA` to broadcast that a unit gained an effect entry.
 */
public class EffectHangAction extends Fixed3ByteAction {
    public static final int ACTION_ID = GameActionId.EFFECT_HANG_ACTION_88.id;
    public static final EffectHangAction global = new EffectHangAction();


    /**
     * Native support extracted from Effect::applyToTarget @0051CE12 and
     * CServerApp::AddEffect @005044BA.
     */
    public EffectHangAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from Fixed3ByteAction::Fixed3ByteAction @0050C3FD and
     * CServerApp::AddEffect @005044BA.
     */
    public EffectHangAction(EffectHangAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from Fixed3ByteAction::Clone @00541560.
     */
    @Override
    public EffectHangAction Clone() {
        return new EffectHangAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00411F5F.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        CGameObject target = mapVisualObject.getObjectByToken((short) (int) unitTokenId.get());
        if (target == null) {
            return;
        }
        CUnit targetUnit = (CUnit) target;
        int effectType = effectTypeId.get();
        int packedEffect = (effectType << 16) | 0xFFFF;
        int existingIndex = targetUnit.findPackedEffectIndex(effectType);
        if (existingIndex < 0) {
            targetUnit.dwarr_130.add(packedEffect);
        } else {
            targetUnit.dwarr_130.set(existingIndex, packedEffect);
        }
        targetUnit.m_bSelectionDirty = 1;
    }

}
