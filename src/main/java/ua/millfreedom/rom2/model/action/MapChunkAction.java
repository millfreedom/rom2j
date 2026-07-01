package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.net.CBufferManager;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Variable-size map/chunk payload packet (ID 0xC0 path).
 */
public class MapChunkAction extends CGameAction {
    public static final int ACTION_ID = GameActionId.MAP_CHUNK_ACTION_C0.id;
    public static final MapChunkAction global = new MapChunkAction();

    //0x0A
    public final Property<String> chunkFilePath = fixedCString(BODY_OFFSET, 0x20);
    //0x2A
    public final Property<Integer> currentOffset = i32(BODY_OFFSET + 0x20);
    //0x2E
    public final Property<Integer> totalFileSize = i32(BODY_OFFSET + 0x24);
    //0x32
    public final Property<Integer> size = i32(BODY_OFFSET + 0x28);
    //0x36
    public final Property<byte[]> chunkBytes = bytes(
            BODY_OFFSET + 0x2C,
            () -> Math.max(size.get(), 0)
    );

    /**
     * Native: MapChunkAction::MapChunkAction @0050C5A0.
     * Fully ported.
     */
    public MapChunkAction() {
        super();
    }

    /**
     * Native: MapChunkAction::MapChunkAction @0050C5BF.
     * Fully ported.
     */
    public MapChunkAction(MapChunkAction from) {
        super();
        int copySize = size.get() + 0x2D;
        PutSlice(ID_OFFSET, from.GetSlice(ID_OFFSET, copySize), 0, copySize);
    }

    /**
     * vtbl +0x04: MapChunkAction::Clone @005417A0.
     * Fully ported.
     */
    @Override
    public MapChunkAction Clone() {
        return new MapChunkAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2.
     * Partial port. Java keeps the temp-file write, next-chunk request, final rename, and transfer-completion dispatch
     * for packet id `0xC0`; the native progress-screen drawing remains outside this packet handler.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        String targetFilePath = chunkFilePath.get();
        Path targetPath = Path.of(targetFilePath);
        Path tempPath = Path.of(targetFilePath + "_tmp");
        int currentOffsetValue = currentOffset.get();
        int totalFileSizeValue = totalFileSize.get();
        byte[] chunkData = chunkBytes.get();
        int nextOffset = Math.addExact(currentOffsetValue, chunkData.length);

        writeMapChunkTempFile(tempPath, currentOffsetValue, chunkData);
        if (nextOffset < totalFileSizeValue) {
            mapVisualObject.requestNextMapChunk(nextOffset);
            return;
        }

        try {
            Files.deleteIfExists(targetPath);
            Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to finalize chunked map file transfer for " + targetPath, e);
        }
        mapVisualObject.notifyMapChunkTransferComplete();
    }

    /**
     * Native support extracted from packet-id `0xC0` handler @0040D9B2.
     * Partial port. Java keeps the recovered offset-sensitive temp-file write behavior, including the fail-fast
     * missing-file boundary for non-zero offsets.
     */
    private static void writeMapChunkTempFile(Path tempPath, int currentOffset, byte[] chunkBytes) {
        if (currentOffset != 0 && !Files.exists(tempPath)) {
            throw new IllegalStateException("Missing temp chunk file for non-zero offset write: " + tempPath);
        }
        try (RandomAccessFile chunkFile = new RandomAccessFile(tempPath.toFile(), "rw")) {
            if (currentOffset == 0) {
                chunkFile.setLength(0);
            }
            chunkFile.seek(Integer.toUnsignedLong(currentOffset));
            chunkFile.write(chunkBytes);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write chunk data into " + tempPath, e);
        }
    }

    /**
     * vtbl +0x08: MapChunkAction::WritePayload @0050C600.
     * Fully ported.
     */
    @Override
    public boolean WritePayload(CBufferManager target) {
        return target.Write(this, ID_OFFSET, size.get() + 0x2D);
    }

    /**
     * vtbl +0x10: MapChunkAction::GetPayloadSize @00541820.
     * Port name: GetPayloadSize.
     * Fully ported.
     */
    @Override
    public int GetPayloadSize() {
        return size.get() + 0x2D;
    }

    /**
     * vtbl +0x0C: MapChunkAction::ReadPayload @0050C626.
     * Fully ported.
     */
    @Override
    public boolean ReadPayload(CBufferManager source) {
        boolean result = source.Read(this, BODY_OFFSET, 0x2C);
        if (size.get() > 0) {
            result = source.Read(this, BODY_OFFSET + 0x2C, size.get());
        }
        return result;
    }

}
