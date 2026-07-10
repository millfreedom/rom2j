package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.TokenEntry;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.window.CMainWindow;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ua.millfreedom.rom2.model.enums.MessageCodes.DIALOG_OK;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RENDER_FRAME;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RETURN_TO_GAME;
import static ua.millfreedom.rom2.text.DialogsText.*;
import static ua.millfreedom.rom2.text.GameTexts.get;

/**
 * Native class: DropGoldPromptVisualObject (vtbl @0x005CC170).
 * Purpose: gold-drop prompt window with a numeric amount input.
 */
public class DropGoldPromptVisualObject extends HandlerVisualObject {
    public static final int NATIVE_SIZE = 0xD0; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int PROMPT_WIDTH = 0x128;
    private static final int PROMPT_HEIGHT = 0xA8;
    private static final int AMOUNT_INPUT_ID = 0x989685;
    private static final int ACCEPT_BUTTON_ID = 0x989681;
    private static final int CANCEL_BUTTON_ID = 0x989682;
    private static final int HEADER_LINE_1_ID = 0x989683;
    private static final int HEADER_LINE_2_ID = 0x989684;
    private static final String DEFAULT_AMOUNT_TEXT = "0";
    private static final Pattern SSCANF_FLOAT_PATTERN = Pattern.compile("^[\\s]*[+-]?(?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:[eE][+-]?\\d+)?");

    //0x68
    public int selectionEntryIndex;

    /**
     * Native: DropGoldPromptVisualObject::DropGoldPromptVisualObject @00440DA0.
     * Fully ported.
     */
    public DropGoldPromptVisualObject(int id, int xLeft, int yTop) {
        super(id, xLeft, yTop, xLeft + PROMPT_WIDTH, yTop + PROMPT_HEIGHT, null);
        initialize();
    }

    /**
     * vtbl +0x78: DropGoldPromptVisualObject::Initialize @00440E33.
     * Fully ported.
     */
    @Override
    public void initialize() {
        CBitmapFont dialogFont = Globals.fonts.font1;

        addChild(new StaticTextVisualObject(
                AMOUNT_INPUT_ID,
                0x1E,
                0x41,
                0x10A,
                0x55,
                dialogFont,
                Palettes.grayDim,
                null
        ));

        int buttonTop = cRect.height() - 0x32;
        int buttonBottom = cRect.height() - 0x1E;
        addChild(new CommandButtonVisualObject(
                ACCEPT_BUTTON_ID,
                0x44,
                buttonTop,
                0x8A,
                buttonBottom,
                get(OK_0),
                dialogFont,
                Palettes.grayDim,
                DIALOG_OK,
                0,
                get(DROP_MONEY_46)
        ));
        addChild(new CommandButtonVisualObject(
                CANCEL_BUTTON_ID,
                0x9E,
                buttonTop,
                0xE4,
                buttonBottom,
                get(CANCEL_1),
                dialogFont,
                Palettes.grayDim,
                RETURN_TO_GAME,
                0,
                get(CANCEL_DROP_47)
        ));
        addChild(new DialogWindowVisualObject(
                HEADER_LINE_1_ID,
                0x14,
                0x14,
                0x114,
                0x28,
                get(HOW_MUCH_GOLD_DO_48),
                dialogFont,
                Palettes.grayDim,
                2
        ));
        addChild(new DialogWindowVisualObject(
                HEADER_LINE_2_ID,
                0x14,
                0x28,
                0x114,
                0x3C,
                get(YOU_WANT_TO_DROP_49),
                dialogFont,
                Palettes.grayDim,
                2
        ));
    }

    /**
     * vtbl +0x34: DropGoldPromptVisualObject::Draw @004410D3.
     * Fully ported.
     */
    @Override
    public void draw() {
        Globals.renderer.lockSurface();
        try {
            super.draw();
        } finally {
            Globals.renderer.unlockSurface();
        }
    }

    /**
     * Java rendering extension for the active drop-gold modal during gameplay full-surface redraws.
     * not ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        if (msg == RENDER_FRAME && activeFlag != 0) {
            int result = super.onMessage(msg, wParam, lParam);
            draw();
            return result;
        }
        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * vtbl +0x80: DropGoldPromptVisualObject::ShowDialog @00441157.
     * Fully ported.
     */
    @Override
    public void showDialog() {
        getAmountInputControl().setInputText(DEFAULT_AMOUNT_TEXT);
        super.showDialog();
    }

