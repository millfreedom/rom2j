package ua.millfreedom.rom2.model.action;

import java.nio.ByteBuffer;

/**
 * Converts one typed Java value to and from a native-position game-action payload buffer.
 */
public interface Marshaller<T> {
    T get(ByteBuffer buffer, int startPosition, int size);

    void put(ByteBuffer buffer, int startPosition, int size, T value);
}
