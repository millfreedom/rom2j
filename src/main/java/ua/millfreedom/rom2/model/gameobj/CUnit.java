package ua.millfreedom.rom2.model.gameobj;

import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CBmp256;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CGameBitmap;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.CPlayer;
import ua.millfreedom.rom2.model.CProjectileInfo;
import ua.millfreedom.rom2.model.CSprite256;
import ua.millfreedom.rom2.model.CUnitInfo;
import ua.millfreedom.rom2.model.CMousePointer;
import ua.millfreedom.rom2.model.GraphicsUnitsFile;
import ua.millfreedom.rom2.model.Projectiles;
import ua.millfreedom.rom2.model.TokenEntry;
import ua.millfreedom.rom2.model.UnitRenderState;
import ua.millfreedom.rom2.model.UnitTypes;
import ua.millfreedom.rom2.model.action.ItemListAction;
import ua.millfreedom.rom2.model.color.RGB16;
import ua.millfreedom.rom2.model.color.RGB32;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.palette.CGamePalette;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palette256;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.render.Rle8SpriteDecoder;
import ua.millfreedom.rom2.model.quest.Quest;
import ua.millfreedom.rom2.model.sound.Sound;
import ua.millfreedom.rom2.model.sound.SoundManager;
import ua.millfreedom.rom2.model.sound.SoundPack;
import ua.millfreedom.rom2.model.sound.SoundSystem;
import ua.millfreedom.rom2.model.spell.EffectVisualBuilder;
import ua.millfreedom.rom2.model.spell.Spell;
import ua.millfreedom.rom2.model.spell.VisualElem;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.unit.humanoid.Humanoid;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.res.Resources;
import ua.millfreedom.rom2.text.HeroPictureText;
import ua.millfreedom.rom2.text.SpellText;
import ua.millfreedom.rom2.text.UnitNameText;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ua.millfreedom.rom2.model.color.Consts.MIDGRAY565;
import static ua.millfreedom.rom2.res.Constants.GRAPHICS;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.*;
import static ua.millfreedom.rom2.text.TextTableId.HEROPICTURE;
import static ua.millfreedom.rom2.text.TextTableId.SPELL;
import static ua.millfreedom.rom2.text.TextTableId.UNITNAME;

public class CUnit extends CGameObject {
    private static final int UNIT_FLAG_HUMANOID = 0x01;
    private static final int UNIT_FLAG_MAGIC_CLASS = 0x02;
    private static final int UNIT_FLAG_DYNAMIC_INFO_PICTURE_MASK = 0x11;
    private static final int DIRTY_RECT_TILE_ALIGNMENT = 0xFFFFFFE0;
    private static final int DIRTY_RECT_TILE_SIZE = 0x20;

    //0x144
    public byte body;

    //0x145
    public byte mind;

    //0x146
    public byte spirit;

    //0x147
    public byte reaction;

    //0x148
    public short MP;

    //0x14A
    public short MaxMP;

    //0x14C
    public short field_0x14c;

    //0x14E
    public byte absorption;

    //0x150
    public short defence;

    //0x152
    public short attackSkill;

    //0x154
    public byte physicalDamageMin;

    //0x155
    public byte physicalDamageSpread;

    //0x156
    public byte armorPiercingSkillIndex;

    //0x157
    public final byte[] sphereLevels = new byte[5];

    //0x15C
    public final byte[] protectionLevels = new byte[5];

    //0x161
    public byte field_0x161;

    //0x162
    public byte field_0x162;

    //0x164
    public short field_0x164;

    //0x16C
    public final int[] copiedSkillBonusesPermille = new int[5];

    //0x180
    public byte copiedBody;

    //0x181
    public byte copiedReaction;

    //0x182
    public byte copiedMind;

    //0x183
    public byte copiedSpirit;

    //0x184
    public byte field51_0x184;

    //0x188
    public final TokenEntry[] equipmentTokenEntries = new TokenEntry[12];

    //0x1B8
    public int unitFlags;

    //0x1BC
    public int lastVoicePlaybackTick;

    //0x1C0
    public CSprite256 pCSprite256;

    //0x1C4
    public CSprite256 pCSprite256_2;

    //0x1C8
    public String cachedHeroSpritePictureName = "";

    //0x1D8
    public int cachedHeroSpriteArmorMaterial = -1;

    //0x1DC
    public short serverID;

    //0x1E0
    public int questFlags;

    /**
     * Native: CUnit::CUnit @00462A4A.
     * Fully ported. Java field defaults cover the native zeroed fields; the constructor sets the nonzero quest default.
     */
    public CUnit() {
        questFlags = -1;
    }

    /**
     * Native: CUnit::CUnit @00462B43.
     * Fully ported. Native delegates to CGameObject copy construction, copies the unit stat/protection bytes,
     * deep-copies equipment tokens and cached sprites, and copies hero sprite/server/quest cache fields.
     */
    public CUnit(CUnit source) {
        super(source);
        body = source.body;
        mind = source.mind;
        spirit = source.spirit;
        reaction = source.reaction;
        MP = source.MP;
        MaxMP = source.MaxMP;
        field_0x14c = source.field_0x14c;
        absorption = source.absorption;
        defence = source.defence;
        attackSkill = source.attackSkill;
        physicalDamageMin = source.physicalDamageMin;
        physicalDamageSpread = source.physicalDamageSpread;
        field_0x164 = source.field_0x164;
        field_0x161 = source.field_0x161;
        field_0x162 = source.field_0x162;
        armorPiercingSkillIndex = source.armorPiercingSkillIndex;
        System.arraycopy(source.sphereLevels, 0, sphereLevels, 0, sphereLevels.length);
        System.arraycopy(source.protectionLevels, 0, protectionLevels, 0, protectionLevels.length);
        for (int i = 0; i < equipmentTokenEntries.length; i++) {
            if (source.equipmentTokenEntries[i] == null) {
                equipmentTokenEntries[i] = null;
            } else {
                equipmentTokenEntries[i] = new TokenEntry(source.equipmentTokenEntries[i]);
            }
        }
        field51_0x184 = source.field51_0x184;
        unitFlags = source.unitFlags;
        lastVoicePlaybackTick = source.lastVoicePlaybackTick;
        if (source.pCSprite256 == null) {
            pCSprite256 = null;
        } else {
            pCSprite256 = new CSprite256(source.pCSprite256);
        }
        if (source.pCSprite256_2 == null) {
            pCSprite256_2 = null;
        } else {
            pCSprite256_2 = new CSprite256(source.pCSprite256_2);
        }
        cachedHeroSpritePictureName = source.cachedHeroSpritePictureName;
        cachedHeroSpriteArmorMaterial = source.cachedHeroSpriteArmorMaterial;
        serverID = source.serverID;
        questFlags = source.questFlags;
    }

    /**
     * Native: CUnit::CreateObject @0041D8BB.
     * Fully ported.
     */
    public static CUnit createObject(int serverId) {
        CUnit unit = new CUnit();
        unit.unitFlags = 0x56;
        unit.type = 1;
        unit.field8_0x28 = serverId - 0x50;
        unit.serverID = (short) serverId;
        return unit;
    }

    /**
     * Native: CUnit::CopyFromRuntimeUnit @004699D2.
     * Fully ported. Java mirrors the native visual stat copy, humanoid/non-humanoid skill snapshots, protection copy,
     * armor-piercing marker, and ItemListAction::global equipment payload stream materialization.
     */
    public void copyFromRuntimeUnit(Unit source) {
        type = source.getTokenTypeId();
        applyScenarioFace(source.face);

        body = (byte) source.m_nBody;
        mind = (byte) source.m_nMind;
        reaction = (byte) source.m_nReaction;
        spirit = (byte) source.m_nSpirit;
        HP = (short) source.m_nHP;
        MaxHP = (short) source.m_nMaxHP;
        MP = (short) source.m_nMP;
        MaxMP = (short) source.m_nMaxMP;
        attackSkill = source.skillData.toHit;
        defence = source.unitStatData.defence;
        absorption = (byte) source.unitStatData.absorbtion;
        physicalDamageMin = source.skillData.skillDamageType0And3Min;
        physicalDamageSpread = source.skillData.skillDamageType0And3Modifier;
        speed = (short) source.speed;
        packedSightRange = (short) ((source.sightFraction & 0xFF) | ((source.sightRange & 0xFF) << 8));
        serverID = (short) source.serverID;
        copiedEncumbranceWeight = (short) source.m_nEncumbranceWeight;

        if (source.isHumanoidToken() == 0) {
            copyMonsterSphereLevels(source);
        } else {
            copyHumanoidVisualState((Humanoid) source);
        }
        copyProtectionLevels(source);
        armorPiercingSkillIndex = (type == 0x49 || type == 0x68 || type == 0x46) ? (byte) 2 : 0;
        refreshUnitSpritesAfterRuntimeCopy();
    }

    /**
     * Native: CUnit::ApplyScenarioFace @0046A0CD.
     * Fully ported.
     */
    public void applyScenarioFace(int face) {
        if (type < 0x40 && type > 0x1F) {
            field8_0x28 = face;
            unitFlags = (unitFlags & 0x80) | 0x09;
            type -= 0x21;
            if ((type & 0x01) != 0) {
                unitFlags |= 0x04;
            }
            if ((type & 0x02) != 0) {
                unitFlags |= 0x02;
            }
            type = 1;
            return;
        }
        if (type < 0x1A) {
            field8_0x28 = face;
            unitFlags = (unitFlags & 0x80) | 0x18;
            if ((field8_0x28 & 0x80) != 0) {
                unitFlags |= 0x04;
            }
            field8_0x28 &= 0x7F;
            if (type > 0x16 && type < 0x19) {
                unitFlags |= 0x02;
            }
            return;
        }
        field8_0x28 = face;
    }

    /**
     * Native: CUnit::IsSelectableForShopEntry @0046A9C2.
     * Fully ported.
     */
    public boolean isSelectableForShopEntry(TokenEntry entry) {
        boolean magicUnit = (unitFlags & UNIT_FLAG_MAGIC_CLASS) != 0;
        return magicUnit
                ? (entry.wireFlags & TokenEntry.FLAG_EQUIPPABLE_BY_MAGIC_UNIT) != 0
                : (entry.wireFlags & TokenEntry.FLAG_EQUIPPABLE_BY_NON_MAGIC_UNIT) != 0;
    }

    /**
     * Native support extracted from CUnit::CopyFromRuntimeUnit @004699D2 non-humanoid branch.
     */
    private void copyMonsterSphereLevels(Unit source) {
        for (int i = 1; i < 6; i++) {
            sphereLevels[i - 1] = source.unitStatData.m_bModifiers[i];
        }
    }

    /**
     * Native support extracted from CUnit::CopyFromRuntimeUnit @004699D2 humanoid branch.
     */
    private void copyHumanoidVisualState(Humanoid source) {
        for (int i = 1; i < 6; i++) {
            sphereLevels[i - 1] = (byte) source.skillData.skillLevels[i];
        }
        experience = 0;
        for (int i = 1; i < 6; i++) {
            int bonusPermille = source.skillBonusesPermille.data[i];
            copiedSkillBonusesPermille[i - 1] = bonusPermille;
            experience += bonusPermille;
        }
        Arrays.fill(equipmentTokenEntries, null);
        ItemListAction equipmentSnapshotAction = ItemListAction.prepareForRuntimeUnitEquipmentSnapshot(source);
        ByteBuffer itemPayload = ByteBuffer.wrap(equipmentSnapshotAction.trailingData.get()).order(ByteOrder.LITTLE_ENDIAN);
        for (int slotIndex = 0; slotIndex < equipmentTokenEntries.length; slotIndex++) {
            equipmentTokenEntries[slotIndex] = readRuntimeEquipmentTokenEntry(itemPayload);
        }
    }

    /**
     * Native support extracted from CUnit::CopyFromRuntimeUnit @004699D2 TokenEntry::TokenEntryFromItemPayload loop.
     */
    private static TokenEntry readRuntimeEquipmentTokenEntry(ByteBuffer itemPayload) {
        TokenEntry entry = new TokenEntry(itemPayload, false);
        if (entry.packedTokenHash == 0) {
            return null;
        }
        entry.gridModeCode = 1;
        return entry;
    }

    /**
     * Native support extracted from CUnit::CopyFromRuntimeUnit @004699D2 protection copy to CUnit +0x15C..0x160.
     */
    private void copyProtectionLevels(Unit source) {
        for (int i = 1; i < 6; i++) {
            protectionLevels[i - 1] = (byte) source.unitStatData.protections[i];
        }
    }

    /**
     * Native: CUnit::RefreshUnitSpritesAfterRuntimeCopy @0046A215.
     * Fully ported. Java object replacement covers the native sprite destructor cleanup path.
     */
    public void refreshUnitSpritesAfterRuntimeCopy() {
        if ((unitFlags & UNIT_FLAG_HUMANOID) == 0) {
            return;
        }

        int previousType = type;
        String pictureName = resolveHeroSpritePictureName();
        if (!needsHeroSpriteRefresh(pictureName)) {
            return;
        }

        String spriteDirectory = resolveHeroSpriteDirectory();
        pCSprite256 = new CSprite256(Resources.path(GRAPHICS, "units", spriteDirectory, pictureName, "sprites.256"));
        pCSprite256_2 = new CSprite256(Resources.path(GRAPHICS, "units", spriteDirectory, pictureName, "spritesb.256"));
        cachedHeroSpritePictureName = pictureName;
        applyHeroSpriteTypeAlias(pictureName);
        realignActiveAttackAnimationAfterTypeChange(previousType);
    }

    /**
     * Native support extracted from CUnit::RefreshUnitSpritesAfterRuntimeCopy @0046A215 hero-picture selection.
     */
    private String resolveHeroSpritePictureName() {
        String pictureName;
        if (action == 6) {
            pictureName = "unarmed";
            if ((unitFlags & UNIT_FLAG_MAGIC_CLASS) != 0) {
                pictureName = "mage_st";
            }
        } else {
            int pictureIndex = equipmentTokenEntries[0] == null ? 0 : equipmentTokenEntries[0].getId() - 1;
            pictureName = get(HEROPICTURE, HeroPictureText.byIndex(pictureIndex));
            if (equipmentTokenEntries[1] != null) {
                pictureName += "_";
            }
            if ((unitFlags & UNIT_FLAG_MAGIC_CLASS) != 0 && pictureName.equals("unarmed")) {
                pictureName = "mage";
            }
        }
        return pictureName;
    }

