package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.column.MagicColumn;
import ua.millfreedom.rom2.model.column.SpellColumn;
import ua.millfreedom.rom2.model.enums.EffectId;
import ua.millfreedom.rom2.model.enums.SpellId;
import ua.millfreedom.rom2.model.spell.Spell;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.unit.UnitDirtyFlags;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static ua.millfreedom.rom2.model.EffectType.*;
import static ua.millfreedom.rom2.model.column.MagicColumn.*;
import static ua.millfreedom.rom2.model.column.SpellColumn.BOOK_COST;
import static ua.millfreedom.rom2.model.column.SpellColumn.SCROLL_COST;
import static ua.millfreedom.rom2.model.enums.EffectId.*;

public class Effect extends Token {
    private static final int EFFECT_KEY_HP_DELTA = 0x06;
    private static final int EFFECT_KEY_HUMANOID_DAMAGE = 0x0E;
    private static final int EFFECT_KEY_NON_HUMANOID_DAMAGE = 0x0F;
    private static final int EFFECT_KEY_OPPOSITE_A = 0x14;
    private static final int EFFECT_KEY_OPPOSITE_B = 0x1C;

    //0x3C
    public int id;
    //0x3D
    public int type;
    //0x40
    public final PolyValue mValue = new PolyValue();
    //0x44
    public Unit sourceUnit;

    /**
     * Native: Effect::New @0051CA4D.
     * Fully ported.
     */
    public Effect() {
        typeID = 0;
        id = EMPTY.id;
        type = PERMANENT;
        mValue.setFull(0);
        key = 0;
        sourceUnit = null;
    }

    /**
     * Native: Effect::Effect @0051CAA0.
     * Fully ported.
     */
    public Effect(String effectText) {
        Effect parsed = createFromString(effectText);
        typeID = 0;
        if (parsed == null) {
            id = EMPTY.id;
            type = PERMANENT;
            mValue.setFull(0);
            key = 0;
            return;
        }

        id = parsed.id;
        type = parsed.type;
        mValue.setFull(parsed.mValue.getFull());
        key = parsed.key;
        sourceUnit = null;
    }

    /**
     * Native: Effect::operator>> @0051CA31 (ReadObject path).
     * Fully ported.
     */
    public static Effect readFromArchive(CArchive ar) throws IOException {
        return ar.readObject(Effect.class);
    }

    /**
     * Native: ParseEffectsList @0051E78E.
     * Fully ported.
     */
    public static void parseEffectsList(String effectsText, List<Effect> effectsList) {
        String remaining = effectsText.trim();
        if (remaining.isEmpty()) {
            return;
        }
        remaining = remaining + ",";

        int commaPos = remaining.indexOf(',');
        while (commaPos != -1) {
            String effectToken = remaining.substring(0, commaPos).trim();
            Effect effect = createFromString(effectToken);
            if (effect != null) {
                effectsList.add(effect);
            }

            remaining = remaining.substring(commaPos + 1).trim();
            commaPos = remaining.indexOf(',');
        }
    }

    /**
     * Native: Effect::CreateFromString @0051E8FA.
     * Fully ported.
     */
    public static Effect createFromString(String effectText) {
        String source = effectText.trim().toLowerCase(Locale.ROOT);
        if (source.isEmpty()) {
            return null;
        }

        int equalsPos = source.indexOf('=');
        if (equalsPos < 0) {
            return null;
        }

        String effectName = source.substring(0, equalsPos).trim();
        EffectId effectId = findEffectIdByName(effectName);
        if (effectId == UNKNOWN) {
            return null;
        }

        String rhs = source.substring(equalsPos + 1).trim();
        int commaPos = rhs.indexOf(',');
        String firstValue = commaPos < 0 ? rhs : rhs.substring(0, commaPos);
        firstValue = firstValue.trim();
        String valueText = firstValue;
        String usageText = "";
        int colonPos = firstValue.indexOf(':');
        if (colonPos >= 0) {
            valueText = firstValue.substring(0, colonPos).trim();
            usageText = firstValue.substring(colonPos + 1).trim();
        }

        Effect effect = new Effect();
        effect.id = effectId.id;

        if (effectId == CAST_SPELL || effectId == TEACH_SPELL) {
            int spellId = findSpellIdByName(valueText);
            if (spellId < 0) {
                return null;
            }
            effect.mValue.setS1(spellId);
            if (colonPos >= 0) {
                if (effectId == CAST_SPELL) {
                    effect.mValue.setS2(parseUnsignedIntOrZero(usageText));
                    effect.type = PERMANENT;
                } else {
                    applyParsedUsage(effect, usageText);
                }
            }
            return effect;
        }

        if (effectId.isBetween(DAMAGE, DAMAGE_ASTRAL)) {
            parseMinMaxValue(effect, valueText);
        } else {
            effect.mValue.setFull(parseSignedIntOrZero(valueText));
        }

        applyParsedUsage(effect, usageText);
        if (colonPos < 0 && !effectId.isBetween(DAMAGE, DAMAGE_ASTRAL)) {
            effect.price = effect.mValue.getFull() * 50;
        }
        return effect;
    }

