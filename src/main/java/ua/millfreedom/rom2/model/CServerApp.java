package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.model.net.CBufferManager;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.NetBuffer;
import ua.millfreedom.rom2.model.action.BuildingHealthAction;
import ua.millfreedom.rom2.model.action.AreaEffectAction;
import ua.millfreedom.rom2.model.action.CGameAction;
import ua.millfreedom.rom2.model.action.ChatTextAction;
import ua.millfreedom.rom2.model.action.CreatureKnowledgeAction;
import ua.millfreedom.rom2.model.action.DiplomacyAction;
import ua.millfreedom.rom2.model.action.ItemListAction;
import ua.millfreedom.rom2.model.action.EffectAction;
import ua.millfreedom.rom2.model.action.EffectMultiFromAction;
import ua.millfreedom.rom2.model.action.EffectMultiTargetAction;
import ua.millfreedom.rom2.model.action.EnemyEquipmentAction;
import ua.millfreedom.rom2.model.action.Fixed3ByteAction;
import ua.millfreedom.rom2.model.action.FixedDwordAction0E;
import ua.millfreedom.rom2.model.action.GameEventNotificationAction;
import ua.millfreedom.rom2.model.action.IncomingGameActionFactory;
import ua.millfreedom.rom2.model.action.InnQuestsAction;
import ua.millfreedom.rom2.model.action.LatencySettingAction;
import ua.millfreedom.rom2.model.action.NewSegmentAction;
import ua.millfreedom.rom2.model.action.NewPlayerAction;
import ua.millfreedom.rom2.model.action.PlayerKnowledgeProgressAction;
import ua.millfreedom.rom2.model.action.PlayerKickedAction;
import ua.millfreedom.rom2.model.action.PlayerQuestsAction;
import ua.millfreedom.rom2.model.action.PointProjectileVisualAction;
import ua.millfreedom.rom2.model.action.QuestObjectivesQueryOpenAction;
import ua.millfreedom.rom2.model.action.RangedAttackAction;
import ua.millfreedom.rom2.model.action.SackAction;
import ua.millfreedom.rom2.model.action.SackRemovedAction;
import ua.millfreedom.rom2.model.action.ShortArrayBlobAction;
import ua.millfreedom.rom2.model.action.TileVisibilityMaskAction;
import ua.millfreedom.rom2.model.action.TwoDwordAction;
import ua.millfreedom.rom2.model.action.UnitCommandStartAction;
import ua.millfreedom.rom2.model.action.UnitDamagedAction;
import ua.millfreedom.rom2.model.action.UnitEquipmentStateAction;
import ua.millfreedom.rom2.model.action.UnitInventoryStateAction;
import ua.millfreedom.rom2.model.action.UnitChangeAction;
import ua.millfreedom.rom2.model.action.UnitTokenAction;
import ua.millfreedom.rom2.model.column.UnitColumn;
import ua.millfreedom.rom2.model.compression.ByteHuffmanPacker;
import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.enums.ProtocolId;
import ua.millfreedom.rom2.model.net.CLlDriver;
import ua.millfreedom.rom2.model.spell.AreaEffect;
import ua.millfreedom.rom2.model.spell.Spell;
import ua.millfreedom.rom2.model.spell.SpellEffect;
import ua.millfreedom.rom2.model.unit.UnitInfo;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.unit.UnitDirtyFlags;
import ua.millfreedom.rom2.model.unit.humanoid.Humanoid;
import ua.millfreedom.rom2.model.unit.humanoid.human.Human;
import ua.millfreedom.rom2.model.unit.humanoid.human.HumanInfo;
import ua.millfreedom.rom2.data.ByteBufferAdaptor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Native CServerApp bridge methods used by the current Unit::Update port.
 */
public final class CServerApp {
    // Native event kind 1 passed through CServerApp::sendGameEventNotification @005052D2.
    public static final int PATH_UNREACHABLE_EVENT = 1;
    // Native event kind 2 passed through CServerApp::sendGameEventNotification @005052D2.
    public static final int SKILL_IMPROVED_EVENT = 2;
    // Native event kind 5 passed through CServerApp::sendGameEventNotification @005052D2.
    public static final int CHEAT_NOTIFICATION_EVENT = 5;
    private static final String PACKER1_TABLE_PATH = "Packer1.dat";
    private static final String PACKER2_TABLE_PATH = "Packer2.dat";
    private static final int PACKER_FREQUENCY_TABLE_BYTES = 0x400;
    private static final int SPECIAL_PLAYER_ID_FIRST = 0x10;
    private static final int SPECIAL_PLAYER_ID_LIMIT = 0x20;
    private static final int CLIENT_SOCKET_ID_MASK = 0x3FFF;
    private static final int PLAYER_RELATION_VISIBLE_MASK = 0x10;
    private static final int UNIT_STATUS_HIDDEN_OR_INACTIVE = 0x08;
    private static final int HUMANOID_PUBLIC_TYPE_FIRST = 0x21;
    private static final int HUMANOID_PUBLIC_TYPE_LAST = 0x3F;
    private static final int EQUIPMENT_SLOT_MASK = 0x0FFF;
    private static final int EQUIPMENT_BROADCAST_MASK = 0x0FFB;
    private static final int ALL_UNIT_UPDATE_FLAGS = -1;
    private static final int SCENARIO_MISSION_ENTRY_TEMPLATE_BASE = 10_000;
    private static final int SCENARIO_MISSION_ENTRY_CHAPTER_VARIANT_BASE = 0x270E;
    private static final int SCENARIO_MISSION_ENTRY_VARIANT_LIMIT = 10;
    private static final int INITIAL_LOW_ID_UNIT_AND_CORPSE_UPDATE_FLAGS = 0xA35FFFFF;
    private static final int INITIAL_HIGH_ID_UNIT_UPDATE_FLAGS = UnitDirtyFlags.toValue(
            UnitDirtyFlags.SPELLBOOK,
            UnitDirtyFlags.POSITION_AND_FACING,
            UnitDirtyFlags.MP,
            UnitDirtyFlags.HP
    );
    private static final int INITIAL_HIGH_ID_CORPSE_UPDATE_FLAGS = UnitDirtyFlags.toValue(
            UnitDirtyFlags.POSITION_AND_FACING,
            UnitDirtyFlags.MP,
            UnitDirtyFlags.HP
    );
    private static final int LOCAL_CAMPAIGN_INVENTORY_SERVER_ID_FIRST = 0x16;
    private static final int LOCAL_CAMPAIGN_INVENTORY_SERVER_ID_LIMIT = 0x18;
    private static final int SPELL_EFFECT_VISIBILITY_FIRST_DEFERRED_ACTION_ID = GameActionId.AREA_EFFECT_ACTION_87.id;
    private static final int SPELL_EFFECT_VISIBILITY_LAST_DEFERRED_ACTION_ID = GameActionId.EFFECT_MULTI_TARGET_ACTION_8A.id;
    private static final int SPELL_EFFECT_TOKEN_VISUAL_TYPE = 0x16;
    private static final int HIDDEN_MOVE_DEFERRED_FLAGS = UnitDirtyFlags.toValue(
            UnitDirtyFlags.VISIBILITY_STATE,
            UnitDirtyFlags.POSITION_AND_FACING
    );
    private static final int DIRECT_DELIVERY_IP_ADDRESS = -1;
    private static final int NET_UPDATE_BODY_SKIP_MASK = UnitDirtyFlags.toValue(
            UnitDirtyFlags.VISIBILITY_STATE,
            UnitDirtyFlags.EFFECTS,
            UnitDirtyFlags.EQUIPPED_ITEMS,
            UnitDirtyFlags.INVENTORY_ITEMS
    );
    private static final int NON_OWNER_NON_HUMANOID_MASK_WITH_MAX_MP = UnitDirtyFlags.toValue(
            UnitDirtyFlags.DISPLAY_NAME,
            UnitDirtyFlags.VISIBILITY_STATE,
            UnitDirtyFlags.EFFECTS,
            UnitDirtyFlags.SERVER_ID,
            UnitDirtyFlags.OWNER_PLAYER_ID,
            UnitDirtyFlags.TYPE_AND_FACE,
            UnitDirtyFlags.POSITION_AND_FACING,
            UnitDirtyFlags.MAX_MP,
            UnitDirtyFlags.HP
    );
    private static final int NON_OWNER_NON_HUMANOID_MASK_WITH_MP = UnitDirtyFlags.toValue(
            UnitDirtyFlags.DISPLAY_NAME,
            UnitDirtyFlags.VISIBILITY_STATE,
            UnitDirtyFlags.EFFECTS,
            UnitDirtyFlags.SERVER_ID,
            UnitDirtyFlags.OWNER_PLAYER_ID,
            UnitDirtyFlags.TYPE_AND_FACE,
            UnitDirtyFlags.POSITION_AND_FACING,
            UnitDirtyFlags.MP,
            UnitDirtyFlags.HP
    );
    // Native global ticksCount_from_0050164f accumulated by CServerApp::SendGameAction @0050164F.
    private static int itemListActionSendElapsedTicks;
    private static final int NON_HUMANOID_HIGH_SERVER_ID_MASK = UnitDirtyFlags.toValue(
            UnitDirtyFlags.DISPLAY_NAME,
            UnitDirtyFlags.SERVER_ID,
            UnitDirtyFlags.OWNER_PLAYER_ID,
            UnitDirtyFlags.POSITION_AND_FACING,
            UnitDirtyFlags.MAX_MP,
            UnitDirtyFlags.HP
    );
    private static final int NON_OWNER_HUMANOID_MASK = UnitDirtyFlags.toValue(
            UnitDirtyFlags.DISPLAY_NAME,
            UnitDirtyFlags.VISIBILITY_STATE,
            UnitDirtyFlags.EFFECTS,
            UnitDirtyFlags.EQUIPPED_ITEMS,
            UnitDirtyFlags.SERVER_ID,
            UnitDirtyFlags.OWNER_PLAYER_ID,
            UnitDirtyFlags.TYPE_AND_FACE,
            UnitDirtyFlags.POSITION_AND_FACING,
            UnitDirtyFlags.MAX_MP,
            UnitDirtyFlags.HP
    );
    private static final Endpoint LOCAL_ENDPOINT = constructEndpoint(true);
    private static final Endpoint REMOTE_ENDPOINT = constructEndpoint(false);

    // not ported.
    private CServerApp() {
    }

    /**
     * Native: CServerApp::CServerApp @0050077A.
     * Fully ported.
     */
    private static Endpoint constructEndpoint(boolean local) {
        return new Endpoint(local);
    }

    /**
     * Native: CServerApp::SetNetworkDriver @00501008 on g_CServerApp_local.
     * Fully ported for the local singleton call path.
     */
    public static void setLocalNetworkDriver(Class<?> driver) {
        LOCAL_ENDPOINT.networkDriver = driver;
    }

    /**
     * Native: CServerApp::SetNetworkDriver @00501008 on g_CServerApp_remote.
     * Fully ported for the remote singleton call path.
     */
    public static void setRemoteNetworkDriver(Class<?> driver) {
        REMOTE_ENDPOINT.networkDriver = driver;
    }

    /**
     * Native support extracted from CLlDriver::SetServerApp @00493A60 local g_CServerApp binding.
     */
    public static Object localEndpointHandle() {
        return LOCAL_ENDPOINT;
    }

    /**
     * Native support extracted from CLlDriver::SetServerApp @00493A60 remote g_CServerApp binding.
     */
    public static Object remoteEndpointHandle() {
        return REMOTE_ENDPOINT;
    }

    /**
     * Native support extracted from Unit::Update @0050F12C; calls CServerApp::GetClientByNetId @0050155F
     * and CServerApp::RemoveClient @00500AD9 on g_CServerApp_local.
     */
    public static void removeLocalClientByNetId(int playerId) {
        CBufferManager client = getLocalClientByNetId(playerId);
        if (client != null) {
            removeClient(LOCAL_ENDPOINT, client);
        }
    }

    /**
     * Native support extracted from CMainWindow::kickSelectedPlayerCommand @00492A3A.
     */
    public static void removeLocalClient(CBufferManager client) {
        removeClient(LOCAL_ENDPOINT, client);
    }

    /**
     * Native support extracted from Unit::Update @0050F12C; calls CServerApp::ProcessNetworkEvents @0050101E
     * on g_CServerApp_local.
     */
    public static void processLocalNetworkEvents() {
        processNetworkEvents(LOCAL_ENDPOINT);
    }

    /**
     * Native: CServerApp::HasActiveConnection @00501970 on g_CServerApp_remote.
     * Fully ported.
     */
    public static boolean hasActiveRemoteConnection() {
        return hasActiveConnection(REMOTE_ENDPOINT);
    }

    /**
     * Native: CServerApp::HasActiveConnection @00501970 on g_CServerApp_local.
     * Fully ported.
     */
    public static boolean hasActiveLocalConnection() {
        return hasActiveConnection(LOCAL_ENDPOINT);
    }

    /**
     * Native: CServerApp::HasActiveConnection @00501970.
     * Fully ported.
     */
    private static boolean hasActiveConnection(Endpoint endpoint) {
        if (endpoint.networkDriver == null) {
            return endpoint.pairedServerApp != null;
        }
        return CLlDriver.getStatus() || endpoint.pairedServerApp != null;
    }

    /**
     * Native: CServerApp::RemoveClient @00500AD9.
     * Fully ported.
     */
    private static void removeClient(Endpoint endpoint, CBufferManager client) {
        if (client == endpoint.pairedClient) {
            queueClientRemoval(endpoint, endpoint.pairedClient);
            endpoint.pairedClient = null;
            return;
        }

        CLlDriver.disconnectClientBoundary(client.GetIPAddress());
    }

    /**
     * Native support extracted from GameServer::rejectClientJoin @004F0B71.
     */
    public static void rejectClientJoin(CBufferManager client, int statusCode, String message) {
        flushActiveClientWriteBuffers();
        Player player = new Player();
        player.playerId = 9999;
        client.SetNetId(9999);
        if (!message.isEmpty()) {
            sendServerChatText(message, player);
        }
        sendTwoDwordAction(player, GameActionId.TWO_DWORD_ACTION_0B, statusCode, 0);
        sendCurrentServerLoopCounter(player);
        flushActiveClientWriteBuffers();
        player.playerId = 0;
        if (CLlDriver.getProtocolId() == ProtocolId.TCP_IP) {
            removeClient(LOCAL_ENDPOINT, client);
        }
    }

