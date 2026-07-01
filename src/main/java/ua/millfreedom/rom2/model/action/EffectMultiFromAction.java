package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.gameobj.CProjectile;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `ShortArrayBlobAction` packet id `0x8C` handled by MapVisualObject::HandleGameAction @00411B3F.
 */
public class EffectMultiFromAction extends ShortArrayBlobAction {
    public static final int ACTION_ID = GameActionId.EFFECT_MULTI_FROM_ACTION_8C.id;
    public static final EffectMultiFromAction global = new EffectMultiFromAction();
    // Native visual spell/effect type used by MapVisualObject::HandleGameAction @00411B3F.
    private static final int PRISMATIC_SPRAY_VISUAL_ID = 0x1E;

    /**
     * Native support extracted from ShortArrayBlobAction::ShortArrayBlobAction @0050C726 and MapVisualObject::HandleGameAction @00411B3F.
     */
    public EffectMultiFromAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from ShortArrayBlobAction::ShortArrayBlobAction @0050C745 and MapVisualObject::HandleGameAction @00411B3F.
     */
    public EffectMultiFromAction(EffectMultiFromAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CServerApp::dispatchSpellTargets @00504391 packet field writes.
     */
    public static EffectMultiFromAction prepareForSpellTargets(Unit caster, CustomList<Unit> targets) {
        return EffectMultiTargetAction.prepareSpellTargetsAction(global, ACTION_ID, caster, targets);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00411B3F.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        short[] decodedValues = MapVisualObject.decodeShortValues(shortValueCount.get(), shortValues.get());
        int sourceTile = Short.toUnsignedInt(decodedValues[0]);
        short firstTarget = decodedValues.length > 1 ? decodedValues[1] : 0;
        CProjectile projectile = mapVisualObject.spawnProjectile(
                MapVisualObject.pixelCenterFromTile(sourceTile & 0xFF),
                MapVisualObject.pixelCenterFromTile((sourceTile >>> 8) & 0xFF),
                0,
                0,
                PRISMATIC_SPRAY_VISUAL_ID,
                0x0D,
                firstTarget
        );
        projectile.actionTargets.clear();
        for (int i = 1; i < decodedValues.length; i++) {
            projectile.actionTargets.add(decodedValues[i]);
        }
    }

    /**
     * Native support extracted from ShortArrayBlobAction::Clone @00541940.
     */
    @Override
    public EffectMultiFromAction Clone() {
        return new EffectMultiFromAction(this);
    }
}
