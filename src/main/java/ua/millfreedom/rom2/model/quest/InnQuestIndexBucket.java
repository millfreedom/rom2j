package ua.millfreedom.rom2.model.quest;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.gameserver.MissionScriptRuntime;
import ua.millfreedom.rom2.model.Building;
import ua.millfreedom.rom2.model.CServerApp;
import ua.millfreedom.rom2.model.Item;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.TargetHandle;
import ua.millfreedom.rom2.model.UnitGroup;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.unit.UnitDirtyFlags;

/**
 * Native class: InnQuestIndexBucket.
 */
public final class InnQuestIndexBucket extends QuestIndexBucket {
    private static final int EQUIPMENT_BROADCAST_MASK = 0x0FFB;
    private static final int RAISE_DEAD_8_UNIT_TYPE = 0x52;
    private static final int RAISE_DEAD_9_UNIT_TYPE = 0x59;
    private static final int RAISE_DEAD_10_UNIT_TYPE = 0x45;
    private static final int RAISED_UNIT_KILL_HP = -0x32;

    //0x34
    public TargetHandle targetHandle;

    //0x38
    public MissionScriptRuntime missionRuntime;

    //0x3C
    public int radius;

    /**
     * Native: InnQuestIndexBucket::InnQuestIndexBucket @0057E5B0.
     */
    public InnQuestIndexBucket(
            int secondaryIndexKey,
            TargetHandle targetHandle,
            MissionScriptRuntime missionRuntime,
            int radius
    ) {
        super(secondaryIndexKey);
        this.targetHandle = targetHandle;
        this.missionRuntime = missionRuntime;
        this.radius = radius;
    }

    /**
     * vtbl +0x18: InnQuestIndexBucket::handleCellQuestEvent @0057E5F0.
     */
    @Override
    protected void handleCellQuestEvent(Quest quest) {
        int secondValue = quest.getEventMessageArgument();
        if (quest.runtimePayload == null) {
            quest.runtimePayload = findActiveUnitByTokenId(secondValue);
        }
        Unit unit = (Unit) quest.runtimePayload;
        if (unit != null && isWithinRadius(unit)) {
            if (quest.getId() == 4) {
                Building building = Globals.gameServer.objectLists.buildings.findClosestPresentBuilding(targetHandle);
                if (building != null) {
                    unit.transferToPlayerForMissionScript(building.owner);
                }
            }
            storage.addQuestRelatedEntry(Quest.MESSAGE_CELL_EVENT, secondValue, secondaryIndexKey << 16);
        }
    }

    /**
     * vtbl +0x1C: InnQuestIndexBucket::handleOwnerCellQuestEvent @0057E708.
     */
    @Override
    protected void handleOwnerCellQuestEvent(Quest quest) {
        int itemHash = quest.getEventMessageArgument() | 0x0E00;
        int matchedPlayerId = 0;
        boolean matched = false;
        for (Player player : missionRuntime.playerList.players) {
            if (player.isActive == 0 && player.controlledUnit != null) {
                Unit unit = (Unit) player.controlledUnit;
                if (isWithinRadius(unit)) {
                    Item item = unit.inventory.takeOneByHash(itemHash);
                    if (item != null) {
                        matched = true;
                        CServerApp.netUpdate(
                                unit,
                                player,
                                UnitDirtyFlags.INVENTORY_ITEMS.value | UnitDirtyFlags.ENCUMBRANCE_WEIGHT.value,
                                EQUIPMENT_BROADCAST_MASK,
                                0,
                                0
                        );
                        matchedPlayerId = player.playerId & 0xFFFF;
                    }
                }
            }
        }
        if (matched) {
            storage.addQuestRelatedEntry(
                    Quest.MESSAGE_OWNER_CELL_EVENT,
                    quest.getEventMessageArgument(),
                    (secondaryIndexKey << 16) | matchedPlayerId
            );
        }
    }

    /**
     * vtbl +0x20: InnQuestIndexBucket::handleGroupCellQuestEvent @0057E87A.
     */
    @Override
    protected void handleGroupCellQuestEvent(Quest quest) {
        int groupKey = quest.getEventMessageArgument();
        if (quest.runtimePayload == null) {
            quest.runtimePayload = findActivePlayerGroup(groupKey);
        }
        UnitGroup group = (UnitGroup) quest.runtimePayload;
        if (group != null && groupHasUnitWithinRadius(group)) {
            storage.addQuestRelatedEntry(
                    Quest.MESSAGE_GROUP_CELL_EVENT,
                    groupKey,
                    secondaryIndexKey << 16
            );
        }
    }

    /**
     * vtbl +0x24: InnQuestIndexBucket::handleRaiseDead8QuestEvent @0057E9CF.
     */
    @Override
    protected void handleRaiseDead8QuestEvent(Quest quest) {
        handleRaiseDeadQuestEvent(quest, Quest.MESSAGE_RAISE_DEAD_8_EVENT, RAISE_DEAD_8_UNIT_TYPE, true);
    }

    /**
     * vtbl +0x28: InnQuestIndexBucket::handleRaiseDead9QuestEvent @0057EBD4.
     */
    @Override
    protected void handleRaiseDead9QuestEvent(Quest quest) {
        handleRaiseDeadQuestEvent(quest, Quest.MESSAGE_RAISE_DEAD_9_EVENT, RAISE_DEAD_9_UNIT_TYPE, false);
    }

