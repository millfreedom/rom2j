package ua.millfreedom.rom2.model.world;

import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.ShelfFlags;
import ua.millfreedom.rom2.model.ShopAssortmentEntry;

import java.util.List;

import static ua.millfreedom.rom2.model.ShelfFlagValues.*;

/**
 * Java port of the static Scenario.dll {@code ScenarioLocation} records.
 * Scenario.dll constructs 52 global records through ScenarioLocation::ScenarioLocation @100038e0, but only the 49
 * entries in ALL_LOCATIONS are inserted into g_allLocations by FUN_10001a0f.
 */
public final class ScenarioLocation {
    public static final ScenarioLocation TOWN_1 = createStaticLocation(2, 1, 215, 234, 0, 0);
    public static final ScenarioLocation TOWN_2 = createStaticLocation(2, 2, 370, 305, 0, 0);
    public static final ScenarioLocation TOWN_3 = createStaticLocation(2, 3, 194, 225, 0, 0);
    public static final ScenarioLocation MISSION_10 = createStaticLocation(1, 10, 302, 158, 0, 0);
    public static final ScenarioLocation MISSION_20 = createStaticLocation(1, 20, 380, 364, 0, 0);
    public static final ScenarioLocation MISSION_30 = createStaticLocation(1, 30, 331, 395, 0, 0);
    public static final ScenarioLocation MISSION_40 = createStaticLocation(1, 40, 339, 226, 0, 0);
    public static final ScenarioLocation MISSION_50 = createStaticLocation(1, 50, 210, 260, 0, 0);
    public static final ScenarioLocation MISSION_60 = createStaticLocation(1, 60, 511, 301, 0, 0);
    public static final ScenarioLocation MISSION_70 = createStaticLocation(1, 70, 86, 193, 0, 0);
    public static final ScenarioLocation MISSION_80 = createStaticLocation(1, 80, 554, 299, 0, 0);
    public static final ScenarioLocation MISSION_90 = createStaticLocation(1, 90, 283, 244, 0, 0);
    public static final ScenarioLocation MISSION_100 = createStaticLocation(1, 100, 388, 140, 0, 0);
    public static final ScenarioLocation MISSION_110 = createStaticLocation(1, 110, 448, 140, 0, 0);
    public static final ScenarioLocation MISSION_21 = createStaticLocation(1, 21, 423, 367, 0, 0);
    public static final ScenarioLocation MISSION_31 = createStaticLocation(1, 31, 443, 225, 0, 0);
    public static final ScenarioLocation MISSION_32 = createStaticLocation(1, 32, 457, 202, 0, 0);
    public static final ScenarioLocation MISSION_41 = createStaticLocation(1, 41, 403, 228, 0, 0);
    public static final ScenarioLocation MISSION_42 = createStaticLocation(1, 42, 531, 207, 0, 0);
    public static final ScenarioLocation MISSION_43 = createStaticLocation(1, 43, 454, 358, 0, 0);
    public static final ScenarioLocation MISSION_51 = createStaticLocation(1, 51, 450, 240, 0, 0);
    public static final ScenarioLocation MISSION_52 = createStaticLocation(1, 52, 132, 303, 0, 0);
    public static final ScenarioLocation MISSION_53 = createStaticLocation(1, 53, 292, 191, 0, 0);
    public static final ScenarioLocation MISSION_61 = createStaticLocation(1, 61, 475, 312, 0, 0);
    public static final ScenarioLocation MISSION_62 = createStaticLocation(1, 62, 253, 169, 0, 0);
    public static final ScenarioLocation MISSION_63 = createStaticLocation(1, 63, 385, 221, 0, 0);
    public static final ScenarioLocation MISSION_71 = createStaticLocation(1, 71, 337, 306, 0, 0);
    public static final ScenarioLocation MISSION_72 = createStaticLocation(1, 72, 530, 255, 0, 0);
    public static final ScenarioLocation MISSION_73 = createStaticLocation(1, 73, 343, 347, 0, 0);
    public static final ScenarioLocation MISSION_74 = createStaticLocation(1, 74, 595, 313, 0, 0);
    public static final ScenarioLocation MISSION_75 = createStaticLocation(1, 75, 356, 359, 0, 0);
    public static final ScenarioLocation MISSION_76 = createStaticLocation(1, 76, 111, 227, 0, 0);
    public static final ScenarioLocation MISSION_77 = createStaticLocation(1, 77, 406, 117, 0, 0);
    public static final ScenarioLocation MISSION_81 = createStaticLocation(1, 81, 510, 182, 0, 0);
    public static final ScenarioLocation MISSION_82 = createStaticLocation(1, 82, 424, 186, 0, 0);
    public static final ScenarioLocation MISSION_83 = createStaticLocation(1, 83, 521, 372, 0, 0);
    public static final ScenarioLocation MISSION_84 = createStaticLocation(1, 84, 552, 377, 0, 0);
    public static final ScenarioLocation MISSION_85 = createStaticLocation(1, 85, 393, 360, 0, 0);
    public static final ScenarioLocation MISSION_86 = createStaticLocation(1, 86, 67, 213, 0, 0);
    public static final ScenarioLocation MISSION_87 = createStaticLocation(1, 87, 509, 161, 0, 0);
    public static final ScenarioLocation MISSION_91 = createStaticLocation(1, 91, 477, 269, 0, 0);
    public static final ScenarioLocation MISSION_92 = createStaticLocation(1, 92, 93, 269, 0, 0);
    public static final ScenarioLocation MISSION_93 = createStaticLocation(1, 93, 306, 148, 0, 0);
    public static final ScenarioLocation MISSION_94 = createStaticLocation(1, 94, 306, 148, 0, 0);
    public static final ScenarioLocation MISSION_95 = createStaticLocation(1, 95, 306, 148, 0, 0);
    public static final ScenarioLocation MISSION_96 = createStaticLocation(1, 96, 306, 148, 0, 0);
    public static final ScenarioLocation MISSION_101 = createStaticLocation(1, 101, 273, 300, 0, 0);
    public static final ScenarioLocation MISSION_102 = createStaticLocation(1, 102, 351, 190, 0, 0);
    public static final ScenarioLocation MISSION_103 = createStaticLocation(1, 103, 575, 362, 0, 0);

