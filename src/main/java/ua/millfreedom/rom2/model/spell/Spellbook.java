package ua.millfreedom.rom2.model.spell;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.container.CustomList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Spellbook implements MfcSerializable {
    // Native global availableSpellMaskBySpellId @005F8E98.
    // Index is SpellId.id; value is the spell panel/available-spell mask bit.
    private static final int[] AVAILABLE_SPELL_MASK_BY_SPELL_ID = {
            0x00000000, 0x00000001, 0x00000002, 0x00000004,
            0x00000008, 0x00001000, 0x00002000, 0x00004000,
            0x00008000, 0x00000000, 0x00000800, 0x00000400,
            0x00000200, 0x00000100, 0x00000000, 0x00000000,
            0x00800000, 0x00400000, 0x00200000, 0x00100000,
            0x00000020, 0x00000040, 0x00020000, 0x00040000,
            0x00000010, 0x00010000, 0x00000080, 0x00080000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000
    };

    //0x04
    public final CustomList<Spell> spells = new CustomList<>(Spell.class, 1);
    //0x18
    public int lastFoundSpellIndex;

    /**
     * Native: Spellbook::New @0053A600.
     * Fully ported.
     */
    public Spellbook() {
    }

    /**
     * Native: Spellbook::Find @0051c391.
     * Fully ported.
     */
    public Spell find(int index) {
        if (index >= spells.size()) {
            return null;
        }
        Spell spell = spells.get(index);
        if (spell != null) {
            lastFoundSpellIndex = index;
        }
        return spell;
    }

    /**
     * Native: Spellbook::SetAt @0051c29e.
     * Fully ported.
     */
    public void setAt(int index, Spell spell) {
        while (spells.size() <= index) {
            spells.add(null);
        }
        spells.set(index, spell);
    }

    /**
     * Native: Spellbook::ClearAt @0051C334.
     * Fully ported.
     */
    public void clearAt(int index) {
        spells.set(index, null);
    }

    /**
     * Native: Spellbook::CopyFrom @0051C1B8.
     * Fully ported.
     */
    public void copyFrom(Spellbook source) {
        List<Spell> sourceSpells = new ArrayList<>(source.spells);
        spells.clear();
        for (Spell sourceSpell : sourceSpells) {
            spells.add(new Spell(sourceSpell));
        }
        lastFoundSpellIndex = source.lastFoundSpellIndex;
    }

    /**
     * Native: Spellbook::UpdatePrismaticCasterStats @0051C3E4.
     * Fully ported.
     */
    public void updatePrismaticCasterStats(Unit caster) {
        for (int i = 1; i < spells.size(); i++) {
            Spell spell = spells.get(i);
            if (spell != null) {
                spell.updatePrismaticCasterStats(caster);
            }
        }
    }

    /**
     * Native: Spellbook::GetSpellbookMask @0051C4A4.
     * Fully ported.
     */
    public int getSpellbookMask() {
        int mask = 0;
        for (int index = 1; index < spells.size(); index++) {
            if (spells.get(index) != null) {
                mask |= 1 << (index & 0x1F);
            }
        }
        return mask;
    }

    /**
     * Native: Spellbook::GetAvailableSpellMask @0051C445.
     * Fully ported.
     */
    public int getAvailableSpellMask() {
        int mask = 0;
        for (int index = 1; index < spells.size(); index++) {
            if (spells.get(index) != null) {
                mask |= availableSpellMaskBitForSpellId(index);
            }
        }
        return mask;
    }

    /**
     * Native support extracted from Spellbook::GetAvailableSpellMask @0051C445 and
     * MapVisualObject::UpdateSelectionState @004167C2, using global availableSpellMaskBySpellId @005F8E98.
     * Fully ported.
     */
    public static int availableSpellMaskBitForSpellId(int spellId) {
        return AVAILABLE_SPELL_MASK_BY_SPELL_ID[spellId];
    }

    /**
     * Native: Spellbook::Serialize @0051C505.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        if (!ar.isStoring()) {
            lastFoundSpellIndex = ar.readInt();
        } else {
            ar.writeInt(lastFoundSpellIndex);
        }
        ar.serialize(spells);
    }
}
