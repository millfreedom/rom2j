package ua.millfreedom.rom2.model.spell;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.container.CustomList;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Native class: EffectVisualBuilder (size 0xE0).
 */
public class EffectVisualBuilder {
    //0x0
    public final Point originPoint = new Point();

    //0x8
    public int lineDeltaXAbs;

    //0xc
    public int lineDeltaYAbs;

    //0x10
    public int lineMajorDelta;

    //0x14
    public int lineAccumulatorX;

    //0x18
    public int lineAccumulatorY;

    //0x1c
    public int lineStepX;

    //0x20
    public int lineStepY;

    //0x24
    public final Point sourcePoint = new Point();

    //0x2c
    public final Point targetPoint = new Point();

    //0x34
    public final Point[] sourceControlPoints = {new Point(), new Point()};

    //0x44
    public final Point[] targetControlPoints = {new Point(), new Point()};

    //0x54
    public final byte[] reserved0x54 = new byte[0x04];

    //0x58
    public double arcDeviationScale;

    //0x60
    public double pathLength;

    //0x68
    public final List<Point> points = new ArrayList<>();

    //0x7c
    public final CustomList<VisualElem> visualElements = CustomList.std(VisualElem.class);

    //0x90
    public final CustomList<Double> arcPathX = new CustomList<>(Double.class);

    //0xa4
    public final CustomList<Double> arcPathY = new CustomList<>(Double.class);

    //0xb8
    public final CustomList<Double> arcControlProgress = new CustomList<>(Double.class);

    //0xcc
    public final CustomList<Double> arcControlOffsets = new CustomList<>(Double.class);

    /**
     * Native: EffectVisualBuilder::EffectVisualBuilder @004C78B0.
     * Fully ported.
     */
    public EffectVisualBuilder() {
        visualElements.clear();
    }

    /**
     * Native support extracted from EffectVisualBuilder::buildStraightPathVisuals @004C96E2.
     * Fully ported.
     */
    public void buildStraightPathVisuals(int sourceX, int sourceY, int targetX, int targetY, int spriteId) {
        double deltaX = targetX - sourceX;
        double deltaY = targetY - sourceY;
        double length = Math.hypot(deltaX, deltaY);
        double stepX = deltaX / length * 13.0d;
        double stepY = deltaY / length * 13.0d;
        int count = (int) (length / 13.0d + 0.5d);
        double x = sourceX;
        double y = sourceY;
        for (int index = 0; index < count; index++) {
            visualElements.add(new VisualElem((int) x, (int) y, 0, spriteId, 0));
            x += stepX;
            y += stepY;
        }
    }

    /**
     * Native support extracted from EffectVisualBuilder::buildJaggedPathVisuals @004C7BA8.
     * Fully ported.
     */
    public void buildJaggedPathVisuals(int sourceX, int sourceY, int targetX, int targetY, int spriteId) {
        sourcePoint.x = sourceX;
        sourcePoint.y = sourceY;
        targetPoint.x = targetX;
        targetPoint.y = targetY;

        int absDeltaX = targetX - sourceX;
        int absDeltaY = targetY - sourceY;
        if (absDeltaX < 0) {
            absDeltaX = -absDeltaX;
        }
        if (absDeltaY < 0) {
            absDeltaY = -absDeltaY;
        }

        Point controlOffset = new Point();
        if (absDeltaY < absDeltaX) {
            if (absDeltaX < 10) {
                controlOffset.y = 1;
            } else {
                controlOffset.y = (int) (absDeltaX * 0.2d);
            }
        } else if (absDeltaY < 10) {
            controlOffset.x = 1;
        } else {
            controlOffset.x = (int) (absDeltaY * 0.2d);
        }

        sourceControlPoints[0].x = sourceX - controlOffset.x;
        sourceControlPoints[0].y = sourceY - controlOffset.y;
        sourceControlPoints[1].x = sourceX + controlOffset.x;
        sourceControlPoints[1].y = sourceY + controlOffset.y;
        targetControlPoints[0].x = targetX - controlOffset.x;
        targetControlPoints[0].y = targetY - controlOffset.y;
        targetControlPoints[1].x = targetX + controlOffset.x;
        targetControlPoints[1].y = targetY + controlOffset.y;

        int distance = (int) Math.hypot(absDeltaX, absDeltaY);
        int closeDistance = distance * 0x28 / 100 + 1;
        originPoint.x = sourceX;
        originPoint.y = sourceY;

        int targetControlIndex = Globals.currentTickMillis() & 1;
        Point lineTarget = targetControlPoints[targetControlIndex];
        while (!originPoint.equals(targetPoint)) {
            initializeLineWalker(originPoint.x, originPoint.y, lineTarget.x, lineTarget.y);
            int segmentLength = ((Globals.currentTickMillis() & 0x1F) * distance) / 100;
            if (segmentLength < (closeDistance >> 1)) {
                segmentLength = closeDistance >> 1;
            }

            int step = 0;
            while (!originPoint.equals(targetPoint) && step < segmentLength) {
                advanceLineWalker();
                visualElements.add(new VisualElem(originPoint.x, originPoint.y, 0, spriteId, 0));
                step++;
            }

            int remainingX = originPoint.x - targetPoint.x;
            int remainingY = originPoint.y - targetPoint.y;
            if ((double) closeDistance <= Math.hypot(remainingX, remainingY)) {
                if (targetControlIndex == 0) {
                    targetControlIndex = 1;
                    lineTarget = targetControlPoints[1];
                } else if (targetControlIndex == 1) {
                    targetControlIndex = 0;
                    lineTarget = targetControlPoints[0];
                }
            } else {
                lineTarget = targetPoint;
            }
        }
    }

