package ua.millfreedom.rom2;

/**
 * Shared console boundary around the native-compatible server.cfg parser.
 * not ported.
 */
public final class ServerConfigurationLoader {
    public static final String DEFAULT_CONFIG_PATH = "server.cfg";

    /**
     * Java utility constructor.
     * not ported.
     */
    private ServerConfigurationLoader() {
    }

    /**
     * Java support wrapper around Global::LoadConfig @004EF479 error handling for console entry points.
     * not ported.
     */
    public static void loadServerConfig(String configPath, boolean explicitConfig, String missingDefaultMessage) {
        String resolvedConfigPath = configPath == null ? DEFAULT_CONFIG_PATH : configPath;
        int result = CMainApp.loadConfig(resolvedConfigPath);
        if (result < 0 && explicitConfig) {
            throw new IllegalStateException("Error loading " + resolvedConfigPath + ".");
        }
        if (result > 0) {
            throw new IllegalStateException("Error in " + resolvedConfigPath + ". Line " + result + ".");
        }
        if (result < 0 && missingDefaultMessage != null && !missingDefaultMessage.isBlank()) {
            System.err.println(missingDefaultMessage);
        }
    }

    /**
     * Java support parser for native-shaped quoted command-line options such as -cfg"server.cfg".
     * not ported.
     */
    public static String extractNativeQuotedOption(String commandLine, String marker) {
        int markerIndex = commandLine.indexOf(marker);
        if (markerIndex < 0) {
            return null;
        }
        int valueStart = markerIndex + marker.length();
        int valueEnd = commandLine.indexOf('"', valueStart);
        if (valueEnd < 0) {
            return null;
        }
        return commandLine.substring(valueStart, valueEnd);
    }
}
