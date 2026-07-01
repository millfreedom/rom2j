package ua.millfreedom.rom2.model;

/**
 * Native ClientTrafficStats record allocated by CServerApp::onClientActivated @00505F07.
 */
public final class ClientTrafficStats {
    //0x00
    public int currentIntervalBytes;
    //0x04
    public int lastIntervalBytes;
    //0x08
    public int totalBytes;
    //0x0C
    public int peakIntervalBytes;
    //0x10
    public int sampleCount;
}
