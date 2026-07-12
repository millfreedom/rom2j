package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.column.MagicColumn;
import ua.millfreedom.rom2.model.column.SpellColumn;
import ua.millfreedom.rom2.model.column.WorldItemColumn;
import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.model.enums.EffectId;
import ua.millfreedom.rom2.model.enums.MagicItemId;
import ua.millfreedom.rom2.model.enums.SpellId;

import java.util.List;

import static ua.millfreedom.rom2.model.EffectType.PERMANENT;
import static ua.millfreedom.rom2.model.ShelfFlagValues.*;
import static ua.millfreedom.rom2.model.enums.SpellId.*;

/**
 * Native assortment generator shared by shops and monster loot:
 * ctor/init @0052469E, price-targeted generate @00525B41, generate @00525F67.
 */
public final class ItemAssortmentGenerator {
    private static final String RANDOM_ITEM_KIND_WEAPON = "Weapon";
    private static final String RANDOM_ITEM_KIND_ARMOR = "Armor";
    private static final String RANDOM_ITEM_KIND_SHIELD = "Shield";
    private static final String RANDOM_ITEM_KIND_POTION = "Potion";
    private static final int RANDOM_ITEM_RETRY_LIMIT = 0x32;
    private static final int RANDOM_POTION_RETRY_LIMIT = 0x14;
    private static final int RANDOM_WEAPON_MIN_INDEX = 2;
    private static final int RANDOM_ARMOR_MIN_INDEX = 3;
    private static final int RANDOM_SHIELD_MIN_INDEX = 1;
    private static final int[] RANDOM_ITEM_WEAPON_EXCLUDED_INDICES = {0x0D, 0x0E};
    private static final int ITEM_KIND_ARMOR = 1;
    private static final int ITEM_KIND_WEAPON = 2;
    private static final int ITEM_KIND_SHIELD = 7;
    private static final int ITEM_KIND_STAFF = 8;
    private static final int PREPARED_EQUIPMENT_TYPE_MASK =
            TYPE_WEAPON.value | TYPE_SHIELD.value | TYPE_ARMOR.value | TYPE_OTHER.value | TYPE_STAFF.value;

    // Native DAT_005d07f8 / DAT_005d0800. These are 1-based runtime spell ids,
    // so they intentionally match CStaticDataMgr.spells / SpellId rather than 0-based SpellText indices.
    private static final SpellId[] FIGHTER_CAST_SPELLS = {STONE_CURSE, DRAIN_LIFE};
    private static final SpellId[] STAFF_CAST_SPELLS = {
            FIRE_ARROW, LIGHTNING, PRISMATIC_SPRAY, STONE_CURSE, DRAIN_LIFE, ICE_MISSILE, DIAMOND_DUST
    };
    private static final MagicItemId[] BONUS_POTIONS = {
            MagicItemId.POTION_HEALTH_REGENERATION,
            MagicItemId.POTION_MEDIUM_HEALING,
            MagicItemId.POTION_BIG_HEALING,
            MagicItemId.POTION_MANA_REGENERATION,
            MagicItemId.POTION_MEDIUM_MANA,
            MagicItemId.POTION_BIG_MANA
    };

    //0x00
    public final CustomList<Double> inverseDistanceTable = new CustomList<>(Double.class);
    //0x18
    public double edgeValue;
    //0x20
    public final CustomList<Item> candidates = new CustomList<>(Item.class);
    //0x34
    public int minPrice;
    //0x38
    public int maxPrice;
    //0x40
    public double indexShift;
    //0x48
    public double priceStep;
    //0x54
    public int selectionMask;

    /**
     * Native: ItemAssortmentGenerator::ItemAssortmentGenerator @0052469E.
     * Fully ported.
     */
    public ItemAssortmentGenerator() {
        minPrice = 0;
        maxPrice = 0;
        initInverseDistanceTable();
    }

    /**
     * Native: CreateRandomItemForPriceRange @00524070.
     * Fully ported.
     */
    public static Item createRandomItemForPriceRange(int minPrice, int maxPrice) {
        double roll = randomUnitFloat();
        if (roll < 0.4d) {
            return createRandomItemOfKindForPriceRange(RANDOM_ITEM_KIND_WEAPON, minPrice, maxPrice);
        }
        if (roll < 0.65d) {
            return createRandomItemOfKindForPriceRange(RANDOM_ITEM_KIND_ARMOR, minPrice, maxPrice);
        }
        if (roll < 0.8d) {
            return createRandomItemOfKindForPriceRange(RANDOM_ITEM_KIND_SHIELD, minPrice, maxPrice);
        }
        return createRandomItemOfKindForPriceRange(RANDOM_ITEM_KIND_POTION, minPrice, maxPrice);
    }

    /**
     * Native: CreateRandomItemOfKindForPriceRange @00523ABD.
     * Fully ported.
     */
    public static Item createRandomItemOfKindForPriceRange(String itemKind, int minPrice, int maxPrice) {
        if (maxPrice - minPrice < 10) {
            return null;
        }
        if (RANDOM_ITEM_KIND_WEAPON.equals(itemKind)) {
            return createRandomWeaponForPriceRange(minPrice, maxPrice);
        }
        if (RANDOM_ITEM_KIND_ARMOR.equals(itemKind)) {
            return createRandomArmorForPriceRange(minPrice, maxPrice);
        }
        if (RANDOM_ITEM_KIND_SHIELD.equals(itemKind)) {
            return createRandomShieldForPriceRange(minPrice, maxPrice);
        }
        if (RANDOM_ITEM_KIND_POTION.equals(itemKind) && maxPrice >= 500) {
            return createRandomPotionForPriceRange(minPrice, maxPrice);
        }
        return createRandomItemForPriceRange(minPrice, maxPrice);
    }