    /**
     * vtbl +0x38: Effect::updateOnTick @0051CC9F.
     * Fully ported.
     */
    public void updateOnTick(Unit target) {
        if (!hasDurationOrContinuous()) {
            return;
        }

        if ((type & CONTINUOUS) != 0 && ((mValue.getS2() & 7) == 0)) {
            target.changedValues = UnitDirtyFlags.NONE.value;
            applyOnAdd(target);
            emitNetUpdate(target);
        }

        int durationTicks = mValue.getS2();
        if (durationTicks <= 0x2580) {
            int next = (durationTicks - 1) & 0xFFFF;
            mValue.setS2(next);

            if (next == 0) {
                if ((type & CONTINUOUS) == 0) {
                    target.changedValues = UnitDirtyFlags.NONE.value;
                    applyOnRemove(target);
                    emitNetUpdate(target);
                }

                target.effectKeyFlags &= ~bitMaskForEffectKey();
                if (effectKey() != 0) {
                    notifyEffectRemoved(this, target);
                }
                type |= EXPIRED;
            }
        }
    }

    /**
     * vtbl +0x3C: Effect::applyToTarget @0051CE12.
     * Fully ported.
     */
    public void applyToTarget(Token target) {
        Unit targetUnit = (Unit) target;

        targetUnit.changedValues = UnitDirtyFlags.NONE.value;

        if (hasDurationOrContinuous()
                && (effectKey() == EFFECT_KEY_OPPOSITE_A || effectKey() == EFFECT_KEY_OPPOSITE_B)) {
            Effect opposite = findOppositeByKey(targetUnit.effects);
            if (opposite != null) {
                targetUnit.effectKeyFlags &= ~opposite.bitMaskForEffectKey();
                opposite.applyOnRemove(targetUnit);
                notifyEffectRemoved(opposite, targetUnit);
                targetUnit.effects.remove(opposite);
                emitNetUpdate(targetUnit);
                return;
            }
        }

        if (!hasDurationOrContinuous()) {
            applyOnAdd(targetUnit);
        } else {
            Effect existing = findByKey(targetUnit.effects);
            if (existing == null) {
                Effect cloned = new Effect().copyFrom(this);
                if (cloned.effectKey() != EFFECT_KEY_HP_DELTA) {
                    cloned.applyOnAdd(targetUnit);
                }
                targetUnit.effects.add(cloned);
                targetUnit.effectKeyFlags |= bitMaskForEffectKey();
                if (effectKey() != 0) {
                    notifyEffectAdded(this, targetUnit);
                }
            } else {
                existing.applyOnRemove(targetUnit);
                existing.mValue.setS1(Math.max(existing.mValue.getS1Signed(), mValue.getS1Signed()));
                existing.mValue.setS2(Math.max(existing.mValue.getS2(), mValue.getS2()));
                if (existing.effectKey() != EFFECT_KEY_HP_DELTA) {
                    existing.applyOnAdd(targetUnit);
                }
            }
        }

        emitNetUpdate(targetUnit);
    }

