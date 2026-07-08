package ua.millfreedom.rom2.maptransfer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Java-only loader for dedicated-server map-transfer portal definitions.
 * not ported.
 */
public final class TransferPortalLoader {
    // Java support, not a native field.
    public static final Path DEFAULT_PORTAL_FILE = Path.of("portals.txt");
    // Java support, not a native field.
    private static final Pattern PORTAL_LINE = Pattern.compile(
            "^\\s*([^;]+?)\\s*;\\s*([+-]?\\d+)\\s*:\\s*([+-]?\\d+)\\s*->\\s*([^;]+?)\\s*;\\s*([+-]?\\d+)\\s*:\\s*([+-]?\\d+)\\s*$"
    );

    /**
     * Java utility constructor.
     * not ported.
     */
    private TransferPortalLoader() {
    }

    /**
     * Java support loader for transfer zones belonging to the current dedicated map.
     * not ported.
     */
    public static List<TransferZone> loadTransferZonesForMap(String mapName) {
        return loadTransferZonesForMap(DEFAULT_PORTAL_FILE, mapName);
    }

    /**
     * Java support loader for transfer zones belonging to the current dedicated map.
     * not ported.
     */
    public static List<TransferZone> loadTransferZonesForMap(Path portalFile, String mapName) {
        List<TransferPortal> portals = loadPortals(portalFile);
        List<TransferZone> zones = new ArrayList<>();
        Map<Long, TransferPortal> bySourceCoordinate = new HashMap<>();
        for (TransferPortal portal : portals) {
            if (!portal.sourceMapMatches(mapName)) {
                continue;
            }
            TransferPortal previous = bySourceCoordinate.putIfAbsent(portal.sourceCoordinateKey(), portal);
            if (previous != null) {
                throw new IllegalArgumentException(
                        "%s:%d duplicate transfer zone for %s at %d:%d; first declared on line %d".formatted(
                                portalFile,
                                portal.lineNumber(),
                                mapName,
                                portal.sourceX(),
                                portal.sourceY(),
                                previous.lineNumber()
                        )
                );
            }
            zones.add(portal.toTransferZone());
        }
        return List.copyOf(zones);
    }

    /**
     * Java support parser for all portal entries in one file.
     * not ported.
     */
    private static List<TransferPortal> loadPortals(Path portalFile) {
        if (Files.notExists(portalFile)) {
            return List.of();
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(portalFile, Charset.defaultCharset());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read " + portalFile, exception);
        }

        List<TransferPortal> portals = new ArrayList<>();
        for (int index = 0; index < lines.size(); index++) {
            String rawLine = lines.get(index);
            String trimmedLine = rawLine.strip();
            if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                continue;
            }
            portals.add(parsePortalLine(portalFile, rawLine, index + 1));
        }
        return portals;
    }

    /**
     * Java support parser for one `fromMap;fromX:fromY -> toMap;toX:toY` entry.
     * not ported.
     */
    private static TransferPortal parsePortalLine(Path portalFile, String rawLine, int lineNumber) {
        Matcher matcher = PORTAL_LINE.matcher(rawLine);
        if (!matcher.matches()) {
            throw malformedPortalLine(portalFile, lineNumber, rawLine);
        }

        String sourceMapName = matcher.group(1).strip();
        String destinationMapName = matcher.group(4).strip();
        if (sourceMapName.isEmpty() || destinationMapName.isEmpty()) {
            throw malformedPortalLine(portalFile, lineNumber, rawLine);
        }
        return new TransferPortal(
                sourceMapName,
                parseCellCoordinate(portalFile, lineNumber, rawLine, matcher.group(2)),
                parseCellCoordinate(portalFile, lineNumber, rawLine, matcher.group(3)),
                destinationMapName,
                parseCellCoordinate(portalFile, lineNumber, rawLine, matcher.group(5)),
                parseCellCoordinate(portalFile, lineNumber, rawLine, matcher.group(6)),
                lineNumber
        );
    }

    /**
     * Java support parser for non-negative map-cell coordinate values.
     * not ported.
     */
    private static int parseCellCoordinate(Path portalFile, int lineNumber, String rawLine, String coordinateText) {
        int coordinate;
        try {
            coordinate = Integer.parseInt(coordinateText);
        } catch (NumberFormatException exception) {
            throw malformedPortalLine(portalFile, lineNumber, rawLine);
        }
        if (coordinate < 0) {
            throw malformedPortalLine(portalFile, lineNumber, rawLine);
        }
        return coordinate;
    }

    /**
     * Java support parse-error factory with file and line evidence.
     * not ported.
     */
    private static IllegalArgumentException malformedPortalLine(Path portalFile, int lineNumber, String rawLine) {
        return new IllegalArgumentException(
                "%s:%d malformed transfer portal: %s".formatted(portalFile, lineNumber, rawLine)
        );
    }
}
