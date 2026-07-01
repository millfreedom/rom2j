package ua.millfreedom.rom2.model.spell;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.TargetHandle;
import ua.millfreedom.rom2.model.Token;
import ua.millfreedom.rom2.model.unit.Unit;

import java.io.IOException;

public class SpellEffect extends Token {
    //0x10
    // Java keeps target coordinates in Token.m_pTargetHandle; MapVisualObject cancel paths also use the native pointer
    // value at this offset as a local spell-effect lifetime counter.
    public int localCancelTargetHandleValue = 1;

    //0x3C
    public Unit sourceCaster;
    //0x40
    public int completedFlag;
    //0x41
    public int damageAttributionEnabled = 1;
    //0x42
    public int visiblePlayerMask;
    //0x44
    public int lastPublishedVisiblePlayerMask;
    //0x46
    public int deferredAreaEffectActionMask;

    /**
     * Native: SpellEffect::SpellEffect @00517719.
     * Fully ported.
     */
    public SpellEffect() {
        typeID = 0;
        sourceCaster = null;
        completedFlag = 0;
        damageAttributionEnabled = 1;
        visiblePlayerMask = 0;
        lastPublishedVisiblePlayerMask = 0;
        deferredAreaEffectActionMask = 0;
    }

    /**
     * Native: SpellEffect::SpellEffect @00517774.
     * Fully ported.
     */
    public SpellEffect(TargetHandle targetHandle) {
        this();
        m_pTargetHandle.assignFrom(targetHandle);
    }

    /**
     * Native: SpellEffect::operator>> @005176FD (ReadObject path).
     * Fully ported.
     */
    public static SpellEffect readFromArchive(CArchive ar) throws IOException {
        return ar.readObject(SpellEffect.class);
    }

    /**
     * Native: SpellEffect::serialize @0051C6B5.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        super.serialize(ar);
        if (!ar.isStoring()) {
            completedFlag = ar.readByte() & 0xFF;
            damageAttributionEnabled = ar.readByte() & 0xFF;
        } else {
            ar.writeByte(completedFlag);
            ar.writeByte(damageAttributionEnabled);
        }
    }

    /**
     * Native: restoreMappedPointer @005432C0.
     * Fully ported.
     */
    public static Object restoreMappedPointer(Object tokenOrRef) {
        return Globals.gameServer.lookupPointerMapOrNull(tokenOrRef);
    }

    /**
     * not ported.
     */
    public SpellTokenKind getTokenKind() {
        return SpellTokenKind.OTHER;
    }

    /**
     * not ported.
     */
    public int getSpellIdIndex() {
        return key & 0xFF;
    }

    /**
     * Native support extracted from MapVisualObject::IssueCastSpellAtPoint @0041918E,
     * MapVisualObject::IssueCastSpellAtUnit @004195FA, and MapVisualObject::ActivateSpellPanelSlot @00419A54.
     */
    public boolean consumeLocalCancelTargetHandle() {
        if (localCancelTargetHandleValue == 1) {
            return true;
        }
        localCancelTargetHandleValue--;
        return false;
    }

    /**
     * vtbl +0x18: SpellEffect::update @005177D5, base no-op.
     * Fully ported.
     */
    @Override
    public Object update() {
        return null;
    }

    /**
     * vtbl +0x38: SpellEffect::applyPayloadToObject @005177E0, base no-op.
     * Fully ported.
     */
    public void applyPayloadToObject(Token target) {
        // no-op in base SpellEffect
    }
}
