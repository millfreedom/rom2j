package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.action.ItemListAction;
import ua.millfreedom.rom2.model.actiondata.ItemInfoPacketHeader;
import ua.millfreedom.rom2.model.column.WorldItemColumn;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.unit.UnitDirtyFlags;
import ua.millfreedom.rom2.model.unit.humanoid.Humanoid;

import java.io.IOException;

import static ua.millfreedom.rom2.model.enums.EffectId.*;

public class Armor extends Item {
    private static final int MAX_PRICE = 19_999_999;

    //0x54
    public int armorSlot;
    //0x56
    public final StatData statData = new StatData();

    /**
     * Native: Armor::New @00527378.
     * Fully ported.
     */
    public Armor() {
        materialID = 0;
        armorSlot = 0;
        key = 0;
        exceedsMagicCapacity = 0;
    }

    /**
     * Native: Armor::Armor @005273F0.
     * Fully ported.
     */
    public Armor(String data) {
        key = 0;
        armorSlot = 0;
        exceedsMagicCapacity = 0;

        StringBuilder keyText = new StringBuilder(extractStructuredItemKey(data));
        String effectsText = extractStructuredItemValue(data);
        shapeID = consumeShapeId(keyText) & 0xFF;
        materialID = consumeMaterialId(keyText) & 0xFF;
        String normalizedArmorName = Globals.staticDataMgr.formatMaterialName(keyText.toString(), materialID & 0xFF);
        int armorIndex = findWorldItemIndexByName(Globals.staticDataMgr.armors, normalizedArmorName);
        key = armorIndex & 0xFFFF;
        if ((key & 0xFFFF) == 0 || (materialID & 0xFF) > 0x0F) {
            Globals.gameServer.pushMessage("Invalid armor " + data + " created - discarded.");
            return;
        }

        applyArmorTemplateStats();
        if (!effectsText.isEmpty()) {
            parseEffects(effectsText);
        }
    }

    /**
     * Native: Armor::Armor @005275D2.
     * Fully ported.
     */
    public Armor(int shapeId, int materialId, int armorIndex) {
        shapeID = shapeId & 0xFF;
        materialID = materialId & 0xFF;
        key = armorIndex & 0xFF;
        exceedsMagicCapacity = 0;
        applyArmorTemplateStats();
    }

    /**
     * Java support wrapper around native Armor construction for item-name probing.
     * not ported.
     */
    public static Armor createByServiceName(String data) {
        if (data == null || data.isBlank()) {
            return null;
        }

        StringBuilder keyText = new StringBuilder(extractStructuredItemKey(data));
        String effectsText = extractStructuredItemValue(data);
        int shapeId = consumeShapeId(keyText);
        int materialId = consumeMaterialId(keyText);
        String normalizedArmorName = Globals.staticDataMgr.formatMaterialName(keyText.toString(), materialId & 0xFF);
        int armorIndex = findWorldItemIndexByName(Globals.staticDataMgr.armors, normalizedArmorName);
        if (armorIndex == 0 || materialId > 0x0F) {
            return null;
        }

        Armor armor = new Armor(shapeId, materialId, armorIndex);
        if (!effectsText.isEmpty()) {
            armor.parseEffects(effectsText);
        }
        return armor;
    }

    /**
     * Native support extracted from ItemAssortmentGenerator::appendConcreteEquipmentCandidates @005247DB.
     */
    public static Armor createByTemplateIds(int armorIndex, int shapeId, int materialId) {
        if (armorIndex <= 0 || materialId > 0x0F) {
            return null;
        }
        return new Armor(shapeId, materialId, armorIndex);
    }

    /**
     * Java support wrapper around native Armor::Armor @005275D2.
     * not ported.
     */
    public static Armor createFromNativeIds(int shapeId, int materialId, int armorIndex) {
        return new Armor(shapeId, materialId, armorIndex);
    }

    /**
     * Native: Armor::Armor(copy) @00527992.
     * Fully ported.
     */
    public Armor copyFrom(Armor source) {
        super.copyFrom(source);
        armorSlot = source.armorSlot;
        statData.assign(source.statData);
        return this;
    }

    /**
     * vtbl +0x38: Armor::useAndConsume @00527A13.
     * Fully ported.
     */
    @Override
    public Item useAndConsume(Unit target) {
        if ((armorSlot & 0xFF) == 0) {
            Globals.gameServer.pushMessage("Illegal armor");
            return this;
        }
        if (target.isHumanoidToken() == 0) {
            return this;
        }

        Humanoid humanoid = (Humanoid) target;
        int slotIndex = (armorSlot & 0xFF) - 1;
        Item previous = humanoid.items[slotIndex];
        if (previous != null) {
            previous.takeOff(target);
        }

        humanoid.items[slotIndex] = this;
        target.mModifiers.statMods.add(statData);
        target.unitStatData.add(statData);
        target.refreshEncumbrance(weight);
        target.changedValues |= UnitDirtyFlags.ARMOR_DEFENCE_ABSORBTION.value;
        applyEffectsOnEquip(target);
        return previous;
    }