    public static final ScenarioLocation MISSION_10_UNUSED_VARIANT = createStaticLocation(1, 10, 380, 364, 0, 0);
    public static final ScenarioLocation MISSION_20_UNUSED_VARIANT = createStaticLocation(1, 20, 210, 260, 0, 0);
    public static final ScenarioLocation MISSION_30_UNUSED_VARIANT = createStaticLocation(1, 30, 511, 301, 0, 0);

    public static final List<ScenarioLocation> ALL_LOCATIONS = List.of(
            TOWN_1,
            TOWN_2,
            TOWN_3,
            MISSION_10,
            MISSION_20,
            MISSION_21,
            MISSION_30,
            MISSION_31,
            MISSION_32,
            MISSION_40,
            MISSION_41,
            MISSION_42,
            MISSION_43,
            MISSION_50,
            MISSION_51,
            MISSION_52,
            MISSION_53,
            MISSION_60,
            MISSION_61,
            MISSION_62,
            MISSION_63,
            MISSION_70,
            MISSION_71,
            MISSION_72,
            MISSION_73,
            MISSION_74,
            MISSION_75,
            MISSION_76,
            MISSION_77,
            MISSION_80,
            MISSION_81,
            MISSION_82,
            MISSION_83,
            MISSION_84,
            MISSION_85,
            MISSION_86,
            MISSION_87,
            MISSION_90,
            MISSION_91,
            MISSION_92,
            MISSION_93,
            MISSION_94,
            MISSION_95,
            MISSION_96,
            MISSION_100,
            MISSION_101,
            MISSION_102,
            MISSION_103,
            MISSION_110
    );

    /**
     * Constructor-backed globals recovered from ScenarioLocation::ScenarioLocation @100038e0 that are not added to
     * g_allLocations by FUN_10001a0f.
     */
    public static final List<ScenarioLocation> UNUSED_VARIANTS = List.of(
            MISSION_10_UNUSED_VARIANT,
            MISSION_20_UNUSED_VARIANT,
            MISSION_30_UNUSED_VARIANT
    );

