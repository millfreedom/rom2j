package ua.millfreedom.rom2.model;

import lombok.SneakyThrows;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.palette.CGamePalette;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.palette.Palette256;
import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.res.ResInHeap;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

import static ua.millfreedom.rom2.res.Constants.GRAPHICS;

/**
 * Java support loader for the native `g_UnitTypes` / `g_CArray<GraphicsUnitsFile>` runtime.
 */
public final class UnitTypes {
    private static final String UNITS = "units";
    private static final String UNITS_REG = "units.reg";
    private static final String GLOBAL = "Global";
    private static final String FILES = "Files";
    private static final String FILE_COUNT = "FileCount";
    private static final String UNIT_COUNT = "UnitCount";
    private static final String FILE_KEY = "File%d";
    private static final String UNIT_KEY = "Unit%d";
    private static final String INDEX = "Index";
    private static final String PARENT = "Parent";
    private static final String FILE = "File";
    private static final String MOVE_PHASES = "MovePhases";
    private static final String MOVE_BEGIN_PHASES = "MoveBeginPhases";
    private static final String ATTACK_PHASES = "AttackPhases";
    private static final String DYING_PHASES = "DyingPhases";
    private static final String BONE_PHASES = "BonePhases";
    private static final String IDLE_PHASES = "IdlePhases";
    private static final String WIDTH = "Width";
    private static final String HEIGHT = "Height";
    private static final String CENTER_X = "CenterX";
    private static final String CENTER_Y = "CenterY";
    private static final String SELECTION_X1 = "SelectionX1";
    private static final String SELECTION_X2 = "SelectionX2";
    private static final String SELECTION_Y1 = "SelectionY1";
    private static final String SELECTION_Y2 = "SelectionY2";
    private static final String ATTACK_ANIM_TIME = "AttackAnimTime";
    private static final String ATTACK_ANIM_FRAME = "AttackAnimFrame";
    private static final String MOVE_ANIM_TIME = "MoveAnimTime";
    private static final String MOVE_ANIM_FRAME = "MoveAnimFrame";
    private static final String DYING = "Dying";
    private static final String PALETTE = "Palette";
    private static final String SOUND = "Sound";
    private static final String TILE_SIZE = "TileSize";
    private static final String PROJECTILE = "Projectile";
    private static final String INFO_PICTURE = "InfoPicture";
    private static final String SHOOT_OFFSET = "ShootOffset";
    private static final String SHOOT_DELAY = "ShootDelay";
    private static final String ATTACK_DELAY = "AttackDelay";
    private static final String DESC_TEXT = "DescText";
    private static final String IDLE_ANIM_TIME = "IdleAnimTime";
    private static final String IDLE_ANIM_FRAME = "IdleAnimFrame";
    private static final String FLIP = "Flip";
    private static final String Z = "Z";
    private static final String ID = "ID";
    private static final String[] LOCAL_PALETTE_NAMES = {
            "palette.pal",
            "palette2.pal",
            "palette3.pal",
            "palette4.pal",
            "palette5.pal",
            "palette6.pal",
            "palette7.pal",
            "palette8.pal"
    };

    /**
     * Native global: {@code g_CArray<GraphicsUnitsFile> @006269B0}. Static CArray lifetime glue:
     * initializer @004762EE, constructor @004762FD, atexit wrapper @0047630C, destructor @0047631E.
     */
    public static final CustomList<GraphicsUnitsFile> GRAPHICS_UNITS_FILES = new CustomList<>(GraphicsUnitsFile.class);
    /**
     * Native global: {@code g_UnitTypes @00622978}. Static CArray lifetime glue:
     * initializer @0047632D, constructor @0047633C, atexit wrapper @0047634B, destructor @0047635D.
     */
    public static final CustomList<CUnitInfo> UNIT_TYPES_BY_ID = new CustomList<>(CUnitInfo.class);

    static {
        loadUnitTypes();
    }

    // not ported.
    private UnitTypes() {
    }

