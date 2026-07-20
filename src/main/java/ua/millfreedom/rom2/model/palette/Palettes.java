package ua.millfreedom.rom2.model.palette;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.color.RGB32;
import ua.millfreedom.rom2.res.Resources;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static ua.millfreedom.rom2.res.Constants.GRAPHICS;

public final class Palettes {
    private static final String HUMAN_UNIT_PALETTE_PATH = Resources.path(GRAPHICS, "units", "humans", "human.pal");
    private static final int UNIT_OWNER_PALETTE_COUNT = 0x10;
    private static final int UNIT_OWNER_PALETTE_BYTES = 0x400;
    private static final int UNIT_OWNER_COMPLEMENT_COLOR_INDEX = 0xA4;
    private static final int MESSAGE_PRIMARY_UNIT_PALETTE_INDEX = 2;
    private static final int MESSAGE_DIM_UNIT_PALETTE_INDEX = 7;
    private static final int CHAT_DELIVERY_ALLIED_UNIT_PALETTE_INDEX = 1;
    private static final int CHAT_DELIVERY_PRIVATE_UNIT_PALETTE_INDEX = 4;
    private static final int CHAT_DELIVERY_SHOUT_UNIT_PALETTE_INDEX = 0;

    public static final int SIZE16 = 16;
    public static final int SIZE256 = 256;
    public static final Palette16 flat = new Palette16(new int[SIZE16]);
    public static final Palette16 gray = new Palette16(new int[SIZE16]);
    public static final Palette16 grayDim = new Palette16(new int[SIZE16]);
    public static final Palette16 yellowish = new Palette16(new int[SIZE16]);
    public static final Palette16 greenLeaningGray = new Palette16(new int[SIZE16]);
    public static final Palette16 orangeish = new Palette16(new int[SIZE16]);
    public static final Palette16 redish = new Palette16(new int[SIZE16]);
    public static final Palette16 greenish = new Palette16(new int[SIZE16]);
    public static final Palette16 sepia = new Palette16(new int[SIZE16]);
    public static final Palette16 darkRed = new Palette16(new int[SIZE16]);
    public static final Palette16 brownish = new Palette16(new int[SIZE16]);
    public static final Palette16 greenToRed = new Palette16(new int[SIZE16]);
    public static final Palette16 highlight = new Palette16(new int[SIZE16]);
    public static final Palette16 hover = new Palette16(new int[SIZE16]);

    public static final Palette256 warmYellowBrown = new Palette256(new int[SIZE256]);
    public static final Palette256 brightYellow = new Palette256(new int[SIZE256]);
    public static final Palette256 black = new Palette256(new int[SIZE256]);
    public static final Palette256 darkBrown = new Palette256(new int[SIZE256]);
    public static final Palette256 purple = new Palette256(new int[SIZE256]);

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
    /**
     * Java retention for the stable entries backing native {@code GamePaletteArray_units @00622960}.
     */
    private static final List<CGamePalette> UNIT_GAME_PALETTE_STORAGE = initUnitGamePaletteStorage();
    /**
     * Java cache for the immutable mode-5 conversion selected by CUnit::Draw @004632A1.
     */
    private static final Map<Palette256, CGamePalette> UNIT_GRAYSCALE_PALETTES = new IdentityHashMap<>();
    /**
     * Java retention for the raw {@code human.pal} sources read by UnitTypes::loadUnitTypes @00479B1E.
     */
    private static Palette256[] unitOwnerPaletteSources;
    /**
     * Packed effective tint recipe used by CGamePalette::Init @0042339E for the retained owner palettes.
     */
    private static int unitOwnerPaletteTintRecipe = -1;
    public static final Palette16[] unitPaletteComplements = initUnitPaletteComplements();
    private static Palette16 messagePrimary = gray;
    private static Palette16 messageDim = grayDim;
    private static Palette16 chatSayOrBroadcast = gray;
    private static Palette16 chatPrivate = gray;
    private static Palette16 chatAllied = gray;
    private static Palette16 chatShout = gray;

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

