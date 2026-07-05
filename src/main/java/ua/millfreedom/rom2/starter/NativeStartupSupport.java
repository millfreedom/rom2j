package ua.millfreedom.rom2.starter;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.jthemedetecor.OsThemeDetector;
import ua.millfreedom.rom2.CTextFile;
import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.SkillProgression;
import ua.millfreedom.rom2.model.Projectiles;
import ua.millfreedom.rom2.model.Structures;
import ua.millfreedom.rom2.model.UnitTypes;
import ua.millfreedom.rom2.model.VObjects;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.world.TerrainGraphics;
import ua.millfreedom.rom2.text.TextTableId;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.nio.file.Path;

/**
 * Shared Java support for non-GL launchers that need the native startup resource subset.
 * not ported.
 */
public final class NativeStartupSupport {
    private static final TextTableId[] STARTUP_TEXT_TABLES = {
            TextTableId.MAIN,
            TextTableId.HEROPICTURE,
            TextTableId.STATS,
            TextTableId.SPELLS,
            TextTableId.SPELL,
            TextTableId.DIALOGS,
            TextTableId.UNITNAME,
            TextTableId.BUILDING,
            TextTableId.ITEMNAME,
            TextTableId.NPCNAMES,
            TextTableId.CUTSCENE,
            TextTableId.CUTPATHS,
            TextTableId.TUNES,
            TextTableId.PATCH
    };

    /**
     * Java utility constructor.
     * not ported.
     */
    private NativeStartupSupport() {
    }

    /**
     * Java support theme initialization for Swing utility launchers.
     * not ported.
     */
    public static void installSystemAwareSwingLookAndFeel() {
        OsThemeDetector detector = OsThemeDetector.getDetector();
        applyFlatLaf(detector.isDark());
        detector.registerListener(isDark -> SwingUtilities.invokeLater(() -> {
            applyFlatLaf(isDark);
            FlatLaf.updateUI();
        }));
    }

    /**
     * Java support FlatLaf theme selection for Swing utility launchers.
     * not ported.
     */
    private static void applyFlatLaf(boolean dark) {
        try {
            UIManager.setLookAndFeel(dark ? new FlatDarkLaf() : new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException exception) {
            exception.printStackTrace(System.err);
        }
    }

    /**
     * Native support extracted from the GetTempPathA/AddSearchPath block in CMainApp::InitInstance @0048100B.
     */
    public static void initializeNativeSearchPaths() {
        Globals.gameFileManager.addSearchPath(
                Path.of(System.getProperty("java.io.tmpdir")).toAbsolutePath().normalize().toString()
        );
    }

    /**
     * Native support extracted from the CMainApp::InitInstance @00480C8D resource loading sequence.
     */
    public static void loadNativeResourceArchives() {
        Globals.gameFileManager.loadNativePrimaryStartupResources();
        Globals.gameFileManager.loadNativeSecondaryStartupResources();
        Globals.gameFileManager.loadNativeUpdateList();
    }

    /**
     * Native support extracted from the help text load in CMainApp::InitInstance @00480C8D.
     */
    public static void loadHelpText() {
        Globals.helpText = CTextFile.loadTextFileToOemString("main/text/help.txt");
    }

    /**
     * Native support extracted from the CTextFile::LoadAndParse sequence in CMainApp::InitInstance @00480C8D.
     */
    public static void loadTextFiles() {
        for (TextTableId tableId : STARTUP_TEXT_TABLES) {
            CTextFile.LoadAndParse(tableId);
        }
    }

    /**
     * Native support extracted from CStaticDataMgr::LoadOrRebuild, ItemNames::loadItemNames,
     * and GameFonts::loadFonts calls in CMainApp::InitInstance @00480C8D.
     */
    public static void loadStaticStartupData() {
        int staticDataLoadResult = Globals.staticDataMgr.loadOrRebuild();
        if (staticDataLoadResult != 0 || !Globals.staticDataMgr.m_bLoaded) {
            throw new IllegalStateException("Static data manager did not load static data, result " + staticDataLoadResult);
        }
        Globals.itemNames.loadItemNames();
        Globals.fonts.loadFonts();
    }

    /**
     * Native support extracted from the scenario.dll initialization tail in CMainApp::InitInstance @00480C8D.
     */
    public static void initializeScenarioSupportTables() {
        SkillProgression.initializeNativeCompoundingTable();
        Palettes.setMessageColorsPalette(Globals.gamePreferences.messageColors);
    }

    /**
     * Native support extracted from ResetTerrainTileSet @00476A54 for editor preview startup.
     */
    public static void initializeHeadlessWindowRuntime() {
        TerrainGraphics.resetTerrainTileSet();
    }

    /**
     * Java support for loading terrain visual-object sprites used by the standalone MapEditor preview.
     * not ported.
     */
    public static void loadEditorObjectPreviewResources() {
        VObjects.loadVObjects();
    }

    /**
     * Java support for loading structure sprites used by the standalone MapEditor preview.
     * not ported.
     */
    public static void loadEditorStructurePreviewResources() {
        Structures.loadStructures();
    }

    /**
     * Java support for loading unit sprites used by the standalone MapEditor preview.
     * not ported.
     */
    public static void loadEditorUnitPreviewResources() {
        UnitTypes.loadUnitTypes();
    }

    /**
     * Java support for loading backpack sprites used by the standalone MapEditor preview.
     * not ported.
     */
    public static void loadEditorSackPreviewResources() {
        GUI.loadBackpackSpritesForEditorPreview();
    }

    /**
     * Java support for loading projectile sprites used by the standalone MapEditor effect preview.
     * not ported.
     */
    public static void loadEditorEffectPreviewResources() {
        Projectiles.loadProjectiles();
    }

    /**
     * Java support startup subset for editor tools that need resources, static data, and scenario support without
     * starting CMainApp's GLFW window or gameplay message loop.
     * not ported.
     */
    public static void initializeEditorNativeStartup() {
        initializeNativeSearchPaths();
        loadNativeResourceArchives();
        Globals.materialRuntimeData.loadMaterials();
        loadHelpText();
        loadTextFiles();
        loadStaticStartupData();
        initializeScenarioSupportTables();
        initializeHeadlessWindowRuntime();
        loadEditorObjectPreviewResources();
        loadEditorStructurePreviewResources();
        loadEditorUnitPreviewResources();
        loadEditorSackPreviewResources();
        loadEditorEffectPreviewResources();
    }
}
