package ua.millfreedom.rom2.model.unit.humanoid.human;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.Armor;
import ua.millfreedom.rom2.model.Item;
import ua.millfreedom.rom2.model.ItemAssortmentGenerator;
import ua.millfreedom.rom2.model.SequentialArrayWalker;
import ua.millfreedom.rom2.model.Shield;
import ua.millfreedom.rom2.model.Weapon;
import ua.millfreedom.rom2.model.column.HumanColumn;
import ua.millfreedom.rom2.model.spell.Spell;
import ua.millfreedom.rom2.model.spell.Spellbook;
import ua.millfreedom.rom2.model.unit.humanoid.Humanoid;
import ua.millfreedom.rom2.CStaticDataMgr;

import java.io.IOException;

public class Human extends Humanoid {
    private static final String DEFAULT_TEMPLATE_NAME = "Man_Unarmed";
    private static final String[] HERO_MALE_NAMES = {
            "",
            "Biker",
            "Vasya",
            "Duke Killer",
            "Samuil",
            "Danath",
            "Legolas",
            "Fat Sam",
            "Ragnar",
            "Sir Gerald"
    };
    private static final String[] HERO_FEMALE_NAMES = {
            "",
            "Lady Moro",
            "Reniesta",
            "Scilla",
            "Red Hat",
            "Alice",
            "Khisanth"
    };

    /**
     * Native: Human::initializeDefaultTemplate @00514377.
     * Fully ported.
     */
    public Human initializeDefaultTemplate() {
        initializeFromTemplate(DEFAULT_TEMPLATE_NAME, false, false);
        return this;
    }

    /**
     * Native: Human::Human @005143E4.
     * Fully ported Java factory analogue.
     */
    public static Human createFromTemplate(
            String templateName,
            boolean assignTypeFromSexAndClass,
            boolean skipTemplateEquipment
    ) {
        Human human = new Human();
        human.initializeFromTemplate(templateName, assignTypeFromSexAndClass, skipTemplateEquipment);
        return human;
    }

    /**
     * Native: Human::Serialize @0052D87B.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        super.serialize(ar); // Humanoid::Serialize

        if (!ar.isStoring()) {
            int tableIndex = ((typeID & 0xFFFF) < 0x21) ? (key & 0xFFFF) : 5;
            unitInfoLine = Globals.staticDataMgr.humans.get(tableIndex);
        }
    }

    /**
     * Native support extracted from MapDescriptor::MapDescriptor @004A449C humanoid scenario-unit branch.
     */
    private static HumanInfo getHumanInfoAt(int index) {
        if (index < 0 || index >= Globals.staticDataMgr.humans.size()) {
            return null;
        }
        return Globals.staticDataMgr.humans.get(index);
    }

    /**
     * Native: Human::ApplyHumanInfoValues @00514CF2.
     * Fully ported.
     */
    private void applyHumanInfoValues(HumanInfo info) {
        SequentialArrayWalker values = new SequentialArrayWalker(info.values);
        m_nBody = values.nextShort(m_nBody);
        m_nReaction = values.nextShort(m_nReaction);
        m_nMind = values.nextShort(m_nMind);
        m_nSpirit = values.nextShort(m_nSpirit);
        m_nMaxHP = values.nextShort(m_nMaxHP);
        m_nHP = m_nMaxHP;
        m_nMaxMP = values.nextShort(m_nMaxMP);
        m_nMP = m_nMaxMP;
        speed = values.nextShort(speed);
        movementState.rotationSpeed = values.nextByte(movementState.rotationSpeed);
        sightRange = values.nextByte(sightRange);
        unitStatData.defence = (short) values.nextShort(unitStatData.defence);

        for (int skillOffset = 0; skillOffset < 6; skillOffset++) {
            int currentSkillLevel = skillData.skillLevels[skillOffset];
            skillData.skillLevels[skillOffset] = (short) values.nextShort(currentSkillLevel);
        }
        skillData.toHit = 0;

        int highestBaseSkillIndex = 0;
        int highestBaseSkillLevel = 0;
        for (int skillIndex = 1; skillIndex < 6; skillIndex++) {
            skillDataSnapshot.skillLevels[skillIndex] = skillData.skillLevels[skillIndex];
            if (highestBaseSkillLevel < skillData.skillLevels[skillIndex]) {
                highestBaseSkillLevel = skillData.skillLevels[skillIndex];
                highestBaseSkillIndex = skillIndex;
            }
        }
        if (usesPreferredSkillFromTemplateName(info)) {
            skillBonusesPermille.data[0] = highestBaseSkillIndex;
        }

        typeID = values.nextUnsignedShort(typeID & 0xFFFF) & 0xFFFF;
        face = values.nextByte(face);
        // Native reads IsFemale into a local here; sex handling runs on the scenario path.
        values.nextInt(0);
        attackChargeTicks = values.nextByte(attackChargeTicks);
        attackRelaxTicks = values.nextByte(attackRelaxTicks);
        tokenSize = values.nextByte(tokenSize);
        movementType = values.nextByte(movementType);
    }

