package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.GameCharsets;
import ua.millfreedom.rom2.model.net.CBufferManager;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.IntSupplier;

/**
 * Base network game action packet object.
 */
public class CGameAction implements Packet, GameActionHandler {
    public static final int BASE_OFFSET = 0x04;
    public static final int ID_OFFSET = 0x09;
    public static final int BODY_OFFSET = 0x0A;
    public static final int BUFFER_SIZE = 200_000;

    // Native global singleton used in dispatcher fallback paths.
    public static final int ACTION_ID = 0;
    public static final CGameAction global = new CGameAction();

    // not native.
    private final ByteBuffer nativeObjectBuffer = ByteBuffer.allocate(BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN);
    // not native.
    private CBufferManager sourceClient;
    //0x04
    public final Property<Integer> unitOrderMode = u8(0x04);
    //0x05
    public final Property<Integer> netID = u16(0x05);
    //0x07
    public final Property<Integer> playerID = u16(0x07);
    //0x09
    public final Property<Integer> ID = u8(ID_OFFSET);

    /**
     * Native: CGameAction::CGameAction @0050BA7D.
     * Fully ported.
     */
    public CGameAction() {
        unitOrderMode.set(0);
        netID.set(0);
        playerID.set(0);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CServerApp::sendNoPayloadAction @005052F8 packet header writes.
     */
    public static CGameAction prepareNoPayloadAction(GameActionId actionId, Player player) {
        CGameAction action = global;
        action.ID.set(actionId.id);
        action.playerID.set(player == null ? 0 : player.playerId);
        return action;
    }

    /**
     * Native: CGameAction::CGameAction @0050BAB4.
     * Fully ported.
     */
    public CGameAction(CGameAction from) {
        ID.set(from.ID.get());
    }

    /**
     * Native support extracted from CServerApp::dispatchSpellEffectVisibilityGatedAction @00503BEF
     * per-recipient packet field writes.
     */
    public CGameAction cloneForSpellEffectVisibilityRecipient(Player player) {
        playerID.set(player.playerId);
        return Clone();
    }

    /**
     * Native support extracted from CServerApp::decodeIncomingGameAction @005056F1 final header writes.
     * Fully ported.
     */
    public void finalizeIncomingHeader(int actionIdValue, CBufferManager client, boolean localEndpoint) {
        ID.set(actionIdValue);
        sourceClient = client;
        if (!localEndpoint) {
            netID.set(0);
            return;
        }
        int netId = client.GetNetId();
        netID.set(netId == 0 ? (client.GetIPAddress() & 0x3FFF) | 0x4000 : netId);
    }

    /**
     * Native support extracted from GameServer::rejectClientJoin @004F0B71 callers.
     */
    public CBufferManager getSourceClient() {
        return sourceClient;
    }

    /**
     * vtbl +0x04: CGameAction::Clone @00540490.
     * Fully ported.
     */
    @Override
    public CGameAction Clone() {
        return new CGameAction(this);
    }

    /**
     * vtbl +0x08: CGameAction::WritePayload @0050BAE5.
     * Writes GetPayloadSize() bytes starting at CGameAction_Base.ID.
     * Fully ported.
     */
    @Override
    public boolean WritePayload(CBufferManager target) {
        return target.Write(this, ID_OFFSET, GetPayloadSize());
    }

    /**
     * vtbl +0x0C: CGameAction::ReadPayload @0050BB15.
     * Input stream is already positioned after CGameAction_Base.ID.
     * Fully ported.
     */
    @Override
    public boolean ReadPayload(CBufferManager source) {
        int bodySize = GetPayloadSize() - 1;
        if (bodySize == 0) {
            return false;
        }
        return source.Read(this, BODY_OFFSET, bodySize);
    }

    /**
     * vtbl +0x10: CGameAction::GetPayloadSize @00540510.
     * Returns packet wire size in bytes, counted from CGameAction_Base.ID.
     * Fully ported.
     */
    @Override
    public int GetPayloadSize() {
        return 1;
    }

    /**
     * Native support extracted from CGameAction::WritePayload @0050BAE5 and CBufferManager::Write @005000A1.
     */
    public byte[] GetSlice(int nativeObjectOffset, int size) {
        validateNativeRange(nativeObjectOffset, size);
        byte[] target = new byte[size];
        copyNativeObjectBufferToArray(nativeObjectOffset, target, 0, size);
        return target;
    }

    /**
     * Native support extracted from CGameAction::ReadPayload @0050BB15 and CBufferManager::Read @0050014A.
     */
    public void PutSlice(int nativeObjectOffset, byte[] source, int sourceOffset, int size) {
        if (source == null || sourceOffset < 0 || size < 0 || sourceOffset + size > source.length) {
            throw new IllegalArgumentException("Invalid native object source");
        }
        validateNativeRange(nativeObjectOffset, size);
        copyArrayToNativeObjectBuffer(source, sourceOffset, nativeObjectOffset, size);
    }

    /**
     * Native support extracted from CGameAction::WritePayload @0050BAE5 and CBufferManager::Write @005000A1.
     */
    protected final boolean writeNativeRange(CBufferManager target, int nativeObjectOffset, int size) {
        return target != null && target.Write(GetSlice(nativeObjectOffset, size), 0, size);
    }

    /**
     * Native support extracted from CGameAction::ReadPayload @0050BB15 and CBufferManager::Read @0050014A.
     */
    protected final boolean readNativeRange(CBufferManager source, int nativeObjectOffset, int size) {
        if (source == null || size < 0) {
            return false;
        }
        byte[] data = new byte[size];
        if (!source.Read(data, 0, size)) {
            return false;
        }
        PutSlice(nativeObjectOffset, data, 0, size);
        return true;
    }

    /**
     * not ported.
     */
    private void copyNativeObjectBufferToArray(int nativeObjectOffset, byte[] target, int targetOffset, int size) {
        if (target == null || targetOffset < 0 || size < 0 || targetOffset + size > target.length) {
            throw new IllegalArgumentException("Invalid native object target");
        }
        ByteBuffer duplicate = nativeObjectBuffer.duplicate();
        duplicate.position(nativeObjectOffset);
        duplicate.get(target, targetOffset, size);
    }

    /**
     * not ported.
     */
    private void copyArrayToNativeObjectBuffer(byte[] source, int sourceOffset, int nativeObjectOffset, int size) {
        if (source == null || sourceOffset < 0 || size < 0 || sourceOffset + size > source.length) {
            throw new IllegalArgumentException("Invalid native object source");
        }
        ByteBuffer duplicate = nativeObjectBuffer.duplicate();
        duplicate.position(nativeObjectOffset);
        duplicate.put(source, sourceOffset, size);
    }

    /**
     * not ported.
     */
    static boolean nativeRangeTouches(int rangeStart, int rangeSize, int targetStart, int targetSize) {
        return rangeSize > 0
                && targetSize > 0
                && rangeStart < targetStart + targetSize
                && targetStart < rangeStart + rangeSize;
    }

    /**
     * not ported.
     */
    protected final Property<Integer> u8(int nativeObjectOffset) {
        return Property.unsignedByte(nativeObjectBuffer, nativeObjectOffset);
    }

    /**
     * not ported.
     */
    protected final Property<Integer> u16(int nativeObjectOffset) {
        return Property.unsignedWord(nativeObjectBuffer, nativeObjectOffset);
    }

    /**
     * not ported.
     */
    protected final Property<Integer> i32(int nativeObjectOffset) {
        return Property.int32(nativeObjectBuffer, nativeObjectOffset);
    }

    /**
     * not ported.
     */
    protected final Property<byte[]> bytes(int nativeObjectOffset, int size) {
        return Property.bytes(nativeObjectBuffer, nativeObjectOffset, size);
    }

    /**
     * not ported.
     */
    protected final Property<byte[]> bytes(int nativeObjectOffset, IntSupplier sizeSupplier) {
        return Property.bytes(nativeObjectBuffer, nativeObjectOffset, sizeSupplier);
    }

    /**
     * not ported.
     */
    protected final Property<byte[]> u16Array(int nativeObjectOffset, int elementCount) {
        return bytes(nativeObjectOffset, Math.multiplyExact(elementCount, Short.BYTES));
    }

    /**
     * not ported.
     */
    protected final Property<byte[]> u16Array(int nativeObjectOffset, IntSupplier elementCountSupplier) {
        return bytes(nativeObjectOffset, () -> Math.multiplyExact(elementCountSupplier.getAsInt(), Short.BYTES));
    }

    /**
     * not ported.
     */
    protected final Property<String> fixedCString(int nativeObjectOffset, int size) {
        return Property.fixedCString(nativeObjectBuffer, nativeObjectOffset, size);
    }

    /**
     * not ported.
     */
    protected final Property<String> countedPayloadCString(int payloadOffset, Property<Integer> byteLengthProperty) {
        return new Property<>(
                nativeObjectBuffer,
                BODY_OFFSET + payloadOffset,
                () -> Math.max(byteLengthProperty.get(), 0) + 1,
                String.class,
                new CountedCStringMarshaller(byteLengthProperty)
        );
    }

    /**
     * not ported.
     */
    protected final void copyNativeObjectBufferFrom(CGameAction from) {
        if (from == null) {
            return;
        }
        byte[] source = new byte[BUFFER_SIZE];
        from.copyNativeObjectBufferToArray(0, source, 0, source.length);
        copyArrayToNativeObjectBuffer(source, 0, 0, source.length);
    }

    /**
     * not ported.
     */
    private static void validateNativeRange(int nativeObjectOffset, int size) {
        if (nativeObjectOffset < 0 || size < 0 || nativeObjectOffset + size > BUFFER_SIZE) {
            throw new IndexOutOfBoundsException("Native object range 0x"
                    + Integer.toHexString(nativeObjectOffset) + "..0x"
                    + Integer.toHexString(nativeObjectOffset + size));
        }
    }

    /**
     * Java dispatch default for MapVisualObject::HandleGameAction @0040D9B2.
     * not ported.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
    }

    private record CountedCStringMarshaller(Property<Integer> byteLengthProperty) implements Marshaller<String> {

            /**
             * not ported.
             */
            @Override
            public String get(ByteBuffer buffer, int startPosition, int size) {
                int textBytes = 0;
                while (textBytes < size && buffer.get(startPosition + textBytes) != 0) {
                    textBytes++;
                }
                byte[] encoded = new byte[textBytes];
                ByteBuffer duplicate = buffer.duplicate();
                duplicate.position(startPosition);
                duplicate.get(encoded, 0, textBytes);
                return new String(encoded, GameCharsets.GAME_TEXT);
            }

            /**
             * not ported.
             */
            @Override
            public void put(ByteBuffer buffer, int startPosition, int size, String value) {
                byte[] encoded = (value == null ? "" : value).getBytes(GameCharsets.GAME_TEXT);
                byteLengthProperty.set(encoded.length);
                ByteBuffer duplicate = buffer.duplicate();
                duplicate.position(startPosition);
                for (int i = 0; i <= encoded.length; i++) {
                    duplicate.put((byte) 0);
                }
                duplicate.position(startPosition);
                duplicate.put(encoded, 0, encoded.length);
            }
        }
}
