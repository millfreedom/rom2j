package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CMousePointer;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.CSprite256;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.sound.Sound;
import ua.millfreedom.rom2.model.sound.SoundSystem;
import ua.millfreedom.rom2.model.window.CMainWindow;

import java.awt.*;
import java.util.Locale;

import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_ARMOR_62;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_DO_YOU_WANT_TO_IDENTIFY_THIS_ITEM_79;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_FOR_D_GOLD_COINS_80;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_MAGIC_ITEMS_64;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_SCROLLS_BOOKS_AND_POTIONS_65;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_SHOPKEEPER_61;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_WEAPONS_63;

/**
 * Native class: ShopCompassVisualObject.
 * Purpose: shop-dialog compass / radial selection widget.
 */
public class ShopCompassVisualObject extends CVisualObject {
    public static final int NATIVE_SIZE = 0x25C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int[] OUTER_DIRECTION_TEXTS = {
            MAIN_ARMOR_62,
            MAIN_WEAPONS_63,
            MAIN_MAGIC_ITEMS_64,
            MAIN_SCROLLS_BOOKS_AND_POTIONS_65
    };

    private static final int ANIMATION_TICK_MILLIS = 100;
    private static final int IDLE_RANDOM_DELAY_BASE_MILLIS = 5000;
    private static final int IDLE_RANDOM_DELAY_STEP_MILLIS = 1000;
    private static final int IDLE_RANDOM_DELAY_VARIANTS = 5;
    private static final int DIRECTION_COUNT = 4;
    private static final int DIRECTION_FRAME_RESTART_INDEX = 3;
    private static final int DIRECTION_FRAME_STOP_INDEX = 10;
    private static final int CENTER_TRIGGER_FRAME_COUNT = 0x1C;
    private static final int CENTER_SEQUENCE_FRAME_COUNT = 0x0C;
    private static final int STATE_TRIGGER_CENTER_ANIMATION = 0x10;
    private static final int STATE_FORWARD_CENTER_ANIMATION = 0x20;
    private static final int STATE_RETURN_CENTER_ANIMATION = 0x40;
    private static final int STATE_CONFIRM_PROMPT_VISIBLE = 0x80;
    private static final int COMPASS_HOVER_REGION = 100;
    private static final int CONFIRMATION_PROMPT_BRIGHTNESS = 8;
    private static final String YES_LABEL = "Yes";
    private static final String NO_LABEL = "No";
    private static final String GRAPHICS_INTERFACE_SHOPFRAME_256 = "graphics/interface/shopframe.256";
    private static final String GRAPHICS_INTERFACE_SHOPANIM_SHOPMAIN_BMP = "graphics/interface/shopanim/shopmain.bmp";
    private static final String MOVIES_SHOPANIM_POSE2_3_1_BMP = "movies/shopanim/pose2-3/1.bmp";
    private static final String GRAPHICS_INTERFACE_SHOPANIM_DIRECTION_BMP_PATTERN = "graphics/interface/shopanim/%02d/%d.bmp";
    private static final String MOVIES_SHOPANIM_YES_FRAME_BMP_PATTERN = "movies/shopanim/yes/%d.bmp";
    private static final String MOVIES_SHOPANIM_NO_FRAME_BMP_PATTERN = "movies/shopanim/no/%d.bmp";
    private static final String MOVIES_SHOPANIM_POSE2_3_FRAME_BMP_PATTERN = "movies/shopanim/pose2-3/%d.bmp";

    private static boolean animationTimersInitialized;
    private static long lastDirectionAnimationTick;
    private static long lastCenterAnimationStateTick;

    //0x5c
    public ShopDialogVisualObject ownerDialog;
    //0x60
    public final CRect[] outerCompassRects = {new CRect(), new CRect(), new CRect(), new CRect()};
    //0xa0
    public final CRect[] innerCompassRects = {new CRect(), new CRect(), new CRect(), new CRect()};
    //0xe0
    public final CRect centerCompassRect = new CRect();
    //0xf0
    public final CRect confirmationDialogRect = new CRect();
    //0x100
    public final CRect confirmationYesButtonRect = new CRect();
    //0x110
    public final CRect confirmationNoButtonRect = new CRect();
    //0x120
    public CSprite256 shopFrameSprite;
    //0x124
    public CBmp64k backgroundBitmap;
    //0x128
    public CBmp64k centerIdleBitmap;
    //0x12c
    public CBmp64k centerTriggerBitmap;
    //0x130
    public final CBmp64k[][] directionFrames = new CBmp64k[DIRECTION_COUNT][0x0B];
    //0x1e0
    public final CBmp64k[] centerForwardFrames = new CBmp64k[CENTER_SEQUENCE_FRAME_COUNT];
    //0x210
    public final CBmp64k[] centerReturnFrames = new CBmp64k[CENTER_SEQUENCE_FRAME_COUNT];
    //0x240
    public int stateFlags;
    //0x244
    public int directionFrameIndex0;
    //0x248
    public int directionFrameIndex1;
    //0x24c
    public int directionFrameIndex2;
    //0x250
    public int directionFrameIndex3;
    //0x254
    public int centerAnimationFrame;