    /**
     * vtbl +0x6C: DropGoldPromptVisualObject::OnKeyDown @004410F0.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        CMainWindow mainWindow = Globals.mainWindow;
        if (nChar == 0x0D) {
            mainWindow.postMessage(DIALOG_OK, 0, 0);
            return 1;
        }
        if (nChar == 0x1B) {
            mainWindow.postMessage(RETURN_TO_GAME, 0, 0);
            return 1;
        }
        return super.onKeyDown(nChar);
    }

    /**
     * vtbl +0x84: DropGoldPromptVisualObject::HideDialog @004411A2.
     * Fully ported.
     */
    @Override
    public HandlerVisualObject hideDialog(MessageCodes reason) {
        CMainWindow mainWindow = Globals.mainWindow;
        if (reason == DIALOG_OK) {
            String rawAmountText = readAmountInputText();
            int confirmedAmount = clampConfirmedDropAmount(parseAmount(rawAmountText), mainWindow);
            commitConfirmedDropAmount(mainWindow, confirmedAmount);
            mainWindow.clearUiLockState();
        }
        return super.hideDialog(reason);
    }

    /**
     * Native support: child id `0x989685` lookups used by DropGoldPromptVisualObject::ShowDialog @00441157 and HideDialog @004411A2.
     */
    private StaticTextVisualObject getAmountInputControl() {
        return (StaticTextVisualObject) getChildById(AMOUNT_INPUT_ID);
    }

    /**
     * Native support: `CopyTextToBuffer` call site in DropGoldPromptVisualObject::HideDialog @004411A2.
     */
    private String readAmountInputText() {
        StringBuilder rawAmountText = new StringBuilder();
        getAmountInputControl().copyTextToBuffer(rawAmountText);
        return rawAmountText.toString();
    }

    /**
     * Native support extracted from `sscanf("%f", ...)` parse in DropGoldPromptVisualObject::HideDialog @004411A2.
     */
    private static float parseAmount(String rawAmountText) {
        Matcher matcher = SSCANF_FLOAT_PATTERN.matcher(rawAmountText);
        if (!matcher.find()) {
            throw new IllegalArgumentException("DropGoldPromptVisualObject amount text has no sscanf(\"%f\") prefix: " + rawAmountText);
        }
        return Float.parseFloat(matcher.group().trim());
    }

    /**
     * Native support: non-negative and available-gold clamp block in DropGoldPromptVisualObject::HideDialog @004411A2.
     */
    private static int clampConfirmedDropAmount(float parsedAmount, CMainWindow mainWindow) {
        float clampedAmount = Math.max(0.0f, parsedAmount);
        int availableGold = readCurrentPlayerGold(mainWindow);
        if (availableGold < clampedAmount) {
            clampedAmount = availableGold;
        }
        return (int) clampedAmount;
    }

    /**
     * Native support boundary: current player-gold read in DropGoldPromptVisualObject::HideDialog @004411A2.
     */
    private static int readCurrentPlayerGold(CMainWindow mainWindow) {
        return mainWindow.pMapVisualObject.currentPlayer.gold;
    }

    /**
     * Native support boundary: current player-gold write in DropGoldPromptVisualObject::HideDialog @004411A2.
     */
    private static void writeCurrentPlayerGold(CMainWindow mainWindow, int amount) {
        mainWindow.pMapVisualObject.currentPlayer.gold = amount;
    }

    /**
     * Native support extracted from DropGoldPromptVisualObject::HideDialog @004411A2.
     */
    private void commitConfirmedDropAmount(CMainWindow mainWindow, int confirmedAmount) {
        GridOverlayVisualObject gridOverlayVisualObject = mainWindow.pHeroInventoryControlVisualObject;
        TokenEntry detachedPayload = (TokenEntry) gridOverlayVisualObject.beginUiDrag(selectionEntryIndex, confirmedAmount);
        int detachedAmount = detachedPayload.quantity;

        writeCurrentPlayerGold(mainWindow, readCurrentPlayerGold(mainWindow) - detachedAmount);
        dispatchDroppedGoldAction(detachedAmount);
    }

    /**
     * Native: DropGoldPromptVisualObject::setSelectionEntryIndex @004A42E0.
     * Fully ported.
     */
    public void setSelectionEntryIndex(int selectionEntryIndex) {
        this.selectionEntryIndex = selectionEntryIndex;
    }

    /**
     * Native support extracted from DropGoldPromptVisualObject::HideDialog @004411A2 and MapVisualObject::sendDropGoldAction @0041A6CD.
     */
    private static void dispatchDroppedGoldAction(int confirmedAmount) {
        MapVisualObject mapVisualObject = Globals.mainWindow.pMapVisualObject;
        CUnit selectedUnit = mapVisualObject.getSelectedCUnit();
        int packedCell = (selectedUnit.tileX & 0xFF) | ((selectedUnit.tileY & 0xFF) << 8);
        mapVisualObject.sendDropGoldAction(confirmedAmount, packedCell);
    }

}
