package ua.millfreedom.rom2.res;

import lombok.SneakyThrows;

import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.String.join;
import static ua.millfreedom.rom2.Globals.gameFileManager;

public class Resources {
    public static String locale = "en";

    // not ported.
    public static String path(String... pth) {
        return join("/", pth);
    }

    // not ported.
    public static ByteBuffer open(String... resourcePath) {
        return open(path(resourcePath));
    }

    // not ported.
    public static Path getPath(String resPath) {
        return Paths.get(resPath);
    }

    // not ported.
    @SneakyThrows
    public static ByteBuffer open(String resourcePath) {
        return gameFileManager.get(resourcePath);
    }

}
