package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `CGameAction` packet id `0x4C` used by hotkey `U` to trigger the stuck-hero self-damage/revive flow.
 */
public class ReviveStuckHeroAction extends CGameAction {
    public static final int ACTION_ID = GameActionId.REVIVE_STUCK_HERO_ACTION_4C.id;
    public static final ReviveStuckHeroAction global = new ReviveStuckHeroAction();

    /**
     * Native support extracted from MapVisualObject::sendReviveStuckHeroAction @0041C851 and
     * MapVisualObject::OnKeyDown @0040C8A0.
     */
    public ReviveStuckHeroAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::sendReviveStuckHeroAction @0041C851 and
     * MapVisualObject::OnKeyDown @0040C8A0.
     */
    public ReviveStuckHeroAction(ReviveStuckHeroAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::sendReviveStuckHeroAction @0041C851 and
     * MapVisualObject::OnKeyDown @0040C8A0.
     */
    @Override
    public ReviveStuckHeroAction Clone() {
        return new ReviveStuckHeroAction(this);
    }
}
