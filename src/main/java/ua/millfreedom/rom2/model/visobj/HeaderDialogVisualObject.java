package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.palette.Palettes;

import java.util.ArrayList;
import java.util.List;

import static ua.millfreedom.rom2.model.enums.MessageCodes.DIALOG_OK;
import static ua.millfreedom.rom2.model.enums.MessageCodes.HEADER_DIALOG_ABORT;
import static ua.millfreedom.rom2.model.enums.MessageCodes.HEADER_DIALOG_IGNORE;
import static ua.millfreedom.rom2.model.enums.MessageCodes.HEADER_DIALOG_NO;
import static ua.millfreedom.rom2.model.enums.MessageCodes.HEADER_DIALOG_RETRY;
import static ua.millfreedom.rom2.model.enums.MessageCodes.HEADER_DIALOG_YES;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RETURN_TO_GAME;
import static ua.millfreedom.rom2.text.DialogsText.ABORT_2;
import static ua.millfreedom.rom2.text.DialogsText.CANCEL_1;
import static ua.millfreedom.rom2.text.DialogsText.IGNORE_4;
import static ua.millfreedom.rom2.text.DialogsText.OK_0;
import static ua.millfreedom.rom2.text.DialogsText.RETRY_3;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_NO_76;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_YES_75;
import static ua.millfreedom.rom2.text.TextTableId.DIALOGS;

/**
 * Native class: HeaderDialogVisualObject.
 * Purpose: centered dialog with header/title strings and opaque payload pointer.
 */
public abstract class HeaderDialogVisualObject extends CenteredDialogVisualObject {
    public static final int NATIVE_SIZE = 0x78; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!
    private static final int BUTTON_ID_PRIMARY = 4;
    private static final int BUTTON_ID_SECONDARY = 5;
    private static final int BUTTON_ID_TERTIARY = 6;
    private static final int BUTTON_LAYOUT_OK = 0;
    private static final int BUTTON_LAYOUT_OK_CANCEL = 1;
    private static final int BUTTON_LAYOUT_ABORT_RETRY_IGNORE = 2;
    private static final int BUTTON_LAYOUT_YES_NO_CANCEL = 3;
    private static final int BUTTON_LAYOUT_YES_NO = 4;
    private static final int BUTTON_LAYOUT_RETRY_CANCEL = 5;

    //0x68
    public Object payload;
    //0x6c
    public String header;
    //0x70
    public String title;
    //0x74
    public int buttonLayoutMode;

