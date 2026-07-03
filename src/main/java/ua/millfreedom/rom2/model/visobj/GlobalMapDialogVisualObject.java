package ua.millfreedom.rom2.model.visobj;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.*;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.palette.Palettes;
import ua.millfreedom.rom2.model.sound.Sound;
import ua.millfreedom.rom2.model.sound.SoundSystem;
import ua.millfreedom.rom2.model.world.ScenarioLocation;
import ua.millfreedom.rom2.res.Resources;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import static ua.millfreedom.rom2.model.enums.MessageCodes.*;

/**
 * Native class: GlobalMapDialogVisualObject.
 * Purpose: global-map travel dialog with location hover text and route animation.
 */
public class GlobalMapDialogVisualObject extends HandlerVisualObject {
    public static final int NATIVE_SIZE = 0x1B4; //VERIFIED WITH NATIVE CODE, DO NOT RE-CHECK!

    private static final int RENDER_FRAME_CADENCE_MS = 100;
    private static final int ROUTE_ANIMATION_STEP = 8;
    private static final String GLOBAL_MAP_BMP = "main/graphics/global.map/gmap.bmp";
    private static final String UMOIR_BMP = "main/graphics/global.map/umoir.bmp";
    private static final String PATH_MAP_BMP = "graphics/global.map/pathmap.bmp";
    private static final String UMOIR_PATH_BMP = "graphics/global.map/umoirpath.bmp";
    private static final String HERO_BMP = "graphics/global.map/hero.bmp";
    private static final String BALL_MAP_BMP = "graphics/global.map/ballmap.bmp";
    private static final String FLAG1_SPRITES = "graphics/global.map/flag1/sprites.16a";
    private static final String FLAG_SPRITES = "graphics/global.map/flag/sprites.16a";
    private static final String CROSS_SPRITES = "graphics/global.map/cross/sprites.16a";
    private static final String MISSION_FLAG_SPRITES = "graphics/global.map/missionflag/sprites.16a";
    private static final String FLAG_ON_MAP_SPRITES = "graphics/global.map/flagonmap/sprites.16a";
    private static final String YOUR_FLAG_SPRITES = "graphics/global.map/yourflag/sprites.16a";
    private static final String SCROLL_01_BMP = "graphics/global.map/scroll01.bmp";
    private static final String SCROLL_02_BMP = "graphics/global.map/scroll02.bmp";
    private static final String SCROLL_03_BMP = "graphics/global.map/scroll03.bmp";
    private static final String SCROLL_P1_BMP = "graphics/global.map/scrollp1.bmp";
    private static final String SCROLL_P2_BMP = "graphics/global.map/scrollp2.bmp";
    private static final String SCROLL_P3_BMP = "graphics/global.map/scrollp3.bmp";
    private static final String SCROLL_UP_WAV = "sfx/scrollup.wav";
    private static final String SCROLL_DOWN_WAV = "sfx/scrolldn.wav";
    private static final String POINT1_WAV = "sfx/point1.wav";
    private static final String POINT2_WAV = "sfx/point2.wav";
    private static final int PATH_MAP_WIDTH = 0x280;
    private static final int PATH_MAP_HEIGHT = 0x1E0;
    private static final int PATH_PIXEL = 1;
    private static final int NODE_PIXEL = 2;

    // Native global @006212C8, render-frame cadence guard used by OnMessage @004715A5.
    private static int renderThrottleInitialized;
    // Native global @00621300, last accepted render-frame tick used by OnMessage @004715A5.
    private static int lastRenderFrameTick;

    //0x68
    public CBmp64k mapBackgroundBitmap;
    //0x6c
    public CBmp64k heroBitmap;
    //0x70
    public CBmp64k routeDotBitmap;
    //0x74
    public CA16 travelStartFlagSprite;
    //0x78
    public CA16 currentLocationFlagSprite;
    //0x7c
    public CA16 targetCrossSprite;
    //0x80
    public CA16 missionLocationFlagSprite;
    //0x84
    public CA16 availableLocationFlagSprite;
    //0x88
    public CA16 partyLocationFlagSprite;
    //0x8c
    public CBmp64k scrollTopBitmap;
    //0x90
    public CBmp64k scrollMiddleBitmap;
    //0x94
    public CBmp64k scrollBottomBitmap;
    //0x98
    public CBmp64k scrollPanelTopBitmap;
    //0x9c
    public CBmp64k scrollPanelBottomBitmap;
    //0xa0
    public CBmp64k scrollPanelMiddleBitmap;
    //0xa4
    public final List<Integer> locationAvailabilityFlags = new ArrayList<>();
    //0xb8
    public final List<CRect> locationHitRects = new ArrayList<>();
    //0xcc
    public final List<Point> locationPoints = new ArrayList<>();
    //0xe0
    public final List<Point> travelRoutePoints = new ArrayList<>();
    //0xf4
    public final CSize heroBitmapSize = new CSize();
    //0xfc
    public final Point heroDrawPoint = new Point();
    //0x104
    public final Point currentLocationPoint = new Point();
    //0x10c
    public final Point targetLocationPoint = new Point();
    //0x114
    public final CRect partyDetailsRect = new CRect();
    //0x124
    public int hoveredLocationIndex;
    //0x128
    public int mapFlagAnimationFrame;
    //0x12c
    public int travelProgress;
    //0x130
    public int partyFlagAnimationFrame;
    //0x134
    public int targetCrossAnimationFrame;
    //0x138
    public int bestRouteCost;
    //0x13c Native GlobalMapRouteNodeList; Java stores node indices here and carries native +0x14 routeCost as a parameter.
    public final List<Integer> routeNodeIndices = new ArrayList<>();
    //0x154
    public Object[][] routeAdjacencyMatrix;
    //0x158
    public final List<Point> graphNodePoints = new ArrayList<>();
    //0x16c Native CArray<GlobalMapLocationMetadata>; element constructor ported at GlobalMapLocationMetadata::New @00473740.
    public final List<GlobalMapLocationMetadata> locationMetadata = new ArrayList<>();
    //0x180
    public Sound scrollUpSound;
    //0x184
    public Sound scrollDownSound;
    //0x188
    public Sound routePointSound1;
    //0x18c
    public Sound routePointSound2;
    //0x190
    public int routePointSoundIndex;
    //0x194
    public int renderActiveFlag;
    //0x198
    public String hoveredLocationTitle = "";
    //0x19c
    public final List<String> hoveredLocationLines = new ArrayList<>();
    //0x1b0
    public int umoirMapMode;
    // Java helper, not a native field.
    public CVisualObject closeButton;

