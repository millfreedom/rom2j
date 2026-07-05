package ua.millfreedom.rom2.model;

import lombok.SneakyThrows;
import ua.millfreedom.rom2.model.palette.CGamePalette;
import ua.millfreedom.rom2.model.palette.Palette256;
import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.res.ResInHeap;
import ua.millfreedom.rom2.res.Resources;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import static ua.millfreedom.rom2.Globals.gameFileManager;
import static ua.millfreedom.rom2.res.Constants.*;

/**
 * Port of LoadProjectiles @0047CEE2 plus the recovered projectile resource helpers it uses.
 */
public final class Projectiles {

    /**
     * Native global: {@code g_CArray<CProjectileInfo> @00622948}. Static CArray lifetime glue:
     * initializer @004763AB, constructor @004763BA, atexit wrapper @004763C9, destructor @004763DB.
     */
    public static final CustomList<CProjectileInfo> PROJECTILES_BY_ID = new CustomList<>(CProjectileInfo.class);
    public static final String PALS_NAME = "projectiles.pal";
    public static final String PAL_NAME = "projectile_.pal";

    /**
     * DAT_00626d28 / DAT_00626d2c
     */
    public static CGamePalette PROJECTILES_PAL;
    public static CGamePalette PROJECTILE_PAL;

    /**
     * DAT_00622990[2]
     */
    public static final CA16[] SMOKE_SPRITES = new CA16[2];

    /**
     * Java utility constructor.
     * not ported.
     */
    private Projectiles() {
    }

    /**
     * Native: LoadProjectiles @0047CEE2.
     * Full port of registry-visible loader behavior. Native reloads entries by projectile ID without clearing the
     * existing CArray; cleanupProjectiles owns projectile registry clearing.
     */
    @SneakyThrows
    public static void loadProjectiles()  {
        ResInHeap projectilesReg = ResInHeap.load(GRAPHICS, PROJECTILES, PROJECTILES_REG);

        int count = projectilesReg.getInt(GLOBAL, COUNT, 0);

        for (int i = 0; i < count; i++) {
            String section = String.format(PROJECTILE_N, i);

            StringBuilder fileBuf = new StringBuilder(0x100);
            projectilesReg.getValueAsString(section, FILE, "", fileBuf, 0x100);
            String projectileFile = fileBuf.toString();

            boolean a16 = projectilesReg.getInt(section, A16, 0) != 0;

            // FUN_0047cabc(this, ProjectileFile, a16)
            CProjectileInfo prj = new CProjectileInfo(projectileFile, a16);

            prj.id = projectilesReg.getInt(section, ID, -1);
            prj.phases = projectilesReg.getInt(section, PHASES, -1);
            prj.rotationPhases = projectilesReg.getInt(section, ROTATION_PHASES, 0x10);
            prj.width = projectilesReg.getInt(section, WIDTH, 0x40);
            prj.height = projectilesReg.getInt(section, HEIGHT, 0x40);
            prj.palette = projectilesReg.getInt(section, PALETTE, 0);
            prj.homing = projectilesReg.getInt(section, HOMING, 0);
            prj.flip = projectilesReg.getInt(section, FLIP, 0);
            prj.sfx = projectilesReg.getInt(section, SFX, 0);

            // FUN_0047f410(CArray<>_00622948, id, prj)
            while (PROJECTILES_BY_ID.size() <= prj.id) {
                PROJECTILES_BY_ID.add(null);
            }
            PROJECTILES_BY_ID.set(prj.id, prj);
        }

        // projectiles.pal @ +0x36, size 0x400
        PROJECTILES_PAL = loadPalInto(Resources.path(GRAPHICS, PROJECTILES, PALS_NAME));

        // projectile_.pal @ +0x36, size 0x400
        PROJECTILE_PAL = loadPalInto(Resources.path(GRAPHICS, PROJECTILES, PAL_NAME));

        // smoke sprites
        for (int i = 0; i < 2; i++) {
            String smokePath = String.format(Resources.path(GRAPHICS, PROJECTILES, SMOKE_N, SPRITES_16A), i);
            CA16 smoke = new CA16(smokePath);
            SMOKE_SPRITES[i] = smoke;

            // CGameBitmap::InitPalette(smoke, 0x10, 4, 0)
            smoke.initPalette(0x10, 4, 0);
        }
    }

    /**
     * Native: cleanupProjectiles @0047D386.
     * Fully ported. Java clears the managed projectile registry, palettes, and smoke sprite globals.
     */
    public static void cleanupProjectiles() {
        PROJECTILES_BY_ID.clear();
        PROJECTILES_PAL = null;
        PROJECTILE_PAL = null;
        Arrays.fill(SMOKE_SPRITES, null);
    }

    /**
     * Native support extracted from LoadProjectiles @0047CEE2.
     * Replays the recovered `seek(0x36) -> read(0x400) -> CGamePalette::Init(...)` palette load.
     */
    private static CGamePalette loadPalInto(String path) {
        CGamePalette p = new CGamePalette();

        ByteBuffer bb = gameFileManager.get(path).slice(0x36, 0x400).order(ByteOrder.LITTLE_ENDIAN);
        Palette256 palette256 = Palette256.read(bb);
        // Port of: CGamePalette::Init(p, local_478, 0x10, 2, 0)
        p.init(palette256, 16, 2, 0);
        return p;
    }

}
