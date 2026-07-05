package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.net.CLlDriver;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.DirectPlayRuntime;
import ua.millfreedom.rom2.model.LlDriverSessionEntry;
import ua.millfreedom.rom2.model.MultiplayerSessionDialogContext;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.enums.ProtocolId;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.window.MessageSystem;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static ua.millfreedom.rom2.model.enums.MessageCodes.NOTIFY_DIALOG_CLOSED;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SHOW_MULTIPLAYER_CONNECTION_DIALOG;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SHOW_HAT_SERVER_BROWSER_DIALOG;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SHOW_TCP_IP_SETTINGS_DIALOG;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SHOW_MODEM_SETTINGS_DIALOG;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SHOW_SERIAL_SETTINGS_DIALOG;
import static ua.millfreedom.rom2.model.enums.MessageCodes.MULTIPLAYER_CANCEL_PENDING_OPERATION;
import static ua.millfreedom.rom2.model.enums.MessageCodes.MULTIPLAYER_CREATE_GAME;
import static ua.millfreedom.rom2.model.enums.MessageCodes.MULTIPLAYER_ENUMERATION_COMPLETE;
import static ua.millfreedom.rom2.model.enums.MessageCodes.MULTIPLAYER_JOIN_GAME;
import static ua.millfreedom.rom2.model.enums.MessageCodes.MULTIPLAYER_REFRESH_GAMES;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RETURN_TO_GAME;
import static ua.millfreedom.rom2.model.enums.MessageCodes.TEXT_LIST_SELECTION_DBLCLK;
import static ua.millfreedom.rom2.model.window.windowproc.handlers.CMainWindowWindowProcSupport.readMessageInt;
import static ua.millfreedom.rom2.text.DialogsText.AVAILABLE_GAMES_82;
import static ua.millfreedom.rom2.text.DialogsText.CANCEL_1;
import static ua.millfreedom.rom2.text.DialogsText.CONNECTING_163;
import static ua.millfreedom.rom2.text.DialogsText.CREATE_86;
import static ua.millfreedom.rom2.text.DialogsText.CREATE_OR_JOIN_GAME_142;
import static ua.millfreedom.rom2.text.DialogsText.DIALING_NOW_PLEASE_WAIT_119;
import static ua.millfreedom.rom2.text.DialogsText.GAME_NOT_STARTED_124;
import static ua.millfreedom.rom2.text.DialogsText.GAMES_83;
import static ua.millfreedom.rom2.text.DialogsText.JOIN_87;
import static ua.millfreedom.rom2.text.DialogsText.PLAYER_LIST_84;
import static ua.millfreedom.rom2.text.DialogsText.PLAYERS_85;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.TextTableId.DIALOGS;

/**
 * Native class: CenteredDialogContextArrayVisualObject.
 * Purpose: centered multiplayer dialog variant with a session-browser context, saved bounds, selection state, and an
 * array-backed per-session player cache.
 */
public class CenteredDialogContextArrayVisualObject extends CenteredDialogVisualObject {
    private static final String NO_PREVIOUS_SESSION_SENTINEL =
            "When night are cold and friends are few I sit alone and think of you";
    private static final int GAME_LIST_ID = 1;
    private static final int GAME_LIST_SCROLLBAR_ID = 2;
    private static final int GAME_LIST_HEADER_ID = 4;
    private static final int PLAYER_LIST_HEADER_ID = 6;
    private static final int PLAYER_LIST_ID = 7;
    private static final int PLAYER_LIST_SCROLLBAR_ID = 8;
    private static final int CREATE_BUTTON_ID = 0x0E;
    private static final int JOIN_BUTTON_ID = 0x0F;
    private static final int CANCEL_BUTTON_ID = 0x10;
    private static final int REFRESH_BUTTON_ID = 0x11;
    private static final int JOIN_BUTTON_EXTRA_STATE_FLAG = 0x10;

    public static final int NATIVE_SIZE = 0x94; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    //0x64
    public int selectedGameIndex;
    //0x68
    public MultiplayerSessionDialogContext context;
    //0x6c
    public final CRect rect = new CRect();
    //0x7c
    public final List<List<String>> cachedPlayerRows = new ArrayList<>();

