package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.gameobj.CGameObject;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `UnitTokenAction` packet id `0x74` used to hide a tracked unit from the recipient.
 */
public class UnitHiddenAction extends UnitTokenAction {
    public static final int ACTION_ID = GameActionId.UNIT_HIDDEN_ACTION_74.id;
    public static final UnitHiddenAction global = new UnitHiddenAction();

    /**
     * Native support extracted from CServerApp::sendUnitVisibilityAction @005053A8 and
     * MapVisualObject::HandleGameAction @00415BDA.
     */
    public UnitHiddenAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CServerApp::sendUnitVisibilityAction @005053A8 and
     * MapVisualObject::HandleGameAction @00415BDA.
     */
    public UnitHiddenAction(UnitHiddenAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from CServerApp::sendUnitVisibilityAction @005053A8 and
     * UnitTokenAction::GetSize @00540F60.
     */
    @Override
    public UnitHiddenAction Clone() {
        return new UnitHiddenAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00415BDA.
     * Ported action-id case: `UnitHiddenAction`.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        CGameObject gameObject = mapVisualObject.getObjectByToken((short) (int) unitTokenId.get());
        if (gameObject != null) {
            ((CUnit) gameObject).unitFlags |= 0x80;
        }
    }
}
