package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.CSprite256;

import java.util.Locale;

import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_ARMORS_278;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_MAGIC_ITEMS_279;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_SCROLLS_SPELLBOOKS_AND_POTIONS_280;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_SHOPKEEPER_61;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_WEAPONS_281;

/**
 * Native class: KaargShopCompassVisualObject (vtbl @0x005CF6B8).
 * Purpose: kaarg shop-compass descendant with category-specific overlay art and a looping tail animation.
 */
public class KaargShopCompassVisualObject extends ShopCompassVisualObject {
    public static final int NATIVE_SIZE = 0x304; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int ANIMATION_TICK_MILLIS = 100;
    private static final int RANDOM_IDLE_DELAY_BASE_MILLIS = 5000;
    private static final int RANDOM_IDLE_DELAY_STEP_MILLIS = 1000;
    private static final int RANDOM_IDLE_DELAY_VARIANTS = 5;
    private static final int CATEGORY_COUNT = 4;
    private static final int CENTER_PRIMARY_ANIMATION_FLAG = 0x10;
    private static final int CENTER_SECONDARY_ANIMATION_FLAG = 0x20;
    private static final int CENTER_ANIMATION_MASK = 0x30;
    private static final int TOOLTIP_SUPPRESS_STATE_FLAG = 0x80;
    private static final int CENTER_ANIMATION_FRAME_CYCLE_LENGTH = 0x14;
    private static final int WEAPONS_CATEGORY_INDEX = 3;
    private static final int WEAPONS_TAIL_FRAME_START = 0x0C;
    private static final int WEAPONS_TAIL_FRAME_END_EXCLUSIVE = 0x12;
    private static final int IDLE_TAIL_FRAME_END_EXCLUSIVE = 0x06;
    private static final int TAIL_SEGMENT_FRAME_COUNT = 6;
    private static final int[] CATEGORY_TEXTS = {
            MAIN_ARMORS_278,
            MAIN_MAGIC_ITEMS_279,
            MAIN_SCROLLS_SPELLBOOKS_AND_POTIONS_280,
            MAIN_WEAPONS_281
    };
    private static final String GRAPHICS_INTERFACE_SHOP_KAARG_SHOPFRAME_256 = "graphics/interface/shop_kaarg/shopframe.256";
    private static final String GRAPHICS_INTERFACE_SHOP_KAARG_SHOPMAIN_BMP = "graphics/interface/shop_kaarg/shopmain.bmp";
    private static final String MOVIES_SHOP_KAARG_A1_BMP_PATTERN = "movies/shop_kaarg/a1%04d.bmp";
    private static final String MOVIES_SHOP_KAARG_A2_BMP_PATTERN = "movies/shop_kaarg/a2%04d.bmp";
    private static final String MOVIES_SHOP_KAARG_A10000_BMP = "movies/shop_kaarg/a10000.bmp";
    private static final String GRAPHICS_INTERFACE_SHOP_KAARG_HILI_ARMOR_BMP = "graphics/interface/shop_kaarg/hili_armor.bmp";
    private static final String GRAPHICS_INTERFACE_SHOP_KAARG_HILI_MAGIC_BMP = "graphics/interface/shop_kaarg/hili_magic.bmp";
    private static final String GRAPHICS_INTERFACE_SHOP_KAARG_HILI_POTION_BMP = "graphics/interface/shop_kaarg/hili_potion.bmp";
    private static final String GRAPHICS_INTERFACE_SHOP_KAARG_HILI_WEAPON_BMP = "graphics/interface/shop_kaarg/hili_weapon.bmp";
    private static final String GRAPHICS_INTERFACE_SHOP_KAARG_FIRE_DARK_BURN_BMP_PATTERN = "graphics/interface/shop_kaarg/fire/dark/burn%d.bmp";
    private static final String GRAPHICS_INTERFACE_SHOP_KAARG_FIRE_DARK_LITE_BMP_PATTERN = "graphics/interface/shop_kaarg/fire/dark/lite%d.bmp";
    private static final String GRAPHICS_INTERFACE_SHOP_KAARG_FIRE_DARK_CICLE_BMP_PATTERN = "graphics/interface/shop_kaarg/fire/dark/cicle%d.bmp";
    private static final String GRAPHICS_INTERFACE_SHOP_KAARG_FIRE_SELECT_BURN_BMP_PATTERN = "graphics/interface/shop_kaarg/fire/select/burn%d.bmp";
    private static final String GRAPHICS_INTERFACE_SHOP_KAARG_FIRE_SELECT_LITE_BMP_PATTERN = "graphics/interface/shop_kaarg/fire/select/lite%d.bmp";
    private static final String GRAPHICS_INTERFACE_SHOP_KAARG_FIRE_SELECT_CICLE_BMP_PATTERN = "graphics/interface/shop_kaarg/fire/select/cicle%d.bmp";

