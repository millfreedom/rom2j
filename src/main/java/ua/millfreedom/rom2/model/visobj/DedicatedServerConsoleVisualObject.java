package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.net.CBufferManager;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.ClientTrafficStats;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.CServerApp;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.action.ChatTextAction;
import ua.millfreedom.rom2.model.control.CGameListControl;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.text.PatchText;

import java.util.Locale;
import java.util.Map;

import static ua.millfreedom.rom2.model.enums.MessageCodes.DIALOG_OK;
import static ua.millfreedom.rom2.model.enums.MessageCodes.EXIT_MAP;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RETURN_TO_GAME;
import static ua.millfreedom.rom2.model.enums.MessageCodes.TEXT_LIST_SELECTION_CHANGED;
import static ua.millfreedom.rom2.model.enums.MessageCodes.WM_KEYDOWN;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_RETURN;
import static ua.millfreedom.rom2.model.window.windowproc.handlers.CMainWindowWindowProcSupport.readMessageInt;
import static ua.millfreedom.rom2.text.DialogsText.CHANGE_MAP_42;
import static ua.millfreedom.rom2.text.DialogsText.STOP_14;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.PatchText.AVG_62;
import static ua.millfreedom.rom2.text.PatchText.BPS_61;
import static ua.millfreedom.rom2.text.PatchText.ID_58;
import static ua.millfreedom.rom2.text.PatchText.KEEP_SAVED_CHARACTERS_ON_SERVER_56;
import static ua.millfreedom.rom2.text.PatchText.KILLS_64;
import static ua.millfreedom.rom2.text.PatchText.MAX_63;
import static ua.millfreedom.rom2.text.PatchText.ONLINE_60;
import static ua.millfreedom.rom2.text.PatchText.PK_65;
import static ua.millfreedom.rom2.text.PatchText.PLAYER_NAME_59;
import static ua.millfreedom.rom2.text.PatchText.PLAYERS_D_MONSTERS_D_BUILDINGS_D_CORPSES_D_SACKS_D_CPU_D_CPU_AVG_66;
import static ua.millfreedom.rom2.text.PatchText.RAGE_OF_MAGES_2_NECROMANCER_SERVER_1_05_57;

/**
 * Native class: DedicatedServerConsoleVisualObject (vtbl @0x005CCEF0).
 * Purpose: dedicated-server console overlay with message log, command input, and server control buttons.
 */
public class DedicatedServerConsoleVisualObject extends HandlerVisualObject {
    private static final int STOP_BUTTON_ID = 99;
    private static final int CHANGE_MAP_BUTTON_ID = 100;
    private static final int KEEP_SAVED_CHARACTERS_LIST_ID = 101;
    private static final int CONSOLE_INPUT_ID = 104;
    private static final short BLACK_565 = 0;
    private static final short WHITE_565 = (short) 0xFFFF;
    private static final int SERVER_TABLE_TOP = 0x10;
    private static final int SERVER_TABLE_HEADER_Y = 0x14;
    private static final int SERVER_TABLE_BOTTOM = 200;
    private static final int SERVER_TABLE_SUMMARY_Y = SERVER_TABLE_BOTTOM + 4;
    private static final int SERVER_TABLE_FIRST_ROW_Y = 0x24;
    private static final int SERVER_TABLE_ROW_STEP = 10;
    private static final int SERVER_TABLE_VISIBLE_INDEX_LIMIT = 0x10;
    private static final int ONLINE_SECONDS_PER_HOUR = 0xE10;
    private static final int ONLINE_SECONDS_PER_MINUTE = 0x3C;
    private static final int[] SERVER_TABLE_VERTICAL_LINES = {
            0, 0x18, 0x78, 0xC0, 0xF0, 0x120, 0x150, 0x180
    };
    private static final String DISCONNECT_SERVER_COMMAND = "disconnect";
    private static final String CURSE_SERVER_COMMAND = "curse";
    private static final int DISCONNECT_COMMAND_ARGUMENT_OFFSET = 0x0B;
    private static final int CURSE_COMMAND_ARGUMENT_OFFSET = 0x06;
    private static final int ALL_UNIT_UPDATE_FLAGS = -1;
    private static final int SERVER_CURSE_EQUIPMENT_MASK = 0x0FFB;
    private static final int SERVER_CURSE_BODY = 10;
    private static final int SERVER_CURSE_MIND = 10;
    private static final int SERVER_CURSE_REACTION = 10;
    private static final int SERVER_CURSE_SPIRIT = 1;
    public static final int NATIVE_SIZE = 0x78; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    //0x68
    public CGameListControl consoleMessageListControl;
    //0x6c
    public int field0x6c;
    //0x70
    public int field0x70;
    //0x74
    public int playerTableScrollOffset;

