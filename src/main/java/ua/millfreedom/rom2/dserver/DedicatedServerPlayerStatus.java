package ua.millfreedom.rom2.dserver;

/**
 * Java support DTO for the Swing dedicated-server operator UI.
 * not ported.
 */
public final class DedicatedServerPlayerStatus {
    public final int playerId;
    public final String name;
    public final boolean connected;
    public final String loginName;
    public final String ipAddress;
    public final String onlineDuration;
    public final int lastIntervalBytes;
    public final int averageBytes;
    public final int peakIntervalBytes;
    public final int creatureKills;
    public final int playerKills;
    public final int frags;
    public final int deaths;

    /**
     * Java support constructor for a dedicated-server player status row.
     * not ported.
     */
    public DedicatedServerPlayerStatus(
            int playerId,
            String name,
            boolean connected,
            String loginName,
            String ipAddress,
            String onlineDuration,
            int lastIntervalBytes,
            int averageBytes,
            int peakIntervalBytes,
            int creatureKills,
            int playerKills,
            int frags,
            int deaths
    ) {
        this.playerId = playerId;
        this.name = name;
        this.connected = connected;
        this.loginName = loginName;
        this.ipAddress = ipAddress;
        this.onlineDuration = onlineDuration;
        this.lastIntervalBytes = lastIntervalBytes;
        this.averageBytes = averageBytes;
        this.peakIntervalBytes = peakIntervalBytes;
        this.creatureKills = creatureKills;
        this.playerKills = playerKills;
        this.frags = frags;
        this.deaths = deaths;
    }
}
