package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.CString;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CPlayer;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.control.CGameListControl;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.world.scenario.ScenarioFileHeader;
import ua.millfreedom.rom2.model.world.scenario.ScenarioSectionHeader;
import ua.millfreedom.rom2.model.world.scenario.ScenarioSectionId;

import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static ua.millfreedom.rom2.model.enums.MessageCodes.MULTIPLAYER_LOBBY_REFRESH_PLAYER_LIST;
import static ua.millfreedom.rom2.model.enums.MessageCodes.DIALOG_OK;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RETURN_TO_GAME;
import static ua.millfreedom.rom2.model.window.CMainWindow.SESSION_MODE_MULTIPLAYER_CLIENT;
import static ua.millfreedom.rom2.model.window.windowproc.handlers.CMainWindowWindowProcSupport.readMessageInt;
import static ua.millfreedom.rom2.text.DialogsText.AVAILABLE_MAPS_118;
import static ua.millfreedom.rom2.text.DialogsText.CANCEL_1;
import static ua.millfreedom.rom2.text.DialogsText.CHAT_140;
import static ua.millfreedom.rom2.text.DialogsText.MAP_NAME_SIZE_LEVEL_139;
import static ua.millfreedom.rom2.text.DialogsText.MESSAGES_SENT_BY_OTHER_PLAYERS_132;
import static ua.millfreedom.rom2.text.DialogsText.OK_0;
import static ua.millfreedom.rom2.text.DialogsText.PLAYER_LIST_137;
import static ua.millfreedom.rom2.text.DialogsText.PLAYERS_141;
import static ua.millfreedom.rom2.text.DialogsText.SELECT_MAP_TO_PLAY_138;
import static ua.millfreedom.rom2.text.DialogsText.TYPE_YOUR_MESSAGE_TO_OTHER_PLAYERS_HERE_133;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.TextTableId.DIALOGS;

/**
 * Native class: MultiplayerMapSelectionDialogVisualObject.
 * Purpose: multiplayer map-selection lobby dialog with available-map rows, chat, player list, and background worker state.
 */
public class MultiplayerMapSelectionDialogVisualObject extends CenteredDialogVisualObject {
    private static final int MAP_LIST_ID = 1;
    private static final int MAP_SCROLLBAR_ID = 10;
    private static final int CHAT_LIST_ID = 3;
    private static final int CHAT_INPUT_ID = 4;
    private static final int PLAYERS_LIST_ID = 5;
    private static final int CHAT_SCROLLBAR_ID = 0x0B;
    private static final int PLAYERS_SCROLLBAR_ID = 0x0C;
    private static final int OK_BUTTON_ID = 0x14;
    private static final int CANCEL_BUTTON_ID = 0x15;
    private static final int MAP_COLUMNS_HEADER_ID = 0x1A;
    private static final int CHAT_HEADER_ID = 0x1B;
    private static final int PLAYERS_HEADER_ID = 0x1C;
    private static final int MAP_FILE_MINIMUM_SECTION_COUNT = 2;
    private static final int MAP_INFO_GROUP_COUNT_VERSION = 0x47D;
    private static final int MAP_INFO_DESCRIPTOR_COUNT_VERSION = 0x4CD;
    private static final int MAP_INFO_MUSIC_COUNT_VERSION = 0x513;
    private static final int MAP_INFO_EXTENDED_FIELDS_VERSION = 0x487;
    private static final int MAP_INFO_FIXED_DWORD_COUNT_AFTER_SIZE = 10;
    private static final int MAP_TITLE_BYTES = 0x40;
    private static final int MAP_DESCRIPTION_BYTES = 0x200;
    private static final Path MAP_SEARCH_DIRECTORY = Utils.getCurDirectory();
    private static final String MAP_EXTENSION = ".alm";

