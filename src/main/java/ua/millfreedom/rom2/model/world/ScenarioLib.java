package ua.millfreedom.rom2.model.world;

import ua.millfreedom.rom2.model.ShelfFlags;
import ua.millfreedom.rom2.model.ShopAssortmentEntry;
import ua.millfreedom.rom2.res.CGameFile;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static ua.millfreedom.rom2.model.ShelfFlagValues.*;

/**
 * Java replacement for the Scenario.dll API loaded by CWinApp::InitInstance @00480C8D.
 */
public final class ScenarioLib {
    static final int LOCATION_TRANSIENT_COUNT = ScenarioState.CURRENT_LOCATION_TRANSIENT_COUNT;
    static final int LOCATION_ENTRY_COUNT = ScenarioState.LOCATION_ENTRY_COUNT;
    static final int SHOP_BLOCK_COUNT = 4;
    static final int SHOP_ENTRY_COUNT = 4;

    public final ScenarioState state = new ScenarioState();
    final ShopAssortmentEntry[][] shopAssortmentBlocks = createShopAssortmentBlocks();
    private final List<Integer> innEntries = new ArrayList<>();
    private final List<ScenarioLocation> allLocations = new ArrayList<>();
    final List<ScenarioLocation> availableLocations = new ArrayList<>();
    ScenarioLocation currentLocation;

    /**
     * Native: ScenarioGetVar @10001cfd.
     * Partial port. Native reads directly from g_scenarioReadBuffer[varId]; Java reads from the structured 4 KB ScenarioState mirror.
     */
    public int getVar(int varId) {
        return state.getDword(varId);
    }

    /**
     * Native: ScenarioSetVar @10001d0e.
     * Partial port. Native writes directly to g_scenarioReadBuffer[varId]; Java writes through the structured 4 KB ScenarioState mirror.
     */
    public void setVar(int varId, int value) {
        state.setDword(varId, value);
    }

    /**
     * Support method for completed-location flags stored in `g_scenarioReadBuffer[0x380..0x3ff]`, not ported.
     */
    private int getCompletedLocationFlag(int locationId) {
        if (locationId < 0 || locationId >= state.completedLocationFlags.length) {
            return 0;
        }
        return state.completedLocationFlags[locationId];
    }

    /**
     * Support method for completed-location flags stored in `g_scenarioReadBuffer[0x380..0x3ff]`, not ported.
     */
    void setCompletedLocationFlag(int locationId, int value) {
        if (locationId < 0 || locationId >= state.completedLocationFlags.length) {
            return;
        }
        state.completedLocationFlags[locationId] = value;
    }

    /**
     * Native: ScenarioTalkTo @100032c0.
     * Partial port. The packed token decode, direct g_scenarioReadBuffer writes, and mission unlock path are ported, but the surrounding global location table is still modeled through Java lists rather than the original MFC containers.
     */
    public int talkTo(int token) {
        int entryId = token & 0xffff;
        int actionId = token >>> 16 & 0x0fff;
        int actionKind = token >>> 28 & 0x7;
        if (actionKind == 0) {
            if (entryId == 5) {
                state.locationEntryStates[4] = 1;
                state.locationEntryLocationIds[4] = 3;
                state.laterSceneState = 2;
            }
            if (entryId == 0x02a3 && actionId == 0x4e) {
                state.specialDialogState = 2;
                shopAssortmentBlocks[3][3].selectionMask |= ShelfFlags.toValue(RARITY_ELVEN);
            }
            return actionKind;
        }
        if (actionKind == 1) {
            int entryIndex = entryId - 1;
            int flagValue = state.locationEntrySelectionFlags[entryIndex] == 0 ? 1 : 0;
            state.locationEntrySelectionFlags[entryIndex] = flagValue;
            return flagValue;
        }
        if (actionKind == 3) {
            unlockAvailableLocation(1, actionId);
            if (actionId == ScenarioLocation.MISSION_10.id) {
                state.startMissionAllowed = 1;
            }
            if (entryId == 0x16 && actionId == 0x1e) {
                state.locationEntryStates[1] = 1;
                state.locationEntryLocationIds[1] = 2;
            }
        }
        return actionKind;
    }

