package ua.millfreedom.rom2.model.palette;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.color.RGB16;
import ua.millfreedom.rom2.model.color.RGB32;
import ua.millfreedom.rom2.res.Resources;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ua.millfreedom.rom2.res.Constants.GRAPHICS;

public final class Palettes {
    private static final String HUMAN_UNIT_PALETTE_PATH = Resources.path(GRAPHICS, "units", "humans", "human.pal");
    private static final int UNIT_OWNER_PALETTE_COUNT = 0x10;
    private static final int UNIT_OWNER_PALETTE_BYTES = 0x400;
    private static final int UNIT_OWNER_COMPLEMENT_COLOR_INDEX = 0xA4;

    public static final int SIZE16 = 16;
    public static final int SIZE256 = 256;
    public static final Palette16 flat = new Palette16(new RGB16[SIZE16]);
    public static final Palette16 gray = new Palette16(new RGB16[SIZE16]);
    public static final Palette16 grayDim = new Palette16(new RGB16[SIZE16]);
    public static final Palette16 yellowish = new Palette16(new RGB16[SIZE16]);
    public static final Palette16 greenLeaningGray = new Palette16(new RGB16[SIZE16]);
    public static final Palette16 orangeish = new Palette16(new RGB16[SIZE16]);
    public static final Palette16 redish = new Palette16(new RGB16[SIZE16]);
    public static final Palette16 greenish = new Palette16(new RGB16[SIZE16]);
    public static final Palette16 sepia = new Palette16(new RGB16[SIZE16]);
    public static final Palette16 darkRed = new Palette16(new RGB16[SIZE16]);
    public static final Palette16 brownish = new Palette16(new RGB16[SIZE16]);
    public static final Palette16 greenToRed = new Palette16(new RGB16[SIZE16]);
    public static final Palette16 highlight = new Palette16(new RGB16[SIZE16]);
    public static final Palette16 hover = new Palette16(new RGB16[SIZE16]);

    public static final Palette256 warmYellowBrown = new Palette256(new RGB32[SIZE256]);
    public static final Palette256 brightYellow = new Palette256(new RGB32[SIZE256]);
    public static final Palette256 black = new Palette256(new RGB32[SIZE256]);
    public static final Palette256 darkBrown = new Palette256(new RGB32[SIZE256]);
    public static final Palette256 purple = new Palette256(new RGB32[SIZE256]);

    public static final CGamePalette p1 = new CGamePalette();
    public static final CGamePalette p2 = new CGamePalette();
    public static final CGamePalette p3 = new CGamePalette();
    public static final CGamePalette p4 = new CGamePalette();
    public static final CGamePalette p5 = new CGamePalette();

    public static final List<Palette16> p16;
    public static final List<Palette256> p256;
    public static final List<CGamePalette> gamePalettes;
    /**
     * Native global: {@code GamePaletteArray_units @00622960}. Static CArray lifetime glue:
     * initializer @0047636C, constructor @0047637B, atexit wrapper @0047638A, destructor @0047639C.
     */
    public static final List<CGamePalette> unitGamePalettes = new ArrayList<>();
    public static final Palette16[] unitPaletteComplements = new Palette16[UNIT_OWNER_PALETTE_COUNT];
    private static Palette16 messagePrimary = gray;
    private static Palette16 messageDim = grayDim;
    private static Palette16 messageAccent = gray;
    private static Palette16 messagePlayerSlot4 = gray;
    private static Palette16 messagePlayerSlot1 = gray;
    private static Palette16 messagePlayerSlot0 = gray;

    static {
        p16 = initP16();
        p256 = initP256();
        gamePalettes = initGamePalettes();
    }

    // Native support extracted from Global::InitPalettes @0045EA70 CGamePalette initialization tail.
    private static List<CGamePalette> initGamePalettes() {
        p1.init(warmYellowBrown, 16, 4, 0);
        p2.init(brightYellow, 16, 4, 0);
        p3.init(black, 16, 4, 0);
        p4.init(darkBrown, 16, 4, 0);
        p5.init(purple, 16, 4, 0);
        return List.of(p1, p2, p3, p4, p5);
    }

