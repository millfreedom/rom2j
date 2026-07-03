package ua.millfreedom.rom2;

import lombok.SneakyThrows;
import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.CFile.CFileException;
import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.res.ResFS;
import ua.millfreedom.rom2.res.ResInFile;
import ua.millfreedom.rom2.res.Resources;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static ua.millfreedom.rom2.CFile.CFileException.Cause.BAD_PATH;
import static ua.millfreedom.rom2.CFile.CFileException.Cause.FILE_NOT_FOUND;
import static ua.millfreedom.rom2.res.Constants.*;


public final class CGameFileManager implements MfcSerializable {
    /**
     * Native PATH_TYPE enum returned by Global::GetPathType @004E34D0.
     */
    public enum PathType {
        NOT_FOUND,
        FILE,
        DIRECTORY
    }


    //0x00
    public final ResFS data = new ResFS();

    //0x14
    public final CustomList<String> names = CustomList.std(String.class);

    //0x28
    public final List<ua.millfreedom.rom2.CFile.CFile> files = new ArrayList<>();

    // not native. Java controls whether resource names are normalized before lookup.
    public final boolean lowerCaseEnabled;

    // not ported.
    public static CGameFileManager Init() {
        return Init(null);
    }

    // not ported.
    public static CGameFileManager Init(String path) {
        CGameFileManager fm = createNativeGlobalFileManager(path);
        fm.loadNativeStartupResources();
        return fm;
    }

    /**
     * Native support extracted from CMainApp::InitInstance @00480C8D search-path setup and
     * CGameFileManager::New @004E24F6 current-directory initialization. CMainApp::InitInstance owns
     * the startup resource-loading sequence.
     */
    public static CGameFileManager createNativeGlobalFileManager(String path) {
        CGameFileManager fm = new CGameFileManager(true);
        if (path != null) {
            fm.addSearchPath(path);
        }
        fm.addCurrentDirectorySearchPath();
        return fm;
    }

    /**
     * Native support extracted from CMainApp::InitInstance @00480C8D resource loading sequence.
     */
    public void loadNativeStartupResources() {
        loadNativePrimaryStartupResources();
        loadNativeSecondaryStartupResources();
        loadNativeUpdateList();
        loadBundledPatchUpdateList();
    }

    /**
     * Native support extracted from the graphics/main/patch/world/music/video LoadResource block in CMainApp::InitInstance @00480C8D.
     */
    public void loadNativePrimaryStartupResources() {
        //loadResources(List.of(PATCH));
        loadResources(Arrays.asList(GRAPHICS, MAIN, WORLD, PATCH));
        loadOptionalPrimaryStartupResource(MUSIC, () -> Globals.soundPreferences.musicAvailable = 0);
        loadOptionalPrimaryStartupResource(VIDEO, () -> Globals.videoResourcesAvailable = false);
    }

    /**
     * Native support extracted from the sfx/movies/scenario/speech LoadResource block in CMainApp::InitInstance @00480C8D.
     */
    public void loadNativeSecondaryStartupResources() {
        loadResources(Arrays.asList(SFX, MOVIES, SCENARIO, SPEECH));
    }

    /**
     * Native support extracted from Global::LoadUpdateList @004E2A1D as used by
     * CMainApp::InitInstance @00480C8D.
     * Fully ported.
     */
    public void loadNativeUpdateList() {
        loadUpdateList("update.lst");
    }