    private static boolean animationTickInitialized;
    private static boolean centerAnimationTickInitialized;
    private static int lastAnimationTick;
    private static int lastCenterAnimationTick;

    //0x25c
    public CBmp64k armorCategoryBitmap;
    //0x260
    public CBmp64k magicItemsCategoryBitmap;
    //0x264
    public CBmp64k scrollsSpellbooksPotionsCategoryBitmap;
    //0x268
    public CBmp64k weaponsCategoryBitmap;
    //0x26c
    public final CBmp64k[] defaultTailFrames = new CBmp64k[0x12];
    //0x2b4
    public final CBmp64k[] weaponsTailFrames = new CBmp64k[0x12];
    //0x2fc
    public int tailAnimationFrameIndex;
    //0x300
    public int tailAnimationFrameStep;

    /**
     * Native: KaargShopCompassVisualObject::KaargShopCompassVisualObject @004C0EE5.
     * Full port.
     */
    public KaargShopCompassVisualObject(
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

        this.outerCompassRects[0].set(baseX, baseY, baseX + 0x3C, baseY + 0xCA);
        this.outerCompassRects[0].offset(5, 5);
        this.outerCompassRects[1].set(baseX + 0x3C, baseY + 0x3A, baseX + 0x6C, baseY + 0xD0);
        this.outerCompassRects[1].offset(5, 5);
        this.outerCompassRects[2].set(baseX + 0x3C, baseY, baseX + 0xB8, baseY + 0x3A);
        this.outerCompassRects[2].offset(5, 5);
        this.outerCompassRects[3].set(baseX + 0xBE, baseY, baseX + 0x120, baseY + 0x104);
        this.outerCompassRects[3].offset(5, 5);
        this.centerCompassRect.set(baseX + 0x78, baseY + 0x6C, baseX + 0xC0, baseY + 0xCC);
        this.centerCompassRect.offset(5, 5);
    }

    /**
     * vtbl +0x78: KaargShopCompassVisualObject::LoadVisualResources @004C12CA.
     * Full port.
     */
    @Override
    public void loadVisualResources() {
        releaseVisualResources();
        shopFrameSprite = new CSprite256(GRAPHICS_INTERFACE_SHOP_KAARG_SHOPFRAME_256);
        shopFrameSprite.initPalette(1, 1, 0);
        Globals.renderer.refreshMousePointer();
        backgroundBitmap = new CBmp64k(GRAPHICS_INTERFACE_SHOP_KAARG_SHOPMAIN_BMP);
        centerIdleBitmap = new CBmp64k(MOVIES_SHOP_KAARG_A10000_BMP);
        armorCategoryBitmap = new CBmp64k(GRAPHICS_INTERFACE_SHOP_KAARG_HILI_ARMOR_BMP);
        magicItemsCategoryBitmap = new CBmp64k(GRAPHICS_INTERFACE_SHOP_KAARG_HILI_MAGIC_BMP);
        scrollsSpellbooksPotionsCategoryBitmap = new CBmp64k(GRAPHICS_INTERFACE_SHOP_KAARG_HILI_POTION_BMP);
        weaponsCategoryBitmap = new CBmp64k(GRAPHICS_INTERFACE_SHOP_KAARG_HILI_WEAPON_BMP);
        loadTailFrames(defaultTailFrames,
                GRAPHICS_INTERFACE_SHOP_KAARG_FIRE_DARK_BURN_BMP_PATTERN,
                GRAPHICS_INTERFACE_SHOP_KAARG_FIRE_DARK_LITE_BMP_PATTERN,
                GRAPHICS_INTERFACE_SHOP_KAARG_FIRE_DARK_CICLE_BMP_PATTERN);
        loadTailFrames(weaponsTailFrames,
                GRAPHICS_INTERFACE_SHOP_KAARG_FIRE_SELECT_BURN_BMP_PATTERN,
                GRAPHICS_INTERFACE_SHOP_KAARG_FIRE_SELECT_LITE_BMP_PATTERN,
                GRAPHICS_INTERFACE_SHOP_KAARG_FIRE_SELECT_CICLE_BMP_PATTERN);
        Globals.renderer.refreshMousePointer();
        tailAnimationFrameIndex = 0;
        tailAnimationFrameStep = 0;
    }

