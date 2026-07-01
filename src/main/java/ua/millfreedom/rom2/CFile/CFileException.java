package ua.millfreedom.rom2.CFile;

/**
 * Native CFileException cause wrapper used by the resource-file port.
 */
public final class CFileException extends RuntimeException {
    /**
     * Native CFileException::m_cause values consumed by CFileException::GetErrorDescription @004E3405.
     */
    public enum Cause {
        NONE(0, "No error occurred."),
        GENERIC(1, "An unspecified error occurred."),
        FILE_NOT_FOUND(2, "The file could not be located."),
        BAD_PATH(3, "All or part of the path is invalid."),
        TOO_MANY_OPEN_FILES(4, "The permitted number of open files was exceeded."),
        ACCESS_DENIED(5, "The file could not be accessed."),
        INVALID_FILE(6, "There was an attempt to use an invalid file handle."),
        REMOVE_CURRENT_DIR(7, "The current working directory cannot be removed."),
        DIRECTORY_FULL(8, "There are no more directory entries."),
        BAD_SEEK(9, "There was an error trying to set the file pointer."),
        HARD_IO(10, "There was a hardware error."),
        SHARING_VIOLATION(11, "SHARE.EXE was not loaded, or a shared region was locked."),
        LOCK_VIOLATION(12, "There was an attempt to lock a region that was already locked."),
        DISK_FULL(13, "The disk is full."),
        END_OF_FILE(14, "The end of file was reached.");

        public final int nativeId;
        public final String description;

        /**
         * Native support extracted from CFileException::GetErrorDescription @004E3405 cause table.
         */
        Cause(int nativeId, String description) {
            this.nativeId = nativeId;
            this.description = description;
        }
    }

    public final Cause causeCode;

    /**
     * Native support extracted from AfxThrowFileException callers and
     * CFileException::GetErrorDescription @004E3405.
     */
    public CFileException(Cause causeCode) {
        super(getErrorDescription(causeCode));
        this.causeCode = causeCode;
    }

    /**
     * Native: CFileException::GetErrorDescription @004E3405.
     * Fully ported.
     */
    public static String getErrorDescription(Cause causeCode) {
        return (causeCode == null ? Cause.GENERIC : causeCode).description;
    }

    /**
     * Native support extracted from CFileException::GetErrorDescription @004E3405 default branch.
     */
    public static Cause causeFromNativeId(int nativeId) {
        for (Cause cause : Cause.values()) {
            if (cause.nativeId == nativeId) {
                return cause;
            }
        }
        return Cause.GENERIC;
    }
}
