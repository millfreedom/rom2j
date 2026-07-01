package ua.millfreedom.rom2;

import ua.millfreedom.rom2.model.BuildingList;
import ua.millfreedom.rom2.model.SackList;
import ua.millfreedom.rom2.model.UnitList;
import ua.millfreedom.rom2.model.VirtualCasterList;
import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.model.enums.UnitActionState;
import ua.millfreedom.rom2.model.spell.Spell;
import ua.millfreedom.rom2.model.spell.Spellbook;
import ua.millfreedom.rom2.model.spell.SpellEffectList;
import ua.millfreedom.rom2.model.unit.Unit;

import java.util.ArrayList;

/**
 * Native object-list aggregate owned by GameServer::objects.
 */
public class GameServerObjectLists {
    //0x00
    public BuildingList buildings;
    //0x04
    public SpellEffectList spellEffects;
    //0x08
    public SackList sacks;
    //0x0C
    public UnitList corpses;
    //0x10
    public final VirtualCasterList virtualCasters = new VirtualCasterList();
    //0x2C
    public final CustomList<Unit> transientCasterUnits = new CustomList<>(Unit.class);

    /**
     * Native: GameServerObjectLists::GameServerObjectLists @004F01CE.
     * Fully ported.
     */
    public GameServerObjectLists() {
    }

    /**
     * Native support extracted from GameServer::Start @004EB356 object-list allocation block.
     */
    public void allocateCorpseListForServerStart() {
        corpses = new UnitList();
    }

    /**
     * Native: GameServerObjectLists::clearOwnedObjectLists @004F02A0.
     * Fully ported.
     */
    public void clearOwnedObjectLists() {
        if (sacks != null) {
            sacks.sacks.clear();
            sacks = null;
        }
        if (corpses != null) {
            corpses.clear();
            corpses = null;
        }
        if (spellEffects != null) {
            spellEffects.clear();
            spellEffects = null;
        }
        if (buildings != null) {
            buildings.clear();
            buildings = null;
        }
        virtualCasters.clear();
        transientCasterUnits.clear();
    }

    /**
     * Native: GameServerObjectLists::clearLoadedWorldObjectLists @004F03E9.
     * Fully ported.
     */
    public void clearLoadedWorldObjectLists() {
        if (sacks != null) {
            sacks.sacks.clear();
        }
        sacks = null;
        buildings.clear();
        buildings = null;
        spellEffects.clear();
        spellEffects = null;
        corpses.clear();
        virtualCasters.clear();
        transientCasterUnits.clear();
    }

    /**
     * Native support extracted from GameServer::LoadMapByName @004EB715 object-list allocation block.
     */
    public void allocateMapObjectListsForMapLoad() {
        buildings = new BuildingList();
        spellEffects = new SpellEffectList();
        sacks = new SackList();
    }

    /**
     * Native support extracted from GameServer::Serialize @004EC732 extended read branch.
     */
    public void ensureBuildingsForExtendedSaveRead() {
        if (buildings == null) {
            buildings = new BuildingList();
        }
    }

    /**
     * Native support extracted from GameServer::Serialize @004EC732 extended read branch.
     */
    public void ensureSpellEffectsForExtendedSaveRead() {
        if (spellEffects == null) {
            spellEffects = new SpellEffectList();
        }
    }

    /**
     * Native support extracted from GameServer::Serialize @004EC732 extended read branch.
     */
    public void ensureSacksForExtendedSaveRead() {
        if (sacks == null) {
            sacks = new SackList();
        }
    }

    /**
     * Native: GameServerObjectLists::updatePeriodicBuildingAndSackState @004F0548.
     * Fully ported.
     */
    public void updatePeriodicBuildingAndSackState() {
        buildings.updateRegen();
        sacks.updateVisibilityAndPopulation();
    }

    /**
     * Native: GameServerObjectLists::createTransientCasterUnit @004F06AC.
     * Fully ported.
     */
    public Unit createTransientCasterUnit(int sourceX, int sourceY, int spellId, int skillLevel) {
        Unit unit = new Unit();
        unit.idFull = 0;
        transientCasterUnits.add(unit);
        unit.m_pTargetHandle.initFromBytes(sourceX, sourceY, Globals.worldMap);

        unit.spellbook = new Spellbook();
        Spell spell = new Spell((byte) spellId);
        unit.m_nMind = 0x1E;
        unit.skillData.skillLevels[Byte.toUnsignedInt(spell.getSphere())] = (short) skillLevel;
        unit.spellbook.setAt(spellId, spell);
        unit.spell = spell;
        unit.attackChargeTicks = 1;
        unit.attackRelaxTicks = 1;
        return unit;
    }

    /**
     * Native: GameServerObjectLists::queueTransientTargetSpellCast @004F0832.
     * Fully ported.
     */
    public void queueTransientTargetSpellCast(int sourceX, int sourceY, Unit targetUnit, int spellId, int skillLevel) {
        Unit unit = createTransientCasterUnit(sourceX, sourceY, spellId, skillLevel);
        unit.state = UnitActionState.CAST_SPELL;
        unit.actionTarget = targetUnit;
    }

    /**
     * Native: GameServerObjectLists::queueTransientPointSpellCast @004F086F.
     * Fully ported.
     */
    public void queueTransientPointSpellCast(int sourceX, int sourceY, int targetX, int targetY, int spellId,
                                             int skillLevel) {
        Unit unit = createTransientCasterUnit(sourceX, sourceY, spellId, skillLevel);
        unit.state = UnitActionState.USE_SKILL;
        unit.skillTargetX = targetX & 0xFF;
        unit.skillTargetY = targetY & 0xFF;
    }

    /**
     * Native: GameServerObjectLists::update @004F04CF.
     * Fully ported.
     */
    public void update() {
        for (Unit unit : new ArrayList<>(transientCasterUnits)) {
            unit.update();
            if (unit.actionReadyFlag != 0) {
                transientCasterUnits.remove(unit);
            }
        }
        spellEffects.update();
        corpses.publishNewlyVisibleUnits();
    }
}
