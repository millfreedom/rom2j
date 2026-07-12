package ua.millfreedom.rom2;


import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.model.*;
import ua.millfreedom.rom2.model.column.HumanColumn;
import ua.millfreedom.rom2.model.column.MagicColumn;
import ua.millfreedom.rom2.model.column.UnitColumn;
import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.model.spell.SpellInfo;
import ua.millfreedom.rom2.model.unit.UnitInfo;
import ua.millfreedom.rom2.model.unit.humanoid.human.HumanInfo;
import ua.millfreedom.rom2.res.CGameFile;
import ua.millfreedom.rom2.res.Resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static ua.millfreedom.rom2.res.Constants.DATA;
import static ua.millfreedom.rom2.res.Constants.WORLD;

public final class CStaticDataMgr implements MfcSerializable {

    private static final CStaticDataMgr inst = new CStaticDataMgr();

    public final CustomList<Material> materials = new CustomList<>(Material.class);
    public final CustomList<Material> shapes = new CustomList<>(Material.class);
    public final CustomList<MagicInfo> magic = new CustomList<>(MagicInfo.class);
    public final CustomList<WorldItem> shields = new CustomList<>(WorldItem.class, 1);
    public final CustomList<WorldItem> armors = new CustomList<>(WorldItem.class, 1);
    public final CustomList<WorldItem> weapons = new CustomList<>(WorldItem.class, 1);
    public final CustomList<MagicalItemInfo> magicItems = new CustomList<>(MagicalItemInfo.class, 1);
    public final CustomList<UnitInfo> units = new CustomList<>(UnitInfo.class, 1);
    public final CustomList<HumanInfo> humans = new CustomList<>(HumanInfo.class, 1);
    public final CustomList<HumanInfo> a10 = new CustomList<>(HumanInfo.class, 1); // NOT serialized here
    public final CustomList<BuildingInfo> buildings = new CustomList<>(BuildingInfo.class, 1);
    public final CustomList<SpellInfo> spells = new CustomList<>(SpellInfo.class, 1);
    public static final int SPELL_LIMIT = 30;
    public boolean m_bLoaded; // NOT serialized here

    // not ported.
    public static CStaticDataMgr getInstance() {
        return inst;
    }

    /**
     * Native support extracted from CMainApp::InitInstance @00480C8D CStaticDataMgr::LoadOrRebuild call.
     */
    public int loadOrRebuild() {
        return loadOrRebuild(defaultStaticDataBasePath());
    }

    /**
     * Native: CStaticDataMgr::LoadOrRebuild @004F8F09.
     * Fully ported.
     */
    public int loadOrRebuild(Path basePath) {
        if (m_bLoaded) {
            return 0;
        }
        int loadResult = loadBinary(basePath);
        if (loadResult != 0) {
            if (loadResult != 1) {
                return 2;
            }
            Globals.gameServer.pushMessage("StaticData files not found");
            Globals.gameServer.pushMessage("Parsing .txt files");
            if (parseTextFiles(basePath) != 0) {
                Globals.gameServer.pushMessage("Error loading .txt files");
                return 1;
            }
            Globals.gameServer.pushMessage("Writing new .bin file");
            saveBinary(basePath);
        }
        m_bLoaded = true;
        return 0;
    }

    /**
     * Native: CStaticDataMgr::LoadBinary @004F9FB0.
     * Fully ported.
     */
    public int loadBinary(Path basePath) {
        Path dataPath = resolveNativeDataBinPath(basePath);
        byte[] data = readPrimaryStaticDataBytes(dataPath);
        if (data == null) {
            try {
                data = readBundledStaticDataBytes();
            } catch (Exception e) {
                return 1;
            }
        }

        try (CArchive ar = CArchive.forReadingFromBytes(data)) {
            serialize(ar);
        } catch (Exception e) {
            Globals.gameServer.pushMessage("Invalid or outdated " + dataPath);
            return 2;
        }
        return 0;
    }

