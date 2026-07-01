package ua.millfreedom.rom2.res;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.util.Objects;

public  final class ResBasicFile implements AutoCloseable {
    public final Path path;
    public final SeekableByteChannel ch;
    public final ResBasicFileInfo info;

    // not ported.
    public ResBasicFile(Path path, SeekableByteChannel ch, ResBasicFileInfo info) {
        this.path = Objects.requireNonNull(path);
        this.ch = Objects.requireNonNull(ch);
        this.info = Objects.requireNonNull(info);
    }

    @Override
    // not ported.
    public void close() throws IOException {
        ch.close();
    }

    @Override
    // not ported.
    public String toString() {
        return "\nResBasicFile{" +
                "path=" + path +
                ", ch=" + ch +
                ", info=" + info +
                '}';
    }
}
