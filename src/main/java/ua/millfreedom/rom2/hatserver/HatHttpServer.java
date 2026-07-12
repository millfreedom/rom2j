package ua.millfreedom.rom2.hatserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ua.millfreedom.rom2.GameCharsets;
import ua.millfreedom.rom2.ServerConfigurationLoader;
import ua.millfreedom.rom2.model.ServerConfig;
import ua.millfreedom.rom2.model.net.CLlDriver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * Main-project console HAT-compatible HTTP endpoint for local ROM2 TCP/IP discovery.
 * not ported.
 */
public final class HatHttpServer {
    private static final int DEFAULT_PORT = 6666;
    private static final Charset HAT_CHARSET = GameCharsets.GAME_TEXT;
    private static final String DEFAULT_BIND_ADDRESS = "0.0.0.0";
    private static final String LOOPBACK_CONNECT_ADDRESS = "127.0.0.1";
    private static final String RESPONSE_CONTENT_TYPE = "text/plain; charset=windows-1251";
    private static final String NO_MAP_NAME = "no map";
    public static final String MAP_EXTENSION = ".alm";
    private static final String NO_MAP_SIZE = "0x0";
    private static final String NO_MAP_DIFFICULTY_LEVEL = "9";
    private static final String NO_MAP_PLAYER_COUNT = "16";
    private static final String CONFIGURED_MAP_SIZE_PLACEHOLDER = "0x0";
    private static final String CONFIGURED_MAP_DIFFICULTY_LEVEL = "1";
    private static final String CONFIGURED_MAP_PLAYER_COUNT = "0";
    private static final int HTTP_OK = 200;
    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_NOT_FOUND = 404;
    private static final int HTTP_METHOD_NOT_ALLOWED = 405;
    private static final long TRANSFER_TICKET_TTL_MILLIS = 60_000L;

    private final Map<String, ServerEntry> reportedServers = new ConcurrentHashMap<>();
    private final Map<String, TransferTicket> transferTickets = new ConcurrentHashMap<>();

    /**
     * not ported.
     */
    public static void main(String[] args) {
        int exitCode = runFromArgs(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    /**
     * Java support process entry point for command-line parsing and startup failure reporting.
     * not ported.
     */
    public static int runFromArgs(String[] args) {
        try {
            Options options = Options.parse(args);
            if (options.helpRequested()) {
                System.out.println(usage());
                return 0;
            }
//            ServerConfigurationLoader.loadServerConfig(
//                    options.configPath(),
//                    options.explicitConfig(),
//                    "No " + ServerConfigurationLoader.DEFAULT_CONFIG_PATH + " found; using HAT server defaults."
//            );
            start(options);
            return 0;
        } catch (IllegalArgumentException exception) {
            System.err.println(exception.getMessage());
            System.err.println(usage());
            return 64;
        } catch (RuntimeException | IOException exception) {
            exception.printStackTrace(System.err);
            return 1;
        }
    }

    /**
     * Java support boundary for binding the JDK HTTP server after server.cfg has populated Globals.serverConfig.
     * not ported.
     */
    public static HttpServer start(Options options) throws IOException {
        HatHttpServer application = new HatHttpServer();
        HttpServer server = HttpServer.create(
                new InetSocketAddress(options.bindAddressOrDefault(), options.port()),
                0
        );
        server.createContext("/", application::handleRequest);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.printf(
                Locale.ROOT,
                "HAT HTTP server listening on http://%s:%d/%n",
                options.displayBindAddress(),
                options.port()
        );
        return server;
    }

    /**
     * Java support HTTP request dispatcher for HAT list reads and server-status reports.
     * not ported.
     */
    private void handleRequest(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendText(exchange, HTTP_METHOD_NOT_ALLOWED, "Only GET is supported.\n");
                return;
            }

            Map<String, String> parameters = parseQuery(exchange.getRequestURI().getRawQuery());
            String path = exchange.getRequestURI().getPath();
            if (path.startsWith("/map-transfer")) {
                handleMapTransferRequest(exchange, path, parameters);
                return;
            }

            if (isServerStatusReport(parameters)) {
                ServerEntry serverEntry = ServerEntry.fromStatusReport(parameters, remoteHost(exchange));
                reportedServers.put(serverEntry.key(), serverEntry);
                System.out.printf(Locale.ROOT, "HAT status update: %s%n", serverEntry.toRow());
                sendText(exchange, HTTP_OK, "OK\n");
                return;
            }

            sendText(exchange, HTTP_OK, buildServerListResponse());
        } catch (IllegalArgumentException exception) {
            sendText(exchange, HTTP_BAD_REQUEST, exception.getMessage() + "\n");
        }
    }

