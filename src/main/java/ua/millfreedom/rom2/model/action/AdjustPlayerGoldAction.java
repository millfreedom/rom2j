package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `TwoDwordAction` packet id `0x3E` used to apply a gold delta to the current player.
 */
public class AdjustPlayerGoldAction extends TwoDwordAction {
    public static final int ACTION_ID = GameActionId.ADJUST_PLAYER_GOLD_ACTION_3E.id;
    public static final AdjustPlayerGoldAction global = new AdjustPlayerGoldAction();

    /**
     * Native support extracted from MapVisualObject::sendAdjustPlayerGoldAction @0041AAFF,
     * CMainWindow::WindowProc @004852D8, and Player::AdjustGoldAndNotify @00516238.
     */
    public AdjustPlayerGoldAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::sendAdjustPlayerGoldAction @0041AAFF,
     * CMainWindow::WindowProc @004852D8, and Player::AdjustGoldAndNotify @00516238.
     */
    public AdjustPlayerGoldAction(AdjustPlayerGoldAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from MapVisualObject::sendAdjustPlayerGoldAction @0041AAFF packet field writes.
     */
    public static AdjustPlayerGoldAction prepareForCurrentPlayerGoldDelta(int netId, int goldDelta) {
        AdjustPlayerGoldAction action = global;
        action.ID.set(ACTION_ID);
        action.netID.set(netId);
        action.playerID.set(0);
        action.firstPayloadDword.set(goldDelta);
        return action;
    }

    /**
     * Native support extracted from MapVisualObject::sendAdjustPlayerGoldAction @0041AAFF,
     * CMainWindow::WindowProc @004852D8, and Player::AdjustGoldAndNotify @00516238.
     */
    @Override
    public AdjustPlayerGoldAction Clone() {
        return new AdjustPlayerGoldAction(this);
    }

}