    /**
     * vtbl +0x40: Effect::applyOnAdd @0051D175.
     * Fully ported.
     */
    public void applyOnAdd(Unit target) {
        int key = effectKey();
        if (key == EFFECT_KEY_HP_DELTA) {
            int damage = -mValue.getS1Signed();
            int hpResist = target.unitStatData.protections[2];
            if (hpResist != 0) {
                damage = (int) ((damage * (100.0d - hpResist)) / 100.0d + 0.5d);
            }
            if (damage != 0) {
                target.m_nHP = (short) (target.m_nHP - damage);
                if (sourceUnit != null) {
                    if (!sourceUnit.isDying()) {
                        sourceUnit.awardDamageSkillProgress(target, damage, EFFECT_KEY_HP_DELTA);
                    } else {
                        sourceUnit = null;
                    }
                }
                notifyHpDelta(target);
            }
            return;
        }

        if (key == EFFECT_KEY_NON_HUMANOID_DAMAGE) {
            if (target.isHumanoidToken() == 0) {
                setPackedSightRange(target, getPackedSightRange(target) - (mValue.getS1Signed() << 8));
                target.changedValues |= UnitDirtyFlags.SIGHT_RANGE.value;
            }
            return;
        }

        if (key == EFFECT_KEY_HUMANOID_DAMAGE) {
            if (target.isHumanoidToken() != 0) {
                target.mModifiers.packedSightRange =
                        (short) (target.mModifiers.packedSightRange + (mValue.getS1Signed() << 8));
                setPackedSightRange(target, getPackedSightRange(target) + (mValue.getS1Signed() << 8));
                target.changedValues |= UnitDirtyFlags.SIGHT_RANGE.value;
            }
            return;
        }

        applyScaledModifier(target, 1);
    }

    /**
     * vtbl +0x44: Effect::applyOnRemove @0051D337.
     * Fully ported.
     */
    public void applyOnRemove(Unit target) {
        int key = effectKey();
        if (key == EFFECT_KEY_NON_HUMANOID_DAMAGE) {
            if (target.isHumanoidToken() == 0) {
                setPackedSightRange(target, getPackedSightRange(target) + (mValue.getS1Signed() << 8));
                target.changedValues |= UnitDirtyFlags.SIGHT_RANGE.value;
            }
            return;
        }

        if (key == EFFECT_KEY_HUMANOID_DAMAGE) {
            if (target.isHumanoidToken() != 0) {
                target.mModifiers.packedSightRange =
                        (short) (target.mModifiers.packedSightRange - (mValue.getS1Signed() << 8));
                setPackedSightRange(target, getPackedSightRange(target) - (mValue.getS1Signed() << 8));
                target.changedValues |= UnitDirtyFlags.SIGHT_RANGE.value;
            }
            return;
        }

        if (key != EFFECT_KEY_HP_DELTA) {
            applyScaledModifier(target, -1);
        }
    }