    /**
     * Java support dispatcher for dedicated map-transfer HAT endpoints.
     * not ported.
     */
    private void handleMapTransferRequest(
            HttpExchange exchange,
            String path,
            Map<String, String> parameters
    ) throws IOException {
        purgeExpiredTransferTickets();
        switch (path) {
            case "/map-transfer", "/map-transfer/servers" -> handleTransferServersRequest(exchange, parameters);
            case "/map-transfer/prepare" -> handlePrepareTransferRequest(exchange, parameters);
            case "/map-transfer/redirected" -> handleMarkTransferRedirectedRequest(exchange, parameters);
            case "/map-transfer/claim" -> handleClaimTransferTicketRequest(exchange, parameters);
            case "/map-transfer/commit" -> handleCommitTransferTicketRequest(exchange, parameters);
            case "/map-transfer/cancel" -> handleCancelTransferTicketRequest(exchange, parameters);
            case "/map-transfer/ticket" -> handleTransferTicketStatusRequest(exchange, parameters);
            default -> sendText(exchange, HTTP_NOT_FOUND, "Unknown map-transfer endpoint.\n");
        }
    }

    /**
     * Java support response for the current dedicated map-server registry.
     * not ported.
     */
    private void handleTransferServersRequest(
            HttpExchange exchange,
            Map<String, String> parameters
    ) throws IOException {
        String mapName = parameters.get("mapname");
        List<ServerEntry> rows = transferRegistryRows(mapName);
        StringBuilder response = new StringBuilder(96 + rows.size() * 96);
        response.append("OK|COUNT|").append(rows.size()).append('\n');
        response.append("SERVER|NAME|MAP|HOST|GAMEPORT|DISCOVERYPORT|PLAYERS|ADDRESS\n");
        for (ServerEntry row : rows) {
            response.append(row.toTransferRegistryRow()).append('\n');
        }
        sendText(exchange, HTTP_OK, response.toString());
    }

    /**
     * Java support endpoint for creating a short-lived transfer ticket after the source server saved a payload.
     * not ported.
     */
    private void handlePrepareTransferRequest(
            HttpExchange exchange,
            Map<String, String> parameters
    ) throws IOException {
        String targetMapName = requireParameter(parameters, "tomapname");
        int destinationX = parseRequiredCell(parameters, "tox");
        int destinationY = parseRequiredCell(parameters, "toy");
        ServerEntry targetServer = findTargetServerForMap(targetMapName);
        if (targetServer == null) {
            sendText(exchange, HTTP_NOT_FOUND, "NO_TARGET\n");
            return;
        }

        long nowMillis = System.currentTimeMillis();
        TransferTicket ticket = TransferTicket.prepare(
                UUID.randomUUID().toString(),
                requireParameter(parameters, "sourceserver"),
                requireParameter(parameters, "sourceplayer"),
                parameters.getOrDefault("sourcemap", ""),
                targetServer,
                destinationX,
                destinationY,
                requireParameter(parameters, "payloadref"),
                nowMillis,
                nowMillis + TRANSFER_TICKET_TTL_MILLIS
        );
        transferTickets.put(ticket.token(), ticket);
        sendText(exchange, HTTP_OK, ticket.prepareResponse());
    }

    /**
     * Java support endpoint marking that the source server sent the client redirect packet.
     * not ported.
     */
    private void handleMarkTransferRedirectedRequest(
            HttpExchange exchange,
            Map<String, String> parameters
    ) throws IOException {
        TransferTicket ticket = requireTicket(parameters);
        ticket.markRedirected();
        sendText(exchange, HTTP_OK, ticket.statusResponse());
    }

    /**
     * Java support endpoint for target-side single-use ticket claim before loading a saved transfer payload.
     * not ported.
     */
    private void handleClaimTransferTicketRequest(
            HttpExchange exchange,
            Map<String, String> parameters
    ) throws IOException {
        TransferTicket ticket = requireTicket(parameters);
        ticket.verifyOptionalTargetAddress(parameters.get("targetaddress"));
        ticket.claim();
        sendText(exchange, HTTP_OK, ticket.claimResponse());
    }

    /**
     * Java support endpoint for target-side ticket commit after player load/spawn/bootstrap succeeds.
     * not ported.
     */
    private void handleCommitTransferTicketRequest(
            HttpExchange exchange,
            Map<String, String> parameters
    ) throws IOException {
        TransferTicket ticket = requireTicket(parameters);
        ticket.commit();
        sendText(exchange, HTTP_OK, ticket.statusResponse());
    }

