package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `UnitTokenListAction` packet id `0x1F` used for point-target spell casts over a list of unit tokens.
 */
public class CastSpellAtPointAction extends UnitTokenListAction {
    public static final int ACTION_ID = GameActionId.CAST_SPELL_AT_POINT_ACTION_1F.id;
    public static final CastSpellAtPointAction global = new CastSpellAtPointAction();

    //0x0A
    public final Property<Integer> targetCellX = u16(BODY_OFFSET);
    //0x0C
    public final Property<Integer> targetCellY = u16(BODY_OFFSET + Short.BYTES);
    //0x10
    public final Property<Integer> spellSlot = u8(BODY_OFFSET + 6);

    /**
     * Native support extracted from MapVisualObject::IssueCastSpellAtPoint @0041918E.
     */
    public CastSpellAtPointAction() {
        super();
        ID.set(ACTION_ID);
        targetCellX.set(0);
        targetCellY.set(0);
        spellSlot.set(0);
    }

    /**
     * Native support extracted from MapVisualObject::IssueCastSpellAtPoint @0041918E.
     */
    public CastSpellAtPointAction(CastSpellAtPointAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from MapVisualObject::IssueCastSpellAtPoint @0041918E.
     */
    @Override
    public CastSpellAtPointAction Clone() {
        return new CastSpellAtPointAction(this);
    }

}
