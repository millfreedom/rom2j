package ua.millfreedom.rom2.dserver;

import java.util.List;

/**
 * Java support DTO for transferring dedicated-server state from the game loop to Swing.
 * not ported.
 */
public final class DedicatedServerStatusSnapshot {
    public static final DedicatedServerStatusSnapshot EMPTY = new DedicatedServerStatusSnapshot(
            "",
            List.of(),
            0,
            "",
            false,
            "",
            false
    );

    public final String summaryText;
    public final List<DedicatedServerPlayerStatus> players;
    public final int selectedPlayerId;
    public final String selectedPlayerDetails;
    public final boolean keepSavedCharactersOnServer;
    public final String mapName;
    public final boolean serverRunning;

    /**
     * Java support constructor for one immutable dedicated-server status snapshot.
     * not ported.
     */
    public DedicatedServerStatusSnapshot(
            String summaryText,
            List<DedicatedServerPlayerStatus> players,
            int selectedPlayerId,
            String selectedPlayerDetails,
            boolean keepSavedCharactersOnServer,
            String mapName,
            boolean serverRunning
    ) {
        this.summaryText = summaryText;
        this.players = List.copyOf(players);
        this.selectedPlayerId = selectedPlayerId;
        this.selectedPlayerDetails = selectedPlayerDetails;
        this.keepSavedCharactersOnServer = keepSavedCharactersOnServer;
        this.mapName = mapName;
        this.serverRunning = serverRunning;
    }
}
