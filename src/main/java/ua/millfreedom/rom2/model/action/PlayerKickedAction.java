package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.net.CLlDriver;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.sound.SoundManager;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;
import ua.millfreedom.rom2.text.PatchText;

import static ua.millfreedom.rom2.model.enums.SfxSounds.MESSAGE;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.TextTableId.PATCH;

/**
 * Native `ChatTextAction` packet id `0x93` used to broadcast that a player was kicked.
 */
public class PlayerKickedAction extends ChatTextAction {
    public static final int ACTION_ID = GameActionId.PLAYER_KICKED_ACTION_93.id;
    public static final PlayerKickedAction global = new PlayerKickedAction();
    // Native timed event line lifetime used by MapVisualObject::HandleGameAction @0041557B.
    private static final int EVENT_LINE_LIFETIME_MS = 5000;

    /**
     * Native support extracted from CServerApp::broadcastPlayerKickedAction @00505697,
     * FUN_00492A3A @00492A3A, and
     * FUN_004F3D68 @004F3D68.
     */
    public PlayerKickedAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CServerApp::broadcastPlayerKickedAction @00505697.
     */
    public PlayerKickedAction(PlayerKickedAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CServerApp::broadcastPlayerKickedAction @00505697 packet field writes.
     * Fully ported.
     */
    public static PlayerKickedAction prepareForPlayerKickedBroadcast(Player player) {
        PlayerKickedAction action = global;
        action.ID.set(ACTION_ID);
        action.text.set(player.name);
        action.playerID.set(0);
        action.senderIdAndChannel.set((int) (short) player.playerId);
        return action;
    }

    /**
     * Native support extracted from ChatTextAction::Clone @00541250.
     */
    @Override
    public PlayerKickedAction Clone() {
        return new PlayerKickedAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0041557B.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        mapVisualObject.gameListControl.addTimedLine(
                get(PATCH, PatchText.PLAYER_78) + " " + text.get() + " " + get(PATCH, PatchText.KICKED_FROM_GAME_79),
                Palettes.messagePrimary(),
                EVENT_LINE_LIFETIME_MS
        );
        SoundManager.playSfx(MESSAGE);
        if (targetsCurrentPlayer(mapVisualObject) && GAMEPLAY.isSetIn(Globals.mainWindow.dialogsMask)) {
            Globals.mainWindow.postMessage(MessageCodes.RETURN_TO_MULTIPLAYER_SETUP, 0, 0);
            CLlDriver.handleNetworkErrorAndClose();
        }
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0041557B self-kick branch.
     */
    public boolean targetsCurrentPlayer(MapVisualObject mapVisualObject) {
        return senderIdAndChannel.get() == mapVisualObject.currentPlayer.playerId;
    }

}
