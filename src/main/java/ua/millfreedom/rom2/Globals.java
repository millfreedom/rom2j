package ua.millfreedom.rom2;

import com.fasterxml.jackson.databind.ObjectMapper;
import ua.millfreedom.rom2.model.CGameLighting;
import ua.millfreedom.rom2.model.CMousePointer;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.GameFonts;
import ua.millfreedom.rom2.model.GamePreferences;
import ua.millfreedom.rom2.model.ItemNames;
import ua.millfreedom.rom2.model.QuestsStorage;
import ua.millfreedom.rom2.model.MaterialRuntimeData;
import ua.millfreedom.rom2.model.PasswordManager;
import ua.millfreedom.rom2.model.Screen;
import ua.millfreedom.rom2.model.ServerConfig;
import ua.millfreedom.rom2.model.render.Renderer;
import ua.millfreedom.rom2.model.sound.SoundPreferences;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.model.world.CWorldMap;
import ua.millfreedom.rom2.model.world.ScenarioLib;

import java.nio.file.Path;
import java.util.function.BooleanSupplier;

public class Globals {
    public static boolean useCustomEncoding = false;

    public static final String HOME_DIR = System.getProperty("user.home");
    // Native global g_CurrentDirectory @00621460, captured by CMainApp::InitInstance @00480FEA.
    public static Path currentDirectory = Path.of("").toAbsolutePath().normalize();
    public static final int isLowMemory_ALWAYS_ZERO = 0; //ALWAYS ZERO! IT IS SAFE TO GET RID OF ANY CODE FOR OTHER PATHS.

    // Native global `_g_screenRect`, initialized by CMainWindow::New @00481EFC.
    public static final CRect screenRect = new CRect();
    // Native global `_g_mainWindowRect`, initialized by CMainWindow::New @00481EFC.
    public static final CRect mainWindowRect = new CRect();
    public static Screen screen;

