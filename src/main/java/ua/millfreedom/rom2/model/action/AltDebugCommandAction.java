package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.model.enums.GameActionId;

/**
 * Native `TwoDwordAction` packet id `0x46` kind `0x80` used for player Alt+letter debug commands.
 */
public class AltDebugCommandAction extends TwoDwordAction {
    public static final int ACTION_ID = GameActionId.UPDATE_BATTLE_PREFERENCE_ACTION_46.id;
    public static final AltDebugCommandAction global = new AltDebugCommandAction();

    /**
     * Native support extracted from MapVisualObject::sendAltDebugCommand @0041A672,
     * CMainWindow::OnSysKeyDown @0048509B, and MissionScriptRuntime::handleAltDebugCommand @00578910.
     */
    public AltDebugCommandAction() {
        super();
        ID.set(ACTION_ID);
        firstPayloadDword.set(0x80);
    }

    /**
     * Native support extracted from MapVisualObject::sendAltDebugCommand @0041A672,
     * CMainWindow::OnSysKeyDown @0048509B, and MissionScriptRuntime::handleAltDebugCommand @00578910.
     */
    public AltDebugCommandAction(AltDebugCommandAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from MapVisualObject::sendAltDebugCommand @0041A672,
     * CMainWindow::OnSysKeyDown @0048509B, and MissionScriptRuntime::handleAltDebugCommand @00578910.
     */
    @Override
    public AltDebugCommandAction Clone() {
        return new AltDebugCommandAction(this);
    }

}
