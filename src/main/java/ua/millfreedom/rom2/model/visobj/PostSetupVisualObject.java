package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;

import java.awt.*;
import java.nio.IntBuffer;

import static ua.millfreedom.rom2.model.enums.MessageCodes.POST_SETUP_SLIDER_RELEASED;
import static ua.millfreedom.rom2.model.enums.MessageCodes.POST_MAIN_WINDOW_SETUP;
import static ua.millfreedom.rom2.model.enums.MessageCodes.TEXT_LIST_PAGE_DOWN;
import static ua.millfreedom.rom2.model.enums.MessageCodes.TEXT_LIST_PAGE_UP;
import static ua.millfreedom.rom2.model.enums.MessageCodes.TEXT_LIST_SELECT_NEXT_ROW;
import static ua.millfreedom.rom2.model.enums.MessageCodes.TEXT_LIST_SELECT_PREVIOUS_ROW;
import static ua.millfreedom.rom2.model.enums.MessageCodes.TEXT_LIST_SELECTION_CHANGED;
import static ua.millfreedom.rom2.model.enums.MessageCodes.TEXT_LIST_SET_SELECTED_ROW;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_LEFT;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.VK_RIGHT;

/**
 * Native class: PostSetupVisualObject.
 * Purpose: posted-setup scrollbar/selector control used by multiple dialog screens.
 */
public class PostSetupVisualObject extends CVisualObject implements TextListSelectionMirror {
    public static final int NATIVE_SIZE = 0xCC; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int STATE_ACTIVE = 0x01;
    private static final int STATE_ENABLED = 0x04;
    private static final int STATE_VISIBLE = 0x08;
    private static final int SHADOW_BRIGHTNESS = 4;

    //0x5c
    public int geometryInitialized;
    //0x60
    public int field0x60;
    //0x64
    public int firstButtonHot;
    //0x68
    public int secondButtonHot;
    //0x6c
    public int thumbDragging;
    //0x70
    public int repeatDirection;
    //0x74
    public final CRect firstButtonRect = new CRect();
    //0x84
    public final CRect secondButtonRect = new CRect();
    //0x94
    public final CRect thumbRect = new CRect();
    //0xa4
    public final CRect leadingTrackRect = new CRect();
    //0xb4
    public final CRect trailingTrackRect = new CRect();
    //0xc4
    public int currentValue;
    //0xc8
    public int maxValue;

    /**
     * Native: PostSetupVisualObject::PostSetupVisualObject @004DA123.
     * Fully ported.
     */
    public PostSetupVisualObject() {
        super();
    }

