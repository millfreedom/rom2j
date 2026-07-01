package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CMousePointer;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.TokenEntry;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.gameobj.CGameObject;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.spell.StatModifiers;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.text.MainText;
import ua.millfreedom.rom2.text.SpellsText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_A;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.SHOP_DIALOG;
import static ua.millfreedom.rom2.model.window.windowproc.handlers.CMainWindowWindowProcSupport.readMessageInt;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.MainText.*;
import static ua.millfreedom.rom2.text.TextTableId.MAIN;
import static ua.millfreedom.rom2.text.TextTableId.SPELLS;

/**
 * Native class: SpellPanelVisualObject.
 * Purpose: spellbook/ability panel (id 3 in map UI).
 */
public class SpellPanelVisualObject extends CVisualObject {
    public static final int NATIVE_SIZE = 0x6C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int SPELL_SLOT_COLUMNS = 12;
    private static final int SPELL_SLOT_COUNT = 24;
    private static final int SPELL_SLOT_SIZE = 0x26;
    private static final int SPELL_SLOT_DRAW_INSET = 6;
    private static final int SPELL_SLOT_MARKER_OFFSET = 8;
    private static final int SPELL_SLOT_AUTOCAST_MARKER_Y = 0x22;
    private static final int PLAYER_SLOT_COUNT = 9;
    private static final int PLAYER_SLOT_FUNCTION_KEY_BASE = 4;
    private static final int PRESSED_SLOT_MARKER_SHIFT = 2;
    private static final int PRESSED_SLOT_SHADE_SIZE = 0x24;
    private static final int PRESSED_SLOT_SHADE_BRIGHTNESS = 4;
    private static final int SPELL_SLOT_PICK_MARGIN = 5;
    private static final int SPELL_PANEL_PICK_WIDTH = 0x1C8;
    private static final int SPELL_PANEL_PICK_HEIGHT = 0x4B;
    // Native right-side draw starts 0x10 pixels under the spellbook before the spellbook overdraws it.
    private static final int SPELLBOOK_RIGHT_SPACER_UNDERLAP = 0x10;
    private static final int LOWERCASE_A_CHAR = 0x61;
    // Native global spellTargetsUnitByPressedSlot @005F1810.
    private static final int[] SPELL_TARGETS_UNIT_BY_PRESSED_SLOT = {
            1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            1, 0, 0, 1, 1, 0, 0, 1, 1, 1, 0, 1
    };
    // Native global spellTargetsUnitBySelectedSlot @005F1990.
    private static final int[] SPELL_TARGETS_UNIT_BY_SELECTED_SLOT = {
            1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 1,
            1, 0, 0, 1, 0, 1, 1, 1, 1, 0, 0, 1
    };
    // Native global pressedSpellSelectCursorBySlot @005F1870, consumed by MapVisualObject::SelectMapCursor @0040B2B8.
    private static final int[] PRESSED_SPELL_SELECT_CURSOR_BY_SLOT = {
            1, 1, 1, 0, 0, 0, 0, 1, 0, 0, 1, 1,
            1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1
    };
    // Native global autoCastEnabledBySpellPanelSlot @005F18D0.
    private static final int[] AUTO_CAST_ENABLED_BY_SLOT = {
            1, 1, 1, 0, 1, 0, 0, 1, 0, 0, 1, 1,
            1, 1, 1, 0, 1, 0, 0, 0, 0, 1, 1, 1
    };
    // Native global directActivationEnabledBySlot @005F1930.
    private static final int[] DIRECT_ACTIVATION_ENABLED_BY_SLOT = {
            0, 0, 0, 1, 1, 1, 1, 0, 1, 1, 0, 0,
            0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 0
    };

    //0x5C
    public int field0x5c;
    //0x60
    public int pressedSpellSlot;
    //0x64
    public int selectedSpellSlot;
    //0x68
    public int selectedSpellEntryIndex;

