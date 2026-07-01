package ua.millfreedom.rom2.model.spell;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.TargetHandle;

import java.io.IOException;

public class SpellTransport extends SpellEffect {
    //0x48
    public SpellEffect primaryEffect;
    //0x4C
    public SpellEffect secondaryEffect;
    //0x50
    public int travelTicks;

    /**
     * Native: SpellTransport::SpellTransport @00518CEB.
     * Fully ported.
     */
    public SpellTransport() {
        primaryEffect = null;
        secondaryEffect = null;
    }

    /**
     * Native: SpellTransport::SpellTransport @00518D1E.
     * Fully ported.
     */
    public SpellTransport(SpellEffect primaryEffect, TargetHandle sourceTargetHandle, int speed) {
        super(sourceTargetHandle);
        this.primaryEffect = primaryEffect;
        secondaryEffect = null;
        travelTicks = sourceTargetHandle.euclideanDistanceByPackedPosition(primaryEffect.m_pTargetHandle) / speed;
    }

    /**
     * Native: SpellTransport::operator>> @00518CCF (ReadObject path).
     * Fully ported.
     */
    public static SpellTransport readFromArchive(CArchive ar) throws IOException {
        return ar.readObject(SpellTransport.class);
    }

    /**
     * Fully ported. vtbl +0x08: SpellTransport::serialize @0051C718.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        super.serialize(ar);
        if (!ar.isStoring()) {
            primaryEffect = SpellEffect.readFromArchive(ar);
            secondaryEffect = SpellEffect.readFromArchive(ar);
            travelTicks = ar.readUShort();
        } else {
            ar.writeObject(primaryEffect);
            ar.writeObject(secondaryEffect);
            ar.writeShort(travelTicks);
        }
    }

    /**
     * Fully ported. vtbl +0x18: SpellTransport::update @00518EFD.
     */
    @Override
    public Object update() {
        travelTicks = (short) (travelTicks - 1);
        if ((short) travelTicks < 1) {
            Globals.gameServer.objectLists.spellEffects.add(primaryEffect == null ? secondaryEffect : primaryEffect);
            primaryEffect = null;
            secondaryEffect = null;
            completedFlag = 1;
        }
        return null;
    }

    /**
     * Fully ported. vtbl +0x24: SpellTransport::restoreContext @0051C79E.
     */
    @Override
    public void restoreContext() {
        super.restoreContext();
        if (primaryEffect != null) {
            primaryEffect.restoreContext();
        }
        if (secondaryEffect != null) {
            secondaryEffect.restoreContext();
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
