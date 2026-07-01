package ua.millfreedom.rom2.hatserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ua.millfreedom.rom2.ServerConfigurationLoader;
import ua.millfreedom.rom2.model.ServerConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * Main-project console HAT-compatible HTTP endpoint for local ROM2 TCP/IP discovery.
 * not ported.
 */
public final class HatHttpServer {
    private static final int DEFAULT_PORT = 6666;
    private static final Charset HAT_CHARSET = StandardCharsets.ISO_8859_1;
    private static final String DEFAULT_BIND_ADDRESS = "0.0.0.0";
    private static final String LOOPBACK_CONNECT_ADDRESS = "127.0.0.1";
    private static final String RESPONSE_CONTENT_TYPE = "text/plain; charset=ISO-8859-1";
    private static final String NO_MAP_NAME = "no map";
    private static final String NO_MAP_SIZE = "0x0";
    private static final String NO_MAP_DIFFICULTY_LEVEL = "9";
    private static final String NO_MAP_PLAYER_COUNT = "16";
    private static final String CONFIGURED_MAP_SIZE_PLACEHOLDER = "0x0";
    private static final String CONFIGURED_MAP_DIFFICULTY_LEVEL = "1";
    private static final String CONFIGURED_MAP_PLAYER_COUNT = "0";
    private static final int HTTP_OK = 200;
    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_METHOD_NOT_ALLOWED = 405;

    private final Map<String, ServerEntry> reportedServers = new ConcurrentHashMap<>();


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
        List<ServerEntry> rows = new ArrayList<>(reportedServers.values());
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
        private final String mapSize;
        private final String difficultyLevel;
        private final String players;
        private final String connectAddress;

        /**
         * not ported.
         */
        private ServerEntry(
                String serverName,
                String version,
                String mapName,
                String mapSize,
                String difficultyLevel,
                String players,
                String connectAddress
        ) {
            this.serverName = sanitizeField(serverName);
            this.version = sanitizeField(version);
            this.mapName = sanitizeField(mapName);
            this.mapSize = sanitizeField(mapSize);
            this.difficultyLevel = sanitizeField(difficultyLevel);
            this.players = sanitizeField(players);
            this.connectAddress = sanitizeConnectAddress(connectAddress);
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
            if (reportedAddress.isBlank()) {
                reportedAddress = remoteHost;
            }
            return new ServerEntry(
                    parameters.get("servername"),
                    parameters.get("version"),
                    rawMapName,
                    parameters.get("mapsize"),
                    parameters.get("difficultylevel"),
                    rowPlayers,
                    reportedAddress
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
        private String connectAddress() {
            return connectAddress;
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
            return NO_MAP_NAME.equalsIgnoreCase(mapName.strip()) || parseLeadingInt(players) < 0;
        }

        /**
         * Java support leading integer parser for HAT status `players`.
         * not ported.
         */
        private static int parseLeadingInt(String value) {
            String trimmed = value.stripLeading();
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
                return sanitized.substring(1, sanitized.indexOf(']'));
            }
            int colonIndex = sanitized.indexOf(':');
            if (colonIndex > 0 && colonIndex == sanitized.lastIndexOf(':')) {
                String portSuffix = sanitized.substring(colonIndex + 1);
                if (isUnsignedDecimal(portSuffix)) {
                    return sanitized.substring(0, colonIndex);
                }
            }
            return sanitized;
        }

        /**
         * not ported.
         */
        private static boolean isUnsignedDecimal(String value) {
            if (value.isEmpty()) {
                return false;
            }
            for (int index = 0; index < value.length(); index++) {
                if (!Character.isDigit(value.charAt(index))) {
                    return false;
                }
            }
            return true;
        }

        /**
         * not ported.
         */
        private static String defaultIfBlank(String value, String fallback) {
            return value == null || value.isBlank() ? fallback : value;
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
