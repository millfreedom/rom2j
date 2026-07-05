package ua.millfreedom.rom2.starter;

import ua.millfreedom.rom2.mapeditor.MapEditorFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Standalone Java support entry point for the MapEditor.
 * not ported.
 */
public final class MapEditorStarter {
    /**
     * Java utility constructor.
     * not ported.
     */
    private MapEditorStarter() {
    }

    /**
     * Java support process entry point for the standalone MapEditor.
     * not ported.
     */
    public static void main(String[] args) {
        NativeStartupSupport.installSystemAwareSwingLookAndFeel();
        MapEditorFrame frame = MapEditorFrame.createAndShow();
        startEditorNativeStartup(frame);
    }

    /**
     * Java support boundary for loading editor native resources after the startup frame display is queued.
     * not ported.
     */
    private static void startEditorNativeStartup(MapEditorFrame frame) {
        Thread startupThread = new Thread(() -> initializeEditorNativeStartup(frame), "mapeditor-native-startup");
        startupThread.start();
    }

    /**
     * Java support error-reporting wrapper around editor native resource initialization.
     * not ported.
     */
    private static void initializeEditorNativeStartup(MapEditorFrame frame) {
        try {
            NativeStartupSupport.initializeEditorNativeStartup();
            SwingUtilities.invokeLater(() -> {
                frame.setTerrainPreviewResourcesReady(true);
                frame.setObjectPreviewResourcesReady(true);
                frame.setStructurePreviewResourcesReady(true);
                frame.setUnitPreviewResourcesReady(true);
                frame.setSackPreviewResourcesReady(true);
                frame.setEffectPreviewResourcesReady(true);
            });
        } catch (RuntimeException | Error exception) {
            SwingUtilities.invokeLater(() -> showStartupError(frame, exception));
            throw exception;
        }
    }

    /**
     * Java support startup failure dialog for the standalone MapEditor shell.
     * not ported.
     */
    private static void showStartupError(MapEditorFrame frame, Throwable exception) {
        JOptionPane.showMessageDialog(
                frame,
                exception.getMessage(),
                "MapEditor startup failed",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