    /**
     * Native: SpellPanelVisualObject::SpellPanelVisualObject @004C61A0.
     * Fully ported.
     */
    public SpellPanelVisualObject() {
        super();
        this.field0x5c = 0;
        this.pressedSpellSlot = -1;
        this.selectedSpellSlot = -1;
        this.selectedSpellEntryIndex = -1;
    }

    /**
     * Native: SpellPanelVisualObject::SpellPanelVisualObject @004C61E7.
     * Fully ported.
     */
    public SpellPanelVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        this.field0x5c = 0;
        this.pressedSpellSlot = -1;
        this.selectedSpellSlot = -1;
        this.selectedSpellEntryIndex = -1;
    }

    /**
     * Native: SpellPanelVisualObject::SpellPanelVisualObject @004C6246.
     * Fully ported. Native leaves selectedSpellEntryIndex at primitive default zero.
     */
    public SpellPanelVisualObject(int id, CRect rect) {
        super(id, rect, null);
        this.field0x5c = 0;
        this.pressedSpellSlot = -1;
        this.selectedSpellSlot = -1;
    }

    /**
     * vtbl +0x14: SpellPanelVisualObject::GetText @004C628F.
     * Fully ported.
     */
    @Override
    public String getText() {
        CMainWindow mainWindow = Globals.mainWindow;
        MapVisualObject map = mainWindow.pMapVisualObject;

        int slot = getSpellSlotAtScreenPoint(Globals.mousePointer.getX(), Globals.mousePointer.getY());
        if ((getTooltipSpellMask(map) & (1 << (slot & 0x1F))) == 0) {
            return null;
        }

        int modeFlags = mainWindow.dialogsMask;
        if (SHOP_DIALOG.isUnsetIn(modeFlags) && modeFlags != GAMEPLAY.mask) {
            return null;
        }

        if (slot < 0) {
            return null;
        }
        StatModifiers modifiers = map.getStatModifiers();
        int manaCost = modifiers.manaCost().get(slot);
        if (manaCost > 0xFFFE) {
            return null;
        }

        String spellName = get(SPELLS, SpellsText.byIndex(slot));

        List<String> lines = new ArrayList<>();
        lines.add(spellName);
        lines.add(String.format(
                Locale.US,
                "%s: %d",
                get(MAIN, MANA_COST_117),
                manaCost
        ));

        appendNonZeroMaxLine(lines, DAMAGE_118, modifiers.minDamage().get(slot), modifiers.maxDamage().get(slot));
        appendNonZeroMaxLine(lines, RANGE_123, modifiers.minRange().get(slot), modifiers.maxRange().get(slot));
        appendDurationLine(lines, modifiers.minDuration().get(slot), modifiers.maxDuration().get(slot));

        String bonusLine = formatSignedMaxLine(
                SPEED_182,
                modifiers.minSpeed().get(slot),
                modifiers.maxSpeed().get(slot)
        );
        String nextBonusLine = formatNonZeroMaxLine(
                RESISTANCE_183,
                modifiers.minResistance().get(slot),
                modifiers.maxResistance().get(slot)
        );
        if (nextBonusLine != null) {
            bonusLine = nextBonusLine;
        }
        nextBonusLine = formatSignedMaxLine(
                SIGHT_184,
                modifiers.minSight().get(slot),
                modifiers.maxSight().get(slot)
        );
        if (nextBonusLine != null) {
            bonusLine = nextBonusLine;
        }
        nextBonusLine = formatNonZeroMaxLine(
                MAXIMUM_DAMAGE_PROBABILITY_185,
                modifiers.minMaximumDamageProbability().get(slot),
                modifiers.maxMaximumDamageProbability().get(slot)
        );
        if (nextBonusLine != null) {
            bonusLine = nextBonusLine;
        }
        nextBonusLine = formatNonZeroMaxLine(
                MINIMUM_DAMAGE_PROBABILITY_187,
                modifiers.minMinimumDamageProbability().get(slot),
                modifiers.maxMinimumDamageProbability().get(slot)
        );
        if (nextBonusLine != null) {
            bonusLine = nextBonusLine;
        }
        nextBonusLine = formatNonZeroMaxLine(
                RAYS_186,
                modifiers.minRays().get(slot),
                modifiers.maxRays().get(slot)
        );
        if (nextBonusLine != null) {
            bonusLine = nextBonusLine;
        }
        nextBonusLine = formatNonZeroMaxLine(
                ABSORPTION_217,
                modifiers.minAbsorption().get(slot),
                modifiers.maxAbsorption().get(slot)
        );
        if (nextBonusLine != null) {
            bonusLine = nextBonusLine;
        }
        if (bonusLine != null) {
            lines.add(bonusLine);
        }

        return String.join("#", lines);
    }

    /**
     * vtbl +0x2C: SpellPanelVisualObject::Update @004C6AC1.
     * Fully ported.
     */
    @Override
    public void update() {
        CMainWindow mainWindow = Globals.mainWindow;
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        int leftInset = getPanelLeftInset(mainWindow);

        Globals.renderer.lockSurface();
        try {
            drawPanelSides(screenRect, leftInset);
            GUI.spellBook.drawRectMasked(screenRect.left + leftInset, screenRect.top);

            MapVisualObject map = mainWindow.pMapVisualObject;
            int availableMask = getPanelSpellMask(map);
            for (int slot = 0; slot < SPELL_SLOT_COUNT; slot++) {
                if ((availableMask & (1 << slot)) == 0) {
                    int slotLeft = screenRect.left + leftInset + SPELL_SLOT_DRAW_INSET
                            + (slot % SPELL_SLOT_COLUMNS) * SPELL_SLOT_SIZE;
                    int slotTop = screenRect.top + SPELL_SLOT_DRAW_INSET + (slot / SPELL_SLOT_COLUMNS) * SPELL_SLOT_SIZE;
                    GUI.spellBack.draw(slotLeft, slotTop, 0, 0, false);
                }
            }

            if (pressedSpellSlot >= 0 && (availableMask & (1 << pressedSpellSlot)) != 0) {
                int slotX = (pressedSpellSlot % SPELL_SLOT_COLUMNS) * SPELL_SLOT_SIZE + SPELL_SLOT_DRAW_INSET;
                int slotY = (pressedSpellSlot / SPELL_SLOT_COLUMNS) * SPELL_SLOT_SIZE + SPELL_SLOT_DRAW_INSET;
                Globals.renderer.applyShadeAdditiveToRect(
                        screenRect.left + leftInset + slotX,
                        screenRect.top + slotY,
                        screenRect.left + leftInset + slotX + PRESSED_SLOT_SHADE_SIZE,
                        screenRect.top + slotY + PRESSED_SLOT_SHADE_SIZE,
                        PRESSED_SLOT_SHADE_BRIGHTNESS
                );
            }

            int autoCastMask = map.getAutoCastSpellbookMask();
            for (int slot = 0; slot < SPELL_SLOT_COUNT; slot++) {
                if ((availableMask & (1 << slot)) != 0) {
                    int playerSlotLabel = resolvePlayerSlotLabel(mainWindow, slot);
                    if (playerSlotLabel != 0) {
                        drawSpellPanelMarker(screenRect, leftInset, slot, String.valueOf(playerSlotLabel),
                                SPELL_SLOT_MARKER_OFFSET);
                    }
                }
                if ((autoCastMask & (1 << slot)) != 0) {
                    drawSpellPanelMarker(screenRect, leftInset, slot, "A", SPELL_SLOT_AUTOCAST_MARKER_Y);
                }
            }
        } finally {
            Globals.renderer.unlockSurface();
        }
    }

    /**
     * vtbl +0x48: SpellPanelVisualObject::OnMessage @004C72F5.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        CMainWindow mainWindow = Globals.mainWindow;
        if (msg == MessageCodes.SPELL_PANEL_CLEAR_PRESSED_SLOT) {
            pressedSpellSlot = -1;
        } else if (msg == MessageCodes.ASSIGN_PLAYER_SLOT) {
            int w = readMessageInt(wParam);
            int l = readMessageInt(lParam);
            if (l == 0) {
                int preferredSlot = mainWindow.getAssignedSpellPanelSlot(w);
                if (preferredSlot >= 0) {
                    pressedSpellSlot = preferredSlot;
                }
            } else {
                int targetSlot = pressedSpellSlot;
                if (targetSlot < 0) {
                    targetSlot = getSpellSlotAtScreenPoint(Globals.mousePointer.getX(), Globals.mousePointer.getY());
                }
                if (targetSlot >= 0) {
                    mainWindow.assignSpellPanelSlotToPlayer(w, targetSlot);
                    mainWindow.clearSpellPanelSlotAssignmentsForOtherPlayers(w, targetSlot);
                    mainWindow.refreshSpellPanelPlayerAssignments();
                }
            }
        }
        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * vtbl +0x4C: SpellPanelVisualObject::OnMouseMove @004C77A2.
     * Fully ported.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        if (Globals.mousePointer.isSelecting()) {
            Globals.mousePointer.finishSelectionDrag();
        }
        if ((nFlags & 0x1) != 0) {
            onLButtonDown(nFlags, x, y);
        }
        if ((nFlags & 0x2) != 0) {
            onRButtonDown(nFlags, x, y);
        }
        return 0;
    }

    /**
     * vtbl +0x50: SpellPanelVisualObject::OnUserMsg @004C780B.
     * Fully ported.
     */
    @Override
    public int onUserMsg(int nFlags, int x, int y) {
        return onLButtonDown(nFlags, x, y);
    }

    /**
     * vtbl +0x54: SpellPanelVisualObject::OnLButtonDown @004C7580.
     * Fully ported.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        MapVisualObject map = Globals.mainWindow.pMapVisualObject;
        int slot = getSpellSlotAtScreenPoint(x, y);
        if (slot >= 0 && (getPanelSpellMask(map) & (1 << slot)) != 0) {
            pressedSpellSlot = slot;
        }
        return 1;
    }

    /**
     * vtbl +0x58: SpellPanelVisualObject::OnLButtonUp @004C76BC.
     * Fully ported.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        CMainWindow mainWindow = Globals.mainWindow;
        if (mainWindow.getUiLockPayload() == null) {
            return 1;
        }

        CMousePointer.Cursor_Default.setToMousePointer();
        if (SHOP_DIALOG.isUnsetIn(mainWindow.dialogsMask)) {
            HeroInventoryControlVisualObject selectionPanel = mainWindow.pHeroInventoryControlVisualObject;
            int packedModeCode = mainWindow.getUiLockPackedModeCode();
            if (packedModeCode == 2) {
                selectionPanel.completeUiDrag(mainWindow.getUiLockSourceIndex());
            } else if (packedModeCode == 1) {
                selectionPanel.completeUiDrag(selectionPanel.getVisibleStart());
            }
        } else {
            ShopDialogVisualObject shopDialog = (ShopDialogVisualObject) mainWindow.getInputController().getChildById(1000);
            shopDialog.cancelUiLockSelection();
        }
        return 1;
    }

    /**
     * vtbl +0x5C: SpellPanelVisualObject::OnLButtonDblClk @004C7604.
     * Fully ported.
     */
    @Override
    public int onLButtonDblClk(int nFlags, int x, int y) {
        MapVisualObject map = Globals.mainWindow.pMapVisualObject;
        int slot = getSpellSlotAtScreenPoint(x, y);
        if (slot >= 0
                && (getPanelSpellMask(map) & (1 << slot)) != 0
                && isDirectActivationSlot(slot)) {
            map.activateSpellPanelSlot(slot + 1);
        }
        return onLButtonDown(nFlags, x, y);
    }

    /**
     * vtbl +0x60: SpellPanelVisualObject::OnRButtonDown @004C75E8.
     * Fully ported.
     */
    @Override
    public int onRButtonDown(int nFlags, int x, int y) {
        pressedSpellSlot = -1;
        return 1;
    }

    /**
     * vtbl +0x64: SpellPanelVisualObject::OnRButtonUp @004C7786.
     * Fully ported.
     */
    @Override
    public int onRButtonUp(int nFlags, int x, int y) {
        pressedSpellSlot = -1;
        return 1;
    }

    /**
     * vtbl +0x68: SpellPanelVisualObject::OnRButtonDblClk @004C76AA.
     * Fully ported.
     */
    @Override
    public int onRButtonDblClk(int nFlags, int x, int y) {
        return 1;
    }

    /**
     * Native: SpellPanelVisualObject::GetActiveSpellSlot @0041F370.
     * Fully ported.
     */
    public int getActiveSpellSlot() {
        return selectedSpellSlot < 0 ? pressedSpellSlot : selectedSpellSlot;
    }

    /**
     * Native: SpellPanelVisualObject::SelectDraggedSpellEntry @004C7143.
     * Fully ported.
     */
    public void selectDraggedSpellEntry(int entryIndex) {
        CMainWindow mainWindow = Globals.mainWindow;
        MapVisualObject map = mainWindow.pMapVisualObject;
        CGameObject selectedObject = map.getPrimarySelectedObjectForGridOverlay();
        selectedSpellEntryIndex = entryIndex;
        TokenEntry entry = selectedObject.tokenEntries.get(selectedSpellEntryIndex);
        selectedSpellSlot = entry.getCastSpellId() - 1;
    }

    /**
     * Native: SpellPanelVisualObject::ActiveSpellTargetsUnit @004C72C5.
     * Fully ported.
     */
    public boolean activeSpellTargetsUnit() {
        int slot = getActiveSpellSlot();
        int[] targetModeTable = selectedSpellSlot < 0
                ? SPELL_TARGETS_UNIT_BY_PRESSED_SLOT
                : SPELL_TARGETS_UNIT_BY_SELECTED_SLOT;
        return targetModeTable[slot] != 0;
    }

    /**
     * Native: SpellPanelVisualObject::HasSelectedSpellSlot @0041F3A0.
     * Fully ported.
     */
    public boolean hasSelectedSpellSlot() {
        return selectedSpellSlot >= 0;
    }

    /**
     * Native: SpellPanelVisualObject::clearSelectedSpellSlot @004C7287.
     * Fully ported.
     */
    public void clearSelectedSpellSlot() {
        if (selectedSpellSlot >= 0) {
            selectedSpellSlot = -1;
            selectedSpellEntryIndex = -1;
        }
    }

    /**
     * Native: SpellPanelVisualObject::hasSelectedAvailableSpellSlot @004C70B9.
     * Fully ported.
     */
    public boolean hasSelectedAvailableSpellSlot(int spellSlot) {
        return (Globals.mainWindow.pMapVisualObject.getSelectedAvailableSpellMask() & (1 << (spellSlot & 0x1F))) != 0;
    }

    /**
     * Native: SpellPanelVisualObject::hasActiveSpellEffectSlot @004C70FE.
     * Fully ported.
     */
    public boolean hasActiveSpellEffectSlot(int spellSlot) {
        return (Globals.mainWindow.pMapVisualObject.getActiveSpellEffectMask() & (1 << (spellSlot & 0x1F))) != 0;
    }

    /**
     * Native: SpellPanelVisualObject::detachSelectedSpellEntry @004C71A0.
     * Fully ported.
     */
    public void detachSelectedSpellEntry() {
        CMainWindow mainWindow = Globals.mainWindow;
        MapVisualObject map = mainWindow.pMapVisualObject;
        if (selectedSpellSlot >= 0) {
            if (selectedSpellEntryIndex >= 0) {
                CGameObject primarySelectedObject = map.getPrimarySelectedObjectForGridOverlay();
                TokenEntry entry = primarySelectedObject.tokenEntries.get(selectedSpellEntryIndex);
                mainWindow.pHeroInventoryControlVisualObject.detachMatchingTokenEntry(entry, 1);
                if (map.getPrimarySelectedObjectForGridOverlay() != null) {
                    mainWindow.pHeroInventoryControlVisualObject.bindGridSourceFromContext(map.getPrimarySelectedObjectForGridOverlay());
                }
            }
            selectedSpellSlot = -1;
            selectedSpellEntryIndex = -1;
        }
    }

    /**
     * Native support: pressedSpellSelectCursorBySlot @005F1870 consumed by MapVisualObject::SelectMapCursor @0040B2B8.
     */
    public boolean activePressedSpellSelectsUnitCursor() {
        int slot = getActiveSpellSlot();
        return slot >= 0 && slot < PRESSED_SPELL_SELECT_CURSOR_BY_SLOT.length
                && PRESSED_SPELL_SELECT_CURSOR_BY_SLOT[slot] != 0;
    }

    /**
     * vtbl +0x6C: SpellPanelVisualObject::OnKeyDown @004C74B1.
     * Native compares both `VK_A` (`0x41`) and lowercase ASCII `a` (`0x61`), then maps the pressed
     * or hovered spell-panel slot through the native auto-cast enable table and `g_Spell_IDs`.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        CMainWindow mainWindow = Globals.mainWindow;
        MapVisualObject map = mainWindow.pMapVisualObject;
        if (!mainWindow.isControlKeyDown()) {
            return 0;
        }
        if (nChar != LOWERCASE_A_CHAR && nChar != VK_A) {
            return 0;
        }

        int slot = pressedSpellSlot;
        if (slot < 0) {
            slot = getSpellSlotAtScreenPoint(Globals.mousePointer.getX(), Globals.mousePointer.getY());
        }
        if (slot >= 0 && isAutoCastSlot(slot)) {
            map.toggleAutoCastSpellByPanelSlot(slot);
            map.updateSelectionState();
        }
        return 0;
    }

    /**
     * Native helper: SpellPanelVisualObject::GetSpellSlotAtScreenPoint @004C6FE0.
     * Fully ported.
     */
    private int getSpellSlotAtScreenPoint(int x, int y) {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);

        int localX = x - screenRect.left - SPELL_SLOT_PICK_MARGIN;
        int localY = y - screenRect.top - SPELL_SLOT_PICK_MARGIN;
        CMainWindow mainWindow = Globals.mainWindow;
        if (SHOP_DIALOG.isUnsetIn(mainWindow.dialogsMask)) {
            localX -= Globals.mainWindowRect.left;
        }

        if (localX < 0 || localY < 0 || localY >= SPELL_PANEL_PICK_HEIGHT || localX >= SPELL_PANEL_PICK_WIDTH) {
            return -1;
        }
        return (localY / SPELL_SLOT_SIZE) * SPELL_SLOT_COLUMNS + (localX / SPELL_SLOT_SIZE);
    }

    /**
     * Native support extracted from SpellPanelVisualObject::Update @004C6AC1 selection/spell-effect mask merge.
     */
    private static int getPanelSpellMask(MapVisualObject mapVisualObject) {
        return mapVisualObject.getSelectedAvailableSpellMask() | mapVisualObject.getActiveSpellEffectMask();
    }

    /**
     * Native support extracted from SpellPanelVisualObject::GetText @004C628F selected-available spell mask read.
     */
    private static int getTooltipSpellMask(MapVisualObject mapVisualObject) {
        return mapVisualObject.getSelectedAvailableSpellMask();
    }

    /**
     * Native support extracted from SpellPanelVisualObject::Update @004C6AC1 spell-panel chrome x-offset branch.
     */
    private static int getPanelLeftInset(CMainWindow mainWindow) {
        if (SHOP_DIALOG.isSetIn(mainWindow.dialogsMask)) {
            return 0;
        }
        return Globals.mainWindowRect.left;
    }

    /**
     * Native support extracted from SpellPanelVisualObject::Update @004C6AC1 left/right chrome draw branch.
     */
    private static void drawPanelSides(CRect screenRect, int leftInset) {
        if (leftInset == 0) {
            return;
        }

        RightPanelLayout rightPanelLayout = RightPanelLayout.forScreenHeight(Globals.screenRect.bottom);
        CBmp64k leftBitmap;
        CBmp64k rightBitmap;
        if (rightPanelLayout.usesHighResolutionArt()) {
            leftBitmap = GUI.spbLeft1024;
            rightBitmap = GUI.spbRight1024;
        } else if (rightPanelLayout.usesMediumResolutionArt()) {
            leftBitmap = GUI.spbLeft800;
            rightBitmap = GUI.spbRight800;
        } else {
            return;
        }

        int spellBookLeft = screenRect.left + leftInset;
        int spellBookRight = spellBookLeft + GUI.spellBook.xSizeOf(0);
        drawLeftPanelSpacerTiles(leftBitmap, screenRect.left, spellBookLeft, screenRect.top);
        drawRightPanelSpacerTiles(leftBitmap, rightBitmap, spellBookRight, screenRect.right, screenRect.top);
    }

    /**
     * not ported. Java native-resolution extension repeats the left spellbook side art toward the map's left edge.
     */
    private static void drawLeftPanelSpacerTiles(CBmp64k bitmap, int panelLeft, int spellBookLeft, int y) {
        int tileWidth = bitmap.xSizeOf(0);
        int tileHeight = bitmap.ySizeOf(0);
        for (int tileRight = spellBookLeft; tileRight > panelLeft; tileRight -= tileWidth) {
            int tileLeft = Math.max(panelLeft, tileRight - tileWidth);
            int width = tileRight - tileLeft;
            bitmap.drawRectMasked(tileLeft, y, tileWidth - width, 0, tileWidth, tileHeight);
        }
    }

    /**
     * not ported. Java native-resolution extension anchors the right spellbook cap at the right panel and repeats the
     * left side art back toward the spellbook.
     */
    private static void drawRightPanelSpacerTiles(CBmp64k fillBitmap, CBmp64k rightCapBitmap, int spellBookRight,
                                                  int panelRight, int y) {
        int rightCapWidth = rightCapBitmap.xSizeOf(0);
        int tileHeight = rightCapBitmap.ySizeOf(0);
        int rightCapLeft = Math.max(spellBookRight - SPELLBOOK_RIGHT_SPACER_UNDERLAP, panelRight - rightCapWidth);
        int rightCapVisibleLeft = Math.max(spellBookRight, rightCapLeft);
        rightCapBitmap.drawRectMasked(
                rightCapVisibleLeft,
                y,
                rightCapVisibleLeft - rightCapLeft,
                0,
                rightCapWidth,
                tileHeight
        );

        int fillLeftBoundary = spellBookRight - SPELLBOOK_RIGHT_SPACER_UNDERLAP;
        int fillTileWidth = fillBitmap.xSizeOf(0);
        int fillTileHeight = fillBitmap.ySizeOf(0);
        for (int tileRight = rightCapLeft; tileRight > fillLeftBoundary; tileRight -= fillTileWidth) {
            int tileLeft = Math.max(fillLeftBoundary, tileRight - fillTileWidth);
            int width = tileRight - tileLeft;
            fillBitmap.drawRectMasked(
                    tileLeft,
                    y,
                    fillTileWidth - width,
                    0,
                    fillTileWidth,
                    fillTileHeight
            );
        }
    }

    /**
     * Native owner: SpellPanelVisualObject::OnLButtonDblClk @004C7604 slot-enable table at DAT_005F1930.
     */
    private static boolean isDirectActivationSlot(int slot) {
        return slot >= 0 && slot < DIRECT_ACTIVATION_ENABLED_BY_SLOT.length
                && DIRECT_ACTIVATION_ENABLED_BY_SLOT[slot] != 0;
    }

    /**
     * Native support extracted from SpellPanelVisualObject::OnKeyDown @004C74B1.
     */
    private static boolean isAutoCastSlot(int slot) {
        return slot >= 0 && slot < AUTO_CAST_ENABLED_BY_SLOT.length && AUTO_CAST_ENABLED_BY_SLOT[slot] != 0;
    }

    /**
     * Native support extracted from SpellPanelVisualObject::Update @004C6AC1 player-slot marker loop.
     */
    private int resolvePlayerSlotLabel(CMainWindow mainWindow, int slot) {
        for (int playerIndex = 0; playerIndex < PLAYER_SLOT_COUNT; playerIndex++) {
            if (mainWindow.getAssignedSpellPanelSlot(playerIndex) == slot) {
                return playerIndex + PLAYER_SLOT_FUNCTION_KEY_BASE;
            }
        }
        return 0;
    }

    /**
     * Native support extracted from SpellPanelVisualObject::Update @004C6AC1 marker text draw calls.
     */
    private void drawSpellPanelMarker(CRect screenRect, int leftInset, int slot, String text, int markerY) {
        int pressedShift = pressedSpellSlot == slot ? PRESSED_SLOT_MARKER_SHIFT : 0;
        int slotColumn = slot % SPELL_SLOT_COLUMNS;
        int slotRow = slot / SPELL_SLOT_COLUMNS;
        Globals.fonts.font3.drawTextShadowed(
                screenRect.left + leftInset + SPELL_SLOT_MARKER_OFFSET + pressedShift + slotColumn * SPELL_SLOT_SIZE,
                screenRect.top + markerY + pressedShift + slotRow * SPELL_SLOT_SIZE,
                text,
                0,
                Palettes.grayDim,
                1
        );
    }

    /**
     * Native support extracted from SpellPanelVisualObject::GetText @004C628F max-value-present stat lines.
     */
    private static void appendNonZeroMaxLine(List<String> lines, MainText labelEntry, int minValue, int maxValue) {
        String formatted = formatNonZeroMaxLine(labelEntry, minValue, maxValue);
        if (formatted != null) {
            lines.add(formatted);
        }
    }

    /**
     * Native support extracted from SpellPanelVisualObject::GetText @004C628F duration branch.
     */
    private static void appendDurationLine(List<String> lines, int minValue, int maxValue) {
        if (maxValue == 0) {
            return;
        }

        String label = get(MAIN, DURATION_124);
        if (minValue == maxValue) {
            lines.add(String.format(Locale.US, "%s: %.1f", label, minValue / 16.0));
            return;
        }
        lines.add(String.format(Locale.US, "%s: %.1f - %.1f", label, minValue / 16.0, maxValue / 16.0));
    }

    /**
     * Native support extracted from SpellPanelVisualObject::GetText @004C628F zero-max sentinel branches.
     */
    private static String formatNonZeroMaxLine(MainText labelEntry, int minValue, int maxValue) {
        if (maxValue == 0) {
            return null;
        }
        return formatMinMaxLine(labelEntry, minValue, maxValue);
    }

    /**
     * Native support extracted from SpellPanelVisualObject::GetText @004C628F signed max sentinel branches.
     */
    private static String formatSignedMaxLine(MainText labelEntry, int minValue, int maxValue) {
        if (maxValue <= -0xFFFF) {
            return null;
        }
        return formatMinMaxLine(labelEntry, minValue, maxValue);
    }

    /**
     * Native support extracted from SpellPanelVisualObject::GetText @004C628F min/max CString formatting.
     */
    private static String formatMinMaxLine(MainText labelEntry, int minValue, int maxValue) {
        String label = get(MAIN, labelEntry);
        if (minValue == maxValue) {
            return String.format(Locale.US, "%s: %d", label, minValue);
        }
        return String.format(Locale.US, "%s: %d - %d", label, minValue, maxValue);
    }

}