    /**
     * Native support extracted from CUnit::RefreshUnitSpritesAfterRuntimeCopy @0046A215 cached picture/material gate.
     */
    private boolean needsHeroSpriteRefresh(String pictureName) {
        if (!cachedHeroSpritePictureName.equals(pictureName)) {
            return true;
        }
        if ((unitFlags & UNIT_FLAG_MAGIC_CLASS) != 0) {
            return false;
        }
        if (equipmentTokenEntries[7] == null) {
            return cachedHeroSpriteArmorMaterial != -1;
        }
        return cachedHeroSpriteArmorMaterial == -1;
    }

    /**
     * Native support extracted from CUnit::RefreshUnitSpritesAfterRuntimeCopy @0046A215 armor-material sprite path routing.
     */
    private String resolveHeroSpriteDirectory() {
        if ((unitFlags & UNIT_FLAG_MAGIC_CLASS) != 0) {
            return "heroes";
        }
        TokenEntry armor = equipmentTokenEntries[7];
        if (armor == null) {
            cachedHeroSpriteArmorMaterial = -1;
            return "heroes_l";
        }
        cachedHeroSpriteArmorMaterial = armor.getMaterial();
        return Globals.materialRuntimeData.unitMaterialSpritePaths.get(cachedHeroSpriteArmorMaterial);
    }

    /**
     * Native support extracted from CUnit::RefreshUnitSpritesAfterRuntimeCopy @0046A215 picture-to-unit-type aliases.
     */
    private void applyHeroSpriteTypeAlias(String pictureName) {
        switch (pictureName) {
            case "unarmed" -> type = 1;
            case "unarmed_" -> type = 2;
            case "swordsman" -> type = 3;
            case "swordsman_" -> type = 4;
            case "swordsman2h" -> type = 5;
            case "axeman" -> type = 7;
            case "axeman_" -> type = 8;
            case "axeman2h" -> type = 9;
            case "clubman" -> type = 0x0A;
            case "clubman_" -> type = 0x0B;
            case "pikeman" -> type = 0x0C;
            case "pikeman_" -> type = 0x0D;
            case "archer", "bowman" -> type = 0x0E;
            case "xbowman" -> type = 0x0F;
            case "mage" -> type = 0x17;
            case "mage_st" -> type = 0x18;
            default -> {
            }
        }
    }

    /**
     * Native support for active-action table consistency after CUnit::RefreshUnitSpritesAfterRuntimeCopy @0046A215
     * rewrites CGameObject::type. Native CDWordArray::GetAt @00402880 delegates to unchecked operator[] @004028A0;
     * Java keeps the managed action phase inside the current native attack-frame table instead of reading stale MFC
     * backing memory after a humanoid equipment/picture alias change.
     */
    private void realignActiveAttackAnimationAfterTypeChange(int previousType) {
        if (type == previousType || (action != 3 && action != 7 && action != 8)) {
            return;
        }
        int attackFrameSequenceCount = getUnitInfo().m_AttackFrameSequenceCount;
        if (attackFrameSequenceCount == 0) {
            throw new IllegalStateException("Active attack action has empty native attack frame sequence for unit type " + type);
        }
        if (phase >= attackFrameSequenceCount) {
            phase = attackFrameSequenceCount - 1;
        }
        actionSegments = Math.max(0, attackFrameSequenceCount - actionPhase);
    }

    /**
     * Native: CUnit::RenderFullStatsInfo @0046AA1D.
     * Fully ported.
     */
    public void renderFullStatsInfo(CRect pRect) {
        int visibilityLevel = getFullStatsVisibilityLevel();
        renderFullStatsNameBlock(pRect);
        if (visibilityLevel >= 5) {
            renderFullStatsPrimaryAttributes(pRect);
        }
        if (visibilityLevel >= 1) {
            renderFullStatsVitals(pRect);
        }
        if (visibilityLevel >= 3) {
            renderFullStatsCombat(pRect);
        }
        if (visibilityLevel >= 4) {
            renderFullStatsAbsorbAndDefense(pRect);
        }
        if (visibilityLevel >= 2) {
            renderFullStatsMovement(pRect);
        }
        if (visibilityLevel >= 7) {
            renderFullStatsSkillsColumn(pRect);
        }
        if (visibilityLevel >= 6) {
            renderFullStatsResistanceColumn(pRect);
        }
        if (visibilityLevel == 7) {
            renderFullStatsDetailExtras(pRect);
        }
    }

    /**
     * Native: CUnit::GetFullStatsTooltipText @0046B9F0.
     * Fully ported.
     */
    public String getFullStatsTooltipText(int x, int y) {
        if (x >= 0x9F) {
            return null;
        }
        int visibilityLevel = getFullStatsVisibilityLevel();
        if (visibilityLevel >= 5 && x <= 0x4F && inClosedRange(y, 0x2C, 0x53)) {
            return get(MAIN_BODY_TOOLTIP_155 + ((y - 0x2C) / 10));
        }
        if (visibilityLevel >= 1 && x >= 0x50 && inClosedRange(y, 0x2C, 0x53)) {
            return get(MAIN_HEALTH_IS_THE_VITALITY_OF_THE_CHARACTER_THE_HIGHER_THE_HEALTH_159
                    + ((y - 0x2C) / 0x14));
        }
        if (visibilityLevel >= 3 && x <= 0x49 && inClosedRange(y, 0x58, 0x6B)) {
            return get(MAIN_DAMAGE_IS_THE_AMOUNT_OF_INJURY_WHICH_A_CHARACTER_CAN_INFLICT_161
                    + ((y - 0x58) / 10));
        }
        if (visibilityLevel >= 4 && x >= 0x4B && inClosedRange(y, 0x58, 0x6B)) {
            return get(MAIN_ABSORPTION_WILL_REDUCE_THE_TOTAL_NUMBER_OF_DAMAGE_POINTS_BEING_163
                    + ((y - 0x58) / 10));
        }
        if (visibilityLevel >= 2 && x >= 0x29 && x <= 0x77 && inClosedRange(y, 0xC8, 0xDB)) {
            return get(MAIN_THE_TOTAL_WEIGHT_OF_THE_ITEMS_WORN_AND_OWNED_BY_A_CHARACTER_IT_165
                    + ((y - 0xBE) / 10));
        }
        if (visibilityLevel >= 7) {
            if (x < 0x50 && y > 0x79 && y < 0xAC) {
                int tooltipBase = type == 0x18 || type == 0x17
                        ? MAIN_FIRE_SPHERE_TOOLTIP_176
                        : MAIN_BLADE_TOOLTIP_171;
                return get(tooltipBase + ((y - 0x7A) / 10));
            }
        }
        if (visibilityLevel >= 6) {
            if (x < 0x4A && y > 0x6F && y < 0x7A) {
                return (unitFlags & UNIT_FLAG_DYNAMIC_INFO_PICTURE_MASK) == 0
                        ? get(MAIN_THE_RESISTANCE_OF_THE_MONSTER_TO_A_WEAPON_TYPE_SHOWS_THE_DAMAGE_188)
                        : get(MAIN_THE_SKILL_OF_WIELDING_VARIOUS_TYPES_OF_WEAPONS_OR_MAGIC_SPHERES_168);
            }
            if (x > 0x49 && y > 0x6F && y < 0x7A) {
                return get(MAIN_RESISTANCE_TO_A_CERTAIN_MAGIC_SPHERE_SHOWS_THE_PERCENTAGE_OF_THE_169);
            }
            if (x > 0x49 && y > 0x79 && y < 0xAC) {
                return get(MAIN_FIRE_SPHERE_TOOLTIP_176 + ((y - 0x7A) / 10));
            }
        }
        if ((unitFlags & UNIT_FLAG_HUMANOID) != 0 && cPlayer == pMapVisualObject.currentPlayer) {
            if (x > 0x0F && x < 0x90 && y > 0xAD && y < 0xB8) {
                return get(MAIN_THE_TOTAL_WEIGHT_OF_THE_ITEMS_WORN_AND_OWNED_BY_A_CHARACTER_IT_165);
            }
            if (x > 0x0F && x < 0x90 && y > 0xB9 && y < 0xC4) {
                return get(MAIN_EXPERIENCE_IS_THE_SUM_TOTAL_OF_ALL_THE_KNOWLEDGE_ABILITIES_AND_170);
            }
        }
        if ((unitFlags & UNIT_FLAG_HUMANOID) == 0
                && availableSpellMask != 0
                && x > 0x0F && x < 0x90
                && y > 0xAD && y < 0xB8) {
            return buildFullStatsSpellTooltip();
        }
        return null;
    }

    /**
     * Native support extracted from CUnit::RenderFullStatsInfo @0046AA1D and CUnit::GetFullStatsTooltipText @0046B9F0
     * visibility/detail gates.
     */
    private int getFullStatsVisibilityLevel() {
        int visibilityLevel = 0;
        int serverId = Short.toUnsignedInt(serverID);
        if (serverId > 0 && serverId < 0x0A00) {
            visibilityLevel = Byte.toUnsignedInt(pMapVisualObject.playerKnowledgeTable[serverId]) >> 1;
        }
        int ownerPlayerId = cPlayer.playerId;
        if (ownerPlayerId == 0) {
            visibilityLevel = 7;
        } else if (pMapVisualObject.currentPlayer.isMapVisible(ownerPlayerId)) {
            visibilityLevel = 7;
        }
        if (Globals.terrainLightOverrideTransferMode != 0) {
            visibilityLevel = 7;
        }
        return visibilityLevel;
    }

    /**
     * Native support extracted from CUnit::GetFullStatsTooltipText @0046B9F0 monster spell-list tooltip branch.
     */
    private String buildFullStatsSpellTooltip() {
        StringBuilder tooltip = new StringBuilder(get(MAIN_SPELLS_192));
        tooltip.append('#');
        int spellCount = Integer.bitCount(availableSpellMask);
        int spellsPerLine = (spellCount - 1) / 7 + 1;
        int spellsOnLine = 0;
        int emittedSpells = 0;
        for (int bit = 0; bit < Integer.SIZE; bit++) {
            if ((availableSpellMask & (1 << bit)) != 0) {
                emittedSpells++;
                tooltip.append(get(SPELL, SpellText.byIndex(bit - 1)));
                spellsOnLine++;
                if (emittedSpells < spellCount) {
                    if (spellsOnLine == spellsPerLine) {
                        tooltip.append(",#");
                        spellsOnLine = 0;
                    } else {
                        tooltip.append(", ");
                    }
                }
            }
        }
        return tooltip.toString();
    }

    /**
     * Native support extracted from CUnit::RenderFullStatsInfo @0046AA1D name drawing.
     */
    private void renderFullStatsNameBlock(CRect pRect) {
        int centerX = pRect.right - 0x58;
        if (name.isEmpty()) {
            if ((unitFlags & UNIT_FLAG_DYNAMIC_INFO_PICTURE_MASK) != 0) {
                int ownerPlayerId = cPlayer.playerId;
                if (!pMapVisualObject.currentPlayer.isEnemy(ownerPlayerId)) {
                    drawFullStatsText(centerX, pRect.top + 0x12, get(MAIN_MERCENARY_84), TextAlign.CENTER,
                            Palettes.yellowish);
                } else if (type < 0x40) {
                    drawFullStatsText(centerX, pRect.top + 0x12, get(MAIN_ENEMY_83), TextAlign.CENTER,
                            Palettes.yellowish);
                }
            }
            drawFullStatsText(centerX, pRect.top + 0x1C, get(UNITNAME, UnitNameText.byIndex(type)),
                    TextAlign.CENTER, Palettes.yellowish);
            return;
        }
        drawFullStatsText(centerX, pRect.top + 0x12, name, TextAlign.CENTER, Palettes.yellowish);
        drawFullStatsText(centerX, pRect.top + 0x1C, clan, TextAlign.CENTER, Palettes.yellowish);
    }

    /**
     * Native support extracted from CUnit::RenderFullStatsInfo @0046AA1D primary attribute drawing.
     */
    private void renderFullStatsPrimaryAttributes(CRect pRect) {
        int[] labelIds = {MAIN_BODY_15, MAIN_AGILITY_16, MAIN_MIND_17, MAIN_SPIRIT_18};
        int[] values = {
                Byte.toUnsignedInt(body),
                Byte.toUnsignedInt(reaction),
                Byte.toUnsignedInt(mind),
                Byte.toUnsignedInt(spirit)
        };
        for (int i = 0; i < labelIds.length; i++) {
            int y = pRect.top + 0x2C + i * 10;
            drawFullStatsText(pRect.left + 6, y, get(labelIds[i]), TextAlign.DEFAULT, Palettes.yellowish);
            drawFullStatsText(pRect.left + 0x50, y, Integer.toString(values[i]), TextAlign.RIGHT,
                    Palettes.greenLeaningGray);
        }
    }

    /**
     * Native support extracted from CUnit::RenderFullStatsInfo @0046AA1D health/mana drawing.
     */
    private void renderFullStatsVitals(CRect pRect) {
        int centerX = pRect.left + 0x73;
        drawFullStatsText(centerX, pRect.top + 0x2C, get(MAIN_HEALTH_19), TextAlign.CENTER, Palettes.yellowish);
        drawFullStatsText(centerX, pRect.top + 0x40, get(MAIN_MANA_20), TextAlign.CENTER, Palettes.yellowish);
        drawFullStatsText(centerX, pRect.top + 0x36, (int) HP + "/" + (int) MaxHP,
                TextAlign.CENTER, Palettes.greenLeaningGray);
        drawFullStatsText(centerX, pRect.top + 0x4A, (int) MP + "/" + (int) MaxMP,
                TextAlign.CENTER, Palettes.greenLeaningGray);
    }

    /**
     * Native support extracted from CUnit::RenderFullStatsInfo @0046AA1D combat stat drawing.
     */
    private void renderFullStatsCombat(CRect pRect) {
        if ((unitFlags & UNIT_FLAG_MAGIC_CLASS) == 0) {
            renderFullStatsFighterDamageAndAttack(pRect);
        } else {
            renderFullStatsMageDamage(pRect);
        }
    }