    /**
     * Native support extracted from EffectVisualBuilder::initializeLineWalker @004C7AB0.
     * Fully ported.
     */
    private void initializeLineWalker(int startX, int startY, int targetX, int targetY) {
        lineAccumulatorY = 0;
        lineAccumulatorX = 0;
        lineDeltaXAbs = targetX - startX;
        lineDeltaYAbs = targetY - startY;
        if (lineDeltaXAbs < 0) {
            lineStepX = -1;
            lineDeltaXAbs = -lineDeltaXAbs;
        } else if (lineDeltaXAbs < 1) {
            lineStepX = 0;
        } else {
            lineStepX = 1;
        }

        if (lineDeltaYAbs < 0) {
            lineStepY = -1;
            lineDeltaYAbs = -lineDeltaYAbs;
        } else if (lineDeltaYAbs < 1) {
            lineStepY = 0;
        } else {
            lineStepY = 1;
        }

        if (lineDeltaYAbs < lineDeltaXAbs) {
            lineMajorDelta = lineDeltaXAbs;
        } else {
            lineMajorDelta = lineDeltaYAbs;
        }
        originPoint.x = startX;
        originPoint.y = startY;
    }

    /**
     * Native support extracted from EffectVisualBuilder::advanceLineWalker @004C9BF0.
     * Fully ported.
     */
    private void advanceLineWalker() {
        lineAccumulatorX += lineDeltaXAbs;
        lineAccumulatorY += lineDeltaYAbs;
        if (lineMajorDelta <= lineAccumulatorX) {
            lineAccumulatorX -= lineMajorDelta;
            originPoint.x += lineStepX;
        }
        if (lineMajorDelta <= lineAccumulatorY) {
            lineAccumulatorY -= lineMajorDelta;
            originPoint.y += lineStepY;
        }
    }

    /**
     * Native support extracted from EffectVisualBuilder::buildArcPathVisuals @004C8863.
     * Fully ported.
     */
    public void buildArcPathVisuals(double sourceX, double sourceY, double targetX, double targetY, int spriteId) {
        double deltaX = targetX - sourceX;
        double deltaY = targetY - sourceY;
        pathLength = Math.hypot(deltaX, deltaY);
        buildArcPathSamples(sourceX, sourceY, sourceX + pathLength, sourceY);

        double directionX = deltaX / pathLength;
        double directionY = deltaY / pathLength;
        double baseX = arcPathX.get(0);
        double baseY = arcPathY.get(0);
        for (int index = 0; index < arcPathX.size(); index++) {
            double localX = arcPathX.get(index);
            double localY = arcPathY.get(index);
            int visualX = (int) (((localX - baseX) * directionX + baseX) - (localY - baseY) * directionY);
            int visualY = (int) ((localY - baseY) * directionX + (localX - baseX) * directionY + baseY);
            visualElements.add(new VisualElem(visualX, visualY, 0, spriteId, 0));
        }
    }

