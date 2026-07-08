package ua.millfreedom.rom2.starter;

import ua.millfreedom.rom2.CTextFile;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.ServerConfigurationLoader;
import ua.millfreedom.rom2.dserver.DedicatedServerConsoleSink;
import ua.millfreedom.rom2.dserver.DedicatedServerSwingConsole;
import ua.millfreedom.rom2.model.CServerApp;
import ua.millfreedom.rom2.model.SkillProgression;
import ua.millfreedom.rom2.model.UnitTypes;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.model.window.MessageSystem;
import ua.millfreedom.rom2.model.world.TerrainGraphics;
import ua.millfreedom.rom2.text.TextTableId;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.jthemedetecor.OsThemeDetector;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * Java support entry point for running the recovered dedicated server without GLFW/OpenGL initialization.
 * not ported.
 */
public final class DedicatedServerStarter {
    private static final long DEFAULT_IDLE_SLEEP_MILLIS = 10L;
    private static final TextTableId[] STARTUP_TEXT_TABLES = {
            TextTableId.MAIN,
            TextTableId.HEROPICTURE,
            TextTableId.STATS,
            TextTableId.SPELLS,
            TextTableId.SPELL,
            TextTableId.DIALOGS,
            TextTableId.UNITNAME,
            TextTableId.BUILDING,
            TextTableId.ITEMNAME,
            TextTableId.NPCNAMES,
            TextTableId.CUTSCENE,
            TextTableId.CUTPATHS,
            TextTableId.TUNES,
            TextTableId.PATCH
    };

    /**
     * Java utility constructor.
     * not ported.
     */
    private DedicatedServerStarter() {
    }

    /**
     * Java support process entry point for the headless dedicated server.
     * not ported.
     */
    public static void main(String[] args) {
        installSystemAwareSwingLookAndFeel();
        int exitCode = runFromArgs(args);
        System.exit(exitCode);
    }

    /**
     * Java support theme initialization for the Swing dedicated-server operator UI.
     * not ported.
     */
    private static void installSystemAwareSwingLookAndFeel() {
        OsThemeDetector detector = OsThemeDetector.getDetector();
        applyFlatLaf(detector.isDark());
        detector.registerListener(isDark -> SwingUtilities.invokeLater(() -> {
            applyFlatLaf(isDark);
            FlatLaf.updateUI();
        }));
    }