    // Native support extracted from Global::InitPalettes @0045EA70 256-color RGB ramp initialization.
    private static List<Palette256> initP256() {
        for (int i = 0; i < SIZE256; i += 1) {
            warmYellowBrown.data()[i] = RGB32.from(((i * 0xb9) / 0xff), ((i * 0x9f) / 0xff), ((i * 0x49) / 0xff));
            brightYellow.data()[i] = RGB32.from(((i * 0xff) / 0xff), ((i * 0xff) / 0xff), ((i * 0x74) / 0xff));
            black.data()[i] = RGB32.from(0, 0, 0);
            darkBrown.data()[i] = RGB32.from(((i * 0x41) / 0xff), ((i * 0x2f) / 0xff), ((i * 0x14) / 0xff));
            purple.data()[i] = RGB32.from(((i * 0x65) / 0xff), ((i * 0x27) / 0xff), ((i * 0x3d) / 0xff));
        }
        return List.of(warmYellowBrown, brightYellow, black, darkBrown, purple);
    }

    // Native support extracted from Global::InitPalettes @0045EA70 16-color palette initialization.
    private static List<Palette16> initP16() {
        for (int i = 0; i < SIZE16; i += 1) {
            flat.data()[i] = RGB16.from(8, 8, 8);
            gray.data()[i] = RGB16.from(i * 0x11, i * 0x11, i * 0x11);
            grayDim.data()[i] = RGB16.from(i * 0xe, i * 0xe, i * 0xe);
            yellowish.data()[i] = RGB16.from((i * 0xb9) / 0xf, (i * 0x9f) / 0xf, (i * 0x49) / 0xf);
            greenLeaningGray.data()[i] = RGB16.from((i * 0x6b) / 0xf, (i * 0x9a) / 0xf, (i * 0x78) / 0xf);
            orangeish.data()[i] = RGB16.from((i * 0xff) / 0xf, (i * 0xff) / 0xf, (i << 6) / 0xf);
            redish.data()[i] = RGB16.from((i * 0xff) / 0xf, (i << 6) / 0xf, (i << 6) / 0xf);
            greenish.data()[i] = RGB16.from((i << 6) / 0xf, (i * 0xff) / 0xf, (i << 6) / 0xf);
            sepia.data()[i] = RGB16.from((i * 0xa0) / 0xf, (i * 0x78) / 0xf, (i * 0x32) / 0xf);
            darkRed.data()[i] = RGB16.from((i * 0x8b) / 0xf, (i * 0x41) / 0xf, (i << 5) / 0xf);
            brownish.data()[i] = RGB16.from((i * 0x96) / 0xf, (i * 0x5a) / 0xf, 0);
            greenToRed.data()[i] = RGB16.from(0x8d - (i * 0x3d) / 0xf, (i * 0x7e) / 0xf, (i * 0x31) / 0xf);
            highlight.data()[i] = RGB16.from(0xff - (i * 0xaf) / 0xf, (i * 0xfc) / 0xf, (i * 0x62) / 0xf);
            hover.data()[i] = RGB16.from(((0xf - i) * 0xa0) / 0xf + 0x20, ((0xf - i) * 0x94) / 0xf + 0x20, ((0xf - i) * 0x58) / 0xf + 0x20);
        }
        return List.of(flat, gray, grayDim, yellowish, greenLeaningGray, orangeish, redish, greenish, sepia, darkRed, brownish, greenToRed, highlight, hover);
    }

    /**
     * Native: Global::setMessageColorsPalette @004756C3.
     * Fully ported.
     */
    public static void setMessageColorsPalette(int messageColors) {
        Globals.gamePreferences.messageColors = messageColors;
        if (messageColors == 0) {
            messagePrimary = gray;
            messageDim = grayDim;
            messageAccent = gray;
            messagePlayerSlot1 = gray;
            messagePlayerSlot4 = gray;
            messagePlayerSlot0 = gray;
            return;
        }

        messagePrimary = p16.get(2);
        messageDim = p16.get(7);
        messageAccent = orangeish;
        messagePlayerSlot1 = p16.get(1);
        messagePlayerSlot4 = p16.get(4);
        messagePlayerSlot0 = p16.get(0);
    }

    /**
     * Native support extracted from message palette pointer `PTR_g_gray_005f17f4`.
     */
    public static Palette16 messagePrimary() {
        return messagePrimary;
    }

    /**
     * Native support extracted from message palette pointer `PTR_g_grayDim_005f17f8`.
     */
    public static Palette16 messageDim() {
        return messageDim;
    }

