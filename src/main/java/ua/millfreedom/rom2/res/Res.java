package ua.millfreedom.rom2.res;

import ua.millfreedom.rom2.CString;
import ua.millfreedom.rom2.CFile.CFileException;
import ua.millfreedom.rom2.GameCharsets;
import ua.millfreedom.rom2.Utils.Counter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static ua.millfreedom.rom2.Utils.join;
import static ua.millfreedom.rom2.CFile.CFileException.Cause.BAD_PATH;
import static ua.millfreedom.rom2.CFile.CFileException.Cause.FILE_NOT_FOUND;

public class Res {
    //0x00
    public ResNode resNode;

    //0x20
    public int nodesCount;

    //0x24
    public int nodesCap;

    //0x28
    public final List<ResNode> nodes;

    //0x2c
    public ResNode pInsertion;

    //0x30
    public int totalDataSize;

    /**
     * not ported.
     */
    public Res(ResNode resNode, int nodesCount, int nodesCap, List<ResNode> nodes,
               ResNode pInsertion, int totalDataSize) {
        this.resNode = Objects.requireNonNull(resNode);
        this.nodesCount = nodesCount;
        this.nodesCap = nodesCap;
        this.nodes = Objects.requireNonNull(nodes);
        this.pInsertion = pInsertion;
        this.totalDataSize = totalDataSize;
    }

    @Override
    /**
     * not ported.
     */
    public String toString() {
        return "\nRes{" +
                "root=" + resNode +
                ", nodesCount=" + nodesCount +
                ", nodesCap=" + nodesCap +
                ", nodes=\n" + join(nodes, Counter.from(0), ",\n") +
                ", pInsertion=" + pInsertion +
                ", totalDataSize=" + totalDataSize +
                '}';
    }

