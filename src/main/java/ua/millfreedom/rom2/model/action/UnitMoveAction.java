package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.gameobj.CGameObject;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `UnitCommandStartAction` packet id `0x6B` used by
 * `CServerApp::sendUnitActionVisibilityGated @005039E2`
 * and `CServerApp::sendUnitCommandStartAction @005040C4` to broadcast unit move-action starts.
 */
public class UnitMoveAction extends UnitCommandStartAction {
    public static final int ACTION_ID = GameActionId.UNIT_MOVE_ACTION_6B.id;
    public static final UnitMoveAction global = new UnitMoveAction();

    // Native: INT_ARRAY_005f81c0 @005F81C0.
    private static final int[] MOVE_DELTA_X_BY_DIRECTION = {
            0, 0, 1, 0,
            1, 0, 1, 0,
            0, 0, -1, 0,
            -1, 0, -1, 0
    };

    // Native: INT_ARRAY_005f8200 @005F8200.
    private static final int[] MOVE_DELTA_Y_BY_DIRECTION = {
            -1, 0, -1, 0,
            0, 0, 1, 0,
            1, 0, 1, 0,
            0, 0, -1, 0
    };

    /**
     * Native support extracted from CServerApp::sendUnitActionVisibilityGated @005039E2,
     * CServerApp::sendUnitCommandStartAction @005040C4, and
     * Unit::FUN_0050FF2D @0050FF2D.
     */
    public UnitMoveAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from UnitCommandStartAction::UnitCommandStartAction @0050C236 and
     * CServerApp::sendUnitCommandStartAction @005040C4.
     */
    public UnitMoveAction(UnitMoveAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from UnitCommandStartAction::Clone @00541320.
     */
    @Override
    public UnitMoveAction Clone() {
        return new UnitMoveAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040DD1B.
     * Partial port. Java keeps the recovered move-command state mutation.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        CGameObject gameObject = mapVisualObject.getObjectByToken((short) (int) unitTokenId.get());
        if (gameObject != null && gameObject.HP > 0) {
            int direction = commandDirection.get();
            gameObject.actionSegments = commandSegments.get();
            gameObject.action = 1;
            gameObject.actionDir = (byte) direction;
            gameObject.actionX = resolveMoveDeltaX(direction) << 8;
            gameObject.actionY = resolveMoveDeltaY(direction) << 8;
            mapVisualObject.renderFrameDirty = 1;
            mapVisualObject.markMapOccupancyDirty();
        }
    }

    /**
     * Native support extracted from movement packet handler @00412D78 and movement retry routine @00510032.
     */
    private static int resolveMoveDeltaX(int commandDirection) {
        return MOVE_DELTA_X_BY_DIRECTION[(byte) commandDirection];
    }

    /**
     * Native support extracted from movement packet handler @00412D9B and movement retry routine @00510047.
     */
    private static int resolveMoveDeltaY(int commandDirection) {
        return MOVE_DELTA_Y_BY_DIRECTION[(byte) commandDirection];
    }

}