    /**
     * vtbl +0x48: Effect::applyScaledModifier @0051D436.
     * Fully ported.
     */
    public void applyScaledModifier(Unit target, int scale) {
        if (target.mModifiers.attack > 0x18) {
            target.mModifiers.attack = 0;
        }

        int delta = ((type & (DURATION | CONTINUOUS | CHARGES)) == 0)
                ? scale * mValue.getFull()
                : scale * mValue.getS1Signed();

        int effectIndex = effectId();
        EffectId effect = effectKind();

        switch (effect) {
            case EMPTY, PRICE, CAST_SPELL -> {
            }
            case BODY -> {
                target.m_nBody = (short) Math.min(target.m_nBody + (short) delta, 100);
                if ((type & SINGLE_USE) == 0) {
                    target.mModifiers.body = (byte) (target.mModifiers.body + delta);
                }
                target.changedValues |= UnitDirtyFlags.BODY.value;
            }
            case MIND -> {
                target.m_nMind = (short) Math.min(target.m_nMind + (short) delta, 100);
                if ((type & SINGLE_USE) == 0) {
                    target.mModifiers.mind = (byte) (target.mModifiers.mind + delta);
                }
                target.changedValues |= UnitDirtyFlags.MIND.value;
            }
            case REACTION -> {
                target.m_nReaction = (short) Math.min(target.m_nReaction + (short) delta, 100);
                if ((type & SINGLE_USE) == 0) {
                    target.mModifiers.reaction = (byte) (target.mModifiers.reaction + delta);
                }
                target.changedValues |= UnitDirtyFlags.REACTION.value;
            }
            case SPIRIT -> {
                target.m_nSpirit = (short) Math.min(target.m_nSpirit + (short) delta, 100);
                if ((type & SINGLE_USE) == 0) {
                    target.mModifiers.spirit = (byte) (target.mModifiers.spirit + delta);
                }
                target.changedValues |= UnitDirtyFlags.SPIRIT.value;
            }
            case HEALTH -> {
                target.m_nHP = (short) Math.min(target.m_nHP + delta, target.m_nMaxHP);
                target.changedValues |= UnitDirtyFlags.HP.value;
            }
            case HEALTH_MAX -> {
                target.m_nHP = (short) (target.m_nHP + (short) delta);
                target.mModifiers.maxHp = (short) (target.mModifiers.maxHp + delta);
                target.changedValues |= UnitDirtyFlags.MAX_HP.value;
            }
            case HEALTH_REGENERATION -> target.mModifiers.hpRegen = (short) (target.mModifiers.hpRegen + delta);
            case MANA -> {
                target.m_nMP = (short) Math.min(target.m_nMP + delta, target.m_nMaxMP);
                target.changedValues |= UnitDirtyFlags.MP.value;
            }
            case MANA_MAX -> {
                if (target.isMageClass()) {
                    target.m_nMP = (short) (target.m_nMP + (short) delta);
                    target.mModifiers.maxMp = (short) (target.mModifiers.maxMp + delta);
                    target.changedValues |= UnitDirtyFlags.MAX_MP.value;
                }
            }
            case MANA_REGENERATION -> {
                if (target.isMageClass()) {
                    target.mModifiers.mpRegen = (short) (target.mModifiers.mpRegen + delta);
                }
            }
            case TO_HIT -> {
                target.mModifiers.skillMods.toHit = (short) (target.mModifiers.skillMods.toHit + delta);
                target.changedValues |= UnitDirtyFlags.TO_HIT.value;
            }
            case DAMAGE_MIN, DAMAGE, DAMAGE_BONUS -> {
                target.mModifiers.skillMods.skillDamageType0And3Min =
                        (byte) (target.mModifiers.skillMods.skillDamageType0And3Min + delta);
                target.changedValues |= UnitDirtyFlags.DAMAGE_PROFILE.value;
            }
            case DAMAGE_MAX -> {
                target.mModifiers.skillMods.skillDamageType0And3Modifier =
                        (byte) (target.mModifiers.skillMods.skillDamageType0And3Modifier + delta);
                target.changedValues |= UnitDirtyFlags.DAMAGE_PROFILE.value;
            }
            case DEFENCE -> {
                if (target.isHumanoidToken() != 0) {
                    target.mModifiers.statMods.defence = (short) (target.mModifiers.statMods.defence + (short) delta);
                } else {
                    target.unitStatData.defence = (short) Math.max(target.unitStatData.defence + (short) delta, 0);
                }
                target.changedValues |= UnitDirtyFlags.DEFENCE.value;
            }
            case ABSORBTION -> {
                if (target.isHumanoidToken() != 0) {
                    target.mModifiers.statMods.absorbtion =
                            (short) (target.mModifiers.statMods.absorbtion + (short) delta);
                    if (target.mModifiers.statMods.absorbtion < 0) {
                        target.mModifiers.statMods.absorbtion = 0;
                    }
                } else {
                    target.unitStatData.absorbtion = (short) (target.unitStatData.absorbtion + (short) delta);
                }
                target.changedValues |= UnitDirtyFlags.ABSORBTION.value;
            }
            case SPEED -> {
                if (target.isHumanoidToken() != 0) {
                    target.mModifiers.attack = (short) (target.mModifiers.attack + delta);
                } else {
                    target.speed = (short) (target.speed + (short) delta);
                }
                target.changedValues |= UnitDirtyFlags.SPEED.value;
            }
            case ROTATION_SPEED -> target.movementState.addRotationSpeed(delta);
            case SCAN_RANGE -> {
                // Native updates the high byte of packed sight range (Unit +0xA4 / Modifiers +0x10) via `delta << 8`.
                if (target.isHumanoidToken() != 0) {
                    target.mModifiers.packedSightRange =
                            (short) (target.mModifiers.packedSightRange + (delta << 8));
                } else {
                    setPackedSightRange(target, getPackedSightRange(target) + (delta << 8));
                }
                target.changedValues |= UnitDirtyFlags.SIGHT_RANGE.value;
            }
            case PROTECTION_0, PROTECTION_FIRE, PROTECTION_WATER, PROTECTION_AIR, PROTECTION_EARTH,
                 PROTECTION_ASTRAL -> {
                int statIndex = effectIndex - PROTECTION_0.id;
                short next = (short) (target.mModifiers.statMods.protections[statIndex] + (short) delta);
                target.mModifiers.statMods.protections[statIndex] = (short) Math.max(next, 0);
                target.changedValues |= UnitDirtyFlags.PROTECTIONS.value;
            }
            case FIGHTER_SKILL_0, SKILL_BLADE, SKILL_AXE, SKILL_BLUDGEON, SKILL_PIKE, SKILL_SHOOTING -> {
                if (target.isNonMageClass()) {
                    int skillIndex = effectIndex - FIGHTER_SKILL_0.id;
                    target.mModifiers.skillMods.skillLevels[skillIndex] =
                            (short) (target.mModifiers.skillMods.skillLevels[skillIndex] + delta);
                }
                if (effect != FIGHTER_SKILL_0) {
                    target.changedValues |= UnitDirtyFlags.FIGHTER_SKILLS.value;
                }
            }
            case MAGE_SKILL_0, SKILL_FIRE, SKILL_WATER, SKILL_AIR, SKILL_EARTH, SKILL_ASTRAL -> {
                if (target.isMageClass()) {
                    int skillIndex = effectIndex - MAGE_SKILL_0.id;
                    target.mModifiers.skillMods.skillLevels[skillIndex] =
                            (short) (target.mModifiers.skillMods.skillLevels[skillIndex] + delta);
                }
                if (effect != MAGE_SKILL_0) {
                    target.changedValues |= UnitDirtyFlags.SKILLS.value;
                }
            }
            case ITEM_LORE, MAGIC_LORE, CREATURE_LORE ->
                    Globals.gameServer.pushMessage("Effect affects not implemented yet.");
            case TEACH_SPELL -> {
                if (target.spellbook != null && target.spellbook.find(delta) == null) {
                    target.spellbook.setAt(delta, new Spell((byte) delta));
                }
                target.changedValues |= UnitDirtyFlags.SPELLBOOK.value;
            }
            case DAMAGE_FIRE, DAMAGE_WATER, DAMAGE_AIR, DAMAGE_EARTH, DAMAGE_ASTRAL -> {
                target.mModifiers.skillMods.skillDamageType2ProtectionIndex = (byte) (effectIndex - DAMAGE.id);
                target.mModifiers.skillMods.skillDamageType2Min = (byte) mValue.getB1();
                target.mModifiers.skillMods.skillDamageType2Modifier = (byte) mValue.getB2();
                target.changedValues |= UnitDirtyFlags.DAMAGE_PROFILE.value;
            }
            default -> Globals.gameServer.pushMessage("Oops! unknown Effect case.");
        }

        target.recalculateDerivedStats();
    }

