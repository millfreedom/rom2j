package ua.millfreedom.rom2.model;

import lombok.SneakyThrows;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.res.ResInHeap;
import ua.millfreedom.rom2.res.Resources;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static ua.millfreedom.rom2.Globals.gameFileManager;
import static ua.millfreedom.rom2.res.Constants.*;

/**
 * Java/Ghidra grouping owner for native material and render-distance startup globals.
 */
public final class MaterialRuntimeData {
    private static final int CPU_INFO_MMX = 0x2;
    private static final int DISTANCE_RADIUS = 0x14;
    private static final int DISTANCE_TABLE_SIZE = DISTANCE_RADIUS * 2 + 1;
    private static final int MAIN_ID_BUFFER_SIZE = 0x50;
    private static final int MATERIAL_COUNT = 0x10;
    private static final int DISTORTION_RADIUS = 0x14;
    private static final int DISTORTION_DEPTH = 0x10;
    private static final String MATERIAL_REG = "material.reg";
    private static final String MATERIAL_SECTION = "Material%d";
    private static final String MATERIAL_PATH_KEY = "Path";
    private static final String HEROES = "heroes";

    /**
     * Native global g_squaredDistanceByDelta @00621898.
     */
    public final short[][] squaredDistanceByDelta = new short[DISTANCE_TABLE_SIZE][DISTANCE_TABLE_SIZE];

    /**
     * Native global g_radialScreenDistortion @00621454.
     */
    public RadialScreenDistortion radialScreenDistortion;

    /**
     * Native global g_unitMaterialSpritePaths @00622708.
     * Static CStringArray lifecycle thunks @00473FDC/@00473FEB/@00473FFA/@0047400C are represented by Java object lifecycle.
     */
    public final List<String> unitMaterialSpritePaths = new ArrayList<>(MATERIAL_COUNT);

    /**
     * Java support constructor for the shared material runtime wrapper.
     * not ported.
     */
    public MaterialRuntimeData() {
    }

    /**
     * Native: MaterialRuntimeData::loadMaterials @00474756.
     * Java port status: fully ported; native CPUInfo/MMX probing is a Java renderer boundary.
     */
    @SneakyThrows
    public void loadMaterials() {
        initializeSquaredDistanceTable();
        loadMainIdCustomEncodingFlag();
        radialScreenDistortion = new RadialScreenDistortion(DISTORTION_RADIUS, DISTORTION_DEPTH);
        loadUnitMaterialSpritePaths();
    }

    /**
     * Native: MaterialRuntimeData::releaseMaterialAndItemNameGlobals @00474A76.
     * Fully ported. g_itemname_pkt is not modeled in Java; radialScreenDistortion is the modeled native global released
     * here.
     */
    public void releaseMaterialAndItemNameGlobals() {
        radialScreenDistortion = null;
    }

    /**
     * Native support extracted from DAT_00621898/DAT_006218C0 initialization in MaterialRuntimeData::loadMaterials @00474756.
     */
    private void initializeSquaredDistanceTable() {
        for (int yDelta = 0; yDelta <= DISTANCE_RADIUS; yDelta++) {
            int ySquare = yDelta * yDelta;
            for (int xDelta = 0; xDelta <= DISTANCE_RADIUS; xDelta++) {
                short sumOfSquares = (short) (ySquare + xDelta * xDelta);
                squaredDistanceByDelta[DISTANCE_RADIUS + yDelta][DISTANCE_RADIUS + xDelta] = sumOfSquares;
                squaredDistanceByDelta[DISTANCE_RADIUS + yDelta][DISTANCE_RADIUS - xDelta] = sumOfSquares;
                squaredDistanceByDelta[DISTANCE_RADIUS - yDelta][DISTANCE_RADIUS + xDelta] = sumOfSquares;
                squaredDistanceByDelta[DISTANCE_RADIUS - yDelta][DISTANCE_RADIUS - xDelta] = sumOfSquares;
            }
        }
    }

    /**
     * Native support extracted from the main/id load in MaterialRuntimeData::loadMaterials @00474756.
     */
    private static void loadMainIdCustomEncodingFlag() {
        byte[] buffer = new byte[MAIN_ID_BUFFER_SIZE];
        try { //on some clients the main/id may not be present...
            ByteBuffer idData = gameFileManager.get(Resources.path(MAIN, "id"));
            idData.get(buffer, 0, idData.remaining());
            int idLength = nativeStringLength(buffer);
            Globals.useCustomEncoding = buffer[idLength - 1] - '0' != 0;
        } catch (Throwable ignored) { //fallback to English
            Globals.useCustomEncoding = false;
        }
    }

    /**
     * Native support extracted from strlen(buffer) in MaterialRuntimeData::loadMaterials @00474756.
     */
    private static int nativeStringLength(byte[] buffer) {
        int length = 0;
        while (length < buffer.length && buffer[length] != 0) {
            length++;
        }
        return length;
    }

    /**
     * Native support extracted from ResInHeap material.reg reads in MaterialRuntimeData::loadMaterials @00474756.
     */
    private void loadUnitMaterialSpritePaths() throws Exception {
        unitMaterialSpritePaths.clear();
        ResInHeap materialReg = ResInHeap.load(GRAPHICS, UNITS, MATERIAL_REG);
        for (int materialIndex = 0; materialIndex < MATERIAL_COUNT; materialIndex++) {
            String section = MATERIAL_SECTION.formatted(materialIndex);
            StringBuilder value = new StringBuilder(0x100);
            materialReg.getValueAsString(section, MATERIAL_PATH_KEY, HEROES, value, 0x100);
            unitMaterialSpritePaths.add(value.toString());
        }
    }

    /**
     * Native support extracted from g_squaredDistanceByDelta @00621898 readers.
     */
    public int getSquaredDistanceByDelta(int xDelta, int yDelta) {
        return Short.toUnsignedInt(squaredDistanceByDelta[yDelta + DISTANCE_RADIUS][xDelta + DISTANCE_RADIUS]);
    }
}
