package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.CSprite256;

import java.util.Locale;

import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_ARMORS_AND_WEAPONS_274;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_ELVEN_GOODS_277;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_MAGIC_ITEMS_275;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_SCROLLS_SPELLBOOKS_AND_POTIONS_276;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_SHOPKEEPER_61;

/**
 * Native class: DruidShopCompassVisualObject (vtbl @0x005CF570).
 * Purpose: druid shop compass with category highlights and the elven-goods campaign gate.
 */
public class DruidShopCompassVisualObject extends ShopCompassVisualObject {
    public static final int NATIVE_SIZE = 0x270; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int ANIMATION_TICK_MILLIS = 100;
    private static final int RANDOM_IDLE_DELAY_BASE_MILLIS = 5000;
    private static final int RANDOM_IDLE_DELAY_STEP_MILLIS = 1000;
    private static final int RANDOM_IDLE_DELAY_VARIANTS = 5;
    private static final int CENTER_PRIMARY_ANIMATION_FLAG = 0x10;
    private static final int CENTER_SECONDARY_ANIMATION_FLAG = 0x20;
    private static final int CENTER_ANIMATION_MASK = 0x30;
    private static final int TOOLTIP_SUPPRESS_STATE_FLAG = 0x80;
    private static final int CENTER_TRIGGER_FRAME_CYCLE_LENGTH = 0x1E;
    private static final String GRAPHICS_INTERFACE_SHOP_DRUID_SHOPFRAME_256 = "graphics/interface/shop_druid/shopframe.256";
    private static final String GRAPHICS_INTERFACE_SHOP_DRUID_SHOPMAIN_BMP = "graphics/interface/shop_druid/shopmain.bmp";
    private static final String MOVIES_SHOP_DRUID_A1_BMP_PATTERN = "movies/shop_druid/a1%04d.bmp";
    private static final String MOVIES_SHOP_DRUID_A2_BMP_PATTERN = "movies/shop_druid/a2%04d.bmp";
    private static final String MOVIES_SHOP_DRUID_A10001_BMP = "movies/shop_druid/a10001.bmp";
    private static final String GRAPHICS_INTERFACE_SHOP_DRUID_HILI_ARMOR_BMP = "graphics/interface/shop_druid/hili_armor.bmp";
    private static final String GRAPHICS_INTERFACE_SHOP_DRUID_HILI_MAGIC_BMP = "graphics/interface/shop_druid/hili_magic.bmp";
    private static final String GRAPHICS_INTERFACE_SHOP_DRUID_HILI_POTION_BMP = "graphics/interface/shop_druid/hili_potion.bmp";
    private static final String GRAPHICS_INTERFACE_SHOP_DRUID_HILI_ELVEN_BMP = "graphics/interface/shop_druid/hili_elven.bmp";
    private static final String GRAPHICS_INTERFACE_SHOP_DRUID_ELVEN_BMP = "graphics/interface/shop_druid/elven.bmp";
    private static final int[] OUTER_DIRECTION_TEXTS = {
            MAIN_ARMORS_AND_WEAPONS_274,
            MAIN_MAGIC_ITEMS_275,
            MAIN_SCROLLS_SPELLBOOKS_AND_POTIONS_276,
            MAIN_ELVEN_GOODS_277
    };

    private static boolean animationTickInitialized;
    private static boolean centerStateTickInitialized;
    private static long lastAnimationTick;
    private static long lastCenterStateTick;

    //0x25c
    public CBmp64k armorCategoryHighlightBitmap;
    //0x260
    public CBmp64k magicItemsCategoryHighlightBitmap;
    //0x264
    public CBmp64k potionsCategoryHighlightBitmap;
    //0x268
    public CBmp64k selectedElvenGoodsHighlightBitmap;
    //0x26c
    public CBmp64k elvenGoodsCategoryBitmap;

