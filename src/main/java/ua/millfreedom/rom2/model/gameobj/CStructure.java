package ua.millfreedom.rom2.model.gameobj;

import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CGameBitmap;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.CSprite256;
import ua.millfreedom.rom2.model.StructureDef;
import ua.millfreedom.rom2.model.Structures;
import ua.millfreedom.rom2.model.color.RGB16;
import ua.millfreedom.rom2.model.palette.CGamePalette;
import ua.millfreedom.rom2.model.palette.Palettes;

import java.util.Objects;

public class CStructure extends CGameObject {
    private static final int TILE_OCCUPIED_MASK = 0xC000;
    private static final int TILE_PARTIALLY_OCCUPIED_MASK = 0x8000;
    private static final int STRUCTURE_MAP_LAYER = 1;
    private static final int MINIMAP_BORDER_TILES = 8;
    private static final int MINIMAP_OWNER_PALETTE_PAGE = 8;
    private static final int MINIMAP_OWNER_COLOR_INDEX = 0xA4;
    private static final int LIGHT_BASE_INTENSITY = 0x20;

    /**
     * Native: CStructure::CStructure @00461403.
     * Fully ported.
     */
    public CStructure() {
    }

    /**
     * Native: CStructure::CStructure @00461422.
     * Fully ported. Native delegates to CGameObject copy construction and installs the CStructure vtable.
     */
    public CStructure(CStructure source) {
        super(source);
    }

    /**
     * vtbl +0x10: CStructure::Dump @004620E2.
     * Full port. Native writes the `CStructure` class label into CDumpContext.
     */
    @Override
    public String dump() {
        return "CStructure";
    }

    /**
     * vtbl +0x20: CStructure::GetTileWidth @004620FC.
     * Full port. Native returns `g_StructureDefs[type].tileWidth`.
     */
    @Override
    public int getTileWidth() {
        return getStructureDef().tileWidth;
    }

    /**
     * vtbl +0x24: CStructure::GetTileHeight @0046211D.
     * Full port. Native returns `g_StructureDefs[type].tileHeight`.
     */
    @Override
    public int getTileHeight() {
        return getStructureDef().tileHeight;
    }

    /**
     * vtbl +0x28: CStructure::Draw @00461649.
     * Full port. Native row/frame selection, destroyed-frame routing, animation-mask remapping, owner palette page,
     * and smoothing overlay are mapped through CSprite256 draw primitives.
     */
    @Override
    public void draw(int viewTileX, int viewTileY, int palettePage) {
        StructureDef def = getStructureDef();
        CGamePalette mainPalette = getMainPalette(def);
        int firstStructureRow = viewTileY + pMapVisualObject.view.y - tileY;
        int rowEnd = firstStructureRow + def.fullHeight - def.tileHeight;
        int rowStart = firstStructureRow == 0 ? 0 : rowEnd;
        int drawY = (viewTileY << 5) - z - terrainHeightOffset;
        for (int row = rowEnd; rowStart <= row; row--) {
            int frameGridIndex = viewTileX + pMapVisualObject.view.x - tileX + row * def.tileWidth;
            int frameIndex = resolveStructureFrameIndex(def, frameGridIndex, HP < 1 && def.indestructible == 0);
            CSprite256 mainSprite = def.getSpriteMain();
            mainSprite.draw(viewTileX << 5, drawY, frameIndex, palettePage, false);
            if (Globals.gamePreferences.smoothing != 0) {
                def.getSpriteSecondary().drawFrameClippedY(viewTileX << 5, drawY, frameIndex, palettePage, mainPalette, false);
            }
            drawY -= 0x20;
        }
    }

