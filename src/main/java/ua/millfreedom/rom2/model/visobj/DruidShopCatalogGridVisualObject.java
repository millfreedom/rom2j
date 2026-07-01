package ua.millfreedom.rom2.model.visobj;

/**
 * Native class: DruidShopCatalogGridVisualObject (vtbl @0x005CF768).
 * Purpose: druid-specific shop catalog item-grid skin over the shared catalog-grid branch.
 */
public class DruidShopCatalogGridVisualObject extends ShopCatalogGridVisualObject {
    public static final int NATIVE_SIZE = 0x2150; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final String GRAPHICS_INTERFACE_SHOP_DRUID_DIRECTORY = "graphics/interface/shop_druid/";

    /**
     * Native: DruidShopCatalogGridVisualObject::DruidShopCatalogGridVisualObject @004C3470.
     * Full port.
     */
    public DruidShopCatalogGridVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            ShopDialogVisualObject ownerDialog
    ) {
        super(id, xLeft, yTop, xRight, yBottom, ownerDialog);
    }

    /**
     * vtbl +0xB4: DruidShopCatalogGridVisualObject::InitializeOverlayBitmaps @004B307F.
     * Full port.
     */
    @Override
    public void initializeOverlayBitmaps() {
        initializeOverlayBitmapsFromDirectory(GRAPHICS_INTERFACE_SHOP_DRUID_DIRECTORY);
    }
}
