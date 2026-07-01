package ua.millfreedom.rom2.model;

import lombok.SneakyThrows;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.res.ResInHeap;

import java.util.ArrayList;
import java.util.List;

import static ua.millfreedom.rom2.res.Constants.GRAPHICS;

/**
 * Java support loader for the native structure definition registry.
 */
public final class Structures {
    private static final String STRUCTURES = "structures";
    private static final String STRUCTURES_REG = "structures.reg";
    private static final String GLOBAL = "Global";
    private static final String COUNT = "Count";
    private static final String STRUCTURE_KEY = "Structure%d";
    private static final String FILE = "File";
    private static final String ID = "ID";
    private static final String TILE_WIDTH = "TileWidth";
    private static final String TILE_HEIGHT = "TileHeight";
    private static final String FULL_HEIGHT = "FullHeight";
    private static final String PHASES = "Phases";
    private static final String SELECTION_X1 = "SelectionX1";
    private static final String SELECTION_Y1 = "SelectionY1";
    private static final String SELECTION_X2 = "SelectionX2";
    private static final String SELECTION_Y2 = "SelectionY2";
    private static final String SHADOW_Y = "ShadowY";
    private static final String PICTURE = "Picture";
    private static final String INDESTRUCTIBLE = "Indestructible";
    private static final String DESC_TEXT = "DescText";
    private static final String VARIABLE_SIZE = "VariableSize";
    private static final String USABLE = "Usable";
    private static final String LIGHT_RADIUS = "LightRadius";
    private static final String LIGHT_PULSE = "LightPulse";
    private static final String FLAT = "Flat";
    private static final String ANIM_TIME = "AnimTime";
    private static final String ANIM_FRAME = "AnimFrame";
    private static final String ANIM_MASK = "AnimMask";

    /**
     * Native global: {@code g_StructureDefs @00622930}. Static CArray lifetime glue:
     * initializer @004763EA, constructor @004763F9, atexit wrapper @00476408, destructor @0047641A.
     */
    public static final CustomList<StructureDef> STRUCTURE_DEFS = new CustomList<>(StructureDef.class);

    /**
     * not ported.
     */
    private Structures() {
    }

    /**
     * Native: LoadStructures @0047D8AF.
     * Full port of registry-visible loader behavior. Native reloads entries by structure ID without clearing the
     * existing CArray; cleanupStructures owns structure registry clearing.
     */
    @SneakyThrows
    public static void loadStructures() {
        ResInHeap structuresReg = ResInHeap.load(GRAPHICS, STRUCTURES, STRUCTURES_REG);

        int count = structuresReg.getInt(GLOBAL, COUNT, 0);
        for (int index = 0; index < count; index++) {
            Globals.mousePointer.update();
            String section = STRUCTURE_KEY.formatted(index);
            StructureDef structure = new StructureDef(getStringValue(structuresReg, section, FILE));
            structure.id = structuresReg.getInt(section, ID, -1);
            structure.tileWidth = structuresReg.getInt(section, TILE_WIDTH, -1);
            structure.tileHeight = structuresReg.getInt(section, TILE_HEIGHT, -1);
            structure.fullHeight = structuresReg.getInt(section, FULL_HEIGHT, -1);
            structure.phases = structuresReg.getInt(section, PHASES, -1);
            structure.selection.left = structuresReg.getInt(section, SELECTION_X1, -1);
            structure.selection.top = structuresReg.getInt(section, SELECTION_Y1, -1);
            structure.selection.right = structuresReg.getInt(section, SELECTION_X2, -1);
            structure.selection.bottom = structuresReg.getInt(section, SELECTION_Y2, -1);
            structure.shadowY = structuresReg.getInt(section, SHADOW_Y, 0);
            structure.animationMask = null;
            structure.animationDataCount = 0;
            structure.animationMaskSolidCount = 0;
            structure.picture = getStringValue(structuresReg, section, PICTURE, 0x10);
            structure.indestructible = structuresReg.getInt(section, INDESTRUCTIBLE, 0);
            structure.descriptionText = getStringValue(structuresReg, section, DESC_TEXT, 0x20);
            structure.variableSize = structuresReg.getInt(section, VARIABLE_SIZE, 0);
            structure.usable = structuresReg.getInt(section, USABLE, 0);
            structure.lightRadius = structuresReg.getInt(section, LIGHT_RADIUS, 0);
            structure.lightPulse = structuresReg.getInt(section, LIGHT_PULSE, 0);
            structure.flat = structuresReg.getInt(section, FLAT, 0);

            if (structure.phases > 1) {
                loadExpandedAnimation(structuresReg, section, structure.animationData);
                structure.animationDataCount = structure.animationData.size();
                structure.animationMask = getStringValue(
                        structuresReg,
                        section,
                        ANIM_MASK,
                        structure.tileWidth * structure.fullHeight + 1
                );
                structure.animationMaskSolidCount = countAnimationMaskSolidCells(structure.animationMask);
            }
            setStructureDefById(structure);
        }
    }

