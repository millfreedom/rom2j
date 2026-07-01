package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palettes;

import java.util.Arrays;

import static ua.millfreedom.rom2.model.enums.MessageCodes.DIALOG_OK;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RETURN_TO_GAME;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_ESCAPE;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_RETURN;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_UP;

/**
 * Native class: StatusBanner4b0VisualObject.
 * Purpose: top status banner (`id=0x4B0`) with a text block child.
 */
public class StatusBanner4b0VisualObject extends HandlerVisualObject {
    public static final int NATIVE_SIZE = 0x170; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    //0x68 Native 0x100-byte owner buffer cleared when the banner input opens.
    public final byte[] dialogInputResetBuffer = new byte[0x100];
    //0x168
    public SoundConfigRootVisualObject textBlock;
    //0x16c
    public int dialogActiveFlag;

    /**
     * Native: StatusBanner4b0VisualObject::StatusBanner4b0VisualObject @0043B1C5.
     * Fully ported.
     */
    public StatusBanner4b0VisualObject() {
        super();
        initialize();
    }

    /**
     * Native: StatusBanner4b0VisualObject::StatusBanner4b0VisualObject @0043B219.
     * Fully ported.
     */
    public StatusBanner4b0VisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        initialize();
    }

    /**
     * Native: StatusBanner4b0VisualObject::StatusBanner4b0VisualObject @0043B285.
     * Fully ported.
     */
    public StatusBanner4b0VisualObject(int id, CRect rect) {
        super(id, rect.left, rect.top, rect.right, rect.bottom, null);
        initialize();
    }

    /**
     * vtbl +0x78: StatusBanner4b0VisualObject::Initialize @0043B2E5.
     * Fully ported.
     */
    @Override
    public void initialize() {
        CBitmapFont bitmapFont = Globals.fonts.font1;
        int textHeight = bitmapFont.getHeight() * 3;
        textBlock = new SoundConfigRootVisualObject(
                4,
                0,
                0,
                cRect.width(),
                textHeight,
                bitmapFont,
                Palettes.gray,
                null
        );
        cRect.bottom = cRect.top + 4 + textHeight;
        addChild(textBlock);
        dialogActiveFlag = 0;
    }

    /**
     * vtbl +0x2C: StatusBanner4b0VisualObject::Update @0043B529.
     * Fully ported.
     */
    @Override
    public void update() {
        if (dialogActiveFlag != 0) {
            super.update();
        }
    }

    /**
     * vtbl +0x30: StatusBanner4b0VisualObject::RenderSelf @0043B51C.
     * Fully ported. Native returns without drawing.
     */
    @Override
    public void renderSelf(CRect clipRect) {
    }

    /**
     * vtbl +0x48: StatusBanner4b0VisualObject::OnMessage @0043B474.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * vtbl +0x6C: StatusBanner4b0VisualObject::OnKeyDown @0043B495.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        return switch (nChar) {
            case VK_RETURN -> {
                textBlock.copyCommittedLinesToHistory();
                onMessage(DIALOG_OK, 0, 0);
                yield 1;
            }
            case VK_ESCAPE -> {
                onMessage(RETURN_TO_GAME, 0, 0);
                yield 1;
            }
            case VK_UP -> {
                textBlock.restoreCommittedLinesFromHistory();
                yield super.onKeyDown(nChar);
            }
            default -> super.onKeyDown(nChar);
        };
    }

    /**
     * vtbl +0x80: StatusBanner4b0VisualObject::ShowDialog @0043B3D7.
     * Fully ported.
     */
    @Override
    public void showDialog() {
        Globals.mainWindow.setStatusBannerInputCapture(true);
        Arrays.fill(dialogInputResetBuffer, (byte) 0);
        textBlock.resetInputSession();
        super.showDialog();
        dialogActiveFlag = 1;
        textBlock.lastBlinkTick = Globals.currentTickMillis();
    }

    /**
     * vtbl +0x84: StatusBanner4b0VisualObject::HideDialog @0043B43F.
     * Fully ported.
     */
    @Override
    public HandlerVisualObject hideDialog(MessageCodes reason) {
        Globals.mainWindow.setStatusBannerInputCapture(false);
        dialogActiveFlag = 0;
        return super.hideDialog(reason);
    }

    /**
     * Native: StatusBanner4b0VisualObject::refreshMapPanelLayout @0043B548.
     * Fully ported.
     */
    public void refreshMapPanelLayout() {
        CVisualObject mapVisual = m_pParent;
        int height = cRect.height();
        if (mapVisual.hasSpellPanelChild()) {
            height += Globals.mainWindow.pSpellPanelVisualObject.getRect().height();
        }
        if (mapVisual.hasSelectionPanelChild()) {
            height += Globals.mainWindow.pHeroInventoryControlVisualObject.getRect().height();
        }
        int width = cRect.width();
        int bannerHeight = cRect.height();
        int top = mapVisual.getRect().bottom - height;
        setBounds(0, top, width, top + bannerHeight);
    }
}
