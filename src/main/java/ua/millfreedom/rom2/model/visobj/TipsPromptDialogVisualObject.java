package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.window.DialogsMaskFlag;

import java.awt.*;

import static ua.millfreedom.rom2.Utils.point;
import static ua.millfreedom.rom2.model.enums.MessageCodes.DIALOG_OK;
import static ua.millfreedom.rom2.model.enums.MessageCodes.CLEAR_TIP_PROMPT;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RETURN_TO_GAME;
import static ua.millfreedom.rom2.model.enums.MessageCodes.TEXT_LIST_SELECTION_CHANGED;
import static ua.millfreedom.rom2.model.enums.MessageCodes.WM_KEYDOWN;
import static ua.millfreedom.rom2.model.enums.MessageCodes.WM_LBUTTONUP;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.SHOP_DIALOG;
import static ua.millfreedom.rom2.model.window.windowproc.handlers.CMainWindowWindowProcSupport.readMessageInt;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.MainText.CLOSE_127;
import static ua.millfreedom.rom2.text.MainText.SHOW_TIPS_NEXT_TIME_128;

/**
 * Native class: TipsPromptDialogVisualObject (vtbl @0x005D0568).
 * Purpose: framed tips prompt with wrapped text, a close button, and a "show tips next time" checkbox.
 */
public class TipsPromptDialogVisualObject extends HandlerVisualObject {
    public static final int NATIVE_SIZE = 0x74; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int MESSAGE_TEXT_ID = 0x0D;
    private static final int CLOSE_BUTTON_ID = 0x0E;
    private static final int SHOW_TIPS_CHECKBOX_ID = 0x0F;
    private static final int FRAME_BOTTOM_RIGHT_OFFSET = 8;
    private static final int FRAME_CORNER_SIZE = 0x20;
    private static final int FRAME_ALPHA_OFFSET = 0x18;
    private static final int FRAME_TILE_WIDTH = 0x30;
    private static final int FRAME_TILE_HEIGHT = 0x20;

    public WrappedTextSourceListVisualObject messageText;
    public CommandButtonVisualObject closeButton;
    public StringListVariantCVisualObject showTipsCheckbox;

    /**
     * Native: TipsPromptDialogVisualObject::TipsPromptDialogVisualObject @004DEB86.
     * Fully ported.
     */
    public TipsPromptDialogVisualObject() {
        super();
    }

    /**
     * Native: TipsPromptDialogVisualObject::TipsPromptDialogVisualObject @004DEBA5.
     * Fully ported.
     */
    public TipsPromptDialogVisualObject(int id, CRect rect, String promptText) {
        super(id, rect.left, rect.top, rect.right, rect.bottom, null);
        initializePromptControls(promptText);
    }

