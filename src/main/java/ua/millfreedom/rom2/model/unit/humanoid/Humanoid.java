package ua.millfreedom.rom2.model.unit.humanoid;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.Armor;
import ua.millfreedom.rom2.model.CServerApp;
import ua.millfreedom.rom2.model.Item;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.Shield;
import ua.millfreedom.rom2.model.Token;
import ua.millfreedom.rom2.model.Weapon;
import ua.millfreedom.rom2.model.column.HumanColumn;
import ua.millfreedom.rom2.model.column.SpellColumn;
import ua.millfreedom.rom2.model.column.WorldItemColumn;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.unit.UnitDirtyFlags;

import java.io.IOException;
import java.util.Arrays;

import static ua.millfreedom.rom2.model.SkillProgression.clampAwardedSkillProgressPermille;
import static ua.millfreedom.rom2.model.SkillProgression.convertSkillBonusPermilleToCompoundLevel;
import static ua.millfreedom.rom2.model.SkillProgression.get10pctCompoundPermille;
import static ua.millfreedom.rom2.model.SkillProgression.skillLevelForBonusPermille;

public class Humanoid extends Unit {
    private static final int ITEMS_COUNT = 0x0C; // native serialized loop: i=1..12, object offsets +0x20C..+0x238.
    private static final int FIRST_SKILL_INDEX = 1;
    private static final int SKILL_INDEX_LIMIT = 6;
    private static final int MAX_SKILL_LEVEL = 100;
    private static final int PLAYER_RELATION_ALLIED_MASK = 0x02;
    private static final int CONTROLLED_HUMANOID_SAVE_INTERVAL_MS = 15_000;

    //0x208
    public Item equipmentSlot0;
    //0x20C
    public final Item[] items = new Item[ITEMS_COUNT];
    //0x23C
    public final SkillBonus skillBonusesPermille = new SkillBonus();

    /**
     * Native: Humanoid::New @00512133.
     * Fully ported; Java object allocation represents Humanoid::Humanoid @0051207C.
     */
    public Humanoid() {
        initializeNativeHumanoidDefaults();
    }

    /**
     * Native: Humanoid::Init @005121E1.
     * Fully ported.
     */
    private void initializeNativeHumanoidDefaults() {
        equipmentSlot0 = null;
        Arrays.fill(items, null);
        Arrays.fill(skillBonusesPermille.data, 0);
        m_nBody = 0x1E;
        m_nMind = 0x1E;
        m_nReaction = 0x1E;
        m_nSpirit = 0x1E;
        m_nMaxHP = 0x32;
        m_nHP = 0x32;
        speed = 0x10;
        movementState.rotationSpeed = 8;
        attackChargeTicks = 8;
        attackRelaxTicks = 4;
        skillData.skillDamageType0And3Min = 1;
        skillData.skillDamageType0And3Modifier = (byte) (2 - (skillData.skillDamageType0And3Min & 0xFF));
        movementState.applyTokenMovementLayerMask(this);
    }

    /**
     * Native: Humanoid::recalcSkillBonuses @00513497.
     * Fully ported.
     */
    public void recalcSkillBonuses() {
        skillsTotalBonusPermille = 0;
        for (int skillIndex = FIRST_SKILL_INDEX; skillIndex < SKILL_INDEX_LIMIT; skillIndex++) {
            int bonusPermille = get10pctCompoundPermille(skillData.skillLevels[skillIndex]);
            skillBonusesPermille.data[skillIndex] = bonusPermille;
            skillsTotalBonusPermille += bonusPermille;
        }
        for (int skillIndex = FIRST_SKILL_INDEX; skillIndex < SKILL_INDEX_LIMIT; skillIndex++) {
            skillDataSnapshot.skillLevels[skillIndex] = skillData.skillLevels[skillIndex];
        }
    }

