package ua.millfreedom.rom2.model.net;

import ua.millfreedom.rom2.NetBuffer;
import ua.millfreedom.rom2.NetBufferInfo;
import ua.millfreedom.rom2.model.*;
import ua.millfreedom.rom2.model.enums.ProtocolId;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

/**
 * Native CLlDriver bridge methods used by the current Unit::Update port.
 */
public final class CLlDriver {
    // Native reset/default state from CLlDriver::CLlDriver @005068BF and CLlDriver::ResetProtocol @005078C4.
    private static final int RESET_PROTOCOL_ID = ProtocolId.TCP_IP | ProtocolId.DPSP_MODEM;
    // Native CLlDriver::CLlDriver @005068BF passes 3000 through CLlDriver::SetReliablePacketTimeoutMs @00493AA0.
    private static final int DEFAULT_RELIABLE_PACKET_TIMEOUT_MS = 3000;
    // Native BuildTcpSockAddr @00508F25 writes raw sin_port word 0x1F40, which is network port 0x401F.
    private static final int TCP_GAME_PORT = 0x401F;
    // Native StartTcpListener @0050916D writes raw sin_port word 0x1F41, which is network port 0x411F.
    private static final int TCP_DISCOVERY_PORT = 0x411F;
    // Native PingThreadTcp @00508E74 writes this magic dword after the active TCP client count.
    private static final int TCP_DISCOVERY_MAGIC = 0x56CE2AB5;
    // Java support for the visible raw TCP/IP replacement session browser; native DPSP_TCPIP uses this timeout.
    private static final int TCP_SESSION_DISCOVERY_TIMEOUT_MS = 0x5DC;
    // Native CLlDriver::CLlDriver @005068BF initializes nextSocketIdBase to 0x3BEF0000.
    private static final int INITIAL_SOCKET_ID_BASE = 0x3BEF0000;
    // Native AcceptThreadTcp @00508AB3 increments nextSocketIdBase by 0x10000 per accepted TCP client.
    private static final int SOCKET_ID_BASE_INCREMENT = 0x10000;
    // Java support, not a native field.
    private static final List<LlDriverSessionEntry> activeSessionBoundaryEntries = new ArrayList<>();
    // Java support, not a native field.
    private static final List<LlDriverConnectionOption> availableModemConnectionOptions = new ArrayList<>();
    // Java support for skipped native CLocalClient array fields used by latency chat commands.
    private static final Map<Integer, ClientLatencyBoundaryState> clientLatencyBoundaryBySocketId = new HashMap<>();
    // Java support, not a native field. Serializes TCP lifecycle state like native driverLock.
    private static final Object tcpLifecycleLock = new Object();
    // Java support for native CLocalClient TCP lookup by socket id.
    private static final Map<Integer, TcpConnection> tcpConnectionsBySocketId = new ConcurrentHashMap<>();
    // Java support for native _beginthread TCP accept/receive workers.
    private static ExecutorService tcpExecutor = createTcpExecutor();
    // Java support for native localClient.connection.socket while in server listener mode.
    private static ServerSocket tcpListenerSocket;
    // Java support for native discoveryClient.connection.socket while in server listener mode.
    private static ServerSocket tcpDiscoverySocket;
    // Java support for native localClient.connection while in client mode.
    private static TcpConnection localTcpConnection;
    // Java support for native clients array allocated by CLlDriver::StartServer @0050791A.
    private static TcpConnection[] serverTcpConnections = new TcpConnection[0];

    //0x00 Native CLlDriver.serverApp, set by CLlDriver::SetServerApp @00493A60.
    private static Object serverApp;
    // Native CLlDriver.serverMode mirrored for CServerApp client-removal callbacks.
    private static volatile boolean serverMode;
    // Native CLlDriver +0x04 localClient.connection.status; value 1 means active/connected.
    private static volatile boolean connectionActive;
    //0x7f4 Native CLlDriver.reliablePacketTimeoutMs, set by CLlDriver::SetReliablePacketTimeoutMs @00493AA0.
    private static int reliablePacketTimeoutMs = DEFAULT_RELIABLE_PACKET_TIMEOUT_MS;
    //0x7f8 Native CLlDriver.directPlaySessionFlagEnabled, set by CLlDriver::SetDirectPlaySessionFlag0x40Enabled @00493A80.
    private static boolean directPlaySessionFlagEnabled;
    //0x4e0 Native CLlDriver.protocolId mirrored for protocol-selection routing.
    private static int protocolId = RESET_PROTOCOL_ID;
    //0x4ec Native CLlDriver.connectAddress, set by CLlDriver::PrepareForConnect @00507B9A.
    private static String connectAddress = "";
    //0x594 Native CLlDriver.maxClients, set by CLlDriver::StartServer @0050791A.
    private static int maxClients = -1;
    //0x598 Native CLlDriver.clientCount.
    private static int clientCount;
    //0x59c Native CLlDriver.nextSocketIdBase.
    private static int nextSocketIdBase = INITIAL_SOCKET_ID_BASE;
    //0x5a0 Native CLlDriver.maxTcpSendDurationMs.
    private static int maxTcpSendDurationMs;
    //0x5c8+0x100 Native CLlDriver.preparedConnectionOption.addressText copied by PrepareForConnect @00507B9A.
    private static String preparedBindAddress = "";
    // Java support for the visible raw TCP/IP replacement server. Native DirectPlay clients use a non-null login
    // sentinel, while native raw TCP clients require a login request before PLAYER_JOIN_ACTION_02.
    private static boolean visibleRawTcpSessionServer;