    /**
     * Native: ScenarioEnterLocation @10001d22.
     * Partial port. Native stores the selected location handle and clears 16 transient dwords starting at g_scenarioReadBuffer + 0xBC0.
     */
    public void enterLocation(ScenarioLocation location) {
        currentLocation = location;
        for (int flagIndex = 0; flagIndex < LOCATION_TRANSIENT_COUNT; flagIndex++) {
            state.currentLocationTransientFlags[flagIndex] = 0;
        }
    }

    /**
     * Native: ScenarioLeaveLocation @10002806.
     * Partial port. Native initializes `inOutResult[0]` to `-1`, returns only the helper status from
     * `ClearCurrentAvailableLocation`/`OnCompleted`, and writes the transition code through the caller-provided
     * in/out dword.
     */
    public int leaveLocation(int[] inOutResult) {
        if (inOutResult.length == 0) {
            throw new IllegalArgumentException("inOutResult must have length >= 1");
        }
        inOutResult[0] = -1;
        if (currentLocation == null) {
            return 0;
        }
        int locationId = currentLocation.id;
        if (currentLocation.kind == 2) {
            return ScenarioLocation.clearCurrentAvailableLocation(this, inOutResult, locationId);
        }
        return ScenarioLocation.onCompleted(this, inOutResult, locationId);
    }

    /**
     * Native: ScenarioEnterShop @1000285f.
     * Full port. Native implementation is a no-op that always returns 0.
     */
    public int enterShop() {
        return 0;
    }

    /**
     * Native: ScenarioLeaveShop @10002866.
     * Full port. Native implementation is an empty no-op.
     */
    public void leaveShop() {
    }

