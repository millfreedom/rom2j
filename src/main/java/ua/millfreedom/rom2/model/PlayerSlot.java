package ua.millfreedom.rom2.model;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class PlayerSlot {
    //0x00
    public short type;
    //0x02
    public short color;
    //0x04
    public int dataSize;
    //0x08
    public byte[] data;

    /**
     * Native: PlayerSlot::PlayerSlot @00493B10.
     * Fully ported.
     */
    public PlayerSlot() {
        reset();
    }

    /**
     * Native support extracted from PlayerSlot::PlayerSlot @00493B10 for in-place slot reset call sites.
     * Fully ported.
     */
    public void reset() {
        type = 0;
        color = 0;
        dataSize = 0;
        data = null;
    }

    /**
     * Native: PlayerSlot::AssignSpellPanelSlot @0041DD5E.
     * Fully ported.
     */
    public void assignSpellPanelSlot(int spellSlot) {
        type = 1;
        color = (short) spellSlot;
    }

    /**
     * Native: PlayerSlot::MatchesSpellPanelSlot @0041DE8F.
     * Fully ported.
     */
    public boolean matchesSpellPanelSlot(int spellSlot) {
        return type == 1 && color == (short) spellSlot;
    }

    /**
     * Native: PlayerSlot::Load @0041DF74.
     * Fully ported.
     */
    public void load(ByteBuffer buffer) {
        readPayload(buffer);
    }

    /**
     * Native: PlayerSlot::readFromBuffer @0041E0C2.
     * Fully ported.
     */
    public void readFromBuffer(ByteBuffer buffer) {
        readPayload(buffer);
    }

    /**
     * Native support extracted from PlayerSlot::Load @0041DF74 and PlayerSlot::readFromBuffer @0041E0C2.
     * Fully ported.
     */
    private void readPayload(ByteBuffer buffer) {
        if (dataSize != 0) {
            data = null;
            dataSize = 0;
        }
        type = buffer.getShort();
        color = buffer.getShort();
        dataSize = buffer.getInt();
        if (dataSize != 0) {
            data = new byte[dataSize];
            buffer.get(data);
        }
    }

    /**
     * Native: PlayerSlot::writeToFile @0041DF0C.
     * Fully ported.
     */
    public void writeToFile(ByteBuffer buffer) {
        writePayload(buffer);
    }

    /**
     * Native: PlayerSlot::writeToBuffer @0041E026.
     * Fully ported.
     */
    public int writeToBuffer(ByteBuffer buffer) {
        return writePayload(buffer);
    }

    /**
     * Native support extracted from PlayerSlot::writeToFile @0041DF0C and PlayerSlot::writeToBuffer @0041E026.
     * Fully ported.
     */
    private int writePayload(ByteBuffer buffer) {
        buffer.putShort(type);
        buffer.putShort(color);
        buffer.putInt(dataSize);
        if (dataSize != 0) {
            buffer.put(data, 0, dataSize);
        }
        return dataSize + 8;
    }

    /**
     * Native: PlayerSlot::AssignFromTokenEntry @0041DD7E.
     * Fully ported.
     */
    public PlayerSlot assignFromTokenEntry(TokenEntry entry) {
        type = 2;
        color = (short) entry.packedTokenHash;
        dataSize = entry.payloadSize & 0xFF;
        if (dataSize != 0) {
            data = Arrays.copyOf(entry.payloadBytes, dataSize);
        }
        return this;
    }

    /**
     * Native: PlayerSlot::MatchesTokenEntry @0041DDEA.
     * Fully ported.
     */
    public boolean matchesTokenEntry(TokenEntry entry) {
        if (entry == null || type != 2 || color != (short) entry.packedTokenHash) {
            return false;
        }
        if (dataSize != (entry.payloadSize & 0xFF)) {
            return false;
        }
        for (int index = 0; index < dataSize; index++) {
            if (data[index] != entry.payloadBytes[index]) {
                return false;
            }
        }
        return true;
    }
}
