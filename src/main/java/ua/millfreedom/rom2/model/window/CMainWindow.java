package ua.millfreedom.rom2.model.window;

import lombok.extern.slf4j.Slf4j;
import ua.millfreedom.rom2.*;
import ua.millfreedom.rom2.CFile.LEWriter;
import ua.millfreedom.rom2.dserver.DedicatedServerPlayerStatus;
import ua.millfreedom.rom2.dserver.DedicatedServerStatusSnapshot;
import ua.millfreedom.rom2.model.*;
import ua.millfreedom.rom2.model.action.ChatTextAction;
import ua.millfreedom.rom2.model.action.LoginRequestAction;
import ua.millfreedom.rom2.model.control.*;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.enums.ProtocolId;
import ua.millfreedom.rom2.model.gameobj.CGameObject;
import ua.millfreedom.rom2.model.gameobj.CProjectile;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.net.CBufferManager;
import ua.millfreedom.rom2.model.net.CLlDriver;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.sound.*;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.video.SMKPlayer;
import ua.millfreedom.rom2.model.visobj.*;
import ua.millfreedom.rom2.model.window.windowproc.handlers.*;
import ua.millfreedom.rom2.model.world.ScenarioLocation;
import ua.millfreedom.rom2.model.world.ScenarioProgressSupport;
import ua.millfreedom.rom2.model.world.TerrainGraphics;
import ua.millfreedom.rom2.res.ResInHeap;
import ua.millfreedom.rom2.text.CutPathsText;
import ua.millfreedom.rom2.text.GameTexts;
import ua.millfreedom.rom2.text.MainText;
import ua.millfreedom.rom2.text.PatchText;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;

import static ua.millfreedom.rom2.model.enums.MessageCodes.*;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.*;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.*;
import static ua.millfreedom.rom2.text.StringTableIndex.*;
import static ua.millfreedom.rom2.text.TextTableId.MAIN;

@Slf4j
public class CMainWindow extends CFrameWnd {
    public static final int MAIN_WINDOW_WIDTH = 640;
    public static final int MAIN_WINDOW_HEIGHT = 480;
    private static final String RESOLUTION_640 = "-640";
    private static final String RESOLUTION_800 = "-800";
    private static final String RESOLUTION_1024 = "-1024";
    public static final int SESSION_MODE_MULTIPLAYER_CLIENT = 0;
    public static final int SESSION_MODE_NETWORK_HOST = 1;
    public static final int SESSION_MODE_CAMPAIGN = 2;
    public static final int SESSION_MODE_DEDICATED_SERVER = 3;
    private static final int DEFAULT_MUSIC_BUFFER_BYTES = 0xAC000;
    private static final int NATIVE_SOUND_CHANNEL_COUNT = 32;
    private static final int CUTSCENE_SEGMENT_LIMIT_EXCLUSIVE = 100;
    private static final int INVALID_MEDIA_LOGO_WAIT_MILLIS = 5000;
    private static final int SYSKEY_ALT_CONTEXT_BIT = 0x2000;
    private static final String INVALID_MEDIA_ALLODS_LOGO_BMP = "main/graphics/logo/allods.bmp";
    private static final String STARTUP_PUBLISHER_LOGO_SMK = "video/logos/publisher.smk";
    private static final String STARTUP_NIVAL_LOGO_SMK = "video/logos/nival.smk";
    private static final int SCENARIO_CHAPTER_VAR_ID = 0x300;
    private static final int MAP_LOAD_TIMEOUT_STATUS_WORD = 0x1005;
    private static final int MULTIPLAYER_BOOTSTRAP_STATUS_MAIN_TEXT_BASE = 0xC0;
    private static final int SAVE_FILE_TITLE_BLOCK_SIZE = 0x100;
    private static final Path FAME_HALL_FILE = Path.of("famehall.dat");
    private static final Path TRACE_ERROR_LOG = Path.of("error.log");
    private static final DateTimeFormatter TRACE_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm:ss ");
    private static final String TRACE_CONNECTION_LOST = "Connection lost";
    private static final int TRACE_CONNECTION_LOST_LINE_LIFETIME_MS = 30000;
    private static final int DEDICATED_SERVER_STATUS_CONTROL_ID = 0x7DE0;
    private static final int DEDICATED_SERVER_PLAYER_DETAILS_CONTROL_ID = 0x7DDF;
    private static final String DEDICATED_SERVER_DISCONNECTED_PLAYER_PREFIX = "(-)";
    private static final String DEDICATED_SERVER_DETAILS_SEPARATOR = "\n";
    private static final String DEDICATED_SERVER_TRAFFIC_DURATION_FORMAT = "%d:%02d:%02d";
    private static final String DEDICATED_SERVER_TRAFFIC_BYTES_FORMAT = "%d/%d/%d";
    private static final String MAP_NOT_LOADED_STATUS_TEXT = "Map not loaded";
    private static final int STATUS_BAR_CONTROL_ID = 0xE801;
    private static final int DEDICATED_SERVER_LOG_LIST_CONTROL_ID = 0x7DDE;
    private static final int DEDICATED_SERVER_MESSAGE_EDIT_CONTROL_ID = 0x7DE1;
    private static final int DEDICATED_SERVER_PLAYER_LIST_CONTROL_ID = 0x7DE2;
    private static final int STATUS_BAR_CREATE_STYLE = 0x50008200;
    private static final int SERVER_LOG_LIST_CREATE_STYLE = 0x50600040;
    private static final int SERVER_PLAYER_DETAILS_CREATE_STYLE = 0x5040000C;
    private static final int SERVER_STATUS_CREATE_STYLE = 0x50400000;
    private static final int SERVER_MESSAGE_EDIT_CREATE_STYLE = 0x50400480;
    private static final int SERVER_PLAYER_LIST_CREATE_STYLE = 0x50700040;
    private static int allodsBmpCaptureIndex;

    //0xbc
    public int haveFocus = 1;
    //0xc0
    public int chatOpen;
    //0xc4 Java models the native updateMusicStreaming callback pointer as a nullable no-arg callback.
    public Runnable musicUpdater;
    //0xc8
    public MusicPlayer musicPlayer;
    //0xcc
    public CVisualObject inputController;
    //0xd0
    public MapVisualObject pMapVisualObject;
    //0xd4
    public RightPanelContainerVisualObject pRightPanelContainerVisualObject;
    //0xd8
    public MinimapVisualObject pMinimapVisualObject;
    //0xdc
    public OrderToolbarVisualObject pOrderToolbarVisualObject;
    //0xe0
    public SelectionInfoPanelVisualObject pSelectionInfoPanelVisualObject;
    //0xe4
    public SideStatusVisualObject pSideStatusVisualObject;
    //0xe8
    public HeroInventoryControlVisualObject pHeroInventoryControlVisualObject;
    //0xec
    public SpellPanelVisualObject pSpellPanelVisualObject;
    //0xf0
    public GlobalMapDialogVisualObject pGlobalMapDialogVisualObject;
    //0xf4
    public MainMenuVisualObject pMainMenuVisualObject;
    //0xf8
    public ShopDialogVisualObject pShopDialogVisualObject;
    //0xfc
    public DruidShopDialogVisualObject pDruidShopDialogVisualObject;
    //0x100
    public KaargShopDialogVisualObject pKaargShopDialogVisualObject;
    //0x104
    public BasicInnDialogVisualObject pBasicInnDialogVisualObject;
    //0x108
    public DruidInnDialogVisualObject pDruidInnDialogVisualObject;
    //0x10c
    public KaargInnDialogVisualObject pKaargInnDialogVisualObject;
    //0x110
    public BasicTownDialogVisualObject pBasicTownDialogVisualObject;
    //0x114
    public DruidTownDialogVisualObject pDruidTownDialogVisualObject;
    //0x118
    public KaargTownDialogVisualObject pKaargTownDialogVisualObject;
    //0x11c
    public FameHallDialogVisualObject pFameHallDialogVisualObject;
    //0x120
    public DropGoldPromptVisualObject pDropGoldPromptVisualObject;
    //0x124
    public MissionFailedHeaderDialogVisualObject pMissionFailedHeaderDialogVisualObject;
    //0x128
    public TwoActionMenuListDialogVisualObject pTwoActionMenuListDialogVisualObject;
    //0x12c
    public HeaderDialogVariantVisualObject pTownReturnPromptDialogVisualObject;
    //0x130
    public CreditsDialogVisualObject pCreditsDialogVisualObject;
    //0x134
    public StartupLogoDialogVisualObject pStartupLogoDialogVisualObject;
    //0x138
    public ChatVisualObject pChatVisualObject;
    //0x13c
    public LoadDialogVisualObject pLoadDialogVisualObject;
    //0x140
    public SaveDialogVisualObject pSaveDialogVisualObject;
    //0x144
    public DiplomacySettingsDialogVisualObject pDiplomacySettingsDialogVisualObject;
    //0x148
    public SaveFile mSaveFile = new SaveFile();
    //0x348
    public DiplomacyWrapper m_Dilpomacy = new DiplomacyWrapper();
    //0x34c
    public CenteredDialogContextArrayVisualObject pCenteredDialogContextArrayVisualObject;
    //0x350
    public MpConnectionDialogVisualObject m_pMPConnectionDialog;
    //0x354
    public MultiplayerSessionDialogContext multiplayerSessionDialogContext = new MultiplayerSessionDialogContext();
    //0x368
    public int LastProtocol;
    //0x36c
    public CharacterGeneratorDialogVisualObject pCharacterGeneratorDialogVisualObject;
    //0x370
    public CharacterLoaderDialogVisualObject pCharacterLoaderDialogVisualObject;
    //0x374
    public StartGameSetupDialogVisualObject pStartGameSetupDialogVisualObject;
    //0x378
    public MultiplayerMapSelectionDialogVisualObject pMultiplayerMapSelectionDialogVisualObject;
    //0x37c
    public HatServerBrowserDialogVisualObject pHatServerBrowserDialogVisualObject;
    //0x380
    public PhoneBook PhoneBook = new PhoneBook();
    //0x39c
    public ComPortSettings serialSettings = new ComPortSettings();
    //0x3b0
    public CString lastIP = new CString(0x100);
    //0x3b4
    public Hat Hat = new Hat();
    //0x3bc
    public DedicatedServerConsoleVisualObject pDedicatedServerConsoleVisualObject;
    //0x3c0
    public ViewCutscenesHeaderDialogVisualObject pViewCutscenesHeaderDialogVisualObject;
    //0x3c4
    public FameHallDocumentDialogVisualObject pFameHallDocumentDialogVisualObject;
    //0x3c8
    public HeaderDialogVariantVisualObject pHeaderDialogVariantVisualObject;
    //0x3cc
    public MainWindowConnectionScratchState connectionScratchState = new MainWindowConnectionScratchState();
    //0x3e4
    public int completedMissionExitPending;
    //0x3e8
    public int missionFailureDialogShown;
    //0x3ec
    public byte field62_0x3ec;
    //0x3ed
    public byte field63_0x3ed;
    //0x3ee
    public byte field64_0x3ee;
    //0x3ef
    public byte field65_0x3ef;
    //0x3f0
    public int scenarioCameraOverrideLock;
    //0x3f4
    public Object uiLockPayload;
    //0x3f8
    public int uiLockSourceIndex = -1;
    //0x3fc
    public int uiLockPackedModeCode;
    //0x400
    public CCursor cursor;
    //0x404 initialized to zero by CMainWindow::New @004822AB.
    public int dialogsMask;
    //0x408 server loop counter used by MapVisualObject::RefreshTimeFlowLighting @0041D3B9.
    public int serverLoopCounter;
    //0x40c
    public int m_FrameCounter;
    //0x414
    public int m_LastTickTime = Integer.MAX_VALUE;
    //0x418
    public int m_TickInterval;
    //0x41c
    public int gameSpeed = 4;
    //0x424
    public int m_LastRenderTime = Integer.MAX_VALUE;
    //0x428
    public int m_LagAccumulator;
    //0x42c
    public int field90_0x42c;
    //0x434
    public int singleStepGameplayTickMode;
    //0x438 initialized to zero by CMainWindow::New @004822B8.
    public int field90_0x438;
    //0x43c
    public int questEventDialogId;
    //0x440
    public int multiplayerRefreshGamesPending;
    //0x444
    public int fileTransferChecksumMatched;
    //0x448
    public int fileTransferDownloadPending;
    //0x44c
    public int field149_0x44C;
    //0x450
    public CGameSession m_GameSession = new CGameSession();
    //0x598
    public CFameHall m_FameHall = new CFameHall();
    //0x5d0
    public CString map_ = new CString(0x100);
    //0x5d4
    public int serverBootstrapEnabled;
    //0x5d8
    public int sessionMode = SESSION_MODE_CAMPAIGN;
    //0x5dc
    public CRect clipRect = new CRect();
    //0x5ec
    public CStatusBar cStatusBar = new CStatusBar();
    //0x668
    public CListBox cListBox1 = new CListBox();
    //0x6a4
    public GameListBox gameListBox = new GameListBox();
    //0x6e0
    public CStatic cStatic1 = new CStatic();
    //0x71c
    public CStatic cStatic2 = new CStatic();
    //0x758
    public GameEdit gameEdit = new GameEdit();
    //0x794
    public DedicatedServerControlDialog pDedicatedServerControlDialog;
    //0x798
    public int dedicatedServerControlDialogCreated;

    // Java support, not a native field.
    private boolean windowCloseRequested;

    // Java support, not a native field.
    private static final Map<MessageCodes, MessageHandler<CMainWindow>> handlers =
            Map.<MessageCodes, MessageHandler<CMainWindow>>ofEntries(
                    Map.entry(WM_CLOSE, WmCloseMessageHandler::handle),
                    Map.entry(WM_MOUSEMOVE, MouseMoveMessageHandler::handle),
                    Map.entry(WM_SIZE, SizeMessageHandler::handle),
                    Map.entry(WM_SETFOCUS, SetFocusMessageHandler::handle),
                    Map.entry(WM_SETCURSOR, SetCursorMessageHandler::handle),
                    Map.entry(WM_KEYUP, KeyUpMessageHandler::handle),
                    Map.entry(WM_SYSKEYDOWN, SysKeyDownMessageHandler::handle),
                    Map.entry(WM_SYSKEYUP, SysKeyUpMessageHandler::handle),
                    Map.entry(WM_COMMAND, CommandMessageHandler::handle),
                    Map.entry(WM_USER, UserMessageHandler::handle),
                    Map.entry(WM_LBUTTONDOWN, LeftButtonDownMessageHandler::handle),
                    Map.entry(WM_LBUTTONUP, LeftButtonUpMessageHandler::handle),
                    Map.entry(WM_KEYDOWN, KeyDownMessageHandler::handle),
                    Map.entry(ESC_MENU, EscMenuMessageHandler::handle),
                    Map.entry(LOAD_GAME, LoadGameMessageHandler::handle),
                    Map.entry(LOAD_GAME_CREATE, LoadGameCreateMessageHandler::handle),
                    Map.entry(SAVE_GAME, SaveGameMessageHandler::handle),
                    Map.entry(GAME_OPTIONS, GameOptionsMessageHandler::handle),
                    Map.entry(END_QUEST, EndQuestMessageHandler::handle),
                    Map.entry(EXIT_MAP, ExitMapMessageHandler::handle),
                    Map.entry(EXIT_TO_MENU, ExitToMenuMessageHandler::handle),
                    Map.entry(SHOW_TOWN_MENU, ShowTownMenuMessageHandler::handle),
                    Map.entry(QUEST_OBJECTIVES, QuestObjectivesMessageHandler::handle),
                    Map.entry(SHOW_MAIN_MENU, ShowMainMenuMessageHandler::handle),
                    Map.entry(SOUND_OPTIONS, SoundOptionsMessageHandler::handle),
                    Map.entry(START_NEW_GAME, StartNewGameMessageHandler::handle),
                    Map.entry(SHOW_CHARACTER_LOADER_DIALOG, CharacterLoaderDialogMessageHandler::handle),
                    Map.entry(SHOW_STARTUP_LOGO_DIALOG, ShowStartupLogoDialogMessageHandler::handle),
                    Map.entry(SHOW_CREDITS_DIALOG, CreditsDialogMessageHandler::handle),
                    Map.entry(SHOW_FAME_HALL_DIALOG, FameHallDialogMessageHandler::handle),
                    Map.entry(SHOW_SHOP_DIALOG, ShowShopDialogMessageHandler::handle),
                    Map.entry(SHOW_INN_DIALOG, ShowInnDialogMessageHandler::handle),
                    Map.entry(SHOW_GLOBAL_MAP_DIALOG, GlobalMapTransitionMessageHandler::handle),
                    Map.entry(SHOW_CURRENT_TOWN_DIALOG, ShowCurrentTownDialogMessageHandler::handle),
                    Map.entry(RUN_SESSION_BOOTSTRAP, RunSessionBootstrapMessageHandler::handle),
                    Map.entry(SHOW_MISSION_COMPLETED_DIALOG, ShowMissionCompletedDialogMessageHandler::handle),
                    Map.entry(SHOW_MISSION_FAILED_DIALOG, ShowMissionFailedDialogMessageHandler::handle),
                    Map.entry(CLOSE_AFTER_MISSION_FAILURE, CloseAfterMissionFailureMessageHandler::handle),
                    Map.entry(HANDLE_QUEST_EVENT_DIALOG, HandleQuestEventDialogMessageHandler::handle),
                    Map.entry(SHOW_HELP_TEXT_DIALOG, ShowHelpTextDialogMessageHandler::handle),
                    Map.entry(INITIALIZE_RUNTIME_GRAPHICS_AND_AUDIO, InitializeRuntimeGraphicsAndAudioMessageHandler::handle),
                    Map.entry(PLAY_LOCATION_CUTSCENE_AND_CLOSE, PlayLocationCutsceneAndCloseMessageHandler::handle),
                    Map.entry(STARTUP_LOGO_STEP_COMPLETE, StartupLogoStepCompleteMessageHandler::handle),
                    Map.entry(VIEW_CUTSCENES, ViewCutscenesMessageHandler::handle),
                    Map.entry(DIPLOMACY, DiplomacyMessageHandler::handle),
                    Map.entry(START_HAT_DEDICATED_SERVER, StartHatDedicatedServerMessageHandler::handle),
                    Map.entry(CONNECT_TO_SERVER_ADDRESS, ConnectToServerAddressMessageHandler::handle),
                    Map.entry(WRITE_CURRENT_MISSION_RESUME_SAVE, WriteCurrentMissionResumeSaveMessageHandler::handle),
                    Map.entry(ENTER_MODAL_DIALOG_MODE, EnterModalDialogModeMessageHandler::handle),
                    Map.entry(NOTIFY_DIALOG_CLOSED, NotifyDialogClosedMessageHandler::handle),
                    Map.entry(SHOW_MULTIPLAYER_CONNECTION_DIALOG, ShowMultiplayerConnectionDialogMessageHandler::handle),
                    Map.entry(SHOW_MULTIPLAYER_SESSION_DIALOG, ShowMultiplayerSessionDialogMessageHandler::handle),
                    Map.entry(SHOW_TCP_IP_SETTINGS_DIALOG, ShowTcpIpSettingsDialogMessageHandler::handle),
                    Map.entry(SHOW_SERIAL_SETTINGS_DIALOG, ShowSerialSettingsDialogMessageHandler::handle),
                    Map.entry(SHOW_MODEM_SETTINGS_DIALOG, ShowModemSettingsDialogMessageHandler::handle),
                    Map.entry(RUN_MULTIPLAYER_SESSION_BOOTSTRAP, RunMultiplayerSessionBootstrapMessageHandler::handle),
                    Map.entry(PREPARE_MULTIPLAYER_MAP_SELECTION, PrepareMultiplayerMapSelectionMessageHandler::handle),
                    Map.entry(CLEAR_TIP_PROMPT, ClearTipPromptMessageHandler::handle),
                    Map.entry(SHOW_TIP_BY_ID, ShowTipByIdMessageHandler::handle),
                    Map.entry(RETURN_TO_MULTIPLAYER_SETUP, ReturnToMultiplayerSetupMessageHandler::handle),
                    Map.entry(CLIENT_RETURN_TO_MAP_SELECTION, ClientReturnToMapSelectionMessageHandler::handle),
                    Map.entry(SHOW_FAME_HALL_DOCUMENT_DIALOG, ShowFameHallDocumentDialogMessageHandler::handle),
                    Map.entry(SHOW_INVALID_CD_PROMPT, InvalidCdPromptMessageHandler::handle),
                    Map.entry(SHOW_INVALID_CD_PROMPT_CREATE, InvalidCdPromptCreateMessageHandler::handle),
                    Map.entry(TOGGLE_QUEST_STATUS_DIALOG, ToggleQuestStatusDialogMessageHandler::handle),
                    Map.entry(CONTINUE_SCENARIO_LOCATION_ENTRY, ContinueScenarioLocationEntryMessageHandler::handle),
                    Map.entry(APPEND_DEDICATED_SERVER_LOG_LINE, AppendDedicatedServerLogLineMessageHandler::handle),
                    Map.entry(SELECT_MODEM_HOST_DRIVER, SelectModemHostDriverMessageHandler::handle),
                    Map.entry(SELECT_SERIAL_HOST_DRIVER, SelectSerialHostDriverMessageHandler::handle),
                    Map.entry(EXIT_GAME, ExitGameMessageHandler::handle),
                    Map.entry(SHOW_HAT_SERVER_LIST_DIALOG, ShowHatServerListDialogMessageHandler::handle),
                    Map.entry(SHOW_HAT_SERVER_BROWSER_DIALOG, ShowHatServerBrowserDialogMessageHandler::handle)
            );

    /**
     * Native: CMainWindow::New @00481EFC.
     * Java port status: fully ported at the Java platform window boundary; native HWND creation is handled by CMainApp.
     */
    public CMainWindow() {
        initializeNativeConstructionState();
    }

    /**
     * Native support extracted from CMainWindow::New @00482045, @00482156, @0048224D, and @0048236B.
     */
    private void initializeNativeConstructionState() {
        inputController = null;
        initializeNativeLogicalScreenGeometry();
        pDedicatedServerControlDialog = null;
        dedicatedServerControlDialogCreated = 0;
        musicPlayer = null;
        uiLockPayload = null;
        uiLockSourceIndex = -1;
        cursor = null;
        musicUpdater = null;
        dialogsMask = 0;
        field90_0x438 = 0;
        gameSpeed = 4;
        m_LastRenderTime = Integer.MAX_VALUE;
        m_LastTickTime = Integer.MAX_VALUE;
        singleStepGameplayTickMode = 0;
        serverBootstrapEnabled = 0;
        scenarioCameraOverrideLock = 0;
        pDiplomacySettingsDialogVisualObject = null;
        multiplayerRefreshGamesPending = 0;
        pCenteredDialogContextArrayVisualObject = null;
        pHeaderDialogVariantVisualObject = null;
        pMultiplayerMapSelectionDialogVisualObject = null;
        pHatServerBrowserDialogVisualObject = null;
        pMapVisualObject = null;
        connectionScratchState.directAddress = "";
        connectionScratchState.loginName = "";
        connectionScratchState.acceptedCharacterFileOwnerId = 0;
        connectionScratchState.directAddressLoginAccepted = false;
        connectionScratchState.serverListSourceIsWebPage = 0;
    }

    /**
     * Native support extracted from CMainWindow::New @0048205B and @00482156 logical screen geometry writes.
     */
    public static void initializeNativeLogicalScreenGeometry() {
        CRect screenRect = resolveNativeScreenRect(Globals.commandLine, Globals.resolutionPreference);
        Globals.screenRect.set(screenRect);
        int mainWindowLeft = (screenRect.right - MAIN_WINDOW_WIDTH) / 2;
        int mainWindowTop = (screenRect.bottom - MAIN_WINDOW_HEIGHT) / 2;
        Globals.mainWindowRect.set(
                mainWindowLeft,
                mainWindowTop,
                screenRect.right - mainWindowLeft,
                screenRect.bottom - mainWindowTop
        );
    }