    /**
     * vtbl +0x4C: Effect::recalculatePrice @0051F48D.
     * Fully ported.
     */
    public int recalculatePrice() {
        if (isA(CAST_SPELL)) {
            int spellValue = getSpellValue(SCROLL_COST);
            double durationFactor = (mValue.getS2Signed() / 30.0d) + 1.0d;
            double exponent = Math.log(durationFactor) / Math.log(1.2d);
            price = (int) (Math.pow(2.0d, exponent) * (spellValue * 10.0d));
        } else {
            price = 0;
        }
        return price;
    }

    /**
     * vtbl +0x50: Effect::computeValueFromPrice @0051F54B.
     * Fully ported.
     */
    public int computeValueFromPrice(double inputValue) {
        int out = 1;
        if (!hasDurationOrContinuous()) {
            if (isA(CAST_SPELL)) {
                int divider = getSpellValue(BOOK_COST);
                out = (int) ((inputValue * 10.0d) / divider);
                if (out > 100) {
                    out = 100;
                }
                if (out < 1) {
                    out = -1;
                }
            } else {
                int minValue = getMagicValue(AFFECT_MIN);
                int maxValue = getMagicValue(AFFECT_MAX);
                int baseValue = getMagicValue(COST_MP);

                double scaled = Math.log(inputValue / (baseValue * 500.0d)) / Math.log(20.0d);
                out = (int) (scaled * maxValue + 0.5d);
                if (out > maxValue) {
                    out = maxValue;
                }
                if (out < minValue) {
                    out = -1;
                }
            }
        }
        return out;
    }

