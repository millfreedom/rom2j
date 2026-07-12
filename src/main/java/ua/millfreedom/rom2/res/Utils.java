package ua.millfreedom.rom2.res;

import ua.millfreedom.rom2.GameCharsets;

public class Utils {
    // not ported.
    public static String extractRootName(String lFilename) {
        int dot = lFilename.indexOf('.');
        String noExt = (dot >= 0) ? lFilename.substring(0, dot) : lFilename;

        int bs = noExt.lastIndexOf('\\');
        int fs = noExt.lastIndexOf('/');
        int sep = Math.max(bs, fs);

        return (sep >= 0) ? noExt.substring(sep + 1) : noExt;
    }

    // not ported.
    public static byte[] encodeName16(String s) {
        byte[] out = new byte[16];
        if (s != null) {
            byte[] src = s.getBytes(GameCharsets.GAME_TEXT);
            System.arraycopy(src, 0, out, 0, Math.min(src.length, 15));
        }
        return out;
    }

    // not ported.
    public static String decodeName16(byte[] name16) {
        int n = 0;
        int max = Math.min(16, name16.length);
        while (n < max && name16[n] != 0) n++;
        return new String(name16, 0, n, GameCharsets.GAME_TEXT);
    }

/*    public static int readI32LE(SeekableByteChannel ch) throws IOException {
        ByteBuffer b = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        readFully(ch, b);
        return b.getInt(0);
    }*/

/*    public static void readFully(SeekableByteChannel ch, ByteBuffer buf) throws IOException {
        while (buf.hasRemaining()) {
            int n = ch.read(buf);
            if (n < 0) throw new EOFException("Unexpected EOF");
        }
        buf.flip();
    }*/

}
