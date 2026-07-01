package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.actiondata.ActionPayloads;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native short-array packet id `0x9B` used to transfer the run-length encoded tile-visibility mask for the loaded map.
 */
public class TileVisibilityMaskAction extends ShortArrayBlobAction {
    public static final int ACTION_ID = GameActionId.TILE_VISIBILITY_MASK_ACTION_9B.id;
    public static final TileVisibilityMaskAction global = new TileVisibilityMaskAction();

    /**
     * Native support extracted from GameServer::sendInitialScenarioState @004F1D9C and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public TileVisibilityMaskAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from GameServer::sendInitialScenarioState @004F1D9C and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public TileVisibilityMaskAction(TileVisibilityMaskAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from GameServer::sendInitialScenarioState @004F1D9C
     * TILE_VISIBILITY_MASK_ACTION_9B packet field writes.
     */
    public static TileVisibilityMaskAction prepareForInitialScenarioState(
            Player player,
            short[] visibilityMaskRuns
    ) {
        TileVisibilityMaskAction action = global;
        action.ID.set(ACTION_ID);
        action.playerID.set(player.playerId);
        ActionPayloads.setShortArray(action.shortValueCount, action.shortValues, visibilityMaskRuns);
        return action;
    }

    /**
     * Native support extracted from MapVisualObject::ApplyTileVisibilityMaskRuns @0041C071 and MapVisualObject::HandleGameAction @0040D9B2.
     * Ported action-id case: run-length decode for packet id `0x9B` into `MapDescriptor.tilesWxH` tile flag `0x2000`.
     * Fully ported.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        short[] runLengths = ActionPayloads.getShortArray(shortValues);
        mapVisualObject.applyTileVisibilityMaskRuns(runLengths);
    }

    /**
     * Native support extracted from GameServer::sendInitialScenarioState @004F1D9C and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    @Override
    public TileVisibilityMaskAction Clone() {
        return new TileVisibilityMaskAction(this);
    }

}
