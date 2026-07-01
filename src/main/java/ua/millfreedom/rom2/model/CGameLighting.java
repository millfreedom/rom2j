package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.Globals;

public class CGameLighting {

    // not ported. Java default lighting instance.
    public static CGameLighting standard = new CGameLighting();

    //0x00
    public int tintR = 0;
    //0x01
    public int tintG = 0;
    //0x02
    public int tintB = 0;
    //0x04
    public int brightness = 0xe;
    //0x08
    public int shadowContrast = 0x20;
    //0x0c
    public int shadowLength = 4;
    //0x10
    public int lightHeight = 2;
    //0x14
    public int unk;
    //0x18
    public double sunAngleRad = 0.78539815;

    // not ported.
    public CGameLighting(
            int tintR,
            int tintG,
            int tintB,
            int brightness,
            int shadowContrast,
            int shadowLength,
            int lightHeight,
            double sunAngleRad) {
        this.tintR = tintR;
        this.tintG = tintG;
        this.tintB = tintB;
        this.brightness = brightness;
        this.shadowContrast = shadowContrast;
        this.shadowLength = shadowLength;
        this.lightHeight = lightHeight;
        this.sunAngleRad = sunAngleRad;
    }

    // not ported.
    public CGameLighting() {

    }

    /**
     * Native support extracted from UpdateGlobalLighting @00474D98 assignments to g_GameLighting.
     */
    private void assign(
            int tintR,
            int tintG,
            int tintB,
            int brightness,
            int shadowContrast,
            int shadowLength,
            int lightHeight,
            double sunAngleRad) {
        this.tintR = tintR;
        this.tintG = tintG;
        this.tintB = tintB;
        this.brightness = brightness;
        this.shadowContrast = shadowContrast;
        this.shadowLength = shadowLength;
        this.lightHeight = lightHeight;
        this.sunAngleRad = sunAngleRad;
    }

    /**
     * Native: UpdateGlobalLighting @00474D98.
     * Fully ported.
     */
    public static void UpdateGlobalLighting(int timeVal) {
        if (Globals.gamePreferences.showTimeFlow == 0) {
            Globals.lighting.assign(
                    0,
                    0,
                    0,
                    0xe,
                    0x20,
                    4,
                    2,
                    0.78539815);
            return;
        }

        int hour = (timeVal / 60) % 24;
        if ((hour < 6) || (17 < hour)) {
            if ((hour < 2) || (21 < hour)) {
                Globals.lighting.assign(
                        0,
                        0xc,
                        0x30,
                        0x20,
                        8,
                        8,
                        4,
                        0.78539815 - (((double) ((timeVal + 0x78) % 0xf0) * 3.1415926) / 2.0) / 240.0);
            } else if ((hour < 2) || (3 < hour)) {
                if ((hour < 4) || (5 < hour)) {
                    if ((hour < 18) || (19 < hour)) {
                        if ((19 < hour) && (hour < 22)) {
                            Globals.lighting.assign(
                                    0x18 - (((timeVal % 0x78) * 0x18) / 0x78),
                                    ((timeVal % 0x78) * 0xc) / 0x78,
                                    (((timeVal % 0x78) * 0x28) / 0x78) + 8,
                                    (timeVal % 0x78 << 3) / 0x78 + 0x18,
                                    0x14 - ((timeVal % 0x78) * 0xc) / 0x78,
                                    6,
                                    3,
                                    0.78539815);
                        }
                    } else {
                        Globals.lighting.assign(
                                ((timeVal % 0x78) * 0x18) / 0x78,
                                0,
                                (timeVal % 0x78 << 3) / 0x78,
                                ((timeVal % 0x78) * 10) / 0x78 + 0xe,
                                0x20 - ((timeVal % 0x78) * 0xc) / 0x78,
                                6,
                                3,
                                0.78539815);
                    }
                } else {
                    Globals.lighting.assign(
                            0x18 - (((timeVal % 0x78) * 0x18) / 0x78),
                            0xc - (((timeVal % 0x78) * 0xc) / 0x78),
                            0,
                            0x1c - ((timeVal % 0x78) * 0xe) / 0x78,
                            ((timeVal % 0x78) * 0xc) / 0x78 + 0x14,
                            6,
                            3,
                            -0.78539815);
                }
            } else {
                Globals.lighting.assign(
                        ((timeVal % 0x78) * 0x18) / 0x78,
                        0xc,
                        0x30 - (((timeVal % 0x78) * 0x30) / 0x78),
                        0x20 - (timeVal % 0x78 << 2) / 0x78,
                        ((timeVal % 0x78) * 0xc) / 0x78 + 8,
                        6,
                        3,
                        -0.78539815);
            }
        } else {
            Globals.lighting.assign(
                    0,
                    0,
                    0,
                    0xe,
                    0x20,
                    4,
                    2,
                    (((double) ((timeVal + 0x168) % 0x2d0) * 3.1415926) / 2.0) / 720.0 + -0.78539815);
        }
    }
}