    /**
     * vtbl +0x2C: InnQuestIndexBucket::handleRaiseDead10QuestEvent @0057EDDA.
     */
    @Override
    protected void handleRaiseDead10QuestEvent(Quest quest) {
        handleRaiseDeadQuestEvent(quest, Quest.MESSAGE_RAISE_DEAD_10_EVENT, RAISE_DEAD_10_UNIT_TYPE, false);
    }

    /**
     * vtbl +0x30: InnQuestIndexBucket::getX @0057EFE0.
     */
    @Override
    public int getX() {
        return targetHandle.getX();
    }

    /**
     * vtbl +0x34: InnQuestIndexBucket::getY @0057EFFB.
     */
    @Override
    public int getY() {
        return targetHandle.getY();
    }

    /**
     * Native support extracted from InnQuestIndexBucket::handleCellQuestEvent @0057E5F0.
     */
    private Unit findActiveUnitByTokenId(int tokenId) {
        int normalizedTokenId = tokenId & 0xFFFF;
        for (Unit unit : missionRuntime.worldMap.activeUnits0xA456C) {
            if ((unit.idFull & 0xFFFF) == normalizedTokenId) {
                return unit;
            }
        }
        return null;
    }

    /**
     * Native support extracted from InnQuestIndexBucket::handleGroupCellQuestEvent @0057E87A.
     */
    private UnitGroup findActivePlayerGroup(int groupKey) {
        for (Player player : missionRuntime.playerList.players) {
            if (player.isActive != 0) {
                for (UnitGroup group : player.unitGroups) {
                    if (group.groupKey == groupKey) {
                        return group;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Native support extracted from InnQuestIndexBucket::handleGroupCellQuestEvent @0057E87A.
     */
    private boolean groupHasUnitWithinRadius(UnitGroup group) {
        for (Unit unit : group.units) {
            if (isWithinRadius(unit)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Native support extracted from InnQuestIndexBucket raise-dead handlers @0057E9CF, @0057EBD4, and @0057EDDA.
     */
    private void handleRaiseDeadQuestEvent(Quest quest, int message, int unitType, boolean publishBeforeKilling) {
        int eventArgument = quest.getEventMessageArgument();
        int playerId = eventArgument & 0xFFFF;
        int requiredCount = eventArgument >> 16;
        if (quest.runtimePayload == null) {
            quest.runtimePayload = missionRuntime.playerList.getPlayerById(playerId);
        }

        Player player = (Player) quest.runtimePayload;
        RaisedUnitCount count = countRaisedUnits(player, unitType);
        if (requiredCount <= count.units) {
            if (publishBeforeKilling) {
                publishRaiseDeadQuestEvent(message, playerId, count);
            }
            killRaisedUnits(player, unitType);
            if (!publishBeforeKilling) {
                publishRaiseDeadQuestEvent(message, playerId, count);
            }
        }
    }

    /**
     * Native support extracted from InnQuestIndexBucket raise-dead handlers @0057E9CF, @0057EBD4, and @0057EDDA.
     */
    private RaisedUnitCount countRaisedUnits(Player player, int unitType) {
        int units = 0;
        int maxHpSum = 0;
        for (UnitGroup group : player.unitGroups) {
            for (Unit unit : group.units) {
                if ((unit.getTokenTypeId() & 0xFF) == unitType && isWithinRadius(unit)) {
                    units++;
                    maxHpSum = (maxHpSum + unit.m_nMaxHP) & 0xFFFF;
                }
            }
        }
        return new RaisedUnitCount(units, maxHpSum);
    }

    /**
     * Native support extracted from InnQuestIndexBucket raise-dead handlers @0057E9CF, @0057EBD4, and @0057EDDA.
     */
    private void killRaisedUnits(Player player, int unitType) {
        for (UnitGroup group : player.unitGroups) {
            for (Unit unit : group.units) {
                if ((unit.getTokenTypeId() & 0xFF) == unitType && isWithinRadius(unit)) {
                    unit.m_nHP = RAISED_UNIT_KILL_HP;
                }
            }
        }
    }

    /**
     * Native support extracted from InnQuestIndexBucket raise-dead handlers @0057E9CF, @0057EBD4, and @0057EDDA.
     */
    private void publishRaiseDeadQuestEvent(int message, int playerId, RaisedUnitCount count) {
        storage.addQuestRelatedEntry(
                message,
                (count.maxHpSum << 16) | count.units,
                (secondaryIndexKey << 16) | (playerId & 0xFFFF)
        );
    }

    /**
     * Native support extracted from InnQuestIndexBucket distance checks @0057E5F0, @0057E708, @0057E87A,
     *
     * @0057E9CF, @0057EBD4, and @0057EDDA.
     */
    private boolean isWithinRadius(Unit unit) {
        return MissionScriptRuntime.cellChebyshevDistance(
                unit.m_pTargetHandle.getCell(),
                targetHandle.getCell()
        ) <= radius;
    }

    /**
     * Native support extracted from InnQuestIndexBucket raise-dead count locals @0057E9CF, @0057EBD4, and @0057EDDA.
     */
    private record RaisedUnitCount(int units, int maxHpSum) {
    }
}