    /**
     * Native: Humanoid::applyDirectSkillLevelIncrease @00513546.
     * Fully ported.
     */
    public void applyDirectSkillLevelIncrease(int directSkillIndex) {
        int skillIndex = directSkillIndex & 0xFF;
        copySkillSnapshotToCurrent();
        skillData.skillLevels[skillIndex] = (short) (skillData.skillLevels[skillIndex] + 1);

        int bonusDelta = get10pctCompoundPermille(skillData.skillLevels[skillIndex])
                + 1
                - skillBonusesPermille.data[skillIndex];
        skillBonusesPermille.data[skillIndex] += bonusDelta;
        skillsTotalBonusPermille += bonusDelta;

        copyCurrentSkillsToSnapshot();
        recalculateDerivedStats();
    }

    /**
     * Native: Humanoid::prepareMissionReentryRespawn @005132C2.
     * Fully ported.
     */
    public void prepareMissionReentryRespawn() {
        skillsTotalBonusPermille = 0;
        for (int skillIndex = FIRST_SKILL_INDEX; skillIndex < SKILL_INDEX_LIMIT; skillIndex++) {
            int bonusPermille = (skillBonusesPermille.data[skillIndex] * 9) / 10;
            skillBonusesPermille.data[skillIndex] = bonusPermille;
            skillsTotalBonusPermille += bonusPermille;
            skillData.skillLevels[skillIndex] = (short) skillLevelForBonusPermille(bonusPermille);
        }
        for (int skillIndex = FIRST_SKILL_INDEX; skillIndex < SKILL_INDEX_LIMIT; skillIndex++) {
            skillDataSnapshot.skillLevels[skillIndex] = skillData.skillLevels[skillIndex];
        }
        movementState.resetToDefaults();
        missionRuntimeState.resetToDefaults();
        resetMissionEntryRespawnState();
    }

    /**
     * vtbl +0x24: Humanoid::restoreContext @0052CF7A.
     * Fully ported.
     */
    @Override
    public void restoreContext() {
        super.restoreContext();
    }

    /**
     * vtbl +0x2C: Humanoid::isUnitToken @00542720.
     * Fully ported.
     */
    @Override
    public int isUnitToken() {
        return 1;
    }

    /**
     * vtbl +0x30: Humanoid::isHumanoidToken @00542730.
     * Fully ported.
     */
    @Override
    public int isHumanoidToken() {
        return 1;
    }

    /**
     * vtbl +0x5C: Humanoid::refreshPrice @0051754D.
     * Fully ported.
     */
    @Override
    public int refreshPrice() {
        int totalBonusPermille = 0;
        for (int i = 1; i < skillBonusesPermille.data.length; i++) {
            totalBonusPermille += skillBonusesPermille.data[i];
        }
        price = totalBonusPermille / 100;
        return price;
    }

    /**
     * vtbl +0x38: Humanoid::clearOwnedResources @00512341.
     * Fully ported.
     */
    @Override
    public void clearOwnedResources() {
        super.clearOwnedResources();
        for (int i = 0; i < items.length; i++) {
            items[i] = null;
        }
    }

    /**
     * vtbl +0x40: Humanoid::prepareIncomingObject @00512573.
     * Fully ported.
     */
    @Override
    public Item prepareIncomingObject(Item candidate) {
        if (isEquippableInventoryItem(candidate) && !canEquipInventoryItem(candidate)) {
            return candidate;
        }
        return candidate.useAndConsume(this);
    }

    /**
     * vtbl +0x44: Humanoid::addIncomingObjectToInventory @00512638.
     * Fully ported.
     */
    @Override
    public void addIncomingObjectToInventory(Item candidate) {
        Item processed = prepareIncomingObject(candidate);
        if (processed != null) {
            inventory.addItem(processed);
        }
    }

    /**
     * vtbl +0x48: Humanoid::releaseIncomingObject @0051266E.
     * Fully ported.
     */
    @Override
    public Item releaseIncomingObject(Item candidate) {
        if (candidate == null) {
            return null;
        }
        candidate.takeOff(this);
        return candidate;
    }