    //0x0 Native ScenarioLocation::GetKind @00473A20 returns this field; Java uses direct field access.
    public int kind;
    //0x4 Native ScenarioLocation::GetId @00473A30 returns this field; Java uses direct field access.
    public int id;
    //0x8 Native ScenarioLocation::GetRect @00473A50 returns this field; Java uses direct field access.
    public final CRect rect = new CRect();

    /**
     * Support constructor for Scenario.dll save/load helpers, not ported.
     */
    public ScenarioLocation() {
    }

    /**
     * Native: ScenarioLocation::ScenarioLocation @100038e0.
     */
    public ScenarioLocation(int kind, int id, int left, int top, int width, int height) {
        setLocation(kind, id, left, top, width, height);
    }

    /**
     * Native: ScenarioLocation::SetLocation @10003920.
     */
    public void setLocation(int kind, int id, int left, int top, int width, int height) {
        this.kind = kind;
        this.id = id;
        rect.set(left, top, left + width, top + height);
    }

    /**
     * Native: ScenarioLocation::ClearCurrentAvailableLocation @10001d5c.
     * Partial port. Native ignores the passed in/out pointer, removes town 1 from the available-location list when it
     * is the current town, clears the current location, and returns 0.
     */
    static int clearCurrentAvailableLocation(ScenarioLib scenarioLib, int[] inOutResult, int id) {
        if (id == 1) {
            scenarioLib.availableLocations.remove(scenarioLib.currentLocation);
        }
        scenarioLib.currentLocation = null;
        return 0;
    }

