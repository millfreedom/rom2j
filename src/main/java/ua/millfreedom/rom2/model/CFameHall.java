package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.CFile.LEReader;
import ua.millfreedom.rom2.CFile.LEWriter;
import ua.millfreedom.rom2.model.gameobj.CUnit;
import ua.millfreedom.rom2.text.GameTexts;
import ua.millfreedom.rom2.text.MainText;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * Native class: CFameHall.
 * Purpose: fame-hall score table plus the paired document/image entries shown by the fame-hall dialogs.
 */
public class CFameHall {
    //0x00
    public int selectedDifficulty;
    //0x04
    public int m_nTotalTime;
    //0x08
    public int m_nDifficultyWeight;
    //0x0c
    public int m_nMaxEntries = 10;
    //0x10
    public final List<HighScoreEntry> m_Entries = new ArrayList<>();
    //0x24
    public final List<ImageContainer> m_Documents = new ArrayList<>();

    /**
     * Native: CFameHall::New @004A8B73.
     * Full port. Java field initializers cover native CArray construction.
     */
    public CFameHall() {
        m_nTotalTime = 0;
        m_nDifficultyWeight = 0;
        m_nMaxEntries = 10;
    }

    /**
     * Native: CFameHall::ZeroFactors @004A8C3F.
     * Full port.
     */
    public void zeroFactors() {
        m_nTotalTime = 0;
        m_nDifficultyWeight = 0;
    }

    /**
     * Native: CFameHall::SetSelectedDifficulty @00493B60.
     * Fully ported.
     */
    public void setSelectedDifficulty(int selectedDifficulty) {
        this.selectedDifficulty = selectedDifficulty;
    }

    /**
     * Native: CFameHall::AddMissionElapsedTime @00493B40.
     * Fully ported.
     */
    public void addMissionElapsedTime(int elapsedTime) {
        m_nTotalTime += elapsedTime;
    }

    /**
     * Native: CFameHall::Read @004A9032.
     * Full port.
     */
    public void read(ByteBuffer buffer) {
        int documentCount = buffer.getInt();
        resizeDocumentsForRead(documentCount);
        for (int index = 0; index < documentCount; index++) {
            ImageContainer document = m_Documents.get(index);
            document.read(buffer);
        }
        m_nTotalTime = buffer.getInt();
        m_nDifficultyWeight = buffer.getInt();
    }

    /**
     * Native: CFameHall::Write @004A8F96.
     * Full port.
     */
    public void write(ByteBuffer buffer) {
        buffer.putInt(m_Documents.size());
        for (ImageContainer document : m_Documents) {
            document.write(buffer);
        }
        buffer.putInt(m_nTotalTime);
        buffer.putInt(m_nDifficultyWeight);
    }

    /**
     * Native: CFameHall::RegisterTextDocument @004A8DF6.
     * Full port.
     */
    public boolean registerTextDocument(int documentId) {
        return registerDocument(documentId, true);
    }

    /**
     * Native: CFameHall::RegisterBitmapDocument @004A8EC6.
     * Full port.
     */
    public boolean registerBitmapDocument(int documentId) {
        return registerDocument(documentId, false);
    }

    /**
     * Native support extracted from CFameHall::RegisterTextDocument @004A8DF6 and
     * CFameHall::RegisterBitmapDocument @004A8EC6.
     */
    private boolean registerDocument(int documentId, boolean textOnly) {
        for (ImageContainer document : m_Documents) {
            if (document.documentId == documentId && document.textOnly == textOnly) {
                return false;
            }
        }

        ImageContainer document = new ImageContainer();
        document.setDocumentDescriptor(documentId, textOnly);
        m_Documents.add(new ImageContainer(document));
        return true;
    }

    /**
     * Native: CFameHall::GetSelectedDifficulty @00493B80.
     * Fully ported.
     */
    public int getSelectedDifficulty() {
        return selectedDifficulty;
    }

    /**
     * Native: CFameHall::Load @004A91D5.
     * Full port.
     */
    public void loadEntries(LEReader reader) throws IOException {
        int entryCount = reader.readI32();
        m_Entries.clear();
        for (int index = 0; index < entryCount; index++) {
            HighScoreEntry entry = new HighScoreEntry();
            int nameByteCount = reader.readI32();
            byte[] nameBytes = new byte[nameByteCount];
            reader.readFully(nameBytes, 0, nameByteCount);
            entry.m_strName = decodeNativeString(nameBytes);
            entry.m_nScore = reader.readI32();
            entry.m_nMission = reader.readI32();
            entry.m_nDifficulty = reader.readI32();
            m_Entries.add(entry);
        }
    }