    /**
     * Native support extracted from EffectVisualBuilder::buildArcPathSamples @004C819D.
     * Fully ported.
     */
    private void buildArcPathSamples(double sourceX, double sourceY, double targetX, double targetY) {
        double maxYOffset = pathLength * 0.15d;
        double deltaX = targetX - sourceX;
        double deltaY = targetY - sourceY;
        double progress = 0.0d;
        double offset = 0.0d;
        clearReusableArcState();
        arcDeviationScale = pathLength * 0.03d;

        double bendSign = Utils.randInclusive(1) == 0 ? -1.0d : 1.0d;
        arcControlProgress.clear();
        arcControlOffsets.clear();
        arcControlProgress.add(0.0d);
        arcControlOffsets.add(0.0d);
        do {
            double bendStep = Utils.randInclusive(6) * bendSign;
            double progressStep = Utils.randInclusive(0x31) * 0.01d;
            if (Math.abs(bendStep) > 2.0d && progressStep > 0.15d) {
                offset += bendStep;
                if (offset > 3.0d) {
                    offset = 3.0d;
                }
                if (offset < -3.0d) {
                    offset = -3.0d;
                }
                arcControlOffsets.add(offset);
                progress += progressStep;
                arcControlProgress.add(progress);
                bendSign *= -1.0d;
            }
        } while (progress < 0.7d);

        int lastIndex = arcControlProgress.size() - 1;
        if (arcControlProgress.get(lastIndex) >= 1.0d) {
            arcControlProgress.remove(lastIndex);
            arcControlOffsets.remove(lastIndex);
        }
        arcControlProgress.add(1.0d);
        arcControlOffsets.add(0.0d);

        List<Double> knotX = new ArrayList<>();
        List<Double> knotY = new ArrayList<>();
        knotX.add(sourceX);
        knotY.add(sourceY);

        double firstProgress = arcControlProgress.get(1);
        knotX.add(firstProgress * deltaX + sourceX);
        knotY.add(arcControlOffsets.get(1) * arcDeviationScale + firstProgress * deltaY + sourceY);

        int previousKnotIndex = 1;
        for (int controlIndex = 2; controlIndex < arcControlProgress.size() - 1; controlIndex++) {
            double controlX = arcControlProgress.get(controlIndex) * deltaX + sourceX;
            double controlY = arcControlOffsets.get(controlIndex) * arcDeviationScale
                    + arcControlProgress.get(controlIndex) * deltaY
                    + sourceY;
            double previousX = knotX.get(previousKnotIndex);
            double previousY = knotY.get(previousKnotIndex);
            knotX.add((controlX - previousX) / 2.0d + previousX);
            knotY.add((controlY - previousY) / 2.0d + previousY);
            knotX.add(controlX);
            knotY.add(controlY);
            previousKnotIndex = knotX.size() - 1;
        }
        knotX.add(targetX);
        knotY.add(targetY);

        for (int index = 2; index < knotX.size(); index += 2) {
            appendParabolaSamples(
                    knotX.get(index - 2),
                    knotY.get(index - 2),
                    knotX.get(index - 1),
                    knotY.get(index - 1),
                    knotX.get(index),
                    knotY.get(index)
            );
        }

        for (double y : arcPathY) {
            if (Math.abs(y - sourceY) > maxYOffset) {
                buildArcPathSamples(sourceX, sourceY, targetX, targetY);
                return;
            }
        }
    }

    /**
     * Native support extracted from EffectVisualBuilder::appendParabolaSamples @004C8069.
     * Fully ported.
     */
    private void appendParabolaSamples(double x0, double y0, double x1, double y1, double x2, double y2) {
        double x0Squared = x0 * x0;
        double x1Squared = x1 * x1;
        double x2Squared = x2 * x2;
        double denominator = (x1 - x0) * x2Squared + (x0 - x2) * x1Squared + (x2 - x1) * x0Squared;
        double quadratic = -((y1 - y0) * x2 + (y0 - y2) * x1 + (y2 - y1) * x0) / denominator;
        double linear = ((y1 - y0) * x2Squared + (y0 - y2) * x1Squared + (y2 - y1) * x0Squared)
                / denominator;
        double constant = -((x0 * y1 - x1 * y0) * x2Squared
                + (x2 * y0 - x0 * y2) * x1Squared
                + (x1 * y2 - x2 * y1) * x0Squared) / denominator;
        for (double x = x0; x < x2; x += 6.0d) {
            double y = (quadratic * x + linear) * x + constant;
            arcPathX.add(x);
            arcPathY.add(y);
        }
    }