    /**
     * Native support extracted from message palette pointer `PTR_g_gray_005f17fc`.
     */
    public static Palette16 messageAccent() {
        return messageAccent;
    }

    /**
     * Native support extracted from message palette pointer `PTR_g_gray_005f1800`.
     */
    public static Palette16 messagePlayerSlot4() {
        return messagePlayerSlot4;
    }

    /**
     * Native support extracted from message palette pointer `PTR_g_gray_005f1804`.
     */
    public static Palette16 messagePlayerSlot1() {
        return messagePlayerSlot1;
    }

    /**
     * Native support extracted from message palette pointer `PTR_g_gray_005f1808`.
     */
    public static Palette16 messagePlayerSlot0() {
        return messagePlayerSlot0;
    }

    /**
     * Native support extracted from CA16Font::DrawTextInternal @0045E8FD forwarding pColorTable to CA16::Draw @00427EF0.
     */
    public static Object a16FontPaletteOverride(Palette16 palette) {
        for (CGamePalette gamePalette : gamePalettes) {
            if (gamePalette.paletteData[0] == palette) {
                return gamePalette.paletteData;
            }
        }
        return palette.data();
    }

    /**
     * Native support extracted from the trailing `human.pal` bootstrap in UnitTypes::loadUnitTypes @00479B1E and
     * owner-palette rebuild in Global::RefreshGamePalettes @0047E345. Populates Java equivalents of native
     * `GamePaletteArray_units @00622960` and `g_palComplements @0061FAC8`.
     */
    public static void loadUnitOwnerPalettes() {
        unitGamePalettes.clear();
        Arrays.fill(unitPaletteComplements, null);

        ByteBuffer humanPaletteData = Globals.gameFileManager.get(HUMAN_UNIT_PALETTE_PATH).duplicate().order(ByteOrder.LITTLE_ENDIAN);
        for (int paletteIndex = 0; paletteIndex < UNIT_OWNER_PALETTE_COUNT; paletteIndex++) {
            Palette256 rawPalette = readUnitOwnerPalette(humanPaletteData, paletteIndex);
            CGamePalette gamePalette = new CGamePalette();
            gamePalette.init(rawPalette, UNIT_OWNER_PALETTE_COUNT, 2, 1);
            unitGamePalettes.add(gamePalette);
            unitPaletteComplements[paletteIndex] = buildUnitPaletteComplement(gamePalette);
        }

        CGamePalette grayscalePalette = new CGamePalette();
        grayscalePalette.init(readUnitOwnerPalette(humanPaletteData, 0), UNIT_OWNER_PALETTE_COUNT, 5, 0);
        unitGamePalettes.add(grayscalePalette);
    }

    /**
     * Native support extracted from GamePaletteArray_units cleanup in Global::releaseUnitTypeRuntimeGlobals @0047AF9D.
     */
    public static void releaseUnitOwnerGamePalettesForShutdown() {
        unitGamePalettes.clear();
    }

    /**
     * Native support extracted from the `CGameFile::Read(&gameFile, human.pal, 0x4000)` bootstrap loop in
     * UnitTypes::loadUnitTypes @00479B1E.
     */
    private static Palette256 readUnitOwnerPalette(ByteBuffer humanPaletteData, int paletteIndex) {
        ByteBuffer paletteSlice = humanPaletteData.slice(paletteIndex * UNIT_OWNER_PALETTE_BYTES, UNIT_OWNER_PALETTE_BYTES)
                .order(ByteOrder.LITTLE_ENDIAN);
        return Palette256.read(paletteSlice);
    }

    /**
     * Native support extracted from the `g_palComplements[i1][j1][0..1]` writes in UnitTypes::loadUnitTypes @00479B1E.
     */
    private static Palette16 buildUnitPaletteComplement(CGamePalette gamePalette) {
        RGB16[] complement = new RGB16[SIZE16];
        for (int pairIndex = 0; pairIndex < 8; pairIndex++) {
            RGB16 color = gamePalette.paletteData[0x0F - pairIndex].data()[UNIT_OWNER_COMPLEMENT_COLOR_INDEX];
            complement[pairIndex * 2] = color;
            complement[pairIndex * 2 + 1] = color;
        }
        return new Palette16(complement);
    }

    /**
     * Native: Global::InitPalettes @0045EA70.
     * Java port status: fully ported through class initialization.
     */
    public static void ensureStaticLoad() {
    }
}