    /**
     * Native support extracted from MapDescriptor::MapDescriptor @004A449C humanoid scenario-unit branch.
     */
    public Human applyScenarioHumanInfo(int humanInfoIndex) {
        HumanInfo info = getHumanInfoAt(humanInfoIndex);
        if (info == null) {
            throw new IllegalStateException("Missing scenario human info at index " + humanInfoIndex);
        }
        key = (humanInfoIndex) & 0xFFFF;
        unitInfoLine = info;
        applyHumanInfoValues(info);
        if (info.values.size() > HumanColumn.SERVER_ID.index) {
            serverID = getUnsignedHumanInfoShort(info, HumanColumn.SERVER_ID);
        }
        if (serverID > 10_000) {
            serverID = (serverID / 10) % 1000;
        }
        if (getSignedHumanInfoValue(info, HumanColumn.IS_FEMALE) != 0) {
            face |= 0x80;
        }
        recalcSkillBonuses();
        recalculateDerivedStats();
        if (m_nMaxMP > 0) {
            status |= 0x04;
        }
        return this;
    }

    /**
     * Native support extracted from MapDescriptor::MapDescriptor @004A449C humanoid equipment loop.
     */
    public void addScenarioEquipment(HumanInfo info) {
        for (int slotIndex = 0; slotIndex < info.equipment.length; slotIndex++) {
            String equipmentName = info.equipment[slotIndex];
            if (equipmentName == null || equipmentName.isEmpty()) {
                continue;
            }
            Item equipment = createHumanEquipment(slotIndex, equipmentName);
            if (equipment != null) {
                addIncomingObjectToInventory(equipment);
            }
        }
    }

    /**
     * Native support extracted from Human::ApplyHumanInfoValues @00514CF2 signed table-value reads.
     */
    private static int getSignedHumanInfoValue(HumanInfo info, HumanColumn column) {
        return info.values.get(column.index);
    }

    /**
     * Native support extracted from Human::ApplyHumanInfoValues @00514CF2 unsigned byte table-value reads.
     */
    private static int getUnsignedHumanInfoByte(HumanInfo info, HumanColumn column) {
        return getSignedHumanInfoValue(info, column) & 0xFF;
    }

    /**
     * Native support extracted from Human::ApplyHumanInfoValues @00514CF2 unsigned word table-value reads.
     */
    private static int getUnsignedHumanInfoShort(HumanInfo info, HumanColumn column) {
        return getSignedHumanInfoValue(info, column) & 0xFFFF;
    }

    /**
     * Native support extracted from Human::ApplyHumanInfoValues @00514CF2 preferred-skill template check.
     */
    private static boolean usesPreferredSkillFromTemplateName(HumanInfo info) {
        return info.name.contains("_Hero") || info.name.contains("Start_");
    }

