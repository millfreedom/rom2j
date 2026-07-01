package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.CA16Font;
import ua.millfreedom.rom2.model.CBmp64k;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.CFameHall;
import ua.millfreedom.rom2.model.CMousePointer;
import ua.millfreedom.rom2.model.HighScoreEntry;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.sound.Sound;
import ua.millfreedom.rom2.model.sound.SoundSystem;

import java.util.ArrayList;
import java.util.List;

import static ua.millfreedom.rom2.model.enums.MessageCodes.DIALOG_OK;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RENDER_FRAME;

/**
 * Native class: FameHallDialogVisualObject (recovered from `CMainWindow::m_FameHall` usage and fame-hall assets, vtbl @0x005CD288).
 * Purpose: fame-hall dialog with rank/name/score rows and a bitmap close button.
 */
public class FameHallDialogVisualObject extends HandlerVisualObject {
    private static final int CLOSE_BUTTON_ID = 4;
    private static final String FAME_HALL_BACKGROUND_BMP = "main/graphics/famehall/hall.bmp";
    private static final String CLOSE_BUTTON_OFF_BMP = "graphics/interface/docs/ok/ok_l_off.bmp";
    private static final String CLOSE_BUTTON_ON_BMP = "graphics/interface/docs/ok/ok_l_on.bmp";
    private static final String CLOSE_BUTTON_SFX = "sfx/chrgen/ok.wav";
    private static final int TEXT_SHADOW_OFFSET = 1;

    public static final int NATIVE_SIZE = 0xD0; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    //0x68
    public CFameHall fameHall;
    //0x6c
    public final List<CRect> nameEntryRects = new ArrayList<>();
    //0x80
    public final List<CRect> rankEntryRects = new ArrayList<>();
    //0x94
    public final List<CRect> scoreEntryRects = new ArrayList<>();
    //0xa8
    public final CRect closeButtonRect = new CRect();
    //0xb8
    public CBmp64k backgroundBitmap;
    //0xbc
    public CBmp64k closeButtonOffBitmap;
    //0xc0
    public CBmp64k closeButtonOnBitmap;
    //0xc4
    public CBmp64k currentCloseButtonBitmap;
    //0xc8
    public Sound closeButtonSound;
    //0xcc
    public int dialogVisibleFlag;

    /**
     * Native: FameHallDialogVisualObject::FameHallDialogVisualObject @0045B600.
     * Full port.
     */
    public FameHallDialogVisualObject() {
        super();
        initialize();
    }