    /**
     * Native: ScenarioEnterInn @1000286b.
     * Partial port. The chapter/location switch tree and packed inn-entry emission are ported directly from g_scenarioReadBuffer, but the Java callers still consume the result as a plain list instead of native out-parameters.
     */
    public List<Integer> enterInn() {
        int chapter = state.scenarioChapter;
        int currentLocationId = getCurrentLocationId();
        innEntries.clear();
        switch (chapter) {
            case 10 -> {
                innEntries.add(0x000900cf);
                innEntries.add(0x0008083c);
                innEntries.add(0x300a0205);
            }
            case 0x1e -> {
                innEntries.add(0x301e0016);
                if (getCompletedLocationFlag(31) == 0) {
                    innEntries.add(0x301f083c);
                }
                innEntries.add(0x0027083e);
            }
            case 0x28 -> {
                innEntries.add(0x30280016);
                innEntries.add(0x0030083c);
                if (getCompletedLocationFlag(41) == 0) {
                    innEntries.add(0x302907df);
                }
                if (getCompletedLocationFlag(42) == 0) {
                    innEntries.add(0x302a083f);
                }
                if (getCompletedLocationFlag(43) == 0) {
                    innEntries.add(0x302b07d4);
                }
            }
            case 0x32 -> {
                innEntries.add(0x00310017);
                if (state.locationEntryStates[0] != 0 && getCompletedLocationFlag(51) == 0) {
                    innEntries.add(0x30330002);
                }
                if (getCompletedLocationFlag(53) == 0) {
                    innEntries.add(0x303507e3);
                }
            }
            case 0x3c, 0x46, 0x50 -> {
                if (currentLocationId == 3
                        && state.innSceneUnlocked != 0
                        && getCompletedLocationFlag(70) == 0) {
                    innEntries.add(0x304602a8);
                    if (getCompletedLocationFlag(71) == 0) {
                        innEntries.add(0x304702a9);
                    }
                }
                if (currentLocationId == 3
                        && chapter == 0x50
                        && getCompletedLocationFlag(83) == 0
                        && state.locationEntryStates[4] != 0) {
                    innEntries.add(0x305302a5);
                }
                if (currentLocationId == 2 && chapter == 0x3c) {
                    if (getCompletedLocationFlag(61) == 0) {
                        innEntries.add(0x303d07d6);
                    }
                    if (getCompletedLocationFlag(63) == 0) {
                        innEntries.add(0x303f083d);
                    }
                }
                if (currentLocationId == 2 && chapter == 0x46) {
                    if (getCompletedLocationFlag(73) == 0) {
                        innEntries.add(0x304907d4);
                    }
                    if (getCompletedLocationFlag(72) == 0) {
                        innEntries.add(0x3048083c);
                    }
                }
                if (chapter == 0x50 && currentLocationId == 2) {
                    if (state.isMage == 0) {
                        if (getCompletedLocationFlag(82) == 0) {
                            innEntries.add(0x305207d9);
                        }
                    } else if (getCompletedLocationFlag(81) == 0) {
                        innEntries.add(0x305107da);
                    }
                }
            }
            case 0x5a -> {
                if (currentLocationId == 2) {
                    innEntries.add(0x305a07d4);
                } else if (currentLocationId == 3) {
                    if (getCompletedLocationFlag(91) == 0) {
                        innEntries.add(0x305b02a9);
                    }
                    if (getCompletedLocationFlag(92) == 0) {
                        innEntries.add(0x305c07d3);
                    }
                }
            }
            case 100 -> {
                if (currentLocationId == 2) {
                    innEntries.add(0x306407d6);
                    if (getCompletedLocationFlag(102) == 0) {
                        innEntries.add(0x3066083d);
                    }
                    if (getCompletedLocationFlag(91) != 0 && getCompletedLocationFlag(103) == 0) {
                        innEntries.add(0x306707d5);
                    }
                } else if (currentLocationId == 3 && getCompletedLocationFlag(101) == 0) {
                    innEntries.add(0x306502a9);
                }
            }
            case 0x6e -> {
                if (currentLocationId == 2) {
                    innEntries.add(0x306e07d6);
                }
            }
            default -> {
            }
        }
        if (chapter > 0x3c && state.laterSceneState == 1 && currentLocationId == 3) {
            innEntries.add(0x004f0005);
        }
        if (chapter > 0x3c && state.specialDialogState == 1 && currentLocationId == 3) {
            innEntries.add(0x004e02a3);
        }
        if (currentLocationId == 2) {
            if (getCompletedLocationFlag(63) != 0
                    && getCompletedLocationFlag(74) == 0
                    && getCompletedLocationFlag(75) == 0
                    && getCompletedLocationFlag(76) == 0
                    && getCompletedLocationFlag(77) == 0) {
                innEntries.add(resolveBranchingInnEntry(
                        0x304a07e6,
                        0x304b07e6,
                        0x304d07e6,
                        0x304c07e6
                ));
            }
            if ((getCompletedLocationFlag(74) != 0
                    || getCompletedLocationFlag(75) != 0
                    || getCompletedLocationFlag(76) != 0
                    || getCompletedLocationFlag(77) != 0)
                    && getCompletedLocationFlag(84) == 0
                    && getCompletedLocationFlag(85) == 0
                    && getCompletedLocationFlag(86) == 0
                    && getCompletedLocationFlag(87) == 0) {
                innEntries.add(resolveBranchingInnEntry(
                        0x305407e6,
                        0x305507e6,
                        0x305707e6,
                        0x305607e6
                ));
            }
            if ((getCompletedLocationFlag(84) != 0
                    || getCompletedLocationFlag(85) != 0
                    || getCompletedLocationFlag(86) != 0
                    || getCompletedLocationFlag(87) != 0)
                    && getCompletedLocationFlag(94) == 0
                    && getCompletedLocationFlag(95) == 0
                    && getCompletedLocationFlag(96) == 0
                    && getCompletedLocationFlag(93) == 0) {
                innEntries.add(resolveBranchingInnEntry(
                        0x305d07e6,
                        0x305e07e6,
                        0x306007e6,
                        0x305f07e6
                ));
            }
        }
        if (currentLocationId == 2
                && chapter > 0x3b
                && getCompletedLocationFlag(62) == 0
                && getCompletedLocationFlag(53) != 0) {
            innEntries.add(0x303e0016);
        }
        for (int entryIndex = 0; entryIndex < LOCATION_ENTRY_COUNT; entryIndex++) {
            if (state.locationEntryLocationIds[entryIndex] != currentLocationId) {
                continue;
            }
            int entryState = state.locationEntryStates[entryIndex];
            if (entryState == 1) {
                int entryToken = entryIndex + 1 | 0x10000000;
                if (state.locationEntrySelectionFlags[entryIndex] > 0) {
                    entryToken |= 0x80000000;
                }
                innEntries.add(entryToken);
            } else if (entryState == 2) {
                int entryToken = entryIndex + 1 | 0x20000000;
                if (state.locationEntrySelectionFlags[entryIndex] > 0) {
                    entryToken |= 0x80000000;
                }
                innEntries.add(entryToken);
            }
        }
        return List.copyOf(innEntries);
    }

