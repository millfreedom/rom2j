package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Native `BinaryBlobAction` packet id `0xBE` used to upload a selected character file during multiplayer setup.
 */
public class UploadCharacterFileAction extends BinaryBlobAction {
    public static final int ACTION_ID = GameActionId.UPLOAD_CHARACTER_FILE_ACTION_BE.id;
    public static final UploadCharacterFileAction global = new UploadCharacterFileAction();
    private static final int MAX_UPLOAD_CHARACTER_FILE_BYTES = 0x8000;

    /**
     * Native support extracted from MapVisualObject::uploadCharacterFile @0041C6B5,
     * CGameSession::submitCharacterSetupAndWaitForSelectedUnit @0049183E, and CMainWindow::onDialogClosed @004891D8.
     */
    public UploadCharacterFileAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::uploadCharacterFile @0041C6B5,
     * CGameSession::submitCharacterSetupAndWaitForSelectedUnit @0049183E, and CMainWindow::onDialogClosed @004891D8.
     */
    public UploadCharacterFileAction(UploadCharacterFileAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::uploadCharacterFile @0041C6B5 packet field writes and CFile read.
     */
    public static UploadCharacterFileAction prepareForCharacterFileUpload(int netId, String characterFilePath) {
        UploadCharacterFileAction action = global;
        action.ID.set(ACTION_ID);
        action.netID.set(netId);
        action.playerID.set(0);
        byte[] payload = readUploadPayload(characterFilePath);
        action.payloadSize.set(payload.length);
        action.data.set(payload);
        return action;
    }

    /**
     * Native support extracted from MapVisualObject::uploadCharacterFile @0041C6B5 CFile::Open/GetLength/Read block.
     */
    private static byte[] readUploadPayload(String characterFilePath) {
        try {
            Path path = Path.of(characterFilePath);
            long size = Files.size(path);
            if (size >= MAX_UPLOAD_CHARACTER_FILE_BYTES) {
                return new byte[0];
            }
            byte[] payload = Files.readAllBytes(path);
            return payload.length < MAX_UPLOAD_CHARACTER_FILE_BYTES ? payload : new byte[0];
        } catch (IOException ignored) {
            // Native CFile::Open failure leaves payloadSize at zero and still sends the packet.
            return new byte[0];
        }
    }

    /**
     * Native support extracted from MapVisualObject::uploadCharacterFile @0041C6B5,
     * CGameSession::submitCharacterSetupAndWaitForSelectedUnit @0049183E, and CMainWindow::onDialogClosed @004891D8.
     */
    @Override
    public UploadCharacterFileAction Clone() {
        return new UploadCharacterFileAction(this);
    }
}
