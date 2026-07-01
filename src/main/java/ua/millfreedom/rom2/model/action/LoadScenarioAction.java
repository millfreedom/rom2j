package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `ChatTextAction` packet id `0x06` used to push the scenario/map file name that the client must load.
 */
public class LoadScenarioAction extends ChatTextAction {
    public static final int ACTION_ID = GameActionId.LOAD_SCENARIO_ACTION_06.id;
    public static final LoadScenarioAction global = new LoadScenarioAction();

    /**
     * Native support extracted from GameServer::FUN_004F1D9C @004F1D9C and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public LoadScenarioAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from GameServer::FUN_004F1D9C @004F1D9C and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public LoadScenarioAction(LoadScenarioAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from GameServer::FUN_004F1D9C @004F1D9C and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    @Override
    public LoadScenarioAction Clone() {
        return new LoadScenarioAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040DACC.
     * Ported action-id case: `LoadScenarioAction` packet id `0x06`.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        if (mapVisualObject.mapDescriptor == null) {
            mapVisualObject.loadScenarioMap(text.get());
        }
        if (mapVisualObject.mapDescriptor == null) {
            Globals.multiplayerBootstrapStatusWord = 0x1004;
            return;
        }
        Globals.mapLoadActionStatus = 0;
    }

}
