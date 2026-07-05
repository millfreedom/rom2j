package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.*;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.sound.Sound;
import ua.millfreedom.rom2.model.sound.SoundSystem;
import ua.millfreedom.rom2.model.window.CMainWindow;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static ua.millfreedom.rom2.model.window.windowproc.handlers.CMainWindowWindowProcSupport.readMessageInt;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.MODAL_DIALOG;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.SHOP_DIALOG;

/**
 * Native class: ShopDialogVisualObject.
 * Purpose: shop dialog base with shared catalog, transfer, and compass controls.
 */
public class ShopDialogVisualObject extends HandlerVisualObject {
    public static final int NATIVE_SIZE = 0x164; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int HOVERED_SELECTION_INFO_PANEL_REGION = 0x66;
    private static final int SHOP_AMBIENT_IDLE_BASE_DELAY_MS = 30000;
    private static final int SHOP_AMBIENT_IDLE_RANDOM_DELAY_MS = 30000;
    private static boolean shopAmbientTimerInitialized;
    private static int shopAmbientTick;

    private static final String GRAPHICS_INTERFACE_MYITEM_256 = "graphics/interface/myitem.256";
    private static final String GRAPHICS_INTERFACE_SHOPITEM_256 = "graphics/interface/shopitem.256";
    private static final String GRAPHICS_INTERFACE_COSTS_BMP_PATTERN = "graphics/interface/costs%d.bmp";
    private static final String GRAPHICS_INTERFACE_COSTM_BMP_PATTERN = "graphics/interface/costm%d.bmp";
    private static final String GRAPHICS_INTERFACE_BACKINVG_BMP = "graphics/interface/backinvg.bmp";
    private static final String GRAPHICS_INTERFACE_BACKINVB_BMP = "graphics/interface/backinvb.bmp";
    private static final String GRAPHICS_INTERFACE_BACKINVS_BMP = "graphics/interface/backinvs.bmp";

    private static final String SFX_TOWN_SHOP_NOFIT_WAV = "SFX/Town/Shop/nofit.wav";
    private static final String SFX_TOWN_SHOP_STEP1_WAV = "SFX/Town/Shop/step1.wav";
    private static final String SFX_TOWN_SHOP_STEP2_WAV = "SFX/Town/Shop/step2.wav";
    private static final String SFX_TOWN_SHOP_BREATH_WAV = "SFX/Town/Shop/breath.wav";
    private static final String SFX_TOWN_SHOP_DEPART_WAV = "SFX/Town/Shop/depart.wav";
    private static final String SFX_TOWN_BUY_WAV = "SFX/Town/buy.wav";
    private static final String SFX_TOWN_SELL_WAV = "SFX/Town/sell.wav";
    private static final String SFX_TOWN_SHOP_ENTER_WAV = "SFX/Town/Shop/enter.wav";
    private static final String SFX_TOWN_SHOP_START_WAV = "SFX/Town/Shop/start.wav";
    private static final String SFX_TOWN_SHOP_POVOROT1_WAV = "SFX/Town/Shop/Povorot1.wav";
    private static final String SFX_TOWN_SHOP_POVOROT2_WAV = "SFX/Town/Shop/Povorot2.wav";
    private static final String SFX_TOWN_SHOP_INSHOP_WAV = "SFX/Town/Shop/InShop.wav";
    private static final String SFX_OUT_WAV = "SFX/Out.wav";
    private static final String SFX_UNDO_WAV = "SFX/Undo.wav";

    //0x68
    public ShopCatalogGridVisualObject shopCatalogGrid;
    //0x6c
    public ShopSelectionGridVisualObject unitInventoryGrid;
    //0x70
    public ShopTransferGridVisualObject tradeTransferGrid;
    //0x74
    public ShopCompassVisualObject shopCompass;
    //0x78
    public ShopRingButtonsVisualObject ringButtons;
    //0x7c
    public SelectionInfoPanelVisualObject selectionInfoPanel;
    //0x80
    public MapVisualObject mapVisual;
    //0x84
    public SpellPanelVisualObject embeddedSpellPanel;
    //0x88
    public TipsPromptDialogVisualObject tipsPrompt;
    //0x8c
    public int tipsPromptTextUpdatedFlag;

    //0x90
    public Sound noFitSound;
    //0x94
    public Sound shopStep1Sound;
    //0x98
    public Sound shopStep2Sound;
    //0x9c
    public Sound shopBreathSound;
    //0xa0
    public Sound shopDepartSound;
    //0xa4
    public Sound buySound;
    //0xa8
    public Sound sellSound;
    //0xac
    public Sound shopEnterSound;
    //0xb0
    public Sound shopStartSound;
    //0xb4
    public Sound shopTurn1Sound;
    //0xb8
    public Sound shopTurn2Sound;
    //0xbc
    public Sound shopInShopSound;
    //0xc0
    public Sound outSound;
    //0xc4
    public Sound undoSound;

    //0xc8
    public CBmp64k backInventoryGreenBitmap;
    //0xcc
    public CBmp64k backInventoryBlueBitmap;
    //0xd0
    public CBmp64k backInventorySelectedBitmap;
    //0xd4
    public CSprite256 playerItemSprite;
    //0xd8
    public CSprite256 shopItemSprite;

    //0xdc
    public final List<CBmp64k> costMediumBitmaps = new ArrayList<>();
    //0xf0
    public final List<CBmp64k> costSmallBitmaps = new ArrayList<>();
    //0x104
    public int hoveredControlRegion;
    //0x108
    public final List<CUnit> selectedPrimaryUnits = new ArrayList<>();
    //0x11c
    public final List<Integer> selectedPrimaryUnitOrder = new ArrayList<>();
    //0x130
    public int selectedUnitIndex;
    //0x132
    public int selectedCatalogCategoryIndex;
    //0x144
    public int uiLockPlacementAllowedFlag;
    //0x148
    public int dialogActiveFlag;
    //0x14c
    public int dirtyFlags;
    //0x150
    public int currentGold;
    //0x154
    public int pendingBuyGoldDelta;
    //0x158
    public int pendingSellGoldValue;
    //0x15c
    public int resultingGold;
    //0x160
    public int shopScenarioTalkTarget;

    /**
     * Native: ShopDialogVisualObject::ShopDialogVisualObject @004B6A62.
     * Full port.
     */
    public ShopDialogVisualObject() {
        super();
        initializeShopDialogConstructorState();
    }

    /**
     * Native: ShopDialogVisualObject::ShopDialogVisualObject @004B6CE7.
     * Full port.
     */
    public ShopDialogVisualObject(int id, CRect rect, Object handler) {
        super(id, rect, handler);
        initializeShopDialogConstructorState();
    }