    /**
     * Native: ItemAssortmentGenerator::ClearCandidates @0052545E.
     * Fully ported.
     */
    public void clearCandidates() {
        candidates.clear();
    }

    /**
     * Native: ItemAssortmentGenerator::PrepareCandidates @005254E7.
     * Fully ported.
     */
    public void prepareCandidates(ShopAssortmentEntry entry) {
        clearCandidates();
        minPrice = entry.minPrice;
        maxPrice = entry.maxPrice;
        selectionMask = entry.selectionMask;
        appendPreparedCandidates();
    }

    /**
     * Native support extracted from ItemAssortmentGenerator::PrepareCandidates @005254E7.
     * Fully ported.
     */
    public void prepareCandidates(int minPrice, int maxPrice, int selectionMask) {
        clearCandidates();
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.selectionMask = selectionMask;
        appendPreparedCandidates();
    }

    /**
     * Native support extracted from ItemAssortmentGenerator::PrepareCandidates @005254E7.
     * Fully ported.
     */
    private void appendPreparedCandidates() {
        int equipmentMaskBase = selectionMask & ~PREPARED_EQUIPMENT_TYPE_MASK;
        if (hasFlag(TYPE_WEAPON)) {
            appendEquipmentCandidates(Globals.staticDataMgr.weapons, equipmentMaskBase | TYPE_WEAPON.value);
        }
        if (hasFlag(TYPE_STAFF)) {
            appendEquipmentCandidates(Globals.staticDataMgr.weapons, equipmentMaskBase | TYPE_STAFF.value);
        }
        if (hasFlag(TYPE_ARMOR)) {
            appendEquipmentCandidates(Globals.staticDataMgr.armors, equipmentMaskBase | TYPE_ARMOR.value);
        }
        if (hasFlag(TYPE_SHIELD)) {
            appendEquipmentCandidates(Globals.staticDataMgr.shields, equipmentMaskBase | TYPE_SHIELD.value);
        }
        if (hasFlag(TYPE_OTHER)) {
            appendOtherCandidates();
        }
    }

    /**
     * Native: ItemAssortmentGenerator::GenerateItems @00525F67.
     * Fully ported.
     */
    public Inventory generateItems(ShopAssortmentEntry entry, Inventory output) {
        prepareCandidates(entry);
        return generateItems(entry.itemCount, entry.maxSameTypeItems, entry.minPrice, entry.maxPrice, output);
    }

    /**
     * Native support extracted from Unit::FinalizeDeath @00510A70 and
     * ItemAssortmentGenerator::GenerateItems @00525F67 CArray output path.
     * Fully ported.
     */
    public CustomList<Item> generateItemArray(ShopAssortmentEntry entry) {
        prepareCandidates(entry);
        if (candidates.isEmpty()) {
            return new CustomList<>(Item.class);
        }
        return generateItemArrayFromPreparedCandidates(
                entry.itemCount,
                entry.maxSameTypeItems,
                entry.minPrice,
                entry.maxPrice
        );
    }

    /**
     * Native: ItemAssortmentGenerator::GenerateItemsForTargetPrice @00525B41.
     * Fully ported.
     */
    public Inventory generateItemsForTargetPrice(
            int targetPrice,
            int itemCount,
            int maxSameTypeItems,
            CustomList<Item> outputItems) {
        if (candidates.isEmpty()) {
            return null;
        }

        targetPrice = Math.min(Math.max(targetPrice, minPrice), maxPrice);
        CustomList<Item> generatedItems = new CustomList<>(Item.class);
        if (hasFlag(TYPE_OTHER)) {
            appendTypeOtherSeedItems(generatedItems);
        }

        int generatedCount = 0;
        int rejectedCount = 0;
        while (generatedCount < itemCount && rejectedCount <= itemCount * 10) {
            double threshold = Utils.randPercent0To99() / 100.0d;
            int[] range = computeIndexRangeForPriceThreshold(targetPrice, threshold);
            Item candidate = selectCandidateForTargetPrice(range[0], range[1]);

            candidate.count = 2;
            Item generated = candidate.splitOne();

            if (!hasFlag(EXTRA_MAGIC)) {
                finalizeGeneratedItemCount(generated, maxSameTypeItems);
                mergeGeneratedItem(generatedItems, generated);
                generatedCount++;
                continue;
            }

            boolean keepGeneratedItem;
            if (hasOnlyFlag(EXTRA_MAGIC)) {
                keepGeneratedItem = applyRandomMagic(generated);
            } else if (Utils.randPercent0To99() < 0x33) {
                keepGeneratedItem = true;
            } else {
                keepGeneratedItem = applyRandomMagic(generated);
            }

            if (keepGeneratedItem) {
                finalizeGeneratedItemCount(generated, maxSameTypeItems);
                mergeGeneratedItem(generatedItems, generated);
                generatedCount++;
                continue;
            }

            rejectedCount++;
        }

        sortGeneratedItems(generatedItems);
        if (outputItems == null) {
            Inventory output = new Inventory();
            for (Item generated : generatedItems) {
                output.addItem(generated);
            }
            return output;
        }

        outputItems.clear();
        outputItems.addAll(generatedItems);
        return null;
    }

