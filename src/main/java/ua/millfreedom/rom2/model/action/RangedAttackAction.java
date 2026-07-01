package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.CUnitInfo;
import ua.millfreedom.rom2.model.Token;
import ua.millfreedom.rom2.model.UnitTypes;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.gameobj.CGameObject;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native action class `FixedDwordAction72` / packet id `0x72` used by
 * `CServerApp::sendUnitActionVisibilityGated @005039E2`
 * and `CServerApp::emitActionStartExtended @00504155` to broadcast unit ranged-attack starts.
 */
public class RangedAttackAction extends CGameAction {
    public static final int ACTION_ID = GameActionId.RANGED_ATTACK_ACTION_72.id;
    public static final RangedAttackAction global = new RangedAttackAction();

    //0x0A
    public final Property<Integer> unitTokenId = u16(BODY_OFFSET);
    //0x0C
    public final Property<Integer> targetTokenId = u16(BODY_OFFSET + Short.BYTES);

    /**
     * Native: FixedDwordAction72::FixedDwordAction72 @0050C50B.
     * Fully ported.
     */
    public RangedAttackAction() {
        super();
        targetTokenId.set(0);
    }

    /**
     * Native support extracted from CServerApp::emitActionStartExtended @00504155 packet field writes.
     */
    public static RangedAttackAction prepareForExtendedActionStart(Unit unit) {
        RangedAttackAction action = global;
        action.ID.set(ACTION_ID);
        action.unitTokenId.set(unit.idFull);
        action.targetTokenId.set(unit.actionTarget.idFull);
        action.playerID.set(0);
        return action;
    }

    /**
     * Native: FixedDwordAction72::FixedDwordAction72 @0050C533.
     * Fully ported.
     */
    public RangedAttackAction(RangedAttackAction from) {
        super();
        int copySize = GetPayloadSize();
        PutSlice(ID_OFFSET, from.GetSlice(ID_OFFSET, copySize), 0, copySize);
    }

    /**
     * vtbl +0x04: FixedDwordAction72::Clone @005416E0.
     * Fully ported.
     */
    @Override
    public RangedAttackAction Clone() {
        return new RangedAttackAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @004132D2.
     * Partial port. Java keeps the recovered ranged-attack command state mutation.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        CGameObject gameObject = mapVisualObject.getObjectByToken((short) (int) unitTokenId.get());
        if (gameObject != null && gameObject.actionSegments == 0 && gameObject.HP > 0) {
            CUnitInfo unitInfo = UnitTypes.getUnitInfo(gameObject.type);
            gameObject.actionSegments = unitInfo.m_AttackFrameSequenceCount;
            gameObject.action = 7;
            gameObject.actionPhase = 0;
            gameObject.field40_0xa4 = 0;
            gameObject.field39_0xa0 = 0;
            gameObject.actionTarget = (short) (int) targetTokenId.get();
            gameObject.actionDir = (byte) gameObject.dir;
            mapVisualObject.renderFrameDirty = 1;
        }
    }

    /**
     * vtbl +0x10: FixedDwordAction72::getWireSize @00541760.
     * Fully ported.
     */
    @Override
    public int GetPayloadSize() {
        return 0x05;
    }
}
