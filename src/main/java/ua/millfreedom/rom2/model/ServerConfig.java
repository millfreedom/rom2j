package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.model.net.CLlDriver;

import java.util.ArrayList;
import java.util.List;

/**
 * Native global config holder: g_ServerConfig @00690418.
 */
public final class ServerConfig {
    // Native Global::LoadConfig @004EF90F writes this for `protocol=dplay_ipx`.
    public static final int CONFIG_PROTOCOL_DPLAY_IPX = 0;
    // Native Global::LoadConfig @004EF935 writes this for `protocol=dplay_tcpip`.
    public static final int CONFIG_PROTOCOL_DPLAY_TCPIP = 1;
    // Native Global::LoadConfig @004EF958 writes this for `protocol=wsock_tcpip`.
    public static final int CONFIG_PROTOCOL_WSOCK_TCPIP = 2;
    private static final int NO_ENDPOINT_PORT = -1;
    private static final int INVALID_ENDPOINT_PORT = -2;

    //0x00
    public int repopdelay;

    //0x04
    public int protocol = -1;

    //0x08
    public int gameSpeed = 4;

    //0x0c
    public String logfile = "";

    //0x10
    public String ipaddress = "";

    //0x14
    public String chrbase = "";

    //0x18
    public String ServerName = "";

    //0x1c
    public int serverid;

    //0x20
    public final List<String> bannedips = new ArrayList<>();

    //0x34
    public final List<String> bannedplayers = new ArrayList<>();

    //0x48
    public final List<String> maps = new ArrayList<>();

    //0x5c
    public final List<String> reporttowww = new ArrayList<>();

    //0x70
    public final List<Integer> field12_0x70 = new ArrayList<>();

    //0x84
    public int sayrange;

    //0x88
    public int shoutdelay;

    //0x8c
    public int field15_0x8c;

    //0x90
    public int save;

    //0x94
    public int maxplayers;

    // Java support, not a native field. Dedicated raw TCP game listener/connect port.
    public int gamePort = CLlDriver.DEFAULT_TCP_GAME_PORT;

    // Java support, not a native field. Dedicated raw TCP discovery listener port.
    public int discoveryPort = CLlDriver.DEFAULT_TCP_DISCOVERY_PORT;

    // Java support, not a native field. Nonzero hides the dedicated server from public HAT lists.
    public int notPublic = 0;

    // Java support, not a native field. Prevents a later game-port line from overwriting an explicit discovery port.
    private boolean discoveryPortConfigured;

    /**
     * Native: ServerConfig::ServerConfig @004EF34D.
     * Fully ported.
     */
    public ServerConfig() {
        repopdelay = 100;
        protocol = -1;
        gameSpeed = 4;
        field15_0x8c = 0;
        sayrange = 0x100;
        shoutdelay = 0x78;
        save = 0;
        maxplayers = 0x10;
        ServerName = "unnamed server";
    }

    /**
     * Java support for dedicated-server `[settings] ipaddress=host[:port]` endpoint parsing.
     * not ported.
     */
    public boolean applyConfiguredIpAddress(String endpoint) {
        String normalizedEndpoint = normalizeEndpoint(endpoint);
        int endpointPort = parseEndpointPort(normalizedEndpoint);
        if (endpointPort == INVALID_ENDPOINT_PORT) {
            return false;
        }
        ipaddress = normalizedEndpoint;
        if (endpointPort != NO_ENDPOINT_PORT) {
            setConfiguredGamePort(endpointPort);
        }
        return true;
    }

    /**
     * Java support for dedicated-server `[settings] gameport=...` parsing.
     * not ported.
     */
    public boolean setConfiguredGamePort(int port) {
        if (!isValidTcpPort(port)) {
            return false;
        }
        gamePort = port;
        if (!discoveryPortConfigured) {
            discoveryPort = CLlDriver.defaultDiscoveryPortForGamePort(port);
        }
        if (hasTcpEndpointPort(ipaddress)) {
            ipaddress = replaceEndpointPort(ipaddress, port);
        }
        return true;
    }

    /**
     * Java support for dedicated-server `[settings] discoveryport=...` parsing.
     * not ported.
     */
    public boolean setConfiguredDiscoveryPort(int port) {
        if (!isValidTcpPort(port)) {
            return false;
        }
        discoveryPort = port;
        discoveryPortConfigured = true;
        return true;
    }

    /**
     * Java support accessor for a configured dedicated bind host without an optional endpoint port.
     * not ported.
     */
    public String dedicatedBindAddressOrDefault(String fallback) {
        return tcpEndpointHostOrDefault(ipaddress, fallback);
    }

    /**
     * Java support accessor for the HAT/client connect endpoint advertised by a dedicated server.
     * not ported.
     */
    public String dedicatedAdvertisedAddress() {
        if (ipaddress.isBlank()) {
            return "";
        }
        if (hasTcpEndpointPort(ipaddress)) {
            return ipaddress;
        }
        return appendEndpointPort(ipaddress, gamePort);
    }