    /**
     * Native: ItemAssortmentGenerator::GenerateItems @00525F67.
     * Fully ported.
     */
    public Inventory generateItems(int itemCount, int maxSameTypeItems, int minPrice, int maxPrice, Inventory output) {
        if (candidates.isEmpty()) {
            return output;
        }

        CustomList<Item> generatedItems = generateItemArrayFromPreparedCandidates(
                itemCount,
                maxSameTypeItems,
                minPrice,
                maxPrice
        );
        if (output == null) {
            output = new Inventory();
        }
        for (Item generated : generatedItems) {
            output.addItem(generated);
        }
        return output;
    }

    /**
     * Native support extracted from ItemAssortmentGenerator::GenerateItems @00525F67 local generated CArray.
     * Fully ported.
     */
    private CustomList<Item> generateItemArrayFromPreparedCandidates(
            int itemCount,
            int maxSameTypeItems,
            int minPrice,
            int maxPrice
    ) {
        CustomList<Item> generatedItems = new CustomList<>(Item.class);
        if (hasFlag(TYPE_OTHER)) {
            appendBonusOtherItems(generatedItems);
        }

        int generatedCount = 0;
        int retries = 0;
        int retryLimit = Math.max(itemCount, 1) * 10;
        while (generatedCount < itemCount && retries <= retryLimit) {
            Item candidate = selectCandidateForGeneration();
            if (candidate == null) {
                break;
            }

            candidate.count = 2;
            Item generated = candidate.splitOne();

            if (hasFlag(EXTRA_MAGIC)) {
                boolean keepCommon = hasFlag(EXTRA_COMMON) && Utils.randPercent0To99() < 0x33;
                boolean keepGeneratedItem = keepCommon || applyRandomMagic(generated);
                if (!keepGeneratedItem || generated.price < minPrice || maxPrice < generated.price) {
                    retries++;
                    continue;
                }
            }

            finalizeGeneratedItemCount(generated, maxSameTypeItems);
            mergeGeneratedItem(generatedItems, generated);
            generatedCount++;
        }

        sortGeneratedItems(generatedItems);
        return generatedItems;
    }

    /**
     * Native: ItemAssortmentGenerator::InitInverseDistanceTable @00525606 with OneOverAbs @00544600.
     * Fully ported.
     */
    public void initInverseDistanceTable() {
        inverseDistanceTable.clear();
        for (int cur = 0; cur < 0x400; cur++) {
            double value = (3.2d / 0x400) * cur - 1.6d;
            inverseDistanceTable.add(1.0d / Math.exp(value * value));
        }
        edgeValue = 1.0d / Math.exp(1.6d * 1.6d);
    }

    /**
     * Native: ItemAssortmentGenerator::ComputeIndexRangeForPriceThreshold @0052586A.
     * Fully ported.
     */
    public int[] computeIndexRangeForPriceThreshold(int price, double threshold) {
        int minIndex = 0;
        for (int index = 0; index < inverseDistanceTable.size() / 2; index++) {
            if (threshold < inverseDistanceTable.get(index)) {
                minIndex = index;
                break;
            }
        }

        int maxIndex = inverseDistanceTable.size() - 1;
        for (int index = inverseDistanceTable.size() / 2; index < inverseDistanceTable.size(); index++) {
            if (inverseDistanceTable.get(index) < threshold) {
                maxIndex = index;
                break;
            }
        }

        indexShift = inverseDistanceTable.size() / 2.0d - ((double) (price - minPrice) / priceStep);
        int truncatedShift = (int) indexShift;
        if (minIndex > 0) {
            minIndex -= truncatedShift;
        }
        if (maxIndex < inverseDistanceTable.size() - 1) {
            maxIndex -= truncatedShift;
        }

        minIndex = Math.max(minIndex, 0);
        maxIndex = Math.min(maxIndex, inverseDistanceTable.size() - 1);
        return new int[]{minIndex, maxIndex};
    }

    /**
     * Native support extracted from ItemAssortmentGenerator::GenerateItemsForTargetPrice @00525B41 TYPE_OTHER fast path.
     * Fully ported.
     */
    private void appendTypeOtherSeedItems(CustomList<Item> generatedItems) {
        for (Item candidate : candidates) {
            if (candidate.type != 5) {
                continue;
            }
            candidate.count = 2;
            generatedItems.add(candidate.splitOne());
        }
    }

    /**
     * Native support extracted from ItemAssortmentGenerator::GenerateItemsForTargetPrice @00525B41 candidate selection.
     * Fully ported.
     */
    private Item selectCandidateForTargetPrice(int minTableIndex, int maxTableIndex) {
        while (true) {
            int tableIndex = minTableIndex + Utils.randInclusive(maxTableIndex - minTableIndex);
            int candidateUpperBound = candidates.size() - 1;
            int candidateIndex = (int) (((double) candidateUpperBound / (double) inverseDistanceTable.size()) * tableIndex);
            Item candidate = candidates.get(candidateIndex);
            if (candidate.type != 5) {
                return candidate;
            }
        }
    }

    /**
     * Native support extracted from ItemAssortmentGenerator::GenerateItemsForTargetPrice @00525B41 and
     * ItemAssortmentGenerator::GenerateItems @00525F67.
     * Fully ported.
     */
    private static void finalizeGeneratedItemCount(Item generated, int maxSameTypeItems) {
        if (generated.canStackInInventory() == 0) {
            generated.count = 1;
            return;
        }
        generated.count = Utils.randBasedInclusive(1, maxSameTypeItems);
    }

