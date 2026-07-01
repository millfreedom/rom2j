package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.gameobj.CGameObject;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `UnitTokenAction` packet id `0x69` used to reveal a tracked unit to the recipient.
 */
public class UnitShownAction extends UnitTokenAction {
    public static final int ACTION_ID = GameActionId.UNIT_SHOWN_ACTION_69.id;
    public static final UnitShownAction global = new UnitShownAction();

    /**
     * Native support extracted from CServerApp::sendUnitVisibilityAction @005053A8 and
     * MapVisualObject::HandleGameAction @00415BDA.
     */
    public UnitShownAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CServerApp::sendUnitVisibilityAction @005053A8 and
     * MapVisualObject::HandleGameAction @00415BDA.
     */
    public UnitShownAction(UnitShownAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from CServerApp::sendUnitVisibilityAction @005053A8 and
     * UnitTokenAction::GetSize @00540F60.
     */
    @Override
    public UnitShownAction Clone() {
        return new UnitShownAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00415BDA.
     * Ported action-id case: `UnitShownAction`.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        CGameObject gameObject = mapVisualObject.getObjectByToken((short) (int) unitTokenId.get());
        if (gameObject != null) {
            ((CUnit) gameObject).unitFlags &= ~0x80;
        }
    }
}
