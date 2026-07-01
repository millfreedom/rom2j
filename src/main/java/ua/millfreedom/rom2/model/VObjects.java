package ua.millfreedom.rom2.model;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.res.ResInHeap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

import static ua.millfreedom.rom2.res.Constants.GRAPHICS;

/**
 * Java support loader for the native visual object file/object registries.
 */
@Slf4j
public final class VObjects {
    private static final String OBJECTS = "objects";
    private static final String OBJECTS_REG = "objects.reg";
    private static final String GLOBAL = "Global";
    private static final String FILES = "Files";
    private static final String FILE_COUNT = "FileCount";
    private static final String OBJECT_COUNT = "ObjectCount";
    private static final String FILE_KEY = "File%d";
    private static final String OBJECT_KEY = "Object%d";
    private static final String ID = "ID";
    private static final String PARENT = "Parent";
    private static final String FILE = "File";
    private static final String INDEX = "Index";
    private static final String PHASES = "Phases";
    private static final String WIDTH = "Width";
    private static final String HEIGHT = "Height";
    private static final String CENTER_X = "CenterX";
    private static final String CENTER_Y = "CenterY";
    private static final String FIRE_OBJECT = "FireObject";
    private static final String DEAD_OBJECT = "DeadObject";
    private static final String IN_MAP_EDITOR = "InMapEditor";
    private static final String DESC_TEXT = "DescText";
    private static final String ANIMATION_TIME = "AnimationTime";
    private static final String ANIMATION_FRAME = "AnimationFrame";

    /**
     * Native global: {@code g_GraphicsObjectsFiles @00622998}. Static CArray lifetime glue:
     * initializer @00476270, constructor @0047627F, atexit wrapper @0047628E, destructor @004762A0.
     */
    public static final CustomList<GraphicsObjectsFile> GRAPHICS_OBJECTS_FILES = new CustomList<>(GraphicsObjectsFile.class);
    /**
     * Native global: {@code g_VObjects @00626BC8}. Static CArray lifetime glue:
     * initializer @004762AF, constructor @004762BE, atexit wrapper @004762CD, destructor @004762DF.
     */
    public static final CustomList<VObject> V_OBJECTS = new CustomList<>(VObject.class);

    /**
     * not ported.
     */
    private VObjects() {
    }

    /**
     * Native: LoadVObjects @00478EB6.
     * Java port status: fully ported.
     */
    @SneakyThrows
    public static void loadVObjects() {
        ResInHeap objectsReg = ResInHeap.load(GRAPHICS, OBJECTS, OBJECTS_REG);

        GRAPHICS_OBJECTS_FILES.clear();
        int fileCount = objectsReg.getInt(GLOBAL, FILE_COUNT, 0);
        for (int fileIndex = 0; fileIndex < fileCount; fileIndex++) {
            String relativePath = getStringValue(objectsReg, FILES, FILE_KEY.formatted(fileIndex));
            GRAPHICS_OBJECTS_FILES.add(new GraphicsObjectsFile(relativePath));
        }

        V_OBJECTS.clear();
        int objectCount = objectsReg.getInt(GLOBAL, OBJECT_COUNT, 0);
        for (int objectIndex = 0; objectIndex < objectCount; objectIndex++) {
            Globals.mousePointer.update();
            String section = OBJECT_KEY.formatted(objectIndex);
            VObject object = new VObject();
            object.id = objectsReg.getInt(section, ID, -1);
            object.registryIndex = objectIndex;

            int parentId = objectsReg.getInt(section, PARENT, -1);
            VObject parent = parentId == -1 ? null : V_OBJECTS.get(parentId);
            String parentSection = parentId == -1 ? null : OBJECT_KEY.formatted(parent.registryIndex);

            object.fileId = objectsReg.getInt(section, FILE, -1);
            object.spriteIndex = getInheritedInt(objectsReg, section, INDEX, parent, -1, value -> value.spriteIndex);
            object.phases = getInheritedInt(objectsReg, section, PHASES, parent, -1, value -> value.phases);
            object.width = getInheritedInt(objectsReg, section, WIDTH, parent, -1, value -> value.width);
            object.height = getInheritedInt(objectsReg, section, HEIGHT, parent, -1, value -> value.height);
            object.centerX = getInheritedInt(objectsReg, section, CENTER_X, parent, -1, value -> value.centerX);
            object.centerY = getInheritedInt(objectsReg, section, CENTER_Y, parent, -1, value -> value.centerY);
            object.fireObjectId = getInheritedInt(objectsReg, section, FIRE_OBJECT, parent, -1, value -> value.fireObjectId);
            object.deadObjectId = getInheritedInt(objectsReg, section, DEAD_OBJECT, parent, -1, value -> value.deadObjectId);
            object.inMapEditor = objectsReg.getInt(section, IN_MAP_EDITOR, 0);
            object.descriptionText = getStringValue(objectsReg, section, DESC_TEXT, 0x1F);

            loadExpandedAnimation(objectsReg, section, parentSection, object.animationFrames);
            object.animationFrameCount = object.animationFrames.size();
            V_OBJECTS.add(object);
        }
        log.debug("done");
    }

