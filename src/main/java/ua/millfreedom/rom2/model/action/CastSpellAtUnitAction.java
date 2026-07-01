package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `UnitTokenListAction` packet id `0x1E` used for unit-target spell casts over a list of unit tokens.
 */
public class CastSpellAtUnitAction extends UnitTokenListAction {
    public static final int ACTION_ID = GameActionId.CAST_SPELL_AT_UNIT_ACTION_1E.id;
    public static final CastSpellAtUnitAction global = new CastSpellAtUnitAction();

    //0x0E
    public final Property<Integer> targetTokenId = u16(BODY_OFFSET + 4);
    //0x10
    public final Property<Integer> spellSlot = u8(BODY_OFFSET + 6);

    /**
     * Native support extracted from MapVisualObject::IssueCastSpellAtUnit @004195FA and
     * MapVisualObject::ActivateSpellPanelSlot @00419A54.
     */
    public CastSpellAtUnitAction() {
        super();
        ID.set(ACTION_ID);
        targetTokenId.set(0);
        spellSlot.set(0);
    }

    /**
     * Native support extracted from MapVisualObject::IssueCastSpellAtUnit @004195FA and
     * MapVisualObject::ActivateSpellPanelSlot @00419A54.
     */
    public CastSpellAtUnitAction(CastSpellAtUnitAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from MapVisualObject::IssueCastSpellAtUnit @004195FA and
     * MapVisualObject::ActivateSpellPanelSlot @00419A54.
     */
    @Override
    public CastSpellAtUnitAction Clone() {
        return new CastSpellAtUnitAction(this);
    }

}
