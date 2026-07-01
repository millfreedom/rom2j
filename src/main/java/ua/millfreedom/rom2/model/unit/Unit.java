package ua.millfreedom.rom2.model.unit;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CStaticDataMgr;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.gameserver.MissionScriptRuntime;
import ua.millfreedom.rom2.model.*;
import ua.millfreedom.rom2.model.column.SpellColumn;
import ua.millfreedom.rom2.model.column.UnitColumn;
import ua.millfreedom.rom2.model.column.WorldItemColumn;
import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.model.enums.EffectId;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.enums.MissionActionCode;
import ua.millfreedom.rom2.model.enums.ProtocolId;
import ua.millfreedom.rom2.model.enums.SpellId;
import ua.millfreedom.rom2.model.enums.UnitActionState;
import ua.millfreedom.rom2.model.net.CLlDriver;
import ua.millfreedom.rom2.model.spell.Spell;
import ua.millfreedom.rom2.model.spell.SpellEffect;
import ua.millfreedom.rom2.model.spell.Spellbook;
import ua.millfreedom.rom2.model.unit.humanoid.Humanoid;

import java.io.IOException;
import java.util.Arrays;

import static ua.millfreedom.rom2.model.enums.BuildingId.*;
import static ua.millfreedom.rom2.model.enums.MissionActionCode.ENGAGE_NEAREST;

/**
 * Native Unit model; individual methods below carry native port status.
 */
public class Unit extends Token {
    // AP attack profile bit set by Unit::FromTableLine @005114F3 and consumed by Unit::calculateIncomingDamage @00516F6F.
    private static final int UNIT_STATUS_AP_ATTACK_PROFILE = 0x10;
    public static final int UNIT_STATUS_CAN_CAST = 0x02;
    public static final int UNIT_STATUS_MAGE_CLASS = 0x04;
    private static final int UNIT_STATUS_INACTIVE = 0x08;
    private static final int MAGIC_ITEM_TYPE = 0x0E;

    // Unit action sub-state values used by move/cast/skill path in Unit::update.
    private static final int ACTION_SUBSTATE_READY = 0;
    private static final int ACTION_SUBSTATE_ACTIVE = 5;
    private static final int ACTION_SUBSTATE_RECOVERY = 7;

    // Token base occupies 0x04..0x3B (fields inherited from Token).
    //0x3C
    public TableLine unitInfoLine;
    // Last Token* damage source written by Unit::calculateIncomingDamage @00516F6F, serialized by
    // Unit::Serialize @0052C618, restored through the generic pointer map by Unit::restoreContext @0052CEF7,
    // and consumed by UnitList::updateActiveUnits @0052B459.
    //0x40
    public Token lastDamageSource;
    // Spell* pointer serialized by Unit::Serialize @0052C618, resolved by Spell::RestoreContext from
    // Unit::restoreContext @0052CEF7, populated by GameServer::handleServerGameAction @004F515D selected
    // spell and spell-effect item orders, and consumed/cleared by MissionScriptRuntime::initializeTargetSpellOrderGroup
    // @005702BE and MissionScriptRuntime::initializeCellSpellOrderGroup @0057033D.
    //0x44
    public Spell secondarySpell;
    // Skill context byte written with lastDamageSource by Unit::calculateIncomingDamage @00516F6F and
    // passed as signed char by UnitList::updateActiveUnits @0052B459 into kill-credit skill progress.
    //0x48
    public int killCreditSkillContext;
    // Java-only restore token for native +0x40 before Unit::restoreContext @0052CEF7 resolves the pointer map.
    private Object lastDamageSourceRestoreToken;
    // Java-only restore token for native +0x44 before Unit::restoreContext @0052CEF7 resolves the pointer map.
    private Object secondarySpellRestoreToken;
    // Java-only restore token for native +0x64 before Unit::restoreContext @0052CEF7 resolves the pointer map.
    private Object spellRestoreToken;
    //0x49
    public int tokenSize;
    //0x4A
    public int movementType;
    //0x4B
    public int face;
    //0x4C
    public int status;
    //0x50
    public MissionActionCode missionActionCode = MissionActionCode.DYING;
    //0x54
    public UnitActionState state = UnitActionState.DYING;
    //0x58
    public int subState;
    //0x5C
    public Token actionTarget;
    // Java-only restore token for native +0x5C before Unit::restoreContext @0052CEF7 resolves the pointer map.
    private Object actionTargetRestoreToken;
    //0x60
    public int skillTargetX;
    //0x61
    public int skillTargetY;
    //0x62
    public int f0x062;
    //0x63
    public int f0x063;
    //0x64
    public Spell spell;
    //0x68
    public Item pItem = new Item();
    // Byte timer/counter read through CArchive::FUN_005464E0 @005464E0 in Unit::Serialize @0052C618.
    //0x6C
    public int timerOrCounter;
    //0x70
    public UnitGroup unitGroup;
    //0x74
    public Weapon pWeapon;
    //0x78
    public Shield pShield;
    //0x7C
    public Inventory inventory;
    //0x80
    public String str = "";
    //0x84
    public int m_nBody;
    //0x86
    public int m_nReaction;
    //0x88
    public int m_nMind;
    //0x8A
    public int m_nSpirit;
    //0x8C
    public int speed;
    // Unit::refreshEncumbrance @0050F065 updates both fields from equipped item weight and inventory load.
    //0x8E
    public int m_nEquippedWeight;
    //0x90
    public int m_nEncumbranceWeight;
    //0x92
    public int m_nMaxWeight;
    //0x94
    public int m_nHP;
    //0x96
    public int m_nMaxHP;
    //0x98
    public int m_nHPRegenRate;
    //0x9A
    public int m_nMP;
    //0x9C
    public int m_nMaxMP;
    //0x9E
    public int m_nMPRegenRate;
    //0xA0
    public int m_wRegenStore;
    //0xA2
    public int m_nHPFraction;
    //0xA3
    public int m_nMPFraction;
    //0xA4
    public int sightFraction;
    //0xA5
    public int sightRange;
    //0xA6
    public final SkillData skillData = new SkillData();
    //0xBE
    public final StatData unitStatData = new StatData();
    // Includes m_StatMods at native +0xFE.
    //0xD4
    public final Modifiers mModifiers = new Modifiers();
    // Skill snapshot copied from current skills before applying temporary modifiers in Humanoid::updateSkills @005128CB.
    //0x114
    public final SkillData skillDataSnapshot = new SkillData();
    //0x12C
    public int defaultCastRange;
    //0x12D
    public int f0x12d;
    //0x12E
    public int f0x12e;
    //0x12F
    public int f0x12f;
    //0x130
    public int skillsTotalBonusPermille;
    // Byte action wind-up duration added into CServerApp::sendUnitCommandStartAction @005040C4.
    //0x134
    public int attackChargeTicks;
    // Byte action recovery duration added into CServerApp::sendUnitCommandStartAction @005040C4.
    //0x135
    public int attackRelaxTicks;
    //0x136
    public int actionReadyFlag;
    //0x137
    public int f0x137;
    //0x138
    public int lastOwnerSyncTick;
    //0x13C
    public int respawning;
    //0x140
    public Spellbook spellbook;
    //0x144
    public int effectKeyFlags;
    //0x148
    public int forceFinalCorpseStageOnDeath;
    //0x14C
    public int serverID;
    // Byte at full Unit +0x14E; UnitList::updateActiveUnits @0052B459 uses killCreditSkillContext at +0x48
    // for death skill credit.
    //0x14E
    public int f0x14e;
    //0x14F
    public int f0x14f;
    //0x150
    // Runtime dirty/update flags (see UnitDirtyFlags). Native writes are concentrated in
    // Effect::applyScaledModifier @0051D436 and armor equip/take-off paths.
    public int changedValues;
    //0x154
    // Dword dirty flags deferred for player ids 0x10..0x1F by CServerApp::NetUpdate.
    public final int[] deferredNetUpdateFlagsByPlayerId = new int[16];
    //0x194
    public int innUnitRelocationQuestFlag;
    //0x198
    public int innRecruitmentQuestFlag;
    //0x19C
    public int hostileUnitRelocationQuestFlag;
    // Scenario quest flag bit 3 copied by ScenarioDescriptor::materializeScenarioUnits @00561BF9;
    // suppresses death inventory/treasure in Unit::FinalizeDeath @00510A70.
    //0x1A0
    public int suppressDeathLootFlag;
    //0x1A4
    public int visiblePlayerMask;
    //0x1A6
    public int lastPublishedVisiblePlayerMask;
    // Packed victim kill-history entries: low 16 bits are defeated unit serverID, high 16 bits are repeat count.
    // Native stores this as an initially empty CDWordArray and caps it to five entries in
    // Humanoid::awardKillSkillProgress @00512FCB.
    //0x1A8
    public final CustomList<Integer> killHistoryEntries = CustomList.std(Integer.class);
    // Nonzero marker checked by CServerApp::sendLobbyPlayerInfoSnapshot @00504D39 before event 5.
    //0x1BC
    public int savedCharacterKillHistoryMarker;
    //0x1C0
    public final UnitMovementState movementState = new UnitMovementState();
    //0x1C4
    public final UnitMissionRuntimeState missionRuntimeState = new UnitMissionRuntimeState();
    //0x1C8
    public final CustomList<Short> mList1 = CustomList.std(Short.class);
    //0x1E4
    public final CustomList<Short> mList2 = CustomList.std(Short.class);
    //0x200
    public TargetHandle pTargetHandle0x200;
    //0x204
    public int ownershipTransferRefreshFlag;

    /**
     * Native: Unit::Unit @0050DC2B.
     * Fully ported.
     */
    public Unit() {
        initializeNativeDefaults("");
    }

    /**
     * Native: Unit::Unit(CString*) @0050DD41.
     * Fully ported.
     */
    public Unit(String templateName) {
        initializeNativeDefaults(templateName);
    }

    /**
     * Native: Unit::initFromTemplateAndTargetHandle @0050DE3A.
     * Fully ported.
     */
    public Unit(String templateName, TargetHandle targetHandle) {
        super(targetHandle);
        initializeNativeDefaults(templateName);
    }

    /**
     * Native: Unit::initFromTargetHandle @0050DF37.
     * Fully ported.
     */
    public Unit(TargetHandle targetHandle) {
        super(targetHandle);
        initializeNativeDefaults("");
    }

    /**
     * Native: Unit::constructFromTargetHandleAndOwner @0050E053.
     * Fully ported.
     */
    public Unit(TargetHandle targetHandle, Player owner) {
        super(targetHandle, owner);
        initializeNativeDefaults("");
    }

    /**
     * Native support extracted from Unit::Unit(CString*) @0050DD41 and
     * Unit::initializeFromTemplateName @00511959.
     */
    public static Unit createFromTemplateName(String templateName) {
        return new Unit(templateName);
    }

    /**
     * Native: Unit::initializeFromTemplateName @00511959.
     * Fully ported.
     */
    public void initializeFromTemplateName(String templateName) {
        typeID = 0;

        int unitInfoIndex = findTemplateUnitInfoIndex(templateName);
        UnitInfo unitInfo = null;
        if (unitInfoIndex != 0) {
            key = (unitInfoIndex) & 0xFFFF;
            unitInfo = Globals.staticDataMgr.units.get(key & 0xFFFF);
            applyUnitInfoValues(unitInfo);
            if (unitInfo.values.size() > UnitColumn.SERVER_ID.index) {
                serverID = unitInfo.getAttribute(UnitColumn.SERVER_ID) & 0xFFFF;
            }
            addTemplateEquipment(unitInfo);
        }

        if (getTokenTypeId() == 0) {
            Globals.gameServer.pushMessage("Invalid unit " + templateName + " created");
            return;
        }

        initializeTemplateSpellbook(unitInfo);
        mModifiers.addToUnit(this);
        refreshPrice();
        int templatePrice = unitInfo.getAttribute(UnitColumn.XP_VALUE);
        if (templatePrice > 0) {
            price = templatePrice;
        }
        movementState.applyTokenMovementLayerMask(this);
        initializeUnitBattlePreferenceDefaults(Globals.gameServer.missionScriptRuntime);
    }