    /**
     * Native: Effect::computeValueFromPrice @0051F7D3.
     * Fully ported.
     */
    public int computeValueFromPrice(int priceBudget, int magicCapacity) {
        int costMp = getMagicValue(COST_MP);
        int value = magicCapacity / costMp;
        if (isA(CAST_SPELL)) {
            int scrollCost = getSpellValue(SCROLL_COST);
            double ratio = priceBudget / (scrollCost * 10.0d);
            int duration = Math.log(ratio) / Math.log(2.0d) > 0.0d
                    ? (int) ((Math.pow(1.2d, Math.log(ratio) / Math.log(2.0d)) - 1.0d) * 30.0d)
                    : -1;
            value = Math.min(duration, value);
            value = Math.min(value, magicCapacity);
            value = Math.min(value, 100);
            return value < 0 ? -1 : value;
        }

        double rawValue = Math.log(priceBudget / (magicCapacity * 50.0d) - 1.0d) / Math.log(1.5d) * 70.0d / costMp;
        value = (int) Math.min(value, rawValue);
        int affectMin = getMagicValue(AFFECT_MIN);
        if (value < affectMin) {
            return -1;
        }
        int affectMax = getMagicValue(AFFECT_MAX);
        return Math.min(value, affectMax);
    }

    /**
     * vtbl +0x54: Effect::randomizeValue @0051F6AF.
     * Fully ported.
     */
    public void randomizeValue(int param) {
        EffectId effect = effectKind();
        int rolled = Utils.randInclusive(1,param);
        if (effect == CAST_SPELL) {
            mValue.setS2(rolled);
            if (mValue.getS2Signed() > 100) {
                mValue.setS2(100);
            }
            return;
        }

        if (effect.isBetween(DAMAGE_FIRE, DAMAGE_ASTRAL)) {
            if (param > 0xFF) {
                param = 0xFF;
            }
            mValue.setB1(Utils.randInclusive(1,param));
            int halfParam = param / 2;
            mValue.setB2(Utils.randInclusive(1,halfParam));
            return;
        }

        int minValue = getMagicValue(AFFECT_MIN);
        mValue.setFull(Math.max(rolled, minValue));
    }

    /**
     * Native: Effect::calculateScore @0051E318.
     * Fully ported.
     */
    public int calculateScore() {
        if (isA(EffectId.EMPTY)) {
            return 0;
        }
        EffectId effect = effectKind();
        int multiplier = getMagicValue(MagicColumn.COST_MP);

        if (effect == CAST_SPELL) {
            return mValue.getS1Signed() * multiplier;
        }
        if (effect.isBetween(DAMAGE_FIRE, DAMAGE_ASTRAL)) {
            return (mValue.getB1() + mValue.getB2()) * multiplier;
        }
        return mValue.getFull() * multiplier;
    }

    /**
     * Native: Effect::Effect(copy) @0051CB98.
     * Fully ported.
     */
    public Effect copyFrom(Effect source) {
        copyTokenStateFrom(source);
        id = source.id;
        type = source.type;
        mValue.setFull(source.mValue.getFull());
        sourceUnit = source.sourceUnit;
        return this;
    }

    /**
     * vtbl +0x08: Effect::serialize @0051C5ED.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        super.serialize(ar);

        if (!ar.isStoring()) {
            id = ar.readByte() & 0xFF;
            type = ar.readByte() & 0xFF;
            mValue.setFull(ar.readInt());
            // Effect::serialize reads a WORD at +0x0C after mValue.
            // In this port that maps to Token.key.
            key = ar.readUShort();
        } else {
            ar.writeByte(id);
            ar.writeByte(type);
            ar.writeInt(mValue.getFull());
            ar.writeShort((key & 0xFFFF));
        }
    }

    // not ported.
    public boolean isA(EffectId effectId) {
        return effectId != null && effectKind() == effectId;
    }

    // not ported.
    private int effectId() {
        return id & 0xFF;
    }

    // not ported.
    private EffectId effectKind() {
        return EffectId.fromId(effectId());
    }

    // not ported.
    private int effectKey() {
        return key & 0xFFFF;
    }

    /**
     * Native: Effect::hasDurationOrContinuous @0051CC53.
     * Fully ported.
     */
    public boolean hasDurationOrContinuous() {
        return (type & (DURATION | CONTINUOUS)) != 0;
    }

    /**
     * Native: Effect::isExpired @00542540.
     * Fully ported.
     */
    public boolean isExpired() {
        return (type & EXPIRED) != 0;
    }

