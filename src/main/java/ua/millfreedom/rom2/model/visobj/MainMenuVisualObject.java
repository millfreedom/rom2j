package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CA16;
import ua.millfreedom.rom2.model.CBmp256;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.sound.Sound;
import ua.millfreedom.rom2.res.Resources;

import java.util.ArrayList;
import java.util.List;

import static ua.millfreedom.rom2.model.enums.MessageCodes.*;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.MAIN_MENU;
import static ua.millfreedom.rom2.res.Constants.*;

/**
 * Native class: MainMenuVisualObject.
 * Purpose: main-menu root with mask-driven button hit testing and asset caches.
 */
public class MainMenuVisualObject extends HandlerVisualObject {
    public static final int NATIVE_SIZE = 256; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int BUTTON_COUNT = 8;
    private static final int LABEL_DRAW_X_OFFSET = 232;
    private static final int LABEL_DRAW_Y_OFFSET = 200;
    private static final int UI_SPRITE_DRAW_X_OFFSET = 480;
    private static final int UI_SPRITE_DRAW_Y_OFFSET = 360;
    private static final int MENU_MASK_STRIDE = 0x280;

    private static final String MAIN_MENU_DIR = "mainmenu";
    private static final String MENU_BITMAP = "menu_.bmp";
    private static final String MENU_MASK_BITMAP = "menumask.bmp";
    private static final String UI_CLICK_SOUND = "chrgen/ok.wav";

    // Native table at 0x005CEB48 used by MainMenuVisualObject::Initialize @004A7421.
    private static final CRect[] BUTTON_RECT_TABLE = {
            new CRect(204, 52, 308, 148),
            new CRect(124, 156, 232, 232),
            new CRect(124, 252, 220, 340),
            new CRect(208, 340, 308, 440),
            new CRect(340, 52, 428, 152),
            new CRect(424, 152, 508, 240),
            new CRect(412, 260, 508, 344),
            new CRect(344, 348, 416, 428)
    };

    // Native table at 0x005CEBC8 used by MainMenuVisualObject::Initialize @004A7421.
    private static final CRect[] LABEL_RECT_TABLE = {
            new CRect(116, 64, 324, 202),
            new CRect(88, 88, 324, 240),
            new CRect(88, 236, 324, 388),
            new CRect(116, 272, 324, 412),
            new CRect(320, 64, 532, 204),
            new CRect(324, 88, 556, 240),
            new CRect(324, 236, 560, 388),
            new CRect(320, 272, 532, 412)
    };

    //0x68
    public final List<CBmp64k> buttonBitmaps = new ArrayList<>();
    //0x7c
    public final List<CBmp64k> pressedButtonBitmaps = new ArrayList<>();
    //0x90
    public final List<CBmp64k> buttonLabelBitmaps = new ArrayList<>();
    //0xa4
    public final List<CRect> buttonRects = new ArrayList<>();
    //0xb8
    public final List<CRect> labelRects = new ArrayList<>();
    //0xcc
    public CBmp64k menuBitmap;
    //0xd0
    public CBmp256 menuMaskBitmap;
    //0xd4
    public Sound buttonClickSound;
    //0xd8
    public CBmp64k activeButtonBitmap;
    //0xdc
    public final CRect activeButtonRect = new CRect();
    //0xec
    public int hotButtonIndex;
    //0xf0
    public int activeButtonIndex;
    //0xf4
    public int pressedButtonIndex;
    //0xf8
    public byte disabledButtonMask;
    //0xfc
    public CA16 uiSprite;

    /**
     * Native: MainMenuVisualObject::MainMenuVisualObject @004A7120.
     * Fully ported.
     */
    public MainMenuVisualObject() {
        super();
        initialize();
    }