    /**
     * Native: initializeUnitBattlePreferenceDefaults @0057382C.
     * Fully ported.
     * NOTE: native stores the MissionScriptRuntime ECX and forwards it to @0056AC5B, but neither callee reads the
     * runtime object. `Unit::initializeFromTemplateName @00511959` can pass a null runtime on town inn entry.
     *
     * @param missionScriptRuntime native MissionScriptRuntime receiver; may be null on town inn entry.
     */
    public void initializeUnitBattlePreferenceDefaults(MissionScriptRuntime missionScriptRuntime) {
        if (isHumanoidToken() == 0) {
            return;
        }
        if (spellbook == null) {
            int castRange = getCastRangeForFirstCastableSpellOrFallbackSupport() & 0xFF;
            if (castRange < 2 || (getMovementType() & 0xFF) == 3) {
                missionRuntimeState.battlePreferenceMode = 0;
                missionRuntimeState.withdraw = 0;
                missionRuntimeState.wimpy = 0;
            } else {
                missionRuntimeState.battlePreferenceMode = 1;
                missionRuntimeState.withdraw = (short) m_nMaxHP / 2;
                missionRuntimeState.wimpy = 0;
            }
        } else {
            int maxHp = (short) m_nMaxHP;
            missionRuntimeState.battlePreferenceMode = 2;
            missionRuntimeState.withdraw = maxHp;
            missionRuntimeState.wimpy = maxHp / 4;
        }
    }

    /**
     * Native support extracted from Unit::initializeNativeDefaults @0050E173 as used by
     * Spell::finalizeCastOnPoint @00519DB8 reusable-unit summon paths.
     * Fully ported support helper.
     */
    public void reinitializeFromTemplateName(String templateName) {
        initializeNativeDefaults(templateName);
    }

    /**
     * Native support extracted from Unit::initializeFromTemplateName @00511959 table scan.
     */
    private static int findTemplateUnitInfoIndex(String templateName) {
        int index = 0x1A;
        while (index < Globals.staticDataMgr.units.size()) {
            if (index == 0x1E) {
                index = 0x3F;
            }
            UnitInfo unitInfo = Globals.staticDataMgr.units.get(index);
            if (unitInfo.name.equals(templateName)) {
                return index;
            }
            index++;
        }
        return 0;
    }

    /**
     * Native support extracted from Unit::initializeFromTemplateName @00511959 equipment loop.
     */
    private void addTemplateEquipment(UnitInfo unitInfo) {
        for (int slotIndex = 0; slotIndex < 2; slotIndex++) {
            String equipmentName = unitInfo.equipment[slotIndex];
            if (!equipmentName.isEmpty()) {
                addIncomingObjectToInventory(createTemplateEquipment(equipmentName));
            }
        }
    }

    /**
     * Native support extracted from Unit::initializeFromTemplateName @00511959 equipment constructor branch.
     */
    private static Item createTemplateEquipment(String equipmentName) {
        if (equipmentName.contains("Shield")) {
            return Shield.createByServiceName(equipmentName);
        }
        return Weapon.createByServiceName(equipmentName);
    }

    /**
     * Native support extracted from Unit::initializeFromTemplateName @00511959 spellbook and skill setup.
     */
    private void initializeTemplateSpellbook(UnitInfo unitInfo) {
        if (unitInfo.getAttribute(UnitColumn.SPELL_1) > 0) {
            status |= UNIT_STATUS_CAN_CAST;
            spellbook = new Spellbook();
        }
        for (int spellSlot = 0; spellSlot < 3; spellSlot++) {
            int spellId = unitInfo.getAttribute(UnitColumn.from(UnitColumn.SPELL_1.index + spellSlot * 2));
            int probability = unitInfo.getAttribute(UnitColumn.from(UnitColumn.SPELL_PROBABILITY_1.index + spellSlot * 2));
            if (spellId > 0) {
                spellbook.setAt(spellId, new Spell((byte) spellId));
                missionRuntimeState.engagementSpells[spellSlot] = spellId;
                missionRuntimeState.engagementSpellProbabilities[spellSlot] = probability * 0x147;
            }
        }
        for (int skillIndex = 1; skillIndex < 6; skillIndex++) {
            skillData.skillLevels[skillIndex] = (short) unitInfo.getAttribute(UnitColumn.SPELL_POWER);
        }
        if (unitInfo.values.size() > UnitColumn.KNOWN_SPELLS.index) {
            initializeKnownTemplateSpells(unitInfo);
        }
    }

    /**
     * Native support extracted from Unit::initializeFromTemplateName @00511959 known-spell bitset branch.
     */
    private void initializeKnownTemplateSpells(UnitInfo unitInfo) {
        int knownSpellsBitset = unitInfo.getAttribute(UnitColumn.KNOWN_SPELLS);
        if (knownSpellsBitset != 0 && knownSpellsBitset != -1) {
            status |= UNIT_STATUS_MAGE_CLASS | UNIT_STATUS_CAN_CAST;
            spellbook = new Spellbook();
            for (int spellIndex = 1; spellIndex < CStaticDataMgr.SPELL_LIMIT; spellIndex++) {
                if ((knownSpellsBitset & (1 << spellIndex)) != 0) {
                    spellbook.setAt(spellIndex, new Spell((byte) spellIndex));
                }
            }
            skillData.skillLevels[1] = (short) unitInfo.getAttribute(UnitColumn.SKILL_FIRE);
            skillData.skillLevels[2] = (short) unitInfo.getAttribute(UnitColumn.SKILL_WATER);
            skillData.skillLevels[3] = (short) unitInfo.getAttribute(UnitColumn.SKILL_AIR);
            skillData.skillLevels[4] = (short) unitInfo.getAttribute(UnitColumn.SKILL_EARTH);
            skillData.skillLevels[5] = (short) unitInfo.getAttribute(UnitColumn.SKILL_ASTRAL);
        }
    }

    /**
     * Native: Unit::computeInnQuestThreatRating @00510A26.
     * Fully ported.
     */
    public int computeInnQuestThreatRating() {
        int threat = unitStatData.defence;
        if ((movementType & 0xFF) == 3) {
            threat += 0x19;
        }
        if ((m_nMaxMP & 0xFFFF) != 0) {
            threat += 0x32;
        }
        return threat;
    }

    /**
     * Native: Unit::initializeNativeDefaults @0050E173.
     * Fully ported.
     */
    private void initializeNativeDefaults(String templateName) {
        initializeNativeDefaultState();
        if (!templateName.isEmpty()) {
            initializeFromTemplateName(templateName);
        }
    }

    /**
     * Native support extracted from Unit::initializeNativeDefaults @0050E173 before its optional template-name branch.
     */
    private void initializeNativeDefaultState() {
        lastDamageSource = null;
        lastDamageSourceRestoreToken = null;
        killCreditSkillContext = 0;
        secondarySpell = null;
        secondarySpellRestoreToken = null;
        savedCharacterKillHistoryMarker = 0;
        unitInfoLine = null;
        typeID = 0;
        respawning = 0;
        tokenSize = 1;
        movementType = 1;
        status = 0;
        serverID = 0;
        face = 1;
        movementState.resetToDefaults();
        missionRuntimeState.resetToDefaults();
        pTargetHandle0x200 = null;
        movementState.facing = Utils.randInclusive(0x80) + 0x40;
        movementState.facingLast = movementState.facing;
        unitGroup = null;
        missionActionCode = MissionActionCode.DYING;
        state = UnitActionState.DYING;
        subState = 0;
        actionReadyFlag = 1;
        actionTarget = null;
        actionTargetRestoreToken = null;
        skillTargetX = 0;
        skillTargetY = 0;
        spell = null;
        spellRestoreToken = null;
        pItem = null;
        spellbook = null;
        inventory = new Inventory();
        pWeapon = null;
        pShield = null;
        skillsTotalBonusPermille = 0;
        m_nHPFraction = 0;
        m_nMPFraction = 0;
        str = "";
        lastOwnerSyncTick = 0;
        forceFinalCorpseStageOnDeath = 0;
        innUnitRelocationQuestFlag = 0;
        innRecruitmentQuestFlag = 0;
        hostileUnitRelocationQuestFlag = 0;
        suppressDeathLootFlag = 0;
        visiblePlayerMask = 0;
        lastPublishedVisiblePlayerMask = 0;
        ownershipTransferRefreshFlag = 0;
        Arrays.fill(deferredNetUpdateFlagsByPlayerId, 0);
        initializeDefaultStats();
    }

    /**
     * Native: Unit::initializeDefaultStats @0050EC60.
     * Fully ported.
     */
    private void initializeDefaultStats() {
        missionActionCode = MissionActionCode.SCRIPT_CELL_STATUS;
        m_nBody = 0x1E;
        m_nReaction = 0x1E;
        m_nMind = 0x14;
        m_nSpirit = 0x14;
        speed = 10;
        movementState.rotationSpeed = 8;
        sightFraction = 0;
        sightRange = 5;
        m_nEquippedWeight = 0;
        m_nEncumbranceWeight = 0;
        m_nMaxWeight = m_nBody * 10;
        m_nMaxHP = 0x1E;
        m_nHP = 0x1E;
        m_nHPRegenRate = 100;
        m_nMaxMP = 0;
        m_nMP = 0;
        m_wRegenStore = m_nMaxMP;
        m_nMPRegenRate = 0x32;
        attackChargeTicks = 8;
        attackRelaxTicks = 4;
        defaultCastRange = 1;
    }

    /**
     * Native: Unit::operator>>(CArchive*, Unit**) @0050DC0F.
     * Fully ported.
     */
    public static Unit readFromArchive(CArchive ar) throws IOException {
        return ar.readObject(Unit.class);
    }

    /**
     * Native: Unit::RestoreContext @00546700.
     * Fully ported.
     */
    public static Unit restoreContextToken(Object tokenOrRef) {
        Object resolved = Globals.gameServer.lookupPointerMapOrNull(tokenOrRef);
        return (resolved instanceof Unit unit) ? unit : null;
    }

