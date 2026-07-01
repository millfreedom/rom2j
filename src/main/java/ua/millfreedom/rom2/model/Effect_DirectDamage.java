package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.model.unit.Unit;

import java.io.IOException;

public class Effect_DirectDamage extends Effect {
    //0x48
    public final SkillData unitSkillData = new SkillData();

    /**
     * Native: Effect_DirectDamage::Effect_DirectDamage @0051E505.
     * Fully ported.
     */
    public Effect_DirectDamage() {
    }

    /**
     * Native: Effect_DirectDamage::setFireDamageProfile @00543040.
     * Fully ported.
     */
    public void setFireDamageProfile(int minDamage, int damageSpread) {
        setMagicDamageProfile(minDamage, damageSpread, 1);
    }

    /**
     * Native: Effect_DirectDamage::setWaterDamageProfile @00543070.
     * Fully ported.
     */
    public void setWaterDamageProfile(int minDamage, int damageSpread) {
        setMagicDamageProfile(minDamage, damageSpread, 2);
    }

    /**
     * Native: Effect_DirectDamage::setAirDamageProfile @005430A0.
     * Fully ported.
     */
    public void setAirDamageProfile(int minDamage, int damageSpread) {
        setMagicDamageProfile(minDamage, damageSpread, 3);
    }

    /**
     * Native: Effect_DirectDamage::setEarthDamageProfile @005430D0.
     * Fully ported.
     */
    public void setEarthDamageProfile(int minDamage, int damageSpread) {
        setMagicDamageProfile(minDamage, damageSpread, 4);
    }

    /**
     * Native: Effect_DirectDamage::setAstralDamageProfile @00543100.
     * Fully ported.
     */
    public void setAstralDamageProfile(int minDamage, int damageSpread) {
        setMagicDamageProfile(minDamage, damageSpread, 5);
    }

    /**
     * Native support extracted from Effect_DirectDamage direct-damage profile helpers
     *
     * @00543040, @00543070, @005430A0, @005430D0, and @00543100.
     */
    private void setMagicDamageProfile(int minDamage, int damageSpread, int protectionIndex) {
        unitSkillData.skillDamageType2Min = (byte) minDamage;
        unitSkillData.skillDamageType2Modifier = (byte) damageSpread;
        unitSkillData.skillDamageType2ProtectionIndex = (byte) protectionIndex;
    }

    /**
     * vtbl +0x08: Effect_DirectDamage::serialize @0051C68D.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        super.serialize(ar);
        ar.serialize(unitSkillData);
    }

    /**
     * Native: Effect_DirectDamage::copyFrom @0051E5DB.
     * Fully ported.
     */
    @Override
    public Effect_DirectDamage copyFrom(Effect source) {
        Effect_DirectDamage directDamage = (Effect_DirectDamage) source;
        super.copyFrom(directDamage);
        unitSkillData.copy(directDamage.unitSkillData);
        sourceUnit = directDamage.sourceUnit;
        return this;
    }

    /**
     * vtbl +0x3C: Effect_DirectDamage::applyToTarget @0051E65B.
     * Fully ported.
     */
    @Override
    public void applyToTarget(Token target) {
        if (target.isUnitToken() == 0) {
            if (target.isBuildingToken() != 0) {
                Building building = (Building) target;
                int damage = building.calculateIncomingDamage(unitSkillData, sourceUnit);
                short updatedHealth = (short) (building.healthCurrent - (short) damage);
                building.healthCurrent = updatedHealth < 0 ? 0 : updatedHealth & 0xFFFF;
                if (damage > 0) {
                    CServerApp.notifyBuildingStateChanged(building);
                }
            }
            return;
        }

        Unit targetUnit = (Unit) target;
        int damage = targetUnit.calculateIncomingDamage(unitSkillData, sourceUnit);
        targetUnit.m_nHP = (short) (targetUnit.m_nHP - (short) damage);
        if (damage > 0) {
            if (sourceUnit != null && (key & 0xFFFF) != 0) {
                sourceUnit.awardDamageSkillProgress(targetUnit, damage, key & 0xFFFF);
            }
            CServerApp.notifyUnitHitPointsChanged(targetUnit);
        }
    }
}
