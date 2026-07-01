package ua.millfreedom.rom2;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import ua.millfreedom.rom2.model.CMousePointer;
import ua.millfreedom.rom2.model.CServerApp;
import ua.millfreedom.rom2.model.ScriptDataSupport;
import ua.millfreedom.rom2.model.Screen;
import ua.millfreedom.rom2.model.ServerConfig;
import ua.millfreedom.rom2.model.SkillProgression;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.render.FpsCounter;
import ua.millfreedom.rom2.model.render.GLCursor;
import ua.millfreedom.rom2.model.render.GLRenderer;
import ua.millfreedom.rom2.model.sound.SoundSystem;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.model.window.DialogsMaskFlag;
import ua.millfreedom.rom2.model.window.MessageSystem;
import ua.millfreedom.rom2.model.world.TerrainGraphics;
import ua.millfreedom.rom2.CFile.LEReader;
import ua.millfreedom.rom2.model.net.CLlDriver;
import ua.millfreedom.rom2.platform.glfw.GlfwKeyboardMessageAdapter;
import ua.millfreedom.rom2.platform.glfw.GlfwMouseMessageAdapter;
import ua.millfreedom.rom2.text.GameTexts;
import ua.millfreedom.rom2.text.TextTableId;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_AUTO_ICONIFY;
import static org.lwjgl.glfw.GLFW.GLFW_BLUE_BITS;
import static org.lwjgl.glfw.GLFW.GLFW_DECORATED;
import static org.lwjgl.glfw.GLFW.GLFW_FLOATING;
import static org.lwjgl.glfw.GLFW.GLFW_FOCUS_ON_SHOW;
import static org.lwjgl.glfw.GLFW.GLFW_GREEN_BITS;
import static org.lwjgl.glfw.GLFW.GLFW_MAXIMIZED;
import static org.lwjgl.glfw.GLFW.GLFW_RED_BITS;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwFocusWindow;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCharCallback;
import static org.lwjgl.glfw.GLFW.glfwSetCursorEnterCallback;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSetWindowTitle;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static ua.millfreedom.rom2.model.enums.MessageCodes.STATIC_TEXT_CARET_BLINK_TICK;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.FAME_HALL_DOCUMENT;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.MODAL_DIALOG;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SHOW_INVALID_CD_PROMPT;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SHOW_MAIN_MENU;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_RAGE_OF_MAGES_2_NECROMANCER_150;

/**
 * Native-inspired Java application object for the recovered CMainApp lifecycle.
 */
public final class CMainApp {
    private static final int DEFAULT_TIMEOUT_SECONDS = 0x0F;
    private static final int MAX_TIMEOUT_SECONDS = 0x78;
    private static final int MIN_LATENCY_MILLIS = 0x32;
    private static final int MAX_LATENCY_MILLIS = 10_000;
    private static final int SERVER_WORLD_PUMP_INTERVAL_MS = 0x3E;
    private static final int LOAD_CONFIG_OPEN_FAILED = -1;
    private static final int LOAD_CONFIG_OK = 0;
    private static final int LOAD_CONFIG_PARSE_STOP = 1;
    private static final int SAVE_LOCATION_CLIENT = 0;
    private static final int SAVE_LOCATION_SERVER = 1;
    private static final String FALLBACK_WINDOW_TITLE = "Rage of Mages 2: Necromancer";
    private static final Path FAME_HALL_FILE = Path.of("famehall.dat");

    // Native global: DWORD_006275b4, used by CMainApp::OnIdle @00481CCB.
    private static int lastServerWorldTick;

    private final String commandLine;
    private long window = NULL;
    private GlfwMouseMessageAdapter mouseMessageAdapter;
    private GlfwKeyboardMessageAdapter keyboardMessageAdapter;

    /**
     * Native: CMainApp::CMainApp @00492C00.
     * Java port status: fully ported.
     */
    public CMainApp() {
        this("");
    }

    /**
     * Java support constructor for platform-provided command-line arguments.
     * not ported.
     */
    public CMainApp(String[] args) {
        this(String.join(" ", args == null ? new String[0] : args));
    }

    /**
     * Native support extracted from CMainApp::CMainApp @00492C00 and command-line storage used by InitInstance @00480C8D.
     */
    private CMainApp(String commandLine) {
        this.commandLine = commandLine == null ? "" : commandLine;
        Globals.commandLine = this.commandLine;
    }

    /**
     * Native support extracted from CWinApp::Run, CMainApp::InitInstance @00480C8D, and CMainApp::ExitInstance @00481B0C.
     */
    public void run() throws Exception {
        try {
            if (InitInstance()) {
                Run();
            }
        } finally {
            ExitInstance();
        }
    }

    /**
     * Native support extracted from the window creation branch in CMainApp::InitInstance @00480C8D.
     * CD validation is intentionally collapsed to default-success startup.
     */
    private void initWindow() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        CMainWindow.initializeNativeLogicalScreenGeometry();
        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode mode = glfwGetVideoMode(monitor);

        glfwWindowHint(GLFW_RED_BITS, mode.redBits());
        glfwWindowHint(GLFW_GREEN_BITS, mode.greenBits());
        glfwWindowHint(GLFW_BLUE_BITS, mode.blueBits());
        glfwWindowHint(GLFW_DECORATED, 0);
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
        glfwWindowHint(GLFW_FLOATING, GLFW_TRUE);
        glfwWindowHint(GLFW_AUTO_ICONIFY, 0);
        glfwWindowHint(GLFW_FOCUS_ON_SHOW, GLFW_TRUE);

