package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `UnitTokenAction` packet id `0x33` used to commit the current shop buy table for a shopkeeper unit token.
 */
public class ShopBuyAction extends UnitTokenAction {
    public static final int ACTION_ID = GameActionId.SHOP_BUY_ACTION_33.id;
    public static final ShopBuyAction global = new ShopBuyAction();

    /**
     * Native support extracted from ShopDialogVisualObject::HandleBuyAction @004B93CF and MapVisualObject::CommitShopBuyAction @0041A387.
     */
    public ShopBuyAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from ShopDialogVisualObject::HandleBuyAction @004B93CF and MapVisualObject::CommitShopBuyAction @0041A387.
     */
    public ShopBuyAction(ShopBuyAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from ShopDialogVisualObject::HandleBuyAction @004B93CF and MapVisualObject::CommitShopBuyAction @0041A387.
     */
    @Override
    public ShopBuyAction Clone() {
        return new ShopBuyAction(this);
    }
}
