package ua.millfreedom.rom2.model.gameobj;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CPlayer;
import ua.millfreedom.rom2.model.TokenEntry;
import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.model.sound.Sound;
import ua.millfreedom.rom2.model.sound.SoundSystem;
import ua.millfreedom.rom2.model.spell.VisualElem;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;

public abstract class CGameObject implements MfcSerializable {
    private static final int MAP_READY_STATE = 0x10;
    private static final int MAP_BLOCKED_MASK = 0xC000;
    private static final int OCCUPANCY_GRID_SIZE = 0x29;
    private static final int OCCUPANCY_GRID_CENTER = 0x14;

    // Offsets below are expressed in the owning main-object layout, not the *_Base layout.

    //0x04
    public int m_id;

    //0x08
    public final Point location = new Point();

    //0x10
    public int z;

    //0x14
    public CPlayer cPlayer;

    //0x18
    public int availableSpellMask;

    //0x1C
    public int spellbookMask;

    //0x20
    public int activeSpellEffectMask;

    //0x24
    public int type;

    //0x28
    public int field8_0x28;

    //0x2C
    public final Point location2 = new Point();

    //0x34
    public int field10_0x34;

    //0x38
    public int tileX;

    //0x3C
    public int tileY;

    //0x40
    public int mapBoundsLeft;

    //0x44
    public int mapBoundsTop;

    //0x48
    public int mapBoundsRight;

    //0x4C
    public int mapBoundsBottom;

    //0x50
    public int mapLayerActive;

    //0x54
    public int screenX;

    //0x58
    public int screenY;

    //0x5C
    public int centerWorldX8;

    //0x60
    public int centerWorldY8;

    //0x64
    public int centerScreenX;

    //0x68
    public int centerScreenY;

    //0x6C
    public int terrainHeightOffset;

    //0x70
    public int dir;

    //0x74
    public int phase;

    //0x78
    public int lastAction;

    //0x7C
    public int bIsBlocked;

    //0x80
    public boolean m_bSelected;

    //0x84
    public int field30_0x84;

    //0x88
    public int controlGroupMask;

    //0x8C
    public byte action;

    //0x8D
    public byte actionDir;

    //0x8E
    public short actionTarget;

    //0x90
    public int actionX;

    //0x94
    public int actionY;

    //0x98
    public int actionZ;

    //0x9C
    public int actionPhase;

    //0xA0
    public int field39_0xa0;

    //0xA4
    public int field40_0xa4;

    //0xA8
    public int actionSegments;

    //0xAC
    public int actionSpell;

    //0xB0
    public final CustomList<Short> actionTargets = CustomList.std(Short.class);

    //0xC4
    public int field44_0xc4;

    //0xC8
    public final Point occupiedLocation = new Point();

    //0xD0
    public final List<TokenEntry> tokenEntries = new ArrayList<>();

    //0xE4
    public final int[] shopInventoryVisibleStart = new int[]{0};

    //0xE8
    public MapVisualObject pMapVisualObject;

    //0xEC
    public String name = "";

    //0xF8
    public String clan = "";

    //0x104
    public short HP;

    //0x106
    public short field52_0x106;

    //0x108
    public short MaxHP;

    //0x10A
    public short packedSightRange;

    //0x10C
    public short speed;

    //0x10E
    public short copiedEncumbranceWeight;

    //0x110
    public int experience;

    //0x114
    public int m_bSelectionDirty;

    //0x118
    public byte autoCastSpellId;

    //0x11C
    public final List<VisualElem> transientVisualElements = new ArrayList<>();

    //0x130
    public final CustomList<Integer> dwarr_130 = CustomList.std(Integer.class);

    /**
     * Native: CGameObject::CGameObject @0045FA00.
     * Full port. Java field initializers cover native CObject/MFC array construction and zeroed scalar fields.
     */
    protected CGameObject() {
        field30_0x84 = -1;
        m_bSelectionDirty = 1;
        if (Globals.mainWindow != null) {
            pMapVisualObject = Globals.mainWindow.pMapVisualObject;
        }
    }

