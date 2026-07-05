package ua.millfreedom.rom2.mapeditor;

import ua.millfreedom.rom2.model.world.ScenarioDescriptor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Java support file service for MapEditor map creation, loading, and save-as.
 * not ported.
 */
public final class MapEditorFileService {
    /**
     * Java utility constructor.
     * not ported.
     */
    private MapEditorFileService() {
    }

    /**
     * Java support factory for a new blank editor map.
     * not ported.
     */
    public static MapEditorDocument createNewMap(int width, int height, int tileId, int heightValue) {
        validateMapDimensions(width, height);
        return MapEditorDocument.newBlank(width, height, tileId, heightValue);
    }

    /**
     * Java support loader for existing ALM files selected by the editor.
     * not ported.
     */
    public static MapEditorDocument load(Path path) {
        ScenarioDescriptor scenario = MapEditorScenarioIO.load(path);
        if (!scenario.loaded) {
            throw new IllegalArgumentException("Failed to load map " + path + ": error " + scenario.error);
        }
        return MapEditorDocument.loaded(scenario, path);
    }

    /**
     * Java support save-as operation that intentionally refuses to overwrite an existing map file.
     * not ported.
     */
    public static void saveAs(MapEditorDocument document, Path targetPath) throws IOException {
        byte[] payload = document.toAlmBytes();
        Files.write(targetPath, payload, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        document.markSavedAs(targetPath, payload);
    }

    /**
     * Java support validation for editor-created map dimensions.
     * not ported.
     */
    private static void validateMapDimensions(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Map dimensions must be positive.");
        }
        Math.multiplyExact(width, height);
    }
}
