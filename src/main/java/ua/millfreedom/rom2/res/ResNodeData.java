package ua.millfreedom.rom2.res;

/**
 * union ResNodeData (8 bytes)
 */
public sealed interface ResNodeData
        permits ResValueNode, ResContainerNode, ResHeapNode, ResFileNode {
    int SIZE = 0x08;

    // not ported.
    static int firstWord(ResNodeData d) {
        return switch (d) {
            case ResContainerNode c -> c.firstChildIndex();
            case ResHeapNode h -> h.heapOffset();
            case ResFileNode f -> f.fileOffset();
            case ResByteNode b -> b.value();
            case ResShortNode s -> s.value();
            case ResIntNode i -> i.value();
            case ResDoubleNode dbl -> (int) (Double.doubleToRawLongBits(dbl.value()) & 0xffffffffL);
            case null -> 0;
        };
    }

    // not ported.
    static int secondWord(ResNodeData d) {
        return switch (d) {
            case ResContainerNode c -> c.childCount();
            case ResHeapNode h -> h.byteSize();
            case ResFileNode f -> f.byteSize();
            case ResDoubleNode dbl -> (int) ((Double.doubleToRawLongBits(dbl.value()) >>> 32) & 0xffffffffL);
            case null, default -> 0;
        };
    }

    // not ported.
    static ResNodeData decodeNodeData(int flags, int w0, int w1, boolean isFile) {
        if ((flags & ResNodeFlags.RESFLAG_IS_CONTAINER) != 0) {
            return new ResContainerNode(w0, w1);
        }

        int kind = (flags & ResNodeFlags.RESFLAG_MASK_ANY_OF_BASIC);
        if (kind == ResNodeFlags.RESFLAG_TYPE_INT32) {
            return new ResIntNode(w0);
        }
        if (kind == ResNodeFlags.RESFLAG_TYPE_DOUBLE) {
            long bits = (Integer.toUnsignedLong(w0)) | (Integer.toUnsignedLong(w1) << 32);
            return new ResDoubleNode(Double.longBitsToDouble(bits));
        }

        // For BLOB/arrays/other: keep the raw 2x int payload (matches both HeapNode and FileNode shape)
        return isFile? new ResFileNode(w0, w1): new ResHeapNode(w0, w1);
    }
}