    /**
     * Java support FlatLaf theme selection for the Swing dedicated-server operator UI.
     * not ported.
     */
    private static void applyFlatLaf(boolean dark) {
        try {
            UIManager.setLookAndFeel(dark ? new FlatDarkLaf() : new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException exception) {
            exception.printStackTrace(System.err);
        }
    }

    /**
     * Java support boundary for command-line parsing, startup failure reporting, and process exit codes.
     * not ported.
     */
    public static int runFromArgs(String[] args) {
        try {
            Options options = Options.parse(args);
            if (options.helpRequested()) {
                System.out.println(usage());
                return 0;
            }
            return run(options);
        } catch (IllegalArgumentException exception) {
            System.err.println(exception.getMessage());
            System.err.println(usage());
            return 64;
        } catch (RuntimeException exception) {
            exception.printStackTrace(System.err);
            return 1;
        }
    }

    /**
     * Java support orchestration for the no-GL dedicated-server lifecycle.
     * not ported.
     */
    public static int run(Options options) {
        DedicatedServerConsoleSink.installStdoutSink();
        Globals.commandLine = options.commandLine();
        Globals.soundPreferences.musicAvailable = 0;

        initializeHeadlessNativeStartup(options);
        CMainWindow mainWindow = createHeadlessMainWindow();
        DedicatedServerSwingConsole operatorConsole = createOperatorConsole(options, mainWindow);
        initializeHeadlessWindowRuntime();
        String bindAddress = configureDedicatedEndpoint(options);
        String mapName = configureDedicatedMapSchedule(options);
        System.out.println("Starting dedicated server on " + Globals.serverConfig.dedicatedAdvertisedAddress()
                + " (bind " + bindAddress + ") with map " + mapName);
        if (!mainWindow.startHeadlessDedicatedServer(mapName, bindAddress)) {
            if (operatorConsole != null) {
                operatorConsole.close();
            }
            return 2;
        }
        Thread shutdownHook = installShutdownHook(mainWindow);
        try {
            runLoop(mainWindow, options.exitAfterMillis(), options.idleSleepMillis(), operatorConsole);
        } finally {
            mainWindow.destroyServer();
            if (operatorConsole != null) {
                operatorConsole.close();
            }
            removeShutdownHook(shutdownHook);
        }
        return 0;
    }

    /**
     * Java support extracted from the non-visual subset of CMainApp::InitInstance @00480C8D.
     * not ported.
     */
    private static void initializeHeadlessNativeStartup(Options options) {
        initializeNativeSearchPaths();
        Globals.gameFileManager.loadNativePrimaryStartupResources();
        Globals.gameFileManager.loadNativeSecondaryStartupResources();
        Globals.gameFileManager.loadNativeUpdateList();
        loadConfiguration(options);
        Globals.materialRuntimeData.loadMaterials();
        Globals.helpText = CTextFile.loadTextFileToOemString("main/text/help.txt");
        loadTextFiles();
        loadStaticStartupData();
        initializeScenarioSupportTables();
    }

    /**
     * Native support extracted from ResetTerrainTileSet @00476A54 and UnitTypes::loadUnitTypes @00479B1E after the
     * CMainWindow::New @00481EFC state exists.
     */
    private static void initializeHeadlessWindowRuntime() {
        TerrainGraphics.resetTerrainTileSet();
        UnitTypes.loadUnitTypes();
    }

    /**
     * Native support extracted from the GetTempPathA/AddSearchPath block in CMainApp::InitInstance @0048100B.
     */
    private static void initializeNativeSearchPaths() {
        Globals.gameFileManager.addSearchPath(
                Path.of(System.getProperty("java.io.tmpdir")).toAbsolutePath().normalize().toString()
        );
    }

    /**
     * Java support boundary around the shared Global::LoadConfig @004EF479 parser.
     * not ported.
     */
    private static void loadConfiguration(Options options) {
        ServerConfigurationLoader.loadServerConfig(
                options.configPath(),
                options.explicitConfig(),
                "No " + ServerConfigurationLoader.DEFAULT_CONFIG_PATH + " found; using dedicated-server defaults."
        );
    }

    /**
     * Native support extracted from the CTextFile::LoadAndParse sequence in CMainApp::InitInstance @00480C8D.
     */
    private static void loadTextFiles() {
        for (TextTableId tableId : STARTUP_TEXT_TABLES) {
            CTextFile.LoadAndParse(tableId);
        }
    }

    /**
     * Native support extracted from CStaticDataMgr::LoadOrRebuild, ItemNames::loadItemNames,
     * and GameFonts::loadFonts calls in CMainApp::InitInstance @00480C8D.
     */
    private static void loadStaticStartupData() {
        int staticDataLoadResult = Globals.staticDataMgr.loadOrRebuild();
        if (staticDataLoadResult != 0 || !Globals.staticDataMgr.m_bLoaded) {
            throw new IllegalStateException("Static data manager did not load static data, result " + staticDataLoadResult);
        }
        Globals.itemNames.loadItemNames();
        Globals.fonts.loadFonts();
    }

    /**
     * Native support extracted from the scenario.dll initialization tail in CMainApp::InitInstance @00480C8D.
     */
    private static void initializeScenarioSupportTables() {
        SkillProgression.initializeNativeCompoundingTable();
        Palettes.setMessageColorsPalette(Globals.gamePreferences.messageColors);
    }

    /**
     * Java support boundary for creating the modeled CMainWindow without native HWND/GLFW construction.
     * not ported.
     */
    private static CMainWindow createHeadlessMainWindow() {
        CMainWindow mainWindow = new CMainWindow();
        Globals.mainWindow = mainWindow;
        mainWindow.onCreate(null);
        mainWindow.initializeHeadlessDedicatedVisualState();
        return mainWindow;
    }

    /**
     * Java support boundary for showing the Swing dedicated-server operator UI.
     * not ported.
     */
    private static DedicatedServerSwingConsole createOperatorConsole(Options options, CMainWindow mainWindow) {
        if (!options.swingUiEnabled()) {
            return null;
        }
        installSystemAwareSwingLookAndFeel();
        return DedicatedServerSwingConsole.createAndShow(mainWindow);
    }

    /**
     * Java support boundary for dedicated map choice before the native server bootstrap consumes CMainWindow::map_.
     * not ported.
     */
    private static String configureDedicatedMapSchedule(Options options) {
        if (options.mapName() != null) {
            Globals.serverConfig.maps.clear();
            Globals.serverConfig.field12_0x70.clear();
            Globals.serverConfig.maps.add(options.mapName());
            Globals.serverConfig.field12_0x70.add(Integer.MAX_VALUE);
            Globals.serverConfig.field15_0x8c = 0;
            return options.mapName();
        }
        if (Globals.serverConfig.maps.isEmpty()) {
            throw new IllegalArgumentException("Headless dedicated server requires --map or a [maps] entry in server.cfg.");
        }
        while (Globals.serverConfig.field12_0x70.size() < Globals.serverConfig.maps.size()) {
            Globals.serverConfig.field12_0x70.add(Integer.MAX_VALUE);
        }
        if (Globals.serverConfig.field15_0x8c < 0 || Globals.serverConfig.field15_0x8c >= Globals.serverConfig.maps.size()) {
            Globals.serverConfig.field15_0x8c = 0;
        }
        return Globals.serverConfig.maps.get(Globals.serverConfig.field15_0x8c);
    }

    /**
     * Java support for resolving dedicated bind host/game port after server.cfg is loaded.
     * not ported.
     */
    private static String configureDedicatedEndpoint(Options options) {
        if (options.bindAddress() != null
                && !Globals.serverConfig.applyConfiguredIpAddress(options.bindAddress())) {
            throw new IllegalArgumentException("Invalid dedicated bind endpoint: " + options.bindAddress());
        }
        return Globals.serverConfig.dedicatedBindAddressOrDefault("0.0.0.0");
    }

    /**
     * Java support boundary for running the dedicated OnIdle subset without GLFW's message loop.
     * not ported.
     */
    private static void runLoop(
            CMainWindow mainWindow,
            long exitAfterMillis,
            long idleSleepMillis,
            DedicatedServerSwingConsole operatorConsole
    ) {
        long deadline = exitAfterMillis <= 0 ? Long.MAX_VALUE : System.currentTimeMillis() + exitAfterMillis;
        long nextOperatorUiRefresh = 0L;
        while (!mainWindow.isWindowCloseRequested() && System.currentTimeMillis() < deadline) {
            if (!MessageSystem.pumpPostedMessage()) {
                mainWindow.pumpDedicatedServerIdle();
                CServerApp.processRemoteNetworkEvents();
                CServerApp.processLocalNetworkEvents();
            }
            if (operatorConsole != null && System.currentTimeMillis() >= nextOperatorUiRefresh) {
                operatorConsole.update(mainWindow.dedicatedServerStatusSnapshot(operatorConsole.selectedPlayerId()));
                nextOperatorUiRefresh = System.currentTimeMillis() + 500L;
            }
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(idleSleepMillis));
        }
    }