    /**
     * vtbl +0x4C: Humanoid::moveEquippedItemsToInventory @00512697.
     * Fully ported.
     */
    @Override
    public void moveEquippedItemsToInventory() {
        for (Item item : items) {
            inventory.addItem(releaseIncomingObject(item));
        }
    }

    /**
     * Native: Humanoid::copyHumanoidCloneState @005123C6.
     * Fully ported.
     */
    public void copyHumanoidCloneState(Humanoid sourceClone) {
        super.copyFrom(sourceClone);
        equipmentSlot0 = copyOutpostEquipmentClone(sourceClone.equipmentSlot0);
        for (int i = 0; i < items.length; i++) {
            items[i] = copyOutpostEquipmentClone(sourceClone.items[i]);
        }
        System.arraycopy(
                sourceClone.skillBonusesPermille.data,
                0,
                skillBonusesPermille.data,
                0,
                skillBonusesPermille.data.length
        );
    }

    /**
     * vtbl +0x50: Humanoid::creditOwnerForObjectValue @0051327B.
     * Fully ported.
     */
    @Override
    public void creditOwnerForObjectValue(Token candidate) {
        if (Globals.gameServer.networkSessionActive == 0
                || candidate.isHumanoidToken() != 0
                || isDying()) {
            return;
        }
        owner.adjustGoldAndNotify(candidate.price, 1);
    }

