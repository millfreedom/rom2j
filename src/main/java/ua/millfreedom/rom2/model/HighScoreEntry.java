package ua.millfreedom.rom2.model;

public class HighScoreEntry {
    //0x00
    public String m_strName = "";
    //0x04
    public int m_nScore;
    //0x08
    public int m_nMission;
    //0x0c
    public int m_nDifficulty;

    /**
     * Native: HighScoreEntry::HighScoreEntry @004AA070.
     * Full port. Java field initializers cover native CString construction.
     */
    public HighScoreEntry() {
        m_strName = "";
        m_nScore = 0;
        m_nMission = 0;
        m_nDifficulty = 0;
    }

    /**
     * Native: HighScoreEntry::CopyFrom @004AA0D0.
     * Full port.
     */
    public void copyFrom(HighScoreEntry source) {
        m_strName = source.m_strName;
        m_nScore = source.m_nScore;
        m_nMission = source.m_nMission;
        m_nDifficulty = source.m_nDifficulty;
    }
}
