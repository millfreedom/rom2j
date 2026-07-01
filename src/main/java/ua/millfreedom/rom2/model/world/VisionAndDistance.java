package ua.millfreedom.rom2.model.world;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.column.UnitColumn;
import ua.millfreedom.rom2.model.unit.Unit;
import ua.millfreedom.rom2.model.unit.humanoid.Humanoid;
import ua.millfreedom.rom2.res.ResInHeap;

import java.util.Arrays;

/**
 * Native VisionAndDistance structure embedded in CWorldMap at 0x58EC0.
 * Size in native code: 0x3A00C.
 */
public final class VisionAndDistance {
    private static final String MAP_REG_PATH = "World/Data/map.reg";
    private static final String SCANNING_SECTION = "Scanning";
    private static final String SCAN_SHIFT_KEY = "ScanShift";
    private static final int DEFAULT_SCAN_SHIFT = 7;
    private static final int RAYCAST_CENTER = 0x14;
    private static final int RAYCAST_SPAN = 0x15;

    //0x00000
    public final short[][] grid = new short[256][256];

    //0x20000
    public final short[][] distanceSqr = new short[64][64];

    //0x22000
    public final Point8[][] rayStep = new Point8[64][64];

    //0x24000
    public final int[][] scanMap = new int[64][64];

    //0x28000
    public final short[][] stepCost = new short[64][64];

    //0x2A000
    public int bitfield;

    //0x2A004
    public int scanShift;

    //0x2A008
    public final byte[] visibilityMarkers = new byte[0x10000];

    //0x3A008
    public CWorldMap pCWorldMap;

    /**
     * Native: VisionAndDistance::VisionAndDistance @005536A4.
     * Fully ported.
     */
    public VisionAndDistance() {
        initializeRayStepHolders();
        scanShift = loadScanShift();
        bitfield = 1 << (scanShift & 0x1F);
        initDistanceSqrTable();
        initRaycastTable();
        clearGrid();
    }

    /**
     * Native: VisionAndDistance::ClearVisibilityMarkers @00553681.
     * Fully ported.
     */
    public void clearVisibilityMarkers() {
        Arrays.fill(visibilityMarkers, (byte) 0);
    }

    /**
     * Native: VisionAndDistance::ScanHumanoidVisibility @00553329.
     * Fully ported.
     */
    public void scanHumanoidVisibility(Humanoid humanoid, int posXY) {
        scanUnitVisibility(humanoid, posXY);
    }

    /**
     * Native: VisionAndDistance::ScanUnitVisibility @00552C28.
     * Fully ported.
     */
    public void scanUnitVisibility(Unit unit, int posXY) {
        int baseX = (posXY & 0xFF) - RAYCAST_CENTER;
        int baseY = ((posXY >>> 8) & 0xFF) - RAYCAST_CENTER;
        int baseCellOffset = baseY * 0x100 + baseX;
        for (int[] row : scanMap) {
            Arrays.fill(row, 0);
        }
        scanMap[RAYCAST_CENTER][RAYCAST_CENTER] = ((unit.sightRange & 0xFF) << (scanShift & 0x1F))
                + (1 << ((scanShift - 1) & 0x1F));
        int unitCell = unit.m_pTargetHandle.getCell();
        visibilityMarkers[unitCell] = 1;
        int sourceHeight = pCWorldMap.unkByteArray0x944F4[unitCell];

        for (int radius = 1; radius <= 0x13; radius++) {
            boolean allRaysExhausted = true;
            for (int delta = -radius; delta <= radius; delta++) {
                int horizontalRayX = RAYCAST_CENTER + delta;
                int topRayY = RAYCAST_CENTER - radius;
                if (pCWorldMap.isFullyInside(baseX + horizontalRayX, baseY + topRayY)
                        && !scanVisibilityRayCell(horizontalRayX, topRayY, baseCellOffset, sourceHeight)) {
                    allRaysExhausted = false;
                }

                int bottomRayY = RAYCAST_CENTER + radius;
                if (pCWorldMap.isFullyInside(baseX + horizontalRayX, baseY + bottomRayY)
                        && !scanVisibilityRayCell(horizontalRayX, bottomRayY, baseCellOffset, sourceHeight)) {
                    allRaysExhausted = false;
                }

                int leftRayX = RAYCAST_CENTER - radius;
                int verticalRayY = RAYCAST_CENTER + delta;
                if (pCWorldMap.isFullyInside(baseX + leftRayX, baseY + verticalRayY)
                        && !scanVisibilityRayCell(leftRayX, verticalRayY, baseCellOffset, sourceHeight)) {
                    allRaysExhausted = false;
                }

                int rightRayX = RAYCAST_CENTER + radius;
                int mirroredRayY = RAYCAST_CENTER - delta;
                if (pCWorldMap.isFullyInside(baseX + rightRayX, baseY + mirroredRayY)
                        && !scanVisibilityRayCell(rightRayX, mirroredRayY, baseCellOffset, sourceHeight)) {
                    allRaysExhausted = false;
                }
            }
            if (allRaysExhausted) {
                return;
            }
        }
    }

