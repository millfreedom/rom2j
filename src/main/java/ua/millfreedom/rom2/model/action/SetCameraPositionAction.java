package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `TwoDwordAction` packet id `0xAB` used to center the mission camera on a tile position.
 */
public class SetCameraPositionAction extends TwoDwordAction {
    public static final int ACTION_ID = GameActionId.SET_CAMERA_POSITION_ACTION_AB.id;
    public static final SetCameraPositionAction global = new SetCameraPositionAction();

    /**
     * Native support extracted from GameServer::FUN_004F1D9C @004F1D9C and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public SetCameraPositionAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from GameServer::FUN_004F1D9C @004F1D9C and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public SetCameraPositionAction(SetCameraPositionAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from GameServer::FUN_004F1D9C @004F1D9C and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    @Override
    public SetCameraPositionAction Clone() {
        return new SetCameraPositionAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00415BF8.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        if (Globals.mainWindow.scenarioCameraOverrideLock == 0) {
            mapVisualObject.onMessage(
                    MessageCodes.SET_CAMERA_POS,
                    firstPayloadDword.get() - mapVisualObject.gridWidth / 2,
                    secondPayloadDword.get() - mapVisualObject.gridHeight / 2
            );
        }
    }

}
