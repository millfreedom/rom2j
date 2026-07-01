package ua.millfreedom.rom2;

import java.util.Map;
import java.util.TreeMap;

/**
 * Native DirectPlay latency tracker used to derive reliable-send pacing from round-trip samples.
 */
public class DpLatencyTracker {
    private static final int ROUND_TRIP_SAMPLE_CAPACITY = 0x80;

    //0x000
    public final int[] roundTripSamplesMs = new int[ROUND_TRIP_SAMPLE_CAPACITY];
    // Java support, not a native field. Sorted occurrence index for the current 0x000 sample window.
    private final TreeMap<Integer, Integer> roundTripSampleCountsByLatencyMs = new TreeMap<>();
    //0x200
    public int roundTripSampleCount;
    //0x204
    public int latencyMs;

    /**
     * Native: DpLatencyTracker::DpLatencyTracker @00540CA0.
     * Fully ported.
     */
    public DpLatencyTracker() {
        roundTripSampleCount = 0;
        latencyMs = 0;
    }

    /**
     * Native: DpLatencyTracker::AddRoundTripSample @00506646.
     * Port status: sample capture and median update ported; native 25..1000 clamp intentionally omitted in Java.
     */
    public void AddRoundTripSample(int roundTripMs) {
        roundTripSamplesMs[roundTripSampleCount] = roundTripMs;
        roundTripSampleCountsByLatencyMs.merge(roundTripMs, 1, Integer::sum);
        roundTripSampleCount++;
        if (roundTripSampleCount == ROUND_TRIP_SAMPLE_CAPACITY) {
            roundTripSampleCount = 0;
            flushSortedSamples();
            latencyMs = roundTripSamplesMs[0x40];
        }
    }

    /**
     * Native: DpLatencyTracker::GetSendIntervalMs @005066F3.
     * Fully ported.
     */
    public int GetSendIntervalMs() {
        return latencyMs << 1;
    }

    /**
     * Native: DpLatencyTracker::SetLatencyMs @00540DC0.
     * Fully ported.
     */
    public void SetLatencyMs(int latencyMs) {
        roundTripSampleCount = 0;
        roundTripSampleCountsByLatencyMs.clear();
        this.latencyMs = latencyMs;
    }

    /**
     * Native support extracted from DpLatencyTracker::AddRoundTripSample @00506646 and
     * DpLatencyTracker::SortLatencySamplesAscending @00540AF0.
     * Java uses a TreeMap occurrence table instead of the native four-pass radix scratch buffer.
     */
    private void flushSortedSamples() {
        int sortedIndex = 0;
        for (Map.Entry<Integer, Integer> entry : roundTripSampleCountsByLatencyMs.entrySet()) {
            int latencyMs = entry.getKey();
            int occurrences = entry.getValue();
            for (int i = 0; i < occurrences; i++) {
                roundTripSamplesMs[sortedIndex] = latencyMs;
                sortedIndex++;
            }
        }
        roundTripSampleCountsByLatencyMs.clear();
    }
}
