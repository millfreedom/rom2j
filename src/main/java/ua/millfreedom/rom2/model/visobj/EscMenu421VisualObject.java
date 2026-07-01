package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.SavedGameFiles;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.sound.MusicPlayer;

import static ua.millfreedom.rom2.model.enums.MessageCodes.DIPLOMACY;
import static ua.millfreedom.rom2.model.enums.MessageCodes.END_QUEST;
import static ua.millfreedom.rom2.model.enums.MessageCodes.GAME_OPTIONS;
import static ua.millfreedom.rom2.model.enums.MessageCodes.LOAD_GAME;
import static ua.millfreedom.rom2.model.enums.MessageCodes.QUEST_OBJECTIVES;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RETURN_TO_GAME;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SAVE_GAME;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SOUND_OPTIONS;
import static ua.millfreedom.rom2.text.DialogsText.*;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.TextTableId.DIALOGS;

/**
 * Native class: EscMenu421VisualObject.
 * Purpose: in-game esc menu variant with save/load/options/diplomacy/return actions.
 */
public class EscMenu421VisualObject extends MenuListDialogVisualObject {
    public static final int NATIVE_SIZE = 0x7C; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    /**
     * Native: EscMenu421VisualObject::EscMenu421VisualObject @004400D7.
     * Fully ported.
     */
    public EscMenu421VisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, Object image, int field0x64, CRect rect) {
        super(id, xLeft, yTop, xRight, yBottom, image, field0x64, rect);
        int sessionMode = resolveSessionMode();
        CBitmapFont dialogFont = Globals.fonts.font1;

        MenuListCommandButtonVisualObject saveGameButton = createEscButton(1, get(DIALOGS, SAVE_GAME_34), SAVE_GAME, 'S', dialogFont);
        if (sessionMode == 0 || sessionMode == 1) {
            saveGameButton.setStateFlag(1, 0);
        }
        appendItem(saveGameButton, 0x1E);

        if (sessionMode == 2) {
            MenuListCommandButtonVisualObject loadGameButton = createEscButton(2, get(DIALOGS, LOAD_GAME_35), LOAD_GAME, 'L', dialogFont);
            if (!SavedGameFiles.saveFileExists()) {
                loadGameButton.setStateFlag(1, 0);
            }
            appendItem(loadGameButton, 0x1E);
        } else {
            MenuListCommandButtonVisualObject diplomacyButton = createEscButton(7, get(DIALOGS, DIPLOMACY_76), DIPLOMACY, 'D', dialogFont);
            appendItem(diplomacyButton, 0x1E);
        }

        MenuListCommandButtonVisualObject gameOptionsButton = createEscButton(3, get(DIALOGS, GAME_OPTIONS_36), GAME_OPTIONS, 'O', dialogFont);
        appendItem(gameOptionsButton, 0x1E);

        MenuListCommandButtonVisualObject soundOptionsButton = createEscButton(4, get(DIALOGS, SOUND_OPTIONS_37), SOUND_OPTIONS, 'N', dialogFont);
        if (resolveSoundDriverState() == MusicPlayer.PLAYBACK_STATE_UNAVAILABLE) {
            soundOptionsButton.setStateFlag(1, 0);
        }
        appendItem(soundOptionsButton, 0x1E);

        MenuListCommandButtonVisualObject questObjectivesButton = createEscButton(5, get(DIALOGS, QUEST_OBJECTIVES_38), QUEST_OBJECTIVES, 'M', dialogFont);
        appendItem(questObjectivesButton, 0x1E);
        if (sessionMode != 2) {
            questObjectivesButton.setStateFlag(1, 0);
        }

        MenuListCommandButtonVisualObject endQuestButton = createEscButton(6, get(DIALOGS, END_QUEST_39), END_QUEST, 'E', dialogFont);
        appendItem(endQuestButton, 0x1E);

        MenuListCommandButtonVisualObject returnToGameButton = createEscButton(8, get(DIALOGS, RETURN_TO_GAME_40), RETURN_TO_GAME, 'R', dialogFont);
        appendItem(returnToGameButton, 0x1E);
    }

    /**
     * Native support extracted from EscMenu421VisualObject::EscMenu421VisualObject @004400D7.
     */
    private MenuListCommandButtonVisualObject createEscButton(int buttonId, String caption, MessageCodes message, char hotKey, CBitmapFont dialogFont) {
        return new MenuListCommandButtonVisualObject(
                buttonId,
                caption,
                dialogFont,
                null,
                message,
                hotKey,
                null
        );
    }

    /**
     * Native owner: CMainWindow::sessionMode reads in EscMenu421VisualObject::EscMenu421VisualObject @004400D7.
     * not ported.
     */
    private static int resolveSessionMode() {
        return Globals.mainWindow.sessionMode;
    }

    /**
     * Native support extracted from EscMenu421VisualObject::EscMenu421VisualObject @004400D7 and MusicPlayer::GetPlaybackState @00450150.
     */
    private static int resolveSoundDriverState() {
        return Globals.mainWindow.musicPlayer.getPlaybackState();
    }
}