    /**
     * Native: ScenarioLeaveInn @100032bb.
     * Fully ported. Native implementation is an empty no-op.
     */
    public void leaveInn() {
    }

    /**
     * Native: ScenarioNewGame @100033be.
     * Partial port. The 4 KB scenario-buffer reset, static location-table seeding, initial chapter value, initial available town, and initial enter-location call are ported, but the separate non-buffer campaign arrays initialized after that remain unresolved.
     */
    public void newGame() {
        state.clear();
        innEntries.clear();
        seedAllLocationsFromStaticTable();
        clearShopAssortmentBlocks();
        availableLocations.clear();
        state.scenarioChapter = 10;
        unlockAvailableLocation(2, 1);
        enterLocation(findLocationByKindAndId(2, 1));
        initializeDefaultShopAssortments();
    }

    /**
     * Native: ScenarioSave @10003585.
     * Partial port. Java writes the 4 KB scenario buffer, the 4x4x0x14 native assortment records, the available-location records, and the current-location index in native order, but still relies on `CGameFile` exposing a writable `ByteBuffer` instead of the original MFC file vtable.
     */
    public void save(CGameFile gameFile) {
        ByteBuffer buffer = gameFile.getData().duplicate().order(ByteOrder.LITTLE_ENDIAN);
        writeSavePayload(buffer);
    }