    // not ported.
    private CLlDriver() {
    }

    /**
     * Native: CLlDriver::GetProtocolID @0044FFF0.
     * Fully ported.
     */
    public static int getProtocolId() {
        return protocolId;
    }

    /**
     * Native: CLlDriver::IsServerMode @00540AA0.
     * Fully ported.
     */
    public static boolean isServerMode() {
        return serverMode;
    }

    /**
     * Native: CLlDriver::GetStatus @00540450.
     * Fully ported.
     */
    public static boolean getStatus() {
        return connectionActive;
    }

    /**
     * Native support boundary for CLlDriver::SetProtocolID @00507864.
     * Java closes active TCP transport state and records the selected protocol id; DirectPlay initialization remains
     * outside the Java port.
     */
    public static boolean setProtocolId(int protocolId) {
        resetProtocolStateBoundary();
        CLlDriver.protocolId = protocolId;
        return true;
    }

    /**
     * Native support boundary for CLlDriver::GetAvailableProtocols @00507006 used by
     * MpConnectionDialogVisualObject::createDialogContent @004451FE.
     * Java exposes the implemented raw TCP/IP row directly; native DirectPlay provider enumeration is skipped outside
     * the Java port.
     */
    public static void loadAvailableProtocols(List<LlDriverProtocolOption> availableProtocols) {
        availableProtocols.clear();
        availableProtocols.add(new LlDriverProtocolOption("TCP/IP", ProtocolId.TCP_IP));
    }

    /**
     * Native support boundary for CLlDriver::ResetProtocol @005078C4.
     * Fully ported for TCP/IP transport shutdown; DirectPlay release remains outside the Java port.
     */
    private static void resetProtocolStateBoundary() {
        if (connectionActive) {
            handleNetworkErrorAndClose();
        } else {
            closeTcpTransport();
            serverMode = false;
        }
        protocolId = RESET_PROTOCOL_ID;
        connectAddress = "";
        preparedBindAddress = "";
        visibleRawTcpSessionServer = false;
    }

    /**
     * Native support boundary for CLlDriver::ResetDirectPlayConnection @0050A12E used by
     * CMainWindow::WindowProc @004852D8.
     * Java represents native HandleDPNetworkError/EnsureDirectPlay by clearing the modeled active connection.
     */
    public static void resetDirectPlayConnectionBoundary() {
        handleNetworkErrorAndClose();
    }

    /**
     * Native support boundary for CLlDriver::PrepareForConnect @00507B9A used by CMainWindow::WindowProc @004852D8.
     * Fully ported for TCP/IP connection state; DirectPlay setup remains outside the Java port.
     */
    public static boolean prepareForConnectBoundary(
            String connectAddress,
            LlDriverConnectionOption connectionOption
    ) {
        serverMode = false;
        connectionActive = false;
        closeTcpTransport();
        CLlDriver.connectAddress = connectAddress;
        preparedBindAddress = connectionOption == null ? "" : connectionOption.addressText;
        if (protocolId == ProtocolId.TCP_IP) {
            return true;
        }
        if ((protocolId == ProtocolId.DPSP_MODEM || protocolId == ProtocolId.DPSP_TCPIP)
                && Integer.compareUnsigned(reliablePacketTimeoutMs, 16000) < 0) {
            reliablePacketTimeoutMs = 16000;
        }
        return false;
    }

    /**
     * Native support boundary for CLlDriver::GetConnectionOptions @00507744 used by
     * ModemSettingsDialogVisualObject::Initialize @004470A2.
     * not ported.
     */
    public static void loadAvailableModemConnectionOptions(List<LlDriverConnectionOption> availableModemOptions) {
        availableModemOptions.clear();
        availableModemOptions.addAll(availableModemConnectionOptions);
    }

    /**
     * Native support boundary for CLlDriver::PrepareForConnect @00507B9A used by
     * ModemSettingsDialogVisualObject::OnMessage @0044793A.
     * Java leaves native DirectPlay dial/setup outside the port.
     */
    public static boolean prepareModemConnect(
            @SuppressWarnings("unused") String dialNumber,
            @SuppressWarnings("unused") LlDriverConnectionOption connectionOption
    ) {
        return false;
    }

    /**
     * Native support boundary for CLlDriver::PrepareForConnect @00507B9A used by
     * SerialSettingsDialogVisualObject::OnMessage @00448E48.
     * Java leaves native DirectPlay serial setup outside the port.
     */
    public static boolean prepareSerialConnectBoundary(@SuppressWarnings("unused") LlDriverConnectionOption connectionOption) {
        return false;
    }

    /**
     * Native support boundary for CLlDriver::StartServer @0050791A used by
     * SerialSettingsDialogVisualObject::OnMessage @00448E48.
     * Java leaves native DirectPlay serial server setup outside the port.
     */
    public static boolean startSerialServerBoundary(
            @SuppressWarnings("unused") String playerName,
            @SuppressWarnings("unused") LlDriverConnectionOption connectionOption
    ) {
        return false;
    }