    /**
     * vtbl +0x2C: CStructure::DrawShadow @00461890.
     * Native row/frame selection, terrain shadow slope, and smoothing overlay are mapped through
     * CSprite256::DrawWithRenderEffect for real registry ShadowY values. Java culls registry sentinel-scale ShadowY
     * values so smoothed global lighting does not reveal shadows that native's snap/original viewport kept off-screen.
     */
    @Override
    public void drawShadow(int viewTileX, int viewTileY) {
        StructureDef def = getStructureDef();
        if (def.hasSentinelShadowY()) {
            return;
        }
        int shadowSlope = resolveShadowSlope();
        double shadowAngle = Math.tan(pMapVisualObject.mapDescriptor.getShadowAngle());
        int firstStructureRow = viewTileY + pMapVisualObject.view.y - tileY;
        int rowEnd = firstStructureRow + def.fullHeight - def.tileHeight;
        int rowStart = firstStructureRow == 0 ? 0 : rowEnd;
        int drawY = (viewTileY << 5) - z - terrainHeightOffset;
        for (int row = rowEnd; rowStart <= row; row--) {
            int frameGridIndex = viewTileX + pMapVisualObject.view.x - tileX + row * def.tileWidth;
            int frameIndex = resolveStructureFrameIndex(def, frameGridIndex, HP < 1);
            int shadowXOffset = (int) (shadowAngle * ((def.fullHeight - row) * 32.0 - def.shadowY));
            int drawX = (viewTileX << 5) + shadowXOffset;
            def.getSpriteMain().drawWithRenderEffect(
                    drawX,
                    drawY,
                    frameIndex,
                    Globals.lighting.shadowLength,
                    shadowSlope,
                    false
            );
            if (Globals.gamePreferences.smoothing != 0) {
                def.getSpriteSecondary().drawWithRenderEffect(
                        drawX,
                        drawY,
                        frameIndex,
                        Globals.lighting.lightHeight,
                        shadowSlope,
                        false
                );
            }
            drawY -= 0x20;
        }
    }

    /**
     * vtbl +0x30: CStructure::DrawSelectionOverlay @00461B2B.
     * Full port. Native selection geometry, corner markers, signed HP bar thresholds, and show-all-HP rendering are
     * mapped; MapVisualObject enforces the native tile-parameter top-left guard before dispatch.
     */
    @Override
    public void drawSelectionOverlay() {
        StructureDef def = getStructureDef();
        if (def.indestructible != 0) {
            return;
        }

        int left = (tileX - pMapVisualObject.view.x) * 0x20 + def.selection.left;
        int right = (tileX - pMapVisualObject.view.x) * 0x20 + def.selection.right;
        int top = ((tileY - pMapVisualObject.view.y + def.tileHeight - def.fullHeight) * 0x20
                + def.selection.top) - terrainHeightOffset;

        GUI.ball.drawRectMasked(left, top - 2, 0, 0, 4, 4);
        GUI.ball.drawRectMasked(right - 4, top - 2, 0, 0, 4, 4);

        int maxHp = MaxHP;
        int hp = HP;
        int fillWidth = (hp * (right - left - 8)) / maxHp;
        if (fillWidth == 0 && HP != 0) {
            fillWidth = 1;
        }
        drawStructureHealthBar(left, right, top, fillWidth, hp, maxHp);
    }

    /**
     * vtbl +0x34: CStructure::DrawMinimap @0046213E.
     * Full port. Native fills the structure footprint on the minimap only when the current map tile is fully occupied.
     */
    @Override
    public void drawMinimap(int minimapLeft, int minimapTop, int zoomShift) {
        if (!isCurrentTileFullyOccupied()) {
            return;
        }

        int markerX;
        int markerY;
        int markerWidth = getTileWidth();
        int markerHeight = getTileHeight();
        if (zoomShift < 0) {
            markerX = ((location.x >> 8) - MINIMAP_BORDER_TILES) >> 1;
            markerY = ((location.y >> 8) - MINIMAP_BORDER_TILES) >> 1;
        } else {
            int shift = zoomShift & 0x1F;
            markerX = ((location.x >> 8) - MINIMAP_BORDER_TILES) << shift;
            markerY = ((location.y >> 8) - MINIMAP_BORDER_TILES) << shift;
            markerWidth <<= shift;
            markerHeight <<= shift;
        }

        int left = minimapLeft + markerX;
        int top = minimapTop + markerY;
        Globals.renderer.fillScreenRect(left, top, left + markerWidth, top + markerHeight, resolveMinimapColor());
    }

