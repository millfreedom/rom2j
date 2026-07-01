package ua.millfreedom.rom2.model;

/**
 * Native support type: LlDriverProtocolOption.
 */
public class LlDriverProtocolOption {
    public final String displayName;
    public final int protocolId;

    /**
     * Native support extracted from LlDriverProtocolOption entries produced by
     * CLlDriver::GetAvailableProtocols @00507006.
     */
    public LlDriverProtocolOption(String displayName, int protocolId) {
        this.displayName = displayName;
        this.protocolId = protocolId;
    }
}