    /**
     * Native: CStaticDataMgr::SaveBinary @004FA342.
     * Fully ported.
     */
    public void saveBinary(Path basePath) {
        Path dataPath = resolveNativeDataBinPath(basePath);
        if (!canOpenNativeDataBinForWrite(dataPath)) {
            return;
        }

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (CArchive ar = CArchive.forWritingToBytes(output)) {
                serialize(ar);
            }
            Files.write(
                    dataPath,
                    output.toByteArray(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Native: CStaticDataMgr::ParseTextFiles @004F90F4.
     * Fully ported.
     */
    public int parseTextFiles(Path basePath) {
        if (parseRemStaticDataTable(
                basePath,
                "Spells.txt",
                Tables.spells,
                spells,
                0x23,
                1,
                1,
                SpellInfo::new,
                SpellInfo::init
        ) != 0) {
            return 1;
        }
        if (parseSimpleStaticDataTable(
                basePath,
                "Armors.txt",
                Tables.worldItems,
                armors,
                0x0F,
                1,
                1,
                true,
                WorldItem::new,
                WorldItem::init
        ) != 0) {
            return 1;
        }
        if (parseSimpleStaticDataTable(
                basePath,
                "Materials.txt",
                Tables.matShapes,
                materials,
                0x10,
                0,
                0x0B,
                false,
                Material::new,
                Material::init
        ) != 0) {
            return 1;
        }
        if (parseSimpleStaticDataTable(
                basePath,
                "Shapes.txt",
                Tables.matShapes,
                shapes,
                7,
                0,
                0x0B,
                false,
                Material::new,
                Material::init
        ) != 0) {
            return 1;
        }
        if (parseSimpleStaticDataTable(
                basePath,
                "Magic.txt",
                Tables.magic,
                magic,
                0x18,
                1,
                1,
                true,
                MagicInfo::new,
                MagicInfo::init
        ) != 0) {
            return 1;
        }
        buildMagicCumulativeWeights();
        if (parseSimpleStaticDataTable(
                basePath,
                "Weapons.txt",
                Tables.worldItems,
                weapons,
                0x14,
                1,
                1,
                true,
                WorldItem::new,
                WorldItem::init
        ) != 0) {
            return 1;
        }
        if (parseSimpleStaticDataTable(
                basePath,
                "Shields.txt",
                Tables.worldItems,
                shields,
                4,
                1,
                1,
                true,
                WorldItem::new,
                WorldItem::init
        ) != 0) {
            return 1;
        }
        if (parseSimpleStaticDataTable(
                basePath,
                "Magic Items.txt",
                Tables.magicItems,
                magicItems,
                10,
                1,
                1,
                true,
                MagicalItemInfo::new,
                MagicalItemInfo::init
        ) != 0) {
            return 1;
        }
        if (parseUnitInfoTextTable(basePath) != 0) {
            return 1;
        }
        if (parseRemStaticDataTable(
                basePath,
                "Humans.txt",
                Tables.humans,
                humans,
                5,
                1,
                1,
                HumanInfo::new,
                HumanInfo::init
        ) != 0) {
            return 1;
        }
        return parseSimpleStaticDataTable(
                basePath,
                "Buildings.txt",
                Tables.buildings,
                buildings,
                0x14,
                1,
                1,
                true,
                BuildingInfo::new,
                BuildingInfo::init
        );
    }

    /**
     * Native support extracted from CStaticDataMgr::ParseTextFiles @004F90F4 simple table-loading branches.
     */
    private <T> int parseSimpleStaticDataTable(
            Path basePath,
            String fileName,
            CustomList<String> columns,
            CustomList<T> target,
            int initialSize,
            int startIndex,
            int minimumLineLength,
            boolean grow,
            Supplier<T> factory,
            BiConsumer<T, String> initializer
    ) {
        List<String> rows = openStaticDataTextFileAndReadHeader(basePath, fileName, columns);
        if (rows == null) {
            return 1;
        }
        resetStaticDataTable(target, initialSize, factory);
        int index = startIndex;
        for (String row : rows) {
            if (!isStaticDataTextRow(row, minimumLineLength)) {
                continue;
            }
            if (grow) {
                ensureStaticDataIndex(target, index, factory);
            }
            initializer.accept(target.get(index), row);
            index++;
        }
        return 0;
    }

    /**
     * Native support extracted from CStaticDataMgr::ParseTextFiles @004F90F4 `rem` index-skip table branches.
     */
    private <T> int parseRemStaticDataTable(
            Path basePath,
            String fileName,
            CustomList<String> columns,
            CustomList<T> target,
            int initialSize,
            int startIndex,
            int minimumLineLength,
            Supplier<T> factory,
            BiConsumer<T, String> initializer
    ) {
        List<String> rows = openStaticDataTextFileAndReadHeader(basePath, fileName, columns);
        if (rows == null) {
            return 1;
        }
        resetStaticDataTable(target, initialSize, factory);
        int index = startIndex;
        for (String row : rows) {
            if (!isStaticDataTextRow(row, minimumLineLength)) {
                continue;
            }
            if (row.indexOf("rem") == 0) {
                index++;
                continue;
            }
            ensureStaticDataIndex(target, index, factory);
            initializer.accept(target.get(index), row);
            index++;
        }
        return 0;
    }

    /**
     * Native support extracted from CStaticDataMgr::ParseTextFiles @004F90F4 `Units.txt` branch.
     */
    private int parseUnitInfoTextTable(Path basePath) {
        List<String> rows = openStaticDataTextFileAndReadHeader(basePath, "Units.txt", Tables.units);
        if (rows == null) {
            return 1;
        }
        resetStaticDataTable(units, 0x40, UnitInfo::new);
        int index = 0x40;
        for (String row : rows) {
            if (!isStaticDataTextRow(row, 1)) {
                continue;
            }
            if (row.indexOf("rem") == 0) {
                index++;
                continue;
            }
            if (row.indexOf("goto") == 0) {
                index = 0x1A;
                continue;
            }
            ensureStaticDataIndex(units, index, UnitInfo::new);
            units.get(index).init(row);
            index++;
        }
        return 0;
    }

    /**
     * Native: global helper openStaticDataTextFileAndReadHeader @004F9015.
     * Fully ported.
     */
    private static List<String> openStaticDataTextFileAndReadHeader(
            Path basePath,
            String fileName,
            CustomList<String> columns
    ) {
        Path textPath = resolveNativeStaticDataTextPath(basePath, fileName);
        if (!Files.exists(textPath)) {
            Globals.gameServer.pushMessage("Error - file " + textPath + " not found");
            return null;
        }
        try {
            List<String> lines = Files.readAllLines(textPath, GameCharsets.GAME_TEXT);
            if (lines.isEmpty()) {
                columns.clear();
                return List.of();
            }
            parseColumnNamesFromHeaderLine(lines.getFirst(), columns);
            return lines.subList(1, lines.size());
        } catch (IOException e) {
            Globals.gameServer.pushMessage("Error - file " + textPath + " not found");
            return null;
        }
    }

    /**
     * Native: CStaticDataMgr::ParseColumnNamesFromHeaderLine @004FE25B.
     * Fully ported Java support for CStaticDataMgr::ParseTextFiles @004F90F4.
     */
    private static void parseColumnNamesFromHeaderLine(String headerLine, CustomList<String> columns) {
        columns.clear();
        String remaining = headerLine + '\t';
        int tabIndex = remaining.indexOf('\t');
        while (tabIndex >= 0) {
            columns.add(remaining.substring(0, tabIndex).strip());
            remaining = remaining.substring(tabIndex + 1);
            tabIndex = remaining.indexOf('\t');
        }
    }

    /**
     * Native support extracted from CStaticDataMgr::ParseTextFiles @004F90F4 content-row filters.
     */
    private static boolean isStaticDataTextRow(String row, int minimumLineLength) {
        return row.length() > minimumLineLength && row.charAt(0) != '\t';
    }

    /**
     * Native support extracted from CStaticDataMgr::ParseTextFiles @004F90F4 CArray::SetSize calls.
     */
    private static <T> void resetStaticDataTable(CustomList<T> target, int size, Supplier<T> factory) {
        target.clear();
        for (int index = 0; index < size; index++) {
            target.add(factory.get());
        }
    }

    /**
     * Native support extracted from CStaticDataMgr::ParseTextFiles @004F90F4 dynamic CArray::SetSize calls.
     */
    private static <T> void ensureStaticDataIndex(CustomList<T> target, int index, Supplier<T> factory) {
        while (target.size() <= index) {
            target.add(factory.get());
        }
    }

    /**
     * Native support extracted from CStaticDataMgr::ParseTextFiles @004F90F4 filename construction and
     * openStaticDataTextFileAndReadHeader @004F9015 CStdioFile::Open.
     */
    private static Path resolveNativeStaticDataTextPath(Path basePath, String fileName) {
        Path nativeName = basePath.resolve(fileName);
        if (Files.exists(nativeName)) {
            return nativeName;
        }

        Path lowerCaseName = basePath.resolve(fileName.toLowerCase(Locale.ROOT));
        if (Files.exists(lowerCaseName)) {
            return lowerCaseName;
        }

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(basePath)) {
            for (Path entry : entries) {
                if (entry.getFileName().toString().equalsIgnoreCase(fileName)) {
                    return entry;
                }
            }
        } catch (IOException e) {
            // Java filesystem scan failure falls through to the native @004F9015 not-found branch.
        }
        return nativeName;
    }

    /**
     * Native support extracted from CStaticDataMgr::LoadBinary @004F9FB0 and SaveBinary @004FA342.
     */
    private static Path resolveNativeDataBinPath(Path basePath) {
        Path nativeName = basePath.resolve("Data.bin");
        if (Files.exists(nativeName)) {
            return nativeName;
        }
        Path lowerCaseName = basePath.resolve(DATA + ".bin");
        if (Files.exists(lowerCaseName)) {
            return lowerCaseName;
        }
        return nativeName;
    }

    /**
     * Native support extracted from CStaticDataMgr::LoadBinary @004F9FB0 primary CFile::Open path.
     */
    private static byte[] readPrimaryStaticDataBytes(Path dataPath) {
        try {
            return Files.readAllBytes(dataPath);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Native support extracted from CStaticDataMgr::LoadBinary @004F9FB0 World.res fallback path.
     */
    private static byte[] readBundledStaticDataBytes() {
        ByteBuffer buffer = CGameFile.getDataFor(Resources.path(WORLD, DATA, DATA + ".bin"));
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    /**
     * Native support extracted from CStaticDataMgr::SaveBinary @004FA342 CFile::Open failure branch.
     */
    private static boolean canOpenNativeDataBinForWrite(Path dataPath) {
        try (var ignored = Files.newOutputStream(
                dataPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        )) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Native support extracted from CStaticDataMgr::LoadBinary @004F9FB0 Java startup resource path.
     */
    private static Path defaultStaticDataBasePath() {
        return Path.of("src/main/resources", WORLD, DATA);
    }

    /**
     * Native: CStaticDataMgr::FormatMaterialName @004FA7E1.
     * Fully ported.
     */
    public String formatMaterialName(String itemName, int materialId) {
        String formattedName = itemName;
        Material material = materials.get(materialId);
        if (material.name.contains("Leather")) {
            formattedName = "Soft " + formattedName.stripLeading();
        } else if (material.name.contains("Wood")) {
            formattedName = "Wooden " + formattedName.stripLeading();
        }
        return formattedName;
    }

    /**
     * Native: CStaticDataMgr::FindShapeID @004FA437.
     * Fully ported.
     */
    public int findShapeID(String input, StringBuilder output) {
        return findNamedMaterialId(input, output, shapes, 0);
    }

    /**
     * Native: CStaticDataMgr::FindMaterialID @004FA583.
     * Fully ported.
     */
    public int findMaterialID(String input, StringBuilder output) {
        return findNamedMaterialId(input, output, materials, 0x0F);
    }

    /**
     * Native support extracted from CStaticDataMgr::FindShapeID @004FA437 and
     * CStaticDataMgr::FindMaterialID @004FA583.
     * Fully ported.
     */
    private static int findNamedMaterialId(String input, StringBuilder output, List<Material> entries, int defaultId) {
        for (int index = entries.size() - 1; index >= 0; index--) {
            String entryName = entries.get(index).name;
            int matchOffset = input.indexOf(entryName);
            if (matchOffset == -1) {
                continue;
            }
            if (output != null) {
                output.setLength(0);
                output.append(input, 0, matchOffset);
                int rightOffset = entryName.length() + 1;
                if (rightOffset < input.length()) {
                    output.append(input.substring(rightOffset));
                }
            }
            return index & 0xFF;
        }

        output.setLength(0);
        output.append(input);
        return defaultId;
    }


    /**
     * Native: CStaticDataMgr::CStaticDataMgr @004F8E09.
     * Fully ported.
     */
    private CStaticDataMgr() {
    }

    /**
     * Native: CStaticDataMgr::buildMagicCumulativeWeights @004FF2FD.
     * Fully ported.
     */
    public void buildMagicCumulativeWeights() {
        MagicInfo firstInfo = magic.get(0);
        resizeMagicValues(firstInfo, MagicColumn.IN_SHOES.index + 1);
        for (int column = MagicColumn.IN_WEAPON_A.index; column <= MagicColumn.IN_SHOES.index; column++) {
            firstInfo.values.set(column, 0);
        }

        for (int column = MagicColumn.IN_WEAPON_A.index; column <= MagicColumn.IN_SHOES.index; column++) {
            for (int index = 1; index < magic.size(); index++) {
                MagicInfo info = magic.get(index);
                int value = info.values.get(column);
                if (value < 0) {
                    value = 0;
                    info.values.set(column, value);
                }
                info.values.set(column, value + magic.get(index - 1).values.get(column));
            }
        }
    }

    /**
     * Native support extracted from CStaticDataMgr::buildMagicCumulativeWeights @004FF2FD.
     */
    private static void resizeMagicValues(MagicInfo info, int size) {
        while (info.values.size() > size) {
            info.values.remove(info.values.size() - 1);
        }
        while (info.values.size() < size) {
            info.values.add(0);
        }
    }

    /**
     * Native: CStaticDataMgr::Serialize @004FAC89.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {

        ar.serialize(Tables.matShapes);

        ar.serialize(shapes);          // start at 0
        ar.serialize(materials);          // start at 0

        ar.serialize(Tables.magic);
        ar.serialize(magic);         // start at 0

        ar.serialize(Tables.worldItems);
        ar.serialize(armors);        // start at 1 (skip [0])
        ar.serialize(shields);        // start at 1
        ar.serialize(weapons);        // start at 1

        ar.serialize(Tables.magicItems);
        ar.serialize(magicItems);   // start at 1

        ar.serialize(Tables.units);
        ar.serialize(units);           // start at 1

        ar.serialize(Tables.humans);
        ar.serialize(humans);         // start at 1

        ar.serialize(Tables.buildings);
        ar.serialize(buildings);   // start at 1

        ar.serialize(Tables.spells);
        ar.serialize(spells);         // start at 1
    }

    /**
     * Native: CStaticDataMgr::FindUnitByServerId @004FD62F.
     * Fully ported.
     */
    public int findUnitByServerId(int serverId) {
        for (int index = units.size() - 1; index >= 1; index--) {
            UnitInfo info = units.get(index);
            if (info.values.isEmpty()) {
                continue;
            }
            if (info.getAttribute(UnitColumn.SERVER_ID) == serverId) {
                return index;
            }
        }
        return 0;
    }

    /**
     * Native: CStaticDataMgr::findUnitInfoIndexByTypeAndFace @004FA928.
     * Fully ported.
     */
    public int findUnitInfoIndexByTypeAndFace(int typeId, int faceId) {
        int normalizedTypeId = typeId & 0xFF;
        if (normalizedTypeId < 0x40 && normalizedTypeId != 0x1A && normalizedTypeId != 0x1B) {
            for (int index = 1; index < humans.size(); index++) {
                HumanInfo info = humans.get(index);
                if (!info.name.isEmpty() && info.getAttribute(HumanColumn.TYPE_ID) == normalizedTypeId) {
                    return index;
                }
            }
        } else {
            int normalizedFaceId = faceId & 0xFF;
            for (int index = 1; index < units.size(); index++) {
                UnitInfo info = units.get(index);
                if (!info.name.isEmpty()
                        && info.getAttribute(UnitColumn.TYPE_ID) == normalizedTypeId
                        && info.getAttribute(UnitColumn.FACE) == normalizedFaceId) {
                    return index;
                }
            }
        }
        return 0;
    }

    /**
     * Native: CStaticDataMgr::findInnQuestUnitInfoIndexByTypeAndFace @004FD6B1.
     * Fully ported.
     */
    public int findInnQuestUnitInfoIndexByTypeAndFace(int typeId, int faceId) {
        for (int index = units.size() - 1; index >= 1; index--) {
            UnitInfo info = units.get(index);
            if (info.values.isEmpty()) {
                continue;
            }
            if (info.getAttribute(UnitColumn.TYPE_ID) == typeId
                    && info.getAttribute(UnitColumn.FACE) == faceId) {
                return index;
            }
        }
        return 0;
    }

    /**
     * Native: CStaticDataMgr::findInnRewardUnitInfoIndex @004FAA9C.
     * Fully ported.
     */
    public int findInnRewardUnitInfoIndex(int maxXpValue) {
        int selectedIndex = 0;
        int selectedXpValue = 1;
        for (int index = 1; index < units.size(); index++) {
            UnitInfo info = units.get(index);
            if (info.name.isEmpty()) {
                continue;
            }
            int typeId = info.getAttribute(UnitColumn.TYPE_ID);
            int face = info.getAttribute(UnitColumn.FACE);
            if (isInnRewardUnitInfoEligible(info, typeId, face, selectedXpValue, maxXpValue)) {
                selectedIndex = index;
                selectedXpValue = info.getAttribute(UnitColumn.XP_VALUE);
            }
        }
        return selectedIndex;
    }

    /**
     * Native support extracted from CStaticDataMgr::findInnRewardUnitInfoIndex @004FAA9C unit filters.
     */
    private static boolean isInnRewardUnitInfoEligible(
            UnitInfo info,
            int typeId,
            int face,
            int selectedXpValue,
            int maxXpValue
    ) {
        int xpValue = info.getAttribute(UnitColumn.XP_VALUE);
        return 0x3F < typeId
                && typeId < 99
                && (typeId != 0x45 || face != 1)
                && typeId != 0x52
                && typeId != 0x59
                && !info.name.contains("_5")
                && xpValue <= maxXpValue
                && selectedXpValue < xpValue;
    }

    /**
     * Native: CStaticDataMgr::FindHumanByServerId @004FD5AD.
     * Fully ported.
     */
    public int findHumanByServerId(int serverId) {
        for (int index = humans.size() - 1; index >= 1; index--) {
            HumanInfo info = humans.get(index);
            if (info.values.isEmpty()) {
                continue;
            }
            if (info.getValue(HumanColumn.SERVER_ID.index) == serverId) {
                return index;
            }
        }
        return 0;
    }

    /**
     * Native: CStaticDataMgr::FindHumanInfoIndexByName @0053EBE0.
     * Fully ported.
     */
    public int findHumanInfoIndexByName(String humanName) {
        for (int index = humans.size() - 1; index >= 1; index--) {
            HumanInfo info = humans.get(index);
            if (humanName.equals(info.name)) {
                return index;
            }
        }
        return 0;
    }

    /**
     * Native: CStaticDataMgr::createItemFromPackedHash @004FBF01.
     * Fully ported.
     */
    public Item createItemFromPackedHash(int packedHash) {
        int slot = (packedHash >>> 8) & 0x0F;
        int shapeId = (packedHash >>> 5) & 0x07;
        int materialId = (packedHash >>> 12) & 0x0F;
        int itemIndex = slot == 0x0E ? packedHash & 0xFF : packedHash & 0x1F;
        return createItemFromNativeIds(slot, shapeId, materialId, itemIndex);
    }

    /**
     * Native: CStaticDataMgr::createItemFromNativeIds @004FBF99.
     * Fully ported.
     */
    public Item createItemFromNativeIds(int slot, int shapeId, int materialId, int itemIndex) {
        return switch (slot) {
            case 1 -> new Weapon(shapeId, materialId, itemIndex);
            case 2 -> new Shield(shapeId, materialId, itemIndex);
            case 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 -> Armor.createFromNativeIds(shapeId, materialId, itemIndex);
            case 0xE -> new MagicItem(slot, itemIndex);
            default -> null;
        };
    }

    @Override
    // not ported.
    public String toString() {
        return "CStaticDataMgr{" +
                "materials=" + materials +
                ", shapes=" + shapes +
                ", shields=" + shields +
                ", armors=" + armors +
                ", weapons=" + weapons +
                ", magicItems=" + magicItems +
                ", magic=" + magic +
                ", units=" + units +
                ", humans=" + humans +
                ", buildings=" + buildings +
                ", spells=" + spells +
                '}';
    }

    public static final class Tables {
        public static final CustomList<String> matShapes = new CustomList<>(String.class);
        public static final CustomList<String> magic = new CustomList<>(String.class);
        public static final CustomList<String> worldItems = new CustomList<>(String.class);
        public static final CustomList<String> magicItems = new CustomList<>(String.class);
        public static final CustomList<String> units = new CustomList<>(String.class);
        public static final CustomList<String> humans = new CustomList<>(String.class);
        public static final CustomList<String> buildings = new CustomList<>(String.class);
        public static final CustomList<String> spells = new CustomList<>(String.class);
    }
}
