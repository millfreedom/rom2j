package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.Sack;
import ua.millfreedom.rom2.model.gameobj.CBackPack;
import ua.millfreedom.rom2.model.gameobj.CGameObject;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;

/**
 * Native action class `Fixed8ByteAction7A` / packet id `0x7A` used by
 * `CServerApp::notifyStateChanged @00503672`
 * to create or refresh the client-side `CBackPack` visual for a sack token.
 */
public class SackAction extends CGameAction {
    public static final int ACTION_ID = GameActionId.SACK_ACTION_7A.id;
    public static final SackAction global = new SackAction();

    //0x0A
    public final Property<Integer> sackTokenId = u16(BODY_OFFSET);
    //0x0D
    public final Property<Integer> sackType = u8(BODY_OFFSET + 3);
    //0x0E
    public final Property<Integer> packedXdX = u16(BODY_OFFSET + 4);
    //0x10
    public final Property<Integer> packedYdY = u16(BODY_OFFSET + 6);

    /**
     * Native: Fixed8ByteAction7A::Fixed8ByteAction7A @0050C2A3.
     * Fully ported.
     */
    public SackAction() {
        super();
    }

    /**
     * Native: Fixed8ByteAction7A::Fixed8ByteAction7A @0050C2C2.
     * Fully ported.
     */
    public SackAction(SackAction from) {
        super();
        int copySize = GetPayloadSize();
        PutSlice(ID_OFFSET, from.GetSlice(ID_OFFSET, copySize), 0, copySize);
    }

    /**
     * Native support extracted from CServerApp::notifyStateChanged @00503672 sack branch.
     */
    public static SackAction createForBuildingStateChanged(Sack sack, Player player) {
        SackAction action = global;
        action.ID.set(ACTION_ID);
        action.playerID.set(player.playerId);
        action.sackTokenId.set(sack.idFull);
        action.sackType.set(sack.computeSackTypeByte());
        action.packedXdX.set(sack.m_pTargetHandle.packXdX());
        action.packedYdY.set(sack.m_pTargetHandle.packYdY());
        return action;
    }

    /**
     * vtbl +0x04: Fixed8ByteAction7A::Clone @005413E0.
     * Fully ported.
     */
    @Override
    public SackAction Clone() {
        return new SackAction(this);
    }

    /**
     * vtbl +0x10: Fixed8ByteAction7A::getWireSize @00541460.
     * Fully ported.
     */
    @Override
    public int GetPayloadSize() {
        return 0x09;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00414B10.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        int tokenId = sackTokenId.get();
        int sackType1 = sackType.get();
        int packedXdX1 = packedXdX.get();
        int packedYdY1 = packedYdY.get();
        CGameObject object = mapVisualObject.getObjectByToken((short) tokenId);
        if (object == null) {
            CBackPack backpack = new CBackPack();
            backpack.initializeBackpackVisualState(
                    tokenId,
                    1,
                    packedXdX1,
                    packedYdY1,
                    0,
                    0,
                    0,
                    0,
                    1
            );
            backpack.tileX = backpack.location2.x >> 8;
            backpack.tileY = backpack.location2.y >> 8;
            backpack.pMapVisualObject = mapVisualObject;
            mapVisualObject.putScenarioObject((short) tokenId, backpack);
            backpack.cPlayer = mapVisualObject.currentPlayer;
            object = backpack;
        }
        object.type = Math.min(sackType1, 5);
        if (GAMEPLAY.isSetIn(Globals.mainWindow.dialogsMask) && mapVisualObject.mapDescriptor != null) {
            object.refreshMapDerivedState();
        }
    }

}