    /**
     * Native support for the 17 stable entries in {@code GamePaletteArray_units @00622960}.
     */
    private static List<CGamePalette> initUnitGamePaletteStorage() {
        List<CGamePalette> palettes = new ArrayList<>(UNIT_OWNER_PALETTE_COUNT + 1);
        for (int paletteIndex = 0; paletteIndex <= UNIT_OWNER_PALETTE_COUNT; paletteIndex++) {
            palettes.add(new CGamePalette());
        }
        return List.copyOf(palettes);
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
            flat.data()[i] = RGB32.from(8, 8, 8);
            gray.data()[i] = RGB32.from(i * 0x11, i * 0x11, i * 0x11);
            grayDim.data()[i] = RGB32.from(i * 0xe, i * 0xe, i * 0xe);
            yellowish.data()[i] = RGB32.from((i * 0xb9) / 0xf, (i * 0x9f) / 0xf, (i * 0x49) / 0xf);
            greenLeaningGray.data()[i] = RGB32.from((i * 0x6b) / 0xf, (i * 0x9a) / 0xf, (i * 0x78) / 0xf);
            orangeish.data()[i] = RGB32.from((i * 0xff) / 0xf, (i * 0xff) / 0xf, (i << 6) / 0xf);
            redish.data()[i] = RGB32.from((i * 0xff) / 0xf, (i << 6) / 0xf, (i << 6) / 0xf);
            greenish.data()[i] = RGB32.from((i << 6) / 0xf, (i * 0xff) / 0xf, (i << 6) / 0xf);
            sepia.data()[i] = RGB32.from((i * 0xa0) / 0xf, (i * 0x78) / 0xf, (i * 0x32) / 0xf);
            darkRed.data()[i] = RGB32.from((i * 0x8b) / 0xf, (i * 0x41) / 0xf, (i << 5) / 0xf);
            brownish.data()[i] = RGB32.from((i * 0x96) / 0xf, (i * 0x5a) / 0xf, 0);
            greenToRed.data()[i] = RGB32.from(0x8d - (i * 0x3d) / 0xf, (i * 0x7e) / 0xf, (i * 0x31) / 0xf);
            highlight.data()[i] = RGB32.from(0xff - (i * 0xaf) / 0xf, (i * 0xfc) / 0xf, (i * 0x62) / 0xf);
            hover.data()[i] = RGB32.from(((0xf - i) * 0xa0) / 0xf + 0x20, ((0xf - i) * 0x94) / 0xf + 0x20, ((0xf - i) * 0x58) / 0xf + 0x20);
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
            chatSayOrBroadcast = gray;
            chatAllied = gray;
            chatPrivate = gray;
            chatShout = gray;
            return;
        }

        messagePrimary = unitOwnerTextPalette(MESSAGE_PRIMARY_UNIT_PALETTE_INDEX);
        messageDim = unitOwnerTextPalette(MESSAGE_DIM_UNIT_PALETTE_INDEX);
        chatSayOrBroadcast = orangeish;
        chatAllied = unitOwnerTextPalette(CHAT_DELIVERY_ALLIED_UNIT_PALETTE_INDEX);
        chatPrivate = unitOwnerTextPalette(CHAT_DELIVERY_PRIVATE_UNIT_PALETTE_INDEX);
        chatShout = unitOwnerTextPalette(CHAT_DELIVERY_SHOUT_UNIT_PALETTE_INDEX);
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
     * Native support extracted from chat delivery 0/4 palette pointer `PTR_chatSayOrBroadcastPalette_005f17fc`.
     */
    public static Palette16 chatSayOrBroadcast() {
        return chatSayOrBroadcast;
    }

    /**
     * Native support extracted from chat delivery 2 palette pointer `PTR_chatPrivatePalette_005f1800`.
     */
    public static Palette16 chatPrivate() {
        return chatPrivate;
    }

    /**
     * Native support extracted from chat delivery 1 palette pointer `PTR_chatAlliedPalette_005f1804`.
     */
    public static Palette16 chatAllied() {
        return chatAllied;
    }

    /**
     * Native support extracted from chat delivery 3 palette pointer `PTR_chatShoutPalette_005f1808`.
     */
    public static Palette16 chatShout() {
        return chatShout;
    }

    /**
     * Native support extracted from `g_pal16Colors[index] @0061FAC8`.
     */
    public static Palette16 unitOwnerTextPalette(int paletteIndex) {
        return unitPaletteComplements[paletteIndex];
    }