    /**
     * Java support endpoint parser shared by dedicated startup and raw TCP clients.
     * not ported.
     */
    public static String tcpEndpointHostOrDefault(String endpoint, String fallback) {
        String normalizedEndpoint = normalizeEndpoint(endpoint);
        if (normalizedEndpoint.isBlank()) {
            return fallback;
        }
        if (normalizedEndpoint.startsWith("[")) {
            int bracketEnd = normalizedEndpoint.indexOf(']');
            if (bracketEnd > 0) {
                return normalizedEndpoint.substring(1, bracketEnd);
            }
        }
        int colonIndex = singleHostPortColonIndex(normalizedEndpoint);
        if (colonIndex > 0 && parseEndpointPort(normalizedEndpoint) > 0) {
            return normalizedEndpoint.substring(0, colonIndex);
        }
        return normalizedEndpoint;
    }

    /**
     * Java support endpoint parser shared by dedicated startup and raw TCP clients.
     * not ported.
     */
    public static int tcpEndpointPortOrDefault(String endpoint, int fallback) {
        int endpointPort = parseEndpointPort(endpoint);
        if (endpointPort == INVALID_ENDPOINT_PORT) {
            throw new IllegalArgumentException("Invalid TCP endpoint port: " + endpoint);
        }
        return endpointPort == NO_ENDPOINT_PORT ? fallback : endpointPort;
    }

    /**
     * Java support endpoint predicate for host:port config strings.
     * not ported.
     */
    public static boolean hasTcpEndpointPort(String endpoint) {
        return parseEndpointPort(endpoint) > 0;
    }

    /**
     * Java support TCP port range predicate.
     * not ported.
     */
    public static boolean isValidTcpPort(int port) {
        return port >= 1 && port <= 0xFFFF;
    }

    /**
     * Java support endpoint cleanup before parsing config or command-line values.
     * not ported.
     */
    private static String normalizeEndpoint(String endpoint) {
        return endpoint == null ? "" : endpoint.strip();
    }

    /**
     * Java support optional TCP port parser for host:port and `[host]:port` endpoint strings.
     * not ported.
     */
    private static int parseEndpointPort(String endpoint) {
        String normalizedEndpoint = normalizeEndpoint(endpoint);
        if (normalizedEndpoint.isBlank()) {
            return NO_ENDPOINT_PORT;
        }
        if (normalizedEndpoint.startsWith("[")) {
            int bracketEnd = normalizedEndpoint.indexOf(']');
            if (bracketEnd < 0 || bracketEnd + 1 >= normalizedEndpoint.length()) {
                return NO_ENDPOINT_PORT;
            }
            if (normalizedEndpoint.charAt(bracketEnd + 1) != ':') {
                return NO_ENDPOINT_PORT;
            }
            return parsePortSuffix(normalizedEndpoint.substring(bracketEnd + 2));
        }
        int colonIndex = singleHostPortColonIndex(normalizedEndpoint);
        if (colonIndex < 0) {
            return NO_ENDPOINT_PORT;
        }
        return parsePortSuffix(normalizedEndpoint.substring(colonIndex + 1));
    }

    /**
     * Java support parser for the numeric suffix in a TCP endpoint.
     * not ported.
     */
    private static int parsePortSuffix(String suffix) {
        if (!isUnsignedDecimal(suffix)) {
            return INVALID_ENDPOINT_PORT;
        }
        int port;
        try {
            port = Integer.parseInt(suffix);
        } catch (NumberFormatException exception) {
            return INVALID_ENDPOINT_PORT;
        }
        return isValidTcpPort(port) ? port : INVALID_ENDPOINT_PORT;
    }

    /**
     * Java support predicate for unambiguous IPv4/hostname `host:port` endpoint strings.
     * not ported.
     */
    private static int singleHostPortColonIndex(String endpoint) {
        int colonIndex = endpoint.indexOf(':');
        if (colonIndex <= 0 || colonIndex != endpoint.lastIndexOf(':')) {
            return -1;
        }
        return colonIndex;
    }

    /**
     * Java support endpoint updater used when `[settings] gameport` follows an `ipaddress=host:port` line.
     * not ported.
     */
    private static String replaceEndpointPort(String endpoint, int port) {
        String normalizedEndpoint = normalizeEndpoint(endpoint);
        if (normalizedEndpoint.startsWith("[")) {
            int bracketEnd = normalizedEndpoint.indexOf(']');
            if (bracketEnd > 0) {
                return normalizedEndpoint.substring(0, bracketEnd + 1) + ":" + port;
            }
        }
        int colonIndex = singleHostPortColonIndex(normalizedEndpoint);
        if (colonIndex > 0) {
            return normalizedEndpoint.substring(0, colonIndex) + ":" + port;
        }
        return appendEndpointPort(normalizedEndpoint, port);
    }

    /**
     * Java support endpoint formatter for HAT-advertised dedicated addresses.
     * not ported.
     */
    private static String appendEndpointPort(String host, int port) {
        if (host.startsWith("[") || !host.contains(":")) {
            return host + ":" + port;
        }
        return "[" + host + "]:" + port;
    }

    /**
     * Java support decimal predicate for endpoint port suffix parsing.
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
}
