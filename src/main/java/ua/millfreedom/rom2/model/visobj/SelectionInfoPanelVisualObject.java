package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBmp256;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CMousePointer;
import ua.millfreedom.rom2.model.CPlayer;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.MagicItem;
import ua.millfreedom.rom2.model.CUnitInfo;
import ua.millfreedom.rom2.model.StructureDef;
import ua.millfreedom.rom2.model.Structures;
import ua.millfreedom.rom2.model.TokenEntry;
import ua.millfreedom.rom2.model.UnitTypes;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.enums.SfxSounds;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.gameobj.CGameObject;
import ua.millfreedom.rom2.model.gameobj.CStructure;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.sound.SoundManager;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.res.Resources;
import ua.millfreedom.rom2.text.BuildingText;
import ua.millfreedom.rom2.text.HeroPictureText;
import ua.millfreedom.rom2.text.TextTableId;

import java.util.Arrays;
import java.util.Objects;

import static ua.millfreedom.rom2.model.window.CMainWindow.SESSION_MODE_CAMPAIGN;
import static ua.millfreedom.rom2.res.Constants.GRAPHICS;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.INN_DIALOG;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.MODAL_DIALOG;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.SHOP_DIALOG;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.*;

/**
 * Native class: SelectionInfoPanelVisualObject.
 * Purpose: right-side selected-object info panel with hero navigation and picture/stats tabs.
 */
public class SelectionInfoPanelVisualObject extends CVisualObject {
    public static final int NATIVE_SIZE = 0x17C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int MK_LBUTTON = 0x1;
    private static final int PICTURE_WIDTH = 0xA0;
    private static final int PICTURE_HEIGHT = 0xF0;
    private static final int EQUIPMENT_SLOT_COUNT = 0x0C;
    private static final int UNIT_FLAG_HUMANOID = 0x01;
    private static final int UNIT_FLAG_MAGIC_CLASS = 0x02;
    private static final int UNIT_FLAG_EQUIPMENT_PORTRAIT_DIRTY = 0x08;
    private static final int UNIT_FLAG_INFO_PORTRAIT_DYNAMIC_MASK = 0x11;
    private static final int ACTION_ATTACK = 3;
    private static final int ACTION_CAST = 7;
    private static final int ACTION_USE = 8;
    private static final int QUEST_DIALOG_TOKEN_HASH = 0x0E4D;
    private static final int TOKEN_ATTR_SPELL_ID = 0x2A;
    private static final int GRID_MODE_EQUIPMENT_SLOT = 1;
    private static final int GRID_MODE_HERO_INVENTORY = 2;
    private static final int ORDER_TYPE_CAST_SLOT_B = 10;
    private static final String INFOWINDOW_DIRECTORY = "infowindow";
    private static final String INVENTORY_DIRECTORY = "inventory";
    private static final String A16_SUFFIX = ".16a";
    private static final String BMP_SUFFIX = ".bmp";
    private static final String BLANK_INFO_PICTURE_CACHE_KEY = "<blank>";

    private static final int MODE_FLAGS_PANEL_ACTIVE_MASK = 0x627;
    private static final int MODE_FLAGS_SPELLBOOK_MASK = 0x3;
    private static final int MODE_FLAGS_BLOCK_SPELLBOOK_AND_INVENTORY = 0x4;
    private static final int MODE_FLAGS_HERO_NAV_OR_INVENTORY_MASK = 0x226;
    private static final int MODE_FLAGS_PORTRAIT_NAV_MASK = 0x200;
    private static final int MODE_FLAGS_HIDE_ESC_ICON = 0x400;
    private static final int MODE_FLAGS_HIDE_MODE_TOGGLE = 0x600;

    //0x5c
    public MapVisualObject mapContext0x5c;
    //0x60
    public int dirtyFlag0x60;
    //0x64
    public int field0x64;
    //0x68
    public int selectionPanelState0x68;
    //0x6c
    public int spellPanelState0x6c;
    //0x70
    public int selectionInfoViewMode0x70;
    //0x74
    public CBmp64k pictureBitmap0x74;
    //0x78
    public CBmp256 pictureHitMap0x78;
    //0x7c
    public String cachedInfoPictureName0x7c;

    /**
     * Native: SelectionInfoPanelVisualObject::SelectionInfoPanelVisualObject @004ADE34.
     */
    public SelectionInfoPanelVisualObject() {
        super();
        initializeSelectionInfoPanelFields();
    }

    /**
     * Native: SelectionInfoPanelVisualObject::SelectionInfoPanelVisualObject @004AE06E.
     */
    public SelectionInfoPanelVisualObject(int id, CRect rect) {
        super(id, rect, null);
        initializeSelectionInfoPanelFields();
    }