    /**
     * Native support extracted from CMainWindow::New @0048205B resolution selection.
     */
    private static CRect resolveNativeScreenRect(String commandLine, String resolutionPreference) {
        if (Globals.isWindowed != 0) {
            return new CRect(0, 0, MAIN_WINDOW_WIDTH, MAIN_WINDOW_HEIGHT);
        }
        if (commandLine.contains(RESOLUTION_800)) {
            return new CRect(0, 0, 800, 600);
        }
        if (commandLine.contains(RESOLUTION_1024)) {
            return new CRect(0, 0, 1024, 768);
        }
        if (commandLine.contains(RESOLUTION_640)) {
            return new CRect(0, 0, MAIN_WINDOW_WIDTH, MAIN_WINDOW_HEIGHT);
        }
        if (resolutionPreference.contains(RESOLUTION_800)) {
            return new CRect(0, 0, 800, 600);
        }
        if (resolutionPreference.contains(RESOLUTION_1024)) {
            return new CRect(0, 0, 1024, 768);
        }
        return new CRect(0, 0, MAIN_WINDOW_WIDTH, MAIN_WINDOW_HEIGHT);
    }

    /**
     * Native support extracted from GridOverlayVisualObject::BeginUiDrag @004A235D,
     * GridOverlayVisualObject::CompleteUiDrag @004A24E8, and CMainWindow::ClearUiLockState @0048AE1E.
     */
    public int getUiLockFlag3f4() {
        if (uiLockPayload instanceof Number number) {
            return number.intValue();
        }
        return uiLockPayload == null ? 0 : 1;
    }

    /**
     * Native support extracted from GridOverlayVisualObject::BeginUiDrag @004A235D,
     * GridOverlayVisualObject::CompleteUiDrag @004A24E8, and CMainWindow::ClearUiLockState @0048AE1E.
     */
    public Object getUiLockPayload() {
        if (uiLockPayload instanceof Number) {
            return null;
        }
        return uiLockPayload;
    }

    /**
     * Native support extracted from GridOverlayVisualObject::BeginUiDrag @004A235D and CMainWindow::ClearUiLockState @0048AE1E.
     */
    public void setUiLockPayload(Object payload) {
        uiLockPayload = payload;
    }

    /**
     * Native owner: CMainWindow::uiLockSourceIndex source index in grid drag flows.
     * not ported.
     */
    public int getUiLockSourceIndex() {
        return uiLockSourceIndex;
    }

    /**
     * Native owner: CMainWindow::uiLockSourceIndex source index in grid drag flows.
     * not ported.
     */
    public void setUiLockSourceIndex(int index) {
        uiLockSourceIndex = index;
    }

    /**
     * Native support extracted from GridOverlayVisualObject::BeginUiDrag @004A235D,
     * GridOverlayVisualObject::CompleteUiDrag @004A24E8, and CMainWindow::ClearUiLockState @0048AE1E.
     */
    public int getUiLockPackedModeCode() {
        return uiLockPackedModeCode;
    }

    /**
     * Native support extracted from GridOverlayVisualObject::BeginUiDrag @004A235D and CMainWindow::ClearUiLockState @0048AE1E.
     */
    public void setUiLockPackedModeCode(int packedModeCode) {
        uiLockPackedModeCode = packedModeCode;
    }

    /**
     * Native: CMainWindow::BeginShopGridDragVisual @0048AD7B.
     * Native creates a 0x28x0x28 cursor from caller-provided spritePath, then captures the UI-lock payload/index/mode.
     * Fully ported.
     */
    public void beginShopGridDragVisual(Object payload, int sourceIndex, String spritePath, int modeCode) {
        cursor = new CCursor(spritePath, 0x28, 0x28, 1000000000);
        uiLockSourceIndex = sourceIndex;
        uiLockPackedModeCode = modeCode;
        uiLockPayload = payload;
    }

    /**
     * Native owner: keyboard modifier checks in SpellPanelVisualObject::OnKeyDown @004C74B1.
     * not ported.
     */
    public boolean isControlKeyDown() {
        return Globals.controlKeyDown;
    }

    /**
     * Native support extracted from GridOverlayVisualObject::BeginUiDrag @004A235D.
     */
    public boolean isGridOverlayContextCurrentPlayer() {
        CGameObject selectedObject = getGridOverlayBindingContext();
        return selectedObject.cPlayer == pMapVisualObject.currentPlayer;
    }

    /**
     * Native support extracted from GridOverlayVisualObject::BeginUiDrag @004A235D.
     */
    public CGameObject getGridOverlayBindingContext() {
        return pMapVisualObject.getPrimarySelectedObjectForGridOverlay();
    }

    /**
     * Native support extracted from GridOverlayVisualObject::CompleteUiDrag @004A24E8 and
     * MapVisualObject::sendInventoryTransferAction @0041A20C drop-commit callback.
     */
    public void onGridOverlayDropCommitted(
            int sourcePackedModeCode,
            int sourceIndex,
            int targetPackedModeCode,
            int targetIndex,
            int quantity
    ) {
        pMapVisualObject.sendInventoryTransferAction(
                sourcePackedModeCode,
                sourceIndex,
                targetPackedModeCode,
                targetIndex,
                quantity
        );
    }

    /**
     * Native owner: player-slot color/type lookup in SpellPanelVisualObject::OnMessage @004C72F5 and
     * SpellPanelVisualObject::Update @004C6AC1 player marker loop.
     */
    public int getAssignedSpellPanelSlot(int playerIndex) {
        PlayerSlot playerSlot = m_GameSession.m_PlayerSlots[playerIndex];
        return playerSlot.type == 1 ? Short.toUnsignedInt(playerSlot.color) : -1;
    }

    /**
     * Native support extracted from SpellPanelVisualObject::OnMessage @004C72F5 and
     * PlayerSlot::AssignSpellPanelSlot @0041DD5E.
     * Fully ported.
     */
    public void assignSpellPanelSlotToPlayer(int playerIndex, int spellSlot) {
        PlayerSlot playerSlot = m_GameSession.m_PlayerSlots[playerIndex];
        playerSlot.assignSpellPanelSlot(spellSlot);
    }

    /**
     * Native owner: duplicate-assignment cleanup in SpellPanelVisualObject::OnMessage @004C72F5.
     * Native support includes PlayerSlot::MatchesSpellPanelSlot @0041DE8F and the in-place
     * PlayerSlot::PlayerSlot reset @00493B10.
     */
    public void clearSpellPanelSlotAssignmentsForOtherPlayers(int playerIndex, int spellSlot) {
        for (int otherPlayerIndex = 0; otherPlayerIndex < m_GameSession.m_PlayerSlots.length; otherPlayerIndex++) {
            PlayerSlot playerSlot = m_GameSession.m_PlayerSlots[otherPlayerIndex];
            if (otherPlayerIndex != playerIndex && playerSlot.matchesSpellPanelSlot(spellSlot)) {
                playerSlot.reset();
            }
        }
    }

    /**
     * Native owner: game-session refresh after spell-slot assignment in SpellPanelVisualObject::OnMessage @004C72F5.
     */
    public void refreshSpellPanelPlayerAssignments() {
        m_GameSession.refreshSavedPlayerSlots();
    }

    /**
     * Native: CMainWindow::ClearUiLockState @0048AE1E.
     * Fully ported. Java drops the cursor reference instead of emulating native cursor deletion.
     */
    public void clearUiLockState() {
        if (getUiLockFlag3f4() == 0) {
            return;
        }
        cursor = null;
        uiLockPayload = null;
        uiLockSourceIndex = -1;
        uiLockPackedModeCode = -1;
    }

    /**
     * Native: CMainWindow::initializeNewCampaignSession @0048C0E2.
     * Fully ported.
     */
    public void initializeNewCampaignSession() {
        field62_0x3ec = 1;
        field63_0x3ed = 0;
        field64_0x3ee = 0;
        field65_0x3ef = 0;
        sessionMode = SESSION_MODE_CAMPAIGN;
        int result = createServer(sessionMode);
        if (result != 0) {
            throw new IllegalStateException("Suxx");
        }
        m_GameSession.type = 0;
        String commandLine = Globals.commandLine;
        if (commandLine.contains("-female")) {
            m_GameSession.type |= CGameSession.SESSION_TYPE_FEMALE;
        }
        if (commandLine.contains("-mage")) {
            m_GameSession.type |= CGameSession.SESSION_TYPE_MAGE;
        }
        int nameIndex = commandLine.indexOf("-name");
        if (nameIndex == -1) {
            m_GameSession.m_PlayerName = "Unnamed";
        } else {
            String playerName = commandLine.substring(nameIndex + 5);
            if (playerName.length() > 0x1f) {
                playerName = playerName.substring(0, 0x1f);
            }
            int spaceIndex = playerName.indexOf(' ');
            if (spaceIndex != -1) {
                playerName = playerName.substring(0, spaceIndex);
            }
            m_GameSession.m_PlayerName = playerName;
        }
    }

    /**
     * Native: CMainWindow::ShowStartGameSetupForNewSession @0048F4E2.
     * Fully ported. Java preserves the recovered right-panel map-context update, start-game setup attachment,
     * player-name clearing for non-campaign modes, difficulty/portrait handoff, dialog activation, mode bit, cursor
     * reset, and character-generator music handoff.
     */
    public void showStartGameSetupForNewSession() {
        CMousePointer.Cursor_Wait.setToMousePointer();
        pRightPanelContainerVisualObject.onMessage(MessageCodes.SET_MAP_CONTEXT, pMapVisualObject, 0);
        inputController.addChild(pStartGameSetupDialogVisualObject);
        if (sessionMode != SESSION_MODE_CAMPAIGN) {
            m_GameSession.m_PlayerName = "";
        }
        pStartGameSetupDialogVisualObject.setLeaderName(m_GameSession.m_PlayerName);
        pStartGameSetupDialogVisualObject.setSelectedDifficulty(m_FameHall.getSelectedDifficulty());
        pStartGameSetupDialogVisualObject.setSelectedPortrait(m_GameSession.type >>> 6);
        pStartGameSetupDialogVisualObject.showDialog();
        inputController.draw();
        field149_0x44C = 0;
        dialogsMask = START_GAME_SETUP.includeTo(dialogsMask);
        CMousePointer.Cursor_Default.setToMousePointer();
        if (Globals.soundPreferences.musicAvailable != 0) {
            List<String> currentTracks = musicPlayer.getMusicFileNames();
            if (currentTracks.isEmpty() || !"music/chrgen.wav".equals(currentTracks.getFirst())) {
                musicPlayer.setMusicFileNames(List.of("music/chrgen.wav"));
            }
            musicPlayer.play();
        }
    }

    /**
     * Native: CMainWindow::pumpDedicatedServerIdle @00488CA5.
     * Fully ported.
     */
    public void pumpDedicatedServerIdle() {
        while (Integer.compareUnsigned(
                Globals.currentTickMillis(),
                m_LastTickTime + m_TickInterval * (m_FrameCounter + 1)
        ) >= 0) {
            if (m_FrameCounter == 0) {
                m_LastTickTime = Globals.currentTickMillis();
            }
            m_FrameCounter = (m_FrameCounter + 1) & 0xF;
            Globals.gameServer.runServerLoopTick();
            if (Globals.isWindowed != 0 && m_FrameCounter == 1) {
                refreshWindowedDedicatedServerStatus();
            }
            if (Globals.isWindowed == 0
                    && haveFocus != 0
                    && pDedicatedServerConsoleVisualObject != null
                    && m_FrameCounter == 1) {
                pDedicatedServerConsoleVisualObject.draw();
            }
            int mapDurationTicks = Globals.serverConfig.field12_0x70.get(Globals.serverConfig.field15_0x8c) * 0xE100;
            if (Integer.compareUnsigned(Globals.gameServer.serverLoopCounter, mapDurationTicks) > 0) {
                advanceDedicatedServerMapSchedule();
                break;
            }
        }
    }

    /**
     * Native: CMainWindow::refreshWindowedDedicatedServerStatus @004828D7.
     * Fully ported for the modeled windowed dedicated-server controls.
     */
    private void refreshWindowedDedicatedServerStatus() {
        if (Globals.gameServer == null) {
            return;
        }
        refreshDedicatedServerSummaryText();

        String previousSelection = gameListBox.getText(gameListBox.getCurSel());
        gameListBox.resetContent();
        for (Player player : Globals.gameServer.playerList.players) {
            if (player.isActive == 0) {
                String playerListEntry = player.name;
                if (player.clientConnected == 0) {
                    playerListEntry = DEDICATED_SERVER_DISCONNECTED_PLAYER_PREFIX + playerListEntry;
                }
                gameListBox.addString(playerListEntry);
            }
        }

        gameListBox.selectString(0, previousSelection);
        Player selectedPlayer = findInactivePlayerByListIndex(gameListBox.getCurSel());
        if (selectedPlayer == null) {
            setDlgItemText(DEDICATED_SERVER_PLAYER_DETAILS_CONTROL_ID, "");
        } else {
            setDlgItemText(DEDICATED_SERVER_PLAYER_DETAILS_CONTROL_ID, dedicatedServerPlayerDetailsText(selectedPlayer));
        }
    }

    /**
     * Native support extracted from the status-text branch in CMainWindow::refreshWindowedDedicatedServerStatus @004828D7.
     */
    private void refreshDedicatedServerSummaryText() {
        String statusText = dedicatedServerSummaryText();
        if (!statusText.isEmpty()) {
            setDlgItemText(DEDICATED_SERVER_STATUS_CONTROL_ID, statusText);
        }
    }

    /**
     * Native support extracted from the status-text branch in CMainWindow::refreshWindowedDedicatedServerStatus @004828D7.
     */
    private String dedicatedServerSummaryText() {
        int averageCpuPct = 0;
        if (Globals.gameServer.cpuUsageSampleCount != 0) {
            averageCpuPct = (Globals.gameServer.cpuUsageTenthPctSum / Globals.gameServer.cpuUsageSampleCount) / 10;
        }
        if (Globals.gameServer.objectLists.sacks != null) {
            return String.format(
                    Locale.ROOT,
                    GameTexts.get(PatchText.PLAYERS_D_MONSTERS_D_BUILDINGS_D_CORPSES_D_SACKS_D_CPU_D_CPU_AVG_66),
                    Globals.gameServer.playerList.getPlayersCount(),
                    Globals.gameServer.activeUnits.size(),
                    Globals.gameServer.objectLists.buildings.size(),
                    Globals.gameServer.objectLists.corpses.size(),
                    Globals.gameServer.objectLists.sacks.size(),
                    Globals.gameServer.cpuUsageTenthPct / 10,
                    averageCpuPct,
                    map_.toString(),
                    GameTexts.get(MAIN_GAME_SPEED_IS_SLOWER_THAN_SYRUP_108 + gameSpeed)
            );
        }
        return MAP_NOT_LOADED_STATUS_TEXT;
    }

    /**
     * Native support extracted from CMainWindow::refreshWindowedDedicatedServerStatus @004828D7 for Java system UIs.
     */
    public DedicatedServerStatusSnapshot dedicatedServerStatusSnapshot(int preferredPlayerId) {
        if (Globals.gameServer == null) {
            return DedicatedServerStatusSnapshot.EMPTY;
        }
        List<DedicatedServerPlayerStatus> playerStatuses = dedicatedServerPlayerStatuses();
        Player selectedPlayer = preferredPlayerId == 0 ? null : Globals.gameServer.playerList.getPlayerById(preferredPlayerId);
        if (selectedPlayer == null || selectedPlayer.isActive != 0) {
            selectedPlayer = findFirstInactiveDedicatedServerPlayer();
        }
        int selectedPlayerId = selectedPlayer == null ? 0 : (short) selectedPlayer.playerId;
        String selectedPlayerDetails = selectedPlayer == null ? "" : dedicatedServerPlayerDetailsText(selectedPlayer);
        return new DedicatedServerStatusSnapshot(
                dedicatedServerSummaryText(),
                playerStatuses,
                selectedPlayerId,
                selectedPlayerDetails,
                Globals.gameServer.keepSavedCharactersOnServer != 0,
                map_.toString(),
                CLlDriver.getStatus()
        );
    }

    /**
     * Native support extracted from the player-list branch in CMainWindow::refreshWindowedDedicatedServerStatus @004828D7.
     */
    private static List<DedicatedServerPlayerStatus> dedicatedServerPlayerStatuses() {
        List<DedicatedServerPlayerStatus> playerStatuses = new ArrayList<>();
        for (Player player : Globals.gameServer.playerList.players) {
            if (player.isActive == 0) {
                playerStatuses.add(dedicatedServerPlayerStatus(player));
            }
        }
        return playerStatuses;
    }

    /**
     * Native support extracted from the selected-player details branch in
     * CMainWindow::refreshWindowedDedicatedServerStatus @004828D7.
     */
    private static DedicatedServerPlayerStatus dedicatedServerPlayerStatus(Player player) {
        String loginName = "";
        String ipAddressText = "";
        String connectedDurationText = "";
        int lastIntervalBytes = 0;
        int averageBytes = 0;
        int peakIntervalBytes = 0;
        CBufferManager client = CServerApp.getLocalClientByNetId(player.playerId);
        if (client != null) {
            int ipAddress = client.GetIPAddress();
            CBufferManager statusClient = client;
            if (CLlDriver.getProtocolId() == ProtocolId.TCP_IP) {
                CBufferManager maskedClient = CServerApp.getLocalClientByMaskedSocketId(ipAddress & 0x3FFF);
                if (maskedClient != null) {
                    statusClient = maskedClient;
                }
                Object nativeLoginName = statusClient.GetLoginName();
                loginName = nativeLoginName == null ? "" : nativeLoginName.toString();
                ipAddressText = statusClient.getAddressText();
            }
            ClientTrafficStats trafficStats = CServerApp.getLocalClientTrafficStats(ipAddress);
            if (trafficStats != null) {
                connectedDurationText = dedicatedServerTrafficDurationText(trafficStats.sampleCount);
                if (trafficStats.sampleCount != 0) {
                    averageBytes = trafficStats.totalBytes / trafficStats.sampleCount;
                }
                lastIntervalBytes = trafficStats.lastIntervalBytes;
                peakIntervalBytes = trafficStats.peakIntervalBytes;
            }
        }

        return new DedicatedServerPlayerStatus(
                (short) player.playerId,
                player.name,
                player.clientConnected != 0,
                loginName,
                ipAddressText,
                connectedDurationText,
                lastIntervalBytes,
                averageBytes,
                peakIntervalBytes,
                player.creatureKillCount,
                player.playerKillCount,
                player.fragCount,
                player.deathCount
        );
    }

    /**
     * Native support extracted from the selected-list-index walk in
     * CMainWindow::refreshWindowedDedicatedServerStatus @004828D7.
     */
    private static Player findFirstInactiveDedicatedServerPlayer() {
        for (Player player : Globals.gameServer.playerList.players) {
            if (player.isActive == 0) {
                return player;
            }
        }
        return null;
    }

    /**
     * Native support extracted from the selected-list-index walk in
     * CMainWindow::refreshWindowedDedicatedServerStatus @004828D7.
     */
    private static Player findInactivePlayerByListIndex(int selectedIndex) {
        int listIndex = 0;
        for (Player player : Globals.gameServer.playerList.players) {
            if (player.isActive == 0) {
                if (listIndex == selectedIndex) {
                    return player;
                }
                listIndex++;
            }
        }
        return null;
    }

    /**
     * Native support extracted from the selected-player details branch in
     * CMainWindow::refreshWindowedDedicatedServerStatus @004828D7.
     */
    private static String dedicatedServerPlayerDetailsText(Player player) {
        String loginName = "";
        String ipAddressText = "";
        String connectedDurationText = "";
        String trafficText = "";
        CBufferManager client = CServerApp.getLocalClientByNetId(player.playerId);
        if (client != null) {
            int ipAddress = client.GetIPAddress();
            if (CLlDriver.getProtocolId() == ProtocolId.TCP_IP) {
                CBufferManager maskedClient = CServerApp.getLocalClientByMaskedSocketId(ipAddress & 0x3FFF);
                Object nativeLoginName = maskedClient.GetLoginName();
                // MFC 4.2 CString::operator=(LPCTSTR) routes null through SafeStrlen(NULL) == 0.
                loginName = nativeLoginName == null ? "" : nativeLoginName.toString();
                ipAddressText = maskedClient.getAddressText();
            }
            ClientTrafficStats trafficStats = CServerApp.getLocalClientTrafficStats(ipAddress);
            if (trafficStats != null) {
                connectedDurationText = dedicatedServerTrafficDurationText(trafficStats.sampleCount);
                int averageBytes = 0;
                if (trafficStats.sampleCount != 0) {
                    averageBytes = trafficStats.totalBytes / trafficStats.sampleCount;
                }
                trafficText = String.format(
                        Locale.ROOT,
                        DEDICATED_SERVER_TRAFFIC_BYTES_FORMAT,
                        trafficStats.lastIntervalBytes,
                        averageBytes,
                        trafficStats.peakIntervalBytes
                );
            }
        }

        int unitX = 0;
        int unitY = 0;
        if (player.controlledUnit != null) {
            Unit controlledUnit = (Unit) player.controlledUnit;
            unitY = controlledUnit.m_pTargetHandle.getY();
            unitX = controlledUnit.m_pTargetHandle.getX();
        }

        return String.format(
                Locale.ROOT,
                GameTexts.get(PatchText.NAME_S_SLOGIN_S_SIP_ADDRESS_S_SCHARACTER_U_U_A2C_SID_D_SKILLS_D_67)
                        .replace("%u", "%d"),
                player.name,
                DEDICATED_SERVER_DETAILS_SEPARATOR,
                loginName,
                DEDICATED_SERVER_DETAILS_SEPARATOR,
                ipAddressText,
                DEDICATED_SERVER_DETAILS_SEPARATOR,
                Integer.toUnsignedLong(player.characterSessionKeyPart1),
                Integer.toUnsignedLong(player.characterSessionKeyPart2),
                DEDICATED_SERVER_DETAILS_SEPARATOR,
                (short) player.playerId,
                DEDICATED_SERVER_DETAILS_SEPARATOR,
                player.creatureKillCount,
                DEDICATED_SERVER_DETAILS_SEPARATOR,
                player.playerKillCount,
                DEDICATED_SERVER_DETAILS_SEPARATOR,
                player.fragCount,
                DEDICATED_SERVER_DETAILS_SEPARATOR,
                player.deathCount,
                DEDICATED_SERVER_DETAILS_SEPARATOR,
                player.gold,
                DEDICATED_SERVER_DETAILS_SEPARATOR,
                unitX,
                unitY,
                DEDICATED_SERVER_DETAILS_SEPARATOR,
                connectedDurationText,
                DEDICATED_SERVER_DETAILS_SEPARATOR,
                trafficText
        );
    }

    /**
     * Native support extracted from the CMainWindow::refreshWindowedDedicatedServerStatus @004828D7 traffic timer
     * formatting block.
     */
    private static String dedicatedServerTrafficDurationText(int sampleCount) {
        return String.format(
                Locale.ROOT,
                DEDICATED_SERVER_TRAFFIC_DURATION_FORMAT,
                sampleCount / 0xE10,
                (sampleCount % 0xE10) / 0x3C,
                sampleCount % 0x3C
        );
    }

    /**
     * Native support extracted from the g_ServerConfig.field15_0x8c rotation tail in
     * CMainWindow::pumpDedicatedServerIdle @00488CA5.
     */
    private void advanceDedicatedServerMapSchedule() {
        Globals.serverConfig.field15_0x8c++;
        if (Globals.serverConfig.field12_0x70.size() <= Globals.serverConfig.field15_0x8c) {
            Globals.serverConfig.field15_0x8c = 0;
        }
        if (isHeadlessDedicatedRuntime()) {
            setDedicatedServerMapName(Globals.serverConfig.maps.get(Globals.serverConfig.field15_0x8c));
            runSessionBootstrap(0);
            return;
        }
        pDedicatedServerConsoleVisualObject.onMessage(MessageCodes.DIALOG_OK, 0, 0);
    }