    /**
     * Native: Human::InitializeFromTemplate @005145D1.
     * Fully ported.
     */
    public void initializeFromTemplate(String templateName, boolean assignTypeFromSexAndClass, boolean skipTemplateEquipment) {
        typeID = 0;

        boolean shouldApplyTemplateEquipment = !skipTemplateEquipment;
        int heroFaceId = -1;
        int isFemale = 1;
        String normalizedTemplateName = templateName;
        int dotPosition = normalizedTemplateName.lastIndexOf('.');
        if (dotPosition != -1 && dotPosition + 1 < normalizedTemplateName.length()) {
            heroFaceId = parseTemplateFaceId(normalizedTemplateName, dotPosition);
            isFemale = normalizedTemplateName.charAt(dotPosition + 1) == 'f' ? 1 : 0;
            normalizedTemplateName = normalizedTemplateName.substring(0, dotPosition);
        }

        if (normalizedTemplateName.startsWith("Hero")) {
            assignTypeFromSexAndClass = true;
            shouldApplyTemplateEquipment = false;
            normalizedTemplateName = normalizedTemplateName.length() > 5 ? normalizedTemplateName.substring(5) : "";
            if (!normalizedTemplateName.startsWith("Man")) {
                normalizedTemplateName = "Man_" + normalizedTemplateName;
            }
            assignHeroDisplayName(isFemale, heroFaceId);
        }

        HumanInfo info = null;
        int humanIndex = 0;
        for (int index = 1; index < Globals.staticDataMgr.humans.size(); index++) {
            HumanInfo candidate = Globals.staticDataMgr.humans.get(index);
            if (candidate == null || candidate.name == null || !candidate.name.equals(normalizedTemplateName)) {
                continue;
            }
            humanIndex = index;
            info = candidate;
            break;
        }

        if (info != null) {
            key = (humanIndex) & 0xFFFF;
            unitInfoLine = info;
            applyHumanInfoValues(info);

            int templateFemaleFlag = getSignedHumanInfoValue(info, HumanColumn.IS_FEMALE);
            if (templateFemaleFlag != -1) {
                isFemale = templateFemaleFlag;
            }

            if (info.values != null && info.values.size() > HumanColumn.SERVER_ID.index) {
                serverID = getUnsignedHumanInfoShort(info, HumanColumn.SERVER_ID);
                if (serverID > 10_000) {
                    serverID = (serverID / 10) % 1000;
                }
            }
            if (m_nMaxMP > 0) {
                status |= 0x06;
            }
            if (shouldApplyTemplateEquipment) {
                for (int slotIndex = 0; slotIndex < info.equipment.length; slotIndex++) {
                    String equipmentName = info.equipment[slotIndex];
                    if (equipmentName == null || equipmentName.isEmpty()) {
                        continue;
                    }
                    Item equipment = createHumanEquipment(slotIndex, equipmentName);
                    if (equipment != null) {
                        addIncomingObjectToInventory(equipment);
                    }
                }
            }
        }

        if (m_nMaxMP > 0) {
            status |= 0x06;
            spellbook = new Spellbook();
            HumanInfo spellSourceInfo = info != null
                    ? info
                    : unitInfoLine instanceof HumanInfo humanInfo ? humanInfo : null;
            int knownSpellsBitset = getSignedHumanInfoValue(spellSourceInfo, HumanColumn.KNOWN_SPELLS);
            for (int spellIndex = 1; spellIndex < CStaticDataMgr.SPELL_LIMIT; spellIndex++) {
                if ((knownSpellsBitset & (1 << spellIndex)) != 0) {
                    spellbook.setAt(spellIndex, new Spell((byte) spellIndex));
                }
            }
        }

        if (heroFaceId > 0) {
            face = heroFaceId & 0xFF;
        }
        if (!assignTypeFromSexAndClass) {
            face |= (isFemale << 7) & 0xFF;
        } else if (!isMageClass()) {
            typeID = (isFemale + 0x21) & 0xFFFF;
        } else {
            typeID = (isFemale + 0x23) & 0xFFFF;
        }

        recalcSkillBonuses();
        recalculateDerivedStats();
        m_nHP = m_nMaxHP;
        m_nMP = m_nMaxMP;
        refreshPrice();

        if ((typeID & 0xFFFF) == 0) {
            Globals.gameServer.pushMessage("Invalid human " + normalizedTemplateName + " created");
        }
    }