    /**
     * Native fighter branch in CUnit::RenderFullStatsInfo @0046AA1D.
     */
    private void renderFullStatsFighterDamageAndAttack(CRect pRect) {
        int damageMin = Byte.toUnsignedInt(physicalDamageMin);
        int damageSpread = Byte.toUnsignedInt(physicalDamageSpread);
        if ((physicalDamageMin & 0x80) != 0 && (unitFlags & UNIT_FLAG_DYNAMIC_INFO_PICTURE_MASK) == 0) {
            damageMin = (damageMin & 0x7F) * 0x0F;
            damageSpread *= 0x0F;
        }
        int damageMax = damageMin + damageSpread;
        drawFullStatsText(pRect.left + 6, pRect.top + 0x58, get(MAIN_DMG_23), TextAlign.DEFAULT, Palettes.yellowish);
        drawFullStatsText(pRect.left + 6, pRect.top + 0x62, get(MAIN_ATTACK_25), TextAlign.DEFAULT, Palettes.yellowish);
        drawFullStatsText(pRect.left + 0x46, pRect.top + 0x58, damageMin + "-" + damageMax, TextAlign.RIGHT,
                Palettes.greenLeaningGray);
        drawFullStatsText(pRect.left + 0x46, pRect.top + 0x62, Integer.toString(attackSkill),
                TextAlign.RIGHT, Palettes.greenLeaningGray);
    }

    /**
     * Native mage equipment-token spell-damage branch in CUnit::RenderFullStatsInfo @0046AA1D.
     */
    private void renderFullStatsMageDamage(CRect pRect) {
        TokenEntry weaponToken = equipmentTokenEntries[0];
        if (weaponToken == null || weaponToken.payloadEntryCount == 0) {
            return;
        }

        Spell spell = new Spell((byte) weaponToken.getCastSpellId());
        spell.updateStats(weaponToken.getSpellLevel());
        if (spell.getMinDamage() + spell.getDamageSpread() == 0) {
            return;
        }

        drawFullStatsText(pRect.left + 6, pRect.top + 0x58, get(MAIN_DMG_23), TextAlign.DEFAULT, Palettes.yellowish);
        drawFullStatsText(pRect.left + 0x46, pRect.top + 0x58, spell.getMinDamage() + "-" + spell.getMaxDamage(),
                TextAlign.RIGHT, Palettes.greenLeaningGray);
    }

    /**
     * Native defense branch in CUnit::RenderFullStatsInfo @0046AA1D.
     */
    private void renderFullStatsAbsorbAndDefense(CRect pRect) {
        drawFullStatsText(pRect.left + 0x4A, pRect.top + 0x58, get(MAIN_ABSORB_24), TextAlign.DEFAULT,
                Palettes.yellowish);
        drawFullStatsText(pRect.left + 0x4A, pRect.top + 0x62, get(MAIN_DEFENSE_26), TextAlign.DEFAULT,
                Palettes.yellowish);
        drawFullStatsText(pRect.left + 0x8C, pRect.top + 0x58, Integer.toString(Byte.toUnsignedInt(absorption)),
                TextAlign.RIGHT, Palettes.greenLeaningGray);
        drawFullStatsText(pRect.left + 0x8C, pRect.top + 0x62, Integer.toString(defence),
                TextAlign.RIGHT, Palettes.greenLeaningGray);
    }

    /**
     * Native support extracted from CUnit::RenderFullStatsInfo @0046AA1D left skills/resistance column.
     */
    private void renderFullStatsSkillsColumn(CRect pRect) {
        drawFullStatsText(pRect.left + 6, pRect.top + 0x70,
                get((unitFlags & UNIT_FLAG_DYNAMIC_INFO_PICTURE_MASK) == 0 ? MAIN_RESISTANCE_28 : MAIN_SKILLS_27),
                TextAlign.DEFAULT, Palettes.brownish);
        int leftLabelBase = usesSphereSkillLabels() ? MAIN_FIRE_36 : MAIN_BLADE_30;
        for (int i = 0; i < 5; i++) {
            int y = pRect.top + 0x7A + i * 10;
            drawFullStatsText(pRect.left + 6, y, get(leftLabelBase + i), TextAlign.DEFAULT, Palettes.yellowish);
            drawFullStatsText(pRect.left + 0x46, y, Integer.toString(Byte.toUnsignedInt(sphereLevels[i])),
                    TextAlign.RIGHT, Palettes.greenLeaningGray);
        }
    }

    /**
     * Native support extracted from CUnit::RenderFullStatsInfo @0046AA1D right resistance column.
     */
    private void renderFullStatsResistanceColumn(CRect pRect) {
        drawFullStatsText(pRect.left + 0x4A, pRect.top + 0x70, get(MAIN_RESISTANCE_28),
                TextAlign.DEFAULT, Palettes.brownish);
        for (int i = 0; i < 5; i++) {
            int y = pRect.top + 0x7A + i * 10;
            drawFullStatsText(pRect.left + 0x4A, y, get(MAIN_FIRE_41 + i), TextAlign.DEFAULT, Palettes.yellowish);
            drawFullStatsText(pRect.left + 0x8C, y, Integer.toString(Byte.toUnsignedInt(protectionLevels[i])),
                    TextAlign.RIGHT, Palettes.greenLeaningGray);
        }
    }

    /**
     * Native support extracted from CUnit::RenderFullStatsInfo @0046AA1D sight/speed block.
     */
    private void renderFullStatsMovement(CRect pRect) {
        drawFullStatsText(pRect.left + 0x28, pRect.top + 0xC8, get(MAIN_SIGHT_21), TextAlign.DEFAULT,
                Palettes.yellowish);
        drawFullStatsText(pRect.left + 0x28, pRect.top + 0xD2, get(MAIN_SPEED_22), TextAlign.DEFAULT,
                Palettes.yellowish);
        int sightRangeValue = packedSightRange;
        drawFullStatsText(pRect.left + 0x6A, pRect.top + 0xC8,
                (sightRangeValue >> 8) + "." + (((sightRangeValue & 0xFF) * 10) >> 8),
                TextAlign.RIGHT, Palettes.greenLeaningGray);
        drawFullStatsText(pRect.left + 0x6A, pRect.top + 0xD2, Integer.toString(speed),
                TextAlign.RIGHT, Palettes.greenLeaningGray);
    }

    /**
     * Native support extracted from CUnit::RenderFullStatsInfo @0046AA1D bottom humanoid/monster details.
     */
    private void renderFullStatsDetailExtras(CRect pRect) {
        if ((unitFlags & UNIT_FLAG_HUMANOID) != 0) {
            drawFullStatsText(pRect.left + 0x10, pRect.top + 0xAE, get(MAIN_WEIGHT_35), TextAlign.DEFAULT,
                    Palettes.yellowish);
            drawFullStatsText(pRect.left + 0x7F, pRect.top + 0xAE,
                    (copiedEncumbranceWeight / 10) + "." + (copiedEncumbranceWeight % 10),
                    TextAlign.RIGHT, Palettes.greenLeaningGray);
            drawFullStatsText(pRect.left + 0x10, pRect.top + 0xBA, get(MAIN_XP_46), TextAlign.DEFAULT,
                    Palettes.yellowish);
            drawFullStatsText(pRect.left + 0x7F, pRect.top + 0xBA,
                    Integer.toString(experience), TextAlign.RIGHT, Palettes.greenLeaningGray);
        } else {
            if (availableSpellMask != 0) {
                drawFullStatsText(pRect.left + 0x4A, pRect.top + 0xAE, get(MAIN_SPELLCASTER_190),
                        TextAlign.CENTER, Palettes.yellowish);
            }
            if ((unitFlags & UNIT_FLAG_DYNAMIC_INFO_PICTURE_MASK) == 0 && armorPiercingSkillIndex != 0) {
                drawFullStatsText(pRect.left + 0x4A, pRect.top + 0xBA, get(MAIN_ARMOR_PIERCING_191),
                        TextAlign.CENTER, Palettes.yellowish);
            }
        }
    }

    /**
     * Native support extracted from CUnit::RenderFullStatsInfo @0046AA1D magic-vs-fighting label selection.
     */
    private boolean usesSphereSkillLabels() {
        return type == 0x18 || type == 0x17
                || ((unitFlags & UNIT_FLAG_HUMANOID) != 0 && (unitFlags & UNIT_FLAG_MAGIC_CLASS) != 0);
    }

    /**
     * Native support extracted from CUnit::GetFullStatsTooltipText @0046B9F0 bounds checks.
     */
    private static boolean inClosedRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    /**
     * Native support extracted from CBitmapFont::DrawTextShadowed calls in CUnit::RenderFullStatsInfo @0046AA1D.
     */
    private static void drawFullStatsText(int x, int y, String text, TextAlign align, Palette16 palette) {
        CBitmapFont font = Globals.fonts.font2;
        font.drawTextShadowed(x, y, text, align.mask, palette, 1);
    }

    /**
     * vtbl +0x10: CUnit::Dump @00466162.
     * Fully ported at the modeled dump-string boundary; native writes the class name into `CDumpContext`.
     */
    @Override
    public String dump() {
        return "CUnit";
    }

    /**
     * vtbl +0x14: CUnit::SetSelected @00463216.
     * Fully ported. Native cancels the hero-inventory drag lock when the primary-selected unit is deselected, then
     * delegates to CGameObject::SetSelected.
     */
    @Override
    public void setSelected(boolean selected) {
        if (!selected
                && pMapVisualObject.getPrimarySelectedObject() == this
                && Globals.mainWindow.uiLockPayload != null) {
            CMousePointer.Cursor_Default.setToMousePointer();
            Globals.mainWindow.pHeroInventoryControlVisualObject.completeUiDrag(
                    Globals.mainWindow.pHeroInventoryControlVisualObject.getVisibleStart()
            );
            Globals.mainWindow.clearUiLockState();
        }
        super.setSelected(selected);
    }

    /**
     * vtbl +0x18: CUnit::GetPortraitWidth @0046DED0.
     * Full port. Native returns a constant portrait width of `0x30`.
     */
    @Override
    public int getPortraitWidth() {
        return 0x30;
    }

    /**
     * vtbl +0x1C: CUnit::GetPortraitHeight @0046DEE0.
     * Full port. Native returns a constant portrait height of `0x50`.
     */
    @Override
    public int getPortraitHeight() {
        return 0x50;
    }

    /**
     * vtbl +0x20: CUnit::GetTileWidth @004631CE.
     * Full port. Native reads `g_UnitTypes[type].m_TileSize`.
     */
    @Override
    public int getTileWidth() {
        return UnitTypes.getUnitInfo(type).m_TileSize;
    }

    /**
     * vtbl +0x24: CUnit::GetTileHeight @004631F2.
     * Full port. Native reads `g_UnitTypes[type].m_TileSize`.
     */
    @Override
    public int getTileHeight() {
        return UnitTypes.getUnitInfo(type).m_TileSize;
    }

    /**
     * vtbl +0x28: CUnit::Draw @004632A1.
     * Fully ported. Native frame selection, palette resolution, hovered-unit palette-page override, invisibility
     * gating, smoothing overlays, and transient-effect draw ordering are mapped.
     */
    @Override
    public void draw(int param1, int param2, int param3) {
        if (getDeathState() >= 5) {
            return;
        }

        UnitRenderState renderState = resolveRenderState();
        int deathState = getDeathState();
        boolean grayscaleEffect = findPackedEffectIndex(0x2c) >= 0 && deathState < 3;
        if (grayscaleEffect) {
            applyGrayscaleEffectRenderFrame(renderState);
        }

        drawTransientEffectVisuals(true);
        CGamePalette palette = resolveRenderPalette(
                getUnitInfo(),
                renderState.info,
                grayscaleEffect
        );
        if (pMapVisualObject.isHoveredObject(this) && Globals.mainWindow.uiLockPayload == null) {
            param3 = 0;
        }
        boolean invisibleEffect = findPackedEffectIndex(0x20) >= 0;
        if (invisibleEffect && !pMapVisualObject.currentPlayer.isMapVisible(cPlayer.playerId)) {
            return;
        }

        if ((unitFlags & 0x1) == 0) {
            drawGroundUnit(renderState, param3, palette, invisibleEffect);
        } else {
            if (renderState.frameIndex >= pCSprite256.frameCount) {
                return;
            }
            drawAirUnit(renderState, param3, palette, invisibleEffect);
        }
        drawTransientEffectVisuals(false);
    }

    /**
     * vtbl +0x2C: CUnit::DrawShadow @00464487.
     * Fully ported. Native shadow frame selection, grayscale-effect frame override, invisibility/light-height gating,
     * CAirUnit alpha shadow path, and smoothing overlay gating are mapped.
     */
    @Override
    public void drawShadow(int viewTileX, int viewTileY) {
        if (getDeathState() >= 5) {
            return;
        }

        UnitRenderState renderState = resolveRenderState();
        int deathState = getDeathState();
        if (findPackedEffectIndex(0x2c) >= 0 && deathState < 3) {
            applyGrayscaleEffectRenderFrame(renderState);
        }
        boolean invisibleEffect = findPackedEffectIndex(0x20) >= 0;
        if (invisibleEffect && !pMapVisualObject.isOwnerVisible(cPlayer.playerId)) {
            return;
        }

        int primaryEffectIndex = invisibleEffect ? Globals.lighting.lightHeight : Globals.lighting.shadowLength;
        if ((unitFlags & 0x1) == 0) {
            GraphicsUnitsFile graphicsUnitsFile = UnitTypes.getGraphicsUnitsFile(renderState.fileId);
            boolean airUnitShadow = this instanceof CAirUnit;
            drawShadowSprite(
                    graphicsUnitsFile.getSprite(),
                    renderState,
                    primaryEffectIndex,
                    airUnitShadow
            );
            if (!invisibleEffect && Globals.gamePreferences.smoothing != 0) {
                drawShadowSprite(
                        graphicsUnitsFile.getBSprite(),
                        renderState,
                        Globals.lighting.lightHeight,
                        airUnitShadow
                );
            }
            return;
        }

        if (renderState.frameIndex < pCSprite256.frameCount) {
            drawShadowSprite(pCSprite256, renderState, primaryEffectIndex, false);
            if (!invisibleEffect && Globals.gamePreferences.smoothing != 0) {
                drawShadowSprite(pCSprite256_2, renderState, Globals.lighting.lightHeight, false);
            }
        }
    }

