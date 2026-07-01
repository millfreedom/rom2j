package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `TwoDwordAction` packet id `0xAA` handled by MapVisualObject::HandleGameAction @0040EC8C.
 */
public class MapLightOverrideAction extends TwoDwordAction {
    public static final int ACTION_ID = GameActionId.TWO_DWORD_ACTION_AA.id;
    public static final MapLightOverrideAction global = new MapLightOverrideAction();

    /**
     * Native support extracted from TwoDwordAction::TwoDwordAction @0050BDA2 and MapVisualObject::HandleGameAction @0040EC8C.
     */
    public MapLightOverrideAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from TwoDwordAction::TwoDwordAction @0050BDD5 and MapVisualObject::HandleGameAction @0040EC8C.
     */
    public MapLightOverrideAction(MapLightOverrideAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040EC8C.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        applyMapLightOverride(mapVisualObject, firstPayloadDword.get());
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040EC8C.
     */
    static void applyMapLightOverride(MapVisualObject mapVisualObject, int mode) {
        if (mode == 0) {
            Globals.terrainLightOverrideTransferMode = 0;
            return;
        }
        if (mode == 1) {
            Globals.terrainLightOverrideTransferMode = 1;
            for (int i = 0; i < mapVisualObject.mapDescriptor.mapCellCount(); i++) {
                mapVisualObject.mapDescriptor.tilesWxH[i] = (short) (mapVisualObject.mapDescriptor.tilesWxH[i] | 0xC000);
            }
            return;
        }
        if (mode == 2) {
            Globals.mainWindow.postMessage(MessageCodes.SHOW_MISSION_COMPLETED_DIALOG, 0, 0);
        }
    }

    /**
     * Native support extracted from TwoDwordAction::Clone @005410D0.
     */
    @Override
    public MapLightOverrideAction Clone() {
        return new MapLightOverrideAction(this);
    }
}
