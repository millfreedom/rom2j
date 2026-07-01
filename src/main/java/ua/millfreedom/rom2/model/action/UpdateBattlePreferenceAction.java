package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `TwoDwordAction` packet id `0x46` used to synchronize map-level battle preference settings.
 */
public class UpdateBattlePreferenceAction extends TwoDwordAction {
    public static final int ACTION_ID = GameActionId.UPDATE_BATTLE_PREFERENCE_ACTION_46.id;
    public static final UpdateBattlePreferenceAction global = new UpdateBattlePreferenceAction();

    /**
     * Native support extracted from MapVisualObject::FUN_0041A4EF @0041A4EF,
     * MapVisualObject::FUN_0041A617 @0041A617, and MapVisualObject::FUN_0041A5B9 @0041A5B9.
     */
    public UpdateBattlePreferenceAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::FUN_0041A4EF @0041A4EF,
     * MapVisualObject::FUN_0041A617 @0041A617, and MapVisualObject::FUN_0041A5B9 @0041A5B9.
     */
    public UpdateBattlePreferenceAction(UpdateBattlePreferenceAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from MapVisualObject::FUN_0041A4EF @0041A4EF,
     * MapVisualObject::FUN_0041A617 @0041A617, and MapVisualObject::FUN_0041A5B9 @0041A5B9.
     */
    @Override
    public UpdateBattlePreferenceAction Clone() {
        return new UpdateBattlePreferenceAction(this);
    }

}