    /**
     * vtbl +0x08: Unit::Serialize @0052C618.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        // Leading always-serialized blocks.
        super.serialize(ar);                        // Token::serialize
        serializeEffectsList(ar);                   // CList<Effect>::Serialize
        ar.serialize(mList1);           // m_list1 Serialize
        ar.serialize(mList2);           // m_list2 Serialize
        ar.serialize(skillData);                    // SkillData::Serialize
        ar.serialize(unitStatData);                 // StatData::Serialize
        ar.serialize(skillDataSnapshot);            // SkillData::Serialize
        ar.serialize(mModifiers);                   // Modifiers::Serialize
        ar.serialize(movementState);
        ar.serialize(missionRuntimeState);

        if (!ar.isStoring()) {
            serializeReadBody(ar);
        } else {
            serializeWriteBody(ar);
        }
    }

    /**
     * Native support extracted from Unit::Serialize @0052C618.
     * Fully ported.
     */
    private void serializeReadBody(CArchive ar) throws IOException {
        tokenSize = ar.readByte() & 0xFF;
        movementType = ar.readByte() & 0xFF;
        face = ar.readByte() & 0xFF;
        status = ar.readByte() & 0xFF;

        missionActionCode = MissionActionCode.fromValue(ar.readInt());
        state = UnitActionState.fromValue(ar.readInt());
        subState = ar.readInt();

        skillTargetX = ar.readByte() & 0xFF;
        skillTargetY = ar.readByte() & 0xFF;
        timerOrCounter = readSerializedByte(ar);

        pWeapon = (Weapon) Item.readFromArchive(ar);
        pShield = (Shield) Item.readFromArchive(ar);

        str = ar.readCString();

        m_nBody = ar.readShort();
        m_nReaction = ar.readShort();
        m_nMind = ar.readShort();
        m_nSpirit = ar.readShort();
        speed = ar.readShort();
        m_nEquippedWeight = ar.readShort();
        m_nEncumbranceWeight = ar.readShort();
        m_nMaxWeight = ar.readShort();
        m_nHP = ar.readShort();
        m_nMaxHP = ar.readShort();
        m_nHPRegenRate = ar.readShort();
        m_nMP = ar.readShort();
        m_nMaxMP = ar.readShort();
        m_nMPRegenRate = ar.readShort();
        m_nHPFraction = ar.readByte() & 0xFF;
        m_nMPFraction = ar.readByte() & 0xFF;
        m_wRegenStore = ar.readShort();

        int packedSight = ar.readUShort();
        sightFraction = packedSight & 0xFF;
        sightRange = (packedSight >>> 8) & 0xFF;

        defaultCastRange = ar.readByte() & 0xFF;
        skillsTotalBonusPermille = ar.readInt();
        attackChargeTicks = ar.readByte() & 0xFF;
        attackRelaxTicks = ar.readByte() & 0xFF;
        actionReadyFlag = ar.readByte() & 0xFF;
        lastOwnerSyncTick = ar.readInt();
        respawning = ar.readByte() & 0xFF;

        int local14 = ar.readInt();
        effectKeyFlags = ar.readInt();
        pItem = Item.readFromArchive(ar);

        int hasInventory = ar.readByte() & 0xFF;
        if (hasInventory != 0) {
            ar.serialize(inventory);
        }

        int hasSpellbook = ar.readByte() & 0xFF;
        if (hasSpellbook != 0) {
            spellbook = new Spellbook();
            ar.serialize(spellbook);
        }

        actionTarget = null;
        actionTargetRestoreToken = ar.readInt();
        spell = null;
        spellRestoreToken = ar.readInt();
        secondarySpell = null;
        secondarySpellRestoreToken = ar.readInt();
        lastDamageSource = null;
        lastDamageSourceRestoreToken = ar.readInt();
        killCreditSkillContext = readSerializedByte(ar);

        serverID = local14 & 0xFFFF;
        ownershipTransferRefreshFlag = (local14 & 0x00FF0000) != 0 ? 1 : 0;
        suppressDeathLootFlag = (local14 & 0xFF000000) != 0 ? 1 : 0;

        int tokenVtableCheck = isHumanoidToken();
        if (tokenVtableCheck == 0) {
            resolveUnitInfoFromStaticTables();
        } else {
            unitInfoLine = null;
        }
    }

    /**
     * Native support extracted from Unit::Serialize @0052C618.
     * Fully ported.
     */
    private void serializeWriteBody(CArchive ar) throws IOException {
        ar.writeByte(tokenSize);
        ar.writeByte(movementType);
        ar.writeByte(face);
        ar.writeByte(status);

        ar.writeInt(missionActionCode.value);
        ar.writeInt(state.value);
        ar.writeInt(subState);

        ar.writeByte(skillTargetX);
        ar.writeByte(skillTargetY);
        ar.writeByte(timerOrCounter);

        ar.writeObject(pWeapon);
        ar.writeObject(pShield);

        ar.writeCString(str);

        ar.writeShort(m_nBody);
        ar.writeShort(m_nReaction);
        ar.writeShort(m_nMind);
        ar.writeShort(m_nSpirit);
        ar.writeShort(speed);
        ar.writeShort(m_nEquippedWeight);
        ar.writeShort(m_nEncumbranceWeight);
        ar.writeShort(m_nMaxWeight);
        ar.writeShort(m_nHP);
        ar.writeShort(m_nMaxHP);
        ar.writeShort(m_nHPRegenRate);
        ar.writeShort(m_nMP);
        ar.writeShort(m_nMaxMP);
        ar.writeShort(m_nMPRegenRate);
        ar.writeByte(m_nHPFraction);
        ar.writeByte(m_nMPFraction);
        ar.writeShort(m_wRegenStore);

        int packedSight = (sightFraction & 0xFF) | ((sightRange & 0xFF) << 8);
        ar.writeShort(packedSight);

        ar.writeByte(defaultCastRange);
        ar.writeInt(skillsTotalBonusPermille);
        ar.writeByte(attackChargeTicks);
        ar.writeByte(attackRelaxTicks);
        ar.writeByte(actionReadyFlag);
        ar.writeInt(lastOwnerSyncTick);
        ar.writeByte(respawning);

        int local14 = (serverID & 0xFFFF)
                | ((ownershipTransferRefreshFlag & 0xFF) << 16)
                | ((suppressDeathLootFlag & 0xFF) << 24);
        ar.writeInt(local14);
        ar.writeInt(effectKeyFlags);
        ar.writeObject(pItem);

        if (inventory == null) {
            ar.writeByte(0);
        } else {
            ar.writeByte(1);
            ar.serialize(inventory);
        }

        if (spellbook == null) {
            ar.writeByte(0);
        } else {
            ar.writeByte(1);
            ar.serialize(spellbook);
        }

        ar.writeInt(Utils.encodePointerLike(actionTarget != null ? actionTarget : actionTargetRestoreToken));
        ar.writeInt(Utils.encodePointerLike(spell != null ? spell : spellRestoreToken));
        ar.writeInt(Utils.encodePointerLike(secondarySpell != null ? secondarySpell : secondarySpellRestoreToken));
        ar.writeInt(Utils.encodePointerLike(lastDamageSource != null ? lastDamageSource : lastDamageSourceRestoreToken));
        ar.writeByte(killCreditSkillContext);
    }

    /**
     * Native support extracted from Unit::Serialize @0052C618.
     * Fully ported.
     */
    private void resolveUnitInfoFromStaticTables() {
        unitInfoLine = Globals.staticDataMgr.units.get(key & 0xFFFF);
    }

    /**
     * Native: Unit::FromTableLine @005114F3.
     * Fully ported.
     */
    public void applyUnitInfoValues(UnitInfo info) {
        unitInfoLine = info;
        SequentialArrayWalker values = new SequentialArrayWalker(info.values);
        m_nBody = values.nextShort(m_nBody);
        m_nReaction = values.nextShort(m_nReaction);
        m_nMind = values.nextShort(m_nMind);
        m_nSpirit = values.nextShort(m_nSpirit);
        m_nMaxHP = values.nextShort(m_nMaxHP);
        m_nHP = m_nMaxHP;
        m_nHPRegenRate = values.nextShort(m_nHPRegenRate);
        m_nMaxMP = values.nextShort(m_nMaxMP);
        m_nMP = m_nMaxMP;
        m_wRegenStore = m_nMP;
        m_nMPRegenRate = values.nextShort(m_nMPRegenRate);
        speed = values.nextShort(speed);
        movementState.rotationSpeed = values.nextByte(movementState.rotationSpeed);
        sightFraction = 0;
        sightRange = values.nextByte(sightRange);
        applyUnitInfoDamage(values);
        skillData.toHit = (short) values.nextShort(skillData.toHit);
        skillData.skillLevels[0] = skillData.toHit;
        unitStatData.defence = (short) values.nextShort(unitStatData.defence);
        unitStatData.absorbtion = (short) values.nextShort(unitStatData.absorbtion);
        attackChargeTicks = values.nextByte(attackChargeTicks);
        attackRelaxTicks = values.nextByte(attackRelaxTicks);
        for (int i = 1; i < 6; i++) {
            unitStatData.protections[i] = (short) values.nextShort(unitStatData.protections[i]);
        }
        for (int i = 1; i < 6; i++) {
            unitStatData.m_bModifiers[i] = (byte) values.nextByte(Byte.toUnsignedInt(unitStatData.m_bModifiers[i]));
        }
        typeID = values.nextUnsignedShort(typeID & 0xFFFF) & 0xFFFF;
        face = values.nextByte(face);
        tokenSize = values.nextByte(tokenSize);
        movementType = values.nextByte(movementType);
        // Native reads DyingTime into a local and discards it.
        values.nextInt(0);
        missionRuntimeState.withdraw = values.nextInt(missionRuntimeState.withdraw);
        missionRuntimeState.wimpy = values.nextInt(missionRuntimeState.wimpy);
        missionRuntimeState.seeInvisible = values.nextByte(missionRuntimeState.seeInvisible);
        price = values.nextInt(price);
    }

    /**
     * Native support extracted from Unit::FromTableLine @005114F3 damage-kind packing.
     */
    private void applyUnitInfoDamage(SequentialArrayWalker values) {
        int physicalMin = values.nextInt(-1);
        int physicalSpread = values.nextInt(-1) - physicalMin;
        int attackKind = values.nextInt(0);
        if (attackKind < 1) {
            if (physicalMin < 0x80 && physicalSpread <= physicalMin + 0xFF) {
                skillData.skillDamageType0And3Min = (byte) physicalMin;
                skillData.skillDamageType0And3Modifier = (byte) physicalSpread;
                return;
            }
            int correctedMin = Math.min(physicalMin / 0x0F, 0x7F);
            int correctedSpread = Math.min(physicalSpread / 0x0F, 0xFF);
            skillData.skillDamageType0And3Min = (byte) (correctedMin | 0x80);
            skillData.skillDamageType0And3Modifier = (byte) correctedSpread;
            return;
        }
        if (attackKind == 1) {
            skillData.skillDamageType1Min = (byte) physicalMin;
            skillData.skillDamageType1Modifier = (byte) physicalSpread;
            return;
        }
        if (attackKind == 2) {
            skillData.skillDamageType2Min = (byte) -physicalMin;
            skillData.skillDamageType2Modifier = (byte) -physicalSpread;
            return;
        }
        if (attackKind == 3) {
            status |= UNIT_STATUS_AP_ATTACK_PROFILE;
            skillData.skillDamageType0And3Min = (byte) physicalMin;
            skillData.skillDamageType0And3Modifier = (byte) physicalSpread;
        }
    }

    // ---- Unresolved native helpers ----

    /**
     * Native support extracted from Unit::Serialize @0052C618; uses CArchive::FUN_005464E0 @005464E0.
     * Fully ported.
     */
    private int readSerializedByte(CArchive ar) throws IOException {
        return ar.readByte() & 0xFF;
    }

    /**
     * Native: Global::isUnitMoveState @0057355E.
     * Fully ported.
     */
    public boolean isMoveState() {
        return state == UnitActionState.MOVE;
    }

    /**
     * Native: Global::hideUnitMissionStateIfDead @005736E4.
     * Fully ported.
     */
    public void hideMissionStateIfDead() {
        if ((short) m_nHP < 1) {
            missionActionCode = MissionActionCode.MISSION_HIDDEN;
            state = UnitActionState.MISSION_HIDDEN;
        }
    }

    // ---- Unit vtable overrides / hooks ----

    /**
     * vtbl +0x18: Unit::update (@0050F12C).
     */
    @Override
    public Object update() {
        if (state == UnitActionState.DEAD) {
            return this;
        }

        tickEffects();

        if ((short) m_nHP < 1) {
            return updateDyingState();
        }

        maybeEmitOwnerHeartbeat();

        int targetXdX = m_pTargetHandle.packXdX();
        int targetYdY = m_pTargetHandle.packYdY();
        int facingLast = movementState.facingLast;

        if (hasUnitInfoLine()) {
            notifyMissionScriptRuntimeUnitTick();
        }

        return switch (state) {
            case IDLE -> {
                updateIdleMotion(targetXdX, targetYdY, facingLast);
                subState = ACTION_SUBSTATE_READY;
                yield this;
            }
            case ATTACK -> updateAttackState();
            case MOVE, CAST_SPELL, USE_SKILL -> updateActionState();
            case INTERACT -> updateInteractState();
            default -> this;
        };
    }

    /**
     * Native support extracted from Unit::update @0050F12C.
     */
    private void tickEffects() {
        for (int i = 0; i < effects.size(); ) {
            Effect effect = effects.get(i);
            effect.updateOnTick(this);
            if (effect.isExpired()) {
                effects.remove(i);
                continue;
            }
            i++;
        }
    }