    /**
     * Native: ShopDialogVisualObject::ShopDialogVisualObject @004B6BB2.
     * Full port.
     */
    public ShopDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, CSprite256 image) {
        super(id, xLeft, yTop, xRight, yBottom, image);
        initializeShopDialogConstructorState();
    }

    /**
     * Native support extracted from ShopDialogVisualObject constructors @004B6A62, @004B6BB2, and @004B6CE7.
     * Full support port.
     */
    private void initializeShopDialogConstructorState() {
        this.hoveredControlRegion = -1;
        this.selectedUnitIndex = 0;
        this.selectedCatalogCategoryIndex = 0;
        this.uiLockPlacementAllowedFlag = 1;
        this.dialogActiveFlag = 0;
        this.dirtyFlags = 0;
        this.currentGold = 0;
        this.pendingBuyGoldDelta = 0;
        this.pendingSellGoldValue = 0;
        this.resultingGold = 0;
        this.shopScenarioTalkTarget = 0;
    }

    /**
     * vtbl +0x30: ShopDialogVisualObject::RenderSelf @004B8007.
     * Full port. Java uses the renderer wrapper for LockBackBufferDirectDrawSurface @00452BDE,
     * FillScreenRect @00456348, and UnlockBackBufferDirectDrawSurface @00452C3A.
     */
    @Override
    public void renderSelf(CRect clipRect) {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        if (dialogActiveFlag == 0) {
            return;
        }
        Globals.renderer.lockSurface();
        try {
            Globals.renderer.fillScreenRect(
                    screenRect.left,
                    screenRect.top + 0x184,
                    screenRect.right + 0x1D0,
                    screenRect.bottom + 0x188,
                    (short) 0
            );
        } finally {
            Globals.renderer.unlockSurface();
        }
    }

    /**
     * Native: ShopDialogVisualObject::DrawSelectionInfoPanelLeftBackdrop @004B758A.
     * Full port.
     */
    void drawSelectionInfoPanelLeftBackdrop() {
        if (dialogActiveFlag == 0 || selectionInfoPanel == null) {
            return;
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        CBmp64k backdrop = selectionInfoPanel.getSelectionInfoViewMode() == 0
                ? GUI.textBackL
                : GUI.humanBackL;
        backdrop.drawRectMasked(screenRect.left + 0x1D0, screenRect.top + 0xEE, 0, 0, 0x10, 0xF2);
    }

    /**
     * vtbl +0x48: ShopDialogVisualObject::OnMessage @004B7102.
     * Full port. Java preserves the native message fan-out and delegates child callbacks to helpers with their own status comments.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        int w = readMessageInt(wParam);
        switch (msg) {
            case RENDER_FRAME -> handleRenderFrameMessage();
            case TOGGLE_SPELL_PANEL -> handleToggleSpellPanelMessage();
            case SHOP_ITEM_GRID_TRANSFER -> handleShopItemGridTransferMessage(w, lParam);
            case SELECT_PREVIOUS_HERO -> {
                shiftSelectedUnitBackward();
                dirtyFlags |= 0x1;
            }
            case SELECT_NEXT_HERO -> {
                shiftSelectedUnitForward();
                dirtyFlags |= 0x1;
            }
            case CLEAR_TIP_PROMPT -> removeTipsPrompt();
            case TEXT_LIST_SELECTION_CHANGED -> handleSelectionChangedMessage(w);
            default -> {
            }
        }
        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * vtbl +0x4C: ShopDialogVisualObject::OnMouseMove @004B7638.
     * Full port. Java uses TokenEntry adapter helpers for the native carried-entry type and value reads.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        int localX = x - cRect.left;
        int localY = y - cRect.top;
        if (Globals.mainWindow.getUiLockPayload() != null
                && isUiLockPlacementRejected(localX, localY)) {
            uiLockPlacementAllowedFlag = 0;
            CMousePointer.Cursor_CantPut.setToMousePointer();
        }
        if (selectionInfoPanel.getRect().contains(localX, localY)
                && hoveredControlRegion != HOVERED_SELECTION_INFO_PANEL_REGION) {
            hoveredControlRegion = HOVERED_SELECTION_INFO_PANEL_REGION;
            refreshSelectionInfoPanelHoverState();
        }
        return 0;
    }

    /**
     * vtbl +0x58: ShopDialogVisualObject::OnLButtonUp @004B78B2.
     * Full port.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        if (Globals.mainWindow.getUiLockPayload() != null
                && embeddedSpellPanel != null
                && embeddedSpellPanel.getRect().contains(x, y)) {
            cancelUiLockSelection();
            return 1;
        }
        if (Globals.mainWindow.getUiLockPayload() != null
                && !cRect.contains(x, y)) {
            cancelUiLockSelection();
            return 1;
        }
        return super.onLButtonUp(nFlags, x, y);
    }

    /**
     * vtbl +0x6C: ShopDialogVisualObject::OnKeyDown @004B70A2.
     * Full port.
     */
    @Override
    public int onKeyDown(int nChar) {
        if (nChar == 0x0D) {
            return 1;
        }
        if (nChar != 0x1B) {
            return 0;
        }
        handleExitAction();
        if (Globals.mainWindow.dialogsMask == SHOP_DIALOG.mask) {
            Globals.mainWindow.postMessage(MessageCodes.SHOW_CURRENT_TOWN_DIALOG, 0, 0);
        }
        return 1;
    }

    /**
     * vtbl +0x78: ShopDialogVisualObject::InitializeBitmapCatalogDialog @004B85E2.
     * Full port.
     */
    @Override
    public void initialize() {
        tipsPrompt = null;
        tradeTransferGrid = new ShopTransferGridVisualObject(0x3EB, 0, 0x12F, 0x1E0, 0x186, this);
        unitInventoryGrid = new ShopSelectionGridVisualObject(0x3E9, 0, 0x186, 0x1E0, 0x1E0, this);
        shopCatalogGrid = new ShopCatalogGridVisualObject(0x3EA, 0, 0, 0xA4, 0x12F, this);
        shopCompass = new ShopCompassVisualObject(0x3ED, 0xA4, 0, 0x1E0, 0x12F, this);
        ringButtons = new ShopRingButtonsVisualObject(0x3EE, 0x1D0, 0, 0x280, 0xEE, this);
        addChild(shopCatalogGrid);
        addChild(unitInventoryGrid);
        addChild(tradeTransferGrid);
        addChild(shopCompass);
        addChild(ringButtons);
        selectionInfoPanel = null;
        mapVisual = null;
        embeddedSpellPanel = null;
        noFitSound = null;
        shopStep1Sound = null;
        shopStep2Sound = null;
        shopBreathSound = null;
        shopDepartSound = null;
        buySound = null;
        sellSound = null;
        shopEnterSound = null;
        shopStartSound = null;
        shopTurn1Sound = null;
        shopTurn2Sound = null;
        shopInShopSound = null;
        outSound = null;
        undoSound = null;
        backInventoryGreenBitmap = null;
        backInventoryBlueBitmap = null;
        backInventorySelectedBitmap = null;
        playerItemSprite = null;
        shopItemSprite = null;
    }

    /**
     * vtbl +0x80: ShopDialogVisualObject::ShowDialog @004B8B98.
     * Java port status: native setup ported; Java binds the inventory visible-start pointer to the same selected unit
     * as the grid source so shop inventory refreshes preserve the displayed unit's scroll position.
     */
    @Override
    public void showDialog() {
        Globals.mousePointer.disableBackgroundCapture();
        ringButtons.initializeButtonState();
        initializeTradeTransferGridOverlay();
        initializeShopCatalogGridOverlay();
        shopCompass.loadVisualResources();
        ringButtons.loadButtonBitmaps();
        loadCatalogArt();
        loadCatalogSounds();
        mapVisual = Globals.mainWindow.getMapVisual();
        selectionInfoPanel = Globals.mainWindow.pSelectionInfoPanelVisualObject;
        if (!shouldShowTipsPrompt()) {
            removeTipsPrompt();
        } else {
            createTipsPrompt();
        }
        tipsPromptTextUpdatedFlag = 0;
        moveSelectionInfoPanelIntoDialog();
        loadSelectedUnits();
        unitInventoryGrid.visibleStartRef = getSelectedShopUnit().shopInventoryVisibleStart;
        resetShopCatalogGridVisibleStart();
        selectedCatalogCategoryIndex = 100;
        prepareCompassSelection();
        bindInitialSelectedUnitInventory();
        shopCatalogGrid.selectCatalogCategory(0);
        shopCatalogGrid.clearCatalogCategoryEntries();
        tradeTransferGrid.clearTransferEntries();
        mapVisual.onMessage(MessageCodes.REFRESH_LAYOUT, 0, 0);
        CUnit selectedUnit = getSelectedShopUnit();
        selectedUnit.setSelected(true);
        mapVisual.updateSelectionState();
        selectedUnit.unitFlags |= 0x08;
        refreshShopItemGrid(unitInventoryGrid);
        refreshShopItemGrid(shopCatalogGrid);
        refreshShopItemGrid(tradeTransferGrid);
        dirtyFlags |= 0x2F;
        shopCatalogGrid.loadGridSounds();
        unitInventoryGrid.loadGridSounds();
        tradeTransferGrid.loadGridSounds();
        clearScreen();
        super.showDialog();
        dialogActiveFlag = 1;
        CMousePointer.Cursor_Default.setToMousePointer();

        shopEnterSound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);

        shopInShopSound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, true, Sound.POINTER_SFX_PRIORITY, 0);

        initializeVariantAmbientTimers();
        if (shopStartSound != null) {
            shopStartSound.playIfNotPlaying(Globals.soundPreferences.speechVolume, false, (byte) 0x80, 0);
        }
        if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN) {
            shopScenarioTalkTarget = Globals.scenarioLib.enterShop();
        } else {
            shopScenarioTalkTarget = 0;
        }
        if (shopScenarioTalkTarget != 0) {
            RoleDialogSupport.showRoleKeyDialog(String.format(
                    Locale.ROOT,
                    "shop_npc31m_%d",
                    (shopScenarioTalkTarget >>> 16) & 0xFFF
            ));
            Globals.scenarioLib.talkTo(shopScenarioTalkTarget);
        }
        Globals.mousePointer.enableBackgroundCapture();
    }

    /**
     * Native variant hook in DruidShopDialogVisualObject::ShowDialog @004BE6B0 and
     * KaargShopDialogVisualObject::ShowDialog @004C03C2.
     * Full support port. The base shop dialog has no variant ambient timers.
     */
    protected void initializeVariantAmbientTimers() {
    }

    /**
     * vtbl +0x84: ShopDialogVisualObject::HideDialog @004B8954.
     * Full port. Child cleanup calls are represented by support helpers/classes with their own port-status comments.
     */
    @Override
    public HandlerVisualObject hideDialog(MessageCodes reason) {
        draw();
        dialogActiveFlag = 0;
        releaseCatalogSounds();
        if (Globals.mainWindow.getUiLockPayload() != null) {
            cancelUiLockSelection();
            CMousePointer.Cursor_Default.setToMousePointer();
            Globals.mainWindow.clearUiLockState();
            uiLockPlacementAllowedFlag = 0;
        }
        removeTipsPrompt();
        shopCompass.stateFlags = 0;
        restoreSelectionInfoPanelToSideBar();
        if (embeddedSpellPanel != null) {
            removeChild(embeddedSpellPanel);
        }
        embeddedSpellPanel = null;
        releaseTradeTransferGridOverlay();
        releaseShopCatalogGridOverlay();
        ringButtons.releaseButtonBitmaps();
        shopCompass.releaseVisualResources();
        shopCompass.releaseAnimationResources();
        releaseCatalogArt();
        shopCatalogGrid.clearCatalogCategoryEntries();
        shopCatalogGrid.releaseGridSounds();
        unitInventoryGrid.releaseGridSounds();
        tradeTransferGrid.releaseGridSounds();
        HandlerVisualObject result = super.hideDialog(reason);
        if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN) {
            Globals.scenarioLib.leaveShop();
        }
        return result;
    }

    /**
     * vtbl +0x88: ShopDialogVisualObject::UpdateShopAmbientSound @004B978F.
     * Full port.
     */
    public void updateShopAmbientSound() {
        if (!shopAmbientTimerInitialized) {
            shopAmbientTimerInitialized = true;
            shopAmbientTick = (int) System.currentTimeMillis();
        }

        int currentTick = (int) System.currentTimeMillis();
        int ambientDelay = Utils.randInclusive(SHOP_AMBIENT_IDLE_RANDOM_DELAY_MS)
                + SHOP_AMBIENT_IDLE_BASE_DELAY_MS;
        int stateFlags = shopCompass.stateFlags;
        if ((stateFlags & 0x70) == 0) {
            if (ambientDelay < currentTick - shopAmbientTick
                    && !SoundSystem.get().isSoundPlaying(tradeTransferGrid.primaryGridSound)
                    && shopStartSound != null
                    && !SoundSystem.get().isSoundPlaying(shopStartSound)) {
                shopStartSound.playIfNotPlaying(Globals.soundPreferences.speechVolume, false, (byte) 0x80, 0);
                shopAmbientTick = currentTick;
                Utils.randInclusive(10000);
            }
            return;
        }
        if ((stateFlags & 0x10) != 0) {
            switch (shopCompass.centerAnimationFrame) {
                case 1 -> playPointerSound(shopTurn1Sound);
                case 10, 0x0E, 0x12, 0x16 -> playPointerSound(shopStep1Sound);
                case 0x18 -> playPointerSound(shopTurn2Sound);
                default -> {
                }
            }
        }
    }

    /**
     * vtbl +0x8C: ShopDialogVisualObject::LoadCatalogSounds @004B995B.
     * Full port.
     */
    public void loadCatalogSounds() {
        releaseCatalogSounds();
        noFitSound = loadSound(noFitSound, SFX_TOWN_SHOP_NOFIT_WAV);
        shopStep1Sound = loadSound(shopStep1Sound, SFX_TOWN_SHOP_STEP1_WAV);
        shopStep2Sound = loadSound(shopStep2Sound, SFX_TOWN_SHOP_STEP2_WAV);
        shopBreathSound = loadSound(shopBreathSound, SFX_TOWN_SHOP_BREATH_WAV);
        shopDepartSound = loadSound(shopDepartSound, SFX_TOWN_SHOP_DEPART_WAV);
        buySound = loadSound(buySound, SFX_TOWN_BUY_WAV);
        sellSound = loadSound(sellSound, SFX_TOWN_SELL_WAV);
        shopEnterSound = loadSound(shopEnterSound, SFX_TOWN_SHOP_ENTER_WAV);
        shopStartSound = loadSound(shopStartSound, SFX_TOWN_SHOP_START_WAV);
        shopTurn1Sound = loadSound(shopTurn1Sound, SFX_TOWN_SHOP_POVOROT1_WAV);
        shopTurn2Sound = loadSound(shopTurn2Sound, SFX_TOWN_SHOP_POVOROT2_WAV);
        shopInShopSound = loadSound(shopInShopSound, SFX_TOWN_SHOP_INSHOP_WAV);
        outSound = loadSound(outSound, SFX_OUT_WAV);
        undoSound = loadSound(undoSound, SFX_UNDO_WAV);
    }

    /**
     * vtbl +0x90: ShopDialogVisualObject::ReleaseCatalogSounds @004B9AB1.
     * Full port.
     */
    public void releaseCatalogSounds() {
        noFitSound = releaseSound(noFitSound);
        shopStep1Sound = releaseSound(shopStep1Sound);
        shopStep2Sound = releaseSound(shopStep2Sound);
        shopBreathSound = releaseSound(shopBreathSound);
        shopDepartSound = releaseSound(shopDepartSound);
        buySound = releaseSound(buySound);
        sellSound = releaseSound(sellSound);
        shopEnterSound = releaseSound(shopEnterSound);
        shopStartSound = releaseSound(shopStartSound);
        shopTurn1Sound = releaseSound(shopTurn1Sound);
        shopTurn2Sound = releaseSound(shopTurn2Sound);
        shopInShopSound = releaseSound(shopInShopSound);
        outSound = releaseSound(outSound);
        undoSound = releaseSound(undoSound);
    }

    /**
     * Native support thunk: DeleteSound @00438480.
     * Full support port.
     */
    private static Sound releaseSound(Sound sound) {
        if (sound != null) {
            sound.release();
        }
        return null;
    }

    /**
     * Native support thunk: LoadSound @004384F0.
     * Full support port.
     */
    private static Sound loadSound(Sound sound, String resourcePath) {
        releaseSound(sound);
        return new Sound(resourcePath);
    }

    /**
     * Native support thunk: Sound::PlayPointer @00438570.
     * Full support port for single sound slots.
     */
    private static void playPointerSound(Sound sound) {
        if (sound != null) {
            sound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);
        }
    }

    /**
     * vtbl +0x94: ShopDialogVisualObject::GetShopResourceDirectory @004C33C0.
     * Full port.
     */
    public String getShopResourceDirectory() {
        return "";
    }

    /**
     * Native: ShopDialogVisualObject::CancelUiLockSelection @004B91E8.
     * Full port. Child complete/drop callbacks keep their own port-status comments.
     */
    public void cancelUiLockSelection() {
        CMousePointer.Cursor_Default.setToMousePointer();
        int sourceIndex = Globals.mainWindow.getUiLockSourceIndex();
        switch (Globals.mainWindow.getUiLockPackedModeCode()) {
            case 1 -> {
                selectionInfoPanel.applyCarriedTokenToSelectionSlot(sourceIndex);
                dirtyFlags |= 0x8;
            }
            case 2 -> {
                unitInventoryGrid.completeUiDrag(sourceIndex);
                refreshShopItemGrid(unitInventoryGrid);
                dirtyFlags |= 0x1;
            }
            case 4 -> {
                tradeTransferGrid.completeUiDrag(sourceIndex);
                refreshShopItemGrid(tradeTransferGrid);
                if (embeddedSpellPanel == null) {
                    dirtyFlags |= 0x4;
                }
            }
            case 5, 6, 7, 8 -> {
                shopCatalogGrid.completeUiDrag(sourceIndex);
                refreshShopItemGrid(shopCatalogGrid);
                dirtyFlags |= 0x2;
            }
            default -> {
            }
        }
        Globals.mainWindow.clearUiLockState();
    }

    /**
     * Native: ShopDialogVisualObject::HandleSellAction @004B935D.
     * Full port.
     */
    public boolean handleSellAction() {
        recomputeCatalogTransactionTotals();
        if (pendingSellGoldValue == 0) {
            return true;
        }
        applyAcceptedSellAction();
        return true;
    }

    /**
     * Native: ShopDialogVisualObject::HandleBuyAction @004B93CF.
     * Full port.
     */
    public boolean handleBuyAction() {
        recomputeCatalogTransactionTotals();
        if (pendingBuyGoldDelta == 0) {
            return false;
        }
        if (currentGold + pendingBuyGoldDelta < 0) {
            applyRejectedBuyAction();
            return false;
        }
        applyAcceptedBuyAction();
        return true;
    }

    /**
     * Native: ShopDialogVisualObject::HandleUndoAction @004B954D.
     * Full port.
     */
    public void handleUndoAction() {
        clearPendingCatalogTransactions();
        applyUndoAction();
    }

    /**
     * Native: ShopDialogVisualObject::HandleExitAction @004B956E.
     * Full port.
     */
    public void handleExitAction() {
        if (hasLinkedSpellPanel()) {
            restoreEmbeddedPanel();
            removeMapSpellPanel();
        }
        clearPendingCatalogTransactions();
        onMessage(MessageCodes.DIALOG_OK, 0, 0);
    }

    /**
     * Native support extracted from ShopRingButtonsVisualObject::OnLButtonDown @004BD988.
     * Full support port.
     */
    public void playRingButtonsPressSound(int buttonIndex) {
        if (buttonIndex == 0) {

            undoSound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);

        } else if (buttonIndex == 3) {

            outSound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);

        }
    }

    /**
     * Native support extracted from ShopDialogVisualObject::OnMessage @004B7102.
     * Full port of the render-frame branch.
     */
    private void handleRenderFrameMessage() {
        if (MODAL_DIALOG.isSetIn(Globals.mainWindow.dialogsMask)) {
            return;
        }
        refreshTipsPromptState();
        shopCompass.draw();
        unitInventoryGrid.draw();
        shopCatalogGrid.draw();
        if (embeddedSpellPanel == null) {
            tradeTransferGrid.draw();
        } else {
            embeddedSpellPanel.draw();
        }
        selectionInfoPanel.draw();
        recomputeCatalogTransactionTotals();
        ringButtons.draw();
    }

    /**
     * Native support extracted from ShopDialogVisualObject::OnMessage @004B7102.
     * Full port of the toggle branch.
     */
    private void handleToggleSpellPanelMessage() {
        if (embeddedSpellPanel == null) {
            prepareEmbeddedPanel();
        } else {
            restoreEmbeddedPanel();
        }
    }

    /**
     * Native support extracted from ShopDialogVisualObject::OnMessage @004B7102.
     * Full port of the shop-item transfer branch. Payload adapters are documented on the helper methods they use.
     */
    private void handleShopItemGridTransferMessage(int wParam, Object transferredEntriesParam) {
        if (transferredEntriesParam == null) {
            refreshSelectedShopUnitInventoryGrid();
            refreshShopItemGrid(shopCatalogGrid);
            refreshShopItemGrid(tradeTransferGrid);
            getSelectedShopUnit().unitFlags |= 0x08;
            dirtyFlags |= 0x28;
            return;
        }
        List<TokenEntry> transferredEntries = mutableEntryList(transferredEntriesParam);
        switch (wParam) {
            case 1 -> {
                mapVisual.getSelectedCUnit().unitFlags |= 0x08;
                dirtyFlags |= 0x8;
            }
            case 2 -> {
                invokeGridTransferCommit(unitInventoryGrid, transferredEntries);
                refreshShopItemGrid(unitInventoryGrid);
            }
            case 4 -> {
                invokeGridTransferCommit(tradeTransferGrid, transferredEntries);
                refreshShopItemGrid(tradeTransferGrid);
                recomputeCatalogTransactionTotals();
                dirtyFlags |= 0x20;
            }
            case 5, 6, 7, 8 -> {
                applyDirectionalTransfer(wParam - 5, transferredEntries);
                refreshShopItemGrid(shopCatalogGrid);
            }
            default -> {
            }
        }
        transferredEntries.clear();
    }

    /**
     * Java shop-context support for ItemListAction subtype-2 routing from
     * MapVisualObject::HandleGameAction @0040D9B2 and ShopDialogVisualObject::OnMessage @004B7102.
     * Native routes a null payload refresh through the shop dialog; Java carries the updated unit so the open shop
     * inventory can keep the same selected-unit source/ref binding after buy/sell inventory updates.
     * not ported as a standalone native method.
     */
    public void handleShopUnitInventoryUpdated(CUnit updatedUnit) {
        if (updatedUnit == getSelectedShopUnit()) {
            bindSelectedShopUnitInventorySourceAndRef();
        }
        refreshShopItemGrid(unitInventoryGrid);
        refreshShopItemGrid(shopCatalogGrid);
        refreshShopItemGrid(tradeTransferGrid);
        updatedUnit.unitFlags |= 0x08;
        dirtyFlags |= 0x28;
    }

    /**
     * Native support extracted from ShopDialogVisualObject::OnMessage @004B7102.
     * Full port of the text-list selection branch.
     */
    private void handleSelectionChangedMessage(int wParam) {
        switch (wParam) {
            case 1, 2 -> refreshShopItemGrid(unitInventoryGrid);
            case 4 -> {
                refreshShopItemGrid(tradeTransferGrid);
                recomputeCatalogTransactionTotals();
            }
            case 5, 6, 8 -> refreshShopItemGrid(shopCatalogGrid);
            case 7 -> {
                refreshShopItemGrid(unitInventoryGrid);
                refreshShopItemGrid(tradeTransferGrid);
            }
            default -> {
            }
        }
    }

    /**
     * Native: ShopDialogVisualObject::PrepareEmbeddedPanel @004B95B7.
     * Full port.
     */
    private void prepareEmbeddedPanel() {
        embeddedSpellPanel = (SpellPanelVisualObject) Globals.mainWindow.getSpellPanelVisual();
        removeChild(tradeTransferGrid);
    }

    /**
     * Native: ShopDialogVisualObject::RestoreEmbeddedPanel @004B95E5.
     * Full port.
     */
    private void restoreEmbeddedPanel() {
        embeddedSpellPanel = null;
        addChild(tradeTransferGrid);
    }

    /**
     * Native: ShopDialogVisualObject::RecomputeCatalogTransactionTotals @004B960C.
     * Full port. Current-gold and entry-field reads are factored through native-support helpers below.
     */
    void recomputeCatalogTransactionTotals() {
        currentGold = mapVisual.currentPlayer.gold;
        pendingSellGoldValue = 0;
        pendingBuyGoldDelta = 0;
        for (TokenEntry entry : resolveTransactionEntries()) {
            int value = getCatalogEntryValue(entry);
            int quantity = entry.quantity;
            if (getCatalogEntryType(entry) == 2) {
                pendingSellGoldValue += ((value + 1) / 2) * quantity;
            } else {
                pendingBuyGoldDelta -= value * quantity;
            }
        }
        resultingGold = currentGold + pendingBuyGoldDelta + pendingSellGoldValue;
    }

    /**
     * Native: ShopDialogVisualObject::RefreshCatalogPanelState @004B9144.
     * Full port.
     */
    private void refreshTipsPromptState() {
        if (tipsPrompt == null || tipsPromptTextUpdatedFlag != 0) {
            return;
        }
        if (resolveTransactionEntries().isEmpty()) {
            return;
        }
        tipsPromptTextUpdatedFlag = 1;
        tipsPrompt.setPromptText(ScriptDataSupport.getTipText(4));
    }

    /**
     * Native helper: FUN_004B15A3 @004B15A3.
     * Native support extracted from shop-grid refresh call sites.
     * Full support port.
     */
    private void refreshShopItemGrid(CVisualObject child) {
        if (child instanceof ShopItemGridVisualObject itemGrid) {
            itemGrid.resetCellUpdateCountersAndRefresh();
        } else if (child != null) {
            child.draw();
        }
    }

    /**
     * Native: ShopDialogVisualObject::RefreshSelectionInfoPanelHoverState @004B9710.
     * Full port.
     */
    private void refreshSelectionInfoPanelHoverState() {
        ringButtons.initializeButtonState();
        dirtyFlags &= ~0x200;
        dirtyFlags &= ~0x400;
        dirtyFlags &= ~0x80;
        dirtyFlags &= ~0x100;
        dirtyFlags |= 0x20;
    }

    /**
     * Native: ShopDialogVisualObject::ShiftSelectionBackward @004B8273.
     * Full port.
     */
    private void shiftSelectedUnitBackward() {
        mapVisual.onMessage(MessageCodes.REFRESH_LAYOUT, 0, 0);
        if (selectedUnitIndex == 0) {
            selectedUnitIndex = selectedPrimaryUnits.size() - 1;
        } else {
            selectedUnitIndex--;
        }
        bindSelectedUnitInventory();
        dirtyFlags |= 0x9;
    }

    /**
     * Native: ShopDialogVisualObject::ShiftSelectionForward @004B8102.
     * Full port.
     */
    private void shiftSelectedUnitForward() {
        mapVisual.onMessage(MessageCodes.REFRESH_LAYOUT, 0, 0);
        selectedUnitIndex++;
        if (selectedUnitIndex >= selectedPrimaryUnits.size()) {
            selectedUnitIndex = 0;
        }
        bindSelectedUnitInventory();
        dirtyFlags |= 0x9;
    }

    /**
     * Native: ShopDialogVisualObject::ApplyDirectionalTransfer @004B8443.
     * Full Java logical port. Native object deletion is represented by normal Java collection ownership.
     */
    private void applyDirectionalTransfer(
            int directionIndex,
            List<TokenEntry> transferredEntries
    ) {
        for (TokenEntry entry : transferredEntries) {
            setCatalogEntryDirection(entry, directionIndex);
        }
        for (int sourceIndex = 0; sourceIndex < transferredEntries.size(); sourceIndex++) {
            transferredEntries.get(sourceIndex).sourceSlotDescriptor = sourceIndex;
        }
        sortCatalogEntriesForShelf(transferredEntries);
        List<TokenEntry> categoryEntries = shopCatalogGrid.catalogCategoryEntriesAt(directionIndex);
        categoryEntries.clear();
        categoryEntries.addAll(transferredEntries);
        transferredEntries.clear();
    }

    /**
     * Native support equivalent to ShopDialogVisualObject::SortCatalogEntriesForShelf @004B6A3C.
     * Full support port. Java List.sort represents native CArray::GetData + qsort over TokenEntry pointers.
     */
    private void sortCatalogEntriesForShelf(List<TokenEntry> entries) {
        entries.sort(this::compareCatalogEntriesForShelf);
    }

    /**
     * Native support equivalent to ShopDialogVisualObject::CompareCatalogEntriesForShelf @004B6790.
     * Full support port.
     */
    private int compareCatalogEntriesForShelf(TokenEntry left, TokenEntry right) {
        return Comparator
                .comparingInt((TokenEntry entry) -> (entry.wireFlags & 0x2) == 0 ? 1 : 0)
                .thenComparingInt(TokenEntry::getType)
                .thenComparingInt(this::getNativeItemShelfGroup)
                .thenComparingInt(TokenEntry::getCatalogEntryValue)
                .compare(left, right);
    }

    /**
     * Native support extracted from ShopDialogVisualObject catalog-entry comparator @004B6790.
     */
    private int getNativeItemShelfGroup(TokenEntry entry) {
        if (entry.getType() != 1) {
            return 1;
        }
        int id = entry.getId();
        if (id >= 1 && id <= 6) {
            return 1;
        }
        if (id >= 18 && id <= 19) {
            return 2;
        }
        if (id >= 15 && id <= 17) {
            return 3;
        }
        if (id >= 7 && id <= 12) {
            return 5;
        }
        if (id >= 20 && id <= 22) {
            return 6;
        }
        return 7;
    }

    /**
     * Native: ShopDialogVisualObject::LoadCatalogArt @004B796F.
     * Full port.
     */
    private void loadCatalogArt() {
        releaseCatalogArt();
        playerItemSprite = new CSprite256(GRAPHICS_INTERFACE_MYITEM_256);
        playerItemSprite.initPalette(1, 1, 0);
        Globals.renderer.refreshMousePointer();
        shopItemSprite = new CSprite256(GRAPHICS_INTERFACE_SHOPITEM_256);
        shopItemSprite.initPalette(1, 1, 0);
        Globals.renderer.refreshMousePointer();
        for (int index = 1; index <= 7; index++) {
            costSmallBitmaps.add(new CBmp64k(String.format(Locale.ROOT, GRAPHICS_INTERFACE_COSTS_BMP_PATTERN, index)));
            Globals.renderer.refreshMousePointer();
            costMediumBitmaps.add(new CBmp64k(String.format(Locale.ROOT, GRAPHICS_INTERFACE_COSTM_BMP_PATTERN, index)));
            Globals.renderer.refreshMousePointer();
        }
        backInventoryGreenBitmap = new CBmp64k(GRAPHICS_INTERFACE_BACKINVG_BMP);
        Globals.renderer.refreshMousePointer();
        backInventoryBlueBitmap = new CBmp64k(GRAPHICS_INTERFACE_BACKINVB_BMP);
        Globals.renderer.refreshMousePointer();
        backInventorySelectedBitmap = new CBmp64k(GRAPHICS_INTERFACE_BACKINVS_BMP);
        Globals.renderer.refreshMousePointer();
    }

    /**
     * Native: ShopDialogVisualObject::ReleaseCatalogArt @004B7D68.
     * Full Java logical port. Native bitmap deletion is represented by clearing Java references/lists.
     */
    private void releaseCatalogArt() {
        playerItemSprite = null;
        shopItemSprite = null;
        backInventoryGreenBitmap = null;
        backInventoryBlueBitmap = null;
        backInventorySelectedBitmap = null;
        costSmallBitmaps.clear();
        costMediumBitmaps.clear();
    }

    /**
     * Native helper in ShopDialogVisualObject::ShowDialog @004B8B98.
     * Full port of the selection-info rehost sequence.
     */
    private void moveSelectionInfoPanelIntoDialog() {
        CVisualObject rightPanelContainer = Globals.mainWindow.getRightPanelContainerVisual();
        rightPanelContainer.removeChild(selectionInfoPanel);
        CRect panelRect = selectionInfoPanel.getRect();
        panelRect.offset(0x280 - panelRect.width(), 0);
        selectionInfoPanel.setBounds(panelRect);
        addChild(selectionInfoPanel);
    }

    /**
     * Native helper in ShopDialogVisualObject::HideDialog @004B8954.
     * Full port of the selection-info restore sequence.
     */
    private void restoreSelectionInfoPanelToSideBar() {
        CVisualObject rightPanelContainer = Globals.mainWindow.getRightPanelContainerVisual();
        CRect panelRect = selectionInfoPanel.getRect();
        panelRect.offset(panelRect.width() - 0x280, 0);
        selectionInfoPanel.setBounds(panelRect);
        removeChild(selectionInfoPanel);
        rightPanelContainer.addChild(selectionInfoPanel);
        selectionInfoPanel = null;
        mapVisual = null;
    }

    /**
     * Native helper in ShopDialogVisualObject::ShowDialog @004B8B98.
     * Native support extracted from the selected-units snapshot rebuild and primary-unit array copy.
     * Full support port.
     */
    private void loadSelectedUnits() {
        SelectedUnitsSnapshot selectedUnitsSnapshot = SelectedUnitsSnapshot.GLOBAL;
        selectedUnitsSnapshot.rebuildFromCurrentPlayerUnits(mapVisual);
        selectedPrimaryUnits.clear();
        selectedPrimaryUnits.addAll(selectedUnitsSnapshot.getPrimaryUnits());
        selectedUnitIndex = selectedUnitsSnapshot.findFlag20PrimaryUnitIndex();
    }

    /**
     * Native helper in ShopDialogVisualObject::ShowDialog @004B8B98.
     * Native support extracted from the initial selected-unit inventory source binding.
     * Full support port.
     */
    private void bindInitialSelectedUnitInventory() {
        bindSelectedShopUnitInventorySourceAndRef();
    }

    /**
     * Native helper shared by ShopDialogVisualObject::ShiftSelectionBackward @004B8273 and @004B8102.
     * Full port.
     */
    private void bindSelectedUnitInventory() {
        CUnit selectedUnit = getSelectedShopUnit();
        selectedUnit.setSelected(true);
        mapVisual.updateSelectionState();
        unitInventoryGrid.setGridSource(selectedUnit.tokenEntries);
        unitInventoryGrid.visibleStartRef = selectedUnit.shopInventoryVisibleStart;
        refreshShopItemGrid(unitInventoryGrid);
        unitInventoryGrid.clampVisibleStart();
        selectedUnit.unitFlags |= 0x08;
    }

    /**
     * Java shop-context support extracted from the selected-unit source/ref binding in
     * ShopDialogVisualObject::ShowDialog @004B8B98, ShiftSelectionForward @004B8102, and
     * ShiftSelectionBackward @004B8273.
     * not ported as a standalone native method.
     */
    private void bindSelectedShopUnitInventorySourceAndRef() {
        CUnit selectedUnit = getSelectedShopUnit();
        unitInventoryGrid.setGridSource(selectedUnit.tokenEntries);
        unitInventoryGrid.visibleStartRef = selectedUnit.shopInventoryVisibleStart;
    }

    /**
     * Java shop-context support extracted from ShopDialogVisualObject::OnMessage @004B7102 subtype-2/null refresh
     * and the selected-unit source/ref binding in ShowDialog @004B8B98.
     * not ported as a standalone native method.
     */
    private void refreshSelectedShopUnitInventoryGrid() {
        bindSelectedShopUnitInventorySourceAndRef();
        refreshShopItemGrid(unitInventoryGrid);
    }

    /**
     * Java shop-context support extracted from ShopDialogVisualObject::ShowDialog @004B8B98 selected-unit source
     * binding and the selected-unit source/ref binding in ShiftSelectionForward @004B8102 and
     * ShiftSelectionBackward @004B8273.
     * not ported as a standalone native method.
     */
    CUnit getSelectedShopUnit() {
        return selectedPrimaryUnits.get(selectedUnitIndex & 0xFFFF);
    }

    /**
     * Java shop-context support for MapVisualObject::sendInventoryTransferAction @0041A20C packet dispatch.
     * Native GridOverlayVisualObject uses MapVisualObject primary selection; Java shop grids use the dialog-selected
     * unit that owns the displayed inventory source.
     * not ported as a standalone native method.
     */
    void sendSelectedShopUnitInventoryTransferAction(
            int sourceContainerType,
            int sourceSlot,
            int destinationContainerType,
            int destinationSlot,
            int quantityOrItemId
    ) {
        mapVisual.sendInventoryTransferAction(
                getSelectedShopUnit(),
                sourceContainerType,
                sourceSlot,
                destinationContainerType,
                destinationSlot,
                quantityOrItemId
        );
    }

    /**
     * Native: ShopDialogVisualObject::ClearPendingCatalogTransactions @004B94A4.
     * Full port.
     */
    private void clearPendingCatalogTransactions() {
        recomputeCatalogTransactionTotals();
        List<TokenEntry> transferEntries = mutableEntryList(tradeTransferGrid.gridSource);
        while (!transferEntries.isEmpty()) {
            TokenEntry entry = transferEntries.get(0);
            Object payload = tradeTransferGrid.beginUiDrag(0, entry.quantity);
            if (payload == null) {
                continue;
            }
            if (getCatalogEntryType(payload) == 2) {
                unitInventoryGrid.completeUiDrag(-1);
            } else {
                shopCatalogGrid.completeUiDrag(-1);
            }
        }
    }

    /**
     * Native support extracted from the map rollback side effect in ShopDialogVisualObject::HandleUndoAction @004B954D.
     * Native support extracted from MapVisualObject::CommitShopUndoAction @0041A43B.
     * Full support port.
     */
    private void applyUndoAction() {
        mapVisual.commitShopUndoAction(getSelectedShopUnit());
    }

    /**
     * Native support extracted from ShopDialogVisualObject::HandleBuyAction @004B93CF.
     * Full support port.
     */
    private void applyRejectedBuyAction() {
        shopCompass.loadCenterReturnFrames();
        shopCompass.stateFlags |= 0x40;

        noFitSound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);

    }

    /**
     * Native support extracted from ShopDialogVisualObject::HandleBuyAction @004B93CF.
     * Full support port.
     */
    private void applyAcceptedBuyAction() {
        shopCompass.loadCenterForwardFrames();
        shopCompass.stateFlags |= 0x20;
        mapVisual.commitShopBuyAction(getSelectedShopUnit());

        buySound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);

    }

    /**
     * Native support extracted from ShopDialogVisualObject::HandleSellAction @004B935D.
     * Full support port.
     */
    private void applyAcceptedSellAction() {
        shopCompass.loadCenterForwardFrames();
        shopCompass.stateFlags |= 0x20;
        mapVisual.commitShopSellAction(getSelectedShopUnit());

        sellSound.playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, Sound.POINTER_SFX_PRIORITY, 0);

    }

    /**
     * Native support extracted from the recovered `field0x84 != 0` check in ShopDialogVisualObject::HandleExitAction @004B956E.
     * Full support port.
     */
    private boolean hasLinkedSpellPanel() {
        return embeddedSpellPanel != null;
    }

    /**
     * Native support extracted from ShopDialogVisualObject::HandleExitAction @004B956E.
     * Full support port.
     */
    private void removeMapSpellPanel() {
        mapVisual.removeSpellPanelForTownDialog();
    }

    /**
     * Native helper in ShopDialogVisualObject::OnMouseMove @004B7638.
     * Full support port of the carried-entry placement branch.
     */
    private boolean isUiLockPlacementRejected(int localX, int localY) {
        Object payload = Globals.mainWindow.getUiLockPayload();
        int payloadType = getCatalogEntryType(payload);
        boolean inUnitInventory = unitInventoryGrid.getRect().contains(localX, localY);
        boolean inSelectionInfoPanel = selectionInfoPanel.getRect().contains(localX, localY);
        if ((inUnitInventory || inSelectionInfoPanel) && payloadType >= 5 && payloadType <= 8) {
            return true;
        }
        boolean inShopCatalog = shopCatalogGrid.getRect().contains(localX, localY);
        if (inShopCatalog && payloadType <= 2) {
            return true;
        }
        boolean inTradeTransfer = tradeTransferGrid.getRect().contains(localX, localY);
        if (!inUnitInventory && !inTradeTransfer && !inShopCatalog && !inSelectionInfoPanel) {
            return true;
        }
        if (inUnitInventory) {
            return false;
        }
        return getCatalogEntryValue(payload) == 0;
    }

    /**
     * Native helper near ShopDialogVisualObject::ShowDialog @004B8B98.
     * Native support extracted from the `field0x68 + vtbl +0xB4` call.
     * Full support port.
     */
    private void initializeShopCatalogGridOverlay() {
        shopCatalogGrid.initializeOverlayBitmaps();
    }

    /**
     * Native helper near ShopDialogVisualObject::ShowDialog @004B8B98.
     * Native support extracted from the `field0x70 + vtbl +0xB4` call.
     * Full support port.
     */
    private void initializeTradeTransferGridOverlay() {
        tradeTransferGrid.initializeOverlayBitmaps();
    }

    /**
     * Native helper near ShopDialogVisualObject::ShowDialog @004B8B98.
     * Native support extracted from the `field0x74 + vtbl +0xA4` default-direction call.
     * Full support port.
     */
    private void prepareCompassSelection() {
        shopCompass.selectCatalogDirection(0);
    }

    /**
     * Native helper near ShopDialogVisualObject::ShowDialog @004B8B98.
     * Native support extracted from the shop-catalog visible-start reset.
     * Full support port.
     */
    private void resetShopCatalogGridVisibleStart() {
        if (shopCatalogGrid.visibleStartRef instanceof int[] visibleStart
                && visibleStart.length > 0) {
            visibleStart[0] = 0;
            return;
        }
        shopCatalogGrid.visibleStartRef = new int[]{0};
    }

    /**
     * Native helper near ShopDialogVisualObject::HideDialog @004B8954.
     * Native support extracted from the `field0x70 + vtbl +0xB8` call.
     * Full support port.
     */
    private void releaseTradeTransferGridOverlay() {
        tradeTransferGrid.releaseOverlayBitmaps();
    }

    /**
     * Native helper near ShopDialogVisualObject::HideDialog @004B8954.
     * Native support extracted from the `field0x68 + vtbl +0xB8` call.
     * Full support port.
     */
    private void releaseShopCatalogGridOverlay() {
        shopCatalogGrid.releaseOverlayBitmaps();
    }

    /**
     * Native helper in ShopDialogVisualObject::ShowDialog @004B8B98.
     * Native support extracted from the `g_GamePreferences.TipsMode` / campaign-mode branch.
     * Full support port.
     */
    private boolean shouldShowTipsPrompt() {
        return Globals.gamePreferences.tipsMode != 0
                && Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN;
    }

    /**
     * Native helper in ShopDialogVisualObject::ShowDialog @004B8B98.
     * Full support port.
     */
    private void createTipsPrompt() {
        removeTipsPrompt();
        tipsPrompt = new TipsPromptDialogVisualObject(0x3F3, 0, 0xA2, 0x138, 0x12A);
        tipsPrompt.setPromptText(ScriptDataSupport.getTipText(3));
        shopCompass.addChild(tipsPrompt);
    }

    /**
     * Native inline tips-prompt removal tails in ShopDialogVisualObject::OnMessage @004B7102 and ShopDialogVisualObject::HideDialog @004B8954.
     * Full support port.
     */
    private void removeTipsPrompt() {
        if (tipsPrompt == null) {
            return;
        }
        if (shopCompass != null) {
            shopCompass.removeChild(tipsPrompt);
        }
        tipsPrompt.detachFromParentSlot(1);
        tipsPrompt = null;
    }

    /**
     * Native support adapter for the `field0x70 + 0x84` transaction-array source in ShopDialogVisualObject::RecomputeCatalogTransactionTotals @004B960C.
     * Native support extracted from the transfer-grid CArray source read.
     * Full support port.
     */
    private List<TokenEntry> resolveTransactionEntries() {
        if (tradeTransferGrid.gridSource instanceof List<?> list) {
            return new ArrayList<>(castCatalogEntries(list));
        }
        throw new IllegalStateException("Shop transfer grid source is not a CArray-compatible list");
    }

    /**
     * Native support adapter for CArray<TokenEntry> payloads passed to ShopDialogVisualObject::OnMessage @004B7102.
     * Native support extracted from ShopDialogVisualObject::OnMessage @004B7102 and ApplyDirectionalTransfer @004B8443.
     * Full support port.
     */
    @SuppressWarnings("unchecked")
    private List<TokenEntry> mutableEntryList(Object transferredEntries) {
        if (transferredEntries instanceof List<?> list) {
            return (List<TokenEntry>) list;
        }
        throw new IllegalArgumentException("Shop transfer payload is not a CArray-compatible list");
    }

    /**
     * Native support adapter for shop-grid CArray sources used by ShopDialogVisualObject::ClearPendingCatalogTransactions @004B94A4.
     * Full support port for the Java list adapter.
     */
    @SuppressWarnings("unchecked")
    private List<TokenEntry> mutableGridSourceList(ShopItemGridVisualObject itemGrid) {
        if (itemGrid.gridSource instanceof List<?> list) {
            return (List<TokenEntry>) list;
        }
        throw new IllegalStateException("Shop item-grid source is not a CArray-compatible list");
    }

    /**
     * Native support adapter for CArray<TokenEntry> reads in ShopDialogVisualObject own methods.
     * Native support extracted from ShopDialogVisualObject::RecomputeCatalogTransactionTotals @004B960C.
     * Full support port.
     */
    @SuppressWarnings("unchecked")
    private List<TokenEntry> castCatalogEntries(List<?> entries) {
        return (List<TokenEntry>) entries;
    }

    /**
     * Native support adapter for entry type reads at `entry + 0x18` in ShopDialogVisualObject own methods.
     * Native support extracted from ShopDialogVisualObject::RecomputeCatalogTransactionTotals @004B960C and OnMouseMove @004B7638.
     * Full support port.
     */
    private int getCatalogEntryType(Object entry) {
        return requireCatalogEntry(entry).gridModeCode;
    }

    /**
     * Native support adapter for `FUN_004B6680` value reads in ShopDialogVisualObject own methods.
     * Native support extracted from ShopDialogVisualObject own methods.
     * Full support port.
     */
    private int getCatalogEntryValue(Object entry) {
        return requireCatalogEntry(entry).getCatalogEntryValue();
    }

    /**
     * Native support extracted from ShopDialogVisualObject::ApplyDirectionalTransfer @004B8443.
     * Full support port.
     */
    private void setCatalogEntryDirection(TokenEntry entry, int directionIndex) {
        entry.gridModeCode = directionIndex + 5;
        entry.categoryIndex = directionIndex;
    }

    /**
     * Native support adapter for TokenEntry-backed shop entry adapters in ShopDialogVisualObject own methods.
     * Native support extracted from ShopDialogVisualObject::RecomputeCatalogTransactionTotals @004B960C, OnMouseMove @004B7638, and ApplyDirectionalTransfer @004B8443.
     * Full support port.
     */
    private static TokenEntry requireCatalogEntry(Object entry) {
        if (entry instanceof TokenEntry tokenEntry) {
            return tokenEntry;
        }
        throw new IllegalArgumentException("Unsupported shop catalog entry: " + entry);
    }

    /**
     * Native support adapter for child `vtbl +0xB0` transfer commits in ShopDialogVisualObject::OnMessage @004B7102.
     * Native support extracted from child ShopItemGridVisualObject::AdoptEntriesFromArray dispatch.
     * Full support port.
     */
    private void invokeGridTransferCommit(
            CVisualObject child,
            List<TokenEntry> transferredEntries
    ) {
        if (child instanceof ShopItemGridVisualObject itemGrid) {
            itemGrid.adoptEntriesFromArray(transferredEntries);
            return;
        }
        throw new IllegalArgumentException("Shop transfer target is not a shop item grid");
    }

}