    /**
     * Native: CMainWindow::pumpTimedGameplayTicks @00488AA1.
     * Fully ported.
     */
    public void pumpTimedGameplayTicks() {
        while (Integer.compareUnsigned(
                Globals.currentTickMillis(),
                m_LastTickTime + m_TickInterval * (m_FrameCounter + 1)
        ) >= 0) {
            if (m_FrameCounter == 0) {
                m_LastTickTime = Globals.currentTickMillis();
            }
            m_FrameCounter = (m_FrameCounter + 1) & 0xF;
            Globals.gameServer.runServerLoopTick();
            pMapVisualObject.handleGameAction(null, 100);
            inputController.onMessage(MessageCodes.INITIALIZE_UI, 0, 0);
        }
        scrollMapAtScreenEdges();
        renderFrameIfFocused();
    }

    /**
     * Native: CMainWindow::pumpRemoteGameplayIdle @00488E25.
     * Fully ported.
     */
    public void pumpRemoteGameplayIdle() {
        if (!CServerApp.hasActiveRemoteConnection()) {
            getInputController().onMessage(MessageCodes.RETURN_TO_GAME, 0, 0);
            postMessage(MessageCodes.RETURN_TO_MULTIPLAYER_SETUP, 0, 0);
            recordRemoteConnectionLost();
            return;
        }

        if (CServerApp.getPendingSegmentMarkerCount() == 0) {
            int currentTick = Globals.currentTickMillis();
            if (Math.abs(currentTick - Globals.lastRemoteServerLoopCounterBroadcastTick) > 1000) {
                CServerApp.broadcastRemoteServerLoopCounter(1);
                Globals.lastRemoteServerLoopCounterBroadcastTick = currentTick;
            }
        } else {
            drainRemoteGameplayPackets();
            Globals.lastRemoteServerLoopCounterBroadcastTick = Globals.currentTickMillis();
        }

        scrollMapAtScreenEdgesWithoutMapContextNotify();
        renderFrameIfFocused();
    }

