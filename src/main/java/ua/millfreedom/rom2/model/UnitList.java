package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.enums.UnitActionState;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.unit.UnitDirtyFlags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Native shape:
 * +0x00 vtbl
 * +0x04 CList<Unit> list
 * <p>
 * Native methods:
 * - New @0052B19A
 * - AddTail @0052B22A
 * - AddAndAssignRuntimeId @0052B246
 * - AddAndAssignScenarioId @0052B281
 * - FindByTokenId @0052B2BD
 * - updateActiveUnits @0052B459
 * - ClearVisibilityMaskForPlayer @0052BE01
 * - publishNewlyVisibleUnits @0052B321
 * - Serialize @0052C500
 * - RestoreContext @0052C51C
 */
public class UnitList extends CustomList<Unit> {
    private static final int SCENARIO_OBJECT_ID_BASE = 0x6000;

    /**
     * Native: UnitList::New @0052B19A.
     * Fully ported.
     */
    public UnitList() {
        super(Unit.class);
    }

    /**
     * Native: UnitList::AddAndAssignScenarioId @0052B281.
     * Fully ported.
     */
    public void addAndAssignScenarioId(Unit unit) {
        add(unit);
        unit.idFull = (unit.scenarioObjectId + SCENARIO_OBJECT_ID_BASE) & 0xFFFF;
    }

    /**
     * Native: UnitList::AddAndAssignRuntimeId @0052B246.
     * Fully ported.
     */
    public void addAndAssignRuntimeId(Unit unit) {
        add(unit);
        unit.idFull = Globals.gameServer.allocateNextFreeId() & 0xFFFF;
    }

    /**
     * vtbl +0x00: UnitList::Serialize @0052C500.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        super.serialize(ar);
    }

    /**
     * vtbl +0x04: UnitList::RestoreContext @0052C51C.
     * Fully ported.
     */
    public void restoreContext() {
        for (Unit unit : this) {
            unit.restoreContext();
        }
    }

    /**
     * Native: UnitList::FindByTokenId @0052B2BD.
     * Fully ported.
     */
    public Unit findByTokenId(int tokenId) {
        int nativeTokenId = (short) tokenId;
        for (Unit unit : this) {
            if (unit.idFull == nativeTokenId) {
                return unit;
            }
        }
        return null;
    }

    /**
     * Native: UnitList::ClearVisibilityMaskForPlayer @0052BE01.
     * Fully ported.
     */
    public void clearVisibilityMaskForPlayer(Player player) {
        for (Unit unit : this) {
            unit.word &= ~player.scanMask;
        }
    }

    /**
     * Native: UnitList::countInnRewardEligibleOwnedUnits @0052BE6E.
     * Fully ported.
     */
    public int countInnRewardEligibleOwnedUnits() {
        int count = 0;
        for (Unit unit : this) {
            int typeId = unit.getTokenTypeId() & 0xFF;
            if (unit.forceFinalCorpseStageOnDeath == 0
                    && -10 < unit.m_nHP
                    && 0x3F < typeId
                    && typeId < 99
                    && (typeId != 0x45 || (unit.face & 0xFF) != 1)
                    && typeId != 0x52
                    && typeId != 0x59) {
                count++;
            }
        }
        return count;
    }

    /**
     * Native: UnitList::updateActiveUnits @0052B459.
     * Fully ported.
     */
    public void updateActiveUnits() {
        for (Unit unit : new ArrayList<>(this)) {
            clearOrphanLastDamageSource(unit);
            publishNewlyVisibleUnit(unit);
            if (unit.state != UnitActionState.DEAD) {
                unit.update();
            }
            if (unit.state == UnitActionState.DEAD) {
                int questGroupKey = deathQuestGroupKey(unit);
                if (unit.owner != null) {
                    unit.owner.detachDeadUnitFromOwner(unit);
                }
                remove(unit);
                Globals.gameServer.objectLists.corpses.add(unit);
                unit.flushCorpseMigrationEffects();
                creditKillerForDeadUnit(unit);
                publishUnitDeathQuestMessages(unit, questGroupKey);
                releaseNonGroundCorpseTokenId(unit);
            }
        }
    }

    /**
     * Native support extracted from UnitList::updateActiveUnits @0052B459 stale last-damage-source owner check.
     * Fully ported.
     */
    private static void clearOrphanLastDamageSource(Unit unit) {
        Token source = unit.lastDamageSource;
        if (source != null && source.owner == null) {
            unit.lastDamageSource = null;
        }
    }

    /**
     * Native support extracted from UnitList::updateActiveUnits @0052B459 group-key death event precheck.
     * Fully ported.
     */
    private static int deathQuestGroupKey(Unit unit) {
        if (unit.unitGroup != null
                && unit.unitGroup.units.size() == 1
                && (unit.unitGroup.hostileGroupRelocationQuestFlag != 0 || unit.unitGroup.innGroupRelocationQuestFlag != 0)) {
            return unit.unitGroup.groupKey;
        }
        return 0;
    }

