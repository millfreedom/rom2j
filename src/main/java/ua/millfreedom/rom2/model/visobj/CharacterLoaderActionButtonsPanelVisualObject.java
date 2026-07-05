package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;

import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_ACCEPT_238;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_CANCEL_78;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_DELETE_240;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_RENAME_241;

/**
 * Native class: CharacterLoaderActionButtonsPanelVisualObject (vtbl @0x005CB7A8).
 * Purpose: setup action pane used as child `id=0x461` under dialog `0x460`.
 */
public class CharacterLoaderActionButtonsPanelVisualObject extends CVisualObject {
    public static final int NATIVE_SIZE = 0xE0; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int BUTTON_COUNT = 4;
    private static final int ACCEPT_BUTTON_INDEX = 0;
    private static final int DELETE_BUTTON_INDEX = 1;
    private static final int RENAME_BUTTON_INDEX = 2;
    private static final int CANCEL_BUTTON_INDEX = 3;
    private static final String SHOP_BUTTON_1_BMP = "graphics/interface/shop_druid/shopbutton1.bmp";
    private static final String SHOP_BUTTON_2_BMP = "graphics/interface/shop_druid/shopbutton2.bmp";
    private static final String SHOP_BUTTON_3_BMP = "graphics/interface/shop_druid/shopbutton3.bmp";
    private static final String SHOP_BUTTON_4_BMP = "graphics/interface/shop_druid/shopbutton4.bmp";
    private static final String BUTTONS_AREA_BMP = "graphics/interface/chrgen/buttonsarea.bmp";

    //0x5c
    public CharacterLoaderDialogVisualObject ownerDialog;

    //0x60
    public final String[] buttonLabels = new String[BUTTON_COUNT];

    //0x74
    public final CBmp64k[] pressedButtonGraphics = new CBmp64k[BUTTON_COUNT];

    //0x84
    public final CBmp64k[] unusedSecondaryButtonGraphics = new CBmp64k[BUTTON_COUNT];

    //0x94
    public CBmp64k backgroundGraphic;

    //0x98
    public final CRect[] buttonRects = {new CRect(), new CRect(), new CRect(), new CRect()};

    //0xd8
    public int pressedButtonIndex;

    //0xdc
    public int hoveredButtonIndex;