    /**
     * Native: ItemAssortmentGenerator::appendEquipmentCandidates @00524BCA.
     * Fully ported.
     */
    private void appendEquipmentCandidates(List<? extends WorldItem> table, int mask) {
        boolean extraMagic = hasMaskFlag(mask, EXTRA_MAGIC);
        for (int shapeId : collectShapeIds(mask)) {
            for (int materialId : collectMaterialIds(mask)) {
                if (hasMaskFlag(mask, TYPE_WEAPON)) {
                    appendConcreteEquipmentCandidates(table, ITEM_KIND_WEAPON, shapeId, materialId, false);
                }
                if (hasMaskFlag(mask, TYPE_ARMOR)) {
                    appendConcreteEquipmentCandidates(table, ITEM_KIND_ARMOR, shapeId, materialId, extraMagic);
                }
                if (hasMaskFlag(mask, TYPE_SHIELD)) {
                    appendConcreteEquipmentCandidates(table, ITEM_KIND_SHIELD, shapeId, materialId, false);
                }
                if (hasMaskFlag(mask, TYPE_STAFF) && extraMagic) {
                    appendConcreteEquipmentCandidates(table, ITEM_KIND_STAFF, shapeId, materialId, true);
                }
            }
        }
    }

    /**
     * Native: ItemAssortmentGenerator::appendConcreteEquipmentCandidates @005247DB.
     * Fully ported.
     */
    private void appendConcreteEquipmentCandidates(
            List<? extends WorldItem> table,
            int itemKind,
            int shapeId,
            int materialId,
            boolean extraMagic) {
        for (int itemIndex = 1; itemIndex < table.size(); itemIndex++) {
            WorldItem itemInfo = table.get(itemIndex);
            if ((((itemInfo.materialMasks[shapeId] & 0xFFFF) >>> materialId) & 1) == 0) {
                continue;
            }
            if (!extraMagic && !isEquipmentBasePriceWithinRange(itemInfo, shapeId, materialId)) {
                continue;
            }
            if (itemKind == ITEM_KIND_ARMOR && isInvalidArmorCombination(shapeId, materialId, itemIndex)) {
                continue;
            }

            boolean fighterSuitable = isFighterSuitable(itemInfo);
            if (itemKind == ITEM_KIND_WEAPON && !fighterSuitable) {
                continue;
            }
            if (itemKind == ITEM_KIND_STAFF && fighterSuitable) {
                continue;
            }

            Item candidate = createEquipmentCandidate(itemKind, itemIndex, shapeId, materialId);
            if (extraMagic && candidate.magicVolume < Math.pow(minPrice, 0.4d)) {
                continue;
            }
            candidates.add(candidate);
        }
    }

    /**
     * Native: ItemAssortmentGenerator::appendOtherCandidates @00524E31.
     * Fully ported.
     */
    private void appendOtherCandidates() {
        for (int spellId = 1; spellId <= 29; spellId++) {
            if (!isBookSpellAllowed(spellId)) {
                continue;
            }
            MagicItemId bookId = bookItemForSpell(spellId);
            Item book = createTeachSpellBookCandidate(bookId, spellId);
            if (isWithinPriceRange(book.price)) {
                candidates.add(book);
            }
        }

        for (int spellId = 1; spellId <= 29; spellId++) {
            int scrollItemOffset = Utils.randInclusive(1, 2) == 1 ? 5 : 0x22;
            Item scroll = createCastSpellScrollCandidate(spellId + scrollItemOffset);
            if (isWithinPriceRange(scroll.price)) {
                candidates.add(scroll);
            }
        }
    }

    /**
     * Native support extracted from ItemAssortmentGenerator::GenerateItems @00525F67 TYPE_OTHER fast path.
     * Fully ported.
     */
    private void appendBonusOtherItems(CustomList<Item> generatedItems) {
        for (Item candidate : candidates) {
            if (candidate.type != 5) {
                continue;
            }
            candidate.count = 2;
            generatedItems.add(candidate.splitOne());
        }

        for (MagicItemId potionId : BONUS_POTIONS) {
            Item potion = MagicItem.createById(potionId);
            potion.count = Utils.randInclusive(0x33, 0x64);
            generatedItems.add(potion);
        }
    }

    /**
     * Native support extracted from ItemAssortmentGenerator::applyRandomMagic @00526B5D and
     * createRandomMagicEffect @00526EC4. Native reaches magic-item rows through unchecked WorldItem-column overread,
     * then rejects zero-capacity rows when creating the random effect; Java performs the zero-capacity rejection before
     * the non-representable overread.
     */
    private static boolean hasRandomMagicCapacity(Item item) {
        if (item.magicVolume < 1) {
            return false;
        }
        return true;
    }