    /**
     * Native: CServerApp::QueueClientRemoval @00500FD2.
     * Fully ported.
     */
    private static void queueClientRemoval(Endpoint endpoint, CBufferManager client) {
        endpoint.queueClientRemoval(client);
    }

    /**
     * Native: CServerApp::ProcessNetworkEvents @0050101E.
     * Fully ported.
     */
    private static void processNetworkEvents(Endpoint endpoint) {
        processQueuedClientActivations(endpoint);
        processQueuedClientRemovals(endpoint);
        if (endpoint.networkDriver != null) {
            endpoint.pumpDirectPlaySendsBoundary();
        }
    }

    /**
     * Native: CServerApp::ProcessQueuedClientActivations @00500CC0.
     * Fully ported.
     */
    private static void processQueuedClientActivations(Endpoint endpoint) {
        synchronized (endpoint.clientListsLock) {
            for (CBufferManager client : endpoint.queuedClientActivations) {
                if (client != null) {
                    int compressionStreamId = endpoint.packetCompressionMode == 0
                            ? 0
                            : endpoint.defaultCompressionStreamId;
                    client.SetCompressionStreamId(compressionStreamId);
                    endpoint.activeClients.add(client);
                    onClientActivated(endpoint, client);
                }
            }
            endpoint.queuedClientActivations.clear();
        }
    }

    /**
     * Native: CServerApp::ProcessQueuedClientRemovals @00500BD0.
     * Fully ported.
     */
    private static void processQueuedClientRemovals(Endpoint endpoint) {
        synchronized (endpoint.clientListsLock) {
            for (CBufferManager client : endpoint.queuedClientRemovals) {
                int activeClientIndex = endpoint.activeClients.indexOf(client);
                if (activeClientIndex >= 0) {
                    onClientRemoved(endpoint, client);
                    endpoint.activeClients.remove(activeClientIndex);
                }
                if (client != null) {
                    CLlDriver.unregisterClientLatencyBoundary(client.GetIPAddress());
                    client.releasePendingReadBuffers();
                }
            }
            endpoint.queuedClientRemovals.clear();
        }
    }

    /**
     * Native: CServerApp::NewClient @00500F0E.
     * Fully ported.
     */
    private static CBufferManager newClient(Endpoint endpoint, int ipAddress) {
        CBufferManager client = new CBufferManager();
        client.SetIPAddress(ipAddress);
        client.SetServerAndDriver(endpoint, endpoint.networkDriver);
        CLlDriver.registerClientLatencyBoundary(ipAddress);
        endpoint.queueClientActivation(client);
        return client;
    }

    /**
     * Native support extracted from CServerApp::NewClient @00500F0E for
     * CLlDriver::ConnectTcp @00509218 and CLlDriver::AcceptThreadTcp @00508AB3.
     */
    public static CBufferManager newNetworkClient(Object serverApp, int ipAddress) {
        return newClient(endpointFromHandle(serverApp, "CServerApp::NewClient @00500F0E"), ipAddress);
    }

    /**
     * Native support extracted from CServerApp::QueueClientRemoval @00500FD2 for
     * CLlDriver::CloseClientConnection @0050957A.
     */
    public static void queueNetworkClientRemoval(Object serverApp, CBufferManager client) {
        queueClientRemoval(endpointFromHandle(serverApp, "CServerApp::QueueClientRemoval @00500FD2"), client);
    }

    /**
     * Native: CServerApp::SetPeerApp @00500A72.
     * Fully ported.
     */
    private static void setPeerApp(Endpoint endpoint, Endpoint peerApp) {
        endpoint.pairedServerApp = peerApp;
        if (endpoint.pairedClient != null) {
            removeClient(endpoint, endpoint.pairedClient);
        }
        endpoint.pairedClient = newClient(endpoint, DIRECT_DELIVERY_IP_ADDRESS);
        endpoint.pairedClient.SetDirectDelivery(true);
        endpoint.pairedClient.SetCompressionStreamId(0);
    }

    /**
     * Native: CServerApp::GetBufferManager @0053F270.
     * Fully ported.
     */
    public static CBufferManager getBufferManager(Object serverApp) {
        return getBufferManager(endpointFromHandle(serverApp, "CServerApp::GetBufferManager @0053F270"));
    }

    /**
     * Native support extracted from CServerApp::GetBufferManager @0053F270.
     */
    private static CBufferManager getBufferManager(Endpoint endpoint) {
        if (endpoint.pairedServerApp == null) {
            return null;
        }
        return endpoint.pairedServerApp.pairedClient;
    }

    /**
     * vtbl +0x00: CServerApp::onClientActivated @00505F07.
     * Fully ported for native protocol login-name initialization; Java also maps the visible raw TCP/IP replacement
     * server to the native DirectPlay non-null login sentinel so its no-password session-browser join can progress.
     */
    private static void onClientActivated(Endpoint endpoint, CBufferManager client) {
        ClientTrafficStats trafficStats = new ClientTrafficStats();
        endpoint.clientTrafficStatsByIp.put(client.GetIPAddress(), trafficStats);
        if (isTcpIpEndpoint(endpoint) && !CLlDriver.isVisibleRawTcpSessionServer()) {
            client.SetLoginName(null);
        } else {
            client.setLoginNameUnavailableSentinel();
        }
    }

    /**
     * vtbl +0x04: CServerApp::onClientRemoved @00505D02.
     * Fully ported. Java relies on garbage collection for native ClientTrafficStats/CString deletion; the legacy
     * DirectPlay modem restart branch delegates to an explicit CLlDriver boundary.
     */
    private static void onClientRemoved(Endpoint endpoint, CBufferManager client) {
        restartModemServerIfNeeded(endpoint);
        if (Globals.gameServer != null) {
            Player player = Globals.gameServer.playerList.getPlayerById(client.GetNetId());
            if (player != null && player.pendingRemovalServerTick == 0) {
                player.markDisconnectedForRemoval();
                if (isTcpIpEndpoint(endpoint)) {
                    player.pendingRemovalServerTick = Globals.gameServer.someValue + 300;
                }
                if (Globals.worldMap == null && Globals.gameServer.networkSessionActive != 0) {
                    sendTwoDwordAction(null, GameActionId.DELETE_PLAYER_ACTION_97, (short) player.playerId, 0);
                    Globals.gameServer.pushMessage("Player " + player.name + " disconnected.");
                    Globals.gameServer.playerList.destroy(player);
                    Globals.gameServer.reportServerStatusToConfiguredTargets();
                }
            }
        }
        endpoint.clientTrafficStatsByIp.remove(client.GetIPAddress());
        client.releaseOwnedLoginName();
    }

    /**
     * Native support extracted from CServerApp::onClientRemoved @00505D02.
     * Fully ported.
     */
    private static void restartModemServerIfNeeded(Endpoint endpoint) {
        if (endpoint.networkDriver != null
                && CLlDriver.isServerMode()
                && CLlDriver.getProtocolId() == ProtocolId.DPSP_MODEM) {
            CLlDriver.restartModemServerBoundary();
        }
    }

    /**
     * Native support extracted from CServerApp::onClientActivated @00505F07 and
     * CServerApp::onClientRemoved @00505D02.
     * Fully ported.
     */
    private static boolean isTcpIpEndpoint(Endpoint endpoint) {
        return endpoint.networkDriver != null && CLlDriver.getProtocolId() == ProtocolId.TCP_IP;
    }

    /**
     * Native: CServerApp::sampleClientTrafficStats @00505F83.
     * Fully ported.
     */
    private static void sampleClientTrafficStats(Endpoint endpoint) {
        for (ClientTrafficStats trafficStats : endpoint.clientTrafficStatsByIp.values()) {
            trafficStats.sampleCount++;
            trafficStats.totalBytes += trafficStats.currentIntervalBytes;
            if (trafficStats.peakIntervalBytes < trafficStats.currentIntervalBytes) {
                trafficStats.peakIntervalBytes = trafficStats.currentIntervalBytes;
            }
            trafficStats.lastIntervalBytes = trafficStats.currentIntervalBytes;
            trafficStats.currentIntervalBytes = 0;
        }
    }

    /**
     * Native: CServerApp::sampleClientTrafficStats @00505F83 on g_CServerApp_local.
     * Fully ported.
     */
    public static void sampleLocalClientTrafficStats() {
        sampleClientTrafficStats(LOCAL_ENDPOINT);
    }

    /**
     * Native: CServerApp::sampleClientTrafficStats @00505F83 on g_CServerApp_remote.
     * Fully ported.
     */
    public static void sampleRemoteClientTrafficStats() {
        sampleClientTrafficStats(REMOTE_ENDPOINT);
    }

    /**
     * Native: CServerApp::recordClientActionBytesByIp @00506030.
     * Fully ported.
     */
    private static void recordClientActionBytesByIp(Endpoint endpoint, CBufferManager client, int actionSizeBytes) {
        ClientTrafficStats trafficStats = endpoint.clientTrafficStatsByIp.get(client.GetIPAddress());
        if (trafficStats != null) {
            trafficStats.currentIntervalBytes += actionSizeBytes;
        }
    }

    /**
     * Native: CServerApp::recordPacketStats @00506071.
     * Fully ported.
     */
    private static void recordPacketStats(Endpoint endpoint, CGameAction action) {
        int actionId = action.ID.get();
        endpoint.packetCountByActionId[actionId]++;
        endpoint.packetBytesByActionId[actionId] += action.GetPayloadSize();
    }

    /**
     * Native support extracted from CServerApp::ConfigurePacketCompression @0050104D.
     * Fully ported.
     */
    private static void configurePacketCompression(
            Endpoint endpoint,
            int compressionStreamId,
            int compressionMode
    ) throws IOException {
        endpoint.defaultCompressionStreamId = compressionStreamId;
        endpoint.packetCompressionMode = compressionMode;
        if (endpoint.packetCompressionMode == 1) {
            endpoint.packer1.clearFrequencies();
            endpoint.packer2.clearFrequencies();
        }
        if (endpoint.packetCompressionMode == 2) {
            readPackerFrequencies(PACKER1_TABLE_PATH, endpoint.packer1);
            readPackerFrequencies(PACKER2_TABLE_PATH, endpoint.packer2);
        }
    }

    /**
     * Native support extracted from CServerApp::SavePacketCompressionTables @005011AA.
     * Fully ported.
     */
    private static boolean savePacketCompressionTables(Endpoint endpoint) throws IOException {
        boolean saveTables = endpoint.packetCompressionMode == 1;
        if (saveTables) {
            writePackerFrequencies(PACKER1_TABLE_PATH, endpoint.packer1);
            writePackerFrequencies(PACKER2_TABLE_PATH, endpoint.packer2);
        }
        return saveTables;
    }

    /**
     * Native support extracted from CServerApp::GetPacketCompressionRatioPercent @005012C9.
     * Fully ported.
     */
    private static int getPacketCompressionRatioPercent(Endpoint endpoint, int compressionStreamId) {
        if (compressionStreamId == 1) {
            return endpoint.stream1SourceBytes == 0
                    ? 100
                    : (endpoint.stream1PackedBytes * 100) / endpoint.stream1SourceBytes;
        }
        if (compressionStreamId == 2) {
            return endpoint.stream2SourceBytes == 0
                    ? 100
                    : (endpoint.stream2PackedBytes * 100) / endpoint.stream2SourceBytes;
        }
        return 100;
    }

    /**
     * Native support extracted from CServerApp::ConfigurePacketCompression @0050104D CFile table reads.
     */
    private static void readPackerFrequencies(String tablePath, ByteHuffmanPacker packer) throws IOException {
        ByteBuffer table = ByteBuffer.wrap(Files.readAllBytes(Path.of(tablePath))).order(ByteOrder.LITTLE_ENDIAN);
        packer.readFrequencies(new ByteBufferAdaptor(table));
    }

