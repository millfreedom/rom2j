package ua.millfreedom.rom2.model.gameobj;

import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.Globals;

public class CBackPack extends CGameObject {
    private static final int MISC_MAP_LAYER = 0;

    /**
     * Native: CBackPack::CBackPack @0046117B.
     * Full port. Java superclass construction covers the native CGameObject constructor call.
     */
    public CBackPack() {
    }

    /**
     * Native: CBackPack::CBackPack @0046119A.
     * Full port. Native delegates to CGameObject copy construction and installs the CBackPack vtable.
     */
    public CBackPack(CBackPack source) {
        super(source);
    }

    /**
     * Native: CBackPack::InitializeBackpackVisualState @004601CD.
     * Full port. Native initializes the vtable-created backpack identity, location, facing, phase, speed, and HP.
     */
    public void initializeBackpackVisualState(
            int objectId,
            int backpackType,
            int x,
            int y,
            int backpackZ,
            int direction,
            int animationPhase,
            int speed,
            int hp
    ) {
        m_id = objectId & 0xFFFF;
        location.x = x;
        location2.x = location.x;
        location.y = y;
        location2.y = location.y;
        z = backpackZ;
        dir = direction;
        phase = animationPhase;
        this.speed = (short) speed;
        MaxHP = (short) hp;
        HP = (short) hp;
        type = backpackType;
    }

    /**
     * vtbl +0x10: CBackPack::Dump @004613E9.
     * Full port. Native writes the `CBackPack` class label into CDumpContext.
     */
    @Override
    public String dump() {
        return "CBackPack";
    }

    /**
     * vtbl +0x20: CBackPack::GetTileWidth @0046DEB0.
     * Fully ported.
     */
    @Override
    public int getTileWidth() {
        return 1;
    }

    /**
     * vtbl +0x24: CBackPack::GetTileHeight @0046DEC0.
     * Fully ported.
     */
    @Override
    public int getTileHeight() {
        return 1;
    }

    /**
     * vtbl +0x28: CBackPack::Draw @004611BF.
     * Full port. Native draws the backpack sprite from the object's screen center and conditionally draws smoothing.
     */
    @Override
    public void draw(int viewTileX, int viewTileY, int palettePage) {
        int x = centerScreenX - GUI.sprBackpack.xSizeOf(0) / 2;
        int y = centerScreenY - GUI.sprBackpack.ySizeOf(0) / 2 - terrainHeightOffset - 4;
        GUI.sprBackpack.draw(x, y, type, palettePage, false);
        if (Globals.gamePreferences.smoothing != 0) {
            GUI.sprBackpackB.drawFrameClippedY(x, y, type, palettePage, GUI.sprBackpack.palette, false);
        }
    }

    /**
     * vtbl +0x2C: CBackPack::DrawShadow @00461292.
     * Full port. Native draws the backpack shadow and smoothing shadow overlay from the object's screen center.
     */
    @Override
    public void drawShadow(int viewTileX, int viewTileY) {
        int shadowSlope = resolveShadowSlope();
        double shadowAngle = Math.tan(pMapVisualObject.mapDescriptor.getShadowAngle());
        int halfHeight = GUI.sprBackpack.ySizeOf(0) / 2;
        int shadowSkew = (int) (shadowAngle * halfHeight);
        int x = centerScreenX - GUI.sprBackpack.xSizeOf(0) / 2 - shadowSkew;
        int y = centerScreenY - GUI.sprBackpack.ySizeOf(0) / 2 - terrainHeightOffset - 4;
        GUI.sprBackpack.drawWithRenderEffect(x, y, type, Globals.lighting.shadowLength, shadowSlope, false);
        GUI.sprBackpackB.drawWithRenderEffect(x, y, type, Globals.lighting.lightHeight, shadowSlope, false);
    }

    /**
     * vtbl +0x38: CBackPack::UpdateMapLayer @004613D0.
     * Full port. Native writes this backpack into misc map layer `0`.
     */
    @Override
    public void updateMapLayer() {
        markObjectLayerCell(MISC_MAP_LAYER, this);
    }
}
