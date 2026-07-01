package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.enums.SfxSounds;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.sound.Sound;
import ua.millfreedom.rom2.model.sound.MusicPlayer;
import ua.millfreedom.rom2.model.sound.SoundManager;
import ua.millfreedom.rom2.model.sound.SoundPreferences;
import ua.millfreedom.rom2.text.DialogsText;
import ua.millfreedom.rom2.text.TunesText;

import java.awt.*;
import java.util.List;
import java.util.Locale;

import static ua.millfreedom.rom2.model.enums.MessageCodes.DIALOG_OK;
import static ua.millfreedom.rom2.model.enums.MessageCodes.MUSIC_PLAYBACK_PROGRESS_NOTIFICATION;
import static ua.millfreedom.rom2.model.enums.MessageCodes.POST_SETUP_SLIDER_RELEASED;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RESET_SELECTION_GRID;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SOUND_OPTIONS_CONFIRM;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SOUND_OPTIONS_PLAY_MUSIC_PREVIEW;
import static ua.millfreedom.rom2.model.enums.MessageCodes.SOUND_OPTIONS_STOP_MUSIC_PREVIEW;
import static ua.millfreedom.rom2.model.enums.MessageCodes.TEXT_LIST_SELECTION_CHANGED;
import static ua.millfreedom.rom2.model.window.windowproc.handlers.CMainWindowWindowProcSupport.readMessageInt;
import static ua.millfreedom.rom2.text.DialogsText.MUSIC_IS_NOT_AVAILABLE_23;
import static ua.millfreedom.rom2.text.DialogsText.MUSIC_OPTIONS_ARE_NOT_AVAILABLE_NOW_75;
import static ua.millfreedom.rom2.text.DialogsText.MUSIC_VOLUME_16;
import static ua.millfreedom.rom2.text.DialogsText.MUSIC_VOLUME_19;
import static ua.millfreedom.rom2.text.DialogsText.OK_0;
import static ua.millfreedom.rom2.text.DialogsText.PLAY_12;
import static ua.millfreedom.rom2.text.DialogsText.PLAY_13;
import static ua.millfreedom.rom2.text.DialogsText.RETURN_TO_GAME_8;
import static ua.millfreedom.rom2.text.DialogsText.SELECT_TRACK_11;
import static ua.millfreedom.rom2.text.DialogsText.SFX_VOLUME_20;
import static ua.millfreedom.rom2.text.DialogsText.SFX_VOLUME_17;
import static ua.millfreedom.rom2.text.DialogsText.SPEECH_VOLUME_21;
import static ua.millfreedom.rom2.text.DialogsText.SOUND_OPTIONS_7;
import static ua.millfreedom.rom2.text.DialogsText.SPEECH_VOLUME_18;
import static ua.millfreedom.rom2.text.DialogsText.STOP_14;
import static ua.millfreedom.rom2.text.DialogsText.STOP_15;
import static ua.millfreedom.rom2.text.DialogsText.TRACKS_143;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.TextTableId.DIALOGS;
import static ua.millfreedom.rom2.text.TextTableId.TUNES;

/**
 * Native class: SoundPreferencesDialogVisualObject.
 * Purpose: centered sound-settings dialog with playlist selection and three volume sliders.
 */
public class SoundPreferencesDialogVisualObject extends CenteredDialogVisualObject {
    private static final int SOUND_OPTIONS_HEADER_ID = 0x22B;
    private static final int TRACKS_HEADER_ID = 0x22D;
    private static final int RETURN_TO_GAME_BUTTON_ID = 1;
    private static final int ACKNOWLEDGEMENT_RANDOM_ORDER_LIST_ID = 0x28;
    private static final int SOUND_RANDOM_SELECTION_MESSAGE_ID = 2;
    private static final int TRACK_LIST_ID = 3;
    private static final int MUSIC_PLAY_BUTTON_ID = 4;
    private static final int MUSIC_STOP_BUTTON_ID = 5;
    private static final int MUSIC_VOLUME_LABEL_ID = 0x1A;
    private static final int MUSIC_VOLUME_SLIDER_ID = 6;
    private static final int SFX_VOLUME_LABEL_ID = 0x1B;
    private static final int SFX_VOLUME_SLIDER_ID = 7;
    private static final int SPEECH_VOLUME_LABEL_ID = 0x1C;
    private static final int SPEECH_VOLUME_SLIDER_ID = 8;
    private static final int TRACK_SCROLLBAR_ID = 10;
    private static final int ACKNOWLEDGEMENTS_TEXT_INDEX = 0xA5;
    private static final int SPEECH_PREVIEW_SOUND_INDEX = 1;
    private static final int MUSIC_STOP_FADE_DELAY_MS = 2000;
    private static final int MUSIC_STOP_FADE_DURATION_MS = 8000;
    private static final byte DEFAULT_PREVIEW_PRIORITY = (byte) 0xDC;