    /**
     * Native: DedicatedServerConsoleVisualObject::DedicatedServerConsoleVisualObject @0044CDDC.
     * Fully ported. Native reads the parent id from `this+0x04` before superclass construction; Java keeps
     * the explicit caller id from CMainWindow::runSessionBootstrap @0048C9C6.
     */
    public DedicatedServerConsoleVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            CGameListControl consoleMessageListControl
    ) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        this.consoleMessageListControl = consoleMessageListControl;
    }

    /**
     * vtbl +0x78: DedicatedServerConsoleVisualObject::Initialize @0044C27C.
     * Fully ported.
     */
    @Override
    public void initialize() {
        playerTableScrollOffset = 0;

        CBitmapFont font2 = Globals.fonts.font2;
        int screenWidth = cRect.width();
        int screenHeight = cRect.height();

        addChild(new CommandButtonVisualObject(
                STOP_BUTTON_ID,
                0x140,
                screenHeight - 0x1E,
                0x1A4,
                screenHeight - 0x0C,
                get(STOP_14),
                font2,
                null,
                DIALOG_OK,
                0,
                null
        ));
        addChild(new CommandButtonVisualObject(
                CHANGE_MAP_BUTTON_ID,
                0x1B8,
                screenHeight - 0x1E,
                0x21C,
                screenHeight - 0x0C,
                get(CHANGE_MAP_42),
                font2,
                null,
                RETURN_TO_GAME,
                0,
                null
        ));

        StringListVariantCVisualObject keepCharactersList = new StringListVariantCVisualObject(
                KEEP_SAVED_CHARACTERS_LIST_ID,
                0,
                screenHeight - 0x1C,
                300,
                screenHeight - 0x0C,
                font2,
                null,
                null
        );
        keepCharactersList.addRow(get(KEEP_SAVED_CHARACTERS_ON_SERVER_56));
        addChild(keepCharactersList);

        int keepSavedCharactersSelection = 1;
        if (shouldForceRemoteServerMode()) {
            Globals.gameServer.keepSavedCharactersOnServer = 0;
        }
        if (Globals.gameServer.keepSavedCharactersOnServer != 0) {
            keepSavedCharactersSelection = 0;
        }
        keepCharactersList.setValue(keepSavedCharactersSelection);

        addChild(new StaticTextVisualObject(
                CONSOLE_INPUT_ID,
                4,
                screenHeight - 0x38,
                screenWidth - 4,
                screenHeight - 0x28,
                font2,
                Palettes.gray,
                null
        ));
    }

    /**
     * vtbl +0x2C: DedicatedServerConsoleVisualObject::Update @0044C553.
     * Fully ported.
     */
    @Override
    public void update() {
        Globals.renderer.lockSurface();
        try {
            Globals.renderer.fillScreenRect(
                    0,
                    0,
                    Globals.screenRect.right,
                    Globals.screenRect.bottom,
                    BLACK_565
            );
            super.update();
            Globals.fonts.font2.drawTextShadowed(
                    0,
                    0,
                    get(RAGE_OF_MAGES_2_NECROMANCER_SERVER_1_05_57),
                    TextAlign.DEFAULT.mask,
                    Palettes.gray,
                    1
            );
            drawDedicatedServerStats();
            consoleMessageListControl.draw();
        } finally {
            Globals.renderer.unlockSurface();
        }
    }

    /**
     * vtbl +0x30: DedicatedServerConsoleVisualObject::RenderSelf @0044C521.
     * Fully ported.
     */
    @Override
    public void renderSelf(CRect clipRect) {
        Globals.renderer.fillScreenRect(clipRect.left, clipRect.top, clipRect.right, clipRect.bottom, BLACK_565);
    }

    /**
     * vtbl +0x48: DedicatedServerConsoleVisualObject::OnMessage @0044D135.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        int w = readMessageInt(wParam);
        int l = readMessageInt(lParam);
        if (msg == WM_KEYDOWN) {
            onKeyDown(w);
            return 1;
        }
        if (msg == RETURN_TO_GAME) {
            super.onMessage(RETURN_TO_GAME, 0, 0);
            Globals.mainWindow.postMessage(EXIT_MAP, 0, 0);
            return 1;
        }
        if (msg == TEXT_LIST_SELECTION_CHANGED && w == KEEP_SAVED_CHARACTERS_LIST_ID) {
            Globals.gameServer.keepSavedCharactersOnServer = l == 0 ? 1 : 0;
            return 1;
        }
        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * vtbl +0x6C: DedicatedServerConsoleVisualObject::OnKeyDown @0044D04B.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        StaticTextVisualObject consoleInput = (StaticTextVisualObject) getChildById(CONSOLE_INPUT_ID);
        if (nChar == VK_RETURN) {
            ChatTextAction action = ChatTextAction.global;
            action.ID.set(ChatTextAction.ACTION_ID);
            action.firstPayloadDword.set(0);
            action.playerID.set(0);
            action.text.set(readConsoleInputText(consoleInput));
            String submittedText = action.text.get();
            if (!submittedText.isEmpty() && submittedText.charAt(0) == '#') {
                sendServerCommand(submittedText.substring(1));
            } else {
                echoConsoleMessage(submittedText);
                CServerApp.sendGameAction(action);
            }
            consoleInput.setInputText("");
            return 1;
        }
        return consoleInput.onKeyDown(nChar);
    }

    /**
     * Native support: StaticTextVisualObject::CopyTextToBuffer call site in DedicatedServerConsoleVisualObject::OnKeyDown @0044D04B.
     * Fully ported.
     */
    private static String readConsoleInputText(StaticTextVisualObject consoleInput) {
        StringBuilder text = new StringBuilder();
        consoleInput.copyTextToBuffer(text);
        return text.toString();
    }

    /**
     * Native support: `_saveonserver` / `_internetserver` command-line checks in DedicatedServerConsoleVisualObject::Initialize @0044C27C.
     * Fully ported.
     */
    private static boolean shouldForceRemoteServerMode() {
        return Globals.commandLine.contains("_saveonserver") || Globals.commandLine.contains("_internetserver");
    }

    /**
     * Native support extracted from DedicatedServerConsoleVisualObject::Update @0044C553 stats-table draw body.
     * Fully ported.
     */
    private void drawDedicatedServerStats() {
        drawServerTableSeparators();

        int nativeIndex = 0;
        int rowY = SERVER_TABLE_FIRST_ROW_Y;
        for (Map.Entry<Integer, ClientTrafficStats> entry : CServerApp.localClientTrafficStatsEntries()) {
            if (nativeIndex >= playerTableScrollOffset + SERVER_TABLE_VISIBLE_INDEX_LIMIT) {
                break;
            }
            if (nativeIndex < playerTableScrollOffset) {
                nativeIndex++;
                continue;
            }

            CBufferManager client = CServerApp.getLocalClientByMaskedSocketId(entry.getKey() >>> 16);
            Player player = Globals.gameServer.playerList.getPlayerById(client.GetNetId());
            if (player == null) {
                nativeIndex++;
                continue;
            }

            Palette16 playerPalette = Palettes.p16.get(((short) player.playerId) & 0x0F);
            ClientTrafficStats stats = entry.getValue();
            int averageBytes = 0;
            if (stats.sampleCount != 0) {
                averageBytes = stats.totalBytes / stats.sampleCount;
            }

            if (nativeIndex == playerTableScrollOffset) {
                drawServerTableVerticalLines();
            }

            drawServerTableColumn(ID_58, 0x0C, rowY, Integer.toString((short) player.playerId), playerPalette);
            drawServerTableColumn(PLAYER_NAME_59, 0x48, rowY, player.name, playerPalette);
            drawServerTableColumn(ONLINE_60, 0x9C, rowY, formatOnlineDuration(stats.sampleCount), playerPalette);
            drawServerTableColumn(BPS_61, 0xD8, rowY, Integer.toString(stats.lastIntervalBytes), playerPalette);
            drawServerTableColumn(AVG_62, 0x108, rowY, Integer.toString(averageBytes), playerPalette);
            drawServerTableColumn(MAX_63, 0x138, rowY, Integer.toString(stats.peakIntervalBytes), playerPalette);
            drawServerTableColumn(KILLS_64, 0x168, rowY, Integer.toString(player.creatureKillCount), playerPalette);
            drawServerTableColumn(PK_65, 0x18C, rowY, Integer.toString(player.playerKillCount), playerPalette);

            rowY += SERVER_TABLE_ROW_STEP;
        }

        drawDedicatedServerSummary();
    }

    /**
     * Native support extracted from DedicatedServerConsoleVisualObject::Update @0044C553 separator fills.
     * Fully ported.
     */
    private void drawServerTableSeparators() {
        Globals.renderer.fillScreenRect(0, SERVER_TABLE_TOP, cRect.right, SERVER_TABLE_TOP + 1, WHITE_565);
        Globals.renderer.fillScreenRect(0, 0x20, cRect.right, 0x21, WHITE_565);
        Globals.renderer.fillScreenRect(0, SERVER_TABLE_BOTTOM, cRect.right, SERVER_TABLE_BOTTOM + 1, WHITE_565);
        Globals.renderer.fillScreenRect(0, 0xD8, cRect.right, 0xD9, WHITE_565);
    }

    /**
     * Native support extracted from DedicatedServerConsoleVisualObject::Update @0044C553 vertical table borders.
     * Fully ported.
     */
    private void drawServerTableVerticalLines() {
        for (int x : SERVER_TABLE_VERTICAL_LINES) {
            Globals.renderer.drawLine(x, SERVER_TABLE_TOP, x, SERVER_TABLE_BOTTOM, WHITE_565);
        }
        int rightBorderX = cRect.right - 1;
        Globals.renderer.drawLine(rightBorderX, SERVER_TABLE_TOP, rightBorderX, SERVER_TABLE_BOTTOM, WHITE_565);
    }

    /**
     * Native support extracted from DedicatedServerConsoleVisualObject::Update @0044C553 table header/value draw calls.
     * Fully ported.
     */
    private static void drawServerTableColumn(
            PatchText header,
            int x,
            int rowY,
            String value,
            Palette16 valuePalette
    ) {
        Globals.fonts.font2.drawTextInternal(x, SERVER_TABLE_HEADER_Y, get(header), TextAlign.CENTER.mask, Palettes.gray);
        Globals.fonts.font2.drawTextInternal(x, rowY, value, TextAlign.CENTER.mask, valuePalette);
    }

    /**
     * Native support extracted from DedicatedServerConsoleVisualObject::Update @0044C553 `%d:%02d:%02d` CString::Format.
     * Fully ported.
     */
    private static String formatOnlineDuration(int sampleCount) {
        return String.format(
                Locale.ROOT,
                "%d:%02d:%02d",
                sampleCount / ONLINE_SECONDS_PER_HOUR,
                (sampleCount % ONLINE_SECONDS_PER_HOUR) / ONLINE_SECONDS_PER_MINUTE,
                sampleCount % ONLINE_SECONDS_PER_MINUTE
        );
    }

    /**
     * Native support extracted from DedicatedServerConsoleVisualObject::Update @0044C553 server summary draw branch.
     * Fully ported.
     */
    private static void drawDedicatedServerSummary() {
        int averageCpuPct = 0;
        if (Globals.gameServer.cpuUsageSampleCount != 0) {
            averageCpuPct = (Globals.gameServer.cpuUsageTenthPctSum / Globals.gameServer.cpuUsageSampleCount) / 10;
        }
        if (Globals.gameServer.objectLists.sacks != null) {
            String summary = String.format(
                    Locale.ROOT,
                    get(PLAYERS_D_MONSTERS_D_BUILDINGS_D_CORPSES_D_SACKS_D_CPU_D_CPU_AVG_66),
                    Globals.gameServer.playerList.getPlayersCount(),
                    Globals.gameServer.activeUnits.size(),
                    Globals.gameServer.objectLists.buildings.size(),
                    Globals.gameServer.objectLists.corpses.size(),
                    Globals.gameServer.objectLists.sacks.size(),
                    Globals.gameServer.cpuUsageTenthPct / 10,
                    averageCpuPct,
                    Globals.mainWindow.map_.toString()
            );
            Globals.fonts.font2.drawTextInternal(
                    0,
                    SERVER_TABLE_SUMMARY_Y,
                    summary,
                    TextAlign.DEFAULT.mask,
                    Palettes.gray
            );
        }
    }

    /**
     * Native support: Global::SendServerCommand @0044CE41.
     * Fully ported.
     */
    public static void sendServerCommand(String commandText) {
        String normalizedCommand = commandText.toLowerCase(Locale.ROOT);
        if (normalizedCommand.startsWith(DISCONNECT_SERVER_COMMAND)) {
            int playerId = parseServerCommandInt(nativeMid(
                    normalizedCommand,
                    DISCONNECT_COMMAND_ARGUMENT_OFFSET
            ));
            if (Globals.gameServer.playerList.getPlayerById(playerId) != null) {
                CServerApp.removeLocalClientByNetId(playerId);
            }
            return;
        }
        if (normalizedCommand.startsWith(CURSE_SERVER_COMMAND)) {
            int playerId = parseServerCommandInt(nativeMid(normalizedCommand, CURSE_COMMAND_ARGUMENT_OFFSET));
            Player player = Globals.gameServer.playerList.getPlayerById(playerId);
            if (player != null && player.controlledUnit != null) {
                Unit controlledUnit = (Unit) player.controlledUnit;
                controlledUnit.skillsTotalBonusPermille = 0;
                controlledUnit.m_nBody = SERVER_CURSE_BODY;
                controlledUnit.m_nMind = SERVER_CURSE_MIND;
                controlledUnit.m_nReaction = SERVER_CURSE_REACTION;
                controlledUnit.m_nSpirit = SERVER_CURSE_SPIRIT;
                controlledUnit.recalculateDerivedStats();
                CServerApp.netUpdate(
                        controlledUnit,
                        null,
                        ALL_UNIT_UPDATE_FLAGS,
                        SERVER_CURSE_EQUIPMENT_MASK,
                        0,
                        0
                );
            }
        }
    }

    /**
     * Native support extracted from CString::Mid call sites in Global::SendServerCommand @0044CE41.
     * Fully ported.
     */
    private static String nativeMid(String text, int offset) {
        return text.length() > offset ? text.substring(offset) : "";
    }

    /**
     * Native support extracted from GetInt call sites in Global::SendServerCommand @0044CE41.
     * Fully ported.
     */
    private static int parseServerCommandInt(String text) {
        int index = 0;
        while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
            index++;
        }
        int sign = 1;
        if (index < text.length()) {
            char signChar = text.charAt(index);
            if (signChar == '-') {
                sign = -1;
                index++;
            } else if (signChar == '+') {
                index++;
            }
        }
        int value = 0;
        boolean hasDigits = false;
        while (index < text.length()) {
            char digitChar = text.charAt(index);
            if (digitChar < '0' || digitChar > '9') {
                break;
            }
            hasDigits = true;
            value = value * 10 + digitChar - '0';
            index++;
        }
        return hasDigits ? value * sign : 0;
    }

    /**
     * Native support: local console echo `PushMessage` call site @0043A0A8 in DedicatedServerConsoleVisualObject::OnKeyDown @0044D04B.
     * Fully ported.
     */
    private void echoConsoleMessage(String submittedText) {
        Globals.gameServer.pushMessage(submittedText);
    }

}