    /**
     * vtbl +0x38: CStructure::UpdateMapLayer @00461447.
     * Full port. Native writes this structure across its visible footprint in map layer `1`.
     */
    @Override
    public void updateMapLayer() {
        markObjectLayerCell(STRUCTURE_MAP_LAYER, this);
    }

    /**
     * vtbl +0x3C: CStructure::AdvanceMapObjectState @00462333.
     * Full port. Native advances the animated structure phase when animation data exists and `bIsBlocked == 0`.
     */
    @Override
    public boolean advanceMapObjectState() {
        StructureDef def = getStructureDef();
        if (def.animationDataCount != 0 && bIsBlocked == 0) {
            phase = (phase + 1) % def.animationDataCount;
        }
        return true;
    }

    /**
     * vtbl +0x40: CStructure::UpdateMapOverlay @00462386.
     * Full port. Native writes pulsing light intensity into the dynamic-light override grid and unions the dirty
     * render rect for the affected map bounds.
     */
    @Override
    public void updateMapOverlay() {
        if (Globals.gamePreferences.lighting == 0) {
            return;
        }

        StructureDef def = getStructureDef();
        if (def.lightRadius == 0 || HP <= 0) {
            return;
        }

        int lightIntensity = resolveStructureLightIntensity(def, pMapVisualObject.mapAnimationTick);
        applyStructureLightIntensity(def, lightIntensity);
    }

    /**
     * vtbl +0x4C: CStructure::UpdateBlockedState @00461460.
     * Full port. Native scans the structure footprint against map tile occupancy masks and clears selection when the
     * structure transitions into a blocked-but-selected state.
     */
    @Override
    public boolean updateBlockedState() {
        if (pMapVisualObject.isOwnerVisible(cPlayer.playerId)) {
            bIsBlocked = 0;
            return false;
        }

        StructureDef def = getStructureDef();
        short[] tileFlags = pMapVisualObject.getOccupancyTileFlags();
        int mapWidth = pMapVisualObject.getOccupancyMapWidth();
        bIsBlocked = 2;
        boolean foundFullyOccupied = false;
        for (int x = 0; x < getTileWidth(); x++) {
            for (int y = getTileHeight() - def.fullHeight; y < getTileHeight(); y++) {
                int structureTileX = tileX + x;
                int structureTileY = tileY + y;
                int tileIndex = pMapVisualObject.mapDescriptor.tileIndex(structureTileX, structureTileY);
                int occupiedMask = (tileFlags[tileIndex] & TILE_OCCUPIED_MASK)
                        | (tileFlags[tileIndex + 1] & TILE_OCCUPIED_MASK)
                        | (tileFlags[tileIndex + mapWidth] & TILE_OCCUPIED_MASK)
                        | (tileFlags[tileIndex + mapWidth + 1] & TILE_OCCUPIED_MASK);
                if (occupiedMask == TILE_PARTIALLY_OCCUPIED_MASK) {
                    bIsBlocked = 1;
                }
                if (occupiedMask == TILE_OCCUPIED_MASK) {
                    bIsBlocked = 0;
                    foundFullyOccupied = true;
                    break;
                }
            }
            if (foundFullyOccupied) {
                break;
            }
        }
        if (bIsBlocked == 0 || !m_bSelected) {
            return false;
        }

        m_bSelected = false;
        m_bSelectionDirty = 1;
        return true;
    }