    /**
     * Native: MainMenuVisualObject::MainMenuVisualObject @004A71D6.
     * Fully ported.
     */
    public MainMenuVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        initialize();
    }

    /**
     * Native: MainMenuVisualObject::MainMenuVisualObject @004A72A4.
     * Fully ported.
     */
    public MainMenuVisualObject(int id, CRect rect) {
        super(id, rect, null);
        initialize();
    }

    /**
     * vtbl +0x78: MainMenuVisualObject::Initialize @004A7421.
     * Fully ported.
     */
    @Override
    public void initialize() {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);

        menuBitmap = null;
        menuMaskBitmap = null;
        buttonClickSound = null;
        activeButtonBitmap = null;
        uiSprite = null;
        buttonRects.clear();
        labelRects.clear();

        for (int i = 0; i < BUTTON_COUNT; i++) {
            CRect buttonRect = BUTTON_RECT_TABLE[i];
            buttonRects.add(new CRect(
                    screenRect.left + buttonRect.left,
                    screenRect.top + buttonRect.top,
                    screenRect.left + buttonRect.right,
                    screenRect.top + buttonRect.bottom
            ));

            CRect labelRect = LABEL_RECT_TABLE[i];
            labelRects.add(new CRect(
                    screenRect.left + labelRect.left,
                    screenRect.top + labelRect.top,
                    screenRect.left + labelRect.right,
                    screenRect.top + labelRect.bottom
            ));
        }

        setDisabledButtonMask(0);
        CVisualObject closeButton = new CommandButtonVisualObject(4, 0, 0, 0, 0, "", Globals.fonts.font1, Palettes.grayDim, HIDDEN_CLOSE_BUTTON_COMMAND, 0, null);
        addChild(closeButton);
    }

    /**
     * vtbl +0x2C: MainMenuVisualObject::Update @004A7B3F.
     * Fully ported.
     */
    @Override
    public void update() {
        if (Globals.mainWindow.dialogsMask != MAIN_MENU.mask) {
            return;
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);

        Globals.renderer.lockSurface();
        try {
            menuBitmap.draw(screenRect.left, screenRect.top, 0, 0, false);

            if (activeButtonBitmap == null) {
                if (hotButtonIndex >= 0) {
                    buttonLabelBitmaps.get(hotButtonIndex).draw(
                            screenRect.left + LABEL_DRAW_X_OFFSET,
                            screenRect.top + LABEL_DRAW_Y_OFFSET,
                            0,
                            0,
                            false
                    );
                }
            } else {
                activeButtonBitmap.draw(activeButtonRect.left, activeButtonRect.top, 0, 0, false);
                if (activeButtonIndex >= 0) {
                    buttonLabelBitmaps.get(activeButtonIndex).draw(
                            screenRect.left + LABEL_DRAW_X_OFFSET,
                            screenRect.top + LABEL_DRAW_Y_OFFSET,
                            0,
                            0,
                            false
                    );
                }
            }

            uiSprite.draw(
                    screenRect.left + UI_SPRITE_DRAW_X_OFFSET,
                    screenRect.top + UI_SPRITE_DRAW_Y_OFFSET,
                    0,
                    0,
                    false
            );
        } finally {
            Globals.renderer.unlockSurface();
        }

    }

    /**
     * vtbl +0x30: MainMenuVisualObject::RenderSelf @004A7CC1.
     * Fully ported.
     */
    @Override
    public void renderSelf(CRect clipRect) {
    }

    /**
     * vtbl +0x48: MainMenuVisualObject::OnMessage @004A7CCE.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        if (msg == RENDER_FRAME) {
            draw();
        }
        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * vtbl +0x4C: MainMenuVisualObject::OnMouseMove @004A813C.
     * Full port.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        updateActiveButtonState(nFlags, x, y);
        return 0;
    }

    /**
     * vtbl +0x54: MainMenuVisualObject::OnLButtonDown @004A815F.
     * Full port.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        updateActiveButtonState(nFlags, x, y);
        if (pressedButtonIndex != -1) {
            buttonClickSound.playIfNotPlaying(
                    Globals.soundPreferences.sfxVolume,
                    false,
                    Sound.POINTER_SFX_PRIORITY,
                    0
            );
        }
        return 1;
    }

    /**
     * vtbl +0x58: MainMenuVisualObject::OnLButtonUp @004A81A3.
     * Full port.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        MessageCodes msg = resolveActivatedMessage();
        if (msg != WM_NULL) {
            Globals.mainWindow.postMessage(msg, 0, 0);
        }
        pressedButtonIndex = -1;
        updateActiveButtonState(nFlags, x, y);
        if (msg != WM_NULL) {
            draw();
            hideDialog(msg);
        }
        return 1;
    }

    /**
     * vtbl +0x80: MainMenuVisualObject::ShowDialog @004A833D.
     * Full port. Native PresentFullScreenRenderRegion @00453788 is covered by Java's full-target renderer.
     */
    @Override
    public void showDialog() {
        loadMenuAssets();
        initializeButtonClickSound();
        Globals.mousePointer.disableBackgroundCapture();
        hotButtonIndex = -1;
        activeButtonIndex = -1;
        pressedButtonIndex = -1;
        activeButtonBitmap = null;
        activeButtonRect.set(0, 0, 0, 0);
        clearScreen();
        super.showDialog();
    }

    /**
     * vtbl +0x84: MainMenuVisualObject::HideDialog @004A83C4.
     * Full port.
     */
    @Override
    public HandlerVisualObject hideDialog(MessageCodes reason) {
        releaseMenuAssets();
        releaseButtonClickSound();
        HandlerVisualObject hidden = super.hideDialog(reason);
        Globals.mainWindow.sendMessage(NOTIFY_DIALOG_CLOSED, this, 0);
        Globals.mousePointer.enableBackgroundCapture();
        return hidden;
    }

    /**
     * Native: MainMenuVisualObject::SetDisabledButtonMask @00493BC0.
     * Fully ported.
     */
    public void setDisabledButtonMask(int disabledButtonMask) {
        this.disabledButtonMask = (byte) disabledButtonMask;
    }

    /**
     * Native: MainMenuVisualObject::LoadMenuAssets @004A7657.
     * Fully ported.
     */
    private void loadMenuAssets() {
        releaseMenuAssets();

        menuMaskBitmap = loadMenuMaskBitmap(MENU_MASK_BITMAP);
        Globals.renderer.refreshMousePointer();
        menuBitmap = loadMainMenuBitmap(MENU_BITMAP);
        Globals.renderer.refreshMousePointer();
        uiSprite = loadUiSprite();
        uiSprite.initPalette(16, 4, 0);
        Globals.renderer.refreshMousePointer();

        for (int i = 0; i < BUTTON_COUNT; i++) {
            int buttonId = i + 1;
            pressedButtonBitmaps.add(loadMainMenuBitmap("button" + buttonId + "p.bmp"));
            Globals.renderer.refreshMousePointer();
            buttonBitmaps.add(loadMainMenuBitmap("button" + buttonId + ".bmp"));
            Globals.renderer.refreshMousePointer();
            buttonLabelBitmaps.add(loadMainMenuBitmap("text" + buttonId + ".bmp"));
            Globals.renderer.refreshMousePointer();
        }

        activeButtonBitmap = null;
    }

    /**
     * Native: MainMenuVisualObject::ReleaseMenuAssets @004A792B.
     * Full port. Java releases cached asset references instead of reproducing native destructor/free semantics.
     */
    private void releaseMenuAssets() {
        menuBitmap = null;
        menuMaskBitmap = null;
        uiSprite = null;
        activeButtonBitmap = null;
        buttonBitmaps.clear();
        pressedButtonBitmaps.clear();
        buttonLabelBitmaps.clear();
    }

    /**
     * Native: MainMenuVisualObject::UpdateActiveButtonState @004A7D0D.
     * Full port.
     */
    private void updateActiveButtonState(int nFlags, int x, int y) {
        if (!cRect.contains(x, y)) {
            return;
        }

        int hoveredButtonIndex = resolveHoveredButtonIndex(x, y);
        activeButtonBitmap = null;
        hotButtonIndex = hoveredButtonIndex;

        if (nFlags == 1) {
            if (pressedButtonIndex == -1) {
                pressedButtonIndex = hotButtonIndex;
                activeButtonIndex = pressedButtonIndex;
            } else if (pressedButtonIndex == hotButtonIndex) {
                activeButtonIndex = pressedButtonIndex;
            } else {
                activeButtonIndex = -1;
            }
        } else if (pressedButtonIndex == -1) {
            activeButtonIndex = hotButtonIndex;
        }

        if (pressedButtonIndex == -1) {
            if (activeButtonIndex != -1) {
                activeButtonBitmap = getBitmapAtIndex(buttonBitmaps, activeButtonIndex);
                copyButtonRect(activeButtonIndex);
            }
        } else if (hotButtonIndex == pressedButtonIndex) {
            activeButtonBitmap = getBitmapAtIndex(pressedButtonBitmaps, pressedButtonIndex);
            copyButtonRect(pressedButtonIndex);
            activeButtonIndex = pressedButtonIndex;
        }

        if (hotButtonIndex != -1 && isButtonDisabled(hotButtonIndex)) {
            activeButtonBitmap = null;
        }
    }

    /**
     * Native: MainMenuVisualObject::InitializeButtonClickSound @004A840E.
     * Full port.
     */
    private void initializeButtonClickSound() {
        releaseButtonClickSound();
        buttonClickSound = new Sound(Resources.path(SFX, UI_CLICK_SOUND));
    }

    /**
     * Native: MainMenuVisualObject::ReleaseButtonClickSound @004A8437.
     * Full port. Java clears the retained reference instead of reproducing native sound destructor semantics.
     */
    private void releaseButtonClickSound() {
        buttonClickSound = null;
    }

    /**
     * Native branch inside MainMenuVisualObject::OnLButtonUp @004A81A3.
     */
    private MessageCodes resolveActivatedMessage() {
        return switch (activeButtonIndex) {
            case 0 -> isButtonDisabled(0) ? WM_NULL : START_NEW_GAME;
            case 1 -> isButtonDisabled(1) ? WM_NULL : SHOW_CHARACTER_LOADER_DIALOG;
            case 2 -> isButtonDisabled(2) ? WM_NULL : VIEW_CUTSCENES;
            case 3 -> isButtonDisabled(3) ? WM_NULL : SHOW_CREDITS_DIALOG;
            case 4 -> isButtonDisabled(4) ? WM_NULL : LOAD_GAME;
            case 5 -> isButtonDisabled(5) ? WM_NULL : SHOW_HAT_SERVER_LIST_DIALOG;
            case 6 -> isButtonDisabled(6) ? WM_NULL : SHOW_FAME_HALL_DIALOG;
            case 7 -> isButtonDisabled(7) ? WM_NULL : WM_CLOSE;
            default -> WM_NULL;
        };
    }

    /**
     * Native helper branch inside MainMenuVisualObject::UpdateActiveButtonState @004A7D0D.
     */
    private int resolveHoveredButtonIndex(int x, int y) {
        int localX = x - cRect.left;
        int localY = y - cRect.top;
        int pixelValue = menuMaskBitmap.frames.getFirst().pixels()[localX + localY * MENU_MASK_STRIDE];
        return switch (pixelValue) {
            case 128 -> 0;
            case 144 -> 1;
            case 160 -> 2;
            case 176 -> 3;
            case 192 -> 4;
            case 208 -> 5;
            case 224 -> 6;
            case 240 -> 7;
            default -> -1;
        };
    }

    /**
     * Native helper branch inside MainMenuVisualObject::UpdateActiveButtonState @004A7D0D.
     */
    private void copyButtonRect(int buttonIndex) {
        activeButtonRect.set(buttonRects.get(buttonIndex));
    }

    /**
     * Native helper branch inside MainMenuVisualObject::OnLButtonUp @004A81A3 and UpdateActiveButtonState @004A7D0D.
     */
    private boolean isButtonDisabled(int buttonIndex) {
        return ((disabledButtonMask & 0xFF) & (1 << (buttonIndex & 0x1F))) != 0;
    }

    /**
     * Native support extracted from CArray<CBmp64k>::GetAt call sites in
     * MainMenuVisualObject::UpdateActiveButtonState @004A7D0D.
     */
    private static CBmp64k getBitmapAtIndex(List<CBmp64k> bitmaps, int index) {
        return bitmaps.get(index);
    }

    /**
     * Native support extracted from MainMenuVisualObject::LoadMenuAssets @004A7657.
     */
    private static CBmp64k loadMainMenuBitmap(String fileName) {
        return new CBmp64k(Resources.path(MAIN, GRAPHICS, MAIN_MENU_DIR, fileName));
    }

    /**
     * Native support extracted from MainMenuVisualObject::LoadMenuAssets @004A7657.
     */
    private static CBmp256 loadMenuMaskBitmap(String fileName) {
        return new CBmp256(Resources.path(MAIN, GRAPHICS, MAIN_MENU_DIR, fileName));
    }

    /**
     * Native support extracted from MainMenuVisualObject::LoadMenuAssets @004A7657.
     */
    private static CA16 loadUiSprite() {
        return new CA16(Resources.path(GRAPHICS, INTERFACE, SPRITES_16A));
    }

}
