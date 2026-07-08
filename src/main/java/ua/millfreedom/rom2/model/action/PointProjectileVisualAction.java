package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.TargetHandle;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.gameobj.CProjectile;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Java-only transfer-spot point-projectile visual packet using the EffectAction payload shape.
 * not ported.
 */
public final class PointProjectileVisualAction extends EffectAction {
    public static final int ACTION_ID = GameActionId.MAP_TRANSFER_SPOT_VISUAL_ACTION_C4.id;
    public static final PointProjectileVisualAction global = new PointProjectileVisualAction();

    /**
     * Java support constructor for casterless point-projectile visual packets.
     * not ported.
     */
    public PointProjectileVisualAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Java support copy constructor for visibility-gated casterless point-projectile visual packets.
     * not ported.
     */
    public PointProjectileVisualAction(PointProjectileVisualAction from) {
        super(from);
    }

    /**
     * Java support packet builder for a casterless projectile visual at a map cell.
     * not ported.
     */
    public static PointProjectileVisualAction prepareForPointProjectileVisual(
            TargetHandle targetHandle,
            int projectileType,
            int segments
    ) {
        PointProjectileVisualAction action = global;
        action.ID.set(ACTION_ID);
        byte[] actionPayload = new byte[7];
        writePayloadWord(actionPayload, 0, 0);
        actionPayload[2] = (byte) projectileType;
        actionPayload[3] = (byte) targetHandle.getX();
        actionPayload[4] = (byte) targetHandle.getY();
        writePayloadWord(actionPayload, 5, segments & 0xFF);
        action.playerID.set(0);
        action.payload.set(actionPayload);
        return action;
    }

    /**
     * Java support write helper matching EffectAction packet word fields.
     * not ported.
     */
    private static void writePayloadWord(byte[] actionPayload, int offset, int value) {
        actionPayload[offset] = (byte) value;
        actionPayload[offset + 1] = (byte) (value >>> Byte.SIZE);
    }

    /**
     * Java-only transfer-spot visual handler for casterless point-projectile payloads.
     * not ported.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        byte[] actionPayload = payload.get();
        int projectileType = MapVisualObject.u8(actionPayload, 2);
        if (MapVisualObject.u16(actionPayload, 0) == 0 && MapVisualObject.hasProjectileInfo(projectileType)) {
            spawnCasterlessPointProjectile(mapVisualObject, actionPayload, projectileType);
        }
    }

    /**
     * Java support clone preserving the casterless point-projectile subclass.
     * not ported.
     */
    @Override
    public PointProjectileVisualAction Clone() {
        return new PointProjectileVisualAction(this);
    }

    /**
     * Java support point-projectile spawn with initialized first-frame state.
     * not ported.
     */
    private static void spawnCasterlessPointProjectile(
            MapVisualObject mapVisualObject,
            byte[] actionPayload,
            int projectileType
    ) {
        int sourceX = MapVisualObject.pixelCenterFromTile(MapVisualObject.u8(actionPayload, 3));
        int sourceY = MapVisualObject.pixelCenterFromTile(MapVisualObject.u8(actionPayload, 4));
        CProjectile projectile = mapVisualObject.spawnProjectile(
                sourceX,
                sourceY,
                sourceX,
                sourceY,
                projectileType,
                MapVisualObject.u8(actionPayload, 5),
                (short) 0
        );
        projectile.phase = 0;
        projectile.actionPhase = 0;
    }
}