    /**
     * Native support extracted from CStructure vtable methods that read `g_StructureDefs[type]`:
     * CStructure::GetTileWidth @004620FC, CStructure::GetTileHeight @0046211D, CStructure::Draw @00461649,
     * CStructure::DrawShadow @00461890, CStructure::DrawSelectionOverlay @00461B2B,
     * CStructure::DrawMinimap @0046213E, CStructure::AdvanceMapObjectState @00462333,
     * CStructure::UpdateMapOverlay @00462386, and CStructure::UpdateBlockedState @00461460.
     */
    protected StructureDef getStructureDef() {
        return Objects.requireNonNull(Structures.getStructureDef(type), "Missing StructureDef for id " + type);
    }

    /**
     * Native support extracted from CStructure::Draw @00461649, CVerticalWoodenBridge::Draw @00462654,
     * and CHorisontalWoodenBridge::Draw @0046283C.
     */
    protected static CGamePalette getMainPalette(StructureDef def) {
        CGameBitmap sprite = def.getSpriteMain();
        return sprite.palette;
    }

    /**
     * Native support extracted from CStructure::Draw @00461649 and CStructure::DrawShadow @00461890.
     */
    private int resolveStructureFrameIndex(StructureDef def, int frameGridIndex, boolean useDestroyedFrame) {
        if (useDestroyedFrame) {
            return frameGridIndex + def.getSpriteMain().frameCount - def.tileWidth * def.fullHeight;
        }

        int animationFrame = 0;
        if (def.animationDataCount != 0) {
            animationFrame = def.animationData.get(phase);
        }
        if (Globals.gamePreferences.animation == 0 || animationFrame == 0) {
            return frameGridIndex;
        }

        if (def.animationMask.charAt(frameGridIndex) == '-') {
            return frameGridIndex;
        }

        int frameIndex = def.tileWidth * def.fullHeight + (animationFrame - 1) * def.animationMaskSolidCount;
        for (int i = 0; i < frameGridIndex; i++) {
            if (def.animationMask.charAt(i) != '-') {
                frameIndex++;
            }
        }
        return frameIndex;
    }

    /**
     * Native support extracted from CStructure::DrawSelectionOverlay @00461B2B and
     * CUnit::DrawSelectionHighlight @00460DB1.
     */
    private void drawStructureHealthBar(int left, int right, int top, int fillWidth, int hp, int maxHp) {
        short lightColor;
        short middleColor;
        short darkColor;
        if (hp < (maxHp + ((maxHp >> 31) & 3)) >> 2) {
            lightColor = RGB16.from(0xFF, 0, 0).val();
            middleColor = RGB16.from(0xC0, 0, 0).val();
            darkColor = RGB16.from(0x80, 0, 0).val();
        } else if (hp < maxHp / 2) {
            lightColor = RGB16.from(0xFF, 0xFF, 0).val();
            middleColor = RGB16.from(0xC0, 0xC0, 0).val();
            darkColor = RGB16.from(0x80, 0x80, 0).val();
        } else {
            lightColor = RGB16.from(0, 0xFF, 0).val();
            middleColor = RGB16.from(0, 0xC0, 0).val();
            darkColor = RGB16.from(0, 0x80, 0).val();
        }

        int barLeft = left + 4;
        int fillRight = barLeft + fillWidth;
        if (pMapVisualObject.showHitPointBars != 0 && !isSelected()) {
            lightColor = CUnit.dimSelectionBarColorForShowAll(lightColor);
            middleColor = CUnit.dimSelectionBarColorForShowAll(middleColor);
            darkColor = CUnit.dimSelectionBarColorForShowAll(darkColor);
            Globals.renderer.applyShadeToRect(barLeft, top - 2, fillRight, top + 2, 8);
            Globals.renderer.addColorToRect(barLeft, top - 2, fillRight, top - 1, darkColor);
            Globals.renderer.addColorToRect(barLeft, top - 1, fillRight, top, lightColor);
            Globals.renderer.addColorToRect(barLeft, top, fillRight, top + 1, middleColor);
            Globals.renderer.addColorToRect(barLeft, top + 1, fillRight, top + 2, darkColor);
            return;
        }

        int barRight = right - 4;
        Globals.renderer.fillScreenRect(barLeft, top - 2, barRight, top + 2, RGB16.from(0x40, 0x40, 0x40).val());
        Globals.renderer.fillScreenRect(barLeft, top - 1, barRight, top, RGB16.from(0x80, 0x80, 0x80).val());
        Globals.renderer.fillScreenRect(barLeft, top, barRight, top + 1, RGB16.from(0x60, 0x60, 0x60).val());
        Globals.renderer.fillScreenRect(barLeft, top - 2, fillRight, top + 2, darkColor);
        Globals.renderer.fillScreenRect(barLeft, top - 1, fillRight, top, lightColor);
        Globals.renderer.fillScreenRect(barLeft, top, fillRight, top + 1, middleColor);
    }