    /**
     * vtbl +0x7C: KaargShopCompassVisualObject::ReleaseVisualResources @004C22B8.
     * Full port.
     */
    @Override
    public void releaseVisualResources() {
        super.releaseVisualResources();
        armorCategoryBitmap = null;
        magicItemsCategoryBitmap = null;
        scrollsSpellbooksPotionsCategoryBitmap = null;
        weaponsCategoryBitmap = null;
        clearFrames(defaultTailFrames);
        clearFrames(weaponsTailFrames);
    }

    /**
     * vtbl +0x14: KaargShopCompassVisualObject::GetText @004C3036.
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
        for (int directionIndex = 0; directionIndex < CATEGORY_TEXTS.length; directionIndex++) {
            if (getOuterCompassRect(directionIndex).contains(mouseX, mouseY)) {
                return get(CATEGORY_TEXTS[directionIndex]);
            }
        }
        if (centerCompassRect.contains(mouseX, mouseY)) {
            return get(MAIN_SHOPKEEPER_61);
        }
        return null;
    }

    /**
     * vtbl +0x2C: KaargShopCompassVisualObject::Update @004C28C6.
     * Full port.
     */
    @Override
    public void update() {
        ShopDialogVisualObject resolvedOwnerDialog = ownerDialog;
        if (resolvedOwnerDialog.dialogActiveFlag == 0) {
            return;
        }

        int currentTick = currentTick();
        initializeAnimationTimers(currentTick);
        if (currentTick - lastAnimationTick >= ANIMATION_TICK_MILLIS) {
            resolvedOwnerDialog.updateShopAmbientSound();
            lastAnimationTick = currentTick;

            int elapsed = currentTick - lastCenterAnimationTick;
            int randomDelay = Utils.randInclusive(RANDOM_IDLE_DELAY_VARIANTS - 1)
                    * RANDOM_IDLE_DELAY_STEP_MILLIS
                    + RANDOM_IDLE_DELAY_BASE_MILLIS;
            if (elapsed >= randomDelay
                    && (stateFlags & CENTER_PRIMARY_ANIMATION_FLAG) == 0
                    && (stateFlags & CENTER_SECONDARY_ANIMATION_FLAG) == 0) {
                stateFlags |= CENTER_PRIMARY_ANIMATION_FLAG << Utils.randExclusive(0, 2);
            }
            if ((stateFlags & CENTER_ANIMATION_MASK) != 0) {
                loadCenterTriggerFrame();
                if (centerAnimationFrame == 0) {
                    lastCenterAnimationTick = currentTick;
                }
            }
            advanceTailAnimation();
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        Globals.renderer.lockSurface();
        try {
            drawBaseCompassArt(screenRect);

            switch (resolvedOwnerDialog.selectedCatalogCategoryIndex) {
                case 0 -> drawBitmap(armorCategoryBitmap, screenRect.left + 5, screenRect.top + 8);
                case 1 -> drawBitmap(magicItemsCategoryBitmap, screenRect.left + 0x31, screenRect.top + 0x48);
                case 2 ->
                        drawBitmap(scrollsSpellbooksPotionsCategoryBitmap, screenRect.left + 0x31, screenRect.top + 8);
                case 3 -> drawBitmap(weaponsCategoryBitmap, screenRect.left + 0x99, screenRect.top + 8);
                default -> {
                }
            }

            CBmp64k centerBitmap = (stateFlags & CENTER_ANIMATION_MASK) == 0
                    ? centerIdleBitmap
                    : centerTriggerBitmap;
            if (centerBitmap != null) {
                drawBitmap(centerBitmap, screenRect.left + 0x7D, screenRect.top + 0x74);
            }

            CBmp64k tailBitmap = resolvedOwnerDialog.selectedCatalogCategoryIndex == WEAPONS_CATEGORY_INDEX
                    ? weaponsTailFrames[tailAnimationFrameIndex]
                    : defaultTailFrames[tailAnimationFrameIndex];
            drawBitmap(tailBitmap, screenRect.left + 0xC1, screenRect.top + 0xCC);
        } finally {
            Globals.renderer.unlockSurface();
        }
        updateBaseVisualObject();
    }

    /**
     * vtbl +0x54: KaargShopCompassVisualObject::OnLButtonDown @004C2F57.
     * Full port.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        ShopDialogVisualObject resolvedOwnerDialog = ownerDialog;

        for (int directionIndex = 0; directionIndex < CATEGORY_COUNT; directionIndex++) {
            if (!isOuterCompassRectHit(directionIndex, x, y)) {
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
     * vtbl +0x80: KaargShopCompassVisualObject::LoadCenterForwardFrames @004C259C.
     * Full port. Kaarg only clears the inherited center-forward frame array for this slot.
     */
    @Override
    public void loadCenterForwardFrames() {
        releaseCenterForwardFrames();
    }

    /**
     * vtbl +0x84: KaargShopCompassVisualObject::LoadCenterReturnFrames @004C25B5.
     * Full port. Kaarg only clears the inherited center-return frame array for this slot.
     */
    @Override
    public void loadCenterReturnFrames() {
        releaseCenterReturnFrames();
    }

    /**
     * vtbl +0x8C: KaargShopCompassVisualObject::LoadDirectionAnimationFrames @004C24E0.
     * Full port. Kaarg only clears the inherited direction frame array for this slot.
     */
    @Override
    protected void loadDirectionSelectionFrames(int directionIndex) {
        releaseDirectionAnimationFrames(directionIndex);
    }

    /**
     * vtbl +0x90: KaargShopCompassVisualObject::ReleaseDirectionAnimationFrames @004C24FF.
     * Full port.
     */
    @Override
    protected void releaseDirectionAnimationFrames(int directionIndex) {
        clearFrames(directionFrames[directionIndex]);
    }

    /**
     * vtbl +0x94: KaargShopCompassVisualObject::ReleaseCenterForwardFrames @004C2712.
     * Full port.
     */
    @Override
    protected void releaseCenterForwardFrames() {
        clearFrames(centerForwardFrames);
    }

    /**
     * vtbl +0x98: KaargShopCompassVisualObject::ReleaseCenterReturnFrames @004C278F.
     * Full port.
     */
    @Override
    protected void releaseCenterReturnFrames() {
        clearFrames(centerReturnFrames);
    }

    /**
     * vtbl +0x9C: KaargShopCompassVisualObject::ReleaseCenterTriggerFrame @004C280C.
     * Full port.
     */
    @Override
    protected void releaseCenterTriggerFrame() {
        centerTriggerBitmap = null;
    }

    /**
     * vtbl +0xA0: KaargShopCompassVisualObject::ReleaseAnimationResources @004C2863.
     * Full port.
     */
    @Override
    public void releaseAnimationResources() {
        for (int directionIndex = 0; directionIndex < CATEGORY_COUNT; directionIndex++) {
            releaseDirectionAnimationFrames(directionIndex);
        }
        releaseCenterForwardFrames();
        releaseCenterReturnFrames();
        releaseCenterTriggerFrame();
    }

    /**
     * vtbl +0x88: KaargShopCompassVisualObject::LoadCenterTriggerFrame @004C25CE.
     * Full port.
     */
    @Override
    public void loadCenterTriggerFrame() {
        releaseCenterTriggerFrame();
        centerAnimationFrame++;
        centerAnimationFrame %= CENTER_ANIMATION_FRAME_CYCLE_LENGTH;
        if (centerAnimationFrame == 0) {
            stateFlags &= 0x0F;
        }

        String resourcePath;
        if ((stateFlags & CENTER_PRIMARY_ANIMATION_FLAG) != 0) {
            resourcePath = String.format(
                    Locale.ROOT,
                    MOVIES_SHOP_KAARG_A1_BMP_PATTERN,
                    centerAnimationFrame + 1
            );
        } else if ((stateFlags & CENTER_SECONDARY_ANIMATION_FLAG) != 0) {
            resourcePath = String.format(
                    Locale.ROOT,
                    MOVIES_SHOP_KAARG_A2_BMP_PATTERN,
                    centerAnimationFrame + 1
            );
        } else {
            resourcePath = MOVIES_SHOP_KAARG_A10000_BMP;
        }
        centerTriggerBitmap = new CBmp64k(resourcePath);
    }

    /**
     * vtbl +0xA4: KaargShopCompassVisualObject::SelectCatalogDirection @004C2DF2.
     * Full port.
     */
    @Override
    protected boolean selectCatalogDirection(int directionIndex) {
        ShopDialogVisualObject resolvedOwnerDialog = ownerDialog;
        if ((resolvedOwnerDialog.selectedCatalogCategoryIndex & 0xffff) == (directionIndex & 0xffff)) {
            return false;
        }

        restartDirectionSelectionSound(resolvedOwnerDialog.shopDepartSound);
        if (resolvedOwnerDialog.selectedCatalogCategoryIndex == 100) {
            stateFlags = 0;
            centerAnimationFrame = 0;
            directionFrameIndex3 = 0;
            directionFrameIndex2 = 0;
            directionFrameIndex1 = 0;
            directionFrameIndex0 = 0;
        } else {
            setDirectionFrameIndex(resolvedOwnerDialog.selectedCatalogCategoryIndex, 9);
        }
        setDirectionFrameIndex(directionIndex, 0);

        if (resolvedOwnerDialog.selectedCatalogCategoryIndex != 100) {
            if (directionIndex == WEAPONS_CATEGORY_INDEX) {
                if (tailAnimationFrameIndex < WEAPONS_TAIL_FRAME_START) {
                    tailAnimationFrameStep = 1;
                }
            } else if (tailAnimationFrameIndex > 0) {
                tailAnimationFrameStep = -1;
            }
        }

        resolvedOwnerDialog.selectedCatalogCategoryIndex = directionIndex;
        resetOwnerCatalogGridVisibleStart(resolvedOwnerDialog);
        return true;
    }

    /**
     * Native helper tail in KaargShopCompassVisualObject::Update @004C28C6.
     * Full support port.
     */
    private static void initializeAnimationTimers(int currentTick) {
        if (!animationTickInitialized) {
            animationTickInitialized = true;
            lastAnimationTick = currentTick - ANIMATION_TICK_MILLIS;
        }
        if (!centerAnimationTickInitialized) {
            centerAnimationTickInitialized = true;
            lastCenterAnimationTick = currentTick;
        }
    }

    /**
     * Native helper tail in KaargShopCompassVisualObject::Update @004C28C6.
     * Full support port.
     */
    private void advanceTailAnimation() {
        if (tailAnimationFrameStep == 0) {
            tailAnimationFrameIndex++;
            if (tailAnimationFrameIndex == IDLE_TAIL_FRAME_END_EXCLUSIVE) {
                tailAnimationFrameIndex = 0;
            }
            if (tailAnimationFrameIndex >= WEAPONS_TAIL_FRAME_END_EXCLUSIVE) {
                tailAnimationFrameIndex = WEAPONS_TAIL_FRAME_START;
            }
            return;
        }

        tailAnimationFrameIndex += tailAnimationFrameStep;
        if (tailAnimationFrameIndex <= 0) {
            tailAnimationFrameStep = 0;
        }
        if (tailAnimationFrameIndex >= WEAPONS_TAIL_FRAME_END_EXCLUSIVE - 1) {
            tailAnimationFrameStep = 0;
        }
    }

    /**
     * Native support extracted from KaargShopCompassVisualObject::Update @004C28C6.
     * Full support port for `timeGetTime` call sites.
     */
    private static int currentTick() {
        return (int) System.currentTimeMillis();
    }

    /**
     * Native support extracted from KaargShopCompassVisualObject::LoadVisualResources @004C12CA.
     */
    private static void loadTailFrames(CBmp64k[] frames, String burnPattern, String litePattern, String ciclePattern) {
        for (int frameIndex = 0; frameIndex < TAIL_SEGMENT_FRAME_COUNT; frameIndex++) {
            frames[frameIndex] = new CBmp64k(String.format(Locale.ROOT, burnPattern, frameIndex + 1));
            frames[frameIndex + TAIL_SEGMENT_FRAME_COUNT] =
                    new CBmp64k(String.format(Locale.ROOT, litePattern, frameIndex + 1));
            frames[frameIndex + TAIL_SEGMENT_FRAME_COUNT * 2] =
                    new CBmp64k(String.format(Locale.ROOT, ciclePattern, frameIndex + 1));
        }
    }

    /**
     * Native support extracted from KaargShopCompassVisualObject::ReleaseVisualResources @004C22B8,
     * ReleaseDirectionAnimationFrames @004C24FF, ReleaseCenterForwardFrames @004C2712, and
     * ReleaseCenterReturnFrames @004C278F.
     */
    private static void clearFrames(CBmp64k[] frames) {
        for (int frameIndex = 0; frameIndex < frames.length; frameIndex++) {
            frames[frameIndex] = null;
        }
    }

}
