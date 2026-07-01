package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.SaveFile;
import ua.millfreedom.rom2.model.SavedGameFiles;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palettes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static ua.millfreedom.rom2.model.enums.MessageCodes.DIALOG_OK;
import static ua.millfreedom.rom2.model.enums.MessageCodes.CANCEL_SAVE_LOAD_DIALOG;
import static ua.millfreedom.rom2.model.enums.MessageCodes.CONFIRM_SAVE_LOAD_DIALOG;
import static ua.millfreedom.rom2.model.enums.MessageCodes.DELETE_SAVED_GAME;
import static ua.millfreedom.rom2.model.enums.MessageCodes.HEADER_DIALOG_YES;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RETURN_TO_GAME;
import static ua.millfreedom.rom2.model.window.windowproc.handlers.CMainWindowWindowProcSupport.readMessageInt;
import static ua.millfreedom.rom2.text.DialogsText.CANCEL_1;
import static ua.millfreedom.rom2.text.DialogsText.CANCEL_27;
import static ua.millfreedom.rom2.text.DialogsText.CONFIRM_SELECTION_26;
import static ua.millfreedom.rom2.text.DialogsText.DELETE_157;
import static ua.millfreedom.rom2.text.DialogsText.DELETE_SELECTED_SAVED_GAME_158;
import static ua.millfreedom.rom2.text.DialogsText.LOAD_GAME_24;
import static ua.millfreedom.rom2.text.DialogsText.LOAD_SAVED_GAME_151;
import static ua.millfreedom.rom2.text.DialogsText.OK_0;
import static ua.millfreedom.rom2.text.DialogsText.SELECT_GAME_TO_LOAD_25;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.PatchText.ARE_YOU_SURE_YOU_WANT_TO_DELETE_GAME_50;
import static ua.millfreedom.rom2.text.PatchText.BLANK_51;
import static ua.millfreedom.rom2.text.TextTableId.DIALOGS;
import static ua.millfreedom.rom2.text.TextTableId.PATCH;

/**
 * Native class: LoadDialogVisualObject.
 * Purpose: centered save-load picker dialog bound to SaveFile storage.
 */
public class LoadDialogVisualObject extends CenteredDialogVisualObject {
    private static final int SAVED_GAMES_LIST_ID = 3;
    private static final int OK_BUTTON_ID = 4;
    private static final int CANCEL_BUTTON_ID = 5;
    private static final int DELETE_BUTTON_ID = 6;
    private static final int SAVED_GAMES_SCROLLBAR_ID = 10;
    private static final int DELETE_CONFIRM_BUTTON_LAYOUT_YES_NO = 4;

    public static final int NATIVE_SIZE = 0x98; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int OK_BUTTON_EXTRA_STATE_FLAG = 0x10;
    private static final int SAVE_FILE_MAGIC = 0x26677342;
    private static final int SAVE_FILE_TITLE_LENGTH = 0x100;

    //0x6c
    public final List<String> savedGameFileNames = new ArrayList<>();
    //0x80
    public final List<String> savedGameDetailsRows = new ArrayList<>();
    //0x68
    public SaveFile saveFile;
    //0x94
    public int selectedEntryIndex;