    public static final int NATIVE_SIZE = 0x74; // VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    //0x68
    public MusicPlayer musicPlayer;
    //0x6c
    public int selectedTrackIndex;
    //0x70
    public SoundPreferences soundPreferences;

    /**
     * Native: SoundPreferencesDialogVisualObject::SoundPreferencesDialogVisualObject @0043CACB.
     * Fully ported.
     */
    public SoundPreferencesDialogVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            Object handler,
            SoundPreferences soundPreferences
    ) {
        super(id, xLeft, yTop, xRight, yBottom, handler);
        this.soundPreferences = soundPreferences;
    }

    /**
     * vtbl +0x78: SoundPreferencesDialogVisualObject::Initialize @0043CB0D.
     * Java-normalized for 0..100 volume slider values.
     */
    @Override
    public void initialize() {
        if (!children.isEmpty()) {
            return;
        }

        refreshMusicPlayer();
        selectedTrackIndex = 0;
        CBitmapFont dialogFont = Globals.fonts.font1;
        int dialogWidth = cRect.width();

        DialogWindowVisualObject soundOptionsHeader = new DialogWindowVisualObject(
                SOUND_OPTIONS_HEADER_ID,
                0x28,
                0x14,
                dialogWidth - 0x28,
                0x2D,
                get(DIALOGS, SOUND_OPTIONS_7),
                dialogFont,
                Palettes.grayDim,
                2
        );
        addChild(soundOptionsHeader);

        DialogWindowVisualObject tracksHeader = new DialogWindowVisualObject(
                TRACKS_HEADER_ID,
                0x28,
                0x3C,
                dialogWidth - 0x28,
                0x4E,
                get(DIALOGS, TRACKS_143),
                dialogFont,
                Palettes.grayDim,
                0
        );
        addChild(tracksHeader);

        CommandButtonVisualObject returnToGameButton = new CommandButtonVisualObject(
                RETURN_TO_GAME_BUTTON_ID,
                0x28,
                0x122,
                0xFC,
                0x13A,
                get(DIALOGS, OK_0),
                dialogFont,
                null,
                SOUND_OPTIONS_CONFIRM,
                0,
                get(DIALOGS, RETURN_TO_GAME_8)
        );
        returnToGameButton.setStateFlag(0x10, 1);
        addChild(returnToGameButton);

        StringListVariantAVisualObject randomOrderList = new StringListVariantAVisualObject(
                ACKNOWLEDGEMENT_RANDOM_ORDER_LIST_ID,
                0x28,
                0xBE,
                0xFC,
                0xD6,
                dialogFont,
                Palettes.grayDim,
                resolveAcknowledgementRowText()
        );
        randomOrderList.addRow(resolveAcknowledgementRowText());
        randomOrderList.setSelectionValue(Globals.gamePreferences.acknowledgement);
        if (isAcknowledgementToggleDisabledInNetworkSession()) {
            randomOrderList.setStateFlag(1, 0);
        }
        addChild(randomOrderList);

        TextListVisualObject trackList = new TextListVisualObject(
                TRACK_LIST_ID,
                new CRect(0x28, 0x50, dialogWidth - 0x40, 0xAA),
                dialogFont,
                Palettes.grayDim,
                Palettes.gray,
                TRACK_SCROLLBAR_ID,
                get(DIALOGS, SELECT_TRACK_11)
        );
        trackList.selectedRow = selectedTrackIndex;
        populateTrackRows(trackList);
        addChild(trackList);
        trackList.gameDialogControls = tracksHeader;

        CRect trackListRect = trackList.getRect();
        PostSetupVisualObject trackScrollbar = new PostSetupVisualObject(
                TRACK_SCROLLBAR_ID,
                trackListRect.right,
                trackListRect.top,
                trackListRect.right + 0x18,
                trackListRect.bottom,
                null
        );
        addChild(trackScrollbar);

        CommandButtonVisualObject playButton = new CommandButtonVisualObject(
                MUSIC_PLAY_BUTTON_ID,
                0x28,
                0x100,
                0x8C,
                0x118,
                get(DIALOGS, PLAY_12),
                dialogFont,
                null,
                SOUND_OPTIONS_PLAY_MUSIC_PREVIEW,
                0,
                get(DIALOGS, PLAY_13)
        );
        addChild(playButton);
        disableMusicControlIfNeeded(playButton);

        CommandButtonVisualObject stopButton = new CommandButtonVisualObject(
                MUSIC_STOP_BUTTON_ID,
                0x96,
                0x100,
                0xFC,
                0x118,
                get(DIALOGS, STOP_14),
                dialogFont,
                null,
                SOUND_OPTIONS_STOP_MUSIC_PREVIEW,
                0,
                get(DIALOGS, STOP_15)
        );
        addChild(stopButton);
        disableMusicControlIfNeeded(stopButton);

        stopButton.upNeighbor = randomOrderList;
        playButton.upNeighbor = randomOrderList;
        randomOrderList.downNeighbor = playButton;
        stopButton.downNeighbor = returnToGameButton;
        playButton.downNeighbor = returnToGameButton;
        returnToGameButton.upNeighbor = playButton;
        playButton.rightNeighbor = stopButton;
        stopButton.leftNeighbor = playButton;

        DialogWindowVisualObject musicVolumeLabel = new DialogWindowVisualObject(
                MUSIC_VOLUME_LABEL_ID,
                0x102,
                0xAF,
                dialogWidth - 0x28,
                0xBE,
                get(DIALOGS, MUSIC_VOLUME_16),
                dialogFont,
                Palettes.grayDim,
                2
        );
        addChild(musicVolumeLabel);

        PostSetupVisualObject musicVolumeSlider = new PostSetupVisualObject(
                MUSIC_VOLUME_SLIDER_ID,
                0x102,
                0xBE,
                dialogWidth - 0x28,
                0xD6,
                get(DIALOGS, MUSIC_VOLUME_19)
        );
        musicVolumeSlider.setCurrentValueAndMaxValue(new Point(
                SoundPreferences.clampVolume(soundPreferences.musicVolume),
                SoundPreferences.VOLUME_MAX
        ));
        musicVolumeSlider.gameDialogControls = musicVolumeLabel;
        addChild(musicVolumeSlider);
        disableMusicControlIfNeeded(musicVolumeSlider);

        DialogWindowVisualObject sfxVolumeLabel = new DialogWindowVisualObject(
                SFX_VOLUME_LABEL_ID,
                0x102,
                0xE0,
                dialogWidth - 0x28,
                0xEF,
                get(DIALOGS, SFX_VOLUME_17),
                dialogFont,
                Palettes.grayDim,
                2
        );
        addChild(sfxVolumeLabel);

        PostSetupVisualObject sfxVolumeSlider = new PostSetupVisualObject(
                SFX_VOLUME_SLIDER_ID,
                0x102,
                0xF0,
                dialogWidth - 0x28,
                0x108,
                get(DIALOGS, SFX_VOLUME_20)
        );
        sfxVolumeSlider.setCurrentValueAndMaxValue(new Point(
                SoundPreferences.clampVolume(soundPreferences.sfxVolume),
                SoundPreferences.VOLUME_MAX
        ));
        sfxVolumeSlider.gameDialogControls = sfxVolumeLabel;
        addChild(sfxVolumeSlider);

        DialogWindowVisualObject speechVolumeLabel = new DialogWindowVisualObject(
                SPEECH_VOLUME_LABEL_ID,
                0x102,
                0x113,
                dialogWidth - 0x28,
                0x122,
                get(DIALOGS, SPEECH_VOLUME_18),
                dialogFont,
                Palettes.grayDim,
                2
        );
        addChild(speechVolumeLabel);

        PostSetupVisualObject speechVolumeSlider = new PostSetupVisualObject(
                SPEECH_VOLUME_SLIDER_ID,
                0x102,
                0x122,
                dialogWidth - 0x28,
                0x13A,
                get(DIALOGS, SPEECH_VOLUME_21)
        );
        speechVolumeSlider.setCurrentValueAndMaxValue(new Point(
                SoundPreferences.clampVolume(soundPreferences.speechVolume),
                SoundPreferences.VOLUME_MAX
        ));
        speechVolumeSlider.gameDialogControls = speechVolumeLabel;
        addChild(speechVolumeSlider);

        speechVolumeSlider.leftNeighbor = returnToGameButton;
        returnToGameButton.rightNeighbor = speechVolumeSlider;
        musicVolumeSlider.downNeighbor = sfxVolumeSlider;
        sfxVolumeSlider.upNeighbor = musicVolumeSlider;
        sfxVolumeSlider.downNeighbor = speechVolumeSlider;
        speechVolumeSlider.upNeighbor = sfxVolumeSlider;

        setPlaybackProgressNotificationsEnabled(true);
    }

    /**
     * vtbl +0x48: SoundPreferencesDialogVisualObject::OnMessage @0043D8A4.
     * Java-normalized for 0..100 volume slider values.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        int w = readMessageInt(wParam);
        int l = readMessageInt(lParam);
        if (msg == MUSIC_PLAYBACK_PROGRESS_NOTIFICATION) {
            return 1;
        }
        if (msg == TEXT_LIST_SELECTION_CHANGED) {
            return handleSelectionChangedMessage(w, l);
        }
        if (msg == POST_SETUP_SLIDER_RELEASED) {
            return handleSliderReleaseMessage(w);
        }
        if (msg == SOUND_OPTIONS_CONFIRM) {
            commitSelections();
            return super.onMessage(DIALOG_OK, 0, 0);
        }
        if (msg == SOUND_OPTIONS_PLAY_MUSIC_PREVIEW) {
            playMusicPreview();
            return 1;
        }
        if (msg == SOUND_OPTIONS_STOP_MUSIC_PREVIEW) {
            stopMusicPreview();
            return 1;
        }
        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * Native support boundary for the main-window `musicPlayer` fetch at the top of SoundPreferencesDialogVisualObject::Initialize @0043CB0D.
     */
    private void refreshMusicPlayer() {
        musicPlayer = Globals.mainWindow.getMusicPlayer();
    }

    /**
     * Native support block for `TEXT_LIST_SELECTION_CHANGED` handling in SoundPreferencesDialogVisualObject::OnMessage @0043D8A4.
     * Java-normalized for 0..100 volume slider values.
     */
    private int handleSelectionChangedMessage(int wParam, int lParam) {
        if (wParam == SOUND_RANDOM_SELECTION_MESSAGE_ID || wParam == ACKNOWLEDGEMENT_RANDOM_ORDER_LIST_ID) {
            soundPreferences.soundRandom = lParam;
            musicPlayer.setRandomOrderMask(lParam);
            return 1;
        }
        if (wParam == TRACK_LIST_ID) {
            selectedTrackIndex = lParam;
            return 1;
        }
        if (wParam == MUSIC_VOLUME_SLIDER_ID) {
            int newMusicVolume = SoundPreferences.clampVolume(lParam);
            if (newMusicVolume != musicPlayer.getVolume()) {
                soundPreferences.musicVolume = newMusicVolume;
                if (musicPlayer.getPlaybackState() != MusicPlayer.PLAYBACK_STATE_FADING) {
                    musicPlayer.setVolume(newMusicVolume);
                }
            }
            return 1;
        }
        if (wParam == SFX_VOLUME_SLIDER_ID) {
            int newSfxVolume = SoundPreferences.clampVolume(lParam);
            soundPreferences.sfxVolume = newSfxVolume;
            syncGlobalSfxVolume(newSfxVolume);
            Globals.mainWindow.postMessage(RESET_SELECTION_GRID, 0, 0);
            return 1;
        }
        if (wParam == SPEECH_VOLUME_SLIDER_ID) {
            int newSpeechVolume = SoundPreferences.clampVolume(lParam);
            soundPreferences.speechVolume = newSpeechVolume;
            syncGlobalSpeechVolume(newSpeechVolume);
            return 1;
        }
        return 1;
    }

    /**
     * Native support block for `POST_SETUP_SLIDER_RELEASED` preview handling in SoundPreferencesDialogVisualObject::OnMessage @0043D8A4.
     */
    private int handleSliderReleaseMessage(int wParam) {
        if (wParam == SFX_VOLUME_SLIDER_ID) {
            SoundManager.SFX_SOUNDS.get(SfxSounds.UNITS_SWORD.id)
                    .play(Globals.soundPreferences.sfxVolume, false, DEFAULT_PREVIEW_PRIORITY, 0);
        } else if (wParam == SPEECH_VOLUME_SLIDER_ID) {
            playSpeechPreview();
        }
        return 1;
    }

    /**
     * Native support block for `SOUND_OPTIONS_CONFIRM` in SoundPreferencesDialogVisualObject::OnMessage @0043D8A4.
     */
    private void commitSelections() {
        setPlaybackProgressNotificationsEnabled(false);
        StringListVisualObject listControl =
                (StringListVisualObject) getChildById(ACKNOWLEDGEMENT_RANDOM_ORDER_LIST_ID);
        Globals.gamePreferences.acknowledgement = listControl.getSelectionValue();
    }

    /**
     * Native support block for `SOUND_OPTIONS_PLAY_MUSIC_PREVIEW` in SoundPreferencesDialogVisualObject::OnMessage @0043D8A4.
     */
    private void playMusicPreview() {
        soundPreferences.musicEnabled = 1;
        syncGlobalMusicEnabled(1);

        musicPlayer.setFadeOutActive(false);
        int currentTrackIndex = musicPlayer.getCurrentTrackIndex();
        if (selectedTrackIndex == currentTrackIndex) {
            musicPlayer.setVolume(soundPreferences.musicVolume);
            musicPlayer.play();
            return;
        }

        musicPlayer.stopPlayback();
        musicPlayer.selectTrack(selectedTrackIndex);
        musicPlayer.setVolume(soundPreferences.musicVolume);
        musicPlayer.play();
    }

    /**
     * Native support block for `SOUND_OPTIONS_STOP_MUSIC_PREVIEW` in SoundPreferencesDialogVisualObject::OnMessage @0043D8A4.
     */
    private void stopMusicPreview() {
        int playbackState = musicPlayer.getPlaybackState();
        if (playbackState == MusicPlayer.PLAYBACK_STATE_FADING) {
            musicPlayer.stopPlayback();
        }
        if (playbackState == MusicPlayer.PLAYBACK_STATE_ACTIVE) {
            musicPlayer.beginFadeOut(
                    MUSIC_STOP_FADE_DELAY_MS,
                    MUSIC_STOP_FADE_DURATION_MS
            );
        }
        soundPreferences.musicEnabled = 0;
        syncGlobalMusicEnabled(0);
    }

    /**
     * Native helper path inside SoundPreferencesDialogVisualObject::Initialize @0043CB0D for track-row population.
     */
    private void populateTrackRows(TextListVisualObject trackList) {
        for (int trackIndex = 0; trackIndex < musicPlayer.getMusicFileCount(); trackIndex++) {
            trackList.rows.add(resolveTrackDisplayName(musicPlayer.getMusicFileNameAt(trackIndex)));
        }
    }

    /**
     * Native helper block repeated across SoundPreferencesDialogVisualObject::Initialize @0043CB0D for play/stop/music-volume controls.
     */
    private void disableMusicControlIfNeeded(CVisualObject control) {
        if (soundPreferences.musicAvailable == 0) {
            control.setStateFlag(1, 0);
            control.setText(get(DIALOGS, MUSIC_IS_NOT_AVAILABLE_23));
        }
        if (getTrackFileNames().isEmpty()) {
            control.setStateFlag(1, 0);
            control.setText(get(DIALOGS, MUSIC_OPTIONS_ARE_NOT_AVAILABLE_NOW_75));
        }
    }

    /**
     * Native support boundary for `StringListVariantAVisualObject` row text in SoundPreferencesDialogVisualObject::Initialize @0043CB0D.
     */
    private static String resolveAcknowledgementRowText() {
        return get(DIALOGS, DialogsText.byIndex(ACKNOWLEDGEMENTS_TEXT_INDEX));
    }

    /**
     * Native support boundary for `CStringArray` track-file enumeration in SoundPreferencesDialogVisualObject::Initialize @0043CB0D.
     */
    private List<String> getTrackFileNames() {
        return musicPlayer.getMusicFileNames();
    }

    /**
     * Native support extracted from the `CString` lower/mid + `g_GameConfig` lookup block in
     * SoundPreferencesDialogVisualObject::Initialize @0043CB0D and parseTunes @004753FD.
     */
    private static String resolveTrackDisplayName(String trackFileName) {
        String normalized = trackFileName.toLowerCase(Locale.ROOT);
        String lookupKey = normalized.length() > 6 ? normalized.substring(6) : "";
        for (TunesText tune : TunesText.values()) {
            String line = get(TUNES, tune);
            int separator = line.indexOf('=');
            if (line.substring(0, separator).equals(lookupKey)) {
                return line.substring(separator + 1);
            }
        }
        return "";
    }

    /**
     * Native support boundary for the `sessionMode == 0 || 1` gate in SoundPreferencesDialogVisualObject::Initialize @0043CB0D.
     */
    private static boolean isAcknowledgementToggleDisabledInNetworkSession() {
        int sessionMode = Globals.mainWindow.sessionMode;
        return sessionMode == 0 || sessionMode == 1;
    }

    /**
     * Native support boundary for MusicPlayer::SetPlaybackProgressNotificationsEnabled @00450260, toggled by SoundPreferencesDialogVisualObject::Initialize/@0043CB0D and OnMessage/@0043D8A4.
     */
    private void setPlaybackProgressNotificationsEnabled(boolean dialogOpen) {
        musicPlayer.setPlaybackProgressNotificationsEnabled(dialogOpen);
    }

    /**
     * Native support boundary for the speech preview branch in SoundPreferencesDialogVisualObject::OnMessage @0043D8A4.
     */
    private void playSpeechPreview() {
        Sound sound = SoundManager.FIGHTER_PACKS[0].select[SPEECH_PREVIEW_SOUND_INDEX];
        sound.play(Globals.soundPreferences.speechVolume, false, DEFAULT_PREVIEW_PRIORITY, 0);
    }

    /**
     * Native support boundary for the global `g_SoundPreferences.SfxVolume` write in SoundPreferencesDialogVisualObject::OnMessage @0043D8A4.
     */
    private static void syncGlobalSfxVolume(int sfxVolume) {
        Globals.soundPreferences.sfxVolume = SoundPreferences.clampVolume(sfxVolume);
    }

    /**
     * Native support boundary for the global `g_SoundPreferences.SpeechVolume` write in SoundPreferencesDialogVisualObject::OnMessage @0043D8A4.
     */
    private static void syncGlobalSpeechVolume(int speechVolume) {
        Globals.soundPreferences.speechVolume = SoundPreferences.clampVolume(speechVolume);
    }

    /**
     * Native support boundary for the global `g_SoundPreferences.MusicEnabled` write in SoundPreferencesDialogVisualObject::OnMessage @0043D8A4.
     */
    private static void syncGlobalMusicEnabled(int musicEnabled) {
        Globals.soundPreferences.musicEnabled = musicEnabled;
    }

}