    /**
     * Native support boundary for CLlDriver::StartServer @0050791A used by
     * ModemSettingsDialogVisualObject::OnMessage @0044793A.
     * Java leaves native DirectPlay modem server setup outside the port.
     */
    public static boolean startModemServerBoundary(
            @SuppressWarnings("unused") String playerName,
            @SuppressWarnings("unused") LlDriverConnectionOption connectionOption
    ) {
        return false;
    }

    /**
     * Native support boundary for CLlDriver::HandleNetworkErrorAndClose used by TcpIpSettingsHeaderDialogVisualObject::OnHeaderDialogAction @00449117.
     * Fully ported for TCP/IP transport shutdown; DirectPlay shutdown remains outside the Java port.
     */
    public static void handleNetworkErrorAndClose() {
        if (connectionActive && protocolId == ProtocolId.TCP_IP) {
            closeTcpTransport();
        }
        connectionActive = false;
        serverMode = false;
        visibleRawTcpSessionServer = false;
    }

    /**
     * Native support boundary for CLlDriver::GetActiveSessions @00507ECB used by
     * CenteredDialogContextArrayVisualObject::refreshActiveSessions @00446323.
     * Partially ported: native raw TCP/IP has no session browser, but Java synthesizes one raw TCP/IP row from the
     * native PingThreadTcp @00508DFA discovery response for the visible TCP-only replacement route. Java receives the
     * current modeled DirectPlay session snapshot for legacy providers, while native DirectPlay EnumSessions remains
     * outside the Java port.
     */
    public static void getActiveSessions(
            MultiplayerSessionDialogContext context,
            @SuppressWarnings("unused") BooleanSupplier cancelCallback,
            int timeoutMillis
    ) {
        context.sessionEntries.clear();
        if (protocolId == ProtocolId.TCP_IP) {
            discoverTcpSession(context, timeoutMillis);
            context.sessionEntryCount = context.sessionEntries.size();
            return;
        }
        context.sessionEntries.addAll(activeSessionBoundaryEntries);
        context.sessionEntryCount = context.sessionEntries.size();
    }

    /**
     * Native support boundary for CLlDriver::GetSessionPlayerNames @0050814C used by
     * CenteredDialogContextArrayVisualObject::rebuildCachedPlayerRows @00446DD7.
     * Java stores the recovered player-name rows on the boundary session entry; the DirectPlay EnumPlayers worker is
     * skipped outside the Java port.
     */
    public static List<String> getSessionPlayerNames(LlDriverSessionEntry session) {
        return session.playerNames;
    }

    /**
     * Native support boundary for CLlDriver::SignalSessionEnumerationEvent @005080D6 used by
     * CenteredDialogContextArrayVisualObject::OnMessage @004463C4.
     * not ported.
     */
    public static void signalSessionEnumerationEventBoundary() {
    }

    /**
     * Native support boundary for CLlDriver::Connect @005081B9 in
     * MapVisualObject::connectAndJoinSession @0040D480.
     * Fully ported for TCP/IP prepared-address connections; DirectPlay session-browser connect remains modeled.
     */
    public static boolean connectMultiplayerClientBoundary(
            @SuppressWarnings("unused") String playerName,
            LlDriverSessionEntry sessionEntry
    ) {
        if (connectionActive) {
            handleNetworkErrorAndClose();
        }
        if (protocolId == ProtocolId.TCP_IP) {
            return connectTcp(connectAddress);
        }
        if (sessionEntry == null) {
            return false;
        }
        connectionActive = true;
        return true;
    }

    /**
     * Native support extracted from CLlDriver::SetServerApp @00493A60 in
     * CMainWindow::startDedicatedMultiplayerSession @0048F156 and
     * CMainWindow::startHatDedicatedServer @0048EF1F.
     */
    public static void bindLocalServerAppBoundary() {
        setServerApp(CServerApp.localEndpointHandle());
    }

    /**
     * Native support extracted from CLlDriver::SetServerApp @00493A60 in CMainWindow::connectToServerAddress @0048E90F and
     * CMainWindow::connectToSelectedMultiplayerSession @0048E764.
     */
    public static void bindRemoteServerAppBoundary() {
        setServerApp(CServerApp.remoteEndpointHandle());
    }

    /**
     * Java support for the visible raw TCP/IP replacement route started from
     * CMainWindow::continueAfterMultiplayerSessionDialogClose @004891D8 and
     * CMainWindow::startDedicatedMultiplayerSession @0048F156.
     */
    public static void enableVisibleRawTcpSessionServerBoundary() {
        visibleRawTcpSessionServer = true;
    }

    /**
     * Java support for CServerApp::onClientActivated @00505F07 on the visible raw TCP/IP replacement route.
     */
    public static boolean isVisibleRawTcpSessionServer() {
        return visibleRawTcpSessionServer;
    }

    /**
     * Native: CLlDriver::SetServerApp @00493A60.
     * Fully ported.
     */
    private static void setServerApp(Object serverApp) {
        CLlDriver.serverApp = serverApp;
    }