    /**
     * Native: ItemAssortmentGenerator::applyRandomMagic @00526B5D.
     * Java preserves native selected-row flow by letting generator callers invoke this for magic-item rows. Zero
     * MagicVolume rows are rejected here before Java reaches native's unchecked m_aValues[0xf] overread; native then
     * rejects them when createRandomMagicEffect @00526EC4 sees magicCapacity < 1.
     */
    private boolean applyRandomMagic(Item item) {
        if (!hasRandomMagicCapacity(item)) {
            return false;
        }
        boolean fighterSuitable = isFighterSuitable(item.worldItem);
        int slot = item.getSlot() & 0xFF;
        int priceBudget = maxPrice * 2 - item.price;

        if (!fighterSuitable && item.type == 2) {
            Effect castSpell = createRandomCastSpellEffect(false, item.magicVolume, Math.min(priceBudget, item.price * 100), 100);
            if (castSpell == null) {
                return false;
            }
            item.addOrReplaceEffect(castSpell);
            item.recalculatePrice();
            return true;
        }

        Effect firstEffect = createRandomMagicEffect(
                fighterSuitable,
                item.type,
                slot,
                item.magicVolume,
                priceBudget,
                item.price,
                100
        );
        if (firstEffect == null) {
            return false;
        }
        item.addOrReplaceEffect(firstEffect);
        item.recalculatePrice();

        if (!firstEffect.isA(EffectId.CAST_SPELL)) {
            if (Utils.randPercent0To99() < 0x32) {
                if (tryAddExtraRandomMagic(item, fighterSuitable, slot)) {
                    return true;
                }
            }
            if (Utils.randPercent0To99() < 0x19) {
                if (tryAddExtraRandomMagic(item, fighterSuitable, slot)) {
                    return true;
                }
            }
            item.magicVolume -= item.calculateEffectScore();
        }
        return true;
    }

    /**
     * Native support extracted from ItemAssortmentGenerator::applyRandomMagic @00526B5D second/third effect path.
     * Fully ported.
     */
    private boolean tryAddExtraRandomMagic(Item item, boolean fighterSuitable, int slot) {
        int remainingCapacity = item.magicVolume - item.calculateEffectScore();
        int priceBudget = maxPrice * 2 - item.price;
        Effect extraEffect = createRandomMagicEffect(
                fighterSuitable,
                item.type,
                slot,
                remainingCapacity,
                priceBudget,
                item.price,
                100
        );
        if (extraEffect == null) {
            return true;
        }
        if (extraEffect.isA(EffectId.CAST_SPELL)) {
            return true;
        }
        item.addOrReplaceEffect(extraEffect);
        item.recalculatePrice();
        return false;
    }

    /**
     * Native: ItemAssortmentGenerator::createRandomMagicEffect @00526EC4.
     * Fully ported.
     */
    private Effect createRandomMagicEffect(
            boolean fighterSuitable,
            int itemType,
            int slot,
            int magicCapacity,
            int priceBudget,
            int currentItemPrice,
            int retryLimit) {
        if (priceBudget < 1 || magicCapacity < 1) {
            return null;
        }

        Effect effect = new Effect();
        int effectId = -1;
        int retryCount = 0;
        while (retryCount < retryLimit) {
            retryCount++;
            effectId = selectWeightedEffectId(fighterSuitable, slot);
            if (effectId != -1) {
                break;
            }
        }
        if (retryCount >= retryLimit) {
            return null;
        }

        if (!fighterSuitable && itemType == 2) {
            effectId = EffectId.CAST_SPELL.id;
        }
        effect.id = effectId;
        effect.type = PERMANENT;
        effect.mValue.setFull(0);
        int effectPriceBudget = priceBudget;
        if (effect.isA(EffectId.CAST_SPELL)) {
            SpellId[] spellPool = fighterSuitable ? FIGHTER_CAST_SPELLS : STAFF_CAST_SPELLS;
            effect.mValue.setS1(spellPool[Utils.randInclusive(spellPool.length - 1)].id);
            effectPriceBudget = currentItemPrice * (fighterSuitable ? 10 : 100);
        }
        int maxValue = effect.computeValueFromPrice(effectPriceBudget, magicCapacity);
        if (maxValue < 0) {
            return null;
        }
        effect.randomizeValue(maxValue);
        return effect;
    }

    /**
     * Native: ItemAssortmentGenerator::createRandomCastSpellEffect @0052714F.
     * Fully ported.
     */
    private static Effect createRandomCastSpellEffect(
            boolean fighterSuitable,
            int magicCapacity,
            int priceBudget,
            int retryLimit
    ) {
        Effect effect = new Effect();
        effect.id = EffectId.CAST_SPELL.id;
        effect.type = PERMANENT;
        int maxValue = -1;
        int retryCount = 0;
        while (retryCount < retryLimit && maxValue < 0) {
            SpellId[] spellPool = fighterSuitable ? FIGHTER_CAST_SPELLS : STAFF_CAST_SPELLS;
            effect.mValue.setFull(0);
            effect.mValue.setS1(spellPool[Utils.randInclusive(spellPool.length - 1)].id);
            maxValue = effect.computeValueFromPrice(priceBudget, magicCapacity);
            retryCount++;
        }
        if (retryCount >= retryLimit) {
            return null;
        }
        effect.randomizeValue(maxValue);
        return effect;
    }

    /**
     * Native support extracted from ItemAssortmentGenerator::createRandomMagicEffect @00526EC4 weighted effect selection.
     * Native uses cumulative magic-table columns and ignores unnamed rows.
     * Fully ported.
     */
    private int selectWeightedEffectId(boolean fighterSuitable, int slot) {
        int baseColumnIndex = fighterSuitable ? MagicColumn.IN_WEAPON_A.index : MagicColumn.IN_WEAPON_B.index;
        int columnIndex = baseColumnIndex + slot - 1;
        int lastEffectIndex = Globals.staticDataMgr.magic.size() - 1;
        int totalWeight = Globals.staticDataMgr.magic.get(lastEffectIndex).values.get(columnIndex);

        int roll = Utils.randInclusive(1, totalWeight);
        for (int effectId = 1; effectId <= lastEffectIndex; effectId++) {
            MagicInfo effectInfo = Globals.staticDataMgr.magic.get(effectId);
            if (effectInfo.name.isEmpty()) {
                continue;
            }

            int previousWeight = Globals.staticDataMgr.magic.get(effectId - 1).values.get(columnIndex);
            int currentWeight = effectInfo.values.get(columnIndex);
            if (previousWeight < roll && roll <= currentWeight) {
                return effectId;
            }
        }
        return -1;
    }