    public static final ClassNameIndex classNameIndex = ClassNameIndex.get();
    // Native global g_SoundPreferences @00622688.
    // Static lifecycle thunks @0047409D/@004740AC/@004740BB/@004740CD are represented by Java object lifecycle.
    public static final SoundPreferences soundPreferences = new SoundPreferences();
    // Native global g_hasCdAlwaysOne @0062282C.
    // Set by CMainApp::InitInstance @00480C8D and the unreferenced ForceValidGameMediaFlag @00474199 stub.
    public static final boolean hasValidGameMedia = true;
    // Native global INT_005F17E4, cleared by CMainApp::InitInstance @00481223 when video.res is unavailable.
    public static boolean videoResourcesAvailable = true;
    // Native global g_CGameFileManager, constructed by CGameFileManager::New @004E24F6.
    public static CGameFileManager gameFileManager = CGameFileManager.Init("~/Documents/ROM2");
    public static final MaterialRuntimeData materialRuntimeData = new MaterialRuntimeData();
    public static final CStaticDataMgr staticDataMgr = CStaticDataMgr.getInstance();
    // Native global g_GamePreferences @00622758.
    // Static lifecycle thunks @004740DC/@004740EB/@004740FA/@0047410C are represented by Java object lifecycle.
    public static final GamePreferences gamePreferences = new GamePreferences();
    public static final ServerConfig serverConfig = new ServerConfig();
    // Native global PasswordManager_00668338, used by GameServer::handleServerGameAction @004F515D login requests.
    public static final PasswordManager passwordManager = new PasswordManager();
    public static final ScenarioLib scenarioLib = new ScenarioLib();
    public static final ItemNames itemNames = new ItemNames();
    public static final GameFonts fonts = new GameFonts();
    // Native global g_QuestsStorage @0068D360, constructed by FUN_004E954B and consumed by GameServer::runServerLoopTick @004F08C0.
    public static final QuestsStorage questStorage = new QuestsStorage();
    public static CWorldMap worldMap;
    public static CGameLighting lighting = new CGameLighting();
    public static CMousePointer mousePointer = new CMousePointer();
    // Native support latch for DAT_006227e4, written by CMainWindow::WindowProc @004852D8.
    public static boolean leftButtonPressed;
    // Native global keyCONTROL, consumed by MapVisualObject::SelectMapCursor @0040B2B8.
    public static boolean controlKeyDown;
    // Native global keyALT, consumed by MapVisualObject::SelectMapCursor @0040B2B8.
    public static boolean altKeyDown;
    // Native global keySHIFT, consumed by MapVisualObject::SelectMapCursor @0040B2B8.
    public static boolean shiftKeyDown;
    // Native GetAsyncKeyState(VK_ESCAPE) support for CMainWindow::WindowProc @004852D8.
    public static boolean escapeKeyDown;
    // Native global mainMenuDisabledButtonMask @00627590, read by CMainWindow::showMainMenu @0048B569.
    public static int mainMenuDisabledButtonMask;
    // Native global DAT_00622824, used by CMainWindow::WindowProc @004852D8 piece 4 as the multiplayer bootstrap status word.
    public static int multiplayerBootstrapStatusWord;
    // Native global g_Latency @006275A0, initialized by CMainApp::InitInstance @00480C8D.
    public static int networkLatencyMillis;
    // Native global g_Timeout @005F2D94, initialized by CMainApp::InitInstance @00480C8D.
    public static int networkTimeoutMillis = 15_000;
    // Native support for CWinApp::m_lpCmdLine reads from CMainWindow::InitializeNewCampaignSession @0048C0E2.
    public static String commandLine = "";
    // Native global g_resolution @00627488, loaded by CMainApp::InitInstance @00480EA9 and read by CMainWindow::New @004820EC.
    public static String resolutionPreference = "-1024";
    // Native global DAT_00622828, cleared by MapVisualObject::HandleGameAction @0040D9B2 after map load.
    public static int mapLoadActionStatus;
    // Native global DAT_0062280c, written by MapVisualObject::HandleGameAction @0040EC8C.
    public static int terrainLightOverrideTransferMode;
    // Native global g_CurrentMusicTrack @005F1A08, written by updatePreferredGameplayTrackIndex @004754E9 and read by CMainWindow::playGameplayMusicPlaylist @004924CD.
    public static int currentMusicTrack = -1;
    // Native global g_showRenderStats @00622804, read by MapVisualObject::RenderFrame @00406F43.
    public static int showRenderStats;
    // Native global g_showNetworkStats @00622808, read by MapVisualObject::RenderFrame @00406F43.
    public static int showNetworkStats;
    // Native global g_helpText @00622700, loaded by CMainApp::InitInstance @00480C8D and read by CMainWindow::WindowProc @004852D8.
    // Static CString lifecycle thunks @00473EE0/@00473EEF/@00473EFE/@00473F10 are represented by Java object lifecycle.
    public static String helpText = "";
    // Native global usingVxD @005F1A04, loaded/saved by Global::loadApplicationPreferences @00440BB7 and Global::saveApplicationPreferences @00440CD2.
    public static int usingVxD;
    // Native global DWORD_006275ac, used by CMainWindow::pumpRemoteGameplayIdle @00488E25.
    public static int lastRemoteServerLoopCounterBroadcastTick;
    // Native global IsTraceEnabled @00622810, written by CMainApp::InitInstance @00480C8D and read by CMainWindow::pumpRemoteGameplayIdle @00488E25.
    public static boolean traceDiagnosticsEnabled;
    // Native global g_GameServer @006044c4.
    public static final GameServer gameServer = new GameServer();

    public static Renderer renderer;

    // not ported. Presents the current software surface through the active platform shell.
    public static Runnable presentCurrentSurface = () -> {
    };

    // not ported. Returns whether a blocking playback loop should stop pumping frames.
    public static BooleanSupplier shouldAbortBlockingPlayback = () -> false;

    // not ported. Suppresses normal input routing while the blocking SMK playback loop is active.
    public static boolean blockingPlaybackActive;

    // not ported. Latches native-style mouse/key interruption for the blocking SMK playback loop.
    public static boolean blockingPlaybackAbortRequested;

    public static CMainWindow mainWindow;
    // Native global at 0x005D4B2C.
    public static int isWindowed = 0;

    /**
     * Native support extracted from timeGetTime/GetTickCount users including
     * CGameSession::submitCharacterSetupAndWaitForSelectedUnit @0049183E and
     * CMainWindow::runSessionBootstrap @0048C8A3.
     */
    public static int currentTickMillis() {
        return (int) System.currentTimeMillis();
    }
}
