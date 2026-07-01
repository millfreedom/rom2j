package ua.millfreedom.rom2.model.net;

import ua.millfreedom.rom2.NetBuffer;
import ua.millfreedom.rom2.NetBufferInfo;
import ua.millfreedom.rom2.model.CServerApp;
import ua.millfreedom.rom2.model.action.CGameAction;
import ua.millfreedom.rom2.model.compression.ByteHuffmanPacker;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Native transport/session buffer manager used by game-action packet serialization.
 */
public class CBufferManager {
    // Java sentinel for native SetLoginName((CString *)0xffffffff) in CServerApp::onClientActivated @00505F07.
    private static final Object LOGIN_NAME_UNAVAILABLE_SENTINEL = new Object();

    //0x00
    private Object serverApp;
    //0x04
    private Class<?> networkDriver;
    //0x08
    private String addressText = "";
    //0x108
    private short playerID;
    //0x10C
    private int ipAddress;
    //0x110
    public NetBuffer[] writeBuffers;
    //0x260
    public int currentWriteBufferIndex;
    //0x264
    public Deque<NetBuffer> pendingReadBuffers;
    //0x280
    private final Object pendingReadBuffersLock = new Object();
    //0x298
    private int compressionStreamId;
    //0x29C
    private boolean directDeliveryEnabled;
    //0x2A0
    private Object loginName;

    /**
     * Native: CBufferManager::New @004FFCC7.
     * Fully ported.
     */
    public CBufferManager() {
        writeBuffers = new NetBuffer[]{new NetBuffer(), new NetBuffer()};
        currentWriteBufferIndex = 0;
        writeBuffers[0].Clear();
        writeBuffers[1].Clear();
        pendingReadBuffers = new ArrayDeque<>();
        compressionStreamId = 0;
    }

    /**
     * Native: CBufferManager::CBufferManager(CBufferManager*) @004FFDC5.
     * Fully ported.
     */
    public CBufferManager(CBufferManager from) {
        writeBuffers = new NetBuffer[]{new NetBuffer(), new NetBuffer()};
        pendingReadBuffers = new ArrayDeque<>();
        serverApp = from.serverApp;
        networkDriver = from.networkDriver;
        writeBuffers[0].CopyFrom(from.writeBuffers[0]);
        writeBuffers[1].CopyFrom(from.writeBuffers[1]);
        currentWriteBufferIndex = 0;
        compressionStreamId = 0;
        playerID = from.playerID;
        ipAddress = from.ipAddress;
        directDeliveryEnabled = from.directDeliveryEnabled;
        loginName = from.loginName;
        addressText = from.addressText;
    }

    /**
     * Native support extracted from CBufferManager::Read @0050014A.
     */
    public CBufferManager(byte[] pendingPayload) {
        this();
        int offset = 0;
        while (pendingPayload != null && offset < pendingPayload.length) {
            NetBuffer buffer = new NetBuffer();
            buffer.Clear();
            int chunkSize = Math.min(NetBuffer.PAYLOAD_CAPACITY, pendingPayload.length - offset);
            buffer.AppendBytes(pendingPayload, offset, chunkSize);
            pendingReadBuffers.add(buffer);
            offset += chunkSize;
        }
    }

    /**
     * Native support extracted from CBufferManager::Write @005000A1 CGameAction payload callers.
     */
    public boolean Write(CGameAction source, int nativeOffset, int size) {
        byte[] data = source.GetSlice(nativeOffset, size);
        return Write(data, 0, size);
    }

    /**
     * Native: CBufferManager::Write @005000A1.
     * Fully ported.
     */
    public boolean Write(byte[] data, int dataOffset, int size) {
        NetBuffer current = writeBuffers[currentWriteBufferIndex];
        int freeSize = current.GetFreeSize();
        if (freeSize < size) {
            current.AppendBytes(data, dataOffset, freeSize);
            FlushWriteBuffer();
            Write(data, dataOffset + freeSize, size - freeSize);
        } else {
            current.AppendBytes(data, dataOffset, size);
        }
        return true;
    }

