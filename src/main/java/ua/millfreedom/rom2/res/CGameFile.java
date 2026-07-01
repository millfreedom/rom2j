package ua.millfreedom.rom2.res;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.CFile.CFileException;
import ua.millfreedom.rom2.CFile.CFile.SeekType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static ua.millfreedom.rom2.CFile.CFileException.Cause.ACCESS_DENIED;
import static ua.millfreedom.rom2.CFile.CFileException.Cause.BAD_SEEK;
import static ua.millfreedom.rom2.CFile.CFileException.Cause.END_OF_FILE;
import static ua.millfreedom.rom2.CFile.CFileException.Cause.INVALID_FILE;

/**
 * Native CGameFile resource-file adapter.
 */
public final class CGameFile implements AutoCloseable {
    public static final int MODE_READ = 0x0000;
    public static final int MODE_WRITE = 0x0001;

    private static final int INVALID_FILE_HANDLE = -1;

    //0x04
    public int m_hFile = INVALID_FILE_HANDLE;
    //0x0C
    public String m_strFileName = "";
    //0x10
    public ByteBuffer m_file;
    //0x14
    public int m_buffer;
    //0x18
    public int m_nLength;
    //0x1C
    public int m_nPosition;

    /**
     * Native: CGameFile::CGameFile @004E2EDA.
     * Fully ported.
     */
    public CGameFile() {
        m_file = null;
    }

    /**
     * Native: CGameFile::CGameFile(char*, UINT) @004E2F03.
     * Fully ported for native read-mode resource streams.
     */
    public CGameFile(String file) {
        this(file, MODE_READ);
    }

    /**
     * Native: CGameFile::CGameFile(char*, UINT) @004E2F03.
     * Fully ported for native read-mode resource streams.
     */
    public CGameFile(String file, int flags) {
        if (!open(file, flags)) {
            throw accessDenied("CGameFile::CGameFile failed to open: " + file);
        }
    }

    /**
     * Native support extracted from CGameFile::Open @004E2FC3 for Java callers that need a direct resource buffer.
     */
    public static ByteBuffer getDataFor(String file) {
        return new CGameFile(file).getData();
    }

