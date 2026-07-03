package ua.millfreedom.rom2.model.control;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.window.CMainWindow;

import java.util.ArrayList;
import java.util.List;

/**
 * Native: CGameListControl.
 */
public final class CGameListControl implements MfcSerializable {
    // not ported. Java-only retained history cap for the non-dedicated map chat overlay.
    private static final int NON_DEDICATED_HISTORY_LIMIT = 5000;

    //0x04
    public final List<String> m_arrText = new ArrayList<>();

    //0x18
    public final List<Palette16> m_arrData = new ArrayList<>();

    //0x2c
    public final List<Integer> m_arrStates = new ArrayList<>();

    //0x40
    public int timestamp;

    //0x44
    public int m_nTopIndex;

    //0x48
    public int m_nVisibleLines;

    //0x4c
    public final CRect m_Rect = new CRect();

    //0x5c
    public int m_nState;

    // not ported. Java-only first retained line still participating in the default timed non-dedicated view.
    public int firstRetainedTimedLineIndex;

    /**
     * Native: CGameListControl::New @00401F50.
     * Fully ported. Java field initializers replace native CObject/MFC container construction.
     */
    public CGameListControl() {
        m_nTopIndex = 0;
        m_nState = 1;
    }

    /**
     * Native: CGameListControl::CGameListControl @00401FE8.
     * Fully ported. Java field initializers replace native CObject/MFC container construction.
     */
    public CGameListControl(CRect rect) {
        this();
        m_Rect.set(rect.left, rect.top, rect.right, rect.bottom);
    }

    /**
     * vtbl +0x08: CObject::Serialize @00401970.
     */
    @Override
    public void serialize(CArchive ar) {
        // Native CObject::Serialize is a no-op.
    }

    /**
     * Native: CGameListControl::configureMessageRect @0040212B.
     * Fully ported.
     */
    public void configureMessageRect(CRect rect) {
        m_Rect.set(rect.left, rect.top, rect.right, rect.bottom);
        CMainWindow mainWindow = Globals.mainWindow;
        if (mainWindow.sessionMode == CMainWindow.SESSION_MODE_DEDICATED_SERVER) {
            m_nVisibleLines = (Globals.screenRect.bottom - 0x1E0) / (Globals.fonts.font2.getHeight() + 2) + 0xE;
        } else {
            m_nVisibleLines = Math.max(1, (m_Rect.height() / linePitch(Globals.fonts.font1)) / 2);
        }
    }