    /**
     * Native: CGameObject::CGameObject @0045FC42.
     * Fully ported.
     */
    protected CGameObject(CGameObject source) {
        m_id = source.m_id & 0xFFFF;
        location.x = source.location.x;
        location.y = source.location.y;
        z = source.z;
        cPlayer = source.cPlayer;
        type = source.type;
        field8_0x28 = source.field8_0x28;
        location2.x = source.location2.x;
        location2.y = source.location2.y;
        field10_0x34 = source.field10_0x34;
        tileX = source.tileX;
        tileY = source.tileY;
        mapBoundsLeft = source.mapBoundsLeft;
        mapBoundsTop = source.mapBoundsTop;
        mapBoundsRight = source.mapBoundsRight;
        mapBoundsBottom = source.mapBoundsBottom;
        mapLayerActive = source.mapLayerActive;
        centerScreenX = source.centerScreenX;
        centerScreenY = source.centerScreenY;
        screenX = source.screenX;
        screenY = source.screenY;
        terrainHeightOffset = source.terrainHeightOffset;
        dir = source.dir;
        phase = source.phase;
        lastAction = source.lastAction;
        bIsBlocked = source.bIsBlocked;
        m_bSelected = source.m_bSelected;
        field30_0x84 = source.field30_0x84;
        controlGroupMask = source.controlGroupMask;
        action = source.action;
        actionX = source.actionX;
        actionY = source.actionY;
        actionZ = source.actionZ;
        actionDir = source.actionDir;
        actionPhase = source.actionPhase;
        actionTarget = source.actionTarget;
        field39_0xa0 = source.field39_0xa0;
        field40_0xa4 = source.field40_0xa4;
        actionSpell = source.actionSpell;
        availableSpellMask = source.availableSpellMask;
        spellbookMask = source.spellbookMask;
        actionSegments = source.actionSegments;
        field44_0xc4 = source.field44_0xc4;
        shopInventoryVisibleStart[0] = source.shopInventoryVisibleStart[0];
        pMapVisualObject = source.pMapVisualObject;
        HP = source.HP;
        field52_0x106 = source.field52_0x106;
        MaxHP = source.MaxHP;
        packedSightRange = source.packedSightRange;
        speed = source.speed;
        copiedEncumbranceWeight = source.copiedEncumbranceWeight;
        m_bSelectionDirty = source.m_bSelectionDirty;
        dwarr_130.addAll(source.dwarr_130);
        for (VisualElem visual : source.transientVisualElements) {
            transientVisualElements.add(new VisualElem(visual));
        }
        actionTargets.addAll(source.actionTargets);
        for (TokenEntry entry : source.tokenEntries) {
            tokenEntries.add(new TokenEntry(entry));
        }
        name = source.name;
        clan = source.clan;
    }

    /**
     * vtbl +0x08: CObject::Serialize @00401970.
     */
    @Override
    public void serialize(CArchive ar) {
        // Native CObject::Serialize is a no-op.
    }

    /**
     * vtbl +0x0C: CGameObject::AssertValid @0046074A.
     * Full port for Java diagnostics. Native reports invalid game-object state through AfxMessageBox.
     */
    public void assertValid() {
        if (GAMEPLAY.isUnsetIn(Globals.mainWindow.dialogsMask)) {
            return;
        }
        if (location.x >= 0x700
                && location.y >= 0x700
                && location.x < (pMapVisualObject.cachedMapWidth - 7) * 0x100
                && location.y < (pMapVisualObject.cachedMapHeight - 7) * 0x100
                && type >= 0
                && type <= 0x100
                && actionX >= -0x100
                && actionY >= -0x100
                && actionX < (pMapVisualObject.cachedMapWidth - 8) * 0x100
                && actionY < (pMapVisualObject.cachedMapHeight - 8) * 0x100) {
            return;
        }

        System.err.printf(
                "Shit! Invalid GameObject #%d coordinates X:%d Y:%d player:%d picture:%d%n",
                m_id & 0xFFFF,
                location.x >> 8,
                location.y >> 8,
                cPlayer.playerId,
                type
        );
    }

