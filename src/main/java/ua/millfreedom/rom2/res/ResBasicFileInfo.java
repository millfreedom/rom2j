package ua.millfreedom.rom2.res;

/**
 * Native support type: ResBasicFile::info as populated by ResInFile::Read @004E2040 and
 * ResInFile::GetFileContainer @004E2445.
 */
public record ResBasicFileInfo(
        //0x00
        int offset,
        //0x04
        short modeOrFlag,
        //0x06
        short pad
) {
    public static final int SIZE = 0x08;
}
