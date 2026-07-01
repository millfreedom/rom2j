package ua.millfreedom.rom2.gameserver.missionruntime;

import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.CPlayer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ua.millfreedom.rom2.model.CServerApp;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.enums.UnitActionState;
import ua.millfreedom.rom2.model.unit.Unit;

/**
 * Native 0x132C-byte mission diplomacy state serialized at MissionScriptRuntime +0xA8BC.
 */
public final class MissionDiplomacyState {
    public static final int SERIALIZED_SIZE = 0x132C;
    private static final int FIRST_PLAYER_ID = 1;
    private static final int RELATION_GRID_LIMIT = 0x46;
    public static final int SELF_RELATION_FLAGS = 0x12;
    private static final int RELATION_SETTLED_MASK = CPlayer.ENEMY_MASK | CPlayer.ALLIED_MASK;
    private static final int ENGAGEMENT_COUNTER_THRESHOLD = 10;

    //0x00
    public byte activeToInactiveDefaultRelation;
    //0x01
    public byte inactiveToActiveDefaultRelation;
    //0x02
    public byte activeToActiveDefaultRelation;
    //0x03
    public byte inactiveToInactiveDefaultRelation;
    //0x04
    public Object ownerRuntime;
    //0x08
    public final byte[][] relationGrid = new byte[RELATION_GRID_LIMIT][RELATION_GRID_LIMIT];

    /**
     * Native: MissionDiplomacyState::initialize @0057998C.
     */
    public void initialize() {
        activeToInactiveDefaultRelation = 1;
        inactiveToActiveDefaultRelation = 1;
        activeToActiveDefaultRelation = 0;
        inactiveToInactiveDefaultRelation = 0;
        for (byte[] row : relationGrid) {
            java.util.Arrays.fill(row, (byte) 0);
        }
    }

    /**
     * Native support extracted from MissionScriptRuntime::bindMissionDiplomacyStateOwner @00576FB1.
     */
    public void setOwnerRuntime(Object ownerRuntime) {
        this.ownerRuntime = ownerRuntime;
    }

    /**
     * Native support extracted from MissionScriptRuntime::Serialize @0057468D fixed-buffer write.
     */
    public byte[] toNativeBytes() {
        byte[] bytes = new byte[SERIALIZED_SIZE];
        bytes[0] = activeToInactiveDefaultRelation;
        bytes[1] = inactiveToActiveDefaultRelation;
        bytes[2] = activeToActiveDefaultRelation;
        bytes[3] = inactiveToInactiveDefaultRelation;
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putInt(4, Utils.encodePointerLike(ownerRuntime));
        int offset = 8;
        for (int row = 0; row < RELATION_GRID_LIMIT; row++) {
            System.arraycopy(relationGrid[row], 0, bytes, offset, RELATION_GRID_LIMIT);
            offset += RELATION_GRID_LIMIT;
        }
        return bytes;
    }

    /**
     * Native support extracted from MissionScriptRuntime::Serialize @0057468D fixed-buffer read.
     */
    public void readNativeBytes(byte[] bytes) {
        activeToInactiveDefaultRelation = bytes[0];
        inactiveToActiveDefaultRelation = bytes[1];
        activeToActiveDefaultRelation = bytes[2];
        inactiveToInactiveDefaultRelation = bytes[3];
        ownerRuntime = null;
        int offset = 8;
        for (int row = 0; row < RELATION_GRID_LIMIT; row++) {
            System.arraycopy(bytes, offset, relationGrid[row], 0, RELATION_GRID_LIMIT);
            offset += RELATION_GRID_LIMIT;
        }
    }