    /**
     * vtbl +0x10: CGameObject::Dump @00460730.
     * Full port for Java diagnostics: native writes the same class name into CDumpContext.
     */
    public String dump() {
        return "CGameObject";
    }

    /**
     * vtbl +0x14: CGameObject::SetSelected @0046DD20.
     * Full port. Native sets the selected flag and marks the selection visuals dirty.
     */
    public void setSelected(boolean selected) {
        m_bSelected = selected;
        m_bSelectionDirty = 1;
    }

    /**
     * Native: CGameObject::SetGroup @0041EB80.
     * Fully ported.
     */
    public void setGroup(int group) {
        controlGroupMask = 1 << (group & 0x1F);
        m_bSelectionDirty = 1;
    }

    /**
     * Native: CGameObject::AddToGroup @0041EBB0.
     * Fully ported.
     */
    public void addToGroup(int group) {
        controlGroupMask |= 1 << (group & 0x1F);
        m_bSelectionDirty = 1;
    }

    /**
     * Native: CGameObject::BelongsToGroup @0041EBF0.
     * Fully ported.
     */
    public int belongsToGroup(int group) {
        return controlGroupMask & (1 << (group & 0x1F));
    }

    /**
     * Native: CGameObject::RemoveFromGroup @0041EC20.
     * Fully ported.
     */
    public void removeFromGroup(int group) {
        controlGroupMask &= ~(1 << (group & 0x1F));
    }

    /**
     * Native: CGameObject::GetFirstControlGroup @0041EC50.
     * Fully ported.
     */
    public int getFirstControlGroup() {
        for (int group = 0; group < Integer.SIZE; group++) {
            if ((controlGroupMask & (1 << group)) != 0) {
                return group;
            }
        }
        return -1;
    }

    /**
     * vtbl +0x18: CGameObject::GetPortraitWidth @0046DD50.
     * Full port. Native base implementation returns `0`.
     */
    public int getPortraitWidth() {
        return 0;
    }

    /**
     * vtbl +0x1C: CGameObject::GetPortraitHeight @0046DD60.
     * Full port. Native base implementation returns `0`.
     */
    public int getPortraitHeight() {
        return 0;
    }

    /**
     * vtbl +0x20: CGameObject::GetTileWidth @0046DD70.
     * Full port. Native base implementation returns `0`.
     */
    public int getTileWidth() {
        return 0;
    }

    /**
     * vtbl +0x24: CGameObject::GetTileHeight @0046DD80.
     * Full port. Native base implementation returns `0`.
     */
    public int getTileHeight() {
        return 0;
    }

    /**
     * vtbl +0x28: CGameObject::Draw @0046DD90.
     * Full port. Native base implementation is a no-op.
     */
    public void draw(int param1, int param2, int param3) {
    }

    /**
     * vtbl +0x2C: CGameObject::DrawShadow @0046DDA0.
     * Full port. Native base implementation is a no-op.
     */
    public void drawShadow(int viewTileX, int viewTileY) {
    }

    /**
     * vtbl +0x30: CGameObject::DrawSelectionOverlay @0046DDB0.
     * Full port. Native base implementation is a no-op.
     */
    public void drawSelectionOverlay() {
    }

    /**
     * vtbl +0x34: CGameObject::DrawMinimap @0046DDC0.
     * Full port. Native base implementation is a no-op.
     */
    public void drawMinimap(int param1, int param2, int param3) {
    }

    /**
     * vtbl +0x38: CGameObject::UpdateMapLayer @0046DDD0.
     * Full port. Native base implementation is a no-op.
     */
    public void updateMapLayer() {
    }