    /**
     * Java support boundary for closing the native-mode server transport on JVM shutdown.
     * not ported.
     */
    private static Thread installShutdownHook(CMainWindow mainWindow) {
        Thread shutdownHook = new Thread(mainWindow::destroyServer, "rom2-dedicated-shutdown");
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        return shutdownHook;
    }

    /**
     * Java support boundary for avoiding a second destroyServer call after normal loop exit.
     * not ported.
     */
    private static void removeShutdownHook(Thread shutdownHook) {
        try {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        } catch (IllegalStateException ignored) {
            // JVM shutdown is already in progress.
        }
    }

    /**
     * Java support command usage text for the headless dedicated launcher.
     * not ported.
     */
    private static String usage() {
        return "Usage: DedicatedServerStarter [--config " + ServerConfigurationLoader.DEFAULT_CONFIG_PATH
                + "] [--map kids3.alm] [--bind 0.0.0.0[:port]] "
                + "[--exit-after-ms 5000] [--no-ui]";
    }

    /**
     * Java support parsed command-line options for DedicatedServerStarter.
     * not ported.
     */
    public record Options(
            String commandLine,
            String configPath,
            boolean explicitConfig,
            String mapName,
            String bindAddress,
            long exitAfterMillis,
            long idleSleepMillis,
            boolean swingUiEnabled,
            boolean helpRequested
    ) {
        /**
         * Java support command-line parser for the headless dedicated launcher.
         * not ported.
         */
        public static Options parse(String[] args) {
            String commandLine = String.join(" ", args);
            String configPath = null;
            boolean explicitConfig = false;
            String mapName = null;
            String bindAddress = null;
            long exitAfterMillis = 0L;
            long idleSleepMillis = DEFAULT_IDLE_SLEEP_MILLIS;
            boolean swingUiEnabled = true;
            boolean helpRequested = false;

            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                switch (arg) {
                    case "--help", "-h" -> helpRequested = true;
                    case "--config", "-cfg" -> {
                        configPath = requireValue(args, ++i, arg);
                        explicitConfig = true;
                    }
                    case "--map", "-map" -> mapName = requireValue(args, ++i, arg);
                    case "--bind", "-ip" -> bindAddress = requireValue(args, ++i, arg);
                    case "--exit-after-ms" -> exitAfterMillis = parseLongOption(arg, requireValue(args, ++i, arg));
                    case "--idle-sleep-ms" -> idleSleepMillis = parseLongOption(arg, requireValue(args, ++i, arg));
                    case "--no-ui" -> swingUiEnabled = false;
                    default -> {
                        if (arg.startsWith("--config=")) {
                            configPath = arg.substring("--config=".length());
                            explicitConfig = true;
                        } else if (arg.startsWith("--map=")) {
                            mapName = arg.substring("--map=".length());
                        } else if (arg.startsWith("--bind=")) {
                            bindAddress = arg.substring("--bind=".length());
                        } else if (arg.startsWith("--exit-after-ms=")) {
                            exitAfterMillis = parseLongOption("--exit-after-ms", arg.substring("--exit-after-ms=".length()));
                        } else if (arg.startsWith("--idle-sleep-ms=")) {
                            idleSleepMillis = parseLongOption("--idle-sleep-ms", arg.substring("--idle-sleep-ms=".length()));
                        }
                    }
                }
            }

            if (configPath == null) {
                String nativeConfigPath = ServerConfigurationLoader.extractNativeQuotedOption(commandLine, "-cfg\"");
                if (nativeConfigPath != null) {
                    configPath = nativeConfigPath;
                    explicitConfig = true;
                }
            }
            if (bindAddress == null) {
                bindAddress = ServerConfigurationLoader.extractNativeQuotedOption(commandLine, "-ip\"");
            }

            return new Options(
                    commandLine,
                    emptyToNull(configPath),
                    explicitConfig,
                    emptyToNull(mapName),
                    emptyToNull(bindAddress),
                    exitAfterMillis,
                    Math.max(1L, idleSleepMillis),
                    swingUiEnabled,
                    helpRequested
            );
        }

        /**
         * Java support accessor for the explicit dedicated bind endpoint.
         * not ported.
         */
        public String bindAddress() {
            return bindAddress;
        }

        /**
         * Java support argument-value reader for long options.
         * not ported.
         */
        private static String requireValue(String[] args, int index, String option) {
            if (index >= args.length) {
                throw new IllegalArgumentException(option + " requires a value.");
            }
            return args[index];
        }

        /**
         * Java support numeric option parser.
         * not ported.
         */
        private static long parseLongOption(String option, String value) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(option + " requires a numeric value.");
            }
        }

        /**
         * Java support empty-string normalization for optional launcher arguments.
         * not ported.
         */
        private static String emptyToNull(String value) {
            return value == null || value.isEmpty() ? null : value;
        }
    }
}
