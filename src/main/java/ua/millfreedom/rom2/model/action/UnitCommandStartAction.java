package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.unit.Unit;

/**
 * Shared native `UnitCommandStartAction` wire-shape root for `UnitMoveAction (0x6B)`,
 * `UnitTurnAction (0x6D)`, and `UnitAttackAction (0x71)`.
 */
public class UnitCommandStartAction extends CGameAction {
    public static final int ACTION_ID = GameActionId.UNIT_MOVE_ACTION_6B.id;
    public static final UnitCommandStartAction global = new UnitCommandStartAction();

    //0x0A
    public final Property<Integer> unitTokenId = u16(BODY_OFFSET);
    //0x0C
    public final Property<Integer> commandDirection = u8(BODY_OFFSET + Short.BYTES);
    //0x0D
    public final Property<Integer> commandSegments = u8(BODY_OFFSET + Short.BYTES + Byte.BYTES);

    /**
     * Native: UnitCommandStartAction::UnitCommandStartAction @0050C210.
     * Fully ported.
     */
    public UnitCommandStartAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CServerApp::sendUnitCommandStartAction @005040C4 packet field writes.
     */
    public static UnitCommandStartAction prepareForCommandStart(
            Unit unit,
            int commandDirection,
            int commandSegments,
            GameActionId actionId
    ) {
        UnitCommandStartAction action = selectCommandStartAction(actionId);
        action.ID.set(actionId == GameActionId.UNKNOWN_ACTION_00 ? ACTION_ID : actionId.id);
        action.commandSegments.set(commandSegments & 0xFF);
        action.commandDirection.set(commandDirection & 0xFF);
        action.unitTokenId.set(unit.idFull);
        action.playerID.set(0);
        return action;
    }

    /**
     * Native support extracted from CServerApp::sendUnitCommandStartAction @005040C4 packet id selection.
     */
    private static UnitCommandStartAction selectCommandStartAction(GameActionId actionId) {
        return switch (actionId) {
            case UNKNOWN_ACTION_00, UNIT_MOVE_ACTION_6B -> UnitMoveAction.global;
            case UNIT_TURN_ACTION_6D -> UnitTurnAction.global;
            case UNIT_ATTACK_ACTION_71 -> UnitAttackAction.global;
            default -> global;
        };
    }

    /**
     * Native: UnitCommandStartAction::UnitCommandStartAction @0050C236.
     * Fully ported.
     */
    public UnitCommandStartAction(UnitCommandStartAction from) {
        super();
        int wireSize = GetPayloadSize();
        PutSlice(ID_OFFSET, from.GetSlice(ID_OFFSET, wireSize), 0, wireSize);
    }

    /**
     * vtbl +0x04: UnitCommandStartAction::Clone @00541320.
     * Fully ported.
     */
    @Override
    public UnitCommandStartAction Clone() {
        return new UnitCommandStartAction(this);
    }

    /**
     * vtbl +0x10: UnitCommandStartAction::getWireSize @005413A0.
     * Port name: GetPayloadSize.
     * Fully ported.
     */
    @Override
    public int GetPayloadSize() {
        return 0x05;
    }
}