    /**
     * Native support extracted from ScenarioSave @10003585 for callers that append the save payload directly.
     */
    public byte[] saveToBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(savePayloadSize()).order(ByteOrder.LITTLE_ENDIAN);
        writeSavePayload(buffer);
        return writtenBytes(buffer);
    }

    /**
     * Native support extracted from ScenarioSave @10003585.
     */
    private void writeSavePayload(ByteBuffer buffer) {
        state.writeTo(buffer);
        for (int blockIndex = 0; blockIndex < SHOP_BLOCK_COUNT; blockIndex++) {
            for (ShopAssortmentEntry entry : shopAssortmentBlocks[blockIndex]) {
                buffer.putInt(entry.minPrice);
                buffer.putInt(entry.maxPrice);
                buffer.putInt(entry.itemCount);
                buffer.putInt(entry.maxSameTypeItems);
                buffer.putInt(entry.selectionMask);
            }
        }
        buffer.putInt(availableLocations.size());
        int currentLocationIndex = 0;
        for (int locationIndex = 0; locationIndex < availableLocations.size(); locationIndex++) {
            ScenarioLocation location = availableLocations.get(locationIndex);
            writeLocationRecord(buffer, location);
            if (location == currentLocation) {
                currentLocationIndex = locationIndex;
            }
        }
        buffer.putInt(currentLocationIndex);
    }

    /**
     * Native support extracted from ScenarioSave @10003585 serialized buffer shape.
     */
    private int savePayloadSize() {
        return ScenarioState.DWORD_COUNT * Integer.BYTES
                + SHOP_BLOCK_COUNT * SHOP_ENTRY_COUNT * 0x14
                + Integer.BYTES
                + availableLocations.size() * 0x18
                + Integer.BYTES;
    }

    /**
     * Native support extracted from ScenarioSave @10003585 CFile write payload length.
     */
    private static byte[] writtenBytes(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.position()];
        buffer.flip();
        buffer.get(bytes);
        return bytes;
    }

    /**
     * Native: ScenarioLoad @10003686.
     * Partial port. Java reads the 4 KB scenario buffer, restores the native 4x4x0x14 assortment records, rebuilds available locations from the recovered static location table, and rebinds the current location by saved index.
     */
    public void load(CGameFile gameFile) {
        load(gameFile.getData());
    }

    /**
     * Native: ScenarioLoad @10003686.
     */
    public void load(ByteBuffer sourceBuffer) {
        ByteBuffer buffer = sourceBuffer.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        innEntries.clear();
        state.clear();
        state.readFrom(buffer);
        for (int blockIndex = 0; blockIndex < SHOP_BLOCK_COUNT; blockIndex++) {
            for (ShopAssortmentEntry entry : shopAssortmentBlocks[blockIndex]) {
                entry.minPrice = buffer.getInt();
                entry.maxPrice = buffer.getInt();
                entry.itemCount = buffer.getInt();
                entry.maxSameTypeItems = buffer.getInt();
                entry.selectionMask = buffer.getInt();
            }
        }
        int availableLocationCount = buffer.getInt();
        availableLocations.clear();
        seedAllLocationsFromStaticTable();
        for (int locationIndex = 0; locationIndex < availableLocationCount; locationIndex++) {
            ScenarioLocation loadedLocation = readLocationRecord(buffer);
            ScenarioLocation canonicalLocation = findLocationByKindAndId(loadedLocation.kind, loadedLocation.id);
            if (canonicalLocation == null) {
                throw new IllegalStateException(
                        "ScenarioLoad encountered unknown location kind=" + loadedLocation.kind
                                + " id=" + loadedLocation.id
                );
            }
            if (!availableLocations.contains(canonicalLocation)) {
                availableLocations.add(canonicalLocation);
            }
        }
        int currentLocationIndex = buffer.getInt();
        sourceBuffer.position(buffer.position());
        if (availableLocations.isEmpty()) {
            currentLocation = null;
            return;
        }
        if (currentLocationIndex < 0 || currentLocationIndex >= availableLocations.size()) {
            currentLocationIndex = 0;
        }
        currentLocation = availableLocations.get(currentLocationIndex);
    }

    /**
     * Native: ScenarioGetAvailableLocations @100037de.
     */
    public List<ScenarioLocation> getAvailableLocations() {
        return List.copyOf(availableLocations);
    }

    /**
     * Native: ScenarioGetShopAssortment @100037e8.
     * Partial port. Native returns a pointer to the current location's 4-entry assortment block; Java returns the matching four recovered 0x14 records by direct reference.
     */
    public ShopAssortmentEntry[] getShopAssortment() {
        int currentLocationId = getCurrentLocationId();
        if (currentLocationId < 0 || currentLocationId >= shopAssortmentBlocks.length) {
            return null;
        }
        return shopAssortmentBlocks[currentLocationId];
    }

    /**
     * Native: ScenarioIsTownAvailable @10003800.
     */
    public boolean isTownAvailable(int locationId) {
        for (ScenarioLocation location : availableLocations) {
            if (location.kind == 2 && (location.id == locationId || locationId == 0)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Native: ScenarioIsMissionAvailable @10003871.
     */
    public boolean isMissionAvailable(int missionId) {
        for (ScenarioLocation location : availableLocations) {
            if (location.kind == 1 && location.id == missionId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Native: ScenarioGetCurrentLocation @100038c8.
     */
    public ScenarioLocation getCurrentLocation() {
        return currentLocation;
    }

    /**
     * Native: ScenarioGetAllLocations @100038d2.
     */
    public List<ScenarioLocation> getAllLocations() {
        return List.copyOf(allLocations);
    }

    /**
     * Support method for Scenario.dll location-list unlock helpers, not ported.
     */
    void unlockAvailableLocation(int kind, int id) {
        ScenarioLocation location = findLocationByKindAndId(kind, id);
        if (location != null && !availableLocations.contains(location)) {
            availableLocations.add(location);
        }
    }

    /**
     * Support method for Scenario.dll location-list unlock helpers, not ported.
     */
    private ScenarioLocation findLocationByKindAndId(int kind, int id) {
        for (ScenarioLocation location : allLocations) {
            if (location.kind == kind && location.id == id) {
                return location;
            }
        }
        return null;
    }

    /**
     * Native helper: FUN_10001a0f @10001a0f.
     * Partial port. Seeds the recovered native-order static ScenarioLocation globals from ScenarioLocation.ALL_LOCATIONS, which mirrors g_allLocations after FUN_10001a0f runs.
     */
    private void seedAllLocationsFromStaticTable() {
        allLocations.clear();
        allLocations.addAll(ScenarioLocation.ALL_LOCATIONS);
    }

    /**
     * Native helper tail in ScenarioLeaveLocation @10002806.
     * Partial port. Rebuilds the recovered late-game available-location list and preserves the native insertion order used after the 0xC1C gate trips.
     */
    void rebuildLateScenarioAvailableLocations() {
        availableLocations.clear();
        unlockAvailableLocation(2, 2);
        unlockAvailableLocation(2, 3);
        unlockAvailableLocation(1, 30);
        unlockAvailableLocation(1, 40);
        unlockAvailableLocation(1, 50);
        unlockAvailableLocation(1, 60);
        unlockAvailableLocation(1, 70);
        unlockAvailableLocation(1, 80);
        unlockAvailableLocation(1, 90);
        unlockAvailableLocation(1, 100);
        unlockAvailableLocation(1, 110);
        unlockAvailableLocation(1, 21);
        unlockAvailableLocation(1, 31);
        unlockAvailableLocation(1, 32);
        unlockAvailableLocation(1, 41);
        unlockAvailableLocation(1, 42);
        unlockAvailableLocation(1, 43);
        unlockAvailableLocation(1, 51);
        unlockAvailableLocation(1, 52);
        unlockAvailableLocation(1, 53);
        unlockAvailableLocation(1, 61);
        unlockAvailableLocation(1, 62);
        unlockAvailableLocation(1, 63);
        unlockAvailableLocation(1, 71);
        unlockAvailableLocation(1, 72);
        unlockAvailableLocation(1, 73);
        unlockAvailableLocation(1, 74);
        unlockAvailableLocation(1, 75);
        unlockAvailableLocation(1, 76);
        unlockAvailableLocation(1, 77);
        unlockAvailableLocation(1, 81);
        unlockAvailableLocation(1, 82);
        unlockAvailableLocation(1, 83);
        unlockAvailableLocation(1, 84);
        unlockAvailableLocation(1, 85);
        unlockAvailableLocation(1, 86);
        unlockAvailableLocation(1, 87);
        if (state.isMage == 0) {
            if (state.isFemale == 0) {
                unlockAvailableLocation(1, 91);
            } else {
                unlockAvailableLocation(1, 92);
            }
        } else if (state.isFemale == 0) {
            unlockAvailableLocation(1, 94);
        } else {
            unlockAvailableLocation(1, 93);
        }
        unlockAvailableLocation(1, 95);
        unlockAvailableLocation(1, 96);
        unlockAvailableLocation(1, 101);
        unlockAvailableLocation(1, 102);
        unlockAvailableLocation(1, 103);
    }

    /**
     * Native helper in ScenarioNewGame @100033be.
     * Partial port. Seeds the recovered native assortment defaults for towns 1 and 2, but later chapter-specific mutations are handled separately.
     */
    private void initializeDefaultShopAssortments() {
        for (int i = 0; i < SHOP_ENTRY_COUNT; i++) {
            shopAssortmentBlocks[1][i].minPrice = 0;
            shopAssortmentBlocks[1][i].maxPrice = 1500;
            shopAssortmentBlocks[1][i].itemCount = i < 2 ? 100 : 20;
            shopAssortmentBlocks[1][i].maxSameTypeItems = i < 2 ? 2 : 1;

            shopAssortmentBlocks[2][i].minPrice = 0;
            shopAssortmentBlocks[2][i].maxPrice = 5000;
            shopAssortmentBlocks[2][i].itemCount = (i == 0 || i == 3) ? 100 : 20;
            shopAssortmentBlocks[2][i].maxSameTypeItems = (i == 0 || i == 3) ? 2 : 1;
        }
        shopAssortmentBlocks[1][0].selectionMask = ShelfFlags.toValue(
                EXTRA_COMMON, TYPE_MAGIC, TYPE_ARMOR, TYPE_SHIELD, RARITY_UNCOMMON, RARITY_COMMON, MATERIAL_NONE,
                MATERIAL_HARD_LEATHER, MATERIAL_LEATHER, MATERIAL_BRONZE, MATERIAL_IRON);
        shopAssortmentBlocks[1][1].selectionMask = ShelfFlags.toValue(MATERIAL_IRON, MATERIAL_BRONZE, MATERIAL_WOOD,
                RARITY_COMMON, RARITY_UNCOMMON, TYPE_WEAPON, EXTRA_COMMON);
        shopAssortmentBlocks[1][2].selectionMask = ShelfFlags.toValue(MATERIAL_WOOD, RARITY_COMMON, RARITY_UNCOMMON,
                TYPE_STAFF, EXTRA_MAGIC);
        shopAssortmentBlocks[1][3].selectionMask = ShelfFlags.toValue(TYPE_OTHER);


        shopAssortmentBlocks[2][0].selectionMask = ShelfFlags.toValue(
                MATERIAL_IRON, MATERIAL_STEEL, MATERIAL_WOOD, MATERIAL_MAGIC_WOOD, MATERIAL_NONE, RARITY_COMMON,
                RARITY_UNCOMMON, RARITY_RARE, TYPE_SHIELD, TYPE_ARMOR, TYPE_MAGIC, EXTRA_COMMON
        );
        shopAssortmentBlocks[2][1].selectionMask = ShelfFlags.toValue(
                MATERIAL_STEEL, MATERIAL_WOOD, MATERIAL_MAGIC_WOOD, MATERIAL_NONE, RARITY_COMMON, RARITY_UNCOMMON,
                RARITY_RARE, TYPE_WEAPON, TYPE_SHIELD, TYPE_ARMOR, TYPE_MAGIC, TYPE_STAFF, EXTRA_MAGIC
        );
        shopAssortmentBlocks[2][2].selectionMask = ShelfFlags.toValue(TYPE_OTHER);
        shopAssortmentBlocks[2][3].selectionMask = ShelfFlags.toValue(MATERIAL_IRON, MATERIAL_STEEL, MATERIAL_WOOD,
                MATERIAL_MAGIC_WOOD, RARITY_COMMON, RARITY_UNCOMMON, RARITY_RARE, TYPE_WEAPON, EXTRA_COMMON
        );
    }


    /**
     * Support method for Scenario.dll shop-assortment blocks, not ported.
     */
    private void clearShopAssortmentBlocks() {
        for (int blockIndex = 0; blockIndex < SHOP_BLOCK_COUNT; blockIndex++) {
            for (ShopAssortmentEntry entry : shopAssortmentBlocks[blockIndex]) {
                entry.minPrice = 0;
                entry.maxPrice = 0;
                entry.itemCount = 0;
                entry.maxSameTypeItems = 0;
                entry.selectionMask = ShelfFlags.toValue();
            }
        }
    }

    /**
     * Support method for Scenario.dll shop-assortment blocks, not ported.
     */
    private ShopAssortmentEntry[][] createShopAssortmentBlocks() {
        ShopAssortmentEntry[][] blocks = new ShopAssortmentEntry[SHOP_BLOCK_COUNT][SHOP_ENTRY_COUNT];
        for (int blockIndex = 0; blockIndex < SHOP_BLOCK_COUNT; blockIndex++) {
            for (int entryIndex = 0; entryIndex < SHOP_ENTRY_COUNT; entryIndex++) {
                blocks[blockIndex][entryIndex] = new ShopAssortmentEntry();
            }
        }
        return blocks;
    }

    /**
     * Support method for Scenario.dll location serialization, not ported.
     */
    private void writeLocationRecord(ByteBuffer buffer, ScenarioLocation location) {
        buffer.putInt(location.id);
        buffer.putInt(location.kind);
        buffer.putInt(location.rect.left);
        buffer.putInt(location.rect.top);
        buffer.putInt(location.rect.right);
        buffer.putInt(location.rect.bottom);
    }

    /**
     * Support method for Scenario.dll location serialization, not ported.
     */
    private ScenarioLocation readLocationRecord(ByteBuffer buffer) {
        ScenarioLocation location = new ScenarioLocation();
        location.id = buffer.getInt();
        location.kind = buffer.getInt();
        location.rect.left = buffer.getInt();
        location.rect.top = buffer.getInt();
        location.rect.right = buffer.getInt();
        location.rect.bottom = buffer.getInt();
        return location;
    }

    /**
     * Support method for ScenarioEnterInn branching tokens, not ported.
     */
    private int resolveBranchingInnEntry(int noTownOne, int townTwo, int noTownThree, int townFour) {
        if (state.isMage == 0) {
            if (state.isFemale == 0) {
                return noTownOne;
            }
            return townTwo;
        }
        if (state.isFemale == 0) {
            return noTownThree;
        }
        return townFour;
    }

    /**
     * Support method for Scenario.dll current-location helpers, not ported.
     */
    private int getCurrentLocationId() {
        return currentLocation == null ? 0 : currentLocation.id;
    }
}
