package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.world.CWorldMap;

import java.io.IOException;

/**
 * TargetHandle layout:
 * +0x00 byte x
 * +0x01 byte y
 * +0x02 ushort cell
 * +0x04 byte dx
 * +0x05 byte dy
 * +0x06 ushort serializedPadding
 * +0x08 CWorldMap* mCWorldMap
 */
public final class TargetHandle implements MfcSerializable {
    //0x00
    public int x;
    //0x01
    public int y;
    //0x02
    public int cell;
    //0x04
    public int dx;
    //0x05
    public int dy;
    //0x06
    public int serializedPadding;
    //0x08
    public Object mCWorldMap;

    // not ported.
    public TargetHandle() {
    }

    /**
     * Native: TargetHandle::TargetHandle @0054F971.
     * Fully ported.
     */
    public TargetHandle(int xByte, int yByte, CWorldMap context) {
        initFromBytes(xByte, yByte, context);
    }

    // not ported.
    public void clear() {
        x = 0;
        y = 0;
        cell = 0;
        dx = 0;
        dy = 0;
        serializedPadding = 0;
        mCWorldMap = null;
    }

    /**
     * Native: TargetHandle::initDefault @0054F955.
     * Fully ported.
     */
    public void initDefault() {
        initFromBytes(0, 0, null);
    }

    /**
     * Native: TargetHandle::initFromBytes @0054F9B9.
     * Fully ported.
     */
    public void initFromBytes(int xByte, int yByte, CWorldMap context) {
        x = xByte & 0xFF;
        y = yByte & 0xFF;
        cell = ((y & 0xFF) << 8) | (x & 0xFF);
        dx = 0x80;
        dy = 0x80;
        mCWorldMap = context;
    }

    /**
     * Native: TargetHandle::initFromPackedCellWord @0054FA22.
     * Fully ported.
     */
    public void initFromPackedCellWord(int packedCell, CWorldMap context) {
        initFromPackedCell(packedCell & 0xFFFF, context);
    }

    /**
     * Native: TargetHandle::initFromPackedCell @0054FA43.
     * Fully ported.
     */
    public void initFromPackedCell(int packedCell, CWorldMap context) {
        initFromBytes(packedCell & 0xFF, (packedCell >>> 8) & 0xFF, context);
    }

    /**
     * Native: TargetHandle::setPos @0054FC41.
     * Fully ported.
     */
    public void setPos(int packedXdX, int packedYdY) {
        x = (packedXdX >>> 8) & 0xFF;
        y = (packedYdY >>> 8) & 0xFF;
        dx = packedXdX & 0xFF;
        dy = packedYdY & 0xFF;
        cell = ((y & 0xFF) << 8) | (x & 0xFF);
    }

    /**
     * Native: TargetHandle::setCellAndResetSubPos @0054FBF7.
     * Fully ported.
     */
    public void setCellAndResetSubPos(int xByte, int yByte) {
        x = xByte & 0xFF;
        y = yByte & 0xFF;
        cell = ((y & 0xFF) << 8) | (x & 0xFF);
        dx = 0x80;
        dy = 0x80;
    }

    /**
     * Native: TargetHandle::setPosition @0054FCA8.
     * Fully ported.
     */
    public boolean setPosition(int xByte, int yByte) {
        int newX = xByte & 0xFF;
        int newY = yByte & 0xFF;
        boolean insideBounds = true;

        if (newX < 8) {
            newX = 8;
            insideBounds = false;
        }
        CWorldMap worldMap = (CWorldMap) mCWorldMap;
        if (newX > worldMap.getMapWidth() - 9) {
            newX = (worldMap.getMapWidth() - 9) & 0xFF;
            insideBounds = false;
        }
        if (newY < 8) {
            newY = 8;
            insideBounds = false;
        }
        if (newY > worldMap.getMapHeight() - 9) {
            newY = (worldMap.getMapHeight() - 9) & 0xFF;
            insideBounds = false;
        }
        setCellAndResetSubPos(newX, newY);
        return insideBounds;
    }

    /**
     * Native: TargetHandle::setPackedPositionWithinBounds @0054FD77.
     * Fully ported.
     */
    public boolean setPackedPositionWithinBounds(int packedXdX, int packedYdY) {
        int packedX = packedXdX & 0xFFFF;
        int packedY = packedYdY & 0xFFFF;
        int newX = (packedX >>> 8) & 0xFF;
        int newY = (packedY >>> 8) & 0xFF;
        boolean insideBounds = true;

        if (newX < 8) {
            newX = 8;
            insideBounds = false;
        }
        CWorldMap worldMap = (CWorldMap) mCWorldMap;
        if (newX > worldMap.getMapWidth() + 7) {
            newX = (worldMap.getMapWidth() + 7) & 0xFF;
            insideBounds = false;
        }
        if (newY < 8) {
            newY = 8;
            insideBounds = false;
        }
        if (newY > worldMap.getMapHeight() + 7) {
            newY = (worldMap.getMapHeight() + 7) & 0xFF;
            insideBounds = false;
        }
        setPos((newX << 8) | (packedX & 0xFF), (newY << 8) | (packedY & 0xFF));
        return insideBounds;
    }

