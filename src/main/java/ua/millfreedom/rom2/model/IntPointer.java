package ua.millfreedom.rom2.model;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * Native support type for pointer-backed integer fields passed into dialog constructors.
 */
public final class IntPointer {
    private final IntSupplier getter;
    private final IntConsumer setter;

    /**
     * Native support extracted from pointer-backed integer writes such as
     * MpConnectionDialogVisualObject::OnHeaderDialogAction @0044506F.
     */
    public IntPointer(IntSupplier getter, IntConsumer setter) {
        this.getter = getter;
        this.setter = setter;
    }

    /**
     * Native support extracted from pointer-backed integer reads such as
     * MpConnectionDialogVisualObject::createDialogContent @004451FE.
     */
    public int get() {
        return getter.getAsInt();
    }

    /**
     * Native support extracted from pointer-backed integer writes such as
     * MpConnectionDialogVisualObject::OnHeaderDialogAction @0044506F.
     */
    public void set(int value) {
        setter.accept(value);
    }
}