    /**
     * Native support extracted from CServerApp::SavePacketCompressionTables @005011AA CFile table writes.
     */
    private static void writePackerFrequencies(String tablePath, ByteHuffmanPacker packer) throws IOException {
        ByteBuffer table = ByteBuffer.allocate(PACKER_FREQUENCY_TABLE_BYTES).order(ByteOrder.LITTLE_ENDIAN);
        for (int frequency : packer.freqBySymbol) {
            table.putInt(frequency);
        }
        Files.write(
                Path.of(tablePath),
                table.array(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );
    }

    /**
     * Native support extracted from CServerApp::PackNetBufferPayload @00501347.
     * Fully ported.
     */
    private static int packNetBufferPayload(
            Endpoint endpoint,
            int compressionStreamId,
            byte[] source,
            int sourceSize,
            int[] packedTarget
    ) {
        if (endpoint.packetCompressionMode == 1) {
            ByteHuffmanPacker packer = endpoint.getCompressionPacker(compressionStreamId);
            if (packer != null) {
                packer.countFrequencies(source, sourceSize);
            }
            copyBytesToWords(source, sourceSize, packedTarget);
            return sourceSize << 3;
        }
        if (compressionStreamId == 1) {
            endpoint.stream1SourceBytes += sourceSize;
            int bitCount = endpoint.packer1.pack(source, sourceSize, packedTarget);
            endpoint.stream1PackedBytes += ByteHuffmanPacker.packedByteLength(bitCount);
            return bitCount;
        }
        if (compressionStreamId == 2) {
            endpoint.stream2SourceBytes += sourceSize;
            int bitCount = endpoint.packer2.pack(source, sourceSize, packedTarget);
            endpoint.stream2PackedBytes += ByteHuffmanPacker.packedByteLength(bitCount);
            return bitCount;
        }
        return 0;
    }

    /**
     * Native support extracted from CBufferManager::FlushWriteBuffer @0050036A; calls
     * CServerApp::PackNetBufferPayload @00501347.
     * Fully ported support wrapper for endpoint-backed buffer managers.
     */
    public static int packNetBufferPayload(
            Object serverApp,
            int compressionStreamId,
            byte[] source,
            int sourceSize,
            int[] packedTarget
    ) {
        return packNetBufferPayload(
                endpointFromHandle(serverApp, "CServerApp::PackNetBufferPayload @00501347"),
                compressionStreamId,
                source,
                sourceSize,
                packedTarget
        );
    }

    /**
     * Native support extracted from CServerApp::UnpackNetBufferPayload @0050148A.
     * Fully ported.
     */
    private static int unpackNetBufferPayload(
            Endpoint endpoint,
            int compressionStreamId,
            int[] packedSource,
            int bitCount,
            byte[] target,
            int targetCapacity
    ) {
        if (endpoint.packetCompressionMode == 1) {
            int count = bitCount >> 3;
            copyWordsToBytes(packedSource, target, count);
            ByteHuffmanPacker packer = endpoint.getCompressionPacker(compressionStreamId);
            if (packer != null) {
                packer.countFrequencies(target, count);
            }
            return count;
        }
        if (compressionStreamId == 1) {
            return endpoint.packer1.unpack(packedSource, bitCount, target, targetCapacity);
        }
        if (compressionStreamId == 2) {
            return endpoint.packer2.unpack(packedSource, bitCount, target, targetCapacity);
        }
        return 0;
    }

    /**
     * Native support extracted from CBufferManager::ReceiveData @0050023B; calls
     * CServerApp::UnpackNetBufferPayload @0050148A.
     * Fully ported support wrapper for endpoint-backed buffer managers.
     */
    public static int unpackNetBufferPayload(
            Object serverApp,
            int compressionStreamId,
            int[] packedSource,
            int bitCount,
            byte[] target,
            int targetCapacity
    ) {
        return unpackNetBufferPayload(
                endpointFromHandle(serverApp, "CServerApp::UnpackNetBufferPayload @0050148A"),
                compressionStreamId,
                packedSource,
                bitCount,
                target,
                targetCapacity
        );
    }

    /**
     * Native: CServerApp::AcquireNetBuffer @00500D86.
     * Fully ported.
     */
    public static NetBuffer acquireNetBuffer(Object serverApp) {
        Endpoint endpoint = endpointFromHandle(serverApp, "CServerApp::AcquireNetBuffer @00500D86");
        Endpoint poolOwner = endpoint.netBufferPoolOwner();
        NetBuffer buffer = poolOwner.netBufferPool.pollFirst();
        return buffer != null ? buffer : new NetBuffer();
    }

    /**
     * Native: CServerApp::ReleaseNetBuffer @00500EAA.
     * Fully ported.
     */
    public static void releaseNetBuffer(Object serverApp, NetBuffer buffer) {
        Endpoint endpoint = endpointFromHandle(serverApp, "CServerApp::ReleaseNetBuffer @00500EAA");
        endpoint.netBufferPoolOwner().netBufferPool.addFirst(buffer);
    }

    /**
     * Native support extracted from CServerApp::GetBufferManager @0053F270,
     * CServerApp::PackNetBufferPayload @00501347, CServerApp::UnpackNetBufferPayload @0050148A,
     * CServerApp::AcquireNetBuffer @00500D86, and CServerApp::ReleaseNetBuffer @00500EAA.
     */
    private static Endpoint endpointFromHandle(Object serverApp, String nativeContext) {
        if (serverApp instanceof Endpoint endpoint) {
            return endpoint;
        }
        throw new IllegalStateException(nativeContext + " requires a CServerApp endpoint handle");
    }

    /**
     * Native support extracted from CServerApp::PackNetBufferPayload @00501347 training-mode memcpy into uint*.
     */
    private static void copyBytesToWords(byte[] source, int sourceSize, int[] targetWords) {
        int wordCount = (sourceSize + 3) >>> 2;
        for (int i = 0; i < wordCount; i++) {
            targetWords[i] = 0;
        }
        for (int i = 0; i < sourceSize; i++) {
            int wordIndex = i >>> 2;
            int shift = (i & 3) << 3;
            targetWords[wordIndex] |= Byte.toUnsignedInt(source[i]) << shift;
        }
    }

    /**
     * Native support extracted from CServerApp::UnpackNetBufferPayload @0050148A training-mode memcpy from uint*.
     */
    private static void copyWordsToBytes(int[] sourceWords, byte[] target, int count) {
        for (int i = 0; i < count; i++) {
            int wordIndex = i >>> 2;
            int shift = (i & 3) << 3;
            target[i] = (byte) (sourceWords[wordIndex] >>> shift);
        }
    }

    /**
     * Native: CServerApp::writePacketStatsLog @005060DA.
     * Fully ported.
     */
    private static void writePacketStatsLog(Endpoint endpoint, String name) throws IOException {
        StringBuilder log = new StringBuilder();
        log.append("Cmd|Packets|Avg Size| Total Size |Comment\n");
        log.append("-------------------------------------------\n");
        for (int actionId = 0; actionId < 0x100; actionId++) {
            int packetCount = endpoint.packetCountByActionId[actionId];
            if (packetCount != 0) {
                int packetBytes = endpoint.packetBytesByActionId[actionId];
                log.append(String.format(
                        Locale.ROOT,
                        "%3d|%7d|%8d|%11d|%s\n",
                        actionId,
                        packetCount,
                        packetBytes / packetCount,
                        packetBytes,
                        packetStatsLabel(actionId)
                ));
            }
        }
        Files.writeString(
                Path.of(name),
                log,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );
    }

    /**
     * Native: CServerApp::writePacketStatsLog @005060DA on g_CServerApp_local.
     * Fully ported.
     */
    public static void writeLocalPacketStatsLog(String name) throws IOException {
        writePacketStatsLog(LOCAL_ENDPOINT, name);
    }

    /**
     * Native: CServerApp::writePacketStatsLog @005060DA on g_CServerApp_remote.
     * Fully ported.
     */
    public static void writeRemotePacketStatsLog(String name) throws IOException {
        writePacketStatsLog(REMOTE_ENDPOINT, name);
    }

    /**
     * Native support extracted from CServerApp::writePacketStatsLog @005060DA packet-label switch.
     * Fully ported.
     */
    private static String packetStatsLabel(int actionId) {
        GameActionId id = GameActionId.fromId(actionId);
        if (id == null) {
            return "";
        }
        return switch (id) {
            case NEW_SEGMENT_ACTION_64 -> "GM_ReportNewSegment";
            case MONEY_ACTION_67 -> "GM_ReportMoney";
            case UNIT_SHOWN_ACTION_69 -> "GM_ReportUnitShown";
            case UNIT_MOVE_ACTION_6B -> "GM_ReportUnitMove";
            case UNIT_CHANGE_ACTION_6C, UNIT_CHANGE_ACTION_6E, UNIT_CHANGE_ACTION_6F, UNIT_CHANGE_ACTION_70 ->
                    "GM_ReportUnitChange";
            case UNIT_TURN_ACTION_6D -> "GM_ReportUnitTurn";
            case UNIT_ATTACK_ACTION_71 -> "GM_ReportUnitAttack";
            case RANGED_ATTACK_ACTION_72 -> "GM_ReportRangedAttack";
            case UNIT_DAMAGED_ACTION_73 -> "GM_ReportUnitDamaged";
            case UNIT_HIDDEN_ACTION_74 -> "GM_ReportUnitHidden";
            case ITEM_LIST_ACTION_76 -> "GM_ReportItemList";
            case UNIT_WEAR_ITEMS_ACTION_79 -> "GM_ReportUnitWearItems";
            case SACK_ACTION_7A -> "GM_ReportSack";
            case EFFECT_ACTION_86 -> "GM_ReportEffect";
            case AREA_EFFECT_ACTION_87 -> "GM_ReportAreaEffect";
            case EFFECT_HANG_ACTION_88 -> "GM_ReportEffectHang";
            case EFFECT_GONE_ACTION_89 -> "GM_ReportEffectGone";
            case EFFECT_MULTI_TARGET_ACTION_8A -> "GM_ReportEffectMultiTarget";
            case EFFECT_FROM_ACTION_8B -> "GM_ReportEffectFrom";
            case EFFECT_MULTI_FROM_ACTION_8C -> "GM_ReportEffectMultiFrom";
            case MESSAGE_ACTION_8D -> "GM_ReportMessage";
            case NEW_PLAYER_ACTION_96 -> "GM_ReportNewPlayer";
            case DELETE_PLAYER_ACTION_97 -> "GM_ReportDeletePlayer";
            case ASH_MAP_ACTION_98 -> "GM_ReportAshMap";
            case ENEMY_EQUIPMENT_ACTION_9C -> "GM_ReportEnemyEquipment";
            case DIPLOMACY_ACTION_B9 -> "GM_ReportDiplomacy";
            case CREATURE_KNOWLEDGE_ACTION_BA -> "GM_ReportCreatureKnowledge";
            case PLAYER_QUESTS_ACTION_BB -> "GM_ReportPlayerQuests";
            case INN_QUESTS_ACTION_BC -> "GM_ReportInnQuests";
            default -> "";
        };
    }

    /**
     * Native: CServerApp::notifyStateChanged @00503672.
     * Fully ported.
     */
    public static void notifyStateChanged(Building building) {
        for (Player player : Globals.gameServer.playerList.players) {
            if (player.isMapLoadPending() || player == building.owner) {
                notifyStateChanged(building, player);
            }
        }
    }

    /**
     * Native: CServerApp::notifyStateChanged @00503672.
     * Fully ported.
     */
    public static void notifyStateChanged(Sack sack) {
        for (Player player : Globals.gameServer.playerList.players) {
            if (player.isMapLoadPending() || player == sack.owner || shouldSendOwnerlessLocalSackVisual(player, sack)) {
                notifyStateChanged(sack, player);
            }
        }
    }

    /**
     * Java support for ownerless post-load sacks in the copied map-visual model.
     * Native CServerApp::notifyStateChanged @00503672 can rely on the local native map observing server objects;
     * Java's loaded local client needs SACK_ACTION_7A to create or refresh CBackPack visuals after map load.
     * not ported.
     */
    private static boolean shouldSendOwnerlessLocalSackVisual(Player player, Sack sack) {
        return Globals.gameServer.networkSessionActive == 0
                && sack.owner == null
                && shouldReceiveLiveMapVisualAction(player);
    }

    /**
     * Native support extracted from CServerApp::notifyStateChanged @00503672 building branch.
     * Fully ported.
     */
    private static void notifyStateChanged(Building building, Player player) {
        if (building.isBuildingToken() == 0) {
            return;
        }
        building.word |= player.scanMask;
        if (building.getTokenTypeId() == 0 || building.healthMax == 0) {
            return;
        }

        sendGameAction(BuildingHealthAction.createForBuildingStateChanged(building, player));
    }

    /**
     * Native support extracted from CServerApp::notifyStateChanged @00503672 sack branch.
     * Fully ported.
     */
    public static void notifyStateChanged(Sack sack, Player player) {
        SackAction action = SackAction.createForBuildingStateChanged(sack, player);
        if (Globals.gameServer.networkSessionActive != 0 && !canPlayerSeeSpellEffectTarget(sack.m_pTargetHandle, player)) {
            return;
        }
        sack.word |= player.scanMask;
        sack.visibilityRefreshMask &= ~player.scanMask;
        sendGameAction(action);
    }

    /**
     * Native: CServerApp::notifyStateChanged @00503672 unit dispatch.
     * Fully ported.
     */
    public static void notifyStateChanged(Unit unit) {
        for (Player player : Globals.gameServer.playerList.players) {
            if (shouldReceiveLiveUnitVisualAction(player, unit)) {
                notifyStateChanged(unit, player, '\0');
            }
        }
    }

    /**
     * Native: CServerApp::notifyUnitHitPointsChanged @00504B1D.
     * Fully ported.
     */
    public static void notifyUnitHitPointsChanged(Unit unit) {
        for (Player player : Globals.gameServer.playerList.players) {
            if (shouldReceiveLiveUnitVisualAction(player, unit)) {
                notifyStateChanged(unit, player, 's');
            }
        }
    }

    /**
     * Native: CServerApp::notifyUnitHitPointsChanged @00504B1D.
     * Building Token* branch reaches CServerApp::notifyStateChanged @00503672, which emits BuildingHealthAction.
     * Fully ported.
     */
    public static void notifyUnitHitPointsChanged(Building building) {
        notifyStateChanged(building);
    }

    /**
     * Native support extracted from CServerApp::notifyUnitHitPointsChanged @00504B1D and
     * CServerApp::notifyStateChanged @00503672 unit branch.
     * Fully ported.
     */
    private static void notifyStateChanged(Unit unit, Player player, char eventCode) {
        if (eventCode != 's') {
            return;
        }
        if ((unit.visiblePlayerMask & player.scanMask) == 0
                && Globals.gameServer.networkSessionActive != 0) {
            deferNetUpdateForSpecialPlayer(unit, player, UnitDirtyFlags.HP.value);
            return;
        }
        sendGameAction(UnitDamagedAction.createForUnitHitPointsChanged(unit, player));
    }

    /**
     * Native: CServerApp::notifySackRemoved @00504B3A.
     * Fully ported.
     */
    public static void notifySackRemoved(Sack sack) {
        for (Player player : Globals.gameServer.playerList.players) {
            if (sack.hasInitialSyncSentToPlayer(player)) {
                sendGameAction(SackRemovedAction.createForSackRemoved(sack, player));
            }
        }
    }

    /**
     * Native: CServerApp::broadcastServerLoopCounter @00504BB7.
     * Fully ported.
     */
    public static void broadcastServerLoopCounter(int serverLoopCounter) {
        LOCAL_ENDPOINT.broadcastServerLoopCounter(serverLoopCounter);
    }

    /**
     * Native: CServerApp::broadcastServerLoopCounter @00504BB7 on g_CServerApp_remote.
     */
    public static void broadcastRemoteServerLoopCounter(int serverLoopCounter) {
        REMOTE_ENDPOINT.broadcastServerLoopCounter(serverLoopCounter);
    }

    /**
     * Native: CServerApp::sendCurrentServerLoopCounter @00504BFA.
     * Fully ported.
     */
    public static void sendCurrentServerLoopCounter(Player player) {
        int playerId = 0;
        if (player == null) {
            CBufferManager client = LOCAL_ENDPOINT.GetClientByNetId(0);
            if (client != null) {
                playerId = client.GetNetId();
            }
        } else {
            playerId = player.playerId;
        }
        sendGameAction(NewSegmentAction.createForServerLoopCounter(playerId, Globals.gameServer.serverLoopCounter));
    }

    /**
     * Native: CServerApp::sendPlayerStateResync @005049F5.
     * Fully ported.
     */
    public static void sendPlayerStateResync(Player player) {
        if (player.controlledUnit != null) {
            netUpdate((Unit) player.controlledUnit, player, ALL_UNIT_UPDATE_FLAGS, EQUIPMENT_BROADCAST_MASK, 0, 0);
        }
        if (player.ownedUnits != null) {
            for (Unit unit : player.ownedUnits) {
                unit.word &= ~player.scanMask;
                netUpdate(unit, player, ALL_UNIT_UPDATE_FLAGS, EQUIPMENT_BROADCAST_MASK, 0, 0);
            }
        }
        sendCurrentServerLoopCounter(player);
    }

    /**
     * Native: CServerApp::sendLatencySetting @00504C76.
     * Fully ported.
     */
    public static void sendLatencySetting(int latencyMillis, Player player) {
        sendGameAction(LatencySettingAction.createForLatencySetting(latencyMillis, player));
    }

    /**
     * Native: CServerApp::sendLoginAcceptedHandshake @00504CCC.
     * Fully ported.
     */
    public static void sendLoginAcceptedHandshake(CBufferManager targetClient) {
        targetClient.WriteGameAction(FixedDwordAction0E.createForLoginAcceptedHandshake());
        targetClient.WriteGameAction(NewSegmentAction.createForServerLoopCounter(0, Globals.gameServer.serverLoopCounter));
        targetClient.IncrementCurrentWriteBufferSegmentMarker();
        flushActiveClientWriteBuffers();
    }

    /**
     * Native: CServerApp::sendLobbyPlayerInfoSnapshot @00504D39.
     * Fully ported.
     */
    public static void sendLobbyPlayerInfoSnapshot(Player player) {
        for (Player sourcePlayer : Globals.gameServer.playerList.players) {
            if (sourcePlayer == player) {
                continue;
            }
            sendGameAction(NewPlayerAction.prepareForLobbyPlayerInfoSnapshot(player, sourcePlayer));
            if (sourcePlayer.controlledUnit instanceof Unit controlledUnit
                    && controlledUnit.hasSavedCharacterKillHistoryMarker()) {
                sendGameEventNotification(CHEAT_NOTIFICATION_EVENT, (short) sourcePlayer.playerId, player);
            }
        }
    }

    /**
     * Native support extracted from global sendInitialUnitAndBuildingSnapshotsForPlayer @0050480D.
     * Fully ported support helper.
     */
    public static void sendInitialUnitAndBuildingSnapshotsForPlayer(Player player) {
        for (Unit unit : Globals.gameServer.activeUnits) {
            if (unit != null) {
                netUpdate(unit, player, initialUnitSnapshotUpdateFlags(unit), EQUIPMENT_BROADCAST_MASK, 0, 0);
            }
        }
        for (Unit unit : Globals.gameServer.objectLists.corpses) {
            if (unit != null && unit.respawning < 5) {
                netUpdate(unit, player, initialCorpseSnapshotUpdateFlags(unit), EQUIPMENT_BROADCAST_MASK, 0, 0);
            }
        }
        for (Building building : Globals.gameServer.objectLists.buildings) {
            if (building != null && building.healthCurrent != building.healthMax) {
                notifyStateChanged(building, player);
            }
        }
    }

    /**
     * Native: CServerApp::sendInitialSackSnapshotsForPlayer @00504E34.
     * Fully ported.
     */
    public static void sendInitialSackSnapshotsForPlayer(Player player) {
        for (Sack sack : Globals.gameServer.objectLists.sacks) {
            if (sack != null) {
                notifyStateChanged(sack, player);
            }
        }
    }

    /**
     * Native: CServerApp::sendInitialTokenStateToMapLoadingPlayers @00504007.
     * Fully ported.
     */
    public static void sendInitialTokenStateToMapLoadingPlayers(Token token) {
        Player player = Globals.gameServer.playerList.getFirst();
        while (player != null) {
            sendInitialTokenStateToPlayer(token, player);
            player = Globals.gameServer.playerList.getNext(player);
        }
    }

    /**
     * Native: CServerApp::sendInitialTokenStateToPlayer @00504052.
     * Fully ported.
     */
    private static void sendInitialTokenStateToPlayer(Token token, Player player) {
        if (!player.isMapLoadPending()) {
            return;
        }
        if (!token.needsInitialSyncForPlayer(player)) {
            return;
        }
        token.m_pTargetHandle.isSubPosUnknown();
        if (token.isUnitToken() == 0) {
            sendInitialNonUnitTokenStateToPlayer(token, player);
            return;
        }
        netUpdate((Unit) token, player, ALL_UNIT_UPDATE_FLAGS, EQUIPMENT_BROADCAST_MASK, 0, 0);
    }

    /**
     * Native support extracted from CServerApp::sendInitialTokenStateToPlayer @00504052 and
     * CServerApp::notifyStateChanged @00503672.
     * Fully ported.
     */
    private static void sendInitialNonUnitTokenStateToPlayer(Token token, Player player) {
        if (token instanceof Sack sack) {
            notifyStateChanged(sack, player);
        } else if (token instanceof Building building) {
            notifyStateChanged(building, player);
        }
    }

    /**
     * Native support extracted from ::sendInitialUnitAndBuildingSnapshotsForPlayer @0050480D.
     */
    private static int initialUnitSnapshotUpdateFlags(Unit unit) {
        int updateFlags = unit.idFull <= 0x5FFF
                ? INITIAL_LOW_ID_UNIT_AND_CORPSE_UPDATE_FLAGS
                : INITIAL_HIGH_ID_UNIT_UPDATE_FLAGS;
        if (unit.ownershipTransferRefreshFlag != 0) {
            updateFlags |= UnitDirtyFlags.OWNER_PLAYER_ID.value;
        }
        if (Globals.gameServer.networkSessionActive == 0
                && unit.serverID >= LOCAL_CAMPAIGN_INVENTORY_SERVER_ID_FIRST
                && unit.serverID < LOCAL_CAMPAIGN_INVENTORY_SERVER_ID_LIMIT) {
            updateFlags |= UnitDirtyFlags.INVENTORY_ITEMS.value;
        }
        return updateFlags;
    }

    /**
     * Native support extracted from ::sendInitialUnitAndBuildingSnapshotsForPlayer @0050480D.
     */
    private static int initialCorpseSnapshotUpdateFlags(Unit unit) {
        int updateFlags = unit.idFull <= 0x5FFF
                ? INITIAL_LOW_ID_UNIT_AND_CORPSE_UPDATE_FLAGS
                : INITIAL_HIGH_ID_CORPSE_UPDATE_FLAGS;
        if (unit.ownershipTransferRefreshFlag != 0) {
            updateFlags |= UnitDirtyFlags.OWNER_PLAYER_ID.value;
        }
        return updateFlags;
    }

    /**
     * Native: CServerApp::sendDiplomacyStateSnapshot @00504E87.
     * Fully ported.
     */
    public static void sendDiplomacyStateSnapshot(Player player) {
        if (Globals.gameServer.missionScriptRuntime == null) {
            return;
        }

        recordDiplomacyStateSnapshotRelations(player);
        sendGameAction(DiplomacyAction.prepareForStateSnapshot(player));
    }

    /**
     * Native support extracted from CServerApp::sendDiplomacyStateSnapshot @00504E87
     * g_Grid32x32_ServerRelated updates and PushMessage branch.
     */
    private static void recordDiplomacyStateSnapshotRelations(Player player) {
        int playerId = (short) player.playerId;
        for (Player sourcePlayer : Globals.gameServer.playerList.players) {
            int sourcePlayerId = (short) sourcePlayer.playerId;
            int outgoingFlags = Globals.gameServer.missionScriptRuntime.getRelationFlags(player, sourcePlayer);
            int incomingFlags = Globals.gameServer.missionScriptRuntime.getRelationFlags(sourcePlayer, player);
            boolean relationChanged = Globals.gameServer.updateServerDiplomacyRelationFlags(
                    playerId,
                    sourcePlayerId,
                    outgoingFlags,
                    incomingFlags
            );
            if (sourcePlayer.isActive == 0 && relationChanged && sourcePlayerId != playerId) {
                Globals.gameServer.pushMessage(formatDiplomacyChangedMessage(player, sourcePlayer, incomingFlags));
            }
        }
    }

    /**
     * Native support extracted from CServerApp::sendDiplomacyStateSnapshot @00504E87
     * PushMessage string construction using literals at 005FD538, 005FD558, 005FD564, 005FD56C, and 005FD578.
     */
    private static String formatDiplomacyChangedMessage(Player player, Player sourcePlayer, int incomingFlags) {
        String relationText;
        if ((incomingFlags & CPlayer.ALLIED_MASK) != 0) {
            relationText = " Alliance";
        } else if ((incomingFlags & CPlayer.ENEMY_MASK) != 0) {
            relationText = " War";
        } else {
            relationText = " Neutrality";
        }
        String message = "Diplomacy changed: "
                + sourcePlayer.name
                + " -> "
                + player.name
                + " ="
                + relationText;
        if ((incomingFlags & CPlayer.DIPLOMACY_VISIBLE_MASK) != 0) {
            message += " + Vision";
        }
        return message;
    }

    /**
     * Native support extracted from GameServer::sendInitialScenarioState @004F1D9C and
     * CWorldMap::EncodeTileVisibilityMaskRuns @00559D70.
     */
    public static void sendTileVisibilityMask(Player player) {
        sendGameAction(TileVisibilityMaskAction.prepareForInitialScenarioState(
                player,
                Globals.worldMap.encodeTileVisibilityMaskRuns()
        ));
    }

    /**
     * Native: CServerApp::sendQuestObjectivesQueryOpen @00505204.
     * Fully ported.
     */
    public static void sendQuestObjectivesQueryOpen(
            Player player,
            int queryOpenCode,
            int unusedPayload
    ) {
        sendGameAction(QuestObjectivesQueryOpenAction.prepareForQuestObjectivesQueryOpen(
                player,
                queryOpenCode,
                unusedPayload
        ));
    }

    /**
     * Native: CServerApp::sendGameEventNotification @005052D2.
     * Fully ported.
     */
    public static void sendGameEventNotification(int notificationKind, int notificationValue, Player player) {
        sendGameAction(GameEventNotificationAction.prepareForGameEventNotification(
                player,
                notificationKind,
                notificationValue
        ));
    }

    /**
     * Native: SpawnScenarioMissionEntryUnits @004F36C0.
     * Fully ported.
     */
    public static void spawnScenarioMissionEntryUnits(int scenarioChapter, List<Integer> missionIds) {
        for (int missionId : missionIds) {
            Unit unit = createScenarioMissionEntryUnit(missionId & 0xFFFF, scenarioChapter);
            unit.serverID = missionId & 0xFFFF;
            unit.idFull = Globals.gameServer.allocateNextFreeId() & 0xFFFF;

            Player player = Globals.gameServer.playerList.getFirst();
            unit.owner = player;
            player.ownedUnits.add(unit);

            UnitGroup unitGroup = new UnitGroup();
            player.unitGroups.add(unitGroup);
            unitGroup.addUnit(unit);

            unit.initializeScenarioMissionEntryUnit(Globals.gameServer.missionScriptRuntime);
            unitGroup.initializeScenarioMissionEntryGroup(Globals.gameServer.missionScriptRuntime);
            netUpdate(
                    unit,
                    player,
                    ALL_UNIT_UPDATE_FLAGS,
                    EQUIPMENT_BROADCAST_MASK,
                    0,
                    0
            );
        }
        sendCurrentServerLoopCounter(null);
        flushActiveClientWriteBuffers();
    }

    /**
     * Native support extracted from SpawnScenarioMissionEntryUnits @004F36C0 Unit/Human construction branches.
     * Fully ported support helper.
     */
    private static Unit createScenarioMissionEntryUnit(int missionId, int scenarioChapter) {
        int templateIndex = resolveScenarioMissionEntryTemplateIndex(missionId, scenarioChapter);
        if (templateIndex > 0) {
            HumanInfo humanInfo = Globals.staticDataMgr.humans.get(templateIndex);
            return Human.createFromTemplate(humanInfo.name, false, false);
        }

        int unitInfoIndex = -templateIndex;
        UnitInfo unitInfo = Globals.staticDataMgr.units.get(unitInfoIndex);
        return Unit.createFromTemplateName(unitInfo.name);
    }

    /**
     * Native support extracted from SpawnScenarioMissionEntryUnits @004F36C0 static server-id lookup.
     * Fully ported support helper.
     */
    private static int resolveScenarioMissionEntryTemplateIndex(int missionId, int scenarioChapter) {
        int baseVariantServerId = missionId * 10 + SCENARIO_MISSION_ENTRY_TEMPLATE_BASE;
        int baseHumanIndex = Globals.staticDataMgr.findHumanByServerId(baseVariantServerId);
        if (baseHumanIndex != 0) {
            return resolveScenarioMissionEntryHumanVariantIndex(missionId, scenarioChapter, baseHumanIndex);
        }

        int baseUnitIndex = Globals.staticDataMgr.findUnitByServerId(baseVariantServerId);
        return -resolveScenarioMissionEntryUnitVariantIndex(missionId, scenarioChapter, baseUnitIndex);
    }

    /**
     * Native support extracted from SpawnScenarioMissionEntryUnits @004F36C0 HumanInfo chapter-variant probe loop.
     * Fully ported support helper.
     */
    private static int resolveScenarioMissionEntryHumanVariantIndex(
            int missionId,
            int scenarioChapter,
            int baseVariantIndex
    ) {
        for (int variant = 1; variant < SCENARIO_MISSION_ENTRY_VARIANT_LIMIT; variant++) {
            if (Globals.staticDataMgr.findHumanByServerId(
                    missionId * 10 + SCENARIO_MISSION_ENTRY_TEMPLATE_BASE + variant
            ) != 0) {
                int chapterVariant = Globals.staticDataMgr.findHumanByServerId(
                        missionId * 10 + SCENARIO_MISSION_ENTRY_CHAPTER_VARIANT_BASE + scenarioChapter / 10
                );
                return chapterVariant == 0 ? baseVariantIndex : chapterVariant;
            }
        }
        return baseVariantIndex;
    }

    /**
     * Native support extracted from SpawnScenarioMissionEntryUnits @004F36C0 UnitInfo chapter-variant probe loop.
     * Fully ported support helper.
     */
    private static int resolveScenarioMissionEntryUnitVariantIndex(
            int missionId,
            int scenarioChapter,
            int baseVariantIndex
    ) {
        for (int variant = 1; variant < SCENARIO_MISSION_ENTRY_VARIANT_LIMIT; variant++) {
            if (Globals.staticDataMgr.findUnitByServerId(
                    missionId * 10 + SCENARIO_MISSION_ENTRY_TEMPLATE_BASE + variant
            ) != 0) {
                int chapterVariant = Globals.staticDataMgr.findUnitByServerId(
                        missionId * 10 + SCENARIO_MISSION_ENTRY_CHAPTER_VARIANT_BASE + scenarioChapter / 10
                );
                return chapterVariant == 0 ? baseVariantIndex : chapterVariant;
            }
        }
        return baseVariantIndex;
    }

    /**
     * Native: CServerApp::sendServerChatText @00505263.
     * Fully ported.
     */
    public static void sendServerChatText(
            String text,
            Player player
    ) {
        sendGameAction(ChatTextAction.prepareForServerChatText(text, player));
    }

    /**
     * Native: CServerApp::sendNoPayloadAction @005052F8.
     * Fully ported.
     */
    public static void sendNoPayloadAction(
            GameActionId actionId,
            Player player
    ) {
        sendGameAction(CGameAction.prepareNoPayloadAction(actionId, player));
    }

    /**
     * Native support extracted from Unit::Update @0050F12C and
     * CServerApp::sendTwoDwordAction @00505347.
     * Java helper for the targeted `ShowShopDialogAction (0x83)` send path.
     */
    public static Shop openShopDialog(Shop shop, Player player) {
        sendTwoDwordAction(player, GameActionId.SHOW_SHOP_DIALOG_ACTION_83, shop.id, 0);
        return shop;
    }

    /**
     * Native: CServerApp::sendShopCatalogItemLists @005035E4.
     * `shelfSelector == -1` sends the four sparse shop shelves as subtypes 5..8;
     * positive selectors send one shelf as `selector + 4`.
     * Fully ported.
     */
    static void sendShopCatalogItemLists(CMultiShopInstance shopInstance, Player targetPlayer, int shelfSelector) {
        if (shelfSelector == -1) {
            for (int shelfIndex = 0; shelfIndex < 4; shelfIndex++) {
                sendShopCatalogShelfItemList(
                        shopInstance.getDialogOwnerToken(),
                        shopInstance.shelves[shelfIndex],
                        targetPlayer,
                        shelfIndex + 5
                );
            }
        } else if (shelfSelector > 0) {
            sendShopCatalogShelfItemList(
                    shopInstance.getDialogOwnerToken(),
                    shopInstance.shelves[shelfSelector - 1],
                    targetPlayer,
                    shelfSelector + 4
            );
        }
    }

    /**
     * Native: CServerApp::sendShopCatalogShelfItemList @00503493.
     * Sends one sparse CMultiShopShelf item table, preserving null slots as empty item payloads.
     * Fully ported.
     */
    static void sendShopCatalogShelfItemList(Token dialogOwner, CMultiShopShelf shelf, Player targetPlayer, int actionSubtype) {
        ItemListAction action = ItemListAction.prepareForShopCatalogShelfItemList(
                dialogOwner,
                shelf,
                targetPlayer,
                actionSubtype
        );
        sendGameAction(action);
    }

    /**
     * Native support extracted from Unit::Update @0050F12C and
     * CServerApp::sendTwoDwordAction @00505347.
     * Java helper for the targeted `ShowInnDialogAction (0x84)` send path.
     */
    public static Inn openInnDialog(Inn inn, Player player) {
        sendTwoDwordAction(player, GameActionId.SHOW_INN_DIALOG_ACTION_84, inn.id, inn.idFull);
        return inn;
    }

    /**
     * Native: CServerApp::sendTwoDwordAction @00505347.
     * Fully ported.
     */
    public static void sendTwoDwordAction(
            Player player,
            GameActionId actionId,
            int firstPayload,
            int secondPayload
    ) {
        sendGameAction(TwoDwordAction.prepareForTwoDwordAction(player, actionId, firstPayload, secondPayload));
    }

    /**
     * Native: CServerApp::sendUnitVisibilityAction @005053A8.
     * Fully ported.
     */
    public static void sendUnitVisibilityAction(
            Unit unit,
            boolean hideUnit,
            Player player
    ) {
        UnitTokenAction action = UnitTokenAction.prepareForUnitVisibilityAction(unit, hideUnit, player);

        if (player == null) {
            for (Player targetPlayer : Globals.gameServer.playerList.players) {
                if (shouldReceiveLiveUnitVisualAction(targetPlayer, unit)) {
                    sendUnitVisibilityActionToPlayer(action, unit, hideUnit, targetPlayer);
                }
            }
            return;
        }

        if (shouldSendUnitVisibilityAction(unit, hideUnit, player)) {
            sendGameAction(action.Clone());
        }
    }

    /**
     * Native: CServerApp::sendPlayerKnowledgeAction @00505582.
     * Fully ported.
     */
    public static void sendPlayerKnowledgeAction(
            int knowledgeServerIdOrZeroForSnapshot,
            Player player
    ) {
        if (knowledgeServerIdOrZeroForSnapshot == 0) {
            sendGameAction(CreatureKnowledgeAction.prepareForPlayerKnowledgeSnapshot(player));
            return;
        }

        sendGameAction(PlayerKnowledgeProgressAction.prepareForKnowledgeProgress(
                knowledgeServerIdOrZeroForSnapshot,
                player
        ));
    }

    /**
     * Native: CServerApp::broadcastPlayerKickedAction @00505697.
     * Fully ported.
     */
    public static void broadcastPlayerKickedAction(Player player) {
        sendGameAction(PlayerKickedAction.prepareForPlayerKickedBroadcast(player).Clone());
    }

    /**
     * Native: CServerApp::sendQuestListAction @00506526.
     * Fully ported.
     */
    public static void sendQuestListAction(
            QuestsStorage questsStorage,
            Player player,
            boolean sendInnQuestList
    ) {
        ShortArrayBlobAction action = sendInnQuestList
                ? InnQuestsAction.prepareForQuestList(questsStorage, player)
                : PlayerQuestsAction.prepareForQuestList(questsStorage, player);
        sendGameAction(action);
    }

    /**
     * Native: CServerApp::dispatchUnitTargetSpell @005041D6.
     * Fully ported.
     */
    public static void dispatchUnitTargetSpell(Unit caster, Spell spell, Token target, short castDelayTicks) {
        EffectAction action = EffectAction.prepareForUnitTargetSpell(caster, spell, target, castDelayTicks);
        if (caster.isUnitToken() != 0) {
            caster.lastOwnerSyncTick = Globals.gameServer.serverLoopCounter;
        }
        dispatchSpellEffectVisibilityGatedAction(action, caster.m_pTargetHandle);
    }

    /**
     * Native: CServerApp::dispatchPointTargetSpell @005042BA.
     * Fully ported.
     */
    public static void dispatchPointTargetSpell(Unit caster, Spell spell, TargetHandle targetHandle, short castDelayTicks) {
        EffectAction action = EffectAction.prepareForPointTargetSpell(caster, spell, targetHandle, castDelayTicks);
        if (caster.isUnitToken() != 0) {
            caster.lastOwnerSyncTick = Globals.gameServer.serverLoopCounter;
        }
        dispatchSpellEffectVisibilityGatedAction(action, caster.m_pTargetHandle);
    }

    /**
     * Native: CServerApp::dispatchSpellTargets @00504391.
     * Fully ported.
     */
    public static void dispatchSpellTargets(Unit caster, @SuppressWarnings("unused") Spell spell, CustomList<Unit> targets) {
        ShortArrayBlobAction action = caster.getTokenTypeId() == 0
                ? EffectMultiFromAction.prepareForSpellTargets(caster, targets)
                : EffectMultiTargetAction.prepareForSpellTargets(caster, targets);
        if (caster.isUnitToken() != 0) {
            caster.lastOwnerSyncTick = Globals.gameServer.serverLoopCounter;
        }
        dispatchSpellEffectVisibilityGatedAction(action, caster.m_pTargetHandle);
    }

    /**
     * Native: CServerApp::sendUnitCommandStartAction @005040C4.
     * Fully ported.
     */
    public static void sendUnitCommandStartAction(
            Unit unit,
            int commandDirection,
            int commandSegments,
            GameActionId actionId
    ) {
        unit.getOwnerSyncTickDelta();
        unit.lastOwnerSyncTick = Globals.gameServer.serverLoopCounter + (commandSegments & 0xFF);
        UnitCommandStartAction action = UnitCommandStartAction.prepareForCommandStart(
                unit,
                commandDirection,
                commandSegments,
                actionId
        );
        sendUnitActionVisibilityGated(action, unit);
    }

    /**
     * Native: CServerApp::emitActionStart @00504AB8.
     * Fully ported.
     */
    public static void emitActionStart(Unit unit) {
        if ((unit.defaultCastRange & 0xFF) > 1) {
            emitActionStartExtended(unit);
            return;
        }
        sendUnitCommandStartAction(
                unit,
                unit.movementState.getFacingStep2(),
                (unit.attackChargeTicks & 0xFF) + (unit.attackRelaxTicks & 0xFF),
                GameActionId.UNIT_ATTACK_ACTION_71
        );
    }

    /**
     * Native: CServerApp::emitActionStartExtended @00504155.
     * Fully ported.
     */
    public static void emitActionStartExtended(Unit unit) {
        RangedAttackAction action = RangedAttackAction.prepareForExtendedActionStart(unit);
        unit.lastOwnerSyncTick = Globals.gameServer.serverLoopCounter
                + (unit.attackChargeTicks & 0xFF)
                + (unit.attackRelaxTicks & 0xFF);
        sendRangedAttackActionVisibilityGated(action, unit);
    }

    /**
     * Native: CServerApp::sendUnitActionVisibilityGated @005039E2.
     * Fully ported.
     */
    private static void sendUnitActionVisibilityGated(UnitCommandStartAction action, Unit unit) {
        if (action.playerID.get() == 0) {
            for (Player player : Globals.gameServer.playerList.players) {
                if (shouldReceiveLiveUnitVisualAction(player, unit)) {
                    sendUnitActionVisibilityGatedToPlayer(action, unit, player);
                }
            }
            return;
        }

        Player targetPlayer = Globals.gameServer.playerList.getPlayerById(action.playerID.get());
        sendUnitActionVisibilityGatedToPlayer(action, unit, targetPlayer);
    }

    /**
     * Native support extracted from CServerApp::sendUnitActionVisibilityGated @005039E2.
     * Fully ported.
     */
    private static void sendUnitActionVisibilityGatedToPlayer(
            UnitCommandStartAction action,
            Unit unit,
            Player targetPlayer
    ) {
        if ((unit.visiblePlayerMask & targetPlayer.scanMask) == 0
                && Globals.gameServer.networkSessionActive != 0) {
            deferHiddenMoveActionIfNeeded(action, unit, targetPlayer);
            return;
        }
        action.playerID.set(targetPlayer.playerId);
        sendGameAction(action);
    }

    /**
     * Native support extracted from CServerApp::sendUnitActionVisibilityGated @005039E2 for FixedDwordAction72.
     * Fully ported.
     */
    private static void sendRangedAttackActionVisibilityGated(RangedAttackAction action, Unit unit) {
        if (action.playerID.get() == 0) {
            for (Player player : Globals.gameServer.playerList.players) {
                if (shouldReceiveLiveUnitVisualAction(player, unit)) {
                    sendRangedAttackActionVisibilityGatedToPlayer(action, unit, player);
                }
            }
            return;
        }

        Player targetPlayer = Globals.gameServer.playerList.getPlayerById(action.playerID.get());
        sendRangedAttackActionVisibilityGatedToPlayer(action, unit, targetPlayer);
    }

    /**
     * Native support extracted from CServerApp::sendUnitActionVisibilityGated @005039E2 for FixedDwordAction72.
     * Fully ported.
     */
    private static void sendRangedAttackActionVisibilityGatedToPlayer(
            RangedAttackAction action,
            Unit unit,
            Player targetPlayer
    ) {
        if ((unit.visiblePlayerMask & targetPlayer.scanMask) == 0
                && Globals.gameServer.networkSessionActive != 0) {
            return;
        }
        action.playerID.set(targetPlayer.playerId);
        sendGameAction(action);
    }

    /**
     * Java support for native map-visual broadcasts such as CServerApp::sendUnitActionVisibilityGated @005039E2.
     * Native playerID-0 visual broadcasts iterate map-load-pending recipients only, because the local native map can
     * observe live Unit objects directly. Java renders copied CUnit instances, so loaded connected clients also need
     * these packets to transfer movement/attack/effect state into MapVisualObject.objects.
     */
    private static boolean shouldReceiveLiveMapVisualAction(Player player) {
        return player != null
                && (player.isMapLoadPending() || player.clientConnected != 0 || isCurrentMapVisualPlayer(player));
    }

    /**
     * Java support for CServerApp::sendUnitActionVisibilityGated @005039E2 in the copied-visual-object model.
     * Keeps the existing owner-recipient behavior used by broader unit state updates while also targeting live clients.
     */
    private static boolean shouldReceiveLiveUnitVisualAction(Player player, Unit unit) {
        return shouldReceiveLiveMapVisualAction(player) || isUnitOwnerRecipient(player, unit);
    }

    /**
     * Native support extracted from CServerApp::sendUnitActionVisibilityGated @005039E2 for copied map visuals.
     * Native compares the live server `Player *` owner object, but Java can compare copied or deserialized player
     * representations that still share the native player id.
     */
    private static boolean isUnitOwnerRecipient(Player player, Unit unit) {
        return player != null
                && unit != null
                && unit.owner != null
                && (player == unit.owner || player.playerId == unit.owner.playerId);
    }

    /**
     * Native support extracted from CServerApp::sendUnitActionVisibilityGated @005039E2 for the copied-map recipient
     * that is currently driving `MapVisualObject`.
     */
    private static boolean isCurrentMapVisualPlayer(Player player) {
        return Globals.mainWindow != null
                && Globals.mainWindow.pMapVisualObject != null
                && Globals.mainWindow.pMapVisualObject.currentPlayer != null
                && Globals.mainWindow.pMapVisualObject.currentPlayer.playerId == player.playerId;
    }

    /**
     * Native support extracted from CServerApp::sendUnitActionVisibilityGated @005039E2 hidden move branch.
     * Fully ported.
     */
    private static void deferHiddenMoveActionIfNeeded(
            UnitCommandStartAction action,
            Unit unit,
            Player targetPlayer
    ) {
        if (isSpecialPlayerId(targetPlayer.playerId)
                && action.ID.get() == GameActionId.UNIT_MOVE_ACTION_6B.id
                && (unit.deferredNetUpdateFlagsByPlayerId[targetPlayer.playerId - SPECIAL_PLAYER_ID_FIRST]
                & UnitDirtyFlags.VISIBILITY_STATE.value) == 0) {
            deferNetUpdateForSpecialPlayer(unit, targetPlayer, HIDDEN_MOVE_DEFERRED_FLAGS);
            sendUnitVisibilityAction(unit, true, targetPlayer);
        }
    }

    /**
     * Native: CServerApp::NetUpdate @00502019.
     * Fully ported.
     */
    public static void netUpdate(Unit unit, int updateMask) {
        netUpdate(unit, null, updateMask, 0, 0, 0);
    }

    /**
     * Native: CServerApp::NetUpdate @00502019.
     * Fully ported.
     */
    public static void netUpdate(
            Unit unit,
            Player targetPlayer,
            int updateMask,
            int equipmentMask,
            int inventoryStart,
            int inventoryEnd
    ) {
        if (targetPlayer == null) {
            for (Player player : Globals.gameServer.playerList.players) {
                if (shouldReceiveLiveUnitVisualAction(player, unit)) {
                    netUpdate(unit, player, updateMask, equipmentMask, inventoryStart, inventoryEnd);
                }
            }
            return;
        }

        if ((unit.visiblePlayerMask & targetPlayer.scanMask) == 0
                && Globals.gameServer.networkSessionActive != 0) {
            deferNetUpdateForSpecialPlayer(unit, targetPlayer, updateMask);
            return;
        }

        UnitChangeAction action = UnitChangeAction.prepareForNetUpdate(unit, targetPlayer);

        updateMask = filterNetUpdateMask(unit, targetPlayer, updateMask);
        if (unit.m_nMaxMP == 0) {
            updateMask &= ~UnitDirtyFlags.MP.value;
        }
        updateMask = clearUnsupportedNetUpdateFlags(unit, targetPlayer, updateMask);

        action.appendNetUpdatePayload(unit, updateMask);

        updateMask = action.appendNetUpdateDisplayName(unit, updateMask);

        boolean hasEffects = hasFlag(updateMask, UnitDirtyFlags.EFFECTS);
        action.markNetUpdateEffectsFlag(updateMask);

        boolean phaseChanged = hasFlag(updateMask, UnitDirtyFlags.VISIBILITY_STATE);
        if ((updateMask & ~NET_UPDATE_BODY_SKIP_MASK) != 0) {
            sendGameAction(action.toResolvedNetUpdateAction());
        }
        if (hasFlag(updateMask, UnitDirtyFlags.EQUIPPED_ITEMS)) {
            sendUnitEquipmentStateUpdate(unit, targetPlayer, equipmentMask);
        }
        if (hasFlag(updateMask, UnitDirtyFlags.INVENTORY_ITEMS) && unit.owner == targetPlayer) {
            sendUnitInventoryItemsUpdate(unit, targetPlayer, inventoryStart, inventoryEnd);
        }

        if (unit.needsInitialSyncForPlayer(targetPlayer) || hasEffects) {
            for (Effect effect : unit.effects) {
                if ((effect.key & 0xFFFF) != 0) {
                    addEffect(effect, unit);
                }
            }
        }

        if ((unit.needsInitialSyncForPlayer(targetPlayer) && ((unit.status & UNIT_STATUS_HIDDEN_OR_INACTIVE) != 0))
                || phaseChanged) {
            sendUnitVisibilityAction(unit, (unit.status & UNIT_STATUS_HIDDEN_OR_INACTIVE) != 0, targetPlayer);
        }
        unit.markInitialSyncSentToPlayer(targetPlayer);
        clearDeferredNetUpdateForSpecialPlayer(unit, targetPlayer);
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019.
     * Fully ported support helper.
     */
    private static int filterNetUpdateMask(Unit unit, Player targetPlayer, int updateMask) {
        if (unit.isHumanoidToken() == 0) {
            return filterNonHumanoidNetUpdateMask(unit, targetPlayer, updateMask);
        }
        return filterHumanoidNetUpdateMask(unit, targetPlayer, updateMask);
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019.
     * Fully ported support helper.
     */
    private static int filterNonHumanoidNetUpdateMask(Unit unit, Player targetPlayer, int updateMask) {
        if ((unit.idFull & 0xFFFFFFFFL) < 0x6000L) {
            if (unit.serverID > 0x14) {
                updateMask &= NON_HUMANOID_HIGH_SERVER_ID_MASK;
            }
        } else if (Globals.gameServer.networkSessionActive != 0) {
            updateMask &= ~UnitDirtyFlags.SERVER_ID.value;
        }

        if (unit.owner != targetPlayer) {
            int filteredMask = updateMask & NON_OWNER_NON_HUMANOID_MASK_WITH_MAX_MP;
            int tableMaxHp = getDifficultyAdjustedUnitHealthMax(unit);
            int typeId = unit.getTokenTypeId();
            if (tableMaxHp == unit.m_nMaxHP && typeId != 0x1A && typeId != 0x1B) {
                filteredMask = updateMask & NON_OWNER_NON_HUMANOID_MASK_WITH_MP;
            }
            return filteredMask;
        }
        return updateMask;
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019.
     * Fully ported support helper.
     */
    private static int filterHumanoidNetUpdateMask(Unit unit, Player targetPlayer, int updateMask) {
        if (unit.owner != targetPlayer) {
            int typeId = unit.getTokenTypeId();
            if (typeId < 0x21 || typeId > 0x3F) {
                updateMask &= NON_OWNER_HUMANOID_MASK;
            } else if (Globals.gameServer.missionScriptRuntime != null
                    && !Globals.gameServer.missionScriptRuntime.hasRelationFlag(
                    unit.owner,
                    targetPlayer,
                    PLAYER_RELATION_VISIBLE_MASK
            )) {
                updateMask &= NON_OWNER_HUMANOID_MASK;
            }
        }
        if (Globals.gameServer.networkSessionActive != 0) {
            updateMask &= ~UnitDirtyFlags.SERVER_ID.value;
        }
        return updateMask;
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019.
     * Fully ported support helper.
     */
    private static int getDifficultyAdjustedUnitHealthMax(Unit unit) {
        int healthMax = unit.unitInfoLine.getValue(UnitColumn.HEALTH_MAX.index);
        if (Globals.gameServer.networkSessionActive == 0) {
            if (Globals.gameServer.difficultyLevelSetting == 1) {
                healthMax /= 2;
            } else if (Globals.gameServer.difficultyLevelSetting == 3) {
                healthMax = (int) (healthMax * 1.5d);
            }
        }
        return healthMax;
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019.
     * Fully ported support helper.
     */
    private static int clearUnsupportedNetUpdateFlags(Unit unit, Player targetPlayer, int updateMask) {
        if (unit.isHumanoidToken() == 0) {
            updateMask &= ~UnitDirtyFlags.SKILL_BONUSES.value;
        }
        if (Globals.gameServer.networkSessionActive == 0
                || !hasFlag(updateMask, UnitDirtyFlags.CONTROLLED_OWNER_STATS)
                || unit.owner == null
                || unit != unit.owner.controlledUnit
                || targetPlayer != unit.owner) {
            updateMask &= ~UnitDirtyFlags.CONTROLLED_OWNER_STATS.value;
        }
        return updateMask;
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019.
     * Fully ported support helper.
     */
    private static void deferNetUpdateForSpecialPlayer(Unit unit, Player targetPlayer, int updateMask) {
        if (isSpecialPlayerId(targetPlayer.playerId)) {
            int index = targetPlayer.playerId - SPECIAL_PLAYER_ID_FIRST;
            unit.deferredNetUpdateFlagsByPlayerId[index] |= updateMask;
        }
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019.
     * Fully ported support helper.
     */
    private static void clearDeferredNetUpdateForSpecialPlayer(Unit unit, Player targetPlayer) {
        if (isSpecialPlayerId(targetPlayer.playerId)) {
            int index = targetPlayer.playerId - SPECIAL_PLAYER_ID_FIRST;
            unit.deferredNetUpdateFlagsByPlayerId[index] = UnitDirtyFlags.NONE.value;
        }
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019.
     * Fully ported support helper.
     */
    private static boolean isSpecialPlayerId(int playerId) {
        return playerId >= SPECIAL_PLAYER_ID_FIRST && playerId < SPECIAL_PLAYER_ID_LIMIT;
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019.
     * Fully ported support helper.
     */
    private static boolean hasFlag(int updateMask, UnitDirtyFlags flag) {
        return (updateMask & flag.value) != UnitDirtyFlags.NONE.value;
    }

    /**
     * Native: CServerApp::sendUnitEquipmentStateUpdate @00502D67.
     * Fully ported.
     */
    public static void sendUnitEquipmentStateUpdate(
            Unit unit,
            Player targetPlayer,
            int equipmentMask
    ) {
        if (unit.isHumanoidToken() == 0 || !(unit instanceof Humanoid humanoid)) {
            return;
        }

        int slotMask = equipmentMask & EQUIPMENT_SLOT_MASK;
        if (slotMask == 0) {
            return;
        }

        if (targetPlayer == null) {
            for (Player player : Globals.gameServer.playerList.players) {
                if (shouldReceiveLiveUnitVisualAction(player, unit)) {
                    sendUnitEquipmentStateUpdate(unit, player, EQUIPMENT_BROADCAST_MASK);
                }
            }
            return;
        }

        int typeId = unit.getTokenTypeId();
        if (!isPublicHumanoidType(typeId)) {
            if (unit.hasInitialSyncSentToPlayer(targetPlayer)) {
                return;
            }
            if (unit.owner != targetPlayer) {
                sendUnitEquipmentHashListUpdate(humanoid, targetPlayer, slotMask);
                return;
            }
        }
        sendUnitEquipmentFullStateUpdate(humanoid, targetPlayer, slotMask);
    }

    /**
     * Native support extracted from CServerApp::sendUnitEquipmentStateUpdate @00502D67.
     * Fully ported.
     */
    private static boolean isPublicHumanoidType(int typeId) {
        return typeId >= HUMANOID_PUBLIC_TYPE_FIRST && typeId <= HUMANOID_PUBLIC_TYPE_LAST;
    }

    /**
     * Native support extracted from CServerApp::sendUnitEquipmentStateUpdate @00502D67.
     * Fully ported.
     */
    private static void sendUnitEquipmentHashListUpdate(Humanoid unit, Player targetPlayer, int slotMask) {
        EnemyEquipmentAction action = EnemyEquipmentAction.prepareForEquipmentHashList(unit, targetPlayer, slotMask);
        sendGameAction(action);
    }

    /**
     * Native support extracted from CServerApp::sendUnitEquipmentStateUpdate @00502D67.
     * Fully ported.
     */
    private static void sendUnitEquipmentFullStateUpdate(Humanoid unit, Player targetPlayer, int slotMask) {
        UnitEquipmentStateAction action = UnitEquipmentStateAction.prepareForEquipmentFullState(unit, targetPlayer, slotMask);
        sendGameAction(action);
    }

    /**
     * Native: CServerApp::sendUnitInventoryItemsUpdate @00503236.
     * Fully ported.
     */
    public static void sendUnitInventoryItemsUpdate(
            Unit unit,
            Player targetPlayer,
            int inventoryStart,
            int inventoryEnd
    ) {
        int typeId = unit.getTokenTypeId();
        if (!isPublicHumanoidType(typeId)) {
            return;
        }

        UnitInventoryStateAction action = UnitInventoryStateAction.prepareForUnitInventoryItemsUpdate(
                unit,
                targetPlayer,
                inventoryStart,
                inventoryEnd
        );
        sendGameAction(action);
    }

    /**
     * Native: CServerApp::sendDialogItemList @0050338E.
     * Fully ported.
     */
    static void sendDialogItemList(Token dialogOwner, Inventory inventory, Player targetPlayer, int actionSubtype) {
        ItemListAction action = ItemListAction.prepareForDialogItemList(
                dialogOwner,
                inventory,
                targetPlayer,
                actionSubtype
        );
        sendGameAction(action);
    }

    /**
     * Native: CServerApp::AddEffect @005044BA.
     * Fully ported.
     */
    private static void addEffect(
            Effect effect,
            Token token,
            GameActionId actionId
    ) {
        Fixed3ByteAction action = Fixed3ByteAction.prepareForEffectChange(effect, token, actionId);
        dispatchSpellEffectVisibilityGatedAction(action, token.m_pTargetHandle);
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019; calls CServerApp::AddEffect @005044BA
     * with action id 0, which native maps to EFFECT_HANG_ACTION_88.
     */
    private static void addEffect(Effect effect, Unit unit) {
        addEffect(effect, unit, GameActionId.UNKNOWN_ACTION_00);
    }

    /**
     * Native support extracted from Effect::applyToTarget @0051CE12; calls CServerApp::AddEffect @005044BA.
     */
    public static void notifyEffectAdded(Effect effect, Unit unit) {
        addEffect(effect, unit, GameActionId.UNKNOWN_ACTION_00);
    }

    /**
     * Native: CServerApp::notifyEffectRemoved @00504498.
     * Fully ported.
     */
    public static void notifyEffectRemoved(Effect effect, Unit unit) {
        addEffect(effect, unit, GameActionId.EFFECT_GONE_ACTION_89);
    }

    /**
     * Native: CServerApp::sendEffectTokenVisualAction @00504524.
     * Fully ported.
     */
    public static void sendEffectTokenVisualAction(Token effectToken, int visualType) {
        EffectAction action = EffectAction.prepareForEffectTokenVisual(effectToken, visualType);
        dispatchSpellEffectVisibilityGatedAction(action, effectToken.m_pTargetHandle);
    }

    /**
     * Java support for sending a casterless point-projectile visual through spell-effect visibility gates.
     * not ported.
     */
    public static void sendPointProjectileVisualAction(TargetHandle targetHandle, int projectileType, int segments) {
        PointProjectileVisualAction action = PointProjectileVisualAction.prepareForPointProjectileVisual(
                targetHandle,
                projectileType,
                segments
        );
        dispatchSpellEffectVisibilityGatedAction(action, targetHandle);
    }

    /**
     * Native: CServerApp::sendSpellEffectStateAction @005045A5.
     * Fully ported.
     */
    public static void sendSpellEffectStateAction(SpellEffect spellEffect, int applyFlag) {
        if ((spellEffect.key & 0xFFFF) == 2) {
            sendEffectTokenVisualAction(spellEffect, SPELL_EFFECT_TOKEN_VISUAL_TYPE);
            return;
        }

        AreaEffect areaEffect = (AreaEffect) spellEffect;
        AreaEffectAction action = AreaEffectAction.prepareForSpellEffectState(areaEffect, applyFlag);
        sendAreaEffectActionVisibilityGated(action, areaEffect);
    }

    /**
     * Native: CServerApp::sendAreaEffectActionVisibilityGated @00503E41.
     * Fully ported.
     */
    private static void sendAreaEffectActionVisibilityGated(AreaEffectAction action, AreaEffect areaEffect) {
        if (action.playerID.get() == 0) {
            for (Player player : Globals.gameServer.playerList.players) {
                if (shouldReceiveLiveMapVisualAction(player)) {
                    sendAreaEffectActionVisibilityGatedToPlayer(action, areaEffect, player);
                }
            }
            return;
        }

        Player targetPlayer = Globals.gameServer.playerList.getPlayerById(action.playerID.get());
        sendAreaEffectActionVisibilityGatedToPlayer(action, areaEffect, targetPlayer);
    }

    /**
     * Native support extracted from CServerApp::sendAreaEffectActionVisibilityGated @00503E41.
     * Fully ported.
     */
    private static void sendAreaEffectActionVisibilityGatedToPlayer(
            AreaEffectAction action,
            AreaEffect areaEffect,
            Player targetPlayer
    ) {
        if ((areaEffect.visiblePlayerMask & targetPlayer.scanMask) == 0
                && Globals.gameServer.networkSessionActive != 0) {
            if (isSpecialPlayerId(targetPlayer.playerId)) {
                if (action.applyFlag.get() == 0) {
                    if ((areaEffect.deferredAreaEffectActionMask & targetPlayer.scanMask) == 0) {
                        action.playerID.set(targetPlayer.playerId);
                        sendGameAction(action);
                    }
                } else {
                    areaEffect.deferredAreaEffectActionMask |= targetPlayer.scanMask;
                }
            }
            return;
        }

        action.playerID.set(targetPlayer.playerId);
        sendGameAction(action);
    }

    /**
     * Native support extracted from CServerApp::sendUnitVisibilityAction @005053A8.
     */
    private static void sendUnitVisibilityActionToPlayer(
            UnitTokenAction action,
            Unit unit,
            boolean hideUnit,
            Player player
    ) {
        if (shouldSendUnitVisibilityAction(unit, hideUnit, player)) {
            sendGameAction(action.cloneForUnitVisibilityRecipient(player));
        }
    }

    /**
     * Native support extracted from CServerApp::sendUnitVisibilityAction @005053A8.
     */
    private static boolean shouldSendUnitVisibilityAction(Unit unit, boolean hideUnit, Player player) {
        if (Globals.gameServer.networkSessionActive == 0 || (unit.visiblePlayerMask & player.scanMask) != 0) {
            return true;
        }
        deferNetUpdateForSpecialPlayer(unit, player, UnitDirtyFlags.VISIBILITY_STATE.value);
        return hideUnit;
    }

    /**
     * Native: CServerApp::dispatchSpellEffectVisibilityGatedAction @00503BEF.
     * Fully ported.
     */
    private static void dispatchSpellEffectVisibilityGatedAction(
            CGameAction action,
            TargetHandle targetHandle
    ) {
        if (action.playerID.get() == 0) {
            for (Player player : Globals.gameServer.playerList.players) {
                if (shouldReceiveLiveMapVisualAction(player)) {
                    dispatchSpellEffectVisibilityGatedActionToPlayer(action, targetHandle, player);
                }
            }
            return;
        }

        Player player = Globals.gameServer.playerList.getPlayerById(action.playerID.get());
        if (canPlayerSeeSpellEffectTarget(targetHandle, player)) {
            sendGameAction(action);
            return;
        }
        deferSpellEffectVisibilityActionForPlayer(action, player);
    }

    /**
     * Native support extracted from CServerApp::dispatchSpellEffectVisibilityGatedAction @00503BEF.
     */
    private static void dispatchSpellEffectVisibilityGatedActionToPlayer(
            CGameAction action,
            TargetHandle targetHandle,
            Player player
    ) {
        action.playerID.set(player.playerId);
        if (canPlayerSeeSpellEffectTarget(targetHandle, player)) {
            sendGameAction(action);
            return;
        }
        deferSpellEffectVisibilityActionForPlayer(action, player);
    }

    /**
     * Native support extracted from CServerApp::dispatchSpellEffectVisibilityGatedAction @00503BEF.
     */
    private static boolean canPlayerSeeSpellEffectTarget(TargetHandle targetHandle, Player player) {
        if (Globals.worldMap != null && Globals.gameServer.networkSessionActive != 0) {
            int xBucket = (targetHandle.getX() >> 3) + 1;
            int yBucket = (targetHandle.getY() >> 3) + 1;
            int visibleMask = Globals.worldMap.unitVisibilityState0x92ECC.coarseUnitMaskGrid0x0400[xBucket][yBucket];
            return (visibleMask & (player.scanMask & 0xFFFF)) != 0;
        }
        return Globals.gameServer.networkSessionActive == 0;
    }

    /**
     * Native support extracted from CServerApp::dispatchSpellEffectVisibilityGatedAction @00503BEF.
     */
    private static void deferSpellEffectVisibilityActionForPlayer(CGameAction action, Player player) {
        if (isSpecialPlayerId(player.playerId) && isSpellEffectVisibilityDeferredAction(action.ID.get())) {
            Unit unit = findActiveUnitForDeferredSpellEffectAction(action);
            if (unit == null) {
                return;
            }
            deferNetUpdateForSpecialPlayer(unit, player, UnitDirtyFlags.EFFECTS.value);
        }
    }

    /**
     * Native support extracted from CServerApp::dispatchSpellEffectVisibilityGatedAction @00503BEF;
     * mirrors UnitList::FindByTokenId(g_activeUnits, action payload token id).
     */
    private static Unit findActiveUnitForDeferredSpellEffectAction(CGameAction action) {
        byte[] tokenBytes = action.GetSlice(CGameAction.BODY_OFFSET, Short.BYTES);
        int tokenId = (tokenBytes[0] & 0xFF) | ((tokenBytes[1] & 0xFF) << Byte.SIZE);
        return Globals.gameServer.activeUnits.findByTokenId(tokenId);
    }

    /**
     * Native support extracted from CServerApp::dispatchSpellEffectVisibilityGatedAction @00503BEF.
     */
    private static boolean isSpellEffectVisibilityDeferredAction(int actionId) {
        return actionId > SPELL_EFFECT_VISIBILITY_FIRST_DEFERRED_ACTION_ID
                && actionId < SPELL_EFFECT_VISIBILITY_LAST_DEFERRED_ACTION_ID;
    }

    /**
     * Native support extracted from MapVisualObject::connectAndJoinSession @0040D480 direct-delivery setup.
     */
    public static void initializeDirectDeliveryLoopback() {
        setPeerApp(LOCAL_ENDPOINT, REMOTE_ENDPOINT);
        setPeerApp(REMOTE_ENDPOINT, LOCAL_ENDPOINT);
        processNetworkEvents(LOCAL_ENDPOINT);
        processNetworkEvents(REMOTE_ENDPOINT);
    }

    /**
     * Native support extracted from idle network-event pumping @00481CCB and CServerApp::ProcessNetworkEvents @0050101E.
     */
    public static void processNetworkEvents() {
        processNetworkEvents(REMOTE_ENDPOINT);
        processNetworkEvents(LOCAL_ENDPOINT);
    }

    /**
     * Native support extracted from CServerApp::ProcessNetworkEvents @0050101E on g_CServerApp_remote.
     */
    public static void processRemoteNetworkEvents() {
        processNetworkEvents(REMOTE_ENDPOINT);
    }

    /**
     * Native support extracted from direct client net-id assignment @004F0CBE.
     */
    public static void bindDirectClientNetId(int netId) {
        LOCAL_ENDPOINT.bindPairedClientNetId(netId);
        REMOTE_ENDPOINT.bindPairedClientNetId(netId);
    }

    /**
     * Native: CServerApp::ReadNextGameAction @00501831 on g_CServerApp_remote.
     * Fully ported.
     */
    public static CGameAction readNextGameAction() {
        return readNextClientGameAction();
    }

    /**
     * Native: CServerApp::ReadNextGameAction @00501831 on g_CServerApp_remote.
     * Fully ported.
     */
    public static CGameAction readNextClientGameAction() {
        return REMOTE_ENDPOINT.ReadNextGameAction();
    }

    /**
     * Native: CServerApp::ReadNextGameAction @00501831 on g_CServerApp_local.
     * Fully ported.
     */
    public static CGameAction readNextServerGameAction() {
        return LOCAL_ENDPOINT.ReadNextGameAction();
    }

    /**
     * Native: CServerApp::GetPendingSegmentMarkerCount @00501913 on g_CServerApp_remote.
     * Fully ported.
     */
    public static int getPendingSegmentMarkerCount() {
        return REMOTE_ENDPOINT.GetPendingSegmentMarkerCount();
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2 and
     * MapVisualObject::sendPlayerJoinAndWaitForPlayerList @0040D791 through
     * CServerApp::GetNextClient_ @0041EF50 on g_CServerApp_remote.
     * Fully ported.
     */
    public static void setNextRemoteClientSendInterval(int sendIntervalMs) {
        CBufferManager client = getNextRemoteClient();
        if (client != null) {
            CLlDriver.setClientSendIntervalMs(client.GetIPAddress(), sendIntervalMs);
        }
    }

    /**
     * Native: CServerApp::GetNextClient_ @0041EF50 on g_CServerApp_remote.
     * Fully ported.
     */
    private static CBufferManager getNextRemoteClient() {
        return REMOTE_ENDPOINT.getNextClient();
    }

    /**
     * Native: CServerApp::GetClientByNetId @0050155F on g_CServerApp_local.
     * Fully ported.
     */
    public static CBufferManager getLocalClientByNetId(int playerId) {
        return LOCAL_ENDPOINT.GetClientByNetId(playerId);
    }

    /**
     * Native: CServerApp::GetClientByNetId @0050155F on g_CServerApp_remote.
     * Fully ported.
     */
    public static CBufferManager getRemoteClientByNetId(int playerId) {
        return REMOTE_ENDPOINT.GetClientByNetId(playerId);
    }

    /**
     * Native: CServerApp::GetClientByMaskedSocketId @005015E2 on g_CServerApp_local.
     * Fully ported.
     */
    public static CBufferManager getLocalClientByMaskedSocketId(int maskedSocketId) {
        return LOCAL_ENDPOINT.GetClientByMaskedSocketId(maskedSocketId);
    }

    /**
     * Native support extracted from CMainWindow::refreshWindowedDedicatedServerStatus @004828D7
     * CMap::Lookup(&g_CServerApp_local.clientTrafficStatsByIp, ...).
     */
    public static ClientTrafficStats getLocalClientTrafficStats(int ipAddress) {
        return LOCAL_ENDPOINT.clientTrafficStatsByIp.get(ipAddress);
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43
     * CMap::Lookup(&g_CServerApp_remote.clientTrafficStatsByIp, ...).
     * Fully ported.
     */
    public static ClientTrafficStats getRemoteClientTrafficStats(int ipAddress) {
        return REMOTE_ENDPOINT.clientTrafficStatsByIp.get(ipAddress);
    }

    /**
     * Native support extracted from DedicatedServerConsoleVisualObject::Update @0044C553
     * CMap::GetStartPosition/GetNextAssoc walk over g_CServerApp_local.clientTrafficStatsByIp.
     * Fully ported.
     */
    public static Iterable<Map.Entry<Integer, ClientTrafficStats>> localClientTrafficStatsEntries() {
        return LOCAL_ENDPOINT.clientTrafficStatsByIp.entrySet();
    }

    /**
     * Native: CServerApp::ConfigurePacketCompression @0050104D on g_CServerApp_local.
     * Fully ported.
     */
    public static void configureLocalPacketCompression(int compressionStreamId, int compressionMode) throws IOException {
        configurePacketCompression(LOCAL_ENDPOINT, compressionStreamId, compressionMode);
    }

    /**
     * Native: CServerApp::ConfigurePacketCompression @0050104D on g_CServerApp_remote.
     * Fully ported.
     */
    public static void configureRemotePacketCompression(int compressionStreamId, int compressionMode) throws IOException {
        configurePacketCompression(REMOTE_ENDPOINT, compressionStreamId, compressionMode);
    }

    /**
     * Native: CServerApp::SavePacketCompressionTables @005011AA on g_CServerApp_local.
     * Fully ported.
     */
    public static boolean saveLocalPacketCompressionTables() throws IOException {
        return savePacketCompressionTables(LOCAL_ENDPOINT);
    }

    /**
     * Native: CServerApp::SavePacketCompressionTables @005011AA on g_CServerApp_remote.
     * Fully ported.
     */
    public static boolean saveRemotePacketCompressionTables() throws IOException {
        return savePacketCompressionTables(REMOTE_ENDPOINT);
    }

    /**
     * Native: CServerApp::GetPacketCompressionRatioPercent @005012C9 on g_CServerApp_local.
     * Fully ported.
     */
    public static int getLocalPacketCompressionRatioPercent(int compressionStreamId) {
        return getPacketCompressionRatioPercent(LOCAL_ENDPOINT, compressionStreamId);
    }

    /**
     * Native: CServerApp::GetPacketCompressionRatioPercent @005012C9 on g_CServerApp_remote.
     * Fully ported.
     */
    public static int getRemotePacketCompressionRatioPercent(int compressionStreamId) {
        return getPacketCompressionRatioPercent(REMOTE_ENDPOINT, compressionStreamId);
    }

    /**
     * Native: CServerApp::PackNetBufferPayload @00501347 on g_CServerApp_local.
     * Fully ported.
     */
    public static int packLocalNetBufferPayload(
            int compressionStreamId,
            byte[] source,
            int sourceSize,
            int[] packedTarget
    ) {
        return packNetBufferPayload(LOCAL_ENDPOINT, compressionStreamId, source, sourceSize, packedTarget);
    }

    /**
     * Native: CServerApp::PackNetBufferPayload @00501347 on g_CServerApp_remote.
     * Fully ported.
     */
    public static int packRemoteNetBufferPayload(
            int compressionStreamId,
            byte[] source,
            int sourceSize,
            int[] packedTarget
    ) {
        return packNetBufferPayload(REMOTE_ENDPOINT, compressionStreamId, source, sourceSize, packedTarget);
    }

    /**
     * Native: CServerApp::UnpackNetBufferPayload @0050148A on g_CServerApp_local.
     * Fully ported.
     */
    public static int unpackLocalNetBufferPayload(
            int compressionStreamId,
            int[] packedSource,
            int bitCount,
            byte[] target,
            int targetCapacity
    ) {
        return unpackNetBufferPayload(LOCAL_ENDPOINT, compressionStreamId, packedSource, bitCount, target, targetCapacity);
    }

    /**
     * Native: CServerApp::UnpackNetBufferPayload @0050148A on g_CServerApp_remote.
     * Fully ported.
     */
    public static int unpackRemoteNetBufferPayload(
            int compressionStreamId,
            int[] packedSource,
            int bitCount,
            byte[] target,
            int targetCapacity
    ) {
        return unpackNetBufferPayload(REMOTE_ENDPOINT, compressionStreamId, packedSource, bitCount, target, targetCapacity);
    }

    /**
     * Native: CServerApp::SendGameAction @0050164F on g_CServerApp_local.
     * Fully ported.
     */
    public static void sendGameAction(CGameAction action) {
        LOCAL_ENDPOINT.SendGameAction(action);
    }

    /**
     * Native: CServerApp::SendGameAction @0050164F on g_CServerApp_remote.
     * Fully ported.
     */
    public static void sendClientGameAction(CGameAction action) {
        REMOTE_ENDPOINT.SendGameAction(action);
    }

    /**
     * Native: CServerApp::FlushActiveClientWriteBuffers @005017E6 on g_CServerApp_local.
     * Fully ported.
     */
    public static void flushActiveClientWriteBuffers() {
        LOCAL_ENDPOINT.FlushActiveClientWriteBuffers();
    }

    /**
     * Java endpoint model for native g_CServerApp_local and g_CServerApp_remote.
     */
    private static final class Endpoint {
        //0x04
        private Class<?> networkDriver;
        //0x08
        private Endpoint pairedServerApp;
        //0x90
        private final ByteHuffmanPacker packer1 = new ByteHuffmanPacker();
        //0xC94
        private final ByteHuffmanPacker packer2 = new ByteHuffmanPacker();
        //0x44
        private final Deque<NetBuffer> netBufferPool = new ArrayDeque<>();
        //0x1898
        private int stream1SourceBytes;
        //0x189C
        private int stream1PackedBytes;
        //0x18A0
        private int stream2SourceBytes;
        //0x18A4
        private int stream2PackedBytes;
        //0x18A8
        private int defaultCompressionStreamId = 1;
        //0x18AC
        private int packetCompressionMode;
        //0x18B0
        private final boolean local;
        //0x18B4
        private CBufferManager pairedClient;
        // Java support for native CServerApp::clientListsLock guarding queued and active client lists.
        private final Object clientListsLock = new Object();
        //0x28
        private final List<CBufferManager> queuedClientActivations = new ArrayList<>();
        //0x0C
        private final List<CBufferManager> queuedClientRemovals = new ArrayList<>();
        //0x18B8
        private final List<CBufferManager> activeClients = new ArrayList<>();
        //0x18D4
        private final Map<Integer, ClientTrafficStats> clientTrafficStatsByIp = new HashMap<>();
        //0x18F0
        private final int[] packetCountByActionId = new int[256];
        //0x1CF0
        private final int[] packetBytesByActionId = new int[256];

        /**
         * Native support extracted from CServerApp::CServerApp @0050077A endpoint field initialization.
         */
        private Endpoint(boolean local) {
            this.local = local;
        }

        /**
         * Native support extracted from CServerApp::AcquireNetBuffer @00500D86 and
         * CServerApp::ReleaseNetBuffer @00500EAA remote-peer pool selection.
         */
        private Endpoint netBufferPoolOwner() {
            return pairedServerApp != null && !local ? pairedServerApp : this;
        }

        /**
         * Native support extracted from direct client net-id assignment @004F0CBE.
         */
        private void bindPairedClientNetId(int netId) {
            if (pairedClient != null) {
                pairedClient.SetNetId(netId);
            }
        }

        /**
         * Native support extracted from CServerApp::PackNetBufferPayload @00501347 and
         * CServerApp::UnpackNetBufferPayload @0050148A.
         */
        private ByteHuffmanPacker getCompressionPacker(int compressionStreamId) {
            if (compressionStreamId == 1) {
                return packer1;
            }
            if (compressionStreamId == 2) {
                return packer2;
            }
            return null;
        }

        /**
         * Native support extracted from CServerApp::NewClient @00500F0E.
         */
        private void queueClientActivation(CBufferManager client) {
            synchronized (clientListsLock) {
                queuedClientActivations.add(client);
            }
        }

        /**
         * Native support extracted from CServerApp::QueueClientRemoval @00500FD2.
         */
        private void queueClientRemoval(CBufferManager client) {
            synchronized (clientListsLock) {
                queuedClientRemovals.add(client);
            }
        }

        /**
         * Native support extracted from CServerApp::SendGameAction @0050164F,
         * CServerApp::GetClientByNetId @0050155F, CServerApp::GetClientByMaskedSocketId @005015E2,
         * CServerApp::ReadNextGameAction @00501831, CServerApp::GetPendingSegmentMarkerCount @00501913, and
         * CServerApp::FlushActiveClientWriteBuffers @005017E6.
         */
        private List<CBufferManager> activeClientSnapshot() {
            synchronized (clientListsLock) {
                return new ArrayList<>(activeClients);
            }
        }

        /**
         * Native support extracted from CServerApp action send @0050164F.
         * Fully ported.
         */
        private void SendGameAction(CGameAction action) {
            int sendStartTick = Globals.currentTickMillis();
            if (action.playerID.get() == 0) {
                for (CBufferManager client : activeClientSnapshot()) {
                    if (shouldWriteBroadcastAction(client)) {
                        writeActionToClient(action, client);
                    }
                }
            } else {
                CBufferManager target = GetClientByNetId(action.playerID.get());
                if (target != null) {
                    writeActionToClient(action, target);
                }
            }
            if (local && action.ID.get() == GameActionId.ITEM_LIST_ACTION_76.id) {
                itemListActionSendElapsedTicks += Globals.currentTickMillis() - sendStartTick;
            }
            if (!local && action.ID.get() != GameActionId.NEW_SEGMENT_ACTION_64.id) {
                broadcastServerLoopCounter(1);
            }
        }

        /**
         * Native support extracted from CServerApp action-send broadcast branch @0050164F.
         * Fully ported.
         */
        private boolean shouldWriteBroadcastAction(CBufferManager client) {
            return client != null && (!local || client.GetNetId() != 0);
        }

        /**
         * Native support extracted from CServerApp action-send payload-write branch @0050164F.
         * Fully ported.
         * Native only appends the packet and increments the segment marker for action 0x64; flushing is owned by
         * CServerApp::broadcastServerLoopCounter @00504BB7 and explicit FlushActiveClientWriteBuffers call sites.
         */
        private void writeActionToClient(CGameAction action, CBufferManager client) {
            client.WriteGameAction(action);
            if (local) {
                int actionSizeBytes = action.GetPayloadSize();
                CServerApp.recordClientActionBytesByIp(this, client, actionSizeBytes);
                CServerApp.recordPacketStats(this, action);
            }
            if (action.ID.get() == GameActionId.NEW_SEGMENT_ACTION_64.id) {
                client.IncrementCurrentWriteBufferSegmentMarker();
            }
        }

        /**
         * Native support extracted from CServerApp::GetClientByNetId @0050155F.
         * Fully ported.
         */
        private CBufferManager GetClientByNetId(int playerId) {
            for (CBufferManager client : activeClientSnapshot()) {
                if (client.GetNetId() == (playerId & 0xFFFF)) {
                    return client;
                }
            }
            if (!local && pairedClient != null) {
                return pairedClient;
            }
            return null;
        }

        /**
         * Native support extracted from CServerApp::GetClientByMaskedSocketId @005015E2.
         * Fully ported.
         */
        private CBufferManager GetClientByMaskedSocketId(int maskedSocketId) {
            int maskedId = maskedSocketId & CLIENT_SOCKET_ID_MASK;
            for (CBufferManager client : activeClientSnapshot()) {
                if ((client.GetIPAddress() & CLIENT_SOCKET_ID_MASK) == maskedId) {
                    return client;
                }
            }
            return null;
        }

        /**
         * Native support boundary for CLlDriver::PumpDirectPlaySends @00508618 from
         * CServerApp::ProcessNetworkEvents @0050101E. not ported.
         */
        private void pumpDirectPlaySendsBoundary() {
            // DirectPlay send queue pumping is outside the current Java transport model.
        }

        /**
         * Native support extracted from CServerApp next-action drain @00501831.
         * Fully ported.
         */
        private CGameAction ReadNextGameAction() {
            for (CBufferManager client : activeClientSnapshot()) {
                if (client != null) {
                    CGameAction action = ReadGameActionFromClient(client);
                    if (action != null) {
                        recordReceivedActionStats(client, action);
                        return action;
                    }
                }
            }
            return null;
        }

        /**
         * Native support extracted from CServerApp::ReadNextGameAction @00501831 receive accounting branch.
         * Fully ported.
         */
        private void recordReceivedActionStats(CBufferManager client, CGameAction action) {
            if (!local) {
                int actionSizeBytes = action.GetPayloadSize();
                CServerApp.recordClientActionBytesByIp(this, client, actionSizeBytes);
                CServerApp.recordPacketStats(this, action);
            }
        }

        /**
         * Native support extracted from CServerApp::GetNextClient_ @0041EF50 active-client list access.
         */
        private CBufferManager getNextClient() {
            synchronized (clientListsLock) {
                return activeClients.isEmpty() ? null : activeClients.getFirst();
            }
        }

        /**
         * Native support extracted from CServerApp client action decode boundary @005018CD.
         * Fully ported.
         */
        private CGameAction ReadGameActionFromClient(CBufferManager client) {
            if (client.GetPendingSegmentMarkerCount() == 0) {
                return null;
            }
            byte[] actionIdBytes = new byte[1];
            if (!client.Read(actionIdBytes, 0, actionIdBytes.length)) {
                throw new IllegalStateException(
                        "CServerApp::ReadGameActionFromClient @005018CD could not read pending action id"
                );
            }
            return decodeIncomingGameAction(actionIdBytes[0] & 0xFF, client);
        }

        /**
         * Native support extracted from CServerApp::decodeIncomingGameAction @005056F1.
         * Fully ported.
         */
        private CGameAction decodeIncomingGameAction(int actionId, CBufferManager client) {
            if (!IncomingGameActionFactory.isKnownIncomingAction(actionId)) {
                pushUnknownIncomingActionMessage(actionId);
            }
            CGameAction action = IncomingGameActionFactory.readIncomingAction(actionId, client);
            if (actionId == GameActionId.NEW_SEGMENT_ACTION_64.id) {
                client.ConsumeFirstPendingSegmentMarker();
            }
            action.finalizeIncomingHeader(actionId, client, local);
            return action;
        }

        /**
         * Native support extracted from CServerApp::decodeIncomingGameAction @00505A71 and PushMessage @0043A0A8.
         */
        private void pushUnknownIncomingActionMessage(int actionId) {
            Throwable e = new Throwable();
            e.printStackTrace();
            System.out.printf("Drv: unknown cmd type {%d}!\n", actionId);
        }

        /**
         * Native support extracted from CServerApp pending segment-marker count @00501913.
         * Fully ported.
         */
        private int GetPendingSegmentMarkerCount() {
            int count = 0;
            for (CBufferManager client : activeClientSnapshot()) {
                if (client != null) {
                    count += client.GetPendingSegmentMarkerCount();
                }
            }
            return count;
        }

        /**
         * Native support extracted from CServerApp active-client write-buffer flush @005017E6.
         * Fully ported.
         */
        private void FlushActiveClientWriteBuffers() {
            for (CBufferManager client : activeClientSnapshot()) {
                if (client != null) {
                    client.FlushWriteBuffer();
                }
            }
        }

        /**
         * Native support extracted from CServerApp server-loop counter broadcast @00504BB7.
         */
        private void broadcastServerLoopCounter(int serverLoopCounter) {
            NewSegmentAction action = NewSegmentAction.prepareForBroadcastServerLoopCounter(serverLoopCounter);
            SendGameAction(action);
            FlushActiveClientWriteBuffers();
        }
    }
}