    /**
     * Native: Res::ResToTextFile @004E63A3.
     * Fully ported.
     */
    public void resToTextFile(String filename) throws IOException {
        byte[] heapData = ((ResInHeap) this).heap.data();
        Path outputPath = resolveTextExportPath(filename);
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, GameCharsets.GAME_TEXT)) {
            ResContainerNode rootContainer = (ResContainerNode) resNode.atom().data();
            for (int groupOffset = 0; groupOffset < rootContainer.childCount(); groupOffset++) {
                ResNode group = nodes.get(rootContainer.firstChildIndex() + groupOffset);
                writer.write("[");
                writer.write(group.getName());
                writer.write("]\n");

                ResContainerNode groupContainer = (ResContainerNode) group.atom().data();
                for (int entryOffset = 0; entryOffset < groupContainer.childCount(); entryOffset++) {
                    ResNode entry = nodes.get(groupContainer.firstChildIndex() + entryOffset);
                    writeTextExportEntry(writer, entry, heapData);
                }
                writer.write("\n");
            }
        }
    }

    /**
     * Native: Res::WriteAtPosition @004E4A86.
     * Fully ported for heap-backed resources; Java writes the same native header, node table, heap size, and heap data.
     */
    public void writeAtPosition(Path outputPath, int position) throws IOException {
        byte[] bytes = writeAtPosition();
        if (position == -1) {
            Files.write(outputPath, bytes);
            return;
        }

        try (SeekableByteChannel channel = Files.newByteChannel(
                outputPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE
        )) {
            channel.position(position);
            channel.write(ByteBuffer.wrap(bytes));
            channel.truncate(position + bytes.length);
        }
    }

    /**
     * Native support extracted from Res::WriteAtPosition @004E4A86.
     */
    public byte[] writeAtPosition() {
        if (!(this instanceof ResInHeap heapResource)) {
            throw new IllegalStateException("Res::WriteAtPosition requires a heap-backed resource");
        }

        sortRecursive(resNode);
        nodesCount = nodes.size();
        int heapUsed = heapResource.heap.used;
        int byteSize = Integer.BYTES * 7 + nodesCount * ResNode.SIZE + heapUsed;
        ByteBuffer buffer = ByteBuffer.allocate(byteSize).order(ByteOrder.LITTLE_ENDIAN);
        writeAtomHeader(buffer, resNode.atom());
        buffer.putInt(nodesCount);
        buffer.putInt(totalDataSize);
        for (ResNode node : nodes) {
            writeNode(buffer, node);
        }
        buffer.putInt(heapUsed);
        buffer.put(heapResource.heap.data, 0, heapUsed);
        return buffer.array();
    }

    /**
     * Native: Res::FUN_004E4B74, named Res::ReadNamedHeap.
     * Fully ported. Native reads the serialized root name before delegating to ResInHeap::Read @004E488C.
     */
    public static ResInHeap readNamedHeap(ByteBuffer cursor, int position) throws IOException {
        byte[] rootName16 = new byte[0x10];
        cursor.get(rootName16);
        return ResInHeap.read(cursor, position, new CString(rootName16).toString());
    }

    /**
     * Native support extracted from Res::SortRecursive @004E8E26 for Res::WriteAtPosition @004E4A86.
     */
    private void sortRecursive(ResNode container) {
        if (!(container.atom().data() instanceof ResContainerNode range)) {
            return;
        }

        for (int offset = 0; offset < range.childCount(); offset++) {
            ResNode child = nodes.get(range.firstChildIndex() + offset);
            if ((child.atom().flags()
                    & (ResNodeFlags.RESFLAG_VISITED | ResNodeFlags.RESFLAG_IS_CONTAINER))
                    == ResNodeFlags.RESFLAG_IS_CONTAINER) {
                sortRecursive(child);
            }
        }

        if (range.childCount() != 0) {
            nodes.subList(range.firstChildIndex(), range.firstChildIndex() + range.childCount())
                    .sort(ResNode::compareByName);
            replaceNode(container, withAtom(
                    container,
                    container.atom().data(),
                    container.atom().flags() | ResNodeFlags.RESFLAG_SORTED
            ));
        }
    }

    /**
     * Native support extracted from Res::WriteAtPosition @004E4A86 atom field writes.
     */
    private static void writeAtomHeader(ByteBuffer buffer, ResAtom atom) {
        buffer.putInt(atom.magic());
        buffer.putInt(ResNodeData.firstWord(atom.data()));
        buffer.putInt(ResNodeData.secondWord(atom.data()));
        buffer.putInt(atom.flags());
    }

    /**
     * Native support extracted from Res::WriteAtPosition @004E4A86 node-table writes.
     */
    private static void writeNode(ByteBuffer buffer, ResNode node) {
        writeAtomHeader(buffer, node.atom());
        node.name16().write(buffer);
    }

    /**
     * Native: Res::throwRegistryParseException @004E4840.
     * Fully ported. Native frees raw resource buffers before throwing CRegException; Java relies on managed storage.
     */
    protected final void throwRegistryParseException(String message) {
        throw new IllegalStateException(message);
    }

    /**
     * Native support extracted from Res::ResToTextFile @004E63A3 filename retry loop.
     */
    private static Path resolveTextExportPath(String filename) {
        String candidate = filename;
        String prefix = "-";
        while (Files.exists(Path.of(candidate))) {
            candidate = prefix + filename;
            prefix += "-";
        }
        return Path.of(candidate);
    }

    /**
     * Native support extracted from Res::ResToTextFile @004E63A3 per-node output switch.
     */
    private static void writeTextExportEntry(BufferedWriter writer, ResNode entry, byte[] heapData) throws IOException {
        int type = (entry.atom().flags() & ResNodeFlags.RESFLAG_MASK_ANY_OF_BASIC) >>> 1;
        ResNodeData data = entry.atom().data();
        int offset = ResNodeData.firstWord(data);
        int byteSize = ResNodeData.secondWord(data);
        String name = entry.getName();
        switch (type) {
            case 0 -> writer.write(name + "=" + readHeapCString(heapData, offset) + "\n");
            case 1 -> writer.write(name + "=" + offset + "\n");
            case 2 -> writer.write(name + "=" + formatNativeDouble(readNodeDouble(data)) + "\n");
            case 3 -> writeTextExportIntArray(writer, name, heapData, offset, byteSize);
            case 4 -> writeTextExportStringList(writer, name, heapData, offset);
            case 5 -> writeTextExportDoubleArray(writer, name, heapData, offset, byteSize);
            default -> {
            }
        }
    }

    /**
     * Native support extracted from Res::ResToTextFile @004E63A3 int-array formatting.
     */
    private static void writeTextExportIntArray(
            BufferedWriter writer,
            String name,
            byte[] heapData,
            int offset,
            int byteSize
    ) throws IOException {
        writer.write(name + "=");
        int count = byteSize >>> 2;
        for (int index = 0; index < count; index++) {
            writer.write(" ");
            writer.write(Integer.toString(readHeapInt(heapData, offset + index * Integer.BYTES)));
            writer.write(index < count - 1 ? "," : "\n");
        }
    }

    /**
     * Native support extracted from Res::ResToTextFile @004E63A3 blob/string-list formatting.
     */
    private static void writeTextExportStringList(
            BufferedWriter writer,
            String name,
            byte[] heapData,
            int offset
    ) throws IOException {
        int count = readHeapInt(heapData, offset);
        int cursor = offset + Integer.BYTES;
        for (int index = 0; index < count; index++) {
            String value = readHeapCString(heapData, cursor);
            writer.write(name + "=" + value + "\n");
            cursor += value.getBytes(GameCharsets.GAME_TEXT).length + 1;
        }
    }

    /**
     * Native support extracted from Res::ResToTextFile @004E63A3 double-array formatting.
     */
    private static void writeTextExportDoubleArray(
            BufferedWriter writer,
            String name,
            byte[] heapData,
            int offset,
            int byteSize
    ) throws IOException {
        writer.write(name + "=");
        int count = byteSize >>> 3;
        for (int index = 0; index < count; index++) {
            writer.write(" ");
            writer.write(formatNativeDouble(readHeapDouble(heapData, offset + index * Double.BYTES)));
            writer.write(index < count - 1 ? "," : "\n");
        }
    }

    /**
     * Native support extracted from Res::ResToTextFile @004E63A3 scalar double field reads.
     */
    private static double readNodeDouble(ResNodeData data) {
        if (data instanceof ResDoubleNode doubleNode) {
            return doubleNode.value();
        }
        long bits = Integer.toUnsignedLong(ResNodeData.firstWord(data))
                | (Integer.toUnsignedLong(ResNodeData.secondWord(data)) << 32);
        return Double.longBitsToDouble(bits);
    }

    /**
     * Native support extracted from Res::ResToTextFile @004E63A3 C-string heap reads.
     */
    private static String readHeapCString(byte[] heapData, int offset) {
        int end = offset;
        while (end < heapData.length && heapData[end] != 0) {
            end++;
        }
        return new String(heapData, offset, end - offset, GameCharsets.GAME_TEXT);
    }

    /**
     * Native support extracted from Res::ResToTextFile @004E63A3 little-endian int heap reads.
     */
    private static int readHeapInt(byte[] heapData, int offset) {
        return ByteBuffer.wrap(heapData).order(ByteOrder.LITTLE_ENDIAN).getInt(offset);
    }

    /**
     * Native support extracted from Res::ResToTextFile @004E63A3 little-endian double heap reads.
     */
    private static double readHeapDouble(byte[] heapData, int offset) {
        return ByteBuffer.wrap(heapData).order(ByteOrder.LITTLE_ENDIAN).getDouble(offset);
    }

    /**
     * Native support extracted from Res::ResToTextFile @004E63A3 `%f` formatting.
     */
    private static String formatNativeDouble(double value) {
        return String.format(Locale.US, "%f", value);
    }

    /**
     * Native: Res::FindNodeByPath @004E9046.
     * Fully ported.
     */
    public ResNode findNodeByPath(String name) {
        Objects.requireNonNull(name, "name");

        int nameIndex = 0;
        String rootName = resNode.getName();
        for (int rootNameIndex = 0;
             nativeCharAt(name, nameIndex) != '\\'
                     && nativeCharAt(name, nameIndex) != '/'
                     && nativeCharAt(name, nameIndex) != '\0'
                     && nativeCharAt(rootName, rootNameIndex) != '\0';
             rootNameIndex++) {
            if (nativeCharAt(name, nameIndex) != nativeCharAt(rootName, rootNameIndex)) {
                return null;
            }
            nameIndex++;
        }

        ResNode node = resNode;
        if (nativeCharAt(name, nameIndex) != '\0') {
            nameIndex++;
        }

        PathCursor cursor = new PathCursor(name, nameIndex);
        while (true) {
            if (cursor.isAtEnd()) {
                return node;
            }
            if (node == null) {
                break;
            }
            if ((node.atom().flags()
                    & (ResNodeFlags.RESFLAG_VISITED | ResNodeFlags.RESFLAG_IS_CONTAINER))
                    != ResNodeFlags.RESFLAG_IS_CONTAINER) {
                return null;
            }
            node = findChildPathStep(node, cursor);
        }
        return null;
    }

    /**
     * Native: Res::GetGroup @004E9106.
     * Fully ported. Native throws CFileException::fileNotFound for missing paths and badPath for non-group paths.
     */
    public ResNode getGroup(String name) {
        ResNode result = findNodeByPath(name);
        if (result == null) {
            throw new CFileException(FILE_NOT_FOUND);
        }
        if ((result.atom().flags()
                & (ResNodeFlags.RESFLAG_VISITED | ResNodeFlags.RESFLAG_IS_CONTAINER)) == 0) {
            throw new CFileException(BAD_PATH);
        }
        return result;
    }

    /**
     * Native: Res::FindUnsortedNode @004E9153.
     * Fully ported.
     */
    public ResNode findUnsortedNode(String name) {
        ResNode result = findNodeByPath(name);
        if (result != null
                && (result.atom().flags()
                & (ResNodeFlags.RESFLAG_VISITED | ResNodeFlags.RESFLAG_SORTED)) != 0) {
            return null;
        }
        return result;
    }

    /**
     * Native: Res::FindChildByName @004E918E.
     * Fully ported.
     */
    public ResNode findChildByName(ResNode root, String name) {
        if (root == null) return null;
        if (name == null) name = "";

        if (!(root.atom().data() instanceof ResContainerNode(int firstChildIndex, int childCount))
                || firstChildIndex < 0
                || childCount < 0
        ) return null;

        int end = firstChildIndex + childCount;
        if (end < firstChildIndex || end > nodes.size()) return null;

        if (((root.atom().flags() & ResNodeFlags.RESFLAG_SORTED) == 0) || (childCount == 0)) {
            for (int i = 0; i < childCount; i++) {
                ResNode node = nodes.get(firstChildIndex + i);
                if (node.getName().compareToIgnoreCase(name) == 0) {
                    return node;
                }
            }
            return null;
        }
        List<ResNode> childNodes = nodes.subList(firstChildIndex, end);
        int index = Collections.binarySearch(childNodes, new ResNode(name));
        return index < 0 ? null : childNodes.get(index);
    }

    /**
     * Native: Res::FindChildPathStep @004E9268.
     * Fully ported.
     */
    private ResNode findChildPathStep(ResNode node, PathCursor name) {
        int end = name.index;
        while (nativeCharAt(name.path, end) != '\\'
                && nativeCharAt(name.path, end) != '/'
                && nativeCharAt(name.path, end) != '\0') {
            end++;
        }
        char separator = nativeCharAt(name.path, end);
        ResNode result = findChildByName(node, name.path.substring(name.index, end));
        name.index = separator == '\0' ? end : end + 1;
        return result;
    }

    /**
     * Native support extracted from Global::CompareNodes @004E93C1.
     * Fully ported.
     */
    public static int compareNodesByData(ResNode left, ResNode right) {
        int leftFirstChildIndex = ResNodeData.firstWord(left.atom().data());
        int rightFirstChildIndex = ResNodeData.firstWord(right.atom().data());
        if (rightFirstChildIndex < leftFirstChildIndex) {
            return 1;
        }
        if (leftFirstChildIndex == rightFirstChildIndex) {
            int leftChildCount = ResNodeData.secondWord(left.atom().data());
            int rightChildCount = ResNodeData.secondWord(right.atom().data());
            if (rightChildCount < leftChildCount) {
                return 1;
            }
            return 0;
        }
        return -1;
    }

    /**
     * Native: Res::GetInt @004E6F9F.
     * Fully ported.
     */
    public int getInt(String sectionName, String keyName, int defaultValue) {
        ResNode root = findChildByName(this.resNode, sectionName);
        ResNode node = findChildByName(root, keyName);

        if (node == null) return defaultValue;

        if ((node.atom().flags() & ResNodeFlags.RESFLAG_MASK_ANY_OF_BASIC) != ResNodeFlags.RESFLAG_TYPE_INT32) {
            throw new IllegalStateException("Value is not INT32 for [" + sectionName + "] " + keyName);
        }

        return ResNodeData.firstWord(node.atom().data());
    }

    /**
     * Native: Res::SetInt @004E7018.
     * Fully ported.
     */
    public void setInt(String groupName, String entryName, int value) {
        ResNode group = findChildByName(resNode, groupName);
        ResNode entry = findChildByName(group, entryName);
        if (entry == null) {
            if (group == null) {
                group = createGroup(resNode.getName(), groupName);
            }
            entry = addChildToParent(Objects.requireNonNull(group, "group"), entryName);
            replaceNode(entry, withAtom(entry, new ResIntNode(value), ResNodeFlags.RESFLAG_TYPE_INT32));
            return;
        }

        if ((entry.atom().flags() & ResNodeFlags.RESFLAG_MASK_ANY_OF_BASIC) != ResNodeFlags.RESFLAG_TYPE_INT32) {
            throw new IllegalStateException("Value is not INT32 for [" + groupName + "] " + entryName);
        }
        replaceNode(entry, withAtom(entry, new ResIntNode(value), entry.atom().flags()));
    }

    /**
     * Native: Res::GetDouble @004E70F6.
     * Fully ported.
     */
    public double getDouble(String sectionName, String keyName, double defaultValue) {
        ResNode root = findChildByName(this.resNode, sectionName);
        ResNode node = findChildByName(root, keyName);
        if (node == null) {
            return defaultValue;
        }
        if ((node.atom().flags() & ResNodeFlags.RESFLAG_MASK_ANY_OF_BASIC) != ResNodeFlags.RESFLAG_TYPE_DOUBLE) {
            throw new IllegalStateException("Value is not DOUBLE for [" + sectionName + "] " + keyName);
        }
        ResNodeData data = node.atom().data();
        if (data instanceof ResDoubleNode doubleNode) {
            return doubleNode.value();
        }
        long bits = Integer.toUnsignedLong(ResNodeData.firstWord(data))
                | (Integer.toUnsignedLong(ResNodeData.secondWord(data)) << 32);
        return Double.longBitsToDouble(bits);
    }

    /**
     * Native: Res::SetDouble @004E717B.
     * Fully ported.
     */
    public void setDouble(String groupName, String entryName, double value) {
        ResNode group = findChildByName(resNode, groupName);
        ResNode entry = findChildByName(group, entryName);
        if (entry == null) {
            if (group == null) {
                group = createGroup(resNode.getName(), groupName);
            }
            entry = addChildToParent(Objects.requireNonNull(group, "group"), entryName);
            replaceNode(entry, withAtom(entry, new ResDoubleNode(value), ResNodeFlags.RESFLAG_TYPE_DOUBLE));
            return;
        }

        if ((entry.atom().flags() & ResNodeFlags.RESFLAG_MASK_ANY_OF_BASIC) != ResNodeFlags.RESFLAG_TYPE_DOUBLE) {
            throw new IllegalStateException("Value is not DOUBLE for [" + groupName + "] " + entryName);
        }
        replaceNode(entry, withAtom(entry, new ResDoubleNode(value), entry.atom().flags()));
    }

    /**
     * Native: Res::CreateGroup @004E6873.
     * Fully ported.
     */
    public ResNode createGroup(String parentPath, String newGroupName) {
        ResNode parent = findNodeByPath(parentPath);
        if (parent == null) {
            return null;
        }
        ResNode group = addChildToParent(parent, newGroupName);
        ResNode initializedGroup = withAtom(
                group,
                new ResContainerNode(nodesCount, 0),
                ResNodeFlags.RESFLAG_IS_CONTAINER
        );
        replaceNode(group, initializedGroup);
        return initializedGroup;
    }

    /**
     * Native support extracted from Res::AddChild @004E8ED0.
     */
    protected ResNode addChildToParent(ResNode parent, String childName) {
        int parentIndex = nodeIndex(parent);
        if (parentIndex < -1) {
            throw new IllegalStateException("Resource parent node is not attached: " + parent.getName());
        }
        ResContainerNode container = (ResContainerNode) parent.atom().data();
        int insertionIndex = container.firstChildIndex() + container.childCount();
        ResNode updatedParent = withAtom(
                parent,
                new ResContainerNode(container.firstChildIndex(), container.childCount() + 1),
                parent.atom().flags() & ~ResNodeFlags.RESFLAG_SORTED
        );
        if (parentIndex == -1) {
            resNode = updatedParent;
        } else {
            nodes.set(parentIndex, updatedParent);
        }

        shiftContainerFirstChildIndexes(insertionIndex, parentIndex);
        ResNode child = new ResNode(new ResAtom(), "").withName(childName);
        nodes.add(insertionIndex, child);
        nodesCount = nodes.size();
        nodesCap = Math.max(nodesCap, nodesCount);
        pInsertion = updatedParent;
        return child;
    }

    /**
     * Native support extracted from Res::GrowAndShift @004E8B28 child-index rebasing after node insertion.
     */
    private void shiftContainerFirstChildIndexes(int insertionIndex, int parentIndex) {
        if (parentIndex != -1 && resNode.atom().data() instanceof ResContainerNode rootContainer
                && rootContainer.firstChildIndex() >= insertionIndex) {
            resNode = withAtom(
                    resNode,
                    new ResContainerNode(rootContainer.firstChildIndex() + 1, rootContainer.childCount()),
                    resNode.atom().flags()
            );
        }
        for (int index = 0; index < nodes.size(); index++) {
            if (index == parentIndex) {
                continue;
            }
            ResNode node = nodes.get(index);
            if (node.atom().data() instanceof ResContainerNode container
                    && container.firstChildIndex() >= insertionIndex) {
                nodes.set(index, withAtom(
                        node,
                        new ResContainerNode(container.firstChildIndex() + 1, container.childCount()),
                        node.atom().flags()
                ));
            }
        }
    }

    /**
     * Native support extracted from Res::SetInt @004E7018 in-place node atom writes.
     */
    protected void replaceNode(ResNode target, ResNode replacement) {
        if (target == resNode) {
            resNode = replacement;
            return;
        }
        int index = nodeIndex(target);
        if (index < 0) {
            throw new IllegalStateException("Resource node is not attached: " + target.getName());
        }
        nodes.set(index, replacement);
    }

    /**
     * Native support extracted from pointer identity checks in Res::GrowAndShift @004E8B28 and Res::AddChild @004E8ED0.
     */
    private int nodeIndex(ResNode target) {
        if (target == resNode) {
            return -1;
        }
        for (int index = 0; index < nodes.size(); index++) {
            if (nodes.get(index) == target || nodes.get(index).equals(target)) {
                return index;
            }
        }
        return -2;
    }

    /**
     * Native support extracted from Res::SetInt @004E7018 and Res::CreateGroup @004E6873 atom field writes.
     */
    protected static ResNode withAtom(ResNode node, ResNodeData data, int flags) {
        return new ResNode(new ResAtom(node.atom().magic(), data, flags), node.name16());
    }

    /**
     * Native support extracted from Res::FindChildByName @004E918E `_strnicmp(..., 0xf)`.
     */
    private static int compareNameIgnoreCase15(String left, String right) {
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(right, "right");
        for (int i = 0; i < 0x0F; i++) {
            char leftChar = lowerAscii(nativeCharAt(left, i));
            char rightChar = lowerAscii(nativeCharAt(right, i));
            if (leftChar != rightChar) {
                return leftChar - rightChar;
            }
            if (leftChar == '\0') {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Native support extracted from Res::FindNodeByPath @004E9046, Res::FindChildByName @004E918E, and
     * Res::FindChildPathStep @004E9268 path/name scans that read until a C-string terminator.
     */
    private static char nativeCharAt(String value, int index) {
        if (index >= value.length()) {
            return '\0';
        }
        return value.charAt(index);
    }

    /**
     * Native support extracted from Res::FindChildByName @004E918E `_strnicmp(..., 0xf)`.
     */
    private static char lowerAscii(char value) {
        if (value >= 'A' && value <= 'Z') {
            return (char) (value + ('a' - 'A'));
        }
        return value;
    }

    /**
     * Native support extracted from Res::FindChildPathStep @004E9268 `LPCSTR *` cursor mutation.
     */
    private static final class PathCursor {
        private final String path;
        private int index;

        /**
         * Native support extracted from Res::FindChildPathStep @004E9268 `LPCSTR *` cursor setup.
         */
        private PathCursor(String path, int index) {
            this.path = path;
            this.index = index;
        }

        /**
         * Native support extracted from Res::FindNodeByPath @004E9046 `*name == '\0'` loop stop.
         */
        private boolean isAtEnd() {
            return nativeCharAt(path, index) == '\0';
        }
    }
}