    /**
     * Native: HeaderDialogVisualObject::HeaderDialogVisualObject @004431DD.
     * Fully ported.
     */
    public HeaderDialogVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            Object payload,
            String title,
            int buttonLayoutMode,
            String header
    ) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        this.payload = payload;
        this.title = title;
        this.header = header;
        this.buttonLayoutMode = buttonLayoutMode;
    }

    /**
     * vtbl +0x78: HeaderDialogVisualObject::Initialize @00443238.
     * Fully ported.
     */
    @Override
    public void initialize() {
        CBitmapFont dialogFont = Globals.fonts.font1;
        int dialogWidth = cRect.width();
        int dialogHeight = cRect.height();
        CRect contentRect = new CRect();

        if (header == null || header.isEmpty()) {
            contentRect.set(0x28, 0x38, dialogWidth - 0x28, dialogHeight - 0x58);
        } else if (dialogWidth < dialogFont.getTextWidth(header)) {
            WrappedTextSourceListVisualObject wrappedHeader = new WrappedTextSourceListVisualObject(
                    0,
                    0x28,
                    0x20,
                    dialogWidth - 0x32,
                    0x68,
                    header,
                    dialogFont,
                    Palettes.grayDim,
                    0
            );
            addChild(wrappedHeader);
            wrappedHeader.configureWrappedTextSourceRows();
            contentRect.set(0x28, 0x74, dialogWidth - 0x28, dialogHeight - 0x70);
        } else {
            addChild(new DialogWindowVisualObject(
                    1,
                    0x28,
                    0x20,
                    dialogWidth - 0x28,
                    0x38,
                    header,
                    dialogFont,
                    Palettes.grayDim,
                    TextAlign.CENTER.mask
            ));
            contentRect.set(0x28, 0x44, dialogWidth - 0x28, dialogHeight - 0x70);
        }

        if (title != null && !title.isEmpty()) {
            contentRect.top += 0x0C;
            contentRect.bottom += 0x0C;
            addChild(new DialogWindowVisualObject(
                    -1,
                    contentRect.left,
                    contentRect.top - 0x14,
                    contentRect.right,
                    contentRect.top - 4,
                    title,
                    dialogFont,
                    Palettes.grayDim,
                    0
            ));
        }

        CVisualObject contentChild = createDialogContent(payload, new CRect(contentRect));
        CRect contentChildRect = contentChild.getRect();
        cRect.bottom = cRect.top + 0x90 + contentChildRect.bottom;
        centerOnScreen(Globals.screenRect.right, Globals.screenRect.bottom);

        int buttonTop = cRect.height() - 0x3C;
        int buttonBottom = cRect.height() - 0x24;
        CRect centerButtonRect = new CRect((dialogWidth / 2) - 0x30, buttonTop, (dialogWidth / 2) + 0x30, buttonBottom);
        CRect leftWideButtonRect = new CRect(dialogWidth / 7, buttonTop, (dialogWidth * 3) / 7, buttonBottom);
        CRect rightWideButtonRect = new CRect((dialogWidth * 4) / 7, buttonTop, (dialogWidth * 6) / 7, buttonBottom);
        CRect leftNarrowButtonRect = new CRect((dialogWidth * 3) / 0x14, buttonTop, (dialogWidth * 7) / 0x14, buttonBottom);
        CRect middleNarrowButtonRect = new CRect((dialogWidth * 8) / 0x14, buttonTop, (dialogWidth * 0xC) / 0x14, buttonBottom);
        CRect rightNarrowButtonRect = new CRect((dialogWidth * 0xD) / 0x14, buttonTop, (dialogWidth * 0x11) / 0x14, buttonBottom);

        List<CommandButtonVisualObject> buttons = new ArrayList<>();
        switch (buttonLayoutMode) {
            case BUTTON_LAYOUT_OK -> buttons.add(createDialogButton(
                    BUTTON_ID_PRIMARY,
                    centerButtonRect,
                    get(DIALOGS, OK_0),
                    DIALOG_OK,
                    true,
                    dialogFont
            ));
            case BUTTON_LAYOUT_OK_CANCEL -> {
                buttons.add(createDialogButton(
                        BUTTON_ID_PRIMARY,
                        leftWideButtonRect,
                        get(DIALOGS, OK_0),
                        DIALOG_OK,
                        true,
                        dialogFont
                ));
                buttons.add(createDialogButton(
                        BUTTON_ID_SECONDARY,
                        rightWideButtonRect,
                        get(DIALOGS, CANCEL_1),
                        RETURN_TO_GAME,
                        false,
                        dialogFont
                ));
            }
            case BUTTON_LAYOUT_ABORT_RETRY_IGNORE -> {
                buttons.add(createDialogButton(
                        BUTTON_ID_PRIMARY,
                        leftNarrowButtonRect,
                        get(DIALOGS, ABORT_2),
                        HEADER_DIALOG_ABORT,
                        false,
                        dialogFont
                ));
                buttons.add(createDialogButton(
                        BUTTON_ID_SECONDARY,
                        middleNarrowButtonRect,
                        get(DIALOGS, RETRY_3),
                        HEADER_DIALOG_RETRY,
                        true,
                        dialogFont
                ));
                buttons.add(createDialogButton(
                        BUTTON_ID_TERTIARY,
                        rightNarrowButtonRect,
                        get(DIALOGS, IGNORE_4),
                        HEADER_DIALOG_IGNORE,
                        false,
                        dialogFont
                ));
            }
            case BUTTON_LAYOUT_YES_NO_CANCEL -> {
                buttons.add(createDialogButton(
                        BUTTON_ID_PRIMARY,
                        leftNarrowButtonRect,
                        get(MAIN_YES_75),
                        HEADER_DIALOG_YES,
                        true,
                        dialogFont
                ));
                buttons.add(createDialogButton(
                        BUTTON_ID_SECONDARY,
                        middleNarrowButtonRect,
                        get(MAIN_NO_76),
                        HEADER_DIALOG_NO,
                        false,
                        dialogFont
                ));
                buttons.add(createDialogButton(
                        BUTTON_ID_TERTIARY,
                        rightNarrowButtonRect,
                        get(DIALOGS, CANCEL_1),
                        RETURN_TO_GAME,
                        false,
                        dialogFont
                ));
            }
            case BUTTON_LAYOUT_YES_NO -> {
                buttons.add(createDialogButton(
                        BUTTON_ID_PRIMARY,
                        leftWideButtonRect,
                        get(MAIN_YES_75),
                        HEADER_DIALOG_YES,
                        true,
                        dialogFont
                ));
                buttons.add(createDialogButton(
                        BUTTON_ID_SECONDARY,
                        rightWideButtonRect,
                        get(MAIN_NO_76),
                        HEADER_DIALOG_NO,
                        false,
                        dialogFont
                ));
            }
            case BUTTON_LAYOUT_RETRY_CANCEL -> {
                buttons.add(createDialogButton(
                        BUTTON_ID_PRIMARY,
                        leftWideButtonRect,
                        get(DIALOGS, RETRY_3),
                        HEADER_DIALOG_RETRY,
                        true,
                        dialogFont
                ));
                buttons.add(createDialogButton(
                        BUTTON_ID_SECONDARY,
                        rightWideButtonRect,
                        get(DIALOGS, CANCEL_1),
                        RETURN_TO_GAME,
                        false,
                        dialogFont
                ));
            }
            default -> {
            }
        }

        for (CommandButtonVisualObject button : buttons) {
            addChild(button);
        }
        linkButtonsHorizontally(buttons);
        linkContentDownNeighbors(contentChild, buttons);
    }

    /**
     * vtbl +0x48: HeaderDialogVisualObject::OnMessage @004441C1.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        if (!msg.isBetween(DIALOG_OK, HEADER_DIALOG_IGNORE)) {
            return super.onMessage(msg, wParam, lParam);
        }

        int result = super.onMessage(RETURN_TO_GAME, wParam, lParam);
        closeReasonMessage = msg;
        onHeaderDialogAction(msg);
        return result;
    }

    /**
     * vtbl +0x88: HeaderDialogVisualObject::createDialogContent pure-virtual slot uses _purecall @00584080.
     * Fully ported as an abstract Java method.
     */
    protected abstract CVisualObject createDialogContent(Object payload, CRect contentRect);

    /**
     * vtbl +0x8C: HeaderDialogVisualObject::OnHeaderDialogAction @00444236.
     * Fully ported.
     */
    protected void onHeaderDialogAction(@SuppressWarnings("unused") MessageCodes action) {
    }

    /**
     * Native button-construction branches inside HeaderDialogVisualObject::Initialize @00443238.
     */
    private static CommandButtonVisualObject createDialogButton(
            int id,
            CRect rect,
            String caption,
            MessageCodes msg,
            boolean defaultButton,
            CBitmapFont dialogFont
    ) {
        CommandButtonVisualObject button = new CommandButtonVisualObject(
                id,
                rect,
                caption,
                dialogFont,
                null,
                msg,
                0,
                null
        );
        if (defaultButton) {
            button.setStateFlag(0x10, 1);
        }
        return button;
    }

    /**
     * Native helper pattern `FUN_004D4A8C` used by HeaderDialogVisualObject::Initialize @00443238.
     */
    private static void linkButtonsHorizontally(List<CommandButtonVisualObject> buttons) {
        for (int index = 0; index + 1 < buttons.size(); index++) {
            CommandButtonVisualObject leftButton = buttons.get(index);
            CommandButtonVisualObject rightButton = buttons.get(index + 1);
            leftButton.rightNeighbor = rightButton;
            rightButton.leftNeighbor = leftButton;
        }
    }

    /**
     * Native helper pattern `FUN_004D4ACA` used by HeaderDialogVisualObject::Initialize @00443238.
     */
    private static void linkContentDownNeighbors(CVisualObject contentChild, List<CommandButtonVisualObject> buttons) {
        CommandButtonVisualObject secondaryButton = buttons.size() > 1 ? buttons.get(1) : null;
        CommandButtonVisualObject tertiaryButton = buttons.size() > 2 ? buttons.get(2) : null;
        CommandButtonVisualObject primaryButton = !buttons.isEmpty() ? buttons.get(0) : null;

        if (secondaryButton != null) {
            secondaryButton.upNeighbor = contentChild;
            contentChild.downNeighbor = secondaryButton;
        }
        if (tertiaryButton != null) {
            tertiaryButton.upNeighbor = contentChild;
            contentChild.downNeighbor = tertiaryButton;
        }
        if (primaryButton != null) {
            primaryButton.upNeighbor = contentChild;
            contentChild.downNeighbor = primaryButton;
        }
    }

}
