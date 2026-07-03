package ua.millfreedom.rom2.model.visobj;

import lombok.Getter;
import ua.millfreedom.rom2.model.net.CBufferManager;
import ua.millfreedom.rom2.GUI;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.*;
import ua.millfreedom.rom2.model.action.*;
import ua.millfreedom.rom2.model.actiondata.ActionPayloads;
import ua.millfreedom.rom2.model.color.RGB16;
import ua.millfreedom.rom2.model.container.CustomList;
import ua.millfreedom.rom2.model.control.CGameListControl;
import ua.millfreedom.rom2.model.enums.GameActionId;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.enums.SpellId;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.gameobj.*;
import ua.millfreedom.rom2.model.net.CLlDriver;
import ua.millfreedom.rom2.model.palette.CGamePalette;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.quest.Quest;
import ua.millfreedom.rom2.model.sound.GameplayMusicSupport;
import ua.millfreedom.rom2.model.sound.Sound;
import ua.millfreedom.rom2.model.sound.SoundChannel;
import ua.millfreedom.rom2.model.sound.SoundManager;
import ua.millfreedom.rom2.model.sound.SoundSystem;
import ua.millfreedom.rom2.model.spell.*;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.model.window.MessageSystem;
import ua.millfreedom.rom2.model.world.MapDescriptor;
import ua.millfreedom.rom2.model.world.TerrainGraphics;
import ua.millfreedom.rom2.res.ResInHeap;
import ua.millfreedom.rom2.res.Resources;
import ua.millfreedom.rom2.text.MainText;
import ua.millfreedom.rom2.text.PatchText;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.List;

import static ua.millfreedom.rom2.model.enums.MessageCodes.*;
import static ua.millfreedom.rom2.model.enums.SfxSounds.AMBIENT_RIVER;
import static ua.millfreedom.rom2.model.enums.SfxSounds.CLICK04;
import static ua.millfreedom.rom2.model.enums.SfxSounds.IBOOK;
import static ua.millfreedom.rom2.model.enums.SfxSounds.MAGIC_FIREWALL;
import static ua.millfreedom.rom2.model.enums.SfxSounds.MCOMPLET;
import static ua.millfreedom.rom2.model.enums.SfxSounds.MFAILED;
import static ua.millfreedom.rom2.model.visobj.VirtualKeyCodes.*;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.MODAL_DIALOG;
import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.SHOP_DIALOG;
import static ua.millfreedom.rom2.model.window.windowproc.handlers.CMainWindowWindowProcSupport.readMessageInt;
import static ua.millfreedom.rom2.res.Constants.GRAPHICS;
import static ua.millfreedom.rom2.res.Constants.SCENARIO;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.*;
import static ua.millfreedom.rom2.text.TextTableId.MAIN;
import static ua.millfreedom.rom2.text.TextTableId.PATCH;

public final class MapVisualObject extends CVisualObject {
    public static final int NATIVE_SIZE = 0x49C8; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int MK_RBUTTON = 0x2;
    private static final int MAP_CAMERA_EDGE_TILES = 8;
    private static final int INPUT_MODE_SPELL_CAST = 5;
    private static final int ORDER_TYPE_CAST_SLOT_A = 9;
    private static final int ORDER_TYPE_CAST_SLOT_B = 10;
    private static final int BATTLE_PREFERENCE_WIMPY_MODE = 1;
    private static final int BATTLE_PREFERENCE_FORMATION_MODE = 2;
    private static final int BATTLE_PREFERENCE_AUTO_CASTING_MODE = 3;
    private static final int BATTLE_PREFERENCE_ALT_DEBUG_COMMAND = 0x80;
    private static final int AUTOCAST_MODE_MASK = GamePreferences.AUTOCAST_BASE
            | GamePreferences.AUTOCAST_MODE_AVERAGE
            | GamePreferences.AUTOCAST_MODE_MAXIMUM;
    private static final int OCCUPANCY_GRID_SIZE = 0x29;
    private static final int OCCUPANCY_GRID_CENTER = 0x14;
    private static final int PLAYER_KNOWLEDGE_TABLE_SIZE = 0xA00;
    private static final int TILE_PIXEL_SIZE = 0x100;
    private static final int TILE_SCREEN_SIZE = 0x20;
    private static final int TERRAIN_TILE_PIXELS = TILE_SCREEN_SIZE * TILE_SCREEN_SIZE;
    private static final int TERRAIN_TILE_INDEX_MASK = 0x1FFF;
    private static final int TERRAIN_TILE_VARIANT_MASK = 0x03;
    private static final int TERRAIN_TILE_VARIANT_SHIFT = 4;
    private static final int TERRAIN_TILE_FAMILY_SHIFT = 6;
    private static final int TERRAIN_TILE_FRAME_MASK = 0x0F;
    private static final int TERRAIN_DIRT_OVERLAY_MASK = 0x2000;
    private static final int TERRAIN_DEAD_VISUAL_OBJECT_MASK = 0x2000;
    private static final int TERRAIN_CURRENT_VISIBLE_MASK = 0x4000;
    private static final int TERRAIN_LIGHT_FULLY_BLOCKED_MASK = 0xC000;
    private static final int ANIMATED_TERRAIN_FIRST_TILE = 8;
    private static final int ANIMATED_TERRAIN_LAST_TILE = 0x0B;
    private static final int DYNAMIC_LIGHT_UNSET = 0xFF;
    private static final int DYNAMIC_LIGHT_NATIVE_OFFSET = 0x20;
    private static final int PROJECTILE_CENTER_OFFSET = 0x80;
    private static final int TERRAIN_LIGHTNING_PROJECTILE_ID = 0x0F;
    private static final int TERRAIN_MAGIC_LIGHT_PROJECTILE_ID = 0x15;
    private static final int TERRAIN_SPECIAL_LIGHT_PROJECTILE_ID = 0x2B;
    private static final int ANIMATED_WATER_EFFECT_PROJECTILE_ID = 0x08;
    private static final int TERRAIN_LIGHT_FLICKER_FLAG = 0x08;
    private static final int TERRAIN_LIGHT_MAGIC_FLAG = 0x40;
    private static final int TERRAIN_LIGHT_SPECIAL_FLAG = 0x20000;
    private static final int AMBIENT_SCAN_EDGE_MARGIN = 8;
    private static final int AMBIENT_SOUND_PRIORITY = 0xDC;
    private static final int AMBIENT_OBJECT_SOUND_DELAY_BASE_MS = 5000;
    private static final int SELECTION_RECT_MIN_AREA = 10;
    private static final int SELECTION_RECT_REFERENCE_WIDTH = 0x280;
    private static final int QUEST_WORD_COUNT = 15;
    private static final int PLAYER_SLOT_ACTIVE_FLAG = 0x1;
    private static final int ROLE_DIALOG_PORTRAIT_WIDTH = 0x58;
    private static final int ROLE_DIALOG_PORTRAIT_HEIGHT = 0x6C;
    private static final int INFO_PORTRAIT_WIDTH = 0xA0;
    private static final int INFO_PORTRAIT_HEIGHT = 0xF0;
    private static final int ROLE_DIALOG_PORTRAIT_DST_X = 8;
    private static final int ROLE_DIALOG_PORTRAIT_DST_Y = 7;
    private static final int ROLE_DIALOG_PORTRAIT_SRC_LEFT = 0x24;
    private static final int ROLE_DIALOG_PORTRAIT_SRC_TOP = 0x8C;
    private static final int ROLE_DIALOG_PORTRAIT_SRC_RIGHT = 0x6C;
    private static final int ROLE_DIALOG_PORTRAIT_SRC_BOTTOM = 0xE8;
    private static final int UNIT_FLAG_DYNAMIC_INFO_PICTURE_MASK = 0x11;
    private static final int ROLE_DIALOG_SPECIAL_PORTRAIT_TYPE_BASE = 0x40;
    private static final int TERRAIN_VERTEX_BLOCKED_MASK = 0xC000;
    private static final int TILE_SLOPE_BLOCKED = 0;
    private static final int TILE_SLOPE_DOWN = 0x08;
    private static final int TILE_SLOPE_UP = 0x10;
    private static final int MAP_LAYER_MISC = 0;
    private static final int MAP_LAYER_STRUCTURE = 1;
    private static final int MAP_LAYER_GROUND = 2;
    private static final int MAP_LAYER_AIR = 3;
    private static final int MAP_LAYER_INACTIVE = 4;
    private static final int RENDER_GRID_LEFT_GUARD = 3;
    private static final int RENDER_GRID_TOP_GUARD = 3;
    private static final int OBJECT_LAYER_EXTRA_COLUMNS = 6;
    private static final int OBJECT_LAYER_EXTRA_ROWS = 10;
    private static final int TERRAIN_VERTEX_EXTRA_COLUMNS = 7;
    private static final int TERRAIN_VERTEX_EXTRA_ROWS = 11;
    private static final int TILE_AVERAGE_EXTRA_COLUMNS = 8;
    private static final int TILE_AVERAGE_EXTRA_ROWS = 12;
    // Native: g_terrainDebugGridLines @00622800.
    private static int terrainDebugGridLines;
    /*
     * Java-only map zoom extension. This block is deliberately not modeled after the native renderer: the native game
     * derives gridWidth/gridHeight directly from 32-pixel screen cells, while the Java port now renders the map into a
     * logical 32-pixel-cell framebuffer and scales that framebuffer into the actual screen viewport. Do not "fix" these
     * constants back to native behavior just because the native code has no zoom concept; they are intentional Java UX
     * configuration. Zoom preserves the physical map viewport aspect ratio by deriving columns from the configured row
     * count and the current MapVisualObject rectangle.
     */
    private static final int JAVA_MAP_ZOOM_MIN = -5;
    private static final int JAVA_MAP_ZOOM_MAX = 5;
    private static final int JAVA_MAP_ZOOM_INITIAL = 0;
    private static final int JAVA_MAP_ZOOM_MIN_VISIBLE_ROWS = 20;
    private static final int[][] ROLE_DIALOG_SPECIAL_PORTRAIT_POINTS = {
            {30, 72}, {26, 23}, {10, 44}, {-1, -1}, {15, 21}, {32, 21}, {-1, -1}, {7, 49},
            {-1, -1}, {-1, -1}, {8, 72}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {30, 72},
            {23, 31}, {43, 7}, {37, 28}, {37, 28}, {37, 28}, {37, 28}, {37, 28}, {37, 28},
            {37, 28}, {37, 24}, {37, 24}, {37, 24}, {37, 24}, {37, 15}, {37, 15}, {37, 15},
            {37, 15}, {35, 15}, {35, 15}, {35, 14}, {35, 21}, {32, 18}, {15, 21}
    };
    private static final String INFOWINDOW_DIRECTORY = "infowindow";
    private static final String BMP_SUFFIX = ".bmp";

    public static final int FLAG_UNIT = 0x1;
    public static final int FLAG_AIR = 0x2;
    public static final int FLAG_BUSY = 0x4;
    public static final int FLAG_AUTOMATIC_CAST = 0x8;
    public static final int FLAG_STRUCTURE = 0x20;
    public static final int FLAG_HAS_SPELLS = 0x200;
    private static final int MAP_TARGET_UNIT = 0x1;
    private static final int MAP_TARGET_AIR = 0x2;
    private static final int MAP_TARGET_ENEMY = 0x4;
    private static final int MAP_TARGET_STRUCTURE = 0x20;
    private static final int MAP_TARGET_OCCUPIED_MISC = 0x40;
    private static final int MAP_TARGET_BLOCKED_TERRAIN = 0x400;
    private static final int MAP_TARGET_USABLE_STRUCTURE = 0x800;
    private static final int MAP_TARGET_SELECTABLE_OBJECT = MAP_TARGET_UNIT | MAP_TARGET_AIR | MAP_TARGET_STRUCTURE;
    private static final int UNIT_FLAG_HUMANOID = 0x01;
    private static final int NOT_ENOUGH_JOINED_PLAYERS_STATUS_WORD = 0x1007;
    private static final int PLAYER_JOIN_TIMEOUT_STATUS_WORD = 0x1009;
    private static final int MULTIPLAYER_BOOTSTRAP_STATUS_MAIN_TEXT_BASE = 0xC0;
    private static final int AFTER_NUMBER_KEYS = 0x3A;
    private static final int PLAYER_SLOT_FUNCTION_KEY_BASE = VK_F4;
    private static final int PLAYER_SLOT_TYPE_SPELL = 1;
    private static final int PLAYER_SLOT_TYPE_INVENTORY = 2;

    private static final int EFFECT_COUNT = StatsFromEffects.EFFECT_COUNT;

    // Native global g_Spell_IDs @005F8124 consumed by SpellPanelVisualObject::OnKeyDown @004C74B1.
    private static final byte[] DEFAULT_SPELL_IDS = {
            1, 2, 3, 4, 24, 20, 21, 26, 13, 12, 11, 10,
            5, 6, 7, 8, 25, 22, 23, 27, 19, 18, 17, 16
    };

    // MapVisualObject_Base +0x9CC / MapVisualObject +0x9D0 (native `m_ObjectMap`; Java stores the recovered object map directly).
    private final Map<Short, CGameObject> objects;

    // MapVisualObject_Base +0x9E0 / MapVisualObject +0x9E4 (native `m_ObjectMap2` transient render object map).
    private final Map<Short, CGameObject> transientObjects = new HashMap<>();

    // MapVisualObject_Base +0x150 / MapVisualObject +0x154.
    @Getter
    private final StatModifiers statModifiers;

    // Native global g_Spell_IDs @005F8124.
    private final byte[] spellIds;

    // MapVisualObject_Base +0x58 / MapVisualObject +0x5C.
    public final Point view = new Point();

    // MapVisualObject_Base +0x60 / MapVisualObject +0x64.
    public int gridWidth;

    // MapVisualObject_Base +0x64 / MapVisualObject +0x68.
    public int gridHeight;

    // not ported. Java-only zoom state: higher values show more map rows while preserving viewport aspect ratio.
    private int javaMapZoom = JAVA_MAP_ZOOM_INITIAL;

    // not ported. Java-only max-zoom allocation width; render grids are sized for this, not for the current zoom.
    private int allocatedGridWidth;

    // not ported. Java-only max-zoom allocation height; render grids are sized for this, not for the current zoom.
    private int allocatedGridHeight;

    // not ported. Java-only logical BGRA framebuffer used to draw 32-pixel map cells before scaling to the screen.
    private byte[] javaZoomMapFrameBgra = new byte[0];

    // not ported. Java-only width of javaZoomMapFrameBgra in pixels.
    private int javaZoomMapFrameWidth;

    // not ported. Java-only height of javaZoomMapFrameBgra in pixels.
    private int javaZoomMapFrameHeight;

    // MapVisualObject_Base +0x68 / MapVisualObject +0x6C.
    public int objectLayerGridBytes;

    // MapVisualObject_Base +0x6C / MapVisualObject +0x70.
    public int terrainVertexGridBytes;

    // MapVisualObject_Base +0x70 / MapVisualObject +0x74.
    public int renderFrameDirty;

    // not ported. Java-only guard so render frames do not repeat a full lighting refresh for the same game minute.
    private int lastTimeFlowLightingTimeValue = Integer.MIN_VALUE;

    // MapVisualObject_Base +0x74 / MapVisualObject +0x78.
    public int pendingCameraDeltaX;

    // MapVisualObject_Base +0x78 / MapVisualObject +0x7C.
    public int pendingCameraDeltaY;

    // MapVisualObject_Base +0x7C / MapVisualObject +0x80.
    public MapDescriptor mapDescriptor;

    // MapVisualObject_Base +0x80 / MapVisualObject +0x84.
    public int cachedMapWidth;

    // MapVisualObject_Base +0x84 / MapVisualObject +0x88.
    public int cachedMapHeight;

    // MapVisualObject_Base +0x88 / MapVisualObject +0x8C.
    public CGameObject[][] groundObjectLayer = new CGameObject[0][0];

    // MapVisualObject_Base +0x8C / MapVisualObject +0x90.
    public CGameObject[][] airObjectLayer = new CGameObject[0][0];

    // MapVisualObject_Base +0x90 / MapVisualObject +0x94.
    public CGameObject[][] structureObjectLayer = new CGameObject[0][0];

    // MapVisualObject_Base +0x94 / MapVisualObject +0x98.
    public CGameObject[][] miscObjectLayer = new CGameObject[0][0];

    // MapVisualObject_Base +0x98 / MapVisualObject +0x9C.
    public CGameObject[][] inactiveUnitLayer = new CGameObject[0][0];

    // MapVisualObject_Base +0x9C / MapVisualObject +0xA0.
    public int[][] tileSlopeModeGrid = new int[0][0];

    // MapVisualObject_Base +0xA0 / MapVisualObject +0xA4.
    public int[][] previousTileSlopeModeGrid = new int[0][0];

    // MapVisualObject_Base +0xA4 / MapVisualObject +0xA8.
    public byte[][] dynamicLightOverrideGrid = new byte[0][0];

    // MapVisualObject_Base +0xA8 / MapVisualObject +0xAC.
    public byte[][] previousDynamicLightOverrideGrid = new byte[0][0];

    // MapVisualObject_Base +0xAC / MapVisualObject +0xB0.
    public byte[][] tileBrightnessGrid = new byte[0][0];

    // MapVisualObject_Base +0xB0 / MapVisualObject +0xB4.
    public int[][] screenYVertexGrid = new int[0][0];

    // MapVisualObject_Base +0xB4 / MapVisualObject +0xB8.
    public int[][] screenYRowTopGrid = new int[0][0];

    // MapVisualObject_Base +0xB8 / MapVisualObject +0xBC.
    public int[][] screenYRowBottomGrid = new int[0][0];

    // MapVisualObject_Base +0xBC / MapVisualObject +0xC0.
    public int[][] tileAverageHeightGrid = new int[0][0];

    // MapVisualObject_Base +0xC0 / MapVisualObject +0xC4.
    public int renderStatsFrameCount;

    // MapVisualObject_Base +0xC4 / MapVisualObject +0xC8.
    public int renderStatsElapsedMillis;

    // MapVisualObject_Base +0xC8 / MapVisualObject +0xCC.
    public int renderStatsLastTick;

    // MapVisualObject_Base +0xCC / MapVisualObject +0xD0.
    public double renderStatsFps;

    // MapVisualObject_Base +0xDC / MapVisualObject +0xE0.
    public int areaEffectRefreshPending;

    // MapVisualObject_Base +0xE0 / MapVisualObject +0xE4.
    public int renderRingOriginX;

    // MapVisualObject_Base +0xE4 / MapVisualObject +0xE8.
    public int renderRingOriginY;

    // MapVisualObject_Base +0xE8 / MapVisualObject +0xEC.
    public int lastRenderedViewX;

    // MapVisualObject_Base +0xEC / MapVisualObject +0xF0.
    public int lastRenderedViewY;

    // MapVisualObject_Base +0xF0 / MapVisualObject +0xF4.
    public final CRect dirtyRenderRect = new CRect();

    // MapVisualObject_Base +0xABC / MapVisualObject +0xAC0.
    public final byte[][] visibilityStepXGrid = new byte[OCCUPANCY_GRID_SIZE][OCCUPANCY_GRID_SIZE];

    // MapVisualObject_Base +0xABD / MapVisualObject +0xAC1.
    public final byte[][] visibilityStepYGrid = new byte[OCCUPANCY_GRID_SIZE][OCCUPANCY_GRID_SIZE];

    // MapVisualObject_Base +0xD8 / MapVisualObject +0xDC.
    public int mapOccupancyDirty;

    // MapVisualObject_Base +0x104 / MapVisualObject +0x108.
    public int dynamicLightCellCount;

    // MapVisualObject_Base +0x108 / MapVisualObject +0x10C (native `objectSelectionRects`; Java keeps the active prefix).
    public final List<CRect> objectSelectionRects = new ArrayList<>();

    // MapVisualObject_Base +0x11C / MapVisualObject +0x120 (native `objectSelectionIds`; Java keeps the active prefix).
    public final List<Integer> objectSelectionIds = new ArrayList<>();

    // MapVisualObject_Base +0x130 / MapVisualObject +0x134.
    public int objectSelectionCount;

    // MapVisualObject_Base +0x3F68 / MapVisualObject +0x3F6C.
    private CUnit pCUnit;

    // MapVisualObject_Base +0x9B0 / MapVisualObject +0x9B4.
    public int inputMode;

    // MapVisualObject_Base +0x9C8 / MapVisualObject +0x9CC.
    public CPlayer currentPlayer;

    // MapVisualObject +0xA28.
    public final CGameListControl gameListControl = new CGameListControl();

    // MapVisualObject_Base +0x9B4 / MapVisualObject +0x9B8.
    public final CustomList<CPlayer> clientPlayers = new CustomList<>(CPlayer.class);

    // MapVisualObject_Base +0xA20 / MapVisualObject +0xA24.
    public short nextTransientObjectToken;

    // MapVisualObject_Base +0xA04 / MapVisualObject +0xA08 (native `map3`; packed tile key to terrain-light override flags).
    public final Map<Integer, Integer> terrainLightOverrideCells = new HashMap<>();

    // MapVisualObject_Base +0xA90 / MapVisualObject +0xA94 (native `map4`; packed tile key to transient effect phase).
    public final Map<Integer, Integer> transientEffectCells = new HashMap<>();

    // MapVisualObject_Base +0xA84 / MapVisualObject +0xA88.
    public int mapAnimationTick;

    // MapVisualObject_Base +0xA88 / MapVisualObject +0xA8C.
    public int lastAnimatedTerrainTick;

    // MapVisualObject_Base +0xA8C / MapVisualObject +0xA90.
    public int renderFrameCounter;

    // MapVisualObject_Base +0x100 / MapVisualObject +0x104.
    public int lastPanelLayoutSignature;

    // MapVisualObject_Base +0x3F50 / MapVisualObject +0x3F54 (native `CArray<FloatingUnitText>`).
    private final List<FloatingUnitText> floatingUnitTexts = new ArrayList<>();

    // MapVisualObject_Base +0xAAC / MapVisualObject +0xAB0.
    public int formationMode;

    // MapVisualObject_Base +0xAB0 / MapVisualObject +0xAB4.
    public int wimpyMode;

    // MapVisualObject_Base +0xAB4 / MapVisualObject +0xAB8.
    public int showHitPointBars;

    // MapVisualObject_Base +0xAB8 / MapVisualObject +0xABC.
    public int showFlyingHitPointBars;

    // MapVisualObject_Base +0x3224 / MapVisualObject +0x3228.
    public final short[][] visibilityDistanceCorrectionGrid = new short[OCCUPANCY_GRID_SIZE][OCCUPANCY_GRID_SIZE];

    // MapVisualObject_Base +0x3F48 / MapVisualObject +0x3F4C.
    public float visibilityScale;

    // MapVisualObject_Base +0x3F4C / MapVisualObject +0x3F50.
    public int visibilityScaleShift;

    // MapVisualObject_Base +0x3F64 / MapVisualObject +0x3F68.
    public final CBmp256 m_CBmp256;

    // MapVisualObject_Base +0x3F6C / MapVisualObject +0x3F70.
    public final byte[] playerKnowledgeTable = new byte[PLAYER_KNOWLEDGE_TABLE_SIZE];

    // MapVisualObject_Base +0x4970 / MapVisualObject +0x4974.
    public final List<TokenEntry> dialogItemTokenEntries = new ArrayList<>();

    // MapVisualObject_Base +0x4970 / MapVisualObject +0x4974 Java mirror for the native CArray<> Item* use.
    public final List<Item> savedCharacterInventoryItems = new ArrayList<>();

    // MapVisualObject_Base +0xD4 / MapVisualObject +0xD8.
    public int cameraDragMoved;

    // MapVisualObject_Base +0x49B4 / MapVisualObject +0x49B8.
    public int ambientAudioViewX;

    // MapVisualObject_Base +0x49B8 / MapVisualObject +0x49BC.
    public int ambientAudioViewY;

    // MapVisualObject_Base +0x49BC / MapVisualObject +0x49C0.
    public int nextAmbientObjectSoundTick;

    // MapVisualObject_Base +0x496C / MapVisualObject +0x4970.
    public final QuestsStorage questStorage = new QuestsStorage();

    // MapVisualObject_Base +0x4984 / MapVisualObject +0x4988.
    public final Item[] savedCharacterEquipmentItems = new Item[CGameSession.SAVED_CHARACTER_ITEM_SLOT_COUNT];

    // MapVisualObject_Base +0x13C / MapVisualObject +0x140.
    @Getter
    private int selectedCount;

    // MapVisualObject_Base +0x144 / MapVisualObject +0x148.
    @Getter
    private int selectedAvailableSpellMask;

    // MapVisualObject_Base +0x148 / MapVisualObject +0x14C.
    @Getter
    private int autoCastSpellbookMask;

    // MapVisualObject_Base +0x14C / MapVisualObject +0x150.
    @Getter
    private int activeSpellEffectMask;

    // MapVisualObject_Base +0x140 / MapVisualObject +0x144.
    @Getter
    private int selectionFlags;

    // Native support for MapVisualObject_Base +0x138 / MapVisualObject +0x13C primarySelectedAssoc; Java stores the map key instead.
    private short primarySelectedObjectToken;

    // MapVisualObject_Base +0x134 / MapVisualObject +0x138.
    @Getter
    private CGameObject primarySelectedObject;

    // MapVisualObject_Base +0x990 / MapVisualObject +0x994.
    private CGameObject hoveredObject;

    // MapVisualObject_Base +0x994 / MapVisualObject +0x998.
    private int hoveredPrimaryQuestKey;

    // MapVisualObject_Base +0x998 / MapVisualObject +0x99C.
    private int hoveredSecondaryQuestKey;

    // MapVisualObject_Base +0x99C / MapVisualObject +0x9A0.
    private int hoveredMapQuestKey;

    // MapVisualObject_Base +0x9A0 / MapVisualObject +0x9A4.
    private int hoveredUnitActionQuestKey;

    // MapVisualObject_Base +0x9A4 / MapVisualObject +0x9A8.
    private short hoveredObjectToken;

    // MapVisualObject_Base +0x9A8 / MapVisualObject +0x9AC.
    private int hoveredTileX;

    // MapVisualObject_Base +0x9AC / MapVisualObject +0x9B0.
    private int hoveredTileY;

    /**
     * Java convenience overload.
     * not ported.
     */
    public MapVisualObject(Map<Short, CGameObject> objects, StatModifiers statModifiers, byte[] spellIds) {
        this(0, 0, 0, 0, objects, statModifiers, spellIds);
    }

    /**
     * Native: MapVisualObject::MapVisualObject @00402AF6.
     * Fully ported. Java-only parameters inject object, stat, and spell registries that native owns externally.
     */
    public MapVisualObject(int xLeft, int yTop, int xRight, int yBottom,
                           Map<Short, CGameObject> objects, StatModifiers statModifiers, byte[] spellIds) {
        super(1, xLeft, yTop, xRight, yBottom, null);
        this.objects = objects == null ? new HashMap<>() : objects;
        this.statModifiers = Objects.requireNonNullElseGet(statModifiers, StatModifiers::new);
        this.spellIds = spellIds == null ? DEFAULT_SPELL_IDS.clone() : spellIds.clone();

        recalculateGridMetrics();

        view.x = MAP_CAMERA_EDGE_TILES;
        view.y = MAP_CAMERA_EDGE_TILES;
        renderStatsFrameCount = 0;
        renderStatsElapsedMillis = 0;
        renderStatsLastTick = 0;
        renderStatsFps = 0.0;
        objectSelectionCount = 0;
        inputMode = 0;
        mapAnimationTick = 0;
        lastAnimatedTerrainTick = 0;
        renderFrameCounter = 0;
        cameraDragMoved = 0;
        mapOccupancyDirty = 1;
        areaEffectRefreshPending = 1;
        renderRingOriginX = 0;
        renderRingOriginY = 0;
        lastRenderedViewX = -1;
        lastRenderedViewY = -1;
        lastPanelLayoutSignature = 1;
        formationMode = 1;
        wimpyMode = 0;
        showHitPointBars = 0;
        showFlyingHitPointBars = 1;
        m_CBmp256 = new CBmp256(0x20, 0x20);
        visibilityScaleShift = 7;
        visibilityScale = 1 << (visibilityScaleShift & 0x1F);
        initializeVisibilityGrids();
        ambientAudioViewX = 40000;
        ambientAudioViewY = 40000;
        nextAmbientObjectSoundTick = 0;
        clientPlayers.add(createDefaultClientPlayer());
    }

    /**
     * Native: MapVisualObject::InitializeVisibilityGrids @00403730.
     * Fully ported.
     */
    private void initializeVisibilityGrids() {
        int visibilityScaleInt = (int) visibilityScale;
        for (int gridX = 0; gridX < OCCUPANCY_GRID_SIZE; gridX++) {
            int deltaX = gridX - OCCUPANCY_GRID_CENTER;
            int absX = Math.abs(deltaX);
            for (int gridY = 0; gridY < OCCUPANCY_GRID_SIZE; gridY++) {
                int deltaY = gridY - OCCUPANCY_GRID_CENTER;
                int absY = Math.abs(deltaY);
                visibilityStepXGrid[gridX][gridY] = visibilityStepX(absX, absY, deltaX);
                visibilityStepYGrid[gridX][gridY] = visibilityStepY(absX, absY, deltaY);
                visibilityDistanceCorrectionGrid[gridX][gridY] =
                        visibilityDistanceCorrection(absX, absY, visibilityScaleInt);
            }
        }
        visibilityStepXGrid[OCCUPANCY_GRID_CENTER + 1][OCCUPANCY_GRID_CENTER] = (byte) 0xFF;
        visibilityStepYGrid[OCCUPANCY_GRID_CENTER + 1][OCCUPANCY_GRID_CENTER] = 0;
        visibilityStepXGrid[OCCUPANCY_GRID_CENTER - 1][OCCUPANCY_GRID_CENTER] = 1;
        visibilityStepYGrid[OCCUPANCY_GRID_CENTER - 1][OCCUPANCY_GRID_CENTER] = 0;
    }

    /**
     * Native support extracted from MapVisualObject::Init @00403730 visibility direction table.
     */
    private static byte visibilityStepX(int absX, int absY, int deltaX) {
        if (deltaX == 0) {
            return 0;
        }
        if (absY < (absX >> 1) || !((absX << 1) < absY)) {
            return (byte) -Integer.signum(deltaX);
        }
        return 0;
    }

    /**
     * Native support extracted from MapVisualObject::Init @00403730 visibility direction table.
     */
    private static byte visibilityStepY(int absX, int absY, int deltaY) {
        if (deltaY == 0) {
            return 0;
        }
        if ((absX << 1) < absY || !(absY < (absX >> 1))) {
            return (byte) -Integer.signum(deltaY);
        }
        return 0;
    }

    /**
     * Native support extracted from MapVisualObject::Init @00403730 visibility distance correction writes.
     */
    private static short visibilityDistanceCorrection(int absX, int absY, int visibilityScale) {
        int denominator = Math.max(absX, absY);
        if (denominator == 0) {
            return 0;
        }
        return (short) ((int) (Math.sqrt(absX * absX + absY * absY) * visibilityScale / denominator));
    }

    /**
     * vtbl +0x2C: MapVisualObject::Update @00406EE2.
     * Fully ported.
     */
    @Override
    public void update() {
        renderFrameDirty = 1;
        renderFrame();
    }

    /**
     * vtbl +0x48: MapVisualObject::OnMessage @0040C1D0.
     * Fully ported.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        int result = super.onMessage(msg, wParam, lParam);
        if (result != 0) {
            return result;
        }
        int w = readMessageInt(wParam);
        int l = readMessageInt(lParam);
        return switch (msg) {
            case INITIALIZE_UI -> {
                initializeUiFrame();
                yield result;
            }
            case RENDER_FRAME -> {
                renderFrame();
                yield result;
            }
            case REFRESH_LAYOUT -> {
                refreshLayoutAfterAction();
                yield 1;
            }
            case SET_CAMERA_POS -> {
                setCameraPosition(w, l);
                yield 1;
            }
            case EXECUTE_ORDER -> {
                SoundManager.playSfx(CLICK04);
                executeOrderType(w + 1);
                yield 1;
            }
            case TOGGLE_SELECTION_PANEL -> {
                toggleSelectionPanel();
                yield 1;
            }
            case TOGGLE_SPELL_PANEL -> {
                toggleSpellPanel();
                yield 1;
            }
            case RESET_SELECTION_GRID -> {
                if (GAMEPLAY.isSetIn(Globals.mainWindow.dialogsMask)) {
                    ambientAudioViewX = -1;
                    ambientAudioViewY = -1;
                    refreshAmbientAudio();
                    yield 0;
                }
                yield 0;
            }
            default -> result;
        };
    }

    /**
     * vtbl +0x4C: MapVisualObject::OnMouseMove @0040C7DD.
     * Fully ported.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        int previousMouseX = Globals.mousePointer.getX();
        int previousMouseY = Globals.mousePointer.getY();
        if ((nFlags & MK_RBUTTON) != 0) {
            int dx = x - previousMouseX;
            int dy = y - previousMouseY;
            int stepX = (dx + ((dx >> 31) & 7)) >> 3;
            int stepY = (dy + ((dy >> 31) & 7)) >> 3;

            if (dx != 0 || dy != 0) {
                if (stepX != 0 || stepY != 0) {
                    onMessage(SET_CAMERA_POS, view.x + stepX, view.y + stepY);
                    cameraDragMoved = 1;
                }
                return 1;
            }
        }

        return super.onMouseMove(nFlags, x, y);
    }

    /**
     * Java-only mouse-wheel binding for MapVisualObject zoom. This is not native behavior; wheel-up maps to zoomIn and
     * wheel-down maps to zoomOut for the Java-only logical-map framebuffer scaling layer.
     * not ported.
     */
    @Override
    public int onMouseWheel(int nFlagsAndDelta, int x, int y) {
        if (!isJavaZoomViewportPoint(x, y)) {
            return 0;
        }

        int wheelDelta = (short) ((nFlagsAndDelta >>> 16) & 0xFFFF);
        if (wheelDelta > 0) {
            zoomIn();
            return 1;
        }
        if (wheelDelta < 0) {
            zoomOut();
            return 1;
        }
        return 0;
    }

    /**
     * vtbl +0x54: MapVisualObject::OnLButtonDown @0040C4AE.
     * Fully ported.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        if (GAMEPLAY.isUnsetIn(Globals.mainWindow.dialogsMask)) {
            return 0;
        }

        if (!Globals.mousePointer.isSelecting() && mapDescriptor != null) {
            Globals.mainWindow.clipCursorToMapViewport();
            Globals.mousePointer.startSelectionDrag(x, y);
        }
        return 1;
    }

    /**
     * vtbl +0x58: MapVisualObject::OnLButtonUp @0040C5D1.
     * Fully ported.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        if (GAMEPLAY.isUnsetIn(Globals.mainWindow.dialogsMask)) {
            return 0;
        }

        if (Globals.mousePointer.isSelecting()) {
            Globals.mousePointer.finishSelectionDrag();
            Globals.mainWindow.clipCursorTo();
            selectCursor();
            updateMouseCursorAt(x, y);
        }
        completeMapUiLockDrop();
        return 1;
    }

    /**
     * Native support extracted from MapVisualObject::OnLButtonUp @0040C5D1.
     * Full port of carried inventory/gold release onto the world map.
     */
    private void completeMapUiLockDrop() {
        CMainWindow mainWindow = Globals.mainWindow;
        Object payload = mainWindow.getUiLockPayload();
        if (payload == null) {
            return;
        }

        if (selectedCount == 1 && primarySelectedObject != null) {
            int targetX = primarySelectedObject.tileX;
            int targetY = primarySelectedObject.tileY;
            if (hoveredObject instanceof CUnit hoveredUnit
                    && (hoveredUnit.phase & 1) != 0
                    && Math.abs(hoveredUnit.tileX - primarySelectedObject.tileX) < 3
                    && Math.abs(hoveredUnit.tileY - primarySelectedObject.tileY) < 3) {
                targetX = hoveredUnit.tileX;
                targetY = hoveredUnit.tileY;
            }

            TokenEntry tokenEntry = (TokenEntry) payload;
            int packedTargetCell = (targetX & 0xFF) | ((targetY & 0xFF) << 8);
            if (tokenEntry.isMoneyEntry()) {
                sendDropGoldAction(tokenEntry.quantity, packedTargetCell);
            } else {
                mainWindow.onGridOverlayDropCommitted(
                        mainWindow.getUiLockPackedModeCode(),
                        mainWindow.getUiLockSourceIndex(),
                        3,
                        packedTargetCell,
                        tokenEntry.quantity
                );
            }
        }
        CMousePointer.Cursor_Default.setToMousePointer();
        mainWindow.clearUiLockState();
    }

    /**
     * vtbl +0x5C: MapVisualObject::OnLButtonDblClk @0040C5AD.
     * Fully ported.
     */
    @Override
    public int onLButtonDblClk(int nFlags, int x, int y) {
        return onLButtonDown(nFlags, x, y);
    }

    /**
     * vtbl +0x60: MapVisualObject::OnRButtonDown @0040C50A.
     * Fully ported.
     */
    @Override
    public int onRButtonDown(int nFlags, int x, int y) {
        if (checkStateFlag(0x8) == 0) {
            setVisible(1);
        }
        return 1;
    }

    /**
     * vtbl +0x64: MapVisualObject::OnRButtonUp @0040C53A.
     * Fully ported.
     */
    @Override
    public int onRButtonUp(int nFlags, int x, int y) {
        if (cameraDragMoved == 0) {
            if (GAMEPLAY.isSetIn(Globals.mainWindow.dialogsMask)) {
                onMessage(REFRESH_LAYOUT, 0, 0);
            }
        }
        cameraDragMoved = 0;

        if (checkStateFlag(0x8) != 0) {
            setVisible(0);
        }
        return 1;
    }

    /**
     * vtbl +0x6C: MapVisualObject::OnKeyDown @0040C8A0.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        CMainWindow mainWindow = Globals.mainWindow;
        if (GAMEPLAY.isSetIn(mainWindow.dialogsMask) && mapDescriptor != null) {
            if (mainWindow.chatOpen != 0) {
                mainWindow.pChatVisualObject.onKeyDown(nChar);
                return 0;
            }
            if (VK_HELP < nChar && nChar < AFTER_NUMBER_KEYS) {
                int group = nChar - VK_0;
                if (!Globals.controlKeyDown) {
                    if (!Globals.altKeyDown) {
                        selectGroup(group);
                    } else {
                        jumpToGroup(group);
                    }
                } else if (!Globals.shiftKeyDown) {
                    setGroup(group);
                } else {
                    addToGroup(group);
                }
                return 1;
            }
            if (!Globals.controlKeyDown && selectedCount != 0 && (selectionFlags & (FLAG_BUSY | FLAG_STRUCTURE)) == 0) {
                switch (nChar) {
                    case VK_A -> {
                        executeOrderType(1);
                        return 1;
                    }
                    case VK_C -> {
                        if (!hasSpellPanelChild()) {
                            executeOrderType(5);
                        }
                        return 1;
                    }
                    case VK_D -> {
                        executeOrderType(4);
                        return 1;
                    }
                    case VK_G -> {
                        executeOrderType(3);
                        return 1;
                    }
                    case VK_L -> {
                        issueStandStillOrder();
                        return 1;
                    }
                    case VK_M -> {
                        executeOrderType(2);
                        return 1;
                    }
                    case VK_P -> {
                        issuePickupAllSacksAction();
                        return 1;
                    }
                    case VK_R -> {
                        executeOrderType(8);
                        return 1;
                    }
                    case VK_S -> {
                        executeOrderType(6);
                        return 1;
                    }
                    case VK_T -> {
                        executeOrderType(7);
                        return 1;
                    }
                    default -> {
                    }
                }
            }
            switch (nChar) {
                case VK_BACK -> gameListControl.deinit();
                case VK_RETURN -> mainWindow.focusChatInput();
                case VK_SPACE -> {
                    if (mainWindow.chatOpen == 0) {
                        if (pCUnit == null || pCUnit.HP > -0x28) {
                            toggleSpacebarPanels(mainWindow);
                        } else {
                            sendReturnAfterDeathAction();
                        }
                    }
                }
                case VK_C -> {
                    if (Globals.controlKeyDown) {
                        toggleMessageColors();
                    }
                }
                case VK_E -> selectCurrentPlayerUnits();
                case VK_F -> {
                    if (Globals.controlKeyDown) {
                        cycleFormationMode();
                        addTimedGameListLine(get(MAIN_FORMATION_IS_OFF_97 + getFormation()));
                    }
                }
                case VK_H -> {
                    if (Globals.controlKeyDown) {
                        toggleShowHitPointBars();
                        addTimedGameListLine(get(MAIN_SHOW_HEALTH_OFF_100 + showHitPointBars));
                    }
                }
                case VK_B, VK_Q -> {
                    mainWindow.pRightPanelContainerVisualObject.onMessage(TOGGLE_SPELL_PANEL, 0, 0);
                    onMessage(TOGGLE_SPELL_PANEL, 0, 0);
                }
                case VK_I, VK_OEM_3 -> {
                    mainWindow.pRightPanelContainerVisualObject.onMessage(TOGGLE_SELECTION_PANEL, 0, 0);
                    onMessage(TOGGLE_SELECTION_PANEL, 0, 0);
                }
                case VK_K -> {
                    if (Globals.controlKeyDown && mainWindow.sessionMode != CMainWindow.SESSION_MODE_CAMPAIGN) {
                        Globals.gamePreferences.clanNames = Globals.gamePreferences.clanNames == 0 ? 1 : 0;
                        addTimedGameListLine(get(MAIN_DON_T_SHOW_NAMES_AND_CLANS_367 + Globals.gamePreferences.clanNames));
                    }
                }
                case VK_L -> {
                    if (Globals.controlKeyDown) {
                        toggleShowFlyingHitPointBars();
                        addTimedGameListLine(get(MAIN_FLYING_DAMAGE_IS_OFF_102 + showFlyingHitPointBars));
                    }
                }
                case VK_N -> {
                    if (Globals.controlKeyDown) {
                        Globals.gamePreferences.showTimeFlow = Globals.gamePreferences.showTimeFlow == 0 ? 1 : 0;
                        addTimedGameListLine(get(MAIN_DAY_NIGHT_CHANGES_ARE_OFF_104 + Globals.gamePreferences.showTimeFlow));
                        refreshTimeFlowLighting(true);
                    }
                }
                case VK_O -> {
                    if (Globals.controlKeyDown) {
                        Globals.gamePreferences.smoothing = Globals.gamePreferences.smoothing == 0 ? 1 : 0;
                        addTimedGameListLine(get(MAIN_SMOOTHING_IS_OFF_106 + Globals.gamePreferences.smoothing));
                    }
                }
                case VK_T -> {
                    if (Globals.controlKeyDown) {
                        Globals.showNetworkStats = Globals.showNetworkStats == 0 ? 1 : 0;
                    }
                }
                case VK_U -> {
                    if (Globals.controlKeyDown) {
                        cycleAutoCastingMode();
                        addTimedGameListLine(get(PATCH, PatchText.byIndex(
                                PatchText.AUTOCASTING_IS_SET_TO_MINIMUM_115.index() + autoCastingModeIndex()
                        )));
                    } else if (GAMEPLAY.isSetIn(mainWindow.dialogsMask)) {
                        sendReviveStuckHeroAction();
                    }
                }
                case VK_W -> {
                    if (Globals.controlKeyDown) {
                        cycleWimpyMode();
                        addTimedGameListLine(get(MAIN_RETREAT_MODE_OFF_94 + getWimpyMode()));
                    } else if (mainWindow.sessionMode != CMainWindow.SESSION_MODE_CAMPAIGN) {
                        mainWindow.postMessage(TOGGLE_QUEST_STATUS_DIALOG, 0, 0);
                    }
                }
                case VK_LEFT -> scrollCameraXBy(-1);
                case VK_UP -> scrollCameraYBy(-1);
                case VK_RIGHT -> scrollCameraXBy(1);
                case VK_DOWN -> scrollCameraYBy(1);
                case VK_F4, VK_F5, VK_F6, VK_F7, VK_F8, VK_F9, VK_F10, VK_F11, VK_F12 ->
                        handlePlayerSlotFunctionKey(mainWindow, nChar - PLAYER_SLOT_FUNCTION_KEY_BASE);
                default -> {
                }
            }
        } else if (SHOP_DIALOG.isSetIn(mainWindow.dialogsMask)
                && (nChar == VK_B || nChar == VK_Q)) {
            mainWindow.pRightPanelContainerVisualObject.onMessage(TOGGLE_SPELL_PANEL, 0, 0);
            onMessage(TOGGLE_SPELL_PANEL, 0, 0);
            mainWindow.inputController.getChildById(1000).onMessage(TOGGLE_SPELL_PANEL, 0, 0);
        }
        return 0;
    }

    /**
     * Native support extracted from MapVisualObject::OnKeyDown @0040C8A0 function-key branch.
     */
    private void handlePlayerSlotFunctionKey(CMainWindow mainWindow, int playerSlotIndex) {
        PlayerSlot playerSlot = mainWindow.m_GameSession.m_PlayerSlots[playerSlotIndex];
        if ((playerSlot.type != PLAYER_SLOT_TYPE_INVENTORY || Globals.controlKeyDown) && !Globals.shiftKeyDown) {
            mainWindow.pSpellPanelVisualObject.onMessage(ASSIGN_PLAYER_SLOT, playerSlotIndex, Globals.controlKeyDown);
        }
        if ((playerSlot.type != PLAYER_SLOT_TYPE_SPELL || Globals.shiftKeyDown) && !Globals.controlKeyDown) {
            mainWindow.pHeroInventoryControlVisualObject.onMessage(ASSIGN_PLAYER_SLOT, playerSlotIndex, Globals.shiftKeyDown);
        }
        if (playerSlot.type == PLAYER_SLOT_TYPE_SPELL) {
            int spellSlot = Short.toUnsignedInt(playerSlot.color) & 0xFF;
            if (!hasSpellPanelChild() && mainWindow.pSpellPanelVisualObject.hasSelectedAvailableSpellSlot(spellSlot)) {
                executeOrderType(ORDER_TYPE_CAST_SLOT_A);
            } else if (!hasSpellPanelChild()
                    && mainWindow.pSpellPanelVisualObject.hasActiveSpellEffectSlot(spellSlot)) {
                executeOrderType(ORDER_TYPE_CAST_SLOT_B);
            }
        }
    }

    /**
     * Native: MapVisualObject::selectCurrentPlayerUnits @00416717.
     * Fully ported.
     */
    private void selectCurrentPlayerUnits() {
        for (CGameObject object : objects.values()) {
            if (!(object instanceof CUnit unit)
                    || unit.cPlayer != currentPlayer
                    || unit.field51_0x184 > 1
                    || (unit.unitFlags & 0x80) != 0) {
                object.setSelected(false);
            } else {
                object.setSelected(true);
            }
        }
        updateSelectionState();
    }

    /**
     * Native support extracted from MapVisualObject::OnKeyDown @0040C8A0 timed preference-status lines.
     */
    private void addTimedGameListLine(String text) {
        gameListControl.addTimedLine(text, Palettes.messagePrimary(), 2000);
    }

    /**
     * Native support extracted from MapVisualObject::OnKeyDown @0040C8A0 Ctrl+C branch and global
     * `setMessageColorsPalette @004756C3`.
     */
    private void toggleMessageColors() {
        Palettes.setMessageColorsPalette(Globals.gamePreferences.messageColors == 0 ? 1 : 0);
        addTimedGameListLine(get(PATCH, PatchText.byIndex(
                PatchText.STANDARD_MESSAGE_COLORS_85.index() + Globals.gamePreferences.messageColors
        )));
    }

    /**
     * Native: MapVisualObject::cycleFormationMode @0041F040.
     * Fully ported.
     */
    private void cycleFormationMode() {
        formationMode = (formationMode + 1) % 3;
        applyFormationMode(formationMode);
    }

    /**
     * Native: MapVisualObject::cycleWimpyMode @0041F090.
     * Fully ported.
     */
    private void cycleWimpyMode() {
        wimpyMode = (wimpyMode + 1) % 3;
        applyWimpyMode(wimpyMode);
    }

    /**
     * Native: MapVisualObject::ToggleShowHP @0041F0E0.
     * Fully ported.
     */
    public void toggleShowHitPointBars() {
        showHitPointBars = showHitPointBars == 0 ? 1 : 0;
    }

    /**
     * Native: MapVisualObject::toggleShowFlyingHitPointBars @0041F120.
     * Fully ported.
     */
    private void toggleShowFlyingHitPointBars() {
        showFlyingHitPointBars = showFlyingHitPointBars == 0 ? 1 : 0;
    }

    /**
     * Native: MapVisualObject::GetWimpyMode @0041F160.
     * Fully ported.
     */
    private int getWimpyMode() {
        return wimpyMode;
    }

    /**
     * Native: MapVisualObject::GetFormation @0041F180.
     * Fully ported.
     */
    private int getFormation() {
        return formationMode;
    }

    /**
     * Native: MapVisualObject::cycleAutoCastingMode @0041A54A.
     * Fully ported.
     */
    private void cycleAutoCastingMode() {
        int modeBits = Globals.gamePreferences.autoCasting & AUTOCAST_MODE_MASK;
        if (modeBits == GamePreferences.AUTOCAST_BASE) {
            modeBits = GamePreferences.AUTOCAST_BASE | GamePreferences.AUTOCAST_MODE_AVERAGE;
        } else if (modeBits == (GamePreferences.AUTOCAST_BASE | GamePreferences.AUTOCAST_MODE_AVERAGE)) {
            modeBits = GamePreferences.AUTOCAST_BASE
                    | GamePreferences.AUTOCAST_MODE_AVERAGE
                    | GamePreferences.AUTOCAST_MODE_MAXIMUM;
        } else if (modeBits == (GamePreferences.AUTOCAST_BASE
                | GamePreferences.AUTOCAST_MODE_AVERAGE
                | GamePreferences.AUTOCAST_MODE_MAXIMUM)) {
            modeBits = GamePreferences.AUTOCAST_BASE;
        }
        Globals.gamePreferences.autoCasting = (Globals.gamePreferences.autoCasting & 0x7) | modeBits;
        applyAutoCasting();
    }

    /**
     * Native support extracted from MapVisualObject::OnKeyDown @0040C8A0 Ctrl+U status-line index build.
     */
    private static int autoCastingModeIndex() {
        if ((Globals.gamePreferences.autoCasting & GamePreferences.AUTOCAST_MODE_MAXIMUM) != 0) {
            return 2;
        }
        return (Globals.gamePreferences.autoCasting & GamePreferences.AUTOCAST_MODE_AVERAGE) != 0 ? 1 : 0;
    }

    /**
     * Native: MapVisualObject::issuePickupAllSacksAction @00419F2A.
     * Fully ported.
     */
    private void issuePickupAllSacksAction() {
        PickupAllSacksAction action = PickupAllSacksAction.global;
        action.ID.set(PickupAllSacksAction.ACTION_ID);
        action.netID.set(currentPlayer.playerId);
        action.playerID.set(0);
        action.entryCount.set(0);
        action.unitTokenIds.set(new byte[0]);
        for (CGameObject object : objects.values()) {
            if (object.isSelected() && object instanceof CUnit unit && (unit.unitFlags & 0x1) != 0) {
                action.addUnitToken(object.m_id);
                break;
            }
        }
        CServerApp.sendClientGameAction(action);

        CUnit voiceUnit = selectAcknowledgementVoiceUnit();
        if (voiceUnit != null) {
            voiceUnit.playPickupVoice();
        }
    }

    /**
     * Native: MapVisualObject::issueStandStillOrder @00419FFD.
     * Fully ported.
     */
    private void issueStandStillOrder() {
        StandStillOrderAction action = StandStillOrderAction.global;
        action.ID.set(StandStillOrderAction.ACTION_ID);
        action.netID.set(currentPlayer.playerId);
        action.playerID.set(0);
        action.entryCount.set(0);
        action.unitTokenIds.set(new byte[0]);
        for (CGameObject object : objects.values()) {
            if (object.isSelected()) {
                action.addUnitToken(object.m_id);
            }
        }
        CServerApp.sendClientGameAction(action);

        CUnit voiceUnit = selectAcknowledgementVoiceUnit();
        if (voiceUnit != null) {
            voiceUnit.playDefendVoice();
        }
    }

    /**
     * Native: MapVisualObject::selectAcknowledgementVoiceUnit @0041DA16.
     * Fully ported.
     */
    private CUnit selectAcknowledgementVoiceUnit() {
        if (Globals.mainWindow.sessionMode != CMainWindow.SESSION_MODE_CAMPAIGN
                || Globals.gamePreferences.acknowledgement == 0) {
            return null;
        }

        List<CUnit> primaryHeroes = new ArrayList<>();
        List<CUnit> equippedOrStoryUnits = new ArrayList<>();
        List<CUnit> unequippedSecondaryHeroes = new ArrayList<>();
        for (CGameObject object : objects.values()) {
            if (!(object instanceof CUnit unit) || !unit.isSelected() || unit.HP <= 0) {
                continue;
            }

            if ((unit.unitFlags & 0x1) != 0) {
                primaryHeroes.add(unit);
            } else if (primaryHeroes.isEmpty() && (unit.unitFlags & 0x10) != 0) {
                if (unit.equipmentTokenEntries[0] == null) {
                    if (equippedOrStoryUnits.isEmpty()) {
                        unequippedSecondaryHeroes.add(unit);
                    }
                } else {
                    equippedOrStoryUnits.add(unit);
                }
            } else if (Short.toUnsignedInt(unit.serverID) != 0 && Short.toUnsignedInt(unit.serverID) < 0x15) {
                equippedOrStoryUnits.add(unit);
            }
        }

        if (!primaryHeroes.isEmpty()) {
            return primaryHeroes.get(Utils.randExclusive(0, primaryHeroes.size()));
        }
        if (!equippedOrStoryUnits.isEmpty()) {
            return equippedOrStoryUnits.get(Utils.randExclusive(0, equippedOrStoryUnits.size()));
        }
        if (!unequippedSecondaryHeroes.isEmpty()) {
            return unequippedSecondaryHeroes.get(Utils.randExclusive(0, unequippedSecondaryHeroes.size()));
        }
        return null;
    }

    /**
     * Native support extracted from MapVisualObject::OnKeyDown @0040C8A0 VK_SPACE branch.
     */
    private void toggleSpacebarPanels(CMainWindow mainWindow) {
        if (!hasSelectionPanelChild() && !hasSpellPanelChild()) {
            mainWindow.pRightPanelContainerVisualObject.onMessage(TOGGLE_SELECTION_PANEL, 0, 0);
            onMessage(TOGGLE_SELECTION_PANEL, 0, 0);
            mainWindow.pRightPanelContainerVisualObject.onMessage(TOGGLE_SPELL_PANEL, 0, 0);
            onMessage(TOGGLE_SPELL_PANEL, 0, 0);
            return;
        }

        if (hasSelectionPanelChild()) {
            mainWindow.pRightPanelContainerVisualObject.onMessage(TOGGLE_SELECTION_PANEL, 0, 0);
            onMessage(TOGGLE_SELECTION_PANEL, 0, 0);
        }
        if (hasSpellPanelChild()) {
            mainWindow.pRightPanelContainerVisualObject.onMessage(TOGGLE_SPELL_PANEL, 0, 0);
            onMessage(TOGGLE_SPELL_PANEL, 0, 0);
        }
    }

    /**
     * Native: MapVisualObject::sendReturnAfterDeathAction @0041AB96.
     * Fully ported.
     */
    private void sendReturnAfterDeathAction() {
        ReturnAfterDeathAction action = ReturnAfterDeathAction.global;
        action.ID.set(ReturnAfterDeathAction.ACTION_ID);
        action.netID.set(currentPlayer.playerId);
        action.playerID.set(0);
        CServerApp.sendClientGameAction(action);
    }

    /**
     * Java-only map zoom command. This is not native behavior: it intentionally reduces the current logical cell count,
     * making tiles larger on the physical viewport after the Java render-target scale pass.
     * not ported.
     */
    public void zoomIn() {
        setJavaMapZoom(javaMapZoom - 1);
    }

    /**
     * Java-only map zoom command. This is not native behavior: it intentionally increases the current logical cell count,
     * making more map cells visible after the Java render-target scale pass.
     * not ported.
     */
    public void zoomOut() {
        setJavaMapZoom(javaMapZoom + 1);
    }

    /**
     * vtbl +0x74: MapVisualObject::OnChar @0040D450.
     * Fully ported.
     */
    @Override
    public int onChar(int nChar) {
        return MODAL_DIALOG.isUnsetIn(Globals.mainWindow.dialogsMask) ? 1 : 0;
    }

    /**
     * Native: MapVisualObject::connectAndJoinSession @0040D480.
     * Fully ported. Java preserves the direct-delivery setup, client connect boundary, remote event pump,
     * player-join wait, and recovered failure dialogs.
     */
    public boolean connectAndJoinSession() {
        Globals.multiplayerBootstrapStatusWord = PatchText.SERVER_IS_NOT_RESPONDING_0.index();
        CMainWindow mainWindow = Globals.mainWindow;
        if (mainWindow.sessionMode == CMainWindow.SESSION_MODE_NETWORK_HOST
                || mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN) {
            CServerApp.initializeDirectDeliveryLoopback();
        } else if (!CLlDriver.connectMultiplayerClientBoundary(
                mainWindow.m_GameSession.m_PlayerName,
                resolveCommittedSessionEntry(mainWindow.multiplayerSessionDialogContext)
        )) {
            showMultiplayerBootstrapFailureDialog(get(MAIN_UNKNOWN_ERROR_154));
            return false;
        }
        CServerApp.processRemoteNetworkEvents();
        if (!sendPlayerJoinAndWaitForPlayerList()) {
            showMultiplayerBootstrapFailureDialog(resolveMultiplayerBootstrapFailureText());
            return false;
        }
        return true;
    }

    /**
     * Native support extracted from MapVisualObject::connectAndJoinSession @0040D480 selected session pointer branch.
     */
    private static LlDriverSessionEntry resolveCommittedSessionEntry(MultiplayerSessionDialogContext context) {
        if (context.sessionEntries.isEmpty()) {
            return null;
        }
        return context.sessionEntries.get(context.committedSessionIndex);
    }

    /**
     * Native support extracted from MapVisualObject::connectAndJoinSession @0040D480 failure prompt construction.
     */
    private static String resolveMultiplayerBootstrapFailureText() {
        int statusLowByte = Globals.multiplayerBootstrapStatusWord & 0xFF;
        if (statusLowByte < PatchText.YOUR_CHARACTER_FILE_NOT_FOUND_IT_MAY_BE_DELETED_11.index()) {
            return get(MAIN, MainText.byIndex(MULTIPLAYER_BOOTSTRAP_STATUS_MAIN_TEXT_BASE + statusLowByte));
        }
        return get(PATCH, PatchText.byIndex(statusLowByte));
    }

    /**
     * Native support extracted from MapVisualObject::connectAndJoinSession @0040D480 failure prompt display.
     */
    private static void showMultiplayerBootstrapFailureDialog(String promptText) {
        Globals.mainWindow.showDialog(new HeaderDialogVariantVisualObject(
                1,
                0x40,
                100,
                0x17C,
                0x252,
                promptText,
                null,
                0
        ));
    }

    /**
     * Native: MapVisualObject::sendPlayerJoinAndWaitForPlayerList @0040D791.
     * Fully ported. Java represents the native `pCUnit` destructor branch as managed object detachment and keeps the
     * Win32 `PeekMessage` loop at the shared posted-message pump boundary.
     */
    public boolean sendPlayerJoinAndWaitForPlayerList() {
        CMainWindow mainWindow = Globals.mainWindow;
        if (mainWindow.sessionMode == CMainWindow.SESSION_MODE_MULTIPLAYER_CLIENT) {
            CServerApp.sendLatencySetting(Globals.networkLatencyMillis, null);
            CServerApp.setNextRemoteClientSendInterval(Globals.networkLatencyMillis);
        }

        PlayerJoinAction action = PlayerJoinAction.prepareForPlayerJoin(mainWindow.m_GameSession);
        objects.clear();
        pCUnit = null;
        updateSelectionState();
        CServerApp.sendClientGameAction(action);
        if (mainWindow.serverBootstrapEnabled != 0) {
            Globals.gameServer.pumpServerWorldActions();
        }

        int startTick = Globals.currentTickMillis();
        while (true) {
            if (CServerApp.getPendingSegmentMarkerCount() != 0) {
                if (!handleGameAction(null, GameActionId.NEW_SEGMENT_ACTION_64.id)) {
                    return false;
                }
                if (clientPlayers.size() < 2) {
                    Globals.multiplayerBootstrapStatusWord = NOT_ENOUGH_JOINED_PLAYERS_STATUS_WORD;
                    return false;
                }
                return true;
            }
            MessageSystem.pumpPostedMessage();
            if (Integer.compareUnsigned(Globals.currentTickMillis() - startTick, Globals.networkTimeoutMillis) > 0) {
                Globals.multiplayerBootstrapStatusWord = PLAYER_JOIN_TIMEOUT_STATUS_WORD;
                return false;
            }
            Globals.mousePointer.update();
            CServerApp.processRemoteNetworkEvents();
        }
    }

    /**
     * Java convenience overload for direct recovered action dispatch.
     * ACTION OWNERSHIP RULE: do not put concrete packet behavior in MapVisualObject::handleGameAction.
     * The owning `CGameAction` subclass must implement packet-specific behavior in `handle(MapVisualObject)`;
     * this method is only a dispatch boundary and queue/filter adapter.
     * not ported.
     */
    public boolean handleGameAction(CGameAction pGameAction) {
        return handleGameAction(pGameAction, pGameAction.ID.get());
    }

    /**
     * Native: MapVisualObject::HandleGameAction @0040D9B2.
     * Fully ported in Java. Java dispatches direct actions through `CGameAction::handle` and preserves the native
     * read-next-action boundary through `CServerApp::ReadNextGameAction @00501831` for queue pumping.
     * ACTION OWNERSHIP RULE: do not add per-packet switch branches or concrete action implementations here.
     * The owning `CGameAction` subclass must contain the recovered packet behavior in `handle(MapVisualObject)`.
     * MapVisualObject may expose focused state helpers because the action receives this object explicitly.
     */
    public boolean handleGameAction(CGameAction pGameAction, int p) {
        if (pGameAction != null) {
            pGameAction.handle(this);
            if (shouldReturnFalseAfterNetworkClose(pGameAction)) {
                drainPendingGameActionsBeforeNetworkClose();
                return false;
            }
            if (shouldReturnFalseAfterCurrentPlayerKick(pGameAction)) {
                drainPendingGameActionsBeforeNetworkClose();
                return false;
            }
            if (shouldReturnTrueAfterServerClosed(pGameAction)) {
                return true;
            }
            return gameActionMatchesFilter(pGameAction, p);
        }

        while (true) {
            CGameAction nextAction = CServerApp.readNextGameAction();
            while (nextAction != null) {
                nextAction.handle(this);
                if (shouldReturnFalseAfterNetworkClose(nextAction)) {
                    drainPendingGameActionsBeforeNetworkClose();
                    return false;
                }
                if (shouldReturnFalseAfterCurrentPlayerKick(nextAction)) {
                    drainPendingGameActionsBeforeNetworkClose();
                    return false;
                }
                if (shouldReturnTrueAfterServerClosed(nextAction)) {
                    return true;
                }
                if (gameActionMatchesFilter(nextAction, p)) {
                    return true;
                }
                nextAction = CServerApp.readNextGameAction();
            }
            if (shouldReturnFalseWhenActionQueueDrained(p)) {
                return false;
            }
            if (shouldReturnFalseWhenCurrentPlayerRemovalDrained(p)) {
                return false;
            }
            CServerApp.processRemoteNetworkEvents();
            Globals.mousePointer.update();
        }
    }

    /**
     * Native support extracted from the empty action-queue wait gate in MapVisualObject::HandleGameAction @0040D9B2.
     */
    private static boolean shouldReturnFalseWhenActionQueueDrained(int p) {
        return (p & 0xFF) == 0 && GAMEPLAY.isUnsetIn(Globals.mainWindow.dialogsMask);
    }

    /**
     * not ported. Java direct-paired current-player removal can drain the local client during
     * Unit::Update @0050F12C before GameServer::advanceServerLoopCounterAndObjects @004F8521 broadcasts
     * NEW_SEGMENT_ACTION_64, so the Java wait loop must stop once that current server player is pending removal.
     */
    private boolean shouldReturnFalseWhenCurrentPlayerRemovalDrained(int p) {
        if ((p & 0xFF) != GameActionId.NEW_SEGMENT_ACTION_64.id) {
            return false;
        }
        Player serverPlayer = Globals.gameServer.playerList.getPlayerById(currentPlayer.playerId);
        return serverPlayer == null || serverPlayer.pendingRemovalServerTick > 0;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00415D4B and @0040DC36.
     */
    private static boolean shouldReturnFalseAfterNetworkClose(CGameAction action) {
        int actionId = action.ID.get();
        if (actionId == GameActionId.TWO_DWORD_ACTION_0B.id) {
            return GAMEPLAY.isUnsetIn(Globals.mainWindow.dialogsMask);
        }
        if (actionId != GameActionId.SERVER_CLOSED_ACTION_AF.id
                || Globals.mainWindow.sessionMode != CMainWindow.SESSION_MODE_MULTIPLAYER_CLIENT) return false;
        return GAMEPLAY.isUnsetIn(Globals.mainWindow.dialogsMask);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0041557B self-kick no-gameplay branch.
     */
    private boolean shouldReturnFalseAfterCurrentPlayerKick(CGameAction action) {
        return action instanceof PlayerKickedAction playerKickedAction
                && playerKickedAction.targetsCurrentPlayer(this)
                && GAMEPLAY.isUnsetIn(Globals.mainWindow.dialogsMask);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00415D4B.
     */
    private static boolean shouldReturnTrueAfterServerClosed(CGameAction action) {
        return action.ID.get() == GameActionId.SERVER_CLOSED_ACTION_AF.id
                && Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_MULTIPLAYER_CLIENT
                && GAMEPLAY.isSetIn(Globals.mainWindow.dialogsMask);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00415D4B and @00415CAF.
     */
    private static void drainPendingGameActionsBeforeNetworkClose() {
        while (CServerApp.readNextGameAction() != null) {
            // Native drains queued packets before closing the low-level driver on the false-return tail.
        }
        CLlDriver.handleNetworkErrorAndClose();
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00415F53.
     */
    private static boolean gameActionMatchesFilter(CGameAction pGameAction, int p) {
        return pGameAction.ID.get() == (p & 0xFF);
    }

    /**
     * Native: MapVisualObject::requestMapLoad @0041C50D.
     * Fully ported.
     */
    public void requestMapLoad(int mapLoadMode) {
        RequestMapLoadAction action = RequestMapLoadAction.global;
        action.ID.set(RequestMapLoadAction.ACTION_ID);
        action.netID.set(resolveGameActionNetID());
        action.playerID.set(0);
        action.firstPayloadDword.set(mapLoadMode);
        CServerApp.sendClientGameAction(action);
    }

    /**
     * Native: MapVisualObject::requestNextMapChunk @0041C5A4.
     * Fully ported in Java.
     */
    public void requestNextMapChunk(int nextOffset) {
        CServerApp.sendClientGameAction(RequestMapChunkAction.create(resolveGameActionNetID(), nextOffset));
    }

    /**
     * Native: MapVisualObject::SendChatTextAction @0041ACE5.
     * Fully ported.
     */
    public void sendChatTextAction(String text, int deliveryType, int recipientPlayerIndex) {
        ChatTextAction action = ChatTextAction.global;
        action.ID.set(ChatTextAction.ACTION_ID);
        action.netID.set(resolveGameActionNetID());
        action.playerID.set(0);
        action.text.set(text);
        action.firstPayloadDword.set(resolveChatTargetPlayerId(recipientPlayerIndex) | (deliveryType << 8));
        if (CServerApp.hasActiveRemoteConnection()) {
            CServerApp.sendClientGameAction(action);
        } else if (CServerApp.hasActiveLocalConnection() && Globals.gameServer != null) {
            CServerApp.sendGameAction(action);
            CServerApp.flushActiveClientWriteBuffers();
        }
    }

    /**
     * Native: MapVisualObject::sendSelectedMultiplayerMapAction @0041C7E0.
     * Fully ported.
     */
    public void sendSelectedMultiplayerMapAction(String mapDisplayName) {
        SelectMultiplayerMapAction action = SelectMultiplayerMapAction.global;
        action.ID.set(SelectMultiplayerMapAction.ACTION_ID);
        if (currentPlayer != null) {
            action.netID.set(currentPlayer.playerId);
        }
        action.playerID.set(0);
        action.text.set(mapDisplayName);
        CServerApp.sendGameAction(action);
        CServerApp.flushActiveClientWriteBuffers();
    }

    /**
     * Native: MapVisualObject::sendDiplomacyRelationsAction @0041A17A.
     * Fully ported.
     */
    public void sendDiplomacyRelationsAction() {
        UpdateDiplomacyRelationsAction action = UpdateDiplomacyRelationsAction.global;
        action.ID.set(UpdateDiplomacyRelationsAction.ACTION_ID);
        action.netID.set(currentPlayer.playerId);
        action.playerID.set(0);
        ActionPayloads.setShortArray(action.shortValueCount, action.shortValues, currentPlayer.diplomacyFlags);
        CServerApp.sendClientGameAction(action);
    }

    /**
     * Native: MapVisualObject::notifyMapChunkTransferComplete @0041C55E.
     * Fully ported.
     */
    public void notifyMapChunkTransferComplete() {
        MapChunkTransferCompleteAction action = MapChunkTransferCompleteAction.global;
        action.ID.set(MapChunkTransferCompleteAction.ACTION_ID);
        action.netID.set(currentPlayer.playerId);
        action.playerID.set(0);
        CServerApp.sendClientGameAction(action);
    }

    /**
     * Native: MapVisualObject::submitCharacterSetup @0041C5F5.
     * Fully ported.
     */
    public void submitCharacterSetup() {
        var gameSession = Globals.mainWindow.m_GameSession;
        SubmitCharacterSetupAction action = SubmitCharacterSetupAction.prepareForCharacterSetup(
                resolveGameActionNetID(),
                gameSession
        );
        CServerApp.sendClientGameAction(action);
    }

    /**
     * Native: MapVisualObject::uploadCharacterFile @0041C6B5.
     * Fully ported.
     */
    public void uploadCharacterFile(String characterFilePath) {
        UploadCharacterFileAction action = UploadCharacterFileAction.prepareForCharacterFileUpload(
                resolveGameActionNetID(),
                characterFilePath
        );
        CServerApp.sendClientGameAction(action);
    }

    /**
     * Native non-vtable selection-state pass: MapVisualObject::UpdateSelectionState @004167C2.
     * Fully ported.
     */
    public void updateSelectionState() {
        resetSelectionState();

        boolean primarySet = false;
        boolean spellUiActive = Globals.mainWindow.isSpellUiActive();
        for (Map.Entry<Short, CGameObject> entry : objects.entrySet()) {
            CGameObject obj = entry.getValue();
            if (!obj.isSelected()) {
                continue;
            }

            selectedCount++;
            selectedAvailableSpellMask |= obj.availableSpellMask;

            int spellbookBitForSpellId = Spellbook.availableSpellMaskBitForSpellId(Byte.toUnsignedInt(obj.autoCastSpellId));
            if (spellbookBitForSpellId != 0) {
                autoCastSpellbookMask |= spellbookBitForSpellId;
            }

            if (!primarySet) {
                primarySelectedObjectToken = entry.getKey();
                primarySelectedObject = obj;
                primarySet = true;
            }

            if (!spellUiActive) {
                // Native else-path sets selectionFlags to 0x8 when spell UI mode is not active.
                selectionFlags = FLAG_AUTOMATIC_CAST;
                continue;
            }

            if (obj instanceof CAirUnit) {
                selectionFlags |= FLAG_AIR;
            } else if (obj instanceof CStructure) {
                selectionFlags |= FLAG_STRUCTURE;
            } else if (obj instanceof CUnit cUnit) {
                selectionFlags |= FLAG_UNIT;
                if ((cUnit.unitFlags & 0x1) != 0) {
                    selectionFlags |= FLAG_AUTOMATIC_CAST;
                }

                if ((cUnit.type == 0x17 || cUnit.type == 0x18) && cUnit.availableSpellMask != 0) {
                    selectionFlags |= FLAG_HAS_SPELLS;
                    updateSpellModifiers(cUnit);
                }

                updateSpellEffects(cUnit);
            }
        }

        applyPrimarySelectedOwnerFlag();
        notifySelectionUi();
    }

    // Native support extracted from MapVisualObject::UpdateSelectionState @004167C2.
    private void resetSelectionState() {
        selectedCount = 0;
        selectedAvailableSpellMask = 0;
        autoCastSpellbookMask = 0;
        activeSpellEffectMask = 0;
        selectionFlags = 0;
        primarySelectedObjectToken = 0;
        primarySelectedObject = null;

        resetStatModifiersToNativeDefaults();
    }

    // Native support extracted from MapVisualObject::UpdateSelectionState @004167C2 stat modifier reset block.
    private void resetStatModifiersToNativeDefaults() {
        statModifiers.resetDefaults();
        for (int i = 0; i < EFFECT_COUNT; i++) {
            statModifiers.minRange().set(i, 0xFFFF);
            statModifiers.manaCost().set(i, 0xFFFF);
            statModifiers.minDuration().set(i, 0xFFFF);

            statModifiers.minSpeed().set(i, 0xFFFF);
            statModifiers.maxSpeed().set(i, -0xFFFF);
            statModifiers.minResistance().set(i, 0xFFFF);
            statModifiers.minSight().set(i, 0xFFFF);
            statModifiers.maxSight().set(i, -0xFFFF);
            statModifiers.minMaximumDamageProbability().set(i, 0xFFFF);
            statModifiers.minMinimumDamageProbability().set(i, 0xFFFF);
            statModifiers.minRays().set(i, 0xFFFF);
            statModifiers.minAbsorption().set(i, 0xFFFF);
        }
    }

    // Native support extracted from MapVisualObject::UpdateSelectionState @004167C2 spell stat modifier block.
    private void updateSpellModifiers(CUnit unit) {
        for (int i = 0; i < EFFECT_COUNT; i++) {
            int bit = 1 << i;
            if ((unit.availableSpellMask & bit) == 0) {
                continue;
            }

            byte spellId = getSpellId(i);
            Spell spell = new Spell(spellId);
            byte spellBase = getSpellBaseFromUnit(unit, spell);
            int context = computeSpellContext(spellBase, unit.mind);
            spell.updateStats(spellBase, unit.mind);

            int minDamage = spell.getMinDamage();
            int maxDamage = spell.getMaxDamage();
            int manaCost = spell.getManaCost();
            int range = spell.getMaxRange();
            int duration = spell.getDuration();

            statModifiers.minDamage().set(i, minDamage);
            statModifiers.maxDamage().set(i, maxDamage);
            statModifiers.manaCost().set(i, manaCost);
            statModifiers.manaCostMirror().set(i, manaCost);
            statModifiers.minRange().set(i, range);
            statModifiers.maxRange().set(i, range);
            statModifiers.minDuration().set(i, duration);
            statModifiers.maxDuration().set(i, duration);

            if (spell.isA(SpellId.STONE_CURSE)) {
                statModifiers.minDuration().set(i, 0);
            }

            int bonusType1 = spell.getSpeed(context);
            if (bonusType1 != 0) {
                statModifiers.minSpeed().set(i, bonusType1);
                statModifiers.maxSpeed().set(i, bonusType1);
            }

            int bonusType2 = spell.getResistance(context);
            statModifiers.minResistance().set(i, bonusType2);
            statModifiers.maxResistance().set(i, bonusType2);

            int bonusType3 = spell.getSight(context);
            if (bonusType3 != 0) {
                statModifiers.minSight().set(i, bonusType3);
                statModifiers.maxSight().set(i, bonusType3);
            }

            int bonusType4 = spell.getMaximumDamageProbability(context);
            statModifiers.minMaximumDamageProbability().set(i, bonusType4);
            statModifiers.maxMaximumDamageProbability().set(i, bonusType4);

            int bonusType5 = spell.getMinimumDamageProbability(context);
            statModifiers.minMinimumDamageProbability().set(i, bonusType5);
            statModifiers.maxMinimumDamageProbability().set(i, bonusType5);

            int bonusType6 = spell.getRays(context);
            statModifiers.minRays().set(i, bonusType6);
            statModifiers.maxRays().set(i, bonusType6);

            int bonusType7 = spell.getBonusType5(context);
            statModifiers.minAbsorption().set(i, bonusType7);
            statModifiers.maxAbsorption().set(i, bonusType7);
        }
    }

    // Native support extracted from MapVisualObject::UpdateSelectionState @004167C2.
    private void updateSpellEffects(CGameObject unit) {
        for (TokenEntry entry : unit.tokenEntries) {
            if (entry == null || entry.getType() != TokenEntry.TYPE_SPELL) {
                continue;
            }

            int spellId = entry.getCastSpellId();
            if (spellId == 0) {
                continue;
            }

            int mask = Spellbook.availableSpellMaskBitForSpellId(spellId);
            if (mask == 0) {
                continue;
            }

            unit.activeSpellEffectMask |= mask;
            activeSpellEffectMask |= mask;
        }
    }

    /**
     * Native support: inline `sphereLevels[Spell::GetSphere() - 1]` lookup in MapVisualObject::UpdateSelectionState @004167C2.
     * not ported.
     */
    private static byte getSpellBaseFromUnit(CUnit unit, Spell spell) {
        return unit.sphereLevels[Byte.toUnsignedInt(spell.getSphere()) - 1];
    }

    /**
     * Native support: inline spell-context clamp in MapVisualObject::UpdateSelectionState @004167C2.
     * not ported.
     */
    private static int computeSpellContext(byte base, byte modifier) {
        int context = Byte.toUnsignedInt(base) - 0x1E + Byte.toUnsignedInt(modifier);
        if (context < 0) {
            return 0;
        }
        return Math.min(context, 0xFF);
    }

    // Native support for direct g_Spell_IDs @005F8124 indexing in MapVisualObject::UpdateSelectionState @004167C2
    // and SpellPanelVisualObject::OnKeyDown @004C74B1.
    private byte getSpellId(int index) {
        return spellIds[index];
    }

    // Native support extracted from MapVisualObject::UpdateSelectionState @004167C2 primary-owner flag tail.
    private void applyPrimarySelectedOwnerFlag() {
        if (selectedCount > 0 && primarySelectedObject.cPlayer != currentPlayer) {
            selectionFlags |= FLAG_BUSY;
        }
    }

    /**
     * Native support extracted from the selection UI fan-out tail of MapVisualObject::UpdateSelectionState @004167C2.
     */
    public void notifySelectionUi() {
        CMainWindow mainWindow = Globals.mainWindow;
        if (selectedCount == 0 || (selectionFlags & (FLAG_BUSY | FLAG_STRUCTURE)) != 0) {
            mainWindow.pRightPanelContainerVisualObject.onMessage(NOTIFY_SELECTION_OVERLAY, 0, 0);
        } else {
            mainWindow.pRightPanelContainerVisualObject.onMessage(NOTIFY_SELECTION_PANEL, availableOrderMask(), 0);
        }

        mainWindow.pSelectionInfoPanelVisualObject.onMessage(NOTIFY_SELECTION_SPELL_STATE, 0, 0);
        mainWindow.pSideStatusVisualObject.onMessage(NOTIFY_SELECTION_SPELL_STATE, 0, 0);
        if (selectedCount == 1 && (selectionFlags & FLAG_AUTOMATIC_CAST) != 0) {
            mainWindow.pHeroInventoryControlVisualObject.bindGridSourceFromContext(primarySelectedObject);
        } else {
            mainWindow.pHeroInventoryControlVisualObject.bindGridSourceFromContext(null);
        }
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2 item-list subtype 2 tail.
     */
    public void refreshHeroInventoryBindingForInventoryUnit(CUnit unit) {
        CMainWindow mainWindow = Globals.mainWindow;
        if (SHOP_DIALOG.isUnsetIn(mainWindow.dialogsMask) && selectedCount == 1 && primarySelectedObject == unit) {
            ensureHeroInventoryMoneyEntry(unit);
            mainWindow.pHeroInventoryControlVisualObject.bindGridSourceFromContext(unit);
        }
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2 item-list subtype 2 money-entry gate.
     */
    public boolean shouldAttachHeroInventoryMoneyEntry(CUnit unit) {
        return unit == pCUnit && currentPlayer != null;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2 item-list subtype 2 tail.
     */
    private void ensureHeroInventoryMoneyEntry(CUnit unit) {
        if (!shouldAttachHeroInventoryMoneyEntry(unit)) {
            return;
        }

        List<TokenEntry> inventoryEntries = unit.tokenEntries;
        for (int index = 0; index < inventoryEntries.size(); index++) {
            TokenEntry entry = inventoryEntries.get(index);
            if (entry.isMoneyEntry()) {
                entry.quantity = currentPlayer.gold;
                if (index != inventoryEntries.size() - 1) {
                    inventoryEntries.remove(index);
                    inventoryEntries.add(entry);
                }
                return;
            }
        }

        TokenEntry money = new TokenEntry(0);
        money.packedTokenHash = TokenEntry.MONEY_ENTRY_HASH;
        money.quantity = currentPlayer.gold;
        money.wireFlags = 0;
        money.payloadEntryCount = 0;
        money.gridModeCode = 2;
        inventoryEntries.add(money);
    }

    /**
     * Native: MapVisualObject::GetSelectedCUnit @0041F1A0.
     * Fully ported.
     */
    public CUnit getSelectedCUnit() {
        return pCUnit;
    }

    /**
     * Native support extracted from GridOverlayVisualObject::BeginUiDrag @004A235D and CompleteUiDrag @004A24E8.
     */
    public CGameObject getPrimarySelectedObjectForGridOverlay() {
        return primarySelectedObject;
    }

    /**
     * Native: MapVisualObject::GetCUnit @0041D95E.
     * Fully ported through the modeled object map.
     */
    public CUnit getCUnit(int serverId) {
        return findCUnitByServerId(serverId & 0xFFFF);
    }

    /**
     * Native support extracted from SelectedUnitsSnapshot::rebuildFromCurrentPlayerUnits @00472460.
     */
    public void collectCurrentPlayerUnits(SelectedUnitsSnapshot snapshot) {
        for (CGameObject object : objects.values()) {
            if (!(object instanceof CUnit unit)
                    || unit.cPlayer != currentPlayer
                    || unit.field51_0x184 != 0) {
                continue;
            }

            if ((unit.unitFlags & 0x1) != 0) {
                snapshot.addPrimaryUnit(unit);
            } else if ((unit.unitFlags & 0x10) != 0) {
                snapshot.addSecondaryUnit(unit);
            }
        }
    }

    /**
     * Native support extracted from CGameSession::InitializeNewCharacterSession @00491312 and
     * CGameSession::LoadCharacterRosterEntry @004904C5 selected-unit bootstrap.
     */
    public CUnit ensureSelectedCUnit() {
        if (pCUnit == null) {
            pCUnit = new CUnit();
            pCUnit.initializeUnitVisualState(
                    1,
                    1,
                    0,
                    0,
                    0,
                    clientPlayers.getFirst(),
                    0,
                    0,
                    0,
                    1
            );
            pCUnit.pMapVisualObject = this;
            putScenarioObject((short) 1, pCUnit);
        }
        return pCUnit;
    }

    /**
     * Native support extracted from MapVisualObject::MapVisualObject @00402AF6 and CPlayer::CPlayer @0043B81F.
     */
    private static CPlayer createDefaultClientPlayer() {
        CPlayer player = new CPlayer(0, 0x0F);
        player.flags |= 1;
        return player;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2 pCUnit assignment branches.
     */
    public void markPlayerUnit(CUnit unit) {
        pCUnit = unit;
        unit.unitFlags |= 0x20;
    }

    /**
     * Native support extracted from CServerApp::sendDiplomacyStateSnapshot @00504E87 and
     * CPlayer::isMapVisible @0041E860. Java campaign-town flow can create `currentPlayer` before the native diplomacy snapshot is replayed,
     * but native selected-unit picture tooltips require the current player's own map-visible bit to be present.
     */
    public void ensureCurrentPlayerSelfMapVisible() {
        if (currentPlayer == null || currentPlayer.playerId < 0 || currentPlayer.playerId >= currentPlayer.diplomacyFlags.length) {
            return;
        }
        currentPlayer.diplomacyFlags[currentPlayer.playerId] |= CPlayer.MAP_VISIBLE_MASK;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2 object-map lookup branches, including
     * addresses 0040DE09 / 0040DCD4, CUnit projectile lookup consumers @00466E18 / @0046707E, and inn selected-entry
     * lookup branches @00494E88 / @00494E5F.
     */
    public CGameObject getObjectByToken(short objectTokenId) {
        return objects.get(objectTokenId);
    }

    /**
     * Native support extracted from SelectionInfoPanelVisualObject::Update @004AF3BF hovered-object info-panel branch.
     */
    public CGameObject resolveHoveredObjectForSelectionInfoPanelUpdate() {
        if (hoveredObjectToken == 0) {
            return null;
        }
        return objects.get(hoveredObjectToken);
    }

    /**
     * Native support extracted from CUnit::Draw @004632A1 hovered-object palette-page branch.
     * Fully ported.
     */
    public boolean isHoveredObject(CGameObject object) {
        return hoveredObject == object;
    }

    /**
     * Native: MapVisualObject::FindUnitByQuestFlags @0041DCEA.
     * Fully ported.
     */
    public CUnit findUnitByQuestFlags(int questFlags) {
        for (CGameObject object : objects.values()) {
            if (object instanceof CUnit unit && unit.questFlags == questFlags) {
                return unit;
            }
        }
        return null;
    }

    /**
     * Native: MapVisualObject::GetOrCreateCUnit @0041D9E1.
     * Fully ported at the lookup/create boundary. Java attaches the map owner for detached portrait-model consumers.
     */
    public CUnit getOrCreateCUnit(int serverId) {
        int normalizedServerId = serverId & 0xFFFF;
        CUnit existing = findCUnitByServerId(normalizedServerId);
        if (existing != null) {
            return existing;
        }

        CUnit unit = CUnit.createObject(normalizedServerId);
        // Native support for Java callers that use the detached portrait unit outside the object map.
        unit.pMapVisualObject = this;
        return unit;
    }

    /**
     * Native: MapVisualObject::CreateRoleDialogPortrait @0041D48D.
     * Fully ported.
     */
    public CBmp64k createRoleDialogPortrait(int npcId) {
        CUnit unit = getOrCreateCUnit(npcId);
        CBmp64k roleDialogPortrait = new CBmp64k(ROLE_DIALOG_PORTRAIT_WIDTH, ROLE_DIALOG_PORTRAIT_HEIGHT);
        if (unit == null) {
            return roleDialogPortrait;
        }

        CBmp64k sourcePortrait = createRoleDialogPortraitSource(unit);
        compositeRoleDialogPortrait(roleDialogPortrait, sourcePortrait, unit.type);
        roleDialogPortrait.syncFrameBytesFromSurface();
        return roleDialogPortrait;
    }

    /**
     * Native support extracted from MapVisualObject::CreateRoleDialogPortrait @0041D48D source portrait branches.
     */
    private static CBmp64k createRoleDialogPortraitSource(CUnit unit) {
        if ((unit.unitFlags & UNIT_FLAG_DYNAMIC_INFO_PICTURE_MASK) == 0) {
            return loadRoleDialogStaticInfoPicture(unit);
        }

        CBmp64k sourcePortrait = new CBmp64k(INFO_PORTRAIT_WIDTH, INFO_PORTRAIT_HEIGHT);
        unit.renderEquipmentPortrait(null, sourcePortrait, null);
        return sourcePortrait;
    }

    /**
     * Native support extracted from MapVisualObject::CreateRoleDialogPortrait @0041D48D static info-picture load.
     */
    private static CBmp64k loadRoleDialogStaticInfoPicture(CUnit unit) {
        String pictureName = resolveRoleDialogInfoPictureName(unit);
        CBmp64k sourcePortrait = new CBmp64k(Resources.path(GRAPHICS, INFOWINDOW_DIRECTORY, pictureName + BMP_SUFFIX));
        sourcePortrait.mirrorY();
        return sourcePortrait;
    }

    /**
     * Native support extracted from MapVisualObject::CreateRoleDialogPortrait @0041D48D static info-picture name build.
     */
    private static String resolveRoleDialogInfoPictureName(CUnit unit) {
        CUnitInfo info = UnitTypes.getUnitInfo(unit.type);
        return unit.field8_0x28 == 1 ? info.m_InfoPicture : info.m_InfoPicture + unit.field8_0x28;
    }

    /**
     * Native support extracted from MapVisualObject::CreateRoleDialogPortrait @0041D48D render-target composition.
     */
    private static void compositeRoleDialogPortrait(CBmp64k target, CBmp64k sourcePortrait, int unitType) {
        int[] specialPoint = resolveRoleDialogSpecialPortraitPoint(unitType);
        int srcLeft;
        int srcTop;
        int srcRight;
        int srcBottom;
        if (specialPoint == null) {
            srcLeft = ROLE_DIALOG_PORTRAIT_SRC_LEFT;
            srcTop = ROLE_DIALOG_PORTRAIT_SRC_TOP;
            srcRight = ROLE_DIALOG_PORTRAIT_SRC_RIGHT;
            srcBottom = ROLE_DIALOG_PORTRAIT_SRC_BOTTOM;
        } else {
            srcLeft = specialPoint[0];
            srcTop = 0x90 - specialPoint[1];
            srcRight = specialPoint[0] + 0x48;
            srcBottom = 0xF0 - specialPoint[1];
        }

        GUI.tBack.mirrorY();
        try {
            GUI.tBack.drawRectTo(
                    target,
                    ROLE_DIALOG_PORTRAIT_DST_X,
                    ROLE_DIALOG_PORTRAIT_DST_Y,
                    srcLeft,
                    srcTop,
                    srcRight,
                    srcBottom
            );
        } finally {
            GUI.tBack.mirrorY();
        }
        sourcePortrait.drawRectMaskedTo(
                target,
                ROLE_DIALOG_PORTRAIT_DST_X,
                ROLE_DIALOG_PORTRAIT_DST_Y,
                srcLeft,
                srcTop,
                srcRight,
                srcBottom
        );
        GUI.sprTBorder.drawInto(target, 0, 0, 0, 0, false);
    }

    /**
     * Native support extracted from g_ArrayOfPoints lookup in MapVisualObject::CreateRoleDialogPortrait @0041D48D.
     */
    private static int[] resolveRoleDialogSpecialPortraitPoint(int unitType) {
        int index = unitType - ROLE_DIALOG_SPECIAL_PORTRAIT_TYPE_BASE;
        if (index < 0 || index >= ROLE_DIALOG_SPECIAL_PORTRAIT_POINTS.length) {
            return null;
        }

        int[] point = ROLE_DIALOG_SPECIAL_PORTRAIT_POINTS[index];
        return point[1] == -1 ? null : point;
    }

    /**
     * Native support extracted from MapVisualObject::GetOrCreateCUnit @0041D9E1 and the inn-entry update pass
     * triggered by CMainWindow::showInnDialog @0048B885 through GameServer::PrepareInnEntryUnitUpdates @004F312A.
     */
    public CUnit findCUnitByServerId(int normalizedServerId) {
        for (CGameObject object : objects.values()) {
            if (object instanceof CUnit unit && Short.toUnsignedInt(unit.serverID) == normalizedServerId) {
                return unit;
            }
        }
        return null;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2 visual CUnit/CAirUnit creation.
     */
    public static CUnit createVisualUnitForType(int unitTypeId) {
        CUnitInfo unitInfo = UnitTypes.getUnitInfo(unitTypeId);
        if (unitInfo != null && unitInfo.m_ZOffset != 0) {
            return new CAirUnit();
        }
        return new CUnit();
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040D9B2
     * and the `CMap<short,CGameObject*>::FUN_0041FFF0` branch at @0040DE09.
     */
    public CGameObject removeObjectByToken(short objectTokenId) {
        return objects.remove(objectTokenId);
    }

    /**
     * Native support extracted from MapDescriptor::MapDescriptor @004A449C object insertion into
     * `MapVisualObject::m_ObjectMap`.
     */
    public void putScenarioObject(short objectTokenId, CGameObject object) {
        objects.put(objectTokenId, object);
    }

    /**
     * Native support extracted from CMainWindow::runSessionBootstrap @0048C8A3 projectile restore insertion into
     * MapVisualObject object maps.
     */
    public void putObjectByToken(short objectTokenId, CGameObject object) {
        object.m_id = Short.toUnsignedInt(objectTokenId);
        object.pMapVisualObject = this;
        objects.put(objectTokenId, object);
    }

    /**
     * Native support extracted from CMainWindow::runSessionBootstrap @0048C8A3 projectile restore insertion into
     * MapVisualObject::m_ObjectMap2.
     */
    public void putTransientObjectByToken(short objectTokenId, CGameObject object) {
        object.m_id = Short.toUnsignedInt(objectTokenId);
        object.pMapVisualObject = this;
        transientObjects.put(objectTokenId, object);
    }

    /**
     * Native support extracted from CMainWindow::writeCurrentMissionResumeSave @0048DC9F.
     */
    public void writeCurrentMissionResumeObjectState(ResInHeap saveState, int inBattle) {
        writeCurrentMissionResumeSelectionAndGroups(saveState);
        saveState.setInt("Inventory", "IsOpen", hasSelectionPanelChild() ? 1 : 0);
        if (inBattle != 0) {
            writeCurrentMissionResumeProjectiles(saveState);
            writeCurrentMissionResumeFog(saveState);
        }
    }

    /**
     * Native support extracted from the Objects/Selection and Objects/Group%d write block in
     * CMainWindow::writeCurrentMissionResumeSave @0048DC9F.
     */
    private void writeCurrentMissionResumeSelectionAndGroups(ResInHeap saveState) {
        List<Short> selectedObjectIds = new ArrayList<>();
        List<List<Short>> groupObjectIds = new ArrayList<>(10);
        for (int groupIndex = 0; groupIndex < 10; groupIndex++) {
            groupObjectIds.add(new ArrayList<>());
        }

        for (Map.Entry<Short, CGameObject> entry : objects.entrySet()) {
            CGameObject object = entry.getValue();
            if (object.isSelected()) {
                selectedObjectIds.add(entry.getKey());
            }
            for (int groupIndex = 0; groupIndex < 10; groupIndex++) {
                if (object.belongsToGroup(groupIndex) != 0) {
                    groupObjectIds.get(groupIndex).add(entry.getKey());
                }
            }
        }

        saveState.setShortArray("Objects", "Selection", selectedObjectIds);
        for (int groupIndex = 0; groupIndex < 10; groupIndex++) {
            List<Short> objectIds = groupObjectIds.get(groupIndex);
            if (!objectIds.isEmpty()) {
                saveState.setShortArray("Objects", "Group%d".formatted(groupIndex), objectIds);
            }
        }
    }

    /**
     * Native support extracted from the Projectiles/IDs and Prj%d write block in
     * CMainWindow::writeCurrentMissionResumeSave @0048DC9F.
     */
    private void writeCurrentMissionResumeProjectiles(ResInHeap saveState) {
        List<Short> projectileIds = new ArrayList<>();
        saveState.setInt("Projectiles", "FreeIndex", Short.toUnsignedInt(nextTransientObjectToken));
        for (Map.Entry<Short, CGameObject> entry : transientObjects.entrySet()) {
            short projectileId = entry.getKey();
            CGameObject projectile = entry.getValue();
            projectileIds.add(projectileId);
            String sectionName = "Prj%d".formatted(Short.toUnsignedInt(projectileId));
            saveState.setInt(sectionName, "x", projectile.location.x);
            saveState.setInt(sectionName, "y", projectile.location.y);
            saveState.setInt(sectionName, "z", projectile.z);
            saveState.setInt(sectionName, "picture", projectile.type);
            saveState.setInt(sectionName, "dir", projectile.dir);
            saveState.setInt(sectionName, "phase", projectile.phase);
            saveState.setInt(sectionName, "lastaction", projectile.lastAction);
            saveState.setInt(sectionName, "action", projectile.action);
            saveState.setInt(sectionName, "actiondir", projectile.actionDir);
            saveState.setInt(sectionName, "actiontarget", Short.toUnsignedInt(projectile.actionTarget));
            saveState.setInt(sectionName, "actionx", projectile.actionX);
            saveState.setInt(sectionName, "actiony", projectile.actionY);
            saveState.setInt(sectionName, "actionz", projectile.actionZ);
            saveState.setInt(sectionName, "actionphase", projectile.actionPhase);
            saveState.setInt(sectionName, "actionsegments", projectile.actionSegments);
            saveState.setInt(sectionName, "actionspell", projectile.actionSpell);
        }
        saveState.setShortArray("Projectiles", "IDs", projectileIds);
    }

    /**
     * Native support extracted from the Fog/FirstState and Fog/Data write block in
     * CMainWindow::writeCurrentMissionResumeSave @0048DC9F.
     */
    private void writeCurrentMissionResumeFog(ResInHeap saveState) {
        short[] tiles = mapDescriptor.getTilesWxH();
        int tileCount = mapDescriptor.getWidth() * mapDescriptor.getHeight();
        List<Integer> fogRuns = new ArrayList<>();
        int fogState = tiles[0] & 0x8000;
        saveState.setInt("Fog", "FirstState", fogState);
        int runLength = 1;
        for (int tileIndex = 1; tileIndex < tileCount; tileIndex++) {
            int tileFogState = tiles[tileIndex] & 0x8000;
            if (tileFogState == fogState) {
                runLength++;
                continue;
            }
            fogRuns.add(runLength);
            fogState = tileFogState;
            runLength = 1;
        }
        fogRuns.add(runLength);
        saveState.setIntArray("Fog", "Data", fogRuns);
    }

    /**
     * Native support extracted from CUnit::SpawnAttackProjectile @00466E18 and CUnit::SpawnSpellProjectile @0046707E.
     */
    public short addTransientObject(CGameObject object) {
        short key = nextTransientObjectToken;
        nextTransientObjectToken++;
        object.m_id = Short.toUnsignedInt(key);
        object.pMapVisualObject = this;
        transientObjects.put(key, object);
        return key;
    }

    /**
     * Native support extracted from CMainWindow::showMainMenu @0048B569.
     * Clears native `m_ObjectMap` and the native-owned `base.pCUnit` slot.
     */
    public void clearMainMenuObjectRegistry() {
        objects.clear();
        pCUnit = null;
    }

    /**
     * Native support extracted from the StartGameSetupDialogVisualObject close branch in CMainWindow::onDialogClosed @004891D8.
     */
    public void clearStartGameSetupPreviewObjects() {
        if (!objects.isEmpty()) {
            objects.clear();
            pCUnit = null;
        }
    }

    /**
     * Native: MapVisualObject::clearSessionForLobbyReturn @0041CD15 with ALL_PLAYERS.
     * Fully ported support wrapper for the native `ALL_PLAYERS` call sites.
     */
    public void clearSessionForAllPlayersLobbyReturn() {
        clearSessionForLobbyReturn(true);
    }

    /**
     * Native: MapVisualObject::clearSessionForLobbyReturn @0041CD15 with FIRST_PLAYER.
     * Fully ported support wrapper for the native `FIRST_PLAYER` call sites.
     */
    public void clearSessionForFirstPlayerLobbyReturn() {
        clearSessionForLobbyReturn(false);
    }

    /**
     * Native: MapVisualObject::clearSessionForLobbyReturn @0041CD15.
     * Fully ported.
     */
    private void clearSessionForLobbyReturn(boolean allPlayers) {
        pCUnit = null;
        objects.clear();
        transientObjects.clear();
        floatingUnitTexts.clear();
        if (allPlayers) {
            clearNetworkPlayerSlotsForAllPlayersLobbyReturn();
        } else {
            removeExtraNetworkPlayersForLobbyReturn();
        }
        terrainLightOverrideCells.clear();
        mapDescriptor = null;
        drainRemoteGameActions();
        stopGameplayAudioForSessionTeardown();
        GameplayMusicSupport.clearScenarioMusicRecordsForSessionTeardown();
    }

    /**
     * Native: MapVisualObject::cleanupCompletedMissionMapState @0041C897.
     * Fully ported.
     */
    public void cleanupCompletedMissionMapState() {
        Iterator<Map.Entry<Short, CGameObject>> iterator = objects.entrySet().iterator();
        while (iterator.hasNext()) {
            CGameObject object = iterator.next().getValue();
            if (!(object instanceof CUnit unit)
                    || (unit.unitFlags & UNIT_FLAG_HUMANOID) == 0
                    || unit.HP < -10
                    || unit.cPlayer != currentPlayer) {
                iterator.remove();
            } else {
                unit.field51_0x184 = 0;
                unit.action = 0;
                unit.actionSegments = 0;
                unit.setSelected(false);
            }
        }
        pCUnit.setSelected(true);
        transientObjects.clear();
        floatingUnitTexts.clear();
        mapDescriptor = null;
        removeExtraNetworkPlayersForCompletedMissionReturn();
        terrainLightOverrideCells.clear();
        drainRemoteGameActions();
        stopGameplayAudioForSessionTeardown();
        updateSelectionState();
        GameplayMusicSupport.clearScenarioMusicRecordsForSessionTeardown();
    }

    /**
     * Native: MapVisualObject::stopAmbientLoopingSoundsForCleanup @0041BDDD.
     * Fully ported.
     */
    public void stopAmbientLoopingSoundsForCleanup() {
        ambientAudioViewX = -1;
        ambientAudioViewY = -1;
        SoundSystem soundSystem = SoundSystem.get();
        soundSystem.stopAndRewind(SoundManager.SFX_SOUNDS.get(AMBIENT_RIVER.id));
        soundSystem.stopAndRewind(SoundManager.SFX_SOUNDS.get(MAGIC_FIREWALL.id));
    }

    /**
     * Native: MapVisualObject::sendAdjustPlayerGoldAction @0041AAFF.
     * Fully ported.
     */
    public void sendAdjustPlayerGoldAction(int goldDelta) {
        AdjustPlayerGoldAction action = AdjustPlayerGoldAction.prepareForCurrentPlayerGoldDelta(
                resolveGameActionNetID(),
                goldDelta
        );
        CServerApp.sendClientGameAction(action);
    }

    /**
     * Native support extracted from the ALL_PLAYERS branch in MapVisualObject::clearSessionForLobbyReturn @0041CD15.
     */
    private void clearNetworkPlayerSlotsForAllPlayersLobbyReturn() {
        for (int playerIndex = 1; playerIndex < 0x11; playerIndex++) {
            CPlayer player = clientPlayers.get(playerIndex);
            if (player != null && (player.flags & PLAYER_SLOT_ACTIVE_FLAG) != 0) {
                clientPlayers.set(playerIndex, null);
            }
        }
    }

    /**
     * Native support extracted from the FIRST_PLAYER branch in MapVisualObject::clearSessionForLobbyReturn @0041CD15.
     */
    private void removeExtraNetworkPlayersForLobbyReturn() {
        while (clientPlayers.size() > 1) {
            clientPlayers.remove(1);
        }
    }

    /**
     * Native support extracted from MapVisualObject::cleanupCompletedMissionMapState @0041C897 CPlayer list rebuild.
     */
    private void removeExtraNetworkPlayersForCompletedMissionReturn() {
        while (clientPlayers.size() > 1) {
            clientPlayers.remove(1);
        }
        while (clientPlayers.size() <= currentPlayer.playerId) {
            clientPlayers.add(null);
        }
        clientPlayers.set(currentPlayer.playerId, currentPlayer);
    }

    /**
     * Native support extracted from CServerApp::ReadNextGameAction drain loops in
     * MapVisualObject::clearSessionForLobbyReturn @0041CD15 and
     * MapVisualObject::cleanupCompletedMissionMapState @0041C897.
     */
    private static void drainRemoteGameActions() {
        while (CServerApp.readNextClientGameAction() != null) {
        }
    }

    /**
     * Native support extracted from the SoundChannel::Stop and Sound::Unload tails in
     * MapVisualObject::clearSessionForLobbyReturn @0041CD15 and
     * MapVisualObject::cleanupCompletedMissionMapState @0041C897.
     * Fully ported through the Java SoundSystem backend.
     */
    private static void stopGameplayAudioForSessionTeardown() {
        SoundSystem soundSystem = SoundSystem.get();
        soundSystem.stopAllChannels();
        soundSystem.releaseSound(SoundManager.SFX_SOUNDS.get(MCOMPLET.id));
        soundSystem.releaseSound(SoundManager.SFX_SOUNDS.get(MFAILED.id));
    }

    /**
     * Native support for `*(word *)(*(MapVisualObject + 0x9CC) + 4)` in
     * MapVisualObject::requestMapLoad @0041C50D, MapVisualObject::applyWimpyMode @0041A4EF,
     * MapVisualObject::applyFormationMode @0041A617, MapVisualObject::applyAutoCasting @0041A5B9,
     * MapVisualObject::SendChatTextAction @0041ACE5, MapVisualObject::submitCharacterSetup @0041C5F5,
     * MapVisualObject::uploadCharacterFile @0041C6B5, and MapVisualObject::sendMapLoadComplete @0041C79A.
     */
    public int resolveGameActionNetID() {
        return currentPlayer.playerId;
    }

    /**
     * Native support: `CArray<CPlayer>::GetAt` branch in MapVisualObject::SendChatTextAction @0041ACE5.
     */
    private int resolveChatTargetPlayerId(int recipientPlayerIndex) {
        if (recipientPlayerIndex == 0) {
            return 0;
        }
        return clientPlayers.get(recipientPlayerIndex).playerId;
    }

    /**
     * Native: MapVisualObject::RenderFrame @00406F43.
     * Fully ported.
     */
    private void renderFrame() {
        if (GAMEPLAY.isUnsetIn(Globals.mainWindow.dialogsMask) || mapDescriptor == null) {
            return;
        }

        renderFrameCounter++;
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        clampViewToMapBounds();
        clampPendingCameraDeltaToMapBounds();
        refreshAmbientAudio();

        if (Math.abs(pendingCameraDeltaX) >= gridWidth / 2 || Math.abs(pendingCameraDeltaY) >= gridHeight / 2) {
            renderFrameDirty = 1;
        }
        view.x += pendingCameraDeltaX;
        view.y += pendingCameraDeltaY;
        if (pendingCameraDeltaX != 0 || pendingCameraDeltaY != 0) {
            updateSoundSystemMapAudioView(SoundSystem.get());
        }

        boolean viewChanged = view.x != lastRenderedViewX || view.y != lastRenderedViewY;
        rebuildTileHeightRenderGrids();
        rebuildMapRenderState();
        dirtyRenderRect.intersect(screenRect);
        if (viewChanged) {
            dirtyRenderRect.set(screenRect);
        }
        if (areaEffectRefreshPending != 0) {
            dirtyRenderRect.set(screenRect);
            areaEffectRefreshPending = 0;
        }

        boolean animatedTerrainFrame = mapAnimationTick - lastAnimatedTerrainTick > 3;
        if (animatedTerrainFrame) {
            lastAnimatedTerrainTick = mapAnimationTick;
        }
        CVisualObject inventoryPanel = getChildById(2);
        CVisualObject spellPanel = getChildById(3);
        CRect panelDirtyRect = collectPanelDirtyRect(inventoryPanel, spellPanel);
        int panelLayoutSignature = panelLayoutSignature(inventoryPanel, spellPanel);
        if (panelLayoutSignature != lastPanelLayoutSignature || hasAnimatedVisibleCells(inventoryPanel)) {
            dirtyRenderRect.set(screenRect);
        }

        Globals.mousePointer.update();
        updateRenderRingOriginForNativeState();
        drawModeledMapFrame(screenRect);
        Globals.mousePointer.update();
        drawRightPanelLeftChrome(screenRect);
        if (!Globals.mainWindow.pChatVisualObject.suppressesGameListDraw()) {
            if (Globals.mainWindow.sessionMode != CMainWindow.SESSION_MODE_DEDICATED_SERVER) {
                Globals.mainWindow.pChatVisualObject.refreshDefaultGameListLayout();
            }
            gameListControl.draw();
        }
        updateRenderStats();
        drawRenderStatsOverlay(screenRect);
        drawNetworkStatsOverlay(screenRect);
        super.update();
        Globals.mousePointer.update();
        selectCursor();

        updateRenderRegions(panelLayoutSignature, panelDirtyRect, screenRect);
        renderFrameDirty = 0;
        pendingCameraDeltaX = 0;
        pendingCameraDeltaY = 0;
        lastRenderedViewX = view.x;
        lastRenderedViewY = view.y;
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 panel dirty-union block.
     */
    private CRect collectPanelDirtyRect(CVisualObject inventoryPanel, CVisualObject spellPanel) {
        CRect panelDirtyRect = new CRect(0, 0, 0, 0);
        if (inventoryPanel != null) {
            panelDirtyRect.unionWith(inventoryPanel.getRect());
        }
        if (spellPanel != null) {
            panelDirtyRect.unionWith(spellPanel.getRect());
        }
        return panelDirtyRect;
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 lastPanelLayoutSignature computation.
     */
    private int panelLayoutSignature(CVisualObject inventoryPanel, CVisualObject spellPanel) {
        return System.identityHashCode(inventoryPanel)
                + System.identityHashCode(spellPanel)
                + gameListControl.getSize();
    }

    /**
     * Native support extracted from GridOverlayVisualObject::HasAnimatedVisibleCells call in MapVisualObject::RenderFrame @00406F43.
     */
    private static boolean hasAnimatedVisibleCells(CVisualObject visualObject) {
        return visualObject instanceof GridOverlayVisualObject gridOverlay
                && gridOverlay.hasAnimatedVisibleCells != 0;
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 render-ring origin updates.
     */
    private void updateRenderRingOriginForNativeState() {
        if (renderFrameDirty != 0) {
            renderRingOriginX = 0;
            renderRingOriginY = 0;
            return;
        }
        if (pendingCameraDeltaX == 0 && pendingCameraDeltaY == 0) {
            return;
        }

        renderRingOriginX = wrapRenderRingOrigin(renderRingOriginX + pendingCameraDeltaX, gridWidth);
        renderRingOriginY = wrapRenderRingOrigin(renderRingOriginY + pendingCameraDeltaY, gridHeight);
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 render-ring wrapping.
     */
    private static int wrapRenderRingOrigin(int value, int size) {
        if (size == 0) {
            return 0;
        }
        int wrapped = value % size;
        return wrapped < 0 ? wrapped + size : wrapped;
    }

    /**
     * Native support extracted from CArray<FloatingUnitText>::drawAllAndRemoveExpired @0045C756.
     * Full port. Java's iterator removal covers the native RemoveAt/index-decrement loop.
     */
    private void drawFloatingUnitTexts() {
        if (floatingUnitTexts.isEmpty()) {
            return;
        }

        Globals.renderer.lockSurface();
        try {
            Iterator<FloatingUnitText> iterator = floatingUnitTexts.iterator();
            while (iterator.hasNext()) {
                FloatingUnitText floatingUnitText = iterator.next();
                if (!floatingUnitText.drawIfAlive()) {
                    iterator.remove();
                }
            }
        } finally {
            Globals.renderer.unlockSurface();
        }
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 render-stats overlay branch.
     */
    private void drawRenderStatsOverlay(CRect screenRect) {
        if (Globals.showRenderStats == 0) {
            return;
        }
        Globals.renderer.fillScreenRect(
                screenRect.right - 0x78,
                screenRect.top,
                screenRect.right - 0x1E,
                screenRect.top + 0x18,
                RGB16.from(8, 8, 8).val()
        );
        Globals.fonts.font1.drawTextShadowed(
                screenRect.right - 0x26,
                screenRect.top,
                String.format(Locale.ROOT, "%.1f fps", renderStatsFps),
                TextAlign.RIGHT.mask,
                Palettes.gray,
                1
        );
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 network-stats overlay branch.
     * Fully ported.
     */
    private void drawNetworkStatsOverlay(CRect screenRect) {
        if (Globals.showNetworkStats == 0
                || Globals.mainWindow.sessionMode != CMainWindow.SESSION_MODE_MULTIPLAYER_CLIENT) {
            return;
        }
        CBufferManager client = CServerApp.getRemoteClientByNetId(0);
        if (client == null) {
            return;
        }
        ClientTrafficStats trafficStats = CServerApp.getRemoteClientTrafficStats(client.GetIPAddress());
        if (trafficStats == null) {
            return;
        }

        int averageBytes = 0;
        if (trafficStats.sampleCount != 0) {
            averageBytes = trafficStats.totalBytes / trafficStats.sampleCount;
        }

        Globals.renderer.fillScreenRect(
                screenRect.right - 0xB4,
                screenRect.top + 0x1E,
                screenRect.right - 0x1E,
                screenRect.top + 0x69,
                RGB16.from(8, 8, 8).val()
        );
        Globals.fonts.font1.drawTextShadowed(
                screenRect.right - 0x69,
                screenRect.top + 0x23,
                get(PATCH, PatchText.TRAFFIC_IN_74),
                TextAlign.CENTER.mask,
                Palettes.gray,
                1
        );
        Globals.fonts.font1.drawTextShadowed(
                screenRect.right - 0xAF,
                screenRect.top + 0x34,
                get(PATCH, PatchText.CUR_75) + " " + trafficStats.lastIntervalBytes + " " + get(PATCH, PatchText.BPS_61),
                TextAlign.DEFAULT.mask,
                Palettes.gray,
                1
        );
        Globals.fonts.font1.drawTextShadowed(
                screenRect.right - 0xAF,
                screenRect.top + 0x45,
                get(PATCH, PatchText.AVG_76) + " " + averageBytes + " " + get(PATCH, PatchText.BPS_61),
                TextAlign.DEFAULT.mask,
                Palettes.gray,
                1
        );
        Globals.fonts.font1.drawTextShadowed(
                screenRect.right - 0xAF,
                screenRect.top + 0x56,
                get(PATCH, PatchText.MAX_77) + " " + trafficStats.peakIntervalBytes + " " + get(PATCH, PatchText.BPS_61),
                TextAlign.DEFAULT.mask,
                Palettes.gray,
                1
        );
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 dirty-region update tail.
     */
    private void updateRenderRegions(int panelLayoutSignature, CRect panelDirtyRect, CRect screenRect) {
        if (panelLayoutSignature == lastPanelLayoutSignature) {
            CRect remainingDirtyRect = new CRect();
            remainingDirtyRect.subtract(dirtyRenderRect, panelDirtyRect);
            dirtyRenderRect.set(remainingDirtyRect);
            if (!panelDirtyRect.isEmpty()) {
                updateRenderRegion(panelDirtyRect);
            }
        }
        lastPanelLayoutSignature = panelLayoutSignature;
        updateRenderRegion(dirtyRenderRect);
        if (Globals.showRenderStats != 0) {
            updateRenderRegion(new CRect(screenRect.right - 0x78, screenRect.top, screenRect.right - 0x1E, screenRect.top + 0x18));
        }
        if (Globals.showNetworkStats != 0 && Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_MULTIPLAYER_CLIENT) {
            updateRenderRegion(new CRect(screenRect.right - 0xB4, screenRect.top + 0x1E, screenRect.right - 0x1E, screenRect.top + 0x69));
        }
    }

    /**
     * Native support extracted from PresentRenderRegion calls in MapVisualObject::RenderFrame @00406F43.
     */
    private static void updateRenderRegion(@SuppressWarnings("unused") CRect rect) {
        // Java presents the renderer's full software surface once per idle tick.
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 right-panel left chrome.
     * Fully ported. Java uses the scalable gameplay-sidebar layout with the recovered native left-side assets.
     */
    private void drawRightPanelLeftChrome(CRect screenRect) {
        RightPanelLayout rightPanelLayout = RightPanelLayout.forScreenHeight(Globals.screenRect.bottom);
        int x = screenRect.right - RightPanelLayout.LEFT_STRIP_WIDTH;
        int y = screenRect.top;

        drawRepeatedRightPanelLeftFill(rightPanelLayout, x, y);
        drawRightPanelLeftStrip(GUI.crystalL, x, y + rightPanelLayout.minimapTop, RightPanelLayout.MINIMAP_HEIGHT);

        CBmp64k orderToolbarLeftStrip = selectedCount == 0
                || (selectionFlags & (FLAG_BUSY | FLAG_STRUCTURE)) != 0
                ? GUI.headsL
                : GUI.commandBarL;
        drawRightPanelLeftStrip(orderToolbarLeftStrip, x, y + rightPanelLayout.orderToolbarTop, RightPanelLayout.ORDER_TOOLBAR_HEIGHT);

        CBmp64k selectionInfoLeftStrip = Globals.mainWindow.pSelectionInfoPanelVisualObject.selectionInfoViewMode0x70 == 0
                ? GUI.textBackL
                : GUI.humanBackL;
        drawRightPanelLeftStrip(selectionInfoLeftStrip, x, y + rightPanelLayout.portraitTop, RightPanelLayout.PORTRAIT_PANEL_HEIGHT);
        if (rightPanelLayout.hasStatusInfoPanel()) {
            drawRightPanelLeftStrip(GUI.textBackL, x, y + rightPanelLayout.statusInfoTop, RightPanelLayout.INFO_PANEL_HEIGHT);
        }
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 extra800L/extra1024L filler branches.
     * Fully ported. Java keeps the native 1024px filler at 768px height, then repeats the taller 800px tile above it.
     */
    private static void drawRepeatedRightPanelLeftFill(RightPanelLayout rightPanelLayout, int x, int y) {
        CBmp64k fillBitmap;
        if (rightPanelLayout.usesHighResolutionArt()) {
            fillBitmap = rightPanelLayout.usesTallExtraFillArt() ? GUI.extraLeft800 : GUI.extraLeft1024;
        } else if (rightPanelLayout.usesMediumResolutionArt()) {
            fillBitmap = GUI.extraLeft800;
        } else {
            return;
        }
        drawRepeatedRightPanelLeftFill(rightPanelLayout, fillBitmap, x, y);
    }

    /**
     * not ported. Java helper for clipping repeated right-panel left filler to the active responsive tier area.
     */
    private static void drawRepeatedRightPanelLeftFill(RightPanelLayout rightPanelLayout, CBmp64k fillBitmap, int x, int y) {
        int tileHeight = fillBitmap.ySizeOf(0);
        int fillBottom = rightPanelLayout.extraFillBottom();
        for (int fillTop = rightPanelLayout.extraFillTop; fillTop < fillBottom; fillTop += tileHeight) {
            int height = Math.min(tileHeight, fillBottom - fillTop);
            drawRightPanelLeftStrip(fillBitmap, x, y + fillTop, height);
        }
    }

    /**
     * Native support extracted from DrawRectMasked calls in MapVisualObject::RenderFrame @0040A6CB..0040A8F7.
     */
    private static void drawRightPanelLeftStrip(CBmp64k bitmap, int x, int y, int height) {
        bitmap.drawRectMasked(x, y, 0, 0, RightPanelLayout.LEFT_STRIP_WIDTH, height);
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43.
     */
    private void clampViewToMapBounds() {
        int maxX = maxCameraX();
        int maxY = maxCameraY();
        if (view.x < MAP_CAMERA_EDGE_TILES) {
            view.x = MAP_CAMERA_EDGE_TILES;
        } else if (view.x > maxX) {
            view.x = maxX;
        }
        if (view.y < MAP_CAMERA_EDGE_TILES) {
            view.y = MAP_CAMERA_EDGE_TILES;
        } else if (view.y > maxY) {
            view.y = maxY;
        }
    }

    /**
     * Native: MapVisualObject::RebuildMapRenderState @0040403B.
     * Fully ported.
     */
    private void rebuildMapRenderState() {
        dirtyRenderRect.set(0, 0, 0, 0);
        clearObjectLayers();
        if (TerrainGraphics.terrainGraphicsFlags == 2) {
            copyByteGrid(dynamicLightOverrideGrid, previousDynamicLightOverrideGrid);
        }
        fillByteGrid(dynamicLightOverrideGrid, (byte) 0xFF);
        dynamicLightCellCount = 0;
        for (CGameObject object : objects.values()) {
            object.refreshMapDerivedState();
            object.updateMapLayer();
            object.updateMapOverlay();
        }

        rebuildTileSlopeModeGrid();
        copyIntGrid(tileSlopeModeGrid, previousTileSlopeModeGrid);
        for (CGameObject object : transientObjects.values()) {
            object.refreshMapDerivedState();
            object.updateMapOverlay();
        }
        markTransientEffectDirtyRegions();
        markTerrainLightOverrideDirtyRegions();
        markFloatingUnitTextDirtyRegions();
        rebuildTileBrightnessGrid();
    }

    /**
     * Native support extracted from MapVisualObject::RebuildMapRenderState @0040403B transient-effect dirty pass.
     */
    private void markTransientEffectDirtyRegions() {
        for (Map.Entry<Integer, Integer> entry : transientEffectCells.entrySet()) {
            int packedTile = entry.getKey();
            int tileX = packedTile & 0xFF;
            int tileY = (packedTile >>> 8) & 0xFF;
            int brightness = transientEffectDynamicBrightness(entry.getValue());
            if (brightness >= 0) {
                applyDynamicLightOverride(tileX, tileY, 1, (byte) brightness);
            }
            if (Globals.gamePreferences.animation == 0) {
                int localX = tileX - view.x;
                int localY = tileY - view.y;
                dirtyRenderRect.unionWith(new CRect(
                        (localX - 1) * TILE_SCREEN_SIZE,
                        (localY - 2) * TILE_SCREEN_SIZE,
                        (localX + 2) * TILE_SCREEN_SIZE,
                        (localY + 2) * TILE_SCREEN_SIZE
                ));
            }
        }
    }

    /**
     * Native support extracted from MapVisualObject::RebuildMapRenderState @0040403B transient-effect phase switch.
     */
    private static int transientEffectDynamicBrightness(int phase) {
        return switch (phase >> 1) {
            case 0, 10 -> 0x24;
            case 1 -> 0x18;
            case 2, 4, 6 -> 0x0C;
            case 3, 5, 7 -> 0;
            case 8 -> 0x14;
            case 9 -> 0x1C;
            default -> -1;
        };
    }

    /**
     * Native support extracted from MapVisualObject::RebuildMapRenderState @0040403B terrain-light dirty pass.
     */
    private void markTerrainLightOverrideDirtyRegions() {
        for (Map.Entry<Integer, Integer> entry : terrainLightOverrideCells.entrySet()) {
            if (entry.getValue() == 0) {
                continue;
            }
            int packedTile = entry.getKey();
            int tileX = packedTile & 0xFF;
            int tileY = (packedTile >>> 8) & 0xFF;
            if (!isObjectLayerWorldCell(tileX, tileY)) {
                continue;
            }
            int localX = tileX - view.x;
            int localY = tileY - view.y;
            if (Globals.gamePreferences.lighting == 0) {
                dirtyRenderRect.unionWith(new CRect(
                        (localX - 1) * TILE_SCREEN_SIZE,
                        (localY - 3) * TILE_SCREEN_SIZE,
                        (localX + 2) * TILE_SCREEN_SIZE,
                        (localY + 2) * TILE_SCREEN_SIZE
                ));
            } else {
                dirtyRenderRect.unionWith(new CRect(
                        (localX - 3) * TILE_SCREEN_SIZE,
                        (localY - 3) * TILE_SCREEN_SIZE,
                        (localX + 4) * TILE_SCREEN_SIZE,
                        (localY + 4) * TILE_SCREEN_SIZE
                ));
            }
        }
    }

    /**
     * Native support extracted from MapVisualObject::RebuildMapRenderState @0040403B floating-text dirty pass.
     */
    private void markFloatingUnitTextDirtyRegions() {
        for (FloatingUnitText floatingUnitText : floatingUnitTexts) {
            int screenX = floatingUnitText.screenX();
            int screenY = floatingUnitText.screenY();
            dirtyRenderRect.unionWith(new CRect(
                    (screenX - 0x30) & 0xFFFFFFE0,
                    screenY - 0x30,
                    ((screenX + 0x30) & 0xFFFFFFE0) + TILE_SCREEN_SIZE,
                    screenY + 0x30
            ));
        }
    }

    /**
     * Native: MapVisualObject::RebuildTileHeightRenderGrids @004065B1.
     * Fully ported.
     */
    private void rebuildTileHeightRenderGrids() {
        if (mapDescriptor.heightsWxH.length == 0 || cachedMapWidth == 0) {
            return;
        }

        for (int row = -RENDER_GRID_TOP_GUARD; row <= gridHeight + 7; row++) {
            for (int col = -RENDER_GRID_LEFT_GUARD; col <= gridWidth + 3; col++) {
                int worldX = view.x + col;
                int worldY = view.y + row;
                screenYVertexGrid[terrainVertexGridX(col)][terrainVertexGridY(row)] =
                        row * TILE_SCREEN_SIZE - mapHeightAt(worldX, worldY);
            }
        }

        for (int row = -RENDER_GRID_TOP_GUARD; row < gridHeight + 7; row++) {
            for (int col = -RENDER_GRID_LEFT_GUARD; col < gridWidth + 3; col++) {
                int vertexGridX = terrainVertexGridX(col);
                int vertexGridY = terrainVertexGridY(row);
                int objectGridX = objectLayerGridX(col);
                int objectGridY = objectLayerGridY(row);
                screenYRowTopGrid[objectGridX][objectGridY] = Math.min(
                        screenYVertexGrid[vertexGridX][vertexGridY],
                        screenYVertexGrid[vertexGridX + 1][vertexGridY]
                );
                screenYRowBottomGrid[objectGridX][objectGridY] = Math.max(
                        screenYVertexGrid[vertexGridX][vertexGridY + 1],
                        screenYVertexGrid[vertexGridX + 1][vertexGridY + 1]
                );
            }
        }

        for (int row = -RENDER_GRID_TOP_GUARD; row < gridHeight + 9; row++) {
            if (view.y + row >= cachedMapHeight) {
                continue;
            }
            for (int col = -RENDER_GRID_LEFT_GUARD; col < gridWidth + 5; col++) {
                int baseX = view.x + col - 1;
                int baseY = view.y + row - 1;
                int sum = mapHeightAt(baseX, baseY)
                        + mapHeightAt(baseX + 1, baseY)
                        + mapHeightAt(baseX, baseY + 1)
                        + mapHeightAt(baseX + 1, baseY + 1);
                tileAverageHeightGrid[tileAverageGridX(col)][tileAverageGridY(row)] = arithmeticDivideByFour(sum);
            }
        }
    }

    /**
     * Native support extracted from FUN_0040403B @0040403B.
     */
    private void rebuildTileSlopeModeGrid() {
        fillIntGrid(tileSlopeModeGrid, 0);
        if (mapDescriptor.tilesWxH.length == 0 || cachedMapWidth == 0) {
            return;
        }

        for (int row = -RENDER_GRID_TOP_GUARD; row <= gridHeight + 7; row++) {
            for (int col = -RENDER_GRID_LEFT_GUARD; col <= gridWidth + 3; col++) {
                int worldX = view.x + col;
                int worldY = view.y + row;
                if (!isMapCell(worldX, worldY)) {
                    continue;
                }
                int mode = switch (mapDescriptor.tileWordAt(worldX, worldY) & TERRAIN_VERTEX_BLOCKED_MASK) {
                    case TERRAIN_VERTEX_BLOCKED_MASK -> TILE_SLOPE_BLOCKED;
                    case 0x8000 -> TILE_SLOPE_DOWN;
                    default -> TILE_SLOPE_UP;
                };
                if (mode == TILE_SLOPE_BLOCKED) {
                    markBlockedSlopeDirtyRegion(col, row);
                }
                tileSlopeModeGrid[terrainVertexGridX(col)][terrainVertexGridY(row)] = mode;
            }
        }
    }

    /**
     * Native support extracted from MapVisualObject::RebuildMapRenderState @0040403B blocked-slope dirty rectangles.
     */
    private void markBlockedSlopeDirtyRegion(int col, int row) {
        int objectGridX = objectLayerGridX(Math.min(col, gridWidth + 2));
        int objectGridY = objectLayerGridY(Math.min(row, gridHeight + 6));
        if (Globals.gamePreferences.animation == 0) {
            if (mapOccupancyDirty != 0) {
                dirtyRenderRect.unionWith(new CRect(
                        (col - 1) * TILE_SCREEN_SIZE,
                        screenYRowTopGrid[objectGridX][objectGridY] - 0x60,
                        (col + 2) * TILE_SCREEN_SIZE,
                        screenYRowBottomGrid[objectGridX][objectGridY] + 0x60
                ));
            }
        } else {
            dirtyRenderRect.unionWith(new CRect(
                    (col - 3) * TILE_SCREEN_SIZE,
                    screenYRowTopGrid[objectGridX][objectGridY] - 0x60,
                    (col + 3) * TILE_SCREEN_SIZE,
                    screenYRowBottomGrid[objectGridX][objectGridY] + 0x60
            ));
        }
    }

    /**
     * Native: MapVisualObject::RebuildTileBrightnessGrid @00404A2A.
     * Fully ported.
     */
    private void rebuildTileBrightnessGrid() {
        int baseBrightness = Globals.lighting.brightness;
        byte defaultBrightness = (byte) (baseBrightness >> 2);
        fillByteGrid(tileBrightnessGrid, defaultBrightness);
        for (Map.Entry<Integer, Integer> entry : terrainLightOverrideCells.entrySet()) {
            int packedTile = entry.getKey();
            int tileX = packedTile & 0xFF;
            int tileY = (packedTile >>> 8) & 0xFF;
            if (!isObjectLayerWorldCell(tileX, tileY)) {
                continue;
            }

            int gridX = objectLayerGridX(tileX - view.x);
            int gridY = objectLayerGridY(tileY - view.y);
            int flags = entry.getValue();
            if ((flags & 0x8000) != 0) {
                tileBrightnessGrid[gridX][gridY] = 0;
            }
            if ((flags & 0x4000) != 0) {
                tileBrightnessGrid[gridX][gridY] = 0x0C;
            }
            if ((flags & 0x08) != 0) {
                int phaseValue = Math.abs(mapAnimationTick / 2 + tileX * tileY);
                byte brightness = (byte) (((phaseValue % 5) & 1) == 0 ? 0 : 0x0C);
                applyDynamicLightOverride(tileX, tileY, 1, brightness);
            }
        }

        for (int row = 0; row < gridHeight + 10; row++) {
            for (int col = 0; col < gridWidth + 6; col++) {
                int topLeft = Byte.toUnsignedInt(dynamicLightOverrideGrid[col][row]) - DYNAMIC_LIGHT_NATIVE_OFFSET;
                int topRight = Byte.toUnsignedInt(dynamicLightOverrideGrid[col + 1][row]) - DYNAMIC_LIGHT_NATIVE_OFFSET;
                int bottomLeft = Byte.toUnsignedInt(dynamicLightOverrideGrid[col][row + 1]) - DYNAMIC_LIGHT_NATIVE_OFFSET;
                int bottomRight = Byte.toUnsignedInt(dynamicLightOverrideGrid[col + 1][row + 1]) - DYNAMIC_LIGHT_NATIVE_OFFSET;
                topLeft = Math.max(topLeft, 0);
                topRight = Math.max(topRight, 0);
                bottomLeft = Math.max(bottomLeft, 0);
                bottomRight = Math.max(bottomRight, 0);
                if (topLeft + topRight + bottomLeft + bottomRight == 0x37C) {
                    continue;
                }
                if (topLeft == 0xDF) {
                    topLeft = baseBrightness;
                }
                if (topRight == 0xDF) {
                    topRight = baseBrightness;
                }
                if (bottomLeft == 0xDF) {
                    bottomLeft = baseBrightness;
                }
                if (bottomRight == 0xDF) {
                    bottomRight = baseBrightness;
                }
                int brightness = baseBrightness >> 2;
                int dynamicBrightness = (topLeft + topRight + bottomLeft + bottomRight) >> 4;
                if (dynamicBrightness < brightness) {
                    brightness = dynamicBrightness;
                }
                tileBrightnessGrid[col][row] = (byte) brightness;
            }
        }
    }

    /**
     * Native: MapVisualObject::ApplyDynamicLightOverride @0040631E.
     * Fully ported.
     */
    public void applyDynamicLightOverride(int tileX, int tileY, int radius, byte brightness) {
        if (Globals.gamePreferences.animation == 0) {
            dirtyRenderRect.unionWith(new CRect(
                    ((tileX - view.x - 1) - radius) * TILE_SCREEN_SIZE,
                    ((tileY - view.y - 1) - radius) * TILE_SCREEN_SIZE,
                    ((tileX - view.x + 2) + radius) * TILE_SCREEN_SIZE,
                    ((tileY - view.y + 2) + radius) * TILE_SCREEN_SIZE
            ));
        }
        if (Globals.gamePreferences.lighting == 0) {
            return;
        }

        int limit = radius == 0 ? 1 : radius * (radius + 1);
        int localX = tileX - view.x;
        int localY = tileY - view.y;
        for (int i = 0; i <= radius; i++) {
            for (int j = 0; j <= radius; j++) {
                if (i * i + j * j >= limit) {
                    continue;
                }
                writeDynamicLightCell(localX + 4 + i, localY + 4 + j, brightness);
                writeDynamicLightCell(localX + 4 + i, localY + 3 - j, brightness);
                writeDynamicLightCell(localX + 3 - i, localY + 4 + j, brightness);
                writeDynamicLightCell(localX + 3 - i, localY + 3 - j, brightness);
            }
        }
    }

    /**
     * Native support extracted from MapVisualObject::ApplyDynamicLightOverride @0040631E grid writes.
     */
    private void writeDynamicLightCell(int x, int y, byte brightness) {
        if (x > 2 && x <= gridWidth + 4 && y > 2 && y <= gridHeight + 8) {
            dynamicLightOverrideGrid[x][y] = brightness;
            dynamicLightCellCount++;
        }
    }

    /**
     * Native support extracted from CGameObject::MarkObjectLayerCell @00460591.
     */
    public void markObjectLayerCell(int layerKind, CGameObject owner, CGameObject object) {
        CGameObject[][] layer = layerForKind(layerKind);
        if (layerKind == MAP_LAYER_STRUCTURE) {
            for (int x = owner.mapBoundsLeft; x <= owner.mapBoundsRight; x++) {
                for (int y = owner.mapBoundsTop; y <= owner.mapBoundsBottom; y++) {
                    layer[objectLayerGridX(x)][objectLayerGridY(y)] = object;
                }
            }
            return;
        }

        int x = ((owner.location2.x - PROJECTILE_CENTER_OFFSET + owner.getTileWidth() * PROJECTILE_CENTER_OFFSET) >> 8)
                - view.x;
        int y = ((owner.location2.y - PROJECTILE_CENTER_OFFSET + owner.getTileHeight() * PROJECTILE_CENTER_OFFSET) >> 8)
                - view.y;
        if (isObjectLayerCell(x, y)) {
            layer[objectLayerGridX(x)][objectLayerGridY(y)] = object;
        }
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43.
     * Java-only zoom addition: the native draw sequence still runs in 32-pixel logical map coordinates, but it now draws
     * into a private BGRA target and scales that target into the physical screen rectangle. This is intentional
     * non-native behavior and must not be removed merely because native code drew directly to the screen surface.
     */
    private void drawModeledMapFrame(CRect screenRect) {
        ensureJavaZoomMapFrameBuffer();
        clearJavaZoomMapFrameBuffer();

        Globals.renderer.lockSurface();
        try {
            Globals.renderer.pushJavaRenderTarget(javaZoomMapFrameBgra, javaZoomMapFrameWidth, javaZoomMapFrameHeight);
            try {
                Globals.renderer.pushClip(0, 0, javaZoomMapFrameWidth, javaZoomMapFrameHeight);
                try {
                    drawFullTerrainFrame();
                    if (dynamicLightCellCount != 0
                            && (TerrainGraphics.terrainGraphicsFlags & 0x2) == 0
                            && Globals.gamePreferences.lighting != 0) {
                        drawDynamicLightTerrainCells();
                    }
                    drawObjectLayerPasses();
                    drawFloatingUnitTexts();
                } finally {
                    Globals.renderer.popClip();
                }
            } finally {
                Globals.renderer.popJavaRenderTarget();
            }

            Globals.renderer.pushClip(screenRect.left, screenRect.top, screenRect.right, screenRect.bottom);
            try {
                CRect scaledMapRect = javaZoomScaledMapRect(screenRect);
                Globals.renderer.fillScreenRect(
                        screenRect.left,
                        screenRect.top,
                        screenRect.right,
                        screenRect.bottom,
                        RGB16.BLACK.val()
                );
                Globals.renderer.blitBgraScaled(
                        javaZoomMapFrameBgra,
                        javaZoomMapFrameWidth,
                        javaZoomMapFrameHeight,
                        scaledMapRect.left,
                        scaledMapRect.top,
                        scaledMapRect.width(),
                        scaledMapRect.height()
                );
            } finally {
                Globals.renderer.popClip();
            }
        } finally {
            Globals.renderer.unlockSurface();
            Globals.renderer.refreshMousePointer();
        }
    }

    /**
     * Native: MapVisualObject::DrawFullTerrainFrame @00404E0E.
     * Fully ported.
     */
    private void drawFullTerrainFrame() {
        for (int row = 0; row < gridHeight + 4; row++) {
            for (int col = gridWidth - 1; col >= 0; col--) {
                int worldX = col + view.x;
                int worldY = row + view.y;
                drawTerrainCell(
                        col,
                        row,
                        mapDescriptor.terrainLightAt(worldX, worldY),
                        mapDescriptor.terrainLightAt(worldX + 1, worldY),
                        mapDescriptor.terrainLightAt(worldX, worldY + 1),
                        mapDescriptor.terrainLightAt(worldX + 1, worldY + 1),
                        col,
                        0,
                        false,
                        false,
                        true,
                        true
                );
            }
        }
    }

    /**
     * Native: MapVisualObject::DrawAnimatedTerrainRingSegment @0040538D.
     * Fully ported.
     */
    private void drawAnimatedTerrainRingSegment(int yRingOffsetTiles) {
        int yPixelOffset = (renderRingOriginY + yRingOffsetTiles) * TILE_SCREEN_SIZE;
        for (int row = 0; row < gridHeight + 4; row++) {
            for (int col = gridWidth - 1; col >= 0; col--) {
                int screenCol = col + renderRingOriginX;
                if (screenCol >= gridWidth) {
                    screenCol -= gridWidth;
                }
                int worldX = col + view.x;
                int worldY = row + view.y;
                int animatedCornerFlags = mapDescriptor.tileWordAt(worldX, worldY) & TERRAIN_VERTEX_BLOCKED_MASK
                        | mapDescriptor.tileWordAt(worldX + 1, worldY) & TERRAIN_VERTEX_BLOCKED_MASK
                        | mapDescriptor.tileWordAt(worldX, worldY + 1) & TERRAIN_VERTEX_BLOCKED_MASK
                        | mapDescriptor.tileWordAt(worldX + 1, worldY + 1) & TERRAIN_VERTEX_BLOCKED_MASK;
                if (animatedCornerFlags == TERRAIN_VERTEX_BLOCKED_MASK) {
                    drawTerrainCell(
                            col,
                            row,
                            mapDescriptor.terrainLightAt(worldX, worldY),
                            mapDescriptor.terrainLightAt(worldX + 1, worldY),
                            mapDescriptor.terrainLightAt(worldX, worldY + 1),
                            mapDescriptor.terrainLightAt(worldX + 1, worldY + 1),
                            screenCol,
                            yPixelOffset,
                            true,
                            true,
                            false,
                            false
                    );
                }
            }
        }
    }

    /**
     * Native: MapVisualObject::DrawScrolledTerrainSegment @00405861.
     * Fully ported.
     */
    private void drawScrolledTerrainSegment(int yRingOffsetTiles) {
        int yPixelOffset = (renderRingOriginY + yRingOffsetTiles) * TILE_SCREEN_SIZE;
        for (int row = 0; row < gridHeight + 4; row++) {
            for (int col = gridWidth - 1; col >= 0; col--) {
                int screenCol = col + renderRingOriginX;
                if (screenCol >= gridWidth) {
                    screenCol -= gridWidth;
                }
                int worldX = col + view.x;
                int worldY = row + view.y;
                drawTerrainCell(
                        col,
                        row,
                        mapDescriptor.terrainLightAt(worldX, worldY),
                        mapDescriptor.terrainLightAt(worldX + 1, worldY),
                        mapDescriptor.terrainLightAt(worldX, worldY + 1),
                        mapDescriptor.terrainLightAt(worldX + 1, worldY + 1),
                        screenCol,
                        yPixelOffset,
                        false,
                        false,
                        true,
                        true
                );
            }
        }
    }

    /**
     * Native: MapVisualObject::DrawDynamicLightTerrainCells @00405CF4.
     * Fully ported.
     */
    private void drawDynamicLightTerrainCells() {
        for (int row = 0; row < gridHeight + 4; row++) {
            for (int col = gridWidth - 1; col >= 0; col--) {
                int lightGridX = col + RENDER_GRID_LEFT_GUARD;
                int lightGridY = row + RENDER_GRID_TOP_GUARD;
                int topLeft = Byte.toUnsignedInt(dynamicLightOverrideGrid[lightGridX][lightGridY]);
                int topRight = Byte.toUnsignedInt(dynamicLightOverrideGrid[lightGridX + 1][lightGridY]);
                int bottomLeft = Byte.toUnsignedInt(dynamicLightOverrideGrid[lightGridX][lightGridY + 1]);
                int bottomRight = Byte.toUnsignedInt(dynamicLightOverrideGrid[lightGridX + 1][lightGridY + 1]);
                if (topLeft + topRight + bottomLeft + bottomRight == DYNAMIC_LIGHT_UNSET * 4) {
                    continue;
                }

                int worldX = col + view.x;
                int worldY = row + view.y;
                drawTerrainCell(
                        col,
                        row,
                        dynamicTerrainBrightness(topLeft, worldX, worldY),
                        dynamicTerrainBrightness(topRight, worldX + 1, worldY),
                        dynamicTerrainBrightness(bottomLeft, worldX, worldY + 1),
                        dynamicTerrainBrightness(bottomRight, worldX + 1, worldY + 1),
                        col,
                        0,
                        false,
                        false,
                        true,
                        true
                );
            }
        }
    }

    /**
     * Native support extracted from MapVisualObject::DrawDynamicLightTerrainCells @00405CF4.
     */
    private int dynamicTerrainBrightness(int overrideBrightness, int tileX, int tileY) {
        int terrainBrightness = mapDescriptor.terrainLightAt(tileX, tileY);
        return overrideBrightness == DYNAMIC_LIGHT_UNSET
                ? terrainBrightness
                : Math.min(terrainBrightness, overrideBrightness);
    }

    /**
     * Native support extracted from MapVisualObject::DrawFullTerrainFrame @00404E0E and
     * MapVisualObject::DrawDynamicLightTerrainCells @00405CF4,
     * MapVisualObject::DrawAnimatedTerrainRingSegment @0040538D, and
     * MapVisualObject::DrawScrolledTerrainSegment @00405861.
     */
    private void drawTerrainCell(int col, int row,
                                 int topLeftBrightness, int topRightBrightness,
                                 int bottomLeftBrightness, int bottomRightBrightness,
                                 int screenCol, int yPixelOffset, boolean animatedRingPhase,
                                 boolean animatedOnly, boolean drawDebugGridLines, boolean allowDirtOverlay) {
        int worldX = col + view.x;
        int worldY = row + view.y;
        int tileWord = mapDescriptor.tileWordAt(worldX, worldY);
        int baseTileId = terrainTileId(tileWord);
        if (animatedOnly && !isAnimatedTerrainTile(baseTileId)) {
            return;
        }
        int tileId = animatedRingPhase
                ? resolveAnimatedRingTerrainTileId(baseTileId, worldX, worldY)
                : resolveTerrainTileId(baseTileId, worldX, worldY);
        int variant = (tileWord >>> TERRAIN_TILE_VARIANT_SHIFT) & TERRAIN_TILE_VARIANT_MASK;
        CBmp256 tileBitmap = TerrainGraphics.terrainTileSet[tileId][variant];
        Palette16[] palettePages = TerrainGraphics.terrainTilePalettes[tileId].paletteData;
        GameBitmapFrame frame = tileBitmap.frames.getFirst();
        byte[] sourcePixels = frame.data();
        int sourceOffset = (tileWord & TERRAIN_TILE_FRAME_MASK) * TERRAIN_TILE_PIXELS;
        if (allowDirtOverlay && (tileWord & TERRAIN_DIRT_OVERLAY_MASK) != 0 && !isAnimatedTerrainTile(baseTileId)) {
            sourcePixels = copyTerrainTileWithDirtOverlay(sourcePixels, sourceOffset, worldX, worldY);
            sourceOffset = 0;
        }

        int vertexGridX = terrainVertexGridX(col);
        int vertexGridY = terrainVertexGridY(row);
        int topLeftY = screenYVertexGrid[vertexGridX][vertexGridY] + yPixelOffset;
        int topRightY = screenYVertexGrid[vertexGridX + 1][vertexGridY] + yPixelOffset;
        int bottomLeftY = screenYVertexGrid[vertexGridX][vertexGridY + 1] + yPixelOffset;
        int bottomRightY = screenYVertexGrid[vertexGridX + 1][vertexGridY + 1] + yPixelOffset;
        int leftX = screenCol * TILE_SCREEN_SIZE;
        int rightX = (screenCol + 1) * TILE_SCREEN_SIZE;
        if (topLeftY == topRightY && bottomLeftY == bottomRightY && topLeftY + TILE_SCREEN_SIZE == bottomLeftY) {
            Globals.renderer.drawFlatTerrainTile(
                    leftX,
                    topLeftY,
                    topLeftBrightness,
                    topRightBrightness,
                    bottomLeftBrightness,
                    bottomRightBrightness,
                    sourcePixels,
                    sourceOffset,
                    palettePages
            );
            drawTerrainDebugGridLines(drawDebugGridLines, leftX, rightX, topLeftY, topRightY, bottomLeftY);
            return;
        }

        Globals.renderer.drawSkewedTerrainTile(
                leftX,
                rightX,
                topLeftY,
                topRightY,
                bottomLeftY,
                bottomRightY,
                topLeftBrightness,
                topRightBrightness,
                bottomLeftBrightness,
                bottomRightBrightness,
                sourcePixels,
                sourceOffset,
                palettePages
        );
        drawTerrainDebugGridLines(drawDebugGridLines, leftX, rightX, topLeftY, topRightY, bottomLeftY);
    }

    /**
     * Native support extracted from MapVisualObject::DrawFullTerrainFrame @00404E0E,
     * DrawScrolledTerrainSegment @00405861, and DrawDynamicLightTerrainCells @00405CF4.
     */
    private static void drawTerrainDebugGridLines(
            boolean enabledForNativeCaller,
            int leftX,
            int rightX,
            int topLeftY,
            int topRightY,
            int bottomLeftY
    ) {
        if (!enabledForNativeCaller || terrainDebugGridLines == 0) {
            return;
        }
        Globals.renderer.drawLine(leftX, topLeftY, rightX, topRightY, RGB16.BLACK.val());
        Globals.renderer.drawLine(leftX, topLeftY, leftX, bottomLeftY, RGB16.BLACK.val());
    }

    /**
     * Native support extracted from MapVisualObject::DrawFullTerrainFrame @00404E0E animated terrain branch.
     */
    private int resolveTerrainTileId(int baseTileId, int worldX, int worldY) {
        if (!isAnimatedTerrainTile(baseTileId)) {
            return baseTileId;
        }
        int phase = Globals.gamePreferences.animation == 0
                ? 0
                : (baseTileId + (worldX + 1) * worldY + (mapAnimationTick >> 2)) & TERRAIN_TILE_VARIANT_MASK;
        return ANIMATED_TERRAIN_FIRST_TILE + phase;
    }

    /**
     * Native support extracted from MapVisualObject::DrawAnimatedTerrainRingSegment @0040538D.
     */
    private int resolveAnimatedRingTerrainTileId(int baseTileId, int worldX, int worldY) {
        if (!isAnimatedTerrainTile(baseTileId)) {
            return baseTileId;
        }
        int phase = Globals.gamePreferences.animation == 0
                ? 0
                : (baseTileId + worldX + worldX * worldY + (mapAnimationTick >> 2)) & TERRAIN_TILE_VARIANT_MASK;
        return ANIMATED_TERRAIN_FIRST_TILE + phase;
    }

    /**
     * Native support extracted from MapVisualObject::DrawFullTerrainFrame @00404E0E tile-id extraction.
     */
    private static int terrainTileId(int tileWord) {
        return (tileWord & TERRAIN_TILE_INDEX_MASK) >> TERRAIN_TILE_FAMILY_SHIFT;
    }

    /**
     * Native support extracted from MapVisualObject::DrawFullTerrainFrame @00404E0E animated terrain test.
     */
    private static boolean isAnimatedTerrainTile(int tileId) {
        return tileId >= ANIMATED_TERRAIN_FIRST_TILE && tileId <= ANIMATED_TERRAIN_LAST_TILE;
    }

    /**
     * Native support extracted from MapVisualObject::DrawFullTerrainFrame @00404E0E and
     * OverlayNonZeroIndexedPixels @00452BBD.
     */
    private byte[] copyTerrainTileWithDirtOverlay(byte[] sourcePixels, int sourceOffset, int worldX, int worldY) {
        byte[] combinedPixels = m_CBmp256.frames.getFirst().data();
        System.arraycopy(sourcePixels, sourceOffset, combinedPixels, 0, TERRAIN_TILE_PIXELS);
        GameBitmapFrame dirtFrame = Objects.requireNonNull(TerrainGraphics.terrainDirtBitmap).frames.getFirst();
        int dirtOffset = ((worldX + worldY * 5) & TERRAIN_TILE_VARIANT_MASK) * TERRAIN_TILE_PIXELS;
        overlayNonZeroIndexedPixels(combinedPixels, 0, dirtFrame.data(), dirtOffset, TERRAIN_TILE_PIXELS);
        return combinedPixels;
    }

    /**
     * Native support extracted from OverlayNonZeroIndexedPixels @00452BBD.
     * Fully ported.
     */
    private static void overlayNonZeroIndexedPixels(byte[] destinationPixels, int destinationOffset,
                                                    byte[] sourcePixels, int sourceOffset, int byteCount) {
        for (int i = 0; i < byteCount; i++) {
            byte value = sourcePixels[sourceOffset + i];
            if (value != 0) {
                destinationPixels[destinationOffset + i] = value;
            }
        }
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43.
     */
    private void drawObjectLayerPasses() {
        objectSelectionCount = 0;
        if (Globals.gamePreferences.shadows != 0) {
            drawLayerShadows(structureObjectLayer, true);
        }
        drawFlatStructures();
        drawInactiveAndMiscObjects();
        drawGroundLayerAndTerrainObjects();
        if (Globals.gamePreferences.shadows != 0) {
            drawLayerShadows(airObjectLayer, false);
        }
        drawTransientObjects();
        drawTerrainLightOverridePass(false);
        drawAirObjects();
        drawSelectionOverlayPass();
        applyTerrainSlopeMasks();
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43.
     */
    private void drawLayerShadows(CGameObject[][] layer, boolean allowBlockedOne) {
        for (int row = -4; row < gridHeight + 8; row++) {
            for (int col = gridWidth + 3; col > -5; col--) {
                CGameObject object = objectAt(layer, col, row);
                if (isDrawableObject(object, allowBlockedOne)) {
                    object.drawShadow(col, row);
                }
            }
        }
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43.
     */
    private void drawFlatStructures() {
        for (int row = -4; row < gridHeight + 8; row++) {
            for (int col = gridWidth + 3; col > -5; col--) {
                CGameObject object = objectAt(structureObjectLayer, col, row);
                if (!(object instanceof CStructure structure) || !isDrawableObject(structure, true)) {
                    continue;
                }

                StructureDef def = Objects.requireNonNull(
                        Structures.getStructureDef(structure.type),
                        "Missing StructureDef for id " + structure.type
                );
                if (def.flat == 0) {
                    continue;
                }

                structure.draw(col, row, tileBrightnessAt(col, row));
                if (structure.bIsBlocked == 0) {
                    recordStructureSelectionRect(structure, def, col, row);
                }
            }
        }
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43.
     */
    private void drawInactiveAndMiscObjects() {
        for (int row = -4; row < gridHeight + 8; row++) {
            for (int col = gridWidth + 3; col > -5; col--) {
                drawUnblockedObjectWithInlineShadow(objectAt(inactiveUnitLayer, col, row), col, row);
                drawUnblockedObjectWithInlineShadow(objectAt(miscObjectLayer, col, row), col, row);
            }
        }
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 inactive/misc object passes.
     */
    private void drawUnblockedObjectWithInlineShadow(CGameObject object, int col, int row) {
        if (!isDrawableObject(object, false)) {
            return;
        }
        if (Globals.gamePreferences.shadows != 0) {
            object.drawShadow(col, row);
        }
        object.draw(col, row, tileBrightnessAt(col, row));
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43.
     */
    private void drawGroundLayerAndTerrainObjects() {
        for (int row = -4; row < gridHeight + 8; row++) {
            for (int col = gridWidth + 3; col > -5; col--) {
                int occupancyMask = combinedTileOccupancyMask(col, row);
                int averageHeight = tileAverageHeightAt(col, row);
                CGameObject structureObject = objectAt(structureObjectLayer, col, row);
                if (structureObject instanceof CStructure structure && isDrawableObject(structure, true)) {
                    StructureDef def = Objects.requireNonNull(
                            Structures.getStructureDef(structure.type),
                            "Missing StructureDef for id " + structure.type
                    );
                    if (def.flat == 0) {
                        structure.draw(col, row, tileBrightnessAt(col, row));
                        if (structure.bIsBlocked == 0) {
                            recordStructureSelectionRect(structure, def, col, row);
                        }
                    }
                }

                CGameObject groundObject = objectAt(groundObjectLayer, col, row);
                if (groundObject instanceof CUnit unit
                        && isDrawableObject(unit, false)
                        && shouldDrawUnitThroughVisibility(unit)) {
                    recordUnitSelectionRect(unit);
                    if (Globals.gamePreferences.shadows != 0) {
                        unit.drawShadow(col, row);
                    }
                    unit.draw(col, row, tileBrightnessAt(col, row));
                }

                if (isObjectLayerCell(col, row)) {
                    drawAnimatedWaterTransientEffect(col, row, averageHeight);
                }
                drawTerrainVisualObject(col, row, occupancyMask, averageHeight);
                if (isObjectLayerCell(col, row)) {
                    drawTerrainLightOverrideAt(col, row, occupancyMask, true);
                }
            }
        }
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43.
     */
    private void drawTransientObjects() {
        for (CGameObject object : transientObjects.values()) {
            if (object != null) {
                object.draw(0, 0, 0);
            }
        }
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43.
     */
    private void drawTerrainLightOverridePass(boolean firstPass) {
        for (int row = -4; row < gridHeight + 8; row++) {
            for (int col = gridWidth + 3; col > -5; col--) {
                if (row < 0 || row >= gridHeight + 4 || col < 0 || col >= gridWidth) {
                    continue;
                }
                drawTerrainLightOverrideAt(col, row, combinedTileOccupancyMask(col, row), firstPass);
            }
        }
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43.
     */
    private void drawAirObjects() {
        for (int row = -4; row < gridHeight + 8; row++) {
            for (int col = gridWidth + 3; col > -5; col--) {
                CGameObject airObject = objectAt(airObjectLayer, col, row);
                if (airObject instanceof CUnit unit && isDrawableObject(unit, false)) {
                    recordUnitSelectionRect(unit);
                    unit.draw(col, row, tileBrightnessAt(col, row));
                }
            }
        }
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43.
     */
    private void drawSelectionOverlayPass() {
        for (int row = -3; row < gridHeight + 7; row++) {
            for (int col = gridWidth + 2; col > -4; col--) {
                CGameObject structure = objectAt(structureObjectLayer, col, row);
                if (structure != null
                        && structure.isSelected()
                        && col == structure.mapBoundsLeft
                        && row == structure.mapBoundsTop) {
                    structure.drawSelectionOverlay();
                }

                CGameObject groundObject = objectAt(groundObjectLayer, col, row);
                if (groundObject instanceof CUnit groundUnit
                        && (groundUnit.isSelected() || (showHitPointBars != 0 && isDrawableObject(groundUnit, false)))) {
                    groundUnit.drawSelectionOverlay();
                }

                CGameObject airObject = objectAt(airObjectLayer, col, row);
                if (airObject instanceof CUnit airUnit
                        && (airUnit.isSelected() || (showHitPointBars != 0 && isDrawableObject(airUnit, false)))) {
                    airUnit.drawSelectionOverlay();
                }
            }
        }
    }

    /**
     * Native: MapVisualObject::RecordObjectSelectionRect @00404D99.
     * Fully ported.
     */
    private void recordObjectSelectionRect(CRect rect, int objectId) {
        CRect storedRect = new CRect(rect);
        if (objectSelectionCount < objectSelectionRects.size()) {
            objectSelectionRects.set(objectSelectionCount, storedRect);
            objectSelectionIds.set(objectSelectionCount, objectId);
        } else {
            objectSelectionRects.add(storedRect);
            objectSelectionIds.add(objectId);
        }
        objectSelectionCount++;
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 structure selection-rect branches.
     */
    private void recordStructureSelectionRect(CStructure structure, StructureDef def, int col, int row) {
        CRect selectionRect = new CRect(def.selection);
        selectionRect.offset(
                structure.mapBoundsLeft << 5,
                (((structure.mapBoundsTop + def.tileHeight) - def.fullHeight) << 5) - tileAverageHeightAt(col, row)
        );
        recordObjectSelectionRect(selectionRect, structure.m_id);
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 unit selection-rect branches.
     */
    private void recordUnitSelectionRect(CUnit unit) {
        if (Byte.toUnsignedInt(unit.field51_0x184) >= 2) {
            return;
        }
        CUnitInfo info = Objects.requireNonNull(UnitTypes.getUnitInfo(unit.type), "Missing CUnitInfo for id " + unit.type);
        CRect selectionRect = new CRect(info.m_SelectionRect);
        selectionRect.offset(
                unit.centerScreenX - info.m_CenterX,
                (((unit.centerScreenY - info.m_CenterY) - unit.terrainHeightOffset) - unit.z)
        );
        recordObjectSelectionRect(selectionRect, unit.m_id);
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 invisible-unit draw gate.
     */
    private boolean shouldDrawUnitThroughVisibility(CUnit unit) {
        return findPackedEffectIndex(unit, 0x20) < 0 || isOwnerVisible(unit.cPlayer.playerId);
    }

    /**
     * Native support extracted from FUN_00460D4B @00460D4B use in MapVisualObject::RenderFrame @00406F43.
     */
    private static int findPackedEffectIndex(CGameObject object, int effectType) {
        for (int index = 0; index < object.dwarr_130.size(); index++) {
            if ((object.dwarr_130.get(index) >>> 16) == effectType) {
                return index;
            }
        }
        return -1;
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 four-corner tile occupancy reads.
     */
    private int combinedTileOccupancyMask(int col, int row) {
        int flatIndex = nativeMapFlatIndex(col, row);
        return (mapDescriptor.tileWordFlatAt(flatIndex)
                | mapDescriptor.tileWordFlatAt(flatIndex + 1)
                | mapDescriptor.tileWordFlatAt(flatIndex + cachedMapWidth)
                | mapDescriptor.tileWordFlatAt(flatIndex + cachedMapWidth + 1))
                & TERRAIN_LIGHT_FULLY_BLOCKED_MASK;
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 tileAverageHeightGrid reads.
     */
    private int tileAverageHeightAt(int col, int row) {
        return tileAverageHeightGrid[col + 4][row + 4];
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 packed transient/effect tile keys.
     */
    private int packedTileKey(int col, int row) {
        return ((view.x + col) & 0xFF) | (((view.y + row) & 0xFF) << 8);
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 terrain tile word reads.
     */
    private int tileWordAt(int col, int row) {
        return mapDescriptor.tileWordFlatAt(nativeMapFlatIndex(col, row));
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 flat map-section pointer indices.
     */
    private int nativeMapFlatIndex(int col, int row) {
        return view.x + col + (view.y + row) * cachedMapWidth;
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 animated-water transient branch.
     */
    private void drawAnimatedWaterTransientEffect(int col, int row, int tileAverageHeight) {
        if (Globals.gamePreferences.lighting == 0) {
            return;
        }
        Integer phase = transientEffectCells.get(packedTileKey(col, row));
        if (phase == null || phase >= 0x12) {
            return;
        }
        int tileId = terrainTileId(tileWordAt(col, row));
        if (tileId <= 7 || tileId >= 0x0C) {
            return;
        }
        drawProjectileCentered(
                ANIMATED_WATER_EFFECT_PROJECTILE_ID,
                col * TILE_SCREEN_SIZE + TILE_SCREEN_SIZE / 2,
                (row * TILE_SCREEN_SIZE + TILE_SCREEN_SIZE / 2) - tileAverageHeight,
                phase >> 1
        );
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 terrain-light override branches.
     */
    private void drawTerrainLightOverrideAt(int col, int row, int occupancyMask, boolean firstPass) {
        if (occupancyMask != TERRAIN_LIGHT_FULLY_BLOCKED_MASK) {
            return;
        }
        Integer flags = terrainLightOverrideCells.get(packedTileKey(col, row));
        if (flags == null || flags == 0) {
            return;
        }
        int gridX = objectLayerGridX(col);
        int gridY = objectLayerGridY(row);
        drawTerrainLightOverrideEffects(
                col * TILE_SCREEN_SIZE + TILE_SCREEN_SIZE / 2,
                (screenYRowTopGrid[gridX][gridY] + screenYRowBottomGrid[gridX][gridY]) / 2,
                flags,
                firstPass
        );
    }

    /**
     * Native: MapVisualObject::DrawTerrainLightOverrideEffects @0040AF69.
     * Fully ported.
     */
    private void drawTerrainLightOverrideEffects(int screenX, int screenY, int flags, boolean firstPass) {
        if ((flags & TERRAIN_LIGHT_FLICKER_FLAG) != 0 && firstPass) {
            int frame = Math.abs(mapAnimationTick / 2 + screenX * screenY) % 5 + 3;
            drawProjectileCentered(TERRAIN_LIGHTNING_PROJECTILE_ID, screenX, screenY, frame);
        }
        if ((flags & TERRAIN_LIGHT_MAGIC_FLAG) != 0 && !firstPass) {
            drawProjectileCenteredWithModuloPhase(TERRAIN_MAGIC_LIGHT_PROJECTILE_ID, screenX, screenY);
        } else if ((flags & TERRAIN_LIGHT_SPECIAL_FLAG) != 0 && firstPass) {
            drawProjectileCenteredWithModuloPhase(TERRAIN_SPECIAL_LIGHT_PROJECTILE_ID, screenX, screenY);
        }
    }

    /**
     * Native support extracted from MapVisualObject::DrawTerrainLightOverrideEffects @0040AF69.
     */
    private void drawProjectileCenteredWithModuloPhase(int projectileId, int screenX, int screenY) {
        CProjectileInfo info = projectileInfo(projectileId);
        int frame = Math.abs(mapAnimationTick / 2 + screenX * screenY) % info.phases;
        drawProjectileCentered(info, screenX, screenY, frame);
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 projectile sprite draws.
     */
    private void drawProjectileCentered(int projectileId, int screenX, int screenY, int frame) {
        drawProjectileCentered(projectileInfo(projectileId), screenX, screenY, frame);
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 projectile sprite draws.
     */
    private void drawProjectileCentered(CProjectileInfo info, int screenX, int screenY, int frame) {
        info.getSpriteA().draw(screenX - info.width / 2, screenY - info.height / 2, frame, 0, false);
    }

    /**
     * Native support extracted from `CArray<CProjectileInfo>::GetAt` call sites in MapVisualObject::RenderFrame @00406F43.
     */
    private static CProjectileInfo projectileInfo(int projectileId) {
        return Objects.requireNonNull(Projectiles.PROJECTILES_BY_ID.get(projectileId), "Missing projectile id " + projectileId);
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 visual-object branch.
     */
    private void drawTerrainVisualObject(int col, int row, int occupancyMask, int tileAverageHeight) {
        int encodedVisualObjectId = mapDescriptor.objectIdFlatAt(nativeMapFlatIndex(col, row));
        if (encodedVisualObjectId == 0) {
            return;
        }

        int visualObjectId = encodedVisualObjectId - 1;
        if (isTerrainSlopeCoverageProbeColumn(col)
                && isVisualObjectCoveredByTerrainSlope(visualObjectId, col, row, tileAverageHeight)) {
            return;
        }

        VObject visualObject = Objects.requireNonNull(VObjects.getVObject(visualObjectId), "Missing VObject id " + visualObjectId);
        TerrainVisualSprite visualSprite = resolveTerrainVisualSprite(visualObject, col, row, occupancyMask);
        GraphicsObjectsFile file = Objects.requireNonNull(
                VObjects.getGraphicsObjectsFile(visualSprite.fileId),
                "Missing GraphicsObjectsFile id " + visualSprite.fileId
        );
        CSprite256 spriteA = file.getSpriteA();
        CSprite256 spriteB = file.getSpriteB();
        int brightness = tileBrightnessAt(col, row);
        int shadowSlope = terrainShadowSlope();
        int shadowSkew = shadowSkewForSprite(spriteA, visualSprite.frame, visualObject);
        if (Globals.gamePreferences.shadows != 0) {
            drawTerrainVisualSpriteShadow(col, row, tileAverageHeight, visualObject, spriteA, visualSprite.frame, shadowSlope, shadowSkew);
            if (Globals.gamePreferences.smoothing != 0) {
                drawTerrainVisualSpriteSmoothingShadow(col, row, tileAverageHeight, visualObject, spriteB, visualSprite.frame, shadowSlope, shadowSkew);
            }
        }
        drawTerrainVisualSpriteMain(col, row, tileAverageHeight, visualObject, spriteA, visualSprite.frame, brightness, spriteA.palette);
        if (Globals.gamePreferences.smoothing != 0) {
            drawTerrainVisualSpriteSmoothing(col, row, tileAverageHeight, visualObject, spriteB, visualSprite.frame, brightness, spriteA.palette);
        }

        Integer phase = transientEffectCells.get(packedTileKey(col, row));
        if (phase != null) {
            drawProjectileCentered(
                    TERRAIN_LIGHTNING_PROJECTILE_ID,
                    col * TILE_SCREEN_SIZE + TILE_SCREEN_SIZE / 2,
                    (row * TILE_SCREEN_SIZE + TILE_SCREEN_SIZE / 2) - tileAverageHeight,
                    phase / 2
            );
        }
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 visual-object frame routing.
     */
    private TerrainVisualSprite resolveTerrainVisualSprite(VObject visualObject, int col, int row, int occupancyMask) {
        int tileWord = tileWordAt(col, row);
        int fileId = visualObject.fileId;
        int frame = visualObject.spriteIndex;
        boolean hasLiveObjectOrNoDeadVariant =
                (tileWord & TERRAIN_DEAD_VISUAL_OBJECT_MASK) == 0 || visualObject.deadObjectId == -1;
        if (visualObject.animationFrameCount != 0
                && occupancyMask == TERRAIN_LIGHT_FULLY_BLOCKED_MASK
                && hasLiveObjectOrNoDeadVariant) {
            int worldX = view.x + col;
            int worldY = view.y + row;
            int phase = Math.floorMod(mapAnimationTick + worldX + worldX * worldY, visualObject.animationFrameCount);
            frame += visualObject.animationFrames.get(phase);
        } else if (!hasLiveObjectOrNoDeadVariant) {
            VObject deadObject = Objects.requireNonNull(
                    VObjects.getVObject(visualObject.deadObjectId),
                    "Missing dead VObject id " + visualObject.deadObjectId
            );
            fileId = deadObject.fileId;
            frame = 0;
        }
        if (Globals.gamePreferences.animation == 0) {
            frame = 0;
        }
        return new TerrainVisualSprite(fileId, frame);
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 terrain visual-object shadow draw.
     */
    private void drawTerrainVisualSpriteShadow(
            int col,
            int row,
            int tileAverageHeight,
            VObject visualObject,
            CSprite256 sprite,
            int frame,
            int shadowSlope,
            int shadowSkew
    ) {
        if (Globals.gamePreferences.shadows == 0) {
            return;
        }
        Point drawPoint = terrainVisualShadowDrawPoint(col, row, tileAverageHeight, visualObject, sprite, shadowSkew);
        sprite.drawWithRenderEffect(drawPoint.x, drawPoint.y, frame, Globals.lighting.shadowLength, shadowSlope, false);
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 terrain visual-object smoothing shadow draw.
     */
    private void drawTerrainVisualSpriteSmoothingShadow(
            int col,
            int row,
            int tileAverageHeight,
            VObject visualObject,
            CSprite256 sprite,
            int frame,
            int shadowSlope,
            int shadowSkew
    ) {
        Point drawPoint = terrainVisualShadowDrawPoint(col, row, tileAverageHeight, visualObject, sprite, shadowSkew);
        sprite.drawWithRenderEffect(drawPoint.x, drawPoint.y, frame, Globals.lighting.lightHeight, shadowSlope, false);
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 terrain visual-object main sprite draw.
     */
    private void drawTerrainVisualSpriteMain(
            int col,
            int row,
            int tileAverageHeight,
            VObject visualObject,
            CSprite256 sprite,
            int frame,
            int brightness,
            CGamePalette palette
    ) {
        Point drawPoint = terrainVisualDrawPoint(col, row, tileAverageHeight, visualObject, sprite, frame, 0);
        sprite.drawWithPalette(drawPoint.x, drawPoint.y, frame, brightness, palette, false);
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 terrain visual-object smoothing draw.
     */
    private void drawTerrainVisualSpriteSmoothing(
            int col,
            int row,
            int tileAverageHeight,
            VObject visualObject,
            CSprite256 sprite,
            int frame,
            int brightness,
            CGamePalette palette
    ) {
        Point drawPoint = terrainVisualDrawPoint(col, row, tileAverageHeight, visualObject, sprite, frame, 0);
        sprite.drawFrameClippedY(drawPoint.x, drawPoint.y, frame, brightness, palette, false);
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 terrain visual-object shadow coordinate math.
     * Native anchors terrain visual-object shadows with sprite frame 0 while drawing the current animated frame.
     */
    private Point terrainVisualShadowDrawPoint(
            int col,
            int row,
            int tileAverageHeight,
            VObject visualObject,
            CSprite256 sprite,
            int shadowSkew
    ) {
        return terrainVisualDrawPoint(col, row, tileAverageHeight, visualObject, sprite, 0, shadowSkew);
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 terrain visual-object coordinate math.
     */
    private Point terrainVisualDrawPoint(
            int col,
            int row,
            int tileAverageHeight,
            VObject visualObject,
            CSprite256 sprite,
            int frame,
            int shadowSkew
    ) {
        int centerX = (visualObject.centerX - visualObject.width / 2) + sprite.xSizeOf(frame) / 2;
        int centerY = (visualObject.centerY - visualObject.height / 2) + sprite.ySizeOf(frame) / 2;
        return new Point(
                (col * TILE_SCREEN_SIZE + TILE_SCREEN_SIZE / 2) - centerX - shadowSkew,
                ((row * TILE_SCREEN_SIZE + TILE_SCREEN_SIZE / 2) - centerY) - tileAverageHeight
        );
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 visual-object shadow offset.
     */
    private int shadowSkewForSprite(CSprite256 sprite, int frame, VObject visualObject) {
        double sunSlope = Math.tan(mapDescriptor.getShadowAngle());
        return (int) (sunSlope * ((sprite.ySizeOf(frame) / 2 + visualObject.height / 2) - visualObject.centerY));
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 visual-object shadow slope.
     */
    private int terrainShadowSlope() {
        return (int) (Math.tan(mapDescriptor.getShadowAngle()) * 65536.0);
    }

    /**
     * Native support extracted from MapVisualObject::GetTileY @00417F31 render-grid column contract.
     */
    private boolean isTerrainSlopeCoverageProbeColumn(int col) {
        return -RENDER_GRID_LEFT_GUARD <= col && col < gridWidth + RENDER_GRID_LEFT_GUARD;
    }

    /**
     * Native: MapVisualObject::IsVisualObjectCoveredByTerrainSlope @0040B117.
     * Fully ported.
     */
    private boolean isVisualObjectCoveredByTerrainSlope(int visualObjectId, int viewTileX, int viewTileY, int tileAverageHeight) {
        VObject visualObject = Objects.requireNonNull(VObjects.getVObject(visualObjectId), "Missing VObject id " + visualObjectId);
        int leftTile = Math.max(0, viewTileX - 1);
        int rightTile = Math.min(gridWidth, viewTileX + 2);
        GraphicsObjectsFile file = Objects.requireNonNull(
                VObjects.getGraphicsObjectsFile(visualObject.fileId),
                "Missing GraphicsObjectsFile id " + visualObject.fileId
        );
        CSprite256 sprite = file.getSpriteA();
        int centerY = (visualObject.centerY - visualObject.height / 2) + sprite.ySizeOf(0) / 2;
        int topTileY = getTileYForScreenPoint(
                viewTileX * TILE_SCREEN_SIZE + TILE_SCREEN_SIZE / 2,
                ((viewTileY * TILE_SCREEN_SIZE + TILE_SCREEN_SIZE / 2) - centerY) - tileAverageHeight
        );
        int bottomTileY = getTileYForScreenPoint(
                viewTileX * TILE_SCREEN_SIZE + TILE_SCREEN_SIZE / 2,
                (((viewTileY * TILE_SCREEN_SIZE + TILE_SCREEN_SIZE / 2) - centerY) - tileAverageHeight) + sprite.ySizeOf(0)
        ) + 1;
        topTileY = Math.max(0, topTileY);
        bottomTileY = Math.min(gridHeight + 4, bottomTileY);
        for (int tileX = leftTile; tileX <= rightTile; tileX++) {
            for (int tileY = topTileY; tileY <= bottomTileY; tileY++) {
                if (tileSlopeModeGrid[terrainVertexGridX(tileX)][terrainVertexGridY(tileY)] != TILE_SLOPE_UP) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Native: MapVisualObject::GetTileY @00417F31.
     * Fully ported.
     */
    public int getTileYForScreenPoint(int x, int y) {
        int col = x >> 5;
        int gridX = col + RENDER_GRID_LEFT_GUARD;
        int objectLayerStride = objectLayerStride();
        int terrainVertexStride = terrainVertexStride();
        for (int row = -3; row < gridHeight + 7; row++) {
            int gridY = objectLayerGridY(row);
            int rowGridIndex = gridX + gridY * objectLayerStride;
            if (nativeFlatIntGridAt(screenYRowTopGrid, objectLayerStride, rowGridIndex) > y
                    || y > nativeFlatIntGridAt(screenYRowBottomGrid, objectLayerStride, rowGridIndex)) {
                continue;
            }
            int vertexGridIndex = terrainVertexGridX(col) + terrainVertexGridY(row) * terrainVertexStride;
            int topLeftY = nativeFlatIntGridAt(screenYVertexGrid, terrainVertexStride, vertexGridIndex);
            int topRightY = nativeFlatIntGridAt(screenYVertexGrid, terrainVertexStride, vertexGridIndex + 1);
            int bottomLeftY = nativeFlatIntGridAt(screenYVertexGrid, terrainVertexStride, vertexGridIndex + terrainVertexStride);
            int bottomRightY = nativeFlatIntGridAt(screenYVertexGrid, terrainVertexStride, vertexGridIndex + terrainVertexStride + 1);
            int xRemainder = x & 0x1F;
            int topEdgeY = topLeftY + arithmeticDivideBy32((topRightY - topLeftY) * xRemainder);
            int bottomEdgeY = bottomLeftY + arithmeticDivideBy32((bottomRightY - bottomLeftY) * xRemainder);
            if (topEdgeY <= y && y <= bottomEdgeY) {
                return row;
            }
        }
        return gridHeight + 4;
    }

    /**
     * Native support extracted from MapVisualObject::GetTileY @00417F31 flat render-grid pointer reads.
     */
    private static int nativeFlatIntGridAt(int[][] grid, int stride, int flatIndex) {
        return grid[flatIndex % stride][flatIndex / stride];
    }

    /**
     * Native support extracted from MapVisualObject::GetTileY @00417F31 signed divide-by-32 sequence.
     */
    private static int arithmeticDivideBy32(int value) {
        return (value + ((value >> 31) & 0x1F)) >> 5;
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 terrain-slope mask pass.
     */
    private void applyTerrainSlopeMasks() {
        for (int col = 0; col < gridWidth; col++) {
            int row = 0;
            while (row < gridHeight + 4) {
                int gridX = terrainVertexGridX(col);
                int gridY = terrainVertexGridY(row);
                int topLeftSlope = tileSlopeModeGrid[gridX][gridY];
                int topRightSlope = tileSlopeModeGrid[gridX + 1][gridY];
                int bottomLeftSlope = tileSlopeModeGrid[gridX][gridY + 1];
                int bottomRightSlope = tileSlopeModeGrid[gridX + 1][gridY + 1];
                int topLeftY = screenYVertexGrid[gridX][gridY];
                int topRightY = screenYVertexGrid[gridX + 1][gridY];
                int bottomLeftY = screenYVertexGrid[gridX][gridY + 1];
                int bottomRightY = screenYVertexGrid[gridX + 1][gridY + 1];

                while (topLeftSlope == bottomLeftSlope
                        && topRightSlope == bottomRightSlope
                        && row != gridHeight + 3) {
                    int nextBottomGridY = terrainVertexGridY(row + 2);
                    int nextBottomLeftSlope = tileSlopeModeGrid[gridX][nextBottomGridY];
                    int nextBottomRightSlope = tileSlopeModeGrid[gridX + 1][nextBottomGridY];
                    if (bottomLeftSlope != nextBottomLeftSlope || bottomRightSlope != nextBottomRightSlope) {
                        break;
                    }
                    row++;
                    gridY = terrainVertexGridY(row);
                    bottomLeftSlope = nextBottomLeftSlope;
                    bottomRightSlope = nextBottomRightSlope;
                    bottomLeftY = screenYVertexGrid[gridX][gridY + 1];
                    bottomRightY = screenYVertexGrid[gridX + 1][gridY + 1];
                }

                if (topLeftSlope == topRightSlope && topRightSlope == bottomLeftSlope && bottomLeftSlope == bottomRightSlope) {
                    applyUniformTerrainSlopeMask(col, topLeftY, topRightY, bottomLeftY, bottomRightY, topLeftSlope);
                } else {
                    applyMixedTerrainSlopeMask(col, topLeftY, topRightY, bottomLeftY, bottomRightY,
                            topLeftSlope, topRightSlope, bottomLeftSlope, bottomRightSlope);
                }
                row++;
            }
        }
    }

    /**
     * Native support extracted from ClearFlatTerrainSlopeMask, ClearSkewedTerrainSlopeMask, DimFlatTerrainSlopeMask, and
     * DimSkewedTerrainSlopeMask call sites in MapVisualObject::RenderFrame @00406F43.
     */
    private void applyUniformTerrainSlopeMask(int col, int topLeftY, int topRightY, int bottomLeftY, int bottomRightY, int slopeMode) {
        if (slopeMode == TILE_SLOPE_BLOCKED) {
            return;
        }
        if (slopeMode == TILE_SLOPE_UP) {
            if (isFlatTerrainSlope(topLeftY, topRightY, bottomLeftY, bottomRightY)) {
                Globals.renderer.clearFlatTerrainSlopeMask(col * TILE_SCREEN_SIZE, topLeftY, bottomLeftY);
            } else {
                Globals.renderer.clearSkewedTerrainSlopeMask(
                        col * TILE_SCREEN_SIZE,
                        (col + 1) * TILE_SCREEN_SIZE,
                        topLeftY,
                        topRightY,
                        bottomLeftY,
                        bottomRightY
                );
            }
        } else if (slopeMode == TILE_SLOPE_DOWN) {
            if (isFlatTerrainSlope(topLeftY, topRightY, bottomLeftY, bottomRightY)) {
                Globals.renderer.dimFlatTerrainSlopeMask(col * TILE_SCREEN_SIZE, topLeftY, bottomLeftY);
            } else {
                Globals.renderer.dimSkewedTerrainSlopeMask(
                        col * TILE_SCREEN_SIZE,
                        (col + 1) * TILE_SCREEN_SIZE,
                        topLeftY,
                        topRightY,
                        bottomLeftY,
                        bottomRightY
                );
            }
        }
    }

    /**
     * Native support extracted from ApplyFlatTerrainSlopeMaskBrightness and ApplySkewedTerrainSlopeMaskBrightness call sites
     * in MapVisualObject::RenderFrame @00406F43.
     */
    private void applyMixedTerrainSlopeMask(
            int col,
            int topLeftY,
            int topRightY,
            int bottomLeftY,
            int bottomRightY,
            int topLeftSlope,
            int topRightSlope,
            int bottomLeftSlope,
            int bottomRightSlope
    ) {
        if (isFlatTerrainSlope(topLeftY, topRightY, bottomLeftY, bottomRightY)) {
            Globals.renderer.applyFlatTerrainSlopeMaskBrightness(
                    col * TILE_SCREEN_SIZE,
                    topLeftY,
                    topLeftSlope,
                    topRightSlope,
                    bottomLeftSlope,
                    bottomRightSlope
            );
        } else {
            Globals.renderer.applySkewedTerrainSlopeMaskBrightness(
                    col * TILE_SCREEN_SIZE,
                    (col + 1) * TILE_SCREEN_SIZE,
                    topLeftY,
                    topRightY,
                    bottomLeftY,
                    bottomRightY,
                    topLeftSlope,
                    topRightSlope,
                    bottomLeftSlope,
                    bottomRightSlope
            );
        }
    }

    /**
     * Native support extracted from flat terrain-slope mask dispatch in MapVisualObject::RenderFrame @00406F43.
     */
    private static boolean isFlatTerrainSlope(int topLeftY, int topRightY, int bottomLeftY, int bottomRightY) {
        return topLeftY == topRightY && bottomLeftY == bottomRightY && topLeftY + TILE_SCREEN_SIZE == bottomLeftY;
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 terrain visual-object routing locals.
     */
    private record TerrainVisualSprite(int fileId, int frame) {
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43.
     */
    private void updateRenderStats() {
        renderStatsFrameCount++;
        int currentTick = (int) System.currentTimeMillis();
        if (renderStatsLastTick == 0) {
            renderStatsLastTick = currentTick;
        }
        renderStatsElapsedMillis += currentTick - renderStatsLastTick;
        renderStatsLastTick = currentTick;
        if (renderStatsElapsedMillis > 1000) {
            renderStatsFps = renderStatsFrameCount * 1000.0 / renderStatsElapsedMillis;
            renderStatsElapsedMillis -= 1000;
            renderStatsFrameCount = 0;
        }
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43.
     */
    private CGameObject objectAt(CGameObject[][] layer, int col, int row) {
        if (!isObjectLayerCell(col, row)) {
            return null;
        }
        return layer[objectLayerGridX(col)][objectLayerGridY(row)];
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43.
     */
    private boolean isDrawableObject(CGameObject object, boolean allowBlockedOne) {
        return object != null && (allowBlockedOne ? object.bIsBlocked < 2 : object.bIsBlocked == 0);
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43.
     */
    private int tileBrightnessAt(int col, int row) {
        if (!isObjectLayerCell(col, row)) {
            return Globals.lighting.brightness >> 2;
        }
        return Byte.toUnsignedInt(tileBrightnessGrid[objectLayerGridX(col)][objectLayerGridY(row)]);
    }

    /**
     * Native support extracted from MapVisualObject::RecalculateGridMetrics @00402FB8.
     */
    private void clearObjectLayers() {
        fillObjectGrid(groundObjectLayer, null);
        fillObjectGrid(airObjectLayer, null);
        fillObjectGrid(structureObjectLayer, null);
        fillObjectGrid(miscObjectLayer, null);
        fillObjectGrid(inactiveUnitLayer, null);
    }

    /**
     * Native support extracted from CGameObject::MarkObjectLayerCell @00460591.
     */
    private CGameObject[][] layerForKind(int layerKind) {
        return switch (layerKind) {
            case MAP_LAYER_MISC -> miscObjectLayer;
            case MAP_LAYER_STRUCTURE -> structureObjectLayer;
            case MAP_LAYER_GROUND -> groundObjectLayer;
            case MAP_LAYER_AIR -> airObjectLayer;
            case MAP_LAYER_INACTIVE -> inactiveUnitLayer;
            default -> throw new IllegalArgumentException("Unsupported map layer kind " + layerKind);
        };
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43.
     */
    private boolean isObjectLayerWorldCell(int tileX, int tileY) {
        return view.x - RENDER_GRID_LEFT_GUARD <= tileX
                && tileX < view.x + gridWidth + RENDER_GRID_LEFT_GUARD
                && view.y - RENDER_GRID_TOP_GUARD <= tileY
                && tileY < view.y + gridHeight + 7;
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43.
     */
    private boolean isObjectLayerCell(int col, int row) {
        return -4 < col && -4 < row && col < gridWidth + 3 && row < gridHeight + 7;
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43.
     */
    private int objectLayerGridX(int col) {
        return col + RENDER_GRID_LEFT_GUARD;
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43.
     */
    private int objectLayerGridY(int row) {
        return row + RENDER_GRID_TOP_GUARD;
    }

    /**
     * Native support extracted from MapVisualObject::RebuildTileHeightRenderGrids @004065B1.
     */
    private int terrainVertexGridX(int col) {
        return col + RENDER_GRID_LEFT_GUARD;
    }

    /**
     * Native support extracted from MapVisualObject::RebuildTileHeightRenderGrids @004065B1.
     */
    private int terrainVertexGridY(int row) {
        return row + RENDER_GRID_TOP_GUARD;
    }

    /**
     * Native support extracted from MapVisualObject::RebuildTileHeightRenderGrids @004065B1.
     */
    private int tileAverageGridX(int col) {
        return col + RENDER_GRID_LEFT_GUARD;
    }

    /**
     * Native support extracted from MapVisualObject::RebuildTileHeightRenderGrids @004065B1.
     */
    private int tileAverageGridY(int row) {
        return row + RENDER_GRID_TOP_GUARD;
    }

    /**
     * Native support extracted from MapVisualObject::RecalculateGridMetrics @00402FB8.
     * Java-only zoom addition: stride follows max-zoom allocation width, not the current visible grid width.
     */
    private int objectLayerStride() {
        return allocatedGridWidth + OBJECT_LAYER_EXTRA_COLUMNS;
    }

    /**
     * Native support extracted from MapVisualObject::RecalculateGridMetrics @00402FB8.
     * Java-only zoom addition: stride follows max-zoom allocation width, not the current visible grid width.
     */
    private int terrainVertexStride() {
        return allocatedGridWidth + TERRAIN_VERTEX_EXTRA_COLUMNS;
    }

    /**
     * Native support extracted from MapVisualObject::RecalculateGridMetrics @00402FB8.
     * Java-only zoom addition: stride follows max-zoom allocation width, not the current visible grid width.
     */
    private int tileAverageStride() {
        return allocatedGridWidth + TILE_AVERAGE_EXTRA_COLUMNS;
    }

    /**
     * Native support extracted from MapVisualObject::RecalculateGridMetrics @00402FB8 grid clear loops.
     */
    private static void fillObjectGrid(CGameObject[][] grid, CGameObject value) {
        for (CGameObject[] column : grid) {
            Arrays.fill(column, value);
        }
    }

    /**
     * Native support extracted from MapVisualObject::RecalculateGridMetrics @00402FB8 grid clear loops.
     */
    private static void fillByteGrid(byte[][] grid, byte value) {
        for (byte[] column : grid) {
            Arrays.fill(column, value);
        }
    }

    /**
     * Native support extracted from FUN_0040403B @0040403B grid clear loops.
     */
    private static void fillIntGrid(int[][] grid, int value) {
        for (int[] column : grid) {
            Arrays.fill(column, value);
        }
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43 previous-grid copy.
     */
    private static void copyByteGrid(byte[][] source, byte[][] target) {
        for (int x = 0; x < source.length; x++) {
            System.arraycopy(source[x], 0, target[x], 0, source[x].length);
        }
    }

    /**
     * Native support extracted from FUN_0040403B @0040403B previous-grid copy.
     */
    private static void copyIntGrid(int[][] source, int[][] target) {
        for (int x = 0; x < source.length; x++) {
            System.arraycopy(source[x], 0, target[x], 0, source[x].length);
        }
    }

    /**
     * Native support extracted from MapVisualObject::RebuildTileHeightRenderGrids @004065B1.
     */
    private int mapHeightAt(int tileX, int tileY) {
        if (!isMapCell(tileX, tileY) || mapDescriptor.heightsWxH.length == 0) {
            return 0;
        }
        return mapDescriptor.heightAt(tileX, tileY);
    }

    /**
     * Native support extracted from MapVisualObject::RenderFrame @00406F43.
     */
    private boolean isMapCell(int tileX, int tileY) {
        return 0 <= tileX && tileX < cachedMapWidth && 0 <= tileY && tileY < cachedMapHeight;
    }

    /**
     * Native support extracted from MapVisualObject::RebuildTileHeightRenderGrids @004065B1.
     */
    private static int arithmeticDivideByFour(int value) {
        return (value + ((value >> 31) & 3)) >> 2;
    }

    /**
     * Native: MapVisualObject::SetCameraPosition @004162C2.
     * Fully ported.
     */
    private void setCameraPosition(int x, int y) {
        if (mapDescriptor == null) {
            return;
        }

        int clampedX = Math.max(MAP_CAMERA_EDGE_TILES, x);
        int clampedY = Math.max(MAP_CAMERA_EDGE_TILES, y);

        int maxX = maxCameraX();
        int maxY = maxCameraY();
        if (clampedX > maxX) {
            clampedX = maxX;
        }
        if (clampedY > maxY) {
            clampedY = maxY;
        }

        pendingCameraDeltaX = clampedX - view.x;
        pendingCameraDeltaY = clampedY - view.y;

        notifyMinimapContextChanged();
    }

    /**
     * Native: MapVisualObject::ScrollCameraXBy @00403F4F.
     * Fully ported.
     */
    public void scrollCameraXBy(int delta) {
        pendingCameraDeltaX += delta;
        int targetX = view.x + pendingCameraDeltaX;
        if (targetX < MAP_CAMERA_EDGE_TILES) {
            pendingCameraDeltaX = MAP_CAMERA_EDGE_TILES - view.x;
        }
        int maxX = maxCameraX();
        if (maxX < view.x + pendingCameraDeltaX) {
            pendingCameraDeltaX = maxX - view.x;
        }
    }

    /**
     * Native: MapVisualObject::ScrollCameraYBy @00403FC5.
     * Fully ported.
     */
    public void scrollCameraYBy(int delta) {
        pendingCameraDeltaY += delta;
        int targetY = view.y + pendingCameraDeltaY;
        if (targetY < MAP_CAMERA_EDGE_TILES) {
            pendingCameraDeltaY = MAP_CAMERA_EDGE_TILES - view.y;
        }
        int maxY = maxCameraY();
        if (maxY < view.y + pendingCameraDeltaY) {
            pendingCameraDeltaY = maxY - view.y;
        }
    }

    /**
     * Native support extracted from MapVisualObject::ScrollCameraXBy @00403F4F and
     * MapVisualObject::ScrollCameraYBy @00403FC5. Java-only zoom/layout support re-applies the same pending-target clamp
     * after grid metric changes that native never had.
     */
    private void clampPendingCameraDeltaToMapBounds() {
        int targetX = view.x + pendingCameraDeltaX;
        if (targetX < MAP_CAMERA_EDGE_TILES) {
            pendingCameraDeltaX = MAP_CAMERA_EDGE_TILES - view.x;
        }
        int maxX = maxCameraX();
        if (maxX < view.x + pendingCameraDeltaX) {
            pendingCameraDeltaX = maxX - view.x;
        }

        int targetY = view.y + pendingCameraDeltaY;
        if (targetY < MAP_CAMERA_EDGE_TILES) {
            pendingCameraDeltaY = MAP_CAMERA_EDGE_TILES - view.y;
        }
        int maxY = maxCameraY();
        if (maxY < view.y + pendingCameraDeltaY) {
            pendingCameraDeltaY = maxY - view.y;
        }
    }

    /**
     * Native: map setup branch in MapVisualObject::OnMessage @0040C1D0.
     */
    private void initializeUiFrame() {
        advanceMapObjectsAndEffects();
        gameListControl.advanceTimedLines();
        refreshTimeFlowLighting(false);
    }

    /**
     * Native: MapVisualObject::AdvanceMapObjectsAndEffects @0040BCD8.
     * Fully ported.
     */
    private void advanceMapObjectsAndEffects() {
        mapAnimationTick++;
        progressFloatingUnitTexts();
        if (Globals.terrainLightOverrideTransferMode == 0 && (mapAnimationTick & 0x1F) == 0) {
            rebuildMapOccupancy();
        }
        if ((mapAnimationTick & 0x0F) == 0 && Globals.soundPreferences.musicAvailable != 0 && pCUnit != null) {
            GameplayMusicSupport.updatePreferredGameplayTrackIndex(pCUnit.location.x, pCUnit.location.y);
        }

        boolean refreshSelection = advancePrimaryObjectMap();
        advanceTransientEffectCells();
        for (CGameObject object : objects.values()) {
            refreshSelection |= object.updateBlockedState();
        }
        if (refreshSelection) {
            updateSelectionState();
        }
        advanceTransientObjectMap();
    }

    /**
     * Native: MapVisualObject::RebuildMapOccupancy @0040C129.
     * Fully ported.
     */
    private void rebuildMapOccupancy() {
        mapOccupancyDirty = 1;
        areaEffectRefreshPending = 1;
        short[] tileFlags = mapDescriptor.tilesWxH;
        int cellCount = cachedMapWidth * cachedMapHeight;
        for (int i = 0; i < cellCount; i++) {
            tileFlags[i] = (short) (tileFlags[i] & ~TERRAIN_CURRENT_VISIBLE_MASK);
        }
        for (CGameObject object : objects.values()) {
            object.occupyMapCells();
        }
    }

    /**
     * Native support extracted from MapVisualObject::AdvanceMapObjectsAndEffects @0040BCD8 primary m_ObjectMap pass.
     */
    private boolean advancePrimaryObjectMap() {
        List<Short> removedObjectTokens = collectExpiredMapObjects(objects);
        for (short objectToken : removedObjectTokens) {
            CGameObject object = objects.remove(objectToken);
            if (object.isSelected()) {
                inputMode = 0;
                Globals.mainWindow.pSpellPanelVisualObject.clearSelectedSpellSlot();
                resetOrderToolbarSelection();
            }
        }
        return !removedObjectTokens.isEmpty();
    }

    /**
     * Native support extracted from MapVisualObject::AdvanceMapObjectsAndEffects @0040BCD8 m_ObjectMap2 pass.
     */
    private void advanceTransientObjectMap() {
        List<Short> removedObjectTokens = collectExpiredMapObjects(transientObjects);
        for (short objectToken : removedObjectTokens) {
            transientObjects.remove(objectToken);
        }
    }

    /**
     * Native support extracted from MapVisualObject::AdvanceMapObjectsAndEffects @0040BCD8 object-map scans.
     */
    private static List<Short> collectExpiredMapObjects(Map<Short, CGameObject> objectMap) {
        List<Short> removedObjectTokens = new ArrayList<>();
        for (Map.Entry<Short, CGameObject> entry : new ArrayList<>(objectMap.entrySet())) {
            CGameObject object = entry.getValue();
            if (!object.advanceMapObjectState()) {
                removedObjectTokens.add(entry.getKey());
            }
        }
        return removedObjectTokens;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @004128C0 HP-loss floating text branch.
     * Full port.
     */
    public void addFloatingUnitTextForHpLoss(CUnit unit, short newHp) {
        if (showFlyingHitPointBars == 0) {
            return;
        }
        if (unit.HP <= newHp) {
            return;
        }

        int moveLeftFlag = unit.cPlayer != currentPlayer ? 1 : 0;
        int tileWidth = unit.getTileWidth();
        int xOffset = moveLeftFlag != 0 ? -(tileWidth << 4) : tileWidth << 4;
        int yOffset = -tileWidth * 0x30;
        addOrMergeFloatingUnitText(new FloatingUnitText(unit.HP - newHp, null, moveLeftFlag, xOffset, yOffset, unit));
    }

    /**
     * Native support extracted from addOrMergeFloatingUnitText @0045C801.
     * Full port.
     */
    private void addOrMergeFloatingUnitText(FloatingUnitText incoming) {
        for (FloatingUnitText existing : floatingUnitTexts) {
            if (existing.hasSameMergeKey(incoming)) {
                existing.mergeValue(incoming);
                return;
            }
        }
        floatingUnitTexts.add(incoming);
    }

    /**
     * Native support extracted from CArray<FloatingUnitText>::progressAll @0045C7C5.
     * Full port.
     */
    private void progressFloatingUnitTexts() {
        for (FloatingUnitText floatingUnitText : floatingUnitTexts) {
            floatingUnitText.progress();
        }
    }

    /**
     * Native: MapVisualObject::AdvanceTransientEffectCells @0040C0B0.
     * Fully ported.
     */
    private void advanceTransientEffectCells() {
        Iterator<Map.Entry<Integer, Integer>> iterator = transientEffectCells.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            int phase = entry.getValue() + 1;
            if (phase < 0x16) {
                entry.setValue(phase);
            } else {
                iterator.remove();
            }
        }
    }

    /**
     * Native: MapVisualObject::RefreshLayoutAfterAction @00416388.
     * Fully ported.
     */
    public void refreshLayoutAfterAction() {
        if (inputMode == 0) {
            deselectAllMapObjects();
            updateSelectionState();
            return;
        }

        if (inputMode == INPUT_MODE_SPELL_CAST && hasSpellPanelChild()) {
            removeSpellPanel();
        }

        Globals.mainWindow.pSpellPanelVisualObject.clearSelectedSpellSlot();
        inputMode = 0;
        resetOrderToolbarSelection();
    }

    /**
     * Native: MapVisualObject::ExecuteOrderType @00418A02.
     * Fully ported.
     */
    private void executeOrderType(int orderType) {
        int availableOrderMask = availableOrderMask();
        int canonicalOrder = orderType;
        if (orderType == ORDER_TYPE_CAST_SLOT_A || orderType == ORDER_TYPE_CAST_SLOT_B) {
            canonicalOrder = INPUT_MODE_SPELL_CAST;
        }

        if (inputMode == INPUT_MODE_SPELL_CAST && canonicalOrder != INPUT_MODE_SPELL_CAST && hasSpellPanelChild()) {
            removeSpellPanel();
            Globals.mainWindow.pSpellPanelVisualObject.clearSelectedSpellSlot();
        }

        if (((availableOrderMask & (1 << ((canonicalOrder - 1) & 0x1F))) == 0) && orderType != ORDER_TYPE_CAST_SLOT_B) {
            return;
        }

        inputMode = canonicalOrder;
        Globals.mainWindow.pOrderToolbarVisualObject.onMessage(SELECT_ORDER_IN_TOOLBAR, canonicalOrder - 1, 0);

        switch (orderType) {
            case 3 -> {
                issueGuardOrder();
                inputMode = 0;
                resetOrderToolbarSelection();
            }
            case 7 -> {
                issueStandGroundOrder();
                inputMode = 0;
                resetOrderToolbarSelection();
            }
            case 8 -> {
                issueRetreatOrder();
                inputMode = 0;
                resetOrderToolbarSelection();
            }
            case 5 -> {
                if (!hasSpellPanelChild()) {
                    addSpellPanel();
                }
            }
            default -> {
            }
        }
    }

    /**
     * Native: TOGGLE_SELECTION_PANEL branch in MapVisualObject::OnMessage @0040C1D0.
     */
    private void toggleSelectionPanel() {
        if (hasSelectionPanelChild()) {
            removeSelectionPanel();
        } else {
            addSelectionPanel();
        }
        finishSelectionCursorFlowIfNeeded();
    }

    /**
     * Native support extracted from CMainWindow::runSessionBootstrap @0048C8A3 inventory-panel restore branch.
     */
    public void setSelectionPanelOpen(boolean open) {
        if (open) {
            if (!hasSelectionPanelChild()) {
                addSelectionPanel();
            }
        } else if (hasSelectionPanelChild()) {
            removeSelectionPanel();
        }
    }

    /**
     * Native: TOGGLE_SPELL_PANEL branch in MapVisualObject::OnMessage @0040C1D0.
     */
    private void toggleSpellPanel() {
        if (hasSpellPanelChild()) {
            removeSpellPanel();
        } else {
            addSpellPanel();
        }
        finishSelectionCursorFlowIfNeeded();
    }

    /**
     * Native support extracted from CMainWindow::runSessionBootstrap @0048C8A3 spell-panel restore branch.
     */
    public void setSpellPanelOpen(boolean open) {
        if (open) {
            if (!hasSpellPanelChild()) {
                addSpellPanel();
            }
        } else if (hasSpellPanelChild()) {
            removeSpellPanel();
        }
    }

    /**
     * Native: MapVisualObject::AddSelectionPanel @0041ADC2.
     * Fully ported.
     */
    private void addSelectionPanel() {
        if (hasSpellPanelChild()) {
            Globals.mainWindow.pSpellPanelVisualObject.setBounds(0, cRect.bottom - 0xAF, cRect.right, cRect.bottom - 0x5A);
        }

        addChild(Globals.mainWindow.pHeroInventoryControlVisualObject);
        SoundManager.playSfx(IBOOK);
        refreshPanelGridLayout();
    }

    /**
     * Native: MapVisualObject::RemoveSelectionPanel @0041AE4F.
     * Fully ported.
     */
    private void removeSelectionPanel() {
        if (hasSpellPanelChild()) {
            Globals.mainWindow.pSpellPanelVisualObject.setBounds(0, cRect.bottom - 0x55, cRect.right, cRect.bottom);
        }

        removeChild(Globals.mainWindow.pHeroInventoryControlVisualObject);
        SoundManager.playSfx(IBOOK);
        refreshPanelGridLayout();
    }

    /**
     * Native: MapVisualObject::AddSpellPanel @0041AF4A.
     * Fully ported.
     */
    private void addSpellPanel() {
        SpellPanelVisualObject spellPanel = Globals.mainWindow.pSpellPanelVisualObject;

        if (!hasSelectionPanelChild()) {
            spellPanel.setBounds(0, cRect.bottom - 0x55, cRect.right, cRect.bottom);
        } else {
            spellPanel.setBounds(0, cRect.bottom - 0xAF, cRect.right, cRect.bottom - 0x5A);
        }

        if (SHOP_DIALOG.isUnsetIn(Globals.mainWindow.dialogsMask)) {
            addChild(spellPanel);
        } else {
            spellPanel.setBounds(0, 0x131, 0x1E0, 0x186);
            Globals.mainWindow.inputController.getChildById(1000).addChild(spellPanel);
        }

        SoundManager.playSfx(IBOOK);
        Globals.mainWindow.pRightPanelContainerVisualObject.onMessage(NOTIFY_MAP_CONTEXT_CHANGED, 0, 0);
        refreshPanelGridLayout();
    }

    /**
     * Native: MapVisualObject::RemoveSpellPanel @0041B077.
     * Fully ported.
     */
    private void removeSpellPanel() {
        SpellPanelVisualObject spellPanel = Globals.mainWindow.pSpellPanelVisualObject;

        if (SHOP_DIALOG.isUnsetIn(Globals.mainWindow.dialogsMask)) {
            removeChild(spellPanel);
        } else {
            Globals.mainWindow.inputController.getChildById(1000).removeChild(spellPanel);
        }

        SoundManager.playSfx(IBOOK);
        Globals.mainWindow.pRightPanelContainerVisualObject.onMessage(NOTIFY_MAP_CONTEXT_CHANGED, 0, 0);
        refreshPanelGridLayout();
    }

    /**
     * Native support extracted from CMainWindow::showShopDialog @0048AEA8 and CMainWindow::showInnDialog @0048B885 via
     * MapVisualObject::RemoveSpellPanel @0041B077.
     */
    public void removeSpellPanelForTownDialog() {
        removeSpellPanel();
    }

    /**
     * Native: MapVisualObject::AfterShopDialogShown @0041A29B.
     * Fully ported.
     */
    public void afterShopDialogShown() {
        OpenShopDialogAction action = OpenShopDialogAction.global;
        action.ID.set(OpenShopDialogAction.ACTION_ID);
        action.netID.set(currentPlayer.playerId);
        action.playerID.set(0);
        action.unitTokenId.set(getSelectedCUnit().m_id);
        CServerApp.sendClientGameAction(action);
        Globals.mainWindow.dialogsMask = SHOP_DIALOG.includeTo(Globals.mainWindow.dialogsMask);
    }

    /**
     * Native: MapVisualObject::CloseShopDialog @0041A311.
     * Fully ported.
     */
    public void closeShopDialog() {
        CloseShopDialogAction action = CloseShopDialogAction.global;
        action.ID.set(CloseShopDialogAction.ACTION_ID);
        action.netID.set(currentPlayer.playerId);
        action.playerID.set(0);
        action.unitTokenId.set(getSelectedCUnit().m_id);
        CServerApp.sendClientGameAction(action);
        Globals.mainWindow.dialogsMask = SHOP_DIALOG.excludeIn(Globals.mainWindow.dialogsMask);
    }

    /**
     * Native: MapVisualObject::sendInventoryTransferAction @0041A20C.
     * Fully ported.
     */
    public void sendInventoryTransferAction(
            int sourceContainerType,
            int sourceSlot,
            int destinationContainerType,
            int destinationSlot,
            int quantityOrItemId
    ) {
        InventoryTransferAction action = InventoryTransferAction.global;
        action.ID.set(GameActionId.INVENTORY_TRANSFER_ACTION_22.id);
        action.netID.set(currentPlayer.playerId);
        action.playerID.set(0);
        action.unitTokenId.set(primarySelectedObject.m_id);
        action.sourceSlot.set(sourceSlot & 0xFFFF);
        action.sourceContainerType.set(sourceContainerType & 0xFF);
        action.destinationSlot.set(destinationSlot & 0xFFFF);
        action.destinationContainerType.set(destinationContainerType & 0xFF);
        action.quantityOrItemId.set(quantityOrItemId & 0xFFFF);
        CServerApp.sendClientGameAction(action);
    }

    /**
     * Native: MapVisualObject::sendDropGoldAction @0041A6CD.
     * Fully ported.
     */
    public void sendDropGoldAction(int amount, int targetPackedCell) {
        DropGoldAction action = DropGoldAction.global;
        action.ID.set(DropGoldAction.ACTION_ID);
        action.netID.set(currentPlayer.playerId);
        action.playerID.set(0);
        action.firstPayloadDword.set(amount);
        action.secondPayloadDword.set(targetPackedCell);
        CServerApp.sendClientGameAction(action);
    }

    /**
     * Native: MapVisualObject::CommitShopBuyAction @0041A387.
     * Fully ported.
     */
    public void commitShopBuyAction() {
        sendShopUnitTokenAction(ShopBuyAction.global, ShopBuyAction.ACTION_ID);
    }

    /**
     * Native: MapVisualObject::CommitShopSellAction @0041A3E1.
     * Fully ported.
     */
    public void commitShopSellAction() {
        sendShopUnitTokenAction(ShopSellAction.global, ShopSellAction.ACTION_ID);
    }

    /**
     * Native: MapVisualObject::CommitShopUndoAction @0041A43B.
     * Fully ported.
     */
    public void commitShopUndoAction() {
        sendShopUnitTokenAction(UndoShopAction.global, UndoShopAction.ACTION_ID);
    }

    /**
     * Native support extracted from MapVisualObject::CommitShopBuyAction @0041A387,
     * MapVisualObject::CommitShopSellAction @0041A3E1, and MapVisualObject::CommitShopUndoAction @0041A43B.
     */
    private void sendShopUnitTokenAction(UnitTokenAction action, int actionId) {
        action.ID.set(actionId);
        action.netID.set(currentPlayer.playerId);
        action.playerID.set(0);
        action.unitTokenId.set(primarySelectedObject.m_id);
        CServerApp.sendClientGameAction(action);
    }

    /**
     * Native: MapVisualObject::PrepareRemoteInnDialog @0041A800.
     * Fully ported.
     */
    public void prepareRemoteInnDialog() {
        EnterInnAction action = EnterInnAction.global;
        action.ID.set(EnterInnAction.ACTION_ID);
        action.netID.set(currentPlayer.playerId);
        action.playerID.set(0);
        action.unitTokenId.set(getSelectedCUnit().m_id);
        CServerApp.sendClientGameAction(action);
    }

    /**
     * Native: MapVisualObject::commitLeaveInnSelection @0041A861.
     * Fully ported.
     */
    public void commitLeaveInnSelection(int leaveSelectionValue) {
        if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN) {
            removeCampaignInnTemporaryUnits();
            return;
        }
        CServerApp.sendClientGameAction(
                LeaveInnAction.prepareForLeaveInn(
                        getSelectedCUnit().m_id,
                        currentPlayer.playerId,
                        leaveSelectionValue
                )
        );
    }

    /**
     * Native support extracted from MapVisualObject::commitLeaveInnSelection @0041A861 campaign cleanup branch.
     */
    private void removeCampaignInnTemporaryUnits() {
        List<Short> temporaryUnitTokens = new ArrayList<>();
        for (Map.Entry<Short, CGameObject> entry : objects.entrySet()) {
            if (entry.getValue() instanceof CUnit unit && (unit.unitFlags & 0x1) == 0) {
                temporaryUnitTokens.add(entry.getKey());
            }
        }
        for (Short token : temporaryUnitTokens) {
            removeObjectByToken(token);
        }
    }

    /**
     * Native: MapVisualObject::requestPlayerStateResync @0041AB50.
     * Fully ported.
     */
    public void requestPlayerStateResync() {
        RequestPlayerStateResyncAction action = RequestPlayerStateResyncAction.global;
        action.ID.set(RequestPlayerStateResyncAction.ACTION_ID);
        action.netID.set(currentPlayer.playerId);
        action.playerID.set(0);
        CServerApp.sendClientGameAction(action);
    }

    /**
     * Native: MapVisualObject::sendSaveGameRequestAction @0041AA10.
     * Fully ported.
     */
    public void sendSaveGameRequestAction(String saveFileName) {
        SaveGameRequestAction action = SaveGameRequestAction.global;
        action.ID.set(SaveGameRequestAction.ACTION_ID);
        action.netID.set(currentPlayer.playerId);
        action.playerID.set(0);
        action.text.set(saveFileName);
        CServerApp.sendClientGameAction(action);
    }

    /**
     * Native support boundary extracted from MapVisualObject::HandleGameAction(map, 0), called by
     * CMainWindow::WindowProc @004852D8 in the `CONTINUE_SCENARIO_LOCATION_ENTRY` mission-location branch.
     */
    public void pumpPendingGameActions() {
        handleGameAction(null, 0);
    }

    /**
     * Native support extracted from the direct-delivery server pump used by MapVisualObject::sendPlayerJoinAndWaitForPlayerList @0040D791.
     */
    private static void pumpLocalServerActionsIfOwned() {
        if (Globals.mainWindow.serverBootstrapEnabled != 0) {
            Globals.gameServer.pumpServerWorldActions();
        }
    }

    /**
     * Native support extracted from MapVisualObject::RefreshShopShelves @0041AAA5, called by
     * CMainWindow::WindowProc @004852D8 in the `CONTINUE_SCENARIO_LOCATION_ENTRY` non-mission branch.
     * Fully ported support wrapper for the native `(0, 0)` call site.
     */
    public void refreshShopShelves() {
        refreshShopShelves(0, 0);
    }

    /**
     * Native: MapVisualObject::RefreshShopShelves @0041AAA5.
     * Fully ported. Downstream CServerApp.sendClientGameAction remains the shared Java transport boundary.
     */
    public void refreshShopShelves(int firstPayloadDword, int secondPayloadDword) {
        RefreshShopShelvesAction action = RefreshShopShelvesAction.global;
        action.ID.set(RefreshShopShelvesAction.ACTION_ID);
        action.firstPayloadDword.set(firstPayloadDword);
        action.secondPayloadDword.set(secondPayloadDword);
        action.netID.set(currentPlayer.playerId);
        action.playerID.set(0);
        CServerApp.sendClientGameAction(action);
    }

    /**
     * Native: MapVisualObject::RefreshPanelGridLayout @0041B121.
     * Skipped exact native grid-height rewrite: Java-only zoom keeps gridHeight as the current logical zoom height and
     * leaves panels to overdraw the scaled map, otherwise opening a panel would silently destroy the configured zoom.
     * The camera clamp, status-banner layout refresh, and dirty-frame flag are represented here.
     */
    private void refreshPanelGridLayout() {
        applyJavaZoomGridMetrics();
        allocateRenderGrids();
        renderFrameDirty = 1;
        clampViewToMapBounds();
        clampPendingCameraDeltaToMapBounds();

        if (getChildById(STATUS_BANNER_INPUT_DIALOG.id) != null) {
            Globals.mainWindow.pChatVisualObject.refreshMapPanelLayout();
        }
    }

    /**
     * Native: cursor-finalization branch in MapVisualObject::OnMessage @0040C1D0.
     */
    private void finishSelectionCursorFlowIfNeeded() {
        if (!Globals.mousePointer.isSelecting()) {
            return;
        }

        Globals.mousePointer.finishSelectionDrag();
        Globals.mainWindow.clipCursorTo();
        selectCursor();
        updateMouseCursorAt(Globals.mousePointer.getX(), Globals.mousePointer.getY());
    }

    /**
     * Native: MapVisualObject::SelectMapCursor @0040B2B8.
     * Fully ported.
     */
    private void selectCursor() {
        CMainWindow mainWindow = Globals.mainWindow;
        if (GAMEPLAY.isUnsetIn(mainWindow.dialogsMask)) {
            return;
        }

        CGameBitmap currentBitmap = Globals.mousePointer.getSourceBitmap();
        CRect mapScreenRect = new CRect();
        clientToScreen(mapScreenRect, cRect);
        int mouseX = Globals.mousePointer.getX();
        int mouseY = Globals.mousePointer.getY();
        if (!mapScreenRect.contains(mouseX, mouseY)) {
            refreshMouseTargetFlags();
            return;
        }

        CCursor cursor = edgeScrollCursorForPoint(mouseX, mouseY);
        if (cursor == null) {
            cursor = resolveMapCursorInsideViewport(mainWindow);
        }
        CCursor chatResizeCursor = mainWindow.pChatVisualObject.resizeCursorForPoint(mouseX, mouseY);
        if (chatResizeCursor != null) {
            cursor = chatResizeCursor;
        } else if (isPointInsideChild(getChildById(2), mouseX, mouseY)
                || isPointInsideChild(getChildById(3), mouseX, mouseY)) {
            cursor = CMousePointer.Cursor_Default;
        }
        if (mainWindow.uiLockPayload != null) {
            cursor = resolveUiLockCursor(mapScreenRect);
        }
        if (cursor != null && currentBitmap != cursor.getBitmap()) {
            cursor.setToMousePointer();
        }
    }

    /**
     * Native support extracted from MapVisualObject::SelectMapCursor @0040B2B8 viewport branch.
     */
    private CCursor resolveMapCursorInsideViewport(CMainWindow mainWindow) {
        CRect selectionRect = new CRect(Globals.mousePointer.getSelectionRect()).normalize();
        if (inputMode != 0) {
            int targetFlags = refreshMouseTargetFlags();
            CCursor cursor = resolveInputModeCursor();
            if ((targetFlags & MAP_TARGET_BLOCKED_TERRAIN) != 0 && cursor == CMousePointer.Cursor_Cast) {
                cursor = CMousePointer.Cursor_Move;
            }
            return applySelectionDragCursor(cursor, selectionRect);
        }

        SpellPanelVisualObject spellPanel = mainWindow.pSpellPanelVisualObject;
        if (isSpellCursorBranchActive(spellPanel)) {
            return resolveSpellMapCursor(spellPanel);
        }
        int targetFlags = refreshMouseTargetFlags();
        return applySelectionDragCursor(resolveNormalMapCursor(targetFlags), selectionRect);
    }

    /**
     * Native support extracted from MapVisualObject::SelectMapCursor @0040B2B8 spell-panel gate.
     */
    private boolean isSpellCursorBranchActive(SpellPanelVisualObject spellPanel) {
        return hasSpellPanelChild()
                && spellPanel != null
                && spellPanel.getActiveSpellSlot() >= 0
                && (selectedAvailableSpellMask != 0 || activeSpellEffectMask != 0)
                && (selectionFlags & FLAG_BUSY) == 0;
    }

    /**
     * Native support extracted from MapVisualObject::SelectMapCursor @0040B2B8 non-spell cursor branch.
     */
    private CCursor resolveNormalMapCursor(int targetFlags) {
        if (selectedCount == 0) {
            return (targetFlags & MAP_TARGET_SELECTABLE_OBJECT) == 0
                    ? CMousePointer.Cursor_Default
                    : CMousePointer.Cursor_Select;
        }
        if ((selectionFlags & (FLAG_BUSY | FLAG_STRUCTURE)) != 0) {
            return (targetFlags & MAP_TARGET_SELECTABLE_OBJECT) == 0
                    ? CMousePointer.Cursor_Default
                    : CMousePointer.Cursor_Select;
        }

        boolean pickupTarget = hasPickupCursorTarget(targetFlags);
        boolean usableStructureTarget = hasUsableStructureCursorTarget(targetFlags);
        if ((targetFlags & MAP_TARGET_ENEMY) == 0) {
            if ((targetFlags & MAP_TARGET_SELECTABLE_OBJECT) == 0) {
                if (Globals.controlKeyDown) {
                    return CMousePointer.Cursor_Swarm;
                }
                if (Globals.altKeyDown) {
                    return CMousePointer.Cursor_Move;
                }
                return pickupTarget ? CMousePointer.Cursor_Pickup : CMousePointer.Cursor_Move;
            }
            if (Globals.altKeyDown) {
                return CMousePointer.Cursor_Move;
            }
            if (Globals.controlKeyDown) {
                return CMousePointer.Cursor_Attack;
            }
            if (pickupTarget) {
                return CMousePointer.Cursor_Pickup;
            }
            if (usableStructureTarget) {
                return CMousePointer.Cursor_Town;
            }
            return CMousePointer.Cursor_Select;
        }
        if (Globals.altKeyDown) {
            return CMousePointer.Cursor_Move;
        }
        if ((targetFlags & MAP_TARGET_STRUCTURE) != 0) {
            return CMousePointer.Cursor_Select;
        }
        return Globals.controlKeyDown ? CMousePointer.Cursor_Swarm : CMousePointer.Cursor_Attack;
    }

    /**
     * Native support extracted from MapVisualObject::SelectMapCursor @0040B2B8 active-spell cursor branch.
     */
    private CCursor resolveSpellMapCursor(SpellPanelVisualObject spellPanel) {
        int targetFlags = refreshMouseTargetFlags();
        CCursor cursor = CMousePointer.Cursor_Cast;
        if ((targetFlags & MAP_TARGET_BLOCKED_TERRAIN) != 0) {
            cursor = CMousePointer.Cursor_Move;
        }
        if ((targetFlags & (MAP_TARGET_UNIT | MAP_TARGET_AIR)) == 0 && spellPanel.activeSpellTargetsUnit()) {
            cursor = CMousePointer.Cursor_Move;
        }

        boolean pickupTarget = hasPickupCursorTarget(targetFlags);
        boolean usableStructureTarget = hasUsableStructureCursorTarget(targetFlags);
        if (Globals.altKeyDown) {
            cursor = CMousePointer.Cursor_Move;
        } else if (Globals.controlKeyDown) {
            cursor = (targetFlags & (MAP_TARGET_UNIT | MAP_TARGET_AIR)) == 0
                    ? CMousePointer.Cursor_Swarm
                    : CMousePointer.Cursor_Attack;
        } else if ((targetFlags & (MAP_TARGET_ENEMY | MAP_TARGET_STRUCTURE)) == 0 && Globals.shiftKeyDown) {
            cursor = CMousePointer.Cursor_Select;
        }
        if (pickupTarget && cursor == CMousePointer.Cursor_Move) {
            cursor = CMousePointer.Cursor_Pickup;
        }
        if (cursor == CMousePointer.Cursor_Cast
                && spellPanel.activeSpellTargetsUnit()
                && (targetFlags & MAP_TARGET_ENEMY) == 0
                && !spellPanel.hasSelectedSpellSlot()
                && spellPanel.activePressedSpellSelectsUnitCursor()) {
            cursor = (targetFlags & (MAP_TARGET_UNIT | MAP_TARGET_AIR)) == 0
                    ? CMousePointer.Cursor_Move
                    : CMousePointer.Cursor_Select;
        }
        if (usableStructureTarget && (targetFlags & MAP_TARGET_ENEMY) == 0) {
            cursor = CMousePointer.Cursor_Town;
        }
        refreshMouseTargetFlags();
        return cursor;
    }

    /**
     * Native support extracted from MapVisualObject::SelectMapCursor @0040B2B8 inputMode cursor branch.
     */
    private CCursor resolveInputModeCursor() {
        return switch (inputMode) {
            case 1 -> CMousePointer.Cursor_Attack;
            case 2 -> CMousePointer.Cursor_Move;
            case 4 -> CMousePointer.Cursor_Defend;
            case INPUT_MODE_SPELL_CAST -> CMousePointer.Cursor_Cast;
            case 6 -> CMousePointer.Cursor_Swarm;
            case 8 -> CMousePointer.Cursor_Patrol;
            default -> null;
        };
    }

    /**
     * Native support extracted from MapVisualObject::SelectMapCursor @0040B2B8 selection-rectangle override.
     */
    private CCursor applySelectionDragCursor(CCursor cursor, CRect selectionRect) {
        if (Globals.mousePointer.isSelecting() && !selectionRect.isEmpty()) {
            return CMousePointer.Cursor_Default;
        }
        return cursor;
    }

    /**
     * Native support extracted from MapVisualObject::SelectMapCursor @0040B2B8 screen-edge cursor branch.
     */
    private static CCursor edgeScrollCursorForPoint(int x, int y) {
        int left = Globals.screenRect.left;
        int top = Globals.screenRect.top;
        int eastEdge = Globals.screenRect.right - 2;
        int southEdge = Globals.screenRect.bottom - 2;
        if (x == left) {
            if (y == top) {
                return CMousePointer.Cursor_ArrowNW;
            }
            return y < southEdge ? CMousePointer.Cursor_ArrowW : CMousePointer.Cursor_ArrowSW;
        }
        if (x < eastEdge) {
            if (y == top) {
                return CMousePointer.Cursor_ArrowN;
            }
            return y < southEdge ? null : CMousePointer.Cursor_ArrowS;
        }
        if (y == top) {
            return CMousePointer.Cursor_ArrowNE;
        }
        return y < southEdge ? CMousePointer.Cursor_ArrowE : CMousePointer.Cursor_ArrowSE;
    }

    /**
     * Native: MapVisualObject::RefreshMouseTargetFlags @004180C6.
     * Fully ported.
     */
    private int refreshMouseTargetFlags() {
        CMainWindow mainWindow = Globals.mainWindow;
        CGameObject previousHoveredObject = hoveredObject;
        short previousHoveredToken = hoveredObjectToken;
        clearHoveredTargetState();

        CRect mapScreenRect = new CRect();
        clientToScreen(mapScreenRect, cRect);
        int mouseX = Globals.mousePointer.getX();
        int mouseY = Globals.mousePointer.getY();
        if (!mapScreenRect.contains(mouseX, mouseY)) {
            notifyHoveredObjectChanged(mainWindow, previousHoveredObject, previousHoveredToken);
            return 0;
        }

        Point logicalPoint = javaZoomScreenToLogicalMapPoint(mapScreenRect, mouseX, mouseY);
        int targetFlags = refreshHoveredObjectFromSelectionRects(logicalPoint.x, logicalPoint.y);
        if (isPointInsideChild(getChildById(2), mouseX, mouseY)
                || isPointInsideChild(getChildById(3), mouseX, mouseY)) {
            hoveredObject = null;
            hoveredObjectToken = 0;
        }

        int tileX = logicalPoint.x >> 5;
        int tileY = getTileYForScreenPoint(logicalPoint.x, logicalPoint.y);
        int occupancyMask = combinedTileOccupancyMask(tileX, tileY);
        if (occupancyMask == TERRAIN_LIGHT_FULLY_BLOCKED_MASK
                && miscObjectLayer[objectLayerGridX(tileX)][objectLayerGridY(tileY)] != null) {
            targetFlags |= MAP_TARGET_OCCUPIED_MISC;
        }
        if (occupancyMask != TERRAIN_LIGHT_FULLY_BLOCKED_MASK) {
            targetFlags |= MAP_TARGET_BLOCKED_TERRAIN;
        }
        hoveredTileX = tileX + view.x;
        hoveredTileY = tileY + view.y;
        notifyHoveredObjectChanged(mainWindow, previousHoveredObject, previousHoveredToken);
        return targetFlags;
    }

    /**
     * Native support extracted from MapVisualObject::RefreshMouseTargetFlags @004180C6 target-object hit loop.
     */
    private int refreshHoveredObjectFromSelectionRects(int localX, int localY) {
        CRect pickRect = new CRect(localX - 1, localY - 1, localX + 1, localY + 1);
        for (int index = objectSelectionCount - 1; index >= 0; index--) {
            CRect selectionRect = new CRect(objectSelectionRects.get(index)).normalize();
            CRect intersection = new CRect(selectionRect);
            intersection.intersect(pickRect);
            if (intersection.isEmpty()) {
                continue;
            }

            short objectToken = (short) (int) objectSelectionIds.get(index);
            CGameObject object = getObjectByToken(objectToken);
            if (object == null) {
                break;
            }
            if (isIgnoredIndestructibleStructureHit(object)) {
                continue;
            }

            int targetFlags = classifyHoveredTarget(object);
            if (currentPlayer.isEnemy(object.cPlayer.playerId)) {
                targetFlags |= MAP_TARGET_ENEMY;
            }
            hoveredObject = object;
            hoveredObjectToken = objectToken;
            refreshHoveredQuestKeys(object);
            return targetFlags;
        }
        return 0;
    }

    /**
     * Native support extracted from MapVisualObject::RefreshMouseTargetFlags @004180C6 indestructible-structure skip.
     */
    private static boolean isIgnoredIndestructibleStructureHit(CGameObject object) {
        if (object.getClass() != CStructure.class) {
            return false;
        }
        StructureDef def = Objects.requireNonNull(
                Structures.getStructureDef(object.type),
                "Missing StructureDef for id " + object.type
        );
        return def.usable == 0 && def.indestructible != 0;
    }

    /**
     * Native support extracted from MapVisualObject::RefreshMouseTargetFlags @004180C6 runtime-class target bits.
     */
    private static int classifyHoveredTarget(CGameObject object) {
        int targetFlags = 0;
        if (object instanceof CAirUnit) {
            targetFlags |= MAP_TARGET_AIR;
        } else if (object instanceof CUnit) {
            targetFlags |= MAP_TARGET_UNIT;
        }
        if (object.getClass() == CStructure.class) {
            StructureDef def = Objects.requireNonNull(
                    Structures.getStructureDef(object.type),
                    "Missing StructureDef for id " + object.type
            );
            if (def.usable != 0) {
                targetFlags |= MAP_TARGET_USABLE_STRUCTURE;
            }
            if (def.usable != 0 || def.indestructible == 0) {
                targetFlags |= MAP_TARGET_STRUCTURE;
            }
        }
        return targetFlags;
    }

    /**
     * Native support extracted from MapVisualObject::RefreshMouseTargetFlags @004180C6 quest-hover key recovery.
     */
    private void refreshHoveredQuestKeys(CGameObject object) {
        if (Globals.mainWindow.sessionMode == CMainWindow.SESSION_MODE_CAMPAIGN) {
            return;
        }

        int objectId = object.m_id & 0xFFFF;
        for (Quest quest : questStorage.questsByKey.values()) {
            int questId = quest.getId();
            if (objectId == (quest.primaryArgument & 0xFFFF) && quest.state == 0 && questId != 3 && questId != 0x0C) {
                hoveredPrimaryQuestKey = quest.questKey;
            } else if (objectId == (quest.secondaryIndexKey & 0xFFFF)) {
                hoveredSecondaryQuestKey = quest.questKey;
            }
            if (objectId == (quest.mapNumber & 0xFFFF)) {
                hoveredMapQuestKey = quest.questKey;
            }
            if (object instanceof CUnit unit && quest.state == 0) {
                if (unit.actionPhase == quest.primaryArgument && (questId == 3 || questId == 0x0C)) {
                    hoveredUnitActionQuestKey = quest.questKey;
                }
                if (unit.type == (quest.primaryArgument & 0xFF)
                        && unit.field8_0x28 == (quest.primaryArgument >>> 8)
                        && questId == 2) {
                    hoveredPrimaryQuestKey = quest.questKey;
                }
                if (unit.cPlayer.playerId == quest.primaryArgument && questId == 0x0D) {
                    hoveredPrimaryQuestKey = quest.questKey;
                }
            }
        }
    }

    /**
     * Native support extracted from QuestStatusDialogVisualObject::Update @004DF659 hovered quest-key comparison.
     * Fully ported.
     */
    public boolean matchesHoveredQuestKey(int questKey) {
        return hoveredPrimaryQuestKey == questKey
                || hoveredSecondaryQuestKey == questKey
                || hoveredMapQuestKey == questKey
                || hoveredUnitActionQuestKey == questKey;
    }

    /**
     * Native support extracted from SelectionInfoPanelVisualObject::Update @004AF3BF unit picture quest-hover overlay.
     */
    boolean hasSelectionInfoUnitQuestHover() {
        return hoveredPrimaryQuestKey != 0 || hoveredUnitActionQuestKey != 0;
    }

    /**
     * Native support extracted from SelectionInfoPanelVisualObject::Update @004AF3BF structure region quest-hover overlay.
     */
    boolean hasSelectionInfoRegionQuestHover() {
        return hoveredSecondaryQuestKey != 0;
    }

    /**
     * Native support extracted from SelectionInfoPanelVisualObject::Update @004AF3BF structure customer quest-hover overlay.
     */
    boolean hasSelectionInfoCustomerQuestHover() {
        return hoveredMapQuestKey != 0;
    }

    /**
     * Native support extracted from MapVisualObject::RefreshMouseTargetFlags @004180C6 state reset.
     */
    private void clearHoveredTargetState() {
        hoveredObject = null;
        hoveredObjectToken = 0;
        hoveredPrimaryQuestKey = 0;
        hoveredSecondaryQuestKey = 0;
        hoveredMapQuestKey = 0;
        hoveredUnitActionQuestKey = 0;
    }

    /**
     * Native support extracted from CharacterLoaderDialogVisualObject::ShowDialog @004319F7.
     * Fully ported.
     */
    public void clearCharacterLoaderQuestHoverKeys() {
        hoveredPrimaryQuestKey = 0;
        hoveredSecondaryQuestKey = 0;
        hoveredMapQuestKey = 0;
        hoveredUnitActionQuestKey = 0;
    }

    /**
     * Native support extracted from MapVisualObject::RefreshMouseTargetFlags @004180C6 panel notifications.
     */
    private void notifyHoveredObjectChanged(CMainWindow mainWindow, CGameObject previousHoveredObject, short previousHoveredToken) {
        if (hoveredObject == previousHoveredObject) {
            return;
        }
        mainWindow.pSelectionInfoPanelVisualObject.onMessage(NOTIFY_MAP_CONTEXT_CHANGED, 0, 0);
        mainWindow.pSideStatusVisualObject.onMessage(NOTIFY_MAP_CONTEXT_CHANGED, 0, 0);
        if (hoveredObject != null) {
            hoveredObject.m_bSelectionDirty = 1;
        }
        CGameObject previousTokenObject = getObjectByToken(previousHoveredToken);
        if (previousTokenObject != null) {
            previousTokenObject.m_bSelectionDirty = 1;
        }
    }

    /**
     * Native support extracted from MapVisualObject::SelectMapCursor @0040B2B8 pickup cursor tests.
     */
    private boolean hasPickupCursorTarget(int targetFlags) {
        return hasSingleHumanoidUnitSelection()
                && (targetFlags & MAP_TARGET_OCCUPIED_MISC) != 0
                && (targetFlags == MAP_TARGET_OCCUPIED_MISC || hoveredObject == primarySelectedObject);
    }

    /**
     * Native support extracted from MapVisualObject::SelectMapCursor @0040B2B8 usable-structure cursor tests.
     */
    private boolean hasUsableStructureCursorTarget(int targetFlags) {
        return hasSingleHumanoidUnitSelection() && (targetFlags & MAP_TARGET_USABLE_STRUCTURE) != 0;
    }

    /**
     * Native support extracted from MapVisualObject::SelectMapCursor @0040B2B8 primary humanoid selection tests.
     */
    private boolean hasSingleHumanoidUnitSelection() {
        return selectedCount == 1
                && (selectionFlags & FLAG_UNIT) != 0
                && primarySelectedObject instanceof CUnit unit
                && (unit.unitFlags & UNIT_FLAG_HUMANOID) != 0;
    }

    /**
     * Native support extracted from MapVisualObject::SelectMapCursor @0040B2B8 ui-lock cursor branch.
     */
    private CCursor resolveUiLockCursor(CRect mapScreenRect) {
        CCursor cursor = Globals.mainWindow.cursor;
        int targetFlags = refreshMouseTargetFlags();
        if (selectedCount == 1
                && primarySelectedObject != null
                && primarySelectedObject.cPlayer == currentPlayer
                && hasSingleHumanoidUnitSelection()
                && (targetFlags & MAP_TARGET_UNIT) != 0
                && isMouseWithinBackpackReach(mapScreenRect)) {
            return CMousePointer.Cursor_Backpack;
        }
        return cursor;
    }

    /**
     * Native support extracted from MapVisualObject::SelectMapCursor @0040B2B8 backpack reach check.
     */
    private boolean isMouseWithinBackpackReach(CRect mapScreenRect) {
        Point logicalPoint = javaZoomScreenToLogicalMapPoint(
                mapScreenRect,
                Globals.mousePointer.getX(),
                Globals.mousePointer.getY()
        );
        int worldTileX = (logicalPoint.x >> 5) + view.x;
        int worldTileY = getTileYForScreenPoint(logicalPoint.x, logicalPoint.y) + view.y;
        return Math.abs(worldTileX - primarySelectedObject.tileX) < 3
                && Math.abs(worldTileY - primarySelectedObject.tileY) < 3;
    }

    /**
     * Native support extracted from MapVisualObject::UpdateMouseCursor @004175C7 source-bitmap dispatch.
     */
    private static CCursor currentMouseCursor() {
        CGameBitmap bitmap = Globals.mousePointer.getSourceBitmap();
        if (bitmap == CMousePointer.Cursor_Default.getBitmap()) {
            return CMousePointer.Cursor_Default;
        }
        if (bitmap == CMousePointer.Cursor_Move.getBitmap()) {
            return CMousePointer.Cursor_Move;
        }
        if (bitmap == CMousePointer.Cursor_Swarm.getBitmap()) {
            return CMousePointer.Cursor_Swarm;
        }
        if (bitmap == CMousePointer.Cursor_Attack.getBitmap()) {
            return CMousePointer.Cursor_Attack;
        }
        if (bitmap == CMousePointer.Cursor_Defend.getBitmap()) {
            return CMousePointer.Cursor_Defend;
        }
        if (bitmap == CMousePointer.Cursor_Select.getBitmap()) {
            return CMousePointer.Cursor_Select;
        }
        if (bitmap == CMousePointer.Cursor_Patrol.getBitmap()) {
            return CMousePointer.Cursor_Patrol;
        }
        if (bitmap == CMousePointer.Cursor_Cast.getBitmap()) {
            return CMousePointer.Cursor_Cast;
        }
        if (bitmap == CMousePointer.Cursor_Pickup.getBitmap()) {
            return CMousePointer.Cursor_Pickup;
        }
        if (bitmap == CMousePointer.Cursor_Town.getBitmap()) {
            return CMousePointer.Cursor_Town;
        }
        if (bitmap == CMousePointer.Cursor_Backpack.getBitmap()) {
            return CMousePointer.Cursor_Backpack;
        }
        return null;
    }

    /**
     * Native support extracted from MapVisualObject::SelectMapCursor @0040B2B8 and RefreshMouseTargetFlags @004180C6 child-panel hit tests.
     */
    private static boolean isPointInsideChild(CVisualObject child, int x, int y) {
        if (child == null) {
            return false;
        }
        CRect childScreenRect = new CRect();
        child.clientToScreen(childScreenRect, child.getRect());
        return childScreenRect.contains(x, y);
    }

    /**
     * Native: MapVisualObject::UpdateMouseCursor @004175C7.
     * Fully ported. Java maps zoomed screen coordinates to the native logical map rectangle before dispatch.
     */
    private void updateMouseCursorAt(int x, int y) {
        CRect selectionRect = new CRect(Globals.mousePointer.getSelectionRect());
        selectionRect.right = x;
        selectionRect.bottom = y;
        selectionRect.normalize();
        if (selectionRect.isEmpty()) {
            selectionRect.left--;
            selectionRect.top--;
            selectionRect.right++;
            selectionRect.bottom++;
        }

        CRect mapScreenRect = new CRect();
        clientToScreen(mapScreenRect, cRect);
        selectionRect = javaZoomScreenRectToLogicalMapRect(mapScreenRect, selectionRect);
        int threshold = selectionTinyThreshold();
        if (selectionRect.width() > threshold || selectionRect.height() > threshold || selectedCount == 0) {
            applySelectionRectangle(selectionRect);
            return;
        }

        CCursor cursor = currentMouseCursor();
        if (cursor == CMousePointer.Cursor_Attack) {
            if (hoveredObjectToken == 0 || !(hoveredObject instanceof CUnit)) {
                issueMinimapMoveOrder(hoveredTileX);
            } else {
                issueMinimapAttackTargetOrder(hoveredObjectToken);
            }
        } else if (cursor == CMousePointer.Cursor_Swarm) {
            issueMinimapAttackCellOrder(hoveredTileX, hoveredTileY);
        } else if (cursor == CMousePointer.Cursor_Move) {
            issueMinimapMoveOrder(hoveredTileX);
        } else if (cursor == CMousePointer.Cursor_Patrol) {
            issueMinimapPatrolOrder(hoveredTileX, hoveredTileY);
        } else if (cursor == CMousePointer.Cursor_Defend) {
            if (hoveredObjectToken != 0) {
                issueMinimapDefendTargetOrder(hoveredObjectToken);
            }
        } else if (cursor == CMousePointer.Cursor_Select) {
            applySelectionRectangle(selectionRect);
        } else if (cursor == CMousePointer.Cursor_Pickup) {
            issuePickupOrder(hoveredTileX, hoveredTileY);
        } else if (cursor == CMousePointer.Cursor_Town) {
            issueEnterTownOrder(hoveredObjectToken);
        } else if (cursor == CMousePointer.Cursor_Cast) {
            boolean activeSpellSlot = handleMapCastClick(hoveredTileX, hoveredTileY);
            if (activeSpellSlot && inputMode == INPUT_MODE_SPELL_CAST && hasSpellPanelChild()) {
                removeSpellPanel();
            }
        }

        clearInputModeAfterMapClick();
    }

    /**
     * Native: MapVisualObject::ApplySelectionRectangle @00417986.
     * Fully ported.
     */
    private void applySelectionRectangle(CRect selectionRect) {
        int threshold = selectionTinyThreshold();
        boolean tinySelection = selectionRect.width() < threshold && selectionRect.height() < threshold;
        boolean shouldClearSelection = !Globals.shiftKeyDown;

        if (!tinySelection && !Globals.shiftKeyDown) {
            shouldClearSelection = hasOwnedUnitSelectionHit(selectionRect);
            if (!shouldClearSelection) {
                return;
            }
        } else if (tinySelection && !Globals.shiftKeyDown && hasStructureSelectionHit(selectionRect)) {
            shouldClearSelection = false;
        }

        if (shouldClearSelection) {
            deselectAllMapObjects();
        }

        for (int i = objectSelectionCount - 1; i >= 0; i--) {
            CRect objectRect = objectSelectionRects.get(i);
            CRect intersection = new CRect(objectRect);
            intersection.intersect(selectionRect);
            boolean matched = tinySelection
                    ? !intersection.isEmpty()
                    : intersection.width() * intersection.height() > SELECTION_RECT_MIN_AREA;
            if (!matched) {
                continue;
            }

            CGameObject object = objects.get((short) (objectSelectionIds.get(i) & 0xFFFF));
            if (object != null) {
                if (!Globals.shiftKeyDown) {
                    if (tinySelection) {
                        if (!(object instanceof CStructure)) {
                            object.setSelected(true);
                        }
                    } else if (object.cPlayer == currentPlayer && !(object instanceof CStructure)) {
                        object.setSelected(true);
                    }
                } else if (object.cPlayer == currentPlayer && (selectionFlags & FLAG_BUSY) == 0) {
                    object.setSelected(!object.isSelected());
                }
            }

            if (tinySelection) {
                break;
            }
        }

        updateSelectionState();
        if (Globals.altKeyDown && selectedCount == 1 && primarySelectedObject != null) {
            int group = primarySelectedObject.getFirstControlGroup();
            if (group >= 0) {
                selectGroup(group);
            }
        }

        clearInputModeAfterMapClick();
        if (!Globals.shiftKeyDown && selectedCount != 0 && (selectionFlags & (FLAG_UNIT | FLAG_BUSY)) == FLAG_UNIT) {
            CUnit voiceUnit = selectAcknowledgementVoiceUnit();
            if (voiceUnit != null) {
                voiceUnit.playSelectVoice();
            }
        }
    }

    /**
     * Native support extracted from MapVisualObject::ApplySelectionRectangle @00417986.
     * Java-only zoom addition: converts the native physical-pixel threshold into logical map-frame pixels.
     */
    private int selectionTinyThreshold() {
        int nativePhysicalThreshold = (Globals.screenRect.right * 10) / SELECTION_RECT_REFERENCE_WIDTH;
        CRect mapScreenRect = new CRect();
        clientToScreen(mapScreenRect, cRect);
        CRect scaledMapRect = javaZoomScaledMapRect(mapScreenRect);
        return Math.max(1, javaZoomScalePhysicalToLogical(
                nativePhysicalThreshold,
                javaZoomLogicalMapFrameWidth(),
                scaledMapRect.width()
        ));
    }

    /**
     * Native support extracted from MapVisualObject::ApplySelectionRectangle @00417986 non-tiny preflight.
     */
    private boolean hasOwnedUnitSelectionHit(CRect selectionRect) {
        for (int i = objectSelectionCount - 1; i >= 0; i--) {
            CRect intersection = new CRect(objectSelectionRects.get(i));
            intersection.intersect(selectionRect);
            if (intersection.width() * intersection.height() <= SELECTION_RECT_MIN_AREA) {
                continue;
            }
            CGameObject object = objects.get((short) (objectSelectionIds.get(i) & 0xFFFF));
            if (object != null && object.cPlayer == currentPlayer && !(object instanceof CStructure)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Native support extracted from MapVisualObject::ApplySelectionRectangle @00417986 tiny structure-hit preflight.
     */
    private boolean hasStructureSelectionHit(CRect selectionRect) {
        for (int i = objectSelectionCount - 1; i >= 0; i--) {
            CRect intersection = new CRect(objectSelectionRects.get(i));
            intersection.intersect(selectionRect);
            if (intersection.isEmpty()) {
                continue;
            }
            CGameObject object = objects.get((short) (objectSelectionIds.get(i) & 0xFFFF));
            if (object instanceof CStructure) {
                return true;
            }
        }
        return false;
    }

    /**
     * Native support extracted from MapVisualObject::ApplySelectionRectangle @00417986 and UpdateMouseCursor @004175C7.
     */
    private void clearInputModeAfterMapClick() {
        if (inputMode == 0) {
            return;
        }
        inputMode = 0;
        Globals.mainWindow.pSpellPanelVisualObject.clearSelectedSpellSlot();
        resetOrderToolbarSelection();
    }

    /**
     * Native: MapVisualObject::IssuePickupOrder @00419D8E.
     * Fully ported.
     */
    private void issuePickupOrder(int targetX, int targetY) {
        PickupOrderAction action = PickupOrderAction.global;
        action.ID.set(PickupOrderAction.ACTION_ID);
        action.netID.set(currentPlayer.playerId);
        action.playerID.set(0);
        action.targetCellX.set(targetX & 0xFFFF);
        action.targetCellY.set(targetY & 0xFFFF);
        action.entryCount.set(0);
        action.unitTokenIds.set(new byte[0]);
        for (CGameObject object : objects.values()) {
            if (object.isSelected()) {
                action.inventoryInsertIndex.set(object.shopInventoryVisibleStart[0] & 0xFFFF);
                action.addUnitToken(object.m_id);
            }
        }
        CServerApp.sendClientGameAction(action);

        CUnit voiceUnit = selectAcknowledgementVoiceUnit();
        if (voiceUnit != null) {
            voiceUnit.playPickupVoice();
        }
    }

    /**
     * Native: MapVisualObject::IssueEnterTownOrder @0041A0B5.
     * Fully ported.
     */
    private void issueEnterTownOrder(short targetTokenId) {
        EnterTownOrderAction action = EnterTownOrderAction.global;
        action.ID.set(EnterTownOrderAction.ACTION_ID);
        action.netID.set(currentPlayer.playerId);
        action.playerID.set(0);
        action.targetTokenId.set(Short.toUnsignedInt(targetTokenId));
        addSelectedUnitTokens(action);
        CServerApp.sendClientGameAction(action);

        CUnit voiceUnit = selectAcknowledgementVoiceUnit();
        if (voiceUnit != null) {
            voiceUnit.playMoveVoice();
        }
    }

    /**
     * Native: MapVisualObject::RefreshAmbientAudio @0041B311.
     * Fully ported. SoundSystem remains the Java playback backend while mapping native aggregate attenuation to
     * positional source gain.
     */
    private void refreshAmbientAudio() {
        int tick = Globals.currentTickMillis();
        boolean viewChanged = view.x != ambientAudioViewX || view.y != ambientAudioViewY;
        boolean objectSoundDue = Integer.compareUnsigned(nextAmbientObjectSoundTick, tick) < 0;
        if (!viewChanged && !objectSoundDue) {
            return;
        }
        if (viewChanged) {
            ambientAudioViewX = view.x;
            ambientAudioViewY = view.y;
        }

        Sound riverSound = SoundManager.SFX_SOUNDS.get(AMBIENT_RIVER.id);
        Sound magicFirewallSound = SoundManager.SFX_SOUNDS.get(MAGIC_FIREWALL.id);
        SoundSystem soundSystem = SoundSystem.get();
        updateSoundSystemMapAudioView(soundSystem);
        SoundChannel riverChannel = soundSystem.getPlayingChannel(riverSound);
        SoundChannel magicFirewallChannel = soundSystem.getPlayingChannel(magicFirewallSound);

        SoundSystem.MapSoundAccumulator riverSources = soundSystem.newMapSoundAccumulator();
        SoundSystem.MapSoundAccumulator magicFirewallSources = soundSystem.newMapSoundAccumulator();
        SoundSystem.MapSoundAccumulator objectSources = soundSystem.newMapSoundAccumulator();
        int riverCount = 0;
        int magicFirewallCount = 0;
        int normalObjectCount = 0;
        int simpleObjectCount = 0;
        int fireObjectCount = 0;

        int left = Math.max(view.x - gridWidth, AMBIENT_SCAN_EDGE_MARGIN);
        int right = Math.min(view.x + gridWidth * 2, cachedMapWidth - AMBIENT_SCAN_EDGE_MARGIN);
        int top = Math.max(view.y - gridHeight, AMBIENT_SCAN_EDGE_MARGIN);
        int bottom = Math.min(view.y + gridHeight * 2, cachedMapHeight - AMBIENT_SCAN_EDGE_MARGIN);

        for (int tileY = top; tileY <= bottom; tileY++) {
            int flatIndex = left + tileY * cachedMapWidth;
            for (int tileX = left; tileX <= right; tileX++, flatIndex++) {
                int tileFamily = (mapDescriptor.tileWordFlatAt(flatIndex) & TERRAIN_TILE_INDEX_MASK)
                        >> TERRAIN_TILE_FAMILY_SHIFT;
                if (tileFamily > 7 && tileFamily < 0x0C) {
                    riverSources.addTile(tileX, tileY);
                    riverCount++;
                }

                Integer flags = terrainLightOverrideCells.get(packedTileKey(tileX, tileY));
                if (flags != null && (flags & TERRAIN_LIGHT_FLICKER_FLAG) != 0) {
                    magicFirewallSources.addTile(tileX, tileY);
                    magicFirewallCount++;
                }

                if (objectSoundDue && mapDescriptor.objectIdFlatAt(flatIndex) != 0) {
                    VObject visualObject = VObjects.getVObject(mapDescriptor.objectIdFlatAt(flatIndex) - 1);
                    if (visualObject.deadObjectId < 0) {
                        if (visualObject.fireObjectId == -2) {
                            fireObjectCount++;
                            objectSources.addTile(tileX, tileY);
                        }
                    } else {
                        if (visualObject.phases < 2) {
                            simpleObjectCount++;
                        } else {
                            normalObjectCount++;
                        }
                        objectSources.addTile(tileX, tileY);
                    }
                }
            }
        }

        refreshLoopingAmbientSound(soundSystem, riverSound, riverChannel, riverCount, riverSources);
        refreshLoopingAmbientSound(
                soundSystem,
                magicFirewallSound,
                magicFirewallChannel,
                magicFirewallCount,
                magicFirewallSources
        );

        int objectSoundCount = normalObjectCount + simpleObjectCount + fireObjectCount;
        if (objectSoundCount != 0) {
            playRandomAmbientObjectSound(
                    soundSystem,
                    normalObjectCount,
                    simpleObjectCount,
                    fireObjectCount,
                    objectSources
            );
            nextAmbientObjectSoundTick = Globals.currentTickMillis()
                    + AMBIENT_OBJECT_SOUND_DELAY_BASE_MS
                    + Utils.randInclusive(0x7FFF) / 7;
        }
    }

    /**
     * Native support boundary for MapVisualObject::ComputeMapRelativeSoundPosition @0041B1F8 and Java-only map zoom.
     * not ported.
     */
    public void updateSoundSystemMapAudioView(SoundSystem soundSystem) {
        soundSystem.updateMapAudioView(view.x, view.y, gridWidth, gridHeight);
    }

    /**
     * Native support extracted from MapVisualObject::RefreshAmbientAudio @0041B311.
     */
    private void refreshLoopingAmbientSound(
            SoundSystem soundSystem,
            Sound sound,
            SoundChannel channel,
            int activeCellCount,
            SoundSystem.MapSoundAccumulator source
    ) {
        if (channel == null) {
            if (activeCellCount != 0) {
                soundSystem.playMapAggregateLoopSound(
                        sound,
                        Globals.soundPreferences.sfxVolume,
                        source,
                        (byte) AMBIENT_SOUND_PRIORITY,
                        0
                );
            }
        } else if (activeCellCount == 0) {
            channel.stopAndRewind();
        } else {
            soundSystem.updateMapAggregateLoopChannel(
                    channel,
                    Globals.soundPreferences.sfxVolume,
                    source
            );
        }
    }

    /**
     * Native support extracted from MapVisualObject::RefreshAmbientAudio @0041B311.
     */
    private void playRandomAmbientObjectSound(
            SoundSystem soundSystem,
            int normalObjectCount,
            int simpleObjectCount,
            int fireObjectCount,
            SoundSystem.MapSoundAccumulator source
    ) {
        int selectedIndex = Utils.randInclusive(normalObjectCount + simpleObjectCount + fireObjectCount - 1);
        int soundIdBase = 600;
        if (normalObjectCount <= selectedIndex) {
            soundIdBase = 0x26C;
        }
        if (normalObjectCount + simpleObjectCount <= selectedIndex) {
            soundIdBase += 0x14;
        }
        int hour = ((((Globals.mainWindow.serverLoopCounter >>> 4) + 0x168) / 0x3C) % 0x18);
        if (hour < 4 || hour > 0x13) {
            soundIdBase += 10;
        }

        Sound sound;
        do {
            sound = SoundManager.SFX_SOUNDS.get(soundIdBase + Utils.randExclusive(0, 10));
        } while (sound == null);
        soundSystem.playMapAmbientObjectSound(
                sound,
                Globals.soundPreferences.sfxVolume,
                source,
                (byte) AMBIENT_SOUND_PRIORITY,
                0
        );
    }

    /**
     * Java helper for the recovered panel-message fan-out in MapVisualObject::SetCameraPosition @004162C2 and
     * MapVisualObject::RemoveSpellPanel @0041B077.
     */
    private void notifyPanelContextChanged() {
        Globals.mainWindow.pMinimapVisualObject.onMessage(NOTIFY_MAP_CONTEXT_CHANGED, 0, 0);
        Globals.mainWindow.pSelectionInfoPanelVisualObject.onMessage(NOTIFY_MAP_CONTEXT_CHANGED, 0, 0);
        Globals.mainWindow.pSideStatusVisualObject.onMessage(NOTIFY_MAP_CONTEXT_CHANGED, 0, 0);
    }

    /**
     * Native support extracted from MapVisualObject::SetCameraPosition @004162C2.
     */
    private void notifyMinimapContextChanged() {
        Globals.mainWindow.pMinimapVisualObject.onMessage(NOTIFY_MAP_CONTEXT_CHANGED, 0, 0);
    }

    /**
     * Java helper for the recovered order-toolbar reset in MapVisualObject::RefreshLayoutAfterAction @00416388 and
     * MapVisualObject::ExecuteOrderType @00418A02.
     */
    private void resetOrderToolbarSelection() {
        Globals.mainWindow.pOrderToolbarVisualObject.onMessage(RESET_ORDER_SELECTION, 0, 0);
    }

    /**
     * Native support extracted from MapVisualObject::RefreshLayoutAfterAction @00416388.
     */
    private void deselectAllMapObjects() {
        for (CGameObject object : objects.values()) {
            object.setSelected(false);
        }
    }

    /**
     * Native: MapVisualObject::RecalculateGridMetrics @00402FB8.
     * Fully ported.
     * Java-only zoom addition: gridWidth/gridHeight now describe the current logical map cells rendered to the zoom
     * framebuffer, while allocatedGridWidth/allocatedGridHeight describe the max-zoom grid used to size backing arrays.
     * This deliberate split is not native behavior; it prevents render-grid reallocations when the Java zoom changes.
     */
    private void recalculateGridMetrics() {
        applyJavaZoomGridMetrics();
        if (mapDescriptor != null) {
            clampViewToMapBounds();
        }
        allocateRenderGrids();
        renderFrameDirty = 1;
        pendingCameraDeltaX = 0;
        pendingCameraDeltaY = 0;
        gameListControl.configureMessageRect(cRect);
    }

    /**
     * Java-only zoom support for MapVisualObject::RecalculateGridMetrics @00402FB8. This is not native behavior: the
     * native game used the physical screen rectangle directly, but Java derives a logical visible grid from zoom and a
     * separate max-zoom allocation grid so changing zoom does not invalidate native-style flat render-grid reads.
     * not ported.
     */
    private void applyJavaZoomGridMetrics() {
        int currentZoom = clampJavaMapZoom(javaMapZoom);
        javaMapZoom = currentZoom;
        int viewportWidth = cRect.width();
        int viewportHeight = cRect.height();
        if (viewportWidth <= 0 || viewportHeight <= 0) {
            gridWidth = 0;
            gridHeight = 0;
            allocatedGridWidth = 0;
            allocatedGridHeight = 0;
            return;
        }

        GridMetrics current = javaZoomGridMetrics(viewportWidth, viewportHeight, currentZoom);
        GridMetrics maximum = javaZoomGridMetrics(viewportWidth, viewportHeight, JAVA_MAP_ZOOM_MAX);
        gridWidth = current.columns();
        gridHeight = current.rows();
        allocatedGridWidth = Math.max(gridWidth, maximum.columns());
        allocatedGridHeight = Math.max(gridHeight, maximum.rows());
    }

    /**
     * Java-only zoom support. Not native behavior: this hard-coded policy maps zoom values to visible row counts and
     * derives columns from the physical viewport aspect ratio so zoom preserves aspect instead of changing X/Y
     * independently.
     * not ported.
     */
    private GridMetrics javaZoomGridMetrics(int viewportWidth, int viewportHeight, int zoom) {
        int rows = JAVA_MAP_ZOOM_MIN_VISIBLE_ROWS + zoom;
        if (cachedMapHeight > 0) {
            rows = Math.min(rows, Math.max(1, cachedMapHeight - MAP_CAMERA_EDGE_TILES * 2));
        }
        int columns = Math.max(1, (int) Math.round((double) rows * viewportWidth / viewportHeight));
        if (cachedMapWidth > 0) {
            columns = Math.min(columns, Math.max(1, cachedMapWidth - MAP_CAMERA_EDGE_TILES * 2));
        }
        return new GridMetrics(columns, rows);
    }

    /**
     * Java-only zoom support. Not native behavior; keeps hard-coded zoom state inside the MapVisualObject addition.
     * not ported.
     */
    private static int clampJavaMapZoom(int zoom) {
        return Math.max(JAVA_MAP_ZOOM_MIN, Math.min(JAVA_MAP_ZOOM_MAX, zoom));
    }

    /**
     * Java-only zoom support. Not native behavior; applies a new zoom and rebuilds the logical/current grid while
     * retaining max-zoom render-grid allocation sizing.
     * not ported.
     */
    private void setJavaMapZoom(int zoom) {
        int clampedZoom = clampJavaMapZoom(zoom);
        if (clampedZoom == javaMapZoom) {
            return;
        }
        int centerX = view.x + gridWidth / 2;
        int centerY = view.y + gridHeight / 2;
        javaMapZoom = clampedZoom;
        recalculateGridMetrics();
        view.x = centerX - gridWidth / 2;
        view.y = centerY - gridHeight / 2;
        if (mapDescriptor != null) {
            clampViewToMapBounds();
        }
        renderFrameDirty = 1;
        updateSoundSystemMapAudioView(SoundSystem.get());
    }

    /**
     * Java-only zoom support. Not native behavior; ensures the logical 32-pixel-cell framebuffer matches the current
     * visible grid before the native drawing sequence is scaled into the actual screen rectangle.
     * not ported.
     */
    private void ensureJavaZoomMapFrameBuffer() {
        int width = javaZoomLogicalMapFrameWidth();
        int height = javaZoomLogicalMapFrameHeight();
        int length = Math.multiplyExact(Math.multiplyExact(width, height), 4);
        if (javaZoomMapFrameBgra.length != length) {
            javaZoomMapFrameBgra = new byte[length];
        }
        javaZoomMapFrameWidth = width;
        javaZoomMapFrameHeight = height;
    }

    /**
     * Java-only zoom support. Not native behavior; current logical map framebuffer width in native 32-pixel cells.
     * not ported.
     */
    private int javaZoomLogicalMapFrameWidth() {
        return Math.max(1, Math.multiplyExact(gridWidth, TILE_SCREEN_SIZE));
    }

    /**
     * Java-only zoom support. Not native behavior; current logical map framebuffer height in native 32-pixel cells.
     * not ported.
     */
    private int javaZoomLogicalMapFrameHeight() {
        return Math.max(1, Math.multiplyExact(gridHeight, TILE_SCREEN_SIZE));
    }

    /**
     * Java-only zoom support. Not native behavior; restricts mouse-wheel zoom to the physical map viewport while leaving
     * child overlays free to own their own wheel behavior.
     * not ported.
     */
    private boolean isJavaZoomViewportPoint(int x, int y) {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        if (!screenRect.contains(x, y)) {
            return false;
        }
        return !isPointInsideChild(2, x, y) && !isPointInsideChild(3, x, y);
    }

    /**
     * Java-only zoom support. Not native behavior; hit-tests child overlays in screen coordinates for mouse-wheel
     * routing without changing the native mouse-message router.
     * not ported.
     */
    private boolean isPointInsideChild(int childId, int x, int y) {
        CVisualObject child = getChildById(childId);
        if (child == null || child.checkStateFlag(0x20) != 0) {
            return false;
        }

        CRect childScreenRect = new CRect();
        child.clientToScreen(childScreenRect, child.cRect);
        return childScreenRect.contains(x, y);
    }

    /**
     * Java-only zoom support. Not native behavior; computes a top-left-anchored cover rectangle. The map viewport must
     * start at its physical 0:0 origin, so this deliberately crops any overrun on the right/bottom instead of centering
     * with black letterbox stripes on the left/top.
     * not ported.
     */
    private CRect javaZoomScaledMapRect(CRect screenRect) {
        int logicalWidth = javaZoomLogicalMapFrameWidth();
        int logicalHeight = javaZoomLogicalMapFrameHeight();
        double scaleX = (double) screenRect.width() / logicalWidth;
        double scaleY = (double) screenRect.height() / logicalHeight;
        double scale = Math.max(scaleX, scaleY);
        int width = Math.max(1, (int) Math.ceil(logicalWidth * scale));
        int height = Math.max(1, (int) Math.ceil(logicalHeight * scale));
        return new CRect(screenRect.left, screenRect.top, screenRect.left + width, screenRect.top + height);
    }

    /**
     * Java-only zoom support. Not native behavior; maps physical viewport mouse coordinates back to the native-style
     * logical 32-pixel-cell map framebuffer used by cursor and selection logic.
     * not ported.
     */
    private Point javaZoomScreenToLogicalMapPoint(CRect mapScreenRect, int screenX, int screenY) {
        CRect scaledMapRect = javaZoomScaledMapRect(mapScreenRect);
        return new Point(
                javaZoomScalePhysicalToLogical(
                        screenX - scaledMapRect.left,
                        javaZoomLogicalMapFrameWidth(),
                        scaledMapRect.width()
                ),
                javaZoomScalePhysicalToLogical(
                        screenY - scaledMapRect.top,
                        javaZoomLogicalMapFrameHeight(),
                        scaledMapRect.height()
                )
        );
    }

    /**
     * Java-only zoom support. Not native behavior; converts a physical screen selection rectangle into logical map-frame
     * coordinates before comparing it with native-style object selection rectangles.
     * not ported.
     */
    private CRect javaZoomScreenRectToLogicalMapRect(CRect mapScreenRect, CRect screenRect) {
        Point topLeft = javaZoomScreenToLogicalMapPoint(mapScreenRect, screenRect.left, screenRect.top);
        Point bottomRight = javaZoomScreenToLogicalMapPoint(
                mapScreenRect,
                Math.max(screenRect.left, screenRect.right - 1),
                Math.max(screenRect.top, screenRect.bottom - 1)
        );
        return new CRect(topLeft.x, topLeft.y, bottomRight.x + 1, bottomRight.y + 1).normalize();
    }

    /**
     * Java-only zoom support. Not native behavior; inverse-scales one physical viewport offset into logical map pixels.
     * not ported.
     */
    private static int javaZoomScalePhysicalToLogical(int physicalOffset, int logicalSize, int scaledSize) {
        int logicalOffset = (int) ((long) Math.max(0, physicalOffset) * logicalSize / Math.max(1, scaledSize));
        return Math.max(0, Math.min(logicalSize - 1, logicalOffset));
    }

    /**
     * Java-only zoom support. Not native behavior; clears the offscreen map target to opaque black so any logical
     * edge pixels not touched by native terrain drawing remain presentation-safe when scaled back to the screen.
     * not ported.
     */
    private void clearJavaZoomMapFrameBuffer() {
        Arrays.fill(javaZoomMapFrameBgra, (byte) 0);
        for (int alphaOffset = 3; alphaOffset < javaZoomMapFrameBgra.length; alphaOffset += 4) {
            javaZoomMapFrameBgra[alphaOffset] = (byte) 0xFF;
        }
    }

    /**
     * Java-only zoom support tuple. Not native behavior.
     * not ported.
     */
    private record GridMetrics(int columns, int rows) {
    }

    /**
     * Native support extracted from MapVisualObject::RecalculateGridMetrics @00402FB8.
     * Java-only zoom addition: arrays are allocated for max zoom, while render loops use gridWidth/gridHeight for the
     * current zoom. The extra capacity is intentional and should not be reduced to current visible size.
     */
    private void allocateRenderGrids() {
        int objectLayerColumns = objectLayerStride();
        int objectLayerRows = allocatedGridHeight + OBJECT_LAYER_EXTRA_ROWS;
        int terrainVertexColumns = terrainVertexStride();
        int terrainVertexRows = allocatedGridHeight + TERRAIN_VERTEX_EXTRA_ROWS;
        int tileAverageColumns = tileAverageStride();
        int tileAverageRows = allocatedGridHeight + TILE_AVERAGE_EXTRA_ROWS;
        int objectLayerCells = objectLayerColumns * objectLayerRows;
        int terrainVertexCells = terrainVertexColumns * terrainVertexRows;
        int tileAverageCells = tileAverageColumns * tileAverageRows;
        objectLayerGridBytes = objectLayerCells * Integer.BYTES;
        terrainVertexGridBytes = terrainVertexCells * Integer.BYTES;

        groundObjectLayer = new CGameObject[objectLayerColumns][objectLayerRows];
        airObjectLayer = new CGameObject[objectLayerColumns][objectLayerRows];
        structureObjectLayer = new CGameObject[objectLayerColumns][objectLayerRows];
        miscObjectLayer = new CGameObject[objectLayerColumns][objectLayerRows];
        inactiveUnitLayer = new CGameObject[objectLayerColumns][objectLayerRows];
        tileBrightnessGrid = new byte[objectLayerColumns][objectLayerRows];
        screenYRowTopGrid = new int[objectLayerColumns][objectLayerRows];
        screenYRowBottomGrid = new int[objectLayerColumns][objectLayerRows];

        tileSlopeModeGrid = new int[terrainVertexColumns][terrainVertexRows];
        previousTileSlopeModeGrid = new int[terrainVertexColumns][terrainVertexRows];
        dynamicLightOverrideGrid = new byte[terrainVertexColumns][terrainVertexRows];
        previousDynamicLightOverrideGrid = new byte[terrainVertexColumns][terrainVertexRows];
        screenYVertexGrid = new int[terrainVertexColumns][terrainVertexRows];
        tileAverageHeightGrid = new int[tileAverageColumns][tileAverageRows];
    }

    /**
     * Native: MapVisualObject::GetX @0041EFE0.
     * Fully ported. Native returns the fixed map camera-edge tile margin.
     */
    public int getX() {
        return MAP_CAMERA_EDGE_TILES;
    }

    /**
     * Native: MapVisualObject::GetY @0041EFF0.
     * Fully ported. Native returns the fixed map camera-edge tile margin.
     */
    public int getY() {
        return MAP_CAMERA_EDGE_TILES;
    }

    /**
     * Native: MapVisualObject::MaxCameraX @0041F000.
     * Fully ported.
     */
    private int maxCameraX() {
        return (cachedMapWidth - MAP_CAMERA_EDGE_TILES) - gridWidth;
    }

    /**
     * Native: MapVisualObject::MaxCameraY @0041F020.
     * Fully ported.
     */
    private int maxCameraY() {
        return (cachedMapHeight - MAP_CAMERA_EDGE_TILES) - gridHeight;
    }

    /**
     * Native: MapVisualObject::AvailableOrderMask @0041757B.
     * Fully ported.
     */
    private int availableOrderMask() {
        if ((selectionFlags & FLAG_BUSY) != 0) {
            return 0;
        }
        if (selectedAvailableSpellMask != 0 || activeSpellEffectMask != 0) {
            return 0xFF;
        }
        return 0xEF;
    }

    /**
     * Native: MapVisualObject::SelectGroup @0041644E.
     * Fully ported.
     */
    private void selectGroup(int group) {
        for (CGameObject object : objects.values()) {
            if (object.belongsToGroup(group) == 0) {
                if (!Globals.shiftKeyDown) {
                    object.setSelected(false);
                }
            } else {
                object.setSelected(true);
            }
        }
        updateSelectionState();
    }

    /**
     * Native: MapVisualObject::SetGroup @004164CD.
     * Fully ported.
     */
    private void setGroup(int group) {
        if ((selectionFlags & FLAG_BUSY) != 0) {
            return;
        }

        for (CGameObject object : objects.values()) {
            if (object.belongsToGroup(group) != 0) {
                object.removeFromGroup(group);
            }
            if (object.isSelected() && object.cPlayer == currentPlayer) {
                object.setGroup(group);
            }
        }
        updateSelectionState();
    }

    /**
     * Native: MapVisualObject::AddToGroup @00416571.
     * Fully ported.
     */
    private void addToGroup(int group) {
        if ((selectionFlags & FLAG_BUSY) != 0) {
            return;
        }

        for (CGameObject object : objects.values()) {
            if (object.belongsToGroup(group) != 0) {
                object.removeFromGroup(group);
            }
            if (object.isSelected() && object.cPlayer == currentPlayer) {
                object.addToGroup(group);
            }
        }
        updateSelectionState();
    }

    /**
     * Native: MapVisualObject::JumpToGroup @00416615.
     * Fully ported.
     */
    private void jumpToGroup(int group) {
        int x = 0;
        int y = 0;
        int total = 0;
        for (CGameObject object : objects.values()) {
            if (object.belongsToGroup(group) == 0) {
                object.setSelected(false);
            } else {
                object.setSelected(true);
                total++;
                x += object.location.x >> Byte.SIZE;
                y += object.location.y >> Byte.SIZE;
            }
        }
        updateSelectionState();
        if (total > 0) {
            onMessage(SET_CAMERA_POS, x / total - gridWidth / 2, y / total - gridHeight / 2);
        }
    }

    /**
     * Native owner: field read at MapVisualObject +0xA88 in MinimapVisualObject::Update @004AC414.
     * not ported.
     */
    public int getMinimapCameraMarker() {
        return view.x;
    }

    /**
     * Native support: relation-row bit-8 probe used by @00460AE0 / @00460B76 / @00468762 and multiple CUnit draw paths.
     * not ported.
     */
    public boolean isOwnerVisible(int ownerPlayerId) {
        return ownerPlayerId == 0
                || currentPlayer == null
                || currentPlayer.isMapVisible(ownerPlayerId);
    }

    /**
     * Native support: MapDescriptor::GetTilesWxH consumers in CGameObject @00460B76 / @004609CA.
     * not ported.
     */
    public short[] getOccupancyTileFlags() {
        if (mapDescriptor != null && mapDescriptor.tilesWxH.length != 0) {
            return mapDescriptor.tilesWxH;
        }
        return null;
    }

    /**
     * Native support: row-stride loads from MapVisualObject +0x80 in @00403CA0 / @00460B76 / @004609CA.
     * not ported.
     */
    public int getOccupancyMapWidth() {
        if (cachedMapWidth != 0) {
            return cachedMapWidth;
        }
        return Globals.worldMap == null ? 0 : Globals.worldMap.getMapWidth();
    }

    /**
     * Native support: `field38_0xD8 = 1` in CGameObject::OccupyMapCells @00460B76.
     * not ported.
     */
    public void markMapOccupancyDirty() {
        mapOccupancyDirty = 1;
    }

    /**
     * Native support extracted from MinimapVisualObject::Update @004AC414.
     */
    public void drawMinimapObjects(int minimapLeft, int minimapTop, int zoomLevel) {
        for (CGameObject object : objects.values()) {
            object.drawMinimap(minimapLeft, minimapTop, zoomLevel);
        }
    }

    /**
     * Native: MapVisualObject::BuildVisibleSectorCostGrid @00403CA0.
     * Fully ported. Java returns the native scratch grid instead of writing the embedded native array.
     */
    public int[][] buildVisibleSectorCostGrid(CGameObject gameObject) {
        int[][] visibleSectorCostGrid = new int[OCCUPANCY_GRID_SIZE][OCCUPANCY_GRID_SIZE];
        int startTileX = gameObject.tileX - OCCUPANCY_GRID_CENTER;
        int startTileY = gameObject.tileY - OCCUPANCY_GRID_CENTER;
        int visibilityShift = visibilityScaleShift;
        visibleSectorCostGrid[OCCUPANCY_GRID_CENTER][OCCUPANCY_GRID_CENTER] =
                (Short.toUnsignedInt(gameObject.packedSightRange) >> (8 - visibilityShift))
                        + (1 << ((visibilityShift - 1) & 0x1F));
        int centerHeight = mapDescriptor.heightAt(gameObject.tileX, gameObject.tileY);
        for (int radius = 1; radius < OCCUPANCY_GRID_CENTER; radius++) {
            boolean finished = true;
            for (int offset = -radius; offset <= radius; offset++) {
                if (mapDescriptor.isValidVisibilityTile(startTileX + offset + OCCUPANCY_GRID_CENTER, startTileY + OCCUPANCY_GRID_CENTER - radius)
                        && !updateVisibleSectorCostCell(
                        visibleSectorCostGrid,
                        offset + OCCUPANCY_GRID_CENTER,
                        OCCUPANCY_GRID_CENTER - radius,
                        startTileX,
                        startTileY,
                        centerHeight
                )) {
                    finished = false;
                }
                if (mapDescriptor.isValidVisibilityTile(startTileX + offset + OCCUPANCY_GRID_CENTER, startTileY + radius + OCCUPANCY_GRID_CENTER)
                        && !updateVisibleSectorCostCell(
                        visibleSectorCostGrid,
                        offset + OCCUPANCY_GRID_CENTER,
                        radius + OCCUPANCY_GRID_CENTER,
                        startTileX,
                        startTileY,
                        centerHeight
                )) {
                    finished = false;
                }
                if (mapDescriptor.isValidVisibilityTile(startTileX + OCCUPANCY_GRID_CENTER - radius, startTileY + offset + OCCUPANCY_GRID_CENTER)
                        && !updateVisibleSectorCostCell(
                        visibleSectorCostGrid,
                        OCCUPANCY_GRID_CENTER - radius,
                        offset + OCCUPANCY_GRID_CENTER,
                        startTileX,
                        startTileY,
                        centerHeight
                )) {
                    finished = false;
                }
                if (mapDescriptor.isValidVisibilityTile(startTileX + radius + OCCUPANCY_GRID_CENTER, startTileY + OCCUPANCY_GRID_CENTER - offset)
                        && !updateVisibleSectorCostCell(
                        visibleSectorCostGrid,
                        radius + OCCUPANCY_GRID_CENTER,
                        OCCUPANCY_GRID_CENTER - offset,
                        startTileX,
                        startTileY,
                        centerHeight
                )) {
                    finished = false;
                }
            }
            if (finished) {
                break;
            }
        }
        return visibleSectorCostGrid;
    }

    /**
     * Native support extracted from MinimapVisualObject::Update @004AC414 cached bitmap branch.
     */
    public boolean useAreaEffectMinimapBitmap() {
        return mapOccupancyDirty == 0;
    }

    /**
     * Native: MapVisualObject::FindObjectTokenAtCell @0040C046.
     * Fully ported.
     */
    public short findObjectTokenAtCell(int tileX, int tileY) {
        for (Map.Entry<Short, CGameObject> entry : objects.entrySet()) {
            CGameObject object = entry.getValue();
            if (object.bIsBlocked == 0 && object.tileX == tileX && object.tileY == tileY) {
                return entry.getKey();
            }
        }
        return 0;
    }

    /**
     * Native: MapVisualObject::IssueMinimapMoveOrder @00418BC3.
     * Fully ported.
     */
    public void issueMinimapMoveOrder(int tileX) {
        MoveOrderAction action = MoveOrderAction.global;
        action.ID.set(MoveOrderAction.ACTION_ID);
        action.netID.set(currentPlayer.playerId);
        action.playerID.set(0);
        action.destinationCellX.set(tileX & 0xFFFF);
        action.destinationCellY.set(currentMouseMoveOrderTileY() & 0xFFFF);
        addSelectedUnitTokens(action);
        CServerApp.sendClientGameAction(action);

        CUnit voiceUnit = selectAcknowledgementVoiceUnit();
        if (voiceUnit != null && voiceUnit.lastAction != 1) {
            voiceUnit.playMoveVoice();
        }
    }

    /**
     * Native support extracted from MapVisualObject::IssueMinimapMoveOrder @00418BC3 current-mouse `GetTileY` call.
     * Java maps through the zoomed logical map framebuffer before applying native tile-Y math.
     */
    private int currentMouseMoveOrderTileY() {
        CRect mapScreenRect = new CRect();
        clientToScreen(mapScreenRect, cRect);
        Point logicalPoint = javaZoomScreenToLogicalMapPoint(
                mapScreenRect,
                Globals.mousePointer.getX(),
                Globals.mousePointer.getY()
        );
        return getTileYForScreenPoint(logicalPoint.x, logicalPoint.y) + view.y;
    }

    /**
     * Native: MapVisualObject::IssueMinimapAttackCellOrder @00418D78.
     * Fully ported.
     */
    public void issueMinimapAttackCellOrder(int tileX, int tileY) {
        AttackCellOrderAction action = AttackCellOrderAction.global;
        action.ID.set(AttackCellOrderAction.ACTION_ID);
        action.netID.set(currentPlayer.playerId);
        action.playerID.set(0);
        action.targetCellX.set(tileX & 0xFFFF);
        action.targetCellY.set(tileY & 0xFFFF);
        addSelectedUnitTokens(action);
        CServerApp.sendClientGameAction(action);

        CUnit voiceUnit = selectAcknowledgementVoiceUnit();
        if (voiceUnit != null && voiceUnit.lastAction != 1) {
            voiceUnit.playSwarmVoice();
        }
    }

    /**
     * Native: MapVisualObject::IssueMinimapAttackTargetOrder @00418CB3.
     * Fully ported.
     */
    public void issueMinimapAttackTargetOrder(short targetTokenId) {
        AttackTargetOrderAction action = AttackTargetOrderAction.global;
        action.ID.set(AttackTargetOrderAction.ACTION_ID);
        action.netID.set(currentPlayer.playerId);
        action.playerID.set(0);
        action.targetTokenId.set(Short.toUnsignedInt(targetTokenId));
        addSelectedUnitTokens(action);
        CServerApp.sendClientGameAction(action);

        CUnit voiceUnit = selectAcknowledgementVoiceUnit();
        if (voiceUnit != null) {
            voiceUnit.playAttackVoice();
        }
    }

    /**
     * Native: MapVisualObject::IssueMinimapDefendTargetOrder @00418FC1.
     * Fully ported.
     */
    public void issueMinimapDefendTargetOrder(short targetTokenId) {
        CGameObject target = objects.get(targetTokenId);
        if (target instanceof CUnit) {
            DefendTargetOrderAction action = DefendTargetOrderAction.global;
            action.ID.set(DefendTargetOrderAction.ACTION_ID);
            action.netID.set(currentPlayer.playerId);
            action.playerID.set(0);
            action.targetTokenId.set(Short.toUnsignedInt(targetTokenId));
            addSelectedUnitTokens(action);
            CServerApp.sendClientGameAction(action);

            CUnit voiceUnit = selectAcknowledgementVoiceUnit();
            if (voiceUnit != null) {
                voiceUnit.playDefendVoice();
            }
        }
    }

    /**
     * Native: MapVisualObject::IssueMinimapPatrolOrder @004190BE.
     * Fully ported.
     */
    public void issueMinimapPatrolOrder(int tileX, int tileY) {
        PatrolOrderAction action = PatrolOrderAction.global;
        action.ID.set(PatrolOrderAction.ACTION_ID);
        action.netID.set(currentPlayer.playerId);
        action.playerID.set(0);
        action.targetCellX.set(tileX & 0xFFFF);
        action.targetCellY.set(tileY & 0xFFFF);
        addSelectedUnitTokens(action);
        CServerApp.sendClientGameAction(action);

        CUnit voiceUnit = selectAcknowledgementVoiceUnit();
        if (voiceUnit != null) {
            voiceUnit.playMoveVoice();
        }
    }

    /**
     * Native: MapVisualObject::issueGuardOrder @00418E51.
     * Fully ported.
     */
    private void issueGuardOrder() {
        issueSelectedUnitTokenListOrder(GuardOrderAction.global, GuardOrderAction.ACTION_ID);
        CUnit voiceUnit = selectAcknowledgementVoiceUnit();
        if (voiceUnit != null) {
            voiceUnit.playDefendVoice();
        }
    }

    /**
     * Native: MapVisualObject::issueStandGroundOrder @00418F09.
     * Fully ported.
     */
    private void issueStandGroundOrder() {
        issueSelectedUnitTokenListOrder(StandGroundOrderAction.global, StandGroundOrderAction.ACTION_ID);
        CUnit voiceUnit = selectAcknowledgementVoiceUnit();
        if (voiceUnit != null) {
            voiceUnit.playDefendVoice();
        }
    }

    /**
     * Native: MapVisualObject::issueRetreatOrder @00419E72.
     * Fully ported.
     */
    private void issueRetreatOrder() {
        issueSelectedUnitTokenListOrder(RetreatOrderAction.global, RetreatOrderAction.ACTION_ID);
        CUnit voiceUnit = selectAcknowledgementVoiceUnit();
        if (voiceUnit != null) {
            voiceUnit.playRetreatVoice();
        }
    }

    /**
     * Native support extracted from MapVisualObject::issueGuardOrder @00418E51,
     * MapVisualObject::issueStandGroundOrder @00418F09, and MapVisualObject::issueRetreatOrder @00419E72.
     */
    private void issueSelectedUnitTokenListOrder(UnitTokenListAction action, int actionId) {
        action.ID.set(actionId);
        action.netID.set(currentPlayer.playerId);
        action.playerID.set(0);
        addSelectedUnitTokens(action);
        CServerApp.sendClientGameAction(action);
    }

    /**
     * Native support extracted from MapVisualObject selected-token action loops at @00418BC3, @00418CB3, @00418D78.
     * Also covers @00418E51, @00418F09, @004190BE, @00419D8E, and @00419E72.
     */
    private void addSelectedUnitTokens(UnitTokenListAction action) {
        action.entryCount.set(0);
        action.unitTokenIds.set(new byte[0]);
        for (CGameObject object : objects.values()) {
            if (object.isSelected()) {
                action.addUnitToken(object.m_id);
            }
        }
    }

    /**
     * Native support extracted from MapVisualObject::UpdateMouseCursor @004175C7 cast-cursor branch.
     */
    private boolean handleMapCastClick(int tileX, int tileY) {
        SpellPanelVisualObject spellPanel = Globals.mainWindow.pSpellPanelVisualObject;
        int spellSlot = spellPanel.getActiveSpellSlot();
        if (spellSlot < 0) {
            return false;
        }

        boolean activeSpellTargetsUnit = spellPanel.activeSpellTargetsUnit();
        if (hoveredObjectToken == 0 && activeSpellTargetsUnit) {
            return true;
        }
        if (hoveredObject instanceof CUnit && activeSpellTargetsUnit) {
            issueCastSpellAtUnit(hoveredObjectToken, spellSlot + 1);
            return true;
        }
        issueCastSpellAtPoint(tileX, tileY, spellSlot + 1);
        return true;
    }

    /**
     * Native: MapVisualObject::IssueCastSpellAtPoint @0041918E.
     * Fully ported.
     */
    private void issueCastSpellAtPoint(int targetX, int targetY, int spellSlot) {
        SpellPanelVisualObject spellPanel = Globals.mainWindow.pSpellPanelVisualObject;
        CastSpellAtPointAction castAction = CastSpellAtPointAction.global;
        castAction.ID.set(CastSpellAtPointAction.ACTION_ID);
        castAction.netID.set(currentPlayer.playerId);
        castAction.playerID.set(0);
        castAction.targetCellX.set(targetX & 0xFFFF);
        castAction.targetCellY.set(targetY & 0xFFFF);
        castAction.entryCount.set(0);
        castAction.unitTokenIds.set(new byte[0]);

        int zeroBasedSlot = spellSlot - 1;
        if (spellPanel.hasSelectedAvailableSpellSlot(zeroBasedSlot) && spellPanel.selectedSpellEntryIndex < 0) {
            castAction.spellSlot.set(spellSlot & 0xFF);
            addSelectedAvailableSpellCasterTokens(castAction, zeroBasedSlot);
            CServerApp.sendClientGameAction(castAction);
        }
        cancelPointSpellEffects(targetX, targetY, spellSlot);
        updateSelectionState();
    }

    /**
     * Native: MapVisualObject::IssueCastSpellAtUnit @004195FA.
     * Fully ported.
     */
    private void issueCastSpellAtUnit(short targetTokenId, int spellSlot) {
        SpellPanelVisualObject spellPanel = Globals.mainWindow.pSpellPanelVisualObject;
        CastSpellAtUnitAction castAction = CastSpellAtUnitAction.global;
        castAction.ID.set(CastSpellAtUnitAction.ACTION_ID);
        castAction.netID.set(currentPlayer.playerId);
        castAction.playerID.set(0);
        castAction.targetTokenId.set(Short.toUnsignedInt(targetTokenId));
        castAction.entryCount.set(0);
        castAction.unitTokenIds.set(new byte[0]);

        int zeroBasedSlot = spellSlot - 1;
        if (spellPanel.hasSelectedAvailableSpellSlot(zeroBasedSlot) && spellPanel.selectedSpellEntryIndex < 0) {
            castAction.spellSlot.set(spellSlot & 0xFF);
            addSelectedAvailableSpellCasterTokens(castAction, zeroBasedSlot);
            CServerApp.sendClientGameAction(castAction);
        }
        cancelUnitSpellEffects(targetTokenId, spellSlot);
        updateSelectionState();
    }

    /**
     * Native support extracted from MapVisualObject::IssueCastSpellAtPoint @0041918E selected-caster scan.
     */
    private void addSelectedAvailableSpellCasterTokens(UnitTokenListAction action, int zeroBasedSpellSlot) {
        int spellMask = 1 << (zeroBasedSpellSlot & 0x1F);
        for (CGameObject object : objects.values()) {
            if (object.isSelected() && (object.availableSpellMask & spellMask) != 0) {
                action.addUnitToken(object.m_id);
            }
        }
    }

    /**
     * Native support extracted from MapVisualObject::IssueCastSpellAtPoint @0041918E active-effect branch.
     */
    private void cancelPointSpellEffects(int targetX, int targetY, int spellSlot) {
        SpellPanelVisualObject spellPanel = Globals.mainWindow.pSpellPanelVisualObject;
        if (spellPanel.selectedSpellEntryIndex >= 0 && selectedCount == 1 && primarySelectedObject != null) {
            sendCancelPointSpellEffect(targetX, targetY, primarySelectedObject, spellPanel.selectedSpellEntryIndex);
            applyCancelledSpellEntryMutation(primarySelectedObject, spellPanel.selectedSpellEntryIndex);
            clearSpellPanelSelectionAfterEffectCancel(spellPanel);
            return;
        }

        int zeroBasedSlot = spellSlot - 1;
        if (!spellPanel.hasActiveSpellEffectSlot(zeroBasedSlot)) {
            return;
        }

        int spellMask = 1 << (zeroBasedSlot & 0x1F);
        for (CGameObject object : objects.values()) {
            if (!object.isSelected() || (object.activeSpellEffectMask & spellMask) == 0) {
                continue;
            }
            int entryIndex = resolveActiveSpellEntryIndex(object, spellPanel.selectedSpellEntryIndex, spellSlot);
            if (entryIndex >= 0) {
                sendCancelPointSpellEffect(targetX, targetY, object, entryIndex);
                applyCancelledSpellEntryMutation(object, entryIndex);
                clearSpellPanelSelectionAfterEffectCancel(spellPanel);
            }
        }
    }

    /**
     * Native support extracted from MapVisualObject::IssueCastSpellAtUnit @004195FA active-effect branch.
     */
    private void cancelUnitSpellEffects(short targetTokenId, int spellSlot) {
        SpellPanelVisualObject spellPanel = Globals.mainWindow.pSpellPanelVisualObject;
        if (spellPanel.selectedSpellEntryIndex >= 0 && selectedCount == 1 && primarySelectedObject != null) {
            sendCancelUnitSpellEffect(targetTokenId, primarySelectedObject, spellPanel.selectedSpellEntryIndex);
            applyCancelledSpellEntryMutation(primarySelectedObject, spellPanel.selectedSpellEntryIndex);
            clearSpellPanelSelectionAfterEffectCancel(spellPanel);
            return;
        }

        int zeroBasedSlot = spellSlot - 1;
        if (!spellPanel.hasActiveSpellEffectSlot(zeroBasedSlot)) {
            return;
        }

        int spellMask = 1 << (zeroBasedSlot & 0x1F);
        for (CGameObject object : objects.values()) {
            if (!object.isSelected() || (object.activeSpellEffectMask & spellMask) == 0) {
                continue;
            }
            int entryIndex = resolveActiveSpellEntryIndex(object, spellPanel.selectedSpellEntryIndex, spellSlot);
            if (entryIndex >= 0) {
                sendCancelUnitSpellEffect(targetTokenId, object, entryIndex);
                applyCancelledSpellEntryMutation(object, entryIndex);
                clearSpellPanelSelectionAfterEffectCancel(spellPanel);
            }
        }
    }

    /**
     * Native support extracted from MapVisualObject::IssueCastSpellAtPoint @0041918E spell-entry index search.
     * Also covers MapVisualObject::IssueCastSpellAtUnit @004195FA and
     * MapVisualObject::ActivateSpellPanelSlot @00419A54.
     */
    private int resolveActiveSpellEntryIndex(CGameObject object, int selectedEntryIndex, int spellSlot) {
        if (selectedEntryIndex >= 0) {
            return selectedEntryIndex;
        }

        int spellId = Byte.toUnsignedInt(getSpellId(spellSlot - 1));
        for (int i = 0; i < object.tokenEntries.size(); i++) {
            TokenEntry entry = object.tokenEntries.get(i);
            if (entry != null
                    && (entry.wireFlags & TokenEntry.CANCELABLE_SPELL_FLAG) != 0
                    && entry.getCastSpellId() == spellId) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Native support extracted from MapVisualObject::IssueCastSpellAtPoint @0041918E cancel-action send.
     */
    private void sendCancelPointSpellEffect(int targetX, int targetY, CGameObject caster, int spellEffectIndex) {
        CancelPointSpellEffectAction action = CancelPointSpellEffectAction.global;
        action.ID.set(CancelPointSpellEffectAction.ACTION_ID);
        action.netID.set(currentPlayer.playerId);
        action.playerID.set(0);
        action.targetCellX.set(targetX & 0xFFFF);
        action.targetCellY.set(targetY & 0xFFFF);
        action.entryCount.set(0);
        action.unitTokenIds.set(new byte[0]);
        action.addUnitToken(caster.m_id);
        action.spellEffectIndex.set(spellEffectIndex & 0xFFFF);
        CServerApp.sendClientGameAction(action);
    }

    /**
     * Native support extracted from MapVisualObject::IssueCastSpellAtUnit @004195FA and
     * MapVisualObject::ActivateSpellPanelSlot @00419A54 cancel-action send.
     */
    private void sendCancelUnitSpellEffect(short targetTokenId, CGameObject caster, int spellEffectIndex) {
        CancelUnitSpellEffectAction action = CancelUnitSpellEffectAction.global;
        action.ID.set(CancelUnitSpellEffectAction.ACTION_ID);
        action.netID.set(currentPlayer.playerId);
        action.playerID.set(0);
        action.targetTokenId.set(Short.toUnsignedInt(targetTokenId));
        action.entryCount.set(0);
        action.unitTokenIds.set(new byte[0]);
        action.addUnitToken(caster.m_id);
        action.spellEffectIndex.set(spellEffectIndex & 0xFFFF);
        CServerApp.sendClientGameAction(action);
    }

    /**
     * Native support extracted from MapVisualObject::IssueCastSpellAtPoint @0041918E,
     * MapVisualObject::IssueCastSpellAtUnit @004195FA, and MapVisualObject::ActivateSpellPanelSlot @00419A54.
     */
    private static void applyCancelledSpellEntryMutation(CGameObject object, int spellEntryIndex) {
        TokenEntry entry = object.tokenEntries.get(spellEntryIndex);
        if (entry.quantity == 1) {
            object.tokenEntries.remove(spellEntryIndex);
        } else {
            entry.quantity--;
        }
    }

    /**
     * Native support extracted from MapVisualObject::IssueCastSpellAtPoint @0041918E spell-panel cleanup.
     * Also covers MapVisualObject::IssueCastSpellAtUnit @004195FA and
     * MapVisualObject::ActivateSpellPanelSlot @00419A54.
     */
    private static void clearSpellPanelSelectionAfterEffectCancel(SpellPanelVisualObject spellPanel) {
        spellPanel.selectedSpellEntryIndex = -1;
        spellPanel.selectedSpellSlot = -1;
    }

    /**
     * Native: MapVisualObject::UpdateVisibleSectorCostCell @00403B8F.
     * Fully ported. Java receives the native scratch grid as an explicit parameter.
     */
    private boolean updateVisibleSectorCostCell(
            int[][] visibleSectorCostGrid,
            int gridX,
            int gridY,
            int startTileX,
            int startTileY,
            int centerHeight
    ) {
        int previousGridX = gridX + visibilityStepXGrid[gridX][gridY];
        int previousGridY = gridY + visibilityStepYGrid[gridX][gridY];
        int tileX = startTileX + gridX;
        int tileY = startTileY + gridY;
        visibleSectorCostGrid[gridX][gridY] = visibleSectorCostGrid[previousGridX][previousGridY]
                - (visibilityDistanceCorrectionGrid[gridX][gridY]
                + (mapDescriptor.heightAt(tileX, tileY) - centerHeight));
        return visibleSectorCostGrid[gridX][gridY] < 1;
    }

    /**
     * Native: MapVisualObject::ActivateSpellPanelSlot @00419A54.
     * Fully ported.
     */
    public void activateSpellPanelSlot(int spellSlot) {
        SpellPanelVisualObject spellPanel = Globals.mainWindow.pSpellPanelVisualObject;
        int zeroBasedSlot = spellSlot - 1;
        if (spellPanel.hasSelectedAvailableSpellSlot(zeroBasedSlot) && spellPanel.selectedSpellEntryIndex < 0) {
            int spellMask = 1 << (zeroBasedSlot & 0x1F);
            for (CGameObject object : objects.values()) {
                if (!object.isSelected() || (object.availableSpellMask & spellMask) == 0) {
                    continue;
                }
                sendSelfTargetSpellCast(object, spellSlot);
            }
        }

        if (spellPanel.hasActiveSpellEffectSlot(zeroBasedSlot)) {
            int spellMask = 1 << (zeroBasedSlot & 0x1F);
            for (CGameObject object : objects.values()) {
                if (!object.isSelected() || (object.activeSpellEffectMask & spellMask) == 0) {
                    continue;
                }
                int entryIndex = resolveActiveSpellEntryIndex(object, spellPanel.selectedSpellEntryIndex, spellSlot);
                if (entryIndex >= 0) {
                    sendCancelUnitSpellEffect((short) object.m_id, object, entryIndex);
                    applyCancelledSpellEntryMutation(object, entryIndex);
                    clearSpellPanelSelectionAfterEffectCancel(spellPanel);
                }
            }
        }
        updateSelectionState();
    }

    /**
     * Native support extracted from MapVisualObject::ActivateSpellPanelSlot @00419A54 available-spell branch.
     */
    private void sendSelfTargetSpellCast(CGameObject object, int spellSlot) {
        CastSpellAtUnitAction action = CastSpellAtUnitAction.global;
        action.netID.set(currentPlayer.playerId);
        action.playerID.set(0);
        action.ID.set(CastSpellAtUnitAction.ACTION_ID);
        action.spellSlot.set(spellSlot & 0xFF);
        action.entryCount.set(0);
        action.unitTokenIds.set(new byte[0]);
        action.addUnitToken(object.m_id);
        action.targetTokenId.set(object.m_id & 0xFFFF);
        CServerApp.sendClientGameAction(action);
    }

    /**
     * Native support extracted from SpellPanelVisualObject::OnKeyDown @004C74B1.
     */
    public void toggleAutoCastSpellByPanelSlot(int spellSlot) {
        int spellId = Byte.toUnsignedInt(getSpellId(spellSlot));
        if (spellId != 0) {
            toggleAutoCastSpellBySpellId(spellId);
        }
    }

    /**
     * Native: MapVisualObject::toggleAutoCastSpellBySpellId @0041A727.
     * Fully ported.
     */
    private void toggleAutoCastSpellBySpellId(int spellId) {
        int spellMask = 1 << (spellId & 0x1F);
        for (Map.Entry<Short, CGameObject> entry : objects.entrySet()) {
            CGameObject object = entry.getValue();
            if (!object.isSelected() || (object.spellbookMask & spellMask) == 0) {
                continue;
            }

            ToggleUnitAutocastSpellAction action = ToggleUnitAutocastSpellAction.global;
            action.ID.set(ToggleUnitAutocastSpellAction.ACTION_ID);
            action.netID.set(resolveGameActionNetID());
            action.playerID.set(0);
            action.firstPayloadDword.set(4);
            action.secondPayloadDword.set(Short.toUnsignedInt(entry.getKey()) | (spellId << 16));
            CServerApp.sendClientGameAction(action);
            object.autoCastSpellId = (byte) spellId;
        }
    }

    /**
     * Native: MapVisualObject::applyFormationMode @0041A617.
     * Fully ported.
     */
    public void applyFormationMode(int formationMode) {
        dispatchUpdateBattlePreference(BATTLE_PREFERENCE_FORMATION_MODE, formationMode);
    }

    /**
     * Native: MapVisualObject::applyWimpyMode @0041A4EF.
     * Fully ported.
     */
    public void applyWimpyMode(int wimpyMode) {
        dispatchUpdateBattlePreference(BATTLE_PREFERENCE_WIMPY_MODE, wimpyMode);
    }

    /**
     * Native: MapVisualObject::applyAutoCasting @0041A5B9.
     * Fully ported.
     */
    public void applyAutoCasting() {
        dispatchUpdateBattlePreference(
                BATTLE_PREFERENCE_AUTO_CASTING_MODE,
                Globals.gamePreferences.autoCasting
        );
    }

    /**
     * Native: MapVisualObject::sendAltDebugCommand @0041A672.
     * Fully ported.
     */
    public void sendAltDebugCommand(int command) {
        AltDebugCommandAction action = AltDebugCommandAction.global;
        action.ID.set(AltDebugCommandAction.ACTION_ID);
        action.netID.set(currentPlayer.playerId);
        action.playerID.set(0);
        action.firstPayloadDword.set(BATTLE_PREFERENCE_ALT_DEBUG_COMMAND);
        action.secondPayloadDword.set(command);
        CServerApp.sendClientGameAction(action);
    }

    /**
     * Native: MapVisualObject::LoadScenarioMap @0041BEE2.
     * Fully ported.
     */
    public void loadScenarioMap(String scenarioName) {
        mapDescriptor = null;
        cachedMapWidth = 0;
        cachedMapHeight = 0;

        String descriptorPath = Globals.mainWindow.sessionMode == 2
                ? Resources.path(SCENARIO, scenarioName)
                : scenarioName;
        setMapDescriptor(new MapDescriptor(descriptorPath));
        mapOccupancyDirty = 1;

        Globals.mainWindow.pRightPanelContainerVisualObject.onMessage(SET_MAP_CONTEXT, this, 0);
        refreshTimeFlowLighting(true);
        TerrainGraphics.reloadTerrainTileGraphics(mapDescriptor.terrainTileMask);
        questStorage.removeAndDeleteQuestsForOwner(0);
    }

    /**
     * Native: MapVisualObject::SetMapDescriptor @00403F08.
     * Fully ported. Java-only zoom addition: after native map dimensions are cached, rebuild current/max zoom grid
     * metrics so the max-zoom allocation can be clamped to the actual map size before the first rendered frame.
     */
    public void setMapDescriptor(MapDescriptor mapDescriptor) {
        this.mapDescriptor = mapDescriptor;
        cachedMapWidth = mapDescriptor.getWidth();
        cachedMapHeight = mapDescriptor.getHeight();
        recalculateGridMetrics();
    }

    /**
     * Native support extracted from CenteredDialogVariantVisualObject::OnMessage @00442E8B.
     */
    public void refreshTimeFlowLighting() {
        refreshTimeFlowLighting(true);
    }

    /**
     * Native: MapVisualObject::RefreshTimeFlowLighting @0041D3B9.
     * Java refreshes every game minute so CGameLighting can expose smooth per-minute samples instead of native
     * 20-minute interval jumps.
     */
    public void refreshTimeFlowLighting(boolean forceRefresh) {
        if (GAMEPLAY.isUnsetIn(Globals.mainWindow.dialogsMask)) {
            return;
        }

        int serverLoopCounter = Globals.mainWindow.serverLoopCounter;
        int timeValue = (serverLoopCounter >>> 4) + 0x168;
        boolean scheduledRefresh = (serverLoopCounter & 0xF) == 0
                && timeValue != lastTimeFlowLightingTimeValue
                && Globals.gamePreferences.showTimeFlow != 0;
        if (!scheduledRefresh && !forceRefresh) {
            return;
        }

        lastTimeFlowLightingTimeValue = timeValue;
        CGameLighting.UpdateGlobalLighting(timeValue);
        mapDescriptor.recalculateTerrainLighting(1, 1, 0, 0);
        applyTerrainLightOverrides();
        refreshGamePalettesAfterLighting();
        mapOccupancyDirty = 1;
        renderFrameDirty = 1;
        Globals.mainWindow.pMinimapVisualObject.onMessage(REBUILD_MINIMAP_BITMAPS, 0, 0);
    }

    /**
     * Native: MapVisualObject::ApplyTerrainLightOverrides @00404910.
     * Fully ported.
     */
    private void applyTerrainLightOverrides() {
        byte[] terrainLight = mapDescriptor.getTerrainLightWxH();
        for (Map.Entry<Integer, Integer> entry : terrainLightOverrideCells.entrySet()) {
            int packedTile = entry.getKey();
            int flags = entry.getValue();
            int tileX = packedTile & 0xFF;
            int tileY = (packedTile & 0xFFFF) >> 8;
            if ((flags & 0x8000) != 0) {
                writeTerrainLightOverride(terrainLight, tileX, tileY, (byte) 0);
            }
            if ((flags & 0x4000) != 0) {
                writeTerrainLightOverride(terrainLight, tileX, tileY, (byte) 0x50);
            }
        }
    }

    /**
     * Native support extracted from MapVisualObject::ApplyTerrainLightOverrides @00404910.
     */
    private void writeTerrainLightOverride(byte[] terrainLight, int tileX, int tileY, byte value) {
        int tileIndex = mapDescriptor.tileIndex(tileX, tileY);
        terrainLight[tileIndex] = value;
        terrainLight[tileIndex + 1] = value;
        terrainLight[tileIndex + cachedMapWidth] = value;
        terrainLight[tileIndex + cachedMapWidth + 1] = value;
    }

    /**
     * Native: Global::RefreshGamePalettes @0047E345.
     * Fully ported. Java keeps this global native helper co-located with its only native caller,
     * MapVisualObject::RefreshTimeFlowLighting @0041D3B9.
     */
    private void refreshGamePalettesAfterLighting() {
        refreshUnitPalettesAfterLighting();
        Globals.mousePointer.update();
        Palettes.loadUnitOwnerPalettes();
        Globals.mousePointer.update();
        TerrainGraphics.refreshTerrainTilePalettes();
        Globals.mousePointer.update();
        refreshVisualObjectPalettesAfterLighting();
        Globals.mousePointer.update();
        refreshStructurePalettesAfterLighting();
        Globals.mousePointer.update();
        refreshProjectilePalettesAfterLighting();
        Globals.mousePointer.update();
        GUI.sprBackpack.initPalette(0x10, 2, 1);
    }

    /**
     * Native support extracted from RefreshGamePalettes @0047E345 unit-type palette invalidation.
     */
    private static void refreshUnitPalettesAfterLighting() {
        Globals.mousePointer.update();
        for (CUnitInfo unitInfo : UnitTypes.UNIT_TYPES_BY_ID) {
            if (unitInfo == null) {
                continue;
            }
            for (int paletteIndex = 0; paletteIndex < unitInfo.m_PaletteIndex; paletteIndex++) {
                if (unitInfo.m_Palettes[paletteIndex] != null) {
                    unitInfo.m_Palettes[paletteIndex].free();
                }
            }
        }
    }

    /**
     * Native support extracted from RefreshGamePalettes @0047E345 loaded visual-object sprite palette refresh.
     */
    private static void refreshVisualObjectPalettesAfterLighting() {
        for (GraphicsObjectsFile graphicsObjectsFile : VObjects.GRAPHICS_OBJECTS_FILES) {
            if (graphicsObjectsFile != null && graphicsObjectsFile.loaded) {
                graphicsObjectsFile.sprite.initPalette(0x10, 2, 1);
            }
        }
    }

    /**
     * Native support extracted from RefreshGamePalettes @0047E345 loaded structure sprite palette refresh.
     */
    private static void refreshStructurePalettesAfterLighting() {
        for (StructureDef structureDef : Structures.STRUCTURE_DEFS) {
            if (structureDef != null && structureDef.spritesLoaded) {
                structureDef.spriteMain.initPalette(0x10, 2, 1);
            }
        }
    }

    /**
     * Native support extracted from RefreshGamePalettes @0047E345 loaded projectile sprite palette refresh.
     */
    private static void refreshProjectilePalettesAfterLighting() {
        for (CProjectileInfo projectileInfo : Projectiles.PROJECTILES_BY_ID) {
            if (projectileInfo != null
                    && projectileInfo.loaded
                    && projectileInfo.palette != 0
                    && !projectileInfo.a16) {
                projectileInfo.spriteA.initPalette(0x10, 2, 1);
            }
        }
    }

    /**
     * Native support extracted from MapVisualObject::applyWimpyMode @0041A4EF,
     * MapVisualObject::applyFormationMode @0041A617, and MapVisualObject::applyAutoCasting @0041A5B9.
     */
    private void dispatchUpdateBattlePreference(int preferenceKind, int preferenceValue) {
        UpdateBattlePreferenceAction action = UpdateBattlePreferenceAction.global;
        action.ID.set(UpdateBattlePreferenceAction.ACTION_ID);
        action.netID.set(resolveGameActionNetID());
        action.playerID.set(0);
        action.firstPayloadDword.set(preferenceKind);
        action.secondPayloadDword.set(preferenceValue);
        CServerApp.sendClientGameAction(action);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040F67B / @0040FE15 / @004105B7.
     */
    public static List<TokenEntry> readTokenEntries(byte[] data, int count, boolean includeGridModeTag) {
        ByteBuffer cursor = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        List<TokenEntry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(new TokenEntry(cursor, includeGridModeTag));
        }
        return entries;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @0040F67B.
     */
    public static void copyEquipmentTokenEntries(CUnit unit, List<TokenEntry> entries) {
        for (int slotIndex = 0; slotIndex < unit.equipmentTokenEntries.length; slotIndex++) {
            unit.equipmentTokenEntries[slotIndex] = slotIndex < entries.size()
                    ? normalizeEquipmentTokenEntry(entries.get(slotIndex))
                    : null;
        }
        unit.unitFlags |= 0x08;
        unit.refreshUnitSpritesAfterRuntimeCopy();
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00411C1A item-list subtype 1 branch.
     */
    public static void applyUnitEquipmentPayload(CUnit unit, byte[] data, int slotMask) {
        ByteBuffer cursor = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        for (int slotIndex = 0; slotIndex < unit.equipmentTokenEntries.length; slotIndex++) {
            if ((slotMask & (1 << slotIndex)) == 0) {
                continue;
            }
            unit.equipmentTokenEntries[slotIndex] = readUnitEquipmentTokenEntry(cursor);
        }
        unit.unitFlags |= 0x08;
        unit.refreshUnitSpritesAfterRuntimeCopy();
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00411D11-00412026.
     */
    private static TokenEntry readUnitEquipmentTokenEntry(ByteBuffer cursor) {
        TokenEntry entry = new TokenEntry(cursor, false);
        return normalizeEquipmentTokenEntry(entry);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00411D79-00411DA6.
     */
    private static TokenEntry normalizeEquipmentTokenEntry(TokenEntry entry) {
        if (entry.packedTokenHash == 0) {
            return null;
        }
        entry.gridModeCode = 1;
        return entry;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @004119FC and @00411B3F.
     */
    public CProjectile spawnProjectile(int sourceX, int sourceY, int targetX, int targetY, int projectileType, int segments, short targetToken) {
        CProjectile projectile = new CProjectile();
        projectile.type = projectileType;
        projectile.location.x = sourceX;
        projectile.location.y = sourceY;
        projectile.location2.x = projectile.location.x;
        projectile.location2.y = projectile.location.y;
        projectile.actionX = targetX;
        projectile.actionY = targetY;
        projectile.actionTarget = targetToken;
        projectile.actionSegments = segments;
        projectile.actionPhase = -1;
        projectile.action = 1;
        projectile.cPlayer = currentPlayer;
        projectile.pMapVisualObject = this;
        projectile.refreshMapDerivedState();
        addTransientObject(projectile);
        return projectile;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00411864.
     */
    public static void startSpellVisual(CGameObject caster, byte[] actionPayload) {
        int spellId = u8(actionPayload, 2);
        int actionX = caster.location.x;
        int actionY = caster.location.y;
        int actionTarget = 0;
        CProjectileInfo projectileInfo = projectileInfoOrNull(spellId);
        if (projectileInfo != null) {
            if (projectileInfo.homing == 0) {
                actionX = pixelCenterFromTile(u8(actionPayload, 3));
                actionY = pixelCenterFromTile(u8(actionPayload, 4));
            } else {
                actionTarget = u16(actionPayload, 3);
            }
        }
        startSpellVisual(caster, actionTarget, spellId, actionX, actionY);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00411864 and @00411CB9.
     */
    public static void startSpellVisual(CGameObject caster, int actionTarget, int spellId, int actionX, int actionY) {
        CUnitInfo unitInfo = UnitTypes.getUnitInfo(caster.type);
        if (caster.actionSegments != 0 || unitInfo == null || unitInfo.m_AttackPhases == 0) {
            return;
        }
        caster.actionSegments = unitInfo.m_AttackFrameSequenceCount;
        caster.action = 8;
        caster.actionPhase = 0;
        caster.actionTarget = (short) actionTarget;
        caster.actionX = actionX;
        caster.actionY = actionY;
        caster.actionSpell = spellId;
        caster.pMapVisualObject.renderFrameDirty = 1;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00411864 projectile-info probe.
     */
    public static boolean hasProjectileInfo(int projectileId) {
        return projectileInfoOrNull(projectileId) != null;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @004119FC projectile homing branch.
     */
    public static boolean isHomingProjectile(int projectileId) {
        CProjectileInfo projectileInfo = projectileInfoOrNull(projectileId);
        return projectileInfo != null && projectileInfo.homing != 0;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00411864 projectile-info probe.
     */
    private static CProjectileInfo projectileInfoOrNull(int projectileId) {
        if (projectileId < 0 || Projectiles.PROJECTILES_BY_ID.size() <= projectileId) {
            return null;
        }
        return Projectiles.PROJECTILES_BY_ID.get(projectileId);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00412FC5.
     */
    public static TokenEntry createHashOnlyTokenEntry(int tokenHash, int gridModeCode) {
        TokenEntry token = new TokenEntry();
        token.packedTokenHash = tokenHash;
        token.quantity = 1;
        token.gridModeCode = gridModeCode;
        return token;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @004156C5 / @00415911.
     */
    public CPlayer findClientPlayerById(int playerId) {
        for (CPlayer player : clientPlayers) {
            if (player != null && player.playerId == playerId) {
                return player;
            }
        }
        return null;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @004156C5.
     */
    public CPlayer ensureClientPlayerById(int playerId) {
        CPlayer player = findClientPlayerById(playerId);
        if (player != null) {
            return player;
        }
        CPlayer created = new CPlayer();
        created.playerId = playerId;
        while (clientPlayers.size() <= playerId) {
            clientPlayers.add(null);
        }
        clientPlayers.set(playerId, created);
        return created;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00415911 object removal by owner pointer.
     */
    public void removeScenarioObjectsOwnedBy(CPlayer removedPlayer) {
        Iterator<CGameObject> iterator = objects.values().iterator();
        while (iterator.hasNext()) {
            CGameObject object = iterator.next();
            if (removedPlayer != null && object.cPlayer == removedPlayer) {
                iterator.remove();
            }
        }
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00415911 CPlayer list removal.
     */
    public void removeClientPlayerById(int playerId) {
        clientPlayers.removeIf(player -> player != null && player.playerId == playerId);
    }

    /**
     * Native: MapVisualObject::ApplyTileVisibilityMaskRuns @0041C071.
     * Fully ported.
     */
    public void applyTileVisibilityMaskRuns(short[] runLengths) {
        int tileMask = Short.toUnsignedInt(runLengths[0]) != 0 ? TERRAIN_DEAD_VISUAL_OBJECT_MASK : 0;
        int tileX = 8;
        int tileY = 8;

        for (int runIndex = 1; runIndex < runLengths.length; runIndex++) {
            int runLength = Short.toUnsignedInt(runLengths[runIndex]);
            for (int step = 0; step < runLength; step++) {
                int flatIndex = tileX + tileY * cachedMapWidth;
                int tileWord = mapDescriptor.tileWordFlatAt(flatIndex);
                mapDescriptor.setTileWordFlatAt(flatIndex, (tileWord & 0xDFFF) | tileMask);
                tileX++;
                if (tileX == cachedMapWidth - 8) {
                    tileX = 8;
                    tileY++;
                }
            }
            tileMask ^= TERRAIN_DEAD_VISUAL_OBJECT_MASK;
        }
    }

    /**
     * Native: MapVisualObject::ApplyAreaEffectFootprint @0041C18C.
     * Fully ported.
     */
    public void applyAreaEffectFootprint(
            int originX,
            int originY,
            int footprintWidth,
            int footprintHeight,
            int effectFlagIndex,
            byte[] occupancyBitmap,
            int applyFlag
    ) {
        byte[] terrainLight = mapDescriptor.getTerrainLightWxH();
        int flagMask = 1 << (effectFlagIndex & 0x1F);
        for (int footprintX = 0; footprintX < footprintWidth; footprintX++) {
            for (int footprintY = 0; footprintY < footprintHeight; footprintY++) {
                int footprintBit = footprintY * footprintWidth + footprintX;
                int occupancyByte = Byte.toUnsignedInt(occupancyBitmap[footprintBit >> 3]);
                if ((occupancyByte & (1 << (footprintBit & 7))) == 0) {
                    continue;
                }

                int tileX = originX + footprintX;
                int tileY = originY + footprintY;
                applyAreaEffectFootprintCell(terrainLight, tileX, tileY, flagMask, effectFlagIndex, applyFlag);
                renderFrameDirty = 1;
            }
        }
    }

    /**
     * Native support extracted from MapVisualObject::ApplyAreaEffectFootprint @0041C18C per-cell branch.
     */
    private void applyAreaEffectFootprintCell(
            byte[] terrainLight,
            int tileX,
            int tileY,
            int flagMask,
            int effectFlagIndex,
            int applyFlag
    ) {
        int packedTile = (tileX & 0xFF) | ((tileY & 0xFF) << 8);
        int flags = terrainLightOverrideCells.getOrDefault(packedTile, 0);
        if (applyFlag == 0) {
            flags &= ~flagMask;
            if (flags == 0) {
                terrainLightOverrideCells.remove(packedTile);
            } else {
                terrainLightOverrideCells.put(packedTile, flags);
            }
            if (effectFlagIndex == 3) {
                ambientAudioViewX++;
            } else if (effectFlagIndex > 0x0D && effectFlagIndex < 0x10) {
                mapDescriptor.recalculateTerrainLighting(tileX, tileY, 2, 2);
            }
            return;
        }

        terrainLightOverrideCells.put(packedTile, flags | flagMask);
        int flatIndex = tileX + tileY * cachedMapWidth;
        if (effectFlagIndex == 3) {
            int tileWord = mapDescriptor.tileWordFlatAt(flatIndex);
            if ((tileWord & TERRAIN_DEAD_VISUAL_OBJECT_MASK) == 0) {
                int encodedVisualObjectId = mapDescriptor.objectIdFlatAt(flatIndex);
                if (encodedVisualObjectId != 0) {
                    VObject visualObject = Objects.requireNonNull(
                            VObjects.getVObject(encodedVisualObjectId - 1),
                            "Missing VObject id " + (encodedVisualObjectId - 1)
                    );
                    if (visualObject.deadObjectId != -1) {
                        transientEffectCells.put(packedTile, 0);
                    }
                }
            }
            ambientAudioViewX++;
            mapDescriptor.setTileWordFlatAt(flatIndex, tileWord | TERRAIN_DEAD_VISUAL_OBJECT_MASK);
        } else if (effectFlagIndex == 0x0E) {
            writeTerrainLightOverride(terrainLight, flatIndex, (byte) 0x50);
        } else if (effectFlagIndex == 0x0F) {
            writeTerrainLightOverride(terrainLight, flatIndex, (byte) 0);
        }
    }

    /**
     * Native support extracted from MapVisualObject::ApplyAreaEffectFootprint @0041C18C terrain-light writes.
     */
    private void writeTerrainLightOverride(byte[] terrainLight, int flatIndex, byte value) {
        terrainLight[flatIndex] = value;
        terrainLight[flatIndex + 1] = value;
        terrainLight[flatIndex + cachedMapWidth] = value;
        terrainLight[flatIndex + cachedMapWidth + 1] = value;
    }

    /**
     * Native: MapVisualObject::sendMapLoadComplete @0041C79A.
     * Fully ported.
     */
    public void sendMapLoadComplete() {
        MapLoadCompleteAction action = MapLoadCompleteAction.global;
        action.ID.set(MapLoadCompleteAction.ACTION_ID);
        action.netID.set(resolveGameActionNetID());
        action.playerID.set(0);
        CServerApp.sendClientGameAction(action);
    }

    /**
     * Native: MapVisualObject::sendReviveStuckHeroAction @0041C851.
     * Fully ported.
     */
    private void sendReviveStuckHeroAction() {
        ReviveStuckHeroAction action = ReviveStuckHeroAction.global;
        action.ID.set(ReviveStuckHeroAction.ACTION_ID);
        action.netID.set(resolveGameActionNetID());
        action.playerID.set(0);
        CServerApp.sendClientGameAction(action);
    }

    /**
     * Native support extracted from decompressKnowledgeTable @00421080, decodeRepeatedKnowledgeRun @00422B90,
     * and decodeLiteralKnowledgeRun @00422C10.
     * Fully ported.
     */
    public static byte[] decompressKnowledgeTable(byte[] compressedBytes) {
        ByteBuffer input = ByteBuffer.wrap(compressedBytes).order(ByteOrder.LITTLE_ENDIAN);
        byte[] output = new byte[input.getInt()];
        int outputOffset = 0;
        while (input.hasRemaining() && outputOffset < output.length) {
            int control = Byte.toUnsignedInt(input.get());
            if ((control & 0x80) == 0) {
                input.get(output, outputOffset, control);
                outputOffset += control;
                continue;
            }

            int repeat = control & 0x7F;
            byte value = input.get();
            Arrays.fill(output, outputOffset, outputOffset + repeat, value);
            outputOffset += repeat;
        }
        return output;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @004153CC / @0041547D.
     */
    public static void refreshPlayerKnowledgeIfNeeded() {
        if (Globals.mainWindow.sessionMode != CMainWindow.SESSION_MODE_CAMPAIGN) {
            Globals.mainWindow.rebuildDiplomacy();
        }
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @00414624 / @00414801.
     */
    public static void loadQuestListIntoStorage(QuestsStorage storage, int valueCount, byte[] values) {
        short[] words = decodeShortValues(valueCount, values);
        int questCount = Short.toUnsignedInt(words[0]);
        storage.removeAndDeleteQuestsForOwner(0);
        for (int questIndex = 0; questIndex < questCount; questIndex++) {
            int base = 1 + questIndex * QUEST_WORD_COUNT;
            Quest quest = Quest.createById(Short.toUnsignedInt(words[base]));
            quest.setQuestData(
                    readQuestListDword(words, base + 1),
                    readQuestListDword(words, base + 3),
                    readQuestListDword(words, base + 5),
                    readQuestListDword(words, base + 9),
                    readQuestListDword(words, base + 11),
                    readQuestListDword(words, base + 13),
                    0
            );
            quest.state = readQuestListDword(words, base + 7);
            storage.addQuest(quest);
        }
    }

    /**
     * Native support extracted from the raw Quest_Base memcpy consumed by MapVisualObject::HandleGameAction @00414624 / @00414801.
     */
    private static int readQuestListDword(short[] words, int offset) {
        return Short.toUnsignedInt(words[offset]) | (Short.toUnsignedInt(words[offset + 1]) << Short.SIZE);
    }

    /**
     * Native support extracted from ShortArrayBlobAction::ReadPayload @0050C7AE.
     */
    public static short[] decodeShortValues(int valueCount, byte[] values) {
        ByteBuffer cursor = ByteBuffer.wrap(values).order(ByteOrder.LITTLE_ENDIAN);
        short[] result = new short[Math.min(valueCount, values.length / Short.BYTES)];
        for (int i = 0; i < result.length; i++) {
            result[i] = cursor.getShort();
        }
        return result;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @004135BA / @004119FC / @00411B3F.
     */
    public static int pixelCenterFromTile(int tile) {
        return tile * TILE_PIXEL_SIZE + PROJECTILE_CENTER_OFFSET;
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @004135BA / @004119FC.
     */
    public static int u8(byte[] data, int offset) {
        return Byte.toUnsignedInt(data[offset]);
    }

    /**
     * Native support extracted from MapVisualObject::HandleGameAction @004135BA / @004119FC.
     */
    public static int u16(byte[] data, int offset) {
        return Short.toUnsignedInt(ByteBuffer.wrap(data, offset, Short.BYTES).order(ByteOrder.LITTLE_ENDIAN).getShort());
    }

}
