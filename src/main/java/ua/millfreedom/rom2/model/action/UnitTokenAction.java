package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.unit.Unit;

/**
 * Raw packet model for native UnitTokenAction.
 */
public class UnitTokenAction extends CGameAction {
    public static final UnitTokenAction global = new UnitTokenAction();

    //0x0A
    public final Property<Integer> unitTokenId = u16(BODY_OFFSET);

    /**
     * Native: UnitTokenAction::UnitTokenAction @0050BB5A.
     * Fully ported.
     */
    public UnitTokenAction() {
        super();
        unitTokenId.set(0);
    }

    /**
     * Native support extracted from UnitTokenAction::UnitTokenAction @0050BB5A.
     */
    public UnitTokenAction(UnitTokenAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from CServerApp::sendUnitVisibilityAction @005053A8 packet field writes.
     */
    public static UnitTokenAction prepareForUnitVisibilityAction(Unit unit, boolean hideUnit, Player player) {
        UnitTokenAction action = hideUnit ? UnitHiddenAction.global : UnitShownAction.global;
        action.ID.set(hideUnit ? UnitHiddenAction.ACTION_ID : UnitShownAction.ACTION_ID);
        action.playerID.set(player == null ? 0 : player.playerId);
        action.unitTokenId.set(unit.idFull);
        return action;
    }

    /**
     * Native support extracted from CServerApp::sendUnitVisibilityAction @005053A8 per-recipient packet field writes.
     */
    public CGameAction cloneForUnitVisibilityRecipient(Player player) {
        playerID.set(player.playerId);
        return Clone();
    }

    /**
     * vtbl +0x10: UnitTokenAction::getWireSize @00540F60.
     * Port name: GetPayloadSize.
     * Fully ported.
     */
    @Override
    public int GetPayloadSize() {
        return 0x03;
    }
}