    /**
     * Native: CLlDriver::SetDirectPlaySessionFlag0x40Enabled @00493A80.
     * Fully ported.
     */
    public static void setDirectPlaySessionFlagEnabled(boolean enabled) {
        directPlaySessionFlagEnabled = enabled;
    }

    /**
     * Native: CLlDriver::SetReliablePacketTimeoutMs @00493AA0.
     * Fully ported.
     */
    public static void setReliablePacketTimeoutMs(int timeoutMillis) {
        reliablePacketTimeoutMs = timeoutMillis;
    }

    /**
     * Native support boundary for CLlDriver::StartServer @0050791A in
     * CMainWindow::startDedicatedMultiplayerSession @0048F156 and
     * CMainWindow::startHatDedicatedServer @0048EF1F.
     * Fully ported for TCP/IP listener setup; DirectPlay server setup remains modeled by the existing boundary state.
     */
    public static boolean startMultiplayerServerBoundary(
            int maxPlayers,
            @SuppressWarnings("unused") String playerName,
            String bindAddress
    ) {
        if (connectionActive) {
            handleNetworkErrorAndClose();
        }
        serverMode = true;
        maxClients = maxPlayers;
        clientCount = 0;
        if (protocolId == ProtocolId.TCP_IP) {
            return startTcpListener(maxPlayers, bindAddress == null ? "0.0.0.0" : bindAddress);
        }
        connectionActive = true;
        return true;
    }

    /**
     * Native support boundary for CLlDriver::RestartModemServer @0050A0F8 from
     * CServerApp::onClientRemoved @00505D02.
     * Partially ported: native non-modem protocols return success without side effects; DirectPlay modem restart remains
     * outside the Java TCP/IP transport model.
     */
    public static boolean restartModemServerBoundary() {
        if (protocolId != ProtocolId.DPSP_MODEM) {
            return true;
        }
        connectionActive = false;
        return false;
    }

    /**
     * Native support boundary for CLlDriver::DisconnectClient @005082E0 from
     * CServerApp::RemoveClient @00500AD9. Fully ported for TCP/IP clients.
     */
    public static void disconnectClientBoundary(int socketId) {
        if (protocolId != ProtocolId.TCP_IP) {
            return;
        }
        TcpConnection connection = getTcpConnectionBySocketId(socketId);
        if (connection != null && isTcpConnectionActive(connection)) {
            closeTcpConnection(connection, true);
            clientCount = Math.max(0, clientCount - 1);
        }
    }

    /**
     * Native support boundary for CLlDriver::SendData @005084A7 from
     * CBufferManager::FlushWriteBuffer @0050036A. Fully ported for TCP/IP clients.
     */
    public static void sendDataBoundary(
            int socketId,
            NetBuffer buffer
    ) {
        if (protocolId != ProtocolId.TCP_IP) {
            CServerApp.releaseNetBuffer(serverApp, buffer);
            return;
        }
        TcpConnection connection = getTcpConnectionBySocketId(socketId);
        if (connection == null || !isTcpConnectionActive(connection)) {
            CServerApp.releaseNetBuffer(serverApp, buffer);
            System.out.printf("CLlDriver::SendData @005084A7 connection for socket {%d} is not connected.%n", socketId);
            return;
        }
        sendDataTcp(connection, buffer);
    }

    /**
     * Native support boundary for CLlDriver::PruneClosedTcpClient @005083F5 from
     * CBufferManager::FlushWriteBuffer @0050036A. Fully ported for TCP/IP clients.
     */
    public static void pruneClosedTcpClientBoundary(int socketId) {
        if (protocolId != ProtocolId.TCP_IP) {
            return;
        }
        TcpConnection connection = getTcpConnectionBySocketId(socketId);
        if (connection != null && connection.socketClosedByPeer) {
            closeTcpConnection(connection, true);
            clientCount = Math.max(0, clientCount - 1);
        }
    }

    /**
     * Native support boundary for CLlDriver::PrepareForConnect @00507B9A and CLlDriver::Connect @005081B9 in
     * CMainWindow::connectToServerAddress @0048E90F.
     * Fully ported for TCP/IP direct-address connections.
     */
    public static boolean connectDirectAddressBoundary(
            String address,
            @SuppressWarnings("unused") String playerName
    ) {
        connectAddress = address;
        preparedBindAddress = "";
        return connectTcp(address);
    }

    /**
     * Native support boundary for CLlDriver::SetClientSendIntervalMs @00508774 used by
     * MapVisualObject::HandleGameAction @0040D9B2 and
     * GameServer::handleChatCommand @004F3D68 and
     * GameServer::handleServerGameAction @004F515D.
     * Java mirrors the CLocalClient::sendIntervalMsOverride field write for active boundary clients.
     */
    public static void setClientSendIntervalMs(int socketId, int sendIntervalMs) {
        ClientLatencyBoundaryState client = clientLatencyBoundaryBySocketId.get(socketId);
        if (client != null) {
            client.sendIntervalMsOverride = sendIntervalMs;
        }
    }

    /**
     * Native support extracted from CLlDriver::GetClientSendIntervalMs @005087A4.
     */
    public static int getClientSendIntervalMs(int socketId) {
        ClientLatencyBoundaryState client = clientLatencyBoundaryBySocketId.get(socketId);
        if (client == null) {
            return -1;
        }
        if (client.sendIntervalMsOverride == 0) {
            return client.latencyMs * 2;
        }
        return client.sendIntervalMsOverride;
    }