    /**
     * Native: DruidShopCompassVisualObject::DruidShopCompassVisualObject @004BECCF.
     * Full port.
     */
    public DruidShopCompassVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            ShopDialogVisualObject ownerContext
    ) {
        super(id, xLeft, yTop, xRight, yBottom, ownerContext);
        int baseX = this.cRect.left;
        int baseY = this.cRect.top;
        this.outerCompassRects[0].set(baseX + 0x05, baseY + 0x6D, baseX + 0x5D, baseY + 0xDD);
        this.outerCompassRects[1].set(baseX + 0x05, baseY + 0x31, baseX + 0x65, baseY + 0x6D);
        this.outerCompassRects[2].set(baseX + 0x79, baseY + 0x21, baseX + 0xC1, baseY + 0x91);
        this.outerCompassRects[3].set(baseX + 0x5D, baseY + 0xA9, baseX + 0xBD, baseY + 0x111);
        this.centerCompassRect.set(baseX + 0xC5, baseY + 0x59, baseX + 0x10D, baseY + 0xFD);
        this.armorCategoryHighlightBitmap = null;
        this.magicItemsCategoryHighlightBitmap = null;
        this.potionsCategoryHighlightBitmap = null;
        this.selectedElvenGoodsHighlightBitmap = null;
        this.elvenGoodsCategoryBitmap = null;
    }

    /**
     * vtbl +0x78: DruidShopCompassVisualObject::LoadVisualResources @004BF0D6.
     * Full port.
     */
    @Override
    public void loadVisualResources() {
        releaseVisualResources();
        shopFrameSprite = new CSprite256(GRAPHICS_INTERFACE_SHOP_DRUID_SHOPFRAME_256);
        shopFrameSprite.initPalette(1, 1, 0);
        Globals.renderer.refreshMousePointer();
        backgroundBitmap = new CBmp64k(GRAPHICS_INTERFACE_SHOP_DRUID_SHOPMAIN_BMP);
        centerIdleBitmap = new CBmp64k(MOVIES_SHOP_DRUID_A10001_BMP);
        armorCategoryHighlightBitmap = new CBmp64k(GRAPHICS_INTERFACE_SHOP_DRUID_HILI_ARMOR_BMP);
        magicItemsCategoryHighlightBitmap = new CBmp64k(GRAPHICS_INTERFACE_SHOP_DRUID_HILI_MAGIC_BMP);
        potionsCategoryHighlightBitmap = new CBmp64k(GRAPHICS_INTERFACE_SHOP_DRUID_HILI_POTION_BMP);
        selectedElvenGoodsHighlightBitmap = new CBmp64k(GRAPHICS_INTERFACE_SHOP_DRUID_HILI_ELVEN_BMP);
        elvenGoodsCategoryBitmap = new CBmp64k(GRAPHICS_INTERFACE_SHOP_DRUID_ELVEN_BMP);
        Globals.renderer.refreshMousePointer();
    }

    /**
     * vtbl +0x7C: DruidShopCompassVisualObject::ReleaseVisualResources @004BF37D.
     * Full port.
     */
    @Override
    public void releaseVisualResources() {
        super.releaseVisualResources();
        armorCategoryHighlightBitmap = null;
        magicItemsCategoryHighlightBitmap = null;
        potionsCategoryHighlightBitmap = null;
        selectedElvenGoodsHighlightBitmap = null;
        elvenGoodsCategoryBitmap = null;
    }

    /**
     * vtbl +0x14: DruidShopCompassVisualObject::GetText @004BFEE2.
     * Full port.
     */
    @Override
    public String getText() {
        ShopDialogVisualObject resolvedOwnerDialog = ownerDialog;
        if (resolvedOwnerDialog.dialogActiveFlag == 0
                || (stateFlags & TOOLTIP_SUPPRESS_STATE_FLAG) != 0) {
            return null;
        }

        var ownerTopLeft = getOwnerTopLeftScreenPoint(resolvedOwnerDialog);
        int mouseX = Globals.mousePointer.getX() - ownerTopLeft.x;
        int mouseY = Globals.mousePointer.getY() - ownerTopLeft.y;
        for (int directionIndex = 0; directionIndex < OUTER_DIRECTION_TEXTS.length; directionIndex++) {
            if (getOuterCompassRect(directionIndex).contains(mouseX, mouseY)) {
                return get(OUTER_DIRECTION_TEXTS[directionIndex]);
            }
        }
        if (centerCompassRect.contains(mouseX, mouseY)) {
            return get(MAIN_SHOPKEEPER_61);
        }
        return null;
    }

    /**
     * vtbl +0x2C: DruidShopCompassVisualObject::Update @004BF908.
     * Full port.
     */
    @Override
    public void update() {
        ShopDialogVisualObject resolvedOwnerDialog = ownerDialog;
        if (resolvedOwnerDialog.dialogActiveFlag == 0) {
            return;
        }

        long now = System.currentTimeMillis();
        initializeAnimationTimers(now);
        if (now - lastAnimationTick >= ANIMATION_TICK_MILLIS) {
            resolvedOwnerDialog.updateShopAmbientSound();
            lastAnimationTick = now;

            long elapsed = now - lastCenterStateTick;
            long randomDelay = Utils.randInclusive(RANDOM_IDLE_DELAY_VARIANTS - 1)
                    * (long) RANDOM_IDLE_DELAY_STEP_MILLIS
                    + RANDOM_IDLE_DELAY_BASE_MILLIS;
            if (elapsed >= randomDelay
                    && (stateFlags & CENTER_PRIMARY_ANIMATION_FLAG) == 0
                    && (stateFlags & CENTER_SECONDARY_ANIMATION_FLAG) == 0) {
                stateFlags |= CENTER_PRIMARY_ANIMATION_FLAG << Utils.randExclusive(0, 2);
            }
            if ((stateFlags & CENTER_ANIMATION_MASK) != 0) {
                loadCenterTriggerFrame();
                if (centerAnimationFrame == 0) {
                    lastCenterStateTick = now;
                }
            }
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        Globals.renderer.lockSurface();
        try {
            drawBaseCompassArt(screenRect);
            if (elvenGoodsCategoryBitmap != null && isElvenGoodsAvailable()) {
                if (resolvedOwnerDialog.selectedCatalogCategoryIndex == 3) {
                    drawBitmap(selectedElvenGoodsHighlightBitmap, screenRect.left + 0x5D, screenRect.top + 0xAC);
                } else {
                    drawBitmap(elvenGoodsCategoryBitmap, screenRect.left + 0x5D, screenRect.top + 0xAC);
                }
            }
            switch (resolvedOwnerDialog.selectedCatalogCategoryIndex) {
                case 0 -> drawBitmap(armorCategoryHighlightBitmap, screenRect.left + 0x05, screenRect.top + 0x70);
                case 1 -> drawBitmap(magicItemsCategoryHighlightBitmap, screenRect.left + 0x05, screenRect.top + 0x34);
                case 2 -> drawBitmap(potionsCategoryHighlightBitmap, screenRect.left + 0x79, screenRect.top + 0x24);
                default -> {
                }
            }

            CBmp64k centerBitmap = (stateFlags & CENTER_ANIMATION_MASK) == 0
                    ? centerIdleBitmap
                    : centerTriggerBitmap;
            if (centerBitmap != null) {
                drawBitmap(centerBitmap, screenRect.left + 0xC5, screenRect.top + 0x5C);
            }
        } finally {
            Globals.renderer.unlockSurface();
        }
        updateBaseVisualObject();
    }

    /**
     * vtbl +0x54: DruidShopCompassVisualObject::OnLButtonDown @004BFDF1.
     * Full port.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        ShopDialogVisualObject resolvedOwnerDialog = ownerDialog;

        for (int directionIndex = 0; directionIndex < OUTER_DIRECTION_TEXTS.length; directionIndex++) {
            if (!isOuterCompassRectHit(directionIndex, x, y)) {
                continue;
            }
            if (directionIndex == 3 && !isElvenGoodsAvailable()) {
                continue;
            }
            if (selectCatalogDirection(directionIndex)) {
                refreshOwnerShopGridSelection(resolvedOwnerDialog);
            }
            return 0;
        }
        return 0;
    }

    /**
     * vtbl +0x80: DruidShopCompassVisualObject::LoadCenterForwardFrames @004BF5DE.
     * Full port. Druid only clears the inherited center-forward frame array for this slot.
     */
    @Override
    public void loadCenterForwardFrames() {
        releaseCenterForwardFrames();
    }

    /**
     * vtbl +0x84: DruidShopCompassVisualObject::LoadCenterReturnFrames @004BF5F7.
     * Full port. Druid only clears the inherited center-return frame array for this slot.
     */
    @Override
    public void loadCenterReturnFrames() {
        releaseCenterReturnFrames();
    }

    /**
     * vtbl +0x8C: DruidShopCompassVisualObject::LoadDirectionAnimationFrames @004BF522.
     * Full port. Druid only clears the inherited direction frame array for this slot.
     */
    @Override
    protected void loadDirectionSelectionFrames(int directionIndex) {
        releaseDirectionAnimationFrames(directionIndex);
    }

    /**
     * vtbl +0x90: DruidShopCompassVisualObject::ReleaseDirectionAnimationFrames @004BF541.
     * Full port.
     */
    @Override
    protected void releaseDirectionAnimationFrames(int directionIndex) {
        clearFrames(directionFrames[directionIndex]);
    }

    /**
     * vtbl +0x94: DruidShopCompassVisualObject::ReleaseCenterForwardFrames @004BF754.
     * Full port.
     */
    @Override
    protected void releaseCenterForwardFrames() {
        clearFrames(centerForwardFrames);
    }

    /**
     * vtbl +0x98: DruidShopCompassVisualObject::ReleaseCenterReturnFrames @004BF7D1.
     * Full port.
     */
    @Override
    protected void releaseCenterReturnFrames() {
        clearFrames(centerReturnFrames);
    }

    /**
     * vtbl +0x9C: DruidShopCompassVisualObject::ReleaseCenterTriggerFrame @004BF84E.
     * Full port.
     */
    @Override
    protected void releaseCenterTriggerFrame() {
        centerTriggerBitmap = null;
    }

    /**
     * vtbl +0xA0: DruidShopCompassVisualObject::ReleaseAnimationResources @004BF8A5.
     * Full port.
     */
    @Override
    public void releaseAnimationResources() {
        for (int directionIndex = 0; directionIndex < OUTER_DIRECTION_TEXTS.length; directionIndex++) {
            releaseDirectionAnimationFrames(directionIndex);
        }
        releaseCenterForwardFrames();
        releaseCenterReturnFrames();
        releaseCenterTriggerFrame();
    }

    /**
     * vtbl +0xA4: DruidShopCompassVisualObject::SelectCatalogDirection @004BFD3A.
     * Full port.
     */
    @Override
    protected boolean selectCatalogDirection(int directionIndex) {
        if ((directionIndex & 0xffff) == 3 && !isElvenGoodsAvailable()) {
            return false;
        }
        ShopDialogVisualObject resolvedOwnerDialog = ownerDialog;
        boolean selectedCategoryChanged =
                (resolvedOwnerDialog.selectedCatalogCategoryIndex & 0xffff) != (directionIndex & 0xffff);
        if (!selectedCategoryChanged) {
            return false;
        }

        restartDirectionSelectionSound(resolvedOwnerDialog.shopDepartSound);
        setDirectionFrameIndex(directionIndex, 0);
        resolvedOwnerDialog.selectedCatalogCategoryIndex = directionIndex;
        resetOwnerCatalogGridVisibleStart(resolvedOwnerDialog);
        return true;
    }

    /**
     * Native helper tail in DruidShopCompassVisualObject::Update @004BF908.
     * Full support port.
     */
    private static void initializeAnimationTimers(long now) {
        if (!animationTickInitialized) {
            animationTickInitialized = true;
            lastAnimationTick = now - ANIMATION_TICK_MILLIS;
        }
        if (!centerStateTickInitialized) {
            centerStateTickInitialized = true;
            lastCenterStateTick = now;
        }
    }

    /**
     * vtbl +0x88: DruidShopCompassVisualObject::LoadCenterTriggerFrame @004BF610.
     * Full port.
     */
    @Override
    public void loadCenterTriggerFrame() {
        releaseCenterTriggerFrame();
        centerAnimationFrame++;
        centerAnimationFrame %= CENTER_TRIGGER_FRAME_CYCLE_LENGTH;
        if (centerAnimationFrame == 0) {
            stateFlags &= 0x0F;
        }

        String resourcePath;
        if ((stateFlags & CENTER_PRIMARY_ANIMATION_FLAG) != 0) {
            resourcePath = String.format(
                    Locale.ROOT,
                    MOVIES_SHOP_DRUID_A1_BMP_PATTERN,
                    centerAnimationFrame + 1
            );
        } else if ((stateFlags & CENTER_SECONDARY_ANIMATION_FLAG) != 0) {
            resourcePath = String.format(
                    Locale.ROOT,
                    MOVIES_SHOP_DRUID_A2_BMP_PATTERN,
                    centerAnimationFrame + 1
            );
        } else {
            resourcePath = MOVIES_SHOP_DRUID_A10001_BMP;
        }
        centerTriggerBitmap = new CBmp64k(resourcePath);
    }

    /**
     * Native support extracted from DruidShopCompassVisualObject::ReleaseDirectionAnimationFrames @004BF541,
     * ReleaseCenterForwardFrames @004BF754, and ReleaseCenterReturnFrames @004BF7D1.
     */
    private static void clearFrames(CBmp64k[] frames) {
        for (int frameIndex = 0; frameIndex < frames.length; frameIndex++) {
            frames[frameIndex] = null;
        }
    }

    /**
     * Native: DruidShopCompassVisualObject::IsElvenGoodsAvailable @004BF096.
     * Full port.
     */
    private static boolean isElvenGoodsAvailable() {
        if (Globals.mainWindow.sessionMode != 2) {
            return true;
        }
        return Globals.scenarioLib.getVar(0x302) != 0;
    }

}