    /**
     * Native support extracted from Unit::update @0050F12C.
     */
    private Object updateDyingState() {
        missionActionCode = MissionActionCode.DYING;
        state = UnitActionState.DYING;

        if (respawning == 0) {
            respawning = 1;
            beginDyingTransition();
            unitStatData.defence = (short) (unitStatData.defence / 2);
            setTimerByte(getDyingStateTimerTicks() - 1);
            emitNetUpdate(UnitDirtyFlags.DYING_TRANSITION.value);
            return this;
        }

        if (getTimerSignedByte() > 0) {
            setTimerByte(getTimerSignedByte() - 1);
            return this;
        }

        if ((movementType & 0xFF) > 1) {
            m_nHP = -1000;
        }
        if ((short) m_nHP < -9) {
            state = UnitActionState.DEAD;
            finalizeDeath();
        }
        return this;
    }

    /**
     * Native support extracted from Unit::update @0050F12C.
     */
    private void maybeEmitOwnerHeartbeat() {
        if (Globals.gameServer == null || Globals.gameServer.networkSessionActive == 0) {
            return;
        }
        if (owner == null) {
            return;
        }
        Player ownerPlayer = owner;
        if (ownerPlayer.controlledUnit != this) {
            return;
        }
        if (getOwnerSyncTickDelta() == 0x50) {
            emitNetUpdate(UnitDirtyFlags.POSITION_AND_FACING.value);
        }
    }

