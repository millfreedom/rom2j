package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.CGameSession;
import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Game action packet type 0x02 (T45 wire layout).
 */
public class PlayerJoinAction extends CGameAction {
    public static final int ACTION_ID = GameActionId.PLAYER_JOIN_ACTION_02.id;
    private static final int HAS_CD_ALWAYS_ONE = 1;

    // Native global singleton at 0x00670E10.
    public static final PlayerJoinAction global = new PlayerJoinAction();

    //0x0A
    public final Property<Integer> sessionKeyPart1 = i32(0x0A);
    //0x0E
    public final Property<Integer> sessionKeyPart2 = i32(0x0E);
    //0x12
    public final Property<Integer> joinOptionsPacked = i32(0x12);
    //0x16
    public final Property<String> playerName = fixedCString(0x16, 32);

    /**
     * Native: PlayerJoinAction::PlayerJoinAction @0050BCAD.
     * Fully ported.
     */
    public PlayerJoinAction() {
        sessionKeyPart1.set(0);
        sessionKeyPart2.set(0);
        joinOptionsPacked.set(0);
        playerName.set("");
    }

    /**
     * Native support extracted from MapVisualObject::sendPlayerJoinAndWaitForPlayerList @0040D791 packet writes.
     * Fully ported.
     */
    public static PlayerJoinAction prepareForPlayerJoin(CGameSession gameSession) {
        PlayerJoinAction action = global;
        action.ID.set(ACTION_ID);
        action.playerName.set(gameSession.m_PlayerName);
        action.sessionKeyPart1.set(gameSession.sessionKeyPart1);
        action.sessionKeyPart2.set(gameSession.sessionKeyPart2);
        action.joinOptionsPacked.set(
                HAS_CD_ALWAYS_ONE
                        | ((gameSession.clanServerId & 0xFF) << 8)
                        | ((gameSession.getMinimumPlayerCount() & 0xFF) << 16)
                        | ((gameSession.getMaximumPlayerCount() & 0xFF) << 24)
        );
        action.playerID.set(0);
        return action;
    }

    /**
     * vtbl +0x10: PlayerJoinAction::getWireSize @00541030.
     * Port name: GetPayloadSize.
     * Returns packet wire size in bytes, counted from CGameAction_Base.ID.
     * Fully ported.
     */
    @Override
    public int GetPayloadSize() {
        return 0x2D;
    }

}
