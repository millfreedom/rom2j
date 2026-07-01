package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.CSprite256;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palettes;

/**
 * Native class: SpriteCommandButtonVisualObject.
 * Purpose: scrollable-panel sprite button that toggles its parent panel visibility and renders a sprite frame instead of caption text.
 */
public class SpriteCommandButtonVisualObject extends CommandButtonVisualObject {
    public static final int NATIVE_SIZE = 0x80; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!
    private static final int STATE_VISIBLE = 0x8;

    //0x78
    public CSprite256 sprite256;
    //0x7c
    public int baseFrameIndex;

    /**
     * Native: SpriteCommandButtonVisualObject::SpriteCommandButtonVisualObject @004DE23F.
     * Fully ported.
     */
    public SpriteCommandButtonVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, CSprite256 sprite256, int baseFrameIndex, MessageCodes msg, int hotKey, String name) {
        super(id, xLeft, yTop, xRight, yBottom, "", Globals.fonts.font1, Palettes.grayDim, msg, hotKey, name);
        this.sprite256 = sprite256;
        this.baseFrameIndex = baseFrameIndex;
    }

    /**
     * Native: SpriteCommandButtonVisualObject::SpriteCommandButtonVisualObject @004DE2A2.
     * Fully ported.
     */
    public SpriteCommandButtonVisualObject(int id, CRect rect, CSprite256 sprite256, int baseFrameIndex, MessageCodes msg, int hotKey, String name) {
        super(id, rect, "", Globals.fonts.font1, Palettes.grayDim, msg, hotKey, name);
        this.sprite256 = sprite256;
        this.baseFrameIndex = baseFrameIndex;
    }

    /**
     * vtbl +0x24: SpriteCommandButtonVisualObject::SetVisible @004DE399.
     * Fully ported.
     */
    @Override
    public void setVisible(int bVisible) {
        super.setVisible(bVisible);

        if (bVisible != 0) {
            if (m_pParent.checkStateFlag(STATE_VISIBLE) == 0) {
                m_pParent.setVisible(1);
            }
            return;
        }

        if (m_pParent.checkStateFlag(STATE_VISIBLE) != 0
                && ((ScrollablePanelVisualObject) m_pParent).expandedFlag == 0) {
            m_pParent.setVisible(0);
        }
    }

    /**
     * vtbl +0x2C: SpriteCommandButtonVisualObject::Update @004DE460.
     * Fully ported.
     */
    @Override
    public void update() {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        m_pParent.renderSelf(screenRect);

        int frameIndex = hoverState == 0 ? baseFrameIndex : baseFrameIndex + 1;
        sprite256.draw(screenRect.left, screenRect.top, frameIndex, 0, false);
    }

    /**
     * vtbl +0x4C: SpriteCommandButtonVisualObject::OnMouseMove @004DE43F.
     * Fully ported.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        return super.onMouseMove(nFlags, x, y);
    }

    /**
     * vtbl +0x54: SpriteCommandButtonVisualObject::OnLButtonDown @004DE41E.
     * Fully ported.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        return super.onLButtonDown(nFlags, x, y);
    }

    /**
     * vtbl +0x58: SpriteCommandButtonVisualObject::OnLButtonUp @004DE2F9.
     * Fully ported.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        if (m_pParent == null || checkStateFlag(STATE_ACTIVE) == 0) {
            return 0;
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        if (pressedState != 0) {
            setPressedState(false);
            if (screenRect.contains(x, y)) {
                m_pParent.onMessage(msg, 0, 0);
            }
            return 1;
        }
        return 0;
    }
}
