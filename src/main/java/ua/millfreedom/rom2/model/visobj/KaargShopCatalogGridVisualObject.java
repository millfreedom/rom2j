package ua.millfreedom.rom2.model.visobj;

/**
 * Native class: KaargShopCatalogGridVisualObject (vtbl @0x005CF828).
 * Purpose: kaarg-specific shop catalog item-grid skin over the shared catalog-grid branch.
 */
public class KaargShopCatalogGridVisualObject extends ShopCatalogGridVisualObject {
    public static final int NATIVE_SIZE = 0x2150; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final String GRAPHICS_INTERFACE_SHOP_KAARG_DIRECTORY = "graphics/interface/shop_kaarg/";

    /**
     * Native: KaargShopCatalogGridVisualObject::KaargShopCatalogGridVisualObject @004C3500.
     * Full port.
     */
    public KaargShopCatalogGridVisualObject(
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
     * vtbl +0xB4: KaargShopCatalogGridVisualObject::InitializeOverlayBitmaps @004B3258.
     * Full port.
     */
    @Override
    public void initializeOverlayBitmaps() {
        initializeOverlayBitmapsFromDirectory(GRAPHICS_INTERFACE_SHOP_KAARG_DIRECTORY);
    }
}
