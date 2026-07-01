package ua.millfreedom.rom2.model.spell;

import lombok.Getter;
import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.*;
import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.model.enums.SpellId;
import ua.millfreedom.rom2.model.enums.UnitId;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.unit.UnitDirtyFlags;

import java.io.IOException;

import static ua.millfreedom.rom2.model.EffectType.DURATION;
import static ua.millfreedom.rom2.model.column.SpellColumn.*;
import static ua.millfreedom.rom2.model.enums.SpellId.*;

@Getter
public class Spell implements MfcSerializable {
    //0x08
    public byte id;
    //0x04
    public SpellInfo spellInfo;
    //0x0C
    public int manaCost = 0;
    //0x0A
    public boolean isDefensive = false;
    //0x09
    public int maxRange = 0;
    //0x0E
    public int minDamage = 0;
    //0x0F
    public int damageSpread = 0;
    //0x10
    public int duration = 0;

    /**
     * Native: Spell::New @00519036.
     * Fully ported.
     */
    public Spell() {
    }

    /**
     * Native: Spell::Spell @0051906D.
     * Fully ported.
     */
    public Spell(byte id) {
        maxRange = 1;
        this.id = id;
        init();
    }

    /**
     * Native: Spell::CreateFromId @005190E4.
     * Fully ported.
     */
    public Spell(String spellId) {
        maxRange = 1;
        id = (byte) EMPTY.id;
        for (int i = 1; i < Globals.staticDataMgr.spells.size(); i++) {
            SpellInfo candidate = Globals.staticDataMgr.spells.get(i);
            if (spellId.compareTo(candidate.name) == 0) {
                id = (byte) i;
                break;
            }
        }
        if ((id & 0xFF) == EMPTY.id) {
            Globals.gameServer.pushMessage("Invalind spell " + spellId + " - no such ID");
        } else {
            init();
        }
    }

    /**
     * Native: Spell::Spell copy constructor @00519397.
     * Fully ported.
     */
    public Spell(Spell source) {
        spellInfo = source.spellInfo;
        id = source.id;
        maxRange = source.maxRange;
        isDefensive = source.isDefensive;
        manaCost = source.manaCost;
        minDamage = source.minDamage;
        damageSpread = source.damageSpread;
        duration = source.duration;
    }

    /**
     * Native: Spell::Init @0051922E.
     * Fully ported.
     */
    private void init() {
        int spellId = id & 0xFF;
        if (spellId == EMPTY.id) {
            Globals.gameServer.pushMessage("Invalind spell #" + spellId + " - no such ID");
            return;
        }
        spellInfo = Globals.staticDataMgr.spells.get(spellId);
        manaCost = spellInfo.getAttribute(MANA_COST) & 0xFFFF;
        maxRange = spellInfo.getAttribute(MAX_RANGE) & 0xFF;
        isDefensive = spellInfo.getAttribute(DEFENSIVE) == 1;
    }

    // not ported.
    public boolean isA(byte ID) {
        return id == ID;
    }

    // not ported.
    public boolean isA(SpellId spellId) {
        return spellId != null && (id & 0xFF) == spellId.id;
    }

    /**
     * Native: Spell::isUnitTarget @00519441.
     * Fully ported.
     */
    public boolean isUnitTarget() {
        return spellInfo.getAttribute(SPELL_TARGET) == 1;
    }

    /**
     * Native: Spell::isPointTarget @0051941A.
     * Fully ported.
     */
    public boolean isPointTarget() {
        return spellInfo.getAttribute(SPELL_TARGET) == 2;
    }

    /**
     * Native: Spell::canTargetUnit @00519408.
     * Fully ported.
     */
    public boolean canTargetUnit(@SuppressWarnings("unused") Unit target) {
        return true;
    }

    /**
     * Native: Spell::getSphere @00519466.
     * Fully ported.
     */
    public byte getSphere() {
        return (byte) spellInfo.getAttribute(SPHERE);
    }

    /**
     * Native: Spell::UpdateStatsFromBaseAndModifier @0051951D.
     * Fully ported.
     */
    public void updateStats(byte base, byte modifier) {
        updateStats((Byte.toUnsignedInt(base) + Byte.toUnsignedInt(modifier) - 0x1E) & 0xFF);
    }