    /**
     * vtbl +0x58: Humanoid::recalculateDerivedStats @0051366B.
     * Fully ported.
     */
    @Override
    public void recalculateDerivedStats() {
        int baseBody = 0x34;
        int baseReaction = 0x32;
        int baseMind = 0x30;
        int baseSpirit = 0x2E;
        boolean isMageClass = isMageClass();
        boolean isFemale = isFemale();
        if (!isMageClass && isFemale) {
            baseBody = 0x32;
            baseReaction = 0x34;
            baseMind = 0x2E;
            baseSpirit = 0x30;
        } else if (isMageClass && !isFemale) {
            baseBody = 0x30;
            baseReaction = 0x2E;
            baseMind = 0x34;
            baseSpirit = 0x32;
        } else if (isMageClass) {
            baseBody = 0x2E;
            baseReaction = 0x30;
            baseMind = 0x32;
            baseSpirit = 0x34;
        }

        m_nBody = Math.min(m_nBody, baseBody + mModifiers.body);
        m_nReaction = Math.min(m_nReaction, baseReaction + mModifiers.reaction);
        m_nMind = Math.min(m_nMind, baseMind + mModifiers.mind);
        m_nSpirit = Math.min(m_nSpirit, baseSpirit + mModifiers.spirit);

        int hpMultiplier = isMageClass ? 1 : 2;
        m_nMaxHP = (short) (m_nBody * hpMultiplier);
        if (m_nMaxHP != 0) {
            double skillBonusLevel = convertSkillBonusPermilleToCompoundLevel(skillsTotalBonusPermille);
            m_nMaxHP = (short) (int) (m_nMaxHP + hpMultiplier * skillBonusLevel);
            m_nMaxHP = (short) (int) ((Math.pow(1.1d, m_nBody) / 100.0d + 1.0d) * m_nMaxHP);
        }

        if (m_nMaxMP == 0) {
            m_nMP = 0;
        } else {
            int mpMultiplier = isMageClass ? 2 : 1;
            m_nMaxMP = (short) (m_nSpirit << 1);
            double skillBonusLevel = convertSkillBonusPermilleToCompoundLevel(skillsTotalBonusPermille);
            m_nMaxMP = (short) (int) (m_nMaxMP + mpMultiplier * skillBonusLevel);
            m_nMaxMP = (short) (int) ((Math.pow(1.1d, m_nSpirit) / 100.0d + 1.0d) * m_nMaxMP);
        }

        int packedSight = (int) ((((m_nMind + m_nReaction) / 25.0d) + 4.0d) * 256.0d);
        sightFraction = packedSight & 0xFF;
        sightRange = (packedSight >>> 8) & 0xFF;

        m_nMaxWeight = m_nBody * 10 + 1;
        if (m_nReaction < 0x0C) {
            speed = m_nReaction;
        } else {
            speed = m_nReaction / 5 + 0x0C;
        }
        int humanoidTypeId = getHumanoidTypeId();
        if (humanoidTypeId == 0x13 || humanoidTypeId == 0x15) {
            speed += 10;
        }

        m_nEncumbranceWeight = m_nEquippedWeight;
        if (inventory != null) {
            if (inventory.weight < 64_000) {
                m_nEncumbranceWeight = (short) (m_nEncumbranceWeight + inventory.weight / 2);
            } else {
                m_nEncumbranceWeight = 32_000;
            }
        }
        int signedMaxWeight = nativeSignedShort(m_nMaxWeight);
        int signedEncumbranceWeight = nativeSignedShort(m_nEncumbranceWeight);
        if (signedMaxWeight <= signedEncumbranceWeight) {
            speed -= signedEncumbranceWeight / signedMaxWeight;
            if (speed < 6) {
                speed = 6;
            }
        }

        int bodyDamageBonus = (int) (Math.pow(1.1d, m_nBody) / 20.0d);
        skillData.skillDamageType0And3Modifier = (byte) bodyDamageBonus;
        skillData.skillDamageType0And3Min = (byte) bodyDamageBonus;
        skillData.toHit = (short) ((Math.pow(1.1d, m_nBody) + Math.pow(1.1d, m_nReaction)) / 5.0d);

        copySkillSnapshotToCurrent();
        for (int skillIndex = FIRST_SKILL_INDEX; skillIndex < SKILL_INDEX_LIMIT; skillIndex++) {
            skillData.skillLevels[skillIndex] = clampBaseSkillLevel(skillData.skillLevels[skillIndex]);
        }
        applySkillModifierBonuses();

        int activeSkillIndex = Byte.toUnsignedInt(skillData.activeSkillIndex);
        if (activeSkillIndex != 0) {
            skillData.toHit += (short) (skillData.skillLevels[activeSkillIndex] * 3);
            skillData.skillDamageType0And3Min += (byte) (skillData.skillLevels[activeSkillIndex] / 5);
        }

        skillData.skillDamageType1Min = 0;
        skillData.skillDamageType1Modifier = 0;
        skillData.skillDamageType2Min = 0;
        skillData.skillDamageType2Modifier = 0;

        unitStatData.init();
        unitStatData.defence = (short) (m_nReaction / 3);
        for (int skillIndex = FIRST_SKILL_INDEX; skillIndex < SKILL_INDEX_LIMIT; skillIndex++) {
            unitStatData.protections[skillIndex] = (short) (m_nSpirit / 2);
        }
        mModifiers.addToUnit(this);

        m_nHP = Math.min(m_nHP, m_nMaxHP);
        m_nMP = Math.max(Math.min(m_nMP, m_nMaxMP), 0);
        if (owner != null) {
            m_wRegenStore = (owner.mpRegenPercent * m_nMaxMP) / 100;
        } else {
            m_wRegenStore = m_nMaxMP;
        }
        movementState.rotationSpeed = speed & 0xFF;

        if (unitStatData.defence < 0) {
            unitStatData.defence = 0;
        }
        if (unitStatData.absorbtion < 0) {
            unitStatData.absorbtion = 0;
        }
        if (nativeSignedShort(m_nEncumbranceWeight) < 0) {
            m_nEncumbranceWeight = 0;
        }
        for (int skillIndex = FIRST_SKILL_INDEX; skillIndex < SKILL_INDEX_LIMIT; skillIndex++) {
            int protectionCap = m_nSpirit / 2 + 0x46;
            unitStatData.protections[skillIndex] = (short) Math.max(
                    0,
                    Math.min(MAX_SKILL_LEVEL, Math.min(unitStatData.protections[skillIndex], protectionCap))
            );
        }

        if (spellbook != null) {
            spellbook.updatePrismaticCasterStats(this);
        }
    }