    /**
     * Native support extracted from UnitList::updateActiveUnits @0052B459 quest-death event tail.
     * Fully ported.
     */
    private static void publishUnitDeathQuestMessages(Unit unit, int questGroupKey) {
        int killerPlayerId = deathQuestKillerPlayerId(unit);
        if (unit.forceFinalCorpseStageOnDeath == 0) {
            Globals.questStorage.addQuestRelatedEntry(
                    4,
                    unit.idFull,
                    ((unit.getTokenTypeId() & 0xFFFF) << 16) | ((unit.face & 0xFF) << 24) | killerPlayerId
            );
        }
        if (questGroupKey != 0) {
            Globals.questStorage.addQuestRelatedEntry(5, questGroupKey, killerPlayerId);
        }
        Player owner = unit.owner;
        if (owner.playerEliminationQuestEnabled != 0
                && owner.ownedUnits.isEmpty()) {
            Globals.questStorage.addQuestRelatedEntry(0x0F, owner.playerId, killerPlayerId);
        }
    }

    /**
     * Native support extracted from UnitList::updateActiveUnits @0052B459 killer-player id packing.
     * Fully ported.
     */
    private static int deathQuestKillerPlayerId(Unit unit) {
        Token killer = unit.lastDamageSource;
        if (killer != null
                && isHumanoidTokenType(killer)
                && killer.owner != null) {
            return killer.owner.playerId & 0xFFFF;
        }
        return 0;
    }

    /**
     * Native support extracted from UnitList::updateActiveUnits @0052B459 killer stat and knowledge-credit tail.
     * Fully ported.
     */
    private static void creditKillerForDeadUnit(Unit deadUnit) {
        Token killerToken = deadUnit.lastDamageSource;
        if (killerToken == null) {
            return;
        }
        if (!isHumanoidTokenType(killerToken)) {
            publishHumanoidKilledByCreatureMessage(deadUnit, killerToken);
            return;
        }
        Unit killer = (Unit) killerToken;
        if (killer.isDying()) {
            deadUnit.lastDamageSource = null;
            return;
        }

        killer.creditOwnerForObjectValue(deadUnit);
        killer.awardKillSkillProgress(deadUnit, (byte) deadUnit.killCreditSkillContext);

        Player killerOwner = killer.owner;
        if (isHumanoidTokenType(deadUnit)) {
            publishPlayerKilledByPlayerMessage(deadUnit, killerOwner);
            if (Globals.gameServer.networkSessionActive != 0) {
                CServerApp.sendTwoDwordAction(
                        null,
                        GameActionId.PLAYER_KILL_ANNOUNCEMENT_ACTION_94,
                        deadUnit.owner.playerId,
                        killerOwner.playerId
                );
            }
            killerOwner.playerKillCount += 1;
        } else {
            killerOwner.creatureKillCount += 1;
        }

        CServerApp.netUpdate(killer, killerOwner, UnitDirtyFlags.CONTROLLED_OWNER_STATS.value, 0x0FFB, 0, 0);
        int serverId = deadUnit.serverID & 0xFFFF;
        if (serverId != 0 && serverId < 0x0A00) {
            int knowledge = killerOwner.knowledgeTable[serverId] & 0xFF;
            if (knowledge < 0x0E) {
                knowledge += 1;
                killerOwner.knowledgeTable[serverId] = (byte) knowledge;
                if ((knowledge & 1) == 0) {
                    CServerApp.sendPlayerKnowledgeAction(serverId, killerOwner);
                }
            }
        }
    }

    /**
     * Native support extracted from UnitList::updateActiveUnits @0052B459 non-humanoid killer message branch.
     * Fully ported.
     */
    private static void publishHumanoidKilledByCreatureMessage(Unit deadUnit, Token killer) {
        if (isHumanoidTokenType(deadUnit) && killer.isUnitToken() != 0 && ((Unit) killer).unitInfoLine != null) {
            Unit killerUnit = (Unit) killer;
            Globals.gameServer.pushMessage(String.format(
                    "Player %s killed by %s ID=%d at %d,%d",
                    deadUnit.owner.name,
                    killerUnit.unitInfoLine.name,
                    killer.idFull,
                    deadUnit.m_pTargetHandle.getX(),
                    deadUnit.m_pTargetHandle.getY()
            ));
        }
    }

    /**
     * Native support extracted from UnitList::updateActiveUnits @0052B459 humanoid killer message branch.
     * Fully ported.
     */
    private static void publishPlayerKilledByPlayerMessage(Unit deadUnit, Player killerOwner) {
        Globals.gameServer.pushMessage(String.format(
                "Player %s pkilled by %s at %d,%d",
                deadUnit.owner.name,
                killerOwner.name,
                deadUnit.m_pTargetHandle.getX(),
                deadUnit.m_pTargetHandle.getY()
        ));
    }

