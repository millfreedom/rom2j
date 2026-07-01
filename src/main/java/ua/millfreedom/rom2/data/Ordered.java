package ua.millfreedom.rom2.data;

import java.nio.ByteOrder;

public interface Ordered {
    Ordered setByteOrder(ByteOrder bo);

    ByteOrder getByteOrder();

}