    /**
     * vtbl +0x3C: CGameObject::AdvanceMapObjectState @004608C3.
     * Full port. Native dispatches into vtbl slot `TryOccupyMap` and returns `true`.
     */
    public boolean advanceMapObjectState() {
        tryOccupyMap();
        return true;
    }

    /**
     * Native support: CGameObject::ResolveDirectionToPoint @004608DE.
     * Full port. Native resolves a 16-way direction from this object's location to a target point.
     */
    protected int resolveDirectionToPoint(int x, int y) {
        int dx = x - location.x;
        int dy = y - location.y;
        int absDx = Math.abs(dx);
        int absDy = Math.abs(dy);
        int direction;
        if (absDx < absDy * 4) {
            if (absDx * 3 < absDy * 4) {
                if (absDy < absDx * 4) {
                    if (absDy * 3 < absDx * 4) {
                        direction = 2;
                    } else {
                        direction = 3;
                    }
                } else {
                    direction = 4;
                }
            } else {
                direction = 1;
            }
        } else {
            direction = 0;
        }
        if (dy < 1) {
            if (dx < 0) {
                direction += 0x0C;
            } else {
                direction = 4 - direction;
            }
        } else if (dx < 1) {
            direction = 0x0C - direction;
        } else {
            direction += 4;
        }
        return direction & 0x0F;
    }

    /**
     * Native support extracted from CUnit sound callbacks @00467CD2, @00467DA8, @004681C9, @004682A6,
     * @00468383, @00467E42, @0046853D, @004685F4, @00468460, @004686AB and
     * CProjectile::AdvanceMapObjectState @0046C1D0.
     */
    protected Point soundSourceWorldPosition() {
        return location;
    }

    /**
     * Native support extracted from CUnit sound callbacks @00467CD2, @00467DA8, @004681C9, @004682A6,
     * @00468383, @00467E42, @0046853D, @004685F4, @00468460, @004686AB and
     * CProjectile::AdvanceMapObjectState @0046C1D0.
     */
    protected byte objectMapSoundPriority(SoundSystem soundSystem) {
        Point source = soundSourceWorldPosition();
        return (byte) soundSystem.mapSoundPriority(source.x, source.y);
    }

    /**
     * Native support extracted from CUnit sound callbacks @00467CD2, @00467DA8, @004681C9, @004682A6,
     * @00468383, @00467E42, @0046853D, @004685F4, @00468460, @004686AB and
     * CProjectile::AdvanceMapObjectState @0046C1D0.
     */
    protected void playObjectMapSound(
            SoundSystem soundSystem,
            Sound sound,
            int volume,
            byte priority,
            int freq
    ) {
        Point source = soundSourceWorldPosition();
        soundSystem.playMapSound(sound, volume, source.x, source.y, false, priority, freq);
    }

    /**
     * Native: CGameObject::RefreshMapDerivedState @0046025D.
     * Fully ported.
     */
    public void refreshMapDerivedState() {
        tileX = location2.x >> 8;
        tileY = location2.y >> 8;
        int viewX = pMapVisualObject.view.x;
        int viewY = pMapVisualObject.view.y;
        mapBoundsLeft = ((location2.x - 0x80) >> 8) - viewX;
        mapBoundsTop = ((location2.y - 0x80) >> 8) - viewY;
        mapBoundsRight = ((location2.x + 0x7F + (getTileWidth() - 1) * 0x100) >> 8) - viewX;
        mapBoundsBottom = ((location2.y + 0x7F + (getTileHeight() - 1) * 0x100) >> 8) - viewY;

        mapLayerActive = 1;
        if (mapBoundsLeft < -3) {
            mapBoundsLeft = -3;
        }
        if (pMapVisualObject.gridWidth + 3 <= mapBoundsLeft) {
            mapLayerActive = 0;
        }
        if (mapBoundsRight < -3) {
            mapLayerActive = 0;
        }
        if (pMapVisualObject.gridWidth + 3 <= mapBoundsRight) {
            mapBoundsRight = pMapVisualObject.gridWidth + 2;
        }
        if (mapBoundsTop < -3) {
            mapBoundsTop = -3;
        }
        if (pMapVisualObject.gridHeight + 7 <= mapBoundsTop) {
            mapLayerActive = 0;
        }
        if (mapBoundsBottom < -3) {
            mapLayerActive = 0;
        }
        if (pMapVisualObject.gridHeight + 7 <= mapBoundsBottom) {
            mapBoundsBottom = pMapVisualObject.gridHeight + 6;
        }

        screenX = (location2.x >> 3) - viewX * 0x20;
        screenY = (location2.y >> 3) - viewY * 0x20;
        centerWorldX8 = location2.x - 0x80 + getTileWidth() * 0x80;
        centerWorldY8 = location2.y - 0x80 + getTileHeight() * 0x80;
        centerScreenX = (centerWorldX8 >> 3) - viewX * 0x20;
        centerScreenY = (centerWorldY8 >> 3) - viewY * 0x20;
        terrainHeightOffset = interpolateTerrainHeight(centerWorldX8, centerWorldY8);
    }

