package ua.millfreedom.rom2.res;

import ua.millfreedom.rom2.CFile.CFile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ResFS {

    Map<String, ByteBuffer> files;
    private final Set<String> updatedFiles;


    // not ported.
    public ResFS() {
        this.files = new HashMap<>();
        this.updatedFiles = new HashSet<>();
    }

    // not ported.
    public static ResFS from(ResInFile res) throws IOException {
        return new ResFS().read(res);
    }

    // not ported.
    public boolean exists(String path) {
        return files.containsKey(path.toLowerCase());
    }

    // not ported.
    public ByteBuffer get(String path) {
        return files.get(path.toLowerCase());
    }

    /**
     * Native support extracted from CGameFileManager::ProcessUpdateEntry @004E2AF9.
     */
    public boolean markUpdated(String path) {
        String normalized = path.toLowerCase();
        if (!files.containsKey(normalized)) {
            return false;
        }
        updatedFiles.add(normalized);
        return true;
    }

    /**
     * Native support extracted from ResInFile::GetFileContainer @004E2445 RESFLAG_UPDATED branch.
     */
    public boolean isUpdated(String path) {
        return updatedFiles.contains(path.toLowerCase());
    }

    // not ported.
    public List<String> find(String pathRegex) {
        return files.keySet().stream()
                .filter(e -> e.matches(pathRegex))
                .toList();
    }

    /**
     * Native support extracted from CGameFileManager::LoadResource @004E274A and
     * CGameFileManager::OpenFile @004E2B5D loaded-resource search order.
     */
    public ResFS read(ResInFile res) throws IOException {
        try (CFile file = new CFile(res.resBasicFile.path)) {
            materialize(file, res.resNode, res.nodes, null);
        }
        return this;
    }

    /**
     * Native support extracted from ResInFile::GetFileContainer @004E2445 and
     * CGameFileManager::OpenFile @004E2B5D.
     */
    private void materialize(CFile file, ResNode root, List<ResNode> nodes, String base) throws IOException {
        ResNodeData data = root.atom().data();
        String name = root.getName();
        switch (data) {
            case ResContainerNode(int firstChildIndex, int childCount) -> {
                String current = ((base == null) ? name : Resources.path(base, name)).toLowerCase();
                for (int i = 0; i < childCount; i++) {
                    materialize(file, nodes.get(firstChildIndex + i), nodes, current);
                }
            }
//            case ResHeapNode(int heapOffset, int byteSize) -> {
//                String fullName = Resources.path(base, name).toLowerCase().intern();
//                ByteBuffer byteBuffer = file.readBytes(heapOffset, byteSize).order(ByteOrder.LITTLE_ENDIAN).clear();
//                files.put(fullName, byteBuffer);
//            }
            case ResFileNode(int fileOffset, int byteSize) -> {
                String fullName = Resources.path(base, name).toLowerCase().intern();
                ByteBuffer byteBuffer = file.readBytes(fileOffset, byteSize).order(ByteOrder.LITTLE_ENDIAN).clear();
                files.putIfAbsent(fullName, byteBuffer);
            }
            case null, default -> throw new RuntimeException("THIS CASE IS UNHANDLED, FIX ME! " + data);
        }
    }
}
