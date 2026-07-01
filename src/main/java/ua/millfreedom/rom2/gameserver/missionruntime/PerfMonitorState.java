package ua.millfreedom.rom2.gameserver.missionruntime;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Java representation of native PerfMonitor timing state.
 */
public final class PerfMonitorState {
    public static final int SERIALIZED_SIZE = 0x30;

    //0x00
    public long counterTicks;
    //0x08
    public final byte[] counterReservedBytes = new byte[8];
    //0x10
    public long frequencyTicksPerSecond;
    //0x18
    public final byte[] frequencyReservedBytes = new byte[8];
    //0x20
    public int ticksPerMillisecond;
    //0x24
    public final byte[] ticksPerMillisecondReservedBytes = new byte[4];
    //0x28
    public int elapsedMilliseconds;
    //0x2c
    public int elapsedMillisecondsHighDword;

    /**
     * Native: PerfMonitor::initialize @0055F290.
     * Fully ported.
     */
    public void initialize() {
        frequencyTicksPerSecond = 1_000_000_000L;
        ticksPerMillisecond = (int) (frequencyTicksPerSecond / 1000L);
        counterTicks = System.nanoTime();
        elapsedMilliseconds = 0;
        elapsedMillisecondsHighDword = 0;
    }

    /**
     * Native: PerfMonitor::QueryPerfCounter @0055F2D0.
     * Fully ported.
     */
    public void queryCounter() {
        counterTicks = System.nanoTime();
    }

    /**
     * Native: PerfMonitor::finishElapsedMilliseconds @0057AEE0.
     */
    public int finishElapsedMilliseconds() {
        elapsedMilliseconds = (int) ((System.nanoTime() - counterTicks) * 1000L / frequencyTicksPerSecond);
        elapsedMillisecondsHighDword = 0;
        return elapsedMilliseconds;
    }

    /**
     * Native support extracted from MissionScriptRuntime::Serialize @0057468D PerfMonitor fixed-buffer write.
     */
    public byte[] toNativeBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(SERIALIZED_SIZE).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putLong(0x00, counterTicks);
        buffer.position(0x08);
        buffer.put(counterReservedBytes);
        buffer.putLong(0x10, frequencyTicksPerSecond);
        buffer.position(0x18);
        buffer.put(frequencyReservedBytes);
        buffer.putInt(0x20, ticksPerMillisecond);
        buffer.position(0x24);
        buffer.put(ticksPerMillisecondReservedBytes);
        buffer.putInt(0x28, elapsedMilliseconds);
        buffer.putInt(0x2c, elapsedMillisecondsHighDword);
        return buffer.array();
    }

    /**
     * Native support extracted from MissionScriptRuntime::Serialize @0057468D PerfMonitor fixed-buffer read.
     */
    public void readNativeBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        counterTicks = buffer.getLong(0x00);
        buffer.position(0x08);
        buffer.get(counterReservedBytes);
        frequencyTicksPerSecond = buffer.getLong(0x10);
        buffer.position(0x18);
        buffer.get(frequencyReservedBytes);
        ticksPerMillisecond = buffer.getInt(0x20);
        buffer.position(0x24);
        buffer.get(ticksPerMillisecondReservedBytes);
        elapsedMilliseconds = buffer.getInt(0x28);
        elapsedMillisecondsHighDword = buffer.getInt(0x2c);
    }
}
