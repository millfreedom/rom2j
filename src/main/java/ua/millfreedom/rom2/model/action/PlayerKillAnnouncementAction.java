package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.CPlayer;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;
import ua.millfreedom.rom2.text.PatchText;

import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.TextTableId.PATCH;

/**
 * Native `TwoDwordAction` packet id `0x94` used to announce that one player-controlled unit was killed by another.
 */
public class PlayerKillAnnouncementAction extends TwoDwordAction {
    public static final int ACTION_ID = GameActionId.PLAYER_KILL_ANNOUNCEMENT_ACTION_94.id;
    public static final PlayerKillAnnouncementAction global = new PlayerKillAnnouncementAction();
    // Native timed event line lifetime used by MapVisualObject::HandleGameAction @004155E7.
    private static final int EVENT_LINE_LIFETIME_MS = 5000;

    /**
     * Native support extracted from FUN_0052B459 @0052B459 and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public PlayerKillAnnouncementAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from FUN_0052B459 @0052B459 and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public PlayerKillAnnouncementAction(PlayerKillAnnouncementAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from FUN_0052B459 @0052B459 and
     * TwoDwordAction::Clone @005410D0.
     */
    @Override
    public PlayerKillAnnouncementAction Clone() {
        return new PlayerKillAnnouncementAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @004155E7.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        int killerPlayerId = firstPayloadDword.get();
        int victimPlayerId = secondPayloadDword.get();
        CPlayer killer = mapVisualObject.findClientPlayerById(killerPlayerId);
        CPlayer victim = mapVisualObject.findClientPlayerById(victimPlayerId);
        if (killer == null || victim == null || killerPlayerId == 0 || victimPlayerId == 0) {
            return;
        }
        mapVisualObject.gameListControl.addTimedLine(
                get(PATCH, PatchText.PLAYER_80)
                        + " " + killer.name
                        + " " + get(PATCH, PatchText.KILLED_BY_PLAYER_81)
                        + " " + victim.name
                        + get(PATCH, PatchText.BLANK_82),
                Palettes.messagePrimary(),
                EVENT_LINE_LIFETIME_MS
        );
    }

}
