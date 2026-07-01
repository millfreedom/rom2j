package ua.millfreedom.rom2.CArchive;

import java.io.IOException;

/**
 * This is the replacement for inheritance of a native CObject class.
 * in a native, every CObject inheritor have a Serialize() virtual method, which is the only really matters in Java port.
 * Therefore: EVERY CObject inheritor (classes that are calling CObject::New @00401900 in their constructors)
 * in java MUST inplement MfcSerializable interface.
 */
public interface MfcSerializable {
    void serialize(CArchive ar) throws IOException;

    /**
     * Native support extracted from CRuntimeClass::Store @005AA6C9 runtime class identity.
     * Defaults to the Java class; native-shared runtime classes can override this.
     */
    default Class<?> mfcRuntimeClass() {
        return getClass();
    }

    /**
     * Controls how this object is serialized when it is an element of a serialized list.
     * `false` -> direct payload (`obj.serialize(ar)`).
     * `true`  -> MFC object framing (`ar.readObject` / `ar.writeObject`).
     */
    default boolean isDirect() {
        return false;
    }
}