    /**
     * Native: VisionAndDistance::ScanVisibilityRayCell @00552AF8.
     * Fully ported.
     */
    private boolean scanVisibilityRayCell(int rayX, int rayY, int baseCellOffset, int sourceHeight) {
        int localOffset = rayY * 0x100 + rayX;
        int targetCell = baseCellOffset + localOffset;
        Point8 step = rayStep[rayX][rayY];
        scanMap[rayX][rayY] = scanMap[rayX + step.x][rayY + step.y]
                - (stepCost[rayX][rayY] + (pCWorldMap.unkByteArray0x944F4[targetCell] - sourceHeight));
        boolean exhausted = scanMap[rayX][rayY] < 1;
        if (!exhausted) {
            visibilityMarkers[targetCell] = 1;
        }
        return exhausted;
    }

    /**
     * Native support extracted from VisionAndDistance::VisionAndDistance @005536A4.
     */
    private void initializeRayStepHolders() {
        for (int y = 0; y < rayStep.length; y++) {
            for (int x = 0; x < rayStep[y].length; x++) {
                rayStep[y][x] = new Point8();
            }
        }
    }

    /**
     * Native support extracted from VisionAndDistance::VisionAndDistance @005536A4.
     */
    private int loadScanShift() {
        if (!Globals.gameFileManager.exists(MAP_REG_PATH)) {
            return DEFAULT_SCAN_SHIFT;
        }
        try {
            ResInHeap mapReg = ResInHeap.load(MAP_REG_PATH);
            return mapReg.getInt(SCANNING_SECTION, SCAN_SHIFT_KEY, DEFAULT_SCAN_SHIFT);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load " + MAP_REG_PATH + " for VisionAndDistance", e);
        }
    }

    /**
     * Native: VisionAndDistance::InitDistanceSqrTable @00552244.
     * Fully ported.
     */
    private void initDistanceSqrTable() {
        for (int i = 0; i < RAYCAST_SPAN; i++) {
            for (int j = 0; j < RAYCAST_SPAN; j++) {
                short value = (short) (i * i + j * j);
                setSymmetric(distanceSqr, i, j, value);
            }
        }
    }

    /**
     * Native: VisionAndDistance::InitRaycastTable @00552334.
     * Fully ported.
     */
    private void initRaycastTable() {
        for (int i = 0; i < RAYCAST_SPAN; i++) {
            for (int j = 0; j < RAYCAST_SPAN; j++) {
                byte xStep;
                byte yStep;
                if (j < (i >> 1)) {
                    xStep = (byte) 0xFF;
                    yStep = 0;
                } else if ((i << 1) < j) {
                    xStep = 0;
                    yStep = (byte) 0xFF;
                } else {
                    xStep = (byte) 0xFF;
                    yStep = (byte) 0xFF;
                }

                setSymmetricRayStep(i, j, xStep, yStep);
                setSymmetric(stepCost, i, j, calculateStepCost(i, j));
            }
        }

        rayStep[0x15][0x14].x = (byte) 0xFF;
        rayStep[0x15][0x14].y = 0;
        rayStep[0x13][0x14].x = 1;
        rayStep[0x13][0x14].y = 0;
    }

    /**
     * Native support extracted from VisionAndDistance::InitRaycastTable @00552334.
     */
    private short calculateStepCost(int i, int j) {
        int divisor = Math.max(i, j);
        if (divisor == 0) {
            return 0;
        }
        return (short) ((Math.sqrt(i * i + j * j) * bitfield) / divisor);
    }

    /**
     * Native support extracted from VisionAndDistance::InitDistanceSqrTable @00552244 and InitRaycastTable @00552334.
     */
    private static void setSymmetric(short[][] table, int i, int j, short value) {
        int positiveI = RAYCAST_CENTER + i;
        int negativeI = RAYCAST_CENTER - i;
        int positiveJ = RAYCAST_CENTER + j;
        int negativeJ = RAYCAST_CENTER - j;
        table[positiveI][positiveJ] = value;
        table[positiveI][negativeJ] = value;
        table[negativeI][positiveJ] = value;
        table[negativeI][negativeJ] = value;
    }