    /**
     * vtbl +0x64: Humanoid::updateSkills @005128CB.
     * Fully ported.
     */
    @Override
    public void updateSkills(int skillDeltaPermille, Unit maybeVictim, int updateFlags) {
        int humanoidTypeId = getHumanoidTypeId();
        if (humanoidTypeId <= 0x20 || humanoidTypeId >= 0x40
                || (maybeVictim != null && maybeVictim.suppressDeathLootFlag != 0)) {
            return;
        }

        int dirtyMask = 0;
        skillDeltaPermille = (int) (((m_nMind / 30.0d) + 0.25d) * skillDeltaPermille);
        if (maybeVictim != null) {
            int victimServerId = maybeVictim.serverID & 0xFFFF;
            for (int packedEntry : killHistoryEntries) {
                if ((packedEntry & 0xFFFF) != victimServerId) {
                    continue;
                }
                skillDeltaPermille = (skillDeltaPermille * 10) / ((packedEntry >>> 16) + 10);
                break;
            }
        }

        if (!shouldApplySkillProgress(maybeVictim)) {
            return;
        }

        copySkillSnapshotToCurrent();
        boolean leveledUp = false;
        if (!isMageClass()) {
            int activeSkillIndex = Byte.toUnsignedInt(skillData.activeSkillIndex);
            if (updateFlags == 0 && activeSkillIndex != 0 && skillData.skillLevels[activeSkillIndex] < MAX_SKILL_LEVEL) {
                if (activeSkillIndex != skillBonusesPermille.data[0] && activeSkillIndex != 5) {
                    skillDeltaPermille = divideBy8RoundedTowardZero(skillDeltaPermille);
                }
                skillDeltaPermille = clampAwardedSkillProgressPermille(
                        skillDeltaPermille,
                        skillData.skillLevels[activeSkillIndex]
                );
                skillBonusesPermille.data[activeSkillIndex] += skillDeltaPermille;
                skillsTotalBonusPermille += skillDeltaPermille;
                dirtyMask = UnitDirtyFlags.skillBonusSlot(activeSkillIndex).value;
                leveledUp = get10pctCompoundPermille(skillData.skillLevels[activeSkillIndex] + 1)
                        <= skillBonusesPermille.data[activeSkillIndex];
                if (leveledUp) {
                    copySkillSnapshotToCurrent();
                    skillData.skillLevels[activeSkillIndex] += 1;
                    copyCurrentSkillsToSnapshot();
                    if (owner.isActive == 0) {
                        notifySkillLevelUpPacket(owner, activeSkillIndex);
                    }
                }
            }
        } else if (updateFlags > 0 && skillData.skillLevels[updateFlags] < MAX_SKILL_LEVEL) {
            if (updateFlags != skillBonusesPermille.data[0] && updateFlags != 5) {
                skillDeltaPermille = divideBy8RoundedTowardZero(skillDeltaPermille);
            }
            skillDeltaPermille = clampAwardedSkillProgressPermille(skillDeltaPermille, skillData.skillLevels[updateFlags]);
            skillBonusesPermille.data[updateFlags] += skillDeltaPermille;
            skillsTotalBonusPermille += skillDeltaPermille;
            dirtyMask = UnitDirtyFlags.skillBonusSlot(updateFlags).value;
            leveledUp = get10pctCompoundPermille(skillData.skillLevels[updateFlags] + 1)
                    <= skillBonusesPermille.data[updateFlags];
            if (leveledUp) {
                copySkillSnapshotToCurrent();
                skillData.skillLevels[updateFlags] += 1;
                if (owner.isActive == 0) {
                    notifySkillLevelUpPacket(owner, updateFlags);
                }
                copyCurrentSkillsToSnapshot();
                spellbook.updatePrismaticCasterStats(this);
            }
        }

        if (leveledUp) {
            recalculateDerivedStats();
            emitNetUpdate(dirtyMask | UnitDirtyFlags.SKILL_LEVEL_UP.value);
        } else {
            if (skillDeltaPermille != 0) {
                emitNetUpdate(dirtyMask);
            }
            applySkillModifierBonuses();
        }

        if (skillDeltaPermille != 0 && Globals.gameServer.networkSessionActive != 0) {
            maybeAutosaveAfterSkillProgress();
        }
    }