    /**
     * Native support extracted from EffectVisualBuilder::clearReusableArcState @0046DC80.
     * Fully ported.
     */
    public void clearReusableArcState() {
        arcPathX.clear();
        arcPathY.clear();
        visualElements.clear();
    }

    /**
     * Native support extracted from Global::FUN_004C95E4.
     * Fully ported.
     */
    public void buildDirectionalEffectIcon(int tilePixelSize, int effectType, int frame) {
        int xOffset = 0;
        int yOffset = 0;
        switch (effectType) {
            case 0x10 -> xOffset = 6;
            case 0x18 -> xOffset = -6;
            case 0x22 -> yOffset = 6;
            case 0x2E -> yOffset = -6;
            default -> {
            }
        }
        visualElements.add(new VisualElem(-xOffset, -yOffset - tilePixelSize, 0, effectType, frame));
    }

    /**
     * Native support extracted from Global::FUN_004C96AE.
     * Fully ported.
     */
    public void buildCenteredEffectIcon(int tilePixelSize, int frame) {
        visualElements.add(new VisualElem(0, -tilePixelSize, 0, 0x14, frame));
    }

    /**
     * Native support extracted from Global::FUN_004C92E2.
     * Fully ported.
     */
    public void buildDescendingRingEffect(int tilePixelSize, float radius, int phase) {
        for (int degrees = 0x59; degrees > 0; degrees -= 0x12) {
            addRingQuad(tilePixelSize, radius, phase, degrees, 0x30);
        }
    }

    /**
     * Native support extracted from Global::FUN_004C9463.
     * Fully ported.
     */
    public void buildAscendingRingEffect(int tilePixelSize, float radius, int phase) {
        for (int degrees = 0; degrees < 0x5A; degrees += 0x12) {
            addRingQuad(tilePixelSize, radius, phase, degrees, 0x40);
        }
    }

    /**
     * Native support extracted from Global::FUN_004C8A31.
     * Fully ported.
     */
    public void advanceFallingBurstEffect(
            List<VisualElem> previousVisuals,
            int xRadius,
            int zRadius,
            int fallStepSource,
            int timer
    ) {
        int yStep = (int) ((double) fallStepSource / 7.0d + 1.0d);
        for (VisualElem visual : previousVisuals) {
            if (visual.spriteId == 0x38 && Byte.toUnsignedInt(visual.frame) < 7) {
                visual.frame++;
                visual.y -= (short) yStep;
                visualElements.add(new VisualElem(visual));
            }
        }
        if (timer < 8) {
            return;
        }

        int count = Utils.randBased(3, 3);
        for (int index = 0; index < count; index++) {
            double angle = radiansFromDegrees(Utils.randInclusive(0x167));
            visualElements.add(new VisualElem(
                    (int) (Math.cos(angle) * xRadius),
                    0,
                    (int) (Math.sin(angle) * zRadius),
                    0x38,
                    0
            ));
        }
    }

    /**
     * Native support extracted from Global::FUN_004C8C01.
     * Fully ported.
     */
    public void advanceRisingBurstEffect(
            List<VisualElem> previousVisuals,
            int xRadius,
            int zRadius,
            int riseStepSource,
            int timer
    ) {
        int yStep = (int) ((double) riseStepSource / 7.0d + 1.0d);
        for (VisualElem visual : previousVisuals) {
            if (visual.spriteId == 0x3C && Byte.toUnsignedInt(visual.frame) < 7) {
                visual.frame++;
                visual.y += (short) yStep;
                visualElements.add(new VisualElem(visual));
            }
        }
        if (timer < 8) {
            return;
        }

        int count = Utils.randBased(3, 3);
        for (int index = 0; index < count; index++) {
            double angle = radiansFromDegrees(Utils.randInclusive(0x167));
            visualElements.add(new VisualElem(
                    (int) (Math.cos(angle) * xRadius),
                    -riseStepSource,
                    (int) (Math.sin(angle) * zRadius),
                    0x3C,
                    0
            ));
        }
    }

