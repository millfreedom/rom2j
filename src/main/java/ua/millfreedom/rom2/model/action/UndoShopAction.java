package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `UnitTokenAction` packet id `0x35` used to undo pending shop-table transactions for a shopkeeper unit token.
 */
public class UndoShopAction extends UnitTokenAction {
    public static final int ACTION_ID = GameActionId.UNDO_SHOP_ACTION_35.id;
    public static final UndoShopAction global = new UndoShopAction();

    /**
     * Native support extracted from ShopDialogVisualObject::HandleUndoAction @004B954D and MapVisualObject::CommitShopUndoAction @0041A43B.
     */
    public UndoShopAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from ShopDialogVisualObject::HandleUndoAction @004B954D and MapVisualObject::CommitShopUndoAction @0041A43B.
     */
    public UndoShopAction(UndoShopAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from ShopDialogVisualObject::HandleUndoAction @004B954D and MapVisualObject::CommitShopUndoAction @0041A43B.
     */
    @Override
    public UndoShopAction Clone() {
        return new UndoShopAction(this);
    }
}