    /**
     * vtbl +0x30: CUnit::DrawSelectionOverlay @004653A5.
     * Fully ported. Native selection-rect geometry, HP/MP bars, control-group marker, quest-goal branch, and clan-name
     * text draws are mapped.
     */
    @Override
    public void drawSelectionOverlay() {
        if (getDeathState() >= 5) {
            return;
        }

        if (findPackedEffectIndex(0x20) >= 0 && !pMapVisualObject.isOwnerVisible(cPlayer.playerId)) {
            return;
        }

        CUnitInfo info = getUnitInfo();
        int left = (centerScreenX - info.m_CenterX) + info.m_SelectionRect.left;
        int top = (((centerScreenY - info.m_CenterY) - terrainHeightOffset) + info.m_SelectionRect.top) - z;
        int right = (centerScreenX - info.m_CenterX) + info.m_SelectionRect.right;

        GUI.ball.drawRectMasked(left, top - 2, 0, 0, 4, 4);
        GUI.ball.drawRectMasked(right - 4, top - 2, 0, 0, 4, 4);

        int barWidth = right - left - 8;
        int hp = HP;
        int maxHp = MaxHP;
        int hpFillWidth = (hp * barWidth) / maxHp;
        if (hpFillWidth == 0 && HP != 0) {
            hpFillWidth = 1;
        }
        short hpLightColor = resolveHealthLightColor(hp, maxHp);
        short hpMediumColor = resolveHealthMediumColor(hp, maxHp);
        short hpDarkColor = resolveHealthDarkColor(hp, maxHp);
        if (shouldDimSelectionHighlightColors()) {
            hpLightColor = dimSelectionBarColorForShowAll(hpLightColor);
            hpMediumColor = dimSelectionBarColorForShowAll(hpMediumColor);
            hpDarkColor = dimSelectionBarColorForShowAll(hpDarkColor);
        }
        drawSelectionHighlight(
                left,
                right,
                top,
                hpFillWidth,
                hpLightColor,
                hpMediumColor,
                hpDarkColor
        );

        if (MaxMP > 0) {
            top += 4;
            int mp = MP;
            int maxMp = MaxMP;
            int mpFillWidth = (mp * barWidth) / maxMp;
            if (mpFillWidth == 0) {
                mpFillWidth = 1;
            }
            GUI.ball.drawRectMasked(left, top - 2, 0, 0, 4, 4);
            GUI.ball.drawRectMasked(right - 4, top - 2, 0, 0, 4, 4);
            short mpLightColor = RGB16.from(0, 0, 0xFF).val();
            short mpMediumColor = RGB16.from(0, 0, 0xC0).val();
            short mpDarkColor = RGB16.from(0, 0, 0x80).val();
            if (shouldDimSelectionHighlightColors()) {
                mpLightColor = dimSelectionBarColorForShowAll(mpLightColor);
                mpMediumColor = dimSelectionBarColorForShowAll(mpMediumColor);
                mpDarkColor = dimSelectionBarColorForShowAll(mpDarkColor);
            }
            drawSelectionHighlight(
                    left,
                    right,
                    top,
                    mpFillWidth,
                    mpLightColor,
                    mpMediumColor,
                    mpDarkColor
            );
        }

        drawFirstControlGroupMarker(left, top + 4);
        drawSelectionOverlayText(left, right, top);
    }

    /**
     * vtbl +0x34: CUnit::DrawMinimap @00465F3F.
     * Full port. Native draws the unit minimap marker only when alive/visible and occupancy-confirmed, using owner palette page 8 color index `0xA4`.
     */
    @Override
    public void drawMinimap(int param1, int param2, int param3) {
        if (getDeathState() >= 2 || (unitFlags & 0x80) != 0) {
            return;
        }

        int effectIndex = findPackedEffectIndex(0x20);
        if (effectIndex >= 0 && !pMapVisualObject.isOwnerVisible(cPlayer.playerId)) {
            return;
        }

        short[] tileFlags = pMapVisualObject.getOccupancyTileFlags();
        int unitTileX = location.x >> 8;
        int unitTileY = location.y >> 8;
        int tileIndex = pMapVisualObject.mapDescriptor.tileIndex(unitTileX, unitTileY);
        int mapWidth = pMapVisualObject.getOccupancyMapWidth();
        int blockedMask = (tileFlags[tileIndex] & 0xC000)
                | (tileFlags[tileIndex + 1] & 0xC000)
                | (tileFlags[tileIndex + mapWidth] & 0xC000)
                | (tileFlags[tileIndex + mapWidth + 1] & 0xC000);
        if (blockedMask != 0xC000) {
            return;
        }

        int minimapX;
        int minimapY;
        int markerSize;
        if (param3 < 0) {
            minimapX = ((location.x >> 8) - 8) >> 1;
            minimapY = ((location.y >> 8) - 8) >> 1;
            markerSize = 1;
        } else {
            int zoomShift = param3 & 0x1F;
            minimapX = ((location.x >> 8) - 8) << zoomShift;
            minimapY = ((location.y >> 8) - 8) << zoomShift;
            markerSize = 1 << zoomShift;
        }

        int markerLeft = param1 + minimapX;
        int markerTop = param2 + minimapY;
        short markerColor = resolveMinimapMarkerColor();
        Globals.renderer.fillScreenRect(
                markerLeft,
                markerTop,
                markerLeft + markerSize,
                markerTop + markerSize,
                markerColor
        );
    }

    /**
     * vtbl +0x38: CUnit::UpdateMapLayer @00463093.
     * Fully ported. Native layer-cell writes, dirty-render-rect update, and selection-dirty reset are mapped.
     */
    @Override
    public void updateMapLayer() {
        if (getDeathState() < 2 && (unitFlags & 0x80) == 0) {
            markObjectLayerCell(2, this);
        } else {
            markObjectLayerCell(4, this);
        }
        if (Globals.gamePreferences.animation == 0 && getBlockedDrawState() == 0 && m_bSelectionDirty != 0) {
            CUnitInfo info = getUnitInfo();
            int leftBase = centerScreenX - info.m_CenterX;
            int top = centerScreenY - info.m_CenterY - terrainHeightOffset - z;
            pMapVisualObject.dirtyRenderRect.unionWith(new CRect(
                    leftBase & DIRTY_RECT_TILE_ALIGNMENT,
                    top,
                    ((leftBase + info.m_Width) & DIRTY_RECT_TILE_ALIGNMENT) + DIRTY_RECT_TILE_SIZE,
                    top + info.m_Height
            ));
        }
        m_bSelectionDirty = 0;
    }

    /**
     * vtbl +0x3C: CUnit::AdvanceMapObjectState @0046617C.
     * Fully ported. Native state progression for idle, move, rotate, attack, death, occupancy refresh, and selected
     * panel notifications is preserved.
     */
    @Override
    public boolean advanceMapObjectState() {
        CUnitInfo info = getUnitInfo();

        if (getDeathState() < 5) {
            updateEffectVisuals();
            if (actionSegments == 0) {
                if (lastAction != 0 || !dwarr_130.isEmpty() || info.m_IdlePhases > 0) {
                    m_bSelectionDirty = 1;
                }
                lastAction = 0;
                if (info.m_IdlePhases == 0) {
                    field10_0x34 = -1;
                    phase = 0;
                    actionPhase = 0;
                } else {
                    actionPhase++;
                    phase = actionPhase % info.m_IdleFrameSequenceCount;
                }
                tryOccupyMap();
                location2.x = location.x;
                location2.y = location.y;
            } else {
                m_bSelectionDirty = 1;
                switch (Byte.toUnsignedInt(action)) {
                    case 1 -> advanceMoveAction();
                    case 3 -> {
                        if (actionPhase == info.m_AttackDelay) {
                            playAttackSound();
                        }
                        dir = actionDir;
                        phase = actionPhase;
                        actionPhase++;
                        location2.x = location.x;
                        location2.y = location.y;
                    }
                    case 5 -> advanceRotateAction();
                    case 6 -> {
                        location.x = actionX;
                        location.y = actionY;
                        actionPhase++;
                        phase = actionPhase - 1;
                        location2.x = location.x;
                        location2.y = location.y;
                    }
                    case 7 -> {
                        if (actionPhase == info.m_ShootDelay && info.m_ProjectileID != 0) {
                            spawnAttackProjectile();
                        }
                        if (actionPhase == info.m_AttackDelay) {
                            playAttackSound();
                        }
                        dir = actionDir;
                        phase = actionPhase;
                        actionPhase++;
                        location2.x = location.x;
                        location2.y = location.y;
                    }
                    case 8 -> {
                        if (actionPhase == info.m_AttackDelay) {
                            playSpellCastSound();
                        }
                        if (actionSpell == 0x36) {
                            if (actionPhase == 0) {
                                spawnSpellProjectile();
                            }
                        } else if (actionPhase == info.m_ShootDelay) {
                            spawnSpellProjectile();
                        }
                        phase = actionPhase;
                        actionPhase++;
                        location2.x = location.x;
                        location2.y = location.y;
                    }
                    default -> {
                    }
                }
                lastAction = action;
                actionSegments--;
                tryOccupyMap();
                if (m_bSelected) {
                    notifySelectedUnitPanels();
                }
            }
        }
        return true;
    }

    /**
     * vtbl +0x44: CUnit::TryOccupyMap @0046DEF0.
     * Full port. Native only delegates to CGameObject::TryOccupyMap while the unit death state is below `3`.
     */
    @Override
    public void tryOccupyMap() {
        if (getDeathState() < 3) {
            super.tryOccupyMap();
        }
    }

    /**
     * vtbl +0x48: CUnit::OccupyMapCells @0046DF20.
     * Full port. Native only delegates to CGameObject::OccupyMapCells while the unit death state is below `3`.
     */
    @Override
    public void occupyMapCells() {
        if (getDeathState() < 3) {
            super.occupyMapCells();
        }
    }

    /**
     * vtbl +0x4C: CUnit::UpdateBlockedState @00468762.
     * Full port. Native treats map-visible owners as unblocked, evaluates hidden owners against occupancy bits, treats `unitFlags & 0x80` units as blocked, and clears selection when a blocked unit was selected.
     */
    @Override
    public boolean updateBlockedState() {
        if ((unitFlags & 0x80) != 0) {
            bIsBlocked = 1;
            if (!m_bSelected) {
                return false;
            }
            m_bSelected = false;
            m_bSelectionDirty = 1;
            return true;
        }

        if (pMapVisualObject.currentPlayer.isMapVisible(cPlayer.playerId)) {
            bIsBlocked = 0;
            return false;
        }

        short[] tileFlags = pMapVisualObject.getOccupancyTileFlags();
        int tileIndex = pMapVisualObject.mapDescriptor.tileIndex(tileX, tileY);
        int mapWidth = pMapVisualObject.getOccupancyMapWidth();
        bIsBlocked = ((tileFlags[tileIndex] & 0xC000)
                | (tileFlags[tileIndex + 1] & 0xC000)
                | (tileFlags[tileIndex + mapWidth] & 0xC000)
                | (tileFlags[tileIndex + mapWidth + 1] & 0xC000)) != 0xC000 ? 1 : 0;
        if (bIsBlocked == 0 || !m_bSelected) {
            return false;
        }

        m_bSelected = false;
        m_bSelectionDirty = 1;
        return true;
    }

    /**
     * vtbl +0x50: CUnit::UpdateEffectVisuals @004676DE.
     * Full port. Native rebuilds transient `VisualElem` overlays from packed timed effect ids, sorts them by descending y, copies them into `transientVisualElements`, and decrements/removes effect timers.
     */
    @Override
    public void updateEffectVisuals() {
        List<VisualElem> transientVisuals = new ArrayList<>();
        int index = 0;
        while (index < dwarr_130.size()) {
            int packedEffect = dwarr_130.get(index);
            int effectType = (packedEffect >>> 16) & 0xFFFF;
            int timer = packedEffect & 0xFFFF;
            int tileWidth = getTileWidth();
            EffectVisualBuilder builder = new EffectVisualBuilder();
            switch (effectType) {
                case 0x10, 0x18, 0x22, 0x2E ->
                        builder.buildDirectionalEffectIcon(tileWidth << 5, effectType, timer % 6);
                case 0x14 -> builder.buildCenteredEffectIcon(tileWidth << 5, timer % 6);
                case 0x30 -> builder.buildDescendingRingEffect(tileWidth << 5, 20.0f, timer % 5);
                case 0x38 ->
                        builder.advanceFallingBurstEffect(transientVisualElements, tileWidth << 4, tileWidth << 3, tileWidth << 5, timer);
                case 0x3C ->
                        builder.advanceRisingBurstEffect(transientVisualElements, tileWidth << 4, tileWidth << 3, tileWidth << 5, timer);
                case 0x3E ->
                        builder.buildCrossSectionBandEffect(tileWidth * 0x0B, tileWidth * 0x1C, tileWidth << 4, timer % 0x5A);
                case 0x40 -> builder.buildAscendingRingEffect(tileWidth << 5, 20.0f, timer % 5);
                default -> {
                }
            }
            transientVisuals.addAll(builder.visualElements);

            int remainingTicks = timer - 1;
            dwarr_130.set(index, (packedEffect & 0xFFFF0000) | remainingTicks);
            if (remainingTicks == 0) {
                dwarr_130.remove(index);
                continue;
            }
            index++;
        }
        EffectVisualBuilder.sortVisualElementsByYDescending(transientVisuals);
        transientVisualElements.clear();
        transientVisualElements.addAll(transientVisuals);
    }

    /**
     * Native support: `CArray<CUnitInfo>::ElementAt(&g_UnitTypes, type)` in CUnit render/update paths.
     * not ported.
     */
    protected final CUnitInfo getUnitInfo() {
        return UnitTypes.getUnitInfo(type);
    }

