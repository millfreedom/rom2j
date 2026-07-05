package ua.millfreedom.rom2.gameserver;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.gameserver.missionruntime.ScriptCheck;
import ua.millfreedom.rom2.gameserver.missionruntime.ScriptCheckType;
import ua.millfreedom.rom2.gameserver.missionruntime.ScriptCondition;
import ua.millfreedom.rom2.gameserver.missionruntime.ScriptInstant;
import ua.millfreedom.rom2.gameserver.missionruntime.ScriptPattern;
import ua.millfreedom.rom2.model.Building;
import ua.millfreedom.rom2.model.CPlayer;
import ua.millfreedom.rom2.model.Effect;
import ua.millfreedom.rom2.model.Inventory;
import ua.millfreedom.rom2.model.Inn;
import ua.millfreedom.rom2.model.Item;
import ua.millfreedom.rom2.model.Outpost;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.Pointer;
import ua.millfreedom.rom2.model.Shop;
import ua.millfreedom.rom2.model.ShopAssortmentEntry;
import ua.millfreedom.rom2.model.TargetHandle;
import ua.millfreedom.rom2.model.UnitGroup;
import ua.millfreedom.rom2.model.VirtualCaster;
import ua.millfreedom.rom2.model.enums.BuildingId;
import ua.millfreedom.rom2.model.enums.EffectId;
import ua.millfreedom.rom2.model.quest.InnQuestIndexBucket;
import ua.millfreedom.rom2.model.spell.Spell;
import ua.millfreedom.rom2.model.spell.Spellbook;
import ua.millfreedom.rom2.model.spell.TransientSpellCastSpec;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.unit.UnitInfo;
import ua.millfreedom.rom2.model.unit.humanoid.human.Human;
import ua.millfreedom.rom2.model.unit.humanoid.human.HumanInfo;
import ua.millfreedom.rom2.model.world.CWorldMap;
import ua.millfreedom.rom2.model.world.ScenarioDescriptor;
import ua.millfreedom.rom2.model.world.scenario.BuildingDTO;
import ua.millfreedom.rom2.model.world.scenario.EffectDTO;
import ua.millfreedom.rom2.model.world.scenario.EffectOrTrapMod;
import ua.millfreedom.rom2.model.world.scenario.GroupDTO;
import ua.millfreedom.rom2.model.world.scenario.InnDescriptor;
import ua.millfreedom.rom2.model.world.scenario.Instant;
import ua.millfreedom.rom2.model.world.scenario.PostDescriptor;
import ua.millfreedom.rom2.model.world.scenario.ShopDescriptor;
import ua.millfreedom.rom2.model.world.scenario.Trigger;
import ua.millfreedom.rom2.model.world.scenario.UnitDTO;
import ua.millfreedom.rom2.model.world.scenario.WorldSack;
import ua.millfreedom.rom2.res.Resources;

import java.util.HashMap;
import java.util.Map;

import static ua.millfreedom.rom2.GameServer.LOCAL_CAMPAIGN_HERO_SERVER_ID;
import static ua.millfreedom.rom2.GameServer.SELF_PLAYER_NAME;
import static ua.millfreedom.rom2.gameserver.missionruntime.MissionDiplomacyState.SELF_RELATION_FLAGS;
import static ua.millfreedom.rom2.res.Constants.SCENARIO;

public final class ScenarioMapLoader {
    public static final int SCENARIO_SCRIPT_RECORD_LIMIT = 0x10002;
    private static final int MISSION_ENTRY_DROP_INSTANT_TYPE = SCENARIO_SCRIPT_RECORD_LIMIT;
    private static final int SCENARIO_SCRIPT_ITEM_ID_BASE = 0xE18;
    private static final int SCENARIO_SCRIPT_VARIABLE_BASE_MIN = 8000;
    private static final int SCENARIO_SCRIPT_VARIABLE_BASE_MAX = 9999;
    private static final int SCENARIO_SCRIPT_VARIABLE_SECONDARY_BASE = 9000;
    private static final int SCENARIO_SCRIPT_ARG_ITEM = 8;
    private static final int SCENARIO_SCRIPT_ARG_BUILDING = 9;
    private static final int SCENARIO_SCRIPT_LOCAL_HERO_UNIT_ID_BASE = 0x2711;
    private static final int SCENARIO_SCRIPT_NAMED_PC_UNIT_ID_BASE = 0x2AF9;
    private static final int SCENARIO_HUMANOID_UNIT_FLAG = 0x10;
    private static final String[] SCENARIO_SCRIPT_NAMED_PC_UNIT_NAMES = {
            "Danath",
            "Reniesta",
            "Fergard",
            "Naira",
            "Treyrak",
            "Brian",
            "Glaen",
            "Woman"
    };
    private static final int SCENARIO_EFFECT_MODE_TRANSIENT_SPELL_LIMIT = 4;
    private static final int SCENARIO_EFFECT_MODE_BUILDING_VIRTUAL_CASTER = 0x04;
    private static final int SCENARIO_EFFECT_MODE_UNIT_SPELLBOOK = 0x08;
    private static final int SCENARIO_GROUP_POST_RELEASE_INSTANT_FLAG = 0x01;
    private static final int SCENARIO_GROUP_KEEP_SPAWN_SPREAD_FLAG = 0x02;
    private static final int SCENARIO_GROUP_INN_RELOCATION_QUEST_FLAG = 0x04;
    private static final int SCENARIO_GROUP_HOSTILE_RELOCATION_QUEST_FLAG = 0x08;

    /**
     * Native: ScenarioMapLoader::New @00539CA0. Skipped; Java ScenarioMapLoader is a static native support holder.
     */
    private ScenarioMapLoader() {
    }