    /**
     * Native support class: GlobalMapLocationGraph, initialized by
     * GlobalMapLocationGraph::CreateLocationGraph @00471A75.
     * Native +0x0 pathMapBitmap and +0x4 nodeCount are represented by decoded pathPixels and nodePoints.size().
     */
    private static final class LocationGraph {
        //0x8 Native CArray<CPoint>** routeEdges.
        Object[][] adjacencyMatrix;

        //0xc Native CArray<CPoint> nodePoints.
        final List<Point> nodePoints = new ArrayList<>();

        //0x20 Native short[640][480] visit grid.
        final short[] visitGrid = new short[PATH_MAP_WIDTH * PATH_MAP_HEIGHT];

        //0x96020 Native byte* path pixels returned by CGameBitmap::GetPixels(pathMapBitmap).
        final byte[] pathPixels;

        /**
         * Native: GlobalMapLocationGraph::CreateLocationGraph @00471A75.
         * Fully ported. Native receives raw AfxNew(0x9602C) storage, initializes graph fields, loads the path map,
         * scans node pixels, allocates routeEdges, connects edges, and releases the temporary bitmap.
         */
        LocationGraph(GlobalMapDialogVisualObject dialog) {
            pathPixels = loadPathMapPixels(dialog);
            collectGraphNodes();
            adjacencyMatrix = new Object[nodePoints.size()][nodePoints.size()];
            connectLocationGraphEdges();
        }

        /**
         * Native: GlobalMapLocationGraph::TransferAdjacencyMatrix @0047242A.
         * Fully ported. Native returns routeEdges, clears the source field, and copies nodePoints to the caller array.
         */
        Object[][] transferAdjacencyMatrix(List<Point> outPoints) {
            Object[][] adjacencyMatrix = this.adjacencyMatrix;
            this.adjacencyMatrix = null;
            outPoints.addAll(nodePoints);
            return adjacencyMatrix;
        }

        /**
         * Native: GlobalMapLocationGraph::loadPathMapBitmap @00471DE8.
         * Fully ported. Java decodes the native CBmp256 path map into pathPixels; managed lifetime replaces
         * ReleasePathMapBitmap cleanup.
         */
        private static byte[] loadPathMapPixels(GlobalMapDialogVisualObject dialog) {
            String pathMap = dialog.umoirMapMode == 0 ? PATH_MAP_BMP : UMOIR_PATH_BMP;
            return new CBmp256(Resources.path(pathMap)).frames.getFirst().data();
        }

        /**
         * Native support extracted from the `pixel == 2` node scan in
         * GlobalMapLocationGraph::CreateLocationGraph @00471A75.
         */
        private void collectGraphNodes() {
            for (int x = 0; x < PATH_MAP_WIDTH; x++) {
                for (int y = 0; y < PATH_MAP_HEIGHT; y++) {
                    if (pathPixel(x, y) == NODE_PIXEL) {
                        nodePoints.add(new Point(x, y));
                    }
                }
            }
        }

        /**
         * Native: GlobalMapLocationGraph::connectLocationGraphEdges @00471F0D.
         * Fully ported.
         */
        private void connectLocationGraphEdges() {
            for (int startNodeIndex = 0; startNodeIndex < nodePoints.size(); startNodeIndex++) {
                List<Point> path = new ArrayList<>();
                Point startPoint = nodePoints.get(startNodeIndex);
                path.add(new Point(startPoint));
                markVisited(startPoint.x, startPoint.y, 1);

                int pathSize;
                do {
                    traceNextPathBranch(startPoint.x, startPoint.y, 2, path, startPoint);
                    pathSize = path.size();
                    if (pathSize != 1) {
                        int endNodeIndex = indexOfGraphNodeOrMinusOne(nodePoints, path.getLast());
                        if (endNodeIndex != -1 && adjacencyMatrix[startNodeIndex][endNodeIndex] == null) {
                            adjacencyMatrix[startNodeIndex][endNodeIndex] = copyPointList(path);
                            adjacencyMatrix[endNodeIndex][startNodeIndex] = reverseCopyPointList(path);
                            path.clear();
                            path.add(new Point(startPoint));
                        }
                    }
                } while (pathSize > 1);
            }
        }

        /**
         * Native: GlobalMapLocationGraph::traceNextPathBranch @004721FF.
         * Fully ported. Native always returns 0; Java returns from the void helper after appending the next
         * path segment or endpoint node.
         */
        private void traceNextPathBranch(int x, int y, int visitMark, List<Point> path, Point origin) {
            for (int nextX = x - 1; nextX <= x + 1; nextX++) {
                if (nextX < 0 || nextX >= PATH_MAP_WIDTH) {
                    continue;
                }
                for (int nextY = y - 1; nextY <= y + 1; nextY++) {
                    if (nextY < 0 || nextY >= PATH_MAP_HEIGHT || (nextX == x && nextY == y)) {
                        continue;
                    }
                    int pathPixel = pathPixel(nextX, nextY);
                    if (pathPixel == PATH_PIXEL && visitedMark(nextX, nextY) == 0) {
                        markVisited(nextX, nextY, visitMark);
                        path.add(new Point(nextX, nextY));
                        traceNextPathBranch(nextX, nextY, visitMark + 1, path, origin);
                        return;
                    }
                    if (pathPixel == NODE_PIXEL && (nextX != origin.x || nextY != origin.y)) {
                        path.add(new Point(nextX, nextY));
                        return;
                    }
                }
            }
        }

        /**
         * Native support extracted from CArray<CPoint>::InsertAtArray @00472930 CArray<CPoint> transfer/copy.
         */
        private static List<Point> copyPointList(List<Point> points) {
            List<Point> copy = new ArrayList<>(points.size());
            for (Point point : points) {
                copy.add(new Point(point));
            }
            return copy;
        }

