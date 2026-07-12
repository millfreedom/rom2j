package ua.millfreedom.rom2.res;


import ua.millfreedom.rom2.GameCharsets;
import ua.millfreedom.rom2.model.SavedGameFiles;
import ua.millfreedom.rom2.model.CRect;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static ua.millfreedom.rom2.res.ResNodeData.*;

public final class ResInHeap extends Res {
    public final HeapBlob heap;

    private static final int RESTYPE_STRING = 0;
    private static final int RESTYPE_INT32 = 1;
    private static final int RESTYPE_DOUBLE = 2;
    private static final int RESTYPE_INT32_ARRAY = 3;
    private static final int RESTYPE_BLOB = 4;
    private static final int RESTYPE_DOUBLE_ARRAY = 5;
    private static final int TEXT_VALUE_HAS_DIGIT = 0x01;
    private static final int TEXT_VALUE_HAS_FLOAT_MARKER = 0x02;
    private static final int TEXT_VALUE_HAS_COMMA = 0x04;
    private static final int TEXT_VALUE_HAS_WHITESPACE = 0x08;
    private static final int TEXT_VALUE_HAS_OTHER = 0x10;
    private static final int TEXT_VALUE_IS_STRINGISH = 0x20;

    // not ported.
    private ResInHeap(ResNode resNode,
                      int nodesCount,
                      int nodesCap,
                      List<ResNode> nodes,
                      ResNode pInsertion,
                      int totalDataSize,
                      HeapBlob heap) {
        super(resNode, nodesCount, nodesCap, nodes, pInsertion, totalDataSize);
        this.heap = Objects.requireNonNull(heap);
    }

    /**
     * Native: ResInHeap::New @004E4BB0.
     * Fully ported.
     */
    public static ResInHeap create() {
        ResNode rootNode = new ResNode(
                new ResAtom(
                        0x31415926,
                        new ResContainerNode(0, 0),
                        ResNodeFlags.RESFLAG_IS_CONTAINER
                ),
                ""
        );
        return new ResInHeap(
                rootNode,
                0,
                4,
                new ArrayList<>(4),
                null,
                0,
                new HeapBlob(0, 0x20, new byte[0x20])
        );
    }

    /**
     * Native: ResInHeap::Read @004E488C.
     * Fully ported. The caller supplies the root name because native Load/ReadNamedHeap populate it before this read.
     */
    public static ResInHeap read(ByteBuffer cursor, int pos, String rootName) throws IOException {
        Objects.requireNonNull(cursor, "cursor");

        if (pos != -1) {
            cursor.position(pos);
        }

        final int magic = cursor.getInt();
        if (magic != 0x31415926) {
            throw new IOException("Bad resource signature (magic=0x" + Integer.toHexString(magic) + ")");
        }

        final int data0 = cursor.getInt();
        final int data1 = cursor.getInt();
        final int flags = cursor.getInt();
        final int nodesCount = cursor.getInt();
        final int totalDataSize = cursor.getInt();

        // No SetName() here => empty name[16]
        //final byte[] rootName16 = new byte[16];

        ResNodeData rootData = decodeNodeData(flags, data0, data1,false);
        ResAtom rootAtom = new ResAtom(magic, rootData, flags);
        ResNode rootNode = new ResNode(rootAtom, rootName);

        // nodes table: nodesCount * 0x20
        List<ResNode> nodes = new ArrayList<>(Math.max(nodesCount, 0));

        for (int i = 0; i < nodesCount; i++) {
            int nMagic = cursor.getInt();
            int nData0 = cursor.getInt();
            int nData1 = cursor.getInt();
            int nFlags = cursor.getInt();

            byte[] nName16 = new byte[16];
            cursor.get(nName16);

            ResNodeData nd = decodeNodeData(nFlags, nData0, nData1,false);
            ResAtom na = new ResAtom(nMagic, nd, nFlags);
            nodes.add(new ResNode(na, nName16));
        }

        // nodesCap = nodesCount
        final int used = cursor.getInt();
        if (used < 0 || used > cursor.remaining()) {
            throw new IOException("Heap size out of range: " + used);
        }
        final byte[] heapData = new byte[used];
        if (used > 0) {
            cursor.get(heapData);
        }
        HeapBlob heap = new HeapBlob(used, used, heapData);

        return new ResInHeap(
                rootNode,
                nodesCount,
                nodesCount,
                nodes,
                null,            // pInsertion unchanged in this read; keep null
                totalDataSize,
                heap
        );
    }

