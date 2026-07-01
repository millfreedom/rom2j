package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.column.MagicColumn;
import ua.millfreedom.rom2.model.column.SpellColumn;
import ua.millfreedom.rom2.model.action.ItemListAction;
import ua.millfreedom.rom2.model.actiondata.ActionPayloads;
import ua.millfreedom.rom2.model.actiondata.ItemInfoPacketHeader;
import ua.millfreedom.rom2.model.enums.EffectId;
import ua.millfreedom.rom2.model.spell.SpellInfo;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.unit.humanoid.Humanoid;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import static ua.millfreedom.rom2.model.EffectType.*;

public class Item extends Token {
    private static final int ITEM_TYPE_POTION = 3;
    private static final int ITEM_TYPE_SCROLL = 4;
    public static final int ITEM_TYPE_BOOK = 5;
    private static final int INLINE_QUANTITY_FLAG = 0x80;
    private static final int DYNAMIC_PAYLOAD_FLAG = 0x20;
    private static final int INLINE_QUANTITY_MASK = 0x3F;
    private static final int DYNAMIC_ENTRY_COUNT_MASK = 0x0F;
    private static final int MAGIC_ITEM_SLOT = 0x0E;
    private static final int CONTROLLED_HUMANOID_SAVE_INTERVAL_MS = 15_000;

    //0x3C
    public WorldItem worldItem;
    //0x40
    public int hash;
    //0x42
    public int count;
    //0x44
    public int type;
    //0x45
    public int shapeID;
    //0x46
    public int materialID;
    //0x47
    public int field7_0x43;
    //0x48
    public int magicVolume;
    //0x4A
    public int weight;
    //0x4C
    public int trackedShopReferenceCount;
    //0x4D
    public int inventoryPayloadTag;
    //0x50
    public int exceedsMagicCapacity;

    /**
     * Native: Item::New @00522E5C.
     * Fully ported.
     */
    public Item() {
        worldItem = null;
        hash = 0;
        count = 1;
        type = 0;
        shapeID = 0;
        materialID = 0;
        key = 0;
        weight = 1;
        scenarioObjectId = 0;
        trackedShopReferenceCount = 0;
        inventoryPayloadTag = 0;
        exceedsMagicCapacity = 0;
    }

    /**
     * Native: Item::CreateObject @00522DA8.
     * Fully ported.
     */
    public static Item createObject() {
        return new Item();
    }

    /**
     * Native: Item::Item(copy) @005232BB.
     * Fully ported.
     */
    public Item copyFrom(Item source) {
        copyTokenStateFrom(source);
        worldItem = source.worldItem;
        effects.clear();
        for (Effect effect : source.effects) {
            effects.add(new Effect().copyFrom(effect));
        }
        hash = source.hash;
        count = source.count;
        type = source.type;
        shapeID = source.shapeID;
        materialID = source.materialID;
        magicVolume = source.magicVolume;
        weight = source.weight;
        trackedShopReferenceCount = source.trackedShopReferenceCount;
        inventoryPayloadTag = source.inventoryPayloadTag;
        return this;
    }

    /**
     * Native: Item::findCastSpellEffect @005238C5.
     * Fully ported.
     */
    public Effect findCastSpellEffect() {
        for (Effect effect : effects) {
            if (effect.isA(EffectId.CAST_SPELL)) {
                return effect;
            }
        }
        return null;
    }

    /**
     * Native: Item::readFromArchive @00522E40.
     * Fully ported.
     */
    public static Item readFromArchive(CArchive ar) throws IOException {
        return ar.readObject(Item.class);
    }

    /**
     * Native: Item::RestoreContext @00546590.
     * Fully ported.
     */
    public static Item restoreContextToken(Object tokenOrRef) {
        Object resolved = Globals.gameServer.lookupPointerMapOrNull(tokenOrRef);
        return (resolved instanceof Item item) ? item : null;
    }