        /**
         * Native support extracted from the reversed CArray<CPoint> copy in
         * GlobalMapLocationGraph::connectLocationGraphEdges @00471F0D.
         */
        private static List<Point> reverseCopyPointList(List<Point> points) {
            List<Point> copy = new ArrayList<>(points.size());
            for (int index = points.size() - 1; index >= 0; index--) {
                copy.add(new Point(points.get(index)));
            }
            return copy;
        }

        /**
         * Native support extracted from path-map pixel reads in GlobalMapLocationGraph::CreateLocationGraph @00471A75.
         */
        private int pathPixel(int x, int y) {
            return Byte.toUnsignedInt(pathPixels[pathPixelIndex(x, y)]);
        }

        /**
         * Native support extracted from the transient `short[640][480]` visit-grid writes in
         * GlobalMapLocationGraph::CreateLocationGraph @00471A75 and
         * GlobalMapLocationGraph::traceNextPathBranch @004721FF.
         */
        private void markVisited(int x, int y, int mark) {
            visitGrid[visitGridIndex(x, y)] = (short) mark;
        }

        /**
         * Native support extracted from the transient `short[640][480]` visit-grid reads in
         * GlobalMapLocationGraph::traceNextPathBranch @004721FF.
         */
        private int visitedMark(int x, int y) {
            return Short.toUnsignedInt(visitGrid[visitGridIndex(x, y)]);
        }

        /**
         * Native support extracted from `x * 0x3C0 + y * 2` visit-grid addressing in
         * GlobalMapLocationGraph::traceNextPathBranch @004721FF.
         */
        private static int visitGridIndex(int x, int y) {
            return x * PATH_MAP_HEIGHT + y;
        }
    }

    /**
     * Native: GlobalMapDialogVisualObject::GlobalMapDialogVisualObject @0046E52F.
     * Fully ported.
     */
    public GlobalMapDialogVisualObject() {
        super();
        initialize();
    }

    /**
     * Native: GlobalMapDialogVisualObject::GlobalMapDialogVisualObject @0046E66B.
     * Fully ported.
     */
    public GlobalMapDialogVisualObject(int id, int xLeft, int yTop, int xRight, int yBottom, Object handler) {
        super(id, xLeft, yTop, xRight, yBottom, handler);
        initialize();
    }

    /**
     * Native: GlobalMapDialogVisualObject::GlobalMapDialogVisualObject @0046E7C1.
     * Fully ported.
     */
    public GlobalMapDialogVisualObject(int id, CRect rect, Object handler) {
        super(id, rect, handler);
        initialize();
    }

    /**
     * Native: GlobalMapDialogVisualObject::RebuildScenarioLocations @0046EB3A.
     * Fully ported with PopulateScenarioLocationFlags @0046EB4D folded into this Java method. Native appends all
     * scenario top-left points and availability words; it does not clear existing arrays or populate locationHitRects.
     */
    public void rebuildScenarioLocations() {
        for (ScenarioLocation location : Globals.scenarioLib.getAllLocations()) {
            Point point = new Point(location.rect.left, location.rect.top);
            locationPoints.add(point);
            locationAvailabilityFlags.add(location.rect.right == 0 && location.rect.bottom == 0 ? 1 : 0);
        }
    }

    /**
     * Native: GlobalMapDialogVisualObject::SetTravelOrigin @00493B90, called by
     * CMainWindow::WindowProc @004852D8 in the SHOW_GLOBAL_MAP_DIALOG path.
     * Fully ported.
     */
    public void setTravelOrigin(int x, int y) {
        currentLocationPoint.setLocation(x, y);
    }

    /**
     * vtbl +0x14: GlobalMapDialogVisualObject::GetText @00471017.
     * Fully ported.
     */
    @Override
    public String getText() {
        if (renderActiveFlag == 0 || !partyDetailsRect.isRectNull()) {
            return null;
        }

        Point localPoint = toLocalDialogPoint(Globals.mousePointer.getX(), Globals.mousePointer.getY());
        for (int i = 0; i < locationHitRects.size(); i++) {
            CRect rect = locationHitRects.get(i);
            if (!rect.contains(localPoint.x, localPoint.y)) {
                continue;
            }

            if (locationAvailabilityFlags.get(i) == 0) {
                return null;
            }
            return getGlobalMapTextAt(i);
        }
        return null;
    }

    /**
     * vtbl +0x2C: GlobalMapDialogVisualObject::Update @0046FDD5.
     * Fully ported.
     */
    @Override
    public void update() {
        if (renderActiveFlag == 0) {
            return;
        }

        drawBackground();
        if (umoirMapMode == 0) {
            if (travelRoutePoints.isEmpty() && travelProgress == 0) {
                updateHoveredLocation();
            }
        } else if (travelProgress < ROUTE_ANIMATION_STEP) {
            primeFirstAvailableLocationTravel();
        }

        boolean travelCompleted;
        if (travelProgress == 0) {
            drawRouteDots(travelRoutePoints.size());
            drawAvailableLocations();
            travelCompleted = false;
        } else {
            travelCompleted = drawTravelAnimation();
        }

        advanceFlagAnimation();
        drawPartyLocation();
        drawHoveredLocationText();

        super.update();
        if (travelCompleted) {
            enterTargetLocationAndContinue();
        }
        resetMapCursor(Globals.mousePointer.getX(), Globals.mousePointer.getY());
    }

    /**
     * vtbl +0x30: GlobalMapDialogVisualObject::RenderSelf @004707F6.
     * Fully ported.
     */
    @Override
    public void renderSelf(CRect clipRect) {
        // Native no-op.
    }

    /**
     * vtbl +0x48: GlobalMapDialogVisualObject::OnMessage @004715A5.
     * Fully ported. Native returns void; Java returns the superclass result for the shared handler signature.
     */
    @Override
    public int onMessage(MessageCodes msg, Object wParam, Object lParam) {
        if ((renderThrottleInitialized & 1) == 0) {
            renderThrottleInitialized |= 1;
            lastRenderFrameTick = currentTick() - RENDER_FRAME_CADENCE_MS;
        }
        if (msg == RENDER_FRAME && currentTick() - lastRenderFrameTick > 99) {
            draw();
            lastRenderFrameTick = currentTick();
        }
        return super.onMessage(msg, wParam, lParam);
    }

