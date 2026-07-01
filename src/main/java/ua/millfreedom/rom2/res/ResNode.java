package ua.millfreedom.rom2.res;

import ua.millfreedom.rom2.CString;

import java.util.Objects;

import static ua.millfreedom.rom2.res.Utils.encodeName16;

public record ResNode(
        //0x00
        ResAtom atom,
        //0x10
        CString name16
) implements Comparable<ResNode> {
    public static final int SIZE = 0x20;

    /**
     * not ported.
     */
    public ResNode {
        Objects.requireNonNull(atom);
        Objects.requireNonNull(name16);
        if (name16.length() != 16) {
            throw new IllegalArgumentException("name16 must be 16 bytes");
        }
    }

    /**
     * not ported.
     */
    public ResNode(ResAtom atom, byte[] name16) {
        this(atom, new CString(name16));
    }

    /**
     * not ported.
     */
    public ResNode(ResAtom atom, String name) {
        this(atom, encodeName16(Objects.requireNonNull(name, "name")));
    }

    /**
     * Native support extracted from Res::FindChildByName @004E918E temporary lookup node construction.
     */
    public ResNode(String name) {
        this(new ResAtom(), name);
    }

    /**
     * Native support extracted from Res::FindChildByName @004E918E and ResNode::CompareByName @004E941A name reads.
     */
    public String getName() {
        return name16.toString();
    }

    @Override
    /**
     * not ported.
     */
    public String toString() {
        return "ResNode{" +
                "atom=" + atom +
                ", name=\"" + getName() + "\"}";
    }

    /**
     * Native support extracted from ResNode::SetName @004E3620.
     * Fully ported through immutable ResNode replacement.
     */
    public ResNode withName(String newName) {
        Objects.requireNonNull(newName, "newName");
        int strLen = newName.length();
        int flags = atom.flags();
        if (strLen > 15) {
            flags |= ResNodeFlags.RESFLAG_NAME_TRUNCATED;
        }

        return new ResNode(new ResAtom(atom.magic(), atom.data(), flags), encodeName16(newName));
    }

    /**
     * Native: ResNode::ConstructPath @004E6920.
     * Fully ported.
     */
    public String constructPath(String groupName, String keyName) {
        return getName()
                + "\\"
                + truncateNativeNameSegment(groupName)
                + "\\"
                + truncateNativeNameSegment(keyName);
    }

    /**
     * Native support extracted from ResNode::ConstructPath @004E6920 strncat(..., 0xF) calls.
     */
    private static String truncateNativeNameSegment(String value) {
        return value.length() <= 0xF ? value : value.substring(0, 0xF);
    }

    /**
     * Native: ResNode::CompareByName @004E941A.
     * Fully ported. INTENTIONALLY IGNORING CASE!!!
     */
    public static int compareByName(ResNode left, ResNode right) {
        return left.getName().compareToIgnoreCase(right.getName());
    }

    @Override
    /**
     * Native support extracted from ResNode::CompareByName @004E941A.
     */
    public int compareTo(ResNode other) {
        return compareByName(this, other);
    }
}