    /**
     * Native: LoadDialogVisualObject::LoadDialogVisualObject @0043DEAF.
     * Fully ported.
     */
    public LoadDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, Object handler, SaveFile saveFile) {
        super(id, xLeft, yTop, xRight, yBottom, handler);
        this.saveFile = saveFile;
        this.selectedEntryIndex = 0;
    }

    /**
     * vtbl +0x78: LoadDialogVisualObject::Initialize @0043DF48.
     * Fully ported.
     */
    @Override
    public void initialize() {
        CBitmapFont dialogFont = Globals.fonts.font1;
        int dialogWidth = cRect.width();

        DialogWindowVisualObject titleHeader = new DialogWindowVisualObject(
                1,
                0x28,
                0x20,
                dialogWidth - 0x28,
                0x38,
                get(DIALOGS, LOAD_SAVED_GAME_151),
                dialogFont,
                Palettes.grayDim,
                2
        );
        addChild(titleHeader);

        DialogWindowVisualObject listHeader = new DialogWindowVisualObject(
                2,
                0x28,
                0x44,
                dialogWidth - 0x28,
                0x5C,
                get(DIALOGS, LOAD_GAME_24),
                dialogFont,
                Palettes.grayDim,
                0
        );
        addChild(listHeader);

        SavedGamesTextListVisualObject savedGamesList = new SavedGamesTextListVisualObject(
                SAVED_GAMES_LIST_ID,
                0x28,
                0x5C,
                dialogWidth - 0x40,
                0x11C,
                dialogFont,
                Palettes.grayDim,
                Palettes.gray,
                SAVED_GAMES_SCROLLBAR_ID,
                get(DIALOGS, SELECT_GAME_TO_LOAD_25)
        );
        populateSavedGamesEntries(savedGamesList);

        CRect savedGamesListRect = savedGamesList.getRect();
        PostSetupVisualObject scrollBar = new PostSetupVisualObject(
                SAVED_GAMES_SCROLLBAR_ID,
                savedGamesListRect.right,
                savedGamesListRect.top,
                savedGamesListRect.right + 0x18,
                savedGamesListRect.bottom,
                null
        );
        addChild(scrollBar);
        addChild(savedGamesList);
        savedGamesList.gameDialogControls = listHeader;
        savedGamesList.setTooltipRows(savedGameDetailsRows);

        int buttonTop = savedGamesListRect.bottom + 0x18;
        int buttonBottom = buttonTop + 0x18;
        CRect okRect = new CRect((dialogWidth * 3) / 0x14, buttonTop, (dialogWidth * 7) / 0x14, buttonBottom);
        CRect deleteRect = new CRect((dialogWidth << 3) / 0x14, buttonTop, (dialogWidth * 0xC) / 0x14, buttonBottom);
        CRect cancelRect = new CRect((dialogWidth * 0xD) / 0x14, buttonTop, (dialogWidth * 0x11) / 0x14, buttonBottom);

        CommandButtonVisualObject okButton = new CommandButtonVisualObject(
                OK_BUTTON_ID,
                okRect,
                get(DIALOGS, OK_0),
                dialogFont,
                null,
                CONFIRM_SAVE_LOAD_DIALOG,
                0,
                get(DIALOGS, CONFIRM_SELECTION_26)
        );
        addChild(okButton);
        okButton.setStateFlag(OK_BUTTON_EXTRA_STATE_FLAG, 1);
        if (savedGameFileNames.isEmpty()) {
            okButton.setStateFlag(1, 0);
        }

        CommandButtonVisualObject deleteButton = new CommandButtonVisualObject(
                DELETE_BUTTON_ID,
                deleteRect,
                get(DIALOGS, DELETE_157),
                dialogFont,
                null,
                DELETE_SAVED_GAME,
                0,
                get(DIALOGS, DELETE_SELECTED_SAVED_GAME_158)
        );
        addChild(deleteButton);

        CommandButtonVisualObject cancelButton = new CommandButtonVisualObject(
                CANCEL_BUTTON_ID,
                cancelRect,
                get(DIALOGS, CANCEL_1),
                dialogFont,
                null,
                CANCEL_SAVE_LOAD_DIALOG,
                0,
                get(DIALOGS, CANCEL_27)
        );
        addChild(cancelButton);

        okButton.rightNeighbor = cancelButton;
        cancelButton.leftNeighbor = okButton;
        okButton.leftNeighbor = deleteButton;
        deleteButton.rightNeighbor = okButton;
        okButton.upNeighbor = savedGamesList;
        savedGamesList.downNeighbor = okButton;
        cancelButton.upNeighbor = savedGamesList;
        savedGamesList.downNeighbor = cancelButton;
    }

    /**
     * vtbl +0x48: LoadDialogVisualObject::OnMessage @0043E7F0.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        int w = readMessageInt(wParam);
        int l = readMessageInt(lParam);
        return switch (msg) {
            case TEXT_LIST_SELECTION_DBLCLK, CONFIRM_SAVE_LOAD_DIALOG -> handleConfirmSelection();
            case TEXT_LIST_SELECTION_CHANGED -> {
                if (w == SAVED_GAMES_LIST_ID) {
                    selectedEntryIndex = l;
                    yield 1;
                }
                yield super.onMessage(msg, wParam, lParam);
            }
            case DELETE_SAVED_GAME -> {
                handleDeleteSavedGame();
                yield 1;
            }
            case CANCEL_SAVE_LOAD_DIALOG -> {
                super.onMessage(RETURN_TO_GAME, 0, 0);
                yield 1;
            }
            default -> super.onMessage(msg, wParam, lParam);
        };
    }

    /**
     * Native helper path: LoadDialogVisualObject::Initialize @0043DF48 through the shared save-file enumeration helper @0043DC54.
     */
    private void populateSavedGamesEntries(SavedGamesTextListVisualObject savedGamesList) {
        savedGameFileNames.clear();
        savedGameDetailsRows.clear();

        for (Path savedGamePath : collectSavedGameFiles()) {
            String savedGameTitle = readSavedGameTitle(savedGamePath);
            if (savedGameTitle == null) {
                continue;
            }

            savedGamesList.addRow(savedGameTitle);
            savedGameFileNames.add(savedGamePath.getFileName().toString());
            savedGameDetailsRows.add(formatSavedGameDetailsRow(savedGamePath, savedGameTitle));
        }
    }

    /**
     * Native helper path: LoadDialogVisualObject::Initialize @0043DF48 through the shared save-file enumeration helper @0043DC54.
     */
    private List<Path> collectSavedGameFiles() {
        return SavedGameFiles.collectSavedGameFiles(true);
    }

    /**
     * Native save-header parse path inside LoadDialogVisualObject::Initialize @0043DF48.
     */
    private String readSavedGameTitle(Path saveFilePath) {
        try (SeekableByteChannel saveFileChannel = Files.newByteChannel(saveFilePath, StandardOpenOption.READ)) {
            ByteBuffer header = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            if (saveFileChannel.read(header) != header.capacity()) {
                return null;
            }

            header.flip();
            if (header.getInt() != SAVE_FILE_MAGIC) {
                return null;
            }

            int titleOffset = header.getInt();
            if (titleOffset < 0 || titleOffset > saveFileChannel.size()) {
                return null;
            }

            saveFileChannel.position(titleOffset);
            ByteBuffer titleBytes = ByteBuffer.allocate(SAVE_FILE_TITLE_LENGTH);
            int titleLength = saveFileChannel.read(titleBytes);
            if (titleLength <= 0) {
                return "";
            }

            byte[] rawTitle = titleBytes.array();
            int zeroTerminatedLength = 0;
            while (zeroTerminatedLength < titleLength && rawTitle[zeroTerminatedLength] != 0) {
                zeroTerminatedLength += 1;
            }
            return SavedGameFiles.SAVE_FILE_CHARSET.decode(ByteBuffer.wrap(rawTitle, 0, zeroTerminatedLength)).toString();
        } catch (IOException ignored) {
            return null;
        }
    }

    /**
     * Native timestamp formatting block inside LoadDialogVisualObject::Initialize @0043DF48.
     */
    private String formatSavedGameDetailsRow(Path saveFilePath, String savedGameTitle) {
        LocalDateTime lastModifiedTime = LocalDateTime.ofInstant(
                SavedGameFiles.getLastModifiedTime(saveFilePath).toInstant(),
                ZoneId.systemDefault()
        );
        return String.format(
                Locale.ROOT,
                "%s  %02d/%02d/%d %02d:%02d:%02d",
                savedGameTitle,
                lastModifiedTime.getDayOfMonth(),
                lastModifiedTime.getMonthValue(),
                lastModifiedTime.getYear(),
                lastModifiedTime.getHour(),
                lastModifiedTime.getMinute(),
                lastModifiedTime.getSecond()
        );
    }

    /**
     * Native support branch inside LoadDialogVisualObject::OnMessage @0043E7F0.
     */
    private int handleConfirmSelection() {
        if (selectedEntryIndex < 0) {
            return super.onMessage(RETURN_TO_GAME, 0, 0);
        }

        SavedGamesTextListVisualObject savedGamesList =
                (SavedGamesTextListVisualObject) getChildById(SAVED_GAMES_LIST_ID);
        saveFile.title = savedGamesList.getRowTextAtClampedIndex(selectedEntryIndex);
        saveFile.filename = savedGameFileNames.get(selectedEntryIndex);
        return super.onMessage(DIALOG_OK, 0, 0);
    }

    /**
     * Native support branch inside LoadDialogVisualObject::OnMessage @0043E7F0.
     */
    private void handleDeleteSavedGame() {
        SavedGamesTextListVisualObject savedGamesList =
                (SavedGamesTextListVisualObject) getChildById(SAVED_GAMES_LIST_ID);
        int selectedSavedGameIndex = savedGamesList.getSelectedRow();
        if (selectedSavedGameIndex < 0) {
            return;
        }

        HeaderDialogVariantVisualObject confirmDeleteDialog = new HeaderDialogVariantVisualObject(
                1,
                0x40,
                100,
                0x17C,
                0x252,
                get(PATCH, ARE_YOU_SURE_YOU_WANT_TO_DELETE_GAME_50)
                        + savedGamesList.rows.get(selectedSavedGameIndex)
                        + get(PATCH, BLANK_51),
                null,
                DELETE_CONFIRM_BUTTON_LAYOUT_YES_NO
        );
        MessageCodes dialogResult = Globals.mainWindow.showDialogAndAwaitResult(confirmDeleteDialog);
        if (dialogResult != HEADER_DIALOG_YES) {
            return;
        }

        deleteSavedGameFile(savedGameFileNames.get(selectedSavedGameIndex));
        savedGameFileNames.remove(selectedSavedGameIndex);
        savedGameDetailsRows.remove(selectedSavedGameIndex);
        savedGamesList.removeRowAndAdjustSelection(selectedSavedGameIndex);
        savedGamesList.setSelectedRow(selectedSavedGameIndex);
        if (savedGameFileNames.isEmpty()) {
            disableChildAndDraw(OK_BUTTON_ID);
            disableChildAndDraw(DELETE_BUTTON_ID);
        }
    }

    /**
     * Native file-delete branch inside LoadDialogVisualObject::OnMessage @0043E7F0.
     */
    private static void deleteSavedGameFile(String saveFileName) {
        try {
            Files.deleteIfExists(SavedGameFiles.resolvePath(saveFileName));
        } catch (IOException ignored) {
        }
    }

    /**
     * Native button-state branch inside LoadDialogVisualObject::OnMessage @0043E7F0.
     */
    private void disableChildAndDraw(int childId) {
        CVisualObject child = getChildById(childId);
        child.setStateFlag(1, 0);
        child.draw();
    }

}
