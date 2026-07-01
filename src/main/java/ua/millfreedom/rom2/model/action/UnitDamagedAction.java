package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native action class `FixedDwordAction73` / packet id `0x73` used by
 * `CServerApp::notifyStateChanged @00503672`
 * to send unit token HP updates.
 */
public class UnitDamagedAction extends CGameAction {
    public static final int ACTION_ID = GameActionId.UNIT_DAMAGED_ACTION_73.id;
    public static final UnitDamagedAction global = new UnitDamagedAction();
    private static final int NATIVE_COPY_SIZE = 5;

    //0x0A
    public final Property<Integer> unitTokenId = u16(BODY_OFFSET);
    //0x0C
    public final Property<Integer> hitPoints = u16(BODY_OFFSET + Short.BYTES);

    /**
     * Native support extracted from FixedDwordAction73::FixedDwordAction73 @0050CD0E,
     * CServerApp::notifyStateChanged @00503672, and
     * MapVisualObject::HandleGameAction @00412826.
     * Fully ported.
     */
    public UnitDamagedAction() {
        super();
        unitTokenId.set(0);
        hitPoints.set(0);
    }

    /**
     * Native support extracted from FixedDwordAction73::FixedDwordAction73 @0050CD2D and
     * CServerApp::notifyStateChanged @00503672.
     * Fully ported.
     */
    public UnitDamagedAction(UnitDamagedAction from) {
        super();
        PutSlice(ID_OFFSET, from.GetSlice(ID_OFFSET, NATIVE_COPY_SIZE), 0, NATIVE_COPY_SIZE);
    }

    /**
     * Native support extracted from CServerApp::notifyUnitHitPointsChanged @00504B1D and
     * CServerApp::notifyStateChanged @00503672 unit branch.
     */
    public static UnitDamagedAction createForUnitHitPointsChanged(Unit unit, Player player) {
        UnitDamagedAction action = global;
        action.ID.set(ACTION_ID);
        action.playerID.set(player.playerId);
        action.unitTokenId.set(unit.idFull);
        action.hitPoints.set(unit.m_nHP);
        return action;
    }

    /**
     * vtbl +0x04: FixedDwordAction73::Clone @00541C50.
     * Fully ported.
     */
    @Override
    public UnitDamagedAction Clone() {
        return new UnitDamagedAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00412826.
     * Partial port. Java keeps the recovered hurt-response sound, HP writeback, selection-panel refresh, and
     * selection-dirty mark.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        CUnit unit = (CUnit) mapVisualObject.getObjectByToken((short) (int) unitTokenId.get());
        if (unit != null) {
            short newHitPoints = (short) (int) hitPoints.get();
            mapVisualObject.addFloatingUnitTextForHpLoss(unit, newHitPoints);
            playNativeHurtResponse(unit, newHitPoints);
            unit.HP = newHitPoints;
            if (mapVisualObject.getPrimarySelectedObject() == unit) {
                Globals.mainWindow.pSelectionInfoPanelVisualObject.onMessage(MessageCodes.NOTIFY_MAP_CONTEXT_CHANGED, 0, 0);
                Globals.mainWindow.pSideStatusVisualObject.onMessage(MessageCodes.NOTIFY_MAP_CONTEXT_CHANGED, 0, 0);
            }
            unit.m_bSelectionDirty = 1;
        }
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00412A41 hurt-response branch.
     */
    private static void playNativeHurtResponse(CUnit unit, short newHitPoints) {
        short oldHitPoints = unit.HP;
        if (oldHitPoints == newHitPoints) {
            unit.playHurtResponseSound(0);
            return;
        }
        if (oldHitPoints <= -10) {
            return;
        }
        if (oldHitPoints < unit.MaxHP / 2) {
            unit.playHurtResponseSound(2);
            return;
        }
        unit.playHurtResponseSound(1);
    }

    /**
     * vtbl +0x10: FixedDwordAction73::getWireSize @00541CD0.
     * Fully ported.
     */
    @Override
    public int GetPayloadSize() {
        return 0x05;
    }
}
