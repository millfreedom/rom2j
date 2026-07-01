package ua.millfreedom.rom2.model.world;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.UnitList;
import ua.millfreedom.rom2.model.UnitGroup;
import ua.millfreedom.rom2.model.spell.SpellEffect;
import ua.millfreedom.rom2.model.unit.Unit;

import java.util.Arrays;

/**
 * Native support state embedded in CWorldMap at 0x92ECC.
 */
public final class WorldMapUnitVisibilityState {
    //0x0000
    public final int[][] coarseUnitCountGrid0x0000 = new int[16][16];

    //0x0400
    public final int[][] coarseUnitMaskGrid0x0400 = new int[34][34];

    //0x1610
    public UnitList activeUnits0x1610;

    //0x1614
    public int unitsCount1_0x1614;

    //0x1618
    public int unitsCount2_0x1618;

    //0x161C
    public int field0x161C;

    //0x1620
    public int field0x1620;

    //0x1624
    public int field0x1624;

    /**
     * Fully ported. Native: WorldMapUnitVisibilityState::WorldMapUnitVisibilityState @0055AA75.
     */
    public WorldMapUnitVisibilityState() {
        initialize();
    }

    /**
     * Fully ported. Native: WorldMapUnitVisibilityState::initialize @0055AA96.
     */
    public void initialize() {
        for (int[] row : coarseUnitCountGrid0x0000) {
            Arrays.fill(row, 0);
        }
    }

    /**
     * Fully ported. Native: WorldMapUnitVisibilityState::markUnitVisibilityFootprint @0055ACA2.
     */
    public void markUnitVisibilityFootprint(Unit unit) {
        Player owner = unit.owner;
        if (owner.isActive == 0) {
            int coarseX = (unit.m_pTargetHandle.getX() >> 3) + 1;
            int coarseY = (unit.m_pTargetHandle.getY() >> 3) + 1;
            int ownerVisibilityMask = owner.scanMaskMirror & 0xFFFF;
            for (int xOffset = -2; xOffset <= 2; xOffset++) {
                for (int yOffset = -2; yOffset <= 2; yOffset++) {
                    orCoarseUnitMaskWord(coarseX + xOffset, coarseY + yOffset, ownerVisibilityMask);
                }
            }
            return;
        }

        if ((short) unit.m_nHP < unit.m_nMaxHP) {
            unit.unitGroup.missionState.markDamagedUnitMissionUpdatePending();
        }
    }

    /**
     * Fully ported. Native: WorldMapUnitVisibilityState::resetVisibilityState @0055B879.
     */
    public void resetVisibilityState() {
        for (int[] row : coarseUnitMaskGrid0x0400) {
            Arrays.fill(row, 0);
        }

        for (Player player : Globals.gameServer.playerList.players) {
            if (player.isActive != 0) {
                for (UnitGroup group : player.unitGroups) {
                    group.missionState.clearDamagedUnitMissionUpdatePending();
                }
            }
        }

        for (Unit unit : Globals.gameServer.activeUnits) {
            unit.visiblePlayerMask = 0;
        }
    }

    /**
     * Fully ported. Native: WorldMapUnitVisibilityState::rebuildUnitVisibilityState @0055B5F1.
     */
    public void rebuildUnitVisibilityState() {
        resetVisibilityState();
        unitsCount1_0x1614 = 0;
        field0x1624 = 0;

        if (activeUnits0x1610 != null) {
            for (Unit unit : activeUnits0x1610) {
                if (unit != null) {
                    markUnitVisibilityFootprint(unit);
                }
            }
        }

        for (Player player : Globals.gameServer.playerList.players) {
            if (player.isActive == 0 && player.controlledUnit instanceof Unit controlledUnit) {
                markUnitVisibilityFootprint(controlledUnit);
            }
        }

        if (activeUnits0x1610 != null) {
            for (Unit unit : activeUnits0x1610) {
                applyActiveUnitVisibilityState(unit);
            }
            unitsCount2_0x1618 = activeUnits0x1610.size() - unitsCount1_0x1614;
        }

        for (Unit unit : Globals.gameServer.objectLists.corpses) {
            applyCorpseVisibilityMask(unit);
        }

        if (Globals.gameServer.objectLists.spellEffects != null) {
            for (SpellEffect spellEffect : Globals.gameServer.objectLists.spellEffects) {
                applySpellEffectVisibilityMask(spellEffect);
            }
        }
    }