    /**
     * Native: ScenarioLocation::OnCompleted @10001da5.
     * Partial port. Native writes the transition code through `inOutResult[0]`, rebuilds the late-game available
     * locations, promotes location-entry state flags, clears the current location, marks the location complete, applies
     * scenario progression mutations, and returns 0.
     */
    static int onCompleted(ScenarioLib scenarioLib, int[] inOutResult, int id) {
        ScenarioState state = scenarioLib.state;
        ShopAssortmentEntry[][] shopAssortmentBlocks = scenarioLib.shopAssortmentBlocks;

        state.locationTransitionState = 0;
        if (state.rebuildAvailableLocationsPending != 0) {
            state.rebuildAvailableLocationsPending = 0;
            scenarioLib.rebuildLateScenarioAvailableLocations();
        }
        for (int entryIndex = 0; entryIndex < ScenarioLib.LOCATION_ENTRY_COUNT; entryIndex++) {
            int entryState = state.locationEntryStates[entryIndex];
            if (entryState != 0) {
                state.locationEntryStates[entryIndex] = state.locationEntrySelectionFlags[entryIndex] == 0 ? 1 : 2;
            }
            state.locationEntrySelectionFlags[entryIndex] = 0;
        }

        scenarioLib.availableLocations.remove(scenarioLib.currentLocation);
        scenarioLib.currentLocation = null;
        scenarioLib.setCompletedLocationFlag(id, 1);
        switch (id) {
            case 10 -> {
                scenarioLib.unlockAvailableLocation(1, 20);
                inOutResult[0] = 1;
            }
            case 0x14 -> {
                scenarioLib.unlockAvailableLocation(2, 2);
                if (state.followupLocationUnlockFlag != 0) {
                    scenarioLib.unlockAvailableLocation(1, 21);
                }
                state.locationEntryStates[0] = 1;
                state.locationEntryLocationIds[0] = 2;
            }
            case 0x1e -> {
                shopAssortmentBlocks[2][0].selectionMask = ShelfFlags.toValue(
                        MATERIAL_STEEL,
                        MATERIAL_SILVER,
                        MATERIAL_MITHRILL,
                        MATERIAL_MAGIC_WOOD,
                        MATERIAL_NONE,
                        RARITY_COMMON,
                        RARITY_UNCOMMON,
                        RARITY_RARE,
                        TYPE_SHIELD,
                        TYPE_ARMOR,
                        TYPE_MAGIC,
                        EXTRA_COMMON
                );
                shopAssortmentBlocks[2][1].selectionMask = ShelfFlags.toValue(
                        MATERIAL_STEEL,
                        MATERIAL_MITHRILL,
                        MATERIAL_WOOD,
                        MATERIAL_MAGIC_WOOD,
                        MATERIAL_NONE,
                        RARITY_COMMON,
                        RARITY_UNCOMMON,
                        RARITY_RARE,
                        TYPE_WEAPON,
                        TYPE_SHIELD,
                        TYPE_ARMOR,
                        TYPE_MAGIC,
                        TYPE_STAFF,
                        EXTRA_MAGIC
                );
                shopAssortmentBlocks[2][3].selectionMask = ShelfFlags.toValue(
                        MATERIAL_STEEL,
                        MATERIAL_MITHRILL,
                        MATERIAL_WOOD,
                        MATERIAL_MAGIC_WOOD,
                        RARITY_COMMON,
                        RARITY_UNCOMMON,
                        RARITY_RARE,
                        TYPE_WEAPON,
                        EXTRA_COMMON
                );
                inOutResult[0] = 2;
            }
            case 0x1f -> scenarioLib.unlockAvailableLocation(1, 0x20);
            case 0x28 -> {
                scenarioLib.unlockAvailableLocation(1, 0x32);
                scenarioLib.unlockAvailableLocation(1, 0x3c);
                state.locationTransitionState = 0x17;
                shopAssortmentBlocks[2][0].selectionMask = ShelfFlags.toValue(
                        MATERIAL_STEEL,
                        MATERIAL_SILVER,
                        MATERIAL_MITHRILL,
                        MATERIAL_ADAMANTIUM,
                        MATERIAL_MAGIC_WOOD,
                        MATERIAL_NONE,
                        RARITY_COMMON,
                        RARITY_UNCOMMON,
                        RARITY_RARE,
                        RARITY_VERY_RARE,
                        TYPE_SHIELD,
                        TYPE_ARMOR,
                        TYPE_MAGIC,
                        EXTRA_COMMON
                );
                shopAssortmentBlocks[2][1].selectionMask = ShelfFlags.toValue(
                        MATERIAL_STEEL,
                        MATERIAL_MITHRILL,
                        MATERIAL_WOOD,
                        MATERIAL_MAGIC_WOOD,
                        MATERIAL_NONE,
                        RARITY_COMMON,
                        RARITY_UNCOMMON,
                        RARITY_RARE,
                        RARITY_VERY_RARE,
                        TYPE_WEAPON,
                        TYPE_SHIELD,
                        TYPE_ARMOR,
                        TYPE_MAGIC,
                        TYPE_STAFF,
                        EXTRA_MAGIC
                );
                shopAssortmentBlocks[2][3].selectionMask = ShelfFlags.toValue(
                        MATERIAL_STEEL,
                        MATERIAL_MITHRILL,
                        MATERIAL_ADAMANTIUM,
                        MATERIAL_MAGIC_WOOD,
                        RARITY_COMMON,
                        RARITY_UNCOMMON,
                        RARITY_RARE,
                        RARITY_VERY_RARE,
                        TYPE_WEAPON,
                        EXTRA_COMMON
                );
            }
            case 0x32 -> {
                scenarioLib.unlockAvailableLocation(2, 3);
                if (state.mission52Unlocked != 0) {
                    scenarioLib.unlockAvailableLocation(1, 52);
                }
                state.innSceneUnlocked = 1;
                for (int entryIndex = 0; entryIndex < ScenarioLib.SHOP_ENTRY_COUNT; entryIndex++) {
                    ShopAssortmentEntry entry = shopAssortmentBlocks[3][entryIndex];
                    entry.minPrice = entryIndex == 2 ? 0 : 499;
                    entry.itemCount = entryIndex == 0 || entryIndex == 3 ? 100 : 0x14;
                    entry.maxSameTypeItems = entryIndex == 0 || entryIndex == 3 ? 2 : 1;
                }
                shopAssortmentBlocks[3][0].selectionMask = ShelfFlags.toValue(
                        MATERIAL_SILVER,
                        MATERIAL_GOLD,
                        MATERIAL_WOOD,
                        MATERIAL_MAGIC_WOOD,
                        MATERIAL_DRAGON_LEATHER,
                        MATERIAL_NONE,
                        RARITY_COMMON,
                        RARITY_UNCOMMON,
                        RARITY_RARE,
                        RARITY_VERY_RARE,
                        TYPE_WEAPON,
                        TYPE_SHIELD,
                        TYPE_ARMOR,
                        TYPE_MAGIC,
                        EXTRA_COMMON
                );
                shopAssortmentBlocks[3][1].selectionMask = ShelfFlags.toValue(
                        MATERIAL_STEEL,
                        MATERIAL_MITHRILL,
                        MATERIAL_WOOD,
                        MATERIAL_MAGIC_WOOD,
                        MATERIAL_NONE,
                        RARITY_COMMON,
                        RARITY_UNCOMMON,
                        RARITY_RARE,
                        RARITY_VERY_RARE,
                        TYPE_WEAPON,
                        TYPE_SHIELD,
                        TYPE_ARMOR,
                        TYPE_MAGIC,
                        TYPE_STAFF,
                        EXTRA_MAGIC
                );
                shopAssortmentBlocks[3][2].selectionMask = ShelfFlags.toValue(TYPE_OTHER);
                shopAssortmentBlocks[3][3].selectionMask = ShelfFlags.toValue(
                        MATERIAL_GOLD,
                        MATERIAL_MITHRILL,
                        MATERIAL_ADAMANTIUM,
                        MATERIAL_METEORIC,
                        MATERIAL_MAGIC_WOOD,
                        MATERIAL_NONE,
                        TYPE_WEAPON,
                        TYPE_SHIELD,
                        TYPE_ARMOR,
                        TYPE_MAGIC,
                        TYPE_STAFF,
                        EXTRA_MAGIC
                );
                state.locationEntryStates[2] = 1;
                state.locationEntryLocationIds[2] = 3;
            }
            case 0x3c -> {
                scenarioLib.unlockAvailableLocation(1, 0x50);
                state.locationEntryStates[5] = 1;
                state.locationEntryLocationIds[5] = 2;
                state.laterSceneState = 1;
                shopAssortmentBlocks[2][0].selectionMask = ShelfFlags.toValue(
                        MATERIAL_SILVER,
                        MATERIAL_MITHRILL,
                        MATERIAL_ADAMANTIUM,
                        MATERIAL_MAGIC_WOOD,
                        MATERIAL_NONE,
                        RARITY_COMMON,
                        RARITY_UNCOMMON,
                        RARITY_RARE,
                        RARITY_VERY_RARE,
                        TYPE_SHIELD,
                        TYPE_ARMOR,
                        TYPE_MAGIC,
                        EXTRA_COMMON
                );
                shopAssortmentBlocks[2][1].selectionMask = ShelfFlags.toValue(
                        MATERIAL_SILVER,
                        MATERIAL_MITHRILL,
                        MATERIAL_ADAMANTIUM,
                        MATERIAL_MAGIC_WOOD,
                        MATERIAL_NONE,
                        RARITY_COMMON,
                        RARITY_UNCOMMON,
                        RARITY_RARE,
                        RARITY_VERY_RARE,
                        TYPE_WEAPON,
                        TYPE_SHIELD,
                        TYPE_ARMOR,
                        TYPE_MAGIC,
                        TYPE_STAFF,
                        EXTRA_MAGIC
                );
                shopAssortmentBlocks[2][3].selectionMask = ShelfFlags.toValue(
                        MATERIAL_MITHRILL,
                        MATERIAL_ADAMANTIUM,
                        MATERIAL_MAGIC_WOOD,
                        RARITY_COMMON,
                        RARITY_UNCOMMON,
                        RARITY_RARE,
                        RARITY_VERY_RARE,
                        TYPE_WEAPON,
                        EXTRA_COMMON
                );
            }
            case 0x46 -> {
                state.locationEntryStates[3] = 1;
                state.locationEntryLocationIds[3] = 3;
                if (state.pairedObjectiveACompleted == 0
                        && state.pairedObjectiveBCompleted != 0) {
                    inOutResult[0] = 3;
                }
                state.pairedObjectiveACompleted = 1;
                state.specialDialogState = 1;
            }
            case 0x50 -> {
                if (state.pairedObjectiveBCompleted == 0
                        && state.pairedObjectiveACompleted != 0) {
                    inOutResult[0] = 3;
                }
                state.pairedObjectiveBCompleted = 1;
                shopAssortmentBlocks[2][0].selectionMask = ShelfFlags.toValue(
                        MATERIAL_MITHRILL,
                        MATERIAL_ADAMANTIUM,
                        MATERIAL_METEORIC,
                        MATERIAL_MAGIC_WOOD,
                        MATERIAL_DRAGON_LEATHER,
                        MATERIAL_NONE,
                        RARITY_UNCOMMON,
                        RARITY_RARE,
                        RARITY_VERY_RARE,
                        TYPE_SHIELD,
                        TYPE_ARMOR,
                        TYPE_MAGIC,
                        EXTRA_COMMON
                );
                shopAssortmentBlocks[2][1].selectionMask = ShelfFlags.toValue(
                        MATERIAL_MITHRILL,
                        MATERIAL_ADAMANTIUM,
                        MATERIAL_METEORIC,
                        MATERIAL_MAGIC_WOOD,
                        MATERIAL_DRAGON_LEATHER,
                        MATERIAL_NONE,
                        RARITY_UNCOMMON,
                        RARITY_RARE,
                        RARITY_VERY_RARE,
                        TYPE_WEAPON,
                        TYPE_SHIELD,
                        TYPE_ARMOR,
                        TYPE_MAGIC,
                        TYPE_STAFF,
                        EXTRA_MAGIC
                );
                shopAssortmentBlocks[2][3].selectionMask = ShelfFlags.toValue(
                        MATERIAL_MITHRILL,
                        MATERIAL_ADAMANTIUM,
                        MATERIAL_METEORIC,
                        MATERIAL_MAGIC_WOOD,
                        RARITY_UNCOMMON,
                        RARITY_RARE,
                        RARITY_VERY_RARE,
                        TYPE_WEAPON,
                        EXTRA_COMMON
                );
            }
            case 0x5a -> {
                shopAssortmentBlocks[2][0].selectionMask = ShelfFlags.toValue(
                        MATERIAL_ADAMANTIUM,
                        MATERIAL_METEORIC,
                        MATERIAL_MAGIC_WOOD,
                        MATERIAL_DRAGON_LEATHER,
                        MATERIAL_CRYSTAL,
                        MATERIAL_NONE,
                        RARITY_UNCOMMON,
                        RARITY_RARE,
                        RARITY_VERY_RARE,
                        TYPE_SHIELD,
                        TYPE_ARMOR,
                        TYPE_MAGIC,
                        EXTRA_COMMON
                );
                shopAssortmentBlocks[2][1].selectionMask = ShelfFlags.toValue(
                        MATERIAL_ADAMANTIUM,
                        MATERIAL_METEORIC,
                        MATERIAL_MAGIC_WOOD,
                        MATERIAL_DRAGON_LEATHER,
                        MATERIAL_CRYSTAL,
                        MATERIAL_NONE,
                        RARITY_UNCOMMON,
                        RARITY_RARE,
                        RARITY_VERY_RARE,
                        TYPE_WEAPON,
                        TYPE_SHIELD,
                        TYPE_ARMOR,
                        TYPE_MAGIC,
                        TYPE_STAFF,
                        EXTRA_MAGIC
                );
                shopAssortmentBlocks[2][3].selectionMask = ShelfFlags.toValue(
                        MATERIAL_ADAMANTIUM,
                        MATERIAL_METEORIC,
                        MATERIAL_MAGIC_WOOD,
                        MATERIAL_CRYSTAL,
                        RARITY_UNCOMMON,
                        RARITY_RARE,
                        RARITY_VERY_RARE,
                        TYPE_WEAPON,
                        EXTRA_COMMON
                );
            }
            case 100 -> {
                state.locationEntryStates[6] = 1;
                state.locationEntryLocationIds[6] = 2;
            }
            case 0x6e -> inOutResult[0] = state.endingVariantFlag == 0 ? 4 : 5;
            default -> {
            }
        }
        if (id % 10 == 0) {
            state.scenarioChapter += 10;
        }
        switch (state.scenarioChapter) {
            case 0x1e -> {
                for (int entryIndex = 0; entryIndex < ScenarioLib.SHOP_ENTRY_COUNT; entryIndex++) {
                    shopAssortmentBlocks[2][entryIndex].maxPrice = 10000;
                }
            }
            case 0x28 -> {
                for (int entryIndex = 0; entryIndex < ScenarioLib.SHOP_ENTRY_COUNT; entryIndex++) {
                    shopAssortmentBlocks[2][entryIndex].maxPrice = 22000;
                }
            }
            case 0x32 -> {
                for (int entryIndex = 0; entryIndex < ScenarioLib.SHOP_ENTRY_COUNT; entryIndex++) {
                    shopAssortmentBlocks[3][entryIndex].maxPrice = 60000;
                    shopAssortmentBlocks[2][entryIndex].maxPrice = 60000;
                }
            }
            case 0x3c -> {
                for (int entryIndex = 0; entryIndex < ScenarioLib.SHOP_ENTRY_COUNT; entryIndex++) {
                    shopAssortmentBlocks[3][entryIndex].maxPrice = 150000;
                    shopAssortmentBlocks[2][entryIndex].maxPrice = 150000;
                }
            }
            case 0x46 -> {
                for (int entryIndex = 0; entryIndex < ScenarioLib.SHOP_ENTRY_COUNT; entryIndex++) {
                    shopAssortmentBlocks[3][entryIndex].maxPrice = 400000;
                    shopAssortmentBlocks[2][entryIndex].maxPrice = 400000;
                }
            }
            case 0x50 -> {
                for (int entryIndex = 0; entryIndex < ScenarioLib.SHOP_ENTRY_COUNT; entryIndex++) {
                    shopAssortmentBlocks[3][entryIndex].maxPrice = 800000;
                    shopAssortmentBlocks[2][entryIndex].maxPrice = 800000;
                }
            }
            case 0x5a -> {
                for (int entryIndex = 0; entryIndex < ScenarioLib.SHOP_ENTRY_COUNT; entryIndex++) {
                    shopAssortmentBlocks[3][entryIndex].maxPrice = 1_500_000;
                    shopAssortmentBlocks[2][entryIndex].maxPrice = 1_500_000;
                    if (entryIndex != 2) {
                        shopAssortmentBlocks[2][entryIndex].minPrice = 12000;
                    }
                }
            }
            case 100 -> {
                for (int entryIndex = 0; entryIndex < ScenarioLib.SHOP_ENTRY_COUNT; entryIndex++) {
                    shopAssortmentBlocks[3][entryIndex].maxPrice = 5_000_000;
                    shopAssortmentBlocks[2][entryIndex].maxPrice = 5_000_000;
                    if (entryIndex != 2) {
                        shopAssortmentBlocks[2][entryIndex].minPrice = 40000;
                    }
                }
            }
            case 0x6e -> {
                for (int entryIndex = 0; entryIndex < ScenarioLib.SHOP_ENTRY_COUNT; entryIndex++) {
                    shopAssortmentBlocks[3][entryIndex].maxPrice = 10_000_000;
                    shopAssortmentBlocks[2][entryIndex].maxPrice = 10_000_000;
                }
            }
            default -> {
            }
        }
        return 0;
    }

    /**
     * Support method for Scenario.dll static location globals, not ported.
     */
    private static ScenarioLocation createStaticLocation(
            int kind,
            int id,
            int left,
            int top,
            int width,
            int height
    ) {
        return new ScenarioLocation(kind, id, left, top, width, height);
    }
}