    /**
     * Native support extracted from Inn::closeUnitSession @0052F8E2 accepted quest reward item hash `0xFFFE`.
     */
    public void applyInnTrainingReward(int rewardCount) {
        int rewardBudgetPermille = (rewardCount & 0xFFFF) * 0xFA;
        int carryPermille = 0;
        int level100Permille = get10pctCompoundPermille(100);
        for (int skillIndex = FIRST_SKILL_INDEX; skillIndex < SKILL_INDEX_LIMIT; skillIndex++) {
            if (skillIndex == skillBonusesPermille.data[0] || skillIndex == 5) {
                carryPermille += rewardBudgetPermille / 5;
            } else {
                carryPermille = (carryPermille >> 3) + rewardBudgetPermille / 0x28;
            }
            int currentPermille = skillBonusesPermille.data[skillIndex];
            if (currentPermille < level100Permille) {
                if (currentPermille + carryPermille < level100Permille) {
                    skillBonusesPermille.data[skillIndex] = currentPermille + carryPermille;
                    carryPermille = 0;
                    int skillLevel = skillLevelForBonusPermille(skillBonusesPermille.data[skillIndex]);
                    if (skillDataSnapshot.skillLevels[skillIndex] < skillLevel) {
                        CServerApp.sendGameEventNotification(
                                CServerApp.SKILL_IMPROVED_EVENT,
                                skillIndex,
                                owner
                        );
                        skillDataSnapshot.skillLevels[skillIndex] = (short) skillLevel;
                        int cappedSkillLevel = skillDataSnapshot.skillLevels[skillIndex] < 0x65
                                ? skillDataSnapshot.skillLevels[skillIndex]
                                : 100;
                        skillData.skillLevels[skillIndex] =
                                (short) (cappedSkillLevel + mModifiers.skillMods.skillLevels[skillIndex]);
                    }
                } else {
                    carryPermille -= level100Permille - currentPermille;
                    skillBonusesPermille.data[skillIndex] = level100Permille;
                }
            } else {
                skillBonusesPermille.data[skillIndex] = level100Permille;
            }
        }
        skillsTotalBonusPermille = 0;
        for (int skillIndex = FIRST_SKILL_INDEX; skillIndex < SKILL_INDEX_LIMIT; skillIndex++) {
            skillsTotalBonusPermille += skillBonusesPermille.data[skillIndex];
        }
    }

    /**
     * vtbl +0x68: Humanoid::awardKillSkillProgress @00512FCB.
     * Fully ported.
     */
    @Override
    public void awardKillSkillProgress(Unit defeatedUnit, int spellId) {
        int awardedSphere = 0;
        if (spellId != 0) {
            if (!isMageClass()) {
                return;
            }
            awardedSphere = Globals.staticDataMgr.spells.get(spellId).getAttribute(SpellColumn.SPHERE);
        }

        int skillDeltaPermille = (int) Math.floor(defeatedUnit.price * 0.5d);
        updateSkills(skillDeltaPermille, defeatedUnit, awardedSphere);

        boolean foundExistingEntry = false;
        for (int i = 0; i < killHistoryEntries.size(); i++) {
            int packedEntry = killHistoryEntries.get(i);
            if ((packedEntry & 0xFFFF) != (defeatedUnit.serverID & 0xFFFF)) {
                continue;
            }
            killHistoryEntries.set(i, packedEntry + 0x10000);
            foundExistingEntry = true;
            break;
        }
        if (foundExistingEntry) {
            return;
        }

        if (killHistoryEntries.size() > 4) {
            killHistoryEntries.remove(0);
        }
        killHistoryEntries.add(defeatedUnit.serverID & 0xFFFF);
    }