    /**
     * vtbl +0x4C: GlobalMapDialogVisualObject::OnMouseMove @00471679.
     * Fully ported. Native returns void; Java returns 0 for the shared event-handler signature.
     */
    @Override
    public int onMouseMove(int nFlags, int x, int y) {
        if (travelProgress == 0) {
            updateHoveredLocation();
        }
        return resetMapCursor(x, y);
    }

    /**
     * vtbl +0x54: GlobalMapDialogVisualObject::OnLButtonDown @00471918.
     * Fully ported.
     */
    @Override
    public int onLButtonDown(int nFlags, int x, int y) {
        if (travelProgress == 0) {
            travelProgress = ROUTE_ANIMATION_STEP;
        } else {
            completeTravelAnimation();
        }
        return 1;
    }

    /**
     * vtbl +0x58: GlobalMapDialogVisualObject::OnLButtonUp @004737F0.
     * Fully ported.
     */
    @Override
    public int onLButtonUp(int nFlags, int x, int y) {
        return 0;
    }

    /**
     * vtbl +0x60: GlobalMapDialogVisualObject::OnRButtonDown @00471957.
     * Fully ported.
     */
    @Override
    public int onRButtonDown(int nFlags, int x, int y) {
        CRect heroRect = new CRect(heroDrawPoint, heroBitmapSize);
        Point localPoint = toLocalDialogPoint(x, y);
        if (heroRect.contains(localPoint.x, localPoint.y)) {
            int unitCount = Math.max(2, SelectedUnitsSnapshot.GLOBAL.getPrimaryUnits().size());
            partyDetailsRect.set(100, 100, 0x15E, unitCount * 0x14 + 0x78);
        }
        return 1;
    }

    /**
     * vtbl +0x64: GlobalMapDialogVisualObject::OnRButtonUp @00471A55.
     * Fully ported.
     */
    @Override
    public int onRButtonUp(int nFlags, int x, int y) {
        partyDetailsRect.set(0, 0, 0, 0);
        return 1;
    }

    /**
     * vtbl +0x6C: GlobalMapDialogVisualObject::OnKeyDown @00470FD4.
     * Fully ported.
     */
    @Override
    public int onKeyDown(int nChar) {
        completeTravelAnimation();
        return 1;
    }

    /**
     * vtbl +0x74: GlobalMapDialogVisualObject::OnChar @00470FBA.
     * Fully ported.
     */
    @Override
    public int onChar(int nChar) {
        completeTravelAnimation();
        return 1;
    }

    /**
     * vtbl +0x78: GlobalMapDialogVisualObject::InitializeGlobalMapDialog @0046EC0B.
     * Fully ported. Constructor-owned CArray/CString defaults are represented by Java field initializers; this native
     * slot writes pointer/default fields and adds the hidden close button.
     */
    @Override
    public void initialize() {
        heroBitmap = null;
        routeAdjacencyMatrix = null;
        routeDotBitmap = null;
        travelStartFlagSprite = null;
        currentLocationFlagSprite = null;
        targetCrossSprite = null;
        availableLocationFlagSprite = null;
        partyLocationFlagSprite = null;
        scrollTopBitmap = null;
        scrollPanelTopBitmap = null;
        mapBackgroundBitmap = null;
        scrollBottomBitmap = null;
        missionLocationFlagSprite = null;
        scrollPanelMiddleBitmap = null;
        scrollUpSound = null;
        routePointSound1 = null;
        routePointSound2 = null;
        scrollMiddleBitmap = null;
        scrollPanelBottomBitmap = null;
        scrollDownSound = null;
        partyDetailsRect.set(0, 0, 0, 0);
        mapFlagAnimationFrame = 0;
        travelProgress = 0;
        hoveredLocationIndex = -1;
        heroBitmapSize.setSize(10, 0x14);
        closeButton = new CommandButtonVisualObject(
                4,
                0x21C,
                0x1C2,
                0x21C,
                0x1C2,
                " ",
                Globals.fonts.font1,
                Palettes.grayDim,
                HIDDEN_CLOSE_BUTTON_COMMAND,
                0,
                null
        );
        addChild(closeButton);
    }

    /**
     * vtbl +0x80: GlobalMapDialogVisualObject::ShowDialog @0046FA4B.
     * Fully ported. Java managed lifetime replaces native temporary GlobalMapLocationGraph delete.
     */
    @Override
    public void showDialog() {
        Globals.mousePointer.disableBackgroundCapture();
        loadMapAssets();
        loadDialogSounds();
        loadGlobalMapText();
        routePointSoundIndex = 0;

        LocationGraph locationGraph = createLocationGraph();
        graphNodePoints.clear();
        routeAdjacencyMatrix = locationGraph.transferAdjacencyMatrix(graphNodePoints);
        releaseLocationGraph(locationGraph);

        partyFlagAnimationFrame = 0;
        targetCrossAnimationFrame = 0;
        locationMetadata.clear();
        clearScreen();
        super.showDialog();
        CMousePointer.Cursor_Select.setToMousePointer();
        renderActiveFlag = 1;
        Globals.mousePointer.enableBackgroundCapture();
    }

    /**
     * vtbl +0x84: GlobalMapDialogVisualObject::HideDialog @0046FC55.
     * Fully ported. Java managed lifetime replaces native route adjacency matrix delete loops.
     */
    @Override
    public HandlerVisualObject hideDialog(MessageCodes reason) {
        renderActiveFlag = 0;
        releaseMapAssets();
        releaseDialogSounds();
        routeAdjacencyMatrix = null;
        graphNodePoints.clear();
        travelRoutePoints.clear();
        return super.hideDialog(reason);
    }

    /**
     * Native: GlobalMapDialogVisualObject::CompleteTravelAnimation @00470F72.
     * Fully ported.
     */
    private void completeTravelAnimation() {
        if (travelProgress != 0) {
            travelProgress = travelRoutePoints.size() + 1;
            targetCrossAnimationFrame = frameCount(targetCrossSprite) + 1;
        }
    }

