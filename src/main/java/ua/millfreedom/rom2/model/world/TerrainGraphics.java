package ua.millfreedom.rom2.model.world;

import ua.millfreedom.rom2.model.CBmp256;
import ua.millfreedom.rom2.model.palette.CGamePalette;
import ua.millfreedom.rom2.res.Resources;

import java.util.Arrays;
import java.util.Locale;

import static ua.millfreedom.rom2.res.Constants.GRAPHICS;

public final class TerrainGraphics {
    private static final int TERRAIN_TILE_COUNT = 0x20;
    private static final int TERRAIN_TILE_VARIANTS = 4;
    private static final int TERRAIN_3D_FLAG = 0x2;

    // Native global g_someFlags? @0061B5B0.
    public static int terrainGraphicsFlags;

    // Native global g_pDirt.
    public static CBmp256 terrainDirtBitmap;

    // Native global g_tileSet.
    public static final CBmp256[][] terrainTileSet = new CBmp256[TERRAIN_TILE_COUNT][TERRAIN_TILE_VARIANTS];

    // Native global g_tilesPalettes.
    public static final CGamePalette[] terrainTilePalettes = new CGamePalette[TERRAIN_TILE_COUNT];

    /**
     * not ported.
     */
    private TerrainGraphics() {
    }

    /**
     * Native: ReloadTerrainTileGraphics @00476429.
     * Fully ported at the Java managed-allocation boundary.
     */
    public static void reloadTerrainTileGraphics(int terrainTileMask) {
        if ((terrainGraphicsFlags & TERRAIN_3D_FLAG) != 0) {
            reloadTerrainTilePass(terrainTileMask, true);
        }
        reloadTerrainTilePass(terrainTileMask, false);
        refreshTerrainTilePalettes();
    }

    /**
     * Native: cleanupTerrainGraphics @00476A00.
     * Fully ported.
     */
    public static void cleanupTerrainGraphics() {
        reloadTerrainTileGraphics(0);
        terrainDirtBitmap = null;
    }

    /**
     * Native: ResetTerrainTileSet @00476A54.
     * Fully ported. Native zeroes g_tileSet, a CBmp256 *[32][4] table, before runtime graphics initialization.
     */
    public static void resetTerrainTileSet() {
        for (CBmp256[] tileVariants : terrainTileSet) {
            Arrays.fill(tileVariants, null);
        }
    }

    /**
     * Native support extracted from ReloadTerrainTileGraphics @00476429 and shared with the editor preview reload.
     */
    private static void reloadTerrainTilePass(int terrainTileMask, boolean use3dTiles) {
        if (terrainTileMask != 0) {
            terrainDirtBitmap = loadBitmap(use3dTiles ? "3d/dirt.bmp" : "dirt.bmp");
        }

        for (int tileIndex = 0; tileIndex < TERRAIN_TILE_COUNT; tileIndex++) {
            if ((terrainTileMask & (1 << tileIndex)) == 0) {
                clearTileSet(tileIndex);
                continue;
            }

            int tileGroup = (tileIndex >> 2) + 1;
            for (int variant = 0; variant < TERRAIN_TILE_VARIANTS; variant++) {
                int variantNumber = variant + ((tileIndex & 3) << 2);
                String fileName = String.format(Locale.ROOT, "tile%d-%02d.bmp", tileGroup, variantNumber);
                terrainTileSet[tileIndex][variant] = loadBitmap(use3dTiles ? "3d/" + fileName : fileName);
            }
        }
    }

    /**
     * Native support extracted from ReloadTerrainTileGraphics @00476429.
     */
    private static void clearTileSet(int tileIndex) {
        for (int variant = 0; variant < TERRAIN_TILE_VARIANTS; variant++) {
            terrainTileSet[tileIndex][variant] = null;
        }
    }

    /**
     * Native support extracted from ReloadTerrainTileGraphics @00476429 and RefreshGamePalettes @0047E345.
     */
    public static void refreshTerrainTilePalettes() {
        for (int tileIndex = 0; tileIndex < TERRAIN_TILE_COUNT; tileIndex++) {
            CBmp256 tileBitmap = terrainTileSet[tileIndex][0];
            if (tileBitmap != null) {
                tileBitmap.initPalette(0x60, 3, 1);
                terrainTilePalettes[tileIndex] = tileBitmap.palette;
            }
        }
    }

    /**
     * Native support extracted from ReloadTerrainTileGraphics @00476429.
     */
    private static CBmp256 loadBitmap(String terrainRelativePath) {
        return new CBmp256(Resources.path(GRAPHICS, "terrain", terrainRelativePath));
    }
}
