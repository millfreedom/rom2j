package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.action.ItemListAction;
import ua.millfreedom.rom2.model.actiondata.ItemInfoPacketHeader;
import ua.millfreedom.rom2.model.column.WorldItemColumn;
import ua.millfreedom.rom2.model.enums.EffectId;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.unit.UnitDirtyFlags;

import java.io.IOException;

public class Shield extends Item {
    //0x54
    public final StatData statData = new StatData();

    /**
     * Native: Shield::New @00527DB0.
     * Fully ported.
     */
    public Shield() {
        key = 0;
        hash = 0;
        exceedsMagicCapacity = 0;
    }

    /**
     * Native: Shield::Shield @00527E23.
     * Fully ported.
     */
    public Shield(String data) {
        key = 0;
        exceedsMagicCapacity = 0;

        StringBuilder keyText = new StringBuilder(extractStructuredItemKey(data));
        String effectsText = extractStructuredItemValue(data);
        shapeID = consumeShapeId(keyText) & 0xFF;
        materialID = consumeMaterialId(keyText) & 0xFF;
        String formattedShieldName = Globals.staticDataMgr.formatMaterialName(keyText.toString(), materialID & 0xFF);
        String normalizedShieldName = removeShieldSuffix(formattedShieldName);
        int shieldIndex = findWorldItemIndexByName(Globals.staticDataMgr.shields, normalizedShieldName);
        key = shieldIndex & 0xFFFF;
        if ((key & 0xFFFF) == 0 || (materialID & 0xFF) > 0x0F) {
            Globals.gameServer.pushMessage("Invalid shield <" + data + "> created - discarded.");
            return;
        }

        applyShieldTemplateStats();
        if (!effectsText.isEmpty()) {
            parseEffects(effectsText);
        }
    }

    /**
     * Native: Shield::Shield @00528084.
     * Fully ported.
     */
    public Shield(int shapeId, int materialId, int shieldIndex) {
        shapeID = shapeId & 0xFF;
        materialID = materialId & 0xFF;
        key = shieldIndex & 0xFF;
        exceedsMagicCapacity = 0;
        applyShieldTemplateStats();
    }

    /**
     * Java support wrapper around native Shield construction for item-name probing.
     * not ported.
     */
    public static Shield createByServiceName(String data) {
        if (data == null || data.isBlank()) {
            return null;
        }

        StringBuilder keyText = new StringBuilder(extractStructuredItemKey(data));
        String effectsText = extractStructuredItemValue(data);
        int shapeId = consumeShapeId(keyText);
        int materialId = consumeMaterialId(keyText);
        String formattedShieldName = Globals.staticDataMgr.formatMaterialName(keyText.toString(), materialId & 0xFF);
        String normalizedShieldName = removeShieldSuffix(formattedShieldName);
        int shieldIndex = findWorldItemIndexByName(Globals.staticDataMgr.shields, normalizedShieldName);
        if (shieldIndex == 0 || materialId > 0x0F) {
            return null;
        }

        Shield shield = new Shield(shapeId, materialId, shieldIndex);
        if (!effectsText.isEmpty()) {
            shield.parseEffects(effectsText);
        }
        return shield;
    }

    /**
     * Native support extracted from ItemAssortmentGenerator::appendConcreteEquipmentCandidates @005247DB.
     */
    public static Shield createByTemplateIds(int shieldIndex, int shapeId, int materialId) {
        if (shieldIndex <= 0 || materialId > 0x0F) {
            return null;
        }
        Shield shield = new Shield(shapeId, materialId, shieldIndex);
        return shield.worldItem == null ? null : shield;
    }

    /**
     * Native: Shield::Shield(copy) @0052836A.
     * Fully ported.
     */
    public Shield copyFrom(Shield source) {
        super.copyFrom(source);
        statData.assign(source.statData);
        return this;
    }

    /**
     * vtbl +0x38: Shield::useAndConsume @0052846B.
     * Fully ported.
     */
    @Override
    public Item useAndConsume(Unit target) {
        if ((key & 0xFFFF) == 0) {
            Globals.gameServer.pushMessage("Invalid shield");
            return this;
        }

        Item previousShield = null;
        if (target.pShield != null) {
            previousShield = target.pShield;
            previousShield.takeOff(target);
        }
        if (target.pWeapon != null && isTwoHanded(target.pWeapon)) {
            Item releasedWeapon = target.releaseIncomingObject(target.pWeapon);
            target.inventory.addItem(releasedWeapon);
        }

        target.pShield = this;
        target.mModifiers.statMods.add(statData);
        target.unitStatData.add(statData);
        target.refreshEncumbrance(weight);
        applyEffectsOnEquip(target);
        target.changedValues |= UnitDirtyFlags.ARMOR_DEFENCE_ABSORBTION.value;
        return previousShield;
    }