    /**
     * Native support extracted from CStructure::DrawMinimap @0046213E.
     */
    private boolean isCurrentTileFullyOccupied() {
        short[] tileFlags = pMapVisualObject.getOccupancyTileFlags();
        int tileX = location.x >> 8;
        int tileY = location.y >> 8;
        int tileIndex = pMapVisualObject.mapDescriptor.tileIndex(tileX, tileY);
        int mapWidth = pMapVisualObject.getOccupancyMapWidth();
        int occupiedMask = (tileFlags[tileIndex] & TILE_OCCUPIED_MASK)
                | (tileFlags[tileIndex + 1] & TILE_OCCUPIED_MASK)
                | (tileFlags[tileIndex + mapWidth] & TILE_OCCUPIED_MASK)
                | (tileFlags[tileIndex + mapWidth + 1] & TILE_OCCUPIED_MASK);
        return occupiedMask == TILE_OCCUPIED_MASK;
    }

    /**
     * Native support extracted from CStructure::DrawMinimap @0046213E.
     */
    private short resolveMinimapColor() {
        return Palettes.unitGamePalettes.get(cPlayer.color)
                .paletteData[MINIMAP_OWNER_PALETTE_PAGE]
                .data()[MINIMAP_OWNER_COLOR_INDEX]
                .val();
    }

    /**
     * Native support extracted from CStructure::UpdateMapOverlay @00462386.
     */
    private static int resolveStructureLightIntensity(StructureDef def, int tick) {
        if (def.lightPulse == 0) {
            return LIGHT_BASE_INTENSITY;
        }

        int period = def.lightPulse << 1;
        int pulseOffset = tick % period;
        if (pulseOffset < def.lightPulse) {
            return LIGHT_BASE_INTENSITY - def.lightPulse / 2 + tick % def.lightPulse;
        }
        return LIGHT_BASE_INTENSITY + def.lightPulse / 2 - tick % def.lightPulse;
    }

    /**
     * Native support extracted from CStructure::UpdateMapOverlay @00462386.
     */
    private void applyStructureLightIntensity(StructureDef def, int lightIntensity) {
        byte brightness = (byte) lightIntensity;
        for (int x = mapBoundsLeft - 1 + def.lightRadius; x <= mapBoundsRight + def.lightRadius; x++) {
            for (int y = mapBoundsTop - 1 + def.lightRadius; y <= mapBoundsBottom + def.lightRadius; y++) {
                if (x > -4 && x < pMapVisualObject.gridWidth + 3 && y > -4 && y < pMapVisualObject.gridHeight + 3) {
                    pMapVisualObject.dynamicLightOverrideGrid[x + 3][y + 3] = brightness;
                }
            }
        }
        pMapVisualObject.dynamicLightCellCount++;
        pMapVisualObject.dirtyRenderRect.unionWith(new CRect(
                (mapBoundsLeft - 2) * 0x20,
                (mapBoundsTop - 2) * 0x20,
                (mapBoundsRight + 3) * 0x20,
                (mapBoundsBottom + 3) * 0x20
        ));
    }
}