    /**
     * Native support for `CUnit::Draw @004632A1` / `CUnit::DrawShadow @00464487` frame selection.
     * Fully ported. Native frame-table math is preserved, including the special dying/bones substitution path.
     */
    protected final UnitRenderState resolveRenderState() {
        CUnitInfo info = getUnitInfo();

        UnitRenderState state = new UnitRenderState();
        state.unitTypeId = type;
        state.info = info;
        state.fileId = info.m_FileID;
        int facing = (dir - 8 & 0x0E) >> 1;
        state.flipX = info.m_Flip != 0 && facing > 4;
        if (state.flipX) {
            facing = 8 - facing;
        }

        int movePhaseBlock = info.m_MovePhases + info.m_MoveBeginPhases;
        int frameIndex;
        switch (lastAction) {
            case 0 -> {
                if (info.m_IdlePhases == 0) {
                    if (getDeathState() == 1) {
                        int dyingTypeId = info.m_bDying;
                        info = UnitTypes.getUnitInfo(dyingTypeId);
                        movePhaseBlock = info.m_MovePhases + info.m_MoveBeginPhases;
                        facing = (dir - 8 & 0x0E) >> 1;
                        state.flipX = info.m_Flip != 0 && facing > 4;
                        if (state.flipX) {
                            facing = 8 - facing;
                        }
                        frameIndex = info.m_Flip == 0
                                ? (facing + 1) * info.m_DyingPhases + 0x0F + (movePhaseBlock + info.m_AttackPhases) * 8
                                : (movePhaseBlock + info.m_AttackPhases) * 5 + 8 + (facing + 1) * info.m_DyingPhases;
                        state.unitTypeId = dyingTypeId;
                        state.info = info;
                        state.fileId = info.m_FileID;
                    } else if (getDeathState() < 2) {
                        frameIndex = dir - 8 & 0x0F;
                        state.flipX = info.m_Flip != 0 && frameIndex > 8;
                        if (state.flipX) {
                            frameIndex = 0x10 - frameIndex;
                        }
                    } else {
                        CUnitInfo dyingInfo = UnitTypes.getUnitInfo(info.m_bDying);
                        if (dyingInfo.m_BonePhases == 0) {
                            frameIndex = dir - 8 & 0x0F;
                            state.flipX = info.m_Flip != 0 && frameIndex > 8;
                            if (state.flipX) {
                                frameIndex = 0x10 - frameIndex;
                            }
                        } else {
                            info = dyingInfo;
                            movePhaseBlock = info.m_MovePhases + info.m_MoveBeginPhases;
                            facing = (dir - 8 & 0x0E) >> 1;
                            state.flipX = info.m_Flip != 0 && facing > 4;
                            if (state.flipX) {
                                facing = 8 - facing;
                            }
                            frameIndex = info.m_Flip == 0
                                    ? facing * info.m_BonePhases + (movePhaseBlock + info.m_AttackPhases + info.m_DyingPhases) * 8 + 0x0E + getDeathState()
                                    : (movePhaseBlock + info.m_AttackPhases + info.m_DyingPhases) * 5 + facing * info.m_BonePhases + 7 + getDeathState();
                            state.unitTypeId = info.m_bDying;
                            state.info = info;
                            state.fileId = info.m_FileID;
                        }
                    }
                } else {
                    int idleFrame = getAnimationFrame(info.m_IdleFrameSequence, phase, false);
                    frameIndex = info.m_Flip == 0
                            ? facing * info.m_IdlePhases + 0x10 + (movePhaseBlock + info.m_AttackPhases + info.m_DyingPhases) * 8 + idleFrame
                            : (movePhaseBlock + info.m_AttackPhases + info.m_DyingPhases) * 5 + 9 + facing * info.m_IdlePhases + idleFrame;
                }
            }
            case 1 -> {
                int moveFrame = getAnimationFrame(info.m_MoveFrameSequence, phase, true);
                frameIndex = info.m_Flip == 0
                        ? facing * movePhaseBlock + 0x10 + info.m_MoveBeginPhases + moveFrame
                        : facing * movePhaseBlock + 9 + info.m_MoveBeginPhases + moveFrame;
            }
            case 3, 7, 8 -> {
                int attackFrame = getAnimationFrame(info.m_AttackFrameSequence, phase, false);
                frameIndex = info.m_Flip == 0
                        ? facing * info.m_AttackPhases + 0x10 + movePhaseBlock * 8 + attackFrame
                        : movePhaseBlock * 5 + 9 + facing * info.m_AttackPhases + attackFrame;
            }
            case 5 -> {
                frameIndex = dir - 8 & 0x0F;
                state.flipX = info.m_Flip != 0 && frameIndex > 8;
                if (state.flipX) {
                    frameIndex = 0x10 - frameIndex;
                }
            }
            case 6 -> {
                int dyingTypeId = info.m_bDying;
                info = UnitTypes.getUnitInfo(dyingTypeId);
                movePhaseBlock = info.m_MovePhases + info.m_MoveBeginPhases;
                facing = (dir - 8 & 0x0E) >> 1;
                state.flipX = info.m_Flip != 0 && facing > 4;
                if (state.flipX) {
                    facing = 8 - facing;
                }
                frameIndex = info.m_Flip == 0
                        ? facing * info.m_DyingPhases + 0x10 + (movePhaseBlock + info.m_AttackPhases) * 8 + phase / 2
                        : (movePhaseBlock + info.m_AttackPhases) * 5 + 9 + facing * info.m_DyingPhases + phase / 2;
                state.unitTypeId = dyingTypeId;
                state.info = info;
                state.fileId = info.m_FileID;
            }
            default -> frameIndex = 0;
        }

        state.frameIndex = frameIndex;
        return state;
    }

    /**
     * Native support extracted from CUnit::Draw @004632A1 and CUnit::DrawShadow @00464487 spell-effect 0x2c frame
     * override.
     */
    private void applyGrayscaleEffectRenderFrame(UnitRenderState renderState) {
        int frameIndex = dir - 8 & 0x0F;
        renderState.flipX = renderState.info.m_Flip != 0 && frameIndex > 8;
        if (renderState.flipX) {
            frameIndex = 0x10 - frameIndex;
        }
        renderState.frameIndex = frameIndex;
    }

    /**
     * Native support extracted from CUnit::Draw @004632A1 and CUnit::DrawShadow @00464487
     * `CDWordArray::GetAt` frame-table reads.
     * Fully ported.
     */
    protected final int getAnimationFrame(List<Integer> values, int index, boolean wrap) {
        int resolvedIndex = index;
        if (wrap) {
            resolvedIndex %= values.size();
        }
        return values.get(resolvedIndex);
    }

    /**
     * Native support extracted from CUnit::Draw @004632A1 ground-unit sprite path.
     * Fully ported. Native draws the base sprite with DrawWithPalette or DrawFrame_ClippedY for visible invisible
     * units, and always draws the smoothing/type-0x45 overlay through DrawFrame_ClippedY.
     */
    protected final void drawGroundUnit(
            UnitRenderState renderState,
            int palettePage,
            CGamePalette palette,
            boolean invisibleEffect
    ) {
        GraphicsUnitsFile graphicsUnitsFile = UnitTypes.getGraphicsUnitsFile(renderState.fileId);
        drawSprite(graphicsUnitsFile.getSprite(), renderState, palettePage, palette, invisibleEffect);
        if (Globals.gamePreferences.smoothing != 0 || type == 0x45) {
            drawSprite(graphicsUnitsFile.getBSprite(), renderState, palettePage, palette, true);
        }
    }

    /**
     * Native support extracted from CUnit::Draw @004632A1 air-unit sprite path.
     * Fully ported. Native frame-count gating is handled by the caller; the smoothing overlay always uses
     * DrawFrame_ClippedY.
     */
    protected final void drawAirUnit(
            UnitRenderState renderState,
            int palettePage,
            CGamePalette palette,
            boolean invisibleEffect
    ) {
        drawSprite(pCSprite256, renderState, palettePage, palette, invisibleEffect);
        if (Globals.gamePreferences.smoothing != 0) {
            drawSprite(pCSprite256_2, renderState, palettePage, palette, true);
        }
    }

    /**
     * Native support extracted from CUnit::Draw @004632A1 main sprite draw tuple.
     * Fully ported.
     */
    protected final void drawSprite(
            CSprite256 sprite,
            UnitRenderState renderState,
            int palettePage,
            CGamePalette palette,
            boolean clippedY
    ) {
        int drawX = centerScreenX
                - ((renderState.info.m_CenterX - renderState.info.m_Width / 2) + sprite.xSizeOf(renderState.frameIndex) / 2);
        int drawY = ((centerScreenY
                - ((renderState.info.m_CenterY - renderState.info.m_Height / 2) + sprite.ySizeOf(renderState.frameIndex) / 2))
                - terrainHeightOffset) - z;
        if (clippedY) {
            sprite.drawFrameClippedY(drawX, drawY, renderState.frameIndex, palettePage, palette, renderState.flipX);
            return;
        }
        sprite.drawWithPalette(drawX, drawY, renderState.frameIndex, palettePage, palette, renderState.flipX);
    }

    /**
     * Native support for the shadow sprite tuple in `CUnit::DrawShadow @00464487`.
     * Fully ported. Native uses CSprite256::DrawAlpha for CAirUnit and CSprite256::DrawWithRenderEffect otherwise.
     */
    protected final void drawShadowSprite(
            CSprite256 sprite,
            UnitRenderState renderState,
            int effectIndex,
            boolean airUnitShadow
    ) {
        int shadowSkew = resolveShadowSkewForSprite(sprite, renderState);
        int drawX = centerScreenX
                - ((renderState.info.m_CenterX - renderState.info.m_Width / 2) + sprite.xSizeOf(renderState.frameIndex) / 2)
                - shadowSkew;
        int drawY = (centerScreenY
                - ((renderState.info.m_CenterY - renderState.info.m_Height / 2) + sprite.ySizeOf(renderState.frameIndex) / 2))
                - terrainHeightOffset;
        if (airUnitShadow) {
            sprite.drawAlpha(
                    drawX + shadowSkew + resolveShadowSlope() / 2000,
                    drawY,
                    renderState.frameIndex,
                    effectIndex,
                    renderState.flipX
            );
            return;
        }

        sprite.drawWithRenderEffect(drawX, drawY, renderState.frameIndex, effectIndex, resolveShadowSlope(), renderState.flipX);
    }

    /**
     * Native support extracted from CUnit::DrawShadow @00464487 shadow skew.
     */
    private int resolveShadowSkewForSprite(CSprite256 sprite, UnitRenderState renderState) {
        double shadowAngle = Math.tan(pMapVisualObject.mapDescriptor.getShadowAngle());
        return (int) (shadowAngle
                * ((sprite.ySizeOf(renderState.frameIndex) / 2 + renderState.info.m_Height / 2)
                - renderState.info.m_CenterY));
    }

    /**
     * Native support for `CUnit::Draw @004632A1` transient projectile/effect passes.
     * Fully ported.
     */
    protected final void drawTransientEffectVisuals(boolean beforeUnit) {
        for (int index = 0; index < transientVisualElements.size(); index++) {
            VisualElem visual = transientVisualElements.get(index);
            if (beforeUnit != (visual.z > 0)) {
                continue;
            }
            int projectileId = Byte.toUnsignedInt(visual.spriteId);
            CProjectileInfo projectileInfo = Projectiles.PROJECTILES_BY_ID.get(projectileId);
            int drawX = (centerScreenX + visual.x) - projectileInfo.width / 2;
            int drawY = ((((centerScreenY - terrainHeightOffset) - z) + visual.y) - projectileInfo.height / 2) - visual.z;
            projectileInfo.getSpriteA().draw(drawX, drawY, Byte.toUnsignedInt(visual.frame), 0, false);
        }
    }

    /**
     * Native support for the palette selection branch in `CUnit::Draw @004632A1`.
     * Partial port. Java preserves the native split between original unit-type palette routing and the possibly
     * substituted render unit type used for local palette pointers.
     */
    protected final CGamePalette resolveRenderPalette(
            CUnitInfo baseInfo,
            CUnitInfo renderInfo,
            boolean grayscaleEffect
    ) {
        CGamePalette palette;
        if (baseInfo.m_PaletteIndex == 0) {
            int ownerPaletteIndex = cPlayer.color;
            palette = Palettes.unitGamePalettes.get(ownerPaletteIndex);
        } else if (baseInfo.m_PaletteIndex == 1) {
            palette = renderInfo.getOrInitPalette(0);
        } else {
            palette = renderInfo.getOrInitPalette(field8_0x28 - 1);
        }

        if (grayscaleEffect) {
            if (baseInfo.m_PaletteIndex == 0) {
                palette = Palettes.unitGamePalettes.get(0x10);
            } else {
                Palette256 rawPalette = renderInfo.getRawPalette(0);
                CGamePalette grayPalette = new CGamePalette();
                grayPalette.init(rawPalette, 0x10, 5, 0);
                palette = grayPalette;
            }
        }

        return palette;
    }

    /**
     * Native support: CUnit::DrawSelectionHighlight @00460DB1.
     * Fully ported. Native draws selected HP/MP bars directly and applies the shaded/additive show-all-HP path for
     * unselected units.
     */
    protected final void drawSelectionHighlight(
            int left,
            int right,
            int top,
            int fillWidth,
            short colorOuter,
            short colorMiddle,
            short colorInner
    ) {
        int barLeft = left + 4;
        int fillRight = left + fillWidth + 4;
        if (pMapVisualObject.showHitPointBars != 0 && !isSelected()) {
            Globals.renderer.applyShadeToRect(barLeft, top - 2, fillRight, top + 2, 8);
            Globals.renderer.addColorToRect(barLeft, top - 2, fillRight, top - 1, colorInner);
            Globals.renderer.addColorToRect(barLeft, top - 1, fillRight, top, colorOuter);
            Globals.renderer.addColorToRect(barLeft, top, fillRight, top + 1, colorMiddle);
            Globals.renderer.addColorToRect(barLeft, top + 1, fillRight, top + 2, colorInner);
            return;
        }

        int barRight = right - 4;
        Globals.renderer.fillScreenRect(barLeft, top - 2, barRight, top + 2, RGB16.from(0x40, 0x40, 0x40).val());
        Globals.renderer.fillScreenRect(barLeft, top - 1, barRight, top, RGB16.from(0x80, 0x80, 0x80).val());
        Globals.renderer.fillScreenRect(barLeft, top, barRight, top + 1, RGB16.from(0x60, 0x60, 0x60).val());
        Globals.renderer.fillScreenRect(barLeft, top - 2, fillRight, top + 2, colorInner);
        Globals.renderer.fillScreenRect(barLeft, top - 1, fillRight, top, colorOuter);
        Globals.renderer.fillScreenRect(barLeft, top, fillRight, top + 1, colorMiddle);
    }

    /**
     * Native support extracted from CUnit::DrawSelectionOverlay @004653A5 health-color branch.
     * Fully ported.
     */
    protected final short resolveHealthLightColor(int hp, int maxHp) {
        if (hp < (maxHp + ((maxHp >> 31) & 3)) >> 2) {
            return RGB16.from(0xFF, 0, 0).val();
        }
        if (hp < maxHp / 2) {
            return RGB16.from(0xFF, 0xFF, 0).val();
        }
        return RGB16.from(0, 0xFF, 0).val();
    }

    /**
     * Native support extracted from CUnit::DrawSelectionOverlay @004653A5 health-color branch.
     * Fully ported.
     */
    protected final short resolveHealthMediumColor(int hp, int maxHp) {
        if (hp < (maxHp + ((maxHp >> 31) & 3)) >> 2) {
            return RGB16.from(0xC0, 0, 0).val();
        }
        if (hp < maxHp / 2) {
            return RGB16.from(0xC0, 0xC0, 0).val();
        }
        return RGB16.from(0, 0xC0, 0).val();
    }

