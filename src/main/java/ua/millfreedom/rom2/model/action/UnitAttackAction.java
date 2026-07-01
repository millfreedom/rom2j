package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.CUnitInfo;
import ua.millfreedom.rom2.model.UnitTypes;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.gameobj.CGameObject;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `UnitCommandStartAction` packet id `0x71` used by
 * `CServerApp::sendUnitActionVisibilityGated @005039E2`
 * and `CServerApp::emitActionStart @00504AB8` to broadcast unit melee-attack starts.
 */
public class UnitAttackAction extends UnitCommandStartAction {
    public static final int ACTION_ID = GameActionId.UNIT_ATTACK_ACTION_71.id;
    public static final UnitAttackAction global = new UnitAttackAction();

    /**
     * Native support extracted from CServerApp::sendUnitActionVisibilityGated @005039E2,
     * CServerApp::sendUnitCommandStartAction @005040C4, and
     * CServerApp::emitActionStart @00504AB8.
     */
    public UnitAttackAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from UnitCommandStartAction::UnitCommandStartAction @0050C236 and
     * CServerApp::emitActionStart @00504AB8.
     */
    public UnitAttackAction(UnitAttackAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from UnitCommandStartAction::Clone @00541320.
     */
    @Override
    public UnitAttackAction Clone() {
        return new UnitAttackAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00413032.
     * Partial port. Java keeps the recovered melee-attack command state mutation.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        CGameObject gameObject = mapVisualObject.getObjectByToken((short) (int) unitTokenId.get());
        if (gameObject != null && gameObject.actionSegments == 0 && gameObject.HP > 0) {
            CUnitInfo unitInfo = UnitTypes.getUnitInfo(gameObject.type);
            if (unitInfo.m_AttackPhases != 0) {
                gameObject.actionSegments = unitInfo.m_AttackFrameSequenceCount;
                gameObject.action = 3;
                gameObject.actionDir = (byte) (int) commandDirection.get();
                gameObject.actionPhase = 0;
                gameObject.field40_0xa4 = 0;
                gameObject.field39_0xa0 = 0;
                mapVisualObject.renderFrameDirty = 1;
            }
        }
    }

}
