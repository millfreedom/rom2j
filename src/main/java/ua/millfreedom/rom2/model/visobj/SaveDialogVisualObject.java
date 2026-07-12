package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.GameCharsets;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static ua.millfreedom.rom2.model.enums.MessageCodes.DIALOG_OK;
import static ua.millfreedom.rom2.model.enums.MessageCodes.CANCEL_SAVE_LOAD_DIALOG;
import static ua.millfreedom.rom2.model.enums.MessageCodes.CONFIRM_SAVE_LOAD_DIALOG;
import static ua.millfreedom.rom2.model.enums.MessageCodes.DELETE_SAVED_GAME;
import static ua.millfreedom.rom2.model.enums.MessageCodes.HEADER_DIALOG_YES;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RETURN_TO_GAME;
import static ua.millfreedom.rom2.model.window.windowproc.handlers.CMainWindowWindowProcSupport.readMessageInt;
import static ua.millfreedom.rom2.text.PatchText.ARE_YOU_SURE_YOU_WANT_TO_DELETE_GAME_50;
import static ua.millfreedom.rom2.text.PatchText.BLANK_51;
import static ua.millfreedom.rom2.text.DialogsText.CANCEL_1;
import static ua.millfreedom.rom2.text.DialogsText.CANCLE_33;
import static ua.millfreedom.rom2.text.DialogsText.CONFIRM_SELECTION_32;
import static ua.millfreedom.rom2.text.DialogsText.DELETE_157;
import static ua.millfreedom.rom2.text.DialogsText.DELETE_SELECTED_SAVED_GAME_158;
import static ua.millfreedom.rom2.text.DialogsText.EMPTY_SLOT_28;
import static ua.millfreedom.rom2.text.DialogsText.ENTER_A_NAME_FOR_THE_SAVED_GAME_30;
import static ua.millfreedom.rom2.text.DialogsText.OK_0;
import static ua.millfreedom.rom2.text.DialogsText.SAVED_GAMES_31;
import static ua.millfreedom.rom2.text.DialogsText.SAVED_GAMES_144;
import static ua.millfreedom.rom2.text.DialogsText.SAVE_GAME_29;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.TextTableId.DIALOGS;
import static ua.millfreedom.rom2.text.TextTableId.PATCH;

/**
 * Native class: SaveDialogVisualObject.
 * Purpose: centered save-game chooser with writable slot name list.
 */
public class SaveDialogVisualObject extends CenteredDialogVisualObject {
    private static final int SAVE_NAME_INPUT_ID = 1;
    private static final int SAVED_GAMES_LIST_ID = 3;
    private static final int OK_BUTTON_ID = 4;
    private static final int CANCEL_BUTTON_ID = 5;
    private static final int DELETE_BUTTON_ID = 6;
    private static final int SAVED_GAMES_SCROLLBAR_ID = 10;
    private static final int DELETE_CONFIRM_BUTTON_LAYOUT_YES_NO = 4;

    public static final int NATIVE_SIZE = 0x9C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int OK_BUTTON_EXTRA_STATE_FLAG = 0x10;
    private static final int SAVE_FILE_MAGIC = 0x26677342;
    private static final int SAVE_FILE_TITLE_LENGTH = 0x100;
    private static final String SAVE_FILE_PATTERN = "game%04d.sav";

    //0x6c
    public final List<String> savedGameFileNames = new ArrayList<>();
    //0x80
    public final List<String> savedGameDetailsRows = new ArrayList<>();
    //0x68
    public SaveFile saveFile;
    //0x94
    public int selectedEntryIndex;
    //0x98
    public String emptySlotLabel;

