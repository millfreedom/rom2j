package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `EffectAction` wire-shape packet id `0x8B` handled by MapVisualObject::HandleGameAction @004119FC.
 */
public class EffectFromAction extends EffectAction {
    public static final int ACTION_ID = GameActionId.EFFECT_FROM_ACTION_8B.id;
    public static final EffectFromAction global = new EffectFromAction();

    /**
     * Native support extracted from EffectAction::EffectAction @0050C32F and MapVisualObject::HandleGameAction @004119FC.
     */
    public EffectFromAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from EffectAction::EffectAction @0050C36A and MapVisualObject::HandleGameAction @004119FC.
     */
    public EffectFromAction(EffectFromAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @004119FC.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        byte[] actionPayload = payload.get();
        int projectileType = MapVisualObject.u8(actionPayload, 2);
        int targetPacked = MapVisualObject.u16(actionPayload, 3);
        short targetToken = 0;
        int targetX = MapVisualObject.pixelCenterFromTile(MapVisualObject.u8(actionPayload, 3));
        int targetY = MapVisualObject.pixelCenterFromTile(MapVisualObject.u8(actionPayload, 4));
        if (MapVisualObject.isHomingProjectile(projectileType)) {
            targetX = 0;
            targetY = 0;
            targetToken = (short) targetPacked;
        }
        mapVisualObject.spawnProjectile(
                MapVisualObject.pixelCenterFromTile(MapVisualObject.u8(actionPayload, 0)),
                MapVisualObject.pixelCenterFromTile(MapVisualObject.u8(actionPayload, 1)),
                targetX,
                targetY,
                projectileType,
                MapVisualObject.u8(actionPayload, 5),
                targetToken
        );
    }

    /**
     * Native support extracted from EffectAction::Clone @005414A0.
     */
    @Override
    public EffectFromAction Clone() {
        return new EffectFromAction(this);
    }
}
