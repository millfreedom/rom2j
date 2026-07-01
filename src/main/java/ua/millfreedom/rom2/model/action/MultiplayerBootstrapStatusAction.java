package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `TwoDwordAction` packet id `0x0B` handled by MapVisualObject::HandleGameAction @00415C67.
 */
public class MultiplayerBootstrapStatusAction extends TwoDwordAction {
    public static final int ACTION_ID = GameActionId.TWO_DWORD_ACTION_0B.id;
    public static final MultiplayerBootstrapStatusAction global = new MultiplayerBootstrapStatusAction();

    /**
     * Native support extracted from TwoDwordAction::TwoDwordAction @0050BDA2 and MapVisualObject::HandleGameAction @00415C67.
     */
    public MultiplayerBootstrapStatusAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from TwoDwordAction::TwoDwordAction @0050BDD5 and MapVisualObject::HandleGameAction @00415C67.
     */
    public MultiplayerBootstrapStatusAction(MultiplayerBootstrapStatusAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00415C67.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        super.handle(mapVisualObject);
    }

    /**
     * Native support extracted from TwoDwordAction::Clone @005410D0.
     */
    @Override
    public MultiplayerBootstrapStatusAction Clone() {
        return new MultiplayerBootstrapStatusAction(this);
    }
}