    /**
     * vtbl +0x6C: Humanoid::awardDamageSkillProgress @00513128.
     * Fully ported.
     */
    @Override
    public void awardDamageSkillProgress(Unit targetUnit, int damage, int spellId) {
        int awardedSphere = 0;
        if (spellId != 0) {
            if (!isMageClass()) {
                return;
            }
            awardedSphere = Globals.staticDataMgr.spells.get(spellId).getAttribute(SpellColumn.SPHERE);
        }

        if (targetUnit.owner == null || targetUnit.owner.isActive == 0) {
            return;
        }

        int skillDeltaPermille = (int) Math.floor((targetUnit.price * 0.5d * damage) / targetUnit.m_nMaxHP + 1.0d);
        updateSkills(skillDeltaPermille, targetUnit, awardedSphere);
    }

    /**
     * vtbl +0x70: Humanoid::awardSpellCastSkillProgress @005131EF.
     * Fully ported.
     */
    @Override
    public void awardSpellCastSkillProgress(Token targetToken, int spellId) {
        if (spellId == 0 || !isMageClass()) {
            return;
        }

        int awardedSphere = Globals.staticDataMgr.spells.get(spellId).getAttribute(SpellColumn.SPHERE);
        if (awardedSphere == 0) {
            return;
        }

        int manaCost = Globals.staticDataMgr.spells.get(spellId).getAttribute(SpellColumn.MANA_COST);
        int skillDeltaPermille = (int) Math.floor(manaCost * 0.5d + 0.5d);
        updateSkills(skillDeltaPermille, null, awardedSphere);
    }

    /**
     * vtbl +0x74: Humanoid::getDyingStateTimerTicks @00512526.
     * Fully ported.
     */
    @Override
    public int getDyingStateTimerTicks() {
        int dyingTicks = unitInfoLine.values.isEmpty()
                ? -1
                : unitInfoLine.getValue(HumanColumn.DYING_TIME.index);
        return dyingTicks == -1 ? 8 : dyingTicks;
    }

    /**
     * Native support extracted from Humanoid::prepareIncomingObject @00512573.
     */
    private boolean isEquippableInventoryItem(Item item) {
        return item instanceof Armor || item instanceof Weapon || item instanceof Shield;
    }

    /**
     * Native support extracted from Humanoid::prepareIncomingObject @00512573.
     */
    private boolean canEquipInventoryItem(Item item) {
        int suitableForFlags = item.worldItem.getAttribute(WorldItemColumn.SUITABLE_FOR);

        if ((suitableForFlags & 0x01) == 0 && !isMageClass()) {
            return false;
        }
        if ((suitableForFlags & 0x02) == 0 && isMageClass()) {
            return false;
        }
        return true;
    }

    /**
     * Native support extracted from Humanoid::copyHumanoidCloneState @005123C6 item-slot copy.
     */
    private static Item copyOutpostEquipmentClone(Item item) {
        return item == null ? null : item.copyItemVirtual();
    }

    /**
     * Native support helper for Humanoid virtuals that branch on Token.typeID.
     * not ported.
     */
    private int getHumanoidTypeId() {
        return typeID & 0xFFFF;
    }

    /**
     * Native support extracted from Humanoid::updateSkills @005128CB relation gate.
     * Fully ported.
     */
    private boolean shouldApplySkillProgress(Unit maybeVictim) {
        if (maybeVictim == null || maybeVictim.isDying()) {
            return true;
        }
        if (owner == null || maybeVictim.owner == owner) {
            return false;
        }
        if (Globals.gameServer.missionScriptRuntime == null) {
            return true;
        }
        return !Globals.gameServer.missionScriptRuntime.hasRelationFlag(
                maybeVictim.owner,
                owner,
                PLAYER_RELATION_ALLIED_MASK
        );
    }

