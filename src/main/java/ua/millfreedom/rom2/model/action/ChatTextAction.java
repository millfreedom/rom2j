package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.net.CBufferManager;
import ua.millfreedom.rom2.CString;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CPlayer;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

import java.nio.charset.StandardCharsets;

/**
 * Native chat/text packet used by command and speech message IDs (for example 0x06, 0x0F, 0x91, 0xAE).
 */
public class ChatTextAction extends CGameAction {
    public static final int ACTION_ID = GameActionId.CHAT_TEXT_ACTION_91.id;
    public static final ChatTextAction global = new ChatTextAction();
    // Native timed chat line lifetime used by MapVisualObject::HandleGameAction @00414A90.
    private static final int CHAT_LINE_LIFETIME_MS = 10000;

    //0x0A
    public final Property<Integer> senderIdAndChannel = i32(BODY_OFFSET);
    //0x0E
    public final Property<Integer> textLength = u8(BODY_OFFSET + Integer.BYTES);
    //0x0F
    public final Property<String> text = countedPayloadCString(Integer.BYTES + Byte.BYTES, textLength);

    /**
     * Native: ChatTextAction::ChatTextAction @0050BED8.
     * Fully ported.
     */
    public ChatTextAction() {
        super();
        ID.set(ACTION_ID);
        senderIdAndChannel.set(0);
        text.set("");
    }

    /**
     * Native: ChatTextAction::ChatTextAction @0050BF16.
     * Fully ported.
     */
    public ChatTextAction(ChatTextAction from) {
        super();
        ID.set(from.ID.get());
        netID.set(from.netID.get());
        senderIdAndChannel.set(from.senderIdAndChannel.get());
        textLength.set(from.textLength.get());
        int textBytesWithTerminator = textLength.get() + 1;
        PutSlice(
                BODY_OFFSET + Integer.BYTES + Byte.BYTES,
                from.GetSlice(BODY_OFFSET + Integer.BYTES + Byte.BYTES, textBytesWithTerminator),
                0,
                textBytesWithTerminator
        );
    }

    /**
     * Native: ChatTextAction::ChatTextAction @0050BF8B and ChatTextAction::ChatTextAction @0050C019.
     * Fully ported.
     */
    public ChatTextAction(CString text) {
        super();
        ID.set(ACTION_ID);
        senderIdAndChannel.set(0);
        this.text.set(text.toString());
    }

    /**
     * Native: ChatTextAction::ChatTextAction @0050C0B3.
     * Fully ported.
     */
    public ChatTextAction(String text) {
        super();
        ID.set(ACTION_ID);
        senderIdAndChannel.set(0);
        this.text.set(text);
    }

    /**
     * Native support extracted from CServerApp::sendServerChatText @00505263 packet field writes.
     */
    public static ChatTextAction prepareForServerChatText(String text, Player player) {
        ChatTextAction action = global;
        action.ID.set(ACTION_ID);
        action.text.set(text);
        action.playerID.set(player == null ? 0 : player.playerId);
        action.senderIdAndChannel.set(0);
        return action;
    }

    /**
     * vtbl +0x04: ChatTextAction::Clone @00541250.
     * Fully ported.
     */
    @Override
    public ChatTextAction Clone() {
        return new ChatTextAction(this);
    }

    /**
     * vtbl +0x10: ChatTextAction::getWireSize @005412D0.
     * Port name: GetPayloadSize.
     * Fully ported.
     */
    @Override
    public int GetPayloadSize() {
        int normalizedSize = Math.max(textLength.get(), 0);
        return normalizedSize + 7;
    }

    /**
     * vtbl +0x08: ChatTextAction::WritePayload @0050C19A.
     * Fully ported.
     */
    @Override
    public boolean WritePayload(CBufferManager target) {
        textLength.set(text.get().getBytes(StandardCharsets.ISO_8859_1).length);
        return target.Write(this, ID_OFFSET, textLength.get() + 7);
    }

    /**
     * vtbl +0x0C: ChatTextAction::ReadPayload @0050C1D7.
     * Fully ported.
     */
    @Override
    public boolean ReadPayload(CBufferManager source) {
        source.Read(this, BODY_OFFSET, 5);
        return source.Read(this, BODY_OFFSET + 5, textLength.get() + 1);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00414A90.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        if (ID.get() == ACTION_ID) {
            int senderPlayerId = senderIdAndChannel.get() & 0xFF;
            int channel = (senderIdAndChannel.get() >>> 8) & 0xFF;
            CPlayer sender = mapVisualObject.findClientPlayerById(senderPlayerId);
            if (sender != null
                    && mapVisualObject.currentPlayer != null
                    && mapVisualObject.currentPlayer.isSilentDiplomacy(senderPlayerId)
                    && channel != 4) {
                Globals.mainWindow.getInputController().onMessage(MessageCodes.MULTIPLAYER_LOBBY_APPEND_CHAT_MESSAGE, senderPlayerId, text.get());
                return;
            }

            String line = sender == null ? text.get() : sender.name + ": " + text.get();
            mapVisualObject.gameListControl.addTimedLine(line, resolveChatPalette(sender, channel), CHAT_LINE_LIFETIME_MS);
        }
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00414A90.
     */
    private static Palette16 resolveChatPalette(CPlayer sender, int channel) {
        if (channel == 4) {
            return Palettes.orangeish;
        }
        if (sender != null && sender.color >= 0 && sender.color < Palettes.p16.size()) {
            return Palettes.p16.get(sender.color);
        }
        return Palettes.messagePrimary();
    }
}
