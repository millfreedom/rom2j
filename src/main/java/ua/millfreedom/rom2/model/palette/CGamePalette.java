package ua.millfreedom.rom2.model.palette;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.color.RGB32;

import java.util.Arrays;

import static ua.millfreedom.rom2.Utils.join;
import static ua.millfreedom.rom2.model.color.Utils.clamp255;

/**
 * CGamePalette
 */
public class CGamePalette implements MfcSerializable {


    private static final int SIZE = 0x100;


    //0x04
    public int nPages;
    //0x08
    public Palette16[] paletteData;

    /**
     * Native: CGamePalette::CGamePalette @004232BF.
     */
    public CGamePalette() {
    }

    /**
     * Native: CGamePalette::CGamePalette @004232E8.
     */
    public CGamePalette(CGamePalette from) {
        nPages = from.nPages;
        paletteData = new Palette16[nPages];
        for (int i = 0; i < nPages; i++) {
            paletteData[i] = new Palette16(Arrays.copyOf(from.paletteData[i].data(), SIZE));
        }
    }

    /**
     * vtbl +0x08: CObject::Serialize @00401970.
     */
    @Override
    public void serialize(CArchive ar) {
        // Native CObject::Serialize is a no-op.
    }

    /**
     * Native: CGamePalette::Free @00423C5A.
     */
    public void free() {
        paletteData = null;
    }

    /**
     * Native: CGamePalette::Init @0042339E.
     */
    public void init(Palette256 pSource, int nPages, int nMode, int bApplyTint) {
        this.nPages = nPages;

        this.paletteData = new Palette16[nPages];
        for (int p = 0; p < nPages; p++) {
            this.paletteData[p] = new Palette16(new int[SIZE]);
        }

        int tR = Globals.lighting.tintR & 0xFF;
        int tG = Globals.lighting.tintG & 0xFF;
        int tB = Globals.lighting.tintB & 0xFF;
        if (bApplyTint == 0) {
            tR = tG = tB = 0;
        }

        final int[] src = pSource.data(); // expects 256 entries

        switch (nMode) {
            case 0: {
                int[] dst = paletteData[0].data();
                dst[0] = RGB32.BLACK;
                for (int i = 1; i < SIZE; i++) {
                    dst[i] = RGB32.WHITE;
                }
                break;
            }

            case 1: {
                int[] dst = paletteData[0].data();
                for (int i = 0; i < SIZE; i++) {
                    int color = src[i];
                    int r = clamp255(RGB32.r(color) + tR);
                    int g = clamp255(RGB32.g(color) + tG);
                    int b = clamp255(RGB32.b(color) + tB);
                    dst[i] = RGB32.from(r, g, b);
                }
                break;
            }

            case 2: {
                int outPage = 0;
                for (int page = this.nPages; page > 0; page--, outPage++) {
                    int[] dst = paletteData[outPage].data();
                    for (int i = 0; i < SIZE; i++) {
                        int color = src[i];
                        int r = clamp255(((RGB32.r(color) + tR) * page * 2) / this.nPages);
                        int g = clamp255(((RGB32.g(color) + tG) * page * 2) / this.nPages);
                        int b = clamp255(((RGB32.b(color) + tB) * page * 2) / this.nPages);
                        dst[i] = RGB32.from(r, g, b);
                    }
                }
                break;
            }

            case 3: {
                int outPage = 0;
                for (int page = this.nPages; page > 0; page--, outPage++) {
                    int[] dst = paletteData[outPage].data();
                    for (int i = 0; i < SIZE; i++) {
                        int color = src[i];
                        int r = clamp255(((RGB32.r(color) + tR) * page) / 32);
                        int g = clamp255(((RGB32.g(color) + tG) * page) / 32);
                        int b = clamp255(((RGB32.b(color) + tB) * page) / 32);
                        dst[i] = RGB32.from(r, g, b);
                    }
                }
                break;
            }

            case 4: {
                // Native normal-memory mode 4 generates direct brightness pages 1..16.
                for (int page = 1; page <= 16; page++) {
                    int[] dst = paletteData[page - 1].data();
                    for (int i = 0; i < SIZE; i++) {
                        dst[i] = RGB32.withBrightness(src[i], page);
                    }
                }
                break;
            }

            case 5: {
                int outPage = 0;
                for (int page = this.nPages; page > 0; page--, outPage++) {
                    int[] dst = paletteData[outPage].data();
                    for (int i = 0; i < SIZE; i++) {
                        int color = src[i];
                        int sum = RGB32.r(color) + RGB32.g(color) + RGB32.b(color);
                        int gray = (((sum * page * 2) / 3) / this.nPages);
                        if (gray > 0xFF) gray = 0xFF;
                        dst[i] = RGB32.from(gray, gray, gray);
                    }
                }
                break;
            }

            default:
                break;
        }
    }

    /**
     * not ported.
     */
    @Override
    public String toString() {
        return "CGamePalette{" +
                "nPages=" + nPages +
                ", paletteData=\n" + join("\n", paletteData) +
                '}';
    }
}
