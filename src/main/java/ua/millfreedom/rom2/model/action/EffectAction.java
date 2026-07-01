package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.TargetHandle;
import ua.millfreedom.rom2.model.Token;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.gameobj.CGameObject;
import ua.millfreedom.rom2.model.spell.Spell;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Raw packet model for native EffectAction.
 */
public class EffectAction extends CGameAction {
    public static final int ACTION_ID = GameActionId.EFFECT_ACTION_86.id;
    public static final EffectAction global = new EffectAction();

    //0x0A
    public final Property<byte[]> payload = bytes(BODY_OFFSET, 7);

    /**
     * Native: EffectAction::EffectAction @0050C32F.
     * Fully ported.
     */
    public EffectAction() {
        super();
        ID.set(ACTION_ID);
        payload.fillAt(2, 3, (byte) 0);
    }

    /**
     * Native: EffectAction::EffectAction @0050C36A.
     * Fully ported.
     */
    public EffectAction(EffectAction from) {
        super();
        int copySize = GetPayloadSize();
        PutSlice(ID_OFFSET, from.GetSlice(ID_OFFSET, copySize), 0, copySize);
    }

    /**
     * Native support extracted from CServerApp::dispatchUnitTargetSpell @005041D6 packet field writes.
     */
    public static EffectAction prepareForUnitTargetSpell(Unit caster, Spell spell, Token target, short castDelayTicks) {
        EffectAction action = prepareSpellEffectActionForCaster(caster);
        byte[] actionPayload = baseSpellEffectPayload(caster, spell);
        if (target != null) {
            writePayloadWord(actionPayload, 3, target.idFull);
            writePayloadWord(actionPayload, 5, castDelayTicks);
        }
        action.playerID.set(0);
        action.payload.set(actionPayload);
        return action;
    }

    /**
     * Native support extracted from CServerApp::dispatchPointTargetSpell @005042BA packet field writes.
     */
    public static EffectAction prepareForPointTargetSpell(
            Unit caster,
            Spell spell,
            TargetHandle targetHandle,
            short castDelayTicks
    ) {
        EffectAction action = prepareSpellEffectActionForCaster(caster);
        byte[] actionPayload = baseSpellEffectPayload(caster, spell);
        actionPayload[3] = (byte) targetHandle.getX();
        actionPayload[4] = (byte) targetHandle.getY();
        writePayloadWord(actionPayload, 5, castDelayTicks);
        action.playerID.set(0);
        action.payload.set(actionPayload);
        return action;
    }

    /**
     * Native support extracted from CServerApp::sendEffectTokenVisualAction @00504524 packet field writes.
     */
    public static EffectAction prepareForEffectTokenVisual(Token effectToken, int visualType) {
        EffectAction action = global;
        action.ID.set(ACTION_ID);
        byte[] actionPayload = new byte[7];
        writePayloadWord(actionPayload, 0, effectToken.idFull);
        actionPayload[2] = (byte) effectToken.getTokenTypeId();
        actionPayload[3] = (byte) effectToken.m_pTargetHandle.getX();
        actionPayload[4] = (byte) effectToken.m_pTargetHandle.getY();
        writePayloadWord(actionPayload, 5, visualType & 0xFF);
        action.playerID.set(0);
        action.payload.set(actionPayload);
        return action;
    }

    /**
     * Native support extracted from CServerApp::dispatchUnitTargetSpell @005041D6 and
     * CServerApp::dispatchPointTargetSpell @005042BA packet field writes.
     */
    private static EffectAction prepareSpellEffectActionForCaster(Unit caster) {
        EffectAction action = caster.getTokenTypeId() == 0 ? EffectFromAction.global : global;
        action.ID.set(caster.getTokenTypeId() == 0 ? EffectFromAction.ACTION_ID : ACTION_ID);
        return action;
    }

    /**
     * Native support extracted from CServerApp::dispatchUnitTargetSpell @005041D6 and
     * CServerApp::dispatchPointTargetSpell @005042BA packet field writes.
     */
    private static byte[] baseSpellEffectPayload(Unit caster, Spell spell) {
        byte[] actionPayload = new byte[7];
        writePayloadWord(actionPayload, 0, caster.idFull);
        if (caster.getTokenTypeId() == 0) {
            actionPayload[0] = (byte) caster.m_pTargetHandle.getX();
            actionPayload[1] = (byte) caster.m_pTargetHandle.getY();
        }
        actionPayload[2] = (byte) (((spell.id & 0xFF) * 2 + 8) & 0xFF);
        return actionPayload;
    }

    /**
     * Native support extracted from CServerApp::dispatchUnitTargetSpell @005041D6,
     * CServerApp::dispatchPointTargetSpell @005042BA, and
     * CServerApp::sendEffectTokenVisualAction @00504524 packet field writes.
     */
    private static void writePayloadWord(byte[] actionPayload, int offset, int value) {
        actionPayload[offset] = (byte) value;
        actionPayload[offset + 1] = (byte) (value >>> Byte.SIZE);
    }

    /**
     * vtbl +0x04: EffectAction::Clone @005414A0.
     * Fully ported.
     */
    @Override
    public EffectAction Clone() {
        return new EffectAction(this);
    }

    /**
     * vtbl +0x10: EffectAction::getWireSize @00541520.
     * Port name: GetPayloadSize.
     * Fully ported.
     */
    @Override
    public int GetPayloadSize() {
        return 0x08;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00411864.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        if (ID.get() == ACTION_ID) {
            byte[] actionPayload = payload.get();
            int effectType = MapVisualObject.u8(actionPayload, 2);
            if ((effectType & 1) != 0 && MapVisualObject.hasProjectileInfo(effectType)) {
                int sourceX = MapVisualObject.pixelCenterFromTile(MapVisualObject.u8(actionPayload, 3));
                int sourceY = MapVisualObject.pixelCenterFromTile(MapVisualObject.u8(actionPayload, 4));
                mapVisualObject.spawnProjectile(
                        sourceX,
                        sourceY,
                        sourceX,
                        sourceY,
                        effectType,
                        MapVisualObject.u8(actionPayload, 5),
                        (short) 0
                );
                return;
            }

            CGameObject caster = mapVisualObject.getObjectByToken((short) MapVisualObject.u16(actionPayload, 0));
            if (caster != null) {
                MapVisualObject.startSpellVisual(caster, actionPayload);
            }
        }
    }
}