    /**
     * Native support extracted from VisionAndDistance::InitRaycastTable @00552334.
     */
    private void setSymmetricRayStep(int i, int j, byte xStep, byte yStep) {
        int positiveI = RAYCAST_CENTER + i;
        int negativeI = RAYCAST_CENTER - i;
        int positiveJ = RAYCAST_CENTER + j;
        int negativeJ = RAYCAST_CENTER - j;

        setRayStep(positiveI, positiveJ, xStep, yStep);
        setRayStep(negativeI, negativeJ, (byte) -xStep, (byte) -yStep);
        setRayStep(positiveI, negativeJ, xStep, (byte) -yStep);
        setRayStep(negativeI, positiveJ, (byte) -xStep, yStep);
    }

    /**
     * Native support extracted from VisionAndDistance::InitRaycastTable @00552334.
     */
    private void setRayStep(int i, int j, byte x, byte y) {
        rayStep[i][j].x = x;
        rayStep[i][j].y = y;
    }

    /**
     * Native: VisionAndDistance::clearGrid @00552793.
     * Fully ported.
     */
    public void clearGrid() {
        for (short[] row : grid) {
            Arrays.fill(row, (short) 0);
        }
    }

    /**
     * Native: VisionAndDistance::markUnitScanMask @00552108.
     * Fully ported.
     */
    public void markUnitScanMask(Unit unit) {
        int scanRadius = unit.movementState.missionScanRadius & 0xFF;
        if (scanRadius < 8) {
            markUnitScanMaskUnchecked(unit);
            return;
        }

        int x = unit.m_pTargetHandle.getX() & 0xFF;
        int y = unit.m_pTargetHandle.getY() & 0xFF;
        if (y < x) {
            if (x + scanRadius < 0x101) {
                if (y - scanRadius < 0) {
                    markUnitScanMaskBounded(unit);
                } else {
                    markUnitScanMaskUnchecked(unit);
                }
            } else {
                markUnitScanMaskBounded(unit);
            }
        } else if (x - scanRadius < 0) {
            markUnitScanMaskBounded(unit);
        } else if (y + scanRadius < 0x101) {
            markUnitScanMaskUnchecked(unit);
        } else {
            markUnitScanMaskBounded(unit);
        }
    }

    /**
     * Native: VisionAndDistance::markUnitScanMaskUnchecked @00552EAD.
     * Fully ported.
     */
    private void markUnitScanMaskUnchecked(Unit unit) {
        markUnitScanMaskSymmetric(unit, false);
    }

    /**
     * Native: VisionAndDistance::markUnitScanMaskBounded @0055302F.
     * Fully ported.
     */
    private void markUnitScanMaskBounded(Unit unit) {
        markUnitScanMaskSymmetric(unit, true);
    }

    /**
     * Native support extracted from VisionAndDistance::markUnitScanMaskUnchecked @00552EAD and
     * VisionAndDistance::markUnitScanMaskBounded @0055302F.
     * Fully ported.
     */
    private void markUnitScanMaskSymmetric(Unit unit, boolean bounded) {
        int mask = unit.unitInfoLine.getValue(UnitColumn.SERVER_ID.index) & 0xFFFF;
        int scanRadius = unit.movementState.missionScanRadius & 0xFF;
        int cell = unit.m_pTargetHandle.getCell() & 0xFFFF;
        int distanceLimit = (scanRadius + 1) * (scanRadius + 1);
        for (int dx = 0; dx <= scanRadius; dx++) {
            for (int dy = 0; dy <= scanRadius; dy++) {
                if ((distanceSqr[dx + RAYCAST_CENTER][dy + RAYCAST_CENTER] & 0xFFFF) >= distanceLimit) {
                    continue;
                }
                markGridCell(cell + dy * 0x100 + dx, mask, bounded);
                markGridCell(cell + dy * 0x100 - dx, mask, bounded);
                markGridCell(cell - dy * 0x100 + dx, mask, bounded);
                markGridCell(cell - dy * 0x100 - dx, mask, bounded);
            }
        }
    }

    /**
     * Native support extracted from VisionAndDistance::markUnitScanMaskUnchecked @00552EAD and
     * VisionAndDistance::markUnitScanMaskBounded @0055302F cell writes.
     * Fully ported.
     */
    private void markGridCell(int cell, int mask, boolean bounded) {
        if (bounded && (cell < 0 || cell >= 0x10000)) {
            return;
        }
        int packedCell = cell & 0xFFFF;
        int y = (packedCell >>> 8) & 0xFF;
        int x = packedCell & 0xFF;
        grid[y][x] = (short) (grid[y][x] | mask);
    }
}
