package ua.millfreedom.rom2.model.compression;

import ua.millfreedom.rom2.data.ReadAdaptor;
import ua.millfreedom.rom2.data.WriteAdaptor;

import java.io.IOException;
import java.util.Arrays;

/**
 * Native byte Huffman packer loaded from the two `Packer*.dat` frequency tables used by CServerApp.
 */
public final class ByteHuffmanPacker {
    private static final int SYMBOL_COUNT = 0x100;

    //0x000
    public final int[] codeBySymbol = new int[SYMBOL_COUNT];
    //0x400
    public final int[] bitLenBySymbol = new int[SYMBOL_COUNT];
    //0x800
    public final int[] freqBySymbol = new int[SYMBOL_COUNT];
    //0xc00
    public ByteHuffmanNode root;

    /**
     * Native: ByteHuffmanPacker::ByteHuffmanPacker @0050B326.
     * Fully ported.
     */
    public ByteHuffmanPacker() {
        root = null;
    }

    /**
     * Native: ByteHuffmanPacker::ClearFrequencies @0050B354.
     * Fully ported.
     */
    public void clearFrequencies() {
        Arrays.fill(freqBySymbol, 0);
    }

    /**
     * Native: ByteHuffmanPacker::CountFrequencies @0050B377.
     * Fully ported.
     */
    public void countFrequencies(byte[] source, int length) {
        int sourceIndex = 0;
        for (int i = 0; i < length; i++) {
            int symbol = Byte.toUnsignedInt(source[sourceIndex]);
            freqBySymbol[symbol] = freqBySymbol[symbol] + 1;
            sourceIndex++;
        }
    }

    /**
     * Native: ByteHuffmanPacker::WriteFrequencies @0050B3D6.
     * Fully ported.
     */
    public void writeFrequencies(WriteAdaptor target) throws IOException {
        for (int frequency : freqBySymbol) {
            target.writeInt(frequency);
        }
    }

    /**
     * Native: ByteHuffmanPacker::ReadFrequencies @0050B3FC.
     * Fully ported.
     */
    public void readFrequencies(ReadAdaptor source) throws IOException {
        for (int i = 0; i < SYMBOL_COUNT; i++) {
            freqBySymbol[i] = source.readInt();
        }
        buildTree();
        buildCodeTables();
    }

    /**
     * Native: ByteHuffmanPacker::BuildTree @0050B473.
     * Fully ported.
     */
    public void buildTree() {
        WeightedByteSymbol[] weightedSymbols = new WeightedByteSymbol[SYMBOL_COUNT];
        for (int i = 0; i < SYMBOL_COUNT; i++) {
            WeightedByteSymbol weightedSymbol = new WeightedByteSymbol();
            weightedSymbol.value = i;
            weightedSymbol.weight = freqBySymbol[i];
            weightedSymbols[i] = weightedSymbol;
        }

        Arrays.sort(weightedSymbols);
        root = new ByteHuffmanNode();
        root.weight = weightedSymbols[0].weight + 1;
        root.value = weightedSymbols[0].value;

        for (int i = 1; i < SYMBOL_COUNT; i++) {
            insertBalanced(root, weightedSymbols[i].value, weightedSymbols[i].weight + 1);
        }
    }

    /**
     * Native: ByteHuffmanPacker::BuildCodeTables @0050B77D.
     * Fully ported.
     */
    public void buildCodeTables() {
        Arrays.fill(bitLenBySymbol, 0);
        Arrays.fill(codeBySymbol, 0);
        buildCodeTablesRec(root, 0, 0);
    }

    /**
     * Native: ByteHuffmanPacker::Pack @0050B8DC.
     * Fully ported.
     */
    public int pack(byte[] source, int length, int[] targetWords) {
        int sourceIndex = 0;
        int wordIndex = 0;
        int bitOffset = 0;
        int bitCount = 0;
        targetWords[0] = 0;

        for (; length != 0; length--) {
            int symbol = Byte.toUnsignedInt(source[sourceIndex]);
            int code = codeBySymbol[symbol];
            int symbolBits = bitLenBySymbol[symbol];
            bitCount += symbolBits;
            targetWords[wordIndex] = targetWords[wordIndex] | (code << (bitOffset & 0x1F));
            bitOffset += symbolBits;
            if (bitOffset > 0x1F) {
                bitOffset -= 0x20;
                wordIndex++;
                targetWords[wordIndex] = code >>> (((0x20 - bitOffset) + symbolBits) & 0x1F);
            }
            sourceIndex++;
        }
        return bitCount;
    }

    /**
     * Native: ByteHuffmanPacker::Unpack @0050B9A7.
     * Fully ported.
     */
    public int unpack(int[] sourceWords, int bitCount, byte[] target, int targetLimit) {
        int wordIndex = 0;
        int writeIndex = 0;
        int decodedCount = 0;
        int remainingWordBits = 0;
        int currentWord = 0;

        while (bitCount > 0) {
            ByteHuffmanNode node = root;
            while (node.left != null) {
                if (remainingWordBits == 0) {
                    remainingWordBits = 0x20;
                    currentWord = sourceWords[wordIndex];
                    wordIndex++;
                }
                if ((currentWord & 1) == 0) {
                    node = node.right;
                } else {
                    node = node.left;
                }
                currentWord = currentWord >>> 1;
                remainingWordBits--;
                bitCount--;
            }
            if (targetLimit != 0) {
                target[writeIndex] = (byte) node.value;
                targetLimit--;
                writeIndex++;
            }
            decodedCount++;
        }
        return decodedCount;
    }

    /**
     * Native: Global::PackedByteLength @0050B8C3.
     * Fully ported.
     */
    public static int packedByteLength(int bitCount) {
        return (bitCount >> 3) + ((bitCount & 7) != 0 ? 1 : 0);
    }

    /**
     * Native: ByteHuffmanPacker::InsertBalanced @0050B616.
     * Fully ported.
     */
    private static void insertBalanced(ByteHuffmanNode node, int value, int weight) {
        if (node.left == null) {
            node.left = new ByteHuffmanNode();
            node.left.value = node.value;
            node.left.weight = node.weight;
            node.leftWeight = node.weight;

            node.right = new ByteHuffmanNode();
            node.right.value = value;
            node.right.weight = weight;
            node.rightWeight = weight;
        } else if (node.rightWeight < node.leftWeight) {
            insertBalanced(node.right, value, weight);
            node.rightWeight = node.rightWeight + weight;
        } else {
            insertBalanced(node.left, value, weight);
            node.leftWeight = node.leftWeight + weight;
        }
    }

    /**
     * Native: ByteHuffmanPacker::BuildCodeTablesRec @0050B7C9.
     * Fully ported.
     */
    private void buildCodeTablesRec(ByteHuffmanNode node, int accum, int bitCount) {
        if (node.left == null && node.right == null) {
            codeBySymbol[node.value] = accum >>> ((0x20 - bitCount) & 0x1F);
            bitLenBySymbol[node.value] = bitCount;
        } else {
            buildCodeTablesRec(node.left, (accum >>> 1) | 0x80000000, bitCount + 1);
            buildCodeTablesRec(node.right, accum >>> 1, bitCount + 1);
        }
    }

}

final class WeightedByteSymbol implements Comparable<WeightedByteSymbol> {
    //0x0
    int value;
    //0x4
    int weight;

    /**
     * Native support extracted from ByteHuffmanPacker::BuildTree @0050B473 and
     * Global::CompareWeightedByteSymbol @0050B444.
     * Fully ported support helper.
     */
    @Override
    public int compareTo(WeightedByteSymbol other) {
        return Integer.compare(other.weight, weight);
    }
}