    /**
     * Java support endpoint for source/target rollback or timeout cancellation.
     * not ported.
     */
    private void handleCancelTransferTicketRequest(
            HttpExchange exchange,
            Map<String, String> parameters
    ) throws IOException {
        TransferTicket ticket = requireTicket(parameters);
        ticket.cancel();
        sendText(exchange, HTTP_OK, ticket.statusResponse());
    }

    /**
     * Java support endpoint for manual probes and later source rollback checks.
     * not ported.
     */
    private void handleTransferTicketStatusRequest(
            HttpExchange exchange,
            Map<String, String> parameters
    ) throws IOException {
        TransferTicket ticket = requireTicket(parameters);
        sendText(exchange, HTTP_OK, ticket.detailResponse());
    }

    /**
     * Java support ticket lookup with consistent missing/expired semantics.
     * not ported.
     */
    private TransferTicket requireTicket(Map<String, String> parameters) {
        String token = requireParameter(parameters, "token");
        TransferTicket ticket = transferTickets.get(token);
        if (ticket == null) {
            throw new IllegalArgumentException("Unknown transfer token.");
        }
        ticket.failIfExpired(System.currentTimeMillis());
        return ticket;
    }

    /**
     * Java support current dedicated map-server row selection for transfer lookup.
     * not ported.
     */
    private List<ServerEntry> transferRegistryRows(String mapName) {
        List<ServerEntry> rows = new ArrayList<>();
        for (ServerEntry serverEntry : reportedServers.values()) {
            if (serverEntry.isTransferRegistryCandidate(mapName)) {
                rows.add(serverEntry);
            }
        }
        rows.sort(Comparator.comparing(ServerEntry::transferMapName)
                .thenComparing(ServerEntry::serverName)
                .thenComparing(ServerEntry::connectAddress));
        return rows;
    }

    /**
     * Java support target server lookup for `prepare` requests.
     * not ported.
     */
    private ServerEntry findTargetServerForMap(String mapName) {
        List<ServerEntry> candidates = transferRegistryRows(mapName);
        return candidates.isEmpty() ? null : candidates.getFirst();
    }

    /**
     * Java support periodic ticket expiry cleanup piggybacked on transfer HTTP requests.
     * not ported.
     */
    private void purgeExpiredTransferTickets() {
        long nowMillis = System.currentTimeMillis();
        transferTickets.values().removeIf(ticket -> ticket.expireIfTimedOut(nowMillis));
    }

