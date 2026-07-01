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
            m_nVisibleLines = (m_Rect.height() / (Globals.fonts.font1.getHeight() + 2)) / 2;
        }
    }

    /**
     * Native: CGameListControl::AddTimedLine @004021FE.
     * Fully ported.
     */
    public int addTimedLine(String text, Palette16 palette, int lifetimeMs) {
        m_nState = 1;
        CBitmapFont font = Globals.fonts.font1;
        if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_DEDICATED_SERVER) {
            font = Globals.fonts.font2;
        }
        List<String> lines = font.formatText(m_Rect, text);
        boolean wasEmpty = m_arrText.isEmpty();
        appendFormattedLines(lines, palette, lifetimeMs, false);
        if (wasEmpty) {
            timestamp = currentTick();
            m_nTopIndex = 0;
        }
        trimToVisibleLines();
        return m_arrText.size();
    }

    /**
     * Native: CGameListControl::AddUniqueTimedLine @004023AA.
     * Fully ported.
     */
    public void addUniqueTimedLine(String text, Palette16 palette, int lifetimeMs) {
        m_nState = 1;
        List<String> lines = Globals.fonts.font1.formatText(m_Rect, text);
        boolean completed = appendFormattedLines(lines, palette, lifetimeMs, true);
        if (!completed) {
            return;
        }
        if (m_arrText.size() == 1) {
            timestamp = currentTick();
            m_nTopIndex = 0;
        }
        trimToVisibleLines();
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
     * Fully ported.
     */
    public void advanceTimedLines() {
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
     * Fully ported.
     */
    public void draw() {
        CMainWindow mainWindow = Globals.mainWindow;
        int x = 8;
        int y = 8;
        CBitmapFont font = Globals.fonts.font1;
        if (mainWindow.sessionMode == CMainWindow.SESSION_MODE_DEDICATED_SERVER) {
            x = 0;
            y = 0xDC;
            font = Globals.fonts.font2;
        }

        int lineOffset = 0;
        for (int lineIndex = 0; lineIndex < m_arrText.size(); lineIndex++) {
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

                lineOffset += font.getHeight() + 2;
                if (carriageReturn < 0) {
                    break;
                }
                remaining = remaining.substring(carriageReturn + 1);
            }
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
                    && !m_arrText.isEmpty()
                    && m_arrText.getLast().equals(line)) {
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
     * Native support extracted from CGameListControl::AddTimedLine @004021FE and
     * CGameListControl::AddUniqueTimedLine @004023AA.
     */
    private void trimToVisibleLines() {
        while (m_arrText.size() > m_nVisibleLines) {
            removeFirstLine();
        }
    }

    /**
     * Java support for native timeGetTime reads in CGameListControl @004021FE, @004023AA, and @00402585.
     */
    private static int currentTick() {
        return (int) System.currentTimeMillis();
    }
}
