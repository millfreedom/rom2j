package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `UnitTokenListAction` packet id `0x26` used to remove active point-target spell effects.
 */
public class CancelPointSpellEffectAction extends UnitTokenListAction {
    public static final int ACTION_ID = GameActionId.CANCEL_POINT_SPELL_EFFECT_ACTION_26.id;
    public static final CancelPointSpellEffectAction global = new CancelPointSpellEffectAction();

    //0x0A
    public final Property<Integer> targetCellX = u16(BODY_OFFSET);
    //0x0C
    public final Property<Integer> targetCellY = u16(BODY_OFFSET + Short.BYTES);
    //0x10
    public final Property<Integer> spellEffectIndex = u16(BODY_OFFSET + 6);

    /**
     * Native support extracted from MapVisualObject::IssueCastSpellAtPoint @0041918E.
     */
    public CancelPointSpellEffectAction() {
        super();
        ID.set(ACTION_ID);
        targetCellX.set(0);
        targetCellY.set(0);
        spellEffectIndex.set(0);
    }

    /**
     * Native support extracted from MapVisualObject::IssueCastSpellAtPoint @0041918E.
     */
    public CancelPointSpellEffectAction(CancelPointSpellEffectAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from MapVisualObject::IssueCastSpellAtPoint @0041918E.
     */
    @Override
    public CancelPointSpellEffectAction Clone() {
        return new CancelPointSpellEffectAction(this);
    }

}
