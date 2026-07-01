package ua.millfreedom.rom2.model.world.scenario;

public final class ScenarioRawSection {
    public final ScenarioSectionHeader header;
    public final byte[] payload;

    // not ported.
    public ScenarioRawSection(ScenarioSectionHeader header, byte[] payload) {
        this.header = header;
        this.payload = payload;
    }
}
