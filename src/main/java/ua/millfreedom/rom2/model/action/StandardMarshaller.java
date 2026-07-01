package ua.millfreedom.rom2.model.action;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Built-in value conversions for native-position game-action payload properties.
 */
enum StandardMarshaller implements Marshaller<Object> {
    UNSIGNED_BYTE {
        @Override
        public Object get(ByteBuffer buffer, int startPosition, int size) {
            return Byte.toUnsignedInt(buffer.get(startPosition));
        }

        @Override
        public void put(ByteBuffer buffer, int startPosition, int size, Object value) {
            buffer.put(startPosition, (byte) ((Number) value).intValue());
        }
    },
    UNSIGNED_WORD_LE {
        @Override
        public Object get(ByteBuffer buffer, int startPosition, int size) {
            return Short.toUnsignedInt(buffer.getShort(startPosition));
        }

        @Override
        public void put(ByteBuffer buffer, int startPosition, int size, Object value) {
            buffer.putShort(startPosition, (short) ((Number) value).intValue());
        }
    },
    INT_LE {
        @Override
        public Object get(ByteBuffer buffer, int startPosition, int size) {
            return buffer.getInt(startPosition);
        }

        @Override
        public void put(ByteBuffer buffer, int startPosition, int size, Object value) {
            buffer.putInt(startPosition, ((Number) value).intValue());
        }
    },
    BYTE_ARRAY {
        @Override
        public Object get(ByteBuffer buffer, int startPosition, int size) {
            return readBytes(buffer, startPosition, size);
        }

        @Override
        public void put(ByteBuffer buffer, int startPosition, int size, Object value) {
            byte[] bytes = value == null ? new byte[0] : (byte[]) value;
            writeBytes(buffer, startPosition, size, bytes);
        }
    },
    FIXED_C_STRING_ISO_8859_1 {
        @Override
        public Object get(ByteBuffer buffer, int startPosition, int size) {
            byte[] raw = readBytes(buffer, startPosition, size);
            int length = 0;
            while (length < raw.length && raw[length] != 0) {
                length++;
            }
            return new String(raw, 0, length, StandardCharsets.ISO_8859_1);
        }

        @Override
        public void put(ByteBuffer buffer, int startPosition, int size, Object value) {
            byte[] raw = new byte[size];
            if (value != null) {
                byte[] encoded = value.toString().getBytes(StandardCharsets.ISO_8859_1);
                System.arraycopy(encoded, 0, raw, 0, Math.clamp(size - 1, 0, encoded.length));
            }
            writeBytes(buffer, startPosition, size, raw);
        }
    };

    private static byte[] readBytes(ByteBuffer buffer, int startPosition, int size) {
        byte[] bytes = new byte[size];
        ByteBuffer duplicate = buffer.duplicate();
        duplicate.position(startPosition);
        duplicate.get(bytes);
        return bytes;
    }

    private static void writeBytes(ByteBuffer buffer, int startPosition, int size, byte[] source) {
        byte[] bytes = Arrays.copyOf(source, size);
        ByteBuffer duplicate = buffer.duplicate();
        duplicate.position(startPosition);
        duplicate.put(bytes);
    }
}
