package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.Building;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.gameobj.CGameObject;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native action class `FixedDwordAction82` / packet id `0x82` used by
 * `CServerApp::notifyStateChanged @00503672`
 * to send building token current-health updates.
 */
public class BuildingHealthAction extends CGameAction {
    public static final int ACTION_ID = GameActionId.BUILDING_HEALTH_ACTION_82.id;
    public static final BuildingHealthAction global = new BuildingHealthAction();
    private static final int NATIVE_COPY_SIZE = 5;

    //0x0A
    public final Property<Integer> buildingTokenId = u16(BODY_OFFSET);
    //0x0C
    public final Property<Integer> healthCurrent = u16(BODY_OFFSET + Short.BYTES);

    /**
     * Native support extracted from FixedDwordAction82::FixedDwordAction82 @0050CBEF,
     * CServerApp::notifyStateChanged @00503672, and
     * MapVisualObject::HandleGameAction @00413C82.
     * Fully ported.
     */
    public BuildingHealthAction() {
        super();
        ID.set(ACTION_ID);
        buildingTokenId.set(0);
        healthCurrent.set(0);
    }

    /**
     * Native support extracted from FixedDwordAction82::FixedDwordAction82 @0050CC15 and
     * CServerApp::notifyStateChanged @00503672.
     * Fully ported.
     */
    public BuildingHealthAction(BuildingHealthAction from) {
        super();
        PutSlice(ID_OFFSET, from.GetSlice(ID_OFFSET, NATIVE_COPY_SIZE), 0, NATIVE_COPY_SIZE);
    }

    /**
     * Native support extracted from CServerApp::notifyStateChanged @00503672 building branch.
     */
    public static BuildingHealthAction createForBuildingStateChanged(Building building, Player player) {
        BuildingHealthAction action = global;
        action.ID.set(ACTION_ID);
        action.playerID.set(player.playerId);
        action.buildingTokenId.set(building.idFull);
        action.healthCurrent.set(building.healthCurrent);
        return action;
    }

    /**
     * vtbl +0x04: FixedDwordAction82::Clone @00541AE0.
     * Fully ported.
     */
    @Override
    public BuildingHealthAction Clone() {
        return new BuildingHealthAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00413C82.
     * Ported action-id case: `BuildingHealthAction` packet id `0x82`.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        CGameObject gameObject = mapVisualObject.getObjectByToken((short) (int) buildingTokenId.get());
        if (gameObject != null) {
            gameObject.HP = (short) (int) healthCurrent.get();
            if (mapVisualObject.getPrimarySelectedObject() == gameObject) {
                Globals.mainWindow.pSelectionInfoPanelVisualObject.onMessage(MessageCodes.NOTIFY_MAP_CONTEXT_CHANGED, 0, 0);
                Globals.mainWindow.pSideStatusVisualObject.onMessage(MessageCodes.NOTIFY_MAP_CONTEXT_CHANGED, 0, 0);
            }
        }
    }

    /**
     * vtbl +0x10: FixedDwordAction82::getWireSize @00541B60.
     * Fully ported.
     */
    @Override
    public int GetPayloadSize() {
        return 0x05;
    }

}