    /**
     * Native support extracted from CUnit::DrawSelectionOverlay @004653A5 health-color branch.
     * Fully ported.
     */
    protected final short resolveHealthDarkColor(int hp, int maxHp) {
        if (hp < (maxHp + ((maxHp >> 31) & 3)) >> 2) {
            return RGB16.from(0x80, 0, 0).val();
        }
        if (hp < maxHp / 2) {
            return RGB16.from(0x80, 0x80, 0).val();
        }
        return RGB16.from(0, 0x80, 0).val();
    }

    /**
     * Native support extracted from CUnit::DrawSelectionOverlay @004653A5 show-all HP/MP bar dimming gate.
     */
    private boolean shouldDimSelectionHighlightColors() {
        return pMapVisualObject.showHitPointBars != 0 && !isSelected();
    }

    /**
     * Native support extracted from CUnit::DrawSelectionOverlay @004653A5 and
     * CStructure::DrawSelectionOverlay @00461B2B show-all HP/MP color dimming.
     */
    static short dimSelectionBarColorForShowAll(short color) {
        return (short) (((color & 0xFFFF) >>> 1) & MIDGRAY565);
    }

    /**
     * Native support: CUnit::drawFirstControlGroupMarker @004610F3.
     * Fully ported.
     */
    protected final void drawFirstControlGroupMarker(int left, int top) {
        int controlGroup = getFirstControlGroup();
        if (controlGroup < 0) {
            return;
        }

        Palette16 textPalette = pMapVisualObject.showHitPointBars != 0 && !isSelected()
                ? Palettes.grayDim
                : Palettes.gray;
        Globals.fonts.font3.drawTextShadowed(
                left,
                top,
                Integer.toString(controlGroup),
                TextAlign.DEFAULT.mask,
                textPalette,
                1
        );
    }

    /**
     * Native support extracted from CUnit::DrawSelectionOverlay @004653A5 quest/name text branch.
     * Fully ported.
     */
    protected final void drawSelectionOverlayText(int left, int right, int top) {
        if (Globals.mainWindow.sessionMode != CMainWindow.SESSION_MODE_CAMPAIGN
                && Globals.gamePreferences.clanNames != 0
                && !name.isEmpty()
                && (unitFlags & UNIT_FLAG_HUMANOID) != 0) {
            drawSelectionNameText(left, right, top);
            return;
        }

        if (Globals.mainWindow.sessionMode != CMainWindow.SESSION_MODE_CAMPAIGN
                && Globals.gamePreferences.clanNames != 0
                && hasPendingQuestMarker()) {
            Palette16 textPalette = resolveSelectionTextPalette();
            int centerX = (left + right) >> 1;
            Globals.fonts.font2.drawTextInternal(
                    centerX,
                    top - 0x0F,
                    get(MAIN_QUESTS_346),
                    TextAlign.CENTER.mask,
                    textPalette
            );
            Globals.fonts.font2.drawTextInternal(
                    centerX,
                    top - 0x19,
                    get(MAIN_GOAL_345),
                    TextAlign.CENTER.mask,
                    textPalette
            );
        }
    }

    /**
     * Native support extracted from CUnit::DrawSelectionOverlay @004653A5 name/clan text branch.
     * Fully ported.
     */
    protected final void drawSelectionNameText(int left, int right, int top) {
        CBitmapFont font2 = Globals.fonts.font2;
        Palette16 textPalette = resolveSelectionTextPalette();
        int centerX = (left + right) >> 1;
        if (clan.isEmpty()) {
            font2.drawTextInternal(centerX, top - 0x0F, name, TextAlign.CENTER.mask, textPalette);
            return;
        }
        font2.drawTextInternal(centerX, top - 0x0F, clan, TextAlign.CENTER.mask, textPalette);
        font2.drawTextInternal(centerX, top - 0x19, name, TextAlign.CENTER.mask, textPalette);
    }

    /**
     * Native support extracted from CUnit::DrawSelectionOverlay @004653A5 pending-quest marker scan.
     * Fully ported.
     */
    protected final boolean hasPendingQuestMarker() {
        int objectId = m_id & 0xFFFF;
        for (Quest quest : pMapVisualObject.questStorage.questsByKey.values()) {
            if (!quest.isPending()) {
                continue;
            }

            int primaryArgument = quest.primaryArgument;
            int questId = quest.getId();
            if (objectId == primaryArgument) {
                if (questId != 3 && questId != 0x0C) {
                    return true;
                }
            } else if (type == (primaryArgument & 0xFF)
                    && field8_0x28 == (primaryArgument >>> 8)
                    && questId == 2) {
                return true;
            } else if (questFlags == primaryArgument && (questId == 3 || questId == 0x0C)) {
                return true;
            } else if (cPlayer.playerId == primaryArgument && questId == 0x0D) {
                return true;
            }
        }
        return false;
    }

    /**
     * Native support for the owner-color text palette lookup in `CUnit::DrawSelectionOverlay @004653A5`.
     * Fully ported.
     */
    protected final Palette16 resolveSelectionTextPalette() {
        return Palettes.unitPaletteComplements[cPlayer.color];
    }

    /**
     * Native support for the move-action state branch in `CUnit::AdvanceMapObjectState @0046617C`.
     * Fully ported.
     */
    protected final void advanceMoveAction() {
        int stepX = actionX / actionSegments;
        int stepY = actionY / actionSegments;
        actionX -= stepX;
        field39_0xa0 += Math.abs(stepX);
        actionY -= stepY;
        field40_0xa4 += Math.abs(stepY);
        location.x += stepX;
        location.y += stepY;
        dir = actionDir;
        actionPhase += (int) Math.hypot(stepX, stepY);
        if (type < 0) {
            updateNegativeTypeMoveFrame();
            phase = field10_0x34;
            return;
        }
        location2.x = location.x;
        location2.y = location.y;
        phase = (actionPhase + ((actionPhase >> 31) & 0x0F)) >> 4;
    }

    /**
     * Native support for the `type < 0` move-frame branch in `CUnit::AdvanceMapObjectState @0046617C`.
     * Fully ported.
     */
    protected final void updateNegativeTypeMoveFrame() {
        switch (dir) {
            case 0, 8 -> {
                int yDelta = dir == 8 ? 8 : -8;
                switch ((field40_0xa4 / 0x1A) & 7) {
                    case 0 -> setMoveFrame(0, location.x, location.y);
                    case 1 -> setMoveFrame(1, location.x, location.y);
                    case 2 -> setMoveFrame(2, location.x, location.y);
                    case 3 -> setMoveFrame(3, location.x, location.y);
                    case 4 -> setMoveFrame(4, location.x, location.y - yDelta);
                    case 5 -> setMoveFrame(5, location.x, location.y + yDelta);
                    case 6 -> setMoveFrame(6, location.x, location.y);
                    case 7 -> setMoveFrame(7, location.x, location.y);
                    default -> {
                    }
                }
            }
            case 4, 12 -> {
                int xDelta = dir == 4 ? 8 : -8;
                switch (((field39_0xa0 + ((field39_0xa0 >> 31) & 0x1F)) >> 5) & 7) {
                    case 0 -> setMoveFrame(0, location.x, location.y);
                    case 1, 2 ->
                            setMoveFrame(Math.min(((field39_0xa0 + ((field39_0xa0 >> 31) & 0x1F)) >> 5) & 7, 2), location.x - xDelta, location.y);
                    case 3 -> setMoveFrame(3, location.x + xDelta, location.y);
                    case 4 -> setMoveFrame(4, location.x, location.y);
                    case 5 -> setMoveFrame(5, location.x, location.y);
                    case 6 -> setMoveFrame(6, location.x + xDelta * 2, location.y);
                    case 7 -> setMoveFrame(7, location.x + xDelta, location.y);
                    default -> {
                    }
                }
            }
            default -> {
                int xDelta = dir < 8 ? 8 : -8;
                int yDelta = (dir == 6 || dir == 10) ? 8 : -8;
                switch ((field39_0xa0 / 0x19) & 7) {
                    case 0 -> setMoveFrame(0, location.x, location.y);
                    case 1 -> setMoveFrame(1, location.x - xDelta, location.y - yDelta);
                    case 2 -> setMoveFrame(2, location.x, location.y);
                    case 3 -> setMoveFrame(3, location.x + xDelta * 2, location.y + yDelta * 2);
                    case 4 -> setMoveFrame(4, location.x, location.y);
                    case 5 -> setMoveFrame(5, location.x, location.y);
                    case 6 -> setMoveFrame(6, location.x + xDelta, location.y + yDelta);
                    case 7 -> setMoveFrame(7, location.x + xDelta, location.y + yDelta);
                    default -> {
                    }
                }
            }
        }
    }

    /**
     * Native support for the rotating-action branch in `CUnit::AdvanceMapObjectState @0046617C`.
     * Fully ported.
     */
    protected final void advanceRotateAction() {
        int delta = actionDir * 0x10 - field44_0xc4;
        if (delta < -0x7F) {
            delta += 0x100;
        }
        if (delta > 0x80) {
            delta -= 0x100;
        }
        field44_0xc4 += delta / actionSegments;
        if (field44_0xc4 < 0) {
            field44_0xc4 += 0x100;
        }
        dir = field44_0xc4 >> 4;
        actionPhase++;
        location2.x = location.x;
        location2.y = location.y;
    }

    /**
     * Native support for move-frame and location2 updates in `CUnit::AdvanceMapObjectState @0046617C`.
     * Fully ported.
     */
    protected final void setMoveFrame(int frame, int x, int y) {
        if (field10_0x34 == frame) {
            return;
        }
        field10_0x34 = frame;
        location2.x = x;
        location2.y = y;
    }

    /**
     * Native support for the `NOTIFY_MAP_CONTEXT_CHANGED` fan-out in `CUnit::AdvanceMapObjectState @0046617C`.
     * Fully ported.
     */
    protected final void notifySelectedUnitPanels() {
        Globals.mainWindow.pSelectionInfoPanelVisualObject.onMessage(MessageCodes.NOTIFY_MAP_CONTEXT_CHANGED, 0, 0);
        Globals.mainWindow.pSideStatusVisualObject.onMessage(MessageCodes.NOTIFY_MAP_CONTEXT_CHANGED, 0, 0);
    }

    /**
     * Native support for CUnit::PlaySpellCastSound @00467DA8 and CUnit::PlayAttackSound @00467CD2.
     * Full support port at the modeled audio backend boundary. Native indexes the shared SFX array directly and only
     * skips playback when the resolved Sound pointer is null.
     */
    protected final void playSoundEffect(int soundId) {
        Sound sound = SoundManager.SFX_SOUNDS.get(soundId);
        playPositionedSound(sound, Globals.soundPreferences.sfxVolume);
    }

    /**
     * vtbl +0x54: CUnit::InitializeUnitVisualState @00462FEF.
     * Full port. Native initializes the vtable-created visual unit identity, location, facing, phase, HP, resource pointer, type mirror, and clears unit flags.
     */
    public void initializeUnitVisualState(
            int objectId,
            int unitType,
            int x,
            int y,
            int unitZ,
            CPlayer cPlayer,
            int direction,
            int animationPhase,
            int speed,
            int hp
    ) {
        m_id = objectId & 0xFFFF;
        location.x = x;
        location.y = y;
        location2.x = location.x;
        location2.y = location.y;
        z = unitZ;
        dir = direction;
        phase = animationPhase;
        this.speed = (short) speed;
        HP = (short) hp;
        type = unitType;
        field8_0x28 = type;
        this.cPlayer = cPlayer;
        unitFlags = 0;
    }

    /**
     * vtbl +0x58: CUnit::SpawnAttackProjectile @00466E18.
     * Fully ported at the Java transient-object map boundary. Native creates a `CProjectile`, seeds shoot-offset
     * location/target/resource/travel state, refreshes map-derived fields, and inserts it into
     * `MapVisualObject::m_ObjectMap2`.
     */
    protected void spawnAttackProjectile() {
        CGameObject target = pMapVisualObject.getObjectByToken(actionTarget);
        if (target == null) {
            return;
        }
        CUnitInfo info = getUnitInfo();

        CProjectile projectile = createProjectile();
        projectile.type = info.m_ProjectileID;
        int offsetIndex = (dir - 8) & 0x0E;
        projectile.location.x = location.x + (info.m_CenterX - getShootOffset(info, offsetIndex)) * -8;
        projectile.location.y = location.y + (info.m_CenterY - getShootOffset(info, offsetIndex + 1)) * -8;
        projectile.location2.x = projectile.location.x;
        projectile.location2.y = projectile.location.y;
        projectile.actionTarget = actionTarget;
        projectile.cPlayer = cPlayer;
        int dx = location.x - target.location.x;
        int dy = location.y - target.location.y;
        projectile.actionSegments = (int) Math.sqrt((double) dy * dy + (double) dx * dx) / 200;
        projectile.action = 1;
        projectile.actionPhase = 0;
        projectile.refreshMapDerivedState();
        pMapVisualObject.addTransientObject(projectile);
    }

    /**
     * vtbl +0x5C: CUnit::SpawnSpellProjectile @0046707E.
     * Fully ported at the Java transient-object map boundary. Native creates a spell `CProjectile`, seeds
     * source/target/travel state, copies the action-target list at offset 0xB0, and inserts it into
     * `MapVisualObject::m_ObjectMap2`.
     */
    protected void spawnSpellProjectile() {
        CGameObject target = null;
        if (actionTarget != 0) {
            target = pMapVisualObject.getObjectByToken(actionTarget);
            if (target == null) {
                return;
            }
        }
        CUnitInfo info = getUnitInfo();

        CProjectile projectile = createProjectile();
        projectile.type = actionSpell;
        int offsetIndex = (dir - 8) & 0x0E;
        projectile.actionDir = (byte) dir;
        projectile.dir = dir;
        if (info.m_ShootOffsets.isEmpty() || actionSpell == 0x36) {
            projectile.location.x = (centerWorldX8 - info.m_CenterX) + info.m_SelectionRect.width() / 2;
            projectile.location.y = (centerWorldY8 - info.m_CenterY) + info.m_SelectionRect.height() / 2;
        } else {
            projectile.location.x = centerWorldX8 + (info.m_CenterX - getShootOffset(info, offsetIndex)) * -8;
            projectile.location.y = centerWorldY8 + (info.m_CenterY - getShootOffset(info, offsetIndex + 1)) * -8;
        }
        projectile.location2.x = projectile.location.x;
        projectile.location2.y = projectile.location.y;
        projectile.actionTarget = actionTarget;
        projectile.cPlayer = cPlayer;
        if (actionTarget == 0) {
            projectile.actionX = actionX;
            projectile.actionY = actionY;
            projectile.actionZ = actionZ;
        } else {
            projectile.actionX = target.centerWorldX8;
            projectile.actionY = target.centerWorldY8;
            projectile.actionZ = target.z;
            projectile.actionDir = (byte) resolveDirectionToPoint(actionX, actionY);
        }
        projectile.actionTargets.addAll(actionTargets);
        int dx = projectile.actionX - location.x;
        int dy = projectile.actionY - location.y;
        int distance = (int) Math.sqrt((double) dy * dy + (double) dx * dx);
        projectile.actionSegments = spellProjectileSegmentCount(projectile.type, distance);
        projectile.action = 1;
        projectile.actionPhase = 0;
        projectile.refreshMapDerivedState();
        pMapVisualObject.addTransientObject(projectile);

        if (projectile.type == 0x36) {
            CProjectile secondaryProjectile = new CProjectile(projectile);
            secondaryProjectile.location.x = (secondaryProjectile.actionX - info.m_CenterX) + info.m_SelectionRect.width() / 2;
            secondaryProjectile.location.y = (secondaryProjectile.actionY - info.m_CenterY) + info.m_SelectionRect.height() / 2;
            secondaryProjectile.z = secondaryProjectile.actionZ;
            secondaryProjectile.refreshMapDerivedState();
            pMapVisualObject.addTransientObject(secondaryProjectile);
        }
    }