    /**
     * Native: Spell::UpdateStats @005195B1.
     * Fully ported.
     */
    public void updateStats(int val) {
        val &= 0xFF;
        maxRange = spellInfo.getAttribute(MAX_RANGE) & 0xFF;
        if (isA(TELEPORT)) {
            maxRange += val / 3;
        } else if (maxRange != 0) {
            maxRange += val / 30;
        }
        maxRange &= 0xFF;

        double modifier = val / 30.0 + 1.0;
        int damageMin = spellInfo.getAttribute(DAMAGE_MIN);
        if (damageMin < 1) {
            minDamage = 0;
        } else {
            minDamage = (int) (damageMin * modifier) & 0xFF;
        }

        int damageMax = spellInfo.getAttribute(DAMAGE_MAX);
        if (damageMax < 1) {
            damageSpread = 0;
        } else {
            damageSpread = (int) (damageMax * modifier - minDamage) & 0xFF;
        }

        int spellDuration = spellInfo.getAttribute(SPELL_DURATION);
        if (spellDuration < 1) {
            int areaEffectDuration = spellInfo.getAttribute(AREA_EFFECT_DURATION);
            if (areaEffectDuration < 1) {
                duration = 0;
            } else {
                duration = ((areaEffectDuration << 4) + ((val << 4) / 10)) & 0xFFFF;
            }
        } else if (isA(INVISIBILITY)) {
            duration = (val << 4) & 0xFFFF;
        } else if (isA(STONE_CURSE)) {
            duration = ((val * 0x1e0) / 100) & 0xFFFF;
        } else {
            duration = (int) (Math.pow(1.025, val) * spellDuration * 16.0) & 0xFFFF;
        }
    }

    /**
     * Native: Spell::GetRays @005197E3.
     * Fully ported.
     */
    public int getRays(int context) {
        if (!isA(PRISMATIC_SPRAY)) {
            return 0;
        }
        int base = context & 0xff;
        return Math.min(base / 0x14 + 2, 7);
    }

    /**
     * Native: Spell::GetMaximumDamageProbability @00519837.
     * Fully ported.
     */
    public int getMaximumDamageProbability(int context) {
        if (!isA(BLESS)) {
            return 0;
        }
        int base = context & 0xff;
        return (base * 4) / 5 + 0x14;
    }

    /**
     * Native: Spell::GetMinimumDamageProbability @0051986B.
     * Fully ported.
     */
    public int getMinimumDamageProbability(int context) {
        if (!isA(CURSE)) {
            return 0;
        }
        int base = context & 0xff;
        return (base * 4) / 5 + 0x14;
    }

    /**
     * Native: Spell::GetResistance @0051989F.
     * Fully ported.
     */
    public int getResistance(int context) {
        if (!isA(PROTECTION_FROM_FIRE)
                && !isA(PROTECTION_FROM_AIR)
                && !isA(PROTECTION_FROM_WATER)
                && !isA(PROTECTION_FROM_EARTH)) {
            return 0;
        }
        int base = context & 0xff;
        return base / 2;
    }

    /**
     * Native: Spell::FUN_005198F1.
     * Fully ported.
     */
    public int getBonusType5(int context) {
        if (!isA(SHIELD)) {
            return 0;
        }
        int base = context & 0xff;
        return base / 10 + 3;
    }

    /**
     * Native: Spell::GetSight @00519922.
     * Fully ported.
     */
    public int getSight(int context) {
        if (!isA(LIGHT) && !isA(DARKNESS)) {
            return 0;
        }
        int base = context & 0xff;
        return -(1 + base / 0x1e);
    }

    /**
     * Native: Spell::GetSpeed @00519966.
     * Fully ported.
     */
    public int getSpeed(int context) {
        if (!isA(HASTE)) {
            return 0;
        }
        int base = context & 0xff;
        return base / 0xf + 1;
    }


    // not ported.
    public int getMaxDamage() {
        return minDamage + damageSpread;
    }

    /**
     * Native: Spell::RestoreContext @00546550.
     * Fully ported.
     */
    public static Spell restoreContextToken(Object tokenOrRef) {
        Object resolved = Globals.gameServer.lookupPointerMapOrNull(tokenOrRef);
        return (resolved instanceof Spell spell) ? spell : null;
    }

    /**
     * Native: Spell::operator>> @0051901A (ReadObject path).
     * Fully ported.
     */
    public static Spell readFromArchive(CArchive ar) throws IOException {
        return ar.readObject(Spell.class);
    }