    /**
     * Native: GlobalMapDialogVisualObject::UpdateHoveredLocation @004716AA.
     * Fully ported.
     */
    private void updateHoveredLocation() {
        ScenarioLocation nearestLocation = resolveNearestAvailableLocation();
        if (nearestLocation == null) {
            return;
        }

        Point previousLocation = new Point(targetLocationPoint);
        targetLocationPoint.setLocation(nearestLocation.rect.left, nearestLocation.rect.top);
        if (!samePoint(previousLocation, targetLocationPoint)) {
            buildRouteSelection(
                    currentLocationPoint.x,
                    currentLocationPoint.y,
                    targetLocationPoint.x,
                    targetLocationPoint.y
            );
        }
        refreshHoveredLocationText(nearestLocation);
    }

    /**
     * Native: GlobalMapDialogVisualObject::ResetMapCursor @0047162B.
     * Fully ported. Native subtracts the dialog top-left from the passed point, but the adjusted point is unused.
     */
    private int resetMapCursor(int x, int y) {
        CMousePointer.Cursor_Select.setToMousePointer();
        return 0;
    }

    /**
     * Native: GlobalMapDialogVisualObject::LoadDialogSounds @0046EDF2.
     * Fully ported. Native per-slot Sound::LoadSound starts with DeleteSound; Java performs the same cleanup through
     * ReleaseDialogSounds before assigning managed Sound instances.
     */
    private void loadDialogSounds() {
        releaseDialogSounds();
        scrollUpSound = loadSound(SCROLL_UP_WAV);
        scrollDownSound = loadSound(SCROLL_DOWN_WAV);
        routePointSound1 = loadSound(POINT1_WAV);
        routePointSound2 = loadSound(POINT2_WAV);
    }

    /**
     * Native: GlobalMapDialogVisualObject::ReleaseDialogSounds @0046EE5F.
     * Fully ported. DeleteSound's stop-and-clear pointer semantics are represented by SoundSystem.releaseSound and
     * assigning each field to null.
     */
    private void releaseDialogSounds() {
        scrollUpSound = releaseSound(scrollUpSound);
        scrollDownSound = releaseSound(scrollDownSound);
        routePointSound1 = releaseSound(routePointSound1);
        routePointSound2 = releaseSound(routePointSound2);
    }

    /**
     * Native: GlobalMapDialogVisualObject::LoadMapAssets @0046EEB0.
     * Fully ported. Java managed allocation replaces native bitmap/sprite AfxAllocMemory branches while preserving
     * native mouse-pointer refreshes between loads.
     */
    private void loadMapAssets() {
        releaseMapAssets();
        mapBackgroundBitmap = loadBmp64k(umoirMapMode == 0 ? GLOBAL_MAP_BMP : UMOIR_BMP);
        Globals.mousePointer.update();
        travelStartFlagSprite = loadA16Sprite(FLAG1_SPRITES);
        Globals.mousePointer.update();
        currentLocationFlagSprite = loadA16Sprite(FLAG_SPRITES);
        Globals.mousePointer.update();
        targetCrossSprite = loadA16Sprite(CROSS_SPRITES);
        Globals.mousePointer.update();
        routeDotBitmap = loadBmp64k(BALL_MAP_BMP);
        Globals.mousePointer.update();
        heroBitmap = loadBmp64k(HERO_BMP);
        Globals.mousePointer.update();
        heroBitmapSize.setSize(heroBitmap.xSizeOf(0), heroBitmap.ySizeOf(0));
        availableLocationFlagSprite = loadA16Sprite(FLAG_ON_MAP_SPRITES);
        Globals.mousePointer.update();
        missionLocationFlagSprite = loadA16Sprite(MISSION_FLAG_SPRITES);
        Globals.mousePointer.update();
        partyLocationFlagSprite = loadA16Sprite(YOUR_FLAG_SPRITES);
        Globals.mousePointer.update();
        scrollTopBitmap = loadBmp64k(SCROLL_01_BMP);
        Globals.mousePointer.update();
        scrollMiddleBitmap = loadBmp64k(SCROLL_02_BMP);
        Globals.mousePointer.update();
        scrollBottomBitmap = loadBmp64k(SCROLL_03_BMP);
        Globals.mousePointer.update();
        scrollPanelTopBitmap = loadBmp64k(SCROLL_P1_BMP);
        Globals.mousePointer.update();
        scrollPanelBottomBitmap = loadBmp64k(SCROLL_P3_BMP);
        Globals.mousePointer.update();
        scrollPanelMiddleBitmap = loadBmp64k(SCROLL_P2_BMP);
        Globals.mousePointer.update();
    }

    /**
     * Native: GlobalMapDialogVisualObject::ReleaseMapAssets @0046F5C7.
     * Fully ported. Java managed lifetime replaces native bitmap/sprite destructor calls.
     */
    private void releaseMapAssets() {
        mapBackgroundBitmap = null;
        heroBitmap = null;
        routeDotBitmap = null;
        travelStartFlagSprite = null;
        currentLocationFlagSprite = null;
        targetCrossSprite = null;
        scrollTopBitmap = null;
        scrollMiddleBitmap = null;
        scrollBottomBitmap = null;
        scrollPanelTopBitmap = null;
        scrollPanelBottomBitmap = null;
        scrollPanelMiddleBitmap = null;
        partyLocationFlagSprite = null;
        availableLocationFlagSprite = null;
        missionLocationFlagSprite = null;
    }

    /**
     * Native: GlobalMapDialogVisualObject::BuildRouteSelection @0047110E.
     * Fully ported. Native temporary GlobalMapRouteNodeList allocation is represented by the Java candidate route
     * list and the explicit routeCost parameter in findBestRoute.
     */
    private void buildRouteSelection(int startX, int startY, int endX, int endY) {
        if (startX == endX && startY == endY) {
            travelRoutePoints.clear();
            travelRoutePoints.add(new Point(startX, startY));
            travelRoutePoints.add(new Point(endX, endY));
            return;
        }

        int startNodeIndex = findGraphNodeIndex(startX, startY);
        int endNodeIndex = findGraphNodeIndex(endX, endY);
        routeNodeIndices.clear();
        bestRouteCost = 2_000_000_000;

        List<Integer> candidateRoute = new ArrayList<>();
        candidateRoute.add(startNodeIndex);
        findBestRoute(startNodeIndex, endNodeIndex, candidateRoute, 0);

        travelRoutePoints.clear();
        for (int routeIndex = 1; routeIndex < routeNodeIndices.size(); routeIndex++) {
            int fromNodeIndex = routeNodeIndices.get(routeIndex - 1);
            int toNodeIndex = routeNodeIndices.get(routeIndex);
            travelRoutePoints.addAll(routeEdge(routeAdjacencyMatrix, fromNodeIndex, toNodeIndex));
        }
    }