    /**
     * Native: SelectionInfoPanelVisualObject::SelectionInfoPanelVisualObject @004ADF45.
     */
    public SelectionInfoPanelVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        initializeSelectionInfoPanelFields();
    }

    /**
     * Native support extracted from SelectionInfoPanelVisualObject constructors @004ADE34, @004ADF45, and @004AE06E.
     */
    private void initializeSelectionInfoPanelFields() {
        this.mapContext0x5c = null;
        this.dirtyFlag0x60 = 0;
        this.field0x64 = 0;
        this.selectionPanelState0x68 = 0;
        this.spellPanelState0x6c = 0;
        this.selectionInfoViewMode0x70 = 1;
        this.pictureBitmap0x74 = new CBmp64k(PICTURE_WIDTH, PICTURE_HEIGHT);
        this.pictureHitMap0x78 = new CBmp256(PICTURE_WIDTH, PICTURE_HEIGHT);
        this.cachedInfoPictureName0x7c = null;
    }

    /**
     * vtbl +0x14: SelectionInfoPanelVisualObject::GetText @004AE232.
     * Full port for Java-backed selected-object tooltip data.
     */
    @Override
    public String getText() {
        if (getUiLockFlag3f4(Globals.mainWindow) != 0) {
            return null;
        }

        int modeFlags = dialogModeFlags();
        if (MODAL_DIALOG.isSetIn(modeFlags)) {
            return null;
        }

        MapVisualObject mapVisualObject = resolveMapVisualObject();
        int mouseX = Globals.mousePointer.getX();
        int mouseY = Globals.mousePointer.getY();

        CRect panelScreenRect = new CRect();
        clientToScreen(panelScreenRect, cRect);
        boolean collapsedMode = isSimpleGetTextCollapsedMode(modeFlags, panelScreenRect);

        CRect spellbookRect = new CRect(
                panelScreenRect.left, panelScreenRect.top,
                panelScreenRect.left + 0x1C, panelScreenRect.top + 0x24
        );
        CRect inventoryRect = new CRect(
                panelScreenRect.left, panelScreenRect.bottom - 0x28,
                panelScreenRect.left + 0x1C, panelScreenRect.bottom
        );
        CRect tabSwitchRect = new CRect(
                panelScreenRect.left + 0x80, panelScreenRect.top,
                panelScreenRect.right, panelScreenRect.top + 0x24
        );
        CRect prevRect = new CRect(
                panelScreenRect.left + 0x01, panelScreenRect.top + 0xCD,
                panelScreenRect.left + 0x21, panelScreenRect.top + 0xED
        );
        CRect nextRect = new CRect(
                panelScreenRect.left + 0x77, panelScreenRect.top + 0xCD,
                panelScreenRect.left + 0x97, panelScreenRect.top + 0xED
        );
        CRect escMenuRect = new CRect(
                panelScreenRect.left + 0x7E, panelScreenRect.top + 0xCE,
                panelScreenRect.left + 0x9E, panelScreenRect.top + 0xEE
        );

        if ((modeFlags & MODE_FLAGS_SPELLBOOK_MASK) != 0
                && (modeFlags & MODE_FLAGS_BLOCK_SPELLBOOK_AND_INVENTORY) == 0
                && spellbookRect.contains(mouseX, mouseY)) {
            boolean spellPanelVisible = mapVisualObject != null && mapVisualObject.hasSpellPanelChild();
            return spellPanelVisible
                    ? get(MAIN_CLOSE_SPELLBOOK_Q_B_9)
                    : get(MAIN_OPEN_SPELLBOOK_Q_B_8);
        }

        if (GAMEPLAY.isSetIn(modeFlags)
                && SHOP_DIALOG.isUnsetIn(modeFlags)
                && INN_DIALOG.isUnsetIn(modeFlags)
                && inventoryRect.contains(mouseX, mouseY)) {
            boolean inventoryPanelVisible = mapVisualObject != null && mapVisualObject.hasSelectionPanelChild();
            return inventoryPanelVisible
                    ? get(MAIN_CLOSE_INVENTORY_I_11)
                    : get(MAIN_OPEN_INVENTORY_I_10);
        }

        if ((modeFlags & MODE_FLAGS_HIDE_MODE_TOGGLE) == 0 && !collapsedMode && tabSwitchRect.contains(mouseX, mouseY)) {
            return selectionInfoViewMode0x70 == 0
                    ? get(MAIN_SWITCH_TO_PICTURE_TAB_12)
                    : get(MAIN_SWITCH_TO_STATS_TAB_13);
        }

        if ((modeFlags & MODE_FLAGS_HERO_NAV_OR_INVENTORY_MASK) == 0) {
            if ((modeFlags & MODE_FLAGS_HIDE_ESC_ICON) == 0 && escMenuRect.contains(mouseX, mouseY)) {
                return get(MAIN_MAIN_MENU_ESC_14);
            }
        } else {
            if ((modeFlags & MODE_FLAGS_PORTRAIT_NAV_MASK) != 0 && Globals.mainWindow.sessionMode != SESSION_MODE_CAMPAIGN) {
                if (prevRect.contains(mouseX, mouseY)) {
                    return get(MAIN_PREVIOUS_PORTRAIT_121);
                }
                if (nextRect.contains(mouseX, mouseY)) {
                    return get(MAIN_NEXT_PORTRAIT_122);
                }
            }
            if (prevRect.contains(mouseX, mouseY)) {
                return get(MAIN_PREVIOUS_HERO_52);
            }
            if (nextRect.contains(mouseX, mouseY)) {
                return get(MAIN_NEXT_HERO_53);
            }
        }

        return resolveSelectedObjectTooltip(mapVisualObject, modeFlags, panelScreenRect, mouseX, mouseY);
    }

    /**
     * vtbl +0x2C: SelectionInfoPanelVisualObject::Update @004AF3BF.
     * Full port for Java-backed selection panel rendering.
     */
    @Override
    public void update() {
        int modeFlags = visualDialogModeFlags();
        if ((modeFlags & MODE_FLAGS_PANEL_ACTIVE_MASK) == 0) {
            return;
        }

        CRect panelScreenRect = new CRect();
        clientToScreen(panelScreenRect, cRect);
        boolean collapsedMode = isCollapsedMode(modeFlags, panelScreenRect);
        MapVisualObject mapVisualObject = resolveMapVisualObject();

        Globals.renderer.lockSurface();
        try {
            if (collapsedMode) {
                selectionInfoViewMode0x70 = 1;
            }
            drawPanelBackground(panelScreenRect);
            drawSpellbookButton(panelScreenRect, modeFlags, mapVisualObject);
            drawInventoryOrNavigationButtons(panelScreenRect, modeFlags, mapVisualObject, Globals.mainWindow.sessionMode);
            drawModeToggleButton(panelScreenRect, modeFlags, collapsedMode);
            renderSelectedObjectInfo(panelScreenRect, Globals.mainWindow, mapVisualObject);
        } finally {
            Globals.renderer.unlockSurface();
        }
        dirtyFlag0x60 = 0;
    }

    /**
     * vtbl +0x48: SelectionInfoPanelVisualObject::OnMessage @004AE89E.
     * Full Java-backed port. Java repaints the panel on gameplay render frames because final-overlay tooltips do not
     * restore the native background surface.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        int result = super.onMessage(msg, wParam, lParam);
        boolean collapsedMode = isCollapsedMode(Globals.mainWindow);
        if (result != 0) {
            return result;
        }

        switch (msg) {
            case RENDER_FRAME -> {
                int modeFlags = dialogModeFlags();
                int visualModeFlags = visualDialogModeFlags();
                // Native gates this on dirtyFlag0x60; Java final-overlays need the panel repainted to clear tooltips.
                if (isGameplayDialogMode(visualModeFlags) || visualModeFlags == 3 || visualModeFlags == 5) {
                    draw();
                }
                if ((modeFlags & MODE_FLAGS_SPELLBOOK_MASK) != 0) {
                    updateCursorForPanelHover(Globals.mainWindow);
                }
            }
            case SET_MAP_CONTEXT -> {
                mapContext0x5c = (MapVisualObject) wParam;
                dirtyFlag0x60 = 1;
            }
            case NOTIFY_MAP_CONTEXT_CHANGED, NOTIFY_SELECTION_SPELL_STATE -> {
                dirtyFlag0x60 = 1;
            }
            case TOGGLE_SELECTION_PANEL -> {
                dirtyFlag0x60 = 1;
                selectionPanelState0x68 = selectionPanelState0x68 == 0 ? 1 : 0;
            }
            case TOGGLE_SPELL_PANEL -> {
                dirtyFlag0x60 = 1;
                spellPanelState0x6c = spellPanelState0x6c == 0 ? 1 : 0;
            }
            case TOGGLE_SELECTION_INFO_VIEW_MODE -> {
                if (!collapsedMode) {
                    SoundManager.playSfx(SfxSounds.CLICK00);
                    selectionInfoViewMode0x70 = selectionInfoViewMode0x70 == 0 ? 1 : 0;
                    dirtyFlag0x60 = 1;
                }
            }
            default -> {
            }
        }
        return result;
    }

    /**
     * vtbl +0x4C: SelectionInfoPanelVisualObject::OnMouseMove @004AF120.
     * Full port for Java-backed selected unit equipment drag-start data.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        if ((nFlags & MK_LBUTTON) == 0) {
            return 0;
        }

        CRect panelScreenRect = new CRect();
        clientToScreen(panelScreenRect, cRect);
        CMainWindow mainWindow = Globals.mainWindow;
        MapVisualObject mapVisualObject = resolveMapVisualObject();
        if (mainWindow.uiLockPayload != null
                || selectionInfoViewMode0x70 == 0
                || mapVisualObject.getSelectedCount() != 1) {
            return 0;
        }

        CUnit unit = (CUnit) mapVisualObject.getPrimarySelectedObject();
        if ((unit.unitFlags & UNIT_FLAG_HUMANOID) == 0) {
            return 0;
        }

        int slotIndex = getPictureHitSlotIndex(panelScreenRect, x, y);
        if (slotIndex >= 0) {
            TokenEntry token = beginCarrySelectionSlotToken(slotIndex);
            if (token != null) {
                beginSelectionSlotDragVisual(token);
                unit.unitFlags |= UNIT_FLAG_EQUIPMENT_PORTRAIT_DIRTY;
                onMessage(MessageCodes.NOTIFY_MAP_CONTEXT_CHANGED, 0, 0);
                m_pParent.onMessage(MessageCodes.TEXT_LIST_SELECTION_CHANGED, id, 0);
                unit.m_bSelectionDirty = 1;
            }
        }
        return 0;
    }

    /**
     * vtbl +0x54: SelectionInfoPanelVisualObject::OnLButtonDown @004AEA97.
     * Full port.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        return 1;
    }

    /**
     * vtbl +0x58: SelectionInfoPanelVisualObject::OnLButtonUp @004AEBC8.
     * Full port.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        CMainWindow mainWindow = Globals.mainWindow;
        MapVisualObject mapVisualObject = resolveMapVisualObject();

        CRect panelScreenRect = new CRect();
        clientToScreen(panelScreenRect, cRect);
        int freeBottomPixels = screenBottomY() - panelScreenRect.bottom;
        int modeFlags = dialogModeFlags();
        boolean collapsedMode = panelScreenRect.height() < freeBottomPixels && isNonModalGameplayDialogMode();

        if (mainWindow.uiLockPayload != null) {
            TokenEntry carriedToken = (TokenEntry) mainWindow.uiLockPayload;
            if (carriedToken.gridModeCode == GRID_MODE_HERO_INVENTORY
                    || carriedToken.gridModeCode == GRID_MODE_EQUIPMENT_SLOT) {
                CMousePointer.Cursor_Default.setToMousePointer();
                applyCarriedTokenToSelectionSlot(carriedToken.getType() - 1);
                return 1;
            }
            if (carriedToken.gridModeCode > 4
                    && carriedToken.gridModeCode < 9) {
                cancelShopDialogUiLockSelection();
                return 1;
            }
        }

        CRect inventoryRect = new CRect(
                panelScreenRect.left, panelScreenRect.bottom - 0x28,
                panelScreenRect.left + 0x1C, panelScreenRect.bottom
        );
        if (isNonModalGameplayDialogMode() && inventoryRect.contains(x, y)) {
            onMessage(MessageCodes.TOGGLE_SELECTION_PANEL, 0, 0);
            mapVisualObject.onMessage(MessageCodes.TOGGLE_SELECTION_PANEL, 0, 0);
        }

        CRect spellbookRect = new CRect(
                panelScreenRect.left, panelScreenRect.top,
                panelScreenRect.left + 0x1C, panelScreenRect.top + 0x24
        );
        if ((modeFlags & MODE_FLAGS_SPELLBOOK_MASK) != 0) {
            if ((modeFlags & MODE_FLAGS_BLOCK_SPELLBOOK_AND_INVENTORY) == 0
                    && spellbookRect.contains(x, y)) {
                onMessage(MessageCodes.TOGGLE_SPELL_PANEL, 0, 0);
                mapVisualObject.onMessage(MessageCodes.TOGGLE_SPELL_PANEL, 0, 0);
                if (SHOP_DIALOG.isSetIn(modeFlags)) {
                    CVisualObject root = mainWindow.getInputController().getChildById(1000);
                    root.onMessage(MessageCodes.TOGGLE_SPELL_PANEL, 0, 0);
                }
            }
        }

        CRect tabSwitchRect = new CRect(
                panelScreenRect.left + 0x80, panelScreenRect.top,
                panelScreenRect.right, panelScreenRect.top + 0x24
        );
        if ((modeFlags & MODE_FLAGS_HIDE_MODE_TOGGLE) == 0
                && !collapsedMode
                && tabSwitchRect.contains(x, y)) {
            onMessage(MessageCodes.TOGGLE_SELECTION_INFO_VIEW_MODE, 0, 0);
            mapVisualObject.areaEffectRefreshPending = 1;
        }

        CRect prevRect = new CRect(
                panelScreenRect.left + 0x01, panelScreenRect.top + 0xCD,
                panelScreenRect.left + 0x21, panelScreenRect.top + 0xED
        );
        CRect nextRect = new CRect(
                panelScreenRect.left + 0x77, panelScreenRect.top + 0xCD,
                panelScreenRect.left + 0x97, panelScreenRect.top + 0xED
        );
        if ((modeFlags & MODE_FLAGS_HERO_NAV_OR_INVENTORY_MASK) != 0) {
            CVisualObject inputController = mainWindow.getInputController();
            if (prevRect.contains(x, y)) {
                SoundManager.playSfx(SfxSounds.CLICK00);
                inputController.onMessage(MessageCodes.SELECT_PREVIOUS_HERO, 0, 0);
            }
            if (nextRect.contains(x, y)) {
                SoundManager.playSfx(SfxSounds.CLICK00);
                inputController.onMessage(MessageCodes.SELECT_NEXT_HERO, 0, 0);
            }
        }

        CRect escMenuRect = new CRect(
                panelScreenRect.left + 0x7E, panelScreenRect.top + 0xCE,
                panelScreenRect.left + 0x9E, panelScreenRect.top + 0xEE
        );
        if (GAMEPLAY.isSetIn(modeFlags) && escMenuRect.contains(x, y)) {
            SoundManager.playSfx(SfxSounds.CLICK00);
            mainWindow.postMessage(MessageCodes.ESC_MENU, 0, 0);
        }

        return 1;
    }

    /**
     * vtbl +0x5C: SelectionInfoPanelVisualObject::OnLButtonDblClk @004AEABB.
     * Full port for Java-backed selected unit equipment slots.
     */
    @Override
    public int onLButtonDblClk(int nFlags, int x, int y) {
        CRect panelScreenRect = new CRect();
        clientToScreen(panelScreenRect, cRect);
        int slotIndex = getPictureHitSlotIndex(panelScreenRect, x, y);
        if (slotIndex < 0) {
            return 1;
        }

        MapVisualObject mapVisualObject = resolveMapVisualObject();
        if (mapVisualObject.getSelectedCount() != 1) {
            return 0;
        }

        CUnit unit = (CUnit) mapVisualObject.getPrimarySelectedObject();
        if ((unit.unitFlags & UNIT_FLAG_HUMANOID) == 0) {
            return 0;
        }

        showSelectionSlotInventoryOverlay(slotIndex);
        return 1;
    }

    /**
     * vtbl +0x60: SelectionInfoPanelVisualObject::OnRButtonDown @004AEAA9.
     * Full port.
     */
    @Override
    public int onRButtonDown(int nFlags, int x, int y) {
        return 1;
    }

    /**
     * vtbl +0x64: SelectionInfoPanelVisualObject::OnRButtonUp @004AF0D9.
     * Full port.
     */
    @Override
    public int onRButtonUp(int nFlags, int x, int y) {
        if (isNonModalGameplayDialogMode()) {
            MapVisualObject mapVisualObject = resolveMapVisualObject();
            return mapVisualObject.onMessage(MessageCodes.REFRESH_LAYOUT, 0, 0);
        }
        return 1;
    }

    /**
     * vtbl +0x68: SelectionInfoPanelVisualObject::OnRButtonDblClk @004AEBB6.
     * Full port.
     */
    @Override
    public int onRButtonDblClk(int nFlags, int x, int y) {
        return 1;
    }

    /**
     * vtbl +0x6C: SelectionInfoPanelVisualObject::OnKeyDown @004AF357.
     * Full port.
     */
    @Override
    public int onKeyDown(int nChar) {
        if (nChar != 9) {
            return 0;
        }

        int modeFlags = dialogModeFlags();
        if ((modeFlags & MODE_FLAGS_HIDE_ESC_ICON) == 0 && (modeFlags & MODE_FLAGS_PORTRAIT_NAV_MASK) == 0) {
            onMessage(MessageCodes.TOGGLE_SELECTION_INFO_VIEW_MODE, 0, 0);
        }
        return 1;
    }

    /**
     * vtbl +0x78: SelectionInfoPanelVisualObject::BeginCarrySelectionSlotToken @004B012F.
     * Full port for Java-backed selected unit equipment slots.
     */
    public TokenEntry beginCarrySelectionSlotToken(int slotIndex) {
        CMainWindow mainWindow = Globals.mainWindow;
        MapVisualObject mapVisualObject = resolveMapVisualObject();
        if (mainWindow.uiLockPayload != null || !isSelectionEquipmentDragMode()) {
            return null;
        }

        return detachSelectionEquipmentToken(mapVisualObject, slotIndex);
    }

    /**
     * Native support extracted from SelectionInfoPanelVisualObject::OnMouseMove @004AF120.
     * Builds the carried equipment cursor path and delegates to CMainWindow::BeginShopGridDragVisual @0048AD7B.
     */
    private void beginSelectionSlotDragVisual(TokenEntry token) {
        CMainWindow mainWindow = Globals.mainWindow;
        mainWindow.beginShopGridDragVisual(
                token,
                mainWindow.getUiLockSourceIndex(),
                Resources.path(GRAPHICS, INVENTORY_DIRECTORY, token.getEquipmentPortraitResourceName() + A16_SUFFIX),
                mainWindow.getUiLockPackedModeCode()
        );
        mainWindow.cursor.setToMousePointer();
    }

    /**
     * vtbl +0x7C: SelectionInfoPanelVisualObject::ApplyCarriedTokenToSelectionSlot @004B028D.
     * Full port for Java-backed selected unit equipment slots.
     */
    public int applyCarriedTokenToSelectionSlot(int slotIndex) {
        CMainWindow mainWindow = Globals.mainWindow;
        MapVisualObject mapVisualObject = resolveMapVisualObject();
        boolean blocked = mapVisualObject.getSelectedCount() != 1 || mainWindow.uiLockPayload == null;
        CUnit unit = (CUnit) mapVisualObject.getPrimarySelectedObject();
        if (unit.cPlayer != mapVisualObject.currentPlayer) {
            return 0;
        }
        if (isBlockedEquipmentActionSlot(unit, slotIndex)) {
            blocked = true;
        }

        TokenEntry carriedToken = (TokenEntry) mainWindow.uiLockPayload;
        postCarriedQuestTokenDialogIfNeeded(carriedToken);

        boolean canEquip = canEquipCarriedTokenInSelectionSlot(unit, carriedToken);
        if (blocked || !canEquip) {
            clearSelectionSlotInventoryOverlay();
            return 0;
        }

        if (placeCarriedTokenIntoSelectionSlot(mapVisualObject, slotIndex)) {
            return 0;
        }

        if (executeCarriedSpellOrderFromSelectionSlot(mapVisualObject, slotIndex)) {
            return 0;
        }

        if (applyStackableCarriedTokenToSelectionSlot(mapVisualObject, slotIndex)) {
            return 0;
        }

        clearSelectionSlotInventoryOverlay();
        return 0;
    }

    /**
     * vtbl +0x80: SelectionInfoPanelVisualObject::GetSelectionSlotDescriptor @004B10A0.
     * Full port.
     */
    public int getSelectionSlotDescriptor() {
        return 1;
    }

    /**
     * Native panel-chrome branch in SelectionInfoPanelVisualObject::Update @004AF3BF.
     * Full port.
     */
    private void drawPanelBackground(CRect panelScreenRect) {
        if (selectionInfoViewMode0x70 == 0) {
            GUI.textBackR.draw(panelScreenRect.left, panelScreenRect.top, 0, null, false);
        } else {
            GUI.humanBackR.draw(panelScreenRect.left, panelScreenRect.top, 0, null, false);
        }
    }

    /**
     * Native spellbook toggle icon branch in SelectionInfoPanelVisualObject::Update @004AF3BF.
     * Full port.
     */
    private void drawSpellbookButton(CRect panelScreenRect, int modeFlags, MapVisualObject mapVisualObject) {
        if ((modeFlags & MODE_FLAGS_SPELLBOOK_MASK) == 0 || (modeFlags & MODE_FLAGS_BLOCK_SPELLBOOK_AND_INVENTORY) != 0) {
            return;
        }

        boolean spellbookOpened = mapVisualObject.hasSpellPanelChild();
        if (spellbookOpened) {
            GUI.bookOpened.drawRectMasked(panelScreenRect.left, panelScreenRect.top, 0, 0, 0x1C, 0x26);
        } else {
            GUI.bookClosed.drawRectMasked(panelScreenRect.left, panelScreenRect.top + 4, 0, 0, 0x1C, 0x25);
        }
    }

    /**
     * Native inventory/arrows/diskette branch in SelectionInfoPanelVisualObject::Update @004AF3BF.
     * Full port.
     */
    private void drawInventoryOrNavigationButtons(CRect panelScreenRect, int modeFlags, MapVisualObject mapVisualObject, int sessionMode) {
        if ((modeFlags & MODE_FLAGS_HERO_NAV_OR_INVENTORY_MASK) != 0) {
            if ((modeFlags & MODE_FLAGS_PORTRAIT_NAV_MASK) == 0 || sessionMode != 2) {
                GUI.arrow1.drawRectMasked(panelScreenRect.left + 1, panelScreenRect.top + 0xCD, 0, 0, 0x20, 0x20);
                GUI.arrow2.drawRectMasked(panelScreenRect.left + 0x77, panelScreenRect.top + 0xCD, 0, 0, 0x20, 0x20);
            }
            return;
        }
        if ((modeFlags & MODE_FLAGS_HIDE_ESC_ICON) != 0) {
            return;
        }

        boolean inventoryOpened = mapVisualObject.hasSelectionPanelChild();
        if (inventoryOpened) {
            GUI.backPackOpen.drawRectMasked(panelScreenRect.left, panelScreenRect.top + 0xD0, 0, 0, 0x20, 0x1F);
        } else {
            GUI.backPackClosed.drawRectMasked(panelScreenRect.left + 1, panelScreenRect.top + 0xC9, 0, 0, 0x1C, 0x1E);
        }
        GUI.diskette.drawRectMasked(panelScreenRect.left + 0x7E, panelScreenRect.top + 0xCE, 0, 0, 0x20, 0x20);
    }

    /**
     * Native mode-toggle icon branch in SelectionInfoPanelVisualObject::Update @004AF3BF.
     * Full port.
     */
    private void drawModeToggleButton(CRect panelScreenRect, int modeFlags, boolean collapsedMode) {
        if ((modeFlags & MODE_FLAGS_HIDE_MODE_TOGGLE) != 0 || collapsedMode) {
            return;
        }

        if (selectionInfoViewMode0x70 == 0) {
            GUI.textMode.drawRectMasked(panelScreenRect.left + 0x80, panelScreenRect.top + 4, 0, 0, 0x1C, 0x20);
        } else {
            GUI.humanMode.drawRectMasked(panelScreenRect.left + 0x80, panelScreenRect.top + 4, 0, 0, 0x1C, 0x20);
        }
    }

    /**
     * Native selected-object info block in SelectionInfoPanelVisualObject::Update @004AF3BF.
     * Full port for Java-backed selected and hovered objects.
     */
    private void renderSelectedObjectInfo(CRect panelScreenRect, CMainWindow mainWindow, MapVisualObject mapVisualObject) {
        CGameObject hovered = resolveHoveredUpdateInfoObject(mainWindow, mapVisualObject);
        CGameObject primary = hovered != null ? hovered : mapVisualObject.getPrimarySelectedObject();
        int selectedCount = mapVisualObject.getSelectedCount();
        if (hovered == null && (selectedCount != 1 || primary == null)) {
            renderSelectionCountText(panelScreenRect, selectedCount);
            return;
        }

        if (selectionInfoViewMode0x70 == 0) {
            renderStatsSelection(primary, panelScreenRect, mainWindow);
            return;
        }
        renderPictureSelection(primary, panelScreenRect, mapVisualObject);
    }

    /**
     * Native support extracted from SelectionInfoPanelVisualObject::Update @004AF3BF hovered-object preference branch.
     */
    private CGameObject resolveHoveredUpdateInfoObject(CMainWindow mainWindow, MapVisualObject mapVisualObject) {
        if (mainWindow.uiLockPayload == null && isNonModalGameplayDialogMode()) {
            return mapVisualObject.resolveHoveredObjectForSelectionInfoPanelUpdate();
        }
        return null;
    }

    /**
     * Native selected-count text branches in SelectionInfoPanelVisualObject::Update @004AF3BF.
     * Full port.
     */
    private static void renderSelectionCountText(CRect panelScreenRect, int selectedCount) {
        CBitmapFont font = Globals.fonts.font2;
        int centerX = panelScreenRect.left + 0x48;
        if (selectedCount == 0) {
            font.drawTextShadowed(centerX, panelScreenRect.top + 0x36, get(MAIN_NO_UNITS_47), TextAlign.CENTER.mask, Palettes.yellowish, 1);
            font.drawTextShadowed(centerX, panelScreenRect.top + 0x42, get(MAIN_SELECTED_48), TextAlign.CENTER.mask, Palettes.yellowish, 1);
            return;
        }

        font.drawTextShadowed(centerX, panelScreenRect.top + 0x36, get(MAIN_UNITS_49), TextAlign.CENTER.mask, Palettes.yellowish, 1);
        font.drawTextShadowed(centerX, panelScreenRect.top + 0x42, get(MAIN_SELECTED_50), TextAlign.CENTER.mask, Palettes.yellowish, 1);
        font.drawTextShadowed(centerX, panelScreenRect.top + 0x4E, Integer.toString(selectedCount), TextAlign.CENTER.mask, Palettes.yellowish, 1);
    }

    /**
     * Native stats-tab object summary path (`CUnit::RenderFullStatsInfo @0046AA1D` and structure text branches) from SelectionInfoPanelVisualObject::Update @004AF3BF.
     * Full port for Java-backed selected objects.
     */
    private void renderStatsSelection(
            CGameObject primarySelected,
            CRect panelScreenRect,
            @SuppressWarnings("unused") CMainWindow mainWindow
    ) {
        if (primarySelected instanceof CUnit unit) {
            unit.renderFullStatsInfo(panelScreenRect);
            return;
        }

        if (primarySelected instanceof CStructure structure) {
            renderStructureStatsSelection(structure, panelScreenRect);
        }
    }

    /**
     * Native structure stats branch in SelectionInfoPanelVisualObject::Update @004AF3BF.
     * Full port.
     */
    private static void renderStructureStatsSelection(CStructure structure, CRect panelScreenRect) {
        CBitmapFont font = Globals.fonts.font2;
        int centerX = panelScreenRect.right - 0x58;
        font.drawTextShadowed(
                centerX,
                panelScreenRect.top + 0x1C,
                get(TextTableId.BUILDING, BuildingText.byIndex(structure.type - 1)),
                TextAlign.CENTER.mask,
                Palettes.yellowish,
                1
        );
        font.drawTextShadowed(centerX, panelScreenRect.top + 0x2C, get(MAIN_HEALTH_19), TextAlign.CENTER.mask, Palettes.yellowish, 1);
        font.drawTextShadowed(
                centerX,
                panelScreenRect.top + 0x36,
                Short.toUnsignedInt(structure.HP) + "/" + Short.toUnsignedInt(structure.MaxHP),
                TextAlign.CENTER.mask,
                Palettes.greenLeaningGray,
                1
        );
    }

    /**
     * Native picture-tab object portrait path from SelectionInfoPanelVisualObject::Update @004AF3BF.
     * Full port for Java-backed selected objects.
     */
    private void renderPictureSelection(
            CGameObject primarySelected,
            CRect panelScreenRect,
            MapVisualObject mapVisualObject
    ) {
        if (primarySelected instanceof CUnit unit && (unit.unitFlags & UNIT_FLAG_INFO_PORTRAIT_DYNAMIC_MASK) != 0) {
            ensureDynamicInfoPictureLoaded(unit);
        } else {
            String pictureName = resolveInfoPictureName(primarySelected);
            if (pictureName.isEmpty()) {
                resetInfoPictureBitmap();
            } else {
                ensureInfoPictureLoaded(pictureName);
            }
        }
        pictureBitmap0x74.drawRectMasked(panelScreenRect.left, panelScreenRect.top + 2, 0, 0, PICTURE_WIDTH, PICTURE_HEIGHT);
        renderPictureQuestHoverLabels(primarySelected, panelScreenRect, mapVisualObject);
    }

    /**
     * Native quest-hover overlay branches in SelectionInfoPanelVisualObject::Update @004AF3BF.
     */
    private static void renderPictureQuestHoverLabels(
            CGameObject primarySelected,
            CRect panelScreenRect,
            MapVisualObject mapVisualObject
    ) {
        if (primarySelected instanceof CUnit && mapVisualObject.hasSelectionInfoUnitQuestHover()) {
            drawPictureQuestHoverLabelPair(panelScreenRect, 0x36, MAIN_GOAL_345, MAIN_QUESTS_346);
            return;
        }

        if (primarySelected instanceof CStructure) {
            int labelTop = 0x36;
            if (mapVisualObject.hasSelectionInfoRegionQuestHover()) {
                drawPictureQuestHoverLabelPair(panelScreenRect, labelTop, MAIN_REGION_347, MAIN_QUESTS_348);
                labelTop += 0x20;
            }
            if (mapVisualObject.hasSelectionInfoCustomerQuestHover()) {
                drawPictureQuestHoverLabelPair(panelScreenRect, labelTop, MAIN_CUSTOMER_349, MAIN_QUESTS_350);
            }
        }
    }

    /**
     * Native support extracted from repeated CBitmapFont::DrawTextShadowed quest-hover label pairs in
     * SelectionInfoPanelVisualObject::Update @004AF3BF.
     */
    private static void drawPictureQuestHoverLabelPair(
            CRect panelScreenRect,
            int topOffset,
            int titleTextIndex,
            int questsTextIndex
    ) {
        CBitmapFont font = Globals.fonts.font2;
        int centerX = panelScreenRect.left + 0x48;
        font.drawTextShadowed(centerX, panelScreenRect.top + topOffset, get(titleTextIndex), TextAlign.CENTER.mask, Palettes.yellowish, 1);
        font.drawTextShadowed(centerX, panelScreenRect.top + topOffset + 0x0C, get(questsTextIndex), TextAlign.CENTER.mask, Palettes.yellowish, 1);
    }

    /**
     * Native info-picture name resolution branch inside SelectionInfoPanelVisualObject::Update @004AF3BF.
     * Full port for Java-backed selected objects.
     */
    private static String resolveInfoPictureName(CGameObject primarySelected) {
        if (primarySelected instanceof CUnit unit) {
            CUnitInfo info = Objects.requireNonNull(
                    UnitTypes.getUnitInfo(unit.type),
                    "Missing CUnitInfo for type " + unit.type
            );
            String infoPicture = Objects.requireNonNull(
                    info.m_InfoPicture,
                    "Missing CUnitInfo picture for type " + unit.type
            );
            return unit.field8_0x28 > 1 ? infoPicture + unit.field8_0x28 : infoPicture;
        }

        if (primarySelected instanceof CStructure structure) {
            StructureDef structureDef = Objects.requireNonNull(
                    Structures.getStructureDef(structure.type),
                    "Missing StructureDef for type " + structure.type
            );
            return Objects.requireNonNull(
                    structureDef.picture,
                    "Missing StructureDef picture for type " + structure.type
            );
        }
        return "";
    }

    /**
     * Native dynamic equipment portrait branch from SelectionInfoPanelVisualObject::Update @004AF3BF.
     * Full Java-backed port. Java renders directly into the in-memory panel bitmaps instead of the native temp-file cache.
     */
    private void ensureDynamicInfoPictureLoaded(CUnit unit) {
        String pictureName = resolveDynamicInfoPictureName(unit);
        if (pictureName.equals(cachedInfoPictureName0x7c)
                && (unit.unitFlags & UNIT_FLAG_EQUIPMENT_PORTRAIT_DIRTY) == 0
                && (!hasEquipmentTokens(unit) || hasPictureHitMapSlots())) {
            return;
        }

        cachedInfoPictureName0x7c = pictureName;
        unit.renderEquipmentPortrait(pictureName, pictureBitmap0x74, pictureHitMap0x78);
    }

    /**
     * Native dynamic temp-cache name branch from SelectionInfoPanelVisualObject::Update @004AF3BF.
     */
    private static String resolveDynamicInfoPictureName(CUnit unit) {
        return "allods-2-" + (unit.m_id & 0xFFFF) + ".$$$";
    }

    /**
     * Native static info-window bitmap load branch from SelectionInfoPanelVisualObject::Update @004AF3BF.
     * Fully ported at the modeled bitmap-owner boundary.
     */
    private void ensureInfoPictureLoaded(String pictureName) {
        if (pictureName.equals(cachedInfoPictureName0x7c)) {
            return;
        }

        cachedInfoPictureName0x7c = pictureName;
        pictureBitmap0x74.loadBmp24Pixels(Resources.path(GRAPHICS, INFOWINDOW_DIRECTORY, pictureName + BMP_SUFFIX), null);
        clearPictureHitMap();
    }

    /**
     * Native support extracted from SelectionInfoPanelVisualObject::Update @004AF3BF static-picture hit-map reset.
     */
    private void clearPictureHitMap() {
        Arrays.fill(pictureHitMap0x78.frames.getFirst().pixels(), 0);
    }

    /**
     * Native support extracted from SelectionInfoPanelVisualObject::Update @004AF3BF empty-picture handling.
     */
    private void resetInfoPictureBitmap() {
        if (BLANK_INFO_PICTURE_CACHE_KEY.equals(cachedInfoPictureName0x7c)) {
            return;
        }
        cachedInfoPictureName0x7c = BLANK_INFO_PICTURE_CACHE_KEY;
        pictureBitmap0x74 = new CBmp64k(PICTURE_WIDTH, PICTURE_HEIGHT);
        clearPictureHitMap();
    }

    /**
     * Native tail of SelectionInfoPanelVisualObject::GetText @004AE232 (entity-specific tooltip resolution).
     * Full port for Java-backed selected-object tooltip data.
     */
    private String resolveSelectedObjectTooltip(
            MapVisualObject mapVisualObject,
            int modeFlags,
            CRect panelScreenRect,
            int mouseX,
            int mouseY
    ) {
        if (mapVisualObject == null || mapVisualObject.getSelectedCount() != 1 || mapVisualObject.getPrimarySelectedObject() == null) {
            return null;
        }
        if (MODAL_DIALOG.isSetIn(modeFlags)) {
            return null;
        }

        if (selectionInfoViewMode0x70 != 0) {
            return resolvePictureModeTooltip(mapVisualObject, panelScreenRect, mouseX, mouseY);
        }
        return resolveStatsModeTooltip(mapVisualObject, panelScreenRect, mouseX, mouseY);
    }

    /**
     * Native call path: CUnit::GetFullStatsTooltipText @0046B9F0 from SelectionInfoPanelVisualObject::GetText.
     * Full port for Java-backed selected unit data.
     */
    private static String resolveStatsModeTooltip(
            MapVisualObject mapVisualObject,
            CRect panelScreenRect,
            int mouseX,
            int mouseY
    ) {
        if (selectedUnit(mapVisualObject) instanceof CUnit unit) {
            return unit.getFullStatsTooltipText(mouseX - panelScreenRect.left, mouseY - panelScreenRect.top - 2);
        }
        return null;
    }

    /**
     * Native support extracted from SelectionInfoPanelVisualObject::GetText @004AE232 picture-mode tooltip path,
     * including TokenEntry::resolveTooltipText @0043901F and the CBmp256 pixel map lookup.
     * Full port for Java-backed selected unit data.
     */
    private String resolvePictureModeTooltip(
            MapVisualObject mapVisualObject,
            CRect panelScreenRect,
            int mouseX,
            int mouseY
    ) {
        CUnit unit = selectedUnit(mapVisualObject);
        if (unit == null || !canShowPictureTooltipForUnit(unit, mapVisualObject)) {
            return null;
        }

        if ((unit.unitFlags & UNIT_FLAG_INFO_PORTRAIT_DYNAMIC_MASK) != 0) {
            ensureDynamicInfoPictureLoaded(unit);
        }

        int slotIndex = getPictureHitSlotIndex(panelScreenRect, mouseX, mouseY);
        if (slotIndex < 0) {
            return null;
        }
        TokenEntry token = unit.equipmentTokenEntries[slotIndex];
        return token == null ? null : resolveEquipmentTokenTooltip(token, unit);
    }

    /**
     * Native call path: TokenEntry::resolveTooltipText @0043901F from SelectionInfoPanelVisualObject::GetText @004AE232.
     * Partial port.
     */
    private static String resolveEquipmentTokenTooltip(
            TokenEntry token,
            @SuppressWarnings("unused") CUnit unit
    ) {
        return token.resolveTooltipText();
    }

    /**
     * Native owner: CMainWindow::field115_0x3F4 checks in SelectionInfoPanelVisualObject::GetText.
     * not ported.
     */
    private static int getUiLockFlag3f4(CMainWindow mainWindow) {
        return mainWindow.getUiLockFlag3f4();
    }

    /**
     * Native: SelectionInfoPanelVisualObject::UpdateCursorForPanelHover @004AFF34.
     * Full port.
     */
    private void updateCursorForPanelHover(CMainWindow mainWindow) {
        ua.millfreedom.rom2.model.CGameBitmap sourceBitmap = Globals.mousePointer.getSourceBitmap();
        CRect panelScreenRect = new CRect();
        clientToScreen(panelScreenRect, cRect);
        ua.millfreedom.rom2.model.CCursor nextCursor = null;
        int mouseX = Globals.mousePointer.getX();
        int mouseY = Globals.mousePointer.getY();
        if (!panelScreenRect.contains(mouseX, mouseY)) {
            return;
        }

        int modeFlags = dialogModeFlags();
        if (SHOP_DIALOG.isUnsetIn(modeFlags)) {
            if (mouseX >= Globals.screenRect.right - 2 && isNonModalGameplayDialogMode()) {
                if (mouseY == 0) {
                    nextCursor = CMousePointer.Cursor_ArrowNE;
                } else if (mouseY < Globals.screenRect.bottom - 2) {
                    nextCursor = CMousePointer.Cursor_ArrowE;
                } else {
                    nextCursor = CMousePointer.Cursor_ArrowSE;
                }
            } else if (mouseY >= Globals.screenRect.bottom - 2 && isNonModalGameplayDialogMode()) {
                nextCursor = CMousePointer.Cursor_ArrowS;
            } else {
                nextCursor = CMousePointer.Cursor_Default;
            }
            if (mainWindow.uiLockPayload != null) {
                nextCursor = mainWindow.cursor;
            }
        } else if (mainWindow.uiLockPayload != null) {
            ShopDialogVisualObject shopDialog = (ShopDialogVisualObject) mainWindow.getInputController().getChildById(1000);
            TokenEntry token = (TokenEntry) mainWindow.uiLockPayload;
            if (shopDialog.uiLockPlacementAllowedFlag == 0
                    && token.gridModeCode != GRID_MODE_EQUIPMENT_SLOT
                    && token.gridModeCode != GRID_MODE_HERO_INVENTORY) {
                nextCursor = CMousePointer.Cursor_CantPut;
            } else {
                nextCursor = mainWindow.cursor;
            }
        }

        if (nextCursor != null && sourceBitmap != nextCursor.getBitmap()) {
            nextCursor.setToMousePointer();
        }
    }

    /**
     * Native local collapsed-panel predicate in SelectionInfoPanelVisualObject::GetText @004AE232.
     */
    private boolean isSimpleGetTextCollapsedMode(int modeFlags, CRect panelScreenRect) {
        int freeBottomPixels = screenBottomY() - panelScreenRect.bottom;
        return panelScreenRect.height() < freeBottomPixels && isGameplayDialogMode(modeFlags);
    }

    /**
     * Native helper used in SelectionInfoPanelVisualObject::OnMessage/Update for compact-right-panel state.
     * not ported.
     */
    private boolean isCollapsedMode(CMainWindow mainWindow) {
        CRect panelScreenRect = new CRect();
        clientToScreen(panelScreenRect, cRect);
        return isCollapsedMode(dialogModeFlags(), panelScreenRect);
    }

    /**
     * Native helper used in SelectionInfoPanelVisualObject::OnMessage/Update for compact-right-panel state.
     * not ported.
     */
    private boolean isCollapsedMode(int modeFlags, CRect panelScreenRect) {
        int freeBottomPixels = screenBottomY() - panelScreenRect.bottom;
        return panelScreenRect.height() < freeBottomPixels && isGameplayDialogMode(modeFlags);
    }

    /**
     * Native: SelectionInfoPanelVisualObject::GetSelectionInfoViewMode @004B011E.
     * Full port.
     */
    public int getSelectionInfoViewMode() {
        return selectionInfoViewMode0x70;
    }

    /**
     * Native owner: selection-info panel parent-context pointer at SelectionInfoPanelVisualObject +0x5C.
     * not ported.
     */
    private MapVisualObject resolveMapVisualObject() {
        return mapContext0x5c;
    }

    /**
     * Native support extracted from SelectionInfoPanelVisualObject::OnMouseMove @004AF120 and OnLButtonDblClk @004AEABB picture-slot gates.
     * Partial port.
     */
    private boolean isPictureModeSelectionInteractionActive(MapVisualObject mapVisualObject) {
        return selectionInfoViewMode0x70 != 0
                && mapVisualObject != null
                && mapVisualObject.getSelectedCount() == 1
                && mapVisualObject.getPrimarySelectedObject() instanceof CUnit unit
                && (unit.unitFlags & UNIT_FLAG_HUMANOID) != 0;
    }

    /**
     * Native support extracted from SelectionInfoPanelVisualObject::GetText @004AE232,
     * OnMouseMove @004AF120, and OnLButtonDblClk @004AEABB direct CBmp256 hit-map reads.
     * Java guards off-buffer screen coordinates before indexing the fixed-size hit-map array.
     */
    private int getPictureHitSlotIndex(CRect panelScreenRect, int x, int y) {
        if (pictureHitMap0x78 == null || pictureHitMap0x78.frames == null || pictureHitMap0x78.frames.isEmpty()) {
            return -1;
        }

        int localX = x - panelScreenRect.left;
        int localY = y - panelScreenRect.top - 2;
        if (localX < 0 || localY < 0) {
            return -1;
        }

        var frame = pictureHitMap0x78.frames.get(0);
        if (localX >= frame.width() || localY >= frame.height()) {
            return -1;
        }

        int pixel = frame.pixels()[localY * frame.width() + localX];
        int slotIndex = pixel - 1;
        return slotIndex >= 0 && slotIndex < EQUIPMENT_SLOT_COUNT ? slotIndex : -1;
    }

    /**
     * Native support extracted from SelectionInfoPanelVisualObject::Update @004AF3BF dynamic portrait cache reuse.
     * Native temp-cache loads repopulate the paired hit-map; Java rerenders if an equipped unit has no hit-map entries.
     */
    private boolean hasPictureHitMapSlots() {
        if (pictureHitMap0x78 == null || pictureHitMap0x78.frames == null || pictureHitMap0x78.frames.isEmpty()) {
            return false;
        }
        for (int pixel : pictureHitMap0x78.frames.getFirst().pixels()) {
            if (pixel != 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Native support extracted from SelectionInfoPanelVisualObject::Update @004AF3BF equipment-token driven dynamic
     * portrait handling.
     */
    private static boolean hasEquipmentTokens(CUnit unit) {
        for (TokenEntry token : unit.equipmentTokenEntries) {
            if (token != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Native owner: selected-object inventory detach path inside SelectionInfoPanelVisualObject::BeginCarrySelectionSlotToken @004B012F.
     */
    private TokenEntry detachSelectionEquipmentToken(
            MapVisualObject mapVisualObject,
            int slotIndex
    ) {
        if (mapVisualObject.getSelectedCount() != 1) {
            return null;
        }
        CUnit unit = (CUnit) mapVisualObject.getPrimarySelectedObject();
        CMainWindow mainWindow = Globals.mainWindow;
        if (unit.cPlayer != mapVisualObject.currentPlayer
                || isBlockedEquipmentActionSlot(unit, slotIndex)) {
            return null;
        }

        unit.unitFlags |= UNIT_FLAG_EQUIPMENT_PORTRAIT_DIRTY;
        onMessage(MessageCodes.NOTIFY_MAP_CONTEXT_CHANGED, 0, 0);
        TokenEntry token = unit.equipmentTokenEntries[slotIndex];
        mainWindow.setUiLockPayload(token);
        unit.equipmentTokenEntries[slotIndex] = null;
        mainWindow.setUiLockSourceIndex(slotIndex);
        mainWindow.setUiLockPackedModeCode(getSelectionSlotDescriptor());
        unit.refreshUnitSpritesAfterRuntimeCopy();
        notifyInputControllerSelectionChanged(mainWindow);
        return token;
    }

    /**
     * Native owner: carried-token validation path inside SelectionInfoPanelVisualObject::ApplyCarriedTokenToSelectionSlot @004B028D.
     */
    private boolean canEquipCarriedTokenInSelectionSlot(
            CUnit unit,
            TokenEntry token
    ) {
        boolean blocked = false;
        if (!unit.isSelectableForShopEntry(token)) {
            blocked = true;
        }
        if (isBlockedQuestToken(token)) {
            blocked = true;
        }
        if (!canUseCarriedTokenSpell(unit, token)) {
            blocked = true;
        }
        return !blocked;
    }

    /**
     * Native owner: carried-token placement path inside SelectionInfoPanelVisualObject::ApplyCarriedTokenToSelectionSlot @004B028D.
     */
    private boolean placeCarriedTokenIntoSelectionSlot(
            MapVisualObject mapVisualObject,
            int slotIndex
    ) {
        CUnit unit = (CUnit) mapVisualObject.getPrimarySelectedObject();
        CMainWindow mainWindow = Globals.mainWindow;
        TokenEntry carriedToken = (TokenEntry) mainWindow.getUiLockPayload();
        if ((carriedToken.wireFlags & TokenEntry.FLAG_STACKABLE) != 0) {
            return false;
        }

        TokenEntry replacedToken = unit.equipmentTokenEntries[slotIndex];
        if (replacedToken != null) {
            moveEquipmentTokenToInventory(unit, replacedToken, mainWindow.getUiLockSourceIndex());
        }
        if (slotIndex == 0 && isTwoHandedWeapon(carriedToken) && unit.equipmentTokenEntries[1] != null) {
            moveEquipmentTokenToInventory(unit, unit.equipmentTokenEntries[1], mainWindow.getUiLockSourceIndex());
            unit.equipmentTokenEntries[1] = null;
        }
        if (slotIndex == 1 && unit.equipmentTokenEntries[0] != null && isTwoHandedWeapon(unit.equipmentTokenEntries[0])) {
            moveEquipmentTokenToInventory(unit, unit.equipmentTokenEntries[0], mainWindow.getUiLockSourceIndex());
            unit.equipmentTokenEntries[0] = null;
        }

        unit.unitFlags |= UNIT_FLAG_EQUIPMENT_PORTRAIT_DIRTY;
        onMessage(MessageCodes.NOTIFY_MAP_CONTEXT_CHANGED, 0, 0);
        unit.equipmentTokenEntries[slotIndex] = carriedToken;
        commitSelectionSlotEquipmentChange(
                mapVisualObject,
                mainWindow.getUiLockPackedModeCode(),
                mainWindow.getUiLockSourceIndex(),
                getSelectionSlotDescriptor(),
                slotIndex + 1,
                carriedToken.quantity
        );
        mainWindow.clearUiLockState();
        unit.refreshUnitSpritesAfterRuntimeCopy();
        notifyInputControllerSelectionChanged(mainWindow);
        return true;
    }

    /**
     * Native owner: order-execution tail inside SelectionInfoPanelVisualObject::ApplyCarriedTokenToSelectionSlot @004B028D.
     */
    private boolean executeCarriedSpellOrderFromSelectionSlot(
            MapVisualObject mapVisualObject,
            @SuppressWarnings("unused") int slotIndex
    ) {
        CMainWindow mainWindow = Globals.mainWindow;
        TokenEntry token = (TokenEntry) mainWindow.getUiLockPayload();
        if (!isSelectionSpellOrderToken(token)) {
            return false;
        }

        int selectedEntryIndex = mainWindow.pHeroInventoryControlVisualObject.completeUiDrag(mainWindow.getUiLockSourceIndex());
        mainWindow.pSpellPanelVisualObject.selectDraggedSpellEntry(selectedEntryIndex);
        mapVisualObject.onMessage(MessageCodes.EXECUTE_ORDER, ORDER_TYPE_CAST_SLOT_B, 0);
        return true;
    }

    /**
     * Native owner: stackable non-spell-order branch inside SelectionInfoPanelVisualObject::ApplyCarriedTokenToSelectionSlot @004B028D.
     * Full branch port.
     */
    private boolean applyStackableCarriedTokenToSelectionSlot(
            MapVisualObject mapVisualObject,
            int slotIndex
    ) {
        CMainWindow mainWindow = Globals.mainWindow;
        TokenEntry carriedToken = (TokenEntry) mainWindow.getUiLockPayload();
        if ((carriedToken.wireFlags & TokenEntry.FLAG_STACKABLE) == 0) {
            return false;
        }

        boolean hadRemainder = false;
        if (carriedToken.quantity > 1) {
            hadRemainder = true;
            carriedToken.quantity = 1;
        }

        commitSelectionSlotEquipmentChange(
                mapVisualObject,
                mainWindow.getUiLockPackedModeCode(),
                mainWindow.getUiLockSourceIndex(),
                getSelectionSlotDescriptor(),
                slotIndex + 1,
                carriedToken.quantity
        );
        if (hadRemainder) {
            mainWindow.pHeroInventoryControlVisualObject.completeUiDrag(mainWindow.getUiLockSourceIndex());
        } else {
            mainWindow.clearUiLockState();
        }
        return true;
    }

    /**
     * Native owner: GridOverlayVisualObject selection-slot highlight chain inside SelectionInfoPanelVisualObject::OnLButtonDblClk @004AEABB.
     * Partial port.
     */
    private boolean showSelectionSlotInventoryOverlay(int slotIndex) {
        TokenEntry token = beginCarrySelectionSlotToken(slotIndex);
        if (token == null) {
            return false;
        }

        CMainWindow mainWindow = Globals.mainWindow;
        HeroInventoryControlVisualObject heroInventory = mainWindow.pHeroInventoryControlVisualObject;
        heroInventory.completeUiDrag(heroInventory.getVisibleStart());
        return true;
    }

    /**
     * Native owner: GridOverlayVisualObject reset chain inside SelectionInfoPanelVisualObject::ApplyCarriedTokenToSelectionSlot @004B028D.
     */
    private void clearSelectionSlotInventoryOverlay() {
        CMainWindow mainWindow = Globals.mainWindow;
        HeroInventoryControlVisualObject heroInventory = mainWindow.pHeroInventoryControlVisualObject;
        heroInventory.completeUiDrag(mainWindow.getUiLockSourceIndex());
    }

    /**
     * Native support extracted from child-id-1000 dragged-object branch inside SelectionInfoPanelVisualObject::OnLButtonUp @004AEBC8.
     */
    private void cancelShopDialogUiLockSelection() {
        ShopDialogVisualObject shopDialog = (ShopDialogVisualObject) Globals.mainWindow.getInputController().getChildById(1000);
        shopDialog.cancelUiLockSelection();
    }

    /**
     * Native support extracted from selected-count and primary-selection reads in SelectionInfoPanelVisualObject::Update @004AF3BF and GetText @004AE232.
     */
    private static CUnit selectedUnit(MapVisualObject mapVisualObject) {
        if (mapVisualObject == null || mapVisualObject.getSelectedCount() != 1) {
            return null;
        }
        return mapVisualObject.getPrimarySelectedObject() instanceof CUnit unit ? unit : null;
    }

    /**
     * Native support extracted from CPlayer visibility branch in SelectionInfoPanelVisualObject::GetText @004AE232.
     */
    private static boolean canShowPictureTooltipForUnit(CUnit unit, MapVisualObject mapVisualObject) {
        int ownerPlayerId = unit.cPlayer.playerId;
        return ownerPlayerId == 0
                || mapVisualObject == null
                || mapVisualObject.currentPlayer == null
                || mapVisualObject.currentPlayer.isMapVisible(ownerPlayerId);
    }

    /**
     * Native support extracted from owner pointer equality checks in SelectionInfoPanelVisualObject::BeginCarrySelectionSlotToken @004B012F.
     */
    private static boolean isSelectedUnitOwnedByCurrentPlayer(CUnit unit, MapVisualObject mapVisualObject) {
        if (unit == null || mapVisualObject == null || mapVisualObject.currentPlayer == null) {
            return false;
        }
        if (unit.cPlayer == mapVisualObject.currentPlayer) {
            return true;
        }
        if (unit.cPlayer instanceof CPlayer player) {
            return player.playerId == mapVisualObject.currentPlayer.playerId;
        }
        return unit.cPlayer.playerId == mapVisualObject.currentPlayer.playerId;
    }

    /**
     * Native support extracted from dialogsMask gates in SelectionInfoPanelVisualObject::BeginCarrySelectionSlotToken @004B012F.
     */
    private boolean isSelectionEquipmentDragMode() {
        int modeFlags = dialogModeFlags();
        return SHOP_DIALOG.isSetIn(modeFlags) || isNonModalGameplayDialogMode();
    }

    /**
     * Native support extracted from lastAction gates in SelectionInfoPanelVisualObject::BeginCarrySelectionSlotToken @004B012F and ApplyCarriedTokenToSelectionSlot @004B028D.
     */
    private static boolean isBlockedEquipmentActionSlot(CUnit unit, int slotIndex) {
        return slotIndex < 2
                && (unit.lastAction == ACTION_ATTACK || unit.lastAction == ACTION_CAST || unit.lastAction == ACTION_USE);
    }

    /**
     * Native support extracted from the SHOW_FAME_HALL_DOCUMENT_DIALOG post branch in SelectionInfoPanelVisualObject::ApplyCarriedTokenToSelectionSlot @004B028D.
     */
    private static void postCarriedQuestTokenDialogIfNeeded(TokenEntry token) {
        if ((token.wireFlags & (TokenEntry.FLAG_EQUIPPABLE_BY_NON_MAGIC_UNIT | TokenEntry.FLAG_EQUIPPABLE_BY_MAGIC_UNIT)) == 0
                && (token.packedTokenHash & 0xFFFF) == QUEST_DIALOG_TOKEN_HASH) {
            Globals.mainWindow.postMessage(MessageCodes.SHOW_FAME_HALL_DOCUMENT_DIALOG, 0, 0);
        }
    }

    /**
     * Native support extracted from MagicItem::IsShopCatalogEntryBlockedForDoubleClick @00439D63 used by
     * SelectionInfoPanelVisualObject::ApplyCarriedTokenToSelectionSlot @004B028D.
     * Full port for TokenEntry-backed carried entries.
     */
    private static boolean isBlockedQuestToken(TokenEntry token) {
        return MagicItem.isShopCatalogEntryBlockedForDoubleClick(token);
    }

    /**
     * Native support extracted from GetAttr @00438E8A and spell-duplicate gates in ApplyCarriedTokenToSelectionSlot @004B028D.
     */
    private static boolean canUseCarriedTokenSpell(CUnit unit, TokenEntry token) {
        int spellId = token.getAttribute(TOKEN_ATTR_SPELL_ID);
        if (spellId == 0) {
            return true;
        }
        return (unit.unitFlags & UNIT_FLAG_MAGIC_CLASS) != 0
                && (unit.spellbookMask & (1 << (spellId & 0x1F))) == 0;
    }

    /**
     * Native support extracted from GridOverlayVisualObject::MergeOrInsertEntryAt calls in ApplyCarriedTokenToSelectionSlot @004B028D.
     */
    private static void moveEquipmentTokenToInventory(CUnit unit, TokenEntry token, int insertIndex) {
        token.gridModeCode = GRID_MODE_HERO_INVENTORY;
        HeroInventoryControlVisualObject heroInventory = Globals.mainWindow.pHeroInventoryControlVisualObject;
        heroInventory.mergeOrInsertEntryAt(token, insertIndex);
        heroInventory.bindGridSourceFromContext(unit);
    }

    /**
     * Native support extracted from Token::IsTwoHandedHeroPictureToken @00438DA7 two-handed hero-picture lookup.
     */
    private static boolean isTwoHandedWeapon(TokenEntry token) {
        String pictureName = resolveHeroPictureName(token);
        return switch (pictureName) {
            case "bowman", "archer", "xbowman", "axeman2h", "swordsman2h", "mage_st" -> true;
            default -> false;
        };
    }

    /**
     * Native support extracted from CTextFile::GetAt(&g_heropicture, Token::GetId() - 1) in Token::IsTwoHandedHeroPictureToken @00438DA7.
     */
    private static String resolveHeroPictureName(TokenEntry token) {
        int pictureIndex = token.getId() - 1;
        if (pictureIndex < 0) {
            return "";
        }
        return get(TextTableId.HEROPICTURE, HeroPictureText.byIndex(pictureIndex));
    }

    /**
     * Native support extracted from CMainWindow::field115_0x3F4 carried payload reads in ApplyCarriedTokenToSelectionSlot @004B028D.
     */
    private static TokenEntry carriedToken(CMainWindow mainWindow) {
        Object payload = mainWindow.getUiLockPayload();
        return payload instanceof TokenEntry token ? token : null;
    }

    /**
     * Native support extracted from TokenEntry wire flag branch in SelectionInfoPanelVisualObject::ApplyCarriedTokenToSelectionSlot @004B028D.
     */
    private static boolean isSelectionSpellOrderToken(TokenEntry token) {
        return (token.wireFlags & TokenEntry.FLAG_STACKABLE) != 0
                && (token.wireFlags & TokenEntry.FLAG_SELECTION_SPELL_ORDER) != 0;
    }

    /**
     * Native support boundary for MapVisualObject::sendInventoryTransferAction @0041A20C equipment packet dispatch from SelectionInfoPanelVisualObject::ApplyCarriedTokenToSelectionSlot @004B028D.
     * Full support port.
     */
    private static void commitSelectionSlotEquipmentChange(
            MapVisualObject mapVisualObject,
            int sourcePackedModeCode,
            int sourceIndex,
            int targetPackedModeCode,
            int targetSlotIndex,
            int quantity
    ) {
        mapVisualObject.sendInventoryTransferAction(
                sourcePackedModeCode,
                sourceIndex,
                targetPackedModeCode,
                targetSlotIndex,
                quantity
        );
    }

    /**
     * Native support extracted from input-controller TEXT_LIST_SELECTION_CHANGED fan-out in SelectionInfoPanelVisualObject @004B012F and @004B028D.
     */
    private void notifyInputControllerSelectionChanged(CMainWindow mainWindow) {
        mainWindow.getInputController().onMessage(MessageCodes.TEXT_LIST_SELECTION_CHANGED, id, 0);
    }

    /**
     * Native helper: _g_screenRect.RightBottom.y in SelectionInfoPanelVisualObject::GetText.
     * not ported.
     */
    private static int screenBottomY() {
        return Globals.screenRect.bottom;
    }
}