    /**
     * vtbl +0x3C: Shield::takeOff @00528581.
     * Fully ported.
     */
    @Override
    public void takeOff(Unit target) {
        target.refreshEncumbrance(-weight);
        target.mModifiers.statMods.subtract(statData);
        target.unitStatData.subtract(statData);
        applyEffectsOnTakeOff(target);
        target.changedValues |= UnitDirtyFlags.ARMOR_DEFENCE_ABSORBTION.value;
        target.pShield = null;
    }

    /**
     * vtbl +0x40: Shield::splitOne @005283DF.
     * Fully ported.
     */
    @Override
    public Item splitOne() {
        count = ((count & 0xFFFF) - 1) & 0xFFFF;
        Shield copy = new Shield().copyFrom(this);
        copy.count = 1;
        return copy;
    }

    /**
     * vtbl +0x44: Shield::copyItemVirtual @005447D0.
     * Fully ported.
     */
    @Override
    public Item copyItemVirtual() {
        return new Shield().copyFrom(this);
    }

    /**
     * vtbl +0x4C: Shield::recalculatePrice @0052823A.
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
            effectScore += effect.calculateScore();
        }
        price += calculateEquipmentScorePriceBonus(effectScore);
        if (price > 19_999_999) {
            price = 19_999_999;
        }
        return price;
    }

    /**
     * vtbl +0x54: Shield::writeInfoPacket @005285F5.
     * Fully ported.
     */
    @Override
    public void writeInfoPacket(ItemListAction action, ItemInfoPacketHeader header) {
        action.appendItemInfoIntDescriptor(1, price, header);
        header.flags = 0;
        int shieldFlags = worldItem.getAttribute(WorldItemColumn.SUITABLE_FOR);
        if ((shieldFlags & 1) != 0) {
            header.flags |= 0x02;
        }
        if ((shieldFlags & 2) != 0) {
            header.flags |= 0x04;
        }
        action.appendItemInfoByteDescriptor(0x0F, statData.defence, header);
        if (statData.absorbtion > 0) {
            action.appendItemInfoByteDescriptor(0x10, statData.absorbtion, header);
        }
        writeEffectInfoDescriptors(action, header);
    }

    /**
     * vtbl +0x08: Shield::serialize @0052D11B.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        super.serialize(ar);         // Item::serialize
        ar.serialize(statData);      // StatData::Serialize

        if (!ar.isStoring()) {
            worldItem = Globals.staticDataMgr.shields.get(key & 0xFFFF);
        }
    }

    /**
     * Native: Shield::applyShieldTemplateStats @0052810D.
     * Fully ported.
     */
    private void applyShieldTemplateStats() {
        int shieldIndex = key & 0xFFFF;
        WorldItem shieldInfo = Globals.staticDataMgr.shields.get(shieldIndex);
        worldItem = shieldInfo;
        type = 1;

        Material shape = Globals.staticDataMgr.shapes.get(shapeID & 0xFF);
        Material material = Globals.staticDataMgr.materials.get(materialID & 0xFF);
        statData.absorbtion = (short) (shieldInfo.getAttribute(WorldItemColumn.ABSORPTION)
                * material.attributes.absorption
                * shape.attributes.absorption);
        statData.defence = (short) (shieldInfo.getAttribute(WorldItemColumn.DEFENCE)
                * material.attributes.defence
                * shape.attributes.defence
                + 0.5d);
        magicVolume = (short) (int) (material.attributes.magicVolume * shape.attributes.magicVolume);
        weight = (short) (int) (shieldInfo.getAttribute(WorldItemColumn.WEIGHT)
                * material.attributes.weight
                * shape.attributes.weight
                + 0.5d);
        recalculatePrice();
        hash = packIDs(2, shapeID, materialID, shieldIndex & 0xFF);
    }

    /**
     * Native support extracted from Shield::Shield @00527E23 shield-name suffix removal.
     * Fully ported.
     */
    private static String removeShieldSuffix(String formattedName) {
        int shieldSuffix = formattedName.indexOf(" Shield");
        if (shieldSuffix > 0) {
            formattedName = formattedName.substring(0, shieldSuffix);
        }
        return formattedName;
    }

    /**
     * Native support extracted from Shield::useAndConsume @0052846B.
     * Fully ported.
     */
    private static boolean isTwoHanded(Weapon weapon) {
        return weapon.worldItem.getAttribute(WorldItemColumn.TWO_HANDED) == 2;
    }

}