    /**
     * Native: TipsPromptDialogVisualObject::TipsPromptDialogVisualObject @004DEC24.
     * Fully ported.
     */
    public TipsPromptDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, String promptText) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        initializePromptControls(promptText);
    }

    /**
     * Native support: TipsPromptDialogVisualObject::initializePromptControls @004DECA4.
     * Fully ported.
     */
    private void initializePromptControls(String promptText) {
        CBitmapFont font2 = Globals.fonts.font2;
        int width = cRect.width();
        int height = cRect.height();

        messageText = new WrappedTextSourceListVisualObject(
                MESSAGE_TEXT_ID,
                0x14,
                0x18,
                width - 0x1C,
                height - 0x24,
                promptText,
                font2,
                Palettes.yellowish,
                0
        );
        addChild(messageText);

        closeButton = new CommandButtonVisualObject(
                CLOSE_BUTTON_ID,
                width - 0x78,
                height - 0x28,
                width - 0x28,
                height - 0x16,
                get(CLOSE_127),
                font2,
                Palettes.yellowish,
                CLEAR_TIP_PROMPT,
                0,
                null
        );
        addChild(closeButton);

        showTipsCheckbox = new StringListVariantCVisualObject(
                SHOW_TIPS_CHECKBOX_ID,
                0x28,
                height - 0x28,
                width - 0x7C,
                height - 0x18,
                font2,
                Palettes.yellowish,
                null
        );
        showTipsCheckbox.addRow(get(SHOW_TIPS_NEXT_TIME_128));
        addChild(showTipsCheckbox);
        showTipsCheckbox.setSelectionValue(1);
    }

    /**
     * Java convenience wrapper for callers that set prompt text after allocation.
     * not ported.
     */
    public TipsPromptDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        this(id, xLeft, yTop, xRight, yBottom, "");
    }

    /**
     * Native: TipsPromptDialogVisualObject::SetPromptText @004DF256.
     * Fully ported.
     */
    public void setPromptText(String text) {
        messageText.setSourceText(text);
    }

    /**
     * vtbl +0x30: TipsPromptDialogVisualObject::RenderSelf @004DEEB0.
     * Fully ported.
     */
    @Override
    public void renderSelf(CRect clipRect) {
        CRect frameRect = new CRect();
        clientToScreen(frameRect, cRect);
        frameRect.right -= FRAME_BOTTOM_RIGHT_OFFSET;
        frameRect.bottom -= FRAME_BOTTOM_RIGHT_OFFSET;

        Globals.renderer.pushClip(clipRect.left, clipRect.top, clipRect.right, clipRect.bottom);
        try {
            Globals.renderer.lockSurface();
            try {
                GUI.uiFrameSprite.drawAlpha(frameRect.right - FRAME_ALPHA_OFFSET, frameRect.top + 8, 0x0C, 6, false);
                GUI.uiFrameSprite.drawAlpha(frameRect.left + 8, frameRect.bottom - FRAME_ALPHA_OFFSET, 0x0F, 6, false);
                GUI.uiFrameSprite.drawAlpha(frameRect.right - FRAME_ALPHA_OFFSET, frameRect.bottom - FRAME_ALPHA_OFFSET, 0x11, 6, false);

                for (int i = 0; i < (frameRect.width() - 0x40) / FRAME_TILE_WIDTH; i++) {
                    GUI.uiFrameSprite.drawAlpha(
                            frameRect.left + 0x28 + i * FRAME_TILE_WIDTH,
                            frameRect.bottom - FRAME_ALPHA_OFFSET,
                            0x10,
                            6,
                            false
                    );
                }
                for (int i = 0; i < ((frameRect.height() - 0x40) + (((frameRect.height() - 0x40) >> 31) & 0x1F)) >> 5; i++) {
                    GUI.uiFrameSprite.drawAlpha(
                            frameRect.right - FRAME_ALPHA_OFFSET,
                            frameRect.top + 0x28 + i * FRAME_TILE_HEIGHT,
                            0x0E,
                            6,
                            false
                    );
                }

                GUI.uiFrameSprite.draw(frameRect.left, frameRect.top, 0x0A, 0, false);
                GUI.uiFrameSprite.draw(frameRect.right - FRAME_CORNER_SIZE, frameRect.top, 0x0C, 0, false);
                GUI.uiFrameSprite.draw(frameRect.left, frameRect.bottom - FRAME_CORNER_SIZE, 0x0F, 0, false);
                GUI.uiFrameSprite.draw(frameRect.right - FRAME_CORNER_SIZE, frameRect.bottom - FRAME_CORNER_SIZE, 0x11, 0, false);

                for (int i = 0; i < (frameRect.width() - 0x40) / FRAME_TILE_WIDTH; i++) {
                    int x = frameRect.left + FRAME_CORNER_SIZE + i * FRAME_TILE_WIDTH;
                    GUI.uiFrameSprite.draw(x, frameRect.top, 0x0B, 0, false);
                    GUI.uiFrameSprite.draw(x, frameRect.bottom - FRAME_CORNER_SIZE, 0x10, 0, false);
                }
                for (int i = 0; i < ((frameRect.height() - 0x40) + (((frameRect.height() - 0x40) >> 31) & 0x1F)) >> 5; i++) {
                    int y = frameRect.top + FRAME_CORNER_SIZE + i * FRAME_TILE_HEIGHT;
                    GUI.uiFrameSprite.draw(frameRect.left, y, 0x0D, 0, false);
                    GUI.uiFrameSprite.draw(frameRect.right - FRAME_CORNER_SIZE, y, 0x0E, 0, false);
                }
                for (int xIndex = 0; xIndex < (frameRect.width() - 0x40) / FRAME_TILE_WIDTH; xIndex++) {
                    int x = frameRect.left + FRAME_CORNER_SIZE + xIndex * FRAME_TILE_WIDTH;
                    for (int yIndex = 0; yIndex < ((frameRect.height() - 0x40) + (((frameRect.height() - 0x40) >> 31) & 0x1F)) >> 5; yIndex++) {
                        GUI.uiFrameSprite.draw(x, frameRect.top + FRAME_CORNER_SIZE + yIndex * FRAME_TILE_HEIGHT, 9, 0, false);
                    }
                }
            } finally {
                Globals.renderer.unlockSurface();
            }
        } finally {
            Globals.renderer.popClip();
        }
    }

    /**
     * vtbl +0x48: TipsPromptDialogVisualObject::OnMessage @004DF278.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        int w = readMessageInt(wParam);
        int l = readMessageInt(lParam);
        if (msg == TEXT_LIST_SELECTION_CHANGED && w == SHOW_TIPS_CHECKBOX_ID) {
            setTipsMode(l);
        }
        if (msg == WM_KEYDOWN || msg == DIALOG_OK || msg == RETURN_TO_GAME) {
            return 0;
        }
        Point p = point(l);
        if (msg == WM_LBUTTONUP) {
            onLButtonUp(w, p.x, p.y);
        }
        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * vtbl +0x58: TipsPromptDialogVisualObject::OnLButtonUp @004DF325.
     * Fully ported.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        if (Globals.mainWindow.getUiLockFlag3f4() == 0) {
            return 0;
        }

        int modeFlags = Globals.mainWindow.dialogsMask;
        if (SHOP_DIALOG.isUnsetIn(modeFlags)) {
            if (DialogsMaskFlag.isExactly(modeFlags, GAMEPLAY)) {
                Globals.mainWindow.pMapVisualObject.onLButtonUp(nFlags, x, y);
                return 1;
            }
            return 0;
        }

        CVisualObject inputControllerRoot = Globals.mainWindow.getInputController().getChildById(1000);
        if (inputControllerRoot != null) {
            ((ShopDialogVisualObject) inputControllerRoot).shopCompass.onLButtonUp(nFlags, x, y);
            return 1;
        }
        return 0;
    }

    /**
     * vtbl +0x6C: TipsPromptDialogVisualObject::OnKeyDown @004DF316.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        return 0;
    }

    /**
     * Native support extracted from `g_GamePreferences.TipsMode` write in TipsPromptDialogVisualObject::OnMessage @004DF278.
     */
    private static void setTipsMode(int tipsMode) {
        Globals.gamePreferences.tipsMode = tipsMode;
    }

}