        window = glfwCreateWindow(mode.width(), mode.height(), FALLBACK_WINDOW_TITLE, monitor, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }
    }

    /**
     * Native support boundary for InitDirectDraw_Fullscreen @0045293C and SetVideoWindowMode @00453036
     * calls in CMainApp::InitInstance @00480C8D.
     */
    private void initializeNativeVideoMode() {
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        glfwSwapInterval(1);
        initPalettes();
        try (MemoryStack stack = stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            glfwGetFramebufferSize(window, width, height);
            initializeScreenAndRenderer(width.get(0), height.get(0));
        }
        glfwShowWindow(window);
        glfwPollEvents();
        glfwFocusWindow(window);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    /**
     * Native support boundary for the DirectDraw surfaces and pixel-format state in
     * InitDirectDraw_Fullscreen @0045293C, SetVideoWindowMode @00453036,
     * InitializeDirectDrawPixelFormatState @004531FC, DDrawExists @00452220, InitLUT @0045225B,
     * CountTrailingZeros32 @004528CC, and HighestSetBitIndex16 @00452904.
     * skipped: Java uses renderer object lifetime, fixed RGB565 color packing, BGRA OpenGL upload, and direct shade
     * helpers instead of a DirectDraw global, mask probing, and native LUT allocation.
     */
    private void initializeScreenAndRenderer(int screenWidth, int screenHeight) {
        Globals.screenRect.set(0, 0, screenWidth, screenHeight);
        int mainWindowLeft = (screenWidth - CMainWindow.MAIN_WINDOW_WIDTH) / 2;
        int mainWindowTop = (screenHeight - CMainWindow.MAIN_WINDOW_HEIGHT) / 2;
        Globals.mainWindowRect.set(
                mainWindowLeft,
                mainWindowTop,
                screenWidth - mainWindowLeft,
                screenHeight - mainWindowTop
        );
        Globals.screen = Screen.createBgraSurface(screenWidth, screenHeight);
        Globals.renderer = new GLRenderer(Globals.screen);
        Globals.mousePointer = new GLCursor(window);
        Globals.presentCurrentSurface = this::presentCurrentSurface;
        Globals.shouldAbortBlockingPlayback = () ->
                Globals.blockingPlaybackAbortRequested || glfwWindowShouldClose(window);
        CMousePointer.Cursor_Default.setToMousePointer();
    }

    /**
     * Native: CMainApp::InitInstance @00480C8D.
     * Java port status: fully ported; registry/CD/scenario-DLL/DirectDraw details are explicit Java support boundaries.
     */
    public boolean InitInstance() {
        applyCommandLineOptions();
        loadNativeDisplayPreferences();
        CMainWindow mainWindow = createMainWindow();
        initWindow();
        initializeNativeSearchPaths();
        loadPrimaryNativeResourceArchives();
        applyAudioAndTraceCommandLineOptions();
        resetDirectPlaySessionFlag();
        loadSecondaryNativeResourceArchives();
        loadNativeUpdateList();
        captureInitialClipCursor(mainWindow);
        initializeMaterialAndVideoState(mainWindow);
        loadNativeConfiguration();
        initializeNativeVideoMode();
        loadHelpText();
        loadTextFiles();
        parseTunes();
        initializeFameHallScores(mainWindow);
        applyNativeWindowTitle();
        loadStaticStartupData();
        initializeMainWindow(mainWindow);
        dispatchInitialStartupMessage(mainWindow);
        return initializeScenarioRuntime();
    }

    /**
     * Native: Global::InitPalettes @0045EA70.
     * Java port status: fully ported through Palettes static initialization.
     */
    private void initPalettes() {
        Palettes.ensureStaticLoad();
    }

    /**
     * Native support extracted from CMainApp::InitInstance @00480C8D command-line handling.
     */
    private void applyCommandLineOptions() {
        int latencyMillis = parseIntegerOption("-latency", 0);
        if (latencyMillis != 0 && (latencyMillis < MIN_LATENCY_MILLIS || latencyMillis > MAX_LATENCY_MILLIS)) {
            latencyMillis = 0;
        }

        int timeoutSeconds = parseIntegerOption("-timeout", DEFAULT_TIMEOUT_SECONDS);
        if (timeoutSeconds == 0 || timeoutSeconds > MAX_TIMEOUT_SECONDS) {
            timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
        }
        applyNetworkTimingOptions(latencyMillis, timeoutSeconds * 1000);
    }

    /**
     * Native support extracted from g_Latency/g_Timeout writes in CMainApp::InitInstance @00480C8D.
     */
    private static void applyNetworkTimingOptions(int latencyMillis, int timeoutMillis) {
        Globals.networkLatencyMillis = latencyMillis;
        Globals.networkTimeoutMillis = timeoutMillis;
    }

    /**
     * Native support extracted from the g_resolution registry read in CMainApp::InitInstance @00480EA9.
     */
    private static void loadNativeDisplayPreferences() {
        Globals.resolutionPreference = ApplicationPreferences.loadResolutionPreference();
    }

    /**
     * Native support extracted from IsTraceEnabled write in CMainApp::InitInstance @00480C8D.
     */
    private static void enableTraceDiagnostics() {
        Globals.traceDiagnosticsEnabled = true;
    }

    /**
     * Native support boundary for IsSafeVideo write in CMainApp::InitInstance @00480C8D.
     */
    private static void enableSafeVideoMode() {
    }

    /**
     * Native support extracted from CMainApp::InitInstance @00480C8D command-line substring checks.
     */
    private boolean isCommandLineFlagPresent(String flag) {
        return commandLine.contains(flag);
    }

    /**
     * Native support extracted from GetInt command-line reads in CMainApp::InitInstance @00480C8D.
     */
    private int parseIntegerOption(String option, int defaultValue) {
        int optionIndex = commandLine.indexOf(option);
        if (optionIndex < 0) {
            return defaultValue;
        }
        int cursor = optionIndex + option.length();
        while (cursor < commandLine.length() && Character.isWhitespace(commandLine.charAt(cursor))) {
            cursor++;
        }
        int valueStart = cursor;
        while (cursor < commandLine.length() && Character.isDigit(commandLine.charAt(cursor))) {
            cursor++;
        }
        if (valueStart == cursor) {
            return defaultValue;
        }
        return Integer.parseInt(commandLine.substring(valueStart, cursor));
    }


    /**
     * Native support extracted from the graphics/main/patch/world/music/video LoadResource block in CMainApp::InitInstance @00480C8D.
     */
    private static void loadPrimaryNativeResourceArchives() {
        Globals.gameFileManager.loadNativePrimaryStartupResources();
    }

    /**
     * Native support extracted from the -nomusic/-trace branch in CMainApp::InitInstance @00480C8D.
     */
    private void applyAudioAndTraceCommandLineOptions() {
        if (isCommandLineFlagPresent("-nomusic")) {
            Globals.soundPreferences.musicAvailable = 0;
        }
        if (isCommandLineFlagPresent("-trace")) {
            enableTraceDiagnostics();
        }
    }

    /**
     * Native support extracted from CLlDriver::SetDirectPlaySessionFlag0x40Enabled @00493A80 in CMainApp::InitInstance @00480C8D.
     */
    private static void resetDirectPlaySessionFlag() {
        CLlDriver.setDirectPlaySessionFlagEnabled(false);
    }

    /**
     * Native support extracted from the sfx/movies/scenario/speech LoadResource block in CMainApp::InitInstance @00480C8D.
     */
    private static void loadSecondaryNativeResourceArchives() {
        Globals.gameFileManager.loadNativeSecondaryStartupResources();
    }

    /**
     * Native support boundary for LoadUpdateList("update.lst") in CMainApp::InitInstance @00480C8D.
     */
    private static void loadNativeUpdateList() {
        Globals.gameFileManager.loadNativeUpdateList();
    }

    /**
     * Native support extracted from CMainWindow allocation and CWinThread::m_pMainWnd assignment in
     * CMainApp::InitInstance @00480C8D, including the modeled CMainWindow::OnCreate @004826A0 callback normally
     * reached through MFC window creation.
     */
    private static CMainWindow createMainWindow() {
        CMainWindow mainWindow = new CMainWindow();
        Globals.mainWindow = mainWindow;
        mainWindow.onCreate(null);
        return mainWindow;
    }

    /**
     * Native support extracted from the GetTempPathA/AddSearchPath block in CMainApp::InitInstance @0048100B.
     * CGameFileManager.createNativeGlobalFileManager covers the earlier current-directory AddSearchPath call.
     */
    private static void initializeNativeSearchPaths() {
        Globals.gameFileManager.addSearchPath(
                Path.of(System.getProperty("java.io.tmpdir")).toAbsolutePath().normalize().toString()
        );
    }

    /**
     * Native support boundary for CMainWindow::GetClipCursor in CMainApp::InitInstance @00480C8D.
     */
    private static void captureInitialClipCursor(@SuppressWarnings("unused") CMainWindow mainWindow) {
    }

    /**
     * Native support extracted from MaterialRuntimeData::loadMaterials, fame-hall factor/document setup, and safe-video
     * flag handling in CMainApp::InitInstance @00480C8D.
     */
    private void initializeMaterialAndVideoState(CMainWindow mainWindow) {
        Globals.materialRuntimeData.loadMaterials();
        mainWindow.m_FameHall.zeroFactors();
        mainWindow.m_FameHall.setSelectedDifficulty(1);
        mainWindow.m_FameHall.registerTextDocument(1);
        TerrainGraphics.terrainGraphicsFlags = 0;
        if (isCommandLineFlagPresent("-safevideo")) {
            enableSafeVideoMode();
        }
    }

    /**
     * Native support boundary for LoadConfig(server.cfg/-cfg) in CMainApp::InitInstance @00480C8D.
     */
    private void loadNativeConfiguration() {
        int optionIndex = commandLine.indexOf("-cfg\"");
        if (optionIndex < 0) {
            int result = loadConfig("server.cfg");
            if (result > 0) {
                showNativeStartupWarning("Error in server.cfg. Line %d.".formatted(result));
            }
            return;
        }

        int cursor = optionIndex + "-cfg\"".length();
        int closingQuote = commandLine.indexOf('"', cursor);
        if (closingQuote < 0) {
            return;
        }

        String configPath = commandLine.substring(cursor, closingQuote);
        int result = loadConfig(configPath);
        if (result < 0) {
            showNativeStartupWarning("Error loading " + configPath + ".");
        } else if (result > 0) {
            showNativeStartupWarning("Error in " + configPath + ". Line " + result + ".");
        }
    }

    /**
     * Java support boundary for AfxMessageBox calls in CMainApp::InitInstance @00480C8D.
     * Native intentionally reports these startup warnings and continues.
     */
    private static void showNativeStartupWarning(String message) {
        System.err.println(message);
    }

    /**
     * Native: Global::LoadConfig @004EF479.
     * Fully ported.
     */
    public static int loadConfig(String configPath) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Path.of(configPath), Charset.defaultCharset());
        } catch (Throwable ignored) {
            return LOAD_CONFIG_OPEN_FAILED;
        }

        String section = "";
        for (String rawLine : lines) {
            String lowerLine = rawLine.toLowerCase(Locale.ROOT);
            int commentIndex = lowerLine.indexOf(';');
            int equalsIndex = lowerLine.indexOf('=');
            if (commentIndex >= 0 && commentIndex <= equalsIndex) {
                equalsIndex = -1;
            }
            String workingLine = commentIndex >= 0 ? lowerLine.substring(0, commentIndex) : lowerLine;

            if (!workingLine.isEmpty() && workingLine.charAt(0) == '[') {
                String sectionText = trimLeft(workingLine.substring(1));
                int closeBracket = sectionText.indexOf(']');
                if (closeBracket < 0) {
                    return LOAD_CONFIG_PARSE_STOP;
                }
                section = trimRight(sectionText.substring(0, closeBracket));
                continue;
            }

            if ("maps".equals(section)) {
                if (!applyMapConfigLine(workingLine, equalsIndex)) {
                    return LOAD_CONFIG_PARSE_STOP;
                }
                continue;
            }

            if (equalsIndex < 0) {
                if (!applyConfigListLine(section, workingLine, rawLine)) {
                    return LOAD_CONFIG_PARSE_STOP;
                }
                continue;
            }

            String valueLower = trimBoth(workingLine.substring(equalsIndex + 1));
            String valueRaw = trimBoth(rawLine.substring(Math.min(equalsIndex + 1, rawLine.length())));
            if (!applySettingsConfigLine(section, workingLine, valueLower, valueRaw)) {
                return LOAD_CONFIG_PARSE_STOP;
            }
        }
        return LOAD_CONFIG_OK;
    }

    /**
     * Native support extracted from Global::LoadConfig @004EF479 `[maps]` section parsing.
     */
    private static boolean applyMapConfigLine(String workingLine, int equalsIndex) {
        String mapLine = trimBoth(workingLine);
        if (mapLine.isEmpty()) {
            return true;
        }

        if (equalsIndex < 0) {
            Globals.serverConfig.maps.add(mapLine);
            Globals.serverConfig.field12_0x70.add(Integer.MAX_VALUE);
            return true;
        }

        int mapNameEnd = Math.min(equalsIndex, mapLine.length());
        String mapName = trimRight(mapLine.substring(0, mapNameEnd));
        String durationText = equalsIndex + 1 >= mapLine.length() ? "" : trimLeft(mapLine.substring(equalsIndex + 1));
        Globals.serverConfig.maps.add(mapName);
        Globals.serverConfig.field12_0x70.add(parseLeadingIntOrZero(durationText));
        return true;
    }

    /**
     * Native support extracted from Global::LoadConfig @004EF479 list-only config sections.
     */
    private static boolean applyConfigListLine(String section, String workingLine, String rawLine) {
        if ("bannedips".equals(section)) {
            Globals.serverConfig.bannedips.add(trimBoth(workingLine));
            return true;
        }
        if ("bannedplayers".equals(section)) {
            Globals.serverConfig.bannedplayers.add(trimBoth(rawLine));
            return true;
        }
        if ("reporttowww".equals(section)) {
            Globals.serverConfig.reporttowww.add(trimBoth(workingLine));
            return true;
        }
        return trimBoth(workingLine).isEmpty();
    }

    /**
     * Native support extracted from Global::LoadConfig @004EF479 `[settings]` key-value parsing.
     */
    private static boolean applySettingsConfigLine(
            String section,
            String workingLine,
            String valueLower,
            String valueRaw
    ) {
        if (!"settings".equals(section)) {
            return false;
        }

        int value = parseLeadingIntOrZero(valueLower);
        if (workingLine.startsWith("repopdelay")) {
            Globals.serverConfig.repopdelay = Math.max(0x14, Math.min(500, value));
            return true;
        }
        if (workingLine.startsWith("protocol")) {
            return applyConfigProtocol(valueLower);
        }
        if (workingLine.startsWith("gamespeed")) {
            Globals.serverConfig.gameSpeed = value < 0 || value > 8 ? 4 : value;
            return true;
        }
        if (workingLine.startsWith("logfile")) {
            Globals.serverConfig.logfile = valueLower;
            return true;
        }
        if (workingLine.startsWith("chrbase")) {
            Globals.serverConfig.chrbase = valueLower;
            return true;
        }
        if (workingLine.startsWith("ipaddress")) {
            Globals.serverConfig.ipaddress = valueLower;
            return true;
        }
        if (workingLine.startsWith("description")) {
            Globals.serverConfig.ServerName = sanitizeConfigServerName(valueRaw);
            return true;
        }
        if (workingLine.startsWith("serverid")) {
            Globals.serverConfig.serverid = value;
            return true;
        }
        if (workingLine.startsWith("sayrange")) {
            Globals.serverConfig.sayrange = value < 1 || value > 0xFF ? 0xFF : value;
            return true;
        }
        if (workingLine.startsWith("shoutdelay")) {
            Globals.serverConfig.shoutdelay = Math.max(0, value);
            return true;
        }
        if (workingLine.startsWith("maxplayers")) {
            Globals.serverConfig.maxplayers = value < 1 || value > 0x10 ? 0x10 : value;
            return true;
        }
        if (workingLine.startsWith("save")) {
            return applyConfigSaveLocation(valueLower);
        }
        return false;
    }

    /**
     * Native support extracted from Global::LoadConfig @004EF479 protocol value branch.
     */
    private static boolean applyConfigProtocol(String valueLower) {
        if ("dplay_ipx".equals(valueLower)) {
            Globals.serverConfig.protocol = ServerConfig.CONFIG_PROTOCOL_DPLAY_IPX;
            return true;
        }
        if ("dplay_tcpip".equals(valueLower)) {
            Globals.serverConfig.protocol = ServerConfig.CONFIG_PROTOCOL_DPLAY_TCPIP;
            return true;
        }
        if ("wsock_tcpip".equals(valueLower)) {
            Globals.serverConfig.protocol = ServerConfig.CONFIG_PROTOCOL_WSOCK_TCPIP;
            return true;
        }
        return false;
    }

    /**
     * Native support extracted from Global::LoadConfig @004EF479 save location branch.
     */
    private static boolean applyConfigSaveLocation(String valueLower) {
        if ("client".equals(valueLower)) {
            Globals.serverConfig.save = SAVE_LOCATION_CLIENT;
            return true;
        }
        if ("server".equals(valueLower)) {
            Globals.serverConfig.save = SAVE_LOCATION_SERVER;
            return true;
        }
        return false;
    }

    /**
     * Native support extracted from Global::LoadConfig @004EF479 `description` sanitization.
     */
    private static String sanitizeConfigServerName(String value) {
        StringBuilder sanitized = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            sanitized.append(c == '&' || c == '%' || c == '|' || c == '?' || c > 0x7F ? '*' : c);
        }
        return sanitized.toString();
    }

    /**
     * Native support extracted from GetInt @00584400 callers in Global::LoadConfig @004EF479.
     */
    private static int parseLeadingIntOrZero(String text) {
        String value = trimLeft(text);
        int end = 0;
        if (!value.isEmpty() && (value.charAt(0) == '-' || value.charAt(0) == '+')) {
            end = 1;
        }
        while (end < value.length() && Character.isDigit(value.charAt(end))) {
            end++;
        }
        if (end == 0 || (end == 1 && (value.charAt(0) == '-' || value.charAt(0) == '+'))) {
            return 0;
        }
        try {
            return Integer.parseInt(value.substring(0, end));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    /**
     * Native support extracted from CString::TrimLeft callers in Global::LoadConfig @004EF479.
     */
    private static String trimLeft(String value) {
        return value.substring(countLeadingWhitespace(value));
    }

    /**
     * Native support extracted from CString::TrimRight callers in Global::LoadConfig @004EF479.
     */
    private static String trimRight(String value) {
        int end = value.length();
        while (end > 0 && Character.isWhitespace(value.charAt(end - 1))) {
            end--;
        }
        return value.substring(0, end);
    }

    /**
     * Native support extracted from paired CString::TrimLeft/TrimRight calls in Global::LoadConfig @004EF479.
     */
    private static String trimBoth(String value) {
        return trimRight(trimLeft(value));
    }

    /**
     * Native support extracted from CString::TrimLeft offset handling in Global::LoadConfig @004EF479.
     */
    private static int countLeadingWhitespace(String value) {
        int start = 0;
        while (start < value.length() && Character.isWhitespace(value.charAt(start))) {
            start++;
        }
        return start;
    }

    /**
     * Native support extracted from CFameHall::Load/GenerateFakeScores in CMainApp::InitInstance @00480C8D.
     * Full port for the recovered score-file load/fallback branch.
     */
    private static void initializeFameHallScores(CMainWindow mainWindow) {
        byte[] data;
        try {
            data = Files.readAllBytes(FAME_HALL_FILE);
        } catch (IOException ignored) {
            mainWindow.m_FameHall.generateFakeScores();
            return;
        }
        if (data.length == 0) {
            mainWindow.m_FameHall.generateFakeScores();
            return;
        }

        try (LEReader reader = new LEReader(new ByteArrayInputStream(data))) {
            mainWindow.m_FameHall.loadEntries(reader);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load famehall.dat", exception);
        }
    }

    /**
     * Native support extracted from LoadTextFileToOEM("main/text/help.txt", &g_helpText) in CMainApp::InitInstance @00480C8D.
     */
    private static void loadHelpText() {
        Globals.helpText = CTextFile.loadTextFileToOemString("main/text/help.txt");
    }

    /**
     * Native support extracted from the CTextFile::LoadAndParse sequence in CMainApp::InitInstance @00480C8D.
     */
    private static void loadTextFiles() {
        CTextFile.LoadAndParse(TextTableId.MAIN);
        CTextFile.LoadAndParse(TextTableId.HEROPICTURE);
        CTextFile.LoadAndParse(TextTableId.STATS);
        CTextFile.LoadAndParse(TextTableId.SPELLS);
        CTextFile.LoadAndParse(TextTableId.SPELL);
        CTextFile.LoadAndParse(TextTableId.DIALOGS);
        CTextFile.LoadAndParse(TextTableId.UNITNAME);
        CTextFile.LoadAndParse(TextTableId.BUILDING);
        CTextFile.LoadAndParse(TextTableId.ITEMNAME);
        CTextFile.LoadAndParse(TextTableId.NPCNAMES);
        CTextFile.LoadAndParse(TextTableId.CUTSCENE);
        CTextFile.LoadAndParse(TextTableId.CUTPATHS);
        CTextFile.LoadAndParse(TextTableId.TUNES);
        CTextFile.LoadAndParse(TextTableId.PATCH);
    }

    /**
     * Native support boundary for Global::parseTunes @004753FD call from CMainApp::InitInstance @00480C8D.
     * Java port status: skipped; tune display names resolve from generated TunesText instead of the native g_GameConfig map.
     * Native g_GameConfig @00622618 CMapStringToString static lifecycle thunks @00473E21/@00473E30/@00473E41/@00473E53
     * are skipped with that map.
     */
    private static void parseTunes() {
    }

    /**
     * Native support extracted from CWnd::SetWindowText in CMainApp::InitInstance @00480C8D.
     */
    private void applyNativeWindowTitle() {
        glfwSetWindowTitle(window, GameTexts.get(MAIN_RAGE_OF_MAGES_2_NECROMANCER_150));
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
     * Native support extracted from CMainWindow::InitializeTopLevelVisualObjects, startup flag writes, and input callback binding at @00480C8D.
     */
    private void initializeMainWindow(CMainWindow mainWindow) {
        mainWindow.initializeTopLevelVisualObjects();
        initializeSharedVisualState(mainWindow);
        mainWindow.field149_0x44C = 1;
        mainWindow.dialogsMask = 0;
        mainWindow.initializeRuntimeGraphicsAndAudio();
        mouseMessageAdapter = new GlfwMouseMessageAdapter(mainWindow.getInputController());
        keyboardMessageAdapter = new GlfwKeyboardMessageAdapter(mainWindow);
        glfwSetKeyCallback(window, keyboardMessageAdapter.keyCallback());
        glfwSetCharCallback(window, keyboardMessageAdapter.charCallback());
        glfwSetCursorPosCallback(window, mouseMessageAdapter.cursorPosCallback());
        glfwSetMouseButtonCallback(window, mouseMessageAdapter.mouseButtonCallback());
        glfwSetCursorEnterCallback(window, mouseMessageAdapter.cursorEnterCallback());
        glfwSetScrollCallback(window, mouseMessageAdapter.scrollCallback());
    }

    /**
     * Native support extracted from the invalid-CD/intro/menu post branch in CMainApp::InitInstance @00480C8D.
     */
    private void dispatchInitialStartupMessage(CMainWindow mainWindow) {
        if (!hasValidGameMedia()) {
            mainWindow.showInvalidMediaAllodsLogo();
            mainWindow.postMessage(SHOW_INVALID_CD_PROMPT, 0, 0);
        } else {
            if (!shouldSkipStartupIntro() && mainWindow.playStartupLogoSmkIntro()) {
                mainWindow.playLocationCutscene(0);
            }
            mainWindow.postMessage(SHOW_MAIN_MENU, 0, 0);
        }
    }

    /**
     * Native support boundary for the g_hasCdAlwaysOne branch in CMainApp::InitInstance @00480C8D.
     */
    private static boolean hasValidGameMedia() {
        return Globals.hasValidGameMedia;
    }

    /**
     * Native support extracted from the -startserver/-cfg/-asl checks in CMainApp::InitInstance @00480C8D.
     */
    private boolean shouldSkipStartupIntro() {
        return isCommandLineFlagPresent("-startserver") || isCommandLineFlagPresent("-cfg") || isCommandLineFlagPresent(".asl");
    }

    /**
     * Native support extracted from Global::loadApplicationPreferences @00440BB7 after
     * CMainWindow::InitializeTopLevelVisualObjects in CMainApp::InitInstance @00480C8D.
     */
    private static void initializeSharedVisualState(CMainWindow mainWindow) {
        ApplicationPreferences.loadApplicationPreferences(mainWindow);
    }

    /**
     * Native support extracted from CMainApp::InitInstance @00480C8D scenario.dll initialization branch.
     * Java links scenario callbacks directly through ScenarioLib instead of loading a DLL at runtime.
     */
    private static boolean initializeScenarioRuntime() {
        initializeScenarioSupportTables();
        return true;
    }

    /**
     * Native support extracted from the makeTableOfCompounding10PctPermille @00481AB6 and
     * setMessageColorsPalette @00481AC2 calls in CMainApp::InitInstance @00480C8D.
     */
    private static void initializeScenarioSupportTables() {
        SkillProgression.initializeNativeCompoundingTable();
        Palettes.setMessageColorsPalette(Globals.gamePreferences.messageColors);
    }

    /**
     * Native support extracted from CWinApp::Run message pumping around CMainApp::OnIdle @00481CCB.
     */
    private void Run() {
        while (!glfwWindowShouldClose(window)) {
            if (!MessageSystem.pumpPostedMessage()) {
                OnIdle();
            }
            closeWindowIfNativeCloseRequested();
        }
    }

    /**
     * Native: CMainApp::OnIdle @00481CCB.
     * Java port status: fully ported; ResetFullscreenDirectDrawPrimarySurface @00453129, Java presentation shell
     * overlays, and surface presentation remain support boundaries.
     */
    public boolean OnIdle() {
        CMainWindow mainWindow = Globals.mainWindow;

        if (mainWindow.sessionMode == CMainWindow.SESSION_MODE_DEDICATED_SERVER) {
            mainWindow.pumpDedicatedServerIdle();
        } else {
            if (GAMEPLAY.isUnsetIn(mainWindow.dialogsMask)) {
                pumpNonWorldIdle(mainWindow);
                mainWindow.renderFrameIfFocused();
                Globals.mousePointer.update();
            } else {
                pumpGameplayIdle(mainWindow);
            }
        }

        processNetworkEvents();
        Globals.mousePointer.update();
        mainWindow.getInputController().onMessage(STATIC_TEXT_CARET_BLINK_TICK, 0, 0);
        drawFrameOverlays();
        presentCurrentSurface();
        return true;
    }

    /**
     * Native support extracted from the non-dialog MapVisualObject::HandleGameAction branch in CMainApp::OnIdle @00481CCB.
     */
    private static void pumpNonWorldIdle(CMainWindow mainWindow) {
        if (mainWindow.serverBootstrapEnabled == 0) {
            if (mainWindow.pMultiplayerMapSelectionDialogVisualObject != null
                    && !CServerApp.hasActiveRemoteConnection()) {
                mainWindow.pMultiplayerMapSelectionDialogVisualObject.onMessage(MessageCodes.RETURN_TO_GAME, 0, 0);
            }
            mainWindow.pMapVisualObject.handleGameAction(null, 0);
            return;
        }

        int currentTick = currentTick();
        if (Integer.compareUnsigned(currentTick - lastServerWorldTick, SERVER_WORLD_PUMP_INTERVAL_MS) > 0) {
            Globals.gameServer.pumpServerWorldActions();
            mainWindow.pMapVisualObject.handleGameAction(null, 0);
            lastServerWorldTick = currentTick;
        }
    }

    /**
     * Native support extracted from CMainWindow::pumpRemoteGameplayIdle @00488E25,
     * CMainWindow::pumpTimedGameplayTicks @00488AA1, CMainWindow::pumpSingleGameplayTick @00488900, and
     * their call sites in CMainApp::OnIdle @00481CCB.
     */
    private static void pumpGameplayIdle(CMainWindow mainWindow) {
        if (mainWindow.serverBootstrapEnabled == 0) {
            mainWindow.pumpRemoteGameplayIdle();
            return;
        } else if (mainWindow.sessionMode != CMainWindow.SESSION_MODE_CAMPAIGN
                || DialogsMaskFlag.doesNotContain(mainWindow.dialogsMask, MODAL_DIALOG, FAME_HALL_DOCUMENT)) {
            if (mainWindow.singleStepGameplayTickMode == 0) {
                mainWindow.pumpTimedGameplayTicks();
            } else {
                mainWindow.pumpSingleGameplayTick();
            }
            return;
        }
        renderSuppressedGameplayModal(mainWindow);
    }

    /**
     * Java rendering extension for campaign role/centered modals while native gameplay ticks are suppressed by mask
     * `0x4008` in CMainApp::OnIdle @00481CCB.
     */
    private static void renderSuppressedGameplayModal(CMainWindow mainWindow) {
        mainWindow.renderFrameIfFocused();
        Globals.mousePointer.update();
    }

    /**
     * Native support extracted from Java cursor selection, tooltip, and FPS presentation layered over
     * CMainApp::OnIdle @00481CCB.
     */
    private static void drawFrameOverlays() {
        Globals.mousePointer.drawSelectionOverlay();
        Globals.mousePointer.drawTooltipOverlay();
        FpsCounter.draw();
    }

    /**
     * Native support extracted from the g_CServerApp_remote then g_CServerApp_local ProcessNetworkEvents calls made by
     * CMainApp::OnIdle @00481CCB.
     */
    private static void processNetworkEvents() {
        CServerApp.processRemoteNetworkEvents();
        CServerApp.processLocalNetworkEvents();
    }

    /**
     * Native support for GetTickCount/timeGetTime DWORD comparisons in CMainApp::OnIdle @00481CCB and helpers.
     */
    private static int currentTick() {
        return (int) System.currentTimeMillis();
    }

    /**
     * Presents the current software surface and pumps GLFW events for normal and blocking playback loops.
     * not ported.
     */
    private void presentCurrentSurface() {
        if (window == NULL || Globals.renderer == null) {
            return;
        }

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        int framebufferWidth;
        int framebufferHeight;
        try (MemoryStack stack = stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            glfwGetFramebufferSize(window, width, height);
            framebufferWidth = width.get(0);
            framebufferHeight = height.get(0);
        }

        Globals.renderer.presentSurface(framebufferWidth, framebufferHeight);
        glfwSwapBuffers(window);
        glfwPollEvents();
    }

    /**
     * Java bridge for the native CFrameWnd::OnClose @005B46A0 tail reached from CMainWindow::OnClose @00492235.
     * not ported.
     */
    private void closeWindowIfNativeCloseRequested() {
        if (Globals.mainWindow == null) {
            return;
        }
        if (Globals.mainWindow.isWindowCloseRequested()) {
            glfwSetWindowShouldClose(window, true);
        }
    }

    /**
     * Native: CMainApp::ExitInstance @00481B0C.
     * Java port status: fully ported; Win32 DLL/handle releases map to Java scenario and platform shell boundaries.
     */
    public int ExitInstance() {
        writeTracePacketStatsLog();
        releaseScenarioRuntimeHandles();
        CMainWindow mainWindow = Globals.mainWindow;
        if (mainWindow != null) {
            mainWindow.onDestroy();
        }
        releaseNativeTextResources();
        releaseNativeBitmapResourcePools();
        SoundSystem.shutdownIfInitialized();
        releaseNativeFontAndCursorResourcePools();
        Globals.presentCurrentSurface = () -> {
        };
        Globals.shouldAbortBlockingPlayback = () -> false;
        Globals.blockingPlaybackActive = false;
        Globals.blockingPlaybackAbortRequested = false;
        if (Globals.renderer != null) {
            Globals.renderer.releasePresentationResources();
            Globals.renderer = null;
        }
        releaseNativeColorLutBoundary();
        Globals.screen = null;
        Globals.screenRect.set(0, 0, 0, 0);
        Globals.mainWindowRect.set(0, 0, 0, 0);
        if (Globals.mousePointer instanceof GLCursor glCursor) {
            glCursor.destroy();
            Globals.mousePointer = new CMousePointer();
        }
        mouseMessageAdapter = null;
        keyboardMessageAdapter = null;
        if (window != NULL) {
            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);
            window = NULL;
        }
        glfwTerminate();
        GLFWErrorCallback errorCallback = glfwSetErrorCallback(null);
        if (errorCallback != null) {
            errorCallback.free();
        }
        deleteNativeTemporaryFiles();
        return 0;
    }

    /**
     * Native support extracted from the IsTraceEnabled packet-stat write in CMainApp::ExitInstance @00481B1B.
     */
    private static void writeTracePacketStatsLog() {
        if (!Globals.traceDiagnosticsEnabled) {
            return;
        }
        try {
            CServerApp.writeLocalPacketStatsLog("pkt.log");
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write native packet stats log", exception);
        }
    }

    /**
     * Native support boundary for FreeLibrary(scenarioLibrary) and CloseHandle(event_AutoRun) in CMainApp::ExitInstance @00481B48.
     */
    private static void releaseScenarioRuntimeHandles() {
    }

    /**
     * Native support extracted from the CTextFile::Delete sequence in CMainApp::ExitInstance @00481B58.
     */
    private static void releaseNativeTextResources() {
        CTextFile.delete(TextTableId.PATCH);
        CTextFile.delete(TextTableId.TUNES);
        CTextFile.delete(TextTableId.CUTPATHS);
        CTextFile.delete(TextTableId.CUTSCENE);
        CTextFile.delete(TextTableId.NPCNAMES);
        ScriptDataSupport.clearScriptDataTextFile();
        CTextFile.delete(TextTableId.ITEMNAME);
        CTextFile.delete(TextTableId.BUILDING);
        CTextFile.delete(TextTableId.UNITNAME);
        CTextFile.delete(TextTableId.DIALOGS);
        CTextFile.delete(TextTableId.SPELL);
        CTextFile.delete(TextTableId.SPELLS);
        CTextFile.delete(TextTableId.STATS);
        CTextFile.delete(TextTableId.HEROPICTURE);
        CTextFile.delete(TextTableId.MAIN);
    }

    /**
     * Native support extracted from GUI::releaseInterfaceGraphics @00477EEC call in CMainApp::ExitInstance @00481BF2.
     */
    private static void releaseNativeBitmapResourcePools() {
        GUI.releaseInterfaceGraphics();
    }

    /**
     * Native support boundary for FUN_0045F6D1 @0045F6D1 and ReleaseCursors @0047C3C0 in CMainApp::ExitInstance @00481BFC.
     */
    private static void releaseNativeFontAndCursorResourcePools() {
    }

    /**
     * Native support boundary for ReleaseColorLUT @00452669 in CMainApp::ExitInstance @00481C06.
     * skipped: Java does not allocate g_pColorLUT_UNUSED_IN_JAVA; RGB16/RGB32 shade helpers replace native LUT pages.
     */
    private static void releaseNativeColorLutBoundary() {
    }

    /**
     * Native support extracted from the GetTempPathA/FindFirstFileA/DeleteFileA loop in CMainApp::ExitInstance @00481C41.
     */
    private static void deleteNativeTemporaryFiles() {
        try (Stream<Path> files = Files.list(Path.of(System.getProperty("java.io.tmpdir")))) {
            files.filter(path -> {
                        String fileName = path.getFileName().toString();
                        return fileName.startsWith("allods-2-") && fileName.endsWith(".$$$");
                    })
                    .forEach(CMainApp::deleteNativeTemporaryFileIgnoringFailure);
        } catch (IOException ignored) {
            // Native ignores FindFirstFileA failure.
        }
    }

    /**
     * Native support extracted from the DeleteFileA calls in CMainApp::ExitInstance @00481C87.
     */
    private static void deleteNativeTemporaryFileIgnoringFailure(Path file) {
        try {
            Files.delete(file);
        } catch (IOException ignored) {
            // Native ignores DeleteFileA failure.
        }
    }

    /**
     * Entry point for the CMainApp Java shell.
     * not ported.
     */
    public static void main(String[] args) throws Exception {
        new CMainApp(args).run();
    }
}