    /**
     * Native support boundary for GlobalMapDialogVisualObject::ShowDialog @0046FA4B graph allocation before
     * GlobalMapLocationGraph::CreateLocationGraph @00471A75.
     */
    private LocationGraph createLocationGraph() {
        return new LocationGraph(this);
    }

    /**
     * Native support boundary for GlobalMapLocationGraph cleanup after
     * GlobalMapLocationGraph::CreateLocationGraph @00471A75 in ShowDialog @0046FA4B.
     * Covers GlobalMapLocationGraph::~GlobalMapLocationGraph @00471CAF,
     * GlobalMapLocationGraph::ReleasePathMapBitmap @00471EC2, and
     * GlobalMapLocationGraph::scalar_deleting_destructor @00472680.
     */
    private void releaseLocationGraph(LocationGraph locationGraph) {
    }

    /**
     * Native support boundary for the `umoirMapMode != 0 && travelProgress < 8` branch in
     * GlobalMapDialogVisualObject::Update @0046FDD5.
     */
    private void primeFirstAvailableLocationTravel() {
        List<ScenarioLocation> availableLocations = Globals.scenarioLib.getAvailableLocations();
        ScenarioLocation firstLocation = availableLocations.getFirst();
        targetLocationPoint.setLocation(firstLocation.rect.left, firstLocation.rect.top);
        buildRouteSelection(currentLocationPoint.x, currentLocationPoint.y, targetLocationPoint.x, targetLocationPoint.y);
        travelProgress = ROUTE_ANIMATION_STEP;
    }

    /**
     * Native support boundary for ScenarioGetAvailableLocations iteration in
     * GlobalMapDialogVisualObject::UpdateHoveredLocation @004716AA.
     */
    private ScenarioLocation resolveNearestAvailableLocation() {
        Point localPoint = toLocalDialogPoint(Globals.mousePointer.getX(), Globals.mousePointer.getY());
        ScenarioLocation nearestLocation = null;
        int nearestDistanceSquared = Integer.MAX_VALUE;

        for (ScenarioLocation location : Globals.scenarioLib.getAvailableLocations()) {
            int dx = location.rect.left - localPoint.x;
            int dy = location.rect.top - localPoint.y;
            int distanceSquared = dx * dx + dy * dy;
            if (distanceSquared < nearestDistanceSquared) {
                nearestDistanceSquared = distanceSquared;
                nearestLocation = location;
            }
        }

        return nearestLocation;
    }

    /**
     * Native support extracted from the FUN_004DDEC6 / FUN_004DDFF8 text formatting calls in
     * GlobalMapDialogVisualObject::UpdateHoveredLocation @004716AA.
     */
    private void refreshHoveredLocationText(ScenarioLocation location) {
        String titleText = getGlobalMapTitleText(location);
        hoveredLocationTitle = titleText.substring(0, titleText.length() - 2);
        hoveredLocationLines.clear();
        hoveredLocationLines.addAll(Globals.fonts.font2.formatText(
                new CRect(0x46, 0x30, 0x186, 200),
                getGlobalMapDescriptionText(location)
        ));
    }

    /**
     * Native: GlobalMapDialogVisualObject::FindBestRoute @004713C2.
     * Fully ported. Native GlobalMapRouteNodeList +0x14 routeCost is carried as the Java routeCost parameter;
     * the native route-list contains helper @00471372 is represented by List.contains.
     */
    private void findBestRoute(int currentNodeIndex, int targetNodeIndex, List<Integer> candidateRoute, int routeCost) {
        if (currentNodeIndex == targetNodeIndex) {
            if (routeCost < bestRouteCost) {
                bestRouteCost = routeCost;
                routeNodeIndices.clear();
                routeNodeIndices.addAll(candidateRoute);
            }
            return;
        }

        for (int nextNodeIndex = 0; nextNodeIndex < graphNodePoints.size(); nextNodeIndex++) {
            if (nextNodeIndex == currentNodeIndex || candidateRoute.contains(nextNodeIndex)) {
                continue;
            }
            List<Point> edge = routeEdge(routeAdjacencyMatrix, currentNodeIndex, nextNodeIndex);
            if (edge == null || routeCost + edge.size() >= bestRouteCost) {
                continue;
            }
            candidateRoute.add(nextNodeIndex);
            findBestRoute(nextNodeIndex, targetNodeIndex, candidateRoute, routeCost + edge.size());
            candidateRoute.remove(candidateRoute.size() - 1);
        }
    }

    /**
     * Native support extracted from graph-node CPoint equality checks in
     * GlobalMapDialogVisualObject::BuildRouteSelection @0047110E.
     */
    private int findGraphNodeIndex(int x, int y) {
        return indexOfGraphNode(graphNodePoints, new Point(x, y));
    }

    /**
     * Native support extracted from graph-node CPoint equality checks in
     * GlobalMapLocationGraph::connectLocationGraphEdges @00471F0D.
     */
    private static int indexOfGraphNode(List<Point> nodes, Point point) {
        int nodeIndex = indexOfGraphNodeOrMinusOne(nodes, point);
        if (nodeIndex != -1) {
            return nodeIndex;
        }
        throw new IllegalStateException("Global-map path graph is missing node " + point);
    }

    /**
     * Native support extracted from graph-node CPoint equality checks in
     * GlobalMapLocationGraph::connectLocationGraphEdges @00471F0D.
     */
    private static int indexOfGraphNodeOrMinusOne(List<Point> nodes, Point point) {
        for (int nodeIndex = 0; nodeIndex < nodes.size(); nodeIndex++) {
            if (samePoint(nodes.get(nodeIndex), point)) {
                return nodeIndex;
            }
        }
        return -1;
    }

    /**
     * Native support extracted from CArray<CPoint>::Append @004728B0 route-point append.
     */
    @SuppressWarnings("unchecked")
    private static List<Point> routeEdge(Object[][] adjacencyMatrix, int fromNodeIndex, int toNodeIndex) {
        return (List<Point>) adjacencyMatrix[fromNodeIndex][toNodeIndex];
    }

