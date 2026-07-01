package ua.millfreedom.rom2.model.spell;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.Effect;
import ua.millfreedom.rom2.model.Token;
import ua.millfreedom.rom2.model.unit.Unit;

import java.io.IOException;

public class PointEffect extends SpellEffect {
    //0x48
    public Token targetToken;
    //0x4C
    public Effect payload;

    /**
     * Native: PointEffect::New @005178A1.
     * Fully ported.
     */
    public PointEffect() {
        payload = null;
        targetToken = null;
    }

    /**
     * Native: PointEffect::PointEffect @005178D4.
     * Fully ported.
     */
    public PointEffect(Effect payload, Token targetToken) {
        this.payload = payload;
        this.targetToken = targetToken;
        m_pTargetHandle.assignFrom(targetToken.m_pTargetHandle);
    }

    /**
     * Native: PointEffect::operator>> @00517885 (ReadObject path).
     * Fully ported.
     */
    public static PointEffect readFromArchive(CArchive ar) throws IOException {
        return ar.readObject(PointEffect.class);
    }

    /**
     * vtbl +0x08: PointEffect::serialize @0051C7E5.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        super.serialize(ar);
        if (!ar.isStoring()) {
            payload = Effect.readFromArchive(ar);
            targetToken = (Token) restoreMappedPointer(ar.readInt());
        } else {
            ar.writeObject(payload);
            ar.writeInt(Utils.encodePointerLike(targetToken));
        }
    }

    /**
     * vtbl +0x18: PointEffect::update @00542D00, dispatches PointEffect::applyPayloadToTarget @00517949.
     * Fully ported.
     */
    @Override
    public Object update() {
        applyPayloadToTarget();
        return this;
    }

    /**
     * Native: PointEffect::applyPayloadToTarget @00517949.
     * Fully ported.
     */
    private void applyPayloadToTarget() {
        if (payload != null && payload.sourceUnit != null && payload.sourceUnit.owner == null) {
            payload.sourceUnit = null;
        }

        if (targetToken.isUnitToken() == 0) {
            if (targetToken.isBuildingToken() != 0) {
                payload.applyToTarget(targetToken);
            }
        } else {
            Unit targetUnit = (Unit) targetToken;
            if (targetUnit != null && targetUnit.owner != null) {
                payload.applyToTarget(targetToken);
            }
            if (sourceCaster != null && damageAttributionEnabled != 0) {
                if (!sourceCaster.hasNoTableLine()) {
                    if (sourceCaster.owner != null) {
                        targetUnit.lastDamageSource = sourceCaster;
                        targetUnit.killCreditSkillContext = payload.key & 0xFF;
                    }
                } else {
                    targetUnit.lastDamageSource = null;
                }
                if (targetUnit != null
                        && sourceCaster != null
                        && sourceCaster.owner != null
                        && targetUnit.owner != null) {
                    Globals.gameServer.missionScriptRuntime.recordUnitEngagement(sourceCaster, targetUnit, 0);
                }
            }
        }
        completedFlag = 1;
    }

    /**
     * vtbl +0x24: PointEffect::restoreContext @0051C861.
     * Fully ported.
     */
    @Override
    public void restoreContext() {
        super.restoreContext();
        if (payload != null) {
            payload.restoreContext();
        }
    }

    /**
     * not ported.
     */
    @Override
    public SpellTokenKind getTokenKind() {
        return SpellTokenKind.SPELL;
    }

    /**
     * not ported.
     */
    @Override
    public int getSpellIdIndex() {
        return key & 0xFF;
    }
}