    /**
     * Native: UnitTypes::loadUnitTypes @00479B1E.
     * Recovered Java/Ghidra grouping owner for the global `LoadUnitRegistry` routine.
     * Java port status: fully ported.
     * Full port of registry-visible loader behavior. Java keeps resource/file allocation inside managed helpers, while
     * preserving native file-list loading, per-unit mouse updates, parent inheritance, fixed string buffers, geometry,
     * local palettes, animation expansion tables, delays, and the trailing shared `human.pal` bootstrap.
     */
    @SneakyThrows
    public static void loadUnitTypes() {
        ResInHeap unitsReg = ResInHeap.load(GRAPHICS, UNITS, UNITS_REG);

        GRAPHICS_UNITS_FILES.clear();
        int fileCount = unitsReg.getInt(GLOBAL, FILE_COUNT, 0);
        for (int fileIndex = 0; fileIndex < fileCount; fileIndex++) {
            String relativePath = getStringValue(unitsReg, FILES, FILE_KEY.formatted(fileIndex));
            GRAPHICS_UNITS_FILES.add(new GraphicsUnitsFile(relativePath));
        }

        UNIT_TYPES_BY_ID.clear();
        int unitCount = unitsReg.getInt(GLOBAL, UNIT_COUNT, 0);
        for (int unitSectionIndex = 0; unitSectionIndex < unitCount; unitSectionIndex++) {
            Globals.mousePointer.update();
            String section = UNIT_KEY.formatted(unitSectionIndex);
            CUnitInfo info = new CUnitInfo();
            info.m_ID = unitsReg.getInt(section, ID, -1);
            info.m_ParentID = unitSectionIndex;

            int parentId = unitsReg.getInt(section, PARENT, -1);
            CUnitInfo parentInfo = parentId == -1 ? null : getUnitInfo(parentId);
            String parentSection = parentId == -1 ? null : UNIT_KEY.formatted(parentInfo.m_ParentID);

            info.m_FileID = getInheritedInt(unitsReg, section, FILE, parentInfo, -1, value -> value.m_FileID);
            info.m_SpriteIndex = getInheritedInt(unitsReg, section, INDEX, parentInfo, -1, value -> value.m_SpriteIndex);
            info.m_MovePhases = getInheritedInt(unitsReg, section, MOVE_PHASES, parentInfo, -1, value -> value.m_MovePhases);
            info.m_MoveBeginPhases = getInheritedInt(unitsReg, section, MOVE_BEGIN_PHASES, parentInfo, -1, value -> value.m_MoveBeginPhases);
            info.m_AttackPhases = getInheritedInt(unitsReg, section, ATTACK_PHASES, parentInfo, -1, value -> value.m_AttackPhases);
            info.m_DyingPhases = getInheritedInt(unitsReg, section, DYING_PHASES, parentInfo, -1, value -> value.m_DyingPhases);
            info.m_BonePhases = getInheritedInt(unitsReg, section, BONE_PHASES, parentInfo, -1, value -> value.m_BonePhases);
            info.m_IdlePhases = getInheritedInt(unitsReg, section, IDLE_PHASES, parentInfo, 0, value -> value.m_IdlePhases);
            info.m_Width = getInheritedInt(unitsReg, section, WIDTH, parentInfo, -1, value -> value.m_Width);
            info.m_Height = getInheritedInt(unitsReg, section, HEIGHT, parentInfo, -1, value -> value.m_Height);
            info.m_CenterX = getInheritedInt(unitsReg, section, CENTER_X, parentInfo, -1, value -> value.m_CenterX);
            info.m_CenterY = getInheritedInt(unitsReg, section, CENTER_Y, parentInfo, -1, value -> value.m_CenterY);
            info.m_SelectionRect.left = getInheritedInt(unitsReg, section, SELECTION_X1, parentInfo, -1, value -> value.m_SelectionRect.left);
            info.m_SelectionRect.top = getInheritedInt(unitsReg, section, SELECTION_Y1, parentInfo, -1, value -> value.m_SelectionRect.top);
            info.m_SelectionRect.right = getInheritedInt(unitsReg, section, SELECTION_X2, parentInfo, -1, value -> value.m_SelectionRect.right);
            info.m_SelectionRect.bottom = getInheritedInt(unitsReg, section, SELECTION_Y2, parentInfo, -1, value -> value.m_SelectionRect.bottom);
            info.m_bDying = getInheritedInt(unitsReg, section, DYING, parentInfo, 0, value -> value.m_bDying);
            info.m_PaletteIndex = getInheritedInt(unitsReg, section, PALETTE, parentInfo, 0, value -> value.m_PaletteIndex);
            info.m_TileSize = getInheritedInt(unitsReg, section, TILE_SIZE, parentInfo, 1, value -> value.m_TileSize);
            info.m_ProjectileID = getInheritedInt(unitsReg, section, PROJECTILE, parentInfo, 0, value -> value.m_ProjectileID);
            info.m_ShootDelay = getInheritedInt(unitsReg, section, SHOOT_DELAY, parentInfo, 0, value -> value.m_ShootDelay);
            info.m_AttackDelay = getInheritedInt(unitsReg, section, ATTACK_DELAY, parentInfo, 0, value -> value.m_AttackDelay);
            info.m_ZOffset = getInheritedInt(unitsReg, section, Z, parentInfo, 0, value -> value.m_ZOffset);
            info.m_Flip = getInheritedInt(unitsReg, section, FLIP, parentInfo, 0, value -> value.m_Flip);

            loadInheritedIntArray(unitsReg, section, parentSection, SOUND, info.m_SoundIDs);
            loadInheritedIntArray(unitsReg, section, parentSection, SHOOT_OFFSET, info.m_ShootOffsets);
            loadExpandedAnim(unitsReg, section, parentSection, MOVE_ANIM_TIME, MOVE_ANIM_FRAME, info.m_MoveFrameSequence);
            loadExpandedAnim(unitsReg, section, parentSection, IDLE_ANIM_TIME, IDLE_ANIM_FRAME, info.m_IdleFrameSequence);
            loadExpandedAnim(unitsReg, section, parentSection, ATTACK_ANIM_TIME, ATTACK_ANIM_FRAME, info.m_AttackFrameSequence);

            info.m_DescText = getStringValue(unitsReg, section, DESC_TEXT, 0x20);
            info.m_InfoPicture = getInheritedInfoPicture(unitsReg, section, parentSection);
            info.m_MoveFrameSequenceCount = info.m_MoveFrameSequence.size();
            info.m_IdleFrameSequenceCount = info.m_IdleFrameSequence.size();
            info.m_AttackFrameSequenceCount = info.m_AttackFrameSequence.size();

            loadLocalPalettes(info);
            setUnitInfoById(info);
        }
        Palettes.loadUnitOwnerPalettes();
    }