    /**
     * Native: MissionDiplomacyState::RegisterPlayer @00579596.
     * Fully ported.
     */
    public void registerPlayer(Player player) {
        int playerId = (short) player.playerId;
        int playerKind = player.isActive;
        relationGrid[playerId][0] = (byte) playerKind;
        if (playerKind == 2) {
            relationGrid[playerId][0] = 0;
        }

        relationGrid[0][playerId] = 1;
        for (int otherPlayerId = FIRST_PLAYER_ID; otherPlayerId < RELATION_GRID_LIMIT; otherPlayerId++) {
            if (relationGrid[0][otherPlayerId] == 0) {
                continue;
            }

            if (relationGrid[playerId][0] == 0) {
                if (relationGrid[otherPlayerId][0] == 0) {
                    relationGrid[playerId][otherPlayerId] = inactiveToInactiveDefaultRelation;
                    relationGrid[otherPlayerId][playerId] = inactiveToInactiveDefaultRelation;
                } else {
                    relationGrid[playerId][otherPlayerId] = inactiveToActiveDefaultRelation;
                    relationGrid[otherPlayerId][playerId] = activeToInactiveDefaultRelation;
                }
            } else if (relationGrid[otherPlayerId][0] == 0) {
                relationGrid[playerId][otherPlayerId] = activeToInactiveDefaultRelation;
                relationGrid[otherPlayerId][playerId] = inactiveToActiveDefaultRelation;
            } else {
                relationGrid[playerId][otherPlayerId] = activeToActiveDefaultRelation;
                relationGrid[otherPlayerId][playerId] = activeToActiveDefaultRelation;
            }
        }
        relationGrid[playerId][playerId] = SELF_RELATION_FLAGS;
    }

    /**
     * Native: MissionDiplomacyState::unregisterPlayer @0057975E.
     * Fully ported.
     */
    public void unregisterPlayer(Player player) {
        relationGrid[0][(short) player.playerId] = 0;
    }

    /**
     * Native support extracted from ScenarioMapLoader::loadScenarioMap @005606AA.
     */
    public void setRelationFlags(int rowPlayerId, int columnPlayerId, int relationFlags) {
        relationGrid[rowPlayerId][columnPlayerId] = (byte) relationFlags;
    }

    /**
     * Native support extracted from ScenarioMapLoader::loadScenarioMap @005606AA Self visibility branch.
     */
    public int relationFlags(int rowPlayerId, int columnPlayerId) {
        return relationGrid[rowPlayerId][columnPlayerId] & 0xFF;
    }

    /**
     * Native support extracted from relation matrix reads in MissionScriptRuntime @0056B059, @0056BB53, and @00571E9B.
     */
    public boolean hasRelationFlag(int rowPlayerId, int columnPlayerId, int relationMask) {
        return (relationGrid[rowPlayerId][columnPlayerId] & relationMask) != 0;
    }

    /**
     * Native: MissionDiplomacyState::hasHostileRelation @0057994D.
     */
    public boolean hasHostileRelation(Unit source, Unit target) {
        return (relationGrid[(short) source.owner.playerId]
                [(short) target.owner.playerId] & CPlayer.ENEMY_MASK) != 0;
    }

    /**
     * Native: MissionDiplomacyState::UpdateRelationsForUnitEngagement @0057977A.
     */
    public void updateRelationsForUnitEngagement(Unit source, Unit target, int gatedRelationUpdate) {
        if (source.owner != null && target.owner != null) {
            Player sourceOwner = source.owner;
            Player targetOwner = target.owner;
            int sourcePlayerId = sourceOwner.playerId & 0xFF;
            int targetPlayerId = targetOwner.playerId & 0xFF;
            if ((relationGrid[sourcePlayerId][targetPlayerId] & RELATION_SETTLED_MASK) == 0) {
                relationGrid[sourcePlayerId][targetPlayerId] |= CPlayer.ENEMY_MASK;
                if (sourceOwner.isActive == 0) {
                    CServerApp.sendDiplomacyStateSnapshot(sourceOwner);
                    CServerApp.sendDiplomacyStateSnapshot(targetOwner);
                }
            }

            if ((relationGrid[targetPlayerId][sourcePlayerId] & RELATION_SETTLED_MASK) == 0) {
                if (gatedRelationUpdate == 0
                        || target.missionRuntimeState.engagementCounter > ENGAGEMENT_COUNTER_THRESHOLD
                        || target.m_nHP < target.m_nMaxHP / 2
                        || target.state == UnitActionState.DEAD) {
                    relationGrid[targetPlayerId][sourcePlayerId] |= CPlayer.ENEMY_MASK;
                    if (targetOwner.isActive == 0) {
                        CServerApp.sendDiplomacyStateSnapshot(sourceOwner);
                        CServerApp.sendDiplomacyStateSnapshot(targetOwner);
                    }
                }
                if (gatedRelationUpdate != 0) {
                    target.missionRuntimeState.engagementCounter++;
                }
            }
        }
    }

}
