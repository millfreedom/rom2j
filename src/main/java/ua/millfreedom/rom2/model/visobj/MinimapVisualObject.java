package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CCursor;
import ua.millfreedom.rom2.model.CGameBitmap;
import ua.millfreedom.rom2.model.CMousePointer;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.color.RGB16;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.world.MapDescriptor;

import java.awt.Point;

/**
 * Native class: MinimapVisualObject.
 * Purpose: right-side minimap crystal panel that mirrors and drives the active map context.
 */
public class MinimapVisualObject extends CVisualObject {
    public static final int NATIVE_SIZE = 0x70; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int MODE_FLAG_BLOCK_MINIMAP_UPDATE = 0x2;
    private static final int MODE_FLAG_BLOCK_MINIMAP_INPUT = 0x4;
    private static final int MINIMAP_REFRESH_TICK_DELTA = 0x0B;
    private static final int MINIMAP_CENTER_X = 0x48;
    private static final int MINIMAP_CENTER_Y = 0x52;
    private static final int MINIMAP_INNER_MARGIN = 8;
    private static final int MINIMAP_TILE_MASK = 0x1FFF;
    private static final int MINIMAP_TILE_SOURCE_Y_MASK = 0x0F;
    private static final int TILE_OCCUPANCY_MASK = 0xC000;
    private static final int TILE_PARTIAL_OCCUPANCY_MASK = 0x8000;
    private static final int TERRAIN_LIGHT_BASE = 0x60;
    private static final int RGB16_QUARTER_BLEND_MASK = 0x39E7;
    private static final int RGB16_HALF_BLEND_MASK = 0x7BEF;
    private static final int CURSOR_ACTION_CAMERA = 0;
    private static final int CURSOR_ACTION_MOVE = 1;
    private static final int CURSOR_ACTION_ATTACK = 2;
    private static final int CURSOR_ACTION_DEFEND = 3;
    private static final int CURSOR_ACTION_CAST = 4;
    private static final int CURSOR_ACTION_PATROL = 5;
    private static final int CURSOR_ACTION_NONE = 6;

    //0x5c
    public MapVisualObject mapContext0x5c;
    //0x60
    public CBmp64k minimapBitmapPrimary0x60;
    //0x64
    public CBmp64k minimapBitmapSecondary0x64;
    //0x68
    public int zoomLevel0x68;
    //0x6c
    public int cachedViewMarker0x6c;

    /**
     * Native: MinimapVisualObject::MinimapVisualObject @004AB720.
     * Full port.
     */
    public MinimapVisualObject() {
        super();
        initMinimapBitmapFields();
    }