    /**
     * Native: FameHallDialogVisualObject::FameHallDialogVisualObject @0045B695.
     * Full port.
     */
    public FameHallDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        initialize();
    }

    /**
     * vtbl +0x14: FameHallDialogVisualObject::GetText @0045C4B0.
     * Full port.
     */
    @Override
    public String getText() {
        return null;
    }

    /**
     * vtbl +0x2C: FameHallDialogVisualObject::Update @0045C159.
     * Full port.
     */
    @Override
    public void update() {
        int originX = cRect.left;
        int originY = cRect.top;

        if (dialogVisibleFlag != 0) {
            CA16Font fameHallFont = Globals.fonts.font4;
            Globals.renderer.lockSurface();
            backgroundBitmap.draw(originX, originY, 0, null, false);
            if (currentCloseButtonBitmap != null) {
                currentCloseButtonBitmap.draw(
                        originX + closeButtonRect.left,
                        originY + closeButtonRect.top,
                        0,
                        null,
                        false
                );
            }

            for (int rowIndex = 0; rowIndex < nameEntryRects.size(); rowIndex++) {
                HighScoreEntry entry = fameHall.m_Entries.get(rowIndex);
                CRect rankRect = rankEntryRects.get(rowIndex);
                CRect nameRect = nameEntryRects.get(rowIndex);
                CRect scoreRect = scoreEntryRects.get(rowIndex);

                drawFameHallTextShadowed(
                        fameHallFont,
                        originX + rankRect.left,
                        originY + rankRect.top,
                        Integer.toString(rowIndex + 1),
                        TextAlign.DEFAULT.mask,
                        Palettes.p4.paletteData[0]
                );
                drawFameHallTextShadowed(
                        fameHallFont,
                        originX + nameRect.left,
                        originY + nameRect.top,
                        entry.m_strName,
                        TextAlign.DEFAULT.mask,
                        Palettes.p4.paletteData[0]
                );
                drawFameHallTextShadowed(
                        fameHallFont,
                        originX + scoreRect.right,
                        originY + scoreRect.top,
                        Utils.formatDecimalThousands(entry.m_nScore),
                        TextAlign.RIGHT.mask,
                        Palettes.p5.paletteData[0]
                );
            }
            Globals.renderer.unlockSurface();
        }

        super.update();
    }

    /**
     * vtbl +0x30: FameHallDialogVisualObject::RenderSelf @0045C3CA.
     * Full port.
     */
    @Override
    public void renderSelf(CRect clipRect) {
        // Native no-op.
    }

    /**
     * vtbl +0x48: FameHallDialogVisualObject::OnMessage @0045BEDF.
     * Full port.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        if (msg == RENDER_FRAME) {
            draw();
        }
        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * vtbl +0x4C: FameHallDialogVisualObject::OnMouseMove @0045BF50.
     * Full port.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        if (Globals.mousePointer.getSourceBitmap() != CMousePointer.Cursor_Select.getBitmap()) {
            CMousePointer.Cursor_Select.setToMousePointer();
        }

        CRect screenCloseButtonRect = new CRect(
                cRect.left + closeButtonRect.left,
                cRect.top + closeButtonRect.top,
                cRect.left + closeButtonRect.right,
                cRect.top + closeButtonRect.bottom
        );
        if (!screenCloseButtonRect.contains(x, y)) {
            currentCloseButtonBitmap = null;
        } else if ((nFlags & 1) == 0) {
            currentCloseButtonBitmap = closeButtonOffBitmap;
        } else {
            currentCloseButtonBitmap = closeButtonOnBitmap;
        }
        return 0;
    }

    /**
     * vtbl +0x54: FameHallDialogVisualObject::OnLButtonDown @0045C023.
     * Full port.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        CRect screenCloseButtonRect = new CRect(
                cRect.left + closeButtonRect.left,
                cRect.top + closeButtonRect.top,
                cRect.left + closeButtonRect.right,
                cRect.top + closeButtonRect.bottom
        );
        if (!screenCloseButtonRect.contains(x, y)) {
            currentCloseButtonBitmap = null;
        } else {
            if (closeButtonSound != null) {
                closeButtonSound.playIfNotPlaying(
                        Globals.soundPreferences.sfxVolume,
                        false,
                        Sound.POINTER_SFX_PRIORITY,
                        0
                );
            }
            currentCloseButtonBitmap = closeButtonOnBitmap;
        }
        return super.onLButtonDown(nFlags, x, y);
    }

    /**
     * vtbl +0x58: FameHallDialogVisualObject::OnLButtonUp @0045C0D3.
     * Full port.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        CRect screenCloseButtonRect = new CRect(
                cRect.left + closeButtonRect.left,
                cRect.top + closeButtonRect.top,
                cRect.left + closeButtonRect.right,
                cRect.top + closeButtonRect.bottom
        );
        if (screenCloseButtonRect.contains(x, y)) {
            onCloseRequested();
        }
        return super.onLButtonUp(nFlags, x, y);
    }

    /**
     * vtbl +0x6C: FameHallDialogVisualObject::OnKeyDown @0045BF1E.
     * Full port.
     */
    @Override
    public int onKeyDown(int nChar) {
        return super.onKeyDown(nChar);
    }

    /**
     * vtbl +0x74: FameHallDialogVisualObject::OnChar @0045BF37.
     * Full port.
     */
    @Override
    public int onChar(int nChar) {
        return super.onChar(nChar);
    }

    /**
     * vtbl +0x78: FameHallDialogVisualObject::Initialize @0045B7CE.
     * Full port after the Java constructor-dispatch guard.
     */
    @Override
    public void initialize() {
        if (nameEntryRects == null || rankEntryRects == null || scoreEntryRects == null || closeButtonRect == null) {
            // Java dispatches to overrides during the base constructor, unlike the native C++ constructor flow.
            return;
        }

        backgroundBitmap = null;
        closeButtonOffBitmap = null;
        closeButtonOnBitmap = null;
        closeButtonSound = null;
        closeButtonRect.set(0x230, 0x1A0, 0x25C, 0x1C0);
        addChild(new CommandButtonVisualObject(
                CLOSE_BUTTON_ID,
                0,
                0,
                0,
                0,
                "",
                Globals.fonts.font1,
                Palettes.grayDim,
                DIALOG_OK,
                0,
                null
        ));
        dialogVisibleFlag = 0;
    }

    /**
     * vtbl +0x80: FameHallDialogVisualObject::ShowDialog @0045BB82.
     * Full port.
     */
    @Override
    public void showDialog() {
        Globals.mousePointer.disableBackgroundCapture();
        fameHall = Globals.mainWindow.getFameHall();
        loadFameHallAssets();
        loadCloseButtonSound();
        int entryCount = getEntryCount();
        resizeRectList(nameEntryRects, entryCount);
        resizeRectList(rankEntryRects, entryCount);
        resizeRectList(scoreEntryRects, entryCount);
        layoutEntryRects();
        dialogVisibleFlag = 1;
        super.showDialog();
        CMousePointer.Cursor_Select.setToMousePointer();
    }

    /**
     * vtbl +0x84: FameHallDialogVisualObject::HideDialog @0045BC39.
     * Full port.
     */
    @Override
    public HandlerVisualObject hideDialog(MessageCodes reason) {
        dialogVisibleFlag = 0;
        releaseFameHallAssets();
        releaseCloseButtonSound();
        fameHall = null;
        nameEntryRects.clear();
        HandlerVisualObject hidden = super.hideDialog(reason);
        Globals.mousePointer.enableBackgroundCapture();
        return hidden;
    }

    /**
     * Native: FameHallDialogVisualObject::LoadFameHallAssets @0045BC8E.
     * Full port.
     */
    private void loadFameHallAssets() {
        releaseFameHallAssets();
        backgroundBitmap = new CBmp64k(FAME_HALL_BACKGROUND_BMP);
        closeButtonOffBitmap = new CBmp64k(CLOSE_BUTTON_OFF_BMP);
        closeButtonOnBitmap = new CBmp64k(CLOSE_BUTTON_ON_BMP);
        currentCloseButtonBitmap = null;
    }

    /**
     * Native: FameHallDialogVisualObject::ReleaseFameHallAssets @0045BDAA.
     * Full port for Java lifecycle: native deletes bitmap objects and clears the same fields.
     */
    private void releaseFameHallAssets() {
        backgroundBitmap = null;
        closeButtonOffBitmap = null;
        closeButtonOnBitmap = null;
        currentCloseButtonBitmap = null;
    }

    /**
     * Native: FameHallDialogVisualObject::LoadCloseButtonSound @0045BEA2.
     * Full port.
     */
    private void loadCloseButtonSound() {
        releaseCloseButtonSound();
        closeButtonSound = loadSound(CLOSE_BUTTON_SFX);
    }

    /**
     * Native: FameHallDialogVisualObject::ReleaseCloseButtonSound @0045BEC3.
     * Full port.
     */
    private void releaseCloseButtonSound() {
        if (closeButtonSound != null) {
            SoundSystem.get().releaseSound(closeButtonSound);
            closeButtonSound = null;
        }
    }

    /**
     * Native: FameHallDialogVisualObject::LayoutEntryRects @0045B90E.
     * Full port.
     */
    private void layoutEntryRects() {
        int fontHeight = Globals.fonts.font4.getFrameHeight();
        for (int index = 0; index < nameEntryRects.size(); index++) {
            int top = 0x91 + (int) (fontHeight * index * 1.5);
            rankEntryRects.get(index).set(0x91, top, 0x91 + 0x19, top + fontHeight);
            nameEntryRects.get(index).set(0xAF, top, 0xAF + 200, top + fontHeight);
            scoreEntryRects.get(index).set(0x181, top, 0x181 + 100, top + fontHeight);
        }
    }

    /**
     * Native: FameHallDialogVisualObject::OnCloseRequested @0045C3D7.
     * Full port.
     */
    private void onCloseRequested() {
        onMessage(DIALOG_OK, 0, 0);
    }

    /**
     * Native owner: `CBitmapFont::DrawTextShadowed` call sites inside FameHallDialogVisualObject::Update @0045C159.
     */
    private static void drawFameHallTextShadowed(
            CA16Font font,
            int x,
            int y,
            String text,
            int alignFlags,
            Palette16 palette
    ) {
        font.drawTextShadowed(x, y, text, alignFlags, palette, TEXT_SHADOW_OFFSET);
    }

    /**
     * Native support extracted from CArray<HighScoreEntry>::GetSize in FameHallDialogVisualObject::ShowDialog @0045BB82.
     */
    private int getEntryCount() {
        return fameHall.m_Entries.size();
    }

    /**
     * Native support extracted from CArray<CRect>::SetSize calls in FameHallDialogVisualObject::ShowDialog @0045BB82.
     */
    private static void resizeRectList(List<CRect> rects, int size) {
        while (rects.size() > size) {
            rects.removeLast();
        }
        while (rects.size() < size) {
            rects.add(new CRect());
        }
    }

    /**
     * Native support extracted from Sound::LoadSound @004384F0.
     */
    private static Sound loadSound(String resourcePath) {
        return new Sound(resourcePath);
    }

}