    /**
     * Native: CenteredDialogContextArrayVisualObject::CenteredDialogContextArrayVisualObject @00445677.
     * Fully ported.
     */
    public CenteredDialogContextArrayVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            MultiplayerSessionDialogContext context
    ) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        this.selectedGameIndex = 0;
        this.context = context;
        this.context.sessionEntries.clear();
        this.context.sessionEntryCount = 0;
    }

    /**
     * vtbl +0x78: CenteredDialogContextArrayVisualObject::Initialize @00446047.
     * Fully ported. Native protocol split, saved-bounds handling, child creation, and refresh-post dispatch are ported.
     */
    @Override
    public void initialize() {
        CBitmapFont dialogFont = Globals.fonts.font1;
        int protocolId = CLlDriver.getProtocolId();
        if (protocolId == ProtocolId.DPSP_MODEM
                || protocolId == ProtocolId.DPSP_SERIAL) {
            rect.set(cRect);
            setBounds(0, 0, 0x17C, 0x0C4);
            centerOnScreen(Globals.screenRect.right, Globals.screenRect.bottom);

            addChild(new DialogWindowVisualObject(
                    1,
                    0x28,
                    0x30,
                    0xDC,
                    0x4A,
                    protocolId == ProtocolId.DPSP_MODEM
                            ? get(DIALOGS, DIALING_NOW_PLEASE_WAIT_119)
                            : get(DIALOGS, CONNECTING_163),
                    dialogFont,
                    Palettes.grayDim,
                    0
            ));
            addChild(createButton(
                    2,
                    new CRect(
                            cRect.width() / 2 - 0x30,
                            cRect.height() / 2 + 0x0C,
                            cRect.width() / 2 + 0x30,
                            cRect.height() / 2 + 0x24
                    ),
                    get(DIALOGS, CANCEL_1),
                    MULTIPLAYER_CANCEL_PENDING_OPERATION,
                    get(DIALOGS, CANCEL_1),
                    dialogFont
            ));
        } else {
            initializeCreateOrJoinContent(dialogFont);
        }

        selectedGameIndex = 0;
        Globals.mainWindow.postMessage(MULTIPLAYER_REFRESH_GAMES, 0, 0);
    }

    /**
     * vtbl +0x48: CenteredDialogContextArrayVisualObject::OnMessage @004463C4.
     * Fully ported for native DirectPlay branches; Java also maps the visible raw TCP/IP replacement row to the
     * native TCP/IP settings return route.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        int w = readMessageInt(wParam);
        int l = readMessageInt(lParam);
        return switch (msg) {
            case TEXT_LIST_SELECTION_DBLCLK -> {
                CVisualObject joinButton = getChildById(JOIN_BUTTON_ID);
                if (joinButton.checkStateFlag(1) == 0) {
                    yield 0;
                }
                yield handleCommitSelection(TEXT_LIST_SELECTION_DBLCLK);
            }
            case MULTIPLAYER_CREATE_GAME, MULTIPLAYER_JOIN_GAME -> handleCommitSelection(msg);
            case RETURN_TO_GAME -> {
                super.onMessage(msg, wParam, lParam);
                clearMainWindowDialogsMask();

                int protocolId = CLlDriver.getProtocolId();
                if (protocolId == ProtocolId.DPSP_MODEM) {
                    CLlDriver.handleNetworkErrorAndClose();
                    DirectPlayRuntime.probeAvailabilityBoundary();
                    Globals.mainWindow.postMessage(SHOW_MODEM_SETTINGS_DIALOG, 0, 0);
                } else if (protocolId == ProtocolId.DPSP_SERIAL) {
                    CLlDriver.handleNetworkErrorAndClose();
                    DirectPlayRuntime.probeAvailabilityBoundary();
                    Globals.mainWindow.postMessage(SHOW_SERIAL_SETTINGS_DIALOG, 0, 0);
                } else if (protocolId == ProtocolId.DPSP_TCPIP || protocolId == ProtocolId.TCP_IP) {
                    CLlDriver.handleNetworkErrorAndClose();
                    if (protocolId == ProtocolId.DPSP_TCPIP) {
                        DirectPlayRuntime.probeAvailabilityBoundary();
                    }
                    Globals.mainWindow.postMessage(
                            protocolId == ProtocolId.DPSP_TCPIP && shouldResumeDirectPlaySetup()
                                    ? SHOW_HAT_SERVER_BROWSER_DIALOG
                                    : SHOW_TCP_IP_SETTINGS_DIALOG,
                            0,
                            0
                    );
                } else {
                    CLlDriver.handleNetworkErrorAndClose();
                    Globals.mainWindow.postMessage(SHOW_MULTIPLAYER_CONNECTION_DIALOG, 0, 0);
                }

                clearCachedPlayerRows();
                yield 1;
            }
            case MULTIPLAYER_REFRESH_GAMES -> {
                refreshActiveSessions();
                yield 1;
            }
            case TEXT_LIST_SELECTION_CHANGED -> {
                if (w == GAME_LIST_ID) {
                    selectedGameIndex = Math.max(l, 0);
                    refreshPlayerListFromCache();
                }
                yield 1;
            }
            case MULTIPLAYER_ENUMERATION_COMPLETE -> {
                int protocolId = CLlDriver.getProtocolId();
                if (protocolId == ProtocolId.DPSP_MODEM
                        || protocolId == ProtocolId.DPSP_SERIAL) {
                    cRect.set(rect);
                    writeMainWindowPlayerNameToContext();
                    context.committedSessionIndex = 0;

                    if (context.sessionEntryCount == 0) {
                        clearChildren();
                        addChild(new DialogWindowVisualObject(
                                1,
                                0x28,
                                0x30,
                                0xDC,
                                0x4A,
                                get(DIALOGS, GAME_NOT_STARTED_124),
                                Globals.fonts.font1,
                                Palettes.grayDim,
                                0
                        ));
                        addChild(createButton(
                                2,
                                new CRect(
                                        cRect.width() / 2 - 0x30,
                                        cRect.height() / 2 + 0x0C,
                                        cRect.width() / 2 + 0x30,
                                        cRect.height() / 2 + 0x24
                                ),
                                get(DIALOGS, CANCEL_1),
                                RETURN_TO_GAME,
                                get(DIALOGS, CANCEL_1),
                                Globals.fonts.font1
                        ));
                    } else {
                        applyModemPhoneBookSideEffect();
                        hideDialog(MULTIPLAYER_JOIN_GAME);
                        Globals.mainWindow.postMessage(NOTIFY_DIALOG_CLOSED, this, 0);
                        Globals.mainWindow.postMessage(MULTIPLAYER_JOIN_GAME, 0, 0);
                    }
                    yield 1;
                }

                refreshGameListFromContext();
                refreshPlayerListFromCache();
                context.selectedSessionIndex = selectedGameIndex;
                yield 1;
            }
            case MULTIPLAYER_CANCEL_PENDING_OPERATION -> {
                signalSessionEnumerationEvent();
                yield 1;
            }
            default -> super.onMessage(msg, wParam, lParam);
        };
    }

    /**
     * vtbl +0x6C: CenteredDialogContextArrayVisualObject::OnKeyDown @00446A99.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        if (nChar == 0x1B) {
            onMessage(MULTIPLAYER_CANCEL_PENDING_OPERATION, 0, 0);
            return 0;
        }
        return super.onKeyDown(nChar);
    }

    /**
     * Native support extracted from CenteredDialogContextArrayVisualObject::initializeCreateOrJoinContent @00445717.
     * Fully ported.
     */
    private void initializeCreateOrJoinContent(CBitmapFont dialogFont) {
        int dialogWidth = cRect.width();
        int dialogHeight = cRect.height();
        int listWidth = (dialogWidth - 0xB0) / 2;

        addChild(new DialogWindowVisualObject(
                0x19,
                0x28,
                0x20,
                dialogWidth - 0x28,
                0x38,
                get(DIALOGS, CREATE_OR_JOIN_GAME_142),
                dialogFont,
                Palettes.grayDim,
                0x2
        ));

        TextListVisualObject gameList = new TextListVisualObject(
                GAME_LIST_ID,
                new CRect(0x28, 0x50, 0x28 + listWidth, dialogHeight - 0x60),
                dialogFont,
                Palettes.grayDim,
                Palettes.gray,
                GAME_LIST_SCROLLBAR_ID,
                get(DIALOGS, AVAILABLE_GAMES_82)
        );
        addChild(gameList);

        CRect gameListRect = gameList.getRect();
        PostSetupVisualObject gameScrollbar = new PostSetupVisualObject(
                GAME_LIST_SCROLLBAR_ID,
                gameListRect.right,
                gameListRect.top,
                gameListRect.right + 0x18,
                gameListRect.bottom,
                null
        );
        addChild(gameScrollbar);

        DialogWindowVisualObject gameHeader = new DialogWindowVisualObject(
                GAME_LIST_HEADER_ID,
                gameListRect.left,
                gameListRect.top - 0x18,
                gameListRect.right,
                gameListRect.top,
                get(DIALOGS, GAMES_83),
                dialogFont,
                Palettes.grayDim,
                0
        );
        addChild(gameHeader);
        gameList.gameDialogControls = gameHeader;

        TextListVisualObject playerList = new TextListVisualObject(
                PLAYER_LIST_ID,
                new CRect(gameScrollbar.getRect().right + 0x18, gameListRect.top, gameScrollbar.getRect().right + 0x18 + listWidth, gameListRect.bottom),
                dialogFont,
                Palettes.grayDim,
                Palettes.gray,
                PLAYER_LIST_SCROLLBAR_ID,
                get(DIALOGS, PLAYER_LIST_84)
        );
        addChild(playerList);

        CRect playerListRect = playerList.getRect();
        addChild(new DialogWindowVisualObject(
                PLAYER_LIST_HEADER_ID,
                playerListRect.left,
                playerListRect.top - 0x18,
                playerListRect.right,
                playerListRect.top,
                get(DIALOGS, PLAYERS_85),
                dialogFont,
                Palettes.grayDim,
                0
        ));
        playerList.gameDialogControls = getChildById(PLAYER_LIST_HEADER_ID);

        addChild(new PostSetupVisualObject(
                PLAYER_LIST_SCROLLBAR_ID,
                playerListRect.right,
                playerListRect.top,
                playerListRect.right + 0x18,
                playerListRect.bottom,
                null
        ));

        int buttonTop = dialogHeight - 0x3C;
        int buttonBottom = dialogHeight - 0x24;
        CommandButtonVisualObject refreshButton = createButton(
                REFRESH_BUTTON_ID,
                new CRect((dialogWidth * 3) / 0x1A, buttonTop, (dialogWidth * 7) / 0x1A, buttonBottom),
                get(DIALOGS, GAMES_83),
                MULTIPLAYER_REFRESH_GAMES,
                get(DIALOGS, GAMES_83),
                dialogFont
        );
        addChild(refreshButton);

        CommandButtonVisualObject joinButton = createButton(
                JOIN_BUTTON_ID,
                new CRect((dialogWidth << 3) / 0x1A, buttonTop, (dialogWidth * 0x0C) / 0x1A, buttonBottom),
                get(DIALOGS, JOIN_87),
                MULTIPLAYER_JOIN_GAME,
                get(DIALOGS, JOIN_87),
                dialogFont
        );
        joinButton.setStateFlag(JOIN_BUTTON_EXTRA_STATE_FLAG, 1);
        addChild(joinButton);
        updateButtonEnabledState(joinButton, false);
        joinButton.leftNeighbor = refreshButton;
        refreshButton.rightNeighbor = joinButton;

        CommandButtonVisualObject createButton = createButton(
                CREATE_BUTTON_ID,
                new CRect((dialogWidth * 0x0D) / 0x1A, buttonTop, (dialogWidth * 0x11) / 0x1A, buttonBottom),
                get(DIALOGS, CREATE_86),
                MULTIPLAYER_CREATE_GAME,
                get(DIALOGS, CREATE_86),
                dialogFont
        );
        addChild(createButton);
        if (!Globals.hasValidGameMedia) {
            updateButtonEnabledState(createButton, false);
        }
        createButton.leftNeighbor = joinButton;
        joinButton.rightNeighbor = createButton;

        CommandButtonVisualObject cancelButton = createButton(
                CANCEL_BUTTON_ID,
                new CRect((dialogWidth * 0x12) / 0x1A, buttonTop, (dialogWidth * 0x16) / 0x1A, buttonBottom),
                get(DIALOGS, CANCEL_1),
                RETURN_TO_GAME,
                get(DIALOGS, CANCEL_1),
                dialogFont
        );
        addChild(cancelButton);
        cancelButton.leftNeighbor = createButton;
        createButton.rightNeighbor = cancelButton;
    }

    /**
     * Native support extracted from CenteredDialogContextArrayVisualObject::OnMessage @004463C4.
     * Fully ported.
     */
    private int handleCommitSelection(MessageCodes closeReason) {
        writeMainWindowPlayerNameToContext();
        context.committedSessionIndex = readSelectedRow(GAME_LIST_ID);
        hideDialog(closeReason);
        Globals.mainWindow.postMessage(NOTIFY_DIALOG_CLOSED, this, 0);
        clearCachedPlayerRows();
        return 1;
    }

    /**
     * Native support extracted from CenteredDialogContextArrayVisualObject::refreshPlayerListFromCache @00446CEC.
     * Fully ported.
     */
    private void refreshPlayerListFromCache() {
        TextListVisualObject playerList = (TextListVisualObject) getChildById(PLAYER_LIST_ID);
        while (playerList.getRowCount() != 0) {
            playerList.removeRowAndAdjustSelection(playerList.getRowCount() - 1);
        }
        playerList.firstVisibleRow = 0;
        playerList.selectedRow = -1;
        if (0 <= selectedGameIndex && selectedGameIndex < cachedPlayerRows.size()) {
            for (String row : cachedPlayerRows.get(selectedGameIndex)) {
                playerList.rows.add(row);
            }
        }

        PostSetupVisualObject scrollbar = (PostSetupVisualObject) getChildById(PLAYER_LIST_SCROLLBAR_ID);
        scrollbar.setCurrentValueAndMaxValue(new Point(0, playerList.rows.size()));
        playerList.draw();
    }

    /**
     * Native support extracted from CenteredDialogContextArrayVisualObject::refreshGameListFromContext @00446B2E.
     * Fully ported.
     */
    private void refreshGameListFromContext() {
        TextListVisualObject gameList = (TextListVisualObject) getChildById(GAME_LIST_ID);
        int previousRowCount = gameList.getRowCount();
        String previousSelectedSessionName = selectedGameIndex < previousRowCount
                ? gameList.getRowTextAtClampedIndex(selectedGameIndex)
                : NO_PREVIOUS_SESSION_SENTINEL;

        while (gameList.getRowCount() != 0) {
            gameList.removeRowAndAdjustSelection(gameList.getRowCount() - 1);
        }

        int sessionCount = context.sessionEntryCount;
        selectedGameIndex = 0;
        for (int index = 0; index < sessionCount; index++) {
            String sessionName = context.sessionEntries.get(index).sessionName;
            gameList.rows.add(sessionName);
            if (sessionName.equals(previousSelectedSessionName)) {
                selectedGameIndex = index;
                gameList.selectedRow = index;
            }
        }

        updateButtonEnabledState(getChildById(JOIN_BUTTON_ID), sessionCount != 0);
        gameList.draw();
    }

    /**
     * Java helper for the repeated CommandButtonVisualObject constructor branches in
     * CenteredDialogContextArrayVisualObject::Initialize @00446047 / FUN_00445717 @00445717.
     * not ported.
     */
    private static CommandButtonVisualObject createButton(
            int id,
            CRect rect,
            String caption,
            MessageCodes msg,
            String name,
            CBitmapFont dialogFont
    ) {
        return new CommandButtonVisualObject(id, rect, caption, dialogFont, Palettes.grayDim, msg, 0, name);
    }

    /**
     * Java helper for repeated enabled-flag updates in CenteredDialogContextArrayVisualObject native button paths.
     * not ported.
     */
    private static void updateButtonEnabledState(CVisualObject child, boolean enabled) {
        child.setStateFlag(1, enabled ? 1 : 0);
        if (!enabled && child instanceof CommandButtonVisualObject button) {
            button.pressedState = 0;
        }
        child.draw();
    }

    /**
     * Java helper for repeated enabled-flag updates in CenteredDialogContextArrayVisualObject native button paths.
     * not ported.
     */
    private static void updateButtonEnabledState(CommandButtonVisualObject button, boolean enabled) {
        updateButtonEnabledState((CVisualObject) button, enabled);
    }

    /**
     * Native support extracted from CenteredDialogContextArrayVisualObject::refreshActiveSessions @00446323.
     * Fully ported. CLlDriver enumeration remains a CLlDriver boundary.
     */
    private void refreshActiveSessions() {
        CLlDriver.getActiveSessions(context, CenteredDialogContextArrayVisualObject::pumpSessionEnumerationMessages, resolveRefreshTimeout());
        rebuildCachedPlayerRows();
        Globals.mainWindow.postMessage(MULTIPLAYER_ENUMERATION_COMPLETE, 0, 0);
    }

    /**
     * Native support boundary for the Win32 PeekMessage/TranslateMessage/DispatchMessage callback @004462B5 used by
     * CenteredDialogContextArrayVisualObject::refreshActiveSessions @00446323.
     * not ported.
     */
    private static boolean pumpSessionEnumerationMessages() {
        MessageSystem.pumpPostedMessage();
        return true;
    }

    /**
     * Native support extracted from CenteredDialogContextArrayVisualObject::rebuildCachedPlayerRows @00446DD7.
     * Fully ported. Player-name enumeration remains a CLlDriver boundary.
     */
    private void rebuildCachedPlayerRows() {
        clearCachedPlayerRows();
        context.sessionEntries.sort(Comparator.comparing(
                session -> session.sessionName,
                CenteredDialogContextArrayVisualObject::compareSessionNamesWithLocalHostFirst
        ));
        context.sessionEntryCount = context.sessionEntries.size();
        for (LlDriverSessionEntry session : context.sessionEntries) {
            cachedPlayerRows.add(new ArrayList<>(CLlDriver.getSessionPlayerNames(session)));
        }
    }

    /**
     * Native support extracted from CenteredDialogContextArrayVisualObject::compareSessionNamesWithLocalHostFirst @00446ACE.
     * Fully ported.
     */
    private static int compareSessionNamesWithLocalHostFirst(String left, String right) {
        String localSessionName = getLocalSessionSortSentinel();
        if (left.equals(localSessionName)) {
            return -1;
        }
        if (right.equals(localSessionName)) {
            return 1;
        }
        return left.compareTo(right);
    }

    /**
     * Native support extracted from CenteredDialogContextArrayVisualObject::rebuildCachedPlayerRows @00446DD7.
     * Fully ported.
     */
    private static String getLocalSessionSortSentinel() {
        String computerName = System.getenv("COMPUTERNAME");
        if (computerName == null) {
            computerName = "";
        }
        if (computerName.length() > 16) {
            computerName = computerName.substring(0, 16);
        }
        return "-" + computerName + "-";
    }

    /**
     * Native support extracted from CenteredDialogContextArrayVisualObject::refreshActiveSessions @00446323.
     * Partially ported: raw TCP/IP uses Java's visible TCP-only replacement discovery timeout.
     */
    private static int resolveRefreshTimeout() {
        return switch (CLlDriver.getProtocolId()) {
            case ProtocolId.TCP_IP, ProtocolId.DPSP_TCPIP -> 0x5DC;
            case ProtocolId.DPSP_IPX -> 300;
            case ProtocolId.DPSP_MODEM -> 60000;
            default -> 0;
        };
    }

    /**
     * Native support extracted from CenteredDialogContextArrayVisualObject::OnMessage @004463C4.
     * Fully ported.
     */
    private void writeMainWindowPlayerNameToContext() {
        context.playerName = Globals.mainWindow.m_GameSession.m_PlayerName;
    }

    /**
     * Native support extracted from `CMainWindow::dialogsMask = 0` in
     * CenteredDialogContextArrayVisualObject::OnMessage @004463C4.
     * Fully ported.
     */
    private static void clearMainWindowDialogsMask() {
        Globals.mainWindow.dialogsMask = 0;
    }

    /**
     * Native support extracted from `(wnd->connectionScratchState).serverListSourceIsWebPage` in
     * CenteredDialogContextArrayVisualObject::OnMessage @004463C4.
     * Fully ported.
     */
    private static boolean shouldResumeDirectPlaySetup() {
        return Globals.mainWindow.connectionScratchState.serverListSourceIsWebPage != 0;
    }

    /**
     * Native support boundary for the modem phone-book side effect near FUN_004469F2 in
     * CenteredDialogContextArrayVisualObject::OnMessage @004463C4.
     * not ported.
     */
    private static void applyModemPhoneBookSideEffect() {
    }

    /**
     * Native support boundary for CLlDriver::SignalSessionEnumerationEvent @005080D6 in
     * CenteredDialogContextArrayVisualObject::OnMessage @004463C4.
     * not ported.
     */
    private static void signalSessionEnumerationEvent() {
        CLlDriver.signalSessionEnumerationEventBoundary();
    }

    /**
     * Java helper for the repeated `CArray<CStringArray*>` cleanup loops in
     * CenteredDialogContextArrayVisualObject::OnMessage @004463C4 and FUN_00446DD7 @00446DD7.
     * not ported.
     */
    private void clearCachedPlayerRows() {
        for (List<String> rows : cachedPlayerRows) {
            rows.clear();
        }
        cachedPlayerRows.clear();
    }

    /**
     * Native support extracted from CenteredDialogContextArrayVisualObject::OnMessage @004463C4.
     * Fully ported.
     */
    private int readSelectedRow(int childId) {
        return ((TextListVisualObject) getChildById(childId)).getSelectedRow();
    }

}