    /**
     * Native support extracted from CArray<>::FUN_004C9144.
     * Fully ported.
     */
    public void buildCrossSectionBandEffect(int verticalOffset, float horizontalRadius, float verticalRadius, int phase) {
        buildRotatedOvalBand(0, 0, verticalOffset, horizontalRadius, verticalRadius, phase << 2);

        int offset = (int) Math.abs(((phase * 0.022222223f - 1.0f) * horizontalRadius));
        int height = (int) (Math.sin(Math.acos((double) offset / horizontalRadius)) * verticalRadius);
        int frame = 4 - Math.abs(phase - 0x2D) / 9;
        rebuildCirclePoints(0, 0, height, frame * 2);
        for (Point point : points) {
            visualElements.add(new VisualElem(point.x, -(verticalOffset + offset), point.y, 0x3E, frame));
            visualElements.add(new VisualElem(point.x, -(verticalOffset - offset), point.y, 0x3E, frame));
        }
    }

    /**
     * Native support extracted from Global::FUN_004C7A45 and Global::FUN_004C7A79.
     * Fully ported.
     */
    public static void sortVisualElementsByYDescending(List<VisualElem> elements) {
        elements.sort((left, right) -> Short.compare(right.y, left.y));
    }

    /**
     * Native support extracted from Global::FUN_004C9C90.
     * Fully ported.
     */
    private static double radiansFromDegrees(int degrees) {
        return (Math.PI / 180.0d) * degrees;
    }

    /**
     * Native support extracted from CArray<>::FUN_004C8DD5.
     * Fully ported.
     */
    private void rebuildCirclePoints(int centerX, int centerY, int radius, int stepInterval) {
        int x = 0;
        int y = radius;
        int decision = (1 - radius) * 2;
        int intervalCounter = stepInterval;
        points.clear();
        while (y > 0) {
            if (stepInterval <= intervalCounter) {
                points.add(new Point(centerX + x, centerY + y));
                points.add(new Point(centerX - x, centerY + y));
                points.add(new Point(centerX + x, centerY - y));
                points.add(new Point(centerX - x, centerY - y));
                intervalCounter = 0;
            }
            intervalCounter++;
            if (decision < 0) {
                int threshold = (decision + y) * 2 - 1;
                if (threshold < 1) {
                    decision = decision + 1 + (x + 1) * 2;
                } else {
                    y--;
                    decision = decision + 2 + ((x + 1) - y) * 2;
                }
                x++;
            } else if (decision == 0) {
                x++;
                y--;
                decision = (x - y) * 2 + 2;
            } else {
                int threshold = (decision - x) * 2 - 1;
                if (threshold < 1) {
                    x++;
                    y--;
                    decision = decision + 2 + (x - y) * 2;
                } else {
                    y--;
                    decision = decision + y * -2 + 1;
                }
            }
        }
    }

    /**
     * Native support extracted from CArray<>::FUN_004C8F9C.
     * Fully ported.
     */
    private void buildRotatedOvalBand(
            int originX,
            int originY,
            int zBase,
            float horizontalRadius,
            float verticalRadius,
            int angleDegrees
    ) {
        float yScale = horizontalRadius / verticalRadius;
        double angle = radiansFromDegrees(angleDegrees);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double frameScale = 5.0d / horizontalRadius;
        rebuildCirclePoints(originX, originY, (int) verticalRadius, 4);
        for (Point point : points) {
            int xRotated = (int) (point.x * cos);
            int zRotated = (int) (point.x * sin);
            int y = -(int) (point.y * yScale);
            int frame = Math.abs(4 - (int) (Math.abs(point.y) * frameScale));
            visualElements.add(new VisualElem(originX + xRotated, originY + y, zBase + zRotated, 0x3E, frame));
            visualElements.add(new VisualElem(originX - zRotated, originY + y, zBase + xRotated, 0x3E, frame));
        }
    }

    /**
     * Native support extracted from Global::FUN_004C92E2 and Global::FUN_004C9463 ring quadrants.
     * Fully ported.
     */
    private void addRingQuad(int tilePixelSize, float radius, int phase, int degrees, int spriteId) {
        double angle = radiansFromDegrees(degrees + (phase & 0xFF) * 0x12);
        int x = (int) (Math.cos(angle) * radius);
        int z = (int) (Math.sin(angle) * radius);
        int frame = 4 - degrees / 0x12;
        visualElements.add(new VisualElem(x, -tilePixelSize, z, spriteId, frame));
        visualElements.add(new VisualElem(z, -tilePixelSize, -x, spriteId, frame));
        visualElements.add(new VisualElem(-x, -tilePixelSize, -z, spriteId, frame));
        visualElements.add(new VisualElem(-z, -tilePixelSize, x, spriteId, frame));
    }
}
