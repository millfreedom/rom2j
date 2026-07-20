package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.CMainApp;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.GamePreferences;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.window.CMainWindow;

import java.awt.*;

import static ua.millfreedom.rom2.model.enums.MessageCodes.DIALOG_OK;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RETURN_TO_GAME;
import static ua.millfreedom.rom2.text.DialogsText.*;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.PatchText.*;
import static ua.millfreedom.rom2.text.TextTableId.DIALOGS;
import static ua.millfreedom.rom2.text.TextTableId.PATCH;

/**
 * Native class: CenteredDialogVariantVisualObject.
 * Purpose: centered game-options dialog with slider, toggle rows, list selectors, and confirm/cancel buttons.
 */
public class CenteredDialogVariantVisualObject extends CenteredDialogVisualObject {
    public static final int NATIVE_SIZE = 0x68; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int TITLE_ID = -1;
    private static final int GAME_SPEED_LABEL_ID = 1;
    private static final int GAME_SPEED_SLIDER_ID = 2;
    private static final int SHOW_TIME_FLOW_ID = 0x0C;
    private static final int SMOOTHING_ID = 3;
    private static final int SHADOWS_ID = 0x1F;
    private static final int DYNAMIC_LIGHTING_ID = 0x20;
    private static final int OBJECT_ANIMATIONS_ID = 0x21;
    private static final int SHOW_ALL_HIT_POINTS_ID = 4;
    private static final int SHOW_FLYING_HP_ID = 5;
    private static final int TIPS_OR_NAMES_ID = 0x0D;
    private static final int ALT_MESSAGE_COLORS_ID = 0x29;
    private static final int AUTOCAST_FOR_UNITS_LABEL_ID = 0x0F;
    private static final int AUTOCAST_OWN_ID = 0x83;
    private static final int AUTOCAST_ALLIES_ID = 0x84;
    private static final int AUTOCAST_NEUTRAL_ID = 0x85;
    private static final int FORMATION_MODE_LABEL_ID = 6;
    private static final int FORMATION_MODE_LIST_ID = 7;
    private static final int RETREAT_MODE_LABEL_ID = 8;
    private static final int RETREAT_MODE_LIST_ID = 9;
    private static final int AUTOCAST_MODE_LABEL_ID = 0x97;
    private static final int AUTOCAST_MODE_LIST_ID = 0x8D;
    private static final int OK_BUTTON_ID = 10;
    private static final int CANCEL_BUTTON_ID = 11;
    private static final int GAME_SPEED_MAX_VALUE = 8;
    private static final int BUTTON_ROW_DIVISOR = 7;

    private static final int SCROLL_SPEED_LABEL_ID = 0xF1;
    private static final int SCROLL_SPEED_SLIDER_ID = 0xF2;
    private static final int VSYNC_ID = 0xF3;
    private static final int SCROLL_SPEED_MAX_VALUE = 10;

