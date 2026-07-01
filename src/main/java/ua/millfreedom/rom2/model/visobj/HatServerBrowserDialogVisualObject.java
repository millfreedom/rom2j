package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palettes;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static ua.millfreedom.rom2.model.enums.MessageCodes.*;
import static ua.millfreedom.rom2.model.window.windowproc.handlers.CMainWindowWindowProcSupport.readMessageInt;
import static ua.millfreedom.rom2.text.DialogsText.CANCEL_1;
import static ua.millfreedom.rom2.text.DialogsText.OK_0;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.PatchText.*;
import static ua.millfreedom.rom2.text.TextTableId.DIALOGS;
import static ua.millfreedom.rom2.text.TextTableId.PATCH;

/**
 * Native class: HatServerBrowserDialogVisualObject.
 * Purpose: centered HAT server browser dialog with source URL, server-entry list, and refresh/confirm controls.
 */
public class HatServerBrowserDialogVisualObject extends CenteredDialogVisualObject {
    private static final int TITLE_ID = 0x19;
    private static final int SOURCE_URL_ID = 0x7E;
    private static final int SERVER_LIST_HEADER_ID = 0x1A;
    private static final int SERVER_LIST_ID = 1;
    private static final int SERVER_LIST_SCROLLBAR_ID = 10;
    private static final int OK_BUTTON_ID = 0x14;
    private static final int CANCEL_BUTTON_ID = 0x15;
    private static final int REFRESH_BUTTON_ID = 0x16;
    private static final String HAT_HTTP_PREFIX = "http://";
    private static final String HAT_SERVER_COUNT_MARKER = "CURRENTCOUNT";
    private static final String HAT_USER_AGENT = "RageOfMages2";
    private static final List<String> HAT_SERVER_ROWS = new ArrayList<>();