    /**
     * Native: CFameHall::Save @004A90C9.
     * Full port.
     */
    public void saveEntries(LEWriter writer) throws IOException {
        writer.writeI32(m_Entries.size());
        for (HighScoreEntry entry : m_Entries) {
            byte[] nameBytes = encodeNativeString(entry.m_strName);
            writer.writeI32(nameBytes.length);
            writer.writeBytes(nameBytes);
            writer.writeI32(entry.m_nScore);
            writer.writeI32(entry.m_nMission);
            writer.writeI32(entry.m_nDifficulty);
        }
    }

    /**
     * Native: CFameHall::AddEntry @004A8C5E.
     * Full port.
     */
    public void addEntry(HighScoreEntry entry) {
        int insertIndex = 0;
        while (insertIndex < m_Entries.size() && m_Entries.get(insertIndex).m_nScore > entry.m_nScore) {
            insertIndex++;
        }
        m_Entries.add(insertIndex, copyEntry(entry));
        if (m_Entries.size() > m_nMaxEntries) {
            m_Entries.subList(m_nMaxEntries, m_Entries.size()).clear();
        }
    }

    /**
     * Native: CFameHall::SubmitScore @004A8D28.
     * Full port.
     */
    public void submitScore() {
        HighScoreEntry entry = new HighScoreEntry();
        CUnit selectedUnit = Globals.mainWindow.pMapVisualObject.getSelectedCUnit();
        if (selectedUnit != null) {
            entry.m_strName = selectedUnit.name;
            double experience = selectedUnit.experience;
            if (m_nTotalTime == 0) {
                entry.m_nScore = (int) ((experience / 500000.0) * m_nDifficultyWeight);
            } else {
                entry.m_nScore = (int) ((experience / (m_nTotalTime * 10.0)) * m_nDifficultyWeight);
            }
            addEntry(entry);
        }
    }

    /**
     * Native: CFameHall::GenerateFakeScores @004A938A.
     * Full port.
     */
    public void generateFakeScores() {
        MainText[] names = {
                MainText.DIAMOND_263,
                MainText.GLAZZ_264,
                MainText.GELLA_265,
                MainText.GREATALEX_266,
                MainText.OBERIC_267,
                MainText.RENN_268,
                MainText.VIOLETT_269,
                MainText.REX_270,
                MainText.MONK_271
        };

        int score = 70000;
        HighScoreEntry entry = new HighScoreEntry();
        for (MainText name : names) {
            entry.m_nScore = score + Utils.randInclusive(5000);
            entry.m_strName = GameTexts.get(name);
            addEntry(entry);
            score -= 7000;
        }

        entry.m_nScore = 0;
        entry.m_strName = GameTexts.get(MainText.NICK_272);
        addEntry(entry);
    }

    /**
     * Native support extracted from CString(char*) construction in CFameHall::Load @004A91D5.
     */
    private static String decodeNativeString(byte[] bytes) {
        int length = 0;
        while (length < bytes.length && bytes[length] != 0) {
            length++;
        }
        return new String(bytes, 0, length, Globals.WINDOWS_CYRILLIC_CHARSET);
    }

    /**
     * Native support extracted from CString::GetLength plus null-terminated CFile::Write in CFameHall::Save @004A90C9.
     */
    private static byte[] encodeNativeString(String value) {
        byte[] bytes = value.getBytes(Globals.WINDOWS_CYRILLIC_CHARSET);
        return Arrays.copyOf(bytes, bytes.length + 1);
    }

    /**
     * Native support extracted from the value-copy behavior of CArray<HighScoreEntry>::Add/InsertAt in CFameHall::AddEntry @004A8C5E.
     */
    private static HighScoreEntry copyEntry(HighScoreEntry source) {
        HighScoreEntry copy = new HighScoreEntry();
        copy.copyFrom(source);
        return copy;
    }

    /**
     * Native support extracted from CFameHall::Read @004A9032 and CArray<imageContainer>::SetSize @004A99D0.
     */
    private void resizeDocumentsForRead(int documentCount) {
        while (m_Documents.size() > documentCount) {
            m_Documents.removeLast();
        }
        while (m_Documents.size() < documentCount) {
            m_Documents.add(new ImageContainer());
        }
    }
}