    /**
     * Native: PostSetupVisualObject::PostSetupVisualObject @004DA54D.
     * Native posts `POST_MAIN_WINDOW_SETUP` asynchronously through `CWnd::PostMessage @0041E3F0`.
     * Fully ported.
     */
    public PostSetupVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, String name) {
        super(id, xLeft, yTop, xRight, yBottom, name);
        initializePostedSetupDefaults();
    }

    /**
     * Native: PostSetupVisualObject::PostSetupVisualObject @004DA661.
     * Native posts `POST_MAIN_WINDOW_SETUP` asynchronously through `CWnd::PostMessage @0041E3F0`.
     * Fully ported.
     */
    public PostSetupVisualObject(int id, CRect rect, String name) {
        super(id, rect, name);
        initializePostedSetupDefaults();
    }

    /**
     * Native support extracted from PostSetupVisualObject constructors @004DA54D and @004DA661.
     */
    private void initializePostedSetupDefaults() {
        this.geometryInitialized = 0;
        this.currentValue = 0;
        this.maxValue = 0;
        this.field0x60 = 0;
        this.firstButtonHot = 0;
        this.secondButtonHot = 0;
        this.thumbDragging = 0;
        this.repeatDirection = 0;
        this.gameDialogControls = null;
        Globals.mainWindow.postMessage(POST_MAIN_WINDOW_SETUP, 0, 0);
    }

    /**
     * vtbl +0x24: PostSetupVisualObject::SetVisible @004DADC2.
     * Fully ported.
     */
    @Override
    public void setVisible(int bVisible) {
        super.setVisible(bVisible);
        field0x60 = bVisible;
    }

    /**
     * vtbl +0x2C: PostSetupVisualObject::Update @004DA80F.
     * Fully ported.
     */
    @Override
    public void update() {
        if (m_pParent == null) {
            return;
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        CRect expandedRect = new CRect(screenRect);
        expandedRect.right += 4;
        expandedRect.bottom += 4;

        Globals.renderer.lockSurface();
        try {
            m_pParent.renderSelf(expandedRect);
            clientToScreen(screenRect, cRect);

            if (isVertical(screenRect)) {
                drawVerticalScrollbar(screenRect);
            } else {
                updateThumbAndTrackRects();
                drawHorizontalScrollbar(screenRect);
            }

            if (checkStateFlag(STATE_ACTIVE) == 0) {
                drawDisabledOutline(screenRect);
            }
        } finally {
            Globals.renderer.unlockSurface();
        }
    }

    /**
     * vtbl +0x34: PostSetupVisualObject::Draw @004DA7B3.
     * Fully ported.
     * Full port for Java's renderer model; native performs a DirectDraw present-region copy after the blit, so Java
     * performs only the draw.
     */
    @Override
    public void draw() {
        if (Globals.isWindowed != 0) {
            return;
        }

        update();
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        screenRect.right += 4;
        screenRect.bottom += 4;
    }

    /**
     * vtbl +0x3C: PostSetupVisualObject::getValue @004E0D40.
     * Fully ported. Native writes the current/max pair through one pointer-sized payload; Java models the recovered
     * `POINT`, `int[2]`, and base-walker `IntBuffer` payload cases.
     */
    @Override
    public void getValue(Object value) {
        if (value instanceof Point outCurrentAndMax) {
            getCurrentValueAndMaxValue(outCurrentAndMax);
            return;
        }
        if (value instanceof int[] values && values.length >= 2) {
            values[0] = currentValue;
            values[1] = maxValue;
            return;
        }
        if (value instanceof IntBuffer values) {
            values.put(0, currentValue);
            values.put(1, maxValue);
            return;
        }
        throw new IllegalArgumentException("PostSetupVisualObject slot 0x3C expects Point, int[2], or IntBuffer payload");
    }

    /**
     * Native support: typed Java adapter for PostSetupVisualObject slot `0x3C` @004E0D40.
     */
    public void getCurrentValueAndMaxValue(Point outCurrentAndMax) {
        outCurrentAndMax.x = currentValue;
        outCurrentAndMax.y = maxValue;
    }

    /**
     * vtbl +0x44: PostSetupVisualObject::setValue @004E0D70.
     * Fully ported. Native reads the current/max pair through one pointer-sized payload; Java models the recovered
     * `POINT`, `int[2]`, and base-walker `IntBuffer` payload cases.
     */
    @Override
    public void setValue(Object value) {
        if (value instanceof Point currentAndMax) {
            setCurrentValueAndMaxValue(currentAndMax);
            return;
        }
        if (value instanceof int[] values && values.length >= 2) {
            currentValue = values[0];
            maxValue = values[1];
            return;
        }
        if (value instanceof IntBuffer values) {
            currentValue = values.get(0);
            maxValue = values.get(1);
            return;
        }
        throw new IllegalArgumentException("PostSetupVisualObject slot 0x44 expects Point, int[2], or IntBuffer payload");
    }

    /**
     * Native support: typed Java adapter for PostSetupVisualObject slot `0x44` @004E0D70.
     */
    public void setCurrentValueAndMaxValue(Point currentAndMax) {
        currentValue = currentAndMax.x;
        maxValue = currentAndMax.y;
    }

    /**
     * vtbl +0x48: PostSetupVisualObject::OnMessage @004DB738.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        if (msg == POST_MAIN_WINDOW_SETUP) {
            initializeInteractionRects();
        }
        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * vtbl +0x4C: PostSetupVisualObject::OnMouseMove @004DB215.
     * Fully ported.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        if (m_pParent == null || checkStateFlag(STATE_ACTIVE) == 0) {
            return 0;
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);

        updateButtonHotState(firstButtonRect.contains(x, y), true);
        updateButtonHotState(secondButtonRect.contains(x, y), false);

        if (checkStateFlag(STATE_ENABLED) == 0 && !isVertical(screenRect)) {
            m_pParent.switchEnabledChild(this, true);
        }

        if (checkStateFlag(STATE_VISIBLE) == 0 || field0x60 == 0) {
            if ((nFlags & 1) != 0) {
                onLButtonDown(nFlags, x, y);
            }
            return 0;
        }

        if (isVertical(screenRect)) {
            int rawValue;
            if (maxValue < 2) {
                rawValue = maxValue - 1;
            } else {
                rawValue = ((maxValue - 1) * (y - screenRect.top - 0x18))
                        / (screenRect.height() - 3 * (screenRect.width() - 4));
            }
            int clampedValue = clampNonNegativeToUpper(rawValue, maxValue - 1);
            m_pParent.onMessage(TEXT_LIST_SET_SELECTED_ROW, id, clampedValue);
            int margin = screenRect.width();
            if (x < screenRect.left - margin || x > screenRect.right + margin
                    || y < screenRect.top - margin || y > screenRect.bottom + margin) {
                setVisible(0);
            }
            return 0;
        }

        if (thumbDragging != 0) {
            currentValue = computeHorizontalValueFromMouseX(x);
            draw();
            notifyParent(TEXT_LIST_SELECTION_CHANGED, currentValue);
        }
        return 0;
    }

    /**
     * vtbl +0x50: PostSetupVisualObject::OnUserMsg @004DB5A0.
     * Fully ported.
     */
    @Override
    public int onUserMsg(int nFlags, int x, int y) {
        if (m_pParent == null || checkStateFlag(STATE_ACTIVE) == 0) {
            return 0;
        }

        if (!isVertical(cRect) && checkStateFlag(STATE_VISIBLE) != 0
                && (firstButtonRect.contains(x, y) || secondButtonRect.contains(x, y))) {
            return onLButtonDown(nFlags | 1, x, y);
        }

        if (isVertical(cRect) && nFlags == 1) {
            return onMouseMove(1, x, y);
        }
        return 1;
    }

    /**
     * vtbl +0x54: PostSetupVisualObject::OnLButtonDown @004DADE4.
     * Fully ported.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        if (m_pParent == null || checkStateFlag(STATE_ACTIVE) == 0) {
            return 0;
        }

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        if (isVertical(screenRect)) {
            handleVerticalButtonDown(y, screenRect);
            return 1;
        }

        if (firstButtonRect.contains(x, y)) {
            currentValue = Math.max(0, currentValue - getPageStep());
        } else if (secondButtonRect.contains(x, y)) {
            currentValue = Math.min(maxValue, currentValue + getPageStep());
        } else {
            currentValue = computeHorizontalValueFromMouseX(x);
            updateThumbAndTrackRects();
            if (thumbDragging == 0) {
                thumbDragging = thumbRect.contains(x, y) ? 1 : 0;
                if (thumbDragging != 0 && checkStateFlag(STATE_VISIBLE) == 0) {
                    setVisible(1);
                }
            }
        }

        draw();
        notifyParent(TEXT_LIST_SELECTION_CHANGED, currentValue);
        return 1;
    }

    /**
     * vtbl +0x58: PostSetupVisualObject::OnLButtonUp @004DB6AA.
     * Fully ported.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        if (m_pParent == null || checkStateFlag(STATE_ACTIVE) == 0) {
            return 0;
        }

        repeatDirection = 0;
        if (field0x60 != 0) {
            notifyParent(POST_SETUP_SLIDER_RELEASED, 0);
            setVisible(0);
            thumbDragging = 0;
        }
        return 0;
    }

    /**
     * vtbl +0x5C: PostSetupVisualObject::OnLButtonDblClk @004E0DA0.
     * Fully ported.
     */
    @Override
    public int onLButtonDblClk(int nFlags, int x, int y) {
        return onLButtonDown(nFlags, x, y) != 0 && onLButtonUp(nFlags, x, y) != 0 ? 1 : 0;
    }

    /**
     * vtbl +0x6C: PostSetupVisualObject::OnKeyDown @004DB76A.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        if (checkStateFlag(STATE_ENABLED) != 0 && !isVertical(cRect)) {
            int pageStep = getPageStep();
            if (nChar == VK_LEFT) {
                currentValue -= Math.min(currentValue, pageStep);
                draw();
                notifyParent(TEXT_LIST_SELECTION_CHANGED, currentValue);
                return 1;
            }
            if (nChar == VK_RIGHT) {
                currentValue += Math.min(pageStep, maxValue - currentValue);
                draw();
                notifyParent(TEXT_LIST_SELECTION_CHANGED, currentValue);
                return 1;
            }
            return 0;
        }
        return super.onKeyDown(nChar);
    }

    /**
     * Native: PostSetupVisualObject::syncSelectionState @004DA769.
     * Fully ported.
     */
    public void syncSelectionState(int selectedRow, int rowCount) {
        if (rowCount >= 0) {
            maxValue = rowCount;
        }
        if (selectedRow >= 0 && selectedRow < maxValue) {
            currentValue = selectedRow;
        }
        draw();
    }

    /**
     * Native helper: PostSetupVisualObject::InitializeInteractionRects @004DA1B2.
     * Fully ported.
     */
    private void initializeInteractionRects() {
        geometryInitialized = 1;

        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        if (isVertical(screenRect)) {
            int extent = screenRect.width();
            firstButtonRect.set(screenRect.left, screenRect.top, screenRect.right, screenRect.top + extent - 4);
            secondButtonRect.set(screenRect.left, screenRect.bottom - extent + 4, screenRect.right, screenRect.bottom);
        } else {
            int extent = screenRect.height();
            firstButtonRect.set(screenRect.left, screenRect.top, screenRect.left + extent - 4, screenRect.bottom);
            secondButtonRect.set(screenRect.right - extent + 4, screenRect.top, screenRect.right, screenRect.bottom);
            m_nState |= 2;
        }
        updateThumbAndTrackRects();
    }

    /**
     * Native helper: PostSetupVisualObject::UpdateThumbAndTrackRects @004DA324.
     * Fully ported.
     */
    private void updateThumbAndTrackRects() {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        if (isVertical(screenRect)) {
            return;
        }

        int thumbLeft;
        if (maxValue == 0) {
            thumbLeft = screenRect.left + screenRect.height() - 4;
        } else {
            thumbLeft = screenRect.left + screenRect.height() - 4
                    + (((screenRect.width() - (screenRect.height() * 2 - 8)) - 0xC) * currentValue) / maxValue;
        }

        thumbRect.set(thumbLeft, screenRect.top, thumbLeft + 0x10, screenRect.bottom);
        leadingTrackRect.set(firstButtonRect.right, firstButtonRect.top, thumbRect.left, thumbRect.bottom);
        trailingTrackRect.set(thumbRect.right, thumbRect.top, secondButtonRect.left, secondButtonRect.bottom);
    }

    /**
     * Native helper: PostSetupVisualObject::ComputeHorizontalValueFromMouseX @004DA4A1.
     * Fully ported.
     */
    private int computeHorizontalValueFromMouseX(int mouseX) {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        int rawValue = (maxValue * (mouseX - (screenRect.left + 2 + screenRect.height())))
                / ((screenRect.width() - (screenRect.height() * 2 - 8)) - 0xC);
        return clampNonNegativeToUpper(rawValue, maxValue);
    }

    /**
     * Native support extracted from PostSetupVisualObject::Update @004DA80F.
     */
    private void drawVerticalScrollbar(CRect screenRect) {
        int thumbOffset = maxValue < 2 ? 0
                : (currentValue * (screenRect.height() - screenRect.width() * 3 + 8)) / (maxValue - 1);

        drawShadowedScrollSprite(screenRect.left, screenRect.top, firstButtonHot == 0 ? 0x12 : 0x15);
        for (int segment = 1; segment < screenRect.height() / screenRect.width(); segment++) {
            int y = screenRect.top + segment * screenRect.width();
            drawShadowedScrollSprite(screenRect.left, y, 0x13);
        }
        drawShadowedScrollSprite(screenRect.left, screenRect.top + screenRect.width() - 4 + thumbOffset, 0x16);
        drawShadowedScrollSprite(screenRect.left, screenRect.bottom - screenRect.width(),
                secondButtonHot == 0 ? 0x14 : 0x17);
    }

    /**
     * Native support extracted from PostSetupVisualObject::Update @004DA80F.
     */
    private void drawHorizontalScrollbar(CRect screenRect) {
        drawShadowedScrollSprite(screenRect.left, screenRect.top, firstButtonHot == 0 ? 0x0 : 0x3);
        for (int segment = 1; segment < screenRect.width() / screenRect.height(); segment++) {
            int x = screenRect.left + segment * screenRect.height();
            drawShadowedScrollSprite(x, screenRect.top, 0x7);
        }
        drawShadowedScrollSprite(thumbRect.left + 1, screenRect.top, 0xA);
        drawShadowedScrollSprite(screenRect.right - screenRect.height(), screenRect.top,
                secondButtonHot == 0 ? 0x8 : 0xB);
    }

    /**
     * Native support extracted from PostSetupVisualObject::Update @004DA80F.
     */
    private void drawShadowedScrollSprite(int x, int y, int frame) {
        GUI.sprScrollBars.drawAlpha(x + 4, y + 4, frame, SHADOW_BRIGHTNESS, false);
        GUI.sprScrollBars.draw(x, y, frame, 0, false);
    }

    /**
     * Native support extracted from the disabled outline branch in PostSetupVisualObject::Update @004DA80F;
     * maps native FUN_004564DF @004564DF to Java renderer shade application.
     */
    private void drawDisabledOutline(CRect screenRect) {
        Globals.renderer.applyShadeToRect(
                screenRect.left - 1,
                screenRect.top - 1,
                screenRect.right + 1,
                screenRect.bottom + 1,
                3
        );
    }

    /**
     * Native support extracted from PostSetupVisualObject::OnLButtonDown @004DADE4.
     */
    private void handleVerticalButtonDown(int mouseY, CRect screenRect) {
        int thumbOffset;
        if (maxValue < 2) {
            thumbOffset = maxValue - 1;
        } else {
            thumbOffset = (currentValue * (screenRect.height() - screenRect.width() * 3 + 8)) / (maxValue - 1);
        }

        int relativeY = mouseY - screenRect.top;
        if (relativeY < screenRect.width()) {
            notifyParent(TEXT_LIST_SELECT_PREVIOUS_ROW, 0);
            return;
        }
        if (screenRect.bottom - mouseY < screenRect.width() - 4) {
            notifyParent(TEXT_LIST_SELECT_NEXT_ROW, 0);
            return;
        }
        if (relativeY - screenRect.width() + 4 < thumbOffset) {
            if (repeatDirection != 2) {
                notifyParent(TEXT_LIST_PAGE_UP, 0);
                repeatDirection = 1;
            }
            return;
        }
        if (relativeY - (screenRect.width() * 2 - 4) < thumbOffset) {
            setVisible(1);
            return;
        }
        if (repeatDirection != 1) {
            notifyParent(TEXT_LIST_PAGE_DOWN, 0);
            repeatDirection = 2;
        }
    }

    /**
     * Native support extracted from PostSetupVisualObject::OnMouseMove @004DB215. The hot-state branch calls direct
     * CVisualObject::SetVisible, leaving PostSetupVisualObject::field0x60 unchanged for hover-only transitions.
     */
    private void updateButtonHotState(boolean isHot, boolean firstButton) {
        int currentFlag = firstButton ? firstButtonHot : secondButtonHot;
        if (isHot) {
            if (currentFlag == 0) {
                if (firstButton) {
                    firstButtonHot = 1;
                } else {
                    secondButtonHot = 1;
                }
                draw();
            }
            if (checkStateFlag(STATE_VISIBLE) == 0) {
                super.setVisible(1);
            }
            return;
        }

        if (currentFlag != 0) {
            super.setVisible(0);
            if (firstButton) {
                firstButtonHot = 0;
            } else {
                secondButtonHot = 0;
            }
            draw();
        }
    }

    /**
     * Native support extracted from PostSetupVisualObject input handlers @004DADE4, @004DB215, @004DB6AA,
     * and @004DB76A.
     */
    private void notifyParent(MessageCodes msg, int lParam) {
        m_pParent.onMessage(msg, id, lParam);
    }

    /**
     * Native support extracted from PostSetupVisualObject::OnLButtonDown @004DADE4 and
     * PostSetupVisualObject::OnKeyDown @004DB76A.
     */
    private int getPageStep() {
        return Math.max(1, maxValue / 16);
    }

    /**
     * Native support extracted from PostSetupVisualObject orientation branches @004DA1B2, @004DA80F,
     * @004DADE4, @004DB215, @004DB5A0, and @004DB76A.
     */
    private static boolean isVertical(CRect rect) {
        return rect.width() < rect.height();
    }

    /**
     * Native support extracted from PostSetupVisualObject clamp patterns @004DA4A1 and @004DB215.
     */
    private static int clampNonNegativeToUpper(int value, int maxValue) {
        int nonNegativeValue = value < 0 ? 0 : value;
        return maxValue < nonNegativeValue ? maxValue : nonNegativeValue;
    }
}