    /**
     * Native support extracted from CGameFile::Read @004E329C for Java callers that operate on direct buffers.
     */
    public ByteBuffer getData() {
        ensureOpen();
        ByteBuffer duplicate = m_file.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        duplicate.position(m_buffer);
        duplicate.limit(m_buffer + m_nLength);
        return duplicate.slice().order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Native: CGameFile::Open @004E2FC3.
     * Fully ported for native read-mode resource streams.
     */
    public boolean open(String name, int flags) {
        String openedName = copyLowerIfEnabled(name);
        m_strFileName = openedName;
        m_hFile = INVALID_FILE_HANDLE;
        if ((flags & MODE_WRITE) != 0) {
            return false;
        }

        ByteBuffer opened = Globals.gameFileManager.openGameFileData(openedName);
        if (opened == null) {
            return false;
        }
        m_file = opened.slice().order(ByteOrder.LITTLE_ENDIAN);
        m_buffer = 0;
        m_nLength = m_file.remaining();
        m_nPosition = 0;
        m_hFile = 0;
        return true;
    }

    /**
     * Native: CGameFile::SeekToBegin @004E3187.
     * Fully ported.
     */
    public int seekToBegin() {
        return seek(0, SeekType.BEGIN);
    }

    /**
     * Native: CGameFile::SeekToEnd @004E31A1.
     * Fully ported.
     */
    public int seekToEnd() {
        return seek(0, SeekType.END);
    }

    /**
     * Native: CGameFile::Seek @004E31BB.
     * Fully ported.
     */
    public int seek(int offset, SeekType flag) {
        ensureOpen();
        if (flag == SeekType.BEGIN) {
            if (m_nLength < offset) {
                throw badSeek(offset);
            }
            m_nPosition = offset;
        } else if (flag == SeekType.CURRENT) {
            if (m_nLength < offset + m_nPosition) {
                throw badSeek(offset);
            }
            m_nPosition += offset;
        } else if (flag == SeekType.END) {
            if (-m_nLength != offset && m_nLength <= -offset) {
                throw badSeek(offset);
            }
            m_nPosition = m_nLength + offset;
        }
        return m_nPosition;
    }

    /**
     * Native: CGameFile::SetLength @004E3273.
     * Fully ported read-only failure path.
     */
    public void setLength(int length) {
        throw accessDenied("CGameFile::SetLength is not allowed for read-only resource streams");
    }

    /**
     * Native: CGameFile::GetLength @004E328B.
     * Fully ported.
     */
    public int getLength() {
        return m_nLength;
    }

    /**
     * Native: CGameFile::Read @004E329C.
     * Fully ported.
     */
    public int read(ByteBuffer destination, int count) {
        ensureOpen();
        int readAmount = Math.min(count, m_nLength - m_nPosition);
        if (readAmount == 0) {
            return 0;
        }
        ByteBuffer source = m_file.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        source.position(m_buffer + m_nPosition);
        source.limit(m_buffer + m_nPosition + readAmount);
        m_nPosition += readAmount;
        if (m_nLength < m_nPosition) {
            throw endOfFile();
        }
        destination.put(source);
        return readAmount;
    }

    /**
     * Native support extracted from CGameFile::Read @004E329C.
     */
    public ByteBuffer read(int count) {
        ByteBuffer result = ByteBuffer.allocate(Math.min(count, m_nLength - m_nPosition))
                .order(ByteOrder.LITTLE_ENDIAN);
        read(result, count);
        return result.flip().order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Native: CGameFile::Write @004E3358.
     * Fully ported read-only failure path.
     */
    public void write(ByteBuffer source, int count) {
        throw accessDenied("CGameFile::Write is not allowed for read-only resource streams");
    }

    /**
     * Native: CGameFile::LockRange @004E3370.
     * Fully ported read-only failure path.
     */
    public void lockRange(int position, int count) {
        throw accessDenied("CGameFile::LockRange is not allowed for read-only resource streams");
    }

    /**
     * Native: CGameFile::UnlockRange @004E3388.
     * Fully ported read-only failure path.
     */
    public void unlockRange(int position, int count) {
        throw accessDenied("CGameFile::UnlockRange is not allowed for read-only resource streams");
    }

    /**
     * Native: CGameFile::Abort @004E33A0.
     * Fully ported no-op.
     */
    public void abort() {
    }

    /**
     * Native: CGameFile::Flush @004E33AB.
     * Fully ported no-op.
     */
    public void flush() {
    }

    /**
     * Native: CGameFile::Close @004E33B6.
     * Fully ported for Java-managed resource buffers.
     */
    @Override
    public void close() {
        m_file = null;
    }

    /**
     * Native: CGameFile::Duplicate @004E33DC.
     * Fully ported read-only failure path.
     */
    public CGameFile duplicate() {
        throw accessDenied("CGameFile::Duplicate is not allowed for read-only resource streams");
    }

    /**
     * Native: CGameFile::GetPosition @004E33F4.
     * Fully ported.
     */
    public int getPosition() {
        return m_nPosition;
    }

    /**
     * Native support extracted from CopyLowerIfEnabled @004E26C9 as used by CGameFile::Open @004E2FC3.
     */
    private static String copyLowerIfEnabled(String value) {
        return Globals.gameFileManager.copyLowerIfEnabled(value);
    }

    /**
     * Native support extracted from CGameFile file-handle checks in CGameFile::Seek @004E31BB and
     * CGameFile::Read @004E329C.
     */
    private void ensureOpen() {
        if (m_hFile == INVALID_FILE_HANDLE) {
            throw new CFileException(INVALID_FILE);
        }
    }

    /**
     * Native support extracted from CGameFile::Seek @004E31BB badSeek branches.
     */
    private static CFileException badSeek(int offset) {
        return new CFileException(BAD_SEEK);
    }

    /**
     * Native support extracted from CGameFile::SetLength @004E3273, CGameFile::Write @004E3358,
     * CGameFile::LockRange @004E3370, CGameFile::UnlockRange @004E3388, and
     * CGameFile::Duplicate @004E33DC read-only AfxThrowFileException(accessDenied) branches.
     */
    private static CFileException accessDenied(String message) {
        return new CFileException(ACCESS_DENIED);
    }

    /**
     * Native support extracted from CGameFile::Read @004E329C endOfFile guard.
     */
    private static CFileException endOfFile() {
        return new CFileException(END_OF_FILE);
    }
}