    /**
     * Java support required query parameter reader.
     * not ported.
     */
    private static String requireParameter(Map<String, String> parameters, String name) {
        String value = parameters.get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required parameter: " + name);
        }
        return value.strip();
    }

    /**
     * Java support non-negative map-cell parser for transfer ticket destination coordinates.
     * not ported.
     */
    private static int parseRequiredCell(Map<String, String> parameters, String name) {
        String value = requireParameter(parameters, name);
        int parsed;
        try {
            parsed = Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid map cell parameter: " + name);
        }
        if (parsed < 0) {
            throw new IllegalArgumentException("Invalid map cell parameter: " + name);
        }
        return parsed;
    }

    /**
     * Java support remote-address reader for status reports that omit the native `ip` parameter.
     * not ported.
     */
    private static String remoteHost(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    /**
     * Java support predicate for the recovered GameServer status-report query shape.
     * not ported.
     */
    private static boolean isServerStatusReport(Map<String, String> parameters) {
        return parameters.containsKey("servername")
                || parameters.containsKey("mapname")
                || parameters.containsKey("mapsize")
                || parameters.containsKey("difficultylevel")
                || parameters.containsKey("players")
                || parameters.containsKey("ip");
    }

    /**
     * Java support response formatter for the native HAT CURRENTCOUNT parser.
     * not ported.
     */
    private String buildServerListResponse() {
        List<ServerEntry> rows = currentServerRows();
        StringBuilder response = new StringBuilder(128 + rows.size() * 96);
        response.append("CURRENTCOUNT|").append(rows.size()).append('\n');
        response.append("SERVER|VERSION|MAP|SIZE|LEVEL|PLAYERS|ADDRESS\n");
        response.append("------------------------------------------------\n");
        for (ServerEntry row : rows) {
            response.append(row.toRow()).append('\n');
        }
        return response.toString();
    }

    /**
     * Java support row selection for current reported servers or the server.cfg-derived fallback row.
     * not ported.
     */
    private List<ServerEntry> currentServerRows() {
        if (reportedServers.isEmpty()) {
            return List.of();
        }
        List<ServerEntry> rows = new ArrayList<>();
        for (ServerEntry serverEntry : reportedServers.values()) {
            if (serverEntry.isPublicServerListCandidate()) {
                rows.add(serverEntry);
            }
        }
        rows.sort(Comparator.comparing(ServerEntry::serverName).thenComparing(ServerEntry::connectAddress));
        return rows;
    }

    /**
     * Java support URL query parser for JDK HttpServer request URIs.
     * not ported.
     */
    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> parameters = new LinkedHashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return parameters;
        }
        String[] pairs = rawQuery.split("&");
        for (String pair : pairs) {
            int equalsIndex = pair.indexOf('=');
            String rawKey = equalsIndex < 0 ? pair : pair.substring(0, equalsIndex);
            String rawValue = equalsIndex < 0 ? "" : pair.substring(equalsIndex + 1);
            String key = decodeQueryComponent(rawKey).toLowerCase(Locale.ROOT);
            if (!key.isBlank()) {
                parameters.put(key, decodeQueryComponent(rawValue));
            }
        }
        return parameters;
    }

    /**
     * Java support query decoder using the native HAT byte encoding.
     * not ported.
     */
    private static String decodeQueryComponent(String rawValue) {
        return URLDecoder.decode(rawValue, HAT_CHARSET);
    }

    /**
     * Java support text response writer for the native HAT byte encoding.
     * not ported.
     */
    private static void sendText(HttpExchange exchange, int status, String body) throws IOException {
        byte[] responseBytes = body.getBytes(HAT_CHARSET);
        exchange.getResponseHeaders().set("Content-Type", RESPONSE_CONTENT_TYPE);
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        exchange.sendResponseHeaders(status, responseBytes.length);
        try (OutputStream response = exchange.getResponseBody()) {
            response.write(responseBytes);
        }
    }

    /**
     * Java support usage text for the main-project HAT helper.
     * not ported.
     */
    private static String usage() {
        return "Usage: HatHttpServer [--config " + ServerConfigurationLoader.DEFAULT_CONFIG_PATH + "] "
                + "[--bind 0.0.0.0] [--port 6666]";
    }

    /**
     * Immutable row for the native HAT server-list parser.
     * not ported.
     */
    private static final class ServerEntry {
        private final String serverName;
        private final String version;
        private final String mapName;
        private final String transferMapName;
        private final String mapSize;
        private final String difficultyLevel;
        private final String players;
        private final String connectAddress;
        private final int discoveryPort;
        private final boolean dedicatedMapTransferEnabled;
        private final boolean notPublic;

        /**
         * not ported.
         */
        private ServerEntry(
                String serverName,
                String version,
                String mapName,
                String transferMapName,
                String mapSize,
                String difficultyLevel,
                String players,
                String connectAddress,
                int discoveryPort,
                boolean dedicatedMapTransferEnabled,
                boolean notPublic
        ) {
            this.serverName = sanitizeField(serverName);
            this.version = sanitizeField(version);
            this.mapName = sanitizeField(mapName);
            this.transferMapName = sanitizeField(defaultIfBlank(transferMapName, mapName));
            this.mapSize = sanitizeField(mapSize);
            this.difficultyLevel = sanitizeField(difficultyLevel);
            this.players = sanitizeField(players);
            this.connectAddress = sanitizeConnectAddress(connectAddress);
            this.discoveryPort = ServerConfig.isValidTcpPort(discoveryPort) ? discoveryPort : 0;
            this.dedicatedMapTransferEnabled = dedicatedMapTransferEnabled;
            this.notPublic = notPublic;
        }


        /**
         * Java support conversion from GameServer::reportServerStatusToConfiguredTargets @004ED0B1 query parameters.
         * not ported.
         */
        private static ServerEntry fromStatusReport(
                Map<String, String> parameters,
                String remoteHost
        ) {
            String rawMapName = parameters.get("mapname");
            String rawPlayers = parameters.get("players");
            String rowPlayers = isUnavailableMap(rawMapName, rawPlayers) ? NO_MAP_PLAYER_COUNT : rawPlayers;
            String reportedAddress = parameters.get("ip");
            if (reportedAddress == null || reportedAddress.isBlank()) {
                reportedAddress = remoteHost;
            }
            return new ServerEntry(
                    parameters.get("servername"),
                    parameters.get("version"),
                    rawMapName,
                    parameters.get("mapfile"),
                    parameters.get("mapsize"),
                    parameters.get("difficultylevel"),
                    rowPlayers,
                    reportedAddress,
                    parseLeadingInt(parameters.get("discoveryport")),
                    isDedicatedMapTransferReport(parameters),
                    isAffirmative(parameters.get("notpublic"))
            );
        }

        /**
         * not ported.
         */
        private String key() {
            return connectAddress + "|" + serverName;
        }

        /**
         * not ported.
         */
        private String serverName() {
            return serverName;
        }

        /**
         * not ported.
         */
        private String mapName() {
            return mapName;
        }

        /**
         * not ported.
         */
        private String transferMapName() {
            return transferMapName;
        }

        /**
         * not ported.
         */
        private String connectAddress() {
            return connectAddress;
        }

        /**
         * Java support transfer-registry predicate for dedicated server rows running a current map.
         * not ported.
         */
        private boolean isTransferRegistryCandidate(String requestedMapName) {
            if (!dedicatedMapTransferEnabled || isUnavailableMap(mapName, players) || gamePort() <= 0) {
                return false;
            }
            return requestedMapName == null || requestedMapName.isBlank()
                    || transferMapName.equalsIgnoreCase(requestedMapName.strip())
                    || transferMapName.equalsIgnoreCase(requestedMapName.strip() + MAP_EXTENSION)
                    ;
        }

        /**
         * Java support predicate for the public HAT CURRENTCOUNT response.
         * not ported.
         */
        private boolean isPublicServerListCandidate() {
            return !notPublic;
        }

        /**
         * Java support pipe-row formatter for HatServerBrowserDialogVisualObject.refreshHatServerRows(...).
         * The trailing empty field preserves the delimiter after the native address token consumed at @0044AB6C.
         * not ported.
         */
        private String toRow() {
            return String.join(
                    "|",
                    serverName,
                    version,
                    mapName,
                    mapSize,
                    difficultyLevel,
                    players,
                    connectAddress,
                    ""
            );
        }

        /**
         * Java support pipe-row formatter for dedicated map-transfer registry probes.
         * not ported.
         */
        private String toTransferRegistryRow() {
            return String.join(
                    "|",
                    "SERVER",
                    serverName,
                    transferMapName,
                    host(),
                    Integer.toString(gamePort()),
                    Integer.toString(discoveryPort()),
                    players,
                    connectAddress
            );
        }

        /**
         * Java support endpoint-host accessor for transfer redirect targets.
         * not ported.
         */
        private String host() {
            return ServerConfig.tcpEndpointHostOrDefault(connectAddress, "");
        }

        /**
         * Java support endpoint-port accessor for transfer redirect targets.
         * not ported.
         */
        private int gamePort() {
            try {
                return ServerConfig.tcpEndpointPortOrDefault(connectAddress, 0);
            } catch (IllegalArgumentException exception) {
                return 0;
            }
        }

        /**
         * Java support discovery-port accessor for transfer registry diagnostics. New dedicated reports include the
         * configured discovery port; legacy rows fall back to the native default relation from the advertised game port.
         * not ported.
         */
        private int discoveryPort() {
            if (discoveryPort > 0) {
                return discoveryPort;
            }
            int gamePort = gamePort();
            return gamePort == 0 ? 0 : CLlDriver.defaultDiscoveryPortForGamePort(gamePort);
        }

        /**
         * Java support read of the configured map selected by g_ServerConfig.field15_0x8c.
         * not ported.
         */
        private static String configuredMapName(ServerConfig serverConfig) {
            if (serverConfig.maps.isEmpty()) {
                return null;
            }
            int mapIndex = serverConfig.field15_0x8c;
            if (mapIndex < 0 || mapIndex >= serverConfig.maps.size()) {
                mapIndex = 0;
            }
            return serverConfig.maps.get(mapIndex);
        }

        /**
         * Java support HAT unavailable-map detection matching the no-map status report shape.
         * not ported.
         */
        private static boolean isUnavailableMap(String mapName, String players) {
            return NO_MAP_NAME.equalsIgnoreCase(Objects.requireNonNullElse(mapName, "").strip())
                    || parseLeadingInt(players) < 0;
        }

        /**
         * Java support predicate for status reports eligible for map transfer.
         * not ported.
         */
        private static boolean isDedicatedMapTransferReport(Map<String, String> parameters) {
            return isAffirmative(parameters.get("dedicated")) || isAffirmative(parameters.get("maptransfer"));
        }

        /**
         * Java support boolean query parser for HAT transfer flags.
         * not ported.
         */
        private static boolean isAffirmative(String value) {
            return "1".equals(value)
                    || "true".equalsIgnoreCase(Objects.requireNonNullElse(value, ""))
                    || "yes".equalsIgnoreCase(Objects.requireNonNullElse(value, ""));
        }

        /**
         * Java support leading integer parser for HAT status `players`.
         * not ported.
         */
        private static int parseLeadingInt(String value) {
            String trimmed = Objects.requireNonNullElse(value, "").stripLeading();
            if (trimmed.isEmpty()) {
                return 0;
            }
            int end = 0;
            if (trimmed.charAt(0) == '-' || trimmed.charAt(0) == '+') {
                end++;
            }
            while (end < trimmed.length() && Character.isDigit(trimmed.charAt(end))) {
                end++;
            }
            if (end == 0 || (end == 1 && (trimmed.charAt(0) == '-' || trimmed.charAt(0) == '+'))) {
                return 0;
            }
            return Integer.parseInt(trimmed.substring(0, end));
        }

        /**
         * Java support pipe/control-character cleanup for one HAT row field.
         * not ported.
         */
        private static String sanitizeField(String value) {
            return Objects.requireNonNullElse(value, "")
                    .replace('|', ' ')
                    .replace('\r', ' ')
                    .replace('\n', ' ')
                    .strip();
        }

        /**
         * Java support endpoint token cleanup for HAT row field 7.
         * not ported.
         */
        private static String sanitizeConnectAddress(String value) {
            String sanitized = sanitizeField(value);
            if (sanitized.startsWith("[") && sanitized.contains("]")) {
                int bracketEnd = sanitized.indexOf(']');
                if (bracketEnd == sanitized.length() - 1) {
                    return sanitized.substring(1, bracketEnd);
                }
            }
            return sanitized;
        }

        /**
         * not ported.
         */
        private static String defaultIfBlank(String value, String fallback) {
            return value == null || value.isBlank() ? fallback : value;
        }
    }

    /**
     * Java support transfer-ticket state values owned by HAT.
     * not ported.
     */
    private enum TransferTicketStatus {
        PREPARED,
        REDIRECTED,
        CLAIMED,
        COMMITTED,
        CANCELLED,
        EXPIRED
    }

    /**
     * Java support short-lived HAT-owned transfer ticket metadata.
     * not ported.
     */
    private static final class TransferTicket {
        private final String token;
        private final String sourceServer;
        private final String sourcePlayer;
        private final String sourceMapName;
        private final String targetServer;
        private final String targetMapName;
        private final String targetAddress;
        private final String targetHost;
        private final int targetGamePort;
        private final int targetDiscoveryPort;
        private final int destinationX;
        private final int destinationY;
        private final String payloadRef;
        private final long createdAtMillis;
        private final long expiresAtMillis;
        private TransferTicketStatus status;

        /**
         * Java support constructor for prepared HAT transfer-ticket metadata.
         * not ported.
         */
        private TransferTicket(
                String token,
                String sourceServer,
                String sourcePlayer,
                String sourceMapName,
                ServerEntry targetServer,
                int destinationX,
                int destinationY,
                String payloadRef,
                long createdAtMillis,
                long expiresAtMillis
        ) {
            this.token = token;
            this.sourceServer = ServerEntry.sanitizeField(sourceServer);
            this.sourcePlayer = ServerEntry.sanitizeField(sourcePlayer);
            this.sourceMapName = ServerEntry.sanitizeField(sourceMapName);
            this.targetServer = targetServer.serverName();
            this.targetMapName = targetServer.transferMapName();
            this.targetAddress = targetServer.connectAddress();
            this.targetHost = targetServer.host();
            this.targetGamePort = targetServer.gamePort();
            this.targetDiscoveryPort = targetServer.discoveryPort();
            this.destinationX = destinationX;
            this.destinationY = destinationY;
            this.payloadRef = ServerEntry.sanitizeField(payloadRef);
            this.createdAtMillis = createdAtMillis;
            this.expiresAtMillis = expiresAtMillis;
            this.status = TransferTicketStatus.PREPARED;
        }

        /**
         * Java support factory for a prepared transfer ticket.
         * not ported.
         */
        private static TransferTicket prepare(
                String token,
                String sourceServer,
                String sourcePlayer,
                String sourceMapName,
                ServerEntry targetServer,
                int destinationX,
                int destinationY,
                String payloadRef,
                long createdAtMillis,
                long expiresAtMillis
        ) {
            return new TransferTicket(
                    token,
                    sourceServer,
                    sourcePlayer,
                    sourceMapName,
                    targetServer,
                    destinationX,
                    destinationY,
                    payloadRef,
                    createdAtMillis,
                    expiresAtMillis
            );
        }

        /**
         * Java support accessor for the opaque ticket token.
         * not ported.
         */
        private String token() {
            return token;
        }

        /**
         * Java support transition after the source server sends the redirect packet.
         * not ported.
         */
        private synchronized void markRedirected() {
            requireStatus(TransferTicketStatus.PREPARED, "redirect");
            status = TransferTicketStatus.REDIRECTED;
        }

        /**
         * Java support transition when the destination server claims the ticket.
         * not ported.
         */
        private synchronized void claim() {
            requireStatus(TransferTicketStatus.REDIRECTED, "claim");
            status = TransferTicketStatus.CLAIMED;
        }

        /**
         * Java support transition after destination-side player load/spawn/bootstrap succeeds.
         * not ported.
         */
        private synchronized void commit() {
            requireStatus(TransferTicketStatus.CLAIMED, "commit");
            status = TransferTicketStatus.COMMITTED;
        }

        /**
         * Java support cancellation transition for source/target rollback.
         * not ported.
         */
        private synchronized void cancel() {
            if (status == TransferTicketStatus.COMMITTED || status == TransferTicketStatus.EXPIRED) {
                throw new IllegalArgumentException("Cannot cancel transfer ticket in status " + status + ".");
            }
            status = TransferTicketStatus.CANCELLED;
        }

        /**
         * Java support target-address guard for optional target-side claim validation.
         * not ported.
         */
        private void verifyOptionalTargetAddress(String requestedTargetAddress) {
            if (requestedTargetAddress != null
                    && !requestedTargetAddress.isBlank()
                    && !targetAddress.equalsIgnoreCase(requestedTargetAddress.strip())) {
                throw new IllegalArgumentException("Transfer token is not assigned to target " + requestedTargetAddress);
            }
        }

        /**
         * Java support expiry check for a requested ticket.
         * not ported.
         */
        private synchronized void failIfExpired(long nowMillis) {
            if (!isTerminalStatus() && nowMillis >= expiresAtMillis) {
                status = TransferTicketStatus.EXPIRED;
                throw new IllegalArgumentException("Transfer ticket expired.");
            }
        }

        /**
         * Java support expiry check used by request-time cleanup.
         * not ported.
         */
        private synchronized boolean expireIfTimedOut(long nowMillis) {
            if (!isTerminalStatus() && nowMillis >= expiresAtMillis) {
                status = TransferTicketStatus.EXPIRED;
                return true;
            }
            return false;
        }

        /**
         * Java support status transition guard.
         * not ported.
         */
        private void requireStatus(TransferTicketStatus expectedStatus, String action) {
            if (status != expectedStatus) {
                throw new IllegalArgumentException("Cannot " + action + " transfer ticket in status " + status + ".");
            }
        }

        /**
         * Java support terminal-state predicate for cleanup and cancellation.
         * not ported.
         */
        private boolean isTerminalStatus() {
            return status == TransferTicketStatus.COMMITTED
                    || status == TransferTicketStatus.CANCELLED
                    || status == TransferTicketStatus.EXPIRED;
        }

        /**
         * Java support response for a successful prepare request.
         * not ported.
         */
        private String prepareResponse() {
            return "OK|TOKEN|" + token
                    + "|TARGET|" + targetAddress
                    + "|HOST|" + targetHost
                    + "|GAMEPORT|" + targetGamePort
                    + "|DISCOVERYPORT|" + targetDiscoveryPort
                    + "|EXPIRES|" + expiresAtMillis
                    + "\n";
        }

        /**
         * Java support response for a successful claim request.
         * not ported.
         */
        private synchronized String claimResponse() {
            return "OK|TOKEN|" + token
                    + "|STATUS|" + status
                    + "|PAYLOADREF|" + payloadRef
                    + "|TOMAP|" + targetMapName
                    + "|TOX|" + destinationX
                    + "|TOY|" + destinationY
                    + "\n";
        }

        /**
         * Java support compact status response for state-changing endpoints.
         * not ported.
         */
        private synchronized String statusResponse() {
            return "OK|TOKEN|" + token + "|STATUS|" + status + "\n";
        }

        /**
         * Java support detailed ticket response for probes and rollback polling.
         * not ported.
         */
        private synchronized String detailResponse() {
            return "OK|TOKEN|" + token
                    + "|STATUS|" + status
                    + "|SOURCESERVER|" + sourceServer
                    + "|SOURCEPLAYER|" + sourcePlayer
                    + "|SOURCEMAP|" + sourceMapName
                    + "|TARGETSERVER|" + targetServer
                    + "|TARGET|" + targetAddress
                    + "|TOMAP|" + targetMapName
                    + "|TOX|" + destinationX
                    + "|TOY|" + destinationY
                    + "|PAYLOADREF|" + payloadRef
                    + "|CREATED|" + createdAtMillis
                    + "|EXPIRES|" + expiresAtMillis
                    + "\n";
        }
    }

    /**
     * Command-line options for the main-project HAT helper.
     * not ported.
     */
    public static final class Options {
        private final String configPath;
        private final boolean explicitConfig;
        private final String bindAddress;
        private final int port;
        private final boolean helpRequested;

        /**
         * not ported.
         */
        private Options(String configPath, boolean explicitConfig, String bindAddress, int port, boolean helpRequested) {
            this.configPath = configPath;
            this.explicitConfig = explicitConfig;
            this.bindAddress = bindAddress;
            this.port = port;
            this.helpRequested = helpRequested;
        }

        /**
         * Java support command-line parser for the main-project HAT helper.
         * not ported.
         */
        private static Options parse(String[] args) {
            String commandLine = String.join(" ", args);
            String configPath = null;
            boolean explicitConfig = false;
            String bindAddress = null;
            int port = DEFAULT_PORT;
            boolean helpRequested = false;

            for (int index = 0; index < args.length; index++) {
                String argument = args[index];
                switch (argument) {
                    case "--help", "-h" -> helpRequested = true;
                    case "--config", "-cfg" -> {
                        configPath = requireValue(args, ++index, argument);
                        explicitConfig = true;
                    }
                    case "--bind", "--ip", "-ip" -> bindAddress = requireValue(args, ++index, argument);
                    case "--port" -> port = parsePort(requireValue(args, ++index, argument));
                    default -> {
                        if (argument.startsWith("--config=")) {
                            configPath = argument.substring("--config=".length());
                            explicitConfig = true;
                        } else if (argument.startsWith("--bind=")) {
                            bindAddress = argument.substring("--bind=".length());
                        } else if (argument.startsWith("--ip=")) {
                            bindAddress = argument.substring("--ip=".length());
                        } else if (argument.startsWith("--port=")) {
                            port = parsePort(argument.substring("--port=".length()));
                        } else {
                            throw new IllegalArgumentException("Unexpected argument: " + argument);
                        }
                    }
                }
            }

            if (configPath == null) {
                String nativeConfigPath = ServerConfigurationLoader.extractNativeQuotedOption(commandLine, "-cfg\"");
                if (nativeConfigPath != null) {
                    configPath = nativeConfigPath;
                    explicitConfig = true;
                }
            }
            if (bindAddress == null) {
                bindAddress = ServerConfigurationLoader.extractNativeQuotedOption(commandLine, "-ip\"");
            }
            return new Options(emptyToNull(configPath), explicitConfig, emptyToNull(bindAddress), port, helpRequested);
        }

        /**
         * Java support accessor for the configured server.cfg path.
         * not ported.
         */
        private String configPath() {
            return configPath;
        }

        /**
         * Java support accessor for explicit-config error semantics.
         * not ported.
         */
        private boolean explicitConfig() {
            return explicitConfig;
        }

        /**
         * Java support accessor for the HAT HTTP listen port.
         * not ported.
         */
        public int port() {
            return port;
        }

        /**
         * Java support accessor for help-mode dispatch.
         * not ported.
         */
        private boolean helpRequested() {
            return helpRequested;
        }

        /**
         * Java support accessor for InetSocketAddress bind input.
         * not ported.
         */
        private String bindAddressOrDefault() {
            return bindAddress == null ? DEFAULT_BIND_ADDRESS : bindAddress;
        }

        /**
         * Java support display normalization for the default wildcard bind address.
         * not ported.
         */
        private String displayBindAddress() {
            String resolvedBindAddress = bindAddressOrDefault();
            return DEFAULT_BIND_ADDRESS.equals(resolvedBindAddress) ? LOOPBACK_CONNECT_ADDRESS : resolvedBindAddress;
        }

        /**
         * Java support argument-value reader for long options.
         * not ported.
         */
        private static String requireValue(String[] args, int index, String option) {
            if (index >= args.length) {
                throw new IllegalArgumentException(option + " requires a value.");
            }
            return args[index];
        }

        /**
         * Java support port parser for the HAT HTTP listen port.
         * not ported.
         */
        private static int parsePort(String value) {
            int parsed = Integer.parseInt(value);
            if (parsed < 1 || parsed > 0xFFFF) {
                throw new IllegalArgumentException("Port out of range: " + value);
            }
            return parsed;
        }

        /**
         * Java support empty-string normalization for optional launcher arguments.
         * not ported.
         */
        private static String emptyToNull(String value) {
            return value == null || value.isEmpty() ? null : value;
        }
    }
}
