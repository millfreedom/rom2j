package ua.millfreedom.rom2.model;

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
}
