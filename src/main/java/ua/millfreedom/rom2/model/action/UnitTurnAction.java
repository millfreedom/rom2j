package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.gameobj.CGameObject;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `UnitCommandStartAction` packet id `0x6D` used by
 * `CServerApp::sendUnitActionVisibilityGated @005039E2`
 * and `Unit::FUN_0050FF2D @0050FF2D` to broadcast unit turn-action starts.
 */
public class UnitTurnAction extends UnitCommandStartAction {
    public static final int ACTION_ID = GameActionId.UNIT_TURN_ACTION_6D.id;
    public static final UnitTurnAction global = new UnitTurnAction();

    /**
     * Native support extracted from CServerApp::sendUnitActionVisibilityGated @005039E2,
     * CServerApp::sendUnitCommandStartAction @005040C4, and
     * Unit::FUN_0050FF2D @0050FF2D.
     */
    public UnitTurnAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from UnitCommandStartAction::UnitCommandStartAction @0050C236 and
     * Unit::FUN_0050FF2D @0050FF2D.
     */
    public UnitTurnAction(UnitTurnAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from UnitCommandStartAction::Clone @00541320.
     */
    @Override
    public UnitTurnAction Clone() {
        return new UnitTurnAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00415E1F.
     * Partial port. Java keeps the recovered turn-command state mutation.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        CGameObject gameObject = mapVisualObject.getObjectByToken((short) (int) unitTokenId.get());
        if (gameObject != null && gameObject.HP > 0) {
            gameObject.actionSegments = commandSegments.get();
            gameObject.action = 5;
            gameObject.actionDir = (byte) (int) commandDirection.get();
            gameObject.field44_0xc4 = gameObject.dir << 4;
            gameObject.actionPhase = 0;
            gameObject.field40_0xa4 = 0;
            gameObject.field39_0xa0 = 0;
            mapVisualObject.renderFrameDirty = 1;
        }
    }

}
