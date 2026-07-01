package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CPlayer;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.sound.SoundManager;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.text.PatchText;

import static ua.millfreedom.rom2.model.enums.SfxSounds.MESSAGE;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.*;
import static ua.millfreedom.rom2.text.TextTableId.PATCH;

/**
 * Native `TwoDwordAction` packet id `0x92` used for broad server-originated event notifications and toasts.
 */
public class GameEventNotificationAction extends TwoDwordAction {
    public static final int ACTION_ID = GameActionId.GAME_EVENT_NOTIFICATION_ACTION_92.id;
    public static final GameEventNotificationAction global = new GameEventNotificationAction();
    // Native short timed event line lifetime used by MapVisualObject::HandleGameAction @00415014.
    private static final int SHORT_EVENT_LINE_LIFETIME_MS = 3000;
    // Native timed event line lifetime used by MapVisualObject::HandleGameAction @00415014.
    private static final int EVENT_LINE_LIFETIME_MS = 5000;

    /**
     * Native support extracted from CServerApp::sendGameEventNotification @005052D2,
     * CServerApp::sendTwoDwordAction @00505347, and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public GameEventNotificationAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CServerApp::sendGameEventNotification @005052D2,
     * CServerApp::sendTwoDwordAction @00505347, and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public GameEventNotificationAction(GameEventNotificationAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from CServerApp::sendGameEventNotification @005052D2 and
     * CServerApp::sendTwoDwordAction @00505347 packet field writes.
     */
    public static GameEventNotificationAction prepareForGameEventNotification(
            Player player,
            int notificationKind,
            int notificationValue
    ) {
        GameEventNotificationAction action = global;
        action.ID.set(ACTION_ID);
        action.playerID.set(player == null ? 0 : player.playerId);
        action.firstPayloadDword.set(notificationKind);
        action.secondPayloadDword.set(notificationValue);
        return action;
    }

