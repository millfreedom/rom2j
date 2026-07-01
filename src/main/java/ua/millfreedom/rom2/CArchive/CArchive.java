package ua.millfreedom.rom2.CArchive;

import lombok.SneakyThrows;
import ua.millfreedom.rom2.CFile.CFile;
import ua.millfreedom.rom2.ClassNameIndex;
import ua.millfreedom.rom2.data.RWAdaptor;
import ua.millfreedom.rom2.data.ByteBufferAdaptor;
import ua.millfreedom.rom2.res.CGameFile;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;


public final class CArchive implements AutoCloseable {
    private static final int W_NEW_CLASS_TAG = 0xFFFF;
    private static final int W_CLASS_TAG = 0x8000;
    private static final int W_BIG_OBJECT_TAG = 0x7FFF;
    private static final int DW_BIG_CLASS_TAG = 0x8000_0000;
    private static final int N_MAX_MAP_COUNT = 0x3FFFFFFE;
    // Native VC97 CRuntimeClass::Store writes m_wSchema; ROM2 save runtime classes use schema 1.
    private static final int SINGLE_SCHEMA = 1;

    private final RWAdaptor adaptor;
    private boolean isStoring;
    private ArrayList<Object> loadRefs;
    private IdentityHashMap<Object, Integer> storeRefs;
    private int nextStoreRefIndex;
    private int objectSchema = -1;

    public static CArchive forReadingFromFile(String fileName) throws Exception {
        return new CArchive(fileName, false);
    }

    public static CArchive forReadingFromRes(String path) throws Exception {
        return new CArchive(new ByteBufferAdaptor(CGameFile.getDataFor(path)), false);
    }

    public static CArchive forReadingFromBytes(byte[] data) {
        return forReadingFromBytes(data, ByteOrder.LITTLE_ENDIAN);
    }

    public static CArchive forReadingFromBytes(byte[] data, ByteOrder order) {
        ByteBufferAdaptor adaptor = new ByteBufferAdaptor(ByteBuffer.wrap(data));
        adaptor.setByteOrder(order);
        return new CArchive(adaptor, false);
    }