    /**
     * vtbl +0x40: CGameObject::UpdateMapOverlay @0046DDE0.
     * Full port. Native base implementation is a no-op.
     */
    public void updateMapOverlay() {
    }

    /**
     * vtbl +0x44: CGameObject::TryOccupyMap @00460AE0.
     * Full port. Native gates on current-player visibility, sight range, tile-center screen alignment, and movement.
     */
    public void tryOccupyMap() {
        if (pMapVisualObject.currentPlayer.isMapVisible(cPlayer.playerId)
                && packedSightRange != 0
                && (screenX & 0x1F) == MAP_READY_STATE
                && (screenY & 0x1F) == MAP_READY_STATE
                && (location.x != occupiedLocation.x || location.y != occupiedLocation.y)) {
            occupyMapCells();
        }
    }

    /**
     * vtbl +0x48: CGameObject::OccupyMapCells @00460B76.
     * Full port. Native marks the 41x41 visible-sector footprint as occupied in `MapDescriptor::GetTilesWxH`.
     */
    public void occupyMapCells() {
        if (!pMapVisualObject.currentPlayer.isMapVisible(cPlayer.playerId) || packedSightRange == 0) {
            return;
        }

        pMapVisualObject.mapOccupancyDirty = 1;
        short[] tileFlags = pMapVisualObject.mapDescriptor.tilesWxH;
        int[][] visibleSectorCostGrid = pMapVisualObject.buildVisibleSectorCostGrid(this);
        int centerTileX = tileX + getTileWidth() / 2;
        int centerTileY = tileY + getTileHeight() / 2;
        int mapWidth = pMapVisualObject.cachedMapWidth;
        for (int gridX = 0; gridX < OCCUPANCY_GRID_SIZE; gridX++) {
            for (int gridY = 0; gridY < OCCUPANCY_GRID_SIZE; gridY++) {
                if (visibleSectorCostGrid[gridX][gridY] <= 0) {
                    continue;
                }

                int visibilityTileX = centerTileX - OCCUPANCY_GRID_CENTER + gridX;
                int visibilityTileY = centerTileY - OCCUPANCY_GRID_CENTER + gridY;
                int tileIndex = visibilityTileX + visibilityTileY * mapWidth;
                tileFlags[tileIndex] = (short) (tileFlags[tileIndex] | MAP_BLOCKED_MASK);
                tileFlags[tileIndex + 1] = (short) (tileFlags[tileIndex + 1] | MAP_BLOCKED_MASK);
                tileFlags[tileIndex + mapWidth + 1] = (short) (tileFlags[tileIndex + mapWidth + 1] | MAP_BLOCKED_MASK);
                tileFlags[tileIndex + mapWidth] = (short) (tileFlags[tileIndex + mapWidth] | MAP_BLOCKED_MASK);
            }
        }
        occupiedLocation.x = location.x;
        occupiedLocation.y = location.y;
    }

