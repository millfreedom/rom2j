package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.Effect;
import ua.millfreedom.rom2.model.Token;
import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Raw packet model for native Fixed3ByteAction.
 */
public class Fixed3ByteAction extends CGameAction {
    public static final Fixed3ByteAction global = new Fixed3ByteAction();

    //0x0A
    public final Property<Integer> unitTokenId = u16(BODY_OFFSET);
    //0x0C
    public final Property<Integer> effectTypeId = u8(BODY_OFFSET + Short.BYTES);

    /**
     * Native: Fixed3ByteAction::Fixed3ByteAction @0050C3D7.
     * Fully ported.
     */
    public Fixed3ByteAction() {
        super();
        effectTypeId.set(0);
    }

    /**
     * Native support extracted from CServerApp::AddEffect @005044BA packet field writes.
     * Fully ported.
     */
    public static Fixed3ByteAction prepareForEffectChange(Effect effect, Token token, GameActionId actionId) {
        Fixed3ByteAction action = actionId == GameActionId.UNKNOWN_ACTION_00
                ? EffectHangAction.global
                : EffectGoneAction.global;
        action.ID.set(actionId == GameActionId.UNKNOWN_ACTION_00 ? EffectHangAction.ACTION_ID : actionId.id);
        action.playerID.set(0);
        action.unitTokenId.set(token.idFull);
        action.effectTypeId.set(effect.getTokenTypeId());
        return action;
    }

    /**
     * Native: Fixed3ByteAction::Fixed3ByteAction @0050C3FD.
     * Fully ported.
     */
    public Fixed3ByteAction(Fixed3ByteAction from) {
        super();
        int copySize = GetPayloadSize();
        PutSlice(ID_OFFSET, from.GetSlice(ID_OFFSET, copySize), 0, copySize);
    }

    /**
     * vtbl +0x04: Fixed3ByteAction::Clone @00541560.
     * Fully ported.
     */
    @Override
    public Fixed3ByteAction Clone() {
        return new Fixed3ByteAction(this);
    }

    /**
     * vtbl +0x10: Fixed3ByteAction::getWireSize @005415E0.
     * Port name: GetPayloadSize.
     * Fully ported.
     */
    @Override
    public int GetPayloadSize() {
        return 0x04;
    }
}