    /**
     * vtbl +0x60: CUnit::PlaySpellCastSound @00467DA8.
     * Full port at the modeled audio backend boundary. Native resolves `actionSpell + 500` through the shared SFX
     * table and Java plays it from this unit's map source position.
     */
    protected void playSpellCastSound() {
        playSoundEffect(actionSpell + 500);
    }

    /**
     * vtbl +0x64: CUnit::PlayAttackSound @00467CD2.
     * Full port at the modeled audio backend boundary. Native resolves `m_SoundIDs[0]`, skips zero sound ids, and
     * Java plays it from this unit's map source position.
     */
    protected void playAttackSound() {
        playUnitTypeSoundEffect(0);
    }

    /**
     * Native support extracted from CUnit::SpawnAttackProjectile @00466E18 and CUnit::SpawnSpellProjectile @0046707E.
     * Partial port. Java creates the modeled projectile object and copies the map owner pointer that the native constructor receives later through explicit field writes.
     */
    protected final CProjectile createProjectile() {
        CProjectile projectile = new CProjectile();
        projectile.pMapVisualObject = pMapVisualObject;
        return projectile;
    }

    /**
     * Native support extracted from CUnit::SpawnAttackProjectile @00466E18 and CUnit::SpawnSpellProjectile @0046707E.
     * Fully ported. Native indexes `CUnitInfo::m_ShootOffsets` directly with the recovered direction offset.
     */
    protected final int getShootOffset(CUnitInfo info, int index) {
        return info.m_ShootOffsets.get(index);
    }

    /**
     * Native support extracted from CUnit::SpawnSpellProjectile @0046707E.
     * Full port. Native chooses spell projectile travel duration from projectile type and source-target distance.
     */
    protected final int spellProjectileSegmentCount(int projectileType, int distance) {
        return switch (projectileType) {
            case 10, 0x12, 0x28 -> distance / 200;
            case 0x0C -> distance / 0x180;
            case 0x1C, 0x1E -> 0x0D;
            case 0x36 -> 0x15;
            case 0x38, 0x3C -> 1;
            default -> 0;
        };
    }

    /**
     * vtbl +0x68: CUnit::PlayAttackVoice @004681C9.
     * Full port at the modeled audio backend boundary. Native selects from SoundPack::attack using attackCount,
     * throttles by timeGetTime, and Java plays it from this unit's map source position.
     */
    public void playAttackVoice() {
        SoundPack soundPack = resolveVoiceSoundPack();
        playRandomVoiceLine(soundPack.attack, soundPack.attackCount, 3000);
    }

    /**
     * vtbl +0x6C: CUnit::PlayMoveVoice @004682A6.
     * Full port at the modeled audio backend boundary. Native selects from SoundPack::move using moveCount,
     * throttles by timeGetTime, and Java plays it from this unit's map source position.
     */
    public void playMoveVoice() {
        SoundPack soundPack = resolveVoiceSoundPack();
        playRandomVoiceLine(soundPack.move, soundPack.moveCount, 3000);
    }

    /**
     * vtbl +0x70: CUnit::PlaySwarmVoice @00468383.
     * Full port at the modeled audio backend boundary. Native selects from SoundPack::swarm using swarmCount,
     * throttles by timeGetTime, and Java plays it from this unit's map source position.
     */
    public void playSwarmVoice() {
        SoundPack soundPack = resolveVoiceSoundPack();
        playRandomVoiceLine(soundPack.swarm, soundPack.swarmCount, 3000);
    }

    /**
     * vtbl +0x74: CUnit::PlayHurtResponseSound @00467E42.
     * Full port at the modeled audio backend boundary. Native preserves response `0` SFX-volume playback from unit
     * sound slot `1`, response `1..2` throttle, unit-type response sounds for non-hero units, and direct
     * SoundPack::easy/hard/dead voice pointers for character units.
     */
    public void playHurtResponseSound(int responseKind) {
        if (responseKind > 0 && responseKind < 3 && !refreshVoiceThrottle(0x5DC)) {
            return;
        }
        if (responseKind == 0) {
            playUnitTypeSoundEffect(1);
            return;
        }

        Sound sound = null;
        if ((unitFlags & 0x11) == 0 && Short.toUnsignedInt(serverID) > 0x17) {
            sound = resolveUnitTypeSound(responseKind + 1);
        } else {
            SoundPack soundPack = resolveVoiceSoundPack();
            sound = switch (responseKind) {
                case 1 -> soundPack.easy;
                case 2 -> soundPack.hard;
                case 3 -> soundPack.dead;
                default -> null;
            };
        }
        playVoiceSound(sound);
    }

    /**
     * vtbl +0x78: CUnit::PlayDefendVoice @0046853D.
     * Full port at the modeled audio backend boundary. Native plays the direct SoundPack::defend pointer after the
     * timeGetTime throttle; Java plays it from this unit's map source position.
     */
    public void playDefendVoice() {
        SoundPack soundPack = resolveVoiceSoundPack();
        playSingleVoiceLine(soundPack.defend, 3000);
    }

    /**
     * vtbl +0x7C: CUnit::PlayRetreatVoice @004685F4.
     * Full port at the modeled audio backend boundary. Native plays the direct SoundPack::retreat pointer after the
     * timeGetTime throttle; Java plays it from this unit's map source position.
     */
    public void playRetreatVoice() {
        SoundPack soundPack = resolveVoiceSoundPack();
        playSingleVoiceLine(soundPack.retreat, 3000);
    }

    /**
     * vtbl +0x80: CUnit::PlaySelectVoice @00468460.
     * Full port at the modeled audio backend boundary. Native selects from SoundPack::select using selectCount,
     * throttles by timeGetTime, and Java plays it from this unit's map source position.
     */
    public void playSelectVoice() {
        SoundPack soundPack = resolveVoiceSoundPack();
        playRandomVoiceLine(soundPack.select, soundPack.selectCount, 2000);
    }

    /**
     * vtbl +0x84: CUnit::PlayPickupVoice @004686AB.
     * Full port at the modeled audio backend boundary. Native plays the direct SoundPack::pickup pointer after the
     * timeGetTime throttle; Java plays it from this unit's map source position.
     */
    public void playPickupVoice() {
        SoundPack soundPack = resolveVoiceSoundPack();
        playSingleVoiceLine(soundPack.pickup, 3000);
    }

    /**
     * vtbl +0x88: CUnit::RenderEquipmentPortrait @004688F7.
     * Fully ported at the modeled bitmap-surface boundary. Java preserves the native composition order, optional
     * mirror, mask palette pointer install, dirty-flag clear, and attack/spell action reset. Native writes the mirrored
     * portrait to a temporary BMP cache; Java keeps the same keyed payload in memory to avoid stale temp-file reloads.
     */
    public void renderEquipmentPortrait(String outputPath, CBmp64k targetBitmap, CBmp256 maskBitmap) {
        String cacheKey = outputPath == null ? null : resolveEquipmentPortraitCacheKey(outputPath);
        if (cacheKey != null
                && (unitFlags & 0x08) == 0
                && EquipmentPortraitCache.restore(cacheKey, targetBitmap, maskBitmap)) {
            resetEquipmentPortraitActionState();
            return;
        }

        clearBitmapSurface(targetBitmap);
        clearBitmapSurface(maskBitmap);
        composeEquipmentPortrait(targetBitmap, maskBitmap);
        if (cacheKey != null) {
            cacheMirroredEquipmentPortrait(cacheKey, targetBitmap, maskBitmap);
        }
        if (maskBitmap != null) {
            initializeEquipmentPortraitMaskPalette(maskBitmap);
        }
        if (cacheKey != null) {
            unitFlags &= ~0x08;
        }
        resetEquipmentPortraitActionState();
    }

    /**
     * Native support extracted from CUnit::RenderEquipmentPortrait @004688F7 output branch.
     * Native leaves the target mirrored after CBmp64k::DumpBmp24PixelsWithMask @00425273; Java stores that same
     * bottom-up surface state in memory because CBmp64k draw paths read source rows bottom-up.
     */
    private static void cacheMirroredEquipmentPortrait(String cacheKey, CBmp64k targetBitmap, CBmp256 maskBitmap) {
        targetBitmap.mirrorY();
        EquipmentPortraitCache.store(cacheKey, targetBitmap, maskBitmap);
    }

    /**
     * Native support extracted from CUnit::RenderEquipmentPortrait @004688F7 cache-name handling.
     * Fully ported for Java callers that may pass either the native temp path or the native temp file name.
     */
    private static String resolveEquipmentPortraitCacheKey(String outputPath) {
        int slash = Math.max(outputPath.lastIndexOf('/'), outputPath.lastIndexOf('\\'));
        return slash < 0 ? outputPath : outputPath.substring(slash + 1);
    }

    /**
     * Native support extracted from CUnit::RenderEquipmentPortrait @004688F7 attack/spell action reset tail.
     * Fully ported.
     */
    private void resetEquipmentPortraitActionState() {
        if (action == 3 || action == 7 || action == 8) {
            action = 0;
            phase = 0;
            field10_0x34 = -1;
            actionPhase = 0;
            actionSegments = 0;
        }
    }

    /**
     * Native support extracted from CUnit::RenderEquipmentPortrait @004688F7 and palette pointer helper @0046DC60.
     * Fully ported at the Java field-assignment boundary; native helper @0046DC60 only stores the CGameBitmap +0x20
     * palette pointer before CGameBitmap::InitPalette @00424390.
     */
    static void initializeEquipmentPortraitMaskPalette(CBmp256 maskBitmap) {
        Palette256 palette = Palette256.create();
        RGB32[] entries = palette.data();
        for (int i = 0; i < entries.length; i++) {
            int component = (((i & 0x0F) * 0x08) - 0x80) & 0xFF;
            entries[i] = RGB32.from(component, component, component, 0);
        }
        maskBitmap.palette256 = palette;
        maskBitmap.initPalette(1, 1, 0);
    }

    /**
     * Native support extracted from CUnit::RenderEquipmentPortrait @004688F7 target-surface composition.
     */
    private void composeEquipmentPortrait(CGameBitmap targetBitmap, CGameBitmap maskBitmap) {
        CBmp64k target = (CBmp64k) targetBitmap;
        CBmp256 mask = maskBitmap == null ? null : (CBmp256) maskBitmap;
        String equipmentSet = resolveEquipmentPortraitSet();
        CSprite256[] primarySprites = new CSprite256[equipmentTokenEntries.length];
        CSprite256[] secondarySprites = new CSprite256[equipmentTokenEntries.length];

        loadEquipmentPortraitSprites(equipmentSet, primarySprites, secondarySprites);
        drawHorsePortraitBase(target);
        drawFighterPortraitBack(target);
        CSprite256 bodySprite = loadEquipmentPortraitSprite(Resources.path(GRAPHICS, "equipment", equipmentSet, field8_0x28 + ".256"));
        boolean twoHandedWeapon = equipmentTokenEntries[0] != null && isTwoHandedWeapon(equipmentTokenEntries[0]);
        if ((unitFlags & 0x02) == 0) {
            composeFighterEquipmentPortrait(target, mask, bodySprite, primarySprites, secondarySprites, twoHandedWeapon);
        } else {
            composeMageEquipmentPortrait(target, mask, bodySprite, primarySprites, secondarySprites);
        }
    }

    /**
     * Native support extracted from CUnit::RenderEquipmentPortrait @004688F7 unit flag equipment-directory switch.
     */
    private String resolveEquipmentPortraitSet() {
        return switch (unitFlags & 0x06) {
            case 0x02 -> "mmage";
            case 0x04 -> "ffighter";
            case 0x06 -> "fmage";
            default -> "mfighter";
        };
    }

    /**
     * Native support extracted from CUnit::RenderEquipmentPortrait @004688F7 primary/secondary equipment sprite loads.
     */
    private void loadEquipmentPortraitSprites(String equipmentSet, CSprite256[] primarySprites, CSprite256[] secondarySprites) {
        for (int slotIndex = 0; slotIndex < equipmentTokenEntries.length; slotIndex++) {
            TokenEntry token = equipmentTokenEntries[slotIndex];
            if (token == null) {
                continue;
            }

            String resourceName = token.getEquipmentPortraitResourceName() + ".256";
            primarySprites[slotIndex] = loadEquipmentPortraitSprite(Resources.path(GRAPHICS, "equipment", equipmentSet, "primary", resourceName));
            if (shouldLoadSecondaryEquipmentPortraitSprite(slotIndex)) {
                secondarySprites[slotIndex] = loadEquipmentPortraitSprite(Resources.path(GRAPHICS, "equipment", equipmentSet, "secondary", resourceName));
            }
        }
    }

    /**
     * Native support extracted from CUnit::RenderEquipmentPortrait @004688F7 secondary equipment sprite slot gate.
     */
    private boolean shouldLoadSecondaryEquipmentPortraitSprite(int slotIndex) {
        return slotIndex == 3 || slotIndex == 8 || slotIndex == 9 || (slotIndex == 7 && (unitFlags & 0x02) != 0);
    }

    /**
     * Native support extracted from CUnit::RenderEquipmentPortrait @004688F7 CSprite256 construction and palette init.
     */
    private static CSprite256 loadEquipmentPortraitSprite(String resourcePath) {
        CSprite256 sprite = new CSprite256(resourcePath);
        sprite.initPalette(1, 1, 0);
        return sprite;
    }