    /**
     * Native: SaveDialogVisualObject::SaveDialogVisualObject @0043EBF6.
     * Fully ported.
     */
    public SaveDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, Object handler, SaveFile saveFile) {
        super(id, xLeft, yTop, xRight, yBottom, handler);
        this.saveFile = saveFile;
        this.selectedEntryIndex = 0;
        this.emptySlotLabel = get(DIALOGS, EMPTY_SLOT_28);
    }

    /**
     * vtbl +0x78: SaveDialogVisualObject::Initialize @0043ECE0.
     * Fully ported.
     */
    @Override
    public void initialize() {
        CBitmapFont dialogFont = Globals.fonts.font1;
        int dialogWidth = cRect.width();

        DialogWindowVisualObject titleHeader = new DialogWindowVisualObject(
                2,
                0x28,
                0x20,
                dialogWidth - 0x28,
                0x38,
                get(DIALOGS, SAVE_GAME_29),
                dialogFont,
                Palettes.grayDim,
                2
        );
        addChild(titleHeader);

        DialogWindowVisualObject savedGamesHeader = new DialogWindowVisualObject(
                0,
                0x28,
                0x68,
                dialogWidth - 0x28,
                0x80,
                get(DIALOGS, SAVED_GAMES_144),
                dialogFont,
                Palettes.grayDim,
                0
        );
        addChild(savedGamesHeader);

        StaticTextVisualObject saveNameInput = new StaticTextVisualObject(
                SAVE_NAME_INPUT_ID,
                0x28,
                0x44,
                dialogWidth - 0x28,
                0x5C,
                dialogFont,
                Palettes.grayDim,
                get(DIALOGS, ENTER_A_NAME_FOR_THE_SAVED_GAME_30)
        );
        addChild(saveNameInput);

        SavedGamesTextListVisualObject savedGamesList = new SavedGamesTextListVisualObject(
                SAVED_GAMES_LIST_ID,
                0x28,
                0x80,
                dialogWidth - 0x40,
                0x110,
                dialogFont,
                Palettes.grayDim,
                null,
                SAVED_GAMES_SCROLLBAR_ID,
                get(DIALOGS, SAVED_GAMES_31)
        );
        saveNameInput.downNeighbor = savedGamesList;
        savedGamesList.upNeighbor = saveNameInput;
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
        savedGamesList.gameDialogControls = savedGamesHeader;
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
                get(DIALOGS, CONFIRM_SELECTION_32)
        );
        addChild(okButton);
        okButton.setStateFlag(OK_BUTTON_EXTRA_STATE_FLAG, 1);

        CommandButtonRuntimeVariantVisualObject deleteButton = new CommandButtonRuntimeVariantVisualObject(
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

        CommandButtonRuntimeVariantVisualObject cancelButton = new CommandButtonRuntimeVariantVisualObject(
                CANCEL_BUTTON_ID,
                cancelRect,
                get(DIALOGS, CANCEL_1),
                dialogFont,
                null,
                CANCEL_SAVE_LOAD_DIALOG,
                0,
                get(DIALOGS, CANCLE_33)
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

        refreshOkButtonEnabledState();
    }

    /**
     * vtbl +0x48: SaveDialogVisualObject::OnMessage @0043F8DA.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        int w = readMessageInt(wParam);
        int l = readMessageInt(lParam);
        return switch (msg) {
            case TEXT_LIST_SELECTION_DBLCLK, CONFIRM_SAVE_LOAD_DIALOG -> handleConfirmSave();
            case TEXT_LIST_SELECTION_CHANGED -> {
                if (w == SAVED_GAMES_LIST_ID) {
                    handleSavedGameSelectionChanged(l);
                    yield 1;
                }
                if (w == SAVE_NAME_INPUT_ID) {
                    refreshOkButtonEnabledState();
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
     * Native helper path: SaveDialogVisualObject::Initialize @0043ECE0 through the shared save-file enumeration helper @0043DC54.
     */
    private void populateSavedGamesEntries(SavedGamesTextListVisualObject savedGamesList) {
        savedGameFileNames.clear();
        savedGameDetailsRows.clear();

        savedGamesList.addRow(emptySlotLabel);
        savedGameFileNames.add("");
        savedGameDetailsRows.add("");

        for (Path savedGamePath : collectSavedGameFiles()) {
            String savedGameTitle = readSavedGameTitle(savedGamePath);
            if (savedGameTitle == null) {
                continue;
            }

            savedGamesList.addRow(savedGameTitle);
            savedGameFileNames.add(savedGamePath.getFileName().toString());
            savedGameDetailsRows.add(formatSavedGameDetailsRow(savedGamePath, savedGameTitle));
        }

        String nextSaveFileName = formatSaveFileName(resolveNextAvailableSaveSlotNumber());
        savedGameFileNames.set(0, nextSaveFileName);
    }

    /**
     * Native helper path: SaveDialogVisualObject::Initialize @0043ECE0 through the shared save-file enumeration helper @0043DC54.
     */
    private List<Path> collectSavedGameFiles() {
        return SavedGameFiles.collectSavedGameFiles(false);
    }

    /**
     * Native save-header parse path inside SaveDialogVisualObject::Initialize @0043ECE0.
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
            return GameCharsets.GAME_TEXT.decode(ByteBuffer.wrap(rawTitle, 0, zeroTerminatedLength)).toString();
        } catch (IOException ignored) {
            return null;
        }
    }

    /**
     * Native timestamp formatting block inside SaveDialogVisualObject::Initialize @0043ECE0.
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
     * Native next-slot selection block inside SaveDialogVisualObject::Initialize @0043ECE0 using comparator @0043ECA8.
     */
    private int resolveNextAvailableSaveSlotNumber() {
        List<Integer> usedSlotNumbers = new ArrayList<>();
        for (int i = 1; i < savedGameFileNames.size(); i++) {
            int slotNumber = SavedGameFiles.parseSaveFileSlotNumber(savedGameFileNames.get(i));
            usedSlotNumbers.add(slotNumber);
        }
        usedSlotNumbers.sort(Integer::compareTo);
        if (usedSlotNumbers.isEmpty()) {
            return 0;
        }

        int nextSlotNumber = usedSlotNumbers.get(usedSlotNumbers.size() - 1) + 1;
        for (int expectedSlotNumber = 0; expectedSlotNumber < usedSlotNumbers.size(); expectedSlotNumber++) {
            if (usedSlotNumbers.get(expectedSlotNumber) != expectedSlotNumber) {
                nextSlotNumber = expectedSlotNumber;
                break;
            }
        }
        return nextSlotNumber;
    }

    /**
     * Native filename formatting block inside SaveDialogVisualObject::Initialize @0043ECE0.
     */
    private static String formatSaveFileName(int slotNumber) {
        return String.format(Locale.ROOT, SAVE_FILE_PATTERN, slotNumber);
    }

    /**
     * Native: SaveDialogVisualObject::RefreshOkButtonEnabledState @0043FE78.
     * Fully ported.
     */
    private void refreshOkButtonEnabledState() {
        StaticTextVisualObject saveNameInput = (StaticTextVisualObject) getChildById(SAVE_NAME_INPUT_ID);
        StringBuilder inputText = new StringBuilder();
        saveNameInput.copyTextToBuffer(inputText);

        CVisualObject okButtonChild = getChildById(OK_BUTTON_ID);
        okButtonChild.setStateFlag(1, inputText.isEmpty() ? 0 : 1);
        okButtonChild.draw();
    }

    /**
     * Native support branch inside SaveDialogVisualObject::OnMessage @0043F8DA.
     */
    private int handleConfirmSave() {
        CVisualObject okButtonChild = getChildById(OK_BUTTON_ID);
        if (okButtonChild.checkStateFlag(1) == 0) {
            return 1;
        }

        StringBuilder inputText = new StringBuilder();
        StaticTextVisualObject saveNameInput = (StaticTextVisualObject) getChildById(SAVE_NAME_INPUT_ID);
        saveNameInput.copyTextToBuffer(inputText);
        String selectedTitle = inputText.toString();
        saveFile.title = selectedTitle;

        SavedGamesTextListVisualObject savedGamesList =
                (SavedGamesTextListVisualObject) getChildById(SAVED_GAMES_LIST_ID);
        for (int rowIndex = 1; rowIndex < savedGamesList.rows.size(); rowIndex++) {
            if (selectedTitle.equals(savedGamesList.rows.get(rowIndex))) {
                selectedEntryIndex = rowIndex;
                break;
            }
        }

        saveFile.filename = savedGameFileNames.get(selectedEntryIndex);
        return super.onMessage(DIALOG_OK, 0, 0);
    }

    /**
     * Native support branch inside SaveDialogVisualObject::OnMessage @0043F8DA.
     */
    private void handleSavedGameSelectionChanged(int selectedEntryIndex) {
        this.selectedEntryIndex = selectedEntryIndex;

        StaticTextVisualObject saveNameInput = (StaticTextVisualObject) getChildById(SAVE_NAME_INPUT_ID);
        String selectedSavedGameTitle = selectedEntryIndex < 1
                ? ""
                : ((SavedGamesTextListVisualObject) getChildById(SAVED_GAMES_LIST_ID))
                .getRowTextAtClampedIndex(selectedEntryIndex);
        saveNameInput.setInputText(selectedSavedGameTitle);
        saveNameInput.draw();

        refreshOkButtonEnabledState();
    }

    /**
     * Native support branch inside SaveDialogVisualObject::OnMessage @0043F8DA.
     */
    private void handleDeleteSavedGame() {
        SavedGamesTextListVisualObject savedGamesList =
                (SavedGamesTextListVisualObject) getChildById(SAVED_GAMES_LIST_ID);
        int selectedSavedGameIndex = savedGamesList.getSelectedRow();
        if (selectedSavedGameIndex == 0) {
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
    }

    /**
     * Native file-delete branch inside SaveDialogVisualObject::OnMessage @0043F8DA.
     */
    private static void deleteSavedGameFile(String saveFileName) {
        try {
            Files.deleteIfExists(SavedGameFiles.resolvePath(saveFileName));
        } catch (IOException ignored) {
        }
    }

}
