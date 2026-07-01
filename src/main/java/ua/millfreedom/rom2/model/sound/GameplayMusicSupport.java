package ua.millfreedom.rom2.model.sound;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.world.ScenarioDescriptor;
import ua.millfreedom.rom2.model.world.scenario.MusicDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Native support extracted from CMainWindow::playGameplayMusicPlaylist @004924CD and updatePreferredGameplayTrackIndex @004754E9.
 */
public final class GameplayMusicSupport {
    /**
     * Java utility constructor.
     * not ported.
     */
    private GameplayMusicSupport() {
    }

    /**
     * Native support extracted from updatePreferredGameplayTrackIndex @004754E9.
     * Fully ported. Selects the nearest active scenario music region for the supplied world position and updates
     * g_CurrentMusicTrack @005F1A08.
     */
    public static void updatePreferredGameplayTrackIndex(int unitX, int unitY) {
        int selectedRecordIndex = resolveGameplayMusicRecordIndex(unitX, unitY);
        if (selectedRecordIndex < 0) {
            return;
        }
        MusicDTO selectedRecord =
                collectScenarioMusicRecords().get(selectedRecordIndex);
        Globals.currentMusicTrack = selectRandomActiveTrackIndex(selectedRecord);
    }

    /**
     * Native support extracted from `g_CurrentMusicTrack @005F1A08` reads in CMainWindow::playGameplayMusicPlaylist @004924CD.
     */
    public static int getCurrentMusicTrack() {
        return Globals.currentMusicTrack;
    }

    /**
     * Native support extracted from the `g_CArray<MusicDTO>` pointer-array cleanup tails in
     * MapVisualObject::clearSessionForLobbyReturn @0041CD15 and
     * MapVisualObject::cleanupCompletedMissionMapState @0041C897.
     * Fully ported. Java stores the native global music-record pointer array through the active ScenarioDescriptor
     * backing owner.
     */
    public static void clearScenarioMusicRecordsForSessionTeardown() {
        //no-op in java
//        ScenarioDescriptor scenarioDescriptor = Globals.gameServer.scenarioDescriptor;
//        scenarioDescriptor.sect12Music.clear();
//        scenarioDescriptor.defaultMusic.x = 0;
//        scenarioDescriptor.defaultMusic.y = 0;
//        scenarioDescriptor.defaultMusic.radius = 0;
//        scenarioDescriptor.defaultMusic.m1 = -1;
//        scenarioDescriptor.defaultMusic.m2 = -1;
//        scenarioDescriptor.defaultMusic.m3 = -1;
//        scenarioDescriptor.defaultMusic.m4 = -1;
    }

    /**
     * Native support extracted from updatePreferredGameplayTrackIndex @004754E9.
     */
    private static int resolveGameplayMusicRecordIndex(int unitX, int unitY) {
        List<MusicDTO> musicRecords = collectScenarioMusicRecords();
        int selectedRecordIndex = -1;
        double selectedDistance = 1.0e20d;
        for (int i = 0; i < musicRecords.size(); i++) {
            MusicDTO musicRecord = musicRecords.get(i);
            if (musicRecord.x == 0 && musicRecord.y == 0) {
                if (selectedDistance > 1.0e15d) {
                    selectedDistance = 1.0e10d;
                    selectedRecordIndex = i;
                }
                continue;
            }
            if (!hasActiveGameplayTrack(musicRecord)) {
                continue;
            }
            double deltaX = unitX - (double) musicRecord.x * 0x100;
            double deltaY = unitY - (double) musicRecord.y * 0x100;
            double distance = Math.sqrt(deltaX * deltaX
                    + deltaY * deltaY);
            if (distance < (double) (musicRecord.radius << 8)
                    && distance < selectedDistance) {
                selectedDistance = distance;
                selectedRecordIndex = i;
            }
        }
        return selectedRecordIndex;
    }

    /**
     * Native support extracted from MapDescriptor::MapDescriptor @004A449C global music-record transfer and
     * updatePreferredGameplayTrackIndex @004754E9.
     */
    private static List<MusicDTO> collectScenarioMusicRecords() {
        ScenarioDescriptor scenarioDescriptor = Globals.gameServer.scenarioDescriptor;
        ArrayList<MusicDTO> musicRecords =
                new ArrayList<>(scenarioDescriptor.sect12Music.size() + 1);
        if (scenarioDescriptor.defaultMusic.m1 >= 0) {
            musicRecords.add(scenarioDescriptor.defaultMusic);
        }
        musicRecords.addAll(scenarioDescriptor.sect12Music);
        return musicRecords;
    }

    /**
     * Native support extracted from updatePreferredGameplayTrackIndex @004754E9.
     * Native loops until a non-`-1` slot is selected; malformed records with no active slot fail fast in Java instead
     * of repeating forever.
     */
    private static int selectRandomActiveTrackIndex(MusicDTO musicRecord) {
        int[] candidateTracks = {
                musicRecord.m1,
                musicRecord.m2,
                musicRecord.m3,
                musicRecord.m4
        };
        ArrayList<Integer> activeTracks = new ArrayList<>(candidateTracks.length);
        for (int candidateTrack : candidateTracks) {
            if (candidateTrack != -1) {
                activeTracks.add(candidateTrack);
            }
        }
        if (activeTracks.isEmpty()) {
            throw new IllegalStateException("Selected scenario music record has no active track slots.");
        }
        return activeTracks.get(Utils.randExclusive(0, activeTracks.size()));
    }

    /**
     * Native support extracted from updatePreferredGameplayTrackIndex @004754E9.
     */
    private static boolean hasActiveGameplayTrack(MusicDTO musicRecord) {
        return musicRecord.m1 != -1
                || musicRecord.m2 != -1
                || musicRecord.m3 != -1
                || musicRecord.m4 != -1;
    }
}