    /**
     * Native: Human::ConfigureStartingLoadout @00514F59.
     * Fully ported.
     */
    public void configureStartingLoadout(int primarySkillIndex, int primarySkillLevel) {
        int skillIndex = primarySkillIndex & 0xFF;
        int skillLevel = primarySkillLevel & 0xFF;

        releaseIncomingObject(pWeapon);

        for (int index = 1; index < 6; index++) {
            skillData.skillLevels[index] = 0;
        }
        skillData.skillLevels[skillIndex] = (short) skillLevel;
        skillData.skillLevels[5] = (short) (skillLevel / 2);
        recalcSkillBonuses();
        skillBonusesPermille.data[0] = skillIndex;

        Weapon starterWeapon = isMageClass()
                ? createStartingCasterWeapon(skillIndex)
                : createStartingFighterWeapon(skillIndex);
        addIncomingObjectToInventory(starterWeapon);
    }

    /**
     * Native support extracted from Human::ConfigureStartingLoadout @00514F59.
     */
    private Weapon createStartingFighterWeapon(int primarySkillIndex) {
        return switch (primarySkillIndex) {
            case 1 -> Weapon.createByServiceName("Iron Long Sword");
            case 2 -> Weapon.createByServiceName("Iron Axe");
            case 3 -> Weapon.createByServiceName("Iron Mace");
            case 4 -> Weapon.createByServiceName("Iron Pike");
            case 5 -> Weapon.createByServiceName("Uncommon Wood Long Bow");
            default -> null;
        };
    }

    /**
     * Native support extracted from Human::ConfigureStartingLoadout @00514F59.
     */
    private Weapon createStartingCasterWeapon(int primarySkillIndex) {
        String spellName = switch (primarySkillIndex) {
            case 1 -> "Fire_Arrow";
            case 2 -> "Ice_Missile";
            case 3 -> "Lightning";
            case 4 -> "Diamond_Dust";
            case 5 -> "Drain_Life";
            default -> "";
        };

        String weaponName = isFemale() ? "Wood Staff" : "Uncommon Wood Staff";
        return Weapon.createByServiceName(weaponName + " {castSpell=" + spellName + ":20}");
    }

    /**
     * Native support extracted from Human::InitializeFromTemplate @005145D1 and GetInt @00584400.
     */
    private static int parseTemplateFaceId(String templateName, int dotPosition) {
        int faceStart = dotPosition + 2;
        if (faceStart >= templateName.length()) {
            return 0;
        }

        int sign = 1;
        int index = faceStart;
        char first = templateName.charAt(index);
        if (first == '-') {
            sign = -1;
            index++;
        } else if (first == '+') {
            index++;
        }

        int value = 0;
        boolean foundDigit = false;
        while (index < templateName.length()) {
            char ch = templateName.charAt(index);
            if (ch < '0' || ch > '9') {
                break;
            }
            value = value * 10 + (ch - '0');
            foundDigit = true;
            index++;
        }
        return foundDigit ? value * sign : 0;
    }

    /**
     * Native support extracted from Human::InitializeFromTemplate @005145D1.
     */
    private void assignHeroDisplayName(int isFemale, int heroFaceId) {
        str = "Unknown";
        if (isFemale == 0) {
            if (heroFaceId >= 0 && heroFaceId < HERO_MALE_NAMES.length) {
                str = HERO_MALE_NAMES[heroFaceId];
            }
            return;
        }
        if (heroFaceId >= 0 && heroFaceId < HERO_FEMALE_NAMES.length) {
            str = HERO_FEMALE_NAMES[heroFaceId];
        }
    }

    /**
     * Native support extracted from Human::InitializeFromTemplate @005145D1.
     */
    private static Item createHumanEquipment(int slotIndex, String equipmentName) {
        if (slotIndex == 0) {
            return Weapon.createByServiceName(equipmentName);
        }
        if (slotIndex == 1) {
            return Shield.createByServiceName(equipmentName);
        }
        return Armor.createByServiceName(equipmentName);
    }

    /**
     * vtbl +0x60: Human::generateRandomInventoryItem @00515575.
     * Fully ported.
     */
    @Override
    public void generateRandomInventoryItem() {
        int minimumPrice = price <= 0x3E8 ? 0 : price / 10;
        Item item = ItemAssortmentGenerator.createRandomItemForPriceRange(minimumPrice, price);
        inventory.addItem(item);
    }
}
