package ua.millfreedom.rom2.model.render;

import ua.millfreedom.rom2.model.palette.Palette16;

import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Compact palette-derived straight-ARGB lookup for canonical A16 index/alpha codes.
 * This contains at most 16 * 256 colors and never contains frame pixels.
 */
public final class A16PaletteLookup {
    private static final int ALPHA_LEVEL_COUNT = 16;
    private static final int COLOR_INDEX_COUNT = 256;
    private static final int LOOKUP_SIZE = ALPHA_LEVEL_COUNT * COLOR_INDEX_COUNT;
    private static final Map<Palette16[], A16PaletteLookup> PAGE_LOOKUPS = new WeakHashMap<>();
    private static final Map<int[], MutablePaletteEntry> BASE_LOOKUPS = new WeakHashMap<>();

    private final int[] sourceColors;
    private final int[] colorCountByAlpha;

    /**
     * not ported. Retains one compact palette-derived lookup and each alpha row's valid source-index count.
     */
    private A16PaletteLookup(int[] sourceColors, int[] colorCountByAlpha) {
        this.sourceColors = sourceColors;
        this.colorCountByAlpha = colorCountByAlpha;
    }

    /**
     * Java support for reusing one lookup per immutable CGamePalette page-array generation.
     * not ported.
     */
    public static A16PaletteLookup resolve(Palette16[] palettePages) {
        synchronized (PAGE_LOOKUPS) {
            return PAGE_LOOKUPS.computeIfAbsent(palettePages, A16PaletteLookup::fromPalettePages);
        }
    }

    /**
     * Java support for reusing a compact lookup while an explicit font/base palette remains unchanged.
     * not ported.
     */
    public static A16PaletteLookup resolve(int[] basePalette) {
        synchronized (BASE_LOOKUPS) {
            MutablePaletteEntry entry = BASE_LOOKUPS.get(basePalette);
            if (entry == null || !Arrays.equals(entry.paletteSnapshot, basePalette)) {
                int[] paletteSnapshot = Arrays.copyOf(basePalette, basePalette.length);
                entry = new MutablePaletteEntry(
                        paletteSnapshot,
                        fromBasePalette(paletteSnapshot)
                );
                BASE_LOOKUPS.put(basePalette, entry);
            }
            return entry.lookup;
        }
    }

    /**
     * Java support for resolving one packed A16 pixel through the compact palette table.
     * not ported.
     */
    public int sourceColor(int encodedPixel) {
        int colorIndex = (encodedPixel >>> 1) & 0xFF;
        int alphaLevel = (encodedPixel >>> 9) & 0x0F;
        if (colorIndex >= colorCountByAlpha[alphaLevel]) {
            throw new ArrayIndexOutOfBoundsException(colorIndex);
        }
        return sourceColors[alphaLevel * COLOR_INDEX_COUNT + colorIndex];
    }

    /**
     * Java support for precomputing straight ARGB from immutable native source-contribution palette pages.
     * not ported.
     */
    private static A16PaletteLookup fromPalettePages(Palette16[] palettePages) {
        int[] colors = new int[LOOKUP_SIZE];
        int[] colorCounts = new int[ALPHA_LEVEL_COUNT];
        colorCounts[0] = COLOR_INDEX_COUNT;
        for (int alphaLevel = 1; alphaLevel < ALPHA_LEVEL_COUNT; alphaLevel++) {
            int colorCount = Math.min(palettePages[alphaLevel].data().length, COLOR_INDEX_COUNT);
            colorCounts[alphaLevel] = colorCount;
            int row = alphaLevel * COLOR_INDEX_COUNT;
            for (int colorIndex = 0; colorIndex < colorCount; colorIndex++) {
                int encodedPixel = (alphaLevel << 9) | (colorIndex << 1);
                colors[row + colorIndex] = A16SpriteDecoder.sourceColor(encodedPixel, palettePages);
            }
        }
        return new A16PaletteLookup(colors, colorCounts);
    }

    /**
     * Java support for precomputing straight ARGB from one explicit A16 font/base palette.
     * not ported.
     */
    private static A16PaletteLookup fromBasePalette(int[] basePalette) {
        int[] colors = new int[LOOKUP_SIZE];
        int colorCount = Math.min(basePalette.length, COLOR_INDEX_COUNT);
        int[] colorCounts = new int[ALPHA_LEVEL_COUNT];
        Arrays.fill(colorCounts, colorCount);
        colorCounts[0] = COLOR_INDEX_COUNT;
        for (int alphaLevel = 1; alphaLevel < ALPHA_LEVEL_COUNT; alphaLevel++) {
            int row = alphaLevel * COLOR_INDEX_COUNT;
            for (int colorIndex = 0; colorIndex < colorCount; colorIndex++) {
                int encodedPixel = (alphaLevel << 9) | (colorIndex << 1);
                colors[row + colorIndex] = A16SpriteDecoder.sourceColor(encodedPixel, basePalette);
            }
        }
        return new A16PaletteLookup(colors, colorCounts);
    }

    /**
     * Java-only mutable base-palette cache entry with exact content invalidation.
     */
    private record MutablePaletteEntry(int[] paletteSnapshot, A16PaletteLookup lookup) {
    }
}
