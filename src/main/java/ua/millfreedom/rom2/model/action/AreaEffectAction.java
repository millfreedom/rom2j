package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.spell.AreaEffect;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native action class `Fixed18ByteAction` / packet id `0x87` used by
 * `CServerApp::sendAreaEffectActionVisibilityGated @00503E41`
 * and `CServerApp::sendSpellEffectStateAction @005045A5` to broadcast area-effect footprint/mask updates.
 */
public class AreaEffectAction extends CGameAction {
    public static final int ACTION_ID = GameActionId.AREA_EFFECT_ACTION_87.id;
    public static final AreaEffectAction global = new AreaEffectAction();
    private static final int OCCUPANCY_BITMAP_BYTES = 12;

    //0x0A
    public final Property<Integer> spellEffectTypeId = u8(BODY_OFFSET);
    //0x0B
    public final Property<Integer> originX = u8(BODY_OFFSET + 1);
    //0x0C
    public final Property<Integer> originY = u8(BODY_OFFSET + 2);
    //0x0D
    public final Property<Integer> footprintWidth = u8(BODY_OFFSET + 3);
    //0x0E
    public final Property<Integer> footprintHeight = u8(BODY_OFFSET + 4);
    //0x0F
    public final Property<Integer> applyFlag = u8(BODY_OFFSET + 5);
    //0x10
    public final Property<byte[]> occupancyBitmap = bytes(BODY_OFFSET + 6, OCCUPANCY_BITMAP_BYTES);

    /**
     * Native: Fixed18ByteAction::Fixed18ByteAction @0050C46A.
     * Fully ported.
     */
    public AreaEffectAction() {
        super();
        ID.set(ACTION_ID);
        footprintWidth.set(0);
        footprintHeight.set(0);
    }

    /**
     * Native: Fixed18ByteAction::Fixed18ByteAction @0050C49E.
     * Fully ported.
     */
    public AreaEffectAction(AreaEffectAction from) {
        super();
        int copySize = GetPayloadSize();
        PutSlice(ID_OFFSET, from.GetSlice(ID_OFFSET, copySize), 0, copySize);
    }

    /**
     * Native support extracted from CServerApp::sendSpellEffectStateAction @005045A5 packet field writes.
     */
    public static AreaEffectAction prepareForSpellEffectState(AreaEffect areaEffect, int applyFlag) {
        AreaEffectAction action = global;
        int radius = areaEffect.radiusLengthHalf & 0xFF;
        int originX = areaEffect.m_pTargetHandle.getX() - radius;
        int originY = areaEffect.m_pTargetHandle.getY() - radius;
        int footprintSize = radius * 2 + 1;

        action.ID.set(ACTION_ID);
        action.playerID.set(0);
        action.spellEffectTypeId.set(areaEffect.getTokenTypeId() & 0xFF);
        action.originX.set(originX & 0xFF);
        action.originY.set(originY & 0xFF);
        action.footprintWidth.set(footprintSize & 0xFF);
        action.footprintHeight.set(footprintSize & 0xFF);
        action.applyFlag.set(applyFlag & 0xFF);
        action.occupancyBitmap.set(buildAreaEffectOccupancyBitmap(
                areaEffect,
                applyFlag,
                originX,
                originY,
                footprintSize
        ));
        return action;
    }

    /**
     * vtbl +0x04: Fixed18ByteAction::Clone @00541620.
     * Fully ported.
     */
    @Override
    public AreaEffectAction Clone() {
        return new AreaEffectAction(this);
    }

    /**
     * vtbl +0x10: Fixed18ByteAction::getWireSize @005416A0.
     * Fully ported.
     */
    @Override
    public int GetPayloadSize() {
        return 0x13;
    }

    /**
     * Native support extracted from CServerApp::sendSpellEffectStateAction @005045A5 packet field writes.
     */
    private static byte[] buildAreaEffectOccupancyBitmap(
            AreaEffect areaEffect,
            int applyFlag,
            int originX,
            int originY,
            int footprintSize
    ) {
        byte[] bitmap = new byte[OCCUPANCY_BITMAP_BYTES];
        for (int localY = 0; localY < footprintSize; localY++) {
            for (int localX = 0; localX < footprintSize; localX++) {
                int packedCell = ((originX + localX) & 0xFF) | (((originY + localY) & 0xFF) << 8);
                AreaEffect existing = Globals.worldMap.findAreaEffectAtLayerCell(areaEffect, packedCell);
                if ((applyFlag == 0 && existing == null) || (applyFlag != 0 && existing != null)) {
                    setAreaEffectOccupancyBit(bitmap, localX + localY * footprintSize);
                }
            }
        }
        return bitmap;
    }

    /**
     * Native support extracted from CServerApp::sendSpellEffectStateAction @005045A5 packet field writes.
     */
    private static void setAreaEffectOccupancyBit(byte[] bitmap, int bitIndex) {
        bitmap[bitIndex >> 3] = (byte) (bitmap[bitIndex >> 3] | (1 << (bitIndex & 7)));
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00414CB6.
     * Fully ported.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        int effectFlagIndex = (spellEffectTypeId.get() / 2) - 4;
        mapVisualObject.applyAreaEffectFootprint(
                originX.get(),
                originY.get(),
                footprintWidth.get(),
                footprintHeight.get(),
                effectFlagIndex,
                occupancyBitmap.get(),
                applyFlag.get()
        );
        if (applyFlag.get() == 0) {
            mapVisualObject.areaEffectRefreshPending = 1;
        }
    }

}