    /**
     * Native: Unit::GetOwnerSyncTickDelta @0050FC3E.
     * Fully ported.
     */
    public int getOwnerSyncTickDelta() {
        return Globals.gameServer.serverLoopCounter - lastOwnerSyncTick;
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019 sight-range payload.
     */
    public int packedSightRangeForNetUpdate() {
        return (sightFraction & 0xFF) | ((sightRange & 0xFF) << 8);
    }

    /**
     * Native support extracted from Unit::update @0050F12C.
     */
    private Object updateAttackState() {
        Sack sack = Globals.gameServer.objectLists.sacks.findAtTargetHandle(m_pTargetHandle);
        if (sack == null) {
            return this;
        }

        detachSackFromWorld(sack);
        Globals.gameServer.objectLists.sacks.remove(sack);
        pickupSackContents(sack);
        return this;
    }

    /**
     * Native support extracted from Unit::update @0050F12C.
     */
    private Object updateActionState() {
        if (subState == ACTION_SUBSTATE_READY) {
            return updateActionPrepare();
        }
        if (subState == ACTION_SUBSTATE_ACTIVE) {
            return updateActionActive();
        }
        if (subState == ACTION_SUBSTATE_RECOVERY) {
            setTimerByte(getTimerSignedByte() - 1);
            if (getTimerSignedByte() == 0) {
                subState = ACTION_SUBSTATE_READY;
                actionReadyFlag = 1;
            }
        }
        return this;
    }

    /**
     * Native support extracted from Unit::update @0050F12C.
     */
    private Object updateActionPrepare() {
        int extraWindupTicks = 0;
        if (state == UnitActionState.MOVE) {
            if (hasSpellWeapon() && isMageClass()) {
                spell = pWeapon.spell;
                pItem = pWeapon;
                state = UnitActionState.CAST_SPELL;
            } else {
                extraWindupTicks = beginMoveAction();
            }
        }

        if (state == UnitActionState.CAST_SPELL || state == UnitActionState.USE_SKILL) {
            if (!startSpellCast()) {
                return this;
            }
            actionReadyFlag = 0;
            subState = ACTION_SUBSTATE_ACTIVE;
            setTimerByte(attackChargeTicks);
            return this;
        }

        actionReadyFlag = 0;
        subState = ACTION_SUBSTATE_ACTIVE;
        setTimerByte(attackChargeTicks + extraWindupTicks);
        return this;
    }

    /**
     * Native support extracted from Unit::update @0050F12C.
     */
    private Object updateActionActive() {
        setTimerByte(getTimerSignedByte() - 1);
        if (getTimerSignedByte() != 0) {
            return this;
        }

        if (state == UnitActionState.MOVE) {
            if (hasSpellWeapon() && isMageClass()) {
                spell = pWeapon.spell;
                pItem = pWeapon;
                state = UnitActionState.CAST_SPELL;
            } else {
                finishMoveAction();
            }
        }

        if (state == UnitActionState.CAST_SPELL || state == UnitActionState.USE_SKILL) {
            finishSpellCast();
            if (pItem != null) {
                if (pItem.getSlot() == MAGIC_ITEM_TYPE) {
                    disposeTemporaryCastObjects();
                }
                pItem = null;
                spell = null;
            }
        }

        subState = ACTION_SUBSTATE_RECOVERY;
        int recoveryPenaltyTicks = computeWeaponRecoveryPenaltyTicks();
        setTimerByte(attackRelaxTicks + recoveryPenaltyTicks + Utils.randInclusive(2));

        if ((state == UnitActionState.CAST_SPELL || state == UnitActionState.USE_SKILL) && spell != null) {
            setTimerByte(getTimerSignedByte() + getSpellRecoveryBonus(spell));
        }
        return this;
    }

    /**
     * Native support extracted from Unit::update @0050F12C.
     */
    private Object updateInteractState() {
        Building interactive = Globals.gameServer.objectLists.buildings.findInteractiveNearTarget(m_pTargetHandle);
        if (interactive != null) {
            if (interactive instanceof Shop shop) {
                return CServerApp.openShopDialog(shop, owner);
            }
            if (interactive instanceof Inn inn) {
                return CServerApp.openInnDialog(inn, owner);
            }
            if (interactive instanceof Pointer pointer) {
                if (pointer.scriptInstantIndex < 1) {
                    return pointer;
                }
                return runPointerAction(pointer.scriptInstantIndex);
            }

            if (interactive.isA(MULTIPLAYER_CHURCH) && CLlDriver.getProtocolId() == ProtocolId.TCP_IP) {
                Player ownerPlayer = owner;
                CServerApp.removeLocalClientByNetId(ownerPlayer.playerId);
                CServerApp.processLocalNetworkEvents();
                ownerPlayer.pendingRemovalServerTick = Globals.gameServer.someValue;
            }

            if (isToggleBuilding(interactive)) {
                interactive.healthCurrent = interactive.healthCurrent == 0 ? 1 : 0;
                CServerApp.notifyBuildingStateChanged(interactive);
            }
        }

        Building rewardBuilding = Globals.gameServer.objectLists.buildings.findNearTarget(m_pTargetHandle);
        if (rewardBuilding == null) {
            return null;
        }

        if (rewardBuilding.isA(MAGIC_WELL_2)) {
            if (rewardBuilding.hasMagicWellRewardCharge(MAGIC_WELL_2)) {
                rewardBuilding.consumeMagicWellRewardCharge();
                addIncomingObjectToInventory(MagicItem.createByName("Potion Big Healing"));
            }
        } else if (rewardBuilding.isA(MAGIC_WELL_3)) {
            if (rewardBuilding.hasMagicWellRewardCharge(MAGIC_WELL_3)) {
                rewardBuilding.consumeMagicWellRewardCharge();
                addIncomingObjectToInventory(MagicItem.createByName("Potion Big Mana"));
            }
        }
        return rewardBuilding;
    }

    /**
     * Native support extracted from Unit::NoTableLine @00542560.
     */
    private boolean hasUnitInfoLine() {
        return unitInfoLine != null;
    }

    /**
     * Native: Unit::NoTableLine @00542560.
     * Fully ported.
     */
    public boolean hasNoTableLine() {
        return unitInfoLine == null;
    }

    /**
     * Native: Unit::HasEffectKeyFlag @005106D2.
     * Fully ported.
     */
    public boolean hasEffectKeyFlag(int effectKey) {
        return ((1 << (effectKey & 0x1F)) & effectKeyFlags) != 0;
    }

    /**
     * Native: Unit::updateIdleMotion @0050FF2D.
     * Fully ported.
     */
    private void updateIdleMotion(int xdx, int ydy, int facingLast) {
        int currentXdX = m_pTargetHandle.packXdX();
        int currentYdY = m_pTargetHandle.packYdY();

        if (currentXdX == xdx && currentYdY == ydy) {
            int stepByte = movementState.positionChanged(facingLast);
            if (stepByte >= 0) {
                int dir = ((movementState.facingLast + 8) >> 4) & 0xFF;
                emitFacingPacket(dir, stepByte, 0x6D);
            }
            return;
        }

        if ((xdx & 0xFF) == 0x80 && (ydy & 0xFF) == 0x80) {
            int moveDir = ((movementState.moveDirOrMode << 1) & 0xFF);
            int stepLimit = movementState.stepTickLimit;
            emitFacingPacket(moveDir, stepLimit, 0);
        }
    }

    /**
     * Native support extracted from Unit::update @0050F12C.
     */
    private boolean hasSpellWeapon() {
        return pWeapon != null && pWeapon.hasSpell();
    }

    /**
     * Native: Unit::IsMageClass @0053A5A0.
     * Fully ported.
     */
    public boolean isMageClass() {
        return (status & UNIT_STATUS_MAGE_CLASS) != 0;
    }

    /**
     * Native: Unit::IsFemale @0053A5C0.
     * Fully ported.
     */
    public boolean isFemale() {
        int typeId = typeID & 0xFFFF;
        return typeId == 0x22 || typeId == 0x24;
    }

    /**
     * Native: Unit::IsDying @00542770.
     * Fully ported.
     */
    public boolean isDying() {
        return m_nHP < 0;
    }

    /**
     * Native: Unit::IsNonMageClass @00542C90.
     * Fully ported.
     */
    public boolean isNonMageClass() {
        return (status & UNIT_STATUS_MAGE_CLASS) == 0;
    }

    /**
     * Native: Unit::hasCanCastStatusFlag @00543130.
     * Fully ported.
     */
    public int hasCanCastStatusFlag() {
        return status & UNIT_STATUS_CAN_CAST;
    }

    /**
     * Native: Unit::getSpellPowerContext @00511913.
     * Fully ported.
     */
    public int getSpellPowerContext(int sphere) {
        int context = skillData.skillLevels[sphere & 0xFF] - 0x1E + m_nMind;
        return context < 0 ? 0 : context;
    }

    /**
     * Native support extracted from global beginMoveAction @00516B2F.
     * Fully ported.
     */
    private int beginMoveAction() {
        if (!(actionTarget instanceof Token targetToken)
                || targetToken.owner == null
                || owner == null
                || (short) m_nHP <= 0) {
            return 0;
        }

        int rangeTicks = computeRangeTicks(targetToken);
        if (hasEffectKeyFlag(0x0C)) {
            for (Effect effect : effects) {
                if ((effect.key & 0xFFFF) == 0x0C) {
                    effect.mValue.setS2(1);
                }
            }
        }

        int extraWindupTicks = 0;
        if (rangeTicks > 1) {
            extraWindupTicks = (rangeTicks * 0x100 + 0x80) / 200;
        }
        CServerApp.emitActionStart(this);
        return extraWindupTicks;
    }

    /**
     * Native support extracted from Global::computeRangeTicks @005169DE.
     * Fully ported.
     */
    private int computeRangeTicks(Token target) {
        int dx = Math.abs((getCenterXdX() & 0xFFFF) - (target.getCenterXdX() & 0xFFFF));
        int dy = Math.abs((getCenterYdY() & 0xFFFF) - (target.getCenterYdY() & 0xFFFF));
        int dist = Math.max(dx, dy);
        int sizeFactor = (((getTokenSizeVirtual() & 0xFF) + (target.getTokenSizeVirtual() & 0xFF)) * 0x80) - 0x100;
        int adjusted = dist - sizeFactor;
        if (adjusted < 0x181) {
            return 1;
        }
        return (adjusted + 0x40) >> 8;
    }

    /**
     * Native support extracted from global finishMoveAction @00516C1F.
     * Fully ported.
     */
    private void finishMoveAction() {
        if (actionTarget instanceof Unit target) {
            applyDirectUnitAttack(target);
        } else if (actionTarget instanceof Building target) {
            applyNonUnitAttack(target);
        } else if (actionTarget instanceof Token target) {
            Globals.gameServer.pushMessage("I don't know how to attack " + target.getClass().getSimpleName());
        }
    }

    /**
     * Native support extracted from global applyDirectUnitAttack @00516CEB, called by finishMoveAction @00516C1F.
     * Fully ported.
     */
    private void applyDirectUnitAttack(Unit target) {
        if (target == null || target.owner == null || owner == null || (short) m_nHP <= 0) {
            return;
        }
        if (computeRangeTicks(target) > (defaultCastRange & 0xFF)) {
            return;
        }

        boolean targetWasAlive = (short) target.m_nHP > 0;
        int damage = target.calculateIncomingDamage(skillData, this);
        target.m_nHP = (short) (target.m_nHP - damage);

        if (hasSpellWeapon()
                && isNonMageClass()
                && ((damage > 0 && (short) target.m_nHP > 0)
                || pWeapon.spell.id == SpellId.FIRE_BALL.id)) {
            spell = pWeapon.spell;
            pItem = pWeapon;
            pWeapon.spell.finalizeCastOnTarget(this, target);
            spell = null;
            pItem = null;
        }

        if (shouldNotifyTargetHitPointChange(targetWasAlive, target)) {
            CServerApp.notifyUnitHitPointsChanged(target);
        }
        if (damage > 0 && targetWasAlive && (short) target.m_nHP > -10) {
            awardDamageSkillProgress(target, damage, 0);
        }
    }

    /**
     * Native support extracted from global applyNonUnitAttack @00516EA0, called by finishMoveAction @00516C1F.
     * Fully ported.
     */
    private void applyNonUnitAttack(Building target) {
        if (target == null || (short) m_nHP <= 0 || owner == null) {
            return;
        }
        if (computeRangeTicks(target) > (defaultCastRange & 0xFF)) {
            return;
        }

        boolean targetWasAlive = (short) target.healthCurrent > 0;
        int damage = target.calculateIncomingDamage(skillData, this);
        target.healthCurrent = (short) (target.healthCurrent - damage);
        if (targetWasAlive || (short) target.healthCurrent > -10) {
            CServerApp.notifyUnitHitPointsChanged(target);
        }
    }

    /**
     * Native support extracted from applyDirectUnitAttack @00516CEB and applyNonUnitAttack @00516EA0.
     */
    private static boolean shouldNotifyTargetHitPointChange(boolean targetWasAlive, Unit target) {
        return targetWasAlive || (short) target.m_nHP > -10;
    }

    /**
     * Native support extracted from Unit::update @0050F12C.
     */
    private boolean startSpellCast() {
        Spell activeSpell = spell;
        if (state == UnitActionState.CAST_SPELL) {
            return activeSpell.cast(this, actionTarget, 0, 0);
        }
        return activeSpell.cast(this, null, skillTargetX, skillTargetY);
    }

    /**
     * Native support extracted from Unit::update @0050F12C.
     */
    private void finishSpellCast() {
        Spell activeSpell = spell;
        if (state == UnitActionState.CAST_SPELL) {
            activeSpell.finalizeCastOnTarget(this, actionTarget);
            return;
        }
        activeSpell.finalizeCastOnPoint(this, skillTargetX, skillTargetY);
    }

    /**
     * Native support extracted from Unit::update @0050F12C.
     */
    private int computeWeaponRecoveryPenaltyTicks() {
        if (pWeapon == null || isHumanoidToken() == 0) {
            return 0;
        }
        int penalty = (pWeapon.weight + (0x1E - m_nReaction) * 5) / 0x0C;
        if (penalty < 0) {
            penalty = 0;
        }
        return Math.min(penalty, 0x0C);
    }

    /**
     * Native support extracted from Unit::update @0050F12C.
     */
    private int getSpellRecoveryBonus(Spell activeSpell) {
        return activeSpell.spellInfo.getAttribute(SpellColumn.COMPLICATION_LEVEL);
    }

    /**
     * Native support extracted from Unit::update @0050F12C.
     */
    private int getTimerSignedByte() {
        return (byte) timerOrCounter;
    }

    /**
     * Native support extracted from Unit::update @0050F12C.
     */
    private void setTimerByte(int value) {
        timerOrCounter = value & 0xFF;
    }

    /**
     * Native support extracted from Unit::update @0050F12C.
     */
    private void disposeTemporaryCastObjects() {
        // Native destroys temporary cast item/spell allocations here.
    }

    /**
     * Native support extracted from Unit::update @0050F12C.
     */
    private static boolean isToggleBuilding(Building building) {
        int typeId = building.getTokenTypeId() & 0xFFFF;
        return typeId == SWITCH.id
                || typeId == SWITCH_2.id
                || typeId == ALTAR_1.id
                || typeId == ALTAR_2.id;
    }

    /**
     * Native: Unit::resetActionStateToDying @005105E6.
     * Fully ported.
     */
    public void resetActionStateToDying() {
        beginDyingTransition();
        if (actionReadyFlag == 0 && pItem != null && pItem.getSlot() == MAGIC_ITEM_TYPE) {
            pItem = null;
            spell = null;
        }
        spell = null;
        subState = 0;
        actionReadyFlag = 1;
        state = UnitActionState.DYING;
        missionActionCode = MissionActionCode.DYING;
    }

    /**
     * Native: Unit::beginDyingTransition @00510745.
     * Fully ported.
     */
    public void beginDyingTransition() {
        if (pItem == null || (pItem.type & 0xFF) == 0x02) {
            return;
        }
        if (pItem.effects.isEmpty()) {
            return;
        }
        Effect firstEffect = pItem.effects.getFirst();
        if (firstEffect == null || !firstEffect.isA(EffectId.CAST_SPELL)) {
            return;
        }
        if ((state == UnitActionState.CAST_SPELL || state == UnitActionState.USE_SKILL) && actionReadyFlag == 0) {
            return;
        }
        if (spell == null) {
            return;
        }

        int expectedSpellId = firstEffect.mValue.getS1Signed();
        if ((spell.id & 0xFF) == expectedSpellId) {
            spell = null;
            inventory.addItem(pItem);
            pItem = null;
            return;
        }

        Globals.gameServer.pushMessage("Unit::Canceling wrong spell from item.");
    }

    /**
     * Native: Unit::FinalizeDeath @00510A70.
     * Fully ported.
     */
    public void finalizeDeath() {
        if (forceFinalCorpseStageOnDeath != 0) {
            m_nHP = -10000;
            forceFinalCorpseStageOnDeath = 0;
        }

        state = UnitActionState.DEAD;
        missionActionCode = MissionActionCode.DEAD;
        Globals.worldMap.detachUnit(this);
        m_pTargetHandle.clearSubPos();

        if (pShield != null) {
            inventory.addItem(releaseIncomingObject(pShield));
        }
        if (shouldDropWeaponOnDeath()) {
            inventory.addItem(releaseIncomingObject(pWeapon));
        }
        moveEquippedItemsToInventory();

        if (hasNpcMarkerInName() || suppressDeathLootFlag != 0) {
            inventory = new Inventory();
        }

        int goldDrop = 0;
        if (shouldGenerateDeathTreasure()) {
            goldDrop = rollDeathGold();
            Item deathTreasure = rollDeathTreasureItem();
            if (deathTreasure != null) {
                deathTreasure.count = 1;
                inventory.addItem(deathTreasure);
            }
        }

        Player ownerPlayer = owner;
        if (!inventory.items.isEmpty() || goldDrop != 0) {
            int ownerOnlyFlag = ownerPlayer.controlledUnit == this ? 1 : 0;
            spawnDeathSack(inventory, goldDrop, ownerOnlyFlag);
            inventory = new Inventory();
        }

        flushDeathEffects();

        if (ownerPlayer != null && ownerPlayer.controlledUnit != null && ownerPlayer.controlledUnit == this) {
            ownerPlayer.deathCount += 1;
            CServerApp.netUpdate(this, ownerPlayer, UnitDirtyFlags.CONTROLLED_OWNER_DEATH.value, 0x0FFB, 0, 0);
            Globals.gameServer.saveControlledHumanoid((Humanoid) this);
            m_nHP = -50;
        }

        if (Globals.gameServer.networkSessionActive == 0
                && ownerPlayer.isActive == 0
                && isSinglePlayerQuestDeath()) {
            ownerPlayer.missionResultState = (serverID - 0x13) & 0xFF;
            CServerApp.sendTwoDwordAction(ownerPlayer, GameActionId.MISSION_FAILED_ACTION_B4, ownerPlayer.missionResultState, 0);
        }

        updateCorpseDecay();
    }

    /**
     * Native: Unit::UpdateCorpseDecay @005110B8.
     * Fully ported.
     */
    public void updateCorpseDecay() {
        int previousRespawnStage = respawning;
        if (m_nHP > -10001) {
            if ((Globals.gameServer.someValue & 1) == 0) {
                if (!isShortCorpseDecayUnit() || m_nHP > -61) {
                    m_nHP -= 1;
                }
            }

            if (m_nHP < -10) {
                respawning = 2;
            }
            if (m_nHP < -20) {
                respawning = 3;
            }
            if (m_nHP < -40) {
                respawning = 4;
            }
            if (m_nHP < -600) {
                respawning = 5;
                m_nHP = -10001;
                if (respawning != previousRespawnStage) {
                    emitNetUpdate(UnitDirtyFlags.HP.value);
                }
                if (idFull != 0 && (movementType & 0xFF) < 2) {
                    Globals.gameServer.clearBitForId(idFull);
                    idFull = 0;
                }
                return;
            }
        }

        if (respawning != previousRespawnStage) {
            emitNetUpdate(UnitDirtyFlags.HP.value);
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::executeDropUnitInventoryDeathSackInstant @00578330.
     */
    public void dropInventoryToDeathSackForScript() {
        spawnDeathSack(inventory, 0, 0);
        inventory = new Inventory();
    }

    /**
     * Native: Unit::placeNearMissionCell @00510089.
     * Fully ported.
     */
    public boolean placeNearMissionCell(int x, int y, int diameter) {
        int nativeX = x & 0xFF;
        int nativeY = y & 0xFF;
        int nativeDiameter = diameter & 0xFF;
        int radius = nativeDiameter / 2;
        boolean placed = false;
        m_pTargetHandle.initFromBytes(nativeX, nativeY, Globals.worldMap);

        int attempts = (nativeDiameter * nativeDiameter) / 2 + 1;
        for (int attempt = 0; attempt <= attempts; attempt++) {
            int candidateY = nativeY - radius + randomMissionCellOffset(nativeDiameter);
            int candidateX = nativeX - radius + randomMissionCellOffset(nativeDiameter);
            m_pTargetHandle.setPosition(candidateX, candidateY);
            if (canPlaceReturnedMissionUnitAtCurrentTarget()) {
                placed = true;
                break;
            }
        }

        if (!placed && nativeDiameter != 0) {
            for (int candidateX = nativeX - radius; candidateX <= nativeX + radius; candidateX++) {
                for (int candidateY = nativeY - radius; candidateY <= nativeY + radius; candidateY++) {
                    if (candidateY > 0x0B || Globals.gameServer.networkSessionActive == 0) {
                        m_pTargetHandle.setPosition(candidateX, candidateY);
                        if (Globals.worldMap.canPlaceUnitFootprint(this)) {
                            placed = true;
                            break;
                        }
                    }
                }
                if (placed) {
                    break;
                }
            }
        }
        return placed && Globals.worldMap.refreshSteppedUnitCell(this);
    }

    /**
     * Native support extracted from Unit::placeNearMissionCell @00510089 random candidate generation.
     */
    private static int randomMissionCellOffset(int diameter) {
        return diameter == 0 ? 0 : Utils.randInclusive(diameter);
    }

    /**
     * Native support extracted from Unit::placeNearMissionCell @00510089 placement acceptance check.
     */
    private boolean canPlaceReturnedMissionUnitAtCurrentTarget() {
        int y = m_pTargetHandle.getY();
        return (y > 0x0B || Globals.gameServer.networkSessionActive == 0)
                && Globals.worldMap.canPlaceUnitFootprint(this);
    }

    /**
     * Native: Unit::hideFromMissionMap @005102A3.
     * Fully ported.
     */
    public void hideFromMissionMap() {
        if ((status & UNIT_STATUS_INACTIVE) == 0) {
            Globals.worldMap.detachUnit(this);
            status |= UNIT_STATUS_INACTIVE;
            Globals.gameServer.activeUnits.remove(this);
            CServerApp.sendUnitVisibilityAction(this, true, null);
            CServerApp.sendUnitVisibilityAction(this, false, owner);
        }
    }

    /**
     * Native: Unit::returnToMissionMap @005103AE.
     * Fully ported.
     */
    public int returnToMissionMap() {
        int x = m_pTargetHandle.getX();
        int y = m_pTargetHandle.getY();
        if (!placeNearMissionCell(x, y, 0)
                && !placeNearMissionCell(x, y, 3)) {
            Globals.gameServer.pushMessage("Unit can't return to map - no free place.");
            return 0;
        }
        Globals.gameServer.activeUnits.add(this);
        status &= ~UNIT_STATUS_INACTIVE;
        CServerApp.sendInitialTokenStateToMapLoadingPlayers(this);
        CServerApp.netUpdate(this, null, UnitDirtyFlags.POSITION_AND_FACING.value, 0x0FFB, 0, 0);
        CServerApp.sendUnitVisibilityAction(this, false, null);
        return 1;
    }

    /**
     * Native: Unit::returnToMissionMapNearCell @00510318.
     * Fully ported.
     */
    public boolean returnToMissionMapNearCell(int x, int y, int diameter) {
        if (!placeNearMissionCell(x, y, diameter)) {
            Globals.gameServer.pushMessage("Unit can't enter map - no free place.");
            return false;
        }
        Globals.gameServer.activeUnits.add(this);
        status &= ~UNIT_STATUS_INACTIVE;
        CServerApp.netUpdate(this, null, UnitDirtyFlags.POSITION_AND_FACING.value, 0x0FFB, 0, 0);
        CServerApp.sendUnitVisibilityAction(this, false, null);
        return true;
    }

    /**
     * Native: Unit::applyHpDeltaAndResetRespawnOnRevive @00510484.
     * Fully ported.
     */
    public void applyHpDeltaAndResetRespawnOnRevive(int delta) {
        int previousHp = m_nHP;
        changedValues |= UnitDirtyFlags.HP.value;
        m_nHP += delta;
        if (m_nHP > m_nMaxHP) {
            m_nHP = m_nMaxHP;
        }
        if (previousHp < 1 && m_nHP > 0) {
            resetMissionEntryRespawnState();
        }
    }

    /**
     * Native: Unit::resetMissionEntryRespawnState @00510534.
     * Fully ported.
     */
    public void resetMissionEntryRespawnState() {
        changedValues |= UnitDirtyFlags.toValue(
                UnitDirtyFlags.DEFENCE,
                UnitDirtyFlags.POSITION_AND_FACING,
                UnitDirtyFlags.HP
        );
        m_pTargetHandle.clearSubPos();
        respawning = 0;
        subState = 0;
        actionReadyFlag = 1;
        unitStatData.defence = (short) (unitStatData.defence << 1);
        recalculateDerivedStats();
        if (m_nHP < 1) {
            m_nHP = 1;
        }
        initializeScenarioMissionEntryUnit(Globals.gameServer.missionScriptRuntime);
    }

    /**
     * Native: Unit::transferToPlayerForMissionScript @004F0568.
     * Fully ported.
     */
    public void transferToPlayerForMissionScript(Player targetPlayer) {
        Player previousOwner = owner;
        if (unitGroup != null) {
            unitGroup.removeUnit(this);
        }
        previousOwner.ownedUnits.remove(this);
        owner = targetPlayer;
        targetPlayer.ownedUnits.add(this);
        UnitGroup group = new UnitGroup();
        targetPlayer.unitGroups.add(group);
        group.addUnit(this);
        group.initializeScenarioMissionEntryGroup(Globals.gameServer.missionScriptRuntime);
        word &= ~previousOwner.scanMask;
        word &= ~targetPlayer.scanMask;
        CServerApp.netUpdate(this, null, -1, 0x0FFB, 0, 0);
        ownershipTransferRefreshFlag = 1;
    }

    /**
     * Native: Unit::spawnDeathSack @005109F5.
     * Fully ported.
     */
    private void spawnDeathSack(Inventory inventory, int gold, int ownerOnlyFlag) {
        Globals.gameServer.objectLists.sacks.createOrMergeSackAtTargetAndNotify(
                m_pTargetHandle,
                inventory,
                gold,
                ownerOnlyFlag
        );
    }

    /**
     * Native support extracted from Unit::FinalizeDeath @00510A70 effect cleanup tail.
     * Fully ported.
     */
    private void flushDeathEffects() {
        for (int i = 0; i < effects.size(); ) {
            Effect effect = effects.get(i);
            if (effect.hasDurationOrContinuous()) {
                effect.mValue.setS2(1);
                effect.updateOnTick(this);
                if (effect.isExpired()) {
                    effects.remove(i);
                    continue;
                }
            }
            i++;
        }
    }

    /**
     * Native support extracted from UnitList::updateActiveUnits @0052B459 corpse migration effect cleanup.
     */
    public void flushCorpseMigrationEffects() {
        for (int i = 0; i < effects.size(); ) {
            Effect effect = effects.get(i);
            effect.mValue.setS2(1);
            effect.sourceUnit = null;
            effect.updateOnTick(this);
            if (effect.isExpired()) {
                effects.remove(i);
                continue;
            }
            i++;
        }
    }

    /**
     * Native support extracted from Unit::FinalizeDeath @00510A70 treasure.1 branch.
     * Fully ported.
     */
    private int rollDeathGold() {
        UnitInfo info = (UnitInfo) unitInfoLine;
        int chance = info.getAttribute(UnitColumn.TREASURE1_GOLD);
        if (Utils.randPercent0To99() >= chance) {
            return 0;
        }
        int minGold = info.getAttribute(UnitColumn.TREASURE1_MIN);
        int maxGold = info.getAttribute(UnitColumn.TREASURE1_MAX);
        return minGold + Utils.randInclusive(maxGold);
    }

    /**
     * Native support extracted from Unit::FinalizeDeath @00510A70 treasure.2 branch.
     * Fully ported.
     */
    private Item rollDeathTreasureItem() {
        UnitInfo info = (UnitInfo) unitInfoLine;
        int chance = info.getAttribute(UnitColumn.TREASURE2_ITEM);
        if (Utils.randPercent0To99() >= chance) {
            return null;
        }

        ShopAssortmentEntry entry = new ShopAssortmentEntry();
        entry.minPrice = info.getAttribute(UnitColumn.TREASURE2_MIN);
        entry.maxPrice = info.getAttribute(UnitColumn.TREASURE2_MAX);
        entry.itemCount = 100;
        entry.maxSameTypeItems = 1;
        entry.selectionMask = info.getAttribute(UnitColumn.TREASURE2_MASK);

        ItemAssortmentGenerator generator = new ItemAssortmentGenerator();
        CustomList<Item> generated = generator.generateItemArray(entry);
        if (generated.isEmpty()) {
            return null;
        }

        Item selected = generated.get(Utils.randInclusive(generated.size() - 1));
        selected.count = 1;
        return selected;
    }

    /**
     * Native support extracted from Unit::FinalizeDeath @00510A70.
     * Fully ported.
     */
    private boolean shouldDropWeaponOnDeath() {
        return pWeapon != null && pWeapon.worldItem.getAttribute(WorldItemColumn.SUITABLE_FOR) != 0;
    }

    /**
     * Native support extracted from Unit::FinalizeDeath @00510A70.
     * Fully ported.
     */
    private boolean shouldGenerateDeathTreasure() {
        return getTypeId() >= 0x40 && suppressDeathLootFlag == 0;
    }

    /**
     * Native support extracted from Unit::FinalizeDeath @00510A70.
     * Fully ported.
     */
    private boolean hasNpcMarkerInName() {
        return unitInfoLine.name.contains("NPC");
    }

    /**
     * Native support extracted from Unit::UpdateCorpseDecay @005110B8.
     */
    private boolean isShortCorpseDecayUnit() {
        int typeId = getTypeId();
        return typeId >= 0x21 && typeId <= 0x24;
    }

    /**
     * Native support extracted from Unit::FinalizeDeath @00510A70.
     */
    private boolean isSinglePlayerQuestDeath() {
        int typeId = getTypeId();
        return typeId >= 0x21 && typeId <= 0x3F;
    }

    /**
     * Native support extracted from Unit::FinalizeDeath @00510A70 and Unit::UpdateCorpseDecay @005110B8.
     */
    private int getTypeId() {
        return typeID & 0xFFFF;
    }

    /**
     * Native support extracted from CServerApp::sendLobbyPlayerInfoSnapshot @00504D39.
     */
    public boolean hasSavedCharacterKillHistoryMarker() {
        return savedCharacterKillHistoryMarker != 0;
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019.
     */
    protected void emitNetUpdate(int updateMask) {
        CServerApp.netUpdate(this, updateMask);
    }

    /**
     * Native support extracted from Unit::update @0050F12C; delegates to CWorldMap::DetachSack @005539E5.
     */
    private void detachSackFromWorld(Sack sack) {
        Globals.worldMap.detachSack(sack);
    }

    /**
     * Native: Unit::refreshEncumbrance @0050F065.
     * Fully ported.
     */
    public void refreshEncumbrance(int equippedDeltaWeight) {
        int previousEncumbranceWeight = m_nEncumbranceWeight;
        m_nEquippedWeight = (short) (m_nEquippedWeight + equippedDeltaWeight);
        m_nEncumbranceWeight = m_nEquippedWeight;

        if (inventory != null) {
            if (inventory.weight < 64_000) {
                m_nEncumbranceWeight = (short) (m_nEncumbranceWeight + inventory.weight / 2);
            } else {
                m_nEncumbranceWeight = 32_000;
            }
        }

        int signedMaxWeight = nativeSignedShort(m_nMaxWeight);
        int previousLevel = nativeSignedShort(previousEncumbranceWeight) / signedMaxWeight;
        int currentLevel = nativeSignedShort(m_nEncumbranceWeight) / signedMaxWeight;
        if (currentLevel != previousLevel) {
            recalculateDerivedStats();
        }
    }

    /**
     * Native support extracted from Unit::refreshEncumbrance @0050F065 and
     * Humanoid::recalculateDerivedStats @0051366B signed 16-bit weight reads.
     * Fully ported.
     */
    protected static int nativeSignedShort(int value) {
        return (short) value;
    }

    /**
     * Native: Unit::pickupSackContents @005108FA.
     * Fully ported.
     */
    public void pickupSackContents(Sack sack) {
        if (sack == null) {
            return;
        }
        if (sack.gold > 0) {
            owner.adjustGoldAndNotify(sack.gold, 0);
        }
        if (sack.inventory != null) {
            for (Item item : sack.inventory.items) {
                item.scenarioObjectId = 1;
            }
            inventory.drainItemsFrom(sack.inventory);
            sack.inventory = null;
            refreshEncumbrance(0);
        }
        CServerApp.notifySackRemoved(sack);
        emitNetUpdate(UnitDirtyFlags.INVENTORY_AND_ENCUMBRANCE.value);
    }

    /**
     * Native support extracted from Unit::update @0050F12C; calls MissionScriptRuntime::updateUnitMissionRuntime @0056DCA2.
     */
    private void notifyMissionScriptRuntimeUnitTick() {
        Globals.gameServer.missionScriptRuntime.updateUnitMissionRuntime(this);
    }

    /**
     * Native support extracted from Unit::calculateIncomingDamage @00516F6F; calls
     * MissionScriptRuntime::recordUnitEngagement @0056E6B8.
     */
    private void notifyMissionScriptRuntimeCombat(Unit attacker) {
        Globals.gameServer.missionScriptRuntime.recordUnitEngagement(attacker, this, 0);
    }

    /**
     * Native support extracted from Unit::update @0050F12C; delegates to
     * CServerApp::sendUnitCommandStartAction @005040C4.
     */
    private void emitFacingPacket(int dir, int step, int packetCode) {
        GameActionId actionId = packetCode == 0 ? GameActionId.UNKNOWN_ACTION_00 : GameActionId.fromId(packetCode);
        if (actionId == null) {
            throw new IllegalArgumentException("Unsupported unit command packet id: " + packetCode);
        }
        CServerApp.sendUnitCommandStartAction(this, dir, step, actionId);
    }

    /**
     * Native support extracted from Unit::update @0050F12C; calls
     * MissionScriptRuntime::executeScriptInstant @00574F3F.
     */
    private Object runPointerAction(int pointerActionId) {
        Globals.gameServer.missionScriptRuntime.executeScriptInstant(pointerActionId);
        return null;
    }

    /**
     * vtbl +0x14: Unit::updateRegen @0050FC5A.
     * Fully ported.
     */
    @Override
    public void updateRegen() {
        if (state == UnitActionState.DEAD) {
            return;
        }

        if (m_nHP < 1) {
            if (m_nHP < 0 && signedModulo4(Globals.gameServer.someValue) == 0) {
                m_nHP -= 1;
                CServerApp.netUpdate(this, owner, UnitDirtyFlags.HP.value, 0x0FFB, 0, 0);
            }
            return;
        }

        int regenStep = getOwnerSyncTickDelta() > 0x50 ? 3 : 1;
        if (m_nHP < m_nMaxHP
                && m_nHPRegenRate != 0
                && signedModulo4(Globals.gameServer.someValue) == signedModulo4(idFull)) {
            int hpScaled = m_nHP * 100
                    + m_nHPFraction
                    + (m_nMaxHP * 2 * (mModifiers.hpRegen + 100) * regenStep) / m_nHPRegenRate;
            m_nHPFraction = hpScaled % 100;
            m_nHP = Math.min(hpScaled / 100, m_nMaxHP);
            emitNetUpdate(UnitDirtyFlags.HP.value);
        }

        if (m_nMP < m_nMaxMP) {
            int mpScaled = m_nMP * 100
                    + m_nMPFraction
                    + (m_nMaxMP * (mModifiers.mpRegen + 100) * regenStep) / m_nMPRegenRate;
            m_nMPFraction = mpScaled % 100;
            m_nMP = Math.min(mpScaled / 100, m_nMaxMP);
            emitNetUpdate(UnitDirtyFlags.MP.value);
        }
    }

    /**
     * vtbl +0x1C: Unit::getTokenSizeVirtual @00542360.
     * Fully ported.
     */
    @Override
    public int getTokenSizeVirtual() {
        return tokenSize;
    }

    /**
     * vtbl +0x20: Unit::getMovementType @00542380.
     * Fully ported.
     */
    @Override
    public int getMovementType() {
        return movementType;
    }

    /**
     * vtbl +0x2C: Unit::isUnitToken @00542410.
     * Fully ported.
     */
    @Override
    public int isUnitToken() {
        return 1;
    }

    /**
     * vtbl +0x30: Unit::isHumanoidToken @00542420.
     * Base Unit marker returns 0 for static units tableline resolution.
     * Fully ported.
     */
    @Override
    public int isHumanoidToken() {
        return 0;
    }

    /**
     * vtbl +0x38: Unit::clearOwnedResources @0050EE14.
     * Fully ported.
     */
    public void clearOwnedResources() {
        if (pItem != null && pItem.getSlot() == MAGIC_ITEM_TYPE) {
            pItem = null;
            spell = null;
        }
        movementState.resetToDefaults();
        missionRuntimeState.resetToDefaults();
        inventory = null;
        pShield = null;
        pWeapon = null;
        secondarySpell = null;
        secondarySpellRestoreToken = null;
        spellbook = null;
    }

    /**
     * vtbl +0x3C: Unit::copyFrom @0050E48E.
     * Native semantics are not a pure deep clone:
     * - several pointer fields are reset to null/default
     * - UnitMovementState / UnitMissionRuntimeState are reinitialized, not copied from source
     * - m_list1 / m_list2 are cleared
     * - forceFinalCorpseStageOnDeath is preserved on this unit
     * Fully ported.
     */
    public Unit copyFrom(Unit source) {
        idFull = source.idFull;
        copyTokenStateFrom(source);
        word = 0;        // native sets Token.word to 0
        effects.clear(); // native clears effects list

        unitInfoLine = source.unitInfoLine;
        lastDamageSource = null;
        lastDamageSourceRestoreToken = null;
        secondarySpell = null;
        secondarySpellRestoreToken = null;
        killCreditSkillContext = 0;
        tokenSize = source.tokenSize;
        movementType = source.movementType;
        face = source.face;
        status = source.status;
        missionActionCode = source.missionActionCode;
        state = source.state;
        subState = source.subState;
        actionTarget = null;
        actionTargetRestoreToken = null;
        skillTargetX = 0;
        skillTargetY = 0;
        spell = null;
        pItem = null;
        timerOrCounter = source.timerOrCounter;
        unitGroup = null;

        pWeapon = (source.pWeapon == null) ? null : new Weapon().copyFrom(source.pWeapon);
        pShield = (source.pShield == null) ? null : new Shield().copyFrom(source.pShield);
        inventory = (source.inventory == null) ? null : new Inventory().copyFrom(source.inventory);

        str = source.str;
        m_nBody = source.m_nBody;
        m_nReaction = source.m_nReaction;
        m_nMind = source.m_nMind;
        m_nSpirit = source.m_nSpirit;
        speed = source.speed;
        m_nEquippedWeight = source.m_nEquippedWeight;
        m_nEncumbranceWeight = source.m_nEncumbranceWeight;
        m_nMaxWeight = source.m_nMaxWeight;
        m_nHP = source.m_nHP;
        m_nMaxHP = source.m_nMaxHP;
        m_nHPRegenRate = source.m_nHPRegenRate;
        m_nMP = source.m_nMP;
        m_nMaxMP = source.m_nMaxMP;
        m_nMPRegenRate = source.m_nMPRegenRate;
        m_wRegenStore = source.m_wRegenStore;
        m_nHPFraction = source.m_nHPFraction;
        m_nMPFraction = source.m_nMPFraction;
        sightFraction = source.sightFraction;
        sightRange = source.sightRange;

        skillData.assign(source.skillData);
        unitStatData.assign(source.unitStatData);
        mModifiers.assign(source.mModifiers);
        skillDataSnapshot.assign(source.skillDataSnapshot);

        defaultCastRange = source.defaultCastRange;
        skillsTotalBonusPermille = source.skillsTotalBonusPermille;
        attackChargeTicks = source.attackChargeTicks;
        attackRelaxTicks = source.attackRelaxTicks;
        actionReadyFlag = source.actionReadyFlag;
        lastOwnerSyncTick = source.lastOwnerSyncTick;
        respawning = source.respawning;
        effectKeyFlags = 0;
        serverID = source.serverID;
        changedValues = UnitDirtyFlags.NONE.value;
        Arrays.fill(deferredNetUpdateFlagsByPlayerId, 0);

        movementState.resetToDefaults();      // native realloc+Init of UnitMovementState
        missionRuntimeState.resetToDefaults();      // native realloc+New of UnitMissionRuntimeState
        mList1.clear();
        mList2.clear();

        pTargetHandle0x200 = TargetHandle.newDefaultOrNull(source.pTargetHandle0x200);

        innUnitRelocationQuestFlag = source.innUnitRelocationQuestFlag;
        innRecruitmentQuestFlag = source.innRecruitmentQuestFlag;
        hostileUnitRelocationQuestFlag = source.hostileUnitRelocationQuestFlag;
        ownershipTransferRefreshFlag = source.ownershipTransferRefreshFlag;
        return this;
    }

    /**
     * vtbl +0x40: Unit::prepareIncomingObject @00510854.
     * Fully ported.
     */
    public Item prepareIncomingObject(Item candidate) {
        Item processed = candidate.useAndConsume(this);
        refreshPrice();
        return processed;
    }

    /**
     * vtbl +0x44: Unit::addIncomingObjectToInventory @00510883.
     * Fully ported.
     */
    public void addIncomingObjectToInventory(Item candidate) {
        Item processed = candidate.useAndConsume(this);
        if (processed != null) {
            inventory.addItem(processed);
            refreshPrice();
        }
    }

    /**
     * vtbl +0x48: Unit::releaseIncomingObject @005108C6.
     * Fully ported.
     */
    public Item releaseIncomingObject(Item candidate) {
        if (candidate == null) {
            return null;
        }
        candidate.takeOff(this);
        refreshPrice();
        return candidate;
    }

    /**
     * vtbl +0x4C: Unit::moveEquippedItemsToInventory @005423A0, base no-op hook.
     * Fully ported.
     */
    public void moveEquippedItemsToInventory() {
        // no-op in Unit
    }

    /**
     * vtbl +0x50: Unit::creditOwnerForObjectValue @005423B0, base no-op hook.
     * Fully ported.
     */
    public void creditOwnerForObjectValue(Token candidate) {
        // no-op in Unit
    }

    /**
     * vtbl +0x54: Unit::calculateIncomingDamage @00516F6F.
     * Fully ported.
     */
    public int calculateIncomingDamage(SkillData attackData, Unit attacker) {
        if (attackData == null || (status & UNIT_STATUS_INACTIVE) != 0) {
            return 0;
        }

        int rawPhysicalMin = unsignedByte(attackData.skillDamageType0And3Min);
        int rawPhysicalSpread = unsignedByte(attackData.skillDamageType0And3Modifier);
        int physicalMin = rawPhysicalMin;
        int physicalSpread = rawPhysicalSpread;
        if ((rawPhysicalMin & 0x80) != 0 && attacker.isHumanoidToken() == 0) {
            physicalMin = (rawPhysicalMin & 0x7F) * 0x0F;
            physicalSpread *= 0x0F;
        }

        int physicalSpreadMode = 0;
        if (attacker != null && attacker.owner != null) {
            physicalSpreadMode = resolvePhysicalSpreadMode(attacker);
        }
        if (physicalSpreadMode > 0) {
            physicalMin += physicalSpread;
        } else if (physicalSpreadMode == 0) {
            physicalMin += physicalSpread == 0 ? 0 : Utils.randExclusive(0, physicalSpread);
        }

        int attackSkill = attackData.toHit;
        int toHitRoll = Utils.randExclusive(0, 200) - 100;
        int defence = unitStatData.defence;
        int totalDamage = 0;
        int physicalDamage = 0;
        if (defence < attackSkill + toHitRoll
                || (attacker != null && (attacker.status & UNIT_STATUS_AP_ATTACK_PROFILE) != 0)
                || toHitRoll >= 0x5A) {
            physicalDamage = physicalMin;
        }
        if (toHitRoll < -0x5A) {
            physicalDamage = 0;
        }

        if (physicalDamage > 0) {
            if (attacker != null && (attacker.status & UNIT_STATUS_AP_ATTACK_PROFILE) == 0) {
                physicalDamage -= unitStatData.absorbtion;
            }
            if (physicalDamage < 0) {
                physicalDamage = 0;
            }

            int resistanceIndex = unsignedByte(attackData.activeSkillIndex);
            physicalDamage = applyPercentProtectionRounded(
                    physicalDamage,
                    unsignedByte(unitStatData.m_bModifiers[resistanceIndex])
            );
            totalDamage = physicalDamage;
        }

        int secondaryBaseDamage = unsignedByte(attackData.skillDamageType1Min);
        int secondarySpreadDamage = unsignedByte(attackData.skillDamageType1Modifier);
        if (secondaryBaseDamage + secondarySpreadDamage != 0) {
            int secondaryDamage = secondaryBaseDamage
                    + (secondarySpreadDamage == 0 ? 0 : Utils.randExclusive(0, secondarySpreadDamage));
            if (unitStatData.protections.length > 2) {
                secondaryDamage = applyPercentProtectionRounded(secondaryDamage, unitStatData.protections[2]);
            }
            totalDamage += secondaryDamage;
        }

        int magicBaseDamage = unsignedByte(attackData.skillDamageType2Min);
        int magicSpreadDamage = unsignedByte(attackData.skillDamageType2Modifier);
        if (magicBaseDamage + magicSpreadDamage != 0
                && (defence < attackSkill + toHitRoll || rawPhysicalMin + rawPhysicalSpread == 0)) {
            int magicDamage = magicBaseDamage
                    + (magicSpreadDamage == 0 ? 0 : Utils.randExclusive(0, magicSpreadDamage));
            magicDamage = applyMagicProtectionRounded(magicDamage,
                    unsignedByte(attackData.skillDamageType2ProtectionIndex));
            if (magicDamage > 0) {
                totalDamage += magicDamage;
            }
        }

        updateLastDamageSourceContext(attacker, attackData);
        return Math.max(totalDamage, 0);
    }

    /**
     * Native: Unit::applyGodModeCheat @005175FC.
     * Fully ported.
     */
    public void applyGodModeCheat() {
        for (int index = 0; index < 6; index++) {
            mModifiers.statMods.protections[index] = 100;
            mModifiers.statMods.m_bModifiers[index] = 100;
        }
        recalculateDerivedStats();
    }

    /**
     * vtbl +0x58: Unit::recalculateDerivedStats @005118BE.
     * Fully ported.
     */
    public void recalculateDerivedStats() {
        if (owner == null) {
            m_wRegenStore = m_nMaxMP;
        } else {
            Player player = owner;
            m_wRegenStore = (player.mpRegenPercent * m_nMaxMP) / 100;
        }
    }

    /**
     * vtbl +0x5C: Unit::refreshPrice @005423C0.
     * Native returns the cached Token.price field; Humanoid overrides recompute it.
     * Fully ported.
     */
    public int refreshPrice() {
        return price;
    }

    /**
     * vtbl +0x60: Unit::generateRandomInventoryItem @0051206F, base no-op hook.
     * Fully ported.
     */
    public void generateRandomInventoryItem() {
        // no-op in Unit
    }

    /**
     * vtbl +0x64: Unit::updateSkills @005175AB.
     * Base Unit only accumulates total bonus permille and kill credit.
     * Fully ported.
     */
    public void updateSkills(int skillDeltaPermille, Unit maybeVictim, int unused) {
        skillsTotalBonusPermille += skillDeltaPermille;
        if (maybeVictim != null && maybeVictim.isDying()) {
            owner.creatureKillCount += 1;
        }
    }

    /**
     * vtbl +0x68: Unit::awardKillSkillProgress @005423E0, base no-op hook.
     * Fully ported.
     */
    public void awardKillSkillProgress(Unit defeatedUnit, int spellId) {
        // no-op in Unit
    }

    /**
     * vtbl +0x6C: Unit::awardDamageSkillProgress @005423F0, base no-op hook.
     * Fully ported.
     */
    public void awardDamageSkillProgress(Unit targetUnit, int damage, int spellId) {
        // no-op in Unit
    }

    /**
     * vtbl +0x70: Unit::awardSpellCastSkillProgress @00542400, base no-op hook.
     * Fully ported.
     */
    public void awardSpellCastSkillProgress(Token targetToken, int spellId) {
        // no-op in Unit
    }

    /**
     * vtbl +0x74: Unit::getDyingStateTimerTicks @0050F048.
     * Used during dying-state transition as a timer source.
     * Fully ported.
     */
    public int getDyingStateTimerTicks() {
        return unitInfoLine.getValue(UnitColumn.DYING_TIME.index);
    }

    /**
     * Native support extracted from Unit::updateRegen @0050FC5A.
     */
    private static int signedModulo4(int value) {
        int remainder = (int) (Math.abs((long) value) & 0x3L);
        return value < 0 ? -remainder : remainder;
    }

    /**
     * Native support extracted from Unit::calculateIncomingDamage @00516F6F.
     */
    private int resolvePhysicalSpreadMode(Unit attacker) {
        Effect effect14 = resolveTimedAttackEffect(attacker, 0x14);
        if (effect14 != null && Utils.randPercent0To99() < effect14.mValue.getS1Signed()) {
            return 1;
        }

        Effect effect1c = resolveTimedAttackEffect(attacker, 0x1C);
        if (effect1c != null && Utils.randPercent0To99() < effect1c.mValue.getS1Signed()) {
            return -1;
        }
        return 0;
    }

    /**
     * Native support extracted from Unit::calculateIncomingDamage @00516F6F.
     */
    private Effect resolveTimedAttackEffect(Unit attacker, int effectKey) {
        if (attacker == null || !attacker.hasEffectKeyFlag(effectKey)) {
            return null;
        }
        Effect effect = attacker.getEffect(effectKey);
        if (effect == null) {
            attacker.effectKeyFlags &= ~(1 << (effectKey & 0x1F));
        }
        return effect;
    }

    /**
     * Native support extracted from Unit::calculateIncomingDamage @00516F6F.
     */
    private int applyMagicProtectionRounded(int magicDamage, int damageSchool) {
        int protectionIndex = switch (damageSchool - 1) {
            case 0 -> 1;
            case 1 -> 2;
            case 2 -> 3;
            case 3 -> 4;
            case 4 -> 5;
            default -> -1;
        };
        if (protectionIndex < 0) {
            Globals.gameServer.pushMessage("Unknown magic damage type.");
            return magicDamage;
        }
        return applyPercentProtectionRounded(magicDamage, unitStatData.protections[protectionIndex]);
    }

    /**
     * Native support extracted from Unit::calculateIncomingDamage @00516F6F.
     */
    private void updateLastDamageSourceContext(Unit attacker, SkillData attackData) {
        if (attacker == null || !attacker.hasUnitInfoLine()) {
            return;
        }
        if (attacker.owner == null) {
            lastDamageSource = null;
            lastDamageSourceRestoreToken = null;
        } else {
            lastDamageSource = attacker;
            lastDamageSourceRestoreToken = null;
            killCreditSkillContext = attacker.isMageClass() ? unsignedByte(attackData.skillDamageType2ProtectionIndex) : 0;
        }
        if (attacker.owner != null && owner != null) {
            notifyMissionScriptRuntimeCombat(attacker);
        }
    }

    /**
     * Native support extracted from Unit::calculateIncomingDamage @00516F6F.
     */
    private static int applyPercentProtectionRounded(int damage, int protectionPercent) {
        if (protectionPercent == 0) {
            return damage;
        }
        return Math.max((int) Math.floor(((100.0d - protectionPercent) * damage) / 100.0d + 0.75d), 0);
    }

    /**
     * Native support extracted from Unit::calculateIncomingDamage @00516F6F byte-packed SkillData reads.
     */
    private static int unsignedByte(byte value) {
        return value & 0xFF;
    }

    /**
     * vtbl +0x24: Unit::restoreContext @0052CEF7.
     * Fully ported.
     */
    @Override
    public void restoreContext() {
        super.restoreContext();

        Object actionTargetRef = actionTargetRestoreToken != null ? actionTargetRestoreToken : actionTarget;
        actionTarget = (Token) SpellEffect.restoreMappedPointer(actionTargetRef);
        actionTargetRestoreToken = null;
        Object spellRef = spellRestoreToken != null ? spellRestoreToken : spell;
        spell = Spell.restoreContextToken(spellRef);
        spellRestoreToken = null;
        Object secondarySpellRef = secondarySpellRestoreToken != null ? secondarySpellRestoreToken : secondarySpell;
        secondarySpell = Spell.restoreContextToken(secondarySpellRef);
        secondarySpellRestoreToken = null;
        pItem = Item.restoreContextToken(pItem);
        Object lastDamageSourceRef = lastDamageSourceRestoreToken != null
                ? lastDamageSourceRestoreToken
                : lastDamageSource;
        lastDamageSource = (Token) SpellEffect.restoreMappedPointer(lastDamageSourceRef);
        lastDamageSourceRestoreToken = null;

        movementState.restoreContext();
        missionRuntimeState.restoreContext();
    }

    /**
     * Native: initializeScenarioMissionEntryUnit @0056DBA1.
     * Fully ported.
     *
     * @param missionScriptRuntime
     */
    public void initializeScenarioMissionEntryUnit(MissionScriptRuntime missionScriptRuntime) {
        resetScenarioMissionUnitScriptState(missionScriptRuntime);
        enterScenarioMissionUnitScriptState(missionScriptRuntime);
    }

    /**
     * Native: resetScenarioMissionUnitScriptState @0056DBFB.
     * Fully ported.
     *
     * @param missionScriptRuntime
     */
    public void resetScenarioMissionUnitScriptState(MissionScriptRuntime missionScriptRuntime) {
        resetScenarioMissionUnitRuntimeContext(missionScriptRuntime);
        missionRuntimeState.unitScriptState = 0;
    }

    /**
     * Native: enterScenarioMissionUnitScriptState @0056D52B.
     * Fully ported.
     *
     * @param missionScriptRuntime
     */
    public void enterScenarioMissionUnitScriptState(MissionScriptRuntime missionScriptRuntime) {
        resetScenarioMissionUnitScriptState(missionScriptRuntime);
        missionActionCode = ENGAGE_NEAREST;
        missionRuntimeState.missionScriptCell = m_pTargetHandle.getCell();
        missionRuntimeState.command = MissionScriptRuntime.UNIT_MISSION_COMMAND_NONE;
    }

    /**
     * Native: resetScenarioMissionUnitRuntimeContext @0056D969.
     * Fully ported.
     * NOTE: natives have a bug and this method may eventually to be called with
     * missionScriptRuntime == null. Yet, it looks like 2 Ifs are filtering out NPE from ever occurring.
     *
     * @param missionScriptRuntime
     */
    public void resetScenarioMissionUnitRuntimeContext(MissionScriptRuntime missionScriptRuntime) {
        if (movementState.facing != movementState.facingLast) {
            movementState.facingLast = movementState.facing;
        }
        if (movementState.cell != 0 && m_pTargetHandle.isSubPosUnknown()) {
            int targetCell = m_pTargetHandle.getCell();
            if ((movementState.cell & 0xFFFF) != targetCell) {
                missionScriptRuntime.worldMap.refreshUnitCellFromTargetHandle(this);
            }
        }
        missionRuntimeState.attackRange = getCastRangeForFirstCastableSpellOrFallbackSupport();
        missionRuntimeState.virtualCasterQueuedFlag = 0;
        movementState.movementTargetUnit = null;
        state = UnitActionState.DYING;
    }

    /**
     * Native support extracted from MissionScriptRuntime::getCastRangeForFirstCastableSpellOrFallback @0056AC5B.
     * The native callee stores ECX but does not read the MissionScriptRuntime object.
     */
    public int getCastRangeForFirstCastableSpellOrFallbackSupport() {
        if (missionRuntimeState.spellIndex != 0) {
            Spell candidate = spellbook.find(missionRuntimeState.spellIndex);
            if (candidate != null && (short) candidate.manaCost <= (short) m_nMP) {
                return candidate.maxRange & 0xFF;
            }
        }
        return defaultCastRange & 0xFF;
    }
}