    /**
     * Native: pathPixelIndex @00471C95.
     * Fully ported.
     */
    private static int pathPixelIndex(int x, int y) {
        return x + y * PATH_MAP_WIDTH;
    }

    /**
     * Native support: background draw call at the top of GlobalMapDialogVisualObject::Update @0046FDD5.
     */
    private void drawBackground() {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        mapBackgroundBitmap.draw(screenRect.left, screenRect.top, 0, 0, false);
    }

    /**
     * Native support extracted from the route-dot draw loops in GlobalMapDialogVisualObject::Update @0046FDD5.
     */
    private void drawRouteDots(int exclusiveLimit) {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        int limit = Math.min(exclusiveLimit, travelRoutePoints.size());
        int halfWidth = routeDotBitmap.xSizeOf(0) / 2;
        int halfHeight = routeDotBitmap.ySizeOf(0) / 2;
        for (int i = 0; i < limit; i++) {
            if ((i & 7) != 0) {
                continue;
            }
            Point point = travelRoutePoints.get(i);
            routeDotBitmap.drawRectMasked(screenRect.left + point.x - halfWidth, screenRect.top + point.y - halfHeight);
        }
    }

    /**
     * Native support extracted from ScenarioGetAvailableLocations flag drawing in GlobalMapDialogVisualObject::Update @0046FDD5.
     */
    private void drawAvailableLocations() {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        for (ScenarioLocation location : Globals.scenarioLib.getAvailableLocations()) {
            int x = location.rect.left;
            int y = location.rect.top;
            if (umoirMapMode != 0) {
                drawSprite(travelStartFlagSprite, screenRect.left + x - 4, screenRect.top + y - 0x20, mapFlagAnimationFrame);
            } else if (!samePoint(currentLocationPoint, x, y)) {
                if (samePoint(targetLocationPoint, x, y)) {
                    drawSprite(missionLocationFlagSprite, screenRect.left + x - 5, screenRect.top + y - 0x29, mapFlagAnimationFrame);
                } else {
                    drawSprite(availableLocationFlagSprite, screenRect.left + x - 5, screenRect.top + y - 0x25, mapFlagAnimationFrame);
                }
            }
        }
    }

    /**
     * Native support extracted from the active travel branch in GlobalMapDialogVisualObject::Update @0046FDD5.
     */
    private boolean drawTravelAnimation() {
        int visibleRouteLimit = Math.min(travelProgress, travelRoutePoints.size() - 1);
        drawRouteDots(visibleRouteLimit);
        drawTargetCross();
        targetCrossAnimationFrame++;
        travelProgress += ROUTE_ANIMATION_STEP;

        if (travelProgress < travelRoutePoints.size() - 1) {
            playRoutePointSound();
        }
        if (travelProgress <= travelRoutePoints.size()) {
            return false;
        }
        if (targetCrossAnimationFrame <= frameCount(targetCrossSprite) + 1) {
            return false;
        }

        currentLocationPoint.setLocation(targetLocationPoint);
        travelProgress = 0;
        travelRoutePoints.clear();
        return true;
    }

    /**
     * Native support extracted from Cross sprite drawing in GlobalMapDialogVisualObject::Update @0046FDD5.
     */
    private void drawTargetCross() {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        int frame = Math.min(targetCrossAnimationFrame, frameCount(targetCrossSprite) - 1);
        drawSprite(targetCrossSprite, screenRect.left + targetLocationPoint.x - 10, screenRect.top + targetLocationPoint.y - 0x0C, frame);
    }

    /**
     * Native support extracted from Sound::PlayPointer route-point playback in GlobalMapDialogVisualObject::Update @0046FDD5.
     */
    private void playRoutePointSound() {
        Sound[] sounds = {routePointSound1, routePointSound2};
        Sound currentSound = sounds[routePointSoundIndex & 1];
        if (currentSound != null && currentSound.isPlaying()) {
            return;
        }
        routePointSoundIndex = (routePointSoundIndex + 1) & 1;
        Sound.playPointer(sounds, routePointSoundIndex);
    }

    /**
     * Native support extracted from flag-frame advancement in GlobalMapDialogVisualObject::Update @0046FDD5.
     */
    private void advanceFlagAnimation() {
        mapFlagAnimationFrame = nextFrame(mapFlagAnimationFrame, travelStartFlagSprite);
    }

    /**
     * Native support extracted from current-party flag drawing in GlobalMapDialogVisualObject::Update @0046FDD5.
     */
    private void drawPartyLocation() {
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        if (umoirMapMode == 0) {
            heroDrawPoint.setLocation(currentLocationPoint.x - 0x14, currentLocationPoint.y - 0x2C);
            drawSprite(
                    partyLocationFlagSprite,
                    screenRect.left + heroDrawPoint.x,
                    screenRect.top + heroDrawPoint.y,
                    partyFlagAnimationFrame
            );
        } else {
            heroDrawPoint.setLocation(currentLocationPoint.x - 10, currentLocationPoint.y - 0x18);
            drawSprite(
                    currentLocationFlagSprite,
                    screenRect.left + heroDrawPoint.x,
                    screenRect.top + heroDrawPoint.y,
                    partyFlagAnimationFrame
            );
        }
        partyFlagAnimationFrame = nextFrame(partyFlagAnimationFrame, currentLocationFlagSprite);
    }

    /**
     * Native support extracted from gFont4/gFont2 hover text drawing in GlobalMapDialogVisualObject::Update @0046FDD5.
     */
    private void drawHoveredLocationText() {
        if (umoirMapMode != 0 || hoveredLocationTitle.isEmpty()) {
            return;
        }
        CRect screenRect = new CRect();
        clientToScreen(screenRect, cRect);
        Globals.fonts.font4.drawTextInternal(
                screenRect.left + 0xE6,
                screenRect.top + 0x1B,
                hoveredLocationTitle,
                TextAlign.CENTER.mask,
                Palettes.p4.paletteData[0]
        );
        int lastLineIndex = hoveredLocationLines.size() - 1;
        for (int lineIndex = 0; lineIndex < lastLineIndex; lineIndex++) {
            Globals.fonts.font2.drawTextInternal(
                    screenRect.left + 0xE6,
                    screenRect.top + 0x30 + lineIndex * 10,
                    hoveredLocationLines.get(lineIndex),
                    TextAlign.CENTER.mask,
                    Palettes.hover
            );
        }
        String lastLine = hoveredLocationLines.get(lastLineIndex);
        Globals.fonts.font2.drawTextInternal(
                screenRect.left + 0xE6,
                screenRect.top + 0x30 + lastLineIndex * 10,
                lastLine.substring(0, lastLine.length() - 2),
                TextAlign.CENTER.mask,
                Palettes.hover
        );
    }