    /**
     * Native support extracted from CBufferManager::Read @0050014A CGameAction payload callers.
     */
    public boolean Read(CGameAction target, int nativeOffset, int size) {
        byte[] data = new byte[size];
        if (!Read(data, 0, size)) {
            return false;
        }
        target.PutSlice(nativeOffset, data, 0, size);
        return true;
    }

    /**
     * Native: CBufferManager::Read @0050014A.
     * Fully ported.
     */
    public boolean Read(byte[] target, int targetOffset, int size) {
        synchronized (pendingReadBuffersLock) {
            int remainingSize = size;
            int writeOffset = targetOffset;
            while (true) {
                NetBuffer buffer = pendingReadBuffers.peek();
                if (buffer == null) {
                    return false;
                }

                int remaining = buffer.GetRemainingPayloadSize();
                if (remainingSize <= remaining) {
                    buffer.ReadBytes(target, writeOffset, remainingSize);
                    return true;
                }

                buffer.ReadBytes(target, writeOffset, remaining);
                remainingSize -= remaining;
                writeOffset += remaining;
                NetBuffer consumed = pendingReadBuffers.remove();
                CServerApp.releaseNetBuffer(serverApp, consumed);
            }
        }
    }

    /**
     * Native: CBufferManager::FlushWriteBuffer @0050036A.
     * Fully ported. Java delegates skipped CLlDriver socket send/prune work to explicit transport boundaries.
     */
    public boolean FlushWriteBuffer() {
        NetBuffer outgoing = CServerApp.acquireNetBuffer(serverApp);
        outgoing.Clear();
        NetBufferInfo outgoingHeader = outgoing.GetHeader();
        outgoingHeader.compressionStreamId = compressionStreamId;

        NetBuffer current = writeBuffers[currentWriteBufferIndex];
        int sourceSize = current.GetWireSize() - NetBuffer.WIRE_HEADER_SIZE;
        if (sourceSize < 1) {
            CServerApp.releaseNetBuffer(serverApp, outgoing);
            if (networkDriver != null) {
                CLlDriver.pruneClosedTcpClientBoundary(GetIPAddress());
            }
            return true;
        }

        int compressedBitCount = 0;
        int packedByteCount = NetBuffer.PAYLOAD_CAPACITY + 1;
        int[] packedWords = new int[NetBuffer.PAYLOAD_CAPACITY];
        if (compressionStreamId != 0) {
            compressedBitCount = CServerApp.packNetBufferPayload(
                    serverApp,
                    compressionStreamId,
                    current.payload,
                    sourceSize,
                    packedWords
            );
            packedByteCount = ByteHuffmanPacker.packedByteLength(compressedBitCount);
        }

        if (packedByteCount < sourceSize) {
            outgoingHeader.compressedBitCount = compressedBitCount;
            byte[] packedBytes = wordsToLittleEndianBytes(packedWords, packedByteCount);
            outgoing.AppendBytes(packedBytes, 0, packedByteCount);
        } else {
            outgoingHeader.compressionStreamId = 0;
            outgoingHeader.compressedBitCount = 0;
            outgoing.CopyFrom(current);
        }
        outgoingHeader.segmentMarkerCount = current.GetHeader().segmentMarkerCount;
        AdvanceWriteBuffer();

        if (!IsDirectDelivery()) {
            if (networkDriver == null) {
                CServerApp.releaseNetBuffer(serverApp, outgoing);
                return false;
            }
            CLlDriver.sendDataBoundary(GetIPAddress(), outgoing);
            return true;
        }

        if (serverApp == null) {
            CServerApp.releaseNetBuffer(serverApp, outgoing);
            return false;
        }
        CBufferManager target = CServerApp.getBufferManager(serverApp);
        if (target == null) {
            CServerApp.releaseNetBuffer(serverApp, outgoing);
            return false;
        }
        target.ReceiveData(outgoing);
        return true;
    }

    /**
     * Native: CBufferManager::AdvanceWriteBuffer @00500060.
     * Fully ported.
     */
    private void AdvanceWriteBuffer() {
        currentWriteBufferIndex = currentWriteBufferIndex == 0 ? 1 : 0;
        writeBuffers[currentWriteBufferIndex].Clear();
    }

