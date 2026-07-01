package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;

import java.io.IOException;

public class MaterialAttributes implements MfcSerializable {
    /**
     * Native double slot for the source "Abbreviation" resource column. Material::Init parses it with atof,
     * so nonnumeric abbreviation tokens become 0.0 and are kept only for the serialized nine-double layout.
     */
    //0x0
    public double abbreviationColumnNumericValue;
    /**
     * Native double slot for the source "Materials" resource column. Material::Init parses it with atof,
     * so nonnumeric material tokens become 0.0 and are kept only for the serialized nine-double layout.
     */
    //0x8
    public double materialsColumnNumericValue;
    //0x10
    public double price;
    //0x18
    public double weight;
    //0x20
    public double damage;
    //0x28
    public double toHit;
    //0x30
    public double defence;
    //0x38
    public double absorption;
    //0x40
    public double magicVolume;

    /**
     * Native support extracted from Material::Init @0053E3D0.
     * Fully ported.
     */
    public void loadFromNativeParsedDoubles(double[] parsedDoubles) {
        abbreviationColumnNumericValue = parsedDoubles[0];
        materialsColumnNumericValue = parsedDoubles[1];
        price = parsedDoubles[2];
        weight = parsedDoubles[3];
        damage = parsedDoubles[4];
        toHit = parsedDoubles[5];
        defence = parsedDoubles[6];
        absorption = parsedDoubles[7];
        magicVolume = parsedDoubles[8];
    }

    /**
     * Native support extracted from Material::Serialize @004FE455.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        if (ar.isStoring()) {
            ar.writeDouble(abbreviationColumnNumericValue);
            ar.writeDouble(materialsColumnNumericValue);
            ar.writeDouble(price);
            ar.writeDouble(weight);
            ar.writeDouble(damage);
            ar.writeDouble(toHit);
            ar.writeDouble(defence);
            ar.writeDouble(absorption);
            ar.writeDouble(magicVolume);
        } else {
            abbreviationColumnNumericValue = ar.readDouble();
            materialsColumnNumericValue = ar.readDouble();
            price = ar.readDouble();
            weight = ar.readDouble();
            damage = ar.readDouble();
            toHit = ar.readDouble();
            defence = ar.readDouble();
            absorption = ar.readDouble();
            magicVolume = ar.readDouble();
        }
    }

    /**
     * not ported.
     */
    @Override
    public String toString() {
        return "MaterialAttributes{" +
                //"abbreviationColumnNumericValue=" + abbreviationColumnNumericValue + // layout-only, usually 0.0
                //", materialsColumnNumericValue=" + materialsColumnNumericValue + // layout-only, usually 0.0
                "price=" + price +
                ", weight=" + weight +
                ", damage=" + damage +
                ", toHit=" + toHit +
                ", defence=" + defence +
                ", absorption=" + absorption +
                ", magicVolume=" + magicVolume +
                '}';
    }
}