    /**
     * Native: Spell::Cast @00519998.
     * Fully ported.
     */
    public boolean cast(Unit caster, Token target, int pointX, int pointY) {
        if (caster.isMageClass() && caster.pItem == null) {
            if ((short) caster.m_nMP < (short) manaCost) {
                return false;
            }
            caster.m_nMP = (short) (caster.m_nMP - manaCost);
        }

        TargetHandle castTarget = new TargetHandle();
        castTarget.initDefault();
        if (target == null) {
            castTarget.initFromBytes(pointX, pointY, Globals.worldMap);
        } else {
            castTarget.assignFrom(target.m_pTargetHandle);
            if (target.getTokenSizeVirtual() > 1) {
                castTarget.setPos(target.getCenterXdX() - 1, target.getCenterYdY() - 1);
            }
        }

        if (target != caster && caster.hasEffectKeyFlag(0x0C)) {
            for (Effect effect : caster.effects) {
                if ((effect.key & 0xFFFF) == 0x0C) {
                    effect.mValue.setS2(1);
                }
            }
        }

        if (!caster.isMageClass() && caster.pWeapon != null && caster.pWeapon == caster.pItem) {
            return true;
        }
        if (spellInfo == null) {
            return false;
        }

        int castDelayTicks = 0;
        if (spellInfo.getAttribute(DELIVERY_SYSTEM) == 2) {
            int distance = caster.m_pTargetHandle.euclideanDistanceByPackedPosition(castTarget);
            int speed = spellInfo.getAttribute(SPELL_EFFECT_SPEED);
            castDelayTicks = distance / speed;
            if (isA(LIGHTNING) || isA(PRISMATIC_SPRAY)) {
                castDelayTicks = 5;
            }
        } else if (caster.hasNoTableLine()) {
            return true;
        }

        if (isA(PRISMATIC_SPRAY)) {
            updatePrismaticCasterStats(caster);
            castPrismaticSpray(caster, (Unit) target);
            return true;
        }

        if (isUnitTarget()) {
            CServerApp.dispatchUnitTargetSpell(caster, this, target, (short) castDelayTicks);
        } else {
            CServerApp.dispatchPointTargetSpell(caster, this, castTarget, (short) castDelayTicks);
        }
        return true;
    }

    /**
     * Native: Spell::finalizeCastOnTarget @00519D7F.
     * Fully ported.
     */
    public void finalizeCastOnTarget(Unit caster, Token target) {
        if (!isA(PRISMATIC_SPRAY)) {
            finalizeCastOnPoint(caster, target, 0, 0);
        }
    }

    /**
     * Native: Spell::finalizeCastOnPoint @00519DB8.
     * Fully ported.
     */
    public void finalizeCastOnPoint(Unit caster, int pointX, int pointY) {
        finalizeCastOnPoint(caster, null, pointX, pointY);
    }