    /**
     * Native support extracted from the GraphicsUnitsFile::Unload @0047B374 loop in
     * CMainWindow::cleanupGameplayResources @0048DA92.
     */
    public static void unloadGraphicsUnitsFilesForGameplayCleanup() {
        for (GraphicsUnitsFile graphicsUnitsFile : GRAPHICS_UNITS_FILES) {
            if (graphicsUnitsFile != null) {
                graphicsUnitsFile.unload();
            }
        }
    }

    /**
     * Native: Global::releaseUnitTypeRuntimeGlobals @0047AF9D.
     * Fully ported. Native scalar-deletes entries in g_CArray<GraphicsUnitsFile>, g_UnitTypes, and
     * GamePaletteArray_units, then clears those arrays; Java unloads/detaches the managed equivalents.
     */
    public static void releaseUnitTypeRuntimeGlobals() {
        unloadGraphicsUnitsFilesForGameplayCleanup();
        GRAPHICS_UNITS_FILES.clear();
        UNIT_TYPES_BY_ID.clear();
        Palettes.releaseUnitOwnerGamePalettesForShutdown();
    }

    /**
     * Native support extracted from `CArray<CUnitInfo>::ElementAt(&g_UnitTypes, type)` in CUnit::Draw @004632A1,
     * CUnit::DrawShadow @00464487, CUnit::AdvanceMapObjectState @0046617C, and UnitTypes::loadUnitTypes @00479B1E.
     */
    public static CUnitInfo getUnitInfo(int unitTypeId) {
        return UNIT_TYPES_BY_ID.get(unitTypeId);
    }

    /**
     * Native support extracted from `CArray<GraphicsUnitsFile>::GetAt(..., fileId)` in FUN_0046DFF0 @0046DFF0,
     * FUN_0046E020 @0046E020, and UnitTypes::loadUnitTypes @00479B1E.
     */
    public static GraphicsUnitsFile getGraphicsUnitsFile(int fileId) {
        return GRAPHICS_UNITS_FILES.get(fileId);
    }

    /**
     * Native support extracted from `CArray<CUnitInfo>::SetAtGrowth(&g_UnitTypes, pCUnitInfo->m_ID, pCUnitInfo)` in
     * UnitTypes::loadUnitTypes @00479B1E.
     */
    private static void setUnitInfoById(CUnitInfo info) {
        if (info.m_ID < 0) {
            throw new IllegalStateException("UnitTypes::loadUnitTypes encountered unit without native ID");
        }
        while (UNIT_TYPES_BY_ID.size() <= info.m_ID) {
            UNIT_TYPES_BY_ID.add(null);
        }
        UNIT_TYPES_BY_ID.set(info.m_ID, info);
    }