    /**
     * Native support extracted from the IsTraceEnabled lost-connection block in
     * CMainWindow::pumpRemoteGameplayIdle @00488E25.
     */
    private void recordRemoteConnectionLost() {
        if (!Globals.traceDiagnosticsEnabled) {
            return;
        }
        String line = TRACE_TIMESTAMP_FORMAT.format(LocalDateTime.now()) + TRACE_CONNECTION_LOST + "\n";
        try {
            Files.writeString(
                    TRACE_ERROR_LOG,
                    line,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to append native trace log", e);
        }
        pMapVisualObject.gameListControl.addTimedLine(
                TRACE_CONNECTION_LOST,
                Palettes.messageDim(),
                TRACE_CONNECTION_LOST_LINE_LIFETIME_MS
        );
    }

    /**
     * Native support extracted from the pending-packet drain loop in CMainWindow::pumpRemoteGameplayIdle @00488E25.
     */
    private void drainRemoteGameplayPackets() {
        do {
            m_FrameCounter = (m_FrameCounter + 1) & 0xF;
            if (m_FrameCounter == 0) {
                CServerApp.sampleRemoteClientTrafficStats();
            }
            pMapVisualObject.handleGameAction(null, 100);
            getInputController().onMessage(INITIALIZE_UI, 0, 0);
        } while (CServerApp.getPendingSegmentMarkerCount() != 0);
    }

    /**
     * Native: CMainWindow::pumpSingleGameplayTick @00488900.
     * Fully ported.
     */
    public void pumpSingleGameplayTick() {
        Globals.gameServer.runServerLoopTick();
        pMapVisualObject.handleGameAction(null, 100);
        inputController.onMessage(MessageCodes.INITIALIZE_UI, 0, 0);
        m_FrameCounter++;
        scrollMapAtScreenEdges();
        if (haveFocus != 0) {
            inputController.onMessage(MessageCodes.RENDER_FRAME, 0, 0);
            captureAllodsDebugBmp();
        }
    }

    /**
     * Native support extracted from CMainWindow::pumpTimedGameplayTicks @00488AA1 and
     * CMainWindow::pumpSingleGameplayTick @00488900.
     */
    public void scrollMapAtScreenEdges() {
        scrollMapAtScreenEdges(true);
    }

    /**
     * Native support extracted from CMainWindow::pumpRemoteGameplayIdle @00488E25.
     */
    public void scrollMapAtScreenEdgesWithoutMapContextNotify() {
        scrollMapAtScreenEdges(false);
    }

    /**
     * Native support extracted from CMainWindow::pumpTimedGameplayTicks @00488AA1,
     * CMainWindow::pumpSingleGameplayTick @00488900, and CMainWindow::pumpRemoteGameplayIdle @00488E25.
     */
    private void scrollMapAtScreenEdges(boolean notifyMapContextChanged) {
        if (!Globals.mousePointer.isSelecting() && DialogsMaskFlag.contains(dialogsMask, GAMEPLAY)) {
            boolean scrolled = false;
            if (Globals.mousePointer.getX() == 0) {
                pMapVisualObject.scrollCameraXBy(-1);
                scrolled = true;
            }
            if (Globals.mousePointer.getY() == 0) {
                pMapVisualObject.scrollCameraYBy(-1);
                scrolled = true;
            }
            if (Globals.screenRect.right - 2 <= Globals.mousePointer.getX()) {
                pMapVisualObject.scrollCameraXBy(1);
                scrolled = true;
            }
            if (Globals.screenRect.bottom - 2 <= Globals.mousePointer.getY()) {
                pMapVisualObject.scrollCameraYBy(1);
                scrolled = true;
            }
            if (scrolled && notifyMapContextChanged) {
                pMinimapVisualObject.onMessage(MessageCodes.NOTIFY_MAP_CONTEXT_CHANGED, 0, 0);
            }
        }
    }

    /**
     * Native support extracted from CMainWindow::pumpTimedGameplayTicks @00488AA1,
     * CMainWindow::pumpSingleGameplayTick @00488900, and CMainApp::OnIdle @00481CCB.
     */
    public void renderFrameIfFocused() {
        if (haveFocus != 0) {
            inputController.onMessage(MessageCodes.RENDER_FRAME, 0, 0);
        }
    }

    /**
     * Native: Global::captureAllodsDebugBmp @004526A3.
     * Fully ported.
     */
    private static void captureAllodsDebugBmp() {
        Screen screen = Globals.screen;
        BufferedImage image = new BufferedImage(screen.w(), screen.h(), BufferedImage.TYPE_INT_RGB);
        byte[] surface = screen.surface();
        int pitch = screen.pitchBytes();
        for (int y = 0; y < screen.h(); y++) {
            int rowOffset = y * pitch;
            for (int x = 0; x < screen.w(); x++) {
                int pixelOffset = rowOffset + x * 4;
                int blue = surface[pixelOffset] & 0xFF;
                int green = surface[pixelOffset + 1] & 0xFF;
                int red = surface[pixelOffset + 2] & 0xFF;
                image.setRGB(x, screen.h() - 1 - y, (red << 16) | (green << 8) | blue);
            }
        }
        int captureIndex = allodsBmpCaptureIndex;
        allodsBmpCaptureIndex++;
        try {
            ImageIO.write(image, "bmp", new File("Allods%04d.bmp".formatted(captureIndex)));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write Allods debug BMP", e);
        }
    }

    /**
     * Native: CMainWindow::showCharacterGeneratorAfterStartGameSetup @0048F3F0.
     * Fully ported.
     */
    public void showCharacterGeneratorAfterStartGameSetup() {
        CMousePointer.Cursor_Wait.setToMousePointer();
        pSelectionInfoPanelVisualObject.selectionInfoViewMode0x70 = 1;
        pRightPanelContainerVisualObject.onMessage(MessageCodes.SET_MAP_CONTEXT, pMapVisualObject, 0);
        CUnit selectedUnit = pMapVisualObject.getSelectedCUnit();
        selectedUnit.setSelected(true);
        pMapVisualObject.updateSelectionState();
        inputController.addChild(pCharacterGeneratorDialogVisualObject);
        pCharacterGeneratorDialogVisualObject.showDialog();
        inputController.draw();
        field149_0x44C = 0;
        dialogsMask = CHARACTER_GENERATOR.includeTo(dialogsMask);
        CMousePointer.Cursor_Default.setToMousePointer();
    }

    /**
     * Native: CMainWindow::OnChar @004888BD.
     * Fully ported. Native forwards WM_CHAR to the input controller and then delegates to the stock frame handler;
     * Java has no modeled CFrameWnd default char state.
     */
    public void onChar(int codepoint) {
        inputController.onMessage(MessageCodes.WM_CHAR, codepoint, 0);
    }

    /**
     * Native: CMainWindow::OnKeyDown @00484A76.
     * Java port status: native key dispatch ported; Java intentionally skips the native CMousePointer::EndDrag tail
     * because key presses must not hide or reset final-overlay tooltips.
     */
    public void onKeyDown(int virtualKey, int repeatCount, int flags) {
        if (fileTransferDownloadPending == 0) {
            handleActiveKeyDown(virtualKey);
        }
    }

    /**
     * Native support extracted from the non-file-transfer branch in CMainWindow::OnKeyDown @00484A76.
     */
    private void handleActiveKeyDown(int virtualKey) {
        switch (virtualKey) {
            case VK_BACK, VK_TAB, VK_RETURN, VK_SPACE, VK_PRIOR, VK_NEXT, VK_END, VK_HOME, VK_LEFT, VK_UP, VK_RIGHT,
                 VK_DOWN, VK_INSERT, VK_DELETE, VK_0, VK_1, VK_2, VK_3, VK_4, VK_5, VK_6, VK_7, VK_8, VK_9,
                 VK_F4, VK_F5, VK_F6, VK_F7, VK_F8, VK_F9, VK_F11, VK_F12, VK_OEM_3 ->
                    forwardKeyDownToInputController(virtualKey);
            case VK_SHIFT, VK_LSHIFT, VK_RSHIFT -> Globals.shiftKeyDown = true;
            case VK_CONTROL, VK_LCONTROL, VK_RCONTROL -> Globals.controlKeyDown = true;
            case VK_PAUSE -> showPauseDialogOnKeyDown();
            case VK_ESCAPE -> handleEscapeKeyDown();
            case VK_NUMPAD0, VK_NUMPAD1, VK_NUMPAD2, VK_NUMPAD3, VK_NUMPAD4, VK_NUMPAD5, VK_NUMPAD6, VK_NUMPAD7,
                 VK_NUMPAD8, VK_NUMPAD9 -> forwardKeyDownToInputController(virtualKey - (VK_NUMPAD0 - VK_0));
            case VK_ADD -> handleGameSpeedIncreaseKeyDown();
            case VK_SUBTRACT -> handleGameSpeedDecreaseKeyDown();
            case VK_F1 -> postMessage(MessageCodes.SHOW_HELP_TEXT_DIALOG, 0, 0);
            case VK_F2 -> postSaveGameKeyDown();
            case VK_F3 -> postLoadOrDiplomacyKeyDown();
            default -> {
                if (virtualKey >= VK_A && virtualKey <= VK_Z) {
                    forwardKeyDownToInputController(virtualKey);
                }
            }
        }
    }

    /**
     * Native support extracted from repeated CVisualObject::OnMessage calls in CMainWindow::OnKeyDown @00484A76.
     */
    private void forwardKeyDownToInputController(int virtualKey) {
        inputController.onMessage(MessageCodes.WM_KEYDOWN, virtualKey, 0);
    }

    /**
     * Native support extracted from the VK_PAUSE branch in CMainWindow::OnKeyDown @00484A76.
     */
    private void showPauseDialogOnKeyDown() {
        if (sessionMode == SESSION_MODE_CAMPAIGN && DialogsMaskFlag.contains(dialogsMask, GAMEPLAY)) {
            showDialog(new HeaderDialogVariantVisualObject(
                    1,
                    0x20,
                    0x30,
                    0x260,
                    0x1B0,
                    GameTexts.get(MAIN_GAME_PAUSED_CLICK_OK_TO_CONTINUE_119),
                    null,
                    0
            ));
        }
    }

    /**
     * Native support extracted from the VK_ESCAPE branch in CMainWindow::OnKeyDown @00484A76.
     */
    private void handleEscapeKeyDown() {
        if (GAMEPLAY.isSetIn(dialogsMask)) {
            postMessage(MessageCodes.ESC_MENU, 0, 0);
            if (sessionMode != SESSION_MODE_CAMPAIGN) {
                m_GameSession.refreshSavedCharacterProgress();
            }
        } else if (dialogsMask == 0) {
            if (!connectionScratchState.directAddress.isEmpty()) {
                forwardKeyDownToInputController(VK_ESCAPE);
            } else if (sessionMode == SESSION_MODE_CAMPAIGN) {
                postMessage(MessageCodes.SHOW_TOWN_MENU, 0, 0);
            }
        } else {
            forwardKeyDownToInputController(VK_ESCAPE);
        }
    }

    /**
     * Native support extracted from the VK_ADD branch in CMainWindow::OnKeyDown @00484A76.
     */
    private void handleGameSpeedIncreaseKeyDown() {
        if (Globals.controlKeyDown) {
            singleStepGameplayTickMode = 1;
        } else if (sessionMode != SESSION_MODE_MULTIPLAYER_CLIENT && DialogsMaskFlag.contains(dialogsMask, GAMEPLAY)) {
            applyKeyDownGameSpeedChange(gameSpeed + 1);
        }
    }

    /**
     * Native support extracted from the VK_SUBTRACT branch in CMainWindow::OnKeyDown @00484A76.
     */
    private void handleGameSpeedDecreaseKeyDown() {
        if (Globals.controlKeyDown) {
            singleStepGameplayTickMode = 0;
            m_LastRenderTime = Globals.currentTickMillis();
            m_FrameCounter = 0;
            m_LagAccumulator = 0;
        } else if (sessionMode != SESSION_MODE_MULTIPLAYER_CLIENT && DialogsMaskFlag.contains(dialogsMask, GAMEPLAY)) {
            applyKeyDownGameSpeedChange(gameSpeed - 1);
        }
    }

    /**
     * Native support extracted from SetGameSpeed plus speed-text reporting in CMainWindow::OnKeyDown @00484A76.
     */
    private void applyKeyDownGameSpeedChange(int speedIndex) {
        boolean changed = setGameSpeed(speedIndex);
        String message = GameTexts.get(MAIN_GAME_SPEED_IS_SLOWER_THAN_SYRUP_108 + gameSpeed);
        if (changed) {
            CServerApp.sendServerChatText(message, null);
        } else {
            pMapVisualObject.gameListControl.addUniqueTimedLine(message, Palettes.gray, 5000);
        }
    }

    /**
     * Native support extracted from the VK_F2 branch in CMainWindow::OnKeyDown @00484A76.
     */
    private void postSaveGameKeyDown() {
        if ((DialogsMaskFlag.contains(dialogsMask, GAMEPLAY) || dialogsMask == 0) && sessionMode == SESSION_MODE_CAMPAIGN) {
            postMessage(MessageCodes.SAVE_GAME, 0, 0);
        }
    }

    /**
     * Native support extracted from the VK_F3 branch in CMainWindow::OnKeyDown @00484A76.
     */
    private void postLoadOrDiplomacyKeyDown() {
        if (sessionMode == SESSION_MODE_CAMPAIGN) {
            if (DialogsMaskFlag.contains(dialogsMask, GAMEPLAY) || dialogsMask == 0) {
                postMessage(MessageCodes.LOAD_GAME, 0, 0);
            }
        } else if (DialogsMaskFlag.contains(dialogsMask, GAMEPLAY)) {
            postMessage(MessageCodes.DIPLOMACY, 0, 0);
        }
    }

    /**
     * Native: CMainWindow::OnSysKeyDown @0048509B.
     * Java port status: native sys-key dispatch ported; Java intentionally skips the native CMousePointer::EndDrag tail
     * because key presses must not hide or reset final-overlay tooltips.
     */
    public void onSysKeyDown(int virtualKey, int repeatCount, int flags) {
        if (fileTransferDownloadPending == 0) {
            if (virtualKey == VK_F10) {
                forwardKeyDownToInputController(VK_F10);
                return;
            }
            handleActiveSysKeyDown(virtualKey, flags);
        }
    }

    /**
     * Native support extracted from the non-file-transfer branch in CMainWindow::OnSysKeyDown @0048509B.
     */
    private void handleActiveSysKeyDown(int virtualKey, int flags) {
        if (virtualKey == VK_MENU || virtualKey == VK_LMENU || virtualKey == VK_RMENU) {
            Globals.altKeyDown = true;
        }
        if ((flags & SYSKEY_ALT_CONTEXT_BIT) == 0) {
            return;
        }
        if (virtualKey >= VK_1 && virtualKey <= VK_9) {
            Globals.altKeyDown = true;
            forwardKeyDownToInputController(virtualKey);
        } else if (virtualKey >= VK_NUMPAD0 && virtualKey <= VK_NUMPAD9) {
            Globals.altKeyDown = true;
            forwardKeyDownToInputController(virtualKey - (VK_NUMPAD0 - VK_0));
        } else if (virtualKey == VK_S) {
            captureAllodsDebugBmp();
        } else if (virtualKey > VK_A && virtualKey < VK_Z && DialogsMaskFlag.contains(dialogsMask, GAMEPLAY)) {
            pMapVisualObject.sendAltDebugCommand(virtualKey - VK_A);
        }
    }

    /**
     * Native: CMainWindow::OnKeyUp @0048521C.
     * Java port status: native modifier release dispatch ported; Java intentionally skips the native
     * CMousePointer::EndDrag tail because key releases must not hide or reset final-overlay tooltips.
     */
    public void onKeyUp(int virtualKey, int repeatCount, int flags) {
        if (fileTransferDownloadPending == 0) {
            if (virtualKey == VK_SHIFT || virtualKey == VK_LSHIFT || virtualKey == VK_RSHIFT) {
                Globals.shiftKeyDown = false;
            } else if (virtualKey == VK_CONTROL || virtualKey == VK_LCONTROL || virtualKey == VK_RCONTROL) {
                Globals.controlKeyDown = false;
            } else if (virtualKey == VK_MENU || virtualKey == VK_LMENU || virtualKey == VK_RMENU) {
                Globals.altKeyDown = false;
            }
        }
    }

    /**
     * Native: CMainWindow::OnSysKeyUp @00485291.
     * Java port status: native sys-key release dispatch ported; Java intentionally skips the native
     * CMousePointer::EndDrag tail because key releases must not hide or reset final-overlay tooltips.
     */
    public void onSysKeyUp(int virtualKey, int repeatCount, int flags) {
        if (fileTransferDownloadPending == 0
                && (virtualKey == VK_MENU || virtualKey == VK_LMENU || virtualKey == VK_RMENU)) {
            Globals.altKeyDown = false;
        }
    }

    /**
     * Native: CMainWindow::OnSetFocus @00484A46.
     * Fully ported. Native tail calls the stock frame focus handler; Java has no modeled CFrameWnd focus state.
     */
    public void onSetFocus() {
        haveFocus = 1;
        Globals.altKeyDown = false;
    }

    /**
     * Native: CMainWindow::OnSetCursor @00484A09.
     * Fully ported at the Java platform-cursor boundary. Native hides the Win32 cursor in fullscreen because DirectDraw
     * renders the game cursor; Java keeps its GLFW game cursor visible and restores the OS arrow for windowed mode.
     */
    public int onSetCursor() {
        return Globals.mousePointer.applyMainWindowSetCursor();
    }

    /**
     * Native: CMainWindow::OnKillFocus @00489194.
     * Fully ported. Native first delegates to the default MFC focus handler; Java has no additional CWnd default state
     * and keeps the recovered main-window focus/modifier reset.
     */
    public void onKillFocus() {
        haveFocus = 0;
        Globals.altKeyDown = false;
        Globals.controlKeyDown = false;
        Globals.shiftKeyDown = false;
    }

    /**
     * Native: CMainWindow::onDialogClosed @004891D8.
     * Java port status: native close routing ported; Java additionally uses the visible raw TCP/IP replacement for
     * create-game and HAT session-list join routes because raw TCP/IP is the Java replacement for native DirectPlay TCP/IP.
     */
    public void onDialogClosed(CVisualObject dialog) {
        field149_0x44C = 1;
        inputController.removeChild(dialog);
        pMapVisualObject.areaEffectRefreshPending = 1;
        if (isShopDialogClose(dialog)) {
            pMapVisualObject.closeShopDialog();
            pMapVisualObject.updateSelectionState();
            if (sessionMode != SESSION_MODE_CAMPAIGN) {
                playGameplayMusicPlaylist();
            }
        } else if (dialog == pChatVisualObject) {
            pMapVisualObject.removeChild(pChatVisualObject);
            if (readDialogResult(dialog) == MessageCodes.DIALOG_OK) {
                sendChatInputText();
            }
        } else if (dialog == pStartupLogoDialogVisualObject) {
            dialogsMask = STARTUP_LOGO.excludeIn(dialogsMask);
        } else if (dialog == pFameHallDocumentDialogVisualObject) {
            dialogsMask = FAME_HALL_DOCUMENT.excludeIn(dialogsMask);
        } else if (dialog == pFameHallDialogVisualObject) {
            dialogsMask = FAME_HALL.excludeIn(dialogsMask);
            postMessage(MessageCodes.SHOW_MAIN_MENU, 0, 0);
        } else if (dialog == pMainMenuVisualObject) {
            dialogsMask = MAIN_MENU.excludeIn(dialogsMask);
        } else if (isTownDialogClose(dialog)) {
            // Native has no branch body for town dialog closes.
        } else if (isInnDialogClose(dialog)) {
            if (sessionMode != SESSION_MODE_CAMPAIGN) {
                playGameplayMusicPlaylist();
            }
            dialogsMask = INN_DIALOG.excludeIn(dialogsMask);
            pMapVisualObject.updateSelectionState();
        } else if (dialog == pCharacterGeneratorDialogVisualObject) {
            dialogsMask = CHARACTER_GENERATOR.excludeIn(dialogsMask);
            if (readDialogResult(dialog) == MessageCodes.RETURN_TO_GAME) {
                showStartGameSetupForNewSession();
            } else {
                continueAfterCharacterGeneratorAccept();
            }
        } else if (dialog == pCharacterLoaderDialogVisualObject) {
            dialogsMask = CHARACTER_LOADER.excludeIn(dialogsMask);
            continueAfterCharacterLoaderDialogClose(readDialogResult(dialog));
        } else if (dialog == pGlobalMapDialogVisualObject) {
            dialogsMask = GLOBAL_MAP.excludeIn(dialogsMask);
        } else if (dialog == pCreditsDialogVisualObject) {
            dialogsMask = CREDITS.excludeIn(dialogsMask);
            postMessage(
                    ScenarioProgressSupport.isCampaignEndingReached() ? MessageCodes.SHOW_FAME_HALL_DIALOG : MessageCodes.SHOW_MAIN_MENU,
                    0,
                    0
            );
        } else if (dialog == pDropGoldPromptVisualObject) {
            dialogsMask = MODAL_DIALOG.excludeIn(dialogsMask);
            resetGameplayTimingAfterModalClose();
        } else if (dialog == pTownReturnPromptDialogVisualObject) {
            dialogsMask = MODAL_DIALOG.excludeIn(dialogsMask);
            if (readDialogResult(dialog) == MessageCodes.HEADER_DIALOG_YES) {
                pMapVisualObject.clearSessionForFirstPlayerLobbyReturn();
                postMessage(MessageCodes.SHOW_MAIN_MENU, 0, 0);
            } else {
                postMessage(MessageCodes.SHOW_CURRENT_TOWN_DIALOG, 0, 0);
            }
            pTownReturnPromptDialogVisualObject = null;
        } else if (dialog == pSaveDialogVisualObject) {
            dialogsMask = MODAL_DIALOG.excludeIn(dialogsMask);
            if (readDialogResult(dialog) == MessageCodes.DIALOG_OK) {
                writeCurrentMissionResumeSave();
            }
            resetServerBootstrapGameplayTimingAfterModalClose();
            pSaveDialogVisualObject = null;
        } else if (dialog == pLoadDialogVisualObject) {
            dialogsMask = MODAL_DIALOG.excludeIn(dialogsMask);
            MessageCodes closeReason = readDialogResult(dialog);
            if (closeReason == MessageCodes.DIALOG_OK) {
                postMessage(MessageCodes.LOAD_GAME_CREATE, 0, 0);
            } else if (!Globals.gameServer.isServerLifecycleAllocated()) {
                postMessage(MessageCodes.SHOW_MAIN_MENU, 0, 0);
            } else if (questEventDialogId == 0xFF) {
                postMessage(MessageCodes.EXIT_TO_MENU, 0, 0);
            }
            resetServerBootstrapGameplayTimingAfterModalClose();
            pLoadDialogVisualObject = null;
            if (closeReason == MessageCodes.DIALOG_OK) {
                return;
            }
        } else if (dialog == pCenteredDialogContextArrayVisualObject) {
            dialogsMask = MODAL_DIALOG.excludeIn(dialogsMask);
            continueAfterMultiplayerSessionDialogClose(readDialogResult(dialog));
            pCenteredDialogContextArrayVisualObject = null;
            return;
        } else if (dialog == m_pMPConnectionDialog) {
            dialogsMask = MODAL_DIALOG.excludeIn(dialogsMask);
            if (readDialogResult(dialog) == MessageCodes.RETURN_TO_GAME) {
                if (sessionMode == SESSION_MODE_DEDICATED_SERVER) {
                    postMessage(MessageCodes.SHOW_MAIN_MENU, 0, 0);
                } else {
                    showCharacterLoaderDialog();
                }
            }
            m_pMPConnectionDialog = null;
            return;
        } else if (dialog == pMultiplayerMapSelectionDialogVisualObject) {
            dialogsMask = MODAL_DIALOG.excludeIn(dialogsMask);
            if (readDialogResult(dialog) == MessageCodes.RETURN_TO_GAME) {
                inputController.onMessage(MessageCodes.RETURN_TO_GAME, 0, 0);
                postMessage(MessageCodes.RETURN_TO_MULTIPLAYER_SETUP, 0, 0);
            } else {
                postMessage(MessageCodes.RUN_MULTIPLAYER_SESSION_BOOTSTRAP, 0, 0);
            }
            pMultiplayerMapSelectionDialogVisualObject = null;
            return;
        } else if (dialog == pHatServerBrowserDialogVisualObject) {
            dialogsMask = MODAL_DIALOG.excludeIn(dialogsMask);
            if (readDialogResult(dialog) == MessageCodes.RETURN_TO_GAME) {
                inputController.onMessage(MessageCodes.RETURN_TO_GAME, 0, 0);
                showCharacterLoaderDialog();
                pHatServerBrowserDialogVisualObject = null;
                return;
            }
            CServerApp.setRemoteNetworkDriver(CLlDriver.class);
            CLlDriver.bindRemoteServerAppBoundary();
            CLlDriver.setProtocolId(ProtocolId.TCP_IP);
            postMessage(MessageCodes.SHOW_MULTIPLAYER_SESSION_DIALOG, 0, 0);
            pHatServerBrowserDialogVisualObject = null;
            return;
        } else if (dialog == pDedicatedServerConsoleVisualObject) {
            dialogsMask = MODAL_DIALOG.excludeIn(dialogsMask);
            dialogsMask = 0;
            if (readDialogResult(dialog) == MessageCodes.DIALOG_OK) {
                postMessage(MessageCodes.SHOW_MAIN_MENU, 0, 0);
            }
            pDedicatedServerConsoleVisualObject = null;
        } else if (dialog == pDiplomacySettingsDialogVisualObject) {
            dialogsMask = MODAL_DIALOG.excludeIn(dialogsMask);
            pDiplomacySettingsDialogVisualObject = null;
            if (readDialogResult(dialog) == MessageCodes.DIALOG_OK) {
                commitDiplomacySettingsDialog();
            }
        } else if (dialog == pStartGameSetupDialogVisualObject) {
            dialogsMask = START_GAME_SETUP.excludeIn(dialogsMask);
            if (readDialogResult(dialog) == MessageCodes.RETURN_TO_GAME) {
                pMapVisualObject.clearStartGameSetupPreviewObjects();
                if (sessionMode == SESSION_MODE_CAMPAIGN) {
                    postMessage(MessageCodes.SHOW_MAIN_MENU, 0, 0);
                } else {
                    showCharacterLoaderDialog();
                }
            } else {
                continueAfterStartGameSetupAccept();
            }
        } else if (dialog == pMissionFailedHeaderDialogVisualObject) {
            dialogsMask = MODAL_DIALOG.excludeIn(dialogsMask);
            if (readDialogResult(dialog) == MessageCodes.DIALOG_OK) {
                postMessage(MessageCodes.EXIT_TO_MENU, 0, 0);
            } else {
                postMessage(MessageCodes.LOAD_GAME, 0, 0);
            }
        } else if (dialog == pHeaderDialogVariantVisualObject) {
            dialogsMask = HEADER_DIALOG_VARIANT.excludeIn(dialogsMask);
            if (DialogsMaskFlag.doesNotContain(dialogsMask, GAMEPLAY)) {
                inputController.previousMouseInputTarget = inputController.getChildById(0x3FC);
            }
        } else if (dialog == pViewCutscenesHeaderDialogVisualObject) {
            dialogsMask = MODAL_DIALOG.excludeIn(dialogsMask);
            if (readDialogResult(dialog) == MessageCodes.DIALOG_OK) {
                TextListVisualObject cutsceneList = (TextListVisualObject) dialog.getChildById(2);
                int selectedCutsceneIndex = cutsceneList.getSelectedRow();
                ViewCutscenesHeaderDialogVisualObject.setSelectedCutsceneIndex(selectedCutsceneIndex);
                playLocationCutscene(selectedCutsceneIndex);
                postMessage(MessageCodes.VIEW_CUTSCENES, 0, 0);
            } else {
                postMessage(MessageCodes.SHOW_MAIN_MENU, 0, 0);
            }
        } else if (DialogsMaskFlag.contains(dialogsMask, MODAL_DIALOG)) {
            dialogsMask = MODAL_DIALOG.excludeIn(dialogsMask);
            resetCampaignGameplayTimingAfterModalClose();
        }
        if (DialogsMaskFlag.isExactly(dialogsMask, MAIN_MENU)) {
            CMousePointer.Cursor_Select.setToMousePointer();
        }
        pRightPanelContainerVisualObject.onMessage(MessageCodes.NOTIFY_MAP_CONTEXT_CHANGED, 0, 0);
        if (!DialogsMaskFlag.isExactly(dialogsMask, GAMEPLAY) || pMapVisualObject.mapDescriptor != null) {
            inputController.draw();
        }
        if (DialogsMaskFlag.isExactly(dialogsMask, GAMEPLAY)) {
            HandleQuestEventDialogMessageHandler.postNextQueuedRoleDialogId(this);
        }
    }

    /**
     * Native support extracted from the ChatVisualObject branch in CMainWindow::onDialogClosed @004891D8.
     */
    private void sendChatInputText() {
        StringBuilder text = new StringBuilder();
        pChatVisualObject.getValue(text);
        String chatText = text.toString();
        // Native CString::GetAt @004029E0 returns m_pchData[0], so an empty CString yields NUL.
        char firstChar = chatText.isEmpty() ? '\0' : chatText.charAt(0);
        if (firstChar == '=') {
            pMapVisualObject.sendChatTextAction(chatText.substring(1), ChatTextAction.CHAT_DELIVERY_SHOUT, 0);
        } else if (firstChar == '-') {
            int recipientPlayerIndex = pChatVisualObject.textBlock.chatRecipientPlayerIndex;
            if (recipientPlayerIndex == -1) {
                pMapVisualObject.sendChatTextAction(chatText.substring(1), ChatTextAction.CHAT_DELIVERY_ALLIED, 0);
            } else {
                int messageStart = pChatVisualObject.textBlock.chatRecipientPrefixLength;
                pMapVisualObject.sendChatTextAction(
                        chatText.substring(messageStart),
                        ChatTextAction.CHAT_DELIVERY_PRIVATE,
                        recipientPlayerIndex
                );
            }
        } else {
            pMapVisualObject.sendChatTextAction(chatText, ChatTextAction.CHAT_DELIVERY_SAY, 0);
        }
    }

    /**
     * Native support extracted from repeated timing reset blocks in CMainWindow::onDialogClosed @004891D8.
     */
    private void resetGameplayTimingAfterModalClose() {
        m_LastRenderTime = Globals.currentTickMillis();
        m_FrameCounter = 0;
        m_LagAccumulator = 0;
    }

    /**
     * Native support extracted from save/load dialog close branches in CMainWindow::onDialogClosed @004891D8.
     */
    private void resetServerBootstrapGameplayTimingAfterModalClose() {
        if (DialogsMaskFlag.contains(dialogsMask, GAMEPLAY) && serverBootstrapEnabled != 0) {
            resetGameplayTimingAfterModalClose();
        }
    }

    /**
     * Native support extracted from CenteredDialogContextArrayVisualObject close branch in CMainWindow::onDialogClosed @004891D8.
     * Java raw TCP/IP Create additionally performs the visible replacement listener/server/direct-host-join setup.
     */
    private void continueAfterMultiplayerSessionDialogClose(MessageCodes closeReason) {
        if (closeReason == MessageCodes.MULTIPLAYER_JOIN_GAME) {
            serverBootstrapEnabled = 0;
            connectToSelectedMultiplayerSession();
        } else if (closeReason == MessageCodes.MULTIPLAYER_CREATE_DEDICATED_GAME) {
            startDedicatedMultiplayerSession();
        } else if (closeReason == MessageCodes.MULTIPLAYER_CREATE_GAME) {
            selectNetworkHostDriver();
            if (CLlDriver.getProtocolId() == ProtocolId.TCP_IP && startJavaRawTcpVisibleMultiplayerServer()) {
                if (createServer(0) == 0) {
                    if (pMapVisualObject.connectAndJoinSession()) {
                        postMessage(MessageCodes.PREPARE_MULTIPLAYER_MAP_SELECTION, 0, 0);
                    } else {
                        postMessage(MessageCodes.RETURN_TO_MULTIPLAYER_SETUP, 0, 0);
                    }
                } else {
                    CLlDriver.handleNetworkErrorAndClose();
                }
            }
        } else {
            postMessage(MessageCodes.SHOW_MAIN_MENU, 0, 0);
        }
    }

    /**
     * Native: CMainWindow::connectToSelectedMultiplayerSession @0048E764.
     * Fully ported.
     */
    private void connectToSelectedMultiplayerSession() {
        sessionMode = SESSION_MODE_MULTIPLAYER_CLIENT;
        CServerApp.setRemoteNetworkDriver(CLlDriver.class);
        CLlDriver.bindRemoteServerAppBoundary();
        applySelectedSessionConnectionTimeoutOptions();
        if (!pMapVisualObject.connectAndJoinSession()) {
            CLlDriver.handleNetworkErrorAndClose();
            postMessage(MessageCodes.EXIT_MAP, 0, 0);
            return;
        }
        if (m_GameSession.skipFormerCharacterPrompt == 0) {
            MessageCodes promptResult = showDialogAndAwaitResult(new HeaderDialogVariantVisualObject(
                    1,
                    0x40,
                    100,
                    0x1AC,
                    0x252,
                    GameTexts.get(MainText.YOUR_FORMER_CHARACTER_IS_STILL_AVAILABLE_FOR_PLAY_WOULD_YOU_LIKE_210),
                    null,
                    4
            ));
            if (promptResult != MessageCodes.HEADER_DIALOG_YES) {
                postMessage(MessageCodes.EXIT_MAP, 0, 0);
                return;
            }
        }
        if (multiplayerRefreshGamesPending == 0) {
            postMessage(MessageCodes.PREPARE_MULTIPLAYER_MAP_SELECTION, 0, 0);
        } else {
            postMessage(MessageCodes.RUN_MULTIPLAYER_SESSION_BOOTSTRAP, 0, 0);
        }
    }

    /**
     * Native support extracted from the `-waitforever` and `-timeout` command-line branches in
     * CMainWindow::connectToSelectedMultiplayerSession @0048E764.
     */
    private static void applySelectedSessionConnectionTimeoutOptions() {
        String commandLine = Globals.commandLine;
        if (commandLine.contains("-waitforever")) {
            CLlDriver.setReliablePacketTimeoutMs(-1);
        }
        if (commandLine.contains("-timeout")) {
            CLlDriver.setReliablePacketTimeoutMs(Globals.networkTimeoutMillis);
        }
    }

    /**
     * Native: CMainWindow::connectToServerAddress @0048E90F.
     * Java port status: native direct-address route ported; the accepted-login wait uses a Java receipt latch because
     * the audited native accepted-payload global @00627594 is zero.
     */
    public void connectToServerAddress() {
        sessionMode = SESSION_MODE_MULTIPLAYER_CLIENT;
        CServerApp.setRemoteNetworkDriver(CLlDriver.class);
        CLlDriver.bindRemoteServerAppBoundary();
        CLlDriver.setProtocolId(ProtocolId.TCP_IP);
        applyDirectAddressConnectionTimeoutOptions();
        if (!CLlDriver.connectDirectAddressBoundary(connectionScratchState.directAddress, m_GameSession.m_PlayerName)) {
            CLlDriver.handleNetworkErrorAndClose();
            showDirectAddressConnectionError(PatchText.CAN_T_CONNECT_TO_SERVER_IP_ADDRESS_OR_URL_MAY_BE_INVALID_3);
            postMessage(MessageCodes.WM_CLOSE, 0, 0);
            return;
        }

        CServerApp.processRemoteNetworkEvents();
        connectionScratchState.acceptedCharacterFileOwnerId = 0;
        connectionScratchState.directAddressLoginAccepted = false;
        sendDirectAddressLoginRequest();
        int startTick = Globals.currentTickMillis();
        while (!connectionScratchState.directAddressLoginAccepted) {
            MessageSystem.pumpPostedMessage();
            if (CServerApp.getPendingSegmentMarkerCount() != 0) {
                if (!pMapVisualObject.handleGameAction(null, 100)) {
                    break;
                }
            } else if (Integer.compareUnsigned(Globals.currentTickMillis() - startTick, Globals.networkTimeoutMillis) > 0) {
                showDirectAddressConnectionError(PatchText.SERVER_IS_NOT_RESPONDING_0);
                postMessage(MessageCodes.WM_CLOSE, 0, 0);
                return;
            }
        }
        if (connectionScratchState.directAddressLoginAccepted) {
            postMessage(MessageCodes.SHOW_CHARACTER_LOADER_DIALOG, 0, 0);
            return;
        }
        showDirectAddressConnectionError(PatchText.INCORRECT_LOGIN_NAME_OR_PASSWORD_1);
        postMessage(MessageCodes.WM_CLOSE, 0, 0);
    }

    /**
     * Native support extracted from the `-waitforever` and `-timeout` command-line branches in
     * CMainWindow::connectToServerAddress @0048E90F.
     */
    private static void applyDirectAddressConnectionTimeoutOptions() {
        String commandLine = Globals.commandLine;
        if (commandLine.contains("-waitforever")) {
            CLlDriver.setReliablePacketTimeoutMs(-1);
        }
        if (commandLine.contains("-timeout")) {
            CLlDriver.setReliablePacketTimeoutMs(Globals.networkTimeoutMillis);
        }
    }

    /**
     * Native support extracted from sendDirectAddressLoginRequest @0040D6F6, called by
     * CMainWindow::connectToServerAddress @0048E90F.
     * Fully ported.
     */
    private void sendDirectAddressLoginRequest() {
        CServerApp.sendClientGameAction(LoginRequestAction.prepareForDirectAddressLogin(
                connectionScratchState.loginName,
                connectionScratchState.loginPassword
        ));
    }

    /**
     * Native support extracted from HeaderDialogVariantVisualObject prompt construction in
     * CMainWindow::connectToServerAddress @0048E90F.
     */
    private void showDirectAddressConnectionError(PatchText promptText) {
        showDialog(new HeaderDialogVariantVisualObject(
                1,
                0x40,
                100,
                0x17C,
                0x252,
                GameTexts.get(promptText),
                null,
                0
        ));
    }

    /**
     * Native: CMainWindow::continueDirectAddressPlayerJoin @0048ED21.
     * Fully ported.
     */
    public void continueDirectAddressPlayerJoin() {
        if (!pMapVisualObject.sendPlayerJoinAndWaitForPlayerList()) {
            showDialog(createDirectAddressJoinFailureDialog());
            postMessage(MessageCodes.WM_CLOSE, 0, 0);
            return;
        }
        if (m_GameSession.skipFormerCharacterPrompt == 0) {
            MessageCodes promptResult = showDialogAndAwaitResult(new HeaderDialogVariantVisualObject(
                    1,
                    0x40,
                    100,
                    0x1AC,
                    0x252,
                    GameTexts.get(MainText.YOUR_FORMER_CHARACTER_IS_STILL_AVAILABLE_FOR_PLAY_WOULD_YOU_LIKE_210),
                    null,
                    4
            ));
            if (promptResult != MessageCodes.HEADER_DIALOG_YES) {
                postMessage(MessageCodes.WM_CLOSE, 0, 0);
                return;
            }
        }
        postMessage(MessageCodes.RUN_MULTIPLAYER_SESSION_BOOTSTRAP, 0, 0);
    }

    /**
     * Native support extracted from failure prompt construction in CMainWindow::continueDirectAddressPlayerJoin @0048ED21.
     * Fully ported.
     */
    private static HeaderDialogVariantVisualObject createDirectAddressJoinFailureDialog() {
        int statusLowByte = Globals.multiplayerBootstrapStatusWord & 0xFF;
        String promptText;
        if (statusLowByte < PatchText.YOUR_CHARACTER_FILE_NOT_FOUND_IT_MAY_BE_DELETED_11.index()) {
            promptText = GameTexts.get(MAIN, MainText.byIndex(MULTIPLAYER_BOOTSTRAP_STATUS_MAIN_TEXT_BASE + statusLowByte));
        } else {
            promptText = GameTexts.get(PatchText.byIndex(statusLowByte));
        }
        return new HeaderDialogVariantVisualObject(
                1,
                0x40,
                100,
                0x17C,
                0x252,
                promptText,
                null,
                0
        );
    }

    /**
     * Native: CMainWindow::startDedicatedMultiplayerSession @0048F156.
     * Partially ported: Java additionally starts raw TCP/IP for the visible TCP-only replacement route.
     */
    public void startDedicatedMultiplayerSession() {
        sessionMode = SESSION_MODE_DEDICATED_SERVER;
        CServerApp.setLocalNetworkDriver(CLlDriver.class);
        CLlDriver.bindLocalServerAppBoundary();
        int protocolId = CLlDriver.getProtocolId();
        if (protocolId == ProtocolId.TCP_IP) {
            if (!startJavaRawTcpVisibleMultiplayerServer()) {
                return;
            }
        } else if (protocolId == ProtocolId.DPSP_TCPIP || protocolId == ProtocolId.DPSP_IPX) {
            CLlDriver.startMultiplayerServerBoundary(
                    Globals.serverConfig.maxplayers,
                    m_GameSession.m_PlayerName,
                    null
            );
        }
        createServer(0);
        postMessage(MessageCodes.PREPARE_MULTIPLAYER_MAP_SELECTION, 0, 0);
    }

    /**
     * Native: CMainWindow::startHatDedicatedServer @0048EF1F.
     * Java port status: native dedicated-server setup ported; Java additionally marks the TCP listener as a visible
     * raw TCP session server so HAT/session-browser joins use the native DirectPlay login sentinel behavior.
     */
    public void startHatDedicatedServer() {
        sessionMode = SESSION_MODE_DEDICATED_SERVER;
        CServerApp.setLocalNetworkDriver(CLlDriver.class);
        CLlDriver.bindLocalServerAppBoundary();
        CLlDriver.setProtocolId(ProtocolId.TCP_IP);
        CLlDriver.enableVisibleRawTcpSessionServerBoundary();
        String bindAddress = getHatDedicatedServerBindAddress();
        boolean started = CLlDriver.startMultiplayerServerBoundary(
                Globals.serverConfig.maxplayers,
                m_GameSession.m_PlayerName,
                bindAddress
        );
        if (!started) {
            showDialog(new HeaderDialogVariantVisualObject(
                    1,
                    0x40,
                    100,
                    0x17C,
                    0x252,
                    GameTexts.get(PatchText.CAN_T_START_SERVER_IP_ADDRESS_MAY_BE_INVALID_2),
                    null,
                    0
            ));
            postMessage(MessageCodes.WM_CLOSE, 0, 0);
            return;
        }
        createServer(0);
        postMessage(MessageCodes.PREPARE_MULTIPLAYER_MAP_SELECTION, 0, 0);
        Globals.passwordManager.setPasswordFile("passbase.txt");
    }

    /**
     * Java support boundary for the headless dedicated launcher. Reuses the native
     * CMainWindow::startHatDedicatedServer @0048EF1F TCP/server setup without visual failure dialogs or posted map UI.
     * not ported.
     */
    public boolean startHeadlessDedicatedServer(String mapName, String bindAddress) {
        sessionMode = SESSION_MODE_DEDICATED_SERVER;
        CServerApp.setLocalNetworkDriver(CLlDriver.class);
        CLlDriver.bindLocalServerAppBoundary();
        CLlDriver.setProtocolId(ProtocolId.TCP_IP);
        CLlDriver.enableVisibleRawTcpSessionServerBoundary();
        boolean started = CLlDriver.startMultiplayerServerBoundary(
                Globals.serverConfig.maxplayers,
                m_GameSession.m_PlayerName,
                bindAddress
        );
        if (!started) {
            return false;
        }
        if (createServer(0) != 0) {
            CLlDriver.handleNetworkErrorAndClose();
            return false;
        }
        setDedicatedServerMapName(mapName);
        Globals.passwordManager.setPasswordFile("passbase.txt");
        return runSessionBootstrap(0);
    }

    /**
     * Native support extracted from CString::operator= calls in CMainWindow::WindowProc @004852D8 and the
     * CMainWindow::runSessionBootstrap @0048C8A3 map-name handoff.
     */
    private void setDedicatedServerMapName(String mapName) {
        map_.set(mapName.getBytes(StandardCharsets.ISO_8859_1));
    }

    /**
     * Native support extracted from the `-ip"` command-line branch in
     * CMainWindow::startHatDedicatedServer @0048EF1F.
     */
    private static String getHatDedicatedServerBindAddress() {
        String commandLine = Globals.commandLine;
        String ipOptionMarker = "-ip\"";
        int ipOptionIndex = commandLine.indexOf(ipOptionMarker);
        if (ipOptionIndex == -1) {
            return "0.0.0.0";
        }
        int valueStart = ipOptionIndex + ipOptionMarker.length();
        int valueEnd = commandLine.indexOf('"', valueStart);
        if (valueEnd == -1) {
            return "0.0.0.0";
        }
        return commandLine.substring(valueStart, Math.min(valueEnd, valueStart + 0xFF));
    }

    /**
     * Native: CMainWindow::selectNetworkHostDriver @0048F1E2.
     * Fully ported.
     */
    public void selectNetworkHostDriver() {
        sessionMode = SESSION_MODE_NETWORK_HOST;
        CServerApp.setLocalNetworkDriver(CLlDriver.class);
    }

    /**
     * Java support adapted from CMainWindow::startHatDedicatedServer @0048EF1F and
     * CMainWindow::startDedicatedMultiplayerSession @0048F156 for the visible raw TCP/IP Create Game listener.
     */
    private boolean startJavaRawTcpVisibleMultiplayerServer() {
        CLlDriver.bindLocalServerAppBoundary();
        CLlDriver.enableVisibleRawTcpSessionServerBoundary();
        boolean started = CLlDriver.startMultiplayerServerBoundary(
                Globals.serverConfig.maxplayers,
                m_GameSession.m_PlayerName,
                null
        );
        if (!started) {
            CLlDriver.handleNetworkErrorAndClose();
            showDialog(new HeaderDialogVariantVisualObject(
                    1,
                    0x40,
                    100,
                    0x17C,
                    0x252,
                    GameTexts.get(PatchText.CAN_T_START_SERVER_IP_ADDRESS_MAY_BE_INVALID_2),
                    null,
                    0
            ));
        }
        return started;
    }

    /**
     * Native support extracted from DiplomacySettingsDialogVisualObject close branch in CMainWindow::onDialogClosed @004891D8.
     */
    private void commitDiplomacySettingsDialog() {
        int diplomacyIndex = 0;
        for (CPlayer player : pMapVisualObject.clientPlayers) {
            if (player != null && (player.flags & 0x1) == 0) {
                Diplomacy diplomacy = m_Dilpomacy.m_pDiplomacyArray.get(diplomacyIndex);
                int relationFlags = pMapVisualObject.currentPlayer.diplomacyFlags[player.playerId] & 0x68;
                if (diplomacy.enemy) {
                    relationFlags |= CPlayer.ENEMY_MASK;
                }
                if (diplomacy.alliance) {
                    relationFlags |= CPlayer.ALLIED_MASK;
                }
                if (diplomacy.visible) {
                    relationFlags |= CPlayer.DIPLOMACY_VISIBLE_MASK;
                }
                if (diplomacy.silent) {
                    relationFlags |= CPlayer.SILENT_DIPLOMACY_MASK;
                }
                pMapVisualObject.currentPlayer.diplomacyFlags[player.playerId] = (short) relationFlags;
                pMapVisualObject.currentPlayer.diplomacyFlags[pMapVisualObject.currentPlayer.playerId] = 0x3A;
                diplomacyIndex++;
            }
        }
        pMapVisualObject.sendDiplomacyRelationsAction();
    }

    /**
     * Native support extracted from the `dialogsMask == 1 && sessionMode == CAMPAIGN` tail in
     * CMainWindow::onDialogClosed @004891D8.
     */
    private void resetCampaignGameplayTimingAfterModalClose() {
        if (DialogsMaskFlag.isExactly(dialogsMask, GAMEPLAY) && sessionMode == SESSION_MODE_CAMPAIGN) {
            resetGameplayTimingAfterModalClose();
        }
    }

    /**
     * Native support extracted from the town-dialog branches in CMainWindow::onDialogClosed @004891D8.
     */
    private boolean isTownDialogClose(CVisualObject dialog) {
        return dialog == pBasicTownDialogVisualObject
                || dialog == pDruidTownDialogVisualObject
                || dialog == pKaargTownDialogVisualObject;
    }

    /**
     * Native support extracted from the shop-dialog branch in CMainWindow::onDialogClosed @004891D8.
     */
    private boolean isShopDialogClose(CVisualObject dialog) {
        return dialog == pShopDialogVisualObject
                || dialog == pDruidShopDialogVisualObject
                || dialog == pKaargShopDialogVisualObject;
    }

    /**
     * Native support extracted from the inn-dialog branch in CMainWindow::onDialogClosed @004891D8.
     */
    private boolean isInnDialogClose(CVisualObject dialog) {
        return dialog == pBasicInnDialogVisualObject
                || dialog == pDruidInnDialogVisualObject
                || dialog == pKaargInnDialogVisualObject;
    }

    /**
     * Native support extracted from the CharacterLoaderDialogVisualObject branch in CMainWindow::onDialogClosed @004891D8.
     */
    private void continueAfterCharacterLoaderDialogClose(MessageCodes closeReason) {
        if (closeReason == MessageCodes.RETURN_TO_GAME) {
            postMessage(
                    isCharacterLoaderReturnWindowCloseRequested() ? MessageCodes.WM_CLOSE : MessageCodes.SHOW_MAIN_MENU,
                    0,
                    0
            );
        } else if ((m_GameSession.initialized & 0x4) == 0) {
            showStartGameSetupForNewSession();
        } else if (connectionScratchState.serverListSourceIsWebPage == 0) {
            postMessage(MessageCodes.SHOW_MULTIPLAYER_CONNECTION_DIALOG, 0, 0);
        } else {
            postMessage(MessageCodes.SHOW_HAT_SERVER_BROWSER_DIALOG, 0, 0);
        }
    }

    /**
     * Native support extracted from CString::IsEmpty(&(CMainWindow::connectionScratchState).directAddress) in
     * CMainWindow::onDialogClosed @004891D8.
     */
    private boolean isCharacterLoaderReturnWindowCloseRequested() {
        return !connectionScratchState.directAddress.isEmpty();
    }

    /**
     * Native support extracted from the StartGameSetupDialogVisualObject branch in CMainWindow::onDialogClosed @004891D8.
     */
    private void continueAfterStartGameSetupAccept() {
        StartGameSetupDialogVisualObject dialog = pStartGameSetupDialogVisualObject;
        String leaderName = dialog.getLeaderName();
        String enteredClanName = dialog.getClanName();
        String clanName = enteredClanName;
        m_GameSession.m_PlayerName = leaderName;

        int colonIndex = clanName.indexOf(':');
        if (!clanName.isEmpty() && colonIndex != -1) {
            int clanServerId = parseLeadingIntOrZero(clanName.substring(colonIndex + 1));
            m_GameSession.clanServerId = clanServerId >= 1 && clanServerId <= 0x10 ? clanServerId : 0;
            clanName = clanName.substring(0, colonIndex);
        }
        if (!clanName.isEmpty()) {
            m_GameSession.m_PlayerName += "_" + clanName;
        }

        m_FameHall.setSelectedDifficulty(dialog.getCommittedDifficultyIndex());
        m_GameSession.type = dialog.getCommittedPortraitPlayerType() << 6;

        CUnit selectedUnit = pMapVisualObject.getSelectedCUnit();
        selectedUnit.name = truncateNativeCString(leaderName, 0x0C);
        selectedUnit.clan = truncateNativeCString(enteredClanName, 0x0C);

        m_GameSession.refreshSetupAcceptedCharacterSession();
        showCharacterGeneratorAfterStartGameSetup();
    }

    /**
     * Native support extracted from the CharacterGeneratorDialogVisualObject branch in CMainWindow::onDialogClosed @004891D8.
     */
    private void continueAfterCharacterGeneratorAccept() {
        m_GameSession.initialized |= 0x4;
        if (sessionMode == SESSION_MODE_CAMPAIGN) {
            pMapVisualObject.connectAndJoinSession();
            m_GameSession.submitCharacterSetupAndWaitForSelectedUnit();
            Globals.scenarioLib.setVar(
                    0x308,
                    (m_GameSession.type & CGameSession.SESSION_TYPE_MAGE) != 0 ? 1 : 0
            );
            Globals.scenarioLib.setVar(
                    0x30D,
                    (m_GameSession.type & CGameSession.SESSION_TYPE_FEMALE) != 0 ? 1 : 0
            );
            if (Globals.gameServer.isServerLifecycleAllocated()) {
                Globals.gameServer.setDifficultyLevelSetting(m_FameHall.getSelectedDifficulty() + 1);
            }
            postMessage(MessageCodes.SHOW_CURRENT_TOWN_DIALOG, 0, 0);
        } else {
            m_GameSession.saveSelectedCharacterFile();
            if (connectionScratchState.serverListSourceIsWebPage == 0) {
                postMessage(MessageCodes.SHOW_MULTIPLAYER_CONNECTION_DIALOG, 0, 0);
            } else {
                postMessage(MessageCodes.SHOW_HAT_SERVER_BROWSER_DIALOG, 0, 0);
            }
        }
    }

    /**
     * Native support extracted from GetInt callers in CMainWindow::onDialogClosed @004891D8.
     */
    private static int parseLeadingIntOrZero(String text) {
        String value = text.stripLeading();
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
     * Native: CMainWindow::showCharacterLoaderDialog @0048B0D1.
     * Fully ported.
     */
    public void showCharacterLoaderDialog() {
        CMousePointer.Cursor_Wait.setToMousePointer();
        pSelectionInfoPanelVisualObject.selectionInfoViewMode0x70 = 1;
        inputController.addChild(pCharacterLoaderDialogVisualObject);
        pCharacterLoaderDialogVisualObject.showDialog();
        dialogsMask = CHARACTER_LOADER.includeTo(dialogsMask);
        inputController.draw();
        field149_0x44C = 0;
        CMousePointer.Cursor_Default.setToMousePointer();
    }

    /**
     * Native: CMainWindow::playGameplayMusicPlaylist @004924CD.
     * Java port status: fully ported.
     */
    public void playGameplayMusicPlaylist() {
        if (Globals.soundPreferences.musicAvailable == 0) {
            return;
        }
        musicPlayer.setMusicFileNames(List.of(
                "music/b00.wav",
                "music/b01.wav",
                "music/b02.wav",
                "music/b03.wav",
                "music/b04.wav",
                "music/b05.wav",
                "music/b06.wav",
                "music/b07.wav",
                "music/b08.wav",
                "music/b09.wav",
                "music/b10.wav",
                "music/b11.wav",
                "music/b12.wav",
                "music/b13.wav",
                "music/b14.wav",
                "music/b15.wav",
                "music/b16.wav"
        ));
        refreshGameplayMusicAnchor();
        int currentMusicTrack = resolveCurrentMusicTrack();
        if (currentMusicTrack >= 0) {
            musicPlayer.selectTrack(currentMusicTrack);
        }
        musicPlayer.play();
    }

    /**
     * Native support extracted from the `MapVisualObject::pCUnit` position fetch around
     * `updatePreferredGameplayTrackIndex(unitX, unitY)` inside CMainWindow::playGameplayMusicPlaylist @004924CD.
     */
    private void refreshGameplayMusicAnchor() {
        CUnit selectedUnit = pMapVisualObject.getSelectedCUnit();
        if (selectedUnit != null) {
            GameplayMusicSupport.updatePreferredGameplayTrackIndex(selectedUnit.location.x, selectedUnit.location.y);
        }
    }

    /**
     * Native support extracted from `g_CurrentMusicTrack @005F1A08` inside CMainWindow::playGameplayMusicPlaylist @004924CD.
     */
    private static int resolveCurrentMusicTrack() {
        return GameplayMusicSupport.getCurrentMusicTrack();
    }

    /**
     * Native: CMainWindow::showInvalidMediaAllodsLogo @0048F72B.
     * Fully ported. Java uses resource-normalized `/` separators for native `main\graphics\logo\allods.bmp`.
     */
    public void showInvalidMediaAllodsLogo() {
        dialogsMask = CUTSCENE_PLAYBACK.includeTo(dialogsMask);
        Globals.blockingPlaybackActive = true;
        Globals.blockingPlaybackAbortRequested = false;
        try {
            Globals.mousePointer.hide();
            CBmp64k allodsLogoBitmap = new CBmp64k(INVALID_MEDIA_ALLODS_LOGO_BMP);
            clearTransitionSurface();
            allodsLogoBitmap.draw(Globals.mainWindowRect.left, Globals.mainWindowRect.top, 0, null, false);
            Globals.presentCurrentSurface.run();

            int startTick = Globals.currentTickMillis();
            while (!Globals.shouldAbortBlockingPlayback.getAsBoolean()
                    && Integer.compareUnsigned(Globals.currentTickMillis() - startTick, INVALID_MEDIA_LOGO_WAIT_MILLIS) <= 0) {
                Globals.presentCurrentSurface.run();
                LockSupport.parkNanos(1_000_000L);
            }
        } finally {
            dialogsMask = CUTSCENE_PLAYBACK.excludeIn(dialogsMask);
            try {
                clearTransitionSurface();
                Globals.presentCurrentSurface.run();
                Globals.mousePointer.show();
            } finally {
                Globals.blockingPlaybackActive = false;
                Globals.blockingPlaybackAbortRequested = false;
            }
        }
    }

    /**
     * Native: CMainWindow::playLocationCutscene @0048F919.
     * Fully ported. Mirrors the video-resource gate, preference bit persistence, `video/<cutpath>/<nn>.smk` probing,
     * blocking Smacker playback, input interruption, and pre/post black-screen presentation.
     */
    public void playLocationCutscene(int cutsceneId) {
        musicPlayer.stopPlayback();
        if (!Globals.videoResourcesAvailable) {
            return;
        }
        dialogsMask = CUTSCENE_PLAYBACK.includeTo(dialogsMask);
        Globals.usingVxD |= 1 << (cutsceneId & 0x1F);
        ApplicationPreferences.saveApplicationPreferences(this);
        Globals.blockingPlaybackActive = true;
        Globals.blockingPlaybackAbortRequested = false;
        boolean mouseHidden = false;
        try {
            String cutscenePath = GameTexts.get(CutPathsText.byIndex(cutsceneId));
            for (int segment = 1; segment < CUTSCENE_SEGMENT_LIMIT_EXCLUSIVE; segment++) {
                String smkName = "video/%s/%02d.smk".formatted(cutscenePath, segment);
                if (!Globals.gameFileManager.exists(smkName)) {
                    continue;
                }
                if (!mouseHidden) {
                    Globals.mousePointer.hide();
                    mouseHidden = true;
                    clearTransitionSurface();
                    Globals.presentCurrentSurface.run();
                }
                if (!playSmkCutsceneSegment(smkName) || Globals.blockingPlaybackAbortRequested) {
                    break;
                }
            }
        } finally {
            dialogsMask = CUTSCENE_PLAYBACK.excludeIn(dialogsMask);
            try {
                if (mouseHidden) {
                    Globals.mousePointer.show();
                }
                clearTransitionSurface();
                Globals.presentCurrentSurface.run();
            } finally {
                Globals.blockingPlaybackActive = false;
                Globals.blockingPlaybackAbortRequested = false;
            }
        }
    }

    /**
     * Native: CMainWindow::playStartupLogoSmkIntro @0048FB2A.
     * Fully ported. Java uses resource-normalized `/` separators for native `video\logos\...` paths.
     */
    public boolean playStartupLogoSmkIntro() {
        musicPlayer.stopPlayback();
        if (!Globals.videoResourcesAvailable) {
            return false;
        }
        dialogsMask = CUTSCENE_PLAYBACK.includeTo(dialogsMask);
        Globals.blockingPlaybackActive = true;
        Globals.blockingPlaybackAbortRequested = false;
        try {
            Globals.mousePointer.hide();
            clearTransitionSurface();
            Globals.presentCurrentSurface.run();

            boolean completed = playSmkCutsceneSegment(STARTUP_PUBLISHER_LOGO_SMK);
            if (completed) {
                completed = playSmkCutsceneSegment(STARTUP_NIVAL_LOGO_SMK);
            }
            return completed;
        } finally {
            dialogsMask = CUTSCENE_PLAYBACK.excludeIn(dialogsMask);
            try {
                Globals.mousePointer.show();
                clearTransitionSurface();
                Globals.presentCurrentSurface.run();
            } finally {
                Globals.blockingPlaybackActive = false;
                Globals.blockingPlaybackAbortRequested = false;
            }
        }
    }

    /**
     * Native: CMainWindow::playSmkCutsceneSegment @0048FC2E.
     * Fully ported. Delegates Smacker decode/render/presentation to the Java SMKPlayer while preserving the blocking
     * success/abort return convention and native open-failure-as-success behavior.
     */
    private boolean playSmkCutsceneSegment(String smkName) {
        final SMKPlayer player;
        try {
            player = new SMKPlayer(smkName);
        } catch (Exception e) {
            // Native treats SMKPlayer::OpenWithRegistry failure as a completed segment.
            return true;
        }
        player.setCutsceneWindowOrigin(Globals.mainWindowRect.left, Globals.mainWindowRect.top);

        try (player) {
            return player.playBlocking(
                    Globals.renderer,
                    Globals.presentCurrentSurface,
                    Globals.shouldAbortBlockingPlayback
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to play cutscene segment: " + smkName, e);
        }
    }

    /**
     * Native support extracted from HandlerVisualObject::GetClosedReason calls in
     * CMainWindow::onDialogClosed @004891D8.
     */
    private static MessageCodes readDialogResult(CVisualObject dialog) {
        return ((HandlerVisualObject) dialog).getClosedReason();
    }

    /**
     * Native: CMainWindow::cleanupGameplayResources @0048DA92.
     * Fully ported.
     */
    public void cleanupGameplayResources() {
        pMapVisualObject.stopAmbientLoopingSoundsForCleanup();
        if (Globals.soundPreferences.musicAvailable != 0) {
            musicPlayer.stopPlayback();
        }
        VObjects.cleanupVObjects();
        UnitTypes.unloadGraphicsUnitsFilesForGameplayCleanup();
        TerrainGraphics.cleanupTerrainGraphics();
        Projectiles.cleanupProjectiles();
        Structures.cleanupStructures();
        dialogsMask = GAMEPLAY.excludeIn(dialogsMask);
    }

    /**
     * Native: CMainWindow::cleanupActiveSessionForMenuReturn @0048F2E5.
     * Fully ported. Java keeps g_GameServer as a fixed singleton, so the native g_GameServer null check is represented
     * by the singleton binding.
     */
    public void cleanupActiveSessionForMenuReturn() {
        if ((sessionMode == SESSION_MODE_CAMPAIGN || sessionMode == SESSION_MODE_NETWORK_HOST)
                && DialogsMaskFlag.contains(dialogsMask, GAMEPLAY)) {
            Globals.gameServer.returnToLobby();
        }
        if (CServerApp.hasActiveRemoteConnection() && sessionMode != SESSION_MODE_DEDICATED_SERVER) {
            pMapVisualObject.clearSessionForFirstPlayerLobbyReturn();
        }
        if (DialogsMaskFlag.contains(dialogsMask, GAMEPLAY)) {
            cleanupGameplayResources();
        }
    }

    /**
     * Native: CMainWindow::OnClose @00492235.
     * Fully ported. Java represents the CFrameWnd::OnClose framework tail as an app-window close request.
     */
    public void onClose() {
        cleanupActiveSessionForMenuReturn();
        dialogsMask = 0;
        CMousePointer.Cursor_Default.setToMousePointer();
        Globals.materialRuntimeData.releaseMaterialAndItemNameGlobals();
        windowCloseRequested = true;
    }

    /**
     * Native support boundary for the CFrameWnd::OnClose @005B46A0 tail called by CMainWindow::OnClose @00492235.
     * not ported.
     */
    public boolean isWindowCloseRequested() {
        return windowCloseRequested;
    }

    /**
     * Java support boundary for the no-GL dedicated-server launcher. Native always has a visual shell here, so Java uses
     * the absent renderer as the launcher-owned headless marker.
     * not ported.
     */
    private boolean isHeadlessDedicatedRuntime() {
        return sessionMode == SESSION_MODE_DEDICATED_SERVER && Globals.renderer == null;
    }

    /**
     * Native: CMainWindow::DestroyServer @0048AC34.
     * Fully ported. Java keeps the native `g_GameServer` as a fixed singleton, so GameServer::~GameServer @004EC3BE
     * is represented by explicit lifecycle cleanup before transport close and timer reset.
     */
    public void destroyServer() {
        Globals.gameServer.destroyServerLifecycle();
        CLlDriver.handleNetworkErrorAndClose();
        m_LastRenderTime = Integer.MAX_VALUE;
        m_LastTickTime = Integer.MAX_VALUE;
    }

    /**
     * Native: CMainWindow::loadSelectedCampaignSaveGame @0048DB37.
     * Fully ported.
     */
    public void loadSelectedCampaignSaveGame() {
        Globals.mousePointer.disableBackgroundCapture();
        m_GameSession.m_PlayerName = "Self";
        field62_0x3ec = 1;
        field63_0x3ed = 0;
        field64_0x3ee = 0;
        field65_0x3ef = 0;
        sessionMode = SESSION_MODE_CAMPAIGN;
        createServer(sessionMode);
        Globals.gameServer.loadSaveFile(mSaveFile.filename);
        if (!pMapVisualObject.connectAndJoinSession()) {
            postMessage(MessageCodes.SHOW_MAIN_MENU, 0, 0);
        } else if (!isSavedGameInBattle()) {
            pMapVisualObject.requestPlayerStateResync();
            Globals.gameServer.pumpServerWorldActions();
            while (CServerApp.getPendingSegmentMarkerCount() != 0) {
                pMapVisualObject.handleGameAction(null, 100);
            }
            loadSaveGame();
            pMapVisualObject.refreshShopShelves();
            postMessage(MessageCodes.SHOW_CURRENT_TOWN_DIALOG, 0, 0);
        } else {
            runSessionBootstrap(1);
        }
        Globals.mousePointer.enableBackgroundCapture();
        pGlobalMapDialogVisualObject.rebuildScenarioLocations();
    }

    /**
     * Native: CMainWindow::runSessionBootstrap @0048C8A3.
     * Fully ported for the visual runtime; Java headless dedicated mode skips only the visual console shell.
     */
    public boolean runSessionBootstrap(int bootstrapMode) {
        beginSessionBootstrap();
        if (sessionMode == SESSION_MODE_DEDICATED_SERVER && !isHeadlessDedicatedRuntime()) {
            int screenWidth = Globals.screenRect.right;
            int screenHeight = Globals.screenRect.bottom;
            CRect consoleLogRect = new CRect();
            consoleLogRect.set(Globals.screenRect);
            consoleLogRect.bottom -= 0x48;
            CGameListControl consoleMessageList = pMapVisualObject.gameListControl;
            consoleMessageList.configureMessageRect(consoleLogRect);
            pDedicatedServerConsoleVisualObject = new DedicatedServerConsoleVisualObject(
                    1,
                    0,
                    0,
                    screenWidth,
                    screenHeight,
                    consoleMessageList
            );
            Globals.gameServer.dedicatedServerConsoleActive = 1;
            clearTransitionSurface();
            showDialog(pDedicatedServerConsoleVisualObject);
        }
        String mapName = resolveBootstrapMapName(bootstrapMode);
        if (bootstrapMode == 0 && serverBootstrapEnabled != 0) {
            beginScenarioBootstrap(mapName);
        }
        if (sessionMode != SESSION_MODE_DEDICATED_SERVER) {
            prepareNonDedicatedBootstrapResources();
            scenarioCameraOverrideLock = bootstrapMode;
            replayBootstrapMapPackets(bootstrapMode);
            if (serverBootstrapEnabled != 0) {
                startBootstrapGameServerSession();
            }
            if (!waitForBootstrapMapDescriptor()) {
                return false;
            }
            scenarioCameraOverrideLock = 0;
        }
        if (sessionMode == SESSION_MODE_CAMPAIGN && bootstrapMode != 0) {
            restoreSavedSessionState();
        }
        finishSessionBootstrapUi(bootstrapMode);
        return true;
    }

    /**
     * Native support extracted from the opening shell of CMainWindow::runSessionBootstrap @0048C8A3.
     */
    private void beginSessionBootstrap() {
        pMapVisualObject.mapDescriptor = null;
        pMapVisualObject.cachedMapWidth = 0;
        pMapVisualObject.cachedMapHeight = 0;
        if (!isHeadlessDedicatedRuntime()) {
            CMousePointer.Cursor_Wait.setToMousePointer();
            if (Globals.soundPreferences.musicAvailable != 0) {
                musicPlayer.stopPlayback();
            }
        }
    }

    /**
     * Native support extracted from the map-name selection block in CMainWindow::runSessionBootstrap @0048C8A3.
     */
    private String resolveBootstrapMapName(int bootstrapMode) {
        if (sessionMode == SESSION_MODE_CAMPAIGN && bootstrapMode == 0) {
            return ScenarioProgressSupport.scenarioCurrentLocationId() + ".alm";
        }
        return map_.toString();
    }

    /**
     * Native support extracted from the GameServer::LoadMapByName @004EB715 call site in
     * CMainWindow::runSessionBootstrap @0048C8A3.
     */
    private void beginScenarioBootstrap(String mapName) {
        Globals.gameServer.loadSavedGameOnMapLoad = 0;
        Globals.gameServer.loadMapByName(mapName);
    }

    /**
     * Native support extracted from the `SESSION_MODE_CAMPAIGN && param_1 != 0` save-restore block in
     * CMainWindow::runSessionBootstrap @0048C8A3.
     */
    private void restoreSavedSessionState() {
        ResInHeap saveState = readSavedSessionState();
        int inBattle = restoreSavedSessionOptions(saveState);
        restoreSavedObjectSelectionAndGroups(pMapVisualObject, saveState);
        restoreSavedProjectiles(pMapVisualObject, saveState);
        restoreSavedFog(pMapVisualObject, saveState, inBattle);
        pMapVisualObject.updateSelectionState();
    }

    /**
     * Native support extracted from the CGameFile/ResInHeap/CFameHall/ScenarioLoad/PlayerSlot load sequence in
     * CMainWindow::runSessionBootstrap @0048C8A3.
     */
    private ResInHeap readSavedSessionState() {
        try {
            ByteBuffer saveBuffer = ResInHeap.openSavedGameResourceState(mSaveFile.filename);
            ResInHeap saveState = ResInHeap.read(saveBuffer, -1, "");
            m_FameHall.read(saveBuffer);
            Globals.scenarioLib.load(saveBuffer);
            for (PlayerSlot playerSlot : m_GameSession.m_PlayerSlots) {
                playerSlot.load(saveBuffer);
            }
            return saveState;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read saved session state from " + mSaveFile.filename, e);
        }
    }

    /**
     * Native support extracted from the CurrentState/Character/GameOptions/View/SpellBook/Inventory restore block in
     * CMainWindow::runSessionBootstrap @0048C8A3.
     */
    private int restoreSavedSessionOptions(ResInHeap saveState) {
        int inBattle = saveState.getInt("CurrentState", "InBattle", 1);
        StringBuilder playerName = new StringBuilder(0x20);
        saveState.getValueAsString("Character", "Name", "No name", playerName, 0x20);
        m_GameSession.m_PlayerName = truncateNativeCString(playerName.toString(), 0x1F);

        pMapVisualObject.wimpyMode = saveState.getInt("GameOptions", "Wimpy", pMapVisualObject.wimpyMode);
        pMapVisualObject.showHitPointBars = saveState.getInt("GameOptions", "ShowHP", pMapVisualObject.showHitPointBars);
        pMapVisualObject.showFlyingHitPointBars = saveState.getInt(
                "GameOptions",
                "FlyingHP",
                pMapVisualObject.showFlyingHitPointBars
        );
        pMapVisualObject.formationMode = saveState.getInt("GameOptions", "Formation", pMapVisualObject.formationMode);
        gameSpeed = saveState.getInt("GameOptions", "Speed", gameSpeed);
        Globals.gamePreferences.showTimeFlow = saveState.getInt("GameOptions", "ShowTimeFlow", Globals.gamePreferences.showTimeFlow);
        pMapVisualObject.view.x = saveState.getInt("View", "X", pMapVisualObject.view.x);
        pMapVisualObject.view.y = saveState.getInt("View", "Y", pMapVisualObject.view.y);

        pMapVisualObject.setSpellPanelOpen(saveState.getInt("SpellBook", "IsOpen", 0) != 0);
        pSpellPanelVisualObject.pressedSpellSlot = saveState.getInt(
                "SpellBook",
                "Pressed",
                pSpellPanelVisualObject.pressedSpellSlot
        );
        pMapVisualObject.setSelectionPanelOpen(saveState.getInt("Inventory", "IsOpen", 0) != 0);
        return inBattle;
    }

    /**
     * Native support extracted from the Objects/Selection and Objects/Group%d restore block in
     * CMainWindow::runSessionBootstrap @0048C8A3.
     */
    private static void restoreSavedObjectSelectionAndGroups(MapVisualObject mapVisualObject, ResInHeap saveState) {
        List<Short> objectIds = new ArrayList<>();
        if (saveState.getShortArray("Objects", "Selection", objectIds)) {
            for (short objectId : objectIds) {
                CGameObject object = mapVisualObject.getObjectByToken(objectId);
                if (object != null) {
                    object.setSelected(true);
                }
            }
        }

        for (int groupIndex = 0; groupIndex < 10; groupIndex++) {
            objectIds.clear();
            if (!saveState.getShortArray("Objects", "Group%d".formatted(groupIndex), objectIds)) {
                continue;
            }
            for (short objectId : objectIds) {
                CGameObject object = mapVisualObject.getObjectByToken(objectId);
                if (object != null) {
                    object.addToGroup(groupIndex);
                }
            }
        }
    }

    /**
     * Native support extracted from the Projectiles/IDs and Prj%d restore block in
     * CMainWindow::runSessionBootstrap @0048C8A3.
     */
    private static void restoreSavedProjectiles(MapVisualObject mapVisualObject, ResInHeap saveState) {
        mapVisualObject.nextTransientObjectToken = (short) saveState.getInt(
                "Projectiles",
                "FreeIndex",
                Short.toUnsignedInt(mapVisualObject.nextTransientObjectToken)
        );
        List<Short> projectileIds = new ArrayList<>();
        if (!saveState.getShortArray("Projectiles", "IDs", projectileIds)) {
            return;
        }

        for (short projectileId : projectileIds) {
            int projectileIdUnsigned = Short.toUnsignedInt(projectileId);
            String sectionName = "Prj%d".formatted(projectileIdUnsigned);
            CProjectile projectile = new CProjectile();
            projectile.location.x = saveState.getInt(sectionName, "x", projectile.location.x);
            projectile.location.y = saveState.getInt(sectionName, "y", projectile.location.y);
            projectile.location2.x = projectile.location.x;
            projectile.location2.y = projectile.location.y;
            projectile.z = saveState.getInt(sectionName, "z", projectile.z);
            projectile.type = saveState.getInt(sectionName, "picture", projectile.type);
            projectile.dir = saveState.getInt(sectionName, "dir", projectile.dir);
            projectile.phase = saveState.getInt(sectionName, "phase", projectile.phase);
            projectile.lastAction = saveState.getInt(sectionName, "lastaction", projectile.lastAction);
            projectile.action = (byte) saveState.getInt(sectionName, "action", projectile.action);
            projectile.actionDir = (byte) saveState.getInt(sectionName, "actiondir", projectile.actionDir);
            projectile.actionTarget = (short) saveState.getInt(
                    sectionName,
                    "actiontarget",
                    Short.toUnsignedInt(projectile.actionTarget)
            );
            projectile.actionX = saveState.getInt(sectionName, "actionx", projectile.actionX);
            projectile.actionY = saveState.getInt(sectionName, "actiony", projectile.actionY);
            projectile.actionZ = saveState.getInt(sectionName, "actionz", projectile.actionZ);
            projectile.actionPhase = saveState.getInt(sectionName, "actionphase", projectile.actionPhase);
            projectile.actionSegments = saveState.getInt(sectionName, "actionsegments", projectile.actionSegments);
            projectile.actionSpell = saveState.getInt(sectionName, "actionspell", projectile.actionSpell);
            projectile.cPlayer = mapVisualObject.currentPlayer;
            mapVisualObject.putTransientObjectByToken(projectileId, projectile);
            projectile.refreshMapDerivedState();
        }
    }

    /**
     * Native support extracted from the Fog/FirstState and Fog/Data restore block in
     * CMainWindow::runSessionBootstrap @0048C8A3.
     */
    private static void restoreSavedFog(MapVisualObject mapVisualObject, ResInHeap saveState, int inBattle) {
        if (inBattle == 0) {
            return;
        }
        List<Integer> fogRuns = new ArrayList<>();
        if (!saveState.getIntArray("Fog", "Data", fogRuns)) {
            return;
        }

        int fogState = saveState.getInt("Fog", "FirstState", 0);
        short[] tiles = mapVisualObject.mapDescriptor.tilesWxH;
        int tileIndex = 0;
        for (int runLength : fogRuns) {
            for (int runIndex = 0; runIndex < runLength; runIndex++) {
                tiles[tileIndex] = (short) (tiles[tileIndex] | fogState);
                tileIndex++;
            }
            fogState ^= 0x8000;
        }
    }

    /**
     * Native support boundary extracted from CMainWindow::restoreGameplayVisualTree @00483FE2, FUN_00478EB6,
     * LoadStructures, and LoadProjectiles in the non-dedicated bootstrap block of
     * CMainWindow::runSessionBootstrap @0048C8A3. Java reapplies the scalable right-panel layout before the native
     * visual-tree reset.
     */
    private void prepareNonDedicatedBootstrapResources() {
        applyGameplayRightPanelLayout();
        restoreGameplayVisualTree();
        prepareBootstrapResourcesAfterVisualReset();
    }

    /**
     * Native support extracted from the FUN_0041C50D, FUN_0041A4EF, FUN_0041A617, and FUN_0041A5B9 sequence in the
     * non-dedicated bootstrap block of CMainWindow::runSessionBootstrap @0048C8A3.
     */
    private void replayBootstrapMapPackets(int bootstrapMode) {
        pMapVisualObject.requestMapLoad(bootstrapMode);
        pMapVisualObject.applyWimpyMode(pMapVisualObject.wimpyMode);
        pMapVisualObject.applyFormationMode(pMapVisualObject.formationMode);
        pMapVisualObject.applyAutoCasting();
    }

    /**
     * Native support extracted from FUN_00478EB6 @00478EB6, LoadStructures, and LoadProjectiles @0047CEE2 in the
     * non-dedicated bootstrap block of CMainWindow::runSessionBootstrap @0048C8A3.
     */
    private static void prepareBootstrapResourcesAfterVisualReset() {
        loadBootstrapObjectResources();
        loadBootstrapStructureResources();
        loadBootstrapProjectileResources();
    }

    /**
     * Native support extracted from LoadVObjects @00478EB6, called by CMainWindow::runSessionBootstrap @0048C8A3
     * during non-dedicated bootstrap.
     */
    private static void loadBootstrapObjectResources() {
        VObjects.loadVObjects();
    }

    /**
     * Native support extracted from LoadStructures @0047D8AF, called by CMainWindow::runSessionBootstrap @0048C8A3
     * during non-dedicated bootstrap.
     */
    private static void loadBootstrapStructureResources() {
        Structures.loadStructures();
    }

    /**
     * Native support extracted from LoadProjectiles @0047CEE2, called by CMainWindow::runSessionBootstrap @0048C8A3
     * during non-dedicated bootstrap.
     */
    private static void loadBootstrapProjectileResources() {
        try {
            Projectiles.loadProjectiles();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to reload projectile resources during session bootstrap", e);
        }
    }

    /**
     * Native support extracted from GameServer::runServerLoopTick @004F08C0 in the non-dedicated bootstrap block of
     * CMainWindow::runSessionBootstrap @0048C8A3.
     */
    private static void startBootstrapGameServerSession() {
        Globals.gameServer.runServerLoopTick();
    }

    /**
     * Native support extracted from the map-descriptor handshake wait loop in
     * CMainWindow::runSessionBootstrap @0048C8A3.
     */
    private boolean waitForBootstrapMapDescriptor() {
        int startTick = Globals.currentTickMillis();
        while (pMapVisualObject.mapDescriptor == null) {
            MessageSystem.pumpPostedMessage();
            while (CServerApp.getPendingSegmentMarkerCount() == 0) {
                if (Integer.compareUnsigned(Globals.currentTickMillis() - startTick, Globals.networkTimeoutMillis) > 0) {
                    Globals.multiplayerBootstrapStatusWord = MAP_LOAD_TIMEOUT_STATUS_WORD;
                    return false;
                }
                CServerApp.processRemoteNetworkEvents();
            }
            if (!pMapVisualObject.handleGameAction(null, 100)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Native support extracted from the common post-bootstrap tail in CMainWindow::runSessionBootstrap @0048C8A3.
     */
    private void finishSessionBootstrapUi(int bootstrapMode) {
        if (!isHeadlessDedicatedRuntime()) {
            pRightPanelContainerVisualObject.onMessage(MessageCodes.NOTIFY_MAP_CONTEXT_CHANGED, 0, 0);
            inputController.draw();
        }
        if (sessionMode != SESSION_MODE_DEDICATED_SERVER) {
            refreshWorldUiAfterBootstrap();
            questEventDialogId = -1;
            completedMissionExitPending = 0;
            missionFailureDialogShown = 0;
        }
        if (sessionMode == SESSION_MODE_CAMPAIGN) {
            setGameSpeed(gameSpeed);
        } else {
            setGameSpeed(resolveServerConfiguredGameSpeed());
        }
        loadPostBootstrapTexts(bootstrapMode);
        if (!isHeadlessDedicatedRuntime()) {
            CMousePointer.Cursor_Default.setToMousePointer();
        }
    }

    /**
     * Native support extracted from the non-dedicated music refresh call in CMainWindow::runSessionBootstrap @0048C8A3.
     */
    private void refreshWorldUiAfterBootstrap() {
        playGameplayMusicPlaylist();
    }

    /**
     * Native support extracted from the non-single-player speed branch in CMainWindow::runSessionBootstrap @0048C8A3.
     */
    private static int resolveServerConfiguredGameSpeed() {
        return Globals.serverConfig.gameSpeed;
    }

    /**
     * Native support extracted from the mission/quest text tail in CMainWindow::runSessionBootstrap @0048C8A3.
     */
    private void loadPostBootstrapTexts(int bootstrapMode) {
        if (sessionMode == SESSION_MODE_CAMPAIGN) {
            ScriptDataSupport.loadCampaignMissionScriptData(ScenarioProgressSupport.scenarioCurrentLocationId());
            if (bootstrapMode == 0) {
                postMessage(MessageCodes.WRITE_CURRENT_MISSION_RESUME_SAVE, 0, 0);
            }
        } else {
            ScriptDataSupport.loadQuestScriptData();
        }
    }

    /**
     * Native: CMainWindow::writeCurrentMissionResumeSave @0048DC9F.
     * Fully ported.
     */
    public void writeCurrentMissionResumeSave() {
        if (sessionMode == SESSION_MODE_CAMPAIGN) {
            Globals.gameServer.saveGameFile(mSaveFile.filename);
            m_LastRenderTime = Globals.currentTickMillis();
            m_FrameCounter = 0;
            m_LagAccumulator = 0;
            appendCurrentMissionSaveTitleBlock();
            appendCurrentMissionResumeState();
        } else {
            pMapVisualObject.sendSaveGameRequestAction(mSaveFile.filename);
        }
    }

    /**
     * Native support extracted from the campaign `CFile::Write(&mSaveFile, 0x100)` tail in
     * CMainWindow::writeCurrentMissionResumeSave @0048DC9F.
     */
    private void appendCurrentMissionSaveTitleBlock() {
        byte[] titleBlock = new byte[SAVE_FILE_TITLE_BLOCK_SIZE];
        byte[] titleBytes = mSaveFile.title.getBytes(SavedGameFiles.SAVE_FILE_CHARSET);
        System.arraycopy(titleBytes, 0, titleBlock, 0, Math.min(titleBytes.length, titleBlock.length - 1));
        try {
            Files.write(SavedGameFiles.resolvePath(mSaveFile.filename), titleBlock, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to append mission save title block to " + mSaveFile.filename, e);
        }
    }

    /**
     * Native support extracted from the ResInHeap/CFameHall/ScenarioSave/PlayerSlot tail in
     * CMainWindow::writeCurrentMissionResumeSave @0048DC9F.
     */
    private void appendCurrentMissionResumeState() {
        try {
            Files.write(
                    SavedGameFiles.resolvePath(mSaveFile.filename),
                    createCurrentMissionResumeState().writeAtPosition(),
                    StandardOpenOption.APPEND
            );
            Files.write(
                    SavedGameFiles.resolvePath(mSaveFile.filename),
                    serializeFameHallForMissionResumeSave(),
                    StandardOpenOption.APPEND
            );
            Files.write(
                    SavedGameFiles.resolvePath(mSaveFile.filename),
                    Globals.scenarioLib.saveToBytes(),
                    StandardOpenOption.APPEND
            );
            Files.write(
                    SavedGameFiles.resolvePath(mSaveFile.filename),
                    serializePlayerSlotsForMissionResumeSave(),
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to append mission resume state to " + mSaveFile.filename, e);
        }
    }

    /**
     * Native support extracted from the ResInHeap construction block in
     * CMainWindow::writeCurrentMissionResumeSave @0048DC9F.
     */
    private ResInHeap createCurrentMissionResumeState() {
        ResInHeap saveState = ResInHeap.create();
        int inBattle = GAMEPLAY.isSetIn(dialogsMask) ? GAMEPLAY.mask : 0;
        saveState.setInt("CurrentState", "InBattle", inBattle);
        saveState.setString("Character", "Name", m_GameSession.m_PlayerName);
        saveState.setInt("GameOptions", "Wimpy", pMapVisualObject.wimpyMode);
        saveState.setInt("GameOptions", "ShowHP", pMapVisualObject.showHitPointBars);
        saveState.setInt("GameOptions", "FlyingHP", pMapVisualObject.showFlyingHitPointBars);
        saveState.setInt("GameOptions", "Formation", pMapVisualObject.formationMode);
        saveState.setInt("GameOptions", "Speed", gameSpeed);
        saveState.setInt("GameOptions", "ShowTimeFlow", Globals.gamePreferences.showTimeFlow);
        saveState.setInt("View", "X", pMapVisualObject.view.x);
        saveState.setInt("View", "Y", pMapVisualObject.view.y);
        saveState.setInt("SpellBook", "IsOpen", pMapVisualObject.hasSpellPanelChild() ? 1 : 0);
        saveState.setInt("SpellBook", "Pressed", pSpellPanelVisualObject.pressedSpellSlot);
        saveState.setIntArray("SpellBook", "Shortcuts", List.of());
        pMapVisualObject.writeCurrentMissionResumeObjectState(saveState, inBattle);
        return saveState;
    }

    /**
     * Native support extracted from CFameHall::Write @004A8F96 call in
     * CMainWindow::writeCurrentMissionResumeSave @0048DC9F.
     */
    private byte[] serializeFameHallForMissionResumeSave() {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES * 3 + m_FameHall.m_Documents.size() * 8)
                .order(ByteOrder.LITTLE_ENDIAN);
        m_FameHall.write(buffer);
        return writtenBytes(buffer);
    }

    /**
     * Native support extracted from the PlayerSlot::writeToFile @0041DF0C loop in
     * CMainWindow::writeCurrentMissionResumeSave @0048DC9F.
     */
    private byte[] serializePlayerSlotsForMissionResumeSave() {
        int byteSize = 0;
        for (PlayerSlot playerSlot : m_GameSession.m_PlayerSlots) {
            byteSize += Integer.BYTES * 2 + playerSlot.dataSize;
        }
        ByteBuffer buffer = ByteBuffer.allocate(byteSize).order(ByteOrder.LITTLE_ENDIAN);
        for (PlayerSlot playerSlot : m_GameSession.m_PlayerSlots) {
            playerSlot.writeToFile(buffer);
        }
        return writtenBytes(buffer);
    }

    /**
     * Native support extracted from sequential CFile writes in
     * CMainWindow::writeCurrentMissionResumeSave @0048DC9F.
     */
    private static byte[] writtenBytes(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.position()];
        buffer.flip();
        buffer.get(bytes);
        return bytes;
    }

    /**
     * Native: CMainWindow::ShowCurrentTownDialog @0048BBC0.
     * Fully ported. Java uses resource-normalized `/` separators for native town text/music paths.
     */
    public void showCurrentTownDialog() {
        ScriptDataSupport.loadTownScriptData();
        CMousePointer.Cursor_Wait.setToMousePointer();
        restoreCoreVisualTreeForMenuTransition();
        List<String> townMusicTracks = new ArrayList<>(1);
        ScenarioLocation currentLocation = Globals.scenarioLib.getCurrentLocation();
        switch (currentLocation.id) {
            case 1 -> {
                townMusicTracks.add("music/b14.wav");
                inputController.addChild(pBasicTownDialogVisualObject);
                pBasicTownDialogVisualObject.showDialog();
            }
            case 2 -> {
                townMusicTracks.add("music/b16.wav");
                inputController.addChild(pKaargTownDialogVisualObject);
                pKaargTownDialogVisualObject.showDialog();
            }
            case 3 -> {
                townMusicTracks.add("music/b15.wav");
                inputController.addChild(pDruidTownDialogVisualObject);
                pDruidTownDialogVisualObject.showDialog();
            }
            default -> {
            }
        }
        inputController.draw();
        field149_0x44C = 0;
        playCurrentTownMusicIfNeeded(townMusicTracks);
        CMousePointer.Cursor_Default.setToMousePointer();
    }

    /**
     * Native: CMainWindow::showMainMenu @0048B569.
     * Fully ported. Java uses resource-normalized `/` separators for native `music\menu.wav`.
     */
    public void showMainMenu() {
        connectionScratchState.serverListSourceIsWebPage = 0;
        connectionScratchState.pendingSessionConnectionString = "";
        m_FameHall.zeroFactors();
        pMapVisualObject.clearMainMenuObjectRegistry();
        CMousePointer.Cursor_Wait.setToMousePointer();
        CVisualObject townDialogRoot = inputController.getChildById(0x3FC);
        if (townDialogRoot != null) {
            townDialogRoot.onMessage(MessageCodes.DIALOG_OK, 0, 0);
        }
        restoreCoreVisualTreeForMenuTransition();
        applyMainMenuDisabledButtonMask();
        inputController.addChild(pMainMenuVisualObject);
        pMainMenuVisualObject.showDialog();
        inputController.draw();
        field149_0x44C = 0;
        dialogsMask = MAIN_MENU.includeTo(dialogsMask);
        destroyServer();
        CMousePointer.Cursor_Select.setToMousePointer();
        playMainMenuMusic();
        removeQuestStatusDialog();
        Globals.terrainLightOverrideTransferMode = 0;
    }

    /**
     * Native support extracted from CMainWindow::showMainMenu @0048B569 and SaveFileExists @0043FF4A.
     */
    private void applyMainMenuDisabledButtonMask() {
        int disabledMask = Globals.mainMenuDisabledButtonMask;
        if (!SavedGameFiles.saveFileExists()) {
            disabledMask |= 0x10;
        }
        pMainMenuVisualObject.setDisabledButtonMask(disabledMask);
    }

    /**
     * Native support extracted from the music block inside CMainWindow::showMainMenu @0048B569.
     * Java keeps the recovered single-track menu playlist and replay gate without reproducing the native temporary
     * `CStringArray` allocation pattern.
     */
    private void playMainMenuMusic() {
        if (Globals.soundPreferences.musicAvailable == 0) {
            return;
        }
        List<String> currentTracks = musicPlayer.getMusicFileNames();
        String menuTrack = "music/menu.wav";
        if (currentTracks.isEmpty() || !menuTrack.equals(currentTracks.get(0))) {
            musicPlayer.setMusicFileNames(List.of(menuTrack));
        }
        musicPlayer.play();
    }

    /**
     * Native support extracted from the quest-status overlay removal tail in CMainWindow::showMainMenu @0048B569.
     * Java represents the native child destructor with detachment and normal object reclamation.
     */
    private void removeQuestStatusDialog() {
        CVisualObject questStatusDialog = pMapVisualObject.getChildById(0x12);
        if (questStatusDialog != null) {
            pMapVisualObject.removeChild(questStatusDialog);
        }
    }

    /**
     * Native support extracted from the music block inside CMainWindow::ShowCurrentTownDialog @0048BBC0.
     */
    private void playCurrentTownMusicIfNeeded(List<String> townMusicTracks) {
        if (Globals.soundPreferences.musicAvailable == 0) {
            return;
        }
        List<String> currentTracks = musicPlayer.getMusicFileNames();
        if (currentTracks.isEmpty() || !currentTracks.getFirst().equals(townMusicTracks.getFirst())) {
            musicPlayer.setMusicFileNames(townMusicTracks);
        }
        musicPlayer.play();
    }

    /**
     * Native: CMainWindow::showShopDialog @0048AEA8.
     * Fully ported.
     */
    public void showShopDialog(int shopDialogKind) {
        prepareTownServiceDialogShell();
        CMousePointer.Cursor_Wait.setToMousePointer();
        int townId = resolveShopTownId(shopDialogKind);
        if (townId == 1) {
            inputController.addChild(pShopDialogVisualObject);
            pShopDialogVisualObject.showDialog();
        } else if (townId == 2) {
            inputController.addChild(pKaargShopDialogVisualObject);
            pKaargShopDialogVisualObject.showDialog();
        } else if (townId == 3) {
            inputController.addChild(pDruidShopDialogVisualObject);
            pDruidShopDialogVisualObject.showDialog();
        }
        dialogsMask = SHOP_DIALOG.includeTo(dialogsMask);
        pMapVisualObject.updateSelectionState();
        pMapVisualObject.afterShopDialogShown();
        inputController.draw();
        field149_0x44C = 0;
        CMousePointer.Cursor_Default.setToMousePointer();
    }

    /**
     * Native: CMainWindow::showInnDialog @0048B885.
     * Fully ported.
     */
    public void showInnDialog(int innDialogKind, int innInteractionTargetTokenId) {
        prepareTownServiceDialogShell();
        CMousePointer.Cursor_Wait.setToMousePointer();
        int townId = resolveInnTownId(innDialogKind);
        if (townId == 1) {
            inputController.addChild(pBasicInnDialogVisualObject);
        } else if (townId == 2) {
            inputController.addChild(pKaargInnDialogVisualObject);
        } else if (townId == 3) {
            inputController.addChild(pDruidInnDialogVisualObject);
        }
        if (sessionMode == SESSION_MODE_CAMPAIGN) {
            Globals.gameServer.prepareInnEntryUnitUpdates(
                    Globals.scenarioLib.getVar(SCENARIO_CHAPTER_VAR_ID),
                    Globals.scenarioLib.enterInn()
            );
            pMapVisualObject.pumpPendingGameActions();
        } else {
            pMapVisualObject.prepareRemoteInnDialog();
        }
        BasicInnDialogVisualObject innDialog = (BasicInnDialogVisualObject) inputController.getChildById(0x44C);
        innDialog.setInnInteractionTargetTokenId(innInteractionTargetTokenId);
        innDialog.showDialog();
        dialogsMask = INN_DIALOG.includeTo(dialogsMask);
        pMapVisualObject.updateSelectionState();
        inputController.draw();
        field149_0x44C = 0;
        CMousePointer.Cursor_Default.setToMousePointer();
    }

    /**
     * Native support extracted from CMainWindow::showShopDialog @0048AEA8 and CMainWindow::showInnDialog @0048B885.
     */
    private void prepareTownServiceDialogShell() {
        if (Globals.mousePointer.isSelecting()) {
            Globals.mousePointer.finishSelectionDrag();
            clipCursorTo();
        }
        if (pMapVisualObject.hasSpellPanelChild()) {
            pMapVisualObject.removeSpellPanelForTownDialog();
        }
    }

    /**
     * Native support extracted from CMainWindow::showShopDialog @0048AEA8.
     */
    private int resolveShopTownId(int shopDialogKind) {
        if (sessionMode == SESSION_MODE_CAMPAIGN) {
            ScenarioLocation currentLocation = Globals.scenarioLib.getCurrentLocation();
            return currentLocation.id;
        }
        return switch (shopDialogKind) {
            case 0x22, 0x23 -> 1;
            case 0x5D, 0x5E, 0x5F -> 2;
            case 0x69, 0x6A, 0x6B -> 3;
            default -> 0;
        };
    }

    /**
     * Native support extracted from CMainWindow::showInnDialog @0048B885.
     */
    private int resolveInnTownId(int innDialogKind) {
        if (sessionMode == SESSION_MODE_CAMPAIGN) {
            ScenarioLocation currentLocation = Globals.scenarioLib.getCurrentLocation();
            return currentLocation.id;
        }
        return switch (innDialogKind) {
            case 0x43, 0x44, 0x45 -> 1;
            case 0x63, 0x64, 0x65 -> 2;
            case 0x6F, 0x70, 0x71 -> 3;
            default -> 0;
        };
    }

    /**
     * Native: CMainWindow::showStartupLogoDialog @0048BF4B.
     * Fully ported.
     */
    public void showStartupLogoDialog() {
        inputController.addChild(pStartupLogoDialogVisualObject);
        pStartupLogoDialogVisualObject.showDialog();
        field149_0x44C = 0;
        dialogsMask = STARTUP_LOGO.includeTo(dialogsMask);
    }

    /**
     * Native: CMainWindow::initializeRuntimeGraphicsAndAudio @004823E2.
     * Fully ported at the Java platform/backend boundaries. Native low-memory music-buffer branch is dead because
     * g_IsLowMemory_ALWAYS_ZERO is always zero in this target.
     */
    public void initializeRuntimeGraphicsAndAudio() {
        resetTerrainTileSet();
        CMousePointer.initCursors();
        loadInterfaceGraphicsForRuntimeInit();
        UnitTypes.loadUnitTypes();
        SoundManager.ensureLoaded();
        CMousePointer.Cursor_Default.setToMousePointer();
        Globals.mousePointer.show();
        initializeNativeSoundBackend();
        musicPlayer = new MusicPlayer(DEFAULT_MUSIC_BUFFER_BYTES);
        musicPlayer.setVolume(Globals.soundPreferences.musicVolume);
    }

    /**
     * Native support extracted from ResetTerrainTileSet @00476A54 call in CMainWindow::initializeRuntimeGraphicsAndAudio @004823E2.
     */
    private static void resetTerrainTileSet() {
        TerrainGraphics.resetTerrainTileSet();
    }

    /**
     * Native support extracted from GUI::loadInterfaceGraphics @00476A6D call in
     * CMainWindow::initializeRuntimeGraphicsAndAudio @004823E2.
     */
    private static void loadInterfaceGraphicsForRuntimeInit() {
        try {
            GUI.loadInterfaceGraphics();
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to load native interface graphics", exception);
        }
    }

    /**
     * Native support extracted from InitSound(hwnd, 0x10, null) branch in
     * CMainWindow::initializeRuntimeGraphicsAndAudio @004823E2.
     */
    private static void initializeNativeSoundBackend() {
        if (!SoundSystem.tryInitialize(NATIVE_SOUND_CHANNEL_COUNT)) {
            Globals.soundPreferences.musicAvailable = 0;
        }
    }

    /**
     * Native: CMainWindow::showCreditsDialog @0048BE11.
     * Fully ported. Java uses resource-normalized `/` separators for native `music\credit.wav`.
     */
    public void showCreditsDialog() {
        inputController.addChild(pCreditsDialogVisualObject);
        pCreditsDialogVisualObject.showDialog();
        inputController.draw();
        field149_0x44C = 0;
        dialogsMask = CREDITS.includeTo(dialogsMask);
        playSingleTrack("music/credit.wav");
    }

    /**
     * Native: CMainWindow::showFameHallDialog @0048B16D.
     * Fully ported.
     */
    public void showFameHallDialog() {
        CMousePointer.Cursor_Wait.setToMousePointer();
        inputController.addChild(pFameHallDialogVisualObject);
        pFameHallDialogVisualObject.showDialog();
        inputController.draw();
        field149_0x44C = 0;
        dialogsMask = FAME_HALL.includeTo(dialogsMask);
        CMousePointer.Cursor_Default.setToMousePointer();
    }

    /**
     * Native: CMainWindow::showFameHallDocumentDialog @0048B1F9.
     * Fully ported.
     */
    public void showFameHallDocumentDialog() {
        if (Globals.mousePointer.isSelecting()) {
            Globals.mousePointer.finishSelectionDrag();
            clipCursorTo();
        }
        CMousePointer.Cursor_Wait.setToMousePointer();
        inputController.addChild(pFameHallDocumentDialogVisualObject);
        pFameHallDocumentDialogVisualObject.showDialog();
        inputController.draw();
        field149_0x44C = 0;
        dialogsMask = FAME_HALL_DOCUMENT.includeTo(dialogsMask);
        CMousePointer.Cursor_Default.setToMousePointer();
    }

    /**
     * Native: CMainWindow::showGlobalMapDialog @0048B419.
     * Fully ported. Java uses resource-normalized `/` separators for native `music\map.wav`.
     */
    public void showGlobalMapDialog() {
        CMousePointer.Cursor_Wait.setToMousePointer();
        inputController.addChild(pGlobalMapDialogVisualObject);
        pGlobalMapDialogVisualObject.showDialog();
        inputController.draw();
        field149_0x44C = 0;
        dialogsMask = GLOBAL_MAP.includeTo(dialogsMask);
        playSingleTrack("music/map.wav");
        CMousePointer.Cursor_Default.setToMousePointer();
    }

    /**
     * Native support extracted from the music blocks inside CMainWindow::showCreditsDialog @0048BE11 and
     * CMainWindow::showGlobalMapDialog @0048B419.
     */
    private void playSingleTrack(String musicTrack) {
        if (Globals.soundPreferences.musicAvailable == 0) {
            return;
        }
        musicPlayer.setMusicFileNames(List.of(musicTrack));
        musicPlayer.play();
    }

    /**
     * Native: CMainWindow::restoreCoreVisualTreeForMenuTransition @004840BA.
     * Fully ported. Restores the map plus right-panel visual root for non-gameplay dialog transitions and refreshes
     * the right-panel map context.
     */
    public void restoreCoreVisualTreeForMenuTransition() {
        pRightPanelContainerVisualObject.clearChildren();
        pRightPanelContainerVisualObject.addChild(pSelectionInfoPanelVisualObject);
        inputController.clearChildren();
        inputController.addChild(pMapVisualObject);
        inputController.addChild(pRightPanelContainerVisualObject);
        dialogsMask = GAMEPLAY.excludeIn(dialogsMask);
        pRightPanelContainerVisualObject.onMessage(
                MessageCodes.SET_MAP_CONTEXT,
                pMapVisualObject,
                0
        );
    }

    /**
     * Native: CMainWindow::isSavedGameInBattle @0048C214.
     * Fully ported.
     */
    public boolean isSavedGameInBattle() {
        try {
            ResInHeap saveState = ResInHeap.read(ResInHeap.openSavedGameResourceState(mSaveFile.filename), -1, "");
            return saveState.getInt("CurrentState", "InBattle", 1) != 0;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read saved campaign state from " + mSaveFile.filename, e);
        }
    }

    /**
     * Native: CMainWindow::LoadSaveGame @0048C2E9.
     * Fully ported.
     */
    public void loadSaveGame() {
        try {
            ByteBuffer saveBuffer = ResInHeap.openSavedGameResourceState(mSaveFile.filename);
            ResInHeap saveState = ResInHeap.read(saveBuffer, -1, "");
            m_FameHall.read(saveBuffer);
            Globals.scenarioLib.load(saveBuffer);
            for (PlayerSlot playerSlot : m_GameSession.m_PlayerSlots) {
                playerSlot.load(saveBuffer);
            }
            restoreSavedCampaignOptions(saveState);
            restoreSavedObjectSelectionAndGroups(saveState);
            pMapVisualObject.updateSelectionState();
            questEventDialogId = -1;
            completedMissionExitPending = 0;
            missionFailureDialogShown = 0;
            setGameSpeed(gameSpeed);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load saved game from " + mSaveFile.filename, e);
        }
    }

    /**
     * Native support extracted from the CurrentState/Character/GameOptions/View/SpellBook restore block in
     * CMainWindow::LoadSaveGame @0048C2E9.
     */
    private void restoreSavedCampaignOptions(ResInHeap saveState) {
        saveState.getInt("CurrentState", "InBattle", 1);
        StringBuilder playerName = new StringBuilder(0x20);
        saveState.getValueAsString("Character", "Name", "No name", playerName, 0x20);
        m_GameSession.m_PlayerName = truncateNativeCString(playerName.toString(), 0x1F);

        pMapVisualObject.wimpyMode = saveState.getInt("GameOptions", "Wimpy", pMapVisualObject.wimpyMode);
        pMapVisualObject.showHitPointBars = saveState.getInt("GameOptions", "ShowHP", pMapVisualObject.showHitPointBars);
        pMapVisualObject.showFlyingHitPointBars = saveState.getInt(
                "GameOptions",
                "FlyingHP",
                pMapVisualObject.showFlyingHitPointBars
        );
        pMapVisualObject.formationMode = saveState.getInt("GameOptions", "Formation", pMapVisualObject.formationMode);
        gameSpeed = saveState.getInt("GameOptions", "Speed", gameSpeed);
        Globals.gamePreferences.showTimeFlow = saveState.getInt("GameOptions", "ShowTimeFlow", Globals.gamePreferences.showTimeFlow);
        pMapVisualObject.view.x = saveState.getInt("View", "X", pMapVisualObject.view.x);
        pMapVisualObject.view.y = saveState.getInt("View", "Y", pMapVisualObject.view.y);
        saveState.getInt("SpellBook", "IsOpen", 0);
        pSpellPanelVisualObject.pressedSpellSlot = saveState.getInt(
                "SpellBook",
                "Pressed",
                pSpellPanelVisualObject.pressedSpellSlot
        );
    }

    /**
     * Native support extracted from the Objects/Selection and Objects/Group%d restore block in
     * CMainWindow::LoadSaveGame @0048C2E9.
     */
    private void restoreSavedObjectSelectionAndGroups(ResInHeap saveState) {
        List<Short> objectIds = new ArrayList<>();
        saveState.getShortArray("Objects", "Selection", objectIds);
        for (short objectId : objectIds) {
            CGameObject object = pMapVisualObject.getObjectByToken(objectId);
            if (object != null) {
                object.setSelected(true);
            }
        }

        for (int groupIndex = 0; groupIndex < 10; groupIndex++) {
            objectIds.clear();
            saveState.getShortArray("Objects", "Group%d".formatted(groupIndex), objectIds);
            for (short objectId : objectIds) {
                CGameObject object = pMapVisualObject.getObjectByToken(objectId);
                if (object != null) {
                    object.addToGroup(groupIndex);
                }
            }
        }
    }

    /**
     * Native support extracted from the fixed `m_PlayerName[0x1F] = '\\0'` terminator in
     * CMainWindow::LoadSaveGame @0048C2E9 and strncpy(..., 0x0C) calls in CMainWindow::onDialogClosed @004891D8.
     */
    private static String truncateNativeCString(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    /**
     * Native: CMainWindow::CreateServer @0048AB76.
     * Fully ported.
     */
    public int createServer(int startupMode) {
        HandleQuestEventDialogMessageHandler.clearQueuedRoleDialogIds();
        Globals.gameServer.initializeNewServerState();
        Globals.gameServer.markServerLifecycleAllocated();
        int result = Globals.gameServer.start(startupMode);
        serverBootstrapEnabled = result == 0 ? 1 : 0;
        return result;
    }

    /**
     * Native: CMainWindow::SetGameSpeed @0048BFAA.
     * Java port status: fully ported.
     */
    public boolean setGameSpeed(int speedIndex) {
        int previousGameSpeed = gameSpeed;
        if (speedIndex < 0) {
            speedIndex = 0;
        }
        if (speedIndex > 8) {
            speedIndex = 8;
        }
        gameSpeed = speedIndex;
        int ticksPerSecond = getTicksPerSecond();
        m_TickInterval = 1000 / ticksPerSecond;
        m_FrameCounter = 0;
        m_LagAccumulator = 0;
        int currentTime = (int) System.currentTimeMillis();
        m_LastRenderTime = currentTime;
        m_LastTickTime = currentTime;
        field90_0x42c = 0;
        return previousGameSpeed != gameSpeed;
    }

    /**
     * Java speed scaling helper.
     * not ported.
     */
    private int getTicksPerSecond() {
        //DO NOT CHANGE THIS BACK!
//        return switch (gameSpeed) {
//            case 0 -> 8;
//            case 1 -> 10;
//            case 2 -> 0x0C;
//            case 3 -> 0x0E;
//            case 5 -> 0x14;
//            case 6 -> 0x18;
//            case 7 -> 0x1C;
//            case 8 -> 0x20;
//            default -> 0x10;
//        };
        //intentionally, java-only - to be able to speed up the game beyond native speed.
        return 8 + (16 * gameSpeed);
    }

    /**
     * Native: CMainWindow::postCloseCommand @00492824.
     * Java port status: fully ported.
     */
    public void postCloseCommand() {
        postMessage(MessageCodes.WM_CLOSE, 0, 0);
    }

    /**
     * Native: CMainWindow::showDedicatedServerControlDialogCommand @004928CA.
     * Fully ported at the Java CDialog lifecycle boundary.
     */
    public void showDedicatedServerControlDialogCommand() {
        if (pDedicatedServerControlDialog == null) {
            pDedicatedServerControlDialog = new DedicatedServerControlDialog(null);
        }
        if (dedicatedServerControlDialogCreated == 0) {
            pDedicatedServerControlDialog.create(0x6B, null);
        } else {
            pDedicatedServerControlDialog.setActiveWindow();
        }
        dedicatedServerControlDialogCreated = 1;
    }

    /**
     * Native: CMainWindow::OnCreate @004826A0.
     * Fully ported for the recovered CMainWindow branch; stock CFrameWnd base creation remains a modeled MFC boundary.
     */
    @Override
    public int onCreate(Object createStruct) {
        int baseResult = super.onCreate(createStruct);
        if (baseResult == -1) {
            return -1;
        }
        if (Globals.isWindowed != 0) {
            CRect rect = new CRect();
            cStatusBar.create(this, STATUS_BAR_CREATE_STYLE, STATUS_BAR_CONTROL_ID);
            cListBox1.create(SERVER_LOG_LIST_CREATE_STYLE, rect, this, DEDICATED_SERVER_LOG_LIST_CONTROL_ID);
            cStatic1.create("", SERVER_PLAYER_DETAILS_CREATE_STYLE, rect, this, DEDICATED_SERVER_PLAYER_DETAILS_CONTROL_ID);
            cStatic2.create(MAP_NOT_LOADED_STATUS_TEXT, SERVER_STATUS_CREATE_STYLE, rect, this, DEDICATED_SERVER_STATUS_CONTROL_ID);
            gameEdit.create(SERVER_MESSAGE_EDIT_CREATE_STYLE, rect, this, DEDICATED_SERVER_MESSAGE_EDIT_CONTROL_ID);
            gameListBox.create(SERVER_PLAYER_LIST_CREATE_STYLE, rect, this, DEDICATED_SERVER_PLAYER_LIST_CONTROL_ID);
        }
        return 0;
    }

    /**
     * Native: CMainWindow::OnDestroy @00482526.
     * Fully ported for recovered application-owned shutdown. Java keeps stock MFC, CFile, and scalar-delete behavior at
     * managed lifecycle boundaries.
     */
    @Override
    public void onDestroy() {
        if (Globals.isWindowed != 0 && pDedicatedServerControlDialog != null) {
            pDedicatedServerControlDialog.destroyWindow();
            pDedicatedServerControlDialog = null;
        }
        saveFameHallScoresOnDestroy();
        if (DialogsMaskFlag.contains(dialogsMask, GAMEPLAY)) {
            pMapVisualObject.clearSessionForFirstPlayerLobbyReturn();
            cleanupGameplayResources();
            destroyServer();
        }
        UnitTypes.releaseUnitTypeRuntimeGlobals();
        SoundManager.releaseSounds();
        ApplicationPreferences.saveApplicationPreferences(this);
        destroyServer();
        destroyTopLevelVisualObjects();
        if (musicPlayer != null) {
            musicPlayer = null;
        }
        super.onDestroy();
    }

    /**
     * Native support extracted from the famehall.dat CFile::Open/CFameHall::Save branch in
     * CMainWindow::OnDestroy @00482526.
     */
    private void saveFameHallScoresOnDestroy() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (LEWriter writer = new LEWriter(output)) {
            m_FameHall.saveEntries(writer);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to encode famehall.dat", exception);
        }

        try {
            Files.write(
                    FAME_HALL_FILE,
                    output.toByteArray(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (IOException ignored) {
            // Native CFile::Open failure skips persistence.
        }
    }

    /**
     * Native: CMainWindow::OnSize @004926CA.
     * Java port status: fully ported for the recovered CMainWindow branch; stock CFrameWnd base behavior remains a
     * modeled MFC boundary.
     */
    @Override
    public void onSize(int nType, int cx, int cy) {
        super.onSize(nType, cx, cy);
        if (Globals.isWindowed != 0) {
            positionControls();
        }
    }

    /**
     * Native: CMainWindow::PositionControls @004827A5.
     * Java port status: fully ported.
     */
    public void positionControls() {
        CRect mainClientRect = new CRect();
        CRect gameListClientRect = new CRect();
        CRect chatListClientRect = new CRect();
        getClientRect(mainClientRect);
        cStatic2.setWindowPos(null, 0, 0, mainClientRect.right, 0x28, 0x200);
        gameListBox.setWindowPos(null, 0, 0x28, 0xC8, 0xF0, 0x200);
        gameListBox.getClientRect(gameListClientRect);
        gameListClientRect.bottom += 6;
        cStatic1.setWindowPos(null, 0xC8, 0x28, mainClientRect.right - 0xC8, gameListClientRect.bottom, 0x200);
        cListBox1.setWindowPos(
                null,
                0,
                gameListClientRect.bottom + 0x28,
                mainClientRect.right,
                (mainClientRect.bottom - gameListClientRect.bottom) - 0x50,
                0x200
        );
        cListBox1.getClientRect(chatListClientRect);
        chatListClientRect.bottom += 6;
        gameEdit.setWindowPos(
                null,
                0,
                gameListClientRect.bottom + 0x28 + chatListClientRect.bottom,
                mainClientRect.right,
                ((mainClientRect.bottom - gameListClientRect.bottom) - chatListClientRect.bottom) - 0x3C,
                0x200
        );
    }

    /**
     * Native: CMainWindow::SetMusicTracker @0045B5E0.
     * Full port.
     */
    public void setMusicTracker(Runnable tracker) {
        musicUpdater = tracker;
    }

    /**
     * Native owner: CMainWindow::musicPlayer field at `+0xC8`, read by SoundPreferencesDialogVisualObject::Initialize @0043CB0D.
     * not ported.
     */
    public MusicPlayer getMusicPlayer() {
        return musicPlayer;
    }

    /**
     * Native owner: `g_SoundPreferences` global passed into SoundPreferencesDialogVisualObject ctors.
     * not ported.
     */
    public SoundPreferences getSoundPreferences() {
        return Globals.soundPreferences;
    }

    /**
     * Native owner: CMainWindow::ClipCursorTo @0048AD61.
     * Fully ported at the Java input boundary. Native delegates to the OS cursor clip rectangle; Java keeps the
     * recovered clip bounds and clamps the in-memory mouse position into that rectangle.
     */
    public void clipCursorTo() {
        CRect targetRect = clipRect.width() > 0 && clipRect.height() > 0 ? clipRect : Globals.screenRect;
        CMousePointer mousePointer = Globals.mousePointer;
        int clampedX = Math.max(targetRect.left, Math.min(mousePointer.getX(), targetRect.right - 1));
        int clampedY = Math.max(targetRect.top, Math.min(mousePointer.getY(), targetRect.bottom - 1));
        mousePointer.setPosition(clampedX, clampedY);
    }

    /**
     * Native: CMainWindow::ClipCursorToMapViewport @0048ACBC.
     * Fully ported at the Java input boundary. Native computes a screen-space map viewport and forwards it into the OS
     * cursor clip API; Java keeps the recovered rectangle math and updates the in-memory clip state.
     */
    public void clipCursorToMapViewport() {
        MapVisualObject nativeMapVisualObject = pMapVisualObject;
        clipRect.set(screenRectForCursorClip(nativeMapVisualObject));
        if (nativeMapVisualObject.hasSpellPanelChild()) {
            clipRect.bottom = screenRectForCursorClip(pSpellPanelVisualObject).top;
        } else if (nativeMapVisualObject.hasSelectionPanelChild()) {
            clipRect.bottom = screenRectForCursorClip(pHeroInventoryControlVisualObject).top;
        }
        clipCursorTo();
    }

    /**
     * Native owner: CMainWindow::inputController_ field access from map UI helpers.
     * not ported.
     */
    public CVisualObject getInputController() {
        return inputController;
    }

    /**
     * Native owner: current hero-inventory money amount read from `MapVisualObject.currentPlayer +0x10`.
     * not ported.
     */
    public int getHeroInventoryMoneyAmount() {
        if (pMapVisualObject == null || pMapVisualObject.currentPlayer == null) {
            return 0;
        }
        return pMapVisualObject.currentPlayer.gold;
    }

    /**
     * Native owner: current hero-inventory money amount writes to `MapVisualObject.currentPlayer +0x10`.
     * not ported.
     */
    public void setHeroInventoryMoneyAmount(int amount) {
        if (pMapVisualObject == null || pMapVisualObject.currentPlayer == null) {
            return;
        }
        pMapVisualObject.currentPlayer.gold = amount;
    }

    /**
     * Native owner: CMainWindow field_0x120 drop-gold prompt pointer used by HeroInventoryControlVisualObject::OnLButtonDblClk @004A37F3.
     * not ported.
     */
    public CVisualObject getDropGoldPromptVisual() {
        return pDropGoldPromptVisualObject;
    }

    /**
     * Native owner: CMainWindow::field149_0x44C reset after showing the drop-gold prompt from HeroInventoryControlVisualObject::OnLButtonDblClk @004A37F3.
     * not ported.
     */
    public void clearHeroInventoryDropGoldPromptState() {
        field149_0x44C = 0;
    }

    /**
     * Native owner: CMainWindow::Map side panel field (CVisualObject_1_9).
     * not ported.
     */
    public CVisualObject getSpellPanelVisual() {
        return pSpellPanelVisualObject;
    }

    /**
     * Native owner: CMainWindow::Map side panel field (CVisualObject_1_5).
     * not ported.
     */
    public CVisualObject getOrderPanelVisual() {
        return pOrderToolbarVisualObject;
    }

    /**
     * Native owner: CMainWindow::pRightPanelContainerVisualObject field.
     * not ported.
     */
    public CVisualObject getRightPanelContainerVisual() {
        return pRightPanelContainerVisualObject;
    }

    /**
     * Native owner: CMainWindow::Map field read by ShopDialogVisualObject::ShowDialog @004B8B98.
     * not ported.
     */
    public MapVisualObject getMapVisual() {
        return pMapVisualObject;
    }

    /**
     * Native owner: CMainWindow::connectionScratchState.serverListSourceIsWebPage write in HatServerListDialogVisualObject::OnHeaderDialogAction @004492F0.
     * not ported.
     */
    public void setServerListSourceIsWebPage(boolean sourceIsWebPage) {
        connectionScratchState.serverListSourceIsWebPage = sourceIsWebPage ? 1 : 0;
    }

    /**
     * Native support extracted from CMainWindow::Hat.ip reads in HatServerBrowserDialogVisualObject::Initialize @0044A62F
     * and CMainWindow::WindowProc @004852D8.
     */
    public String getHatIp() {
        return Hat.ip;
    }

    /**
     * Native owner: CMainWindow::connectionScratchState.directAddress read by CMainWindow::connectToServerAddress @0048E90F.
     * Java support for recovered direct-address routes that populate `directAddress` before `CONNECT_TO_SERVER_ADDRESS`.
     */
    public void setDirectAddressConnectionString(String directAddress) {
        connectionScratchState.directAddress = directAddress == null ? "" : directAddress;
    }

    /**
     * Native owner: CMainWindow::connectionScratchState.pendingSessionConnectionString writes in HatServerBrowserDialogVisualObject::OnMessage @0044AB6C
     * and TcpIpSettingsHeaderDialogVisualObject::OnHeaderDialogAction @00449117.
     * not ported.
     */
    public void setPendingConnectionString(String pendingConnectionString) {
        connectionScratchState.pendingSessionConnectionString = pendingConnectionString == null ? "" : pendingConnectionString;
    }

    /**
     * Native: CMainWindow::RebuildDiplomacy @004922DA.
     * Java port status: fully ported.
     */
    public void rebuildDiplomacy() {
        m_Dilpomacy.m_pDiplomacyArray.clear();
        short[] diplomacyFlags = pMapVisualObject.currentPlayer.diplomacyFlags;
        for (CPlayer player : pMapVisualObject.clientPlayers) {
            if (player == null || (player.flags & 1) != 0) {
                continue;
            }

            int playerDiplomacy = diplomacyFlags[player.playerId] & 0xFFFF;
            m_Dilpomacy.m_pDiplomacyArray.add(new Diplomacy(
                    player.name.toString(),
                    (playerDiplomacy & CPlayer.ENEMY_MASK) != 0,
                    (playerDiplomacy & CPlayer.ALLIED_MASK) != 0,
                    (playerDiplomacy & CPlayer.DIPLOMACY_VISIBLE_MASK) != 0,
                    (playerDiplomacy & CPlayer.SILENT_DIPLOMACY_MASK) != 0
            ));
        }
    }

    /**
     * Native: CMainWindow::ShowDialog @0048B33B.
     * Fully ported. Native global surface lock/unlock and PresentFullScreenRenderRegion @00453788 are covered by
     * Java's renderer.
     */
    public void showDialog(HandlerVisualObject dialog) {
        if (dialog == pHeaderDialogVariantVisualObject) {
            dialogsMask = HEADER_DIALOG_VARIANT.includeTo(dialogsMask);
        } else {
            dialogsMask = MODAL_DIALOG.includeTo(dialogsMask);
        }
        inputController.addChild(dialog);
        dialog.initialize();
        dialog.showDialog();
        dialog.shadeScreen(3);
        dialog.draw();
        field149_0x44C = 0;
        CMousePointer.Cursor_Default.setToMousePointer();
        if (Globals.mousePointer.isSelecting()) {
            Globals.mousePointer.finishSelectionDrag();
            clipCursorTo();
        }
    }

    /**
     * Native support extracted from full-surface FillScreenRect(..., 0) transition helpers like
     * MainMenuVisualObject::ShowDialog @004A833D / CreditsDialogVisualObject::ShowDialog @0043BB76.
     */
    public static void clearTransitionSurface() {
        if (Globals.renderer == null || Globals.screen == null) {
            return;
        }
        Globals.renderer.clearSurface();
    }

    /**
     * Native: CMainWindow::showDialogAndRunModalMessageLoop @0049226D followed by
     * HandlerVisualObject::GetClosedReason @00437F80 in callers.
     * Fully ported for Java's GLFW message/presentation pump.
     */
    public MessageCodes showDialogAndAwaitResult(HandlerVisualObject dialog) {
        showDialog(dialog);
        while (dialog.activeFlag != 0) {
            if (!MessageSystem.pumpPostedMessage()) {
                pumpModalDialogFrame();
            }
        }
        return dialog.getClosedReason();
    }

    /**
     * Native support extracted from the GetMessage/DispatchMessage loop in
     * CMainWindow::showDialogAndRunModalMessageLoop @00492282-004922D2, with Java presentation polling added so GLFW
     * input reaches the modal dialog while the caller is blocked.
     */
    private void pumpModalDialogFrame() {
        renderFrameIfFocused();
        inputController.onMessage(STATIC_TEXT_CARET_BLINK_TICK, 0, 0);
        Globals.mousePointer.drawSelectionOverlay();
        Globals.mousePointer.drawTooltipOverlay();
        Globals.presentCurrentSurface.run();
        LockSupport.parkNanos(1_000_000L);
    }

    /**
     * Native owner: CMainWindow::m_FameHall field reads in FameHallDialogVisualObject::ShowDialog @0045BB82 and
     * FameHallDocumentDialogVisualObject::ShowDialog @004AAD03.
     * not ported.
     */
    public CFameHall getFameHall() {
        return m_FameHall;
    }

    /**
     * Native support extracted from ChatVisualObject::ShowDialog @0043B3D7 and HideDialog @0043B43F.
     */
    public void setChatInputCapture(boolean active) {
        chatOpen = active ? 1 : 0;
    }

    /**
     * Native: CMainWindow::focusChatInput @0048F36E.
     * Fully ported.
     */
    public void focusChatInput() {
        pMapVisualObject.areaEffectRefreshPending = 1;
        pMapVisualObject.addChild(pChatVisualObject);
        pChatVisualObject.refreshMapPanelLayout();
        pChatVisualObject.showDialog();
        inputController.draw();
        chatOpen = 1;
    }

    /**
     * Native owner: MapVisualObject::FUN_0041B311 ambient sound refresh path.
     * not ported.
     */
    public void updateAmbientAudio(MapVisualObject mapVisualObject) {
    }

    /**
     * Native support extracted from MapVisualObject::UpdateSelectionState @004167C2.
     */
    public boolean isSpellUiActive() {
        return DialogsMaskFlag.isExactly(dialogsMask, GAMEPLAY) || DialogsMaskFlag.contains(dialogsMask, SHOP_DIALOG);
    }

    /**
     * not ported. Java layout reset for non-gameplay right-panel/dialog compositions that reuse the shared panels.
     */
    public void applyDefaultRightPanelLayout() {
        int selectionTop = RightPanelLayout.MINIMAP_HEIGHT + RightPanelLayout.ORDER_TOOLBAR_HEIGHT;
        int selectionBottom = selectionTop + RightPanelLayout.PORTRAIT_PANEL_HEIGHT;
        pMinimapVisualObject.setBounds(0, 0, RightPanelLayout.PANEL_WIDTH, RightPanelLayout.MINIMAP_HEIGHT);
        pOrderToolbarVisualObject.setBounds(0, RightPanelLayout.MINIMAP_HEIGHT, RightPanelLayout.PANEL_WIDTH, selectionTop);
        pSelectionInfoPanelVisualObject.setBounds(0, selectionTop, RightPanelLayout.PANEL_WIDTH, selectionBottom);
        pSideStatusVisualObject.setBounds(0, selectionBottom, RightPanelLayout.PANEL_WIDTH, Globals.screenRect.bottom);
    }

    /**
     * not ported. Java scalable layout used only for the active gameplay MapVisualObject/right-panel composition.
     */
    public void applyGameplayRightPanelLayout() {
        RightPanelLayout rightPanelLayout = RightPanelLayout.forScreenHeight(Globals.screenRect.bottom);
        pMinimapVisualObject.setBounds(0, rightPanelLayout.minimapTop, RightPanelLayout.PANEL_WIDTH, rightPanelLayout.minimapBottom);
        pOrderToolbarVisualObject.setBounds(0, rightPanelLayout.orderToolbarTop, RightPanelLayout.PANEL_WIDTH, rightPanelLayout.orderToolbarBottom);
        pSelectionInfoPanelVisualObject.setBounds(0, rightPanelLayout.portraitTop, RightPanelLayout.PANEL_WIDTH, rightPanelLayout.portraitBottom);
        pSideStatusVisualObject.setBounds(0, rightPanelLayout.sideStatusTop, RightPanelLayout.PANEL_WIDTH, rightPanelLayout.sideStatusBottom);
    }

    /**
     * Native: CMainWindow::InitializeTopLevelVisualObjects @00483129.
     * Fully ported. Allocates the startup-owned top-level visual objects, resets chat input state, and initializes the
     * recovered shop/inn dialog controls in native order.
     */
    public void initializeTopLevelVisualObjects() {
        int screenWidth = Globals.screenRect.right;
        int screenHeight = Globals.screenRect.bottom;
        CRect mainWindowRect = Globals.mainWindowRect;
        int mainLeft = mainWindowRect.left;
        int mainTop = mainWindowRect.top;
        int mainRight = mainWindowRect.right;
        int mainBottom = mainWindowRect.bottom;
        int mapRight = screenWidth - RightPanelLayout.PANEL_WIDTH;
        int selectionTop = RightPanelLayout.MINIMAP_HEIGHT + RightPanelLayout.ORDER_TOOLBAR_HEIGHT;
        int selectionBottom = selectionTop + RightPanelLayout.PORTRAIT_PANEL_HEIGHT;
        inputController = new CVisualObject(0, 0, 0, screenWidth, screenHeight, null);
        pMapVisualObject = new MapVisualObject(0, 0, mapRight, screenHeight, null, null, null);
        pRightPanelContainerVisualObject = new RightPanelContainerVisualObject(4, mapRight, 0, screenWidth, screenHeight);
        pMinimapVisualObject = new MinimapVisualObject(5, 0, 0, RightPanelLayout.PANEL_WIDTH, RightPanelLayout.MINIMAP_HEIGHT);
        pOrderToolbarVisualObject = new OrderToolbarVisualObject(6, 0, RightPanelLayout.MINIMAP_HEIGHT, RightPanelLayout.PANEL_WIDTH, selectionTop);
        pSelectionInfoPanelVisualObject = new SelectionInfoPanelVisualObject(7, 0, selectionTop, RightPanelLayout.PANEL_WIDTH, selectionBottom);
        pSideStatusVisualObject = new SideStatusVisualObject(8, 0, selectionBottom, RightPanelLayout.PANEL_WIDTH, screenHeight);
        pHeroInventoryControlVisualObject = new HeroInventoryControlVisualObject(2, 0, screenHeight - 0x5A, mapRight, screenHeight);
        pSpellPanelVisualObject = new SpellPanelVisualObject(3, 0, screenHeight - 0x5A, mapRight, screenHeight);
        pSpellPanelVisualObject.onMessage(MessageCodes.SPELL_PANEL_CLEAR_PRESSED_SLOT, 0, 0);
        pShopDialogVisualObject = new ShopDialogVisualObject(1000, mainLeft, mainTop, mainRight, mainBottom, null);
        pDruidShopDialogVisualObject = new DruidShopDialogVisualObject(1000, mainLeft, mainTop, mainRight, mainBottom, null);
        pKaargShopDialogVisualObject = new KaargShopDialogVisualObject(1000, mainLeft, mainTop, mainRight, mainBottom, null);
        pGlobalMapDialogVisualObject = new GlobalMapDialogVisualObject(0x41A, mainLeft, mainTop, mainRight, mainBottom, null);
        pMainMenuVisualObject = new MainMenuVisualObject(0x442, mainLeft, mainTop, mainRight, mainBottom);
        pBasicInnDialogVisualObject = new BasicInnDialogVisualObject(0x44C, mainLeft, mainTop, mainRight, mainBottom);
        pDruidInnDialogVisualObject = new DruidInnDialogVisualObject(0x44C, mainLeft, mainTop, mainRight, mainBottom);
        pKaargInnDialogVisualObject = new KaargInnDialogVisualObject(0x44C, mainLeft, mainTop, mainRight, mainBottom);
        pBasicTownDialogVisualObject = new BasicTownDialogVisualObject(0x3FC, mainLeft, mainTop, mainRight, mainBottom);
        pDruidTownDialogVisualObject = new DruidTownDialogVisualObject(0x3FC, mainLeft, mainTop, mainRight, mainBottom);
        pKaargTownDialogVisualObject = new KaargTownDialogVisualObject(0x3FC, mainLeft, mainTop, mainRight, mainBottom);
        pCreditsDialogVisualObject = new CreditsDialogVisualObject(0x456, mainLeft, mainTop, mainRight, mainBottom);
        pStartupLogoDialogVisualObject = new StartupLogoDialogVisualObject(0x4B0, mainLeft, mainTop, mainRight, mainBottom);
        pCharacterGeneratorDialogVisualObject = new CharacterGeneratorDialogVisualObject(0x456, mainLeft, mainTop, mainRight, mainBottom);
        pCharacterLoaderDialogVisualObject = new CharacterLoaderDialogVisualObject(0x460, mainLeft, mainTop, mainRight, mainBottom);
        pStartGameSetupDialogVisualObject = new StartGameSetupDialogVisualObject(0x466, mainLeft, mainTop, mainRight, mainBottom);
        pFameHallDialogVisualObject = new FameHallDialogVisualObject(0x4B0, mainLeft, mainTop, mainRight, mainBottom);
        pFameHallDocumentDialogVisualObject = new FameHallDocumentDialogVisualObject(0x4BA, mainLeft, mainTop, mainRight, mainBottom);
        pChatVisualObject = new ChatVisualObject(0x4B0, 0, 0, mapRight, 0x1E);
        pChatVisualObject.attachGameListControl(pMapVisualObject.gameListControl);
        chatOpen = 0;
        pDropGoldPromptVisualObject = new DropGoldPromptVisualObject(1000000000, 100, screenHeight - 200);
        pShopDialogVisualObject.initialize();
        pDruidShopDialogVisualObject.initialize();
        pKaargShopDialogVisualObject.initialize();
        pBasicInnDialogVisualObject.initialize();
        pDruidInnDialogVisualObject.initialize();
        pKaargInnDialogVisualObject.initialize();
    }

    /**
     * Java support boundary for headless dedicated launchers that need the native main-window server/map state without
     * constructing the full visual dialog tree or runtime graphics/audio backends.
     * not ported.
     */
    public void initializeHeadlessDedicatedVisualState() {
        int screenWidth = Globals.screenRect.right;
        int screenHeight = Globals.screenRect.bottom;
        int mapRight = screenWidth - RightPanelLayout.PANEL_WIDTH;
        inputController = new CVisualObject(0, 0, 0, screenWidth, screenHeight, null);
        pMapVisualObject = new MapVisualObject(0, 0, mapRight, screenHeight, null, null, null);
        field149_0x44C = 1;
        dialogsMask = 0;
    }

    /**
     * Native: CMainWindow::DestroyTopLevelVisualObjects @00484165.
     * Fully ported for Java lifecycle: native clears visual-child links and scalar-deletes top-level visuals; Java
     * detaches the same graph and drops main-window references for GC.
     */
    public void destroyTopLevelVisualObjects() {
        pRightPanelContainerVisualObject.clearChildren();
        inputController.clearChildren();
        pMapVisualObject.clearChildren();

        pBasicInnDialogVisualObject.removeChild(pSelectionInfoPanelVisualObject);
        pShopDialogVisualObject.removeChild(pSelectionInfoPanelVisualObject);
        pDruidInnDialogVisualObject.removeChild(pSelectionInfoPanelVisualObject);
        pDruidShopDialogVisualObject.removeChild(pSelectionInfoPanelVisualObject);
        pKaargInnDialogVisualObject.removeChild(pSelectionInfoPanelVisualObject);
        pKaargShopDialogVisualObject.removeChild(pSelectionInfoPanelVisualObject);
        pCharacterGeneratorDialogVisualObject.removeChild(pSelectionInfoPanelVisualObject);

        inputController = null;
        pRightPanelContainerVisualObject = null;
        pSelectionInfoPanelVisualObject = null;
        pSideStatusVisualObject = null;
        pOrderToolbarVisualObject = null;
        pMinimapVisualObject = null;
        pMapVisualObject = null;
        pHeroInventoryControlVisualObject = null;
        pSpellPanelVisualObject = null;
        pShopDialogVisualObject = null;
        pDruidShopDialogVisualObject = null;
        pKaargShopDialogVisualObject = null;
        pGlobalMapDialogVisualObject = null;
        pMainMenuVisualObject = null;
        pBasicInnDialogVisualObject = null;
        pDruidInnDialogVisualObject = null;
        pKaargInnDialogVisualObject = null;
        pCharacterGeneratorDialogVisualObject = null;
        pCharacterLoaderDialogVisualObject = null;
        pStartGameSetupDialogVisualObject = null;
        pDropGoldPromptVisualObject = null;
        pBasicTownDialogVisualObject = null;
        pDruidTownDialogVisualObject = null;
        pKaargTownDialogVisualObject = null;
        pCreditsDialogVisualObject = null;
        pStartupLogoDialogVisualObject = null;
        pChatVisualObject = null;
        pFameHallDialogVisualObject = null;
        pFameHallDocumentDialogVisualObject = null;
    }

    /**
     * Native: CMainWindow::restoreGameplayVisualTree @00483FE2.
     * Fully ported. Rebuilds the gameplay root visuals and resets the map message list exactly through the recovered
     * pointer dereference sequence.
     */
    public void restoreGameplayVisualTree() {
        pRightPanelContainerVisualObject.clearChildren();
        pRightPanelContainerVisualObject.addChild(pMinimapVisualObject);
        pRightPanelContainerVisualObject.addChild(pOrderToolbarVisualObject);
        pRightPanelContainerVisualObject.addChild(pSelectionInfoPanelVisualObject);
        pRightPanelContainerVisualObject.addChild(pSideStatusVisualObject);
        inputController.clearChildren();
        pMapVisualObject.gameListControl.deinit();
        inputController.addChild(pMapVisualObject);
        inputController.addChild(pRightPanelContainerVisualObject);
        dialogsMask = GAMEPLAY.mask;
    }

    /**
     * Native owner: selection UI refresh fan-out around 004167C2.
     * not ported.
     */
    public void onSelectionUpdated(MapVisualObject state) {
    }

    /**
     * Native support extracted from CWnd::PostMessage @0041E3F0 and CMainWindow::WindowProc @004852D8.
     * Java preserves the native asynchronous boundary by enqueueing through the global message system.
     */
    @Override
    public void postMessage(MessageCodes msg, Object wParam, Object lParam) {
        MessageSystem.post(msg, wParam, lParam);
    }

    /**
     * Native: CMainWindow::WindowProc @004852D8.
     * Java port status: fully ported for direct top-level dispatch; native callees stay on their native owners or
     * explicit Java support boundaries.
     */
    public int windowProc(MessageCodes msg, Object wParam, Object lParam) {
        MessageHandler<CMainWindow> handler = handlers.get(msg);
        if (handler != null) {
            return handler.handle(this, wParam, lParam);
        }

        // Grouped and default cases keep the message code as part of the dispatch.
        return switch (msg) {
            case WM_CHAR -> {
                onChar(((Number) wParam).intValue());
                yield 0;
            }
            case WM_KILLFOCUS -> {
                onKillFocus();
                yield 0;
            }
            case WM_LBUTTONDBLCLK, WM_RBUTTONDOWN, WM_RBUTTONUP, WM_RBUTTONDBLCLK ->
                    MouseButtonMessageHandler.handleMouseButtonMessage(this, msg, wParam, lParam);
            default -> dispatchDefaultWindowProcMessage(msg, wParam, lParam);
        };
    }

    /**
     * Native support extracted from the input-controller fallback at CMainWindow::WindowProc @0048865E.
     */
    private int dispatchDefaultWindowProcMessage(MessageCodes msg, Object wParam, Object lParam) {
        if (inputController != null && inputController.onMessage(msg, wParam, lParam) != 0) {
            return defWindowProc(msg, wParam, lParam);
        }
        return 0;
    }

    /**
     * Native support extracted from CMainWindow::ClipCursorToMapViewport @0048ACBC.
     */
    private CRect screenRectForCursorClip(CVisualObject visualObject) {
        CRect screenRect = new CRect();
        visualObject.clientToScreen(screenRect, visualObject.getRect());
        return screenRect;
    }

    /**
     * Java message-target bridge for GLFW input adapters.
     * Native support delegates into CMainWindow::WindowProc @004852D8.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        return this.windowProc(msg, wParam, lParam);
    }
}