    /**
     * Native: CArray<Item>::mergeGeneratedItem @005259AF.
     * Fully ported.
     */
    private void mergeGeneratedItem(CustomList<Item> generatedItems, Item candidate) {
        for (Item existing : generatedItems) {
            if (candidate.canStackInInventory() != 0 && existing.matchesInventoryIdentity(candidate)) {
                existing.count += candidate.count;
                return;
            }
        }
        generatedItems.add(candidate);
    }

    /**
     * Native: CompareGeneratedItems @0052571C.
     * Fully ported.
     */
    private static int compareGeneratedItems(Item left, Item right) {
        if (left == right) {
            return 0;
        }

        int suitableCompare = Boolean.compare(
                isGeneratedItemFighterSuitable(right),
                isGeneratedItemFighterSuitable(left));
        if (suitableCompare != 0) {
            return suitableCompare;
        }

        int keyCompare = Integer.compare((left.key & 0xFFFF), (right.key & 0xFFFF));
        if (keyCompare != 0) {
            return keyCompare;
        }
        return Integer.compare(left.price, right.price);
    }

    /**
     * Native: SortGeneratedItems @00525844.
     * Fully ported using Java sort with the native comparator.
     */
    private static void sortGeneratedItems(CustomList<Item> generatedItems) {
        generatedItems.sort(ItemAssortmentGenerator::compareGeneratedItems);
    }

    /**
     * Native support extracted from ItemAssortmentGenerator::appendOtherCandidates @00524E31.
     * Fully ported.
     */
    private static MagicItemId bookItemForSpell(int spellId) {
        if (spellId >= 1 && spellId <= 4) {
            return MagicItemId.BOOK_FIRE;
        }
        if (spellId >= 5 && spellId <= 8) {
            return MagicItemId.BOOK_WATER;
        }
        if (spellId >= 10 && spellId <= 13) {
            return MagicItemId.BOOK_AIR;
        }
        if (spellId >= 16 && spellId <= 19) {
            return MagicItemId.BOOK_EARTH;
        }
        return MagicItemId.BOOK_ASTRAL;
    }

    /**
     * Native support extracted from ItemAssortmentGenerator::appendOtherCandidates @00524E31.
     * Fully ported.
     */
    private static boolean isBookSpellAllowed(int spellId) {
        return spellId != 9
                && spellId != 14
                && spellId != 15
                && spellId != 24
                && spellId != 28
                && spellId != 29;
    }

    /**
     * Native support extracted from ItemAssortmentGenerator::appendConcreteEquipmentCandidates @005247DB.
     * Fully ported.
     */
    private boolean isEquipmentBasePriceWithinRange(WorldItem itemInfo, int shapeId, int materialId) {
        Material shape = Globals.staticDataMgr.shapes.get(shapeId);
        Material material = Globals.staticDataMgr.materials.get(materialId);
        int basePrice = (int) (itemInfo.getAttribute(WorldItemColumn.PRICE)
                * material.attributes.price
                * shape.attributes.price);
        return basePrice >= minPrice && basePrice <= maxPrice;
    }

    /**
     * Native support extracted from ItemAssortmentGenerator::appendConcreteEquipmentCandidates @005247DB.
     * Fully ported.
     */
    private static boolean isInvalidArmorCombination(int shapeId, int materialId, int itemIndex) {
        return (shapeId == 0 && materialId == 0 && itemIndex == 2)
                || (shapeId == 0 && materialId == 1 && itemIndex == 2)
                || (shapeId == 6 && materialId == 4 && itemIndex == 6);
    }

    /**
     * Native support extracted from ItemAssortmentGenerator::appendOtherCandidates @00524E31 book path.
     * Fully ported.
     */
    private static Item createTeachSpellBookCandidate(MagicItemId magicItemId, int spellId) {
        Item item = MagicItem.createById(magicItemId);
        item.price = Globals.staticDataMgr.spells.get(spellId).getAttribute(SpellColumn.BOOK_COST);
        Effect teachSpell = new Effect();
        teachSpell.id = EffectId.TEACH_SPELL.id;
        teachSpell.type = PERMANENT;
        teachSpell.mValue.setS1(spellId);
        item.effects.add(teachSpell);
        return item;
    }

    /**
     * Native support extracted from ItemAssortmentGenerator::appendOtherCandidates @00524E31 scroll path.
     * Fully ported.
     */
    private static Item createCastSpellScrollCandidate(int magicItemId) {
        Item item = MagicItem.createById(MagicItemId.fromId(magicItemId));
        item.recalculatePrice();
        return item;
    }

    /**
     * Native support extracted from ItemAssortmentGenerator::appendConcreteEquipmentCandidates @005247DB.
     * Fully ported.
     */
    private static Item createEquipmentCandidate(int itemKind, int itemIndex, int shapeId, int materialId) {
        return switch (itemKind) {
            case ITEM_KIND_ARMOR -> Armor.createByTemplateIds(itemIndex, shapeId, materialId);
            case ITEM_KIND_WEAPON, ITEM_KIND_STAFF -> Weapon.createByTemplateIds(itemIndex, shapeId, materialId);
            case ITEM_KIND_SHIELD -> Shield.createByTemplateIds(itemIndex, shapeId, materialId);
            default -> throw new IllegalArgumentException("Unsupported item kind: " + itemKind);
        };
    }