    /**
     * Native support extracted from `ResInHeap::GetInt(..., default)` branches in UnitTypes::loadUnitTypes @00479B1E.
     */
    private static int getInheritedInt(
            ResInHeap res,
            String section,
            String key,
            CUnitInfo parentInfo,
            int rootDefault,
            ToIntFunction<CUnitInfo> inheritedValue
    ) {
        int defaultValue = parentInfo == null ? rootDefault : inheritedValue.applyAsInt(parentInfo);
        return res.getInt(section, key, defaultValue);
    }

    /**
     * Native support extracted from `ResInHeap::GetValueAsString` calls in UnitTypes::loadUnitTypes @00479B1E.
     */
    private static String getStringValue(ResInHeap res, String section, String key) {
        return getStringValue(res, section, key, 0x100);
    }

    /**
     * Native support extracted from fixed-size `ResInHeap::GetValueAsString` destination buffers in
     * UnitTypes::loadUnitTypes @00479B1E.
     */
    private static String getStringValue(ResInHeap res, String section, String key, int destSize) {
        StringBuilder value = new StringBuilder(0x100);
        res.getValueAsString(section, key, "", value, destSize);
        return value.toString();
    }

    /**
     * Native support extracted from the inherited `InfoPicture` fallback branch in UnitTypes::loadUnitTypes @00479B1E.
     */
    private static String getInheritedInfoPicture(ResInHeap res, String section, String parentSection) {
        String value = getStringValue(res, section, INFO_PICTURE, 0x10);
        if (value.isEmpty() && parentSection != null) {
            return getStringValue(res, parentSection, INFO_PICTURE, 0x10);
        }
        return value;
    }

    /**
     * Native support extracted from `ResInHeap::GetIntArray` inherited-array branches in UnitTypes::loadUnitTypes @00479B1E.
     */
    private static void loadInheritedIntArray(
            ResInHeap res,
            String section,
            String parentSection,
            String key,
            List<Integer> dest
    ) {
        dest.clear();
        res.getIntArray(section, key, dest);
        if (dest.isEmpty() && parentSection != null) {
            res.getIntArray(parentSection, key, dest);
        }
    }

    /**
     * Native support extracted from the move/idle/attack animation expansion loops in UnitTypes::loadUnitTypes @00479B1E,
     * including the front-pop order at 0047A9F7, 0047AB41, and 0047AC8E.
     */
    private static void loadExpandedAnim(
            ResInHeap res,
            String section,
            String parentSection,
            String timeKey,
            String frameKey,
            List<Integer> dest
    ) {
        List<Integer> animTimes = new ArrayList<>();
        List<Integer> animFrames = new ArrayList<>();
        loadInheritedIntArray(res, section, parentSection, timeKey, animTimes);
        loadInheritedIntArray(res, section, parentSection, frameKey, animFrames);
        dest.clear();
        while (!animTimes.isEmpty() && !animFrames.isEmpty()) {
            int repeatCount = animTimes.getFirst();
            int frame = animFrames.getFirst();
            for (int i = 0; i < repeatCount; i++) {
                dest.add(frame);
            }
            animTimes.removeFirst();
            animFrames.removeFirst();
        }
    }

    /**
     * Native support extracted from local `CUnitInfo::m_Palettes` / `m_PaletteRawData` population in
     * UnitTypes::loadUnitTypes @00479B1E. This is the per-unit `palIDX < m_PaletteIndex` loop, not the later once-only
     * `human_pal` bootstrap that runs after the unit loop exits.
     */
    private static void loadLocalPalettes(CUnitInfo info) {
        GraphicsUnitsFile graphicsFile = getGraphicsUnitsFile(info.m_FileID);
        int lastSlash = graphicsFile.path.lastIndexOf('/');
        String directory = lastSlash >= 0 ? graphicsFile.path.substring(0, lastSlash + 1) : graphicsFile.path;
        for (int paletteIndex = 0; paletteIndex < info.m_PaletteIndex; paletteIndex++) {
            String palettePath = directory + LOCAL_PALETTE_NAMES[paletteIndex];
            Palette256 palette256 = loadPalette256(palettePath);
            info.m_PaletteRawData[paletteIndex] = palette256;
            info.m_Palettes[paletteIndex] = new CGamePalette();
        }
    }

    /**
     * Native support extracted from the palette-file read branch in UnitTypes::loadUnitTypes @00479B1E.
     */
    private static Palette256 loadPalette256(String path) {
        ByteBuffer buffer = Globals.gameFileManager.get(path).slice(0x36, 0x400).order(ByteOrder.LITTLE_ENDIAN);
        return Palette256.read(buffer);
    }
}
