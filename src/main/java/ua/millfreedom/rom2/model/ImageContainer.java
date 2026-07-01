package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CTextFile;
import ua.millfreedom.rom2.Globals;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Native class: imageContainer.
 * Purpose: fame-hall document payload that can display either a bitmap sheet or wrapped text pages.
 */
public class ImageContainer {
    public static final int PAGE_LINE_COUNT = 0x15;

    private static final String DOCUMENT_BITMAP_TEMPLATE = "graphics/interface/docs/%d.bmp";
    private static final String DOCUMENT_TEXT_TEMPLATE = "main/text/docs/%d.txt";
    private static final int DOCUMENT_RECT_LEFT = 0x5C;
    private static final int DOCUMENT_RECT_TOP = 0x48;
    private static final int DOCUMENT_RECT_WIDTH = 0x1C8;
    private static final int DOCUMENT_RECT_HEIGHT = 0x158;

    //0x00
    public int documentId;
    //0x04
    public boolean textOnly;
    //0x08
    public CBmp64k bitmap;
    //0x0c
    public String textContent = "";
    //0x10
    public final List<String> formattedLines = new ArrayList<>();
    //0x24
    public final CRect rect = new CRect();
    //0x34
    public int field34;
    //0x38
    public int pageStartLine;

    /**
     * Native: imageContainer::imageContainer @004A863B.
     * Full port. Java field initializers cover native CString/CStringArray/CRect construction.
     */
    public ImageContainer() {
        documentId = -1;
        textOnly = true;
        bitmap = null;
        textContent = "";
        field34 = 1;
        pageStartLine = 0;
    }

    /**
     * Native: imageContainer::imageContainer(imageContainer const&) @004A86D7.
     * Full port. Java field initializers cover native CString/CStringArray/CRect construction before operator=.
     */
    public ImageContainer(ImageContainer source) {
        copyFrom(source);
    }

    /**
     * Native: imageContainer::operator= @004A85C0.
     * Full port. Native leaves formattedLines unchanged while copying the remaining stored fields.
     */
    public void copyFrom(ImageContainer source) {
        documentId = source.documentId;
        textOnly = source.textOnly;
        bitmap = source.bitmap;
        textContent = source.textContent;
        rect.set(source.rect);
        field34 = source.field34;
        pageStartLine = source.pageStartLine;
    }

    /**
     * Native: imageContainer::Clear @004A8B0F.
     * Full port. Java nulling replaces native CBmp64k scalar-deleting destructor ownership cleanup.
     */
    public void clear() {
        bitmap = null;
        textContent = "";
        formattedLines.clear();
    }

    /**
     * Native: imageContainer::SetDocumentDescriptor @004A879F.
     * Full port.
     */
    public void setDocumentDescriptor(int documentId, boolean textOnly) {
        clear();
        this.documentId = documentId;
        this.textOnly = textOnly;
    }

    /**
     * Native: imageContainer::Write @004A88DB and imageContainer::Read @004A890D storage layout.
     * Full port.
     */
    public void read(ByteBuffer buffer) {
        documentId = buffer.getInt();
        textOnly = buffer.getInt() != 0;
    }

    /**
     * Native: imageContainer::Write @004A88DB.
     * Full port.
     */
    public void write(ByteBuffer buffer) {
        buffer.putInt(documentId);
        buffer.putInt(textOnly ? 1 : 0);
    }

    /**
     * Native: imageContainer::LoadContent @004A893F.
     * Full port.
     */
    public void loadContent() {
        clear();
        rect.set(
                DOCUMENT_RECT_LEFT,
                DOCUMENT_RECT_TOP,
                DOCUMENT_RECT_LEFT + DOCUMENT_RECT_WIDTH,
                DOCUMENT_RECT_TOP + DOCUMENT_RECT_HEIGHT
        );
        if (!textOnly) {
            bitmap = new CBmp64k(DOCUMENT_BITMAP_TEMPLATE.formatted(documentId));
        } else {
            textContent = loadTextContent(DOCUMENT_TEXT_TEMPLATE.formatted(documentId));
            formattedLines.addAll(Globals.fonts.font4.formatText(rect, textContent));
        }
        pageStartLine = 0;
    }

    /**
     * Native: imageContainer::AdvancePage @004A87C5.
     * Full port.
     */
    public boolean advancePage() {
        boolean advanced = pageStartLine + PAGE_LINE_COUNT < formattedLines.size();
        if (advanced) {
            pageStartLine += PAGE_LINE_COUNT;
        }
        return advanced;
    }

    /**
     * Native: imageContainer::RewindPage @004A8802.
     * Full port.
     */
    public boolean rewindPage() {
        if (pageStartLine == 0) {
            return false;
        }
        pageStartLine -= PAGE_LINE_COUNT;
        if (pageStartLine < 0) {
            pageStartLine = 0;
        }
        return true;
    }

    /**
     * Native support extracted from Global::LoadTextFileToOEM @004741AD call in imageContainer::LoadContent @004A893F.
     */
    private static String loadTextContent(String resourcePath) {
        return CTextFile.loadTextFileToOemString(resourcePath);
    }
}
