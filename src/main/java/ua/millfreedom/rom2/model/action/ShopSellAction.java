package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `UnitTokenAction` packet id `0x34` used to commit the current shop sell table for a shopkeeper unit token.
 */
public class ShopSellAction extends UnitTokenAction {
    public static final int ACTION_ID = GameActionId.SHOP_SELL_ACTION_34.id;
    public static final ShopSellAction global = new ShopSellAction();

    /**
     * Native support extracted from ShopDialogVisualObject::HandleSellAction @004B935D and MapVisualObject::CommitShopSellAction @0041A3E1.
     */
    public ShopSellAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from ShopDialogVisualObject::HandleSellAction @004B935D and MapVisualObject::CommitShopSellAction @0041A3E1.
     */
    public ShopSellAction(ShopSellAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from ShopDialogVisualObject::HandleSellAction @004B935D and MapVisualObject::CommitShopSellAction @0041A3E1.
     */
    @Override
    public ShopSellAction Clone() {
        return new ShopSellAction(this);
    }
}