    /**
     * Native: TargetHandle::clearSubPos @005551D6.
     * Fully ported.
     */
    public void clearSubPos() {
        dx = 0x80;
        dy = 0x80;
    }

    /**
     * Native: TargetHandle::isSubPosUnknown @00551383.
     * Fully ported.
     */
    public boolean isSubPosUnknown() {
        return dx == 0x80 && dy == 0x80;
    }

    /**
     * Native support extracted from TargetHandle::TargetHandle @0054FEAE and @0054FF06.
     * Fully ported.
     */
    public void assignFrom(TargetHandle from) {
        x = from.x;
        y = from.y;
        cell = from.cell;
        dx = from.dx;
        dy = from.dy;
        mCWorldMap = from.mCWorldMap;
    }

    // not ported.
    public static TargetHandle newDefaultOrNull(TargetHandle sourcePresence) {
        return sourcePresence == null ? null : new TargetHandle();
    }

    /**
     * Native: TargetHandle::packXdX @0054FF5B.
     * Fully ported.
     */
    public int packXdX() {
        return ((x & 0xFF) << 8) | (dx & 0xFF);
    }

    /**
     * Native: TargetHandle::packYdY @0054FF7C.
     * Fully ported.
     */
    public int packYdY() {
        return ((y & 0xFF) << 8) | (dy & 0xFF);
    }

    /**
     * Native: TargetHandle::getCell @0054FF9E.
     * Fully ported.
     */
    public int getCell() {
        return cell & 0xFFFF;
    }

    /**
     * Native: TargetHandle::euclideanDistanceByXY @0054FA9F.
     * Fully ported.
     */
    public int euclideanDistanceByXY(TargetHandle other) {
        int diffX = (x & 0xFF) - other.getX();
        int diffY = (y & 0xFF) - other.getY();
        return (int) Math.sqrt(diffX * diffX + diffY * diffY);
    }

    /**
     * Native: TargetHandle::euclideanDistanceByPackedPosition @0054FAFF.
     * Fully ported.
     */
    public int euclideanDistanceByPackedPosition(TargetHandle other) {
        int diffX = (packXdX() & 0xFFFF) - (other.packXdX() & 0xFFFF);
        int diffY = (packYdY() & 0xFFFF) - (other.packYdY() & 0xFFFF);
        return (int) Math.sqrt(diffX * diffX + diffY * diffY);
    }

    /**
     * Native: TargetHandle::isSameCell @0054FA73.
     * Fully ported.
     */
    public boolean isSameCell(TargetHandle other) {
        return (cell & 0xFFFF) == (other.cell & 0xFFFF);
    }

    /**
     * Native: TargetHandle::getX @0054FFB0.
     * Fully ported.
     */
    public int getX() {
        return x & 0xFF;
    }

    /**
     * Native: TargetHandle::getY @0054FFC0.
     * Fully ported.
     */
    public int getY() {
        return y & 0xFF;
    }

    /**
     * Native: TargetHandle::chebyshevDistanceByXY @0054FB7F.
     * Fully ported.
     */
    public int chebyshevDistanceByXY(TargetHandle other) {
        int dx = Math.abs((x & 0xFF) - other.getX());
        int dy = Math.abs((y & 0xFF) - other.getY());
        return Math.max(dx, dy);
    }

    /**
     * Native: TargetHandle::Serialize @0054FFD1.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        if (!ar.isStoring()) {
            x = ar.readByte() & 0xFF;
            y = ar.readByte() & 0xFF;
            cell = ar.readUShort();
            dx = ar.readByte() & 0xFF;
            dy = ar.readByte() & 0xFF;
            serializedPadding = ar.readUShort();

            int worldMapToken = ar.readInt();
            mCWorldMap = worldMapToken == 0 ? null : worldMapToken;
        } else {
            ar.writeByte(x);
            ar.writeByte(y);
            ar.writeShort(cell);
            ar.writeByte(dx);
            ar.writeByte(dy);
            ar.writeShort(serializedPadding);
            ar.writeInt(Utils.encodePointerLike(mCWorldMap));
        }
    }

    /**
     * Native: TargetHandle::RestoreContext @005596C0.
     * Fully ported.
     */
    void restoreContext() {
        if (mCWorldMap != null) {
            mCWorldMap = Globals.gameServer.lookupPointerMapOrKeepToken(mCWorldMap);
        }
    }
}
