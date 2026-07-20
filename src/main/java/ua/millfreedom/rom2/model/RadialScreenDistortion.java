package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.Globals;

import java.awt.Point;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Port of the radial render-surface distortion object allocated by LoadMaterials @00474756.
 */
public final class RadialScreenDistortion {
    private static final int OFFSET_ENTRY_BYTES = 4;

    //0x00
    public RadialDistortionOffset[][] offsetRows;
    //0x04
    public int radius;
    //0x08
    public int distortionDepth;
    //0x0c
    public int tableSize;

    /**
     * Native: RadialScreenDistortion::RadialScreenDistortion @004A6840.
     * Fully ported.
     */
    public RadialScreenDistortion(int radius, int distortionDepth) {
        tableSize = (int) Math.sqrt(radius * radius - (radius - distortionDepth) * (radius - distortionDepth));
        offsetRows = new RadialDistortionOffset[tableSize][tableSize];
        this.radius = radius;
        this.distortionDepth = distortionDepth;
        buildOffsetTable();
    }

    /**
     * Native: RadialScreenDistortion::buildOffsetTable @004A695F.
     * Fully ported.
     */
    private void buildOffsetTable() {
        double[] distanceScales = new double[tableSize];
        int projectionDepth = radius - distortionDepth;
        for (int distance = 0; distance < tableSize; distance++) {
            double angle = Math.atan((double) distance / projectionDepth);
            if (distance == 0) {
                distanceScales[distance] = 1.0;
            } else {
                distanceScales[distance] = distance / (Math.sin(angle) * radius);
            }
        }

        for (int xDelta = 0; xDelta < tableSize; xDelta++) {
            for (int yDelta = 0; yDelta < tableSize; yDelta++) {
                int distanceIndex = (int) (Math.sqrt(xDelta * xDelta + yDelta * yDelta) + 0.5);
                if (distanceIndex < tableSize) {
                    int sourceXDelta = (int) (xDelta * distanceScales[distanceIndex] + 0.5);
                    int sourceYDelta = (int) (yDelta * distanceScales[distanceIndex] + 0.5);
                    offsetRows[xDelta][yDelta] = new RadialDistortionOffset(sourceXDelta, sourceYDelta);
                } else {
                    offsetRows[xDelta][yDelta] = new RadialDistortionOffset(xDelta, yDelta);
                }
            }
        }
    }

    /**
     * Native: RadialScreenDistortion::writeOffsetTableFile @004A6EE8.
     * Fully ported.
     */
    public void writeOffsetTableFile(Path filePath) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES * 3 + tableSize * tableSize * OFFSET_ENTRY_BYTES)
                .order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(radius);
        buffer.putInt(tableSize);
        buffer.putInt(distortionDepth);
        for (int xDelta = 0; xDelta < tableSize; xDelta++) {
            for (int yDelta = 0; yDelta < tableSize; yDelta++) {
                RadialDistortionOffset offset = offsetRows[xDelta][yDelta];
                buffer.putShort(offset.sourceXDelta);
                buffer.putShort(offset.sourceYDelta);
            }
        }
        Files.write(filePath, buffer.array());
    }

    /**
     * Native: RadialScreenDistortion::readOffsetTableFile @004A6FCC.
     * Fully ported.
     */
    public void readOffsetTableFile(Path filePath) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(Files.readAllBytes(filePath)).order(ByteOrder.LITTLE_ENDIAN);
        radius = buffer.getInt();
        tableSize = buffer.getInt();
        distortionDepth = buffer.getInt();
        offsetRows = new RadialDistortionOffset[tableSize][tableSize];
        for (int xDelta = 0; xDelta < tableSize; xDelta++) {
            for (int yDelta = 0; yDelta < tableSize; yDelta++) {
                int sourceXDelta = buffer.getShort();
                int sourceYDelta = buffer.getShort();
                offsetRows[xDelta][yDelta] = new RadialDistortionOffset(sourceXDelta, sourceYDelta);
            }
        }
    }

    /**
     * Native: RadialScreenDistortion::applyToRenderSurface @004A6B4B.
     * Fully ported for Java's active render target surface layout.
     */
    public void applyToRenderSurface(int centerX, int centerY) {
        Screen screen = Globals.screen;
        int[] surface = screen.surface();
        int surfaceLeft = screen.x();
        int surfaceTop = screen.y();
        int pitchPixels = screen.pitchPixels();
        CRect screenRect = new CRect(surfaceLeft, surfaceTop, screen.cx(), screen.cy());
        Point destPoint = new Point();
        Point sourcePoint = new Point();

        for (int xDelta = tableSize - 1; xDelta >= 0; xDelta--) {
            for (int yDelta = tableSize - 1; yDelta >= 0; yDelta--) {
                RadialDistortionOffset offset = offsetRows[xDelta][yDelta];
                copyDistortedPixel(surface, pitchPixels, surfaceLeft, surfaceTop, screenRect,
                        destPoint, sourcePoint, centerX, centerY,
                        xDelta, yDelta, offset.sourceXDelta, offset.sourceYDelta);
                copyDistortedPixel(surface, pitchPixels, surfaceLeft, surfaceTop, screenRect,
                        destPoint, sourcePoint, centerX, centerY,
                        xDelta, -yDelta, offset.sourceXDelta, -offset.sourceYDelta);
                copyDistortedPixel(surface, pitchPixels, surfaceLeft, surfaceTop, screenRect,
                        destPoint, sourcePoint, centerX, centerY,
                        -xDelta, yDelta, -offset.sourceXDelta, offset.sourceYDelta);
                copyDistortedPixel(surface, pitchPixels, surfaceLeft, surfaceTop, screenRect,
                        destPoint, sourcePoint, centerX, centerY,
                        -xDelta, -yDelta, -offset.sourceXDelta, -offset.sourceYDelta);
            }
        }
    }

    /**
     * Native support extracted from RadialScreenDistortion::applyToRenderSurface @004A6B4B.
     */
    private static void copyDistortedPixel(int[] surface,
                                           int pitchPixels,
                                           int surfaceLeft,
                                           int surfaceTop,
                                           CRect screenRect,
                                           Point destPoint,
                                           Point sourcePoint,
                                           int centerX,
                                           int centerY,
                                           int destXDelta,
                                           int destYDelta,
                                           int sourceXDelta,
                                           int sourceYDelta) {
        destPoint.move(centerX + destXDelta, centerY + destYDelta);
        if (!screenRect.contains(destPoint)) {
            return;
        }

        sourcePoint.move(centerX + sourceXDelta, centerY + sourceYDelta);
        if (!screenRect.contains(sourcePoint)) {
            return;
        }

        int destOffset = (destPoint.y - surfaceTop) * pitchPixels + destPoint.x - surfaceLeft;
        int sourceOffset = (sourcePoint.y - surfaceTop) * pitchPixels + sourcePoint.x - surfaceLeft;
        surface[destOffset] = surface[sourceOffset];
    }
}
