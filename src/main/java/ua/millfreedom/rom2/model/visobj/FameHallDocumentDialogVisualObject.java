package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.*;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.palette.Palettes;

import java.util.ArrayList;
import java.util.List;

import static ua.millfreedom.rom2.model.enums.MessageCodes.DIALOG_OK;
import static ua.millfreedom.rom2.model.enums.MessageCodes.RENDER_FRAME;

/**
 * Native class: FameHallDocumentDialogVisualObject (recovered from `CMainWindow::m_FameHall` usage and `graphics/interface/docs/*`, vtbl @0x005CED10).
 * Purpose: document-sheet viewer for fame-hall entries with previous/next arrows and an OK button.
 */
public class FameHallDocumentDialogVisualObject extends HandlerVisualObject {
    private static final int CLOSE_BUTTON_ID = 4;
    private static final String DOCUMENT_SHEET_BMP = "graphics/interface/docs/sheet.bmp";
    private static final String LEFT_ARROW_DEFAULT_BMP = "graphics/interface/docs/arrows/00_l.bmp";
    private static final String LEFT_ARROW_HOVER_BMP = "graphics/interface/docs/arrows/01_l.bmp";
    private static final String LEFT_ARROW_PRESSED_BMP = "graphics/interface/docs/arrows/11_l.bmp";
    private static final String RIGHT_ARROW_DEFAULT_BMP = "graphics/interface/docs/arrows/00_r.bmp";
    private static final String RIGHT_ARROW_HOVER_BMP = "graphics/interface/docs/arrows/01_r.bmp";
    private static final String RIGHT_ARROW_PRESSED_BMP = "graphics/interface/docs/arrows/11_r.bmp";
    private static final String OK_DEFAULT_BMP = "graphics/interface/docs/ok/ok_off.bmp";
    private static final String OK_ALT_BMP = "graphics/interface/docs/ok/ok_on.bmp";
    private static final String OK_HOVER_BMP = "graphics/interface/docs/ok/ok_l_off.bmp";
    private static final String OK_PRESSED_BMP = "graphics/interface/docs/ok/ok_l_on.bmp";

    public static final int NATIVE_SIZE = 0xF8; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    //0x68
    public CFameHall fameHall;
    //0x6c
    public CBmp64k documentSheetBitmap;
    //0x70
    public final List<CBmp64k> leftArrowBitmaps = new ArrayList<>();
    //0x84
    public final List<CBmp64k> rightArrowBitmaps = new ArrayList<>();
    //0x98
    public final List<CBmp64k> okButtonBitmaps = new ArrayList<>();
    //0xac
    public CBmp64k currentLeftArrowBitmap;
    //0xb0
    public CBmp64k currentRightArrowBitmap;
    //0xb4
    public CBmp64k currentOkButtonBitmap;
    //0xb8
    public int selectedDocumentIndex;
    //0xc0
    public final CRect previousButtonRect = new CRect();
    //0xd0
    public final CRect nextButtonRect = new CRect();
    //0xe0
    public final CRect okButtonRect = new CRect();
    //0xf4
    public int dialogVisibleFlag;

    /**
     * Native: FameHallDocumentDialogVisualObject::FameHallDocumentDialogVisualObject @004AA140.
     * Full port.
     */
    public FameHallDocumentDialogVisualObject() {
        super();
        initialize();
    }