    /**
     * Native: cleanupVObjects @004795F8.
     * Fully ported. Java clears the managed object/file registries after detaching loaded sprite references.
     */
    public static void cleanupVObjects() {
        for (GraphicsObjectsFile graphicsObjectsFile : GRAPHICS_OBJECTS_FILES) {
            if (graphicsObjectsFile != null) {
                graphicsObjectsFile.freeResources();
            }
        }
        GRAPHICS_OBJECTS_FILES.clear();
        V_OBJECTS.clear();
    }

    /**
     * Native support extracted from `CArray<VObject>::GetAt(&g_VObjects, id)` in LoadVObjects @00478EB6.
     */
    public static VObject getVObject(int objectId) {
        return V_OBJECTS.get(objectId);
    }

    /**
     * Native support extracted from `CArray<GraphicsObjectsFile>::Add(&g_GraphicsObjectsFiles, file)` in LoadVObjects
     *
     * @00478EB6.
     */
    public static GraphicsObjectsFile getGraphicsObjectsFile(int fileId) {
        return GRAPHICS_OBJECTS_FILES.get(fileId);
    }

    /**
     * Native support extracted from `ResInHeap::GetInt(..., default)` branches in LoadVObjects @00478EB6.
     */
    private static int getInheritedInt(
            ResInHeap res,
            String section,
            String key,
            VObject parent,
            int rootDefault,
            ToIntFunction<VObject> inheritedValue
    ) {
        int defaultValue = parent == null ? rootDefault : inheritedValue.applyAsInt(parent);
        return res.getInt(section, key, defaultValue);
    }

    /**
     * Native support extracted from `ResInHeap::GetValueAsString` calls in LoadVObjects @00478EB6.
     */
    private static String getStringValue(ResInHeap res, String section, String key) {
        return getStringValue(res, section, key, 0x100);
    }

    /**
     * Native support extracted from `ResInHeap::GetValueAsString` calls in LoadVObjects @00478EB6.
     */
    private static String getStringValue(ResInHeap res, String section, String key, int destSize) {
        StringBuilder value = new StringBuilder(destSize);
        res.getValueAsString(section, key, "", value, destSize);
        return value.toString();
    }

    /**
     * Native support extracted from the animation expansion loop in LoadVObjects @00478EB6.
     */
    private static void loadExpandedAnimation(ResInHeap res, String section, String parentSection, List<Integer> dest) {
        List<Integer> animationTimes = new ArrayList<>();
        List<Integer> animationFrames = new ArrayList<>();
        loadInheritedIntArray(res, section, parentSection, ANIMATION_TIME, animationTimes);
        loadInheritedIntArray(res, section, parentSection, ANIMATION_FRAME, animationFrames);
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
     * Native support extracted from inherited `ResInHeap::GetIntArray` branches in LoadVObjects @00478EB6.
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
}