    /**
     * Native support extracted from CLlDriver::GetClientRetransmitRate @005087F8.
     */
    public static long getClientRetransmitRate(int socketId) {
        ClientLatencyBoundaryState client = clientLatencyBoundaryBySocketId.get(socketId);
        if (client == null) {
            return 0;
        }
        if (client.sequenceOut > 1000) {
            client.sequenceOut /= 10;
            client.retransmitCount /= 10;
        }
        if (client.retransmitCount == 0) {
            return 0;
        }
        return (long) ((client.retransmitCount * 100000.0) / client.sequenceOut);
    }

    /**
     * Native support extracted from CLocalClient::CLocalClient @00506709 allocation state.
     */
    public static void registerClientLatencyBoundary(int socketId) {
        clientLatencyBoundaryBySocketId.put(socketId, new ClientLatencyBoundaryState());
    }

    /**
     * Native support extracted from CLocalClient::~CLocalClient @005067E3 removal boundary.
     */
    public static void unregisterClientLatencyBoundary(int socketId) {
        clientLatencyBoundaryBySocketId.remove(socketId);
    }

    /**
     * Native support extracted from CLlDriver TCP _beginthread sites at
     * StartTcpListener @005090C3, AcceptThreadTcp @00508AB3, and ConnectTcp @00509218.
     */
    private static ExecutorService createTcpExecutor() {
        AtomicInteger threadIndex = new AtomicInteger();
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable, "rom2-tcp-" + threadIndex.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
        return Executors.newCachedThreadPool(threadFactory);
    }

    /**
     * Native support extracted from CLlDriver::StartTcpListener @00508FD0,
     * CLlDriver::AcceptThreadTcp @00508AB3, and CLlDriver::ConnectTcp @00509218 TCP thread restart behavior.
     */
    private static ExecutorService tcpExecutor() {
        synchronized (tcpLifecycleLock) {
            if (tcpExecutor.isShutdown()) {
                tcpExecutor = createTcpExecutor();
            }
            return tcpExecutor;
        }
    }

    /**
     * Native support extracted from CLlDriver::StartTcpListener @00508FD0.
     */
    private static boolean startTcpListener(int maxPlayers, String bindAddress) {
        ServerSocket listener = null;
        ServerSocket discoveryListener = null;
        try {
            listener = new ServerSocket();
            listener.setReuseAddress(true);
            String tcpBindAddress = bindAddress.isBlank() ? "0.0.0.0" : bindAddress;
            listener.bind(new InetSocketAddress(tcpBindAddress, TCP_GAME_PORT), maxPlayers);
            discoveryListener = new ServerSocket();
            discoveryListener.setReuseAddress(true);
            discoveryListener.bind(new InetSocketAddress(tcpBindAddress, TCP_DISCOVERY_PORT), 5);
            ServerSocket activeListener = listener;
            ServerSocket activeDiscoveryListener = discoveryListener;
            synchronized (tcpLifecycleLock) {
                serverTcpConnections = new TcpConnection[maxPlayers];
                tcpListenerSocket = activeListener;
                tcpDiscoverySocket = activeDiscoveryListener;
                clientCount = 0;
                connectionActive = true;
            }
            tcpExecutor().execute(() -> acceptThreadTcp(activeListener));
            tcpExecutor().execute(() -> pingThreadTcp(activeDiscoveryListener));
            return true;
        } catch (IOException e) {
            closeServerSocketOnly(listener);
            closeServerSocketOnly(discoveryListener);
            closeTcpTransport();
            return false;
        }
    }

    /**
     * Native support extracted from CLlDriver::AcceptThreadTcp @00508AB3.
     */
    private static void acceptThreadTcp(ServerSocket listener) {
        while (connectionActive && serverMode && listener == tcpListenerSocket) {
            try {
                Socket socket = listener.accept();
                acceptTcpClient(socket);
            } catch (IOException e) {
                if (connectionActive) {
                    System.out.println("CLlDriverAcceptThreadTcp @00508AB3 unable to accept TCP client: "
                            + e.getMessage());
                }
            }
        }
    }

    /**
     * Native support extracted from CLlDriver::PingThreadTcp @00508DFA.
     */
    private static void pingThreadTcp(ServerSocket discoveryListener) {
        while (connectionActive && serverMode && discoveryListener == tcpDiscoverySocket) {
            try (Socket socket = discoveryListener.accept()) {
                socket.setTcpNoDelay(true);
                OutputStream output = socket.getOutputStream();
                output.write(encodeTcpDiscoveryResponse());
                output.flush();
            } catch (IOException e) {
                if (connectionActive) {
                    System.out.println("CLlDriverPingThreadTcp @00508DFA unable to accept TCP discovery client: "
                            + e.getMessage());
                }
            }
        }
    }