    public static final int NATIVE_SIZE = 0xFC; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    //0x68
    public CString selectedMapNameOut;
    //0x6c
    public final List<MultiplayerMapSelectionEntry> availableMaps = new ArrayList<>();
    //0x80
    public Object workerThread;
    //0xe8
    public Object workerStopEvent;
    //0xec
    public int selectedMapIndex;
    //0xf0
    public CGameListControl lobbyChatLog;
    //0xf4
    public MapVisualObject mapVisual;
    //0xf8
    public int hostSessionModeAtInitialize;

    /**
     * Native: MultiplayerMapSelectionDialogVisualObject::MultiplayerMapSelectionDialogVisualObject @00449908.
     * Fully ported. Java represents the embedded native `CWinThread` callback state with the worker fields that
     * Initialize @0044B0FB drives through the synchronous worker boundary.
     */
    public MultiplayerMapSelectionDialogVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            CString selectedMapNameOut
    ) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        this.selectedMapNameOut = selectedMapNameOut;
        initializeMapSelectionWorkerThreadState();
    }

    /**
     * vtbl +0x78: MultiplayerMapSelectionDialogVisualObject::Initialize @0044B0FB.
     * Fully ported. Java runs the recovered map-list worker body synchronously at the MFC `CWinThread` boundary.
     */
    @Override
    public void initialize() {
        CBitmapFont dialogFont = Globals.fonts.font1;
        hostSessionModeAtInitialize =
                Globals.mainWindow.sessionMode != SESSION_MODE_MULTIPLAYER_CLIENT ? 1 : 0;

        addChild(new DialogWindowVisualObject(
                0x19,
                0x28,
                0x16,
                cRect.width() - 0x28,
                0x38,
                get(DIALOGS, SELECT_MAP_TO_PLAY_138),
                dialogFont,
                Palettes.grayDim,
                0x2
        ));
        addChild(new DialogWindowVisualObject(
                MAP_COLUMNS_HEADER_ID,
                0x28,
                0x2C,
                cRect.width() - 0x28,
                0x44,
                get(DIALOGS, MAP_NAME_SIZE_LEVEL_139),
                dialogFont,
                Palettes.grayDim,
                0
        ));

        AvailableMapsTextListVisualObject availableMapsList = new AvailableMapsTextListVisualObject(
                MAP_LIST_ID,
                0x28,
                0x44,
                cRect.width() - 0x40,
                cRect.height() / 2,
                dialogFont,
                Palettes.grayDim,
                Palettes.gray,
                MAP_SCROLLBAR_ID,
                get(DIALOGS, AVAILABLE_MAPS_118),
                () -> selectedMapIndex
        );
        addChild(availableMapsList);
        availableMapsList.gameDialogControls = getChildById(MAP_COLUMNS_HEADER_ID);
        CRect availableMapsRect = availableMapsList.getRect();
        addChild(new PostSetupVisualObject(
                MAP_SCROLLBAR_ID,
                availableMapsRect.right,
                availableMapsRect.top,
                availableMapsRect.right + 0x18,
                availableMapsRect.bottom,
                null
        ));

        CRect chatRect = new CRect(availableMapsRect.left, 0x11C, 0x15E, 0x180);
        addChild(new DialogWindowVisualObject(
                CHAT_HEADER_ID,
                chatRect.left,
                chatRect.top - 0x18,
                chatRect.right,
                chatRect.top - 6,
                get(DIALOGS, CHAT_140),
                dialogFont,
                Palettes.grayDim,
                0
        ));

        MultiplayerMessagesTextListVisualObject chatList = new MultiplayerMessagesTextListVisualObject(
                CHAT_LIST_ID,
                chatRect,
                dialogFont,
                Palettes.grayDim,
                Palettes.gray,
                CHAT_SCROLLBAR_ID,
                get(DIALOGS, MESSAGES_SENT_BY_OTHER_PLAYERS_132)
        );
        addChild(chatList);
        chatList.gameDialogControls = getChildById(CHAT_HEADER_ID);
        addChild(new PostSetupVisualObject(
                CHAT_SCROLLBAR_ID,
                chatRect.right,
                chatRect.top,
                chatRect.right + 0x18,
                chatRect.bottom,
                null
        ));

        int buttonCenterY = cRect.height() - 0x30;
        int buttonCenterX = cRect.width() / 2;
        CRect okButtonRect = new CRect(
                buttonCenterX - 0x78,
                buttonCenterY - 0x0C,
                buttonCenterX + 0x18,
                buttonCenterY + 0x0C
        );
        CommandButtonVisualObject okButton = new CommandButtonVisualObject(
                OK_BUTTON_ID,
                okButtonRect,
                get(DIALOGS, OK_0),
                dialogFont,
                Palettes.grayDim,
                DIALOG_OK,
                0,
                get(DIALOGS, OK_0)
        );
        addChild(okButton);
        if (hostSessionModeAtInitialize == 0) {
            okButton.setStateFlag(1, 0);
        }

        CRect chatInputRect = new CRect(chatRect.left, chatRect.bottom + 0x0C, chatRect.right, chatRect.bottom + 0x24);
        StaticTextVariantVisualObject chatInput = new StaticTextVariantVisualObject(
                CHAT_INPUT_ID,
                chatInputRect,
                dialogFont,
                Palettes.grayDim,
                get(DIALOGS, TYPE_YOUR_MESSAGE_TO_OTHER_PLAYERS_HERE_133)
        );
        addChild(chatInput);
        chatInput.upNeighbor = chatList;
        chatList.downNeighbor = chatInput;

        CRect playerListRect = new CRect(0x181, 0x11C, 0x20C, 0x1A4);
        addChild(new DialogWindowVisualObject(
                PLAYERS_HEADER_ID,
                playerListRect.left,
                playerListRect.top - 0x18,
                playerListRect.right,
                playerListRect.top - 6,
                get(DIALOGS, PLAYERS_141),
                dialogFont,
                Palettes.grayDim,
                0
        ));

        MultiplayerPlayersTextListVisualObject playersList = new MultiplayerPlayersTextListVisualObject(
                PLAYERS_LIST_ID,
                playerListRect,
                dialogFont,
                Palettes.grayDim,
                Palettes.grayDim,
                PLAYERS_SCROLLBAR_ID,
                get(DIALOGS, PLAYER_LIST_137)
        );
        addChild(playersList);
        playersList.gameDialogControls = getChildById(PLAYERS_HEADER_ID);
        Globals.mainWindow.postMessage(MULTIPLAYER_LOBBY_REFRESH_PLAYER_LIST, 0, 0);
        addChild(new PostSetupVisualObject(
                PLAYERS_SCROLLBAR_ID,
                playerListRect.right,
                playerListRect.top,
                playerListRect.right + 0x18,
                playerListRect.bottom,
                null
        ));

        CRect cancelButtonRect = new CRect(
                okButtonRect.left + 0x90,
                okButtonRect.top,
                okButtonRect.right + 0x90,
                okButtonRect.bottom
        );
        CommandButtonVisualObject cancelButton = new CommandButtonVisualObject(
                CANCEL_BUTTON_ID,
                cancelButtonRect,
                get(DIALOGS, CANCEL_1),
                dialogFont,
                Palettes.grayDim,
                RETURN_TO_GAME,
                0,
                get(DIALOGS, CANCEL_1)
        );
        addChild(cancelButton);
        cancelButton.leftNeighbor = okButton;
        okButton.rightNeighbor = cancelButton;

        workerStopEvent = new Object();
        mapVisual = Globals.mainWindow.getMapVisual();
        lobbyChatLog = resolveLobbyChatLog();
        selectedMapIndex = 0;
        runMapListWorker();
    }

    /**
     * vtbl +0x48: MultiplayerMapSelectionDialogVisualObject::OnMessage @0044BBE8.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        switch (msg) {
            case TEXT_LIST_SELECTION_DBLCLK:
                if (readMessageInt(wParam) != MAP_LIST_ID) {
                    return 0;
                }
                return handleConfirmSelection();
            case DIALOG_OK:
                return handleConfirmSelection();
            case RETURN_TO_GAME:
                stopMapListWorker();
                super.onMessage(RETURN_TO_GAME, 0, 0);
                return 1;
            case VALIDATE_MULTIPLAYER_MAP_SELECTION:
                return handleSessionValidationMessage();
            case MULTIPLAYER_LOBBY_APPEND_CHAT_MESSAGE:
                appendLatestLobbyMessage();
                return 1;
            case MULTIPLAYER_LOBBY_REFRESH_PLAYER_LIST:
                rebuildPlayerList();
                return 1;
            case MULTIPLAYER_MAP_SELECTION_APPEND_AVAILABLE_MAP:
                appendAvailableMap(readPendingAvailableMapEntry(wParam, lParam));
                return 1;
            case TEXT_LIST_SELECTION_CHANGED:
            case TEXT_LIST_SELECTION_COMMITTED:
                if (readMessageInt(wParam) != MAP_LIST_ID) {
                    return 0;
                }
                handleMapSelectionStateChange(readMessageInt(lParam));
                return 1;
            case MULTIPLAYER_MAP_SELECTION_SELECT_MAP_BY_NAME:
                if (hostSessionModeAtInitialize == 0) {
                    selectMapByName(readPendingSelectedMapName(wParam), readMessageInt(lParam));
                    getChildById(MAP_LIST_ID).draw();
                }
                return 1;
            default:
                return super.onMessage(msg, wParam, lParam);
        }
    }

    /**
     * Native ctor helper pair `CArray` ctor @00449955 and FUN_005B7AB3(0x449499,this) @005B7AB3.
     * Fully ported. Java initializes the recovered list and worker boundaries without modeling native thread storage.
     */
    private void initializeMapSelectionWorkerThreadState() {
        availableMaps.clear();
        workerThread = null;
        workerStopEvent = null;
        selectedMapIndex = 0;
        lobbyChatLog = null;
        mapVisual = null;
        hostSessionModeAtInitialize = 0;
    }

    /**
     * Native helper block inside MultiplayerMapSelectionDialogVisualObject::OnMessage @0044BBE8.
     * Fully ported.
     */
    private int handleConfirmSelection() {
        CVisualObject okButton = getChildById(OK_BUTTON_ID);
        if (okButton.checkStateFlag(1) == 0) {
            return 1;
        }

        stopMapListWorker();
        super.onMessage(DIALOG_OK, 0, 0);
        applySelectedMapOutput();
        return 1;
    }

    /**
     * Native helper branch inside MultiplayerMapSelectionDialogVisualObject::OnMessage @0044BBE8 for message 0x45E.
     * Fully ported.
     */
    private int handleSessionValidationMessage() {
        if (Globals.mainWindow.sessionMode == SESSION_MODE_MULTIPLAYER_CLIENT) {
            stopMapListWorker();
            if (hostSessionModeAtInitialize == 0) {
                if (isSelectedMapCompatibleWithSession()) {
                    super.onMessage(DIALOG_OK, 0, 0);
                } else {
                    super.onMessage(RETURN_TO_GAME, 0, 0);
                }
            } else {
                super.onMessage(DIALOG_OK, 0, 0);
            }
        }
        return 1;
    }

    /**
     * Native helper branch inside MultiplayerMapSelectionDialogVisualObject::OnMessage @0044BBE8 for messages
     * `TEXT_LIST_SELECTION_CHANGED` / `TEXT_LIST_SELECTION_COMMITTED`.
     * Fully ported.
     */
    private void handleMapSelectionStateChange(int selectedRow) {
        if (selectedRow < 0 || hostSessionModeAtInitialize == 0) {
            return;
        }

        selectedMapIndex = selectedRow;
        CVisualObject mapList = getChildById(MAP_LIST_ID);
        mapList.draw();
        postSelectedMapAnnouncement();
        refreshOkButtonState();
    }

    /**
     * Native: MultiplayerMapSelectionDialogVisualObject::appendAvailableMap @0044BAB0.
     * Fully ported. The native first-selection helper at @0044BAA3 is a no-op.
     */
    private void appendAvailableMap(MultiplayerMapSelectionEntry entry) {
        availableMaps.add(entry);
        AvailableMapsTextListVisualObject availableMapsList =
                (AvailableMapsTextListVisualObject) getChildById(MAP_LIST_ID);

        int selectedRow = availableMapsList.selectedRow;
        availableMapsList.rows.add(entry.formatDisplayRow());
        if (selectedRow < 0) {
            selectedRow++;
            availableMapsList.setSelectedRow(0);
        }
        PostSetupVisualObject mapScrollbar = (PostSetupVisualObject) getChildById(MAP_SCROLLBAR_ID);
        mapScrollbar.setCurrentValueAndMaxValue(new Point(selectedRow, availableMaps.size()));
        availableMapsList.draw();
        if (selectedRow == 0) {
            Globals.mainWindow.postMessage(MessageCodes.TEXT_LIST_SELECTION_CHANGED, MAP_LIST_ID, 0);
        }
    }

    /**
     * Native support extracted from MultiplayerMapSelectionDialogVisualObject::mapListWorkerThreadProc @00449499 and
     * MultiplayerMapSelectionDialogVisualObject::enumerateAvailableMapsWorker @004494A8.
     * Fully ported. Java runs the native worker body synchronously after Initialize creates the stop event and list.
     */
    private void runMapListWorker() {
        workerThread = Thread.currentThread();
        try (DirectoryStream<Path> mapFiles = Files.newDirectoryStream(MAP_SEARCH_DIRECTORY)) {
            for (Path mapFile : mapFiles) {
                if (workerStopEvent == null) {
                    break;
                }
                String fileName = mapFile.getFileName().toString();
                if (!hasMapExtension(fileName) || Files.isDirectory(mapFile)) {
                    continue;
                }
                MultiplayerMapSelectionEntry entry = readAvailableMapEntry(mapFile);
                if (entry != null) {
                    appendAvailableMap(entry);
                }
            }
        } catch (IOException ignored) {
            // Native FindFirstFileA failure leaves the available-map list empty.
        } finally {
            workerThread = null;
        }
    }

    /**
     * Native support extracted from the FindFirstFileA("*.alm") mask in
     * MultiplayerMapSelectionDialogVisualObject::enumerateAvailableMapsWorker @004494A8.
     */
    private static boolean hasMapExtension(String fileName) {
        return fileName.length() >= MAP_EXTENSION.length()
                && fileName.regionMatches(
                true,
                fileName.length() - MAP_EXTENSION.length(),
                MAP_EXTENSION,
                0,
                MAP_EXTENSION.length()
        );
    }

    /**
     * Native support extracted from MapDescriptor::readAvailableMapEntry @0044D5AD and the worker body @004494A8.
     * Fully ported. Reads only ALM metadata section 0, matching the native lobby-list fast path.
     */
    private static MultiplayerMapSelectionEntry readAvailableMapEntry(Path mapFile) throws IOException {
        ByteBuffer file = ByteBuffer.wrap(Files.readAllBytes(mapFile)).order(ByteOrder.LITTLE_ENDIAN);
        ScenarioFileHeader fileHeader = ScenarioFileHeader.read(file);
        if (!fileHeader.isM7R() || fileHeader.sectionCount < MAP_FILE_MINIMUM_SECTION_COUNT) {
            return null;
        }

        while (file.hasRemaining()) {
            ScenarioSectionHeader sectionHeader = ScenarioSectionHeader.read(file, fileHeader);
            int sectionStart = file.position();
            int nextSection = sectionStart + sectionHeader.dataSize;
            if (sectionHeader.id == ScenarioSectionId.INFO) {
                ByteBuffer section = file.slice().order(ByteOrder.LITTLE_ENDIAN);
                section.limit(sectionHeader.dataSize);
                return readAvailableMapInfoSection(mapFile.getFileName().toString(), fileHeader.version, section);
            }
            file.position(nextSection);
        }
        return null;
    }

    /**
     * Native support extracted from MapDescriptor::readAvailableMapEntry @0044D5AD.
     * Fully ported.
     */
    private static MultiplayerMapSelectionEntry readAvailableMapInfoSection(
            String fileName,
            int version,
            ByteBuffer section
    ) {
        MultiplayerMapSelectionEntry entry = new MultiplayerMapSelectionEntry();
        entry.internalMapId = fileName;
        entry.rawMapWidth = section.getInt();
        entry.rawMapHeight = section.getInt();
        skipScenarioInfoBytes(section, MAP_INFO_FIXED_DWORD_COUNT_AFTER_SIZE * Integer.BYTES);
        if (version > MAP_INFO_GROUP_COUNT_VERSION) {
            skipScenarioInfoBytes(section, Integer.BYTES);
        }
        if (version > MAP_INFO_DESCRIPTOR_COUNT_VERSION) {
            skipScenarioInfoBytes(section, Integer.BYTES * 3);
        }
        if (version > MAP_INFO_MUSIC_COUNT_VERSION) {
            skipScenarioInfoBytes(section, Integer.BYTES);
        }

        String displayName = readFixedCString(section, MAP_TITLE_BYTES);
        entry.displayName = containsNativeText(displayName) ? displayName : fileName;
        entry.recommendedPlayers = section.getInt();
        entry.mapLevel = section.getInt();
        if (version > MAP_INFO_EXTENDED_FIELDS_VERSION) {
            skipScenarioInfoBytes(section, Integer.BYTES * 2);
        }

        String tooltipText = readFixedCString(section, MAP_DESCRIPTION_BYTES).replace('\n', '#');
        entry.tooltipText = containsNativeText(tooltipText) ? tooltipText : "";
        if (entry.tooltipText.isEmpty()) {
            entry.tooltipText = entry.displayName;
        }
        return entry.recommendedPlayers < 2 ? null : entry;
    }

    /**
     * Native support extracted from sequential CFile::Read skips in MapDescriptor::readAvailableMapEntry @0044D5AD.
     */
    private static void skipScenarioInfoBytes(ByteBuffer section, int byteCount) {
        section.position(section.position() + byteCount);
    }

    /**
     * Native support extracted from fixed-size char-buffer CString assignments in MapDescriptor::readAvailableMapEntry
     *
     * @0044D5AD.
     */
    private static String readFixedCString(ByteBuffer source, int byteCount) {
        return new CString(byteCount).read(source).toString();
    }

    /**
     * Native support extracted from the worker's printable-text scan at @004496C0 and @0044970E.
     */
    private static boolean containsNativeText(String value) {
        for (int index = 0; index < value.length(); index++) {
            if (value.charAt(index) > ' ') {
                return true;
            }
        }
        return false;
    }

    /**
     * Native helper branch inside MultiplayerMapSelectionDialogVisualObject::OnMessage @0044BBE8 for
     * `MULTIPLAYER_LOBBY_APPEND_CHAT_MESSAGE`.
     * Fully ported.
     */
    private void appendLatestLobbyMessage() {
        MultiplayerMessagesTextListVisualObject chatList =
                (MultiplayerMessagesTextListVisualObject) getChildById(CHAT_LIST_ID);
        int rowCount = chatList.getRowCount();
        int selectedRow = chatList.getSelectedRow();
        PostSetupVisualObject chatScrollbar = (PostSetupVisualObject) getChildById(CHAT_SCROLLBAR_ID);
        chatScrollbar.syncSelectionState(selectedRow, rowCount);

        chatList.rows.add(readLatestLobbyMessageText());
        chatList.rowPalettes.add(readLatestLobbyMessagePalette());
        chatList.followAppendedRowIfSelectionAtEnd();
        chatList.draw();
    }

    /**
     * Native helper branch inside MultiplayerMapSelectionDialogVisualObject::OnMessage @0044BBE8 for
     * `MULTIPLAYER_LOBBY_REFRESH_PLAYER_LIST`.
     * Fully ported.
     */
    private void rebuildPlayerList() {
        MapVisualObject currentMapVisual = Globals.mainWindow.getMapVisual();
        MultiplayerPlayersTextListVisualObject playersList =
                (MultiplayerPlayersTextListVisualObject) getChildById(PLAYERS_LIST_ID);

        playersList.clearSelection();
        playersList.rowPalettes.clear();
        playersList.firstVisibleRow = 0;

        for (int playerIndex = 0x10; playerIndex < currentMapVisual.clientPlayers.size(); playerIndex++) {
            CPlayer player = currentMapVisual.clientPlayers.get(playerIndex);
            if (player != null) {
                playersList.rows.add(player.name.toString());
                playersList.rowPalettes.add(Palettes.p16.get(player.color));
            }
        }
        playersList.draw();
    }

    /**
     * Native helper block inside the confirm branch of MultiplayerMapSelectionDialogVisualObject::OnMessage @0044BBE8.
     * Fully ported.
     */
    private void applySelectedMapOutput() {
        MultiplayerMapSelectionEntry selectedEntry = getAvailableMapByIndex(readSelectedMapRow());
        writeCString(selectedMapNameOut, selectedEntry.internalMapId);
    }

    /**
     * Native helper block inside the map-selection branch of MultiplayerMapSelectionDialogVisualObject::OnMessage
     * at `0044BBE8`.
     * Fully ported.
     */
    private void postSelectedMapAnnouncement() {
        MultiplayerMapSelectionEntry selectedEntry = availableMaps.get(selectedMapIndex);
        mapVisual.sendSelectedMultiplayerMapAction(selectedEntry.displayName);
    }

    /**
     * Native helper block inside the map-selection branch of MultiplayerMapSelectionDialogVisualObject::OnMessage
     * at `0044BBE8`.
     * Fully ported.
     */
    private void refreshOkButtonState() {
        CVisualObject okButton = getChildById(OK_BUTTON_ID);
        okButton.setStateFlag(1, isSelectedMapCompatibleWithSession() ? 1 : 0);
        okButton.draw();
    }

    /**
     * Native branch inside MultiplayerMapSelectionDialogVisualObject::OnMessage @0044BBE8 for
     * `MULTIPLAYER_MAP_SELECTION_SELECT_MAP_BY_NAME`.
     * Fully ported.
     */
    private void selectMapByName(String displayName, int boundsGateIndex) {
        if (boundsGateIndex >= 0 && boundsGateIndex < availableMaps.size()) {
            for (int index = 0; index < availableMaps.size(); index++) {
                MultiplayerMapSelectionEntry entry = availableMaps.get(index);
                if (entry.displayName.equalsIgnoreCase(displayName)) {
                    selectedMapIndex = index;
                    break;
                }
            }
        }
    }

    /**
     * Native: MultiplayerMapSelectionDialogVisualObject::stopMapListWorker @004498B2.
     * Fully ported. Java clears the recovered stop boundary directly because the worker body runs synchronously.
     */
    private void stopMapListWorker() {
        workerStopEvent = null;
        workerThread = null;
    }

    /**
     * Native helper block inside MultiplayerMapSelectionDialogVisualObject::OnMessage @0044BBE8.
     * Fully ported. Native derives a 1..4 bucket from the final character of the selected row text and compares it
     * against `m_GameSession` min/max-player bounds.
     */
    private boolean isSelectedMapCompatibleWithSession() {
        String selectedMapLabel = readSelectedMapLabel();
        int selectedPlayerBucket = selectedMapLabel.charAt(selectedMapLabel.length() - 1) - '1';
        if (selectedPlayerBucket < 0) {
            selectedPlayerBucket = 0;
        }
        if (selectedPlayerBucket > 3) {
            selectedPlayerBucket = 3;
        }

        int minimumPlayers = readGameSessionMinimumPlayers();
        int maximumPlayers = readGameSessionMaximumPlayers();
        int selectedPlayers = selectedPlayerBucket + 1;
        return selectedPlayers >= minimumPlayers && selectedPlayers <= maximumPlayers;
    }

    /**
     * Native helper path through `TextListVisualObject::selectedRow` in MultiplayerMapSelectionDialogVisualObject
     * branches at `0044BBE8`.
     * Fully ported.
     */
    private int readSelectedMapRow() {
        TextListVisualObject mapList = (TextListVisualObject) getChildById(MAP_LIST_ID);
        return mapList.selectedRow;
    }

    /**
     * Native helper path through `FUN_0044F2D0` and `TextListVisualObject` rows in
     * MultiplayerMapSelectionDialogVisualObject::OnMessage at `0044BBE8`.
     * Fully ported.
     */
    private String readSelectedMapLabel() {
        TextListVisualObject mapList = (TextListVisualObject) getChildById(MAP_LIST_ID);
        return mapList.getRowTextAtClampedIndex(selectedMapIndex);
    }

    /**
     * Native helper path through the class-owned available-map array in MultiplayerMapSelectionDialogVisualObject
     * branches at `0044BBE8`.
     * Fully ported.
     */
    private MultiplayerMapSelectionEntry getAvailableMapByIndex(int index) {
        return availableMaps.get(index);
    }

    /**
     * Native support extracted from MultiplayerMapSelectionDialogVisualObject::OnMessage @0044BBE8
     * and CGameSession::getMinimumPlayerCount @0041FAA0.
     */
    private static int readGameSessionMinimumPlayers() {
        return Globals.mainWindow.m_GameSession.getMinimumPlayerCount();
    }

    /**
     * Native support extracted from MultiplayerMapSelectionDialogVisualObject::OnMessage @0044BBE8
     * and CGameSession::getMaximumPlayerCount @0041FAC0.
     */
    private static int readGameSessionMaximumPlayers() {
        return Globals.mainWindow.m_GameSession.getMaximumPlayerCount();
    }

    /**
     * Native support extracted from MultiplayerMapSelectionDialogVisualObject::Initialize @0044B0FB.
     * Resolves the native `Map + 0xA28` chat-log field.
     * Fully ported.
     */
    private CGameListControl resolveLobbyChatLog() {
        return mapVisual.gameListControl;
    }

    /**
     * Native support boundary for the latest chat-row text read in MultiplayerMapSelectionDialogVisualObject::OnMessage
     * at `0044BBE8`.
     * Fully ported.
     */
    private String readLatestLobbyMessageText() {
        return lobbyChatLog.m_arrText.getLast();
    }

    /**
     * Native support boundary for the latest chat-row palette read in MultiplayerMapSelectionDialogVisualObject::OnMessage
     * at `0044BBE8`.
     * Fully ported.
     */
    private Palette16 readLatestLobbyMessagePalette() {
        return lobbyChatLog.m_arrData.getLast();
    }

    /**
     * Native support boundary for pointer-carried `MULTIPLAYER_MAP_SELECTION_APPEND_AVAILABLE_MAP` in
     * MultiplayerMapSelectionDialogVisualObject::OnMessage @0044BBE8.
     * Fully ported for Java object-message payloads.
     */
    private MultiplayerMapSelectionEntry readPendingAvailableMapEntry(
            Object wParam,
            @SuppressWarnings("unused") Object lParam
    ) {
        if (wParam instanceof MultiplayerMapSelectionEntry entry) {
            return entry;
        }
        throw new IllegalArgumentException("Expected MultiplayerMapSelectionEntry message payload");
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2 and
     * MultiplayerMapSelectionDialogVisualObject::OnMessage @0044BBE8.
     * Fully ported.
     */
    private static String readPendingSelectedMapName(Object wParam) {
        if (wParam instanceof CString text) {
            return text.toString();
        }
        if (wParam instanceof CharSequence text) {
            return text.toString();
        }
        throw new IllegalArgumentException("Expected selected map name payload");
    }

    /**
     * Native support extracted from MultiplayerMapSelectionDialogVisualObject::OnMessage @0044BBE8 for
     * `CString::operator=` in the confirm path.
     * Fully ported.
     */
    private static void writeCString(CString target, String value) {
        target.set(value.getBytes(StandardCharsets.ISO_8859_1));
    }

}