    /**
     * Native: Effect::matchesInventoryIdentity @0051CBFB.
     * Fully ported.
     */
    public boolean matchesInventoryIdentity(Effect other) {
        return (id & 0xFF) == (other.id & 0xFF)
                && type == other.type
                && mValue.getFull() == other.mValue.getFull();
    }

    /**
     * Native: Effect::isDifferentFrom @00544180.
     * Fully ported.
     */
    public boolean isDifferentFrom(Effect other) {
        return !matchesInventoryIdentity(other);
    }

    /**
     * Native support extracted from Effect::applyToTarget @0051CE12.
     */
    private Effect findByKey(List<Effect> list) {
        int key = effectKey();
        for (Effect effect : list) {
            if (effect.effectKey() == key) {
                return effect;
            }
        }
        return null;
    }

    /**
     * Native support extracted from Effect::applyToTarget @0051CE12.
     */
    private Effect findOppositeByKey(List<Effect> list) {
        int key = effectKey();
        for (Effect effect : list) {
            int effectKey = effect.effectKey();
            if ((effectKey == EFFECT_KEY_OPPOSITE_A && key == EFFECT_KEY_OPPOSITE_B)
                    || (effectKey == EFFECT_KEY_OPPOSITE_B && key == EFFECT_KEY_OPPOSITE_A)) {
                return effect;
            }
        }
        return null;
    }

    // not ported.
    private int bitMaskForEffectKey() {
        return 1 << (effectKey() & 0x1F);
    }

    /**
     * Native support extracted from Effect::applyOnAdd @0051D175,
     * Effect::applyOnRemove @0051D337, and Effect::applyScaledModifier @0051D436.
     */
    private static int getPackedSightRange(Unit target) {
        return (target.sightFraction & 0xFF) | ((target.sightRange & 0xFF) << 8);
    }

    /**
     * Native support extracted from Effect::applyOnAdd @0051D175,
     * Effect::applyOnRemove @0051D337, and Effect::applyScaledModifier @0051D436.
     */
    private static void setPackedSightRange(Unit target, int packed) {
        target.sightFraction = packed & 0xFF;
        target.sightRange = (packed >>> 8) & 0xFF;
    }

    // not ported.
    public int getSpellValue(SpellColumn column) {
        return Globals.staticDataMgr.spells.get(mValue.getS1Signed()).getAttribute(column);
    }


    // not ported.
    public int getMagicValue(MagicColumn column) {
        return Globals.staticDataMgr.magic.get(effectId()).getAttribute(column);
    }

    /**
     * Native support extracted from Effect::CreateFromString @0051E8FA and ParseMinMax @0051F072.
     * Fully ported.
     */
    private static void parseMinMaxValue(Effect effect, String valueText) {
        int separatorPos = valueText.indexOf('-');
        if (separatorPos < 0) {
            return;
        }

        int minValue = parseSignedIntOrZero(valueText.substring(0, separatorPos));
        int maxValue = parseSignedIntOrZero(valueText.substring(separatorPos + 1));
        effect.mValue.setB1(minValue);
        effect.mValue.setB2(maxValue - minValue);
    }

    /**
     * Native support extracted from Effect::CreateFromString @0051E8FA and ParseSpellUsageType @0051F29C.
     * Fully ported.
     */
    private static void applyParsedUsage(Effect effect, String usageText) {
        if (usageText.isBlank()) {
            return;
        }

        String normalized = usageText.trim().toLowerCase(Locale.ROOT);
        int usageValue;
        if (normalized.equals("permanent")) {
            effect.type = PERMANENT;
            return;
        }
        if (normalized.equals("singleuse")) {
            effect.type = SINGLE_USE;
            return;
        }
        int usagePos = normalized.indexOf("charges");
        if (usagePos >= 0) {
            usageValue = parseSignedIntOrZero(normalized.substring(usagePos + "charges".length()));
            effect.type = CHARGES;
            effect.mValue.setS2(usageValue << 4);
            return;
        }
        usagePos = normalized.indexOf("duration");
        if (usagePos >= 0) {
            usageValue = parseSignedIntOrZero(normalized.substring(usagePos + "duration".length()));
            effect.type = DURATION;
            effect.mValue.setS2(usageValue << 4);
            return;
        }
        usagePos = normalized.indexOf("continuous");
        if (usagePos >= 0) {
            usageValue = parseSignedIntOrZero(normalized.substring(usagePos + "continuous".length()));
            effect.type = CONTINUOUS;
            effect.mValue.setS2(usageValue << 4);
        }
    }