    /**
     * Native: ShopCompassVisualObject::ShopCompassVisualObject @004B9CF5.
     * Full port.
     */
    public ShopCompassVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            ShopDialogVisualObject ownerContext
    ) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        this.ownerDialog = ownerContext;

        int baseX = this.cRect.left;
        int baseY = this.cRect.top;

        this.outerCompassRects[0].set(baseX + 0xBE, baseY + 0x6E, baseX + 0x127, baseY + 0x127);
        this.outerCompassRects[1].set(baseX + 0x05, baseY + 0x6E, baseX + 0x6E, baseY + 0x127);
        this.outerCompassRects[2].set(baseX + 0x96, baseY + 0x05, baseX + 0x122, baseY + 0x69);
        this.outerCompassRects[3].set(baseX + 0x08, baseY + 0x05, baseX + 0x96, baseY + 0x69);
        this.innerCompassRects[0].set(baseX + 0xBD, baseY + 0x6C, baseX + 0x10D, baseY + 0xDC);
        this.innerCompassRects[1].set(baseX + 0x21, baseY + 0x6C, baseX + 0x71, baseY + 0xDC);
        this.innerCompassRects[2].set(baseX + 0x95, baseY + 0x14, baseX + 0x119, baseY + 0x6C);
        this.innerCompassRects[3].set(baseX + 0x25, baseY + 0x14, baseX + 0x95, baseY + 0x6C);
        this.centerCompassRect.set(baseX + 0x6E, baseY + 0x6E, baseX + 0xBE, baseY + 0x127);
        this.confirmationDialogRect.set(0xDC, 0x14, 0x1C7, 0x73);

        int confirmationDialogWidth = this.confirmationDialogRect.width();
        int leftQuarter = (int) (confirmationDialogWidth * 0.25);
        int rightQuarter = (int) (confirmationDialogWidth * 0.75);
        this.confirmationYesButtonRect.set(leftQuarter - 9, 0x22, leftQuarter + 9, 0x2E);
        this.confirmationNoButtonRect.set(rightQuarter - 9, 0x22, rightQuarter + 9, 0x2E);

        this.shopFrameSprite = null;
        this.backgroundBitmap = null;
        this.centerIdleBitmap = null;
        this.centerTriggerBitmap = null;
        this.stateFlags = 0;
        this.directionFrameIndex0 = 0;
        this.directionFrameIndex1 = 0;
        this.directionFrameIndex2 = 0;
        this.directionFrameIndex3 = 0;
        this.centerAnimationFrame = 0;
    }

    /**
     * vtbl +0x14: ShopCompassVisualObject::GetText @004BB2C4.
     * Full port.
     */
    @Override
    public String getText() {
        ShopDialogVisualObject resolvedOwnerDialog = ownerDialog;
        if (resolvedOwnerDialog.dialogActiveFlag == 0) {
            return null;
        }
        if ((stateFlags & STATE_CONFIRM_PROMPT_VISIBLE) != 0) {
            return null;
        }

        Point ownerTopLeft = getOwnerTopLeftScreenPoint(resolvedOwnerDialog);
        int mouseX = Globals.mousePointer.getX() - ownerTopLeft.x;
        int mouseY = Globals.mousePointer.getY() - ownerTopLeft.y;
        for (int directionIndex = 0; directionIndex < DIRECTION_COUNT; directionIndex++) {
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
     * vtbl +0x2C: ShopCompassVisualObject::Update @004BB3D7.
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
        if (now - lastDirectionAnimationTick >= ANIMATION_TICK_MILLIS) {
            lastDirectionAnimationTick = now;
            advanceAllDirectionAnimations();
            advanceCenterAnimationState(now);
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        Point ownerTopLeft = getOwnerTopLeftScreenPoint(resolvedOwnerDialog);
        Globals.renderer.lockSurface();
        try {
            drawBaseCompassArt(screenRect);
            drawDirectionFrames(ownerTopLeft);
            CBmp64k centerBitmap = resolveCenterBitmap();
            if ((stateFlags & (STATE_TRIGGER_CENTER_ANIMATION | STATE_FORWARD_CENTER_ANIMATION | STATE_RETURN_CENTER_ANIMATION)) != 0
                    || centerBitmap != null) {
                drawBitmap(centerBitmap, screenRect.left + 0x71, screenRect.top + 0x70);
            }
            if ((stateFlags & STATE_CONFIRM_PROMPT_VISIBLE) != 0) {
                renderConfirmationPrompt();
            }
        } finally {
            Globals.renderer.unlockSurface();
        }

        super.update();
    }

    /**
     * vtbl +0x78: ShopCompassVisualObject::LoadVisualResources @004BAA98.
     * Full port.
     */
    public void loadVisualResources() {
        releaseVisualResources();
        shopFrameSprite = new CSprite256(GRAPHICS_INTERFACE_SHOPFRAME_256);
        shopFrameSprite.initPalette(1, 1, 0);
        Globals.renderer.refreshMousePointer();
        backgroundBitmap = new CBmp64k(GRAPHICS_INTERFACE_SHOPANIM_SHOPMAIN_BMP);
        centerIdleBitmap = new CBmp64k(MOVIES_SHOPANIM_POSE2_3_1_BMP);
        Globals.renderer.refreshMousePointer();
    }

    /**
     * vtbl +0x7C: ShopCompassVisualObject::ReleaseVisualResources @004BABD2.
     * Full Java logical port. Native bitmap deletion is represented by clearing Java references.
     */
    public void releaseVisualResources() {
        shopFrameSprite = null;
        backgroundBitmap = null;
        centerIdleBitmap = null;
    }

    /**
     * Native tail: direct `CVisualObject::Update` call sites in the ShopCompassVisualObject branch.
     * not ported.
     */
    protected final void updateBaseVisualObject() {
        super.update();
    }

    /**
     * vtbl +0x4C: ShopCompassVisualObject::OnMouseMove @004BBF2C.
     * Full port.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        ShopDialogVisualObject resolvedOwnerDialog = ownerDialog;

        int hoveredRegion = getHoveredControlRegion();
        if (resolvedOwnerDialog.hoveredControlRegion != hoveredRegion) {
            resolvedOwnerDialog.hoveredControlRegion = hoveredRegion;
            resolvedOwnerDialog.ringButtons.initializeButtonState();
            resolvedOwnerDialog.dirtyFlags |= 0x2F;
        }
        return 0;
    }

    /**
     * vtbl +0x54: ShopCompassVisualObject::OnLButtonDown @004BBFA6.
     * Full port.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        ShopDialogVisualObject resolvedOwnerDialog = ownerDialog;

        if ((stateFlags & STATE_CONFIRM_PROMPT_VISIBLE) != 0) {
            if (handleConfirmationClick(x, y)) {
                stateFlags &= ~STATE_CONFIRM_PROMPT_VISIBLE;
                resolvedOwnerDialog.cancelUiLockSelection();
                Globals.mainWindow.clearUiLockState();
            }
            return 0;
        }

        for (int directionIndex = 0; directionIndex < DIRECTION_COUNT; directionIndex++) {
            if (!isOuterCompassRectHit(directionIndex, x, y)) {
                continue;
            }
            if (!selectCatalogDirection(directionIndex)) {
                continue;
            }

            stateFlags |= STATE_FORWARD_CENTER_ANIMATION;
            stateFlags &= ~STATE_RETURN_CENTER_ANIMATION;
            centerAnimationFrame = 0;
            onCatalogDirectionSelected(directionIndex);
            refreshOwnerShopGridSelection(resolvedOwnerDialog);
            return 0;
        }
        return 0;
    }

    /**
     * vtbl +0x58: ShopCompassVisualObject::OnLButtonUp @004BC257.
     * Full port.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        ShopDialogVisualObject resolvedOwnerDialog = ownerDialog;
        CMainWindow mainWindow = Globals.mainWindow;
        if (mainWindow.getUiLockPayload() != null) {
            resolvedOwnerDialog.uiLockPlacementAllowedFlag = 0;
            CMousePointer.Cursor_Default.setToMousePointer();
            resolvedOwnerDialog.cancelUiLockSelection();
            mainWindow.clearUiLockState();
        }
        return 1;
    }

    /**
     * Native support extracted from ShopCompassVisualObject::AdvanceDirectionAnimationFrame @004BBE9D.
     * Full support port.
     */
    private void advanceDirectionAnimationFrame(int directionIndex) {
        int currentFrameIndex = getDirectionFrameIndex(directionIndex) + 1;
        if (currentFrameIndex == 9) {
            currentFrameIndex = DIRECTION_FRAME_RESTART_INDEX;
        } else if (currentFrameIndex == DIRECTION_FRAME_STOP_INDEX) {
            stateFlags &= ~(1 << directionIndex);
            releaseDirectionAnimationFrames(directionIndex);
        }
        setDirectionFrameIndex(directionIndex, currentFrameIndex);
    }

    /**
     * Java helper for the owner screen-space anchor used by the native hit-test branches.
     * not ported.
     */
    protected Point getOwnerTopLeftScreenPoint(ShopDialogVisualObject resolvedOwnerDialog) {
        CRect ownerScreenRect = new CRect();
        resolvedOwnerDialog.clientToScreen(ownerScreenRect, resolvedOwnerDialog.cRect);
        return new Point(ownerScreenRect.left, ownerScreenRect.top);
    }

    /**
     * Java helper for the native `outerCompassRects[idx]` array.
     * not ported.
     */
    protected CRect getOuterCompassRect(int directionIndex) {
        return switch (directionIndex) {
            case 0, 1, 2, 3 -> outerCompassRects[directionIndex];
            default -> new CRect();
        };
    }

    /**
     * Java helper for the native `innerCompassRects[idx]` array.
     * not ported.
     */
    protected CRect getInnerCompassRect(int directionIndex) {
        return switch (directionIndex) {
            case 0, 1, 2, 3 -> innerCompassRects[directionIndex];
            default -> new CRect();
        };
    }

    /**
     * Java helper for the native per-direction frame-index table at `+0x244`.
     * not ported.
     */
    private int getDirectionFrameIndex(int directionIndex) {
        return switch (directionIndex) {
            case 0 -> directionFrameIndex0;
            case 1 -> directionFrameIndex1;
            case 2 -> directionFrameIndex2;
            case 3 -> directionFrameIndex3;
            default -> 0;
        };
    }

    /**
     * Java helper for the native per-direction frame-index table at `+0x244`.
     * not ported.
     */
    protected void setDirectionFrameIndex(int directionIndex, int frameIndex) {
        switch (directionIndex) {
            case 0 -> directionFrameIndex0 = frameIndex;
            case 1 -> directionFrameIndex1 = frameIndex;
            case 2 -> directionFrameIndex2 = frameIndex;
            case 3 -> directionFrameIndex3 = frameIndex;
            default -> {
            }
        }
    }

    /**
     * Java helper for the unresolved native animation timer globals near ShopCompassVisualObject::Update.
     * not ported.
     */
    private static void initializeAnimationTimers(long now) {
        if (!animationTimersInitialized) {
            animationTimersInitialized = true;
            lastDirectionAnimationTick = now - ANIMATION_TICK_MILLIS;
            lastCenterAnimationStateTick = now;
        }
    }

    /**
     * Java helper for the native direction-animation loop inside ShopCompassVisualObject::Update.
     * not ported.
     */
    private void advanceAllDirectionAnimations() {
        for (int directionIndex = 0; directionIndex < DIRECTION_COUNT; directionIndex++) {
            if ((stateFlags & (1 << directionIndex)) == 0) {
                continue;
            }
            CBmp64k[] directionFrames = this.directionFrames[directionIndex];
            int currentFrameIndex = getDirectionFrameIndex(directionIndex);
            if (currentFrameIndex < 0 || currentFrameIndex >= directionFrames.length || directionFrames[currentFrameIndex] == null) {
                continue;
            }
            advanceDirectionAnimationFrame(directionIndex);
        }
    }

    /**
     * Native support extracted from the center-state machine tail in ShopCompassVisualObject::Update @004BB3D7.
     * Full port.
     */
    private void advanceCenterAnimationState(long now) {
        long elapsed = now - lastCenterAnimationStateTick;
        if ((stateFlags & (STATE_TRIGGER_CENTER_ANIMATION | STATE_FORWARD_CENTER_ANIMATION | STATE_RETURN_CENTER_ANIMATION)) == 0) {
            int idleDelay = IDLE_RANDOM_DELAY_BASE_MILLIS
                    + Utils.randExclusive(0, IDLE_RANDOM_DELAY_VARIANTS) * IDLE_RANDOM_DELAY_STEP_MILLIS;
            if (elapsed < idleDelay) {
                return;
            }
            stateFlags |= STATE_TRIGGER_CENTER_ANIMATION;
        }

        if ((stateFlags & STATE_TRIGGER_CENTER_ANIMATION) != 0) {
            loadCenterTriggerFrame();
            if (centerAnimationFrame == CENTER_TRIGGER_FRAME_COUNT) {
                stateFlags &= ~STATE_TRIGGER_CENTER_ANIMATION;
                centerAnimationFrame = 0;
                lastCenterAnimationStateTick = now;
                return;
            }
            return;
        }

        if ((stateFlags & STATE_FORWARD_CENTER_ANIMATION) != 0) {
            centerAnimationFrame++;
            if (centerAnimationFrame == CENTER_SEQUENCE_FRAME_COUNT) {
                stateFlags &= ~STATE_FORWARD_CENTER_ANIMATION;
                centerAnimationFrame = 0;
                lastCenterAnimationStateTick = now;
                releaseCenterForwardFrames();
            }
            return;
        }

        if ((stateFlags & STATE_RETURN_CENTER_ANIMATION) != 0) {
            centerAnimationFrame++;
            if (centerAnimationFrame == CENTER_SEQUENCE_FRAME_COUNT) {
                stateFlags &= ~STATE_RETURN_CENTER_ANIMATION;
                centerAnimationFrame = 0;
                lastCenterAnimationStateTick = now;
                releaseCenterReturnFrames();
            }
        }
    }

    /**
     * Native support extracted from direction-frame render anchors inside ShopCompassVisualObject::Update @004BB3D7.
     */
    private void drawDirectionFrames(Point ownerTopLeft) {
        for (int directionIndex = 0; directionIndex < DIRECTION_COUNT; directionIndex++) {
            if ((stateFlags & (1 << directionIndex)) == 0) {
                continue;
            }
            CBmp64k[] directionFrames = this.directionFrames[directionIndex];
            int currentFrameIndex = getDirectionFrameIndex(directionIndex);
            if (currentFrameIndex < 0 || currentFrameIndex >= directionFrames.length) {
                continue;
            }

            CBmp64k currentFrame = directionFrames[currentFrameIndex];
            if (currentFrame == null) {
                continue;
            }

            CRect drawAnchor = getInnerCompassRect(directionIndex);
            drawBitmap(currentFrame, ownerTopLeft.x + drawAnchor.left, ownerTopLeft.y + drawAnchor.top);
        }
    }

    /**
     * Native support extracted from ShopCompassVisualObject::Update @004BB3D7.
     */
    protected void drawBaseCompassArt(CRect screenRect) {
        if (shopFrameSprite != null) {
            shopFrameSprite.draw(screenRect.left, screenRect.top, 0, 0, false);
        }
        if (backgroundBitmap != null) {
            drawBitmap(backgroundBitmap, screenRect.left + 5, screenRect.top + 8);
        }
    }

    /**
     * Java helper for the native center-sprite selection inside ShopCompassVisualObject::Update.
     * not ported.
     */
    private CBmp64k resolveCenterBitmap() {
        if ((stateFlags & STATE_TRIGGER_CENTER_ANIMATION) != 0) {
            return centerTriggerBitmap;
        }
        if ((stateFlags & STATE_FORWARD_CENTER_ANIMATION) != 0) {
            if (centerAnimationFrame >= 0 && centerAnimationFrame < centerForwardFrames.length) {
                return centerForwardFrames[centerAnimationFrame];
            }
            return null;
        }
        if ((stateFlags & STATE_RETURN_CENTER_ANIMATION) != 0) {
            if (centerAnimationFrame >= 0 && centerAnimationFrame < centerReturnFrames.length) {
                return centerReturnFrames[centerAnimationFrame];
            }
            return null;
        }
        return centerIdleBitmap;
    }

    /**
     * Native support extracted from the confirmation-prompt draw tail in
     * ShopCompassVisualObject::Update @004BB3D7.
     */
    private void renderConfirmationPrompt() {
        Point mousePoint = new Point(Globals.mousePointer.getX(), Globals.mousePointer.getY());
        CRect promptRect = new CRect(
                mousePoint.x + confirmationDialogRect.left,
                mousePoint.y + confirmationDialogRect.top,
                mousePoint.x + confirmationDialogRect.right,
                mousePoint.y + confirmationDialogRect.bottom
        );
        Globals.renderer.applyShadeToRect(
                promptRect.left,
                promptRect.top,
                promptRect.right,
                promptRect.bottom,
                CONFIRMATION_PROMPT_BRIGHTNESS
        );
        int centerX = promptRect.left + promptRect.width() / 2;
        Globals.fonts.font2.drawTextInternal(
                centerX,
                promptRect.top + 5,
                get(MAIN_DO_YOU_WANT_TO_IDENTIFY_THIS_ITEM_79),
                TextAlign.CENTER.mask,
                Palettes.gray
        );
        Globals.fonts.font2.drawTextInternal(
                centerX,
                promptRect.top + 0x0F,
                get(MAIN_FOR_D_GOLD_COINS_80),
                TextAlign.CENTER.mask,
                Palettes.gray
        );
        drawConfirmationButtonLabel(offsetConfirmationButtonRect(confirmationYesButtonRect, promptRect), YES_LABEL);
        drawConfirmationButtonLabel(offsetConfirmationButtonRect(confirmationNoButtonRect, promptRect), NO_LABEL);
    }

    /**
     * vtbl +0xA8: ShopCompassVisualObject::GetHoveredControlRegion @004C33F0.
     * Full port.
     */
    private int getHoveredControlRegion() {
        return COMPASS_HOVER_REGION;
    }

    /**
     * Java helper for the native owner-relative outer-rect hit tests used by GetText and OnLButtonDown.
     * Native support extracted from ShopCompassVisualObject::GetText @004BB2C4 and
     * ShopCompassVisualObject::OnLButtonDown @004BBFA6.
     */
    protected boolean isOuterCompassRectHit(int directionIndex, int x, int y) {
        Point ownerTopLeft = getOwnerTopLeftScreenPoint(ownerDialog);
        CRect compassRect = getOuterCompassRect(directionIndex);
        CRect absoluteRect = new CRect(
                ownerTopLeft.x + compassRect.left,
                ownerTopLeft.y + compassRect.top,
                ownerTopLeft.x + compassRect.right,
                ownerTopLeft.y + compassRect.bottom
        );
        return absoluteRect.contains(x, y);
    }

    /**
     * vtbl +0xA4: ShopCompassVisualObject::SelectCatalogDirection @004BBD46.
     * Full port.
     */
    protected boolean selectCatalogDirection(int directionIndex) {
        ShopDialogVisualObject resolvedOwnerDialog = ownerDialog;
        if ((resolvedOwnerDialog.selectedCatalogCategoryIndex & 0xffff) == (directionIndex & 0xffff)) {
            return false;
        }

        restartDirectionSelectionSound(resolvedOwnerDialog.shopDepartSound);
        if (resolvedOwnerDialog.selectedCatalogCategoryIndex == 100) {
            stateFlags = 0;
            directionFrameIndex0 = 0;
            directionFrameIndex1 = 0;
            directionFrameIndex2 = 0;
            directionFrameIndex3 = 0;
            releaseAnimationResources();
        } else {
            setDirectionFrameIndex(resolvedOwnerDialog.selectedCatalogCategoryIndex, 9);
        }

        stateFlags |= 1 << directionIndex;
        loadDirectionSelectionFrames(directionIndex);
        setDirectionFrameIndex(directionIndex, 0);
        resolvedOwnerDialog.selectedCatalogCategoryIndex = directionIndex;
        resetOwnerCatalogGridVisibleStart(resolvedOwnerDialog);
        return true;
    }

    /**
     * Native support extracted from ShopCompassVisualObject::OnLButtonDown @004BBFA6,
     * DruidShopCompassVisualObject::OnLButtonDown @004BFDF1, and
     * KaargShopCompassVisualObject::OnLButtonDown @004C2F57.
     * Full support port of the owner catalog-grid category refresh and draw tail.
     */
    protected void refreshOwnerShopGridSelection(ShopDialogVisualObject resolvedOwnerDialog) {
        resolvedOwnerDialog.shopCatalogGrid.selectCatalogCategory(resolvedOwnerDialog.selectedCatalogCategoryIndex);
        resolvedOwnerDialog.shopCatalogGrid.draw();
    }

    /**
     * Native support boundary for the self-owned post-selection hook at `vtbl +0x80`
     * used by ShopCompassVisualObject::OnLButtonDown @004BBFA6.
     * Native support extracted from the base-class `LoadCenterForwardFrames` callback.
     */
    private void onCatalogDirectionSelected(@SuppressWarnings("unused") int directionIndex) {
        loadCenterForwardFrames();
    }

    /**
     * vtbl +0x88: ShopCompassVisualObject::LoadCenterTriggerFrame @004BB040.
     * Full port.
     */
    public void loadCenterTriggerFrame() {
        releaseCenterTriggerFrame();
        centerAnimationFrame++;
        centerAnimationFrame %= CENTER_TRIGGER_FRAME_COUNT + 2;
        centerTriggerBitmap = new CBmp64k(String.format(
                Locale.ROOT,
                MOVIES_SHOPANIM_POSE2_3_FRAME_BMP_PATTERN,
                centerAnimationFrame + 1
        ));
    }

    /**
     * vtbl +0x80: ShopCompassVisualObject::LoadCenterForwardFrames @004BAE3E.
     * Full port.
     */
    public void loadCenterForwardFrames() {
        releaseCenterForwardFrames();
        centerForwardFrames[0] = new CBmp64k(MOVIES_SHOPANIM_POSE2_3_1_BMP);
        for (int frameIndex = 1; frameIndex < centerForwardFrames.length; frameIndex++) {
            centerForwardFrames[frameIndex] = new CBmp64k(String.format(
                    Locale.ROOT,
                    MOVIES_SHOPANIM_YES_FRAME_BMP_PATTERN,
                    frameIndex + 1
            ));
        }
    }

    /**
     * vtbl +0x84: ShopCompassVisualObject::LoadCenterReturnFrames @004BAF3F.
     * Full port.
     */
    public void loadCenterReturnFrames() {
        releaseCenterReturnFrames();
        centerReturnFrames[0] = new CBmp64k(MOVIES_SHOPANIM_POSE2_3_1_BMP);
        for (int frameIndex = 1; frameIndex < centerReturnFrames.length; frameIndex++) {
            centerReturnFrames[frameIndex] = new CBmp64k(String.format(
                    Locale.ROOT,
                    MOVIES_SHOPANIM_NO_FRAME_BMP_PATTERN,
                    frameIndex + 1
            ));
        }
    }

    /**
     * vtbl +0x8C: ShopCompassVisualObject::LoadDirectionAnimationFrames @004BACBD.
     * Full port.
     */
    protected void loadDirectionSelectionFrames(int directionIndex) {
        releaseDirectionAnimationFrames(directionIndex);
        for (int frameIndex = 0; frameIndex < directionFrames[directionIndex].length; frameIndex++) {
            String resourcePath = String.format(
                    Locale.ROOT,
                    GRAPHICS_INTERFACE_SHOPANIM_DIRECTION_BMP_PATTERN,
                    DIRECTION_COUNT - directionIndex,
                    frameIndex + 1
            );
            directionFrames[directionIndex][frameIndex] = new CBmp64k(resourcePath);
        }
        setDirectionFrameIndex(directionIndex, 0);
    }

    /**
     * vtbl +0x90: ShopCompassVisualObject::ReleaseDirectionAnimationFrames @004BADA1.
     * Full Java logical port. Native bitmap deletion is represented by clearing Java references.
     */
    protected void releaseDirectionAnimationFrames(int directionIndex) {
        for (int frameIndex = 0; frameIndex < directionFrames[directionIndex].length; frameIndex++) {
            directionFrames[directionIndex][frameIndex] = null;
        }
    }

    /**
     * vtbl +0xA0: ShopCompassVisualObject::ReleaseAnimationResources @004BB261.
     * Full port.
     */
    public void releaseAnimationResources() {
        for (int directionIndex = 0; directionIndex < DIRECTION_COUNT; directionIndex++) {
            releaseDirectionAnimationFrames(directionIndex);
        }
        releaseCenterForwardFrames();
        releaseCenterReturnFrames();
        releaseCenterTriggerFrame();
    }

    /**
     * vtbl +0x94: ShopCompassVisualObject::ReleaseCenterForwardFrames @004BB110.
     * Full Java logical port. Native bitmap deletion is represented by clearing Java references.
     */
    protected void releaseCenterForwardFrames() {
        for (int frameIndex = 0; frameIndex < centerForwardFrames.length; frameIndex++) {
            centerForwardFrames[frameIndex] = null;
        }
    }

    /**
     * vtbl +0x98: ShopCompassVisualObject::ReleaseCenterReturnFrames @004BB18D.
     * Full Java logical port. Native bitmap deletion is represented by clearing Java references.
     */
    protected void releaseCenterReturnFrames() {
        for (int frameIndex = 0; frameIndex < centerReturnFrames.length; frameIndex++) {
            centerReturnFrames[frameIndex] = null;
        }
    }

    /**
     * vtbl +0x9C: ShopCompassVisualObject::ReleaseCenterTriggerFrame @004BB20A.
     * Full Java logical port. Native bitmap deletion is represented by clearing the Java reference.
     */
    protected void releaseCenterTriggerFrame() {
        centerTriggerBitmap = null;
    }

    /**
     * Native helper thunk pair: Sound::StopAndRewindPointerSound @004385B0 + Sound::PlayPointer @00438570.
     * not ported.
     */
    protected static void restartDirectionSelectionSound(Sound sound) {
        if (sound == null) {
            return;
        }
        SoundSystem.get().stopAndRewind(sound);
        sound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
    }

    /**
     * Native helper tail in ShopCompassVisualObject::SelectCatalogDirection @004BBD46.
     * Partial port. Java resets the visible-start binding that backs the owner catalog grid, while the exact
     * category-array pointer rebinding still remains on the unresolved shop-grid support side.
     */
    protected static void resetOwnerCatalogGridVisibleStart(ShopDialogVisualObject resolvedOwnerDialog) {
        if (resolvedOwnerDialog.shopCatalogGrid.visibleStartRef instanceof int[] visibleStart
                && visibleStart.length > 0) {
            visibleStart[0] = 0;
            return;
        }
        resolvedOwnerDialog.shopCatalogGrid.visibleStartRef = new int[]{0};
    }

    /**
     * Java helper for the partially recovered confirmation-click branch in ShopCompassVisualObject::OnLButtonDown.
     * Native support extracted from ShopCompassVisualObject::OnLButtonDown @004BBFA6.
     */
    private boolean handleConfirmationClick(int x, int y) {
        ShopDialogVisualObject resolvedOwnerDialog = ownerDialog;

        Point ownerTopLeft = getOwnerTopLeftScreenPoint(resolvedOwnerDialog);
        CRect promptRect = new CRect(
                ownerTopLeft.x + confirmationDialogRect.left,
                ownerTopLeft.y + confirmationDialogRect.top,
                ownerTopLeft.x + confirmationDialogRect.right,
                ownerTopLeft.y + confirmationDialogRect.bottom
        );
        CRect yesRect = offsetConfirmationButtonRect(confirmationYesButtonRect, promptRect);
        if (yesRect.contains(x, y)) {
            return true;
        }
        CRect noRect = offsetConfirmationButtonRect(confirmationNoButtonRect, promptRect);
        return noRect.contains(x, y);
    }

    /**
     * Java helper for the native `confirmationDialogRect + buttonRect` geometry composition.
     * Native support extracted from ShopCompassVisualObject::Update @004BB3D7 and
     * ShopCompassVisualObject::OnLButtonDown @004BBFA6.
     */
    private static CRect offsetConfirmationButtonRect(CRect buttonRect, CRect promptRect) {
        return new CRect(
                promptRect.left + buttonRect.left,
                promptRect.top + buttonRect.top,
                promptRect.left + buttonRect.right,
                promptRect.top + buttonRect.bottom
        );
    }

    /**
     * Native support extracted from confirmation yes/no label draws in ShopCompassVisualObject::Update @004BB3D7.
     */
    private static void drawConfirmationButtonLabel(CRect buttonRect, String label) {
        int mouseX = Globals.mousePointer.getX();
        int mouseY = Globals.mousePointer.getY();
        Palette16 palette = buttonRect.contains(mouseX, mouseY) ? Palettes.yellowish : Palettes.gray;
        Globals.fonts.font2.drawTextInternal(
                buttonRect.left + buttonRect.width() / 2,
                buttonRect.top + 1,
                label,
                TextAlign.CENTER.mask,
                palette
        );
    }

    /**
     * Native support extracted from CGameBitmap::Draw call sites in ShopCompassVisualObject::Update @004BB3D7.
     */
    protected static void drawBitmap(CBmp64k bitmap, int x, int y) {
        bitmap.draw(x, y, 0, 0, false);
    }

}