    /**
     * vtbl +0x3C: Armor::takeOff @00527B21.
     * Fully ported.
     */
    @Override
    public void takeOff(Unit target) {
        if (target.isHumanoidToken() == 0) {
            return;
        }

        Humanoid humanoid = (Humanoid) target;
        target.refreshEncumbrance(-weight);
        target.mModifiers.statMods.subtract(statData);
        target.unitStatData.subtract(statData);

        int slotIndex = (armorSlot & 0xFF) - 1;
        humanoid.items[slotIndex] = null;

        target.changedValues |= UnitDirtyFlags.ARMOR_DEFENCE_ABSORBTION.value;
        applyEffectsOnTakeOff(target);
    }

    /**
     * vtbl +0x40: Armor::splitOne @00527BBA.
     * Fully ported.
     */
    @Override
    public Item splitOne() {
        count = ((count & 0xFFFF) - 1) & 0xFFFF;
        Armor copy = new Armor().copyFrom(this);
        copy.count = 1;
        return copy;
    }

    /**
     * vtbl +0x44: Armor::copyItemVirtual @00544720.
     * Fully ported.
     */
    @Override
    public Item copyItemVirtual() {
        return new Armor().copyFrom(this);
    }

    /**
     * vtbl +0x4C: Armor::recalculatePrice @00527862.
     * Fully ported.
     */
    @Override
    public int recalculatePrice() {
        Material shape = Globals.staticDataMgr.shapes.get(shapeID & 0xFF);
        Material material = Globals.staticDataMgr.materials.get(materialID & 0xFF);
        price = (int) (worldItem.getAttribute(WorldItemColumn.PRICE) * material.attributes.price * shape.attributes.price + 0.5d);

        int effectScoreSum = 0;
        for (Effect effect : effects) {
            if (effect.isA(PRICE)) {
                price = effect.mValue.getFull();
                return price;
            }
            effectScoreSum += effect.calculateScore();
        }

        price += calculateArmorScorePriceBonus(effectScoreSum);
        if (price > MAX_PRICE) {
            price = MAX_PRICE;
        }
        return price;
    }

    /**
     * Native: Armor::applyArmorTemplateStats @0052765B.
     * Fully ported.
     */
    private void applyArmorTemplateStats() {
        armorSlot = 0;
        int armorIndex = key & 0xFFFF;
        WorldItem armorInfo = Globals.staticDataMgr.armors.get(armorIndex);
        worldItem = armorInfo;
        armorSlot = armorInfo.getAttribute(WorldItemColumn.SLOT) & 0xFF;
        if (armorSlot >= 0x0D) {
            Globals.gameServer.pushMessage("Invalid armor part " + armorSlot + " created - discarded.");
            return;
        }

        type = 1;

        Material shape = Globals.staticDataMgr.shapes.get(shapeID & 0xFF);
        Material material = Globals.staticDataMgr.materials.get(materialID & 0xFF);

        statData.absorbtion = (short) (armorInfo.getAttribute(WorldItemColumn.ABSORPTION)
                * material.attributes.absorption
                * shape.attributes.absorption);
        statData.defence = (short) (armorInfo.getAttribute(WorldItemColumn.DEFENCE)
                * material.attributes.defence
                * shape.attributes.defence
                + 0.5d);
        magicVolume = (short) (int) (material.attributes.magicVolume * shape.attributes.magicVolume);
        weight = (short) (int) (armorInfo.getAttribute(WorldItemColumn.WEIGHT)
                * material.attributes.weight
                * shape.attributes.weight
                + 0.5d);
        recalculatePrice();
        hash = packIDs(armorSlot, shapeID, materialID, armorIndex & 0xFF);
    }

    /**
     * vtbl +0x54: Armor::writeInfoPacket @00527C46.
     * Fully ported.
     */
    @Override
    public void writeInfoPacket(ItemListAction action, ItemInfoPacketHeader header) {
        action.appendItemInfoIntDescriptor(1, price, header);
        header.flags = 0;
        int armorFlags = worldItem.getAttribute(WorldItemColumn.SUITABLE_FOR);
        if ((armorFlags & 1) != 0) {
            header.flags |= 0x02;
        }
        if ((armorFlags & 2) != 0) {
            header.flags |= 0x04;
        }
        action.appendItemInfoByteDescriptor(0x0F, statData.defence, header);
        if (statData.absorbtion > 0) {
            action.appendItemInfoByteDescriptor(0x10, statData.absorbtion, header);
        }
        writeEffectInfoDescriptors(action, header);
    }

    /**
     * vtbl +0x08: Armor::serialize @0052DAD0.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        super.serialize(ar);                    // Item::serialize
        ar.serialize(statData);                 // StatData::Serialize(0x16)

        if (!ar.isStoring()) {
            armorSlot = ar.readByte() & 0xFF;

            worldItem = Globals.staticDataMgr.armors.get(key & 0xFFFF);
        } else {
            ar.writeByte(armorSlot);
        }
    }

    /**
     * Native support extracted from Armor::recalculatePrice @00527862 and
     * calculateEquipmentScorePriceBonus @0051E41A.
     * Fully ported.
     */
    private static int calculateArmorScorePriceBonus(int score) {
        double pow = Math.pow(1.5d, (double) score / 70.0d);
        return (int) ((pow + 1.0d) * score * 50.0d);
    }

}