    /**
     * Native: CharacterLoaderActionButtonsPanelVisualObject::CharacterLoaderActionButtonsPanelVisualObject @0042F416.
     * Fully ported.
     */
    public CharacterLoaderActionButtonsPanelVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            CharacterLoaderDialogVisualObject ownerDialog
    ) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        this.ownerDialog = ownerDialog;
        initializeActionButtonsPanel();
    }

    /**
     * Native: CharacterLoaderActionButtonsPanelVisualObject::InitializeActionButtonsPanel @0042F51C.
     * Fully ported.
     */
    private void initializeActionButtonsPanel() {
        resetButtonTracking();

        for (int i = 0; i < BUTTON_COUNT; i++) {
            pressedButtonGraphics[i] = null;
            unusedSecondaryButtonGraphics[i] = null;
        }
        backgroundGraphic = null;

        buttonRects[0].set(0x1EE, 0x0F, 0x266, 0x43);
        buttonRects[1].set(0x1E3, 0x43, 0x26F, 0x71);
        buttonRects[2].set(0x1E3, 0x72, 0x26F, 0xA0);
        buttonRects[3].set(0x1EE, 0xA0, 0x266, 0xD4);

        buttonLabels[0] = get(MAIN_ACCEPT_238);
        buttonLabels[1] = get(MAIN_DELETE_240);
        buttonLabels[2] = get(MAIN_RENAME_241);
        buttonLabels[3] = get(MAIN_CANCEL_78);

        m_nState |= 0x2;
    }

    /**
     * vtbl +0x2C: CharacterLoaderActionButtonsPanelVisualObject::Update @0042F718.
     * Fully ported.
     */
    @Override
    public void update() {
        if (ownerDialog.dialogActiveFlag == 0) {
            return;
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        Globals.renderer.lockSurface();
        try {
            drawPanelGraphic(backgroundGraphic, screenRect.left, screenRect.top);
            for (int i = 0; i < BUTTON_COUNT; i++) {
                CRect buttonRect = getButtonRectByIndex(i);
                boolean isHovered = hoveredButtonIndex == i;
                boolean isPressed = pressedButtonIndex >= 0
                        && pressedButtonIndex == hoveredButtonIndex
                        && hoveredButtonIndex == i;
                if (isPressed) {
                    drawPressedButtonGraphic(
                            pressedButtonGraphics[i],
                            ownerDialog.cRect.left + buttonRect.left,
                            ownerDialog.cRect.top + buttonRect.top,
                            buttonRect
                    );
                }
                drawButtonLabel(
                        ownerDialog.cRect.left,
                        ownerDialog.cRect.top,
                        buttonRect,
                        buttonLabels[i],
                        isHovered,
                        isPressed
                );
            }
        } finally {
            Globals.renderer.unlockSurface();
        }
    }

    /**
     * vtbl +0x4C: CharacterLoaderActionButtonsPanelVisualObject::OnMouseMove @0042F9AE.
     * Fully ported.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        refreshHoveredButtonIndex(nFlags, x, y);
        return 0;
    }

    /**
     * vtbl +0x54: CharacterLoaderActionButtonsPanelVisualObject::OnLButtonDown @0042F9D1.
     * Fully ported.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        pressedButtonIndex = getButtonIndexAtScreenPoint(x, y);
        if (pressedButtonIndex >= 0) {
            ownerDialog.playActionButtonPressSound(pressedButtonIndex);
        }
        return 1;
    }

    /**
     * vtbl +0x58: CharacterLoaderActionButtonsPanelVisualObject::OnLButtonUp @0042FA81.
     * Fully ported.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        if (pressedButtonIndex < 0 || pressedButtonIndex >= BUTTON_COUNT) {
            return 1;
        }
        int hitButtonIndex = getButtonIndexAtScreenPoint(x, y);
        if (hitButtonIndex != pressedButtonIndex) {
            return 1;
        }
        int activatedButtonIndex = pressedButtonIndex;
        pressedButtonIndex = -1;
        refreshHoveredButtonIndex(nFlags, x, y);
        if (ownerDialog.isRosterEntryRenameActive()) {
            return 1;
        }
        switch (activatedButtonIndex) {
            case ACCEPT_BUTTON_INDEX -> ownerDialog.acceptCharacterLoaderSelection();
            case DELETE_BUTTON_INDEX -> ownerDialog.deleteSelectedCharacter();
            case RENAME_BUTTON_INDEX -> {
                ownerDialog.startRosterEntryRename();
                ownerDialog.playRenameButtonReleaseSound();
            }
            case CANCEL_BUTTON_INDEX -> ownerDialog.returnToGame();
        }
        return 1;
    }

    /**
     * Native: CharacterLoaderActionButtonsPanelVisualObject::ResetButtonTracking @0042FDE4.
     * Fully ported.
     */
    void resetButtonTracking() {
        pressedButtonIndex = -1;
        hoveredButtonIndex = -1;
    }

    /**
     * Native: CharacterLoaderActionButtonsPanelVisualObject::LoadCharacterLoaderButtonArt @0042FF09.
     * Fully ported.
     */
    void loadCharacterLoaderButtonArt() {
        releaseCharacterLoaderButtonArt();
        pressedButtonGraphics[0] = new CBmp64k(SHOP_BUTTON_1_BMP);
        pressedButtonGraphics[1] = new CBmp64k(SHOP_BUTTON_2_BMP);
        pressedButtonGraphics[2] = new CBmp64k(SHOP_BUTTON_3_BMP);
        pressedButtonGraphics[3] = new CBmp64k(SHOP_BUTTON_4_BMP);
        backgroundGraphic = new CBmp64k(BUTTONS_AREA_BMP);
    }

    /**
     * Native: CharacterLoaderActionButtonsPanelVisualObject::ReleaseCharacterLoaderButtonArt @004300D3.
     * Fully ported. Java clears retained bitmap references instead of emulating native delete semantics.
     */
    void releaseCharacterLoaderButtonArt() {
        for (int i = 0; i < BUTTON_COUNT; i++) {
            pressedButtonGraphics[i] = releaseGraphic(pressedButtonGraphics[i]);
            unusedSecondaryButtonGraphics[i] = releaseGraphic(unusedSecondaryButtonGraphics[i]);
        }
        backgroundGraphic = releaseGraphic(backgroundGraphic);
    }

    /**
     * Native: CharacterLoaderActionButtonsPanelVisualObject::GetButtonIndexAtScreenPoint @0042FE09.
     * Fully ported.
     */
    private int getButtonIndexAtScreenPoint(int x, int y) {
        int localX = x - ownerDialog.cRect.left;
        int localY = y - ownerDialog.cRect.top;
        for (int i = 0; i < BUTTON_COUNT; i++) {
            if (getButtonRectByIndex(i).contains(localX, localY)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Native: CharacterLoaderActionButtonsPanelVisualObject::RefreshHoveredButtonIndex @0042FE90.
     * Fully ported.
     */
    private void refreshHoveredButtonIndex(int mouseFlags, int x, int y) {
        int buttonIndex = getButtonIndexAtScreenPoint(x, y);
        if (buttonIndex < 0 || (mouseFlags & 1) != 0) {
            if (buttonIndex >= 0 && buttonIndex == pressedButtonIndex && (mouseFlags & 1) != 0) {
                hoveredButtonIndex = buttonIndex;
            } else {
                hoveredButtonIndex = -1;
            }
            return;
        }
        hoveredButtonIndex = buttonIndex;
    }

    /**
     * Native support extracted from CharacterLoaderActionButtonsPanelVisualObject::Update @0042F718.
     * Fully ported.
     */
    private static void drawPanelGraphic(CBmp64k graphic, int x, int y) {
        graphic.draw(x, y, 0, null, false);
    }

    /**
     * Native support extracted from CharacterLoaderActionButtonsPanelVisualObject::Update @0042F718.
     * Fully ported.
     */
    private static void drawPressedButtonGraphic(CBmp64k graphic, int x, int y, CRect buttonRect) {
        graphic.drawRectMasked(x, y, 0, 0, buttonRect.width(), buttonRect.height());
    }

    /**
     * Native support extracted from CharacterLoaderActionButtonsPanelVisualObject::Update @0042F718.
     * Fully ported.
     */
    private static void drawButtonLabel(
            int ownerLeft,
            int ownerTop,
            CRect buttonRect,
            String label,
            boolean isHovered,
            boolean isPressed
    ) {
        Palette16 palette = isHovered ? Palettes.p2.paletteData[0] : Palettes.p1.paletteData[0];
        int x = ownerLeft + buttonRect.left + buttonRect.width() / 2;
        int y = ownerTop + buttonRect.top + buttonRect.height() / 2 + (isPressed ? 1 : 0);
        Globals.fonts.font4.drawTextInternal(
                x,
                y,
                label,
                TextAlign.combine(TextAlign.CENTER, TextAlign.VERTICAL_CENTER),
                palette
        );
    }

    /**
     * Native support extracted from CharacterLoaderActionButtonsPanelVisualObject::Update @0042F718,
     * CharacterLoaderActionButtonsPanelVisualObject::OnMouseMove @0042F9AE,
     * CharacterLoaderActionButtonsPanelVisualObject::OnLButtonDown @0042F9D1,
     * CharacterLoaderActionButtonsPanelVisualObject::OnLButtonUp @0042FA81,
     * and CharacterLoaderActionButtonsPanelVisualObject::GetButtonIndexAtScreenPoint @0042FE09.
     * Fully ported.
     */
    private CRect getButtonRectByIndex(int buttonIndex) {
        return switch (buttonIndex) {
            case 0, 1, 2, 3 -> buttonRects[buttonIndex];
            default -> throw new IndexOutOfBoundsException("buttonIndex: " + buttonIndex);
        };
    }

    /**
     * Native support extracted from CharacterLoaderActionButtonsPanelVisualObject::ReleaseCharacterLoaderButtonArt @004300D3.
     * Fully ported. Java clears the retained reference after native delete.
     */
    private static CBmp64k releaseGraphic(CBmp64k graphic) {
        return null;
    }

}
