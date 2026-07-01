package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Native `ChatTextAction` packet id `0xBF` used to verify the expected map-file checksum before chunk download.
 */
public class MapChecksumAction extends ChatTextAction {
    public static final int ACTION_ID = GameActionId.MAP_CHECKSUM_ACTION_BF.id;
    public static final MapChecksumAction global = new MapChecksumAction();

    /**
     * Native support extracted from GameServer::sendMapChecksumToPlayer @004F1CDD and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public MapChecksumAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from GameServer::sendMapChecksumToPlayer @004F1CDD and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public MapChecksumAction(MapChecksumAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2.
     * Partial port. Java keeps the local-file checksum gate and `RequestMapChunkAction` dispatch for packet id `0xBF`.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        if (computeFileChecksum(text.get()) == senderIdAndChannel.get()) {
            Globals.mainWindow.fileTransferChecksumMatched = 1;
            Globals.mainWindow.fileTransferDownloadPending = 0;
        } else {
            mapVisualObject.requestNextMapChunk(0);
            Globals.mainWindow.fileTransferDownloadPending = 1;
        }
    }

    /**
     * Native support extracted from checksum helper FUN_004EBC3E @004EBC3E and packet-id `0xBF` handler @0040D9B2.
     * Partial port. Java preserves the recovered file-open failure behavior by returning `0`, and otherwise sums the
     * file as padded little-endian dwords.
     */
    private static int computeFileChecksum(String filePath) {
        try {
            byte[] fileBytes = Files.readAllBytes(Path.of(filePath));
            ByteBuffer buffer = ByteBuffer.allocate((fileBytes.length & ~0x3) + 4).order(ByteOrder.LITTLE_ENDIAN);
            buffer.put(fileBytes);
            buffer.flip();
            int checksum = 0;
            while (buffer.remaining() >= Integer.BYTES) {
                checksum += buffer.getInt();
            }
            return checksum;
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * Native support extracted from GameServer::sendMapChecksumToPlayer @004F1CDD and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    @Override
    public MapChecksumAction Clone() {
        return new MapChecksumAction(this);
    }
}
