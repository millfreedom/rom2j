package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;

import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.Diplomacy;
import ua.millfreedom.rom2.model.DiplomacyWrapper;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palettes;

import java.util.List;

import static ua.millfreedom.rom2.model.enums.MessageCodes.DIALOG_OK;
import static ua.millfreedom.rom2.text.DialogsText.DIPLOMACY_SETTINGS_145;
import static ua.millfreedom.rom2.text.DialogsText.PLAYER_ENEMY_ALLIANCE_VISIBLE_SILENT_79;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.TextTableId.DIALOGS;

/**
 * Native class: DiplomacySettingsDialogVisualObject.
 * Purpose: header dialog bound to diplomacy wrapper payload.
 */
public class DiplomacySettingsDialogVisualObject extends HeaderDialogVisualObject {
    public static final int NATIVE_SIZE = 0x7C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    //0x78
    public DiplomacyWrapper diplomacyWrapper;

    /**
     * Native: DiplomacySettingsDialogVisualObject::DiplomacySettingsDialogVisualObject @00444D7D.
     * Fully ported.
     */
    public DiplomacySettingsDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, DiplomacyWrapper diplomacyWrapper) {
        super(
                id,
                xLeft,
                yTop,
                xRight,
                yBottom,
                diplomacyWrapper.m_pDiplomacyArray,
                get(DIALOGS, PLAYER_ENEMY_ALLIANCE_VISIBLE_SILENT_79),
                1,
                get(DIALOGS, DIPLOMACY_SETTINGS_145)
        );
        this.diplomacyWrapper = diplomacyWrapper;
    }

    /**
     * vtbl +0x88: DiplomacySettingsDialogVisualObject::createDialogContent @00444DE0.
     * Fully ported.
     */
    @Override
    protected CVisualObject createDialogContent(Object payload, CRect contentRect) {
        contentRect.top = 0x50;
        DiplomacySettingsTextListVisualObject listControl = createDiplomacySettingsList(contentRect, diplomacyEntries(payload));
        addChild(listControl);
        listControl.configureVisibleRowsAndScrollbar();
        bindTitleHeaderToList(listControl);
        return listControl;
    }

    /**
     * vtbl +0x8C: DiplomacySettingsDialogVisualObject::OnHeaderDialogAction @00444EA3.
     * Fully ported.
     */
    @Override
    protected void onHeaderDialogAction(MessageCodes action) {
        if (action != DIALOG_OK) {
            return;
        }

        DiplomacySettingsTextListVisualObject diplomacyList = (DiplomacySettingsTextListVisualObject) getChildById(2);
        diplomacyList.commitSelectionControlsToDiplomacyArray();
    }

    /**
     * vtbl +0x44: DiplomacySettingsDialogVisualObject::setValue @00444EE6.
     * Fully ported.
     */
    @Override
    public void setValue(Object value) {
        relayoutWithDiplomacyWrapper((DiplomacyWrapper) value);
    }

    /**
     * Native support block for DiplomacySettingsDialogVisualObject slot `0x44` @00444EE6.
     * Fully ported.
     */
    public void relayoutWithDiplomacyWrapper(DiplomacyWrapper nextDiplomacyWrapper) {
        diplomacyWrapper = nextDiplomacyWrapper;
        payload = nextDiplomacyWrapper.m_pDiplomacyArray;

        CRect listRect = new CRect(0x28, 0x50, cRect.width() - 0x28, cRect.height() - 0x58);
        DiplomacySettingsTextListVisualObject diplomacyList = (DiplomacySettingsTextListVisualObject) getChildById(2);
        diplomacyList.restoreFullWidthAfterScrollbar();
        listRect.set(diplomacyList.getRect());

        if (diplomacyList.linkedChildId >= 0) {
            CVisualObject scrollbarChild = getChildById(diplomacyList.linkedChildId);
            if (scrollbarChild != null) {
                removeChild(scrollbarChild);
            }
        }
        removeAndDetachChild(diplomacyList);

        mouseInputTarget = null;
        keyboardInputTarget = null;
        previousMouseInputTarget = null;
        previousKeyboardInputTarget = null;

        DiplomacySettingsTextListVisualObject refreshedList = createDiplomacySettingsList(listRect, nextDiplomacyWrapper.m_pDiplomacyArray);
        addChild(refreshedList);
        refreshedList.configureVisibleRowsAndScrollbar();
        bindTitleHeaderToList(refreshedList);
    }

    /**
     * Native support block shared by DiplomacySettingsDialogVisualObject::createDialogContent @00444DE0
     * and DiplomacySettingsDialogVisualObject slot `0x44` @00444EE6.
     * Fully ported.
     */
    private DiplomacySettingsTextListVisualObject createDiplomacySettingsList(CRect listRect, List<Diplomacy> diplomacyArray) {
        CBitmapFont dialogFont = Globals.fonts.font1;
        return new DiplomacySettingsTextListVisualObject(
                2,
                new CRect(listRect),
                diplomacyArray,
                dialogFont,
                Palettes.grayDim,
                Palettes.gray,
                0
        );
    }

    /**
     * Native support boundary for the title-child link after DiplomacySettingsDialogVisualObject::createDialogContent @00444DE0
     * and DiplomacySettingsDialogVisualObject slot `0x44` @00444EE6.
     * Fully ported.
     */
    private void bindTitleHeaderToList(DiplomacySettingsTextListVisualObject listControl) {
        listControl.gameDialogControls = getChildById(-1);
    }

    /**
     * Native support for the typed wrapper array passed through HeaderDialogVisualObject payload @00444D7D.
     * Fully ported.
     */
    @SuppressWarnings("unchecked")
    private static List<Diplomacy> diplomacyEntries(Object payload) {
        return (List<Diplomacy>) payload;
    }
}
