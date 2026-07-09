package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.Utils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class SavedGameFiles {
    // Native support extracted from GetCurDirectory @00474AB9 plus "\game*.sav" masks in @0043DC54 and @0043FF4A.
    private static final Path SAVE_DIRECTORY = Utils.getCurDirectory();
    // Java support, not a native field.
    private static final String SAVE_FILE_PREFIX = "game";
    // Java support, not a native field.
    private static final String SAVE_EXTENSION = ".sav";
    // Java support, not a native field.
    private static final String RESERVED_AUTOSAVE_FILE_NAME = "game9998.sav";
    // Java support, not a native field.
    private static final String RESERVED_QUICKSAVE_FILE_NAME = "game9999.sav";
    static {
        try {
            Files.createDirectories(SAVE_DIRECTORY);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Java utility holder.
     * not ported.
     */
    private SavedGameFiles() {
    }

    /**
     * Native support for saved-game opens that consume bare SaveFile::filename values, including
     * GameServer::SaveGameFile @004E9816, GameServer::LoadSaveFile @004E9B95, and CGameFile save-state reads.
     */
    public static Path resolvePath(String saveFileName) {
        return SAVE_DIRECTORY.resolve(saveFileName);
    }

    /**
     * Native: SaveFileExists @0043FF4A.
     * Fully ported.
     */
    public static boolean saveFileExists() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(SAVE_DIRECTORY)) {
            for (Path path : stream) {
                if (matchesNativeSaveMask(path.getFileName().toString())) {
                    return true;
                }
            }
            return false;
        } catch (IOException ignored) {
            return false;
        }
    }

    /**
     * Native: saved-game enumeration helper @0043DC54.
     * Fully ported.
     */
    public static List<Path> collectSavedGameFiles(boolean includeReservedSlots) {
        List<Path> savedGameFiles = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(SAVE_DIRECTORY)) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                if (!matchesNativeSaveMask(fileName) || Files.isDirectory(path)) {
                    continue;
                }
                if (!includeReservedSlots && isReservedSaveFileName(fileName)) {
                    continue;
                }
                savedGameFiles.add(path);
            }
        } catch (IOException ignored) {
            return savedGameFiles;
        }

        savedGameFiles.sort(Comparator.comparing(SavedGameFiles::getLastModifiedTime).reversed());
        return savedGameFiles;
    }

    /**
     * Native support extracted from SaveDialogVisualObject::Initialize @0043ECE0 slot-number arithmetic.
     * Fully ported.
     */
    public static int parseSaveFileSlotNumber(String saveFileName) {
        return nativeDigitValue(saveFileName, 7)
                + nativeDigitValue(saveFileName, 6) * 10
                + nativeDigitValue(saveFileName, 5) * 100
                + nativeDigitValue(saveFileName, 4) * 1000;
    }

    /**
     * Native support extracted from save/load detail-row CTime conversion in
     * LoadDialogVisualObject::Initialize @0043DF48 and SaveDialogVisualObject::Initialize @0043ECE0.
     */
    public static FileTime getLastModifiedTime(Path saveFilePath) {
        try {
            return Files.getLastModifiedTime(saveFilePath);
        } catch (IOException ignored) {
            return FileTime.fromMillis(0);
        }
    }

    /**
     * Native support extracted from the literal `\\game*.sav` FindFirstFileA masks in @0043DC54 and @0043FF4A.
     */
    private static boolean matchesNativeSaveMask(String fileName) {
        return fileName.length() >= SAVE_FILE_PREFIX.length() + SAVE_EXTENSION.length()
                && fileName.regionMatches(true, 0, SAVE_FILE_PREFIX, 0, SAVE_FILE_PREFIX.length())
                && fileName.regionMatches(true, fileName.length() - SAVE_EXTENSION.length(),
                SAVE_EXTENSION, 0, SAVE_EXTENSION.length());
    }

    /**
     * Native support extracted from reserved save-name filters in saved-game enumeration helper @0043DC54.
     */
    private static boolean isReservedSaveFileName(String fileName) {
        return RESERVED_QUICKSAVE_FILE_NAME.equals(fileName) || RESERVED_AUTOSAVE_FILE_NAME.equals(fileName);
    }

    /**
     * Native support extracted from SaveDialogVisualObject::Initialize @0043ECE0 fixed-position digit arithmetic.
     */
    private static int nativeDigitValue(String saveFileName, int index) {
        return (index < saveFileName.length() ? saveFileName.charAt(index) : 0) - '0';
    }
}