    /**
     * Native support extracted from Humanoid::updateSkills @005128CB signed divide-by-eight helper.
     * Fully ported.
     */
    private static int divideBy8RoundedTowardZero(int value) {
        return (value + 7 + (((value + 7) >> 31) & 7)) >> 3;
    }

    /**
     * Native support extracted from Humanoid::recalculateDerivedStats @0051366B and
     * Humanoid::updateSkills @005128CB skill snapshot restore loops.
     * Fully ported.
     */
    private void copySkillSnapshotToCurrent() {
        for (int skillIndex = FIRST_SKILL_INDEX; skillIndex < SKILL_INDEX_LIMIT; skillIndex++) {
            skillData.skillLevels[skillIndex] = skillDataSnapshot.skillLevels[skillIndex];
        }
    }

    /**
     * Native support extracted from Humanoid::updateSkills @005128CB skill snapshot update loops.
     * Fully ported.
     */
    private void copyCurrentSkillsToSnapshot() {
        for (int skillIndex = FIRST_SKILL_INDEX; skillIndex < SKILL_INDEX_LIMIT; skillIndex++) {
            skillDataSnapshot.skillLevels[skillIndex] = skillData.skillLevels[skillIndex];
        }
    }

    /**
     * Native support extracted from Humanoid::recalculateDerivedStats @0051366B and
     * Humanoid::updateSkills @005128CB modifier application loops.
     * Fully ported.
     */
    private void applySkillModifierBonuses() {
        for (int skillIndex = FIRST_SKILL_INDEX; skillIndex < SKILL_INDEX_LIMIT; skillIndex++) {
            short baseSkillLevel = skillData.skillLevels[skillIndex];
            short upperCappedSkillLevel = baseSkillLevel < 0x65 ? baseSkillLevel : 100;
            skillData.skillLevels[skillIndex] =
                    (short) (upperCappedSkillLevel + mModifiers.skillMods.skillLevels[skillIndex]);
        }
    }

    /**
     * Native support extracted from Humanoid::recalculateDerivedStats @0051366B base skill clamp loop.
     * Fully ported.
     */
    private static short clampBaseSkillLevel(short skillLevel) {
        if (skillLevel < 0) {
            return 0;
        }
        return (short) Math.min(skillLevel, MAX_SKILL_LEVEL);
    }

    /**
     * Native support helper for Humanoid::updateSkills @005128CB and
     * CServerApp::sendGameEventNotification @005052D2.
     * Fully ported.
     */
    private void notifySkillLevelUpPacket(Player ownerPlayer, int skillIndex) {
        CServerApp.sendGameEventNotification(
                CServerApp.SKILL_IMPROVED_EVENT,
                skillIndex | (serverID << 16),
                ownerPlayer
        );
    }

    /**
     * Native support helper for Humanoid::updateSkills @005128CB.
     * Fully ported.
     */
    private void maybeAutosaveAfterSkillProgress() {
        int elapsed = Globals.currentTickMillis() - owner.lastSaveTick;
        if (Integer.compareUnsigned(elapsed, CONTROLLED_HUMANOID_SAVE_INTERVAL_MS) > 0) {
            Globals.gameServer.saveControlledHumanoid(this);
        }
    }

    /**
     * vtbl +0x08: Humanoid::serialize @0052D7C8.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        super.serialize(ar); // Unit::Serialize

        ar.serialize(skillBonusesPermille);

        if (!ar.isStoring()) {
            for (int i = 0; i < ITEMS_COUNT; i++) {
                items[i] = Item.readFromArchive(ar);
            }
        } else {
            for (int i = 0; i < ITEMS_COUNT; i++) {
                ar.writeObject(items[i]);
            }
        }
    }
}