    /**
     * Native: CGameListControl::AddTimedLine @004021FE.
     * Fully ported, with Java-only non-dedicated retained-history storage.
     */
    public int addTimedLine(String text, Palette16 palette, int lifetimeMs) {
        m_nState = 1;
        CBitmapFont font = Globals.fonts.font1;
        if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_DEDICATED_SERVER) {
            font = Globals.fonts.font2;
        }
        List<String> lines = font.formatText(m_Rect, text);
        boolean retainHistory = retainsNonDedicatedHistory();
        int firstNewLineIndex = m_arrText.size();
        boolean wasEmpty = retainHistory ? !hasActiveTimedLines() : m_arrText.isEmpty();
        appendFormattedLines(lines, palette, lifetimeMs, false);
        if (wasEmpty && firstNewLineIndex < m_arrText.size()) {
            timestamp = currentTick();
            m_nTopIndex = 0;
            if (retainHistory) {
                firstRetainedTimedLineIndex = firstNewLineIndex;
            }
        }
        if (retainHistory) {
            trimToHistoryLimit();
        } else {
            trimToVisibleLines();
        }
        return m_arrText.size();
    }

    /**
     * Native: CGameListControl::AddUniqueTimedLine @004023AA.
     * Fully ported, with Java-only non-dedicated retained-history storage.
     */
    public void addUniqueTimedLine(String text, Palette16 palette, int lifetimeMs) {
        m_nState = 1;
        List<String> lines = Globals.fonts.font1.formatText(m_Rect, text);
        boolean retainHistory = retainsNonDedicatedHistory();
        int firstNewLineIndex = m_arrText.size();
        boolean wasEmpty = retainHistory ? !hasActiveTimedLines() : m_arrText.isEmpty();
        boolean completed = appendFormattedLines(lines, palette, lifetimeMs, true);
        if (!completed) {
            return;
        }
        if (wasEmpty && firstNewLineIndex < m_arrText.size()) {
            timestamp = currentTick();
            m_nTopIndex = 0;
            if (retainHistory) {
                firstRetainedTimedLineIndex = firstNewLineIndex;
            }
        }
        if (retainHistory) {
            trimToHistoryLimit();
        } else {
            trimToVisibleLines();
        }
    }

    /**
     * Native: CGameListControl::GetSize @0041ECA0.
     * Fully ported.
     */
    public int getSize() {
        return m_arrText.size();
    }

    /**
     * Native: CGameListControl::AdvanceTimedLines @00402585.
     * Fully ported, with Java-only non-dedicated expiry that hides default-view lines without deleting history.
     */
    public void advanceTimedLines() {
        if (retainsNonDedicatedHistory()) {
            advanceRetainedTimedLines();
            return;
        }
        if (!m_arrText.isEmpty()) {
            int currentTick = currentTick();
            m_nTopIndex += currentTick - timestamp;
            timestamp = currentTick;
            if (Integer.compareUnsigned(m_arrStates.getFirst(), m_nTopIndex) < 0) {
                m_nTopIndex = 0;
                removeFirstLine();
            }
        }
    }

    /**
     * Native: CGameListControl::Draw @0040261A.
     * Ported with Java-only clipping and bottom alignment to m_Rect.
     */
    public void draw() {
        CMainWindow mainWindow = Globals.mainWindow;
        Globals.renderer.pushClip(m_Rect.left, m_Rect.top, m_Rect.right, m_Rect.bottom);
        try {
            if (mainWindow.sessionMode == CMainWindow.SESSION_MODE_DEDICATED_SERVER) {
                drawLineRange(0, m_arrText.size(), m_Rect.left, m_Rect.top + 0xDC, Globals.fonts.font2);
                return;
            }

            CBitmapFont font = Globals.fonts.font1;
            int visibleLines = Math.max(1, (m_Rect.height() - 8) / linePitch(font));
            int firstLineIndex = Math.max(firstRetainedTimedLineIndex, m_arrText.size() - visibleLines);
            drawLineRangeBottomAligned(firstLineIndex, m_arrText.size(), m_Rect.left + 8, m_Rect.bottom - 8, font);
        } finally {
            Globals.renderer.popClip();
        }
    }

    /**
     * Native: CGameListControl::Deinit @004021D2.
     * Fully ported.
     */
    public void deinit() {
        m_arrText.clear();
        m_arrData.clear();
        m_arrStates.clear();
        firstRetainedTimedLineIndex = 0;
    }

    /**
     * Native support extracted from CGameListControl::Draw @0040261A for the Java-only history overlay.
     */
    public void drawLineRange(int firstLineIndex, int endLineIndex, int x, int y, CBitmapFont font) {
        int lineOffset = 0;
        for (int lineIndex = firstLineIndex; lineIndex < endLineIndex; lineIndex++) {
            String remaining = m_arrText.get(lineIndex);
            while (true) {
                int carriageReturn = remaining.indexOf('\r');
                if (carriageReturn < 0) {
                    font.drawTextShadowed(
                            x,
                            y + lineOffset,
                            remaining,
                            TextAlign.DEFAULT.mask,
                            m_arrData.get(lineIndex),
                            1);
                } else {
                    font.drawTextShadowed(
                            x,
                            y + lineOffset,
                            remaining.substring(0, Math.max(0, carriageReturn - 1)),
                            TextAlign.DEFAULT.mask,
                            m_arrData.get(lineIndex),
                            1);
                }

                lineOffset += linePitch(font);
                if (carriageReturn < 0) {
                    break;
                }
                remaining = remaining.substring(carriageReturn + 1);
            }
        }
    }

    /**
     * Java-only support for rendering retained chat lines upward from a bottom edge.
     * not ported.
     */
    public void drawLineRangeBottomAligned(int firstLineIndex, int endLineIndex, int x, int bottom, CBitmapFont font) {
        int renderedLineCount = renderedLineCount(firstLineIndex, endLineIndex);
        int y = bottom - renderedLineCount * linePitch(font);
        drawLineRange(firstLineIndex, endLineIndex, x, y, font);
    }

    /**
     * Java-only support for bottom-aligning retained chat lines with the same row splitting as drawLineRange.
     * not ported.
     */
    private int renderedLineCount(int firstLineIndex, int endLineIndex) {
        int count = 0;
        for (int lineIndex = firstLineIndex; lineIndex < endLineIndex; lineIndex++) {
            String remaining = m_arrText.get(lineIndex);
            while (true) {
                count++;
                int carriageReturn = remaining.indexOf('\r');
                if (carriageReturn < 0) {
                    break;
                }
                remaining = remaining.substring(carriageReturn + 1);
            }
        }
        return count;
    }

    /**
     * Native support extracted from CGameListControl::AddTimedLine @004021FE and
     * CGameListControl::AddUniqueTimedLine @004023AA.
     */
    private boolean appendFormattedLines(
            List<String> lines,
            Palette16 palette,
            int lifetimeMs,
            boolean suppressDuplicateSingleLine
    ) {
        for (String formattedLine : lines) {
            String line = formattedLine.replace('\r', ' ');
            if (suppressDuplicateSingleLine
                    && lines.size() == 1
                    && shouldSuppressDuplicateSingleLine(line)) {
                return false;
            }
            m_arrText.add(line);
            m_arrData.add(palette);
            m_arrStates.add(lifetimeMs);
        }
        return true;
    }

    /**
     * Native support extracted from CGameListControl queue RemoveAt(0, 1) sites at @004021FE, @004023AA, and @00402585.
     */
    private void removeFirstLine() {
        m_arrStates.removeFirst();
        m_arrText.removeFirst();
        m_arrData.removeFirst();
    }

    /**
     * Java-only duplicate check that preserves native AddUniqueTimedLine behavior after expired lines are retained.
     * not ported.
     */
    private boolean shouldSuppressDuplicateSingleLine(String line) {
        if (m_arrText.isEmpty()) {
            return false;
        }
        return (!retainsNonDedicatedHistory() || hasActiveTimedLines()) && m_arrText.getLast().equals(line);
    }

    /**
     * Native support extracted from CGameListControl::AddTimedLine @004021FE and
     * CGameListControl::AddUniqueTimedLine @004023AA.
     */
    private void trimToVisibleLines() {
        while (m_arrText.size() > m_nVisibleLines) {
            removeFirstLine();
        }
    }

    /**
     * Java-only retained-history cap for non-dedicated CGameListControl; native removed lines at visible-count limits.
     * not ported.
     */
    private void trimToHistoryLimit() {
        while (m_arrText.size() > NON_DEDICATED_HISTORY_LIMIT) {
            removeFirstLine();
            if (firstRetainedTimedLineIndex > 0) {
                firstRetainedTimedLineIndex--;
            }
        }
        if (firstRetainedTimedLineIndex > m_arrText.size()) {
            firstRetainedTimedLineIndex = m_arrText.size();
        }
    }

    /**
     * Java-only non-dedicated counterpart to CGameListControl::AdvanceTimedLines @00402585 that expires the default
     * visible queue without deleting retained history.
     * not ported.
     */
    private void advanceRetainedTimedLines() {
        if (hasActiveTimedLines()) {
            int currentTick = currentTick();
            m_nTopIndex += currentTick - timestamp;
            timestamp = currentTick;
            if (Integer.compareUnsigned(m_arrStates.get(firstRetainedTimedLineIndex), m_nTopIndex) < 0) {
                m_nTopIndex = 0;
                firstRetainedTimedLineIndex++;
                if (firstRetainedTimedLineIndex > m_arrText.size()) {
                    firstRetainedTimedLineIndex = m_arrText.size();
                }
            }
        }
        trimToHistoryLimit();
    }

    /**
     * Java-only mode check for the non-dedicated retained chat history extension.
     * not ported.
     */
    private boolean retainsNonDedicatedHistory() {
        return Globals.mainWindow.sessionMode != CMainWindow.SESSION_MODE_DEDICATED_SERVER;
    }

    /**
     * Java-only active timed-line check for the retained non-dedicated default view.
     * not ported.
     */
    private boolean hasActiveTimedLines() {
        return firstRetainedTimedLineIndex < m_arrText.size();
    }

    /**
     * Native support extracted from CGameListControl::Draw @0040261A line advance.
     */
    public static int linePitch(CBitmapFont font) {
        return font.getHeight() + 2;
    }

    /**
     * Java support for native timeGetTime reads in CGameListControl @004021FE, @004023AA, and @00402585.
     */
    private static int currentTick() {
        return (int) System.currentTimeMillis();
    }
}