    /**
     * Fully ported. Native: ScenarioMapLoader::loadScenarioMap @005606AA.
     */
    public static int loadScenarioMap(String sourceMapName) {
        String scenarioPath = resolveScenarioLoadPath(sourceMapName);
        Globals.gameServer.scenarioDescriptor = new ScenarioDescriptor(scenarioPath);
        if (Globals.gameServer.scenarioDescriptor.sec2Heights == null) {
            pushScenarioMapError(Globals.gameServer.scenarioDescriptor, scenarioPath);
            return 1;
        }

        ScenarioDescriptor scenario = Globals.gameServer.scenarioDescriptor;
        Globals.gameServer.networkSessionActive = scenario.recommendedPlayers > 1 ? 1 : 0;
        Globals.gameServer.field26_0x178 = Globals.gameServer.networkSessionActive == 0 ? 1 : 0;
        Globals.gameServer.mapName = scenario.mapName.toString();
        Globals.gameServer.difficultyLevel = scenario.mapLevel;

        materializeScenarioPlayers(scenario);

        CWorldMap worldMap = new CWorldMap(scenario, Globals.gameServer.activeUnits);
        Globals.gameServer.worldMap = worldMap;
        Globals.worldMap = worldMap;
        Globals.gameServer.missionScriptRuntime = new MissionScriptRuntime(worldMap, Globals.gameServer.playerList);

        materializeScenarioBuildings(scenario);
        applyScenarioDiplomacy(scenario);
        materializeScenarioUnits(scenario);
        applyNetworkSelfVisibilityMasking();
        materializeScenarioScriptRuntime(scenario, 1);
        materializeScenarioSacksAndEffects(scenario);
        if (Globals.gameServer.networkSessionActive != 0) {
            materializeNetworkOutposts(scenario);
        }
        Globals.gameServer.postWorldLoadRebind();

        Globals.gameServer.useGlobalCampaignShop = Globals.gameServer.networkSessionActive == 0 ? 1 : 0;
        return 0;
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::loadScenarioMap @005606AA scenario-resource path
     * branch.
     */
    private static String resolveScenarioLoadPath(String sourceMapName) {
        if (Globals.gameServer.scenarioMapLoaderGameMapNumber != 0) {
            return Resources.path(SCENARIO, sourceMapName);
        }
        return sourceMapName;
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::loadScenarioMap @005606AA scenario load-error
     * message switch.
     */
    private static void pushScenarioMapError(ScenarioDescriptor scenario, String sourceMapName) {
        String errorText = switch (scenario.error) {
            case ScenarioDescriptor.LOAD_ERR_FILE_NOT_FOUND -> "File not found";
            case ScenarioDescriptor.LOAD_ERR_NOT_A_MAP_FILE -> "Not a map file";
            case ScenarioDescriptor.LOAD_ERR_WRONG_BLOCK_NUMBER -> "Wrong block number";
            case ScenarioDescriptor.LOAD_ERR_MAP_VERSION_TOO_NEW -> "Map version too new, update loader";
            case ScenarioDescriptor.LOAD_ERR_TILES_BLOCK_NOT_FOUND -> "Tiles block not found";
            case ScenarioDescriptor.LOAD_ERR_ALTITUDES_BLOCK_NOT_FOUND -> "Altitudes block not found";
            default -> "Unknown error";
        };
        Globals.gameServer.pushMessage("Map error: " + errorText + " in " + sourceMapName);
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::loadScenarioMap @005606AA diplomacy copy.
     */
    private static void applyScenarioDiplomacy(ScenarioDescriptor scenario) {
        for (Player player : Globals.gameServer.playerList.players) {
            int playerIndex = (short) player.playerId - 1;
            if (playerIndex < scenario.sec5Players.size()) {
                CPlayer scenarioPlayer = scenario.sec5Players.get(playerIndex);
                for (int relationIndex = 0; relationIndex < scenarioPlayer.diplomacyFlags.length; relationIndex++) {
                    Globals.gameServer.missionScriptRuntime.missionDiplomacyState.setRelationFlags(
                            player.playerId,
                            relationIndex + 1,
                            scenarioPlayer.diplomacyFlags[relationIndex]
                    );
                }
            }
            Globals.gameServer.missionScriptRuntime.missionDiplomacyState.setRelationFlags(
                    player.playerId,
                    player.playerId,
                    SELF_RELATION_FLAGS
            );
        }
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::loadScenarioMap @005606AA Self visibility masking
     * branch.
     */
    private static void applyNetworkSelfVisibilityMasking() {
        if (Globals.gameServer.networkSessionActive == 0) {
            return;
        }
        Player self = Globals.gameServer.playerList.getByName(SELF_PLAYER_NAME);
        if (self == null) {
            return;
        }
        for (Player player : Globals.gameServer.playerList.players) {
            if ((Globals.gameServer.missionScriptRuntime.missionDiplomacyState.relationFlags(
                    player.playerId,
                    self.playerId
            ) & 7) == 0
                    && player.isActive != 0) {
                for (Unit unit : player.ownedUnits) {
                    unit.suppressDeathLootFlag = 1;
                }
            }
        }
    }

    /**
     * Fully ported. Native: ScenarioMapLoader::materializeScenarioPlayers @005612AF.
     */
    public static void materializeScenarioPlayers(ScenarioDescriptor scenario) {
        int scenarioPlayerIndex = 1;
        for (CPlayer scenarioPlayer : scenario.sec5Players) {
            if (scenarioPlayer != null) {
                Player player;
                if (Globals.gameServer.networkSessionActive == 0
                        && scenarioPlayerIndex == 1
                        && Globals.gameServer.playerList.getPlayersCount() == 1) {
                    player = Globals.gameServer.playerList.getFirst();
                } else {
                    player = new Player();
                    player.name = scenarioPlayer.name.toString();
                    player.isActive = 1;
                    player.scenarioPlayerId = scenarioPlayer.playerId;
                    Globals.gameServer.playerList.addAssigningIdAndScanMask(player);
                }
                player.colorSlot = scenarioPlayer.color + 1;
                player.isActive = 1;
                if (Globals.gameServer.networkSessionActive == 0 && scenarioPlayerIndex == 1) {
                    player.isActive = 0;
                }
                player.playerEliminationQuestEnabled = (scenarioPlayer.flags & 2) != 0 ? 1 : 0;
            }
            scenarioPlayerIndex++;
        }
    }

    /**
     * Fully ported. Native: ScenarioMapLoader::materializeScenarioBuildings @00561422.
     */
    public static void materializeScenarioBuildings(ScenarioDescriptor scenario) {
        if (Globals.worldMap == null) {
            return;
        }
        Globals.questStorage.clearSecondaryIndexBuckets();
        for (int buildingIndex = 0; buildingIndex < scenario.sec4Buildings.size(); buildingIndex++) {
            BuildingDTO buildingDTO = scenario.sec4Buildings.get(buildingIndex);
            if (buildingDTO == null) {
                continue;
            }
            Building building = createScenarioBuilding(buildingDTO);
            applyScenarioPlainBuildingHpOverride(building, buildingDTO);
            if (building instanceof Shop shop) {
                applyScenarioShopDescriptor(shop, scenario, buildingDTO.buildingID);
            }
            if (building instanceof Inn inn) {
                applyScenarioInnDescriptor(inn, scenario, buildingDTO.buildingID);
            }
            if (building instanceof Pointer pointer) {
                applyScenarioPointerPostDescriptor(pointer, scenario, buildingDTO.buildingID);
            }
            Player owner = Globals.gameServer.playerList.findByScenarioPlayerId(buildingDTO.playerID);
            building.owner = owner;
            if (owner == null) {
                Globals.gameServer.pushMessage("Warning - Building without owner have been loaded");
            }
            building.scenarioObjectId = buildingDTO.buildingID;
            Globals.gameServer.objectLists.buildings.addAndAssignScenarioId(building);
            if (building instanceof Inn) {
                Globals.questStorage.registerSecondaryIndexBucket(new InnQuestIndexBucket(
                        building.idFull,
                        building.m_pTargetHandle,
                        Globals.gameServer.missionScriptRuntime,
                        4
                ));
            }
            scenario.sec4Buildings.set(buildingIndex, null);
        }
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioBuildings @00561422 concrete
     * building constructor branch.
     */
    private static Building createScenarioBuilding(BuildingDTO buildingDTO) {
        int buildingTypeId = buildingDTO.typeID & 0xFF;
        BuildingId buildingId = BuildingId.fromId(buildingTypeId);
        TargetHandle target = new TargetHandle();
        target.initFromBytes(buildingDTO.x, buildingDTO.y, Globals.worldMap);
        if (isScenarioShopBuilding(buildingId)) {
            return new Shop(buildingTypeId, target);
        }
        if (isScenarioInnBuilding(buildingId)) {
            return new Inn(buildingTypeId, target);
        }
        if (isScenarioPointerBuilding(buildingId)) {
            return new Pointer(buildingTypeId, target, buildingDTO.sizeX, buildingDTO.sizeY);
        }
        return new Building(buildingTypeId, target, buildingDTO.sizeX, buildingDTO.sizeY);
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioBuildings @00561422 plain
     * building HP-zero override branch.
     */
    private static void applyScenarioPlainBuildingHpOverride(Building building, BuildingDTO buildingDTO) {
        BuildingId buildingId = BuildingId.fromId(buildingDTO.typeID & 0xFF);
        if (buildingDTO.hp == 0
                && !isScenarioShopBuilding(buildingId)
                && !isScenarioInnBuilding(buildingId)
                && !isScenarioPointerBuilding(buildingId)) {
            building.healthCurrent = buildingDTO.hp;
        }
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioBuildings @00561422
     * shop-descriptor copy.
     */
    private static void applyScenarioShopDescriptor(Shop shop, ScenarioDescriptor scenario, int buildingId) {
        for (ShopDescriptor descriptor : scenario.sect11ShopDescriptors) {
            if (descriptor.id != buildingId) {
                continue;
            }
            for (int shelf = 0; shelf < shop.localShopAssortment.length; shelf++) {
                ShopAssortmentEntry entry = shop.localShopAssortment[shelf];
                entry.minPrice = descriptor.minPrices[shelf];
                entry.maxPrice = descriptor.maxPrices[shelf];
                entry.itemCount = descriptor.maxItems[shelf];
                entry.maxSameTypeItems = descriptor.maxSameTypeItems[shelf];
                entry.selectionMask = descriptor.shelfFlags[shelf];
            }
            return;
        }
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioBuildings @00561422
     * inn-descriptor copy.
     */
    private static void applyScenarioInnDescriptor(Inn inn, ScenarioDescriptor scenario, int buildingId) {
        for (InnDescriptor descriptor : scenario.sect11InnDescriptors) {
            if (descriptor.id != buildingId) {
                continue;
            }
            inn.itemDeliveryQuestItemId = (descriptor.flags & InnDescriptor.FLAG_ITEM_DELIVERY) != 0
                    ? descriptor.itemID & 0xFFFF
                    : 0;
            inn.killAllHumansQuestEnabled = (descriptor.flags & InnDescriptor.FLAG_KILL_ALL_HUMANS) != 0 ? 1 : 0;
            inn.killAllMonstersQuestEnabled = (descriptor.flags & InnDescriptor.FLAG_KILL_ALL_MONSTERS) != 0 ? 1 : 0;
            inn.killAllUndeadNecroQuestEnabled = (descriptor.flags & InnDescriptor.FLAG_KILL_ALL_UNDEAD_NECRO) != 0
                    ? 1
                    : 0;
            inn.raiseDeadQuestEnabled = (descriptor.flags & InnDescriptor.FLAG_RAISE_DEAD) != 0 ? 1 : 0;
            return;
        }
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioBuildings @00561422
     * pointer post-descriptor copy.
     */
    private static void applyScenarioPointerPostDescriptor(Pointer pointer, ScenarioDescriptor scenario, int buildingId) {
        for (PostDescriptor descriptor : scenario.sect11PostDescriptors) {
            if (descriptor.id != buildingId) {
                continue;
            }
            if (descriptor.instanceOn != 0) {
                pointer.scriptInstantIndex = compressedScriptInstantIndexBeforeScenarioInstant(
                        scenario,
                        descriptor.instanceID
                );
            }
            return;
        }
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioBuildings @00561422 and
     * ScenarioMapLoader::materializeNetworkOutpostForGroup @00560D9B compressed instant-index loops.
     */
    private static int compressedScriptInstantIndexBeforeScenarioInstant(ScenarioDescriptor scenario, int instanceId) {
        int scriptInstantIndex = 0;
        for (int instantPosition = 0; instantPosition < instanceId; instantPosition++) {
            if (scenario.sect7Instants.get(instantPosition).typeId < ScenarioMapLoader.SCENARIO_SCRIPT_RECORD_LIMIT) {
                scriptInstantIndex++;
            }
        }
        return scriptInstantIndex;
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioBuildings @00561422 shop class
     * branch.
     */
    private static boolean isScenarioShopBuilding(BuildingId buildingId) {
        return buildingId.isBetween(BuildingId.SHOP, BuildingId.SHOP_2)
                || buildingId.isBetween(BuildingId.KAARG_SHOP_1, BuildingId.MULTIPLAYER_KAARG_SHOP_3)
                || buildingId.isBetween(BuildingId.DRUID_SHOP_1, BuildingId.MULTIPLAYER_DRUID_SHOP_3);
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioBuildings @00561422 inn class
     * branch.
     */
    private static boolean isScenarioInnBuilding(BuildingId buildingId) {
        return buildingId.isBetween(BuildingId.INN_1, BuildingId.INN_3)
                || buildingId.isBetween(BuildingId.MULTIPLAYER_INN_1, BuildingId.MULTIPLAYER_INN_3)
                || buildingId.isBetween(BuildingId.KAARG_INN_1, BuildingId.MULTIPLAYER_KAARG_INN_3)
                || buildingId.isBetween(BuildingId.DRUID_INN_1, BuildingId.MULTIPLAYER_DRUID_INN_3);
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioBuildings @00561422 pointer class
     * branch.
     */
    private static boolean isScenarioPointerBuilding(BuildingId buildingId) {
        return buildingId.isBetween(BuildingId.POINTER_1, BuildingId.POINTER_6);
    }

    /**
     * Fully ported. Native: ScenarioMapLoader::materializeScenarioUnits @00561BF9.
     */
    public static void materializeScenarioUnits(ScenarioDescriptor scenario) {
        if (Globals.worldMap == null) {
            return;
        }
        for (UnitDTO scenarioUnit : scenario.sec6Units) {
            if (scenarioUnit == null) {
                continue;
            }
            Player owner = Globals.gameServer.playerList.findByScenarioPlayerId(scenarioUnit.playerID);
            int scenarioUnitId = scenarioRuntimeUnitId(scenarioUnit);
            if (owner == null) {
                Globals.gameServer.issueWarning("Can't resolve player %d for unit %d.".formatted(
                        (int) (short) scenarioUnit.playerID,
                        scenarioUnitId
                ));
                continue;
            }
            if (Globals.gameServer.field174 == 0 || owner.isActive == 0) {
                Unit unit = createScenarioRuntimeUnit(scenarioUnit);
                applyScenarioQuestFlags(unit, scenarioUnit);
                if (unit.getTokenTypeId() == 0) {
                    Globals.gameServer.issueWarning("Invalid unit %d during loading.".formatted(scenarioUnitId));
                    continue;
                }
                unit.recalculateDerivedStats();
                applyScenarioHealthOverrides(unit, scenarioUnit);
                int targetX = (scenarioUnit.x >>> 8) & 0xFF;
                int targetY = (scenarioUnit.y >>> 8) & 0xFF;
                if (!unit.placeNearMissionCell(targetX, targetY, 0)) {
                    Globals.gameServer.issueWarning("Can't place unit %d during loading on the tile(%d, %d).".formatted(
                            scenarioUnitId,
                            targetX,
                            targetY
                    ));
                    continue;
                }
                unit.scenarioObjectId = scenarioUnitId;
                Globals.gameServer.activeUnits.addAndAssignScenarioId(unit);
                unit.owner = owner;
                owner.ownedUnits.add(unit);
                findOrCreateScenarioUnitGroup(owner, scenarioUnit.groupID).addUnit(unit);
            }
        }
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioUnits @00561BF9 and
     * UnitList::AddAndAssignScenarioId @0052B281.
     */
    private static int scenarioRuntimeUnitId(UnitDTO scenarioUnit) {
        return (short) scenarioUnit.unitID;
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioUnits @00561BF9 unit construction
     * branch.
     */
    private static Unit createScenarioRuntimeUnit(UnitDTO scenarioUnit) {
        if ((scenarioUnit.unitFlags1 & SCENARIO_HUMANOID_UNIT_FLAG) == 0) {
            int unitInfoIndex = Globals.staticDataMgr.findUnitByServerId(scenarioUnit.serverID);
            UnitInfo unitInfo = Globals.staticDataMgr.units.get(unitInfoIndex);
            Unit unit = Unit.createFromTemplateName(unitInfo.name);
            applySinglePlayerDifficultyScaling(unit);
            return unit;
        }

        int humanInfoIndex = Globals.staticDataMgr.findHumanByServerId(scenarioUnit.serverID);
        HumanInfo humanInfo = Globals.staticDataMgr.humans.get(humanInfoIndex);
        return Human.createFromTemplate(humanInfo.name, humanInfo.name.indexOf("_Hero") > 0, false);
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioUnits @00561BF9 single-player
     * difficulty branch.
     */
    private static void applySinglePlayerDifficultyScaling(Unit unit) {
        if (Globals.gameServer.networkSessionActive != 0 || Globals.gameServer.difficultyLevelSetting == 2) {
            return;
        }
        if (Globals.gameServer.difficultyLevelSetting == 1) {
            unit.m_nMaxHP /= 2;
            unit.m_nHP = unit.m_nMaxHP;
            unit.missionRuntimeState.wimpy >>= 1;
            return;
        }
        unit.skillData.toHit += 0x32;
        unit.unitStatData.defence += 0x32;
        unit.m_nMaxHP = (int) (unit.m_nMaxHP * 1.5d);
        unit.m_nHP = unit.m_nMaxHP;
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioUnits @00561BF9 scenario
     * quest-flag copy.
     */
    private static void applyScenarioQuestFlags(Unit unit, UnitDTO scenarioUnit) {
        unit.innUnitRelocationQuestFlag = (scenarioUnit.questFlags & 1) != 0 ? 1 : 0;
        unit.hostileUnitRelocationQuestFlag = (scenarioUnit.questFlags & 2) != 0 ? 1 : 0;
        unit.innRecruitmentQuestFlag = (scenarioUnit.questFlags & 4) != 0 ? 1 : 0;
        unit.suppressDeathLootFlag = (scenarioUnit.questFlags & 8) != 0 ? 1 : 0;
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioUnits @00561BF9 HP override
     * branch.
     */
    private static void applyScenarioHealthOverrides(Unit unit, UnitDTO scenarioUnit) {
        int scenarioHp = signedScenarioShort(scenarioUnit.hp);
        int scenarioMaxHp = signedScenarioShort(scenarioUnit.maxHp);
        if ((scenarioUnit.unitFlags1 & SCENARIO_HUMANOID_UNIT_FLAG) != 0 && scenarioMaxHp != -1 && scenarioMaxHp == scenarioHp) {
            scenarioHp = -1;
            scenarioMaxHp = -1;
        }
        if (scenarioMaxHp != -1) {
            unit.m_nMaxHP = scenarioMaxHp;
        }
        if (scenarioHp == -1) {
            unit.m_nHP = unit.m_nMaxHP;
            return;
        }
        unit.m_nHP = scenarioHp;
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioUnits @00561BF9 signed WORD
     * checks.
     */
    private static int signedScenarioShort(int value) {
        return (short) (value & 0xFFFF);
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioUnits @00561BF9 unit-group
     * lookup/create branch.
     */
    private static UnitGroup findOrCreateScenarioUnitGroup(Player owner, int groupID) {
        for (UnitGroup group : owner.unitGroups) {
            if (group.groupKey == groupID) {
                return group;
            }
        }
        UnitGroup group = new UnitGroup();
        group.groupKey = groupID;
        group.owner = owner;
        owner.unitGroups.add(group);
        return group;
    }

    /**
     * Fully ported. Native: ScenarioMapLoader::materializeScenarioScriptRuntime @00562745.
     */
    public static void materializeScenarioScriptRuntime(ScenarioDescriptor scenario, int initializeLoadedGroups) {
        Map<Integer, Unit> unitsByScenarioId = new HashMap<>();
        Map<Integer, UnitGroup> groupsByScenarioId = new HashMap<>();
        Map<Integer, Player> playersByScenarioId = new HashMap<>();
        Map<Integer, Building> buildingsByScenarioId = new HashMap<>();
        buildScenarioScriptReferenceMaps(
                unitsByScenarioId,
                groupsByScenarioId,
                playersByScenarioId,
                buildingsByScenarioId
        );

        if (initializeLoadedGroups != 0 && Globals.gameServer.networkSessionActive == 0) {
            initializeScenarioUnitGroupScriptState();
        }

        Map<Integer, Integer> instantDigitsByScenarioIndex = materializeScenarioScriptInstants(
                scenario,
                unitsByScenarioId,
                groupsByScenarioId,
                playersByScenarioId,
                buildingsByScenarioId
        );
        Map<Integer, Integer> checkDigitsByScenarioIndex = materializeScenarioScriptChecks(
                scenario,
                unitsByScenarioId,
                groupsByScenarioId,
                playersByScenarioId,
                buildingsByScenarioId
        );
        materializeScenarioScriptPatterns(scenario, instantDigitsByScenarioIndex, checkDigitsByScenarioIndex);
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioScriptRuntime @00562745
     * reference-map setup.
     */
    private static void buildScenarioScriptReferenceMaps(
            Map<Integer, Unit> unitsByScenarioId,
            Map<Integer, UnitGroup> groupsByScenarioId,
            Map<Integer, Player> playersByScenarioId,
            Map<Integer, Building> buildingsByScenarioId
    ) {
        for (Building building : Globals.gameServer.objectLists.buildings) {
            buildingsByScenarioId.put(building.scenarioObjectId, building);
        }
        for (Unit unit : Globals.gameServer.activeUnits) {
            unitsByScenarioId.put(unit.scenarioObjectId, unit);
        }
        for (Unit unit : Globals.gameServer.objectLists.corpses) {
            unitsByScenarioId.put(unit.scenarioObjectId, unit);
        }
        for (Player player : Globals.gameServer.playerList.players) {
            playersByScenarioId.put(player.scenarioPlayerId, player);
            for (UnitGroup group : player.unitGroups) {
                groupsByScenarioId.put(group.groupKey, group);
            }
            for (Unit unit : player.ownedUnits) {
                unitsByScenarioId.put(unit.scenarioObjectId, unit);
            }
        }
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioScriptRuntime @00562745
     * scenario unit-group initialization branch.
     */
    private static void initializeScenarioUnitGroupScriptState() {
        for (Player player : Globals.gameServer.playerList.players) {
            for (UnitGroup group : player.unitGroups) {
                Player owner = group.owner;
                if (owner.playerId == 1) {
                    group.initializeScenarioMissionEntryGroup(Globals.gameServer.missionScriptRuntime);
                } else {
                    Globals.gameServer.missionScriptRuntime.initializeLoadedScenarioGroup(group, 0);
                }
            }
        }
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioScriptRuntime @00562745
     * sect7 instant loop.
     */
    private static Map<Integer, Integer> materializeScenarioScriptInstants(
            ScenarioDescriptor scenario,
            Map<Integer, Unit> unitsByScenarioId,
            Map<Integer, UnitGroup> groupsByScenarioId,
            Map<Integer, Player> playersByScenarioId,
            Map<Integer, Building> buildingsByScenarioId
    ) {
        Globals.gameServer.missionScriptRuntime.scriptInstants.clear();
        Globals.gameServer.missionEntryDropCells.clear();
        for (Instant instant : scenario.sect7Instants) {
            if (instant.typeId < SCENARIO_SCRIPT_RECORD_LIMIT) {
                Globals.gameServer.missionScriptRuntime.scriptInstants.add(new ScriptInstant());
            }
        }

        Map<Integer, Integer> instantDigitsByScenarioIndex = new HashMap<>();
        int digit = 0;
        for (Instant instant : scenario.sect7Instants) {
            if (instant.typeId < SCENARIO_SCRIPT_RECORD_LIMIT) {
                ScriptInstant scriptInstant = new ScriptInstant();
                if (populateScenarioScriptRecord(
                        scriptInstant,
                        instant,
                        unitsByScenarioId,
                        groupsByScenarioId,
                        playersByScenarioId,
                        buildingsByScenarioId,
                        true,
                        false
                )) {
                    scriptInstant.type = instant.typeId;
                    scriptInstant.digit = digit;
                    Globals.gameServer.missionScriptRuntime.scriptInstants.set(digit, scriptInstant);
                    instantDigitsByScenarioIndex.put(instant.index, digit);
                    digit++;
                } else {
                    Globals.gameServer.issueWarning("Failed to create instant " + instant.index + " " + instant.name);
                }
            } else if (instant.typeId == MISSION_ENTRY_DROP_INSTANT_TYPE) {
                addScenarioMissionEntryDropCell(instant);
            }
        }
        return instantDigitsByScenarioIndex;
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioScriptRuntime @00562745
     * sect7 check loop.
     */
    private static Map<Integer, Integer> materializeScenarioScriptChecks(
            ScenarioDescriptor scenario,
            Map<Integer, Unit> unitsByScenarioId,
            Map<Integer, UnitGroup> groupsByScenarioId,
            Map<Integer, Player> playersByScenarioId,
            Map<Integer, Building> buildingsByScenarioId
    ) {
        Globals.gameServer.missionScriptRuntime.scriptChecks.clear();
        Map<Integer, Integer> checkDigitsByScenarioIndex = new HashMap<>();
        int specialCheckIndex = 0;
        for (Instant check : scenario.sect7Checks) {
            int digit = specialCheckIndex;
            checkDigitsByScenarioIndex.put(check.index, digit);
            if (check.typeId < SCENARIO_SCRIPT_RECORD_LIMIT) {
                ScriptCheck scriptCheck = new ScriptCheck();
                scriptCheck.type = check.typeId;
                scriptCheck.digit = digit;
                scriptCheck.executeOnce = check.executeOnce;
                scriptCheck.referenceCount = 0;
                if (populateScenarioScriptRecord(
                        scriptCheck,
                        check,
                        unitsByScenarioId,
                        groupsByScenarioId,
                        playersByScenarioId,
                        buildingsByScenarioId,
                        check.typeId != ScriptCheckType.GROUP_UNIT_COUNT.id,
                        true
                )) {
                    Globals.gameServer.missionScriptRuntime.scriptChecks.add(scriptCheck);
                    specialCheckIndex++;
                } else {
                    checkDigitsByScenarioIndex.put(check.index, 0);
                    Globals.gameServer.issueWarning("Failed to create check " + check.index + " " + check.name);
                }
            } else if (check.typeId == MISSION_ENTRY_DROP_INSTANT_TYPE) {
                Globals.gameServer.missionScriptRuntime.writeScriptVariable(specialCheckIndex, check.arguments[0].value);
                specialCheckIndex++;
            }
        }
        return checkDigitsByScenarioIndex;
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioScriptRuntime @00562745
     * trigger loop.
     */
    private static void materializeScenarioScriptPatterns(
            ScenarioDescriptor scenario,
            Map<Integer, Integer> instantDigitsByScenarioIndex,
            Map<Integer, Integer> checkDigitsByScenarioIndex
    ) {
        Globals.gameServer.missionScriptRuntime.scriptPatterns.clear();
        for (int triggerIndex = 0; triggerIndex < scenario.sect7Triggers.size(); triggerIndex++) {
            Trigger trigger = scenario.sect7Triggers.get(triggerIndex);
            if (trigger.checkIds[0] == 0) {
                continue;
            }

            ScriptPattern pattern = new ScriptPattern();
            for (int pair = 0; pair < 3; pair++) {
                int leftCheckId = trigger.checkIds[pair * 2];
                int rightCheckId = trigger.checkIds[pair * 2 + 1];
                if (leftCheckId != 0 && rightCheckId != 0) {
                    int leftDigit = checkDigitsByScenarioIndex.getOrDefault(leftCheckId, 0);
                    int rightDigit = checkDigitsByScenarioIndex.getOrDefault(rightCheckId, 0);
                    incrementScenarioScriptCheckReferenceCount(leftDigit);
                    incrementScenarioScriptCheckReferenceCount(rightDigit);
                    pattern.conditions.add(new ScriptCondition(
                            leftDigit,
                            rightDigit,
                            scenarioTriggerComparison(trigger, pair)
                    ));
                }
            }

            for (int instantIndex = 0; instantIndex < trigger.instantIds.length; instantIndex++) {
                int instantId = trigger.instantIds[instantIndex];
                if (instantId != 0) {
                    if (instantId > 5000) {
                        Globals.gameServer.issueWarning("Reference to suspicious instant " + instantId);
                    }
                    pattern.instantIds.add(instantDigitsByScenarioIndex.getOrDefault(instantId, 0));
                }
            }
            pattern.selfDestruct = trigger.runOnce;
            pattern.digit = triggerIndex;
            Globals.gameServer.missionScriptRuntime.scriptPatterns.add(pattern);
        }
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioScriptRuntime @00562745
     * check reference-count refresh inside the trigger loop.
     */
    private static void incrementScenarioScriptCheckReferenceCount(int digit) {
        for (ScriptCheck check : Globals.gameServer.missionScriptRuntime.scriptChecks) {
            if (check.digit == digit) {
                check.referenceCount++;
                break;
            }
        }
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioScriptRuntime @00562745
     * trigger comparison fields.
     */
    private static int scenarioTriggerComparison(Trigger trigger, int pair) {
        return switch (pair) {
            case 0 -> trigger.check12Operator;
            case 1 -> trigger.check34Operator;
            default -> trigger.check56Operator;
        };
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioScriptRuntime @00562745
     * shared instant/check argument resolution loop.
     */
    private static boolean populateScenarioScriptRecord(
            ScriptInstant record,
            Instant source,
            Map<Integer, Unit> unitsByScenarioId,
            Map<Integer, UnitGroup> groupsByScenarioId,
            Map<Integer, Player> playersByScenarioId,
            Map<Integer, Building> buildingsByScenarioId,
            boolean markScenarioGroupReferences,
            boolean preferPrimaryUnitReference
    ) {
        boolean firstUnitResolved = false;
        boolean firstGroupResolved = false;
        boolean firstPlayerResolved = false;
        boolean success = true;
        int compactValueIndex = 0;
        Unit secondaryUnit = null;
        UnitGroup secondaryGroup = null;

        for (int argIndex = 0; argIndex < source.arguments.length; argIndex++) {
            int argType = source.arguments[argIndex].type;
            int argValue = source.arguments[argIndex].value;
            record.argTypes[argIndex] = argType;
            if (isScenarioScriptVariableReference(argType, argValue)) {
                source.arguments[argIndex].value = populateScenarioScriptVariableReference(record, argIndex, argValue);
                continue;
            }

            switch (argType) {
                case MissionScriptRuntime.SCENARIO_SCRIPT_ARG_GROUP -> {
                    UnitGroup group = resolveScenarioScriptGroup(groupsByScenarioId, argValue);
                    if (!firstGroupResolved) {
                        record.group = group;
                    } else {
                        record.secondaryTarget = group;
                        secondaryGroup = group;
                    }
                    if (group == null) {
                        success = false;
                    }
                    markScenarioGroupArgumentReference(record, secondaryGroup, markScenarioGroupReferences);
                    firstGroupResolved = true;
                }
                case MissionScriptRuntime.SCENARIO_SCRIPT_ARG_PLAYER -> {
                    Player player = resolveScenarioScriptPlayer(playersByScenarioId, argValue);
                    if (!firstPlayerResolved) {
                        record.player = player;
                    } else {
                        record.secondaryTarget = player;
                    }
                    if (player == null) {
                        success = false;
                    }
                    firstPlayerResolved = true;
                }
                case MissionScriptRuntime.SCENARIO_SCRIPT_ARG_UNIT -> {
                    Unit unit = resolveScenarioScriptUnit(unitsByScenarioId, argValue);
                    if (!firstUnitResolved) {
                        record.unit = unit;
                    } else {
                        record.secondaryTarget = unit;
                        secondaryUnit = unit;
                    }
                    if (unit == null) {
                        success = false;
                    }
                    markScenarioUnitArgumentReference(
                            record,
                            secondaryUnit,
                            markScenarioGroupReferences,
                            preferPrimaryUnitReference
                    );
                    firstUnitResolved = true;
                }
                case SCENARIO_SCRIPT_ARG_ITEM -> record.itemId = (argValue + SCENARIO_SCRIPT_ITEM_ID_BASE) & 0xFFFF;
                case SCENARIO_SCRIPT_ARG_BUILDING -> {
                    record.building = buildingsByScenarioId.get(argValue);
                    if (record.building == null) {
                        Globals.gameServer.issueWarning("Can't resolve building " + argValue);
                        success = false;
                    }
                }
                default -> record.argValues[compactValueIndex++] = argValue;
            }
        }
        return success;
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioScriptRuntime @00562745
     * group-argument mission-state reference marking.
     */
    private static void markScenarioGroupArgumentReference(
            ScriptInstant record,
            UnitGroup secondaryGroup,
            boolean markScenarioGroupReferences
    ) {
        if (!markScenarioGroupReferences) {
            return;
        }
        if (record.group == null) {
            if (secondaryGroup != null) {
                markScenarioGroupReferenced(secondaryGroup);
            }
        } else {
            markScenarioGroupReferenced(record.group);
        }
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioScriptRuntime @00562745
     * unit-argument mission-state reference marking.
     */
    private static void markScenarioUnitArgumentReference(
            ScriptInstant record,
            Unit secondaryUnit,
            boolean markScenarioGroupReferences,
            boolean preferPrimaryUnitReference
    ) {
        if (!markScenarioGroupReferences) {
            return;
        }
        if (preferPrimaryUnitReference && record.unit != null && record.unit.unitGroup != null) {
            markScenarioGroupReferenced(record.unit.unitGroup);
        } else if (secondaryUnit != null && secondaryUnit.unitGroup != null) {
            markScenarioGroupReferenced(secondaryUnit.unitGroup);
        } else if (record.unit != null && record.unit.unitGroup != null) {
            markScenarioGroupReferenced(record.unit.unitGroup);
        }
    }

    /**
     * Fully ported native support extracted from FUN_00562633 @00562633.
     */
    private static Player resolveScenarioScriptPlayer(Map<Integer, Player> playersByScenarioId, int playerId) {
        Player player = playersByScenarioId.get(playerId);
        if (player == null) {
            Globals.gameServer.issueWarning("Can't resolve player " + playerId);
        }
        return player;
    }

    /**
     * Fully ported native support extracted from CMap<uint,UnitGroup*>::GetGroup @005626BC.
     */
    private static UnitGroup resolveScenarioScriptGroup(Map<Integer, UnitGroup> groupsByScenarioId, int groupId) {
        UnitGroup group = groupsByScenarioId.get(groupId);
        if (group == null) {
            Globals.gameServer.issueWarning("Can't resolve group " + groupId);
        }
        return group;
    }

    /**
     * Fully ported native support extracted from FUN_00562274 @00562274.
     */
    private static Unit resolveScenarioScriptUnit(Map<Integer, Unit> unitsByScenarioId, int unitId) {
        Unit unit;
        if (unitId < SCENARIO_SCRIPT_LOCAL_HERO_UNIT_ID_BASE) {
            unit = unitsByScenarioId.get(unitId);
        } else if (unitId < SCENARIO_SCRIPT_NAMED_PC_UNIT_ID_BASE) {
            unit = findLocalScenarioHeroUnit(unitId - SCENARIO_SCRIPT_LOCAL_HERO_UNIT_ID_BASE);
        } else {
            unit = findNamedPcScenarioUnit(
                    SCENARIO_SCRIPT_NAMED_PC_UNIT_NAMES[unitId - SCENARIO_SCRIPT_NAMED_PC_UNIT_ID_BASE]
            );
        }
        if (unit == null) {
            Globals.gameServer.issueWarning(
                    (unitId < SCENARIO_SCRIPT_LOCAL_HERO_UNIT_ID_BASE ? "Can't resolve unit " : "Can't resolve hero ")
                            + unitId
                            + "."
            );
        }
        return unit;
    }

    /**
     * Fully ported native support extracted from FUN_00562518 @00562518.
     */
    private static Unit findLocalScenarioHeroUnit(int localHeroIndex) {
        if (Globals.gameServer.networkSessionActive != 0) {
            return null;
        }
        Player player = Globals.gameServer.playerList.getFirst();
        if (player.controlledUnit == null) {
            return null;
        }
        for (Unit unit : player.ownedUnits) {
            if ((unit.serverID & 0xFFFF) - LOCAL_CAMPAIGN_HERO_SERVER_ID == localHeroIndex) {
                return unit;
            }
        }
        for (Unit unit : Globals.gameServer.activeUnits) {
            if (!unit.str.isEmpty()
                    && (unit.serverID & 0xFFFF) - LOCAL_CAMPAIGN_HERO_SERVER_ID == localHeroIndex) {
                return unit;
            }
        }
        return null;
    }

    /**
     * Fully ported native support extracted from FUN_0056236C @0056236C.
     */
    private static Unit findNamedPcScenarioUnit(String pcNameSuffix) {
        if (Globals.gameServer.networkSessionActive != 0) {
            return null;
        }
        String unitNamePrefix = "PC_" + pcNameSuffix;
        Player player = Globals.gameServer.playerList.getFirst();
        for (Unit unit : player.ownedUnits) {
            if (unit.unitInfoLine.name.startsWith(unitNamePrefix)) {
                return unit;
            }
        }
        for (Unit unit : Globals.gameServer.activeUnits) {
            if (unit.unitInfoLine.name.startsWith(unitNamePrefix)) {
                return unit;
            }
        }
        return null;
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioScriptRuntime @00562745
     * variable-reference branch.
     */
    private static boolean isScenarioScriptVariableReference(int argType, int argValue) {
        return argValue >= SCENARIO_SCRIPT_VARIABLE_BASE_MIN
                && argValue <= SCENARIO_SCRIPT_VARIABLE_BASE_MAX
                && (argType == MissionScriptRuntime.SCENARIO_SCRIPT_ARG_GROUP
                || argType == MissionScriptRuntime.SCENARIO_SCRIPT_ARG_PLAYER
                || argType == MissionScriptRuntime.SCENARIO_SCRIPT_ARG_UNIT);
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioScriptRuntime @00562745
     * variable-reference decode.
     */
    private static int populateScenarioScriptVariableReference(ScriptInstant record, int argIndex, int argValue) {
        record.argValues[argIndex] = argValue;
        int normalized;
        int mode;
        if (argValue < SCENARIO_SCRIPT_VARIABLE_SECONDARY_BASE) {
            mode = ScriptInstant.VARIABLE_REFERENCE_MODE_PRIMARY;
            normalized = argValue - SCENARIO_SCRIPT_VARIABLE_BASE_MIN;
        } else {
            mode = ScriptInstant.VARIABLE_REFERENCE_MODE_SECONDARY;
            normalized = argValue - SCENARIO_SCRIPT_VARIABLE_SECONDARY_BASE;
        }
        record.setVariableReference(argIndex, mode, normalized);
        return normalized;
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioScriptRuntime @00562745
     * group reference marker.
     */
    private static void markScenarioGroupReferenced(UnitGroup group) {
        group.missionState.markScenarioScriptReferenced();
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioScriptRuntime @00562745
     * scenario instant type `0x10002` branch.
     */
    private static void addScenarioMissionEntryDropCell(Instant instant) {
        int x = instant.arguments[0].value & 0xFF;
        int y = instant.arguments[1].value & 0xFF;
        Globals.gameServer.missionEntryDropCells.add(x | (y << 8));
    }

    /**
     * Fully ported. Native: ScenarioMapLoader::materializeNetworkOutposts @0056122E.
     */
    public static void materializeNetworkOutposts(ScenarioDescriptor scenario) {
        for (Player player : Globals.gameServer.playerList.players) {
            if (player.isActive != 0) {
                for (UnitGroup group : player.unitGroups) {
                    materializeNetworkOutpostForGroup(group, scenario);
                }
            }
        }
    }

    /**
     * Fully ported. Native: ScenarioMapLoader::materializeNetworkOutpostForGroup @00560D9B.
     */
    private static void materializeNetworkOutpostForGroup(UnitGroup group, ScenarioDescriptor scenario) {
        if (group.units.isEmpty()) {
            return;
        }

        Outpost outpost = new Outpost(createNetworkOutpostTarget(group));
        outpost.owner = group.owner;
        outpost.groupKey = group.groupKey;
        outpost.respawnDelayTicks = (Globals.serverConfig.repopdelay * 0x78) / 100;
        outpost.respawnTimerTicks = outpost.respawnDelayTicks;
        outpost.setOutpostUnitsFromList(group.units);
        applyScenarioGroupDescriptor(outpost, group, scenario);

        Globals.gameServer.objectLists.buildings.addAndAssignScenarioId(outpost);
        Globals.gameServer.missionScriptRuntime.initializeLoadedScenarioGroup(group, 0);
        if (outpost.postReleaseActionId >= 0) {
            Globals.gameServer.missionScriptRuntime.executeScriptInstant(outpost.postReleaseActionId);
        }
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeNetworkOutpostForGroup @00560D9B
     * group-center target construction.
     */
    private static TargetHandle createNetworkOutpostTarget(UnitGroup group) {
        int sumX = 0;
        int sumY = 0;
        int count = 0;
        for (Unit unit : group.units) {
            sumX += unit.m_pTargetHandle.getX();
            sumY += unit.m_pTargetHandle.getY();
            count++;
        }

        int centerX = sumX / count;
        int centerY = sumY / count;
        TargetHandle target = new TargetHandle();
        target.initDefault();
        target.initFromBytes(centerX, centerY, Globals.worldMap);
        target.setPosition(centerX, centerY);
        return target;
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeNetworkOutpostForGroup @00560D9B
     * scenario group-descriptor copy.
     */
    private static void applyScenarioGroupDescriptor(Outpost outpost, UnitGroup group, ScenarioDescriptor scenario) {
        for (GroupDTO descriptor : scenario.sect10Groups) {
            if (descriptor.id != group.groupKey) {
                continue;
            }

            outpost.respawnDelayTicks = (descriptor.repopTime * Globals.serverConfig.repopdelay) / 100;
            outpost.respawnTimerTicks = outpost.respawnDelayTicks;
            if ((descriptor.flags & SCENARIO_GROUP_POST_RELEASE_INSTANT_FLAG) == 0) {
                outpost.postReleaseActionId = -1;
            } else {
                outpost.postReleaseActionId = compressedScriptInstantIndexBeforeScenarioInstant(
                        scenario,
                        descriptor.instID
                );
            }
            outpost.groupFlag0 = (descriptor.flags & SCENARIO_GROUP_INN_RELOCATION_QUEST_FLAG) != 0 ? 1 : 0;
            group.innGroupRelocationQuestFlag = outpost.groupFlag0;
            outpost.groupFlag1 = (descriptor.flags & SCENARIO_GROUP_HOSTILE_RELOCATION_QUEST_FLAG) != 0 ? 1 : 0;
            group.hostileGroupRelocationQuestFlag = outpost.groupFlag1;
            if ((descriptor.flags & SCENARIO_GROUP_KEEP_SPAWN_SPREAD_FLAG) == 0) {
                outpost.spawnSpread = 0;
            }
        }
    }

    /**
     * Fully ported. Native: ScenarioMapLoader::materializeScenarioSacksAndEffects @00564072.
     */
    public static void materializeScenarioSacksAndEffects(ScenarioDescriptor scenario) {
        Map<Integer, Unit> unitsByScenarioId = new HashMap<>();
        for (Unit unit : Globals.gameServer.activeUnits) {
            unitsByScenarioId.put(unit.scenarioObjectId, unit);
        }
        Map<Integer, Building> buildingsByScenarioId = new HashMap<>();
        for (Building building : Globals.gameServer.objectLists.buildings) {
            buildingsByScenarioId.put(building.scenarioObjectId, building);
        }

        for (EffectDTO effectDTO : scenario.sect9Effects) {
            materializeScenarioEffect(effectDTO, unitsByScenarioId, buildingsByScenarioId);
        }

        if (Globals.gameServer.networkSessionActive != 0) {
            return;
        }

        for (WorldSack worldSack : scenario.sect8Sacks) {
            Inventory inventory = null;
            Unit unit = null;
            if (worldSack.unitID == 0) {
                inventory = new Inventory();
            } else {
                unit = unitsByScenarioId.get(worldSack.unitID);
            }

            for (int itemIndex = 0; itemIndex < worldSack.itemPackedHashes.size(); itemIndex++) {
                Item item = createScenarioSackItem(worldSack, itemIndex, scenario);
                if (item == null) {
                    continue;
                }
                if (worldSack.unitID == 0) {
                    inventory.addItem(item);
                } else if (worldSack.incomingItemFlags.get(itemIndex) == 0) {
                    unit.inventory.addItem(item);
                } else {
                    unit.addIncomingObjectToInventory(item);
                }
            }

            if (worldSack.unitID == 0 && inventory != null) {
                TargetHandle target = new TargetHandle();
                target.initFromBytes((worldSack.x >>> 8) & 0xFF, (worldSack.y >>> 8) & 0xFF, Globals.worldMap);
                Globals.gameServer.objectLists.sacks.createOrMergeSackAtTarget(target, inventory, worldSack.gold, 0);
            }
        }
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioSacksAndEffects @00564072
     * scenario effect loop.
     */
    private static void materializeScenarioEffect(
            EffectDTO effectDTO,
            Map<Integer, Unit> unitsByScenarioId,
            Map<Integer, Building> buildingsByScenarioId
    ) {
        if ((effectDTO.x != 0 || effectDTO.y != 0)
                && (effectDTO.effectMode & 0xFFFF) < SCENARIO_EFFECT_MODE_TRANSIENT_SPELL_LIMIT) {
            materializeScenarioTransientSpellCell(effectDTO);
        }

        boolean allowBuildingVirtualCaster = true;
        if (effectDTO.pWorldSack == null
                && (effectDTO.effectMode & SCENARIO_EFFECT_MODE_UNIT_SPELLBOOK) != 0) {
            int unitScenarioId = packScenarioEffectReference(effectDTO);
            Unit unit = unitsByScenarioId.get(unitScenarioId);
            if (unit != null) {
                grantScenarioSpellbookSpell(unit, effectDTO.spellId);
            } else {
                Globals.gameServer.issueWarning("Can't resolve unit " + unitScenarioId + " for spellbook.");
                allowBuildingVirtualCaster = false;
            }
        }

        if (allowBuildingVirtualCaster
                && (effectDTO.x != 0 || effectDTO.y != 0)
                && effectDTO.pWorldSack == null
                && (effectDTO.effectMode & SCENARIO_EFFECT_MODE_BUILDING_VIRTUAL_CASTER) != 0) {
            int buildingScenarioId = packScenarioEffectReference(effectDTO);
            Building building = buildingsByScenarioId.get(buildingScenarioId);
            if (building == null) {
                Globals.gameServer.issueWarning("Can't resolve building " + buildingScenarioId + " for building spell.");
            } else {
                VirtualCaster virtualCaster = new VirtualCaster();
                virtualCaster.m_pTargetHandle.assignFrom(building.m_pTargetHandle);
                virtualCaster.targetSearchRadius = effectDTO.itemID & 0xFF;
                virtualCaster.spellCastSpec.spellId = effectDTO.spellId & 0xFF;
                virtualCaster.spellCastSpec.skillLevel = effectDTO.spellStrength & 0xFF;
                virtualCaster.spellCastSpec.sourceX = effectDTO.x & 0xFF;
                virtualCaster.spellCastSpec.sourceY = effectDTO.y & 0xFF;
                virtualCaster.owner = building.owner;
                Globals.gameServer.objectLists.virtualCasters.add(virtualCaster);
            }
        }
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioSacksAndEffects @00564072
     * transient spell-cell branch.
     */
    private static void materializeScenarioTransientSpellCell(EffectDTO effectDTO) {
        TransientSpellCastSpec spec = new TransientSpellCastSpec();
        spec.spellId = effectDTO.spellId & 0xFF;
        spec.skillLevel = effectDTO.spellStrength & 0xFF;
        EffectOrTrapMod source = effectDTO.carr.get(0);
        spec.sourceX = source.type & 0xFF;
        spec.sourceY = source.value & 0xFF;
        EffectOrTrapMod target = effectDTO.carr.get(1);
        spec.targetX = target.type & 0xFF;
        spec.targetY = target.value & 0xFF;
        Globals.worldMap.setTransientSpellCastAtCell(((effectDTO.y & 0xFF) << 8) | (effectDTO.x & 0xFF), spec);
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioSacksAndEffects @00564072
     * unit spellbook branch.
     */
    private static void grantScenarioSpellbookSpell(Unit unit, int spellId) {
        if (unit.spellbook == null) {
            unit.spellbook = new Spellbook();
            unit.status |= Unit.UNIT_STATUS_CAN_CAST;
            if (unit.m_nMaxMP != 0) {
                unit.status |= Unit.UNIT_STATUS_MAGE_CLASS;
            }
        }
        unit.spellbook.setAt(spellId & 0xFFFF, new Spell((byte) spellId));
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioSacksAndEffects @00564072
     * EffectDTO min/spread key.
     */
    private static int packScenarioEffectReference(EffectDTO effectDTO) {
        return (effectDTO.min & 0xFFFF) | ((effectDTO.spread & 0xFFFF) << 16);
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioSacksAndEffects @00564072
     * world-sack item loop.
     */
    public static Item createScenarioSackItem(WorldSack worldSack, int itemIndex, ScenarioDescriptor scenario) {
        Item item = Globals.staticDataMgr.createItemFromPackedHash(worldSack.itemPackedHashes.get(itemIndex) & 0xFFFF);
        if (item == null) {
            return null;
        }

        int effectIndex = worldSack.effectIndices.get(itemIndex);
        if (effectIndex != 0) {
            EffectDTO effectDTO = scenario.sect9Effects.get(effectIndex - 1);
            if (effectDTO.x != 0 || effectDTO.y != 0 || effectDTO.pWorldSack == null) {
                return null;
            }
            applyScenarioItemEffects(item, effectDTO);
        }
        return item;
    }

    /**
     * Fully ported native support extracted from ScenarioMapLoader::materializeScenarioSacksAndEffects @00564072
     * item-effect branch.
     */
    private static void applyScenarioItemEffects(Item item, EffectDTO effectDTO) {
        if (effectDTO.effectMode != 0) {
            Effect damageEffect = new Effect();
            damageEffect.id = EffectId.DAMAGE.id + (effectDTO.effectMode & 0xFFFF);
            damageEffect.mValue.setB1(effectDTO.min);
            damageEffect.mValue.setB2(effectDTO.spread);
            item.effects.add(damageEffect);
            item.recalculatePrice();
        }
        if (effectDTO.spellId != 0) {
            Effect spellEffect = new Effect();
            spellEffect.id = item.type == Item.ITEM_TYPE_BOOK ? EffectId.TEACH_SPELL.id : EffectId.CAST_SPELL.id;
            spellEffect.mValue.setS1(effectDTO.spellId);
            spellEffect.mValue.setS2(effectDTO.spellStrength);
            item.effects.add(spellEffect);
            item.recalculatePrice();
        }
        for (EffectOrTrapMod modifier : effectDTO.carr) {
            Effect modifierEffect = new Effect();
            modifierEffect.id = modifier.type == EffectId.CAST_SPELL.id
                    ? EffectId.DAMAGE_BONUS.id
                    : modifier.type;
            modifierEffect.mValue.setFull(modifier.value);
            item.effects.add(modifierEffect);
            item.recalculatePrice();
        }
    }
}
