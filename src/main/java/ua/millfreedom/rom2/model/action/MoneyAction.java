package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.TokenEntry;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;
import ua.millfreedom.rom2.model.window.DialogsMaskFlag;

import java.util.List;

import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_GOLD_89;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_PICKED_UP_88;

/**
 * Native `TwoDwordAction` packet id `0x67` used to mirror the player's current gold total back to the client.
 */
public class MoneyAction extends TwoDwordAction {
    public static final int ACTION_ID = GameActionId.MONEY_ACTION_67.id;
    public static final MoneyAction global = new MoneyAction();
    // Native timed pickup line lifetime used by MapVisualObject::HandleGameAction @004149B3.
    private static final int PICKUP_LINE_LIFETIME_MS = 3000;

    /**
     * Native support extracted from Player::AdjustGoldAndNotify @00516238 and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public MoneyAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from Player::AdjustGoldAndNotify @00516238 and
     * MapVisualObject::HandleGameAction @0040D9B2.
     */
    public MoneyAction(MoneyAction from) {
        super();
        copyNativeObjectBufferFrom(from);
    }

    /**
     * Native support extracted from Player::AdjustGoldAndNotify @00516238 and
     * TwoDwordAction::Clone @005410D0.
     */
    @Override
    public MoneyAction Clone() {
        return new MoneyAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @004149B3.
     * Partial port. Java keeps the recovered hero-inventory gold writeback through `CPlayer +0x10`.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        int oldGold = mapVisualObject.currentPlayer.gold;
        int newGold = firstPayloadDword.get();
        int context = secondPayloadDword.get();
        addPickedUpGoldLine(mapVisualObject, oldGold, newGold, context);
        mapVisualObject.currentPlayer.gold = newGold;
        syncHeroInventoryMoneyEntry(mapVisualObject);
        mapVisualObject.notifySelectionUi();
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @004149B3 MONEY_ACTION_67 gold-gain line.
     */
    private static void addPickedUpGoldLine(MapVisualObject mapVisualObject, int oldGold, int newGold, int context) {
        if (DialogsMaskFlag.isExactly(Globals.mainWindow.dialogsMask, GAMEPLAY)
                && oldGold < newGold
                && context == 0) {
            mapVisualObject.gameListControl.addTimedLine(
                    get(MAIN_PICKED_UP_88) + " " + (newGold - oldGold) + " " + get(MAIN_GOLD_89),
                    Palettes.messageDim(),
                    PICKUP_LINE_LIFETIME_MS
            );
        }
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @004149B3 MONEY_ACTION_67 selected-unit tail.
     */
    private static void syncHeroInventoryMoneyEntry(MapVisualObject mapVisualObject) {
        CUnit heroUnit = mapVisualObject.getSelectedCUnit();
        if (heroUnit == null) {
            return;
        }

        Globals.mainWindow.m_GameSession.refreshSavedCharacterProgress();
        List<TokenEntry> inventoryEntries = heroUnit.tokenEntries;
        if (!inventoryEntries.isEmpty()) {
            TokenEntry lastEntry = inventoryEntries.getLast();
            if (lastEntry.isMoneyEntry()) {
                lastEntry.quantity = mapVisualObject.currentPlayer.gold;
            }
        }
    }

}