    /**
     * not ported. Java support for memory-backed CArchive storing.
     */
    public static CArchive forWritingToBytes(ByteArrayOutputStream output) {
        return forWritingToBytes(output, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * not ported. Java support for memory-backed CArchive storing.
     */
    public static CArchive forWritingToBytes(ByteArrayOutputStream output, ByteOrder order) {
        ByteBufferAdaptor adaptor = ByteBufferAdaptor.forWriting(output, order);
        return new CArchive(adaptor, true);
    }

    private CArchive(String fileName, boolean isStoring) throws Exception {
        this(new CFile(fileName), isStoring);
    }

    private CArchive(RWAdaptor adaptor, boolean isStoring) {
        this.adaptor = Objects.requireNonNull(adaptor);
        this.isStoring = isStoring;
    }

    @Override
    public void close() throws Exception {
        adaptor.close();
    }

    public boolean isStoring() {
        return isStoring;
    }

    public ByteOrder getByteOrder() {
        return adaptor.getByteOrder();
    }

    public void setByteOrder(ByteOrder bo) {
        adaptor.setByteOrder(bo);
    }


    // ---- primitives (delegated to CFile) ----
    public int readInt() throws IOException {
        return adaptor.readInt();
    }

    public void writeInt(int v) throws IOException {
        adaptor.writeInt(v);
    }

    public short readShort() throws IOException {
        return adaptor.readShort();
    }

    public int readUShort() throws IOException {
        return adaptor.readUShort();
    }

    public byte readByte() throws IOException {
        return adaptor.readByte();
    }

    public void writeByte(int v) throws IOException {
        adaptor.writeByte(v);
    }

    /**
     * Native support corresponding to CArchive::Read @005AA873. Java exact-size byte read for ported serializers.
     */
    public byte[] readBytes(int count) throws IOException {
        ByteBuffer buffer = adaptor.readBytes(count);
        if (buffer.remaining() != count) {
            throw new EOFException("CArchive::Read expected " + count + " bytes, got " + buffer.remaining());
        }
        byte[] result = new byte[count];
        buffer.get(result);
        return result;
    }

    /**
     * Native support corresponding to CArchive::Write @005AA981. Java exact byte write for ported serializers.
     */
    public void writeBytes(byte[] v) throws IOException {
        adaptor.writeBytes(v);
    }

    public void writeShort(int v) throws IOException {
        adaptor.writeShort(v);
    }

    public boolean readBool() throws IOException {
        return adaptor.readBool();
    }

    public void writeBool(boolean v) throws IOException {
        adaptor.writeBool(v);
    }

    /**
     * Native: AFXAPI operator>>(CArchive&, CString&) @005AA4F7. Fully ported.
     */
    @SneakyThrows
    public String readCString() {
        int count = readStringCount();
        boolean unicode = false;
        if (count == -1) {
            unicode = true;
            count = readStringCount();
        }
        if (count <= 0) return "";

        if (!unicode) {
            return new String(readBytes(count), adaptor.getCharset());
        } else {
            return new String(readBytes(count * 2), StandardCharsets.UTF_16LE);
        }
    }

    /**
     * Native: AFXAPI operator<<(CArchive&, const CString&) @005AA47D. Fully ported.
     */
    @SneakyThrows
    public void writeCString(String s) {
        byte[] b = s.getBytes(adaptor.getCharset());
        int len = b.length;

        if (len < 0xFF) {
            writeByte(len);
        } else if (len < 0xFFFE) {
            writeByte(0xFF);
            writeShort(len);
        } else {
            writeByte(0xFF);
            writeShort(0xFFFF);
            writeInt(len);
        }
        if (len > 0) writeBytes(b);
    }

    /**
     * Native: CArchive::WriteString @005AABAB. Fully ported for ANSI text archive output.
     */
    public void writeString(String s) throws IOException {
        int nul = s.indexOf('\0');
        String text = nul >= 0 ? s.substring(0, nul) : s;
        writeBytes(text.getBytes(adaptor.getCharset()));
    }

    /**
     * Native: CArchive::ReadString @005AABC8. Fully ported for ANSI text archive input.
     */
    public String readString(int nMax) throws IOException {
        int nStop = nMax < 0 ? -nMax : nMax;
        ByteArrayOutputStream line = new ByteArrayOutputStream(nStop);
        int nRead = 0;
        while (nRead < nStop) {
            int ch;
            try {
                ch = readTextByte() & 0xFF;
            } catch (EOFException e) {
                return nRead == 0 ? null : line.toString(adaptor.getCharset());
            }

            if (ch == '\n' || ch == '\r') {
                if (ch == '\r') {
                    try {
                        ch = readTextByte() & 0xFF;
                    } catch (EOFException e) {
                        return nRead == 0 ? null : line.toString(adaptor.getCharset());
                    }
                }
                if (nMax != nStop) {
                    line.write(ch);
                    nRead++;
                }
                break;
            }
            line.write(ch);
            nRead++;
        }
        return line.toString(adaptor.getCharset());
    }

    /**
     * Native: CArchive::ReadString(CString&) @005AAC7F. Fully ported through Java String return.
     */
    public String readString() throws IOException {
        StringBuilder result = new StringBuilder();
        while (true) {
            String chunk = readString(-128);
            if (chunk == null) {
                return result.isEmpty() ? null : result.toString();
            }
            result.append(chunk);
            int length = chunk.length();
            if (length < 128 || chunk.charAt(length - 1) == '\n') {
                break;
            }
        }
        int length = result.length();
        if (length != 0 && result.charAt(length - 1) == '\n') {
            result.setLength(length - 1);
        }
        return result.toString();
    }

    @SneakyThrows
    public int readCnt(int size) {
        return switch (size) {
            case 0 -> readCount();
            case 1 -> (readByte() & 0xFF);
            case 2 -> readUShort();
            default -> readInt();
        };
    }

    @SneakyThrows
    public void writeCnt(int data, int size) {
        switch (size) {
            case 0 -> writeCount(data);
            case 1 -> writeByte(data);
            case 2 -> writeShort(data);
            default -> writeInt(data);
        }
    }

    /**
     * Native: CArchive::WriteCount @005AAB4D. Fully ported.
     */
    @SneakyThrows
    public void writeCount(int nCount) {
        long v = nCount & 0xFFFF_FFFFL;
        if (v < 0xFFFFL) {
            writeShort((int) v);
        } else {
            writeShort(0xFFFF);
            writeInt((int) v);
        }
    }

    /**
     * Native: CArchive::ReadCount @005AAB7B. Fully ported.
     */
    @SneakyThrows
    public int readCount() {
        int wCount = readUShort();
        if (wCount != 0xFFFF) return wCount;
        return readInt();
    }

    /**
     * Native support extracted from ReadStringLength @005AA594. Fully ported.
     */
    private int readStringCount() throws IOException {
        int b = readByte() & 0xFF;
        if (b != 0xFF) return b;

        int w = readUShort();
        if (w == 0xFFFE) return -1;
        if (w == 0xFFFF) return readInt();
        return w;
    }

    /**
     * Native support extracted from CArchive::ReadString @005AABC8 EOF handling.
     */
    private byte readTextByte() throws IOException {
        try {
            return readByte();
        } catch (BufferUnderflowException e) {
            EOFException eof = new EOFException("CArchive::ReadString reached EOF");
            eof.initCause(e);
            throw eof;
        }
    }

    public double readDouble() throws IOException {
        return adaptor.readDouble();
    }

    public void writeDouble(double v) throws IOException {
        adaptor.writeDouble(v);
    }


    // Direct MfcSerializable object serialization (no wrappers required).
    public <T extends MfcSerializable> void serialize(T obj) throws IOException {
        obj.serialize(this);
    }

    /**
     * Native: CArchive::ReadObject @005AC3E9. Fully ported.
     */
    public <T extends MfcSerializable> T readObject(Class<T> requestedClass) throws IOException {
        if (isStoring()) {
            throw new IllegalStateException("readObject called on storing archive");
        }
        Objects.requireNonNull(requestedClass, "requestedClass");

        ReadClassResult cls = readClass(requestedClass);
        if (cls.objectReference) {
            int obIndex = cls.objectTag;
            if (obIndex < 0 || obIndex >= loadRefs.size()) {
                throw new IOException("CArchive::ReadObject bad index: " + obIndex
                        + " at streamPos=" + position()
                        + " loadRefsSize=" + loadRefs.size());
            }
            Object mapped = loadRefs.get(obIndex);
            if (mapped == null) {
                return null;
            }
            if (!requestedClass.isInstance(mapped)) {
                throw new IOException(
                        "CArchive::ReadObject bad class reference. requested=" + requestedClass.getName()
                                + " actual=" + mapped.getClass().getName()
                                + " index=" + obIndex
                                + " streamPos=" + position()
                );
            }
            return requestedClass.cast(mapped);
        }

        Class<? extends T> compatibleClass = getCompatibleClass(requestedClass, cls.runtimeClassName);
        if (compatibleClass == null) {
            throw new IOException(
                    "CArchive::ReadObject bad class. requested=" + requestedClass.getName()
                            + " stream=" + cls.runtimeClassName
            );
        }

        T obj = instantiate(compatibleClass);
        appendLoadRef(obj); // MFC maps object before calling Serialize
        int savedObjectSchema = objectSchema;
        objectSchema = cls.schema;
        try {
            obj.serialize(this);
        } finally {
            objectSchema = savedObjectSchema;
        }
        return obj;
    }

    /**
     * Native: CArchive::WriteObject @005AC36A. Fully ported.
     */
    public <T extends MfcSerializable> void writeObject(T obj) throws IOException {
        if (!isStoring()) {
            throw new IllegalStateException("writeObject called on loading archive");
        }
        ensureStoreRefsInitialized();
        if (obj == null) {
            // null object tag
            writeShort(0);
            return;
        }

        Integer objectIndex = storeRefs.get(obj);
        if (objectIndex != null && objectIndex != 0) {
            writeObjectRefTag(objectIndex);
            return;
        }

        writeClass(obj.mfcRuntimeClass());
        putStoreRef(obj);
        obj.serialize(this);
    }

    /**
     * Native: CArchive::GetObjectSchema @005AC498. Fully ported.
     */
    public int getObjectSchema() {
        int result = objectSchema;
        objectSchema = -1;
        return result;
    }

    /**
     * Native: CArchive::SerializeClass @005AC7E9. Fully ported.
     */
    public void serializeClass(Class<? extends MfcSerializable> runtimeClass) throws IOException {
        if (isStoring()) {
            writeClass(runtimeClass);
            return;
        }

        ReadClassResult cls = readClass(runtimeClass);
        if (cls.objectReference) {
            throw new IOException("CArchive::SerializeClass expected class tag, got object tag: "
                    + cls.objectTag
                    + " at streamPos=" + position());
        }
        objectSchema = cls.schema;
    }

    private <T extends MfcSerializable> T instantiate(Class<? extends T> type) throws IOException {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IOException("CArchive::ReadObject cannot instantiate " + type.getName(), e);
        }
    }

    /**
     * Native: CArchive::ReadClass @005AC629. Fully ported.
     */
    private ReadClassResult readClass(Class<? extends MfcSerializable> requestedClass) throws IOException {
        Objects.requireNonNull(requestedClass, "requestedClass");
        ensureLoadRefsInitialized();

        int wTag = readUShort();
        int objectTag;
        if ((wTag == W_BIG_OBJECT_TAG)) {
            objectTag = readInt();
        } else {
            objectTag = ((wTag & W_CLASS_TAG) << 16) | (wTag & ~W_CLASS_TAG);
        }

        if ((objectTag & DW_BIG_CLASS_TAG) == 0) {
            return new ReadClassResult(null, 0, objectTag, true);
        }

        RuntimeClassInfo info;
        if (wTag == W_NEW_CLASS_TAG) {
            // New class descriptor path: emulate CRuntimeClass::Load stream reads.
            info = readRuntimeClassInfo();
            appendLoadRef(info); // class descriptor receives an index in the shared map
        } else {
            int classIndex = objectTag & ~DW_BIG_CLASS_TAG;
            if (classIndex <= 0 || classIndex >= loadRefs.size()) {
                throw new IOException("CArchive::ReadClass bad class index: " + classIndex
                        + " at streamPos=" + position()
                        + " loadRefsSize=" + loadRefs.size());
            }
            Object mapped = loadRefs.get(classIndex);
            if (!(mapped instanceof RuntimeClassInfo mappedInfo)) {
                throw new IOException("CArchive::ReadClass expected class ref at index: " + classIndex
                        + " at streamPos=" + position()
                        + " mappedType=" + (mapped == null ? "null" : mapped.getClass().getName()));
            }
            info = mappedInfo;
        }

        return new ReadClassResult(info.className, info.schema, objectTag, false);
    }

    /**
     * Native support extracted from CRuntimeClass::Load @005AA639. Fully ported for stream shape.
     */
    private RuntimeClassInfo readRuntimeClassInfo() throws IOException {
        // VC97 MFC (arccore.cpp / CRuntimeClass::Load):
        //   WORD schema;
        //   WORD nameLen;
        //   BYTE name[nameLen]; // ANSI, no terminator in stream
        int schema = readUShort();
        int nameLen = readUShort();
        // VC97 uses a fixed local buffer char szClassName[64].
        if (nameLen >= 64) {
            throw new IOException("CRuntimeClass::Load class name too long");
        }
        byte[] nameBytes = readBytes(nameLen);
        String className = new String(nameBytes, StandardCharsets.ISO_8859_1);
        return new RuntimeClassInfo(className, schema);
    }

    /**
     * Native support extracted from CRuntimeClass::Store @005AA6C9. Fully ported for single-schema stream shape.
     */
    private void writeRuntimeClassInfo(Class<?> runtimeClass) throws IOException {
        String className = runtimeClass.getSimpleName();
        byte[] nameBytes = className.getBytes(StandardCharsets.ISO_8859_1);
        if (nameBytes.length >= 64) {
            throw new IOException("CRuntimeClass::Store class name too long");
        }
        writeShort(SINGLE_SCHEMA);
        writeShort(nameBytes.length);
        writeBytes(nameBytes);
    }

    private <T extends MfcSerializable> Class<? extends T> getCompatibleClass(
            Class<T> requestedClass,
            String runtimeClassName
    ) {
        if (runtimeClassName == null || runtimeClassName.isBlank()) {
            return requestedClass;
        }
        String reqSimple = requestedClass.getSimpleName();
        String reqName = requestedClass.getName();
        String token = runtimeClassName.trim();

        ClassLoader cl = requestedClass.getClassLoader();
        for (String fqcn : lookupRuntimeClassCandidates(token)) {
            Class<?> runtimeClass = tryLoadClass(fqcn, cl);
            if (runtimeClass != null && requestedClass.isAssignableFrom(runtimeClass)) {
                return runtimeClass.asSubclass(requestedClass);
            }
        }

        if (token.equals(reqSimple) || token.equals(reqName)) {
            return requestedClass;
        }

        if (token.equalsIgnoreCase(reqSimple) || token.equalsIgnoreCase(reqName)) {
            return requestedClass;
        }
        return null;
    }

    private static List<String> lookupRuntimeClassCandidates(String runtimeClassName) {
        Set<String> nameVariants = new LinkedHashSet<>();
        nameVariants.add(runtimeClassName);

        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        for (String v : nameVariants) {
            if (v.contains(".")) {
                candidates.add(v);
            }
            candidates.addAll(ClassNameIndex.get().lookup(v));
        }
        return new ArrayList<>(candidates);
    }

    private static Class<?> tryLoadClass(String binaryName, ClassLoader cl) {
        try {
            return Class.forName(binaryName, false, cl);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    private static final class RuntimeClassInfo {
        private final String className;
        private final int schema;

        private RuntimeClassInfo(String className, int schema) {
            this.className = className;
            this.schema = schema;
        }
    }

    private static final class ReadClassResult {
        private final String runtimeClassName;
        private final int schema;
        private final int objectTag;
        private final boolean objectReference;

        private ReadClassResult(String runtimeClassName, int schema, int objectTag, boolean objectReference) {
            this.runtimeClassName = runtimeClassName;
            this.schema = schema;
            this.objectTag = objectTag;
            this.objectReference = objectReference;
        }
    }

    /**
     * Native support for CArchive::MapObject @005AC4A0. Fully ported through Java load-reference initialization.
     */
    private void ensureLoadRefsInitialized() {
        if (loadRefs != null) {
            return;
        }
        loadRefs = new ArrayList<>();
        loadRefs.add(null); // tag 0 is null in MFC
    }

    /**
     * Native support for CArchive::MapObject @005AC4A0. Fully ported through Java store-reference initialization.
     */
    private void ensureStoreRefsInitialized() {
        if (storeRefs != null) {
            return;
        }
        storeRefs = new IdentityHashMap<>();
        storeRefs.put(null, 0);
        nextStoreRefIndex = 1;
    }

    /**
     * Native support for CArchive::MapObject @005AC4A0. Fully ported for loading reference insertion.
     */
    private void appendLoadRef(Object ref) throws IOException {
        checkCount(loadRefs.size());
        loadRefs.add(ref);
    }

    /**
     * Native support for CArchive::MapObject @005AC4A0. Fully ported for storing reference insertion.
     */
    private void putStoreRef(Object key) throws IOException {
        checkCount(nextStoreRefIndex);
        storeRefs.put(key, nextStoreRefIndex++);
    }

    /**
     * Native support for CArchive::CheckCount @005AC356. Fully ported.
     */
    private static void checkCount(int count) throws IOException {
        if (count >= N_MAX_MAP_COUNT) {
            throw new IOException("CArchive map count overflow");
        }
    }

    private long position() {
            return adaptor.position();
    }

    public int loadRefsSizeOrMinus1() {
        return loadRefs == null ? -1 : loadRefs.size();
    }

    private void writeObjectRefTag(int objectIndex) throws IOException {
        if (objectIndex < W_BIG_OBJECT_TAG) {
            writeShort(objectIndex);
        } else {
            writeShort(W_BIG_OBJECT_TAG);
            writeInt(objectIndex);
        }
    }

    private void writeClassRefTag(int classIndex) throws IOException {
        if (classIndex < W_BIG_OBJECT_TAG) {
            writeShort(W_CLASS_TAG | classIndex);
        } else {
            writeShort(W_BIG_OBJECT_TAG);
            writeInt(DW_BIG_CLASS_TAG | classIndex);
        }
    }

    /**
     * Native: CArchive::WriteClass @005AC598. Fully ported.
     */
    private void writeClass(Class<?> runtimeClass) throws IOException {
        ensureStoreRefsInitialized();
        Integer classIndex = storeRefs.get(runtimeClass);
        if (classIndex != null && classIndex != 0) {
            writeClassRefTag(classIndex);
            return;
        }

        writeShort(W_NEW_CLASS_TAG);
        writeRuntimeClassInfo(runtimeClass);
        putStoreRef(runtimeClass);
    }


//    // CStringArray::Serialize equivalent
//    public void serialize(List<String> cstringArray) throws IOException {
//        if (!isStoring()) {
//            int n = readCount();
//            cstringArray.clear();
//            for (int i = 0; i < n; i++) {
//                cstringArray.add(readCString());
//            }
//        } else {
//            writeCount(cstringArray.size());
//            for (String s : cstringArray) writeCString(s);
//        }
//    }


/*    public <T extends MfcSerializable> void serializeS4(List<T> list, Supplier<T> ctor) throws IOException {
        serializeS4(list, ctor, 0);
    }*/

/*    public <T extends MfcSerializable> void serializeS4(List<T> list, Supplier<T> ctor, int startIndex) throws IOException {
        serialize(list, ctor, startIndex, 4);
    }*/


//    // startIndex==1 matches your “skip [0]” arrays: size includes element 0, but element 0 is not serialized.
//    public <T extends MfcSerializable> void serialize(List<T> list, Supplier<T> ctor, int startIndex, int countSize) throws IOException {
//        if (startIndex < 0) throw new IllegalArgumentException("startIndex < 0");
//
//        if (!isStoring()) {
//            int n = readCnt(countSize);
//            list.clear();
//
//            for (int i = 0; i < n; i++) {
//                T obj = ctor.get();
//                list.add(obj);
//                if (i >= startIndex) obj.serialize(this);
//            }
//        } else {
//            int n = list.size();
//            writeCnt(n, countSize);
//            for (int i = Math.min(startIndex, n); i < n; i++) {
//                T obj = list.get(i);
//                if (obj == null) throw new NullPointerException("list[" + i + "] is null");
//                obj.serialize(this);
//            }
//        }
//    }


    // Generic list serialization used by CustomList:
    // - String
    // - Number/primitive-wrapper
    // - MfcSerializable (serialized directly via serialize(CArchive))
    /**
     * Native support for CArray<>::Serialize @004A0D60 and SerializeElements @004A0F00.
     */
    @SuppressWarnings("unchecked")
    public <T> void serialize(List<T> list, int skip, int countSize, Class<T> cls) throws IOException {
        if (cls == String.class) {
            new StringSerializationStrategy()
                    .run((List<String>) list, skip, countSize, (Class<String>) cls);
        } else if (MfcSerializable.class.isAssignableFrom(cls)) {
            new MFCSerializationStrategy<>()
                    .run((List<MfcSerializable>) list, skip, countSize, (Class<MfcSerializable>) cls);
        } else if (Number.class.isAssignableFrom(cls) || cls.isPrimitive()) {
            new BasicSerializationStrategy<>()
                    .run((List<Number>) list, skip, countSize, (Class<Number>) cls);
        } else {
            throw new RuntimeException("Something is unhandled! class: " + cls.getName());
        }
    }


    @SneakyThrows
    private Number readNumberByClass(Class<?> elementClass) {
        if (elementClass == Byte.class || elementClass == byte.class) {
            return readByte();
        }
        if (elementClass == Short.class || elementClass == short.class) {
            return readShort();
        }
        if (elementClass == Integer.class || elementClass == int.class) {
            return readInt();
        }
        if (elementClass == Double.class || elementClass == double.class) {
            return readDouble();
        }
        throw new IOException("Unsupported numeric list element type: " + elementClass.getName());
    }

    private void writeNumberByClass(Class<?> elementClass, Number value) throws IOException {
        if (elementClass == Byte.class || elementClass == byte.class) {
            writeByte(value.byteValue());
            return;
        }
        if (elementClass == Short.class || elementClass == short.class) {
            writeShort(value.shortValue());
            return;
        }
        if (elementClass == Integer.class || elementClass == int.class) {
            writeInt(value.intValue());
            return;
        }
        if (elementClass == Double.class || elementClass == double.class) {
            writeDouble(value.doubleValue());
            return;
        }
        throw new IOException("Unsupported numeric list element type: " + elementClass.getName());
    }

    @SneakyThrows
    private <T extends MfcSerializable> T getMfcSerializable(Class<T> elementClass) {
        // Cannot instantiate interfaces/abstract classes; treat as object-framed list element.
        if (elementClass.isInterface() || Modifier.isAbstract(elementClass.getModifiers())) {
            return readObject(elementClass);
        }
        T obj = elementClass.getDeclaredConstructor().newInstance();
        if (obj.isDirect()) {
            return readObject(elementClass);
        }
        obj.serialize(this);
        return obj;
    }


    private abstract class SerializationStrategy<T> {
        abstract int readCount(int countSize);

        abstract void writeCount(int i, int countSize);

        abstract T readElement(Class<T> cls);

        abstract void writeElement(Class<T> cls, T e);

        void run(List<T> list, int skip, int countSize, Class<T> cls) {
            if (CArchive.this.isStoring()) {
                int size = list.size();
                writeCount(size, countSize);
                for (int i = skip; i < size; i++) {
                    writeElement(cls, list.get(i));
                }
            } else {
                int size = readCount(countSize);
                list.clear();
                for (int i = 0; i < skip; i++) {
                    list.add(null);
                }
                for (int i = skip; i < size; i++) {
                    list.add(readElement(cls));
                }


            }
        }
    }

    private class StringSerializationStrategy extends SerializationStrategy<String> {
        @Override
        int readCount(int countSize) {
            return CArchive.this.readCount();
        }

        @Override
        void writeCount(int i, int countSize) {
            CArchive.this.writeCount(i);
        }

        @Override
        String readElement(Class<String> cls) {
            return CArchive.this.readCString();
        }

        @Override
        void writeElement(Class<String> cls, String e) {
            CArchive.this.writeCString(e);
        }
    }

    private class BasicSerializationStrategy<T extends Number> extends SerializationStrategy<T> {
        @Override
        int readCount(int countSize) {
            return CArchive.this.readCnt(countSize);
        }

        @Override
        void writeCount(int i, int countSize) {
            CArchive.this.writeCnt(i, countSize);
        }

        @SuppressWarnings("unchecked")
        @Override
        T readElement(Class<T> cls) {
            return (T) CArchive.this.readNumberByClass(cls);
        }

        @SneakyThrows
        @Override
        void writeElement(Class<T> cls, T e) {
            CArchive.this.writeNumberByClass(cls, e);
        }
    }

    private class MFCSerializationStrategy<T extends MfcSerializable> extends SerializationStrategy<T> {
        @Override
        int readCount(int countSize) {
            return CArchive.this.readCnt(countSize);
        }

        @Override
        void writeCount(int i, int countSize) {
            CArchive.this.writeCnt(i, countSize);
        }

        @SuppressWarnings("unchecked")
        @Override
        T readElement(Class<T> cls) {
            return CArchive.this.getMfcSerializable(cls);
        }

        @SneakyThrows
        @Override
        void writeElement(Class<T> cls, T e) {
            if (e == null || e.isDirect()) {
                CArchive.this.writeObject(e);
            } else {
                e.serialize(CArchive.this);
            }
        }
    }


//    @SuppressWarnings("unchecked")
//    private <T> void serializeNumberListByClass(List<T> list, Class<?> elementClass) throws IOException {
//        if (!isStoring()) {
//            int n = readInt();
//            list.clear();
//            for (int i = 0; i < n; i++) {
//                list.add((T) readNumberByClass(elementClass));
//            }
//        } else {
//            int n = list.size();
//            writeInt(n);
//
//            for (int i = 0; i < n; i++) {
//                T obj = list.get(i);
//                if (!(obj instanceof Number number)) {
//                    throw new IOException("list[" + i + "] is not Number");
//                }
//                writeNumberByClass(elementClass, number);
//            }
//        }
//    }


//    @SuppressWarnings("unchecked")
//    private <T> void serializeMfcListByClass(List<T> list, Class<? extends MfcSerializable> elementClass) throws IOException {
//        if (!isStoring()) {
//            int n = readInt();
//            list.clear();
//
//            for (int i = 0; i < n; i++) {
//                MfcSerializable obj = getMfcSerializable(elementClass);
//                list.add((T) obj);
//            }
//        } else {
//            int n = list.size();
//            writeInt(n);
//
//            for (int i = 0; i < n; i++) {
//                T obj = list.get(i);
//                if (obj == null) throw new NullPointerException("list[" + i + "] is null");
//                if (!elementClass.isInstance(obj)) {
//                    throw new IOException("list[" + i + "] has unexpected type: " + obj.getClass().getName());
//                }
//                ((MfcSerializable) obj).serialize(this);
//            }
//        }
//    }

}
