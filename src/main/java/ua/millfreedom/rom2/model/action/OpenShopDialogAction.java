package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `UnitTokenAction` packet id `0x32` used to open a shop dialog for a shopkeeper unit token.
 */
public class OpenShopDialogAction extends UnitTokenAction {
    public static final int ACTION_ID = GameActionId.OPEN_SHOP_DIALOG_ACTION_32.id;
    public static final OpenShopDialogAction global = new OpenShopDialogAction();

    /**
     * Native support extracted from CMainWindow::showShopDialog @0048AEA8 and MapVisualObject::AfterShopDialogShown @0041A29B.
     */
    public OpenShopDialogAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CMainWindow::showShopDialog @0048AEA8 and MapVisualObject::AfterShopDialogShown @0041A29B.
     */
    public OpenShopDialogAction(OpenShopDialogAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from CMainWindow::showShopDialog @0048AEA8 and MapVisualObject::AfterShopDialogShown @0041A29B.
     */
    @Override
    public OpenShopDialogAction Clone() {
        return new OpenShopDialogAction(this);
    }
}
