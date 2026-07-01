package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.CSprite256;
import ua.millfreedom.rom2.model.ScriptDataSupport;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.res.Resources;

import java.util.List;

import static ua.millfreedom.rom2.model.enums.MessageCodes.DIALOG_OK;
import static ua.millfreedom.rom2.res.Constants.GRAPHICS;
import static ua.millfreedom.rom2.res.Constants.INTERFACE;
import static ua.millfreedom.rom2.res.Constants.SUBOBJ_256;
import static ua.millfreedom.rom2.text.DialogsText.OK_0;
import static ua.millfreedom.rom2.text.DialogsText.QUEST_OBJECTIVES_AND_HINTS_68;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.TextTableId.DIALOGS;

/**
 * Native class: QuestObjectivesHeaderDialogVisualObject.
 * Purpose: header-dialog specialization for quest objectives and hints.
 */
public class QuestObjectivesHeaderDialogVisualObject extends HeaderDialogVariantVisualObject {
    public static final int NATIVE_SIZE = 0x78; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    /**
     * Native: QuestObjectivesHeaderDialogVisualObject::QuestObjectivesHeaderDialogVisualObject @00444325.
     * Fully ported.
     */
    public QuestObjectivesHeaderDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        super(id, xLeft, yTop, xRight, yBottom, ScriptDataSupport.briefingText, get(DIALOGS, QUEST_OBJECTIVES_AND_HINTS_68), 0xFFFF);
    }

    /**
     * vtbl +0x78: QuestObjectivesHeaderDialogVisualObject::Initialize @00444393.
     * Fully ported.
     */
    @Override
    public void initialize() {
        super.initialize();

        CBitmapFont dialogFont = Globals.fonts.font1;
        int dialogWidth = cRect.width();
        CRect formatRect = new CRect(0, 0, dialogWidth - 0x80, 0x1E0);
        CVisualObject briefingChild = getChildById(2);
        int nextTop = briefingChild.getRect().bottom + 0x10;

        for (int objectiveIndex = 0; objectiveIndex < ScriptDataSupport.subobjectives.size(); objectiveIndex++) {
            String objectiveText = ScriptDataSupport.subobjectives.get(objectiveIndex);
            List<String> formattedRows = dialogFont.formatText(formatRect, objectiveText);
            int objectiveFlags = Globals.scenarioLib.getVar(objectiveIndex + 0x2F0);
            if (objectiveFlags != 0) {
                Palette16 textPalette = resolveSubobjectiveTextPalette(objectiveFlags);
                LinkedPaletteVisualObject statusIcon = createSubobjectiveStatusIcon(objectiveFlags, nextTop);
                int objectiveBottom = nextTop + (dialogFont.getFrameHeight() + 4) * formattedRows.size();
                WrappedTextSourceListVisualObject objectiveTextChild = new WrappedTextSourceListVisualObject(
                        nextTop,
                        0x50,
                        nextTop,
                        dialogWidth - 0x30,
                        objectiveBottom,
                        objectiveText,
                        dialogFont,
                        textPalette,
                        0
                );
                addChild(objectiveTextChild);
                if (statusIcon != null) {
                    addChild(statusIcon);
                }

                int heightIncrement = 10 + (dialogFont.getFrameHeight() + 4) * formattedRows.size();
                nextTop += heightIncrement;
                cRect.bottom += heightIncrement;
            }
        }

        cRect.bottom += 0x28;
        centerOnScreen(Globals.screenRect.right, Globals.screenRect.bottom);

        int buttonTop = cRect.height() - 0x3C;
        int buttonBottom = cRect.height() - 0x24;
        CRect okButtonRect = new CRect((dialogWidth / 2) - 0x30, buttonTop, (dialogWidth / 2) + 0x30, buttonBottom);
        addChild(new CommandButtonVisualObject(
                4,
                okButtonRect,
                get(DIALOGS, OK_0),
                dialogFont,
                null,
                DIALOG_OK,
                0,
                null
        ));
    }

    /**
     * Native subobjective status-palette branch inside QuestObjectivesHeaderDialogVisualObject::Initialize @00444393.
     */
    private static Palette16 resolveSubobjectiveTextPalette(int objectiveFlags) {
        if ((objectiveFlags & 2) != 0) {
            return Palettes.gray;
        }
        if ((objectiveFlags & 4) != 0) {
            return Palettes.redish;
        }
        if ((objectiveFlags & 1) != 0) {
            return Palettes.grayDim;
        }
        return null;
    }

    /**
     * Native subobjective icon allocation branch inside QuestObjectivesHeaderDialogVisualObject::Initialize @00444393.
     */
    private static LinkedPaletteVisualObject createSubobjectiveStatusIcon(int objectiveFlags, int top) {
        int frameSelector;
        if ((objectiveFlags & 2) != 0) {
            frameSelector = 0x0B;
        } else if ((objectiveFlags & 4) != 0) {
            frameSelector = 0x0C;
        } else if ((objectiveFlags & 1) != 0) {
            frameSelector = 10;
        } else {
            return null;
        }

        CSprite256 sprite = new CSprite256(Resources.path(GRAPHICS, INTERFACE, SUBOBJ_256));
        sprite.initPalette(1, 1, 0);
        return new LinkedPaletteVisualObject(
                top + 1,
                0x30,
                top - 3,
                0x48,
                top + 0x15,
                sprite,
                frameSelector
        );
    }
}
