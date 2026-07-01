package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;

import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_ACCEPT_238;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_BACK_260;
import static ua.millfreedom.rom2.text.StringTableIndex.MAIN_RESET_239;

/**
 * Native class: ActionButtonsPanelVisualObject (vtbl @0x005CB5B8).
 * Purpose: character-generator Accept, Reset, and Back action buttons panel.
 */
public class ActionButtonsPanelVisualObject extends CVisualObject {
    public static final int NATIVE_SIZE = 0xC8; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!
    private static final int BUTTON_COUNT = 3;
    private static final String INN_BUTTON_1_ON_BMP = "graphics/interface/Inn/button1on.bmp";
    private static final String INN_BUTTON_2_ON_BMP = "graphics/interface/Inn/button2on.bmp";
    private static final String INN_BUTTON_3_ON_BMP = "graphics/interface/Inn/button3on.bmp";
    private static final String INN_BUTTON_1_OFF_BMP = "graphics/interface/Inn/button1off.bmp";
    private static final String INN_BUTTON_2_OFF_BMP = "graphics/interface/Inn/button2off.bmp";
    private static final String INN_BUTTON_3_OFF_BMP = "graphics/interface/Inn/button3off.bmp";
    private static final String INN_BUTTONS_AREA_BMP = "graphics/interface/Inn/ButtonsArea.bmp";

    //0x5c
    public CharacterGeneratorDialogVisualObject ownerDialog;
    //0x60
    public final String[] buttonLabels = new String[BUTTON_COUNT];
    //0x74
    public CBmp64k acceptPressedButtonGraphic;
    //0x78
    public CBmp64k resetPressedButtonGraphic;
    //0x7c
    public CBmp64k backPressedButtonGraphic;
    //0x80
    public CBmp64k acceptButtonGraphic;
    //0x84
    public CBmp64k resetButtonGraphic;
    //0x88
    public CBmp64k backButtonGraphic;
    //0x8c
    public CBmp64k backgroundGraphic;
    //0x90
    public final CRect[] buttonRects = {new CRect(), new CRect(), new CRect()};
    //0xc0
    public int pressedButtonIndex;
    //0xc4
    public int hoveredButtonIndex;