    public static final int NATIVE_SIZE = 0x68; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    /**
     * Native: HatServerBrowserDialogVisualObject::HatServerBrowserDialogVisualObject @0044A5F8.
     * Fully ported.
     */
    public HatServerBrowserDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        super(id, xLeft, yTop, xRight, yBottom, null);
    }

    /**
     * vtbl +0x78: HatServerBrowserDialogVisualObject::Initialize @0044A62F.
     * Fully ported.
     */
    @Override
    public void initialize() {
        CBitmapFont dialogFont = Globals.fonts.font1;

        addChild(new DialogWindowVisualObject(
                TITLE_ID,
                0x28,
                0x16,
                cRect.width() - 0x28,
                0x38,
                get(PATCH, SERVER_LIST_FROM_106),
                dialogFont,
                Palettes.grayDim,
                0x2
        ));
        addChild(new DialogWindowVisualObject(
                SOURCE_URL_ID,
                0x28,
                0x2C,
                cRect.width() - 0x28,
                0x44,
                readHatIp(),
                dialogFont,
                Palettes.grayDim,
                0x2
        ));
        addChild(new DialogWindowVisualObject(
                SERVER_LIST_HEADER_ID,
                0x28,
                0x44,
                cRect.width() - 0x28,
                0x5C,
                get(PATCH, SERVER_NAME_MAP_SIZE_LEVEL_PLAYERS_107),
                dialogFont,
                Palettes.grayDim,
                0
        ));

        HatServerEntriesTextListVisualObject serverList = new HatServerEntriesTextListVisualObject(
                SERVER_LIST_ID,
                0x28,
                0x5C,
                cRect.width() - 0x40,
                cRect.height() - 0x60,
                dialogFont,
                Palettes.grayDim,
                Palettes.gray,
                SERVER_LIST_SCROLLBAR_ID,
                null
        );
        addChild(serverList);
        serverList.gameDialogControls = getChildById(SERVER_LIST_HEADER_ID);

        CRect serverListRect = serverList.getRect();
        addChild(new PostSetupVisualObject(
                SERVER_LIST_SCROLLBAR_ID,
                serverListRect.right,
                serverListRect.top,
                serverListRect.right + 0x18,
                serverListRect.bottom,
                null
        ));

        int buttonTop = cRect.height() - 0x3C;
        int buttonBottom = cRect.height() - 0x18;
        CommandButtonVisualObject okButton = new CommandButtonVisualObject(
                OK_BUTTON_ID,
                new CRect(0x48, buttonTop, 0xA8, buttonBottom),
                get(DIALOGS, OK_0),
                dialogFont,
                Palettes.grayDim,
                DIALOG_OK,
                0,
                get(DIALOGS, OK_0)
        );
        addChild(okButton);
        okButton.setStateFlag(0x1, 0);

        CommandButtonVisualObject cancelButton = new CommandButtonVisualObject(
                CANCEL_BUTTON_ID,
                new CRect(0xD8, buttonTop, 0x138, buttonBottom),
                get(DIALOGS, CANCEL_1),
                dialogFont,
                Palettes.grayDim,
                RETURN_TO_GAME,
                0,
                get(DIALOGS, CANCEL_1)
        );
        addChild(cancelButton);
        linkButtonsHorizontally(cancelButton, okButton);

        CommandButtonVisualObject refreshButton = new CommandButtonVisualObject(
                REFRESH_BUTTON_ID,
                new CRect(0x168, buttonTop, 0x1C8, buttonBottom),
                get(PATCH, REFRESH_108),
                dialogFont,
                Palettes.grayDim,
                HAT_SERVER_BROWSER_REFRESH_REQUEST,
                0,
                get(PATCH, REFRESH_108)
        );
        addChild(refreshButton);
        linkButtonsHorizontally(refreshButton, cancelButton);
    }

    /**
     * vtbl +0x48: HatServerBrowserDialogVisualObject::OnMessage @0044AB6C.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        int w = readMessageInt(wParam);
        int l = readMessageInt(lParam);
        switch (msg) {
            case TEXT_LIST_SELECTION_DBLCLK:
                if (w != SERVER_LIST_ID) {
                    return 0;
                }
                return handleConfirmSelection();
            case DIALOG_OK:
                return handleConfirmSelection();
            case RETURN_TO_GAME:
                super.onMessage(msg, 0, 0);
                return 1;
            case TEXT_LIST_SELECTION_CHANGED:
            case TEXT_LIST_SELECTION_COMMITTED:
                if (w != SERVER_LIST_ID) {
                    return 0;
                }
                handleSelectionStateChange(l);
                return 1;
            case HAT_SERVER_BROWSER_REFRESH_COMPLETE:
                handleRefreshComplete(w);
                return 1;
            case HAT_SERVER_BROWSER_REFRESH_REQUEST:
                handleRefreshRequested();
                return super.onMessage(msg, wParam, lParam);
            default:
                return super.onMessage(msg, wParam, lParam);
        }
    }

    /**
     * Native helper block inside HatServerBrowserDialogVisualObject::OnMessage @0044AB6C for
     * `TEXT_LIST_SELECTION_DBLCLK` / `DIALOG_OK`.
     */
    private int handleConfirmSelection() {
        CVisualObject okButton = getChildById(OK_BUTTON_ID);
        if (okButton.checkStateFlag(0x1) == 0) {
            return 1;
        }

        String selectedHatServerToken = readSelectedHatServerToken();
        writeSelectedHatServerToken(selectedHatServerToken);
        super.onMessage(DIALOG_OK, 0, 0);
        return 1;
    }

    /**
     * Native helper branch inside HatServerBrowserDialogVisualObject::OnMessage @0044AB6C for
     * `TEXT_LIST_SELECTION_CHANGED` / `TEXT_LIST_SELECTION_COMMITTED`.
     */
    private void handleSelectionStateChange(int selectedRow) {
        if (selectedRow < 0) {
            return;
        }

        HatServerEntriesTextListVisualObject serverList =
                (HatServerEntriesTextListVisualObject) getChildById(SERVER_LIST_ID);
        serverList.draw();
        syncServerListScrollbar(serverList);
        boolean canJoin = serverList.isServerJoinable(serverList.selectedRow);
        updateOkButtonEnabled(canJoin);
    }

    /**
     * Native helper branch inside HatServerBrowserDialogVisualObject::OnMessage @0044AB6C for
     * `HAT_SERVER_BROWSER_REFRESH_COMPLETE`.
     */
    private void handleRefreshComplete(int refreshState) {
        HatServerEntriesTextListVisualObject serverList =
                (HatServerEntriesTextListVisualObject) getChildById(SERVER_LIST_ID);
        int selectedRow = serverList.selectedRow;
        int firstVisibleRow = serverList.getFirstVisibleRow();
        serverList.clearSelection();
        serverList.refreshState = refreshState;
        if (refreshState == 0) {
            firstVisibleRow = 0;
            selectedRow = 0;
        } else {
            List<String> refreshedRows = readHatServerRows();
            for (String refreshedRow : refreshedRows) {
                serverList.rows.add(refreshedRow);
            }
            if (serverList.rows.size() <= selectedRow) {
                selectedRow = serverList.rows.size() - 1;
            }
            if (serverList.rows.size() <= firstVisibleRow) {
                firstVisibleRow = serverList.rows.size() - 1;
            }
        }
        serverList.setFirstVisibleRow(firstVisibleRow);
        serverList.assignSelectedRow(selectedRow);
        syncServerListScrollbar(serverList);
        onMessage(TEXT_LIST_SELECTION_CHANGED, SERVER_LIST_ID, serverList.selectedRow);
        serverList.draw();
    }

    /**
     * Native helper branch inside HatServerBrowserDialogVisualObject::OnMessage @0044AB6C for
     * `HAT_SERVER_BROWSER_REFRESH_REQUEST`.
     */
    private void handleRefreshRequested() {
        HatServerEntriesTextListVisualObject serverList =
                (HatServerEntriesTextListVisualObject) getChildById(SERVER_LIST_ID);
        serverList.refreshState = -1;
        serverList.clearSelection();
        serverList.setFirstVisibleRow(0);
        serverList.assignSelectedRow(0);
        syncServerListScrollbar(serverList);
        serverList.draw();

        int refreshState = refreshHatServerRows(readHatIp());
        onMessage(HAT_SERVER_BROWSER_REFRESH_COMPLETE, refreshState, 0);
    }

    /**
     * Native helper block inside the confirm branch of HatServerBrowserDialogVisualObject::OnMessage @0044AB6C.
     */
    private String readSelectedHatServerToken() {
        HatServerEntriesTextListVisualObject serverList =
                (HatServerEntriesTextListVisualObject) getChildById(SERVER_LIST_ID);
        return HatServerEntriesTextListVisualObject.readDelimitedField(
                serverList.getRowTextAtClampedIndex(serverList.selectedRow),
                7
        );
    }

    /**
     * Java helper for the repeated `SetStateFlag(..., 1, enabled)` flow in
     * HatServerBrowserDialogVisualObject::OnMessage @0044AB6C.
     */
    private void updateOkButtonEnabled(boolean enabled) {
        CVisualObject okButton = getChildById(OK_BUTTON_ID);
        okButton.setStateFlag(0x1, enabled ? 1 : 0);
        okButton.draw();
    }

    /**
     * Java helper for the recovered `FUN_004D4A6D` horizontal-neighbor wiring in
     * HatServerBrowserDialogVisualObject::Initialize @0044A62F.
     */
    private static void linkButtonsHorizontally(CVisualObject button, CVisualObject leftNeighbor) {
        button.leftNeighbor = leftNeighbor;
        leftNeighbor.rightNeighbor = button;
    }

    /**
     * Native support extracted from HatServerBrowserDialogVisualObject::Initialize @0044A62F and
     * HatServerBrowserDialogVisualObject::OnMessage @0044AB6C for the current `TextListVisualObject` /
     * `PostSetupVisualObject` scrollbar mirror.
     */
    private void syncServerListScrollbar(HatServerEntriesTextListVisualObject serverList) {
        PostSetupVisualObject scrollbar = (PostSetupVisualObject) getChildById(SERVER_LIST_SCROLLBAR_ID);
        scrollbar.syncSelectionState(serverList.selectedRow, serverList.rows.size());
    }

    /**
     * Native support extracted from HatServerBrowserDialogVisualObject::Initialize @0044A62F and
     * RefreshHatServerRows @004E1B56 callers in CMainWindow::WindowProc @004852D8 and
     * HatServerBrowserDialogVisualObject::OnMessage @0044AB6C.
     */
    private static String readHatIp() {
        return Globals.mainWindow.getHatIp();
    }

    /**
     * Native support boundary for the `CMainWindow::connectionScratchState.pendingSessionConnectionString` write in
     * HatServerBrowserDialogVisualObject::OnMessage @0044AB6C.
     */
    private static void writeSelectedHatServerToken(String selectedHatServerToken) {
        Globals.mainWindow.setPendingConnectionString(selectedHatServerToken);
    }

    /**
     * Native: Global::RefreshHatServerRows @004E1B56.
     * Fully ported.
     */
    public static int refreshHatServerRows(String hatIp) {
        byte[] responseBytes;
        try {
            responseBytes = readHatServerResponse(normalizeHatServerUrl(hatIp));
        } catch (IOException | IllegalArgumentException e) {
            return 0;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return 0;
        }

        HAT_SERVER_ROWS.clear();
        String responseText = new String(responseBytes, StandardCharsets.ISO_8859_1);
        if (!responseText.contains(HAT_SERVER_COUNT_MARKER)) {
            return 0;
        }

        int countSeparator = responseText.indexOf('|');
        if (countSeparator < 0) {
            return 0;
        }

        int serverCount = nativeAtoi(responseText.substring(countSeparator + 1));
        int rowCursor = nextNewlineIndex(responseText, 0) + 1;
        rowCursor = nextNewlineIndex(responseText, rowCursor) + 1;
        rowCursor = nextNewlineIndex(responseText, rowCursor);
        for (int rowIndex = 0; rowIndex < serverCount; rowIndex++) {
            while (!isNativePrintable(responseBytes[rowCursor])) {
                rowCursor++;
            }

            int rowStart = rowCursor;
            int rowEnd = rowStart;
            while (rowEnd < responseBytes.length && Byte.toUnsignedInt(responseBytes[rowEnd]) > 0x1F) {
                rowEnd++;
            }
            HAT_SERVER_ROWS.add("|" + new String(responseBytes, rowStart, rowEnd - rowStart, StandardCharsets.ISO_8859_1));
            rowCursor = rowEnd;
        }
        return 1;
    }

    /**
     * Native support extracted from Global::RefreshHatServerRows @004E1B56.
     */
    private static byte[] readHatServerResponse(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", HAT_USER_AGENT)
                .GET()
                .build();
        try (HttpClient client = HttpClient.newBuilder()
                .followRedirects( HttpClient.Redirect.NORMAL)
                .build()) {
            return client.send(request, HttpResponse.BodyHandlers.ofByteArray())
                    .body();
        }
    }

    /**
     * Native support extracted from Global::RefreshHatServerRows @004E1B56.
     */
    private static String normalizeHatServerUrl(String hatIp) {
        StringBuilder normalized = new StringBuilder(hatIp.length());
        for (int index = 0; index < hatIp.length(); index++) {
            char ch = hatIp.charAt(index);
            normalized.append(ch == ' ' ? '+' : ch);
        }
        String normalizedUrl = normalized.toString();
        String prefix = normalizedUrl.substring(0, Math.min(HAT_HTTP_PREFIX.length(), normalizedUrl.length()))
                .toLowerCase();
        if (!HAT_HTTP_PREFIX.equals(prefix)) {
            normalizedUrl = HAT_HTTP_PREFIX + normalizedUrl;
        }
        return normalizedUrl;
    }

    /**
     * Native support extracted from Global::RefreshHatServerRows @004E1B56.
     */
    private static int nextNewlineIndex(String value, int fromIndex) {
        int newlineIndex = value.indexOf('\n', fromIndex);
        if (newlineIndex < 0) {
            throw new IllegalStateException("HAT server response is missing a row-header newline");
        }
        return newlineIndex;
    }

    /**
     * Native support extracted from the `isprint` loop in Global::RefreshHatServerRows @004E1B56.
     */
    private static boolean isNativePrintable(byte value) {
        int unsignedValue = Byte.toUnsignedInt(value);
        return unsignedValue >= 0x20 && unsignedValue <= 0x7E;
    }

    /**
     * Native support extracted from GetInt @00584400, called by Global::RefreshHatServerRows @004E1B56.
     */
    private static int nativeAtoi(String value) {
        int index = 0;
        while (index < value.length() && value.charAt(index) <= ' ') {
            index++;
        }

        int sign = 1;
        if (index < value.length()) {
            char signChar = value.charAt(index);
            if (signChar == '-') {
                sign = -1;
                index++;
            } else if (signChar == '+') {
                index++;
            }
        }

        int result = 0;
        while (index < value.length()) {
            int digit = value.charAt(index) - '0';
            if (digit < 0 || digit > 9) {
                break;
            }
            result = result * 10 + digit;
            index++;
        }
        return sign * result;
    }

    /**
     * Native support extracted from `CStringArray g_hatServerRows @00628290` reads in
     * HatServerBrowserDialogVisualObject::OnMessage @0044AB6C.
     */
    private static List<String> readHatServerRows() {
        return HAT_SERVER_ROWS;
    }
}
