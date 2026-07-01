package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `TwoDwordAction` packet id `0x46` kind `4` used to select or clear an autocast spell for a unit token.
 */
public class ToggleUnitAutocastSpellAction extends TwoDwordAction {
    public static final int ACTION_ID = GameActionId.UPDATE_BATTLE_PREFERENCE_ACTION_46.id;
    public static final ToggleUnitAutocastSpellAction global = new ToggleUnitAutocastSpellAction();

    /**
     * Native support extracted from MapVisualObject::toggleAutoCastSpellBySpellId @0041A727,
     * SpellPanelVisualObject::OnKeyDown @004C74B1, and GameServer::handleServerGameAction @004F515D.
     */
    public ToggleUnitAutocastSpellAction() {
        super();
        ID.set(ACTION_ID);
        firstPayloadDword.set(4);
    }

    /**
     * Native support extracted from MapVisualObject::toggleAutoCastSpellBySpellId @0041A727,
     * SpellPanelVisualObject::OnKeyDown @004C74B1, and GameServer::handleServerGameAction @004F515D.
     */
    public ToggleUnitAutocastSpellAction(ToggleUnitAutocastSpellAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from MapVisualObject::toggleAutoCastSpellBySpellId @0041A727,
     * SpellPanelVisualObject::OnKeyDown @004C74B1, and GameServer::handleServerGameAction @004F515D.
     */
    @Override
    public ToggleUnitAutocastSpellAction Clone() {
        return new ToggleUnitAutocastSpellAction(this);
    }

}
