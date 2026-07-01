package ua.millfreedom.rom2.model.compression;

public final class ByteHuffmanNode {
    //0x0
    ByteHuffmanNode left;
    //0x4
    ByteHuffmanNode right;
    //0x8
    int leftWeight;
    //0xc
    int rightWeight;
    //0x10
    int value;
    //0x14
    int weight;

    /**
     * Native: ByteHuffmanNode::ByteHuffmanNode @00540F30.
     * Fully ported.
     */
    ByteHuffmanNode() {
        right = null;
        left = null;
    }
}
