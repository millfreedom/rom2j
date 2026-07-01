package ua.millfreedom.rom2.res;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ua.millfreedom.rom2.res.ResNodeData.decodeNodeData;
import static ua.millfreedom.rom2.res.Utils.extractRootName;

public final class ResInFile extends Res implements AutoCloseable {
    public final ResBasicFile resBasicFile;

    // not ported.
    private ResInFile(ResNode resNode,
                      int nodesCount,
                      int nodesCap,
                      List<ResNode> nodes,
                      ResNode pInsertion,
                      int totalDataSize,
                      ResBasicFile resBasicFile) {
        super(resNode, nodesCount, nodesCap, nodes, pInsertion, totalDataSize);
        this.resBasicFile = Objects.requireNonNull(resBasicFile);
    }

    /**
     * Native: ResInFile::Read @004E2040.
     * Fully ported for Java-managed resource archive storage.
     */
    public static ResInFile read(String fileName) throws IOException {
        Objects.requireNonNull(fileName, "fileName");

        // lFilename = fileName; if strchr(fileName,'.')==0 => append ".res"
        final String lFilename = (fileName.indexOf('.') < 0) ? (fileName + ".res") : fileName;
        //final String lFilename = fileName;

        final Path path = Path.of(lFilename);
        if (!Files.exists(path)) throw new FileNotFoundException(lFilename);

        final SeekableByteChannel ch = Files.newByteChannel(path, StandardOpenOption.READ);
        try {
            // Res::InitArrIndex => pInsertion = 0 (we keep null in final Res)

            // Root name: take lFilename up to first '.', then strip path component
            final String rootName = extractRootName(lFilename);
            //final byte[] rootName16 = ua.millfreedom.rom2.res.Utils.encodeName16(rootName);

            // Read header fields (exact order/sizes from the binary)
            ByteBuffer header = ByteBuffer.allocate(6 * Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN);
            readFully(ch, header);
            final int magic = header.getInt();
            if (magic != 0x31415926) {
                throw new IOException("Bad resource magic: 0x" + Integer.toHexString(magic));
            }

            final int data0 = header.getInt();
            final int data1 = header.getInt();
            final int flags = header.getInt();

            final int fileOffset = header.getInt();         // reads into info.offset (4 bytes)
            final int nodesCount = header.getInt();

            // Native initializes info.modeOrFlag to zero and reads only info.offset from the file header.
            final short modeOrFlag = 0; // set to 0 before header read and never overwritten by file
            final ResBasicFileInfo info = new ResBasicFileInfo(fileOffset, modeOrFlag, (short) 0);
            final ResBasicFile rbf = new ResBasicFile(path, ch, info);

            final ResNodeData rootData = decodeNodeData(flags, data0, data1,true);
            final ResAtom rootAtom = new ResAtom(magic, rootData, flags);
            final ResNode rootNode = new ResNode(rootAtom, rootName);

            // nodesCap computation
            final int nodesCap = (modeOrFlag == 0)
                    ? nodesCount
                    : (nodesCount + 10_000 + (nodesCount >>> 2));

            // if ((flags & RESFLAG_NODES_AFTER_HEADER) == 0) Seek(info.offset, begin)
            if ((flags & ResNodeFlags.RESFLAG_NODES_AFTER_HEADER) == 0) {
                ch.position(Integer.toUnsignedLong(info.offset()));
            }

            // Read nodes table: nodesCount * 0x20
            final List<ResNode> nodes = new ArrayList<>(Math.max(nodesCount, 0));
            final ByteBuffer nodeBuf = ByteBuffer.allocate(ResNode.SIZE).order(ByteOrder.LITTLE_ENDIAN);

            for (int i = 0; i < nodesCount; i++) {
                nodeBuf.clear();
                readFully(ch, nodeBuf);
                 int nMagic = nodeBuf.getInt();
                int nData0 = nodeBuf.getInt();
                int nData1 = nodeBuf.getInt();
                int nFlags = nodeBuf.getInt();

                byte[] nName16 = new byte[16];
                nodeBuf.get(nName16);

                ResNodeData nd = decodeNodeData(nFlags, nData0, nData1,true);
                ResAtom na = new ResAtom(nMagic, nd, nFlags);
                nodes.add(new ResNode(na, nName16));
            }


            return new ResInFile(rootNode,
                    nodesCount,
                    nodesCap,
                    nodes,
                    null,   // pInsertion = 0
                    0,       // totalDataSize = 0
                    rbf);
        } catch (IOException | RuntimeException e) {
            ch.close();
            throw e;
        }
    }

    @Override
    // not ported.
    public void close() throws IOException {
        resBasicFile.close();
    }

    @Override
    // not ported.
    public String toString() {
        return "ResInFile{" +
                "super=" + super.toString() +
                ", resBasicFile=" + resBasicFile +
                '}';
    }

    // not ported.
    private static void readFully(SeekableByteChannel ch, ByteBuffer buf) throws IOException {
        while (buf.hasRemaining()) {
            int n = ch.read(buf);
            if (n < 0) throw new EOFException("Unexpected EOF");
        }
        buf.flip();
    }
}