    /**
     * Native support extracted from PackIDs @00544120.
     * Fully ported.
     */
    protected static int packIDs(int type, int shape, int material, int id) {
        return (((material & 0xFF) << 12)
                | ((type & 0xFF) << 8)
                | ((shape & 0xFF) << 5)
                | (id & 0xFF)) & 0xFFFF;
    }

    /**
     * Native: Item::GetSlot @0053BFF0.
     * Fully ported.
     */
    public int getSlot() {
        return (hash >>> 8) & 0x0F;
    }

    /**
     * Native: ParseStructuredLine @004FA6C6.
     * Fully ported.
     */
    public static String extractStructuredItemKey(String inputLine) {
        int openBrace = inputLine.indexOf('{');
        if (openBrace < 0) {
            return inputLine;
        }
        return inputLine.substring(0, openBrace).stripTrailing();
    }

    /**
     * Native: ParseStructuredLine @004FA6C6.
     * Fully ported.
     */
    public static String extractStructuredItemValue(String inputLine) {
        int openBrace = inputLine.indexOf('{');
        if (openBrace < 0) {
            return "";
        }
        int closeBrace = inputLine.indexOf('}');
        if (closeBrace < 0) {
            closeBrace = inputLine.length();
        }
        String left = inputLine.substring(0, closeBrace);
        int valueOffset = openBrace + 1;
        if (valueOffset >= left.length()) {
            return "";
        }
        return left.substring(valueOffset);
    }

    /**
     * Native support wrapper around CStaticDataMgr::FindShapeID @004FA437 for item string constructors.
     */
    protected static int consumeShapeId(StringBuilder keyText) {
        return Globals.staticDataMgr.findShapeID(keyText.toString(), keyText);
    }

    /**
     * Native support wrapper around CStaticDataMgr::FindMaterialID @004FA583 for item string constructors.
     */
    protected static int consumeMaterialId(StringBuilder keyText) {
        return Globals.staticDataMgr.findMaterialID(keyText.toString(), keyText);
    }

    /**
     * Native support extracted from CStaticDataMgr::FindWorldItemIndexByName @0053EB10.
     * Fully ported.
     */
    protected static int findWorldItemIndexByName(List<WorldItem> items, String baseName) {
        for (int itemIndex = items.size() - 1; itemIndex >= 1; itemIndex--) {
            WorldItem itemInfo = items.get(itemIndex);
            if (baseName.equals(itemInfo.name)) {
                return itemIndex;
            }
        }
        return 0;
    }

    /**
     * Native: Item::GetIdHash @00544160, low-word usage in FUN_005259AF.
     * Fully ported.
     */
    public int getIdHashLowWord() {
        return hash & 0xFFFF;
    }

    /**
     * Native: Item::isShapeMaterialCombinationAllowed @0052393E.
     * Fully ported.
     */
    public boolean isShapeMaterialCombinationAllowed() {
        if (worldItem == null) {
            return false;
        }
        return ((worldItem.materialMasks[shapeID] & 0xFFFF) & (1 << (materialID & 0x1F))) != 0;
    }

    /**
     * Native: Item::addOrReplaceEffect @0052397E.
     * Fully ported.
     */
    public void addOrReplaceEffect(Effect candidate) {
        int candidateUsableBy = candidate.getMagicValue(MagicColumn.USABLE_BY);
        for (Effect existing : effects) {
            int existingUsableBy = existing.getMagicValue(MagicColumn.USABLE_BY);
            if (candidateUsableBy != 3 && (candidateUsableBy & existingUsableBy) == 0) {
                return;
            }
            if ((existing.id & 0xFF) == (candidate.id & 0xFF)) {
                existing.mValue.setFull(candidate.mValue.getFull());
                return;
            }
        }

        effects.add(candidate);
    }

    /**
     * Native: Item::parseEffects @00523913.
     * Fully ported.
     */
    public void parseEffects(String effectsText) {
        Effect.parseEffectsList(effectsText, effects);
        recalculatePrice();
    }

    /**
     * Native support extracted from calculateItemEffectScore @00527273.
     * Fully ported.
     */
    public int calculateEffectScore() {
        int score = 0;
        for (Effect effect : effects) {
            score += effect.calculateScore();
        }
        return score;
    }