    /**
     * Native: FameHallDocumentDialogVisualObject::FameHallDocumentDialogVisualObject @004AA1F1.
     * Full port.
     */
    public FameHallDocumentDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom) {
        super(id, xLeft, yTop, xRight, yBottom, null);
        initialize();
    }

    /**
     * vtbl +0x14: FameHallDocumentDialogVisualObject::GetText @004AB630.
     * Full port.
     */
    @Override
    public String getText() {
        return null;
    }

    /**
     * vtbl +0x2C: FameHallDocumentDialogVisualObject::Update @004AB033.
     * Full port. Native global surface lock/unlock and PresentFullScreenRenderRegion @00453788 are covered by
     * Java's renderer.
     */
    @Override
    public void update() {
        if (dialogVisibleFlag == 0) {
            return;
        }

        int originX = cRect.left;
        int originY = cRect.top;

        clearScreen();
        if (documentSheetBitmap != null) {
            documentSheetBitmap.drawRectMasked(originX, originY);
        }
        if (currentLeftArrowBitmap != null) {
            currentLeftArrowBitmap.drawRectMasked(originX + previousButtonRect.left, originY + previousButtonRect.top);
        }
        if (currentRightArrowBitmap != null) {
            currentRightArrowBitmap.drawRectMasked(originX + nextButtonRect.left, originY + nextButtonRect.top);
        }
        if (currentOkButtonBitmap != null) {
            currentOkButtonBitmap.drawRectMasked(originX + okButtonRect.left, originY + okButtonRect.top);
        }
        drawCurrentDocument(originX, originY);
        super.update();
    }

    /**
     * vtbl +0x30: FameHallDocumentDialogVisualObject::RenderSelf @004AB1B6.
     * Full port.
     */
    @Override
    public void renderSelf(CRect clipRect) {
        // Native no-op.
    }

    /**
     * vtbl +0x48: FameHallDocumentDialogVisualObject::OnMessage @004AAE0F.
     * Full port. Native leaves the base OnMessage result in EAX.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        if (msg == RENDER_FRAME) {
            draw();
        }
        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * vtbl +0x4C: FameHallDocumentDialogVisualObject::OnMouseMove @004AAE4E.
     * Full port. Native PresentFullScreenRenderRegion @00453788 dirty-region update is covered by Java's
     * full-target renderer.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        int hoveredControl = updateHoveredControl(x, y, (nFlags & 1) != 0);
        refreshNavigationState();
        if (hoveredControl != -1) {
            draw();
        }
        return 0;
    }

    /**
     * vtbl +0x54: FameHallDocumentDialogVisualObject::OnLButtonDown @004AAE97.
     * Full port.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        updateHoveredControl(x, y, (nFlags & 1) != 0);
        refreshNavigationState();
        return 1;
    }

    /**
     * vtbl +0x58: FameHallDocumentDialogVisualObject::OnLButtonUp @004AAFA7.
     * Full port.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        int hoveredControl = updateHoveredControl(x, y, (nFlags & 1) != 0);
        if (hoveredControl == 1) {
            navigatePrevious();
            draw();
        } else if (hoveredControl == 2) {
            navigateNext();
            draw();
        } else if (hoveredControl == 3) {
            sendDialogOkMessage();
        }
        return 1;
    }

    /**
     * vtbl +0x6C: FameHallDocumentDialogVisualObject::OnKeyDown @004AB021.
     * Full port.
     */
    @Override
    public int onKeyDown(int nChar) {
        return 1;
    }

    /**
     * vtbl +0x78: FameHallDocumentDialogVisualObject::Initialize @004AA346.
     * Full port.
     */
    @Override
    public void initialize() {
        fameHall = null;
        documentSheetBitmap = null;
        leftArrowBitmaps.clear();
        rightArrowBitmaps.clear();
        okButtonBitmaps.clear();
        currentLeftArrowBitmap = null;
        currentRightArrowBitmap = null;
        currentOkButtonBitmap = null;
        selectedDocumentIndex = 0;
        previousButtonRect.set(0, 0xC8, 0x38, 0xF0);
        nextButtonRect.set(0x240, 0xC8, 0x27C, 0xF0);
        okButtonRect.set(0x230, 0x1A0, 0x25C, 0x1C0);
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
     * vtbl +0x80: FameHallDocumentDialogVisualObject::ShowDialog @004AAD03.
     * Full port. Native global surface lock/unlock and PresentFullScreenRenderRegion @00453788 are covered by
     * Java's renderer.
     */
    @Override
    public void showDialog() {
        fameHall = Globals.mainWindow.getFameHall();
        Globals.mousePointer.disableBackgroundCapture();
        loadButtonAssets();
        loadDocumentEntries();
        selectedDocumentIndex = 0;
        updateHoveredControl(0, 0, false);
        refreshNavigationState();
        clearScreen();
        dialogVisibleFlag = 1;
        super.showDialog();
        Globals.mousePointer.enableBackgroundCapture();
    }

    /**
     * vtbl +0x84: FameHallDocumentDialogVisualObject::HideDialog @004AADC4.
     * Full port. Native void return is mapped to Java's inherited dialog-chain return contract.
     */
    @Override
    public HandlerVisualObject hideDialog(MessageCodes reason) {
        draw();
        dialogVisibleFlag = 0;
        releaseButtonAssets();
        releaseDocumentEntries();
        fameHall = null;
        return super.hideDialog(reason);
    }

    /**
     * Native support: FameHallDocumentDialogVisualObject::LoadButtonAssets @004AA5B4.
     * Full port. Java nulling/list clearing maps the native delete-and-null ownership cleanup.
     */
    private void loadButtonAssets() {
        releaseButtonAssets();
        documentSheetBitmap = new CBmp64k(DOCUMENT_SHEET_BMP);
        loadBitmapList(
                leftArrowBitmaps,
                LEFT_ARROW_DEFAULT_BMP,
                LEFT_ARROW_HOVER_BMP,
                LEFT_ARROW_PRESSED_BMP
        );
        loadBitmapList(
                rightArrowBitmaps,
                RIGHT_ARROW_DEFAULT_BMP,
                RIGHT_ARROW_HOVER_BMP,
                RIGHT_ARROW_PRESSED_BMP
        );
        loadBitmapList(
                okButtonBitmaps,
                OK_DEFAULT_BMP,
                OK_ALT_BMP,
                OK_HOVER_BMP,
                OK_PRESSED_BMP
        );
        currentLeftArrowBitmap = leftArrowBitmaps.get(0);
        currentRightArrowBitmap = rightArrowBitmaps.get(0);
        currentOkButtonBitmap = okButtonBitmaps.get(0);
    }

    /**
     * Native support: FameHallDocumentDialogVisualObject::ReleaseButtonAssets @004AAA0A.
     * Full port. Java nulling/list clearing maps the native scalar-deleting destructor ownership cleanup.
     */
    private void releaseButtonAssets() {
        documentSheetBitmap = null;
        leftArrowBitmaps.clear();
        rightArrowBitmaps.clear();
        okButtonBitmaps.clear();
        currentLeftArrowBitmap = null;
        currentRightArrowBitmap = null;
        currentOkButtonBitmap = null;
    }

    /**
     * Native support: FameHallDocumentDialogVisualObject::LoadDocumentEntries @004AAC56.
     * Full port.
     */
    private void loadDocumentEntries() {
        releaseDocumentEntries();

        for (ImageContainer document : fameHall.m_Documents) {
            document.loadContent();
        }
    }

    /**
     * Native support: FameHallDocumentDialogVisualObject::ReleaseDocumentEntries @004AACAB.
     * Full port.
     */
    private void releaseDocumentEntries() {
        if (fameHall == null) {
            return;
        }

        for (ImageContainer document : fameHall.m_Documents) {
            document.clear();
        }
    }

    /**
     * Native support: FameHallDocumentDialogVisualObject::RefreshNavigationState @004AB50A.
     * Full port.
     */
    private void refreshNavigationState() {
        if (selectedDocumentIndex == 0) {
            ImageContainer currentDocument = getCurrentDocument();
            if (currentDocument.pageStartLine == 0) {
                currentLeftArrowBitmap = leftArrowBitmaps.get(0);
            }
        }
        if (selectedDocumentIndex == getLastDocumentIndex()) {
            ImageContainer currentDocument = getCurrentDocument();
            if (currentDocument.formattedLines.size() <= currentDocument.pageStartLine + ImageContainer.PAGE_LINE_COUNT) {
                currentRightArrowBitmap = rightArrowBitmaps.get(0);
            }
        }
    }

    /**
     * Native support: imageContainer::DrawContent @004A8841, used by FameHallDocumentDialogVisualObject::Update @004AB033.
     * Full port.
     */
    private void drawCurrentDocument(int originX, int originY) {
        ImageContainer currentDocument = getCurrentDocument();

        if (!currentDocument.textOnly) {
            if (currentDocument.bitmap != null) {
                currentDocument.bitmap.drawRectMasked(
                        originX + currentDocument.rect.left,
                        originY + currentDocument.rect.top
                );
            }
            return;
        }

        CRect documentRect = new CRect(
                originX + currentDocument.rect.left,
                originY + currentDocument.rect.top,
                originX + currentDocument.rect.right,
                originY + currentDocument.rect.bottom
        );
        Globals.fonts.font4.drawWrappedTextRows(
                documentRect,
                currentDocument.pageStartLine,
                currentDocument.pageStartLine + ImageContainer.PAGE_LINE_COUNT,
                currentDocument.formattedLines,
                Palettes.p4.paletteData[0],
                0
        );
    }

    /**
     * Native support: previous document/page navigation helper at @004AAF43.
     * Full port.
     */
    private void navigatePrevious() {
        ImageContainer currentDocument = getCurrentDocument();

        if (!currentDocument.rewindPage()) {
            selectedDocumentIndex--;
            if (selectedDocumentIndex < 1) {
                selectedDocumentIndex = 0;
            }
        }
        refreshNavigationState();
    }

    /**
     * Native support: next document/page navigation helper at @004AAEC8.
     * Full port.
     */
    private void navigateNext() {
        ImageContainer currentDocument = getCurrentDocument();

        if (!currentDocument.advancePage()) {
            selectedDocumentIndex++;
            int lastDocumentIndex = getLastDocumentIndex();
            if (selectedDocumentIndex >= lastDocumentIndex) {
                selectedDocumentIndex = lastDocumentIndex;
            }
        }
        refreshNavigationState();
    }

    /**
     * Native support: `this->OnMessage(0x445, 0, 0)` helper at @004AB1C3.
     * Full port.
     */
    private void sendDialogOkMessage() {
        onMessage(DIALOG_OK, 0, 0);
    }

    /**
     * Native support: FameHallDocumentDialogVisualObject::UpdateHoveredControl @004AB1E2.
     * Full port. Native PresentFullScreenRenderRegion @00453788 dirty-region update is covered by Java's
     * full-target renderer.
     */
    private int updateHoveredControl(int x, int y, boolean pressed) {
        int localX = x - cRect.left;
        int localY = y - cRect.top;

        if (previousButtonRect.contains(localX, localY)) {
            currentLeftArrowBitmap = leftArrowBitmaps.get(pressed ? 2 : 1);
            return 1;
        }

        if (nextButtonRect.contains(localX, localY)) {
            currentRightArrowBitmap = rightArrowBitmaps.get(pressed ? 2 : 1);
            return 2;
        }

        if (okButtonRect.contains(localX, localY)) {
            currentOkButtonBitmap = okButtonBitmaps.get(pressed ? 3 : 2);
            return 3;
        }

        CBmp64k oldLeftBitmap = currentLeftArrowBitmap;
        CBmp64k oldRightBitmap = currentRightArrowBitmap;
        CBmp64k oldOkBitmap = currentOkButtonBitmap;
        currentLeftArrowBitmap = leftArrowBitmaps.get(0);
        currentRightArrowBitmap = rightArrowBitmaps.get(0);
        currentOkButtonBitmap = okButtonBitmaps.get(0);
        if (oldLeftBitmap != currentLeftArrowBitmap
                || oldRightBitmap != currentRightArrowBitmap
                || oldOkBitmap != currentOkButtonBitmap) {
            draw();
        }
        return -1;
    }

    /**
     * Native support extracted from FameHallDocumentDialogVisualObject::RefreshNavigationState @004AB50A,
     * FameHallDocumentDialogVisualObject::NavigatePrevious @004AAF43,
     * FameHallDocumentDialogVisualObject::NavigateNext @004AAEC8, and
     * FameHallDocumentDialogVisualObject::Update @004AB033 CArray<imageContainer>::GetAt calls.
     */
    private ImageContainer getCurrentDocument() {
        return fameHall.m_Documents.get(selectedDocumentIndex);
    }

    /**
     * Native support extracted from FameHallDocumentDialogVisualObject::RefreshNavigationState @004AB50A and
     * FameHallDocumentDialogVisualObject::NavigateNext @004AAEC8 CArray<imageContainer>::GetUpperBound @004AB610 calls.
     */
    private int getLastDocumentIndex() {
        return fameHall.m_Documents.size() - 1;
    }

    /**
     * Java helper for native `CArray<CBmp64k>::SetSize` + per-slot load stores in FameHallDocumentDialogVisualObject::LoadButtonAssets @004AA5B4.
     * not ported.
     */
    private static void loadBitmapList(List<CBmp64k> target, String... resourcePaths) {
        target.clear();
        for (String resourcePath : resourcePaths) {
            target.add(new CBmp64k(resourcePath));
        }
    }

}
