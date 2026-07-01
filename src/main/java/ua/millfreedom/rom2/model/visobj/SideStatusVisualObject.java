package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CCursor;
import ua.millfreedom.rom2.model.CGameBitmap;
import ua.millfreedom.rom2.model.CMousePointer;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.gameobj.CGameObject;
import ua.millfreedom.rom2.model.gameobj.CStructure;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.text.BuildingText;
import ua.millfreedom.rom2.text.TextTableId;

import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_HEALTH_19;

/**
 * Native class: SideStatusVisualObject.
 * Purpose: bottom-right side info panel below the portrait panel.
 */
public class SideStatusVisualObject extends CVisualObject {
    public static final int NATIVE_SIZE = 0x60; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int MAP_MODE_ACTIVE = 1;

    //0x5c
    public int dirtyFlag;

    /**
     * Native: SideStatusVisualObject::SideStatusVisualObject @004B07FA.
     * Full port.
     */
    public SideStatusVisualObject() {
        super();
        dirtyFlag = 0;
    }

    /**
     * Native: SideStatusVisualObject::SideStatusVisualObject @004B0823.
     * Full port.
     */
    public SideStatusVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        dirtyFlag = 0;
    }

    /**
     * Native: SideStatusVisualObject::SideStatusVisualObject @004B0864.
     * Full port.
     */
    public SideStatusVisualObject(int id, CRect rect) {
        super(id, rect, null);
        dirtyFlag = 0;
    }

    /**
     * vtbl +0x14: SideStatusVisualObject::GetText @004B0E39.
     * Full port.
     */
    @Override
    public String getText() {
        if (Globals.mainWindow.getUiLockFlag3f4() != 0) {
            return null;
        }

        MapVisualObject mapContext = resolveMapContext();
        if (mapContext.getSelectedCount() != 1) {
            return null;
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        int infoPanelHeight = selectionInfoPanelHeight();
        RightPanelLayout rightPanelLayout = RightPanelLayout.forScreenHeight(Globals.screenRect.bottom);
        if (!rightPanelLayout.hasStatusInfoPanel() || screenRect.height() < infoPanelHeight) {
            return null;
        }

        CRect statusInfoRect = resolveStatusInfoRect(screenRect, rightPanelLayout, infoPanelHeight);
        int localX = Globals.mousePointer.getX() - statusInfoRect.left;
        int localY = Globals.mousePointer.getY() - statusInfoRect.top - 2;
        return resolveStatusTooltip(mapContext.getPrimarySelectedObject(), localX, localY);
    }

    /**
     * vtbl +0x2C: SideStatusVisualObject::Update @004B0A10.
     * Full port.
     */
    @Override
    public void update() {
        if (!isGameplayVisualDialogMode()) {
            return;
        }
        renderSideStatusPanel();
    }

    /**
     * Native support extracted from SideStatusVisualObject::Update @004B0A10 render body.
     */
    private void renderSideStatusPanel() {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        int infoPanelHeight = selectionInfoPanelHeight();
        RightPanelLayout rightPanelLayout = RightPanelLayout.forScreenHeight(Globals.screenRect.bottom);
        MapVisualObject mapContext = resolveMapContext();

        Globals.renderer.lockSurface();
        try {
            if (rightPanelLayout.hasStatusInfoPanel() && infoPanelHeight <= screenRect.height()) {
                CRect statusInfoRect = resolveStatusInfoRect(screenRect, rightPanelLayout, infoPanelHeight);
                CGameObject currentObject = resolveDisplayedObject(mapContext);
                drawStatusInfoBackground(statusInfoRect);
                if (currentObject instanceof CStructure structure) {
                    renderStructureInfo(structure, statusInfoRect);
                } else if (currentObject instanceof CUnit unit) {
                    renderUnitInfo(unit, statusInfoRect);
                }
            }
            drawExtraRightFiller(screenRect, rightPanelLayout);
        } finally {
            Globals.renderer.unlockSurface();
        }
        dirtyFlag = 0;

    }

    /**
     * vtbl +0x48: SideStatusVisualObject::OnMessage @004B0D0E.
     * Full port.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        int result = super.onMessage(msg, wParam, lParam);
        if (result != 0) {
            return result;
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        int infoPanelHeight = selectionInfoPanelHeight();

        return switch (msg) {
            case RENDER_FRAME -> {
                if (isGameplayVisualDialogMode()) {
                    draw();
                }
                if (isNonModalGameplayDialogMode()) {
                    updateCursorForPanelHover();
                }
                yield 0;
            }
            case SET_MAP_CONTEXT, NOTIFY_MAP_CONTEXT_CHANGED, NOTIFY_SELECTION_SPELL_STATE -> {
                dirtyFlag = 1;
                yield 0;
            }
            default -> 0;
        };
    }

    /**
     * Native: SideStatusVisualObject::UpdateCursorForPanelHover @004B0899.
     * Full port.
     */
    private void updateCursorForPanelHover() {
        CGameBitmap sourceBitmap = Globals.mousePointer.getSourceBitmap();
        CRect panelScreenRect = new CRect();
        clientToScreen(panelScreenRect, cRect);
        CCursor nextCursor = null;
        int mouseX = Globals.mousePointer.getX();
        int mouseY = Globals.mousePointer.getY();
        if (!panelScreenRect.contains(mouseX, mouseY)) {
            return;
        }

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
        if (Globals.mainWindow.getUiLockFlag3f4() != 0) {
            nextCursor = Globals.mainWindow.cursor;
        }

        if (nextCursor != null && sourceBitmap != nextCursor.getBitmap()) {
            nextCursor.setToMousePointer();
        }
    }

    /**
     * Native object-selection branch shared by SideStatusVisualObject::Update @004B0A10 and ::GetText @004B0E39.
     * Full support port.
     */
    private MapVisualObject resolveMapContext() {
        return Globals.mainWindow.pMapVisualObject;
    }

    /**
     * Native object-selection branch in SideStatusVisualObject::Update @004B0A10.
     * Full support port.
     */
    private CGameObject resolveDisplayedObject(MapVisualObject mapContext) {
        CGameObject hoveredObject = resolveHoveredObject(mapContext);
        if (hoveredObject != null) {
            return hoveredObject;
        }
        return mapContext.getSelectedCount() == 1 ? mapContext.getPrimarySelectedObject() : null;
    }

    /**
     * Native hovered-object branch in SideStatusVisualObject::Update @004B0A10.
     * Full support port.
     */
    private CGameObject resolveHoveredObject(MapVisualObject mapContext) {
        if (Globals.mainWindow.uiLockPayload != null) {
            return null;
        }
        return mapContext.resolveHoveredObjectForSelectionInfoPanelUpdate();
    }

    /**
     * Native geometry branch shared by SideStatusVisualObject::Update @004B0A10 and ::GetText @004B0E39.
     * Full support port.
     */
    private CRect resolveStatusInfoRect(CRect screenRect, RightPanelLayout rightPanelLayout, int infoPanelHeight) {
        int top = screenRect.top + rightPanelLayout.statusInfoTop - rightPanelLayout.sideStatusTop;
        return new CRect(screenRect.left, top, screenRect.right, top + infoPanelHeight);
    }

    /**
     * Native owner: CMainWindow::CVisualObject_1_6 height reads in SideStatusVisualObject::Update @004B0A10 and ::OnMessage @004B0D0E.
     * Full support port.
     */
    private int selectionInfoPanelHeight() {
        return Globals.mainWindow.pSelectionInfoPanelVisualObject.getRect().height();
    }

    /**
     * Native owner: text-panel background draw in SideStatusVisualObject::Update @004B0A10.
     * Full support port.
     */
    private void drawStatusInfoBackground(CRect statusInfoRect) {
        GUI.textBackR.draw(statusInfoRect.left, statusInfoRect.top, 0, null, false);
    }

    /**
     * Native support extracted from SideStatusVisualObject::Update @004B0A10.
     * Fully ported. Java keeps the native 1024px filler at 768px height, then repeats the taller 800px tile above it.
     */
    private void drawExtraRightFiller(CRect screenRect, RightPanelLayout rightPanelLayout) {
        CBmp64k fillBitmap;
        if (rightPanelLayout.usesHighResolutionArt()) {
            fillBitmap = rightPanelLayout.usesTallExtraFillArt() ? GUI.extraRight800 : GUI.extraRight1024;
        } else if (rightPanelLayout.usesMediumResolutionArt()) {
            fillBitmap = GUI.extraRight800;
        } else {
            return;
        }

        int tileHeight = fillBitmap.ySizeOf(0);
        int tileWidth = fillBitmap.xSizeOf(0);
        int fillBottom = rightPanelLayout.extraFillBottom();
        for (int fillTop = rightPanelLayout.extraFillTop; fillTop < fillBottom; fillTop += tileHeight) {
            int height = Math.min(tileHeight, fillBottom - fillTop);
            int localFillTop = fillTop - rightPanelLayout.sideStatusTop;
            fillBitmap.drawRectMasked(screenRect.left, screenRect.top + localFillTop, 0, 0, tileWidth, height);
        }
    }

    /**
     * Native owner: unit-detail renderer `CUnit::RenderFullStatsInfo @0046AA1D`, called from SideStatusVisualObject::Update @004B0A10.
     * Full support port.
     */
    private void renderUnitInfo(
            CUnit currentUnit,
            CRect statusInfoRect
    ) {
        currentUnit.renderFullStatsInfo(statusInfoRect);
    }

    /**
     * Native structure branch inside SideStatusVisualObject::Update @004B0A10.
     * Full support port.
     */
    private void renderStructureInfo(
            CStructure currentStructure,
            CRect statusInfoRect
    ) {
        CBitmapFont font = Globals.fonts.font2;
        int centerX = statusInfoRect.right - 0x58;
        font.drawTextShadowed(
                centerX,
                statusInfoRect.top + 0x1C,
                get(TextTableId.BUILDING, BuildingText.byIndex(currentStructure.type - 1)),
                TextAlign.CENTER.mask,
                Palettes.yellowish,
                1
        );
        font.drawTextShadowed(centerX, statusInfoRect.top + 0x2C, get(MAIN_HEALTH_19), TextAlign.CENTER.mask, Palettes.yellowish, 1);
        font.drawTextShadowed(
                centerX,
                statusInfoRect.top + 0x36,
                Short.toUnsignedInt(currentStructure.HP) + "/" + Short.toUnsignedInt(currentStructure.MaxHP),
                TextAlign.CENTER.mask,
                Palettes.greenLeaningGray,
                1
        );
    }

    /**
     * Native owner: `CUnit::GetFullStatsTooltipText @0046B9F0`, called from SideStatusVisualObject::GetText @004B0E39.
     * Full support port.
     */
    private String resolveStatusTooltip(
            CGameObject currentObject,
            int localX,
            int localY
    ) {
        return currentObject instanceof CUnit unit ? unit.getFullStatsTooltipText(localX, localY) : null;
    }

}
