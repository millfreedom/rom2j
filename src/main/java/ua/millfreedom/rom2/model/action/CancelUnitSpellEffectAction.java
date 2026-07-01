package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `UnitTokenListAction` packet id `0x25` used to remove active unit-target spell effects.
 */
public class CancelUnitSpellEffectAction extends UnitTokenListAction {
    public static final int ACTION_ID = GameActionId.CANCEL_UNIT_SPELL_EFFECT_ACTION_25.id;
    public static final CancelUnitSpellEffectAction global = new CancelUnitSpellEffectAction();

    //0x0E
    public final Property<Integer> targetTokenId = u16(BODY_OFFSET + 4);
    //0x10
    public final Property<Integer> spellEffectIndex = u16(BODY_OFFSET + 6);

    /**
     * Native support extracted from MapVisualObject::IssueCastSpellAtUnit @004195FA and
     * MapVisualObject::ActivateSpellPanelSlot @00419A54.
     */
    public CancelUnitSpellEffectAction() {
        super();
        ID.set(ACTION_ID);
        targetTokenId.set(0);
        spellEffectIndex.set(0);
    }

    /**
     * Native support extracted from MapVisualObject::IssueCastSpellAtUnit @004195FA and
     * MapVisualObject::ActivateSpellPanelSlot @00419A54.
     */
    public CancelUnitSpellEffectAction(CancelUnitSpellEffectAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from MapVisualObject::IssueCastSpellAtUnit @004195FA and
     * MapVisualObject::ActivateSpellPanelSlot @00419A54.
     */
    @Override
    public CancelUnitSpellEffectAction Clone() {
        return new CancelUnitSpellEffectAction(this);
    }

}
