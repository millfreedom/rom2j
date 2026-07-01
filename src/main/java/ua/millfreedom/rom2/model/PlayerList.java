package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.unit.humanoid.Humanoid;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class PlayerList implements MfcSerializable {
    private static final int FIRST_PLAYER_ID = 1;
    private static final int MAX_NORMAL_PLAYER_ID_EXCLUSIVE = 0x20;
    private static final int REMOTE_PLAYER_ID_START = 0x10;
    private static final int PLAYER_NUMBER_OVERFLOW_WARNING_THRESHOLD = 0x20;
    private static final int SCAN_MASK_MODULUS = 0x10;
    private static final String SELF_PLAYER_NAME = "Self";
    private static final String NO_FREE_SCAN_MASK_BITS_WARNING = "Warning - no more free scanMask bit's";
    private static final String PLAYER_NUMBER_OVERFLOW_WARNING = "Warning - player number overflow 32";

    //0x00
    public final CustomList<Player> players = new CustomList<>(Player.class);
    //0x20
    public int counter = 1;

    /**
     * Native: PlayerList::PlayerList @005428B0.
     * Fully ported. Field defaults also cover PlayerList::CreateObject @0051628E.
     */
    public PlayerList() {
    }

    /**
     * Native: PlayerList::Serialize @0052D16B.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        if (ar.isStoring()) {
            ar.writeInt(counter);
        } else {
            counter = ar.readInt();
        }

        // CMapPtrToPtr::SetAt(&g_GameServer->cMapPtrToPtr, 0, 0)
        Globals.gameServer.setPointerMapNullEntry();

        // CList<Player>::Serialize at 0x00546610
        ar.serialize(players);
    }

    /**
     * Native: PlayerList::Add @00542C70.
     * Fully ported.
     */
    public void add(Player player) {
        players.add(player);
    }

    /**
     * Native: PlayerList::Remove @0053B3B0.
     * Fully ported.
     */
    public void remove(Player player) {
        players.remove(player);
    }

    /**
     * Native: PlayerList::RemoveAll @0053B8F0.
     * Fully ported.
     */
    public void removeAll() {
        Player player = getFirst();
        while (player != null) {
            destroy(player);
            player = getFirst();
        }
    }

    /**
     * Native: PlayerList::Destroy @0053B010.
     * Fully ported. Native object deletion is omitted; Player::~Player @00515F86 diplomacy unregister is retained.
     */
    public void destroy(Player player) {
        remove(player);
        if (player != null && Globals.gameServer.missionScriptRuntime != null) {
            Globals.gameServer.missionScriptRuntime.missionDiplomacyState.unregisterPlayer(player);
        }
    }

    /**
     * Native: PlayerList::IsEmpty @0053B960.
     * Fully ported.
     */
    public boolean isEmpty() {
        return players.isEmpty();
    }

    /**
     * Native: PlayerList::GetFirst @0053B930.
     * Fully ported.
     */
    public Player getFirst() {
        if (isEmpty()) {
            return null;
        }
        return players.getFirst();
    }

    /**
     * Native: PlayerList::GetHead @0057B120.
     */
    public Player getHead() {
        return getFirst();
    }

    /**
     * Native: PlayerList::GetNext @0053DA40.
     */
    public Player getNext(Player last) {
        int index = players.indexOf(last);
        if (index < 0 || index + 1 >= players.size()) {
            return null;
        }
        return players.get(index + 1);
    }

    /**
     * Native: PlayerList::GetPlayerByID @005167C6.
     * Fully ported.
     */
    public Player getPlayerById(int playerId) {
        for (Player player : players) {
            if (player.playerId == (short) playerId) {
                return player;
            }
        }
        return null;
    }

    /**
     * Native: PlayerList::GetByName @00516851.
     * Fully ported.
     */
    public Player getByName(String name) {
        for (Player player : players) {
            if (player.name.equals(name)) {
                return player;
            }
        }
        return null;
    }

    /**
     * Fully ported. Native: PlayerList::findByScenarioPlayerId @0051680D.
     */
    public Player findByScenarioPlayerId(int scenarioPlayerId) {
        for (Player player : players) {
            if (player.scenarioPlayerId == (short) scenarioPlayerId) {
                return player;
            }
        }
        return null;
    }

    /**
     * Native: PlayerList::GetMaxPlayerId @005168F3.
     * Fully ported.
     */
    public int getMaxPlayerId() {
        int maxPlayerId = 0;
        for (Player player : players) {
            if (maxPlayerId < (short) player.playerId) {
                maxPlayerId = (short) player.playerId;
            }
        }
        return maxPlayerId;
    }

    /**
     * Fully ported. Native: PlayerList::GetPlayersCount @00516993.
     */
    public int getPlayersCount() {
        int count = 0;
        for (Player player : players) {
            if (player.isActive == 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * Native: PlayerList::CountPlayersWithJoinOptions @00516942.
     * Fully ported.
     */
    public int countPlayersWithJoinOptions() {
        int count = 0;
        for (Player player : players) {
            if (player.joinOptions != 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * Fully ported. Native: PlayerList::AddAssigningIdAndScanMask @005165DD.
     */
    public void addAssigningIdAndScanMask(Player player) {
        int occupiedPlayerIds = occupiedNormalPlayerIdsMask();
        int candidatePlayerId = firstCandidatePlayerId(player);

        while (candidatePlayerId < MAX_NORMAL_PLAYER_ID_EXCLUSIVE) {
            if ((occupiedPlayerIds & (1 << (candidatePlayerId - 1))) == 0) {
                player.playerId = candidatePlayerId;
                if (counter <= candidatePlayerId) {
                    counter = candidatePlayerId + 1;
                }
                break;
            }
            candidatePlayerId++;
        }

        if (player.isActive == 0) {
            if (player.playerId == 0) {
                Globals.gameServer.pushMessage(NO_FREE_SCAN_MASK_BITS_WARNING);
                player.playerId = counter;
                counter++;
            }
            player.scanMask = scanMaskForPlayerId(player.playerId);
            player.scanMaskMirror = player.scanMask;
            if ((short) player.playerId > PLAYER_NUMBER_OVERFLOW_WARNING_THRESHOLD) {
                Globals.gameServer.pushMessage(PLAYER_NUMBER_OVERFLOW_WARNING);
            }
        } else {
            player.scanMask = 0;
        }

        add(player);
        if (Globals.gameServer.missionScriptRuntime != null) {
            Globals.gameServer.missionScriptRuntime.missionDiplomacyState.registerPlayer(player);
        }
    }

    /**
     * Native support extracted from PlayerList::AddAssigningIdAndScanMask @005165DD.
     */
    private int occupiedNormalPlayerIdsMask() {
        int occupiedPlayerIds = 0;
        for (Player player : players) {
            int playerId = (short) player.playerId;
            if (playerId < MAX_NORMAL_PLAYER_ID_EXCLUSIVE) {
                occupiedPlayerIds |= 1 << (playerId - 1);
            }
        }
        return occupiedPlayerIds;
    }

    /**
     * Native support extracted from PlayerList::AddAssigningIdAndScanMask @005165DD.
     */
    private static int firstCandidatePlayerId(Player player) {
        if (player.isActive == 0 && Globals.gameServer.networkSessionActive != 0 && player.name.compareTo(SELF_PLAYER_NAME) != 0) {
            return REMOTE_PLAYER_ID_START;
        }
        return FIRST_PLAYER_ID;
    }

    /**
     * Native support extracted from PlayerList::AddAssigningIdAndScanMask @005165DD.
     */
    private static int scanMaskForPlayerId(int playerId) {
        return 1 << ((short) playerId % SCAN_MASK_MODULUS);
    }

    /**
     * Native: PlayerList::updatePeriodicPlayerState @005162F6.
     * Fully ported.
     */
    public void updatePeriodicPlayerState() {
        for (Player player : new ArrayList<>(players)) {
            if (player.shoutDelayTicksRemaining > 0) {
                player.shoutDelayTicksRemaining--;
            }
            if (player.pendingRemovalServerTick > 0
                    && player.pendingRemovalServerTick < Globals.gameServer.someValue) {
                removeExpiredPlayer(player);
            }
        }
    }

    /**
     * Native support extracted from PlayerList::updatePeriodicPlayerState @005162F6.
     * Fully ported.
     */
    private void removeExpiredPlayer(Player player) {
        if (player.missionEntryStateSent != 0) {
            removeExpiredMissionEntryPlayer(player);
        }
        removeExpiredPlayerCharacterLock(player);
        CServerApp.sendTwoDwordAction(null, GameActionId.DELETE_PLAYER_ACTION_97, (short) player.playerId, 0);
        Globals.gameServer.pushMessage("Player " + player.name + " disconnected.");
        if ((short) player.playerId == counter - 1) {
            counter--;
        }
        destroy(player);
        Globals.gameServer.reportServerStatusToConfiguredTargets();
    }

    /**
     * Native support extracted from PlayerList::updatePeriodicPlayerState @005162F6 character lock removal branch.
     * Fully ported.
     */
    private static void removeExpiredPlayerCharacterLock(Player player) {
        if (player.characterLockName.isEmpty()) {
            return;
        }
        Path lockFlagPath = Path.of(Globals.serverConfig.chrbase)
                .resolve("chr")
                .resolve(player.characterLockName)
                .resolve("lockflag");
        try {
            Files.delete(lockFlagPath);
        } catch (IOException ignored) {
            // Native Catch@005164D7 swallows CFile::Remove failures and continues player removal at 005164E8.
        }
    }

    /**
     * Native support extracted from PlayerList::updatePeriodicPlayerState @005162F6 mission-entry cleanup branch.
     * Fully ported.
     */
    private void removeExpiredMissionEntryPlayer(Player player) {
        if (Globals.gameServer.networkSessionActive != 0 && player.controlledUnit != null) {
            Globals.gameServer.saveControlledHumanoid((Humanoid) player.controlledUnit);
        }
        Globals.questStorage.removeAndDeleteQuestsForOwner((short) player.playerId);
        for (Unit unit : player.ownedUnits) {
            Globals.worldMap.detachUnit(unit);
            Globals.gameServer.activeUnits.remove(unit);
            unit.owner = null;
            unit.m_nHP = -600;
            unit.respawning = 5;
            Globals.gameServer.deferredDeathUnits.add(unit);
        }
        player.unitGroups.clear();
    }
}
