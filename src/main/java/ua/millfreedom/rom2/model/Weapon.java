package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.action.ItemListAction;
import ua.millfreedom.rom2.model.actiondata.ItemInfoPacketHeader;
import ua.millfreedom.rom2.model.column.WorldItemColumn;
import ua.millfreedom.rom2.model.enums.EffectId;
import ua.millfreedom.rom2.model.spell.Spell;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.unit.UnitDirtyFlags;

import java.io.IOException;

public class Weapon extends Item {
    //0x54
    public int range;
    //0x56
    public final SkillData skillData = new SkillData();
    //0x6E
    public final StatData statData = new StatData();
    //0x84
    public Spell spell;

    /**
     * Native: Weapon::New @00528762.
     * Fully ported.
     */
    public Weapon() {
        range = 1;
        spell = null;
        exceedsMagicCapacity = 0;
        hash = packIDs(1, shapeID, materialID, key & 0xFF);
    }

    /**
     * Native: Weapon::Weapon @00528808.
     * Fully ported.
     */
    public Weapon(String data) {
        range = 1;
        key = 0;
        spell = null;
        exceedsMagicCapacity = 0;

        StringBuilder keyText = new StringBuilder(extractStructuredItemKey(data));
        String effectsText = extractStructuredItemValue(data);
        shapeID = consumeShapeId(keyText) & 0xFF;
        materialID = consumeMaterialId(keyText) & 0xFF;
        int weaponIndex = findWorldItemIndexByName(Globals.staticDataMgr.weapons, keyText.toString().trim());
        key = weaponIndex & 0xFFFF;
        if ((key & 0xFFFF) == 0) {
            Globals.gameServer.pushMessage("Invalid weapon " + data + " - no such ID");
            return;
        }

        applyWeaponTemplateStats();
        if (!effectsText.isEmpty()) {
            parseEffects(effectsText);
        }
    }

    /**
     * Native: Weapon::Weapon @00528A07.
     * Fully ported.
     */
    public Weapon(int shapeId, int materialId, int weaponIndex) {
        shapeID = shapeId & 0xFF;
        materialID = materialId & 0xFF;
        key = weaponIndex & 0xFF;
        exceedsMagicCapacity = 0;
        applyWeaponTemplateStats();
    }

    /**
     * Java support wrapper around native Weapon construction for item-name probing.
     * not ported.
     */
    public static Weapon createByServiceName(String data) {
        if (data == null || data.isBlank()) {
            return null;
        }

        StringBuilder keyText = new StringBuilder(extractStructuredItemKey(data));
        String effectsText = extractStructuredItemValue(data);
        int shapeId = consumeShapeId(keyText);
        int materialId = consumeMaterialId(keyText);
        int weaponIndex = findWorldItemIndexByName(Globals.staticDataMgr.weapons, keyText.toString().trim());
        if (weaponIndex == 0 || materialId > 0x0F) {
            return null;
        }

        Weapon weapon = new Weapon(shapeId, materialId, weaponIndex);
        if (!effectsText.isEmpty()) {
            weapon.parseEffects(effectsText);
        }
        return weapon;
    }

    /**
     * Native support extracted from ItemAssortmentGenerator::appendConcreteEquipmentCandidates @005247DB.
     */
    public static Weapon createByTemplateIds(int weaponIndex, int shapeId, int materialId) {
        if (weaponIndex <= 0 || materialId > 0x0F) {
            return null;
        }
        Weapon weapon = new Weapon(shapeId, materialId, weaponIndex);
        return weapon.worldItem == null ? null : weapon;
    }

    /**
     * Native: Weapon::Weapon(copy) @00528FD8.
     * Fully ported.
     */
    public Weapon copyFrom(Weapon source) {
        super.copyFrom(source);
        range = source.range;
        skillData.assign(source.skillData);
        statData.assign(source.statData);
        spell = source.spell == null ? null : new Spell(source.spell.id);
        return this;
    }

    /**
     * Native: Weapon::HasSpell @00542580.
     * Fully ported.
     */
    public boolean hasSpell() {
        return spell != null;
    }