    /**
     * Native: cleanupStructures @0047DEB4.
     * Fully ported. Java clears the managed structure registry after detaching loaded sprite references.
     */
    public static void cleanupStructures() {
        for (StructureDef structureDef : STRUCTURE_DEFS) {
            if (structureDef != null) {
                structureDef.unloadForStructureRegistryCleanup();
            }
        }
        STRUCTURE_DEFS.clear();
    }

    /**
     * Native support extracted from `CArray<StructureDef>::GetAt(&g_StructureDefs, id)` uses around LoadStructures
     *
     * @0047D8AF.
     */
    public static StructureDef getStructureDef(int structureId) {
        if (structureId < 0 || structureId >= STRUCTURE_DEFS.size()) {
            return null;
        }
        return STRUCTURE_DEFS.get(structureId);
    }

    /**
     * Native support extracted from `CArray<StructureDef>::SetAtGrow(&g_StructureDefs, id, def)` in LoadStructures
     *
     * @0047D8AF.
     */
    private static void setStructureDefById(StructureDef structure) {
        if (structure.id < 0) {
            throw new IllegalStateException("LoadStructures encountered structure without native ID");
        }
        while (STRUCTURE_DEFS.size() <= structure.id) {
            STRUCTURE_DEFS.add(null);
        }
        STRUCTURE_DEFS.set(structure.id, structure);
    }

    /**
     * Native support extracted from `ResInHeap::GetValueAsString` calls in LoadStructures @0047D8AF.
     */
    private static String getStringValue(ResInHeap res, String section, String key) {
        return getStringValue(res, section, key, 0x100);
    }

    /**
     * Native support extracted from `ResInHeap::GetValueAsString` calls in LoadStructures @0047D8AF.
     */
    private static String getStringValue(ResInHeap res, String section, String key, int destSize) {
        StringBuilder value = new StringBuilder(destSize);
        res.getValueAsString(section, key, "", value, destSize);
        return value.toString();
    }

    /**
     * Native support extracted from the animation expansion loop in LoadStructures @0047D8AF.
     */
    private static void loadExpandedAnimation(ResInHeap res, String section, List<Integer> dest) {
        List<Integer> animationTimes = new ArrayList<>();
        List<Integer> animationFrames = new ArrayList<>();
        res.getIntArray(section, ANIM_TIME, animationTimes);
        res.getIntArray(section, ANIM_FRAME, animationFrames);
        dest.clear();
        while (!animationTimes.isEmpty() && !animationFrames.isEmpty()) {
            int repeatCount = animationTimes.getFirst();
            int frame = animationFrames.getFirst();
            for (int i = 0; i < repeatCount; i++) {
                dest.add(frame);
            }
            animationTimes.removeFirst();
            animationFrames.removeFirst();
        }
    }

    /**
     * Native support extracted from the `m_strAnimMask[animIdx] != '-'` count loop in LoadStructures @0047D8AF.
     */
    private static int countAnimationMaskSolidCells(String animationMask) {
        int count = 0;
        for (int i = 0; i < animationMask.length(); i++) {
            if (animationMask.charAt(i) != '-') {
                count++;
            }
        }
        return count;
    }
}
