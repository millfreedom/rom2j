package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;

import java.io.IOException;

public class Material extends TableLine implements MfcSerializable {

    //0x20
    public MaterialAttributes attributes = new MaterialAttributes();

    /**
     * Native: Material::New @00539620. Fully ported.
     */
    public Material() {
    }

    /**
     * Native: Material::Init @0053E3D0.
     * Fully ported.
     */
    public void init(String text) {
        // MaterialColumnCount @005F81A0.
        double[] parsedDoubles = new double[0x9];
        TokenizeLine(text, 0x9, parsedDoubles);
        attributes.loadFromNativeParsedDoubles(parsedDoubles);
    }

    /**
     * Native: Material::Serialize @004FE455.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        if (ar.isStoring()) {
            ar.writeCString(name);
        } else {
            name = ar.readCString();
        }
        ar.serialize(attributes);
    }


    @Override
    // not ported.
    public String toString() {
        return
                super.toString()
                + ", " + attributes
                ;
    }
}