    /**
     * Native: CGameFileManager::Serialize @004E3850.
     * Skipped in Java: native archives raw CArray<Res> pointer slots; Java uses managed ResFS resource storage.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        throw new UnsupportedOperationException(
                "CGameFileManager::Serialize archives native resource pointers and is not applicable in Java"
        );
    }

    /**
     * Native support extracted from repeated LoadResource calls in CMainApp::InitInstance @00480C8D.
     */
    private void loadResources(List<String> resourceNames) {
        try {
            for (String resourceName : resourceNames) {
                loadResource(resourceName).close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Native support extracted from CMainApp::InitInstance @004811E4 and @00481223 optional music/video resource handlers.
     */
    private void loadOptionalPrimaryStartupResource(String resourceName, Runnable failureHandler) {
        try {
            loadResource(resourceName).close();
        } catch (CFileException | IOException e) {
            failureHandler.run();
        }
    }

    /**
     * Native: CGameFileManager::AddSearchPath @004E2963.
     * Fully ported with Java-only `~/` expansion before native path classification.
     */
    public void addSearchPath(String path) {
        String expandedPath = expandHomeDirectory(path);
        String normalizedPath = copyLowerIfEnabled(expandedPath);
        if (getPathType(expandedPath) != PathType.DIRECTORY) {
            throw new CFileException(BAD_PATH);
        }
        names.add(normalizedPath);
    }

    /**
     * Native support extracted from CGameFileManager::New @004E24F6 GetCurrentDirectoryA/AddSearchPath sequence.
     */
    private void addCurrentDirectorySearchPath() {
        addSearchPath(Utils.getCurDirectory().toString());
    }

    /**
     * Native support extracted from CMainApp::InitInstance @00480C8D Java startup path setup before
     * CGameFileManager::AddSearchPath @004E2963.
     */
    private static String expandHomeDirectory(String path) {
        if (path.startsWith("~/")) {
            return path.replaceFirst("~", Globals.HOME_DIR);
        }
        return path;
    }

    /**
     * Native: Global::CopyLowerIfEnabled @004E26C9.
     * Fully ported.
     */
    public String copyLowerIfEnabled(String value) {
        if (lowerCaseEnabled) {
            return value.toLowerCase(Locale.ROOT);
        }
        return value;
    }

    /**
     * Native: Global::GetPathType @004E34D0.
     * Fully ported using Java filesystem metadata plus the native drive-root special case.
     */
    public static PathType getPathType(String path) {
        Path candidate = Path.of(path);
        if (Files.exists(candidate)) {
            if (Files.isDirectory(candidate)) {
                return PathType.DIRECTORY;
            }
            return PathType.FILE;
        }
        if (isNativeDriveRoot(path)) {
            return PathType.DIRECTORY;
        }
        return PathType.NOT_FOUND;
    }

    /**
     * Native support extracted from Global::GetPathType @004E34D0 `<drive>:\` logical-drive branch.
     */
    private static boolean isNativeDriveRoot(String path) {
        if (path.length() != 3 || path.charAt(1) != ':' || path.charAt(2) != '\\') {
            return false;
        }
        char drive = Character.toLowerCase(path.charAt(0));
        if (!Character.isLetterOrDigit(drive)) {
            return false;
        }
        for (Path root : FileSystems.getDefault().getRootDirectories()) {
            String rootText = root.toString();
            if (!rootText.isEmpty() && Character.toLowerCase(rootText.charAt(0)) == drive) {
                return true;
            }
        }
        return false;
    }

    // not ported.
    public CGameFileManager(boolean lowerCaseEnabled) {
        this.lowerCaseEnabled = lowerCaseEnabled;
    }

    // not ported.
    public ByteArrayInputStream getStream(String path) {
        ByteBuffer data = get(path);
        return new ByteArrayInputStream(data.array(), data.arrayOffset() + data.position(), data.remaining());
    }

    /**
     * Native: CGameFileManager::OpenFile @004E2B5D.
     * Fully ported for Java-managed resource/file buffers.
     * Java returns the opened byte buffer directly instead of the native transient ResBasicFile wrapper.
     * Java also searching a file in that order:
     * regular folders:
     * - locale/
     * - patch/
     * -
     *
     * allowed directories first, then in .res in 2 passes:
     *
     * first one in "patch/", then - normal pass
     */
    @SneakyThrows
    public ByteBuffer openGameFileData(String path) {
        Objects.requireNonNull(path, "path");
        String normalized = copyLowerIfEnabled(path);

        ByteBuffer loadedResource;
        //is it living in a locale/ or patch/?
        if (!normalized.startsWith(PATCH) && !normalized.startsWith(LOCALE)) {
            for (String tried : List.of(LOCALE, PATCH)) {
                loadedResource = openGameFileData(Resources.path(tried, normalized));
                if (loadedResource != null) {
                    return loadedResource;
                }
            }
        }
        //is it in a regular filesystem?
        for (String base : names) {
            Path candidate = Path.of(buildFullFileName(base, normalized));
            if (Files.exists(candidate)) {
                return ByteBuffer.wrap(Files.readAllBytes(candidate)).order(ByteOrder.LITTLE_ENDIAN);
            }
        }

        //is it in .res files?
        loadedResource = data.get(normalized);
        if (loadedResource != null) {
            return loadedResource.slice().order(ByteOrder.LITTLE_ENDIAN);
        }

        //unable to find a file anywhere
        return null;
    }

    /**
     * Java resource-packaging support for native CGameFileManager::OpenFile @004E2B5D plus
     * Global::LoadUpdateList @004E2A1D patched-resource overlay. Native opens updated files by the original resource
     * name; the Java repository keeps unpacked patch assets under `patch/<original resource path>`.
     */
    private static ByteBuffer openBundledPatchOverlayResourceData(String normalizedPath) throws IOException {
        String patchResourcePath = Resources.path(PATCH, normalizedPath);
        try (InputStream input = CGameFileManager.class.getClassLoader().getResourceAsStream(patchResourcePath)) {
            if (input == null) {
                return null;
            }
            return ByteBuffer.wrap(input.readAllBytes()).order(ByteOrder.LITTLE_ENDIAN);
        }
    }

    // not ported.
    @SneakyThrows
    public ByteBuffer get(String path) {
        ByteBuffer result = openGameFileData(path);
        if (result == null) {
            throw new FileNotFoundException(path);
        }
        return result;
    }

    // not ported.
    public List<String> find(String pathRegex) {
        return data.find(pathRegex);
    }

    /**
     * Native support extracted from CGameFile::Open @004E2FC3 callers that use it as an existence probe.
     */
    public boolean exists(String path) {
        return openGameFileData(path) != null;
    }

    /**
     * Native: CGameFileManager::LoadResource @004E274A.
     * Fully ported for Java-managed resource storage.
     */
    public ResInFile loadResource(String resName) throws IOException {
        Objects.requireNonNull(resName, "resName");

        final String fName = copyLowerIfEnabled(resName) + ".res";

        if (names.isEmpty()) {
            throw new CFileException(FILE_NOT_FOUND);
        }

        for (String base : names) {
            final String full = buildFullFileName(base, fName);
            try {
                ResInFile res = ResInFile.read(full);
                data.read(res);
                return res;
            } catch (IOException ignored) {
                // Native catches failed ResInFile::Read attempts and tries the next search path.
            }
        }
        throw new CFileException(FILE_NOT_FOUND);
    }

    /**
     * Native: Global::LoadUpdateList @004E2A1D.
     * Fully ported for the active Java CGameFileManager instance.
     */
    public void loadUpdateList(String path) {
        Path updateList = Path.of(path);
        if (!Files.exists(updateList)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(updateList, StandardCharsets.ISO_8859_1)) {
            processUpdateList(reader);
        } catch (IOException e) {
            throw new RuntimeException("Global::LoadUpdateList failed for " + path, e);
        }
    }

    /**
     * Java packaging support for native Global::LoadUpdateList @004E2A1D when updated loose files are bundled
     * as classpath resources under patch/update.lst.
     * not ported.
     */
    public void loadBundledPatchUpdateList() {
        String updateListPath = Resources.path(PATCH, "update.lst");
        try (InputStream input = CGameFileManager.class.getClassLoader().getResourceAsStream(updateListPath)) {
            if (input == null) {
                return;
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(input, StandardCharsets.ISO_8859_1))) {
                processUpdateList(reader);
            }
        } catch (IOException e) {
            throw new RuntimeException("Bundled patch/update.lst failed", e);
        }
    }

    /**
     * Native support extracted from Global::LoadUpdateList @004E2A1D shared by loose and bundled update lists.
     */
    private void processUpdateList(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty() || !isNativePrintable(line.charAt(0))) {
                break;
            }
            int carriageReturn = line.indexOf('\r');
            if (carriageReturn >= 0) {
                line = line.substring(0, carriageReturn);
            }
            processUpdateEntry(line);
        }
    }

    /**
     * Native support extracted from Global::LoadUpdateList @004E2A1D `isprint(line[0])`.
     */
    private static boolean isNativePrintable(char value) {
        return value >= 0x20 && value <= 0x7e;
    }

    /**
     * Native: CGameFileManager::ProcessUpdateEntry @004E2AF9.
     * Fully ported for Java-managed resource storage.
     */
    public void processUpdateEntry(String filename) {
        data.markUpdated(filename);
    }

    /**
     * Native support extracted from CGameFileManager::LoadResource @004E274A and
     * CGameFileManager::OpenFile @004E2B5D path assembly.
     */
    private static String buildFullFileName(String base, String fName) {
        StringBuilder sb = new StringBuilder(Objects.requireNonNull(base));
        int n = sb.length();
        if (!(n >= 2 && sb.charAt(n - 1) == File.separatorChar && sb.charAt(n - 2) == ':')) {
            sb.append(File.separatorChar);
        }
        sb.append(Objects.requireNonNull(fName));
        return sb.toString();
    }

    @Override
    // not ported.
    public String toString() {
        return "CGameFileManager{" +
                ", names=" + names +
                '}';
    }
}