    /**
     * Native support extracted from CLlDriver::PingThreadTcp @00508DFA send buffer layout.
     */
    private static byte[] encodeTcpDiscoveryResponse() {
        int activeClientCount;
        synchronized (tcpLifecycleLock) {
            activeClientCount = clientCount;
        }
        return ByteBuffer.allocate(Long.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(activeClientCount)
                .putInt(TCP_DISCOVERY_MAGIC)
                .array();
    }

    /**
     * Java support extracted from native CLlDriver::PingThreadTcp @00508DFA response shape and
     * CLlDriver::GetActiveSessions @00507ECB raw TCP/IP bypass.
     */
    private static void discoverTcpSession(MultiplayerSessionDialogContext context, int timeoutMillis) {
        int effectiveTimeoutMillis = timeoutMillis > 0 ? timeoutMillis : TCP_SESSION_DISCOVERY_TIMEOUT_MS;
        Socket socket = new Socket();
        try {
            socket.setTcpNoDelay(true);
            if (!preparedBindAddress.isBlank()) {
                socket.bind(new InetSocketAddress(preparedBindAddress, 0));
            }
            socket.connect(new InetSocketAddress(connectAddress, TCP_DISCOVERY_PORT), effectiveTimeoutMillis);
            socket.setSoTimeout(effectiveTimeoutMillis);
            byte[] responseBytes = socket.getInputStream().readNBytes(Long.BYTES);
            if (responseBytes.length != Long.BYTES) {
                return;
            }
            ByteBuffer response = ByteBuffer.wrap(responseBytes).order(ByteOrder.LITTLE_ENDIAN);
            response.getInt();
            if (response.getInt() != TCP_DISCOVERY_MAGIC) {
                return;
            }
            context.sessionEntries.add(new LlDriverSessionEntry(tcpSessionName()));
        } catch (IOException ignored) {
            // Native raw GetActiveSessions has no browser row; failed Java discovery leaves the list empty.
        } finally {
            closeSocketOnly(socket);
        }
    }

    /**
     * Java support for the synthesized raw TCP/IP session-browser row.
     * not ported.
     */
    private static String tcpSessionName() {
        return connectAddress.isBlank() ? "localhost" : connectAddress;
    }

    /**
     * Native support extracted from CLlDriver::AcceptThreadTcp @00508AB3 accepted-client setup.
     */
    private static void acceptTcpClient(Socket socket) throws IOException {
        socket.setTcpNoDelay(true);
        synchronized (tcpLifecycleLock) {
            int clientIndex = nextAvailableServerTcpClientIndex();
            if (clientIndex < 0) {
                socket.close();
                System.out.println("CLlDriverAcceptThreadTcp @00508AB3 client limit reached.");
                return;
            }

            int socketId = nextSocketIdBase | clientIndex;
            nextSocketIdBase += SOCKET_ID_BASE_INCREMENT;
            CBufferManager bufferManager = CServerApp.newNetworkClient(serverApp, socketId);
            bufferManager.setAddressText(socket.getInetAddress().getHostAddress());
            TcpConnection connection = new TcpConnection(socketId, socket, bufferManager, clientIndex);
            serverTcpConnections[clientIndex] = connection;
            tcpConnectionsBySocketId.put(socketId, connection);
            clientCount++;
            startTcpReceiveThread(connection);
        }
    }

    /**
     * Native support extracted from CLlDriver::AcceptThreadTcp @00508AB3 client-slot search.
     */
    private static int nextAvailableServerTcpClientIndex() {
        if (clientCount >= maxClients) {
            return -1;
        }
        for (int i = 0; i < serverTcpConnections.length; i++) {
            TcpConnection connection = serverTcpConnections[i];
            if (connection == null || !connection.active) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Native support extracted from CLlDriver::ConnectTcp @00509218.
     */
    private static boolean connectTcp(String address) {
        if (connectionActive) {
            handleNetworkErrorAndClose();
        }
        serverMode = false;
        maxClients = 1;
        clientCount = 0;
        Socket socket = new Socket();
        try {
            socket.setTcpNoDelay(true);
            if (!preparedBindAddress.isBlank()) {
                socket.bind(new InetSocketAddress(preparedBindAddress, 0));
            }
            socket.connect(new InetSocketAddress(address, TCP_GAME_PORT), tcpConnectTimeoutMillis());
            int socketId = nextSocketIdBase;
            CBufferManager bufferManager = CServerApp.newNetworkClient(serverApp, socketId);
            TcpConnection connection = new TcpConnection(socketId, socket, bufferManager, -1);
            synchronized (tcpLifecycleLock) {
                localTcpConnection = connection;
                tcpConnectionsBySocketId.put(socketId, connection);
                clientCount = 1;
                connectionActive = true;
                startTcpReceiveThread(connection);
            }
            return true;
        } catch (IOException e) {
            closeSocketOnly(socket);
            closeTcpTransport();
            return false;
        }
    }

    /**
     * Native support extracted from CLlDriver::ConnectTcp @00509218 blocking connect behavior and
     * CLlDriver::SetReliablePacketTimeoutMs @00493AA0.
     */
    private static int tcpConnectTimeoutMillis() {
        return reliablePacketTimeoutMs < 0 ? 0 : reliablePacketTimeoutMs;
    }

    /**
     * Native support extracted from CLlDriver::RecvThreadTcp @0050889B.
     */
    private static void startTcpReceiveThread(TcpConnection connection) {
        tcpExecutor().execute(() -> receiveThreadTcp(connection));
    }

    /**
     * Native support extracted from CLlDriver::RecvThreadTcp @0050889B.
     */
    private static void receiveThreadTcp(TcpConnection connection) {
        try {
            InputStream input = connection.socket.getInputStream();
            while (isTcpConnectionActive(connection)) {
                NetBuffer buffer = readTcpNetBuffer(input);
                if (buffer == null) {
                    markTcpSocketClosedByPeer(connection);
                    return;
                }
                connection.bufferManager.ReceiveData(buffer);
            }
        } catch (IOException e) {
            if (connection.active) {
                markTcpSocketClosedByPeer(connection);
            }
        }
    }

    /**
     * Native support extracted from CLlDriver::RecvThreadTcp @0050889B wire-buffer read loop.
     */
    private static NetBuffer readTcpNetBuffer(InputStream input) throws IOException {
        while (true) {
            byte[] headerBytes = input.readNBytes(NetBuffer.WIRE_HEADER_SIZE);
            if (headerBytes.length != NetBuffer.WIRE_HEADER_SIZE) {
                return null;
            }
            int payloadLength = ByteBuffer.wrap(headerBytes)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .getShort(0) & 0xFFFF;
            if (payloadLength == 0 || payloadLength >= NetBuffer.PAYLOAD_CAPACITY + 1) {
                System.out.printf(
                        "CLlDriverRecvThreadTcp @0050889B received invalid payload length {%d}.%n",
                        payloadLength
                );
                continue;
            }
            NetBuffer buffer = CServerApp.acquireNetBuffer(serverApp);
            buffer.Clear();
            decodeTcpHeader(headerBytes, buffer.GetHeader());
            byte[] payloadBytes = input.readNBytes(payloadLength);
            if (payloadBytes.length != payloadLength) {
                CServerApp.releaseNetBuffer(serverApp, buffer);
                return null;
            }
            System.arraycopy(payloadBytes, 0, buffer.payload, 0, payloadLength);
            buffer.SetPayloadSize(payloadLength);
            return buffer;
        }
    }

    /**
     * Native support extracted from CLlDriver::RecvThreadTcp @0050889B memcpy into NetBufferInfo.
     */
    private static void decodeTcpHeader(byte[] headerBytes, NetBufferInfo header) {
        ByteBuffer wire = ByteBuffer.wrap(headerBytes).order(ByteOrder.LITTLE_ENDIAN);
        header.payloadLengthOrSequence = wire.getInt();
        header.compressionStreamId = wire.get() & 0xFF;
        header.compressedBitCount = wire.getShort() & 0xFFFF;
        header.segmentMarkerCount = wire.get() & 0xFF;
    }

    /**
     * Native support extracted from CLlDriver::SendDataTcp @005093D8.
     */
    private static void sendDataTcp(TcpConnection connection, NetBuffer buffer) {
        long startedNanos = System.nanoTime();
        try {
            byte[] wireBytes = encodeTcpWireBytes(buffer);
            synchronized (connection.sendLock) {
                OutputStream output = connection.socket.getOutputStream();
                output.write(wireBytes);
                output.flush();
            }
            int durationMillis = (int) ((System.nanoTime() - startedNanos) / 1_000_000L);
            if (maxTcpSendDurationMs < durationMillis) {
                maxTcpSendDurationMs = durationMillis;
            }
            CServerApp.releaseNetBuffer(serverApp, buffer);
            if (durationMillis > 500) {
                closeTcpConnection(connection, true);
                clientCount = Math.max(0, clientCount - 1);
            }
        } catch (IOException e) {
            CServerApp.releaseNetBuffer(serverApp, buffer);
            closeTcpConnection(connection, true);
            clientCount = Math.max(0, clientCount - 1);
        }
    }

    /**
     * Native support extracted from CLlDriver::SendData @005084A7 payload-length write and
     * CLlDriver::SendDataTcp @005093D8 socket send source pointer.
     */
    private static byte[] encodeTcpWireBytes(NetBuffer buffer) {
        int payloadLength = buffer.GetWireSize() - NetBuffer.WIRE_HEADER_SIZE;
        NetBufferInfo header = buffer.GetHeader();
        header.payloadLengthOrSequence = (header.payloadLengthOrSequence & 0xFFFF0000) | (payloadLength & 0xFFFF);
        ByteBuffer wire = ByteBuffer.allocate(buffer.GetWireSize()).order(ByteOrder.LITTLE_ENDIAN);
        wire.putInt(header.payloadLengthOrSequence);
        wire.put((byte) header.compressionStreamId);
        wire.putShort((short) header.compressedBitCount);
        wire.put((byte) header.segmentMarkerCount);
        wire.put(buffer.payload, 0, payloadLength);
        return wire.array();
    }

    /**
     * Native support extracted from CLlDriver::GetClientBySocketId @0050827E.
     */
    private static TcpConnection getTcpConnectionBySocketId(int socketId) {
        if (!serverMode && localTcpConnection != null) {
            return localTcpConnection;
        }
        return tcpConnectionsBySocketId.get(socketId);
    }

    /**
     * Native support extracted from CLlDriver::IsConnectionActive @005086E3.
     */
    private static boolean isTcpConnectionActive(TcpConnection connection) {
        return connectionActive && connection.active;
    }

    /**
     * Native support extracted from CLlDriver::RecvThreadTcp @0050889B socket-close tail.
     */
    private static void markTcpSocketClosedByPeer(TcpConnection connection) {
        connection.socketClosedByPeer = true;
        // Server accepted clients stay active until PruneClosedTcpClient/CloseClientConnection owns removal.
        if (connection == localTcpConnection) {
            connection.active = false;
            connectionActive = false;
        }
        closeSocketOnly(connection.socket);
    }

    /**
     * Native support extracted from CLlDriver::HandleNetworkError @00509603.
     */
    private static void closeTcpTransport() {
        synchronized (tcpLifecycleLock) {
            connectionActive = false;
            closeTcpListener();
            closeTcpDiscoveryListener();
            for (TcpConnection connection : new ArrayList<>(tcpConnectionsBySocketId.values())) {
                closeTcpConnection(connection, true);
            }
            tcpConnectionsBySocketId.clear();
            serverTcpConnections = new TcpConnection[0];
            localTcpConnection = null;
            clientCount = 0;
            tcpExecutor.shutdownNow();
        }
    }

    /**
     * Native support extracted from CLlDriver::CloseClientConnection @0050957A.
     */
    private static void closeTcpConnection(TcpConnection connection, boolean queueRemoval) {
        synchronized (tcpLifecycleLock) {
            if (!connection.active && connection.bufferManager == null) {
                return;
            }
            connection.active = false;
            closeSocketOnly(connection.socket);
            tcpConnectionsBySocketId.remove(connection.socketId);
            if (connection.serverClientIndex >= 0
                    && connection.serverClientIndex < serverTcpConnections.length
                    && serverTcpConnections[connection.serverClientIndex] == connection) {
                serverTcpConnections[connection.serverClientIndex] = null;
            }
            if (localTcpConnection == connection) {
                localTcpConnection = null;
            }
            if (queueRemoval && connection.bufferManager != null) {
                CServerApp.queueNetworkClientRemoval(serverApp, connection.bufferManager);
            }
            connection.bufferManager = null;
        }
    }

    /**
     * Native support extracted from CLlDriver::CloseClientConnection @0050957A listener/socket close calls.
     */
    private static void closeTcpListener() {
        ServerSocket listener = tcpListenerSocket;
        tcpListenerSocket = null;
        closeServerSocketOnly(listener);
    }

    /**
     * Native support extracted from CLlDriver::CloseClientConnection @0050957A discovery socket close calls.
     */
    private static void closeTcpDiscoveryListener() {
        ServerSocket listener = tcpDiscoverySocket;
        tcpDiscoverySocket = null;
        closeServerSocketOnly(listener);
    }

    /**
     * Native support extracted from CLlDriver::CloseClientConnection @0050957A socket close calls.
     */
    private static void closeServerSocketOnly(ServerSocket socket) {
        if (socket == null) {
            return;
        }
        try {
            socket.close();
        } catch (IOException ignored) {
            // Native closesocket failure is not surfaced to callers here.
        }
    }

    /**
     * Native support extracted from CLlDriver::CloseClientConnection @0050957A socket close calls.
     */
    private static void closeSocketOnly(Socket socket) {
        try {
            socket.close();
        } catch (IOException ignored) {
            // Native closesocket failure is not surfaced to callers here.
        }
    }

    /**
     * Native support structure for CLocalClient latency fields used by CLlDriver latency helpers.
     */
    private static final class ClientLatencyBoundaryState {
        // Native CLocalClient::latencyTracker.latencyMs, read through DpLatencyTracker::GetSendIntervalMs @005066F3.
        private int latencyMs;
        // Native CLocalClient::sendIntervalMsOverride, read/written by CLlDriver latency helpers @00508774/@005087A4.
        private int sendIntervalMsOverride;
        // Native CLocalClient::sequenceOut, read by CLlDriver::GetClientRetransmitRate @005087F8.
        private int sequenceOut;
        // Native CLocalClient::retransmitCount, read by CLlDriver::GetClientRetransmitRate @005087F8.
        private int retransmitCount;
    }

    /**
     * Native support structure for CLocalClient.connection fields used by CLlDriver TCP helpers.
     */
    private static final class TcpConnection {
        // Native CNetConnection::socketId.
        private final int socketId;
        // Native CNetConnection::socket.
        private final Socket socket;
        // Native CNetConnection::bufferManager.
        private CBufferManager bufferManager;
        // Java support, not a native field. Serializes OutputStream writes for one TCP socket.
        private final Object sendLock = new Object();
        // Java support, not a native field. Mirrors the native server clients-array index.
        private final int serverClientIndex;
        // Native CNetConnection::status.
        private volatile boolean active = true;
        // Java support for native RecvThreadTcp setting socket to INVALID_SOCKET before prune.
        private volatile boolean socketClosedByPeer;

        /**
         * Native support extracted from CNetConnection fields initialized in
         * CLlDriver::ConnectTcp @00509218 and CLlDriver::AcceptThreadTcp @00508AB3.
         */
        private TcpConnection(int socketId, Socket socket, CBufferManager bufferManager, int serverClientIndex) {
            this.socketId = socketId;
            this.socket = socket;
            this.bufferManager = bufferManager;
            this.serverClientIndex = serverClientIndex;
        }
    }
}