    /**
     * Native: CenteredDialogVariantVisualObject::CenteredDialogVariantVisualObject @004417C3.
     * Fully ported.
     */
    public CenteredDialogVariantVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        super(id, xLeft, yTop, xRight, yBottom, null);
    }

    /**
     * vtbl +0x78: CenteredDialogVariantVisualObject::Initialize @004417FA.
     * Fully ported.
     */
    @Override
    public void initialize() {
        CBitmapFont dialogFont = Globals.fonts.font1;
        GamePreferences preferences = Globals.gamePreferences;

        DialogWindowVisualObject title = new DialogWindowVisualObject(
                TITLE_ID,
                0x28,
                0x14,
                cRect.width() - 0x28,
                0x28,
                get(DIALOGS, GAME_OPTIONS_150),
                dialogFont,
                Palettes.grayDim,
                2
        );
        addChild(title);

        DialogWindowVisualObject gameSpeedLabel = new DialogWindowVisualObject(
                GAME_SPEED_LABEL_ID,
                0x28,
                26,
                0xE8,
                44,
                get(DIALOGS, GAME_SPEED_50),
                dialogFont,
                Palettes.grayDim,
                2
        );
        addChild(gameSpeedLabel);

        PostSetupVisualObject gameSpeedSlider = new PostSetupVisualObject(
                GAME_SPEED_SLIDER_ID,
                0x28,
                44,
                0xE8,
                68,
                get(DIALOGS, GAME_SPEED_51)
        );
        addChild(gameSpeedSlider);
        if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_MULTIPLAYER_CLIENT) {
            gameSpeedSlider.setStateFlag(1, 0);
        }
        gameSpeedSlider.setCurrentValueAndMaxValue(new Point(preferences.gameSpeed, GAME_SPEED_MAX_VALUE));
        gameSpeedSlider.gameDialogControls = gameSpeedLabel;

        DialogWindowVisualObject scrollSpeedLabel = new DialogWindowVisualObject(
                SCROLL_SPEED_LABEL_ID,
                0x28,
                66,
                0xE8,
                88,
                "Scroll Speed",//get(DIALOGS, GAME_SPEED_50),
                dialogFont,
                Palettes.grayDim,
                2
        );
        addChild(scrollSpeedLabel);

        PostSetupVisualObject scrollSpeedSlider = new PostSetupVisualObject(
                SCROLL_SPEED_SLIDER_ID,
                0x28,
                88,
                0xE8,
                112,
                "Scroll Speed"//get(DIALOGS, GAME_SPEED_51)
        );
        addChild(scrollSpeedSlider);
        scrollSpeedSlider.setCurrentValueAndMaxValue(new Point(preferences.scrollSpeed, SCROLL_SPEED_MAX_VALUE));
        scrollSpeedSlider.gameDialogControls = scrollSpeedLabel;

        scrollSpeedSlider.upNeighbor = gameSpeedSlider;
        gameSpeedSlider.downNeighbor = scrollSpeedSlider;

        StringListVariantAVisualObject showTimeFlow = createBinaryOption(
                SHOW_TIME_FLOW_ID,
                dialogFont,
                0x28,
                0x78,
                0xE8,
                0x90,
                get(DIALOGS, DAY_NIGHT_CHANGES_55),
                preferences.showTimeFlow
        );
        showTimeFlow.upNeighbor = scrollSpeedSlider;
        scrollSpeedSlider.downNeighbor = showTimeFlow;

        StringListVariantAVisualObject smoothing = createBinaryOption(
                SMOOTHING_ID,
                dialogFont,
                0x28,
                0x94,
                0xE8,
                0xAC,
                get(DIALOGS, SMOOTHING_53),
                preferences.smoothing
        );
        smoothing.upNeighbor = showTimeFlow;
        showTimeFlow.downNeighbor = smoothing;

        StringListVariantAVisualObject shadows = createBinaryOption(
                SHADOWS_ID,
                dialogFont,
                0x28,
                0xB0,
                0xE8,
                0xC8,
                get(PATCH, SHADOWS_52),
                preferences.shadows
        );
        shadows.upNeighbor = smoothing;
        smoothing.downNeighbor = shadows;

        StringListVariantAVisualObject dynamicLighting = createBinaryOption(
                DYNAMIC_LIGHTING_ID,
                dialogFont,
                0x28,
                0xCC,
                0xE8,
                0xE4,
                get(PATCH, DYNAMIC_LIGHTING_53),
                preferences.lighting
        );
        dynamicLighting.upNeighbor = shadows;
        shadows.downNeighbor = dynamicLighting;

        StringListVariantAVisualObject objectAnimations = createBinaryOption(
                OBJECT_ANIMATIONS_ID,
                dialogFont,
                0x28,
                0xE8,
                0xE8,
                0x100,
                get(PATCH, OBJECT_ANIMATIONS_54),
                preferences.animation
        );
        objectAnimations.upNeighbor = dynamicLighting;
        dynamicLighting.downNeighbor = objectAnimations;

        StringListVariantAVisualObject showAllHitPoints = createBinaryOption(
                SHOW_ALL_HIT_POINTS_ID,
                dialogFont,
                0x118,
                0x29,
                0x1F0,
                0x41,
                get(DIALOGS, SHOW_HEALTH_57),
                preferences.showAllHitPoints
        );
        showAllHitPoints.leftNeighbor = gameSpeedSlider;
        gameSpeedSlider.rightNeighbor = showAllHitPoints;

        StringListVariantAVisualObject showFlyingHp = createBinaryOption(
                SHOW_FLYING_HP_ID,
                dialogFont,
                0x118,
                0x45,
                0x1F0,
                0x5D,
                get(DIALOGS, DISPLAY_DAMAGE_POINTS_78),
                preferences.showFlyingHp
        );
        showFlyingHp.upNeighbor = showAllHitPoints;
        showAllHitPoints.downNeighbor = showFlyingHp;

        StringListVariantAVisualObject tipsOrNames = createBinaryOption(
                TIPS_OR_NAMES_ID,
                dialogFont,
                0x118,
                0x61,
                0x1F0,
                0x79,
                resolveTipsOrNamesLabel(),
                resolveTipsOrNamesSelection(preferences)
        );
        tipsOrNames.upNeighbor = showFlyingHp;
        showFlyingHp.downNeighbor = tipsOrNames;

        StringListVariantAVisualObject altMessageColors = createBinaryOption(
                ALT_MESSAGE_COLORS_ID,
                dialogFont,
                0x118,
                0x7D,
                0x1F0,
                0x95,
                get(PATCH, ALT_MESSAGE_COLORS_87),
                preferences.messageColors
        );
        altMessageColors.upNeighbor = tipsOrNames;
        tipsOrNames.downNeighbor = altMessageColors;

        DialogWindowVisualObject autocastUnitsLabel = new DialogWindowVisualObject(
                AUTOCAST_FOR_UNITS_LABEL_ID,
                0x118,
                0x9A,
                0x1D8,
                0xB2,
                get(DIALOGS, AUTOCAST_FOR_UNITS_166),
                dialogFont,
                Palettes.grayDim,
                0
        );
        addChild(autocastUnitsLabel);

        StringListVariantAVisualObject autocastOwn = createBinaryOption(
                AUTOCAST_OWN_ID,
                dialogFont,
                0x118,
                0xB0,
                0x1F0,
                0xC8,
                get(DIALOGS, OWN_167),
                (preferences.autoCasting & GamePreferences.AUTOCAST_OWN) != 0 ? 1 : 0
        );
        autocastOwn.upNeighbor = altMessageColors;
        altMessageColors.downNeighbor = autocastOwn;

        StringListVariantAVisualObject autocastAllies = createBinaryOption(
                AUTOCAST_ALLIES_ID,
                dialogFont,
                0x118,
                0xCC,
                0x1F0,
                0xE4,
                get(DIALOGS, ALLIES_168),
                (preferences.autoCasting & GamePreferences.AUTOCAST_ALLIES) != 0 ? 1 : 0
        );
        autocastAllies.upNeighbor = autocastOwn;
        autocastOwn.downNeighbor = autocastAllies;

        StringListVariantAVisualObject autocastNeutral = createBinaryOption(
                AUTOCAST_NEUTRAL_ID,
                dialogFont,
                0x118,
                0xE8,
                0x1F0,
                0x100,
                get(DIALOGS, NEUTRAL_169),
                (preferences.autoCasting & GamePreferences.AUTOCAST_NEUTRAL) != 0 ? 1 : 0
        );
        autocastNeutral.upNeighbor = autocastAllies;
        autocastAllies.downNeighbor = autocastNeutral;

        DialogWindowVisualObject formationModeLabel = new DialogWindowVisualObject(
                FORMATION_MODE_LABEL_ID,
                0x28,
                0x108,
                0xD0,
                0x120,
                get(DIALOGS, FORMATION_MODE_58),
                dialogFont,
                Palettes.grayDim,
                0
        );
        addChild(formationModeLabel);

        StringListVariantBVisualObject formationMode = createChoiceList(
                FORMATION_MODE_LIST_ID,
                dialogFont,
                0x28,
                0x120,
                0xD0,
                0x168,
                get(DIALOGS, FORMATION_MODE_59),
                preferences.formationMode,
                get(DIALOGS, OFF_60),
                get(DIALOGS, AUTO_61),
                get(DIALOGS, ON_62)
        );
        formationMode.upNeighbor = objectAnimations;
        objectAnimations.downNeighbor = formationMode;

        DialogWindowVisualObject retreatModeLabel = new DialogWindowVisualObject(
                RETREAT_MODE_LABEL_ID,
                0xD0,
                0x108,
                0x178,
                0x120,
                get(DIALOGS, RETREAT_MODE_63),
                dialogFont,
                Palettes.grayDim,
                0
        );
        addChild(retreatModeLabel);

        StringListVariantBVisualObject retreatMode = createChoiceList(
                RETREAT_MODE_LIST_ID,
                dialogFont,
                0xD0,
                0x120,
                0x178,
                0x168,
                get(DIALOGS, RETREAT_MODE_64),
                preferences.wimpyMode,
                get(DIALOGS, NEVER_65),
                get(DIALOGS, LOW_HEALTH_66),
                get(DIALOGS, MEDIUM_HEALTH_67)
        );
        formationMode.rightNeighbor = retreatMode;
        retreatMode.leftNeighbor = formationMode;
        retreatMode.upNeighbor = autocastNeutral;
        autocastNeutral.downNeighbor = retreatMode;

        DialogWindowVisualObject autocastModeLabel = new DialogWindowVisualObject(
                AUTOCAST_MODE_LABEL_ID,
                0x178,
                0x108,
                0x220,
                0x120,
                get(DIALOGS, AUTOCAST_MODE_170),
                dialogFont,
                Palettes.grayDim,
                0
        );
        addChild(autocastModeLabel);

        StringListVariantBVisualObject autocastMode = createChoiceList(
                AUTOCAST_MODE_LIST_ID,
                dialogFont,
                0x178,
                0x120,
                0x220,
                0x168,
                get(DIALOGS, AUTOCAST_MODE_174),
                resolveAutocastModeSelection(preferences.autoCasting),
                get(DIALOGS, MINIMUM_171),
                get(DIALOGS, AVERAGE_172),
                get(DIALOGS, MAXIMUM_173)
        );
        autocastMode.leftNeighbor = retreatMode;
        retreatMode.rightNeighbor = autocastMode;
        autocastMode.upNeighbor = autocastNeutral;
        autocastNeutral.downNeighbor = autocastMode;

        StringListVariantAVisualObject vsync = createBinaryOption(
                VSYNC_ID,
                dialogFont,
                0x28,
                378,
                0xE8,
                402,
                "VSync",//get(PATCH, ALT_MESSAGE_COLORS_87),
                preferences.vsync != 0 ? 1 : 0
        );
        vsync.upNeighbor = formationMode;
        formationMode.downNeighbor = vsync;


        int buttonTop = 368+54;
        int buttonBottom = 392 + 54;
        CRect okRect = new CRect(
                cRect.width() / BUTTON_ROW_DIVISOR,
                buttonTop,
                (cRect.width() * 3) / BUTTON_ROW_DIVISOR,
                buttonBottom
        );
        CRect cancelRect = new CRect(
                (cRect.width() * 4) / BUTTON_ROW_DIVISOR,
                buttonTop,
                (cRect.width() * 6) / BUTTON_ROW_DIVISOR,
                buttonBottom
        );

        CommandButtonVisualObject okButton = new CommandButtonVisualObject(
                OK_BUTTON_ID,
                okRect,
                get(DIALOGS, OK_0),
                dialogFont,
                null,
                DIALOG_OK,
                0,
                ""
        );
        addChild(okButton);
        okButton.setStateFlag(0x10, 1);

        CommandButtonVisualObject cancelButton = new CommandButtonVisualObject(
                CANCEL_BUTTON_ID,
                cancelRect,
                get(DIALOGS, CANCEL_1),
                dialogFont,
                null,
                RETURN_TO_GAME,
                0,
                ""
        );
        addChild(cancelButton);
        okButton.rightNeighbor = cancelButton;
        cancelButton.leftNeighbor = okButton;
    }

    /**
     * vtbl +0x48: CenteredDialogVariantVisualObject::OnMessage @00442E8B.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        GamePreferences preferences = Globals.gamePreferences;
        int previousShowTimeFlow = preferences.showTimeFlow;
        if (msg == DIALOG_OK) {
            commitSelections(preferences);
        }
        if (preferences.showTimeFlow != previousShowTimeFlow) {
            refreshMapTimeFlowLighting();
        }
        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * Native support extracted from repeated StringListVisualObject::AddRow @0044EE50 calls in CenteredDialogVariantVisualObject::Initialize @004417FA.
     */
    private static void appendChoiceLabel(StringListVisualObject control, String label) {
        control.addRow(label);
    }

    /**
     * Native support extracted from CenteredDialogVariantVisualObject::Initialize @004417FA for `StringListVariantAVisualObject` toggle rows.
     */
    private StringListVariantAVisualObject createBinaryOption(
            int id,
            CBitmapFont dialogFont,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            String label,
            int selectionValue
    ) {
        StringListVariantAVisualObject control = new StringListVariantAVisualObject(
                id,
                xLeft,
                yTop,
                xRight,
                yBottom,
                dialogFont,
                Palettes.grayDim,
                null
        );
        appendChoiceLabel(control, label);
        control.setSelectionValue(selectionValue);
        addChild(control);
        return control;
    }

    /**
     * Native support extracted from CenteredDialogVariantVisualObject::Initialize @004417FA for `StringListVariantBVisualObject` cycle selectors.
     */
    private StringListVariantBVisualObject createChoiceList(
            int id,
            CBitmapFont dialogFont,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            String name,
            int selectionValue,
            String... rows
    ) {
        StringListVariantBVisualObject control = new StringListVariantBVisualObject(
                id,
                xLeft,
                yTop,
                xRight,
                yBottom,
                dialogFont,
                Palettes.grayDim,
                name
        );
        for (String row : rows) {
            appendChoiceLabel(control, row);
        }
        control.setSelectionValue(selectionValue);
        addChild(control);
        return control;
    }

    /**
     * Native support block for the `DIALOG_OK` branch in CenteredDialogVariantVisualObject::OnMessage @00442E8B.
     */
    private void commitSelections(GamePreferences preferences) {
        preferences.gameSpeed = readGameSpeed();
        Globals.mainWindow.setGameSpeed(preferences.gameSpeed);
        preferences.scrollSpeed = ((PostSetupVisualObject) getChildById(SCROLL_SPEED_SLIDER_ID)).currentValue;
        preferences.vsync = readSelectionValue(VSYNC_ID);;

        if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN) {
            preferences.tipsMode = readSelectionValue(TIPS_OR_NAMES_ID);
        } else {
            preferences.clanNames = readSelectionValue(TIPS_OR_NAMES_ID);
        }

        preferences.showTimeFlow = readSelectionValue(SHOW_TIME_FLOW_ID);
        preferences.smoothing = readSelectionValue(SMOOTHING_ID);
        preferences.shadows = readSelectionValue(SHADOWS_ID);
        preferences.lighting = readSelectionValue(DYNAMIC_LIGHTING_ID);
        preferences.animation = readSelectionValue(OBJECT_ANIMATIONS_ID);
        if (preferences.animation == 0) {
            preferences.lighting = 0;
        }
        preferences.showAllHitPoints = readSelectionValue(SHOW_ALL_HIT_POINTS_ID);
        preferences.showFlyingHp = readSelectionValue(SHOW_FLYING_HP_ID);
        preferences.messageColors = readSelectionValue(ALT_MESSAGE_COLORS_ID);
        applyMessageColors(preferences.messageColors);
        preferences.formationMode = readSelectionValue(FORMATION_MODE_LIST_ID);
        preferences.wimpyMode = readSelectionValue(RETREAT_MODE_LIST_ID);
        preferences.autoCasting = GamePreferences.AUTOCAST_BASE;

        int autocastModeSelection = readSelectionValue(AUTOCAST_MODE_LIST_ID);
        if (autocastModeSelection == 1) {
            preferences.autoCasting |= GamePreferences.AUTOCAST_MODE_AVERAGE;
        } else if (autocastModeSelection == 2) {
            preferences.autoCasting |= GamePreferences.AUTOCAST_MODE_AVERAGE
                    | GamePreferences.AUTOCAST_MODE_MAXIMUM;
        }
        if (readSelectionValue(AUTOCAST_OWN_ID) != 0) {
            preferences.autoCasting |= GamePreferences.AUTOCAST_OWN;
        }
        if (readSelectionValue(AUTOCAST_ALLIES_ID) != 0) {
            preferences.autoCasting |= GamePreferences.AUTOCAST_ALLIES;
        }
        if (readSelectionValue(AUTOCAST_NEUTRAL_ID) != 0) {
            preferences.autoCasting |= GamePreferences.AUTOCAST_NEUTRAL;
        }

        preferences.applyRuntimeValues(Globals.mainWindow);
        applyMapPreferenceEffects(preferences);
    }

    /**
     * Native support extracted from `StringListVisualObject::getValue` calls in CenteredDialogVariantVisualObject::OnMessage @00442E8B.
     */
    private int readSelectionValue(int childId) {
        return ((StringListVisualObject) getChildById(childId)).getSelectionValue();
    }

    /**
     * Native support extracted from `PostSetupVisualObject::GetCurrentValueAndMaxValue` in CenteredDialogVariantVisualObject::OnMessage @00442E8B.
     */
    private int readGameSpeed() {
        Point currentAndMax = new Point();
        ((PostSetupVisualObject) getChildById(GAME_SPEED_SLIDER_ID)).getCurrentValueAndMaxValue(currentAndMax);
        return currentAndMax.x;
    }

    /**
     * Native support block for map refresh/apply calls at the tail of CenteredDialogVariantVisualObject::OnMessage @00442E8B.
     */
    private static void applyMapPreferenceEffects(GamePreferences preferences) {
        MapVisualObject mapVisual = Globals.mainWindow.pMapVisualObject;
        mapVisual.applyFormationMode(preferences.formationMode % 3);
        mapVisual.applyWimpyMode(preferences.wimpyMode % 3);
        mapVisual.applyAutoCasting();
    }

    /**
     * Native support extracted from the Global::setMessageColorsPalette @004756C3 call in CenteredDialogVariantVisualObject::OnMessage @00442E8B.
     */
    private static void applyMessageColors(int messageColors) {
        Palettes.setMessageColorsPalette(messageColors);
    }

    /**
     * Native support extracted from the `MapVisualObject::RefreshTimeFlowLighting(this, 1)` call after the
     * `ShowTimeFlow` change gate in CenteredDialogVariantVisualObject::OnMessage @00442E8B.
     */
    private static void refreshMapTimeFlowLighting() {
        Globals.mainWindow.pMapVisualObject.refreshTimeFlowLighting();
    }

    /**
     * Native support extracted from the `sessionMode == 2` label split in CenteredDialogVariantVisualObject::Initialize @004417FA.
     */
    private static String resolveTipsOrNamesLabel() {
        return Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN
                ? get(DIALOGS, TIPS_156)
                : get(DIALOGS, NAMES_AND_CLANS_175);
    }

    /**
     * Native support extracted from the `sessionMode == 2` selection source split in CenteredDialogVariantVisualObject::Initialize @004417FA.
     */
    private static int resolveTipsOrNamesSelection(GamePreferences preferences) {
        return Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN
                ? preferences.tipsMode
                : preferences.clanNames;
    }

    /**
     * Native helper for the `AutoCasting` bitmask-to-selector mapping in CenteredDialogVariantVisualObject::Initialize @004417FA.
     */
    private static int resolveAutocastModeSelection(int autoCasting) {
        if ((autoCasting & GamePreferences.AUTOCAST_MODE_MAXIMUM) != 0) {
            return 2;
        }
        if ((autoCasting & GamePreferences.AUTOCAST_MODE_AVERAGE) != 0) {
            return 1;
        }
        return 0;
    }

}
