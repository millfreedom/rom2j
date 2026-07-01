package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `UnitTokenAction` packet id `0x36` used to close a shop dialog for a shopkeeper unit token.
 */
public class CloseShopDialogAction extends UnitTokenAction {
    public static final int ACTION_ID = GameActionId.CLOSE_SHOP_DIALOG_ACTION_36.id;
    public static final CloseShopDialogAction global = new CloseShopDialogAction();

    /**
     * Native support extracted from CMainWindow::onDialogClosed @004891D8 and MapVisualObject::CloseShopDialog @0041A311.
     */
    public CloseShopDialogAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CMainWindow::onDialogClosed @004891D8 and MapVisualObject::CloseShopDialog @0041A311.
     */
    public CloseShopDialogAction(CloseShopDialogAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from CMainWindow::onDialogClosed @004891D8 and MapVisualObject::CloseShopDialog @0041A311.
     */
    @Override
    public CloseShopDialogAction Clone() {
        return new CloseShopDialogAction(this);
    }
}
