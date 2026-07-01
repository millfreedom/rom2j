package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.Sack;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `UnitTokenAction` packet id `0x6A` used by `CServerApp::notifySackRemoved @00504B3A`
 * to remove a sack object from remote clients by token.
 */
public class SackRemovedAction extends UnitTokenAction {
    public static final int ACTION_ID = GameActionId.SACK_REMOVED_ACTION_6A.id;
    public static final SackRemovedAction global = new SackRemovedAction();

    /**
     * Native support extracted from CServerApp::notifySackRemoved @00504B3A and
     * MapVisualObject::HandleGameAction @0041356D.
     */
    public SackRemovedAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CServerApp::notifySackRemoved @00504B3A and
     * UnitTokenAction::UnitTokenAction @0050BB5A.
     */
    public SackRemovedAction(SackRemovedAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from CServerApp::notifySackRemoved @00504B3A.
     */
    public static SackRemovedAction createForSackRemoved(Sack sack, Player player) {
        SackRemovedAction action = global;
        action.ID.set(ACTION_ID);
        action.unitTokenId.set(sack.idFull);
        action.playerID.set(player.playerId);
        return action.Clone();
    }

    /**
     * Native support extracted from CServerApp::notifySackRemoved @00504B3A.
     */
    @Override
    public SackRemovedAction Clone() {
        return new SackRemovedAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0041356D.
     * Ported action-id case: `SackRemovedAction` packet id `0x6A`.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        mapVisualObject.removeObjectByToken((short) (int) unitTokenId.get());
    }
}