    /**
     * Native: MinimapVisualObject::MinimapVisualObject @004AB753.
     * Full port.
     */
    public MinimapVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        initMinimapBitmapFields();
    }

    /**
     * Native: MinimapVisualObject::MinimapVisualObject @004AB79E.
     * Full port.
     */
    public MinimapVisualObject(int id, CRect rect) {
        super(id, rect, null);
        initMinimapBitmapFields();
    }

    /**
     * Native support extracted from MinimapVisualObject constructors @004AB720, @004AB753, and @004AB79E.
     */
    private void initMinimapBitmapFields() {
        minimapBitmapPrimary0x60 = null;
        minimapBitmapSecondary0x64 = null;
    }

    /**
     * vtbl +0x2C: MinimapVisualObject::Update @004AC414.
     * Full port.
     */
    @Override
    public void update() {
        MapVisualObject mapContext = resolveMapContext();
        if (mapContext == null) {
            return;
        }

        int modeFlags = visualDialogModeFlags();
        if ((modeFlags & MODE_FLAG_BLOCK_MINIMAP_UPDATE) != 0 || (modeFlags & MODE_FLAG_BLOCK_MINIMAP_INPUT) != 0) {
            return;
        }
        if (Math.abs(cachedViewMarker0x6c - mapContext.mapAnimationTick) >= MINIMAP_REFRESH_TICK_DELTA) {
            cachedViewMarker0x6c = mapContext.mapAnimationTick;
        }

        MapDescriptor mapDescriptor = mapContext.mapDescriptor;
        if (mapDescriptor == null) {
            return;
        }

        renderMinimapPanel(mapContext, mapDescriptor);
    }

    /**
     * Native support extracted from MinimapVisualObject::Update @004AC414 render body.
     */
    private void renderMinimapPanel(MapVisualObject mapContext, MapDescriptor mapDescriptor) {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        Point bitmapOffset = computeMinimapBitmapOffset(mapDescriptor);
        drawCrystalBackground(screenRect);

        CBmp64k displayedBitmap = resolveDisplayedBitmap(mapContext);
        drawMinimapBitmap(screenRect, bitmapOffset, displayedBitmap);
        if (mapContext.mapOccupancyDirty != 0) {
            refreshSecondaryMinimapBitmap(mapContext, mapDescriptor, bitmapOffset);
            drawMinimapBitmap(screenRect, bitmapOffset, minimapBitmapSecondary0x64);
        }

        mapContext.drawMinimapObjects(screenRect.left + bitmapOffset.x, screenRect.top + bitmapOffset.y, zoomLevel0x68);
        drawViewportRect(screenRect, mapContext, bitmapOffset);
    }

    /**
     * vtbl +0x48: MinimapVisualObject::OnMessage @004AB896.
     * Full port.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        int result = super.onMessage(msg, wParam, lParam);
        if (result != 0) {
            return result;
        }

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
            case SET_MAP_CONTEXT -> {
                mapContext0x5c = (MapVisualObject) wParam;
                yield 0;
            }
            case REBUILD_MINIMAP_BITMAPS -> {
                rebuildMinimapBitmaps();
                yield 1;
            }
            case NOTIFY_MAP_CONTEXT_CHANGED -> {
                cachedViewMarker0x6c = -100;
                yield 0;
            }
            default -> 0;
        };
    }

    /**
     * vtbl +0x4C: MinimapVisualObject::OnMouseMove @004AD4B1.
     * Full port.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        if ((nFlags & 0x1) != 0) {
            return onLButtonDown(nFlags, x, y);
        }
        if ((nFlags & 0x2) != 0) {
            return onRButtonDown(nFlags, x, y);
        }
        return 0;
    }

    /**
     * vtbl +0x54: MinimapVisualObject::OnLButtonDown @004AD00E.
     * Full port.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        MapVisualObject mapContext = resolveMapContext();
        Point centeredTile = computeCenteredMapTileFromPanelPoint(mapContext, x, y);
        int actionKind = resolveLeftClickAction();
        if (actionKind == CURSOR_ACTION_CAMERA) {
            mapContext.onMessage(MessageCodes.SET_CAMERA_POS, centeredTile.x, centeredTile.y);
            return 0;
        }

        Point rawTile = computeRawMapTileFromPanelPoint(mapContext, x, y);
        switch (actionKind) {
            case CURSOR_ACTION_MOVE -> mapContext.issueMinimapMoveOrder(rawTile.x);
            case CURSOR_ACTION_ATTACK -> dispatchMinimapAttackAction(mapContext, rawTile);
            case CURSOR_ACTION_DEFEND -> dispatchMinimapDefendAction(mapContext, rawTile);
            case CURSOR_ACTION_CAST -> mapContext.findObjectTokenAtCell(rawTile.x, rawTile.y);
            case CURSOR_ACTION_PATROL -> mapContext.issueMinimapPatrolOrder(rawTile.x, rawTile.y);
            default -> {
            }
        }

        mapContext.inputMode = 0;
        Globals.mainWindow.getOrderPanelVisual().onMessage(MessageCodes.RESET_ORDER_SELECTION, 0, 0);
        return 0;
    }

    /**
     * vtbl +0x58: MinimapVisualObject::OnLButtonUp @004AD506.
     * Full port.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        if (Globals.mainWindow.uiLockPayload != null) {
            restoreCursorAfterUiLockClick();
        }
        return 1;
    }

    /**
     * vtbl +0x60: MinimapVisualObject::OnRButtonDown @004AD32F.
     * Full port.
     */
    @Override
    public int onRButtonDown(int nFlags, int x, int y) {
        MapVisualObject mapContext = resolveMapContext();
        Point centeredTile = computeCenteredMapTileFromPanelPoint(mapContext, x, y);
        mapContext.onMessage(MessageCodes.SET_CAMERA_POS, centeredTile.x, centeredTile.y);
        return 0;
    }

    /**
     * Native helper: MinimapVisualObject::RebuildMinimapBitmaps @004AB96C.
     * Full port.
     */
    private void rebuildMinimapBitmaps() {
        MapDescriptor mapDescriptor = resolveMapContext().mapDescriptor;
        CBmp64k terrainBitmap = buildMinimapTerrainBitmap(mapDescriptor);

        zoomLevel0x68 = computeZoomLevel(mapDescriptor.getWidth(), mapDescriptor.getHeight());
        int bitmapWidth = scaledMapSpan(mapDescriptor.getWidth() - (MINIMAP_INNER_MARGIN * 2));
        int bitmapHeight = scaledMapSpan(mapDescriptor.getHeight() - (MINIMAP_INNER_MARGIN * 2));

        minimapBitmapPrimary0x60 = new CBmp64k(bitmapWidth, bitmapHeight);
        minimapBitmapSecondary0x64 = new CBmp64k(bitmapWidth, bitmapHeight);
        copyTerrainToPrimaryMinimapBitmap(terrainBitmap);
        applyTerrainLightToPrimaryMinimapBitmap(mapDescriptor);
        minimapBitmapPrimary0x60.syncFrameBytesFromSurface();
    }

    /**
     * Native: MinimapVisualObject::UpdateCursorForPanelHover @004ACCB4.
     * Full port.
     */
    private void updateCursorForPanelHover() {
        CGameBitmap sourceBitmap = Globals.mousePointer.getSourceBitmap();
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        int mouseX = Globals.mousePointer.getX();
        int mouseY = Globals.mousePointer.getY();
        if (!screenRect.contains(mouseX, mouseY)) {
            return;
        }

        CCursor nextCursor;
        if (mouseX >= Globals.screenRect.right - 2 && isNonModalGameplayDialogMode()) {
            if (mouseY == 0) {
                nextCursor = CMousePointer.Cursor_ArrowNE;
            } else if (mouseY < Globals.screenRect.bottom - 2) {
                nextCursor = CMousePointer.Cursor_ArrowE;
            } else {
                nextCursor = CMousePointer.Cursor_ArrowSE;
            }
        } else if (mouseY == 0 && isNonModalGameplayDialogMode()) {
            nextCursor = CMousePointer.Cursor_ArrowN;
        } else {
            nextCursor = resolveMinimapHoverCursor(screenRect, mouseX, mouseY);
        }

        if (Globals.mainWindow.uiLockPayload != null) {
            nextCursor = Globals.mainWindow.cursor;
        }
        if (nextCursor != null && sourceBitmap != nextCursor.getBitmap()) {
            nextCursor.setToMousePointer();
        }
    }

    /**
     * Native minimap cursor branch in MinimapVisualObject::UpdateCursorForPanelHover @004ACCB4.
     * Full support port.
     */
    private CCursor resolveMinimapHoverCursor(CRect screenRect, int mouseX, int mouseY) {
        MapVisualObject mapContext = resolveMapContext();
        if (isOutsideMinimapMapBounds(mapContext, screenRect, mouseX, mouseY)) {
            return CMousePointer.Cursor_SmallDefault;
        }
        if (mapContext.getSelectedCount() == 0) {
            return CMousePointer.Cursor_SmallDefault;
        }

        CCursor nextCursor = CMousePointer.Cursor_SmallMove;
        if (mapContext.inputMode != 0) {
            nextCursor = resolveMinimapInputModeCursor(mapContext.inputMode);
        }
        if ((mapContext.getSelectionFlags() & 0x24) != 0) {
            return CMousePointer.Cursor_SmallDefault;
        }
        return nextCursor;
    }

    /**
     * Native minimap bounds branch in MinimapVisualObject::UpdateCursorForPanelHover @004ACCB4.
     * Full support port.
     */
    private boolean isOutsideMinimapMapBounds(MapVisualObject mapContext, CRect screenRect, int mouseX, int mouseY) {
        MapDescriptor mapDescriptor = mapContext.mapDescriptor;
        int offsetX = MINIMAP_CENTER_X - (scaledMapSpan(mapDescriptor.getWidth() - (MINIMAP_INNER_MARGIN * 2)) / 2);
        int offsetY = MINIMAP_CENTER_Y - (scaledMapSpan(mapDescriptor.getHeight() - (MINIMAP_INNER_MARGIN * 2)) / 2);
        int relativeX = mouseX - screenRect.left - offsetX;
        int relativeY = mouseY - screenRect.top - offsetY;
        int boundsX = unscalePanelOffsetForHoverBounds(relativeX);
        int boundsY = unscalePanelOffsetForHoverBounds(relativeY);
        return relativeX < 0
                || relativeY < 0
                || mapContext.cachedMapWidth - (MINIMAP_INNER_MARGIN * 2) < boundsX
                || mapContext.cachedMapHeight - (MINIMAP_INNER_MARGIN * 2) < boundsY;
    }

    /**
     * Native hover-bound scaling in MinimapVisualObject::UpdateCursorForPanelHover @004ACCB4.
     * Full support port.
     */
    private int unscalePanelOffsetForHoverBounds(int scaledOffset) {
        if (zoomLevel0x68 < 0) {
            return scaledOffset;
        }
        return scaledOffset >> zoomLevel0x68;
    }

    /**
     * Native input-mode cursor branch in MinimapVisualObject::UpdateCursorForPanelHover @004ACCB4.
     * Full support port.
     */
    private static CCursor resolveMinimapInputModeCursor(int inputMode) {
        return switch (inputMode) {
            case 1, 6 -> CMousePointer.Cursor_SmallAttack;
            case 4 -> CMousePointer.Cursor_SmallDefend;
            case 5 -> CMousePointer.Cursor_SmallCast;
            case 8 -> CMousePointer.Cursor_SmallPatrol;
            default -> CMousePointer.Cursor_SmallMove;
        };
    }

    /**
     * Native helper cluster in MinimapVisualObject::OnLButtonDown @004AD00E.
     * Full support port.
     */
    private void dispatchMinimapAttackAction(MapVisualObject mapContext, Point rawTile) {
        short targetTokenId = mapContext.findObjectTokenAtCell(rawTile.x, rawTile.y);
        if (targetTokenId == 0) {
            mapContext.issueMinimapAttackCellOrder(rawTile.x, rawTile.y);
            return;
        }
        mapContext.issueMinimapAttackTargetOrder(targetTokenId);
    }

    /**
     * Native helper cluster in MinimapVisualObject::OnLButtonDown @004AD00E.
     * Full support port.
     */
    private void dispatchMinimapDefendAction(MapVisualObject mapContext, Point rawTile) {
        short targetTokenId = mapContext.findObjectTokenAtCell(rawTile.x, rawTile.y);
        if (targetTokenId != 0) {
            mapContext.issueMinimapDefendTargetOrder(targetTokenId);
        }
    }

    /**
     * Native click-to-map transform shared by MinimapVisualObject::OnLButtonDown @004AD00E and
     * MinimapVisualObject::OnRButtonDown @004AD32F.
     * Full support port.
     */
    private Point computeCenteredMapTileFromPanelPoint(MapVisualObject mapContext, int screenX, int screenY) {
        Point rawTile = computeRawMapTileFromPanelPoint(mapContext, screenX, screenY);
        return new Point(
                rawTile.x - (mapContext.gridWidth / 2),
                rawTile.y - (mapContext.gridHeight / 2)
        );
    }

    /**
     * Native click-to-map transform shared by MinimapVisualObject::OnLButtonDown @004AD00E and
     * MinimapVisualObject::OnRButtonDown @004AD32F.
     * Full support port.
     */
    private Point computeRawMapTileFromPanelPoint(MapVisualObject mapContext, int screenX, int screenY) {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        MapDescriptor mapDescriptor = mapContext.mapDescriptor;
        int offsetX = MINIMAP_CENTER_X - (scaledMapSpan(mapDescriptor.getWidth() - (MINIMAP_INNER_MARGIN * 2)) / 2);
        int offsetY = MINIMAP_CENTER_Y - (scaledMapSpan(mapDescriptor.getHeight() - (MINIMAP_INNER_MARGIN * 2)) / 2);

        int relativeX = screenX - screenRect.left - offsetX;
        int relativeY = screenY - screenRect.top - offsetY;
        int rawTileX = unscalePanelOffset(relativeX) + MINIMAP_INNER_MARGIN;
        int rawTileY = unscalePanelOffset(relativeY) + MINIMAP_INNER_MARGIN;
        return new Point(rawTileX, rawTileY);
    }

    /**
     * Native cursor-selection branch in MinimapVisualObject::OnLButtonDown @004AD00E.
     * Full support port.
     */
    private int resolveLeftClickAction() {
        CGameBitmap sourceBitmap = Globals.mousePointer.getSourceBitmap();
        if (sourceBitmap == CMousePointer.Cursor_SmallDefault.getBitmap()) {
            return CURSOR_ACTION_CAMERA;
        }
        if (sourceBitmap == CMousePointer.Cursor_SmallMove.getBitmap()) {
            return CURSOR_ACTION_MOVE;
        }
        if (sourceBitmap == CMousePointer.Cursor_SmallAttack.getBitmap()) {
            return CURSOR_ACTION_ATTACK;
        }
        if (sourceBitmap == CMousePointer.Cursor_SmallDefend.getBitmap()) {
            return CURSOR_ACTION_DEFEND;
        }
        if (sourceBitmap == CMousePointer.Cursor_SmallCast.getBitmap()) {
            return CURSOR_ACTION_CAST;
        }
        if (sourceBitmap == CMousePointer.Cursor_SmallPatrol.getBitmap()) {
            return CURSOR_ACTION_PATROL;
        }
        return CURSOR_ACTION_NONE;
    }

    /**
     * Native bitmap selection branch in MinimapVisualObject::Update @004AC414.
     * Full support port.
     */
    private CBmp64k resolveDisplayedBitmap(MapVisualObject mapContext) {
        if (mapContext.useAreaEffectMinimapBitmap()) {
            return minimapBitmapSecondary0x64;
        }
        return minimapBitmapPrimary0x60;
    }

    /**
     * Native owner: MinimapVisualObject::Update @004AC414.
     * Full support port.
     */
    private void drawCrystalBackground(CRect screenRect) {
        GUI.crystalR.draw(screenRect.left, screenRect.top, 0, null, false);
    }

    /**
     * Native owner: MinimapVisualObject::Update @004AC414.
     * Full support port.
     */
    private void drawMinimapBitmap(CRect screenRect, Point bitmapOffset, CBmp64k bitmap) {
        bitmap.draw(screenRect.left + bitmapOffset.x, screenRect.top + bitmapOffset.y, 0, null, false);
    }

    /**
     * Native owner: MinimapVisualObject::Update @004AC414.
     * Full support port.
     */
    private void drawViewportRect(CRect screenRect, MapVisualObject mapContext, Point bitmapOffset) {
        int left = screenRect.left + bitmapOffset.x + scalePanelOffset(mapContext.view.x - MINIMAP_INNER_MARGIN);
        int top = screenRect.top + bitmapOffset.y + scalePanelOffset(mapContext.view.y - MINIMAP_INNER_MARGIN);
        int right = left - 1 + scalePanelSpan(mapContext.gridWidth);
        int bottom = top - 1 + scalePanelSpan(mapContext.gridHeight);
        Globals.renderer.drawRect(left, top, right, bottom, (short) 0xFFFF);
    }

    /**
     * Native support extracted from MinimapVisualObject::RebuildMinimapBitmaps @004AB96C.
     */
    private CBmp64k buildMinimapTerrainBitmap(MapDescriptor mapDescriptor) {
        int bitmapWidth = mapDescriptor.getWidth() - (MINIMAP_INNER_MARGIN * 2);
        int bitmapHeight = mapDescriptor.getHeight() - (MINIMAP_INNER_MARGIN * 2);
        CBmp64k terrainBitmap = new CBmp64k(bitmapWidth, bitmapHeight);
        RGB16[] targetPixels = terrainBitmap.surface.pixels();
        RGB16[] sourcePixels = GUI.miniMapData.surface.pixels();
        int sourceWidth = GUI.miniMapData.surface.width();
        int sourceHeight = GUI.miniMapData.surface.height();

        for (int mapY = MINIMAP_INNER_MARGIN; mapY < mapDescriptor.getHeight() - MINIMAP_INNER_MARGIN; mapY++) {
            for (int mapX = MINIMAP_INNER_MARGIN; mapX < mapDescriptor.getWidth() - MINIMAP_INNER_MARGIN; mapX++) {
                int tileWord = mapDescriptor.tileWordAt(mapX, mapY);
                int sourceX = (tileWord & MINIMAP_TILE_MASK) >> 4;
                int sourceY = (sourceHeight - 1) - (tileWord & MINIMAP_TILE_SOURCE_Y_MASK);
                int targetIndex = (mapX - MINIMAP_INNER_MARGIN)
                        + (mapY - MINIMAP_INNER_MARGIN) * bitmapWidth;
                targetPixels[targetIndex] = sourcePixels[sourceX + sourceY * sourceWidth];
            }
        }
        return terrainBitmap;
    }

    /**
     * Native support extracted from MinimapVisualObject::RebuildMinimapBitmaps @004AB96C.
     */
    private void copyTerrainToPrimaryMinimapBitmap(CBmp64k terrainBitmap) {
        RGB16[] sourcePixels = terrainBitmap.surface.pixels();
        RGB16[] targetPixels = minimapBitmapPrimary0x60.surface.pixels();
        int sourceWidth = terrainBitmap.surface.width();
        int sourceHeight = terrainBitmap.surface.height();
        int targetWidth = minimapBitmapPrimary0x60.surface.width();
        int targetHeight = minimapBitmapPrimary0x60.surface.height();

        if (zoomLevel0x68 < 0) {
            for (int targetY = 0; targetY < targetHeight; targetY++) {
                int sourceY0 = ((targetHeight - targetY) * 2) - 2;
                int sourceY1 = sourceY0 + 1;
                for (int targetX = 0; targetX < targetWidth; targetX++) {
                    int sourceX0 = targetX * 2;
                    int sourceIndex = sourceX0 + sourceY0 * sourceWidth;
                    targetPixels[targetX + targetY * targetWidth] = quarterAverageRgb16(
                            sourcePixels[sourceIndex],
                            sourcePixels[sourceIndex + 1],
                            sourcePixels[sourceX0 + sourceY1 * sourceWidth],
                            sourcePixels[sourceX0 + 1 + sourceY1 * sourceWidth]
                    );
                }
            }
            return;
        }

        for (int targetY = 0; targetY < targetHeight; targetY++) {
            int sourceY = (sourceHeight - 1) - (targetY >> zoomLevel0x68);
            for (int targetX = 0; targetX < targetWidth; targetX++) {
                int sourceX = targetX >> zoomLevel0x68;
                targetPixels[targetX + targetY * targetWidth] = sourcePixels[sourceX + sourceY * sourceWidth];
            }
        }
    }

    /**
     * Native support extracted from MinimapVisualObject::RebuildMinimapBitmaps @004AB96C.
     */
    private void applyTerrainLightToPrimaryMinimapBitmap(MapDescriptor mapDescriptor) {
        int effectiveZoom = zoomLevel0x68;
        int mapStep = 1;
        if (effectiveZoom < 0) {
            effectiveZoom = 0;
            mapStep = 2;
        }

        int blockSize = 1 << effectiveZoom;
        int bitmapWidth = minimapBitmapPrimary0x60.surface.width();
        int bitmapHeight = minimapBitmapPrimary0x60.surface.height();
        RGB16[] pixels = minimapBitmapPrimary0x60.surface.pixels();
        byte[] terrainLight = mapDescriptor.getTerrainLightWxH();

        int bitmapX = 0;
        for (int mapX = MINIMAP_INNER_MARGIN; mapX < mapDescriptor.getWidth() - MINIMAP_INNER_MARGIN; mapX += mapStep) {
            int bitmapY = 0;
            for (int mapY = MINIMAP_INNER_MARGIN; mapY < mapDescriptor.getHeight() - MINIMAP_INNER_MARGIN; mapY += mapStep) {
                int lightIndex = mapDescriptor.tileIndex(mapX, mapY);
                int averageLight = (Byte.toUnsignedInt(terrainLight[lightIndex])
                        + Byte.toUnsignedInt(terrainLight[lightIndex + 1])
                        + Byte.toUnsignedInt(terrainLight[lightIndex + mapDescriptor.getWidth()])
                        + Byte.toUnsignedInt(terrainLight[lightIndex + mapDescriptor.getWidth() + 1])) >> 2;
                int sourceIndex = bitmapX + (bitmapHeight - 1 - bitmapY) * bitmapWidth;
                RGB16 shadedPixel = shadeTerrainPixel(pixels[sourceIndex], TERRAIN_LIGHT_BASE - averageLight);
                fillMinimapBlock(pixels, bitmapWidth, bitmapHeight, bitmapX, bitmapY, blockSize, shadedPixel);
                bitmapY += blockSize;
            }
            bitmapX += blockSize;
        }
    }

    /**
     * Native support extracted from MinimapVisualObject::Update @004AC414.
     */
    private void refreshSecondaryMinimapBitmap(MapVisualObject mapContext, MapDescriptor mapDescriptor, Point bitmapOffset) {
        RGB16[] primaryPixels = minimapBitmapPrimary0x60.surface.pixels();
        RGB16[] secondaryPixels = minimapBitmapSecondary0x64.surface.pixels();
        System.arraycopy(primaryPixels, 0, secondaryPixels, 0, primaryPixels.length);

        int effectiveZoom = zoomLevel0x68;
        int mapStep = 1;
        if (effectiveZoom < 0) {
            effectiveZoom = 0;
            mapStep = 2;
        }

        int blockSize = 1 << effectiveZoom;
        int bitmapWidth = minimapBitmapSecondary0x64.surface.width();
        int bitmapHeight = minimapBitmapSecondary0x64.surface.height();
        int crystalWidth = GUI.crystalR.surface.width();
        int crystalHeight = GUI.crystalR.surface.height();
        RGB16[] crystalPixels = GUI.crystalR.surface.pixels();
        short[] tileFlags = mapDescriptor.getTilesWxH();

        int bitmapY = 0;
        for (int mapY = MINIMAP_INNER_MARGIN; mapY < mapDescriptor.getHeight() - MINIMAP_INNER_MARGIN; mapY += mapStep) {
            int bitmapX = 0;
            for (int mapX = MINIMAP_INNER_MARGIN; mapX < mapDescriptor.getWidth() - MINIMAP_INNER_MARGIN; mapX += mapStep) {
                int occupancyMask = tileOccupancyMask(mapDescriptor, tileFlags, mapX, mapY);
                if (occupancyMask == 0 || occupancyMask == TILE_PARTIAL_OCCUPANCY_MASK) {
                    applyCrystalCacheBlock(
                            primaryPixels,
                            secondaryPixels,
                            crystalPixels,
                            bitmapWidth,
                            bitmapHeight,
                            crystalWidth,
                            crystalHeight,
                            bitmapOffset,
                            bitmapX,
                            bitmapY,
                            blockSize,
                            occupancyMask == TILE_PARTIAL_OCCUPANCY_MASK
                    );
                }
                bitmapX += blockSize;
            }
            bitmapY += blockSize;
        }

        minimapBitmapSecondary0x64.syncFrameBytesFromSurface();
        mapContext.mapOccupancyDirty = 0;
    }

    /**
     * Native support extracted from MinimapVisualObject::Update @004AC414.
     */
    private void applyCrystalCacheBlock(
            RGB16[] primaryPixels,
            RGB16[] secondaryPixels,
            RGB16[] crystalPixels,
            int bitmapWidth,
            int bitmapHeight,
            int crystalWidth,
            int crystalHeight,
            Point bitmapOffset,
            int bitmapX,
            int bitmapY,
            int blockSize,
            boolean halfBlend
    ) {
        for (int dy = 0; dy < blockSize; dy++) {
            int bitmapRow = bitmapHeight - 1 - bitmapY - dy;
            int crystalRow = crystalHeight - 1 - bitmapOffset.y - bitmapY - dy;
            for (int dx = 0; dx < blockSize; dx++) {
                int bitmapIndex = bitmapX + dx + bitmapRow * bitmapWidth;
                RGB16 crystalPixel = crystalPixels[bitmapOffset.x + bitmapX + dx + crystalRow * crystalWidth];
                secondaryPixels[bitmapIndex] = halfBlend
                        ? halfBlendRgb16(crystalPixel, primaryPixels[bitmapIndex])
                        : crystalPixel;
            }
        }
    }

    /**
     * Native support extracted from MinimapVisualObject::Update @004AC414.
     */
    private static int tileOccupancyMask(MapDescriptor mapDescriptor, short[] tileFlags, int mapX, int mapY) {
        int tileIndex = mapDescriptor.tileIndex(mapX, mapY);
        int mapWidth = mapDescriptor.getWidth();
        return (Short.toUnsignedInt(tileFlags[tileIndex]) & TILE_OCCUPANCY_MASK)
                | (Short.toUnsignedInt(tileFlags[tileIndex + 1]) & TILE_OCCUPANCY_MASK)
                | (Short.toUnsignedInt(tileFlags[tileIndex + mapWidth]) & TILE_OCCUPANCY_MASK)
                | (Short.toUnsignedInt(tileFlags[tileIndex + mapWidth + 1]) & TILE_OCCUPANCY_MASK);
    }

    /**
     * Native support extracted from MinimapVisualObject::RebuildMinimapBitmaps @004AB96C.
     */
    private static void fillMinimapBlock(
            RGB16[] pixels,
            int bitmapWidth,
            int bitmapHeight,
            int bitmapX,
            int bitmapY,
            int blockSize,
            RGB16 pixel
    ) {
        for (int dy = 0; dy < blockSize; dy++) {
            int rowIndex = bitmapHeight - 1 - bitmapY - dy;
            int pixelIndex = bitmapX + rowIndex * bitmapWidth;
            for (int dx = 0; dx < blockSize; dx++) {
                pixels[pixelIndex + dx] = pixel;
            }
        }
    }

    /**
     * Native support extracted from MinimapVisualObject::Update @004AC414.
     */
    private Point computeMinimapBitmapOffset(MapDescriptor mapDescriptor) {
        int rawWidth = mapDescriptor.getWidth() - (MINIMAP_INNER_MARGIN * 2);
        int rawHeight = mapDescriptor.getHeight() - (MINIMAP_INNER_MARGIN * 2);
        if (zoomLevel0x68 < 0) {
            return new Point(MINIMAP_CENTER_X - (rawWidth >> 2), MINIMAP_CENTER_Y - (rawHeight >> 2));
        }
        return new Point(
                MINIMAP_CENTER_X - ((rawWidth << zoomLevel0x68) >> 1),
                MINIMAP_CENTER_Y - ((rawHeight << zoomLevel0x68) >> 1)
        );
    }

    /**
     * Native support extracted from MinimapVisualObject::RebuildMinimapBitmaps @004AB96C.
     */
    private static RGB16 quarterAverageRgb16(RGB16 a, RGB16 b, RGB16 c, RGB16 d) {
        int averaged = ((Short.toUnsignedInt(a.val()) >> 2) & RGB16_QUARTER_BLEND_MASK)
                + ((Short.toUnsignedInt(b.val()) >> 2) & RGB16_QUARTER_BLEND_MASK)
                + ((Short.toUnsignedInt(c.val()) >> 2) & RGB16_QUARTER_BLEND_MASK)
                + ((Short.toUnsignedInt(d.val()) >> 2) & RGB16_QUARTER_BLEND_MASK);
        return RGB16.of(averaged);
    }

    /**
     * Native support extracted from MinimapVisualObject::Update @004AC414.
     */
    private static RGB16 halfBlendRgb16(RGB16 a, RGB16 b) {
        int blended = ((Short.toUnsignedInt(a.val()) >> 1) & RGB16_HALF_BLEND_MASK)
                + ((Short.toUnsignedInt(b.val()) >> 1) & RGB16_HALF_BLEND_MASK);
        return RGB16.of(blended);
    }

    /**
     * Native support extracted from MinimapVisualObject::RebuildMinimapBitmaps @004AB96C.
     */
    private static RGB16 shadeTerrainPixel(RGB16 pixel, int brightnessFactor) {
        int packed = Short.toUnsignedInt(pixel.val());
        int red = Math.min((((packed >> 11) & 0x1F) * brightnessFactor >> 5) << 3, 0xFF);
        int green = Math.min((((packed >> 5) & 0x3F) * brightnessFactor >> 5) << 2, 0xFF);
        int blue = Math.min(((packed & 0x1F) * brightnessFactor >> 5) << 3, 0xFF);
        return RGB16.of(((red >> 3) << 11) | ((green >> 2) << 5) | (blue >> 3));
    }

    /**
     * Native zoom-selection branch in MinimapVisualObject::RebuildMinimapBitmaps @004AB96C.
     */
    private static int computeZoomLevel(int mapWidth, int mapHeight) {
        int dominantSpan = Math.max(mapWidth, mapHeight);
        if (dominantSpan < 0x31) {
            return 2;
        }
        if (dominantSpan < 0x51) {
            return 1;
        }
        if (dominantSpan < 0x91) {
            return 0;
        }
        return -1;
    }

    /**
     * Native size-scaling branch in MinimapVisualObject::RebuildMinimapBitmaps @004AB96C.
     */
    private int scaledMapSpan(int rawSpan) {
        if (zoomLevel0x68 < 0) {
            return rawSpan / 2;
        }
        return rawSpan << zoomLevel0x68;
    }

    /**
     * Native coordinate-scaling branch in MinimapVisualObject::Update/@004AC414 and input handlers.
     */
    private int scalePanelOffset(int rawOffset) {
        if (zoomLevel0x68 < 0) {
            return rawOffset / 2;
        }
        return rawOffset << zoomLevel0x68;
    }

    /**
     * Native size-scaling branch in MinimapVisualObject::Update/@004AC414.
     */
    private int scalePanelSpan(int rawSpan) {
        if (zoomLevel0x68 < 0) {
            return rawSpan / 2;
        }
        return rawSpan << zoomLevel0x68;
    }

    /**
     * Native inverse scaling used by MinimapVisualObject::OnLButtonDown/@004AD00E and ::OnRButtonDown/@004AD32F.
     */
    private int unscalePanelOffset(int scaledOffset) {
        if (zoomLevel0x68 < 0) {
            return scaledOffset * 2;
        }
        return scaledOffset >> zoomLevel0x68;
    }

    /**
     * Native support for MinimapVisualObject map-context field access in Update @004AC414,
     * OnLButtonDown @004AD00E, OnRButtonDown @004AD32F, and OnMessage @004AB896.
     */
    private MapVisualObject resolveMapContext() {
        return mapContext0x5c;
    }

    /**
     * Native tail of MinimapVisualObject::OnLButtonUp @004AD506.
     * Full support port.
     */
    private void restoreCursorAfterUiLockClick() {
        CMousePointer.Cursor_Default.setToMousePointer();
        Globals.mainWindow.clearUiLockState();
    }
}