    /**
     * Native: Spell::finalizeCastOnPoint @00519DB8.
     * Fully ported.
     */
    public void finalizeCastOnPoint(Unit caster, Token target, int pointX, int pointY) {
        boolean statusNot2 = caster.hasCanCastStatusFlag() == 0;
        if (spellInfo == null) {
            return;
        }

        int sphere = spellInfo.getAttribute(SPHERE);
        int context = resolveCastContext(caster, sphere);
        updateStats(context);

        if (!statusNot2 && caster.pItem == null) {
            caster.awardSpellCastSkillProgress(target, id & 0xFF);
        }

        TargetHandle castTarget = buildCastTargetHandle(target, pointX, pointY);
        if (target != null && target.isUnitToken() != 0 && (((Unit) target).status & 0x08) != 0) {
            return;
        }

        Effect payload;
        if ((minDamage + damageSpread) == 0 || isA(HEAL) || isA(DRAIN_LIFE)) {
            if (isA(EMPTY)) {
                Globals.gameServer.pushMessage("Invalid spell #0 - can't cast.");
                return;
            }
            if (isA(TELEPORT)) {
                applyTeleport(caster, castTarget);
                return;
            }
            if (isA(HEAL)) {
                applyHeal(caster, target);
                return;
            }
            if (isA(DRAIN_LIFE)) {
                applyDrainLife(caster, target);
                return;
            }
            if (isA(SUMMON)) {
                summonCreature(caster);
                return;
            }
            if (isA(CONTROL_SPIRIT)) {
                controlSpirit(caster, castTarget);
                return;
            }

            payload = createNonDamagePayload(caster, target, context);
            if (payload == null) {
                return;
            }
        } else {
            Effect_DirectDamage directDamage = new Effect_DirectDamage();
            directDamage.sourceUnit = sourceCasterOrNull(caster);
            applyDirectDamageProfile(directDamage, sphere);
            markPayloadIdentity(directDamage, 9);
            payload = directDamage;
        }

        SpellEffect distributionEffect = buildDistributionEffect(caster, target, castTarget, payload, context);
        if (distributionEffect == null) {
            return;
        }
        enqueueDeliveryEffect(caster, distributionEffect);
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @00519DB8 cast-context calculation.
     * Fully ported support helper.
     */
    private int resolveCastContext(Unit caster, int sphere) {
        if (caster.pItem != null) {
            Effect castSpellEffect = caster.pItem.findCastSpellEffect();
            return castSpellEffect == null ? 0 : castSpellEffect.mValue.getS1Signed();
        }
        return Math.min(caster.getSpellPowerContext(sphere), 0xFF);
    }

    /**
     * Native support extracted from Spell::Cast @00519998 and Spell::finalizeCastOnPoint @00519DB8 target setup.
     * Fully ported support helper.
     */
    private TargetHandle buildCastTargetHandle(Token targetToken, int pointX, int pointY) {
        TargetHandle target = new TargetHandle();
        target.initDefault();
        if (targetToken == null) {
            target.initFromBytes(pointX, pointY, Globals.worldMap);
            return target;
        }

        target.assignFrom(targetToken.m_pTargetHandle);
        if (targetToken.getTokenSizeVirtual() > 1) {
            target.setPos(targetToken.getCenterXdX() - 1, targetToken.getCenterYdY() - 1);
        }
        return target;
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @0051A0D4 direct-damage sphere dispatch.
     * Fully ported support helper.
     */
    private void applyDirectDamageProfile(Effect_DirectDamage directDamage, int sphere) {
        switch (sphere) {
            case 1 -> directDamage.setFireDamageProfile(minDamage, damageSpread);
            case 2 -> directDamage.setWaterDamageProfile(minDamage, damageSpread);
            case 3 -> directDamage.setAirDamageProfile(minDamage, damageSpread);
            case 4 -> directDamage.setEarthDamageProfile(minDamage, damageSpread);
            case 5 -> directDamage.setAstralDamageProfile(minDamage, damageSpread);
            default -> {
            }
        }
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @00519DB8 non-damage spell switch.
     * Fully ported support helper.
     */
    private Effect createNonDamagePayload(Unit caster, Token targetToken, int context) {
        Effect payload = switch (SpellId.fromId(id & 0xFF)) {
            case PROTECTION_FROM_FIRE, PROTECTION_FROM_WATER, PROTECTION_FROM_AIR, PROTECTION_FROM_EARTH ->
                    createProtectionPayload(context);
            case POISON_CLOUD -> createPoisonCloudPayload(context);
            case INVISIBILITY -> createInvisibilityPayload(context);
            case DARKNESS -> createDarknessPayload(context);
            case LIGHT -> createLightPayload(context);
            case WALL_OF_EARTH -> createWallOfEarthPayload();
            case STONE_CURSE -> createStoneCursePayload(caster, targetToken, context);
            case BLESS, CURSE -> createBlessCursePayload(context);
            case HASTE, SLOW -> createHasteSlowPayload(context);
            case SHIELD -> createShieldPayload(context);
            default -> createDefaultNonDamagePayload();
        };
        if (payload == null) {
            return null;
        }
        markPayloadIdentity(payload, 8);
        return payload;
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @0051A206 protection cases.
     * Fully ported support helper.
     */
    private Effect createProtectionPayload(int context) {
        Effect payload = new Effect(spellInfo.effect);
        payload.type |= DURATION;
        payload.mValue.setS1(context / 2);
        payload.mValue.setS2(durationTicksForContext(context));
        return payload;
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @0051A2B8 poison-cloud case.
     * Fully ported support helper.
     */
    private Effect createPoisonCloudPayload(int context) {
        Effect payload = new Effect(spellInfo.effect);
        int scaled = (int) (payload.mValue.getS1Signed() * (context / 45.0 + 1.0));
        payload.mValue.setS1(scaled);
        return payload;
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @0051B927 invisibility case.
     * Fully ported support helper.
     */
    private Effect createInvisibilityPayload(int context) {
        Effect payload = new Effect();
        payload.type |= DURATION;
        payload.mValue.setS2(context << 4);
        return payload.mValue.getS2Signed() == 0 ? null : payload;
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @0051B2DE darkness case.
     * Fully ported support helper.
     */
    private Effect createDarknessPayload(int context) {
        Effect payload = new Effect(spellInfo.effect);
        payload.mValue.setS1(-(context / 0x1E) - 1);
        return payload;
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @0051B239 light case.
     * Fully ported support helper.
     */
    private Effect createLightPayload(int context) {
        Effect payload = new Effect(spellInfo.effect);
        payload.mValue.setS1(context / 0x1E + 1);
        return payload;
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @0051B1DE wall-of-earth case.
     * Fully ported support helper.
     */
    private Effect createWallOfEarthPayload() {
        return new Effect();
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @0051B6AE stone-curse case.
     * Fully ported support helper.
     */
    private Effect createStoneCursePayload(Unit caster, Token targetToken, int context) {
        Effect payload = new Effect(spellInfo.effect);
        payload.type |= DURATION;
        int durationTicks = (context * 0xF0 * ((caster.pItem == null) ? 2 : 1)) / 100;
        if (targetToken.isUnitToken() != 0) {
            Unit targetUnit = (Unit) targetToken;
            int resistance = targetUnit.unitStatData.protections[4];
            int reduced = (durationTicks * (100 - resistance)) / 100;
            durationTicks = Utils.randInclusive(reduced);
            if (durationTicks == 0) {
                return null;
            }
        }
        payload.mValue.setS2(durationTicks);
        return payload;
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @0051B82C bless/curse cases.
     * Fully ported support helper.
     */
    private Effect createBlessCursePayload(int context) {
        Effect payload = new Effect();
        payload.type |= DURATION;
        payload.mValue.setS1((context * 4) / 5 + 0x14);
        payload.mValue.setS2(durationTicksForContext(context));
        return payload;
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @0051B589 haste/slow cases.
     * Fully ported support helper.
     */
    private Effect createHasteSlowPayload(int context) {
        Effect payload = new Effect(spellInfo.effect);
        payload.type |= DURATION;
        int value = context / 0x0F + 1;
        payload.mValue.setS1(isA(SLOW) ? -value : value);
        payload.mValue.setS2(durationTicksForContext(context));
        return payload;
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @0051B484 shield case.
     * Fully ported support helper.
     */
    private Effect createShieldPayload(int context) {
        Effect payload = new Effect(spellInfo.effect);
        payload.type |= DURATION;
        payload.mValue.setS1(context / 10 + 3);
        payload.mValue.setS2(durationTicksForContext(context));
        return payload;
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @0051BA22 default non-damage case.
     * Fully ported support helper.
     */
    private Effect createDefaultNonDamagePayload() {
        return new Effect();
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @0051A276 duration math.
     * Fully ported support helper.
     */
    private int durationTicksForContext(int context) {
        int spellDuration = spellInfo.getAttribute(SPELL_DURATION);
        return (int) (Math.pow(1.025, context) * spellDuration * 16.0);
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @0051BB6B distribution effect setup.
     * Fully ported support helper.
     */
    private SpellEffect buildDistributionEffect(
            Unit caster,
            Token targetToken,
            TargetHandle castTarget,
            Effect payload,
            int context
    ) {
        int distribution = spellInfo.getAttribute(DISTRIBUTION_SYSTEM);
        Unit sourceCaster = sourceCasterOrNull(caster);

        if (distribution == 1) {
            if (targetToken == null) {
                Globals.gameServer.pushMessage("Spell: oops - can't cast point effect on point.");
                return null;
            }
            PointEffect pointEffect = new PointEffect(payload, targetToken);
            pointEffect.sourceCaster = sourceCaster;
            pointEffect.damageAttributionEnabled = isDefensive ? 0 : 1;
            markSpellTokenIdentity(pointEffect);
            payload.sourceUnit = sourceCaster;
            return pointEffect;
        }

        AreaEffect areaEffect = new AreaEffect(payload, castTarget, spellInfo.getAttribute(RADIUS_LENGTH_HALF));
        areaEffect.sourceCaster = sourceCaster;
        if (areaEffect.payload != null) {
            areaEffect.payload.sourceUnit = areaEffect.sourceCaster;
        }
        areaEffect.durationTicks = spellInfo.getAttribute(AREA_EFFECT_DURATION) << 4;
        if (areaEffect.durationTicks != 0) {
            areaEffect.durationTicks += (context << 4) / 10;
            areaEffect.scenarioObjectId = 1;
        }
        if (distribution == 5) {
            areaEffect.scenarioObjectId = 2;
            areaEffect.durationTicks = 0;
        }
        areaEffect.facing = Globals.worldMap.direction8CodeToCell(caster, castTarget.getCell()) >>> 5;
        markSpellTokenIdentity(areaEffect);
        Globals.worldMap.unitVisibilityState0x92ECC.applySpellEffectVisibilityMask(areaEffect);
        return areaEffect;
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @0051BD2E delivery enqueue setup.
     * Fully ported support helper.
     */
    private void enqueueDeliveryEffect(Unit caster, SpellEffect distributionEffect) {
        int delivery = spellInfo.getAttribute(DELIVERY_SYSTEM);
        if (delivery == 1) {
            markSpellTokenIdentity(distributionEffect);
            Globals.gameServer.objectLists.spellEffects.add(distributionEffect);
            return;
        }
        if (delivery != 2) {
            return;
        }

        SpellTransport transport = new SpellTransport(
                distributionEffect,
                caster.m_pTargetHandle,
                spellInfo.getAttribute(SPELL_EFFECT_SPEED)
        );
        if (isA(LIGHTNING) || isA(PRISMATIC_SPRAY)) {
            transport.travelTicks = 10;
        }
        Globals.gameServer.objectLists.spellEffects.add(transport);
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @00519DB8 teleport branch.
     * Fully ported support helper.
     */
    private void applyTeleport(Unit caster, TargetHandle castTarget) {
        Globals.worldMap.relocateScenarioMissionEntryUnit(caster, castTarget.getX(), castTarget.getY());
        CServerApp.netUpdate(caster, UnitDirtyFlags.POSITION_AND_FACING.value);
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @00519DB8 heal branch.
     * Fully ported support helper.
     */
    private void applyHeal(Unit caster, Token targetToken) {
        Unit targetUnit = (Unit) targetToken;
        if (Globals.gameServer.missionScriptRuntime != null
                && caster.owner != null
                && targetUnit.owner != null
                && Globals.gameServer.missionScriptRuntime.isHostile(caster.owner, targetUnit.owner)) {
            return;
        }
        if (targetUnit.m_nHP < -9 || targetUnit.m_nHPRegenRate == 0) {
            return;
        }

        int healAmount = minDamage + Utils.randInclusive(1, damageSpread);
        int missingHp = targetUnit.m_nMaxHP - targetUnit.m_nHP;
        if (healAmount > missingHp) {
            healAmount = missingHp;
        }
        targetUnit.changedValues = UnitDirtyFlags.NONE.value;
        targetUnit.applyHpDeltaAndResetRespawnOnRevive(healAmount);
        CServerApp.netUpdate(targetUnit, targetUnit.changedValues);
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @00519DB8 drain-life branch.
     * Fully ported support helper.
     */
    private void applyDrainLife(Unit caster, Token targetToken) {
        if (targetToken.isUnitToken() == 0) {
            return;
        }
        Unit targetUnit = (Unit) targetToken;

        int damage = minDamage + Utils.randInclusive(1, damageSpread);
        damage = (damage * (100 - targetUnit.unitStatData.protections[5])) / 100;
        if (targetUnit.m_nHP + 10 <= damage) {
            damage = targetUnit.m_nHP + 10;
        }
        if (damage < 1) {
            return;
        }

        caster.awardDamageSkillProgress(targetUnit, damage, id & 0xFF);
        if (targetToken.owner != null && caster.owner != null) {
            Globals.gameServer.missionScriptRuntime.recordUnitEngagement(caster, targetUnit, 0);
        }
        targetUnit.m_nHP -= damage;
        CServerApp.netUpdate(targetUnit, UnitDirtyFlags.HP.value);
        targetUnit.lastDamageSource = caster;
        targetUnit.killCreditSkillContext = id & 0xFF;
        caster.applyHpDeltaAndResetRespawnOnRevive(damage);
        CServerApp.netUpdate(caster, UnitDirtyFlags.HP.value);
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @00519DB8 summon branch.
     * Fully ported support helper.
     */
    private void summonCreature(Unit caster) {
        Player owner = caster.owner;
        Unit unit = takeReusableDeferredDeathUnit();
        unit.clearOwnedResources();
        unit.reinitializeFromTemplateName(selectSummonedUnitTemplate(caster));
        unit.suppressDeathLootFlag = 1;
        if (!unit.placeNearMissionCell(
                caster.m_pTargetHandle.getX(),
                caster.m_pTargetHandle.getY(),
                6
        )) {
            return;
        }
        registerSpellCreatedUnit(unit, owner, caster.idFull, true);
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @00519DB8 summon unit template selection.
     * Fully ported support helper.
     */
    private static String selectSummonedUnitTemplate(Unit caster) {
        int roll = Utils.randInclusive(1, 3);
        int level = caster.skillData.skillLevels[5] / 0x19 + 1;
        if (level < 1) {
            level = 1;
        }
        if (level > 4) {
            level = 4;
        }

        UnitId baseTemplate = switch (roll) {
            case 1 -> UnitId.SQUIRREL;
            case 2 -> UnitId.TURTLE;
            default -> UnitId.FOOT_ANIMATED;
        };
        return summonedUnitTemplateForLevel(baseTemplate, level);
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @00519DB8 summon level suffix assembly.
     * Fully ported support helper.
     */
    private static String summonedUnitTemplateForLevel(UnitId baseTemplate, int level) {
        return switch (baseTemplate) {
            case SQUIRREL -> switch (level) {
                case 2 -> UnitId.SQUIRREL_2.tableName;
                case 3 -> UnitId.SQUIRREL_3.tableName;
                case 4 -> UnitId.SQUIRREL_4.tableName;
                default -> UnitId.SQUIRREL.tableName;
            };
            case TURTLE -> switch (level) {
                case 2 -> UnitId.TURTLE_2.tableName;
                case 3 -> UnitId.TURTLE_3.tableName;
                case 4 -> UnitId.TURTLE_4.tableName;
                default -> UnitId.TURTLE.tableName;
            };
            case FOOT_ANIMATED -> switch (level) {
                case 2 -> UnitId.FOOT_ANIMATED_2.tableName;
                case 3 -> UnitId.FOOT_ANIMATED_3.tableName;
                case 4 -> UnitId.FOOT_ANIMATED_4.tableName;
                default -> UnitId.FOOT_ANIMATED.tableName;
            };
            default -> baseTemplate.tableName;
        };
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @00519DB8 control-spirit branch.
     * Fully ported support helper.
     */
    private void controlSpirit(Unit caster, TargetHandle castTarget) {
        Unit corpse = findControlSpiritCorpse(castTarget);
        if (corpse == null) {
            return;
        }

        int corpseStage = corpse.respawning;
        corpse.m_nHP = -0x2711;
        corpse.respawning = 5;
        CServerApp.netUpdate(corpse, null, UnitDirtyFlags.HP.value, 0x0FFB, 0, 0);

        Unit unit = takeReusableDeferredDeathUnit();
        unit.clearOwnedResources();
        initializeControlledSpiritUnit(unit, corpse, corpseStage);
        if (!unit.placeNearMissionCell(
                corpse.m_pTargetHandle.getX(),
                corpse.m_pTargetHandle.getY(),
                0
        )) {
            return;
        }

        unit.suppressDeathLootFlag = 1;
        registerSpellCreatedUnit(unit, caster.owner, 0, false);
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @00519DB8 control-spirit corpse search.
     * Fully ported support helper.
     */
    private static Unit findControlSpiritCorpse(TargetHandle castTarget) {
        for (int stage = 2; stage <= 4; stage++) {
            Unit corpse = findControlSpiritCorpseAtStage(castTarget, stage);
            if (corpse != null) {
                return corpse;
            }
        }
        return null;
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @00519DB8 control-spirit corpse stage scan.
     * Fully ported support helper.
     */
    private static Unit findControlSpiritCorpseAtStage(TargetHandle castTarget, int stage) {
        if (Globals.gameServer.objectLists.corpses == null) {
            return null;
        }
        for (Unit corpse : Globals.gameServer.objectLists.corpses) {
            int typeId = corpse.getTokenTypeId() & 0xFFFF;
            if (corpse.respawning == stage
                    && (typeId < 0x52 || typeId > 0x62)
                    && castTarget.isSameCell(corpse.m_pTargetHandle)) {
                return corpse;
            }
        }
        return null;
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @00519DB8 deferred-death unit reuse path.
     * Fully ported support helper.
     */
    private static Unit takeReusableDeferredDeathUnit() {
        for (Unit unit : Globals.gameServer.deferredDeathUnits) {
            if (unit.respawning > 4) {
                Globals.gameServer.objectLists.corpses.remove(unit);
                return unit;
            }
        }
        Unit unit = new Unit();
        Globals.gameServer.deferredDeathUnits.add(unit);
        return unit;
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @00519DB8 controlled corpse template/stat copy.
     * Fully ported support helper.
     */
    private static void initializeControlledSpiritUnit(Unit unit, Unit corpse, int corpseStage) {
        if (corpseStage == 2) {
            unit.reinitializeFromTemplateName(UnitId.F_ZOMBIE_1.tableName);
            unit.m_nBody = corpse.m_nBody;
            unit.m_nReaction = corpse.m_nReaction / 2 + 1;
            unit.m_nMind = corpse.m_nMind;
            unit.m_nSpirit = corpse.m_nSpirit;
            unit.m_nMaxHP = corpse.m_nMaxHP;
            unit.m_nHP = unit.m_nMaxHP;
            unit.skillData.toHit = corpse.skillData.toHit;
            unit.unitStatData.defence = corpse.unitStatData.defence;
        } else if (corpseStage == 3) {
            unit.reinitializeFromTemplateName(UnitId.F_SKELETON_1.tableName);
            unit.m_nBody = corpse.m_nBody / 2 + 1;
            unit.m_nReaction = corpse.m_nReaction / 2 + 1;
            unit.m_nMind = corpse.m_nMind;
            unit.m_nSpirit = corpse.m_nSpirit;
            unit.m_nMaxHP = corpse.m_nMaxHP / 2;
            unit.m_nHP = unit.m_nMaxHP;
            unit.skillData.toHit = corpse.skillData.toHit;
            unit.unitStatData.defence = corpse.unitStatData.defence;
        } else if (corpseStage == 4) {
            unit.reinitializeFromTemplateName(UnitId.GHOST.tableName);
            unit.m_nReaction = corpse.m_nReaction / 2 + 1;
            unit.m_nMind = corpse.m_nMind;
            unit.m_nSpirit = corpse.m_nSpirit;
            unit.m_nMaxHP = corpse.m_nMaxHP / 2;
            unit.m_nHP = unit.m_nMaxHP;
            unit.skillData.toHit = corpse.skillData.toHit;
            unit.unitStatData.defence = corpse.unitStatData.defence;
        }
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @00519DB8 summon/control-spirit registration tail.
     * Fully ported support helper.
     */
    private static void registerSpellCreatedUnit(
            Unit unit,
            Player owner,
            int forceFinalCorpseStageOnDeath,
            boolean advanceToMissionCell
    ) {
        Globals.gameServer.activeUnits.addAndAssignRuntimeId(unit);
        unit.owner = owner;
        unit.forceFinalCorpseStageOnDeath = forceFinalCorpseStageOnDeath;
        owner.ownedUnits.add(unit);

        UnitGroup group = new UnitGroup();
        owner.unitGroups.add(group);
        group.addUnit(unit);

        unit.initializeScenarioMissionEntryUnit(Globals.gameServer.missionScriptRuntime);
        if (advanceToMissionCell) {
            Globals.gameServer.missionScriptRuntime.initializeAdvanceToMissionCellGroup(group, 0);
        } else {
            group.initializeScenarioMissionEntryGroup(Globals.gameServer.missionScriptRuntime);
        }
        Globals.worldMap.unitVisibilityState0x92ECC.rebuildUnitVisibilityState();
        CServerApp.netUpdate(unit, null, spellCreatedUnitUpdateMask(), 0x0FFB, 0, 0);
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @00519DB8 summon/control-spirit NetUpdate mask.
     * Fully ported support helper.
     */
    private static int spellCreatedUnitUpdateMask() {
        return UnitDirtyFlags.toValue(
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
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @00519DB8 token identity writes.
     * Fully ported support helper.
     */
    private void markPayloadIdentity(Effect payload, int variantOffset) {
        payload.key = id & 0xFF;
        payload.typeID = (id & 0xFF) * 2 + variantOffset;
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @00519DB8 spell-effect token identity writes.
     * Fully ported support helper.
     */
    private void markSpellTokenIdentity(SpellEffect spellEffect) {
        spellEffect.key = id & 0xFF;
        spellEffect.typeID = (id & 0xFF) * 2 + 9;
    }

    /**
     * Native support extracted from Spell::finalizeCastOnPoint @00519DB8 source-caster selection.
     * Fully ported support helper.
     */
    private Unit sourceCasterOrNull(Unit caster) {
        return caster.hasNoTableLine() ? null : caster;
    }

    /**
     * Native: Spell::UpdatePrismaticCasterStats @00519483.
     * Fully ported.
     */
    void updatePrismaticCasterStats(Unit caster) {
        int sphere = spellInfo.getAttribute(SPHERE);
        int context = caster.skillData.skillLevels[sphere] - 0x1E + caster.m_nMind;
        if (context < 0) {
            context = 0;
        } else if (context > 0xFF) {
            context = 0xFF;
        }
        updateStats(context);
    }

    /**
     * Native: Spell::CastPrismaticSpray @00519C04.
     * Fully ported.
     */
    private void castPrismaticSpray(Unit caster, Unit target) {
        int maxTargets;
        if (caster.pItem == null) {
            int sphere = spellInfo.getAttribute(SPHERE) & 0xFF;
            int context = caster.getSpellPowerContext(sphere) & 0xFF;
            maxTargets = context / 0x14 + 2;
        } else {
            Effect castSpellEffect = caster.pItem.findCastSpellEffect();
            int itemContext = castSpellEffect.mValue.getS1Signed();
            maxTargets = itemContext / 0x14 + 2;
        }

        if (maxTargets > 7) {
            maxTargets = 7;
        }

        CustomList<Unit> targets = new CustomList<>(Unit.class);
        Globals.gameServer.missionScriptRuntime.collectSpellTargets(caster, target, targets, maxTargets, maxRange);

        CServerApp.dispatchSpellTargets(caster, this, targets);
        for (Unit selected : targets) {
            finalizeCastOnPoint(caster, selected, 0, 0);
        }
    }

    /**
     * Native: Spell::Serialize @0051BF44.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        if (!ar.isStoring()) {
            id = ar.readByte();
            maxRange = ar.readByte() & 0xFF;
            isDefensive = (ar.readByte() & 0xFF) != 0;
            manaCost = ar.readUShort();

            int ptrToken = ar.readInt();
            Globals.gameServer.setPointerMapEntry(ptrToken, this);

            spellInfo = Globals.staticDataMgr.spells.get(id & 0xFF);
        } else {
            ar.writeByte(id);
            ar.writeByte(maxRange);
            ar.writeByte(isDefensive ? 1 : 0);
            ar.writeShort(manaCost);
            ar.writeInt(System.identityHashCode(this));
        }
    }

    @Override
    // not ported.
    public boolean isDirect() {
        return true;
    }

}