    /**
     * vtbl +0x38: Weapon::useAndConsume @005290CC.
     * Fully ported.
     */
    @Override
    public Weapon useAndConsume(Unit target) {
        ensureSpellFromCastEffect();

        Weapon previousWeapon = null;
        if (target.pWeapon != null) {
            previousWeapon = target.pWeapon;
            previousWeapon.takeOff(target);
        }
        if (isTwoHanded() && target.pShield != null) {
            Item releasedShield = target.releaseIncomingObject(target.pShield);
            target.inventory.addItem(releasedShield);
        }

        target.pWeapon = this;
        int attackType = worldItem.getAttribute(WorldItemColumn.ATTACK_TYPE);
        if (attackType < 10) {
            target.mModifiers.skillMods.skillDamageType0And3Min =
                    (byte) (target.mModifiers.skillMods.skillDamageType0And3Min
                            + skillData.skillDamageType0And3Min);
            target.mModifiers.skillMods.skillDamageType0And3Modifier =
                    (byte) (target.mModifiers.skillMods.skillDamageType0And3Modifier
                            + skillData.skillDamageType0And3Modifier);
            target.mModifiers.statMods.defence = (short) (target.mModifiers.statMods.defence + statData.defence);
            target.mModifiers.skillMods.skillDamageType2Min = skillData.skillDamageType2Min;
            target.mModifiers.skillMods.skillDamageType2Modifier = skillData.skillDamageType2Modifier;
            target.mModifiers.skillMods.skillDamageType2ProtectionIndex = skillData.skillDamageType2ProtectionIndex;
            target.mModifiers.skillMods.toHit = (short) (target.mModifiers.skillMods.toHit + skillData.toHit);
            target.skillData.activeSkillIndex = (byte) attackType;
        } else {
            if (attackType == 0x0B || attackType == 0x0C) {
                target.mModifiers.skillMods.skillDamageType2Min =
                        (byte) (target.mModifiers.skillMods.skillDamageType2Min
                                + skillData.skillDamageType0And3Min);
                target.mModifiers.skillMods.skillDamageType2Modifier =
                        (byte) (target.mModifiers.skillMods.skillDamageType2Modifier
                                + skillData.skillDamageType0And3Modifier);
                target.mModifiers.skillMods.skillDamageType2ProtectionIndex = (byte) (attackType - 10);
            }
            target.mModifiers.skillMods.toHit = target.skillData.skillLevels[0];
            target.skillData.activeSkillIndex = 0;
        }

        target.recalculateDerivedStats();
        if (target.isHumanoidToken() != 0) {
            int chargeTicks = worldItem.getAttribute(WorldItemColumn.CHARGE);
            if (chargeTicks != -1) {
                target.attackChargeTicks = chargeTicks & 0xFF;
            }
            int relaxTicks = worldItem.getAttribute(WorldItemColumn.RELAX);
            if (relaxTicks != -1) {
                target.attackRelaxTicks = relaxTicks & 0xFF;
            }
        }
        target.defaultCastRange += range - 1;
        target.refreshEncumbrance(weight);
        target.changedValues |= UnitDirtyFlags.WEAPON_COMBAT.value;
        applyEffectsOnEquip(target);
        return previousWeapon;
    }