    /**
     * Native support extracted from WorldMapUnitVisibilityState::markUnitVisibilityFootprint @0055ACA2.
     * The native 34x34 mask grid is embedded after the 16x16 count grid, so edge footprints may alias the preceding
     * count-grid words instead of trapping.
     */
    private void orCoarseUnitMaskWord(int coarseX, int coarseY, int ownerVisibilityMask) {
        int countGridWords = 16 * 16;
        int maskGridColumns = 34;
        int maskGridWords = maskGridColumns * maskGridColumns;
        int nativeWordIndex = countGridWords + coarseX * maskGridColumns + coarseY;

        if (nativeWordIndex >= countGridWords && nativeWordIndex < countGridWords + maskGridWords) {
            int maskWordIndex = nativeWordIndex - countGridWords;
            coarseUnitMaskGrid0x0400[maskWordIndex / maskGridColumns][maskWordIndex % maskGridColumns]
                    |= ownerVisibilityMask;
            return;
        }

        if (nativeWordIndex >= 0 && nativeWordIndex < countGridWords) {
            coarseUnitCountGrid0x0000[nativeWordIndex / 16][nativeWordIndex % 16] |= ownerVisibilityMask;
            return;
        }

        throw new IndexOutOfBoundsException(
                "Native visibility footprint write outside WorldMapUnitVisibilityState: "
                        + "coarseX=" + coarseX + ", coarseY=" + coarseY
        );
    }

    /**
     * Fully ported native support extracted from
     * WorldMapUnitVisibilityState::applyActiveUnitVisibilityState @0055B406.
     */
    private void applyActiveUnitVisibilityState(Unit unit) {
        int playerMask = coarseVisibilityMaskForUnit(unit);
        if (playerMask == 0) {
            if (unit.unitGroup.missionState.scenarioScriptReferencedFlag != 0) {
                unit.unitGroup.missionState.markDamagedUnitMissionUpdatePending();
                unitsCount1_0x1614++;
                field0x1624++;
            }
            return;
        }
        unit.unitGroup.missionState.markDamagedUnitMissionUpdatePending();
        unitsCount1_0x1614++;
        unit.visiblePlayerMask = playerMask & 0xFFFF;
    }

    /**
     * Fully ported native support extracted from
     * WorldMapUnitVisibilityState::rebuildUnitVisibilityState @0055B5F1 corps branch.
     */
    private void applyCorpseVisibilityMask(Unit unit) {
        int playerMask = coarseVisibilityMaskForUnit(unit);
        if (playerMask != 0) {
            unit.visiblePlayerMask = playerMask & 0xFFFF;
        }
    }

    /**
     * Fully ported native support extracted from WorldMapUnitVisibilityState::applyActiveUnitVisibilityState @0055B406
     * and WorldMapUnitVisibilityState::rebuildUnitVisibilityState @0055B5F1 corps branch.
     */
    private int coarseVisibilityMaskForUnit(Unit unit) {
        int coarseX = (unit.m_pTargetHandle.getX() >> 3) + 1;
        int coarseY = (unit.m_pTargetHandle.getY() >> 3) + 1;
        return coarseUnitMaskGrid0x0400[coarseX][coarseY];
    }

    /**
     * Fully ported native support extracted from
     * WorldMapUnitVisibilityState::applySpellEffectVisibilityMask @0055B507.
     */
    public void applySpellEffectVisibilityMask(SpellEffect spellEffect) {
        int coarseX = spellEffect.m_pTargetHandle.getX() >> 3;
        int coarseY = spellEffect.m_pTargetHandle.getY() >> 3;
        spellEffect.visiblePlayerMask = (
                coarseUnitMaskGrid0x0400[coarseX + 2][coarseY + 2]
                        | coarseUnitMaskGrid0x0400[coarseX + 2][coarseY]
                        | coarseUnitMaskGrid0x0400[coarseX][coarseY + 2]
                        | coarseUnitMaskGrid0x0400[coarseX][coarseY]
        ) & 0xFFFF;
    }
}