    /**
     * Native support extracted from ItemAssortmentGenerator::appendEquipmentCandidates @00524BCA.
     * Fully ported.
     */
    private static int[] collectShapeIds(int mask) {
        CustomList<Integer> shapeIds = CustomList.std(Integer.class);
        if (hasMaskFlag(mask, RARITY_COMMON)) {
            shapeIds.add(0);
        }
        if (hasMaskFlag(mask, RARITY_UNCOMMON)) {
            shapeIds.add(1);
        }
        if (hasMaskFlag(mask, RARITY_RARE)) {
            shapeIds.add(2);
        }
        if (hasMaskFlag(mask, RARITY_VERY_RARE)) {
            shapeIds.add(3);
        }
        if (hasMaskFlag(mask, RARITY_ELVEN)) {
            shapeIds.add(4);
        }
        if (hasMaskFlag(mask, RARITY_BAD)) {
            shapeIds.add(5);
        }
        if (hasMaskFlag(mask, RARITY_GOOD)) {
            shapeIds.add(6);
        }
        return toIntArray(shapeIds);
    }

    /**
     * Native support extracted from ItemAssortmentGenerator::appendEquipmentCandidates @00524BCA.
     * Fully ported.
     */
    private static int[] collectMaterialIds(int mask) {
        CustomList<Integer> materialIds = CustomList.std(Integer.class);
        if (hasMaskFlag(mask, MATERIAL_IRON)) {
            materialIds.add(0);
        }
        if (hasMaskFlag(mask, MATERIAL_BRONZE)) {
            materialIds.add(1);
        }
        if (hasMaskFlag(mask, MATERIAL_STEEL)) {
            materialIds.add(2);
        }
        if (hasMaskFlag(mask, MATERIAL_SILVER)) {
            materialIds.add(3);
        }
        if (hasMaskFlag(mask, MATERIAL_GOLD)) {
            materialIds.add(4);
        }
        if (hasMaskFlag(mask, MATERIAL_MITHRILL)) {
            materialIds.add(5);
        }
        if (hasMaskFlag(mask, MATERIAL_ADAMANTIUM)) {
            materialIds.add(6);
        }
        if (hasMaskFlag(mask, MATERIAL_METEORIC)) {
            materialIds.add(7);
        }
        if (hasMaskFlag(mask, MATERIAL_WOOD)) {
            materialIds.add(8);
        }
        if (hasMaskFlag(mask, MATERIAL_MAGIC_WOOD)) {
            materialIds.add(9);
        }
        if (hasMaskFlag(mask, MATERIAL_LEATHER)) {
            materialIds.add(10);
        }
        if (hasMaskFlag(mask, MATERIAL_HARD_LEATHER)) {
            materialIds.add(11);
        }
        if (hasMaskFlag(mask, MATERIAL_DRAGON_LEATHER)) {
            materialIds.add(12);
        }
        if (hasMaskFlag(mask, MATERIAL_CRYSTAL)) {
            materialIds.add(14);
        }
        if (hasMaskFlag(mask, MATERIAL_NONE)) {
            materialIds.add(15);
        }
        return toIntArray(materialIds);
    }

    /**
     * Native helper used by ItemAssortmentGenerator::FUN_005254E7.
     * not ported.
     */
    private boolean hasFlag(ShelfFlagValues flag) {
        return (selectionMask & flag.value) != 0;
    }

    /**
     * Native support extracted from ItemAssortmentGenerator::appendEquipmentCandidates @00524BCA mask checks.
     * Fully ported.
     */
    private static boolean hasMaskFlag(int mask, ShelfFlagValues flag) {
        return (mask & flag.value) != 0;
    }

    /**
     * Native helper used by ItemAssortmentGenerator selection-mask checks.
     * not ported.
     */
    private boolean hasOnlyFlag(ShelfFlagValues flag) {
        return hasFlag(flag) && hasNoFlagsExcept(flag);
    }

    /**
     * Native helper used by ItemAssortmentGenerator selection-mask checks.
     * not ported.
     */
    private boolean hasNoFlagsExcept(ShelfFlagValues flag) {
        return (selectionMask & ~flag.value) == 0;
    }

    /**
     * Native helper used by ItemAssortmentGenerator::FUN_005254E7.
     * not ported.
     */
    private boolean isWithinPriceRange(int price) {
        return price >= minPrice && price <= maxPrice;
    }

