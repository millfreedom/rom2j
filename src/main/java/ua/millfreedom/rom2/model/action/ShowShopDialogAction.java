package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.Shop;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `TwoDwordAction` packet id `0x83` used to open the shop interaction dialog on the client.
 */
public class ShowShopDialogAction extends TwoDwordAction {
    public static final int ACTION_ID = GameActionId.SHOW_SHOP_DIALOG_ACTION_83.id;
    public static final ShowShopDialogAction global = new ShowShopDialogAction();

    /**
     * Native support extracted from Unit::Update @0050F12C and
     * MapVisualObject::HandleGameAction @0040DCD4.
     */
    public ShowShopDialogAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from Unit::Update @0050F12C and
     * CServerApp::sendTwoDwordAction @00505347 packet field writes.
     */
    public static ShowShopDialogAction prepareForOpenShopDialog(Shop shop, Player player) {
        ShowShopDialogAction action = global;
        action.ID.set(ACTION_ID);
        action.playerID.set(player == null ? 0 : player.playerId);
        action.firstPayloadDword.set(shop.id);
        action.secondPayloadDword.set(0);
        return action;
    }

    /**
     * Native support extracted from Unit::Update @0050F12C and
     * MapVisualObject::HandleGameAction @0040DCD4.
     */
    public ShowShopDialogAction(ShowShopDialogAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040DCD4.
     * Ported action-id case: `ShowShopDialogAction`.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        Globals.mainWindow.postMessage(MessageCodes.SHOW_SHOP_DIALOG, firstPayloadDword.get(), 0);
    }

    /**
     * Native support extracted from Unit::Update @0050F12C and
     * TwoDwordAction::Clone @005410D0.
     */
    @Override
    public ShowShopDialogAction Clone() {
        return new ShowShopDialogAction(this);
    }

}