    /**
     * Native support extracted from ScenarioEnterLocation and CONTINUE_SCENARIO_LOCATION_ENTRY tail in
     * GlobalMapDialogVisualObject::Update @0046FDD5.
     */
    private void enterTargetLocationAndContinue() {
        closeAfterTravel();
        Globals.mainWindow.postMessage(CONTINUE_SCENARIO_LOCATION_ENTRY, 0, 0);
        if (umoirMapMode == 0) {
            ScenarioLocation selectedLocation = findAvailableLocationAt(targetLocationPoint);
            if (selectedLocation != null) {
                Globals.scenarioLib.enterLocation(selectedLocation);
            }
        } else {
            Globals.scenarioLib.enterLocation(firstAvailableLocation());
        }
    }

    /**
     * Native support extracted from ScenarioGetAvailableLocations point matching in GlobalMapDialogVisualObject::Update @0046FDD5.
     */
    private ScenarioLocation findAvailableLocationAt(Point point) {
        for (ScenarioLocation location : Globals.scenarioLib.getAvailableLocations()) {
            if (samePoint(point, location.rect.left, location.rect.top)) {
                return location;
            }
        }
        return null;
    }

    /**
     * Native support extracted from the first ScenarioGetAvailableLocations entry path in GlobalMapDialogVisualObject::Update @0046FDD5.
     */
    private ScenarioLocation firstAvailableLocation() {
        return Globals.scenarioLib.getAvailableLocations().getFirst();
    }

    /**
     * Native: GlobalMapDialogVisualObject::CloseAfterTravel @00470FEE.
     * Fully ported. Native calls AfxGetMainWnd and ignores the result before sending DIALOG_OK to this dialog.
     */
    private void closeAfterTravel() {
        onMessage(MessageCodes.DIALOG_OK, 0, 0);
    }

    /**
     * Native support: `LoadTextFileToOEM("main/text/globalmap.txt", &g_strScriptData)` in
     * GlobalMapDialogVisualObject::ShowDialog @0046FA4B.
     */
    private void loadGlobalMapText() {
        ScriptDataSupport.loadGlobalMapScriptData();
    }

    /**
     * Native support: `CTextFile::GetAt(&g_globalMapTextFile, index)` in GlobalMapDialogVisualObject::GetText @00471017.
     */
    private String getGlobalMapTextAt(int index) {
        return ScriptDataSupport.scriptData.get(index);
    }

    /**
     * Native support call site for GetGlobalMapLocationTitleText @004DDEC6.
     */
    private static String getGlobalMapTitleText(ScenarioLocation location) {
        return ScriptDataSupport.getGlobalMapLocationTitleText(location.kind, location.id);
    }

    /**
     * Native support call site for GetGlobalMapLocationDescriptionText @004DDFF8.
     */
    private static String getGlobalMapDescriptionText(ScenarioLocation location) {
        return ScriptDataSupport.getGlobalMapLocationDescriptionText(location.kind, location.id);
    }

    /**
     * Native support thunk: FUN_004384F0 @004384F0.
     */
    private static Sound loadSound(String resourcePath) {
        return new Sound(resourcePath);
    }

    /**
     * Native support thunk: FUN_00438480 @00438480.
     */
    private static Sound releaseSound(Sound sound) {
        if (sound != null) {
            SoundSystem.get().releaseSound(sound);
        }
        return null;
    }

    /**
     * Java helper for BMP loads in GlobalMapDialogVisualObject own methods.
     * not ported.
     */
    private static CBmp64k loadBmp64k(String resourcePath) {
        return new CBmp64k(Resources.path(resourcePath));
    }

    /**
     * Java helper for CA16 sprite loads in GlobalMapDialogVisualObject own methods.
     * not ported.
     */
    private static CA16 loadA16Sprite(String resourcePath) {
        CA16 sprite = new CA16(Resources.path(resourcePath));
        sprite.initPalette(0x10, 4, 0);
        return sprite;
    }

    /**
     * Java helper for `timeGetTime` call sites in GlobalMapDialogVisualObject own methods.
     * not ported.
     */
    private static int currentTick() {
        return (int) System.currentTimeMillis();
    }

    /**
     * Java helper for CGameBitmap::GetFrameCount call sites in GlobalMapDialogVisualObject own methods.
     * not ported.
     */
    private static int frameCount(CGameBitmap bitmap) {
        return bitmap.frameCount;
    }

    /**
     * Java helper for frame-index wrapping in GlobalMapDialogVisualObject own methods.
     * not ported.
     */
    private static int nextFrame(int frame, CGameBitmap bitmap) {
        return (frame + 1) % frameCount(bitmap);
    }

    /**
     * Java helper for sprite draw call sites in GlobalMapDialogVisualObject own methods.
     * not ported.
     */
    private static void drawSprite(CA16 sprite, int x, int y, int frame) {
        sprite.draw(x, y, Math.floorMod(frame, frameCount(sprite)), 0, false);
    }

    /**
     * Native support extracted from CPoint::IsEqual @00472700 and CPoint::NotEqual @00472740 call sites.
     */
    private static boolean samePoint(Point left, Point right) {
        return left.x == right.x && left.y == right.y;
    }

    /**
     * Native support extracted from CPoint::IsEqual @00472700 and CPoint::NotEqual @00472740 call sites.
     */
    private static boolean samePoint(Point left, int x, int y) {
        return left.x == x && left.y == y;
    }

    /**
     * Java helper for dialog-local point conversion in GlobalMapDialogVisualObject own methods.
     * not ported.
     */
    private Point toLocalDialogPoint(int x, int y) {
        return new Point(x - cRect.left, y - cRect.top);
    }
}