    /**
     * Native support extracted from ItemAssortmentGenerator::GenerateItems @00525F67 candidate selection loop.
     * Fully ported.
     */
    private Item selectCandidateForGeneration() {
        if (!hasFlag(TYPE_OTHER)) {
            return candidates.get(Utils.randInclusive(candidates.size() - 1));
        }

        for (int attempts = 1; attempts < 1000; attempts++) {
            Item candidate = candidates.get(Utils.randInclusive(candidates.size() - 1));
            if (candidate.type != 5) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Native support extracted from ItemAssortmentGenerator::appendConcreteEquipmentCandidates @005247DB.
     * Fully ported.
     */
    private static boolean isFighterSuitable(WorldItem itemInfo) {
        return (itemInfo.getAttribute(WorldItemColumn.SUITABLE_FOR) & 1) != 0;
    }

    /**
     * Native helper used by CompareGeneratedItems @0052571C.
     * Fully ported.
     */
    private static boolean isGeneratedItemFighterSuitable(Item item) {
        return (item.type == ITEM_KIND_WEAPON || item.type == ITEM_KIND_ARMOR || item.type == ITEM_KIND_SHIELD)
                && isFighterSuitable(item.worldItem);
    }

    /**
     * Native support extracted from CreateRandomItemForPriceRange @00524070 and randomFloat0to1Inclusive @0051FA0C.
     * Fully ported.
     */
    private static double randomUnitFloat() {
        return Utils.randomFloat0to1Inclusive();
    }

    /**
     * Native support extracted from CreateRandomItemOfKindForPriceRange @00523ABD.
     * Fully ported.
     */
    private static Item createRandomWeaponForPriceRange(int minPrice, int maxPrice) {
        Item candidate;
        int attempts = 0;
        do {
            int[] variant;
            do {
                variant = selectRandomWorldItemVariant(Globals.staticDataMgr.weapons, RANDOM_WEAPON_MIN_INDEX);
            } while (isExcludedRandomWeaponIndex(variant[0]));
            candidate = Weapon.createByTemplateIds(variant[0], variant[1], variant[2]);
            attempts++;
            if (attempts > RANDOM_ITEM_RETRY_LIMIT) {
                return candidate;
            }
        } while (candidate.price < minPrice || maxPrice < candidate.price);
        return candidate;
    }

    /**
     * Native support extracted from CreateRandomItemOfKindForPriceRange @00523ABD.
     * Fully ported.
     */
    private static Item createRandomArmorForPriceRange(int minPrice, int maxPrice) {
        Item candidate;
        int attempts = 0;
        do {
            int[] variant = selectRandomWorldItemVariant(Globals.staticDataMgr.armors, RANDOM_ARMOR_MIN_INDEX);
            candidate = Armor.createByTemplateIds(variant[0], variant[1], variant[2]);
            attempts++;
            if (attempts > RANDOM_ITEM_RETRY_LIMIT) {
                return candidate;
            }
        } while (candidate.price < minPrice || maxPrice < candidate.price);
        return candidate;
    }

    /**
     * Native support extracted from CreateRandomItemOfKindForPriceRange @00523ABD.
     * Fully ported.
     */
    private static Item createRandomShieldForPriceRange(int minPrice, int maxPrice) {
        Item candidate;
        int attempts = 0;
        do {
            int[] variant = selectRandomWorldItemVariant(Globals.staticDataMgr.shields, RANDOM_SHIELD_MIN_INDEX);
            candidate = Shield.createByTemplateIds(variant[0], variant[1], variant[2]);
            attempts++;
            if (attempts > RANDOM_ITEM_RETRY_LIMIT) {
                return candidate;
            }
        } while (candidate.price < minPrice || maxPrice < candidate.price);
        return candidate;
    }

    /**
     * Native support extracted from CreateRandomItemOfKindForPriceRange @00523ABD.
     * Fully ported.
     */
    private static Item createRandomPotionForPriceRange(int minPrice, int maxPrice) {
        Item candidate;
        int attempts = 0;
        do {
            candidate = MagicItem.createByIndex(Utils.randBasedInclusive(1, 0x0C));
            attempts++;
            if (attempts > RANDOM_POTION_RETRY_LIMIT) {
                return candidate;
            }
        } while (candidate.price < minPrice || maxPrice < candidate.price);
        return candidate;
    }

    /**
     * Native support extracted from CreateRandomItemOfKindForPriceRange @00523ABD.
     * Fully ported.
     */
    private static boolean isExcludedRandomWeaponIndex(int itemIndex) {
        for (int excludedIndex : RANDOM_ITEM_WEAPON_EXCLUDED_INDICES) {
            if (itemIndex == excludedIndex) {
                return true;
            }
        }
        return false;
    }

    /**
     * Native: SelectRandomWorldItemVariant @00523F8D.
     * Fully ported.
     */
    private static int[] selectRandomWorldItemVariant(List<? extends WorldItem> items, int minimumItemIndex) {
        int itemIndex;
        WorldItem itemInfo;
        boolean hasAllowedShape;
        do {
            itemIndex = Utils.randExclusive(minimumItemIndex, items.size());
            itemInfo = items.get(itemIndex);
            hasAllowedShape = false;
            for (int shapeId = 0; shapeId < itemInfo.materialMasks.length; shapeId++) {
                if ((itemInfo.materialMasks[shapeId] & 0xFFFF) != 0) {
                    hasAllowedShape = true;
                }
            }
        } while (!hasAllowedShape);

        int shapeId;
        do {
            shapeId = Utils.randInclusive(itemInfo.materialMasks.length - 1);
        } while ((itemInfo.materialMasks[shapeId] & 0xFFFF) == 0);

        int materialId;
        do {
            materialId = Utils.randInclusive(0x0F);
        } while (((itemInfo.materialMasks[shapeId] & 0xFFFF) & (1 << materialId)) == 0);

        return new int[]{itemIndex, shapeId, materialId};
    }

    /**
     * Native support extracted from ItemAssortmentGenerator::appendEquipmentCandidates @00524BCA.
     * Fully ported.
     */
    private static int[] toIntArray(CustomList<Integer> values) {
        int[] out = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            out[i] = values.get(i);
        }
        return out;
    }

}