    /**
     * Native support extracted from CUnit::RenderEquipmentPortrait @004688F7 horse backdrop branch.
     */
    private void drawHorsePortraitBase(CBmp64k target) {
        if (type <= 0x10 || type >= 0x16) {
            return;
        }
        CBmp64k horse = new CBmp64k(Resources.path(GRAPHICS, "infowindow", "horse.bmp"));
        copyPortraitBitmap(horse, target);
    }

    /**
     * Native support extracted from CUnit::RenderEquipmentPortrait @004688F7 fighter hero-back branch.
     */
    private void drawFighterPortraitBack(CBmp64k target) {
        if ((unitFlags & 0x01) == 0 || (unitFlags & 0x02) != 0) {
            return;
        }
        drawPortraitSprite((unitFlags & 0x04) == 0 ? GUI.sprHeroBackMale : GUI.sprHeroBackFemale, target);
    }

    /**
     * Native support extracted from CUnit::RenderEquipmentPortrait @004688F7 non-mage draw order.
     */
    private void composeFighterEquipmentPortrait(
            CBmp64k target,
            CBmp256 mask,
            CSprite256 bodySprite,
            CSprite256[] primarySprites,
            CSprite256[] secondarySprites,
            boolean twoHandedWeapon
    ) {
        drawPortraitSprite(bodySprite, target);
        drawEquipmentPortraitLayer(primarySprites[11], target, mask, 11);
        drawEquipmentPortraitLayer(primarySprites[10], target, mask, 10);
        drawEquipmentPortraitLayer(primarySprites[6], target, mask, 6);
        drawEquipmentPortraitLayer(primarySprites[3], target, mask, 3);
        drawEquipmentPortraitLayer(primarySprites[4], target, mask, 4);
        drawEquipmentPortraitLayer(primarySprites[8], target, mask, 8);
        drawEquipmentPortraitLayer(primarySprites[9], target, mask, 9);
        if (!twoHandedWeapon) {
            drawEquipmentPortraitLayer(primarySprites[0], target, mask, 0);
        }
        drawEquipmentPortraitLayer(primarySprites[7], target, mask, 7);
        drawEquipmentPortraitLayer(secondarySprites[3], target, mask, 3);
        drawEquipmentPortraitLayer(primarySprites[5], target, mask, 5);
        drawEquipmentPortraitLayer(secondarySprites[8], target, mask, 8);
        drawEquipmentPortraitLayer(secondarySprites[9], target, mask, 9);
        drawEquipmentPortraitLayer(primarySprites[twoHandedWeapon ? 0 : 1], target, mask, twoHandedWeapon ? 0 : 1);
    }

    /**
     * Native support extracted from CUnit::RenderEquipmentPortrait @004688F7 mage draw order.
     */
    private void composeMageEquipmentPortrait(
            CBmp64k target,
            CBmp256 mask,
            CSprite256 bodySprite,
            CSprite256[] primarySprites,
            CSprite256[] secondarySprites
    ) {
        drawEquipmentPortraitLayer(primarySprites[7], target, mask, 7);
        drawPortraitSprite(bodySprite, target);
        if (mask != null) {
            drawPortraitMask(bodySprite, mask, (byte) 0);
        }
        drawEquipmentPortraitLayer(primarySprites[11], target, mask, 11);
        drawEquipmentPortraitLayer(primarySprites[9], target, mask, 9);
        drawEquipmentPortraitLayer(secondarySprites[9], target, mask, 9);
        drawEquipmentPortraitLayer(primarySprites[3], target, mask, 3);
        drawEquipmentPortraitLayer(secondarySprites[3], target, mask, 3);
        drawEquipmentPortraitLayer(primarySprites[6], target, mask, 6);
        drawEquipmentPortraitLayer(primarySprites[4], target, mask, 4);
        drawEquipmentPortraitLayer(primarySprites[8], target, mask, 8);
        drawEquipmentPortraitLayer(primarySprites[0], target, mask, 0);
        drawEquipmentPortraitLayer(primarySprites[5], target, mask, 5);
        drawEquipmentPortraitLayer(secondarySprites[7], target, mask, 7);
    }

    /**
     * Native support extracted from CUnit::RenderEquipmentPortrait @004688F7 sprite draw plus hit-map color pass.
     */
    private static void drawEquipmentPortraitLayer(CSprite256 sprite, CBmp64k target, CBmp256 mask, int slotIndex) {
        drawPortraitSprite(sprite, target);
        if (mask != null) {
            drawPortraitMask(sprite, mask, (byte) (slotIndex + 1));
        }
    }

    /**
     * Native support extracted from CUnit::RenderEquipmentPortrait @004688F7 CGameBitmap::Draw calls on the target bitmap.
     */
    private static void drawPortraitSprite(CSprite256 sprite, CBmp64k target) {
        if (sprite == null) {
            return;
        }
        sprite.drawInto(target, 0, 0, 0, 0, false);
    }

    /**
     * Native support extracted from CUnit::RenderEquipmentPortrait @004688F7 CSprite256::DrawWithColor hit-map pass.
     * Java writes the modeled CBmp256 frame in top-down indexed-bitmap coordinates because
     * SelectionInfoPanelVisualObject::GetText @004AE232 and InnLeftPanelVisualObject::GetText @00495BFD sample that
     * raw hit-map coordinate space directly.
     */
    private static void drawPortraitMask(CSprite256 sprite, CBmp256 mask, byte color) {
        if (sprite == null) {
            return;
        }
        var frame = sprite.frames.getFirst();
        var maskFrame = mask.frames.getFirst();
        byte[] pixels = maskFrame.data();
        int maskWidth = maskFrame.xSize();
        int maskHeight = maskFrame.ySize();
        Rle8SpriteDecoder.decodeClipped(
                0,
                0,
                frame.xSize(),
                frame.ySize(),
                frame.data(),
                0,
                0,
                maskWidth,
                maskHeight,
                (x, y, paletteIndices, offset, count, stepX) -> {
                    int dest = y * maskWidth + x;
                    for (int i = 0; i < count; i++) {
                        pixels[dest] = color;
                        dest += stepX;
                    }
                }
        );
    }

    /**
     * Native support extracted from CUnit::RenderEquipmentPortrait @004688F7 CBmp64k horse bitmap draw path.
     */
    private static void copyPortraitBitmap(CBmp64k source, CBmp64k target) {
        int width = Math.min(source.surface.width(), target.surface.width());
        int height = Math.min(source.surface.height(), target.surface.height());
        source.drawRectTo(target, 0, 0, 0, 0, width, height);
    }

    /**
     * Native support extracted from Token::IsTwoHandedHeroPictureToken @00438DA7 two-handed hero-picture lookup.
     */
    private static boolean isTwoHandedWeapon(TokenEntry token) {
        int pictureIndex = token.getId() - 1;
        if (pictureIndex < 0) {
            return false;
        }
        String pictureName = get(HEROPICTURE, HeroPictureText.byIndex(pictureIndex));
        return switch (pictureName) {
            case "bowman", "archer", "xbowman", "axeman2h", "swordsman2h", "mage_st" -> true;
            default -> false;
        };
    }

    /**
     * Native support: CUnit::FUN_00468094 @00468094.
     * Full port. Native selects the character voice pack from `serverID`, hero class/gender flags, and first equipment-token presence.
     */
    protected final SoundPack resolveVoiceSoundPack() {
        int serverId = Short.toUnsignedInt(serverID);
        return switch (serverId) {
            case 1 -> SoundManager.XBOW;
            case 2 -> SoundManager.KNIGHT;
            case 3 -> SoundManager.DRUID;
            case 4 -> SoundManager.TROLL;
            case 5 -> SoundManager.ORC;
            case 6 -> SoundManager.MAGE;
            case 0x15 -> resolveHeroVoicePack();
            case 0x16 -> SoundManager.IGLES;
            case 0x17 -> SoundManager.DINA;
            default -> resolveGenericVoicePack();
        };
    }

    /**
     * Native support extracted from CUnit::FUN_00468094 @00468094.
     * Full port. Native maps the four hero voice packs by mage and female flags.
     */
    protected final SoundPack resolveHeroVoicePack() {
        boolean mage = (unitFlags & 0x02) != 0;
        boolean female = (unitFlags & 0x04) != 0;
        if (mage) {
            return female ? SoundManager.GALINEL : SoundManager.HILDARIUS;
        }
        return female ? SoundManager.SAGITA : SoundManager.ALDOR;
    }

    /**
     * Native support extracted from CUnit::FUN_00468094 @00468094.
     * Full port. Native maps generic voice packs by mage flag, female flag, and whether equipment slot `0` is populated.
     */
    protected final SoundPack resolveGenericVoicePack() {
        int genderIndex = (unitFlags & 0x04) == 0 ? 0 : 1;
        if ((unitFlags & 0x02) != 0) {
            return SoundManager.MAGE_PACKS[genderIndex];
        }
        if (equipmentTokenEntries[0] == null) {
            return SoundManager.PEASANT_PACKS[genderIndex];
        }
        return SoundManager.FIGHTER_PACKS[genderIndex];
    }

    /**
     * Native support extracted from the throttled CUnit voice callbacks @004681C9, @004682A6, @00468383, and @00468460.
     * Full support port. Native uses SoundPack's array count and Rand(count - 1) for the selected voice pointer.
     */
    protected final void playRandomVoiceLine(Sound[] sounds, int soundCount, int throttleMillis) {
        if (!refreshVoiceThrottle(throttleMillis)) {
            return;
        }
        if (soundCount == 0) {
            return;
        }
        playVoiceSound(sounds[Utils.randInclusive(soundCount - 1)]);
    }

    /**
     * Native support extracted from the single-line CUnit voice callbacks @0046853D, @004685F4, and @004686AB.
     * Full support port. Native plays the resolved direct SoundPack pointer after the shared voice throttle.
     */
    protected final void playSingleVoiceLine(Sound sound, int throttleMillis) {
        if (!refreshVoiceThrottle(throttleMillis)) {
            return;
        }
        playVoiceSound(sound);
    }

    /**
     * Native support extracted from CUnit voice callbacks @004681C9, @004682A6, @00468383, @00467E42, @0046853D, @004685F4, @00468460, and @004686AB.
     * Full support port. Native stores a DWORD `timeGetTime()` tick in field `0x1BC`; Java preserves the same
     * low-32-bit elapsed arithmetic.
     */
    protected final boolean refreshVoiceThrottle(int throttleMillis) {
        int now = (int) System.currentTimeMillis();
        int elapsed = now - lastVoicePlaybackTick;
        if (Integer.compareUnsigned(elapsed, throttleMillis) < 0) {
            return false;
        }
        lastVoicePlaybackTick = now;
        return true;
    }

    /**
     * Native support extracted from CUnit voice callbacks @004681C9, @004682A6, @00468383, @00467E42, @0046853D, @004685F4, @00468460, and @004686AB.
     * Native calls MapVisualObject::ComputeMapRelativeSoundPosition @0041B1F8 and adds `g_SoundPreferences.SpeechVolume`.
     */
    protected final void playVoiceSound(Sound sound) {
        playPositionedSound(sound, Globals.soundPreferences.speechVolume);
    }

    /**
     * Native support extracted from CUnit sound callbacks using MapVisualObject::ComputeMapRelativeSoundPosition @0041B1F8.
     */
    private void playPositionedSound(Sound sound, int volumeBase) {
        if (sound != null) {
            SoundSystem soundSystem = SoundSystem.get();
            pMapVisualObject.updateSoundSystemMapAudioView(soundSystem);
            byte priority = objectMapSoundPriority(soundSystem);
            playObjectMapSound(soundSystem, sound, volumeBase, priority, 0);
        }
    }

    /**
     * Native support extracted from CUnit::PlayHurtResponseSound @00467E42 unit-type `m_SoundIDs` lookups.
     * Full support port. Native skips zero sound ids, then indexes the shared SFX array directly.
     */
    protected final Sound resolveUnitTypeSound(int soundIndex) {
        int soundId = resolveUnitTypeSoundId(soundIndex);
        if (soundId == 0) {
            return null;
        }
        return SoundManager.SFX_SOUNDS.get(soundId);
    }

    /**
     * Native support extracted from CUnit::PlayHurtResponseSound @00467E42 and CUnit::PlayAttackSound @00467CD2.
     * Full support port. Native indexes `CUnitInfo::m_SoundIDs` directly.
     */
    protected final int resolveUnitTypeSoundId(int soundIndex) {
        return getUnitInfo().m_SoundIDs.get(soundIndex);
    }

    /**
     * Native support extracted from CUnit::PlayAttackSound @00467CD2 and response `0` branch of
     * CUnit::PlayHurtResponseSound @00467E42.
     */
    protected final void playUnitTypeSoundEffect(int soundIndex) {
        int soundId = resolveUnitTypeSoundId(soundIndex);
        if (soundId != 0) {
            playSoundEffect(soundId);
        }
    }

    /**
     * Native support extracted from CUnit::RenderEquipmentPortrait @004688F7 target bitmap clears.
     * Partial port. Native clears raw bitmap buffers; Java clears modeled RGB16 surfaces and frame bytes when present.
     */
    protected final void clearBitmapSurface(CGameBitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        if (bitmap.surface != null) {
            Arrays.fill(bitmap.surface.pixels(), RGB16.BLACK);
        }
        if (bitmap.frames != null) {
            for (var frame : bitmap.frames) {
                Arrays.fill(frame.data(), (byte) 0);
            }
        }
    }

    /**
     * Native support: death/bones progression state read in CUnit::DrawShadow, CUnit::TryOccupyMap, CUnit::OccupyMapCells, and CUnit::AdvanceMapObjectState.
     * not ported.
     */
    protected final int getDeathState() {
        return Byte.toUnsignedInt(field51_0x184);
    }

    /**
     * Native support: CUnit::findPackedEffectIndex @00460D4B.
     * Fully ported. Native scans `dwarr_130` and returns the first index whose packed high-word matches `effectType`.
     */
    public final int findPackedEffectIndex(int effectType) {
        for (int index = 0; index < dwarr_130.size(); index++) {
            if ((dwarr_130.get(index) >>> 16) == effectType) {
                return index;
            }
        }
        return -1;
    }

    /**
     * Native support: CUnit::DrawMinimap palette lookup @00465F3F.
     * Fully ported. Native resolves owner palette page 8 and reads color slot `0xA4`.
     */
    protected final short resolveMinimapMarkerColor() {
        CGamePalette ownerPalette = Palettes.unitGamePalettes.get(cPlayer.color);
        return ownerPalette
                .paletteData[8]
                .data()[0xA4]
                .val();
    }

}