    /**
     * vtbl +0x3C: Weapon::takeOff @00529392.
     * Fully ported.
     */
    @Override
    public void takeOff(Unit target) {
        applyEffectsOnTakeOff(target);

        int attackType = worldItem.getAttribute(WorldItemColumn.ATTACK_TYPE);
        if (attackType < 10) {
            target.mModifiers.skillMods.skillDamageType0And3Min =
                    (byte) (target.mModifiers.skillMods.skillDamageType0And3Min
                            - skillData.skillDamageType0And3Min);
            target.mModifiers.skillMods.skillDamageType0And3Modifier =
                    (byte) (target.mModifiers.skillMods.skillDamageType0And3Modifier
                            - skillData.skillDamageType0And3Modifier);
            target.mModifiers.statMods.defence = (short) (target.mModifiers.statMods.defence - statData.defence);
            target.mModifiers.skillMods.skillDamageType2ProtectionIndex = 0;
            target.mModifiers.skillMods.skillDamageType2Min = 0;
            target.mModifiers.skillMods.skillDamageType2Modifier = 0;
        } else if (attackType == 0x0B || attackType == 0x0C) {
            target.mModifiers.skillMods.skillDamageType2Min =
                    (byte) (target.mModifiers.skillMods.skillDamageType2Min
                            - skillData.skillDamageType0And3Min);
            target.mModifiers.skillMods.skillDamageType2Modifier =
                    (byte) (target.mModifiers.skillMods.skillDamageType2Modifier
                            - skillData.skillDamageType0And3Modifier);
            target.mModifiers.skillMods.skillDamageType2ProtectionIndex = 0;
        }

        target.mModifiers.skillMods.toHit = (short) (target.mModifiers.skillMods.toHit - skillData.toHit);
        target.skillData.activeSkillIndex = 0;
        target.recalculateDerivedStats();
        target.defaultCastRange -= range - 1;
        target.attackChargeTicks = 8;
        target.attackRelaxTicks = 4;
        target.refreshEncumbrance(-weight);
        target.changedValues |= UnitDirtyFlags.WEAPON_COMBAT.value;
        spell = null;
        target.pWeapon = null;
    }

    /**
     * vtbl +0x40: Weapon::splitOne @0052959A.
     * Fully ported.
     */
    @Override
    public Item splitOne() {
        count = ((count & 0xFFFF) - 1) & 0xFFFF;
        Weapon copy = new Weapon().copyFrom(this);
        copy.count = 1;
        return copy;
    }

    /**
     * vtbl +0x44: Weapon::copyItemVirtual @00544880.
     * Fully ported.
     */
    @Override
    public Item copyItemVirtual() {
        return new Weapon().copyFrom(this);
    }

    /**
     * vtbl +0x4C: Weapon::recalculatePrice @00528E04.
     * Fully ported.
     */
    @Override
    public int recalculatePrice() {
        Material shape = Globals.staticDataMgr.shapes.get(shapeID & 0xFF);
        Material material = Globals.staticDataMgr.materials.get(materialID & 0xFF);
        price = (int) (worldItem.getAttribute(WorldItemColumn.PRICE) * material.attributes.price * shape.attributes.price + 0.5d);
        int effectScore = 0;
        for (Effect effect : effects) {
            if (effect.isA(EffectId.PRICE)) {
                price = effect.mValue.getFull();
                return price;
            }
            if (effect.isA(EffectId.CAST_SPELL)) {
                price += effect.recalculatePrice();
                continue;
            }
            effectScore += effect.calculateScore();
        }
        price += calculateEquipmentScorePriceBonus(effectScore);
        if (price > 19_999_999) {
            price = 19_999_999;
        }
        return price;
    }

    /**
     * vtbl +0x54: Weapon::writeInfoPacket @00529629.
     * Fully ported.
     */
    @Override
    public void writeInfoPacket(ItemListAction action, ItemInfoPacketHeader header) {
        action.appendItemInfoIntDescriptor(1, price, header);
        header.flags = 0;

        int weaponFlags = worldItem.getAttribute(WorldItemColumn.SUITABLE_FOR);
        if ((weaponFlags & 1) != 0) {
            header.flags |= 0x02;
            action.appendItemInfoByteDescriptor(0x0D, skillData.skillDamageType0And3Min, header);
            action.appendItemInfoByteDescriptor(0x0E, skillData.skillDamageType0And3Modifier, header);
            if (skillData.toHit > 0) {
                action.appendItemInfoByteDescriptor(0x0C, skillData.toHit, header);
            }
            if (statData.defence > 0) {
                action.appendItemInfoByteDescriptor(0x0F, statData.defence, header);
            }
            if (range > 1) {
                action.appendItemInfoByteDescriptor(0x26, range, header);
            }
        }
        if ((weaponFlags & 2) != 0) {
            header.flags |= 0x04;
        }
        if (skillData.skillDamageType2Modifier != 0) {
            int descriptorId = switch (skillData.skillDamageType2ProtectionIndex) {
                case 1 -> 0x2C;
                case 2 -> 0x2D;
                case 3 -> 0x2E;
                case 4 -> 0x2F;
                case 5 -> 0x30;
                default -> throw new IllegalStateException("Unexpected weapon damage protection index: "
                        + skillData.skillDamageType2ProtectionIndex);
            };
            action.appendItemInfoByteDescriptor(descriptorId, skillData.skillDamageType2Min, header);
            action.appendItemInfoByteDescriptor(0x0E, skillData.skillDamageType2Modifier, header);
        }
        writeEffectInfoDescriptors(action, header);
        header.flags &= ~0x10;
    }

