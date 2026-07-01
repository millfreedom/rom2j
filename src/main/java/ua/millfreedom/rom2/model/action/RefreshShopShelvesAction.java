package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `TwoDwordAction` packet id `0x3F` used to rebuild shop shelves from current owner data.
 */
public class RefreshShopShelvesAction extends TwoDwordAction {
    public static final int ACTION_ID = GameActionId.REFRESH_SHOP_SHELVES_ACTION_3F.id;
    public static final RefreshShopShelvesAction global = new RefreshShopShelvesAction();

    /**
     * Native support extracted from CMainWindow::loadSelectedCampaignSaveGame @0048DB37,
     * CMainWindow::WindowProc @004852D8, MapVisualObject::RefreshShopShelves @0041AAA5,
     * and FUN_00521083 @00521083.
     */
    public RefreshShopShelvesAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CMainWindow::loadSelectedCampaignSaveGame @0048DB37,
     * CMainWindow::WindowProc @004852D8, MapVisualObject::RefreshShopShelves @0041AAA5,
     * and FUN_00521083 @00521083.
     */
    public RefreshShopShelvesAction(RefreshShopShelvesAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from CMainWindow::loadSelectedCampaignSaveGame @0048DB37,
     * CMainWindow::WindowProc @004852D8, MapVisualObject::RefreshShopShelves @0041AAA5,
     * and FUN_00521083 @00521083.
     */
    @Override
    public RefreshShopShelvesAction Clone() {
        return new RefreshShopShelvesAction(this);
    }
}