    /**
     * Native evidence: CUnit::Draw @004632A1 constructs a 16-page mode-5 palette from the unit type's raw palette.
     * Java reuses that immutable conversion by the stable raw {@link Palette256} identity.
     */
    public static CGamePalette unitGrayscalePalette(Palette256 rawPalette) {
        CGamePalette grayscalePalette = UNIT_GRAYSCALE_PALETTES.get(rawPalette);
        if (grayscalePalette == null) {
            grayscalePalette = new CGamePalette();
            grayscalePalette.init(rawPalette, UNIT_OWNER_PALETTE_COUNT, 5, 0);
            UNIT_GRAYSCALE_PALETTES.put(rawPalette, grayscalePalette);
        }
        return grayscalePalette;
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
     * `GamePaletteArray_units @00622960` and `g_pal16Colors @0061FAC8`.
     */
    public static void loadUnitOwnerPalettes() {
        unitGamePalettes.clear();
        Palette256[] rawPalettes = getOrLoadUnitOwnerPaletteSources();
        unitGamePalettes.addAll(UNIT_GAME_PALETTE_STORAGE);

        CGamePalette grayscalePalette = UNIT_GAME_PALETTE_STORAGE.get(UNIT_OWNER_PALETTE_COUNT);
        if (grayscalePalette.paletteData == null) {
            grayscalePalette.init(rawPalettes[0], UNIT_OWNER_PALETTE_COUNT, 5, 0);
        }

        int tintRed = Globals.lighting.tintR & 0xFF;
        int tintGreen = Globals.lighting.tintG & 0xFF;
        int tintBlue = Globals.lighting.tintB & 0xFF;
        int tintRecipe = (tintRed << 16) | (tintGreen << 8) | tintBlue;
        if (unitOwnerPaletteTintRecipe == tintRecipe) {
            return;
        }

        clearUnitPaletteComplements();
        for (int paletteIndex = 0; paletteIndex < UNIT_OWNER_PALETTE_COUNT; paletteIndex++) {
            CGamePalette gamePalette = UNIT_GAME_PALETTE_STORAGE.get(paletteIndex);
            gamePalette.init(rawPalettes[paletteIndex], UNIT_OWNER_PALETTE_COUNT, 2, 1);
            writeUnitPaletteComplement(gamePalette, unitPaletteComplements[paletteIndex]);
        }
        unitOwnerPaletteTintRecipe = tintRecipe;
    }

    /**
     * Native support extracted from GamePaletteArray_units cleanup in Global::releaseUnitTypeRuntimeGlobals @0047AF9D.
     */
    public static void releaseUnitOwnerGamePalettesForShutdown() {
        unitGamePalettes.clear();
    }

    /**
     * Native support extracted from the one-time `human.pal` read in UnitTypes::loadUnitTypes @00479B1E.
     */
    private static Palette256[] getOrLoadUnitOwnerPaletteSources() {
        if (unitOwnerPaletteSources == null) {
            ByteBuffer humanPaletteData = Globals.gameFileManager.get(HUMAN_UNIT_PALETTE_PATH)
                    .duplicate()
                    .order(ByteOrder.LITTLE_ENDIAN);
            Palette256[] rawPalettes = new Palette256[UNIT_OWNER_PALETTE_COUNT];
            for (int paletteIndex = 0; paletteIndex < UNIT_OWNER_PALETTE_COUNT; paletteIndex++) {
                rawPalettes[paletteIndex] = readUnitOwnerPalette(humanPaletteData, paletteIndex);
            }
            unitOwnerPaletteSources = rawPalettes;
        }
        return unitOwnerPaletteSources;
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
     * Native support extracted from zeroed storage backing `g_pal16Colors @0061FAC8`.
     */
    private static Palette16[] initUnitPaletteComplements() {
        Palette16[] palettes = new Palette16[UNIT_OWNER_PALETTE_COUNT];
        for (int paletteIndex = 0; paletteIndex < UNIT_OWNER_PALETTE_COUNT; paletteIndex++) {
            int[] data = new int[SIZE16];
            Arrays.fill(data, RGB32.BLACK);
            palettes[paletteIndex] = new Palette16(data);
        }
        return palettes;
    }

    /**
     * not ported. Clears Java's preallocated `g_pal16Colors @0061FAC8` mirror before rebuilding it.
     */
    private static void clearUnitPaletteComplements() {
        for (Palette16 unitPaletteComplement : unitPaletteComplements) {
            Arrays.fill(unitPaletteComplement.data(), RGB32.BLACK);
        }
    }

    /**
     * Native support extracted from the `g_pal16Colors[i1][j1][0..1]` writes in UnitTypes::loadUnitTypes @00479B1E.
     */
    private static void writeUnitPaletteComplement(CGamePalette gamePalette, Palette16 target) {
        for (int pairIndex = 0; pairIndex < 8; pairIndex++) {
            int color = gamePalette.paletteData[0x0F - pairIndex].data()[UNIT_OWNER_COMPLEMENT_COLOR_INDEX];
            target.data()[pairIndex * 2] = color;
            target.data()[pairIndex * 2 + 1] = color;
        }
    }

    /**
     * Native: Global::InitPalettes @0045EA70.
     * Java port status: fully ported through class initialization.
     */
    public static void ensureStaticLoad() {
    }
}
