package ua.millfreedom.rom2.model.action;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.Player;
import ua.millfreedom.rom2.model.QuestsStorage;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.visobj.BasicInnDialogVisualObject;
import ua.millfreedom.rom2.model.visobj.MapVisualObject;

/**
 * Native `ShortArrayBlobAction` packet id `0xBC` used to send the current inn quest-offer list.
 */
public class InnQuestsAction extends ShortArrayBlobAction {
    public static final int ACTION_ID = GameActionId.INN_QUESTS_ACTION_BC.id;
    public static final InnQuestsAction global = new InnQuestsAction();

    /**
     * Native support extracted from CServerApp::sendQuestListAction @00506526 and
     * Inn::openUnitSession @0052F813.
     */
    public InnQuestsAction() {
        super();
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from ShortArrayBlobAction::ShortArrayBlobAction @0050C745 and
     * CServerApp::sendQuestListAction @00506526.
     */
    public InnQuestsAction(InnQuestsAction from) {
        super();
        copyNativeObjectBufferFrom(from);
        ID.set(ACTION_ID);
    }

    /**
     * Native support extracted from CServerApp::sendQuestListAction @00506526 packet field writes.
     */
    public static InnQuestsAction prepareForQuestList(QuestsStorage questsStorage, Player player) {
        return PlayerQuestsAction.prepareQuestListAction(global, ACTION_ID, questsStorage, player);
    }

    /**
     * Native support extracted from ShortArrayBlobAction::Clone @00541940.
     */
    @Override
    public InnQuestsAction Clone() {
        return new InnQuestsAction(this);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00414801.
     */
    @Override
    public void handle(MapVisualObject mapVisualObject) {
        BasicInnDialogVisualObject innDialog = activeInnDialog();
        if (innDialog != null) {
            MapVisualObject.loadQuestListIntoStorage(innDialog.questsStorage, shortValueCount.get(), shortValues.get());
        }
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00414801 active `0x44C` child lookup.
     */
    private static BasicInnDialogVisualObject activeInnDialog() {
        if (Globals.mainWindow.pBasicInnDialogVisualObject.dialogActiveFlag != 0) {
            return Globals.mainWindow.pBasicInnDialogVisualObject;
        }
        if (Globals.mainWindow.pKaargInnDialogVisualObject.dialogActiveFlag != 0) {
            return Globals.mainWindow.pKaargInnDialogVisualObject;
        }
        if (Globals.mainWindow.pDruidInnDialogVisualObject.dialogActiveFlag != 0) {
            return Globals.mainWindow.pDruidInnDialogVisualObject;
        }
        return null;
    }

}