    /**
     * Native support extracted from Effect::CreateFromString @0051E8FA; mirrors g_Effects @005F8F50.
     * Fully ported.
     */
    private static EffectId findEffectIdByName(String effectName) {
        for (int effectIndex = PRICE.id; effectIndex <= DAMAGE_BONUS.id; effectIndex++) {
            EffectId effectId = EffectId.fromId(effectIndex);
            if (nativeEffectName(effectId).equals(effectName)) {
                return effectId;
            }
        }
        return UNKNOWN;
    }

    /**
     * Native support extracted from Effect::CreateFromString @0051E8FA; mirrors g_Effects @005F8F50.
     * Fully ported.
     */
    private static String nativeEffectName(EffectId effectId) {
        if (effectId == EMPTY) {
            return "unused_value_0";
        }
        return effectId.tableName.toLowerCase(Locale.ROOT);
    }

    /**
     * Native support extracted from Effect::CreateFromString @0051E8FA and GetSpellIDByName @0051F1C3;
     * mirrors g_Spells @005F9018.
     * Fully ported.
     */
    private static int findSpellIdByName(String spellName) {
        String lookupName = spellName.trim();
        for (int spellIndex = SpellId.EMPTY.id; spellIndex <= SpellId.SLOW.id; spellIndex++) {
            SpellId spellId = SpellId.fromId(spellIndex);
            if (nativeEffectSpellName(spellId).equals(lookupName)) {
                return spellIndex;
            }
        }
        return -1;
    }

    /**
     * Native support extracted from GetSpellIDByName @0051F1C3; mirrors g_Spells @005F9018.
     * Fully ported.
     */
    private static String nativeEffectSpellName(SpellId spellId) {
        if (spellId == SpellId.EMPTY) {
            return "unused_spell_0";
        }
        return spellId.name().toLowerCase(Locale.ROOT);
    }

    /**
     * Native support extracted from Effect::CreateFromString @0051E8FA and ParseUIntString @0051F151.
     * Fully ported.
     */
    private static int parseUnsignedIntOrZero(String valueText) {
        String text = valueText.trim();
        int value = 0;
        boolean foundDigit = false;
        int index = text.startsWith("+") ? 1 : 0;
        while (index < text.length()) {
            char ch = text.charAt(index++);
            if (ch < '0' || ch > '9') {
                break;
            }
            value = value * 10 + (ch - '0');
            foundDigit = true;
        }
        return foundDigit ? value : 0;
    }

    /**
     * Native support extracted from Effect::CreateFromString @0051E8FA and ParseIntString @0051F20A.
     * Fully ported.
     */
    private static int parseSignedIntOrZero(String valueText) {
        String text = valueText.trim();
        if (text.isEmpty()) {
            return 0;
        }

        int sign = 1;
        int index = 0;
        char first = text.charAt(0);
        if (first == '-') {
            sign = -1;
            index = 1;
        } else if (first == '+') {
            index = 1;
        }

        int value = 0;
        boolean foundDigit = false;
        while (index < text.length()) {
            char ch = text.charAt(index++);
            if (ch < '0' || ch > '9') {
                break;
            }
            value = value * 10 + (ch - '0');
            foundDigit = true;
        }
        return foundDigit ? value * sign : 0;
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019.
     */
    private static void emitNetUpdate(Unit target) {
        CServerApp.netUpdate(target, target.changedValues);
    }

    /**
     * Native support extracted from Effect::updateOnTick @0051CC9F and Effect::applyToTarget @0051CE12;
     * calls CServerApp::notifyEffectRemoved @00504498.
     */
    private static void notifyEffectRemoved(Effect effect, Unit target) {
        CServerApp.notifyEffectRemoved(effect, target);
    }

    /**
     * Native support extracted from Effect::applyToTarget @0051CE12; calls CServerApp::AddEffect @005044BA.
     */
    private static void notifyEffectAdded(Effect effect, Unit target) {
        CServerApp.notifyEffectAdded(effect, target);
    }

    /**
     * Native support extracted from Effect::applyOnAdd @0051D175; calls
     * CServerApp::notifyUnitHitPointsChanged @00504B1D.
     */
    private static void notifyHpDelta(Unit target) {
        CServerApp.notifyUnitHitPointsChanged(target);
    }
}