    /**
     * vtbl +0x4C: CGameObject::UpdateBlockedState @004609CA.
     * Full port. Native derives blocked state from the 2x2 occupancy tile mask under this object's tile.
     */
    public boolean updateBlockedState() {
        short[] tileFlags = pMapVisualObject.mapDescriptor.tilesWxH;
        int mapWidth = pMapVisualObject.cachedMapWidth;
        int tileIndex = tileX + mapWidth * tileY;
        bIsBlocked = ((tileFlags[tileIndex] & MAP_BLOCKED_MASK)
                | (tileFlags[tileIndex + 1] & MAP_BLOCKED_MASK)
                | (tileFlags[tileIndex + mapWidth] & MAP_BLOCKED_MASK)
                | (tileFlags[tileIndex + mapWidth + 1] & MAP_BLOCKED_MASK)) != MAP_BLOCKED_MASK ? 1 : 0;
        if (bIsBlocked == 0 || !m_bSelected) {
            return false;
        }

        m_bSelected = false;
        m_bSelectionDirty = 1;
        return true;
    }

    /**
     * vtbl +0x50: CGameObject::UpdateEffectVisuals @0046DDF0.
     * Full port. Native base implementation is a no-op.
     */
    public void updateEffectVisuals() {
    }

    /**
     * Native: CGameObject::isSelected @0041EB40.
     * Fully ported.
     */
    public boolean isSelected() {
        return m_bSelected;
    }

    /**
     * Native: CGameObject::GetBlockedDrawState @0041EB60.
     * Fully ported.
     */
    public int getBlockedDrawState() {
        return bIsBlocked;
    }

    /**
     * Native: CGameObject::MarkObjectLayerCell @00460591.
     * Full port. Native writes this object into the requested map-object layer when map-layer updates are active.
     */
    protected void markObjectLayerCell(int layerKind, CGameObject object) {
        if (mapLayerActive == 0) {
            return;
        }
        pMapVisualObject.markObjectLayerCell(layerKind, this, object);
    }

    /**
     * Native support extracted from CGameObject::RefreshMapDerivedState @0046025D.
     * Fully ported.
     */
    private int interpolateTerrainHeight(int mapPixelX, int mapPixelY) {
        int tileBaseX = mapPixelX >> 8;
        int tileBaseY = mapPixelY >> 8;
        int horizontalFraction = (mapPixelX >> 3) & 0x1F;
        int verticalFraction = (mapPixelY >> 3) & 0x1F;
        int topLeft = pMapVisualObject.mapDescriptor.heightAt(tileBaseX, tileBaseY);
        int topRight = pMapVisualObject.mapDescriptor.heightAt(tileBaseX + 1, tileBaseY);
        int bottomLeft = pMapVisualObject.mapDescriptor.heightAt(tileBaseX, tileBaseY + 1);
        int bottomRight = pMapVisualObject.mapDescriptor.heightAt(tileBaseX + 1, tileBaseY + 1);
        int top = topLeft + arithmeticDivideBy32((topRight - topLeft) * horizontalFraction);
        int bottom = bottomLeft + arithmeticDivideBy32((bottomRight - bottomLeft) * horizontalFraction);
        return top + arithmeticDivideBy32((bottom - top) * verticalFraction);
    }

    /**
     * Native support extracted from CGameObject::RefreshMapDerivedState @0046025D.
     * Fully ported.
     */
    private static int arithmeticDivideBy32(int value) {
        return (value + ((value >> 31) & 0x1F)) >> 5;
    }

    /**
     * Native support for the `MapDescriptor::GetShadowAngle @004A4429` shadow-slope input used by CStructure::DrawShadow @00461890
     * and CUnit::DrawShadow @00464487.
     * Full port.
     */
    protected final int resolveShadowSlope() {
        return (int) (Math.tan(pMapVisualObject.mapDescriptor.getShadowAngle()) * 65536.0);
    }
}