    /**
     * Native: Item::matchesInventoryIdentity @0052350B.
     * Fully ported.
     */
    public boolean matchesInventoryIdentity(Item other) {
        if (getIdHashLowWord() != other.getIdHashLowWord()) {
            return false;
        }
        int thisCanStack = canStackInInventory();
        int otherCanStack = other.canStackInInventory();
        if (thisCanStack != 0 && otherCanStack != 0) {
            return true;
        }
        if (thisCanStack != 0 || otherCanStack != 0) {
            return false;
        }
        if (effects.size() != other.effects.size()) {
            return false;
        }
        for (int i = 0; i < effects.size(); i++) {
            Effect left = effects.get(i);
            Effect right = other.effects.get(i);
            if (!left.matchesInventoryIdentity(right)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Native support extracted from readSavedCharacterItemPayload @004ED5F5.
     * Fully ported.
     */
    public static Item readSavedCharacterItemPayload(ByteBuffer payloadCursor) {
        payloadCursor.order(ByteOrder.LITTLE_ENDIAN);
        int packedHash = Short.toUnsignedInt(payloadCursor.getShort());
        int flags = Byte.toUnsignedInt(payloadCursor.get());
        if (packedHash == 0) {
            return new Item();
        }

        int quantity = 1;
        int dynamicPayloadEntries = 0;
        if ((flags & INLINE_QUANTITY_FLAG) != 0) {
            quantity = flags & INLINE_QUANTITY_MASK;
        } else if ((flags & DYNAMIC_PAYLOAD_FLAG) == 0) {
            quantity = Short.toUnsignedInt(payloadCursor.getShort());
        } else {
            dynamicPayloadEntries = flags & DYNAMIC_ENTRY_COUNT_MASK;
            payloadCursor.position(payloadCursor.position() + Integer.BYTES);
        }

        Item item = createSavedCharacterItemFromPackedHash(packedHash);
        if ((flags & DYNAMIC_PAYLOAD_FLAG) == 0) {
            item.count = quantity;
        } else {
            readSavedCharacterItemEffects(payloadCursor, item, dynamicPayloadEntries);
        }

        item.recalculatePrice();
        int effectScore = item.calculateEffectScore();
        item.exceedsMagicCapacity = ((item.magicVolume * 3) / 2 < effectScore
                && item.getSlot() != MAGIC_ITEM_SLOT) ? 1 : 0;
        item.magicVolume -= (short) effectScore;
        return item;
    }

    /**
     * Native support extracted from readSavedCharacterItemPayload @004ED5F5 item construction branch.
     * Fully ported.
     */
    private static Item createSavedCharacterItemFromPackedHash(int packedHash) {
        int slot = (packedHash >>> 8) & 0x0F;
        int shapeId = (packedHash >>> 5) & 0x07;
        int materialId = (packedHash >>> 12) & 0x0F;
        int itemIndex = packedHash & (slot == MAGIC_ITEM_SLOT ? 0xFF : 0x1F);
        return switch (slot) {
            case 1 -> new Weapon(shapeId, materialId, itemIndex);
            case 2 -> new Shield(shapeId, materialId, itemIndex);
            case MAGIC_ITEM_SLOT -> new MagicItem(Globals.staticDataMgr.magicItems.get(itemIndex).name);
            default -> new Armor(shapeId, materialId, itemIndex);
        };
    }

    /**
     * Native support extracted from readSavedCharacterItemPayload @004ED5F5 effect payload loop.
     * Fully ported.
     */
    private static void readSavedCharacterItemEffects(ByteBuffer payloadCursor, Item item, int dynamicPayloadEntries) {
        int remainingEntries = dynamicPayloadEntries;
        while (remainingEntries != 0) {
            Effect effect = new Effect();
            int effectId = Byte.toUnsignedInt(payloadCursor.get());
            effect.id = effectId;
            effect.mValue.setS1(Byte.toUnsignedInt(payloadCursor.get()));
            remainingEntries--;

            if (effectId == EffectId.CAST_SPELL.id) {
                payloadCursor.get();
                effect.mValue.setB3(Byte.toUnsignedInt(payloadCursor.get()));
                remainingEntries--;
            } else if (isNetworkElementalDamageEffect(effectId)) {
                payloadCursor.get();
                effect.mValue.setB2(Byte.toUnsignedInt(payloadCursor.get()));
                remainingEntries--;
            }
            item.effects.add(effect);
        }
    }

    /**
     * Native: Item::appendNetworkItemPayload @005241BF.
     * Fully ported.
     */
    public void appendNetworkItemPayload(ItemListAction action, boolean includeInventoryPayloadTag) {
        ActionPayloads.appendTrailingShort(action.trailingDataLength, action.trailingData, getIdHashLowWord());
        int flagsOffset = ActionPayloads.appendTrailingByte(action.trailingDataLength, action.trailingData, 0);
        int flags = 0;

        int effectCount = effects.size();
        if (effectCount == 0 || type == ITEM_TYPE_POTION || type == ITEM_TYPE_SCROLL || count == 0) {
            if ((count & 0xFFFF) < 0x40) {
                flags = (count & 0xFF) | 0x80;
            } else {
                ActionPayloads.appendTrailingShort(action.trailingDataLength, action.trailingData, count);
            }
            ActionPayloads.rewriteTrailingByte(action.trailingDataLength, action.trailingData, flagsOffset, flags);
        } else {
            flags |= 0x20;
            ActionPayloads.rewriteTrailingByte(action.trailingDataLength, action.trailingData, flagsOffset, flags);
            ActionPayloads.appendTrailingInt(action.trailingDataLength, action.trailingData, price);

            int effectPayloadCount = 0;
            for (Effect effect : effects) {
                if ((effect.id & 0xFF) == EffectId.PRICE.id) {
                    continue;
                }
                int effectId = effect.id & 0xFF;
                ActionPayloads.appendTrailingByte(action.trailingDataLength, action.trailingData, effectId);
                ActionPayloads.appendTrailingByte(action.trailingDataLength, action.trailingData, effect.mValue.getB1());

                if (effectId == EffectId.CAST_SPELL.id) {
                    flags |= 0x10;
                    ActionPayloads.rewriteTrailingByte(action.trailingDataLength, action.trailingData, flagsOffset, flags);
                    ActionPayloads.appendTrailingByte(action.trailingDataLength, action.trailingData, 0x32);
                    ActionPayloads.appendTrailingByte(action.trailingDataLength, action.trailingData, effect.mValue.getB3());
                    effectPayloadCount += 2;
                } else if (isNetworkElementalDamageEffect(effectId)) {
                    ActionPayloads.appendTrailingByte(action.trailingDataLength, action.trailingData, effectId);
                    ActionPayloads.appendTrailingByte(action.trailingDataLength, action.trailingData, effect.mValue.getB2());
                    effectPayloadCount += 2;
                } else {
                    effectPayloadCount += 1;
                }
            }
            flags = (flags & 0xF0) | (effectPayloadCount & 0x0F);
            ActionPayloads.rewriteTrailingByte(action.trailingDataLength, action.trailingData, flagsOffset, flags);
        }

        if (scenarioObjectId != 0) {
            flags |= 0x40;
            ActionPayloads.rewriteTrailingByte(action.trailingDataLength, action.trailingData, flagsOffset, flags);
            scenarioObjectId = 0;
        }
        if (includeInventoryPayloadTag) {
            ActionPayloads.appendTrailingByte(action.trailingDataLength, action.trailingData, inventoryPayloadTag);
        }
    }

    /**
     * Native: Item::appendItemInfoPacket @00524147.
     * Fully ported.
     */
    public void appendItemInfoPacket(ItemListAction action) {
        int headerOffset = action.reserveItemInfoPacketHeader();
        ItemInfoPacketHeader header = new ItemInfoPacketHeader();
        header.packedItemHash = getIdHashLowWord();
        header.count = count;
        if ((hash & 0xFFFF) != 0) {
            writeInfoPacket(action, header);
        }
        if (scenarioObjectId != 0) {
            header.flags |= 0x40;
            scenarioObjectId = 0;
        }
        header.writeTo(action, headerOffset);
    }

    /**
     * Native support extracted from Item::appendNetworkItemPayload @005241BF.
     */
    private static boolean isNetworkElementalDamageEffect(int effectId) {
        return effectId >= EffectId.DAMAGE_FIRE.id && effectId <= EffectId.DAMAGE_ASTRAL.id;
    }

    /**
     * vtbl +0x38: Item::useAndConsume @00523613.
     * Fully ported.
     */
    public Item useAndConsume(Unit target) {
        if (target.m_nHP <= 0) {
            return this;
        }

        for (Effect effect : effects) {
            if (effect.type == PERMANENT) {
                effect.type |= SINGLE_USE;
            }
            // Native dispatch: Effect vtbl +0x3C (Effect::applyToTarget @0051CE12).
            effect.applyToTarget(target);
        }

        target.refreshPrice();

        String itemName = worldItem.name;
        if (itemName.contains("ody")
                || itemName.contains("ind")
                || itemName.contains("eaction")
                || itemName.contains("pirit")) {
            Player ownerPlayer = target.owner;
            int elapsed = Globals.currentTickMillis() - ownerPlayer.lastSaveTick;
            if (Integer.compareUnsigned(elapsed, CONTROLLED_HUMANOID_SAVE_INTERVAL_MS) > 0) {
                Globals.gameServer.saveControlledHumanoid((Humanoid) target);
            }
        }

        return null;
    }

    /**
     * vtbl +0x3C: Item::takeOff @00523754.
     * Fully ported.
     */
    public void takeOff(@SuppressWarnings("unused") Unit target) {
        Globals.gameServer.pushMessage("Unknown item takeoff.");
    }

    /**
     * Native: Item::applyEffectsOnEquip @0052377E.
     * Fully ported.
     */
    protected void applyEffectsOnEquip(Unit target) {
        for (Effect effect : effects) {
            effect.applyOnAdd(target);
        }
    }

    /**
     * Native: Item::applyEffectsOnTakeOff @005237C9.
     * Fully ported.
     */
    protected void applyEffectsOnTakeOff(Unit target) {
        for (Effect effect : effects) {
            effect.applyOnRemove(target);
        }
    }

    /**
     * vtbl +0x40: Item::splitOne @00523839.
     * Fully ported.
     */
    public Item splitOne() {
        count = ((count & 0xFFFF) - 1) & 0xFFFF;
        Item copy = new Item().copyFrom(this);
        copy.count = 1;
        return copy;
    }

    /**
     * vtbl +0x44: Item::copyItemVirtual @00544060.
     * Fully ported.
     */
    public Item copyItemVirtual() {
        return new Item().copyFrom(this);
    }

    /**
     * vtbl +0x14: Item::updateRegen @0052381F.
     * Fully ported.
     */
    @Override
    public void updateRegen() {
    }

    /**
     * vtbl +0x18: Item::update @00523814.
     * Fully ported.
     */
    @Override
    public Object update() {
        return null;
    }

    /**
     * vtbl +0x28: Item::isItemToken @005440E0.
     * Fully ported.
     */
    @Override
    public int isItemToken() {
        return 1;
    }

    /**
     * Native: Item::isZeroCountItem @00543960.
     * Fully ported.
     */
    public boolean isZeroCountItem() {
        return (count & 0xFFFF) == 0;
    }

    /**
     * vtbl +0x48: Item::vtblHookSlot48 @0052382A.
     * Fully ported.
     */
    public int vtblHookSlot48(@SuppressWarnings("unused") int param1, @SuppressWarnings("unused") int param2) {
        return 0;
    }

    /**
     * vtbl +0x4C: Item::recalculatePrice @0052340F.
     * Fully ported.
     */
    public int recalculatePrice() {
        price = worldItem.values.getFirst();

        if (type == ITEM_TYPE_BOOK) {
            Effect firstEffect = effects.isEmpty() ? null : effects.getFirst();
            if (firstEffect == null) {
                price = 0;
            } else {
                int spellIndex = firstEffect.mValue.getS1Signed();
                SpellInfo spellInfo = Globals.staticDataMgr.spells.get(spellIndex);
                price = spellInfo.getAttribute(SpellColumn.BOOK_COST);
            }
        }

        if (type == ITEM_TYPE_SCROLL) {
            price = worldItem.values.getFirst();
        }

        return price;
    }

    /**
     * vtbl +0x50: Item::canStackInInventory @005234C2.
     * Fully ported.
     */
    public int canStackInInventory() {
        if (type != ITEM_TYPE_POTION && type != ITEM_TYPE_SCROLL && !effects.isEmpty()) {
            return 0;
        }
        return 1;
    }

    /**
     * Native support helper: calculateEquipmentScorePriceBonus @0051E41A.
     * Fully ported.
     */
    protected static int calculateEquipmentScorePriceBonus(int score) {
        double pow = Math.pow(1.5d, (double) score / 70.0d);
        return (int) ((pow + 1.0d) * score * 50.0d);
    }

    /**
     * vtbl +0x54: Item::writeInfoPacket @005244D6.
     * Fully ported.
     */
    public void writeInfoPacket(ItemListAction action, ItemInfoPacketHeader header) {
        if (price == -1) {
            header.flags = 1;
            action.appendItemInfoIntDescriptor(1, 0, header);
            return;
        }

        action.appendItemInfoIntDescriptor(1, price, header);
        header.flags |= 7;
        if (type == ITEM_TYPE_BOOK) {
            header.flags &= ~0x02;
        }
        writeEffectInfoDescriptors(action, header);
        if (type == ITEM_TYPE_POTION || type == ITEM_TYPE_SCROLL) {
            header.flags &= ~0x20;
        }
    }

    /**
     * Native: Item::writeEffectInfoDescriptors @0052457C.
     * Fully ported.
     */
    protected void writeEffectInfoDescriptors(ItemListAction action, ItemInfoPacketHeader header) {
        if (effects.isEmpty()) {
            return;
        }

        header.flags |= 0x20;
        action.appendItemInfoByteDescriptor(0x33, 0, header);
        for (Effect effect : effects) {
            int effectId = effect.id & 0xFF;
            action.appendItemInfoByteDescriptor(effectId, effect.mValue.getB1(), header);
            if (effectId == EffectId.CAST_SPELL.id) {
                header.flags |= 0x10;
                action.appendItemInfoByteDescriptor(0x32, effect.mValue.getB3(), header);
            } else if (isNetworkElementalDamageEffect(effectId)) {
                action.appendItemInfoByteDescriptor(effectId, effect.mValue.getB2(), header);
            }
        }
    }

    /**
     * vtbl +0x08: Item::serialize @0052D65A.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        super.serialize(ar);          // Token::serialize
        serializeEffectsList(ar);     // CList<Effect>::Serialize

        if (!ar.isStoring()) {
            hash = ar.readUShort();
            count = ar.readUShort();
            type = ar.readByte() & 0xFF;
            shapeID = ar.readByte() & 0xFF;
            materialID = ar.readByte() & 0xFF;
            magicVolume = ar.readUShort();
            weight = ar.readUShort();
            field7_0x43 = ar.readByte() & 0xFF;

            int size = Globals.staticDataMgr.magicItems.size();
            int tokenKey = key & 0xFFFF;
            if (tokenKey < size) {
                worldItem = Globals.staticDataMgr.magicItems.get(tokenKey);
            } else {
                worldItem = null;
            }
        } else {
            ar.writeShort(hash);
            ar.writeShort(count);
            ar.writeByte(type);
            ar.writeByte(shapeID);
            ar.writeByte(materialID);
            ar.writeShort(magicVolume);
            ar.writeShort(weight);
            ar.writeByte(field7_0x43);
        }
    }

}