    /**
     * Native: ActionButtonsPanelVisualObject::ActionButtonsPanelVisualObject @0042A443.
     * Fully ported.
     */
    public ActionButtonsPanelVisualObject(
            int id,
            int xLeft,
            int yTop,
            int xRight,
            int yBottom,
            CharacterGeneratorDialogVisualObject ownerDialog
    ) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        this.ownerDialog = ownerDialog;
        initializeActionButtonsPanel();
    }

    /**
     * Native: ActionButtonsPanelVisualObject::InitializeActionButtonsPanel @0042A549.
     * Fully ported.
     */
    private void initializeActionButtonsPanel() {
        pressedButtonIndex = -1;
        hoveredButtonIndex = -1;
        acceptPressedButtonGraphic = null;
        resetPressedButtonGraphic = null;
        backPressedButtonGraphic = null;
        acceptButtonGraphic = null;
        resetButtonGraphic = null;
        backButtonGraphic = null;
        backgroundGraphic = null;

        buttonRects[0].set(0x1E4, 0x2C, 0x270, 0x5A);
        buttonRects[1].set(0x1E4, 0x5B, 0x270, 0x89);
        buttonRects[2].set(0x1E4, 0x8A, 0x270, 0xB8);

        buttonLabels[0] = get(MAIN_ACCEPT_238);
        buttonLabels[1] = get(MAIN_RESET_239);
        buttonLabels[2] = get(MAIN_BACK_260);
        m_nState |= 0x2;
    }

    /**
     * Native: ActionButtonsPanelVisualObject::LoadCharacterGeneratorButtonArt @0042AC44 from CharacterGeneratorDialogVisualObject::ShowDialog @0042D8DC.
     * Fully ported.
     */
    void loadCharacterGeneratorButtonArt() {
        releaseCharacterGeneratorButtonArt();
        acceptPressedButtonGraphic = loadButtonGraphic(INN_BUTTON_1_ON_BMP);
        resetPressedButtonGraphic = loadButtonGraphic(INN_BUTTON_2_ON_BMP);
        backPressedButtonGraphic = loadButtonGraphic(INN_BUTTON_3_ON_BMP);
        acceptButtonGraphic = loadButtonGraphic(INN_BUTTON_1_OFF_BMP);
        resetButtonGraphic = loadButtonGraphic(INN_BUTTON_2_OFF_BMP);
        backButtonGraphic = loadButtonGraphic(INN_BUTTON_3_OFF_BMP);
        backgroundGraphic = loadButtonGraphic(INN_BUTTONS_AREA_BMP);
    }

    /**
     * Native: ActionButtonsPanelVisualObject::ReleaseCharacterGeneratorButtonArt @0042AEB4 in CharacterGeneratorDialogVisualObject::HideDialog @0042DE80.
     * Fully ported. Java clears retained bitmap references instead of emulating native delete semantics.
     */
    void releaseCharacterGeneratorButtonArt() {
        acceptPressedButtonGraphic = releaseButtonGraphic(acceptPressedButtonGraphic);
        resetPressedButtonGraphic = releaseButtonGraphic(resetPressedButtonGraphic);
        backPressedButtonGraphic = releaseButtonGraphic(backPressedButtonGraphic);
        acceptButtonGraphic = releaseButtonGraphic(acceptButtonGraphic);
        resetButtonGraphic = releaseButtonGraphic(resetButtonGraphic);
        backButtonGraphic = releaseButtonGraphic(backButtonGraphic);
        backgroundGraphic = releaseButtonGraphic(backgroundGraphic);
    }

    /**
     * vtbl +0x2C: ActionButtonsPanelVisualObject::Update @0042A6EE.
     * Fully ported.
     */
    @Override
    public void update() {
        int ownerLeft = ownerDialog.cRect.left;
        int ownerTop = ownerDialog.cRect.top;
        if (ownerDialog.dialogActiveFlag == 0) {
            return;
        }

        Globals.renderer.lockSurface();
        try {
            drawPanelGraphic(backgroundGraphic, ownerLeft + cRect.left, ownerTop + cRect.top);
            for (int i = 0; i < BUTTON_COUNT; i++) {
                boolean isHovered = hoveredButtonIndex == i;
                boolean isPressed = isHovered && pressedButtonIndex == hoveredButtonIndex;
                CBmp64k buttonGraphic = isPressed
                        ? getPressedButtonGraphicByIndex(i)
                        : getButtonGraphicByIndex(i);
                CRect buttonRect = getButtonRectByIndex(i);
                drawPanelGraphic(buttonGraphic, ownerLeft + buttonRect.left, ownerTop + buttonRect.top);
                drawCenteredButtonLabel(ownerLeft, ownerTop, buttonRect, buttonLabels[i], isHovered, isPressed);
            }
        } finally {
            Globals.renderer.unlockSurface();
        }
    }

    /**
     * vtbl +0x4C: ActionButtonsPanelVisualObject::OnMouseMove @0042A9A6.
     * Fully ported.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        refreshHoveredButtonIndex(nFlags, x, y);
        return 0;
    }

    /**
     * vtbl +0x54: ActionButtonsPanelVisualObject::OnLButtonDown @0042A9C9.
     * Fully ported.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        pressedButtonIndex = getButtonIndexAtScreenPoint(x, y);
        if (pressedButtonIndex >= 0) {
            ownerDialog.playActionButtonSound(pressedButtonIndex);
        }
        return 1;
    }

    /**
     * vtbl +0x58: ActionButtonsPanelVisualObject::OnLButtonUp @0042AA58.
     * Fully ported.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        if (pressedButtonIndex >= 0 && pressedButtonIndex < BUTTON_COUNT) {
            int hitButtonIndex = getButtonIndexAtScreenPoint(x, y);
            if (hitButtonIndex == pressedButtonIndex) {
                int activatedButtonIndex = pressedButtonIndex;
                pressedButtonIndex = -1;
                refreshHoveredButtonIndex(nFlags, x, y);
                ownerDialog.activateActionButton(activatedButtonIndex);
            }
        }
        return 1;
    }

    /**
     * Native: ActionButtonsPanelVisualObject::ResetButtonTracking @0042AB1B.
     * Fully ported.
     */
    void resetButtonTracking() {
        pressedButtonIndex = -1;
        hoveredButtonIndex = -1;
    }

    /**
     * Native helper: ActionButtonsPanelVisualObject::GetButtonIndexAtScreenPoint @0042AB40.
     * Fully ported.
     */
    private int getButtonIndexAtScreenPoint(int x, int y) {
        int ownerLeft = ownerDialog.cRect.left;
        int ownerTop = ownerDialog.cRect.top;
        for (int i = 0; i < BUTTON_COUNT; i++) {
            if (getButtonRectByIndex(i).contains(x - ownerLeft, y - ownerTop)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Native helper: ActionButtonsPanelVisualObject::RefreshHoveredButtonIndex @0042ABCB.
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
     * Native support extracted from ActionButtonsPanelVisualObject::Update @0042A6EE.
     */
    private static void drawPanelGraphic(CBmp64k graphic, int x, int y) {
        graphic.draw(x, y, 0, null, false);
    }

    /**
     * Native support extracted from ActionButtonsPanelVisualObject::Update @0042A6EE.
     */
    private static void drawCenteredButtonLabel(
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
     * Native owner: fixed three-slot pressed-button graphic table in ActionButtonsPanelVisualObject::Update @0042A6EE.
     * Native support extracted from ActionButtonsPanelVisualObject::Update @0042A6EE.
     */
    private CBmp64k getPressedButtonGraphicByIndex(int buttonIndex) {
        return switch (buttonIndex) {
            case 0 -> acceptPressedButtonGraphic;
            case 1 -> resetPressedButtonGraphic;
            case 2 -> backPressedButtonGraphic;
            default -> throw new IndexOutOfBoundsException("buttonIndex: " + buttonIndex);
        };
    }

    /**
     * Native owner: fixed three-slot button graphic table in ActionButtonsPanelVisualObject::Update @0042A6EE.
     * Native support extracted from ActionButtonsPanelVisualObject::Update @0042A6EE.
     */
    private CBmp64k getButtonGraphicByIndex(int buttonIndex) {
        return switch (buttonIndex) {
            case 0 -> acceptButtonGraphic;
            case 1 -> resetButtonGraphic;
            case 2 -> backButtonGraphic;
            default -> throw new IndexOutOfBoundsException("buttonIndex: " + buttonIndex);
        };
    }

    /**
     * Native owner: fixed three-slot button rect table in ActionButtonsPanelVisualObject mouse/update paths.
     * Native support extracted from ActionButtonsPanelVisualObject::Update @0042A6EE and GetButtonIndexAtScreenPoint @0042AB40.
     */
    private CRect getButtonRectByIndex(int buttonIndex) {
        return switch (buttonIndex) {
            case 0, 1, 2 -> buttonRects[buttonIndex];
            default -> throw new IndexOutOfBoundsException("buttonIndex: " + buttonIndex);
        };
    }

    /**
     * Native support extracted from ActionButtonsPanelVisualObject::LoadCharacterGeneratorButtonArt @0042AC44.
     */
    private static CBmp64k loadButtonGraphic(String resourcePath) {
        CBmp64k graphic = new CBmp64k(resourcePath);
        Globals.mousePointer.update();
        return graphic;
    }

    /**
     * Native support extracted from ActionButtonsPanelVisualObject::ReleaseCharacterGeneratorButtonArt @0042AEB4.
     */
    private static CBmp64k releaseButtonGraphic(CBmp64k graphic) {
        return null;
    }
}