    /**
     * not ported.
     */
    public byte[] getCurrentWritePayloadCopy() {
        NetBuffer current = writeBuffers[currentWriteBufferIndex];
        byte[] copy = new byte[current.payloadSize];
        System.arraycopy(current.payload, 0, copy, 0, current.payloadSize);
        return copy;
    }

    /**
     * Native: CBufferManager::SetDirectDelivery @00540030.
     * Fully ported.
     */
    public void SetDirectDelivery(boolean directDeliveryEnabled) {
        this.directDeliveryEnabled = directDeliveryEnabled;
    }

    /**
     * Native: CBufferManager::IsDirectDelivery @0053DDD0.
     * Fully ported.
     */
    public boolean IsDirectDelivery() {
        return directDeliveryEnabled;
    }

    /**
     * Native: CBufferManager::SetServerAndDriver @0050075C.
     * Fully ported.
     */
    public void SetServerAndDriver(Object serverApp, Class<?> networkDriver) {
        this.serverApp = serverApp;
        this.networkDriver = networkDriver;
    }

    /**
     * Native: CBufferManager::SetCompressionStreamId @00540010.
     * Fully ported.
     */
    public void SetCompressionStreamId(int compressionStreamId) {
        this.compressionStreamId = compressionStreamId;
    }

    /**
     * Native: CBufferManager::SetIPAddress @005403E0.
     * Fully ported.
     */
    public void SetIPAddress(int ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Native: CBufferManager::GetIPAddress @0041EE40 and @005403F0.
     * Fully ported.
     */
    public int GetIPAddress() {
        return ipAddress;
    }

    /**
     * Native: CBufferManager::getAddressText @004936C0.
     * Fully ported.
     */
    public String getAddressText() {
        return addressText;
    }

    /**
     * Native support extracted from CLlDriver::AcceptThreadTcp @00508AB3 writing the
     * CBufferManager::getAddressText @004936C0 CString storage.
     */
    public void setAddressText(String addressText) {
        this.addressText = addressText;
    }

    /**
     * Native: CBufferManager::SetNetId @0053DA20.
     * Fully ported.
     */
    public void SetNetId(int netId) {
        this.playerID = (short) netId;
    }

    /**
     * Native: CBufferManager::GetNetId @0044F5B0.
     * Fully ported.
     */
    public int GetNetId() {
        return playerID & 0xFFFF;
    }

    /**
     * Native: CBufferManager::SetLoginName @0053DEC0.
     * Fully ported.
     */
    public void SetLoginName(Object loginName) {
        this.loginName = loginName;
    }

    /**
     * Native: CBufferManager::GetLoginName @004936E0.
     * Fully ported.
     */
    public Object GetLoginName() {
        return loginName;
    }

    /**
     * Native support extracted from GameServer::handlePlayerJoinRequest @004F0CBE login-name CString checks.
     */
    public String getActiveLoginName() {
        Object currentLoginName = GetLoginName();
        if (currentLoginName instanceof String loginName) {
            return loginName;
        }
        return null;
    }

    /**
     * Native support extracted from CServerApp::onClientActivated @00505F07 non-TCP branch.
     * Fully ported.
     */
    public void setLoginNameUnavailableSentinel() {
        SetLoginName(LOGIN_NAME_UNAVAILABLE_SENTINEL);
    }

    /**
     * Native support extracted from CServerApp::onClientRemoved @00505D02 login-name delete branch.
     * Fully ported. Java clears only owned CString-equivalent values; null and the native -1 sentinel are preserved.
     */
    public void releaseOwnedLoginName() {
        Object currentLoginName = GetLoginName();
        if (currentLoginName != null && currentLoginName != LOGIN_NAME_UNAVAILABLE_SENTINEL) {
            SetLoginName(null);
        }
    }

    /**
     * Native support extracted from game-action payload write path @0050164F.
     */
    public boolean WriteGameAction(CGameAction action) {
        if (action == null) {
            return false;
        }
        return action.WritePayload(this);
    }

    /**
     * Native support extracted from CBufferManager::ReceiveData @0050023B.
     * Fully ported.
     */
    public boolean ReceiveData(NetBuffer buffer) {
        synchronized (pendingReadBuffersLock) {
            NetBufferInfo header = buffer.GetHeader();
            if (header.compressionStreamId != 0) {
                NetBuffer unpacked = CServerApp.acquireNetBuffer(serverApp);
                unpacked.Clear();
                int decodedSize = CServerApp.unpackNetBufferPayload(
                        serverApp,
                        header.compressionStreamId,
                        littleEndianBytesToWords(buffer.payload, buffer.payloadSize),
                        header.compressedBitCount,
                        unpacked.payload,
                        NetBuffer.PAYLOAD_CAPACITY
                );
                if (decodedSize > NetBuffer.PAYLOAD_CAPACITY) {
                    CServerApp.releaseNetBuffer(serverApp, buffer);
                    CServerApp.releaseNetBuffer(serverApp, unpacked);
                    return false;
                }
                unpacked.SetPayloadSize(decodedSize);
                unpacked.GetHeader().segmentMarkerCount = header.segmentMarkerCount;
                CServerApp.releaseNetBuffer(serverApp, buffer);
                buffer = unpacked;
            }
            pendingReadBuffers.add(buffer);
            return true;
        }
    }

    /**
     * Native: CBufferManager::GetPendingSegmentMarkerCount @0050063F.
     * Fully ported.
     */
    public int GetPendingSegmentMarkerCount() {
        synchronized (pendingReadBuffersLock) {
            int segmentMarkerCount = 0;
            for (NetBuffer buffer : pendingReadBuffers) {
                segmentMarkerCount += buffer.GetSegmentMarkerCount();
            }
            return segmentMarkerCount;
        }
    }

    /**
     * Native: CBufferManager::HasPendingReadPayload @00500591.
     * Fully ported.
     */
    public boolean HasPendingReadPayload() {
        synchronized (pendingReadBuffersLock) {
            if (pendingReadBuffers.isEmpty()) {
                return false;
            }
            for (NetBuffer buffer : pendingReadBuffers) {
                if (buffer.HasRemainingPayload()) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Native: CBufferManager::ConsumeFirstPendingSegmentMarker @005006DB.
     * Fully ported.
     */
    public void ConsumeFirstPendingSegmentMarker() {
        synchronized (pendingReadBuffersLock) {
            for (NetBuffer buffer : pendingReadBuffers) {
                if (buffer.GetSegmentMarkerCount() != 0) {
                    buffer.ConsumeSegmentMarker();
                    return;
                }
            }
        }
    }

    /**
     * Native: CBufferManager::IncrementCurrentWriteBufferSegmentMarker @00540400.
     * Fully ported.
     */
    public void IncrementCurrentWriteBufferSegmentMarker() {
        writeBuffers[currentWriteBufferIndex].IncrementSegmentMarkerCount();
    }

    /**
     * Native: CBufferManager::releasePendingReadBuffers @004FFFD1.
     * Fully ported.
     */
    public void releasePendingReadBuffers() {
        if (serverApp != null) {
            synchronized (pendingReadBuffersLock) {
                while (!pendingReadBuffers.isEmpty()) {
                    CServerApp.releaseNetBuffer(serverApp, pendingReadBuffers.remove());
                }
            }
        }
    }

    /**
     * Native support extracted from CServerApp::PackNetBufferPayload @00501347 caller memcpy.
     */
    private static byte[] wordsToLittleEndianBytes(int[] words, int byteCount) {
        byte[] bytes = new byte[byteCount];
        for (int i = 0; i < byteCount; i++) {
            int wordIndex = i >>> 2;
            int shift = (i & 3) << 3;
            bytes[i] = (byte) (words[wordIndex] >>> shift);
        }
        return bytes;
    }

    /**
     * Native support extracted from CServerApp::UnpackNetBufferPayload @0050148A caller memcpy.
     */
    private static int[] littleEndianBytesToWords(byte[] bytes, int byteCount) {
        int[] words = new int[(byteCount + Integer.BYTES - 1) / Integer.BYTES];
        for (int i = 0; i < byteCount; i++) {
            int wordIndex = i >>> 2;
            int shift = (i & 3) << 3;
            words[wordIndex] |= Byte.toUnsignedInt(bytes[i]) << shift;
        }
        return words;
    }

}