    /**
     * Native support extracted from UnitList::updateActiveUnits @0052B459 Token.typeID humanoid range checks.
     * Fully ported.
     */
    private static boolean isHumanoidTokenType(Token token) {
        int tokenTypeId = token.getTokenTypeId() & 0xFFFF;
        return tokenTypeId >= 0x21 && tokenTypeId <= 0x3F;
    }

    /**
     * Native support extracted from UnitList::updateActiveUnits @0052B459 ClearFlagBit tail.
     * Fully ported.
     */
    private static void releaseNonGroundCorpseTokenId(Unit unit) {
        if (unit.idFull != 0 && (unit.movementType & 0xFF) > 1) {
            Globals.gameServer.clearBitForId(unit.idFull);
            unit.idFull = 0;
        }
    }

    /**
     * Native: UnitList::updateRegenCorpsesAndDeferredDeaths @0052BBB7.
     * Fully ported.
     */
    public void updateRegenCorpsesAndDeferredDeaths(UnitList deferredDeathUnits) {
        updateActiveUnitRegen();
        updateCorpseDecayList();
        Map<Integer, Integer> forcedCorpseStageCounts = chooseForcedCorpseStageFinalizations(deferredDeathUnits);
        finalizeChosenDeferredDeaths(deferredDeathUnits, forcedCorpseStageCounts);
    }

    /**
     * Native support extracted from UnitList::updateRegenCorpsesAndDeferredDeaths @0052BBB7 active-unit regen loop.
     * Fully ported.
     */
    private void updateActiveUnitRegen() {
        for (Unit unit : this) {
            unit.updateRegen();
        }
    }

    /**
     * Native support extracted from UnitList::updateRegenCorpsesAndDeferredDeaths @0052BBB7 corpse decay loop.
     * Fully ported.
     */
    private static void updateCorpseDecayList() {
        for (Unit corpse : Globals.gameServer.objectLists.corpses) {
            corpse.updateCorpseDecay();
        }
    }

    /**
     * Native support extracted from UnitList::updateRegenCorpsesAndDeferredDeaths @0052BBB7 force-final-stage selection.
     * Fully ported.
     */
    private static Map<Integer, Integer> chooseForcedCorpseStageFinalizations(UnitList deferredDeathUnits) {
        Map<Integer, Integer> forcedCorpseStageCounts = new HashMap<>();
        for (Unit unit : deferredDeathUnits) {
            if (unit.forceFinalCorpseStageOnDeath != 0) {
                forcedCorpseStageCounts.merge(unit.forceFinalCorpseStageOnDeath, 1, Integer::sum);
            }
        }
        forcedCorpseStageCounts.replaceAll((ignored, count) -> Utils.randInclusive(1, 100) < count ? -1 : count);
        return forcedCorpseStageCounts;
    }

    /**
     * Native support extracted from UnitList::updateRegenCorpsesAndDeferredDeaths @0052BBB7 selected finalization loop.
     * Fully ported.
     */
    private static void finalizeChosenDeferredDeaths(
            UnitList deferredDeathUnits,
            Map<Integer, Integer> forcedCorpseStageCounts
    ) {
        for (Unit unit : deferredDeathUnits) {
            int key = unit.forceFinalCorpseStageOnDeath;
            if (key != 0 && forcedCorpseStageCounts.getOrDefault(key, 0) == -1) {
                forcedCorpseStageCounts.put(key, -2);
                unit.finalizeDeath();
            }
        }
    }

    /**
     * Native: UnitList::publishNewlyVisibleUnits @0052B321.
     * Fully ported.
     */
    public void publishNewlyVisibleUnits() {
        for (Unit unit : this) {
            publishNewlyVisibleUnit(unit);
        }
    }

    /**
     * Native support extracted from UnitList::publishNewlyVisibleUnits @0052B321 and
     * UnitList::updateActiveUnits @0052B459.
     * Fully ported support helper for the published-visibility branch.
     */
    private static void publishNewlyVisibleUnit(Unit unit) {
        if (unit.visiblePlayerMask != unit.lastPublishedVisiblePlayerMask
                && Globals.gameServer.networkSessionActive != 0) {
            for (int playerIndex = 0; playerIndex < 0x10; playerIndex++) {
                int playerMask = 1 << playerIndex;
                if ((unit.visiblePlayerMask & playerMask) != 0
                        && (unit.lastPublishedVisiblePlayerMask & playerMask) == 0) {
                    Player targetPlayer = Globals.gameServer.playerList.getPlayerById(playerIndex + 0x10);
                    if (targetPlayer != null) {
                        CServerApp.netUpdate(
                                unit,
                                targetPlayer,
                                unit.deferredNetUpdateFlagsByPlayerId[playerIndex],
                                0x0FFB,
                                0,
                                0
                        );
                    }
                }
            }
            unit.lastPublishedVisiblePlayerMask = unit.visiblePlayerMask;
        }
    }
}