    /**
     * Native support extracted from CServerApp::sendGameEventNotification @005052D2 and
     * TwoDwordAction::Clone @005410D0.
     */
    @Override
    public GameEventNotificationAction Clone() {
        return new GameEventNotificationAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00415014.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        int eventCode = firstPayloadDword.get();
        int eventArg = secondPayloadDword.get();
        switch (eventCode) {
            case 1 -> addGameLine(mapVisualObject, get(MAIN_NO_WAY_129), SHORT_EVENT_LINE_LIFETIME_MS);
            case 2 -> handleSkillImprovedNotification(mapVisualObject, eventArg);
            case 3 -> handlePlayerJoinedNotification(mapVisualObject, eventArg);
            case 4 -> handlePlayerReturnedNotification(mapVisualObject, eventArg);
            case 5 ->
                    addPlayerEventLine(mapVisualObject, eventArg, MAIN_PLAYER_221, MAIN_HAS_DECIDED_TO_BECOME_SUPER_LAME_AND_CHEAT_222);
            case 6 ->
                    addPlayerEventLine(mapVisualObject, eventArg, MAIN_PLAYER_223, MAIN_IS_GRASPING_AT_STRAWS_THIS_IS_NOT_A_CODE_TRY_AGAIN_224);
            case 7 ->
                    addPlayerEventLine(mapVisualObject, eventArg, MAIN_PLAYER_225, MAIN_HAS_SUCCESFULLY_ENTERED_A_CHEAT_CODE_226);
            case 8 -> addPatchLine(
                    mapVisualObject,
                    get(PATCH, PatchText.SHOUT_LIMIT_YOU_ARE_RESTRICTED_TO_SHOUT_FOR_83)
                            + " " + eventArg + " " + get(PATCH, PatchText.SEC_84)
            );
            case 0x10 -> addPatchLine(mapVisualObject, get(PATCH, PatchText.YOU_HAVE_ACCEPTED_THE_QUEST_88));
            case 0x20 -> addPatchLine(mapVisualObject, get(PATCH, PatchText.YOU_HAVE_COMPLETED_THE_QUEST_89));
            case 0x40 -> addPatchLine(mapVisualObject, get(PATCH, PatchText.YOU_HAVE_FAILED_THE_QUEST_91));
            case 0x80 -> addPatchLine(mapVisualObject, get(PATCH, PatchText.THE_QUEST_CAN_T_BE_COMPLETED_92));
            default -> {
            }
        }
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2 action 0x92 event 2.
     */
    private static void handleSkillImprovedNotification(MapVisualObject mapVisualObject, int packedValue) {
        int skillIndex = packedValue & 0xFFFF;
        int unitServerId = (packedValue >>> 16) & 0xFFFF;
        CUnit selectedUnit = mapVisualObject.getSelectedCUnit();
        CUnit payloadUnit = mapVisualObject.findCUnitByServerId(unitServerId);
        boolean useCampaignPayloadUnit = Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN
                && payloadUnit != null;
        CUnit textUnit = useCampaignPayloadUnit ? payloadUnit : selectedUnit;
        String line = get(skillImprovedTextBase(textUnit) + skillIndex);
        if (useCampaignPayloadUnit) {
            line = payloadUnit.name + ": " + line;
        }
        addGameLine(mapVisualObject, line, SHORT_EVENT_LINE_LIFETIME_MS);
        playSkillImprovedNotificationSound();
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2 action 0x92 event 2.
     */
    private static int skillImprovedTextBase(CUnit unit) {
        return (unit.unitFlags & 0x02) != 0
                ? MAIN_SHOOTING_SKILL_IMPROVED_134
                : MAIN_NO_WAY_129;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2 action 0x92 event 2.
     */
    private static void playSkillImprovedNotificationSound() {
        if (GAMEPLAY.isSetIn(Globals.mainWindow.dialogsMask)) {
            SoundManager.playSfx(MESSAGE);
        }
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00415014.
     */
    private static void handlePlayerJoinedNotification(MapVisualObject mapVisualObject, int playerId) {
        if (mapVisualObject.currentPlayer != null && playerId == mapVisualObject.currentPlayer.playerId) {
            return;
        }
        CPlayer player = mapVisualObject.findClientPlayerById(playerId);
        addGameLine(mapVisualObject, get(MAIN_NEW_PLAYER_204) + " " + player.name + " " + get(MAIN_HAS_JOINED_THE_GAME_205), EVENT_LINE_LIFETIME_MS);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00415014.
     */
    private static void handlePlayerReturnedNotification(MapVisualObject mapVisualObject, int playerId) {
        CPlayer player = mapVisualObject.findClientPlayerById(playerId);
        if (mapVisualObject.currentPlayer != null && playerId == mapVisualObject.currentPlayer.playerId) {
            addGameLine(mapVisualObject, get(MAIN_WELCOME_BACK_208) + " " + player.name + get(MAIN_BLANK_209), EVENT_LINE_LIFETIME_MS);
            return;
        }
        addGameLine(mapVisualObject, get(MAIN_PLAYER_206) + " " + player.name + " " + get(MAIN_RETURNED_TO_THE_GAME_207), EVENT_LINE_LIFETIME_MS);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00415014.
     */
    private static void addPlayerEventLine(MapVisualObject mapVisualObject, int playerId, int prefixText, int suffixText) {
        CPlayer player = mapVisualObject.findClientPlayerById(playerId);
        addGameLine(mapVisualObject, get(prefixText) + " " + player.name + " " + get(suffixText), EVENT_LINE_LIFETIME_MS);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00415014.
     */
    private static void addGameLine(MapVisualObject mapVisualObject, String line, int lifetimeMs) {
        mapVisualObject.gameListControl.addTimedLine(line, Palettes.messagePrimary(), lifetimeMs);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00415014.
     */
    private static void addPatchLine(MapVisualObject mapVisualObject, String line) {
        mapVisualObject.gameListControl.addTimedLine(line, Palettes.messagePrimary(), EVENT_LINE_LIFETIME_MS);
    }

}