    /**
     * Native: ResInHeap::GetShortArray(CWordArray) @004E748D.
     * Fully ported.
     */
    public boolean getShortArray(String sectionName, String keyName, List<Short> out) {
        Objects.requireNonNull(out, "out");

        ResNode root = findChildByName(this.resNode, sectionName);
        ResNode node = findChildByName(root, keyName);
        if (node == null) return false;

        int prim = (node.atom().flags() & ResNodeFlags.RESFLAG_MASK_ANY_OF_BASIC) >>> 1;

        if (prim != RESTYPE_INT32_ARRAY) {
            if (prim == RESTYPE_INT32) {
                out.clear();
                int v = ResNodeData.firstWord(node.atom().data());
                out.add((short) (v & 0xFF)); // matches *(byte *)&data -> (ushort)
                return true;
            }
            throw new IllegalStateException("Not an int array for [" + sectionName + "] " + keyName);
        }

        // binary-identical InHeap/InContainer: always use raw words
        out.clear();
        int off = ResNodeData.firstWord(node.atom().data());   // heapOffset
        int bytes = ResNodeData.secondWord(node.atom().data());  // byteSize
        int count = bytes >>> 2;

        if (off < 0 || off + (count * 4L) > heap.data().length) {
            throw new IllegalStateException("Heap range OOB for [" + sectionName + "] " + keyName);
        }

        var bb = ByteBuffer.wrap(heap.data()).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < count; i++) {
            short w = bb.getShort(off + i * 4); // matches *(word*)(heap + off + i*4)
            out.add(w);
        }
        return true;
    }

    /**
     * Native: ResInHeap::SetShortArray @004E7B2A.
     * Fully ported.
     */
    public void setShortArray(String sectionName, String keyName, List<Short> values) {
        Objects.requireNonNull(values, "values");
        ResNode node = findOrCreateIntArrayNode(sectionName, keyName);
        int offset = resizeIntArrayNode(node, values.size() * Integer.BYTES);
        ByteBuffer buffer = ByteBuffer.wrap(heap.data()).order(ByteOrder.LITTLE_ENDIAN);
        for (int index = 0; index < values.size(); index++) {
            buffer.putInt(offset + index * Integer.BYTES, Short.toUnsignedInt(values.get(index)));
        }
    }

    /**
     * Native support extracted from ResInHeap::CopyRootChildNamesToBuffer @004E725B and
     * ResInHeap::CopyRootChildNamesToStringArray @004E7335. Java returns the same ordered root-child names directly.
     * Fully ported.
     */
    public List<String> getRootChildNames() {
        List<String> names = new ArrayList<>();
        if (!(resNode.atom().data() instanceof ResContainerNode(int firstChildIndex, int childCount))) {
            return names;
        }
        int end = firstChildIndex + childCount;
        if (firstChildIndex < 0 || childCount < 0 || end > nodes.size()) {
            throw new IllegalStateException("Root child range OOB");
        }
        for (int i = firstChildIndex; i < end; i++) {
            names.add(nodes.get(i).getName());
        }
        return names;
    }

    /**
     * Native: ResInHeap::CopyRootChildNamesToStringArray @004E7335.
     * Fully ported.
     */
    public void copyRootChildNamesTo(List<String> out) {
        Objects.requireNonNull(out, "out");
        out.clear();
        out.addAll(getRootChildNames());
    }

    /**
     * Native: ResInHeap::GetByteArray @004E7397.
     * Fully ported.
     */
    public boolean getByteArray(String sectionName, String keyName, List<Integer> out) {
        Objects.requireNonNull(out, "out");

        ResNode root = findChildByName(this.resNode, sectionName);
        ResNode node = findChildByName(root, keyName);
        if (node == null) {
            return false;
        }

        int prim = (node.atom().flags() & ResNodeFlags.RESFLAG_MASK_ANY_OF_BASIC) >>> 1;
        if (prim == RESTYPE_INT32) {
            out.clear();
            out.add(Byte.toUnsignedInt((byte) ResNodeData.firstWord(node.atom().data())));
            return true;
        }
        if (prim != RESTYPE_INT32_ARRAY) {
            throw new IllegalStateException("Not an int array for [" + sectionName + "] " + keyName);
        }

        int off = ResNodeData.firstWord(node.atom().data());
        int bytes = ResNodeData.secondWord(node.atom().data());
        int count = bytes >>> 2;
        requireHeapRange(off, count * 4);
        out.clear();
        for (int i = 0; i < count; i++) {
            out.add(Byte.toUnsignedInt(heap.data()[off + i * 4]));
        }
        return true;
    }

    /**
     * Native: ResInHeap::GetStringArray @004E77C3.
     * Fully ported.
     */
    public boolean getStringArray(String sectionName, String keyName, List<String> out) {
        Objects.requireNonNull(out, "out");

        ResNode root = findChildByName(this.resNode, sectionName);
        ResNode node = findChildByName(root, keyName);
        if (node == null) {
            return false;
        }

        int prim = (node.atom().flags() & ResNodeFlags.RESFLAG_MASK_ANY_OF_BASIC) >>> 1;
        if (prim != RESTYPE_BLOB && prim != RESTYPE_STRING) {
            throw new IllegalStateException("Not a string array or string for [" + sectionName + "] " + keyName);
        }

        int off = ResNodeData.firstWord(node.atom().data());
        int bytes = ResNodeData.secondWord(node.atom().data());
        requireHeapRange(off, bytes);
        int expectedSize;
        int pos = off;
        if (prim == RESTYPE_STRING) {
            expectedSize = 1;
        } else {
            expectedSize = readI32Strict(off);
            pos += Integer.BYTES;
        }

        int end = off + bytes;
        out.clear();
        for (int i = 0; i < expectedSize; i++) {
            out.add("");
        }
        int index = 0;
        while (pos - off < bytes) {
            String value = readCStringFromHeap(pos, end);
            if (index < out.size()) {
                out.set(index, value);
            } else {
                out.add(value);
            }
            pos += value.length() + 1;
            index++;
        }
        return true;
    }

    /**
     * Native: ResInHeap::GetDoubleArray @004E78C7.
     * Fully ported.
     */
    public boolean getDoubleArray(String sectionName, String keyName, List<Double> out) {
        ResNode root = findChildByName(this.resNode, sectionName);
        ResNode node = findChildByName(root, keyName);
        if (node == null) {
            return false;
        }

        int prim = (node.atom().flags() & ResNodeFlags.RESFLAG_MASK_ANY_OF_BASIC) >>> 1;
        if (prim != RESTYPE_DOUBLE_ARRAY) {
            throw new IllegalStateException("Not a double array for [" + sectionName + "] " + keyName);
        }

        int off = ResNodeData.firstWord(node.atom().data());
        int bytes = ResNodeData.secondWord(node.atom().data());
        int count = bytes >>> 3;
        requireHeapRange(off, count * Double.BYTES);
        ByteBuffer buffer = ByteBuffer.wrap(heap.data()).order(ByteOrder.LITTLE_ENDIAN);
        out.clear();
        for (int i = 0; i < count; i++) {
            out.add(buffer.getDouble(off + i * Double.BYTES));
        }
        return true;
    }

    /**
     * Native: ResInHeap::GetString @004E6D8D.
     * Fully ported.
     */
    public String getString(String sectionName, String keyName, String defaultValue) {
        StringBuilder value = new StringBuilder(0x800);
        getValueAsString(sectionName, keyName, defaultValue, value, 0x800);
        return value.toString();
    }

    /**
     * Native: ResInHeap::SetString @004E6E2D.
     * Fully ported.
     */
    public void setString(String sectionName, String keyName, String value) {
        Objects.requireNonNull(sectionName, "sectionName");
        Objects.requireNonNull(keyName, "keyName");
        Objects.requireNonNull(value, "value");

        ResNode section = findChildByName(resNode, sectionName);
        ResNode key = findChildByName(section, keyName);
        if (key == null) {
            if (section == null) {
                section = createGroup(resNode.getName(), sectionName);
            }
            key = addChildToParent(Objects.requireNonNull(section, "section"), keyName);
            ResNode initializedKey = withAtom(key, new ResHeapNode(0, 0), 0);
            replaceNode(key, initializedKey);
            key = initializedKey;
            allocHeapOffset(ResNodeData.secondWord(key.atom().data()));
            section = findChildByName(resNode, sectionName);
            key = findChildByName(section, keyName);
        }

        if ((key.atom().flags() & ResNodeFlags.RESFLAG_MASK_ANY_OF_BASIC) != 0) {
            throw new IllegalStateException("not a string associated with specified keys");
        }

        byte[] bytes = value.getBytes(GameCharsets.GAME_TEXT);
        int byteSize = bytes.length + 1;
        int offset = ResNodeData.firstWord(key.atom().data());
        int allocatedSize = ResNodeData.secondWord(key.atom().data());
        if (allocatedSize < byteSize) {
            totalDataSize += allocatedSize;
            offset = allocHeapOffset(byteSize);
            section = findChildByName(resNode, sectionName);
            key = findChildByName(section, keyName);
            ResNode expandedKey = withAtom(key, new ResHeapNode(offset, byteSize), key.atom().flags());
            replaceNode(key, expandedKey);
        } else {
            totalDataSize += allocatedSize - byteSize;
        }

        System.arraycopy(bytes, 0, heap.data, offset, bytes.length);
        heap.data[offset + bytes.length] = 0;
    }

    /**
     * Native: ResInHeap::GetIntArray @004E7589.
     * Fully ported. Native leaves the destination untouched on missing keys, converts scalar `int` values through the low byte,
     * accepts short strings as an empty result, and copies heap-backed `int32[]` payloads into the destination accumulator.
     */
    public boolean getIntArray(String sectionName, String keyName, List<Integer> out) {
        Objects.requireNonNull(out, "out");

        ResNode root = findChildByName(this.resNode, sectionName);
        ResNode node = findChildByName(root, keyName);
        if (node == null) {
            return false;
        }

        int prim = (node.atom().flags() & ResNodeFlags.RESFLAG_MASK_ANY_OF_BASIC) >>> 1;
        if (prim == RESTYPE_INT32) {
            out.clear();
            out.add(Byte.toUnsignedInt((byte) ResNodeData.firstWord(node.atom().data())));
            return true;
        }
        if (prim == RESTYPE_STRING && ResNodeData.secondWord(node.atom().data()) < 2) {
            out.clear();
            return true;
        }
        if (prim != RESTYPE_INT32_ARRAY) {
            throw new IllegalStateException("Not an int array for [" + sectionName + "] " + keyName);
        }

        int off = ResNodeData.firstWord(node.atom().data());
        int bytes = ResNodeData.secondWord(node.atom().data());
        int count = bytes >>> 2;
        if (off < 0 || off + (count * 4L) > heap.data().length) {
            throw new IllegalStateException("Heap range OOB for [" + sectionName + "] " + keyName);
        }

        ByteBuffer buffer = ByteBuffer.wrap(heap.data()).order(ByteOrder.LITTLE_ENDIAN);
        out.clear();
        for (int i = 0; i < count; i++) {
            out.add(buffer.getInt(off + i * 4));
        }
        return true;
    }

    /**
     * Native: ResInHeap::SetIntArray @004E7CC2.
     * Fully ported.
     */
    public void setIntArray(String sectionName, String keyName, List<Integer> values) {
        Objects.requireNonNull(values, "values");
        ResNode node = findOrCreateIntArrayNode(sectionName, keyName);
        int offset = resizeIntArrayNode(node, values.size() * Integer.BYTES);
        ByteBuffer buffer = ByteBuffer.wrap(heap.data()).order(ByteOrder.LITTLE_ENDIAN);
        for (int index = 0; index < values.size(); index++) {
            buffer.putInt(offset + index * Integer.BYTES, values.get(index));
        }
    }

    /**
     * Native support extracted from ResInHeap::SetShortArray @004E7B2A and
     * ResInHeap::SetIntArray @004E7CC2.
     */
    private ResNode findOrCreateIntArrayNode(String sectionName, String keyName) {
        ResNode section = findChildByName(resNode, sectionName);
        ResNode node = findChildByName(section, keyName);
        if (node == null) {
            if (section == null) {
                section = createGroup(resNode.getName(), sectionName);
            }
            node = addChildToParent(Objects.requireNonNull(section, "section"), keyName);
            ResNode initializedNode = withAtom(node, new ResHeapNode(0, 0), ResNodeFlags.RESFLAG_TYPE_INT32_ARRAY);
            replaceNode(node, initializedNode);
            node = initializedNode;
        }
        if ((node.atom().flags() & ResNodeFlags.RESFLAG_MASK_ANY_OF_BASIC) != ResNodeFlags.RESFLAG_TYPE_INT32_ARRAY) {
            throw new IllegalStateException("Value is not INT32_ARRAY for [" + sectionName + "] " + keyName);
        }
        return node;
    }

    /**
     * Native support extracted from ResInHeap::SetShortArray @004E7B2A and
     * ResInHeap::SetIntArray @004E7CC2.
     */
    private int resizeIntArrayNode(ResNode node, int byteSize) {
        int oldSize = ResNodeData.secondWord(node.atom().data());
        int offset = ResNodeData.firstWord(node.atom().data());
        if (oldSize < byteSize) {
            totalDataSize += oldSize;
            offset = allocHeapOffset(byteSize);
            replaceNode(node, withAtom(node, new ResHeapNode(offset, byteSize), node.atom().flags()));
        } else {
            totalDataSize += oldSize - byteSize;
        }
        return offset;
    }

    /**
     * Native: ResInHeap::GetArray @004E76A6.
     * Fully ported.
     */
    public boolean getArray(String sectionName, String keyName, List<Integer> out) {
        return getIntArray(sectionName, keyName, out);
    }

    /**
     * Native: ResInHeap::GetRect @004E839A.
     * Fully ported.
     */
    public boolean getRect(String sectionName, String keyName, CRect out) {
        ResNode root = findChildByName(this.resNode, sectionName);
        ResNode node = findChildByName(root, keyName);
        if (node == null) {
            return false;
        }

        int prim = (node.atom().flags() & ResNodeFlags.RESFLAG_MASK_ANY_OF_BASIC) >>> 1;
        if (prim != RESTYPE_INT32_ARRAY || ResNodeData.secondWord(node.atom().data()) != 0x10) {
            throw new IllegalStateException("Not an int array with size 4 for [" + sectionName + "] " + keyName);
        }

        int off = ResNodeData.firstWord(node.atom().data());
        requireHeapRange(off, Integer.BYTES * 4);
        ByteBuffer buffer = ByteBuffer.wrap(heap.data()).order(ByteOrder.LITTLE_ENDIAN);
        int left = buffer.getInt(off);
        int top = buffer.getInt(off + Integer.BYTES);
        int width = buffer.getInt(off + Integer.BYTES * 2);
        int height = buffer.getInt(off + Integer.BYTES * 3);
        out.set(left, top, left + width, top + height);
        return true;
    }

    // not ported.
    private static ByteBuffer readRemaining(SeekableByteChannel ch) throws IOException {
        long remaining = ch.size() - ch.position();
        if (remaining < 0) {
            throw new IOException("Negative remaining bytes: " + remaining);
        }
        if (remaining > Integer.MAX_VALUE) {
            throw new IOException("Resource too large: " + remaining);
        }
        ByteBuffer buffer = ByteBuffer.allocate((int) remaining);
        while (buffer.hasRemaining()) {
            int n = ch.read(buffer);
            if (n < 0) throw new IOException("Unexpected EOF");
        }
        return buffer.flip();
    }

    /**
     * Native: ResInHeap::GetValueAsString @004E69C2.
     * Fully ported.
     */
    public int getValueAsString(String sectionName,
                                String keyName,
                                String defaultValue,
                                StringBuilder destBuffer,
                                int destSize) {
        try { //this is intentional, just to globally mutate destBuffer, replacing  "\\" to "/"
            Objects.requireNonNull(destBuffer, "destBuffer");
            destBuffer.setLength(0);

            if (destSize <= 0) return 0;
            if (defaultValue == null) defaultValue = "";

            ResNode section = findChildByName(this.resNode, sectionName);
            ResNode key = findChildByName(section, keyName);

            if (key == null) {
                appendTrunc(destBuffer, defaultValue, destSize);
                return destBuffer.length();
            }

            int type = (key.atom().flags() & ResNodeFlags.RESFLAG_MASK_ANY_OF_BASIC) >>> 1;

            switch (type) {
                case RESTYPE_STRING -> {
                    int off = firstWord(key.atom().data());
                    int len = secondWord(key.atom().data());
                    appendHeapString(destBuffer, off, len, destSize);
                }
                case RESTYPE_INT32 -> {
                    // C uses InFile.offset (low word)
                    int v = firstWord(key.atom().data());
                    appendTrunc(destBuffer, Integer.toString(v), destSize);
                }
                case RESTYPE_DOUBLE -> {
                    // NOTE: binary behavior looks buggy: it prints %f then overwrites with itoa(InFile.offset).
                    int v = firstWord(key.atom().data());
                    appendTrunc(destBuffer, Integer.toString(v), destSize);
                }
                case RESTYPE_INT32_ARRAY -> {
                    int off = firstWord(key.atom().data());
                    int bytes = secondWord(key.atom().data());
                    appendInt32Array(destBuffer, off, bytes, destSize);
                }
                case RESTYPE_BLOB -> {
                    int off = firstWord(key.atom().data());
                    int bytes = secondWord(key.atom().data());
                    appendBlob(destBuffer, off, bytes, destSize);
                }
                case RESTYPE_DOUBLE_ARRAY -> {
                    int off = firstWord(key.atom().data());
                    int bytes = secondWord(key.atom().data());
                    appendDoubleArray(destBuffer, off, bytes, destSize);
                }
                default -> throw new IllegalArgumentException("UNKNOWN TYPE: " + type);
            }

            return destBuffer.length();
        }finally {
            for (int i = 0; i < destBuffer.length() - 1; i++) {
                if (destBuffer.charAt(i) == '\\'){
                    destBuffer.setCharAt(i,'/');
                }
            }
            //System.out.println(destBuffer);
        }
    }

    /**
     * Native: ResInHeap::Load @004E4C75.
     * Fully ported.
     */
    public static ResInHeap load(String... path) throws Exception {
        return load(Resources.path(path));
    }

    /**
     * Native: ResInHeap::Load @004E4C75 (path wrapper).
     * Fully ported.
     */
    public static ResInHeap load(String filename) throws Exception {
        return read(CGameFile.getDataFor(filename), -1, getPlainFilename(filename));
    }

    /**
     * Native: ResInHeap::TextToReg @004E4EC3.
     * Fully ported.
     */
    public ResInHeap textToReg(String srcFile, String dstFile) throws IOException {
        String outputFileName = resolveTextImportRegistryFileName(dstFile);
        byte[] textBytes = Files.readAllBytes(Path.of(srcFile));
        resetForTextImport(outputFileName, textBytes);
        String text = new String(textBytes, GameCharsets.GAME_TEXT);
        createTextImportGroups(text);
        importTextRegistryEntries(text);
        totalDataSize = 0;
        writeAtPosition(Path.of(outputFileName), -1);
        return this;
    }

    /**
     * Native support extracted from ResInHeap::TextToReg @004E4EC3 `.reg` suffix selection.
     */
    private static String resolveTextImportRegistryFileName(String dstFile) {
        return dstFile.indexOf('.') < 0 ? dstFile + ".reg" : dstFile;
    }

    /**
     * Native support extracted from ResInHeap::TextToReg @004E4EC3 root/header/heap initialization.
     */
    private void resetForTextImport(String outputFileName, byte[] textBytes) {
        resNode = new ResNode(
                new ResAtom(
                        0x31415926,
                        new ResContainerNode(0, 0),
                        ResNodeFlags.RESFLAG_IS_CONTAINER
                ),
                Utils.extractRootName(outputFileName)
        );
        nodes.clear();
        nodesCount = 0;
        nodesCap = countLineFeeds(textBytes) + 1;
        pInsertion = null;
        totalDataSize = 0;
        heap.used = 0;
        heap.capacity = textBytes.length * 2 + 2;
        heap.data = new byte[heap.capacity];
    }

    /**
     * Native support extracted from ResInHeap::TextToReg @004E4EC3 source line counting for nodesCap.
     */
    private static int countLineFeeds(byte[] textBytes) {
        int count = 0;
        for (byte textByte : textBytes) {
            if (textByte == '\n') {
                count++;
            }
        }
        return count;
    }

    /**
     * Native support extracted from ResInHeap::TextToReg @004E4EC3 first pass over `[section]` headers.
     */
    private void createTextImportGroups(String text) {
        int searchIndex = 0;
        while ((searchIndex = text.indexOf('[', searchIndex)) >= 0) {
            if (searchIndex == 0 || text.charAt(searchIndex - 1) == '\n') {
                int closeIndex = text.indexOf(']', searchIndex + 1);
                if (closeIndex < 0) {
                    throwRegistryParseException("Missing closing ']' in registry text section");
                }
                createGroup(resNode.getName(), text.substring(searchIndex + 1, closeIndex));
            }
            searchIndex++;
        }
    }

    /**
     * Native support extracted from ResInHeap::TextToReg @004E4EC3 second pass over section entries.
     */
    private void importTextRegistryEntries(String text) {
        int cursor = text.indexOf('[');
        if (cursor < 0) {
            return;
        }

        int sectionOffset = 0;
        while (true) {
            ResContainerNode rootContainer = (ResContainerNode) resNode.atom().data();
            if (sectionOffset >= rootContainer.childCount()) {
                return;
            }
            ResNode section = nodes.get(rootContainer.firstChildIndex() + sectionOffset);
            while (true) {
                int lineFeed = text.indexOf('\n', cursor);
                if (lineFeed < 0) {
                    return;
                }
                cursor = lineFeed + 1;
                if (cursor >= text.length() || text.charAt(cursor) == '[') {
                    break;
                }
                while (cursor < text.length() && !isNativePrintable(text.charAt(cursor))) {
                    cursor++;
                }
                if (cursor >= text.length() || text.charAt(cursor) == '[') {
                    break;
                }
                if (text.charAt(cursor) != ';') {
                    cursor = importTextRegistryEntry(section.getName(), text, cursor);
                }
            }
            sectionOffset++;
        }
    }

    /**
     * Native support extracted from ResInHeap::TextToReg @004E4EC3 per-line key/value parsing.
     */
    private int importTextRegistryEntry(String sectionName, String text, int lineStart) {
        int equalsIndex = text.indexOf('=', lineStart);
        if (equalsIndex < 0) {
            throwRegistryParseException(String.format("= missed, check the section %s", sectionName));
        }

        int keyEnd = Math.min(equalsIndex, lineStart + 0x0F);
        String keyName = trimTrailingNativeWhitespace(text.substring(lineStart, keyEnd));
        int valueStart = equalsIndex + 1;
        int carriageReturn = text.indexOf('\r', valueStart);
        int valueEnd = carriageReturn < 0 ? text.length() : carriageReturn;
        String value = text.substring(valueStart, valueEnd);
        int valueClassification = classifyTextRegistryValue(value);

        ResNode section = findChildByName(resNode, sectionName);
        ResNode entry = findChildByName(section, keyName);
        if (entry == null) {
            addTextRegistryEntry(section, keyName, value, valueClassification);
        } else {
            appendTextRegistryStringEntry(sectionName, keyName, entry, value, valueClassification);
        }
        return valueStart;
    }

    /**
     * Native support extracted from ResInHeap::TextToReg @004E4EC3 initial key creation by inferred value type.
     */
    private void addTextRegistryEntry(ResNode section, String keyName, String value, int valueClassification) {
        ResNode entry = addChildToParent(Objects.requireNonNull(section, "section"), keyName);
        if (isTextRegistryInt(valueClassification)) {
            replaceNode(entry, withAtom(entry, new ResIntNode(parseNativeInt(value)), ResNodeFlags.RESFLAG_TYPE_INT32));
            return;
        }
        if (isTextRegistryIntArray(valueClassification)) {
            writeTextRegistryIntArray(entry, value);
            return;
        }
        if (isTextRegistryDouble(valueClassification)) {
            replaceNode(entry, withAtom(
                    entry,
                    new ResDoubleNode(parseNativeDouble(value)),
                    ResNodeFlags.RESFLAG_TYPE_DOUBLE
            ));
            return;
        }
        if (isTextRegistryDoubleArray(valueClassification)) {
            writeTextRegistryDoubleArray(entry, value);
            return;
        }
        writeTextRegistryString(entry, value);
    }

    /**
     * Native support extracted from ResInHeap::TextToReg @004E4EC3 repeated string-key handling.
     */
    private void appendTextRegistryStringEntry(
            String sectionName,
            String keyName,
            ResNode entry,
            String value,
            int valueClassification
    ) {
        int type = (entry.atom().flags() & ResNodeFlags.RESFLAG_MASK_ANY_OF_BASIC) >>> 1;
        if (type != RESTYPE_STRING && type != RESTYPE_BLOB) {
            throwRegistryParseException(String.format(
                    "The key %s duplicated in the section %s",
                    keyName,
                    sectionName
            ));
        }

        int offset = firstWord(entry.atom().data());
        int byteSize = secondWord(entry.atom().data());
        if (heap.used != offset + byteSize) {
            throwRegistryParseException(String.format(
                    "Multi-string keys should place together: %s in section %s",
                    keyName,
                    sectionName
            ));
        }
        if (!isTextRegistryStringish(valueClassification) && !value.isEmpty()) {
            throwRegistryParseException(String.format(
                    "Types of multi-string keys should be string: %s in section %s",
                    keyName,
                    sectionName
            ));
        }

        byte[] valueBytes = value.getBytes(GameCharsets.GAME_TEXT);
        int appendSize = valueBytes.length + 1;
        if (type == RESTYPE_STRING) {
            ensureTextImportHeapCapacity(Integer.BYTES + appendSize);
            System.arraycopy(heap.data, offset, heap.data, offset + Integer.BYTES, byteSize);
            heap.used += Integer.BYTES;
            byteSize += Integer.BYTES;
            writeHeapInt(offset, 1);
        } else {
            ensureTextImportHeapCapacity(appendSize);
        }

        writeHeapInt(offset, readI32Strict(offset) + 1);
        System.arraycopy(valueBytes, 0, heap.data, heap.used, valueBytes.length);
        heap.data[heap.used + valueBytes.length] = 0;
        heap.used += appendSize;
        byteSize += appendSize;
        replaceNode(entry, withAtom(entry, new ResHeapNode(offset, byteSize), ResNodeFlags.RESFLAG_TYPE_BLOB));
    }

    /**
     * Native support extracted from ResInHeap::TextToReg @004E4EC3 string value heap writes.
     */
    private void writeTextRegistryString(ResNode entry, String value) {
        byte[] valueBytes = value.getBytes(GameCharsets.GAME_TEXT);
        int byteSize = valueBytes.length + 1;
        ensureTextImportHeapCapacity(byteSize);
        int offset = heap.used;
        System.arraycopy(valueBytes, 0, heap.data, offset, valueBytes.length);
        heap.data[offset + valueBytes.length] = 0;
        heap.used += byteSize;
        replaceNode(entry, withAtom(entry, new ResHeapNode(offset, byteSize), 0));
    }

    /**
     * Native support extracted from ResInHeap::TextToReg @004E4EC3 int-array heap writes.
     */
    private void writeTextRegistryIntArray(ResNode entry, String value) {
        String[] parts = value.split(",", -1);
        int byteSize = parts.length * Integer.BYTES;
        ensureTextImportHeapCapacity(byteSize);
        int offset = heap.used;
        for (int index = 0; index < parts.length; index++) {
            writeHeapInt(offset + index * Integer.BYTES, parseNativeInt(parts[index]));
        }
        heap.used += byteSize;
        replaceNode(entry, withAtom(
                entry,
                new ResHeapNode(offset, byteSize),
                ResNodeFlags.RESFLAG_TYPE_INT32_ARRAY
        ));
    }

    /**
     * Native support extracted from ResInHeap::TextToReg @004E4EC3 double-array heap writes.
     */
    private void writeTextRegistryDoubleArray(ResNode entry, String value) {
        String[] parts = value.split(",", -1);
        int byteSize = parts.length * Double.BYTES;
        ensureTextImportHeapCapacity(byteSize);
        int offset = heap.used;
        for (int index = 0; index < parts.length; index++) {
            writeHeapDouble(offset + index * Double.BYTES, parseNativeDouble(parts[index]));
        }
        heap.used += byteSize;
        replaceNode(entry, withAtom(
                entry,
                new ResHeapNode(offset, byteSize),
                ResNodeFlags.RESFLAG_TYPE_DOUBLE_ARRAY
        ));
    }

    /**
     * Native support extracted from ResInHeap::TextToReg @004E4EC3 heap pointer writes.
     */
    private void ensureTextImportHeapCapacity(int appendSize) {
        if (heap.capacity < heap.used + appendSize) {
            growHeap(appendSize);
        }
    }

    /**
     * Native support extracted from ResInHeap::TextToReg @004E4EC3 little-endian int writes.
     */
    private void writeHeapInt(int offset, int value) {
        ByteBuffer.wrap(heap.data).order(ByteOrder.LITTLE_ENDIAN).putInt(offset, value);
    }

    /**
     * Native support extracted from ResInHeap::TextToReg @004E4EC3 little-endian double writes.
     */
    private void writeHeapDouble(int offset, double value) {
        ByteBuffer.wrap(heap.data).order(ByteOrder.LITTLE_ENDIAN).putDouble(offset, value);
    }

    /**
     * Native support extracted from ResInHeap::TextToReg @004E4EC3 numeric/string type inference.
     */
    private static int classifyTextRegistryValue(String value) {
        int classification = 0;
        int scanIndex = 0;
        if (!value.isEmpty() && value.charAt(0) == '-') {
            scanIndex = 1;
        }
        while (scanIndex < value.length()) {
            char ch = value.charAt(scanIndex);
            if (ch == ',' && (classification & TEXT_VALUE_HAS_OTHER) == 0) {
                classification |= TEXT_VALUE_HAS_COMMA;
            } else if (ch >= '0' && ch <= '9') {
                classification |= TEXT_VALUE_HAS_DIGIT;
            } else if (ch == '.' || ch == 'e' || ch == 'E') {
                classification |= TEXT_VALUE_HAS_FLOAT_MARKER;
            } else if (ch == ' ' || ch == '\t') {
                classification |= TEXT_VALUE_HAS_WHITESPACE;
            } else {
                classification |= TEXT_VALUE_HAS_OTHER;
            }
            scanIndex++;
        }
        if (((classification & (TEXT_VALUE_HAS_FLOAT_MARKER | TEXT_VALUE_HAS_DIGIT)) == 0
                && (classification & TEXT_VALUE_HAS_WHITESPACE) != 0)
                || (classification & TEXT_VALUE_HAS_OTHER) != 0) {
            classification |= TEXT_VALUE_IS_STRINGISH;
        }
        return classification;
    }

    /**
     * Native support extracted from ResInHeap::TextToReg @004E4EC3 scalar int inference.
     */
    private static boolean isTextRegistryInt(int classification) {
        return (classification & TEXT_VALUE_HAS_DIGIT) != 0
                && (classification & (TEXT_VALUE_HAS_FLOAT_MARKER
                | TEXT_VALUE_IS_STRINGISH
                | TEXT_VALUE_HAS_COMMA)) == 0;
    }

    /**
     * Native support extracted from ResInHeap::TextToReg @004E4EC3 int-array inference.
     */
    private static boolean isTextRegistryIntArray(int classification) {
        return (classification & TEXT_VALUE_HAS_DIGIT) != 0
                && (classification & TEXT_VALUE_HAS_COMMA) != 0
                && (classification & (TEXT_VALUE_HAS_FLOAT_MARKER | TEXT_VALUE_IS_STRINGISH)) == 0;
    }

    /**
     * Native support extracted from ResInHeap::TextToReg @004E4EC3 scalar double inference.
     */
    private static boolean isTextRegistryDouble(int classification) {
        return (classification & (TEXT_VALUE_HAS_DIGIT | TEXT_VALUE_HAS_FLOAT_MARKER)) ==
                (TEXT_VALUE_HAS_DIGIT | TEXT_VALUE_HAS_FLOAT_MARKER)
                && (classification & (TEXT_VALUE_IS_STRINGISH | TEXT_VALUE_HAS_COMMA)) == 0;
    }

    /**
     * Native support extracted from ResInHeap::TextToReg @004E4EC3 double-array inference.
     */
    private static boolean isTextRegistryDoubleArray(int classification) {
        return (classification & (TEXT_VALUE_HAS_DIGIT | TEXT_VALUE_HAS_FLOAT_MARKER | TEXT_VALUE_HAS_COMMA)) ==
                (TEXT_VALUE_HAS_DIGIT | TEXT_VALUE_HAS_FLOAT_MARKER | TEXT_VALUE_HAS_COMMA)
                && (classification & TEXT_VALUE_IS_STRINGISH) == 0;
    }

    /**
     * Native support extracted from ResInHeap::TextToReg @004E4EC3 duplicate string-key validation.
     */
    private static boolean isTextRegistryStringish(int classification) {
        return (classification & TEXT_VALUE_IS_STRINGISH) != 0;
    }

    /**
     * Native support extracted from ResInHeap::TextToReg @004E4EC3 `atoi` calls.
     */
    private static int parseNativeInt(String value) {
        int index = skipNativeWhitespace(value, 0);
        int sign = 1;
        if (index < value.length() && value.charAt(index) == '-') {
            sign = -1;
            index++;
        }
        int result = 0;
        while (index < value.length()) {
            char ch = value.charAt(index);
            if (ch < '0' || ch > '9') {
                break;
            }
            result = result * 10 + (ch - '0');
            index++;
        }
        return result * sign;
    }

    /**
     * Native support extracted from ResInHeap::TextToReg @004E4EC3 `atof` calls.
     */
    private static double parseNativeDouble(String value) {
        int index = skipNativeWhitespace(value, 0);
        int start = index;
        if (index < value.length() && value.charAt(index) == '-') {
            index++;
        }
        boolean hasDigit = false;
        while (index < value.length() && Character.isDigit(value.charAt(index))) {
            hasDigit = true;
            index++;
        }
        if (index < value.length() && value.charAt(index) == '.') {
            index++;
            while (index < value.length() && Character.isDigit(value.charAt(index))) {
                hasDigit = true;
                index++;
            }
        }
        if (hasDigit && index < value.length() && (value.charAt(index) == 'e' || value.charAt(index) == 'E')) {
            int exponentStart = index;
            index++;
            if (index < value.length() && (value.charAt(index) == '-' || value.charAt(index) == '+')) {
                index++;
            }
            int exponentDigits = index;
            while (index < value.length() && Character.isDigit(value.charAt(index))) {
                index++;
            }
            if (exponentDigits == index) {
                index = exponentStart;
            }
        }
        return hasDigit ? Double.parseDouble(value.substring(start, index)) : 0.0;
    }

    /**
     * Native support extracted from ResInHeap::TextToReg @004E4EC3 `isprint` line skipping.
     */
    private static boolean isNativePrintable(char ch) {
        return ch >= 0x20 && ch != 0x7F;
    }

    /**
     * Native support extracted from ResInHeap::TextToReg @004E4EC3 trailing key whitespace trimming.
     */
    private static String trimTrailingNativeWhitespace(String value) {
        int end = value.length();
        while (end > 0 && isNativeWhitespace(value.charAt(end - 1))) {
            end--;
        }
        return value.substring(0, end);
    }

    /**
     * Native support extracted from ResInHeap::TextToReg @004E4EC3 `isspace` and `atoi`/`atof` whitespace handling.
     */
    private static int skipNativeWhitespace(String value, int index) {
        while (index < value.length() && isNativeWhitespace(value.charAt(index))) {
            index++;
        }
        return index;
    }

    /**
     * Native support extracted from ResInHeap::TextToReg @004E4EC3 `isspace`.
     */
    private static boolean isNativeWhitespace(char ch) {
        return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r' || ch == '\f' || ch == 0x0B;
    }

    /**
     * Native support extracted from the CGameFile seek/read sequence in CMainWindow::isSavedGameInBattle @0048C214,
     * CMainWindow::LoadSaveGame @0048C2E9, and CMainWindow::runSessionBootstrap @0048C8A3.
     */
    public static ByteBuffer openSavedGameResourceState(String saveFileName) throws IOException {
        ByteBuffer saveBuffer = ByteBuffer.wrap(Files.readAllBytes(SavedGameFiles.resolvePath(saveFileName)))
                .order(ByteOrder.LITTLE_ENDIAN);
        int resourceOffset = saveBuffer.getInt(4);
        saveBuffer.position(resourceOffset + 0x100);
        return saveBuffer;
    }

    /**
     * Native support extracted from ResInHeap::Load @004E4C75 root-name extraction.
     */
    private static String getPlainFilename(String path) {
        if (path == null) {
            return "";
        }
        return Utils.extractRootName(path);
    }

    // --- helpers --- not ported.

    /**
     * Native: ResInHeap::OptimizeHeap @004E60B4.
     * Fully ported.
     */
    private void optimizeHeap() {
        pInsertion = null;
        totalDataSize = 0;

        List<ResNode> heapNodes = new ArrayList<>();
        heapNodes.add(new ResNode(new ResAtom(0, new ResHeapNode(0, 0), 0), ""));
        for (ResNode node : nodes) {
            int flags = node.atom().flags();
            if ((flags & (ResNodeFlags.RESFLAG_VISITED | ResNodeFlags.RESFLAG_IS_CONTAINER)) != 0) {
                continue;
            }
            int type = (flags & ResNodeFlags.RESFLAG_MASK_ANY_OF_BASIC) >>> 1;
            if (type != RESTYPE_INT32 && type != RESTYPE_DOUBLE) {
                heapNodes.add(node);
            }
        }

        if (heapNodes.size() > 1) {
            heapNodes.sort(Res::compareNodesByData);
            for (int i = 0; i < heapNodes.size() - 1; i++) {
                ResNode previous = heapNodes.get(i);
                ResNode current = heapNodes.get(i + 1);
                int currentOffset = ResNodeData.firstWord(current.atom().data());
                int currentSize = ResNodeData.secondWord(current.atom().data());
                int gap = currentOffset
                        - (ResNodeData.firstWord(previous.atom().data())
                        + ResNodeData.secondWord(previous.atom().data()));
                if (gap != 0) {
                    int compactedOffset = currentOffset - gap;
                    System.arraycopy(heap.data, currentOffset, heap.data, compactedOffset, currentSize);
                    ResNode compactedNode = withAtom(
                            current,
                            new ResHeapNode(compactedOffset, currentSize),
                            current.atom().flags()
                    );
                    replaceNode(current, compactedNode);
                    heapNodes.set(i + 1, compactedNode);
                }
            }
        }

        ResNode lastNode = heapNodes.getLast();
        heap.used = ResNodeData.firstWord(lastNode.atom().data())
                + ResNodeData.secondWord(lastNode.atom().data());
        totalDataSize = 0;
    }

    /**
     * Native: ResInHeap::AllocHeap @004E62A6.
     * Fully ported. Java returns the heap offset equivalent of the native heap pointer.
     */
    private int allocHeapOffset(int size) {
        if (heap.capacity <= heap.used + size) {
            if ((heap.used >> 3) < totalDataSize) {
                optimizeHeap();
                if (heap.capacity <= heap.used + size) {
                    growHeap(size);
                }
            } else {
                growHeap(size);
            }
        }

        int offset = heap.used;
        heap.used += size;
        return offset;
    }

    /**
     * Native support extracted from ResInHeap::AllocHeap @004E62A6 realloc growth.
     */
    private void growHeap(int size) {
        heap.capacity = heap.capacity + size + (heap.capacity >> 3);
        heap.data = Arrays.copyOf(heap.data, heap.capacity);
    }

    /**
     * Native support extracted from ResInHeap::GetValueAsString @004E69C2 heap-backed string copy.
     */
    private void appendHeapString(StringBuilder out, int off, int len, int destSize) {
        if (off < 0 || len <= 0 || off >= heap.data().length) return;
        int n = Math.min(len, heap.data().length - off);
        n = Math.min(n, destSize); // mimic min(destSize, childCount)
        int byteLength = 0;
        while (byteLength < n && heap.data()[off + byteLength] != 0) {
            byteLength++;
        }
        String value = new String(heap.data(), off, byteLength, GameCharsets.GAME_TEXT);
        int remaining = Math.max(0, destSize - 1 - out.length());
        out.append(value, 0, Math.min(value.length(), remaining));
    }

    // not ported.
    private void appendInt32Array(StringBuilder out, int off, int bytes, int destSize) {
        if (off < 0 || bytes <= 0) return;
        int count = bytes >>> 2;
        for (int i = 0; i < count; i++) {
            int v = readI32FromHeap(off + i * 4);
            String part = Integer.toString(v) + ((i < count - 1) ? "," : "");
            if (out.length() + part.length() > destSize - 1) break;
            out.append(part);
        }
    }

    // not ported.
    private void appendDoubleArray(StringBuilder out, int off, int bytes, int destSize) {
        if (off < 0 || bytes <= 0) return;
        int count = bytes >>> 3;
        for (int i = 0; i < count; i++) {
            double v = readF64FromHeap(off + i * 8);
            String part = String.format(Locale.US, "%f", v) + ((i < count - 1) ? "," : "");
            if (out.length() + part.length() > destSize - 1) break;
            out.append(part);
        }
    }

    // not ported.
    private void appendBlob(StringBuilder out, int off, int bytes, int destSize) {
        // memcpy(dest, heap+off+4, min(destSize-2, bytes-4)); then set two NULs
        int src = off + 4;
        int max = Math.min(destSize - 2, bytes - 4);
        if (max <= 0 || src < 0 || src >= heap.data().length) return;

        int n = Math.min(max, heap.data().length - src);
        int byteLength = 0;
        while (byteLength < n && heap.data()[src + byteLength] != 0) {
            byteLength++;
        }
        out.append(new String(heap.data(), src, byteLength, GameCharsets.GAME_TEXT));
    }

    // not ported.
    private static void appendTrunc(StringBuilder out, String s, int destSize) {
        int max = Math.max(0, destSize - 1);
        if (s.length() <= max) out.append(s);
        else out.append(s, 0, max);
    }

    /**
     * Native support extracted from ResInHeap::GetByteArray @004E7397 and ResInHeap::GetStringArray @004E77C3.
     */
    private void requireHeapRange(int off, int bytes) {
        if (off < 0 || bytes < 0 || off + (long) bytes > heap.data().length) {
            throw new IllegalStateException("Heap range OOB");
        }
    }

    /**
     * Native support extracted from ResInHeap::GetStringArray @004E77C3.
     */
    private int readI32Strict(int off) {
        requireHeapRange(off, Integer.BYTES);
        return ByteBuffer.wrap(heap.data()).order(ByteOrder.LITTLE_ENDIAN).getInt(off);
    }

    /**
     * Native support extracted from ResInHeap::GetStringArray @004E77C3.
     */
    private String readCStringFromHeap(int off, int endExclusive) {
        requireHeapRange(off, Math.max(0, endExclusive - off));
        int pos = off;
        while (pos < endExclusive && heap.data()[pos] != 0) {
            pos++;
        }
        return new String(heap.data(), off, pos - off, GameCharsets.GAME_TEXT);
    }

    // not ported.
    private int readI32FromHeap(int off) {
        if (off < 0 || off + 4 > heap.data().length) return 0;
        return (heap.data()[off] & 0xff)
                | ((heap.data()[off + 1] & 0xff) << 8)
                | ((heap.data()[off + 2] & 0xff) << 16)
                | ((heap.data()[off + 3] & 0xff) << 24);
    }

    // not ported.
    private double readF64FromHeap(int off) {
        if (off < 0 || off + 8 > heap.data().length) return 0.0;
        long lo = Integer.toUnsignedLong(readI32FromHeap(off));
        long hi = Integer.toUnsignedLong(readI32FromHeap(off + 4));
        return Double.longBitsToDouble(lo | (hi << 32));
    }
}