    /**
     * vtbl +0x08: Weapon::serialize @0052C576.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        super.serialize(ar); // Item::serialize
        ar.serialize(skillData);          // SkillData::Serialize(0x18)
        ar.serialize(statData);               // StatData::Serialize(0x16)

        if (!ar.isStoring()) {
            range = ar.readByte() & 0xFF;
            spell = Spell.readFromArchive(ar);

            worldItem = Globals.staticDataMgr.weapons.get(key & 0xFFFF);
        } else {
            ar.writeByte(range);
            ar.writeObject(spell); // CArchive::operator<<Object(ar, this->spell)
        }
    }

    /**
     * Native support extracted from Weapon::applyWeaponTemplateStats @00528A9B and
     * Weapon::ApplyMaterialShapeModifiers @00528C92.
     * Fully ported.
     */
    private void applyWeaponTemplateStats() {
        range = 1;
        spell = null;

        int weaponIndex = key & 0xFFFF;
        if ((materialID & 0xFF) >= 0x10 || weaponIndex >= Globals.staticDataMgr.weapons.size()) {
            Globals.gameServer.pushMessage(
                    "Invalid weapon " + (materialID & 0xFF) + " " + weaponIndex + " - no such ID"
            );
            return;
        }

        WorldItem weaponInfo = Globals.staticDataMgr.weapons.get(weaponIndex);
        worldItem = weaponInfo;
        type = 2;

        Material shape = Globals.staticDataMgr.shapes.get(shapeID & 0xFF);
        Material material = Globals.staticDataMgr.materials.get(materialID & 0xFF);

        int minDamage = (int) (weaponInfo.getAttribute(WorldItemColumn.PHYSICAL_MIN)
                * material.attributes.damage
                * shape.attributes.damage
                + 0.5d);
        skillData.skillDamageType0And3Min = (byte) minDamage;
        int damageModifier = (int) (weaponInfo.getAttribute(WorldItemColumn.PHYSICAL_MAX)
                * material.attributes.damage
                * shape.attributes.damage
                - (skillData.skillDamageType0And3Min & 0xFF)
                + 0.5d);
        skillData.skillDamageType0And3Modifier = (byte) damageModifier;
        skillData.toHit = (short) (weaponInfo.getAttribute(WorldItemColumn.TO_HIT)
                * material.attributes.toHit
                * shape.attributes.toHit
                + 0.5d);
        statData.defence = (short) (weaponInfo.getAttribute(WorldItemColumn.DEFENCE)
                * material.attributes.defence
                * shape.attributes.defence
                + 0.5d);
        magicVolume = (short) (int) (material.attributes.magicVolume * shape.attributes.magicVolume);
        weight = (short) (int) (weaponInfo.getAttribute(WorldItemColumn.WEIGHT)
                * material.attributes.weight
                * shape.attributes.weight
                + 0.5d);

        int weaponRange = worldItem.getAttribute(WorldItemColumn.RANGE);
        range = weaponRange == -1 ? 1 : weaponRange & 0xFF;
        recalculatePrice();
        hash = packIDs(1, shapeID, materialID, weaponIndex & 0xFF);
    }

    /**
     * Native support extracted from Weapon::ensureSpellFromCastEffect @00529986 and
     * Weapon::replaceSpellFromEffectId @005298D3.
     * Fully ported.
     */
    private void ensureSpellFromCastEffect() {
        Effect castSpellEffect = findCastSpellEffect();
        if (castSpellEffect == null) {
            return;
        }
        int spellId = castSpellEffect.mValue.getS1();
        spell = new Spell((byte) spellId);
    }

    /**
     * Native support helper for Weapon item flow.
     * not ported.
     */
    private boolean isTwoHanded() {
        return worldItem.getAttribute(WorldItemColumn.TWO_HANDED) == 2;
    }

}
