package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CStaticDataMgr;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.column.MagicColumn;
import ua.millfreedom.rom2.model.column.UnitColumn;
import ua.millfreedom.rom2.model.column.WorldItemColumn;
import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.model.enums.EffectId;
import ua.millfreedom.rom2.model.enums.SpellId;
import ua.millfreedom.rom2.model.quest.Quest;
import ua.millfreedom.rom2.model.quest.Quest_1;
import ua.millfreedom.rom2.model.quest.Quest_2;
import ua.millfreedom.rom2.model.quest.Quest_3;
import ua.millfreedom.rom2.model.quest.Quest_4;
import ua.millfreedom.rom2.model.quest.Quest_5;
import ua.millfreedom.rom2.model.quest.Quest_6;
import ua.millfreedom.rom2.model.quest.Quest_8;
import ua.millfreedom.rom2.model.quest.Quest_9;
import ua.millfreedom.rom2.model.quest.Quest_10;
import ua.millfreedom.rom2.model.quest.Quest_11;
import ua.millfreedom.rom2.model.quest.Quest_12;
import ua.millfreedom.rom2.model.quest.Quest_13;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.unit.UnitDirtyFlags;
import ua.millfreedom.rom2.model.unit.UnitInfo;
import ua.millfreedom.rom2.model.unit.humanoid.Humanoid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Inn extends Building {
    private static final int INN_ACCEPTED_QUEST_CANCEL_SELECTION = 0xAAAAAAAA;
    private static final int ACCEPTED_QUEST_SPAWNED_UNIT_REWARD_HASH = 0xFFFD;
    private static final int ACCEPTED_QUEST_TRAINING_REWARD_HASH = 0xFFFE;
    private static final int ACCEPTED_QUEST_GOLD_REWARD_HASH = 0xFFFF;
    private static final int ACCEPTED_QUEST_SPAWNED_UNIT_DIAMETER = 5;
    private static final int ACCEPTED_QUEST_POTION_REWARD_MIN_SKILL_BONUS = 18_000_000;
    private static final int ACCEPTED_QUEST_POTION_REWARD_MIN_BUDGET = 999_999;
    private static final int ACCEPTED_QUEST_MAGIC_UPGRADE_MIN_BUDGET = 999_999;
    private static final int ACCEPTED_QUEST_MAGIC_UPGRADE_MIN_HIGH_SKILLS = 4;
    private static final int ACCEPTED_QUEST_MAGIC_UPGRADE_HIGH_SKILL_LEVEL = 0x4B;
    private static final int INN_QUEST_CANDIDATE_LIMIT = 0x100;
    private static final int ALL_UNIT_UPDATE_FLAGS = -1;
    private static final int EQUIPMENT_BROADCAST_MASK = 0x0FFB;
    private static final String[] ACCEPTED_QUEST_STAT_POTION_REWARD_NAMES = {
            "Potion Body",
            "Potion Mind",
            "Potion Spirit",
            "Potion Reaction"
    };
    private static final String[] ACCEPTED_QUEST_SCROLL_REWARD_NAMES = {
            "Scroll Fire Ball",
            "Scroll Fire Wall",
            "Scroll Protection from Fire",
            "Scroll Poison Cloud",
            "Scroll Blizzard",
            "Scroll Protection from Water",
            "Scroll Acid Stream",
            "Scroll Prismatic Spray",
            "Scroll Invisibility",
            "Scroll Protection from Air",
            "Scroll Wall of Earth",
            "Scroll Stone Curse",
            "Scroll Protection from Earth",
            "Scroll Bless",
            "Scroll Haste",
            "Scroll Teleport",
            "Scroll Heal",
            "SuperScroll Fire Ball",
            "SuperScroll Fire Wall",
            "SuperScroll Protection from Fire",
            "SuperScroll Poison Cloud",
            "SuperScroll Blizzard",
            "SuperScroll Protection from Water",
            "SuperScroll Acid Stream",
            "SuperScroll Prismatic Spray",
            "SuperScroll Invisibility",
            "SuperScroll Protection from Air",
            "SuperScroll Wall of Earth",
            "SuperScroll Stone Curse",
            "SuperScroll Protection from Earth",
            "SuperScroll Bless",
            "SuperScroll Haste",
            "SuperScroll Teleport",
            "SuperScroll Heal"
    };
    private static final String[] ACCEPTED_QUEST_BOOK_REWARD_NAMES = {
            "Book Fire",
            "Book Fire",
            "Book Fire",
            "Book Fire",
            "Book Water",
            "Book Water",
            "Book Water",
            "Book Water",
            "Book Air",
            "Book Air",
            "Book Air",
            "Book Air",
            "Book Earth",
            "Book Earth",
            "Book Earth",
            "Book Earth",
            "Book Astral",
            "Book Astral",
            "Book Astral",
            "Book Astral",
            "Book Astral",
            "Book Astral"
    };
    private static final String[] ACCEPTED_QUEST_BOOK_REWARD_EFFECTS = {
            "teachSpell=Fire_Arrow",
            "teachSpell=Fire_Ball",
            "teachSpell=Wall_of_Fire",
            "teachSpell=Protection_from_Fire",
            "teachSpell=Ice_Missile",
            "teachSpell=Poison_Cloud",
            "teachSpell=Blizzard",
            "teachSpell=Protection_from_Water",
            "teachSpell=Lightning",
            "teachSpell=Prismatic_Spray",
            "teachSpell=Invisibility",
            "teachSpell=Protection_from_Air",
            "teachSpell=Diamond_Dust",
            "teachSpell=Wall_of_Earth",
            "teachSpell=Stone_Curse",
            "teachSpell=Protection_from_Earth",
            "teachSpell=Bless",
            "teachSpell=Haste",
            "teachSpell=Control_Spirit",
            "teachSpell=Teleport",
            "teachSpell=Drain_Life",
            "teachSpell=Summon"
    };
    private static final SpellId[] ACCEPTED_QUEST_BOOK_REWARD_SPELL_IDS = {
            SpellId.FIRE_ARROW,
            SpellId.FIRE_BALL,
            SpellId.WALL_OF_FIRE,
            SpellId.PROTECTION_FROM_FIRE,
            SpellId.ICE_MISSILE,
            SpellId.POISON_CLOUD,
            SpellId.BLIZZARD,
            SpellId.PROTECTION_FROM_WATER,
            SpellId.LIGHTNING,
            SpellId.PRISMATIC_SPRAY,
            SpellId.INVISIBILITY,
            SpellId.PROTECTION_FROM_AIR,
            SpellId.DIAMOND_DUST,
            SpellId.WALL_OF_EARTH,
            SpellId.STONE_CURSE,
            SpellId.PROTECTION_FROM_EARTH,
            SpellId.BLESS,
            SpellId.HASTE,
            SpellId.CONTROL_SPIRIT,
            SpellId.TELEPORT,
            SpellId.DRAIN_LIFE,
            SpellId.SUMMON
    };
    private static final int ACCEPTED_QUEST_TRAINING_UPDATE_FLAGS = UnitDirtyFlags.toValue(
            UnitDirtyFlags.DISPLAY_NAME,
            UnitDirtyFlags.PRIMARY_ATTRIBUTES,
            UnitDirtyFlags.CONTROLLED_OWNER_STATS,
            UnitDirtyFlags.PROTECTIONS,
            UnitDirtyFlags.SPELLBOOK,
            UnitDirtyFlags.SPEED,
            UnitDirtyFlags.SIGHT_RANGE,
            UnitDirtyFlags.DAMAGE_PROFILE,
            UnitDirtyFlags.TO_HIT,
            UnitDirtyFlags.ARMOR_DEFENCE_ABSORBTION,
            UnitDirtyFlags.ENCUMBRANCE_WEIGHT,
            UnitDirtyFlags.SKILL_BONUSES,
            UnitDirtyFlags.SERVER_ID,
            UnitDirtyFlags.OWNER_PLAYER_ID,
            UnitDirtyFlags.TYPE_AND_FACE,
            UnitDirtyFlags.POSITION_AND_FACING,
            UnitDirtyFlags.VITALS_DERIVED,
            UnitDirtyFlags.SKILLS,
            UnitDirtyFlags.MP,
            UnitDirtyFlags.HP
    );

    //0x6C
    public final Map<Integer, QuestsStorage> playerQuestStorageById = new HashMap<>();
    //0x88
    public final Map<Integer, Inventory> playerDialogInventoryById = new HashMap<>();
    //0xA4
    public final CustomList<Unit> visitingUnits = new CustomList<>(Unit.class);
    //0xC4
    public int killAllHumansQuestEnabled;
    //0xC8
    public int killAllMonstersQuestEnabled;
    //0xCC
    public int killAllUndeadNecroQuestEnabled;
    //0xD0
    public int raiseDeadQuestEnabled;
    //0xD4
    public int itemDeliveryQuestItemId;
    //0xD8
    public int questStorageMaintenanceTicks;
    //0xE0
    public QuestsStorage currentQuestStorage;
    //0xE4
    public Quest currentQuest;

    /**
     * Native: Inn::Inn @0052F569.
     * Fully ported.
     */
    public Inn() {
        killAllHumansQuestEnabled = 0;
        killAllMonstersQuestEnabled = 0;
        killAllUndeadNecroQuestEnabled = 0;
        raiseDeadQuestEnabled = 0;
        itemDeliveryQuestItemId = 0;
        questStorageMaintenanceTicks = 0;
    }

    /**
     * Fully ported. Native: Inn::Inn @0052F635.
     */
    public Inn(int buildingId, TargetHandle targetHandle) {
        super(buildingId, targetHandle, 0, 0);
        killAllHumansQuestEnabled = 0;
        killAllMonstersQuestEnabled = 0;
        killAllUndeadNecroQuestEnabled = 0;
        raiseDeadQuestEnabled = 0;
        itemDeliveryQuestItemId = 0;
        questStorageMaintenanceTicks = 0;
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x38` and
     * Inn::openUnitSession @0052F813.
     * Fully ported.
     */
    public void openUnitSession(Unit unit) {
        Player player = unit.owner;
        int playerId = innPlayerId(player);
        purgeCompletedGlobalInnQuestsForPlayer(player);
        Quest acceptedQuest = findAcceptedGlobalInnQuestForPlayer(player);
        if (acceptedQuest == null) {
            generateInnQuestList(player);
            CServerApp.sendQuestListAction(currentQuestStorage, player, true);
        } else {
            generateAcceptedQuestDialogInventory(player);
            Inventory dialogInventory = playerDialogInventoryById.get(playerId);
            if (dialogInventory != null) {
                CServerApp.sendDialogItemList(unit, dialogInventory, player, 9);
            }
        }
        visitingUnits.add(unit);
        if (Globals.gameServer.networkSessionActive != 0) {
            unit.hideFromMissionMap();
        }
    }

    /**
     * Native support extracted from GameServer::handleServerGameAction @004F515D case `0x3A` and
     * Inn::closeUnitSession @0052F8E2.
     * Fully ported.
     */
    public void closeUnitSession(Unit unit, int selection) {
        Unit visitingUnit = null;
        for (Unit candidate : visitingUnits) {
            if ((candidate.idFull & 0xFFFF) == (unit.idFull & 0xFFFF)) {
                visitingUnit = candidate;
                break;
            }
        }
        if (visitingUnit == null) {
            return;
        }
        visitingUnits.remove(unit);
        if (Globals.gameServer.networkSessionActive != 0) {
            unit.returnToMissionMap();
        }
        handleInnSelectionBoundary(unit, selection);
    }

    /**
     * vtbl +0x14: Inn::updateRegen @00534011.
     * Fully ported.
     */
    @Override
    public void updateRegen() {
        for (Unit unit : new ArrayList<>(visitingUnits)) {
            Player player = unit.owner;
            if (player.mapLoadPending == 0) {
                closeUnitSession(unit, 0xFFFFFFFF);
            }
        }

        questStorageMaintenanceTicks += 1;
        if (questStorageMaintenanceTicks > 0x78 && visitingUnits.isEmpty()) {
            for (Map.Entry<Integer, QuestsStorage> entry : playerQuestStorageById.entrySet()) {
                entry.getValue().removeAndDeleteQuestsForOwner(entry.getKey());
            }
            questStorageMaintenanceTicks = 0;
        }
    }

    /**
     * Native: Inn::purgeCompletedGlobalInnQuestsForPlayer @00533F5B.
     * Fully ported.
     */
    private void purgeCompletedGlobalInnQuestsForPlayer(Player player) {
        int playerId = (short) player.playerId;
        for (Quest quest : new ArrayList<>(globalQuestStorage().questsByKey.values())) {
            if (quest != null && quest.getOwnerPlayerId() == playerId && quest.getMapNumber() == idFull && quest.isCompleted()) {
                globalQuestStorage().removeQuest(quest);
            }
        }
    }

    /**
     * Native: Inn::findAcceptedGlobalInnQuestForPlayer @00533E8F.
     * Fully ported.
     */
    private Quest findAcceptedGlobalInnQuestForPlayer(Player player) {
        int playerId = (short) player.playerId;
        for (Quest quest : globalQuestStorage().questsByKey.values()) {
            if (quest != null && quest.getOwnerPlayerId() == playerId && quest.getMapNumber() == idFull && quest.isAccepted()) {
                return quest;
            }
        }
        return null;
    }

    /**
     * Native: Inn::hasPendingInnQuestListForPlayer @00533F13.
     * Fully ported.
     */
    private boolean hasPendingInnQuestListForPlayer(Player player) {
        QuestsStorage questStorage = playerQuestStorageById.get(innPlayerId(player));
        if (questStorage == null) {
            return false;
        }
        currentQuestStorage = questStorage;
        return !currentQuestStorage.questsByKey.isEmpty();
    }

    /**
     * Native support boundary for Inn::closeUnitSession @0052F8E2 selection handling.
     * Fully ported.
     */
    private void handleInnSelectionBoundary(Unit unit, int selection) {
        Player player = unit.owner;
        int playerId = innPlayerId(player);
        if (selection == INN_ACCEPTED_QUEST_CANCEL_SELECTION) {
            int questKey = globalQuestStorage().findQuestKeyByMessage(Quest.MESSAGE_INN_PROBE, playerId, idFull);
            globalQuestStorage().findQuestKeyByMessage(2, questKey, 0);
            return;
        }
        if (Integer.compareUnsigned(selection, 0xFF) > 0) {
            currentQuestStorage = playerQuestStorageById.get(playerId);
            if (currentQuestStorage == null) {
                return;
            }
            currentQuest = currentQuestStorage.findQuestByKey(selection);
            if (currentQuest == null) {
                return;
            }
            currentQuestStorage.removeQuest(currentQuest);
            currentQuest.setState(0);
            globalQuestStorage().addQuest(currentQuest);
            CServerApp.sendQuestListAction(globalQuestStorage(), player, false);
            CServerApp.sendGameEventNotification(0x10, 0, player);
            handleAcceptedInnQuestSideEffectsBoundary(unit, currentQuest);
            return;
        }
        handleInnRewardSelectionBoundary(unit, selection);
    }

    /**
     * Native: Inn::generateInnQuestList @005303ED.
     * Fully ported.
     */
    private void generateInnQuestList(Player player) {
        int playerId = innPlayerId(player);
        currentQuestStorage = playerQuestStorageById.computeIfAbsent(playerId, ignored -> new QuestsStorage());
        if (globalQuestStorage().findQuestKeyByMessage(Quest.MESSAGE_INN_PROBE, playerId, idFull) != 0
                || hasPendingInnQuestListForPlayer(player)) {
            return;
        }
        appendKillAllUnitKindInnQuest(player);
        appendKillEnemyInnQuest(player);
        appendKillEnemyGroupInnQuest(player);
        appendHostileUnitRelocationQuest(player);
        appendHostileGroupRelocationQuest(player);
        appendRecruitOwnedUnitInnQuest(player);
        appendRaiseDeadInnQuest(player);
        appendPlayerEliminationInnQuest(player);
        appendTimedTravelInnQuest(player);
        appendItemDeliveryInnQuest(player);
    }

    /**
     * Native support extracted from Inn::generateInnQuestList @005303ED Quest_2 branch.
     * Fully ported.
     */
    private void appendKillAllUnitKindInnQuest(Player player) {
        Unit controlledUnit = (Unit) player.controlledUnit;
        int strongestSkillLevel = strongestInnQuestSkillLevel(controlledUnit);
        Map<Integer, Integer> unitCountsByTypeFace = new HashMap<>();
        for (Player candidatePlayer : Globals.gameServer.playerList.players) {
            if (candidatePlayer.isActive == 0) {
                continue;
            }
            for (Unit unit : candidatePlayer.ownedUnits) {
                int typeId = unit.getTokenTypeId() & 0xFF;
                if (unit.forceFinalCorpseStageOnDeath == 0
                        && isInnKillQuestTypeEnabled(typeId)
                        && isInnQuestThreatInSkillWindow(unit, strongestSkillLevel)) {
                    int key = typeId | ((unit.face & 0xFF) << 8);
                    unitCountsByTypeFace.merge(key, 1, Integer::sum);
                }
            }
        }
        if (unitCountsByTypeFace.isEmpty()) {
            return;
        }
        int selectedKey = randomKey(unitCountsByTypeFace);
        int matchingUnits = unitCountsByTypeFace.get(selectedKey);
        int targetCount;
        if (matchingUnits < 2) {
            targetCount = Utils.randInclusive(3) + 1;
        } else if (matchingUnits < 4) {
            targetCount = Utils.randInclusive(4) + 2;
        } else if (matchingUnits < 8) {
            targetCount = Utils.randInclusive(5) + 2;
        } else {
            targetCount = Utils.randInclusive(8) + 2;
        }
        int unitInfoIndex = CStaticDataMgr.getInstance().findInnQuestUnitInfoIndexByTypeAndFace(
                selectedKey & 0xFF,
                selectedKey >>> 8
        );
        if (unitInfoIndex == 0) {
            return;
        }
        UnitInfo unitInfo = CStaticDataMgr.getInstance().units.get(unitInfoIndex);
        int rewardBudget = unitInfo.getAttribute(UnitColumn.XP_VALUE) * targetCount;
        Quest quest = new Quest_2();
        quest.setQuestData(
                innQuestKey(player, quest),
                innPlayerId(player),
                idFull,
                selectedKey,
                0,
                targetCount,
                rewardBudget
        );
        quest.setState(3);
        currentQuest = quest;
        currentQuestStorage.addQuest(quest);
    }

    /**
     * Native support extracted from Inn::generateInnQuestList @005303ED strongest skill scan.
     * Fully ported.
     */
    private static int strongestInnQuestSkillLevel(Unit unit) {
        int strongestSkillLevel = 0;
        for (int skillIndex = 1; skillIndex < 6; skillIndex++) {
            if (strongestSkillLevel < unit.skillData.skillLevels[skillIndex]) {
                strongestSkillLevel = unit.skillData.skillLevels[skillIndex];
            }
        }
        return strongestSkillLevel;
    }

    /**
     * Native support extracted from Inn::generateInnQuestList @005303ED kill-category gates.
     * Fully ported.
     */
    private boolean isInnKillQuestTypeEnabled(int typeId) {
        return (0x3F < typeId && typeId < 0x52 && killAllMonstersQuestEnabled != 0)
                || (0x66 < typeId && typeId < 0x6B && killAllMonstersQuestEnabled != 0)
                || (0x51 < typeId && typeId < 99 && killAllUndeadNecroQuestEnabled != 0);
    }

    /**
     * Native support extracted from Inn::generateInnQuestList @005303ED Quest_1 branch.
     * Fully ported.
     */
    private void appendKillEnemyInnQuest(Player player) {
        Unit controlledUnit = (Unit) player.controlledUnit;
        int strongestSkillLevel = strongestInnQuestSkillLevel(controlledUnit);
        int searchRange = innQuestWideSearchRange();
        CustomList<Unit> candidates = new CustomList<>(Unit.class);
        for (Player candidatePlayer : Globals.gameServer.playerList.players) {
            if (!hasInnQuestCandidateCapacity(candidates)) {
                break;
            }
            if (candidatePlayer.isActive == 0 || candidatePlayer == owner) {
                continue;
            }
            for (Unit unit : candidatePlayer.ownedUnits) {
                if (!hasInnQuestCandidateCapacity(candidates)) {
                    break;
                }
                int typeId = unit.getTokenTypeId() & 0xFF;
                if (unit.innUnitRelocationQuestFlag != 0
                        && isInnRelocationQuestTypeEnabled(typeId)
                        && m_pTargetHandle.chebyshevDistanceByXY(unit.m_pTargetHandle) < searchRange
                        && isInnQuestThreatInSkillWindow(unit, strongestSkillLevel)) {
                    candidates.add(unit);
                }
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        Unit selectedUnit = candidates.get(Utils.randInclusive(candidates.size() - 1));
        Building nearestBuilding = Globals.gameServer.objectLists.buildings.findClosestPresentBuilding(selectedUnit.m_pTargetHandle);
        int nearestBuildingId = nearestBuilding == null ? 0 : nearestBuilding.idFull;
        Quest quest = new Quest_1();
        quest.setQuestData(
                innQuestKey(player, quest),
                innPlayerId(player),
                idFull,
                selectedUnit.idFull,
                nearestBuildingId | 0xFFFF0000,
                0,
                selectedUnit.price << 1
        );
        quest.setState(3);
        currentQuest = quest;
        currentQuestStorage.addQuest(quest);
    }

    /**
     * Native support extracted from Inn::generateInnQuestList @005303ED Quest_3 branch.
     * Fully ported.
     */
    private void appendKillEnemyGroupInnQuest(Player player) {
        Unit controlledUnit = (Unit) player.controlledUnit;
        int strongestSkillLevel = strongestInnQuestSkillLevel(controlledUnit);
        int searchRange = innQuestWideSearchRange();
        CustomList<UnitGroup> candidates = new CustomList<>(UnitGroup.class);
        for (Player candidatePlayer : Globals.gameServer.playerList.players) {
            if (!hasInnQuestCandidateCapacity(candidates)) {
                break;
            }
            if (candidatePlayer.isActive == 0) {
                continue;
            }
            for (UnitGroup group : candidatePlayer.unitGroups) {
                if (!hasInnQuestCandidateCapacity(candidates)) {
                    break;
                }
                if (group.innGroupRelocationQuestFlag == 0) {
                    continue;
                }
                for (Unit unit : group.units) {
                    if (isInnQuestThreatInSkillWindow(unit, strongestSkillLevel)
                            && m_pTargetHandle.chebyshevDistanceByXY(unit.m_pTargetHandle) < searchRange
                            && globalQuestStorage().findQuestKeyByMessage(0x0D, group.groupKey, 0) == 0) {
                        candidates.add(group);
                        break;
                    }
                }
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        UnitGroup selectedGroup = candidates.get(Utils.randInclusive(candidates.size() - 1));
        int totalX = 0;
        int totalY = 0;
        int unitCount = 0;
        int maxPrice = 0;
        for (Unit unit : selectedGroup.units) {
            if (maxPrice < unit.price) {
                maxPrice = unit.price;
            }
            totalX += unit.m_pTargetHandle.getX();
            totalY += unit.m_pTargetHandle.getY();
            unitCount++;
        }
        if (unitCount == 0) {
            unitCount = 1;
        }
        TargetHandle groupCenter = new TargetHandle();
        groupCenter.initFromBytes(totalX / unitCount, totalY / unitCount, Globals.worldMap);
        Building nearestBuilding = Globals.gameServer.objectLists.buildings.findClosestPresentBuilding(groupCenter);
        int nearestBuildingId = nearestBuilding == null ? 0 : nearestBuilding.idFull;
        Quest quest = new Quest_3();
        quest.setQuestData(
                innQuestKey(player, quest),
                innPlayerId(player),
                idFull,
                selectedGroup.groupKey,
                nearestBuildingId | 0xFFFF0000,
                0,
                maxPrice * 5
        );
        quest.setState(3);
        currentQuest = quest;
        currentQuestStorage.addQuest(quest);
    }

    /**
     * Native support extracted from Inn::generateInnQuestList @005303ED Quest_11 branch.
     * Fully ported.
     */
    private void appendHostileUnitRelocationQuest(Player player) {
        Unit controlledUnit = (Unit) player.controlledUnit;
        int strongestSkillLevel = strongestInnQuestSkillLevel(controlledUnit);
        CustomList<Unit> candidates = new CustomList<>(Unit.class);
        for (Player candidatePlayer : Globals.gameServer.playerList.players) {
            if (!hasInnQuestCandidateCapacity(candidates)) {
                break;
            }
            if (candidatePlayer.isActive == 0
                    || (Globals.gameServer.missionScriptRuntime.missionDiplomacyState
                    .relationFlags(innPlayerId(player), innPlayerId(candidatePlayer)) & 1) == 0) {
                continue;
            }
            for (Unit unit : candidatePlayer.ownedUnits) {
                if (!hasInnQuestCandidateCapacity(candidates)) {
                    break;
                }
                int typeId = unit.getTokenTypeId() & 0xFF;
                if (unit.hostileUnitRelocationQuestFlag != 0
                        && isInnRelocationQuestTypeEnabled(typeId)
                        && m_pTargetHandle.chebyshevDistanceByXY(unit.m_pTargetHandle) < 0x40
                        && isInnQuestThreatInSkillWindow(unit, strongestSkillLevel)
                        && globalQuestStorage().findQuestKeyByMessage(0x0C, unit.idFull, 0) == 0) {
                    candidates.add(unit);
                }
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        Unit selectedUnit = candidates.get(Utils.randInclusive(candidates.size() - 1));
        Quest quest = new Quest_11();
        quest.setQuestData(
                innQuestKey(player, quest),
                innPlayerId(player),
                idFull,
                selectedUnit.idFull,
                idFull,
                0,
                selectedUnit.price * 3
        );
        quest.setState(3);
        currentQuest = quest;
        currentQuestStorage.addQuest(quest);
    }

    /**
     * Native support extracted from Inn::generateInnQuestList @005303ED Quest_12 branch.
     * Fully ported.
     */
    private void appendHostileGroupRelocationQuest(Player player) {
        Unit controlledUnit = (Unit) player.controlledUnit;
        int strongestSkillLevel = strongestInnQuestSkillLevel(controlledUnit);
        CustomList<UnitGroup> candidates = new CustomList<>(UnitGroup.class);
        for (Player candidatePlayer : Globals.gameServer.playerList.players) {
            if (!hasInnQuestCandidateCapacity(candidates)) {
                break;
            }
            if (candidatePlayer.isActive == 0
                    || (Globals.gameServer.missionScriptRuntime.missionDiplomacyState
                    .relationFlags(innPlayerId(player), innPlayerId(candidatePlayer)) & 1) == 0) {
                continue;
            }
            for (UnitGroup group : candidatePlayer.unitGroups) {
                if (!hasInnQuestCandidateCapacity(candidates)) {
                    break;
                }
                if (group.hostileGroupRelocationQuestFlag == 0) {
                    continue;
                }
                for (Unit unit : group.units) {
                    if (isInnQuestThreatInSkillWindow(unit, strongestSkillLevel)
                            && m_pTargetHandle.chebyshevDistanceByXY(unit.m_pTargetHandle) < 0x40
                            && globalQuestStorage().findQuestKeyByMessage(0x0D, group.groupKey, 0) == 0) {
                        candidates.add(group);
                        break;
                    }
                }
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        UnitGroup selectedGroup = candidates.get(Utils.randInclusive(candidates.size() - 1));
        int maxPrice = 0;
        for (Unit unit : selectedGroup.units) {
            if (maxPrice < unit.price) {
                maxPrice = unit.price;
            }
        }
        Quest quest = new Quest_12();
        quest.setQuestData(
                innQuestKey(player, quest),
                innPlayerId(player),
                idFull,
                selectedGroup.groupKey,
                idFull,
                0,
                maxPrice * 6
        );
        quest.setState(3);
        currentQuest = quest;
        currentQuestStorage.addQuest(quest);
    }

    /**
     * Native support extracted from Inn::generateInnQuestList @005303ED wide search-range setup.
     * Fully ported.
     */
    private static int innQuestWideSearchRange() {
        int mapWidth = Globals.worldMap.getMapWidth();
        return mapWidth - 0x10 < 0x80 ? 0x40 : mapWidth / 2;
    }

    /**
     * Native support extracted from Inn::generateInnQuestList @005303ED relocation-category gates.
     * Fully ported.
     */
    private boolean isInnRelocationQuestTypeEnabled(int typeId) {
        return (0x3F < typeId && typeId < 0x52 && killAllMonstersQuestEnabled != 0)
                || (0x66 < typeId && typeId < 0x6B && killAllMonstersQuestEnabled != 0)
                || (0x51 < typeId && typeId < 100 && killAllUndeadNecroQuestEnabled != 0)
                || (typeId < 0x40 && killAllHumansQuestEnabled != 0);
    }

    /**
     * Native support extracted from Inn::generateInnQuestList @005303ED threat window.
     * Fully ported.
     */
    private static boolean isInnQuestThreatInSkillWindow(Unit unit, int strongestSkillLevel) {
        int threat = unit.computeInnQuestThreatRating();
        return strongestSkillLevel * 3 - 0x32 < threat && threat < strongestSkillLevel * 3 + 0x4B;
    }

    /**
     * Native support extracted from Inn::generateInnQuestList @005303ED random candidate slot selection.
     * Fully ported.
     */
    private static int randomKey(Map<Integer, Integer> candidates) {
        int index = Utils.randInclusive(candidates.size() - 1);
        Iterator<Integer> iterator = candidates.keySet().iterator();
        for (int skipped = 0; skipped < index; skipped++) {
            iterator.next();
        }
        return iterator.next();
    }

    /**
     * Native support extracted from Inn::generateInnQuestList @005303ED stack candidate arrays sized to 0x100.
     * Fully ported.
     */
    private static boolean hasInnQuestCandidateCapacity(CustomList<?> candidates) {
        return candidates.size() < INN_QUEST_CANDIDATE_LIMIT;
    }

    /**
     * Native support extracted from Inn::generateInnQuestList @005303ED quest-key packing.
     * Fully ported.
     */
    private int innQuestKey(Player player, Quest quest) {
        return (idFull << 16) | (innPlayerId(player) << 8) | (quest.getId() & 0xFF);
    }

    /**
     * Native support extracted from Inn quest flows where player ids are cast to signed short before use as quest
     * owners, CMap keys, or relation-grid indexes: @005303ED, @0052F813, @0052F8E2, @005325EE.
     * Fully ported.
     */
    private static int innPlayerId(Player player) {
        return (short) player.playerId;
    }

    /**
     * Native support extracted from Inn::generateInnQuestList @005303ED Quest_4 branch.
     * Fully ported.
     */
    private void appendRecruitOwnedUnitInnQuest(Player player) {
        CustomList<Unit> candidates = new CustomList<>(Unit.class);
        Player innOwner = owner;
        for (Unit unit : innOwner.ownedUnits) {
            if (!hasInnQuestCandidateCapacity(candidates)) {
                break;
            }
            if (unit.innRecruitmentQuestFlag != 0
                    && unit.respawning == 0
                    && globalQuestStorage().findQuestKeyByMessage(0x0C, unit.idFull, 0) == 0) {
                candidates.add(unit);
            }
        }
        int destinationBuildingId = globalQuestStorage().chooseDifferentSecondaryIndexKey(idFull);
        if (candidates.isEmpty() || destinationBuildingId == 0) {
            return;
        }
        Unit selectedUnit = candidates.get(Utils.randInclusive(candidates.size() - 1));
        Unit controlledUnit = (Unit) player.controlledUnit;
        Quest quest = new Quest_4();
        quest.setQuestData(
                innQuestKey(player, quest),
                innPlayerId(player),
                idFull,
                selectedUnit.idFull,
                destinationBuildingId,
                0,
                controlledUnit.skillsTotalBonusPermille / 0x21
        );
        quest.setState(3);
        currentQuest = quest;
        currentQuestStorage.addQuest(quest);
    }

    /**
     * Native support extracted from Inn::generateInnQuestList @005303ED Quest_10/9/8 raise-dead branch.
     * Fully ported.
     */
    private void appendRaiseDeadInnQuest(Player player) {
        Unit controlledUnit = (Unit) player.controlledUnit;
        if (!controlledUnit.isMageClass()
                || raiseDeadQuestEnabled == 0
                || (controlledUnit.spellbook.getSpellbookMask() & 0x400000) == 0) {
            return;
        }
        Quest quest = switch (Utils.randInclusive(2)) {
            case 0 -> new Quest_10();
            case 1 -> new Quest_9();
            default -> new Quest_8();
        };
        quest.setQuestData(
                innQuestKey(player, quest),
                innPlayerId(player),
                idFull,
                0,
                idFull,
                Utils.randInclusive(6) + 3,
                0
        );
        quest.setState(3);
        currentQuest = quest;
        currentQuestStorage.addQuest(quest);
    }

    /**
     * Native support extracted from Inn::generateInnQuestList @005303ED Quest_13 player-elimination branch.
     * Fully ported.
     */
    private void appendPlayerEliminationInnQuest(Player player) {
        CustomList<Player> candidates = new CustomList<>(Player.class);
        Player innOwner = owner;
        for (Player candidatePlayer : Globals.gameServer.playerList.players) {
            if (candidatePlayer.isActive != 0
                    && (Globals.gameServer.missionScriptRuntime.missionDiplomacyState
                    .relationFlags(innPlayerId(innOwner), innPlayerId(candidatePlayer)) & 1) != 0
                    && candidatePlayer.playerEliminationQuestEnabled != 0
                    && Globals.gameServer.objectLists.buildings.findOwnedInn(candidatePlayer) != null) {
                candidates.add(candidatePlayer);
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        Player selectedPlayer = candidates.get(Utils.randInclusive(candidates.size() - 1));
        Building targetInn = Globals.gameServer.objectLists.buildings.findOwnedInn(selectedPlayer);
        int maxPrice = 0;
        for (Unit unit : selectedPlayer.ownedUnits) {
            if (maxPrice < unit.price) {
                maxPrice = unit.price;
            }
        }
        Quest quest = new Quest_13();
        quest.setQuestData(
                innQuestKey(player, quest),
                innPlayerId(player),
                idFull,
                innPlayerId(selectedPlayer),
                targetInn.idFull | 0xFFFF0000,
                0,
                maxPrice * 10
        );
        quest.setState(3);
        currentQuest = quest;
        currentQuestStorage.addQuest(quest);
    }

    /**
     * Native support extracted from Inn::generateInnQuestList @005303ED Quest_6 branch.
     * Fully ported.
     */
    private void appendTimedTravelInnQuest(Player player) {
        int destinationBuildingId = globalQuestStorage().chooseDifferentSecondaryIndexKey(idFull);
        if (destinationBuildingId == 0) {
            return;
        }
        Building destinationBuilding = Globals.gameServer.objectLists.buildings.findByTokenId(destinationBuildingId);
        if (destinationBuilding == null) {
            return;
        }
        int deltaX = m_pTargetHandle.getX() - destinationBuilding.m_pTargetHandle.getX();
        int deltaY = m_pTargetHandle.getY() - destinationBuilding.m_pTargetHandle.getY();
        double distanceSubtiles = Math.sqrt(deltaX * deltaX + deltaY * deltaY) * 256.0;
        double randomScale = Utils.randomFloat0to1Inclusive() + 1.5;
        Unit controlledUnit = (Unit) player.controlledUnit;
        int travelTicks = (int) ((randomScale * distanceSubtiles) / controlledUnit.speed);
        Quest quest = new Quest_6();
        quest.setQuestData(
                innQuestKey(player, quest),
                innPlayerId(player),
                idFull,
                controlledUnit.idFull,
                destinationBuildingId,
                travelTicks,
                controlledUnit.skillsTotalBonusPermille / 0x32
        );
        quest.setState(3);
        currentQuest = quest;
        currentQuestStorage.addQuest(quest);
    }

    /**
     * Native support extracted from Inn::generateInnQuestList @005303ED Quest_5 branch.
     * Fully ported.
     */
    private void appendItemDeliveryInnQuest(Player player) {
        int destinationBuildingId = globalQuestStorage().chooseDifferentSecondaryIndexKey(idFull);
        if (destinationBuildingId == 0 || itemDeliveryQuestItemId == 0) {
            return;
        }
        Unit controlledUnit = (Unit) player.controlledUnit;
        Quest quest = new Quest_5();
        quest.setQuestData(
                innQuestKey(player, quest),
                innPlayerId(player),
                idFull,
                itemDeliveryQuestItemId,
                destinationBuildingId,
                0,
                controlledUnit.skillsTotalBonusPermille / 100
        );
        quest.setState(3);
        currentQuest = quest;
        currentQuestStorage.addQuest(quest);
    }

    /**
     * Native: Inn::generateAcceptedQuestDialogInventory @005325EE.
     * Fully ported.
     */
    private void generateAcceptedQuestDialogInventory(Player player) {
        int playerId = innPlayerId(player);
        Quest acceptedQuest = findAcceptedGlobalInnQuestForPlayer(player);
        int rewardBudget = acceptedQuest == null ? 0 : acceptedQuest.getRewardBudget();
        if (rewardBudget == 0 && !player.isCheatCommandEnabled()) {
            return;
        }
        if (rewardBudget > 0xF9FC18) {
            rewardBudget = 0xF9FC18;
        }
        if (rewardBudget < 0xFA) {
            rewardBudget = 0xFA;
        }
        if (playerDialogInventoryById.containsKey(playerId)) {
            return;
        }
        Inventory dialogInventory = new Inventory();
        populateAcceptedQuestRewardsBeforeFixedOptions(player, dialogInventory, rewardBudget);
        Item goldReward = new Item();
        goldReward.hash = ACCEPTED_QUEST_GOLD_REWARD_HASH;
        goldReward.count = rewardBudget / 0xFA;
        dialogInventory.addItem(goldReward);
        Item trainingReward = new Item();
        trainingReward.hash = ACCEPTED_QUEST_TRAINING_REWARD_HASH;
        trainingReward.count = rewardBudget / 0xFA;
        dialogInventory.addItem(trainingReward);
        appendAcceptedQuestSpawnedUnitReward(player, dialogInventory, rewardBudget);
        appendAcceptedQuestMagicUpgradeReward(player, dialogInventory, rewardBudget);
        playerDialogInventoryById.put(playerId, dialogInventory);
    }

    /**
     * Native support extracted from Inn::generateAcceptedQuestDialogInventory @005325EE reward options before the
     * fixed gold/training entries.
     * Fully ported.
     */
    private void populateAcceptedQuestRewardsBeforeFixedOptions(
            Player player,
            Inventory dialogInventory,
            int rewardBudget
    ) {
        appendAcceptedQuestEquipmentReward(player, dialogInventory, rewardBudget, rewardBudget << 1, 0x1B7FFFFF);
        appendAcceptedQuestEquipmentReward(player, dialogInventory, (rewardBudget * 3) / 4, (rewardBudget * 3) / 2, 0x2BFFFFFF);
        appendAcceptedQuestScrollReward(dialogInventory, rewardBudget);
        appendAcceptedQuestBookReward(player, dialogInventory, rewardBudget);
        appendAcceptedQuestStatPotionReward(player, dialogInventory, rewardBudget);
    }

    /**
     * Native support extracted from Inn::generateAcceptedQuestDialogInventory @005325EE equipment reward passes.
     * Fully ported.
     */
    private static void appendAcceptedQuestEquipmentReward(
            Player player,
            Inventory dialogInventory,
            int minPrice,
            int maxPrice,
            int selectionMask
    ) {
        ShopAssortmentEntry entry = new ShopAssortmentEntry();
        entry.maxPrice = maxPrice;
        entry.minPrice = minPrice;
        entry.itemCount = 100;
        entry.maxSameTypeItems = 1;
        entry.selectionMask = selectionMask;
        Inventory generated = new ItemAssortmentGenerator().generateItems(entry, new Inventory());
        CustomList<Item> suitableItems = new CustomList<>(Item.class);
        int suitabilityMask = ((Unit) player.controlledUnit).isMageClass() ? 2 : 1;
        for (Item item : generated.items) {
            if ((item.worldItem.getAttribute(WorldItemColumn.SUITABLE_FOR) & suitabilityMask & 0xFF) != 0) {
                suitableItems.add(item);
            }
        }
        if (!suitableItems.isEmpty()) {
            Item reward = suitableItems.get(Utils.randInclusive(suitableItems.size() - 1));
            reward.count = 1;
            dialogInventory.addItem(reward);
        }
    }

    /**
     * Native support extracted from Inn::generateAcceptedQuestDialogInventory @005325EE scroll reward pass.
     * Fully ported.
     */
    private static void appendAcceptedQuestScrollReward(Inventory dialogInventory, int rewardBudget) {
        CustomList<Item> scrollRewards = new CustomList<>(Item.class);
        for (String scrollName : ACCEPTED_QUEST_SCROLL_REWARD_NAMES) {
            MagicItem scroll = new MagicItem(scrollName);
            int rewardLimit = scroll.price * 800;
            if (scroll.price < rewardBudget && rewardLimit - rewardBudget != 0 && rewardBudget <= rewardLimit) {
                scroll.count = acceptedQuestScrollRewardCount(rewardBudget / scroll.price);
                scrollRewards.add(scroll);
            }
        }
        if (!scrollRewards.isEmpty()) {
            dialogInventory.addItem(scrollRewards.get(Utils.randInclusive(scrollRewards.size() - 1)));
        }
    }

    /**
     * Native support extracted from Inn::generateAcceptedQuestDialogInventory @005325EE scroll count compression.
     * Fully ported.
     */
    private static int acceptedQuestScrollRewardCount(int count) {
        if (10 < count) {
            count = ((count - 10) >> 1) + 10;
        }
        if (0x14 < count) {
            count = ((count - 0x14) >> 1) + 0x14;
        }
        if (0x32 < count) {
            count = ((count - 0x32) >> 1) + 0x32;
        }
        if (100 < count) {
            count = ((count - 100) >> 1) + 100;
        }
        return count;
    }

    /**
     * Native support extracted from Inn::generateAcceptedQuestDialogInventory @005325EE mage book reward pass.
     * Fully ported.
     */
    private static void appendAcceptedQuestBookReward(Player player, Inventory dialogInventory, int rewardBudget) {
        Unit controlledUnit = (Unit) player.controlledUnit;
        if (!controlledUnit.isMageClass()) {
            return;
        }
        CustomList<Item> bookRewards = new CustomList<>(Item.class);
        int spellbookMask = controlledUnit.spellbook.getSpellbookMask();
        for (int index = 0; index < ACCEPTED_QUEST_BOOK_REWARD_NAMES.length; index++) {
            SpellId spellId = ACCEPTED_QUEST_BOOK_REWARD_SPELL_IDS[index];
            if ((spellbookMask & (1 << (spellId.id & 0x1F))) == 0) {
                MagicItem book = new MagicItem(ACCEPTED_QUEST_BOOK_REWARD_NAMES[index]);
                book.parseEffects(ACCEPTED_QUEST_BOOK_REWARD_EFFECTS[index]);
                book.recalculatePrice();
                if (book.price < (rewardBudget * 5) / 4 && rewardBudget / 2 < book.price) {
                    bookRewards.add(book);
                }
            }
        }
        if (!bookRewards.isEmpty()) {
            dialogInventory.addItem(bookRewards.get(Utils.randInclusive(bookRewards.size() - 1)));
        }
    }

    /**
     * Native support extracted from Inn::generateAcceptedQuestDialogInventory @005325EE stat potion reward pass.
     * Fully ported.
     */
    private static void appendAcceptedQuestStatPotionReward(Player player, Inventory dialogInventory, int rewardBudget) {
        Unit controlledUnit = (Unit) player.controlledUnit;
        if ((controlledUnit.skillsTotalBonusPermille <= ACCEPTED_QUEST_POTION_REWARD_MIN_SKILL_BONUS
                || rewardBudget <= ACCEPTED_QUEST_POTION_REWARD_MIN_BUDGET)
                && !player.isCheatCommandEnabled()) {
            return;
        }
        int[] thresholds = acceptedQuestStatPotionThresholds(controlledUnit);
        boolean[] eligibleStats = {
                controlledUnit.m_nBody < thresholds[0],
                controlledUnit.m_nMind < thresholds[1],
                controlledUnit.m_nSpirit < thresholds[2],
                controlledUnit.m_nReaction < thresholds[3]
        };
        int eligibleCount = 0;
        for (boolean eligible : eligibleStats) {
            if (eligible) {
                eligibleCount++;
            }
        }
        if (eligibleCount == 0) {
            return;
        }
        int statIndex;
        do {
            statIndex = Utils.randInclusive(3);
        } while (!eligibleStats[statIndex]);
        dialogInventory.addItem(new MagicItem(ACCEPTED_QUEST_STAT_POTION_REWARD_NAMES[statIndex]));
    }

    /**
     * Native support extracted from Inn::generateAcceptedQuestDialogInventory @005325EE class/sex stat thresholds.
     * Fully ported.
     */
    private static int[] acceptedQuestStatPotionThresholds(Unit unit) {
        boolean nonMage = !unit.isMageClass();
        boolean female = unit.isFemale();
        if (nonMage) {
            return female
                    ? new int[]{0x32, 0x2E, 0x30, 0x34}
                    : new int[]{0x34, 0x30, 0x2E, 0x32};
        }
        return female
                ? new int[]{0x2E, 0x32, 0x34, 0x30}
                : new int[]{0x30, 0x34, 0x32, 0x2E};
    }

    /**
     * Native support extracted from Inn::generateAcceptedQuestDialogInventory @005325EE spawned-unit reward pass.
     * Fully ported.
     */
    private static void appendAcceptedQuestSpawnedUnitReward(Player player, Inventory dialogInventory, int rewardBudget) {
        if (player.ownedUnits.countInnRewardEligibleOwnedUnits() != 0) {
            return;
        }
        int unitInfoIndex = CStaticDataMgr.getInstance().findInnRewardUnitInfoIndex(rewardBudget >> 4);
        if (unitInfoIndex == 0) {
            return;
        }
        UnitInfo unitInfo = CStaticDataMgr.getInstance().units.get(unitInfoIndex);
        Item reward = new Item();
        reward.hash = ACCEPTED_QUEST_SPAWNED_UNIT_REWARD_HASH;
        reward.count = (unitInfo.getAttribute(UnitColumn.TYPE_ID) & 0xFF)
                | ((unitInfo.getAttribute(UnitColumn.FACE) & 0xFF) << 8);
        dialogInventory.addItem(reward);
    }

    /**
     * Native support extracted from Inn::generateAcceptedQuestDialogInventory @005325EE magic-upgrade reward pass.
     * Fully ported.
     */
    private static void appendAcceptedQuestMagicUpgradeReward(Player player, Inventory dialogInventory, int rewardBudget) {
        Unit controlledUnit = (Unit) player.controlledUnit;
        if ((acceptedQuestHighSkillCount(controlledUnit) < ACCEPTED_QUEST_MAGIC_UPGRADE_MIN_HIGH_SKILLS
                || rewardBudget <= ACCEPTED_QUEST_MAGIC_UPGRADE_MIN_BUDGET)
                && !player.isCheatCommandEnabled()) {
            return;
        }
        CustomList<Item> upgradeCandidates = new CustomList<>(Item.class);
        appendAcceptedQuestMagicUpgradeCandidate(upgradeCandidates, controlledUnit.pWeapon, rewardBudget);
        appendAcceptedQuestMagicUpgradeCandidate(upgradeCandidates, controlledUnit.pShield, rewardBudget);
        Humanoid controlledHumanoid = (Humanoid) controlledUnit;
        for (Item item : controlledHumanoid.items) {
            appendAcceptedQuestMagicUpgradeCandidate(upgradeCandidates, item, rewardBudget);
        }
        if (!upgradeCandidates.isEmpty()) {
            dialogInventory.addItem(upgradeCandidates.get(Utils.randInclusive(upgradeCandidates.size() - 1)));
        }
    }

    /**
     * Native support extracted from Inn::generateAcceptedQuestDialogInventory @005325EE high skill-count gate.
     * Fully ported.
     */
    private static int acceptedQuestHighSkillCount(Unit unit) {
        int count = 0;
        for (int skillIndex = 1; skillIndex < 6; skillIndex++) {
            if (ACCEPTED_QUEST_MAGIC_UPGRADE_HIGH_SKILL_LEVEL < unit.skillData.skillLevels[skillIndex]) {
                count++;
            }
        }
        return count;
    }

    /**
     * Native support extracted from Inn::generateAcceptedQuestDialogInventory @005325EE magic-upgrade candidate scan.
     * Fully ported.
     */
    private static void appendAcceptedQuestMagicUpgradeCandidate(
            CustomList<Item> upgradeCandidates,
            Item equippedItem,
            int rewardBudget
    ) {
        if (equippedItem == null || equippedItem.canStackInInventory() != 0 || equippedItem.magicVolume <= 0) {
            return;
        }
        Effect firstEffect = equippedItem.effects.getFirst();
        int effectCost = firstEffect.getMagicValue(MagicColumn.COST_MP);
        if (equippedItem.magicVolume < effectCost) {
            return;
        }
        int maxIncrement = acceptedQuestMagicUpgradeMaxIncrement(firstEffect);
        if (maxIncrement == 0) {
            return;
        }
        int increment = (rewardBudget * maxIncrement) / 8_000_000;
        if (increment == 0) {
            increment = 1;
        }
        if (maxIncrement < increment) {
            increment = maxIncrement;
        }
        if (increment * effectCost - equippedItem.magicVolume != 0 && equippedItem.magicVolume <= increment * effectCost) {
            increment = equippedItem.magicVolume / effectCost;
        }
        Item upgradedItem = cloneAcceptedQuestMagicUpgradeItem(equippedItem);
        Effect upgradedEffect = upgradedItem.effects.getFirst();
        applyAcceptedQuestMagicUpgradeIncrement(upgradedEffect, increment);
        upgradeCandidates.add(upgradedItem);
    }

    /**
     * Native support extracted from Inn::generateAcceptedQuestDialogInventory @005325EE magic effect increment class.
     * Fully ported.
     */
    private static int acceptedQuestMagicUpgradeMaxIncrement(Effect effect) {
        int affectMax = effect.getMagicValue(MagicColumn.AFFECT_MAX);
        EffectId effectId = EffectId.fromId(effect.id & 0xFF);
        return switch (effectId) {
            case BODY, MIND, REACTION, SPIRIT, ABSORBTION, SPEED, ROTATION_SPEED, SCAN_RANGE ->
                    effect.mValue.getFull() < affectMax ? 1 : 0;
            case HEALTH, HEALTH_MAX, MANA, MANA_MAX, TO_HIT, DAMAGE_MIN, DEFENCE, DAMAGE ->
                    effect.mValue.getFull() < affectMax ? 8 : 0;
            case HEALTH_REGENERATION, MANA_REGENERATION -> effect.mValue.getFull() < affectMax ? 0x14 : 0;
            case PROTECTION_0, PROTECTION_FIRE, PROTECTION_WATER, PROTECTION_AIR, PROTECTION_EARTH,
                 PROTECTION_ASTRAL, FIGHTER_SKILL_0, SKILL_BLADE, SKILL_AXE, SKILL_BLUDGEON, SKILL_PIKE,
                 SKILL_SHOOTING, MAGE_SKILL_0, SKILL_FIRE, SKILL_WATER, SKILL_AIR, SKILL_EARTH, SKILL_ASTRAL ->
                    effect.mValue.getFull() < affectMax ? 5 : 0;
            case CAST_SPELL -> (short) effect.mValue.getS1() < 100 ? 8 : 0;
            case DAMAGE_FIRE, DAMAGE_WATER, DAMAGE_AIR, DAMAGE_EARTH, DAMAGE_ASTRAL, DAMAGE_BONUS ->
                    effect.mValue.getB1() + effect.mValue.getB2() < affectMax ? 8 : 0;
            default -> 0;
        };
    }

    /**
     * Native support extracted from Inn::generateAcceptedQuestDialogInventory @005325EE equipment copy constructors.
     * Fully ported.
     */
    private static Item cloneAcceptedQuestMagicUpgradeItem(Item equippedItem) {
        if (equippedItem instanceof Weapon weapon) {
            return new Weapon().copyFrom(weapon);
        }
        if (equippedItem instanceof Shield shield) {
            return new Shield().copyFrom(shield);
        }
        return new Armor().copyFrom((Armor) equippedItem);
    }

    /**
     * Native support extracted from Inn::generateAcceptedQuestDialogInventory @005325EE upgraded effect writeback.
     * Fully ported.
     */
    private static void applyAcceptedQuestMagicUpgradeIncrement(Effect effect, int increment) {
        EffectId effectId = EffectId.fromId(effect.id & 0xFF);
        if (effectId == EffectId.CAST_SPELL) {
            effect.mValue.setS1(effect.mValue.getS1() + increment);
        } else if (effectId.isBetween(EffectId.DAMAGE_FIRE, EffectId.DAMAGE_BONUS)) {
            effect.mValue.setB1(effect.mValue.getB1() + increment);
        } else {
            effect.mValue.setFull(effect.mValue.getFull() + increment);
        }
    }

    /**
     * Native support boundary for Inn::closeUnitSession @0052F8E2 accepted quest side effects.
     * Fully ported.
     */
    private void handleAcceptedInnQuestSideEffectsBoundary(Unit unit, Quest quest) {
        Player player = unit.owner;
        switch (quest.getId()) {
            case 4 -> {
                Unit activeUnit = findActiveUnitByTokenId(quest.primaryArgument);
                if (activeUnit != null) {
                    activeUnit.transferToPlayerForMissionScript(player);
                } else {
                    globalQuestStorage().removeQuest(quest);
                }
            }
            case 5 -> {
                unit.inventory.addItem(new MagicItem(quest.primaryArgument & 0xFF));
                CServerApp.netUpdate(
                        unit,
                        player,
                        UnitDirtyFlags.INVENTORY_ITEMS.value | UnitDirtyFlags.ENCUMBRANCE_WEIGHT.value,
                        0x0FFB,
                        0,
                        0
                );
            }
            case 6 -> quest.setProgressToCurrentTick();
            case 11 -> {
                Unit activeUnit = findActiveUnitByTokenId(quest.primaryArgument);
                if (activeUnit == null) {
                    globalQuestStorage().removeQuest(quest);
                    return;
                }
                moveQuestGroupToInnCell(activeUnit.unitGroup);
            }
            case 12 -> {
                for (Player candidatePlayer : Globals.gameServer.playerList.players) {
                    if (candidatePlayer.isActive == 0) {
                        continue;
                    }
                    for (UnitGroup group : candidatePlayer.unitGroups) {
                        if (group.groupKey == quest.primaryArgument) {
                            moveQuestGroupToInnCell(group);
                            return;
                        }
                    }
                }
            }
            default -> {
            }
        }
    }

    /**
     * Native support boundary for Inn::closeUnitSession @0052F8E2 reward inventory selection branch.
     * Fully ported.
     */
    private void handleInnRewardSelectionBoundary(Unit unit, int selection) {
        Player player = unit.owner;
        Inventory dialogInventory = playerDialogInventoryById.get(innPlayerId(player));
        if (dialogInventory == null) {
            removeAcceptedGlobalInnQuest(player);
            return;
        }
        int originalItemCount = dialogInventory.size();
        if (selection < 0 || selection >= originalItemCount) {
            cleanupDialogInventory(player, dialogInventory);
            removeAcceptedGlobalInnQuest(player);
            return;
        }
        Item selectedItem = dialogInventory.items.get(selection);
        if ((selectedItem.hash & 0xFFFF) == ACCEPTED_QUEST_SPAWNED_UNIT_REWARD_HASH) {
            spawnAcceptedQuestRewardUnit(unit, dialogInventory.takeItemAt(selection, 0xFFFF));
            cleanupDialogInventory(player, dialogInventory);
            removeAcceptedGlobalInnQuest(player);
            return;
        }
        if ((selectedItem.hash & 0xFFFF) == ACCEPTED_QUEST_TRAINING_REWARD_HASH) {
            Item item = dialogInventory.takeItemAt(selection, 0xFFFF);
            ((Humanoid) unit).applyInnTrainingReward(item.count);
            CServerApp.netUpdate(unit, null, ACCEPTED_QUEST_TRAINING_UPDATE_FLAGS, 0x0FFB, 0, 0);
            cleanupDialogInventory(player, dialogInventory);
            removeAcceptedGlobalInnQuest(player);
            return;
        }
        if ((selectedItem.hash & 0xFFFF) == ACCEPTED_QUEST_GOLD_REWARD_HASH) {
            Item item = dialogInventory.takeItemAt(selection, 0xFFFF);
            player.adjustGoldAndNotify((item.count & 0xFFFF) * 0xFA, 0);
            cleanupDialogInventory(player, dialogInventory);
            removeAcceptedGlobalInnQuest(player);
            return;
        }
        if (selection < originalItemCount - 1) {
            Item item = dialogInventory.takeItemAt(selection, 0xFFFF);
            item.idFull = 1;
            unit.inventory.addItem(item);
            CServerApp.netUpdate(unit, null, UnitDirtyFlags.INVENTORY_AND_ENCUMBRANCE.value, 0x0FFB, 0, 0);
            cleanupDialogInventory(player, dialogInventory);
            removeAcceptedGlobalInnQuest(player);
            return;
        }
        applyFinalInnItemReward(unit, dialogInventory.takeItemAt(selection, 0xFFFF));
        cleanupDialogInventory(player, dialogInventory);
        removeAcceptedGlobalInnQuest(player);
    }

    /**
     * Native support extracted from Inn::closeUnitSession @0052F8E2 active unit lookup through UnitList::FindByTokenId.
     * Fully ported.
     */
    private static Unit findActiveUnitByTokenId(int tokenId) {
        return Globals.gameServer.activeUnits.findByTokenId(tokenId);
    }

    /**
     * Native support extracted from Inn::closeUnitSession @0052F8E2 quest type 11/12 move-to-inn side effects.
     * Fully ported.
     */
    private void moveQuestGroupToInnCell(UnitGroup group) {
        group.missionState.markScenarioScriptReferenced();
        Globals.gameServer.missionScriptRuntime.initializeMoveToCellGroup(
                group,
                m_pTargetHandle.getX(),
                m_pTargetHandle.getY()
        );
    }

    /**
     * Native support extracted from Inn::closeUnitSession @0052F8E2 dialog reward inventory cleanup.
     * Fully ported. Java removes the map entry and clears the inventory; GC replaces native object deletion.
     */
    private void cleanupDialogInventory(Player player, Inventory dialogInventory) {
        playerDialogInventoryById.remove(innPlayerId(player));
        dialogInventory.items.clear();
    }

    /**
     * Native support extracted from Inn::closeUnitSession @0052F8E2 accepted global inn quest cleanup tail.
     * Fully ported. Java removes the quest from storage and clears the field; GC replaces native object deletion.
     */
    private void removeAcceptedGlobalInnQuest(Player player) {
        currentQuest = findAcceptedGlobalInnQuestForPlayer(player);
        if (currentQuest != null) {
            globalQuestStorage().removeQuest(currentQuest);
            currentQuest = null;
        }
    }

    /**
     * Native support extracted from Inn::closeUnitSession @0052F8E2 accepted quest reward item hash `0xFFFD`.
     * Fully ported.
     */
    private static void spawnAcceptedQuestRewardUnit(Unit unit, Item reward) {
        int rewardCount = reward.count & 0xFFFF;
        int unitInfoIndex = CStaticDataMgr.getInstance().findUnitInfoIndexByTypeAndFace(
                rewardCount & 0xFF,
                rewardCount >>> 8
        );
        UnitInfo unitInfo = Globals.staticDataMgr.units.get(unitInfoIndex);
        Unit spawnedUnit = new Unit();
        spawnedUnit.key = (unitInfoIndex) & 0xFFFF;
        spawnedUnit.applyUnitInfoValues(unitInfo);
        if (spawnedUnit.placeNearMissionCell(
                unit.m_pTargetHandle.getX(),
                unit.m_pTargetHandle.getY(),
                ACCEPTED_QUEST_SPAWNED_UNIT_DIAMETER
        )) {
            Globals.gameServer.activeUnits.addAndAssignRuntimeId(spawnedUnit);
            Player player = unit.owner;
            spawnedUnit.owner = player;
            player.ownedUnits.add(spawnedUnit);
            UnitGroup group = new UnitGroup();
            player.unitGroups.add(group);
            group.addUnit(spawnedUnit);
            spawnedUnit.initializeScenarioMissionEntryUnit(Globals.gameServer.missionScriptRuntime);
            group.initializeScenarioMissionEntryGroup(Globals.gameServer.missionScriptRuntime);
            CServerApp.netUpdate(
                    spawnedUnit,
                    null,
                    ALL_UNIT_UPDATE_FLAGS,
                    EQUIPMENT_BROADCAST_MASK,
                    0,
                    0
            );
        }
    }

    /**
     * Native support extracted from Inn::closeUnitSession @0052F8E2 final dialog item reward branch.
     * Fully ported. Java ignores the returned item because native immediately deletes it.
     */
    private static void applyFinalInnItemReward(Unit unit, Item item) {
        Item result = item.useAndConsume(unit);
        if (result == null) {
            item.takeOff(unit);
        }
        CServerApp.netUpdate(
                unit,
                null,
                UnitDirtyFlags.EQUIPPED_ITEMS.value | UnitDirtyFlags.SPEED.value | UnitDirtyFlags.ENCUMBRANCE_WEIGHT.value,
                0x0FFB,
                0,
                0
        );
    }

    /**
     * Native support extracted from Inn::openUnitSession @0052F813 and Inn::closeUnitSession @0052F8E2 global
     * g_QuestsStorage accesses.
     */
    private static QuestsStorage globalQuestStorage() {
        return Globals.questStorage;
    }
}
